# LV AJAX Handler Best Practices Skill

> **Always read `md_files/ajax.md` alongside this file** before writing any LV AJAX handler.
> This file captures *how* to write good AJAX handlers — `ajax.md` is the authoritative API reference.

---

## 1. Class Naming and Package Structure

- Place AJAX handler classes in a `com.<client>.ajax.<module>` package that names both the client and the functional area.
- The class name should be a clear verb-noun pair describing the operation: `GetSampleDetails`, `AssignSampleLocation`, `BulkStatusUpdate`.
- Extend `BaseAjaxRequest` directly — do **not** extend another custom AJAX class.
- No registration required: the framework resolves the class at runtime by fully-qualified class name.

```java
package com.acme.ajax.sample;   // com.<client>.ajax.<module>

import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetSampleDetails extends BaseAjaxRequest {

    @Override
    public void processRequest(HttpServletRequest request,
                               HttpServletResponse response,
                               ServletContext servletContext) throws ServletException {
        // ...
    }
}
```

**Why:** Package naming by client+module makes handlers discoverable. Clear class names serve as documentation at the JavaScript call site.

---

## 2. The processRequest Skeleton — Always try/catch/finally

Every `processRequest` body must follow this exact skeleton. `ajaxResponse.print()` **must** be called unconditionally — it is the only way JSON is written to the HTTP response.

```java
@Override
public void processRequest(HttpServletRequest request,
                           HttpServletResponse response,
                           ServletContext servletContext) throws ServletException {

    AjaxResponse ajaxResponse = new AjaxResponse(request, response);

    try {
        // 1. Read parameters
        // 2. Validate
        // 3. Execute logic
        // 4. Write response arguments

    } catch (Exception e) {
        this.logError("MyHandler failed", e);
        ajaxResponse.setError(e.getMessage(), e);
    } finally {
        ajaxResponse.print();   // unconditional — even on error
    }
}
```

**Never** call `ajaxResponse.print()` only in the `try` block. If an exception is thrown before it, the HTTP response gets no body and the JavaScript callback receives nothing.

---

## 3. Parameter Validation — Early Return Pattern

Validate required parameters immediately after reading them. Call `print()` before every `return` when leaving early — the `finally` block still executes, but having `print()` before `return` makes the intent explicit and avoids any edge case if the `finally` is later refactored away.

```java
String keyid1 = ajaxResponse.getRequestParameter("keyid1");
if (keyid1 == null || keyid1.isEmpty()) {
    ajaxResponse.setError("keyid1 is required");
    return;   // finally block will still call print()
}
```

- `getRequestParameter(name)` returns `""` (not `null`) when the parameter is absent — test for `isEmpty()`, not `null`.
- Use the two-arg form for optional parameters with a known default: `ajaxResponse.getRequestParameter("statusid", "ACTIVE")`.
- Validate all required parameters before touching the database.

---

## 4. Database Access — Always Use Parameterized Forms

| Operation | Use |
|---|---|
| SELECT rows | `this.getQueryProcessor().getPreparedSqlDataSet(sql, Object[])` |
| COUNT | `this.getQueryProcessor().getPreparedCount(sql, Object[])` |
| INSERT / UPDATE / DELETE | `this.getQueryProcessor().execPreparedUpdate(sql, Object[])` |
| Complex dynamic WHERE with IN-list | `SafeSQL` — build SQL with `addVar()` / `addIn()`, pass `sql.getValues()` |

**Never** concatenate user-supplied request parameters directly into SQL strings.

```java
// CORRECT — parameterized SELECT
DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(
    "SELECT statusid, sampletype FROM s_sample WHERE keyid1 = ?",
    new Object[]{ keyid1 }
);

// CORRECT — parameterized DML
int rows = this.getQueryProcessor().execPreparedUpdate(
    "UPDATE s_sample SET statusid = ? WHERE keyid1 = ?",
    new Object[]{ newStatus, keyid1 }
);

// CORRECT — dynamic IN-list via SafeSQL
SafeSQL sql = new SafeSQL();
StringBuilder sb = new StringBuilder("SELECT keyid1 FROM s_sample WHERE sdcid = ");
sb.append(sql.addVar(sdcid)).append(" AND statusid IN (").append(sql.addIn(statusList)).append(")");
DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sb.toString(), sql.getValues());

// WRONG — SQL injection risk
DataSet ds = this.getQueryProcessor().getSqlDataSet(
    "SELECT * FROM s_sample WHERE keyid1 = '" + keyid1 + "'"
);
```

