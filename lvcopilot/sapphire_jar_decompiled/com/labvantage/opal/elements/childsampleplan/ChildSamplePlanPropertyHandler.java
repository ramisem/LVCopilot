/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.childsampleplan;

import com.labvantage.opal.elements.BasePropertyHandler;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.actions.sdi.AddSDIDetail;
import com.labvantage.sapphire.actions.sdi.DeleteSDI;
import com.labvantage.sapphire.actions.sdi.DeleteSDIDetail;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.actions.sdi.EditSDIDetail;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ChildSamplePlanPropertyHandler
extends BasePropertyHandler {
    static List<String> nonUpdateableColumnList = new ArrayList<String>();

    @Override
    public void processProperties(HashMap props) throws SapphireException {
        block39: {
            String elementid = (String)props.get("__propertyhandler_elementid");
            String sdcid = "LV_ChildSamplePlan";
            String keyid1 = (String)props.get("__" + elementid + "_keyid1");
            String keyid2 = (String)props.get("__" + elementid + "_keyid2");
            String data = (String)props.get("__" + elementid + "_data");
            DBUtil database = new DBUtil();
            database.setConnection(this.sapphireConnection);
            try {
                String serviceArray;
                HashMap<String, String> newItemMap = new HashMap<String, String>();
                if (StringUtil.getLen(keyid1) <= 0L || StringUtil.getLen(keyid2) <= 0L) break block39;
                if ("Y".equals(props.get("__" + elementid + "_embedflag"))) {
                    String markparentconsumedflag = (String)props.get("__" + elementid + "_markparentconsumedflag");
                    String quantityvalidationflag = (String)props.get("__" + elementid + "_quantityvalidationflag");
                    String useforaccessionflag = (String)props.get("__" + elementid + "_useforaccessionflag");
                    markparentconsumedflag = markparentconsumedflag == null ? "N" : "Y";
                    quantityvalidationflag = quantityvalidationflag == null ? "N" : "Y";
                    useforaccessionflag = useforaccessionflag == null ? "N" : "Y";
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select markparentconsumedflag, quantityvalidationflag, useforaccessionflag from s_childsampleplan where s_childsampleplanid = ? and s_childsampleplanversionid = ?", (Object[])new String[]{keyid1, keyid2});
                    if (!(ds == null || ds.size() <= 0 || markparentconsumedflag.equals(ds.getString(0, "markparentconsumedflag")) && quantityvalidationflag.equals(ds.getString(0, "quantityvalidationflag")) && useforaccessionflag.equals(ds.getString(0, "useforaccessionflag")))) {
                        PropertyList actionProps = new PropertyList();
                        actionProps.setProperty("sdcid", "LV_ChildSamplePlan");
                        actionProps.setProperty("keyid1", keyid1);
                        actionProps.setProperty("keyid2", keyid2);
                        actionProps.setProperty("markparentconsumedflag", markparentconsumedflag);
                        actionProps.setProperty("quantityvalidationflag", quantityvalidationflag);
                        actionProps.setProperty("useforaccessionflag", useforaccessionflag);
                        this.getActionProcessor().processActionClass(EditSDI.class.getName(), actionProps);
                    }
                }
                ArrayList<String> addIgnoreColumnList = new ArrayList<String>();
                addIgnoreColumnList.add("s_childsampleplanitemid");
                addIgnoreColumnList.add("parentitemid");
                addIgnoreColumnList.add("grandchildflag");
                ArrayList<String> editColumnList = new ArrayList<String>();
                DataSet columnds = this.getSdcProcessor().getTableColumnData("s_childsampleplanitem");
                for (int i = 0; i < columnds.size(); ++i) {
                    String columnid = columnds.getString(i, "columnid");
                    if (nonUpdateableColumnList.contains(columnid)) continue;
                    editColumnList.add(columnid);
                }
                String sequenceKey = new SimpleDateFormat("yyyy").format(new Date());
                ArrayList<String> existingPlanItems = new ArrayList<String>();
                StringBuilder sql = new StringBuilder();
                SafeSQL safeSQL = new SafeSQL();
                sql.append("select * from s_childsampleplanitem");
                sql.append(" where s_childsampleplanid = ").append(safeSQL.addVar(keyid1)).append(" and s_childsampleplanversionid = ").append(safeSQL.addVar(keyid2));
                sql.append(" order by usersequence");
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (ds != null) {
                    DataSet addDS = new DataSet();
                    DataSet editDS = new DataSet();
                    DataSet editSequenceDS = new DataSet();
                    String lastparentitemid = "";
                    HashMap<String, String> filter = new HashMap<String, String>();
                    JSONArray dataArray = new JSONArray(data);
                    for (int i = 0; i < dataArray.length(); ++i) {
                        JSONObject jsonObject = dataArray.getJSONObject(i);
                        boolean grandchild = "Y".equals(jsonObject.has("grandchildflag") ? jsonObject.getString("grandchildflag") : "");
                        String childsampleplanitemid = jsonObject.getString("s_childsampleplanitemid");
                        if (childsampleplanitemid.startsWith("NEWITEM")) {
                            String newchildsampleplanitemid = "CSPI-" + sequenceKey + "-" + StringUtil.padLeft(String.valueOf(this.getSequenceProcessor().getSequence("LV_ChildSamplePlanItem", sequenceKey)), 6, '0');
                            int row = addDS.addRow();
                            addDS.setString(row, "s_childsampleplanitemid", newchildsampleplanitemid);
                            addDS.setString(row, "usersequence", String.valueOf(i + 1));
                            addDS.setString(row, "activeflag", "Y");
                            if (grandchild) {
                                if (lastparentitemid.startsWith("NEWITEM")) {
                                    lastparentitemid = (String)newItemMap.get(lastparentitemid);
                                }
                                addDS.setString(row, "parentitemid", lastparentitemid);
                                newItemMap.put(childsampleplanitemid, newchildsampleplanitemid);
                            } else {
                                newItemMap.put(childsampleplanitemid, newchildsampleplanitemid);
                                addDS.setString(row, "parentitemid", "");
                            }
                            Iterator iterator = jsonObject.keys();
                            while (iterator.hasNext()) {
                                String string = (String)iterator.next();
                                if (addIgnoreColumnList.contains(string)) continue;
                                String value = jsonObject.getString(string);
                                addDS.setString(row, string, value == null ? "" : value);
                            }
                        } else {
                            existingPlanItems.add(childsampleplanitemid);
                            filter.put("s_childsampleplanitemid", childsampleplanitemid);
                            DataSet temp = ds.getFilteredDataSet(filter);
                            if (temp != null && temp.size() > 0) {
                                boolean editdata = false;
                                for (String string : editColumnList) {
                                    if (!jsonObject.has(string)) continue;
                                    String jsonvalue = jsonObject.getString(string);
                                    if (StringUtil.getLen(jsonvalue) == 0L) {
                                        jsonvalue = "";
                                    }
                                    if (jsonvalue.equals(temp.getValue(0, string, ""))) continue;
                                    editdata = true;
                                    break;
                                }
                                if (editdata) {
                                    int row = editDS.addRow();
                                    editDS.setString(row, "s_childsampleplanitemid", childsampleplanitemid);
                                    editDS.setString(row, "usersequence", String.valueOf(i + 1));
                                    for (String columnid2 : editColumnList) {
                                        if (!jsonObject.has(columnid2)) continue;
                                        String jsonvalue = jsonObject.getString(columnid2);
                                        if (StringUtil.getLen(jsonvalue) == 0L) {
                                            jsonvalue = "(null)";
                                        }
                                        editDS.setString(row, columnid2, jsonvalue);
                                    }
                                } else {
                                    String string;
                                    String tempusersequence = temp.getValue(i, "usersequence");
                                    if (!tempusersequence.equals(string = String.valueOf(i + 1))) {
                                        int row = editSequenceDS.addRow();
                                        editSequenceDS.setString(row, "s_childsampleplanitemid", childsampleplanitemid);
                                        editSequenceDS.setString(row, "usersequence", String.valueOf(i + 1));
                                    }
                                }
                            }
                        }
                        if (grandchild) continue;
                        lastparentitemid = childsampleplanitemid;
                    }
                    ArrayList<String> deletedItems = new ArrayList<String>();
                    for (int i = 0; i < ds.size(); ++i) {
                        if (existingPlanItems.contains(ds.getString(i, "s_childsampleplanitemid"))) continue;
                        deletedItems.add(ds.getString(i, "s_childsampleplanitemid"));
                    }
                    if (addDS.size() > 0) {
                        PropertyList actionProps = new PropertyList();
                        actionProps.setProperty("sdcid", sdcid);
                        actionProps.setProperty("linkid", "Child Sample Plan Items");
                        actionProps.setProperty("keyid1", keyid1);
                        actionProps.setProperty("keyid2", keyid2);
                        for (String columnid : addDS.getColumns()) {
                            actionProps.setProperty(columnid, addDS.getColumnValues(columnid, ";"));
                        }
                        this.getActionProcessor().processActionClass(AddSDIDetail.class.getName(), actionProps);
                    }
                    if (editDS.size() > 0) {
                        PropertyList actionProps = new PropertyList();
                        actionProps.setProperty("sdcid", sdcid);
                        actionProps.setProperty("linkid", "Child Sample Plan Items");
                        actionProps.setProperty("keyid1", keyid1);
                        actionProps.setProperty("keyid2", keyid2);
                        for (String columnid : editDS.getColumns()) {
                            actionProps.setProperty(columnid, editDS.getColumnValues(columnid, ";"));
                        }
                        this.getActionProcessor().processActionClass(EditSDIDetail.class.getName(), actionProps);
                    }
                    if (editSequenceDS.size() > 0) {
                        PropertyList actionProps = new PropertyList();
                        actionProps.setProperty("sdcid", sdcid);
                        actionProps.setProperty("linkid", "Child Sample Plan Items");
                        actionProps.setProperty("keyid1", keyid1);
                        actionProps.setProperty("keyid2", keyid2);
                        actionProps.setProperty("s_childsampleplanitemid", editSequenceDS.getColumnValues("s_childsampleplanitemid", ";"));
                        actionProps.setProperty("usersequence", editSequenceDS.getColumnValues("usersequence", ";"));
                        this.getActionProcessor().processActionClass(EditSDIDetail.class.getName(), actionProps);
                    }
                    if (deletedItems.size() > 0) {
                        for (String childsampleplanitemid : deletedItems) {
                            database.executePreparedUpdate("delete from s_childsampleplanworkitem where s_childsampleplanid = ? and s_childsampleplanversionid = ? and s_childsampleplanitemid = ?", new Object[]{keyid1, keyid2, childsampleplanitemid});
                        }
                        PropertyList actionProps = new PropertyList();
                        actionProps.setProperty("sdcid", sdcid);
                        actionProps.setProperty("keyid1", keyid1);
                        actionProps.setProperty("keyid2", keyid2);
                        actionProps.setProperty("linkid", "Child Sample Plan Items");
                        actionProps.setProperty("s_childsampleplanitemid", OpalUtil.toDelimitedString(deletedItems, ";"));
                        this.getActionProcessor().processActionClass(DeleteSDIDetail.class.getName(), actionProps);
                    }
                }
                if (StringUtil.getLen(serviceArray = (String)props.get("__servicearray_" + elementid)) <= 0L) break block39;
                try {
                    PropertyList actionProps = new PropertyList();
                    DataSet deleteds = new DataSet();
                    HashSet<String> deleteSet = new HashSet<String>();
                    JSONArray wiarray = new JSONArray(serviceArray);
                    for (int i = 0; i < wiarray.length(); ++i) {
                        String embedchildsampleplanversionid;
                        JSONObject o = wiarray.getJSONObject(i);
                        String childsampleplanitemid = o.getString("childsampleplanitemid");
                        if (StringUtil.getLen(childsampleplanitemid) <= 0L) continue;
                        String embedchildsampleplanid = o.has("embedchildsampleplanid") ? o.getString("embedchildsampleplanid") : "";
                        String string = embedchildsampleplanversionid = o.has("embedchildsampleplanversionid") ? o.getString("embedchildsampleplanversionid") : "";
                        if ("DELETECHILDPLAN".equals(childsampleplanitemid)) {
                            if (StringUtil.getLen(embedchildsampleplanid) <= 0L || StringUtil.getLen(embedchildsampleplanversionid) <= 0L) continue;
                            int row = deleteds.addRow();
                            deleteds.setString(row, "keyid1", embedchildsampleplanid);
                            deleteds.setString(row, "keyid2", embedchildsampleplanversionid);
                            continue;
                        }
                        String workitemid = o.getString("workitemid");
                        Object[] deleteKeys = new String[]{keyid1, keyid2, childsampleplanitemid};
                        if ("-1".equals(workitemid)) {
                            if (childsampleplanitemid.startsWith("NEWITEM") || database.getPreparedCount("select count(s_childsampleplanitemid) c from s_childsampleplanworkitem where s_childsampleplanid = ? and s_childsampleplanversionid = ? and s_childsampleplanitemid = ?", new Object[]{keyid1, keyid2, childsampleplanitemid}) <= 0) continue;
                            database.executePreparedUpdate("delete from s_childsampleplanworkitem where s_childsampleplanid = ? and s_childsampleplanversionid = ? and s_childsampleplanitemid = ?", deleteKeys);
                            continue;
                        }
                        if (childsampleplanitemid.startsWith("NEWITEM")) {
                            childsampleplanitemid = (String)newItemMap.get(childsampleplanitemid);
                        } else {
                            String key = keyid1 + keyid2 + childsampleplanitemid;
                            if (!deleteSet.contains(key)) {
                                deleteSet.add(key);
                                database.executePreparedUpdate("delete from s_childsampleplanworkitem where s_childsampleplanid = ? and s_childsampleplanversionid = ? and s_childsampleplanitemid = ?", deleteKeys);
                            }
                        }
                        String workitemversionid = o.getString("workitemversionid");
                        String string2 = o.getString("workiteminstance");
                        String applyonaddflag = o.getString("applyonaddflag");
                        actionProps.clear();
                        actionProps.setProperty("sdcid", sdcid);
                        actionProps.setProperty("keyid1", keyid1);
                        actionProps.setProperty("keyid2", keyid2);
                        actionProps.setProperty("linkid", "Child Sample Plan Items");
                        actionProps.setProperty("detaillinkid", "Child Sample Plan WorkItems");
                        actionProps.setProperty("s_childsampleplanitemid", childsampleplanitemid);
                        actionProps.setProperty("workitemid", workitemid);
                        actionProps.setProperty("workitemversionid", "C".equals(workitemversionid) ? "(null)" : workitemversionid);
                        actionProps.setProperty("workiteminstance", string2);
                        actionProps.setProperty("embedchildsampleplanid", embedchildsampleplanid);
                        actionProps.setProperty("embedchildsampleplanversionid", embedchildsampleplanversionid);
                        actionProps.setProperty("applyonaddflag", applyonaddflag);
                        actionProps.setProperty("activeflag", "Y");
                        actionProps.setProperty("usersequence", o.getString("usersequence"));
                        actionProps.setProperty("assigneddepartmentid", o.has("assigneddepartmentid") ? o.getString("assigneddepartmentid") : "");
                        this.getActionProcessor().processActionClass(AddSDIDetail.class.getName(), actionProps);
                    }
                    if (deleteds.size() > 0) {
                        PropertyList deleteProps = new PropertyList();
                        deleteProps.setProperty("sdcid", "LV_ChildSamplePlan");
                        deleteProps.setProperty("keyid1", deleteds.getColumnValues("keyid1", ";"));
                        deleteProps.setProperty("keyid2", deleteds.getColumnValues("keyid2", ";"));
                        this.getActionProcessor().processActionClass(DeleteSDI.class.getName(), deleteProps);
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            catch (JSONException e) {
                Logger.logError("Error parsing JSON string. " + e.getMessage());
                e.printStackTrace();
            }
            catch (SapphireException se) {
                se.printStackTrace(System.out);
            }
        }
    }

    static {
        nonUpdateableColumnList.add("s_childsampleplanid");
        nonUpdateableColumnList.add("s_childsampleplanversionid");
        nonUpdateableColumnList.add("s_childsampleplanitemid");
        nonUpdateableColumnList.add("usersequence");
        nonUpdateableColumnList.add("auditsequence");
        nonUpdateableColumnList.add("tracelogid");
        nonUpdateableColumnList.add("createdt");
        nonUpdateableColumnList.add("createby");
        nonUpdateableColumnList.add("createtool");
        nonUpdateableColumnList.add("moddt");
        nonUpdateableColumnList.add("modby");
        nonUpdateableColumnList.add("modtool");
    }
}

