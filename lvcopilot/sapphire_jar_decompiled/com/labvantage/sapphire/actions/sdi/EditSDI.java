/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.sapphire.actions.sdi.BaseSDIAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class EditSDI
extends BaseSDIAction
implements sapphire.action.EditSDI {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.editSDI("EditSDI", properties);
    }
}

