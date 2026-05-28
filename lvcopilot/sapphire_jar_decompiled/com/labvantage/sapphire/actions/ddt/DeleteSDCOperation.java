/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.ddt;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.util.cache.CacheUtil;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class DeleteSDCOperation
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = "$Revision: 77332 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        DataSet values = new DataSet();
        values.addColumnValues("sdcid", 0, properties.getProperty("sdcid"), ";");
        values.addColumnValues("operationid", 0, properties.getProperty("operationid"), ";");
        try {
            for (int i = 0; i < values.size(); ++i) {
                String sdcid = values.getString(i, "sdcid");
                String operationid = values.getString(i, "operationid");
                this.database.executePreparedUpdate("DELETE FROM sdcsecurity WHERE sdcid = ? AND operationid = ?", new Object[]{sdcid, operationid});
                this.database.executePreparedUpdate("DELETE FROM sdcoperation WHERE sdcid = ? AND operationid = ?", new Object[]{sdcid, operationid});
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to delete operation '" + properties.getProperty("operationid") + "'. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        CacheUtil.clear(this.connectionInfo.getDatabaseId(), "SDC");
    }
}

