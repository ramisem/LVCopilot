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

public class DeleteSDCExport
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = "$Revision: 77332 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        DataSet values = new DataSet();
        values.addColumnValues("sdcid", 0, properties.getProperty("sdcid"), ";");
        values.addColumnValues("exportid", 0, properties.getProperty("exportid"), ";");
        try {
            for (int i = 0; i < values.size(); ++i) {
                String sdcid = values.getString(i, "sdcid");
                String exportid = values.getString(i, "exportid");
                if (exportid.startsWith("Standard")) {
                    throw new SapphireException("Standard sdcexport cannot be deleted");
                }
                this.database.executePreparedUpdate("DELETE FROM sdcexport WHERE sdcid = ? AND exportid = ?", new Object[]{sdcid, exportid});
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to delete exports '" + properties.getProperty("exportid") + "'. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        CacheUtil.clear(this.connectionInfo.getDatabaseId(), "SDC");
    }
}

