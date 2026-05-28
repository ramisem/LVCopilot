/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.dashboard.tasks;

import java.util.HashMap;
import sapphire.accessor.ActionException;
import sapphire.action.BaseScheduleTask;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class DashboardMeasure
extends BaseScheduleTask {
    private static final String LABVANTAGE_CVS_ID = "$Revision: 53890 $";
    private static final String MONITOR_SDCID = "LV_Monitor";
    private static final String MEASUREMENT_SDCID = "LV_Measurement";

    @Override
    public void execute() {
        HashMap hmMonitorInfo = this.getSDCProcessor().getSDCProperties(MONITOR_SDCID);
        String monitoridkeycol = (String)hmMonitorInfo.get("keycolid1");
        String monitortableid = (String)hmMonitorInfo.get("tableid");
        HashMap<String, String> measureprops = new HashMap<String, String>();
        HashMap<String, String> monitorprops = new HashMap<String, String>();
        monitorprops.put("sdcid", MONITOR_SDCID);
        StringBuffer sbMonitorids = new StringBuffer("");
        StringBuffer sbLastmondate = new StringBuffer("");
        measureprops.put("sdcid", MEASUREMENT_SDCID);
        measureprops.put("keyid1", "(auto)");
        this.logger.debug("executing " + this.sourceSdcidList);
        this.logger.debug("executing " + this.sourceKeyid1List);
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select " + monitoridkeycol + " as \"monitorid\", monitorsql from " + monitortableid + " where " + monitoridkeycol + " in (" + safeSQL.addIn(this.sourceKeyid1List, ";") + ")";
        DataSet dsMonitors = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        for (int i = 0; i < dsMonitors.getRowCount(); ++i) {
            String monitorid = dsMonitors.getValue(i, "monitorid");
            sbMonitorids.append(";" + monitorid);
            sbLastmondate.append(";n");
            long startmonitor = System.currentTimeMillis();
            this.logger.debug("fetching " + monitorid);
            String monitorsql = dsMonitors.getValue(i, "monitorsql");
            DataSet dsMeasurements = this.getQueryProcessor().getSqlDataSet(monitorsql);
            StringBuffer sbMMonitorid = new StringBuffer("");
            StringBuffer sbMValues = new StringBuffer("");
            StringBuffer sbMCats = new StringBuffer("");
            StringBuffer sbMDates = new StringBuffer("");
            StringBuffer sbMCost = new StringBuffer("");
            StringBuffer sbMLatest = new StringBuffer("");
            for (int j = 0; j < dsMeasurements.getRowCount(); ++j) {
                sbMValues.append(";" + dsMeasurements.getValue(j, "value"));
                sbMCats.append(";" + dsMeasurements.getValue(j, "category"));
                sbMMonitorid.append(";" + monitorid);
                sbMDates.append(";n");
                sbMCost.append(";" + String.valueOf(System.currentTimeMillis() - startmonitor));
                sbMLatest.append(";Y");
            }
            if (dsMeasurements.getRowCount() > 0) {
                measureprops.put("measurevalue", sbMValues.toString().substring(1));
                measureprops.put("measurecategory", sbMCats.toString().substring(1));
                measureprops.put("monitorid", sbMMonitorid.toString().substring(1));
                measureprops.put("measuredate", sbMDates.toString().substring(1));
                measureprops.put("measurecost", sbMCost.toString().substring(1));
                measureprops.put("latestflag", sbMLatest.toString().substring(1));
                try {
                    this.logger.debug("adding " + dsMeasurements.getRowCount() + " measurement(s)");
                    this.getActionProcessor().processAction("AddSDI", "1", measureprops);
                }
                catch (ActionException ae) {
                    this.logger.debug("measurement: exception " + ae.getMessage());
                    throw new RuntimeException(ae);
                }
            }
            if (dsMonitors.getRowCount() > 0) {
                monitorprops.put("keyid1", sbMonitorids.toString().substring(1));
                monitorprops.put("lastmonitordate", sbLastmondate.toString().substring(1));
                try {
                    this.logger.debug("updating monitors");
                    this.getActionProcessor().processAction("EditSDI", "1", monitorprops);
                }
                catch (ActionException ae) {
                    this.logger.error("monitor: exception " + ae.getMessage(), ae);
                    throw new RuntimeException(ae);
                }
            }
            this.logger.debug("monitor done (" + (System.currentTimeMillis() - startmonitor) + "ms)");
        }
    }

    private String asInClause(String keylist) {
        this.logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + keylist);
        String[] sKeys = keylist.split(";");
        StringBuffer sbKeyclause = new StringBuffer("");
        for (int i = 0; i < sKeys.length; ++i) {
            sbKeyclause.append(", '" + sKeys[i] + "'");
        }
        this.logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + sbKeyclause.toString().substring(1));
        return sbKeyclause.toString().substring(1);
    }
}

