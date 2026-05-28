/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.misc;

import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class AddActivityTraceLog
extends BaseAction {
    public static final String ID = "AddActivityTraceLog";
    public static final String VERSION = "1";
    public static final String PROPERTY_DESCRIPTION = "description";
    public static final String PROPERTY_ACTIVITYTYPE = "activitytype";
    public static final String PROPERTY_ACTIVITYPROPERTYID = "activitypropertyid";
    public static final String PROPERTY_ACTIVITY = "activity";
    public static final String RETURN_TRACELOGID = "tracelogid";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        try {
            String activityProperty = properties.getProperty(PROPERTY_ACTIVITYPROPERTYID);
            String description = properties.getProperty(PROPERTY_DESCRIPTION, "");
            String activity = properties.getProperty(PROPERTY_ACTIVITY, "");
            AuditService audit = new AuditService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
            String tracelogid = audit.addActivityTraceLogEntry(activity, description, activityProperty);
            properties.setProperty(RETURN_TRACELOGID, tracelogid);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to add tracelog activity", e);
        }
    }
}

