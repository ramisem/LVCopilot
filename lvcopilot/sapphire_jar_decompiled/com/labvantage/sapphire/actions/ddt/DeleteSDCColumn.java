/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.ddt;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.sql.CallableStatement;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class DeleteSDCColumn
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = "$Revision: 77332 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        DataSet values = new DataSet();
        values.addColumnValues("tableid", 0, properties.getProperty("tableid"), ";");
        values.addColumnValues("columnid", 0, properties.getProperty("columnid"), ";");
        try {
            for (int i = 0; i < values.size(); ++i) {
                String tableid = values.getString(i, "tableid");
                String columnid = values.getString(i, "columnid").trim();
                String callstmt = "{call lv_clean" + (this.connectionInfo.isOracle() ? "." : "_") + "dropcolumnwithclear( ?, ? ) }";
                CallableStatement cs = this.database.prepareCall(callstmt);
                cs.setString(1, tableid);
                cs.setString(2, columnid);
                cs.executeUpdate();
                this.database.executePreparedUpdate("DELETE FROM sdclink WHERE sdcid IN ( SELECT sdcid FROM sdc WHERE tableid = ? ) AND ( sdccolumnid = ? OR sdccolumnid2 = ? )", new Object[]{tableid, columnid, columnid});
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to delete columns '" + properties.getProperty("columnid") + "'. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        CacheUtil.clear(this.connectionInfo.getDatabaseId(), "SDC");
    }
}

