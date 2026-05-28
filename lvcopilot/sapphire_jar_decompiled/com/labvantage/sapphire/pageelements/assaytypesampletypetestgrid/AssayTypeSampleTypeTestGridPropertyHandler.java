/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.assaytypesampletypetestgrid;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class AssayTypeSampleTypeTestGridPropertyHandler
extends PropertyHandler {
    static final String LABVANTAGE_CVS_ID = "$Revision: 77328 $";

    @Override
    public void processProperties(HashMap props) throws SapphireException {
        PropertyList pl = new PropertyList(props);
        String elementId = pl.getProperty("__assatypesampletypetestgrid_elementid", "");
        String jsonStr = pl.getProperty("__" + elementId + "_jsonString");
        if (jsonStr.length() > 0) {
            DBUtil db = new DBUtil();
            db.setConnection(this.sapphireConnection);
            String keyid1 = pl.getProperty("__" + elementId + "_keyid1", "");
            String keyid2 = pl.getProperty("__" + elementId + "_keyid2", "");
            String keyid3 = pl.getProperty("__" + elementId + "_keyid3", "");
            int assayTypeInsDel = 0;
            int sampleTypeInsDel = 0;
            try {
                JSONArray array;
                JSONObject modifiedWorkItems;
                int inserted;
                String sampleTypeId;
                int deleted;
                String assayTypeId;
                Iterator itr;
                JSONObject json = new JSONObject(jsonStr);
                JSONObject delAssayTypes = json.getJSONObject("deletedAssayTypes");
                JSONObject delSampleTypes = json.getJSONObject("deletedSampleTypes");
                JSONObject addAssayTypes = json.getJSONObject("addedAssayTypes");
                JSONObject addSampleTypes = json.getJSONObject("addedSampleTypes");
                if (delAssayTypes != null && (itr = delAssayTypes.keys()).hasNext()) {
                    PreparedStatement deleteAssayTypes = db.prepareStatement("deleteAssayTypes", "DELETE FROM s_cpassaytypesampletype WHERE s_clinicalprotocolid=? AND s_clinicalprotocolversionid=? AND s_clinicalprotocolrevision=? AND s_assaytypeid = ?");
                    PreparedStatement deleteCPAssayTypes = db.prepareStatement("deleteCPAssayTypes", "DELETE FROM s_cpassaytype WHERE s_clinicalprotocolid=? AND s_clinicalprotocolversionid=? AND s_clinicalprotocolrevision=? AND s_assaytypeid = ?");
                    while (itr.hasNext()) {
                        assayTypeId = delAssayTypes.getString((String)itr.next());
                        if (assayTypeId.trim().length() <= 0) continue;
                        deleteAssayTypes.setString(1, keyid1);
                        deleteAssayTypes.setString(2, keyid2);
                        deleteAssayTypes.setString(3, keyid3);
                        deleteAssayTypes.setString(4, assayTypeId);
                        deleteAssayTypes.executeUpdate();
                        deleteCPAssayTypes.setString(1, keyid1);
                        deleteCPAssayTypes.setString(2, keyid2);
                        deleteCPAssayTypes.setString(3, keyid3);
                        deleteCPAssayTypes.setString(4, assayTypeId);
                        deleted = deleteCPAssayTypes.executeUpdate();
                        assayTypeInsDel += deleted;
                    }
                }
                if (delSampleTypes != null && (itr = delSampleTypes.keys()).hasNext()) {
                    PreparedStatement deleteSampleTypes = db.prepareStatement("deleteSampleTypes", "DELETE FROM s_cpassaytypesampletype WHERE s_clinicalprotocolid=? AND s_clinicalprotocolversionid=? AND s_clinicalprotocolrevision=? AND s_sampletypeid = ?");
                    PreparedStatement deleteCPSampleTypes = db.prepareStatement("deleteCPSampleTypes", "DELETE FROM s_cpsampletype WHERE s_clinicalprotocolid=? AND s_clinicalprotocolversionid=? AND s_clinicalprotocolrevision=? AND s_sampletypeid = ?");
                    while (itr.hasNext()) {
                        sampleTypeId = delSampleTypes.getString((String)itr.next());
                        if (sampleTypeId.trim().length() <= 0) continue;
                        deleteSampleTypes.setString(1, keyid1);
                        deleteSampleTypes.setString(2, keyid2);
                        deleteSampleTypes.setString(3, keyid3);
                        deleteSampleTypes.setString(4, sampleTypeId);
                        deleteSampleTypes.executeUpdate();
                        deleteCPSampleTypes.setString(1, keyid1);
                        deleteCPSampleTypes.setString(2, keyid2);
                        deleteCPSampleTypes.setString(3, keyid3);
                        deleteCPSampleTypes.setString(4, sampleTypeId);
                        deleted = deleteCPSampleTypes.executeUpdate();
                        sampleTypeInsDel += deleted;
                    }
                }
                if (addAssayTypes != null && (itr = addAssayTypes.keys()).hasNext()) {
                    PreparedStatement insertCPAssaytype = db.prepareStatement("insertAssayTypes", "INSERT INTO s_cpassaytype (s_clinicalprotocolid, s_clinicalprotocolversionid, s_clinicalprotocolrevision, s_assaytypeid, createdt, createtool, createby ) values( ?, ?, ?, ?, ?, ?, ?)");
                    PreparedStatement checkAssayType = db.prepareStatement("chkAssayType", "SELECT 1 FROM s_cpassaytype WHERE s_assaytypeid = ?  AND s_clinicalprotocolid = ? AND s_clinicalprotocolversionid = ? AND s_clinicalprotocolrevision = ?");
                    while (itr.hasNext()) {
                        assayTypeId = addAssayTypes.getString((String)itr.next());
                        checkAssayType.setString(1, assayTypeId);
                        checkAssayType.setString(2, keyid1);
                        checkAssayType.setString(3, keyid2);
                        checkAssayType.setString(4, keyid3);
                        if (checkAssayType.executeQuery().next() || assayTypeId.trim().length() <= 0) continue;
                        insertCPAssaytype.setString(1, keyid1);
                        insertCPAssaytype.setString(2, keyid2);
                        insertCPAssaytype.setString(3, keyid3);
                        insertCPAssaytype.setString(4, assayTypeId);
                        insertCPAssaytype.setTimestamp(5, DateTimeUtil.getNowTimestamp());
                        insertCPAssaytype.setString(6, this.connectionInfo.getTool());
                        insertCPAssaytype.setString(7, this.connectionInfo.getSysuserId());
                        inserted = insertCPAssaytype.executeUpdate();
                        assayTypeInsDel += inserted;
                    }
                }
                if (addSampleTypes != null && (itr = addSampleTypes.keys()).hasNext()) {
                    PreparedStatement insertCPSampletype = db.prepareStatement("insertSampleTypes", "INSERT INTO s_cpsampletype (s_clinicalprotocolid, s_clinicalprotocolversionid, s_clinicalprotocolrevision, s_sampletypeid, createdt, createtool, createby ) values( ?, ?, ?, ?, ?, ?, ?)");
                    PreparedStatement checkSampleType = db.prepareStatement("chkSampleType", "SELECT 1 FROM s_cpsampletype  WHERE s_sampletypeid = ? AND s_clinicalprotocolid = ? AND s_clinicalprotocolversionid = ? AND s_clinicalprotocolrevision = ?");
                    while (itr.hasNext()) {
                        sampleTypeId = addSampleTypes.getString((String)itr.next());
                        checkSampleType.setString(1, sampleTypeId);
                        checkSampleType.setString(2, keyid1);
                        checkSampleType.setString(3, keyid2);
                        checkSampleType.setString(4, keyid3);
                        if (checkSampleType.executeQuery().next() || sampleTypeId.trim().length() <= 0) continue;
                        insertCPSampletype.setString(1, keyid1);
                        insertCPSampletype.setString(2, keyid2);
                        insertCPSampletype.setString(3, keyid3);
                        insertCPSampletype.setString(4, sampleTypeId);
                        insertCPSampletype.setTimestamp(5, DateTimeUtil.getNowTimestamp());
                        insertCPSampletype.setString(6, this.connectionInfo.getTool());
                        insertCPSampletype.setString(7, this.connectionInfo.getSysuserId());
                        inserted = insertCPSampletype.executeUpdate();
                        sampleTypeInsDel += inserted;
                    }
                }
                if ((modifiedWorkItems = json.getJSONObject("modifiedWorkItems")) != null && (array = modifiedWorkItems.names()) != null && array.length() > 0) {
                    PreparedStatement delCPAssaytypeSamplType = db.prepareStatement("deleteCPAssaySampTypes", "DELETE FROM s_cpassaytypesampletype WHERE s_clinicalprotocolid = ? AND s_clinicalprotocolversionid = ? AND s_clinicalprotocolrevision = ?  AND s_assaytypeid = ? AND s_sampletypeid = ? ");
                    PreparedStatement updateCPAssaytypeSamplType = db.prepareStatement("updateCPAssaySampTypes", "UPDATE s_cpassaytypesampletype SET workitemid = ?, specimentype = ?, arrivalorder = ? WHERE s_clinicalprotocolid = ? AND s_clinicalprotocolversionid = ? AND s_clinicalprotocolrevision = ?  AND s_assaytypeid = ? AND s_sampletypeid = ? ");
                    PreparedStatement insertCPAssaytypeSamplType = db.prepareStatement("insertCPAssaySampTypes", "INSERT INTO s_cpassaytypesampletype (s_clinicalprotocolid, s_clinicalprotocolversionid, s_clinicalprotocolrevision, s_assaytypeid, s_sampletypeid, workitemid, specimentype, arrivalorder, createdt, createtool, createby ) values( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    for (int i = 0; i < array.length(); ++i) {
                        JSONObject modifiedWI = modifiedWorkItems.getJSONObject(array.getString(i));
                        String sampleTypeId2 = modifiedWI.getString("sampletype");
                        String assayTypeId2 = modifiedWI.getString("assaytype");
                        String workItemId = modifiedWI.getString("workitem");
                        String opStatus = modifiedWI.getString("opstatus");
                        String specimenType = modifiedWI.getString("specimentype");
                        String arrivalOrder = modifiedWI.getString("arrivalorder");
                        if (opStatus == null) continue;
                        if (opStatus.equalsIgnoreCase("D")) {
                            delCPAssaytypeSamplType.setString(1, keyid1);
                            delCPAssaytypeSamplType.setString(2, keyid2);
                            delCPAssaytypeSamplType.setString(3, keyid3);
                            delCPAssaytypeSamplType.setString(4, assayTypeId2);
                            delCPAssaytypeSamplType.setString(5, sampleTypeId2);
                            delCPAssaytypeSamplType.executeUpdate();
                            continue;
                        }
                        if (opStatus.equalsIgnoreCase("E") && workItemId != null && workItemId.trim().length() > 0) {
                            updateCPAssaytypeSamplType.setString(1, workItemId);
                            updateCPAssaytypeSamplType.setString(2, specimenType);
                            updateCPAssaytypeSamplType.setString(3, arrivalOrder);
                            updateCPAssaytypeSamplType.setString(4, keyid1);
                            updateCPAssaytypeSamplType.setString(5, keyid2);
                            updateCPAssaytypeSamplType.setString(6, keyid3);
                            updateCPAssaytypeSamplType.setString(7, assayTypeId2);
                            updateCPAssaytypeSamplType.setString(8, sampleTypeId2);
                            updateCPAssaytypeSamplType.executeUpdate();
                            continue;
                        }
                        if (!opStatus.equalsIgnoreCase("A") || workItemId == null || workItemId.trim().length() <= 0) continue;
                        insertCPAssaytypeSamplType.setString(1, keyid1);
                        insertCPAssaytypeSamplType.setString(2, keyid2);
                        insertCPAssaytypeSamplType.setString(3, keyid3);
                        insertCPAssaytypeSamplType.setString(4, assayTypeId2);
                        insertCPAssaytypeSamplType.setString(5, sampleTypeId2);
                        insertCPAssaytypeSamplType.setString(6, workItemId);
                        insertCPAssaytypeSamplType.setString(7, specimenType);
                        insertCPAssaytypeSamplType.setString(8, arrivalOrder);
                        insertCPAssaytypeSamplType.setTimestamp(9, DateTimeUtil.getNowTimestamp());
                        insertCPAssaytypeSamplType.setString(10, this.connectionInfo.getTool());
                        insertCPAssaytypeSamplType.setString(11, this.connectionInfo.getSysuserId());
                        insertCPAssaytypeSamplType.execute();
                    }
                }
                if (assayTypeInsDel > 0) {
                    this.updateSequence("assaytype", "useqAssayTypes", json, db, keyid1, keyid2, keyid3);
                }
                if (sampleTypeInsDel > 0) {
                    this.updateSequence("sampletype", "useqSampleTypes", json, db, keyid1, keyid2, keyid3);
                }
            }
            catch (Exception se) {
                Trace.logError("AssayTypeSampleTypeTestGridPropertyHandler failed to save: " + se.getMessage(), se);
                se.printStackTrace();
                throw new SapphireException(ErrorUtil.extractMessageFromException(se, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), se);
            }
            finally {
                db.closePreparedStatements();
            }
        }
    }

    private void updateSequence(String type, String arrayName, JSONObject json, DBUtil db, String keyid1, String keyid2, String keyid3) throws Exception {
        JSONArray array;
        JSONObject useqArrayObj = json.getJSONObject(arrayName);
        if (useqArrayObj != null && (array = useqArrayObj.names()) != null && array.length() > 0) {
            int i;
            DataSet ds = new DataSet();
            ds.addColumn("s_clinicalprotocolid", 0);
            ds.addColumn("s_clinicalprotocolversionid", 0);
            ds.addColumn("s_clinicalprotocolrevision", 0);
            ds.addColumn("s_" + type + "id", 0);
            ds.addColumn("usersequence", 1);
            for (i = 0; i < array.length(); ++i) {
                JSONObject obj = useqArrayObj.getJSONObject(array.getString(i));
                String id = obj.getString(type + "id");
                String usersequence = obj.getString("usersequence");
                int r = ds.addRow();
                ds.setString(r, "s_" + type + "id", id);
                ds.setNumber(r, "usersequence", usersequence);
            }
            ds.setString(-1, "s_clinicalprotocolid", keyid1);
            ds.setString(-1, "s_clinicalprotocolversionid", keyid2);
            ds.setString(-1, "s_clinicalprotocolrevision", keyid3);
            ds.sort("usersequence");
            for (i = 0; i < ds.size(); ++i) {
                ds.setNumber(i, "usersequence", i + 1);
            }
            String[] keycols = new String[]{"s_clinicalprotocolid", "s_clinicalprotocolversionid", "s_clinicalprotocolrevision", "s_" + type + "id"};
            DataSetUtil.update(db, ds, "s_cp" + type, keycols);
        }
    }
}

