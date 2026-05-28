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
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddSDISecuritySet
extends BaseAction
implements sapphire.action.AddSDISecuritySet {
    public static final String PROPERTY_OPERATIONID_DEFAULT = "list";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        block27: {
            Object[] securitysetprop;
            String[] operations;
            String sdcid = properties.getProperty("sdcid");
            String props_operationid = properties.getProperty("operationid", "").trim();
            String securitySetIds = properties.getProperty("securityset");
            if (StringUtil.getLen(sdcid) == 0L) {
                throw new SapphireException("Missing mandatory action input: sdcid");
            }
            if (StringUtil.getLen(securitySetIds) == 0L) {
                throw new SapphireException("Missing mandatory action input: SecuritySet");
            }
            for (String s : operations = StringUtil.split(props_operationid, ";")) {
                if (StringUtil.getLen(s) == 0L) {
                    s = PROPERTY_OPERATIONID_DEFAULT;
                }
                if (this.database.getPreparedCount("select count(sdcid) from sdcoperation where sdcid = ? and operationid = ?", new Object[]{sdcid, s}) != 0) continue;
                throw new SapphireException("SDC " + sdcid + " is missing SDC Operation \"" + s + "\"");
            }
            for (String string : securitysetprop = StringUtil.split(properties.getProperty("securityset"), ";")) {
                if (this.database.getPreparedCount("select count(1) from securitysetsdc where  securitysetid = ? and securitysetsdcid = ?", new Object[]{string, sdcid}) != 0) continue;
                throw new SapphireException("SecuritySet \"" + string + "\" does not include \" SDC \"" + sdcid);
            }
            boolean deleterset = false;
            String rsetid = properties.getProperty("rsetid");
            if (rsetid == null) {
                rsetid = "";
            }
            DAMProcessor dam = null;
            if (rsetid.length() == 0) {
                boolean bl = "Y".equals(properties.getProperty("applylock"));
                dam = this.getDAMProcessor();
                rsetid = bl ? dam.createLockedRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3")) : dam.createRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"));
                deleterset = true;
            }
            if (rsetid.length() > 0) {
                try {
                    DataSet dataSet = new DataSet();
                    this.loadSDISecuritySets(sdcid, rsetid, dataSet);
                    Object[] keyid1prop = StringUtil.split(properties.getProperty("keyid1"), ";");
                    String[] keyid2prop = StringUtil.split(properties.getProperty("keyid2"), ";");
                    String[] keyid3prop = StringUtil.split(properties.getProperty("keyid3"), ";");
                    if (StringUtil.getLen(props_operationid) == 0L) {
                        props_operationid = StringUtil.repeat(PROPERTY_OPERATIONID_DEFAULT, securitysetprop.length, ";");
                    }
                    String[] operationidprop = StringUtil.split(props_operationid, ";");
                    String insertsql = this.connectionInfo.isOracle() ? "INSERT INTO sdisecurityset ( sdcid, keyid1, keyid2, keyid3, securityset, operationid, usersequence, createby, createdt, createtool, tracelogid)values ( '" + sdcid + "', ?, ?, ?, ?, ?, (select nvl( max( usersequence ) + 1, 1) from sdisecurityset where sdcid='" + sdcid + "' and keyid1 = ? and keyid2 = ? and keyid3 = ?), ?, ?, '" + "AddSDISecuritySet" + "', ? )" : "INSERT INTO sdisecurityset ( sdcid, keyid1, keyid2, keyid3, securityset, operationid, usersequence, createby, createdt, createtool, tracelogid) select '" + sdcid + "', ?, ?, ?, ?, ?, isnull( max( usersequence ) + 1, 1), ?, ?, '" + "AddSDISecuritySet" + "', ? from sdisecurityset where sdcid='" + sdcid + "' and keyid1 = ? and keyid2 = ? and keyid3 = ?";
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
                            tracelogprops.setProperty("description", "Added security set");
                            tracelogprops.setProperty("auditreason", reason);
                            tracelogprops.setProperty("auditactivity", activity);
                            tracelogprops.setProperty("auditsignedflag", signedFlag);
                            tracelogprops.setProperty("auditdt", auditdt);
                            ActionProcessor ap = this.getActionProcessor();
                            ap.processActionClass(AddSDITraceLog.class.getName(), tracelogprops);
                            tracelogid = tracelogprops.getProperty("tracelogid");
                            properties.setProperty("tracelogid", tracelogid);
                        }
                        PreparedStatement insertstmt = this.database.prepareStatement("securityset", insertsql);
                        if ("Y".equals(properties.getProperty("propsmatch", "N"))) {
                            if (securitysetprop.length != keyid1prop.length) {
                                throw new SapphireException("In PROPSMATCH mode, the number of SDIs must match the number of Security Sets");
                            }
                            for (int i = 0; i < keyid1prop.length; ++i) {
                                Object keyid1 = keyid1prop[i];
                                String operationid = operationidprop[i];
                                String keyid2 = keyid2prop.length == 0 || keyid2prop.length < keyid1prop.length || keyid2prop[i].length() == 0 ? "(null)" : keyid2prop[i];
                                String keyid3 = keyid3prop.length == 0 || keyid3prop.length < keyid1prop.length || keyid3prop[i].length() == 0 ? "(null)" : keyid3prop[i];
                                this.insertSecuritySet(insertstmt, sdcid, (String)keyid1, keyid2, keyid3, (String)securitysetprop[i], operationid, dataSet, tracelogid);
                            }
                        } else {
                            for (int s = 0; s < securitysetprop.length; ++s) {
                                Object securitysetid = securitysetprop[s];
                                String operationid = operationidprop[s];
                                for (int i = 0; i < keyid1prop.length; ++i) {
                                    Object keyid1 = keyid1prop[i];
                                    String keyid2 = keyid2prop.length == 0 || keyid2prop.length < keyid1prop.length || keyid2prop[i].length() == 0 ? "(null)" : keyid2prop[i];
                                    String keyid3 = keyid3prop.length == 0 || keyid3prop.length < keyid1prop.length || keyid3prop[i].length() == 0 ? "(null)" : keyid3prop[i];
                                    this.insertSecuritySet(insertstmt, sdcid, (String)keyid1, keyid2, keyid3, (String)securitysetid, operationid, dataSet, tracelogid);
                                }
                            }
                        }
                        break block27;
                    }
                    catch (Exception e) {
                        Trace.logError(e.getMessage());
                        throw new SapphireException("ADD_SPEC_FAILED", "Failed to add securityset(s) " + Arrays.toString(securitysetprop) + " to " + Arrays.toString(keyid1prop), e);
                    }
                    finally {
                        this.database.closeStatement("securityset");
                        dataSet.reset();
                    }
                }
                catch (SapphireException sapphireException) {
                    throw new SapphireException("Error adding securitysets. Exception: " + ErrorUtil.extractMessageFromException(sapphireException, ErrorUtil.isUserAdmin(this.getConnectionId())), sapphireException);
                }
                finally {
                    if (deleterset) {
                        dam.clearRSet(rsetid);
                    }
                }
            }
            throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create RSET whilst selecting datasets");
        }
    }

    private void insertSecuritySet(PreparedStatement insertstmt, String sdcid, String keyid1, String keyid2, String keyid3, String securityset, String operationid, DataSet sdisecuritysets, String tracelogid) {
        if (StringUtil.getLen(securityset) > 0L) {
            HashMap<String, String> findmap = new HashMap<String, String>();
            findmap.put("sdcid", sdcid);
            findmap.put("keyid1", keyid1);
            if (StringUtil.getLen(keyid2) > 0L && !"(null)".equals(keyid2)) {
                findmap.put("keyid2", keyid2);
            }
            if (StringUtil.getLen(keyid3) > 0L && !"(null)".equals(keyid3)) {
                findmap.put("keyid3", keyid3);
            }
            if (operationid == null || operationid.length() == 0) {
                operationid = PROPERTY_OPERATIONID_DEFAULT;
            }
            findmap.put("securityset", securityset);
            findmap.put("operationid", operationid);
            int findrow = sdisecuritysets.findRow(findmap);
            if (findrow < 0) {
                try {
                    insertstmt.setString(1, keyid1);
                    insertstmt.setString(2, keyid2);
                    insertstmt.setString(3, keyid3);
                    insertstmt.setString(4, securityset);
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
                    this.logger.error("Failed to add security set " + securityset + " to " + keyid1 + ". Ignoring error: " + e.getMessage(), e);
                }
            }
        }
    }

    private void loadSDISecuritySets(String sdcid, String rsetid, DataSet sdisecuritysets) throws SapphireException {
        String selectSecuritySet = "SELECT\tsdisecurityset.sdcid, sdisecurityset.keyid1, sdisecurityset.keyid2, sdisecurityset.keyid3, sdisecurityset.securityset, sdisecurityset.operationid FROM\tsdisecurityset, rsetitems WHERE\trsetitems.sdcid = ? AND \t\trsetitems.rsetid = ? AND \t\trsetitems.sdcid = sdisecurityset.sdcid AND \t\trsetitems.keyid1 = sdisecurityset.keyid1 AND \t\trsetitems.keyid2 = sdisecurityset.keyid2 AND \t\trsetitems.keyid3 = sdisecurityset.keyid3 ";
        this.database.createPreparedResultSet("SelectSecuritySets", selectSecuritySet, new Object[]{sdcid, rsetid});
        sdisecuritysets.setResultSet(this.database.getResultSet("SelectSecuritySets"));
    }
}

