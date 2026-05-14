# LabVantage Action Implementation Guide

## Table of Contents

1. [What is an LV Action?](#1-what-is-an-lv-action)
2. [Core Architecture](#2-core-architecture)
3. [BaseAction Class Reference](#3-baseaction-class-reference)
4. [PropertyList: Inputs and Outputs](#4-propertylist-inputs-and-outputs)
5. [Creating a Java Action (Step-by-Step)](#5-creating-a-java-action-step-by-step)
6. [Database Access](#6-database-access) *(QueryProcessor, DBAccess, SafeSQL, DataSet)*
7. [Error Handling](#7-error-handling)
8. [SDI and SDC Concepts](#8-sdi-and-sdc-concepts)
9. [ConnectionInfo: User Session Context](#9-connectioninfo-user-session-context)
10. [ActionConstants Reference](#10-actionconstants-reference)
11. [Action Registration in System Admin](#11-action-registration-in-system-admin)
12. [ActionBlock Framework](#12-actionblock-framework)
13. [Example Implementations](#13-example-implementations)
14. [Common Patterns and Best Practices](#14-common-patterns-and-best-practices)
15. [System Actions Quick Reference](#15-system-actions-quick-reference)

---

## 1. What is an LV Action?

An **LV Action** is the primary mechanism for implementing custom business logic in LabVantage. It is a set of programmatic instructions that:

- Accepts **Input Properties** (named string parameters)
- Executes business logic (database queries, calculations, validations)
- Returns **Output Properties** (named string results)

LabVantage provides two categories of actions:

| Category | Description |
|---|---|
| **System Actions** | Shipped with LabVantage (e.g., `AddSDI`, `DeleteSDI`, `IsNull`) |
| **User Actions** | Custom actions you implement for client business requirements |

### Execution Contexts

Actions can be triggered from:

- **Workflows** — Step or task processing in a workflow
- **Event Plans** — Automatically executed when SDI events occur (SDICreated, StatusChanged, etc.)
- **Tasks (ToDo List)** — Scheduled background processing
- **Limit Rules** — Triggered when parameter limit conditions are met
- **Buttons / Forms** — Called directly via request parameters from UI
- **Other Actions / Action Blocks** — Called programmatically by another action

---

## 2. Core Architecture

```
┌──────────────────────────────────────────────────────────┐
│                      Caller                              │
│  (Workflow / Event Plan / Task / Button / REST)          │
└────────────────────┬─────────────────────────────────────┘
                     │ PropertyList (inputs)
                     ▼
┌──────────────────────────────────────────────────────────┐
│                 ActionProcessor                           │
│  processAction(actionid, versionid, properties)          │
│  processActionBlock(ActionBlock)                         │
└────────────────────┬─────────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────────┐
│                  BaseAction (abstract)                    │
│  startAction() → processAction(PropertyList) → endAction()│
│                                                          │
│  Members available to subclass:                          │
│    this.database        (DBAccess/DBUtil)                │
│    this.dbUtil          (DBUtil - raw DB access)         │
│    this.connectionInfo  (user session)                   │
│    this.errorHandler    (error collection)               │
│    this.logger          (log4j Logger)                   │
│    this.actionid        (current action id)              │
└────────────────────┬─────────────────────────────────────┘
                     │ extends
                     ▼
┌──────────────────────────────────────────────────────────┐
│              YourCustomAction                            │
│  implements sapphire.action.YourInterface                │
│                                                          │
│  @Override                                               │
│  public void processAction(PropertyList p) throws ...    │
└──────────────────────────────────────────────────────────┘
```

### Key Classes and Their Packages

| Class | Package | Purpose |
|---|---|---|
| `BaseAction` | `sapphire.action` | Abstract base — extend this |
| `ActionConstants` | `sapphire.action` | Standard property name constants |
| `ActionProcessor` | `sapphire.accessor` | Executes actions programmatically |
| `ActionBlock` | `sapphire.util` | Sequences multiple actions |
| `PropertyList` | `sapphire.xml` | Input/output property container |
| `DBUtil` / `DBAccess` | `com.labvantage.sapphire` / `sapphire.util` | Database operations |
| `SafeSQL` | `sapphire.util` | SQL injection–safe parameterized queries |
| `SapphireException` | `sapphire` | Action-level exception class |
| `ErrorHandler` | `sapphire.error` | Collects errors during execution |
| `ConnectionInfo` | `sapphire.util` | Current user session information |
| `SDCProcessor` | `sapphire.accessor` | SDC/SDI metadata accessor |

---

## 3. BaseAction Class Reference

All custom actions **must** extend `BaseAction`.

```java
package sapphire.action;
public abstract class BaseAction {
    // ── Constants ────────────────────────────────────────
    public static final String YES              = "Y";
    public static final String NO               = "N";
    public static final String NULL             = "(null)";
    public static final String RETURN_SUCCESS   = "1";
    public static final String RETURN_FAILURE   = "2";

    public static final String TYPE_FAILURE     = "FAILURE";
    public static final String TYPE_CONFIRM     = "CONFIRM";
    public static final String TYPE_VALIDATION  = "VALIDATION";
    public static final String TYPE_INFORMATION = "INFORMATION";

    public static final String DEFAULT_SEPARATOR = ";";

    // ── Protected Members (available in your subclass) ───
    protected ErrorHandler    errorHandler;   // collect errors
    protected DBUtil          dbUtil;         // low-level DB
    protected DBAccess        database;       // DBMS-agnostic DB
    protected ConnectionInfo  connectionInfo; // user session
    protected String          actionid;       // current action id
    protected Logger          logger;         // log4j logger

    // ── Lifecycle (called by framework) ─────────────────
    public void startAction(String actionid, SapphireConnection conn,
                            ErrorHandler eh, boolean nolog) { ... }
    public abstract void processAction(PropertyList properties)
                            throws SapphireException;
    public void endAction() { ... }

    // ── Override if no DB needed ─────────────────────────
    public boolean isDatabaseRequired() { return true; }

    // ── Error helpers ─────────────────────────────────────
    protected int setError(String errorid, String errorType,
                           String message) { ... }
    protected int setError(String errorid, String errorType,
                           String message, Throwable e) { ... }
    protected void setInfoError(String errorid, String message) { ... }
    protected ErrorHandler getErrorHandler() { return errorHandler; }

    // ── Metadata access ───────────────────────────────────
    protected SDCProcessor getSDCProcessor() { ... }
}
```

### Lifecycle Flow

1. **`startAction()`** — Framework calls this. Sets up `database`, `connectionInfo`, `logger`, `errorHandler`, and `actionid`.
2. **`processAction(PropertyList)`** — **Your implementation goes here.** Read inputs, run logic, write outputs.
3. **`endAction()`** — Framework calls this. Closes DB resources, logs execution time.

---

## 4. PropertyList: Inputs and Outputs

`PropertyList` (package `sapphire.xml`) is the container for all action inputs and outputs. It extends `HashMap<String, String>` with case-insensitive key access (all keys stored lowercase).

### Reading Input Properties

```java
String sdcid   = properties.getProperty("sdcid");           // returns "" if missing
String keyid1  = properties.getProperty("keyid1");
String value   = properties.getProperty("myinput", "default"); // with default
```

### Writing Output Properties

```java
properties.setProperty("myoutput", result);
properties.setProperty("isequal", "Yes");   // boolean results use "Yes"/"No"
properties.setProperty("count",   "5");     // numbers as strings
```

### Nested Property Lists

PropertyList supports nested structure using `/` path notation:

```java
PropertyList nested = (PropertyList) properties.getPropertyList("section");
nested.setProperty("field", "value");
```

### Lists (Semicolon-Delimited)

LV convention for multi-value properties is semicolons:

```java
// Reading a list
String list = properties.getProperty("items");    // "a;b;c"
String[] parts = list.split(";");

// Writing a list
properties.setProperty("results", "item1;item2;item3");
```

---

## 5. Creating a Java Action (Step-by-Step)

### Step 1: Declare Your Interface

Create an interface in `sapphire.action` (optional but recommended for framework alignment):

```java
package sapphire.action;

public interface MyCustomAction {
    String ID        = "MyCustomAction";
    String VERSIONID = "1";

    // Input properties
    String PROPERTY_SDCID  = "sdcid";
    String PROPERTY_KEYID1 = "keyid1";
    String PROPERTY_KEYID2 = "keyid2";
    String PROPERTY_KEYID3 = "keyid3";
    String PROPERTY_INPUT1 = "myinput";

    // Output properties
    String RETURN_RESULT = "myresult";
    String RETURN_STATUS = "status";

    // Return value constants
    String STATUS_OK    = "OK";
    String STATUS_ERROR = "ERROR";
}
```

### Step 2: Implement the Action Class

```java
package com.client.actions;

import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;
import sapphire.SapphireException;
import sapphire.util.SafeSQL;

public class MyCustomAction extends BaseAction
        implements sapphire.action.MyCustomAction {

    @Override
    public boolean isDatabaseRequired() {
        // Return false only if this action never touches the database
        return true;
    }

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // 1. Read inputs
        String sdcid  = properties.getProperty(PROPERTY_SDCID);
        String keyid1 = properties.getProperty(PROPERTY_KEYID1);
        String input  = properties.getProperty(PROPERTY_INPUT1);

        // 2. Validate required inputs
        if (sdcid.isEmpty() || keyid1.isEmpty()) {
            throw new SapphireException("MISSING_INPUT",
                "sdcid and keyid1 are required.");
        }

        // 3. Business logic
        String result = performLogic(sdcid, keyid1, input);

        // 4. Write outputs
        properties.setProperty(RETURN_RESULT, result);
        properties.setProperty(RETURN_STATUS, STATUS_OK);
    }

    private String performLogic(String sdcid, String keyid1, String input)
            throws SapphireException {
        // implementation
        return "computed_value";
    }
}
```

### Step 3: Compile and Deploy

1. Compile the class against the LabVantage Sapphire JAR (`sapphire.jar`)
2. Package into a JAR or place the `.class` file on the application server classpath
3. Restart the LabVantage application server if required

### Step 4: Register in System Admin

See [Section 11](#11-action-registration-in-system-admin).

---

## 6. Database Access

`BaseAction` provides two database handles:

| Handle / Method | Type | Use |
|---|---|---|
| `this.getQueryProcessor()` | `QueryProcessor` | **Primary** — use for SELECT queries; returns `DataSet` |
| `this.database` | `DBAccess` | Use for INSERT / UPDATE / DELETE |
| `this.dbUtil` | `DBUtil` | Low-level — direct Oracle/SQL Server access; avoid unless necessary |

**Always use `SafeSQL` or bind-variable arrays for parameterized queries to prevent SQL injection.**

### QueryProcessor — SELECT Queries (Primary Pattern)

`QueryProcessor` (package `sapphire.accessor`) is the standard way to execute SELECT queries inside an LV Action. It is obtained via `this.getQueryProcessor()` (inherited from `BaseAction`) and returns a `DataSet` — a typed, indexed result container.

**Getting a QueryProcessor:**

```java
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;

QueryProcessor qp = this.getQueryProcessor();
```

**Parameterized SELECT — returns DataSet:**

```java
QueryProcessor qp = this.getQueryProcessor();

DataSet ds = qp.getPreparedSqlDataSet(
    "SELECT col1, col2 FROM s_sample WHERE keyid1 = ? AND statusid = ?",
    new Object[]{keyid1, statusid}
);
```

**Reading from a DataSet:**

```java
// Row count
int rows = ds.getRowCount();

// Iterate all rows
for (int i = 0; i < ds.getRowCount(); i++) {
    String col1 = ds.getValue(i, "col1");               // returns String
    String col2 = ds.getValue(i, "col2", "default");    // with fallback default
}

// Single-row result
if (ds.getRowCount() > 0) {
    String value = ds.getValue(0, "col1");
}

// Column count
int cols = ds.getColumnCount();
```

**DataSet column data types:**

| Type Constant | Value | Description |
|---|---|---|
| `DataSet.STRING` | 0 | Text column |
| `DataSet.NUMBER` | 1 | Numeric column |
| `DataSet.DATE` | 2 | Date/timestamp column |
| `DataSet.CLOB` | 3 | Large text column |

**COUNT query via QueryProcessor:**

```java
QueryProcessor qp = this.getQueryProcessor();
int count = qp.getPreparedCount(
    "SELECT COUNT(*) FROM s_sample WHERE sdcid = ?",
    new Object[]{sdcid}
);
if (count < 0) {
    throw new SapphireException("DB_ERROR", "Count query failed.");
}
```

**Raw SQL (no parameters) — use sparingly:**

```java
DataSet ds = qp.getSqlDataSet("SELECT col1 FROM my_table WHERE activeflag = 'Y'");
```

**Security filter clause (row-level security):**

```java
// Appends LabVantage security filter to your WHERE clause
String securityClause = qp.getSecurityFilterWhere(sdcid);
String sql = "SELECT keyid1 FROM s_sample WHERE 1=1 " + securityClause;
DataSet ds = qp.getSqlDataSet(sql);
```

**QueryProcessor Method Summary:**

| Method | Return | Description |
|---|---|---|
| `getPreparedSqlDataSet(sql, Object[])` | `DataSet` | Parameterized SELECT — primary pattern |
| `getSqlDataSet(sql)` | `DataSet` | Raw SQL SELECT |
| `getPreparedCount(sql, Object[])` | `int` | Parameterized COUNT query |
| `getCount(sql)` | `int` | Raw SQL COUNT |
| `execPreparedUpdate(sql, Object[])` | `int` | Parameterized INSERT/UPDATE/DELETE |
| `execSQL(sql)` | void | Raw non-query SQL |
| `getSecurityFilterWhere(sdcid)` | `String` | Row-level security WHERE clause |
| `getKeyid1List(sdcid, queryid, params[])` | `String[]` | Key list from a named LV query |

### SafeSQL — Parameterized Queries

```java
SafeSQL safeSQL = new SafeSQL();

// Bind a variable (generates ? placeholder for prepared statement)
String value = properties.getProperty("myvalue");
safeSQL.addVar(value);   // appends ? and stores value

// Build the SQL string with ? markers
StringBuffer sql = new StringBuffer();
sql.append("SELECT col1, col2 FROM my_table WHERE keyid1 = ");
sql.append(safeSQL.addVar(keyid1));
sql.append(" AND status = ");
sql.append(safeSQL.addVar(status));

// Execute — pass sql.toString() and safeSQL.getValues()
int count = this.database.getPreparedCount(sql.toString(), safeSQL.getValues());
```

### SELECT / COUNT via DBAccess (low-level alternative)

Prefer `QueryProcessor` (above) for SELECT. Use `this.database` directly only when you need streaming cursor behaviour over large result sets.

```java
// Count
SafeSQL safeSQL = new SafeSQL();
StringBuffer sql = new StringBuffer();
sql.append("SELECT COUNT(*) FROM s_sample WHERE keyid1 = ");
sql.append(safeSQL.addVar(keyid1));

int count = this.database.getPreparedCount(sql.toString(), safeSQL.getValues());
if (count < 0) {
    throw new SapphireException("DB_ERROR", "Count query failed.");
}

// Streaming result set (cursor)
SafeSQL safeSQL2 = new SafeSQL();
StringBuffer sql2 = new StringBuffer();
sql2.append("SELECT keyid1, col1, col2 FROM s_sample WHERE sdcid = ");
sql2.append(safeSQL2.addVar(sdcid));

this.database.createPreparedResultSet(sql2.toString(), safeSQL2.getValues());
while (this.database.getNext()) {
    String key  = this.database.getString("keyid1");
    String col1 = this.database.getString("col1");
    int    col2 = this.database.getInt("col2");
}
// Cursor cleaned up automatically in endAction()
```

### INSERT

```java
SafeSQL safeSQL = new SafeSQL();
StringBuffer sql = new StringBuffer();
sql.append("INSERT INTO s_my_table (keyid1, keyid2, col1, createdate) VALUES (");
sql.append(safeSQL.addVar(keyid1));  sql.append(", ");
sql.append(safeSQL.addVar(keyid2));  sql.append(", ");
sql.append(safeSQL.addVar(col1));    sql.append(", ");
sql.append(safeSQL.addVar(DateTimeUtil.getNow()));
sql.append(")");

int rows = this.database.executePreparedUpdate(sql.toString(), safeSQL.getValues());
if (rows < 0) {
    throw new SapphireException("INSERT_FAILED", "Failed to insert record.");
}
```

### UPDATE

```java
SafeSQL safeSQL = new SafeSQL();
StringBuffer sql = new StringBuffer();
sql.append("UPDATE s_my_table SET col1 = ");
sql.append(safeSQL.addVar(newValue));
sql.append(", modifydate = ");
sql.append(safeSQL.addVar(DateTimeUtil.getNow()));
sql.append(" WHERE keyid1 = ");
sql.append(safeSQL.addVar(keyid1));
sql.append(" AND sdcid = ");
sql.append(safeSQL.addVar(sdcid));

int rows = this.database.executePreparedUpdate(sql.toString(), safeSQL.getValues());
if (rows <= 0) {
    throw new SapphireException("UPDATE_FAILED", "Record not found for update.");
}
```

### DELETE

```java
SafeSQL safeSQL = new SafeSQL();
StringBuffer sql = new StringBuffer();
sql.append("DELETE FROM s_my_table WHERE keyid1 = ");
sql.append(safeSQL.addVar(keyid1));

int rows = this.database.executePreparedUpdate(sql.toString(), safeSQL.getValues());
```

### DBAccess Method Summary

| Method | Return | Description |
|---|---|---|
| `createResultSet(sql)` | void | Open a result set (plain SQL) |
| `createPreparedResultSet(sql, values)` | void | Open a parameterized result set |
| `getNext()` | boolean | Advance cursor, returns false when done |
| `getString(column)` | String | Get column value as String |
| `getInt(column)` | int | Get column value as int |
| `getValue(column)` | Object | Get raw column value |
| `getPreparedCount(sql, values)` | int | Execute COUNT query |
| `executePreparedUpdate(sql, values)` | int | Execute INSERT/UPDATE/DELETE |
| `executeSQL(sql)` | void | Execute raw SQL (no params — avoid) |

---

## 7. Error Handling

### SapphireException

Throw `SapphireException` to signal an action failure and stop processing:

```java
throw new SapphireException("ERROR_ID", "Human-readable message");
```

Common error IDs (by convention, use `UPPER_SNAKE_CASE`):

```java
throw new SapphireException("MISSING_INPUT",  "The property 'sdcid' is required.");
throw new SapphireException("INVALID_STATE",  "Record is locked and cannot be modified.");
throw new SapphireException("DB_ERROR",       "Database operation failed.");
throw new SapphireException("NOT_FOUND",      "No record found for keyid1=" + keyid1);
```

### Error Types

The second constructor argument of `setError` / `SapphireException` maps to these framework constants:

| Constant | String Value | Effect |
|---|---|---|
| `TYPE_FAILURE` | `"FAILURE"` | Terminates execution, shows error to user |
| `TYPE_VALIDATION` | `"VALIDATION"` | Validation failure |
| `TYPE_CONFIRM` | `"CONFIRM"` | Prompts user for confirmation |
| `TYPE_INFORMATION` | `"INFORMATION"` | Informational — does not stop execution |

### setError Methods

```java
// Adds an error to errorHandler without throwing — action continues
int code = this.setError("MY_ERROR_ID", TYPE_FAILURE, "Something went wrong.");

// With a caught exception (includes stack trace in log)
try {
    // risky call
} catch (Exception e) {
    this.setError("UNEXPECTED_ERROR", TYPE_FAILURE, e.getMessage(), e);
}

// Informational — does not set failure state
this.setInfoError("INFO_001", "Record was already in the target state.");
```

### Logging

```java
this.logger.info("Processing sdcid=" + sdcid + " keyid1=" + keyid1);
this.logger.warn("Unexpected empty result for query.");
this.logger.error("Database update failed for " + keyid1, exception);
this.logger.debug("SQL: " + sql.toString());
```

---

## 8. SDI and SDC Concepts

### Key Terms

| Term | Meaning |
|---|---|
| **SDC** | System Data Collection — a named entity type (e.g., `s_Sample`, `s_Login`) |
| **SDI** | System Data Item — a single record instance within an SDC |
| **keyid1/2/3** | Composite primary key fields that uniquely identify an SDI |
| **tableid** | Underlying database table name for an SDC |

### SDCProcessor — Metadata Access

`SDCProcessor` provides metadata about SDC definitions:

```java
SDCProcessor sdcProcessor = this.getSDCProcessor();

// Get a single property
String tableName   = sdcProcessor.getProperty(sdcid, "tableid");
String keyCols     = sdcProcessor.getProperty(sdcid, "keycolumns"); // "1", "2", or "3"
String keyCol1Name = sdcProcessor.getProperty(sdcid, "keycolid1");
String keyCol2Name = sdcProcessor.getProperty(sdcid, "keycolid2");

// Check if SDC uses audit trail
String audited = sdcProcessor.getProperty(sdcid, "auditedflag");
```

### Dynamically Building WHERE for Any SDC

```java
SDCProcessor sdcProcessor = this.getSDCProcessor();
SafeSQL safeSQL = new SafeSQL();

String tableName = sdcProcessor.getProperty(sdcid, "tableid");
int keyCols = Integer.parseInt(sdcProcessor.getProperty(sdcid, "keycolumns"));

StringBuffer sql = new StringBuffer("SELECT * FROM ").append(tableName)
    .append(" WHERE ");

for (int i = 1; i <= keyCols; i++) {
    String colName = sdcProcessor.getProperty(sdcid, "keycolid" + i);
    String keyVal  = properties.getProperty("keyid" + i);
    if (i > 1) sql.append(" AND ");
    sql.append(colName).append(" = ").append(safeSQL.addVar(keyVal));
}

this.database.createPreparedResultSet(sql.toString(), safeSQL.getValues());
```

---

## 9. ConnectionInfo: User Session Context

`this.connectionInfo` provides the current user's session details:

```java
// User identity
String userId    = this.connectionInfo.getSysUserId();    // e.g., "ADMIN"
String logonName = this.connectionInfo.getLogonName();

// Database connection
String dbmsType  = this.connectionInfo.getDbms();   // "ORA" or "MSS"
String dbServer  = this.connectionInfo.getServerName();
String dbName    = this.connectionInfo.getDatabaseName();

// Localization
String language  = this.connectionInfo.getLanguage();
String locale    = this.connectionInfo.getLocale();
String timeZone  = this.connectionInfo.getTimeZone();

// Roles / modules (semicolon-delimited)
String roles     = this.connectionInfo.getRoles();
String modules   = this.connectionInfo.getModules();
```

Use `connectionInfo` when your action needs to:
- Store the acting user (e.g., audit columns like `modifiedby`)
- Apply locale-specific formatting
- Branch logic based on user role
- Write DBMS-specific SQL (check `getDbms()`)

---

## 10. ActionConstants Reference

`ActionConstants` (package `sapphire.action`) is an interface that defines string constants for the most common input property names. Implement it or use the constants directly:

### SDI Identification

| Constant | String Value | Description |
|---|---|---|
| `SDCID` | `"sdcid"` | SDC identifier |
| `KEYID1` | `"keyid1"` | First key field |
| `KEYID2` | `"keyid2"` | Second key field |
| `KEYID3` | `"keyid3"` | Third key field |
| `LINKID` | `"linkid"` | Link identifier |
| `DETAILLINKID` | `"detaillinkid"` | Detail link identifier |

### User / Audit

| Constant | String Value | Description |
|---|---|---|
| `SYSUSERID` | `"sysuserid"` | System user performing action |
| `TRACELOGID` | `"tracelogid"` | Audit trace log ID |
| `AUDITREASON` | `"auditreason"` | Reason code for audit |
| `AUDITACTIVITY` | `"auditactivity"` | Activity code for audit |
| `AUDITSIGNEDFLAG` | `"auditsignedflag"` | Electronic signature flag |

### Processing Flags

| Constant | String Value | Description |
|---|---|---|
| `SEPARATOR` | `"separator"` | List delimiter (default `";"`) |
| `FORCEDELETE` | `"forcedelete"` | Bypass delete restrictions |
| `OVERRIDERELEASED` | `"overridereleased"` | Allow edit of released records |
| `AUTORELEASE` | `"autorelease"` | Auto-release after create |
| `APPLYLOCK` | `"applylock"` | Apply record lock |
| `SUPPRESS_EVENT_GENERATION` | `"suppresseventgeneration"` | Skip event firing |

### Workflow

| Constant | String Value | Description |
|---|---|---|
| `WORKFLOWID` | `"workflowid"` | Workflow definition ID |
| `WORKFLOWVERSIONID` | `"workflowversionid"` | Workflow version |
| `WORKFLOWINSTANCE` | `"workflowinstance"` | Running workflow instance |
| `GOTOSTATE` | `"gotostate"` | Workflow state to advance to |

---

## 11. Action Registration in System Admin

**Path:** System Admin → Configuration → Actions

### Action Maintenance Page Fields

| Field | Description |
|---|---|
| **Action Identifier** | Unique ID for the action (e.g., `MyCustomAction`) |
| **Version** | Version string (e.g., `1`) |
| **Type** | `User` for custom actions |
| **Language** | `Java` for Java class; `Action Block` for block-based |
| **Java Object Name** | Fully-qualified class name (e.g., `com.client.actions.MyCustomAction`) |
| **Description** | Human-readable description |

### Defining Action Properties

After creating the action, define its properties on the **Action Properties** tab:

| Field | Description |
|---|---|
| **Property ID** | Must match the string used in `properties.getProperty()` |
| **Type** | LabVantage Data Type (Text, Number, Date, etc.) |
| **Mode** | `Input`, `Output`, or `Input/Output` |
| **Title** | Label shown in Action Block editor |
| **Default Value** | Pre-filled value when property is not supplied |

---

## 12. ActionBlock Framework

An **ActionBlock** is a declarative sequence of one or more actions with conditional branching, variable sharing, and return values. It can be authored graphically in the Action Block Editor or in code.

### Action Block Structure

```xml
<actionblock>
  <blockproperty propertyid="sharedvar" value=""/>
  <action name="step1" actionid="IsNull" versionid="1">
    <property propertyid="sdcid"   value="s_Sample"/>
    <property propertyid="keyid1"  value="[keyid1]"/>
    <property propertyid="columnid" value="releasedflag"/>
  </action>
  <action name="step2" actionid="MyCustomAction" versionid="1"
          test="step1.isnull == 'No'">
    <property propertyid="sdcid"   value="[sdcid]"/>
    <property propertyid="keyid1"  value="[keyid1]"/>
  </action>
  <returnproperty propertyid="myresult" value="[step2.myresult]"/>
</actionblock>
```

### Property Token Syntax

| Token | Description |
|---|---|
| `[propertyid]` | Input property from calling context |
| `[stepname.outputprop]` | Output property from a previous step |
| `[blockproperty]` | Block-level variable shared across steps |
| `[$G{groovy_expr}]` | Groovy expression evaluated at runtime |

### Executing an Action Block Programmatically

```java
// From within another action or servlet:
ActionProcessor ap = new ActionProcessor(connectionInfo);

ActionBlock block = new ActionBlock();
block.setActionClass("step1", "com.client.actions.MyCustomAction",
    buildProperties("sdcid", sdcid, "keyid1", keyid1));

ap.processActionBlock(block);

// Read output
String result = block.getActionProperty("step1", "myresult");
ErrorHandler eh = block.getErrorHandler();
```

### Conditional Execution (test attribute)

The `test` attribute on an `<action>` element is a Groovy expression. The step is skipped if it evaluates to `false`:

```xml
<action name="checkNull" actionid="IsNull" .../>
<action name="doWork"    actionid="MyAction" ...
        test="checkNull.isnull == 'No'"/>
```

---

## 13. Example Implementations

### Example 1: Simple Utility (No Database)

```java
package com.client.actions;

import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class FormatSampleId extends BaseAction {

    @Override
    public boolean isDatabaseRequired() { return false; }

    @Override
    public void processAction(PropertyList properties) {
        String prefix   = properties.getProperty("prefix");
        String sequence = properties.getProperty("sequence");
        String padded   = String.format("%06d", Integer.parseInt(sequence));
        properties.setProperty("sampleid", prefix + "-" + padded);
    }
}
```

### Example 2: Database Query (SELECT via QueryProcessor)

```java
package com.client.actions;

import sapphire.action.BaseAction;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.SapphireException;

public class GetSampleStatus extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        if (sdcid.isEmpty() || keyid1.isEmpty()) {
            throw new SapphireException("MISSING_INPUT",
                "sdcid and keyid1 are required.");
        }

        QueryProcessor qp = this.getQueryProcessor();
        DataSet ds = qp.getPreparedSqlDataSet(
            "SELECT statusid, releasedflag FROM s_sample WHERE keyid1 = ?",
            new Object[]{keyid1}
        );

        String statusid     = "";
        String releasedFlag = "N";

        if (ds.getRowCount() > 0) {
            statusid     = ds.getValue(0, "statusid");
            releasedFlag = ds.getValue(0, "releasedflag", "N");
        }

        properties.setProperty("statusid",     statusid);
        properties.setProperty("releasedflag",  releasedFlag);
    }
}
```

### Example 3: Database Write (INSERT + UPDATE)

```java
package com.client.actions;

import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;
import sapphire.SapphireException;
import sapphire.util.SafeSQL;

public class RecordSampleReview extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid      = properties.getProperty("sdcid");
        String keyid1     = properties.getProperty("keyid1");
        String reviewedBy = this.connectionInfo.getSysUserId();
        String comments   = properties.getProperty("comments");

        // INSERT into audit table
        SafeSQL insertSQL = new SafeSQL();
        StringBuffer ins = new StringBuffer();
        ins.append("INSERT INTO s_sample_review (keyid1, reviewedby, comments, reviewdate) VALUES (");
        ins.append(insertSQL.addVar(keyid1));    ins.append(", ");
        ins.append(insertSQL.addVar(reviewedBy)); ins.append(", ");
        ins.append(insertSQL.addVar(comments));   ins.append(", SYSDATE)");

        int inserted = this.database.executePreparedUpdate(
            ins.toString(), insertSQL.getValues());
        if (inserted < 1) {
            throw new SapphireException("INSERT_FAILED",
                "Failed to insert review record for keyid1=" + keyid1);
        }

        // UPDATE sample status
        SafeSQL updSQL = new SafeSQL();
        StringBuffer upd = new StringBuffer();
        upd.append("UPDATE s_sample SET reviewflag = 'Y', modifiedby = ");
        upd.append(updSQL.addVar(reviewedBy));
        upd.append(" WHERE keyid1 = ");
        upd.append(updSQL.addVar(keyid1));

        this.database.executePreparedUpdate(upd.toString(), updSQL.getValues());

        properties.setProperty("status", "REVIEWED");
        this.logger.info("Sample " + keyid1 + " reviewed by " + reviewedBy);
    }
}
```

### Example 4: Dynamic SDI Query Using SDCProcessor + QueryProcessor

```java
package com.client.actions;

import sapphire.action.BaseAction;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.SapphireException;

public class CheckColumnValue extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid    = properties.getProperty("sdcid");
        String keyid1   = properties.getProperty("keyid1");
        String columnId = properties.getProperty("columnid");

        SDCProcessor sdc = this.getSDCProcessor();
        String tableName = sdc.getProperty(sdcid, "tableid");
        int    keyCols   = Integer.parseInt(sdc.getProperty(sdcid, "keycolumns"));

        // Build parameterized SQL dynamically
        StringBuilder sql = new StringBuilder("SELECT ")
            .append(columnId).append(" FROM ").append(tableName).append(" WHERE ");
        Object[] params = new Object[keyCols];

        for (int i = 1; i <= keyCols; i++) {
            String colName = sdc.getProperty(sdcid, "keycolid" + i);
            params[i - 1]  = properties.getProperty("keyid" + i);
            if (i > 1) sql.append(" AND ");
            sql.append(colName).append(" = ?");
        }

        QueryProcessor qp = this.getQueryProcessor();
        DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), params);

        String colValue = ds.getRowCount() > 0 ? ds.getValue(0, columnId, "") : "";

        properties.setProperty("columnvalue", colValue);
        properties.setProperty("found", colValue.isEmpty() ? "No" : "Yes");
    }
}
```

### Example 5: Calling Another System Action from Within an Action

```java
import sapphire.accessor.ActionProcessor;
import sapphire.util.ActionBlock;
import sapphire.xml.PropertyList;

// Inside processAction():
ActionProcessor ap = new ActionProcessor(/* SapphireConnection from startAction */);

PropertyList innerProps = new PropertyList();
innerProps.setProperty("sdcid",  sdcid);
innerProps.setProperty("keyid1", keyid1);

// processAction(actionid, versionid, properties)
ap.processAction("AddSDI", "1", innerProps);

// Check for errors
if (this.errorHandler.hasErrors()) {
    throw new SapphireException("INNER_ACTION_FAILED",
        this.errorHandler.getLastErrorMessage());
}
```

---

## 14. Common Patterns and Best Practices

### Input Validation Pattern

```java
@Override
public void processAction(PropertyList properties) throws SapphireException {
    String sdcid  = properties.getProperty("sdcid");
    String keyid1 = properties.getProperty("keyid1");

    // Fail fast on missing required inputs
    if (sdcid.isEmpty()) {
        throw new SapphireException("MISSING_SDCID", "Property 'sdcid' is required.");
    }
    if (keyid1.isEmpty()) {
        throw new SapphireException("MISSING_KEYID1", "Property 'keyid1' is required.");
    }
    // ... continue
}
```

### Boolean Output Convention

```java
// Use "Yes"/"No" for boolean results — consistent with LV system actions
properties.setProperty("isfound",   found    ? "Yes" : "No");
properties.setProperty("isvalid",   valid    ? "Yes" : "No");
properties.setProperty("isreleased", released ? "Yes" : "No");
```

### Returning Counts / Numbers

```java
properties.setProperty("count", String.valueOf(count));   // always stringify
```

### Handling NULL / Empty DB Values

```java
String value = this.database.getString("mycolumn");
if (value == null) value = "";
properties.setProperty("mycolumn", value);
```

### isDatabaseRequired Override

Always override to `false` when your action contains no DB operations — improves performance:

```java
@Override
public boolean isDatabaseRequired() { return false; }
```

### Logging Conventions

```java
this.logger.info("MyAction: start sdcid=" + sdcid + " keyid1=" + keyid1);
// ... business logic ...
this.logger.info("MyAction: complete result=" + result);
```

### Error Codes Naming Convention

Use `UPPER_SNAKE_CASE` and make them descriptive:

```java
"MISSING_SDCID"        // missing required input
"INVALID_STATUS"       // value not allowed
"RECORD_NOT_FOUND"     // SELECT returned 0 rows
"INSERT_FAILED"        // INSERT returned 0
"UPDATE_FAILED"        // UPDATE returned 0
"UNEXPECTED_ERROR"     // catch-all for RuntimeException
```

### Wrapping Unexpected Exceptions

```java
try {
    // ... logic ...
} catch (SapphireException e) {
    throw e;   // re-throw LV exceptions as-is
} catch (Exception e) {
    this.logger.error("Unexpected error in MyAction", e);
    throw new SapphireException("UNEXPECTED_ERROR", e.getMessage());
}
```

### Action Does Not Need Database

```java
public class CalculateDilutionFactor extends BaseAction {

    @Override
    public boolean isDatabaseRequired() { return false; }

    @Override
    public void processAction(PropertyList properties) {
        double stock  = Double.parseDouble(properties.getProperty("stockconc"));
        double final_ = Double.parseDouble(properties.getProperty("finalconc"));
        double factor = stock / final_;
        properties.setProperty("dilutionfactor", String.valueOf(factor));
    }
}
```

---

## 15. System Actions Quick Reference

Commonly used LabVantage System Actions you can call from within Action Blocks or via `ActionProcessor`:

### Data Management

| Action ID | Version | Description | Key Inputs |
|---|---|---|---|
| `AddSDI` | 1 | Create a new SDI record | `sdcid`, `keyid1–3` + column values |
| `DeleteSDI` | 1 | Delete an SDI record | `sdcid`, `keyid1–3` |
| `AddDataSet` | 1 | Add a result dataset | `sdcid`, `keyid1–3`, `datasetid` |
| `UnReleaseDataSet` | 1 | Unreleased a dataset | `sdcid`, `keyid1–3`, `datasetid` |
| `AddSDIVersion` | 1 | Create a versioned copy of an SDI | `sdcid`, `keyid1–3` |
| `AddSDIAttachment` | 1 | Upload attachment to an SDI | `sdcid`, `keyid1–3`, `filename` |

### Conditional / Utility

| Action ID | Version | Description | Key Outputs |
|---|---|---|---|
| `IsNull` | 1 | Check if a DB column value is null | `isnull` (Yes/No) |
| `IsStringEqual` | 1 | Compare two string values | `isstringequal` (Yes/No) |
| `IsMatch` | 1 | Pattern match (regex) | `ismatch` (Yes/No) |
| `IsListItem` | 1 | Check if item is in list | `islistitem` (Yes/No) |
| `CompareNumbers` | 1 | Numeric comparison | `result` (>, <, =) |

### List Handling

| Action ID | Version | Description | Key Inputs/Outputs |
|---|---|---|---|
| `AddListItem` | 1 | Append item to semicolon list | `list`, `item` → `newlist` |
| `GetListItem` | 1 | Get item at index from list | `list`, `index` → `item` |
| `GetListItemCount` | 1 | Count items in list | `list` → `count` |

### Counter

| Action ID | Version | Description |
|---|---|---|
| `IncrementCounter` | 1 | Add 1 to a counter SDI field |
| `DecrementCounter` | 1 | Subtract 1 from a counter SDI field |

### Meta

| Action ID | Version | Description |
|---|---|---|
| `ProcessActionBlock` | 1 | Execute another named Action Block |

---

*Document generated from LabVantage 8.8 LIMS documentation and decompiled Sapphire JAR.*
*Source: `labvantagedoc/` and `sapphire_jar_decompiled/` — refer to `concepts-actions.html`, `actionsimplementingpublicinterfaces.html`, `BaseAction.java`, and action implementation examples for authoritative detail.*
