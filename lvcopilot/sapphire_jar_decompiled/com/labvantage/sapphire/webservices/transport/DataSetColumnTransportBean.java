/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.webservices.transport;

import java.io.Serializable;

public class DataSetColumnTransportBean
implements Serializable {
    private String columnId = "";
    private int type;
    private int length;

    public DataSetColumnTransportBean() {
        this.type = 0;
        this.length = 0;
    }

    public DataSetColumnTransportBean(String columnid, int type, int length) {
        type = 0;
        length = 0;
        this.setColumn(columnid, type, length);
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getColumnId() {
        return this.columnId;
    }

    public int getType() {
        return this.type;
    }

    public int getLength() {
        return this.length;
    }

    protected void setColumn(String columnid, int type, int length) {
        this.columnId = columnid;
        this.type = type;
        this.length = length;
    }
}

