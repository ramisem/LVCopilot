/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.ddt;

import com.labvantage.opal.handler.ErrorUtil;
import java.sql.CallableStatement;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class GenerateDeleteScript
extends BaseAction {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid = properties.getProperty("sdcid");
        if (sdcid.length() == 0) {
            throw new SapphireException("SDC property not defined!");
        }
        try {
            String callstmt = "{call lv_clean" + (this.connectionInfo.isOracle() ? "." : "_") + "killsdc ( ? ) }";
            CallableStatement cs = this.database.prepareCall(callstmt);
            cs.setString(1, sdcid);
            cs.executeUpdate();
        }
        catch (Exception e) {
            throw new SapphireException("Failed to generate delete script for sdc '" + sdcid + "'. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
    }
}

