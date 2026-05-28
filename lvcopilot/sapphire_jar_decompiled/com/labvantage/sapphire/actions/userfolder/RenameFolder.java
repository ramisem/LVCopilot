/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.userfolder;

import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseAction;

public class RenameFolder
extends BaseAction
implements sapphire.action.RenameFolder {
    @Override
    public int processAction(String actionid, String actionversionid, HashMap properties) {
        int rc = 1;
        String sysuserid = (String)properties.get("sysuserid");
        if (sysuserid == null || sysuserid.length() == 0) {
            sysuserid = this.connectionInfo.getSysuserId();
        }
        String folderid = (String)properties.get("folderid");
        String label = (String)properties.get("label");
        try {
            if (label != null && label.length() > 0) {
                this.database.executePreparedUpdate("UPDATE sysuserfolder SET folderlabel=?  WHERE sysuserid=? AND sysuserfolderid=?", new Object[]{label, sysuserid, folderid});
            }
        }
        catch (SapphireException e) {
            rc = this.setError("Unable to delete folder");
        }
        return rc;
    }
}

