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
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class DeleteCategoryItem
extends BaseAction
implements sapphire.action.DeleteCategoryItem {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        int rc = 1;
        boolean deleterset = false;
        String rsetid = properties.getProperty("rsetid");
        DAMProcessor dam = this.getDAMProcessor();
        PreparedStatement pstmt = null;
        String sdcid = properties.getProperty("sdcid");
        if (rsetid.length() == 0) {
            boolean applylock = properties.getProperty("applylock").equals("Y");
            StringHolder rsetidHolder = new StringHolder();
            rc = applylock ? dam.createLockedRSet(sdcid, properties.getProperty("keyid1"), "", "", rsetidHolder) : dam.createRSet(sdcid, properties.getProperty("keyid1"), "", "", rsetidHolder);
            if (rc != 1) {
                throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create rset");
            }
            rsetid = rsetidHolder.value;
            deleterset = true;
        }
        DataSet ds = null;
        String separator = properties.getProperty("separator", ";");
        String deleteSQL = "DELETE FROM categoryitem WHERE sdcid = '" + sdcid + "' AND keyid1 = ?";
        try {
            ds = new DataSet();
            ds.addColumnValues("keyid1", 0, properties.getProperty("keyid1"), separator);
            if (properties.getProperty("categoryid").length() > 0) {
                ds.addColumnValues("categoryid", 0, properties.getProperty("categoryid"), separator);
                deleteSQL = deleteSQL + " AND categoryid = ?";
            }
            ds.padColumns();
            pstmt = this.database.prepareStatement("deletecategories", deleteSQL);
            for (int i = 0; i < ds.size(); ++i) {
                String keyid1 = ds.getString(i, "keyid1");
                try {
                    pstmt.setString(1, keyid1);
                    if (ds.isValidColumn("categoryid")) {
                        pstmt.setString(2, ds.getString(i, "categoryid"));
                    }
                    pstmt.executeUpdate();
                    continue;
                }
                catch (Exception ex) {
                    throw new SapphireException("PREPARE_STMT_FAILED", "Failed to set parameters the delete statement for: " + sdcid + ";" + keyid1 + ";because: " + ErrorUtil.extractMessageFromException(ex, ErrorUtil.isUserAdmin(this.getConnectionId())), ex);
                }
            }
        }
        catch (Exception e) {
            throw new SapphireException("PREPARE_STMT_FAILED", ErrorUtil.extractMessage("Failed to use the SQL. Reason: " + deleteSQL, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        finally {
            this.database.closeStatement("deletecategories");
            ds.reset();
            if (deleterset) {
                dam.clearRSet(rsetid);
            }
        }
    }
}

