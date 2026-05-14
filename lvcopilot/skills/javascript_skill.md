# LV JavaScript Best Practices Skill

> **Always read `md_files/javascript.md` alongside this file** before writing any LV client-side JavaScript.
> This file captures *how* to write good LV JS — `javascript.md` is the authoritative API reference.

---

## 1. File and Class Organization

- One JS file per functional area or form. Name the file after the SDC and purpose: `s_Sample_maint.js`, `s_Batch_approval.js`.
- Deploy custom JS files to the `WEB-CUSTOM/scripts/` directory so they survive LV upgrades.
- Wrap all module-level code in an IIFE (Immediately Invoked Function Expression) or a namespace object to avoid polluting the global scope.
- Expose only the entry-point function that LV calls in the "Custom JS Method" field — keep everything else private.

```javascript
// s_Sample_maint.js

// All logic is private; only initSampleMaint is exported for LV to call
(function() {

    function onSampleTypeChange() { /* ... */ }
    function onLabIdChange()      { /* ... */ }
    function validateBeforeSubmit() { /* ... */ }

    // LV calls this via "Custom JS Method" on the page definition
    window.initSampleMaint = function() {
        sapphire.events.registerLoadListener(function() {
            var typeEl = document.getElementById("sampletypeid");
            var labEl  = document.getElementById("labid");
            if (typeEl) {
                onSampleTypeChange();   // set initial state on load
                sapphire.events.attachEvent(typeEl, "change", onSampleTypeChange);
            }
            if (labEl) {
                sapphire.events.attachEvent(labEl, "change", onLabIdChange);
            }
        });
    };

}());
```

**Why:** LV pages share a single browser window. Unnamespaced functions collide across pages. Wrapping in an IIFE prevents cross-page pollution.

---

## 2. Entry Points — Always Use registerLoadListener

- Never touch the DOM outside a `registerLoadListener` callback. LV elements may not exist yet when the file is parsed.
- Register once per page init function — do not nest multiple `registerLoadListener` calls for the same page.
- If you need to trigger an initial state (e.g., fire `onSampleTypeChange` on load), call the handler directly inside the listener **after** attaching the event.

```javascript
// CORRECT
window.initMyPage = function() {
    sapphire.events.registerLoadListener(function() {
        var el = document.getElementById("statusid");
        if (el) {
            onStatusChange();   // fire once for initial state
            sapphire.events.attachEvent(el, "change", onStatusChange);
        }
    });
};

// WRONG — DOM may not exist yet
window.initMyPage = function() {
    var el = document.getElementById("statusid");  // can be null
    sapphire.events.attachEvent(el, "change", onStatusChange);
};
```

---

## 3. Event Attachment — Always Use sapphire.events

- Use `sapphire.events.attachEvent` / `detachEvent` for all DOM events — never `element.addEventListener` directly. The `sapphire.events` wrapper is cross-browser safe within LV's supported browser matrix.
- Always null-check the element before attaching. Fields may be conditionally rendered.
- Use `"change"` for dropdowns and text fields, `"click"` for buttons, `"blur"` for formatted inputs.

```javascript
// Attach multiple fields safely
["sampletypeid", "labid", "locationid"].forEach(function(fieldId) {
    var el = document.getElementById(fieldId);
    if (el) {
        sapphire.events.attachEvent(el, "change", function() {
            onFieldChanged(fieldId, el.value);
        });
    }
});
```

- Detach events explicitly if you re-attach (e.g., after a panel refresh) to avoid duplicate handlers:

```javascript
sapphire.events.detachEvent(el, "change", myHandler);
sapphire.events.attachEvent(el, "change", myHandler);
```

---

## 4. AJAX Calls — Structure and Error Handling

Every `sapphire.ajax.callClass` call must follow this structure:

1. Show progress indicator before the call.
2. Supply a named callback function (not an anonymous inline) for readability.
3. Always supply an `errorCallback` for operations with side effects.
4. Hide the progress indicator in **both** the success callback and the error callback.

```javascript
function loadSampleDetails(keyid1) {
    sapphire.ui.progress.show();
    sapphire.ajax.callClass(
        "com.syngenta.ajax.GetSampleDetails",
        onSampleDetailsLoaded,
        { keyid1: keyid1 },
        true,   // POST
        false,  // async
        function(err) {
            sapphire.ui.progress.hide();
            sapphire.ui.dialog.alert("Error loading sample details: " + err);
        }
    );
}

function onSampleDetailsLoaded(resp) {
    sapphire.ui.progress.hide();
    if (!resp.found) {
        sapphire.ui.dialog.alert("Sample not found.");
        return;
    }
    document.getElementById("statusid").value   = resp.statusid;
    document.getElementById("locationid").value = resp.locationid || "";
}
```

