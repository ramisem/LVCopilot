/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.data.dsprovider;

import com.labvantage.sapphire.pageelements.datachart.data.dsprovider.DataSetProvider;
import sapphire.xml.PropertyList;

public interface CustomDataSetProvider
extends DataSetProvider {
    public void setConnectionId(String var1);

    public void setInputProperties(PropertyList var1);

    public void setOutputProperties(PropertyList var1);
}

