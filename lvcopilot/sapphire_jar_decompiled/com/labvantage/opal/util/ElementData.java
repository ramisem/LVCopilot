/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.util;

import com.labvantage.opal.util.ElementColumns;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.util.HttpUtil;
import sapphire.util.Logger;
import sapphire.util.StringUtil;

public class ElementData
extends ArrayList {
    private ElementColumns __Ecolumns;
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private boolean jsonflag = true;
    public static final Pattern PATTERN_HEX4 = Pattern.compile("%u[\\w]{4}");
    public static final Pattern PATTERN_HEX2 = Pattern.compile("%[\\w]{2}");

    public ElementData(ElementColumns ecolumns, String str) {
        int index = 0;
        this.__Ecolumns = ecolumns;
        if (StringUtil.getLen(str) > 0L) {
            if (str.startsWith("[")) {
                try {
                    JSONArray jsonArray = new JSONArray(str);
                    for (int row = 0; row < jsonArray.length(); ++row) {
                        JSONObject jsonObject = jsonArray.getJSONObject(row);
                        HashMap<String, String> datamap = new HashMap<String, String>();
                        for (int col = 0; col < this.__Ecolumns.size(); ++col) {
                            String value;
                            String columnid = this.__Ecolumns.getColumnId(col);
                            String key = Integer.toString(col);
                            String string = value = jsonObject.has(key) ? jsonObject.getString(key) : "";
                            if ("{}".equals(value)) {
                                value = "";
                            }
                            datamap.put(columnid, value);
                        }
                        String key = Integer.toString(this.__Ecolumns.size());
                        datamap.put("__status", jsonObject.has(key) ? jsonObject.getString(key) : "");
                        super.add(index++, datamap);
                    }
                }
                catch (JSONException e) {
                    Logger.logError(e.getMessage(), e);
                }
            } else {
                this.jsonflag = false;
                String[] st = StringUtil.split(str, "|");
                for (int row = 0; row < st.length; ++row) {
                    HashMap<String, String> datamap = new HashMap<String, String>();
                    String[] data = StringUtil.split(st[row], ",");
                    for (int col = 0; col < this.__Ecolumns.size(); ++col) {
                        String columnid = this.__Ecolumns.getColumnId(col);
                        datamap.put(columnid, data[col]);
                    }
                    if (data.length > this.__Ecolumns.size()) {
                        datamap.put("__status", data[this.__Ecolumns.size()]);
                    }
                    super.add(index++, datamap);
                }
            }
        }
    }

    public List getColumnList() {
        return this.__Ecolumns;
    }

    public List getDataList(String columnid) {
        ArrayList datalist = new ArrayList();
        for (int i = 0; i < super.size(); ++i) {
            datalist.add(i, ((HashMap)super.get(i)).get(columnid));
        }
        return datalist;
    }

    public String getColumnData(int index, String columnid) {
        HashMap map;
        String value = "";
        if (index > -1 && index < this.size() && (map = (HashMap)super.get(index)) != null && map.containsKey(columnid)) {
            value = (String)map.get(columnid);
            if (!this.jsonflag) {
                value = ElementData.decodeHex4(value);
                value = HttpUtil.decodeURIComponent(value);
            }
        }
        if (value != null && value.contains(";")) {
            value = value.replaceAll(";", "#semicolon#");
        }
        return value;
    }

    public String getColumnDataBuffer(String columnid, String delimiter) {
        StringBuffer sb = new StringBuffer();
        if (this.getColumnList().contains(columnid)) {
            for (int i = 0; i < super.size(); ++i) {
                sb.append(((HashMap)super.get(i)).get(columnid)).append(delimiter);
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public String getColumnDataBufferWithMultiCharDelimeter(String columnid, String delimiter) {
        StringBuffer sb = new StringBuffer();
        if (this.getColumnList().contains(columnid)) {
            for (int i = 0; i < super.size(); ++i) {
                sb.append(((HashMap)super.get(i)).get(columnid)).append(delimiter);
            }
        }
        return sb.length() > 0 ? sb.substring(0, sb.length() - delimiter.length()) : sb.toString();
    }

    public String getColumnDataBuffer(String columnid, String delimiter, String status) {
        StringBuffer sb = new StringBuffer();
        if (this.getColumnList().contains(columnid)) {
            for (int i = 0; i < super.size(); ++i) {
                if (!this.getColumnData(i, "__status").equals(status)) continue;
                sb.append(this.getColumnData(i, columnid)).append(delimiter);
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - delimiter.length());
            }
        }
        return sb.toString();
    }

    public int getStatusRowCount(String status) {
        int count = 0;
        for (int i = 0; i < super.size(); ++i) {
            if (!this.getColumnData(i, "__status").equals(status)) continue;
            ++count;
        }
        return count;
    }

    public String getSequenceBuffer(String status, String delimiter) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < super.size(); ++i) {
            if (!this.getColumnData(i, "__status").equals(status)) continue;
            sb.append(i + 1).append(delimiter);
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - delimiter.length());
        }
        return sb.toString();
    }

    public String getSequenceBuffer() {
        return this.getSequenceBuffer(super.size());
    }

    public String getSequenceBuffer(int size) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < size; ++i) {
            sb.append(i + 1).append(";");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public String getRowDataBuffer(int row) {
        StringBuffer sb = new StringBuffer();
        for (String columnid : this.__Ecolumns) {
            String columntype = this.__Ecolumns.getColumnType(columnid);
            if (columntype.equals("D") || columntype.equals("N")) {
                sb.append(this.getColumnData(row, columnid)).append(",");
                continue;
            }
            sb.append("'").append(this.getColumnData(row, columnid)).append("',");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("ElementData\n");
        if (super.size() == 0) {
            sb.append("No Records Found.");
        } else {
            for (int i = 0; i < super.size(); ++i) {
                sb.append("\nRow: ").append(i);
                HashMap hm = (HashMap)super.get(i);
                for (String key : hm.keySet()) {
                    sb.append("\n\t").append(key).append(" - ").append(hm.get(key));
                }
            }
        }
        return sb.toString();
    }

    public boolean containsColumnData(HashMap columndatamap) {
        boolean flag = false;
        for (int i = 0; i < super.size(); ++i) {
            String s2;
            String columnid;
            String s1;
            int x = 0;
            HashMap thismap = (HashMap)this.get(i);
            Iterator iterator = columndatamap.keySet().iterator();
            while (iterator.hasNext() && (s1 = (String)thismap.get(columnid = (String)iterator.next())).equals(s2 = (String)columndatamap.get(columnid))) {
                if (++x != columndatamap.size()) continue;
                flag = true;
                i = super.size();
            }
        }
        return flag;
    }

    public String getKeyValuePair(int index) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < this.__Ecolumns.size(); ++i) {
            String columnid = this.__Ecolumns.getColumnId(i);
            sb.append(columnid).append(" = '");
            sb.append(this.getColumnData(index, columnid)).append("',");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public String getKeyValuePair(int rowindex, List excludekeylist) {
        StringBuffer sb = new StringBuffer();
        for (String columnid : this.__Ecolumns) {
            String value = this.getColumnData(rowindex, columnid);
            if (excludekeylist.contains(columnid)) continue;
            String columntype = this.__Ecolumns.getColumnType(columnid);
            if (columntype.equals("D") || columntype.equals("N")) {
                if (value.equals("")) {
                    sb.append(columnid).append(" = null,");
                    continue;
                }
                sb.append(columnid).append(" = ").append(value).append(",");
                continue;
            }
            sb.append(columnid).append(" = '").append(value).append("',");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public void setColumnValue(int row, String columnid, String value) {
        ((HashMap)this.get(row)).put(columnid, value);
    }

    public boolean removeColumn(String columnid) {
        if (this.__Ecolumns.contains(columnid)) {
            Iterator i = super.iterator();
            while (i.hasNext()) {
                ((HashMap)i.next()).remove(columnid);
            }
            return this.__Ecolumns.remove(columnid);
        }
        return false;
    }

    public boolean addColumn(String columnid) {
        return this.addColumn(columnid, "");
    }

    public boolean addColumn(String columnid, String str) {
        if (!this.__Ecolumns.contains(columnid)) {
            for (int i = 0; i < super.size(); ++i) {
                ((HashMap)super.get(i)).put(columnid, str);
            }
            this.__Ecolumns.add(columnid);
            return true;
        }
        return false;
    }

    public static String decodeHex4(String str) {
        Matcher m = PATTERN_HEX4.matcher(str);
        while (m.find()) {
            str = m.replaceFirst(String.valueOf((char)Integer.parseInt(str.substring(m.start() + 2, m.end()), 16)));
            m = PATTERN_HEX4.matcher(str);
        }
        return str;
    }
}

