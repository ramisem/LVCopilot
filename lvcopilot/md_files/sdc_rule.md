# LabVantage SDC Rule Implementation Guide

## Table of Contents

1. [What is an SDC Rule?](#1-what-is-an-sdc-rule)
2. [Core Architecture](#2-core-architecture)
3. [BaseSDCRules Class Reference](#3-basesdcrules-class-reference)
4. [Hook Methods Reference](#4-hook-methods-reference)
5. [Requires Flags — Controlling Framework Data Loading](#5-requires-flags--controlling-framework-data-loading)
6. [Change Detection — Before-Edit Image](#6-change-detection--before-edit-image)
7. [SDIData Structure](#7-sdidata-structure)
8. [Database Access in SDC Rules](#8-database-access-in-sdc-rules)
9. [Error Handling](#9-error-handling)
10. [ConnectionInfo: User Session Context](#10-connectioninfo-user-session-context)
11. [Creating a Custom SDC Rule (Step-by-Step)](#11-creating-a-custom-sdc-rule-step-by-step)
12. [Deployment and Registration](#12-deployment-and-registration)
13. [Example Implementations](#13-example-implementations)
14. [Common Patterns and Best Practices](#14-common-patterns-and-best-practices)

---

## 1. What is an SDC Rule?

An **SDC Rule** (System Data Collection Rule) is a Java POJO class that hooks into the LabVantage data lifecycle for a specific SDC (entity type). Where an LV Action is invoked explicitly (by a workflow, event plan, or button), an SDC Rule fires **automatically** whenever the framework performs a create, edit, delete, approve, or other operation on that SDC's data.

SDC Rules are the correct mechanism for:

- **Pre-validation** — reject invalid data before it is written to the database
- **Data defaulting / derivation** — auto-populate or transform fields before persistence
- **Cross-record integrity** — cascade updates or validate referential constraints
- **Post-processing** — trigger follow-on operations after a record change commits
- **Delete protection** — prevent removal of records that are in use

### SDC Rules vs. LV Actions

| Aspect | SDC Rule | LV Action |
|---|---|---|
| **Trigger** | Automatic — any data operation on the SDC | Explicit — invoked by workflow, event plan, button |
| **Base class** | `BaseSDCRules` | `BaseAction` |
| **Entry point** | Lifecycle hook methods (`preAdd`, `postEdit`, …) | Single `processAction(PropertyList)` |
| **Data in** | `SDIData` (full record graph) or `DataSet` | `PropertyList` (named properties) |
| **Registration** | Naming convention + profile property | System Admin → Actions |
| **Multiple custom rules** | Yes — chained by package sequence | N/A |

---

## 2. Core Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Framework Operation                       │
│  (AddSDI / EditSDI / DeleteSDI / Approve / Release / …)     │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│               BaseSDCRules.getInstance(…)                    │
│                                                             │
│  1. Load system rules:                                      │
│     com.labvantage.sapphire.admin.ddt.<SdcId>               │
│  2. Load custom rules (in sequence):                        │
│     <customrulesjavapackage>.<SdcId>                        │
│     (package name read from profile property)               │
│                                                             │
│  All rule instances share:                                  │
│    database      (DBAccess)                                 │
│    errorHandler  (ErrorHandler)                             │
│    logger        (Logger)                                   │
└────────────────────┬────────────────────────────────────────┘
                     │  calls lifecycle hook on each rule in sequence
                     ▼
┌─────────────────────────────────────────────────────────────┐
│   SystemRules (com.labvantage.sapphire.admin.ddt.SdcId)     │
│   → preAdd / preEdit / preDelete / postAdd / …              │
└─────────────────────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│   CustomRule1 (<package1>.SdcId)                            │
│   CustomRule2 (<package2>.SdcId)  … (in sequence order)    │
└─────────────────────────────────────────────────────────────┘
                     │
                     ▼  (if no VALIDATION/CONFIRM errors)
┌─────────────────────────────────────────────────────────────┐
│               Database Write / Operation                     │
└─────────────────────────────────────────────────────────────┘
```

### Custom Rule Loading

The framework reads a profile property named `customrulesjavapackage` (and optionally `customrulesjavapackage2`, etc.) from the `(system)` profile to find custom rule packages. The execution order of multiple packages is controlled by `customrulessequence` (semicolon-delimited list of property names).

Your class **must be named exactly the same as the SDC ID** (case-sensitive):

```
Profile property:   customrulesjavapackage = com.client.rules
SDC ID:             SampleType
Loaded class:       com.client.rules.SampleType
```

---

## 3. BaseSDCRules Class Reference

```java
package sapphire.action;
public class BaseSDCRules extends BaseCustom {

    // ── Error type constants ──────────────────────────────────
    public static final String TYPE_CONFIRM     = "CONFIRM";
    public static final String TYPE_VALIDATION  = "VALIDATION";
    public static final String TYPE_INFORMATION = "INFORMATION";

    // ── Protected members (accessible in your subclass) ───────
    protected DBAccess      database;       // DBMS-agnostic database access
    protected ConnectionInfo connectionInfo; // current user session

    // ── Private — access via methods listed below ─────────────
    // ErrorHandler errorHandler  → setError(), throwError(), hasErrors()
    // PropertyList sdc           → getSdcProps()
    // String sdcid               → getSdcid()
    // SDIData beforeEditImage    → getBeforeEditImage()
    // String event               → getEvent()
}
```

### Accessor Methods

| Method | Return | Description |
|---|---|---|
| `getSdcid()` | `String` | SDC identifier (e.g., `"SampleType"`) |
| `getSdcProps()` | `PropertyList` | SDC metadata (keycolid1–3, tableid, etc.) |
| `getEvent()` | `String` | Current lifecycle event name |
| `getConnectionInfo()` | `ConnectionInfo` | User session details |
| `getBeforeEditImage()` | `SDIData` | Pre-edit snapshot (only if `requiresBeforeEditImage()` returns `true`) |
| `getCustomRuleList()` | `BaseSDCRules[]` | Array of custom rule instances chained after this one |
| `getDatabaseid()` | `String` | Database ID from connection info |
| `isCMTImport()` | `boolean` | `true` when operation is triggered by a CMT import |
| `getSDIView(SDIData)` | `SDIView` | SDI view helper for the given data |

### SDC Property Keys (from `getSdcProps()`)

| Key | Description |
|---|---|
| `"keycolid1"` | Column name for first key |
| `"keycolid2"` | Column name for second key (empty if not composite) |
| `"keycolid3"` | Column name for third key (empty if not composite) |
| `"keycolumns"` | Number of key columns: `"1"`, `"2"`, or `"3"` |
| `"tableid"` | Underlying DB table name |
| `"auditedflag"` | `"Y"` if audit trail is enabled |

---

## 4. Hook Methods Reference

All hook methods have empty default implementations in `BaseSDCRules` — override only the ones you need.

### Primary Record Hooks

| Method Signature | When Called |
|---|---|
| `preAdd(SDIData sdiData, PropertyList actionProps)` | Before the SDI record is inserted |
| `postAdd(SDIData sdiData, PropertyList actionProps)` | After the SDI record is inserted |
| `preAddKey(DataSet primary, PropertyList actionProps)` | Before the primary key is assigned — use to block or alter key generation; throws `SapphireException` |
| `postAddKey(DataSet primary, PropertyList actionProps)` | After the primary key is assigned — **does not declare `throws SapphireException`** — override here to replace the system-generated key with a custom sequence (see Key Generation section below) |
| `preEdit(SDIData sdiData, PropertyList actionProps)` | Before the SDI record is updated |
| `postEdit(SDIData sdiData, PropertyList actionProps)` | After the SDI record is updated |
| `preDelete(String rsetid, PropertyList actionProps)` | Before the SDI is deleted; `rsetid` = rset of keys |
| `postDelete(String rsetid, PropertyList actionProps)` | After the SDI is deleted; `rsetid` = deleted keys |

### Detail Record Hooks

| Method Signature | When Called |
|---|---|
| `preAddDetail(SDIData sdiData, PropertyList actionProps)` | Before a detail (child) record is inserted |
| `postAddDetail(SDIData sdiData, PropertyList actionProps)` | After a detail record is inserted |
| `preEditDetail(SDIData sdiData, PropertyList actionProps)` | Before a detail record is updated |
| `postEditDetail(SDIData sdiData, PropertyList actionProps)` | After a detail record is updated |
| `preDeleteDetail(String rsetid, PropertyList actionProps)` | Before a detail record is deleted |
| `postDeleteDetail(String rsetid, PropertyList actionProps)` | After a detail record is deleted |

> **Note:** By default, `SDIData` in detail hooks contains only detail data. If you also need the primary record, override `requiresEditDetailPrimary()` to return `true` — the framework will then include the primary dataset.

### Data Entry Hooks (Result Recording)

| Method Signature | When Called |
|---|---|
| `preEditSDIDataItem(SDIData sdiData, PropertyList actionProps)` | Before a single data item value is edited |
| `postEditSDIDataItem(SDIData sdiData, PropertyList actionProps)` | After a single data item value is edited |
| `preEditSDIData(SDIData sdiData, PropertyList actionProps)` | Before a batch data entry operation |
| `postEditSDIData(SDIData sdiData, PropertyList actionProps)` | After a batch data entry operation |
| `preEditSDIDataApproval(SDIData sdiData, PropertyList actionProps)` | Before data approval edit |
| `postEditSDIDataApproval(SDIData sdiData, PropertyList actionProps)` | After data approval edit |
| `postDataEntry(SDIData sdiData, PropertyList actionProps)` | After the entire data entry completes |

### Dataset Hooks

| Method Signature | When Called |
|---|---|
| `preAddDataSet(SDIData sdiData, PropertyList actionProps)` | Before a new dataset (result row) is added |
| `postAddDataSet(SDIData sdiData, PropertyList actionProps)` | After a new dataset is added |

### WorkItem Hooks

| Method Signature | When Called |
|---|---|
| `preAddWorkItem(SDIData sdiData, PropertyList actionProps)` | Before a work item is assigned |
| `postAddWorkItem(SDIData sdiData, PropertyList actionProps)` | After a work item is assigned |
| `preEditWorkItem(SDIData sdiData, PropertyList actionProps)` | Before a work item is edited |
| `postEditWorkItem(SDIData sdiData, PropertyList actionProps)` | After a work item is edited |

### Approval and Release Hooks

| Method Signature | When Called |
|---|---|
| `preApprove(DataSet approve)` | Before the SDI approval record is written |
| `postApprove(DataSet approve)` | After the SDI is approved |
| `preReleaseData(DataSet releaseData, PropertyList actionProps)` | Before data results are released |
| `postReleaseData(DataSet releaseData, PropertyList actionProps)` | After data results are released |

### Attribute Hooks

| Method Signature | When Called |
|---|---|
| `preAddAttribute(SDIData sdiData, PropertyList actionProps)` | Before an attribute is added |
| `postAddAttribute(SDIData sdiData, PropertyList actionProps)` | After an attribute is added |
| `preEditAttribute(SDIData sdiData, PropertyList actionProps)` | Before an attribute is edited |
| `postEditAttribute(SDIData sdiData, PropertyList actionProps)` | After an attribute is edited |
| `preDeleteAttribute(String rsetid, PropertyList actionProps)` | Before an attribute is deleted |
| `postDeleteAttribute(String rsetid, PropertyList actionProps)` | After an attribute is deleted |

### Note Hooks

| Method Signature | When Called |
|---|---|
| `preAddNote(SDIData sdiData, PropertyList actionProps)` | Before a note is added |
| `postAddNote(SDIData sdiData, PropertyList actionProps)` | After a note is added |
| `preEditNote(SDIData sdiData, PropertyList actionProps)` | Before a note is edited |
| `postEditNote(SDIData sdiData, PropertyList actionProps)` | After a note is edited |

### Attachment Hooks

| Method Signature | When Called |
|---|---|
| `preGetSDIAttachment(Attachment attachment)` | Before an attachment is retrieved (download) |
| `preAddSDIAttachment(Attachment attachment)` | Before an attachment is uploaded |
| `postAddSDIAttachment(Attachment attachment)` | After an attachment is uploaded |
| `preEditSDIAttachment(Attachment attachment, Attachment preEditAttachment)` | Before attachment metadata is edited |
| `postEditSDIAttachment(Attachment attachment)` | After attachment metadata is edited |
| `preDeleteSDIAttachment(Attachment attachment)` | Before an attachment is deleted |
| `postDeleteSDIAttachment(Attachment attachment)` | After an attachment is deleted |

### CMT Import and Snapshot Hooks

| Method Signature | When Called |
|---|---|
| `preCMTImport(SDIData sdiData, PropertyList actionProps, boolean isAddSDI)` | Before CMT import; `isAddSDI=true` for new record |
| `postCMTImport(SDIData sdiData, PropertyList actionProps, boolean isAddSDI)` | After CMT import |
| `postGenerateSnapshot(Snapshot snapshot, boolean isPackaging)` | After a CMT snapshot is generated |

---

### Key Generation Hooks — Overriding the System-Generated Primary Key

`preAddKey` and `postAddKey` fire during the `AddSDI` operation around the framework's own primary-key generation. The critical difference from every other hook pair:

> **`postAddKey` does NOT declare `throws SapphireException`.**
> Its exact signature is `public void postAddKey(DataSet primary, PropertyList actionProps)` with no checked exception. You cannot throw `SapphireException` from this method — log errors and return instead.

| Hook | Signature | Use Case |
|---|---|---|
| `preAddKey` | `throws SapphireException` | Validate or block key generation before it happens |
| `postAddKey` | no `throws` | Override or supplement the key after the system assigns it |

**Overriding the key in `postAddKey` using `getSequenceProcessor()`:**

```java
@Override
public void postAddKey(DataSet primary, PropertyList actionProps) {
    // Read the actual key column name from SDC metadata
    String keyCol = getSdcProps().getProperty("keycolid1", "keyid1");

    int seq = getSequenceProcessor().getSequence(getSdcid(), getSdcid() + "_SEQ", 1, 1);
    if (seq < 0) {
        // getSequence returns -1 on error; cannot throw SapphireException here — log and bail
        logger.error("postAddKey (" + getSdcid() + "): getSequence returned error — key not overridden");
        return;
    }

    for (int i = 0; i < primary.size(); i++) {
        String customKey = String.format("SMP-%06d", seq);
        primary.setString(i, keyCol, customKey);
        logger.info("postAddKey (" + getSdcid() + "): assigned custom key " + customKey);
    }
}
```

Key points:
- Read the actual key column name from `getSdcProps().getProperty("keycolid1", "keyid1")` rather than hardcoding it.
- `getSequenceProcessor().getSequence()` returns `-1` on error — always guard against it before formatting the key.
- Since `postAddKey` has no `throws SapphireException`, error handling must log-and-return rather than throw.
- The `DataSet primary` here is the key dataset (generated by the framework), not the same reference as the `SDIData` primary dataset from `preAdd`/`postAdd`.

---

## 5. Requires Flags — Controlling Framework Data Loading

These boolean methods tell the framework what additional data to load before invoking the hook. Override to `return true` only for the hooks you actually implement to avoid unnecessary DB overhead.

| Method | Default | What it enables |
|---|---|---|
| `requiresBeforeEditImage()` | `false` | Loads the full `SDIData` snapshot before edit into `beforeEditImage` for change detection in `preEdit`/`postEdit` |
| `requiresBeforeEditDetailImage()` | `false` | Loads pre-edit snapshot for detail record change detection |
| `requiresBeforeEditSDIDataImage()` | `false` | Loads pre-edit snapshot for data entry change detection |
| `requiresBeforeEditWorkItemImage()` | `false` | Loads pre-edit snapshot for work item change detection |
| `requiresBeforeEditSDIAttributeImage()` | `false` | Loads pre-edit snapshot for attribute change detection |
| `requiresBeforeDataEntryImage()` | `false` | Loads pre-entry snapshot for data entry hooks |
| `requiresBeforeDataReleaseImage()` | `false` | Loads pre-release snapshot for release hooks |
| `requiresEditDetailPrimary()` | `false` | Includes primary record in `SDIData` for detail hooks |
| `requiresEditSDIDataPrimary()` | `false` | Includes primary record in `SDIData` for data entry hooks |
| `requiresAddDataSetPrimary()` | `false` | Includes primary record in `SDIData` for dataset hooks |
| `requiresAddWorkItemPrimary()` | `false` | Includes primary record in `SDIData` for workitem add hooks |
| `requiresEditWorkItemPrimary()` | `false` | Includes primary record in `SDIData` for workitem edit hooks |
| `requiresDataEntryPrimary()` | `false` | Includes primary record for data entry operations |
| `requiresDataReleasePrimary()` | `false` | Includes primary record for data release operations |

**Example — enable primary record in detail hooks:**

```java
@Override
public boolean requiresEditDetailPrimary() {
    return true;
}

@Override
public void preEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    DataSet primary = sdiData.getDataset("primary");   // available because of flag above
    DataSet detail  = sdiData.getDataset("detail");
    // ...
}
```

**Example — enable change detection in preEdit:**

```java
@Override
public boolean requiresBeforeEditImage() {
    return true;   // tell framework to load the before-edit snapshot
}

@Override
public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    DataSet primary = sdiData.getDataset("primary");
    for (int i = 0; i < primary.size(); i++) {
        if (hasPrimaryValueChanged(primary, i, "statusid")) {
            String oldStatus = getOldPrimaryValue(primary, i, "statusid");
            String newStatus = primary.getValue(i, "statusid");
            // react to the status change
        }
    }
}
```

---

## 6. Change Detection — Before-Edit Image

When `requiresBeforeEditImage()` returns `true`, the framework takes a snapshot of the SDI before the edit and stores it in `beforeEditImage`. The following methods compare the new data against that snapshot.

### Primary Record Change Detection

```java
// Returns true if columnId value differs from the snapshot
boolean hasPrimaryValueChanged(DataSet newPrimary, int primaryRow, String columnId)

// Returns the old String value from the snapshot ("" if not found)
String getOldPrimaryValue(DataSet newPrimary, int primaryRow, String columnId)

// Returns the old Calendar value from the snapshot (null if not found)
Calendar getOldPrimaryCalendar(DataSet newPrimary, int primaryRow, String columnId)
```

**Usage:**

```java
DataSet primary = sdiData.getDataset("primary");
for (int i = 0; i < primary.size(); i++) {
    if (hasPrimaryValueChanged(primary, i, "priorityid")) {
        String oldPriority = getOldPrimaryValue(primary, i, "priorityid");
        String newPriority = primary.getValue(i, "priorityid");
        // ... handle priority change
    }
}
```

### WorkItem Change Detection

```java
// Requires requiresBeforeEditWorkItemImage() = true
boolean hasSDIWorkItemValueChanged(DataSet newSDIWI, int row, String columnId)
String  getOldSDIWorkItemValue(DataSet newSDIWI, int row, String columnId)
```

### Data Entry Change Detection

```java
// Requires requiresBeforeEditSDIDataImage() = true
boolean hasSDIDataValueChanged(DataSet newSDIData, int row, String columnId)
String  getOldSDIDataValue(DataSet newSDIData, int row, String columnId)
```

### Attribute Change Detection

```java
// Requires requiresBeforeEditSDIAttributeImage() = true
boolean hasSDIAttributeValueChanged(DataSet newAttribute, int row, String columnId)
String  getOldSDIAttributeValue(DataSet newAttribute, int row, String columnId)
```

---

## 7. SDIData Structure

`SDIData` is the container for a complete SDI record hierarchy. In hook methods that receive it, you retrieve specific datasets by name.

### Retrieving Datasets

```java
DataSet primary    = sdiData.getDataset("primary");       // or SDIData.PRIMARY
DataSet detail     = sdiData.getDataset("detail");
DataSet attribute  = sdiData.getDataset("attribute");     // SDI attributes
DataSet dataset    = sdiData.getDataset("dataset");       // result datasets
DataSet dataitem   = sdiData.getDataset("dataitem");      // individual result values
DataSet sdiWI      = sdiData.getDataset("sdiworkitem");   // work items
DataSet attachment = sdiData.getDataset("attachment");    // attachments
DataSet approval   = sdiData.getDataset("dataapproval");  // approval records
DataSet datalimit  = sdiData.getDataset("datalimit");     // limit checks
```

### Standard Dataset Table Names

| Name | Contains |
|---|---|
| `"primary"` / `SDIData.PRIMARY` | Primary record row(s) |
| `"detail"` | Detail (child) records |
| `"attribute"` / `"sdiattribute"` | Custom attribute values |
| `"dataset"` | Result dataset header records |
| `"dataitem"` | Individual result data values |
| `"datalimit"` | Parameter limit definitions |
| `"dataapproval"` | Approval records |
| `"datarelation"` | SDI relationships |
| `"dataspec"` | Specification records |
| `"sdiworkitem"` | Work item assignments |
| `"attachment"` | Attachment metadata |
| `"coc"` | Chain of custody records |
| `"address"` | Address records |

### Working with a DataSet

```java
DataSet primary = sdiData.getDataset("primary");
if (primary == null || primary.getRowCount() == 0) {
    return;  // nothing to process
}

for (int i = 0; i < primary.size(); i++) {
    String keyid1  = primary.getValue(i, "keyid1");      // String
    String status  = primary.getValue(i, "statusid", ""); // with default
    int    count   = primary.getInt(i, "samplecount");
    // Modify a value in-place (before the DB write in pre-hooks)
    primary.setString(i, "derivedflag", "Y");
    primary.setNumber(i, "calculatedqty", new BigDecimal("10.5"));
}
```

---

## 8. Database Access in SDC Rules

`BaseSDCRules` extends `BaseCustom`, which provides all the same processor accessors available in `BaseAction`. The two primary database handles are:

| Handle / Method | Type | Use |
|---|---|---|
| `this.getQueryProcessor()` | `QueryProcessor` | **Primary** — parameterized SELECT; returns `DataSet` |
| `this.database` | `DBAccess` | Streaming cursors; INSERT / UPDATE / DELETE |

**Always use parameterized queries to prevent SQL injection.**

### QueryProcessor — SELECT Queries (Primary Pattern)

`getQueryProcessor()` is inherited from `BaseCustom` and is the preferred way to execute SELECT queries in an SDC rule. It returns a `DataSet` — a typed, random-access, re-iterable result container.

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
    new Object[]{ keyid1, statusid }
);

for (int i = 0; i < ds.getRowCount(); i++) {
    String col1 = ds.getValue(i, "col1");
    String col2 = ds.getValue(i, "col2", "");  // with default
}
```

**COUNT query:**

```java
QueryProcessor qp = this.getQueryProcessor();
int count = qp.getPreparedCount(
    "SELECT COUNT(*) FROM s_sample WHERE sdcid = ?",
    new Object[]{ sdcid }
);
```

**Raw SQL (no parameters — use sparingly):**

```java
DataSet ds = this.getQueryProcessor().getSqlDataSet(
    "SELECT col1 FROM my_table WHERE activeflag = 'Y'"
);
```

**Security filter clause (row-level security):**

```java
String secWhere = this.getQueryProcessor().getSecurityFilterWhere(sdcid);
DataSet ds = this.getQueryProcessor().getSqlDataSet(
    "SELECT keyid1 FROM s_sample WHERE 1=1 " + secWhere
);
```

**Named LV query (keyed result list):**

```java
String keyList = this.getQueryProcessor().getKeyid1List(sdcid, queryid, params);
```

**QueryProcessor Method Summary:**

| Method | Return | Description |
|---|---|---|
| `getPreparedSqlDataSet(sql, Object[])` | `DataSet` | Parameterized SELECT — primary pattern |
| `getSqlDataSet(sql)` | `DataSet` | Raw SQL SELECT |
| `getPreparedCount(sql, Object[])` | `int` | Parameterized COUNT query |
| `getCount(sql)` | `int` | Raw SQL COUNT |
| `execPreparedUpdate(sql, Object[])` | `int` | Parameterized INSERT/UPDATE/DELETE |
| `execSQL(sql)` | `int` | Raw non-query SQL |
| `getSecurityFilterWhere(sdcid)` | `String` | Row-level security WHERE clause |
| `getKeyid1List(sdcid, queryid, params[])` | `String` | Semicolon-delimited key list from a named LV query |

### Streaming SELECT with DBAccess (Large Result Sets)

Use `this.database` for streaming cursor behaviour over large result sets where loading everything into a `DataSet` is undesirable.

```java
database.createPreparedResultSet(
    "SELECT col1, col2 FROM s_sample WHERE keyid1 = ? AND statusid = ?",
    new Object[]{ keyid1, statusid }
);
while (database.getNext()) {
    String col1 = database.getString("col1");
    int    col2 = database.getInt("col2");
    // process each row
}
```

### SELECT into DataSet via Named PreparedStatement

Use `database.prepareStatement()` when you want a re-usable named statement (as seen in system rule examples):

```java
try {
    PreparedStatement ps = database.prepareStatement(
        "myQueryName",
        "SELECT col1, col2 FROM s_my_table WHERE keyid1 = ?"
    );
    ps.setString(1, keyid1);
    DataSet ds = new DataSet(ps.executeQuery());

    if (ds.getRowCount() > 0) {
        String col1 = ds.getValue(0, "col1");
    }
    ps.close();
} catch (SQLException e) {
    logger.error("Query failed for keyid1=" + keyid1, e);
}
```

### INSERT / UPDATE / DELETE

Use `this.database` for all DML:

```java
// INSERT
int rows = database.executePreparedUpdate(
    "INSERT INTO s_audit_log (keyid1, actionid, actiondate) VALUES (?, ?, SYSDATE)",
    new Object[]{ keyid1, "CREATED" }
);

// UPDATE
database.executePreparedUpdate(
    "UPDATE s_sample SET derivedflag = ? WHERE keyid1 = ?",
    new Object[]{ "Y", keyid1 }
);

// DELETE (raw SQL — use when parameterisation is not needed)
database.executeSQL(
    "DELETE FROM s_map_table WHERE keyid1 = '" + StringUtil.replaceAll(keyid1, "'", "''") + "'"
);
```

> For raw SQL DELETE with a list of keys (as the `rsetid` pattern), see the `postDelete` example in [Section 13](#13-example-implementations).

### DBAccess Method Summary

| Method | Return | Description |
|---|---|---|
| `createPreparedResultSet(sql, Object[])` | void | Open parameterized streaming cursor |
| `createResultSet(sql)` | void | Open plain SQL cursor |
| `getNext()` | boolean | Advance cursor; `false` when exhausted |
| `getString(column)` | String | Current row — column as String |
| `getInt(column)` | int | Current row — column as int |
| `getValue(column)` | Object | Current row — raw column value |
| `prepareStatement(name, sql)` | `PreparedStatement` | Named prepared statement (re-usable) |
| `executePreparedUpdate(sql, Object[])` | int | Parameterized INSERT/UPDATE/DELETE |
| `executeSQL(sql)` | void | Raw SQL (no parameters) |
| `getPreparedCount(sql, Object[])` | int | Parameterized COUNT |

### Sequence Generation — `getSequenceProcessor()`

`getSequenceProcessor()` is inherited from `BaseCustom` and returns a lazy-initialized `SequenceProcessor`. Use it to generate monotonically increasing sequence numbers scoped to an SDC — typically inside `postAddKey` to build custom primary keys that replace the system-assigned value.

```java
import sapphire.accessor.SequenceProcessor;

SequenceProcessor sp = this.getSequenceProcessor();
```

**SequenceProcessor Method Summary:**

| Method | Return | Description |
|---|---|---|
| `getSequence(String sdcid, String sequenceid)` | `int` | Next sequence value; starts at 0, increments by 1 |
| `getSequence(String sdcid, String sequenceid, int incrementby)` | `int` | Next value starting at 0 with custom increment |
| `getSequence(String sdcid, String sequenceid, int startsequencenumber, int incrementby)` | `int` | Full control of start value and increment |
| `getUUID()` | `String` | Server-generated UUID string |

- All `getSequence` overloads return **`-1` on error** — always validate before using the value.
- Sequences are persisted in the database and survive server restarts.
- `sdcid` scopes the sequence namespace; `sequenceid` is the counter name within that namespace — choose a stable, unique name (e.g. `"SAMPLEID_SEQ"`).

**Numeric sequence in `postAddKey`:**

```java
@Override
public void postAddKey(DataSet primary, PropertyList actionProps) {
    int seq = this.getSequenceProcessor().getSequence(getSdcid(), "KEYSEQ", 1, 1);
    if (seq < 0) {
        logger.error("postAddKey (" + getSdcid() + "): getSequence failed — key not overridden");
        return;
    }
    String keyCol = getSdcProps().getProperty("keycolid1", "keyid1");
    for (int i = 0; i < primary.size(); i++) {
        primary.setString(i, keyCol, String.format("SMP-%05d", seq));
    }
}
```

**UUID key in `postAddKey`:**

```java
@Override
public void postAddKey(DataSet primary, PropertyList actionProps) {
    String uuid = this.getSequenceProcessor().getUUID();
    if (uuid == null) {
        logger.error("postAddKey (" + getSdcid() + "): getUUID() returned null — key not overridden");
        return;
    }
    String keyCol = getSdcProps().getProperty("keycolid1", "keyid1");
    for (int i = 0; i < primary.size(); i++) {
        primary.setString(i, keyCol, uuid);
    }
}
```

---

## 9. Error Handling

### setError — Non-Fatal Error Collection

Adds an error to the shared error handler without throwing. Processing continues after this call. When the framework calls `endRule()` after all hooks, it throws `SapphireException` if any `VALIDATION` or `CONFIRM` errors were collected.

```java
// Stops the operation at rule-end; shows validation message to user
setError("MY_RULE_ID", TYPE_VALIDATION, "Sample type cannot be a subtype of itself.");

// Prompts user for confirmation before continuing
setError("CONFIRM_DELETE", TYPE_CONFIRM, "This record has associated data. Continue?");

// Informational — operation proceeds
setError("INFO_001", TYPE_INFORMATION, "Record auto-defaulted to active status.");
```

### throwError — Immediate Fatal Error

Adds the error and **immediately throws** `SapphireException`. Use this to stop processing mid-hook when further execution would be unsafe.

```java
throwError("RULE_ID", TYPE_VALIDATION, "Cannot delete a core reference type.");
// execution stops here — exception is thrown
```

### setWarning — Convenience for TYPE_INFORMATION

```java
setWarning("MY_WARNING", "Field was auto-populated from parent record.");
```

### Error Type Constants

| Constant | Value | Effect |
|---|---|---|
| `TYPE_VALIDATION` | `"VALIDATION"` | Prevents operation; error shown to user |
| `TYPE_CONFIRM` | `"CONFIRM"` | Prompts user; operation proceeds if confirmed |
| `TYPE_INFORMATION` | `"INFORMATION"` | Informational message only; operation proceeds |

> Note: `BaseSDCRules` does **not** define `TYPE_FAILURE`. Use `TYPE_VALIDATION` or throw `SapphireException` directly for hard failures.

### Throwing SapphireException Directly

For hard failures (e.g., unexpected state, impossible condition), throw directly:

```java
throw new SapphireException("Primary dataset not available");
throw new SapphireException("INVALID_STATE", "Record is locked.");
```

### hasErrors

```java
if (hasErrors()) {
    // at least one VALIDATION or CONFIRM error has been collected
}
```

### Logging

The `logger` field (type `sapphire.util.Logger`, inherited from `BaseCustom`) is automatically initialized with the class name as the logger name and the current user connection context. No setup is needed — use it directly in any hook method.

**Logger instance method summary:**

| Method | Level | Notes |
|---|---|---|
| `logger.error(String message)` | ERROR | Uses the class logger name automatically |
| `logger.error(String message, Throwable t)` | ERROR | Includes exception stack trace |
| `logger.error(String loggerName, String message)` | ERROR | Override logger name for this call |
| `logger.error(String loggerName, String message, Throwable t)` | ERROR | Override name + stack trace |
| `logger.warn(String message)` | WARN | |
| `logger.warn(String loggerName, String message)` | WARN | Override logger name |
| `logger.info(String message)` | INFO | |
| `logger.info(String loggerName, String message)` | INFO | Override logger name |
| `logger.debug(Object message)` | DEBUG | Only emitted when debug logging is globally enabled |
| `logger.debug(String loggerName, Object message)` | DEBUG | Override logger name |
| `logger.stackTrace(Throwable t)` | ERROR | Logs stack trace only, no additional message |

**Usage in hooks:**

```java
@Override
public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    DataSet primary = sdiData.getDataset("primary");
    for (int i = 0; i < primary.size(); i++) {
        String keyid1 = primary.getValue(i, "keyid1", "");
        logger.debug("preAdd row " + i + ": keyid1=" + keyid1);
        if (keyid1.isEmpty()) {
            logger.warn("preAdd (" + getSdcid() + "): keyid1 is empty at row " + i);
            setError("MISSING_KEY", TYPE_VALIDATION, "Key ID is required.");
        }
    }
}

@Override
public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    try {
        // ... logic ...
    } catch (Exception e) {
        logger.error("Unexpected error in postEdit for SDC=" + getSdcid(), e);
        throw new SapphireException("Unexpected error processing update");
    }
}
```

**Guarding expensive debug messages:**

```java
if (Logger.isDebugEnabled()) {
    logger.debug("Full dataset: " + primary.toString());
}
```

**`logTrace()` helper (defined in `BaseSDCRules`):**

Writes an INFO-level message automatically prefixed with the current event name and SDC ID:

```java
logTrace("Validating subtype constraints for " + keyid1);
// Emits: "preAdd (SampleType): Validating subtype constraints for ABC123"
```

Use `logTrace()` for entry/exit traces within hook logic; use `logger.error()` / `logger.warn()` for structured error and warning messages.

---

## 10. ConnectionInfo: User Session Context

`this.connectionInfo` is the current user session (type `com.labvantage.sapphire.services.ConnectionInfo`):

```java
// User identity
String userId    = connectionInfo.getSysUserId();       // e.g., "JSMITH"
String logonName = connectionInfo.getLogonName();

// Database
String dbmsType  = connectionInfo.getDbms();            // "ORA" or "MSS"
String databaseId = connectionInfo.getDatabaseId();     // logical DB ID

// I18N
String language  = connectionInfo.getLanguage();
String locale    = connectionInfo.getLocale();
String timeZone  = connectionInfo.getTimeZone();
```

Also used when constructing services that require a `SapphireConnection`:

```java
ConfigService config = new ConfigService(
    new SapphireConnection(database.getConnection(), connectionInfo)
);
String propValue = config.getProfileProperty("(system)", "myproperty");
```

---

## 11. Creating a Custom SDC Rule (Step-by-Step)

### Step 1: Determine the SDC ID

Find the SDC ID in LabVantage System Admin (System → SDC Maintenance). The class name **must** match the SDC ID exactly (case-sensitive).

For example, for SDC `SampleType`, your class must be named `SampleType`.

### Step 2: Create the Class

```java
package com.client.rules;   // must match customrulesjavapackage profile property

import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class SampleType extends BaseSDCRules {

    // Override only the hooks you need.
    // Return true on "requires" flags only when you use that data.

    @Override
    public boolean requiresBeforeEditImage() {
        return true;   // only if you compare old vs new values in preEdit/postEdit
    }

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); i++) {
            String keyid1 = primary.getValue(i, "sampletypeid");
            // validation / defaulting logic here
            if (keyid1.isEmpty()) {
                setError("MISSING_KEY", TYPE_VALIDATION, "Sample Type ID is required.");
            }
        }
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); i++) {
            if (hasPrimaryValueChanged(primary, i, "statusid")) {
                String newStatus = primary.getValue(i, "statusid");
                if ("INACTIVE".equals(newStatus)) {
                    // check whether still in use before allowing deactivation
                    validateNotInUse(primary.getValue(i, "sampletypeid"));
                }
            }
        }
    }

    private void validateNotInUse(String sampleTypeId) throws SapphireException {
        int count = this.getQueryProcessor().getPreparedCount(
            "SELECT COUNT(*) FROM s_sample WHERE sampletypeid = ?",
            new Object[]{ sampleTypeId }
        );
        if (count > 0) {
            throwError("TYPE_IN_USE", TYPE_VALIDATION,
                "Cannot deactivate SampleType '" + sampleTypeId + "' — active samples reference it.");
        }
    }
}
```

### Step 3: Compile and Package

Compile against the LabVantage Sapphire JAR (`sapphire.jar` and any required `labvantage*.jar` files). Package the compiled classes into a JAR file and deploy it to the application server classpath.

### Step 4: Configure Profile Property

In System Admin → Configuration → Profile Properties, set:

| Property Name | Value |
|---|---|
| `customrulesjavapackage` | `com.client.rules` |

If you have multiple custom rule packages, add `customrulesjavapackage2`, `customrulesjavapackage3`, etc., and control execution order with `customrulessequence` (semicolon-delimited property names).

### Step 5: Restart and Verify

Restart the LabVantage application server. Test the rule by performing the corresponding operation (add/edit/delete) on the SDC via the UI.

---

## 12. Deployment and Registration

### File Deployment

| Item | Location |
|---|---|
| Compiled JAR | Application server classpath (e.g., `WEB-INF/lib/`) |
| Class name | Must match SDC ID — e.g., `SampleType.class` in package `com.client.rules` |

### System Admin Profile Properties

Navigate to: **System Admin → System → Profile Properties** (profile: `(system)`)

| Property | Purpose |
|---|---|
| `customrulesjavapackage` | Java package containing custom rule classes |
| `customrulesjavapackage2` … | Additional packages (if multiple custom rule sets) |
| `customrulessequence` | Semicolon-delimited list of property names defining execution order |
| `customrulessequence_<SdcId>` | SDC-specific execution order override |

### Naming Convention Summary

```
Profile:   customrulesjavapackage = com.client.rules
SDC:       SampleType
Class:     com.client.rules.SampleType extends BaseSDCRules
```

### Caching

The framework caches the package sequence per database ID using `CacheUtil`. If you change profile properties, you may need to restart the application server (or clear the cache) for changes to take effect.

---

## 13. Example Implementations

### Example 1: Validate and Default on Add/Edit (SampleType Pattern)

Validates parent/subtype constraints and auto-sets the `subtypeflag` field — taken from the LabVantage-provided example.

```java
package com.client.rules;

import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SampleType extends BaseSDCRules {

    private static final String COL_PARENT    = "parentsampletypeid";
    private static final String COL_SUBTYPE   = "subtypeflag";
    private static final String COL_KEYID1    = "s_sampletypeid";

    @Override
    public void preAdd(SDIData sdiData, PropertyList list) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        validateAndSetSubType(primary);
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList list) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        validateAndSetSubType(primary);
    }

    @Override
    public void postDelete(String rsetid, PropertyList list) throws SapphireException {
        String keyid1 = list.getProperty("keyid1");
        if (keyid1.length() > 0) {
            // Replace semicolons with ','' for SQL IN clause
            String key = keyid1.replace(";", "','");
            database.executeSQL(
                "DELETE FROM s_preptypesampletypemap " +
                "WHERE sourcesampletypeid IN ('" + key + "') " +
                "   OR destsampletypeid  IN ('" + key + "')"
            );
        }
    }

    private void validateAndSetSubType(DataSet primary) throws SapphireException {
        try {
            PreparedStatement childCheck = database.prepareStatement("subtypes",
                "SELECT s_sampletypeid FROM s_sampletype WHERE " + COL_PARENT + " = ?");
            PreparedStatement parentIsSubtype = database.prepareStatement("issubtype",
                "SELECT " + COL_SUBTYPE + " FROM s_sampletype WHERE " + COL_KEYID1 + " = ?");

            for (int i = 0; i < primary.size(); i++) {
                String parentId  = primary.getValue(i, COL_PARENT, "");
                String typeId    = primary.getValue(i, COL_KEYID1, "");

                if (parentId.equals(typeId)) {
                    throw new SapphireException("Parent Sample Type ID cannot be same as Sample Type ID");
                }

                if (parentId.length() > 0) {
                    // Verify parent is not itself a subtype
                    parentIsSubtype.setString(1, parentId);
                    DataSet flagDs = new DataSet(parentIsSubtype.executeQuery());
                    if (flagDs.getRowCount() > 0 && "Y".equals(flagDs.getValue(0, COL_SUBTYPE, "N"))) {
                        throw new SapphireException(
                            "Select a Parent SampleType which is not a SubType. " +
                            "Following Parent SampleType is defined as SubType: " + parentId);
                    }
                    // Verify this type has no existing children (cannot become a subtype)
                    childCheck.setString(1, typeId);
                    DataSet childDs = new DataSet(childCheck.executeQuery());
                    if (childDs.getRowCount() > 0) {
                        throw new SapphireException(
                            "Cannot set this SampleType as SubType as it is defined as " +
                            "Parent SampleType of: " + childDs.getColumnValues(COL_KEYID1, ", "));
                    }
                    primary.setString(i, COL_SUBTYPE, "Y");
                } else {
                    primary.setString(i, COL_SUBTYPE, "N");
                }
            }
            parentIsSubtype.close();
            childCheck.close();
        } catch (SQLException e) {
            logger.error("Failed to retrieve data from s_sampletype", e);
        }
    }
}
```

### Example 2: Delete Validation and Usage Check (RefType Pattern)

Protects core types from deletion and checks that the type is not referenced by any SDC links.

```java
package com.client.rules;

import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class RefType extends BaseSDCRules {

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        // Check SDC link references before allowing delete
        database.createPreparedResultSet(
            "SELECT sdclink.sdcid, sdclink.reftypeid " +
            "FROM   sdclink, rsetitems " +
            "WHERE  rsetitems.rsetid = ? " +
            "AND    sdclink.reftypeid = rsetitems.keyid1 " +
            "ORDER BY 1",
            new Object[]{ rsetid }
        );
        StringBuilder refs = new StringBuilder();
        int count = 0;
        while (database.getNext() && count < 10) {
            refs.append("<br/>").append(database.getString("sdcid"));
            count++;
        }
        if (refs.length() > 0) {
            boolean more = database.getNext();
            throwError("RefTypeUsed", TYPE_VALIDATION,
                "RefType cannot be deleted — referenced by the following SDC links:" +
                refs + (more ? "<br/>..." : ""));
        }
    }

    @Override
    public boolean requiresEditDetailPrimary() {
        return true;   // need primary record available in detail hooks
    }

    @Override
    public void preEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        checkNotCoreType(sdiData);
    }

    @Override
    public void preDeleteDetail(String rsetid, PropertyList actionProps) throws SapphireException {
        checkCoreTypeByRset(rsetid, "You cannot delete 'Core' reference values");
    }

    private void checkNotCoreType(SDIData sdiData) throws SapphireException {
        DataSet primary = sdiData.getDataset(SDIData.PRIMARY);
        if (primary == null) throw new SapphireException("Primary dataset not available");
        for (int i = 0; i < primary.size(); i++) {
            if ("C".equals(primary.getString(i, "typeflag"))) {
                throw new SapphireException("You cannot modify 'Core' reference types");
            }
        }
    }

    private void checkCoreTypeByRset(String rsetid, String message) throws SapphireException {
        database.createPreparedResultSet(
            "SELECT typeflag FROM reftype, rsetitems " +
            "WHERE reftype.reftypeid = rsetitems.keyid1 AND rsetid = ?",
            new Object[]{ rsetid }
        );
        while (database.getNext()) {
            String flag = database.getString("typeflag");
            if ("C".equals(flag)) {
                throw new SapphireException(message);
            }
        }
    }
}
```

### Example 3: Change Detection — React to Field Change on Edit

```java
package com.client.rules;

import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class TestRequest extends BaseSDCRules {

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); i++) {
            if (!hasPrimaryValueChanged(primary, i, "priorityid")) {
                continue;  // priority unchanged — nothing to do
            }
            String keyid1      = primary.getValue(i, "keyid1");
            String newPriority = primary.getValue(i, "priorityid");
            String oldPriority = getOldPrimaryValue(primary, i, "priorityid");

            logTrace("Priority changed for " + keyid1 + ": " + oldPriority + " → " + newPriority);

            if ("URGENT".equals(newPriority) && !"URGENT".equals(oldPriority)) {
                // Set rush flag when escalated to URGENT
                primary.setString(i, "rushflag", "Y");
            }
        }
    }
}
```

### Example 4: Post-Add Cascade — Create Related Records After Insert

```java
package com.client.rules;

import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class Project extends BaseSDCRules {

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); i++) {
            String projectId = primary.getValue(i, "projectid");
            String templateId = primary.getValue(i, "templateid", "");

            if (templateId.isEmpty()) {
                continue;
            }
            // Copy default task list from template
            database.createPreparedResultSet(
                "SELECT taskid, taskorder FROM s_template_task WHERE templateid = ? ORDER BY taskorder",
                new Object[]{ templateId }
            );
            int order = 1;
            while (database.getNext()) {
                String taskId = database.getString("taskid");
                database.executePreparedUpdate(
                    "INSERT INTO s_project_task (projectid, taskid, taskorder, statusid) VALUES (?, ?, ?, 'OPEN')",
                    new Object[]{ projectId, taskId, order++ }
                );
            }
            logTrace("Copied " + (order - 1) + " tasks from template " + templateId + " to project " + projectId);
        }
    }
}
```

### Example 5: Data Entry Rule — Validate Result Values on Entry

```java
package com.client.rules;

import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

import java.math.BigDecimal;

public class Sample extends BaseSDCRules {

    @Override
    public void preEditSDIData(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet dataItems = sdiData.getDataset("dataitem");
        if (dataItems == null) return;

        for (int i = 0; i < dataItems.size(); i++) {
            String paramListId = dataItems.getValue(i, "paramlistid");
            String resultText  = dataItems.getValue(i, "sdidataitemvalue", "");

            if ("pH".equals(paramListId) && resultText.length() > 0) {
                try {
                    BigDecimal ph = new BigDecimal(resultText);
                    if (ph.compareTo(BigDecimal.ZERO) < 0 || ph.compareTo(new BigDecimal("14")) > 0) {
                        setError("INVALID_PH", TYPE_VALIDATION,
                            "pH value must be between 0 and 14. Entered: " + resultText);
                    }
                } catch (NumberFormatException e) {
                    setError("NON_NUMERIC_PH", TYPE_VALIDATION,
                        "pH must be a numeric value. Entered: " + resultText);
                }
            }
        }
    }
}
```

---

## 14. Common Patterns and Best Practices

### Guard Against Empty Datasets

```java
DataSet primary = sdiData.getDataset("primary");
if (primary == null || primary.getRowCount() == 0) {
    return;
}
```

### Always Iterate All Rows

Bulk operations may deliver multiple rows in one call. Always loop:

```java
for (int i = 0; i < primary.size(); i++) {
    String keyid1 = primary.getValue(i, "keyid1");
    // process each row
}
```

### Prefer setError Over throwError for Multiple Validations

Collect all errors so the user sees everything in one pass:

```java
@Override
public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    DataSet primary = sdiData.getDataset("primary");
    for (int i = 0; i < primary.size(); i++) {
        String name = primary.getValue(i, "name", "");
        String code = primary.getValue(i, "code", "");
        if (name.isEmpty()) {
            setError("MISSING_NAME", TYPE_VALIDATION, "Name is required.");
        }
        if (code.isEmpty()) {
            setError("MISSING_CODE", TYPE_VALIDATION, "Code is required.");
        }
        // framework collects both errors — user sees both at once
    }
}
```

### Use throwError for Immediately Unsafe Conditions

If further processing would cause a deeper failure (e.g., NullPointerException risk), stop immediately:

```java
DataSet primary = sdiData.getDataset("primary");
if (primary == null) {
    throwError("NULL_PRIMARY", TYPE_VALIDATION, "Primary data not available.");
}
// safe to proceed from here
```

### Modifying Primary Data In-Place (Pre-Hooks Only)

In `preAdd` and `preEdit`, you can set values on the DataSet and they will be written to the DB as part of the same operation:

```java
@Override
public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    DataSet primary = sdiData.getDataset("primary");
    for (int i = 0; i < primary.size(); i++) {
        // Auto-set audit fields
        primary.setString(i, "createdby", connectionInfo.getSysUserId());
        // Derive a flag
        String category = primary.getValue(i, "categoryid", "");
        primary.setString(i, "isspecial", "SPECIAL".equals(category) ? "Y" : "N");
    }
}
```

> Do **not** modify DataSet values in `post*` hooks — the record has already been written.

### Avoid Redundant Requires Flags

Only override a `requires*` method to `true` when you actually use that data. Unnecessary flags cause extra DB queries:

```java
// BAD — do not do this unless you actually read beforeEditImage
@Override
public boolean requiresBeforeEditImage() { return true; }

@Override
public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    // ... but never calls hasPrimaryValueChanged() or getBeforeEditImage()
}
```

### Close PreparedStatements

When using `database.prepareStatement()`, always close in a `finally` block or after use:

```java
PreparedStatement ps = null;
try {
    ps = database.prepareStatement("myQuery", "SELECT ... WHERE keyid1 = ?");
    ps.setString(1, keyid1);
    DataSet ds = new DataSet(ps.executeQuery());
    // use ds
} catch (SQLException e) {
    logger.error("Query failed", e);
} finally {
    if (ps != null) {
        try { ps.close(); } catch (SQLException ignore) {}
    }
}
```

### Logging Convention

```java
logTrace("Start preEdit for keyid1=" + keyid1);   // prefixes event + sdcid automatically
logger.info("Custom logic applied to " + keyid1);
logger.error("Unexpected error in preEdit", exception);
```

### Check isCMTImport for Import-Specific Logic

If certain rules should be skipped or relaxed during CMT (Configuration Management Tool) imports:

```java
@Override
public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    if (isCMTImport()) {
        return;  // skip validation during system import
    }
    // normal validation
}
```

### Error Rule ID Naming Convention

Use `UPPER_SNAKE_CASE` rule IDs; name them after the constraint being enforced:

```java
"PARENT_IS_SUBTYPE"      // parent type is itself a subtype
"TYPE_IN_USE"            // type referenced by existing records
"CORE_TYPE_PROTECTED"    // core type cannot be modified
"MISSING_REQUIRED_FIELD" // required field not populated
"INVALID_VALUE_RANGE"    // value outside allowed bounds
```

---

*Document generated from LabVantage 8.8 LIMS documentation and decompiled Sapphire JAR.*
*Sources: `sapphire_jar_decompiled/sapphire/action/BaseSDCRules.java`, `labvantagedoc/Content/Storage/8-8-lims-guide/System_Admin/docs/examples/sdcruleexamples/` — refer to those files for authoritative API detail.*
