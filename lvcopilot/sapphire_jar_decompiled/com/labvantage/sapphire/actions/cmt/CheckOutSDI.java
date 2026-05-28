/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.cmt;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.cmt.SDISnapshot;
import com.labvantage.sapphire.cmt.SnapshotFactory;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class CheckOutSDI
extends BaseAction
implements sapphire.action.CheckOutSDI {
    public static final String ID = "CheckOutSDI";
    public static final String VERSION = "1";

    @Override
    public void processAction(PropertyList actionProps) throws SapphireException {
        String sdcid = actionProps.getProperty("sdcid").trim();
        String keyid1 = StringUtil.replaceAll(actionProps.getProperty("keyid1").trim(), "%3B", ";");
        String keyid2 = StringUtil.replaceAll(actionProps.getProperty("keyid2").trim(), "%3B", ";");
        String keyid3 = StringUtil.replaceAll(actionProps.getProperty("keyid3").trim(), "%3B", ";");
        String propertytreenodeid = actionProps.getProperty("propertytreenodeid").trim();
        String mode = actionProps.getProperty("mode", "");
        if (sdcid.length() == 0 || keyid1.length() == 0) {
            throw new SapphireException(ID, "VALIDATION", this.getTranslationProcessor().translate("Missing action input SDI"));
        }
        String changecontrolflag = this.getSDCProcessor().getProperty(sdcid, "changecontrolledflag");
        if (!"Y".equals(changecontrolflag) && !"T".equals(changecontrolflag)) {
            Trace.logWarn(ID, this.getTranslationProcessor().translate("SDI not checked Out. The SDC is not under change control.") + " [" + sdcid + "]");
            return;
        }
        if (!"PropertyTree".equals(sdcid) && ("T".equals(changecontrolflag) || "SDC".equals(sdcid) || "LV_Worksheet".equals(sdcid) || "Product".equals(sdcid)) || "SpecSDC".equals(sdcid) || "LV_SamplingPlan".equals(sdcid) || "LV_ChildSamplePlan".equals(sdcid) || "LV_Calendar".equals(sdcid) || "LV_MessageType".equals(sdcid)) {
            String valsql;
            int keycols = Integer.parseInt(this.getSDCProcessor().getProperty(sdcid, "keycolumns"));
            String tableid = this.getSDCProcessor().getProperty(sdcid, "tableid");
            String keycolid1 = this.getSDCProcessor().getProperty(sdcid, "keycolid1");
            String keycolid2 = this.getSDCProcessor().getProperty(sdcid, "keycolid2");
            String keycolid3 = this.getSDCProcessor().getProperty(sdcid, "keycolid3");
            String rsetid = this.getDAMProcessor().createRSet(sdcid, keyid1, keyid2, keyid3);
            String sql = "select count(r.keyid1)";
            sql = sql + " from rsetitems r, " + tableid + " t";
            sql = sql + " where r.sdcid = '" + sdcid + "'";
            sql = sql + " and r.keyid1 = t." + keycolid1;
            sql = sql + (keycols > 1 ? " and r.keyid2 = t." + keycolid2 : "");
            sql = sql + (keycols > 2 ? " and r.keyid3 = t." + keycolid3 : "");
            sql = sql + " and r.rsetid = ?";
            if ("T".equals(changecontrolflag)) {
                boolean hasNoneTemplate;
                valsql = sql + " and (t.templateflag = 'N' or t.templateflag is null)";
                boolean bl = hasNoneTemplate = this.database.getPreparedCount(valsql, new String[]{rsetid}) > 0;
                if (hasNoneTemplate) {
                    throw new SapphireException(ID, "VALIDATION", this.getTranslationProcessor().translate("The SDC change control is Template Only. Only Template SDI is allowed to be checked out"));
                }
            }
            if ("SDC".equals(sdcid) && (";" + keyid1 + ";").indexOf(";SDC;") > -1) {
                throw new SapphireException(ID, "VALIDATION", this.getTranslationProcessor().translate("The SDC SDI of SDC SDC is not allowed to be checked out."));
            }
            if ("LV_Worksheet".equals(sdcid)) {
                valsql = "select r.keyid1, r.keyid2 from rsetitems r, worksheet t where r.sdcid='LV_Worksheet' and r.keyid1=t.worksheetid and r.keyid2=t.worksheetversionid and r.rsetid=? and ( coalesce( t.templateprivacyflag, 'O' ) != 'O' )";
                DataSet dataset = this.getQueryProcessor().getPreparedSqlDataSet(valsql, (Object[])new String[]{rsetid});
                keyid1 = dataset.getColumnValues("keyid1", ";");
                keyid2 = dataset.getColumnValues("keyid2", ";");
            } else if ("Product".equals(sdcid)) {
                valsql = "select r.keyid1, r.keyid2 from rsetitems r, s_product t where r.sdcid='Product' and r.keyid1=t.s_productid and r.keyid2=t.s_productversionid and r.rsetid=? and ( coalesce( t.formulationiterationflag, 'N' ) = 'N' )";
                DataSet dataset = this.getQueryProcessor().getPreparedSqlDataSet(valsql, (Object[])new String[]{rsetid});
                keyid1 = dataset.getColumnValues("keyid1", ";");
                keyid2 = dataset.getColumnValues("keyid2", ";");
            } else if ("SpecSDC".equals(sdcid)) {
                valsql = "select r.keyid1, r.keyid2 from rsetitems r, spec t where r.sdcid='SpecSDC' and r.keyid1=t.specid and r.keyid2=t.specversionid and r.rsetid=? and ( coalesce( t.embeddedflag, 'N' ) != 'Y' )";
                DataSet dataset = this.getQueryProcessor().getPreparedSqlDataSet(valsql, (Object[])new String[]{rsetid});
                keyid1 = dataset.getColumnValues("keyid1", ";");
                keyid2 = dataset.getColumnValues("keyid2", ";");
            } else if ("LV_MessageType".equals(sdcid)) {
                valsql = "select r.keyid1, r.keyid2 from rsetitems r, messagetype t where r.sdcid='LV_MessageType' and r.keyid1=t.messagetypeid and r.rsetid=? and ( coalesce( t.definitionsdcid, '' ) != 'LV_DataFileDef' )";
                DataSet dataset = this.getQueryProcessor().getPreparedSqlDataSet(valsql, (Object[])new String[]{rsetid});
                keyid1 = dataset.getColumnValues("keyid1", ";");
                keyid2 = dataset.getColumnValues("keyid2", ";");
            } else if ("LV_SamplingPlan".equals(sdcid)) {
                valsql = "select r.keyid1, r.keyid2 from rsetitems r, s_samplingplan t where r.sdcid='LV_SamplingPlan' and r.keyid1=t.s_samplingplanid and r.keyid2=t.s_samplingplanversionid and r.rsetid=? and ( coalesce( t.embeddedflag, 'N' ) != 'Y' )";
                DataSet dataset = this.getQueryProcessor().getPreparedSqlDataSet(valsql, (Object[])new String[]{rsetid});
                keyid1 = dataset.getColumnValues("keyid1", ";");
                keyid2 = dataset.getColumnValues("keyid2", ";");
            } else if ("LV_ChildSamplePlan".equals(sdcid)) {
                valsql = "select r.keyid1, r.keyid2 from rsetitems r, s_childsampleplan t where r.sdcid='LV_ChildSamplePlan' and r.keyid1=t.s_childsampleplanid and r.keyid2=t.s_childsampleplanversionid and r.rsetid=? and ( coalesce( t.embeddedflag, 'N' ) != 'Y' )";
                DataSet dataset = this.getQueryProcessor().getPreparedSqlDataSet(valsql, (Object[])new String[]{rsetid});
                keyid1 = dataset.getColumnValues("keyid1", ";");
                keyid2 = dataset.getColumnValues("keyid2", ";");
            } else if ("LV_Calendar".equals(sdcid)) {
                valsql = "select r.keyid1 from rsetitems r, calendar t where r.sdcid='LV_Calendar' and r.keyid1=t.calendarid and r.rsetid=? and ( coalesce( t.sharedflag, 'N' ) != 'N' )";
                DataSet dataset = this.getQueryProcessor().getPreparedSqlDataSet(valsql, (Object[])new String[]{rsetid});
                keyid1 = dataset.getColumnValues("keyid1", ";");
            }
            this.getDAMProcessor().clearRSet(rsetid);
            if (keyid1.length() == 0) {
                return;
            }
        }
        if ("(system)".equals(this.connectionInfo.getSysuserId())) {
            throw new SapphireException(ID, "VALIDATION", "CheckOutSDI action does not support running under \"(system)\" user. If running action from a Task, please set the property \"processassysuserid\" to a valid application user.");
        }
        ArrayList<String> list = new ArrayList<String>();
        int keycolumns = Integer.parseInt(this.getSDCProcessor().getProperty(sdcid, "keycolumns"));
        DataSet inputDS = new DataSet();
        inputDS.addColumn("keyid1", 0);
        inputDS.addColumn("keyid2", 0);
        inputDS.addColumn("keyid3", 0);
        inputDS.addColumn("propertytreenodeid", 0);
        inputDS.addColumnValues("keyid1", 0, keyid1, ";");
        if (keycolumns > 1) {
            inputDS.addColumnValues("keyid2", 0, keyid2, ";");
            inputDS.padColumn("keyid2");
            if (keycolumns > 2) {
                inputDS.addColumnValues("keyid3", 0, keyid3, ";");
                inputDS.padColumn("keyid3");
            }
        }
        if ("PropertyTree".equals(sdcid)) {
            inputDS.addColumnValues("propertytreenodeid", 0, propertytreenodeid, ";");
        }
        String tableid = this.getSDCProcessor().getProperty(sdcid, "tableid");
        String keycolid1 = this.getSDCProcessor().getProperty(sdcid, "keycolid1");
        String keycolid2 = this.getSDCProcessor().getProperty(sdcid, "keycolid2");
        String keycolid3 = this.getSDCProcessor().getProperty(sdcid, "keycolid3");
        String rsetid = this.getDAMProcessor().createRSet(sdcid, inputDS.getColumnValues("keyid1", ";"), inputDS.getColumnValues("keyid2", ";"), inputDS.getColumnValues("keyid3", ";"));
        String sql = "select " + tableid + "." + keycolid1 + " keyid1";
        if (keycolumns > 1) {
            sql = sql + ", " + tableid + "." + keycolid2 + " keyid2";
            if (keycolumns > 2) {
                sql = sql + ", " + tableid + "." + keycolid3 + " keyid3";
            }
        }
        sql = sql + " from " + tableid + ", rsetitems";
        sql = sql + " where " + tableid + "." + keycolid1 + " = rsetitems.keyid1";
        if (keycolumns > 1) {
            sql = sql + " and " + tableid + "." + keycolid2 + " = rsetitems.keyid2";
            if (keycolumns > 2) {
                sql = sql + " and " + tableid + "." + keycolid3 + " = rsetitems.keyid3";
            }
        }
        sql = sql + " and rsetitems.sdcid = ?";
        sql = sql + " and rsetitems.rsetid = ?";
        DataSet existingDataDS = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{sdcid, rsetid});
        this.getDAMProcessor().clearRSet(rsetid);
        HashMap<String, String> filter = new HashMap<String, String>();
        for (int i = 0; i < inputDS.getRowCount(); ++i) {
            String checkoutkeyid1 = inputDS.getString(i, "keyid1");
            String checkoutkeyid2 = inputDS.getString(i, "keyid2");
            String checkoutkeyid3 = inputDS.getString(i, "keyid3");
            filter.clear();
            filter.put("keyid1", checkoutkeyid1);
            if (keycolumns > 1) {
                filter.put("keyid2", checkoutkeyid2);
                if (keycolumns > 2) {
                    filter.put("keyid3", checkoutkeyid3);
                }
            }
            if (existingDataDS.findRow(filter) != -1) {
                list.add(this.checkOutSDI(actionProps, sdcid, checkoutkeyid1, checkoutkeyid2, checkoutkeyid3, inputDS.getString(i, "propertytreenodeid"), mode));
                continue;
            }
            Trace.logWarn("CheckOutSDI Action", "SDI does not exist, bypassing Check out (" + sdcid + ", " + checkoutkeyid1 + (keycolumns > 1 ? ", " + checkoutkeyid2 : "") + (keycolumns > 2 ? ", " + checkoutkeyid3 : "") + ")");
        }
        actionProps.setProperty("newkeyid1", OpalUtil.toDelimitedString(list, ";"));
        actionProps.setProperty("changelogid", actionProps.getProperty("newkeyid1"));
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private String checkOutSDI(PropertyList actionProps, String sdcid, String keyid1, String keyid2, String keyid3, String propertytreenodeid, String mode) throws SapphireException {
        DataSet ds;
        String primarychangelogid = "";
        String departmentid = actionProps.getProperty("departmentid").trim();
        int sdckeycolumns = Integer.parseInt(this.getSDCProcessor().getProperty(sdcid, "keycolumns"));
        String changelogid = null;
        String checkedoutbyuserid = null;
        String checkedoutbydepartmentid = null;
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select changelogid, checkedoutbyuserid, checkedoutbydepartmentid, propertytreenodeid from changelog where linksdcid = " + safeSQL.addVar(sdcid) + " and linkkeyid1 = " + safeSQL.addVar(keyid1) + " and changelogstatus = '" + "Checked Out" + "'";
        if (OpalUtil.isNotEmpty(keyid2)) {
            sql = sql + " and linkkeyid2 = " + safeSQL.addVar(keyid2);
        }
        if (OpalUtil.isNotEmpty(keyid3)) {
            sql = sql + " and linkkeyid3 = " + safeSQL.addVar(keyid3);
        }
        if ((ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues())) == null) throw new SapphireException(ID, "VALIDATION", this.getTranslationProcessor().translate("Failed fetching change log data"));
        if (ds.size() > 0) {
            if ("PropertyTree".equals(sdcid)) {
                DataSet fullChangeLogs;
                HashMap<String, String> filterMap;
                if (propertytreenodeid == null) throw new SapphireException("PropertyTreeNode", "VALIDATION", "Property Tree Node id is mandatory for PropertyTree checkout.");
                if (propertytreenodeid.length() == 0) {
                    throw new SapphireException("PropertyTreeNode", "VALIDATION", "Property Tree Node id is mandatory for PropertyTree checkout.");
                }
                if ("__FULL".equals(propertytreenodeid)) {
                    filterMap = new HashMap<String, String>();
                    filterMap.put("propertytreenodeid", "__FULL");
                    fullChangeLogs = ds.getFilteredDataSet(filterMap);
                    if (fullChangeLogs.getRowCount() > 0) {
                        changelogid = fullChangeLogs.getString(0, "changelogid");
                        checkedoutbyuserid = fullChangeLogs.getString(0, "checkedoutbyuserid", "");
                        checkedoutbydepartmentid = fullChangeLogs.getString(0, "checkedoutbydepartmentid", "");
                    } else {
                        DataSet nonFullChangeLogs = ds.getFilteredDataSet(filterMap, true);
                        if (nonFullChangeLogs.getRowCount() > 0) {
                            changelogid = nonFullChangeLogs.getColumnValues("changelogid", ";");
                            checkedoutbyuserid = nonFullChangeLogs.getColumnValues("checkedoutbyuserid", ";");
                            checkedoutbydepartmentid = nonFullChangeLogs.getColumnValues("checkedoutbydepartmentid", ";");
                        }
                    }
                } else {
                    filterMap = new HashMap();
                    filterMap.put("propertytreenodeid", "__FULL");
                    fullChangeLogs = ds.getFilteredDataSet(filterMap);
                    if (fullChangeLogs.getRowCount() > 0) {
                        changelogid = fullChangeLogs.getString(0, "changelogid");
                        checkedoutbyuserid = fullChangeLogs.getString(0, "checkedoutbyuserid", "");
                        checkedoutbydepartmentid = fullChangeLogs.getString(0, "checkedoutbydepartmentid", "");
                    } else {
                        filterMap.clear();
                        filterMap.put("propertytreenodeid", propertytreenodeid);
                        DataSet nodeChangeLogs = ds.getFilteredDataSet(filterMap);
                        if (nodeChangeLogs.getRowCount() > 0) {
                            changelogid = nodeChangeLogs.getString(0, "changelogid");
                            checkedoutbyuserid = nodeChangeLogs.getString(0, "checkedoutbyuserid", "");
                            checkedoutbydepartmentid = nodeChangeLogs.getString(0, "checkedoutbydepartmentid", "");
                        }
                    }
                }
            } else {
                changelogid = ds.getString(0, "changelogid");
                checkedoutbyuserid = ds.getString(0, "checkedoutbyuserid", "");
                checkedoutbydepartmentid = ds.getString(0, "checkedoutbydepartmentid", "");
            }
        }
        if (OpalUtil.isNotEmpty(checkedoutbyuserid)) {
            if (this.getConnectionProcessor().getSapphireConnection().getSysuserId().equals(checkedoutbyuserid)) {
                primarychangelogid = changelogid;
            } else if (OpalUtil.isNotEmpty(checkedoutbydepartmentid)) {
                if (!(";" + this.getConnectionProcessor().getSapphireConnection().getDepartmentList() + ";").contains(";" + checkedoutbydepartmentid + ";")) throw new SapphireException(ID, "VALIDATION", this.getTranslationProcessor().translate((propertytreenodeid != null && propertytreenodeid.length() > 0 ? "Node" : "SDI") + " already checked out to department:") + checkedoutbydepartmentid + " (" + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3 + ")");
                primarychangelogid = changelogid;
            } else {
                TranslationProcessor translationProcessor = this.getTranslationProcessor();
                throw new SapphireException(ID, "VALIDATION", translationProcessor.translate((propertytreenodeid != null && propertytreenodeid.length() > 0 ? "Node" : "SDI") + " already checked out to user:") + checkedoutbyuserid + " (" + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3 + ")");
            }
        }
        if (!OpalUtil.isEmpty(primarychangelogid)) return primarychangelogid;
        SDISnapshot snapshot = null;
        if (!"add".equalsIgnoreCase(mode) && (snapshot = "PropertyTree".equals(sdcid) ? new SnapshotFactory(this.getConnectionId()).generatePropertyTreeSnapshot(keyid1, propertytreenodeid) : new SnapshotFactory(this.getConnectionId()).generateSDISnapshot(sdcid, keyid1, keyid2, keyid3)) == null) {
            throw new SapphireException(ID, "VALIDATION", this.getTranslationProcessor().translate("Failed to generate snapshot"));
        }
        PropertyList props = new PropertyList();
        props.putAll(actionProps);
        props.setProperty("sdcid", "LV_ChangeLog");
        props.setProperty("copies", VERSION);
        props.setProperty("changerequestid", actionProps.getProperty("changerequestid"));
        props.setProperty("linksdcid", sdcid);
        props.setProperty("linkkeyid1", keyid1);
        props.setProperty("linkkeyid2", sdckeycolumns > 1 ? keyid2 : "(null)");
        props.setProperty("linkkeyid3", sdckeycolumns > 2 ? keyid3 : "(null)");
        props.setProperty("propertytreenodeid", propertytreenodeid);
        props.setProperty("changelogtype", "Primary");
        if (!"add".equalsIgnoreCase(mode) && snapshot != null) {
            props.setProperty("originalsnapshot", snapshot.toXML());
        }
        props.setProperty("changelogreason", actionProps.getProperty("changelogreason"));
        props.setProperty("changelogstatus", "Checked Out");
        props.setProperty("checkedoutdt", "n");
        props.setProperty("checkedoutbyuserid", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
        if (OpalUtil.isNotEmpty(departmentid)) {
            props.setProperty("checkedoutbydepartmentid", departmentid);
        }
        props.setProperty("auditactivity", actionProps.getProperty("auditactivity"));
        props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag"));
        props.setProperty("auditreason", actionProps.getProperty("auditreason"));
        props.setProperty("auditdt", actionProps.getProperty("auditdt"));
        this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
        return props.getProperty("newkeyid1");
    }
}