**Always check the `found`/`success` flag first** before reading other response properties — a missing record is not an error but must be handled.

---

## 5. Dialog Usage — Confirmations and Alerts

- Use `sapphire.ui.dialog.alert` (not `window.alert`) — it renders in the LV-styled overlay, not a browser native popup.
- Use `sapphire.ui.dialog.confirm` for any destructive or irreversible action.
- Pass callback function names as strings to `confirm` and `prompt` — LV invokes them by name. Ensure those functions are in the global (window) scope or the IIFE-exported scope.
- For multi-step workflows, use `sapphire.ui.dialog.open` to embed a full LV page in a popup.
- Use `sapphire.ui.dialog.setContent(dialogNumber, html)` to dynamically update an open dialog's content without closing and reopening it.

```javascript
// CORRECT — callback accessible by LV
window.onDeleteConfirm = function(result) {
    if (result) {
        deleteCurrentRecord();
    }
};

function promptDelete() {
    sapphire.ui.dialog.confirm(
        "Confirm Delete",
        "This action cannot be undone. Delete this record?",
        "onDeleteConfirm"   // string name — must be window-scoped
    );
}

// Popup a full LV page
function openRelatedSample(keyid1) {
    sapphire.ui.dialog.open(
        "Related Sample",
        "rc?command=SampleMaint&sdcid=s_Sample&keyid1=" + encodeURIComponent(keyid1),
        true, 900, 600,
        { "Close": "sapphire.ui.dialog.close(sapphire.ui.dialog.dialogCount)" },
        null,
        true
    );
}
```

---

## 6. Field Manipulation — Read, Write, Show/Hide, ReadOnly

### Read a field value

```javascript
// Raw DOM (always works on Maintenance Forms)
var val = document.getElementById("sampletypeid").value;

// Page API (preferred on Maintenance pages — handles grid mode correctly)
var val2 = sapphire.page.maint.getFieldValue("sampletypeid");
```

### Set a field value

```javascript
// Raw DOM
document.getElementById("sampledesc").value = "Derived value";

// Page API (preferred on Maintenance pages)
sapphire.page.maint.setFieldValue("sampledesc", "Derived value");
```

### Show or hide a field row

Prefer `sapphire.style.setDisplay` over manual TR traversal — it handles element-type differences correctly:

```javascript
// Preferred
sapphire.style.setDisplay(document.getElementById("sampletypeid"), false);  // hide
sapphire.style.setDisplay(document.getElementById("sampletypeid"), true);   // show

// Manual TR walk (fallback for complex layouts)
function setFieldVisible(fieldId, visible) {
    var el = document.getElementById(fieldId);
    if (!el) { return; }
    var row = el;
    while (row && row.tagName !== "TR") {
        row = row.parentNode;
    }
    if (row) {
        row.style.display = visible ? "" : "none";
    }
}
```

### Make a field read-only

```javascript
function setReadOnly(fieldId, readOnly) {
    var el = document.getElementById(fieldId);
    if (!el) { return; }
    el.readOnly = readOnly;
    el.style.backgroundColor = readOnly ? "#f0f0f0" : "";
}
```

---

## 7. Page Context — Use `sapphire.page` Instead of Raw DOM Scraping

The `sapphire.page` API is the correct way to read page-level context. Do not scrape `submitdata` form fields manually.

```javascript
// Get keys and SDC of the current page (works on List, Maint, DataEntry)
var sdcid  = sapphire.page.getSDCId();
var keyid1 = sapphire.page.getKeyId1();
var keyid2 = sapphire.page.getKeyId2();

// Maintenance-specific context
var isLocked  = sapphire.page.maint.getLocked();
var lockedBy  = sapphire.page.maint.getLockedBy();
var isGrid    = sapphire.page.maint.isGrid();
var saveOk    = sapphire.page.maint.getSaveSuccessful();
var queryId   = sapphire.page.maint.getQueryId();

// List-specific — get all selected keys as semicolon-delimited string
var selectedKeys = sapphire.page.list.getSelectedKeyId1(";");
var selectedCount = sapphire.page.list.getSelectedKeyId1(";").split(";").filter(Boolean).length;

// Read a URL parameter from the current page request
var mode = sapphire.page.request.getParameter("mode");

// Get page type
var pageType = sapphire.page.getPageType();
```

