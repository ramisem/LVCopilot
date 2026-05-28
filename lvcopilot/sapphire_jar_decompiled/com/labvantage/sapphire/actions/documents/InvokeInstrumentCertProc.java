/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.documents;

import java.sql.CallableStatement;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class InvokeInstrumentCertProc
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String result;
        block11: {
            String connectionId = properties.getProperty("connectionId");
            String sysUserId = properties.getProperty("sysUserId");
            String instrumentTyepId = properties.getProperty("instrumenttypeid", "");
            String paramlistId = properties.getProperty("paramlistId");
            String paramlistVersionId = properties.getProperty("paramlistVersionId");
            String variantId = properties.getProperty("variantId");
            String certflag = properties.getProperty("certflag");
            result = "";
            if (instrumentTyepId.length() > 0) {
                String callstmt = "{call LV_List" + (this.connectionInfo.isOracle() ? "." : "_") + "InstrumentCert ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) }";
                this.database.prepareCall(callstmt);
                try {
                    CallableStatement cs = this.database.prepareCall(callstmt);
                    try {
                        cs.registerOutParameter(1, 12);
                        cs.setString(2, connectionId);
                        cs.setString(3, sysUserId);
                        cs.setString(4, null);
                        cs.setString(5, null);
                        cs.setString(6, null);
                        cs.setString(7, certflag);
                        cs.setString(8, ";");
                        cs.setString(9, "Y");
                        cs.setString(10, instrumentTyepId);
                        cs.execute();
                        result = cs.getString(1);
                        break block11;
                    }
                    finally {
                        this.database.closeCall();
                    }
                }
                catch (Exception e) {
                    throw new SapphireException(e);
                }
            }
            String callstmt = "{call LV_List" + (this.connectionInfo.isOracle() ? "." : "_") + "InstrumentCert ( ?, ?, ?, ?, ?, ?, ?, ? ) }";
            this.database.prepareCall(callstmt);
            try {
                CallableStatement cs = this.database.prepareCall(callstmt);
                try {
                    cs.registerOutParameter(1, 12);
                    cs.setString(2, connectionId);
                    cs.setString(3, sysUserId);
                    cs.setString(4, paramlistId);
                    cs.setString(5, paramlistVersionId);
                    cs.setString(6, variantId);
                    cs.setString(7, certflag);
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
        }
        properties.setProperty("rsetResult", result);
    }
}

