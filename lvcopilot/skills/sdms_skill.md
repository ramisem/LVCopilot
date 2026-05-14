# LV SDMS Attachment Handler Best Practices Skill

> **Always read `md_files/sdms.md` alongside this file** before writing any SDMS Attachment Handler.
> This file captures *how* to write good handlers — `sdms.md` is the authoritative API reference.

---

## 1. Class and Package Structure

- Always place handler classes in the `sapphire.attachmenthandler` package — the LV classloader expects this.
- Always extend `com.labvantage.sapphire.modules.sdms.handlers.BaseAttachmentHandler` (not the thin public wrapper).
- Only `handleData` is abstract — do not override framework lifecycle methods (`startHandle`, `endHandle`).

```java
package sapphire.attachmenthandler;          // required package

import com.labvantage.sapphire.modules.sdms.handlers.BaseAttachmentHandler;
import sapphire.SapphireException;
import sapphire.attachment.Attachment;
import sapphire.xml.PropertyList;
import java.util.List;

public class MyInstrumentHandler extends BaseAttachmentHandler {

    @Override
    public void handleData(List<Attachment> attachments, PropertyList properties)
            throws SapphireException {
        // ...
    }
}
```

**Why:** The LV classloader resolves handler classes by the package + class name registered on the Attachment Handler record. A wrong package causes a `ClassNotFoundException` at runtime.

---

## 2. Guard Clause — Validate Attachments First

Always validate the `attachments` list before any other logic.

```java
@Override
public void handleData(List<Attachment> attachments, PropertyList properties)
        throws SapphireException {

    logMessage("MyHandler started");

    if (attachments == null || attachments.isEmpty()) {
        throw new SapphireException("No attachments provided.");
    }

    if (attachments.size() > 1) {
        logMessage("Multiple attachments — only first will be processed.");
    }
    // ... rest of logic
}
```

**Why:** A null or empty list causes `NullPointerException` or `IndexOutOfBoundsException` at `attachments.get(0)`. The Data Capture silently ends in `Failure` without a useful log message unless you guard early.

---

## 3. Reading Setup Variables Safely

Always provide defaults when reading setup variables — they are strings and may be empty or absent.

```java
// CORRECT — always use a default
String outputFormat = properties.getProperty("outputformat", "CSV");
int maxRows         = Integer.parseInt(properties.getProperty("maxrows", "100"));
boolean debugFlag   = "Y".equalsIgnoreCase(properties.getProperty("debug", "N"));

// WRONG — will throw NullPointerException or NumberFormatException if absent
int maxRows = Integer.parseInt(properties.getProperty("maxrows"));
```

**Why:** `PropertyList.getProperty(key)` returns `null` when the key is not configured. `Integer.parseInt(null)` throws at runtime.

---

## 4. Always Close Streams with try-with-resources

Always wrap `attachment.getInputStream()` in a try-with-resources block.

```java
import org.apache.commons.io.IOUtils;
import java.io.*;

Attachment att = attachments.get(0);
ByteArrayOutputStream bos = new ByteArrayOutputStream();
try (InputStream is = att.getInputStream()) {
    IOUtils.copy(is, bos);
}
byte[] rawBytes = bos.toByteArray();
String content  = new String(rawBytes, "UTF-8");
```

**Why:** The framework does not close streams on your behalf. Leaked streams cause resource exhaustion under load.

---

## 5. Logging Strategy

Use the correct log level at each stage. All log output appears in the Data Capture execution log in the LIMS UI.

```java
logMessage("MyHandler started — file: " + att.getSourceFilename());  // always visible
logDebug("Raw content length: " + rawBytes.length);                  // debug mode only
logWarn("Skipping row 5 — missing required column 'batchId'");       // non-fatal anomaly
logError("Unexpected parse failure", caughtException);               // throws automatically
logMessage("MyHandler finished");
```

| Method | When to use |
|---|---|
| `logMessage` | Entry, exit, business milestones |
| `logDebug` | Intermediate values, SQL results, file content (disabled in prod) |
| `logWarn` | Skipped rows, missing optional fields, recoverable anomalies |
| `logError` | Unrecoverable failure — automatically throws `SapphireException` |

**Why:** Without entry/exit `logMessage` calls, diagnosing silent failures in production is nearly impossible — the execution log is the only visibility you have.

---

## 6. Error Handling Contract

| Scenario | Correct approach |
|---|---|
| Missing / empty attachments | `throw new SapphireException("No attachments provided.")` |
| Unreadable or malformed file | `throw new SapphireException("Failed to parse file: " + filename)` |
| Non-critical row/file skip | `logWarn(...)` and `continue` — do not re-throw |
| Unexpected system error | `logError(message, exception)` — auto-throws |
| Partial failure acceptable | Catch per-file, accumulate errors in a list, throw at end if any failed |
| LV `SapphireException` from nested call | Re-throw as-is — do not double-wrap |

