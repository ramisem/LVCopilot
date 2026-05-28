/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.clinicalevtsmptypdtl;

import com.labvantage.opal.elements.BasePropertyHandler;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.sdi.DeleteSDI;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ClinicalEventSampleTypePropertyHandler
extends BasePropertyHandler {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53675 $";
    private static final String PROPERTY_KEYID1 = "s_eventdefid";
    private static final String PREFIX_JSONSTRING = "__submitjsonstring_";
    public static final String COLUMN_DATATYPE_CHAR = "C";
    public static final String COLUMN_DATATYPE_DATE = "D";
    public static final String COLUMN_DATATYPE_NUMERIC = "N";
    public static final String COLUMN_DATATYPE_REAL = "R";

    @Override
    public void processProperties(HashMap props) throws SapphireException {
        DataSet assayTypeData;
        DataSet specimenDefData;
        DataSet sampleTypeData;
        TranslationProcessor tp = this.getTranslationProcessor();
        String propHandlerPrefix = "__propertyhandler_";
        String elementId = null;
        for (Object o : props.keySet()) {
            String value;
            String key = o.toString();
            if (!key.toLowerCase().startsWith(propHandlerPrefix) || !(value = props.get(key).toString()).equalsIgnoreCase(this.getClass().getName())) continue;
            elementId = key.substring(propHandlerPrefix.length());
            this.logDebug("elementId = " + elementId);
        }
        if (elementId == null || elementId.trim().length() == 0) {
            throw new SapphireException(tp.translate("Could not extract ElementId."));
        }
        PropertyList pl = new PropertyList(props);
        DBUtil db = new DBUtil();
        db.setConnection(this.sapphireConnection);
        String cpEventId = pl.getProperty("s_eventdefid_" + elementId, "");
        String jsonStr = pl.getProperty(PREFIX_JSONSTRING + elementId, "");
        if (cpEventId.length() == 0) {
            throw new SapphireException(tp.translate("EventDefId not found."));
        }
        if (jsonStr.length() == 0) {
            throw new SapphireException(tp.translate("Element JSON String not found."));
        }
        try {
            sampleTypeData = this.getSampleTypeDataSetFromJSONString(jsonStr);
            specimenDefData = this.getSpecimenDefDataSetFromJSONString(jsonStr);
            specimenDefData.sort("usersequence");
            assayTypeData = this.getAssayTypeDataSetFromJSONString(jsonStr);
        }
        catch (Exception e) {
            throw new SapphireException("ClinicalEventSampleTypePropertyHandler", tp.translate("Failed to get required datasets from json string.") + " " + tp.translate(e.getMessage()));
        }
        try {
            String serviceArray;
            String specimendefid;
            if (assayTypeData.size() > 0) {
                Trace.logInfo("ClinicalEventSampleTypePropertyHandler", "Manipulating data in s_eventdefstatmap");
                ArrayList<String> keys = new ArrayList<String>();
                keys.add(PROPERTY_KEYID1);
                keys.add("s_sampletypeid");
                keys.add("s_assaytypeid");
                keys.add("s_mapid");
                DataSet columnData = this.getSdcProcessor().getTableColumnData("s_eventdefstatmap");
                HashMap<String, String> columnMap = new HashMap<String, String>();
                for (int i = 0; i < columnData.size(); ++i) {
                    columnMap.put(columnData.getString(i, "columnid"), columnData.getString(i, "datatype"));
                }
                assayTypeData.sort("stateflag");
                ArrayList<DataSet> dataSetList = assayTypeData.getGroupedDataSets("stateflag");
                for (DataSet dataSet : dataSetList) {
                    String columnid;
                    int col;
                    if (dataSet.size() <= 0) continue;
                    if ("E".equals(dataSet.getString(0, "stateflag", ""))) {
                        DataSet dsupdate = new DataSet(this.connectionInfo);
                        for (col = 0; col < dataSet.getColumnCount(); ++col) {
                            String updatevaluelist;
                            columnid = dataSet.getColumnId(col);
                            if (!columnMap.containsKey(columnid)) continue;
                            String columndatatype = (String)columnMap.get(columnid);
                            if (columndatatype.equals(COLUMN_DATATYPE_CHAR)) {
                                updatevaluelist = dataSet.getColumnValues(columnid, ";");
                                if (!keys.contains(columnid)) {
                                    updatevaluelist = StringUtil.replaceAll(updatevaluelist, "(null)", "");
                                }
                                dsupdate.addColumnValues(columnid, 0, updatevaluelist, ";");
                                continue;
                            }
                            if (columndatatype.equals(COLUMN_DATATYPE_DATE)) {
                                dsupdate.addColumnValues(columnid, 2, dataSet.getColumnValues(columnid, ";"), ";");
                                continue;
                            }
                            if (!columndatatype.equals(COLUMN_DATATYPE_NUMERIC) && !columndatatype.equalsIgnoreCase(COLUMN_DATATYPE_REAL)) continue;
                            updatevaluelist = dataSet.getColumnValues(columnid, ";");
                            updatevaluelist = StringUtil.replaceAll(updatevaluelist, "(null)", "0");
                            dsupdate.addColumnValues(columnid, 1, updatevaluelist, ";");
                        }
                        if (dsupdate.size() <= 0) continue;
                        dsupdate.setDate(-1, "moddt", DateTimeUtil.getNowCalendar());
                        dsupdate.setString(-1, "modby", this.connectionInfo.getSysuserId());
                        dsupdate.setString(-1, "modtool", "EventPropertyHandler");
                        dsupdate.setString(-1, PROPERTY_KEYID1, cpEventId);
                        DataSetUtil.update(db, dsupdate, "s_eventdefstatmap", StringUtil.split(OpalUtil.toDelimitedString(keys, ";"), ";"));
                        continue;
                    }
                    if (COLUMN_DATATYPE_NUMERIC.equals(dataSet.getString(0, "stateflag", ""))) {
                        DataSet dsinsert = new DataSet(this.connectionInfo);
                        for (col = 0; col < dataSet.getColumnCount(); ++col) {
                            String insertvaluelist;
                            columnid = dataSet.getColumnId(col);
                            if (!columnMap.containsKey(columnid)) continue;
                            String columndatatype = (String)columnMap.get(columnid);
                            if (columndatatype.equals(COLUMN_DATATYPE_CHAR)) {
                                insertvaluelist = dataSet.getColumnValues(columnid, ";");
                                if (!keys.contains(columnid)) {
                                    insertvaluelist = StringUtil.replaceAll(insertvaluelist, "(null)", "");
                                }
                                dsinsert.addColumnValues(columnid, 0, insertvaluelist, ";");
                                continue;
                            }
                            if (columndatatype.equals(COLUMN_DATATYPE_DATE)) {
                                dsinsert.addColumnValues(columnid, 2, dataSet.getColumnValues(columnid, ";"), ";");
                                continue;
                            }
                            if (!columndatatype.equals(COLUMN_DATATYPE_NUMERIC) && !columndatatype.equalsIgnoreCase(COLUMN_DATATYPE_REAL)) continue;
                            insertvaluelist = dataSet.getColumnValues(columnid, ";");
                            insertvaluelist = StringUtil.replaceAll(insertvaluelist, "(null)", "0");
                            dsinsert.addColumnValues(columnid, 1, insertvaluelist, ";");
                        }
                        if (dsinsert.size() <= 0) continue;
                        String sequenceKey = "AT-" + new SimpleDateFormat("yyyy").format(new Date()) + "-";
                        StringBuilder detailkey = new StringBuilder();
                        for (int i = 0; i < dsinsert.size(); ++i) {
                            detailkey.append(OpalUtil.getNextSequence(sequenceKey, this.getSequenceProcessor())).append(";");
                        }
                        detailkey = detailkey.deleteCharAt(detailkey.length() - 1);
                        dsinsert.addColumnValues("s_mapid", 0, detailkey.toString(), ";");
                        dsinsert.setString(-1, PROPERTY_KEYID1, cpEventId);
                        dsinsert.setDate(-1, "createdt", DateTimeUtil.getNowCalendar());
                        dsinsert.setString(-1, "createby", this.connectionInfo.getSysuserId());
                        dsinsert.setString(-1, "createtool", "EventPropertyHandler");
                        dsinsert.setDate(-1, "moddt", DateTimeUtil.getNowCalendar());
                        dsinsert.setString(-1, "modby", this.connectionInfo.getSysuserId());
                        dsinsert.setString(-1, "modtool", "EventPropertyHandler");
                        DataSetUtil.insert(db, dsinsert, "s_eventdefstatmap");
                        continue;
                    }
                    if (!COLUMN_DATATYPE_DATE.equals(dataSet.getString(0, "stateflag", ""))) continue;
                    PreparedStatement deleteSpecimenDef = db.prepareStatement("deleteCPEventSTSpecimenDef", "DELETE FROM s_eventdefstatmap WHERE s_eventdefid = ? AND s_sampletypeid = ? AND s_assaytypeid = ? and s_mapid = ?");
                    for (int row = 0; row < dataSet.size(); ++row) {
                        deleteSpecimenDef.setString(1, cpEventId);
                        deleteSpecimenDef.setString(2, dataSet.getString(row, "s_sampletypeid"));
                        deleteSpecimenDef.setString(3, dataSet.getString(row, "s_assaytypeid"));
                        deleteSpecimenDef.setString(4, dataSet.getString(row, "s_mapid"));
                        deleteSpecimenDef.executeUpdate();
                    }
                }
            }
            ArrayList<String> newSpecimenDefList = new ArrayList<String>();
            if (specimenDefData.size() > 0) {
                Trace.logInfo("ClinicalEventSampleTypePropertyHandler", "Manipulating data in s_eventdefstspecimendef");
                ArrayList<String> keys = new ArrayList<String>();
                keys.add(PROPERTY_KEYID1);
                keys.add("s_sampletypeid");
                keys.add("s_specimendefid");
                DataSet columnData = this.getSdcProcessor().getTableColumnData("s_eventdefstspecimendef");
                HashMap<String, String> columnMap = new HashMap<String, String>();
                for (int i = 0; i < columnData.size(); ++i) {
                    columnMap.put(columnData.getString(i, "columnid"), columnData.getString(i, "datatype"));
                }
                specimenDefData.sort("stateflag");
                ArrayList<DataSet> dataSetList = specimenDefData.getGroupedDataSets("stateflag");
                for (DataSet ds : dataSetList) {
                    if (ds.size() <= 0) continue;
                    if ("E".equals(ds.getString(0, "stateflag", ""))) {
                        DataSet dsupdate = new DataSet(this.connectionInfo);
                        for (int col = 0; col < ds.getColumnCount(); ++col) {
                            String updatevaluelist;
                            String columnid = ds.getColumnId(col);
                            if (!columnMap.containsKey(columnid)) continue;
                            String columndatatype = (String)columnMap.get(columnid);
                            if (columndatatype.equals(COLUMN_DATATYPE_CHAR)) {
                                updatevaluelist = ds.getColumnValues(columnid, ";");
                                if (!keys.contains(columnid)) {
                                    updatevaluelist = StringUtil.replaceAll(updatevaluelist, "(null)", "");
                                }
                                dsupdate.addColumnValues(columnid, 0, updatevaluelist, ";");
                                continue;
                            }
                            if (columndatatype.equals(COLUMN_DATATYPE_DATE)) {
                                dsupdate.addColumnValues(columnid, 2, ds.getColumnValues(columnid, ";"), ";");
                                continue;
                            }
                            if (!columndatatype.equals(COLUMN_DATATYPE_NUMERIC) && !columndatatype.equalsIgnoreCase(COLUMN_DATATYPE_REAL)) continue;
                            updatevaluelist = ds.getColumnValues(columnid, ";");
                            updatevaluelist = StringUtil.replaceAll(updatevaluelist, "(null)", "0");
                            dsupdate.addColumnValues(columnid, 1, updatevaluelist, ";");
                        }
                        if (dsupdate.size() <= 0) continue;
                        for (int i = 0; i < dsupdate.size(); ++i) {
                            if (!COLUMN_DATATYPE_CHAR.equals(dsupdate.getValue(i, "childsampleplanversionid", ""))) continue;
                            dsupdate.setString(i, "childsampleplanversionid", "");
                        }
                        dsupdate.setDate(-1, "moddt", DateTimeUtil.getNowCalendar());
                        dsupdate.setString(-1, "modby", this.connectionInfo.getSysuserId());
                        dsupdate.setString(-1, "modtool", "EventPropertyHandler");
                        dsupdate.setString(-1, PROPERTY_KEYID1, cpEventId);
                        DataSetUtil.update(db, dsupdate, "s_eventdefstspecimendef", StringUtil.split(OpalUtil.toDelimitedString(keys, ";"), ";"));
                        continue;
                    }
                    if (COLUMN_DATATYPE_NUMERIC.equals(ds.getString(0, "stateflag", ""))) {
                        DataSet dsinsert = new DataSet(this.connectionInfo);
                        for (int col = 0; col < ds.getColumnCount(); ++col) {
                            String insertvaluelist;
                            String columnid = ds.getColumnId(col);
                            if (!columnMap.containsKey(columnid)) continue;
                            String columndatatype = (String)columnMap.get(columnid);
                            if (columndatatype.equals(COLUMN_DATATYPE_CHAR)) {
                                insertvaluelist = ds.getColumnValues(columnid, ";");
                                if (!keys.contains(columnid)) {
                                    insertvaluelist = StringUtil.replaceAll(insertvaluelist, "(null)", "");
                                }
                                dsinsert.addColumnValues(columnid, 0, insertvaluelist, ";");
                                continue;
                            }
                            if (columndatatype.equals(COLUMN_DATATYPE_DATE)) {
                                dsinsert.addColumnValues(columnid, 2, ds.getColumnValues(columnid, ";"), ";");
                                continue;
                            }
                            if (!columndatatype.equals(COLUMN_DATATYPE_NUMERIC) && !columndatatype.equalsIgnoreCase(COLUMN_DATATYPE_REAL)) continue;
                            insertvaluelist = ds.getColumnValues(columnid, ";");
                            insertvaluelist = StringUtil.replaceAll(insertvaluelist, "(null)", "0");
                            dsinsert.addColumnValues(columnid, 1, insertvaluelist, ";");
                        }
                        if (dsinsert.size() <= 0) continue;
                        String sequenceKey = "SDF-" + new SimpleDateFormat("yyyy").format(new Date()) + "-";
                        StringBuilder detailkey = new StringBuilder();
                        for (int i = 0; i < dsinsert.size(); ++i) {
                            detailkey.append(OpalUtil.getNextSequence(sequenceKey, this.getSequenceProcessor())).append(";");
                            if (!COLUMN_DATATYPE_CHAR.equals(dsinsert.getValue(i, "childsampleplanversionid", ""))) continue;
                            dsinsert.setString(i, "childsampleplanversionid", "");
                        }
                        detailkey = detailkey.deleteCharAt(detailkey.length() - 1);
                        dsinsert.addColumnValues("s_specimendefid", 0, detailkey.toString(), ";");
                        dsinsert.setString(-1, PROPERTY_KEYID1, cpEventId);
                        dsinsert.setDate(-1, "createdt", DateTimeUtil.getNowCalendar());
                        dsinsert.setString(-1, "createby", this.connectionInfo.getSysuserId());
                        dsinsert.setString(-1, "createtool", "EventPropertyHandler");
                        dsinsert.setDate(-1, "moddt", DateTimeUtil.getNowCalendar());
                        dsinsert.setString(-1, "modby", this.connectionInfo.getSysuserId());
                        dsinsert.setString(-1, "modtool", "EventPropertyHandler");
                        DataSetUtil.insert(db, dsinsert, "s_eventdefstspecimendef");
                        newSpecimenDefList.addAll(OpalUtil.toList(dsinsert.getColumnValues("s_specimendefid", ";"), ";"));
                        continue;
                    }
                    if (!COLUMN_DATATYPE_DATE.equals(ds.getString(0, "stateflag", ""))) continue;
                    PreparedStatement deleteSpecimenDefWI = db.prepareStatement("deleteCPEventSTSpecimenDefWI", "DELETE FROM s_eventdefstspecimendefwi WHERE s_eventdefid = ? AND s_sampletypeid = ? AND s_specimendefid = ? ");
                    PreparedStatement deleteSpecimenDef = db.prepareStatement("deleteCPEventSTSpecimenDef", "DELETE FROM s_eventdefstspecimendef WHERE s_eventdefid = ? AND s_sampletypeid = ? AND s_specimendefid = ? ");
                    for (int row = 0; row < ds.size(); ++row) {
                        String sampletypeid = ds.getString(row, "s_sampletypeid");
                        specimendefid = ds.getString(row, "s_specimendefid");
                        deleteSpecimenDefWI.setString(1, cpEventId);
                        deleteSpecimenDefWI.setString(2, sampletypeid);
                        deleteSpecimenDefWI.setString(3, specimendefid);
                        deleteSpecimenDefWI.executeUpdate();
                        deleteSpecimenDef.setString(1, cpEventId);
                        deleteSpecimenDef.setString(2, sampletypeid);
                        deleteSpecimenDef.setString(3, specimendefid);
                        deleteSpecimenDef.executeUpdate();
                    }
                }
            }
            if (sampleTypeData.getRowCount() > 0) {
                Trace.logInfo("ClinicalEventSampleTypePropertyHandler", "Manipulating data in s_eventdefsampletype");
                PreparedStatement deleteSampleType = db.prepareStatement("deleteCPEventSampletType", "DELETE FROM s_eventdefsampletype WHERE s_eventdefid = ? AND s_sampletypeid = ?");
                for (int row = 0; row < sampleTypeData.getRowCount(); ++row) {
                    if (!COLUMN_DATATYPE_DATE.equalsIgnoreCase(sampleTypeData.getString(row, "stateflag"))) continue;
                    deleteSampleType.setString(1, cpEventId);
                    deleteSampleType.setString(2, sampleTypeData.getString(row, "s_sampletypeid"));
                    deleteSampleType.executeUpdate();
                }
            }
            if (StringUtil.getLen(serviceArray = pl.getProperty("__servicearray_" + elementId)) > 0L) {
                JSONArray jsonArray = new JSONArray(serviceArray);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("rows", jsonArray);
                JSONObject cols = new JSONObject();
                cols.put(PROPERTY_KEYID1, 0);
                cols.put("s_specimendefid", 0);
                cols.put("s_sampletypeid", 0);
                cols.put("workitemid", 0);
                cols.put("workitemversionid", 0);
                cols.put("workiteminstance", 1);
                cols.put("usersequence", 1);
                cols.put("applyonaddflag", 0);
                cols.put("embedchildsampleplanid", 0);
                cols.put("embedchildsampleplanversionid", 0);
                cols.put("assigneddepartmentid", 0);
                jsonObject.put("columns", cols);
                DataSet dataSet = new DataSet(jsonObject);
                if (dataSet.size() > 0) {
                    String sampletypeid;
                    int i;
                    DataSet data = new DataSet();
                    DBUtil database = new DBUtil();
                    database.setConnection(this.sapphireConnection);
                    DataSet deleteds = new DataSet();
                    for (i = 0; i < dataSet.size(); ++i) {
                        String s_eventdefid = dataSet.getString(i, PROPERTY_KEYID1, "");
                        if ("DELETECHILDPLAN".equals(s_eventdefid)) {
                            String embedchildsampleplanid = dataSet.getString(i, "embedchildsampleplanid", "");
                            String embedchildsampleplanversionid = dataSet.getString(i, "embedchildsampleplanversionid", "");
                            if (StringUtil.getLen(embedchildsampleplanid) <= 0L || StringUtil.getLen(embedchildsampleplanversionid) <= 0L) continue;
                            int row = deleteds.addRow();
                            deleteds.setString(row, "keyid1", embedchildsampleplanid);
                            deleteds.setString(row, "keyid2", embedchildsampleplanversionid);
                            continue;
                        }
                        specimendefid = dataSet.getString(i, "s_specimendefid", "");
                        if ("NEW[row]".equals(specimendefid)) continue;
                        String workitemid = dataSet.getString(i, "workitemid", "");
                        if ("-1".equals(workitemid)) {
                            String eventdefid;
                            if (specimendefid.startsWith("NEW") || database.getPreparedCount("select count(s_specimendefid) from s_eventdefstspecimendefwi where s_eventdefid = ? and s_specimendefid = ? and s_sampletypeid = ?", new Object[]{eventdefid = dataSet.getString(i, PROPERTY_KEYID1), specimendefid, sampletypeid = dataSet.getString(i, "s_sampletypeid")}) <= 0) continue;
                            database.executePreparedUpdate("delete from s_eventdefstspecimendefwi where s_eventdefid = ? and s_specimendefid = ? and s_sampletypeid = ?", new Object[]{eventdefid, specimendefid, sampletypeid});
                            continue;
                        }
                        data.copyRow(dataSet, i, 1);
                    }
                    if (data.size() > 0) {
                        if (newSpecimenDefList.size() > 0) {
                            for (i = 0; i < data.size(); ++i) {
                                String specimendefid2 = data.getString(i, "s_specimendefid", "");
                                if (!specimendefid2.startsWith("NEW")) continue;
                                int index = Integer.parseInt(specimendefid2.substring(3));
                                data.setString(i, "s_specimendefid", (String)newSpecimenDefList.get(index));
                            }
                        }
                        data.sort("s_eventdefid,s_specimendefid,s_sampletypeid");
                        ArrayList<DataSet> list = data.getGroupedDataSets("s_eventdefid,s_specimendefid,s_sampletypeid");
                        for (DataSet d : list) {
                            String eventdefid = d.getString(0, PROPERTY_KEYID1);
                            String specimendefid3 = d.getString(0, "s_specimendefid");
                            sampletypeid = d.getString(0, "s_sampletypeid");
                            if (StringUtil.getLen(specimendefid3) <= 0L) continue;
                            StringBuilder sql = new StringBuilder();
                            SafeSQL safeSQL = new SafeSQL();
                            sql.append("delete from s_eventdefstspecimendefwi");
                            sql.append(" where s_eventdefid = ").append(safeSQL.addVar(eventdefid));
                            sql.append(" and s_specimendefid = ").append(safeSQL.addVar(specimendefid3));
                            sql.append(" and s_sampletypeid = ").append(safeSQL.addVar(sampletypeid));
                            database.executePreparedUpdate(sql.toString(), safeSQL.getValues());
                        }
                        data.setDate(-1, "createdt", DateTimeUtil.getNowCalendar());
                        data.setString(-1, "createby", this.connectionInfo.getSysuserId());
                        data.setString(0, "createtool", "detailmaint");
                        data.setDate(-1, "moddt", DateTimeUtil.getNowCalendar());
                        data.setString(-1, "modby", this.connectionInfo.getSysuserId());
                        data.setString(0, "modtool", "detailmaint");
                        for (int i2 = 0; i2 < data.size(); ++i2) {
                            if (!COLUMN_DATATYPE_CHAR.equals(data.getString(i2, "workitemversionid"))) continue;
                            data.setString(i2, "workitemversionid", "");
                        }
                        DataSetUtil.insert(db, data, "s_eventdefstspecimendefwi");
                    }
                    if (deleteds.size() > 0) {
                        PropertyList actionProps = new PropertyList();
                        actionProps.setProperty("sdcid", "LV_ChildSamplePlan");
                        actionProps.setProperty("keyid1", deleteds.getColumnValues("keyid1", ";"));
                        actionProps.setProperty("keyid2", deleteds.getColumnValues("keyid2", ";"));
                        this.getActionProcessor().processActionClass(DeleteSDI.class.getName(), actionProps);
                    }
                }
            }
        }
        catch (Exception ex) {
            Logger.logError("ClinicalEventSampleTypePropertyHandler", ex.getMessage());
            throw new SapphireException("ClinicalEventSampleTypePropertyHandler", tp.translate("Failed to Save Data"));
        }
        finally {
            db.closePreparedStatements();
        }
    }

    private DataSet getSampleTypeDataSetFromJSONString(String jsonString) throws Exception {
        DataSet ds = new DataSet();
        ds.addColumn("s_sampletypeid", 0);
        ds.addColumn("stateflag", 0);
        JSONObject jsonObject = null;
        JSONArray jsonArray = new JSONArray(jsonString);
        for (int i = 0; i < jsonArray.length(); ++i) {
            jsonObject = jsonArray.getJSONObject(i);
            int newRow = ds.addRow();
            for (int j = 0; j < ds.getColumnCount(); ++j) {
                ds.setValue(newRow, ds.getColumnId(j), jsonObject.getString(ds.getColumnId(j)));
            }
        }
        return ds;
    }

    private DataSet getAssayTypeDataSetFromJSONString(String jsonString) throws Exception {
        DataSet ds = new DataSet();
        JSONArray jsonArray = new JSONArray(jsonString);
        for (int i = 0; i < jsonArray.length(); ++i) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            JSONObject assayTypeArrayObject = jsonObject.getJSONObject("assaytypearray");
            JSONArray jsonAssayTypeNameArray = assayTypeArrayObject.names();
            if (jsonAssayTypeNameArray == null) continue;
            for (int k = 0; k < jsonAssayTypeNameArray.length(); ++k) {
                JSONObject jsonAssayTypeObject = assayTypeArrayObject.getJSONObject(jsonAssayTypeNameArray.getString(k));
                int newRow = ds.addRow();
                ds.setString(newRow, "s_sampletypeid", jsonObject.getString("s_sampletypeid"));
                JSONArray columnArray = jsonAssayTypeObject.names();
                for (int col = 0; col < columnArray.length(); ++col) {
                    String columnid = columnArray.getString(col);
                    if ("agrowindex".equals(columnid)) continue;
                    ds.setString(newRow, columnid, jsonAssayTypeObject.getString(columnid));
                }
            }
        }
        return ds;
    }

    private DataSet getSpecimenDefDataSetFromJSONString(String jsonString) throws Exception {
        DataSet ds = new DataSet();
        JSONArray jsonArray = new JSONArray(jsonString);
        for (int i = 0; i < jsonArray.length(); ++i) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            JSONObject specimenDefArrayObject = jsonObject.getJSONObject("specimendefarray");
            JSONArray jsonSpecimenDefNameArray = specimenDefArrayObject.names();
            if (jsonSpecimenDefNameArray == null) continue;
            for (int k = 0; k < jsonSpecimenDefNameArray.length(); ++k) {
                JSONObject jsonSpecimenDefObject = specimenDefArrayObject.getJSONObject(jsonSpecimenDefNameArray.getString(k));
                int newRow = ds.addRow();
                ds.setString(newRow, "s_sampletypeid", jsonObject.getString("s_sampletypeid"));
                JSONArray columnArray = jsonSpecimenDefObject.names();
                for (int col = 0; col < columnArray.length(); ++col) {
                    String columnid = columnArray.getString(col);
                    if ("agrowindex".equals(columnid)) continue;
                    ds.setString(newRow, columnid, jsonSpecimenDefObject.getString(columnid));
                }
            }
        }
        return ds;
    }
}

