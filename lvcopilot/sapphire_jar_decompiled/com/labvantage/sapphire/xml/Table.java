/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.xml.Column;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.util.DataSet;

public class Table
extends DataSet {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private HashMap columns = new HashMap();
    private ArrayList keyColumns = new ArrayList();
    private String tableid;
    private String linkid;

    public Table(String tableid) {
        this.tableid = tableid;
    }

    public String getTableid() {
        return this.tableid;
    }

    public boolean isForceUpdate(String columnid) {
        Column c = (Column)this.columns.get(this.getColidCaseSensitive() ? columnid : columnid.toLowerCase());
        return c != null && c.isForceUpdate();
    }

    public boolean getForceNullUpdate(String columnid) {
        Column c = (Column)this.columns.get(this.getColidCaseSensitive() ? columnid : columnid.toLowerCase());
        return c != null && c.isForceNullUpdate();
    }

    public void addColumn(Column column) {
        this.columns.put(column.getColumnid(), column);
        this.addColumn(column.getColumnid(), column.getDatatype().equals("C") ? 0 : (column.getDatatype().equals("N") || column.getDatatype().equals("R") ? 1 : (column.getDatatype().equals("D") ? 2 : 3)), column.getColumnlength());
        if (column.isPrimarykey()) {
            this.keyColumns.add(column.getColumnid());
        }
    }

    public String[] getKeyColumns() {
        String[] keys = new String[this.keyColumns.size()];
        for (int i = 0; i < this.keyColumns.size(); ++i) {
            keys[i] = (String)this.keyColumns.get(i);
        }
        return keys;
    }

    public String getLinkid() {
        return this.linkid;
    }

    public void setLinkid(String linkid) {
        this.linkid = linkid;
    }
}

