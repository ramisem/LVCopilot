/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdiapproval;

import com.labvantage.sapphire.actions.sdi.BaseSDIAction;
import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class UpdateSDIToCurrentVersion
extends BaseSDIAction {
    SDCProcessor sdcProcessor;

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.sdcProcessor = this.getSDCProcessor();
        String overwriteApprovedDtFlag = properties.getProperty("overwriteapproveddtflag", "Y");
        DataSet dslistofsdcstobequeried = this.fetchListOfSDCsWithEffectiveDateFlag();
        if (dslistofsdcstobequeried != null && dslistofsdcstobequeried.getRowCount() > 0) {
            for (int i = 0; i < dslistofsdcstobequeried.getRowCount(); ++i) {
                String sdcid = dslistofsdcstobequeried.getValue(i, "sdcid");
                this.updateStatusToCurrent(sdcid, overwriteApprovedDtFlag);
            }
        }
    }

    private void updateStatusToCurrent(String sdcid, String overwriteApprovedDtFlag) throws SapphireException {
        boolean tripleKey = this.sdcProcessor.getProperty(sdcid, "keycolumns").equals("3");
        StringBuilder sqltofetcheffectivedate = new StringBuilder();
        String effectivedatewhereclause = this.database.isSqlServer() ? "versioneffectivedt <= GETDATE()" : "versioneffectivedt <= sysdate";
        String tableId = this.sdcProcessor.getProperty(sdcid, "tableid");
        String keycolid1 = this.sdcProcessor.getProperty(sdcid, "keycolid1");
        String keycolid2 = this.sdcProcessor.getProperty(sdcid, "keycolid2");
        String keycolid3 = this.sdcProcessor.getProperty(sdcid, "keycolid3");
        sqltofetcheffectivedate.append("SELECT ").append(keycolid1).append(", ").append(keycolid2);
        if (tripleKey) {
            sqltofetcheffectivedate.append(", ").append(keycolid3);
        }
        sqltofetcheffectivedate.append(", ").append("versioneffectivedt").append(", ").append("versionapproveddt").append(" FROM ").append(tableId).append(" WHERE ").append(effectivedatewhereclause).append(" AND versionstatus = 'P' AND ").append("versionapproveddt").append(" IS NOT NULL ");
        sqltofetcheffectivedate.append(" AND ").append(keycolid1).append(" NOT IN ( SELECT t.LINKKEYID1 from CHANGELOG t where t.CHANGELOGSTATUS = 'Checked Out'").append(" AND t.LINKKEYID1 = ").append(keycolid1).append(" AND t.LINKKEYID2 = ").append(keycolid2);
        if (tripleKey) {
            sqltofetcheffectivedate.append(" AND t.LINKKEYID3 = ").append(keycolid3);
        }
        sqltofetcheffectivedate.append(")");
        DataSet dsforeffectivedate = this.getQueryProcessor().getSqlDataSet(sqltofetcheffectivedate.toString());
        if (dsforeffectivedate != null && dsforeffectivedate.getRowCount() > 0) {
            String groupcriteria = this.sdcProcessor.getProperty(sdcid, "keycolid1");
            if (tripleKey) {
                groupcriteria = groupcriteria + "," + this.sdcProcessor.getProperty(sdcid, "keycolid3");
            }
            String sortcriteria = groupcriteria + "," + this.sdcProcessor.getProperty(sdcid, "keycolid2") + " D";
            dsforeffectivedate.sort(sortcriteria);
            ArrayList<DataSet> arrayList = dsforeffectivedate.getGroupedDataSets(groupcriteria);
            String versionStatus = "";
            String keyid1 = "";
            String keyid2 = "";
            String keyid3 = "";
            for (int j = 0; j < arrayList.size(); ++j) {
                DataSet dsfromarraylist = arrayList.get(j);
                for (int k = 0; k < dsfromarraylist.getRowCount(); ++k) {
                    keyid1 = keyid1 + ";" + dsfromarraylist.getValue(k, this.sdcProcessor.getProperty(sdcid, "keycolid1"));
                    keyid2 = keyid2 + ";" + dsfromarraylist.getValue(k, this.sdcProcessor.getProperty(sdcid, "keycolid2"));
                    if (tripleKey) {
                        keyid3 = keyid3 + ";" + dsfromarraylist.getValue(k, this.sdcProcessor.getProperty(sdcid, "keycolid3"));
                    }
                    versionStatus = k == 0 ? versionStatus + ";C" : versionStatus + ";A";
                }
            }
            if (keyid1.trim().length() > 0 && keyid2.trim().length() > 0) {
                PropertyList plupdateversionstatus = new PropertyList();
                plupdateversionstatus.setProperty("sdcid", sdcid);
                plupdateversionstatus.setProperty("keyid1", keyid1.substring(1));
                plupdateversionstatus.setProperty("keyid2", keyid2.substring(1));
                if (tripleKey) {
                    plupdateversionstatus.setProperty("keyid3", keyid3.substring(1));
                }
                plupdateversionstatus.setProperty("versionstatus", versionStatus.substring(1));
                plupdateversionstatus.setProperty("overwriteapproveddtflag", overwriteApprovedDtFlag);
                this.getActionProcessor().processAction("SetSDIVersionStatus", "1", plupdateversionstatus);
            }
        }
    }

    private DataSet fetchListOfSDCsWithEffectiveDateFlag() throws SapphireException {
        return this.getQueryProcessor().getSqlDataSet("SELECT sdcid, tableid, versionedflag FROM sdc WHERE versionuseeffectivedtflag = 'Y' ");
    }
}

