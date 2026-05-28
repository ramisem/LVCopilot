/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class LV_PrepType
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54000 $";

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        String keyid1 = actionProps.getProperty("keyid1");
        SafeSQL safeSQL = new SafeSQL();
        this.database.executePreparedUpdate("delete from s_preptypesampletypemap where s_preptypeid in (" + safeSQL.addIn(keyid1, ";") + ")", safeSQL.getValues());
        safeSQL.reset();
        this.database.executePreparedUpdate("delete from s_preptypetreatmenttypemap where s_preptypeid in (" + safeSQL.addIn(keyid1, ";") + ")", safeSQL.getValues());
    }
}

