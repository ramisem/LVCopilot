/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.sapphire.actions.eln.BaseELNAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class DeleteWorksheetSection
extends BaseELNAction {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String worksheetversionid;
        String worksheetid = properties.getProperty("worksheetid");
        if (this.worksheetInProgress(this.database, worksheetid, worksheetversionid = properties.getProperty("worksheetversionid"))) {
            String worksheetsectionid = properties.getProperty("worksheetsectionid");
            String worksheetsectionversionid = properties.getProperty("worksheetsectionversionid");
            this.database.createPreparedResultSet("SELECT worksheetsectiondesc, usersequence, sectionstatus FROM worksheetsection WHERE worksheetsectionid = ? AND worksheetsectionversionid = ?", new Object[]{worksheetsectionid, worksheetsectionversionid});
            if (this.database.getNext()) {
                if (!this.database.getValue("sectionstatus").equals("InProgress")) {
                    throw new SapphireException("Blocked delete of worksheet section " + DeleteWorksheetSection.getIdVersionText(worksheetsectionid, worksheetsectionversionid) + " as the status is not " + "InProgress");
                }
                String sectiondesc = this.database.getValue("worksheetsectiondesc");
                int usersequence = this.database.getInt("usersequence");
                this.database.executePreparedUpdate("UPDATE worksheetsection SET usersequence = usersequence - 1 WHERE worksheetsectionid = ? AND worksheetsectionversionid = ? AND usersequence >= ?", new Object[]{worksheetsectionid, worksheetsectionversionid, usersequence});
                this.addActivityLog(worksheetid, worksheetversionid, "Delete", "LV_WorksheetSection", worksheetsectionid, worksheetsectionversionid, "Deleted section '" + sectiondesc + "'");
                PropertyList deleteProps = new PropertyList();
                deleteProps.setProperty("sdcid", "LV_WorksheetSection");
                deleteProps.setProperty("keyid1", worksheetsectionid);
                deleteProps.setProperty("keyid2", worksheetsectionversionid);
                this.getActionProcessor().processAction("DeleteSDI", "1", deleteProps);
            } else {
                throw new SapphireException("Failed to find worksheet section " + DeleteWorksheetSection.getIdVersionText(worksheetsectionid, worksheetsectionversionid));
            }
        }
    }
}

