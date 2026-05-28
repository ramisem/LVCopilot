/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.test;

import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class NewTransAction
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54291 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.database.executeSQL("INSERT INTO sysconfig ( propertyid, propertyvalue ) VALUES ( 'newtransaction', 'Y' )");
    }
}

