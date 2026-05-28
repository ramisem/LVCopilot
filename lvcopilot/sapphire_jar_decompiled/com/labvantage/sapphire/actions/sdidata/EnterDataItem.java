/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import com.labvantage.sapphire.actions.sdidata.BaseSDIDataEntryAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class EnterDataItem
extends BaseSDIDataEntryAction
implements sapphire.action.EnterDataItem {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.dataEntry(properties, false, false);
    }
}

