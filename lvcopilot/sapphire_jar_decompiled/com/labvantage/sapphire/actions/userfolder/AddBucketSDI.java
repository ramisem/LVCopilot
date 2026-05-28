/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.userfolder;

import java.sql.CallableStatement;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddBucketSDI
extends BaseAction
implements sapphire.action.AddBucketSDI {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sysuserid = properties.getProperty("sysuserid", this.connectionInfo.getSysuserId());
        String sdcid = properties.getProperty("sdcid");
        String keyid1list = properties.getProperty("keyid1");
        String keyid2list = properties.getProperty("keyid2");
        String keyid3list = properties.getProperty("keyid3");
        boolean clearflag = StringUtil.getYN(properties.getProperty("clear"), "N").equals("Y");
        if (sysuserid.length() == 0) {
            throw new SapphireException("INVALID_PARAMETER", "Sysuserid not specified");
        }
        if (StringUtil.getLen(sdcid) == 0L) {
            throw new SapphireException("INVALID_PARAMETER", "SdcId not specified");
        }
        if (StringUtil.getLen(keyid1list) == 0L) {
            throw new SapphireException("INVALID_PARAMETER", "Keyid1 list not specified");
        }
        String callstmt = "";
        callstmt = this.database.isOracle() ? "{call lv_bucket.insertbucket( ?, ?, ?, ?, ?, ?, ?, ? ) }" : "{call lv_bucket_insertbucket( ?, ?, ?, ?, ?, ?, ?, ? ) }";
        try {
            CallableStatement cs = this.database.prepareCall(callstmt);
            cs.setString(1, this.connectionInfo.getConnectionId());
            cs.setString(2, sysuserid);
            cs.setString(3, sdcid);
            cs.setInt(4, clearflag ? 1 : 0);
            cs.setString(5, keyid1list);
            cs.setString(6, keyid2list);
            cs.setString(7, keyid3list);
            cs.setInt(8, 0);
            cs.executeQuery();
        }
        catch (Exception e) {
            this.logger.error("BUCKET Exception: " + e.getMessage(), e);
            throw new SapphireException("CREATE_RSET_FAILURE", "Failed to insert bucket items for user: " + sysuserid + " , sdcid: " + sdcid + ", keyid1list: " + keyid1list + ", keyid2list: " + keyid2list + ", keyid3list: " + keyid3list, e);
        }
        finally {
            this.database.closeCall();
        }
    }
}

