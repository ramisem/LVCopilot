/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import com.labvantage.sapphire.actions.sdidata.BaseSDIDataEntryAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class ApplyDataItemLimits
extends BaseSDIDataEntryAction
implements sapphire.action.ApplyDataItemLimits {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.dataEntry(properties, false, true);
    }
}

