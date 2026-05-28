/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.scheduleplan;

import com.labvantage.opal.elements.BasePropertyHandler;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.cmt.CheckOutSDI;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class PlanItemManagerPropertyHandler
extends BasePropertyHandler {
    private static final List<String> PREVENT_EDIT_COLUMN_LIST = new ArrayList<String>();

    @Override
    public void processProperties(HashMap sdiProps) throws SapphireException {
        Set entrySet;
        PropertyList props = new PropertyList(sdiProps);
        String elementId = props.getProperty("__propertyhandler_elementid");
        String datasetName = props.getProperty("__" + elementId + "_dataset");
        String oldRSetId = props.getProperty("__" + elementId + "_rsetid");
        int rows = props.size();
        PropertyListCollection columnCollection = this.getSdcProcessor().getColumns("SchedulePlanItem");
        StringBuilder deleteSDIKeyId1s = new StringBuilder();
        StringBuilder deleteSDIKeyId2s = new StringBuilder();
        StringBuilder addSDIKeyId1s = new StringBuilder();
        StringBuilder addSDIKeyId2s = new StringBuilder();
        HashMap<String, StringBuilder> addSDIColumnValues = new HashMap<String, StringBuilder>();
        StringBuilder editSDIKeyId1s = new StringBuilder();
        StringBuilder editSDIKeyId2s = new StringBuilder();
        HashMap<String, StringBuilder> editSDIColumnValues = new HashMap<String, StringBuilder>();
        HashMap checkOutSDIMap = new HashMap();
        for (int i = 0; i < rows; ++i) {
            Object keyId2;
            String keyId1;
            String keyIds = props.getProperty("__" + datasetName + i + "_key");
            if (keyIds.contains(";")) {
                keyId1 = keyIds.split(";")[0];
                keyId2 = keyIds.split(";")[1];
            } else {
                keyId1 = props.getProperty(datasetName + i + "_scheduleplanid");
                if (!keyId1.isEmpty()) {
                    keyId2 = new DecimalFormat("00000").format(this.getSequenceProcessor().getSequence("SchedulePlanItem", keyId1));
                    keyId2 = this.schedulePlanItemIdExists(keyId1, (String)keyId2);
                } else {
                    keyId2 = "";
                }
            }
            if (keyId1.isEmpty() || ((String)keyId2).isEmpty()) continue;
            String rowStatus = props.getProperty("__" + datasetName + i + "_rs");
            if (rowStatus.equals("U")) {
                editSDIKeyId1s.append(";").append(keyId1);
                editSDIKeyId2s.append(";").append((String)keyId2);
            } else if (rowStatus.equals("I")) {
                addSDIKeyId1s.append(";").append(keyId1);
                addSDIKeyId2s.append(";").append((String)keyId2);
            } else if (rowStatus.equals("D")) {
                deleteSDIKeyId1s.append(";").append(keyId1);
                deleteSDIKeyId2s.append(";").append((String)keyId2);
            }
            String unmanagedsdcflag = props.getProperty(datasetName + i + "_unmanagedsdcflag");
            if ("CO".equals(unmanagedsdcflag)) {
                String linksdcid = props.getProperty(datasetName + i + "_linksdcid");
                String linkkeyid1 = props.getProperty(datasetName + i + "_linkkeyid1");
                String linkkeyid2 = props.getProperty(datasetName + i + "_linkkeyid2");
                if (linksdcid.length() > 0 && linkkeyid1.length() > 0) {
                    Map map;
                    if (!checkOutSDIMap.containsKey(linksdcid)) {
                        checkOutSDIMap.put(linksdcid, new HashMap());
                    }
                    map.put("keyid1", (map = (Map)checkOutSDIMap.get(linksdcid)).containsKey("keyid1") ? (String)map.get("keyid1") + ";" + (String)linkkeyid1 : linkkeyid1);
                    if (linkkeyid2.length() > 0) {
                        map.put("keyid2", map.containsKey("keyid2") ? (String)map.get("keyid2") + ";" + linkkeyid2 : linkkeyid2);
                    }
                }
            }
            if (!rowStatus.equals("U") && !rowStatus.equals("I")) continue;
            Set propIdSet = props.keySet();
            for (Object propIdObj : propIdSet) {
                StringBuilder columnValues;
                String propId = propIdObj.toString();
                String value = props.getProperty(propId);
                if (!propId.startsWith(datasetName + i + "_")) continue;
                String column = propId.split(datasetName + i + "_")[1];
                if (rowStatus.equals("U")) {
                    if (PREVENT_EDIT_COLUMN_LIST.contains(column) || !this.columnExists(columnCollection, column)) continue;
                    columnValues = (StringBuilder)editSDIColumnValues.get(column);
                    if (columnValues == null) {
                        columnValues = new StringBuilder();
                        editSDIColumnValues.put(column, columnValues);
                    }
                    if (value.isEmpty()) {
                        value = "(null)";
                    }
                    columnValues.append(";").append(value);
                    continue;
                }
                if (!rowStatus.equals("I") || PREVENT_EDIT_COLUMN_LIST.contains(column) || !this.columnExists(columnCollection, column)) continue;
                columnValues = (StringBuilder)addSDIColumnValues.get(column);
                if (columnValues == null) {
                    columnValues = new StringBuilder();
                    addSDIColumnValues.put(column, columnValues);
                }
                columnValues.append(";").append(value);
            }
        }
        ActionBlock actionBlock = null;
        if (deleteSDIKeyId1s.length() > 0) {
            PropertyList deleteSDIProps = new PropertyList();
            deleteSDIProps.setProperty("sdcid", "SchedulePlanItem");
            deleteSDIProps.setProperty("keyid1", deleteSDIKeyId1s.substring(1));
            deleteSDIProps.setProperty("keyid2", deleteSDIKeyId2s.substring(1));
            actionBlock = new ActionBlock();
            actionBlock.setAction("DeletePlanItem", "DeleteSDI", "1", deleteSDIProps);
        }
        if (addSDIKeyId1s.length() > 0) {
            PropertyList addSDIProps = new PropertyList();
            addSDIProps.setProperty("sdcid", "SchedulePlanItem");
            addSDIProps.setProperty("keyid1", addSDIKeyId1s.substring(1));
            addSDIProps.setProperty("keyid2", addSDIKeyId2s.substring(1));
            addSDIProps.setProperty("convert_usertimezones", "Y");
            entrySet = addSDIColumnValues.entrySet();
            for (Map.Entry entry : entrySet) {
                addSDIProps.setProperty((String)entry.getKey(), ((StringBuilder)entry.getValue()).substring(1));
            }
            if (actionBlock == null) {
                actionBlock = new ActionBlock();
            }
            actionBlock.setAction("AddPlanItem", "AddSDI", "1", addSDIProps);
        }
        if (checkOutSDIMap.size() > 0) {
            String changerequestid = "";
            String checkedoutbydepartmentid = "";
            String changelogoptions = this.getConfigurationProcessor().getProfileProperty("changelogoptions");
            if (OpalUtil.isNotEmpty(changelogoptions)) {
                try {
                    JSONObject o = new JSONObject(changelogoptions);
                    changerequestid = o.has("changerequestid") ? o.getString("changerequestid") : "";
                    checkedoutbydepartmentid = o.has("checkedoutbydepartmentid") ? o.getString("checkedoutbydepartmentid") : "";
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            for (Map.Entry entry : checkOutSDIMap.entrySet()) {
                String sdcid = (String)entry.getKey();
                Map keys = (Map)entry.getValue();
                PropertyList checkOutProps = new PropertyList();
                checkOutProps.setProperty("sdcid", sdcid);
                checkOutProps.setProperty("keyid1", (String)keys.get("keyid1"));
                if (keys.containsKey("keyid2")) {
                    checkOutProps.setProperty("keyid2", (String)keys.get("keyid2"));
                }
                if (changerequestid.length() > 0) {
                    checkOutProps.setProperty("changerequestid", changerequestid);
                }
                if (checkedoutbydepartmentid.length() > 0) {
                    checkOutProps.setProperty("departmentid", checkedoutbydepartmentid);
                }
                this.getActionProcessor().processActionClass(CheckOutSDI.class.getName(), checkOutProps);
            }
        }
        if (editSDIKeyId1s.length() > 0) {
            PropertyList editSDIProps = new PropertyList();
            editSDIProps.setProperty("sdcid", "SchedulePlanItem");
            editSDIProps.setProperty("keyid1", editSDIKeyId1s.substring(1));
            editSDIProps.setProperty("keyid2", editSDIKeyId2s.substring(1));
            editSDIProps.setProperty("convert_usertimezones", "Y");
            entrySet = editSDIColumnValues.entrySet();
            for (Map.Entry entry : entrySet) {
                editSDIProps.setProperty((String)entry.getKey(), ((StringBuilder)entry.getValue()).substring(1));
            }
            if (actionBlock == null) {
                actionBlock = new ActionBlock();
            }
            actionBlock.setAction("EditPlanItem", "EditSDI", "1", editSDIProps);
        }
        if (actionBlock != null) {
            this.getActionProcessor().processActionBlock(actionBlock);
        }
        if (oldRSetId != null && !oldRSetId.isEmpty()) {
            this.getDamProcessor().clearRSet(oldRSetId);
        }
    }

    private boolean columnExists(PropertyListCollection columnCollection, String column) {
        boolean exists = false;
        for (int i = 0; i < columnCollection.size(); ++i) {
            PropertyList columnProps = columnCollection.getPropertyList(i);
            String columnId = columnProps.getProperty("columnid");
            if (!columnId.equals(column)) continue;
            exists = true;
            break;
        }
        return exists;
    }

    private String schedulePlanItemIdExists(String keyId1, String keyId2) {
        String sql = "SELECT * from SchedulePlanItem where SCHEDULEPLANID=? AND SCHEDULEPLANITEMID=?";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{keyId1, keyId2});
        if (ds != null && ds.getRowCount() > 0) {
            keyId2 = new DecimalFormat("00000").format(this.getSequenceProcessor().getSequence("SchedulePlanItem", keyId1));
            this.schedulePlanItemIdExists(keyId1, keyId2);
        }
        return keyId2;
    }

    static {
        PREVENT_EDIT_COLUMN_LIST.add("scheduleplanid");
        PREVENT_EDIT_COLUMN_LIST.add("scheduleplanitemid");
        PREVENT_EDIT_COLUMN_LIST.add("createdt");
        PREVENT_EDIT_COLUMN_LIST.add("moddt");
        PREVENT_EDIT_COLUMN_LIST.add("createby");
        PREVENT_EDIT_COLUMN_LIST.add("modby");
        PREVENT_EDIT_COLUMN_LIST.add("lastscheduleddt");
        PREVENT_EDIT_COLUMN_LIST.add("scheduledto");
        PREVENT_EDIT_COLUMN_LIST.add("usersequence");
    }
}

