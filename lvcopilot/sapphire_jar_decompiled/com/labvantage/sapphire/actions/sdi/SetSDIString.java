/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.sapphire.actions.sdi.BaseSDIAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class SetSDIString
extends BaseSDIAction
implements sapphire.action.SetSDIString {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.setSDIValue("SetSDIString", properties.getProperty("columnid"), properties.getProperty("value"), properties);
    }
}

