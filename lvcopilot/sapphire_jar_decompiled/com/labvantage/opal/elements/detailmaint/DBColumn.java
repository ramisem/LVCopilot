/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.detailmaint;

public class DBColumn {
    public static String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private String __ColumnID;
    private String __NullValue;
    private String __Alias;
    private boolean __NVLFlag;
    private boolean __DateColumn;
    private String __Prefix;
    private boolean __Oracle;

    public DBColumn(String columnid, boolean datecolumn, boolean isora) {
        this(columnid, "", columnid, false, isora);
        this.__DateColumn = datecolumn;
    }

    public DBColumn(String columnid, String nullvalue, boolean isora) {
        this(columnid, nullvalue, columnid, true, isora);
    }

    public DBColumn(String columnid, String nullvalue, String alias, boolean nvl, boolean isora) {
        this.__ColumnID = columnid;
        this.__NullValue = nullvalue;
        this.__Alias = alias;
        this.__NVLFlag = nvl;
        this.setOracle(isora);
    }

    public void setDateColumn(boolean dateColumn) {
        this.__DateColumn = dateColumn;
    }

    public void setPrefix(String prefix) {
        this.__Prefix = prefix;
    }

    public String getNvlImpl() {
        if (this.isOracle()) {
            if (this.__NVLFlag) {
                if (this.__Prefix != null) {
                    return "NVL( " + this.__Prefix + "." + this.__ColumnID + ", '" + this.__NullValue + "' ) " + this.__Alias;
                }
                return "NVL( " + this.__ColumnID + ", '" + this.__NullValue + "' ) " + this.__Alias;
            }
            if (this.__Prefix != null) {
                if (this.__DateColumn) {
                    return "to_char( " + this.__Prefix + "." + this.__ColumnID + ", 'MM/DD/YY HH12:MI:SS AM') " + this.__ColumnID;
                }
                return this.__Prefix + "." + this.__ColumnID;
            }
            if (this.__DateColumn) {
                return "to_char( " + this.__ColumnID + ", 'MM/DD/YY HH12:MI:SS AM') " + this.__ColumnID;
            }
            return this.__ColumnID;
        }
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

    public String getLengthImpl() {
        if (this.__Prefix != null) {
            return "LENGTH( " + this.__Prefix + "." + this.__ColumnID + ")";
        }
        return "LENGTH( " + this.__ColumnID + ")";
    }

    public boolean isOracle() {
        return this.__Oracle;
    }

    public void setOracle(boolean __Oracle) {
        this.__Oracle = __Oracle;
    }
}

