/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.util.StringHolder;
import java.sql.PreparedStatement;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.action.BaseAction;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class DeleteSDIAddress
extends BaseAction
implements sapphire.action.DeleteSDIAddress {
    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        int rc = 1;
        boolean deleterset = false;
        String rsetid = properties.getProperty("rsetid");
        if (rsetid == null) {
            rsetid = "";
        }
        DAMProcessor dam = null;
        PreparedStatement pstmt = null;
        String sdcid = properties.getProperty("sdcid", "");
        String keyid1p = properties.getProperty("keyid1", "");
        String addressid = properties.getProperty("addressid", "");
        String addressType = properties.getProperty("addresstype", "");
        String contactFunction = properties.getProperty("contactfunction", "");
        if (sdcid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTIES", "sdcid is mandatory");
        }
        if (keyid1p.length() == 0) {
            throw new SapphireException("INVALID_PROPERTIES", "keyid1 is mandatory");
        }
        if (addressid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTIES", "addressid is mandatory");
        }
        if (addressType.length() == 0) {
            throw new SapphireException("INVALID_PROPERTIES", "addresstype is mandatory");
        }
        if (contactFunction.length() == 0) {
            throw new SapphireException("INVALID_PROPERTIES", "contactfunction is mandatory");
        }
        if (rsetid.length() == 0) {
            boolean applylock = properties.getProperty("applylock").equals("Y");
            dam = this.getDAMProcessor();
            StringHolder rsetidHolder = new StringHolder();
            rc = applylock ? dam.createLockedRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"), rsetidHolder) : dam.createRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"), rsetidHolder);
            if (rc == 1) {
                rsetid = rsetidHolder.value;
            }
            deleterset = true;
        }
        if (rc != true) throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create RSET.");
        String deleteSQL = "DELETE FROM sdiaddress WHERE sdcid = '" + sdcid + "' AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND addressid = ? AND addresstype = ? AND contactfunction = ?";
        try {
            String separator = properties.getProperty("separator", ";");
            pstmt = this.database.prepareStatement("psmt", deleteSQL);
            String[] keyid1prop = StringUtil.split(properties.getProperty("keyid1"), separator);
            String[] keyid2prop = StringUtil.split(properties.getProperty("keyid2"), separator);
            String[] keyid3prop = StringUtil.split(properties.getProperty("keyid3"), separator);
            String[] addressidprop = StringUtil.split(properties.getProperty("addressid"), separator);
            String[] addresstypeprop = StringUtil.split(properties.getProperty("addresstype"), separator);
            String[] functionprop = StringUtil.split(properties.getProperty("contactfunction"), separator);
            if (addressidprop.length != addresstypeprop.length || addressidprop.length != functionprop.length) return;
            for (int sdi = 0; sdi < keyid1prop.length; ++sdi) {
                String keyid1 = keyid1prop[sdi];
                String keyid2 = keyid2prop.length == 0 || keyid2prop.length < keyid1prop.length || keyid2prop[sdi].length() == 0 ? "(null)" : keyid2prop[sdi];
                String keyid3 = keyid3prop.length == 0 || keyid3prop.length < keyid1prop.length || keyid3prop[sdi].length() == 0 ? "(null)" : keyid3prop[sdi];
                try {
                    pstmt.setString(1, keyid1);
                    pstmt.setString(2, keyid2);
                    pstmt.setString(3, keyid3);
                    pstmt.setString(4, addressidprop[sdi]);
                    pstmt.setString(5, addresstypeprop[sdi]);
                    pstmt.setString(6, functionprop[sdi]);
                    this.logger.info("Deleting the sdiaddress record of {" + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3 + ";" + addressidprop[sdi] + ";" + addresstypeprop[sdi] + ";" + functionprop[sdi] + "}");
                    int rows = pstmt.executeUpdate();
                    if (rows == 1) continue;
                    throw new SapphireException("EXECUTE_STMT_FAILED", "Failed to run delete statement for: " + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3);
                }
                catch (Exception ex) {
                    throw new SapphireException("PREPARE_STMT_FAILED", "Failed to set parameters the delete statement for: " + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3 + " because: " + ErrorUtil.extractMessageFromException(ex, ErrorUtil.isUserAdmin(this.getConnectionId())), ex);
                }
            }
            return;
        }
        catch (Exception ex) {
            throw new SapphireException("PREPARE_STMT_FAILED", ErrorUtil.extractMessage("Failed to use the SQL. Reason: " + deleteSQL, ErrorUtil.isUserAdmin(this.getConnectionId())), ex);
        }
        finally {
            if (deleterset) {
                dam.clearRSet(rsetid);
            }
            this.database.closeStatement("psmt");
        }
    }
}

