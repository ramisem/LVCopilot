# LabVantage JavaScript Customization — Developer Reference

This document is the authoritative reference for writing custom JavaScript business logic in LabVantage. All API details are sourced from the LabVantage Sapphire help system (`labvantagedoc`). Use this document exclusively when implementing JS-based customizations — do not re-explore the raw docs.

---

## Table of Contents

1. [Where JavaScript Runs in LabVantage](#1-where-javascript-runs-in-labvantage)
2. [JavaScript Customization Entry Points](#2-javascript-customization-entry-points)
3. [Global Namespace: `sapphire`](#3-global-namespace-sapphire)
4. [`sapphire` — Core Root Object](#4-sapphire--core-root-object)
5. [`sapphire.events` — Event Registration](#5-sapphireevents--event-registration)
6. [`sapphire.connection` — Session and User Context](#6-sapphireconnection--session-and-user-context)
7. [`sapphire.ui` — Dialogs, Panels, and Animation](#7-sapphireui--dialogs-panels-and-animation)
8. [`sapphire.ajax` — Calling Server-Side Logic](#8-sapphireajax--calling-server-side-logic)
9. [`sapphire.page` — Page Data API](#9-sapphirepage--page-data-api)
10. [`sapphire.util` — Utility Methods and Classes](#10-sapphireutil--utility-methods-and-classes)
11. [`sapphire.debug` — Debugging Utilities](#11-sapphiredebug--debugging-utilities)
12. [`sapphire.worksheet` — ELN Worksheet API](#12-sapphireworksheet--eln-worksheet-api)
13. [`sapphire.sdc` — Client-Side SDC Processor](#13-sapphiresdc--client-side-sdc-processor)
14. [`sapphire.cookie` — Cookie Management](#14-sapphirecookie--cookie-management)
15. [`sapphire.browser` — Browser Detection](#15-sapphirebrowser--browser-detection)
16. [`sapphire.style` — CSS Manipulation](#16-sapphirestyle--css-manipulation)
17. [`sapphire.navigator` — Navigator Utilities](#17-sapphirenavigator--navigator-utilities)
18. [`sapphire.userConfig` — User Configuration](#18-sapphireuserconfig--user-configuration)
19. [`sapphire.garbage` — Memory Management](#19-sapphiregarbage--memory-management)
20. [`sapphire.lookup` — Lookup Dialogs](#20-sapphirelookup--lookup-dialogs)
21. [Portal Maintenance Page JS API (Stellar)](#21-portal-maintenance-page-js-api-stellar)
22. [Common Patterns and Recipes](#22-common-patterns-and-recipes)
23. [Calling System Actions from JavaScript](#23-calling-system-actions-from-javascript)
24. [Field-Level Events on Maintenance Forms](#24-field-level-events-on-maintenance-forms)
25. [Integration with Java (AJAX ↔ JS)](#25-integration-with-java-ajax--js)
26. [Quick Reference Cheat Sheet](#26-quick-reference-cheat-sheet)

---

## 1. Where JavaScript Runs in LabVantage

LabVantage JavaScript executes in the browser (client-side) and is wired into the LV page lifecycle. Custom JS can be attached at these levels:

| Context | How to Attach JS |
|---|---|
| **Maintenance Form page** | Custom JS method field on the page/element definition |
| **SDC Rule (JavaScript type)** | Rule body written in JS, evaluated server-side via Rhino engine |
| **Button / Operation** | Script field on Button or Operation definition |
| **Page load hook** | `sapphire.events.registerLoadListener(fn)` in a custom JS file |
| **ELN Worksheet Control** | Operations of type "Custom" on a Worksheet Control |
| **Form Builder element** | JavaScript callbacks on Form Builder elements |
| **Stellar Portal Page** | Column/Section/Page events on Stellar Maint page types |

> **Critical distinction:** JavaScript in *SDC Rules* and *Stored Calc* executes server-side via the Rhino engine and has a different scope. The `sapphire.*` browser APIs described here are only valid in browser-context JS (Maintenance Forms, Buttons, ELN Worksheets). See `sdc_rule.md` for the server-side JS API.

---

## 2. JavaScript Customization Entry Points

### 2.1 Custom JS Method on a Form Page

In a Maintenance Form page definition, you can specify a JS method that is called after the page loads.

```javascript
function myPageInit() {
    sapphire.events.registerLoadListener(function() {
        var fieldEl = document.getElementById("sampletypeid");
        if (fieldEl) {
            sapphire.events.attachEvent(fieldEl, "change", onSampleTypeChange);
        }
    });
}
```

### 2.2 Button / Operation Script

On a Maintenance Form button, the Script field contains JavaScript executed in the browser when the button is clicked.

```javascript
sapphire.ui.dialog.confirm(
    "Confirm",
    "Are you sure you want to approve this sample?",
    function(result) {
        if (result) {
            sapphire.ajax.callService("ProcessAction", null, {
                actionid: "ApproveSample",
                sdcid:    "s_Sample",
                keyid1:   document.getElementById("keyid1").value
            });
        }
    }
);
```

### 2.3 Page Load via `registerLoadListener`

```javascript
sapphire.events.registerLoadListener(function() {
    initMyCustomBehavior();
});
```

---

## 3. Global Namespace: `sapphire`

All LabVantage client-side APIs live under the `sapphire` global object. The primary sub-namespaces are:

| Namespace | File | Purpose |
|---|---|---|
| `sapphire` | `sapphirecore.js` | Core root object: call, alert, translate, encryptField |
| `sapphire.events` | `sapphirecore.js` | Event registration and cross-browser event utilities |
| `sapphire.connection` | `sapphirecore.js` | Session properties, user info, connection management |
| `sapphire.ui` | `sapphireui.js` | Dialogs, panels, animations, drag-drop, links |
| `sapphire.ajax` | `sapphirecore.js` | AJAX calls to Java handlers and LV services |
| `sapphire.page` | `sapphirecore.js` / `sapphirelist.js` | Page data: keys, navigation, field values, list/maint context |
| `sapphire.util` | `sapphirecore.js` | Utility: string, number, array, DOM, URL, dataset helpers |
| `sapphire.debug` | `sapphirecore.js` | JS debugging utilities |
| `sapphire.worksheet` | *(dynamic/GWT)* | ELN Worksheet Manager API |
| `sapphire.sdc` | `sapphirecore.js` | Client-side SDC property processor |
| `sapphire.cookie` | `sapphirecore.js` | Cookie read/write |
| `sapphire.browser` | `sapphirecore.js` | Browser/OS detection flags |
| `sapphire.style` | `sapphirecore.js` | CSS class manipulation |
| `sapphire.navigator` | `sapphirecore.js` | Navigator layout utilities |
| `sapphire.userConfig` | `sapphirecore.js` | User configuration cookies |
| `sapphire.garbage` | `sapphirecore.js` | IE memory management |
| `sapphire.lookup` | `sapphirecore.js` | Lookup dialog openers |

---

## 4. `sapphire` — Core Root Object

**File:** `WEB-CORE\scripts\sapphirecore.js`

### Properties

| Property | Type | Description |
|---|---|---|
| `jsdebug` | Boolean | `true` if the system config property `jsdebug` is enabled |

### Methods

#### `sapphire.call(oFunction, oaArguments, oObject)`

Calls any JavaScript function. Prompts the user if the function cannot be found.

| Parameter | Type | Description |
|---|---|---|
| `oFunction` | Function | Function object to call |
| `oaArguments` | Object Array | Arguments to pass to the function |
| `oObject` | Object | Scope object (defaults to Window) |

Returns: Result of the called function.

#### `sapphire.statusMsg(sMsg, iShowTime)`

Displays a message in the browser status bar for a timed period.

| Parameter | Type | Description |
|---|---|---|
| `sMsg` | String | Message to display |
| `iShowTime` | Integer | Duration in ms (defaults to 3000) |

#### `sapphire.alert(sMsg)`

Displays a message using LV dialogs if available, or the native browser alert box if not.

| Parameter | Type | Description |
|---|---|---|
| `sMsg` | String | Message to display |

#### `sapphire.translate(textid)`

Translates text using the LabVantage translation API over AJAX.

#### `sapphire.translatePartial(textid)`

Translates tokenized text, e.g. `"hello {{world}}"` using the translation API.

#### `sapphire.encryptField(fieldid)`

Performs RSA JavaScript encryption (1024-bit public key) of a form field.

| Parameter | Type | Description |
|---|---|---|
| `fieldid` | String | ID of the field to encrypt |

---

## 5. `sapphire.events` — Event Registration

**File:** `WEB-CORE\scripts\sapphirecore.js`

### Properties

| Property | Type | Description |
|---|---|---|
| `oaLoadListeners` | Function Array | List of functions called when the LabVantage page loads |

### Methods

#### `registerLoadListener(vFunction, lInsertBefore, iTimeout)`

Registers a function to call when the LabVantage page loads. **This is the primary hook for page initialization.**

| Parameter | Type | Description |
|---|---|---|
| `vFunction` | Function | Function to call on page load |
| `lInsertBefore` | Boolean | If `true`, insert at start of queue; else append |
| `iTimeout` | Integer | Optional. Millisecond timeout on the call |

```javascript
sapphire.events.registerLoadListener(function() {
    console.log("Page ready");
}, false, 0);
```

#### `registerListener(vFunction, oaArray, lInsertBefore, iTimeout)`

Registers a function to call for any specified event array (not just page load).

| Parameter | Type | Description |
|---|---|---|
| `vFunction` | Function | Function to call |
| `oaArray` | Object Array | List of listeners to append to |
| `lInsertBefore` | Boolean | Insert at start if `true` |
| `iTimeout` | Integer | Optional timeout in ms |

#### `callListeners(oaArray)`

Calls all events registered in the given array.

#### `callLoadListeners()`

Calls all page-load listeners. Called internally by LV framework on page ready.

#### `attachEvent(oElement, sTriggerName, vFunction)`

Browser-independent event attachment.

| Parameter | Type | Description |
|---|---|---|
| `oElement` | Object | DOM element to attach the event to |
| `sTriggerName` | String | Event name: IE (`onclick`) or Mozilla (`click`) syntax |
| `vFunction` | String/Function | Script code string or function reference |

```javascript
sapphire.events.attachEvent(document.getElementById("myField"), "change", function() {
    alert("Field changed");
});
```

#### `detachEvent(oElement, sTriggerName, vFunction)`

Detaches an event previously attached via `attachEvent`.

#### `fireEvent(oElement, sTriggerName)`

Fires an event on an element programmatically.

#### `cancelEvent(eEvent, lStopBubble)`

Cancels an event and optionally stops bubbling. Use in `onKeyDown`/`onKeyPress` handlers.

| Parameter | Type | Description |
|---|---|---|
| `eEvent` | Object | The event object |
| `lStopBubble` | Boolean | `true` to stop bubbling |

### Sub-object: `sapphire.events.util`

#### `getButton(e)`

Gets the mouse button integer from an event, cross-browser. Returns: `Integer`.

---

## 6. `sapphire.connection` — Session and User Context

**File:** `WEB-CORE\scripts\sapphirecore.js`

### Properties

| Property | Type | Description |
|---|---|---|
| `databaseId` | String | Database currently in use by the client |
| `sysUserId` | String | Current logged-in user ID |
| `sysUserDesc` | String | Description of the current user |
| `sysUserRoleList` | String | Roles of the current user |
| `sysUserModuleList` | String | Modules assigned to the current user |
| `rsetid` | String | Current RSetId (blank if not in use) |
| `pingRsetFrqcy` | Integer | Timeout until next RSet ping (seconds) |
| `pingConnectionFrqcy` | Integer | Timeout until next connection ping (seconds) |
| `timeToTimeout` | Integer | Remaining seconds before connection timeout |
| `connectionDate` | String | Date the connection was created |
| `decimalSeparator` | String | Locale decimal separator |
| `groupingSeparator` | String | Locale number grouping separator |
| `groupingInterval` | Number | Number grouping interval |
| `isDevMode` | Boolean | `true` if the application is in Development Mode |
| `isLoggingOff` | Boolean | `true` if a log-off has been initiated |

```javascript
var userId = sapphire.connection.sysUserId;
var roles  = sapphire.connection.sysUserRoleList;
var db     = sapphire.connection.databaseId;
```

### Methods

#### `getConnectionId()`
Returns: `String` — current connection ID.

#### `checkConnection()`
Checks whether the connection is still valid.

#### `pingConnection()`
Pings the server to keep the connection alive.

#### `pingRset()`
Pings the RSet to keep it alive.

#### `pingRsetError()`
Called when an RSet ping fails.

#### `getRSetId()`
Gets the current RSetId from the Maintenance or Data Entry page and sets `connection.rsetid`. Returns: `String`.

#### `setCookieRSetList()`
Sets the cookie list of RSet IDs.

#### `releaseLock(sReleaseLockFlag)`

| Parameter | Type | Description |
|---|---|---|
| `sReleaseLockFlag` | String | Set to `"Y"` to release the current lock |

#### `logOff(lForce)`

| Parameter | Type | Description |
|---|---|---|
| `lForce` | Boolean | `true` to force immediate log-off |

#### `dsCall(sSDCId, sKeyId1, sOperation, sAccessType, sFailureScript, oFunction, oaArguments)`

Executes a function only if Departmental Security allows it.

| Parameter | Type | Description |
|---|---|---|
| `sSDCId` | String | SDC on which the operation is performed |
| `sKeyId1` | String | List of SDIs on which the operation is performed |
| `sOperation` | String | Operation identifier |
| `sAccessType` | String | Access type (e.g., `"World"` bypasses DS check) |
| `sFailureScript` | String | JS to run on failure; has access to `this.failedsdis` / `this.passedsdis` |
| `oFunction` | Function | Function to execute if security check passes |
| `oaArguments` | Object Array | Arguments to pass to `oFunction` |

---

## 7. `sapphire.ui` — Dialogs, Panels, and Animation

**File:** `WEB-CORE\scripts\sapphireui.js`

### 7.1 `sapphire.ui.dialog` — Dialogs and Popups

**Properties:**

| Property | Type | Description |
|---|---|---|
| `dialogCount` | Integer | Number of dialogs created on the page (last dialog = this number) |
| `focusedNumber` | Integer | Dialog number of the currently focused dialog |
| `started` | Boolean | `true` if any dialog has been created on the current page |

#### `sapphire.ui.dialog.alert(sContent)`

Shows a LabVantage-styled alert dialog.

| Parameter | Type | Description |
|---|---|---|
| `sContent` | String | Text for the dialog |

Returns: `Object` (the dialog object)

```javascript
sapphire.ui.dialog.alert("Sample has been approved successfully.");
```

#### `sapphire.ui.dialog.show(stitle, sContent, lModal, sURL)`

Shows a general LabVantage dialog (text or URL content).

| Parameter | Type | Description |
|---|---|---|
| `stitle` | String | Dialog title (defaults to "LabVantage" if omitted) |
| `sContent` | String | Text content for the dialog |
| `lModal` | Boolean | `true` = modal (blocks page), `false` = non-modal |
| `sURL` | String | Optional. URL to load in dialog |

Returns: `Object`

#### `sapphire.ui.dialog.open(stitle, sURL, lModal, iWidth, iHeight, oBtns, oForm, lResizable)`

Opens a URL in a dialog with optional buttons and form submission.

| Parameter | Type | Description |
|---|---|---|
| `stitle` | String | Dialog title |
| `sURL` | String | URL to load |
| `lModal` | Boolean | `true` for modal |
| `iWidth` | Integer | Width in pixels |
| `iHeight` | Integer | Height in pixels |
| `oBtns` | Object | Buttons map, e.g., `{"OK": "alert(1)", "Cancel": "sapphire.ui.dialog.close(1)"}` |
| `oForm` | Object | Form to POST to the URL |
| `lResizable` | Boolean | `true` (default) to allow resize |

Returns: `Object`

```javascript
sapphire.ui.dialog.open("Add Sample", "rc?command=addSample", true, 800, 600,
    {"Close": "sapphire.ui.dialog.close(sapphire.ui.dialog.dialogCount)"}
);
```

#### `sapphire.ui.dialog.create(iWidth, iHeight, stitle, sHTML, oBtns, lResizable, lShowInnerContainer, lModal, iInnerPadding, sCloseFunction)`

Creates a fully custom dialog with HTML content.

| Parameter | Type | Description |
|---|---|---|
| `iWidth` | Integer | Width in pixels |
| `iHeight` | Integer | Height in pixels |
| `stitle` | String | Dialog title |
| `sHTML` | String | HTML content body |
| `oBtns` | Object | Buttons map |
| `lResizable` | Boolean | `true` to allow resize |
| `lShowInnerContainer` | Boolean | `true` if buttons are included |
| `lModal` | Boolean | `true` for modal |
| `iInnerPadding` | Integer | Padding in pixels (default 10) |
| `sCloseFunction` | String | JS to run when closed via X button or Esc |

Returns: `Object`

#### `sapphire.ui.dialog.confirm(stitle, sPrompt, sReturnFunction)`

Prompts the user for yes/no.

| Parameter | Type | Description |
|---|---|---|
| `stitle` | String | Dialog title |
| `sPrompt` | String | Question text |
| `sReturnFunction` | String | Callback function name |

Returns: `Object`

#### `sapphire.ui.dialog.prompt(stitle, sPrompt, sDefault, sReturnFunction)`

Prompts user for a single text input.

| Parameter | Type | Description |
|---|---|---|
| `stitle` | String | Dialog title |
| `sPrompt` | String | Prompt label |
| `sDefault` | String | Pre-filled default value |
| `sReturnFunction` | String | Callback with the entered value |

#### `sapphire.ui.dialog.multiPrompt(stitle, saPromptArray, saDefaultArray, sReturnFunction)`

Prompts the user for multiple inputs.

| Parameter | Type | Description |
|---|---|---|
| `stitle` | String | Dialog title |
| `saPromptArray` | String Array | Array of prompt labels |
| `saDefaultArray` | String Array | Array of default values |
| `sReturnFunction` | String | Callback with entered values |

#### `sapphire.ui.dialog.close(iDialogNumber)`

Closes the dialog identified by its handle number.

| Parameter | Type | Description |
|---|---|---|
| `iDialogNumber` | Integer | Dialog handle (use `sapphire.ui.dialog.dialogCount` for the last opened) |

#### `sapphire.ui.dialog.maximize(iDialogNumber)` / `restore(iDialogNumber)`

Maximizes or restores a dialog.

#### `sapphire.ui.dialog.attachOnShowEvent(iDialogNumber, vFunction)`

Attaches a callback to fire when the dialog has finished showing.

#### `sapphire.ui.dialog.attachOnHideEvent(iDialogNumber, vFunction)`

Attaches a callback to fire when the dialog closes.

#### `sapphire.ui.dialog.postConfirm(oDialog, lValue)`

Processes the result of a confirm dialog.

| Parameter | Type | Description |
|---|---|---|
| `oDialog` | Object | Dialog object returned by `confirm()` |
| `lValue` | String | Result (`"true"` or `"false"`) |

#### `sapphire.ui.dialog.keyDown(iDialogNumber, eEvent)`

Handles keydown events on a dialog (used internally).

#### `sapphire.ui.dialog.showEnd(iDialogNumber)`

Called internally after a dialog finishes showing animation.

#### `sapphire.ui.dialog.closeEnd(iDialogNumber)`

Called internally after a dialog finishes closing animation.

#### `sapphire.ui.dialog.clearFocus()`

Clears focus from the currently focused dialog.

#### `sapphire.ui.dialog.focus(iDialogNumber)`

Sets focus to the specified dialog.

#### `sapphire.ui.dialog.getDialogNumber(oDialog)`

Returns the dialog number for a dialog object. Returns: `Integer`.

#### `sapphire.ui.dialog.getDialogButton(iDialogNumber, sButtonLabel)`

Returns the button element of a dialog by label. Returns: `Object`.

#### `sapphire.ui.dialog.getDialogObject(iDialogNumber)`

Returns the dialog object for a given dialog number. Returns: `Object`.

#### `sapphire.ui.dialog.setContent(iDialogNumber, sHTML)`

Sets the HTML content of an existing dialog.

| Parameter | Type | Description |
|---|---|---|
| `iDialogNumber` | Integer | Dialog handle number |
| `sHTML` | String | New HTML content |

---

### 7.2 `sapphire.ui.progress` — Progress Indicator

#### `sapphire.ui.progress.show()`
Shows the LabVantage progress/loading indicator.

#### `sapphire.ui.progress.hide()`
Hides the progress indicator.

#### `sapphire.ui.progress.hideEnd()`
Called internally after the hide animation completes.

#### `sapphire.ui.progress.update()`
Updates the progress indicator state.

```javascript
sapphire.ui.progress.show();
sapphire.ajax.callClass("com.client.ajax.LongTask", function(resp) {
    sapphire.ui.progress.hide();
}, { param: "value" });
```

---

### 7.3 `sapphire.ui.panel` — Content Panels

Manages content panels within a LabVantage page. Panels are used for split/detail views.

#### `sapphire.ui.panel.register(...)` 
Registers a panel for management.

#### `sapphire.ui.panel.load(...)`
Loads content into a panel.

#### `sapphire.ui.panel.focus(...)`
Sets focus to a panel.

#### `sapphire.ui.panel.close(...)`
Closes a panel.

---

### 7.4 `sapphire.ui.modalCover` — Modal Overlay

#### `sapphire.ui.modalCover.create(sId, oParent, iOpacity)`

Creates a div that covers the page (used internally by dialogs, useful when running AJAX).

| Parameter | Type | Description |
|---|---|---|
| `sId` | String | ID of the cover element |
| `oParent` | Object | Owner/parent element |
| `iOpacity` | Integer | Opacity 0–100 |

Returns: `Object`

#### `sapphire.ui.modalCover.show(oModal)` / `hide(oModal)` / `resize(oModal)` / `remove(sId, oParent)`

Show, hide, resize, or remove a modal cover.

---

### 7.5 `sapphire.ui.animation` — Animations

| Method | Parameters | Description |
|---|---|---|
| `fadeIn(oElement, iDuration)` | element, ms | Fades an element in |
| `fadeOut(oElement, iDuration)` | element, ms | Fades an element out |
| `changeObjectFade(oElement, iDuration, iStart, iEnd)` | element, ms, start%, end% | Animates opacity from start to end |
| `showObjectVertical(oElement)` | element | Slides element open vertically |
| `hideObjectVertical(oElement)` | element | Slides element closed vertically |
| `showObjectHorizontal(oElement)` | element | Slides element open horizontally |
| `hideObjectHorizontal(oElement)` | element | Slides element closed horizontally |
| `changeObjectVertical(oElement, iHeight, iDuration)` | element, px, ms | Animates element to a specific height |
| `changeObjectHorizontal(oElement, iWidth, iDuration)` | element, px, ms | Animates element to a specific width |

---

### 7.6 `sapphire.ui.order` — Z-Index Management

Manages the z-index order of layered UI elements (dialogs, panels).

| Method | Description |
|---|---|
| `register(oEl)` | Registers an element for z-index management |
| `unregister(oEl)` | Unregisters an element |
| `restore(oEl)` | Restores the element's original z-index |
| `bringToFront(oEl)` | Brings element to the front |
| `setZIndexChangedEvent(oEl, vFunction)` | Attaches a callback for z-index changes |

---

### 7.7 `sapphire.ui.resize` — Drag-Resize

Manages drag-to-resize behavior for UI elements.

| Method | Description |
|---|---|
| `start(oEl, ...)` | Starts a resize operation |
| `doMMFrame(eEvent)` | Mouse-move frame handler |
| `doMUFrame(eEvent)` | Mouse-up frame handler |
| `doResizeMM(eEvent)` | Mouse-move resize handler |
| `doResize(eEvent)` | Performs the resize |
| `doResizeEnd(eEvent)` | Ends the resize operation |
| `maximize(oEl)` | Maximizes the element |
| `restore(oEl)` | Restores the element's original size |
| `setResizeEvent(oEl, vFunction)` | Attaches a callback when resize completes |

---

### 7.8 `sapphire.ui.dragdrop` — Drag and Drop

| Method | Description |
|---|---|
| `setMoveX(lValue)` | Allows/disallows horizontal movement |
| `setMoveY(lValue)` | Allows/disallows vertical movement |
| `setFollowElements(oaElements)` | Elements that follow the dragged element |
| `setDropOnTargetEvent(vFunction)` | Callback when dropped on a valid target |
| `setDropOffTargetEvent(vFunction)` | Callback when dropped off a valid target |
| `defaultBounds()` | Resets bounds to default (full page) |
| `registerDragObject(oEl, oHandle, ...)` | Registers an element as draggable |
| `registerDropTarget(oEl, ...)` | Registers an element as a drop target |
| `doDragMM(eEvent)` | Mouse-move drag handler |
| `doDrag(eEvent)` | Performs the drag |
| `doDragEnter(eEvent)` | Handles drag entering a drop target |
| `doDragLeave(eEvent)` | Handles drag leaving a drop target |
| `doDragEnd(eEvent)` | Ends the drag operation |

---

### 7.9 `sapphire.ui.link` — Visual Link Lines

Draws visual connector lines between elements on a page (used in workflow/tree displays).

| Method | Description |
|---|---|
| `attachEvent(oEl, sTrigger, vFn)` | Attaches an event to a link element |
| `detachEvent(oEl, sTrigger, vFn)` | Detaches an event |
| `register(sId, oConfig)` | Registers a link configuration |
| `updateAll()` | Updates all registered links |
| `removeAll()` | Removes all registered links |
| `hideAll()` | Hides all registered links |
| `change(sId, oConfig)` | Updates a single link configuration |
| `add(sId, oConfig)` | Adds a new link |
| `draw(sId)` | Draws a single link |
| `drawLine(sId, oLine)` | Draws a line segment for a link |
| `hide(sId)` | Hides a single link |
| `remove(sId)` | Removes a single link |

---

### 7.10 `sapphire.ui.util` — UI Utilities

| Method | Returns | Description |
|---|---|---|
| `findElementPos(oEl)` | Object `{x, y}` | Gets the absolute page position of an element |
| `setCaretPos(oEl, iPos)` | None | Sets the caret position in a text element |
| `getCaretPos(oEl)` | Integer | Gets the caret position from a text element |
| `findCaretCoord(oEl)` | Object `{x, y}` | Gets the pixel coordinates of the caret |
| `addCover(oEl)` | Object | Adds a cover div over an element |
| `remoteCover(oEl)` | None | Removes the cover div |
| `prepareFragment(oEl)` | None | Prepares a document fragment for manipulation |
| `restoreFragment(oEl)` | None | Restores a previously prepared fragment |
| `scrollTo(oEl, iY)` | None | Scrolls the page/element to a Y position |
| `createPopup(sId, sHTML, ...)` | Object | Creates a lightweight popup element |

---

### 7.11 `sapphire.ui.selectBlocker`

Blocks `<select>` elements in IE6 from showing through overlays.

| Method | Description |
|---|---|
| `show(sId, oParent, iX, iY, iWidth, iHeight)` | Creates and shows the iframe blocker; returns Object |
| `hide(sId)` | Hides the select blocker; returns Object |

---

## 8. `sapphire.ajax` — Calling Server-Side Logic

**File:** `WEB-CORE\scripts\sapphirecore.js` (built-in)

### Properties

| Property | Type | Description |
|---|---|---|
| `defaultToPOST` | Boolean | Default HTTP method for AJAX calls; `true` = POST |

### 8.1 `sapphire.ajax.callClass(className, callback, callProperties, post, synchronous, errorCallback)`

Invokes a custom Java class extending `BaseAjaxRequest`.

| Parameter | Type | Default | Description |
|---|---|---|---|
| `className` | String | required | Fully-qualified Java class name |
| `callback` | Function | required | Called on success with parsed JSON response |
| `callProperties` | Object | `{}` | Key-value parameters sent to the server |
| `post` | Boolean | `true` | Use POST (always recommended) |
| `synchronous` | Boolean | `false` | Block until complete (avoid in UI context) |
| `errorCallback` | Function | none | Called on HTTP or server error |

```javascript
sapphire.ajax.callClass(
    "com.syngenta.ajax.GetSampleDetails",
    function(response) {
        if (response.found) {
            document.getElementById("statusid").value = response.statusid;
        }
    },
    { keyid1: document.getElementById("keyid1").value }
);
```

### 8.2 `sapphire.ajax.callService(service, callback, callProperties, ...)`

Calls a built-in LabVantage AJAX service.

```javascript
sapphire.ajax.callService("ProcessAction", function(resp) {
    console.log("Action result:", resp);
}, {
    actionid:      "ApproveSample",
    actionversion: "1",
    sdcid:         "s_Sample",
    keyid1:        "SAMP-001"
});
```

### 8.3 `sapphire.ajax.callCommand(command, callback, callProperties, ...)`

Calls a server-side LabVantage command.

```javascript
sapphire.ajax.callCommand("getSDIList", function(resp) {
    // process resp.data
}, {
    sdcid:  "s_Sample",
    filter: "statusid = 'ACTIVE'"
});
```

### 8.4 `sapphire.ajax.callServiceError(errorData)`

Handles service-level errors; called internally when `callService` returns an error.

### 8.5 `sapphire.ajax.callServiceHandler(response)`

Internal handler that processes service responses before invoking the caller's callback.

### 8.6 `sapphire.ajax.handleResponse(response, callback, errorCallback)`

Parses and dispatches an AJAX response; called internally after `io.send` completes.

### 8.7 `sapphire.ajax.handleError(status, text, errorCallback)`

Called when an HTTP-level error occurs during an AJAX call.

### 8.8 `sapphire.ajax.io` — Low-Level HTTP

#### `sapphire.ajax.io.getXMLHttpRequest()`

Returns a cross-browser `XMLHttpRequest` object. Returns: `Object`.

#### `sapphire.ajax.io.send(oConfig)`

Sends an HTTP request with fine-grained control.

| Key | Type | Description |
|---|---|---|
| `url` | String | Target URL |
| `data` | Object | Key-value pairs to send as form data |
| `success` | Function | Called with response text on success |
| `error` | Function | Called with `(status, text)` on failure |
| `timeout` | Integer | Timeout in ms |
| `async` | Boolean | `true` for async (default) |

```javascript
sapphire.ajax.io.send({
    url:     "/servlet?ajaxclass=com.syngenta.ajax.MyHandler",
    data:    { param1: "value1" },
    success: function(responseText) {
        var parsed = JSON.parse(responseText);
    },
    error:   function(status, text) {
        console.error("HTTP " + status + ": " + text);
    },
    timeout: 60000
});
```

### 8.9 `sapphire.ajax.uti` — AJAX Utilities

| Method | Returns | Description |
|---|---|---|
| `getRandomString()` | String | Generates a random string (used for cache-busting) |
| `createForm(oData)` | Object | Creates a hidden form from a data object |
| `getTimeStamp()` | String | Returns current timestamp string |
| `setInnerHTML(oEl, sHTML)` | None | Sets innerHTML cross-browser safely |

---

## 9. `sapphire.page` — Page Data API

**File:** `WEB-CORE\scripts\sapphirecore.js` / `sapphirelist.js`

The `sapphire.page` namespace provides contextual information about the current page and enables interaction with page state.

### Properties

| Property | Type | Description |
|---|---|---|
| `name` | String | Name of the current page |
| `data` | Object | Client-side Property List of page data (only populated if `jsdebug` is enabled) |
| `disabled` | Boolean | `true` if the page is disabled |

### Methods

#### `sapphire.page.getSDCId()`
Returns the SDCId of the current Maintenance, List, or DataEntry page. Returns: `String`.

#### `sapphire.page.getKeyId1()` / `getKeyId2()` / `getKeyId3()`
Gets the current keyid (semicolon-delimited list) from any Core Page Type. On a List page, returns selected keys; on Maintenance, returns the form field value. Returns: `String`.

#### `sapphire.page.navigate(sUrl, sReleaseLockFlag, sTarget, oForm)`

Navigates to another URL from a LabVantage page using POST (supports Unicode and large payloads). Supersedes the legacy `navigateTo` method.

| Parameter | Type | Description |
|---|---|---|
| `sUrl` | String | Target URL |
| `sReleaseLockFlag` | String | `"Y"` to release RSet locks on navigation |
| `sTarget` | String | Form/link target |
| `oForm` | Object | Optional form object to submit to the URL |

#### `sapphire.page.getSelected(sColumn, sElementId)`

Gets selected column values from any List, Maintenance, or DataEntry page. Can also get selected values from detail elements on a Maintenance page by passing the element ID. Returns: `String`.

#### `sapphire.page.maximizeLayout()`
Maximizes the page so the layout menu bar is hidden. Returns: `String`.

#### `sapphire.page.minimizeLayout()`
Minimizes the page so the layout menu bar is visible. Returns: `String`.

#### `sapphire.page.toggleLoading(lShow)`
Shows or hides the loading bar in the center of the screen.

| Parameter | Type | Description |
|---|---|---|
| `lShow` | Boolean | `true` forces the bar to show without toggling |

#### `sapphire.page.getPageType()`
Returns the page type ID for the current page, or empty string if not found. Returns: `String`.

#### `sapphire.page.toggleDisable(lDisable)`
Places a modal cover over the page rendering it unusable (`true`) or removes it (`false`).

---

### 9.1 `sapphire.page.list` — List Page API

**File:** `WEB-CORE\scripts\sapphirelist.js`

LabVantage API for List pages based on the MaintenanceList Page Type.

#### `add(sAddPage, sAddTarget, oInitValidation, lStoreSelection)`
Opens the SDI Maintenance page for adding a new SDI.

#### `edit(sEditPage, sEditTarget)`
Opens the edit page for the selected SDI.

#### `copy(sCopyPage, sCopyTarget)`
Copies the selected SDI.

#### `remove(sConfirmMsg, sPostDeletePage)`
Deletes the selected SDI(s) with optional confirmation.

#### `report(sReportId)`
Runs a report for the selected SDI(s).

#### `refresh()`
Refreshes the list.

#### `getSelected(sColumn)` 
Gets selected column values from the list.

#### `getIsSelected()`
Returns `true` if at least one item is selected. Returns: `Boolean`.

#### `getSDCId()`
Returns the SDCId of the list. Returns: `String`.

#### `getKeyId1()` / `getKeyId2()` / `getKeyId3()`
Returns the key column value(s) from selected rows.

#### `getQueryId()`
Returns the current query ID. Returns: `String`.

#### `getKeyColumns()`
Returns the key column definitions for the list.

#### `getDescColumn()`
Returns the description column for the list.

#### `getColumnIds()`
Returns all column IDs for the list.

#### `getReturnColumnIds()`
Returns column IDs that are returned on selection.

#### `getRowColumnIds()`
Returns column IDs for all rows.

#### `selectAll()` / `clearSelection()`
Selects or clears all rows.

#### `expandOrCollapse()`
Expands or collapses grouped rows.

#### `getSelectedKeyId1(sDelimiter)` / `getSelectedKeyId2(sDelimiter)` / `getSelectedKeyId3(sDelimiter)`
Returns semicolon (or custom delimiter) separated list of selected key values.

#### `hideColumn(sColumnId)` / `showColumn(sColumnId)`
Hides or shows a column by ID.

#### `addColumn(sColumnId, sLabel, sWidth)`
Dynamically adds a column to the list.

#### `showVersionStatus()`
Shows the version status column in the list.

#### `getSelectedValue(sColumn)` / `getSelectedValueArray(sColumn)`
Returns the value(s) of a specific column for selected rows.

#### `getValue(iRow, sColumn)` / `getValueItem(iRow, sColumn)`
Returns the value at a specific row and column.

#### `getColumnValue(sKeyId1, sColumn)` / `getColumnValues(sColumn)`
Returns column value(s) by key or for all rows.

---

### `sapphire.page.list.util`

#### `findInArray(saArray, sValue)`
Finds a value in an array. Returns: `Number` (index).

#### `rowsAndColumnsToString(oRows, saColumns)`
Converts row/column data to a delimited string.

---

### 9.2 `sapphire.page.maint` — Maintenance Page API

#### `add(sAddPage)`
Opens the add page for a new SDI.

#### `report(sReportId)`
Runs a report for the current SDI.

#### `preSave()`
Executes pre-save validation logic.

#### `save()` / `saveAsTemplate()`
Saves the current record or as a template.

#### `reset()`
Resets the form to its last saved state.

#### `refresh()`
Refreshes the maintenance form.

#### `getSelected(sColumn)` / `getSelectedKeyId1()` / `getSelectedKeyId2()` / `getSelectedKeyId3()`
Returns selected values from the maintenance page.

#### `getKeyId1()` / `getKeyId2()` / `getKeyId3()`
Returns the current record's key values.

#### `getSDCId()`
Returns the SDCId of the maintenance page.

#### `getQueryId()`
Returns the query ID.

#### `getRSetId()`
Returns the RSet ID.

#### `getIsSelected()`
Returns whether an item is selected. Returns: `Boolean`.

#### `getSaveSuccessful()`
Returns whether the last save was successful. Returns: `Boolean`.

#### `getPostSaveSuccessful()`
Returns whether post-save processing was successful. Returns: `Boolean`.

#### `setFieldValue(sFieldId, sValue)`
Sets a field's value on the maintenance form.

| Parameter | Type | Description |
|---|---|---|
| `sFieldId` | String | Column/field ID |
| `sValue` | String | Value to set |

#### `getFieldValue(sFieldId)`
Gets a field's value from the maintenance form. Returns: `String`.

#### `getSelectedColumnValue(sColumn)`
Gets the selected detail row's column value. Returns: `String`.

#### `getRowCount(sElementId)`
Returns the row count of a detail element.

#### `getAllColumnValues(sColumn, sElementId)`
Returns all values of a column across rows.

#### `getLocked()`
Returns whether the current record is locked. Returns: `Boolean`.

#### `getLockedBy()`
Returns the user ID who locked the record. Returns: `String`.

#### `isGrid()`
Returns whether the current maintenance form is in grid mode. Returns: `Boolean`.

---

### 9.3 `sapphire.page.request`

#### `sapphire.page.request.getParameter(sParam)`
Returns a URL parameter value from the current page request. Returns: `String`.

---

### 9.4 `sapphire.page.dataEntry`

#### `getQuerySelectedKeys()`
Returns the selected keys from a query on a Data Entry page.

#### `makeEntriesUnique()`
Removes duplicate entries from the Data Entry grid.

---

### 9.5 `sapphire.page.elements.dataView`

API for managing DataView elements on Maintenance pages.

| Method | Returns | Description |
|---|---|---|
| `getSelected(sElementId)` | Object | Gets selected rows in a dataView element |
| `getRowCount(sElementId)` | Integer | Gets the row count |
| `filterRows(sElementId, sFilter)` | None | Applies a row filter |
| `moveUp(sElementId)` | None | Moves selected row up |
| `moveDown(sElementId)` | None | Moves selected row down |
| `selectAll(sElementId)` | None | Selects all rows |
| `getKeyColumns(sElementId)` | Object | Returns key column definitions |
| `getFieldValue(sElementId, iRow, sColumn)` | String | Gets a field value from a specific row |

#### `sapphire.page.elements.dataView.events`

| Method | Description |
|---|---|
| `attachAddEvent(sElementId, vFunction)` | Fires when a row is added |
| `attachRemoveEvent(sElementId, vFunction)` | Fires when a row is removed |
| `attachUpEvent(sElementId, vFunction)` | Fires when a row is moved up |
| `attachDownEvent(sElementId, vFunction)` | Fires when a row is moved down |
| `attachResetEvent(sElementId, vFunction)` | Fires when the element is reset |

---

### 9.6 `sapphire.page.elements.sdcLinkMaint`

API for managing SDC Link Maint elements on Maintenance pages. Methods mirror `dataView`.

#### `sapphire.page.elements.sdcLinkMaint.events`

| Method | Description |
|---|---|
| `attachAddEvent(sElementId, vFunction)` | Fires on add |
| `attachRemoveEvent(sElementId, vFunction)` | Fires on remove |
| `attachUpEvent(sElementId, vFunction)` | Fires on move up |
| `attachDownEvent(sElementId, vFunction)` | Fires on move down |
| `attachResetEvent(sElementId, vFunction)` | Fires on reset |

---

## 10. `sapphire.util` — Utility Methods and Classes

**File:** `WEB-CORE\scripts\sapphirecore.js`

### Root Methods

#### `sapphire.util.evaluateExpression(sString)`

Evaluates a string containing `[token]` placeholders and replaces them with page values. Works with field/column names, `keyid1`, `sdcid`, and JavaScript strings (starting `javascript:`). Returns: `String`.

### 10.1 `sapphire.util.number` — Number Utilities

#### `parseFraction(sNumber)`
Converts a fraction string (e.g. `"1 3/4"` or `"3/4"`) to decimal. Returns: `Number` (NaN if invalid).

#### `number(sNumber, lAllowFractions)`
Validates whether a string is a valid number.

| Parameter | Type | Description |
|---|---|---|
| `sNumber` | String | Number string to validate |
| `lAllowFractions` | Boolean | Whether to accept fractions as valid |

Returns: `Boolean`.

---

### 10.2 `sapphire.util.array` — Array Utilities

Added as prototypes to the `Array` object.

#### `findInArray(saArray, sValue, lIgnoreCase)`
Returns the index of `sValue` in the array. Also accessible as `array.find(sValue, lIgnoreCase)`. Returns: `Number`.

#### `toString(saArray, sDelim)`
Converts an array to a delimited string. Returns: `String`.

---

### 10.3 `sapphire.util.string` — String Utilities

Added as prototypes to the `String` object.

| Method | Returns | Description |
|---|---|---|
| `escapeRegExp(sStr)` | String | Escapes special regex characters in a string |
| `trim(sString, sChars)` | String | Trims chars from both ends (defaults to space); also `string.trim(sChars)` |
| `leftTrim(sString, sChars)` | String | Trims chars from left; also `string.leftTrim(sChars)` |
| `rightTrim(sString, sChars)` | String | Trims chars from right; also `string.rightTrim(sChars)` |
| `getTokens(sString, sDelim)` | String Array | Splits a string into tokens by delimiter |
| `replaceAll(sString, sFind, sReplace)` | String | Replaces all occurrences of sFind with sReplace |
| `startsWith(sString, sPrefix)` | Boolean | Returns true if string starts with prefix |
| `endWith(sString, sSuffix)` | Boolean | Returns true if string ends with suffix |

---

### 10.4 `sapphire.util.timer` — Timing

| Method | Description |
|---|---|
| `start()` | Starts the timer |
| `lap()` | Returns elapsed time since start or last lap (ms) |
| `stop()` | Stops the timer and returns total elapsed time (ms) |

---

### 10.5 `sapphire.util.propertyList` — Property List (Key-Value Store)

A client-side Property List mirrors the server-side LV PropertyList.

| Method | Returns | Description |
|---|---|---|
| `create()` | Object | Creates a new empty property list |
| `set(oPL, sId, sValue)` | None | Sets a property value |
| `get(oPL, sId)` | String | Gets a property value |
| `setId(oPL, sId)` | None | Sets the ID of the property list |
| `getId(oPL)` | String | Gets the ID of the property list |
| `setSequence(oPL, iSeq)` | None | Sets the sequence number |
| `toForm(oPL, oForm)` | None | Serializes the property list to a form |
| `remove(oPL, sId)` | None | Removes a property by ID |
| `getCollection(oPL)` | Object Array | Returns all properties as an array |
| `getPropertyList(oPL, sId)` | Object | Gets a nested property list |
| `toJSONString(oPL)` | String | Serializes the property list to JSON |
| `clone(oPL)` | Object | Deep-clones the property list |

---

### 10.6 `sapphire.util.script` — Script Utilities

| Method | Returns | Description |
|---|---|---|
| `createProxyFunction(vFn, oScope)` | Function | Creates a proxy function in a given scope |
| `execScript(sScript)` | Any | Executes a string as JavaScript in a cross-browser way |
| `getRequestArguments(sArgString)` | Object | Parses a request argument string into an object |

---

### 10.7 `sapphire.util.dom` — DOM Utilities

| Method | Returns | Description |
|---|---|---|
| `getElementByName(sName, oDoc)` | Object | Gets an element by name (cross-browser) |
| `getAttribute(oEl, sAttr)` | String | Gets an attribute value cross-browser |
| `setAttribute(oEl, sAttr, sValue)` | None | Sets an attribute cross-browser |
| `containsNode(oParent, oChild)` | Boolean | Returns true if oParent contains oChild |
| `getAll(oEl)` | Object Array | Returns all child nodes (cross-browser; use instead of deprecated `sapphire.util.getAll`) |
| `setFormField(oForm, sName, sValue)` | None | Sets a form field value by name |
| `getOuterHTML(oEl)` | String | Returns the outer HTML of an element |
| `findFrame(sFrameName, oWin)` | Object | Finds a frame by name within a window |
| `focusElement(oEl)` | None | Sets focus to an element safely |
| `createDocumentFragment(sHTML)` | Object | Creates a document fragment from HTML |
| `getChildren(oEl)` | Object Array | Returns direct child elements |
| `createForm(oData, sAction, sTarget)` | Object | Creates a hidden form from a data object |
| `moveTableRow(oTbl, iFrom, iTo)` | None | Moves a table row from one index to another |

---

### 10.8 `sapphire.util.date` — Date Comparison

| Method | Returns | Description |
|---|---|---|
| `compare(sDate1, sDate2)` | Integer | Compares two date strings; negative/zero/positive |
| `compareTime(sTime1, sTime2)` | Integer | Compares two time strings |

---

### 10.9 `sapphire.util.url` — URL Utilities

| Method | Returns | Description |
|---|---|---|
| `encode(sValue)` | String | URL-encodes a string |
| `decode(sValue)` | String | URL-decodes a string |
| `addToForm(oForm, sKey, sValue)` | None | Adds a key-value pair as a hidden form field |
| `getFromForm(oForm, sKey)` | String | Gets a form field value by key |

#### `sapphire.util.url.href.get()`
Returns the current page's full URL (href). Returns: `String`.

---

### 10.10 `sapphire.util.dataset` — DataSet Utilities

Client-side representation of a server-side DataSet (result set).

| Method | Returns | Description |
|---|---|---|
| `create(saColumns)` | Object | Creates a new empty DataSet with column definitions |
| `isValidColumn(oDS, sColumn)` | Boolean | Returns whether the column exists in the DataSet |
| `getValue(oDS, iRow, sColumn)` | String | Gets a value at a specific row and column |
| `setValue(oDS, iRow, sColumn, sValue)` | None | Sets a value at a specific row and column |
| `find(oDS, sColumn, sValue)` | Integer | Finds the first row index where the column matches the value |
| `addColumn(oDS, sColumn)` | None | Adds a new column to the DataSet |
| `addRow(oDS, oRowData)` | None | Adds a new row; `oRowData` is a key-value object |
| `getRowCount(oDS)` | Integer | Returns the number of rows |
| `getColumnCount(oDS)` | Integer | Returns the number of columns |
| `toJSONString(oDS)` | String | Serializes the DataSet to a JSON string |
| `clone(oDS)` | Object | Deep-clones the DataSet |

---

## 11. `sapphire.debug` — Debugging Utilities

**File:** `WEB-CORE\scripts\sapphirecore.js`

### Properties

| Property | Type | Description |
|---|---|---|
| `iaFrameIndex` | Integer Array | Frame registration array |

### Methods

#### `sapphire.debug.show()`
Opens the JavaScript debug popup window.

#### `sapphire.debug.executeFunction(sFrameName, sFunctionText)`

Executes a function in a specific frame (useful for testing cross-frame code).

| Parameter | Type | Description |
|---|---|---|
| `sFrameName` | String | Name of the target frame |
| `sFunctionText` | String | Script or function to execute |

```javascript
if (sapphire.connection.isDevMode) {
    sapphire.debug.show();
}
```

---

## 12. `sapphire.worksheet` — ELN Worksheet API

The `sapphire.worksheet` object is **dynamically generated** and is only available within the ELN Worksheet Manager context — in Custom Operation scripts and Control editor JavaScript files.

### 12.1 Worksheet Manager Context Methods

#### `sapphire.worksheet.getConfigProperty(propertyid, callback)`

Returns a config property of the current Control via callback.

| Parameter | Type | Description |
|---|---|---|
| `propertyid` | String | Property name |
| `callback` | Function | Called with the property value as the first argument |

#### `sapphire.worksheet.setConfigProperty(propertyid, propertyvalue, refresh)`

| Parameter | Type | Description |
|---|---|---|
| `propertyid` | String | Property name |
| `propertyvalue` | String | New value |
| `refresh` | Boolean | `true` to refresh the Control after setting |

#### `sapphire.worksheet.getConfigProperties(callback)`
Returns all config properties as a JSON string via callback.

#### `sapphire.worksheet.setConfigProperties(jsonprops, merge, refresh)`

| Parameter | Type | Description |
|---|---|---|
| `jsonprops` | String | JSON string of properties |
| `merge` | Boolean | `true` to merge; `false` to overwrite |
| `refresh` | Boolean | `true` to refresh after setting |

#### `sapphire.worksheet.getCurrentType()`
Returns current selection type: `"W"` (Worksheet), `"S"` (Section), `"I"` (Control).

#### `sapphire.worksheet.getCurrentId()`
Returns the ID of the currently selected item.

#### `sapphire.worksheet.getCurrentVersion()`
Returns the version of the currently selected item.

#### `sapphire.worksheet.refresh()`
Refreshes the current Control.

#### `sapphire.worksheet.refreshSection()`
Refreshes all Controls in the current Section.

#### `sapphire.worksheet.refreshWorksheet()`
Reloads the entire Worksheet.

#### `sapphire.worksheet.scrollToTop(setfocus)` / `scrollToBottom(setfocus)`

| Parameter | Type | Description |
|---|---|---|
| `setfocus` | Boolean | `true` to give focus to the first/last item |

#### `sapphire.worksheet.setCurrentWorksheetItem(worksheetitemid, worksheetitemversionid)`
Selects/sets the current Worksheet item.

#### `sapphire.worksheet.getWorksheetManagerWidth()` / `getWorksheetManagerHeight()`
Returns the Worksheet Manager dimensions in pixels.

#### `sapphire.worksheet.getWorksheetPanelWidth()` / `getWorksheetPanelHeight()`
Returns the middle content panel dimensions in pixels.

#### `sapphire.worksheet.addReference(refworksheetid, refworksheetversionid, refid, refversion, reftype, reffunction, refresh)`

Adds a reference to the current Control.

| Parameter | Type | Description |
|---|---|---|
| `refworksheetid` | String | Referenced Worksheet ID |
| `refworksheetversionid` | String | Referenced Worksheet version |
| `refid` | String | Referenced Section/Control ID |
| `refversion` | String | Referenced Section/Control version |
| `reftype` | String | `"S"` (Section) or `"I"` (Control) |
| `reffunction` | String | `"link"` or `"include"` |
| `refresh` | Boolean | `true` to refresh after adding |

#### `sapphire.worksheet.selectWorksheetItem(worksheetid, worksheetversionid, callback)`
Lets the user pick a section or control from a worksheet via a selection dialog. The `callback` receives: `worksheetid`, `worksheetversionid`, `id`, `version`, `type`, `copy`.

### 12.2 Global Worksheet Context Methods

#### `sapphire.worksheet.addWorksheet(workbookid, workbookversionid, createCallback)`
Displays the Add Worksheet dialog.

#### `sapphire.worksheet.copyWorksheet(worksheetid, worksheetversionid, copyCallback)`
Displays the Copy Worksheet dialog.

#### `sapphire.worksheet.addTemplate(templateprivacyflag, createCallback)`
Creates a new Worksheet Template. `templateprivacyflag`: `"G"` (Global) or `"O"` (Owner).

#### `sapphire.worksheet.addControlTemplate(templateprivacyflag, createCallback)`
Creates a new Control Template.

---

## 13. `sapphire.sdc` — Client-Side SDC Processor

**File:** `WEB-CORE\scripts\sapphirecore.js`

Loads SDC properties from the server-side SDC processor and provides client-side access.

#### `sapphire.sdc.setProperties(oProperties)`

Passes properties from the SDC processor. `oProperties` is a JSON representation of the property list returned from the SDC processor.

#### `sapphire.sdc.getProperties()`
Returns the loaded SDC properties. Returns: `Object` (property list).

#### `sapphire.sdc.setProperty(sPropertyId)`
Returns the value for the specified property ID. Returns: `String`.

#### `sapphire.sdc.getColumns()`
Returns the columns collection. Returns: `Object Array` (property list collection).

---

## 14. `sapphire.cookie` — Cookie Management

**File:** `WEB-CORE\scripts\sapphirecore.js`

#### `sapphire.cookie.getCookieCrumb(sCrumbId)`
Gets a value from a cookie by name. Returns: `String`.

#### `sapphire.cookie.setCookieCrumb(sCrumbId, sValue)`
Sets a cookie value by name.

---

## 15. `sapphire.browser` — Browser Detection

**File:** `WEB-CORE\scripts\sapphirecore.js`

### Properties

| Property | Type | Description |
|---|---|---|
| `supported` | Boolean | `true` if the browser is supported by LabVantage |
| `osId` | String | Operating system identifier |
| `compat` | String | Compatibility mode string |
| `compatMode` | Boolean | `true` if IE8/IE9 is in compatibility mode |
| `compatVersion` | Float | Version in compatibility mode (e.g. 7.0) |
| `version` | Float | Actual browser version |
| `mozillaVersion` | Float | Mozilla engine version |
| `webkitVersion` | Float | Apple WebKit version |
| `mozilla` | Boolean | `true` if Mozilla-based |
| `webkit` | Boolean | `true` if WebKit-based |
| `firefox` | Boolean | `true` if Firefox |
| `chrome` | Boolean | `true` if Chrome |
| `safari` | Boolean | `true` if Safari |
| `android` | Boolean | `true` if Android default browser |
| `skyfire` | Boolean | `true` if Skyfire mobile browser |
| `blackberry` | Boolean | `true` if BlackBerry browser |
| `opera` | Boolean | `true` if Opera |
| `ie` | Boolean | `true` if Internet Explorer |
| `mobile` | Boolean | `true` if on a mobile platform |
| `gc` | Boolean | `true` if garbage collection routines are required (IE only) |

```javascript
if (sapphire.browser.chrome) {
    // Chrome-specific behavior
}
if (sapphire.browser.mobile) {
    // mobile-specific layout
}
```

---

## 16. `sapphire.style` — CSS Manipulation

**File:** `WEB-CORE\scripts\sapphirecore.js`

#### `setClassElement(sTheId, sTheRule, sTheElement, sTheValue)`
Sets a CSS property within a CSS class. Returns: `String`.

| Parameter | Type | Description |
|---|---|---|
| `sTheId` | String | ID of the CSS stylesheet element |
| `sTheRule` | String | CSS rule/class name |
| `sTheElement` | String | CSS property name |
| `sTheValue` | String | Value to set |

#### `getClassElement(sTheId, sTheRule, sTheElement)`
Gets a CSS property value from a class. Returns: `String`.

#### `getstyleSheet(sTheId, oDoc)`
Gets a stylesheet element by ID. Returns: `Object`.

#### `createstyleSheet(sTheId, oDoc)`
Creates a new stylesheet element. Returns: `Object`.

#### `addRule(sTheId, sTheRule, sstyle, oDoc)`
Creates or edits a rule in a stylesheet. Returns: `Integer` (new rule index).

| Parameter | Type | Description |
|---|---|---|
| `sTheId` | String | Stylesheet ID |
| `sTheRule` | String | CSS rule name (e.g. `"#arule"` or `"DIV"`) |
| `sstyle` | String | CSS content (e.g. `"background-color:red"`) |
| `oDoc` | Object | Document (defaults to current document) |

#### `getRuleIndex(sTheId, sTheRule, oDoc)`
Gets the index of a CSS rule in a stylesheet. Returns: `Integer`.

#### `removeRule(sTheId, sTheRule, oDoc)`
Removes a CSS rule from a stylesheet.

#### `setDisplay(oEl, lShow)`
Updates the `display` style on an element according to its type.

| Parameter | Type | Description |
|---|---|---|
| `oEl` | Object | Element to update |
| `lShow` | Boolean | `true` to show, `false` to hide |

---

## 17. `sapphire.navigator` — Navigator Utilities

**File:** `WEB-CORE\scripts\sapphirecore.js`

### Properties

| Property | Type | Description |
|---|---|---|
| `isNavigator` | Boolean | `true` if the current page is rendered inside the Navigator layout |

### Methods

#### `sapphire.navigator.open(navigatornodeid, pageid, sdcid, Keyid1, Keyid2, Keyid3)`

Opens a Navigator layout from generic layout border bar configuration.

| Parameter | Type | Description |
|---|---|---|
| `navigatornodeid` | String | Navigator node in the Navigator Policy |
| `pageid` | String | Optional originating page ID (defaults to `sapphire.page.name`) |
| `sdcid` | String | Optional SDC ID (defaults to `sapphire.page.getSDCid()`) |
| `Keyid1` | String | Optional keyid1 (defaults to `sapphire.page.getKeyid1()`) |
| `Keyid2` | String | Optional keyid2 |
| `Keyid3` | String | Optional keyid3 |

### `sapphire.navigator.util`

#### `getTop()`
Returns a reference to the correct top window regardless of whether the page is inside navigator or another layout.

#### `getTopName()`
Returns the frame name of the correct top window.

---

## 18. `sapphire.userConfig` — User Configuration

**File:** `WEB-CORE\scripts\sapphirecore.js`

Manages user configuration cookies.

#### `sapphire.userConfig.set(sPropertyId, sValue)`
Sets a value into user configuration cookies.

#### `sapphire.userConfig.get(sPropertyId)` *(DEPRECATED)*
Gets a value from user configuration cookies. Note: deprecated — does not reliably return a value in current versions.

---

## 19. `sapphire.garbage` — Memory Management

**File:** `WEB-CORE\scripts\sapphirecore.js`

Used internally to manage memory leaks in IE. Rarely needed in custom code.

### Properties

| Property | Type | Description |
|---|---|---|
| `bin` | Object Array | Array of elements queued for garbage collection |
| `limit` | Integer | Maximum number of elements before collection runs |
| `size` | Integer | Current number of elements in the bin |

### Methods

#### `sapphire.garbage.add(oEl)`
Adds an element to the garbage collection bin.

#### `sapphire.garbage.collect()`
Cleans up elements in the bin (sets innerHTML to empty, breaks circular references).

---

## 20. `sapphire.lookup` — Lookup Dialogs

**File:** `WEB-CORE\scripts\sapphirecore.js`

### `sapphire.lookup.open(sUrl, ...)`
Opens a generic lookup window.

### `sapphire.lookup.sdi` — SDI Lookup

| Method | Description |
|---|---|
| `openSimple(sSDCId, sCallback, ...)` | Opens a simple SDI lookup dialog |
| `openPage(sSDCId, sCallback, ...)` | Opens a full-page SDI lookup |
| `openDefault(sSDCId, sCallback, ...)` | Opens the default lookup for an SDC |
| `open(sSDCId, sCallback, ...)` | Opens a lookup dialog (general) |

### `sapphire.lookup.date` — Date Lookup

| Property/Method | Description |
|---|---|
| `newlookup` | Boolean: `true` to use the new date picker |
| `open(sFieldId, ...)` | Opens a date picker for a field |

### `sapphire.lookup.reftype` — Ref Type Lookup

#### `open(sFieldId, ...)`
Opens a reference type lookup dialog.

### `sapphire.lookup.fileSystem` — File System Lookup

| Method | Description |
|---|---|
| `browseFile(sCallback)` | Opens a file browser |
| `browseWeb(sCallback)` | Opens a web file browser |
| `browseZip(sCallback)` | Opens a ZIP file browser |
| `browseFolder(sCallback)` | Opens a folder browser |
| `create(sType, sCallback)` | Creates a new file lookup of a given type |

### `sapphire.lookup.util`

| Method | Description |
|---|---|
| `getCoord(oEl)` | Returns `{x, y}` coordinates for positioning a lookup window |
| `openWindow(sUrl, sOptions)` | Opens a lookup in a new browser window |

---

## 21. Portal Maintenance Page JS API (Stellar)

The Portal (Stellar) Maintenance Page API is available within JavaScript events on **Stellar Maint page types**. All calls use `this.` as the context prefix.

### Events That Trigger JS

| Event | Where Configured |
|---|---|
| Page Load / Unload | Page Events on Stellar Page |
| Section Load / Unload | Section Events on Maintenance Element |
| Column On Focus | Column configuration on Maintenance Element |
| Column On Change | Column configuration on Maintenance Element |
| Column On Blur | Column configuration on Maintenance Element |
| Operation Button Click | Client Side Script on Operation |

### Basic Field Methods

```javascript
this.setFocus('columnid')              // Move cursor focus to a field
this.setField('columnid', 'value')     // Set a field value
this.disableField('columnid')          // Make a field read-only
this.enableField('columnid')           // Make a field editable
this.getField('columnid', 'default')   // Get a field value (returns default if not found)
```

### User Messaging (Toast Notifications)

```javascript
this.alert('message')    // Show a toast alert
this.info('message')     // Show an info toast
this.success('message')  // Show a success toast
this.error('message')    // Show an error toast
this.warn('message')     // Show a warning toast

// Confirmation dialog (returns a Promise)
this.confirm('Title', 'Are you sure?')
    .then(() => { this.alert('Yes confirmed'); })
    .catch(() => { this.alert('Cancelled'); });
```

### `this.executeOperation(operationid)`

Executes any page operation by ID (visible Button/Icon or hidden Script type). Returns a Promise.

```javascript
this.executeOperation('myActionOperation')
    .then((props) => { console.log('Result props:', props); })
    .catch(() => { this.error('Something went wrong'); });
```

### `this.getDataSource(datasourceid, parameters)`

Retrieves data from a Stellar Datasource configured on the page. Returns a Promise resolving to a datasource object.

```javascript
// SDIData datasource — retrieve a single record
this.getDataSource('moredata', { sysuserid: this.getField('sysuserid') })
    .then((datasource) => {
        this.success('Disabled reason: ' + datasource.getValue(0, 'disabledreason', 'none'));
    })
    .catch(() => { this.error('Could not load data'); });

// ColumnValueCounter datasource
this.getDataSource('count', { desc: 'New' })
    .then((data) => { this.alert('Count: ' + data.getCount('Initial')); });

// SDIList datasource
this.getDataSource('list', { desc: 'New' })
    .then((data) => { this.alert('First sample: ' + data.getValue(0, 'sampledesc', '(none)')); });
```

> **Security:** All Portal API calls include a CRC check. Operations of type "Script" can be used to hide which LV Action is being invoked.

---

## 22. Common Patterns and Recipes

### 22.1 Running Code on Page Load (Safe DOM Access)

```javascript
sapphire.events.registerLoadListener(function() {
    var el = document.getElementById("sampletypeid");
    if (el) {
        onSampleTypeChange();
        sapphire.events.attachEvent(el, "change", onSampleTypeChange);
    }
});

function onSampleTypeChange() {
    var sampleType = document.getElementById("sampletypeid").value;
    // toggle visibility, run AJAX, etc.
}
```

### 22.2 Field-Level Change → AJAX → Update Another Field

```javascript
sapphire.events.registerLoadListener(function() {
    var lotField = document.getElementById("lotid");
    sapphire.events.attachEvent(lotField, "change", function() {
        sapphire.ajax.callClass(
            "com.syngenta.ajax.GetLotDetails",
            function(resp) {
                if (resp.found) {
                    document.getElementById("expirydate").value = resp.expirydate;
                    document.getElementById("statusid").value   = resp.statusid;
                }
            },
            { lotid: lotField.value }
        );
    });
});
```

### 22.3 Confirm Before Submitting

```javascript
function confirmAndSubmit() {
    sapphire.ui.dialog.confirm(
        "Confirm Submit",
        "This will lock the record. Proceed?",
        "onSubmitConfirm"
    );
}

function onSubmitConfirm(result) {
    if (result) {
        document.forms[0].submit();
    }
}
```

### 22.4 Show a Loading Indicator During AJAX

```javascript
function runLongOperation() {
    sapphire.ui.progress.show();
    sapphire.ajax.callClass(
        "com.syngenta.ajax.ProcessBatch",
        function(resp) {
            sapphire.ui.progress.hide();
            sapphire.ui.dialog.alert(resp.message);
        },
        { batchid: getBatchId() },
        true, false,
        function(err) {
            sapphire.ui.progress.hide();
            sapphire.ui.dialog.alert("Error: " + err);
        }
    );
}
```

### 22.5 Show a URL in a Popup Dialog (LV Page)

```javascript
function openSampleLookup(keyid1) {
    sapphire.ui.dialog.open(
        "Sample Details",
        "rc?command=SampleMaint&sdcid=s_Sample&keyid1=" + keyid1,
        true, 900, 600,
        {"Close": "sapphire.ui.dialog.close(sapphire.ui.dialog.dialogCount)"},
        null, true
    );
}
```

### 22.6 Access Current User and Navigate Programmatically

```javascript
var currentUser = sapphire.connection.sysUserId;
var userRoles   = sapphire.connection.sysUserRoleList;

if (userRoles.indexOf("APPROVER") >= 0) {
    showApproverPanel();
}

// Navigate using POST (handles Unicode, large payloads)
sapphire.page.navigate("rc?command=SampleList&sdcid=s_Sample", "Y");
```

### 22.7 Departmental Security Check Before Action

```javascript
sapphire.connection.dsCall(
    "s_Sample",
    selectedKeyid1,
    "APPROVE",
    "",
    "onDSCheckFailed(this.failedsdis)",
    doApprove,
    [selectedKeyid1]
);

function doApprove(keyid1) {
    sapphire.ajax.callService("ProcessAction", null, {
        actionid:      "ApproveSample",
        actionversion: "1",
        sdcid:         "s_Sample",
        keyid1:        keyid1
    });
}
```

### 22.8 Get Page Context Keys

```javascript
// Works on any page type (List, Maint, DataEntry)
var sdcid  = sapphire.page.getSDCId();
var keyid1 = sapphire.page.getKeyId1();
var keyid2 = sapphire.page.getKeyId2();

// Maintenance-specific
var locked   = sapphire.page.maint.getLocked();
var lockedBy = sapphire.page.maint.getLockedBy();
var isGrid   = sapphire.page.maint.isGrid();

// List-specific — get all selected keys
var selectedKeys = sapphire.page.list.getSelectedKeyId1(";");
```

### 22.9 Evaluate Token Expressions

```javascript
// Replace [fieldname] and [keyid1] tokens with live page values
var evaluatedUrl = sapphire.util.evaluateExpression(
    "rc?command=SampleView&sdcid=[sdcid]&keyid1=[keyid1]"
);
sapphire.page.navigate(evaluatedUrl);
```

### 22.10 String and Array Utilities

```javascript
// String utilities
var trimmed = sapphire.util.string.trim("  hello  ");    // "hello"
var tokens  = sapphire.util.string.getTokens("a;b;c", ";");  // ["a","b","c"]
var replaced = sapphire.util.string.replaceAll("aababc", "a", "x");  // "xxbxbc"

// Array utilities
var idx = sapphire.util.array.findInArray(["FOO","BAR"], "bar", true);  // 1
var str = sapphire.util.array.toString(["a","b","c"], ",");  // "a,b,c"

// Number utilities
var dec = sapphire.util.number.parseFraction("1 3/4");  // 1.75
var valid = sapphire.util.number.number("3.14", false);  // true
```

---

## 23. Calling System Actions from JavaScript

System Actions are invoked from JavaScript via `sapphire.ajax.callService("ProcessAction", ...)`.

```javascript
sapphire.ajax.callService(
    "ProcessAction",
    function(response) {
        console.log("newkeyid1:", response.newkeyid1);
    },
    {
        actionid:      "AddSDI",
        actionversion: "1",
        sdcid:         "s_Sample",
        keyid1:        "AUTO",
    }
);
```

### Complete System Actions Reference

| Action | Purpose | Key Input Properties |
|---|---|---|
| `AddSDI` | Create a new SDI record | `sdcid`, `keyid1`, `sdcnumber` |
| `EditSDI` | Edit an SDI field | `sdcid`, `keyid1`, `columnid`, `value` |
| `DeleteSDI` | Delete an SDI | `sdcid`, `keyid1` |
| `AnonymizeSDI` | Anonymize SDI data | `sdcid`, `keyid1` |
| `AddSDIVersion` | Add a version to an SDI | `sdcid`, `keyid1` |
| `SetSDIString` | Set a string column | `sdcid`, `keyid1`, `columnid`, `value` |
| `SetSDIDate` | Set a date column | `sdcid`, `keyid1`, `columnid`, `value` |
| `SetSDINumber` | Set a numeric column | `sdcid`, `keyid1`, `columnid`, `value` |
| `UndoSDIColumnValue` | Undo a column value change | `sdcid`, `keyid1`, `columnid` |
| `AddSDIAttribute` | Add an attribute | `sdcid`, `keyid1`, `attributeid`, `value` |
| `EditSDIAttribute` | Edit an attribute | `sdcid`, `keyid1`, `attributeid`, `value` |
| `GetSDIAttribute` | Get an attribute value | `sdcid`, `keyid1`, `attributeid` |
| `AddSDIAttachment` | Add an attachment | `sdcid`, `keyid1`, `filename`, `filecontent` |
| `DeleteSDIAttachment` | Delete an attachment | `sdcid`, `keyid1`, `attachmentid` |
| `AddSDIAddress` | Add an address | `sdcid`, `keyid1`, `addresstype` |
| `EditSDIAddress` | Edit an address | `sdcid`, `keyid1`, `addresstype` |
| `DeleteSDIAddress` | Delete an address | `sdcid`, `keyid1`, `addresstype` |
| `AddSDIRole` | Add a role to an SDI | `sdcid`, `keyid1`, `roleid` |
| `DeleteSDIRole` | Delete a role from an SDI | `sdcid`, `keyid1`, `roleid` |
| `AddSDISpec` | Add a spec to an SDI | `sdcid`, `keyid1`, `specid` |
| `UnApplySDISpec` | Remove a spec from an SDI | `sdcid`, `keyid1`, `specid` |
| `AddSDISecuritySet` | Add a security set | `sdcid`, `keyid1`, `securitysetid` |
| `AddSDISecurityDept` | Add a departmental security entry | `sdcid`, `keyid1`, `deptid` |
| `EditSDIDetail` | Edit a detail record | `sdcid`, `keyid1`, `detailsdcid`, `detailkeyid1` |
| `EditSDIDataRelation` | Edit a data relation | `sdcid`, `keyid1` |
| `AddSDIFormRule` | Add a form rule | `sdcid`, `keyid1`, `formruleid` |
| `DeleteSDIFormRule` | Delete a form rule | `sdcid`, `keyid1`, `formruleid` |
| `AddSDIWorksheetRule` | Add a worksheet rule | `sdcid`, `keyid1` |
| `DeleteSDIWorksheetRule` | Delete a worksheet rule | `sdcid`, `keyid1` |
| `AddSDIEventPlan` | Add an event plan | `sdcid`, `keyid1`, `eventplanid` |
| `DeleteSDIEventPlan` | Delete an event plan | `sdcid`, `keyid1`, `eventplanid` |
| `AddSDIWorkItem` | Add a work item | `sdcid`, `keyid1`, `workitemid` |
| `ApplySDIWorkItem` | Apply a work item | `sdcid`, `keyid1`, `workitemid` |
| `CancelSDIWorkItem` | Cancel a work item | `sdcid`, `keyid1`, `workitemid` |
| `DeleteSDIWorkItem` | Delete a work item | `sdcid`, `keyid1`, `workitemid` |
| `EditSDIWorkItem` | Edit a work item | `sdcid`, `keyid1`, `workitemid` |
| `SetSDIWIIComplete` | Set work item instance complete | `sdcid`, `keyid1`, `workitemid` |
| `SubmitSDIForApproval` | Submit SDI for approval | `sdcid`, `keyid1` |
| `AddStage` | Add a workflow stage | `sdcid`, `keyid1`, `stageid` |
| `RedoStage` | Re-execute a stage | `sdcid`, `keyid1`, `stageid` |
| `AddDataSet` | Add a dataset | `sdcid`, `keyid1` |
| `DeleteDataSet` | Delete a dataset | `sdcid`, `keyid1`, `datasetid` |
| `AddDataItem` | Add a data item | `sdcid`, `keyid1`, `dataitemid` |
| `EditDataItem` | Edit a data item | `sdcid`, `keyid1`, `dataitemid` |
| `DeleteDataItem` | Delete a data item | `sdcid`, `keyid1`, `dataitemid` |
| `EnterDataItem` | Enter a result for a data item | `sdcid`, `keyid1`, `dataitemid`, `value` |
| `SetDataItemString` | Set a string value on a data item | `sdcid`, `keyid1`, `dataitemid`, `value` |
| `SetDataItemNumber` | Set a numeric value on a data item | `sdcid`, `keyid1`, `dataitemid`, `value` |
| `SetDataItemDate` | Set a date value on a data item | `sdcid`, `keyid1`, `dataitemid`, `value` |
| `SetDataSetNumber` | Set a numeric column on a dataset | `sdcid`, `keyid1`, `datasetid`, `columnid`, `value` |
| `SetCalcExclude` | Exclude a result from calculations | `sdcid`, `keyid1`, `dataitemid` |
| `RedoCalculations` | Recalculate stored calcs | `sdcid`, `keyid1` |
| `EnterResultDataGrid` | Enter results via data grid | `sdcid`, `keyid1` |
| `PromoteArrayResults` | Promote array results | `sdcid`, `keyid1` |
| `AddReplicate` | Add a replicate to a dataset | `sdcid`, `keyid1`, `datasetid` |
| `DeleteDataItemLimit` | Delete a data item limit | `sdcid`, `keyid1`, `dataitemid` |
| `EditDataApproval` | Edit data approval status | `sdcid`, `keyid1` |
| `IsDataReleased` | Check if data is released | `sdcid`, `keyid1` |
| `GenerateReport` | Generate a report | `reportid`, `sdcid`, `keyid1` |
| `PrintReport` | Print a report | `reportid`, `printerid` |
| `SendBOReport` | Send a BO report | `reportid`, `sdcid`, `keyid1` |
| `SendMail` | Send an email | `to`, `subject`, `body` |
| `GenerateLabel` | Generate a label | `sdcid`, `keyid1`, `labelid` |
| `RaiseAlert` | Create an alert notification | `alertdefid`, `sdcid`, `keyid1` |
| `AddEvent` | Add a calendar event | `sdcid`, `keyid1` |
| `AddBucketSDI` | Add an SDI to a bucket | `bucketid`, `sdcid`, `keyid1` |
| `ClearBucket` | Clear a bucket | `bucketid` |
| `GetSDITrackItem` | Get track item for an SDI | `sdcid`, `keyid1` |
| `AdjustTrackItemInv` | Adjust track item inventory | `sdcid`, `keyid1`, `quantity` |
| `ReagentEvent` | Record a reagent event | `sdcid`, `keyid1` |
| `AssignQCBatch` | Assign SDI to a QC batch | `sdcid`, `keyid1`, `qcbatchid` |
| `AddQCSample` | Add a QC sample | `sdcid`, `keyid1` |
| `ExecutePlanItem` | Execute a plan item | `planitemid` |
| `ProcessFile` | Process an input file | `fileid` |
| `ProcessInMessage` | Process an incoming message | `messageid` |
| `CreateProtocol` | Create a protocol | `sdcid`, `keyid1` |
| `AddProtocolTest` | Add a test to a protocol | `protocolid`, `testid` |
| `DeleteProtocolRev` | Delete a protocol revision | `protocolid`, `revisionid` |
| `AddUpdProtocolSample` | Add/update protocol sample | `protocolid`, `sdcid`, `keyid1` |
| `AddPackage` | Add a shipping package | `sdcid`, `keyid1` |
| `AddCOCEntry` | Add a chain of custody entry | `sdcid`, `keyid1` |
| `IsMatch` | Check if SDI matches criteria | `sdcid`, `keyid1` |
| `CheckDates` | Validate date fields | `sdcid`, `keyid1` |
| `ExportWebPage` | Export a page as HTML | `pageid` |
| `ExportPropertyTree` | Export property tree | `sdcid`, `keyid1` |
| `ExportTaskDef` | Export a task definition | `taskdefid` |
| `ExportWorkflowDef` | Export a workflow definition | `workflowdefid` |
| `RecordIncident` | Record a system incident | `sdcid`, `keyid1` |
| `SetBulletinStatus` | Set bulletin status | `bulletinid`, `statusid` |
| `UpRevSiteNPartcpnt` | Up-revision a site/participant | `sdcid`, `keyid1` |
| `PopulateStorageUnitStats` | Populate storage unit statistics | `storageunitid` |
| `AddTimepoint` | Add a timepoint | `sdcid`, `keyid1`, `timepointid` |
| `ApplySamplingPlan` | Apply a sampling plan | `sdcid`, `keyid1`, `samplingplanid` |
| `AddCategoryItem` | Add a category item | `categoryid`, `itemid` |
| `DeleteCategoryItem` | Delete a category item | `categoryid`, `itemid` |
| `DeleteCohort` | Delete a cohort | `cohortid` |
| `AddUpdateParticipant` | Add or update a participant | `sdcid`, `keyid1` |

---

## 24. Field-Level Events on Maintenance Forms

LabVantage Maintenance Form pages render fields as standard HTML input elements.

### 24.1 Getting Field Values

```javascript
var val = document.getElementById("sampletypeid").value;
var form = document.forms[0];
var val2 = form.elements["sampletypeid"].value;

// Using the page API (preferred on maint pages)
var val3 = sapphire.page.maint.getFieldValue("sampletypeid");
```

### 24.2 Setting Field Values

```javascript
document.getElementById("sampledesc").value = "New Description";

// Using the page API
sapphire.page.maint.setFieldValue("sampledesc", "New Description");
```

### 24.3 Hiding/Showing a Field Row

```javascript
function setFieldVisible(fieldId, visible) {
    var el = document.getElementById(fieldId);
    if (el) {
        var row = el;
        while (row && row.tagName !== "TR") {
            row = row.parentNode;
        }
        if (row) {
            row.style.display = visible ? "" : "none";
        }
    }
}

// Or using sapphire.style
sapphire.style.setDisplay(document.getElementById("sampletypeid"), false);
```

### 24.4 Making a Field Read-Only via JS

```javascript
document.getElementById("expirydate").readOnly = true;
document.getElementById("expirydate").style.backgroundColor = "#f0f0f0";
```

### 24.5 Attaching Change/Blur/Focus Events

```javascript
sapphire.events.registerLoadListener(function() {
    ["sampletypeid", "labid"].forEach(function(fid) {
        var el = document.getElementById(fid);
        if (el) {
            sapphire.events.attachEvent(el, "change", function() {
                onFieldChanged(fid, el.value);
            });
        }
    });
});
```

---

## 25. Integration with Java (AJAX ↔ JS)

### Full Flow

```
Browser JS
  sapphire.ajax.callClass("com.syngenta.ajax.MyHandler", callback, {param:"value"})
       │  HTTP POST  (ajaxclass=..., callproperties=JSON)
       ▼
  AjaxRequest Servlet
       │  instantiates MyHandler (extends BaseAjaxRequest)
       ▼
  MyHandler.processRequest(request, response, context)
       │  AjaxResponse.getRequestParameter("param")
       │  business logic
       │  AjaxResponse.addCallbackArgument("result", value)
       │  AjaxResponse.print()
       ▼
  JSON response  { "result": "...", "found": true, ... }
       ▼
  callback(response)
```

### Passing Parameters to Java

```javascript
sapphire.ajax.callClass("com.syngenta.ajax.MyHandler", callback, {
    keyid1:    "SAMP-001",
    labid:     "LAB-A",
    batchMode: "true"      // all values are Strings on the Java side
});
```

```java
String keyid1    = ajaxResponse.getRequestParameter("keyid1");
String labid     = ajaxResponse.getRequestParameter("labid", "DEFAULT_LAB");
boolean batchMode = "true".equals(ajaxResponse.getRequestParameter("batchMode"));
```

### Returning Data to JS

```java
ajaxResponse.addCallbackArgument("statusid",   "ACTIVE");
ajaxResponse.addCallbackArgument("found",      true);
ajaxResponse.addCallbackArgument("count",      42);

DataSet ds = qp.getPreparedSqlDataSet("SELECT ...", params);
ajaxResponse.addCallbackArgument("rows", ds);
ajaxResponse.print();
```

```javascript
function callback(resp) {
    console.log(resp.statusid);    // "ACTIVE"
    console.log(resp.found);       // true
    resp.rows.forEach(function(row) {
        console.log(row.keyid1, row.sampledesc);
    });
}
```

---

## 26. Quick Reference Cheat Sheet

### Root Object

```javascript
sapphire.jsdebug                          // is JS debug enabled?
sapphire.call(fn, args, scope)            // call any function safely
sapphire.statusMsg("text", 3000)          // show status bar message
sapphire.alert("message")                 // LV dialog or native alert
sapphire.translate("textid")              // translate via AJAX
sapphire.encryptField("fieldid")          // RSA encrypt a field
```

### Event Registration

```javascript
sapphire.events.registerLoadListener(fn);
sapphire.events.attachEvent(element, "change", fn);
sapphire.events.detachEvent(element, "change", fn);
sapphire.events.fireEvent(element, "change");
sapphire.events.cancelEvent(event, true);
```

### Session Info

```javascript
sapphire.connection.sysUserId          // current user
sapphire.connection.sysUserRoleList    // role list (comma-separated)
sapphire.connection.databaseId         // database
sapphire.connection.isDevMode          // dev mode flag
sapphire.connection.getConnectionId()  // connection ID
```

### Page Context

```javascript
sapphire.page.getSDCId()               // current SDC
sapphire.page.getKeyId1()              // current keyid1
sapphire.page.getPageType()            // page type
sapphire.page.navigate("rc?command=X", "Y");  // navigate (POST, Unicode-safe)
sapphire.page.toggleDisable(true);     // disable page
sapphire.page.maint.getFieldValue("col");
sapphire.page.maint.setFieldValue("col", "val");
sapphire.page.maint.getLocked();
sapphire.page.list.getSelectedKeyId1(";");
sapphire.page.request.getParameter("param");
```

### Dialogs

```javascript
sapphire.ui.dialog.alert("message");
sapphire.ui.dialog.show("title", "message", true);
sapphire.ui.dialog.open("title", "url", true, 800, 600, {});
sapphire.ui.dialog.confirm("title", "question?", "callbackFn");
sapphire.ui.dialog.prompt("title", "label", "default", "callbackFn");
sapphire.ui.dialog.multiPrompt("title", ["L1","L2"], ["",""], "callbackFn");
sapphire.ui.dialog.close(sapphire.ui.dialog.dialogCount);
sapphire.ui.dialog.setContent(sapphire.ui.dialog.dialogCount, "<p>New content</p>");
```

### AJAX Calls

```javascript
// Custom Java handler
sapphire.ajax.callClass("com.syngenta.ajax.MyHandler", callback, { key: val });

// LV System Action
sapphire.ajax.callService("ProcessAction", callback, {
    actionid: "MyAction", actionversion: "1", sdcid: "s_Sample", keyid1: "..."
});

// LV Command
sapphire.ajax.callCommand("commandName", callback, { param: val });
```

### Progress Indicator

```javascript
sapphire.ui.progress.show();
sapphire.ui.progress.hide();
```

### Utilities

```javascript
// String
sapphire.util.string.trim("  hello  ");
sapphire.util.string.getTokens("a;b;c", ";");
sapphire.util.string.replaceAll("text", "old", "new");
sapphire.util.evaluateExpression("[keyid1]");

// DOM
sapphire.util.dom.getAll(containerEl);
sapphire.util.dom.setFormField(form, "fieldname", "value");
sapphire.style.setDisplay(el, false);  // hide element

// URL
sapphire.util.url.encode("my value");
sapphire.util.url.decode("my%20value");

// Date
sapphire.util.date.compare("2025-01-01", "2024-12-31");  // 1 (first is later)
```

### Worksheet (ELN context only)

```javascript
sapphire.worksheet.refresh();
sapphire.worksheet.refreshSection();
sapphire.worksheet.refreshWorksheet();
sapphire.worksheet.getConfigProperty("propId", function(v) { /* use v */ });
sapphire.worksheet.setConfigProperty("propId", "newValue", true);
sapphire.worksheet.getCurrentType();    // "W" | "S" | "I"
sapphire.worksheet.getCurrentId();
```

### Portal (Stellar) — `this.` context

```javascript
this.getField('columnid', 'default');
this.setField('columnid', 'value');
this.setFocus('columnid');
this.disableField('columnid');
this.enableField('columnid');
this.alert('msg'); this.info('msg'); this.success('msg');
this.error('msg'); this.warn('msg');
this.confirm('title', 'prompt').then(ok).catch(cancel);
this.executeOperation('operationid').then(props => {}).catch(err => {});
this.getDataSource('dsid', { param: val }).then(ds => ds.getValue(0, 'col', ''));
```

---

*Source: LabVantage Sapphire 8.8 help documentation — `labvantagedoc/Content/HTML/jsapi-*.html`, `portal-maint-page-js-api.html`, `ge-util-js.html`, `ge-layout-js.html`, `mt-maintpage-js.html`, `custompagewrapper-js.html`, `ge-editsdidetails-js.html`, `elnapis.html`, `concepts-actions.html`, and the full `actions/` directory.*
