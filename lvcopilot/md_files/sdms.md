# LabVantage SDMS Attachment Handler — Developer Reference

This document is the authoritative reference for writing custom Java POJO attachment handler business logic in the LabVantage SDMS (Scientific Data Management System) framework. All examples are based on the decompiled LabVantage Sapphire JAR and official product documentation.

---

## 1. What Is an SDMS Attachment Handler?

An Attachment Handler is a custom Java class that processes data files captured by the SDMS from instruments or external sources. It is invoked automatically by LabVantage when a Data Capture reaches "Pending Processing" status after being collected by an SDMS Collector.

A handler can:
- Read and parse the attached file(s) (CSV, JSON, XML, PDF, binary, etc.)
- Extract data values and write them into LIMS records via `ActionBlock`
- Set metadata attributes on the Data Capture SDI
- Generate new output files (e.g., convert PDF → CSV) and re-attach them
- Link the Data Capture to Samples, Datasets, or any other SDI
- Return structured result grids

---

## 2. SDMS Architecture Context

```
Instrument / Device
       │ produces file
       ▼
  SDMS Collector  (Internal or External)
       │ picks up file, creates Data Capture
       ▼
  Data Capture (LV_DataCapture SDI)
       │  status: Capturing → Captured → Pending Processing
       ▼
  Attachment Handler (your Java class)
       │  processes attachments
       ▼
  LIMS Records (Samples, Datasets, Results …)
```

Data Capture statuses (from `SDMSConstants`):
| Constant | String Value |
|---|---|
| `DATACAPTURE_STATUS_CAPTURING` | `"Capturing"` |
| `DATACAPTURE_STATUS_CAPTURED` | `"Captured"` |
| `DATACAPTURE_STATUS_PENDINGPROCESSING` | `"Pending Processing"` |
| `DATACAPTURE_STATUS_PROCESSING` | `"Processing"` |
| `DATACAPTURE_STATUS_PROCESSED` | `"Processed"` |
| `DATACAPTURE_STATUS_FAILURE` | `"Failure"` |

---

## 3. Class Hierarchy

```
com.labvantage.sapphire.BaseCustom                  (LV framework base)
    └── com.labvantage.sapphire.modules.sdms.handlers.BaseAttachmentHandler   (SDMS base — extend this)
            └── YourCustomHandler                    (your implementation)
```

The public wrapper `sapphire.attachmenthandler.BaseAttachmentHandler` is a thin delegate to the above and is what appears in official Javadoc — but the actual implementation to extend is `com.labvantage.sapphire.modules.sdms.handlers.BaseAttachmentHandler`.

The `sapphire.attachmenthandler.AttachmentHandler` interface defines the full contract.

---

## 4. Minimal Handler Template

```java
package sapphire.attachmenthandler;          // keep this package for classloader compatibility

import com.labvantage.sapphire.modules.sdms.handlers.BaseAttachmentHandler;
import sapphire.SapphireException;
import sapphire.attachment.Attachment;
import sapphire.xml.PropertyList;

import java.util.List;

public class MyCustomHandler extends BaseAttachmentHandler {

    @Override
    public void handleData(List<Attachment> attachments, PropertyList properties)
            throws SapphireException {

        logMessage("MyCustomHandler started");

        if (attachments == null || attachments.isEmpty()) {
            throw new SapphireException("No attachments provided.");
        }

        // --- your business logic here ---

        logMessage("MyCustomHandler finished");
    }
}
```

**Rules:**
- Package should be `sapphire.attachmenthandler` (matches LV classloader expectations; keep consistent with SampleCreationHandler).
- Class must extend `com.labvantage.sapphire.modules.sdms.handlers.BaseAttachmentHandler`.
- Only `handleData` is abstract — everything else is already implemented in the base class.
- Throw `sapphire.SapphireException` to signal failure; the framework marks the Data Capture as `Failure`.

---

## 5. `handleData` — The Entry Point

```java
protected abstract void handleData(List<Attachment> attachments, PropertyList properties)
        throws SapphireException;
```

| Parameter | Type | Description |
|---|---|---|
| `attachments` | `List<Attachment>` | All files attached to this Data Capture |
| `properties` | `PropertyList` | Setup variables defined on the Attachment Handler record in LIMS |