---

## 8. Navigation — Use `sapphire.page.navigate`

Always use `sapphire.page.navigate` instead of `window.location.href` or the legacy `navigateTo`. It uses POST, handles Unicode correctly, and supports large payloads.

```javascript
// Navigate, releasing RSet lock
sapphire.page.navigate(
    "rc?command=SampleList&sdcid=s_Sample",
    "Y"    // release lock
);

// Navigate to a specific record
sapphire.page.navigate(
    "rc?command=SampleMaint&sdcid=s_Sample&keyid1=" + encodeURIComponent(keyid1)
);

// Navigate with a target frame
sapphire.page.navigate("rc?command=SampleMaint", "", "_blank");
```

---

## 9. String and Utility Helpers — Use `sapphire.util`

Prefer the built-in `sapphire.util` helpers over writing custom string/array/DOM utilities.

### String utilities

```javascript
var trimmed  = sapphire.util.string.trim("  hello  ");           // "hello"
var tokens   = sapphire.util.string.getTokens("a;b;c", ";");    // ["a","b","c"]
var replaced = sapphire.util.string.replaceAll("a-b-c", "-", "_"); // "a_b_c"
var starts   = sapphire.util.string.startsWith("SAMP-001", "SAMP"); // true

// Token evaluation — replaces [fieldname], [keyid1], [sdcid] with live page values
var url = sapphire.util.evaluateExpression(
    "rc?command=SampleMaint&sdcid=[sdcid]&keyid1=[keyid1]"
);
```

### Array utilities

```javascript
var idx = sapphire.util.array.findInArray(["FOO","BAR"], "bar", true);  // 1 (case-insensitive)
var str = sapphire.util.array.toString(["a","b","c"], ";");              // "a;b;c"
```

### Number utilities

```javascript
var dec   = sapphire.util.number.parseFraction("1 3/4");       // 1.75
var valid = sapphire.util.number.number("3.14", false);        // true
```

### DOM utilities

```javascript
// Cross-browser getAll (prefer over element.all)
var children = sapphire.util.dom.getAll(containerEl);

// Set a form field value by name
sapphire.util.dom.setFormField(document.forms[0], "keyid1", newKey);

// Find a frame by name
var targetFrame = sapphire.util.dom.findFrame("contentFrame", window.top);
```

### URL utilities

```javascript
var encoded = sapphire.util.url.encode("sample type A");    // "sample%20type%20A"
var decoded = sapphire.util.url.decode("sample%20type%20A"); // "sample type A"
```

---

## 10. System Actions from JavaScript

Invoke LV System Actions via `sapphire.ajax.callService("ProcessAction", ...)`. Always include `actionid`, `actionversion`, `sdcid`, and `keyid1`.

```javascript
function approveSample(keyid1) {
    sapphire.ui.progress.show();
    sapphire.ajax.callService(
        "ProcessAction",
        function(resp) {
            sapphire.ui.progress.hide();
            if (resp.returncode === "SUCCESS") {
                sapphire.ui.dialog.alert("Sample approved.");
                document.forms[0].submit();   // refresh the form
            } else {
                sapphire.ui.dialog.alert("Approval failed: " + (resp.errormessage || resp.returncode));
            }
        },
        {
            actionid:      "ApproveSample",
            actionversion: "1",
            sdcid:         "s_Sample",
            keyid1:        keyid1
        }
    );
}
```

Always check `resp.returncode` for `"SUCCESS"` — actions can return soft failures without an HTTP error.

---

## 11. Departmental Security (DS) Check Before Action

When an operation is DS-gated, use `sapphire.connection.dsCall` before invoking the action. Provide a meaningful failure message that lists which SDIs failed.

```javascript
function requestApproval(keyid1) {
    sapphire.connection.dsCall(
        "s_Sample",
        keyid1,
        "APPROVE",
        "",
        "onDSFailed(this.failedsdis)",
        doApprove,
        [keyid1]
    );
}

window.onDSFailed = function(failedSDIs) {
    sapphire.ui.dialog.alert("You do not have permission to approve: " + failedSDIs);
};

function doApprove(keyid1) {
    approveSample(keyid1);
}
```

---

## 12. Session and User Context

Read user and session data from `sapphire.connection`. Never hard-code user IDs or role names — always compare dynamically.