---

## 5. DataSet Access — Always Guard and Use Default Values

- Null-check and empty-check every `DataSet` before accessing rows.
- Use the **three-argument** `getValue(row, col, default)` for nullable columns to avoid `NullPointerException`.
- Use the **two-argument** `getValue(row, col)` only for guaranteed non-null columns (primary keys).

```java
DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
if (ds == null || ds.getRowCount() == 0) {
    ajaxResponse.addCallbackArgument("found", false);
    return;
}

String statusid   = ds.getValue(0, "statusid");           // PK-like column, non-null
String locationid = ds.getValue(0, "locationid", "");     // nullable column
```

---

## 6. Response Building — Be Explicit and Typed

- Use `addCallbackArgument(name, boolean)` for flags — serialises as a true JSON boolean, not the string `"true"`.
- Pass a `DataSet` directly to `addCallbackArgument` for multi-row results — the framework serialises it as a JSON array automatically.
- Always include a `found` / `success` / `updated` boolean flag in the response so the JavaScript caller can branch without inspecting other fields.

```java
// Boolean result (not string "true")
ajaxResponse.addCallbackArgument("found",   ds.getRowCount() > 0);
ajaxResponse.addCallbackArgument("updated", rows > 0);

// Single row — individual fields
ajaxResponse.addCallbackArgument("statusid",   ds.getValue(0, "statusid"));
ajaxResponse.addCallbackArgument("locationid", ds.getValue(0, "locationid", ""));

// Multi-row — DataSet serialised as JSON array
ajaxResponse.addCallbackArgument("samples", ds);
ajaxResponse.addCallbackArgument("count",   ds.getRowCount());
```

---

## 7. Error Handling — setError vs. Early Return

| Situation | Use |
|---|---|
| Validation failure (user error) | `ajaxResponse.setError("message")` then `return` |
| Unexpected exception in `catch` | `ajaxResponse.setError(e.getMessage(), e)` — also logs stack trace |
| AjaxResponse not yet constructed (servlet-level crash) | `AjaxResponse.handleException(request, response, exception)` |

- Always pass the exception object to `setError` in the `catch` block — it ensures the stack trace is logged server-side.
- Do **not** call `setError` and then throw; choose one or the other per scenario.
- Do **not** throw a checked exception from `processRequest` (other than `ServletException`) — catch everything and use `setError`.

```java
// Validation failure — user-facing message, no stack trace
if (keyid1.isEmpty()) {
    ajaxResponse.setError("keyid1 is required");
    return;
}

// Unexpected failure — includes stack trace in log
} catch (Exception e) {
    this.logError("GetSampleDetails failed for keyid1=" + keyid1, e);
    ajaxResponse.setError(e.getMessage(), e);
}
```

---

## 8. Method Dispatch — One Handler Per Functional Area

When a handler needs to support multiple related operations, dispatch on a `method` parameter. This avoids class sprawl and keeps related logic together.

- Name each private dispatch method `do<MethodName>(AjaxResponse ar)` and declare it `throws Exception` — exceptions propagate to the shared `catch` in `processRequest`.
- Use `equalsIgnoreCase` (or `switch` on `method.toLowerCase()`) for the method parameter to be case-tolerant.
- Set an error for unknown method values rather than silently doing nothing.

```java
@Override
public void processRequest(HttpServletRequest request,
                           HttpServletResponse response,
                           ServletContext servletContext) throws ServletException {

    AjaxResponse ajaxResponse = new AjaxResponse(request, response);

    try {
        String method = ajaxResponse.getRequestParameter("method");

        switch (method.toLowerCase()) {
            case "getstatus":    doGetStatus(ajaxResponse);    break;
            case "updatestatus": doUpdateStatus(ajaxResponse); break;
            case "validate":     doValidate(ajaxResponse);     break;
            default:
                ajaxResponse.setError("Unknown method: " + method);
        }

    } catch (Exception e) {
        this.logError("SampleAjaxHandler failed", e);
        ajaxResponse.setError(e.getMessage(), e);
    } finally {
        ajaxResponse.print();
    }
}

private void doGetStatus(AjaxResponse ar) throws Exception {
    String keyid1 = ar.getRequestParameter("keyid1");
    // ... query and respond
}
```

