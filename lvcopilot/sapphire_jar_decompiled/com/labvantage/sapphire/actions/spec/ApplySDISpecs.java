/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.spec;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.sdi.AddSDITraceLog;
import com.labvantage.sapphire.actions.sdidata.BaseSDIDataEntryAction;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ApplySDISpecs
extends BaseSDIDataEntryAction
implements sapphire.action.ApplySDISpecs {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        boolean postAddSDISpec = StringUtil.getYN(properties.getProperty("postaddsdispec"), "N").equals("Y");
        if (postAddSDISpec) {
            properties.remove("postaddsdispec");
            properties.setProperty("postspecapply", "Y");
            this.dataEntry(properties, true, false);
        } else {
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
                    tracelogprops.setProperty("description", "Applied Specification");
                    tracelogprops.setProperty("auditreason", reason);
                    tracelogprops.setProperty("auditactivity", activity);
                    tracelogprops.setProperty("auditsignedflag", signedFlag);
                    tracelogprops.setProperty("auditdt", auditdt);
                    ActionProcessor ap = this.getActionProcessor();
                    ap.processActionClass(AddSDITraceLog.class.getName(), tracelogprops);
                    tracelogid = tracelogprops.getProperty("tracelogid");
                    properties.setProperty("tracelogid", tracelogid);
                }
                DataSet processedSDISpecs = new DataSet();
                DataSet processedSDIs = new DataSet();
                DataSet processedSpecs = new DataSet();
                for (int i = 0; i < specidprop.length; ++i) {
                    String specid = specidprop[i];
                    String specversionid = specversionidprop.length == 0 || specversionidprop.length < specidprop.length || specversionidprop[i].length() == 0 ? "1" : specversionidprop[i];
                    String keyid1 = keyid1prop[i];
                    String keyid2 = keyid2prop.length == 0 || keyid2prop.length < keyid1prop.length || keyid2prop[i].length() == 0 ? "(null)" : keyid2prop[i];
                    String keyid3 = keyid3prop.length == 0 || keyid3prop.length < keyid1prop.length || keyid3prop[i].length() == 0 ? "(null)" : keyid3prop[i];
                    HashMap<String, String> findProcessedSDI = new HashMap<String, String>();
                    findProcessedSDI.put("specid", specid);
                    findProcessedSDI.put("specversionid", specversionid);
                    findProcessedSDI.put("keyid1", keyid1);
                    findProcessedSDI.put("keyid2", keyid2);
                    findProcessedSDI.put("keyid3", keyid3);
                    if (processedSDISpecs.findRow(findProcessedSDI) >= 0) continue;
                    SafeSQL safeSQL = new SafeSQL();
                    StringBuffer sql = new StringBuffer("INSERT INTO sdidataitemspec (sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, ");
                    sql.append(" variantid, dataset, paramid, paramtype, replicateid, specid, specversionid, usersequence, reportflag, tracelogid ) ");
                    sql.append("  ( ");
                    sql.append(" SELECT sdidataitem.sdcid, sdidataitem.keyid1, sdidataitem.keyid2, sdidataitem.keyid3, ");
                    sql.append(" sdidataitem.paramlistid, sdidataitem.paramlistversionid, sdidataitem.variantid, ");
                    sql.append(" sdidataitem.dataset, sdidataitem.paramid, sdidataitem.paramtype, sdidataitem.replicateid, ");
                    sql.append(" ").append(safeSQL.addVar(specid)).append(",").append(safeSQL.addVar(specversionid)).append(", specparamitems.usersequence, specparamitems.reportflag,").append(safeSQL.addVar(tracelogid));
                    sql.append(" FROM sdidataitem, specparamitems, sdispec ");
                    sql.append(" WHERE  sdispec.sdcid = ").append(safeSQL.addVar(sdcid));
                    sql.append(" AND  sdispec.keyid1 = ").append(safeSQL.addVar(keyid1));
                    sql.append(" AND  sdispec.keyid2 = ").append(safeSQL.addVar(keyid2));
                    sql.append(" AND  sdispec.keyid3 = ").append(safeSQL.addVar(keyid3));
                    sql.append(" AND  sdispec.specid = ").append(safeSQL.addVar(specid));
                    sql.append(" AND  sdispec.specversionid = ").append(safeSQL.addVar(specversionid)).append(" AND sdispec.appliedflag != 'Y'");
                    sql.append(" AND sdidataitem.sdcid = sdispec.sdcid ");
                    sql.append(" AND sdidataitem.keyid1 = sdispec.keyid1 ");
                    sql.append(" AND sdidataitem.keyid2 = sdispec.keyid2");
                    sql.append(" AND sdidataitem.keyid3 = sdispec.keyid3");
                    sql.append(" AND sdidataitem.paramid = specparamitems.paramid ");
                    sql.append(" AND sdidataitem.paramtype = specparamitems.paramtype ");
                    sql.append(" AND ");
                    sql.append(" ( ");
                    sql.append(" ( ");
                    sql.append(" specparamitems.allowanyparamlistflag = 'Y' ");
                    sql.append(" ) ");
                    sql.append(" OR ");
                    sql.append(" ( ");
                    sql.append(" sdidataitem.paramlistid = specparamitems.paramlistid ");
                    sql.append(" AND sdidataitem.paramlistversionid = specparamitems.paramlistversionid ");
                    sql.append(" AND sdidataitem.variantid = specparamitems.variantid ");
                    sql.append(" AND (specparamitems.allowanyparamlistflag = 'N' OR specparamitems.allowanyparamlistflag is null OR specparamitems.allowanyparamlistflag='') ");
                    sql.append(" ) ");
                    sql.append(" OR ");
                    sql.append(" ( ");
                    sql.append(" sdidataitem.paramlistid = specparamitems.paramlistid ");
                    sql.append(" AND sdidataitem.variantid = specparamitems.variantid ");
                    sql.append(" AND specparamitems.allowanyparamlistflag = 'V' ");
                    sql.append(" ) ");
                    sql.append(" OR ");
                    sql.append(" ( ");
                    sql.append(" sdidataitem.paramlistid = specparamitems.paramlistid ");
                    sql.append(" AND specparamitems.allowanyparamlistflag = 'A' ");
                    sql.append(" ) ");
                    sql.append(" ) ");
                    sql.append(" AND specparamitems.specid = sdispec.specid ");
                    sql.append(" AND specparamitems.specversionid = sdispec.specversionid ");
                    sql.append(" ) ");
                    Trace.logDebug(" sql = " + sql.toString());
                    int updateCnt = this.database.executePreparedUpdate(sql.toString(), safeSQL.getValues());
                    if (updateCnt <= 0) continue;
                    safeSQL.reset();
                    StringBuffer sqlUpdate = new StringBuffer("Update sdispec set appliedflag = 'Y', tracelogid = ").append(safeSQL.addVar(tracelogid)).append(" WHERE sdcid = ");
                    sqlUpdate.append(safeSQL.addVar(sdcid)).append(" and keyid1 = ").append(safeSQL.addVar(keyid1)).append(" and keyid2 = ").append(safeSQL.addVar(keyid2)).append(" and keyid3 = ").append(safeSQL.addVar(keyid3)).append(" and specid = ").append(safeSQL.addVar(specid)).append(" and specversionid = ").append(safeSQL.addVar(specversionid));
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
                        findRow.put("specid", specid);
                        findRow.put("specversionid", specversionid);
                        if (processedSDISpecs.findRow(findRow) < 0) {
                            r = processedSDISpecs.addRow();
                            processedSDISpecs.setString(r, "specid", specid);
                            processedSDISpecs.setString(r, "specversionid", specversionid);
                            processedSDISpecs.setString(r, "keyid1", keyid1);
                            processedSDISpecs.setString(r, "keyid2", keyid2);
                            processedSDISpecs.setString(r, "keyid3", keyid3);
                        }
                        findRow.remove("keyid1");
                        findRow.remove("keyid2");
                        findRow.remove("keyid3");
                        if (processedSpecs.findRow(findRow) < 0) {
                            r = processedSpecs.addRow();
                            processedSpecs.setString(r, "specid", specid);
                            processedSpecs.setString(r, "specversionid", specversionid);
                        }
                        Trace.logDebug("Spec " + specid + ";" + specversionid + " applied to the  SDI: " + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3);
                        continue;
                    }
                    Trace.logDebug("SDISpec applied flag could not be set for SDI " + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3 + " Spec:" + specid + ";" + specversionid);
                }
                if (processedSDISpecs.getRowCount() > 0) {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", sdcid);
                    props.setProperty("keyid1", processedSDIs.getColumnValues("keyid1", ";"));
                    props.setProperty("keyid2", processedSDIs.getColumnValues("keyid2", ";"));
                    props.setProperty("keyid3", processedSDIs.getColumnValues("keyid3", ";"));
                    props.setProperty("specid", processedSpecs.getColumnValues("specid", ";"));
                    props.setProperty("specversionid", processedSpecs.getColumnValues("specversionid", ";"));
                    props.setProperty("postspecapply", "Y");
                    this.dataEntry(props, true, false);
                }
            }
        }
    }
}

