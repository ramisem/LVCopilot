/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.actions;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class AddSDIAttachmentOperation
extends BaseAction
implements SDMSConstants {
    private static final String specialDelimer = "^^^";

    public static void createAttachmentOperation(PropertyList propertyList, M18NUtil m18n, DBAccess dbUtil, QueryProcessor qp, ConnectionProcessor cp, Logger logger) throws SapphireException {
        String sdcid = propertyList.getProperty("sdcid");
        String keyid1 = propertyList.getProperty("keyid1");
        String keyid2 = propertyList.getProperty("keyid2");
        String keyid3 = propertyList.getProperty("keyid3");
        String attachmenthandlerid = propertyList.getProperty("attachmenthandlerid");
        String attachmentclass = propertyList.getProperty("attachmentclass");
        String propertyclob = propertyList.getProperty("propertyclob");
        boolean sync = propertyList.getProperty("synchronousflag").equalsIgnoreCase("Y");
        String processinggroupname = propertyList.getProperty("processinggroupname");
        DataSet toinsert = new DataSet();
        toinsert.addColumn("sdcid", 0);
        toinsert.addColumn("keyid1", 0);
        toinsert.addColumn("keyid2", 0);
        toinsert.addColumn("keyid3", 0);
        toinsert.addColumn("attachmentoperationid", 0);
        toinsert.addColumn("operationsdcid", 0);
        toinsert.addColumn("operationkeyid1", 0);
        toinsert.addColumn("attachmentclass", 0);
        toinsert.addColumn("synchronousflag", 0);
        toinsert.addColumn("processinggroupname", 0);
        toinsert.addColumn("propertyclob", 3);
        toinsert.addColumn("__rowstatus", 0);
        toinsert.setM18NUtil(m18n);
        toinsert.addRow();
        toinsert.setValue(0, "sdcid", sdcid);
        toinsert.setValue(0, "keyid1", keyid1);
        if (keyid2 != null && keyid2.length() > 0) {
            toinsert.setValue(0, "keyid2", keyid2);
        }
        if (keyid3 != null && keyid3.length() > 0) {
            toinsert.setValue(0, "keyid3", keyid3);
        }
        toinsert.setValue(0, "operationsdcid", "LV_AttachmentHandler");
        toinsert.setValue(0, "operationkeyid1", attachmenthandlerid);
        toinsert.setValue(0, "attachmentclass", attachmentclass != null ? attachmentclass : "");
        toinsert.setValue(0, "synchronousflag", sync ? "Y" : "N");
        toinsert.setValue(0, "processinggroupname", processinggroupname);
        toinsert.setClob(0, "propertyclob", propertyclob);
        toinsert.setValue(0, "__rowstatus", "I");
        AddSDIAttachmentOperation.saveAttachmentOperations(sdcid, toinsert, m18n, dbUtil, qp, cp, logger);
    }

    public static void saveAttachmentOperations(String sdcid, DataSet capturedata, M18NUtil m18n, DBAccess dbUtil, QueryProcessor qp, ConnectionProcessor cp, Logger logger) throws SapphireException {
        AddSDIAttachmentOperation.saveAttachmentOperations(sdcid, capturedata, m18n, dbUtil, qp, cp, logger, true);
    }

    public static void saveAttachmentOperations(String sdcid, DataSet capturedata, M18NUtil m18n, DBAccess dbUtil, QueryProcessor qp, ConnectionProcessor cp, Logger logger, boolean updatepropertyclobOnInsert) throws SapphireException {
        DataSet toinsert = new DataSet();
        toinsert.addColumn("sdcid", 0);
        toinsert.addColumn("keyid1", 0);
        toinsert.addColumn("keyid2", 0);
        toinsert.addColumn("keyid3", 0);
        toinsert.addColumn("attachmentoperationid", 0);
        toinsert.addColumn("operationsdcid", 0);
        toinsert.addColumn("operationkeyid1", 0);
        toinsert.addColumn("attachmentclass", 0);
        toinsert.addColumn("synchronousflag", 0);
        toinsert.addColumn("processinggroupname", 0);
        toinsert.addColumn("usersequence", 0);
        toinsert.addColumn("propertyclob", 3);
        DataSet toupdate = (DataSet)toinsert.clone();
        toupdate.removeColumn("propertyclob");
        HashMap maxUserSeq = new HashMap();
        for (int i = 0; i < capturedata.getRowCount(); ++i) {
            String keyid1 = capturedata.getValue(i, "keyid1", "");
            String keyid2 = capturedata.getValue(i, "keyid2", "(null)");
            String keyid3 = capturedata.getValue(i, "keyid3", "(null)");
            String attachmentoperationid = capturedata.getValue(i, "attachmentoperationid", "");
            String rowstatus = capturedata.getValue(i, "__rowstatus", "S");
            if (attachmentoperationid.length() == 0 && rowstatus.equalsIgnoreCase("I")) {
                SequenceProcessor sp = new SequenceProcessor(cp.getConnectionid());
                int sequence = sp.getSequence("SDIAttachmentOperation", "AttachmentOperationID");
                attachmentoperationid = "CO" + String.format("%04d", sequence);
            }
            if (attachmentoperationid.length() > 0) {
                if (rowstatus.equalsIgnoreCase("I")) {
                    int usersequence = capturedata.getInt(i, "usersequence", 0);
                    if (usersequence == 0) {
                        usersequence = AddSDIAttachmentOperation.getMaxUsersequence(sdcid, keyid1, keyid2, keyid3, maxUserSeq, qp) + 1;
                    }
                    int r = toinsert.addRow();
                    toinsert.setValue(r, "sdcid", sdcid);
                    toinsert.setValue(r, "keyid1", keyid1);
                    if (keyid2.length() > 0 && !keyid2.equalsIgnoreCase("(null)")) {
                        toinsert.setValue(r, "keyid2", keyid2);
                        if (keyid3.length() > 0 && !keyid3.equalsIgnoreCase("(null)")) {
                            toinsert.setValue(r, "keyid3", keyid3);
                        } else {
                            toinsert.setValue(r, "keyid3", "(null)");
                        }
                    } else {
                        toinsert.setValue(r, "keyid2", "(null)");
                        toinsert.setValue(r, "keyid3", "(null)");
                    }
                    toinsert.setValue(r, "attachmentoperationid", attachmentoperationid);
                    toinsert.setValue(r, "operationsdcid", capturedata.getValue(i, "operationsdcid", ""));
                    toinsert.setValue(r, "operationkeyid1", capturedata.getValue(i, "operationkeyid1", ""));
                    toinsert.setValue(r, "attachmentclass", capturedata.getValue(i, "attachmentclass", ""));
                    toinsert.setValue(r, "synchronousflag", capturedata.getValue(i, "synchronousflag", ""));
                    toinsert.setValue(r, "processinggroupname", capturedata.getValue(i, "processinggroupname", ""));
                    toinsert.setValue(r, "usersequence", String.valueOf(usersequence));
                    toinsert.setValue(r, "propertyclob", capturedata.getClob(i, "propertyclob", ""));
                    continue;
                }
                if (rowstatus.equalsIgnoreCase("U")) {
                    int r = toupdate.addRow();
                    toupdate.setValue(r, "sdcid", sdcid);
                    toupdate.setValue(r, "keyid1", keyid1);
                    if (keyid2.length() > 0 && !keyid2.equalsIgnoreCase("(null)")) {
                        toupdate.setValue(r, "keyid2", keyid2);
                        if (keyid3.length() > 0 && !keyid3.equalsIgnoreCase("(null)")) {
                            toupdate.setValue(r, "keyid3", keyid3);
                        } else {
                            toupdate.setValue(r, "keyid3", "(null)");
                        }
                    } else {
                        toupdate.setValue(r, "keyid2", "(null)");
                        toupdate.setValue(r, "keyid3", "(null)");
                    }
                    toupdate.setValue(r, "attachmentoperationid", attachmentoperationid);
                    toupdate.setValue(r, "operationsdcid", capturedata.getValue(i, "operationsdcid", ""));
                    toupdate.setValue(r, "operationkeyid1", capturedata.getValue(i, "operationkeyid1", ""));
                    toupdate.setValue(r, "attachmentclass", capturedata.getValue(i, "attachmentclass", ""));
                    String synchronousflag = capturedata.getValue(i, "synchronousflag", "");
                    toupdate.setValue(r, "synchronousflag", synchronousflag);
                    toupdate.setValue(r, "processinggroupname", synchronousflag.equalsIgnoreCase("Y") ? "" : capturedata.getValue(i, "processinggroupname", ""));
                    toupdate.setValue(r, "usersequence", capturedata.getValue(i, "usersequence"));
                    continue;
                }
                if (rowstatus.equalsIgnoreCase("D")) {
                    Object[] objects;
                    StringBuilder sql = new StringBuilder("DELETE sdiattachmentoperation WHERE sdcid=? AND keyid1=? ");
                    if (keyid2.length() > 0 && !keyid2.equalsIgnoreCase("(null)")) {
                        sql.append(" AND keyid2=? ");
                        if (keyid3.length() > 0 && !keyid3.equalsIgnoreCase("(null)")) {
                            sql.append(" AND keyid3=? ");
                            objects = new Object[]{sdcid, keyid1, keyid2, keyid3, attachmentoperationid};
                        } else {
                            objects = new Object[]{sdcid, keyid1, keyid2, attachmentoperationid};
                        }
                    } else {
                        objects = new Object[]{sdcid, keyid1, attachmentoperationid};
                    }
                    sql.append(" AND attachmentoperationid=?");
                    try {
                        dbUtil.executePreparedUpdate(sql.toString(), objects);
                        continue;
                    }
                    catch (Exception e) {
                        logger.warn("Failed to remove capture operation. Error - " + e.getMessage());
                        throw new SapphireException("Failed to remove capture operation.", e);
                    }
                }
                logger.debug("Row status not D or I thus ignore.");
                continue;
            }
            logger.debug("To capture operation found for capture operation");
        }
        if (toinsert.getRowCount() > 0) {
            try {
                DataSetUtil.insert(dbUtil, toinsert, "sdiattachmentoperation");
                if (updatepropertyclobOnInsert) {
                    AddSDIAttachmentOperation.updatePropertyClob(dbUtil, qp, toinsert);
                }
                AddSDIAttachmentOperation.syncInstrumentAttachmentOperations(dbUtil, qp, toinsert);
            }
            catch (Exception e) {
                logger.warn("Failed to add capture operation. Error - " + e.getMessage());
                throw new SapphireException("Failed to add capture operation.", e);
            }
        }
        if (toupdate.getRowCount() > 0) {
            try {
                DataSetUtil.update(dbUtil, toupdate, "sdiattachmentoperation", new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "attachmentoperationid"});
            }
            catch (Exception e) {
                logger.warn("Failed to update capture operation. Error - " + e.getMessage());
                throw new SapphireException("Failed to update capture operation.", e);
            }
        }
    }

    private static void updatePropertyClob(DBAccess dbUtil, QueryProcessor qp, DataSet ds) throws SapphireException {
        DataSet toupdate = new DataSet();
        toupdate.addColumn("sdcid", 0);
        toupdate.addColumn("keyid1", 0);
        toupdate.addColumn("keyid2", 0);
        toupdate.addColumn("keyid3", 0);
        toupdate.addColumn("attachmentoperationid", 0);
        toupdate.addColumn("propertyclob", 0);
        String attachmenthandlerid = ds.getColumnValues("operationkeyid1", ";");
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        sql.append("select * from attachmenthandler");
        sql.append(" where attachmenthandlerid in(").append(safeSQL.addIn(attachmenthandlerid, ";")).append(")");
        DataSet dsDH = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues(), true);
        if (dsDH != null && dsDH.size() > 0) {
            HashMap<String, String> hm = new HashMap<String, String>();
            for (int i = 0; i < ds.size(); ++i) {
                hm.clear();
                hm.put("attachmenthandlerid", ds.getString(i, "operationkeyid1", ""));
                int findRow = dsDH.findRow(hm);
                if (findRow <= -1) continue;
                int newRow = toupdate.addRow();
                toupdate.setString(newRow, "sdcid", ds.getString(i, "sdcid", ""));
                toupdate.setString(newRow, "keyid1", ds.getString(i, "keyid1", ""));
                toupdate.setString(newRow, "keyid2", ds.getString(i, "keyid2", ""));
                toupdate.setString(newRow, "keyid3", ds.getString(i, "keyid3", ""));
                toupdate.setString(newRow, "attachmentoperationid", ds.getString(i, "attachmentoperationid", ""));
                PropertyList parent = new PropertyList();
                String p1 = dsDH.getClob(findRow, "propertyclob", "");
                PropertyList child = new PropertyList();
                String c1 = ds.getClob(i, "propertyclob", "");
                if (p1.length() > 0) {
                    try {
                        parent = new PropertyList(new JSONObject(p1));
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                if (c1.length() > 0) {
                    try {
                        child = new PropertyList(new JSONObject(c1));
                        parent.setPropertyList(child.toXMLString(), true);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                toupdate.setClob(newRow, "propertyclob", parent.toJSONString());
            }
            if (toupdate.size() > 0) {
                DataSetUtil.update(dbUtil, toupdate, "sdiattachmentoperation", new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "attachmentoperationid"});
            }
        }
    }

    private static void syncInstrumentAttachmentOperations(DBAccess dbUtil, QueryProcessor qp, DataSet toinsert) throws SapphireException {
        String instrumenttype;
        String instrumentmodelid;
        DataSet instrumentDS;
        String sdcid = toinsert.getString(0, "sdcid", "");
        if (sdcid.equalsIgnoreCase("LV_InstrumentModel") && (instrumentDS = AddSDIAttachmentOperation.getInstruments(qp, instrumentmodelid = toinsert.getString(0, "keyid1", ""), instrumenttype = toinsert.getString(0, "keyid2", ""))) != null && instrumentDS.size() > 0) {
            DataSet toInstrInsert = new DataSet();
            toInstrInsert.addColumn("sdcid", 0);
            toInstrInsert.addColumn("keyid1", 0);
            toInstrInsert.addColumn("keyid2", 0);
            toInstrInsert.addColumn("keyid3", 0);
            toInstrInsert.addColumn("attachmentoperationid", 0);
            toInstrInsert.addColumn("operationsdcid", 0);
            toInstrInsert.addColumn("operationkeyid1", 0);
            toInstrInsert.addColumn("attachmentclass", 0);
            toInstrInsert.addColumn("synchronousflag", 0);
            toInstrInsert.addColumn("processinggroupname", 0);
            toInstrInsert.addColumn("usersequence", 1);
            DataSet attachmentOperation = AddSDIAttachmentOperation.getAttachmentOperation(qp, instrumentDS);
            HashMap<String, String> hm = new HashMap<String, String>();
            for (int i = 0; i < toinsert.size(); ++i) {
                for (int j = 0; j < instrumentDS.size(); ++j) {
                    hm.clear();
                    hm.put("operationkeyid1", toinsert.getString(i, "operationkeyid1", ""));
                    hm.put("keyid1", instrumentDS.getString(j, "instrumentid", ""));
                    hm.put("instrumentmodelid", instrumentDS.getString(j, "instrumentmodelid", ""));
                    hm.put("instrumenttype", instrumentDS.getString(j, "instrumenttype", ""));
                    int findRow = attachmentOperation.findRow(hm);
                    if (findRow != -1) continue;
                    int r = toInstrInsert.addRow();
                    toInstrInsert.setValue(r, "sdcid", "Instrument");
                    toInstrInsert.setValue(r, "keyid1", instrumentDS.getString(j, "instrumentid", ""));
                    toInstrInsert.setValue(r, "keyid2", "(null)");
                    toInstrInsert.setValue(r, "keyid3", "(null)");
                    toInstrInsert.setValue(r, "attachmentoperationid", toinsert.getValue(i, "attachmentoperationid", ""));
                    toInstrInsert.setValue(r, "operationsdcid", toinsert.getValue(i, "operationsdcid", ""));
                    toInstrInsert.setValue(r, "operationkeyid1", toinsert.getValue(i, "operationkeyid1", ""));
                    toInstrInsert.setValue(r, "attachmentclass", toinsert.getValue(i, "attachmentclass", ""));
                    toInstrInsert.setValue(r, "synchronousflag", toinsert.getValue(i, "synchronousflag", "N"));
                    toInstrInsert.setValue(r, "processinggroupname", toinsert.getValue(i, "processinggroupname", ""));
                    toInstrInsert.setClob(r, "propertyclob", toinsert.getClob(i, "propertyclob", ""));
                    toInstrInsert.setNumber(r, "usersequence", toinsert.getValue(i, "usersequence", "1"));
                }
            }
            if (toInstrInsert.getRowCount() > 0) {
                try {
                    DataSetUtil.insert(dbUtil, toInstrInsert, "sdiattachmentoperation");
                }
                catch (Exception e) {
                    throw new SapphireException("Failed to add capture operation.", e);
                }
            }
        }
    }

    private static DataSet getInstruments(QueryProcessor qp, String instrumentmodelid, String instrumenttype) {
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("select instrumentid,instrumentmodelid,instrumenttype");
        sql.append(" from instrument");
        sql.append(" where instrumentmodelid=").append(safeSQL.addVar(instrumentmodelid));
        sql.append(" and instrumenttype=").append(safeSQL.addVar(instrumenttype));
        return qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
    }

    private static DataSet getAttachmentOperation(QueryProcessor qp, DataSet instrumentDS) {
        DataSet ds = new DataSet();
        if (instrumentDS.size() > 0) {
            StringBuilder sql = new StringBuilder();
            SafeSQL safeSQL = new SafeSQL();
            sql.append("select sdiao.*,inst.instrumenttype,inst.instrumentmodelid from sdiattachmentoperation sdiao left join instrument inst on inst.instrumentid=sdiao.keyid1 ");
            sql.append(" where sdcid='Instrument'");
            sql.append(" and keyid1 IN (" + safeSQL.addIn(instrumentDS.getColumnValues("instrumentid", ";"), ";") + ")");
            ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        }
        return ds;
    }

    public static int getMaxUsersequence(String sdcid, String keyid1, String keyid2, String keyid3, Map maxUserSeq, QueryProcessor qp) {
        int max = 0;
        String key = sdcid + "%3B" + keyid1 + "%3B" + keyid2 + "%3B" + keyid3;
        if (maxUserSeq.containsKey(key)) {
            max = (Integer)maxUserSeq.get(key);
        } else {
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT count(keyid1) FROM sdiattachmentoperation ");
            sql.append(" where sdcid=").append(safeSQL.addVar(sdcid));
            sql.append(" and keyid1=").append(safeSQL.addVar(keyid1));
            if (keyid2.length() > 0 && !keyid2.equalsIgnoreCase("(null)")) {
                sql.append(" and keyid2=").append(safeSQL.addVar(keyid2));
            }
            if (keyid3.length() > 0 && !keyid3.equalsIgnoreCase("(null)")) {
                sql.append(" and keyid3=").append(safeSQL.addVar(keyid3));
            }
            try {
                if (qp.getPreparedCount(sql.toString(), safeSQL.getValues()) > 0) {
                    DataSet ds;
                    safeSQL = new SafeSQL();
                    sql = new StringBuffer();
                    sql.append("select max(usersequence) usersequence from sdiattachmentoperation");
                    sql.append(" where sdcid=").append(safeSQL.addVar(sdcid));
                    sql.append(" and keyid1=").append(safeSQL.addVar(keyid1));
                    if (keyid2.length() > 0 && !keyid2.equalsIgnoreCase("(null)")) {
                        sql.append(" and keyid2=").append(safeSQL.addVar(keyid2));
                    }
                    if (keyid3.length() > 0 && !keyid3.equalsIgnoreCase("(null)")) {
                        sql.append(" and keyid3=").append(safeSQL.addVar(keyid3));
                    }
                    if ((ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues(), true)) != null && ds.size() > 0) {
                        max = ds.getInt(0, "usersequence", 0);
                    }
                }
            }
            catch (Exception e) {
                max = 0;
            }
        }
        maxUserSeq.put(key, max + 1);
        return max;
    }

    @Override
    public void processAction(PropertyList propertyList) throws SapphireException {
        M18NUtil m18n = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()));
        AddSDIAttachmentOperation.createAttachmentOperation(propertyList, m18n, this.database, this.getQueryProcessor(), this.getConnectionProcessor(), this.logger);
    }
}