The method is called once per handler invocation. Use the `attachments` list to iterate over files and `properties` to read configuration values set in the UI.

---

## 6. Reading Setup Variables (Configuration)

Setup variables are key-value pairs configured on the Attachment Handler record in LabVantage. They arrive as the `properties` parameter.

```java
// read with a default fallback
String outputFormat = properties.getProperty("outputformat", "CSV");
int maxRows        = Integer.parseInt(properties.getProperty("maxrows", "100"));
boolean debugFlag  = "Y".equalsIgnoreCase(properties.getProperty("debug", "N"));
```

`PropertyList` behaves like a `HashMap<String, String>`. All values are strings.

---

## 7. Reading Attachment Data

```java
import org.apache.commons.io.IOUtils;
import java.io.*;

Attachment attachment = attachments.get(0);

// --- read raw bytes ---
ByteArrayOutputStream bos = new ByteArrayOutputStream();
try (InputStream is = attachment.getInputStream()) {
    IOUtils.copy(is, bos);
}
byte[] rawBytes = bos.toByteArray();
String content  = new String(rawBytes, "UTF-8");

// --- useful attachment metadata ---
String filename    = attachment.getSourceFilename();   // original file name
String attachClass = attachment.getAttachmentClass();  // attachment classification
int    attachNum   = attachment.getAttachmentNum();    // attachment number within SDI
String sdcid       = attachment.getSDCId();            // parent SDI type
String keyid1      = attachment.getKeyId1();           // parent SDI key
```

**Always close streams.** Use try-with-resources or explicit `finally` blocks.

---

## 8. Context Keys — Who Am I Operating On?

The framework injects the identity of the owning Data Capture SDI before calling `handleData`. Retrieve it via:

```java
String sdcid  = getSDCId();    // e.g. "LV_DataCapture"
String keyid1 = getKeyId1();   // primary key of the Data Capture
String keyid2 = getKeyId2();   // usually empty for Data Captures
String keyid3 = getKeyId3();   // usually empty for Data Captures
String handlerId   = getHandlerId();          // ID of this Attachment Handler record
String executionId = getExecutionId();        // unique execution run ID
```

---

## 9. Logging

```java
logMessage("Informational message");         // always logged
logWarn("Something unusual happened");       // prefixed WARN:
logDebug("Only shown in debug mode");        // prefixed DEBUG: — only when Debug Mode enabled on handler
logError("Fatal error description");         // throws SapphireException automatically
logError("Fatal error", someException);      // throws SapphireException wrapping the cause
```

All log output appears in the Data Capture execution log visible in the LIMS UI.

---

## 10. Firing LIMS Actions via `ActionBlock`

Use `ActionBlock` to invoke any standard LabVantage action (e.g., `AddSDI`, `UpdateSDI`) as part of handler post-processing. The framework executes the block **after** `handleData` returns.

```java
import sapphire.util.ActionBlock;
import com.labvantage.sapphire.actions.sdi.AddSDI;

ActionBlock ab = new ActionBlock();

PropertyList sampleProps = new PropertyList();
sampleProps.setProperty("sdcid",       "Sample");
sampleProps.setProperty("sampledesc",  "Auto-created from instrument file");
sampleProps.setProperty("lotnum",      "LOT-001");

// register the action — name must be unique within this ActionBlock
ab.setAction("CreateSample", AddSDI.ID, "1", sampleProps);

setActionBlock(ab);  // hand it to the framework for deferred execution
```

**`[bracket]` expressions** in property values are resolved by the framework after action execution:

```java
// Link the Data Capture to the newly created Sample using the return value from AddSDI
addLinkSDI("Sample", "[" + AddSDI.RETURN_NEWKEYID1 + "]", "", "");
```

The `[RETURN_NEWKEYID1]` placeholder is substituted with the actual key after `CreateSample` runs.

---

## 11. Linking to Other SDIs

```java
// addLinkSDI(sdcid, keyid1, keyid2, keyid3)
addLinkSDI("Sample",  "SAMPLE-001",        "",  "");
addLinkSDI("Dataset", "DS-42",             "",  "");
addLinkSDI("Sample",  "[RETURN_NEWKEYID1]", "", "");  // deferred — resolved after ActionBlock
```

