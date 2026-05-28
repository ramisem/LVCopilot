/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.util;

import com.labvantage.sapphire.services.ConnectionInfo;
import java.io.File;
import sapphire.util.DataSet;
import sapphire.util.ResultGridOptions;

public class ResultDataGrid
extends sapphire.util.ResultDataGrid {
    public ResultDataGrid(ConnectionInfo connectionInfo) {
        super(connectionInfo);
    }

    public ResultDataGrid(ConnectionInfo connectionInfo, File rakFile) {
        super(connectionInfo, rakFile);
    }

    @Override
    public ResultGridOptions getOptions() {
        return super.getOptions();
    }

    @Override
    public void setOptions(ResultGridOptions resultGridOptions) {
        super.setOptions(resultGridOptions);
    }

    @Override
    public DataSet getDataSet() {
        return super.getDataSet();
    }

    @Override
    public void setDataSet(DataSet grid) {
        super.setDataSet(grid);
    }
}

