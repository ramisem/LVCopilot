/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.ddt;

import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseAction;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.SDIList;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SDISecurityCheck
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = "$Revision: 68080 $";

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String items = properties.getProperty("keyid1");
        String items2 = properties.getProperty("keyid2");
        String items3 = properties.getProperty("keyid3");
        if (items == null || items.length() <= 0) throw new SapphireException(this.getTranslationProcessor().translate("No sdis provided."));
        String operation = properties.getProperty("operation");
        if (operation == null || operation.length() <= 0) throw new SapphireException(this.getTranslationProcessor().translate("No operation provided."));
        String sdcid = properties.getProperty("sdcid");
        if (sdcid == null || sdcid.length() <= 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("No SDC Id provided."));
        }
        this.checkSDIAccessList(properties, operation, sdcid, items, items2, items3);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void checkIndividual(PropertyList props, String operation, String sdcid, String items, String items2, String items3) throws SapphireException {
        SDCProcessor sdcproc = this.getSDCProcessor();
        HashMap sdcprops = sdcproc.getSDCProperties(sdcid);
        if (props == null) throw new SapphireException(this.getTranslationProcessor().translate("Invalid SDC Id provided."));
        String tableid = sdcprops.get("tableid").toString();
        String keycol1 = sdcprops.get("keycolid1").toString();
        int keyCount = Integer.parseInt(sdcproc.getProperty(sdcid, "keycolumns"));
        String keycol2 = "";
        String keycol3 = "";
        StringBuffer sqlKeyCol = new StringBuffer();
        sqlKeyCol.append("t." + keycol1);
        if (keyCount > 1) {
            keycol2 = sdcproc.getProperty(sdcid, "keycolid2");
            sqlKeyCol.append(", t." + keycol2);
            if (keyCount > 2) {
                keycol3 = sdcproc.getProperty(sdcid, "keycolid3");
                sqlKeyCol.append(", t." + keycol3);
            }
        }
        DataSet data = null;
        QueryProcessor qp = this.getQueryProcessor();
        DAMProcessor dam = this.getDAMProcessor();
        SafeSQL safeSQL = new SafeSQL();
        if (keyCount < 2) {
            String sql = "SELECT " + keycol1 + ", securityset FROM " + tableid + " WHERE " + keycol1 + " IN ( " + safeSQL.addIn(StringUtil.replaceAll(items, ";", "','", true)) + " )";
            data = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        } else {
            String rsetId = dam.createRSet(sdcid, items, items2, items3);
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT " + sqlKeyCol.toString() + ", t.securityset FROM " + tableid + " t, rsetitems r  WHERE r.rsetid = " + safeSQL.addVar(rsetId) + " AND r.sdcid = " + safeSQL.addVar(sdcid) + " AND t." + keycol1 + " = r.keyid1 AND  t." + keycol2 + " = r.keyid2");
            if (keyCount > 2) {
                sql.append(" AND t." + keycol3 + " = r.keyid3");
            }
            data = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            dam.clearRSet(rsetId);
        }
        if (data == null || data.size() <= 0) throw new SapphireException(this.getTranslationProcessor().translate("Could not get sdc data."));
        data.addColumn("result", 1);
        ConnectionInfo connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId());
        String passlist = "";
        String faillist = "";
        String callstmt = "{? = call lv_rset" + (connectionInfo.isOracle() ? "." : "_") + "checkaccessset ( ?, ?, ?, ?, ? ) }";
        block9: for (int index = 0; index < data.getRowCount(); ++index) {
            String securitySet = data.getString(index, "securityset", "");
            if (securitySet.length() > 0) {
                BigDecimal bd;
                int result = -1;
                HashMap<String, String> find = new HashMap<String, String>(1);
                find.put("securityset", securitySet);
                int found = data.findRow(find);
                if (found > -1 && (bd = data.getBigDecimal(found, "result")) != null) {
                    result = bd.intValue();
                }
                if (result == -1) {
                    try {
                        CallableStatement cs = this.database.prepareCall(callstmt);
                        try {
                            cs.registerOutParameter(1, 2);
                            cs.setString(2, sdcid);
                            cs.setString(3, operation);
                            cs.setString(4, connectionInfo.getSysuserId());
                            cs.setString(5, connectionInfo.getCurrentJobtype());
                            cs.setString(6, securitySet);
                            cs.execute();
                            result = cs.getInt(1);
                        }
                        finally {
                            this.database.closeCall();
                        }
                    }
                    catch (Exception e) {
                        this.logger.info("Could not run procedure for " + data.getString(index, keycol1, "") + ". Reason: " + e.getMessage());
                        result = 0;
                    }
                    data.setNumber(index, "result", result);
                }
                switch (result) {
                    case 0: {
                        data.setNumber(index, "result", 0);
                        faillist = faillist + "; " + data.getString(index, keycol1, "");
                        if (keyCount <= 1) continue block9;
                        faillist = faillist + "(" + data.getString(index, keycol2, "");
                        if (keyCount > 2) {
                            faillist = faillist + "," + data.getString(index, keycol3, "");
                        }
                        faillist = faillist + ")";
                        break;
                    }
                    case 1: {
                        data.setNumber(index, "result", 1);
                        passlist = passlist + "; " + data.getString(index, keycol1, "");
                        if (keyCount <= 1) continue block9;
                        passlist = passlist + "(" + data.getString(index, keycol2, "");
                        if (keyCount > 2) {
                            passlist = passlist + "," + data.getString(index, keycol3, "");
                        }
                        passlist = passlist + ")";
                        break;
                    }
                    default: {
                        data.setNumber(index, "result", 0);
                        faillist = faillist + "; " + data.getString(index, keycol1, "");
                        if (keyCount <= 1) continue block9;
                        faillist = faillist + "(" + data.getString(index, keycol2, "");
                        if (keyCount > 2) {
                            faillist = faillist + "," + data.getString(index, keycol3, "");
                        }
                        faillist = faillist + ")";
                        break;
                    }
                }
                continue;
            }
            passlist = passlist + "; " + data.getString(index, keycol1, "");
            if (keyCount <= 1) continue;
            passlist = passlist + "(" + data.getString(index, keycol2, "");
            if (keyCount > 2) {
                passlist = passlist + "," + data.getString(index, keycol3, "");
            }
            passlist = passlist + ")";
        }
        if (faillist.length() > 0) {
            faillist = faillist.substring(1);
        }
        if (passlist.length() > 0) {
            passlist = passlist.substring(1);
        }
        props.setProperty("operation", operation);
        props.setProperty("failedsdis", faillist);
        props.setProperty("passedsdis", passlist);
    }

    private void checkSDIAccessList(PropertyList props, String operation, String sdcid, String items, String items2, String items3) throws SapphireException {
        ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(this.getConnectionId());
        boolean viewHidden = "Y".equals(configurationProcessor.getProfileProperty(this.connectionInfo.getSysuserId(), "viewhidden", "N"));
        SDIList sdiList = this.getDAMProcessor().checkSDIAccess(sdcid, items, items2, items3, viewHidden, operation);
        int includedCnt = sdiList.size();
        String retKeyId1 = sdiList.getKeyid1();
        String retKeyId2 = sdiList.getKeyid2();
        String retKeyId3 = sdiList.getKeyid3();
        int keyCount = Integer.parseInt(this.getSDCProcessor().getProperty(sdcid, "keycolumns"));
        DataSet data = new DataSet();
        DataSet includedData = new DataSet();
        data.addColumnValues("keyid1", 0, items, ";");
        if (keyCount > 1) {
            data.addColumnValues("keyid2", 0, items2, ";");
            if (keyCount > 2) {
                data.addColumnValues("keyid3", 0, items3, ";");
            }
        }
        if (includedCnt > 0) {
            includedData.addColumnValues("keyid1", 0, retKeyId1, ";");
            if (keyCount > 1) {
                includedData.addColumnValues("keyid2", 0, retKeyId2, ";");
                if (keyCount > 2) {
                    includedData.addColumnValues("keyid3", 0, retKeyId3, ";");
                }
            }
        }
        if (includedCnt > 0) {
            StringBuffer faillist = new StringBuffer();
            StringBuffer passlist = new StringBuffer();
            HashMap<String, String> filter = new HashMap<String, String>();
            for (int i = 0; i < data.getRowCount(); ++i) {
                filter.put("keyid1", data.getString(i, "keyid1", ""));
                if (keyCount > 1) {
                    filter.put("keyid2", data.getString(i, "keyid2", ""));
                    if (keyCount > 2) {
                        filter.put("keyid3", data.getString(i, "keyid3", ""));
                    }
                }
                if (includedData.findRow(filter) > -1) {
                    passlist.append("; ").append(data.getString(i, "keyid1", ""));
                    if (keyCount > 1) {
                        passlist.append("(").append(data.getString(i, "keyid2", ""));
                        if (keyCount > 2) {
                            passlist.append(",").append(data.getString(i, "keyid3", ""));
                        }
                        passlist.append(")");
                    }
                } else {
                    faillist.append("; ").append(data.getString(i, "keyid1", ""));
                    if (keyCount > 1) {
                        faillist.append("(").append(data.getString(i, "keyid2", ""));
                        if (keyCount > 2) {
                            faillist.append(",").append(data.getString(i, "keyid3", ""));
                        }
                        faillist.append(")");
                    }
                }
                filter.clear();
            }
            props.setProperty("operation", operation);
            props.setProperty("failedsdis", faillist.length() > 0 ? faillist.substring(1) : "");
            props.setProperty("passedsdis", passlist.length() > 0 ? passlist.substring(1) : "");
        } else {
            StringBuffer faillist = new StringBuffer();
            for (int i = 0; i < data.getRowCount(); ++i) {
                faillist.append("; ").append(data.getString(i, "keyid1", ""));
                if (keyCount <= 1) continue;
                faillist.append("(").append(data.getString(i, "keyid2", ""));
                if (keyCount > 2) {
                    faillist.append(",").append(data.getString(i, "keyid3", ""));
                }
                faillist.append(")");
            }
            props.setProperty("operation", operation);
            props.setProperty("failedsdis", faillist.length() > 0 ? faillist.substring(1) : "");
            props.setProperty("passedsdis", "");
        }
    }
}