```java
try {
    processData(att, properties);
} catch (SapphireException e) {
    throw e;                                             // re-throw as-is
} catch (Exception e) {
    throw new SapphireException("Failed to process file: " + e.getMessage(), e);
} finally {
    logMessage("MyHandler finished.");
}
```

Throwing `SapphireException` transitions the Data Capture to `Failure`. The message is stored in the execution log.

---

## 7. Firing LIMS Actions via ActionBlock

Use `ActionBlock` + `setActionBlock()` to queue LIMS actions. Never call `getActionProcessor()` directly.

```java
import sapphire.util.ActionBlock;
import com.labvantage.sapphire.actions.sdi.AddSDI;

ActionBlock ab = new ActionBlock();

PropertyList sampleProps = new PropertyList();
sampleProps.setProperty("sdcid",      "Sample");
sampleProps.setProperty("sampledesc", "Auto-created from instrument file");
sampleProps.setProperty("lotnum",     parsedLotNum);

// action name must be unique within this ActionBlock instance
ab.setAction("CreateSample", AddSDI.ID, "1", sampleProps);
setActionBlock(ab);
```

**Why:** Direct `getActionProcessor()` calls bypass the framework's post-processing hooks. `setActionBlock()` is the correct, supported integration point.

---

## 8. Linking to Other SDIs — Use Deferred Resolution

After creating a new SDI via `ActionBlock`, use bracket expressions to link — never hardcode a key.

```java
// Link the Data Capture to the newly created Sample
addLinkSDI("Sample", "[" + AddSDI.RETURN_NEWKEYID1 + "]", "", "");

// Link to an existing known SDI
addLinkSDI("Dataset", "DS-42", "", "");
```

`[RETURN_NEWKEYID1]` is resolved by the framework **after** the `ActionBlock` executes. Call `addLinkSDI` inside `handleData` — the framework defers persistence until `endHandle`.

---

## 9. Writing Metadata Attributes

Use `addMetaData` to tag the Data Capture with parsed values. Use `getMetaData()` to read pre-existing values.

```java
// write
addMetaData("InstrumentSerial", serialNumber);
addMetaData("BatchId",          batchId);
addMetaData("ParsedRowCount",   String.valueOf(rowCount));

// read pre-loaded metadata
PropertyList existing = getMetaData();
String existingBatch  = existing.getProperty("BatchId", "");
```

All values are strings. Normalize nulls to `""` before calling `addMetaData`.

---

## 10. Returning Result Grids

Use `addResultGrid()` (preferred over `setResultGrid`) to populate the tabular display on the Data Capture record.

```java
import com.labvantage.sapphire.modules.sdms.util.ResultDataGrid;
import sapphire.util.DataSet;

ResultDataGrid grid = addResultGrid();     // auto-wires ConnectionInfo
DataSet ds = new DataSet();
ds.addColumn("SampleId");
ds.addColumn("Result");
ds.addColumn("Units");

for (ParsedRow row : parsedRows) {
    ds.addRow(new String[]{row.sampleId, row.result, row.units});
}
grid.setDataSet(ds);

addMetaData("ParsedRowCount", String.valueOf(ds.getRowCount()));
```

Always tag the row count in metadata — it provides a quick sanity check in the LIMS UI without opening the grid.

---

## 11. Adding Output Files

Generate and attach a new file back to the Data Capture using `addFile`.

```java
import java.nio.file.*;

Path tempFile = Files.createTempFile("output_", ".csv");
Files.write(tempFile, csvContent.getBytes("UTF-8"));

// (absoluteFilePath, aliasDisplayName, attachmentClass)
addFile(tempFile.toString(), "results.csv", "ProcessedOutput");
```

`attachmentClass` must match an Attachment Classification configured in LIMS.

---

## 12. Multi-File Handling Pattern

Delegate single-file logic to a private method; iterate with per-file error isolation.

```java
@Override
public void handleData(List<Attachment> attachments, PropertyList properties)
        throws SapphireException {

    logMessage("Processing " + attachments.size() + " file(s)");
    List<String> errors = new ArrayList<>();

    for (int i = 0; i < attachments.size(); i++) {
        Attachment att = attachments.get(i);
        logMessage("File " + (i + 1) + ": " + att.getSourceFilename());
        try {
            processSingleFile(att, properties);
        } catch (Exception e) {
            // decide: accumulate or re-throw immediately
            logWarn("Failed file " + att.getSourceFilename() + ": " + e.getMessage());
            errors.add(att.getSourceFilename());
        }
    }

    if (!errors.isEmpty()) {
        throw new SapphireException("Failed to process files: " + String.join(", ", errors));
    }
}

private void processSingleFile(Attachment att, PropertyList props) throws SapphireException {
    // ...
}
```

