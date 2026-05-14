# LV SDC Rule Best Practices Skill

> **Always read `md_files/sdc_rule.md` alongside this file** before writing any LV SDC Rule.
> This file captures *how* to write good SDC rules — `sdc_rule.md` is the authoritative API reference.

---

## 1. Class Naming and Package Structure

- The class name **must** match the SDC ID exactly (case-sensitive) — this is how the framework loads it.
- Place rule classes in the package declared under profile property `customrulesjavapackage` (e.g., `com.client.rules`), never in `sapphire.*`.
- Extend `BaseSDCRules` directly — do **not** extend another custom rule class.
- Use a private `static final` constant block at the top for all column name literals.

```java
package com.client.rules;   // must match customrulesjavapackage profile property

import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class SampleType extends BaseSDCRules {   // class name == SDC ID

    // Column name constants — never use bare string literals in logic
    private static final String COL_KEYID1    = "sampletypeid";
    private static final String COL_STATUS    = "statusid";
    private static final String COL_PARENT    = "parentsampletypeid";
    private static final String COL_SUBTYPE   = "subtypeflag";

    // Override only the hooks you actually need
}
```

**Why:** The framework resolves the class by combining the package from the profile property with the SDC ID. A name mismatch silently means the rule is never loaded.

---

## 2. Override Only What You Need

`BaseSDCRules` provides empty default implementations for every hook. Override only the hooks your rule actually uses.

```java
// CORRECT — only preAdd and preEdit are relevant for this rule
@Override
public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException { ... }

@Override
public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException { ... }

// Do NOT add empty overrides like:
// @Override
// public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {}
```

**Why:** Empty hook overrides add noise and can mislead future maintainers into thinking logic exists there.

---

## 3. Requires Flags — Enable Only What You Use

Only override a `requires*` method to `return true` when you actually use that data in the corresponding hook. Each flag triggers an extra DB round-trip.

```java
// CORRECT — enabled because preEdit calls hasPrimaryValueChanged()
@Override
public boolean requiresBeforeEditImage() {
    return true;
}

// WRONG — enabled but never used
@Override
public boolean requiresBeforeEditImage() {
    return true;  // ← expensive and misleading if hasPrimaryValueChanged() is never called
}
@Override
public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    // no change detection here — flag above is wasted
}
```

| Flag | Enable only if |
|---|---|
| `requiresBeforeEditImage()` | `preEdit`/`postEdit` calls `hasPrimaryValueChanged()` or `getOldPrimaryValue()` |
| `requiresBeforeEditDetailImage()` | Detail hook compares old vs new detail values |
| `requiresEditDetailPrimary()` | Detail hook reads `sdiData.getDataset("primary")` |
| `requiresEditSDIDataPrimary()` | Data entry hook needs the primary record |
| `requiresBeforeEditSDIDataImage()` | Data entry hook detects old vs new result values |

---

## 4. Guard Every Dataset Access

Always null-check and empty-check before iterating. The framework may invoke hooks where the expected dataset is absent.

```java
DataSet primary = sdiData.getDataset("primary");
if (primary == null || primary.getRowCount() == 0) {
    return;
}

for (int i = 0; i < primary.size(); i++) {
    String keyid1 = primary.getValue(i, COL_KEYID1, "");
    // process
}
```

- Use `primary.getValue(i, col, "")` (three-arg form) for nullable columns to avoid null pointer issues.
- Use `primary.getValue(i, col)` only when the column is guaranteed non-null (primary key columns, etc.).

---

## 5. Always Iterate All Rows

Bulk operations may pass multiple rows in a single hook invocation. Never assume a single row.

```java
// WRONG
String keyid1 = primary.getValue(0, COL_KEYID1);  // misses rows 1..n

// CORRECT
for (int i = 0; i < primary.size(); i++) {
    String keyid1 = primary.getValue(i, COL_KEYID1, "");
    // ...
}
```

---

## 6. Pre-Hook vs. Post-Hook: Modify DataSet in Pre-Hooks Only

