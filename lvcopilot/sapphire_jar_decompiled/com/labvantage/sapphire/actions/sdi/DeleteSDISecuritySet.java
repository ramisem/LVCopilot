/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.opal.handler.ErrorUtil;
import java.sql.PreparedStatement;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.action.BaseAction;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class DeleteSDISecuritySet
extends BaseAction
implements sapphire.action.DeleteSDISecuritySet {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        boolean deleterset = false;
        String rsetid = properties.getProperty("rsetid");
        if (rsetid == null) {
            rsetid = "";
        }
        DAMProcessor dam = null;
        String sdcid = properties.getProperty("sdcid", "");
        String keyid1p = properties.getProperty("keyid1", "");
        if (sdcid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTIES", "sdcid is mandatory");
        }
        if (keyid1p.length() == 0) {
            throw new SapphireException("INVALID_PROPERTIES", "keyid1 is mandatory");
        }
        if (properties.getProperty("securityset", "").length() == 0) {
            throw new SapphireException("INVALID_PROPERTIES", "Security Set is mandatory");
        }
        if (rsetid.length() == 0) {
            boolean applylock = properties.getProperty("applylock").equals("Y");
            dam = this.getDAMProcessor();
            rsetid = applylock ? dam.createLockedRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3")) : dam.createRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"));
            deleterset = true;
        }
        PreparedStatement pstmt = null;
        try {
            String deleteSQL = "DELETE FROM sdisecurityset WHERE sdcid = '" + sdcid + "' AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND securityset = ?";
            pstmt = this.database.prepareStatement("psmt", deleteSQL);
            String separator = properties.getProperty("separator", ";");
            String[] keyid1prop = StringUtil.split(properties.getProperty("keyid1"), separator);
            String[] keyid2prop = StringUtil.split(properties.getProperty("keyid2"), separator);
            String[] keyid3prop = StringUtil.split(properties.getProperty("keyid3"), separator);
            String[] securitysetprop = StringUtil.split(properties.getProperty("securityset"), separator);
            if ("Y".equals(properties.getProperty("propsmatch", "N"))) {
                if (securitysetprop.length != keyid1prop.length) {
                    throw new SapphireException("In PROPSMATCH mode, the number of SDIs must match the number of Security Sets");
                }
                for (int i = 0; i < keyid1prop.length; ++i) {
                    String keyid1 = keyid1prop[i];
                    String keyid2 = keyid2prop.length == 0 || keyid2prop.length < keyid1prop.length || keyid2prop[i].length() == 0 ? "(null)" : keyid2prop[i];
                    String keyid3 = keyid3prop.length == 0 || keyid3prop.length < keyid1prop.length || keyid3prop[i].length() == 0 ? "(null)" : keyid3prop[i];
                    String securityset = securitysetprop[i];
                    if (StringUtil.getLen(securityset) <= 0L) continue;
                    pstmt.setString(1, keyid1);
                    pstmt.setString(2, keyid2);
                    pstmt.setString(3, keyid3);
                    pstmt.setString(4, securitysetprop[i]);
                    this.logger.info("Deleting the sdisecurityset record of {" + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3 + ";" + securitysetprop[i] + "}");
                    pstmt.executeUpdate();
                }
            } else {
                for (int i = 0; i < keyid1prop.length; ++i) {
                    String keyid1 = keyid1prop[i];
                    String keyid2 = keyid2prop.length == 0 || keyid2prop.length < keyid1prop.length || keyid2prop[i].length() == 0 ? "(null)" : keyid2prop[i];
                    String keyid3 = keyid3prop.length == 0 || keyid3prop.length < keyid1prop.length || keyid3prop[i].length() == 0 ? "(null)" : keyid3prop[i];
                    for (String securityset : securitysetprop) {
                        if (StringUtil.getLen(securityset) <= 0L) continue;
                        pstmt.setString(1, keyid1);
                        pstmt.setString(2, keyid2);
                        pstmt.setString(3, keyid3);
                        pstmt.setString(4, securityset);
                        this.logger.info("Deleting the sdisecurityset record of {" + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3 + ";" + securityset + "}");
                        pstmt.executeUpdate();
                    }
                }
            }
        }
        catch (Exception ex) {
            throw new SapphireException("PREPARE_STMT_FAILED", ErrorUtil.extractMessageFromException(ex, ErrorUtil.isUserAdmin(this.getConnectionId())), ex);
        }
        finally {
            if (deleterset) {
                dam.clearRSet(rsetid);
            }
            if (pstmt != null) {
                this.database.closeStatement("psmt");
            }
        }
    }
}

