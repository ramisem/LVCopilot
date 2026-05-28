/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.documents;

import java.sql.CallableStatement;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class InvokeGetSDIFormsProc
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String connectionId = properties.getProperty("connectionId");
        String sysUserId = properties.getProperty("sysUserId");
        String keyid1 = properties.getProperty("keyid1");
        String keyid2 = properties.getProperty("keyid2");
        String keyid3 = properties.getProperty("keyid3");
        String sdcid = properties.getProperty("sdcid");
        String callstmt = "{call LV_List" + (this.connectionInfo.isOracle() ? "." : "_") + "GetSDIForms  ( ?, ?, ?, ?, ?, ?, ?, ? ) }";
        this.database.prepareCall(callstmt);
        String result = "";
        try {
            CallableStatement cs = this.database.prepareCall(callstmt);
            try {
                cs.registerOutParameter(1, 12);
                cs.setString(2, connectionId);
                cs.setString(3, sysUserId);
                cs.setString(4, sdcid);
                cs.setString(5, keyid1);
                cs.setString(6, keyid2);
                cs.setString(7, keyid3);
                cs.setString(8, ";");
                cs.execute();
                result = cs.getString(1);
            }
            finally {
                this.database.closeCall();
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        properties.setProperty("rsetResult", result);
    }
}

