/*
 * Decompiled with CFR 0.152.
 */
package sapphire.action;

public interface ReagentEvent {
    public static final String ID = "ReagentEvent";
    public static final String VERSIONID = "1";
    public static final String PROPERTY_EVENTTYPE = "eventtype";
    public static final String PROPERTY_KEYID1 = "keyid1";
    public static final String PROPERTY_EXPIRYDATE = "expirydate";
    public static final String PROPERTY_REORDERDATE = "reorderdate";
    public static final String PROPERTY_TOTALQUANTITY = "totalquantity";
    public static final String EVENTTYPE_EXPIRY_WARNING_NOTIFICATION = "ExpiryWarningNotification";
    public static final String EVENTTYPE_EXPIRY_REORDER_NOTIFICATION = "ExpiryReorderNotification";
    public static final String EVENTTYPE_EXPIRY_NOTIFICATION = "ExpiryNotification";
    public static final String EVENTTYPE_REORDER_NOTIFICATION = "ReorderNotification";
}

