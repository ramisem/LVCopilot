/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions.favorite;

import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseAction;

public class RemoveFavorite
extends BaseAction {
    private static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public int processAction(String actionid, String actionversionid, HashMap properties) {
        int rc = 1;
        String sysuserid = (String)properties.get("sysuserid");
        if (sysuserid == null || sysuserid.length() == 0) {
            sysuserid = this.connectionInfo.getSysuserId();
        }
        String favoriteid = (String)properties.get("favoriteid");
        this.logger.debug("Executing an action :removeFavorite:" + properties.toString());
        try {
            this.database.executePreparedUpdate("DELETE FROM sysuserfavorite WHERE sysuserid=? AND favoriteid=?", new Object[]{sysuserid, favoriteid});
        }
        catch (SapphireException e) {
            rc = this.setError("Unable to remove favorite", e);
        }
        return rc;
    }
}

