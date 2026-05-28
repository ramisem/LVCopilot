/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.opal.handler.ErrorUtil;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.action.BaseAction;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class DeleteSDISecurityDep
extends BaseAction
implements sapphire.action.DeleteSDISecurityDep {
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
        String departmentid = properties.getProperty("departmentid", "");
        String operationid = properties.getProperty("operationid", "");
        if (sdcid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTIES", "sdcid is mandatory");
        }
        if (keyid1p.length() == 0) {
            throw new SapphireException("INVALID_PROPERTIES", "keyid1 is mandatory");
        }
        if (departmentid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTIES", "Department is mandatory");
        }
        if (rsetid.length() == 0) {
            boolean applylock = properties.getProperty("applylock").equals("Y");
            dam = this.getDAMProcessor();
            rsetid = applylock ? dam.createLockedRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3")) : dam.createRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"));
            deleterset = true;
        }
        try {
            String separator = properties.getProperty("separator", ";");
            String[] keyid1prop = StringUtil.split(properties.getProperty("keyid1"), separator);
            String[] keyid2prop = StringUtil.split(properties.getProperty("keyid2"), separator);
            String[] keyid3prop = StringUtil.split(properties.getProperty("keyid3"), separator);
            String[] departmentidprop = StringUtil.split(properties.getProperty("departmentid"), separator);
            String[] operationidprop = StringUtil.split(operationid, separator);
            for (int i = 0; i < keyid1prop.length; ++i) {
                String keyid1 = keyid1prop[i];
                String keyid2 = keyid2prop.length == 0 || keyid2prop.length < keyid1prop.length || keyid2prop[i].length() == 0 ? "(null)" : keyid2prop[i];
                String keyid3 = keyid3prop.length == 0 || keyid3prop.length < keyid1prop.length || keyid3prop[i].length() == 0 ? "(null)" : keyid3prop[i];
                for (int j = 0; j < departmentidprop.length; ++j) {
                    if (StringUtil.getLen(operationid) > 0L && departmentidprop.length == operationidprop.length) {
                        this.deleteSDISecurityDepartment(sdcid, keyid1, keyid2, keyid3, departmentidprop[j], operationidprop[j]);
                        continue;
                    }
                    this.deleteSDISecurityDepartment(sdcid, keyid1, keyid2, keyid3, departmentidprop[j], null);
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
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void deleteSDISecurityDepartment(String sdcid, String keyid1, String keyid2, String keyid3, String department, String operation) throws SapphireException, SQLException {
        String deleteSQL = "DELETE FROM sdisecuritydepartment WHERE sdcid = '" + sdcid + "' AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND securitydepartment = ?";
        if (StringUtil.getLen(operation) > 0L) {
            deleteSQL = deleteSQL + "  AND operationid = ?";
        }
        PreparedStatement pstmt = null;
        try {
            pstmt = this.database.prepareStatement("psmt", deleteSQL);
            pstmt.setString(1, keyid1);
            pstmt.setString(2, keyid2);
            pstmt.setString(3, keyid3);
            pstmt.setString(4, department);
            if (StringUtil.getLen(operation) > 0L) {
                pstmt.setString(5, operation);
            }
            this.logger.info("Deleting the sdisecuritydepartment record of {" + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3 + ";" + department + "}");
            pstmt.executeUpdate();
        }
        finally {
            if (pstmt != null) {
                this.database.closeStatement("psmt");
            }
        }
    }
}

