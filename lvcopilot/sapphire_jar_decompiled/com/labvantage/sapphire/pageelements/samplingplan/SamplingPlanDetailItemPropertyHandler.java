/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.samplingplan;

import com.labvantage.opal.elements.BasePropertyHandler;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.actions.sdi.AddSDITraceLog;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class SamplingPlanDetailItemPropertyHandler
extends BasePropertyHandler {
    static final String LABVANTAGE_CVS_ID = "$Revision: 74770 $";
    static final String propHandlerPrefix = "__propertyhandler_";
    String elementId = null;
    String samplingPlanId;
    String samplingPlanVersionId;
    DBUtil db = null;
    static final String DETAILITEM_CHECKED = "T";
    static final String DETAILITEM_UNCHECKED = "F";
    static final String DETAILITEM_UNCHANGED = "U";
    static final char ITEM_SAVED = 'S';
    static final char ITEM_EDITED = 'E';
    static final char ITEM_NEW = 'N';
    static final char ITEM_DELETED = 'D';
    static final String LINK_SPITEM = "item";
    static final String LINK_SPDETAILITEM = "detail";

    @Override
    public void processProperties(HashMap props) throws SapphireException {
        this.logInfo("Start SamplingPlanDetailitemPropertyHandler processProperties(): ");
        try {
            this.elementId = this.getElementId(props);
            if (this.elementId == null || this.elementId.trim().length() == 0) {
                throw new SapphireException("Could not extract ElementId.");
            }
            ActionProcessor actionProcessor = this.getActionProcessor();
            DataSet addItemsDS = new DataSet();
            DataSet editItemsDS = new DataSet();
            DataSet deleteItemsDS = new DataSet();
            DataSet addDetailItemsDS = new DataSet();
            DataSet deleteDetailItemsDS = new DataSet();
            PropertyList pl = new PropertyList(props);
            String jsonStr = pl.getProperty("_" + this.elementId + "_jsonstr");
            if (jsonStr.length() > 0) {
                this.db = new DBUtil();
                this.db.setConnection(this.sapphireConnection);
                this.samplingPlanId = pl.getProperty("_" + this.elementId + "_keyid1", "");
                this.samplingPlanVersionId = pl.getProperty("_" + this.elementId + "_keyid2", "");
                JSONObject json = new JSONObject(jsonStr);
                JSONObject items = json.getJSONObject("items");
                JSONArray itemNames = items.names();
                this.logInfo(" json: \n" + json.toString(2));
                if (itemNames != null) {
                    block8: for (int itemCount = 0; itemCount < itemNames.length(); ++itemCount) {
                        JSONObject item = items.getJSONObject((String)itemNames.get(itemCount));
                        int itemNo = item.getInt("itemNo");
                        String sdcId = item.getString("sdcId");
                        String keyId1 = item.getString("keyId1");
                        String keyId2 = item.getString("keyId2");
                        String keyId3 = item.getString("keyId3");
                        char itemStatus = item.getString("status").charAt(0);
                        String userSequence = item.getString("userSequence");
                        JSONObject detailItems = item.getJSONObject("detailItems");
                        JSONArray detailItemNames = detailItems.names();
                        switch (itemStatus) {
                            case 'E': {
                                this.logInfo("itemNo: " + itemNo + ", to be Edited.");
                                this.editItem(editItemsDS, this.samplingPlanId, this.samplingPlanVersionId, itemNo, userSequence);
                            }
                            case 'S': {
                                String postValue;
                                String preValue;
                                int detailItemNo;
                                JSONObject detailItem;
                                int detailItemCount;
                                this.logInfo("itemNo: " + itemNo + ", nothing to do");
                                for (detailItemCount = 0; detailItemCount < detailItemNames.length(); ++detailItemCount) {
                                    detailItem = detailItems.getJSONObject((String)detailItemNames.get(detailItemCount));
                                    detailItemNo = detailItem.getInt("detailNo");
                                    preValue = detailItem.getString("preValue").trim();
                                    postValue = detailItem.getString("postValue").trim();
                                    this.logInfo("detailItemNo: " + detailItemNo + ", status: " + postValue);
                                    if (postValue.equals(DETAILITEM_UNCHANGED)) continue;
                                    if (preValue.equals(DETAILITEM_CHECKED) && postValue.equals(DETAILITEM_UNCHECKED)) {
                                        this.logInfo("detailItemNo: " + detailItemNo + " to be deleted.");
                                        this.deleteDetailItem(deleteDetailItemsDS, this.samplingPlanId, this.samplingPlanVersionId, detailItemNo, itemNo);
                                        continue;
                                    }
                                    if (!preValue.equals(DETAILITEM_UNCHECKED) || !postValue.equals(DETAILITEM_CHECKED)) continue;
                                    this.logInfo("detailItemNo: " + detailItemNo + " to be inserted.");
                                    this.insertNewDetailItem(addDetailItemsDS, this.samplingPlanId, this.samplingPlanVersionId, detailItemNo, itemNo);
                                }
                                continue block8;
                            }
                            case 'N': {
                                String postValue;
                                String preValue;
                                int detailItemNo;
                                JSONObject detailItem;
                                int detailItemCount;
                                if ("".equals(keyId1)) {
                                    throw new SapphireException("KeyID not provided for the new Item");
                                }
                                this.logInfo("itemNo: " + itemNo + ", to be Inserted");
                                this.insertNewItem(addItemsDS, this.samplingPlanId, this.samplingPlanVersionId, itemNo, sdcId, keyId1, keyId2, keyId3, userSequence);
                                for (detailItemCount = 0; detailItemCount < detailItemNames.length(); ++detailItemCount) {
                                    detailItem = detailItems.getJSONObject((String)detailItemNames.get(detailItemCount));
                                    detailItemNo = detailItem.getInt("detailNo");
                                    preValue = detailItem.getString("preValue").trim();
                                    postValue = detailItem.getString("postValue").trim();
                                    if (postValue.equals(DETAILITEM_UNCHANGED) || !postValue.equals(DETAILITEM_CHECKED)) continue;
                                    this.logInfo("detailItemNo: " + detailItemNo + " to be inserted.");
                                    this.insertNewDetailItem(addDetailItemsDS, this.samplingPlanId, this.samplingPlanVersionId, detailItemNo, itemNo);
                                }
                                continue block8;
                            }
                            case 'D': {
                                this.logInfo("itemNo: " + itemNo + " to be Deleted.");
                                this.logInfo("itemNo: " + itemNo + ", All Detail Items to be deleted.");
                                this.deleteAllDetailItem(deleteDetailItemsDS, this.samplingPlanId, this.samplingPlanVersionId, itemNo);
                                this.deleteItem(deleteItemsDS, this.samplingPlanId, this.samplingPlanVersionId, itemNo);
                            }
                        }
                    }
                    PropertyList actionProps = new PropertyList();
                    if (deleteDetailItemsDS.getRowCount() > 0) {
                        this.logInfo("Deleting Detail Items - Begin");
                        actionProps.clear();
                        actionProps.setProperty("sdcid", "LV_SamplingPlan");
                        actionProps.setProperty("linkid", LINK_SPITEM);
                        actionProps.setProperty("detaillinkid", LINK_SPDETAILITEM);
                        actionProps.setProperty("s_samplingplanid", deleteDetailItemsDS.getColumnValues("s_samplingplanid", ";"));
                        actionProps.setProperty("s_samplingplanversionid", deleteDetailItemsDS.getColumnValues("s_samplingplanversionid", ";"));
                        actionProps.setProperty("s_samplingplandetailno", deleteDetailItemsDS.getColumnValues("s_samplingplandetailno", ";"));
                        actionProps.setProperty("s_samplingplanitemno", deleteDetailItemsDS.getColumnValues("s_samplingplanitemno", ";"));
                        actionProps.setProperty("separator", ";");
                        actionProps.setProperty("tracelogid", SamplingPlanDetailItemPropertyHandler.getTraceLog(pl, actionProcessor));
                        actionProcessor.processAction("DeleteSDIDetail", "1", actionProps);
                        this.logInfo("Deleting Detail Items - End");
                    }
                    if (deleteItemsDS.getRowCount() > 0) {
                        this.logInfo("Deleting Items - Begin");
                        actionProps.clear();
                        actionProps.setProperty("sdcid", "LV_SamplingPlan");
                        actionProps.setProperty("linkid", LINK_SPITEM);
                        actionProps.setProperty("s_samplingplanid", deleteItemsDS.getColumnValues("s_samplingplanid", ";"));
                        actionProps.setProperty("s_samplingplanversionid", deleteItemsDS.getColumnValues("s_samplingplanversionid", ";"));
                        actionProps.setProperty("s_samplingplanitemno", deleteItemsDS.getColumnValues("s_samplingplanitemno", ";"));
                        actionProps.setProperty("separator", ";");
                        actionProps.setProperty("tracelogid", SamplingPlanDetailItemPropertyHandler.getTraceLog(pl, actionProcessor));
                        actionProcessor.processAction("DeleteSDIDetail", "1", actionProps);
                        this.logInfo("Deleting Items - End");
                    }
                    if (addItemsDS.getRowCount() > 0) {
                        this.logInfo("Adding Items - Begin");
                        actionProps.clear();
                        actionProps.setProperty("sdcid", "LV_SamplingPlan");
                        actionProps.setProperty("linkid", LINK_SPITEM);
                        actionProps.setProperty("s_samplingplanid", addItemsDS.getColumnValues("s_samplingplanid", ";"));
                        actionProps.setProperty("s_samplingplanversionid", addItemsDS.getColumnValues("s_samplingplanversionid", ";"));
                        actionProps.setProperty("s_samplingplanitemno", addItemsDS.getColumnValues("s_samplingplanitemno", ";"));
                        actionProps.setProperty("itemsdcid", addItemsDS.getColumnValues("itemsdcid", ";"));
                        actionProps.setProperty("itemkeyid1", addItemsDS.getColumnValues("itemkeyid1", ";"));
                        actionProps.setProperty("itemkeyid2", addItemsDS.getColumnValues("itemkeyid2", ";"));
                        actionProps.setProperty("itemkeyid3", addItemsDS.getColumnValues("itemkeyid3", ";"));
                        actionProps.setProperty("usersequence", addItemsDS.getColumnValues("usersequence", ";"));
                        actionProps.setProperty("separator", ";");
                        actionProps.setProperty("tracelogid", SamplingPlanDetailItemPropertyHandler.getTraceLog(pl, actionProcessor));
                        actionProcessor.processAction("AddSDIDetail", "1", actionProps);
                        this.logInfo("Adding Items - End");
                    }
                    if (editItemsDS.getRowCount() > 0) {
                        this.logInfo("Editing Items - Begin");
                        actionProps.clear();
                        actionProps.setProperty("sdcid", "LV_SamplingPlan");
                        actionProps.setProperty("linkid", LINK_SPITEM);
                        actionProps.setProperty("s_samplingplanid", editItemsDS.getColumnValues("s_samplingplanid", ";"));
                        actionProps.setProperty("s_samplingplanversionid", editItemsDS.getColumnValues("s_samplingplanversionid", ";"));
                        actionProps.setProperty("s_samplingplanitemno", editItemsDS.getColumnValues("s_samplingplanitemno", ";"));
                        actionProps.setProperty("usersequence", editItemsDS.getColumnValues("usersequence", ";"));
                        actionProps.setProperty("separator", ";");
                        actionProps.setProperty("tracelogid", SamplingPlanDetailItemPropertyHandler.getTraceLog(pl, actionProcessor));
                        actionProcessor.processAction("EditSDIDetail", "1", actionProps);
                        this.logInfo("Editing Items - End");
                    }
                    if (addDetailItemsDS.getRowCount() > 0) {
                        this.logInfo("Adding Detail Items - Begin");
                        actionProps.clear();
                        actionProps.setProperty("sdcid", "LV_SamplingPlan");
                        actionProps.setProperty("linkid", LINK_SPITEM);
                        actionProps.setProperty("detaillinkid", LINK_SPDETAILITEM);
                        actionProps.setProperty("s_samplingplanid", addDetailItemsDS.getColumnValues("s_samplingplanid", ";"));
                        actionProps.setProperty("s_samplingplanversionid", addDetailItemsDS.getColumnValues("s_samplingplanversionid", ";"));
                        actionProps.setProperty("s_samplingplanitemno", addDetailItemsDS.getColumnValues("s_samplingplanitemno", ";"));
                        actionProps.setProperty("s_samplingplandetailno", addDetailItemsDS.getColumnValues("s_samplingplandetailno", ";"));
                        actionProps.setProperty("separator", ";");
                        actionProps.setProperty("tracelogid", SamplingPlanDetailItemPropertyHandler.getTraceLog(pl, actionProcessor));
                        actionProcessor.processAction("AddSDIDetail", "1", actionProps);
                        this.logInfo("Adding Detail Items - End");
                    }
                }
            }
        }
        catch (Exception e) {
            this.logError("Error executing SamplingPlanDetailItemPropertyHandler", e);
            throw new SapphireException(e);
        }
    }

    private String getElementId(Map props) throws SapphireException {
        String elementId = null;
        try {
            Iterator it = props.keySet().iterator();
            while (it.hasNext()) {
                String value;
                String key = it.next().toString();
                if (!key.toLowerCase().startsWith(propHandlerPrefix) || !(value = props.get(key).toString()).equalsIgnoreCase(this.getClass().getName())) continue;
                elementId = key.substring(propHandlerPrefix.length());
                this.logDebug("elementId = " + elementId);
                this.logInfo(" elementId: " + elementId);
            }
        }
        catch (Exception e) {
            throw new SapphireException("Could Not Retrieve Element ID", e);
        }
        finally {
            return elementId;
        }
    }

    private void insertNewDetailItem(DataSet insertDS, String spId, String spVerId, int detailNo, int itemNo) throws SapphireException {
        try {
            int row = insertDS.addRow();
            insertDS.setString(row, "s_samplingplanid", spId);
            insertDS.setString(row, "s_samplingplanversionid", spVerId);
            insertDS.setNumber(row, "s_samplingplandetailno", detailNo);
            insertDS.setNumber(row, "s_samplingplanitemno", itemNo);
        }
        catch (Exception e) {
            throw new SapphireException("Could not insert New Detail Item", e);
        }
    }

    private void deleteDetailItem(DataSet deleteDS, String spId, String spVerId, int detailNo, int itemNo) throws SapphireException {
        try {
            int row = deleteDS.addRow();
            deleteDS.setString(row, "s_samplingplanid", spId);
            deleteDS.setString(row, "s_samplingplanversionid", spVerId);
            deleteDS.setNumber(row, "s_samplingplandetailno", detailNo);
            deleteDS.setNumber(row, "s_samplingplanitemno", itemNo);
        }
        catch (Exception e) {
            throw new SapphireException("Could not delete Detail Item", e);
        }
    }

    private void deleteAllDetailItem(DataSet deleteDS, String spId, String spVerId, int itemNo) throws SapphireException {
        try {
            SafeSQL safeSQL = new SafeSQL();
            String sql = "SELECT s_samplingplanid, s_samplingplanversionid, s_samplingplanitemno, s_samplingplandetailno FROM s_spdetailitem WHERE s_samplingplanid = " + safeSQL.addVar(spId) + " AND s_samplingplanversionid = " + safeSQL.addVar(spVerId) + " AND s_samplingplanitemno = " + safeSQL.addVar(itemNo);
            DataSet allSPDetailItems = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            deleteDS.copyRow(allSPDetailItems, -1, 1);
        }
        catch (Exception e) {
            throw new SapphireException("Could Not delete All Detail Items", e);
        }
    }

    private void insertNewItem(DataSet insertDS, String spId, String spVerId, int itemNo, String sdcId, String keyId1, String keyId2, String keyId3, String userSeq) throws SapphireException {
        try {
            int row = insertDS.addRow();
            insertDS.setString(row, "s_samplingplanid", spId);
            insertDS.setString(row, "s_samplingplanversionid", spVerId);
            insertDS.setNumber(row, "s_samplingplanitemno", itemNo);
            insertDS.setString(row, "itemsdcid", sdcId);
            insertDS.setString(row, "itemkeyid1", keyId1);
            insertDS.setString(row, "itemkeyid2", keyId2);
            insertDS.setString(row, "itemkeyid3", keyId3);
            insertDS.setString(row, "usersequence", userSeq);
        }
        catch (Exception e) {
            throw new SapphireException("Could Not insert New Item", e);
        }
    }

    private void editItem(DataSet editDS, String spId, String spVerId, int itemNo, String userSeq) throws SapphireException {
        try {
            int row = editDS.addRow();
            editDS.setString(row, "s_samplingplanid", spId);
            editDS.setString(row, "s_samplingplanversionid", spVerId);
            editDS.setNumber(row, "s_samplingplanitemno", itemNo);
            editDS.setString(row, "usersequence", userSeq);
        }
        catch (Exception e) {
            throw new SapphireException("Could Not edit existing Item", e);
        }
    }

    private void deleteItem(DataSet deleteDS, String spId, String spVerId, int itemNo) throws SapphireException {
        try {
            int row = deleteDS.addRow();
            deleteDS.setString(row, "s_samplingplanid", spId);
            deleteDS.setString(row, "s_samplingplanversionid", spVerId);
            deleteDS.setNumber(row, "s_samplingplanitemno", itemNo);
        }
        catch (Exception e) {
            throw new SapphireException("Could Not delete Item", e);
        }
    }

    private static String getTraceLog(PropertyList props, ActionProcessor actionProcessor) throws SapphireException {
        String traceLogId = props.getProperty("tracelogid", "");
        if (traceLogId.length() == 0) {
            String samplingPlanId = props.getProperty("pr0_s_samplingplanid", "");
            String samplingPlanVersionId = props.getProperty("pr0_s_samplingplanversionid", "");
            PropertyList extraProps = new PropertyList(OpalUtil.parseExtraProps(props.getProperty("__pr_extraprops", "")));
            String reason = extraProps.getProperty("auditreason", "");
            String activity = extraProps.getProperty("auditactivity", "");
            String signedFlag = extraProps.getProperty("auditsignedflag", "");
            if (reason.length() > 0) {
                PropertyList tracelogprops = new PropertyList();
                tracelogprops.setProperty("sdcid", "LV_SamplingPlan");
                tracelogprops.setProperty("keyid1", samplingPlanId);
                tracelogprops.setProperty("keyid2", samplingPlanVersionId);
                tracelogprops.setProperty("description", "Added workitems");
                tracelogprops.setProperty("auditreason", reason);
                tracelogprops.setProperty("auditactivity", activity);
                tracelogprops.setProperty("auditsignedflag", signedFlag);
                try {
                    actionProcessor.processActionClass(AddSDITraceLog.class.getName(), tracelogprops, false);
                    traceLogId = tracelogprops.getProperty("tracelogid");
                }
                catch (Exception e) {
                    throw new SapphireException("DB_ACTION_FAILED", "Error adding tracelog", e);
                }
                props.setProperty("tracelogid", String.valueOf(traceLogId));
            }
        }
        return traceLogId;
    }
}