---

## 9. callProperties (JSON Request Body) — PropertyList Wrapping

Use `getCallProperties()` when the client sends complex or nested JSON via the `callProperties` parameter. Wrap the returned `JSONObject` in a `PropertyList` for consistent property access.

- Null-check `getCallProperties()` — it returns `null` if the client omitted the parameter.
- Use `pl.getProperty(id)` for scalar values (returns `""` if absent, never `null`).
- Use `pl.getProperty(id, default)` for optional fields with explicit fallbacks.
- Use `pl.getPropertyList(id)` for nested objects; null-check before accessing.

```java
JSONObject job = ajaxResponse.getCallProperties();
if (job == null) {
    ajaxResponse.setError("callProperties is required");
    return;
}

PropertyList pl     = new PropertyList(job);
String dashboardid  = pl.getProperty("dashboardid");
PropertyList config = (PropertyList) pl.getPropertyList("config");
String columns      = config != null ? config.getProperty("columns", "2") : "2";
```

Only use `getCallProperties()` when the client actually sends a JSON object. For flat key-value parameters, use `getRequestParameter()` — it is simpler and avoids an unnecessary JSON parse.

---

## 10. ConnectionInfo — Audit Columns and DBMS Branching

Obtain `ConnectionInfo` via `getConnectionProcessor().getConnectionInfo(getRequestContext().getConnectionId())`. Use it for:

- Recording `modifiedby` / `createdby` audit columns
- Writing DBMS-specific SQL (ORA vs. MSS)
- Role-based authorisation

```java
ConnectionInfo ci = this.getConnectionProcessor()
    .getConnectionInfo(this.getRequestContext().getConnectionId());

String userId = ci.getSysUserId();   // for audit column
String dbms   = ci.getDbms();        // "ORA" or "MSS" for DBMS-specific SQL

// Audit column
this.getQueryProcessor().execPreparedUpdate(
    "UPDATE s_sample SET locationid = ?, modifiedby = ? WHERE keyid1 = ?",
    new Object[]{ locationid, userId, keyid1 }
);

// Role-based check
String roles = ci.getRoles();   // semicolon-delimited
if (!roles.contains("SAMPLE_ADMIN")) {
    ajaxResponse.setError("Insufficient permissions");
    return;
}
```

Do **not** obtain `ConnectionInfo` unless you actually need it — it involves a processor lookup. Don't cache it across requests; obtain fresh per invocation.

---

## 11. Calling an LV Action from Inside a Handler

Use `getActionProcessor().processAction(actionid, version, props)` to delegate to an existing LV Action. Check the return code after the call.

```java
PropertyList props = new PropertyList();
props.setProperty("sdcid",  "s_Sample");
props.setProperty("keyid1", keyid1);
props.setProperty("statusid", newStatus);

this.getActionProcessor().processAction("UpdateSDI", "1", props);

String returnCode = props.getProperty("returncode");
if (!"SUCCESS".equalsIgnoreCase(returnCode)) {
    ajaxResponse.setError("Action failed: " + props.getProperty("errormessage", returnCode));
    return;
}

ajaxResponse.addCallbackArgument("updated", true);
```

---

## 12. Logging Conventions

- Log **entry** at `logInfo` with the handler name and key parameter value.
- Log **errors** at `logError` — always include the exception object so the stack trace appears in server logs.
- Log **debug/trace** details at `logDebug` — method names, row counts, intermediate values (verbose, off in production).
- Do **not** log every step in normal execution — it floods the log.

```java
this.logInfo("GetSampleDetails: start keyid1=" + keyid1);

// Inside catch:
this.logError("GetSampleDetails failed for keyid1=" + keyid1, e);

// Optional debug:
this.logDebug("GetSampleDetails: row count=" + ds.getRowCount());
```

Logging format: `<HandlerName>: <action/context> <keyParam>=<value>` — consistent prefix makes log searching easy.

---

## 13. JavaScript Call Conventions

