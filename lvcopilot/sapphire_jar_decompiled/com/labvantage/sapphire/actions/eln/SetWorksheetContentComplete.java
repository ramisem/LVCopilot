/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.sapphire.actions.eln.BaseELNAction;
import com.labvantage.sapphire.actions.eln.SetWorksheetStatus;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class SetWorksheetContentComplete
extends BaseELNAction
implements sapphire.action.SetWorksheetContentComplete {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        DataSet props = new DataSet();
        props.addColumnValues("worksheetid", 0, properties.getProperty("worksheetid"), ";");
        props.addColumnValues("worksheetversionid", 0, properties.getProperty("worksheetversionid"), ";", "1");
        String completedBy = this.connectionInfo.getSysuserId();
        if (completedBy.equalsIgnoreCase("(system)")) {
            throw new ActionException("Worksheet completion cannot be automated. This action can only be executed by a user who will be flagged as the person who completed the worksheet.");
        }
        for (int i = 0; i < props.size(); ++i) {
            String worksheetid = props.getValue(i, "worksheetid");
            String worksheetversionid = props.getValue(i, "worksheetversionid");
            this.database.createPreparedResultSet("SELECT worksheetstatus, options FROM worksheet WHERE worksheetid = ? AND worksheetversionid = ?", new Object[]{worksheetid, worksheetversionid});
            if (!this.database.getNext()) continue;
            PropertyList wsOptions = new PropertyList();
            wsOptions.setPropertyList(this.database.getClob("options"));
            String newStatus = wsOptions.getProperty("worksheetcompletion").equals("A") ? "PendingApproval" : "Complete";
            ActionBlock ab = new ActionBlock();
            PropertyList setwsStatus = new PropertyList();
            setwsStatus.setProperty("worksheetid", worksheetid);
            setwsStatus.setProperty("worksheetversionid", worksheetversionid);
            setwsStatus.setProperty("auditactivity", properties.getProperty("auditactivity"));
            setwsStatus.setProperty("auditreason", properties.getProperty("auditreason"));
            setwsStatus.setProperty("status", newStatus);
            ab.setActionClass("SetWorksheetStatus", SetWorksheetStatus.class.getName(), setwsStatus);
            this.getActionProcessor().processActionBlock(ab);
        }
    }
}

