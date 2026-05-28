/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.sapphire.actions.sdi.BaseSDILinkAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class EditSDIDetail
extends BaseSDILinkAction
implements sapphire.action.EditSDIDetail {
    public static final String ID = "EditSDIDetail";
    public static final String VERSION = "1";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.maintainSDIDetail(ID, properties);
    }
}