Duplicates are automatically de-duplicated by the base class. Links show up as "Data Capture Links" on the created/modified SDI in the LIMS UI.

---

## 12. Writing Metadata Attributes

Metadata is stored as attributes on the Data Capture SDI.

```java
// set a single attribute
addMetaData("InstrumentSerial", "SN-20240101");
addMetaData("BatchId",          "B-202401");

// retrieve existing metadata (pre-loaded from LIMS before handleData is called)
PropertyList existing = getMetaData();
String existingBatch = existing.getProperty("BatchId", "");
```

---

## 13. Adding Output Files

A handler can generate new files and attach them back to the Data Capture.

```java
import java.nio.file.*;

// write a temp file
Path tempFile = Files.createTempFile("output_", ".csv");
Files.write(tempFile, "col1,col2\nval1,val2\n".getBytes());

// attach it — (filePath, aliasFileName, attachmentClass)
addFile(tempFile.toString(), "results.csv", "ProcessedOutput");
```

`attachmentClass` corresponds to an Attachment Classification defined in the LIMS configuration.

---

## 14. Returning Result Grids

Result grids populate the tabular display on the Data Capture record.

```java
import com.labvantage.sapphire.modules.sdms.util.ResultDataGrid;
import sapphire.util.DataSet;

// Option A — use addResultGrid() which auto-wires ConnectionInfo
ResultDataGrid grid = addResultGrid();
DataSet ds = new DataSet();
ds.addColumn("SampleId");
ds.addColumn("Result");
ds.addRow(new String[]{"S-001", "98.5"});
ds.addRow(new String[]{"S-002", "97.1"});
grid.setDataSet(ds);

// Option B — use setResultGrid(grid) if you construct it yourself
setResultGrid(grid);
```

---

## 15. Attachment File Metadata

Read or write metadata (attributes) stored directly on an individual `Attachment` object:

```java
// read metadata on an attachment
PropertyList fileMeta = getFileMetaData(attachment);
String instrumentId = fileMeta.getProperty("instrumentid", "");

// write metadata onto an attachment
PropertyList newMeta = new PropertyList();
newMeta.setProperty("processedby", "MyHandler");
addFileMetaData(newMeta, attachment);
```

---

## 16. Database Access

The `database` field (type `sapphire.util.DBAccess`) is available when `isDatabaseRequired()` returns `true` (the default).

```java
// Override only if you do NOT need database access — saves a connection
@Override
protected boolean isDatabaseRequired() {
    return false;
}

// Direct DB query example (when isDatabaseRequired() == true)
// database is a DBAccess / DBUtil instance — use framework query patterns
```

Direct database queries should be rare. Prefer using `ActionBlock` with standard LV actions, which respect business rules and security.

---

## 17. Multi-File Handling Pattern

When the Data Capture contains multiple files, iterate:

```java
@Override
public void handleData(List<Attachment> attachments, PropertyList properties)
        throws SapphireException {

    logMessage("Processing " + attachments.size() + " file(s)");

    for (int i = 0; i < attachments.size(); i++) {
        Attachment att = attachments.get(i);
        logMessage("Processing file " + (i + 1) + ": " + att.getSourceFilename());
        try {
            processSingleFile(att, properties);
        } catch (Exception e) {
            logWarn("Failed to process " + att.getSourceFilename() + ": " + e.getMessage());
            // decide: re-throw to fail entire handler, or continue with remaining files
        }
    }
}

private void processSingleFile(Attachment att, PropertyList props) throws SapphireException {
    // ...
}
```

Alternatively, use the built-in `IterateFilesAttachmentHandler` which calls a child handler once per file:
- Set up a parent handler of type `IterateFilesAttachmentHandler`
- Add a setup variable `childattachmenthandlerid` pointing to your single-file handler ID

---

## 18. JSON Parsing Pattern

