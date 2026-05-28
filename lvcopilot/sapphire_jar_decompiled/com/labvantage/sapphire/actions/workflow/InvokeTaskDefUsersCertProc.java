/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.workflow;

import java.sql.CallableStatement;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class InvokeTaskDefUsersCertProc
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String connectionId = properties.getProperty("connectionId");
        String departmentid = properties.getProperty("departmentd");
        String sysUserId = properties.getProperty("sysUserId");
        String taskdefid = properties.getProperty("taskdefid");
        String taskdefversionid = properties.getProperty("taskdefversionid");
        String taskdefvariantid = properties.getProperty("taskdefvariantid");
        String callstmt = "{call LV_List" + (this.connectionInfo.isOracle() ? "." : "_") + "TaskDefUsersCert  ( ?, ?, ?, ?, ?, ?, ?, ? ) }";
        this.database.prepareCall(callstmt);
        String result = "";
        try {
            CallableStatement cs = this.database.prepareCall(callstmt);
            try {
                cs.registerOutParameter(1, 12);
                cs.setString(2, connectionId);
                cs.setString(3, sysUserId);
                cs.setString(4, departmentid);
                cs.setString(5, taskdefid);
                cs.setString(6, taskdefversionid);
                cs.setString(7, taskdefvariantid);
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

