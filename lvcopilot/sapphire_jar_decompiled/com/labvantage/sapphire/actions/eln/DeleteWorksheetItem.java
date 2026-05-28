/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.sapphire.actions.eln.BaseELNAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class DeleteWorksheetItem
extends BaseELNAction {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String worksheetid = properties.getProperty("worksheetid");
        String worksheetversionid = properties.getProperty("worksheetversionid");
        String worksheetitemid = properties.getProperty("worksheetitemid");
        String worksheetitemversionid = properties.getProperty("worksheetitemversionid");
        this.database.createPreparedResultSet("SELECT usersequence, itemstatus, worksheetsectionid, worksheetsectionversionid, propertytreeid FROM worksheetitem WHERE worksheetitemid = ? AND worksheetitemversionid = ?", new Object[]{worksheetitemid, worksheetitemversionid});
        if (this.database.getNext()) {
            String worksheetsectionversionid;
            if (!this.database.getValue("itemstatus").equals("InProgress")) {
                throw new SapphireException("Blocked delete of worksheetitem " + DeleteWorksheetItem.getIdVersionText(worksheetitemid, worksheetitemversionid) + " as the status is not " + "InProgress");
            }
            String worksheetsectionid = this.database.getValue("worksheetsectionid");
            if (this.sectionInProgress(this.database, worksheetsectionid, worksheetsectionversionid = this.database.getValue("worksheetsectionversionid"))) {
                String propertytreeid = this.database.getValue("propertytreeid");
                int usersequence = this.database.getInt("usersequence");
                int activitylogid = this.addActivityLog(worksheetid, worksheetversionid, "Delete Control", "LV_WorksheetItem", worksheetitemid, worksheetitemversionid, "Deleted '" + propertytreeid + "' worksheet item");
                this.database.executePreparedUpdate("UPDATE worksheetitem SET usersequence = usersequence - 1 WHERE worksheetsectionid = ? AND worksheetsectionversionid = ? AND usersequence >= ?", new Object[]{worksheetsectionid, worksheetsectionversionid, usersequence});
                PropertyList deleteProps = new PropertyList();
                deleteProps.setProperty("sdcid", "LV_WorksheetItem");
                deleteProps.setProperty("keyid1", worksheetitemid);
                deleteProps.setProperty("keyid2", worksheetitemversionid);
                deleteProps.setProperty("auditreason", properties.getProperty("auditreason"));
                deleteProps.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
                deleteProps.setProperty("auditactivity", properties.getProperty("auditactivity"));
                this.getActionProcessor().processAction("DeleteSDI", "1", deleteProps);
                String tracelogid = deleteProps.getProperty("tracelogid");
                if (tracelogid.length() > 0) {
                    this.database.executePreparedUpdate("UPDATE worksheetactivitylog SET tracelogid=? WHERE worksheetid=? AND worksheetversionid=? AND activitylogid=?", new Object[]{tracelogid, worksheetid, worksheetversionid, activitylogid});
                }
            }
        } else {
            throw new SapphireException("Failed to find worksheet item " + DeleteWorksheetItem.getIdVersionText(worksheetitemid, worksheetitemversionid));
        }
    }
}