```javascript
// Role-based visibility
if (sapphire.connection.sysUserRoleList.indexOf("APPROVER") >= 0) {
    setFieldVisible("approvalnotes", true);
}

// Dev-mode guard for debug tooling
if (sapphire.connection.isDevMode) {
    sapphire.debug.show();
}

// Keep connection alive during long operations
sapphire.connection.pingConnection();
```

---

## 13. ELN Worksheet Operations

`sapphire.worksheet` is only available inside ELN Worksheet Control Custom Operation scripts. Never reference it from Maintenance Form JS.

- Prefer `refresh()` over `refreshSection()` over `refreshWorksheet()` — refresh the smallest scope needed.
- Use `getConfigProperty` / `setConfigProperty` for Control-level state that must persist across user interactions.

```javascript
// Custom Operation script (Worksheet Control context only)
sapphire.worksheet.getConfigProperty("selectedMode", function(mode) {
    if (mode === "BATCH") {
        processBatch();
    } else {
        processSingle();
    }
});

function processBatch() {
    // ... logic ...
    sapphire.worksheet.refresh();   // refresh only this control
}
```

---

## 14. Portal / Stellar Page JavaScript

Stellar Maint page events use a different API surface — all calls use `this.` as context. This API is **only** available inside Stellar page column/section/page event scripts.

```javascript
// Field access (use instead of document.getElementById on Stellar pages)
var sampleType = this.getField('sampletypeid', '');
this.setField('sampledesc', 'Computed description');
this.setFocus('labid');
this.disableField('expirydate');
this.enableField('expirydate');

// User messaging (toast notifications — not modal dialogs)
this.alert('Validation failed');
this.success('Record saved successfully');
this.error('Server error occurred');
this.warn('Value is outside expected range');
this.info('Lookup returned ' + count + ' results');

// Confirmation (returns Promise — use .then/.catch)
this.confirm('Confirm Submit', 'Submit this record for approval?')
    .then(() => {
        this.executeOperation('submitForApproval')
            .then((props) => { this.success('Submitted.'); })
            .catch(() => { this.error('Submission failed.'); });
    })
    .catch(() => { /* user cancelled — no action needed */ });

// Execute an operation (calls an Action or navigates)
this.executeOperation('myOperation')
    .then((props) => { console.log('Action result:', props); })
    .catch(() => { this.error('Operation failed.'); });

// Data source query
this.getDataSource('lotData', { lotid: this.getField('lotid') })
    .then((ds) => {
        this.setField('expirydate', ds.getValue(0, 'expirydate', ''));
    })
    .catch(() => { this.error('Could not load lot data.'); });
```

**Key differences from classic Maintenance Form JS:**
- No `sapphire.events.registerLoadListener` — Stellar events fire directly.
- No `sapphire.ui.dialog` — use `this.alert/confirm/error/success/warn` for messaging.
- No raw `document.getElementById` — use `this.getField` / `this.setField`.
- Operations call Actions server-side; no direct `sapphire.ajax.callService` needed.

---

## 15. Naming Conventions

| Artifact | Convention | Example |
|---|---|---|
| JS file | `<SDC>_<purpose>.js` | `s_Sample_maint.js` |
| Page init function | `init<FormName>` (window-scoped) | `initSampleApproval` |
| Event handler | `on<FieldId>Change`, `on<Action>Click` | `onSampleTypeChange`, `onApproveClick` |
| AJAX callback | `on<HandlerName><Outcome>` | `onSampleDetailsLoaded`, `onStatusUpdated` |
| Dialog callback | `on<DialogPurpose>Confirm/Result` (window-scoped) | `onDeleteConfirm`, `onBatchIdEntered` |
| Helper/utility | camelCase verb-noun | `setFieldVisible`, `getSelectedKeyid1` |

---

## 16. Security Rules

| Concern | Rule |
|---|---|
| XSS via DOM | Never set `innerHTML` from server response values. Use `el.value = resp.field` or `el.textContent = resp.field` |
| URL injection | Always `encodeURIComponent(value)` when building `rc?command=...` URLs for `dialog.open` or `sapphire.page.navigate` |
| Eval | Never use `eval()` — use named functions and callbacks |
| Sensitive data | Do not log passwords, tokens, or PII via `console.log` in production JS |
| Script injection in confirm callbacks | Pass only function *names* (strings) as `sReturnFunction` — never dynamically composed code strings |
| AJAX response HTML | Never pass `resp.html` directly to `dialog.create` or `setContent` without sanitizing — treat server-provided HTML as untrusted |

---

## 17. Debugging

