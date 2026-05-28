/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions.favorite;

import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseAction;

public class AddFavorite
extends BaseAction {
    private static final String LABVANTAGE_CVS_ID = "$Revision: 53544 $";

    @Override
    public int processAction(String actionid, String actionversionid, HashMap properties) {
        int rc = 1;
        try {
            String favoriteId = (String)properties.get("favoriteid");
            String webpageId = (String)properties.get("webpageid");
            String pageId = (String)properties.get("pageid");
            String command = (String)properties.get("command");
            String url = "rc?command=" + command + "&" + command + "=" + pageId;
            String sysuserid = (String)properties.get("sysuserid");
            if (sysuserid == null || sysuserid.length() == 0) {
                sysuserid = this.connectionInfo.getSysuserId();
                properties.put("sysuserid", sysuserid);
            }
            this.logger.debug("Before nextseq");
            int nextSequence = this.database.getPreparedCount("SELECT max(usersequence) maxusersequence FROM sysuserfavorite WHERE sysuserid=?", new Object[]{sysuserid}) + 1;
            this.logger.debug("nextSquence:" + nextSequence);
            if (nextSequence < 0) {
                nextSequence = 0;
            }
            this.database.executePreparedUpdate("INSERT INTO sysuserfavorite ( sysuserid, favoriteid, webpageid, url, usersequence ) VALUES ( ?, ?, ?, ?, ? )", new Object[]{sysuserid, favoriteId, webpageId, url, new Integer(nextSequence)});
            this.logger.debug("datainsert :" + favoriteId);
        }
        catch (SapphireException e) {
            this.logger.error("Unable to create a new Favorite", e);
            rc = this.setError("Unable to create a new Favorite");
        }
        return rc;
    }
}

