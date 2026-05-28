/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.spec;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.sdi.AddSDITraceLog;
import com.labvantage.sapphire.util.StringHolder;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddSDISpec
extends BaseAction
implements sapphire.action.AddSDISpec {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid = properties.getProperty("sdcid");
        DataSet sdispecs = new DataSet();
        DataSet rsetSDIs = new DataSet();
        StringHolder rsetidHolder = new StringHolder();
        boolean deleterset = false;
        String rsetid = properties.getProperty("rsetid");
        if (rsetid == null) {
            rsetid = "";
        }
        DAMProcessor dam = null;
        if (rsetid.length() == 0) {
            boolean applylock = properties.getProperty("applylock").equals("Y");
            dam = this.getDAMProcessor();
            if (applylock) {
                if (dam.createLockedRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"), rsetidHolder) == 1) {
                    rsetid = rsetidHolder.value;
                }
            } else if (dam.createRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"), rsetidHolder) == 1) {
                rsetid = rsetidHolder.value;
            }
            deleterset = true;
        }
        if (rsetid.length() > 0) {
            try {
                String isVersionProtectEnabled;
                PropertyList versionprotection;
                this.loadSDISpecs(sdcid, rsetid, sdispecs);
                this.loadRsetSDIS(sdcid, rsetid, rsetSDIs);
                HashMap<String, String> findmap = new HashMap<String, String>();
                DataSet appliedSDISpecs = new DataSet();
                String[] keyid1prop = StringUtil.split(properties.getProperty("keyid1"), ";");
                String[] keyid2prop = StringUtil.split(properties.getProperty("keyid2"), ";");
                String[] keyid3prop = StringUtil.split(properties.getProperty("keyid3"), ";");
                String[] specidprop = StringUtil.split(properties.getProperty("specid"), ";");
                String[] specversionidprop = StringUtil.split(properties.getProperty("specversionid"), ";");
                String[] specappliedflagprop = StringUtil.split(properties.getProperty("applyspec"), ";");
                String[] oosgeneratingflag = StringUtil.split(properties.getProperty("oosgeneratingflag"), ";");
                String[] autoapplyflag = StringUtil.split(properties.getProperty("autoapplyflag"), ";");
                String[] saveversionascurrent = StringUtil.split(properties.getProperty("savespecversionascurrent"), ";");
                PropertyList paramListPolicy = this.getConfigurationProcessor().getPolicy("ParamListPolicy", "Sapphire Custom");
                if (paramListPolicy != null && (versionprotection = paramListPolicy.getPropertyListNotNull("expireddataprotection")) != null && versionprotection.size() > 0 && (isVersionProtectEnabled = versionprotection.getProperty("SpecSDC".toLowerCase(), "")).equalsIgnoreCase("Y")) {
                    this.validateSpecExpiry(properties);
                }
                String specsql = this.connectionInfo.isOracle() ? "INSERT INTO sdispec ( tracelogid, sdcid, keyid1, keyid2, keyid3, specid, specversionid, usersequence, decheckflag, createby, createdt, createtool, modby, moddt, modtool, appliedflag, oosgeneratingflag, autoapplyflag ) values ( ?, '" + sdcid + "', ?, ?, ?, ?, ?, (select nvl( max( usersequence ) + 1, 1) from sdispec where sdcid='" + sdcid + "' and keyid1 = ? and keyid2 = ? and keyid3 = ?), 'Y', ?, ?, '" + "AddSDISpec" + "', ?, ?, '" + "AddSDISpec" + "', ?, ?, ? )" : "INSERT INTO sdispec ( tracelogid, sdcid, keyid1, keyid2, keyid3, specid, specversionid, usersequence, decheckflag, createby, createdt, createtool, modby, moddt, modtool, appliedflag, oosgeneratingflag, autoapplyflag ) select ?, '" + sdcid + "', ?, ?, ?, ?, ?, isnull( max( usersequence ) + 1, 1), 'Y', ?, ?, '" + "AddSDISpec" + "', ?, ?, '" + "AddSDISpec" + "', ?, ?, ? from sdispec where sdcid='" + sdcid + "' and keyid1 = ? and keyid2 = ? and keyid3 = ?";
                String specrulesql = "INSERT INTO sdispecrule ( sdcid, keyid1, keyid2, keyid3, specid, specversionid, ruleno, usersequence, ruledesc ) select '" + sdcid + "', ?, ?, ?, specid, specversionid, ruleno, usersequence, ruledesc from specrule where specid=? and specversionid=?";
                try {
                    String reason = properties.getProperty("auditreason");
                    String activity = properties.getProperty("auditactivity");
                    String signedFlag = properties.getProperty("auditsignedflag");
                    String auditdt = properties.getProperty("auditdt");
                    String tracelogid = properties.getProperty("tracelogid", "").trim();
                    if (reason.length() > 0 && tracelogid.length() == 0) {
                        this.logger.info("Generate the tracelog record");
                        PropertyList tracelogprops = new PropertyList();
                        tracelogprops.setProperty("sdcid", sdcid);
                        tracelogprops.setProperty("description", "Added specification");
                        tracelogprops.setProperty("auditreason", reason);
                        tracelogprops.setProperty("auditactivity", activity);
                        tracelogprops.setProperty("auditsignedflag", signedFlag);
                        tracelogprops.setProperty("auditdt", auditdt);
                        ActionProcessor ap = this.getActionProcessor();
                        ap.processActionClass(AddSDITraceLog.class.getName(), tracelogprops);
                        tracelogid = tracelogprops.getProperty("tracelogid");
                        properties.setProperty("tracelogid", tracelogid);
                    }
                    PreparedStatement insertspec = this.database.prepareStatement("spec", specsql);
                    PreparedStatement specdetails = this.database.prepareStatement("specdetails", "select oosgeneratingflag, specusetype from spec where specid = ? and specversionid = ?");
                    PreparedStatement insertspecrules = this.database.prepareStatement("specrule", specrulesql);
                    StringBuffer resolvedSpecVersionIdBuffer = new StringBuffer();
                    for (int s = 0; s < specidprop.length; ++s) {
                        String specid = specidprop[s];
                        String specversionid = specversionidprop.length == 0 || specversionidprop.length < specidprop.length || specversionidprop[s].length() == 0 ? "1" : specversionidprop[s];
                        String oosgeneratingSpec = oosgeneratingflag.length == 0 || oosgeneratingflag.length < specidprop.length || oosgeneratingflag[s].length() == 0 ? "" : oosgeneratingflag[s];
                        String autoApplyFlag = autoapplyflag.length == 0 || autoapplyflag.length < specidprop.length || autoapplyflag[s].length() == 0 ? "" : autoapplyflag[s];
                        String applySpecFlag = specappliedflagprop.length == 0 || specappliedflagprop.length < specidprop.length || specappliedflagprop[s].length() == 0 ? "" : specappliedflagprop[s];
                        String saveVersionAsCurrent = saveversionascurrent.length == 0 || saveversionascurrent.length < specidprop.length || saveversionascurrent[s].length() == 0 ? "N" : saveversionascurrent[s];
                        DataSet processedSDIs = new DataSet();
                        String resolvedCurrentVersion = "";
                        boolean saveAsCurrent = "Y".equalsIgnoreCase(saveVersionAsCurrent);
                        if (saveAsCurrent && "Y".equalsIgnoreCase(applySpecFlag)) {
                            throw new SapphireException("INVALID_PROPERTY", this.getTranslationProcessor().translate("Both \"savespecversionascurrent\" and \"applyspec\" are passed as \"Y\" for Spec") + " \"" + specid + "\"");
                        }
                        if (saveAsCurrent && !"C".equalsIgnoreCase(specversionid) && (s >= specversionidprop.length || specversionidprop[s].length() == 0)) {
                            specversionid = "C";
                        }
                        if ("C".equalsIgnoreCase(specversionid)) {
                            String sql = "SELECT specversionid FROM spec WHERE specid=? and ( versionstatus='P' or versionstatus='C' ) order by versionstatus, cast (specversionid as numeric) desc";
                            this.database.createPreparedResultSet("CurrentVersion", sql, new Object[]{specid});
                            if (this.database.getNext("CurrentVersion")) {
                                resolvedCurrentVersion = this.database.getString("CurrentVersion", "specversionid");
                                if (!saveAsCurrent) {
                                    specversionid = resolvedCurrentVersion;
                                }
                            }
                        }
                        resolvedSpecVersionIdBuffer.append(";").append(specversionid);
                        insertspec.setString(5, specid);
                        insertspec.setString(6, specversionid);
                        insertspecrules.setString(4, specid);
                        insertspecrules.setString(5, specversionid);
                        if (tracelogid == null) {
                            insertspec.setNull(1, 12);
                        } else {
                            insertspec.setString(1, tracelogid);
                        }
                        findmap.put("specid", specid);
                        findmap.put("specversionid", specversionid);
                        int findrow = sdispecs.findRow(findmap);
                        if (findrow < 0) {
                            boolean applySpec;
                            String specuseType = "";
                            if (oosgeneratingSpec.equals("") || "".equals(applySpecFlag)) {
                                specdetails.setString(1, specid);
                                specdetails.setString(2, "C".equalsIgnoreCase(specversionid) ? resolvedCurrentVersion : specversionid);
                                DataSet dsSpec = new DataSet(specdetails.executeQuery());
                                oosgeneratingSpec = dsSpec.getValue(0, "oosgeneratingflag", "N");
                                specuseType = dsSpec.getValue(0, "specusetype");
                            }
                            if ("".equals(applySpecFlag) && ("Y".equalsIgnoreCase(oosgeneratingSpec) || !"Customer".equalsIgnoreCase(specuseType))) {
                                applySpecFlag = "Y";
                            }
                            if ((applySpec = "Y".equalsIgnoreCase(applySpecFlag)) && saveAsCurrent) {
                                applySpec = false;
                            }
                            if (autoApplyFlag.equals("")) {
                                autoApplyFlag = "Y".equalsIgnoreCase(oosgeneratingSpec) || !"Customer".equalsIgnoreCase(specuseType) ? "Y" : "N";
                            }
                            this.database.createPreparedResultSet("SELECT paramlistid, paramlistversionid, variantid FROM specparamlist WHERE specid=? and specversionid=? order by usersequence", new Object[]{specid, specversionid});
                            String paramlistid = "";
                            String paramlistversionid = "";
                            String variantid = "";
                            while (this.database.getNext()) {
                                paramlistid = paramlistid + ";" + this.database.getString("paramlistid");
                                paramlistversionid = paramlistversionid + ";" + this.database.getString("paramlistversionid");
                                variantid = variantid + ";" + this.database.getString("variantid");
                            }
                            if (!paramlistid.equals("")) {
                                SDCProcessor sdcProcessor = this.getSDCProcessor();
                                String keycolid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
                                String keycolid2 = sdcProcessor.getProperty(sdcid, "keycolid2");
                                String keycolid3 = sdcProcessor.getProperty(sdcid, "keycolid3");
                                String table = sdcProcessor.getProperty(sdcid, "tableid");
                                StringBuffer sql = new StringBuffer("SELECT ");
                                SafeSQL safeSQL = new SafeSQL();
                                sql.append(table).append(".").append(keycolid1).append(", ");
                                if (StringUtil.getLen(keycolid2) > 0L) {
                                    sql.append(table).append(".").append(keycolid2).append(", ");
                                }
                                if (StringUtil.getLen(keycolid3) > 0L) {
                                    sql.append(table).append(".").append(keycolid3).append(", ");
                                }
                                sql.append(table).append(".templateflag");
                                sql.append(" FROM ").append(table).append(", rsetitems");
                                sql.append(" WHERE  rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(" ");
                                sql.append(" AND rsetitems.keyid1 = ");
                                sql.append(table).append(".").append(keycolid1);
                                if (StringUtil.getLen(keycolid2) > 0L) {
                                    sql.append(" AND rsetitems.keyid2 = ");
                                    sql.append(table).append(".").append(keycolid2);
                                }
                                if (StringUtil.getLen(keycolid3) > 0L) {
                                    sql.append(" AND rsetitems.keyid3 = ");
                                    sql.append(table).append(".").append(keycolid3);
                                }
                                sql.append(" AND rsetitems.sdcid = ").append(safeSQL.addVar(sdcid)).append("");
                                sql.append(" ORDER BY ").append(table).append(".templateflag");
                                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("gettemplateflag", sql.toString(), safeSQL.getValues());
                                this.logger.info("AddSDISpec: templateflag for all the primary sdis " + ds.toString());
                                ArrayList<DataSet> groupdDs = ds.getGroupedDataSets("templateflag");
                                Iterator<DataSet> iter = groupdDs.iterator();
                                PropertyList p = new PropertyList();
                                while (iter.hasNext()) {
                                    DataSet temp = iter.next();
                                    if (temp.getRowCount() <= 0) continue;
                                    String templateFlag = StringUtil.getYN(temp.getValue(0, "templateflag"), "N");
                                    p.setProperty("sdcid", sdcid);
                                    p.setProperty("keyid1", temp.getColumnValues(keycolid1, ";"));
                                    p.setProperty("keyid2", temp.getColumnValues(keycolid2, ";"));
                                    p.setProperty("keyid3", temp.getColumnValues(keycolid3, ";"));
                                    p.setProperty("paramlistid", paramlistid.substring(1));
                                    p.setProperty("paramlistversionid", paramlistversionid.substring(1));
                                    p.setProperty("variantid", variantid.substring(1));
                                    p.setProperty("addnewonly", "Y");
                                    p.setProperty("rsetid", rsetid);
                                    p.setProperty("tracelogid", tracelogid);
                                    if (templateFlag.equals("Y")) {
                                        p.setProperty("createworksheet", "N");
                                    }
                                    ActionProcessor ap = this.getActionProcessor();
                                    ap.processAction("AddDataSet", "1", p);
                                }
                            }
                            for (int i = 0; i < keyid1prop.length; ++i) {
                                String keyid1 = keyid1prop[i];
                                String keyid2 = keyid2prop.length == 0 || keyid2prop.length < keyid1prop.length || keyid2prop[i].length() == 0 ? "(null)" : keyid2prop[i];
                                String keyid3 = keyid3prop.length == 0 || keyid3prop.length < keyid1prop.length || keyid3prop[i].length() == 0 ? "(null)" : keyid3prop[i];
                                HashMap<String, String> findSDI = new HashMap<String, String>();
                                findSDI.put("keyid1", keyid1);
                                findSDI.put("keyid2", keyid2);
                                findSDI.put("keyid3", keyid3);
                                if (processedSDIs.findRow(findSDI) >= 0 || rsetSDIs.findRow(findSDI) <= -1) continue;
                                insertspec.setString(2, keyid1);
                                insertspec.setString(3, keyid2);
                                insertspec.setString(4, keyid3);
                                if (this.connectionInfo.isOracle()) {
                                    insertspec.setString(7, keyid1);
                                    insertspec.setString(8, keyid2);
                                    insertspec.setString(9, keyid3);
                                    insertspec.setString(10, this.connectionInfo.getSysuserId());
                                    insertspec.setTimestamp(11, DateTimeUtil.getNowTimestamp());
                                    insertspec.setString(12, this.connectionInfo.getSysuserId());
                                    insertspec.setTimestamp(13, DateTimeUtil.getNowTimestamp());
                                    insertspec.setString(14, applySpec ? "Y" : "N");
                                    insertspec.setString(15, oosgeneratingSpec);
                                    insertspec.setString(16, autoApplyFlag);
                                } else {
                                    insertspec.setString(7, this.connectionInfo.getSysuserId());
                                    insertspec.setTimestamp(8, DateTimeUtil.getNowTimestamp());
                                    insertspec.setString(9, this.connectionInfo.getSysuserId());
                                    insertspec.setTimestamp(10, DateTimeUtil.getNowTimestamp());
                                    insertspec.setString(11, applySpec ? "Y" : "N");
                                    insertspec.setString(12, oosgeneratingSpec);
                                    insertspec.setString(13, autoApplyFlag);
                                    insertspec.setString(14, keyid1);
                                    insertspec.setString(15, keyid2);
                                    insertspec.setString(16, keyid3);
                                }
                                insertspecrules.setString(1, keyid1);
                                insertspecrules.setString(2, keyid2);
                                insertspecrules.setString(3, keyid3);
                                try {
                                    insertspec.execute();
                                    insertspecrules.execute();
                                }
                                catch (Exception e) {
                                    this.logger.error("Failed to add spec " + specid + " to " + keyid1 + ". Ignoring error: " + e.getMessage(), e);
                                }
                                int r = processedSDIs.addRow();
                                processedSDIs.setString(r, "keyid1", keyid1);
                                processedSDIs.setString(r, "keyid2", keyid2);
                                processedSDIs.setString(r, "keyid3", keyid3);
                            }
                            if (!applySpec) continue;
                            SafeSQL safeSQL = new SafeSQL();
                            StringBuffer sql = new StringBuffer("INSERT INTO sdidataitemspec (sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, ");
                            sql.append(" variantid, dataset, paramid, paramtype, replicateid, specid, specversionid, usersequence, reportflag ) ");
                            sql.append("  ( ");
                            sql.append(" SELECT sdidataitem.sdcid, sdidataitem.keyid1, sdidataitem.keyid2, sdidataitem.keyid3, ");
                            sql.append(" sdidataitem.paramlistid, sdidataitem.paramlistversionid, sdidataitem.variantid, ");
                            sql.append(" sdidataitem.dataset, sdidataitem.paramid, sdidataitem.paramtype, sdidataitem.replicateid, ");
                            sql.append(" ").append(safeSQL.addVar(specid)).append(",").append(safeSQL.addVar(specversionid)).append(", specparamitems.usersequence, specparamitems.reportflag ");
                            sql.append(" FROM sdidataitem, specparamitems, rsetitems ");
                            sql.append(" WHERE  rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(" ");
                            sql.append(" and rsetitems.keyid1 = sdidataitem.keyid1 ");
                            sql.append(" AND rsetitems.keyid2 = sdidataitem.keyid2 ");
                            sql.append(" AND rsetitems.keyid3 = sdidataitem.keyid3 ");
                            sql.append(" AND rsetitems.sdcid = sdidataitem.sdcid ");
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
                            sql.append(" AND specparamitems.specid = ").append(safeSQL.addVar(specid)).append(" ");
                            sql.append(" AND specparamitems.specversionid = ").append(safeSQL.addVar(specversionid)).append(" ");
                            sql.append(" ) ");
                            Trace.logDebug(" sql = " + sql.toString());
                            int updCnt = this.database.executePreparedUpdate(sql.toString(), safeSQL.getValues());
                            if (updCnt <= 0) continue;
                            int r = appliedSDISpecs.addRow();
                            appliedSDISpecs.setString(r, "specid", specid);
                            appliedSDISpecs.setString(r, "specversionid", specversionid);
                            continue;
                        }
                        this.setInfoError("Spec " + specid + " (Ver: " + specversionid + ") already exists on at least one SDI. Skipping this Spec and continuing.");
                    }
                    if (resolvedSpecVersionIdBuffer.length() > 0) {
                        properties.setProperty("specversionid", resolvedSpecVersionIdBuffer.substring(1));
                    }
                    if (appliedSDISpecs.getRowCount() > 0) {
                        PropertyList props = new PropertyList();
                        props.setProperty("sdcid", sdcid);
                        props.setProperty("keyid1", properties.getProperty("keyid1"));
                        props.setProperty("keyid2", properties.getProperty("keyid2"));
                        props.setProperty("keyid3", properties.getProperty("keyid3"));
                        props.setProperty("specid", appliedSDISpecs.getColumnValues("specid", ";"));
                        props.setProperty("specversionid", appliedSDISpecs.getColumnValues("specversionid", ";"));
                        props.setProperty("postaddsdispec", "Y");
                        this.getActionProcessor().processAction("ApplySDISpecs", "1", props);
                    }
                    this.database.closeStatement("spec");
                    this.database.closeStatement("specrule");
                }
                catch (Exception e) {
                    Trace.logError(e.getMessage());
                    throw new SapphireException("ADD_SPEC_FAILED", "Failed to add specification(s) " + specidprop + " version " + specversionidprop + " to " + keyid1prop, e);
                }
                if (deleterset) {
                    dam.clearRSet(rsetid);
                }
                sdispecs.reset();
            }
            catch (SapphireException se) {
                throw new SapphireException("Error adding specs. Exception: " + ErrorUtil.extractMessageFromException(se, ErrorUtil.isUserAdmin(this.getConnectionId())), se);
            }
        }
        throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create RSET whilst selecting datasets");
    }

    private void loadSDISpecs(String sdcid, String rsetid, DataSet sdispecs) throws SapphireException {
        String selectSpecs = "SELECT\tsdispec.keyid1, sdispec.keyid2, sdispec.keyid3, sdispec.specid, sdispec.specversionid FROM\tsdispec, rsetitems WHERE\trsetitems.sdcid = ? AND \t\trsetitems.rsetid = ? AND \t\trsetitems.sdcid = sdispec.sdcid AND \t\trsetitems.keyid1 = sdispec.keyid1 AND \t\trsetitems.keyid2 = sdispec.keyid2 AND \t\trsetitems.keyid3 = sdispec.keyid3 ";
        this.database.createPreparedResultSet("SelectSpecs", selectSpecs, new Object[]{sdcid, rsetid});
        sdispecs.setResultSet(this.database.getResultSet("SelectSpecs"));
    }

    private void loadRsetSDIS(String sdcid, String rsetid, DataSet rsetSDIs) throws SapphireException {
        String selectRsetItems = "SELECT\tkeyid1, keyid2, keyid3 FROM rsetitems WHERE\trsetitems.sdcid = ? AND rsetitems.rsetid = ?";
        this.database.createPreparedResultSet("SelectRsetSDIs", selectRsetItems, new Object[]{sdcid, rsetid});
        rsetSDIs.setResultSet(this.database.getResultSet("SelectRsetSDIs"));
    }

    private void validateSpecExpiry(PropertyList properties) throws SapphireException {
        StringBuffer message = new StringBuffer();
        String[] specidprop = StringUtil.split(properties.getProperty("specid"), ";");
        String[] specversionidprop = StringUtil.split(properties.getProperty("specversionid"), ";");
        for (int sp = 0; sp < specidprop.length; ++sp) {
            String sql;
            String specid = specidprop[sp];
            String specversionid = specversionidprop[sp];
            if ("C".equalsIgnoreCase(specversionid)) {
                sql = "SELECT specversionid FROM spec WHERE specid=? and ( versionstatus='P' or versionstatus='C' ) order by versionstatus, cast (specversionid as numeric) desc";
                this.database.createPreparedResultSet("CurrentSpecVersion", sql, new Object[]{specid});
                if (this.database.getNext("CurrentSpecVersion")) {
                    specversionid = this.database.getString("CurrentSpecVersion", "specversionid");
                    continue;
                }
                if (message.length() > 0) {
                    message.append(", ").append("<br>");
                }
                message.append("Specification ").append("-").append(specid).append(";").append(specversionid).append(" is Expired");
                continue;
            }
            sql = "SELECT specid FROM spec WHERE specid = ? and SPECVERSIONID= ? and ( versionstatus='E' ) order by versionstatus ";
            this.database.createPreparedResultSet("ExpiredVersion", sql, new Object[]{specid, specversionid});
            DataSet ds = new DataSet(this.database.getResultSet("ExpiredVersion"));
            if (ds.size() <= 0) continue;
            for (int i = 0; i < ds.size(); ++i) {
                if (message.length() > 0) {
                    message.append(", ").append("<br>");
                }
                message.append("Specification ").append("-").append(specid).append(";").append(specversionid).append(" is Expired");
            }
        }
        if (message.length() > 0) {
            throw new SapphireException("Failed to add dataSet due to expired ParamList.", message.toString());
        }
    }
}

