/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.dashboard.brules;

import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LV_Measure
extends BaseSDCRules {
    private static final String LABVANTAGE_CVS_ID = "$Revision: 53836 $";
    private String ruleid = "";
    private static String MEASUREMENT_SDCID = "LV_Measure";
    public static String LOG_NAME = "DashboardMeasure";
    private static String MEASUREMENT_MEASUREMENT = "measurevalue";
    private static String MEASUREMENT_MEASUREDATE = "measuredate";
    private static String MEASUREMENT_MEASURECAT = "measurecategory";
    public String[] noeditcolumns = new String[]{MEASUREMENT_MEASUREMENT, MEASUREMENT_MEASUREDATE, MEASUREMENT_MEASURECAT};

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.logTrace(LOG_NAME + ": begin " + MEASUREMENT_SDCID + " pre-add");
        this.ruleid = "UpdateLatestFlag";
        String measurementtableid = (String)this.getSDCProcessor().getSDCProperties(MEASUREMENT_SDCID).get("tableid");
        String measurementkeyid1 = (String)this.getSDCProcessor().getSDCProperties(MEASUREMENT_SDCID).get("keycolid1");
        DataSet primary = sdiData.getDataset("primary");
        HashMap<String, String> hmUProps = new HashMap<String, String>();
        StringBuffer sbMIDs = new StringBuffer("");
        StringBuffer sbMLF = new StringBuffer("");
        this.logTrace(LOG_NAME + ": scanning " + primary.size() + " records");
        try {
            for (int i = 0; i < primary.size(); ++i) {
                String measurementid = primary.getValue(i, measurementkeyid1);
                String latestflag = primary.getValue(i, "latestflag");
                String monitorid = primary.getValue(i, "monitorid");
                if (!latestflag.equalsIgnoreCase("Y")) {
                    primary.setValue(i, "latestflag", "Y");
                }
                String sql = "select " + measurementkeyid1 + " from " + measurementtableid + " where latestflag = 'Y' and monitorid = ? and " + measurementkeyid1 + " != ?";
                Object[] p = new Object[]{monitorid, measurementid};
                DataSet dsMUpdates = this.getQueryProcessor().getPreparedSqlDataSet(sql, p);
                for (int j = 0; j < dsMUpdates.getRowCount(); ++j) {
                    sbMIDs.append(";" + dsMUpdates.getValue(j, measurementkeyid1));
                    sbMLF.append(";N");
                }
            }
            if (sbMIDs.length() > 1) {
                hmUProps.put("sdcid", MEASUREMENT_SDCID);
                hmUProps.put("keyid1", sbMIDs.toString().substring(1));
                hmUProps.put("latestflag", sbMLF.toString().substring(1));
                this.logTrace(LOG_NAME + ": updating latestflags for " + sbMIDs.toString().substring(1));
                this.getActionProcessor().processAction("EditSDI", "1", hmUProps);
            }
        }
        catch (Exception e) {
            this.logTrace(LOG_NAME + ": " + this.ruleid + " failed - " + e.getMessage());
            this.setError(this.ruleid, "VALIDATION", e.getMessage());
        }
        this.logTrace(LOG_NAME + ": scan done");
        this.logTrace(LOG_NAME + ": end " + MEASUREMENT_SDCID + " pre-add");
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.logTrace(LOG_NAME + ": begin " + MEASUREMENT_SDCID + " pre-edit");
        this.ruleid = "NoChanges";
        DataSet primary = sdiData.getDataset("primary");
        this.logTrace(LOG_NAME + ": scanning " + primary.size() + " records");
        try {
            for (int i = 0; i < primary.size(); ++i) {
                for (int j = 0; j < this.noeditcolumns.length; ++j) {
                    if (!this.hasPrimaryValueChanged(primary, i, this.noeditcolumns[j])) continue;
                    throw new SapphireException("Changes are not allowed in measured values!");
                }
            }
        }
        catch (Exception e) {
            this.logTrace(LOG_NAME + ": " + this.ruleid + " failed - " + e.getMessage());
            this.setError(this.ruleid, "VALIDATION", e.getMessage());
        }
        this.logTrace(LOG_NAME + ": scan done");
        this.logTrace(LOG_NAME + ": end " + MEASUREMENT_SDCID + " pre-edit");
    }

    public String getKeyFor(String rgmonid, DataSet dsDetails) {
        StringBuffer sbKey = new StringBuffer("");
        sbKey.append("<table>");
        for (int i = 0; i < dsDetails.getRowCount(); ++i) {
            sbKey.append("<tr>");
            sbKey.append("<td>");
            String select = dsDetails.getValue(i, "rgmonselect");
            String column = dsDetails.getValue(i, "rgmoncolumn");
            String value = dsDetails.getValue(i, "rgmonvalue");
            sbKey.append(select + " " + column + " " + value);
            sbKey.append("</td>");
            sbKey.append("</tr>");
        }
        sbKey.append("</table>");
        return sbKey.toString();
    }

    @Override
    public String getSdcid() {
        String classwithpackage = this.getClass().getName();
        String classname = classwithpackage.substring(classwithpackage.lastIndexOf(".") + 1);
        return classname;
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }
}

