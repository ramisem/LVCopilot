/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.cmt.SDISnapshotItem;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class AddSDIVersion
extends AddSDI {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");
        String keyid2 = properties.getProperty("keyid2");
        String keyid3 = properties.getProperty("keyid3");
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        String tableid = sdcProcessor.getProperty(sdcid, "tableid");
        boolean versioned = sdcProcessor.getProperty(sdcid, "versionedflag").equals("Y");
        String keycolid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
        String keycolid2 = sdcProcessor.getProperty(sdcid, "keycolid2");
        String keycolid3 = sdcProcessor.getProperty(sdcid, "keycolid3");
        boolean isImport = false;
        SDISnapshotItem sdiSnapshotItem = null;
        SDIData sdiData = null;
        if (properties.get("sdisnapshotitem") != null && properties.get("sdisnapshotitem") instanceof SDISnapshotItem) {
            sdiSnapshotItem = (SDISnapshotItem)properties.get("sdisnapshotitem");
            sdiData = sdiSnapshotItem.getSDIData();
        }
        if (sdiData != null) {
            DataSet primary = sdiData.getDataset("primary");
            keyid1 = primary.getValue(0, keycolid1);
            keyid2 = primary.getValue(0, keycolid2);
            keyid3 = primary.getValue(0, keycolid3);
            isImport = true;
            properties.put("sdidata", sdiData);
            properties.put("snapshotkeyid1", keyid1);
            properties.put("snapshotkeyid2", keyid2);
            properties.put("snapshotkeyid3", keyid3);
        }
        if (sdcid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No SDC specified");
        }
        if (keyid1.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No keyid1 specified");
        }
        if (keyid2.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No keyid2 specified");
        }
        if (keycolid3.length() > 0 && keyid3.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No keyid3 specified");
        }
        if (versioned) {
            try {
                SafeSQL safeSQL = new SafeSQL();
                String sql = "SELECT * FROM " + tableid + " WHERE " + keycolid1 + " = " + safeSQL.addVar(keyid1) + (isImport ? "" : " AND " + keycolid2 + " = " + safeSQL.addVar(keyid2)) + (keycolid3.length() > 0 ? " AND " + keycolid3 + " = " + safeSQL.addVar(keyid3) : "");
                this.database.createPreparedResultSet(sql, safeSQL.getValues());
                if (this.database.getNext()) {
                    properties.setProperty("overrideautokey", "Y");
                    if (!isImport) {
                        properties.setProperty("templatekeyid1", this.database.getString(keycolid1));
                        properties.setProperty("templatekeyid2", this.database.getString(keycolid2));
                        properties.setProperty("excludecategory", "Y");
                        if (keycolid3.length() > 0) {
                            properties.setProperty("templatekeyid3", this.database.getString(keycolid3));
                        }
                    }
                    String maxSql = this.connectionInfo.isOracle() ? " nvl( max( to_number( " + tableid + "versionid ) ), 0 )" : " isnull( max( cast( " + tableid + "versionid AS Integer ) ), 0 )";
                    safeSQL.reset();
                    int nextVersion = this.database.getPreparedCount("SELECT " + maxSql + " FROM " + tableid + " WHERE " + keycolid1 + " = " + safeSQL.addVar(keyid1) + (keycolid3.length() > 0 ? " AND " + keycolid3 + " = " + safeSQL.addVar(keyid3) : ""), safeSQL.getValues());
                    properties.setProperty("keyid2", String.valueOf(nextVersion + 1));
                }
                if (isImport) {
                    properties.setProperty("overrideautokey", "Y");
                    properties.setProperty("keyid1", keyid1);
                    properties.setProperty("keyid2", "1");
                    properties.setProperty("keyid3", keyid3);
                }
                throw new SapphireException("INVALID_PROPERTY", "Failed to find: " + new SDI(sdcid, keyid1, keyid2, "").getKeyText());
            }
            catch (SapphireException e) {
                throw new SapphireException("DB_ACTION_FAILED", "Failed to get next version id. Exception: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
        } else {
            throw new SapphireException("INVALID_PROPERTY", "SDC " + sdcid + " is not version controlled");
        }
        properties.setProperty("newversion", "Y");
        super.processAction(properties);
    }
}

