/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.json;

import java.util.ArrayList;
import java.util.Calendar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import sapphire.util.DataSet;
import sapphire.util.SDIRequest;

public class JSONUtil {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    public static String toJSONString(DataSet dataset) {
        return JSONUtil.toJSONObject(dataset).toString();
    }

    public static JSONObject toJSONObject(DataSet dataset) {
        return JSONUtil.toJSONObject(dataset, false);
    }

    public static JSONObject toJSONObject(DataSet dataset, String[] columns, boolean useDataTypes) {
        return JSONUtil.toJSONObject(dataset, columns, useDataTypes, true, false);
    }

    public static JSONObject toJSONObject(DataSet dataset, String[] columns, boolean useDataTypes, boolean includeClobs, boolean includeUnknows) {
        JSONObject jsonObj = new JSONObject();
        try {
            JSONObject colMap = new JSONObject();
            JSONArray typesArray = new JSONArray();
            for (int i = 0; i < columns.length; ++i) {
                int type = dataset.getColumnType(columns[i]);
                switch (type) {
                    case 0: {
                        colMap.put(columns[i], new Integer(i));
                        break;
                    }
                    case 3: {
                        if (!includeClobs) break;
                        colMap.put(columns[i], new Integer(i));
                        break;
                    }
                    case 1: {
                        colMap.put(columns[i], new Integer(i));
                        break;
                    }
                    case 2: {
                        colMap.put(columns[i], new Integer(i));
                        break;
                    }
                    default: {
                        if (!includeUnknows) break;
                        colMap.put(columns[i], new Integer(i));
                    }
                }
                if (!useDataTypes) continue;
                typesArray.put(i, type);
            }
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < dataset.getRowCount(); ++i) {
                JSONArray jsonObjRow = new JSONArray();
                block22: for (int c = 0; c < columns.length; ++c) {
                    if (useDataTypes) {
                        try {
                            switch (dataset.getColumnType(columns[c])) {
                                case 0: {
                                    jsonObjRow.put(c, dataset.getValue(i, columns[c]));
                                    break;
                                }
                                case 3: {
                                    if (!includeClobs) continue block22;
                                    jsonObjRow.put(c, dataset.getValue(i, columns[c]));
                                    break;
                                }
                                case 1: {
                                    double d = dataset.getDouble(i, columns[c]);
                                    if (d != -9.99999999E8) {
                                        jsonObjRow.put(c, d);
                                        break;
                                    }
                                    jsonObjRow.put(c, JSONObject.NULL);
                                    break;
                                }
                                case 2: {
                                    Calendar o = dataset.getCalendar(i, columns[c]);
                                    jsonObjRow.put(c, o != null ? Long.valueOf(o.getTimeInMillis()) : JSONObject.NULL);
                                    break;
                                }
                                default: {
                                    if (!includeUnknows) continue block22;
                                    jsonObjRow.put(c, dataset.getObject(i, columns[c]).toString());
                                    break;
                                }
                            }
                        }
                        catch (Exception exception) {}
                        continue;
                    }
                    switch (dataset.getColumnType(columns[c])) {
                        case 0: 
                        case 1: 
                        case 2: {
                            jsonObjRow.put(c, dataset.getValue(i, columns[c], ""));
                            continue block22;
                        }
                        case 3: {
                            if (!includeClobs) continue block22;
                            jsonObjRow.put(c, dataset.getValue(i, columns[c], ""));
                            continue block22;
                        }
                        default: {
                            if (!includeUnknows) continue block22;
                            jsonObjRow.put(c, dataset.getValue(i, columns[c], ""));
                        }
                    }
                }
                jsonArray.put(i, jsonObjRow);
            }
            jsonObj.put("columns", colMap);
            jsonObj.put("columncount", colMap.length());
            jsonObj.put("dataset", jsonArray);
            if (useDataTypes) {
                jsonObj.put("types", typesArray);
            }
        }
        catch (Exception e) {
            return null;
        }
        return jsonObj;
    }

    public static JSONObject toJSONObject(DataSet dataset, boolean useDataTypes) {
        return JSONUtil.toJSONObject(dataset, dataset.getColumns(), useDataTypes);
    }

    public static JSONArray toJSONArray(ArrayList list) {
        JSONArray jarray = new JSONArray();
        if (list != null) {
            for (int i = 0; i < list.size(); ++i) {
                jarray.put(list.get(i));
            }
        }
        return jarray;
    }

    public static JSONArray toJSONArray(String[] headerColids) {
        JSONArray jarray = new JSONArray();
        for (int i = 0; i < headerColids.length; ++i) {
            jarray.put(headerColids[i]);
        }
        return jarray;
    }

    public static SDIRequest getSDIRequest(String jsonRequest) {
        SDIRequest sdiRequest = new SDIRequest();
        try {
            JSONArray jsonArray;
            JSONObject jsonObject = new JSONObject(new JSONTokener(jsonRequest));
            sdiRequest.setSDCid(jsonObject.getString("sdcid"));
            sdiRequest.setKeyid1List(jsonObject.getString("keyid1list"));
            sdiRequest.setKeyid2List(jsonObject.getString("keyid2list"));
            sdiRequest.setKeyid3List(jsonObject.getString("keyid3list"));
            sdiRequest.setQueryid(jsonObject.getString("queryid"));
            if (!jsonObject.isNull("queryparams")) {
                jsonArray = jsonObject.getJSONArray("queryparams");
                String[] queryParams = new String[jsonArray.length()];
                for (int i = 0; i < jsonArray.length(); ++i) {
                    queryParams[i] = jsonArray.getString(i);
                }
                sdiRequest.setQueryParams(queryParams);
            }
            sdiRequest.setQueryFrom(jsonObject.getString("queryfrom"));
            sdiRequest.setQueryWhere(jsonObject.getString("querywhere"));
            sdiRequest.setQueryOrderBy(jsonObject.getString("queryorderby"));
            sdiRequest.setRsetid(jsonObject.getString("rsetid"));
            sdiRequest.setLockOption(jsonObject.getString("lockoption"));
            sdiRequest.setPrimaryLockOption(jsonObject.getString("primarylockoption"));
            sdiRequest.setDataLockOption(jsonObject.getString("datalockoption"));
            sdiRequest.setRetainRsetid(jsonObject.getString("retainrsetid").equals("Y"));
            sdiRequest.setRetrieve(jsonObject.getString("retrieve").equals("Y"));
            sdiRequest.setShowTemplates(jsonObject.getString("showtemplates"));
            sdiRequest.setPropsMatch(jsonObject.getString("propsmatch").equals("Y"));
            sdiRequest.setParamlistidList(jsonObject.getString("paramlistidlist"));
            sdiRequest.setParamlistversionidList(jsonObject.getString("paramlistversionidlist"));
            sdiRequest.setVariantidList(jsonObject.getString("variantidlist"));
            sdiRequest.setDatasetList(jsonObject.getString("datasetlist"));
            sdiRequest.setWorkitemidList(jsonObject.getString("workitemidlist"));
            sdiRequest.setWorkiteminstanceList(jsonObject.getString("workiteminstancelist"));
            sdiRequest.setRetrieveLimit(jsonObject.getInt("retrievelimit"));
            sdiRequest.setVersionStatus(jsonObject.getString("versionstatus"));
            sdiRequest.setExtendedDataTypes(jsonObject.getString("extendeddatatypes").equals("Y"));
            sdiRequest.setRetrieveMappedKey(jsonObject.getString("retrievemappedkey").equals("Y"));
            sdiRequest.setExtendedAudit(jsonObject.getString("extendedaudit").equals("Y"));
            sdiRequest.setShowHiddenRecords(jsonObject.getString("showhiddenrecords").equals("Y"));
            sdiRequest.setSecurityBypassCode(jsonObject.getInt("securitybypasscode"));
            if (!jsonObject.isNull("requestitems")) {
                jsonArray = jsonObject.getJSONArray("requestitems");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    sdiRequest.setRequestItem(jsonArray.getString(i));
                }
            }
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
        return sdiRequest;
    }

    public static String escape(String s) {
        if (s == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        block10: for (int i = 0; i < s.length(); ++i) {
            char ch = s.charAt(i);
            switch (ch) {
                case '\'': {
                    sb.append("\\'");
                    continue block10;
                }
                case '\\': {
                    sb.append("\\\\");
                    continue block10;
                }
                case '\b': {
                    sb.append("\\b");
                    continue block10;
                }
                case '\f': {
                    sb.append("\\f");
                    continue block10;
                }
                case '\n': {
                    sb.append("\\n");
                    continue block10;
                }
                case '\r': {
                    sb.append("\\r");
                    continue block10;
                }
                case '\t': {
                    sb.append("\\t");
                    continue block10;
                }
                case '/': {
                    sb.append("\\/");
                    continue block10;
                }
                default: {
                    if (ch >= '\u0000' && ch <= '\u001f') {
                        String ss = Integer.toHexString(ch);
                        sb.append("\\u");
                        for (int k = 0; k < 4 - ss.length(); ++k) {
                            sb.append('0');
                        }
                        sb.append(ss.toUpperCase());
                        continue block10;
                    }
                    sb.append(ch);
                }
            }
        }
        return sb.toString();
    }
}

