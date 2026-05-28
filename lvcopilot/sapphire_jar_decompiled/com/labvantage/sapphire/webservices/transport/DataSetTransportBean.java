/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.webservices.transport;

import com.labvantage.sapphire.webservices.transport.DataSetColumnTransportBean;
import com.labvantage.sapphire.webservices.transport.DataSetRowTransportBean;
import java.io.Serializable;
import java.util.HashMap;
import sapphire.util.DataSet;

public class DataSetTransportBean
implements Serializable {
    private DataSetColumnTransportBean[] columns = new DataSetColumnTransportBean[0];
    private DataSetRowTransportBean[] rows = new DataSetRowTransportBean[0];
    private String id = "";

    public DataSetColumnTransportBean[] getColumns() {
        return this.columns;
    }

    public DataSetRowTransportBean[] getRows() {
        return this.rows;
    }

    public void setColumns(DataSetColumnTransportBean[] columns) {
        this.columns = columns;
    }

    public void setRows(DataSetRowTransportBean[] rows) {
        this.rows = rows;
    }

    public DataSetTransportBean() {
    }

    public DataSetTransportBean(DataSet data, boolean includeClobs, boolean includeUnknowns) {
        this.setDataSet(data, includeClobs, includeUnknowns);
    }

    public DataSet toDataSet() {
        return this.getDataSet();
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    protected DataSet getDataSet() {
        int i;
        DataSet data = new DataSet();
        data.setId(data.getId());
        for (i = 0; i < this.columns.length; ++i) {
            data.addColumn(this.columns[i].getColumnId(), this.columns[i].getType());
        }
        for (i = 0; i < this.rows.length; ++i) {
            int row = data.addRow();
            String[] values = this.rows[i].getValues();
            int l = values.length;
            if (this.columns.length < l) {
                l = this.columns.length;
            }
            for (int k = 0; k < l; ++k) {
                data.setValue(row, this.columns[k].getColumnId(), values[k]);
            }
        }
        return data;
    }

    protected void setDataSet(DataSet data, boolean includeClobs, boolean includeUnknowns) {
        if (data != null) {
            int iCL = data.getColumnCount();
            int iRL = data.getRowCount();
            this.columns = new DataSetColumnTransportBean[iCL];
            for (int iCol = 0; iCol < iCL; ++iCol) {
                DataSetColumnTransportBean dscb = new DataSetColumnTransportBean();
                String sCol = data.getColumnId(iCol);
                dscb.setColumn(sCol, data.getColumnType(sCol), data.getColumnLength(sCol));
                this.columns[iCol] = dscb;
            }
            this.rows = new DataSetRowTransportBean[iRL];
            for (int iRow = 0; iRow < iRL; ++iRow) {
                DataSetRowTransportBean dsrb = new DataSetRowTransportBean();
                dsrb.setRow(this.columns, (HashMap)data.get(iRow), includeClobs, includeUnknowns);
                this.rows[iRow] = dsrb;
            }
            this.setId(data.getId());
        } else {
            this.columns = new DataSetColumnTransportBean[0];
            this.rows = new DataSetRowTransportBean[0];
            this.id = "";
        }
    }
}

