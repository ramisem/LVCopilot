/*
 * Decompiled with CFR 0.152.
 */
package sapphire.action;

public interface SetDocumentStatus {
    public static final String ID = "SetDocumentStatus";
    public static final String VERSIONID = "1";
    public static final String PROPERTY_DOCUMENTID = "documentid";
    public static final String PROPERTY_DOCUMENTVERSIONID = "documentversionid";
    public static final String PROPERTY_DOCUMENTSTATUS = "documentstatus";
    public static final String PROPERTY_STATUSMESSAGE = "statusmessage";
    public static final String PROPERTY_AUDITREASON = "auditreason";
    public static final String PROPERTY_AUDITACTIVITY = "auditactivity";
    public static final String PROPERTY_AUDITSIGNEDFLAG = "auditsignedflag";
    public static final String PROPERTY_AUDITDT = "auditdt";
    public static final String PROPERTY_EVENTNOTIFY = "eventnotify";
    public static final String PROPERTY_APPLYLOCK = "applylock";
    public static final String DOCUMENTSTATUS_DONE = "DN";
    public static final String DOCUMENTSTATUS_CANCELLED = "CN";
    public static final String DOCUMENTSTATUS_LOCK = "LK";
}

