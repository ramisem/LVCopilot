/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.gwt.shared.error;

import java.io.Serializable;

public class ErrorDetail
implements Serializable {
    public static final String TYPE_FAILURE = "FAILURE";
    public static final String TYPE_CONFIRM = "CONFIRM";
    public static final String TYPE_VALIDATION = "VALIDATION";
    public static final String TYPE_INFORMATION = "INFORMATION";
    public static final String EVENT_PREADD = "PreAdd";
    public static final String EVENT_POSTADDKEY = "PostAddKey";
    public static final String EVENT_PREADDKEY = "PreAddKey";
    public static final String EVENT_POSTADD = "PostAdd";
    public static final String EVENT_PREEDIT = "PreEdit";
    public static final String EVENT_POSTEDIT = "PostEdit";
    public static final String EVENT_PREDELETE = "PreDelete";
    public static final String EVENT_POSTDELETE = "PostDelete";
    public static final String EVENT_PREADDDETAIL = "PreAddDetail";
    public static final String EVENT_POSTADDDETAIL = "PostAddDetail";
    public static final String EVENT_PREEDITDETAIL = "PreEditDetail";
    public static final String EVENT_POSTEDITDETAIL = "PostEditDetail";
    public static final String EVENT_PREDELETEDETAIL = "PreDeleteDetail";
    public static final String EVENT_POSTDELETEDETAIL = "PostDeleteDetail";
    public static final String EVENT_POSTDATAENTRY = "PostDataEntry";
    public static final String EVENT_PREEDITDATASET = "PreEditDataSet";
    public static final String EVENT_POSTEDITDATASET = "PostEditDataSet";
    public static final String EVENT_PREEDITDATAITEM = "PreEditDataItem";
    public static final String EVENT_POSTEDITDATAITEM = "PostEditDataItem";
    public static final String EVENT_PREEDITDATAAPPROVAL = "PreEditDataApproval";
    public static final String EVENT_POSTEDITDATAAPPROVAL = "PostEditDataApproval";
    public static final String EVENT_PREDATARELEASE = "PreDataRelease";
    public static final String EVENT_POSTDATARELEASE = "PostDataRelease";
    public static final String EVENT_PREADDDATASET = "PreAddDataSet";
    public static final String EVENT_POSTADDDATASET = "PostAddDataSet";
    public static final String EVENT_PREADDWORKITEM = "PreAddWorkItem";
    public static final String EVENT_POSTADDWORKITEM = "PostAddWorkItem";
    public static final String EVENT_PREEDITWORKITEM = "PreEditWorkItem";
    public static final String EVENT_POSTEDITWORKITEM = "PostEditWorkItem";
    public static final String EVENT_POSTGENERATESNAPSHOT = "PostGenerateSnapshot";
    public static final String EVENT_PREADDNOTE = "PreAddNote";
    public static final String EVENT_POSTADDNOTE = "PostAddNote";
    public static final String EVENT_PREEDITNOTE = "PreEditNote";
    public static final String EVENT_POSTEDITNOTE = "PostEditNote";
    public static final String EVENT_PREADDATTRIBUTE = "PreAddAttribute";
    public static final String EVENT_POSTADDATTRIBUTE = "PostAddAttribute";
    public static final String EVENT_PREEDITATTRIBUTE = "PreEditAttribute";
    public static final String EVENT_POSTEDITATTRIBUTE = "PostEditAttribute";
    public static final String EVENT_PREDELETEATTRIBUTE = "PreDeleteAttribute";
    public static final String EVENT_POSTDELETEATTRIBUTE = "PostDeleteAttribute";
    public static final String EVENT_PREGETATTACHMENT = "PreGetAttachment";
    public static final String EVENT_PREADDATTACHMENT = "PreAddAttachment";
    public static final String EVENT_POSTADDATTACHMENT = "PostAddAttachment";
    public static final String EVENT_PREEDITATTACHMENT = "PreEditAttachment";
    public static final String EVENT_POSTEDITATTACHMENT = "PostEditAttachment";
    public static final String EVENT_PREDELETEATTACHMENT = "PreDeleteAttachment";
    public static final String EVENT_POSTDELETEATTACHMENT = "PostDeleteAttachment";
    public static final String EVENT_PREAPPROVE = "PreApprove";
    public static final String EVENT_POSTAPPROVE = "PostApprove";
    private static final String UNDEFINED = "Undefined";
    private String errorid;
    private String message;
    private String event;
    private String errorType;
    private String sdcid;

    public ErrorDetail(String sdcid, String event, String errorid, String errorType, String message) {
        this.sdcid = sdcid;
        this.event = event;
        this.errorid = errorid;
        this.errorType = errorType;
        this.message = message;
    }

    public String getErrorid() {
        return this.errorid != null && this.errorid.length() > 0 ? this.errorid : UNDEFINED;
    }

    public String getMessage() {
        return this.message;
    }

    public String getEvent() {
        return this.event != null && this.event.length() > 0 ? this.event : UNDEFINED;
    }

    public String getErrorType() {
        return this.errorType != null && this.errorType.length() > 0 ? this.errorType : TYPE_VALIDATION;
    }

    public String getSdcid() {
        return this.sdcid != null && this.sdcid.length() > 0 ? this.sdcid : UNDEFINED;
    }

    public String toString() {
        return this.sdcid + "::" + this.event + "::" + this.errorid + "::" + this.errorType + "::" + this.message;
    }
}

