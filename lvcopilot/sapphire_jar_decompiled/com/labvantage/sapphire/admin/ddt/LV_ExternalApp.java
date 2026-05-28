/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.util.cache.CacheNames;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.util.Arrays;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LV_ExternalApp
extends BaseSDCRules
implements CacheNames {
    private int newNonCoreExternalAppCount = 0;

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            if (primary.isNull(i, "authorizationcode")) {
                String authorizationCode = LV_ExternalApp.getNextAuthorizationCode(this.database, this.getDatabaseid());
                primary.setString(i, "authorizationcode", authorizationCode);
            }
            if (primary.isNull(i, "externalappstatus")) {
                primary.setString(i, "externalappstatus", "Disabled");
            }
            if (!Configuration.isDevmode(this.connectionInfo.getDatabaseId())) {
                primary.setString(i, "coreflag", "N");
            }
            this.updateNonCoreExternalAppCount(primary, i, true);
        }
        this.validateNonCoreExternalAppCount();
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (primary != null) {
            for (int i = 0; i < primary.size(); ++i) {
                this.updateNonCoreExternalAppCount(primary, i, false);
            }
            this.validateNonCoreExternalAppCount();
        }
    }

    private static synchronized String getNextAuthorizationCode(DBAccess db, String databaseid) throws SapphireException {
        String millis = "" + System.currentTimeMillis();
        String authorizationCode = databaseid + "|" + String.valueOf(Math.round(Math.random() * 1.0E7 * 1000000.0)) + millis.substring(millis.length() - 4);
        authorizationCode = EncryptDecrypt.encrypt(authorizationCode);
        while (db.checkPreparedExists("SELECT authorizationcode FROM externalapp WHERE authorizationcode = ?", new Object[]{authorizationCode})) {
            millis = "" + System.currentTimeMillis();
            authorizationCode = databaseid + "|" + String.valueOf(Math.round(Math.random() * 1.0E7 * 1000000.0)) + millis.substring(millis.length() - 4);
            authorizationCode = EncryptDecrypt.encrypt(authorizationCode);
        }
        return authorizationCode;
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.flushCaches(actionProps);
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT coreflag FROM externalapp, rsetitems WHERE externalapp.externalappid = rsetitems.keyid1 AND rsetid = ?", new Object[]{rsetid});
        for (int i = 0; i < ds.size(); ++i) {
            if (!ds.getString(i, "coreflag", "N").equalsIgnoreCase("Y")) continue;
            throw new SapphireException("You cannot delete 'Core' External App");
        }
    }

    @Override
    public void postDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        this.flushCaches(actionProps);
    }

    public void flushCaches(PropertyList actionProps) {
        HashSet<String> externalAppSet = new HashSet<String>();
        externalAppSet.addAll(Arrays.asList(StringUtil.split(actionProps.getProperty("keyid1"), ";")));
        for (String externalappid : externalAppSet) {
            CacheUtil.remove(this.getDatabaseid(), "ExternalApp", externalappid);
        }
    }

    private void updateNonCoreExternalAppCount(DataSet primary, int i, boolean add) throws SapphireException {
        if (add) {
            String coreflag = primary.getString(i, "coreflag", "N");
            String activeflag = primary.getString(i, "activeflag", "Y");
            if (coreflag.equalsIgnoreCase("N") && activeflag.equalsIgnoreCase("Y")) {
                ++this.newNonCoreExternalAppCount;
            }
        } else if (this.hasPrimaryValueChanged(primary, i, "coreflag") || this.hasPrimaryValueChanged(primary, i, "activeflag")) {
            boolean isNonCore;
            String newcoreflag = this.getColumnValue(primary, i, "coreflag", "N");
            String oldcoreflag = this.getOldPrimaryColumnValue(primary, i, "coreflag", "N");
            String newActiveflag = this.getColumnValue(primary, i, "activeflag", "Y");
            String oldActiveflag = this.getOldPrimaryColumnValue(primary, i, "activeflag", "Y");
            boolean wasNonCore = oldcoreflag.equalsIgnoreCase("N") && oldActiveflag.equalsIgnoreCase("Y");
            boolean bl = isNonCore = newcoreflag.equalsIgnoreCase("N") && newActiveflag.equalsIgnoreCase("Y");
            if (wasNonCore && !isNonCore) {
                --this.newNonCoreExternalAppCount;
            } else if (!wasNonCore && isNonCore) {
                ++this.newNonCoreExternalAppCount;
            }
        }
    }

    private void validateNonCoreExternalAppCount() throws SapphireException {
        if (this.newNonCoreExternalAppCount > 0) {
            int maxNonCoreExternalAppCount = Configuration.getInstance().getLicense(this.getDatabaseid()).getNonCoreExternalAppCount();
            this.database.createResultSet("SELECT count(*) count FROM externalapp WHERE ( activeflag = 'Y' OR activeflag IS NULL ) AND (coreflag = 'N' OR coreflag IS NULL)");
            int existingNonCoreExternalAppCount = 0;
            if (this.database.getNext()) {
                existingNonCoreExternalAppCount = this.database.getInt("count");
            }
            if (this.newNonCoreExternalAppCount + existingNonCoreExternalAppCount > maxNonCoreExternalAppCount) {
                this.throwError("SDMSInstrumentCount", "VALIDATION", this.getTranslationProcessor().translate("You have exceeded maximum number of allowable Non-Core External Apps."));
            }
        }
    }

    private String getColumnValue(DataSet primary, int indx, String columnid, String defaultvalue) {
        String value = "";
        value = primary.isValidColumn(columnid) ? primary.getString(indx, columnid, "") : this.getOldPrimaryValue(primary, indx, columnid);
        if (value == null || value.length() == 0) {
            value = defaultvalue;
        }
        return value;
    }

    private String getOldPrimaryColumnValue(DataSet primary, int i, String columnid, String defaultvalue) {
        String value = this.getOldPrimaryValue(primary, i, columnid);
        if (value == null || value.length() == 0) {
            value = defaultvalue;
        }
        return value;
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }
}

