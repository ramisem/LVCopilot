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

public class DeleteSDIRole
extends BaseAction
implements sapphire.action.DeleteSDIRole {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        int rc = 1;
        boolean deleterset = false;
        String rsetid = properties.getProperty("rsetid");
        DAMProcessor dam = this.getDAMProcessor();
        PreparedStatement pstmt = null;
        String sdcid = properties.getProperty("sdcid");
        if (rsetid.length() == 0) {
            StringHolder rsetidHolder = new StringHolder(rsetid);
            boolean applylock = properties.getProperty("applylock").equals("Y");
            rc = applylock ? dam.createLockedRSet(sdcid, properties.getProperty("keyid1"), "", "", rsetidHolder) : dam.createRSet(sdcid, properties.getProperty("keyid1"), "", "", rsetidHolder);
            if (rc != 1) {
                throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create rset");
            }
            rsetid = rsetidHolder.value;
            deleterset = true;
        }
        DataSet ds = null;
        String separator = properties.getProperty("separator", ";");
        String deleteSQL = "DELETE FROM sdirole WHERE sdcid = '" + sdcid + "' AND keyid1 = ?";
        try {
            ds = new DataSet();
            ds.addColumnValues("keyid1", 0, properties.getProperty("keyid1"), separator);
            if (properties.getProperty("roleid").length() > 0) {
                ds.addColumnValues("roleid", 0, properties.getProperty("roleid"), separator);
                deleteSQL = deleteSQL + " AND roleid = ?";
            }
            if (properties.getProperty("privid").length() > 0) {
                ds.addColumnValues("privid", 0, properties.getProperty("privid"), separator);
                deleteSQL = deleteSQL + " AND privid = ?";
            }
            ds.padColumns();
            pstmt = this.database.prepareStatement("deleteroles", deleteSQL);
            for (int i = 0; i < ds.size(); ++i) {
                String keyid1 = ds.getString(i, "keyid1");
                try {
                    pstmt.setString(1, keyid1);
                    if (ds.isValidColumn("roleid")) {
                        pstmt.setString(2, ds.getString(i, "roleid"));
                    }
                    if (ds.isValidColumn("privid")) {
                        if (ds.isValidColumn("roleid")) {
                            pstmt.setString(3, ds.getString(i, "privid"));
                        } else {
                            pstmt.setString(2, ds.getString(i, "privid"));
                        }
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
            this.database.closeStatement("deleteroles");
            ds.reset();
            if (deleterset) {
                dam.clearRSet(rsetid);
            }
        }
    }
}

