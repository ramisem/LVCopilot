/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import com.labvantage.sapphire.actions.sdidata.BaseSDIDataAction;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.xml.PropertyList;

public class ExtendDataSet
extends BaseSDIDataAction
implements sapphire.action.ExtendDataSet {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        properties.setProperty("paramlistcheck", "N");
        ActionProcessor ap = this.getActionProcessor();
        ap.processAction("AddDataItem", "1", properties);
    }
}

