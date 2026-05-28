/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.storage;

import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class UnReserveTrackItem
extends BaseAction
implements sapphire.action.UnReserveTrackItem {
    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        boolean propsmatch;
        String[] trackitemidprop = StringUtil.split(properties.getProperty("trackitemid"), ";");
        String[] storageunitidprop = StringUtil.split(properties.getProperty("storageunitid"), ";");
        boolean bl = propsmatch = "Y".equalsIgnoreCase(properties.getProperty("propsmatch"));
        if (properties.getProperty("storageunitid").length() == 0) {
            for (int i = 0; i < trackitemidprop.length; ++i) {
                String trackitemid = trackitemidprop[i];
                if (trackitemid.length() <= 0) continue;
                try {
                    this.database.executePreparedUpdate("delete from reservestorageunit where trackitemid = ?", new Object[]{trackitemid});
                    continue;
                }
                catch (Exception ex) {
                    throw new SapphireException("EXECUTE_STMT_FAILED", "Failed to run delete reserve storageunit for: " + trackitemid);
                }
            }
            return;
        } else if (propsmatch) {
            if (trackitemidprop.length != storageunitidprop.length) throw new SapphireException("INVALID_PROPERTIES", "There must be a storageunitid for each trackitem");
            for (int i = 0; i < trackitemidprop.length; ++i) {
                String trackitemid = trackitemidprop[i];
                String storageunitid = storageunitidprop[i];
                if (trackitemid.length() <= 0 || storageunitid.length() <= 0) continue;
                try {
                    this.database.executePreparedUpdate("delete from reservestorageunit where trackitemid = ? and storageunitid = ?", new Object[]{trackitemid, storageunitid});
                    continue;
                }
                catch (Exception ex) {
                    throw new SapphireException("EXECUTE_STMT_FAILED", "Failed to run delete reserve storageunit for: " + trackitemid + ";" + storageunitid);
                }
            }
            return;
        } else {
            for (int i = 0; i < trackitemidprop.length; ++i) {
                for (int storage = 0; storage < storageunitidprop.length; ++storage) {
                    String trackitemid = trackitemidprop[i];
                    String storageunitid = storageunitidprop[storage];
                    if (trackitemid.length() <= 0 || storageunitid.length() <= 0) continue;
                    try {
                        this.database.executePreparedUpdate("delete from reservestorageunit where trackitemid = ? and storageunitid = ?", new Object[]{trackitemid, storageunitid});
                        continue;
                    }
                    catch (Exception ex) {
                        throw new SapphireException("EXECUTE_STMT_FAILED", "Failed to run delete reserve storageunit for: " + trackitemid + ";" + storageunitid);
                    }
                }
            }
        }
    }
}

