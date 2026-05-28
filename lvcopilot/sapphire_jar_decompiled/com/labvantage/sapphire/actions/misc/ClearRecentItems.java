/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.misc;

import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class ClearRecentItems
extends BaseAction
implements sapphire.action.ClearRecentItems {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sysuserid = properties.getProperty("sysuserid", this.connectionInfo.getSysuserId());
        if (sysuserid.length() == 0) {
            throw new SapphireException("INVALID_PARAMETER", "Sysuserid not specified");
        }
        try {
            this.database.executePreparedUpdate("DELETE FROM webpagelogtitle WHERE webpagelogid IN ( SELECT webpagelogid FROM webpagelog WHERE ( logtypeflag is null or (logtypeflag <> 'S' AND logtypeflag <> 'P') ) and sysuserid = ? )", new Object[]{sysuserid});
        }
        catch (SapphireException e) {
            throw new SapphireException("EXECUTE_STMT_FAILED", "Failed to delete from webpagelogtitle", e);
        }
        try {
            this.database.executePreparedUpdate("DELETE FROM webpagelog WHERE ( logtypeflag is null or (logtypeflag <> 'S' AND logtypeflag <> 'P')) and sysuserid = ?", new Object[]{sysuserid});
        }
        catch (SapphireException e) {
            throw new SapphireException("EXECUTE_STMT_FAILED", "Failed to delete from webpagelog", e);
        }
        this.logger.info("Cleared all items from webpagelog for user '" + this.connectionInfo.getSysuserId() + "'");
    }
}

