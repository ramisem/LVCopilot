/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import com.labvantage.sapphire.actions.sdidata.BaseSDIDataEntryAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class ApplyDataSetLimits
extends BaseSDIDataEntryAction
implements sapphire.action.ApplyDataSetLimits {
    public static final String ID = "ApplyDataSetLimits";
    public static final String VERSION = "1";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.dataEntry(properties, false, true);
    }
}

