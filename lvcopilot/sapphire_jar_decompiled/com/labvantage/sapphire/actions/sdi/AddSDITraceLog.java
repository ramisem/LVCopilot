/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class AddSDITraceLog
extends BaseAction {
    public static final String ID = "AddSDITraceLog";
    public static final String VERSION = "1";
    public static final String PROPERTY_SDCID = "sdcid";
    public static final String PROPERTY_KEYID1 = "keyid1";
    public static final String PROPERTY_KEYID2 = "keyid2";
    public static final String PROPERTY_KEYID3 = "keyid3";
    public static final String PROPERTY_DESCRIPTION = "description";
    public static final String PROPERTY_STANDARD = "standard";
    public static final String PROPERTY_AUDITREASON = "auditreason";
    public static final String PROPERTY_AUDITACTIVITY = "auditactivity";
    public static final String PROPERTY_AUDITSIGNEDFLAG = "auditsignedflag";
    public static final String PROPERTY_AUDITDT = "auditdt";
    public static final String RETURN_TRACELOGID = "tracelogid";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        AuditService audit = new AuditService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
        try {
            String tracelogid = audit.addSDITraceLogEntry(properties.getProperty(PROPERTY_SDCID), properties.getProperty(PROPERTY_KEYID1), properties.getProperty(PROPERTY_KEYID2), properties.getProperty(PROPERTY_KEYID3), properties.getProperty(PROPERTY_AUDITREASON), properties.getProperty(PROPERTY_AUDITACTIVITY, ""), properties.getProperty(PROPERTY_AUDITSIGNEDFLAG, "N"), properties.getProperty(PROPERTY_AUDITDT, "Now"), properties.getProperty(PROPERTY_DESCRIPTION), properties.getProperty(PROPERTY_STANDARD, "N").equals("Y"));
            properties.setProperty(RETURN_TRACELOGID, tracelogid);
        }
        catch (ServiceException e) {
            throw new SapphireException("Failed to add sdi trace", e);
        }
    }
}

