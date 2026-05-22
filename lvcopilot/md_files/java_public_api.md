# LabVantage Java Public API Reference

## Table of Contents

1. [Overview](#1-overview)
2. [Package Structure](#2-package-structure)
3. [BaseAction — Class Reference](#3-baseaction--class-reference)
4. [BaseSDCRules — Class Reference](#4-basesdcrules--class-reference)
5. [BaseAttachmentHandler — Class Reference](#5-baseattachmenthandler--class-reference)
6. [AjaxResponse — Class Reference](#6-ajaxresponse--class-reference)
7. [PropertyList — Class Reference](#7-propertylist--class-reference)
8. [PropertyListCollection — Class Reference](#8-propertylistcollection--class-reference)
9. [DataSet — Class Reference](#9-dataset--class-reference)
10. [SDIData — Class Reference](#10-sdidata--class-reference)
11. [SDIDataStore — Class Reference](#11-sdidatastore--class-reference)
12. [SafeSQL — Class Reference](#12-safesql--class-reference)
13. [QueryProcessor — Class Reference](#13-queryprocessor--class-reference)
14. [DBAccess — Interface Reference](#14-dbaccess--interface-reference)
15. [ActionProcessor — Class Reference](#15-actionprocessor--class-reference)
16. [SDIProcessor — Class Reference](#16-sdiprocessor--class-reference)
17. [SDCProcessor — Class Reference](#17-sdcprocessor--class-reference)
18. [SequenceProcessor — Class Reference](#18-sequenceprocessor--class-reference)
19. [AttachmentProcessor — Class Reference](#19-attachmentprocessor--class-reference)
20. [ConfigurationProcessor — Class Reference](#20-configurationprocessor--class-reference)
21. [TranslationProcessor — Class Reference](#21-translationprocessor--class-reference)
22. [WorkflowProcessor — Class Reference](#22-workflowprocessor--class-reference)
23. [ConnectionInfo — Class Reference](#23-connectioninfo--class-reference)
24. [ErrorHandler — Class Reference](#24-errorhandler--class-reference)
25. [SapphireException — Class Reference](#25-sapphireexception--class-reference)
26. [ConnectionProcessor — Class Reference](#26-connectionprocessor--class-reference)
27. [DAMProcessor — Class Reference](#27-damprocessor--class-reference)
28. [HttpProcessor — Class Reference](#28-httpprocessor--class-reference)
29. [UtilityProcessor — Class Reference](#29-utilityprocessor--class-reference)
30. [Attachment — Class Reference](#30-attachment--class-reference)
31. [ActionBlock — Class Reference](#31-actionblock--class-reference)
32. [SDIRequest — Class Reference](#32-sdirequest--class-reference)
33. [SDIList — Class Reference](#33-sdilist--class-reference)
34. [ResultDataGrid — Class Reference](#34-resultdatagrid--class-reference)
35. [BaseCustom Inherited Accessors — Quick Reference](#35-basecustom-inherited-accessors--quick-reference)
36. [API Usage by Extension Type](#36-api-usage-by-extension-type)
37. [Common Patterns](#37-common-patterns)
38. [Other Helper Classes Reference by Package](#38-other-helper-classes-reference-by-package)

---

## 1. Overview

LabVantage 8.8 exposes a set of **Java Public APIs** in the `sapphire.*` package hierarchy. These APIs are the only supported, forward-compatible entry points for all custom Java development inside LabVantage. They are used across all four extension types:

| Extension Type | Base Class | Package |
|---|---|---|
| Custom Action | `BaseAction` | `sapphire.action` |
| SDC Business Rule | `BaseSDCRules` | `sapphire.action` |
| SDMS Attachment Handler | `BaseAttachmentHandler` | `sapphire.attachmenthandler` |
| AJAX Handler (JSP/Servlet) | `AjaxResponse` | `sapphire.servlet` |

All custom Java extension classes inherit `BaseCustom` (either directly via `BaseAction` / `BaseSDCRules` / `BaseAttachmentHandler`, or indirectly). `BaseCustom` provides accessor factory methods that return pre-initialised processor instances bound to the current session connection.

---

## 2. Package Structure

```
sapphire/
├── action/
│   ├── BaseAction                 ← extend for custom actions
│   ├── BaseSDCRules               ← extend for SDC business rules
│   ├── BaseAdvancedPullSample
│   ├── BaseAuthentication
│   ├── BasePasswordValidator
│   ├── BaseScheduleTask
│   └── BaseSpecRule
├── accessor/
│   ├── ActionProcessor            ← invoke actions programmatically
│   ├── AttachmentProcessor        ← add/get/edit/delete attachments
│   ├── ConfigurationProcessor     ← policy & profile properties
│   ├── HttpProcessor              ← HTTP tag manager access
│   ├── QueryProcessor             ← SQL query execution
│   ├── SDCProcessor               ← SDC metadata
│   ├── SDIProcessor               ← SDI record retrieval (SDIRequest/SDIData)
│   ├── SequenceProcessor          ← sequence numbers & UUIDs
│   ├── TranslationProcessor       ← multilingual translations
│   ├── UtilityProcessor
│   └── WorkflowProcessor          ← task queues & workflow data
├── attachmenthandler/
│   ├── BaseAttachmentHandler      ← extend for SDMS handlers
│   ├── AttachmentHandler          ← interface
│   ├── HandlerType                ← enum
│   ├── SDILink
│   └── SDILinks
├── attachment/
│   └── Attachment                 ← attachment record object
├── error/
│   └── ErrorHandler               ← error accumulator
├── servlet/
│   └── AjaxResponse               ← JSON response for AJAX JSP handlers
├── util/
│   ├── ActionBlock                ← programmatic action sequencer
│   ├── DataSet                    ← 2-D tabular result set
│   ├── DBAccess                   ← low-level DB interface (injected)
│   ├── SafeSQL                    ← parameterised SQL builder
│   ├── SDIData                    ← SDI record container (multi-dataset)
│   └── SDIDataStore               ← change-tracked SDI data wrapper
└── xml/
    ├── PropertyList               ← named property map (core I/O container)
    ├── PropertyListCollection     ← ArrayList of PropertyLists
    └── PropertyValue              ← annotated property wrapper
```

---

---

---

---

---

## 3. BaseAction — Class Reference

**Full name:** `sapphire.action.BaseAction`
**Declaration:** `public class BaseAction extends com.labvantage.sapphire.BaseCustom`

Deprecated.
As of version 5.0 replaced by Logger API

### 3.1 Protected Members

| Member | Type | Description |
|---|---|---|
| `database` | `DBAccess` | Pre-initialised low-level DB access |
| `connectionInfo` | `ConnectionInfo` | Current user session details |

### 3.2 Constants and Constants/Error Types

| Constant | Value (logical) | Use |
|---|---|---|
| `YES` | `"Y"` | Standard boolean flag |
| `NO` | `"N"` | Standard boolean flag |
| `NULL` | `""` | Empty/null sentinel |
| `POLL` | `"POLL"` | Async poll sentinel |
| `DEFAULT_SEPARATOR` | `";"` | Default list delimiter |
| `RETURN` | — | Generic return key |
| `RETURN_SUCCESS` | — | Success return value |
| `RETURN_FAILURE` | — | Failure return value |
| `VERSION` | — | Current LV version |
| `ESC_SEMICOLON` | — | Escaped semicolon literal |
| `TYPE_FAILURE` | `"failure"` | Hard failure error type |
| `TYPE_CONFIRM` | `"confirm"` | Confirmation prompt error type |
| `TYPE_VALIDATION` | `"validation"` | Validation error type |
| `TYPE_INFORMATION` | `"information"` | Informational message type |

Constants inherited from `BaseCustom`: `SUCCESS` (int 1), `FAILURE` (int 0).

### 3.3 Constructors

```java
BaseAction()
```

### 3.4 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|
| `protected String` | `createDatabaseTransactionLogId(PropertyList properties)` | — |
| `protected String` | `createDatabaseTransactionLogId(String sdcid, String keyid1, String keyid2, String keyid3, PropertyList properties)` | — |
| `void` | `endAction()` | — |
| `ErrorHandler` | `getErrorHandler()` | — |
| `boolean` | `isDatabaseRequired()` | — |
| `protected void` | `logError(String message)` | **[Deprecated]** As of version 5.0 replaced by Logger API. Deprecated. As of version 5.0 replaced by Logger API |
| `protected void` | `logTrace(String message)` | **[Deprecated]** As of version 5.0 replaced by Logger API. Deprecated. As of version 5.0 replaced by Logger API |
| `void` | `processAction(PropertyList properties)` | — |
| `int` | `processAction(String actionid, String actionversionid, HashMap properties)` | — |
| `protected int` | `setError(String message)` | — |
| `protected int` | `setError(String message, Exception e)` | — |
| `protected int` | `setError(String errorid, String message)` | — |
| `protected int` | `setError(String errorid, String message, Exception e)` | — |
| `int` | `setError(String errorid, String errorType, String message)` | — |
| `int` | `setError(String errorid, String errorType, String message, Throwable e)` | — |
| `void` | `setErrors(ErrorHandler errorHandler)` | — |
| `void` | `setInfoError(String message)` | — |
| `void` | `setInfoError(String errorid, String message)` | — |
| `void` | `startAction(String actionid, SapphireConnection sapphireConnection, ErrorHandler errorHandler, boolean nolog)` | — |

### 3.5 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 4. BaseSDCRules — Class Reference

**Full name:** `sapphire.action.BaseSDCRules`
**Declaration:** `public class BaseSDCRules extends com.labvantage.sapphire.BaseCustom`

Ancestor class for SDC Rules
Extend this class and give it the same name as the SDC it represents.
You will need to put all your business rule classes in the same package and define this package in the profile.

### 4.1 Protected Members

| Member | Type | Description |
|---|---|---|
| `database` | `DBAccess` | Pre-initialised low-level DB access |
| `connectionInfo` | `ConnectionInfo` | Current user session details |

### 4.2 Constants and Constants/Error Types

| Constant | Value (logical) | Use |
|---|---|---|
| `YES` | `"Y"` | Standard boolean flag |
| `NO` | `"N"` | Standard boolean flag |
| `NULL` | `""` | Empty/null sentinel |
| `POLL` | `"POLL"` | Async poll sentinel |
| `DEFAULT_SEPARATOR` | `";"` | Default list delimiter |
| `RETURN` | — | Generic return key |
| `RETURN_SUCCESS` | — | Success return value |
| `RETURN_FAILURE` | — | Failure return value |
| `VERSION` | — | Current LV version |
| `ESC_SEMICOLON` | — | Escaped semicolon literal |
| `TYPE_FAILURE` | `"failure"` | Hard failure error type |
| `TYPE_CONFIRM` | `"confirm"` | Confirmation prompt error type |
| `TYPE_VALIDATION` | `"validation"` | Validation error type |
| `TYPE_INFORMATION` | `"information"` | Informational message type |

Constants inherited from `BaseCustom`: `SUCCESS` (int 1), `FAILURE` (int 0).

### 4.3 Pre/Post Hook Methods

All hook methods follow the same signature pattern and `throws SapphireException`.

#### Primary Record Hooks (AddSDI / EditSDI / DeleteSDI)

| Method | Trigger | Receives |
|---|---|---|
| `preAdd(SDIData, PropertyList)` | Before AddSDI DB inserts | new data |
| `postAdd(SDIData, PropertyList)` | After AddSDI DB inserts, before commit | inserted data |
| `postAddKey(DataSet, PropertyList)` | Override key generation in AddSDI | primary dataset |
| `preEdit(SDIData, PropertyList)` | Before EditSDI DB updates | new data |
| `postEdit(SDIData, PropertyList)` | After EditSDI DB updates, before commit | updated data |
| `preDelete(String rsetid, PropertyList)` | Before DeleteSDI DB deletes | rset of records to delete |
| `postDelete(String rsetid, PropertyList)` | After DeleteSDI DB deletes, before commit | deleted rset |
| `preApprove(DataSet)` | Before approval action DB write | approve dataset |
| `postApprove(DataSet)` | After approval action DB write | approve dataset |

#### Detail Record Hooks

| Method | Trigger |
|---|---|
| `preAddDetail(SDIData, PropertyList)` | Before AddSDIDetail inserts |
| `postAddDetail(SDIData, PropertyList)` | After AddSDIDetail inserts |
| `preEditDetail(SDIData, PropertyList)` | Before EditSDIDetail updates |
| `postEditDetail(SDIData, PropertyList)` | After EditSDIDetail updates |
| `preDeleteDetail(String rsetid, PropertyList)` | Before DeleteSDIDetail deletes |
| `postDeleteDetail(String rsetid, PropertyList)` | After DeleteSDIDetail deletes |

#### Attribute Hooks

| Method | Trigger |
|---|---|
| `preAddAttribute(SDIData, PropertyList)` | Before AddSDIAttribute inserts |
| `postAddAttribute(SDIData, PropertyList)` | After AddSDIAttribute inserts |
| `preEditAttribute(SDIData, PropertyList)` | Before EditSDIAttribute updates |
| `postEditAttribute(SDIData, PropertyList)` | After EditSDIAttribute updates |
| `preDeleteAttribute(String rsetid, PropertyList)` | Before DeleteSDIAttribute deletes |
| `postDeleteAttribute(String rsetid, PropertyList)` | After DeleteSDIAttribute deletes |

#### Data Item / Dataset Hooks

| Method | Trigger |
|---|---|
| `preEditSDIDataItem(SDIData, PropertyList)` | Before EditDataItem updates |
| `postEditSDIDataItem(SDIData, PropertyList)` | After EditDataItem updates |
| `preEditSDIData(SDIData, PropertyList)` | Before EditDataSet updates |
| `postEditSDIData(SDIData, PropertyList)` | After EditDataSet updates |
| `preEditSDIDataApproval(SDIData, PropertyList)` | Before EditDataApproval updates |
| `postEditSDIDataApproval(SDIData, PropertyList)` | After EditDataApproval updates |
| `postDataEntry(SDIData, PropertyList)` | After EnterDataItem action |
| `preAddDataSet(SDIData, PropertyList)` | Before AddDataSet inserts |
| `postAddDataSet(SDIData, PropertyList)` | After AddDataSet inserts |
| `preReleaseData(DataSet, PropertyList)` | Before ReleaseDataItem / UnreleaseDataItem |
| `postReleaseData(DataSet, PropertyList)` | After ReleaseDataItem / UnreleaseDataItem |

#### Work Item Hooks

| Method | Trigger |
|---|---|
| `preAddWorkItem(SDIData, PropertyList)` | Before AddSDIWorkItem inserts |
| `postAddWorkItem(SDIData, PropertyList)` | After AddSDIWorkItem inserts |
| `preEditWorkItem(SDIData, PropertyList)` | Before EditSDIWorkitem updates |
| `postEditWorkItem(SDIData, PropertyList)` | After EditSDIWorkitem updates |

#### Note Hooks

| Method | Trigger |
|---|---|
| `preAddNote(SDIData, PropertyList)` | Before AddSDINote DB updates |
| `postAddNote(SDIData, PropertyList)` | After AddSDINote DB updates |
| `preEditNote(SDIData, PropertyList)` | Before EditSDINote DB updates |
| `postEditNote(SDIData, PropertyList)` | After EditSDINote DB updates |
| `postDeleteNote(... )` | After DeleteSDINote |

#### Attachment Hooks

| Method | Parameter | Trigger |
|---|---|---|
| `preAddSDIAttachment(Attachment)` | new attachment | Before attachment add |
| `postAddSDIAttachment(Attachment)` | added attachment | After attachment add |
| `preEditSDIAttachment(Attachment, Attachment preEdit)` | new + original | Before attachment edit |
| `postEditSDIAttachment(Attachment)` | updated attachment | After attachment edit |
| `preDeleteSDIAttachment(Attachment)` | attachment | Before attachment delete |
| `postDeleteSDIAttachment(Attachment)` | attachment | After attachment delete |
| `preGetSDIAttachment(Attachment)` | attachment | Before attachment retrieval |

#### CMT Import Hooks

| Method | Trigger |
|---|---|
| `preCMTImport(SDIData, PropertyList, boolean isAddSDI)` | Before AddSDI/EditSDI from CMT import |
| `postCMTImport(SDIData, PropertyList, boolean isAddSDI)` | After AddSDI/EditSDI from CMT import |

### 4.5 Utility Methods

```java
// Session context
ConnectionInfo getConnectionInfo()
String         getSdcid()
PropertyList   getSdcProps()

// Before-image access (only valid when requires*Image() returns true)
SDIData        getBeforeEditImage()

// Change detection (for post-edit hooks)
boolean hasPrimaryValueChanged(DataSet newPrimary, int row, String columnId)
String  getOldPrimaryValue(DataSet newPrimary, int row, String columnId)
Calendar getOldPrimaryCalendar(DataSet newPrimary, int row, String columnId)

boolean hasSDIAttributeValueChanged(DataSet newAttribute, int row, String columnId)
String  getOldSDIAttributeValue(DataSet newAttribute, int row, String columnId)

boolean hasSDIDataValueChanged(DataSet newSDIData, int row, String columnId)
String  getOldSDIDataValue(DataSet newSDIData, int row, String columnId)

boolean hasSDIWorkItemValueChanged(DataSet newSDIWI, int row, String columnId)
String  getOldSDIWorkItemValue(DataSet newSDIWI, int row, String columnId)

// CMT context
boolean isCMTImport()

// Error reporting
void setError(String ruleid, String errorType, String message)
void setWarning(String ruleid, String message)
void throwError(String ruleid, String errorType, String message)
    throws SapphireException  // throws immediately — stops the rule

boolean hasErrors()
void endRule() throws SapphireException
```

### 4.6 Registration

1. Class name must match SDC name exactly (`Sample` → `Sample.java`).
2. Register package in `ProfileProperty` table:
   - `ProfileId = System`
   - `PropertyId` starts with `customrulesjavapackage` (e.g., `customrulesjavapackage_myapp`)
   - `PropertyValue` = fully qualified package name (e.g., `sapphire.custom.myapp.rules`)
3. Optionally define execution order via `customrulessequence` or `customrulessequence_[SDC]` profile properties.

### 4.7 Constructors

```java
BaseSDCRules()
// Constructor.
BaseSDCRules(ConnectionInfo connectionInfo)
// Constructor.
```

### 4.8 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|
| `boolean` | `customRulesRequiresAddDataSetPrimary()` | Internal method. |
| `boolean` | `customRulesRequiresAddWorkItemPrimary()` | Internal method. |
| `boolean` | `customRulesRequiresBeforeDataEntryImage()` | Internal method. |
| `boolean` | `customRulesRequiresBeforeDataReleaseImage()` | Internal method. |
| `boolean` | `customRulesRequiresBeforeEditDetailImage()` | Internal method. |
| `boolean` | `customRulesRequiresBeforeEditImage()` | Internal method. |
| `boolean` | `customRulesRequiresBeforeEditSDIAttributeImage()` | Internal method. |
| `boolean` | `customRulesRequiresBeforeEditSDIDataImage()` | Internal method. |
| `boolean` | `customRulesRequiresBeforeEditWorkItemImage()` | Internal method. |
| `boolean` | `customRulesRequiresDataEntryPrimary()` | Internal method. |
| `boolean` | `customRulesRequiresDataReleasePrimary()` | Internal method. |
| `boolean` | `customRulesRequiresEditDetailPrimary()` | Internal method. |
| `boolean` | `customRulesRequiresEditSDIDataPrimary()` | Internal method. |
| `boolean` | `customRulesRequiresEditWorkItemPrimary()` | Internal method. |
| `void` | `endRule()` | Tidy-up. |
| `SDIData` | `getBeforeEditImage()` | Get a handle to the SDI data containing the primary dataset of the database image prior to the update |
| `ConnectionInfo` | `getConnectionInfo()` | Get details about the current connection. |
| `BaseSDCRules[]` | `getCustomRuleList()` | — |
| `String` | `getDatabaseid()` | Internal method. |
| `String` | `getEvent()` | Internal method. |
| `static BaseSDCRules` | `getInstance(SapphireConnection sapphireConnection, ErrorHandler errorHandler, String sdcid, PropertyList sdcProps, String event)` | Internal method. |
| `Calendar` | `getOldPrimaryCalendar(DataSet newPrimary, int primaryRow, String columnId)` | For a given row and column in the current primary dataset, return the corresponding Calendar value in the original data |
| `String` | `getOldPrimaryValue(DataSet newPrimary, int primaryRow, String columnId)` | For a given row and column in the current primary dataset, return the corresponding value in the original data |
| `String` | `getOldSDIAttributeValue(DataSet newAttribute, int row, String columnId)` | For a given row and column in the current dataset, return the corresponding value in the original data |
| `String` | `getOldSDIDataValue(DataSet newSDIData, int row, String columnId)` | For a given row and column in the current sdidata dataset, return the corresponding value in the original sdidata data |
| `String` | `getOldSDIWorkItemValue(DataSet newSDIWI, int row, String columnId)` | For a given row and column in the current sdiworkitem dataset, return the corresponding value in the original sdiworkitem data |
| `String` | `getSdcid()` | Utility method to get the SDC (also available from the actionProps paramater) |
| `PropertyList` | `getSdcProps()` | Utility method to get detailed information about the SDC. |
| `SDIView` | `getSDIView(SDIData sdiData)` | Override this method to add a view of the data |
| `boolean` | `hasErrors()` | Check for errors. |
| `boolean` | `hasPrimaryValueChanged(DataSet newPrimary, int primaryRow, String columnId)` | Utility method to determine whether a column value has changed as a result of the action processing. |
| `boolean` | `hasSDIAttributeValueChanged(DataSet newAttribute, int row, String columnId)` | Utility method to determine whether a column value has changed as a result of the action processing. |
| `boolean` | `hasSDIDataValueChanged(DataSet newSDIData, int row, String columnId)` | Utility method to determine whether a column value has changed as a result of the action processing. |
| `boolean` | `hasSDIWorkItemValueChanged(DataSet newSDIWI, int row, String columnId)` | Utility method to determine whether a column value has changed as a result of the action processing. |
| `boolean` | `isCMTImport()` | — |
| `protected void` | `logTrace(String message)` | **[Deprecated]** As of version 5.0 replaced by Logger API. Deprecated. As of version 5.0 replaced by Logger API |
| `void` | `postAdd(SDIData sdiData, PropertyList actionProps)` | Override this method to add business logic AFTER the database inserts in the AddSDI action |
| `void` | `postAddAttribute(SDIData sdiData, PropertyList actionProps)` | Override this method to add business logic AFTER the database inserts in the AddSDIAttribute action |
| `void` | `postAddDataSet(SDIData sdiData, PropertyList actionProps)` | Override this method to add business logic AFTER the database inserts in the AddDataSet action |
| `void` | `postAddDetail(SDIData sdiData, PropertyList actionProps)` | Override this method to add business logic AFTER the database inserts in the AddSDIDetail action |
| `void` | `postAddKey(DataSet primary, PropertyList actionProps)` | Override this method to add business logic to override or create keyids in the AddSDI action |
| `void` | `postAddNote(SDIData sdiData, PropertyList actionProps)` | Override this method to add business logic AFTER the database updates in the AddSDINote action |
| `void` | `postAddSDIAttachment(Attachment attachment)` | Override this method to add business logic AFTER the database updates in the Attachment service |
| `void` | `postAddWorkitem(SDIData sdiData, PropertyList actionProps)` | **[Deprecated]** Use postAddWorkItem (note the camelcase on WorkItem). Deprecated. Use postAddWorkItem (note the camelcase on WorkItem) |
| `void` | `postAddWorkItem(SDIData sdiData, PropertyList actionProps)` | Override this method to add business logic AFTER the database updates in the EditSDIWorkitem action |
| `void` | `postApprove(DataSet approve)` | Override this method to add business logic AFTER the database performs approval action |
| `void` | `postCMTImport(SDIData sdiData, PropertyList actionProps, boolean isAddSDI)` | Override this method to add business logic AFTER the database inserts or updates in the AddSDI or EditSDI actions from CMT SDI import |
| `void` | `postDataEntry(SDIData sdiData, PropertyList actionProps)` | Override this method to add business logic AFTER the database updates in the EnterDataItem action |
| `void` | `postDelete(String rsetid, PropertyList actionProps)` | Override this method to add business logic AFTER the database deletes in the DeleteSDI action |
| `void` | `postDeleteAttribute(String rsetid, PropertyList actionProps)` | Override this method to add business logic AFTER the database deletes in the DeleteSDIAttribute action |
| `void` | `postDeleteDetail(String rsetid, PropertyList actionProps)` | Override this method to add business logic AFTER the database deletes in the DeleteSDIDetail action |
| `void` | `postDeleteSDIAttachment(Attachment attachment)` | Override this method to add business logic AFTER the database updates in the Attachment service |
| `void` | `postEdit(SDIData sdiData, PropertyList actionProps)` | Override this method to add business logic AFTER the database updates in the EditSDI action |
| `void` | `postEditAttribute(SDIData sdiData, PropertyList actionProps)` | Override this method to add business logic AFTER the database updates in the EditSDIAttribute action |
| `void` | `postEditDetail(SDIData sdiData, PropertyList actionProps)` | Override this method to add business logic AFTER the database updates in the EditSDIDetail action |
| `void` | `postEditNote(SDIData sdiData, PropertyList actionProps)` | Override this method to add business logic AFTER the database updates in the EditSDINote action |
| `void` | `postEditSDIAttachment(Attachment attachment)` | Override this method to add business logic AFTER the database updates in the Attachment service |
| `void` | `postEditSDIData(SDIData sdiData, PropertyList actionProps)` | Override this method to add business logic AFTER the database updates in the EditDataSet action |
| `void` | `postEditSDIDataApproval(SDIData sdiData, PropertyList actionProps)` | Override this method to add business logic AFTER the database updates in the EditDataApproval action |
| `void` | `postEditSDIDataItem(SDIData sdiData, PropertyList actionProps)` | Override this method to add business logic AFTER the database updates in the EditDataItem action |
| `void` | `postEditWorkitem(SDIData sdiData, PropertyList actionProps)` | **[Deprecated]** Use postEditWorkItem (note the camelcase on WorkItem). Deprecated. Use postEditWorkItem (note the camelcase on WorkItem) |
| `void` | `postEditWorkItem(SDIData sdiData, PropertyList actionProps)` | Override this method to add business logic AFTER the database updates in the EditSDIWorkitem action |
| `void` | `postGenerateSnapshot(Snapshot snapshot, boolean isPackaging)` | Override this method to add business logic AFTER the Snapshot is generated for a SDI. |
| `void` | `postReleaseData(DataSet releaseData, PropertyList actionProps)` | Override this method to add business logic AFTER the database updates in the ReleaseDataItem and UnreleaseDataItem action |
| `void` | `preAdd(SDIData sdiData, PropertyList actionProps)` | Override this method to add business logic BEFORE the database inserts in the AddSDI action |
| `void` | `preAddAttribute(SDIData sdiData, PropertyList actionProps)` | Override this method to add business logic BEFORE the database inserts in the AddSDIAttribute action |
| `void` | `preAddDataSet(SDIData sdiData, PropertyList actionProps)` | Override this method to add business logic BEFORE the database inserts in the AddDataSet action |
| `void` | `preAddDetail(SDIData sdiData, PropertyList actionProps)` | Override this method to add business logic BEFORE the database inserts in the AddSDIDetail action |
| `void` | `preAddKey(DataSet primary, PropertyList actionProps)` | — |
| `void` | `preAddNote(SDIData sdiData, PropertyList actionProps)` | Override this method to add business logic BEFORE the database updates in the AddSDINote action |
| `void` | `preAddSDIAttachment(Attachment attachment)` | Override this method to add business logic BEFORE the database updates in the Attachment service |
| `void` | `preAddWorkItem(SDIData sdiData, PropertyList actionProps)` | Override this method to add business logic BEFORE the database inserts in the AddSDIWorkItem action |
| `void` | `preApprove(DataSet approve)` | Override this method to add business logic BEFORE the database performs approval action |
| `void` | `preCMTImport(SDIData sdiData, PropertyList actionProps, boolean isAddSDI)` | Override this method to add business logic BEFORE the database inserts or updates in the AddSDI or EditSDI actions from CMT SDI import |
| `void` | `preDelete(String rsetid, PropertyList actionProps)` | Override this method to add business logic BEFORE the database deletes in the DeleteSDI action |
| `void` | `preDeleteAttribute(String rsetid, PropertyList actionProps)` | Override this method to add business logic BEFORE the database deletes in the DeleteSDIAttribute action |
| `void` | `preDeleteDetail(String rsetid, PropertyList actionProps)` | Override this method to add business logic BEFORE the database deletes in the DeleteSDIDetail action |
| `void` | `preDeleteSDIAttachment(Attachment attachment)` | Override this method to add business logic BEFORE the database updates in the Attachment service |
| `void` | `preEdit(SDIData sdiData, PropertyList actionProps)` | Override this method to add business logic BEFORE the database updates in the EditSDI action |
| `void` | `preEditAttribute(SDIData sdiData, PropertyList actionProps)` | Override this method to add business logic BEFORE the database updates in the EditSDIAttribute action |
| `void` | `preEditDetail(SDIData sdiData, PropertyList actionProps)` | Override this method to add business logic BEFORE the database updates in the EditSDIDetail action |
| `void` | `preEditNote(SDIData sdiData, PropertyList actionProps)` | Override this method to add business logic BEFORE the database updates in the EditSDINote action |
| `void` | `preEditSDIAttachment(Attachment attachment, Attachment preEditAttachment)` | Override this method to add business logic BEFORE the database updates in the Attachment service |
| `void` | `preEditSDIData(SDIData sdiData, PropertyList actionProps)` | Override this method to add business logic BEFORE the database updates in the EditDataSet action |
| `void` | `preEditSDIDataApproval(SDIData sdiData, PropertyList actionProps)` | Override this method to add business logic BEFORE the database updates in the EditDataApproval action |
| `void` | `preEditSDIDataItem(SDIData sdiData, PropertyList actionProps)` | Override this method to add business logic BEFORE the database updates in the EditDataItem action |
| `void` | `preEditWorkitem(SDIData sdiData, PropertyList actionProps)` | **[Deprecated]** Use preEditWorkItem (note the camelcase on WorkItem). Deprecated. Use preEditWorkItem (note the camelcase on WorkItem) |
| `void` | `preEditWorkItem(SDIData sdiData, PropertyList actionProps)` | Override this method to add business logic BEFORE the database updates in the EditSDIWorkitem action |
| `void` | `preGetSDIAttachment(Attachment attachment)` | Override this method to add business logic BEFORE the attchment is returned from Attachment repository |
| `void` | `preReleaseData(DataSet releaseData, PropertyList actionProps)` | Override this method to add business logic AFTER the database updates in the ReleaseDataItem and UnreleaseDataItem action |
| `boolean` | `requiresAddDataSetPrimary()` | Override this method for AddDataSet implementations to return 'true'. |
| `boolean` | `requiresAddWorkItemPrimary()` | Override this method for AddSDIWorkItem implementations to return 'true'. |
| `boolean` | `requiresBeforeDataEntryImage()` | Override this method for DataEntry implementations (postDataEntry) to return 'true'. |
| `boolean` | `requiresBeforeDataReleaseImage()` | Override this method for DataRelease implementations (preReleaseData, postReleaseData) to return 'true'. |
| `boolean` | `requiresBeforeEditDetailImage()` | Override this method for EditSDIDetail implementations (preEditDetail, postEditDetail) to return 'true'. |
| `boolean` | `requiresBeforeEditImage()` | Override this method for EditSDI implementations (preEdit, postEdit) to return 'true'. |
| `boolean` | `requiresBeforeEditSDIAttributeImage()` | Override this method for SDIAttribute implementations (preEditSDIAttribute etc.) to return 'true'. |
| `boolean` | `requiresBeforeEditSDIDataImage()` | Override this method for SDIDataItem, SDIData and SDDataApproval implementations (preEditSDIDataItem, preEditSDIData, preEditSDIDataApproval etc.) to return 'true'. |
| `boolean` | `requiresBeforeEditWorkItemImage()` | Override this method for SDIWorkItem implementations (preEditSDIWorkItem) to return 'true'. |
| `boolean` | `requiresDataEntryPrimary()` | Override this method for DataEntry implementations (preDataEntry, postDataEntry) to return 'true'. |
| `boolean` | `requiresDataReleasePrimary()` | Override this method for DataRelease implementations (preReleaseData, postReleaseData) to return 'true'. |
| `boolean` | `requiresEditDetailPrimary()` | Override this method for EditSDIDetail implementations (preEditDetail, postEditDetail) to return 'true'. |
| `boolean` | `requiresEditSDIDataPrimary()` | Override this method for SDIDataItem, SDIData and SDDataApproval implementations (preEditSDIDataItem, preEditSDIData, preEditSDIDataApproval etc.) to return 'true'. |
| `boolean` | `requiresEditWorkItemPrimary()` | Override this method for EditSDIWorkItem implementations to return 'true'. |
| `void` | `setBeforeEditImage(SDIData beforeEditImage)` | Internal method. |
| `void` | `setCMTImport(boolean CMTImport)` | Internal method. |
| `void` | `setConnectionInfo(ConnectionInfo connectionInfo)` | Internal method. |
| `void` | `setCustomRules(BaseSDCRules[] customRules)` | Internal method. |
| `void` | `setError(String ruleid, String errorType, String message)` | This method will put an error record in the action error handler. |
| `void` | `setErrors(ErrorHandler errorHandler)` | Internal method. |
| `void` | `setEvent(String event)` | Internal method. |
| `void` | `setSdcid(String sdcid)` | Internal method. |
| `void` | `setSDCProps(PropertyList sdc)` | Internal method. |
| `void` | `setWarning(String ruleid, String message)` | This method will put a Warning or information record in the action error handler. |
| `void` | `throwError(String ruleid, String errorType, String message)` | This method will put an error record in the action error handler and then throw a sapphire exception to leave the rule and force the action to fail. |

### 4.9 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 5. BaseAttachmentHandler — Class Reference

**Full name:** `sapphire.attachmenthandler.BaseAttachmentHandler`
**Declaration:** `public class BaseAttachmentHandler extends com.labvantage.sapphire.modules.sdms.handlers.BaseAttachmentHandler implements AttachmentHandler`

Extend this class for your Attachment Handler implementation and implement the handleData method.

### 5.1 Protected Members

| Member | Type | Description |
|---|---|---|
| `database` | `DBAccess` | Pre-initialised low-level DB access |
| `connectionInfo` | `ConnectionInfo` | Current user session details |

### 5.3 Constructors

```java
BaseAttachmentHandler()
```

### 5.4 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|
| `void` | `addFile(String filepath, String aliasFileName, String attachmentClass)` | Add a file to the return of the attachment handler The result will be this file will be come an additional attachment on the primary. |
| `void` | `addFileMetaData(PropertyList propertyList, Attachment attachment)` | Will add one or many attributes to the provided attachment record. |
| `void` | `addLinkSDI(String sdcid, String keyid1, String keyid2, String keyid3)` | Add an sdi link which is a link from the primary executing the attachment handler to the sdi provided (e.g. |
| `void` | `addMetaData(String name, String value)` | Will add an attribute to the primary record running the attachment handler (e.g. |
| `String` | `getAtachmentHandlerId()` | Returns the attachment handler keyid1 |
| `PropertyList` | `getFileMetaData(Attachment attachment)` | Returns the attributes for the provided attachment object |
| `String` | `getHandlerId()` | Returns the handler id |
| `HandlerType` | `getHandlerType()` | Returns the HandlerType value of the enumeration |
| `String` | `getHelperURL()` | Returns the helper GUI The helper URL will be opened in a dialog and must return JSON of set up properties. |
| `String` | `getKeyId1()` | Returns the keyid1 for the primary that the attachment handler is running on |
| `String` | `getKeyId2()` | Returns the keyid2 for the primary that the attachment handler is running on |
| `String` | `getKeyId3()` | Returns the keyid3 for the primary that the attachment handler is running on |
| `SDILinks` | `getLinkSDI()` | Returns the collection of sdi links |
| `PropertyList` | `getMetaData()` | Returns all the attributes for the primary (e.g. |
| `ResultDataGrid` | `getResultGrid()` | Will return the result grid from the collection of result grids |
| `ResultDataGrid` | `getResultGrid(int resultgridindex)` | Will return a result grid from the collection of result grids |
| `int` | `getResultResultGridCount()` | Will return the number of result grids added into this attachment handler |
| `String` | `getSDCId()` | Returns the sdcid for the primary that the attachment handler is running on (e.g. |
| `abstract void` | `handleData(List<Attachment> attachments, PropertyList properties)` | Override this method to provide the entry point for your handler. |
| `boolean` | `isDatabaseRequired()` | Returns true if the database object is required. |
| `boolean` | `isDebugMode()` | Returns true if the handler is run in debug mode from a test button Debug mode will output higher level of logging |
| `void` | `logMessage(String message)` | Will add a message to the log which will end up being stored in the operation execution. |
| `void` | `setActionBlock(ActionBlock actionBlock)` | Set the action block of the attachment handler |
| `void` | `setResultGrid(ResultDataGrid resultGrid)` | Add a result grid object into the result grid collection. |

### 5.5 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 6. AjaxResponse — Class Reference

**Full name:** `sapphire.servlet.AjaxResponse`
**Declaration:** `public class AjaxResponse extends java.lang.Object`

Ajax response in a JSON format

### 6.3 Constructors

```java
AjaxResponse(HttpServletRequest request,
HttpServletResponse response)
AjaxResponse(HttpServletRequest request,
HttpServletResponse response,
String callback)
```

### 6.4 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|
| `void` | `addCallbackArgument(String name, boolean data)` | — |
| `void` | `addCallbackArgument(String name, Collection data)` | — |
| `void` | `addCallbackArgument(String name, DataSet dataset)` | — |
| `void` | `addCallbackArgument(String name, double data)` | — |
| `void` | `addCallbackArgument(String name, int data)` | — |
| `void` | `addCallbackArgument(String name, long data)` | — |
| `void` | `addCallbackArgument(String name, Map data)` | — |
| `void` | `addCallbackArgument(String name, Object data)` | — |
| `Browser` | `getBrowser()` | — |
| `JSONObject` | `getCallProperties()` | — |
| `PageContext` | `getPageContext(Servlet servlet, ServletContext servletContext, HttpServletRequest request, HttpServletResponse response)` | — |
| `DataSet` | `getRegisteredSQLDataSet(QueryProcessor queryProcessor, Map params)` | — |
| `String` | `getRequestParameter(String propertyid)` | — |
| `String` | `getRequestParameter(String propertyid, String defaultValue)` | — |
| `Map` | `getRequestParameters()` | — |
| `static void` | `handleException(HttpServletRequest request, HttpServletResponse response, Throwable t)` | — |
| `boolean` | `isCallbackSet()` | — |
| `void` | `print()` | — |
| `void` | `setCallback(String callback)` | — |
| `void` | `setCallProperties(Object properties)` | — |
| `void` | `setError(String error)` | — |
| `void` | `setError(String error, Throwable t)` | — |
| `void` | `setErrorCallback(String errorcallback)` | — |
| `String` | `toString()` | — |

### 6.5 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 7. PropertyList — Class Reference

**Full name:** `sapphire.xml.PropertyList`
**Declaration:** `public class PropertyList extends java.util.HashMap implements java.lang.Cloneable, java.lang.Comparable, com.labvantage.sapphire.gwt.shared.JSONable`

Creates a new PropertyList from a HashMap

### 7.2 Public Fields and Constants

| Name | Type | Description |
|---|---|---|
| `ALL_MODULES` | `static String` | — |
| `ALL_ROLES` | `static String` | — |
| `attributes` | `protected HashMap` | — |
| `ENCRYPT_PREFIX` | `static String` | — |
| `id` | `protected String` | — |
| `JSON_PROPERTYLISTATTRIBUTES` | `static String` | — |
| `JSON_PROPERTYLISTID` | `static String` | — |
| `JSON_PROPERTYLISTSEQUENCE` | `static String` | — |
| `usePropertyValues` | `protected boolean` | — |

### 7.3 Constructors

```java
PropertyList()
PropertyList(HashMap mapProps)
// Creates a new PropertyList from a HashMap
PropertyList(JsonObject jsonObject)
// Creates a new propertylist from a JSONObhect (created with toJSONObject)
PropertyList(JSONObject jsonObject)
// Creates a new propertylist from a JSONObhect (created with toJSONObject)
PropertyList(PropertyList propertyList)
// Creates a new PropertyList from a (core) PropertyList
PropertyList(String id)
```

### 7.4 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|
| `void` | `addPropertyList(Node propertyList, boolean merge, String propertyTreeNodeId)` | — |
| `void` | `addPropertyList(PropertyList propertyList)` | — |
| `int` | `compareTo(Object o)` | — |
| `PropertyList` | `copy()` | — |
| `PropertyList` | `copy(String rolelist, String modulelist)` | — |
| `PropertyList` | `copy(String rolelist, String modulelist, Set<String> inactiveRoles)` | — |
| `PropertyList` | `copy(String languageid, TranslationProcessor tp)` | — |
| `PropertyList` | `copy(String languageid, TranslationProcessor tp, String rolelist, String modulelist, Set<String> inactiveRoles)` | — |
| `PropertyList` | `copy(String languageid, TranslationProcessor tp, String rolelist, String modulelist, Set<String> inactiveRoles, String guimode)` | — |
| `PropertyList` | `copy(String languageid, TranslationProcessor tp, String rolelist, String modulelist, Set<String> inactiveRoles, String guimode, boolean forceTranslation)` | — |
| `void` | `deleteProperty(String propertyid)` | — |
| `boolean` | `equals(PropertyList compare)` | — |
| `String` | `findProperty(String findId)` | — |
| `PropertyList` | `findPropertyList(String findId)` | — |
| `String` | `getAttribute(String attributeId)` | — |
| `HashMap` | `getAttributes()` | — |
| `protected String` | `getAttributesText(HashMap attribs, String ignorelist)` | — |
| `PropertyListCollection` | `getCollection(String propertyId)` | Returns the propertylistcollection for the specified property. |
| `PropertyListCollection` | `getCollectionNotNull(String propertyId)` | Returns the propertylistcollection for the specified property. |
| `String` | `getDecryptedProperty(String propertyId)` | — |
| `ArrayList` | `getFilteredCollections(String propertyId, HashMap filterMap)` | — |
| `String` | `getId()` | Returns the PropertyList identifier |
| `String` | `getModuleListNodeid()` | — |
| `PropertyValue` | `getParentPropertyValue()` | Returns the PropertyValue for the property that this propertylist resides in. |
| `String` | `getPreviousModuleList()` | — |
| `String` | `getPreviousRoleList()` | — |
| `String` | `getPreviousSequence()` | — |
| `String` | `getProperty(String propertyId)` | — |
| `String` | `getProperty(String propertyId, String defaultValue)` | — |
| `PropertyList` | `getPropertyList(String propertyId)` | Returns the propertylist for the specified property. |
| `PropertyList` | `getPropertyListNotNull(String propertyId)` | Returns the propertylist for the specified property. |
| `String` | `getPropertyTreeNodeId()` | Returns the treenodeid when the propertylist has been populated from a propertytree |
| `String` | `getPropertyTreeNodeId(String propertyId)` | — |
| `PropertyValue` | `getPropertyValue(String propertyId)` | — |
| `String` | `getRoleListNodeid()` | — |
| `long` | `getSequence()` | — |
| `boolean` | `isCollection(String propertyId)` | — |
| `boolean` | `isPropertyList(String propertyId)` | — |
| `boolean` | `isSimple(String propertyId)` | — |
| `void` | `mergeAttributes(HashMap attributes)` | — |
| `void` | `mergeAttributes(HashMap attributes, String propertyTreeNodeid)` | — |
| `protected void` | `propertyToXML(StringBuffer xml, String propertyid, Object value, PropertyValue pv, String roleList, String moduleList, int level)` | — |
| `void` | `setAttribute(String id, String value)` | Sets a single attribute on this propertylist |
| `void` | `setAttribute(String id, String value, boolean deep)` | Sets a single attribute on this propertylist with the option of cascading |
| `void` | `setAttributes(HashMap attributes)` | Sets the attributes for the PropertyList |
| `void` | `setDatabaseid(String databaseid)` | Sets the databaseid |
| `void` | `setDbms(String dbms)` | Sets the database type |
| `void` | `setGuiMode(String guiMode)` | Sets the guimode value to return when encountering a guimode-based property like $R{high:xxx:|:medium:yyy:|:low:zzz} |
| `void` | `setId(String id)` | Set the PropertyList identifier |
| `void` | `setJSONString(String jsonString)` | — |
| `void` | `setJSONString(String jsonString, boolean blankIsPropertyList)` | — |
| `void` | `setLanguage(String language)` | Sets the language when getting translated toXMLString |
| `void` | `setLocale(Locale locale)` | — |
| `void` | `setProperty(String propertyId, PropertyList value)` | — |
| `void` | `setProperty(String propertyId, PropertyListCollection value)` | — |
| `void` | `setProperty(String propertyId, String value)` | — |
| `void` | `setPropertyList(File file)` | — |
| `void` | `setPropertyList(File file, boolean merge)` | — |
| `void` | `setPropertyList(File file, boolean merge, boolean cache)` | — |
| `void` | `setPropertyList(String xml)` | — |
| `void` | `setPropertyList(String xml, boolean merge)` | — |
| `void` | `setPropertyList(String xml, boolean merge, boolean cache)` | — |
| `void` | `setPropertyList(String xml, boolean merge, String propertyTreeNodeid, boolean cache)` | — |
| `void` | `setPropertyTree(File file, String nodeId)` | Deprecated. |
| `void` | `setPropertyTree(File file, String nodeId, boolean setDefaults)` | Deprecated. |
| `void` | `setPropertyTree(File file, String nodeId, boolean setDefaults, PropertyDefinitionList propertyDefinitionList)` | — |
| `void` | `setPropertyTree(File file, String nodeId, PropertyDefinitionList propertyDefinitionList)` | — |
| `void` | `setPropertyTree(Node contextNode, String nodeId, boolean setDefaults)` | Deprecated. |
| `void` | `setPropertyTree(Node contextNode, String nodeId, boolean setDefaults, PropertyDefinitionList propertyDefinitionList)` | — |
| `void` | `setPropertyTree(String xml, String nodeId)` | Deprecated. |
| `void` | `setPropertyTree(String xml, String nodeId, boolean setDefaults)` | Deprecated. |
| `void` | `setPropertyTree(String xml, String nodeId, boolean setDefaults, boolean cache)` | Deprecated. |
| `void` | `setPropertyTree(String xml, String nodeId, boolean setDefaults, boolean cache, PropertyDefinitionList propertyDefinitionList)` | — |
| `void` | `setPropertyTree(String xml, String nodeId, boolean setDefaults, PropertyDefinitionList propertyDefinitionList)` | — |
| `void` | `setPropertyTree(String xml, String nodeId, PropertyDefinitionList propertyDefinitionList)` | — |
| `boolean` | `setPropertyTreeDefaults(Node propertyListDef)` | Deprecated. |
| `boolean` | `setPropertyTreeDefaults(Node propertyListDef, PropertyDefinitionList propertyDefinitionList)` | — |
| `boolean` | `setPropertyTreeDefaults(PropertyDefaultList propertyDefaultList)` | Deprecated. |
| `boolean` | `setPropertyTreeDefaults(PropertyDefaultList propertyDefaultList, PropertyDefinitionList propertyDefinitionList)` | — |
| `void` | `setPropertyTreeDefaults(String propertyTree)` | Deprecated. |
| `void` | `setPropertyTreeDefaults(String propertyTree, PropertyDefinitionList propertyDefinitionList)` | Sets the defaults into the PropertyList. |
| `void` | `setSequence(long seq)` | — |
| `void` | `setSequence(String seq)` | — |
| `void` | `setTimeZone(TimeZone timezone)` | — |
| `void` | `setTranslationProcessor(TranslationProcessor tp)` | Translation processor to be used when translating in toXMLString() |
| `void` | `setUsePropertyValues(boolean usePropertyValues)` | States whether to use PropertyValues. |
| `Element` | `toElement(Document doc)` | — |
| `JSONObject` | `toJSONObject()` | — |
| `JSONObject` | `toJSONObject(boolean includeAttributes)` | — |
| `JSONObject` | `toJSONObject(boolean includeAttributes, boolean includeEmpties)` | — |
| `String` | `toJSONString()` | — |
| `String` | `toJSONString(boolean includeAttributes)` | — |
| `String` | `toJSONString(boolean includeAttributes, boolean includeEmpties)` | — |
| `JsonObject` | `toSimpleJSON()` | — |
| `JsonObject` | `toSimpleJSON(PropertyDefinitionList definition)` | — |
| `JsonObject` | `toSimpleJSON(PropertyDefinitionList definition, HashMap<String,String> breakpoints)` | — |
| `String` | `toString()` | — |
| `String` | `toXMLString()` | — |
| `String` | `toXMLString(int level)` | — |
| `String` | `toXMLString(String roleList, String moduleList)` | — |

### 7.5 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 8. PropertyListCollection — Class Reference

**Full name:** `sapphire.xml.PropertyListCollection`
**Declaration:** `public class PropertyListCollection extends java.util.ArrayList implements com.labvantage.sapphire.gwt.shared.JSONable`

### 8.3 Constructors

```java
PropertyListCollection()
```

### 8.4 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|
| `PropertyList` | `find(String propertyid, String value)` | — |
| `PropertyList` | `find(String propertyid, String value, boolean caseInsensitive)` | — |
| `String` | `getAttribute(String attributeId)` | — |
| `HashMap` | `getAttributes()` | — |
| `String` | `getFieldName()` | — |
| `String` | `getId()` | — |
| `int` | `getIndex(String value)` | — |
| `PropertyList` | `getIndexedPropertyList(String value)` | — |
| `PropertyList` | `getPropertyList(int index)` | — |
| `PropertyList` | `getPropertyList(String id)` | — |
| `String` | `getPropertyTreeNodeId()` | — |
| `String` | `getUniqueId()` | — |
| `void` | `index(String propertyid)` | — |
| `void` | `index(String[] propertyid, String separator)` | — |
| `void` | `setAttributes(HashMap attributes)` | — |
| `void` | `setFieldName(String fieldName)` | — |
| `void` | `setId(String id)` | — |
| `void` | `setJSONArray(JSONArray jsonArray)` | — |
| `void` | `setJSONString(String jsonString)` | — |
| `void` | `setPropertyTreeNodeId(String propertyTreeNodeId)` | — |
| `JSONArray` | `toJSONArray()` | — |
| `JSONArray` | `toJSONArray(boolean includeAttributes, boolean includeEmpties)` | — |
| `String` | `toJSONString()` | — |
| `String` | `toJSONString(boolean includeAttributes, boolean includeEmpties)` | — |

### 8.5 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 9. DataSet — Class Reference

**Full name:** `sapphire.util.DataSet`
**Declaration:** `public class DataSet extends java.util.ArrayList implements java.io.Serializable, com.labvantage.sapphire.gwt.shared.JSONable`

This utility class is designed to hold a 2-dimensional "table" of data, and provide methods to manage and manipulate that data.
The DataSet is typically populated by one of two mechanisms:
1) Passing a JDBC java.sql.ResultSet class to the constructor or setResultSet() method.
2) Manually adding columns, calling addRow() and then the "setter" methods e.g. setString()
Data can be extracted out of the class using a variety of "getter" methods such as getString(), getCalendar() and getObject().
"Sort", "filter" and "find" methods can be used to manipulate and navigate the data.

### 9.2 Public Fields and Constants

| Name | Type | Description |
|---|---|---|
| `CLOB` | `static int` | — |
| `type.` | `static int` | Constant indicating an Date column/data type. |
| `FAILURE` | `static int` | — |
| `NULLNUMBER` | `static int` | — |
| `type.` | `static int` | Constant indicating a Numeric column/data type. |
| `type.` | `static int` | Constant indicating a String column/data type. |
| `SUCCESS` | `static int` | — |
| `type.` | `static int` | Constant indicating an unknown column/data type. |

### 9.3 Constructors

```java
DataSet()
// Constructs a new empty DataSet.
DataSet(ConnectionInfo connectionInfo)
// Constructs a new empty DataSet with Locale and TimeZone set according to ConnectionInfo.
DataSet(JSONObject job)
DataSet(Locale locale,
TimeZone timezone)
// Deprecated.
Use the DataSet( ConnectionInfo ) constructor instead
DataSet(ResultSet rs)
// Constructs a new DataSet based on a JDBC resultset.
DataSet(ResultSet rs,
ConnectionInfo connectionInfo)
// Constructs a new DataSet based on a JDBC resultset.
DataSet(ResultSet rs,
Locale locale,
TimeZone timezone)
// Deprecated.
Use the DataSet( ResultSet, ConnectionInfo ) constructor instead
DataSet(String xml)
// Constructs a new DataSet based on a XML string.
DataSet(String xml,
ConnectionInfo connectionInfo)
// Constructs a new DataSet based on a XML string.
DataSet(String xml,
Locale locale,
TimeZone timezone)
// Deprecated.
Use the DataSet( xml, ConnectionInfo ) constructor instead
DataSet(String columnnames,
String rowdata)
// Constructs a new DataSet based on two strings, one containing column names (semi-colon delimted) and
one containing the data (pipe and semicolon delimited).
```

### 9.4 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|
| `boolean` | `addColumn(String columnid, int type)` | Adds a blank column. |
| `boolean` | `addColumn(String columnid, int type, int length)` | Adds a blank column. |
| `boolean` | `addColumnValues(String columnid, int type, String valueslist, String delimeter)` | Used to simultaneously add a column and a list of values. |
| `boolean` | `addColumnValues(String columnid, int type, String valueslist, String delimeter, String defaultvalue)` | Used to simultaneously add a column and a list of values. |
| `int` | `addRow()` | Adds a blank row to the end of the dataset. |
| `int` | `addRow(int newrow)` | Adds a blank row before the specifiedrow in the dataset. |
| `DataSet` | `copy()` | Return a copy of the dataset object with no references to this object. |
| `DataSet` | `copy(String[] columns, boolean includeClobs)` | Return a copy of the dataset object with no references to this object. |
| `void` | `copyRow(DataSet from, int copyrow, int copies)` | Copies a row in a different dataset 'n' times. |
| `void` | `copyRow(int copyrow, int copies)` | Copies a row in the dataset 'n' times. |
| `void` | `deleteRow(int row)` | Deletes a specific row. |
| `int` | `findRow(HashMap findmap)` | Returns the row number of the first row found matching a specified find HashMap. |
| `int` | `findRow(HashMap findmap, int start)` | Returns the row number of the first row found matching a specified find HashMap. |
| `int` | `findRow(String find)` | **[Deprecated]** This method fails to find the row if any column value contains comma, hence it is recommended to use findRow(HashMap findMap) for multiple column-value pairs and for single column-value pair use findRow(String, String).. Deprecated. This method fails to find the row if any column value contains comma, hence it is recommended to use findRow(HashMap findMap) for multiple column-value pairs and for single column-value pair use findRow(String, String). |
| `int` | `findRow(String columnId, String value)` | Returns the row number of the first row found matching with the value specified with the column. |
| `BigDecimal` | `getBigDecimal(int row, String columnid)` | Get a BigDecimal value from a Numeric column. |
| `BigDecimal` | `getBigDecimal(int row, String columnid, BigDecimal defaultvalue)` | Get a BigDecimal value from a Numeric column with a default value if the BigDecimal is null. |
| `Calendar` | `getCalendar(int row, String columnid)` | Get a Calendar object from a Date column. |
| `Calendar` | `getCalendar(int row, String columnid, Calendar defaultvalue)` | Get a Calendar object from a Date column with a default value if the value is null. |
| `String` | `getCdataEscape()` | get the CDATA escape sequence that was used when calling toXML() on the DataSet |
| `String` | `getClob(int row, String columnid)` | Get a string value from a Clob column. |
| `String` | `getClob(int row, String columnid, String defaultvalue)` | Get a string value from a Clob column with a default value if the string is null (or empty). |
| `boolean` | `getColidCaseSensitive()` | Return whether the dataset object treats columnid as case sensitive. |
| `int` | `getColumnCount()` | Returns the number of columns. |
| `String` | `getColumnId(int col)` | Returns the Id (Name) of the specified column number. |
| `int` | `getColumnLength(String columnid)` | Returns the column length if DataSet.STRING type and from a java.sql.ResultSet. |
| `String[]` | `getColumns()` | Returns the list of columns. |
| `int` | `getColumnType(String columnid)` | Returns the column data type. |
| `String` | `getColumnValues(String columnid, int startrow, int endrow, String delimeter)` | Returns concatenated list of column values, with each value separated by a given delimeter |
| `String` | `getColumnValues(String columnid, int startrow, int endrow, String delimeter, boolean distinct)` | Returns concatenated list of column values, with each value separated by a given delimeter |
| `String` | `getColumnValues(String columnid, String delimeter)` | Returns concatenated list of column values, with each value separated by a given delimeter |
| `DateFormat` | `getDateDisplayFormat(String columnid)` | Returns current date display format for column. |
| `double` | `getDouble(int row, String columnid)` | Get an int value from a Numeric column. |
| `double` | `getDouble(int row, String columnid, double defaultvalue)` | Get an int value from a Numeric column with a default value if the value is null. |
| `DataSet` | `getFilteredDataSet(HashMap filtermap)` | Returns a new DataSet representing a subset of the rows in the current DataSet, as specified by a filtering HashMap. |
| `DataSet` | `getFilteredDataSet(HashMap filtermap, boolean exclusive)` | — |
| `ArrayList<DataSet>` | `getGroupedDataSets(String columnList)` | Returns an ArrayList of DataSets that have been grouped by one or more columns. |
| `String` | `getId()` | — |
| `DataSetIndex` | `getIndex()` | Get index for this data set. |
| `int` | `getInt(int row, String columnid)` | Get an int value from a Numeric column. |
| `int` | `getInt(int row, String columnid, int defaultvalue)` | Get an int value from a Numeric column with a default value if the value is null. |
| `Set<String>` | `getKeyColumnSet()` | Get list of key columns of this data set. |
| `Locale` | `getLocale()` | Return the locale the dataset object uses. |
| `long` | `getLong(int row, String columnid)` | Get an int value from a Numeric column. |
| `M18NUtil` | `getM18n()` | — |
| `Object` | `getObject(int row, String columnid)` | Returns an object containing the String, BigDecimal or Calendar for a given row / column. |
| `int` | `getRowCount()` | Returns the number of rows. |
| `DataSet` | `getRows(int from, int to)` | — |
| `ArrayList<DataSet>` | `getSplitDataSets(int maxRows)` | Returns an ArrayList of DataSets that have been split from the original into datasets of a maximum size. |
| `String` | `getString(int row, String columnid)` | Get a string value from a String column. |
| `String` | `getString(int row, String columnid, String defaultvalue)` | Get a string value from a String column with a default value if the string is null (or empty). |
| `Timestamp` | `getTimestamp(int row, String columnid)` | Get a JDBC Timestamp object from a Date column. |
| `Timestamp` | `getTimestamp(int row, String columnid, Timestamp defaultvalue)` | Get a JDBC Timestamp object from a Date column with a default value if the value is null. |
| `TimeZone` | `getTimeZone()` | Return the timezone the dataset object uses. |
| `String` | `getValue(int row, String columnid)` | Returns a String representing the value for the specified row / column, independant of column type. |
| `String` | `getValue(int row, String columnid, boolean ignoreMasking)` | Returns a String representing the value for the specified row / column, independant of column type. |
| `String` | `getValue(int row, String columnid, String nullvalue)` | Returns a String representing the value for the specified row / column, independant of column type. |
| `boolean` | `isForceISOFormat()` | — |
| `boolean` | `isIndexing()` | Check if any column is indexed. |
| `boolean` | `isIndexing(String column)` | Check if column is indexed. |
| `boolean` | `isMasked(int row, String columnid)` | Checks if the value in the specified row and column is applicable for data masking when being accessed by the getValue method. |
| `boolean` | `isNull(int row, String columnid)` | Checks whether a value is null (or an empty string if the column is a String column). |
| `boolean` | `isValidColumn(String columnid)` | Checks whether the columnid has already been added. |
| `void` | `moveRow(int from, int amount)` | — |
| `void` | `padColumn(String columnid)` | Runs down a column and fills in any null values (or empty String values if it is a String column ) with the non-null value from the previous row. |
| `void` | `padColumns()` | Runs down all columns and fills in any null values (or empty String values if it is a String column ) with the non-null value from the previous row. |
| `void` | `populateFindMap(String columnId, String value, HashMap findmap)` | Internal. |
| `void` | `removeColumn(String oldColumnid)` | — |
| `void` | `renameColumn(String oldColumnid, String newColumnid)` | Rename a column |
| `void` | `reset()` | Clears all data, sorts and columns from the dataset. |
| `void` | `setCdataEscape(String escape)` | set the CDATA escape sequence to be used when calling toXML() on the DataSet |
| `boolean` | `setClob(int row, String columnid, String value)` | Adds a clob into the dataset at the specified point. |
| `void` | `setColidCaseSensitive(boolean isColidCaseSensitive)` | Specify a whether the dataset object should treat columnid as case sensitive. |
| `boolean` | `setColumnRowString(String columns, String rowdata)` | Populates the dataset from a string containing column names and a string containing row data. |
| `boolean` | `setColumnRowString(String columns, String rowdata, String delim1, String delim2)` | — |
| `void` | `setConnectionInfo(ConnectionInfo connectionInfo)` | Specify a connectionInfo object(usually available in the sapphireConnection variable) for formatting date and number type column to string by getValue methods and for setValue methods to interpret inputs of date and number |
| `boolean` | `setDate(int row, String columnid, Calendar value)` | Adds a Calendar into the dataset at the specified point. |
| `boolean` | `setDate(int row, String columnid, long value)` | Adds a date (parsed from a long) into the dataset at the specified point. |
| `boolean` | `setDate(int row, String columnid, String value)` | Adds a date (parsed from a String) into the dataset at the specified point. |
| `boolean` | `setDate(int row, String columnid, Timestamp value)` | Adds a JDBC Timestamp into the dataset at the specified point. |
| `void` | `setDateDisplayFormat(String columnid, DateFormat dateformat)` | Specify a DateFormat to use for date type column by getValue methods |
| `void` | `setForceISOFormat(boolean forceISOFormat)` | If set to true, then this DataSet object will get/set Date values in ISO format i.e. |
| `void` | `setId(String id)` | — |
| `boolean` | `setJSONObject(JSONObject json)` | — |
| `void` | `setKeyColumns(Set<String> columnIds)` | Set key columns of this data set. |
| `void` | `setKeyColumns(String columnIds)` | Set key columns of this data set. |
| `void` | `setLocale(Locale locale)` | **[Deprecated]** You should not set the locale directly. Use the DataSet( connectionInfo ) constructor or setM18NUtil instead. Deprecated. You should not set the locale directly. Use the DataSet( connectionInfo ) constructor or setM18NUtil instead |
| `void` | `setM18NUtil(M18NUtil m18NUtil)` | Specify a M18NUtil object to use for formatting date and number type column to string by getValue methods and for setValue methods to interpret inputs of date and number |
| `boolean` | `setMaskedString(int row, String columnid, String value)` | Sets the Masked value of the requested cell. |
| `boolean` | `setNumber(int row, String columnid, BigDecimal value)` | Adds a number (BigDecimal) into the dataset at the specified point. |
| `boolean` | `setNumber(int row, String columnid, double value)` | Adds a number (long) into the dataset at the specified point. |
| `boolean` | `setNumber(int row, String columnid, int value)` | Adds a number (int) into the dataset at the specified point. |
| `boolean` | `setNumber(int row, String columnid, long value)` | Adds a number (long) into the dataset at the specified point. |
| `boolean` | `setNumber(int row, String columnid, String value)` | Adds a number (parsed from a String) into the dataset at the specified point. |
| `boolean` | `setObject(int row, String columnid, Object o)` | Adds an object into the dataset at the specified point. |
| `boolean` | `setResultSet(ResultSet rs)` | Populates dataset with JDBC result set. |
| `boolean` | `setResultSet(ResultSet rs, boolean extendedDataTypes, String dbms)` | Populates dataset with JDBC result set. |
| `boolean` | `setResultSet(ResultSet rs, boolean extendedDataTypes, String dbms, ResultSetRowProcessor rowProcessor)` | Populates dataset with JDBC result set. |
| `boolean` | `setSequence(int startrow, int endrow, String columnid, int startseq)` | Populates a subset of rows of a numeric column with a sequentially increasing value. |
| `boolean` | `setSequence(String columnid)` | Populates a numeric column with a sequentially increasing value. |
| `boolean` | `setString(int row, String columnid, String value)` | Adds a string into the dataset at the specified point. |
| `void` | `setTimeZone(TimeZone timezone)` | **[Deprecated]** You should not set the timezone directly. Use the DataSet( connectionInfo ) constructor or setM18NUtil instead. Deprecated. You should not set the timezone directly. Use the DataSet( connectionInfo ) constructor or setM18NUtil instead |
| `void` | `setTimeZoneInsensitive(String columnid)` | Specify a Date Column to use system default time zone for parsing and formatting when setValue and getValue methods are called. |
| `void` | `setTimezoneOffset(String zoneid)` | Sets an offset (in addition to the user's timezone offset) to incoming and outgoing String representations of a Calendar value |
| `void` | `setTimezoneOffset(ZoneId zoneId)` | Sets an offset (in addition to the user's timezone offset) to incoming and outgoing String representations of a Calendar value |
| `boolean` | `setValue(int row, String columnid, String value)` | Adds a string value into the dataset at the specified point, converted to the appropriate data type. |
| `boolean` | `setXML(String xml)` | Populates dataset with JDBC result set. |
| `void` | `showData()` | Writes the contents of the dataset to the System.out stream. |
| `void` | `sort(String sortstring)` | Sorts the dataset according to the specified sort criteria. |
| `void` | `sort(String sortstring, boolean ignoreMaskedData)` | — |
| `String` | `toHTML()` | Default toHTML() method. |
| `String` | `toHTML(List titles)` | Default toHTML() method. |
| `String` | `toHTML(List titles, List columnIds)` | — |
| `String` | `toHTML(List titles, String stylePrefix)` | Default toHTML() method. |
| `String` | `toHTML(List titles, String stylePrefix, List<String> columnIds)` | Default toHTML() method. |
| `JSONObject` | `toJSONObject()` | — |
| `JSONObject` | `toJSONObject(boolean includeClobs, boolean includeUnknows)` | — |
| `JSONObject` | `toJSONObject(boolean optimizedFormat, boolean includeClobs, boolean includeUnknows)` | — |
| `JSONObject` | `toJSONObject(boolean optimizedFormat, String[] columns, boolean useDataTypes, boolean includeClobs, boolean includeUnknows)` | — |
| `String` | `toJSONString()` | — |
| `String` | `toJSONString(boolean includeClobs, boolean includeUnknows)` | — |
| `String` | `toJSONString(boolean optimizedFormat, boolean includeClobs, boolean includeUnknows)` | — |
| `String` | `toJSONString(boolean optimizedFormat, String[] columns, boolean useDataTypes, boolean includeClobs, boolean includeUnknows)` | — |
| `String` | `toString()` | Default toString() method. |
| `String` | `toXML()` | Default toXML() method. |
| `String` | `toXML(boolean includeClobs)` | Default toXML() method. |
| `String` | `toXML(boolean includeClobs, boolean includeColumnDefinitions)` | Default toXML() method. |
| `boolean` | `useIndex(HashMap filter)` | Check if filtering data set with given filter would benefit from using indexing. |

### 9.5 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 10. SDIData — Class Reference

**Full name:** `sapphire.util.SDIData`
**Declaration:** `public class SDIData extends java.lang.Object implements java.io.Serializable, com.labvantage.sapphire.gwt.shared.constants.DatasetNameConstants`

Class used to hold information about SDIs and their related detail tables.
The class it typically populated by first populating an SDIRequest object and passing it to the SDIProcessor.
This returns a populated SDIData class.
The SDIData class is essentially a collection of DataSets, each representing an SDI releated table that was requested through
the SDIRequest object. Methods are provided to access these DataSets through datasetnames as defined in the original SDIRequest object.

### 10.2 Public Fields and Constants

| Name | Type | Description |
|---|---|---|
| `linkedSDIData` | `protected LinkedHashMap` | — |
| `sdiData` | `protected LinkedHashMap` | — |

### 10.3 Constructors

```java
SDIData()
// Default constructor
SDIData(String sdcid)
SDIData(String[] primarykeycols)
// Internal constructor, not currently supported.
SDIData(String keycolid1,
String keycolid2,
String keycolid3)
// Internal constructor, not currently supported.
SDIData(String sdcid,
String keycolid1,
String keycolid2,
String keycolid3)
```

### 10.4 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|
| `DataSet` | `getDataset(String datasetname)` | Returns a sapphire.util.SDIData for the specified dataset name |
| `static String` | `getDatasetCode(String datasetname)` | Return the dataset code for a given dataset name. |
| `static String[]` | `getDatasetCodes()` | Returns the full list of dataset codes currently recognized by the SDIData and SDIRequest classes. |
| `String[]` | `getDataSetKeys(String datasetName)` | — |
| `static String` | `getDatasetName(String datasetcode)` | Return the dataset name for a given dataset code. |
| `static String` | `getDatasetNameByTableName(String tablename)` | Return the dataset name for a given table name. |
| `static String[]` | `getDatasetNames()` | Returns the full list of dataset names currently recognized by the SDIData and SDIRequest classes. |
| `Set` | `getDatasets()` | Returns the datanames of all datasets currently populated. |
| `static String` | `getDatasetTablename(String datasetname)` | Return the dataset table name for a given datasetname. |
| `static String[]` | `getDatasetTables()` | Internal method, not currently supported. |
| `String` | `getDetailDetailLinkid(String linktable)` | — |
| `String` | `getDetailLinkid(String linktable)` | — |
| `String[]` | `getDetailLinkTableKeys(String linktable)` | Internal method, not currently supported. |
| `String[]` | `getDetailLinkTables()` | Internal method, not currently supported. |
| `String[]` | `getKeys(String datasetname)` | Returns the key columns for the specified dataset name. |
| `String` | `getLinkid()` | — |
| `String` | `getLinkid(String linktable)` | Internal method, not currently supported. |
| `String[]` | `getLinkTableKeys(String linktable)` | Internal method, not currently supported. |
| `String[]` | `getLinkTables()` | Internal method, not currently supported. |
| `String` | `getPrimaryFKRsetid(String primaryfkcolumnid)` | — |
| `String` | `getPrimaryRsetid()` | Returns the Rsetid that was used to populate the SDIData. |
| `int` | `getQualifiedRows()` | Returns the actual qualified SDIs according to SDIRequest. |
| `int` | `getRequestStatus()` | Internal method, not currently supported. |
| `String` | `getRsetid()` | Returns the Rsetid that was used to populate the SDIData. |
| `String` | `getSdcid()` | — |
| `Set` | `getSDIData()` | Returns the datanames of all additional linked SDIData currently populated. |
| `SDIData` | `getSDIData(String sdidataname)` | Returns a sapphire.util.DataSet for the specified dataset name |
| `void` | `removeDataset(String datasetname)` | — |
| `void` | `sanitizeDataset(String datasetname, String sysuserid, String tool, Calendar now)` | Internal method, not currently supported. |
| `void` | `sanitizeDataset(String datasetname, String sysuserid, String tool, Calendar now, PropertyList props)` | Internal method, not currently supported. |
| `void` | `setDataset(String dataset, DataSet ds)` | Internal method, not currently supported. |
| `void` | `setDataset(String dataset, ResultSet rs)` | Internal method, not currently supported. |
| `void` | `setDataSetKeys(String[][] dataSetKeys)` | — |
| `void` | `setDetailLinks(String[] linkIds, String[] detailLinkIds, String[] linktables)` | Internal method, not currently supported. |
| `void` | `setDetailLinkTableKeys(String linktable, String[] keys)` | Internal method, not currently supported. |
| `void` | `setKeys(String datasetname, String keyid1, String keyid2, String keyid3)` | — |
| `void` | `setLinkid(String linkid)` | — |
| `void` | `setLinks(String[] linkids, String[] linktables)` | Internal method, not currently supported. |
| `void` | `setLinkTableKeys(String linktable, String[] keys)` | Internal method, not currently supported. |
| `void` | `setPrimaryFKRsetid(String primaryfkcolumnid, String rsetId)` | — |
| `void` | `setPrimaryKeyCols(String keycolid1, String keycolid2, String keycolid3)` | — |
| `void` | `setQualifiedRows(int rows)` | Internal method, not currently supported. |
| `void` | `setRequestStatus(int status)` | Internal method, not currently supported. |
| `void` | `setRsetid(String rsetid)` | Internal method, not currently supported. |
| `void` | `setSdcid(String sdcid)` | — |
| `boolean` | `setSDIData(String xmlString)` | — |
| `void` | `setSDIData(String dataname, SDIData sdiData)` | Internal method, not currently supported. |
| `JSONObject` | `toJSONObject()` | — |
| `String` | `toJSONString()` | — |
| `String` | `toXML()` | — |
| `String` | `toXML(int indentFactor, boolean ignoreEmpty)` | — |
| `String` | `toXML(int indentFactor, boolean ignoreEmpty, boolean forceISOFormat)` | — |

### 10.5 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 11. SDIDataStore — Class Reference

**Full name:** `sapphire.util.SDIDataStore`
**Declaration:** `public class SDIDataStore extends java.lang.Object implements com.labvantage.sapphire.services.DDTConstants`

This class wraps an SDIData object can converts each of its DataSet into DataStores. In this way, modifications to the contents of the SDIData can be tracked.
If the SDIData holds any nested SDIDatas (e.g. for child SDIs) then the nested SDIDatas are converted to SDIDataStore objects too.
See details of the SDIData and DataStore object for additional information.

### 11.2 Constants and Constants/Error Types

| Constant | Value (logical) | Use |
|---|---|---|
| `YES` | `"Y"` | Standard boolean flag |
| `NO` | `"N"` | Standard boolean flag |
| `NULL` | `""` | Empty/null sentinel |
| `POLL` | `"POLL"` | Async poll sentinel |
| `DEFAULT_SEPARATOR` | `";"` | Default list delimiter |
| `RETURN` | — | Generic return key |
| `RETURN_SUCCESS` | — | Success return value |
| `RETURN_FAILURE` | — | Failure return value |
| `VERSION` | — | Current LV version |
| `ESC_SEMICOLON` | — | Escaped semicolon literal |
| `TYPE_FAILURE` | `"failure"` | Hard failure error type |
| `TYPE_CONFIRM` | `"confirm"` | Confirmation prompt error type |
| `TYPE_VALIDATION` | `"validation"` | Validation error type |
| `TYPE_INFORMATION` | `"information"` | Informational message type |

Constants inherited from `BaseCustom`: `SUCCESS` (int 1), `FAILURE` (int 0).

### 11.3 Constructors

```java
SDIDataStore(JsonObject jso)
// Constructs a new SDIDataStore that was previously serialized with .toJsonObject()
SDIDataStore(JsonObject jso,
ConnectionInfo connectionInfo)
SDIDataStore(SDIData sdiData)
// Constructs a new SDIDataStore, wrapping the provided SDIData.
```

### 11.4 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|
| `SDIDataStore` | `findSDIDataStore(String sdcid)` | — |
| `List<String>` | `getDataSetNames()` | Returns the list of datasetnames held in the wrapped SDIData |
| `DataStore` | `getDataStore(String datasetName)` | Fetches the specified DataStore |
| `String[]` | `getKeys(String datasetName)` | Convenience method to fetch the primary keys for the specified DataStore |
| `String` | `getLinkid()` | The LinkId if this is wraps a nested SDIData |
| `String` | `getSdcid()` | The SDC for the wrapped SDIData |
| `SDIDataStore` | `getSDIDataStore(String name)` | Returns a nested SDIDataStore |
| `List<String>` | `getSDIDataStoreNames()` | Returns the list of datasetnames held in the wrapped SDIData |
| `JsonObject` | `toJsonObject()` | Serialized the SDIDataStore object |

### 11.5 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 12. SafeSQL — Class Reference

**Full name:** `sapphire.util.SafeSQL`
**Declaration:** `public class SafeSQL extends java.lang.Object`

This utility class is designed to facilitate converting queryProcessor.getSQLDataSet call
to getPreparedSQLDataSet calls to prevent SQL injection attack and improve database performance.

### 12.3 Constructors

```java
SafeSQL()
```

### 12.4 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|
| `String` | `addIn(Collection<String> collection)` | Adds literals in collection to bind variables in SQL |
| `String` | `addIn(String valuelist)` | Replace a single literal in SQL and remember the value |
| `String` | `addIn(String valuelist, String delimiter)` | Replace a single literal in SQL and remember the value |
| `String` | `addVar(Object value)` | Replace a single literal in SQL and remember the value |
| `static String` | `convertToSQLInClause(String input, String delimiter, boolean isOracle)` | Utility method to convert String input of a list of items separated by delimiter to be used as literals with escaped in a SQL in clause |
| `static String` | `encodeForSQL(String input, boolean isOracle)` | Utility method to encode String literal when building query where clause for SDIRequest as PreparedStatement cannot be used in this case |
| `String` | `getPreparedSQL()` | — |
| `Object[]` | `getValues()` | Return all previously added values since instantiation or last reset calle as an object array from calling addValue and addIn calls to be use as bind variable values in the QueryProcessor getPreparedSQLDataSet call |
| `static Object[]` | `joinArrays(Object[] array1, Object[] array2)` | Utility method to join two object arrays. |
| `static String` | `replaceAllWithInVars(String query, String tokenToReplace, String replaceWithvaluelist, String listdelimitor, SafeSQL safeSQL)` | Utility method to allow the passed in SafeSQL to replace any replaced token with ?,?,?,... |
| `static String` | `replaceAllWithVars(String query, String tokenToReplace, String replaceWithvalue, SafeSQL safeSQL)` | Utility method to allow the passed in SafeSQL to replace any replaced token with ? and registerd the replace value to be included in the getValues() return. |
| `void` | `reset()` | Reset the bind variable values for building a new SQL. |
| `void` | `setPreparedSQL(String preparedSQL)` | Set the preparedSQL string. |

### 12.5 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 13. QueryProcessor — Class Reference

**Full name:** `sapphire.accessor.QueryProcessor`
**Declaration:** `public class QueryProcessor extends com.labvantage.sapphire.BaseAccessor`

Supports QueryManager access and related functions

### 13.3 Constructors

```java
QueryProcessor(File rakFile,
String connectionid)
// Constructor for a client application with an existing connection
QueryProcessor(PageContext pageContext)
// Constructor for a client web application with a page Context and existing connectionid
QueryProcessor(String connectionid)
// Constructor for creating an accessor from components
QueryProcessor(String nameserverlist,
String connectionid)
// Deprecated.
```

### 13.4 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|
| `int` | `execPreparedUpdate(String sql, Object[] bindvars)` | Utility method for passing a prepared SQL along with bind variables to be executed against the database by the application server. |
| `int` | `execSQL(int sqlCode)` | — |
| `int` | `execSQL(int sqlCode, Object[] bindVars)` | — |
| `int` | `execSQL(String sql)` | Utility method for passing SQL to be executed against the database by the application server. |
| `int` | `getCount(String sql)` | Executes the sql the should return a single row with a single number value. |
| `String` | `getKeyid1List(String sdcid, String queryid)` | Executes the supplied Sapphire query and returns the list of sdi(s). |
| `String` | `getKeyid1List(String sdcid, String queryid, String[] params)` | Executes the supplied Sapphire query and returns the list of sdi(s). |
| `String` | `getKeyid1List(String sdcid, String queryid, String param1, String param2, String param3, String param4, String param5)` | Executes the supplied Sapphire query and returns the list of sdi(s). |
| `int` | `getPreparedCount(String sql, Object[] params)` | Executes the sql with bind variables the should return a single row with a single number value. |
| `DataSet` | `getPreparedSqlDataSet(int sqlCode, Object[] params)` | Returns a sapphire.util.DataSet that reflects the resultset for the given sql |
| `DataSet` | `getPreparedSqlDataSet(int sqlCode, Object[] params, boolean extendedDataTypes)` | Returns a sapphire.util.DataSet that reflects the resultset for the given sql |
| `DataSet` | `getPreparedSqlDataSet(String sql, Object[] params)` | Returns a sapphire.util.DataSet that reflects the resultset for the given sql |
| `DataSet` | `getPreparedSqlDataSet(String sql, Object[] params, boolean extendedDataTypes)` | Returns a sapphire.util.DataSet that reflects the resultset for the given sql |
| `DataSet` | `getPreparedSqlDataSet(String name, String sql, Object[] params)` | Returns a sapphire.util.DataSet that reflects the resultset for the given sql |
| `DataSet` | `getPreparedSqlDataSet(String name, String sql, Object[] params, boolean extendedDataTypes)` | Returns a sapphire.util.DataSet that reflects the resultset for the given sql |
| `DataSet` | `getPreparedSqlDataSet(String name, String sql, Object[] params, boolean extendedDataTypes, int queryTimeout)` | Returns a sapphire.util.DataSet that reflects the resultset for the given sql |
| `DataSet` | `getRefTypeDataSet(String reftypeid)` | Returns a dataset that contains the details of the reference values for the supplied ReferenceType. |
| `String` | `getSecurityFilterWhere(String sdcid)` | — |
| `DataSet` | `getSqlDataSet(int sqlCode)` | Returns a sapphire.util.DataSet that reflects the resultset for the given sqlcode |
| `DataSet` | `getSqlDataSet(int sqlCode, boolean extendedDataTypes)` | Returns a sapphire.util.DataSet that reflects the resultset for the given sqlcode |
| `DataSet` | `getSqlDataSet(int sqlCode, Object[] bindVars)` | Returns a sapphire.util.DataSet that reflects the resultset for the given sqlcode |
| `DataSet` | `getSqlDataSet(int sqlCode, Object[] bindVars, boolean extendedDataTypes)` | Returns a sapphire.util.DataSet that reflects the resultset for the given sqlcode |
| `DataSet` | `getSqlDataSet(String sql)` | Returns a sapphire.util.DataSet that reflects the resultset for the given sql |
| `DataSet` | `getSqlDataSet(String sql, boolean extendedDataTypes)` | Returns a sapphire.util.DataSet that reflects the resultset for the given sql |
| `DataSet` | `getSqlDataSet(String name, String sql)` | Returns a sapphire.util.DataSet that reflects the resultset for the given sql |
| `DataSet` | `getSqlDataSet(String name, String sql, boolean extendedDataTypes)` | Returns a sapphire.util.DataSet that reflects the resultset for the given sql |
| `DataSet` | `getSqlDataSet(String name, String sql, boolean extendedDataTypes, int queryTimeout)` | Returns a sapphire.util.DataSet that reflects the resultset for the given sql |
| `DataSet` | `getSqlDataSet(String name, String sql, boolean extendedDataTypes, int queryTimeout, boolean keepAlive)` | Returns a sapphire.util.DataSet that reflects the resultset for the given sql |

### 13.5 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 14. DBAccess — Interface Reference

**Full name:** `sapphire.util.DBAccess`
**Declaration:** `public interface DBAccess`

Checks the existence of a record using a SQL query.

### 14.4 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|
| `boolean` | `checkExists(String existsSQL)` | Checks the existence of a record using a SQL query. |
| `boolean` | `checkPreparedExists(String existsSQL, Object[] o1)` | Checks the existence of a record using a prepared SQL query with bind variables. |
| `void` | `closeCall()` | Closes the default callablestatement. |
| `void` | `closeCall(String name)` | Closes a named callablestatement. |
| `void` | `closeResultSet()` | Closes the default resultset. |
| `void` | `closeResultSet(String name)` | Closes a named resultset. |
| `void` | `closeStatement()` | Closes the default prepared statement. |
| `void` | `closeStatement(String name)` | Closes a named prepared statement. |
| `void` | `createPreparedResultSet(String sql, Object[] objectlist)` | Creates a new auto-named result set based on bind variables. |
| `void` | `createPreparedResultSet(String name, String sql, Object[] objectlist)` | Creates a new named result set based on bind variables. |
| `void` | `createResultSet(String sql)` | Creates a new auto-named result set. |
| `void` | `createResultSet(String name, String sql)` | Creates a new named result set. |
| `int` | `executePreparedUpdate(String sql, Object[] objectlist)` | Executes a statement (insert, update or delete) based on bind bind variables. |
| `void` | `executeSQL(String sql)` | executeSQL Method |
| `int` | `executeUpdate(String sql)` | executeUpdate Method |
| `BigDecimal` | `getBigDecimal(String column)` | Returns a column big decimal value from the current record of the default resultset. |
| `BigDecimal` | `getBigDecimal(String name, String column)` | Returns a column big decimal value from the current record of a resultset. |
| `String` | `getBinaryStream(String column)` | **[Deprecated]** This method is replaced by getClob to retrieve from nvarchar(max) column. Deprecated. This method is replaced by getClob to retrieve from nvarchar(max) column |
| `String` | `getBinaryStream(String name, String column)` | **[Deprecated]** This method is replaced by getClob to retrieve from nvarchar(max) column. Deprecated. This method is replaced by getClob to retrieve from nvarchar(max) column |
| `Blob` | `getBlob(String column)` | Returns a column blob value from the current record of the default resultset. |
| `Blob` | `getBlob(String name, String column)` | Returns a column blob value from the current record of a resultset. |
| `String` | `getClob(String column)` | Returns a column clob value from the current record of the default resultset. |
| `String` | `getClob(String name, String column)` | Returns a column clob value from the current record of a resultset |
| `int` | `getColumnCount()` | getColumnCount Method |
| `int` | `getColumnCount(String name)` | getColumnCount Method |
| `String` | `getColumnName(int col)` | getColumnName Method |
| `String` | `getColumnName(String name, int col)` | getColumnName Method |
| `Connection` | `getConnection()` | getConnection Method |
| `int` | `getCount(String countSQL)` | Returns an int being the result of a select count(*) type query |
| `int` | `getInt(String column)` | Returns a column integer value from the current record of the default resultset. |
| `int` | `getInt(String name, String column)` | Returns a column integer value from the current record of a resultset. |
| `boolean` | `getNext()` | Moves to the next record in the objects resultset. |
| `boolean` | `getNext(String name)` | Moves to the next record of a resultset. |
| `int` | `getPreparedCount(String countSQL, Object[] o1)` | Returns an int being the result of a select count(*) type query |
| `int` | `getQueryTimeout()` | getQueryTimeout Method |
| `ResultSet` | `getResultSet()` | getResultSet Method |
| `ResultSet` | `getResultSet(String name)` | getResultSet Method |
| `String` | `getString(String column)` | Returns a column string value from the current record of the default resultset. |
| `String` | `getString(String name, String column)` | Returns a column string value from the current record of a resultset. |
| `Timestamp` | `getTimestamp(String column)` | Returns a column timestamp value from the current record of the default resultset. |
| `Timestamp` | `getTimestamp(String name, String column)` | Returns a column timestamp value from the current record of a resultset. |
| `String` | `getValue(String column)` | Returns a not null string value from the current record of the default resultset. |
| `String` | `getValue(String name, String column)` | Returns a not null string value from the current record of a resultset. |
| `String` | `hint(String table)` | **[Deprecated]** This method is no longer required for SQLServer implementations due to the implementation of row versioning isolation mode. Deprecated. This method is no longer required for SQLServer implementations due to the implementation of row versioning isolation mode |
| `boolean` | `isOracle()` | isOracle Method |
| `boolean` | `isSqlServer()` | isSqlServer Method |
| `String` | `newName()` | Returns a unique name that can be used when creating resultsets or prepared statements. |
| `CallableStatement` | `prepareCall(String statement)` | Prepares a new auto-named callable statement. |
| `CallableStatement` | `prepareCall(String name, String statement)` | Prepares a new named callable statement. |
| `PreparedStatement` | `prepareStatement(String statement)` | Prepares a new auto-named callable statement. |
| `PreparedStatement` | `prepareStatement(String name, String statement)` | Prepares a new named prepared statement. |
| `void` | `setQueryTimeout(int queryTimeout)` | setQueryTimeout Method |

### 14.5 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 15. ActionProcessor — Class Reference

**Full name:** `sapphire.accessor.ActionProcessor`
**Declaration:** `public class ActionProcessor extends com.labvantage.sapphire.BaseAccessor`

Supports APM access and related functions

### 15.3 Constructors

```java
ActionProcessor(File rakFile,
String connectionid)
// Constructor for a client application with an existing connection
ActionProcessor(PageContext pageContext)
// Constructor for a client web application with a page Context and existing connectionid
ActionProcessor(String connectionid)
// Constructor for creating an accessor from components
ActionProcessor(String nameserverlist,
String connectionid)
// Deprecated.
```

### 15.4 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|
| `void` | `processAction(String actionid, String versionid, HashMap properties)` | — |
| `void` | `processAction(String actionid, String versionid, HashMap properties, boolean newtransaction)` | — |
| `void` | `processAction(String actionid, String versionid, HashMap properties, boolean newtransaction, boolean processasynchronous)` | — |
| `void` | `processActionBlock(ActionBlock actionblock)` | Processes all actions in the action block. |
| `void` | `processActionBlock(ActionBlock actionblock, boolean newtransaction)` | Processes all actions in the action block. |
| `void` | `processActionBlock(ActionBlock actionblock, boolean newtransaction, boolean processasynchronous)` | Processes all actions in the action block. |
| `void` | `processActionClass(String actionClass, HashMap properties, boolean newtransaction)` | — |
| `void` | `processActionClass(String actionClass, PropertyList properties)` | — |
| `void` | `reset()` | Resets the error stack |

### 15.5 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 16. SDIProcessor — Class Reference

**Full name:** `sapphire.accessor.SDIProcessor`
**Declaration:** `public class SDIProcessor extends com.labvantage.sapphire.BaseAccessor`

Supports SDIList access and related functions

### 16.3 Constructors

```java
SDIProcessor(File rakFile,
String connectionid)
// Constructor for a client application with an existing connection
SDIProcessor(PageContext pageContext)
// Constructor for a client web application with a page Context and existing connectionid
SDIProcessor(String connectionid)
// Constructor for creating an accessor from components
SDIProcessor(String nameserverlist,
String connectionid)
// Deprecated.
```

### 16.4 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|
| `int` | `getSDICount(SDIRequest sdiRequest)` | — |
| `int` | `getSDICount(SDIRequest sdiRequest, boolean keepAlive)` | — |
| `SDIData` | `getSDIData(SDIRequest sdirequest)` | Returns the SDI data for a given request |

### 16.5 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 17. SDCProcessor — Class Reference

**Full name:** `sapphire.accessor.SDCProcessor`
**Declaration:** `public class SDCProcessor extends com.labvantage.sapphire.BaseAccessor implements com.labvantage.sapphire.services.DDTConstants`

Supports SDCManager access and related functions

### 17.3 Constructors

```java
SDCProcessor(File rakFile,
String connectionid)
// Constructor for a client application with an existing connection
SDCProcessor(PageContext pageContext)
// Constructor for a client web application with a page Context and existing connectionid
SDCProcessor(String connectionid)
// Constructor for creating an accessor from components
SDCProcessor(String nameserverlist,
String connectionid)
// Deprecated.
```

### 17.4 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|
| `PropertyListCollection` | `getAttributes(String sdcid)` | — |
| `DataSet` | `getColumnData(String sdcid)` | — |
| `PropertyListCollection` | `getColumns(String sdcid)` | — |
| `HashMap` | `getDetailLinkProperties(String sdcid, String linkid)` | — |
| `PropertyListCollection` | `getDetailLinks(String sdcid)` | — |
| `HashMap` | `getLinkProperties(String sdcid, String linkid)` | — |
| `PropertyListCollection` | `getLinks(String sdcid)` | — |
| `DataSet` | `getLinksData(String sdcid)` | — |
| `PropertyList` | `getProperties(String sdcid)` | — |
| `String` | `getProperty(String sdcid, String propertyid)` | — |
| `String` | `getProperty(String sdcid, String propertyid, String defaultValue)` | — |
| `PropertyList` | `getPropertyList(String sdcid)` | — |
| `DataSet` | `getReverseLinksData(String sdcid)` | — |
| `String` | `getSDCColumnProperty(String sdcid, String columnid, String propertyname)` | — |
| `HashMap` | `getSDCProperties(String sdcid)` | — |
| `DataSet` | `getTableColumnData(String tableid)` | — |

### 17.5 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 18. SequenceProcessor — Class Reference

**Full name:** `sapphire.accessor.SequenceProcessor`
**Declaration:** `public class SequenceProcessor extends com.labvantage.sapphire.BaseAccessor`

Support SQM access and related function

### 18.3 Constructors

```java
SequenceProcessor(File rakFile,
String connectionid)
// Constructor for a client application with an existing connection
SequenceProcessor(PageContext pageContext)
// Constructor for a client web application with a page Context and existing connectionid
SequenceProcessor(String connectionid)
// Constructor for creating an accessor from components
SequenceProcessor(String nameserverlist,
String connectionid)
// Deprecated.
```

### 18.4 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|
| `int` | `getSequence(String sdcid, String sequenceid)` | Insert the method's description here. |
| `int` | `getSequence(String sdcid, String sequenceid, int incrementby)` | Insert the method's description here. |
| `int` | `getSequence(String sdcid, String sequenceid, int startsequencenumber, int incrementby)` | Insert the method's description here. |
| `String` | `getUUID()` | Creating a random UUID (Universally unique identifier). |

### 18.5 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 19. AttachmentProcessor — Class Reference

**Full name:** `sapphire.accessor.AttachmentProcessor`
**Declaration:** `public class AttachmentProcessor extends com.labvantage.sapphire.BaseAccessor`

Support attachment and related function
AttachmentProcessor.java
Created on April 2, 2003, 9:40 AM

### 19.3 Constructors

```java
AttachmentProcessor(File rakFile,
String connectionid)
// Constructor for a client application with an existing connection
AttachmentProcessor(PageContext pageContext)
// Constructor for a client web application with a page Context and existing connectionid
AttachmentProcessor(String connectionid)
// Constructor for creating an accessor from components
AttachmentProcessor(String nameserverlist,
String connectionid)
// Deprecated.
```

### 19.4 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|
| `Attachment` | `addSDIAttachment(Attachment attachment)` | Method for adding an attachment to an SDI from an Attachment object |
| `Attachment` | `addSDIAttachment(Attachment attachment, boolean applyLock, boolean index, String attachmentPolicyNode)` | Method for adding an attachment to an SDI from an Attachment object |
| `int` | `addSDIAttachment(HashMap properties, byte[] data)` | Method for adding an attachment to an SDI from an byte Array. |
| `int` | `addSDIAttachment(HashMap properties, InputStream data)` | Method for adding an attachment to an SDI from an input stream |
| `int` | `deleteSDIAttachment(Attachment attachment)` | Method for deleting an sdi attachment record. |
| `int` | `deleteSDIAttachment(Attachment attachment, boolean applyLock, String attachmentPolicyNode)` | Method for deleting an sdi attachment record. |
| `Attachment` | `editSDIAttachment(Attachment attachment)` | Method for updating an attachment to an SDI from an Attachment object |
| `Attachment` | `editSDIAttachment(Attachment attachment, boolean applyLock, boolean index, String attachmentPolicyNode)` | Method for updating an attachment to an SDI from an Attachment object |
| `int` | `editSDIAttachment(HashMap properties, byte[] data)` | Method for updating an attachment to an SDI from an byte Array Please note it is recommended to avoid using byte arrays and instead use the inputstream or attachment object methods which are more efficient on memory. |
| `int` | `editSDIAttachment(HashMap properties, InputStream data)` | Method for adding an attachment to an SDI from an input stream |
| `Attachment` | `getSDIAttachment(Attachment attachment, ThumbnailGeneration thumbnailGeneration)` | Get an attachment record in the form of an Attachment object |
| `Attachment` | `getSDIAttachment(Attachment attachment, int auditLog, ThumbnailGeneration thumbnailGeneration)` | Get an attachment record in the form of an Attachment object from the sdiattachment audit table |
| `Attachment` | `getTempAttachment(Attachment attachment, ThumbnailGeneration thumbnailGeneration)` | Get an attachment record in the form of an Attachment object to represent the temporary attachment |

### 19.5 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 20. ConfigurationProcessor — Class Reference

**Full name:** `sapphire.accessor.ConfigurationProcessor`
**Declaration:** `public class ConfigurationProcessor extends com.labvantage.sapphire.BaseAccessor`

Provides access to configuration settings

### 20.3 Constructors

```java
ConfigurationProcessor(File rakFile,
String connectionid)
// Constructor for a client application with an existing connection
ConfigurationProcessor(PageContext pageContext)
// Constructor for a client web application with a page Context and existing connectionid
ConfigurationProcessor(String connectionid)
// Constructor for creating an accessor from components
```

### 20.4 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|
| `PropertyList` | `findPolicy(String policyid, String propertyid, String propertyvalue)` | Return a PropertyList representing the requested policy |
| `PropertyList` | `getPolicy(String policyid)` | Return a PropertyList representing the requested policy |
| `PropertyList` | `getPolicy(String policyid, String nodeid)` | Return a PropertyList representing the requested policy |
| `PropertyList` | `getPolicy(String policyid, String nodeid, boolean translate)` | Return a PropertyList representing the requested policy |
| `String` | `getProfileProperty(String propertyid)` | Return a profiile property for the current user. |
| `String` | `getProfileProperty(String propertyid, String defaultValue)` | Return a profiile property for the current user. |

### 20.5 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 21. TranslationProcessor — Class Reference

**Full name:** `sapphire.accessor.TranslationProcessor`
**Declaration:** `public class TranslationProcessor extends com.labvantage.sapphire.BaseAccessor`

Supports Translation access and related functions

### 21.2 Public Fields and Constants

| Name | Type | Description |
|---|---|---|
| `texttype` | `protected String` | — |

### 21.3 Constructors

```java
TranslationProcessor(File rakFile,
String connectionid)
// Constructor for a client application with an existing connection
TranslationProcessor(PageContext pageContext)
// Constructor for a client web application with a page Context and existing connectionid
TranslationProcessor(String connectionid)
// Constructor for creating an accessor from components
TranslationProcessor(String nameserverlist,
String connectionid)
// Deprecated.
```

### 21.4 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|
| `String` | `getLanguage()` | — |
| `String` | `getTextType()` | — |
| `boolean` | `isRTL()` | — |
| `void` | `saveTranslation(String languageid, String textidlist, String transtextlist)` | — |
| `void` | `saveTranslation(String languageid, String textidlist, String transtextlist, String texttypelist)` | — |
| `void` | `setLanguage(String language)` | — |
| `void` | `setTextType(String textType)` | — |
| `String` | `translate(String textid)` | Method for a client web application to translate text to language in the user profile. |
| `String` | `translate(String textid, Map tokenValueMap)` | Method for a client web application to translate text to language in the user profile. |
| `String` | `translate(String textid, String language)` | Method for a client web application with a page Context to translate text. |
| `String` | `translate(String textid, String language, String texttype)` | Method for a client web application with a page Context to translate text. |
| `String` | `translatePartial(String textid)` | Method for a client web application with a page Context to translate toekens inside text using user's language. |
| `String` | `translatePartial(String textid, String language)` | Method for a client web application with a page Context to translate toekens inside text. |
| `void` | `translateTable(String languageid, HashMap transtable)` | — |

### 21.5 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 22. WorkflowProcessor — Class Reference

**Full name:** `sapphire.accessor.WorkflowProcessor`
**Declaration:** `public class WorkflowProcessor extends com.labvantage.sapphire.BaseAccessor implements com.labvantage.sapphire.pageelements.gwt.shared.WorkflowManagerConstants`

Supports workflow related functions

### 22.2 Public Fields and Constants

| Name | Type | Description |
|---|---|---|
| `QUEUEITEMS_ACTIVE` | `static String` | — |
| `QUEUEITEMS_ALL` | `static String` | — |
| `QUEUEITEMS_AVAILABLE` | `static String` | — |
| `QUEUEITEMS_WAITING` | `static String` | — |

### 22.3 Constructors

```java
WorkflowProcessor(File rakFile,
String connectionid)
// Constructor for a client application with an existing connection
WorkflowProcessor(PageContext pageContext)
// Constructor for a client web application with a page Context and existing connectionid
WorkflowProcessor(String connectionid)
// Constructor for creating an accessor from components
```

### 22.4 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|
| `void` | `allocateTaskQueueItems(String taskexecid, SDIList queueItems)` | — |
| `void` | `allocateTaskQueueItems(String taskexecid, SDIList queueItems, boolean sharedQueue, boolean testMode)` | — |
| `void` | `deleteTaskQueueItems(TaskContext taskContext, String ioid)` | — |
| `DataSet` | `getSDIWorkflowData(String sdcid, String keyid1, String keyid2, String keyid3)` | — |
| `SDIList` | `getTaskQueueItems(TaskContext taskContext, String ioid)` | Returns the available task queue items for the specified queue |
| `SDIList` | `getTaskQueueItems(TaskContext taskContext, String ioid, String queueStatuses)` | Returns the task queue items for the specified queue |
| `DataSet` | `getTaskQueueItemsDataSet(TaskContext taskContext, String ioid)` | Returns the available task queue items for the specified queue |
| `DataSet` | `getTaskQueueItemsDataSet(TaskContext taskContext, String ioid, String queueStatuses)` | Returns the task queue items for the specified queue |

### 22.5 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 23. ConnectionInfo — Class Reference

**Full name:** `sapphire.util.ConnectionInfo`
**Declaration:** `public class ConnectionInfo extends com.labvantage.sapphire.BaseClass implements com.labvantage.sapphire.services.ConnectionInfo, java.io.Serializable`

Deprecated.

### 23.3 Constructors

```java
ConnectionInfo(HashMap userAttributeMap)
ConnectionInfo(SapphireConnection sapphireConnection)
ConnectionInfo(SapphireDatabase sapphireDatabase)
ConnectionInfo(String username,
String password)
ConnectionInfo(String nameserverlist,
String username,
String password)
// Deprecated.
```

### 23.4 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|
| `String` | `getAuthTokenId()` | — |
| `String` | `getConnectionId()` | — |
| `String` | `getCurrentJobtype()` | — |
| `String` | `getDatabaseId()` | — |
| `String` | `getDbms()` | — |
| `String` | `getDbServer()` | — |
| `String` | `getDefaultDepartment()` | — |
| `String` | `getDepartmentList()` | — |
| `String` | `getDeviceId()` | — |
| `String` | `getExternalAppId()` | — |
| `String` | `getGuiMode()` | — |
| `String` | `getJobtypeList()` | — |
| `String` | `getLanguage()` | — |
| `String` | `getLocale()` | — |
| `String` | `getLogonName()` | — |
| `String` | `getModuleList()` | — |
| `String` | `getNameServerList()` | Deprecated. |
| `String` | `getPassword()` | — |
| `String` | `getPortalId()` | — |
| `String` | `getRoleList()` | — |
| `String` | `getServerPassword()` | — |
| `String` | `getServerUsername()` | — |
| `String` | `getSqlDatabase()` | — |
| `String` | `getSysuserId()` | — |
| `String` | `getSysuserName()` | — |
| `String` | `getTimeZone()` | — |
| `String` | `getTool()` | — |
| `boolean` | `getUseFullIncludes()` | — |
| `HashMap` | `getUserAttributeMap()` | — |
| `String` | `getUserType()` | — |
| `boolean` | `hasModule(String module)` | — |
| `boolean` | `hasRole(String role)` | — |
| `boolean` | `isDepartmentMember(String department)` | — |
| `boolean` | `isOracle()` | — |
| `boolean` | `isRtl()` | — |
| `boolean` | `isSqlServer()` | — |
| `String` | `toString()` | — |

### 23.5 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 24. ErrorHandler — Class Reference

**Full name:** `sapphire.error.ErrorHandler`
**Declaration:** `public class ErrorHandler extends com.labvantage.sapphire.gwt.shared.error.ErrorHandler`

Public version of ErrorHandler

### 24.2 Constants and Constants/Error Types

| Constant | Value (logical) | Use |
|---|---|---|
| `YES` | `"Y"` | Standard boolean flag |
| `NO` | `"N"` | Standard boolean flag |
| `NULL` | `""` | Empty/null sentinel |
| `POLL` | `"POLL"` | Async poll sentinel |
| `DEFAULT_SEPARATOR` | `";"` | Default list delimiter |
| `RETURN` | — | Generic return key |
| `RETURN_SUCCESS` | — | Success return value |
| `RETURN_FAILURE` | — | Failure return value |
| `VERSION` | — | Current LV version |
| `ESC_SEMICOLON` | — | Escaped semicolon literal |
| `TYPE_FAILURE` | `"failure"` | Hard failure error type |
| `TYPE_CONFIRM` | `"confirm"` | Confirmation prompt error type |
| `TYPE_VALIDATION` | `"validation"` | Validation error type |
| `TYPE_INFORMATION` | `"information"` | Informational message type |

Constants inherited from `BaseCustom`: `SUCCESS` (int 1), `FAILURE` (int 0).

### 24.3 Constructors

```java
ErrorHandler()
ErrorHandler(String errorString)
ErrorHandler(String errorId,
String errorType,
String errorString)
```

### 24.4 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|
| `ErrorDetail` | `createErrorDetail(String sdcid, String event, String errorid, String errorType, String message)` | — |

### 24.5 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 25. SapphireException — Class Reference

**Full name:** `sapphire.SapphireException`
**Declaration:** `public class SapphireException extends java.lang.Exception`

Default constructor

### 25.2 Public Fields and Constants

| Name | Type | Description |
|---|---|---|
| `TYPE_CONFIRM` | `static String` | — |
| `TYPE_FAILURE` | `static String` | — |
| `TYPE_INFORMATION` | `static String` | — |
| `TYPE_VALIDATION` | `static String` | — |

### 25.3 Constructors

```java
SapphireException()
// Default constructor
SapphireException(String message)
SapphireException(String errorid,
String message)
SapphireException(String errorid,
String errorType,
String message)
SapphireException(String errorid,
String errorType,
String message,
Throwable e)
SapphireException(String errorid,
String message,
Throwable e)
SapphireException(String message,
Throwable e)
SapphireException(Throwable e)
```

### 25.4 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|
| `String` | `getErrorid()` | Returns The errorid as specified by the Exception constructor. |
| `String` | `getErrorType()` | Returns The errortype as specified by the Exception constructor. |

### 25.5 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 26. ConnectionProcessor — Class Reference

**Full name:** `sapphire.accessor.ConnectionProcessor`
**Declaration:** `public class ConnectionProcessor extends com.labvantage.sapphire.BaseAccessor`

Supports CCM access and related functions

### 26.3 Constructors

```java
ConnectionProcessor()
// Default constructor
ConnectionProcessor(File rakFile)
// Constructor for a client application with an existing connection
ConnectionProcessor(File rakFile,
String connectionid)
// Constructor for a client application with an existing connection
ConnectionProcessor(PageContext pageContext)
// Constructor for a client web application with a page Context and existing connectionid
ConnectionProcessor(String connectionid)
// Constructor for creating an accessor from components
ConnectionProcessor(String nameserverlist,
String connectionid)
// Deprecated.
As of version 4.6 replaced by ConnectionProcessor(String) and ConnectionProcessor(File, String)
```

### 26.4 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|
| `void` | `changePassword(String userid, String oldpassword, String newpassword)` | Changes a user's password with remote access |
| `void` | `changePassword(String databaseid, String userid, String oldpassword, String newpassword)` | Changes a user's password with local access |
| `int` | `changePassword(String nameserverlist, String databaseid, String userid, String oldpassword, String newpassword)` | **[Deprecated]** As of version 4.6 replaced by changePassword(String, String, String, String) and changePassword(String, String, String). Deprecated. As of version 4.6 replaced by changePassword(String, String, String, String) and changePassword(String, String, String) |
| `boolean` | `checkConnection(String connectionid)` | Returns true if the connection is valid |
| `boolean` | `checkUser(String sysuserid, String password)` | Returns true if it is a valid username/password combination |
| `void` | `clearConnection(String connectionid)` | Returns a connection id |
| `int` | `clearRSets(String rsetlist)` | — |
| `void` | `disableUser(String sysuserid, String reason)` | Disables a user account |
| `void` | `enableUser(String sysuserid)` | Enables a user account |
| `void` | `forceChangePassword(String sysuserid)` | Forces a user to change password the next time they log in |
| `String` | `getConfigProperty(String propertyid)` | Returns a system-wide configuration property |
| `String` | `getConfigProperty(String propertyid, String defaultValue)` | Returns a system-wide configuration property |
| `String` | `getConnectionid(File rakFile, String userid, String password)` | Returns a connection id |
| `String` | `getConnectionid(String userid, String password)` | Generates and returns a new connectionid with RemoteAccess |
| `String` | `getConnectionid(String databaseid, String userid, String password)` | Returns a connection id |
| `String` | `getConnectionid(String databaseid, String userid, String password, HashMap options)` | Returns a connection id |
| `String` | `getConnectionid(String nameserverlist, String databaseid, String userid, String password)` | **[Deprecated]** As of version 4.6 replaced by getConnectionid(String, String, String) and getConnectionid(String, String). Deprecated. As of version 4.6 replaced by getConnectionid(String, String, String) and getConnectionid(String, String) |
| `String` | `getConnectionid(String nameserverlist, String databaseid, String userid, String password, HashMap options)` | **[Deprecated]** As of version 4.6 replaced by getConnectionid(String, String, String) and getConnectionid(String, String). Deprecated. As of version 4.6 replaced by getConnectionid(String, String, String) and getConnectionid(String, String) |
| `ConnectionInfo` | `getConnectionInfo(String connectionid)` | Returns the connectioninformation associated with the current user |
| `String[]` | `getDatabaseList()` | Returns a list active databases |
| `String` | `getLanguage()` | Returns the languageid associated with the current user |
| `String` | `getLicenseProperty(String propertyid)` | Returns a CCIDHolder class |
| `List` | `getModuleList()` | Returns a semi-colon separated list of modules the current user has access to |
| `String` | `getProfileProperty(String propertyid)` | **[Deprecated]** As of version R5.1 replaced by methods in the ConfigurationProcessor. Deprecated. As of version R5.1 replaced by methods in the ConfigurationProcessor |
| `List` | `getRoleList()` | Returns a semi-colon separated list of roles for the current user |
| `SapphireConnection` | `getSapphireConnection()` | — |
| `String` | `getSysConfigProperty(String propertyid)` | Returns a system-wide configuration property |
| `String` | `getSysConfigProperty(String propertyid, String defaultValue)` | Returns a system-wide configuration property |
| `boolean` | `isMSS()` | Returns whether the current connection uses an Microsoft SQLServer |
| `boolean` | `isOra()` | Returns whether the current connection uses an Oracle database |
| `boolean` | `isValidPassword(String userid, String password)` | Checks a user's password against the current password policy |
| `boolean` | `isValidPassword(String databaseid, String userid, String password)` | Checks a user's password against the current password policy |
| `int` | `isValidPassword(String nameserverlist, String databaseid, String userid, String password)` | **[Deprecated]** As of version 4.6 replaced by isValidPassword(String, String, String, String) and isValidPassword(String, String, String). Deprecated. As of version 4.6 replaced by isValidPassword(String, String, String, String) and isValidPassword(String, String, String) |
| `void` | `prepareToDeleteConnection(String connectionid)` | — |

### 26.5 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 27. DAMProcessor — Class Reference

**Full name:** `sapphire.accessor.DAMProcessor`
**Declaration:** `public class DAMProcessor extends com.labvantage.sapphire.BaseAccessor`

Supports SDCManager access and related functions

### 27.3 Constructors

```java
DAMProcessor(File rakFile,
String connectionid)
// Constructor for a client application with an existing connection
DAMProcessor(PageContext pageContext)
// Constructor for a client web application with a page Context and existing connectionid
DAMProcessor(String connectionid)
// Constructor for creating an accessor from components
DAMProcessor(String nameserverlist,
String connectionid)
// Deprecated.
```

### 27.4 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|
| `SDIList` | `checkSDIAccess(String sdcid, String keyid1list, String keyid2list, String keyid3list, boolean viewHiddenRecord, String operation)` | Method checks if the current user has access to the provided set of SDIs. |
| `int` | `clearRSet(String rsetid)` | Clears a specified RSet |
| `String` | `createLockedRSet(String sdcid, String keyid1list, String keyid2list, String keyid3list)` | Create a new RSet with lock |
| `int` | `createLockedRSet(String sdcid, String keyid1list, String keyid2list, String keyid3list, StringHolder rsetidholder)` | Deprecated. |
| `String` | `createLockedRSetDS(String sdcid, String keyid1list, String keyid2list, String keyid3list, String paramlistidlist, String paramlistversionidlist, String variantidlist, String datasetlist, boolean propsMatch)` | Create a new DS RSet with lock |
| `int` | `createLockedRSetDS(String sdcid, String keyid1list, String keyid2list, String keyid3list, String paramlistidlist, String paramlistversionidlist, String variantidlist, String datasetlist, boolean propsMatch, StringHolder rsetidholder)` | Deprecated. |
| `String` | `createRSet(String sdcid, String keyid1list, String keyid2list, String keyid3list)` | Create a new RSet with no lock |
| `String` | `createRSet(String sdcid, String keyid1list, String keyid2list, String keyid3list, boolean viewHiddenRecord, int bypassSecurityCode)` | Create a new RSet with no lock |
| `int` | `createRSet(String sdcid, String keyid1list, String keyid2list, String keyid3list, StringHolder rsetidholder)` | Deprecated. |
| `String` | `createRSetDS(String sdcid, String keyid1list, String keyid2list, String keyid3list, String paramlistidlist, String paramlistversionidlist, String variantidlist, String datasetlist, boolean propsMatch)` | Create a new DS RSet with lock |
| `String` | `createRSetDS(String sdcid, String keyid1list, String keyid2list, String keyid3list, String paramlistidlist, String paramlistversionidlist, String variantidlist, String datasetlist, boolean propsMatch, boolean populateBoth, boolean calcexpand)` | Create a new DS RSet with lock |
| `int` | `createRSetDS(String sdcid, String keyid1list, String keyid2list, String keyid3list, String paramlistidlist, String paramlistversionidlist, String variantidlist, String datasetlist, boolean propsMatch, boolean populateBoth, boolean calcexpand, StringHolder rsetidholder)` | Deprecated. |
| `int` | `createRSetDS(String sdcid, String keyid1list, String keyid2list, String keyid3list, String paramlistidlist, String paramlistversionidlist, String variantidlist, String datasetlist, boolean propsMatch, StringHolder rsetidholder)` | Deprecated. |
| `String` | `createRSetQ(String sdcid, String queryid, String[] params)` | Create a new RSet with no lock |
| `String` | `createRSetQ(String sdcid, String queryid, String param1, String param2, String param3, String param4, String param5)` | Create a new RSet with no lock |
| `int` | `createRSetQ(String sdcid, String queryid, String param1, String param2, String param3, String param4, String param5, StringHolder rsetidholder)` | Deprecated. |
| `String` | `createRSetWI(String sdcid, String keyid1list, String keyid2list, String keyid3list, String workitemidlist, String workiteminstancelist, boolean populateBoth)` | Create a new WI RSet |
| `String` | `getAllDSRSet(String sdcid, String keyid1list, String keyid2list, String keyid3list)` | Create a new DS RSet for the SDIs. |
| `boolean` | `isGlobalLock()` | — |
| `String` | `lockRSet(String rsetid)` | Applys a lock to an existing rset |
| `int` | `lockRSet(StringHolder rsetidholder)` | Deprecated. |
| `boolean` | `setGlobalLock(boolean lock)` | — |
| `void` | `touchRSet(String rsetid)` | Pings a locked rset to keep the lock alive |

### 27.5 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 28. HttpProcessor — Class Reference

**Full name:** `sapphire.accessor.HttpProcessor`
**Declaration:** `public class HttpProcessor extends com.labvantage.sapphire.BaseAccessor`

Supports TagManager access and related functions

### 28.3 Constructors

```java
HttpProcessor(File rakFile,
String connectionid)
// Constructor for a client application with an existing connection
HttpProcessor(PageContext pageContext)
// Constructor for a client web application with a page Context and existing connectionid
HttpProcessor(String connectionid)
// Constructor for creating an accessor from components
HttpProcessor(String nameserverlist,
String connectionid)
// Deprecated.
```

### 28.4 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|

### 28.5 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 29. UtilityProcessor — Class Reference

**Full name:** `sapphire.accessor.UtilityProcessor`
**Declaration:** `public class UtilityProcessor extends com.labvantage.sapphire.BaseAccessor`

Supports calcs and other utility access  functions

### 29.3 Constructors

```java
UtilityProcessor()
// Default constructor
UtilityProcessor(File rakFile)
// Constructor for a client application with an existing connection
UtilityProcessor(File rakFile,
String connectionid)
// Constructor for a client application with an existing connection
UtilityProcessor(PageContext pageContext)
// Constructor for a client web application with a page Context and existing connectionid
UtilityProcessor(String connectionid)
// Constructor for creating an accessor from components
```

### 29.4 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|
| `BigDecimal` | `convertUnits(BigDecimal number, String fromUnit, String toUnit)` | Unit conversion util |
| `String` | `evaluate(String expression, HashMap params)` | Expression evaluator util |

### 29.5 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 30. Attachment — Class Reference

**Full name:** `sapphire.attachment.Attachment`
**Declaration:** `public class Attachment extends java.lang.Object implements java.io.Serializable, com.labvantage.sapphire.gwt.shared.JSONable`

Class to represent an sdi attachment record including its type, file details, storage details and data.
This class can then be used to feed in and out of the AttachmentProcessor methods to get, add and edit the attachment.

### 30.2 Public Fields and Constants

| Name | Type | Description |
|---|---|---|
| `TYPE_FORMATTEDTEXT` | `static String` | — |
| `TYPE_LINKEDREFERENCE` | `static String` | — |
| `TYPE_PLAINTEXT` | `static String` | — |
| `TYPE_REFERENCE` | `static String` | — |
| `TYPE_STORE` | `static String` | — |
| `TYPE_UPLOADANDREFERENCE` | `static String` | — |
| `TYPE_UPLOADANDSTORE` | `static String` | — |
| `TYPE_URL` | `static String` | — |

### 30.3 Constructors

```java
Attachment()
```

### 30.4 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|
| `protected boolean` | `checkHash()` | — |
| `protected boolean` | `checkHash(long hash)` | — |
| `static String` | `evaluateFileNameExpressions(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentnum, ConnectionInfo connectionInfo, String input, Attachment attachment)` | — |
| `static String` | `evaluateFileNameExpressions(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentnum, int auditsequence, ConnectionInfo connectionInfo, String input, Attachment attachment)` | — |
| `void` | `generateThumbnail(String connectionid)` | Will generate the thumbnail and populate the Thumbnail property of the attachment object. |
| `String` | `getAdditionalColumn(String key, String defaultValue)` | Will return the value of the column provided. |
| `protected PropertyList` | `getAdditionalColumns()` | — |
| `protected boolean` | `getAllowLocalCache()` | — |
| `static Attachment` | `getAttachment(DataSet sdiAttachmentData, int sdiAttRow, String connectionId)` | Used to fetch a new Attachment object based off a data set of sdiattachment records. |
| `static Attachment` | `getAttachment(DataSet sdiAttachmentData, String sdcid, String keyid1, String keyid2, String keyid3, int attachmentNum, String connectionId)` | Get an attachment object based off attachment data |
| `static Attachment` | `getAttachment(PropertyList propertyList, String connectionId)` | Used to fetch a new Attachment object based off a property list provided. |
| `static Attachment` | `getAttachment(String sdcid, String keyid1, String keyid2, String keyid3)` | Used to fetch a new black attachment object which can then be fed into the Attachment Processor. |
| `static Attachment` | `getAttachment(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentnum)` | Used to fetch a new black attachment object which can then be fed into the Attachment Processor. |
| `static Attachment` | `getAttachment(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentNum, QueryProcessor queryProcessor, String connectionId)` | Get an attachment object based on sdcid,keyid1, 2 and 3 |
| `String` | `getAttachmentClass()` | Returns the attachment class |
| `int` | `getAttachmentNum()` | Returns the attachment number |
| `protected BaseAttachmentRepository` | `getAttachmentRepository(SapphireConnection sapphireConnection, PropertyList attachmentPolicy)` | Will return the Attachment Repository object based of the Attachment object. |
| `protected BaseAttachmentRepository` | `getAttachmentRepository(SapphireConnection sapphireConnection, PropertyList attachmentPolicy, boolean defaultToCompatability)` | Will return the Attachment Repository object based of the Attachment object. |
| `protected BaseAttachmentRepository` | `getAttachmentRepository(SapphireConnection sapphireConnection, PropertyList attachmentPolicy, boolean defaultToCompatability, Attachment basedOn)` | Will return the Attachment Repository object based of the Attachment object. |
| `protected String` | `getAttachmentRepositoryId()` | — |
| `protected String` | `getAttachmentRepositoryNodeId()` | — |
| `protected AttachmentRule` | `getAttachmentRule()` | — |
| `AttachmentType` | `getAttachmentType()` | Will return the attachment type of the attachment |
| `Blob` | `getBlob()` | Returns the blob value. |
| `protected Path` | `getCachedPath(String databaseid, boolean explodedzip)` | — |
| `String` | `getClob()` | Used to get the clob column on the attachment. |
| `byte[]` | `getData()` | Gets the data in the attachment as a byte array. |
| `byte[]` | `getData(int maxsizelimit)` | Gets the data in the attachment as a byte array. |
| `protected long` | `getDataHash()` | — |
| `String` | `getDescription()` | Returns the attachment description |
| `String` | `getFilename()` | Returns the file name of the attachment. |
| `InputStream` | `getInputStream()` | Gets the input stream for adding/updating an attachment We recommend using this over getting the byte array or blob of the attachment for efficiency and memory management |
| `String` | `getKeyId1()` | Get the key id 1 |
| `String` | `getKeyId2()` | Get the key id 2 |
| `String` | `getKeyId3()` | Get the key id 3 |
| `int` | `getLinkAttachmentNum()` | Gets the Link Attachment Number if the attachment is a linked attachment created from a copy down. |
| `String` | `getLinkKeyId1()` | Gets the Link Key Id 1 if the attachment is a linked attachment created from a copy down. |
| `String` | `getLinkKeyId2()` | Gets the Link Key Id 2 if the attachment is a linked attachment created from a copy down. |
| `String` | `getLinkKeyId3()` | Gets the Link Key Id 3 if the attachment is a linked attachment created from a copy down. |
| `String` | `getLinkSdcid()` | Gets the Link SDC Id if the attachment is a linked attachment created from a copy down. |
| `protected Path` | `getLocalFile()` | — |
| `boolean` | `getLockAttachment()` | Returns true if the attachment is locked from update as part of version control. |
| `String` | `getOleClass()` | Gets the OLE class. |
| `protected String` | `getParentDepartment()` | — |
| `String` | `getRepositoryId()` | Get the external id for the external repository. |
| `String` | `getSDCId()` | Get the SDC Id |
| `protected String` | `getSDIAttachmentId()` | — |
| `long` | `getSize()` | Gets the size of the data in the attachment |
| `String` | `getSourceFilename()` | Returns the source filename of the attachment. |
| `String` | `getTempId()` | Returns the Temporary Id for the SDI Temp record Note this is used internally when uploading an attachment prior to save on a maintenance page. |
| `String` | `getThumbnailImage()` | Gets the thumnail image of the attachment. |
| `protected boolean` | `getTriggerBusinessRule()` | — |
| `String` | `getType()` | Get the attachment type flag. |
| `String` | `getUploadTo()` | — |
| `String` | `getUrl()` | Returns the URL if a URL attachment. |
| `boolean` | `hasData()` | Returns true if the attachment has data in it and that data is available to read. |
| `boolean` | `isByReference()` | Will return true to indicate that the file name in this attachment is a reference attachment. |
| `boolean` | `isCompressed()` | Returns true if the attachment was compressed in storage. |
| `boolean` | `isEncrypted()` | Returns true if the attachment was encrypted in storage. |
| `protected boolean` | `isEncryptionRequired(PropertyList attachmentPolicy, ConfigurationProcessor cp)` | Will return back if encryption is requied based on the attachment rules |
| `boolean` | `isHashed()` | Returns true if the data inside the attachment has been hashed Internally this returns trus if the datahash of the attachment is not 0. |
| `boolean` | `isInvalidHash()` | Returns true if hashing failed and the attachment has an invalid has. |
| `protected boolean` | `isZippingRequired(PropertyList attachmentPolicy, ConfigurationProcessor cp)` | Will return back if Zipping is requied based on the attachment rules |
| `protected void` | `loadLocalFile(Path p)` | — |
| `static String` | `replaceDateTokens(String filename)` | — |
| `static String` | `replaceDbTokens(String filename, String sdcid, String keyid1, String keyid2, String keyid3, Attachment attachment, ConnectionInfo connectionInfo)` | — |
| `static String` | `replaceInvalidChars(String tokens, String replacement)` | — |
| `protected Path` | `saveLocalFile(boolean cache, String databaseid, boolean checkHash, boolean generateHash, Path usepath)` | — |
| `protected void` | `setAdditionalColumn(String key, String value)` | — |
| `void` | `setAdditionalColumns(DataSet sdiAttachmentData, int sdiAttRow)` | Will set any additional columns found in the provided dataset against this attachment object. |
| `void` | `setAdditionalColumns(PropertyList additionalColumns, String connectionid)` | Will set any additional columns found in the provided property list against this attachment object. |
| `protected void` | `setAllowLocalCache(boolean allowlocalcache)` | — |
| `void` | `setAttachment(DataSet sdiAttachmentData, int sdiAttRow)` | Used to set an attachment object based off a data set of sdiattachment records. |
| `void` | `setAttachmentClass(String attclass)` | Set the attachment class for the attachment. |
| `protected void` | `setAttachmentNum(int attnum)` | — |
| `protected void` | `setAttachmentRepositoryId(String attachmentRepositoryId)` | — |
| `protected void` | `setAttachmentRepositoryNodeId(String attachmentRepositoryNodeId)` | — |
| `void` | `setAttachmentType(AttachmentType attachmentType)` | Used to set the type of attachment. |
| `void` | `setBlob(Blob blob)` | Sets the blob object on the attachment. |
| `void` | `setByReference(boolean byReference)` | Use this will a parameter value of true to indicate that the file name in this attachment is a reference attachment. |
| `void` | `setClob(String clob)` | Used to set the clob column on the attachment. |
| `void` | `setCompressed(boolean compressedFlag)` | Set to true if you wish to force compression. |
| `void` | `setData(byte[] data)` | Sets the file data for the attachment. |
| `void` | `setDescription(String description)` | Sets the description of the attachment. |
| `void` | `setEncrypted(boolean encryptedFlag)` | Set to true to encrypt the attachment and force this over the rules evaluation. |
| `void` | `setFilename(String filename)` | Sets the filename of the attachment. |
| `void` | `setInputStream(InputStream in)` | Sets the input stream for adding/updating a new attachment We recommend using this over setting the byte array or blob of the attachment for efficiency and memory management |
| `protected void` | `setInvalidHash(boolean invalidHash)` | — |
| `protected void` | `setKeyId1(String keyid1)` | — |
| `protected void` | `setKeyId2(String keyid2)` | — |
| `protected void` | `setKeyId3(String keyid3)` | — |
| `protected void` | `setLinkSDI(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentnum)` | — |
| `void` | `setLockAttachment(boolean lockAttachment)` | Set to true to lock the attachment for update from others and to allow version control. |
| `void` | `setOleClass(String oleClass)` | Sets the OLE class. |
| `protected void` | `setParentDepartment(String parentDepartment)` | — |
| `void` | `setRepositoryId(String repositoryId)` | Set the external id for the external repository. |
| `protected void` | `setSDCId(String sdcid)` | — |
| `protected void` | `setSDIAttachmentId(String sdiattachmentid)` | — |
| `void` | `setSize(long size)` | — |
| `void` | `setSourceFilename(String sourcefilename)` | Sets the source filename of the attachment. |
| `void` | `setTempId(String tempid)` | Sets the temporary id for the sdi temp record. |
| `void` | `setThumbnailImage(String thumbnailimage)` | Sets the thumnail image of the attachment. |
| `protected void` | `setTriggerBusinessRule(boolean triggerBusinessRule)` | — |
| `protected void` | `setType(String type)` | — |
| `void` | `setUploadTo(String uploadTo)` | — |
| `protected void` | `setUrl(String url)` | — |
| `String` | `toJSONString()` | This returns a JSON representation of the attachment object. |
| `PropertyList` | `toPropertyList(boolean returnData, boolean thumbnail, boolean additionalColumns, String connectionid)` | Will convert the Attachment object to a property list version where all variables become root properties and additional columns also become root properties. |
| `void` | `toPropertyList(PropertyList properties, boolean returnData, boolean thumbnail, boolean returnAdditionalColumns, String connectionid)` | Will convert the Attachment object to a property list version where all variables become root properties and additional columns also become root properties. |

### 30.5 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 31. ActionBlock — Class Reference

**Full name:** `sapphire.util.ActionBlock`
**Declaration:** `public class ActionBlock extends java.lang.Object implements java.io.Serializable`

Allows to create and maintain a group of actions and their properties.

### 31.2 Public Fields and Constants

| Name | Type | Description |
|---|---|---|
| `COMMAND_ACTION` | `static String` | — |
| `COMMAND_ACTIONBLOCK` | `static String` | — |
| `COMMAND_UNKNOWN` | `static String` | — |

### 31.3 Constructors

```java
ActionBlock()
// Default contructor
ActionBlock(JSONObject jsonObject)
// Contructor based on JSONObject
ActionBlock(String xml)
// Contructor based on xml definition
ActionBlock(String name,
String xml)
// Contructor based on a name (for performance monitoring) and an xml definition
```

### 31.4 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|
| `void` | `addActionBlockCommand(ActionBlock actionBlock)` | Internal method. |
| `void` | `addBlockPropertyCommand(String propertyid, String value)` | Internal method. |
| `void` | `addChildCaseActionBlock(String caseValue, ActionBlock childActionBlock)` | Internal method. |
| `void` | `addReturnPropertyCommand(String propertyid, String value)` | Internal method. |
| `void` | `endProcessing()` | Internal method. |
| `Action` | `getAction(String name)` | Internal method. |
| `ActionBlock` | `getActionBlock(int index)` | Internal method. |
| `String` | `getActionClass(int actionindex)` | Returns the actionclass for the given index of an acion in the block |
| `String` | `getActionClass(String name)` | Returns the action class for the given action block name |
| `int` | `getActionCount()` | Returns the number of actions defined in the block. |
| `String` | `getActionid(int actionindex)` | Returns the actionid for the given index of an acion in the block |
| `String` | `getActionid(String name)` | Returns the actionid for the given action block name |
| `String` | `getActionName(int actionindex)` | Returns the name of an action for the given index |
| `HashMap` | `getActionProperties(int actionindex)` | Gets the property values for the given Action in the Block. |
| `HashMap` | `getActionProperties(String name)` | Gets the property values for the given Action in the Block. |
| `String` | `getActionProperty(int actionindex, String propertyid)` | Gets a property value for the given Action in the Block. |
| `String` | `getActionProperty(String name, String propertyid)` | Gets a property value for the given Action in the Block. |
| `String` | `getActionTest(int actionindex)` | Returns the test for the given index of an acion in the block |
| `String` | `getAsyncDueDt()` | Returns the due date for action block when executed asynchronously |
| `HashMap` | `getBlockProperties()` | Returns a pointer to all of the actionblock properties |
| `String` | `getBlockProperty(String propertyid)` | Returns a single block property. |
| `String` | `getCaseValue()` | — |
| `ActionBlock` | `getChildCaseActionBlock(String caseValue)` | — |
| `Object` | `getCommand(int index)` | Internal method. |
| `int` | `getCommandCount()` | Internal method. |
| `String` | `getDebugLog()` | Returns the debug log |
| `List<String>` | `getDistinctActionClasses()` | — |
| `List<String>` | `getDistinctActions()` | — |
| `int` | `getErrorAction()` | Returns the current error action number (-1 if no error action) |
| `String` | `getErrorActionName()` | Returns the current error action name (empty string if no error action) |
| `ErrorHandler` | `getErrorHandler()` | Internal method. |
| `HashMap` | `getGroovyBindings()` | Returns a pointer to all of the actionblock properties |
| `String` | `getName()` | Returns the name assigned to the action block |
| `HashMap` | `getReturnProperties()` | Returns a pointer to all of the actionblock properties |
| `String` | `getReturnProperty(String propertyid)` | Returns a single block property. |
| `String` | `getTest()` | Internal method. |
| `String` | `getTest(String name)` | Returns the test for the given action block name |
| `String` | `getTestName()` | Returns the label assigned to the action block test condition |
| `String` | `getTodolistid()` | — |
| `String` | `getVersionid(int actionindex)` | Returns the versionid for the given index of an acion in the block |
| `String` | `getVersionid(String name)` | Returns the versionid for the given action block name |
| `boolean` | `hasChildCaseActionBlock()` | — |
| `boolean` | `isDebugMode()` | Whether the actionblock is in debug mode |
| `void` | `log(String message)` | Internal method. |
| `void` | `setAction(String name, String actionid, String versionid)` | Creates a new action inside the action block and initialize its properties. |
| `void` | `setAction(String name, String actionid, String versionid, HashMap properties)` | Creates a new action inside the action block and initialize its properties. |
| `void` | `setAction(String name, String actionid, String versionid, PropertyList properties)` | Creates a new action inside the action block and initialize its properties. |
| `void` | `setAction(String name, String test, String actionClass, String actionid, String versionid, HashMap hmProperties)` | Internal method. |
| `void` | `setAction(String name, String test, String actionClass, String actionid, String versionid, PropertyList properties)` | Internal method. |
| `void` | `setActionBlockProperty(String name, String blockpropertyid, String actionpropertyid)` | Sets a block property based on an action output property. |
| `void` | `setActionClass(String name, String actionClass)` | Creates a new action inside the action block and initialize its properties. |
| `void` | `setActionClass(String name, String actionClass, HashMap properties)` | Creates a new action inside the action block and initialize its properties. |
| `void` | `setActionClass(String name, String actionClass, PropertyList properties)` | Creates a new action inside the action block and initialize its properties. |
| `void` | `setActionLabel(String actionName, String actionlabel)` | Internal method. |
| `void` | `setActionProperties(int actionindex, HashMap properties)` | Sets the property values for the given Action in the Block. |
| `void` | `setActionProperties(int actionindex, PropertyList properties)` | Sets the property values for the given Action in the Block. |
| `void` | `setActionProperties(String name, HashMap properties)` | Sets the property values for the given Action in the Block. |
| `void` | `setActionProperties(String name, PropertyList properties)` | Sets the property values for the given Action in the Block. |
| `void` | `setActionProperty(String name, String propertyid, String value)` | Sets a property value for the given Action in the Block. |
| `void` | `setAsyncDueDt(String asyncDueDt)` | Sets a due date for action blocks executed asynchronously |
| `void` | `setBlockProperties(HashMap properties)` | Sets a collection of properties for the action block. |
| `void` | `setBlockProperties(PropertyList properties)` | Sets a collection of properties for the action block. |
| `void` | `setBlockProperty(String propertyid, String value)` | Sets a single property for the block. |
| `void` | `setCaseValue(String caseValue)` | — |
| `void` | `setDebugLog(String log)` | Internal method. |
| `void` | `setDebugMode(boolean debugMode)` | Put the actionblock into Debug mode |
| `void` | `setErrorAction(int errorAction)` | Sets the current error action number (-1 if no error action) |
| `void` | `setGroovyBindings(HashMap bindings)` | Sets a collection of binding maps for the Groovy expressions |
| `void` | `setJSONObject(JSONObject jsonObject)` | Populates an action block based on a xml definition |
| `void` | `setName(String name)` | Populates an action block based on a xml definition |
| `void` | `setReturnProperties(HashMap properties)` | Sets a collection of properties for the action block. |
| `void` | `setReturnProperties(PropertyList properties)` | Sets a collection of properties for the action block. |
| `void` | `setReturnProperty(String propertyid, String value)` | Sets a single property for the block. |
| `void` | `setTest(String test)` | Sets a conditional test for the action block. |
| `void` | `setTestName(String testName)` | Sets a label for the conditional test for the action block. |
| `void` | `setTodolistid(String todolistid)` | — |
| `void` | `setXML(String xml)` | Populates an action block based on a xml definition |
| `void` | `startProcessing()` | Internal method. |
| `void` | `synchronizeProperties(ActionBlock returnactionblock)` | Internal method. |
| `JSONObject` | `toJSONObject()` | — |
| `String` | `toJSONString()` | Public toJSON |
| `String` | `toString()` | Public toString |
| `String` | `toXML()` | Public toXML |

### 31.5 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 32. SDIRequest — Class Reference

**Full name:** `sapphire.util.SDIRequest`
**Declaration:** `public class SDIRequest extends com.labvantage.sapphire.gwt.shared.util.SDIRequest implements java.io.Serializable, com.labvantage.sapphire.gwt.shared.JSONable`

Holder class used to pass request information to the SDIProcessor accessor.
Typically, an SDIRequest class will be instanciated and the appropriate "setter" methods called to indicate
which SDIs are required and what information about them is needed. The SDIRequest class is then sent to the SDIProcessor accessor
which returns an instance of a SDIData class that contains all the requested data.

### 32.3 Constructors

```java
SDIRequest()
```

### 32.4 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|
| `SDIRequest` | `findSDIRequest(String sdcid)` | — |
| `SDIRequest` | `getSDIRequest(String sdiRequestName)` | Returns the SDIRequest with this name |
| `SDIRequest[]` | `getSDIRequests()` | Returns the list of SDIRequests that have been requested. |
| `void` | `setSDIList(SDIList sdiList)` | — |
| `String` | `toJSONString()` | — |

### 32.5 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 33. SDIList — Class Reference

**Full name:** `sapphire.util.SDIList`
**Declaration:** `public class SDIList extends java.lang.Object implements com.labvantage.sapphire.gwt.shared.JSONable, java.io.Serializable`

SDIList
- holds and manages a list of SDIs

### 33.3 Constructors

```java
SDIList()
```

### 33.4 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|
| `int` | `addSDI(String keyid1)` | Adds a single keyed SDI to the list |
| `int` | `addSDI(String keyid1, String keyid2, String keyid3)` | Adds a multi-keyed SDI to the list |
| `int` | `addSDIList(String keyid1)` | Add a list of single key SDIs to the list |
| `int` | `addSDIList(String keyid1, String keyid2, String keyid3)` | Add a list of multi-key SDIs to the list |
| `int` | `addSDIList(String keyid1, String keyid2, String keyid3, String delimeter)` | Add a list of multi-key SDIs to the list |
| `String` | `getKeyid(int index)` | — |
| `String` | `getKeyid1()` | Returns a semi-colon separated list of keyid1 |
| `String` | `getKeyid1(int index)` | Returns a specific keyid1 in the list |
| `String` | `getKeyid2()` | Returns a semi-colon separated list of keyid2 |
| `String` | `getKeyid2(int index)` | Returns a specific keyid2 in the list |
| `String` | `getKeyid3()` | Returns a semi-colon separated list of keyid3 |
| `String` | `getKeyid3(int index)` | Returns a specific keyid3 in the list |
| `int` | `getListIndex(String keyid1, String keyid2, String keyid3)` | Returns the index in the list of a specific SDI |
| `String` | `getSdcid()` | Returns the sdcid for the list of SDIs |
| `String` | `getSDIAttribute(int index, String attributeid)` | Returns a specific SDI attribute value |
| `String` | `getSDIAttributeList(String attributeid)` | Returns a list of attribute values for the SDIs in the list |
| `String` | `getSDIAttributeList(String attributeid, String delimeter)` | Returns a list of attribute values for the SDIs in the list |
| `String` | `getSDIList()` | — |
| `String` | `getSDIList(KeyId keyId)` | Returns a semi-colon separated list of keyid |
| `String` | `getSDIList(KeyId keyId, String delimeter)` | Returns a semi-colon separated list of keyid |
| `String` | `getSDIList(String delimeter)` | — |
| `void` | `removeSDI(int index)` | Removes a specific SDI from the list |
| `boolean` | `removeSDI(String keyid1)` | Removes a specific SDI from the list |
| `boolean` | `removeSDI(String keyid1, String keyid2, String keyid3)` | Removes a specific SDI from the list |
| `void` | `setAllowDups(boolean allowDups)` | Determines if duplicate SDIs can exist - default is false |
| `void` | `setJSONObject(JSONObject jsonObject)` | Sets up the list with the JSON object definition |
| `void` | `setSdcid(String sdcid)` | Sets the sdcid for the lists of SDIs |
| `void` | `setSDIAttribute(int index, String attributeid, String attributevalue)` | Sets a specific SDI attribute value |
| `int` | `size()` | Returns the number of SDIs in the object |
| `DataSet` | `toDataSet()` | — |
| `JSONObject` | `toJSONObject()` | Returns the list as a JSON object |
| `String` | `toJSONString()` | Returns the list as a JSON String |
| `String` | `toString()` | — |
| `String` | `toString(String delimeter)` | — |
| `String` | `toText()` | — |

### 33.5 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 34. ResultDataGrid — Class Reference

**Full name:** `sapphire.util.ResultDataGrid`
**Declaration:** `public class ResultDataGrid extends com.labvantage.sapphire.BaseCustom`

ResultGrid Save

### 34.3 Constructors

```java
ResultDataGrid(ConnectionInfo connectionInfo)
ResultDataGrid(ConnectionInfo connectionInfo,
File rakFile)
```

### 34.4 Method Reference

| Return Type | Method Signature | Description |
|---|---|---|
| `int` | `addResult(HashMap<CoreColumns,String> resultFields)` | — |
| `int` | `addResult(HashMap<CoreColumns,String> resultFields, HashMap<String,String> additionalFields)` | — |
| `int` | `addRow()` | — |
| `String` | `getColumnValues(CoreColumns column, String delimeter)` | — |
| `protected DataSet` | `getDataSet()` | — |
| `String` | `getExecutionLog()` | — |
| `protected ResultGridOptions` | `getOptions()` | — |
| `int` | `getRowCount()` | — |
| `String` | `getValue(int row, CoreColumns column)` | — |
| `String` | `getValue(int row, CoreColumns column, String defaultValue)` | — |
| `void` | `save()` | — |
| `void` | `save(ResultGridOptions options)` | — |
| `protected void` | `setDataSet(DataSet grid)` | — |
| `protected void` | `setOptions(ResultGridOptions resultGridOptions)` | — |
| `void` | `setString(int row, String column, String value)` | — |
| `void` | `setValue(int row, CoreColumns column, String value)` | — |

### 34.5 Usage Example and Patterns

```java
package sapphire.custom.myapp;

import sapphire.action.BaseAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MyAction extends BaseAction {

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        // read inputs
        String sdcid  = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");

        // access DB via injected members
        QueryProcessor qp = getQueryProcessor();
        // ... business logic ...

        // write outputs
        properties.setProperty("result", "some value");
    }
}
```

---


---


---


---


---

## 35. BaseCustom Inherited Accessors — Quick Reference

All extensions that inherit `BaseCustom` (via `BaseAction`, `BaseSDCRules`, `BaseAttachmentHandler`) can call these methods without constructing processor instances:

| Method | Returns | Purpose |
|---|---|---|
| `getQueryProcessor()` | `QueryProcessor` | Execute SQL / named queries |
| `getActionProcessor()` | `ActionProcessor` | Invoke LV actions |
| `getSDCProcessor()` | `SDCProcessor` | SDC metadata |
| `getSDIProcessor()` | `SDIProcessor` | Fetch SDI records |
| `getSequenceProcessor()` | `SequenceProcessor` | Sequences & UUIDs |
| `getConfigurationProcessor()` | `ConfigurationProcessor` | Policies & profile props |
| `getTranslationProcessor()` | `TranslationProcessor` | I18n translations |
| `getHttpProcessor()` | `HttpProcessor` | HTTP/tag manager |
| `getConnectionProcessor()` | `ConnectionProcessor` | Connection management |
| `getDAMProcessor()` | `DAMProcessor` | DAM operations |
| `getConnectionid()` / `getConnectionId()` | `String` | Current connection ID |
| `getDepartmentList()` | — | User department list |
| `getAccessorErrorIds()` | — | Accumulated error IDs |
| `getAccessorErrorMsgs()` | — | Accumulated error messages |
| `setConnectionId(String)` | `void` | Override connection |
| `setLanguage(String)` | `void` | Override language |
| `setRakFile(File)` | `void` | Set remote access key |

---

---

---

---

---

## 36. API Usage by Extension Type

This section details how the Java Public API is applied across the four main LabVantage extension types. Each subsection provides the architectural context, registration details, development rules, and a complete, production-grade example.

---

### 36.1 Custom Actions (`BaseAction`)

#### Architectural Context
An **LV Action** is the primary mechanism for implementing custom business logic. It is called explicitly from workflows, event plans, limit rules, or programmatically via an `ActionProcessor`. It receives input properties, executes logic, and returns output properties.

#### Rules & Standards
1. **Extend Base Class:** Must extend `sapphire.action.BaseAction`.
2. **Override Entry Point:** Implement `public void processAction(PropertyList properties) throws SapphireException`.
3. **Database Flag:** Always override `public boolean isDatabaseRequired()` to return `false` if the action does not perform database operations. This avoids opening unnecessary database transactions and significantly improves performance.
4. **Error Handling:** Avoid catching exceptions without logging or wrapping. Throw `SapphireException` with descriptive UPPER_SNAKE_CASE error codes (e.g., `MISSING_INPUT`, `RECORD_NOT_FOUND`).
5. **Output Conventions:** Use "Yes"/"No" string values for boolean properties (e.g., `properties.setProperty("isvalid", "Yes")`) to remain consistent with LabVantage system actions. Always stringify numbers (e.g., `String.valueOf(count)`).

#### Complete Production-Grade Example
```java
package com.client.actions;

import sapphire.action.BaseAction;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.SapphireException;
import sapphire.util.SafeSQL;

/**
 * Custom action to validate a sample status and insert an audit trail review record.
 * Inputs: sdcid (String), keyid1 (String), comments (String)
 * Outputs: status (String), processed (String - Yes/No)
 */
public class RecordSampleReview extends BaseAction {

    @Override
    public boolean isDatabaseRequired() {
        return true; // Requires database connection for query and insert
    }

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");
        String comments = properties.getProperty("comments");
        String sysUser = this.connectionInfo.getSysUserId();

        // 1. Validate inputs
        if (sdcid == null || sdcid.trim().isEmpty()) {
            throw new SapphireException("MISSING_SDCID", "Property 'sdcid' is required.");
        }
        if (keyid1 == null || keyid1.trim().isEmpty()) {
            throw new SapphireException("MISSING_KEYID1", "Property 'keyid1' is required.");
        }

        this.logger.info("RecordSampleReview: Starting review for sample " + keyid1);

        try {
            // 2. Query current sample status
            QueryProcessor qp = this.getQueryProcessor();
            DataSet ds = qp.getPreparedSqlDataSet(
                "SELECT statusid, releasedflag FROM s_sample WHERE keyid1 = ?",
                new Object[]{keyid1}
            );

            if (ds.getRowCount() == 0) {
                throw new SapphireException("SAMPLE_NOT_FOUND", "Sample with ID " + keyid1 + " does not exist.");
            }

            String currentStatus = ds.getValue(0, "statusid", "");
            String releasedFlag = ds.getValue(0, "releasedflag", "N");

            if ("Released".equalsIgnoreCase(currentStatus) || "Y".equals(releasedFlag)) {
                throw new SapphireException("INVALID_STATUS", "Sample is already released and cannot be reviewed.");
            }

            // 3. Write review record using SafeSQL for parameterization
            SafeSQL insertSQL = new SafeSQL();
            StringBuffer ins = new StringBuffer();
            ins.append("INSERT INTO s_sample_review (keyid1, reviewedby, comments, reviewdate) VALUES (");
            ins.append(insertSQL.addVar(keyid1)); ins.append(", ");
            ins.append(insertSQL.addVar(sysUser)); ins.append(", ");
            ins.append(insertSQL.addVar(comments != null ? comments : "")); ins.append(", SYSDATE)");

            int inserted = this.database.executePreparedUpdate(ins.toString(), insertSQL.getValues());
            if (inserted < 1) {
                throw new SapphireException("INSERT_FAILED", "Failed to insert sample review log record.");
            }

            // 4. Update the sample record
            SafeSQL updateSQL = new SafeSQL();
            StringBuffer upd = new StringBuffer();
            upd.append("UPDATE s_sample SET reviewflag = 'Y', modifiedby = ");
            upd.append(updateSQL.addVar(sysUser));
            upd.append(" WHERE keyid1 = ");
            upd.append(updateSQL.addVar(keyid1));

            this.database.executePreparedUpdate(upd.toString(), updateSQL.getValues());

            // 5. Populate output properties
            properties.setProperty("status", "REVIEWED");
            properties.setProperty("processed", "Yes");
            
            this.logger.info("RecordSampleReview: Successfully completed for sample " + keyid1);

        } catch (SapphireException e) {
            throw e; // Re-throw SapphireExceptions as-is
        } catch (Exception e) {
            this.logger.error("Unexpected error in RecordSampleReview", e);
            throw new SapphireException("UNEXPECTED_ERROR", e.getMessage());
        }
    }
}
```

---

### 36.2 SDC Business Rules (`BaseSDCRules`)

#### Architectural Context
An **SDC Rule** is a Java class that hooks directly into the data lifecycle of a specific SDC. SDC Rules are triggered **automatically** by the framework during create, edit, delete, release, or other data operations. They are the ideal place for data validation, field defaulting, or database-level cascading integrity.

#### Rules & Standards
1. **Extend Base Class:** Must extend `sapphire.action.BaseSDCRules`.
2. **Naming Convention:** The class name must match the SDC ID exactly (case-sensitive). For SDC `SampleType`, the class must be named `SampleType.java`.
3. **Registration:** Set the profile property `customrulesjavapackage` to the package containing the rules class (e.g. `com.client.rules`).
4. **Lifecycle Hooks:** Implement only the needed hooks (`preAdd`, `preEdit`, `postAdd`, `postEdit`, `preDelete`, `postDelete`, `preApprove`, `postApprove`, `preRelease`, `postRelease`).
5. **Change Detection:** Implement change detection inside `preEdit` by overriding `requiresBeforeEditImage()` to return `true`, then fetching the pre-edit snapshot via `getBeforeEditImage()`.
6. **Error Reporting:** To reject operations, raise validation/confirm dialogs by calling `setError` or `throwError` using the constants:
   - `TYPE_VALIDATION` (hard blocker; aborts transaction)
   - `TYPE_CONFIRM` (warning requiring user confirmation)
   - `TYPE_INFORMATION` (pop-up info dialog)

#### Complete Production-Grade Example
```java
package com.client.rules;

import sapphire.action.BaseSDCRules;
import sapphire.util.SDIData;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.SapphireException;
import java.sql.PreparedStatement;

/**
 * Custom rule class for the SampleType SDC.
 * Class name must exactly match SDC ID.
 */
public class SampleType extends BaseSDCRules {

    private static final String COL_PARENT = "parentsampletypeid";
    private static final String COL_SUBTYPE = "subtypeflag";
    private static final String COL_KEYID1 = "s_sampletypeid";

    @Override
    public boolean requiresBeforeEditImage() {
        return true; // Enable getBeforeEditImage() snapshot
    }

    @Override
    public void preAdd(SDIData sdiData, PropertyList list) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        validateAndSetSubType(primary);
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList list) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        
        // Example change detection: Verify if parent ID is modified
        SDIData beforeImage = getBeforeEditImage();
        if (beforeImage != null) {
            DataSet beforePrimary = beforeImage.getDataset("primary");
            if (beforePrimary.getRowCount() > 0 && primary.getRowCount() > 0) {
                String oldParent = beforePrimary.getValue(0, COL_PARENT, "");
                String newParent = primary.getValue(0, COL_PARENT, "");
                if (!oldParent.equals(newParent)) {
                    this.logger.info("SampleType parent changed from " + oldParent + " to " + newParent);
                }
            }
        }
        
        validateAndSetSubType(primary);
    }

    @Override
    public void postDelete(String rsetid, PropertyList list) throws SapphireException {
        String keyid1 = list.getProperty("keyid1");
        if (keyid1 != null && !keyid1.trim().isEmpty()) {
            // Replaces semicolons with single quotes for an IN clause (in case of bulk delete)
            String key = keyid1.replace(";", "','");
            try {
                int deleted = database.executeSQL(
                    "DELETE FROM s_preptypesampletypemap " +
                    "WHERE sourcesampletypeid IN ('" + key + "') " +
                    "   OR destsampletypeid  IN ('" + key + "')"
                );
                this.logger.info("SampleType postDelete: clean mapped records count = " + deleted);
            } catch (Exception e) {
                throw new SapphireException("DELETE_MAPPING_FAILED", e.getMessage());
            }
        }
    }

    private void validateAndSetSubType(DataSet primary) throws SapphireException {
        try {
            PreparedStatement childCheck = database.prepareStatement("subtypes",
                "SELECT s_sampletypeid FROM s_sampletype WHERE " + COL_PARENT + " = ?");
            PreparedStatement parentIsSubtype = database.prepareStatement("issubtype",
                "SELECT " + COL_SUBTYPE + " FROM s_sampletype WHERE " + COL_KEYID1 + " = ?");

            for (int i = 0; i < primary.getRowCount(); i++) {
                String parentId = primary.getValue(i, COL_PARENT, "");
                String typeId = primary.getValue(i, COL_KEYID1, "");

                if (parentId.equals(typeId) && !parentId.isEmpty()) {
                    setError("Parent Sample Type ID cannot be same as Sample Type ID", TYPE_VALIDATION);
                    return;
                }

                if (parentId.length() > 0) {
                    // Verify parent is not itself a subtype
                    parentIsSubtype.setString(1, parentId);
                    DataSet flagDs = new DataSet(parentIsSubtype.executeQuery());
                    if (flagDs.getRowCount() > 0 && "Y".equals(flagDs.getValue(0, COL_SUBTYPE, "N"))) {
                        throwError("Select a Parent SampleType which is not a SubType. " +
                                   "Following Parent SampleType is defined as SubType: " + parentId, 
                                   TYPE_VALIDATION);
                    }
                }
            }
        } catch (SapphireException e) {
            throw e;
        } catch (Exception e) {
            throw new SapphireException("VALIDATION_ERROR", e.getMessage());
        }
    }
}
```

---

### 36.3 Ajax Custom Handlers (`AjaxResponse` & `BaseAjaxRequest`)

#### Architectural Context
An **Ajax Request Handler** is a server-side component that responds asynchronously to browser requests without reloading the page. JavaScript sends JSON/form data and receives a structured JSON object containing data or execution status.

#### Rules & Standards
1. **Extend Base Class:** Must extend `sapphire.servlet.BaseAjaxRequest`.
2. **Override Entry Point:** Implement `public void processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext ctx) throws ServletException`.
3. **Return Type:** The response **must** be written out as a JSON string using an `AjaxResponse` instance.
4. **Method Dispatch Pattern:** Use a request parameter `method` to dispatch the request to private helper methods. This allows a single class to handle a family of related client-side operations.
5. **No System Admin Registration:** AJAX classes do not require registration in System Admin. The framework instantiates them at runtime using reflection with the fully-qualified class name provided to `sapphire.ajax.callClass()`.

#### Complete Production-Grade Example
```java
package com.client.ajax;

import sapphire.servlet.BaseAjaxRequest;
import sapphire.servlet.AjaxResponse;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.SapphireException;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * AJAX Handler class to process sample queries and assign locations asynchronously.
 * Invoked via JavaScript: sapphire.ajax.callClass("com.client.ajax.SampleAjaxHandler", {...})
 */
public class SampleAjaxHandler extends BaseAjaxRequest {

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) 
            throws ServletException {
        
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        
        try {
            // 1. Extract standard dispatch method parameter
            String method = ajaxResponse.getRequestParameter("method");
            if (method == null || method.trim().isEmpty()) {
                ajaxResponse.setError("Parameter 'method' is missing.");
                ajaxResponse.print();
                return;
            }

            // 2. Route dispatch based on method name
            if ("getSampleDetails".equalsIgnoreCase(method)) {
                getSampleDetails(ajaxResponse);
            } else if ("assignLocation".equalsIgnoreCase(method)) {
                assignLocation(ajaxResponse);
            } else {
                ajaxResponse.setError("Unknown dispatch method: " + method);
            }
            
        } catch (Exception e) {
            logError("Error processing AJAX request", e);
            ajaxResponse.setError("Server exception: " + e.getMessage(), e);
        } finally {
            // 3. Print final built JSON response to the client stream
            ajaxResponse.print();
        }
    }

    private void getSampleDetails(AjaxResponse ajaxResponse) throws Exception {
        String keyid1 = ajaxResponse.getRequestParameter("keyid1");
        if (keyid1 == null || keyid1.trim().isEmpty()) {
            ajaxResponse.setError("Parameter 'keyid1' is required.");
            return;
        }

        QueryProcessor qp = this.getQueryProcessor();
        DataSet ds = qp.getPreparedSqlDataSet(
            "SELECT s_sampleid, statusid, sdcid, locationid FROM s_sample WHERE keyid1 = ?",
            new Object[]{keyid1}
        );

        if (ds.getRowCount() > 0) {
            ajaxResponse.addCallbackArgument("sampleid", ds.getValue(0, "s_sampleid", ""));
            ajaxResponse.addCallbackArgument("status", ds.getValue(0, "statusid", ""));
            ajaxResponse.addCallbackArgument("sdcid", ds.getValue(0, "sdcid", ""));
            ajaxResponse.addCallbackArgument("location", ds.getValue(0, "locationid", ""));
            ajaxResponse.addCallbackArgument("success", true);
        } else {
            ajaxResponse.setError("Sample ID " + keyid1 + " not found.");
        }
    }

    private void assignLocation(AjaxResponse ajaxResponse) throws Exception {
        String keyid1 = ajaxResponse.getRequestParameter("keyid1");
        String locationid = ajaxResponse.getRequestParameter("locationid");

        if (keyid1 == null || locationid == null) {
            ajaxResponse.setError("Parameters 'keyid1' and 'locationid' are required.");
            return;
        }

        // Run parameterized update
        int updated = this.database.executePreparedUpdate(
            "UPDATE s_sample SET locationid = ?, modifiedby = ? WHERE keyid1 = ?",
            new Object[]{locationid, this.connectionInfo.getSysUserId(), keyid1}
        );

        if (updated > 0) {
            ajaxResponse.addCallbackArgument("success", true);
            ajaxResponse.addCallbackArgument("message", "Location assigned successfully.");
        } else {
            ajaxResponse.setError("Failed to assign location. Record may not exist.");
        }
    }
}
```

---

### 36.4 SDMS Attachment Handlers (`BaseAttachmentHandler`)

#### Architectural Context
An **SDMS Attachment Handler** is executed by the Scientific Data Management System when a Captured Data file reaches the "Pending Processing" state. The handler parses the physical file attachments and maps their contents (e.g. instrument readings, result tables) directly to LIMS records.

#### Rules & Standards
1. **Extend Base Class:** Must extend `com.labvantage.sapphire.modules.sdms.handlers.BaseAttachmentHandler` (which delegates to the public interface `sapphire.attachmenthandler.AttachmentHandler`).
2. **Override Entry Point:** Implement `public void handleData(List<Attachment> attachments, PropertyList properties) throws SapphireException`.
3. **Setup Variables:** Read custom configuration variables defined on the handler record in LIMS from the `properties` PropertyList param.
4. **File Streams:** Access attachments by index, and read their contents using `attachment.getInputStream()` in try-with-resources blocks.
5. **Record Creation:** To write records, instantiate an `ActionBlock` inside the handler and add action definitions (e.g., `AddSDI`, `AddDataSet`). Associate the handler using `setActionBlock()`. Link captured files using `addLinkSDI()`.

#### Complete Production-Grade Example
```java
package sapphire.attachmenthandler; // Package namespace is mandatory for SDMS classloader compatibility

import com.labvantage.sapphire.modules.sdms.handlers.BaseAttachmentHandler;
import sapphire.SapphireException;
import sapphire.attachment.Attachment;
import sapphire.xml.PropertyList;
import sapphire.util.ActionBlock;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Iterator;

/**
 * SDMS Attachment handler that parses JSON instrument files and creates Sample SDI records.
 */
public class SampleCreationHandler extends BaseAttachmentHandler {

    @Override
    public void handleData(List<Attachment> attachments, PropertyList properties)
            throws SapphireException {

        logMessage("SampleCreationHandler: Initialized.");

        if (attachments == null || attachments.isEmpty()) {
            throw new SapphireException("No file attachments received for capture processing.");
        }

        // 1. Read LIMS setup variables
        int copies = Integer.parseInt(properties.getProperty("copies", "1"));
        String sampleDesc = properties.getProperty("sampledesc", "Captured via SDMS");

        try {
            Attachment attachment = attachments.get(0);
            logMessage("Processing attachment file: " + attachment.getFileName());

            // 2. Extract attachment data stream
            String fileContents = "";
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (InputStream is = attachment.getInputStream()) {
                IOUtils.copy(is, bos);
            } catch (Exception e) {
                throw new SapphireException("FILE_READ_FAILED", "Failed to extract input stream from attachment.", e);
            }

            if (bos.size() > 0) {
                fileContents = new String(bos.toByteArray(), "UTF-8");
            } else {
                throw new SapphireException("EMPTY_FILE", "The capture attachment is empty.");
            }

            // 3. Parse JSON contents into property list
            PropertyList sampleProps = new PropertyList();
            try {
                JSONObject json = new JSONObject(fileContents);
                Iterator<?> keys = json.keys();
                while (keys.hasNext()) {
                    String k = (String) keys.next();
                    Object v = json.get(k);
                    // Standard PropertyList requires String values
                    sampleProps.setProperty(k, v != null ? v.toString() : "");
                }
                
                // Overlay variables
                sampleProps.setProperty("copies", String.valueOf(copies));
                sampleProps.setProperty("sampledesc", sampleDesc);
                
            } catch (Exception e) {
                throw new SapphireException("PARSE_ERROR", "Failed to parse JSON instrument file payload.", e);
            }

            // 4. Build and assign ActionBlock to generate LIMS Sample records
            ActionBlock actionBlock = new ActionBlock();
            // actionBlock.setAction(callerName, actionId, version, properties)
            actionBlock.setAction("SDMS_Handler", "AddSDI", "1", sampleProps);
            setActionBlock(actionBlock);

            // 5. Establish links to the new Sample SDI record using AddSDI replacement variables
            addLinkSDI("Sample", "[" + "AddSDI.RETURN_NEWKEYID1" + "]", "", "");
            
            logMessage("SampleCreationHandler: Successfully registered action blocks and linked SDIs.");

        } catch (SapphireException e) {
            throw e;
        } catch (Exception e) {
            logError("Unexpected error during SDMS processing", e);
            throw new SapphireException("SDMS_PROCESSING_FAILED", e.getMessage(), e);
        }
    }
}
```

---

## 37. Common Patterns

This section compiles the standard programming patterns and architectural best practices for writing clean, reliable, and high-performance custom Java code in LabVantage.

---

### 37.1 Database Access Patterns

#### Parameterized SELECT Queries via `QueryProcessor`
Never concatenate variables into SQL strings for SELECT statements. Always use `QueryProcessor` with parameterized placeholders (`?`) to prevent SQL injection and enable Oracle/SQL Server execution plan caching.

```java
// Correct Pattern for SELECT queries
QueryProcessor qp = this.getQueryProcessor();
String sql = "SELECT statusid, s_sampleid, departmentid FROM s_sample WHERE locationid = ? AND category = ?";
Object[] params = new Object[]{ "LOC-101", "Chemical" };

DataSet ds = qp.getPreparedSqlDataSet(sql, params);

for (int i = 0; i < ds.getRowCount(); i++) {
    String sampleId = ds.getValue(i, "s_sampleid", "");
    String status = ds.getValue(i, "statusid", "");
    // Process row
}
```

#### Safe Updates and Writes via `SafeSQL` & `database`
For INSERT, UPDATE, and DELETE operations, use `SafeSQL` to compile parameterized query strings dynamically.

```java
// Correct Pattern for UPDATE queries
SafeSQL sqlBuilder = new SafeSQL();
StringBuffer sql = new StringBuffer();

sql.append("UPDATE s_sample SET ");
sql.append("statusid = ").append(sqlBuilder.addVar("Reviewed")).append(", ");
sql.append("reviewdate = SYSDATE, ");
sql.append("comments = ").append(sqlBuilder.addVar(comments != null ? comments : ""));
sql.append(" WHERE keyid1 = ").append(sqlBuilder.addVar(keyid1));

int updatedRows = this.database.executePreparedUpdate(sql.toString(), sqlBuilder.getValues());
if (updatedRows < 1) {
    throw new SapphireException("UPDATE_FAILED", "Failed to update status for sample " + keyid1);
}
```

#### Proper Handling of NULL and Empty Fields
Database columns returning NULL values will cause Java NullPointerExceptions if treated directly. Always supply default fallbacks when retrieving values from a `DataSet` or `DBAccess`.

```java
// Use fallback strings to avoid NullPointerException
String department = ds.getValue(0, "departmentid", ""); // returns empty string if NULL
String isReleased = ds.getValue(0, "releasedflag", "N"); // returns "N" default if NULL
```

---

### 37.2 SDI and SDC Record Processing

#### Dynamic Table-Driven Queries
When writing reusable utility classes, do not hardcode table or key column names. Retrieve metadata dynamically from the SDC definition.

```java
// Dynamic metadata extraction
String sdcid = "Sample";
SDCProcessor sdcProcessor = this.getSDCProcessor();

String tableName = sdcProcessor.getProperty(sdcid, "tableid"); // e.g., "s_sample"
int keyCount = Integer.parseInt(sdcProcessor.getProperty(sdcid, "keycolumns")); // e.g., 1

StringBuilder sql = new StringBuilder("SELECT * FROM ").append(tableName).append(" WHERE ");
Object[] params = new Object[keyCount];

for (int i = 1; i <= keyCount; i++) {
    String keyColName = sdcProcessor.getProperty(sdcid, "keycolid" + i);
    params[i - 1] = properties.getProperty("keyid" + i);
    
    if (i > 1) sql.append(" AND ");
    sql.append(keyColName).append(" = ?");
}

QueryProcessor qp = this.getQueryProcessor();
DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), params);
```

#### Iterating Over Multi-Table DataStores
`SDIData` containers contain hierarchies of multiple related tables represented as a `DataSet` collection. When reading SDC rule inputs:

```java
// SDC Rule input iteration
public void preEdit(SDIData sdiData, PropertyList list) throws SapphireException {
    DataSet primaryDataSet = sdiData.getDataset("primary"); // main table
    DataSet childDataSet = sdiData.getDataset("s_sampletestmap"); // child table (if mapped)
    
    for (int i = 0; i < primaryDataSet.getRowCount(); i++) {
        String key = primaryDataSet.getValue(i, "keyid1", "");
        // perform validation
    }
}
```

---

### 37.3 Error Handling & Transaction Scope

#### Standard Try-Catch Exception Wrapper
Always catch general exceptions, log the full details using `this.logger.error`, and wrap the message inside a `SapphireException` so it rolls back the current database transaction.

```java
try {
    // Business logic
} catch (SapphireException e) {
    throw e; // Propagate LabVantage exceptions as-is
} catch (Exception e) {
    this.logger.error("Unexpected error in custom business logic: ", e);
    throw new SapphireException("UNEXPECTED_SYSTEM_ERROR", "An internal system error occurred: " + e.getMessage());
}
```

#### Warning Accumulation (ErrorHandler)
For non-blocking warnings or multiple validation errors that should be displayed together on the UI, accumulate messages into the `ErrorHandler`.

```java
ErrorHandler err = this.database.getErrorHandler(); // or getErrorHandler() directly depending on base

if (comments == null || comments.isEmpty()) {
    err.addError("MISSING_COMMENTS", "Comments must be provided.", "comments", "VALIDATION");
}
if (reviewer == null || reviewer.isEmpty()) {
    err.addError("MISSING_REVIEWER", "Reviewer ID is missing.", "reviewer", "VALIDATION");
}

if (err.hasErrors()) {
    return; // Stop execution; framework intercepts ErrorHandler and presents warnings on the UI
}
```

---

### 37.4 Sequence & Unique ID Generation

#### Programmatic Sequence Fetching
Do not write custom SQL select statements targeting Oracle sequences. Use the `SequenceProcessor` utility to guarantee cluster-wide unique, continuous sequences.

```java
SequenceProcessor seqProcessor = this.getSequenceProcessor();

// Get the next sequence value for a defined LabVantage counter
String nextId = seqProcessor.getSequence("SampleIDCounter", properties);
properties.setProperty("keyid1", nextId);
```

---

### 37.5 Calling Actions Programmatically (Chaining)

To call system or custom actions from within other custom Java actions or handlers, use `ActionProcessor`.

```java
// Initialise processor using the active database connection
ActionProcessor actionProcessor = new ActionProcessor(this.database.getConnection());

PropertyList actionInputs = new PropertyList();
actionInputs.setProperty("sdcid", "Sample");
actionInputs.setProperty("keyid1", sampleId);

// Execute "ReleaseSDI" action (version 1)
actionProcessor.processAction("ReleaseSDI", "1", actionInputs);

// Verify execution status
if (this.errorHandler.hasErrors()) {
    String error = this.errorHandler.getLastErrorMessage();
    throw new SapphireException("ACTION_CHAIN_FAILED", "ReleaseSDI action failed: " + error);
}
```

---

## 38. Other Helper Classes Reference by Package

This section contains reference listings for other classes inside the `sapphire.*` package structure, providing concise method and signature summaries.

### Package `sapphire.accessor`

#### `ActionException` (Class)
Constructor including the exception message.

*Declaration:* `public class ActionException extends SapphireException`

<details>
<summary>View public methods (3)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `int` | `getActionIndex()` | Returns the index of the action that generated the Exception. |
| `String` | `getActionName()` | Returns the name of the action that generated the Exception. |
| `ErrorHandler` | `getErrorHandler()` | — |

</details>

---

### Package `sapphire.action`

#### `BaseAdvancedPullSample` (Class)
Public API of the class com.labvantage.opal.actions.tasks.AdvancedPullSample

*Declaration:* `public class BaseAdvancedPullSample extends com.labvantage.opal.actions.tasks.AdvancedPullSample`

<details>
<summary>View public methods (23)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `protected void` | `addCustomSampleColumns(String schedulePlanId, String schedulePlanItemId, String conditionid, DataSet studyDS, PropertyList sampleProps)` | — |
| `protected void` | `applySpec(String keyIdList, PropertyListCollection specs, String schedulePlanId, String schedulePlanItemId, String sdcid)` | — |
| `String` | `getColor()` | — |
| `String[]` | `getDetailLevels()` | — |
| `HashMap` | `getDetails(PropertyList properties)` | — |
| `protected String` | `getFirstPlanItemId(String schedulePlanId, String schedulePlanItemId, String workItemId)` | — |
| `protected DataSet` | `getFirstPlanItemSample(String schedulePlanId, String firstPlanItemId, String workItemId)` | — |
| `protected String` | `getLogName()` | — |
| `protected StringBuffer` | `getPullAmounts(PropertyList propertyList)` | — |
| `protected StringBuffer` | `getSummary(PropertyList propertyList)` | — |
| `String` | `getSummaryHTML(PropertyList propertyList, String detailLevel)` | — |
| `String` | `getSummaryText(PropertyList propertyList, String detailLevel)` | — |
| `String` | `getTitle()` | — |
| `protected StringBuffer` | `getWorkorder(PropertyList propertyList)` | — |
| `boolean` | `isComplete(String planid, String planitemid, DBAccess database)` | Default behaviour of task |
| `protected boolean` | `isSampleLogged(String scheduleplanid, String scheduleplanitemid)` | — |
| `protected boolean` | `isTestLogged(String scheduleplanid, String scheduleplanitemid, String workitemid)` | — |
| `protected boolean` | `isTimeZeroTask(String scheduleplanid, String scheduleplanitemid)` | — |
| `protected DataSet` | `loadStudy(String schedulePlanId, String schedulePlanItemId)` | This method returns dataset with the Study SDC columns and controlsubstanceflag, cocrequiredflag of the Product joined with the Study and may return extra columns of other SDCs joined with Study SDI |
| `protected DataSet` | `loadTestProperties(String scheduleplanid, String scheduleplanitemid, boolean isTimeZeroTask)` | — |
| `protected void` | `postAddStabilitySample(String sampleIds, String schedulePlanId, String schedulePlanItemId)` | — |
| `protected void` | `preAddStabilitySample(String schedulePlanId, String schedulePlanItemId)` | — |
| `protected void` | `processReuseSample(String sampleId, String workItemId, String schedulePlanId, String schedulePlanItemId, boolean firstTimePoint)` | — |

</details>

#### `BaseAuthentication` (Class)
Created by IntelliJ IDEA.
User: cliu
Date: Jul 5, 2005
Time: 2:38:58 PM
To change this template use File | Settings | File Templates.

*Declaration:* `public class BaseAuthentication extends com.labvantage.sapphire.BaseCustom`

<details>
<summary>View public methods (9)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `abstract void` | `authenticateUser(String userid, String password, PropertyList properties)` | — |
| `void` | `createUser(String userid, String password, PropertyList properties)` | — |
| `void` | `endAuthenticate()` | — |
| `protected void` | `logError(String errormsg)` | — |
| `protected void` | `logError(String errormsg, Exception exception)` | — |
| `protected void` | `logTrace(String tracemsg)` | — |
| `void` | `secondaryAuthentication(PropertyList properties)` | — |
| `void` | `startAuthenticate(String databaseid, DBUtil dbutil)` | — |
| `void` | `synchronizeUser(String userid, String password, PropertyList properties)` | — |

</details>

#### `BasePasswordValidator` (Class)
Returns whether the password is a valid password for the current user

*Declaration:* `public class BasePasswordValidator extends com.labvantage.sapphire.BaseCustom`

<details>
<summary>View public methods (14)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `abstract void` | `checkPasswordFormat(String userid, String password, PropertyList properties)` | Returns whether the password is a valid password for the current user |
| `String` | `encodePassword(String password)` | Return encoded form of password. |
| `String` | `encrypt(String password)` | Deprecated. |
| `String` | `encrypt(String password, boolean isCaseSensitive)` | Deprecated. |
| `void` | `endPasswordHandler()` | — |
| `String` | `generatePassword(PropertyList properties)` | — |
| `protected void` | `logError(String errormsg)` | — |
| `protected void` | `logError(String errormsg, Exception exception)` | — |
| `protected void` | `logTrace(String tracemsg)` | — |
| `boolean` | `passwordMatches(String password, String encodedPassword, boolean isCaseSensitive)` | Checks if a stored password matches the provided one. |
| `boolean` | `passwordNeedUpgrade(String encodedPassword)` | Checks if a stored password should be upgraded to latest encoding. |
| `void` | `startPasswordHandler()` | — |
| `void` | `startPasswordHandler(DBUtil dbutil)` | — |
| `void` | `startPasswordHandler(SapphireConnection sapphireConnection)` | — |

</details>

#### `BaseScheduleTask` (Class)
*Declaration:* `public class BaseScheduleTask extends com.labvantage.sapphire.scheduler.BaseScheduleTask`

<details>
<summary>View public methods (1)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `void` | `execute()` | — |

</details>

#### `BaseSpecRule` (Class)
BaseSpecRule - standard interface for implementing custom spec rules

*Declaration:* `public class BaseSpecRule extends com.labvantage.sapphire.BaseCustom`

<details>
<summary>View public methods (1)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `String` | `getSpecCondition(String sdcid, String keyid1, String keyid2, String keyid3, String specid, String specversionid, DataSet sdidataitemspec, DataSet sdidataitem)` | — |

</details>

---

### Package `sapphire.attachment`

#### `Attachment.AttachmentType` (Enum)
Attachment Type Enumeration
Type File now includes all backwards compatiable types such as Store, Reference, Upload and Store and Upload and Reference

<details>
<summary>View public methods (8)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `String` | `getFlag()` | — |
| `String` | `getImageId()` | — |
| `static AttachmentType` | `getType(String name)` | — |
| `static AttachmentType` | `getTypeByAttachmentTypeFlag(String typeflag)` | — |
| `static AttachmentType` | `getTypeByAttachmentTypeFullName(String typename)` | — |
| `boolean` | `isEditable()` | — |
| `static AttachmentType` | `valueOf(String name)` | Returns the enum constant of this type with the specified name. |
| `static AttachmentType[]` | `values()` | Returns an array containing the constants of this enum type, in the order they are declared. |

</details>

#### `Attachment.ThumbnailGeneration` (Enum)
Thummail Generation Enumeration
Used to determine how the system handles and generates Thumbnails

<details>
<summary>View public methods (11)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `boolean` | `canGenerate()` | — |
| `boolean` | `canShow()` | — |
| `boolean` | `canStore()` | — |
| `String` | `getFlag()` | — |
| `static ThumbnailGeneration` | `getThumbnailGeneration(String titleOrFlag)` | — |
| `static ThumbnailGeneration` | `getThumbnailGeneration(String titleOrFlag, ThumbnailGeneration defaultThumb)` | — |
| `static ThumbnailGeneration` | `getThumbnailGenerationByFlag(String flag)` | — |
| `static ThumbnailGeneration` | `getThumbnailGenerationByName(String title)` | — |
| `String` | `getTitle()` | — |
| `static ThumbnailGeneration` | `valueOf(String name)` | Returns the enum constant of this type with the specified name. |
| `static ThumbnailGeneration[]` | `values()` | Returns an array containing the constants of this enum type, in the order they are declared. |

</details>

#### `BaseAttachmentRepository` (Class)
Base Class for Attachment Repositories
Extend this class and implement the abstract methods to create a custom Attachment repository.
You would then need to register the repository in the Attachment Repositories Property Tree.

*Declaration:* `public class BaseAttachmentRepository extends com.labvantage.sapphire.BaseCustom`

<details>
<summary>View public methods (38)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `abstract boolean` | `canBrowseRepository()` | Override and return true if the repository can browse a repository (default false) It is recomended to only enable this if an unmanaged repository. |
| `abstract boolean` | `canCompress()` | Override and return true if the repository does not require local compression (as it does its own) or false if the system controls compression (default false) |
| `abstract boolean` | `canEncrypt()` | Override and return true if the repository does not require local encrypting (as it does its own) or false if the system controls encrytion (default false) |
| `abstract boolean` | `canGenerateThumbnail()` | Override and return true if the repository does its own thumbnail generation (default false) |
| `abstract boolean` | `canHash()` | Override and return true if the repository does not require local hashing (as it does its own) or false if the system controls hashing (default false) |
| `abstract boolean` | `checkHash(Attachment attachment)` | Override and add any hashing logic for validating the attachment. |
| `abstract void` | `cleanUpRepository(Attachment oldAttachment)` | Override and implement this method to handle the deletion of the sdi attachment record. |
| `abstract boolean` | `enableCaching()` | Override and return true if the repository does not require local caching or false if the system controls caching (default false) |
| `PropertyList` | `getAttachmentPolicy()` | Returns the attachment policy property list |
| `abstract String` | `getBrowseButtonText()` | Returns the text for support of the browse button. |
| `abstract String[]` | `getBrowseIncludes()` | Returns the array of script includes for support of the browse button. |
| `abstract String` | `getBrowseScript(String attachmentElementId)` | Returns the script for support of the browse button. |
| `ClassLoader` | `getClassLoader()` | — |
| `InputStream` | `getDirectInputStream(PropertyList propertyList)` | Overridable method to return inputstream directly |
| `static InputStream` | `getDirectInputStream(String repositoryId, String repositoryNodeId, PropertyList props, SapphireConnection sapphireConnection)` | — |
| `String` | `getFileViewerHTML(String fileViewerId, PropertyList propertyList, PageContext pageContext)` | Overridable method to return fileviewerhtml directly |
| `static String` | `getFileViewerHTML(String repositoryId, String repositoryNodeId, String fileViewerId, PropertyList propertyList, PageContext pageContext, SapphireConnection sapphireConnection)` | — |
| `static BaseAttachmentRepository` | `getRepository(String filerepositoryid, String filerepositorynode, SapphireConnection conn)` | Will return an Attachment Repository object based on the repository property tree id and node. |
| `PropertyList` | `getRepositoryProperties()` | Returns the list of properties for the repository |
| `protected SapphireConnection` | `getSapphireConnection()` | Returns the sapphire connection object |
| `abstract void` | `getSDIAttachment(Attachment attachment, ThumbnailGeneration thumbnailGeneration)` | Override and implement this method to handle the getting of the sdi attachment record. |
| `protected DataSet` | `getUniqueFilesInAudit(Attachment oldAttachment)` | Will return a dataset of audit records where the files are not in any other audit record or in the sdiattachment table. |
| `protected boolean` | `isFilePresentInOtherAttachments(Attachment oldAttachment, boolean postDelete)` | Will return true if the repository reference is found on another attachment record or inside the audit table |
| `protected boolean` | `isFilePresentInOtherAttachments(Attachment oldAttachment, boolean postDelete, boolean checkAudit)` | Will return true if the repository reference is found on another attachment record or inside the audit table |
| `boolean` | `isManaged()` | Returns true if this is a managed repository and false if not It is recommended to use managed repositories where possible as they are more secure and robust. |
| `abstract void` | `postAddSDIAttachment(Attachment attachment)` | Override and implement this method to handle the addition of the sdi attachment record. |
| `abstract void` | `postDeleteSDIAttachment(Attachment oldAttachment)` | Override and implement this method to handle the deletion of the sdi attachment record. |
| `abstract void` | `postEditSDIAttachment(Attachment attachment, Attachment oldAttachment)` | Override and implement this method to handle the update of the sdi attachment record. |
| `abstract void` | `preAddSDIAttachment(Attachment attachment)` | Override and implement this method to handle the addition of the sdi attachment record. |
| `abstract void` | `preDeleteSDIAttachment(Attachment attachment)` | Override and implement this method to handle the deletion of the sdi attachment record. |
| `abstract boolean` | `preEditSDIAttachment(Attachment attachment, Attachment oldAttachment)` | Override and implement this method to handle the update of the sdi attachment record. |
| `protected void` | `setAttachmentPolicy(PropertyList attachmentPolicy)` | — |
| `protected void` | `setClassLoader(ClassLoader classLoader)` | — |
| `protected void` | `setManaged(boolean managed)` | — |
| `protected void` | `setRepositoryProperties(PropertyList repositoryProperties)` | — |
| `protected void` | `setSapphireConnection(SapphireConnection sapphireConnection)` | Sets a sapphire connection |
| `protected void` | `transferData(InputStream inputStream, File output, boolean closeInput, boolean closeOutput)` | — |
| `protected void` | `transferData(InputStream inputStream, OutputStream outputStream, boolean closeInput, boolean closeOutput)` | — |

</details>

---

### Package `sapphire.attachmenthandler`

#### `AttachmentHandler` (Interface)
The interface for an Attachment Handler object

<details>
<summary>View public methods (23)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `void` | `addFile(String filepath, String aliasFileName, String attachmentClass)` | — |
| `void` | `addFileMetaData(PropertyList propertyList, Attachment attachment)` | — |
| `void` | `addLinkSDI(String sdcid, String keyid1, String keyid2, String keyid3)` | — |
| `void` | `addMetaData(String name, String value)` | — |
| `ResultDataGrid` | `addResultGrid()` | — |
| `String` | `getAtachmentHandlerId()` | — |
| `PropertyList` | `getFileMetaData(Attachment attachment)` | — |
| `String` | `getHandlerId()` | — |
| `HandlerType` | `getHandlerType()` | — |
| `String` | `getKeyId1()` | — |
| `String` | `getKeyId2()` | — |
| `String` | `getKeyId3()` | — |
| `SDILinks` | `getLinkSDI()` | — |
| `PropertyList` | `getMetaData()` | — |
| `ResultDataGrid` | `getResultGrid()` | — |
| `ResultDataGrid` | `getResultGrid(int resultgridindex)` | — |
| `int` | `getResultResultGridCount()` | — |
| `String` | `getSDCId()` | — |
| `void` | `handleData(List<Attachment> attachments, PropertyList properties)` | — |
| `boolean` | `isDatabaseRequired()` | — |
| `void` | `logMessage(String message)` | — |
| `void` | `setActionBlock(ActionBlock actionBlock)` | — |
| `void` | `setResultGrid(ResultDataGrid resultGrid)` | — |

</details>

#### `HandlerType` (Enum)
Returns the enum constant of this type with the specified name.

<details>
<summary>View public methods (4)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `static HandlerType` | `getHandlerType(String flagOrName)` | — |
| `String` | `getTypeFlag()` | — |
| `static HandlerType` | `valueOf(String name)` | Returns the enum constant of this type with the specified name. |
| `static HandlerType[]` | `values()` | Returns an array containing the constants of this enum type, in the order they are declared. |

</details>

#### `SDILink` (Class)
Class to represent an SDI Link that can be converted to and from JSON

*Declaration:* `public class SDILink extends java.lang.Object implements com.labvantage.sapphire.gwt.shared.JSONable`

<details>
<summary>View public methods (6)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `String` | `getKeyId1()` | — |
| `String` | `getKeyId2()` | — |
| `String` | `getKeyId3()` | — |
| `String` | `getSDCId()` | — |
| `JSONObject` | `toJSONObject()` | — |
| `String` | `toJSONString()` | — |

</details>

#### `SDILinks` (Class)
A class to represent an array of SDI Links that can be converted to JSON

*Declaration:* `public class SDILinks implements com.labvantage.sapphire.gwt.shared.JSONable`

<details>
<summary>View public methods (2)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `JSONArray` | `toJSONArray()` | — |
| `String` | `toJSONString()` | — |

</details>

---

### Package `sapphire.error`

#### `ErrorDetail` (Class)
Public version of ErrorDetail

*Declaration:* `public class ErrorDetail extends com.labvantage.sapphire.gwt.shared.error.ErrorDetail implements java.io.Serializable`

---

### Package `sapphire.ext`

#### `BaseCollectorType` (Class)
BaseCollectorType is the base implementation for SDMS Collector Types.
If external collectors are to be used, then the custom jar will need to get added into the SDMS install-image in LabVantageHome

*Declaration:* `public class BaseCollectorType extends com.labvantage.sapphire.modules.sdms.collector.collectortypes.BaseCollectorType implements com.labvantage.sapphire.modules.sdms.SDMSConstants`

<details>
<summary>View public methods (11)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `void` | `configure(PropertyList collectorTypeProps)` | Override this method to set up all the working properties for the collector. |
| `String` | `doDeliverRunFile(String filename, byte[] bytes)` | Implement this method to perform emulation |
| `abstract boolean` | `doRunCollector(FileSenderFactory fileSenderFactory)` | Implement this method to perform collection |
| `abstract boolean` | `doRunEmulator()` | Implement this method to perform emulation |
| `abstract int` | `getCollectionPollInterval()` | Return the poll interval. |
| `int` | `getEmulatorPollInterval()` | Return the emulator interval if there is one |
| `abstract boolean` | `isCollectionEnabled()` | Indicate whether collection is enabled |
| `boolean` | `isContinuousOperation()` | Return true if the collector and emulator are self threaded like the network collector and so are not polled |
| `boolean` | `isEmulatorEnabled()` | Indicate whether emulation is enabled |
| `abstract boolean` | `isRunfileDeliveryEnabled()` | Indicate whether runfile deliver is enabled |
| `protected void` | `raiseInstrumentAlert(String alertType, String alertSeverity, String description, String message, boolean forceNew, boolean throwException)` | Call this method to |

</details>

#### `BaseInstrumentProvider` (Class)
BaseInstrumentProvider class is the default implementation of the Instrument Protocol Provider.
Custom implementation for an instrument model can override the executeCommand method, or parseResponse method or both.

*Declaration:* `public class BaseInstrumentProvider extends com.labvantage.sapphire.BaseCustom`

<details>
<summary>View public methods (3)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `void` | `cancelCommand(PropertyList commandProps)` | override this method to allow cancel a command, especially when response is pushed from instrument. |
| `String` | `executeCommand(PropertyList commandProps)` | override this method to customize communicating with the instrument for push response type, returning empty string meaning result is not ready, checkResponse will be called repeatedly until a none empty response returned. |
| `HashMap` | `parseResponse(String responseStr, PropertyList commandProps)` | override this method to customize parsing instrument response string into name value pairs |

</details>

#### `BaseIssueHandler` (Class)
Description: Base class for Issue Handler. In order to submit an issue to an external Issue Repository, this class needs to be extended and implemented.

*Declaration:* `public class BaseIssueHandler extends com.labvantage.sapphire.BaseCustom`

<details>
<summary>View public methods (14)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `static void` | `deleteSubmissionId(String submissionId)` | — |
| `void` | `executeMethod(String method, PropertyList properties)` | — |
| `static String` | `generateSubmissionId(String connectionId)` | — |
| `static BaseIssueHandler` | `getInstance(String handlerClass, SapphireConnection sapphireConnection)` | — |
| `PropertyList` | `getIssue(String issueId, String issueRepositoryId, String loginCredentials)` | — |
| `protected String` | `getLoginCredentials(PropertyList repositoryProps, String enteredLoginCredentials)` | — |
| `protected String` | `getRepositoryExtraPropsValue(PropertyList repositoryProps, String propertyId)` | — |
| `static String` | `getSubmitProgress(String submissionId)` | — |
| `PropertyList` | `searchIssue(String issueId, String issueRepositoryId, String loginCredentials)` | — |
| `void` | `setDatabase(DBAccess database)` | — |
| `void` | `setSubmissionId(String submissionId)` | — |
| `String` | `submitIssue(String issueId, String issueRepositoryId, String loginCredentials)` | — |
| `void` | `transferChangeRequestInfoToIssue(String changeRequestId, String issueRepositoryId, String loginCredentials)` | — |
| `protected void` | `updateProgressStatus(String progressText)` | — |

</details>

#### `BaseSDCRO` (Class)
Created by IntelliJ IDEA.
User: hgurla
Date: Nov 16, 2008
Time: 2:27:20 PM
To change this template use File | Settings | File Templates.

*Declaration:* `public class BaseSDCRO extends com.labvantage.sapphire.modules.configreport.ro.BaseRO`

<details>
<summary>View public methods (40)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `String` | `generateSDISectionXMLFileName(SDI currentSDI)` | — |
| `String` | `getAccessControl()` | — |
| `DataSet` | `getCategories()` | — |
| `DataSet` | `getColumnData()` | — |
| `protected DataSet` | `getColumnDataFromXMLReport(String refReportFolder)` | — |
| `PropertyList` | `getCurrentSDCProperties()` | — |
| `DataSet` | `getDataSet(String dsName)` | — |
| `String[]` | `getDataSetKeyCols(String dsName)` | — |
| `String` | `getDescription()` | — |
| `ArrayList` | `getDetailColumns(String tableName)` | — |
| `String[]` | `getDetailLinkTableKeys(String detail)` | — |
| `String[]` | `getDetailLinkTables()` | — |
| `String[]` | `getDetailTables()` | — |
| `int` | `getKeyColCount()` | — |
| `String` | `getKeyColId1()` | — |
| `String` | `getKeyColId2()` | — |
| `String` | `getKeyColId3()` | — |
| `String` | `getKeyid1()` | — |
| `String` | `getKeyid2()` | — |
| `String` | `getKeyid3()` | — |
| `String` | `getLinkid(String detailtableid)` | — |
| `ArrayList` | `getPrimaryColumnLabels()` | — |
| `ArrayList` | `getPrimaryColumns()` | — |
| `String` | `getPrimaryValue(String columnName)` | — |
| `DataSet` | `getRoleMatrix()` | — |
| `String` | `getSDCDescription()` | — |
| `String` | `getSDCName()` | — |
| `String` | `getSDCPlural()` | — |
| `HashMap` | `getSDCProperties()` | — |
| `String` | `getSDCSingular()` | — |
| `protected static SDI` | `getSDI(SDIData sdiData)` | — |
| `int` | `getSDICount()` | — |
| `String` | `getTemplateFlag()` | — |
| `int` | `gotoSection(SDI sdi)` | — |
| `boolean` | `hasNextSection()` | Internal method, do not override |
| `void` | `nextSection()` | Internal method do not override |
| `void` | `reset()` | Internal method do not override |
| `void` | `setCurrentSDIData(SDIData sdiData)` | Internal method do not overrideSDI |
| `void` | `setSDIList(ArrayList sdiList)` | — |
| `void` | `startChapter()` | This method can be overriden if some "setup" is to be done in the custom RO for the entire chapter |

</details>

#### `BaseSDCRenderer` (Class)
Created by IntelliJ IDEA.
User: hgurla
Date: Dec 4, 2008
Time: 1:21:50 PM
To change this template use File | Settings | File Templates.

*Declaration:* `public class BaseSDCRenderer extends com.labvantage.sapphire.modules.configreport.renderer.BaseRenderer`

<details>
<summary>View public methods (33)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `boolean` | `checkIfIgnore(BaseSDCRO sdcRO, BaseSDCRO refSdcRO)` | — |
| `void` | `createXMLReport()` | — |
| `ConfigReportContent` | `getCustomSectionContent(String customSectionName)` | If the custom renderer has any non-SDI sections, override this method and return appropriate content for each custom section. |
| `ArrayList` | `getCustomSectionNames()` | If the custom renderer has any non-SDI sections, override this method and return an ArrayList with the section names for the custom sections. |
| `PropertyListCollection` | `getOptions()` | — |
| `static String` | `getPrimaryValue(SDIData sdiData, String columnName)` | — |
| `BaseSDCRO` | `getReferenceRO()` | — |
| `SDIData` | `getReferenceSDIData()` | — |
| `String` | `getSDCId()` | — |
| `ArrayList` | `getSDIsIncluded()` | — |
| `ConfigReportContent` | `getSectionContent(BaseSDCRO sdcRO, BaseSDCRO refSdcRO, TranslationProcessor translationProcessor)` | — |
| `ArrayList` | `getSectionList()` | — |
| `ArrayList` | `getSectionTitleList()` | — |
| `BaseSDCRO` | `getSourceRO()` | — |
| `SDIData` | `getSourceSDIData()` | — |
| `protected ConfigReportContent` | `getSpecialContent(BaseSDCRO sdcRO, BaseSDCRO refSdcRO, TranslationProcessor translationProcessor)` | — |
| `protected ConfigReportContent` | `getSpecialContent(BaseSDCRO sdcRO, TranslationProcessor translationProcessor)` | — |
| `protected ConfigReportContent` | `getSpecialContent(SDIData sdiData, SDIData refSDIData, TranslationProcessor translationProcessor)` | — |
| `protected ConfigReportContent` | `getSpecialContent(SDIData sdiData, TranslationProcessor translationProcessor)` | — |
| `boolean` | `hasChapterChanged()` | — |
| `boolean` | `hasCustomSections()` | If the custom renderer has any non-SDI sections, override this method and return true |
| `protected boolean` | `ignoreDiff(String currCol)` | — |
| `void` | `initialize(SapphireConnection sapphireConnection, BaseRO srcRO, BaseRO refRO)` | — |
| `void` | `initialize(SapphireConnection sapphireConnection, PropertyList config, BaseRO srcRO, HashMap sdisIncluded, boolean includeSDIRoleMatrix)` | — |
| `void` | `initialize(SapphireConnection sapphireConnection, PropertyList config, BaseSDCRO srcRO, BaseSDCRO refRO, HashMap sdisIncluded, boolean includeDiffReport, boolean includeSDIRoleMatrix)` | Internal method. |
| `boolean` | `isClob(String columnName)` | — |
| `DataSet` | `removeAuditColumns(DataSet orig)` | — |
| `void` | `renderAttributes(ConfigReportContent content)` | — |
| `protected ConfigReportContent` | `renderDetails(boolean reportDetailsTables, String hideValueFor, TranslationProcessor translationProcessor)` | t This method can be called by the getSectionContent() to render the complete details of the SDI. |
| `protected void` | `renderDetails(ConfigReportContent content, boolean reportDetailsTables, TranslationProcessor translationProcessor)` | This method can be called by the getSectionContent() to render the complete details of the SDI. |
| `void` | `setIgnoreDiffs(PropertyListCollection ignorePrimaryDiffs, PropertyListCollection ignoreDetailsDiffs)` | — |
| `void` | `setOptions(DataSet options)` | — |
| `void` | `setOptions(PropertyListCollection options)` | — |

</details>

#### `BaseSQLRegister` (Class)
Base class to extend when creating a SQL statement register

*Declaration:* `public class BaseSQLRegister extends java.lang.Object`

<details>
<summary>View public methods (3)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `String` | `getDbms()` | — |
| `abstract String` | `getSQLStatement(int sqlCode)` | — |
| `void` | `setDbms(String dbms)` | — |

</details>

#### `BaseScript` (Class)
Base Groovy Script Class

*Declaration:* `public class BaseScript extends java.lang.Object`

<details>
<summary>View public methods (4)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `protected ConnectionInfo` | `getConnectionInfo()` | — |
| `static BaseScript` | `getInstance(Class theclass, Script callingScript)` | — |
| `static BaseScript` | `getInstance(String classname, Script callingScript)` | — |
| `protected void` | `setContext(HashMap bindings)` | — |

</details>

#### `BaseStatementHandler` (Class)
*Declaration:* `public class BaseStatementHandler extends java.lang.Object implements java.io.Serializable`

<details>
<summary>View public methods (17)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `PropertyList` | `getAuthenticationProps()` | — |
| `String` | `getConnectionid()` | — |
| `String` | `getDatabase()` | — |
| `String` | `getDoNotPromptAgain()` | — |
| `String` | `getStatementCounter()` | — |
| `String` | `getStatementId()` | — |
| `DataSet` | `getStatements()` | — |
| `String` | `getStatementType()` | — |
| `String` | `getStatementVersionId()` | — |
| `String` | `getUsername()` | — |
| `void` | `init(PropertyList authenticationPL, String username, String database, String connectionid, String systemPassword)` | — |
| `abstract void` | `renderPrompt(HttpServletRequest request, HttpServletResponse response)` | — |
| `void` | `setDoNotPromptAgain(String donotpromptagain)` | — |
| `void` | `setStatementCounter(String statementcounter)` | — |
| `void` | `setStatementId(String statementid)` | — |
| `void` | `setStatementType(String statementtype)` | — |
| `void` | `setStatementVersionId(String statementversionid)` | — |

</details>

#### `BaseTaskScript` (Class)
Base Task Groovy Script Class

*Declaration:* `public class BaseTaskScript extends BaseScript`

<details>
<summary>View public methods (16)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `protected ActionProcessor` | `getActionProcessor()` | — |
| `protected DBRead` | `getDatabase()` | — |
| `protected GroovyLogger` | `getLogger()` | — |
| `protected M18NUtil` | `getM18N()` | — |
| `protected QueryProcessor` | `getQueryProcessor()` | — |
| `protected SDCProcessor` | `getSDCProcessor()` | — |
| `protected SDIProcessor` | `getSDIProcessor()` | — |
| `protected SequenceProcessor` | `getSequenceProcessor()` | — |
| `protected PropertyList` | `getStepProperty()` | — |
| `protected TaskContext` | `getTaskContext()` | — |
| `protected JSONable` | `getVariable(String variableid)` | — |
| `protected WorkflowProcessor` | `getWorkflowProcessor()` | — |
| `protected void` | `setContext(HashMap bindings)` | — |
| `protected void` | `setOutput(String name, Object value)` | — |
| `protected void` | `setVariable(String variableid, SDIList value)` | — |
| `protected void` | `setVariable(String variableid, String value)` | — |

</details>

#### `BaseTismScanParser` (Class)
BaseTismScanParser class is an Abstract class that is extended by the classes that require to parse
scanned value in TISM page and pre-populate the additional data after an item has been scanned.

*Declaration:* `public class BaseTismScanParser extends com.labvantage.sapphire.BaseCustom`

<details>
<summary>View public methods (1)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `abstract PropertyList` | `parseScannedString(String sdcid, String scannedid)` | — |

</details>

#### `BaseWebMFAHandler` (Class)
Used from LIMS MFA Login.

*Declaration:* `public class BaseWebMFAHandler extends java.lang.Object implements java.io.Serializable`

<details>
<summary>View public methods (10)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `void` | `completeLogin(HttpServletRequest request)` | Used from LIMS MFA Login. |
| `void` | `completeLogin(HttpSession httpSession)` | Used from Portal MFA Login. |
| `PropertyList` | `getAuthenticationProps()` | — |
| `String` | `getConnectionid()` | — |
| `protected ConnectionInfo` | `getConnectionInfo()` | — |
| `String` | `getUsername()` | — |
| `void` | `init(PropertyList authenticationProps, String username, String database, String connectionid, String secretKey)` | — |
| `abstract void` | `renderPrompt(HttpServletRequest request, HttpServletResponse response)` | — |
| `void` | `setAuthenticationProps(PropertyList authenticationProps)` | — |
| `abstract boolean` | `verifyResponse(HttpServletRequest request, HttpServletResponse response)` | — |

</details>

#### `BaseWebSSOHandler` (Class)
Created by IntelliJ IDEA.
User: cliu
Date: 7/24/13
Time: 4:37 PM
To change this template use File | Settings | File Templates.

*Declaration:* `public class BaseWebSSOHandler extends java.lang.Object implements LogonRequestValidator`

<details>
<summary>View public methods (22)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `PropertyList` | `getSSOAttributes(HttpServletRequest request)` | Retrieve the SSO attributes in the Request object |
| `String` | `getUserid(HttpServletRequest request)` | override this method to extract user identity to map to LV a sysuserid or logonname. |
| `String` | `getUseridattributename()` | — |
| `static String` | `getUseridattributename(String portalId, String databaseId)` | — |
| `String` | `getWebssodatabase()` | — |
| `String` | `getWebssoesigurl()` | — |
| `static String` | `getWebssoesigurl(String portalId, String databaseId)` | — |
| `String` | `getWebssologoffurl()` | — |
| `void` | `handleLVLogonError(String errorMessage, HttpServletResponse response)` | allow customized handling LV log on error when a LV user logon fails after Web SSO authentication Default to writing the LV log on error message to the http servlet response in red. |
| `boolean` | `isAllowHeaderAttributes()` | — |
| `static boolean` | `isAllowHeaderAttributes(String portalId, String databaseId)` | — |
| `static boolean` | `isEnabled(String portalId, String databaseId)` | — |
| `boolean` | `isRequireSysuserInfo()` | System calls validateRequest with Request and DataSet sysuser if this method returns true and with Request only if returns false |
| `void` | `logoff(HttpServletRequest request, HttpServletResponse response)` | Called after logout LV Override this method to customize web sso log out after log out LV. |
| `void` | `setAllowHeaderAttributes(boolean allowHeaderAttributes)` | — |
| `static void` | `setPortalSSOProps(String databaseId, String portalId, PropertyList portalProps)` | Called during system startup and populated. |
| `void` | `setUseridattributename(String useridattributename)` | — |
| `void` | `setWebssodatabase(String webssodatabase)` | — |
| `void` | `setWebssoesigurl(String webssoesigurl)` | — |
| `void` | `setWebssologoffurl(String webssologoffurl)` | — |
| `String` | `validateRequest(HttpServletRequest request)` | override this method to allow customized validation of request attributes to further determine whether to authorize access to the application after initial authentication return not allow acess message html or empty string if pass validation. |
| `String` | `validateRequest(HttpServletRequest request, DataSet sysuser)` | override this method to allow customized validation of request attributes to further determine whether to authorize access to the application after initial authentication return not allow acess message html or empty string if pass validation. |

</details>

#### `BaseWorksheetItem` (Class)
Base WorksheetItem class

*Declaration:* `public class BaseWorksheetItem extends com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItem`

#### `ConfigReportContent` (Class)
Created by IntelliJ IDEA.
User: hgurla
Date: 9/4/12
Time: 3:31 PM
To change this template use File | Settings | File Templates.

*Declaration:* `public class ConfigReportContent extends java.lang.Object`

<details>
<summary>View public methods (191)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `void` | `addBullet(String str)` | — |
| `void` | `addCategories(DataSet categories)` | — |
| `void` | `addCategoriesDiff(DataSet categories, DataSet refCategories)` | — |
| `void` | `addColumnHeader(String colHeader)` | — |
| `void` | `addColumnHeader(String colHeader, boolean rotateHeader)` | — |
| `void` | `addColumnHeader(String columnid, HashMap<String,String> columnTitleMap, TranslationProcessor translationProcessor)` | — |
| `StringBuffer` | `addDeletedRowItem(String columnName, String columnVal, TranslationProcessor translationProcessor)` | — |
| `StringBuffer` | `addDeletedRowItemEmphasis(String columnName, String columnVal, TranslationProcessor translationProcessor)` | — |
| `void` | `addDiffBullet(String str, String refStr)` | — |
| `DataSet` | `addDiffInfo(DataSet listItems, DataSet ref, String[] keyColumns)` | — |
| `DataSet` | `addDiffInfo(DataSet srcItemsOriginal, DataSet refItemsOriginal, String[] keyColumns, PropertyListCollection ignoreCols)` | — |
| `protected void` | `addDiffListItems(DataSet srcItems, DataSet refItems, String[] keycols, PropertyListCollection ignoreCols, TranslationProcessor translationProcessor, boolean hideEmptyColumns)` | — |
| `protected void` | `addDiffListItems(DataSet listItems, DataSet ref, String[] keycols, TranslationProcessor translationProcessor, boolean hideEmptyColumns)` | — |
| `StringBuffer` | `addDiffRowItem(String columnName, String columnVal, String refVal)` | — |
| `StringBuffer` | `addDiffRowItem(String columnName, String columnVal, String refVal, boolean ignoreDiff, TranslationProcessor translationProcessor)` | — |
| `StringBuffer` | `addDiffRowItem(String columnName, String columnVal, String refVal, int colspan, boolean ignoreDiff, TranslationProcessor translationProcessor, boolean hideEmptyColumns)` | — |
| `StringBuffer` | `addDiffRowItem(String columnName, String columnVal, String refVal, int colspan, TranslationProcessor translationProcessor)` | — |
| `StringBuffer` | `addDiffRowItem(String columnName, String lhsClass, String rhsClass, String columnVal, String refVal, int colspan, boolean ignoreDiff, TranslationProcessor translationProcessor, boolean hideEmptyColumns)` | — |
| `StringBuffer` | `addDiffRowItem(String columnName, String columnVal, String refVal, TranslationProcessor translationProcessor)` | — |
| `StringBuffer` | `addDiffRowItemCheckbox(String columnName, String columnVal, String refVal)` | — |
| `StringBuffer` | `addDiffRowItemEmphasis(String columnName, String oldVal, String newVal, TranslationProcessor translationProcessor)` | — |
| `protected void` | `addListItems(DataSet listItems, TranslationProcessor translationProcessor)` | — |
| `protected void` | `addListItems(DataSet listItems, TranslationProcessor translationProcessor, String status, boolean hideEmptyColumns)` | — |
| `void` | `addMatrix(DataSet roleMatrix, int keycols)` | — |
| `StringBuffer` | `addNewRowItem(String columnName, String columnVal, TranslationProcessor translationProcessor)` | — |
| `StringBuffer` | `addNewRowItemEmphasis(String columnName, String columnVal, TranslationProcessor translationProcessor)` | — |
| `void` | `addRoleMatrix(DataSet roleMatrix, int keycols)` | — |
| `protected void` | `addRotatedHeaderListItems(DataSet listItems, int keycols, TranslationProcessor translationProcessor, boolean hideEmptyColumns)` | — |
| `StringBuffer` | `addRowItem(String columnName, String columnVal)` | — |
| `StringBuffer` | `addRowItem(String columnName, String columnVal, boolean icon)` | — |
| `StringBuffer` | `addRowItem(String columnName, String columnVal, int colspan)` | — |
| `StringBuffer` | `addRowItem(String columnName, String columnVal, String lhsClass, String rhsClass, TranslationProcessor translationProcessor)` | — |
| `StringBuffer` | `addRowItem(String columnName, String columnVal, TranslationProcessor translationProcessor)` | — |
| `StringBuffer` | `addRowItemCheckbox(String columnName, String columnVal)` | — |
| `StringBuffer` | `addRowItemEmphasis(String columnName, String columnVal, TranslationProcessor translationProcessor)` | — |
| `StringBuffer` | `append(String string)` | — |
| `StringBuffer` | `append(StringBuffer buffer)` | — |
| `void` | `appendInnerNodeContent(ConfigReportContent nodecontent, String nodeId, String nodelabel, String status)` | — |
| `void` | `appendNodeContent(ConfigReportContent nodecontent, String nodeId, String nodelabel)` | — |
| `void` | `appendNodeContent(ConfigReportContent nodecontent, String nodeId, String nodelabel, String status)` | — |
| `void` | `appendNodeContent(ConfigReportContent nodecontent, String nodeId, String nodelabel, String status, String nodetag)` | — |
| `void` | `appendSpecialContent(ConfigReportContent content)` | — |
| `void` | `appendSpecialContent(ConfigReportContent content, boolean diffonly)` | — |
| `void` | `appendSubSection(ConfigReportContent subSectionContent, String subSection)` | — |
| `void` | `appendSubSection(ConfigReportContent subSectionContent, String subSection, boolean diffOnly)` | — |
| `static String` | `changeHTMLtags(String value)` | — |
| `static String` | `changeImageFolder(String imageURL, String folder, String applicationRoot)` | — |
| `protected boolean` | `checkIfEmpty(DataSet ds)` | — |
| `boolean` | `checkIfSimple(PropertyListCollection coll)` | — |
| `void` | `clearContent()` | — |
| `static DataSet` | `convertSimpleCollToDS(PropertyListCollection coll, PropertyDefinitionList defList)` | — |
| `static String` | `convertToID(String title)` | — |
| `static void` | `copyFile(File in, File out)` | — |
| `static String` | `createHyperLink(String source, String ref)` | — |
| `static String` | `createHyperLink(String sdcid, String fkkeyid1, String fkkeyid2, String fkkeyid3, HashMap sdisIncluded, boolean frames)` | — |
| `void` | `endBulletList()` | — |
| `StringBuffer` | `endChapter(String sdcId)` | — |
| `void` | `endFile()` | — |
| `void` | `endHeader()` | — |
| `protected void` | `endListTable()` | — |
| `void` | `endReport(StringBuffer buffer)` | — |
| `void` | `endRow()` | — |
| `void` | `endSection()` | — |
| `void` | `endSubSection(String title, String desc)` | — |
| `void` | `endTable()` | — |
| `int` | `findRow(DataSet ds, HashMap filter)` | — |
| `static String` | `generateNodeAnchor(String node)` | — |
| `static String` | `generateSDISectionAnchor(SDI currentSDI)` | — |
| `static String` | `generateSDISectionFileName(SDI currentSDI)` | — |
| `static String` | `generateSDISectionTitle(SDI currentSDI)` | — |
| `static String` | `generateSDISectionXMLFileName(SDI currentSDI)` | — |
| `static String` | `generateSDISubSectionFileName(SDI currentSDI)` | — |
| `static String` | `generateSectionAnchor(String layout)` | — |
| `static String` | `generateSectionFileName(String chapterName, String sectionName)` | — |
| `static String` | `generateSectionTitle(String layout)` | — |
| `static String` | `generateSectionXMLFileName(String chapterName, String sectionName)` | — |
| `static String` | `generateSubSectionFileName(String chapterName, String sectionName)` | — |
| `static String` | `generateTOCFileName(String chapterName)` | — |
| `static String` | `generateTOCXMLFileName(String chapterName)` | — |
| `String` | `getApplicationRoot()` | — |
| `static String` | `getDeletedString(String orig)` | — |
| `static String` | `getDiffString(String value, String refValue)` | — |
| `String` | `getEndTable()` | — |
| `static String` | `getFileName(String link)` | — |
| `String` | `getFolder()` | — |
| `String` | `getFormattedDiffVal(String columnName, String columnVal, String refVal, boolean top, TranslationProcessor translationProcessor)` | — |
| `String` | `getFormattedItemLabel(SDIData sdiData, SDI sdi, String labelformat)` | — |
| `String` | `getFormattedValue(String columnName, String columnVal)` | — |
| `boolean` | `getFoundDiff()` | — |
| `DataSet` | `getMatrixDiffInfo(DataSet roleMatrix, DataSet refRoleMatrix, String[] keyColumns)` | — |
| `DataSet` | `getMatrixDiffInfo(DataSet roleMatrix, DataSet refRoleMatrix, String[] keyColumns, boolean sortby)` | — |
| `DataSet` | `getMenuMatrixDiffInfo(DataSet roleMatrix, DataSet refRoleMatrix, String[] keyColumns)` | — |
| `static String` | `getModifiedString(String orig)` | — |
| `static String` | `getNewString(String orig)` | — |
| `DataSet` | `getNodeInfo()` | — |
| `static String` | `getPageEdition(String connectionid, String webpageId)` | — |
| `static String` | `getPageName(String link)` | — |
| `static String` | `getRefTypeValue(QueryProcessor queryProcessor, String reftypeid, String value)` | — |
| `String[]` | `getRolesFromMatrix(DataSet roleMatrix, String[] keyColumns)` | — |
| `protected static SDI` | `getSDI(SDIData sdiData)` | — |
| `protected String[]` | `getSDITableLabelInfo(SDCProcessor sdcProcessor, String sdcid)` | — |
| `String` | `getStartTableInner()` | — |
| `String` | `getStartTableTop()` | — |
| `static String` | `getThumbnailDiff(String value, String refValue, TranslationProcessor translationProcessor)` | — |
| `static String` | `getWizardName(String link)` | — |
| `static boolean` | `hasCollectionChanged(PropertyListCollection src, PropertyListCollection ref)` | — |
| `boolean` | `hasDiffDetailTables(DataSet src, DataSet ref, String[] keycols)` | — |
| `static boolean` | `hasPropertyChanged(String val1, String val2)` | — |
| `static boolean` | `hasPropertyListChanged(PropertyList pl, PropertyList refPl)` | — |
| `int` | `indexOf(String pattern)` | — |
| `String` | `insertDiffAnchors()` | — |
| `static boolean` | `isFKIncluded(String sdcId, String keyid1, HashMap sdisIncluded)` | — |
| `static boolean` | `isFKIncluded(String sdcId, String keyid1, String keyid2, String keyid3, HashMap sdisIncluded)` | — |
| `protected static boolean` | `isImageObselete(String val)` | — |
| `protected boolean` | `isValidActionBlock(String xml)` | — |
| `protected boolean` | `isValidDataSet(String xml)` | — |
| `int` | `length()` | — |
| `void` | `markAsDeleted()` | — |
| `void` | `markAsNew()` | — |
| `void` | `pageBreak()` | — |
| `static DataSet` | `parseDisplayValues(String displayValue)` | — |
| `static PropertyList` | `parseDisplayValues(String displayValue, String folder, String applicationRoot)` | — |
| `static PropertyList` | `parseValidationValues(String validation)` | — |
| `static DataSet` | `removeAuditColumns(DataSet orig)` | — |
| `static String` | `removeIllegalChars(String input)` | — |
| `protected static String` | `removeStrangeChars(String value)` | — |
| `void` | `renderCategories(DataSet categories)` | — |
| `void` | `renderCategoriesDiff(DataSet categories, DataSet refCategories)` | — |
| `ConfigReportContent` | `renderCollection(PropertyListCollection coll, boolean top)` | — |
| `ConfigReportContent` | `renderCollection(PropertyListCollection coll, boolean top, TranslationProcessor translationProcessor)` | — |
| `ConfigReportContent` | `renderCollection(PropertyListCollection coll, PropertyDefinitionList defList, boolean reportAdvancedProperties, boolean top, TranslationProcessor translationProcessor)` | — |
| `ConfigReportContent` | `renderCollectionDiff(PropertyListCollection coll, PropertyListCollection refColl, boolean top, TranslationProcessor translationProcessor, boolean hideEmptyColumns)` | — |
| `ConfigReportContent` | `renderCollectionDiff(PropertyListCollection coll, PropertyListCollection refColl, PropertyDefinitionList defList, boolean reportAdvancedProperties, boolean top, TranslationProcessor translationProcessor, boolean hideEmptyColumns)` | — |
| `ConfigReportContent` | `renderCollectionDiff(String basenode, boolean highlightOverride, boolean hideInheritedProperties, PropertyListCollection coll, PropertyListCollection refColl, PropertyDefinitionList defList, boolean reportAdvancedProperties, boolean top, TranslationProcessor translationProcessor, boolean hideEmptyColumns)` | — |
| `ConfigReportContent` | `renderDeletedPropertyList(PropertyList pl, boolean top, TranslationProcessor translationProcessor)` | — |
| `ConfigReportContent` | `renderDeletedPropertyList(String currnodeid, boolean highlightOverride, boolean hideInheritedProperties, PropertyList pl, boolean top, TranslationProcessor translationProcessor)` | — |
| `void` | `renderDetailTable(HashMap<String,String> columnTitleMap, String detailtable, String tablelabel, String itemdisplay, DataSet src, String[] keycols, TranslationProcessor translationProcessor, boolean hideEmptyColumns)` | — |
| `void` | `renderDetailTablesDiff(HashMap<String,String> columnTitleMap, String detailtable, String tablelabel, String itemdisplay, DataSet src, DataSet ref, String[] keycols, TranslationProcessor translationProcessor, boolean hideEmptyColumns)` | — |
| `void` | `renderDiffListTable(DataSet ds, DataSet ref, String[] keycols)` | — |
| `void` | `renderDiffListTable(DataSet ds, DataSet ref, String[] keycols, boolean top, boolean hideEmptyColumns)` | — |
| `void` | `renderDiffListTable(DataSet ds, DataSet ref, String[] keycols, boolean top, TranslationProcessor translationProcessor, boolean hideEmptyColumns)` | — |
| `void` | `renderDiffListTable(DataSet ds, DataSet ref, String[] keycols, PropertyListCollection ignoreCols, TranslationProcessor translationProcessor)` | — |
| `void` | `renderDiffListTable(DataSet ds, DataSet ref, String[] keycols, TranslationProcessor translationProcessor, boolean hideEmptyColumns)` | — |
| `void` | `renderDiffMatrix(DataSet matrix, DataSet refMatrix, String[] keycols)` | — |
| `void` | `renderDiffRoleMatrix(DataSet roleMatrix, DataSet refRoleMatrix, String[] keycols)` | — |
| `void` | `renderDiffRoleMatrix(DataSet roleMatrix, DataSet refRoleMatrix, String[] keycols, boolean sortby)` | — |
| `static String` | `renderLink(String link, HashMap sdisIncluded, boolean frames, String connectionid)` | — |
| `void` | `renderListTable(DataSet ds, boolean top, TranslationProcessor translationProcessor, String status, boolean hideEmptyColumns)` | — |
| `void` | `renderListTable(DataSet ds, TranslationProcessor translationProcessor)` | — |
| `void` | `renderMatrix(DataSet matrix, int keycols)` | — |
| `ConfigReportContent` | `renderNewPropertyList(PropertyList pl, boolean top, TranslationProcessor translationProcessor)` | — |
| `ConfigReportContent` | `renderNewPropertyList(String currnodeid, boolean highlightoverride, boolean hideinheritedproperties, PropertyList pl, PropertyDefinitionList defList, boolean top, TranslationProcessor translationProcessor)` | — |
| `ConfigReportContent` | `renderOverridePropertyListDiff(String basenode, PropertyList pl, PropertyList refPl, PropertyDefinitionList defList, boolean reportAdvancedProperties, boolean top, boolean hideInheritedProperties, TranslationProcessor translationProcessor, boolean hideEmptyColumns)` | — |
| `void` | `renderProcessingScript(String applicationRoot, String folder, String processingScript, TranslationProcessor translationProcessor, boolean configreport)` | — |
| `void` | `renderProcessingScript(String processingScript, TranslationProcessor translationProcessor)` | — |
| `void` | `renderProcessingScriptDiff(String srcProcessingScript, String refProcessingScript, boolean showTranslation, TranslationProcessor translationProcessor)` | — |
| `void` | `renderProcessingScriptDiff(String applicationRoot, String folder, String srcProcessingScript, String refProcessingScript, boolean showTranslation, TranslationProcessor translationProcessor, boolean configreport)` | — |
| `ConfigReportContent` | `renderPropertyDefinitionList(String defpropertyid, String defpropertytitle, PropertyDefinitionList srcdeflist, PropertyDefinitionList refdeflist, TranslationProcessor translationProcessor, boolean inner)` | — |
| `ConfigReportContent` | `renderPropertyList(PropertyList pl, boolean top)` | — |
| `ConfigReportContent` | `renderPropertyList(PropertyList pl, boolean top, TranslationProcessor translationProcessor)` | — |
| `ConfigReportContent` | `renderPropertyList(PropertyList pl, PropertyDefinitionList defList, boolean reportAdvancedProperties, boolean top, TranslationProcessor translationProcessor)` | — |
| `ConfigReportContent` | `renderPropertyListAttributesDiff(PropertyList pl, PropertyList refPl)` | — |
| `ConfigReportContent` | `renderPropertyListDiff(PropertyList pl, PropertyList refPl, boolean top, TranslationProcessor translationProcessor)` | — |
| `ConfigReportContent` | `renderPropertyListDiff(PropertyList pl, PropertyList refPl, PropertyDefinitionList defList, boolean reportAdvancedProperties, boolean top, boolean hideEmptyColumns)` | — |
| `ConfigReportContent` | `renderPropertyListDiff(PropertyList pl, PropertyList refPl, PropertyDefinitionList defList, boolean reportAdvancedProperties, boolean top, TranslationProcessor translationProcessor, boolean hideEmptyColumns)` | — |
| `ConfigReportContent` | `renderPropertyListDiff(String basenode, boolean highlightoverride, boolean hideInheritedProperties, PropertyList pl, PropertyList refPl, PropertyDefinitionList defList, boolean reportAdvancedProperties, boolean top, TranslationProcessor translationProcessor, boolean hideEmptyColumns)` | — |
| `void` | `renderRoleMatrix(DataSet roleMatrix, int keycols)` | — |
| `void` | `setFoundDiff(boolean val)` | — |
| `void` | `startBulletList()` | — |
| `ConfigReportContent` | `startChapter(String chapterNo, String sdcId, String preamble)` | — |
| `void` | `startDeletedSubSection(String title, String desc)` | — |
| `void` | `startFile()` | — |
| `void` | `startFile(String subSectionFileName)` | — |
| `void` | `startHeader()` | — |
| `void` | `startListTableInner()` | — |
| `void` | `startListTableTop()` | — |
| `void` | `startNewSubSection(String title, String desc)` | — |
| `void` | `startReport(int chapterCount, String firstChapter, boolean hideChapterTOC, boolean hideSubsections)` | — |
| `void` | `startRow()` | — |
| `void` | `startSDISection(SDI currentSDI, String desc)` | — |
| `void` | `startSDISection(SDI currentSDI, String sectionTitle, String desc)` | — |
| `void` | `startSDISectionDiff(SDCProcessor sdcProcessor, SDIData sdiData, SDI currentSDI, String desc, String refDesc)` | — |
| `void` | `startSDISectionDiff(SDI currentSDI, String desc, String refDesc)` | — |
| `void` | `startSDISectionDiff(SDI currentSDI, String sectionTitle, String desc, String refDesc)` | — |
| `void` | `startSection(String sectionIdentifier)` | — |
| `void` | `startSubHeading(String title, String desc)` | — |
| `void` | `startSubHeading(String title, String desc, String anchor)` | — |
| `void` | `startSubSection(String title, String desc)` | — |
| `void` | `startTable()` | — |
| `void` | `startTableInner()` | — |
| `String` | `toString()` | — |

</details>

#### `LogonRequestValidator` (Interface)
Created by cliu on 8/24/2015.

<details>
<summary>View public methods (3)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `boolean` | `isRequireSysuserInfo()` | System calls validateRequest with Request and DataSet sysuser if this method returns true and with Request only if returns false |
| `String` | `validateRequest(HttpServletRequest request)` | implement this method to allow customized validation of request attributes to further determine whether to authorize access to the application after initial authentication return not allow access message html or empty string if pass validation. |
| `String` | `validateRequest(HttpServletRequest request, DataSet sysuser)` | implement this method to allow customized validation of request attributes to further determine whether to authorize access to the application after initial authentication return not allow access message html or empty string if pass validation. |

</details>

---

### Package `sapphire.pageelements`

#### `BaseElement` (Class)
Checks if the element is visible in Add mode(specifically for detail elements)

*Declaration:* `public class BaseElement extends com.labvantage.sapphire.BaseCustom`

<details>
<summary>View public methods (26)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `PropertyList` | `getElementProperties()` | — |
| `protected String` | `getError()` | — |
| `abstract String` | `getHtml()` | — |
| `protected String` | `getJavaScriptAPI()` | — |
| `String` | `getSDIFormId()` | — |
| `SDITagInfo` | `getSDIInfo()` | — |
| `protected TranslationProcessor` | `getTranslationProcessor()` | — |
| `boolean` | `isVisibleInAddMode()` | Checks if the element is visible in Add mode(specifically for detail elements) |
| `protected void` | `logDebug(String message)` | **[Deprecated]** As of version 5.0 replaced by Logger API. Deprecated. As of version 5.0 replaced by Logger API |
| `protected void` | `logError(String errormsg)` | **[Deprecated]** As of version 5.0 replaced by Logger API. Deprecated. As of version 5.0 replaced by Logger API |
| `protected void` | `logError(String errormsg, Throwable exception)` | **[Deprecated]** As of version 5.0 replaced by Logger API. Deprecated. As of version 5.0 replaced by Logger API |
| `protected void` | `logInfo(String message)` | **[Deprecated]** As of version 5.0 replaced by Logger API. Deprecated. As of version 5.0 replaced by Logger API |
| `protected void` | `logTrace(String tracemsg)` | **[Deprecated]** As of version 5.0 replaced by Logger API. Deprecated. As of version 5.0 replaced by Logger API |
| `protected void` | `logWarn(String message)` | **[Deprecated]** As of version 5.0 replaced by Logger API. Deprecated. As of version 5.0 replaced by Logger API |
| `void` | `setBrowser(Browser browser)` | — |
| `void` | `setElementClass(String elementClass)` | — |
| `void` | `setElementid(String elementid)` | — |
| `void` | `setElementProperties(PropertyList properties)` | — |
| `void` | `setElementProperties(String properties)` | Sets the properties for the element using a JSTL expression |
| `static void` | `setElementResolution(PropertyList element, Browser browser)` | — |
| `void` | `setElementType(String elementType)` | — |
| `void` | `setPageContext(PageContext pageContext)` | — |
| `void` | `setPrefix(String prefix)` | — |
| `void` | `setSDIFormId(String sdiFormId)` | — |
| `void` | `setSDIInfo(SDITagInfo sdiInfo)` | — |
| `void` | `setSuffix(String suffix)` | — |

</details>

#### `BaseGizmo` (Class)
Use this to obtain dynamic script when element is rendered by ajax

*Declaration:* `public class BaseGizmo extends com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo`

<details>
<summary>View public methods (1)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `abstract String` | `getScript()` | Use this to obtain dynamic script when element is rendered by ajax |

</details>

---

### Package `sapphire.report`

#### `BaseJavaReport` (Class)
This is the ancestor class for all Java-based reports. These classes should be registered against a corresponding report sdi
The following is an OOB Java report class that accepts a 'keyid1' QCBatchId and generates an excel file with each of the samples from QCBatcItems.
public class QCBatchRunFile extends BaseJavaReport {
private HashMap paramsMap;
public void init( String reportid, String reportVersionid, HashMap paramsMap, ConnectionInfo connectionInfo ) {
// Remember the params. We will need this later
this.paramsMap = paramsMap;
}
public String getLogicalFileName( String defaultFileName ) {
// This file always returns a .xslx file - but will allow an alternative filename to be provided
return defaultFileName.endsWith( ".xlsx" ) ? defaultFileName : "sequence.xlsx";
}
public String[] getReportParameters() {
return new String[] { "keyid1" };
}
public void runReport( OutputStream outputStream ) throws SapphireException {
// Load the sampleids from qcbatchitem
String qcbatchid = (String)paramsMap.get( "keyid1" );
SafeSQL safeSQL = new SafeSQL();
DataSet ds = getQueryProcessor().getPreparedSqlDataSet( "select distinct keyid1 from sdidata where s_qcbatchid=" + safeSQL.addVar( qcbatchid ), safeSQL.getValues() );
String runfilecontent = ds.toString()    // Convert the dataset into a String with the required content
try {
outputStream.write( runfilecontent.getBytes() );
}
catch ( IOException e ) {
throw new SapphireException( "Unable to stream the report back" );
}
}
}

*Declaration:* `public class BaseJavaReport extends com.labvantage.sapphire.BaseCustom implements com.labvantage.sapphire.report.ReportConstants`

<details>
<summary>View public methods (10)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `void` | `adjustPrintOptions(PrintReportOptions options)` | Prior to sending the output file to a printer, you can override this method to adjust the print options if needed. |
| `boolean` | `canPrint()` | — |
| `ClassLoader` | `getClassLoader()` | — |
| `abstract String` | `getLogicalFileName(String defaultFileName)` | Return the logical name of the file. |
| `String` | `getMimeType(String filename)` | LabVantage will try to assign a mimi-type for known filetypes. |
| `String[]` | `getReportParameters()` | — |
| `abstract void` | `init(String reportid, String reportVersionid, HashMap paramsMap, ConnectionInfo connectionInfo)` | In this oiverride you can retain this information needed downstream, such as the report parameters |
| `abstract void` | `runReport(OutputStream outputStream)` | Send the contents of the report to this output stream to be sent to a browser or to a file |
| `void` | `sendToPrinter(String printerName, File file, PrintReportOptions options)` | LabVantage knows how to print various file types, include excel, word, pdf, text and images. |
| `void` | `setClassLoader(ClassLoader classLoader)` | — |

</details>

#### `JasperReportScriptlet` (Class)
Default JasperScriplet class for sapphire jasper report.
Providing sapphire formatting according to sapphire user's locale, timezone and language profile

*Declaration:* `public class JasperReportScriptlet extends JRDefaultScriptlet`

<details>
<summary>View public methods (32)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `void` | `beforeDetailEval()` | Sapphire implementation of JRDefaultScriptlet. |
| `void` | `beforeReportInit()` | Sapphire implementation of JRDefaultScriptlet. |
| `String` | `format(Object valueobject)` | Provide default sapphire formatting support |
| `String` | `formatDataEntryDisplay(String displayvalue, String datatype, Object transformvalue, Object transformdt, String displayformat, String displayvalueformat)` | Provide sapphire formatting support for dataentry displayvalue |
| `String` | `formatDate(Calendar datetime)` | Provide sapphire date time formatting support |
| `String` | `formatDate(Calendar datetime, int datePattern, int timePattern)` | Provide sapphire date time formatting support |
| `String` | `formatDate(Date datetime)` | Provide sapphire date time formatting support |
| `String` | `formatDate(Date datetime, int datePattern, int timePattern)` | Provide sapphire date time formatting support |
| `String` | `formatDate(Timestamp datetime)` | Provide sapphire date time formatting support |
| `String` | `formatDate(Timestamp datetime, int datePattern, int timePattern)` | Provide sapphire date time formatting support |
| `String` | `formatDateOnly(Calendar datetime)` | Provide sapphire date only formatting support |
| `String` | `formatDateOnly(Calendar datetime, int datePattern)` | Provide sapphire date time formatting support |
| `String` | `formatDateOnly(Date datetime)` | Provide sapphire date time formatting support |
| `String` | `formatDateOnly(Date datetime, int datePattern)` | Provide sapphire date time formatting support |
| `String` | `formatDateOnly(Timestamp datetime)` | Provide sapphire date time formatting support |
| `String` | `formatDateOnly(Timestamp datetime, int datePattern)` | Provide sapphire date time formatting support |
| `String` | `formatNumber(BigDecimal num)` | Provide sapphire number formatting support |
| `String` | `formatNumber(BigDecimal num, boolean group)` | Provide sapphire number formatting support |
| `String` | `getAddress()` | — |
| `String` | `getAddress(String columnname)` | — |
| `ByteArrayInputStream` | `getAttachment(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentNum, String connectonId)` | Returns Bytes array of attachment |
| `ByteArrayInputStream` | `getAttachment(String sdcid, String keyid1, String keyid2, String keyid3, String attachmentClass, String connectonId)` | Returns Bytes array of attachment |
| `ConnectionInfo` | `getConnectionInfo()` | — |
| `HashMap` | `getFieldValueMap()` | — |
| `ByteArrayInputStream` | `getLogo()` | — |
| `ByteArrayInputStream` | `getLogoByte()` | — |
| `static Calendar` | `getOffsetDate(Timestamp offsetfromDt, String periodUnit, BigDecimal period)` | Provide date calculation support with adding or reducing the offsetfromDt with the input period unit. |
| `void` | `setLogo(ByteArrayInputStream logoByte)` | — |
| `String` | `text(String key)` | This method now internally call getAdress() method to get the Address if the key value is Address. |
| `String` | `translate(String text)` | Provide sapphire translation support |
| `String` | `translate(String text, String context)` | Provide sapphire translation support |
| `String` | `translate(String text, String context, String languageid)` | Provide sapphire translation support |

</details>

#### `PrintReportOptions` (Class)
Used to control how custom java reports get printed.

*Declaration:* `public class PrintReportOptions extends java.lang.Object`

<details>
<summary>View public methods (3)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `void` | `setExcelSheetIndexToPrint(int excelSheetIndexToPrint)` | Sets the sheet index number to print. |
| `void` | `setImagePrintFavor(INPUT_STREAM imagePrintFavor)` | Sets or overrides the print flavor when printing images |
| `void` | `setPrintMode(int printMode)` | Sets or overrides the print mode. |

</details>

#### `QCBatchRunFile` (Class)
Java Report class to output list of sampleids from QCBatch.

*Declaration:* `public class QCBatchRunFile extends BaseJavaReport`

<details>
<summary>View public methods (5)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `String` | `getLogicalFileName(String defaultFileName)` | Return the logical name of the file. |
| `String[]` | `getReportParameters()` | — |
| `protected StringBuffer` | `getTextFromDataSet(DataSet ds, List<String> titles, List<String> columnIds)` | — |
| `void` | `init(String reportid, String reportVersionid, HashMap paramsMap, ConnectionInfo connectionInfo)` | In this oiverride you can retain this information needed downstream, such as the report parameters |
| `void` | `runReport(OutputStream outputStream)` | Send the contents of the report to this output stream to be sent to a browser or to a file |

</details>

---

### Package `sapphire.servlet`

#### `BaseAjaxRequest` (Class)
Implementation of this abstract class provides server side handling of Ajax calls using command=ajax&ajaxclass=fullqualifiedclassname.
The ajaxclass should implement
public abstract void processRequest( HttpServletRequest request, HttpServletResponse response, ServletContext servletContext ) throws ServletException;

*Declaration:* `public class BaseAjaxRequest extends com.labvantage.sapphire.servlet.command.BaseRequest`

<details>
<summary>View public methods (13)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `boolean` | `acceptContentType(String contentType)` | Override this to allow certain ajax commands which are not the standard form or json |
| `void` | `close()` | — |
| `BaseHttpServlet` | `getServlet()` | — |
| `protected void` | `logDebug(String msg)` | The debug method |
| `protected void` | `logError(String msg)` | The error debug override method |
| `protected void` | `logError(String msg, Throwable exception)` | The error debug override method |
| `protected void` | `logInfo(String msg)` | The info debug method |
| `protected void` | `logWarn(String msg)` | The warning debug method |
| `void` | `open(PrintWriter out)` | — |
| `protected void` | `print(Object output)` | — |
| `protected void` | `println(Object output)` | — |
| `void` | `setServlet(BaseHttpServlet servlet)` | — |
| `protected void` | `write(Object output)` | — |

</details>

#### `BaseExternalHandler` (Class)
Internal.

*Declaration:* `public class BaseExternalHandler extends com.labvantage.sapphire.BaseCustom implements com.labvantage.sapphire.util.cache.CacheNames, com.labvantage.sapphire.servlet.externalapp.ExternalAppConstants`

<details>
<summary>View public methods (15)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `static String` | `getAuthenticationValue(String authenticationParam, HttpServletRequest request, HttpServletResponse response)` | Internal. |
| `String` | `getDatabaseId()` | Get the databaseid this command handler is associated with |
| `String` | `getExternalAppid()` | Get the external app this command handler is associated with |
| `String` | `getExternalUserid()` | Get the external user this command handler is associated with |
| `static BaseExternalHandler` | `getInstance(HttpServletRequest request, HttpServletResponse response)` | Internal. |
| `String` | `getProcessAsUserId()` | Get the process-as user this command handler is associated with |
| `JSONObject` | `processCommand(String command, JSONObject commandRequest)` | Overide this method to handle commands coming from an external app. |
| `PropertyList` | `processCommand(String command, PropertyList commandRequest)` | Overide this method to handle commands coming from an external app. |
| `JSONObject` | `processFileCommand(String command, Path file, JSONObject commandRequest)` | Overide this method to handle commands coming from an external app that are accompanited by a file. |
| `JSONObject` | `processFileCommand(String command, String filename, InputStream inputStream, JSONObject commandRequest)` | Overide this method to handle commands coming from an external app that are accompanited by a file. |
| `File` | `processFileDownloadCommand(String command, JSONObject commandRequest)` | Overide this method to handle commands coming from an external app that require a file to be downloaded |
| `void` | `setDatabaseId(String databaseId)` | Internal. |
| `void` | `setExternalAppid(String externalAppid)` | Internal. |
| `void` | `setExternalUserid(String externalUserid)` | Internal. |
| `void` | `setProcessAsUserId(String processAsUserId)` | Internal. |

</details>

#### `BaseHttpServlet` (Class)
Internal class. This class and it's methods are not currently supported.

*Declaration:* `public class BaseHttpServlet extends HttpServlet`

<details>
<summary>View public methods (7)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `void` | `doInit()` | — |
| `protected String` | `getNameserverlist()` | Deprecated. |
| `void` | `goErrorPage(HttpServletRequest request, HttpServletResponse response, String errorpage, String errormsg)` | — |
| `protected void` | `logError(String errormsg)` | — |
| `protected void` | `logError(String errormsg, Exception exception)` | — |
| `protected void` | `logTrace(String tracemsg)` | — |
| `protected void` | `setNameserverlist(String nameserverlist)` | Deprecated. |

</details>

#### `ExternalHandlerProcessor` (Class)
Internal.

*Declaration:* `public class ExternalHandlerProcessor extends java.lang.Object implements com.labvantage.sapphire.servlet.externalapp.ExternalAppConstants`

<details>
<summary>View public methods (17)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `String` | `checkConnection(String connectionid)` | — |
| `void` | `clearConnection(String connectionid)` | — |
| `String` | `getConnectionId(String username, String password, String database)` | — |
| `HttpURLConnection` | `getHttpURLConnection(String processAs)` | Internal. |
| `JSONObject` | `getJsonObject(HttpURLConnection conn, JSONObject jsonResponse)` | Internal. |
| `String` | `getToken()` | — |
| `boolean` | `isTokenActive()` | Internal. |
| `String` | `requestToken(String authorizationcode, String requestReason)` | Internal. |
| `String` | `requestToken(String authorizationCode, String requestReason, String externalUserid)` | Internal. |
| `JSONObject` | `sendCommandToLIMS(String command, JSONObject jsonRequest)` | Use this to send a JSON command to LIMS. |
| `PropertyList` | `sendCommandToLIMS(String command, PropertyList commandRequest)` | Use this to send a PropertyList command to LIMS. |
| `JSONObject` | `sendCommandToLIMS(String processAs, String command, JSONObject jsonRequest)` | Use this to send a JSON command to LIMS. |
| `PropertyList` | `sendCommandToLIMS(String processas, String command, PropertyList commandRequest)` | Use this to send a PropertyList command to LIMS. |
| `void` | `sendDownloadFileCommandToLIMS(String command, JSONObject commandRequest, Path targetFile)` | — |
| `void` | `sendDownloadFileCommandToLIMS(String processAs, String command, JSONObject jsonRequest, Path targetFile)` | — |
| `JSONObject` | `sendFileCommandToLIMS(String command, Path file, JSONObject jsonRequest)` | Use this to send a JSON command to LIMS with an accompanying file. |
| `JSONObject` | `sendFileCommandToLIMS(String processAs, String command, Path file, JSONObject jsonRequest)` | Use this to send a JSON command to LIMS with an accompanying file. |

</details>

#### `RequestContext` (Class)
Provides request information to JSPs. The information is
one of three types of properties. Page properties are properties that come from
the webpage property table in the database. Request properties contain
properties that have been submitted from a form or pass through the URL.
Finally, Session properties contains a "sysuserid" property which is the
Sapphire username of whoever is currently using the page. The Session
properties also contains some profile property values; specifically
bousername, bopassword and bouniverse.
such as the username.

*Declaration:* `public class RequestContext extends com.labvantage.sapphire.servlet.BaseContext implements java.io.Serializable`

<details>
<summary>View public methods (14)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `boolean` | `copyRequestParameters()` | Internal method. |
| `String` | `getConnectionid()` | Return the connection ID in use by the current page |
| `String` | `getConnectionId()` | Return the connection ID in use by the current page |
| `static RequestContext` | `getInstance(HttpServletRequest request)` | Internal method. |
| `static RequestContext` | `getRequestContext(HttpServletRequest request)` | Internal method. |
| `static RequestContext` | `getRequestContext(PageContext pageContext)` | Internal method. |
| `String` | `getRequestId()` | Return the requestID of this request context |
| `boolean` | `isControlledPage()` | Return whether the page is controlled page or not. |
| `boolean` | `isRtl()` | — |
| `void` | `setConnectionId(String connectionId)` | Internal method. |
| `void` | `setControlledPage(boolean controlledPage)` | Internal method. |
| `void` | `setCopyRequestParameters(boolean copy)` | Internal method. |
| `void` | `setRequestId(String requestId)` | Internal method. |
| `void` | `setRtl(boolean rtl)` | — |

</details>

#### `RestClient` (Class)
REST Client

*Declaration:* `public class RestClient extends java.lang.Object implements com.labvantage.sapphire.servlet.rest.RestConstants`

<details>
<summary>View public methods (14)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `void` | `clearConnection(String connectionid)` | — |
| `boolean` | `connect(String tokenvalue)` | — |
| `boolean` | `connect(String databaseid, String username, String password)` | — |
| `JSONObject` | `delete(String resource)` | — |
| `void` | `disconnect()` | — |
| `static String` | `encode(String input)` | — |
| `JSONObject` | `get(String resource)` | — |
| `String` | `getConnection()` | — |
| `String` | `getConnection(String databaseid, String username, String password)` | — |
| `static String` | `getFieldsParam(String[] fields)` | — |
| `boolean` | `isConnected()` | — |
| `JSONObject` | `post(String resource, JSONObject jsonParams)` | — |
| `JSONObject` | `put(String resource, JSONObject jsonParams)` | — |
| `String` | `status(String databaseid)` | — |

</details>

#### `SessionContext` (Class)
*Declaration:* `public class SessionContext extends com.labvantage.sapphire.servlet.BaseContext`

---

### Package `sapphire.tagext`

#### `ActionTagInfo` (Class)
Default Constructor

*Declaration:* `public class ActionTagInfo extends BaseTagInfo`

<details>
<summary>View public methods (18)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `String` | `getActionClass(int actionindex)` | Returns the actionid for the given index of an acion in the block |
| `String` | `getActionClass(String name)` | Returns the actionid for the given action block name |
| `int` | `getActionCount()` | Returns the number of actions defined in the block. |
| `String` | `getActionid(int actionindex)` | Returns the actionid for the given index of an acion in the block |
| `String` | `getActionid(String name)` | Returns the actionid for the given action block name |
| `String` | `getActionName(int actionindex)` | Returns the name of an action for the given index |
| `HashMap` | `getActionProperties(int actionindex)` | Gets the property values for the given Action in the Block. |
| `HashMap` | `getActionProperties(String name)` | Gets the property values for the given Action in the Block. |
| `String` | `getActionProperty(int actionindex, String propertyid)` | Gets a property value for the given Action in the Block. |
| `String` | `getActionProperty(String name, String propertyid)` | Gets a property value for the given Action in the Block. |
| `HashMap` | `getBlockProperties()` | Gets the properties for the block. |
| `String` | `getBlockProperty(String propertyid)` | Gets a block property. |
| `ErrorHandler` | `getErrorHandler()` | Gets the error handler for the block. |
| `String` | `getVersionid(int actionindex)` | Returns the versionid for the given index of an acion in the block |
| `String` | `getVersionid(String name)` | Returns the versionid for the given action block name |
| `boolean` | `hasErrors()` | — |
| `boolean` | `hasInfoErrors()` | — |
| `void` | `setErrorHandler(ErrorHandler errorHandler)` | Sets the errorHandlerr for the block. |

</details>

#### `BaseBodyTagSupport` (Class)
Internal class. This class and it's methods are not currently supported.

*Declaration:* `public class BaseBodyTagSupport extends BodyTagSupport`

<details>
<summary>View public methods (21)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `int` | `doEndTag()` | — |
| `void` | `doInit()` | — |
| `String` | `getAttribute(String paramid)` | — |
| `String` | `getAttribute(String paramid, String defaultValue)` | — |
| `PropertyList` | `getAttributes()` | — |
| `protected String` | `getConnectionId()` | — |
| `String` | `getErrorpage()` | — |
| `protected String` | `getNameserverlist()` | Deprecated. |
| `protected RequestContext` | `getRequestContext()` | — |
| `void` | `goErrorPage(String errormsg)` | — |
| `void` | `goErrorPage(String errormsg, String extraparameters)` | — |
| `boolean` | `isControlledPage()` | — |
| `protected void` | `logDebug(Object message)` | — |
| `protected void` | `logError(String errormsg)` | — |
| `protected void` | `logError(String errormsg, Exception exception)` | — |
| `protected void` | `logTrace(String tracemsg)` | — |
| `void` | `setAttribute(String paramid, String paramvalue)` | — |
| `void` | `setErrorpage(String errorpage)` | — |
| `protected void` | `setNameserverlist(String nameserverlist)` | Deprecated. |
| `protected void` | `write(String output)` | — |
| `protected void` | `writeBodyContent()` | — |

</details>

#### `BaseTagInfo` (Class)
Default Constructor

*Declaration:* `public class BaseTagInfo extends com.labvantage.sapphire.BaseClass`

<details>
<summary>View public methods (4)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `String` | `getErrorCodes()` | Retreives the list of error codes. |
| `String` | `getErrorStack(String endlinestring)` | Returns the content of the error stack in a string. |
| `String` | `getLastError()` | Gets the last error message contained in the error stack. |
| `void` | `setErrorStack(String errorcodes, ArrayList errorstack)` | Appends the error codes and error stack to the existing ones. |

</details>

#### `BaseTagSupport` (Class)
Internal class. This class and it's methods are not currently supported.

*Declaration:* `public class BaseTagSupport extends TagSupport`

<details>
<summary>View public methods (20)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `int` | `doEndTag()` | — |
| `void` | `doInit()` | — |
| `String` | `getAttribute(String paramid)` | — |
| `String` | `getAttribute(String paramid, String defaultValue)` | — |
| `PropertyList` | `getAttributes()` | — |
| `protected String` | `getConnectionId()` | — |
| `String` | `getErrorpage()` | — |
| `protected String` | `getNameserverlist()` | Deprecated. |
| `protected RequestContext` | `getRequestContext()` | — |
| `void` | `goErrorPage(String errormsg)` | — |
| `void` | `goErrorPage(String errormsg, String extraparameters)` | — |
| `boolean` | `isControlledPage()` | — |
| `protected void` | `logDebug(Object message)` | — |
| `protected void` | `logError(String errormsg)` | — |
| `protected void` | `logError(String errormsg, Exception exception)` | — |
| `protected void` | `logTrace(String tracemsg)` | — |
| `void` | `setAttribute(String paramid, String paramvalue)` | — |
| `void` | `setErrorpage(String errorpage)` | — |
| `protected void` | `setNameserverlist(String nameserverlist)` | Deprecated. |
| `protected void` | `write(String output)` | — |

</details>

#### `ControlledPageTagInfo` (Class)
Return the connection ID in use by the current page

*Declaration:* `public class ControlledPageTagInfo extends BaseTagInfo`

<details>
<summary>View public methods (28)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `String` | `getConnectionid()` | — |
| `String` | `getConnectionId()` | Return the connection ID in use by the current page |
| `String` | `getDecodedProperty(String propertyid)` | Returns the first property value found in the Request properties, then the Page Properties and finally the Session properties. |
| `String` | `getEncodedProperty(String propertyid)` | Returns the first property value found in the Request properties, then the Page Properties and finally the Session properties. |
| `HashMap` | `getPageProperties()` | NOTE: Legacy method with altered functionality Return a HashMap containing all request, page and session properties. |
| `String` | `getPageProperty(String propertyid)` | Return a value from a page property |
| `HashMap` | `getProperties()` | Return a HashMap containing all request, page and session properties. |
| `String` | `getProperty(String propertyid)` | Returns the first property value found in the Request properties, then the Page Properties and finally the Session properties. |
| `HashMap` | `getRequestProperties()` | NOTE: Legacy method with altered functionality Return a HashMap containing all request, page and session properties. |
| `String` | `getRequestProperty(String propertyid)` | Return a value from a request property |
| `HashMap` | `getSessionProperties()` | NOTE: Legacy method with altered functionality Return a HashMap containing all request, page and session properties. |
| `String` | `getSessionProperty(String propertyid)` | Return a value from a session property |
| `boolean` | `isControlledpage()` | Return whether the page is controlled page or not. |
| `boolean` | `isCopyrequestparameters()` | Internal method. |
| `boolean` | `isPageProperty(String propertyid)` | Return whether a property is a page property |
| `boolean` | `isProperty(String propertyid)` | Return a property is either a request, page or session property. |
| `boolean` | `isRequestProperty(String propertyid)` | Return whether a property is a request property |
| `boolean` | `isSessionProperty(String propertyid)` | Return whether a property is a session property |
| `void` | `resetPageProperties()` | Clear all page properties |
| `void` | `resetProperties()` | Clear all request, context, page, and session properties |
| `void` | `resetRequestProperties()` | Clear all request properties |
| `void` | `resetSessionProperties()` | Clear all session properties |
| `void` | `setConnectionid(String connectionid)` | Internal method. |
| `void` | `setControlledpage(boolean controlledpage)` | Internal method. |
| `void` | `setCopyrequestparameters(boolean copy)` | Internal method. |
| `void` | `setPageProperty(String propertyid, String propertyvalue)` | Set a value for a page property. |
| `void` | `setRequestProperty(String propertyid, String propertyvalue)` | Set a value for a request property. |
| `void` | `setSessionProperty(String propertyid, String propertyvalue)` | Internal method. |

</details>

#### `PageTagInfo` (Class)
Deprecated.

*Declaration:* `public class PageTagInfo extends BaseTagInfo`

<details>
<summary>View public methods (18)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `ActionProcessor` | `getActionProcessor()` | — |
| `String` | `getConnectionid()` | — |
| `String` | `getConnectionId()` | — |
| `ConnectionProcessor` | `getConnectionProcessor()` | — |
| `String` | `getDatabaseid()` | — |
| `String` | `getNameserverlist()` | Deprecated. |
| `String` | `getNameServerList()` | Deprecated. |
| `String` | `getProperty(String propertyId)` | — |
| `String` | `getProperty(String propertyId, String defaultValue)` | — |
| `PropertyList` | `getPropertyList()` | — |
| `PropertyList` | `getPropertyList(String propertyId)` | — |
| `QueryProcessor` | `getQueryProcessor()` | — |
| `SDCProcessor` | `getSDCProcessor()` | — |
| `SDIProcessor` | `getSDIProcessor()` | — |
| `TaskContext` | `getTaskContext()` | — |
| `boolean` | `isRTL()` | — |
| `boolean` | `isTaskPage()` | — |
| `void` | `setTaskPage(boolean taskPage)` | — |

</details>

#### `QueryTagInfo` (Class)
Supports query tag information access

*Declaration:* `public class QueryTagInfo extends BaseTagInfo`

<details>
<summary>View public methods (20)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `int` | `findRow(String find)` | Returns the row number of the first row found matching a specified find String. |
| `BigDecimal` | `getBigDecimal(int row, String columnid)` | Get a BigDecimal value from a Numeric column. |
| `BigDecimal` | `getBigDecimal(String columnid)` | Get a BigDecimal value from a Numeric column for the current row. |
| `Calendar` | `getCalendar(int row, String columnid)` | Get a Calendar object from a Date column. |
| `Calendar` | `getCalendar(String columnid)` | Get a Calendar object from a Date column for the current row. |
| `int` | `getColumnCount()` | Returns the number of columns. |
| `int` | `getCurrentCol()` | Returns the current column number. |
| `String` | `getCurrentColId()` | Returns the current column ID |
| `int` | `getCurrentRow()` | Returns the current row number. |
| `int` | `getInt(int row, String columnid)` | Get an int value from a Numeric column. |
| `int` | `getInt(String columnid)` | Get an int value from a Numeric column for the current row. |
| `Object` | `getObject(int row, String columnid)` | Returns an object containing the String, BigDecimal or Calendar for the specified row / column. |
| `Object` | `getObject(String columnid)` | Returns an object containing the String, BigDecimal or Calendar for the current row. |
| `int` | `getRowCount()` | Returns the number of rows. |
| `String` | `getString(int row, String columnid)` | Get a string value from a String column. |
| `String` | `getString(String columnid)` | Get a string value from a String column for the current row. |
| `String` | `getValue(int col)` | Returns a String representing the value for the specified column, independant of column type. |
| `String` | `getValue(int row, int col)` | Returns a String representing the value for the specified row / column, independant of column type. |
| `String` | `getValue(int row, String columnid)` | Returns a String representing the value for the specified row / column, independant of column type. |
| `String` | `getValue(String columnid)` | Returns a String representing the value for the current row, independant of column type. |

</details>

#### `RefTypeTagInfo` (Class)
Provides access to the underlying data structre that drives the  tags.

*Declaration:* `public class RefTypeTagInfo extends BaseTagInfo`

<details>
<summary>View public methods (6)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `int` | `getColumnCount()` | Returns the number of columns (which may hold additional information about the reference value). |
| `int` | `getCurrentCol()` | Returns the current column number. |
| `int` | `getCurrentRow()` | Returns the current row number. |
| `int` | `getRowCount()` | Returns the number of rows (reference values). |
| `String` | `getValue(int row, String columnid)` | Returns string reference item. |
| `String` | `getValue(String columnid)` | Returns the current reference value. |

</details>

#### `SDIFormFailureTagInfo` (Class)
Gets the last error message contained in the error stack.

*Declaration:* `public class SDIFormFailureTagInfo extends BaseTagInfo`

<details>
<summary>View public methods (8)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `ErrorHandler` | `getErrorHandler()` | — |
| `String` | `getErrorString()` | — |
| `String` | `getLastError()` | Gets the last error message contained in the error stack. |
| `String` | `getLastErrorParsed()` | — |
| `String` | `getLastErrorParsed(boolean forErrorHandler)` | — |
| `boolean` | `hasErrors()` | — |
| `void` | `setErrorString(String errorString)` | — |
| `void` | `setLastError(String lastError)` | — |

</details>

#### `SDIFormSuccessTagInfo` (Class)
*Declaration:* `public class SDIFormSuccessTagInfo extends BaseTagInfo`

<details>
<summary>View public methods (5)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `ErrorHandler` | `getErrorHandler()` | — |
| `String` | `getInfoErrorString()` | — |
| `boolean` | `hasErrors()` | — |
| `boolean` | `hasInfoErrors()` | — |
| `void` | `setInfoErrorString(String infoErrorString)` | — |

</details>

#### `SDITagInfo` (Class)
Supports custom tag functions

*Declaration:* `public class SDITagInfo extends BaseTagInfo`

<details>
<summary>View public methods (42)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `int` | `findRow(String dataset, String find)` | Returns the row number of the first row found matching a specified find String. |
| `BigDecimal` | `getBigDecimal(String dataset, int row, String columnid)` | Get a BigDecimal value from a Numeric column. |
| `BigDecimal` | `getBigDecimal(String dataset, String columnid)` | Get a BigDecimal value from a Numeric column for the current row. |
| `Calendar` | `getCalendar(String dataset, int row, String columnid)` | Get a Calendar object from a Date column. |
| `Calendar` | `getCalendar(String dataset, String columnid)` | Get a Calendar object from a Date column for the current row. |
| `Object` | `getChildTagData(String dataname)` | Internal method, not currently supported. |
| `int` | `getColumnCount(String dataset)` | Returns the number of columns. |
| `int` | `getCurrentCol(String dataset)` | Returns the current column number. |
| `String` | `getCurrentColId(String dataset)` | Returns the current column number. |
| `int` | `getCurrentRow()` | Returns the current row number for the primary dataset. |
| `int` | `getCurrentRow(String dataset)` | Returns the current row number. |
| `DataSet` | `getDataSet(String dataset)` | Returns a sapphire.util.DataSet object for the specified dataset |
| `String` | `getError()` | Returns any errors that may have been generated, particularly after a "round-trip". |
| `int` | `getInt(String dataset, int row, String columnid)` | Get an int value from a Numeric column. |
| `int` | `getInt(String dataset, String columnid)` | Get an int value from a Numeric column for the current row. |
| `String[]` | `getKeycols()` | — |
| `Object` | `getObject(String dataset, int row, String columnid)` | Returns an object containing the String, BigDecimal or Calendar for the specified row / column. |
| `Object` | `getObject(String dataset, String columnid)` | Returns an object containing the String, BigDecimal or Calendar for the current row. |
| `PageContext` | `getPageContext()` | Returns the original PageContext containing the sdiinfo |
| `QueryData` | `getQueryData(String dataset)` | Internal method, not currently supported. |
| `int` | `getRowCount()` | Returns the number of rows for the primary dataset. |
| `int` | `getRowCount(String dataset)` | Returns the number of rows. |
| `String` | `getRowId(String dataset)` | Returns a unique identifier for the row. |
| `String` | `getRowStatus(String dataset)` | Returns the "update" status of the row. |
| `String` | `getSdcid()` | Returns the sdcid |
| `SDIData` | `getSDIData()` | Returns the original SDIData for the data |
| `SDIRequest` | `getSDIRequest()` | Returns the original SDIRequest for the data |
| `String` | `getString(String dataset, int row, String columnid)` | Get a string value from a String column. |
| `String` | `getString(String dataset, String columnid)` | Get a string value from a String column for the current row. |
| `String` | `getValue(String dataset, int row, String columnid)` | Returns String SDI query result for specified row |
| `String` | `getValue(String dataset, String columnid)` | Returns a String representing the value for the current row, independant of column type. |
| `boolean` | `isGrouping(String dataset)` | Internal method, not currently supported. |
| `boolean` | `isTemplateRow()` | Internal method, not currently supported. |
| `boolean` | `isTemplateRow(String dataset)` | Internal method, not currently supported. |
| `void` | `setChildTagData(String dataname, Object data)` | Internal method, not currently supported. |
| `void` | `setDataSet(String datasetName, DataSet dataset)` | — |
| `void` | `setError(String error)` | Internal method. |
| `void` | `setKeycols(String[] keycols)` | — |
| `void` | `setPageContext(PageContext pageContext)` | Sets the original PageContext containing the sdiinfo |
| `void` | `setSdcid(String sdcid)` | Sets the sdcid for the collection of data |
| `void` | `setSDIData(SDIData sdiData)` | Sets the original SDIData for the data |
| `void` | `setSDIRequest(SDIRequest sdiRequest)` | Sets the original SDIRequest for the data |

</details>

---

### Package `sapphire.talend`

#### `LabVantageUtil` (Class)
Public class for using from Talend. Contains utility methods for working with LabVantage.

*Declaration:* `public class LabVantageUtil extends java.lang.Object`

<details>
<summary>View public methods (5)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `static DataSet` | `getSQLDataSet(String query, Properties context, Map<String,Object> globalmap)` | Query the Labvantage system for data. |
| `static String` | `getVariable(String name, String defaultValue, Properties context, Map<String,Object> globalmap)` | Will return a set up variable for either a report or attachment hander. |
| `static String` | `getVariable(String name, String defaultValue, Properties context, Map<String,Object> globalmap, boolean ignoreCase)` | Will return a set up variable for either a report or attachment hander. |
| `static PropertyList` | `processAction(String actionid, String actionversion, PropertyList propertyList, Properties context, Map<String,Object> globalmap)` | Execute an action in LabVantage. |
| `static void` | `startJob(String jobName, Properties context, Map<String,Object> globalmap)` | Entry point for Jobs which use the LV API. |

</details>

---

### Package `sapphire.util`

#### `Browser` (Class)
detects what operating system and browser is being used for the provided request
can be used then to form decisions when generating client html and script

*Declaration:* `public class Browser extends java.lang.Object implements java.io.Serializable`

<details>
<summary>View public methods (53)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `String` | `getBlankSrc()` | — |
| `int` | `getBrowser()` | — |
| `boolean` | `getChromiumBased()` | — |
| `boolean` | `getCompatibilityMode()` | — |
| `String` | `getCompatibleId()` | — |
| `double` | `getCompatibleVersion()` | — |
| `double` | `getCSSVersion()` | — |
| `GUIMode` | `getGUIMode()` | — |
| `ArrayList<GUIMode>` | `getGUIModes()` | — |
| `static ArrayList<GUIMode>` | `getGUIModes(ConfigurationProcessor cp)` | — |
| `static ArrayList<GUIMode>` | `getGUIModes(SapphireConnection sapphireConnection, String databaseid)` | — |
| `double` | `getHTMLVersion()` | — |
| `String` | `getId()` | — |
| `double` | `getMozilla()` | — |
| `boolean` | `getMozillaBased()` | — |
| `String` | `getName()` | — |
| `protected String` | `getName(int b)` | — |
| `boolean` | `getNetEnabled()` | — |
| `int` | `getOS()` | — |
| `String` | `getOSId()` | — |
| `String` | `getSupportedText()` | — |
| `String` | `getUseragent()` | — |
| `double` | `getVersion()` | — |
| `ViewPort` | `getViewPort()` | — |
| `double` | `getWebkit()` | — |
| `boolean` | `getWebkitBased()` | — |
| `boolean` | `hasTouch()` | — |
| `boolean` | `isChrome()` | — |
| `boolean` | `isChromium()` | — |
| `boolean` | `isEdge()` | — |
| `boolean` | `isEmbedded()` | — |
| `boolean` | `isFireFox()` | — |
| `boolean` | `isIE()` | — |
| `boolean` | `isMobile()` | — |
| `boolean` | `isMozilla()` | — |
| `boolean` | `isOpera()` | — |
| `boolean` | `isPhone()` | — |
| `boolean` | `isSafari()` | — |
| `boolean` | `isSupported()` | — |
| `boolean` | `isTablet()` | — |
| `boolean` | `isWebkit()` | — |
| `boolean` | `requiresGarbageCollection()` | — |
| `void` | `setBrowser(Browser basedon)` | — |
| `void` | `setGUIMode(GUIMode guimode, HttpServletRequest request)` | — |
| `void` | `setGUIMode(String guimode, HttpServletRequest request)` | — |
| `void` | `setGUIModes(String databaseid, HttpSession session)` | — |
| `void` | `setMobile(boolean mobile, PageContext pageContext)` | Deprecated. |
| `void` | `setPageContext(PageContext pageContext)` | — |
| `void` | `setPageContext(PageContext pageContext, boolean useCache)` | — |
| `void` | `setRequest(HttpServletRequest request)` | — |
| `void` | `setRequest(HttpServletRequest request, boolean useCache)` | — |
| `void` | `setUseragent(String user)` | — |
| `void` | `setViewPort(String viewportString, HttpServletRequest request)` | — |

</details>

#### `Browser.GUIMode` (Class)
*Declaration:* `public class Browser.GUIMode extends java.lang.Object implements java.io.Serializable`

<details>
<summary>View public methods (20)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `boolean` | `adaptForTouch()` | — |
| `int[]` | `getBrowser()` | — |
| `String` | `getId()` | — |
| `String` | `getImageRef()` | — |
| `int` | `getMaxheight()` | — |
| `int` | `getMaxwidth()` | — |
| `boolean` | `getNavigationBar()` | — |
| `String` | `getNavigationBarMode()` | — |
| `int[]` | `getoS()` | — |
| `boolean` | `getSidebar()` | — |
| `String` | `getStartupGroupGizmo()` | — |
| `String` | `getStartupMenuGizmo()` | — |
| `String` | `getStartupUrl()` | — |
| `String` | `getTitle()` | — |
| `String` | `getUserAgentMatch()` | — |
| `boolean` | `hastouch()` | — |
| `boolean` | `isAutodetect()` | — |
| `boolean` | `isMobile()` | — |
| `boolean` | `isPhone()` | — |
| `boolean` | `isTablet()` | — |

</details>

#### `Cache` (Class)
This object allows you to store and later retrieve values from the cache.
It also allows you to selectively remove items from the cache. If the system is running in a cluster, this will also remove items from the caches on the other servers.
Note that *Adding* items into a cache does not add the items into the cache on other servers.
If a cache does not contain a value for the supplied key, the .get() method returns null.
To avoid memory leaks, a cache can have a maximum of 1000 entries. If the cache fills up beyond that it gets cleared to be filled up again.
A typical usage pattern for the cache will be
Cache cache = new Cache( connectionid );
String value = cache.get( "Addresses", "Fred" )
if ( value == null ) {
value = .... go fetch Fred's address
cache.put( "Addresses", "Fred", value );   // Lazy populate the cache
}
...
...
...
actionProcessor( "UpdateAddress", 1, {userid=Fred...} )  // Fred's address just changed
Cache cache = new Cache( connectionid );
cache.remove( "Addresses", "Fred" );                      // This will remove the cached value from all nodes in the cluster. It will be populated again lazily the next time it's needed
Warning: Objects put into the cluster should be immutable or should be copied as soon as they are retrieved.
This is to ensure that the code that retrieved does not modify (and potentialyl corrupt) the cached value.
Any changes to the cached value will NOT be synchronized across the cluster. The cache value should instead be removed from the cache and lazily loaded on other servers as described above.

*Declaration:* `public class Cache extends java.lang.Object`

<details>
<summary>View public methods (6)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `void` | `clear(String cacheName)` | Removes ALL items from the cache - and does the same across a cluster |
| `Object` | `get(String cacheName, String cacheKey)` | Gets an object out of the cluster |
| `int` | `getCacheSize(String cacheName)` | Returns the number of items in the cache. |
| `Set<String>` | `keySet(String cacheName)` | Returns a list of all keys in the cache |
| `void` | `put(String cacheName, String cacheKey, Object cacheItem)` | Stored a value in a cache with a given key |
| `void` | `remove(String cacheName, String cacheKey)` | Removes a single item from a cache and from the same cache across a cluster |

</details>

#### `CalcUtil` (Class)
Enter description here

*Declaration:* `public class CalcUtil extends java.lang.Object implements java.io.Serializable`

<details>
<summary>View public methods (2)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `BigDecimal` | `convertUnits(BigDecimal value, String fromUnit, String toUnit)` | Unit conversion of a value between two specified units |
| `String` | `evaluate(String expression, HashMap params)` | Evaluates the expression substituting the params specified |

</details>

#### `DataSet.ResultSetRowProcessor` (Interface)
<details>
<summary>View public methods (1)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `void` | `processRow(HashMap newrow, ResultSet resultSet, DataSet dataSet)` | — |

</details>

#### `DataStore` (Class)
This class wraps a dataset class and tracks the state of rows - whether they are added, updated or deleted.
This class exposes most of the methods of the wrapped DataSet and tracks the row status as the methods are called.
The status of a row can be fetched using isRowInsert and isRowUpdate methods.
A DataSet of all added, modified or deleted row can be fetched using the getInsertDataSet(), getUpdateDataSet() and getDeleteDataSet() methods.

*Declaration:* `public class DataStore extends java.lang.Object`

<details>
<summary>View public methods (64)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `boolean` | `addColumn(String columnid, int type)` | See documentation for the DataSet class for details of this method. |
| `int` | `addRow()` | Adds a new row to the end of the dataset. |
| `int` | `addRow(int newrow)` | Adds a new row as a specified position in the dataset. |
| `static String` | `convertDateToUserFormat(String dateVal, M18NUtil userM18NUtil, boolean isTimeZoneIndependent, boolean isDateOnly)` | — |
| `void` | `deleteRow(int row)` | Deletes the specified row from the wrapped dataset. |
| `int` | `findRow(HashMap findmap)` | See documentation for the DataSet class for details of this method. |
| `int` | `findRow(HashMap findmap, int start)` | See documentation for the DataSet class for details of this method. |
| `int` | `findRow(String columnId, String value)` | See documentation for the DataSet class for details of this method. |
| `BigDecimal` | `getBigDecimal(int row, String columnid)` | See documentation for the DataSet class for details of this method. |
| `BigDecimal` | `getBigDecimal(int row, String columnid, BigDecimal defaultvalue)` | See documentation for the DataSet class for details of this method. |
| `Calendar` | `getCalendar(int row, String columnid)` | See documentation for the DataSet class for details of this method. |
| `Calendar` | `getCalendar(int row, String columnid, Calendar defaultvalue)` | See documentation for the DataSet class for details of this method. |
| `int` | `getColumnCount()` | See documentation for the DataSet class for details of this method. |
| `String[]` | `getColumns()` | See documentation for the DataSet class for details of this method. |
| `int` | `getColumnType(String columnid)` | See documentation for the DataSet class for details of this method. |
| `String` | `getColumnValues(String columnid, int startrow, int endrow, String delimeter)` | See documentation for the DataSet class for details of this method. |
| `DataSet` | `getDataSet()` | Returns the wrapped dataset Note: This returned dataset should only be used for reading data passively. |
| `DateFormat` | `getDateDisplayFormat(String columnid)` | See documentation for the DataSet class for details of this method. |
| `DataSet` | `getDeleteDataSet()` | Returns a dataset comprising those rows that were deleted |
| `double` | `getDouble(int row, String columnid)` | See documentation for the DataSet class for details of this method. |
| `double` | `getDouble(int row, String columnid, double defaultvalue)` | See documentation for the DataSet class for details of this method. |
| `JsonArray` | `getEncryptedColumns()` | — |
| `DataSet` | `getFilteredDataSet(HashMap filtermap)` | See documentation for the DataSet class for details of this method. |
| `DataSet` | `getFilteredDataSet(HashMap filtermap, boolean exclusive)` | See documentation for the DataSet class for details of this method. |
| `ArrayList<DataSet>` | `getGroupedDataSets(String columnList)` | See documentation for the DataSet class for details of this method. |
| `DataSet` | `getInsertDataSet()` | Returns a dataset comprising only those rows that were inserted |
| `int` | `getInt(int row, String columnid)` | See documentation for the DataSet class for details of this method. |
| `int` | `getInt(int row, String columnid, int defaultvalue)` | See documentation for the DataSet class for details of this method. |
| `long` | `getLong(int row, String columnid)` | See documentation for the DataSet class for details of this method. |
| `M18NUtil` | `getM18n()` | See documentation for the DataSet class for details of this method. |
| `Object` | `getObject(int row, String columnid)` | See documentation for the DataSet class for details of this method. |
| `int` | `getRowCount()` | See documentation for the DataSet class for details of this method. |
| `String` | `getString(int row, String columnid)` | See documentation for the DataSet class for details of this method. |
| `String` | `getString(int row, String columnid, String defaultvalue)` | See documentation for the DataSet class for details of this method. |
| `DataSet` | `getUnlinkDataSet()` | Returns a dataset comprising those rows that were unlinked |
| `DataSet` | `getUpdateDataSet()` | Returns a dataset comprising only those rows that were updated |
| `String` | `getValue(int row, String columnid)` | See documentation for the DataSet class for details of this method. |
| `String` | `getValue(int row, String columnid, String nullvalue)` | See documentation for the DataSet class for details of this method. |
| `boolean` | `isNull(int row, String columnid)` | See documentation for the DataSet class for details of this method. |
| `boolean` | `isRowInsert(int row)` | Returns whether the specified row has been inserted |
| `boolean` | `isRowUpdate(int row)` | Returns whether the specified row has been updated |
| `void` | `setConnectionInfo(ConnectionInfo connectionInfo)` | See documentation for the DataSet class for details of this method. |
| `boolean` | `setDate(int row, String columnid, Calendar value)` | See documentation for the DataSet class for details of this method. |
| `boolean` | `setDate(int row, String columnid, long value)` | See documentation for the DataSet class for details of this method. |
| `boolean` | `setDate(int row, String columnid, String value)` | See documentation for the DataSet class for details of this method. |
| `boolean` | `setDate(int row, String columnid, Timestamp value)` | See documentation for the DataSet class for details of this method. |
| `void` | `setEncrypted(String columnid)` | — |
| `void` | `setM18NUtil(M18NUtil m18NUtil)` | See documentation for the DataSet class for details of this method. |
| `boolean` | `setNumber(int row, String columnid, BigDecimal value)` | See documentation for the DataSet class for details of this method. |
| `boolean` | `setNumber(int row, String columnid, double value)` | See documentation for the DataSet class for details of this method. |
| `boolean` | `setNumber(int row, String columnid, int value)` | See documentation for the DataSet class for details of this method. |
| `boolean` | `setNumber(int row, String columnid, long value)` | See documentation for the DataSet class for details of this method. |
| `boolean` | `setNumber(int row, String columnid, String value)` | See documentation for the DataSet class for details of this method. |
| `boolean` | `setObject(int row, String columnid, Object o)` | See documentation for the DataSet class for details of this method. |
| `void` | `setRowInsert(int row)` | Can be used to flag that a row has been Inserted |
| `void` | `setRowUpdate(int row)` | Can be used to flag that a row has been Updated |
| `boolean` | `setSequence(String columnid)` | See documentation for the DataSet class for details of this method. |
| `boolean` | `setString(int row, String columnid, String value)` | See documentation for the DataSet class for details of this method. |
| `void` | `setTimeZoneInsensitive(String columnid)` | See documentation for the DataSet class for details of this method. |
| `boolean` | `setValue(int row, String columnid, String value)` | See documentation for the DataSet class for details of this method. |
| `int` | `size()` | See documentation for the DataSet class for details of this method. |
| `void` | `sort(String sortstring)` | See documentation for the DataSet class for details of this method. |
| `JsonObject` | `toJsonObject()` | Serialized the DataStore object |
| `void` | `unlinkRow(int row)` | Unlinks the specified row from the wrapped dataset. |

</details>

#### `FormatUtil` (Class)
Formats a BigDecimal into a local-sensitive String

*Declaration:* `public class FormatUtil extends java.lang.Object implements java.io.Serializable`

<details>
<summary>View public methods (11)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `String` | `format(BigDecimal bigDecimal)` | Formats a BigDecimal into a local-sensitive String |
| `String` | `format(BigDecimal bigDecimal, boolean group)` | Formats a BigDecimal into a local-sensitive String |
| `String` | `format(BigDecimal bigDecimal, boolean group, boolean stripTrailingZeros)` | Formats a BigDecimal into a local-sensitive String |
| `char` | `getDecimalSeparator()` | Returns the decimal separator for the locale |
| `int` | `getGroupingInterval()` | Returns the grouping interval for the locale |
| `char` | `getGroupingSeparator()` | Returns the grouping separator for the locale |
| `static FormatUtil` | `getInstance()` | Gets a new instance of FormatUtil using the default locale |
| `static FormatUtil` | `getInstance(ConnectionInfo connectionInfo)` | Gets a new instance of FormatUtil using the connectionInfo specified |
| `static FormatUtil` | `getInstance(Locale locale)` | Gets a new instance of FormatUtil using the locale specified |
| `BigDecimal` | `parseBigDecimal(String value)` | Parses a locale-sensitive String into a BigDecimal |
| `BigDecimal` | `parseBigDecimal(String value, char decimalSeparator, char groupingSeparator, boolean allowGroupingSeparator, boolean validateGroupingPos)` | Parses a locale-sensitive String into a BigDecimal |

</details>

#### `ForwardUtil` (Class)
Used inside Servlets or JSP Pages to    create an HTML form. The form can contain hidden fields and values (properties)
that can be submitted to generate a new HTTP request.

*Declaration:* `public class ForwardUtil extends com.labvantage.sapphire.BaseClass`

<details>
<summary>View public methods (5)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `String` | `getForm(String formid, String action, String method, boolean autosubmit)` | Returns a String containing the text to generate the HTML form. |
| `String` | `getForm(String formid, String action, String method, boolean autosubmit, boolean parseURLToForm)` | Returns a String containing the text to generate the HTML form. |
| `String` | `getURLParams()` | Returns a query string (e.g. |
| `void` | `setProperties(HashMap props)` | Add a collection of property/value pairs. |
| `void` | `setProperty(String propertyid, String value)` | Add a property/value pair. |

</details>

#### `HttpUtil` (Class)
Utility class providing methods to manipulate cookies and redirecting

*Declaration:* `public class HttpUtil extends com.labvantage.sapphire.BaseClass`

<details>
<summary>View public methods (36)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `static String` | `appendRequestParameters(PageContext pageContext, String url)` | — |
| `static String` | `decode(String str)` | Decodes a String that is encoded using HttpUtil.encode method |
| `static String` | `decodeURIComponent(String component)` | Performs decoding on a URL component (individual parameter values) that has been encoded client-side using JavaScript encodeURIComponent. |
| `static String` | `decodeURIComponent(String component, String encoding)` | Performs decoding on a URL component (individual parameter values) that has been encoded client-side using JavaScript encodeURIComponent |
| `static String` | `decrypt(String text)` | — |
| `static String` | `encode(String str)` | Encodes a String value for BO authentication |
| `static String` | `encodeURIComponent(String component)` | Performs encoding on a URL component (individual parameter values) that can safely be decoded on the client-side using JavaScript decodeURIComponent. |
| `static String` | `encodeURIComponent(String component, String encoding)` | Performs encoding on a URL component (individual parameter values) that can safely be decoded on the client-side using JavaScript decodeURIComponent |
| `boolean` | `existCookie(String name)` | Checks for the existence of a cookie (requires constructor option1 or option2) |
| `static String` | `getApplicationHome()` | Gets the APPLICATION_HOME folder |
| `static String` | `getAppRoot(ServletContext servletContext)` | Gets the root folder for the application server |
| `static String` | `getConnectionId(PageContext pageContext)` | Gets the current sapphire connectionid from the page Context |
| `static ConnectionInfo` | `getConnectionInfo(PageContext pageContext)` | Gets the current sapphire ConnectionInfo from the page Context |
| `HashMap` | `getCookies()` | Get cookies |
| `String` | `getCookieValue(String name)` | Gets a cookie value (requires constructor option1 or option2 or option3) |
| `static String` | `getEncryptionJS()` | Gets the core sapphire javascript encryption API |
| `static String` | `getEncryptionJS(boolean useFullIncludes)` | — |
| `static String` | `getEncryptionJS(String pathpreifx)` | — |
| `static String` | `getEncryptionJS(String pathpreifx, boolean useFullIncludes)` | — |
| `static String` | `getGWTEncryptionJS(File consoleConfigFile)` | — |
| `static String` | `getNameServerList(PageContext pageContext)` | Deprecated. |
| `static HashMap<String,String>` | `getQueryStringMap(URL url)` | Converts a query string in a URL to a hashmap of parameter and value |
| `static HashMap` | `getRequestMap(ServletRequest request)` | Puts the request parameters into a convenient hashmap |
| `static PropertyList` | `getRequestPropertyList(ServletRequest request)` | Puts the request parameters into a convenient propertylist |
| `static String` | `getSapphireHome()` | Gets the SAPPHIRE_HOME folder |
| `static Locale` | `getSessionLocale(PageContext pageContext)` | Returns the current user's locale |
| `static TimeZone` | `getSessionTimeZone(PageContext pageContext)` | Returns the current user's timezone |
| `static String` | `getWebAppRoot(ServletContext servletContext)` | Gets the root folder for the web application |
| `void` | `goErrorPage(String errorpage, String errormsg)` | Redirects to an error page if the response has not been commited, otherwise displays the error message (requires constructor option1) |
| `void` | `removeCookie(String name)` | Removes a cookie, i.e. |
| `void` | `setCookieHeader(String name, String value, boolean httponly)` | — |
| `void` | `setCookieHeader(String name, String value, boolean permanent, boolean httponly)` | — |
| `void` | `setCookieValue(String name, String value)` | Sets a cookie value (requires constructor option1 or option2 or option3) |
| `void` | `setCookieValue(String name, String value, boolean permenant)` | Sets a cookie value (requires constructor option1 or option2 or option3) |
| `void` | `setCookieValue(String name, String value, boolean permenant, boolean httponly)` | Sets a cookie value (requires constructor option1 or option2 or option3) |
| `static void` | `setRequestVariables(HttpServletRequest request, String varName, PropertyList propertyList)` | — |

</details>

#### `JsonArray` (Class)
*Declaration:* `public class JsonArray extends java.lang.Object implements java.lang.Iterable, java.io.Serializable`

<details>
<summary>View public methods (16)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `boolean` | `contains(Object c)` | — |
| `JsonObject` | `findJsonObject(String key, Object value)` | — |
| `JSONArray` | `getJSONArray()` | — |
| `JsonObject` | `getJsonObject(int index)` | — |
| `String` | `getString(int index)` | — |
| `Iterator` | `iterator()` | — |
| `int` | `length()` | — |
| `void` | `put(JsonObject jso)` | — |
| `void` | `put(String value)` | — |
| `int` | `size()` | — |
| `void` | `sort(String propertyid)` | — |
| `ArrayList<Serializable>` | `toArray()` | — |
| `<T> T[]` | `toArray(T[] a)` | — |
| `JsonObject[]` | `toJsonObjectArray()` | — |
| `String` | `toString()` | — |
| `String[]` | `toStringArray()` | — |

</details>

#### `JsonObject` (Class)
*Declaration:* `public class JsonObject extends java.lang.Object implements java.io.Serializable`

<details>
<summary>View public methods (31)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `Serializable` | `get(String key)` | — |
| `boolean` | `getBoolean(String id)` | — |
| `boolean` | `getBoolean(String id, boolean defaultValue)` | — |
| `double` | `getDouble(String id, double defaultValue)` | — |
| `int` | `getInt(String id, int defaultValue)` | — |
| `JsonArray` | `getJsonArray(String id)` | — |
| `JsonObject` | `getJsonObject(String id)` | — |
| `long` | `getLong(String id, long defaultValue)` | — |
| `String` | `getString(String id)` | — |
| `String` | `getString(String id, String defaultValue)` | — |
| `boolean` | `has(String key)` | — |
| `boolean` | `isEmpty()` | — |
| `Set<String>` | `keys()` | — |
| `int` | `length()` | — |
| `JsonObject` | `put(String id, boolean value)` | — |
| `JsonObject` | `put(String id, double value)` | — |
| `JsonObject` | `put(String id, int value)` | — |
| `JsonObject` | `put(String id, JsonArray array)` | — |
| `void` | `put(String id, JsonObject jso)` | — |
| `JsonObject` | `put(String id, String value)` | — |
| `JsonObject` | `putAnyType(String id, Serializable value)` | — |
| `JsonObject` | `putRes(String id, boolean value)` | — |
| `JsonObject` | `putRes(String id, double value)` | — |
| `JsonObject` | `putRes(String id, int value)` | — |
| `JsonObject` | `putRes(String id, JsonArray value)` | — |
| `JsonObject` | `putRes(String id, JsonObject value)` | — |
| `JsonObject` | `putRes(String id, String value)` | — |
| `void` | `remove(String id)` | — |
| `JsonArray` | `toJsonArray(String keyid, String valueid)` | — |
| `protected JSONObject` | `toJSONObject()` | — |
| `String` | `toString()` | — |

</details>

#### `JstlUtil` (Class)
*Declaration:* `public class JstlUtil extends com.labvantage.sapphire.BaseClass`

<details>
<summary>View public methods (2)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `static Object` | `evaluateExpression(String expression, PageContext pageContext)` | — |
| `static Object` | `evaluateExpression(String expression, PageContext pageContext, Object nullValue)` | — |

</details>

#### `ListMaintUtil` (Class)
Enter description here

*Declaration:* `public class ListMaintUtil extends java.lang.Object`

<details>
<summary>View public methods (13)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `String` | `getContentcellheight()` | — |
| `String` | `getContentcellwidth()` | — |
| `String` | `getInitiallHeight()` | — |
| `String` | `getInitiallWidth()` | — |
| `String` | `getListWidthRightInline(boolean showNotes, boolean showNoteInitially)` | — |
| `String` | `getPosition()` | — |
| `boolean` | `isEmbeddedMaint()` | — |
| `boolean` | `isPositionBottm()` | — |
| `boolean` | `isPositionRight()` | — |
| `boolean` | `isPositionRightInline()` | — |
| `void` | `modifyMaintToolbarButtons()` | — |
| `void` | `removeLayout()` | — |
| `void` | `updatePagedata()` | — |

</details>

#### `LogContext` (Class)
LogContext contains contextual information used for logging

*Declaration:* `public class LogContext extends java.lang.Object`

<details>
<summary>View public methods (5)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `String` | `getConnectionId()` | — |
| `String` | `getLoggerName()` | — |
| `String` | `getSysuserid()` | — |
| `void` | `setConnectionId(String connectionid)` | — |
| `void` | `setLoggerName(String loggerName)` | — |

</details>

#### `Logger` (Class)
Logger API class
- automatically instanciated in BaseXXX classes or can be manually created

*Declaration:* `public class Logger extends java.lang.Object`

<details>
<summary>View public methods (27)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `void` | `debug(Object message)` | Logs a debug message |
| `void` | `debug(String loggerName, Object message)` | Logs a debug message to a specific logger |
| `void` | `error(String message)` | Logs an error message |
| `void` | `error(String loggerName, String message)` | Logs an error message to a specific logger |
| `void` | `error(String loggerName, String message, Throwable t)` | Logs an error message to a specific logger plus the associated exception |
| `void` | `error(String message, Throwable t)` | Logs an error message plus the associated exception |
| `void` | `info(String message)` | Logs an information message |
| `void` | `info(String loggerName, String message)` | Logs an information message to a specific logger |
| `static boolean` | `isDebugEnabled()` | Determines if debug messages are being logged |
| `static void` | `logDebug(Object message)` | Logs a debug message |
| `static void` | `logDebug(String loggerName, Object message)` | Logs a debug message to a specific logger |
| `static void` | `logError(String message)` | Logs an error message |
| `static void` | `logError(String loggerName, Object message)` | Logs an error message to a specific logger |
| `static void` | `logError(String loggerName, Object message, Throwable t)` | Logs an error message to a specific logger plus the associated exception |
| `static void` | `logError(String message, Throwable t)` | Logs an error message plus the associated exception |
| `protected boolean` | `logging()` | — |
| `static void` | `logInfo(String message)` | Logs an information message |
| `static void` | `logInfo(String loggerName, Object message)` | Logs an information message to a specific logger |
| `static void` | `logStackTrace(Throwable t)` | Prints a stack trace to the log |
| `static void` | `logWarn(String message)` | Logs a warning message |
| `static void` | `logWarn(String loggerName, Object message)` | Logs a warning message to a specific logger |
| `void` | `noLog(boolean nolog)` | — |
| `void` | `setLogContextConnectionId(String connectionid)` | Set the logContext connectionid |
| `void` | `setLoggerName(String loggerName)` | Set the logger name (overrides default) |
| `void` | `stackTrace(Throwable t)` | Prints a stack trace to the log |
| `void` | `warn(String message)` | Logs a warning message |
| `void` | `warn(String loggerName, String message)` | Logs a warning message to a specific logger |

</details>

#### `M18NUtil` (Class)
This utility class is designed to provide sapphire M18N date and number parsing and formatting functionalities.

*Declaration:* `public class M18NUtil extends java.lang.Object implements java.io.Serializable`

<details>
<summary>View public methods (26)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `String` | `format(BigDecimal bigDecimal)` | Formats a BigDecimal into a local-sensitive String |
| `String` | `format(BigDecimal bigDecimal, boolean group)` | Formats a BigDecimal into a local-sensitive String |
| `String` | `format(BigDecimal bigDecimal, boolean group, boolean stripTrailingZeros)` | Formats a BigDecimal into a local-sensitive String |
| `String` | `format(Calendar calendar)` | Formats a Calender into date time string according to user locale and timezone |
| `String` | `format(Calendar calendar, boolean userTimeZoneSensitive)` | Formats a Calender into date time string according to user locale and with or without user timezone adjustment |
| `String` | `formatDateOnly(Calendar calendar)` | Formats a Calender into date only string according to user locale and timezone |
| `String` | `formatDateOnly(Calendar calendar, boolean userTimeZoneSensitive)` | Formats a Calender into date only string according to user locale and with or without user timezone adjustment |
| `DateTimeFormatter` | `getDateFormatter(ZoneId displayZone)` | — |
| `DateTimeFormatter` | `getDateTimeFormatter(ZoneId displayZone)` | — |
| `DateFormat` | `getDefaultDateFormat()` | — |
| `DateFormat` | `getDefaultDateFormat(boolean userTimeZoneSensitive)` | — |
| `DateFormat` | `getDefaultDateOnlyFormat()` | — |
| `DateFormat` | `getDefaultDateOnlyFormat(boolean userTimeZoneSensitive)` | — |
| `Locale` | `getLocale()` | — |
| `Calendar` | `getNowCalendar()` | — |
| `DateFormat` | `getSysQueryDateFormat()` | — |
| `DateTimeFormatter` | `getTimeFormatter(ZoneId displayZone)` | — |
| `TimeZone` | `getTimezone()` | Return the time zone used to format and parse date time string by this object in user's time zone |
| `boolean` | `isRelDate(String date)` | — |
| `boolean` | `isRelDate(String date, boolean dateonly)` | — |
| `BigDecimal` | `parseBigDecimal(String value)` | Parses a locale-sensitive String into a BigDecimal |
| `Calendar` | `parseCalendar(String datetime)` | Parse a date time string into a Calender object assuming the user'local and timezone |
| `Calendar` | `parseCalendar(String datetime, boolean userTimeZoneSensitive)` | Parse a date time string into a Calender object assuming the user'local and with or without user timezone adjustment String |
| `Timestamp` | `parseTimestamp(String datetime)` | — |
| `Timestamp` | `parseTimestamp(String datetime, boolean userTimeZoneSensitive)` | — |
| `void` | `setTimeZone(TimeZone timeZone)` | Set the user Time Zone. |

</details>

#### `ResultDataGrid.CoreColumns` (Enum)
Returns the enum constant of this type with the specified name.

<details>
<summary>View public methods (4)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `String` | `getColumnId()` | — |
| `int` | `getType()` | — |
| `static CoreColumns` | `valueOf(String name)` | Returns the enum constant of this type with the specified name. |
| `static CoreColumns[]` | `values()` | Returns an array containing the constants of this enum type, in the order they are declared. |

</details>

#### `ResultGridOptions` (Class)
Created by gstimson on 08/04/2019.

*Declaration:* `public class ResultGridOptions extends java.lang.Object implements com.labvantage.sapphire.gwt.shared.JSONable`

<details>
<summary>View public methods (45)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `PropertyList` | `getAddDataItemProperties()` | — |
| `PropertyList` | `getAddDatasetProperties()` | — |
| `PropertyList` | `getAddSDIProperties()` | — |
| `boolean` | `getApplyLock()` | — |
| `String` | `getAuditActivity()` | — |
| `String` | `getAuditDt()` | — |
| `String` | `getAuditReason()` | — |
| `String` | `getAuditSignedFlag()` | — |
| `AutoAddDataSet` | `getAutoAddDataset()` | — |
| `AutoAddParameter` | `getAutoAddParameter()` | — |
| `AutoAddReplicate` | `getAutoAddReplicate()` | — |
| `AutoAddSDI` | `getAutoAddSDI()` | — |
| `AutoAddWorkItem` | `getAutoAddWorkItem()` | — |
| `boolean` | `getAutoRelease()` | — |
| `List` | `getCoreColsList()` | — |
| `DefaultDataSet` | `getDefaultDataSet()` | — |
| `DefaultReplicateId` | `getDefaultReplicateId()` | — |
| `PropertyList` | `getEnterDataItemProperties()` | — |
| `MissingDataErrorHandling` | `getMissingDataErrorHandling()` | — |
| `ReleaseHandlingRule` | `getReleaseHandlingRule()` | — |
| `String` | `getSdcId()` | — |
| `String` | `getTraceLogId()` | — |
| `void` | `setAddDataItemProperties(PropertyList properties)` | — |
| `void` | `setAddDatasetProperties(PropertyList properties)` | — |
| `void` | `setAddSDIProperties(PropertyList properties)` | — |
| `void` | `setApplyLock(boolean lock)` | — |
| `void` | `setAuditActivity(String activity)` | — |
| `void` | `setAuditDt(String auditdt)` | — |
| `void` | `setAuditReason(String reason)` | — |
| `void` | `setAuditSignedFlag(String signedFlag)` | — |
| `void` | `setAutoAddDataset(AutoAddDataSet autoAddDataset)` | — |
| `void` | `setAutoAddParameter(AutoAddParameter autoAddParameter)` | — |
| `void` | `setAutoAddReplicate(AutoAddReplicate autoAddReplicate)` | — |
| `void` | `setAutoAddSDI(AutoAddSDI autoAddSDI)` | — |
| `void` | `setAutoAddWorkItem(AutoAddWorkItem autoAddWorkItem)` | — |
| `void` | `setAutoRelease(boolean autoRelease)` | — |
| `void` | `setDefaultDataSet(DefaultDataSet dataset)` | — |
| `void` | `setDefaultReplicateId(DefaultReplicateId replicateId)` | — |
| `void` | `setEnterDataItemProperties(PropertyList properties)` | — |
| `void` | `setMissingDataErrorHandling(MissingDataErrorHandling missingDataErrorHandling)` | — |
| `void` | `setReleaseHandlingRule(ReleaseHandlingRule releaseHandlingRule)` | — |
| `void` | `setSdcId(String sdcid)` | — |
| `void` | `setTraceLogId(String tracelogId)` | — |
| `String` | `toJSONString()` | — |
| `PropertyList` | `toPropertyList()` | — |

</details>

#### `ResultGridOptions.AutoAddDataSet` (Enum)
Returns the enum constant of this type with the specified name.

<details>
<summary>View public methods (2)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `static AutoAddDataSet` | `valueOf(String name)` | Returns the enum constant of this type with the specified name. |
| `static AutoAddDataSet[]` | `values()` | Returns an array containing the constants of this enum type, in the order they are declared. |

</details>

#### `ResultGridOptions.AutoAddParameter` (Enum)
Returns the enum constant of this type with the specified name.

<details>
<summary>View public methods (2)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `static AutoAddParameter` | `valueOf(String name)` | Returns the enum constant of this type with the specified name. |
| `static AutoAddParameter[]` | `values()` | Returns an array containing the constants of this enum type, in the order they are declared. |

</details>

#### `ResultGridOptions.AutoAddReplicate` (Enum)
Returns the enum constant of this type with the specified name.

<details>
<summary>View public methods (2)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `static AutoAddReplicate` | `valueOf(String name)` | Returns the enum constant of this type with the specified name. |
| `static AutoAddReplicate[]` | `values()` | Returns an array containing the constants of this enum type, in the order they are declared. |

</details>

#### `ResultGridOptions.AutoAddSDI` (Enum)
Returns the enum constant of this type with the specified name.

<details>
<summary>View public methods (2)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `static AutoAddSDI` | `valueOf(String name)` | Returns the enum constant of this type with the specified name. |
| `static AutoAddSDI[]` | `values()` | Returns an array containing the constants of this enum type, in the order they are declared. |

</details>

#### `ResultGridOptions.AutoAddWorkItem` (Enum)
Returns the enum constant of this type with the specified name.

<details>
<summary>View public methods (2)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `static AutoAddWorkItem` | `valueOf(String name)` | Returns the enum constant of this type with the specified name. |
| `static AutoAddWorkItem[]` | `values()` | Returns an array containing the constants of this enum type, in the order they are declared. |

</details>

#### `ResultGridOptions.DefaultDataSet` (Enum)
Returns the enum constant of this type with the specified name.

<details>
<summary>View public methods (4)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `String` | `toString()` | — |
| `static DefaultDataSet` | `valueOf(String name)` | Returns the enum constant of this type with the specified name. |
| `static DefaultDataSet` | `valueOfDefaultDataSet(String datasetDesc)` | — |
| `static DefaultDataSet[]` | `values()` | Returns an array containing the constants of this enum type, in the order they are declared. |

</details>

#### `ResultGridOptions.DefaultReplicateId` (Enum)
Returns the enum constant of this type with the specified name.

<details>
<summary>View public methods (4)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `String` | `toString()` | — |
| `static DefaultReplicateId` | `valueOf(String name)` | Returns the enum constant of this type with the specified name. |
| `static DefaultReplicateId` | `valueOfDefaultReplicateId(String desc)` | — |
| `static DefaultReplicateId[]` | `values()` | Returns an array containing the constants of this enum type, in the order they are declared. |

</details>

#### `ResultGridOptions.MissingDataErrorHandling` (Enum)
Returns the enum constant of this type with the specified name.

<details>
<summary>View public methods (2)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `static MissingDataErrorHandling` | `valueOf(String name)` | Returns the enum constant of this type with the specified name. |
| `static MissingDataErrorHandling[]` | `values()` | Returns an array containing the constants of this enum type, in the order they are declared. |

</details>

#### `ResultGridOptions.ReleaseHandlingRule` (Enum)
Returns the enum constant of this type with the specified name.

<details>
<summary>View public methods (2)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `static ReleaseHandlingRule` | `valueOf(String name)` | Returns the enum constant of this type with the specified name. |
| `static ReleaseHandlingRule[]` | `values()` | Returns an array containing the constants of this enum type, in the order they are declared. |

</details>

#### `SDIList.KeyId` (Enum)
Returns the enum constant of this type with the specified name.

<details>
<summary>View public methods (2)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `static KeyId` | `valueOf(String name)` | Returns the enum constant of this type with the specified name. |
| `static KeyId[]` | `values()` | Returns an array containing the constants of this enum type, in the order they are declared. |

</details>

#### `SDIView` (Class)
Created by IntelliJ IDEA.
User: gstimson
Date: 26/09/14
Time: 10:52
To change this template use File | Settings | File Templates.

*Declaration:* `public class SDIView extends com.labvantage.sapphire.BaseCustom`

<details>
<summary>View public methods (14)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `void` | `addField(String columId, String label, String value, FieldPriority priority)` | — |
| `void` | `fromSDIData(SDIData sdiData, int row, ArrayList<SDIViewResponsiveField> fields)` | — |
| `String` | `getEditURL()` | — |
| `ArrayList<SDIViewResponsiveField>` | `getFields()` | — |
| `ImageRef` | `getImageRef()` | — |
| `String` | `getLabel()` | — |
| `SDI` | `getSdi()` | — |
| `String` | `getViewURL()` | — |
| `void` | `setEditURL(String editURL)` | — |
| `void` | `setFields(ArrayList<SDIViewResponsiveField> fields)` | — |
| `void` | `setImageRef(ImageRef imageRef)` | — |
| `void` | `setLabel(String label)` | — |
| `void` | `setSdi(SDI sdi)` | — |
| `void` | `setViewURL(String viewURL)` | — |

</details>

#### `SDIView.FieldPriority` (Enum)
Returns the enum constant of this type with the specified name.

<details>
<summary>View public methods (2)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `static FieldPriority` | `valueOf(String name)` | Returns the enum constant of this type with the specified name. |
| `static FieldPriority[]` | `values()` | Returns an array containing the constants of this enum type, in the order they are declared. |

</details>

#### `SDIView.SDIViewResponsiveField` (Class)
*Declaration:* `public class SDIView.SDIViewResponsiveField extends java.lang.Object`

<details>
<summary>View public methods (9)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `int` | `compareTo(SDIViewResponsiveField o)` | — |
| `String` | `getColumnid()` | — |
| `String` | `getLabel()` | — |
| `FieldPriority` | `getPriority()` | — |
| `String` | `getValue()` | — |
| `void` | `setColumnid(String columnid)` | — |
| `void` | `setLabel(String label)` | — |
| `void` | `setPriority(FieldPriority priority)` | — |
| `void` | `setValue(String value)` | — |

</details>

#### `SafeHTML` (Class)
Class used to encode un-trusted content to be inserted into HTML or javascript to prevent XSS attacks

*Declaration:* `public class SafeHTML extends java.lang.Object`

<details>
<summary>View public methods (7)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `static String` | `encodeForCSS(String input)` | — |
| `static String` | `encodeForHTML(String input)` | — |
| `static String` | `encodeForHTML(String input, boolean allowBasicFormatTags)` | — |
| `static String` | `encodeForHTMLAttribute(String input)` | — |
| `static String` | `encodeForJavaScript(String input)` | — |
| `static String` | `encodeForURL(String input)` | — |
| `static String[]` | `getAllowedHtmlTags()` | — |

</details>

#### `StringUtil` (Class)
Provides a collection of String utility classes

*Declaration:* `public class StringUtil extends com.labvantage.sapphire.gwt.shared.util.StringUtil`

<details>
<summary>View public methods (24)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `static String` | `arrayToString(String[] array, String delimiter)` | converts an array to a string with delimiter |
| `static String` | `escape(String in)` | Escapes a String as per the JavaScript escaping mechanism by converting (most) non-alphanumerics to %nn |
| `static String` | `escapeXMLAttributeValue(String attributeValue)` | — |
| `static String[]` | `getExpressionTokens(String input)` | Returns a list of tokens (delimetered by [token]) in a string for a groovy or regular calculation expression |
| `static long` | `getLen(String str)` | Returns the length of a string checking for nulls as well as standard length |
| `static String` | `getRandomString(int length)` | Fetches you a random string |
| `static String` | `getRGBHex(int value)` | Returns an RGB numeric value in Hex format |
| `static String[]` | `getTokens(String input)` | Returns a list of tokens (delimetered by [token]) in a string |
| `static String[]` | `getTokens(String input, String starttoken, String endtoken)` | Returns a list of tokens (delimetered by [token]) in a string |
| `static String[]` | `getTokens(String input, String starttoken, String endtoken, boolean ignoreCDATA)` | Returns a list of tokens (delimetered by [token]) in a string |
| `static String[]` | `getTokens(String input, String starttoken, String endtoken, boolean ignoreCDATA, boolean keepDuplicate)` | Returns a list of tokens (delimetered by [token]) in a string |
| `static String` | `getYN(String input, String defaultresponse)` | Converts Yes/No, Y/N, yes/no, true/false, TRUE/FALSE etc. |
| `static String` | `initCaps(String in)` | Converts the intial letter to caps in a string |
| `static String` | `padLeft(String input, int desiredlength)` | Pads the left side of a String with spaces to the desired length |
| `static String` | `padLeft(String input, int desiredlength, char padchar)` | Pads the left side of a String with a specified character to the desired length |
| `static String` | `padRight(String input, int desiredlength)` | Pads the right side of a String with a specified character to the desired length |
| `static String` | `padRight(String input, int desiredlength, char padchar)` | Pads the right side of a String with a specified character to the desired length |
| `static String` | `repeat(String basestring, int repeat)` | Returns a String that represents a given string repeated 'n' times |
| `static String` | `repeat(String basestring, int repeat, String separator)` | Returns a String that represents a given string repeated 'n' times delimited by the separator |
| `static String` | `replaceAll(String inputString, String oldString, String newString)` | Replaces all instances of a string in a string |
| `static String` | `replaceAll(String inputString, String oldString, String newString, boolean caseSensitive)` | Replaces all instances of a string in a string |
| `static String[]` | `split(String input, String delimeter)` | Splits a string into an array of substrings |
| `static String[]` | `split(String input, String delimeter, boolean trim)` | Splits a string into an array of substrings |
| `static String` | `unescape(String in)` | Unescapes a String as per the JavaScript unescaping mechanism by converting %nn text to their ascii equivalent |

</details>

#### `TaskContext` (Class)
Task Context - holds details about the current task

*Declaration:* `public class TaskContext extends java.lang.Object`

<details>
<summary>View public methods (39)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `String` | `getHostFrameId()` | — |
| `int` | `getStepCount()` | — |
| `String` | `getStepid()` | — |
| `PropertyList` | `getStepProps()` | — |
| `String` | `getTaskdefid()` | — |
| `String` | `getTaskdefitemid()` | — |
| `String` | `getTaskdefvariantid()` | — |
| `String` | `getTaskdefversionid()` | — |
| `String` | `getTaskexecgroup()` | — |
| `String` | `getTaskexecid()` | — |
| `String` | `getWorkflowdefid()` | — |
| `String` | `getWorkflowdefvariantid()` | — |
| `String` | `getWorkflowdefversionid()` | — |
| `String` | `getWorkflowexecid()` | — |
| `String` | `getWorkflowexecname()` | — |
| `boolean` | `isBack()` | — |
| `boolean` | `isStandaloneMode()` | — |
| `boolean` | `isTaskPage()` | — |
| `boolean` | `isTestMode()` | — |
| `void` | `setBack(boolean back)` | — |
| `void` | `setHostFrameId(String hostFrameId)` | — |
| `void` | `setJSONObject(JSONObject jsonObject)` | — |
| `void` | `setStandaloneMode(boolean standaloneMode)` | — |
| `void` | `setStepCount(int stepCount)` | — |
| `void` | `setStepid(String stepid)` | — |
| `void` | `setStepProps(PropertyList stepProps)` | — |
| `void` | `setTaskdefid(String taskdefid)` | — |
| `void` | `setTaskdefitemid(String taskdefitemid)` | — |
| `void` | `setTaskdefvariantid(String taskdefvariantid)` | — |
| `void` | `setTaskdefversionid(String taskdefversionid)` | — |
| `void` | `setTaskexecgroup(String taskexecgroup)` | — |
| `void` | `setTaskexecid(String taskexecid)` | — |
| `void` | `setTestMode(boolean testMode)` | — |
| `void` | `setWorkflowdefid(String workflowdefid)` | — |
| `void` | `setWorkflowdefvariantid(String workflowdefvariantid)` | — |
| `void` | `setWorkflowdefversionid(String workflowdefversionid)` | — |
| `void` | `setWorkflowexecid(String workflowexecid)` | — |
| `void` | `setWorkflowexecname(String workflowexecname)` | — |
| `String` | `toJSONString()` | — |

</details>

---

### Package `sapphire.xml`

#### `DOMUtil` (Class)
*Declaration:* `public class DOMUtil extends com.labvantage.sapphire.BaseClass`

<details>
<summary>View public methods (16)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `static String` | `convertChars(String text)` | — |
| `static Element` | `findNode(Node ptreenode, String nodeid)` | — |
| `static ArrayList` | `getAllNodes(Node node)` | — |
| `static HashMap` | `getAttributes(Element element)` | — |
| `static Node` | `getChildElement(Node parentNode, String elementName)` | — |
| `static List` | `getChildElements(Node parentNode, String elementName)` | — |
| `static Element` | `getElementByAttribute(String attributeId, String attributeValue, List<Element> elements)` | — |
| `static Document` | `getNewDocument()` | — |
| `static Document` | `getNewDocument(Object xml)` | — |
| `static Document` | `getNewDocument(Object xml, boolean cacheDocument)` | — |
| `static Document` | `getNewDocument(Object xml, boolean cacheDocument, String noValidatePublicId)` | — |
| `static void` | `save(Document dom, File file)` | — |
| `static void` | `saveRawXml(Document dom, File file)` | — |
| `static String` | `toString(Node node)` | — |
| `static String` | `toString(Node node, boolean addHeader, boolean useCDATA)` | — |
| `static String` | `toString(Node node, String header, boolean useCDATA)` | — |

</details>

#### `PropertyValue` (Class)
*Declaration:* `public class PropertyValue extends java.lang.Object implements java.io.Serializable`

<details>
<summary>View public methods (9)</summary>

| Return Type | Method Signature | Description |
|---|---|---|
| `String` | `getAttribute(String attributeId)` | — |
| `HashMap` | `getAttributes()` | — |
| `String` | `getId()` | — |
| `PropertyList` | `getParentPropertyList()` | — |
| `String` | `getPropertyTreeNodeId()` | — |
| `boolean` | `isDefault()` | — |
| `void` | `setAttributes(HashMap attributes)` | — |
| `void` | `setPropertyTreeNodeId(String propertyTreeNodeId)` | — |
| `String` | `toString()` | — |

</details>

---
