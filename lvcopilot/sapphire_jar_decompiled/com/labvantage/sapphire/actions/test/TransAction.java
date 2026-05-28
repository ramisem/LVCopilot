/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.test;

import com.labvantage.sapphire.actions.test.NewTransAction;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class TransAction
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54292 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String mode = properties.getProperty("mode");
        if ("pass".equals(mode)) {
            this.getActionProcessor().processActionClass(NewTransAction.class.getName(), new PropertyList(), true);
            this.database.executeSQL("INSERT INTO sysconfig ( propertyid, propertyvalue ) VALUES ( 'transaction', 'Y' )");
        } else if ("fail".equals(mode)) {
            this.getActionProcessor().processActionClass(NewTransAction.class.getName(), new PropertyList(), true);
            this.database.executeSQL("XXINSERT INTO sysconfig ( propertyid, propertyvalue ) VALUES ( 'transaction', 'Y' )");
        }
    }
}

