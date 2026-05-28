/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import com.labvantage.sapphire.actions.sdidata.BaseReleaseUnreleaseData;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class UnReleaseDataItem
extends BaseReleaseUnreleaseData
implements sapphire.action.UnReleaseDataItem {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.releaseData(properties, "N", true);
    }
}

