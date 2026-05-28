/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.EncryptDecrypt;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddCOCEntry
extends BaseAction
implements sapphire.action.AddCOCEntry {
    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String[] keyid3s;
        String[] keyid2s;
        boolean isPasswordCorrect;
        String sdcid = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");
        String keyid2 = properties.getProperty("keyid2");
        String keyid3 = properties.getProperty("keyid3");
        String fromCustodianid = properties.getProperty("fromcustodianid");
        String fromCustodianPwd = properties.getProperty("fromcustodianpwd");
        String toCustodianId = properties.getProperty("tocustodianid");
        String toCustodianPwd = properties.getProperty("tocustodianpwd");
        if (fromCustodianid == null) throw new SapphireException("PROCESSACTION_FAILED", "Could not process Action: AddCOCEntry. fromcustodianid is null. ");
        boolean fromCustodianExists = this.checkCustodianIdExists(fromCustodianid);
        if (!fromCustodianExists) throw new SapphireException("PROCESSACTION_FAILED", "Could not process Action: AddCOCEntry. fromcustodianid does not exist. ");
        boolean fromCustodianPwdRequired = this.checkCustodianPassword(fromCustodianid);
        if (fromCustodianPwdRequired && !(isPasswordCorrect = this.verifyCustodianPassword(fromCustodianid, fromCustodianPwd))) {
            throw new SapphireException("PROCESSACTION_FAILED", "Could not process Action: AddCOCEntry. Password for fromcustodianid is either wrong or null. ");
        }
        if (toCustodianId == null) throw new SapphireException("PROCESSACTION_FAILED", "Could not process Action: AddCOCEntry. tocustodianid is null. ");
        boolean toCustodianExists = this.checkCustodianIdExists(toCustodianId);
        if (!toCustodianExists) throw new SapphireException("PROCESSACTION_FAILED", "Could not process Action: AddCOCEntry. tocustodianid does not exist. ");
        boolean toCustodianPwdRequired = this.checkCustodianPassword(toCustodianId);
        if (toCustodianPwdRequired && !(isPasswordCorrect = this.verifyCustodianPassword(toCustodianId, toCustodianPwd))) {
            throw new SapphireException("PROCESSACTION_FAILED", "Could not process Action: AddCOCEntry. Password for tocustodianid is either wrong or null. ");
        }
        String[] keyid1s = StringUtil.split(keyid1, ";");
        int count = this.isNewCOC(sdcid, keyid1 = keyid1s[0], keyid2 = (keyid2s = StringUtil.split(keyid2, ";")).length == 0 || keyid2s[0].length() == 0 ? "(null)" : keyid2s[0], keyid3 = (keyid3s = StringUtil.split(keyid3, ";")).length == 0 || keyid3s[0].length() == 0 ? "(null)" : keyid3s[0]);
        if (count == -1) {
            if (fromCustodianid.equals(toCustodianId)) {
                throw new SapphireException("PROCESSACTION_FAILED", "Could not process Action: AddCOCEntry. fromcustodianid and tocustodianid are the same. ");
            }
            this.doCOCAdd(properties, keyid1s, keyid2s, keyid3s, 0);
            return;
        } else {
            String prevToCustodianId = this.getLastToCustodianId(count);
            properties.setProperty("fromcustodianid", prevToCustodianId);
            if (prevToCustodianId.equals(toCustodianId)) {
                throw new SapphireException("PROCESSACTION_FAILED", "Could not process Action: AddCOCEntry. fromcustodianid and tocustodianid are the same. ");
            }
            this.doCOCAdd(properties, keyid1s, keyid2s, keyid3s, count + 1);
        }
    }

    private boolean checkCustodianIdExists(String custodianId) throws SapphireException {
        boolean exists = false;
        this.database.createPreparedResultSet("CheckCustodianIdExists", "SELECT count(*) count FROM custodian WHERE custodianId = ?", new Object[]{custodianId});
        while (this.database.getNext("CheckCustodianIdExists")) {
            if (this.database.getInt("CheckCustodianIdExists", "count") <= 0) continue;
            exists = true;
        }
        this.database.closeResultSet("CheckCustodianIdExists");
        return exists;
    }

    private boolean checkCustodianPassword(String custodianId) throws SapphireException {
        boolean needPwd = false;
        this.database.createPreparedResultSet("CheckCustodianPassword", "select passwordflag from custodian where custodianid=?", new Object[]{custodianId});
        while (this.database.getNext("CheckCustodianPassword")) {
            if (!this.database.getString("CheckCustodianPassword", "passwordflag").equalsIgnoreCase("Y")) continue;
            needPwd = true;
        }
        this.database.closeResultSet("CheckCustodianPassword");
        return needPwd;
    }

    private boolean verifyCustodianPassword(String custodianId, String pwd) throws SapphireException {
        boolean isCorrectPwd = false;
        boolean isCaseSensitivePassword = false;
        String pwdFromTable = "";
        this.database.createPreparedResultSet("VerifyCustodianPassword", "select password, casesensitivepasswordflag from custodian where custodianid=?", new Object[]{custodianId});
        while (this.database.getNext("VerifyCustodianPassword")) {
            pwdFromTable = this.database.getString("VerifyCustodianPassword", "password");
            isCaseSensitivePassword = "Y".equals(this.database.getString("VerifyCustodianPassword", "casesensitivepasswordflag"));
        }
        this.database.closeResultSet("VerifyCustodianPassword");
        if (pwd != null && pwd.indexOf("{|}") == 0) {
            pwd = EncryptDecrypt.decryptRSA(pwd.substring(3));
        }
        if (EncryptDecrypt.encryptJCE(pwd, isCaseSensitivePassword).equals(pwdFromTable)) {
            isCorrectPwd = true;
        } else if (pwdFromTable != null && pwdFromTable.equals(EncryptDecrypt.encrypt(EncryptDecrypt.decrypt(pwdFromTable))) && pwd.equalsIgnoreCase(EncryptDecrypt.decrypt(pwdFromTable))) {
            isCorrectPwd = true;
            SafeSQL safeSQL = new SafeSQL();
            this.database.executePreparedUpdate("update custodian set password=" + safeSQL.addVar(EncryptDecrypt.encryptJCE(pwd, isCaseSensitivePassword)) + "  casesensitivepasswordflag=" + safeSQL.addVar(isCaseSensitivePassword ? "Y" : "N") + " where custodianid = " + safeSQL.addVar(custodianId), safeSQL.getValues());
        }
        return isCorrectPwd;
    }

    private int isNewCOC(String sdcid, String keyid1, String keyid2, String keyid3) throws SapphireException {
        int count = -1;
        this.database.createPreparedResultSet("IsNewCOC", "SELECT count(*) count FROM sdicoc WHERE sdcid=? and keyid1=? and keyid2=? and keyid3=?", new Object[]{sdcid, keyid1, keyid2, keyid3});
        while (this.database.getNext("IsNewCOC")) {
            count = this.database.getInt("IsNewCOC", "count");
        }
        this.database.closeResultSet("IsNewCOC");
        if (count > 0) {
            this.database.createPreparedResultSet("IsNewCOC", "SELECT MAX(cocid) maxcocid FROM sdicoc WHERE sdcid=? and keyid1=? and keyid2=? and keyid3=?", new Object[]{sdcid, keyid1, keyid2, keyid3});
            while (this.database.getNext("IsNewCOC")) {
                count = this.database.getInt("IsNewCOC", "maxcocid");
            }
            this.database.closeResultSet("IsNewCOC");
        } else {
            count = -1;
        }
        return count;
    }

    private String getLastToCustodianId(int count) throws SapphireException {
        String toCustodianId = "";
        String sql = "SELECT tocustodianid FROM sdicoc WHERE cocid=?";
        this.database.createPreparedResultSet("GetLastToCustodianId", sql, new Object[]{count});
        while (this.database.getNext("GetLastToCustodianId")) {
            toCustodianId = this.database.getString("GetLastToCustodianId", "tocustodianid");
        }
        this.database.closeResultSet("GetLastToCustodianId");
        return toCustodianId;
    }

    private void doCOCAdd(PropertyList properties, String[] keyid1s, String[] keyid2s, String[] keyid3s, int count) throws SapphireException {
        DataSet cocEntries = new DataSet();
        cocEntries.addColumn("keyid1", 0);
        cocEntries.addColumn("keyid2", 0);
        cocEntries.addColumn("keyid3", 0);
        cocEntries.addColumn("cocid", 1);
        cocEntries.addColumn("createby", 0);
        cocEntries.addColumn("createdt", 2);
        for (int i = 0; i < keyid1s.length; ++i) {
            this.logger.info("Adding row for: " + keyid1s[i]);
            int row = cocEntries.addRow();
            cocEntries.setString(row, "keyid1", keyid1s[i]);
            String keyid2 = keyid2s[i];
            String keyid3 = keyid3s[i];
            if (keyid2s.length < keyid1s.length || keyid2s[i] == null || keyid2s[i].length() <= 0) {
                keyid2 = "(null)";
            }
            cocEntries.setString(row, "keyid2", keyid2);
            if (keyid3s.length < keyid1s.length || keyid3s[i] == null || keyid3s[i].length() <= 0) {
                keyid3 = "(null)";
            }
            cocEntries.setString(row, "keyid3", keyid3);
            cocEntries.setValue(i, "cocid", String.valueOf(count));
            cocEntries.setValue(i, "createdt", "n");
            cocEntries.setValue(i, "createby", this.connectionInfo.getSysuserId());
            this.logger.info("Setting the values");
            this.database.createResultSet("DoCOCAdd", "SELECT columnid, datatype FROM syscolumn WHERE lower( tableid ) = 'sdicoc'");
            while (this.database.getNext("DoCOCAdd")) {
                String id = this.database.getString("DoCOCAdd", "columnid");
                String value = properties.getProperty(id);
                if (StringUtil.getLen(value) <= 0L) continue;
                this.logger.info("Adding the column '" + id + "'");
                String datatype = this.database.getString("DoCOCAdd", "datatype");
                if (datatype.equalsIgnoreCase("C")) {
                    cocEntries.addColumn(id, 0);
                } else if (datatype.equalsIgnoreCase("N") || datatype.equalsIgnoreCase("R")) {
                    cocEntries.addColumn(id, 1);
                } else {
                    cocEntries.addColumn(id, 2);
                    if ("Y".equals(this.getSDCProcessor().getSDCColumnProperty("COC", id, "timezoneindependent"))) {
                        cocEntries.setTimeZoneInsensitive(id);
                    }
                }
                this.logger.info("Setting the value '" + value + "'");
                if (id.equalsIgnoreCase("keyid1") || id.equalsIgnoreCase("keyid2") || id.equalsIgnoreCase("keyid3") || id.equalsIgnoreCase("cocid")) continue;
                cocEntries.setValue(i, id, properties.getProperty(id));
            }
        }
        this.logger.info("Update the database");
        DataSetUtil.insert(this.database, cocEntries, "sdicoc");
    }
}

