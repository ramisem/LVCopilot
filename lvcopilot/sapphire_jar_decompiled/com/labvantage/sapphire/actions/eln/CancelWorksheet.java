/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.sapphire.actions.eln.BaseELNAction;
import com.labvantage.sapphire.actions.eln.SetWorksheetStatus;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import sapphire.SapphireException;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class CancelWorksheet
extends BaseELNAction
implements sapphire.action.CancelWorksheet {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        DataSet props = new DataSet();
        props.addColumnValues("worksheetid", 0, properties.getProperty("worksheetid"), ";");
        props.addColumnValues("worksheetversionid", 0, properties.getProperty("worksheetversionid"), ";", "1");
        String tracelogid = properties.getProperty("tracelogid");
        int numberTracelog = -1;
        if (tracelogid.length() == 0 && properties.getProperty("auditreason").length() > 0) {
            try {
                AuditService audit = new AuditService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                numberTracelog = Integer.parseInt(audit.addSDITraceLogEntry("LV_Worksheet", properties.getProperty("worksheetid"), properties.getProperty("worksheetversionid"), "", properties.getProperty("auditreason"), properties.getProperty("auditactivity"), properties.getProperty("auditsignedflag"), "N", "CancelWorksheet", true));
            }
            catch (ServiceException audit) {
                // empty catch block
            }
        }
        for (int i = 0; i < props.size(); ++i) {
            String worksheetid = props.getValue(i, "worksheetid");
            String worksheetversionid = props.getValue(i, "worksheetversionid");
            this.database.createPreparedResultSet("SELECT worksheetstatus FROM worksheet WHERE worksheetid = ? AND worksheetversionid = ?", new Object[]{worksheetid, worksheetversionid});
            if (!this.database.getNext()) continue;
            ActionBlock ab = new ActionBlock();
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("worksheetid", worksheetid);
            actionProps.setProperty("worksheetversionid", worksheetversionid);
            if (tracelogid.length() > 0) {
                actionProps.setProperty("tracelogid", tracelogid);
            } else if (numberTracelog >= 0) {
                actionProps.setProperty("tracelogid", "" + (numberTracelog + i));
            }
            actionProps.setProperty("status", "Cancelled");
            ab.setActionClass("SetWorksheetStatus", SetWorksheetStatus.class.getName(), actionProps);
            this.getActionProcessor().processActionBlock(ab);
        }
    }
}

