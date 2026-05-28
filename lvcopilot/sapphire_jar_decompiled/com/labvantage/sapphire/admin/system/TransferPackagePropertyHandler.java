/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.system;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import java.util.HashMap;
import sapphire.SapphireException;

public class TransferPackagePropertyHandler
extends PropertyHandler {
    @Override
    public void processProperties(HashMap props) throws SapphireException {
        String transferpackagescript = (String)props.get("transferpackagescript");
        DBUtil dbu = new DBUtil();
        try {
            dbu.setConnection(this.sapphireConnection);
            dbu.updateClob("transferpackage", "transferpackage", transferpackagescript, new String[]{"transferpackageid", "transferpackageversionid"}, new Object[]{props.get("pr0_transferpackageid"), props.get("pr0_transferpackageversionid")});
        }
        catch (SapphireException e) {
            throw new SapphireException("Failed to update transferpackage clob column. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.sapphireConnection.getConnectionId())), e);
        }
        finally {
            dbu.reset();
        }
    }
}

