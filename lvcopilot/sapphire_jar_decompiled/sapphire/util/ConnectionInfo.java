/*
 * Decompiled with CFR 0.152.
 */
package sapphire.util;

import com.labvantage.sapphire.BaseClass;
import com.labvantage.sapphire.platform.SapphireDatabase;
import com.labvantage.sapphire.services.SapphireConnection;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import sapphire.util.StringUtil;

public class ConnectionInfo
extends BaseClass
implements com.labvantage.sapphire.services.ConnectionInfo,
Serializable {
    private String connectionId;
    private String databaseId;
    private String sysuserId;
    private String sysuserName;
    private String logonName;
    private String password;
    private String deviceId;
    private String portalId;
    private String externalAppId;
    private String authTokenId;
    private String guiMode;
    private String tool;
    private String dbms;
    private String userType;
    private String dbServer;
    private String serverUsername;
    private String serverPassword;
    private String sqlDatabase;
    private String roleList;
    private String moduleList;
    private String language;
    private String locale;
    private String timezone;
    private String defaultDepartment;
    private String departmentList;
    private String currentJobtype;
    private String jobtypeList;
    private boolean rtlFlag;
    private boolean useFullIncludes;

    public ConnectionInfo(SapphireConnection sapphireConnection) {
        this.connectionId = sapphireConnection.getConnectionId();
        this.databaseId = sapphireConnection.getDatabaseId();
        this.sysuserId = sapphireConnection.getSysuserId();
        this.sysuserName = sapphireConnection.getSysuserName();
        this.logonName = sapphireConnection.getLogonName();
        this.password = sapphireConnection.getPassword();
        this.deviceId = sapphireConnection.getDeviceId();
        this.portalId = sapphireConnection.getPortalId();
        this.externalAppId = sapphireConnection.getExternalAppId();
        this.authTokenId = sapphireConnection.getAuthTokenId();
        this.guiMode = sapphireConnection.getGuiMode();
        this.tool = sapphireConnection.getTool();
        this.dbms = sapphireConnection.getDbms();
        this.userType = sapphireConnection.getUserType();
        this.dbServer = sapphireConnection.getDbServer();
        this.sqlDatabase = sapphireConnection.getSqlDatabase();
        this.roleList = sapphireConnection.getRoleList();
        this.moduleList = sapphireConnection.getModuleList();
        this.language = sapphireConnection.getLanguage();
        this.locale = sapphireConnection.getLocale();
        this.timezone = sapphireConnection.getTimeZone();
        this.defaultDepartment = sapphireConnection.getDefaultDepartment();
        this.departmentList = sapphireConnection.getDepartmentList();
        this.currentJobtype = sapphireConnection.getCurrentJobtype();
        this.jobtypeList = sapphireConnection.getJobtypeList();
        this.rtlFlag = sapphireConnection.isRtl();
        this.useFullIncludes = sapphireConnection.getUseFullIncludes();
    }

    public ConnectionInfo(SapphireDatabase sapphireDatabase) {
        this.databaseId = sapphireDatabase.getDatabaseId();
        this.dbms = sapphireDatabase.getDbms();
        this.dbServer = sapphireDatabase.getDbServer();
        this.sqlDatabase = sapphireDatabase.getSqlDatabase();
    }

    public ConnectionInfo(HashMap userAttributeMap) {
        this.databaseId = (String)userAttributeMap.get("databaseid");
        this.sysuserId = (String)userAttributeMap.get("sysuserid");
        this.sysuserName = (String)userAttributeMap.get("sysusername");
        this.logonName = (String)userAttributeMap.get("logonname");
        this.password = (String)userAttributeMap.get("password");
        this.deviceId = (String)userAttributeMap.get("deviceid");
        this.portalId = (String)userAttributeMap.get("portalid");
        this.externalAppId = (String)userAttributeMap.get("externalappid");
        this.authTokenId = (String)userAttributeMap.get("authtokenid");
        this.guiMode = (String)userAttributeMap.get("guimode");
        this.tool = (String)userAttributeMap.get("tool");
        this.dbms = (String)userAttributeMap.get("dbms");
        this.userType = (String)userAttributeMap.get("usertype");
        this.roleList = this.convertToDelimeteredString((List)userAttributeMap.get("rolelist"));
        this.moduleList = this.convertToDelimeteredString((List)userAttributeMap.get("modulelist"));
        this.language = (String)userAttributeMap.get("language");
        this.locale = (String)userAttributeMap.get("locale");
        this.timezone = (String)userAttributeMap.get("timezone");
        this.defaultDepartment = (String)userAttributeMap.get("defaultdepartment");
        this.departmentList = this.convertToDelimeteredString((List)userAttributeMap.get("departmentlist"));
        this.currentJobtype = (String)userAttributeMap.get("lastlogonjobtype");
        this.jobtypeList = this.convertToDelimeteredString((List)userAttributeMap.get("jobtypelist"));
    }

    public ConnectionInfo(String nameserverlist, String username, String password) {
        this.serverUsername = username;
        this.serverPassword = password;
    }

    public ConnectionInfo(String username, String password) {
        this.serverUsername = username;
        this.serverPassword = password;
    }

    @Override
    public String getConnectionId() {
        return this.connectionId;
    }

    @Override
    public String getDatabaseId() {
        return this.databaseId;
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
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getDeviceId() {
        return this.deviceId;
    }

    @Override
    public String getPortalId() {
        return this.portalId;
    }

    @Override
    public String getExternalAppId() {
        return this.externalAppId;
    }

    @Override
    public String getAuthTokenId() {
        return this.authTokenId;
    }

    @Override
    public String getGuiMode() {
        return this.guiMode;
    }

    @Override
    public String getTool() {
        return this.tool;
    }

    @Override
    public String getDbms() {
        return this.dbms;
    }

    @Override
    public String getUserType() {
        return this.userType;
    }

    @Override
    public String getDbServer() {
        return this.dbServer;
    }

    public String getNameServerList() {
        return "";
    }

    public String getServerUsername() {
        return this.serverUsername;
    }

    public String getServerPassword() {
        return this.serverPassword;
    }

    @Override
    public String getSqlDatabase() {
        return this.sqlDatabase;
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
    public boolean isOracle() {
        return this.dbms.equals("ORA");
    }

    @Override
    public boolean isSqlServer() {
        return this.dbms.equals("MSS");
    }

    @Override
    public String getDefaultDepartment() {
        return this.defaultDepartment;
    }

    @Override
    public String getDepartmentList() {
        return this.departmentList;
    }

    @Override
    public boolean isDepartmentMember(String department) {
        return (";" + this.departmentList + ";").indexOf(";" + department + ";") != -1;
    }

    @Override
    public String getCurrentJobtype() {
        return this.currentJobtype;
    }

    @Override
    public String getJobtypeList() {
        return this.jobtypeList;
    }

    @Override
    public boolean isRtl() {
        return this.rtlFlag;
    }

    @Override
    public boolean getUseFullIncludes() {
        return this.useFullIncludes;
    }

    public String toString() {
        StringBuffer out = new StringBuffer();
        out.append("CONNECTIONID=" + this.connectionId + ", ");
        out.append("DATABASEID=" + this.databaseId + ", ");
        out.append("SYSUSERID=" + this.sysuserId + ", ");
        out.append("LOGONNAME=" + this.logonName + ", ");
        out.append("PASSWORD=" + this.password + ", ");
        out.append("DEVICEID=" + this.deviceId + ", ");
        out.append("PORTALID=" + this.portalId + ", ");
        out.append("EXTERNALAPPID=" + this.externalAppId + ", ");
        out.append("AUTHTOKENID=" + this.authTokenId + ", ");
        out.append("GUIMODE=" + this.guiMode + ", ");
        out.append("TOOL=" + this.tool + ", ");
        out.append("DBMS=" + this.dbms + ", ");
        out.append("USERTYPE=" + this.userType + ", ");
        out.append("DBSERVER=" + this.dbServer + ", ");
        out.append("SQLDATABASE=" + this.sqlDatabase + ", ");
        out.append("ROLELIST=" + this.roleList + ", ");
        out.append("MODULELIST=" + this.moduleList + ", ");
        out.append("LANGUAGE=" + this.language + ", ");
        out.append("LOCALE=" + this.locale + ", ");
        out.append("TIMEZONE=" + this.timezone + ", ");
        return out.toString();
    }

    public HashMap getUserAttributeMap() {
        HashMap<String, Object> user = new HashMap<String, Object>();
        user.put("databaseid", this.databaseId);
        user.put("sysuserid", this.sysuserId);
        user.put("sysusername", this.sysuserName);
        user.put("logonname", this.logonName);
        user.put("deviceid", this.deviceId);
        user.put("portalid", this.portalId);
        user.put("externalappid", this.externalAppId);
        user.put("authtokenid", this.authTokenId);
        user.put("guimode", this.guiMode);
        user.put("tool", this.tool);
        user.put("dbms", this.dbms);
        user.put("usertype", this.userType);
        user.put("rolelist", this.convertToList(this.roleList));
        user.put("modulelist", this.convertToList(this.moduleList));
        user.put("language", this.language);
        user.put("locale", this.locale);
        user.put("timezone", this.timezone);
        user.put("defaultdepartment", this.defaultDepartment);
        user.put("departmentlist", this.convertToList(this.departmentList));
        user.put("lastlogonjobtype", this.currentJobtype);
        user.put("jobtypelist", this.convertToList(this.jobtypeList));
        return user;
    }

    private List convertToList(String delimitedString) {
        ArrayList<String> list = new ArrayList<String>();
        if (delimitedString != null && delimitedString.length() > 0) {
            String[] arrs = StringUtil.split(delimitedString, ";");
            for (int i = 0; i < arrs.length; ++i) {
                list.add(arrs[i]);
            }
        }
        return list;
    }

    private String convertToDelimeteredString(List list) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < list.size(); ++i) {
            sb.append(";").append((String)list.get(i));
        }
        return sb.length() > 0 ? sb.substring(1) : "";
    }
}

