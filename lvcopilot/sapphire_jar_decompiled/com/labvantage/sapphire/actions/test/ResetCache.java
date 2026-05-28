/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.test;

import com.labvantage.sapphire.util.UnitsUtil;
import com.labvantage.sapphire.util.cache.CacheUtil;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ResetCache
extends BaseAction {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        boolean resetAll = properties.getProperty("resetall", "N").equalsIgnoreCase("Y");
        String cacheName = properties.getProperty("cachename", "");
        String startsWith = properties.getProperty("startswith", "");
        String endsWith = properties.getProperty("endswith", "");
        String databaseid = this.getConnectionProcessor().getSapphireConnection().getDatabaseId();
        if (resetAll) {
            CacheUtil.resetCache(databaseid, true);
        } else if (cacheName.length() > 0) {
            if (cacheName.equalsIgnoreCase("UnitConversion")) {
                UnitsUtil.popupulateUnitConversationCache(this.getQueryProcessor(), databaseid);
            } else {
                String[] cn = StringUtil.split(cacheName, ";");
                for (int i = 0; i < cn.length; ++i) {
                    if (startsWith.length() > 0) {
                        CacheUtil.removeAllStartWith(databaseid, cn[i], startsWith);
                    } else if (endsWith.length() > 0) {
                        CacheUtil.removeAllEndWith(databaseid, cn[i], endsWith);
                    } else {
                        CacheUtil.clear(databaseid, cn[i]);
                    }
                    this.logger.debug("Reset cache " + cn[i] + " complete.");
                }
            }
        }
    }
}

