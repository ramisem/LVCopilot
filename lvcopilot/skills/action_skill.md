# LV Action Best Practices Skill

> **Always read `md_files/action.md` alongside this file** before writing any LV Action.
> This file captures *how* to write good actions — `action.md` is the authoritative API reference.

---

## 1. Class and Package Structure

- Place action classes in `com.client.actions` (or a client-specific sub-package), never in `sapphire.*`.
- Always declare a companion **interface** in `sapphire.action` that holds the action's string constants.
- The implementation class extends `BaseAction` **and** implements its companion interface.

```java
// Interface (sapphire.action package)
package sapphire.action;
public interface CreateSampleReview {
    String ID        = "CreateSampleReview";
    String VERSIONID = "1";

    // Inputs
    String PROP_SDCID    = "sdcid";
    String PROP_KEYID1   = "keyid1";
    String PROP_COMMENTS = "comments";

    // Outputs
    String OUT_STATUS = "status";

    // Return value constants
    String STATUS_OK    = "OK";
    String STATUS_ERROR = "ERROR";
}

// Implementation (com.client.actions package)
package com.client.actions;
public class CreateSampleReview extends BaseAction
        implements sapphire.action.CreateSampleReview { ... }
```

**Why:** Keeps all string literals in one place, prevents typos in property names, and mirrors the pattern used by LabVantage system actions.

---

## 2. isDatabaseRequired

Override `isDatabaseRequired()` to `false` whenever the action performs no DB operations.

```java
@Override
public boolean isDatabaseRequired() { return false; }
```

**Why:** The framework skips connection acquisition, improving performance for utility/calculation actions.

---

## 3. Input Reading and Fail-Fast Validation

- Read **all** inputs at the top of `processAction`, before any logic.
- Validate required fields immediately — throw `SapphireException` before touching the database.
- Use `properties.getProperty("key", "default")` only when a sensible default exists.

```java
@Override
public void processAction(PropertyList properties) throws SapphireException {
    // Read all inputs first
    String sdcid    = properties.getProperty(PROP_SDCID);
    String keyid1   = properties.getProperty(PROP_KEYID1);
    String comments = properties.getProperty(PROP_COMMENTS, "");

    // Fail fast
    if (sdcid.isEmpty())  throw new SapphireException("MISSING_SDCID",  "'sdcid' is required.");
    if (keyid1.isEmpty()) throw new SapphireException("MISSING_KEYID1", "'keyid1' is required.");

    // Business logic follows
}
```

**Why:** Early exit prevents partial execution and makes error messages precise.

---

## 4. Database Access — Choose the Right Handle

| Scenario | Use |
|---|---|
| SELECT queries | `this.getQueryProcessor().getPreparedSqlDataSet(sql, params)` |
| COUNT queries | `this.getQueryProcessor().getPreparedCount(sql, params)` |
| INSERT / UPDATE / DELETE | `this.database.executePreparedUpdate(sql, safeSQL.getValues())` |
| Large streaming result sets | `this.database.createPreparedResultSet(sql, values)` + `getNext()` loop |
| Raw DDL / non-parameterized (avoid) | `this.database.executeSQL(sql)` — last resort only |

**Never** use `dbUtil` (low-level) unless you have a specific reason that `database` cannot satisfy.

---

## 5. Always Use Parameterized Queries (SafeSQL / Object[])

**Never** concatenate user-supplied values directly into SQL strings.

```java
// CORRECT — parameterized via QueryProcessor
QueryProcessor qp = this.getQueryProcessor();
DataSet ds = qp.getPreparedSqlDataSet(
    "SELECT statusid FROM s_sample WHERE keyid1 = ? AND sdcid = ?",
    new Object[]{keyid1, sdcid}
);

// CORRECT — parameterized via SafeSQL + DBAccess
SafeSQL sql = new SafeSQL();
StringBuffer sb = new StringBuffer();
sb.append("UPDATE s_sample SET statusid = ").append(sql.addVar(newStatus));
sb.append(" WHERE keyid1 = ").append(sql.addVar(keyid1));
this.database.executePreparedUpdate(sb.toString(), sql.getValues());

// WRONG — SQL injection risk
this.database.executeSQL("SELECT * FROM s_sample WHERE keyid1 = '" + keyid1 + "'");
```

