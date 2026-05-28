/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.misc;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.EncryptDecrypt;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class CheckCustodian
extends BaseAction
implements sapphire.action.CheckCustodian {
    public static final String RETURN_RESULT_YES = "Yes";
    public static final String RETURN_RESULT_NO = "No";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String result = "";
        String custodianId = properties.getProperty("custodianid");
        String password = properties.getProperty("custodianpwd");
        try {
            boolean isCorrectPwd;
            boolean passwordReqd;
            boolean custodianExists = this.checkCustodianIdExists(custodianId);
            result = !custodianExists ? RETURN_RESULT_NO : (!(passwordReqd = this.checkCustodianPassword(custodianId)) ? RETURN_RESULT_YES : ((isCorrectPwd = this.verifyCustodianPassword(custodianId, password)) ? RETURN_RESULT_YES : RETURN_RESULT_NO));
            properties.setProperty("result", result);
        }
        catch (SapphireException e) {
            throw new SapphireException("PROCESSACTION_FAILED", "Could not process Action: CheckCustodian " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
    }

    private boolean checkCustodianIdExists(String custodianId) throws SapphireException {
        boolean exists = false;
        String sql = null;
        try {
            sql = "select count(*) count from custodian where lower(custodianId) = ?";
            if (this.database.getPreparedCount(sql, new Object[]{custodianId.toLowerCase()}) > 0) {
                exists = true;
            }
        }
        catch (SapphireException e) {
            throw new SapphireException("DB_ACTION_FAILED", ErrorUtil.extractMessage("Failed query with sql. Reason: " + sql, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        return exists;
    }

    private boolean checkCustodianPassword(String custodianId) throws SapphireException {
        boolean needPwd = false;
        String sql = null;
        try {
            sql = "select passwordflag from custodian where lower(custodianid)=?";
            this.database.createPreparedResultSet(sql, new Object[]{custodianId.toLowerCase()});
            while (this.database.getNext()) {
                if (!this.database.getString("passwordflag").equalsIgnoreCase("Y")) continue;
                needPwd = true;
            }
            this.database.closeResultSet();
        }
        catch (SapphireException e) {
            throw new SapphireException("DB_ACTION_FAILED", ErrorUtil.extractMessage("Failed query with sql. Reason: " + sql, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        return needPwd;
    }

    private boolean verifyCustodianPassword(String custodianId, String pwd) throws SapphireException {
        boolean isCorrectPwd = false;
        boolean isCaseSensitivePassword = false;
        String pwdFromTable = "";
        String sql = null;
        try {
            sql = "select password, casesensitivepasswordflag from custodian where lower(custodianid)=?";
            this.database.createPreparedResultSet(sql, new Object[]{custodianId.toLowerCase()});
            while (this.database.getNext()) {
                pwdFromTable = this.database.getString("password");
                isCaseSensitivePassword = "Y".equals(this.database.getString("casesensitivepasswordflag"));
            }
            this.database.closeResultSet();
        }
        catch (Exception e) {
            throw new SapphireException("DB_ACTION_FAILED", ErrorUtil.extractMessage("Failed query with sql. Reason: " + sql, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        String encryptPwd = this.encryptPwd(pwd);
        if (pwdFromTable.equals(encryptPwd)) {
            isCorrectPwd = true;
        }
        if (!isCorrectPwd && pwdFromTable.equals(EncryptDecrypt.encryptJCE(pwd, isCaseSensitivePassword))) {
            isCorrectPwd = true;
        }
        return isCorrectPwd;
    }

    private String encryptPwd(String pwd) {
        String encryptedPwd = EncryptDecrypt.encrypt(pwd);
        return encryptedPwd;
    }
}

