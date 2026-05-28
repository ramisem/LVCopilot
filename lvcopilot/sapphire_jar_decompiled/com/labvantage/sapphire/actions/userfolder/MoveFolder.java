/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.userfolder;

import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseAction;

public class MoveFolder
extends BaseAction
implements sapphire.action.MoveFolder {
    @Override
    public int processAction(String actionid, String actionversionid, HashMap properties) {
        int rc = 1;
        String sysuserid = (String)properties.get("sysuserid");
        if (sysuserid == null || sysuserid.length() == 0) {
            sysuserid = this.connectionInfo.getSysuserId();
        }
        String folderid = (String)properties.get("folderid");
        String position = (String)properties.get("position");
        try {
            if (position != null && position.equalsIgnoreCase("top")) {
                int nextSequence = this.database.getPreparedCount("SELECT max(usersequence) maxusersequence FROM sysuserfolder WHERE sysuserid=?", new Object[]{sysuserid}) + 1;
                if (nextSequence < 0) {
                    nextSequence = 0;
                }
                this.database.executePreparedUpdate("UPDATE sysuserfolder SET usersequence=? WHERE sysuserid=? AND sysuserfolderid=?", new Object[]{new Integer(nextSequence), sysuserid, folderid});
            } else if (position != null && position.equalsIgnoreCase("bottom")) {
                this.database.executePreparedUpdate("UPDATE sysuserfolder SET usersequence=usersequence + 1 WHERE sysuserid=?", new Object[]{sysuserid});
                this.database.executePreparedUpdate("UPDATE sysuserfolder SET usersequence=0 WHERE sysuserid=? AND sysuserfolderid= ?", new Object[]{sysuserid, folderid});
            }
        }
        catch (SapphireException e) {
            rc = this.setError("Unable to move the folder");
        }
        return rc;
    }
}