- Guard all debug statements with `sapphire.connection.isDevMode` to avoid logging in production.
- Use `sapphire.debug.show()` to open the LV debug popup.
- Use `console.error` for unexpected failures so they appear in the browser error console.
- On Stellar pages, add `debugger;` in the event script and open browser DevTools to step through.

```javascript
if (sapphire.connection.isDevMode) {
    console.log("initSampleMaint: loaded, userId=" + sapphire.connection.sysUserId);
    console.log("page SDC:", sapphire.page.getSDCId(), "key:", sapphire.page.getKeyId1());
}
```

---

## 18. Anti-Pattern Reference

| Anti-Pattern | Correct Pattern |
|---|---|
| DOM access outside `registerLoadListener` | Wrap all DOM access inside a `registerLoadListener` callback |
| `window.alert(...)` or `window.confirm(...)` | Use `sapphire.ui.dialog.alert` / `sapphire.ui.dialog.confirm` |
| `element.addEventListener(...)` directly | Use `sapphire.events.attachEvent` for cross-browser compatibility |
| Anonymous function as `sReturnFunction` in `confirm` | Pass a string function name; function must be window-scoped |
| No `errorCallback` in `callClass` for a mutating call | Always supply `errorCallback` — silent failures are invisible to users |
| No `found`/`success` check before reading response fields | Check the boolean flag first; never assume the happy path |
| `sapphire.ui.progress.show()` without matching `hide()` on all paths | Hide in both success callback and error callback |
| `innerHTML = resp.field` with a server value | Use `textContent` or `el.value` to prevent XSS |
| `sapphire.worksheet.*` called from Maintenance Form JS | `sapphire.worksheet` is ELN-context only; never use from Maintenance Form |
| Polluting the global scope with helper functions | Wrap in IIFE; expose only LV entry points via `window.initXxx` |
| Hard-coded role names or user IDs in conditional logic | Always read from `sapphire.connection.sysUserRoleList` at runtime |
| Dialog URL built without `encodeURIComponent` | Always encode dynamic values inserted into `rc?command=...` URLs |
| `window.location.href = ...` or legacy `navigateTo(...)` | Use `sapphire.page.navigate(url, releaseLockFlag)` — POST-safe, Unicode-safe |
| Scraping `submitdata` form fields to get page keys | Use `sapphire.page.getKeyId1()` / `sapphire.page.getSDCId()` |
| Raw DOM read on Maintenance pages (`el.value`) when page API exists | Prefer `sapphire.page.maint.getFieldValue(col)` — handles grid mode correctly |
| Writing custom string trim / token split helpers | Use `sapphire.util.string.trim()` / `sapphire.util.string.getTokens()` |
| Building token URLs manually | Use `sapphire.util.evaluateExpression("[sdcid]/[keyid1]")` |
| Using `element.all` for cross-browser child access | Use `sapphire.util.dom.getAll(el)` |
| Stellar page using `sapphire.ui.dialog.alert` | Use `this.alert(msg)` — Stellar uses toast notifications |
| Stellar page using `document.getElementById` | Use `this.getField('col', 'default')` / `this.setField('col', val)` |

---

## 19. Deployment Checklist

Before deploying a new or modified LV JS file, verify:

- [ ] File placed in `WEB-CUSTOM/scripts/` (not `WEB-CORE`)
- [ ] All module code wrapped in an IIFE; only entry-point function(s) exposed on `window`
- [ ] Page init function registered as "Custom JS Method" on the correct page definition
- [ ] All DOM access inside `sapphire.events.registerLoadListener`
- [ ] Every `getElementById` call has a null guard before use
- [ ] All events attached via `sapphire.events.attachEvent`
- [ ] Progress indicator shown before AJAX calls and hidden in both success and error paths
- [ ] Dialog callbacks that LV invokes by name are window-scoped
- [ ] No `innerHTML` assignments from server-provided values
- [ ] URL parameters for `dialog.open` / `sapphire.page.navigate` are `encodeURIComponent`-escaped
- [ ] Page context read via `sapphire.page.*` (not form field scraping)
- [ ] Navigation uses `sapphire.page.navigate` (not `window.location.href` or `navigateTo`)
- [ ] `sapphire.util.string.*` / `sapphire.util.dom.*` used where applicable instead of custom helpers
- [ ] Tested: happy path, field not present (conditional render), AJAX server error, DS check failure
- [ ] `isDevMode` guard on all `console.log` / `sapphire.debug.show()` calls
- [ ] Stellar page scripts use `this.*` API — not `sapphire.ui.dialog` or `document.getElementById`