---

## 6. DataSet Access Pattern

```java
DataSet ds = qp.getPreparedSqlDataSet(sql, params);

// Single row expected
if (ds.getRowCount() == 0) {
    throw new SapphireException("RECORD_NOT_FOUND", "No record for keyid1=" + keyid1);
}
String statusid = ds.getValue(0, "statusid");
String flag     = ds.getValue(0, "releasedflag", "N");   // default when null

// Multiple rows
for (int i = 0; i < ds.getRowCount(); i++) {
    String col = ds.getValue(i, "colname");
    // process col
}
```

- Always supply a fallback default with `ds.getValue(row, col, "default")` for nullable columns.
- Check `ds.getRowCount() > 0` before calling `ds.getValue(0, ...)`.

---

## 7. Write Operations — Check Return Values

```java
int rows = this.database.executePreparedUpdate(sql, safeSQL.getValues());

// INSERT: expect exactly 1
if (rows < 1) throw new SapphireException("INSERT_FAILED", "Insert returned 0 rows.");

// UPDATE: expect at least 1 (throw if nothing was updated)
if (rows < 1) throw new SapphireException("UPDATE_FAILED",
    "No record updated for keyid1=" + keyid1);
```

**Why:** Silent zero-row writes are a common source of data integrity bugs.

---

## 8. Error Handling Strategy

| Situation | Approach |
|---|---|
| Missing / invalid required input | `throw new SapphireException("MISSING_X", "message")` |
| DB operation returned unexpected result | `throw new SapphireException("DB_ERROR", "message")` |
| Record not found when it must exist | `throw new SapphireException("RECORD_NOT_FOUND", "message")` |
| Non-fatal informational notice | `this.setInfoError("INFO_001", "message")` — does not stop execution |
| Unexpected `RuntimeException` | Catch, log with stack trace, re-throw as `SapphireException("UNEXPECTED_ERROR", ...)` |
| LV `SapphireException` from nested call | Re-throw as-is — do not wrap |

```java
try {
    // ... logic ...
} catch (SapphireException e) {
    throw e;
} catch (Exception e) {
    this.logger.error("Unexpected error in " + this.getClass().getSimpleName(), e);
    throw new SapphireException("UNEXPECTED_ERROR", e.getMessage());
}
```

Error ID convention: `UPPER_SNAKE_CASE`, descriptive (`MISSING_SDCID`, `INVALID_STATUS`, `INSERT_FAILED`).

---

## 9. Output Property Conventions

| Data type | Convention | Example |
|---|---|---|
| Boolean | `"Yes"` / `"No"` (capital Y/N) | `properties.setProperty("isfound", "Yes")` |
| Number | Stringify with `String.valueOf()` | `properties.setProperty("count", String.valueOf(n))` |
| Multi-value list | Semicolon-delimited | `properties.setProperty("ids", "A;B;C")` |
| Null / empty DB value | Normalize to `""` | `if (v == null) v = "";` |
| Status / result codes | Defined as interface constants | `STATUS_OK = "OK"` |

**Always** write all declared Output properties even on the success path — callers depend on them.

---

## 10. Logging Conventions

Log at action entry and exit with key identifiers; log errors with the exception object.

```java
this.logger.info(getClass().getSimpleName() + ": start sdcid=" + sdcid + " keyid1=" + keyid1);

// ... business logic ...

this.logger.info(getClass().getSimpleName() + ": complete status=" + status);
this.logger.debug("SQL result count=" + ds.getRowCount());
this.logger.warn("Empty result set for sdcid=" + sdcid);
this.logger.error("DB update failed for keyid1=" + keyid1, exception);
```