Alternatively, use the built-in `IterateFilesAttachmentHandler` (configure `childattachmenthandlerid` as a setup variable) for clean per-file delegation without custom iteration code.

---

## 13. JSON Parsing Pattern

```java
import org.json.JSONObject;
import org.json.JSONArray;

JSONObject root = new JSONObject(content);

String batchId  = root.getString("batchId");
double value    = root.getDouble("result");
JSONArray rows  = root.getJSONArray("rows");

for (int i = 0; i < rows.length(); i++) {
    JSONObject row = rows.getJSONObject(i);
    // ...
}

// Coerce all values to String before passing to PropertyList
Iterator<?> it = root.keys();
while (it.hasNext()) {
    String k = (String) it.next();
    Object v = root.get(k);
    if (!(v instanceof String)) root.put(k, v.toString());
}
PropertyList pl = new PropertyList(root);
```

**Why:** `PropertyList` requires all values to be `String`. Non-string JSON values (`int`, `double`, `boolean`) cause a `ClassCastException` at `PropertyList` construction time unless coerced first.

---

## 14. CSV Parsing Pattern

```java
import java.io.*;

String delimiter = properties.getProperty("delimiter", ",");

BufferedReader reader = new BufferedReader(new StringReader(content));
String headerLine = reader.readLine();
if (headerLine == null) throw new SapphireException("CSV file is empty.");

String[] headers = headerLine.split(delimiter, -1);

String line;
while ((line = reader.readLine()) != null) {
    if (line.trim().isEmpty()) continue;          // skip blank lines
    String[] values = line.split(delimiter, -1);  // -1 preserves trailing empty fields
}
```

Always pass `-1` as the limit to `String.split` — without it, trailing empty columns are silently dropped.

---

## 15. isDatabaseRequired — Override When Not Needed

```java
@Override
protected boolean isDatabaseRequired() {
    return false;   // skip connection acquisition for parse-only handlers
}
```

Override to `false` for handlers that only parse files and queue an `ActionBlock` without direct DB access. The framework skips connection acquisition, improving throughput under load.

---

## 16. Context Keys — Always Available in handleData

```java
String sdcid       = getSDCId();       // "LV_DataCapture"
String keyid1      = getKeyId1();      // primary key of this Data Capture
String handlerId   = getHandlerId();   // ID of the Attachment Handler record
String executionId = getExecutionId(); // unique execution run ID (useful for temp file naming)
```

Use `executionId` to name temp files — prevents collisions when multiple handlers run concurrently.

---

## 17. Attachment Metadata — Read and Write Per-File Attributes

```java
// read per-attachment metadata
PropertyList fileMeta  = getFileMetaData(attachment);
String instrumentId    = fileMeta.getProperty("instrumentid", "");

// write per-attachment metadata
PropertyList newMeta = new PropertyList();
newMeta.setProperty("processedby", "MyHandler");
newMeta.setProperty("processedat", LocalDate.now().toString());
addFileMetaData(newMeta, attachment);
```

Distinguish from `addMetaData` / `getMetaData`, which operate on the **Data Capture SDI** itself.

---

## 18. Deployment Checklist

Before connecting a handler to a live instrument:

- [ ] Package is `sapphire.attachmenthandler` and class name matches the Handler Class field in LIMS
- [ ] JAR compiled against `sapphire.jar` (compile-only classpath — do not bundle it)
- [ ] JAR uploaded via **SDMS → Attachment Handlers → [record] → Handler Library** tab
- [ ] All Setup Variables added on the **Setup Variables** tab with correct keys and defaults
- [ ] Handler tested via the **Test Handler** button with a real sample file
- [ ] Debug Mode enabled during initial validation — disable before go-live

---

## 19. Quick Anti-Pattern Reference

| Anti-Pattern | Correct Pattern |
|---|---|
| Not closing `InputStream` | Wrap in try-with-resources |
| `attachments.get(0)` before null/empty check | Guard clause first |
| `Integer.parseInt(properties.getProperty("key"))` | Always provide a default string |
| `JSONObject` → `PropertyList` without coercion | Coerce all non-String values to String first |
| `String.split(delimiter)` without `-1` limit | Use `split(delimiter, -1)` to preserve trailing empty fields |
| Calling `getActionProcessor()` directly | Use `setActionBlock(ActionBlock)` |
| Hardcoding SDI keys in `addLinkSDI` after `AddSDI` | Use `"[" + AddSDI.RETURN_NEWKEYID1 + "]"` |
| Wrapping `SapphireException` in another `SapphireException` | Re-throw as-is |
| Silent `catch (Exception e) {}` | Log warn + continue, or log error + throw |
| Leaving `isDatabaseRequired()` true for parse-only handlers | Override to `return false` |
| Bundling `sapphire.jar` in the deployed JAR | Compile-only dependency — never bundle |
| Naming temp files without a unique suffix | Use `executionId` or `Files.createTempFile` |
