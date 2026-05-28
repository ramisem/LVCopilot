/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions;

import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class TestCaseSQL
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    public static final String ACTIONID = "TestCaseSQL";
    public static final String VERSION = "1";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.database.executeSQL(properties.getProperty("sql"));
    }
}