- Use `logger.debug` for SQL strings and intermediate values (disabled in production).
- Use `logger.info` for action start/end and business milestones.
- Use `logger.error` only with the exception object so stack traces appear in the log.

---

## 11. ConnectionInfo — When and How to Use

```java
// Audit columns
String userId = this.connectionInfo.getSysUserId();
safeSQL.addVar(userId);  // use in INSERT modifiedby / createdby columns

// DBMS branching (write DBMS-neutral SQL where possible)
if ("ORA".equals(this.connectionInfo.getDbms())) {
    // Oracle-specific syntax (e.g., SYSDATE, ROWNUM)
} else {
    // SQL Server syntax (e.g., GETDATE(), TOP)
}
```

Use `connectionInfo` only for audit, locale, or DBMS-branch needs — do not pass it around as a general-purpose context object.

---

## 12. Calling Another Action Programmatically

```java
ActionProcessor ap = new ActionProcessor(this.connectionInfo.getConnection());

PropertyList innerProps = new PropertyList();
innerProps.setProperty("sdcid",  sdcid);
innerProps.setProperty("keyid1", keyid1);

ap.processAction("AddSDI", "1", innerProps);

if (this.errorHandler.hasErrors()) {
    throw new SapphireException("INNER_ACTION_FAILED",
        this.errorHandler.getLastErrorMessage());
}
```

Always check `errorHandler.hasErrors()` after delegating to another action.

---

## 13. SDCProcessor — Dynamic Table/Key Resolution

Use `SDCProcessor` whenever the action must work generically across multiple SDC types rather than hardcoding table names.

```java
SDCProcessor sdc  = this.getSDCProcessor();
String tableName  = sdc.getProperty(sdcid, "tableid");
int    keyCols    = Integer.parseInt(sdc.getProperty(sdcid, "keycolumns"));

StringBuilder sql = new StringBuilder("SELECT col1 FROM ")
    .append(tableName).append(" WHERE ");
Object[] params = new Object[keyCols];

for (int i = 1; i <= keyCols; i++) {
    String colName = sdc.getProperty(sdcid, "keycolid" + i);
    params[i - 1]  = properties.getProperty("keyid" + i);
    if (i > 1) sql.append(" AND ");
    sql.append(colName).append(" = ?");
}
```

---

## 14. Action Registration Checklist

Before deploying, confirm:

- [ ] Fully-qualified class name entered correctly in System Admin → Actions
- [ ] Action Identifier matches the `ID` constant in the interface
- [ ] Version matches `VERSIONID` in the interface (`"1"` unless versioned)
- [ ] All `Input` properties declared with correct Property IDs
- [ ] All `Output` properties declared with correct Property IDs
- [ ] JAR deployed to server classpath and server restarted

---

## 15. Quick Anti-Pattern Reference

| Anti-Pattern | Correct Pattern |
|---|---|
| Raw string concat in SQL | Always use `SafeSQL` / `Object[]` params |
| Ignoring `executePreparedUpdate` return value | Check `rows < 1` and throw |
| Not overriding `isDatabaseRequired()` for pure logic | Override to `return false` |
| Magic string property names in implementation | Declare as interface constants |
| Wrapping `SapphireException` in another `SapphireException` | Re-throw as-is |
| `catch (Exception e) { /* silent */ }` | Log + re-throw as `UNEXPECTED_ERROR` |
| Calling `this.dbUtil` for standard queries | Use `getQueryProcessor()` or `this.database` |
| Hardcoding table names for generic SDC queries | Use `SDCProcessor.getProperty(sdcid, "tableid")` |
| Writing boolean outputs as `"true"`/`"false"` | Use `"Yes"` / `"No"` — LV convention |
| Not initializing output properties on error path | Set all outputs to `""` in a `finally` block or on every code path |
