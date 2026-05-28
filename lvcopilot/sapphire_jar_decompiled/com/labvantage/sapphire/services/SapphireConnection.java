/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.services;

import com.labvantage.sapphire.platform.SapphireDatabase;
import com.labvantage.sapphire.services.ConnectionInfo;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import sapphire.xml.PropertyList;

public class SapphireConnection
extends SapphireDatabase
implements ConnectionInfo {
    public static final String CONNECTIONID = "connectionid";
    public static final String SYSUSERID = "sysuserid";
    public static final String SYSUSERNAME = "sysusername";
    public static final String LOGONNAME = "logonname";
    public static final String PASSWORD = "password";
    public static final String DEVICEID = "deviceid";
    public static final String PORTALID = "portalid";
    public static final String EXTERNALAPPID = "externalappid";
    public static final String AUTHTOKENID = "authtokenid";
    public static final String GUIMODE = "guimode";
    public static final String TOOL = "tool";
    public static final String USERTYPE = "usertype";
    public static final String ROLELIST = "rolelist";
    public static final String MODULELIST = "modulelist";
    public static final String LANGUAGE = "language";
    public static final String LOCALE = "locale";
    public static final String TIMEZONE = "timezone";
    public static final String DEFAULTDEPARTMENT = "defaultdepartment";
    public static final String DEPARTMENTLIST = "departmentlist";
    public static final String CURRENTJOBTYPE = "lastlogonjobtype";
    public static final String JOBTYPELIST = "jobtypelist";
    public static final String IGNOREEXPIRYWARNING = "ignoreexpirywarning";
    public static final String SSOATTRIBUTES = "ssoattributes";
    private static final String UNKNOWN_DEVICE = "(Unknown)";
    private static final String UNKNOWN_TOOL = "(Unknown)";
    private String connectionId;
    protected String sysuserId;
    protected String sysuserName;
    protected String logonName;
    protected String password;
    private String deviceId;
    private String portalId;
    private String externalAppId;
    private String authTokenId;
    private String guiMode;
    private String tool;
    private String userType;
    private String serverUsername;
    private String serverPassword;
    private String roleList;
    private String moduleList;
    private String language;
    private String locale;
    private String timezone;
    private String defaultDepartment;
    private PropertyList profileProperties = new PropertyList();
    private Connection connection;
    private boolean changePassword;
    private boolean ignoreExpiryWarning;
    private Calendar lastAccessedDt;
    private Calendar connectDt;
    private Calendar deleteDt;
    private String connectFrom;
    private String remoteIP;
    private String remoteHost;
    private String userAgent;
    private String browser;
    private String departmentList;
    private String currentJobtype;
    private String jobtypeList;
    private String operatingSystem;
    private String connectionTypeFlag;
    private boolean rtlFlag;
    private boolean useFullIncludes;
    private String clearOldConnectionOption;
    private PropertyList ssoAttributes = new PropertyList();

    public SapphireConnection() {
    }

    public SapphireConnection(Connection connection, SapphireDatabase sapphireDatabase) {
        this.setConnection(connection);
        this.setSapphireDatabase(sapphireDatabase);
    }

    public SapphireConnection(Connection connection, ConnectionInfo connectionInfo) {
        this.setConnection(connection);
        this.setConnectionInfo(connectionInfo);
    }

    public SapphireConnection copy() {
        SapphireConnection newConnection = new SapphireConnection();
        newConnection.connectionId = this.connectionId;
        newConnection.sysuserId = this.sysuserId;
        newConnection.sysuserName = this.sysuserName;
        newConnection.logonName = this.logonName;
        newConnection.rtlFlag = this.rtlFlag;
        newConnection.useFullIncludes = this.useFullIncludes;
        newConnection.password = this.password;
        newConnection.deviceId = this.deviceId;
        newConnection.portalId = this.portalId;
        newConnection.externalAppId = this.externalAppId;
        newConnection.authTokenId = this.authTokenId;
        newConnection.guiMode = this.guiMode;
        newConnection.tool = this.tool;
        newConnection.userType = this.userType;
        newConnection.serverUsername = this.serverUsername;
        newConnection.serverPassword = this.serverPassword;
        newConnection.roleList = this.roleList;
        newConnection.moduleList = this.moduleList;
        newConnection.language = this.language;
        newConnection.locale = this.locale;
        newConnection.timezone = this.timezone;
        newConnection.defaultDepartment = this.defaultDepartment;
        newConnection.departmentList = this.departmentList;
        if (this.profileProperties != null) {
            newConnection.profileProperties = this.profileProperties.copy();
        }
        newConnection.changePassword = this.changePassword;
        newConnection.ignoreExpiryWarning = this.ignoreExpiryWarning;
        newConnection.lastAccessedDt = this.lastAccessedDt;
        newConnection.connectDt = this.connectDt;
        newConnection.deleteDt = this.deleteDt;
        newConnection.connectFrom = this.connectFrom;
        newConnection.currentJobtype = this.currentJobtype;
        newConnection.jobtypeList = this.jobtypeList;
        newConnection.userAgent = this.userAgent;
        newConnection.browser = this.browser;
        newConnection.operatingSystem = this.operatingSystem;
        newConnection.connectionTypeFlag = this.connectionTypeFlag;
        newConnection.databaseId = this.databaseId;
        newConnection.jndiname = this.jndiname;
        newConnection.dbms = this.dbms;
        newConnection.dbServer = this.dbServer;
        newConnection.sqlDatabase = this.sqlDatabase;
        newConnection.dbConnectionKey = this.dbConnectionKey;
        newConnection.port = this.port;
        newConnection.sid = this.sid;
        newConnection.ssoAttributes = this.ssoAttributes;
        return newConnection;
    }

    public void setConnectionInfo(ConnectionInfo connectionInfo) {
        this.connectionId = connectionInfo.getConnectionId();
        this.databaseId = connectionInfo.getDatabaseId();
        this.dbms = connectionInfo.getDbms();
        this.sysuserId = connectionInfo.getSysuserId();
        this.sysuserName = connectionInfo.getSysuserName();
        this.logonName = connectionInfo.getLogonName();
        this.rtlFlag = connectionInfo.isRtl();
        this.useFullIncludes = connectionInfo.getUseFullIncludes();
        this.password = connectionInfo.getPassword();
        this.userType = connectionInfo.getUserType();
        this.dbServer = connectionInfo.getDbServer();
        this.sqlDatabase = connectionInfo.getSqlDatabase();
        this.tool = connectionInfo.getTool();
        this.deviceId = connectionInfo.getDeviceId();
        this.portalId = connectionInfo.getPortalId();
        this.externalAppId = connectionInfo.getExternalAppId();
        this.authTokenId = connectionInfo.getAuthTokenId();
        this.guiMode = connectionInfo.getGuiMode();
        this.roleList = connectionInfo.getRoleList();
        this.moduleList = connectionInfo.getModuleList();
        this.language = connectionInfo.getLanguage();
        this.locale = connectionInfo.getLocale();
        this.timezone = connectionInfo.getTimeZone();
        this.defaultDepartment = connectionInfo.getDefaultDepartment();
        this.departmentList = connectionInfo.getDepartmentList();
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return this.connection;
    }

    public void setSapphireDatabase(SapphireDatabase sapphireDatabase) {
        this.databaseId = sapphireDatabase.getDatabaseId();
        this.jndiname = sapphireDatabase.getJndiname();
        this.dbms = sapphireDatabase.getDbms();
        this.dbServer = sapphireDatabase.getDbServer();
        this.port = sapphireDatabase.getPort();
        this.sid = sapphireDatabase.getSid();
        this.sqlDatabase = sapphireDatabase.getSqlDatabase();
        this.dbConnectionKey = sapphireDatabase.getDbConnectionKey();
    }

    public SapphireDatabase getSapphireDatabase() {
        SapphireDatabase sapphireDatabase = new SapphireDatabase();
        sapphireDatabase.setDatabaseId(this.databaseId);
        sapphireDatabase.setJndiname(this.jndiname);
        sapphireDatabase.setDbms(this.dbms);
        sapphireDatabase.setDbServer(this.dbServer);
        sapphireDatabase.setPort(this.port);
        sapphireDatabase.setSid(this.sid);
        sapphireDatabase.setSqlDatabase(this.sqlDatabase);
        sapphireDatabase.setDbConnectionKey(this.dbConnectionKey);
        return sapphireDatabase;
    }

    @Override
    public String getConnectionId() {
        return this.connectionId;
    }

    @Override
    public String getSysuserId() {
        return this.sysuserId;
    }

    @Override
    public String getSysuserName() {
        return this.sysuserName;
    }

    @Override
    public String getLogonName() {
        return this.logonName;
    }

    @Override
    public boolean isRtl() {
        return this.rtlFlag;
    }

    @Override
    public boolean getUseFullIncludes() {
        return this.useFullIncludes;
    }

    public void setSysuserId(String sysuserId) {
        this.sysuserId = sysuserId;
    }

    public void setSysuserName(String sysuserName) {
        this.sysuserName = sysuserName;
    }

    public void setLogonName(String logonName) {
        this.logonName = logonName;
    }

    public void setRtl(boolean rtl) {
        this.rtlFlag = rtl;
    }

    public void setUseFullIncludes(boolean useFullIncludes) {
        this.useFullIncludes = useFullIncludes;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getDeviceId() {
        return this.deviceId != null ? this.deviceId : "(Unknown)";
    }

    @Override
    public String getPortalId() {
        return this.portalId != null ? this.portalId : "";
    }

    @Override
    public String getExternalAppId() {
        return this.externalAppId != null ? this.externalAppId : "";
    }

    @Override
    public String getAuthTokenId() {
        return this.authTokenId;
    }

    @Override
    public String getGuiMode() {
        return this.guiMode != null ? this.guiMode : "(Unknown)";
    }

    @Override
    public String getTool() {
        return this.tool != null ? this.tool : "(Unknown)";
    }

    @Override
    public String getUserType() {
        return this.userType;
    }

    @Override
    public String getRoleList() {
        return this.roleList;
    }

    @Override
    public boolean hasRole(String role) {
        return (";" + this.roleList + ";").indexOf(";" + role + ";") != -1;
    }

    @Override
    public String getModuleList() {
        return this.moduleList;
    }

    @Override
    public boolean hasModule(String module) {
        return (";" + this.moduleList + ";").indexOf(";" + module + ";") != -1;
    }

    @Override
    public String getLanguage() {
        return this.language;
    }

    @Override
    public String getLocale() {
        return this.locale;
    }

    @Override
    public String getTimeZone() {
        return this.timezone;
    }

    @Override
    public String getDefaultDepartment() {
        return this.defaultDepartment;
    }

    @Override
    public String getDepartmentList() {
        return this.departmentList;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setPortalId(String portalId) {
        this.portalId = portalId;
    }

    public void setExternalAppId(String externalAppId) {
        this.externalAppId = externalAppId;
    }

    public void setAuthTokenId(String authTokenId) {
        this.authTokenId = authTokenId;
    }

    public void setGuimode(String guiMode) {
        this.guiMode = guiMode;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public void setRoleList(String roleList) {
        this.roleList = roleList;
    }

    public void setModuleList(String moduleList) {
        this.moduleList = moduleList;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public void setDefaultDepartment(String defaultDepartment) {
        this.defaultDepartment = defaultDepartment;
    }

    public void setDepartmentList(String departmentList) {
        this.departmentList = departmentList;
    }

    @Override
    public boolean isDepartmentMember(String department) {
        return (";" + this.departmentList + ";").indexOf(";" + department + ";") != -1;
    }

    public boolean isChangePassword() {
        return this.changePassword;
    }

    public void setChangePassword(boolean changePassword) {
        this.changePassword = changePassword;
    }

    public boolean isIgnoreExpiryWarning() {
        return this.ignoreExpiryWarning;
    }

    public void setIgnoreExpiryWarning(boolean ignoreExpiryWarning) {
        this.ignoreExpiryWarning = ignoreExpiryWarning;
    }

    public String getProfileProperty(String propertyid) {
        return this.profileProperties.getProperty(propertyid);
    }

    public void setProfileProperty(String propertyid, String propertyvalue) {
        this.profileProperties.setProperty(propertyid, propertyvalue);
        if (LANGUAGE.equals(propertyid)) {
            this.language = propertyvalue;
        }
    }

    public Set getProfileProperties() {
        return this.profileProperties.keySet();
    }

    public void setLastAccessedDt(Calendar lastAccessedDt) {
        this.lastAccessedDt = lastAccessedDt;
    }

    public Calendar getLastAccessedDt() {
        return this.lastAccessedDt;
    }

    public Calendar getConnectDt() {
        return this.connectDt;
    }

    public void setConnectDt(Timestamp connectDt) {
        if (connectDt != null) {
            Date tempdate = new Date();
            tempdate.setTime(connectDt.getTime());
            this.connectDt = Calendar.getInstance();
            this.connectDt.setTime(tempdate);
        }
    }

    public Calendar getDeleteDt() {
        return this.deleteDt;
    }

    public void setDeleteDt(Timestamp deleteDt) {
        if (deleteDt != null) {
            Date tempdate = new Date();
            tempdate.setTime(deleteDt.getTime());
            this.deleteDt = Calendar.getInstance();
            this.deleteDt.setTime(tempdate);
        }
    }

    public String getConnectFrom() {
        return this.connectFrom;
    }

    public void setConnectFrom(String connectFrom) {
        this.connectFrom = connectFrom;
    }

    public String getRemoteIP() {
        return this.remoteIP != null ? this.remoteIP : "";
    }

    public void setRemoteIP(String remoteIP) {
        this.remoteIP = remoteIP;
    }

    public String getRemoteHost() {
        return this.remoteHost != null ? this.remoteHost : "";
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getUserAgent() {
        return this.userAgent != null ? this.userAgent : "";
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getBrowser() {
        return this.browser != null ? this.browser : "";
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getOperatingSystem() {
        return this.operatingSystem != null ? this.operatingSystem : "";
    }

    public void setConnectionTypeFlag(String connectionTypeFlag) {
        this.connectionTypeFlag = connectionTypeFlag;
    }

    public String getConnectionTypeFlag() {
        return this.connectionTypeFlag != null ? this.connectionTypeFlag : "";
    }

    @Override
    public String getCurrentJobtype() {
        return this.currentJobtype;
    }

    public void setCurrentJobtype(String jobtype) {
        this.currentJobtype = jobtype;
    }

    @Override
    public String getJobtypeList() {
        return this.jobtypeList;
    }

    public void setJobtypeList(String jobtypeList) {
        this.jobtypeList = jobtypeList;
    }

    public String getClearOldConnectionOption() {
        return this.clearOldConnectionOption;
    }

    public void setClearOldConnectionOption(String clearOldConnectionOption) {
        this.clearOldConnectionOption = clearOldConnectionOption;
    }

    public PropertyList getSSOAttributes() {
        return this.ssoAttributes;
    }

    public void setSSOAttributes(PropertyList ssoAttributes) {
        this.ssoAttributes = ssoAttributes;
    }
}

