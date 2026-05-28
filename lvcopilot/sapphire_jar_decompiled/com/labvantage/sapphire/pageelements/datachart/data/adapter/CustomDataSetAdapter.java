/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.data.adapter;

import com.labvantage.sapphire.pageelements.datachart.data.adapter.DataSetAdapter;
import sapphire.xml.PropertyList;

public interface CustomDataSetAdapter
extends DataSetAdapter {
    public void setConnectionId(String var1);

    public void setInputProperties(PropertyList var1);

    public void setOutputProperties(PropertyList var1);
}

