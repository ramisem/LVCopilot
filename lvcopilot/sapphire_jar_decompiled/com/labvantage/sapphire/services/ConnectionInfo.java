/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.services;

public interface ConnectionInfo {
    public static final String MODULE_ASL = "ASL";
    public static final String MODULE_SECURITY = "Security";
    public static final String MODULE_SCHEDULER = "Scheduler";
    public static final String MODULE_SMS = "SMS";
    public static final String MODULE_STABILITY = "Stability";
    public static final String MODULE_SYSTEMCONFIG = "SystemConfig";
    public static final String MODULE_WPDSTD = "WPDStd";
    public static final String MODULE_WPDPRO = "WPDPro";
    public static final String MODULE_SAMPLEMONITORING = "SampleMonitoring";

    public String getConnectionId();

    public String getDatabaseId();

    public String getSysuserId();

    public String getSysuserName();

    public String getLogonName();

    public String getPassword();

    public String getDeviceId();

    public String getPortalId();

    public String getExternalAppId();

    public String getAuthTokenId();

    public String getGuiMode();

    public String getTool();

    public String getDbms();

    public String getUserType();

    public String getDbServer();

    public String getSqlDatabase();

    public String getRoleList();

    public boolean hasRole(String var1);

    public String getModuleList();

    public boolean hasModule(String var1);

    public String getLanguage();

    public String getLocale();

    public String getTimeZone();

    public boolean isOracle();

    public boolean isSqlServer();

    public String getDefaultDepartment();

    public String getDepartmentList();

    public boolean isDepartmentMember(String var1);

    public String getCurrentJobtype();

    public String getJobtypeList();

    public boolean isRtl();

    public boolean getUseFullIncludes();
}

