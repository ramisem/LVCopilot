/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.userfolder;

import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseAction;

public class DeleteFolder
extends BaseAction
implements sapphire.action.DeleteFolder {
    @Override
    public int processAction(String actionid, String actionversionid, HashMap properties) {
        int rc = 1;
        String sysuserid = (String)properties.get("sysuserid");
        if (sysuserid == null || sysuserid.length() == 0) {
            sysuserid = this.connectionInfo.getSysuserId();
        }
        String folderid = (String)properties.get("folderid");
        try {
            this.database.executePreparedUpdate("DELETE FROM sysuserfolderitem WHERE sysuserid=? AND sysuserfolderid=?", new Object[]{sysuserid, folderid});
            this.database.executePreparedUpdate("DELETE FROM sysuserfolder WHERE sysuserid=? AND sysuserfolderid=?", new Object[]{sysuserid, folderid});
        }
        catch (SapphireException e) {
            rc = this.setError("Unable to delete folder");
        }
        return rc;
    }
}

