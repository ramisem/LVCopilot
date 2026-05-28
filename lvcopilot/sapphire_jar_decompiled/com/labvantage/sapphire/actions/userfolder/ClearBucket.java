/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.userfolder;

import java.sql.CallableStatement;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class ClearBucket
extends BaseAction
implements sapphire.action.ClearBucket {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sysuserid = properties.getProperty("sysuserid", this.connectionInfo.getSysuserId());
        String sdcid = properties.getProperty("sdcid");
        if (sysuserid.length() == 0) {
            throw new SapphireException("INVALID_PARAMETER", "Sysuserid not specified");
        }
        if (sdcid.length() == 0) {
            throw new SapphireException("INVALID_PARAMETER", "SdcId not specified");
        }
        String callstmt = "";
        callstmt = this.database.isOracle() ? "{call lv_bucket.clearbucket( ?, ? ) }" : "{call lv_bucket_clearbucket( ?, ? ) }";
        try {
            CallableStatement cs = this.database.prepareCall(callstmt);
            cs.setString(1, sysuserid);
            cs.setString(2, sdcid);
            cs.executeQuery();
        }
        catch (Exception e) {
            this.logger.info("BUCKET Exception: " + e.getMessage());
            throw new SapphireException("CREATE_RSET_FAILURE", "Failed to clear bucket items for user: " + sysuserid + " , sdcid: " + sdcid, e);
        }
        finally {
            this.database.closeCall();
        }
    }
}