- Always supply an `errorCallback` when the result matters or side-effects occur — silent failures are hard to diagnose.
- Keep `callProperties` as a flat object when possible; resort to nested JSON only for complex structures.
- Use `post: true` (the default) for all mutating operations and for any request carrying sensitive parameters.
- For method-dispatched handlers, always include `method` as the first property in `callProperties`.

```javascript
// Read operation — error callback recommended
sapphire.ajax.callClass(
    "com.acme.ajax.sample.GetSampleDetails",
    function(response) {
        if (response.found) {
            document.getElementById("statusField").value = response.statusid;
        }
    },
    { keyid1: currentKeyid1 },
    true,    // POST
    false,   // async
    function(error) { console.error("GetSampleDetails error: " + error); }
);

// Method-dispatched call
sapphire.ajax.callClass(
    "com.acme.ajax.sample.SampleAjaxHandler",
    function(resp) { /* handle */ },
    { method: "updateStatus", keyid1: "SAMP-001", statusid: "ACTIVE" }
);
```

---

## 14. Security Checklist

| Concern | Rule |
|---|---|
| SQL injection | **Always** use `getPreparedSqlDataSet()`, `execPreparedUpdate()`, or `SafeSQL` — never concatenate request parameters into SQL |
| XSS | Encode with `SafeHTML.encodeForHTML(value)` before embedding values in HTML strings returned in the response |
| Authorisation | Check `ConnectionInfo.getRoles()` or `getModules()` when the operation is role-gated; the framework only validates the session, not permissions |
| Sensitive data | Do not include passwords, tokens, or raw stack traces in `addCallbackArgument` values |
| Content type | Do not override `acceptContentType()` — the default allows only `application/x-www-form-urlencoded` and `application/json` |

---

## 15. Deployment Checklist

Before deploying a new or modified AJAX handler, verify:

- [ ] Class is in `com.<client>.ajax.<module>` package
- [ ] Class extends `BaseAjaxRequest` with no intermediate base class
- [ ] `processRequest` signature matches exactly: `(HttpServletRequest, HttpServletResponse, ServletContext) throws ServletException`
- [ ] `ajaxResponse.print()` is in the `finally` block — guaranteed to execute on every code path
- [ ] All SQL uses parameterized form (`Object[]`); no request parameter concatenated into SQL
- [ ] Every `DataSet` access is guarded with null / `getRowCount()` check
- [ ] Error path calls `logError(msg, exception)` with the exception object
- [ ] JavaScript call supplies an `errorCallback` for operations with side effects
- [ ] `.class` file compiled against the correct Sapphire JAR version and deployed to server classpath
- [ ] Application server restarted if class was not previously on the classpath
- [ ] Tested: happy path, missing required parameter, record not found, database error simulation

---

## 16. Quick Anti-Pattern Reference

| Anti-Pattern | Correct Pattern |
|---|---|
| `ajaxResponse.print()` only inside `try` block | Move to `finally` block — must be unconditional |
| SQL built from request parameter: `"WHERE keyid1 = '" + keyid1 + "'"` | `getPreparedSqlDataSet(sql, new Object[]{ keyid1 })` |
| `getRequestParameter` result compared to `null` | Compare to `""` — the method never returns `null` |
| `ds.getValue(row, col)` on nullable column | `ds.getValue(row, col, "")` — provide default |
| Boolean result returned as string `"true"` | `addCallbackArgument("flag", booleanExpr)` — use `boolean` overload |
| `catch (Exception e)` without logging `e` | `this.logError("handler failed", e)` — always pass the exception |
| One class per AJAX operation (class sprawl) | Group related operations in one class; dispatch via `method` parameter |
| `getCallProperties()` used for flat params | Use `getRequestParameter()` for flat key-value — simpler, no JSON overhead |
| No `errorCallback` in `callClass` for mutating operations | Always supply `errorCallback` when the operation has side effects |
| Response written without a `found`/`success` boolean | Include a boolean flag so the JS caller can branch cleanly |
| `ConnectionInfo` obtained but never used | Only obtain when needed (audit, DBMS, roles) — it costs a processor lookup |
| Action result unchecked after `processAction` | Always read `props.getProperty("returncode")` and check for failure |
