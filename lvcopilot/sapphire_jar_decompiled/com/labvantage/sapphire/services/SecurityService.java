/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.services;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.Build;
import com.labvantage.sapphire.Cache;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.License;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.mail.SendMail;
import com.labvantage.sapphire.actions.misc.AddActivityTraceLog;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.admin.system.ThreadUtil;
import com.labvantage.sapphire.ejb.TDQManagerLocal;
import com.labvantage.sapphire.modules.eventmanager.EventManager;
import com.labvantage.sapphire.modules.eventmanager.NotifyManager;
import com.labvantage.sapphire.modules.eventmanager.eventobject.UserAuthenticationEventObject;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.platform.SapphireDatabase;
import com.labvantage.sapphire.services.ActionService;
import com.labvantage.sapphire.services.AutomationService;
import com.labvantage.sapphire.services.BaseService;
import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.services.I18NService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.SapphireService;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.services.WebAdminService;
import com.labvantage.sapphire.servlet.command.Login;
import com.labvantage.sapphire.servlet.externalapp.ExternalAuthenticationUtil;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.jndi.ServiceLocator;
import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import com.labvantage.sapphire.xml.PropertyTree;
import com.labvantage.sapphire.xml.PropertyTreeUtil;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.CallableStatement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAuthentication;
import sapphire.action.BasePasswordValidator;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SecurityService
extends BaseService {
    public static final String LOGNAME = "SecurityService";
    public static final String CACHE_SAPPHIRE_CONNECTION = "SapphireConnection";
    public static final String TOO_MANY_INVALID_LOGINS = "Too many invalid logins, try again later";
    public static final String INVALID_USERNAME_OR_PASSWORD = "Invalid username or password specified";
    private static String VIRTUALUSER_MARKER = "@";
    private static String PORTALUSER_MARKER = "~";
    public static String ENCRYPTEDCONNECTIONID_MARKER = "#";
    private static Cache sapphireConnectionCache = new Cache("SapphireConnection");
    private static Cache hasExtAuthentication = new Cache("hasExtAuthentication");
    private static Cache alwaysExtAuthentication = new Cache("alwaysExtAuthentication");
    private static String systemPassword;
    private static long lastUpdateMillis;
    private static boolean updatePending;
    private static ExecutorService executor;

    public SecurityService(SapphireConnection sapphireConnection) throws ServiceException {
        super(sapphireConnection);
        this.logName = LOGNAME;
        if (executor == null) {
            ThreadUtil.LVThreadFactory threadFactory = ThreadUtil.getThreadFactory(sapphireConnection.getDatabaseId(), "ss-timeout");
            executor = Executors.newSingleThreadExecutor(threadFactory);
        }
    }

    public static String decryptConnectionId(String connectionId) {
        if (connectionId.startsWith(ENCRYPTEDCONNECTIONID_MARKER)) {
            return EncryptDecrypt.decrypt(connectionId.substring(1));
        }
        return connectionId;
    }

    public static void generateSystemPassword() {
        Trace.logInfo("STARTUP", "Generating system password");
        Random random = new Random(System.currentTimeMillis());
        systemPassword = String.valueOf(random.nextLong());
        SapphireService.setSystemPassword(systemPassword);
        AutomationService.setSystemPassword(systemPassword);
        ServiceLocator.setSystemPassword(systemPassword);
        Login.setSystemPassword(systemPassword);
        ExternalAuthenticationUtil.setSystemPassword(systemPassword);
        NotifyManager.setSystemPassword(systemPassword);
    }

    public static String getConnectionId(SapphireConnection sapphireConnection) throws ServiceException {
        Trace.logInfo(LOGNAME, "Getting connectionid for user '" + (sapphireConnection.getSysuserId() != null ? sapphireConnection.getSysuserId() : "") + "'");
        String connectionid = null;
        if (Configuration.getState() != 0) {
            throw new ServiceException("SERVER_STARTUP_FAILURE", "Invalid configuration or server not ready. Last error reported was: " + Configuration.getErrorMsg());
        }
        DBUtil db = new DBUtil();
        try {
            Configuration configuration = Configuration.getInstance();
            String databaseid = sapphireConnection.getDatabaseId();
            if (databaseid == null || databaseid.length() == 0) {
                throw new ServiceException("INVALID_PARAMETER", "Databaseid not specified whilst trying to get a connection id");
            }
            String sysuserid = sapphireConnection.getSysuserId();
            if (sysuserid == null || sysuserid.length() == 0) {
                throw new ServiceException("INVALID_PARAMETER", "Username name not specified whilst trying to get a connection id");
            }
            String password = sapphireConnection.getPassword();
            String unicodepassword = "";
            if (password != null && password.indexOf("{|}") == 0) {
                if ((password = EncryptDecrypt.decryptRSA(password.substring("{|}".length()))).indexOf("{|}") > 0) {
                    String[] pwds = StringUtil.split(password, "{|}");
                    password = pwds[1];
                    try {
                        unicodepassword = URLDecoder.decode(pwds[0], "UTF-8");
                    }
                    catch (UnsupportedEncodingException unsupportedEncodingException) {
                        // empty catch block
                    }
                }
                sapphireConnection.setPassword(password);
            }
            if ("".equals(unicodepassword)) {
                unicodepassword = password;
            }
            if (password == null || password.length() == 0) {
                throw new ServiceException("INVALID_PARAMETER", "Password not specified whilst trying to get a connection id");
            }
            String tool = sapphireConnection.getTool();
            if (SecurityService.isSaaSSupportUser(databaseid, sysuserid)) {
                sapphireConnection.setTool("Internal_SaaS");
            }
            if (!configuration.isDatabaseActive(databaseid)) {
                throw new ServiceException("DATABASE_INACTIVE", "The database " + databaseid + " is not active - no connections can be made");
            }
            if (configuration.getLicense(sapphireConnection.getDatabaseId()) == null) {
                throw new ServiceException("GENERAL_ERROR", "License key not defined in Configuration");
            }
            if (configuration.getLicense(sapphireConnection.getDatabaseId()).isLicenseExpired()) {
                throw new ServiceException("EXPIRED_LICENSEKEY", "License key has expired - contact Labvantage for a new license file");
            }
            if (!configuration.isWebServer() && ("RequestController".equals(tool) || "SapphireAdmin".equals(tool))) {
                throw new ServiceException("INVALID_SERVER", "The server accessed (" + configuration.getServerHostName() + ") does not provide web connections");
            }
            db.setConnection(sapphireConnection);
            DataSet sysuser = SecurityService.getUserDataSet(sapphireConnection);
            String usertype = "";
            if (SecurityService.isSystemUser(databaseid, sysuserid)) {
                if (SecurityService.validSystemUser(sapphireConnection, databaseid, sysuserid, password)) {
                    sysuserid = "(system)";
                    sapphireConnection.setSysuserId(sysuserid);
                    usertype = "Y";
                }
            } else {
                PropertyList ssoUserAttributes = sapphireConnection.getSSOAttributes();
                if (ssoUserAttributes != null) {
                    SecurityService.externalSSOAuthenticate(sapphireConnection, sysuser);
                    sysuser = SecurityService.getUserDataSet(sapphireConnection);
                } else if (hasExtAuthentication.get(databaseid) == null || "Y".equals(hasExtAuthentication.get(databaseid))) {
                    try {
                        if (!systemPassword.equals(sapphireConnection.getPassword())) {
                            SecurityService.externalAuthenticate(sapphireConnection, sysuser, unicodepassword);
                            sysuser = SecurityService.getUserDataSet(sapphireConnection);
                        }
                    }
                    catch (ServiceException externalAuthFailException) {
                        boolean logFailedAttempt;
                        String message = externalAuthFailException.getMessage();
                        boolean bl = logFailedAttempt = message.indexOf("Authentication failed. Incorrect username or password for username") >= 0;
                        if (message.indexOf("|%|") > 0) {
                            message = message.substring(message.indexOf("|%|") + 3);
                        }
                        if (logFailedAttempt) {
                            sapphireConnection.setPassword("failedExternalAuthentication_" + sapphireConnection.getPassword());
                            try {
                                SecurityService.authenticateUser(sapphireConnection, sysuser, true);
                            }
                            catch (Exception e) {
                                throw new ServiceException(message);
                            }
                        }
                        throw new ServiceException(message);
                    }
                }
                SecurityService.authenticateUser(sapphireConnection, sysuser, true);
                usertype = sysuser.getString(0, "nameduserflag");
                sysuserid = sysuser.getString(0, "sysuserid");
                sapphireConnection.setSysuserId(sysuserid);
            }
            if ("A".equals(usertype) && ("RequestController".equals(tool) || "SapphireAdmin".equals(tool))) {
                throw new ServiceException("EXTERNALUSER_IN_BROWSER", "External users cannot connect with a browser.");
            }
            if (("P".equals(usertype) || "Q".equals(usertype)) && ("RequestController".equals(tool) || "SapphireAdmin".equals(tool))) {
                throw new ServiceException("PORTALUSER_IN_LIMS", "Portal users cannot connect to LIMS.");
            }
            if (!"P".equals(usertype) && !"Q".equals(usertype) && "Stellar".equals(tool)) {
                throw new ServiceException("NONPORTALUSER_IN_PORTAL", "Non-Portal users cannot connect to Portal.");
            }
            connectionid = SecurityService.getNextConnectionId(db, databaseid, sysuserid, usertype);
            boolean isVirtualUser = usertype.equals("V") || usertype.equals("I");
            boolean isPortalUser = usertype.equals("P") || usertype.equals("Q");
            String connectFrom = SecurityPolicyUtil.getSessionBinding(databaseid, isVirtualUser, isPortalUser, sapphireConnection.getRemoteIP(), sapphireConnection.getRemoteHost());
            String timezone = sysuser.getString(0, "timezone", "");
            if (sapphireConnection.getTimeZone() != null && sapphireConnection.getTimeZone().length() > 0 && !sapphireConnection.getTimeZone().equalsIgnoreCase(timezone)) {
                timezone = sapphireConnection.getTimeZone() != null && sapphireConnection.getTimeZone().length() > 0 ? sapphireConnection.getTimeZone() : timezone;
                try {
                    ZoneId z = I18nUtil.getZoneIdFromString(timezone);
                    try {
                        db.executePreparedUpdate("UPDATE sysuser SET timezone=? WHERE sysuserid=?", new Object[]{timezone, sysuserid});
                    }
                    catch (Exception e) {
                        Trace.logWarn("Unable to update timezone from client.");
                    }
                }
                catch (Exception e) {
                    Trace.logWarn("Invalid timezone provided");
                }
            }
            String locale = sysuser.getString(0, "localeid", "");
            if (sapphireConnection.getLocale() != null && sapphireConnection.getLocale().length() > 0 && !sapphireConnection.getLocale().equalsIgnoreCase(locale)) {
                locale = sapphireConnection.getLocale() != null && sapphireConnection.getLocale().length() > 0 ? sapphireConnection.getLocale() : locale;
                try {
                    if (db.getPreparedCount("SELECT count(localeid) FROM locale WHERE localeid=?", new Object[]{locale}) > 0) {
                        db.executePreparedUpdate("UPDATE sysuser SET localeid=? WHERE sysuserid=?", new Object[]{locale, sysuserid});
                    } else {
                        Trace.logWarn("Invalid locale from client.");
                    }
                }
                catch (Exception e) {
                    Trace.logWarn("Unable to update locale from client.");
                }
            }
            int timezoneoffset = 0;
            if (timezone != null && timezone.length() > 0) {
                timezoneoffset = I18nUtil.getSysTimeZoneOffSet(TimeZone.getTimeZone(timezone));
            }
            String passedinjobtype = sapphireConnection.getCurrentJobtype();
            String currentjobtype = "";
            if (!("(null)".equals(passedinjobtype) || "SapphireWS".equals(sapphireConnection.getTool()) || SecurityService.isSystemUser(databaseid, sysuserid))) {
                currentjobtype = SecurityService.synchronizeJobType(db, sysuserid, sapphireConnection, passedinjobtype, true, sapphireConnection);
            }
            if (Trace.stats) {
                Trace.setStartCodeBlock("New Connection");
            }
            Timestamp now = DateTimeUtil.getNowTimestamp();
            String authTokenId = sapphireConnection.getAuthTokenId();
            db.executePreparedUpdate("INSERT INTO connection ( connectionid, sysuserid, usertypeflag, deviceid, authtokenid, tool, dbms, connectfrom, connectdt, lastaccesseddt, timezoneoffset, currentjobtypeid, portalid ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )", new Object[]{connectionid, sapphireConnection.getSysuserId(), usertype, sapphireConnection.getDeviceId(), authTokenId, sapphireConnection.getTool(), db.getDbms(), connectFrom, now, now, timezoneoffset, currentjobtype, sapphireConnection.getPortalId()});
            if (Trace.stats) {
                Trace.setEndCodeBlock("New Connection");
            }
            if (!systemPassword.equals(sapphireConnection.getPassword()) || "Stellar".equals(tool) || "RequestController".equals(tool) || authTokenId != null && authTokenId.length() > 0) {
                String concurrentUserSQL = "SELECT count(*) FROM connection WHERE usertypeflag = ? AND deletedt IS NULL AND tool NOT IN (?,?)";
                int concurrentUserCount = db.getPreparedCount(concurrentUserSQL, new Object[]{"C", "TDLProcessing", "Internal_SaaS"});
                String concurrentVirtualUserSQL = "SELECT count(*) FROM connection WHERE usertypeflag = ? AND deletedt IS NULL AND tool NOT IN (?,?)";
                int concurrentVirtualUserCount = db.getPreparedCount(concurrentVirtualUserSQL, new Object[]{"I", "TDLProcessing", "Internal_SaaS"});
                String concurrentPortalUserSQL = "SELECT count(*) FROM connection WHERE usertypeflag = ? AND deletedt IS NULL AND tool NOT IN (?,?)";
                int concurrentPortalUserCount = db.getPreparedCount(concurrentPortalUserSQL, new Object[]{"Q", "TDLProcessing", "Internal_SaaS"});
                String totalUserSql = "SELECT count(*) FROM connection WHERE deletedt IS NULL AND tool NOT IN (?,?,?) AND (authtokenid is null OR authtokenid='')";
                int totalUserCount = db.getPreparedCount(totalUserSql, new Object[]{"TDLProcessing", "Internal", "Internal_SaaS"});
                String sql = "INSERT INTO connectionlog ( connectionid, sysuserid, connectioncreateddt, jobtypeid, serveripaddress, clientipaddress, clienthostname, deviceid, authtokenid, guimode, serverhostname, serverid, serverbuild, clientbrowser, clientuseragent, clientos, connectiontypeflag, concurrentusercount, concurrentvirtualusercount, concurrentportalusercount, totalusercount, portalid ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
                db.executePreparedUpdate(sql, new Object[]{connectionid, sapphireConnection.getSysuserId(), DateTimeUtil.getNowTimestamp(), currentjobtype, connectFrom, sapphireConnection.getRemoteIP(), sapphireConnection.getRemoteHost(), sapphireConnection.getDeviceId(), authTokenId, sapphireConnection.getGuiMode(), configuration.getServerHostName(), configuration.getServerid(), Build.getBuild(), sapphireConnection.getBrowser(), sapphireConnection.getUserAgent(), sapphireConnection.getOperatingSystem(), sapphireConnection.getConnectionTypeFlag(), concurrentUserCount, concurrentVirtualUserCount, concurrentPortalUserCount, totalUserCount, sapphireConnection.getPortalId()});
            }
            Trace.logInfo(LOGNAME, "New Connection: User '" + sysuserid + "' connected to database '" + databaseid + "' with new connectionid [" + connectionid + "]");
        }
        catch (SapphireException se) {
            throw new ServiceException(se.getErrorid(), "Failed to get a connectionid", se);
        }
        finally {
            db.reset();
        }
        return connectionid;
    }

    private static boolean isSaaSSupportUser(String databaseId, String sysUserId) {
        boolean retVal = false;
        if ("(system)".equals(sysUserId)) {
            return false;
        }
        Trace.logDebug("Determine SaaS support user. DB: " + databaseId + ". User: " + sysUserId);
        try {
            Configuration configuration = Configuration.getInstance();
            License license = configuration.getLicense(databaseId);
            if (license.isSaaS()) {
                Path saasSupportUserListPath = Paths.get(configuration.getApplicationHome(), "saassupportusers_" + databaseId + ".txt");
                Trace.logDebug("Checking file: " + saasSupportUserListPath);
                if (!Files.exists(saasSupportUserListPath, new LinkOption[0])) {
                    saasSupportUserListPath = Paths.get(configuration.getApplicationHome(), "saassupportusers.txt");
                    Trace.logDebug("Checking file: " + saasSupportUserListPath);
                }
                if (Files.exists(saasSupportUserListPath, new LinkOption[0])) {
                    List<String> saasNames = Files.readAllLines(saasSupportUserListPath, StandardCharsets.UTF_8);
                    retVal = saasNames.contains(sysUserId);
                } else {
                    retVal = false;
                }
            }
        }
        catch (Throwable e) {
            Trace.logError("Exception occurred when trying to determine SaaS Support User. DB: " + databaseId + ". User: " + sysUserId, e);
        }
        Trace.logInfo("SaaS support user: " + retVal);
        return retVal;
    }

    public static boolean checkConnection(DBUtil database, String connectionid) throws ServiceException {
        Trace.logDebug(LOGNAME, "Checking connection '" + connectionid + "'");
        try {
            database.createPreparedResultSet("checkConnection", "SELECT connectionid FROM connection WHERE connectionid = ? AND " + (database.isOracle() ? "lastaccesseddt <> Nvl( deletedt, Sysdate - 100 )" : "lastaccesseddt <> ISNULL( deletedt, GETDATE() - 100 )"), connectionid);
            if (database.getNext("checkConnection")) {
                boolean bl = true;
                return bl;
            }
            if (sapphireConnectionCache.get(connectionid) != null) {
                sapphireConnectionCache.remove(connectionid);
            }
            boolean bl = false;
            return bl;
        }
        catch (SapphireException e) {
            throw new ServiceException("DB_ACTION_FAILED", "Failed to check connection: " + connectionid, e);
        }
        finally {
            database.closeResultSet("checkConnection");
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static String synchronizeJobType(DBUtil database, String sysuserid, SapphireConnection userConnection, String jobtypeid, boolean isInitialLogon, SapphireConnection sapphireConnection) throws ServiceException {
        String string;
        Trace.logInfo(LOGNAME, "Synchronize jobtype to " + jobtypeid + " for '" + sysuserid + "'");
        try {
            String currentJobType;
            block47: {
                String basedepartment;
                String defaultdepartment;
                String defaultjobtype;
                String lastjobtype;
                block46: {
                    currentJobType = "";
                    String securitytypeflag = "";
                    lastjobtype = "";
                    defaultjobtype = "";
                    defaultdepartment = "";
                    basedepartment = "";
                    database.createPreparedResultSet("SELECT securitytypeflag, lastjobtype, defaultjobtype, defaultdepartment, basedepartment FROM sysuser WHERE sysuserid = ?", sysuserid);
                    if (database.getNext()) {
                        securitytypeflag = database.getString("securitytypeflag");
                        lastjobtype = database.getString("lastjobtype");
                        defaultjobtype = database.getString("defaultjobtype");
                        defaultdepartment = database.getString("defaultdepartment");
                        basedepartment = database.getString("basedepartment");
                    }
                    try {
                        String callstmt = "{call lv_app" + (database.isOracle() ? "." : "_") + "setappinfouser( ? ) }";
                        CallableStatement cs = database.prepareCall(callstmt);
                        cs.setString(1, sysuserid);
                        cs.executeUpdate();
                    }
                    catch (Exception ignore) {
                        Trace.logInfo("exception: " + ignore.getMessage());
                    }
                    String oldDefaultTestingLab = "";
                    database.createPreparedResultSet("defaulttestinglab", "SELECT departmentid FROM departmentsysuser WHERE sysuserid=? AND defaulttestinglabflag='Y'", new String[]{sysuserid});
                    if (database.getNext("defaulttestinglab")) {
                        oldDefaultTestingLab = database.getString("defaulttestinglab", "departmentid");
                    }
                    database.closeResultSet("defaulttestinglab");
                    database.executePreparedUpdate("DELETE FROM departmentsysuser WHERE sysuserid=? and sourceflag='T'", sysuserid);
                    if ("J".equals(securitytypeflag)) break block46;
                    if (basedepartment != null && basedepartment.length() > 0 && !basedepartment.equals(defaultdepartment)) {
                        database.executePreparedUpdate("UPDATE sysuser SET tracelogid = null, defaultdepartment=? WHERE sysuserid=?", new String[]{basedepartment, sysuserid});
                        defaultdepartment = basedepartment;
                    }
                    if (defaultdepartment != null && defaultdepartment.length() > 0) {
                        database.createPreparedResultSet("SELECT ds.departmentid FROM departmentsysuser ds, department d WHERE ds.departmentid=d.departmentid AND ds.sysuserid = ? AND ds.departmentid=? AND (d.activeflag is null OR d.activeflag!='N')", new String[]{sysuserid, defaultdepartment});
                        if (!database.getNext()) {
                            database.executePreparedUpdate("INSERT INTO departmentsysuser (sysuserid, departmentid, sourceflag, defaulttestinglabflag ) values (?,?,?, ?)", new String[]{sysuserid, defaultdepartment, "T", oldDefaultTestingLab.equals(defaultdepartment) ? "Y" : ""});
                        }
                    }
                    break block47;
                }
                if (jobtypeid == null || jobtypeid.length() == 0) {
                    String jobtypeoption = new ConfigService(userConnection).getProfileProperty(sysuserid, "jobtypeoption");
                    database.createPreparedResultSet("SELECT jobtypeid FROM sysuserjobtype WHERE sysuserid = ? AND jobtypeid = ?", new Object[]{sysuserid, lastjobtype});
                    boolean lastjobvalid = database.getNext();
                    if (jobtypeoption != null && jobtypeoption.length() > 0) {
                        jobtypeid = "defaultjobtype".equals(jobtypeoption) || !lastjobvalid ? defaultjobtype : lastjobtype;
                    } else {
                        String string2 = jobtypeid = lastjobtype == null || lastjobtype.length() == 0 || !lastjobvalid ? defaultjobtype : lastjobtype;
                    }
                }
                if (jobtypeid == null || jobtypeid.length() == 0) {
                    database.createPreparedResultSet("SELECT jobtypeid FROM sysuserjobtype WHERE sysuserid = ? ORDER BY jobtypeid", sysuserid);
                    if (database.getNext()) {
                        jobtypeid = database.getString("jobtypeid");
                    }
                }
                if (jobtypeid == null) throw new ServiceException("User " + sysuserid + " has security type of job type but has not assigned a job type");
                if (jobtypeid.length() == 0) {
                    throw new ServiceException("User " + sysuserid + " has security type of job type but has not assigned a job type");
                }
                database.createPreparedResultSet("SELECT jobtypeid FROM sysuserjobtype WHERE sysuserid = ? AND jobtypeid = ?", new Object[]{sysuserid, jobtypeid});
                if (!database.getNext()) {
                    throw new ServiceException("You are not assigned the job type '" + jobtypeid + "'. Cannot log on as Jobtype '" + jobtypeid + "'.");
                }
                database.createPreparedResultSet("SELECT connectionid, sysuserid, currentjobtypeid FROM connection WHERE sysuserid=? AND currentjobtypeid is not null" + (database.isSqlServer() ? " AND currentjobtypeid != ''" : "") + " AND currentjobtypeid!=? AND deletedt is null", new String[]{sysuserid, jobtypeid});
                String clearOldConnectionWithDiffJobType = sapphireConnection.getClearOldConnectionOption();
                if ("on".equals(clearOldConnectionWithDiffJobType)) {
                    while (database.getNext()) {
                        String oldconnectionid = database.getString("connectionid");
                        new SecurityService(sapphireConnection).prepareToDeleteConnection(oldconnectionid);
                    }
                } else if (database.getNext()) {
                    throw new ServiceException("detailedLoginMessages:" + SecurityService.translateText(sapphireConnection, "Another active connection with a different jobtype found for user") + " '" + sysuserid + "', JobType '" + database.getString("currentjobtypeid") + "'. " + SecurityService.translateText(sapphireConnection, "Cannot log on as Jobtype") + " '" + jobtypeid + "'.|%|" + SecurityService.translateText(sapphireConnection, "Clear all my active connections with different job types"));
                }
                HashSet<String> sysuserRoleSet = new HashSet<String>();
                database.createPreparedResultSet("SELECT roleid FROM sysuserrole WHERE sysuserid = ?", sysuserid);
                while (database.getNext()) {
                    sysuserRoleSet.add(database.getString("roleid"));
                }
                HashSet<String> jobtypeRoleSet = new HashSet<String>();
                database.createPreparedResultSet("SELECT jr.roleid FROM jobtyperole jr, role r WHERE jr.roleid=r.roleid AND (r.activeflag is null OR r.activeflag!='N') AND jr.jobtypeid = ?", jobtypeid);
                while (database.getNext()) {
                    jobtypeRoleSet.add(database.getString("roleid"));
                }
                if (!sysuserRoleSet.equals(jobtypeRoleSet)) {
                    Iterator itr = jobtypeRoleSet.iterator();
                    String insertSQL = "INSERT INTO sysuserrole (sysuserid, roleid ) values (?,?)";
                    while (itr.hasNext()) {
                        String roleid = (String)itr.next();
                        if (sysuserRoleSet.contains(roleid)) continue;
                        database.executePreparedUpdate(insertSQL, sysuserid, roleid);
                    }
                    sysuserRoleSet.removeAll(jobtypeRoleSet);
                    if (sysuserRoleSet.size() > 0) {
                        String deleteSQL = "DELETE FROM sysuserrole WHERE sysuserid=? and roleid=?";
                        for (String removeroleid : sysuserRoleSet) {
                            database.executePreparedUpdate(deleteSQL, sysuserid, removeroleid);
                        }
                    }
                }
                String jobtypedefaultdept = "";
                database.createPreparedResultSet("SELECT defaultdepartment FROM jobtype WHERE jobtypeid=?", jobtypeid);
                if (!database.getNext()) throw new ServiceException("DB_ACTION_FAILED", "Failed to find job type '" + jobtypeid + "'");
                jobtypedefaultdept = database.getString("defaultdepartment");
                if (jobtypedefaultdept != null && jobtypedefaultdept.length() > 0) {
                    if (!jobtypedefaultdept.equals(defaultdepartment)) {
                        database.executePreparedUpdate("UPDATE sysuser SET tracelogid = null, defaultdepartment=? WHERE sysuserid=?", new String[]{jobtypedefaultdept, sysuserid});
                    }
                } else {
                    database.executePreparedUpdate("UPDATE sysuser SET tracelogid = null, defaultdepartment=? WHERE sysuserid=?", new String[]{basedepartment, sysuserid});
                }
                HashSet<String> jobtypeDeptSet = new HashSet<String>();
                database.createPreparedResultSet("SELECT defaultdepartment AS departmentid FROM sysuser WHERE sysuserid=? AND defaultdepartment NOT IN (SELECT departmentid FROM departmentsysuser WHERE sysuserid=? ) UNION SELECT jd.departmentid FROM jobtypedepartment jd, department d WHERE jd.departmentid=d.departmentid AND (d.activeflag is null OR d.activeflag!='N') AND jd.jobtypeid = ? AND jd.departmentid NOT IN (SELECT departmentid FROM departmentsysuser WHERE sysuserid=? )", new String[]{sysuserid, sysuserid, jobtypeid, sysuserid});
                while (database.getNext()) {
                    String deptid = database.getString("departmentid");
                    if (deptid == null || deptid.length() <= 0) continue;
                    jobtypeDeptSet.add(deptid);
                }
                if (jobtypeDeptSet.size() > 0) {
                    Iterator itr = jobtypeDeptSet.iterator();
                    String insertSQL = "INSERT INTO departmentsysuser (sysuserid, departmentid, sourceflag, usersequence ) values (?,?,?,? )";
                    while (itr.hasNext()) {
                        String departmentid = (String)itr.next();
                        database.executePreparedUpdate(insertSQL, new String[]{sysuserid, departmentid, "T", "1000"});
                    }
                }
                HashSet<String> sysuserSDCSecuritySet = new HashSet<String>();
                database.createPreparedResultSet("SELECT sdcid, operationid, accesstype FROM sdcsecurity WHERE sysuserid = ?", sysuserid);
                while (database.getNext()) {
                    sysuserSDCSecuritySet.add(database.getString("sdcid") + ";" + database.getString("operationid") + ";" + database.getString("accesstype"));
                }
                HashSet<String> jobtypeSDCSecuritySet = new HashSet<String>();
                database.createPreparedResultSet("SELECT sdcid, operationid, accesstype FROM sdcjobtypesecurity WHERE jobtypeid = ?", jobtypeid);
                while (database.getNext()) {
                    jobtypeSDCSecuritySet.add(database.getString("sdcid") + ";" + database.getString("operationid") + ";" + database.getString("accesstype"));
                }
                if (!sysuserSDCSecuritySet.equals(jobtypeSDCSecuritySet)) {
                    Iterator itr = jobtypeSDCSecuritySet.iterator();
                    String insertSQL = "INSERT INTO sdcsecurity (sdcid, operationid, accesstype, sysuserid, createtool, createdt ) values ( ?,?,?,?,?,? )";
                    while (itr.hasNext()) {
                        String soa = (String)itr.next();
                        if (sysuserSDCSecuritySet.contains(soa)) continue;
                        Object[] params = new Object[6];
                        String[] soas = StringUtil.split(soa, ";");
                        params[0] = soas[0];
                        params[1] = soas[1];
                        params[2] = soas[2];
                        params[3] = sysuserid;
                        params[4] = "FROM JOBTYPE";
                        params[5] = DateTimeUtil.getNowTimestamp();
                        database.executePreparedUpdate(insertSQL, params);
                    }
                    sysuserSDCSecuritySet.removeAll(jobtypeSDCSecuritySet);
                    if (sysuserSDCSecuritySet.size() > 0) {
                        String deleteSQL = "DELETE FROM sdcsecurity WHERE sdcid=? AND operationid=? AND accesstype=? and sysuserid='" + sysuserid + "'";
                        for (String soa : sysuserSDCSecuritySet) {
                            database.executePreparedUpdate(deleteSQL, StringUtil.split(soa, ";"));
                        }
                    }
                }
                if (!jobtypeid.equals(lastjobtype)) {
                    database.executePreparedUpdate("UPDATE sysuser SET tracelogid = null, lastjobtype=? WHERE sysuserid=?", jobtypeid, sysuserid);
                }
                if (!isInitialLogon) {
                    database.executePreparedUpdate("UPDATE connection set currentjobtypeid=? WHERE connectionid=?", jobtypeid, userConnection.getConnectionId());
                }
                currentJobType = jobtypeid;
            }
            string = currentJobType;
        }
        catch (SapphireException e) {
            try {
                throw new ServiceException("DB_ACTION_FAILED", "Failed to synchronize job type to " + jobtypeid + " for user " + sysuserid + ". Likely caused by database not updated.", e);
            }
            catch (Throwable throwable) {
                try {
                    String callstmt = "{call lv_app" + (database.isOracle() ? "." : "_") + "setappinfouser( ? ) }";
                    CallableStatement cs = database.prepareCall(callstmt);
                    cs.setString(1, "");
                    cs.executeUpdate();
                    throw throwable;
                }
                catch (Exception ignore) {
                    Trace.logInfo("exception: " + ignore.getMessage());
                }
                throw throwable;
            }
        }
        try {
            String callstmt = "{call lv_app" + (database.isOracle() ? "." : "_") + "setappinfouser( ? ) }";
            CallableStatement cs = database.prepareCall(callstmt);
            cs.setString(1, "");
            cs.executeUpdate();
            return string;
        }
        catch (Exception ignore) {
            Trace.logInfo("exception: " + ignore.getMessage());
        }
        return string;
    }

    public static boolean isValidPassword(DBUtil database, String sysuserid, String password) throws ServiceException {
        Trace.logInfo(LOGNAME, "Checking valid password for sysuserid '" + sysuserid + "'");
        try {
            if (password == null) {
                password = "";
            }
            password = password.trim();
            String passwordValidator = ConfigService.getConfigProperty("com.labvantage.sapphire.server.passwordvalidator");
            String passwordValidatorNode = ConfigService.getConfigProperty("com.labvantage.sapphire.server.passwordvalidatornode");
            if (passwordValidator == null || passwordValidator.length() == 0) {
                passwordValidator = "DefaultPassword";
            }
            if (passwordValidatorNode == null || passwordValidatorNode.length() == 0) {
                passwordValidatorNode = "Default";
            }
            database.createPreparedResultSet("SELECT objectname, valuetree FROM propertytree WHERE propertytreeid = ?", passwordValidator);
            if (database.getNext()) {
                String className = database.getString("objectname");
                Trace.logInfo(LOGNAME, "Creating password validator class using: " + className);
                BasePasswordValidator validator = (BasePasswordValidator)Class.forName(className).newInstance();
                String valuetree = database.getClob("valuetree");
                PropertyDefinitionList propertyDefinitionList = PropertyTreeUtil.getPropertyTreeDefinitionList(database, passwordValidator);
                PropertyList properties = new PropertyList();
                properties.setPropertyTree(valuetree, passwordValidatorNode, propertyDefinitionList);
                boolean isCaseSensitive = database.getPreparedCount("SELECT count(*) FROM sysuser WHERE sysuserid=? AND casesensitivepasswordflag='Y'", new Object[]{sysuserid}) == 1;
                properties.setProperty("iscasesensitive", isCaseSensitive ? "Y" : "N");
                validator.startPasswordHandler(database);
                validator.checkPasswordFormat(sysuserid, password, properties);
                validator.endPasswordHandler();
            } else {
                Trace.logWarn(LOGNAME, "Property tree not found for passwordValidator: " + passwordValidator + ". Applying default rules.");
                if (password.length() < 1 || password.length() > 100) {
                    throw new ServiceException("INVALID_USERPASSWORD", "Password must be between 1 and 100 letters long");
                }
            }
        }
        catch (SapphireException se) {
            throw new ServiceException("INVALID_USERPASSWORD", "Error checking password. Exception: " + se.getMessage(), se);
        }
        catch (ClassNotFoundException e) {
            throw new ServiceException("INVALID_USERPASSWORD", "Password validator class not found. Exception: " + e.getMessage(), e);
        }
        catch (InstantiationException e) {
            throw new ServiceException("INVALID_USERPASSWORD", "Failed to instantiate password validator class. Exception: " + e.getMessage(), e);
        }
        catch (IllegalAccessException e) {
            throw new ServiceException("INVALID_USERPASSWORD", "Failed to access password validator class. Exception: " + e.getMessage(), e);
        }
        return true;
    }

    public static void changePassword(DBUtil database, String databaseid, String sysuserid, String oldpassword, String newpassword) throws ServiceException {
        Trace.logInfo(LOGNAME, "Changing password for user '" + sysuserid + "'");
        try {
            if ("Y".equals(hasExtAuthentication.get(databaseid))) {
                database.createPreparedResultSet("SELECT authenticationtypeflag FROM sysuser WHERE sysuserid=?", sysuserid);
                if (database.getNext() && !"I".equals(database.getString("authenticationtypeflag"))) {
                    throw new ServiceException("CHANGEPASSWORD_FAILURE", "User " + sysuserid + " is externally authenticated. Password change must be done through the external authentication server.");
                }
            }
            if (oldpassword != null && oldpassword.indexOf("{|}") == 0) {
                oldpassword = EncryptDecrypt.decryptRSA(oldpassword.substring("{|}".length()));
                newpassword = EncryptDecrypt.decryptRSA(newpassword.substring("{|}".length()));
            }
            if (SecurityService.isValidPassword(database, sysuserid, newpassword)) {
                String sql;
                SapphireConnection sapphireConnection = new SapphireConnection();
                sapphireConnection.setDbms(database.getDbms());
                sapphireConnection.setConnection(database.getConnection());
                sapphireConnection.setDatabaseId(databaseid);
                sapphireConnection.setSysuserId(sysuserid);
                sapphireConnection.setPassword(oldpassword);
                sapphireConnection.setChangePassword(true);
                DataSet sysuser = SecurityService.getUserDataSet(sapphireConnection);
                SecurityService.authenticateUser(sapphireConnection, sysuser, true);
                ConfigService configService = new ConfigService(sapphireConnection);
                String days = configService.getProfileProperty("(system)", "PasswordExpiryDays");
                SafeSQL safeSQL = new SafeSQL();
                if (days != null && days.length() > 0) {
                    Calendar cal = new DateTimeUtil().getCalendar("now+" + days + "d");
                    sql = "UPDATE sysuser SET tracelogid = null, changepasswordflag='N', password=" + safeSQL.addVar(EncryptDecrypt.encodePassword(newpassword)) + ", casesensitivepasswordflag=" + safeSQL.addVar("Y") + ", passwordexpirydt = " + safeSQL.addVar(new Timestamp(cal.getTime().getTime())) + " WHERE upper( sysuserid ) = " + safeSQL.addVar(sysuserid.toUpperCase()) + " OR upper( logonname ) = " + safeSQL.addVar(sysuserid.toUpperCase());
                } else {
                    sql = "UPDATE sysuser SET tracelogid = null, changepasswordflag='N', password=" + safeSQL.addVar(EncryptDecrypt.encodePassword(newpassword)) + ", casesensitivepasswordflag=" + safeSQL.addVar("Y") + ", passwordexpirydt = null WHERE upper( sysuserid ) = " + safeSQL.addVar(sysuserid.toUpperCase()) + " OR upper( logonname ) = " + safeSQL.addVar(sysuserid.toUpperCase());
                }
                if (database.executePreparedUpdate(sql, safeSQL.getValues()) != 1) {
                    throw new ServiceException("CHANGEPASSWORD_FAILURE", "No rows updated when attempting to change password for user " + sysuserid + ".");
                }
            }
        }
        catch (SapphireException e) {
            throw new ServiceException("CHANGEPASSWORD_FAILURE", "Failed to change password for user " + sysuserid + ". Exception: " + e.getMessage(), e);
        }
    }

    public static SapphireConnection getSapphireConnection(DBUtil database, String connectionid) throws ServiceException {
        return SecurityService.getSapphireConnection(database, connectionid, true);
    }

    public static SapphireConnection getSapphireConnection(DBUtil database, String connectionid, boolean keepAlive) throws ServiceException {
        SapphireConnection sapphireConnection = (SapphireConnection)sapphireConnectionCache.get(connectionid);
        if (sapphireConnection == null) {
            try {
                database.createPreparedResultSet("SELECT c.sysuserid, c.deviceid, c.portalid, c.tool, c.dbms, c.usertypeflag, c.connectdt, c.deletedt, c.connectfrom, c.currentjobtypeid, s.localeid, s.timezone, s.externalappid, s.languageid, s.defaultdepartment, s.defaultjobtype, s.sysuserdesc, s.logonname, s.nameduserflag, cl.guimode, cl.clientuseragent FROM   connection c LEFT OUTER JOIN " + database.hint("sysuser s") + " ON c.sysuserid=s.sysuserid LEFT OUTER JOIN connectionlog cl ON c.connectionid=cl.connectionid WHERE  c.connectionid = ?", connectionid);
                if (!database.getNext()) {
                    throw new ServiceException("INVALID_CONNECTION", "Failed to find connection '" + connectionid + "' in connection table");
                }
                sapphireConnection = new SapphireConnection();
                sapphireConnection.setConnectionId(connectionid);
                sapphireConnection.setDatabaseId(SecurityService.getDatabaseId(connectionid));
                sapphireConnection.setJndiname(Configuration.getInstance().getSapphireDatabase(sapphireConnection.getDatabaseId()).getJndiname());
                sapphireConnection.setSysuserId(database.getString("sysuserid"));
                sapphireConnection.setSysuserName(database.getString("sysuserdesc"));
                sapphireConnection.setLogonName(database.getString("logonname"));
                sapphireConnection.setDeviceId(database.getString("deviceid"));
                sapphireConnection.setPortalId(database.getString("portalid"));
                sapphireConnection.setGuimode(database.getString("guimode"));
                sapphireConnection.setTool(database.getString("tool"));
                sapphireConnection.setDbms(database.getString("dbms"));
                sapphireConnection.setUserType(database.getString("nameduserflag") == null ? "" : database.getString("nameduserflag"));
                sapphireConnection.setLocale(database.getString("localeid"));
                sapphireConnection.setExternalAppId(database.getString("externalappid"));
                sapphireConnection.setTimezone(database.getString("timezone"));
                sapphireConnection.setDefaultDepartment(database.getString("defaultdepartment"));
                sapphireConnection.setConnectDt(database.getTimestamp("connectdt"));
                sapphireConnection.setDeleteDt(database.getTimestamp("deletedt"));
                sapphireConnection.setConnectFrom(database.getString("connectfrom"));
                sapphireConnection.setCurrentJobtype(database.getString("currentjobtypeid"));
                sapphireConnection.setUserAgent(database.getString("clientuseragent"));
                String sysuserLanguageid = database.getString("languageid");
                StringBuffer rolelist = new StringBuffer();
                if (sapphireConnection.getSysuserId().equals("(system)")) {
                    database.createResultSet("SELECT roleid FROM role");
                } else {
                    database.createPreparedResultSet("SELECT roleid FROM sysuserrole WHERE sysuserid = ?", sapphireConnection.getSysuserId());
                }
                while (database.getNext()) {
                    rolelist.append(";").append(database.getString("roleid"));
                }
                sapphireConnection.setRoleList(rolelist.length() == 0 ? "" : rolelist.substring(1));
                StringBuffer moduleList = new StringBuffer();
                StringBuffer licensedModules = new StringBuffer();
                String[] modules = Configuration.getInstance().getLicense(sapphireConnection.getDatabaseId()).getModuleList();
                for (int i = 0; i < modules.length; ++i) {
                    licensedModules.append(",'").append(modules[i]).append("'");
                }
                if (sapphireConnection.getSysuserId().equals("(system)")) {
                    database.createResultSet("SELECT moduleid FROM module WHERE licensedflag = 'N' UNION SELECT moduleid FROM module WHERE " + (modules.length > 0 ? "moduleid IN (" + licensedModules.substring(1) + ") AND" : "") + " maxusers='S'");
                } else {
                    database.createPreparedResultSet("SELECT moduleid FROM modulesysuser WHERE sysuserid = ? UNION SELECT moduleid FROM module WHERE " + (modules.length > 0 ? "moduleid IN (" + licensedModules.substring(1) + ") AND" : "") + " maxusers='S'", sapphireConnection.getSysuserId());
                }
                while (database.getNext()) {
                    moduleList.append(";").append(database.getString("moduleid"));
                }
                sapphireConnection.setModuleList(moduleList.length() == 0 ? "" : moduleList.substring(1));
                String propertySelect = "SELECT\tpropertyid,sysuserid,propertyvalue FROM\tprofileproperty WHERE\tprofileid = 'System' AND \t\tsysuserid in ('(system)',?) AND \t\tpropertyid = 'language' AND \t\t((profileid) NOT IN \t\t(SELECT a.profileid \t\tFROM\tprofileproperty a,profileproperty b \t\tWHERE\ta.profileid = b.profileid AND \t\t\t\ta.propertyid = b.propertyid AND \t\t\t\ta.sysuserid = '(system)' AND \t\t\t\tb.sysuserid = ?) OR \t\t(propertyid) NOT IN \t\t(SELECT a.propertyid \t\tFROM\tprofileproperty a,profileproperty b \t\tWHERE\ta.profileid = b.profileid AND \t\t\t\ta.propertyid = b.propertyid AND \t\t\t\ta.sysuserid = '(system)' AND \t\t\t\tb.sysuserid = ?) OR \t\t(sysuserid) NOT IN \t\t(SELECT '(system)' \t\tFROM\tprofileproperty a,profileproperty b \t\tWHERE\ta.profileid = b.profileid AND \t\t\t\ta.propertyid = b.propertyid AND \t\t\t\ta.sysuserid = '(system)' AND \t\t\t\tb.sysuserid = ?))";
                Object[] params = new Object[]{sapphireConnection.getSysuserId(), sapphireConnection.getSysuserId(), sapphireConnection.getSysuserId(), sapphireConnection.getSysuserId()};
                database.createPreparedResultSet(propertySelect, params);
                while (database.getNext()) {
                    String value = database.getString("propertyvalue");
                    sapphireConnection.setProfileProperty(database.getString("propertyid"), value == null || value.length() == 0 ? null : value);
                }
                if (sysuserLanguageid != null && sysuserLanguageid.length() > 0) {
                    sapphireConnection.setLanguage(sysuserLanguageid);
                }
                StringBuffer departmentList = new StringBuffer();
                if (!sapphireConnection.getSysuserId().equals("(system)")) {
                    Object[] o = new Object[]{sapphireConnection.getSysuserId(), sapphireConnection.getSysuserId()};
                    database.createPreparedResultSet("SELECT ds.departmentid FROM departmentsysuser ds, department d WHERE ds.departmentid=d.departmentid AND (d.activeflag is null OR d.activeflag!='N') AND ds.sysuserid = ? UNION SELECT defaultdepartment departmentid FROM sysuser WHERE sysuserid = ? AND defaultdepartment is not null AND defaultdepartment in ( SELECT departmentid FROM department WHERE activeflag is null OR activeflag!='N' ) ", o);
                    while (database.getNext()) {
                        departmentList.append(";").append(database.getString("departmentid"));
                    }
                }
                sapphireConnection.setDepartmentList(departmentList.length() == 0 ? "" : departmentList.substring(1));
                StringBuffer jobtypeList = new StringBuffer();
                if (!sapphireConnection.getSysuserId().equals("(system)")) {
                    String jobtypeid;
                    database.createPreparedResultSet("SELECT defaultjobtype as jobtypeid FROM sysuser WHERE sysuserid = ? ", new String[]{sapphireConnection.getSysuserId()});
                    while (database.getNext()) {
                        jobtypeid = database.getString("jobtypeid");
                        if (jobtypeid == null || jobtypeList.indexOf(";" + jobtypeid) >= 0) continue;
                        jobtypeList.append(";").append(jobtypeid);
                    }
                    database.createPreparedResultSet("SELECT jobtypeid FROM sysuserjobtype WHERE sysuserid = ?", new String[]{sapphireConnection.getSysuserId()});
                    while (database.getNext()) {
                        jobtypeid = database.getString("jobtypeid");
                        if (jobtypeid == null || jobtypeList.indexOf(";" + jobtypeid) >= 0) continue;
                        jobtypeList.append(";").append(jobtypeid);
                    }
                }
                sapphireConnection.setJobtypeList(jobtypeList.indexOf(";") == 0 ? jobtypeList.substring(1) : jobtypeList.toString());
                try {
                    WebAdminService was = new WebAdminService(sapphireConnection);
                    PropertyList guiPolicy = null;
                    PropertyTree policy = new PropertyTree("GUIPolicy");
                    String xml = PropertyTreeUtil.getPropertyTreeValue(database, "GUIPolicy", true);
                    String defXml = PropertyTreeUtil.getPropertyTreeDefinition(database, "GUIPolicy", true);
                    policy.setValueXML(xml);
                    policy.setDefinitionXML(defXml);
                    if (policy != null) {
                        guiPolicy = policy.getNodePropertyList("Sapphire Custom", true);
                        guiPolicy.setDbms(sapphireConnection.getDbms());
                        sapphireConnection.setUseFullIncludes(guiPolicy.getProperty("usefullincludes", "N").equalsIgnoreCase("Y"));
                    }
                }
                catch (Exception e) {
                    Trace.logWarn(LOGNAME, "Failed to obtain GUI Policy.");
                }
                if (sysuserLanguageid != null && sysuserLanguageid.length() > 0) {
                    try {
                        database.createPreparedResultSet("SELECT languageid, rtlflag FROM language WHERE languageid = ? ", new Object[]{sysuserLanguageid});
                        if (database.getNext()) {
                            sapphireConnection.setRtl(database.getString("rtlflag").equalsIgnoreCase("Y"));
                        }
                    }
                    catch (Exception e) {
                        Trace.logWarn(LOGNAME, "Failed to obtain RT Flag.");
                    }
                }
                sapphireConnectionCache.put(connectionid, sapphireConnection);
            }
            catch (SapphireException e) {
                throw new ServiceException("DB_ACTION_FAILED", "Failed to retrieve connection details for connectionid " + connectionid, e);
            }
        }
        if (keepAlive && !Trace.isNoKeepAliveThread()) {
            sapphireConnection.setLastAccessedDt(Calendar.getInstance());
            if (SecurityService.isUpdateConnectionCacheDue() && !updatePending) {
                updatePending = true;
                final String cid = sapphireConnection.getConnectionId();
                executor.submit(new Runnable(){

                    @Override
                    public void run() {
                        try {
                            ServiceLocator.getInstance().getTDQManager().writeConnectionCache(cid);
                        }
                        catch (Throwable t) {
                            Trace.logWarn(t.getMessage());
                        }
                        finally {
                            updatePending = false;
                        }
                    }
                });
            }
        }
        sapphireConnection = sapphireConnection.copy();
        sapphireConnection.setConnection(database.getConnection());
        return sapphireConnection;
    }

    public void prepareToDeleteConnection(String connectionid) throws ServiceException {
        this.logInfo("Preparing connection for delete: " + connectionid);
        if (connectionid == null || connectionid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Connectionid not specified");
        }
        DBUtil dbu = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            dbu.setConnection(this.sapphireConnection);
            String callstmt = "{call lv_rset" + (this.sapphireConnection.isOracle() ? "." : "_") + "MarkConnList( ?, ? ) }";
            CallableStatement cs = dbu.prepareCall(callstmt);
            cs.setString(1, connectionid);
            cs.registerOutParameter(2, 12);
            cs.executeUpdate();
            String status = cs.getString(2);
            if (status.equals("U")) {
                this.logDebug("Connection " + connectionid + " marked for delete");
            } else if (status.equals("I")) {
                this.logDebug("Connection " + connectionid + " already marked for delete");
            } else {
                this.logDebug("Connection mark for delete status for " + connectionid + ": " + status + " ==> " + (status.equals("B") ? "busy - can't get connection lock" : (status.equals("M") ? "missing  - connectionid not found" : "Unrecognized return status")));
            }
            SapphireConnection sapphireConnection2 = (SapphireConnection)sapphireConnectionCache.get(connectionid);
            if (sapphireConnection2 != null) {
                sapphireConnection2.setDeleteDt(DateTimeUtil.getNowTimestamp());
                this.logDebug("The connectionid " + connectionid + " removed from cache");
            }
            this.removeProcessAsConnectionCache(this.sapphireConnection.getDatabaseId(), connectionid);
        }
        catch (Exception e) {
            throw new ServiceException("DB_ACTION_FAILED", "Failed to mark connectionid for delete: " + connectionid, e);
        }
        finally {
            dbu.reset();
        }
    }

    public void clearConnection(String connectionid, boolean prepared) throws ServiceException {
        this.logInfo("Clearing connectionid '" + connectionid + "'");
        if (connectionid == null || connectionid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Connectionid not specified");
        }
        DBUtil dbu = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            dbu.setConnection(this.sapphireConnection);
            FileManager.TempFile.removeTempFilesForConnection(connectionid, dbu);
            String callstmt = "{call lv_rset" + (this.sapphireConnection.isOracle() ? "." : "_") + "ClearConnection( ?, ?, ? ) }";
            CallableStatement cs = dbu.prepareCall(callstmt);
            cs.setString(1, connectionid);
            cs.registerOutParameter(2, 12);
            cs.setString(3, prepared ? "Y" : "N");
            cs.executeUpdate();
            String status = cs.getString(2);
            if (status.equals("Y")) {
                this.logInfo("Connection " + connectionid + " cleared");
            } else {
                this.logInfo("Connection clear status for " + connectionid + ": " + status + " ==> " + (status.equals("B") ? "busy - can't get connection lock" : (status.equals("L") ? "busy - can't get connection log lock" : (status.equals("M") ? "missing  - connectionid not found" : (status.equals("N") ? "not cleared - failed to clear all associated rsets" : (status.equals("D") ? "not cleared - not found (missing connection or deletedt is null when specified)" : "Unrecognized return status"))))));
            }
            CacheUtil.removeAllStartWith(this.sapphireConnection.getDatabaseId(), "WebPageAccess", connectionid + ";");
            CacheUtil.remove(this.connectionInfo.getDatabaseId(), "Connection Properties", connectionid);
            CacheUtil.removeAllEndWith(this.sapphireConnection.getDatabaseId(), "GizmoDefAccess", ";" + connectionid);
            CacheUtil.removeAllEndWith(this.sapphireConnection.getDatabaseId(), "GizmoDefSecured", ";" + connectionid);
            CacheUtil.removeAllEndWith(this.sapphireConnection.getDatabaseId(), "GizmoDefUser", ";" + connectionid);
            CacheUtil.removeAllEndWith(this.sapphireConnection.getDatabaseId(), "DataCharts", ";" + connectionid);
            SapphireConnection sapphireConnection2 = (SapphireConnection)sapphireConnectionCache.get(connectionid);
            if (sapphireConnection2 != null) {
                try {
                    NotifyManager.notifyLogoff(sapphireConnection2, true);
                }
                catch (Exception e) {
                    this.logError("Failed to notify logoff", e);
                }
            }
            AutomationService.broadcastServerCommand(this.sapphireConnection.getDatabaseId(), "ClearConnectionFromCache", connectionid);
            sapphireConnectionCache.remove(connectionid);
            this.removeProcessAsConnectionCache(this.sapphireConnection.getDatabaseId(), connectionid);
        }
        catch (Exception e) {
            throw new ServiceException("DB_ACTION_FAILED", "Database action exception: " + e.getMessage(), e);
        }
        finally {
            dbu.reset();
        }
    }

    private void removeProcessAsConnectionCache(String databaseId, String connectionId) {
        String[] parts;
        if (connectionId.startsWith(ENCRYPTEDCONNECTIONID_MARKER)) {
            connectionId = EncryptDecrypt.decrypt(connectionId.substring(1));
        }
        if ((parts = StringUtil.split(connectionId, "|")).length > 1) {
            String userIdPart = parts[1];
            String CACHENAME = "ProcessAsConnections";
            String[] keys = CacheUtil.keySet(databaseId, CACHENAME).toArray(new String[0]);
            if (keys == null) {
                return;
            }
            for (String key : keys) {
                if (!userIdPart.startsWith(key + "-") && !userIdPart.startsWith(VIRTUALUSER_MARKER + key + "-") && !userIdPart.startsWith(PORTALUSER_MARKER + key + "-")) continue;
                CacheUtil.remove(databaseId, CACHENAME, key + "-");
            }
        }
    }

    public static String getDatabaseId(String connectionid) {
        String databaseid = "";
        if (connectionid != null && connectionid.length() > 0) {
            int pos;
            if (connectionid.startsWith(ENCRYPTEDCONNECTIONID_MARKER)) {
                connectionid = EncryptDecrypt.decrypt(connectionid.substring(1));
            }
            if ((pos = connectionid.indexOf("|")) > -1) {
                databaseid = connectionid.substring(0, pos);
            }
        } else {
            Trace.logDebug(LOGNAME, "Warning: Trying to get the databaseid for empty connectionid");
        }
        return databaseid;
    }

    public static boolean isVirtualUser(String connectionid) {
        boolean isVirtual = false;
        if (connectionid != null && connectionid.length() > 0) {
            int pos;
            if (connectionid.startsWith(ENCRYPTEDCONNECTIONID_MARKER)) {
                connectionid = EncryptDecrypt.decrypt(connectionid.substring(1));
            }
            if ((pos = connectionid.indexOf("|")) > -1) {
                isVirtual = connectionid.substring(pos + 1, pos + 2).equals(VIRTUALUSER_MARKER);
            }
        }
        return isVirtual;
    }

    public static boolean isSystemUser(String connectionid) {
        boolean isSystemUser = false;
        if (connectionid != null && connectionid.length() > 0) {
            if (connectionid.startsWith(ENCRYPTEDCONNECTIONID_MARKER)) {
                connectionid = EncryptDecrypt.decrypt(connectionid.substring(1));
            }
            isSystemUser = connectionid.contains("|(system)-");
        }
        return isSystemUser;
    }

    public static boolean isPortalUser(String connectionid) {
        boolean isPortal = false;
        if (connectionid != null && connectionid.length() > 0) {
            int pos;
            if (connectionid.startsWith(ENCRYPTEDCONNECTIONID_MARKER)) {
                connectionid = EncryptDecrypt.decrypt(connectionid.substring(1));
            }
            if ((pos = connectionid.indexOf("|")) > -1) {
                isPortal = connectionid.substring(pos + 1, pos + 2).equals(PORTALUSER_MARKER);
            }
        }
        return isPortal;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean checkUser(String sysuserid, String password) throws ServiceException {
        this.logInfo("Checking user '" + sysuserid + "'");
        String connectionUsername = this.sapphireConnection.getSysuserId();
        String connectionPassword = this.sapphireConnection.getPassword();
        this.sapphireConnection.setSysuserId(sysuserid);
        if (password != null && password.indexOf("{|}") == 0) {
            password = EncryptDecrypt.decryptRSA(password.substring("{|}".length()));
        }
        String unicodepassword = "";
        if (password.indexOf("{|}") > 0) {
            String[] pwds = StringUtil.split(password, "{|}");
            password = pwds[1];
            try {
                unicodepassword = URLDecoder.decode(pwds[0], "UTF-8");
            }
            catch (UnsupportedEncodingException unsupportedEncodingException) {
                // empty catch block
            }
        }
        if ("".equals(unicodepassword)) {
            unicodepassword = password;
        }
        this.sapphireConnection.setPassword(password);
        this.sapphireConnection.setIgnoreExpiryWarning(true);
        try {
            DataSet sysuser;
            block21: {
                sysuser = SecurityService.getUserDataSet(this.sapphireConnection);
                if (!"N".equals(hasExtAuthentication.get(this.sapphireConnection.getDatabaseId()))) {
                    DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
                    try {
                        db.setConnection(this.sapphireConnection);
                        db.createPreparedResultSet("isUserInSession", "SELECT sysuserid FROM connection WHERE deletedt is null and upper( sysuserid ) = ?", this.sapphireConnection.getSysuserId().toUpperCase());
                        if ("N".equals(alwaysExtAuthentication.get(this.sapphireConnection.getDatabaseId() + "_syncpassword")) || "Y".equals(alwaysExtAuthentication.get(this.sapphireConnection.getDatabaseId())) || !db.getNext("isUserInSession") || hasExtAuthentication.get(this.sapphireConnection.getDatabaseId()) == null) {
                            SecurityService.externalAuthenticate(this.sapphireConnection, sysuser, unicodepassword);
                            sysuser = SecurityService.getUserDataSet(this.sapphireConnection);
                        }
                    }
                    catch (Exception e) {
                        boolean logFailedAttempt;
                        String message = e.getMessage();
                        boolean bl = logFailedAttempt = message.indexOf("Authentication failed. Incorrect username or password for username") >= 0;
                        if (message.indexOf("|%|") > 0) {
                            message = message.substring(message.indexOf("|%|") + 3);
                        }
                        if (logFailedAttempt) {
                            this.sapphireConnection.setPassword("failedExternalAuthentication_" + this.sapphireConnection.getPassword());
                            try {
                                SecurityService.authenticateUser(this.sapphireConnection, sysuser, true);
                                break block21;
                            }
                            catch (Exception ex) {
                                throw new ServiceException(message + ex.getMessage());
                            }
                        }
                        throw new ServiceException("DB_ACTION_FAILED", "Failed to check '" + this.sapphireConnection.getSysuserId() + "' in connection table. Exception: " + e.getMessage(), e);
                    }
                    finally {
                        db.reset();
                    }
                }
            }
            SecurityService.authenticateUser(this.sapphireConnection, sysuser, false);
            boolean bl = true;
            return bl;
        }
        catch (ServiceException e) {
            throw e;
        }
        finally {
            this.sapphireConnection.setSysuserId(connectionUsername);
            this.sapphireConnection.setPassword(connectionPassword);
        }
    }

    public static boolean isSystemUser(String databaseid, String sysuserid) throws ServiceException {
        boolean isSystemUser;
        Trace.logDebug(LOGNAME, "Checking system user '" + sysuserid + "'");
        if (sysuserid.equals("(system)")) {
            isSystemUser = true;
        } else {
            try {
                Configuration configuration = Configuration.getInstance();
                SapphireDatabase database = configuration.getSapphireDatabase(databaseid);
                String[] parts = StringUtil.split(EncryptDecrypt.decrypt(database.getDbConnectionKey(), "admindb"), "|");
                isSystemUser = parts[0].equals(sysuserid);
            }
            catch (SapphireException e) {
                throw new ServiceException("CONFIGURATION_ERROR", "Configuration error", e);
            }
        }
        return isSystemUser;
    }

    private static boolean validSystemUser(SapphireConnection sapphireConnection, String databaseid, String userid, String password) throws ServiceException {
        try {
            boolean enableSystemUser = ConfigService.getConfigPropertyBoolean("com.labvantage.sapphire.server.enablesystemuser", false);
            if (userid.equals("(system)") && password.equals(systemPassword)) {
                return true;
            }
            Configuration configuration = Configuration.getInstance();
            SapphireDatabase database = configuration.getSapphireDatabase(databaseid);
            if (enableSystemUser && database.getDbConnectionKey().equals(EncryptDecrypt.encrypt(userid + "|" + password, "admindb"))) {
                SecurityService.logUserActivity(sapphireConnection, "(system)", "log_setuser", null, "", 0, 0, true);
                return true;
            }
            SecurityService.logUserActivity(sapphireConnection, "(system)", "log_setuserfail", null, "", 0, 0, true);
            throw new ServiceException("INVALID_USERPASSWORD", SecurityService.translateText(sapphireConnection, INVALID_USERNAME_OR_PASSWORD));
        }
        catch (SapphireException e) {
            throw new ServiceException("CONFIGURATION_ERROR", "Configuration error", e);
        }
    }

    private static void externalAuthenticate(SapphireConnection sapphireConnection, DataSet sysuser, String unicodepassword) throws ServiceException {
        block27: {
            if (sysuser.size() == 0 || sysuser.size() == 1 && !"I".equals(sysuser.getString(0, "authenticationtypeflag"))) {
                DBUtil db = new DBUtil();
                try {
                    db.setConnection(sapphireConnection);
                    String authenticationPropertytree = ConfigService.getConfigProperty("com.labvantage.sapphire.server.authenticationpropertytree");
                    String authenticationPropertytreeNode = ConfigService.getConfigProperty("com.labvantage.sapphire.server.authenticationpropertytreenode");
                    if (authenticationPropertytree == null || authenticationPropertytree.length() == 0) {
                        authenticationPropertytree = "DefaultLDAPAuthentic";
                    }
                    if (authenticationPropertytreeNode == null || authenticationPropertytreeNode.length() == 0) {
                        authenticationPropertytreeNode = "Default";
                    }
                    db.createPreparedResultSet("SELECT objectname, valuetree FROM propertytree WHERE propertytreeid = ?", authenticationPropertytree);
                    if (db.getNext()) {
                        BaseAuthentication authentication;
                        String className;
                        String valuetree = db.getClob("valuetree");
                        PropertyList ldapPropertyTree = new PropertyList();
                        PropertyDefinitionList propertyDefinitionList = PropertyTreeUtil.getPropertyTreeDefinitionList(db, authenticationPropertytree);
                        ldapPropertyTree.setPropertyTree(valuetree, authenticationPropertytreeNode, propertyDefinitionList);
                        if (!"Y".equals(ldapPropertyTree.getProperty("enabled")) || "DefaultLDAPAuthentic".equals(authenticationPropertytree) && ldapPropertyTree.getProperty("PROVIDER_URL").length() == 0) {
                            hasExtAuthentication.put(sapphireConnection.getDatabaseId(), "N");
                        }
                        if ("Y".equals(ldapPropertyTree.getProperty("always"))) {
                            alwaysExtAuthentication.put(sapphireConnection.getDatabaseId(), "Y");
                        }
                        if ("N".equals(ldapPropertyTree.getProperty("syncpassword"))) {
                            alwaysExtAuthentication.put(sapphireConnection.getDatabaseId() + "_syncpassword", "N");
                        }
                        if ("N".equals(hasExtAuthentication.get(sapphireConnection.getDatabaseId()))) break block27;
                        ldapPropertyTree.setProperty("tool", sapphireConnection.getTool());
                        if (sapphireConnection.getPortalId() != null) {
                            ldapPropertyTree.setProperty("portalid", sapphireConnection.getPortalId());
                        }
                        if ((className = db.getString("objectname")) == null || className.length() == 0) {
                            className = "";
                        }
                        Trace.log("Creating external authentication class using: " + className);
                        try {
                            authentication = (BaseAuthentication)Class.forName(className).newInstance();
                        }
                        catch (Exception cnfe) {
                            throw new SapphireException(cnfe.getMessage(), cnfe);
                        }
                        authentication.startAuthenticate(sapphireConnection.getDatabaseId(), db);
                        String logonname = sysuser != null && sysuser.size() == 1 && sysuser.getValue(0, "logonname").length() > 0 ? sysuser.getValue(0, "logonname") : sapphireConnection.getSysuserId();
                        authentication.authenticateUser(logonname, unicodepassword, ldapPropertyTree);
                        String inputPassword = sapphireConnection.getPassword();
                        String storedPassword = null;
                        boolean isSyncPassword = !"N".equals(alwaysExtAuthentication.get(sapphireConnection.getDatabaseId() + "_syncpassword"));
                        boolean row = false;
                        if (sysuser != null && sysuser.size() == 1) {
                            storedPassword = sysuser.getValue(0, "password");
                            row = true;
                        }
                        SapphireConnection sysSapphireConnection = new SapphireConnection(sapphireConnection.getConnection(), sapphireConnection.getSapphireDatabase());
                        sysSapphireConnection.setSysuserId("(system)");
                        sysSapphireConnection.setPassword(systemPassword);
                        sysSapphireConnection.setTool(LOGNAME);
                        String sysConnectionid = SecurityService.getConnectionId(sysSapphireConnection);
                        authentication.setConnectionId(sysConnectionid);
                        if (row && isSyncPassword && !EncryptDecrypt.passwordMatches(inputPassword, storedPassword, true)) {
                            CallableStatement cs;
                            String callstmt;
                            try {
                                callstmt = "{call lv_app" + (db.isOracle() ? "." : "_") + "setappinfouser( ? ) }";
                                cs = db.prepareCall(callstmt);
                                cs.setString(1, "(system)");
                                cs.executeUpdate();
                            }
                            catch (Exception ignore) {
                                Trace.logInfo("exception: " + ignore.getMessage());
                            }
                            db.executePreparedUpdate("update sysuser set tracelogid = null, password=?, casesensitivepasswordflag=? where upper( sysuserid ) = ?", new Object[]{EncryptDecrypt.encodePassword(inputPassword), "Y", sapphireConnection.getSysuserId().toUpperCase()});
                            try {
                                callstmt = "{call lv_app" + (db.isOracle() ? "." : "_") + "setappinfouser( ? ) }";
                                cs = db.prepareCall(callstmt);
                                cs.setString(1, "");
                                cs.executeUpdate();
                            }
                            catch (Exception ignore) {
                                Trace.logInfo("exception: " + ignore.getMessage());
                            }
                        }
                        try {
                            authentication.secondaryAuthentication(ldapPropertyTree);
                        }
                        catch (SapphireException ex) {
                            throw new ServiceException("Secondary authentication failed.", "Secondary authentication failed for user " + sapphireConnection.getSysuserId() + ".");
                        }
                        if (!row) {
                            authentication.createUser(sapphireConnection.getSysuserId(), isSyncPassword ? inputPassword : "", ldapPropertyTree);
                        } else if (row && ldapPropertyTree.getPropertyList("defaultuserprops") != null && ldapPropertyTree.getPropertyList("defaultuserprops").getCollection("columns") != null && ldapPropertyTree.getPropertyList("defaultuserprops").getCollection("columns").size() > 0) {
                            authentication.synchronizeUser(sapphireConnection.getSysuserId(), isSyncPassword ? inputPassword : "", ldapPropertyTree);
                        }
                        authentication.endAuthenticate();
                        SecurityService securityService = new SecurityService(sysSapphireConnection);
                        securityService.clearConnection(sysConnectionid, false);
                        break block27;
                    }
                    Trace.log("Property tree not found for authentication: " + authenticationPropertytree + ". No external authentication.");
                }
                catch (SapphireException e) {
                    throw new ServiceException("SECURITY_SERVICE_FAILED", "Failed to externally authenticate user " + sapphireConnection.getSysuserId() + ". " + e.getMessage(), e);
                }
                finally {
                    db.reset();
                }
            }
        }
    }

    private static void externalSSOAuthenticate(SapphireConnection sapphireConnection, DataSet sysuser) throws ServiceException {
        block18: {
            if (sysuser.size() <= 1) {
                DBUtil db = new DBUtil();
                try {
                    db.setConnection(sapphireConnection);
                    String authenticationPropertytree = ConfigService.getConfigProperty("com.labvantage.sapphire.server.ssopropertytree");
                    String authenticationPropertytreeNode = ConfigService.getConfigProperty("com.labvantage.sapphire.server.ssopropertytreenode");
                    if (authenticationPropertytree == null || authenticationPropertytree.length() == 0) {
                        authenticationPropertytree = "DefaultSSOAuthentication";
                    }
                    if (authenticationPropertytreeNode == null || authenticationPropertytreeNode.length() == 0) {
                        authenticationPropertytreeNode = "Default";
                    }
                    db.createPreparedResultSet("SELECT objectname, valuetree FROM propertytree WHERE propertytreeid = ?", authenticationPropertytree);
                    if (db.getNext()) {
                        BaseAuthentication authentication;
                        String valuetree = db.getClob("valuetree");
                        PropertyList ssoPropertyTree = new PropertyList();
                        PropertyDefinitionList propertyDefinitionList = PropertyTreeUtil.getPropertyTreeDefinitionList(db, authenticationPropertytree);
                        ssoPropertyTree.setPropertyTree(valuetree, authenticationPropertytreeNode, propertyDefinitionList);
                        if (!"Y".equals(ssoPropertyTree.getProperty("enabled", "N"))) break block18;
                        String className = db.getString("objectname");
                        if (className == null || className.length() == 0) {
                            className = "";
                        }
                        Trace.log("Creating external authentication class using: " + className);
                        try {
                            authentication = (BaseAuthentication)Class.forName(className).newInstance();
                        }
                        catch (Exception cnfe) {
                            throw new SapphireException(cnfe.getMessage(), cnfe);
                        }
                        ssoPropertyTree.setProperty("tool", sapphireConnection.getTool());
                        if (sapphireConnection.getPortalId() != null) {
                            ssoPropertyTree.setProperty("portalid", sapphireConnection.getPortalId());
                        }
                        ssoPropertyTree.setProperty("ssoattributes", sapphireConnection.getSSOAttributes());
                        authentication.startAuthenticate(sapphireConnection.getDatabaseId(), db);
                        SapphireConnection sysSapphireConnection = new SapphireConnection(sapphireConnection.getConnection(), sapphireConnection.getSapphireDatabase());
                        sysSapphireConnection.setSysuserId("(system)");
                        sysSapphireConnection.setPassword(systemPassword);
                        sysSapphireConnection.setTool(LOGNAME);
                        String sysConnectionid = SecurityService.getConnectionId(sysSapphireConnection);
                        authentication.setConnectionId(sysConnectionid);
                        try {
                            authentication.secondaryAuthentication(ssoPropertyTree);
                        }
                        catch (SapphireException ex) {
                            throw new ServiceException("Secondary authentication failed.", "Secondary authentication failed for user " + sapphireConnection.getSysuserId() + ".");
                        }
                        if (sysuser == null || sysuser.getRowCount() == 0) {
                            authentication.createUser(sapphireConnection.getSysuserId(), "", ssoPropertyTree);
                        } else if (sysuser.getRowCount() == 1 && ssoPropertyTree.getPropertyList("ssouserprops") != null && ssoPropertyTree.getPropertyList("ssouserprops").getCollection("columns") != null && ssoPropertyTree.getPropertyList("ssouserprops").getCollection("columns").size() > 0) {
                            authentication.synchronizeUser(sysuser.getValue(0, "sysuserid"), "", ssoPropertyTree);
                        }
                        authentication.endAuthenticate();
                        SecurityService securityService = new SecurityService(sysSapphireConnection);
                        securityService.clearConnection(sysConnectionid, false);
                        break block18;
                    }
                    Trace.log("Property tree not found for authentication: " + authenticationPropertytree + ". No external authentication.");
                }
                catch (SapphireException e) {
                    throw new ServiceException("SECURITY_SERVICE_FAILED", "Failed to externally authenticate user " + sapphireConnection.getSysuserId() + ". " + e.getMessage(), e);
                }
                finally {
                    db.reset();
                }
            }
        }
    }

    private static DataSet getUserDataSet(SapphireConnection sapphireConnection) throws ServiceException {
        DataSet sysuser = new DataSet();
        DBUtil db = new DBUtil();
        db.setConnection(sapphireConnection);
        try {
            String passedInlogonid = sapphireConnection.getSysuserId();
            db.createPreparedResultSet("authenticateUser", "SELECT sysuserid, logonname, password, casesensitivepasswordflag, passwordexpirydt, cooloffenddt, disabledflag, disabledreason, totalattempts, failedattempts, nameduserflag, changepasswordflag, localeid, timezone, templateflag, authenticationtypeflag FROM sysuser WHERE ( activeflag != 'N' OR activeflag is null ) AND nameduserflag <> 'U' AND ( upper( sysuserid ) = ? OR upper( logonname ) = ? )", new String[]{passedInlogonid.toUpperCase(), passedInlogonid.toUpperCase()});
            sysuser.setResultSet(db.getResultSet("authenticateUser"));
        }
        catch (SapphireException se) {
            throw new ServiceException("SECURITY_SERVICE_FAILED", "Failed to authenticate user " + sapphireConnection.getSysuserId());
        }
        finally {
            db.reset();
        }
        return sysuser;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private static void authenticateUser(SapphireConnection sapphireConnection, DataSet sysuser, boolean newConnection) throws ServiceException {
        int totalAttempts = 0;
        int failedAttempts = 0;
        boolean failedAttempt = false;
        boolean isSystemPassword = false;
        boolean isWebSSO = false;
        String disableReason = "";
        String sysuserid = "";
        DBUtil db = new DBUtil();
        ConfigService configService = null;
        boolean disable_legacy_session_tracking = true;
        boolean disableUserOnPasswordExpiry = false;
        try {
            String callstmt;
            Timestamp coolOffDate = sysuser.getTimestamp(0, "cooloffenddt");
            if (coolOffDate != null && !coolOffDate.before(DateTimeUtil.getNowTimestamp())) {
                throw new ServiceException("INVALID_USERPASSWORD", SecurityService.translateText(sapphireConnection, "You have attempted too many invalid logins. Try again later."));
            }
            db.setConnection(sapphireConnection);
            configService = new ConfigService(sapphireConnection);
            sysuserid = sysuser.size() == 1 ? sysuser.getString(0, "sysuserid") : sapphireConnection.getSysuserId();
            disable_legacy_session_tracking = "Y".equals(configService.getProfileProperty("(system)", "disablelegacysessiontracking"));
            if (sysuser.size() != 1) throw new ServiceException("INVALID_USERPASSWORD", SecurityService.translateText(sapphireConnection, INVALID_USERNAME_OR_PASSWORD));
            String userType = sysuser.getString(0, "nameduserflag");
            totalAttempts = sysuser.getInt(0, "totalattempts");
            failedAttempts = sysuser.getInt(0, "failedattempts");
            if (sysuser.getString(0, "templateflag") != null && sysuser.getString(0, "templateflag").equals("Y")) {
                throw new ServiceException("DISABLED_USERACCOUNT", "You cannot logon as a template user!");
            }
            if (sysuser.getString(0, "disabledflag") != null && sysuser.getString(0, "disabledflag").equals("Y")) {
                throw new ServiceException("DISABLED_USERACCOUNT", "User '" + sapphireConnection.getSysuserId() + "' disabled. Reason: " + sysuser.getString(0, "disabledreason", ""));
            }
            if ((userType.equals("V") || userType.equals("I")) && sapphireConnection.getTool().equals("SapphireAdmin")) {
                failedAttempt = true;
                throw new ServiceException("VUSER_ACCESS_FAILURE", SecurityService.translateText(sapphireConnection, "Virtual users cannot use ") + "SapphireAdmin");
            }
            boolean isValidPassword = false;
            if (sapphireConnection.getPassword().equals(systemPassword)) {
                isSystemPassword = true;
                isValidPassword = true;
                if ("RequestController".equals(sapphireConnection.getTool())) {
                    isWebSSO = true;
                }
            } else if (sapphireConnection.getPassword().indexOf("failedExternalAuthentication_") == 0) {
                isValidPassword = false;
            } else if (!"I".equals(sysuser.getString(0, "authenticationtypeflag")) && !"N".equals(hasExtAuthentication.get(sapphireConnection.getDatabaseId())) && "N".equals(alwaysExtAuthentication.get(sapphireConnection.getDatabaseId() + "_syncpassword"))) {
                isValidPassword = true;
            } else {
                String encPassword = sysuser.getString(0, "password");
                boolean isCaseSensitivePassword = sysuser.getValue(0, "casesensitivepasswordflag", "Y").equals("Y");
                isValidPassword = EncryptDecrypt.passwordMatches(sapphireConnection.getPassword(), encPassword, isCaseSensitivePassword);
                if (!sapphireConnection.isChangePassword() && isValidPassword && !isCaseSensitivePassword) {
                    throw new ServiceException("MUST_CHANGE_CASE_INSENSITIVE_PASSWORD", SecurityService.translateText(sapphireConnection, "You must change your password to a case sensitive one."));
                }
                if (isValidPassword && EncryptDecrypt.passwordNeedUpgrade(encPassword) && isCaseSensitivePassword) {
                    db.executePreparedUpdate("update sysuser set tracelogid = null, casesensitivepasswordflag ='Y', password=? where upper( sysuserid ) = ?", new Object[]{EncryptDecrypt.encodePassword(sapphireConnection.getPassword()), sapphireConnection.getSysuserId().toUpperCase()});
                }
            }
            if (!isSystemPassword || isWebSSO) {
                Timestamp expirydt;
                if (!isValidPassword) {
                    failedAttempt = true;
                    throw new ServiceException("INVALID_USERPASSWORD", SecurityService.translateText(sapphireConnection, INVALID_USERNAME_OR_PASSWORD));
                }
                if (!isWebSSO && !sapphireConnection.isChangePassword() && sysuser.getString(0, "changepasswordflag") != null && sysuser.getString(0, "changepasswordflag").equals("Y")) {
                    throw new ServiceException("MUST_CHANGE_PASSWORD", SecurityService.translateText(sapphireConnection, "Administrator has requested that user '") + sysuserid + SecurityService.translateText(sapphireConnection, "' must change their password."));
                }
                Configuration configuration = Configuration.getInstance();
                if (userType.equals("C") || userType.equals("I") || userType.equals("X") || userType.equals("Q")) {
                    db.createPreparedResultSet("SELECT count(*) \"count\" FROM connection WHERE usertypeflag = ? AND deletedt IS NULL AND tool NOT IN (?,?)", new Object[]{userType, "TDLProcessing", "Internal_SaaS"});
                    db.getNext();
                    int count = db.getInt("count");
                    if (count == -1) throw new ServiceException("Failed to get concurrent user count");
                    if (newConnection) {
                        ++count;
                    }
                    if (count > (userType.equals("C") ? configuration.getLicense(sapphireConnection.getDatabaseId()).getConcurrentUsers() : (userType.equals("I") ? configuration.getLicense(sapphireConnection.getDatabaseId()).getVirtualConcurrentUsers() : (userType.equals("Q") ? configuration.getLicense(sapphireConnection.getDatabaseId()).getPortalConcurrentUsers() : configuration.getLicense(sapphireConnection.getDatabaseId()).getExpressConcurrentUsers()))) && !"Internal_SaaS".equals(sapphireConnection.getTool())) {
                        throw new ServiceException("EXCEEDED_CONCURRENTUSERS", SecurityService.translateText(sapphireConnection, "The number of concurrent ") + (userType.equals("C") ? "named" : (userType.equals("I") ? "virtual" : (userType.equals("Q") ? "portal" : "Express"))) + SecurityService.translateText(sapphireConnection, " users in the database exceeds the license key"));
                    }
                } else if (!userType.equals("A")) {
                    db.createPreparedResultSet("SELECT count(*) \"count\" FROM sysuser WHERE nameduserflag = ? AND ( templateflag is null or templateflag = 'N' ) AND ( ACTIVEFLAG = 'Y' OR ACTIVEFLAG IS NULL )", userType);
                    db.getNext();
                    int count = db.getInt("count");
                    if (count == -1) throw new ServiceException("Failed to get nameduser count");
                    if (count > (userType.equals("S") ? configuration.getLicense(sapphireConnection.getDatabaseId()).getNamedUsers() : (userType.equals("V") ? configuration.getLicense(sapphireConnection.getDatabaseId()).getVirtualUsers() : (userType.equals("P") ? configuration.getLicense(sapphireConnection.getDatabaseId()).getPortalNamedUsers() : configuration.getLicense(sapphireConnection.getDatabaseId()).getExpressUsers())))) {
                        throw new ServiceException("EXCEEDED_NAMEDUSERS", SecurityService.translateText(sapphireConnection, "The number of named ") + (userType.equals("S") ? "standard" : (userType.equals("V") ? "virtual" : (userType.equals("P") ? "portal" : "Express"))) + SecurityService.translateText(sapphireConnection, " users in the database exceeds the license key"));
                    }
                }
                if (!sapphireConnection.isChangePassword() && (expirydt = sysuser.getTimestamp(0, "passwordexpirydt")) != null) {
                    if (!expirydt.before(DateTimeUtil.getNowTimestamp())) {
                        if (!sapphireConnection.isIgnoreExpiryWarning()) {
                            try {
                                int warningDays = Integer.parseInt(configService.getProfileProperty("(system)", "PasswordExpiryWarningDays"));
                                if (expirydt.before(new DateTimeUtil().getTimestamp("n+" + warningDays + "D"))) {
                                    throw new ServiceException("PASSWORDEXPIRY_WARNING", SecurityService.translateText(sapphireConnection, "Password due to expire for username '") + sysuserid + "' on " + expirydt);
                                }
                            }
                            catch (NumberFormatException numberFormatException) {}
                        }
                    } else {
                        disableUserOnPasswordExpiry = StringUtil.getYN(configService.getProfileProperty("(system)", "DisableUserOnPasswordExpiry"), "Y").equals("Y");
                        if (!disableUserOnPasswordExpiry) throw new ServiceException("EXPIRED_PASSWORD", SecurityService.translateText(sapphireConnection, "Password expired for username '") + sysuserid + "'");
                        failedAttempt = true;
                        disableReason = "Account disabled because password has expired";
                        throw new ServiceException("DISABLED_USERACCOUNT", SecurityService.translateText(sapphireConnection, "Account disabled because password has expired for username '") + sysuserid + "'");
                    }
                }
            }
            if (isSystemPassword && !isWebSSO) return;
            if (totalAttempts < 0) {
                totalAttempts = 0;
            }
            try {
                callstmt = "{call lv_app" + (db.isOracle() ? "." : "_") + "setappinfouser( ? ) }";
                CallableStatement cs = db.prepareCall(callstmt);
                cs.setString(1, "(system)");
                cs.executeUpdate();
            }
            catch (Exception ignore) {
                Trace.logInfo("exception: " + ignore.getMessage());
            }
            if (disable_legacy_session_tracking) {
                db.executePreparedUpdate("UPDATE sysuser SET tracelogid = null, failedattempts = 0, cooloffenddt = null WHERE sysuserid = ? and failedattempts > 0", new Object[]{sysuserid});
            } else {
                db.executePreparedUpdate("UPDATE sysuser SET tracelogid = null, totalattempts = ?, failedattempts = 0, cooloffenddt = null, lastsetuserdt = ? WHERE sysuserid = ?", new Object[]{String.valueOf(totalAttempts + 1), DateTimeUtil.getNowTimestamp(), sysuserid});
            }
            try {
                callstmt = "{call lv_app" + (db.isOracle() ? "." : "_") + "setappinfouser( ? ) }";
                CallableStatement cs = db.prepareCall(callstmt);
                cs.setString(1, "");
                cs.executeUpdate();
            }
            catch (Exception ignore) {
                Trace.logInfo("exception: " + ignore.getMessage());
            }
            SecurityService.logUserActivity(sapphireConnection, sysuserid, "log_setuser", null, "", 0, 0, newConnection);
            return;
        }
        catch (SapphireException se) {
            throw new ServiceException("SECURITY_SERVICE_FAILED", "Failed to authenticate user " + sapphireConnection.getSysuserId());
        }
        catch (ServiceException se) {
            String errorid = se.getErrorid();
            ServiceException se2 = null;
            if (failedAttempt) {
                if (failedAttempts < 0) {
                    failedAttempts = 0;
                }
                ++failedAttempts;
                int logonAttempts = 3;
                int additionalLogonAttempts = 0;
                double maxCooldown = 3600.0;
                try {
                    logonAttempts = Integer.parseInt(configService.getProfileProperty("(system)", "logonattempts"));
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
                try {
                    additionalLogonAttempts = Integer.parseInt(configService.getProfileProperty("(system)", "additionallogonattempts"));
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
                try {
                    maxCooldown = Double.parseDouble(configService.getProfileProperty("(system)", "maxcooldown"));
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
                int maxLogonAttempts = logonAttempts + (newConnection ? additionalLogonAttempts : 0);
                PropertyList updateUserProps = new PropertyList();
                updateUserProps.setProperty("sdcid", "User");
                updateUserProps.setProperty("keyid1", sysuserid);
                updateUserProps.setProperty("tracelogid", "");
                updateUserProps.setProperty("failedattempts", String.valueOf(failedAttempts));
                if (failedAttempts >= maxLogonAttempts && (disableReason == null || disableReason.length() == 0)) {
                    disableReason = "Max login attempts exceeded";
                } else if (failedAttempts >= logonAttempts) {
                    updateUserProps.setProperty("cooloffenddt", "n+" + (long)Math.min(maxCooldown, Math.pow(2.0, failedAttempts - logonAttempts) * 10.0) + "s");
                }
                if (disableReason != null && disableReason.length() > 0) {
                    updateUserProps.setProperty("disabledflag", "Y");
                    updateUserProps.setProperty("disabledreason", disableReason);
                    String newExceptionMsg = se.getMessage() + "Reason: " + disableReason;
                    se2 = new ServiceException(se.getErrorid(), newExceptionMsg, se);
                }
                if ("EXPIRED_PASSWORD".equals(errorid) || !disableUserOnPasswordExpiry && "DISABLED_USERACCOUNT".equals(errorid)) throw se2 == null ? se : se2;
                SecurityService.logUserActivity(sapphireConnection, sysuserid, "log_setuserfail", updateUserProps, disableReason, logonAttempts, failedAttempts, newConnection);
                throw se2 == null ? se : se2;
            }
            if (errorid.length() <= 0) throw se2 == null ? se : se2;
            SecurityService.generateUserAuthenticationEvent(sapphireConnection, sysuserid, errorid, se.getMessage(), newConnection);
            throw se2 == null ? se : se2;
        }
        finally {
            db.reset();
        }
    }

    private static String translateText(SapphireConnection sapphireConnection, String text) throws SapphireException, ServiceException {
        String translatedText = "";
        if (sapphireConnection.getConnectionId() == null || "".equals(sapphireConnection.getConnectionId())) {
            String language = "";
            DBUtil db = new DBUtil();
            db.setConnection(sapphireConnection);
            String qry = "SELECT languageid FROM sysuser WHERE sysuserid = ?";
            db.createPreparedResultSet("userlanguage", qry.toString(), new Object[]{sapphireConnection.getSysuserId()});
            DataSet userLangDS = new DataSet(db.getResultSet("userlanguage"));
            db.reset();
            language = userLangDS.getString(0, "languageid", "");
            if ("".equals(language)) {
                ConfigService cs = new ConfigService(sapphireConnection);
                language = cs.getProfileProperty("(system)", "LANGUAGE", "");
            }
            if (!"".equals(language)) {
                I18NService i18NService = new I18NService(sapphireConnection);
                PropertyList sourceText = new PropertyList();
                sourceText.setProperty(text, "");
                translatedText = i18NService.translateTable(language, sourceText).getProperty(text);
                if ("".equals(translatedText)) {
                    translatedText = text;
                }
            } else {
                translatedText = text;
            }
        } else {
            TranslationProcessor tp = new TranslationProcessor(sapphireConnection.getConnectionId());
            translatedText = tp.translate(text);
        }
        return translatedText;
    }

    private static void generateUserAuthenticationEvent(SapphireConnection sapphireConnection, String sysuserid, String errorid, String errormessage, boolean newConnection) {
        try {
            String databaseid = sapphireConnection.getDatabaseId();
            ConnectionProcessor cp = new ConnectionProcessor();
            String connectionid = cp.getConnectionid(databaseid, "(system)", systemPassword);
            EventManager.generateEvent(cp.getSapphireConnection(), null, new UserAuthenticationEventObject(sysuserid, databaseid, newConnection ? "logon" : "esig", errorid, errormessage));
            new SecurityService(sapphireConnection).clearConnection(connectionid, false);
        }
        catch (Exception e) {
            Trace.logError(LOGNAME, (Object)("Failed to trace logon activity for user '" + sysuserid + "'"), e);
        }
    }

    private static void logUserActivity(SapphireConnection sapphireConnection, String sysuserid, String logActivityProperty, PropertyList sysUserUpdateProps, String disableReason, int logonAttempts, int failedAttempts, boolean newConnection) {
        try {
            String databaseid = sapphireConnection.getDatabaseId();
            ConnectionProcessor cp = new ConnectionProcessor();
            String connectionid = cp.getConnectionid(databaseid, "(system)", systemPassword);
            ActionProcessor ap = new ActionProcessor(connectionid);
            EventManager.generateEvent(cp.getSapphireConnection(), null, new UserAuthenticationEventObject(sysuserid, databaseid, logActivityProperty.equals("log_setuser") && newConnection ? "logon" : "esig", disableReason, failedAttempts));
            ActionBlock ab = new ActionBlock();
            HashMap<String, String> addTraceLogProps = new HashMap<String, String>();
            addTraceLogProps.put("description", "'" + sysuserid + "' " + (logActivityProperty.equals("log_setuser") ? (newConnection ? "logon" : "esig") + " success" : "failed " + (newConnection ? "logon" : "esig") + " attempt"));
            addTraceLogProps.put("activitypropertyid", logActivityProperty);
            addTraceLogProps.put("activity", "SetUser");
            ab.setActionClass("AddActivityTraceLog", AddActivityTraceLog.class.getName(), addTraceLogProps);
            if (sysUserUpdateProps != null) {
                ab.setActionClass("EditSDI", EditSDI.class.getName(), sysUserUpdateProps);
            }
            ap.processActionBlock(ab, logActivityProperty.equals("log_setuserfail"));
            if (disableReason.equals("Max login attempts exceeded")) {
                HashMap<String, String> mailProps = new HashMap<String, String>();
                mailProps.put("to", ConfigService.getConfigProperty("com.labvantage.sapphire.server.securityemailtoaddress"));
                mailProps.put("subject", "Sapphire Security Violation");
                mailProps.put("message", "User '" + sysuserid + "' has failed on " + logonAttempts + " attempts to login - the account has been disabled.");
                ap.processActionClass(SendMail.class.getName(), mailProps, true);
            }
            new SecurityService(sapphireConnection).clearConnection(connectionid, false);
        }
        catch (Exception e) {
            Trace.logError(LOGNAME, (Object)("Failed to trace logon activity for user '" + sysuserid + "'"), e);
        }
    }

    private static synchronized String getNextConnectionId(DBUtil db, String databaseid, String username, String usertype) throws SapphireException {
        boolean isVirtualUser = usertype.equals("V") || usertype.equals("I");
        boolean isPortalUser = usertype.equals("P") || usertype.equals("Q");
        String connectionid = databaseid + "|" + (isVirtualUser ? VIRTUALUSER_MARKER : (isPortalUser ? PORTALUSER_MARKER : "")) + username + "-" + String.valueOf(Math.round(Math.random() * 1.0E7 * 1000000.0));
        boolean encrypt = SecurityPolicyUtil.isConnectionidEncryptionEnabled(databaseid, isVirtualUser, isPortalUser);
        if (encrypt) {
            connectionid = ENCRYPTEDCONNECTIONID_MARKER + EncryptDecrypt.encrypt(connectionid);
        }
        while (db.checkPreparedExists("SELECT connectionid FROM connectionlog WHERE connectionid = ?", new Object[]{connectionid})) {
            connectionid = databaseid + "|" + (isVirtualUser ? VIRTUALUSER_MARKER : (isPortalUser ? PORTALUSER_MARKER : "")) + username + "-" + String.valueOf(Math.round(Math.random() * 1.0E7 * 1000000.0));
            if (!encrypt) continue;
            connectionid = ENCRYPTEDCONNECTIONID_MARKER + EncryptDecrypt.encrypt(connectionid);
        }
        return connectionid;
    }

    private static synchronized boolean isUpdateConnectionCacheDue() {
        long interval = System.currentTimeMillis() - lastUpdateMillis;
        if (interval > 60000L) {
            if (interval > 300000L) {
                updatePending = false;
                Trace.log("set updatePending to false as last updated 5 minutes ago");
            }
            return true;
        }
        return false;
    }

    public void disableUser(String sysuserid, String disableReason) throws ServiceException {
        this.logInfo("Disabling sysuserid '" + sysuserid + "'");
        try {
            ActionService actionService = new ActionService(this.sapphireConnection);
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "User");
            props.setProperty("keyid1", sysuserid);
            props.setProperty("disabledflag", "Y");
            props.setProperty("disabledreason", disableReason != null && disableReason.length() > 0 ? disableReason : "No reason given for disable");
            props.setProperty("tracelogid", "");
            actionService.processAction("EditSDI", "1", props);
        }
        catch (ServiceException e) {
            throw new ServiceException("SETUSERSTATUS_FAILURE", "Failed to disable user " + sysuserid + ". Exception: " + e.getMessage(), e);
        }
    }

    public void enableUser(String sysuserid) throws ServiceException {
        block10: {
            this.logInfo("Enabling sysuserid '" + sysuserid + "'");
            DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
            try {
                db.setConnection(this.sapphireConnection);
                db.createPreparedResultSet("SELECT nameduserflag FROM sysuser WHERE sysuserid = ?", sysuserid);
                if (db.getNext()) {
                    boolean enable = true;
                    String nameduserflag = db.getString("nameduserflag");
                    if (nameduserflag.equals("S") || nameduserflag.equals("V") || nameduserflag.equals("P")) {
                        Configuration config = Configuration.getInstance();
                        License license = config.getLicense(this.sapphireConnection.getDatabaseId());
                        db.createPreparedResultSet("SELECT count(*) \"count\" FROM sysuser WHERE nameduserflag = ? AND ( disabledflag is null or disabledflag = 'N' )", nameduserflag);
                        if (db.getNext() && db.getInt("count") >= (nameduserflag.equals("S") ? license.getNamedUsers() + 1 : (nameduserflag.equals("P") ? license.getPortalNamedUsers() + 1 : license.getVirtualUsers() + 1))) {
                            throw new ServiceException("SETUSERSTATUS_FAILURE", "Failed to enable user '" + sysuserid + "' because the max " + (nameduserflag.equals("S") ? "standard" : (nameduserflag.equals("P") ? "portal" : "virtual")) + " named users has been met");
                        }
                    }
                    if (!enable) break block10;
                    ActionService actionService = new ActionService(this.sapphireConnection);
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", "User");
                    props.setProperty("keyid1", sysuserid);
                    props.setProperty("disabledflag", "N");
                    props.setProperty("failedattempts", "0");
                    props.setProperty("disabledreason", "");
                    props.setProperty("cooloffenddt", "");
                    props.setProperty("tracelogid", "");
                    try {
                        actionService.processAction("EditSDI", "1", props);
                        break block10;
                    }
                    catch (ServiceException e) {
                        throw new ServiceException("SETUSERSTATUS_FAILURE", "Failed to enable user " + sysuserid + ". Failed to update sysuser record");
                    }
                }
                throw new ServiceException("SETUSERSTATUS_FAILURE", "Failed to find user '" + sysuserid + "'");
            }
            catch (SapphireException e) {
                throw new ServiceException("SETUSERSTATUS_FAILURE", "Failed to enable user " + sysuserid + ". Exception: " + e.getMessage(), e);
            }
            finally {
                db.reset();
            }
        }
    }

    public void forcePasswordChange(String sysuserid) throws ServiceException {
        this.logInfo("Forcing password change for sysuserid '" + sysuserid + "'");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            if (db.executePreparedUpdate("UPDATE sysuser SET tracelogid = null, changepasswordflag = 'Y' WHERE sysuserid = ?", new Object[]{sysuserid}) != 1) {
                throw new ServiceException("SETUSERSTATUS_FAILURE", "Failed to set changepasswordflag for user " + sysuserid + ". Failed to update sysuser record");
            }
        }
        catch (SapphireException e) {
            throw new ServiceException("SETUSERSTATUS_FAILURE", "Failed to set changepasswordflag user " + sysuserid + ". Exception: " + e.getMessage(), e);
        }
        finally {
            db.reset();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void updateConnections() throws ServiceException {
        this.logDebug("Updating cached Sapphire connections");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            ArrayList<SapphireConnection> updatelist = new ArrayList<SapphireConnection>();
            Map<String, Object> map = SecurityService.sapphireConnectionCache.cache;
            synchronized (map) {
                for (String connectionid : sapphireConnectionCache.keySet()) {
                    SapphireConnection sapphireConnection = (SapphireConnection)sapphireConnectionCache.get(connectionid);
                    if (sapphireConnection == null || !sapphireConnection.getDatabaseId().equals(this.sapphireConnection.getDatabaseId()) || sapphireConnection.getLastAccessedDt() == null || sapphireConnection.getLastAccessedDt().getTime().getTime() <= lastUpdateMillis) continue;
                    updatelist.add(sapphireConnection);
                }
            }
            StringBuilder connectionidlist = new StringBuilder();
            StringBuilder lastaccesseddtlist = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
            String callstmt = "{call lv_rset" + (db.isOracle() ? "." : "_") + "ConnPingList( ?, ?, ? ) }";
            CallableStatement cs = db.prepareCall(callstmt);
            for (int i = 0; i < updatelist.size(); ++i) {
                SapphireConnection sapphireConnection = (SapphireConnection)updatelist.get(i);
                connectionidlist.append(";" + sapphireConnection.getConnectionId());
                lastaccesseddtlist.append(";" + sdf.format(sapphireConnection.getLastAccessedDt().getTime()));
                if ((i == 0 || i % 50 != 0) && i != updatelist.size() - 1 || connectionidlist.length() <= 1) continue;
                cs.setString(1, connectionidlist.substring(1));
                cs.setString(2, lastaccesseddtlist.substring(1));
                if (db.isOracle()) {
                    cs.setString(3, "yyyy/mm/dd hh24:mi:ss");
                } else {
                    cs.setInt(3, 120);
                }
                cs.executeUpdate();
                connectionidlist = new StringBuilder();
                lastaccesseddtlist = new StringBuilder();
            }
            lastUpdateMillis = System.currentTimeMillis();
        }
        catch (Exception e) {
            throw new ServiceException("DB_ACTION_FAILED", "Failed to update connection table from cache. Reason: " + e.getMessage(), e);
        }
        finally {
            updatePending = false;
            db.reset();
        }
    }

    public void timeoutConnections() throws ServiceException {
        this.logDebug("Timing out Sapphire connections");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            db.createResultSet("SELECT connectionid FROM connection WHERE deletedt IS NOT NULL");
            while (db.getNext()) {
                String connectionid = db.getString("connectionid");
                this.clearConnection(connectionid, true);
            }
        }
        catch (Exception e) {
            throw new ServiceException("DB_ACTION_FAILED", "Failed to update connection table from cache", e);
        }
        finally {
            db.reset();
        }
    }

    public void prepareTimeoutConnections(int timeouttime) throws ServiceException {
        block17: {
            this.logInfo("Preparing timeouts for Sapphire connections");
            DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
            try {
                db.setConnection(this.sapphireConnection);
                if (timeouttime <= 0) break block17;
                HashSet<String> logOffNotify = new HashSet<String>();
                String sql = "SELECT connectionid FROM connection WHERE lastaccesseddt < ? and tool <> 'Internal' AND NullIf (authtokenid,'') is null";
                db.createPreparedResultSet("connections1", sql.toString(), new DateTimeUtil().getTimestamp("now-" + timeouttime + "s"));
                DataSet connectionDS = new DataSet(db.getResultSet("connections1"));
                if (OpalUtil.isNotEmpty(connectionDS)) {
                    this.logDebug("Found " + connectionDS.getRowCount() + " connections ready to be timedout.");
                    TDQManagerLocal tdqManager = ServiceLocator.getInstance().getTDQManager();
                    for (int i = 0; i < connectionDS.getRowCount(); ++i) {
                        String connectionid = connectionDS.getString(i, "connectionid");
                        try {
                            tdqManager.timeoutConnection(this.sapphireConnection.getConnectionId(), connectionid);
                            SapphireConnection sapphireConnection = SecurityService.getSapphireConnection(db, connectionid, false);
                            if (sapphireConnection == null) continue;
                            logOffNotify.add(connectionid);
                            try {
                                NotifyManager.notifyLogoff(sapphireConnection, true);
                                this.logDebug("Notified connection for delete: " + connectionid);
                            }
                            catch (Throwable th) {
                                this.logError("Failed to Notify connection for delete: " + connectionid, th);
                            }
                            continue;
                        }
                        catch (Exception ee) {
                            this.logWarn("Failed deleting connection " + connectionid + ":" + ee.getMessage());
                        }
                    }
                }
                ConfigService configService = new ConfigService(this.sapphireConnection);
                int polltime = 60;
                try {
                    polltime = Integer.parseInt(configService.getSysConfigProperty("timeoutpoll"));
                }
                catch (Exception e) {
                    this.logDebug("Couldn't determine Timeout Poll override. Using Default Timeout: " + polltime);
                }
                db.createPreparedResultSet("connections2", sql.toString(), new DateTimeUtil().getTimestamp("now-" + (timeouttime - polltime) + "s"));
                DataSet connectionDataset2 = new DataSet(db.getResultSet("connections2"));
                if (OpalUtil.isNotEmpty(connectionDataset2)) {
                    this.logDebug("Found " + connectionDataset2.getRowCount() + " connections which are up for timeout by the next poll.");
                    for (int i = 0; i < connectionDataset2.getRowCount(); ++i) {
                        String connectionid = connectionDataset2.getString(i, "connectionid");
                        this.logDebug("Preparing connection '" + connectionid + "' for notify");
                        SapphireConnection sapphireConnection = SecurityService.getSapphireConnection(db, connectionid, false);
                        if (sapphireConnection == null || logOffNotify.contains(connectionid)) continue;
                        try {
                            NotifyManager.notifyLogoff(sapphireConnection, false);
                            this.logDebug("Notified connection for 'about to logoff': " + connectionid);
                            continue;
                        }
                        catch (Throwable th) {
                            this.logError("Failed to Notify connection for 'about to logoff': " + connectionid, th);
                        }
                    }
                }
            }
            catch (Exception e) {
                throw new ServiceException("DB_ACTION_FAILED", "Failed to timeout connections for database '" + this.connectionInfo.getDatabaseId() + "'", e);
            }
            finally {
                db.reset();
            }
        }
    }

    public static void updateConnectionLog(String connectionid, PropertyList connectionLogProps) {
        QueryProcessor qp = new QueryProcessor(connectionid);
        String updateCols = "";
        SafeSQL safeSQL = new SafeSQL();
        for (String columnid : connectionLogProps.keySet()) {
            updateCols = updateCols + (updateCols.length() > 0 ? "," : "") + columnid + "=" + safeSQL.addVar(connectionLogProps.getProperty(columnid, ""));
        }
        String sql = "UPDATE connectionlog SET " + updateCols + " WHERE connectionid=" + safeSQL.addVar(connectionid);
        qp.execPreparedUpdate(sql, safeSQL.getValues());
    }

    public static void clearConnectionFromCache(String connectionid) {
        if (sapphireConnectionCache.get(connectionid) != null) {
            sapphireConnectionCache.remove(connectionid);
        }
    }

    static {
        lastUpdateMillis = 0L;
        updatePending = false;
        executor = null;
    }
}