```java
import org.json.JSONObject;
import org.json.JSONArray;

String json = new String(rawBytes, "UTF-8");
JSONObject root = new JSONObject(json);

String batchId  = root.getString("batchId");
double value    = root.getDouble("result");
JSONArray rows  = root.getJSONArray("rows");

for (int i = 0; i < rows.length(); i++) {
    JSONObject row = rows.getJSONObject(i);
    // process each row ...
}

// PropertyList can be built directly from a JSONObject
PropertyList pl = new PropertyList(root);
```

---

## 19. CSV Parsing Pattern

```java
import java.io.BufferedReader;
import java.io.StringReader;

BufferedReader reader = new BufferedReader(new StringReader(content));
String headerLine = reader.readLine();
String[] headers  = headerLine.split(",");

String line;
while ((line = reader.readLine()) != null) {
    String[] values = line.split(",", -1);
    // map headers[i] → values[i]
}
```

---

## 20. Error Handling Contract

| Scenario | Recommended approach |
|---|---|
| Missing or empty attachments | `throw new SapphireException("No attachments provided.")` |
| Unreadable / malformed file | `throw new SapphireException("Failed to parse file: " + filename)` |
| Non-critical file skip | `logWarn(...)` and `continue` (don't re-throw) |
| Unexpected system error | `logError(message, exception)` — this throws automatically |
| Partial failure acceptable | Catch per-file, log warn, accumulate errors, throw at end if any failed |

Throwing `SapphireException` causes the Data Capture to transition to `Failure` status. The message is stored in the Data Capture execution log.

---

## 21. Complete Working Example — JSON to Sample Creator

```java
package sapphire.attachmenthandler;

import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.modules.sdms.handlers.BaseAttachmentHandler;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.attachment.Attachment;
import sapphire.util.ActionBlock;
import sapphire.xml.PropertyList;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

public class SampleCreationHandler extends BaseAttachmentHandler {

    @Override
    public void handleData(List<Attachment> attachments, PropertyList properties)
            throws SapphireException {

        logDebug("Inside SampleCreationHandler");
        int    copies     = Integer.parseInt(properties.getProperty("copies", "-1"));
        String sampleDesc = properties.getProperty("sampledesc", "");

        if (attachments == null || attachments.isEmpty()) {
            throw new SapphireException("No attachments provided.");
        }

        logMessage("Sample creation handler started...");

        try {
            if (attachments.size() > 1) {
                logMessage("More than one attachment — only first will be processed.");
            }

            // --- read file content ---
            String json = "";
            try {
                Attachment attachment = attachments.get(0);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try (InputStream is = attachment.getInputStream()) {
                    IOUtils.copy(is, bos);
                }
                if (bos.size() > 0) {
                    json = new String(bos.toByteArray());
                } else {
                    logMessage("No data in attachment.");
                }
            } catch (Exception e) {
                throw new SapphireException("Failed to read file/attachment");
            }

            // --- parse JSON into PropertyList ---
            PropertyList sampleProps = null;
            if (!json.isEmpty()) {
                try {
                    JSONObject job = new JSONObject(json);
                    // coerce all values to String (required by PropertyList)
                    Iterator<?> it = job.keys();
                    while (it.hasNext()) {
                        String k = (String) it.next();
                        Object v = job.get(k);
                        if (!(v instanceof String)) job.put(k, v.toString());
                    }
                    sampleProps = new PropertyList(job);
                    if (copies > -1)         sampleProps.setProperty("copies",     String.valueOf(copies));
                    if (!sampleDesc.isEmpty()) sampleProps.setProperty("sampledesc", sampleDesc);
                } catch (Exception e) {
                    throw new SapphireException("Failed to parse JSON.");
                }
            }

            // --- fire AddSDI action ---
            if (sampleProps != null) {
                logMessage("About to generate AddSDI");
                try {
                    ActionBlock actionBlock = new ActionBlock();
                    actionBlock.setAction("SampleCreationHandler", AddSDI.ID, "1", sampleProps);
                    setActionBlock(actionBlock);
                    // link the Data Capture to the newly created Sample
                    addLinkSDI("Sample", "[" + AddSDI.RETURN_NEWKEYID1 + "]", "", "");
                } catch (Exception e) {
                    throw new SapphireException(e);
                } finally {
                    logMessage("AddSDI generated.");
                }
            }

        } finally {
            logMessage("Sample creation handler finished.");
        }
    }
}
```

---

## 22. Complete Example — CSV Result Parser (Returns Result Grid)

```java
package sapphire.attachmenthandler;

import com.labvantage.sapphire.modules.sdms.handlers.BaseAttachmentHandler;
import com.labvantage.sapphire.modules.sdms.util.ResultDataGrid;
import org.apache.commons.io.IOUtils;
import sapphire.SapphireException;
import sapphire.attachment.Attachment;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

import java.io.*;
import java.util.List;

public class CsvResultHandler extends BaseAttachmentHandler {

    @Override
    public void handleData(List<Attachment> attachments, PropertyList properties)
            throws SapphireException {

        logMessage("CsvResultHandler started");

        if (attachments == null || attachments.isEmpty()) {
            throw new SapphireException("No attachments provided.");
        }

        String delimiter = properties.getProperty("delimiter", ",");

        try {
            Attachment att = attachments.get(0);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (InputStream is = att.getInputStream()) {
                IOUtils.copy(is, bos);
            }
            String content = new String(bos.toByteArray(), "UTF-8");

            BufferedReader reader = new BufferedReader(new StringReader(content));
            String headerLine = reader.readLine();
            if (headerLine == null) throw new SapphireException("CSV file is empty.");

            String[] headers = headerLine.split(delimiter, -1);

            ResultDataGrid grid = addResultGrid();
            DataSet ds = new DataSet();
            for (String h : headers) ds.addColumn(h.trim());

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                ds.addRow(line.split(delimiter, -1));
            }
            grid.setDataSet(ds);

            // tag the Data Capture with the row count
            addMetaData("ParsedRowCount", String.valueOf(ds.getRowCount()));
            logMessage("Parsed " + ds.getRowCount() + " rows.");

        } catch (SapphireException e) {
            throw e;
        } catch (Exception e) {
            throw new SapphireException("Failed to process CSV: " + e.getMessage(), e);
        } finally {
            logMessage("CsvResultHandler finished");
        }
    }
}
```

---

## 23. Key Imports Reference

```java
// Handler base — always required
import com.labvantage.sapphire.modules.sdms.handlers.BaseAttachmentHandler;
import sapphire.SapphireException;
import sapphire.attachment.Attachment;
import sapphire.xml.PropertyList;
import java.util.List;

// Actions
import sapphire.util.ActionBlock;
import com.labvantage.sapphire.actions.sdi.AddSDI;   // AddSDI.ID, AddSDI.RETURN_NEWKEYID1
// com.labvantage.sapphire.actions.sdi.UpdateSDI, DeleteSDI, etc.

// Result grid
import com.labvantage.sapphire.modules.sdms.util.ResultDataGrid;
import sapphire.util.DataSet;

// I/O
import java.io.*;
import org.apache.commons.io.IOUtils;   // available in LV classpath

// JSON (available in LV classpath)
import org.json.JSONObject;
import org.json.JSONArray;

// File output
import java.nio.file.*;
```

---

## 24. `BaseAttachmentHandler` Protected API — Quick Reference

| Method | Description |
|---|---|
| `logMessage(String)` | Log an info message — always visible |
| `logWarn(String)` | Log a warning (prefixed `WARN:`) |
| `logDebug(String)` | Log only when Debug Mode is on (prefixed `DEBUG:`) |
| `logError(String)` | Log error and throw `SapphireException` |
| `logError(String, Throwable)` | Log error + cause and throw `SapphireException` |
| `getSDCId()` | SDCID of the Data Capture (e.g. `"LV_DataCapture"`) |
| `getKeyId1()` | Primary key of the Data Capture |
| `getKeyId2()` | Secondary key (usually empty) |
| `getKeyId3()` | Tertiary key (usually empty) |
| `getHandlerId()` | LIMS ID of this Attachment Handler record |
| `getAtachmentHandlerId()` | Alias for `getHandlerId()` |
| `getExecutionId()` | Unique ID for this handler execution run |
| `addFile(path, alias, class)` | Attach a generated file to the Data Capture |
| `addMetaData(name, value)` | Write an attribute on the Data Capture SDI |
| `getMetaData()` | Read all existing metadata as `PropertyList` |
| `addFileMetaData(PropertyList, Attachment)` | Write attributes onto a specific attachment |
| `getFileMetaData(Attachment)` | Read attributes from a specific attachment |
| `setActionBlock(ActionBlock)` | Queue a block of LIMS actions for post-execution |
| `addLinkSDI(sdcid, k1, k2, k3)` | Link Data Capture to another SDI |
| `getLinkSDI()` | Retrieve current SDI link list |
| `addResultGrid()` | Create and register a new `ResultDataGrid` |
| `setResultGrid(ResultDataGrid)` | Register an externally constructed grid |
| `getResultGrid()` | Get the first result grid |
| `getResultGrid(int)` | Get result grid by index |
| `getResultResultGridCount()` | Count of registered result grids |
| `isDatabaseRequired()` | Override to return `false` if no DB needed |
| `database` (field) | `DBAccess` instance — available when `isDatabaseRequired() == true` |
| `connectionInfo` (field) | `ConnectionInfo` for current session |

---

## 25. JAR Packaging & Deployment

1. Compile your handler class(es) against the LabVantage `sapphire.jar` (add to compile classpath only — do not bundle it).
2. Package into a JAR: `jar cvf my-handler.jar sapphire/attachmenthandler/MyCustomHandler.class`
3. In LIMS: navigate to **SDMS → Attachment Handlers → [your handler record] → Handler Library tab**.
4. Upload the JAR via the Handler Library section.
5. Set **Handler Type** = `Handler Class` and **Handler Class** = `sapphire.attachmenthandler.MyCustomHandler`.
6. Add Setup Variables as needed on the **Setup Variables** tab.
7. Use the **Test Handler** button (upload an example file) to validate before connecting to a live instrument.

---

## 26. Handler Execution Lifecycle (Framework Internals)

The `AttachmentHandlerProcessor` performs this sequence — you do not call these directly:

```
1. Load JAR from Handler Library via LabVantageClassLoader
2. Instantiate your handler class via reflection
3. handler.startHandle(handlerId, sdcid, keyid1..., connection)
       └─ loads existing metadata from LIMS into metadataCombined
4. handler.handleData(attachments, properties)     ← YOUR CODE RUNS HERE
5. handler.endHandle()
6. Framework post-processing:
       └─ Execute ActionBlock (if setActionBlock() was called)
       └─ Resolve [bracket] expressions in SDI links
       └─ Store generated files (from addFile())
       └─ Update metadata attributes (from addMetaData())
       └─ Create SDI links (from addLinkSDI())
       └─ Persist result grids (from addResultGrid())
7. Update Data Capture status → Processed (or Failure on exception)
```

---

## 27. Setup Variable Inheritance

Setup variables can be defined at three levels; more specific overrides less specific:

```
Attachment Handler record (default values)
    └── Instrument Model (overrides handler defaults)
            └── Instrument (overrides model values)
```

All merged values arrive in `properties` inside `handleData`.

---

## 28. Asynchronous Processing

To run a handler asynchronously (non-blocking, via Automation Service):
- Enable **Asynchronous** flag on the Attachment Operation linked to the handler.
- Use **Asynchronous Groups** to chain multiple operations that must run in sequence.

The handler code itself is identical — async vs sync is purely a framework configuration choice.

---

## 29. Common Pitfalls

| Pitfall | Fix |
|---|---|
| Not closing `InputStream` | Use try-with-resources on `attachment.getInputStream()` |
| Forgetting to call `setActionBlock()` | Framework won't execute actions unless you call this |
| Using `getActionProcessor()` directly | Prefer `setActionBlock()` — direct calls bypass post-processing hooks |
| Integer.parseInt with empty setup var | Always provide a default: `properties.getProperty("key", "0")` |
| Mutating `attachments` list | Don't remove/add to the input list — it's managed by the framework |
| Duplicate action names in ActionBlock | Each action name must be unique within one `ActionBlock` instance |
| Setting properties before `setAction()` | Call `setAction()` first, then `setActionProperty()` / `setActionProperties()` |
