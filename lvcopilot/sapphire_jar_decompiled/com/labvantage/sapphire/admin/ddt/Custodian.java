/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.EncryptDecrypt;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class Custodian
extends BaseSDCRules {
    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            String id = primary.getString(i, "custodianid");
            if (this.checkExist(id)) {
                this.throwError("CheckExists", "VALIDATION", "Custodian Id '" + id + "' already exists.");
            }
            if (!"Y".equals(primary.getString(i, "passwordflag"))) continue;
            String password = primary.getString(i, "password");
            this.checkPasswordExists(id, password);
            this.checkPasswordLength(id, password);
            boolean isCaseSensitivePasswords = true;
            primary.setString(i, "password", EncryptDecrypt.encryptJCE(password, isCaseSensitivePasswords));
            primary.setString(i, "casesensitivepasswordflag", isCaseSensitivePasswords ? "Y" : "N");
        }
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            String id = primary.getString(i, "custodianid");
            if (!"Y".equals(primary.getString(i, "passwordflag")) && !this.checkPasswordRequired(id)) continue;
            String password = primary.getString(i, "password");
            if (!password.equals("(storedpassword)")) {
                this.checkPasswordExists(id, password);
            }
            this.database.createPreparedResultSet("SELECT password FROM custodian WHERE custodianid = ?", new Object[]{id});
            if (!this.database.getNext() || password.equals(this.database.getString("password"))) continue;
            boolean isCaseSensitivePasswords = true;
            if (password.equals("(storedpassword)")) {
                primary.setString(i, "password", this.database.getString("password"));
            } else {
                this.checkPasswordLength(id, password);
                primary.setString(i, "password", EncryptDecrypt.encryptJCE(password, isCaseSensitivePasswords));
            }
            primary.setString(i, "casesensitivepasswordflag", isCaseSensitivePasswords ? "Y" : "N");
        }
    }

    private void checkPasswordExists(String sysuserid, String password) throws SapphireException {
        if (password == null || password.length() == 0) {
            this.throwError("CheckPasswordExists", "VALIDATION", "Password not defined for custodian: " + sysuserid);
        }
    }

    private void checkPasswordLength(String sysuserid, String password) throws SapphireException {
        if (password.length() > 14) {
            this.throwError("CheckPasswordLength", "VALIDATION", "Password can be a maximum of 14 characters for custodian: " + sysuserid);
        }
    }

    private boolean checkExist(String id) throws SapphireException {
        this.database.createPreparedResultSet("SELECT\tcustodianid FROM\tcustodian WHERE\tcustodianid = ?", new Object[]{id});
        return this.database.getNext();
    }

    private boolean checkPasswordRequired(String id) throws SapphireException {
        this.database.createPreparedResultSet("SELECT passwordflag FROM\tcustodian WHERE\tcustodianid = ?", new Object[]{id});
        if (this.database.getNext()) {
            return "Y".equals(this.database.getString("passwordflag"));
        }
        return false;
    }
}

