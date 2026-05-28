/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.misc;

import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseAction;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class IsNull
extends BaseAction
implements sapphire.action.IsNull {
    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid = properties.getProperty("sdcid");
        String columnid = properties.getProperty("columnid");
        String isnull = "No";
        if (sdcid.length() <= 0 || columnid.length() <= 0) throw new SapphireException("INVALID_PROPERTY", "The sdcid and columnid properties were missing.");
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT count(*)");
        sql.append(" FROM " + sdcProcessor.getProperty(sdcid, "tableid"));
        sql.append(" WHERE " + columnid + " is null");
        int keycols = Integer.parseInt(sdcProcessor.getProperty(sdcid, "keycolumns"));
        for (int i = 0; i < keycols; ++i) {
            sql.append(" AND " + sdcProcessor.getProperty(sdcid, "keycolid" + String.valueOf(i + 1)) + " = " + safeSQL.addVar(properties.getProperty("keyid" + String.valueOf(i + 1))));
        }
        this.logger.info("Executing: " + sql.toString());
        int count = this.database.getPreparedCount(sql.toString(), safeSQL.getValues());
        if (count < 0) throw new SapphireException("INVALID_PROPERTY", "The column '" + columnid + "' is invalid.");
        if (count == 1) {
            isnull = "Yes";
        }
        properties.setProperty("isnull", isnull);
    }
}

