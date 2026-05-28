/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.sql.mss;

import com.labvantage.opal.sql.DBColumn;

public class DBColumnImpl
implements DBColumn {
    public static String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private String __ColumnID;
    private String __NullValue;
    private String __Alias;
    private boolean __NVLFlag;
    private boolean __DateColumn;
    private String __Prefix;

    public DBColumnImpl(String columnid, boolean datecolumn) {
        this(columnid, "", columnid, false);
        this.__DateColumn = datecolumn;
    }

    public DBColumnImpl(String columnid, String nullvalue) {
        this(columnid, nullvalue, columnid, true);
    }

    public DBColumnImpl(String columnid, String nullvalue, String alias, boolean nvl) {
        this.__ColumnID = columnid;
        this.__NullValue = nullvalue;
        this.__Alias = alias;
        this.__NVLFlag = nvl;
    }

    @Override
    public void setDateColumn(boolean dateColumn) {
        this.__DateColumn = dateColumn;
    }

    @Override
    public void setPrefix(String prefix) {
        this.__Prefix = prefix;
    }

    @Override
    public String getNvlImpl() {
        this.__NullValue = this.__NullValue.equals("") || this.__NullValue == null ? "null" : "'" + this.__NullValue + "'";
        if (this.__NVLFlag) {
            if (this.__Prefix != null) {
                return "ISNULL( " + this.__Prefix + "." + this.__ColumnID + ", " + this.__NullValue + " ) " + this.__Alias;
            }
            return "ISNULL( " + this.__ColumnID + ", " + this.__NullValue + " ) " + this.__Alias;
        }
        if (this.__Prefix != null) {
            if (this.__DateColumn) {
                return this.__Prefix + "." + this.__ColumnID;
            }
            return this.__Prefix + "." + this.__ColumnID;
        }
        if (this.__DateColumn) {
            return this.__ColumnID;
        }
        return this.__ColumnID;
    }

    @Override
    public String getLengthImpl() {
        if (this.__Prefix != null) {
            return "LEN( " + this.__Prefix + "." + this.__ColumnID + ")";
        }
        return "LEN( " + this.__ColumnID + ")";
    }
}

