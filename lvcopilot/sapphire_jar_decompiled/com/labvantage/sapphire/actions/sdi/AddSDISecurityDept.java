/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.sdi.AddSDITraceLog;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddSDISecurityDept
extends BaseAction
implements sapphire.action.AddSDISecurityDept {
    public static final String PROPERTY_OPERATIONID_DEFAULT = "list";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        block25: {
            String[] operations;
            String sdcid = properties.getProperty("sdcid");
            String props_operationid = properties.getProperty("operationid", "").trim();
            if (StringUtil.getLen(sdcid) == 0L) {
                throw new SapphireException("Missing mandatory action input: sdcid");
            }
            for (String s : operations = StringUtil.split(props_operationid, ";")) {
                if (StringUtil.getLen(s) == 0L) {
                    s = PROPERTY_OPERATIONID_DEFAULT;
                }
                if (this.database.getPreparedCount("select count(sdcid) from sdcoperation where sdcid = ? and operationid = ?", new Object[]{sdcid, s}) != 0) continue;
                throw new SapphireException("SDC " + sdcid + " is missing SDC Operation \"" + s + "\"");
            }
            boolean deleterset = false;
            String rsetid = properties.getProperty("rsetid");
            if (rsetid == null) {
                rsetid = "";
            }
            DAMProcessor dam = null;
            if (rsetid.length() == 0) {
                boolean applylock = "Y".equals(properties.getProperty("applylock"));
                dam = this.getDAMProcessor();
                rsetid = applylock ? dam.createLockedRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3")) : dam.createRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"));
                deleterset = true;
            }
            if (rsetid.length() > 0) {
                try {
                    DataSet sdisecuritydepts = this.loadSDISecurityDepartments(sdcid, rsetid);
                    HashMap findmap = new HashMap();
                    Object[] departmentprop = StringUtil.split(properties.getProperty("departmentid"), ";");
                    String[] keyid1prop = StringUtil.split(properties.getProperty("keyid1"), ";");
                    String[] keyid2prop = StringUtil.split(properties.getProperty("keyid2"), ";");
                    String[] keyid3prop = StringUtil.split(properties.getProperty("keyid3"), ";");
                    if (StringUtil.getLen(props_operationid) == 0L) {
                        props_operationid = StringUtil.repeat(PROPERTY_OPERATIONID_DEFAULT, departmentprop.length, ";");
                    }
                    Object[] operationidprop = StringUtil.split(props_operationid, ";");
                    String insertsql = this.connectionInfo.isOracle() ? "INSERT INTO sdisecuritydepartment ( sdcid, keyid1, keyid2, keyid3, securitydepartment, operationid, usersequence, createby, createdt, createtool, tracelogid) values ( '" + sdcid + "', ?, ?, ?, ?, ?, (select nvl( max( usersequence ) + 1, 1) from sdisecuritydepartment where sdcid='" + sdcid + "' and keyid1 = ? and keyid2 = ? and keyid3 = ?), ?, ?, 'AddSDISecurityDept', ? )" : "INSERT INTO sdisecuritydepartment ( sdcid, keyid1, keyid2, keyid3, securitydepartment, operationid, usersequence, createby, createdt, createtool, tracelogid) select '" + sdcid + "', ?, ?, ?, ?, ?, isnull( max( usersequence ) + 1, 1), ?, ?, 'AddSDISecurityDept', ? from sdisecuritydepartment where sdcid='" + sdcid + "' and keyid1 = ? and keyid2 = ? and keyid3 = ?";
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
                            tracelogprops.setProperty("keyid1", properties.getProperty("keyid1"));
                            tracelogprops.setProperty("keyid2", properties.getProperty("keyid2"));
                            tracelogprops.setProperty("keyid3", properties.getProperty("keyid3"));
                            tracelogprops.setProperty("description", "Added security department");
                            tracelogprops.setProperty("auditreason", reason);
                            tracelogprops.setProperty("auditactivity", activity);
                            tracelogprops.setProperty("auditsignedflag", signedFlag);
                            tracelogprops.setProperty("auditdt", auditdt);
                            this.getActionProcessor().processActionClass(AddSDITraceLog.class.getName(), tracelogprops);
                            tracelogid = tracelogprops.getProperty("tracelogid");
                            properties.setProperty("tracelogid", tracelogid);
                        }
                        PreparedStatement insertstmt = this.database.prepareStatement("securitydepartment", insertsql);
                        if ("Y".equals(properties.getProperty("propsmatch", "N"))) {
                            if (departmentprop.length != keyid1prop.length) {
                                throw new SapphireException("In PROPSMATCH mode, the number of SDIs must match the number of Departments");
                            }
                            for (int i = 0; i < keyid1prop.length; ++i) {
                                String keyid1 = keyid1prop[i];
                                String keyid2 = keyid2prop.length == 0 || keyid2prop.length < keyid1prop.length || keyid2prop[i].length() == 0 ? "(null)" : keyid2prop[i];
                                String keyid3 = keyid3prop.length == 0 || keyid3prop.length < keyid1prop.length || keyid3prop[i].length() == 0 ? "(null)" : keyid3prop[i];
                                Object departmentid = departmentprop[i];
                                Object operationid = operationidprop[i];
                                this.insertSecurityDepartment(insertstmt, sdcid, keyid1, keyid2, keyid3, (String)departmentid, (String)operationid, sdisecuritydepts, tracelogid);
                            }
                        } else {
                            for (int s = 0; s < departmentprop.length; ++s) {
                                Object departmentid = departmentprop[s];
                                Object operationid = operationidprop[s];
                                for (int i = 0; i < keyid1prop.length; ++i) {
                                    String keyid1 = keyid1prop[i];
                                    String keyid2 = keyid2prop.length == 0 || keyid2prop.length < keyid1prop.length || keyid2prop[i].length() == 0 ? "(null)" : keyid2prop[i];
                                    String keyid3 = keyid3prop.length == 0 || keyid3prop.length < keyid1prop.length || keyid3prop[i].length() == 0 ? "(null)" : keyid3prop[i];
                                    this.insertSecurityDepartment(insertstmt, sdcid, keyid1, keyid2, keyid3, (String)departmentid, (String)operationid, sdisecuritydepts, tracelogid);
                                }
                            }
                        }
                        break block25;
                    }
                    catch (Exception e) {
                        Trace.logError(e.getMessage());
                        throw new SapphireException("ADD_SPEC_FAILED", "Failed to add departments(s) " + Arrays.toString(departmentprop) + " operationid " + Arrays.toString(operationidprop) + " to " + keyid1prop, e);
                    }
                    finally {
                        this.database.closeStatement("securitydepartment");
                        sdisecuritydepts.reset();
                    }
                }
                catch (SapphireException se) {
                    throw new SapphireException("Error adding departments. Exception: " + ErrorUtil.extractMessageFromException(se, ErrorUtil.isUserAdmin(this.getConnectionId())), se);
                }
                finally {
                    if (deleterset) {
                        dam.clearRSet(rsetid);
                    }
                }
            }
            throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create RSET while selecting datasets");
        }
    }

    private void insertSecurityDepartment(PreparedStatement insertstmt, String sdcid, String keyid1, String keyid2, String keyid3, String departmentid, String operationid, DataSet sdisecuritydepts, String tracelogid) {
        HashMap<String, String> findmap = new HashMap<String, String>();
        findmap.put("sdcid", sdcid);
        findmap.put("keyid1", keyid1);
        if (StringUtil.getLen(keyid2) > 0L && !"(null)".equals(keyid2)) {
            findmap.put("keyid2", keyid2);
        }
        if (StringUtil.getLen(keyid3) > 0L && !"(null)".equals(keyid3)) {
            findmap.put("keyid3", keyid3);
        }
        findmap.put("securitydepartment", departmentid);
        findmap.put("operationid", operationid);
        int findrow = sdisecuritydepts.findRow(findmap);
        if (findrow < 0) {
            try {
                insertstmt.setString(1, keyid1);
                insertstmt.setString(2, keyid2);
                insertstmt.setString(3, keyid3);
                insertstmt.setString(4, departmentid);
                insertstmt.setString(5, operationid);
                if (this.connectionInfo.isOracle()) {
                    insertstmt.setString(6, keyid1);
                    insertstmt.setString(7, keyid2);
                    insertstmt.setString(8, keyid3);
                    insertstmt.setString(9, this.connectionInfo.getSysuserId());
                    insertstmt.setTimestamp(10, DateTimeUtil.getNowTimestamp());
                    insertstmt.setString(11, tracelogid);
                } else {
                    insertstmt.setString(6, this.connectionInfo.getSysuserId());
                    insertstmt.setTimestamp(7, DateTimeUtil.getNowTimestamp());
                    insertstmt.setString(8, tracelogid);
                    insertstmt.setString(9, keyid1);
                    insertstmt.setString(10, keyid2);
                    insertstmt.setString(11, keyid3);
                }
                insertstmt.execute();
            }
            catch (Exception e) {
                this.logger.error("Failed to add department " + departmentid + " operation " + operationid + " to " + keyid1 + ". Ignoring error: " + e.getMessage(), e);
            }
        }
    }

    private DataSet loadSDISecurityDepartments(String sdcid, String rsetid) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        String selectSecurityDepartment = "SELECT\tsdisecuritydepartment.sdcid, sdisecuritydepartment.keyid1, sdisecuritydepartment.keyid2, sdisecuritydepartment.keyid3, sdisecuritydepartment.securitydepartment, sdisecuritydepartment.operationid FROM\tsdisecuritydepartment, rsetitems WHERE\trsetitems.sdcid = " + safeSQL.addVar(sdcid) + " AND \t\trsetitems.rsetid = " + safeSQL.addVar(rsetid) + " AND \t\trsetitems.sdcid = sdisecuritydepartment.sdcid AND \t\trsetitems.keyid1 = sdisecuritydepartment.keyid1 AND \t\trsetitems.keyid2 = sdisecuritydepartment.keyid2 AND \t\trsetitems.keyid3 = sdisecuritydepartment.keyid3 ";
        return this.getQueryProcessor().getPreparedSqlDataSet(selectSecurityDepartment, safeSQL.getValues());
    }
}

