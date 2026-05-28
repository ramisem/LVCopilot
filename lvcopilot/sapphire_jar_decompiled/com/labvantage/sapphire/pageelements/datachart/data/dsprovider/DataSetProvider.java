/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.data.dsprovider;

import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public interface DataSetProvider {
    public DataSet getDataSet();

    public PropertyList getOutputProps();

    public String getRSetId();
}

