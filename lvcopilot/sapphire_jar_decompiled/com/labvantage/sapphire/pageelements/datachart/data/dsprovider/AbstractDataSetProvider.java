/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.data.dsprovider;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.pageelements.datachart.data.dsprovider.DataSetProvider;
import sapphire.xml.PropertyList;

public abstract class AbstractDataSetProvider
extends BaseCustom
implements DataSetProvider {
    private final PropertyList outputProps;
    private String rSetId;

    AbstractDataSetProvider(String connectionId) {
        if (connectionId == null || connectionId.isEmpty()) {
            throw new IllegalArgumentException("Null or empty connection ID: " + connectionId);
        }
        this.setConnectionId(connectionId);
        this.outputProps = new PropertyList();
        this.rSetId = "";
    }

    public void setRSetId(String rSetId) {
        this.rSetId = rSetId;
    }

    @Override
    public PropertyList getOutputProps() {
        return this.outputProps;
    }

    @Override
    public String getRSetId() {
        return this.rSetId;
    }
}

