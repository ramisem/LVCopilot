/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.data.adapter;

import com.labvantage.sapphire.pageelements.datachart.data.adapter.AbstractDataSetAdapter;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.SDISpecAdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.DataBindingMap;
import sapphire.action.ActionConstants;
import sapphire.util.DataSet;

public final class SDISpecAdapter
extends AbstractDataSetAdapter
implements ActionConstants {
    private static final int SDC_ID_INDEX = 0;
    private static final int KEY_ID1_INDEX = 1;
    private static final int KEY_ID2_INDEX = 2;
    private static final int KEY_ID3_INDEX = 3;
    private static final int PARAM_LIST_ID_INDEX = 4;
    private static final int PARAM_LIST_VERSION_ID_INDEX = 5;
    private static final int VARIANT_ID_INDEX = 6;
    private static final int DATA_SET_INDEX = 7;
    private static final int PARAM_ID_INDEX = 8;
    private static final int PARAM_TYPE_INDEX = 9;
    private static final int REPLICATE_ID_INDEX = 10;
    private final SDISpecAdapterConfiguration adapterConfiguration;

    public SDISpecAdapter(String connectionId, DataBindingMap dataBindingMap, SDISpecAdapterConfiguration adapterConfiguration) {
        super(connectionId, dataBindingMap);
        if (adapterConfiguration == null) {
            throw new IllegalArgumentException("Source configuration is null");
        }
        this.adapterConfiguration = adapterConfiguration;
    }

    @Override
    public void processDataSetAdapter(DataSet dataSet) {
        DataSet resultDataSet = new DataSet();
        this.addColumnsToDataSet(resultDataSet, dataSet);
        resultDataSet.addColumn("limittypeid", 0);
        resultDataSet.addColumn("operator1", 0);
        resultDataSet.addColumn("operator2", 0);
        resultDataSet.addColumn("value1", 0);
        resultDataSet.addColumn("value2", 0);
        String sdcId = this.adapterConfiguration.getSdcId();
        Object[] dataItemKeys = new Object[11];
        for (int i = 0; i < dataSet.getRowCount(); ++i) {
            dataItemKeys[0] = sdcId;
            dataItemKeys[1] = dataSet.getString(i, "keyid1");
            dataItemKeys[2] = dataSet.getString(i, "keyid2", "(null)");
            dataItemKeys[3] = dataSet.getString(i, "keyid3", "(null)");
            dataItemKeys[4] = dataSet.getString(i, "paramlistid");
            dataItemKeys[5] = dataSet.getString(i, "paramlistversionid");
            dataItemKeys[6] = dataSet.getString(i, "variantid");
            dataItemKeys[7] = Integer.parseInt(dataSet.getValue(i, "dataset", "1"));
            dataItemKeys[8] = dataSet.getString(i, "paramid");
            dataItemKeys[9] = dataSet.getString(i, "paramtype");
            dataItemKeys[10] = Integer.parseInt(dataSet.getValue(i, "replicateid", "1"));
            DataSet limitRows = this.getSpecLimits(dataItemKeys);
            if (limitRows.getRowCount() > 0) {
                int[] rowNums;
                for (int rowNum : rowNums = this.appendRows(resultDataSet, limitRows)) {
                    this.copyRowData(dataSet, i, resultDataSet, rowNum);
                }
                continue;
            }
            int rowNum = resultDataSet.addRow();
            this.copyRowData(dataSet, i, resultDataSet, rowNum);
        }
        this.setProcessedDataSet(resultDataSet);
    }

    private void addColumnsToDataSet(DataSet baseDataSet, DataSet addColumns) {
        for (int i = 0; i < addColumns.getColumnCount(); ++i) {
            String columnId = addColumns.getColumnId(i);
            baseDataSet.addColumn(columnId, addColumns.getColumnType(columnId));
        }
    }

    private int[] appendRows(DataSet baseDataSet, DataSet addRows) {
        int[] rowNumbers = new int[addRows.getRowCount()];
        for (int i = 0; i < addRows.getRowCount(); ++i) {
            int rowNum;
            rowNumbers[i] = rowNum = baseDataSet.addRow();
            this.copyRowData(addRows, i, baseDataSet, rowNum);
        }
        return rowNumbers;
    }

    private void copyRowData(DataSet source, int sourceRowNum, DataSet target, int targetRowNum) {
        for (int i = 0; i < source.getColumnCount(); ++i) {
            String columnId = source.getColumnId(i);
            target.setValue(targetRowNum, columnId, source.getValue(sourceRowNum, columnId));
        }
    }

    DataSet getSpecLimits(Object[] dataItemKeys) {
        String sql = "select slt.limittypeid, spl.operator1, spl.operator2, spl.value1, spl.value2 from sdidataitemspec dis, specparamitems spi, specparamlimits spl, speclimittype slt, sdidataitem i   where i.sdcid=? and i.keyid1=? and i.keyid2=? and i.keyid3=? and i.paramlistid=? and i.paramlistversionid=? and i.variantid=? and i.dataset=? and i.paramid=? and i.paramtype=? and i.replicateid=? and dis.sdcid=i.sdcid and dis.keyid1=i.keyid1 and dis.keyid2=i.keyid2 and dis.keyid3=i.keyid3 and dis.paramlistid=i.paramlistid and dis.paramlistversionid=i.paramlistversionid and dis.variantid=i.variantid and dis.dataset=i.dataset and dis.paramid=i.paramid and dis.paramtype=i.paramtype and dis.replicateid=i.replicateid and spi.specid=dis.specid and spi.specversionid=dis.specversionid and spi.paramlistid=dis.paramlistid and spi.paramlistversionid=dis.paramlistversionid and spi.variantid=dis.variantid and spi.paramid=dis.paramid and spi.paramtype=dis.paramtype and spl.specid=spi.specid and spl.specversionid=spi.specversionid and spl.paramlistid=spi.paramlistid and spl.paramlistversionid=spi.paramlistversionid and spl.variantid=spi.variantid and spl.paramid=spi.paramid and spl.paramtype=spi.paramtype and slt.specid=spi.specid and slt.specversionid=spi.specversionid and slt.limittypesequence=spl.limittypesequence";
        return this.getQueryProcessor().getPreparedSqlDataSet(sql, dataItemKeys);
    }
}