- In `pre*` hooks, you can call `primary.setString(i, col, value)` / `primary.setNumber(...)` to default or derive field values — they will be persisted with the DB write.
- In `post*` hooks, the record is already committed. Do **not** modify the DataSet to set field values; issue an explicit `database.executePreparedUpdate(...)` instead.

```java
// PRE-HOOK: in-place field defaulting (correct)
@Override
public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    DataSet primary = sdiData.getDataset("primary");
    for (int i = 0; i < primary.size(); i++) {
        primary.setString(i, "createdby", connectionInfo.getSysUserId());
        primary.setString(i, "activeflag", "Y");
    }
}

// POST-HOOK: cascade insert (correct — explicit DML, not setString on DataSet)
@Override
public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    DataSet primary = sdiData.getDataset("primary");
    for (int i = 0; i < primary.size(); i++) {
        String keyid1 = primary.getValue(i, COL_KEYID1, "");
        database.executePreparedUpdate(
            "INSERT INTO s_audit_log (keyid1, eventid, eventdate) VALUES (?, 'CREATED', SYSDATE)",
            new Object[]{ keyid1 }
        );
    }
}
```

---

## 7. Error Handling — setError vs. throwError

| Situation | Use |
|---|---|
| Multiple validation rules to check in one pass | `setError(...)` — collect all, user sees everything at once |
| Immediately unsafe to continue (null ref, impossible state) | `throwError(...)` — stops mid-hook |
| Hard unexpected failure (not a user error) | `throw new SapphireException(...)` directly |
| Informational message, operation continues | `setError("ID", TYPE_INFORMATION, "msg")` or `setWarning(...)` |

```java
// CORRECT — collect multiple errors so user sees all at once
@Override
public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    DataSet primary = sdiData.getDataset("primary");
    for (int i = 0; i < primary.size(); i++) {
        String name = primary.getValue(i, "name", "");
        String code = primary.getValue(i, "code", "");
        if (name.isEmpty()) setError("MISSING_NAME", TYPE_VALIDATION, "Name is required.");
        if (code.isEmpty()) setError("MISSING_CODE", TYPE_VALIDATION, "Code is required.");
    }
}

// CORRECT — stop immediately on impossible state
DataSet primary = sdiData.getDataset("primary");
if (primary == null) {
    throwError("NULL_PRIMARY", TYPE_VALIDATION, "Primary dataset not available.");
}
```

**Error ID convention:** `UPPER_SNAKE_CASE`, named after the constraint enforced:
- `PARENT_IS_SUBTYPE`, `TYPE_IN_USE`, `CORE_TYPE_PROTECTED`, `MISSING_REQUIRED_FIELD`, `INVALID_VALUE_RANGE`

---

## 8. Database Access — Choose the Right Handle

| Scenario | Use |
|---|---|
| Parameterized SELECT | `getQueryProcessor().getPreparedSqlDataSet(sql, Object[])` |
| COUNT query | `getQueryProcessor().getPreparedCount(sql, Object[])` |
| INSERT / UPDATE / DELETE | `database.executePreparedUpdate(sql, Object[])` |
| Large result set (streaming) | `database.createPreparedResultSet(sql, Object[])` + `getNext()` loop |
| Named/reusable statement | `database.prepareStatement(name, sql)` — always close in `finally` |
| Raw DML with key list (e.g., `postDelete` rsetid) | `database.executeSQL(sql)` — last resort; sanitize manually |
| Custom primary key sequence | `getSequenceProcessor().getSequence(getSdcid(), seqId, start, increment)` — use in `postAddKey`; returns `-1` on error |

**Never** concatenate user-controlled values into SQL strings. Always use parameterized forms.

```java
// CORRECT — parameterized SELECT
DataSet ds = getQueryProcessor().getPreparedSqlDataSet(
    "SELECT col1 FROM s_sample WHERE keyid1 = ? AND statusid = ?",
    new Object[]{ keyid1, statusid }
);

// CORRECT — parameterized DML
database.executePreparedUpdate(
    "UPDATE s_sample SET derivedflag = ? WHERE keyid1 = ?",
    new Object[]{ "Y", keyid1 }
);
```

