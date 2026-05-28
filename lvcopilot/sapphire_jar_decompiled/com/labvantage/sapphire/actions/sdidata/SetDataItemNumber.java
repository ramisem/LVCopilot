/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import com.labvantage.sapphire.actions.sdidata.BaseSDIDataAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class SetDataItemNumber
extends BaseSDIDataAction
implements sapphire.action.SetDataItemNumber {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.setSDIDataValue(properties, "sdidataitem");
    }
}

