/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.actions.sdidata.BaseSDIDataAction;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class CopyDataSet
extends BaseSDIDataAction
implements sapphire.action.CopyDataSet {
    public static String LABVANTAGE_CVS_ID = "$Revision: 83464 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String rsetid = properties.getProperty("rsetid");
        String sdcid = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1", "(null)");
        String keyid2 = properties.getProperty("keyid2", "(null)");
        String keyid3 = properties.getProperty("keyid3", "(null)");
        String paramlistid = properties.getProperty("paramlistid");
        String paramlistversionid = properties.getProperty("paramlistversionid");
        String variantid = properties.getProperty("variantid");
        String dataset = properties.getProperty("dataset");
        String[] arrKeyid1 = StringUtil.split(keyid1, ";");
        String[] arrKeyid2 = StringUtil.split(keyid2, ";");
        String[] arrKeyid3 = StringUtil.split(keyid3, ";");
        String[] arrParamlistid = StringUtil.split(paramlistid, ";");
        String[] arrParamlistversionid = StringUtil.split(paramlistversionid, ";");
        String[] arrVariantid = StringUtil.split(variantid, ";");
        String[] arrDataset = StringUtil.split(dataset, ";");
        PropertyList datasetSDCProps = this.getSDCProcessor().getPropertyList("DataSet");
        boolean isDSecEnabled = datasetSDCProps.getProperty("accesscontrolledflag", "").equals("D") || datasetSDCProps.getProperty("accesscontrolledflag", "").equals("B");
        DataSet sdidataRelation = new DataSet(this.connectionInfo);
        for (int index = 0; index < arrKeyid1.length; ++index) {
            keyid1 = arrKeyid1[index];
            keyid2 = arrKeyid2[index];
            keyid3 = arrKeyid3[index];
            boolean deleterset = false;
            boolean setWapStatus = true;
            if (rsetid.length() == 0) {
                rsetid = this.getRSet(properties.getProperty("sdcid"), keyid1, keyid2, keyid3, properties.getProperty("applylock").equals("Y"));
                deleterset = true;
            }
            if (rsetid.length() > 0) {
                paramlistid = arrParamlistid[index];
                paramlistversionid = arrParamlistversionid[index];
                variantid = arrVariantid[index];
                dataset = arrDataset[index];
                SafeSQL safeSQL = new SafeSQL();
                String sql = "SELECT ds.dataset, ds.sdidataid, ds.workareadepartmentid, sw.workitemid, sw.workitemversionid FROM sdidata ds LEFT OUTER JOIN sdiworkitem sw  ON ds.sdcid = sw.sdcid AND ds.keyid1 = sw.keyid1 AND ds.keyid2 = sw.keyid2 AND ds.keyid3 = sw.keyid3 AND ds.sourceworkitemid = sw.workitemid AND ds.sourceworkiteminstance = sw.workiteminstance  WHERE ds.sdcid=" + safeSQL.addVar(sdcid) + " and ds.keyid1=" + safeSQL.addVar(keyid1) + " and ds.keyid2=" + safeSQL.addVar(keyid2) + " and ds.keyid3=" + safeSQL.addVar(keyid3) + " and ds.paramlistid=" + safeSQL.addVar(paramlistid) + " and ds.paramlistversionid=" + safeSQL.addVar(paramlistversionid) + " and ds.variantid=" + safeSQL.addVar(variantid) + " order by ds.dataset desc";
                this.database.createPreparedResultSet(sql, safeSQL.getValues());
                PropertyList sdcProps = this.getSDCProcessor().getPropertyList(sdcid);
                DataSet allDs = new DataSet(this.database.getResultSet());
                if (allDs.getRowCount() > 0) {
                    String[] pseudokeys;
                    int sourceDSRow = allDs.findRow("dataset", dataset);
                    boolean template = false;
                    if ("Sample".equals(sdcid) || sdcProps.getProperty("plannableflag").equals("Y")) {
                        String tableId = sdcProps.getProperty("tableid");
                        String keyColId1 = sdcProps.getProperty("keycolid1");
                        String keyColId2 = sdcProps.getProperty("keycolid2");
                        String keyColId3 = sdcProps.getProperty("keycolid3");
                        boolean templatable = sdcProps.getProperty("templatableflag").equals("Y");
                        safeSQL.reset();
                        StringBuffer sqlSDIWapStatus = new StringBuffer();
                        sqlSDIWapStatus.append("SELECT wapstatus ").append(templatable ? ", templateflag" : "").append(" FROM ").append(tableId).append(" WHERE ").append(keyColId1).append(" = ").append(safeSQL.addVar(keyid1));
                        if (keyColId2.length() > 0) {
                            sqlSDIWapStatus.append(" AND ").append(keyColId2).append("=").append(safeSQL.addVar(keyid2));
                        }
                        if (keyColId3.length() > 0) {
                            sqlSDIWapStatus.append(" AND ").append(keyColId3).append("=").append(safeSQL.addVar(keyid3));
                        }
                        this.database.createPreparedResultSet("sdiwapstatus", sqlSDIWapStatus.toString(), safeSQL.getValues());
                        DataSet sdiDS = new DataSet(this.database.getResultSet("sdiwapstatus"));
                        if (sdiDS.getRowCount() > 0) {
                            template = "Y".equalsIgnoreCase(sdiDS.getValue(0, "templateflag"));
                            String sdiWapStatus = sdiDS.getValue(0, "wapstatus");
                            if (sdiWapStatus.length() > 0 && !"Never".equalsIgnoreCase(sdiWapStatus) || template) {
                                setWapStatus = false;
                            }
                        }
                    }
                    String tracelogid = this.getTracelogid(sdcid, "Copied dataset", properties.getProperty("auditreason"), properties.getProperty("auditactivity", ""), properties.getProperty("auditsignedflag", "N"), properties.getProperty("auditdt"));
                    int nextdataset = allDs.getInt(0, "dataset") + 1;
                    String sdidataid = allDs.getValue(0, "sdidataid");
                    String workitemid = allDs.getValue(sourceDSRow, "workitemid");
                    String sdidataWorkArea = allDs.getValue(sourceDSRow, "workareadepartmentid");
                    String newds_assignDept = "";
                    String newds_assignAnalyst = "";
                    boolean autoAssignWorkArea = false;
                    boolean autoAssignAnalyst = false;
                    if (!template) {
                        if (workitemid != null && workitemid.length() > 0) {
                            String workitemversionid = allDs.getValue(sourceDSRow, "workitemversionid");
                            this.database.createPreparedResultSet("workitemrule", "SELECT createactivityrule,autoassignrule,autoassignanalystid FROM workitem WHERE  workitemid = ? AND workitemversionid=?", new String[]{workitemid, workitemversionid});
                            DataSet workitemDS = new DataSet(this.database.getResultSet("workitemrule"));
                            this.database.createPreparedResultSet("workitemitemrule", "SELECT autoassignrule, autoassignanalystid FROM workitemitem WHERE  workitemid = ? AND workitemversionid=? AND sdcid = 'ParamList' and keyid1 = ? AND keyid2 = ? AND keyid3 = ?", new String[]{workitemid, workitemversionid, paramlistid, paramlistversionid, variantid});
                            DataSet workitemitemDS = new DataSet(this.database.getResultSet("workitemitemrule"));
                            if (workitemitemDS.getRowCount() > 0) {
                                autoAssignWorkArea = "Workarea".equalsIgnoreCase(workitemitemDS.getValue(0, "autoassignrule"));
                                autoAssignAnalyst = "Analyst".equalsIgnoreCase(workitemitemDS.getValue(0, "autoassignrule"));
                                if (autoAssignAnalyst) {
                                    newds_assignAnalyst = workitemitemDS.getValue(0, "autoassignanalystid");
                                }
                                if (autoAssignWorkArea && sdidataWorkArea != null && sdidataWorkArea.length() > 0) {
                                    newds_assignDept = sdidataWorkArea;
                                }
                            }
                            if (workitemDS.getRowCount() > 0) {
                                if (setWapStatus) {
                                    setWapStatus = "On Demand By DataSet".equalsIgnoreCase(workitemDS.getValue(0, "createactivityrule"));
                                }
                                if (!autoAssignWorkArea && !autoAssignAnalyst) {
                                    autoAssignWorkArea = "Workarea".equalsIgnoreCase(workitemDS.getValue(0, "autoassignrule"));
                                    autoAssignAnalyst = "Analyst".equalsIgnoreCase(workitemDS.getValue(0, "autoassignrule"));
                                    if (autoAssignAnalyst) {
                                        newds_assignAnalyst = workitemDS.getValue(0, "autoassignanalystid");
                                    }
                                    if (autoAssignWorkArea && sdidataWorkArea != null && sdidataWorkArea.length() > 0) {
                                        newds_assignDept = sdidataWorkArea;
                                    }
                                }
                            }
                        } else {
                            this.database.createPreparedResultSet("plrule", "SELECT createactivityrule, autoassignrule, autoassignanalystid FROM paramlist WHERE  paramlistid = ? AND paramlistversionid = ? AND variantid = ?", new String[]{paramlistid, paramlistversionid, variantid});
                            DataSet paramlistDS = new DataSet(this.database.getResultSet("plrule"));
                            if (paramlistDS.getRowCount() > 0) {
                                if (setWapStatus) {
                                    setWapStatus = paramlistDS.getRowCount() > 0 && "On Demand".equalsIgnoreCase(paramlistDS.getValue(0, "createactivityrule"));
                                }
                                autoAssignWorkArea = "Workarea".equalsIgnoreCase(paramlistDS.getValue(0, "autoassignrule"));
                                autoAssignAnalyst = "Analyst".equalsIgnoreCase(paramlistDS.getValue(0, "autoassignrule"));
                                if (autoAssignAnalyst) {
                                    newds_assignAnalyst = paramlistDS.getValue(0, "autoassignanalystid");
                                }
                                if (autoAssignWorkArea && sdidataWorkArea != null && sdidataWorkArea.length() > 0) {
                                    newds_assignDept = sdidataWorkArea;
                                }
                            }
                        }
                    }
                    if ((pseudokeys = ((DBUtil)this.database).getUUIDList(1)) == null || pseudokeys.length != 1) {
                        throw new SapphireException("Unable to generate sdidataid.");
                    }
                    SafeSQL safeSQLWhere = new SafeSQL();
                    String where = " WHERE sdcid=" + safeSQLWhere.addVar(sdcid) + " and keyid1=" + safeSQLWhere.addVar(keyid1) + " and keyid2=" + safeSQLWhere.addVar(keyid2) + " and keyid3=" + safeSQLWhere.addVar(keyid3) + " and paramlistid=" + safeSQLWhere.addVar(paramlistid) + " and paramlistversionid=" + safeSQLWhere.addVar(paramlistversionid) + " and variantid=" + safeSQLWhere.addVar(variantid) + " and dataset=" + safeSQLWhere.addVar(dataset) + ")";
                    String attWhere = " WHERE sdcid='DataSet' AND keyid1='" + sdidataid + "' )";
                    boolean setAssignedAnalyst = false;
                    boolean setAssignedDept = false;
                    if (newds_assignAnalyst != null && newds_assignAnalyst.length() > 0) {
                        setAssignedAnalyst = true;
                    }
                    if (newds_assignDept != null && newds_assignDept.length() > 0) {
                        setAssignedDept = true;
                    }
                    String optionalColumnsParts = "";
                    if (setAssignedAnalyst) {
                        optionalColumnsParts = optionalColumnsParts + "s_assignedanalyst,";
                    }
                    if (setAssignedDept) {
                        optionalColumnsParts = optionalColumnsParts + "s_assigneddepartment,";
                    }
                    if (setWapStatus) {
                        optionalColumnsParts = optionalColumnsParts + "wapstatus,";
                    }
                    if (isDSecEnabled) {
                        sql = "INSERT INTO sdidata (sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset, sdidataid, s_datasetstatus, usersequence, limitruleid, limitruleversionid, modifiableflag,s_cancellableflag, approvalsequenceflag, approvalpassrule, availabilityflag, testingdepartmentid, workareadepartmentid,";
                        sql = sql + optionalColumnsParts;
                        sql = sql + " sourceworkitemid, sourceworkiteminstance, securitydepartment, securityuser, tracelogid, createdt, createby, createtool, moddt, modby, modtool ) ( SELECT sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, " + nextdataset + ", '" + pseudokeys[0] + "', '" + "Initial" + "', usersequence, limitruleid, limitruleversionid, modifiableflag,s_cancellableflag, approvalsequenceflag, approvalpassrule, availabilityflag, testingdepartmentid, workareadepartmentid," + (setAssignedAnalyst ? "'" + newds_assignAnalyst + "'," : "") + (setAssignedDept ? "'" + newds_assignDept + "'," : "") + (setWapStatus ? "'Pending'," : "") + " sourceworkitemid, sourceworkiteminstance, securitydepartment, " + (this.connectionInfo.getSysuserId() != null ? "'" + this.connectionInfo.getSysuserId() + "'" : "securityuser") + ", " + (tracelogid != null ? tracelogid : "NULL") + ", {ts '" + DateTimeUtil.getNowTimestamp() + "'}, '" + this.connectionInfo.getSysuserId() + "', '" + "CopyDataSet" + "', {ts '" + DateTimeUtil.getNowTimestamp() + "'}, '" + this.connectionInfo.getSysuserId() + "', '" + "CopyDataSet" + "'  FROM sdidata" + where;
                    } else {
                        sql = "INSERT INTO sdidata (sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset, sdidataid, s_datasetstatus, usersequence, limitruleid, limitruleversionid, modifiableflag,s_cancellableflag, approvalsequenceflag, approvalpassrule, availabilityflag, testingdepartmentid, workareadepartmentid, ";
                        sql = sql + optionalColumnsParts;
                        sql = sql + " sourceworkitemid, sourceworkiteminstance, tracelogid, createdt, createby, createtool, moddt, modby, modtool ) ( SELECT sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, " + nextdataset + ",  '" + pseudokeys[0] + "', '" + "Initial" + "', usersequence, limitruleid, limitruleversionid, modifiableflag, s_cancellableflag, approvalsequenceflag, approvalpassrule, availabilityflag, testingdepartmentid, workareadepartmentid," + (setAssignedAnalyst ? "'" + newds_assignAnalyst + "'," : "") + (setAssignedDept ? "'" + newds_assignDept + "'," : "") + (setWapStatus ? "'Pending'," : "") + " sourceworkitemid, sourceworkiteminstance, " + (tracelogid != null ? tracelogid : "NULL") + ", {ts '" + DateTimeUtil.getNowTimestamp() + "'}, '" + this.connectionInfo.getSysuserId() + "', '" + "CopyDataSet" + "', {ts '" + DateTimeUtil.getNowTimestamp() + "'}, '" + this.connectionInfo.getSysuserId() + "', '" + "CopyDataSet" + "'  FROM sdidata" + where;
                    }
                    this.database.executePreparedUpdate(sql, safeSQLWhere.getValues());
                    sql = "INSERT INTO sdidataapproval (sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset, approvalstep, roleid, forcepeerflag, mandatoryflag, approvalflag, usersequence, createdt, createby, createtool, moddt, modby, modtool ) ( SELECT sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, " + nextdataset + ", approvalstep, roleid, forcepeerflag, mandatoryflag, 'U', usersequence, {ts '" + DateTimeUtil.getNowTimestamp() + "'}, '" + this.connectionInfo.getSysuserId() + "', '" + "CopyDataSet" + "', {ts '" + DateTimeUtil.getNowTimestamp() + "'}, '" + this.connectionInfo.getSysuserId() + "', '" + "CopyDataSet" + "'  FROM sdidataapproval" + where;
                    this.database.executePreparedUpdate(sql, safeSQLWhere.getValues());
                    sql = "INSERT INTO sdidataitem (sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset, paramid, paramtype, replicateid, aliasid, transformrule, mandatoryflag, datatypes, operatorrule, entrysdcid, entryreftypeid, displayformat, displayunits, defaultvalue, instrumentfieldid, uncertaintyfunction, uncertaintydisplayformat, uncertaintyfunctionupper, uncertaintydisplayformatupper, uncertaintyasymmetricflag, usersequence, calcrule, measurementactionid, releasedflag, transformdeferflag, reportflag, createdt, createby, createtool, moddt, modby, modtool ) ( SELECT sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, " + nextdataset + ", paramid, paramtype, replicateid, aliasid, transformrule, mandatoryflag, datatypes, operatorrule, entrysdcid, entryreftypeid, displayformat, displayunits, defaultvalue, instrumentfieldid, uncertaintyfunction, uncertaintydisplayformat, uncertaintyfunctionupper, uncertaintydisplayformatupper, uncertaintyasymmetricflag, usersequence, calcrule, measurementactionid, 'N', transformdeferflag, reportflag, {ts '" + DateTimeUtil.getNowTimestamp() + "'}, '" + this.connectionInfo.getSysuserId() + "', '" + "CopyDataSet" + "', {ts '" + DateTimeUtil.getNowTimestamp() + "'}, '" + this.connectionInfo.getSysuserId() + "', '" + "CopyDataSet" + "'  FROM sdidataitem" + where;
                    this.database.executePreparedUpdate(sql, safeSQLWhere.getValues());
                    sql = "INSERT INTO sdidataitemlimits (sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset, paramid, paramtype, replicateid, limittypeid, operator, value1, value2, value1num, value2num, usersequence, unitsid, limitfailedactionid ) ( SELECT sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, " + nextdataset + ", paramid, paramtype, replicateid, limittypeid, operator, value1, value2, value1num, value2num, usersequence, unitsid, limitfailedactionid  FROM sdidataitemlimits" + where;
                    this.database.executePreparedUpdate(sql, safeSQLWhere.getValues());
                    sql = "INSERT INTO sdidataitemspec (sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset, paramid, paramtype, replicateid, specid, specversionid, usersequence, waivedflag, reportflag, createdt, createby, createtool, moddt, modby, modtool) ( SELECT sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, " + nextdataset + ", paramid, paramtype, replicateid, specid, specversionid, usersequence, 'N', reportflag, {ts '" + DateTimeUtil.getNowTimestamp() + "'}, '" + this.connectionInfo.getSysuserId() + "', '" + "CopyDataSet" + "', {ts '" + DateTimeUtil.getNowTimestamp() + "'}, '" + this.connectionInfo.getSysuserId() + "', '" + "CopyDataSet" + "'  FROM sdidataitemspec" + where;
                    this.database.executePreparedUpdate(sql, safeSQLWhere.getValues());
                    safeSQL.reset();
                    String selectSDIDataRel = "SELECT relationtype, relationfunction,sourcesdcid,sourcekeyid1,sourcekeyid2,sourcekeyid3,requiredamount,requiredamountunits,requiredamountunitstype,relationinstance FROM sdidatarelation  WHERE sdcid = " + safeSQL.addVar(sdcid) + " and keyid1 = " + safeSQL.addVar(keyid1) + " and keyid2 = " + safeSQL.addVar(keyid2) + " and keyid3 = " + safeSQL.addVar(keyid3) + " AND paramlistid = " + safeSQL.addVar(paramlistid) + " AND paramlistversionid = " + safeSQL.addVar(paramlistversionid) + " AND variantid = " + safeSQL.addVar(variantid) + "  AND dataset =" + safeSQL.addVar(dataset);
                    this.database.createPreparedResultSet("getsdidatarelation", selectSDIDataRel, safeSQL.getValues());
                    DataSet dsSDIRel = new DataSet(this.database.getResultSet("getsdidatarelation"));
                    this.database.closeResultSet("getsdidatarelation");
                    for (int d = 0; d < dsSDIRel.getRowCount(); ++d) {
                        int newRow = sdidataRelation.addRow();
                        sdidataRelation.setString(newRow, "sdcid", sdcid);
                        sdidataRelation.setString(newRow, "keyid1", keyid1);
                        sdidataRelation.setString(newRow, "keyid2", keyid2);
                        sdidataRelation.setString(newRow, "keyid3", keyid3);
                        sdidataRelation.setString(newRow, "paramlistid", paramlistid);
                        sdidataRelation.setString(newRow, "paramlistversionid", paramlistversionid);
                        sdidataRelation.setString(newRow, "variantid", variantid);
                        sdidataRelation.setNumber(newRow, "dataset", nextdataset);
                        sdidataRelation.setString(newRow, "relationtype", dsSDIRel.getValue(d, "relationtype"));
                        sdidataRelation.setString(newRow, "relationfunction", dsSDIRel.getValue(d, "relationfunction"));
                        sdidataRelation.setString(newRow, "sourcesdcid", dsSDIRel.getValue(d, "sourcesdcid"));
                        sdidataRelation.setString(newRow, "sourcekeyid1", dsSDIRel.getValue(d, "sourcekeyid1"));
                        sdidataRelation.setString(newRow, "sourcekeyid2", dsSDIRel.getValue(d, "sourcekeyid2"));
                        sdidataRelation.setString(newRow, "sourcekeyid3", dsSDIRel.getValue(d, "sourcekeyid3"));
                        sdidataRelation.setString(newRow, "requiredamount", dsSDIRel.getValue(d, "requiredamount"));
                        sdidataRelation.setString(newRow, "requiredamountunits", dsSDIRel.getValue(d, "requiredamountunits"));
                        sdidataRelation.setString(newRow, "requiredamountunitstype", dsSDIRel.getValue(d, "requiredamountunitstype"));
                        sdidataRelation.setString(newRow, "relationinstance", dsSDIRel.getValue(d, "relationinstance"));
                    }
                    safeSQL.reset();
                    StringBuffer sqlB = new StringBuffer();
                    sqlB.append("select ds_attribute.attributeid, ds_attribute.attributesdcid, ds_attribute.attributeinstance, ds_attribute.sourcesdcid, ds_attribute.attributesourcetype, ").append("ds_attribute.sdcid, ds_attribute.keyid1, ds_attribute.keyid2, ds_attribute.keyid3, ").append("ds_attribute.editorstyleid, ds_attribute.datatype, ds_attribute.updateableflag, ds_attribute.hiddenflag, ds_attribute.mandatoryflag, ").append("ds_attribute.editsdcid, ds_attribute.editreftypeid, ds_attribute.usersequence, ").append("coalesce(pl_attribute.defaulttextvalue, attributedef.defaulttextvalue) textvalue, ").append("coalesce(pl_attribute.defaultnumericvalue, attributedef.defaultnumericvalue) numericvalue, ").append("coalesce(pl_attribute.defaultdatevalue, attributedef.defaultdatevalue) datevalue, ").append("coalesce(pl_attribute.defaultclobvalue, attributedef.defaultclobvalue) clobvalue ").append("from sdiattribute ds_attribute ").append("inner join attributedef on ds_attribute.attributeid = attributedef.attributedefid and attributedef.basedonid='DataSet' ").append("inner join sdidata on sdidata.sdidataid = ds_attribute.keyid1 ").append("left outer join sdiattribute pl_attribute on pl_attribute.sdcid = 'ParamList' and sdidata.paramlistid = pl_attribute.keyid1 and sdidata.paramlistversionid = pl_attribute.keyid2 and sdidata.variantid = pl_attribute.keyid3  and pl_attribute.attributeid = ds_attribute.attributeid and pl_attribute.attributeinstance = ds_attribute.attributeinstance ").append("where ds_attribute.keyid1 = ").append(safeSQL.addVar(sdidataid));
                    DataSet attributes = this.getQueryProcessor().getPreparedSqlDataSet(sqlB.toString(), safeSQL.getValues(), true);
                    if (attributes != null && attributes.size() > 0) {
                        attributes.addColumn("modby", 0);
                        attributes.addColumn("createby", 1);
                        attributes.addColumn("createtool", 0);
                        attributes.addColumn("modtool", 0);
                        attributes.addColumn("createdt", 2);
                        attributes.addColumn("moddt", 2);
                        for (int r = 0; r < attributes.getRowCount(); ++r) {
                            attributes.setValue(r, "keyid1", pseudokeys[0]);
                            attributes.setDate(r, "createdt", DateTimeUtil.getNowCalendar());
                            attributes.setValue(r, "createby", this.connectionInfo.getSysuserId());
                            attributes.setValue(r, "createtool", "CopyDataSet");
                            attributes.setDate(r, "moddt", DateTimeUtil.getNowCalendar());
                            attributes.setValue(r, "modby", this.connectionInfo.getSysuserId());
                            attributes.setValue(r, "modtool", "CopyDataSet");
                        }
                        DataSetUtil.insert(this.database, attributes, "sdiattribute");
                    }
                    safeSQL.reset();
                    String selectSDIWIWI = "SELECT sw.workitemitemid, sw.workiteminstance FROM sdiworkitemitem sw, sdidata ds WHERE sw.sdcid = ds.sdcid AND sw.keyid1 = ds.keyid1 AND sw.keyid2 = ds.keyid2  AND sw.keyid3 = ds.keyid3 AND sw.workitemid = ds.sourceworkitemid AND sw.workiteminstance = ds.sourceworkiteminstance  AND sw.itemsdcid = 'ParamList' AND sw.itemkeyid1 = ds.paramlistid AND sw.itemkeyid2 = ds.paramlistversionid AND sw.itemkeyid3 = ds.variantid  AND ds.sdcid = " + safeSQL.addVar(sdcid) + " AND ds.keyid1 = " + safeSQL.addVar(keyid1) + " AND ds.keyid2 = " + safeSQL.addVar(keyid2) + " AND ds.keyid3 = " + safeSQL.addVar(keyid3) + "  AND ds.paramlistid = " + safeSQL.addVar(paramlistid) + " AND ds.paramlistversionid =" + safeSQL.addVar(paramlistversionid) + " AND  ds.variantid = " + safeSQL.addVar(variantid) + " AND ds.dataset = " + safeSQL.addVar(dataset) + "";
                    this.database.createPreparedResultSet("getsdiworkitemitem", selectSDIWIWI, safeSQL.getValues());
                    DataSet dsSDIWIWI = new DataSet(this.database.getResultSet("getsdiworkitemitem"));
                    this.database.closeResultSet("getsdiworkitemitem");
                    if (dsSDIWIWI.getRowCount() > 0) {
                        String[] wiitemids = StringUtil.split(dsSDIWIWI.getColumnValues("workitemitemid", ";"), ";");
                        int maxwiwi = 0;
                        String preDotPart = "";
                        String nxtWorkItemItemId = "";
                        String workItemInstance = dsSDIWIWI.getValue(0, "workiteminstance", "");
                        for (int wi = 0; wi < wiitemids.length; ++wi) {
                            int dotIndex = wiitemids[wi].indexOf(".");
                            if (dotIndex <= -1) continue;
                            preDotPart = wiitemids[wi].substring(0, dotIndex);
                            String postDotPart = wiitemids[wi].substring(dotIndex + 1);
                            if (maxwiwi >= Integer.valueOf(postDotPart)) continue;
                            maxwiwi = Integer.valueOf(postDotPart);
                        }
                        if (maxwiwi == 0) {
                            String workItemItemId = wiitemids[0];
                            nxtWorkItemItemId = workItemItemId + "." + 2;
                        } else {
                            nxtWorkItemItemId = preDotPart + "." + (maxwiwi + 1);
                        }
                        safeSQL.reset();
                        sql = "INSERT INTO sdiworkitemitem (sdcid, keyid1, keyid2, keyid3, workitemid, workiteminstance, workitemitemid,  itemsdcid, itemkeyid1, itemkeyid2, itemkeyid3, iteminstance, mandatoryflag, completeflag ) (SELECT sdcid, keyid1, keyid2, keyid3, workitemid, workiteminstance, " + safeSQL.addVar(nxtWorkItemItemId) + ", itemsdcid, itemkeyid1, itemkeyid2, itemkeyid3, " + safeSQL.addVar(nextdataset) + ", mandatoryflag, 'N'  FROM sdiworkitemitem  WHERE sdcid = " + safeSQL.addVar(sdcid) + " and keyid1 = " + safeSQL.addVar(keyid1) + " and keyid2 = " + safeSQL.addVar(keyid2) + " and keyid3 = " + safeSQL.addVar(keyid3) + " and itemsdcid = 'ParamList' and itemkeyid1 = " + safeSQL.addVar(paramlistid) + " and itemkeyid2 = " + safeSQL.addVar(paramlistversionid) + " and itemkeyid3 = " + safeSQL.addVar(variantid) + " and iteminstance = " + safeSQL.addVar(dataset) + " AND workiteminstance = " + safeSQL.addVar(workItemInstance) + ")";
                        this.database.executePreparedUpdate(sql, safeSQL.getValues());
                    }
                    sql = "SELECT documentid, documentversionid, blockflag FROM sdidata" + where.substring(0, where.length() - 1);
                    DataSet documentDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQLWhere.getValues());
                    if (documentDs.getValue(0, "documentid", "").length() > 0) {
                        safeSQL.reset();
                        sql = "SELECT createworksheetrule FROM paramlist WHERE ";
                        sql = sql + " paramlist.paramlistid = " + safeSQL.addVar(paramlistid);
                        sql = sql + " AND paramlist.paramlistversionid = " + safeSQL.addVar(paramlistversionid);
                        sql = sql + " AND paramlist.variantid = " + safeSQL.addVar(variantid);
                        sql = sql + " AND paramlist.createworksheetrule = 'On Creation'";
                        DataSet wsRule = this.getQueryProcessor().getPreparedSqlDataSet("wsRule", sql, safeSQL.getValues());
                        if (wsRule.getRowCount() > 0) {
                            safeSQL.reset();
                            sql = "SELECT formid, formversionid FROM document WHERE ";
                            sql = sql + " document.documentid = " + safeSQL.addVar(documentDs.getValue(0, "documentid", ""));
                            sql = sql + " AND document.documentversionid = " + safeSQL.addVar(documentDs.getValue(0, "documentversionid", ""));
                            DataSet formid = this.getQueryProcessor().getPreparedSqlDataSet("formid", sql, safeSQL.getValues());
                            this.logger.info("CopyDataSet: Call CreateWorksheet with form " + formid.toString());
                            PropertyList createWSProps = new PropertyList();
                            createWSProps.setProperty("sdcid", sdcid);
                            createWSProps.setProperty("keyid1", keyid1);
                            createWSProps.setProperty("keyid2", keyid2);
                            createWSProps.setProperty("keyid3", keyid3);
                            createWSProps.setProperty("paramlistid", paramlistid);
                            createWSProps.setProperty("paramlistversionid", paramlistversionid);
                            createWSProps.setProperty("variantid", variantid);
                            createWSProps.setProperty("dataset", String.valueOf(nextdataset));
                            createWSProps.setProperty("formid", formid.getString(0, "formid", ""));
                            createWSProps.setProperty("formversionid", formid.getString(0, "formversionid", ""));
                            this.getActionProcessor().processAction("CreateWorksheet", "1", createWSProps);
                        }
                    }
                }
                if (!deleterset) continue;
                this.getDAMProcessor().clearRSet(rsetid);
                rsetid = "";
                continue;
            }
            throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create RSET whilst copying datasets");
        }
        if (sdidataRelation.getRowCount() > 0) {
            this.addSDIDataRelation(sdidataRelation);
        }
    }

    private void addSDIDataRelation(DataSet datarelations) throws SapphireException {
        PropertyList dataRelationProps = new PropertyList();
        dataRelationProps.setProperty("sdcid", datarelations.getString(0, "sdcid"));
        dataRelationProps.setProperty("keyid1", datarelations.getColumnValues("keyid1", ";"));
        dataRelationProps.setProperty("keyid2", datarelations.getColumnValues("keyid2", ";"));
        dataRelationProps.setProperty("keyid3", datarelations.getColumnValues("keyid3", ";"));
        dataRelationProps.setProperty("paramlistid", datarelations.getColumnValues("paramlistid", ";"));
        dataRelationProps.setProperty("paramlistversionid", datarelations.getColumnValues("paramlistversionid", ";"));
        dataRelationProps.setProperty("variantid", datarelations.getColumnValues("variantid", ";"));
        dataRelationProps.setProperty("dataset", datarelations.getColumnValues("dataset", ";"));
        dataRelationProps.setProperty("relationtype", datarelations.getColumnValues("relationtype", ";"));
        dataRelationProps.setProperty("relationfunction", datarelations.getColumnValues("relationfunction", ";"));
        dataRelationProps.setProperty("sourcesdcid", datarelations.getColumnValues("sourcesdcid", ";"));
        dataRelationProps.setProperty("sourcekeyid1", datarelations.getColumnValues("sourcekeyid1", ";"));
        dataRelationProps.setProperty("sourcekeyid2", datarelations.getColumnValues("sourcekeyid2", ";"));
        dataRelationProps.setProperty("sourcekeyid3", datarelations.getColumnValues("sourcekeyid3", ";"));
        dataRelationProps.setProperty("requiredamount", datarelations.getColumnValues("requiredamount", ";"));
        dataRelationProps.setProperty("requiredamountunits", datarelations.getColumnValues("requiredamountunits", ";"));
        dataRelationProps.setProperty("requiredamountunitstype", datarelations.getColumnValues("requiredamountunitstype", ";"));
        dataRelationProps.setProperty("relationinstance", datarelations.getColumnValues("relationinstance", ";"));
        this.getActionProcessor().processAction("AddSDIDataRelation", "1", dataRelationProps);
    }
}

