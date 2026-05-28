/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.sapphire.actions.eln.BaseELNAction;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddWorksheetContributor
extends BaseELNAction
implements sapphire.action.AddWorksheetContributor {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String[] worksheetidlist = StringUtil.split(properties.getProperty("worksheetid"), ";");
        String[] worksheetversionidlist = StringUtil.split(properties.getProperty("worksheetversionid"), ";");
        String contributorid = properties.getProperty("contributorid");
        if (worksheetidlist.length != worksheetversionidlist.length || worksheetidlist.length == 0) {
            throw new ActionException("Inconsistent list of worksheets and versions");
        }
        for (int i = 0; i < worksheetidlist.length; ++i) {
            if (this.worksheetInProgress(this.database, worksheetidlist[i], worksheetversionidlist[i])) continue;
            throw new ActionException("Worksheets must be InProgress to have contributors assigned");
        }
        String[] contributors = StringUtil.split(contributorid, ";");
        String log = null;
        for (int i = 0; i < worksheetidlist.length; ++i) {
            String worksheetid = worksheetidlist[i];
            String worksheetversionid = worksheetversionidlist[i];
            SafeSQL safeSQL = new SafeSQL();
            DataSet worksheetcontributors = this.getQueryProcessor().getPreparedSqlDataSet("SELECT contributorid, nominatedflag FROM worksheetcontributor WHERE worksheetid=" + safeSQL.addVar(worksheetid) + " AND worksheetversionid=" + safeSQL.addVar(worksheetversionid) + " AND contributorid IN (" + safeSQL.addIn(contributorid, ";") + ")", safeSQL.getValues());
            StringBuffer add = new StringBuffer();
            StringBuffer edit = new StringBuffer();
            for (int j = 0; j < contributors.length; ++j) {
                int findRow = worksheetcontributors.findRow("contributorid", contributors[j]);
                if (findRow == -1) {
                    add.append(";").append(contributors[j]);
                    continue;
                }
                if (worksheetcontributors.getValue(findRow, "nominatedflag").equals("Y")) continue;
                edit.append(";").append(contributors[j]);
            }
            log = "";
            if (add.length() > 0) {
                PropertyList actionProps = new PropertyList();
                actionProps.setProperty("sdcid", "LV_Worksheet");
                actionProps.setProperty("keyid1", worksheetid);
                actionProps.setProperty("keyid2", worksheetversionid);
                actionProps.setProperty("linkid", "Contributor");
                actionProps.setProperty("contributorid", add.substring(1));
                actionProps.setProperty("nominatedflag", "Y");
                this.getActionProcessor().processAction("AddSDIDetail", "1", actionProps);
                log = "Added named contributor: " + add.substring(1);
            }
            if (edit.length() > 0) {
                PropertyList actionProps = new PropertyList();
                actionProps.setProperty("sdcid", "LV_Worksheet");
                actionProps.setProperty("keyid1", worksheetid);
                actionProps.setProperty("keyid2", worksheetversionid);
                actionProps.setProperty("linkid", "Contributor");
                actionProps.setProperty("contributorid", edit.substring(1));
                actionProps.setProperty("nominatedflag", "Y");
                this.getActionProcessor().processAction("EditSDIDetail", "1", actionProps);
                log = log + (log.length() > 0 ? ", " : "") + "Made contributor " + edit.substring(1) + " named";
            }
            this.addActivityLog(worksheetid, worksheetversionid, "Add", "LV_Worksheet", worksheetid, worksheetversionid, log);
        }
    }
}

