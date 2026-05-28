/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions.favorite;

import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseAction;

public class MoveFavorite
extends BaseAction {
    private static final String LABVANTAGE_CVS_ID = "$Revision: 53545 $";

    @Override
    public int processAction(String actionid, String actionversionid, HashMap properties) {
        int rc = 1;
        String sysuserid = (String)properties.get("sysuserid");
        if (sysuserid == null || sysuserid.length() == 0) {
            sysuserid = this.connectionInfo.getSysuserId();
        }
        String favoriteId = (String)properties.get("favoriteid");
        String position = (String)properties.get("position");
        this.logger.debug("Position :" + position);
        try {
            if (position != null && position.equalsIgnoreCase("MoveTop")) {
                this.logger.debug("MoveTep");
                int nextSequence = this.database.getPreparedCount("SELECT max(usersequence) maxusersequence FROM sysuserfavorite WHERE sysuserid=?", new Object[]{sysuserid}) + 1;
                if (nextSequence < 0) {
                    nextSequence = 0;
                }
                this.database.executePreparedUpdate("UPDATE sysuserfavorite SET usersequence=? WHERE sysuserid=? AND favoriteid=?", new Object[]{new Integer(nextSequence), sysuserid, favoriteId});
            } else if (position != null && position.equalsIgnoreCase("MoveBottom")) {
                this.logger.debug("MoveBottom");
                this.database.executePreparedUpdate("UPDATE sysuserfavorite SET usersequence=usersequence + 1 WHERE sysuserid=?", new Object[]{sysuserid});
                this.database.executePreparedUpdate("UPDATE sysuserfavorite SET usersequence=0 WHERE sysuserid=? AND favoriteid= ?", new Object[]{sysuserid, favoriteId});
            }
        }
        catch (SapphireException e) {
            rc = this.setError("Unable to move the Favorites");
        }
        return rc;
    }
}