---

## 9. PreparedStatement Lifecycle — Always Close

When using `database.prepareStatement()`, close in a `finally` block to avoid connection leaks.

```java
PreparedStatement ps = null;
try {
    ps = database.prepareStatement("myQuery",
        "SELECT col1 FROM s_my_table WHERE keyid1 = ?");
    ps.setString(1, keyid1);
    DataSet ds = new DataSet(ps.executeQuery());
    if (ds.getRowCount() > 0) {
        String col1 = ds.getValue(0, "col1");
    }
} catch (SQLException e) {
    logger.error("Query failed for keyid1=" + keyid1, e);
} finally {
    if (ps != null) {
        try { ps.close(); } catch (SQLException ignore) {}
    }
}
```

**Prefer `getQueryProcessor().getPreparedSqlDataSet()`** over raw `PreparedStatement` for simple one-shot queries — it manages the statement lifecycle automatically.

---

## 10. Change Detection Pattern (preEdit / postEdit)

Enable `requiresBeforeEditImage()` **only** if you use change detection. Then always check before reacting.

```java
@Override
public boolean requiresBeforeEditImage() { return true; }

@Override
public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    DataSet primary = sdiData.getDataset("primary");
    if (primary == null || primary.getRowCount() == 0) return;

    for (int i = 0; i < primary.size(); i++) {
        if (!hasPrimaryValueChanged(primary, i, COL_STATUS)) {
            continue;   // field unchanged — skip
        }
        String oldStatus = getOldPrimaryValue(primary, i, COL_STATUS);
        String newStatus = primary.getValue(i, COL_STATUS, "");
        logTrace("Status change for " + primary.getValue(i, COL_KEYID1, "") +
            ": " + oldStatus + " → " + newStatus);
        // react to change
    }
}
```

- Use `continue` to skip unchanged rows cleanly rather than nesting the entire body in an `if`.
- Always pair `hasPrimaryValueChanged()` / `getOldPrimaryValue()` — never call `getOldPrimaryValue()` without the guard.

---

## 11. Detail Hook Pattern

When you need the primary record inside a detail hook, override `requiresEditDetailPrimary()`:

```java
@Override
public boolean requiresEditDetailPrimary() { return true; }

@Override
public void preEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    DataSet primary = sdiData.getDataset("primary");   // available because of flag above
    DataSet detail  = sdiData.getDataset("detail");
    if (primary == null || detail == null) return;

    for (int i = 0; i < detail.size(); i++) {
        // validate detail row against primary
    }
}
```

---

## 12. Delete Protection Pattern

`preDelete` receives `rsetid` (an rset of keys), not an `SDIData`. Use `database.createPreparedResultSet` with the rset to check references.

```java
@Override
public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
    database.createPreparedResultSet(
        "SELECT ref.sdcid FROM ref_table ref, rsetitems ri " +
        "WHERE ri.rsetid = ? AND ref.keyid1 = ri.keyid1",
        new Object[]{ rsetid }
    );
    StringBuilder refs = new StringBuilder();
    int count = 0;
    while (database.getNext() && count < 10) {
        refs.append("\n").append(database.getString("sdcid"));
        count++;
    }
    if (refs.length() > 0) {
        throwError("RECORD_IN_USE", TYPE_VALIDATION,
            "Cannot delete — referenced by:" + refs);
    }
}
```

