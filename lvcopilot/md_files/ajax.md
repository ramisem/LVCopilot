# LabVantage AJAX Implementation Guide

## Table of Contents

1. [What is LV AJAX?](#1-what-is-lv-ajax)
2. [Core Architecture](#2-core-architecture)
3. [BaseAjaxRequest Class Reference](#3-baseajaxrequest-class-reference)
4. [AjaxResponse: Request Parameters and Response Building](#4-ajaxresponse-request-parameters-and-response-building)
5. [Creating a Java AJAX Handler (Step-by-Step)](#5-creating-a-java-ajax-handler-step-by-step)
6. [Calling AJAX from the Client (JavaScript API)](#6-calling-ajax-from-the-client-javascript-api)
7. [Database Access Inside AJAX Handlers](#7-database-access-inside-ajax-handlers)
8. [Error Handling](#8-error-handling)
9. [Method Dispatch Pattern](#9-method-dispatch-pattern)
10. [Working with JSON (PropertyList + JSONObject)](#10-working-with-json-propertylist--jsonobject)
11. [ConnectionInfo and Session Context](#11-connectioninfo-and-session-context)
12. [Available Processor Utilities](#12-available-processor-utilities)
13. [Security Considerations](#13-security-considerations)
14. [Example Implementations](#14-example-implementations)
15. [Common Patterns and Best Practices](#15-common-patterns-and-best-practices)
16. [Built-in AJAX Services Quick Reference](#16-built-in-ajax-services-quick-reference)

---

## 1. What is LV AJAX?

An **LV AJAX handler** is the primary mechanism for implementing **asynchronous, server-side business logic** that is invoked from the browser without a full page reload. It enables:

- Receiving JSON or form-encoded request parameters from a JavaScript caller
- Executing business logic (database queries, validations, calculations)
- Returning a JSON response consumed by a JavaScript callback function

| Category | Description |
|---|---|
| **Custom AJAX class** | Your Java class extending `BaseAjaxRequest` — invoked via `sapphire.ajax.callClass()` |
| **Built-in AJAX service** | Shipped with LV (e.g., `AddSDI`, `ProcessAction`) — invoked via `sapphire.ajax.callService()` |
| **LV Command** | Server-side command executed after page load — invoked via `sapphire.ajax.callCommand()` |

### Execution Contexts

AJAX handlers are called from:

- **Form fields / controls** — on change, on blur, on focus events
- **Buttons** — triggered by user interaction
- **Page scripts** — programmatic calls on load or in response to other events
- **Other JavaScript code** — direct `sapphire.ajax.callClass()` invocations

---

## 2. Core Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                  Browser (JavaScript)                        │
│  sapphire.ajax.callClass("com.client.ajax.MyHandler", ...)  │
└─────────────────────┬───────────────────────────────────────┘
                      │ HTTP POST  (ajaxclass=..., callproperties=...)
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                AjaxRequest Servlet                           │
│  1. Validates connectionId                                   │
│  2. Instantiates class via reflection                        │
│  3. Sets RequestContext, connectionId, PrintWriter           │
│  4. Calls processRequest(req, resp, ctx)                     │
│  5. Writes response: Content-Type: application/json          │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                BaseCustom (abstract)                         │
│  Provides all processor instances as protected fields        │
└─────────────────────┬───────────────────────────────────────┘
                      │ extends
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                BaseRequest (abstract)                        │
│  processRequest() abstract                                   │
│  getProperty() / setProperty()                               │
│  getRequestContext() / setRequestContext()                   │
└─────────────────────┬───────────────────────────────────────┘
                      │ extends
                      ▼
┌─────────────────────────────────────────────────────────────┐
│              BaseAjaxRequest (abstract)                      │
│  open() / close() / write() / print() / println()           │
│  logDebug() / logInfo() / logWarn() / logError()             │
└─────────────────────┬───────────────────────────────────────┘
                      │ extends
                      ▼
┌─────────────────────────────────────────────────────────────┐
│              YourCustomAjaxHandler                           │
│  @Override                                                   │
│  public void processRequest(HttpServletRequest req,          │
│      HttpServletResponse resp, ServletContext ctx)           │
│      throws ServletException { ... }                         │
└─────────────────────────────────────────────────────────────┘
```

### Key Classes and Their Packages

| Class | Package | Purpose |
|---|---|---|
| `BaseAjaxRequest` | `sapphire.servlet` | Abstract base — extend this |
| `BaseRequest` | `sapphire.servlet.command` (or `sapphire.servlet`) | Request abstraction |
| `BaseCustom` | `com.labvantage.sapphire` | Processor access |
| `AjaxResponse` | `sapphire.servlet` | Builds and writes the JSON response |
| `RequestContext` | `sapphire.servlet` | Current HTTP request context |
| `PropertyList` | `sapphire.xml` | JSON/XML property container |
| `DataSet` | `sapphire.util` | Tabular result data |
| `QueryProcessor` | `sapphire.accessor` | Database SELECT queries |
| `SafeSQL` | `sapphire.util` | SQL injection–safe parameterised queries |
| `ConnectionInfo` | `sapphire.util` | Current user session |
| `SDCProcessor` | `sapphire.accessor` | SDC/SDI metadata |

---

## 3. BaseAjaxRequest Class Reference

All custom AJAX handlers **must** extend `BaseAjaxRequest`.

```java
package sapphire.servlet;

public abstract class BaseAjaxRequest extends BaseRequest {

    // ── Response I/O ─────────────────────────────────────
    protected void open(PrintWriter out)    // set response writer (called by framework)
    protected void close()                  // flush and close writer
    protected void write(Object data)       // write without newline
    protected void print(Object data)       // write without newline (alias)
    protected void println(Object data)     // write with newline

    // ── Logging ───────────────────────────────────────────
    protected void logDebug(String msg)
    protected void logInfo(String msg)
    protected void logWarn(String msg)
    protected void logError(String msg)
    protected void logError(String msg, Throwable t)

    // ── Servlet reference ─────────────────────────────────
    protected HttpServlet getServlet()
    protected void        setServlet(HttpServlet servlet)

    // ── Content type ──────────────────────────────────────
    protected boolean acceptContentType(String contentType)
    // Accepts: "application/x-www-form-urlencoded", "application/json"

    // ── Required override ─────────────────────────────────
    @Override
    public abstract void processRequest(
        HttpServletRequest  request,
        HttpServletResponse response,
        ServletContext      servletContext) throws ServletException;
}
```

### Lifecycle Flow

1. **Framework** instantiates your class, injects `RequestContext`, connection ID, and `PrintWriter`.
2. **Framework** calls `processRequest(request, response, servletContext)` — **your logic goes here**.
3. Inside `processRequest`, create an `AjaxResponse`, build the response, call `ajaxResponse.print()`.
4. **Framework** flushes the writer and sets `Content-Type: application/json` on the response.

---

## 4. AjaxResponse: Request Parameters and Response Building

`AjaxResponse` is the central helper inside `processRequest`. It reads incoming parameters and assembles the outgoing JSON.

```java
package sapphire.servlet;

public class AjaxResponse {

    // ── Constructors ──────────────────────────────────────
    AjaxResponse(HttpServletRequest request, HttpServletResponse response)
    AjaxResponse(HttpServletRequest request, HttpServletResponse response,
                 String callback)

    // ── Reading request parameters ────────────────────────
    String     getRequestParameter(String propertyId)
    String     getRequestParameter(String propertyId, String defaultValue)
    Map        getRequestParameters()

    // ── Reading structured JSON request body ──────────────
    JSONObject getCallProperties()   // parses "callproperties" param as JSON

    // ── Writing response properties ───────────────────────
    void addCallbackArgument(String name, Object  data)
    void addCallbackArgument(String name, int     data)
    void addCallbackArgument(String name, long    data)
    void addCallbackArgument(String name, double  data)
    void addCallbackArgument(String name, boolean data)
    void addCallbackArgument(String name, Map     data)
    void addCallbackArgument(String name, Collection data)
    void addCallbackArgument(String name, DataSet dataset)

    // ── Error handling ────────────────────────────────────
    void setError(String error)
    void setError(String error, Throwable t)
    static void handleException(HttpServletRequest req,
                                HttpServletResponse resp, Throwable t)

    // ── Callback configuration ────────────────────────────
    void   setCallback(String callback)
    void   setErrorCallback(String errorCallback)
    void   setCallProperties(Object properties)

    // ── Advanced helpers ──────────────────────────────────
    PageContext getPageContext()              // JSP page context for element rendering
    String      getBrowser()                 // browser type
    DataSet     getRegisteredSQLDataSet(String queryId, PropertyList params)

    // ── Output ────────────────────────────────────────────
    void   print()        // serialize to JSON and write to response
    String toString()     // get JSON string without writing
}
```

### Typical `processRequest` Structure

```java
@Override
public void processRequest(HttpServletRequest request,
                           HttpServletResponse response,
                           ServletContext servletContext) throws ServletException {
    AjaxResponse ajaxResponse = new AjaxResponse(request, response);
    try {
        // 1. Read parameters
        String param1 = ajaxResponse.getRequestParameter("param1");

        // 2. Business logic
        String result = doWork(param1);

        // 3. Write response
        ajaxResponse.addCallbackArgument("result", result);

    } catch (Exception e) {
        ajaxResponse.setError(e.getMessage(), e);
    } finally {
        ajaxResponse.print();   // always call print() — even on error
    }
}
```

---

## 5. Creating a Java AJAX Handler (Step-by-Step)

### Step 1: Create the Handler Class

```java
package com.client.ajax;

import com.labvantage.sapphire.BaseCustom;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetSampleDetails extends BaseAjaxRequest {

    @Override
    public void processRequest(HttpServletRequest request,
                               HttpServletResponse response,
                               ServletContext servletContext) throws ServletException {

        AjaxResponse ajaxResponse = new AjaxResponse(request, response);

        try {
            String keyid1 = ajaxResponse.getRequestParameter("keyid1");

            if (keyid1 == null || keyid1.isEmpty()) {
                ajaxResponse.setError("keyid1 is required");
                return;
            }

            QueryProcessor qp = this.getQueryProcessor();
            DataSet ds = qp.getPreparedSqlDataSet(
                "SELECT statusid, sampletype FROM s_sample WHERE keyid1 = ?",
                new Object[]{keyid1}
            );

            if (ds.getRowCount() > 0) {
                ajaxResponse.addCallbackArgument("statusid",   ds.getValue(0, "statusid"));
                ajaxResponse.addCallbackArgument("sampletype", ds.getValue(0, "sampletype"));
                ajaxResponse.addCallbackArgument("found",      true);
            } else {
                ajaxResponse.addCallbackArgument("found", false);
            }

        } catch (Exception e) {
            this.logError("GetSampleDetails failed", e);
            ajaxResponse.setError(e.getMessage(), e);
        } finally {
            ajaxResponse.print();
        }
    }
}
```

### Step 2: Compile and Deploy

1. Compile against the LabVantage Sapphire JAR (`sapphire.jar`).
2. Package the `.class` file into the application WAR or place it on the server classpath.
3. Restart the LabVantage application server if class loading requires it.

### Step 3: No Registration Required

Unlike LV Actions, AJAX handler classes do **not** need to be registered in System Admin. The framework instantiates them directly via reflection using the fully-qualified class name supplied by the client.

---

## 6. Calling AJAX from the Client (JavaScript API)

### `sapphire.ajax.callClass(className, callback, callProperties, post, synchronous, errorCallback)`

Invokes a custom Java class extending `BaseAjaxRequest`.

| Parameter | Type | Default | Description |
|---|---|---|---|
| `className` | String | required | Fully-qualified Java class name |
| `callback` | Function | required | Called on success with parsed JSON |
| `callProperties` | Object | `{}` | Key-value parameters sent to server |
| `post` | Boolean | `true` | Use POST (recommended) |
| `synchronous` | Boolean | `false` | Block until complete |
| `errorCallback` | Function | none | Called on HTTP or server error |

```javascript
// Basic call
sapphire.ajax.callClass(
    "com.client.ajax.GetSampleDetails",
    function(response) {
        if (response.found) {
            console.log("Status: " + response.statusid);
        }
    },
    { keyid1: "SAMP-001" }
);

// With error callback
sapphire.ajax.callClass(
    "com.client.ajax.GetSampleDetails",
    function(response) { /* success */ },
    { keyid1: fieldValue },
    true,    // POST
    false,   // async
    function(error) { alert("AJAX error: " + error); }
);
```

### `sapphire.ajax.callService(service, callback, callProperties, ...)`

Calls a built-in LabVantage AJAX service.

```javascript
sapphire.ajax.callService("ProcessAction", callback, {
    actionid:     "MyAction",
    actionversion: "1",
    sdcid:        "s_Sample",
    keyid1:       "SAMP-001"
});
```

### `sapphire.ajax.callCommand(command, callback, callProperties, ...)`

Calls a server-side LV command.

```javascript
sapphire.ajax.callCommand("getSDIList", callback, {
    sdcid:  "s_Sample",
    filter: "statusid = 'ACTIVE'"
});
```

### Low-Level Send

```javascript
sapphire.ajax.io.send({
    url:     "/servlet?ajaxclass=com.client.ajax.MyHandler",
    data:    { param1: "value1" },
    success: function(responseText) { /* parse manually */ },
    error:   function(status, text) { /* handle */ },
    timeout: 60000   // ms, default 60000
});
```

---

## 7. Database Access Inside AJAX Handlers

`BaseAjaxRequest` extends `BaseCustom`, which provides all processor instances. The access pattern is identical to LV Actions.

### QueryProcessor — SELECT (Primary Pattern)

```java
QueryProcessor qp = this.getQueryProcessor();

// Parameterized SELECT
DataSet ds = qp.getPreparedSqlDataSet(
    "SELECT col1, col2 FROM s_sample WHERE keyid1 = ? AND statusid = ?",
    new Object[]{keyid1, statusid}
);

// Iterate rows
for (int i = 0; i < ds.getRowCount(); i++) {
    String col1 = ds.getValue(i, "col1");
    String col2 = ds.getValue(i, "col2", "");  // with default
}

// COUNT query
int count = qp.getPreparedCount(
    "SELECT COUNT(*) FROM s_sample WHERE sdcid = ?",
    new Object[]{sdcid}
);

// Row-level security filter
String secClause = qp.getSecurityFilterWhere(sdcid);
DataSet secured  = qp.getSqlDataSet(
    "SELECT keyid1 FROM s_sample WHERE 1=1 " + secClause
);
```

### INSERT / UPDATE / DELETE via QueryProcessor

```java
QueryProcessor qp = this.getQueryProcessor();

int rows = qp.execPreparedUpdate(
    "UPDATE s_sample SET statusid = ? WHERE keyid1 = ?",
    new Object[]{newStatus, keyid1}
);
```

### SafeSQL (Alternative for Complex Queries)

```java
SafeSQL sql = new SafeSQL();
StringBuilder sb = new StringBuilder();
sb.append("SELECT keyid1 FROM s_sample WHERE sdcid = ");
sb.append(sql.addVar(sdcid));
sb.append(" AND statusid IN (");
sb.append(sql.addIn(statusList));   // addIn() handles comma-separated lists
sb.append(")");

DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(
    sb.toString(), sql.getValues()
);
```

### Returning a DataSet Directly in the Response

`AjaxResponse.addCallbackArgument` accepts a `DataSet` and serialises it as a JSON array:

```java
DataSet ds = qp.getPreparedSqlDataSet(sql, params);
ajaxResponse.addCallbackArgument("rows", ds);
ajaxResponse.print();
```

### QueryProcessor Method Summary

| Method | Return | Description |
|---|---|---|
| `getPreparedSqlDataSet(sql, Object[])` | `DataSet` | Parameterized SELECT — primary pattern |
| `getSqlDataSet(sql)` | `DataSet` | Raw SQL SELECT |
| `getPreparedCount(sql, Object[])` | `int` | Parameterized COUNT |
| `getCount(sql)` | `int` | Raw SQL COUNT |
| `execPreparedUpdate(sql, Object[])` | `int` | Parameterized INSERT/UPDATE/DELETE |
| `execSQL(sql)` | void | Raw non-query SQL |
| `getSecurityFilterWhere(sdcid)` | `String` | Row-level security WHERE clause |

---

## 8. Error Handling

### Setting Errors on AjaxResponse

```java
// Error without exception — response JSON includes { "error": "message" }
ajaxResponse.setError("Record not found for keyid1=" + keyid1);

// Error with exception — also logs stack trace
ajaxResponse.setError("Unexpected failure", e);
```

The JavaScript `errorCallback` (if supplied to `callClass`) is invoked when the response contains an error.

### Always Call `print()` in a `finally` Block

```java
try {
    // ... logic ...
    ajaxResponse.addCallbackArgument("result", value);
} catch (Exception e) {
    this.logError("MyHandler failed", e);
    ajaxResponse.setError(e.getMessage(), e);
} finally {
    ajaxResponse.print();   // must be called unconditionally
}
```

### Static Exception Handler (Servlet-Level Fallback)

```java
// Use when AjaxResponse is not yet constructed
AjaxResponse.handleException(request, response, exception);
```

### Early Return on Validation Failure

```java
String keyid1 = ajaxResponse.getRequestParameter("keyid1");
if (keyid1 == null || keyid1.isEmpty()) {
    ajaxResponse.setError("keyid1 is required");
    ajaxResponse.print();
    return;
}
```

### Logging

```java
this.logInfo("MyHandler: start keyid1=" + keyid1);
this.logWarn("No record found, returning empty response");
this.logError("Database error in MyHandler", exception);
this.logDebug("SQL result count=" + ds.getRowCount());
```

---

## 9. Method Dispatch Pattern

When a single AJAX handler needs to perform several related operations, use a `method` parameter to dispatch to private methods. This avoids creating one class per operation.

```java
public class SampleAjaxHandler extends BaseAjaxRequest {

    @Override
    public void processRequest(HttpServletRequest request,
                               HttpServletResponse response,
                               ServletContext servletContext) throws ServletException {

        AjaxResponse ajaxResponse = new AjaxResponse(request, response);

        try {
            String method = ajaxResponse.getRequestParameter("method");

            if ("getStatus".equalsIgnoreCase(method)) {
                doGetStatus(ajaxResponse);
            } else if ("updateStatus".equalsIgnoreCase(method)) {
                doUpdateStatus(ajaxResponse);
            } else if ("validate".equalsIgnoreCase(method)) {
                doValidate(ajaxResponse);
            } else {
                ajaxResponse.setError("Unknown method: " + method);
            }

        } catch (Exception e) {
            this.logError("SampleAjaxHandler failed", e);
            ajaxResponse.setError(e.getMessage(), e);
        } finally {
            ajaxResponse.print();
        }
    }

    private void doGetStatus(AjaxResponse ajaxResponse) throws Exception {
        String keyid1 = ajaxResponse.getRequestParameter("keyid1");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(
            "SELECT statusid FROM s_sample WHERE keyid1 = ?",
            new Object[]{keyid1}
        );
        ajaxResponse.addCallbackArgument("statusid",
            ds.getRowCount() > 0 ? ds.getValue(0, "statusid") : "");
    }

    private void doUpdateStatus(AjaxResponse ajaxResponse) throws Exception {
        String keyid1    = ajaxResponse.getRequestParameter("keyid1");
        String newStatus = ajaxResponse.getRequestParameter("statusid");
        this.getQueryProcessor().execPreparedUpdate(
            "UPDATE s_sample SET statusid = ? WHERE keyid1 = ?",
            new Object[]{newStatus, keyid1}
        );
        ajaxResponse.addCallbackArgument("updated", true);
    }

    private void doValidate(AjaxResponse ajaxResponse) throws Exception {
        String keyid1 = ajaxResponse.getRequestParameter("keyid1");
        int count = this.getQueryProcessor().getPreparedCount(
            "SELECT COUNT(*) FROM s_sample WHERE keyid1 = ?",
            new Object[]{keyid1}
        );
        ajaxResponse.addCallbackArgument("valid", count > 0);
    }
}
```

**JavaScript call for dispatched method:**

```javascript
sapphire.ajax.callClass(
    "com.client.ajax.SampleAjaxHandler",
    function(resp) { console.log("status=" + resp.statusid); },
    { method: "getStatus", keyid1: "SAMP-001" }
);
```

---

## 10. Working with JSON (PropertyList + JSONObject)

When the client sends complex structured data via `callProperties`, parse it with `getCallProperties()`:

```javascript
// Client: send nested JSON
sapphire.ajax.callClass(
    "com.client.ajax.DashboardSaveHandler",
    callback,
    {
        dashboardid: "DASH-001",
        config: { columns: 3, theme: "dark" }
    }
);
```

```java
// Server: parse callProperties
@Override
public void processRequest(HttpServletRequest request,
                           HttpServletResponse response,
                           ServletContext servletContext) throws ServletException {

    AjaxResponse ajaxResponse = new AjaxResponse(request, response, "DashboardCallback");

    try {
        JSONObject job = ajaxResponse.getCallProperties();

        if (job != null) {
            // Wrap in PropertyList for convenient property access
            PropertyList pl = new PropertyList(job);

            String dashboardId = pl.getProperty("dashboardid");
            PropertyList config = (PropertyList) pl.getPropertyList("config");
            String columns = config != null ? config.getProperty("columns") : "2";

            // ... save logic ...

            ajaxResponse.addCallbackArgument("saved",       true);
            ajaxResponse.addCallbackArgument("dashboardid", dashboardId);
        } else {
            ajaxResponse.setError("No callProperties received");
        }

    } catch (Exception e) {
        ajaxResponse.setError(e.getMessage(), e);
    } finally {
        ajaxResponse.print();
    }
}
```

### PropertyList Key Methods

| Method | Description |
|---|---|
| `getProperty(id)` | Returns String value, `""` if missing |
| `getProperty(id, default)` | Returns String value with fallback |
| `setProperty(id, value)` | Write a String value |
| `getPropertyList(id)` | Returns nested PropertyList |
| `toJSONString()` | Serialize to JSON string |

---

## 11. ConnectionInfo and Session Context

`BaseCustom` provides the current user's session via `getConnectionInfo()` (or the `connectionInfo` field after `startAction`). In AJAX handlers the equivalent is obtained from `getRequestContext()`:

```java
// Get the SapphireConnection from the request context
RequestContext rc = this.getRequestContext();
String connectionId = rc.getConnectionId();

// Get ConnectionInfo via ConnectionProcessor
ConnectionInfo ci = this.getConnectionProcessor().getConnectionInfo(connectionId);

String userId    = ci.getSysUserId();    // e.g., "ADMIN"
String logon     = ci.getLogonName();
String dbms      = ci.getDbms();         // "ORA" or "MSS"
String language  = ci.getLanguage();
String timeZone  = ci.getTimeZone();
String roles     = ci.getRoles();        // semicolon-delimited
```

Use `ConnectionInfo` when your handler needs to:

- Record the acting user (audit columns like `modifiedby`)
- Write DBMS-specific SQL (check `getDbms()`)
- Apply locale or timezone formatting

---

## 12. Available Processor Utilities

All processors are available via `this.get*Processor()` inherited from `BaseCustom`:

| Processor | Accessor Method | Purpose |
|---|---|---|
| `QueryProcessor` | `this.getQueryProcessor()` | SQL SELECT / DML / COUNT queries |
| `ConnectionProcessor` | `this.getConnectionProcessor()` | DB connection and ConnectionInfo |
| `SDIProcessor` | `this.getSDIProcessor()` | Create/read/update SDI records |
| `SDCProcessor` | `this.getSDCProcessor()` | SDC metadata (table name, key columns) |
| `ActionProcessor` | `this.getActionProcessor()` | Execute LV Actions programmatically |
| `TranslationProcessor` | `this.getTranslationProcessor()` | Translate text to user's language |
| `DAMProcessor` | `this.getDAMProcessor()` | Document/media management |
| `SequenceProcessor` | `this.getSequenceProcessor()` | Sequence number generation |
| `ConfigurationProcessor` | `this.getConfigurationProcessor()` | System configuration properties |
| `HttpProcessor` | `this.getHttpProcessor()` | HTTP utilities |

### Calling an LV Action from Inside an AJAX Handler

```java
import sapphire.accessor.ActionProcessor;
import sapphire.xml.PropertyList;

ActionProcessor ap = this.getActionProcessor();

PropertyList props = new PropertyList();
props.setProperty("sdcid",  "s_Sample");
props.setProperty("keyid1", keyid1);
props.setProperty("statusid", newStatus);

ap.processAction("UpdateSDI", "1", props);

String result = props.getProperty("returncode");
```

### Translating Text

```java
String translated = this.getTranslationProcessor().translate("Sample not found");
ajaxResponse.setError(translated);
```

---

## 13. Security Considerations

| Concern | Approach |
|---|---|
| **SQL injection** | Always use `getPreparedSqlDataSet()`, `execPreparedUpdate()`, or `SafeSQL` — never concatenate user input directly into SQL |
| **XSS** | Encode HTML output: `SafeHTML.encodeForHTML(value)` before embedding in HTML strings; `SafeHTML.encodeForJavaScript(value)` for JS strings |
| **Authentication** | The framework validates the connection ID before invoking your class — do not perform a separate authentication check |
| **Authorisation** | Use `ConnectionInfo.getRoles()` or `ConnectionInfo.getModules()` to enforce role-based access inside the handler |
| **Content-type** | `BaseAjaxRequest.acceptContentType()` only allows `application/x-www-form-urlencoded` and `application/json` |

---

## 14. Example Implementations

### Example 1: Simple Lookup (Single Value Return)

```java
package com.client.ajax;

import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetContainerStatus extends BaseAjaxRequest {

    @Override
    public void processRequest(HttpServletRequest request,
                               HttpServletResponse response,
                               ServletContext servletContext) throws ServletException {

        AjaxResponse ajaxResponse = new AjaxResponse(request, response);

        try {
            String containerid = ajaxResponse.getRequestParameter("containerid");

            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(
                "SELECT statusid, locationid FROM s_container WHERE keyid1 = ?",
                new Object[]{containerid}
            );

            if (ds.getRowCount() > 0) {
                ajaxResponse.addCallbackArgument("statusid",   ds.getValue(0, "statusid"));
                ajaxResponse.addCallbackArgument("locationid", ds.getValue(0, "locationid", ""));
            } else {
                ajaxResponse.addCallbackArgument("statusid",   "");
                ajaxResponse.addCallbackArgument("locationid", "");
            }

        } catch (Exception e) {
            this.logError("GetContainerStatus error", e);
            ajaxResponse.setError(e.getMessage(), e);
        } finally {
            ajaxResponse.print();
        }
    }
}
```

### Example 2: Multi-Row Result as JSON Array

```java
package com.client.ajax;

import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetSamplesForProject extends BaseAjaxRequest {

    @Override
    public void processRequest(HttpServletRequest request,
                               HttpServletResponse response,
                               ServletContext servletContext) throws ServletException {

        AjaxResponse ajaxResponse = new AjaxResponse(request, response);

        try {
            String projectid = ajaxResponse.getRequestParameter("projectid");
            String statusid  = ajaxResponse.getRequestParameter("statusid", "");

            StringBuilder sql = new StringBuilder(
                "SELECT keyid1, sampletype, statusid FROM s_sample WHERE projectid = ?");
            Object[] params;

            if (!statusid.isEmpty()) {
                sql.append(" AND statusid = ?");
                params = new Object[]{projectid, statusid};
            } else {
                params = new Object[]{projectid};
            }

            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(
                sql.toString(), params
            );

            // DataSet is serialised to JSON array automatically
            ajaxResponse.addCallbackArgument("samples", ds);
            ajaxResponse.addCallbackArgument("count",   ds.getRowCount());

        } catch (Exception e) {
            this.logError("GetSamplesForProject error", e);
            ajaxResponse.setError(e.getMessage(), e);
        } finally {
            ajaxResponse.print();
        }
    }
}
```

### Example 3: Write Operation (UPDATE with Validation)

```java
package com.client.ajax;

import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.accessor.ConnectionProcessor;
import sapphire.util.ConnectionInfo;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AssignSampleLocation extends BaseAjaxRequest {

    @Override
    public void processRequest(HttpServletRequest request,
                               HttpServletResponse response,
                               ServletContext servletContext) throws ServletException {

        AjaxResponse ajaxResponse = new AjaxResponse(request, response);

        try {
            String keyid1      = ajaxResponse.getRequestParameter("keyid1");
            String locationid  = ajaxResponse.getRequestParameter("locationid");

            // Validate inputs
            if (keyid1.isEmpty() || locationid.isEmpty()) {
                ajaxResponse.setError("keyid1 and locationid are required");
                return;
            }

            // Validate sample exists
            int count = this.getQueryProcessor().getPreparedCount(
                "SELECT COUNT(*) FROM s_sample WHERE keyid1 = ?",
                new Object[]{keyid1}
            );
            if (count == 0) {
                ajaxResponse.setError("Sample not found: " + keyid1);
                return;
            }

            // Get current user for audit
            ConnectionInfo ci = this.getConnectionProcessor()
                .getConnectionInfo(this.getRequestContext().getConnectionId());
            String userId = ci.getSysUserId();

            // Perform update
            int rows = this.getQueryProcessor().execPreparedUpdate(
                "UPDATE s_sample SET locationid = ?, modifiedby = ?, modifydate = SYSDATE " +
                "WHERE keyid1 = ?",
                new Object[]{locationid, userId, keyid1}
            );

            if (rows > 0) {
                ajaxResponse.addCallbackArgument("success",    true);
                ajaxResponse.addCallbackArgument("locationid", locationid);
            } else {
                ajaxResponse.setError("Update failed — no rows affected");
            }

        } catch (Exception e) {
            this.logError("AssignSampleLocation error", e);
            ajaxResponse.setError(e.getMessage(), e);
        } finally {
            ajaxResponse.print();
        }
    }
}
```

### Example 4: JSON Request Body (callProperties)

```java
package com.client.ajax;

import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;

public class BulkStatusUpdate extends BaseAjaxRequest {

    @Override
    public void processRequest(HttpServletRequest request,
                               HttpServletResponse response,
                               ServletContext servletContext) throws ServletException {

        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "BulkUpdateCallback");

        try {
            JSONObject job = ajaxResponse.getCallProperties();

            if (job == null) {
                ajaxResponse.setError("No callProperties provided");
                return;
            }

            PropertyList pl     = new PropertyList(job);
            String newStatus    = pl.getProperty("newstatus");
            String keyidList    = pl.getProperty("keyids");   // "SAMP-001;SAMP-002;SAMP-003"

            if (newStatus.isEmpty() || keyidList.isEmpty()) {
                ajaxResponse.setError("newstatus and keyids are required");
                return;
            }

            String[] keyids = keyidList.split(";");
            int updated = 0;

            for (String keyid1 : keyids) {
                if (!keyid1.isEmpty()) {
                    int rows = this.getQueryProcessor().execPreparedUpdate(
                        "UPDATE s_sample SET statusid = ? WHERE keyid1 = ?",
                        new Object[]{newStatus, keyid1.trim()}
                    );
                    if (rows > 0) updated++;
                }
            }

            ajaxResponse.addCallbackArgument("updatedCount", updated);
            ajaxResponse.addCallbackArgument("totalCount",   keyids.length);

        } catch (Exception e) {
            this.logError("BulkStatusUpdate error", e);
            ajaxResponse.setError(e.getMessage(), e);
        } finally {
            ajaxResponse.print();
        }
    }
}
```

### Example 5: Method Dispatch with ActionProcessor

```java
package com.client.ajax;

import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WorkflowAjaxHandler extends BaseAjaxRequest {

    @Override
    public void processRequest(HttpServletRequest request,
                               HttpServletResponse response,
                               ServletContext servletContext) throws ServletException {

        AjaxResponse ajaxResponse = new AjaxResponse(request, response);

        try {
            String method = ajaxResponse.getRequestParameter("method");

            switch (method.toLowerCase()) {
                case "startworkflow": doStartWorkflow(ajaxResponse); break;
                case "getstate":      doGetState(ajaxResponse);      break;
                case "advancestate":  doAdvanceState(ajaxResponse);  break;
                default:
                    ajaxResponse.setError("Unknown method: " + method);
            }

        } catch (Exception e) {
            this.logError("WorkflowAjaxHandler failed", e);
            ajaxResponse.setError(e.getMessage(), e);
        } finally {
            ajaxResponse.print();
        }
    }

    private void doStartWorkflow(AjaxResponse ar) throws Exception {
        String sdcid      = ar.getRequestParameter("sdcid");
        String keyid1     = ar.getRequestParameter("keyid1");
        String workflowid = ar.getRequestParameter("workflowid");

        PropertyList props = new PropertyList();
        props.setProperty("sdcid",      sdcid);
        props.setProperty("keyid1",     keyid1);
        props.setProperty("workflowid", workflowid);

        this.getActionProcessor().processAction("StartWorkflow", "1", props);

        ar.addCallbackArgument("started",    true);
        ar.addCallbackArgument("instanceid", props.getProperty("workflowinstance"));
    }

    private void doGetState(AjaxResponse ar) throws Exception {
        String keyid1 = ar.getRequestParameter("keyid1");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(
            "SELECT currentstate FROM s_sample WHERE keyid1 = ?",
            new Object[]{keyid1}
        );
        ar.addCallbackArgument("state",
            ds.getRowCount() > 0 ? ds.getValue(0, "currentstate") : "");
    }

    private void doAdvanceState(AjaxResponse ar) throws Exception {
        String sdcid  = ar.getRequestParameter("sdcid");
        String keyid1 = ar.getRequestParameter("keyid1");
        String toState = ar.getRequestParameter("tostate");

        PropertyList props = new PropertyList();
        props.setProperty("sdcid",     sdcid);
        props.setProperty("keyid1",    keyid1);
        props.setProperty("gotostate", toState);

        this.getActionProcessor().processAction("AdvanceWorkflow", "1", props);
        ar.addCallbackArgument("advanced", true);
    }
}
```

---

## 15. Common Patterns and Best Practices

### Always Use `try/catch/finally` with `print()`

`ajaxResponse.print()` **must** be called even when an error is set — it is the only way the JSON is written to the response. Use `finally` to guarantee this.

```java
try {
    // ... logic ...
} catch (Exception e) {
    ajaxResponse.setError(e.getMessage(), e);
} finally {
    ajaxResponse.print();
}
```

### Early Return for Validation

```java
String keyid1 = ajaxResponse.getRequestParameter("keyid1");
if (keyid1 == null || keyid1.isEmpty()) {
    ajaxResponse.setError("keyid1 is required");
    ajaxResponse.print();
    return;
}
```

### Boolean Callback Arguments

Use Java `boolean` — `addCallbackArgument` serialises it as a JSON boolean (`true`/`false`):

```java
ajaxResponse.addCallbackArgument("found",   ds.getRowCount() > 0);
ajaxResponse.addCallbackArgument("updated", rows > 0);
```

### Null-Safe Parameter Reading

`getRequestParameter` returns `""` (not `null`) if the parameter is absent:

```java
String value = ajaxResponse.getRequestParameter("myfield");   // "" if absent
String val2  = ajaxResponse.getRequestParameter("field", "DEFAULT");  // explicit fallback
```

### DataSet Default Values

```java
String col = ds.getValue(0, "columnid", "");   // "" instead of null on missing/null
```

### Log at Entry and on Error, Not for Every Step

```java
this.logInfo("MyHandler.processRequest: keyid1=" + keyid1);
// ... logic ...
// only log errors inside catch
this.logError("MyHandler failed for keyid1=" + keyid1, e);
```

### Package Naming Convention

Place AJAX handler classes in a package that makes their purpose clear:

```
com.<client>.ajax.<module>.<HandlerName>
```

Examples: `com.acme.ajax.sample.GetSampleDetails`, `com.acme.ajax.workflow.WorkflowAjaxHandler`

### One Handler Per Functional Area (Method Dispatch)

Rather than creating dozens of one-method classes, group related operations in one handler and dispatch via a `method` parameter. This reduces class sprawl and keeps related logic together.

### No Registration Required

AJAX handler classes are resolved at runtime by fully-qualified class name. There is no System Admin entry to create. If the class is on the classpath, it is callable.

---

## 16. Built-in AJAX Services Quick Reference

Invoke these via `sapphire.ajax.callService(serviceName, ...)` on the client or by mapping `ajaxservice=<name>` in the URL.

| Service Name | Java Class | Description |
|---|---|---|
| `AddSDI` | `com.labvantage.sapphire.ajax.operations.AddSDI` | Create a new SDI record |
| `AddToDoListEntry` | `com.labvantage.sapphire.ajax.operations.AddToDoListEntry` | Add a To-Do list entry |
| `ProcessAction` | `com.labvantage.sapphire.ajax.operations.ProcessAction` | Execute a named LV Action |

### ProcessAction Service — Key Parameters

| Parameter | Description |
|---|---|
| `actionid` | LV Action identifier |
| `actionversion` | Action version string |
| `sdcid` | Target SDC (if applicable) |
| `keyid1` | Primary key field 1 |
| `keyid2` | Primary key field 2 (if multi-key) |
| `keyid3` | Primary key field 3 (if multi-key) |

```javascript
sapphire.ajax.callService(
    "ProcessAction",
    function(response) { console.log(JSON.stringify(response)); },
    {
        actionid:      "MyCustomAction",
        actionversion: "1",
        sdcid:         "s_Sample",
        keyid1:        "SAMP-001",
        myinput:       "value"
    }
);
```

---

*Document generated from LabVantage 8.8 LIMS documentation and decompiled Sapphire JAR.*
*Source: `labvantagedoc/Content/HTML/jsapi-ajax.html`, `sapphire_jar_decompiled/sapphire/servlet/BaseAjaxRequest.java`, `AjaxResponse.java`, `RequestContext.java`, and concrete AJAX handler examples in `sapphire_jar_decompiled/`.*
