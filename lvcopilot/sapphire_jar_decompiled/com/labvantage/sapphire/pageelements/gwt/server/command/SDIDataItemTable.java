/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.gwt.server.command;

import com.labvantage.sapphire.pageelements.gwt.server.command.Table;
import org.json.JSONObject;
import sapphire.util.DataSet;

public class SDIDataItemTable
extends Table {
    private Table dataentry;

    public Table getDataentry() {
        return this.dataentry;
    }

    public void setDataentry(Table dataentry) {
        this.dataentry = dataentry;
    }

    public SDIDataItemTable(String[] keyColumns) {
        super(keyColumns);
    }

    public SDIDataItemTable(String[] keyColumns, DataSet dataset) {
        super(keyColumns, dataset);
    }

    public SDIDataItemTable(String[] keyColumns, JSONObject jsonObject) {
        super(keyColumns, jsonObject);
        try {
            JSONObject dataentryJSON = jsonObject.getJSONObject("dataentry");
            if (dataentryJSON != null) {
                this.dataentry = new Table(keyColumns, dataentryJSON);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}

