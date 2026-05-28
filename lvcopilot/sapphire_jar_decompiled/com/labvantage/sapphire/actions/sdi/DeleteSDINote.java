/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.sapphire.modules.search.Indexer;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class DeleteSDINote
extends BaseAction {
    public static final String PROPERTY_SDCID = "sdcid";
    public static final String PROPERTY_KEYID1 = "keyid1";
    public static final String PROPERTY_KEYID2 = "keyid2";
    public static final String PROPERTY_KEYID3 = "keyid3";
    public static final String PROPERTY_NOTENUM = "notenum";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid = properties.getProperty(PROPERTY_SDCID);
        if (sdcid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "Missing sdcid!");
        }
        String keyid1 = properties.getProperty(PROPERTY_KEYID1);
        if (keyid1.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "Missing keyid1!");
        }
        String keyid2 = properties.getProperty(PROPERTY_KEYID2, "(null)");
        String keyid3 = properties.getProperty(PROPERTY_KEYID3, "(null)");
        String notenum = properties.getProperty(PROPERTY_NOTENUM);
        if (notenum.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "Missing notenum!");
        }
        if (this.database.isOracle()) {
            this.database.executePreparedUpdate("DELETE FROM sdinote WHERE ( linksdcid, linkkeyid1, linkkeyid2, linkkeyid3, linknotenum ) IN ( SELECT sdcid, keyid1, keyid2, keyid3, notenum FROM sdinote WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND notenum = ?)", new Object[]{sdcid, keyid1, keyid2, keyid3, notenum});
        } else {
            this.database.executePreparedUpdate("DELETE FROM sdinote WHERE linksdcid IN (SELECT sdcid FROM sdinote WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND notenum = ?) AND linkkeyid1 IN (SELECT keyid1 FROM sdinote WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND notenum = ?) AND linkkeyid2 IN (SELECT keyid2 FROM sdinote WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND notenum = ?) AND linkkeyid3 IN (SELECT keyid3 FROM sdinote WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND notenum = ?) AND linknotenum IN (SELECT notenum FROM sdinote WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND notenum = ?)", new Object[]{sdcid, keyid1, keyid2, keyid3, notenum, sdcid, keyid1, keyid2, keyid3, notenum, sdcid, keyid1, keyid2, keyid3, notenum, sdcid, keyid1, keyid2, keyid3, notenum, sdcid, keyid1, keyid2, keyid3, notenum});
        }
        this.database.executePreparedUpdate("DELETE FROM sdinote WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND notenum = ?", new Object[]{sdcid, keyid1, keyid2, keyid3, notenum});
        if (properties.getProperty("index", "Y").equals("Y")) {
            Indexer.removeNote(this.connectionInfo, sdcid, keyid1, keyid2, keyid3, new Integer(notenum));
        }
    }
}

