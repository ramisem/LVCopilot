/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.ddt;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.util.cache.CacheUtil;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class AddSDCExport
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = "$Revision: 77311 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        DataSet values = new DataSet();
        values.addColumnValues("sdcid", 0, properties.getProperty("sdcid"), "+||+");
        values.addColumnValues("exportid", 0, properties.getProperty("exportid"), "+||+");
        values.addColumnValues("exportdesc", 0, properties.getProperty("exportdesc"), "+||+");
        values.addColumnValues("exportscript", 0, properties.getProperty("exportscript"), "+||+");
        values.addColumnValues("usersequence", 1, properties.getProperty("usersequence"), "+||+");
        for (int i = 0; i < values.size(); ++i) {
            if (values.getString(i, "exportdesc").equals("(null)")) {
                values.setString(i, "exportdesc", "");
            }
            if (values.getString(i, "exportscript").equals("(null)")) {
                values.setString(i, "exportscript", "");
            }
            String sdcid = values.getString(i, "sdcid");
            this.database.createPreparedResultSet("SELECT sdcid FROM sdc WHERE sdcid = ?", new Object[]{sdcid});
            if (!this.database.getNext()) {
                throw new SapphireException("Invalid link data. SDC = '" + sdcid + "'");
            }
            String exportid = values.getString(i, "exportid");
            if (exportid != null && exportid.length() != 0 && exportid.length() <= 20) continue;
            throw new SapphireException("Invalid export data, exportid = '" + exportid + "'");
        }
        try {
            DataSetUtil.insert(this.database, values, "sdcexport");
        }
        catch (Exception e) {
            throw new SapphireException("Failed to add new export scripts '" + properties.getProperty("exportid") + "'. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        CacheUtil.clear(this.connectionInfo.getDatabaseId(), "SDC");
    }
}

