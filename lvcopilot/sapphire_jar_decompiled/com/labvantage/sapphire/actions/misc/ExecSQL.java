/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.misc;

import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class ExecSQL
extends BaseAction {
    public static final String ID = "ExecSQL";
    public static final String VERSION = "1";
    public static final String PROPERTY_SQL = "sql";
    public static final String RETURN_ROWSAFFECTED = "rowsaffected";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sql = properties.getProperty(PROPERTY_SQL);
        if (sql.length() <= 0) {
            throw new SapphireException("INVALID_PARAMETER", "No sql parameter specified");
        }
        properties.setProperty(RETURN_ROWSAFFECTED, String.valueOf(this.database.executeUpdate(sql)));
    }
}

