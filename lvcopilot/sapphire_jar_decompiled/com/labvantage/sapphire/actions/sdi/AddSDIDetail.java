/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.sapphire.actions.sdi.BaseSDILinkAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class AddSDIDetail
extends BaseSDILinkAction
implements sapphire.action.AddSDIDetail {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.maintainSDIDetail("AddSDIDetail", properties);
    }
}

