/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.spec;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.sdi.AddSDITraceLog;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class UnApplySDISpec
extends BaseAction
implements sapphire.action.UnApplySDISpec {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515$";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        boolean propsmatch = StringUtil.getYN(properties.getProperty("propsmatch"), "N").equals("Y");
        String sdcid = properties.getProperty("sdcid");
        String[] keyid1prop = StringUtil.split(properties.getProperty("keyid1"), ";");
        String[] keyid2prop = StringUtil.split(properties.getProperty("keyid2"), ";");
        String[] keyid3prop = StringUtil.split(properties.getProperty("keyid3"), ";");
        String[] specidprop = StringUtil.split(properties.getProperty("specid"), ";");
        String[] specversionidprop = StringUtil.split(properties.getProperty("specversionid"), ";");
        if (!propsmatch) {
            DataSet dsProps = new DataSet();
            for (int i = 0; i < specidprop.length; ++i) {
                String specid = specidprop[i];
                String specversionid = specversionidprop.length == 0 || specversionidprop.length < specidprop.length || specversionidprop[i].length() == 0 ? "1" : specversionidprop[i];
                for (int k = 0; k < keyid1prop.length; ++k) {
                    String keyid1 = keyid1prop[k];
                    String keyid2 = keyid2prop.length == 0 || keyid2prop.length < keyid1prop.length || keyid2prop[k].length() == 0 ? "(null)" : keyid2prop[k];
                    String keyid3 = keyid3prop.length == 0 || keyid3prop.length < keyid1prop.length || keyid3prop[k].length() == 0 ? "(null)" : keyid3prop[k];
                    int r = dsProps.addRow();
                    dsProps.setString(r, "specid", specid);
                    dsProps.setString(r, "specversionid", specversionid);
                    dsProps.setString(r, "keyid1", keyid1);
                    dsProps.setString(r, "keyid2", keyid2);
                    dsProps.setString(r, "keyid3", keyid3);
                }
            }
            if (dsProps.getRowCount() > 0) {
                properties.setProperty("sdcid", sdcid);
                properties.setProperty("propsmatch", "Y");
                properties.setProperty("specid", dsProps.getColumnValues("specid", ";"));
                properties.setProperty("specversionid", dsProps.getColumnValues("specversionid", ";"));
                properties.setProperty("keyid1", dsProps.getColumnValues("keyid1", ";"));
                properties.setProperty("keyid2", dsProps.getColumnValues("keyid2", ";"));
                properties.setProperty("keyid3", dsProps.getColumnValues("keyid3", ";"));
                this.processAction(properties);
            }
        } else {
            String reason = properties.getProperty("auditreason");
            String activity = properties.getProperty("auditactivity");
            String signedFlag = properties.getProperty("auditsignedflag");
            String auditdt = properties.getProperty("auditdt");
            String tracelogid = properties.getProperty("tracelogid", "").trim();
            if (reason.length() > 0 && tracelogid.length() == 0) {
                this.logger.info("Generate the tracelog record");
                PropertyList tracelogprops = new PropertyList();
                tracelogprops.setProperty("sdcid", sdcid);
                tracelogprops.setProperty("keyid1", properties.getProperty("keyid1"));
                tracelogprops.setProperty("keyid2", properties.getProperty("keyid2"));
                tracelogprops.setProperty("keyid3", properties.getProperty("keyid3"));
                tracelogprops.setProperty("description", "Unapplied Specification");
                tracelogprops.setProperty("auditreason", reason);
                tracelogprops.setProperty("auditactivity", activity);
                tracelogprops.setProperty("auditsignedflag", signedFlag);
                tracelogprops.setProperty("auditdt", auditdt);
                ActionProcessor ap = this.getActionProcessor();
                ap.processActionClass(AddSDITraceLog.class.getName(), tracelogprops);
                tracelogid = tracelogprops.getProperty("tracelogid");
                properties.setProperty("tracelogid", tracelogid);
            }
            DataSet processedSDIs = new DataSet();
            DataSet processedSpecs = new DataSet();
            for (int i = 0; i < specidprop.length; ++i) {
                String specid = specidprop[i];
                String specversionid = specversionidprop.length == 0 || specversionidprop.length < specidprop.length || specversionidprop[i].length() == 0 ? "1" : specversionidprop[i];
                String keyid1 = keyid1prop[i];
                String keyid2 = keyid2prop.length == 0 || keyid2prop.length < keyid1prop.length || keyid2prop[i].length() == 0 ? "(null)" : keyid2prop[i];
                String keyid3 = keyid3prop.length == 0 || keyid3prop.length < keyid1prop.length || keyid3prop[i].length() == 0 ? "(null)" : keyid3prop[i];
                SafeSQL safeSQL = new SafeSQL();
                StringBuffer sql = new StringBuffer("DELETE FROM sdidataitemspec ");
                sql.append(" WHERE  sdcid = ").append(safeSQL.addVar(sdcid));
                sql.append(" AND keyid1 = ").append(safeSQL.addVar(keyid1));
                sql.append(" AND keyid2 = ").append(safeSQL.addVar(keyid2));
                sql.append(" AND keyid3 = ").append(safeSQL.addVar(keyid3));
                sql.append(" AND specid = ").append(safeSQL.addVar(specid));
                sql.append(" AND specversionid = ").append(safeSQL.addVar(specversionid));
                Trace.logDebug(" sql = " + sql.toString());
                int updateCnt = this.database.executePreparedUpdate(sql.toString(), safeSQL.getValues());
                if (updateCnt > 0) {
                    safeSQL.reset();
                    StringBuffer sqlUpdate = new StringBuffer("Update sdispec set appliedflag = 'N', condition = null, tracelogid = ").append(safeSQL.addVar(tracelogid)).append(" WHERE sdcid = ");
                    sqlUpdate.append(safeSQL.addVar(sdcid)).append(" AND keyid1 = ").append(safeSQL.addVar(keyid1)).append(" AND keyid2 = ").append(safeSQL.addVar(keyid2)).append(" AND keyid3 = ").append(safeSQL.addVar(keyid3)).append(" AND specid = ").append(safeSQL.addVar(specid)).append(" AND specversionid = ").append(safeSQL.addVar(specversionid));
                    updateCnt = this.database.executePreparedUpdate(sqlUpdate.toString(), safeSQL.getValues());
                    if (updateCnt > 0) {
                        int r;
                        HashMap<String, String> findRow = new HashMap<String, String>();
                        findRow.put("keyid1", keyid1);
                        findRow.put("keyid2", keyid2);
                        findRow.put("keyid3", keyid3);
                        if (processedSDIs.findRow(findRow) < 0) {
                            r = processedSDIs.addRow();
                            processedSDIs.setString(r, "keyid1", keyid1);
                            processedSDIs.setString(r, "keyid2", keyid2);
                            processedSDIs.setString(r, "keyid3", keyid3);
                        }
                        findRow.clear();
                        findRow.put("specid", specid);
                        findRow.put("specversionid", specversionid);
                        if (processedSpecs.findRow(findRow) >= 0) continue;
                        r = processedSpecs.addRow();
                        processedSpecs.setString(r, "specid", specid);
                        processedSpecs.setString(r, "specversionid", specversionid);
                        continue;
                    }
                    Trace.logDebug("SDISpec applied flag could not be set for SDI " + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3 + " Spec:" + specid + ";" + specversionid);
                    continue;
                }
                Trace.logDebug(" Spec " + specid + ";" + specversionid + " could not be un applied from SDI: " + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3);
            }
        }
    }
}

