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

public class AddSDCOperation
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = "$Revision: 77311 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        DataSet values = new DataSet();
        values.addColumnValues("sdcid", 0, properties.getProperty("sdcid"), ";");
        values.addColumnValues("operationid", 0, properties.getProperty("operationid"), ";");
        values.addColumnValues("operationdesc", 0, properties.getProperty("operationdesc"), ";");
        values.addColumnValues("usersequence", 1, properties.getProperty("usersequence"), ";");
        for (int i = 0; i < values.size(); ++i) {
            if (values.getString(i, "operationdesc").equals("(null)")) {
                values.setString(i, "operationdesc", "");
            }
            String sdcid = values.getString(i, "sdcid");
            this.database.createPreparedResultSet("SELECT sdcid FROM sdc WHERE sdcid = ?", new Object[]{sdcid});
            if (!this.database.getNext()) {
                throw new SapphireException("Invalid operation data. SDC = '" + sdcid + "'");
            }
            String operationid = values.getString(i, "operationid");
            if (operationid == null || operationid.length() == 0 || operationid.length() > 20) {
                throw new SapphireException("Invalid operation data, operationid = '" + operationid + "'");
            }
            this.database.createPreparedResultSet("SELECT operationid FROM sdcoperation WHERE sdcid = ? AND operationid = ?", new Object[]{sdcid, operationid});
            if (!this.database.getNext()) continue;
            values.deleteRow(i);
        }
        try {
            DataSetUtil.insert(this.database, values, "sdcoperation");
        }
        catch (Exception e) {
            throw new SapphireException("Failed to add new operation '" + properties.getProperty("operationid") + "'. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        CacheUtil.clear(this.connectionInfo.getDatabaseId(), "SDC");
    }
}

