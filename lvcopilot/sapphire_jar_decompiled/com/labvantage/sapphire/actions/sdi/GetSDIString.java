/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.sapphire.actions.sdi.BaseGetSDI;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class GetSDIString
extends BaseGetSDI
implements sapphire.action.GetSDIString {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.getSDIValue(properties, 0);
    }
}

