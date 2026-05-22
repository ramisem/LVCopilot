# LabVantage Architecture Guide: Design & Scoping Manual

This manual provides the architectural logic and component definitions required for technical scoping (Phase 2). It focuses on **when** to use specific components and provides a reference for the framework's core utility classes.

---

## 1. Component Selection Strategy (The "When to Use" Matrix)

| Requirement | Recommended Component | Rationale |
| :--- | :--- | :--- |
| **Explicit User Action** | **Java Action** | Triggered by a button. Best for complex workflows, reports, or multi-step processes where the user "starts" the task. |
| **Data Integrity / Auto-Calc** | **SDC Rule** | Triggered by DB saves. Ensures logic runs regardless of the source (UI, API, or Import). |
| **Real-time UI Feedback** | **AJAX Handler** | Triggered by JS events (onchange). Fetches server data for the UI without performing a full record save. |
| **Instrument Integration** | **SDMS Handler** | Triggered by file arrival. Specialized for parsing raw files (CSV/XML) into structured LIMS data. |

---

## 2. SDC Rule Hook Lifecycle & Decision Guide

### A. Primary Record Hooks
- `preAdd` / `preEdit`: **Validation & Defaulting.** Best place to throw `SapphireException` to block invalid data.
- `postAdd` / `postEdit`: **Side Effects.** Trigger logic that requires the record ID to be generated or the primary save to be confirmed.
- `preAddKey` / `postAddKey`: **Custom Identity.** Use `postAddKey` to override the system-generated sequence with a custom format.

### B. Child & Result Hooks
- `preAddDetail` / `postAddDetail`: Specific to detail (child) grid records.
- `postDataEntry`: Triggered after a full result recording session.
- `preApprove` / `postApprove`: Logic triggered specifically by the Approval workflow.

---

## 3. Core Framework Processors (Utility Classes)

These classes are the workhorses of the LabVantage framework. Use them inside Actions, AJAX, and SDC Rules to interact with the system.

- **`QueryProcessor`**: The primary tool for data retrieval. Efficiently executes queries and returns `DataSet` objects.
- **`ActionProcessor`**: Used to execute other Actions programmatically from within your Java code.
- **`SDCProcessor`**: Used to programmatically trigger SDC logic, load SDI metadata, or manipulate SDI data structures.
- **`DataProcessor`**: Provides high-level methods for manipulating records, managing state, and handling complex data relationships.
- **`ApprovalProcessor`**: Manages the approval lifecycle, checking permissions and transitioning record statuses.
- **`ActionBlock`**: A container used to group multiple Actions together to be executed as a single atomic unit.

---

## 4. Database Access & Raw Statement Execution

### A. SELECT Queries — Use `QueryProcessor` (Primary Pattern)

Obtain via `this.getQueryProcessor()`. Returns a `DataSet`.

```java
QueryProcessor qp = this.getQueryProcessor();

// Parameterized SELECT — always use Object[] for bind variables
DataSet ds = qp.getPreparedSqlDataSet(
    "SELECT col1, col2 FROM s_sample WHERE keyid1 = ? AND statusid = ?",
    new Object[]{keyid1, statusid}
);

// COUNT query
int count = qp.getPreparedCount(
    "SELECT COUNT(*) FROM s_sample WHERE sdcid = ?",
    new Object[]{sdcid}
);
```

### B. INSERT / UPDATE / DELETE — Use `this.database` (DBAccess) with `SafeSQL`

```java
SafeSQL safeSQL = new SafeSQL();
StringBuffer sql = new StringBuffer();
sql.append("UPDATE s_sample SET statusid = ").append(safeSQL.addVar(newStatus));
sql.append(" WHERE keyid1 = ").append(safeSQL.addVar(keyid1));

int rows = this.database.executePreparedUpdate(sql.toString(), safeSQL.getValues());
if (rows < 1) {
    throw new SapphireException("UPDATE_FAILED", "No record updated.");
}
```

### C. Key Rules
- **NEVER** use string concatenation for SQL — always use `SafeSQL` with `addVar()` or `Object[]` params.
- Use `QueryProcessor` for SELECT, `this.database` for writes. Avoid `this.dbUtil` (low-level).

---

## 5. Architectural Standards (Design Patterns)

- **The "Fail-Fast" Pattern**: Use `pre-` hooks to validate data. If validation fails, throw a `SapphireException`.
- **The "Silent Calculation" Pattern**: Use SDC rules to populate read-only summary fields so the user doesn't have to.
- **The "Safe Access" Pattern**: Always check `if (ds.getRowCount() > 0)` before calling `ds.getValue(0, ...)`.
- **The "Atomic Action" Pattern**: Use `ActionBlock` when you need multiple actions to succeed or fail together as a single transaction.
