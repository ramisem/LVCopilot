/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import com.labvantage.sapphire.actions.sdidata.BaseSDIDataAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class EditDataApproval
extends BaseSDIDataAction
implements sapphire.action.EditDataApproval {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.editSDIData(properties, "sdidataapproval");
    }
}

