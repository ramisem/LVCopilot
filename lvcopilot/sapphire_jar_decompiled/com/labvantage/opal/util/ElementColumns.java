/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.util;

import com.labvantage.opal.util.OpalUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import sapphire.util.StringUtil;

public class ElementColumns
extends ArrayList {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private HashMap __ColumnTypeMap = new HashMap();

    public ElementColumns(String str, String delimiter) {
        if (str != null && str.length() > 0) {
            String[] st = StringUtil.split(str, delimiter);
            for (int i = 0; i < st.length; ++i) {
                super.add(i, st[i]);
                this.__ColumnTypeMap.put(st[i], "C");
            }
        }
    }

    public ElementColumns(String str) {
        this(str, "|");
    }

    public String getColumnId(int index) {
        return (String)super.get(index);
    }

    public int getColumnIndex(String columnid) {
        return super.indexOf(columnid);
    }

    public String getColumnIdBuffer() {
        return this.getColumnIdBuffer(null, ",");
    }

    public String getColumnIdBuffer(String prefix, String delimiter) {
        StringBuffer sb = new StringBuffer();
        if (prefix == null || prefix.length() == 0) {
            prefix = "";
        }
        for (int i = 0; i < super.size(); ++i) {
            sb.append(prefix).append(this.getColumnId(i)).append(delimiter);
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public boolean containsColumn(String columnid) {
        return super.contains(columnid);
    }

    public List getExcludedColumnList(String columnbuffer) {
        ArrayList<String> columnlist = new ArrayList<String>();
        List excludeList = OpalUtil.stringArrayToList(StringUtil.split(columnbuffer, ";"));
        for (int i = 0; i < this.size(); ++i) {
            if (excludeList.contains(this.getColumnId(i))) continue;
            columnlist.add(this.getColumnId(i));
        }
        return columnlist;
    }

    public List getExcludedColumnList(List coreColumnsList) {
        ArrayList<String> columnlist = new ArrayList<String>();
        for (int i = 0; i < this.size(); ++i) {
            if (coreColumnsList.contains(this.getColumnId(i))) continue;
            columnlist.add(this.getColumnId(i));
        }
        return columnlist;
    }

    public List getExcludedColumnList(String columnbuffer, String delimiter) {
        ArrayList<String> columnlist = new ArrayList<String>();
        List excludeList = OpalUtil.stringArrayToList(StringUtil.split(columnbuffer, delimiter));
        for (int i = 0; i < this.size(); ++i) {
            if (excludeList.contains(this.getColumnId(i))) continue;
            columnlist.add(this.getColumnId(i));
        }
        return columnlist;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("ElementColumns");
        sb.append("\nSIZE: " + super.size());
        for (int i = 0; i < super.size(); ++i) {
            sb.append("\nColumn ID: " + this.getColumnId(i));
        }
        return sb.toString();
    }

    @Override
    public boolean remove(Object key) {
        if (super.remove(key)) {
            this.__ColumnTypeMap.remove(key);
            return true;
        }
        return false;
    }

    @Override
    public Object remove(int index) {
        this.__ColumnTypeMap.remove(super.get(index));
        return super.remove(index);
    }

    public void setColumnType(String columnid, String type) {
        this.__ColumnTypeMap.put(columnid, type);
    }

    public String getColumnType(String columnid) {
        return (String)this.__ColumnTypeMap.get(columnid);
    }
}

