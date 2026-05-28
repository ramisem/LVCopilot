/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.sapphire.actions.eln.BaseELNAction;
import com.labvantage.sapphire.actions.eln.SetWorksheetSectionStatus;
import com.labvantage.sapphire.actions.eln.SetWorksheetStatus;
import sapphire.SapphireException;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class ResetWorksheetStatus
extends BaseELNAction
implements sapphire.action.ResetWorksheetStatus {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String worksheetversionid;
        String worksheetid = properties.getProperty("worksheetid");
        if (this.workbookInProgress(this.database, worksheetid, worksheetversionid = properties.getProperty("worksheetversionid"))) {
            ActionBlock ab = new ActionBlock();
            PropertyList setwsStatus = new PropertyList();
            setwsStatus.setProperty("worksheetid", worksheetid);
            setwsStatus.setProperty("worksheetversionid", worksheetversionid);
            setwsStatus.setProperty("status", "InProgress");
            ab.setActionClass("SetWorksheetStatus", SetWorksheetStatus.class.getName(), setwsStatus);
            if (properties.getProperty("resetall", "N").equals("Y")) {
                DataSet sections = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetsectionid, worksheetsectionversionid FROM worksheetsection WHERE sectionlevel = 1 AND worksheetid = ? AND worksheetversionid = ? ORDER BY usersequence", new Object[]{worksheetid, worksheetversionid});
                for (int i = 0; i < sections.size(); ++i) {
                    PropertyList setwssStatus = new PropertyList();
                    setwssStatus.setProperty("worksheetid", worksheetid);
                    setwssStatus.setProperty("worksheetversionid", worksheetversionid);
                    setwssStatus.setProperty("worksheetsectionid", sections.getValue(i, "worksheetsectionid"));
                    setwssStatus.setProperty("worksheetsectionversionid", sections.getValue(i, "worksheetsectionversionid"));
                    setwssStatus.setProperty("status", "InProgress");
                    ab.setActionClass("SetWorksheetSectionStatus-" + sections.getValue(i, "worksheetsectionid"), SetWorksheetSectionStatus.class.getName(), setwssStatus);
                }
            }
            this.getActionProcessor().processActionBlock(ab);
        }
    }
}

