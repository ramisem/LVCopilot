/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.License;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.util.Enumeration;
import java.util.Properties;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LV_App
extends BaseSDCRules {
    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (!Configuration.isDevmode(this.connectionInfo.getDatabaseId())) {
            primary.setString(-1, "coreflag", "N");
        } else {
            primary.setString(-1, "coreflag", "Y");
        }
        primary.setString(-1, "licensedflag", "N");
        primary.setString(-1, "maxusers", "");
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String appid = primary.getString(i, "appid");
            CacheUtil.remove(this.getDatabaseid(), "AppDetails", appid);
            CacheUtil.remove(this.getDatabaseid(), "AppPropertyList", appid);
            if (Configuration.isDevmode(this.connectionInfo.getDatabaseId()) && Configuration.getCompcode(this.connectionInfo.getDatabaseId()).length() != 0) continue;
            String sql = "SELECT a.appid, a.coreflag, a.compcode FROM app a WHERE a.appid = ?";
            DataSet modifiedApp = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{appid});
            if ((!modifiedApp.getString(0, "coreflag", "").equalsIgnoreCase("Y") || Configuration.isDevmode(this.connectionInfo.getDatabaseId())) && (modifiedApp.getString(0, "compcode", "").isEmpty() || !Configuration.getCompcode(this.connectionInfo.getDatabaseId()).equalsIgnoreCase(modifiedApp.getString(0, "compcode"))) || !this.hasPrimaryValueChanged(primary, i, "licensedflag") && !this.hasPrimaryValueChanged(primary, i, "maxusers") && !this.hasPrimaryValueChanged(primary, i, "coreflag") && !this.hasPrimaryValueChanged(primary, i, "compcode")) continue;
            throw new SapphireException("CoreAppCheck", "VALIDATION", this.getTranslationProcessor().translate("You are not allowed to modify any of the Core columns."));
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        String[] apps;
        String sql;
        if (!Configuration.isDevmode(this.connectionInfo.getDatabaseId())) {
            sql = "SELECT a.appid, a.coreflag FROM app a, rsetitems r WHERE a.appid = r.keyid1 AND r.rsetid = ? AND a.coreflag = 'Y'";
            DataSet deletedApps = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{rsetid});
            if (deletedApps.getRowCount() > 0) {
                throw new SapphireException("DeleteCoreApps", "VALIDATION", this.getTranslationProcessor().translate("Core App SDIs cannot be deleted."));
            }
        }
        sql = "SELECT a.appid FROM app a, rsetitems r WHERE a.appid = r.keyid1 AND r.rsetid = ? AND a.compcode is not null AND a.compcode != ?";
        DataSet deletedCompApps = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{rsetid, Configuration.getCompcode(this.connectionInfo.getDatabaseId())});
        if (deletedCompApps.getRowCount() > 0) {
            throw new SapphireException("DeleteComponentApps", "VALIDATION", this.getTranslationProcessor().translate("Component App SDIs cannot be deleted."));
        }
        for (String appid : apps = StringUtil.split(actionProps.getProperty("appid", actionProps.getProperty("keyid1")), ";")) {
            CacheUtil.remove(this.getDatabaseid(), "AppDetails", appid);
            CacheUtil.remove(this.getDatabaseid(), "AppPropertyList", appid);
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            String appid = primary.getString(i, "appid");
            CacheUtil.remove(this.getDatabaseid(), "AppDetails", appid);
            CacheUtil.remove(this.getDatabaseid(), "AppPropertyList", appid);
        }
    }

    @Override
    public void postAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (sdiData.getDatasets().contains("appsysuser")) {
            LV_App.checkAppLicenses(this.getDatabaseid(), this.database, this.getQueryProcessor());
        }
    }

    @Override
    public void postEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (sdiData.getDatasets().contains("appsysuser")) {
            LV_App.checkAppLicenses(this.getDatabaseid(), this.database, this.getQueryProcessor());
        }
    }

    public static void checkAppLicenses(String databaseId, DBAccess database, QueryProcessor queryProcessor) throws SapphireException {
        Properties licensedAppProperties = Configuration.getInstance().getLicense(databaseId).getAppProperties();
        Enumeration<?> e = licensedAppProperties.propertyNames();
        while (e.hasMoreElements()) {
            String licenseAppId = (String)e.nextElement();
            String appLicenseCount = licensedAppProperties.getProperty(licenseAppId);
            LV_App.checkAppLicense(licenseAppId, appLicenseCount, database, queryProcessor);
        }
    }

    public static void checkAppLicense(String licenseAppId, String maxUsersCount, DBAccess database, QueryProcessor queryProcessor) throws SapphireException {
        String[][] licensedApps = License.getApps();
        boolean isAppRegistered = false;
        for (String[] appInfo : licensedApps) {
            if (!appInfo[0].equals(licenseAppId)) continue;
            isAppRegistered = true;
        }
        if (!isAppRegistered) {
            throw new SapphireException("UnRegisteredApp", "VALIDATION", "Unregistered App found in License: " + licenseAppId);
        }
        if (!"U".equalsIgnoreCase(maxUsersCount) && !"S".equalsIgnoreCase(maxUsersCount)) {
            String sql = "SELECT COUNT(appsysuser.sysuserid) FROM appsysuser, sysuser WHERE appsysuser.sysuserid = sysuser.sysuserid AND sysuser.templateflag != 'Y'AND sysuser.activeflag != 'N'AND appid = ?";
            int dbCount = 0;
            dbCount = database != null ? database.getPreparedCount(sql, new Object[]{licenseAppId}) : queryProcessor.getPreparedCount(sql, new Object[]{licenseAppId});
            if (dbCount > Integer.parseInt(maxUsersCount)) {
                throw new SapphireException("MaxAssignmentExceeded", "VALIDATION", "App user assignment count exceeds license for App: '" + licenseAppId + "'");
            }
        }
    }
}

