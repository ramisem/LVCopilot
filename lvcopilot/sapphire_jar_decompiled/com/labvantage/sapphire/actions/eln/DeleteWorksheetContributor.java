/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.sapphire.actions.eln.BaseELNAction;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class DeleteWorksheetContributor
extends BaseELNAction {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String worksheetversionid;
        String worksheetid = properties.getProperty("worksheetid");
        if (this.worksheetInProgress(this.database, worksheetid, worksheetversionid = properties.getProperty("worksheetversionid"))) {
            String contributorid = properties.getProperty("contributorid");
            DataSet worksheetcontributors = this.getQueryProcessor().getPreparedSqlDataSet("SELECT contributorid FROM worksheetcontributor WHERE worksheetid = ? AND worksheetversionid = ? AND contributorid = ?", new Object[]{worksheetid, worksheetversionid, contributorid});
            if (worksheetcontributors.size() == 1) {
                this.database.createPreparedResultSet("SELECT activityby FROM worksheetactivitylog WHERE worksheetid = ? AND worksheetversionid = ? AND activityby = ?", new Object[]{worksheetid, worksheetversionid, contributorid});
                if (this.database.getNext()) {
                    PropertyList actionProps = new PropertyList();
                    actionProps.setProperty("sdcid", "LV_Worksheet");
                    actionProps.setProperty("keyid1", worksheetid);
                    actionProps.setProperty("keyid2", worksheetversionid);
                    actionProps.setProperty("linkid", "Contributor");
                    actionProps.setProperty("contributorid", contributorid);
                    actionProps.setProperty("nominatedflag", "N");
                    this.getActionProcessor().processAction("EditSDIDetail", "1", actionProps);
                } else {
                    PropertyList actionProps = new PropertyList();
                    actionProps.setProperty("sdcid", "LV_Worksheet");
                    actionProps.setProperty("keyid1", worksheetid);
                    actionProps.setProperty("keyid2", worksheetversionid);
                    actionProps.setProperty("linkid", "Contributor");
                    actionProps.setProperty("contributorid", contributorid);
                    this.getActionProcessor().processAction("DeleteSDIDetail", "1", actionProps);
                }
            }
            this.addActivityLog(worksheetid, worksheetversionid, "Delete", "LV_Worksheet", worksheetid, worksheetversionid, "Deleted contributor - " + contributorid);
        }
    }
}

