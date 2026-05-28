/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package sapphire.accessor;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.BaseAccessor;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.platform.SapphireDatabase;
import com.labvantage.sapphire.services.SapphireConnection;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.util.ConnectionInfo;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ConnectionProcessor
extends BaseAccessor {
    public ConnectionProcessor() {
    }

    public ConnectionProcessor(String connectionid) {
        super(connectionid);
    }

    public ConnectionProcessor(String nameserverlist, String connectionid) {
        super(connectionid);
    }

    public ConnectionProcessor(File rakFile) throws SapphireException {
        this.setRakFile(rakFile);
    }

    public ConnectionProcessor(File rakFile, String connectionid) {
        super(rakFile, connectionid);
    }

    public ConnectionProcessor(PageContext pageContext) {
        super(pageContext);
    }

    public int changePassword(String nameserverlist, String databaseid, String userid, String oldpassword, String newpassword) {
        try {
            if (local) {
                this.changePassword(databaseid, userid, oldpassword, newpassword);
                return 1;
            }
            this.createRemoteAccessKey(databaseid);
            this.changePassword(userid, oldpassword, newpassword);
            return 1;
        }
        catch (Exception e) {
            this.setError(this.parseServiceExceptionMsg(e, "Failed to change password for user " + userid + ". Exception: " + e.getMessage()), e);
            return 2;
        }
    }

    public void changePassword(String databaseid, String userid, String oldpassword, String newpassword) throws SapphireException {
        try {
            this.getLocalAccessManager().changePassword(databaseid, userid, oldpassword, newpassword);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to change password for user " + userid + ".", e);
        }
    }

    public void changePassword(String userid, String oldpassword, String newpassword) throws SapphireException {
        try {
            if (this.remoteAccessKey == null) {
                throw new SapphireException("You have not provided a Remote Access Key");
            }
            this.getRemoteAccessManager().changePassword(this.remoteAccessKey.getProperty("sapphire.databaseid"), userid, oldpassword, newpassword);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to change password for user " + userid + ".", e);
        }
    }

    public int isValidPassword(String nameserverlist, String databaseid, String userid, String password) {
        try {
            if (local) {
                return this.isValidPassword(databaseid, userid, password) ? 1 : 2;
            }
            this.createRemoteAccessKey(databaseid);
            return this.isValidPassword(userid, password) ? 1 : 2;
        }
        catch (Exception e) {
            this.setError(this.parseServiceExceptionMsg(e, "Failed to check password for user " + userid + ". Exception: " + e.getMessage()), e);
            return 2;
        }
    }

    public boolean isValidPassword(String databaseid, String userid, String password) throws SapphireException {
        try {
            return this.getLocalAccessManager().isValidPassword(databaseid, userid, password);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to check password for user " + userid + ". Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionid())), e);
        }
    }

    public boolean isValidPassword(String userid, String password) throws SapphireException {
        try {
            if (this.remoteAccessKey == null) {
                throw new SapphireException("You have not provided a Remote Access Key");
            }
            return this.getRemoteAccessManager().isValidPassword(this.remoteAccessKey.getProperty("sapphire.databaseid"), userid, password);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to check password for user " + userid + ". Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionid())), e);
        }
    }

    private String getConnectionid(SapphireConnection sapphireConnection) {
        this.resetConnectionid();
        try {
            if (local) {
                String connectionid = this.getLocalAccessManager().getConnectionId(sapphireConnection);
                this.setConnectionid(connectionid);
            } else {
                this.createRemoteAccessKey(sapphireConnection.getDatabaseId());
                String connectionid = this.getRemoteAccessManager().getConnectionId(sapphireConnection);
                this.setConnectionid(connectionid);
            }
        }
        catch (Exception e) {
            this.setError(this.parseServiceExceptionMsg(e, "Failed to get connection id. Exception: " + e.getMessage()), e);
            this.setConnectionid("");
        }
        return this.getConnectionid();
    }

    public String getConnectionid(String userid, String password) throws SapphireException {
        try {
            if (this.remoteAccessKey == null) {
                throw new SapphireException("You have not provided a Remote Access Key");
            }
            return this.getConnectionid(this.remoteAccessKey.getProperty("sapphire.databaseid"), userid, password);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to get connection id for user '" + userid + ".", e);
        }
    }

    public String getConnectionid(String nameserverlist, String databaseid, String userid, String password) {
        return this.getConnectionid(databaseid, userid, password);
    }

    public String getConnectionid(File rakFile, String userid, String password) throws SapphireException {
        super.setRakFile(rakFile);
        return this.getConnectionid(userid, password);
    }

    public String getConnectionid(String nameserverlist, String databaseid, String userid, String password, HashMap options) {
        return this.getConnectionid(databaseid, userid, password, options);
    }

    public String getConnectionid(String databaseid, String userid, String password) {
        SapphireConnection sapphireConnection = new SapphireConnection();
        sapphireConnection.setDatabaseId(databaseid);
        sapphireConnection.setSysuserId(userid);
        sapphireConnection.setPassword(password);
        sapphireConnection.setDeviceId("");
        sapphireConnection.setTool("ConnectionProcessor");
        sapphireConnection.setIgnoreExpiryWarning(true);
        try {
            if (!local) {
                InetAddress clientAddress = InetAddress.getLocalHost();
                sapphireConnection.setRemoteHost(clientAddress.getHostName());
                sapphireConnection.setRemoteIP(clientAddress.getHostAddress());
                sapphireConnection.setOperatingSystem(System.getProperty("os.name"));
                sapphireConnection.setConnectionTypeFlag("R");
            }
        }
        catch (UnknownHostException e) {
            Trace.logWarn("Failed to update sapphireConnection with client address." + e.getMessage());
        }
        return this.getConnectionid(sapphireConnection);
    }

    public String getConnectionid(String databaseid, String userid, String password, HashMap options) {
        SapphireConnection sapphireConnection = new SapphireConnection();
        sapphireConnection.setDatabaseId(databaseid);
        sapphireConnection.setSysuserId(userid);
        sapphireConnection.setPassword(password);
        sapphireConnection.setDeviceId((String)options.get("deviceid"));
        sapphireConnection.setPortalId(options.containsKey("portalid") ? (String)options.get("portalid") : "");
        sapphireConnection.setAuthTokenId((String)options.get("authtokenid"));
        sapphireConnection.setExternalAppId((String)options.get("externalappid"));
        sapphireConnection.setAuthTokenId((String)options.get("authtokenid"));
        sapphireConnection.setGuimode((String)options.get("guimode"));
        sapphireConnection.setTool((String)options.get("tool"));
        sapphireConnection.setSSOAttributes(options.containsKey("ssoattributes") ? (PropertyList)options.get("ssoattributes") : null);
        if (options.get("dbms") != null) {
            sapphireConnection.setDbms((String)options.get("dbms"));
        }
        sapphireConnection.setCurrentJobtype((String)options.get("lastlogonjobtype"));
        sapphireConnection.setIgnoreExpiryWarning(options.get("ignoreexpirywarning") == null || "true".equalsIgnoreCase((String)options.get("ignoreexpirywarning")));
        sapphireConnection.setCurrentJobtype((String)options.get("jobtype"));
        sapphireConnection.setRemoteIP((String)options.get("remoteip"));
        sapphireConnection.setRemoteHost((String)options.get("remotehost"));
        sapphireConnection.setUserAgent((String)options.get("useragent"));
        sapphireConnection.setBrowser((String)options.get("browser"));
        sapphireConnection.setOperatingSystem((String)options.get("operatingsystem"));
        sapphireConnection.setConnectionTypeFlag((String)options.get("connectiontypeflag"));
        sapphireConnection.setClearOldConnectionOption((String)options.get("clearoldconnectionoption"));
        if (options.containsKey("clienttimezone")) {
            try {
                ZoneId z = I18nUtil.getZoneIdFromString(options.get("clienttimezone").toString());
                sapphireConnection.setTimezone(z.getId());
            }
            catch (Exception e) {
                Trace.logWarn("Invalid timezone provided");
            }
        }
        if (options.containsKey("clientlocale")) {
            String locale = (String)options.get("clientlocale");
            try {
                Locale loc = Locale.forLanguageTag(locale);
                if (loc.getDisplayName().length() > 0) {
                    locale = StringUtil.replaceAll(loc.toLanguageTag(), "-", "_");
                    sapphireConnection.setLocale(locale);
                } else {
                    Trace.logWarn("Invalid locale string provided");
                }
            }
            catch (Exception e) {
                Trace.logWarn("Invalid locale provided");
            }
        }
        return this.getConnectionid(sapphireConnection);
    }

    public void clearConnection(String connectionid) {
        try {
            if (local) {
                this.getLocalAccessManager().clearConnection(this.getConnectionid(), connectionid);
            } else {
                this.getRemoteAccessManager().clearConnection(this.getConnectionid(), connectionid);
            }
        }
        catch (Exception e) {
            this.setError(this.parseServiceExceptionMsg(e, "Failed to clear connection. Exception: " + e.getMessage()), e);
        }
    }

    public void prepareToDeleteConnection(String connectionid) {
        try {
            if (local) {
                this.getLocalAccessManager().prepareToDeleteConnection(this.getConnectionid(), connectionid);
            } else {
                this.getRemoteAccessManager().prepareToDeleteConnection(this.getConnectionid(), connectionid);
            }
        }
        catch (Exception e) {
            this.setError(this.parseServiceExceptionMsg(e, "Failed to clear connection. Exception: " + e.getMessage()), e);
        }
    }

    public String[] getDatabaseList() {
        String[] databases;
        try {
            List sapphireDatabases = local ? this.getLocalAccessManager().getSapphireDatabases() : this.getRemoteAccessManager().getSapphireDatabases();
            databases = new String[sapphireDatabases.size()];
            for (int i = 0; i < sapphireDatabases.size(); ++i) {
                databases[i] = ((SapphireDatabase)sapphireDatabases.get(i)).getDatabaseId();
            }
        }
        catch (Exception e) {
            this.setError("Failed to get database list. Exception: " + e.getMessage(), e);
            databases = new String[]{};
        }
        return databases;
    }

    public boolean checkUser(String sysuserid, String password) {
        try {
            return local ? this.getLocalAccessManager().checkUser(this.getConnectionid(), sysuserid, password) : this.getRemoteAccessManager().checkUser(this.getConnectionid(), sysuserid, password);
        }
        catch (Exception e) {
            this.setError(this.parseServiceExceptionMsg(e, "Unable to check user. Exception: " + e.getMessage()), e);
            return false;
        }
    }

    public boolean checkConnection(String connectionid) {
        try {
            return local ? this.getLocalAccessManager().checkConnection(connectionid, false) : this.getRemoteAccessManager().checkConnection(connectionid, false);
        }
        catch (Exception e) {
            this.setError(this.parseServiceExceptionMsg(e, "Unable to check connection. Exception: " + e.getMessage()), e);
            return false;
        }
    }

    public int clearRSets(String rsetlist) {
        try {
            if (local) {
                this.getLocalAccessManager().clearRSets(this.getConnectionid(), rsetlist);
            } else {
                this.getRemoteAccessManager().clearRSets(this.getConnectionid(), rsetlist);
            }
            return 1;
        }
        catch (Exception e) {
            this.setError(this.parseServiceExceptionMsg(e, "Failed to clear rsetlist. Exception: " + e.getMessage()), e);
            return 2;
        }
    }

    public String getLicenseProperty(String propertyid) {
        try {
            return this.getConfigurationManager().getLicenseProperty(this.getConnectionid(), propertyid);
        }
        catch (Exception e) {
            this.setError(this.parseServiceExceptionMsg(e, "Failed to get license property. Exception: " + e.getMessage()), e);
            return "";
        }
    }

    public void disableUser(String sysuserid, String reason) {
        try {
            if (local) {
                this.getLocalAccessManager().disableUser(this.getConnectionid(), sysuserid, reason);
            } else {
                this.getRemoteAccessManager().disableUser(this.getConnectionid(), sysuserid, reason);
            }
        }
        catch (Exception e) {
            this.setError(this.parseServiceExceptionMsg(e, "Failed to disable user. Exception: " + e.getMessage()), e);
        }
    }

    public void enableUser(String sysuserid) {
        try {
            if (local) {
                this.getLocalAccessManager().enableUser(this.getConnectionid(), sysuserid);
            } else {
                this.getRemoteAccessManager().enableUser(this.getConnectionid(), sysuserid);
            }
        }
        catch (Exception e) {
            this.setError(this.parseServiceExceptionMsg(e, "Failed to enable user. Exception: " + e.getMessage()), e);
        }
    }

    public void forceChangePassword(String sysuserid) {
        try {
            if (local) {
                this.getLocalAccessManager().forcePasswordChange(this.getConnectionid(), sysuserid);
            } else {
                this.getRemoteAccessManager().forcePasswordChange(this.getConnectionid(), sysuserid);
            }
        }
        catch (Exception e) {
            this.setError(this.parseServiceExceptionMsg(e, "Failed to set the flag to force a user's password change. Exception: " + e.getMessage()), e);
        }
    }

    public String getProfileProperty(String propertyid) {
        try {
            String value = this.getConfigurationManager().getProfileProperty(this.getConnectionid(), this.getSapphireConnection().getSysuserId(), propertyid, "");
            if (value == null) {
                value = "";
            }
            return value;
        }
        catch (Exception donothing) {
            return "";
        }
    }

    public boolean isOra() {
        return this.getSapphireConnection().isOracle();
    }

    public boolean isMSS() {
        return this.getSapphireConnection().isSqlServer();
    }

    public List getRoleList() {
        String rolelist = this.getSapphireConnection().getRoleList();
        String[] roles = StringUtil.split(rolelist, ";");
        return Arrays.asList(roles);
    }

    public List getModuleList() {
        String modulelist = this.getSapphireConnection().getModuleList();
        String[] modules = StringUtil.split(modulelist, ";");
        return Arrays.asList(modules);
    }

    public String getLanguage() {
        return this.getSapphireConnection().getLanguage();
    }

    public SapphireConnection getSapphireConnection() {
        try {
            SapphireConnection sapphireConnection = local ? this.getLocalAccessManager().getSapphireConnection(this.getConnectionid()) : this.getRemoteAccessManager().getSapphireConnection(this.getConnectionid());
            return sapphireConnection;
        }
        catch (Exception e) {
            this.setError(this.parseServiceExceptionMsg(e, "Unable to get connection details - " + e.getMessage()), e);
            return null;
        }
    }

    public ConnectionInfo getConnectionInfo(String connectionid) {
        SapphireConnection sapphireConnection = this.getSapphireConnection();
        return sapphireConnection != null ? new ConnectionInfo(sapphireConnection) : null;
    }

    public String getConfigProperty(String propertyid) {
        try {
            return this.getConfigurationManager().getConfigProperty(this.getConnectionid(), propertyid, "");
        }
        catch (Exception e) {
            this.setError(e.getMessage(), e);
            return "";
        }
    }

    public String getConfigProperty(String propertyid, String defaultValue) {
        try {
            return this.getConfigurationManager().getConfigProperty(this.getConnectionid(), propertyid, defaultValue);
        }
        catch (Exception e) {
            this.setError(e.getMessage(), e);
            return "";
        }
    }

    public String getSysConfigProperty(String propertyid) {
        try {
            return this.getConfigurationManager().getSysConfigProperty(this.getConnectionid(), propertyid, "");
        }
        catch (Exception e) {
            this.setError(e.getMessage(), e);
            return "";
        }
    }

    public String getSysConfigProperty(String propertyid, String defaultValue) {
        try {
            return this.getConfigurationManager().getSysConfigProperty(this.getConnectionid(), propertyid, defaultValue);
        }
        catch (Exception e) {
            this.setError(e.getMessage(), e);
            return "";
        }
    }
}

