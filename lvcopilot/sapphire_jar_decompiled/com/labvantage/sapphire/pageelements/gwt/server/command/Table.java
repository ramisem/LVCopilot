/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.gwt.server.command;

import com.labvantage.sapphire.util.json.JSONUtil;
import com.labvantage.sapphire.xml.Column;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class Table
extends DataSet {
    private HashSet<String> rowsModified = new HashSet();
    private HashSet<String> rowsAdded = new HashSet();
    private HashSet<String> rowsDeleted = new HashSet();
    private HashMap<String, Integer> rowMap = new HashMap();
    private String[] keyColumns;

    public Table(String[] keyColumns) {
        this.keyColumns = keyColumns;
    }

    public Table(String[] keyColumns, DataSet dataset) {
        this.keyColumns = keyColumns;
        this.setM18NUtil(dataset.getM18n());
        this.loadJSONObject(dataset.toJSONObject(true, dataset.getColumns(), false, true, true));
    }

    public Table(String[] keyColumns, JSONObject jsonObject) {
        this.keyColumns = keyColumns;
        this.loadJSONObject(jsonObject);
    }

    private void loadJSONObject(JSONObject jsonObject) {
        this.setJSONObject(jsonObject);
        try {
            int i;
            if (jsonObject.has("rowsmodified")) {
                JSONArray modified = jsonObject.getJSONArray("rowsmodified");
                for (i = 0; i < modified.length(); ++i) {
                    this.rowsModified.add(modified.getString(i));
                }
            }
            if (jsonObject.has("rowsadded")) {
                JSONArray added = jsonObject.getJSONArray("rowsadded");
                for (i = 0; i < added.length(); ++i) {
                    this.rowsAdded.add(added.getString(i));
                }
            }
            if (jsonObject.has("rowsdeleted")) {
                JSONArray deleted = jsonObject.getJSONArray("rowsdeleted");
                for (i = 0; i < deleted.length(); ++i) {
                    this.rowsDeleted.add(deleted.getString(i));
                }
            }
            if (jsonObject.has("keyindexmap")) {
                JSONObject keyindex = jsonObject.getJSONObject("keyindexmap");
                Iterator it = keyindex.keys();
                while (it.hasNext()) {
                    String keyString = (String)it.next();
                    this.rowMap.put(keyString, keyindex.getInt(keyString));
                }
            }
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
    }

    @Override
    public JSONObject toJSONObject() {
        try {
            JSONObject dataset = JSONUtil.toJSONObject(this);
            JSONArray modified = new JSONArray();
            int m = 0;
            for (String keyString : this.rowsModified) {
                modified.put(m++, keyString);
            }
            dataset.put("rowsmodified", modified);
            JSONArray added = new JSONArray();
            int a = 0;
            for (String keyString : this.rowsAdded) {
                added.put(a++, keyString);
            }
            dataset.put("rowsadded", added);
            JSONArray deleted = new JSONArray();
            int d = 0;
            for (String keyString : this.rowsDeleted) {
                deleted.put(d++, keyString);
            }
            dataset.put("rowsdeleted", deleted);
            JSONObject keyindex = new JSONObject();
            for (String keyString : this.rowMap.keySet()) {
                keyindex.put(keyString, this.rowMap.get(keyString));
            }
            dataset.put("keyindexmap", keyindex);
            return dataset;
        }
        catch (JSONException e) {
            return new JSONObject();
        }
    }

    @Override
    public String toJSONString() {
        return this.toJSONObject().toString();
    }

    public int addRow(String[] keyValues) {
        int row = super.addRow();
        this.addKeys(row, keyValues);
        return row;
    }

    private void addKeys(int row, String[] keyValues) {
        StringBuffer keyString = new StringBuffer();
        for (int i = 0; i < keyValues.length; ++i) {
            super.setValue(row, this.keyColumns[i], keyValues[i]);
            keyString.append(";").append(keyValues[i]);
        }
        this.rowsAdded.add(keyString.substring(1));
        this.rowMap.put(keyString.substring(1), row);
    }

    public void getSaveProps(String sdcid, boolean primary, String linkid, String detailLinkId, String[] keyCols, PropertyList editprops, PropertyList delprops, PropertyList addprops, String separator) throws SapphireException {
        HashMap<String, String> primaryKeyMap = null;
        if (primary) {
            primaryKeyMap = new HashMap<String, String>();
            for (int i = 0; i < keyCols.length; ++i) {
                primaryKeyMap.put(keyCols[i], "keyid" + (i + 1));
            }
        }
        if (this.rowsDeleted.size() > 0) {
            StringBuffer[] keyValues = new StringBuffer[keyCols.length];
            for (int i = 0; i < keyCols.length; ++i) {
                keyValues[i] = new StringBuffer();
            }
            Iterator<String> it = this.rowsDeleted.iterator();
            while (it.hasNext()) {
                String[] delKeyValues = it.next().split(separator);
                if (delKeyValues.length != keyCols.length) continue;
                for (int i = 0; i < keyCols.length; ++i) {
                    keyValues[i].append(separator).append(delKeyValues[i]);
                }
            }
            if (keyValues[0].length() > 0) {
                delprops.setProperty("sdcid", sdcid);
                if (linkid != null && linkid.length() > 0) {
                    delprops.setProperty("linkid", linkid);
                }
                if (detailLinkId != null && detailLinkId.length() > 0) {
                    delprops.setProperty("detaillinkid", detailLinkId);
                }
                for (int i = 0; i < keyCols.length; ++i) {
                    delprops.setProperty(primary ? "keyid" + (i + 1) : keyCols[i], keyValues[i].substring(separator.length()));
                }
            }
        }
        if (this.rowsAdded.size() > 0) {
            addprops.setProperty("sdcid", sdcid);
            if (linkid != null && linkid.length() > 0) {
                addprops.setProperty("linkid", linkid);
            }
            if (detailLinkId != null && detailLinkId.length() > 0) {
                addprops.setProperty("detaillinkid", detailLinkId);
            }
            Iterator<String> it = this.rowsAdded.iterator();
            while (it.hasNext()) {
                int row = this.rowMap.get(it.next());
                this.setRowProperties(row, addprops, primaryKeyMap, separator);
            }
            addprops.setProperty("copies", String.valueOf(this.rowsAdded.size()));
        }
        if (this.rowsModified.size() > 0) {
            editprops.setProperty("sdcid", sdcid);
            if (linkid != null && linkid.length() > 0) {
                editprops.setProperty("linkid", linkid);
            }
            if (detailLinkId != null && detailLinkId.length() > 0) {
                editprops.setProperty("detaillinkid", detailLinkId);
            }
            editprops.setProperty("propsmatch", "Y");
            for (String key : this.rowsModified) {
                if (this.rowMap.containsKey(key)) {
                    int row = this.rowMap.get(key);
                    this.setRowProperties(row, editprops, primaryKeyMap, separator);
                    continue;
                }
                throw new SapphireException("Missing key in rowMap - did you forget to add a row in a table without a key?");
            }
        }
    }

    private void setRowProperties(int row, PropertyList props, HashMap<String, String> primaryKeyMap, String separator) {
        String[] columns = this.getColumns();
        for (int i = 0; i < columns.length; ++i) {
            if (Column.isAuditColumn(columns[i])) continue;
            String value = this.getValue(row, columns[i]);
            if (primaryKeyMap != null && primaryKeyMap.containsKey(columns[i])) {
                columns[i] = primaryKeyMap.get(columns[i]);
            }
            if (value == null || value.length() == 0 || value.equals("(none)")) {
                value = "(null)";
            }
            if (value.indexOf(";") != -1) {
                value = value.replaceAll(";", "#semicolon#");
            }
            if (props.containsKey(columns[i])) {
                props.put(columns[i], props.get(columns[i]) + separator + value);
                continue;
            }
            props.put(columns[i], value);
        }
    }
}

