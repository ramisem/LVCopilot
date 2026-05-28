/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import com.labvantage.sapphire.actions.sdidata.BaseSDIDataValue;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class GetDataSetString
extends BaseSDIDataValue
implements sapphire.action.GetDataSetString {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.getSDIDataValue(properties, "sdidata", 0);
    }
}

