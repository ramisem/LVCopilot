/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.sapphire.actions.sdi.BaseSDIAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class SetSDINumber
extends BaseSDIAction
implements sapphire.action.SetSDINumber {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.setSDIValue("SetSDINumber", properties.getProperty("columnid"), properties.getProperty("value"), properties);
    }
}

