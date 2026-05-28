/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.webservices.transport;

import com.labvantage.sapphire.webservices.transport.DataSetColumnTransportBean;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;

public class DataSetRowTransportBean
implements Serializable {
    private String[] values = new String[0];

    public DataSetRowTransportBean() {
    }

    public DataSetRowTransportBean(DataSetColumnTransportBean[] columns, HashMap rowvalues, boolean includeClobs, boolean includeUnknowns) {
        this.setRow(columns, rowvalues, includeClobs, includeUnknowns);
    }

    public String[] getValues() {
        return this.values;
    }

    public void setValues(String[] values) {
        this.values = values;
    }

    protected void setRow(DataSetColumnTransportBean[] columns, HashMap rowvalues, boolean includeClobs, boolean includeUnknowns) {
        this.values = new String[columns.length];
        block6: for (int colIndex = 0; colIndex < columns.length; ++colIndex) {
            String col = columns[colIndex].getColumnId();
            if (!rowvalues.containsKey(col)) continue;
            Object object = rowvalues.get(col);
            if (object == null) {
                this.values[colIndex] = "";
                continue;
            }
            switch (columns[colIndex].getType()) {
                case 0: {
                    this.values[colIndex] = object.toString();
                    continue block6;
                }
                case 1: {
                    this.values[colIndex] = object.toString();
                    continue block6;
                }
                case 2: {
                    this.values[colIndex] = "" + ((Calendar)object).getTimeInMillis();
                    continue block6;
                }
                case 3: {
                    if (!includeClobs) continue block6;
                    this.values[colIndex] = object.toString();
                    continue block6;
                }
                default: {
                    if (!includeUnknowns) continue block6;
                    this.values[colIndex] = object.toString();
                }
            }
        }
    }
}

