/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.data.adapter;

import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public interface DataSetAdapter {
    public void processDataSetAdapter(DataSet var1) throws SapphireException;

    public DataSet getProcessedDataSet();

    public PropertyList getOutputProperties();
}