- Cap the reference list at ~10 rows for usability (the user doesn't need all 10 000 references listed).
- Call `throwError` (not `setError`) in delete protection — no point continuing after confirming it's in use.

---

## 13. postDelete Cascade — Key List Pattern

`postDelete` also receives `rsetid`. To delete cascade records, convert the rset key list from `actionProps`:

```java
@Override
public void postDelete(String rsetid, PropertyList actionProps) throws SapphireException {
    String keyid1 = actionProps.getProperty("keyid1", "");
    if (keyid1.isEmpty()) return;

    // Convert semicolon-delimited key list to SQL IN-clause (safe: framework-provided values)
    String inList = "'" + keyid1.replace(";", "','") + "'";
    database.executeSQL(
        "DELETE FROM s_map_table WHERE sourceid IN (" + inList + ") " +
        "OR destid IN (" + inList + ")"
    );
}
```

> The `keyid1` value comes from the framework (rset of deleted keys), not user input — the replace pattern is acceptable here. Never apply this pattern to user-supplied data.

---

## 14. CMT Import Guard

Skip or relax validation when the operation is triggered by a CMT import, unless the rule specifically needs to enforce during import.

```java
@Override
public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    if (isCMTImport()) {
        return;  // skip UI-only validation during system configuration import
    }
    // normal validation
}
```

Only skip rules that are genuinely irrelevant for imports (e.g., UI-facing business rules). Do not skip data integrity constraints.

---

## 15. Logging Conventions

Use `logTrace()` as the primary trace helper — it automatically prepends the event and SDC context. Use `logger.*` for structured info/warn/error messages.

```java
logTrace("Processing preEdit for keyid1=" + keyid1);
// output: "preEdit (SampleType): Processing preEdit for keyid1=ABC123"

logger.info("SDC rule applied status default for keyid1=" + keyid1);
logger.warn("Parent not found for " + keyid1 + " — proceeding without validation");
logger.error("Unexpected DB error in preAdd for " + keyid1, e);
logger.debug("SQL row count=" + ds.getRowCount());
```

**Level guidance:**

- `logTrace` / `logger.debug` — method entry, row counts, intermediate values (verbose, off in prod)
- `logger.info` — significant business events (status change detected, cascade executed, key assigned)
- `logger.warn` — recoverable anomalies that don't stop the operation
- `logger.error` — always include the exception object so the stack trace appears in the log; also use in `postAddKey` for non-throwable failures
- `logger.stackTrace(e)` — logs stack trace only when you already have the message logged separately

**Guard expensive debug messages** to avoid string construction overhead when debug is off:

```java
if (Logger.isDebugEnabled()) {
    logger.debug("Full dataset dump: " + primary.toString());
}
```

**`postAddKey` logging pattern** (no `throws SapphireException` available):

```java
@Override
public void postAddKey(DataSet primary, PropertyList actionProps) {
    int seq = getSequenceProcessor().getSequence(getSdcid(), "KEYSEQ", 1, 1);
    if (seq < 0) {
        logger.error("postAddKey (" + getSdcid() + "): getSequence returned -1 — key not overridden");
        return;   // cannot throw; log-and-return is the only option
    }
    // ... assign key ...
}
```

---

## 16. ConnectionInfo — Audit and DBMS Branching

```java
// Audit columns
String userId = connectionInfo.getSysUserId();
primary.setString(i, "modifiedby", userId);

// DBMS-specific SQL
String dateFn = "ORA".equals(connectionInfo.getDbms()) ? "SYSDATE" : "GETDATE()";
database.executeSQL("UPDATE s_sample SET modifieddate = " + dateFn + " WHERE keyid1 = '" + keyid1 + "'");

// Profile property lookup
ConfigService config = new ConfigService(
    new SapphireConnection(database.getConnection(), connectionInfo)
);
String propValue = config.getProfileProperty("(system)", "myproperty");
```

Use `connectionInfo` only for audit fields, locale/timezone needs, or DBMS branching — not as a general-purpose context bag.

---

## 17. Custom Key Generation — `postAddKey` and `getSequenceProcessor`

Override `postAddKey` to replace the framework-assigned primary key with a custom sequence. This hook fires during `AddSDI` after the system generates an initial key value.

**Critical constraint: `postAddKey` does NOT declare `throws SapphireException`.** Throw-based error handling is not available — use log-and-return for all failure paths.

```java
@Override
public void postAddKey(DataSet primary, PropertyList actionProps) {
    // Always read the key column from SDC metadata — never hardcode it
    String keyCol = getSdcProps().getProperty("keycolid1", "keyid1");

    int seq = this.getSequenceProcessor().getSequence(getSdcid(), "KEYSEQ", 1, 1);
    if (seq < 0) {
        // getSequence returns -1 on error; cannot throw SapphireException here
        logger.error("postAddKey (" + getSdcid() + "): getSequence failed — key not overridden");
        return;
    }

    for (int i = 0; i < primary.size(); i++) {
        String customKey = String.format("SMP-%05d", seq);
        primary.setString(i, keyCol, customKey);
        logger.info("postAddKey (" + getSdcid() + "): assigned key " + customKey);
    }
}
```

| Decision point | Guidance |
|---|---|
| Key column name | `getSdcProps().getProperty("keycolid1", "keyid1")` — never hardcode the column literal |
| `getSequence` error | Returns `-1` — always guard before formatting the key string; log-and-return on failure |
| No `throws SapphireException` | Cannot throw checked exceptions; do not wrap and re-throw — only log and return |
| `DataSet primary` identity | This is the framework key dataset, **not** the `SDIData` primary from `preAdd`/`postAdd` |
| UUID instead of integer sequence | `getSequenceProcessor().getUUID()` — returns `null` on error; null-check before assigning |
| `getSequence` parameters | `getSequence(sdcid, sequenceid, startsequencenumber, incrementby)` — use `startsequencenumber=1` to begin at 1 |

**UUID variant:**

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

## 18. Deployment Checklist

Before deploying a new or modified SDC Rule, verify:

- [ ] Class name matches the SDC ID exactly (case-sensitive)
- [ ] Package matches `customrulesjavapackage` profile property value
- [ ] Extends `BaseSDCRules` with no intermediate base class
- [ ] Only required `requires*` flags are enabled (audit for unused ones)
- [ ] All `PreparedStatement` references are closed in `finally` blocks
- [ ] No SQL string concatenation of user/record values outside of the `postDelete` rsetid pattern
- [ ] Every hook with a `DataSet` access guards against null / empty
- [ ] JAR deployed to server classpath (`WEB-INF/lib/` or equivalent)
- [ ] Application server restarted (rule class cache cleared)
- [ ] Tested: add, edit, delete, and edge cases (bulk, empty dataset, CMT import)

---

## 19. Quick Anti-Pattern Reference

| Anti-Pattern | Correct Pattern |
|---|---|
| Class name doesn't match SDC ID | Rename class to match SDC ID exactly — framework won't load it otherwise |
| `requires*` flag enabled but data never used | Remove the flag to avoid extra DB round-trips |
| `primary.getValue(0, col)` without loop | Always loop `for (int i = 0; i < primary.size(); i++)` |
| Modifying DataSet in a `post*` hook | Issue `database.executePreparedUpdate(...)` instead |
| SQL string built from user record values | Use parameterized `Object[]` form via `QueryProcessor` or `executePreparedUpdate` |
| `throwError` for every validation | Use `setError` to collect all errors; use `throwError` only for immediately unsafe states |
| No null check on `sdiData.getDataset(...)` | Always guard with `if (ds == null || ds.getRowCount() == 0) return;` |
| `PreparedStatement` not closed | Wrap in `try/finally` and close; or use `getQueryProcessor()` which manages lifecycle |
| Empty override of unused hooks | Delete empty hook overrides — they add noise |
| `isCMTImport()` not considered | Check when validation is UI-only and should not apply to imports |
| `getOldPrimaryValue()` called without `hasPrimaryValueChanged()` guard | Always guard with `if (!hasPrimaryValueChanged(...)) continue;` |
| Throwing `SapphireException` from `postAddKey` | `postAddKey` has no `throws` declaration — use log-and-return instead |
| Hardcoding key column name in `postAddKey` | Use `getSdcProps().getProperty("keycolid1", "keyid1")` to read it from SDC metadata |
| Using `getSequence()` result without checking for `-1` | Always validate: `if (seq < 0) { logger.error(...); return; }` |
| Placing custom key logic in `preAdd` / `postAdd` | Override `postAddKey` — that is the hook called with the key dataset after key generation |
