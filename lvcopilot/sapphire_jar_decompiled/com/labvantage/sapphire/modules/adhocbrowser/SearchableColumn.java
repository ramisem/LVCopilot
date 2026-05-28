/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

public class SearchableColumn {
    private String columnid;
    private String datatype = "C";
    private String columnlength;
    private String columndesc;
    private String columndefinition;

    public SearchableColumn(String columnid, String datatype, String columndesc, String columnlength) {
        this.columnid = columnid;
        this.datatype = datatype;
        this.columndesc = columndesc;
        this.columnlength = columnlength;
    }

    public SearchableColumn(String columnid, String datatype, String columndesc, String columnlength, String columndefinition) {
        this.columnid = columnid;
        this.datatype = datatype;
        this.columndesc = columndesc;
        this.columnlength = columnlength;
        this.columndefinition = columndefinition;
    }

    public String getColumnid() {
        return this.columnid;
    }

    public String getDatatype() {
        return this.datatype;
    }

    public String getColumnlength() {
        return this.columnlength;
    }

    public String getColumndesc() {
        return this.columndesc;
    }

    public String getColumndefinition() {
        return this.columndefinition;
    }

    public String getHibernateType() {
        String type = "string";
        if ("D".equals(this.datatype)) {
            type = "timestamp";
        } else if ("N".equals(this.datatype) || "R".equals(this.datatype)) {
            type = "big_decimal";
        }
        return type;
    }
}

