/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.system;

import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.admin.system.AutomationProcessor;
import com.labvantage.sapphire.admin.system.automation.LAMException;
import com.labvantage.sapphire.modules.eventmanager.EventManager;
import com.labvantage.sapphire.modules.eventmanager.NotifyManager;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.services.ConfigurationConstants;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.services.StatusService;
import com.labvantage.sapphire.util.cache.CacheNames;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.logger.LogConfig;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.util.Browser;
import sapphire.xml.PropertyList;

public class SysToolsPropertyHandler
extends PropertyHandler
implements ConfigurationConstants,
CacheNames {
    public static final String MODE = "systoolsmode";
    public static final String MODE_SYSCONFIG_SAVE = "sysconfigsave";
    public static final String MODE_USERPROFILE_SAVE = "userprofilesave";
    public static final String MODE_SYSCONFIG_RESET = "sysconfigreset";
    public static final String MODE_STATS_RESET = "statsreset";
    public static final String MODE_RESET_CACHE = "resetcache";
    public static final String MODE_RESET_STATS = "resetstats";
    public static final String MODE_RESET_AUTOMATION = "resetautomation";
    public static final String MODE_RUN_HOUSEKEEPING = "runhousekeeping";
    public static final String PROFILEPROPERTY_USERID = "profileuserid";
    public static final String BOPASSWORD_CHANGED = "passwordChanged";

    @Override
    public void processProperties(HashMap props) throws SapphireException {
        String submittedpd;
        boolean passwordChanged;
        String mode = (String)props.get(MODE);
        String pwdchgnd = (String)props.get(BOPASSWORD_CHANGED);
        boolean bl = passwordChanged = pwdchgnd != null ? pwdchgnd.equals("yes") : false;
        if (mode == null || mode.length() == 0) {
            mode = (String)props.get("dothis");
        }
        if ((submittedpd = (String)props.get("bopassword")) != null && submittedpd.indexOf("{|}") == 0) {
            props.put("bopassword", EncryptDecrypt.decryptRSA(submittedpd.substring(3)));
        }
        try {
            ConfigService config = new ConfigService(this.sapphireConnection);
            if (MODE_SYSCONFIG_SAVE.equalsIgnoreCase(mode)) {
                config.setSysConfigProperty("aappoolsize", ((String)props.get("aappoolsize")).trim());
                config.setSysConfigProperty("processservercommand", ((String)props.get("processservercommand")).trim());
                config.setSysConfigProperty("todolistpoll", ((String)props.get("todolistpoll")).trim());
                config.setSysConfigProperty("todolistbuffermillis", ((String)props.get("todolistbuffermillis")).trim());
                config.setSysConfigProperty("todolistlogging", ((String)props.get("todolistlogging")).trim());
                config.setSysConfigProperty("taskpoll", ((String)props.get("taskpoll")).trim());
                config.setSysConfigProperty("schedulepoll", ((String)props.get("schedulepoll")).trim());
                config.setSysConfigProperty("timeoutpoll", ((String)props.get("timeoutpoll")).trim());
                config.setSysConfigProperty("connectiontimeout", ((String)props.get("connectiontimeout")).trim());
                config.setSysConfigProperty("rsettimeout", ((String)props.get("rsettimeout")).trim());
                config.setSysConfigProperty("RSetQueryLimit", ((String)props.get("RSetQueryLimit")).trim());
                config.setSysConfigProperty("RSetAllowQuotes", ((String)props.get("RSetAllowQuotes")).trim());
                config.setSysConfigProperty("RSetLogLevel", ((String)props.get("RSetLogLevel")).trim());
                config.setSysConfigProperty("SQLTimeout", ((String)props.get("SQLTimeout")).trim());
                config.setSysConfigProperty("enableexecsql", props.get("enableexecsql") == null || !props.get("enableexecsql").equals("on") ? "N" : "Y");
                config.setSysConfigProperty("checksumprocessing", ((String)props.get("checksumprocessing")).trim());
                config.setSysConfigProperty("importprocessing", ((String)props.get("importprocessing")).trim());
                config.setSysConfigProperty("importdir", ((String)props.get("importdir")).trim());
                config.setSysConfigProperty("exportdir", ((String)props.get("exportdir")).trim());
                config.setSysConfigProperty("hkwebpagehistory", ((String)props.get("hkwebpagehistory")).trim());
                config.setSysConfigProperty("hkconnectionlog", ((String)props.get("hkconnectionlog")).trim());
                config.setSysConfigProperty("hkdeptassignment", ((String)props.get("hkdeptassignment")).trim());
                config.setSysConfigProperty("hkbulletins", ((String)props.get("hkbulletins")).trim());
                config.setSysConfigProperty("defaultpassword", ((String)props.get("defaultpassword")).trim());
                config.setSysConfigProperty("RSetEmbedSecurity", (String)props.get("RSetEmbedSecurity"));
                config.setSysConfigProperty("serverheartbeat", (String)props.get("serverheartbeat"));
                config.setSysConfigProperty("serverlatency", (String)props.get("serverlatency"));
                config.setSysConfigProperty("fileuploadmaxsize", ((String)props.get("fileuploadmaxsize")).trim());
                config.setSysConfigProperty("filedownloadmaxsize", ((String)props.get("filedownloadmaxsize")).trim());
                config.setSysConfigProperty("fileuploaddfdmaxsize", ((String)props.get("fileuploaddfdmaxsize")).trim());
                config.setSysConfigProperty("disablethumbnails", props.get("disablethumbnails") == null || !props.get("disablethumbnails").toString().equalsIgnoreCase("Y") ? "N" : "Y");
                config.setSysConfigProperty("dynamicclasslibrary", ((String)props.get("dynamicclasslibrary")).trim());
                config.setSysConfigProperty("smtphost", ((String)props.get("smtphost")).trim());
                config.setSysConfigProperty("emailfromaddress", ((String)props.get("emailfromaddress")).trim());
                config.setSysConfigProperty("securityemailtoaddress", ((String)props.get("securityemailtoaddress")).trim());
                config.setSysConfigProperty("mailserveradditionalprops", ((String)props.get("mailserveradditionalprops")).trim());
                config.setSysConfigProperty("mailserverusernam", ((String)props.get("mailserverusernam")).trim());
                String sysmailpassowrd = ((String)props.get("mailserverpassword")).trim();
                if (!sysmailpassowrd.equalsIgnoreCase("(storedpassword)")) {
                    if (sysmailpassowrd.length() > 0) {
                        sysmailpassowrd = EncryptDecrypt.encrypt(sysmailpassowrd, this.sapphireConnection.getDatabaseId());
                    }
                    config.setSysConfigProperty("mailserverpassword", sysmailpassowrd);
                }
                config.setProfileProperty("(system)", "language", ((String)props.get("language")).trim());
                config.setProfileProperty("(system)", "mfaoption", ((String)props.get("mfaoption")).trim());
                config.setProfileProperty("(system)", "mfaprovider", ((String)props.get("mfaprovider")).trim());
                config.setProfileProperty("(system)", "mfaportaloption", ((String)props.get("mfaportaloption")).trim());
                config.setProfileProperty("(system)", "mfaportalprovider", ((String)props.get("mfaportalprovider")).trim());
                config.setProfileProperty("(system)", "logonattempts", ((String)props.get("logonattempts")).trim());
                config.setProfileProperty("(system)", "additionallogonattempts", ((String)props.get("additionallogonattempts")).trim());
                config.setProfileProperty("(system)", "maxcooldown", ((String)props.get("maxcooldown")).trim());
                config.setProfileProperty("(system)", "log_setuser", ((String)props.get("log_setuser")).trim());
                config.setProfileProperty("(system)", "log_setuserfail", ((String)props.get("log_setuserfail")).trim());
                config.setProfileProperty("(system)", "disablelegacysessiontracking", ((String)props.get("disablelegacysessiontracking")).trim());
                config.setProfileProperty("(system)", "PasswordExpiryWarningDays", ((String)props.get("PasswordExpiryWarningDays")).trim());
                config.setProfileProperty("(system)", "PasswordExpiryDays", ((String)props.get("PasswordExpiryDays")).trim());
                config.setProfileProperty("(system)", "PasswordResetMins", ((String)props.get("PasswordResetMins")).trim());
                config.setProfileProperty("(system)", "DisableUserOnPasswordExpiry", ((String)props.get("DisableUserOnPasswordExpiry")).trim());
                config.setProfileProperty("(system)", "lockactions", ((String)props.get("lockactions")).trim());
                config.setProfileProperty("(system)", "protocolproductsdcid", ((String)props.get("protocolproductsdcid")).trim());
                config.setProfileProperty("(system)", "studyproductcolumnid", ((String)props.get("studyproductcolumnid")).trim());
                config.setProfileProperty("(system)", "executeahead", ((String)props.get("executeahead")).trim());
                config.setProfileProperty("(system)", "executeaheadunits", ((String)props.get("executeaheadunits")).trim());
                config.setProfileProperty("(system)", "customrulesjavapackage", ((String)props.get("customrulesjavapackage")).trim());
                config.setProfileProperty("(system)", "bouniverse", ((String)props.get("bouniverse")).trim());
                config.setProfileProperty("(system)", "bodocumentdomain", ((String)props.get("bodocumentdomain")).trim());
                config.setProfileProperty("(system)", "bodomain", ((String)props.get("bodomain")).trim());
                config.setProfileProperty("(system)", "boexchangemode", ((String)props.get("boexchangemode")).trim());
                String password = ((String)props.get("bopassword")).trim();
                if (passwordChanged && !password.equalsIgnoreCase("(storedpassword)")) {
                    if (password.length() > 0) {
                        password = EncryptDecrypt.encrypt(password, this.sapphireConnection.getDatabaseId());
                    }
                    config.setProfileProperty("(system)", "bopassword", password);
                }
                config.setProfileProperty("(system)", "bourl", ((String)props.get("bourl")).trim());
                config.setProfileProperty("(system)", "bocmsname", ((String)props.get("bocmsname")).trim());
                config.setProfileProperty("(system)", "bousername", ((String)props.get("bousername")).trim());
                config.setProfileProperty("(system)", "boauthenticationtype", ((String)props.get("boauthenticationtype")).trim());
                config.setProfileProperty("(system)", "borootfoldername", ((String)props.get("borootfoldername")).trim());
                config.setProfileProperty("(system)", "boconnectiontimeout", ((String)props.get("boconnectiontimeout")).trim());
                config.setProfileProperty("(system)", "bocrystaldateformat", ((String)props.get("bocrystaldateformat")).trim());
                config.setProfileProperty("(system)", "bodeskidateformat", ((String)props.get("bodeskidateformat")).trim());
                config.setProfileProperty("(system)", "bowebidateformat", ((String)props.get("bowebidateformat")).trim());
                config.setProfileProperty("(system)", "masterupdate", ((String)props.get("masterupdate")).trim());
                config.setProfileProperty("(system)", "showtranslations", ((String)props.get("showtranslations")).trim());
                config.setProfileProperty("(system)", "notificationformat", ((String)props.get("notificationformat")).trim());
                config.setProfileProperty("(system)", "sqlregisterclass", ((String)props.get("sqlregisterclass")).trim());
                config.setProfileProperty("(system)", "disableesig", ((String)props.get("disableesig")).trim());
                boolean startlogBackupNeeded = false;
                File tempFile = null;
                if (LogConfig.startuplogFileSize() != 0L && props.get("startuplog").equals("Y")) {
                    startlogBackupNeeded = true;
                }
                if (startlogBackupNeeded) {
                    tempFile = LogConfig.backupStartupLog();
                }
                LogConfig.generateConfigFile(new PropertyList(props));
                if (tempFile != null && startlogBackupNeeded) {
                    LogConfig.restoreStartupLog(tempFile);
                }
                ConfigService.setConfigProperty("com.labvantage.sapphire.server.obfuscatesql", props.get("com.labvantage.sapphire.server.obfuscatesql") == null ? "O" : (String)props.get("com.labvantage.sapphire.server.obfuscatesql"));
                ConfigService.setConfigProperty("com.labvantage.sapphire.server.filter.rejectrequest.sqllogging", props.get("com.labvantage.sapphire.server.filter.rejectrequest.sqllogging") == null ? "N" : (String)props.get("com.labvantage.sapphire.server.filter.rejectrequest.sqllogging"));
                ConfigService.setConfigProperty("com.labvantage.sapphire.server.stats", props.get("com.labvantage.sapphire.server.stats") == null ? "N" : (String)props.get("com.labvantage.sapphire.server.stats"));
                ConfigService.setConfigProperty("com.labvantage.sapphire.server.tracestats", props.get("com.labvantage.sapphire.server.tracestats") == null ? "N" : (String)props.get("com.labvantage.sapphire.server.tracestats"));
                ConfigService.setConfigProperty("com.labvantage.sapphire.server.smtphost", ((String)props.get("com.labvantage.sapphire.server.smtphost")).trim());
                ConfigService.setConfigProperty("com.labvantage.sapphire.server.mailserveradditionalprops", ((String)props.get("com.labvantage.sapphire.server.mailserveradditionalprops")).trim());
                ConfigService.setConfigProperty("com.labvantage.sapphire.server.mailserverusernam", ((String)props.get("com.labvantage.sapphire.server.mailserverusernam")).trim());
                String mailpassowrd = ((String)props.get("com.labvantage.sapphire.server.mailserverpassword")).trim();
                if (!mailpassowrd.equalsIgnoreCase("(storedpassword)")) {
                    if (mailpassowrd.length() > 0) {
                        mailpassowrd = EncryptDecrypt.encrypt(mailpassowrd, this.sapphireConnection.getDatabaseId());
                    }
                    ConfigService.setConfigProperty("com.labvantage.sapphire.server.mailserverpassword", mailpassowrd);
                }
                ConfigService.setConfigProperty("com.labvantage.sapphire.server.snapshotfolder", ((String)props.get("com.labvantage.sapphire.server.snapshotfolder")).trim());
                ConfigService.setConfigProperty("com.labvantage.sapphire.server.emailfromaddress", ((String)props.get("com.labvantage.sapphire.server.emailfromaddress")).trim());
                ConfigService.setConfigProperty("com.labvantage.sapphire.server.securityemailtoaddress", ((String)props.get("com.labvantage.sapphire.server.securityemailtoaddress")).trim());
                ConfigService.setConfigProperty("com.labvantage.sapphire.server.webappbaseurl", ((String)props.get("com.labvantage.sapphire.server.webappbaseurl")).trim());
                config.setSysConfigProperty("signingmode", (String)props.get("signingmode"));
                config.setSysConfigProperty("signingprovider", (String)props.get("signingprovider"));
                config.setSysConfigProperty("signingprovidernode", (String)props.get("signingprovidernode"));
                config.setProfileProperty("(system)", "menuanimation", (String)props.get("menuanimation"));
                config.setProfileProperty("(system)", "fadeanimation", (String)props.get("fadeanimation"));
                config.setProfileProperty("(system)", "resizeanimation", (String)props.get("resizeanimation"));
                ConfigService.resetCache();
                CacheUtil.removeAllStartWith(this.sapphireConnection.getDatabaseId(), "ProfileProperties", "(system);");
                CacheUtil.clear(this.sapphireConnection.getDatabaseId(), "SysConfigProperties");
            } else if (MODE_SYSCONFIG_RESET.equalsIgnoreCase(mode)) {
                config.setSysConfigProperty("aappoolsize", String.valueOf(5));
                config.setSysConfigProperty("processservercommand", String.valueOf(3));
                config.setSysConfigProperty("todolistpoll", String.valueOf(1));
                config.setSysConfigProperty("todolistbuffermillis", String.valueOf(100));
                config.setSysConfigProperty("todolistlogging", "N");
                config.setSysConfigProperty("taskpoll", String.valueOf(60));
                config.setSysConfigProperty("schedulepoll", String.valueOf(3600));
                config.setSysConfigProperty("timeoutpoll", String.valueOf(60));
                config.setSysConfigProperty("connectiontimeout", String.valueOf(3600));
                config.setSysConfigProperty("rsettimeout", String.valueOf(60));
                config.setSysConfigProperty("RSetQueryLimit", "");
                config.setSysConfigProperty("RSetAllowQuotes", "N");
                config.setSysConfigProperty("RSetLogLevel", "0");
                config.setSysConfigProperty("SQLTimeout", "300");
                config.setSysConfigProperty("enableexecsql", "N");
                config.setSysConfigProperty("checksumprocessing", "I");
                config.setSysConfigProperty("importprocessing", "F");
                config.setSysConfigProperty("importdir", "");
                config.setSysConfigProperty("exportdir", "");
                config.setSysConfigProperty("hkwebpagehistory", "30");
                config.setSysConfigProperty("hkconnectionlog", "180");
                config.setSysConfigProperty("hkdeptassignment", "30");
                config.setSysConfigProperty("hkbulletins", "0");
                config.setSysConfigProperty("defaultpassword", "");
                config.setSysConfigProperty("RSetEmbedSecurity", "N");
                config.setSysConfigProperty("serverheartbeat", "10");
                config.setSysConfigProperty("serverlatency", "30");
                config.setSysConfigProperty("fileuploadmaxsize", "");
                config.setSysConfigProperty("filedownloadmaxsize", "");
                config.setSysConfigProperty("disablethumbnails", "N");
                config.setSysConfigProperty("dynamicclasslibrary", "[applicationhome]/dynamicclasslibraries/");
                config.setSysConfigProperty("signingmode", "");
                config.setSysConfigProperty("signingprovider", "");
                config.setSysConfigProperty("signingprovidernode", "");
                LogConfig.generateConfigFile(new PropertyList());
                ConfigService.setConfigProperty("com.labvantage.sapphire.server.obfuscatesql", "O");
                ConfigService.setConfigProperty("com.labvantage.sapphire.server.filter.rejectrequest.sqllogging", "N");
                ConfigService.setConfigProperty("com.labvantage.sapphire.server.stats", "N");
                ConfigService.setConfigProperty("com.labvantage.sapphire.server.tracestats", "N");
                ConfigService.setConfigProperty("com.labvantage.sapphire.server.smtphost", "");
                ConfigService.setConfigProperty("com.labvantage.sapphire.server.mailserveradditionalprops", "");
                ConfigService.setConfigProperty("com.labvantage.sapphire.server.mailserverusernam", "");
                ConfigService.setConfigProperty("com.labvantage.sapphire.server.mailserverpassword", "");
                ConfigService.setConfigProperty("com.labvantage.sapphire.server.emailfromaddress", "");
                ConfigService.setConfigProperty("com.labvantage.sapphire.server.securityemailtoaddress", "");
                ConfigService.setConfigProperty("com.labvantage.sapphire.server.webappbaseurl", "");
                config.setProfileProperty("(system)", "logonpageurl", "rc?command=page&page=SystemAdminTramline");
                config.setProfileProperty("(system)", "logonmenu", "SystemAdminMenu");
                config.setProfileProperty("(system)", "logongroup", "");
                ArrayList<Browser.GUIMode> guiModes = Browser.getGUIModes(this.sapphireConnection, this.sapphireConnection.getDatabaseId());
                for (int g = 0; g < guiModes.size(); ++g) {
                    String id = guiModes.get(g).getId();
                    config.setProfileProperty("(system)", id + "logonpageurl", "");
                    config.setProfileProperty("(system)", id + "logonmenu", "");
                    config.setProfileProperty("(system)", id + "logongroup", "");
                }
                config.setProfileProperty("(system)", "language", "");
                config.setProfileProperty("(system)", "log_setuser", "N");
                config.setProfileProperty("(system)", "log_setuserfail", "N");
                config.setProfileProperty("(system)", "disablelegacysessiontracking", "Y");
                config.setProfileProperty("(system)", "PasswordExpiryWarningDays", "");
                config.setProfileProperty("(system)", "PasswordExpiryDays", "");
                config.setProfileProperty("(system)", "PasswordResetMins", "");
                config.setProfileProperty("(system)", "DisableUserOnPasswordExpiry", "Y");
                config.setProfileProperty("(system)", "executeahead", "");
                config.setProfileProperty("(system)", "executeaheadunits", "");
                config.setProfileProperty("(system)", "studyproductcolumnid", "");
                config.setProfileProperty("(system)", "protocolproductsdcid", "");
                config.setProfileProperty("(system)", "lockactions", "N");
                config.setProfileProperty("(system)", "customrulesjavapackage", "");
                config.setProfileProperty("(system)", "bodocumentdomain", "");
                config.setProfileProperty("(system)", "bodomain", "");
                config.setProfileProperty("(system)", "boexchangemode", "");
                config.setProfileProperty("(system)", "bopassword", "");
                config.setProfileProperty("(system)", "bouniverse", "");
                config.setProfileProperty("(system)", "bourl", "http://<boserver>:<port>");
                config.setProfileProperty("(system)", "bocmsname", "");
                config.setProfileProperty("(system)", "bousername", "Administrator");
                config.setProfileProperty("(system)", "boauthenticationtype", "Enterprise");
                config.setProfileProperty("(system)", "borootfoldername", "Root Folder");
                config.setProfileProperty("(system)", "boconnectiontimeout", "");
                config.setProfileProperty("(system)", "bocrystaldateformat", "MM/dd/yyyy H:m:ss a");
                config.setProfileProperty("(system)", "bodeskidateformat", "MM-dd-yyyy H:m:s");
                config.setProfileProperty("(system)", "bowebidateformat", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                config.setProfileProperty("(system)", "masterupdate", "N");
                config.setProfileProperty("(system)", "showtranslations", "N");
                config.setProfileProperty("(system)", "sqlregisterclass", "");
                config.setProfileProperty("(system)", "menuanimation", "Y");
                config.setProfileProperty("(system)", "fadeanimation", "Y");
                config.setProfileProperty("(system)", "resizeanimation", "Y");
                config.setProfileProperty("(system)", "notificationformat", "Bulletin");
                CacheUtil.removeAllStartWith(this.sapphireConnection.getDatabaseId(), "ProfileProperties", "(system);");
                CacheUtil.clear(this.sapphireConnection.getDatabaseId(), "SysConfigProperties");
            } else if (MODE_STATS_RESET.equalsIgnoreCase(mode)) {
                StatusService status = new StatusService(this.sapphireConnection);
                status.resetStats();
            } else if (MODE_RESET_CACHE.equalsIgnoreCase(mode)) {
                CacheUtil.resetCache(this.sapphireConnection.getDatabaseId(), true);
                NotifyManager.reset(this.sapphireConnection.getDatabaseId());
            } else if (MODE_RESET_AUTOMATION.equalsIgnoreCase(mode)) {
                try {
                    new AutomationProcessor(this.sapphireConnection.getConnectionId()).resetAutomation();
                }
                catch (LAMException e) {
                    throw new SapphireException(e);
                }
            } else if (MODE_RUN_HOUSEKEEPING.equalsIgnoreCase(mode)) {
                new AutomationProcessor(this.sapphireConnection.getConnectionId()).performHousekeeping();
            } else if (MODE_RESET_STATS.equalsIgnoreCase(mode)) {
                EventManager.resetStats(this.sapphireConnection);
            } else if (MODE_USERPROFILE_SAVE.equalsIgnoreCase(mode)) {
                String userid = (String)props.get(PROFILEPROPERTY_USERID);
                if (userid == null || userid.length() == 0) {
                    userid = "(system)";
                }
                if (props.get("__userprofilereset") != null) {
                    config.resetProfileProperty(userid, "logonpageurl");
                    config.resetProfileProperty(userid, "logonmenu");
                    config.resetProfileProperty(userid, "logongroup");
                    ArrayList<Browser.GUIMode> guiModes = Browser.getGUIModes(this.sapphireConnection, this.sapphireConnection.getDatabaseId());
                    for (int g = 0; g < guiModes.size(); ++g) {
                        String id = guiModes.get(g).getId();
                        config.resetProfileProperty(userid, id + "logonpageurl");
                        config.resetProfileProperty(userid, id + "logonmenu");
                        config.resetProfileProperty(userid, id + "logongroup");
                    }
                    config.resetProfileProperty(userid, "startuptype");
                    config.resetProfileProperty(userid, "defaultworkbook");
                    config.resetProfileProperty(userid, "bourl");
                    config.resetProfileProperty(userid, "bouniverse");
                    config.resetProfileProperty(userid, "bodocumentdomain");
                    config.resetProfileProperty(userid, "bodomain");
                    config.resetProfileProperty(userid, "boexchangemode");
                    config.resetProfileProperty(userid, "bopassword");
                    config.resetProfileProperty(userid, "bousername");
                    config.resetProfileProperty(userid, "bocmsname");
                    config.resetProfileProperty(userid, "boauthenticationtype");
                    config.resetProfileProperty(userid, "borootfoldername");
                    config.resetProfileProperty(userid, "boconnectiontimeout");
                    config.resetProfileProperty(userid, "bocrystaldateformat");
                    config.resetProfileProperty(userid, "bodeskidateformat");
                    config.resetProfileProperty(userid, "bowebidateformat");
                    config.resetProfileProperty(userid, "menuanimation");
                    config.resetProfileProperty(userid, "resizeanimation");
                    config.resetProfileProperty(userid, "fadeanimation");
                    config.resetProfileProperty(userid, "notificationformat");
                } else {
                    String value = (String)props.get("logonpageurl");
                    if (value == null || value.length() == 0) {
                        config.resetProfileProperty(userid, "logonpageurl");
                    } else if (!props.get("logonpageurl").equals(config.getProfileProperty(userid, "logonpageurl"))) {
                        config.setProfileProperty(userid, "logonpageurl", (String)props.get("logonpageurl"));
                    }
                    value = (String)props.get("logonmenu");
                    if (value == null || value.length() == 0) {
                        config.resetProfileProperty(userid, "logonmenu");
                    } else if (!props.get("logonmenu").equals(config.getProfileProperty(userid, "logonmenu"))) {
                        config.setProfileProperty(userid, "logonmenu", (String)props.get("logonmenu"));
                    }
                    value = (String)props.get("logongroup");
                    if (value == null || value.length() == 0) {
                        config.resetProfileProperty(userid, "logongroup");
                    } else if (!props.get("logongroup").equals(config.getProfileProperty(userid, "logongroup"))) {
                        config.setProfileProperty(userid, "logongroup", (String)props.get("logongroup"));
                    }
                    ArrayList<Browser.GUIMode> guiModes = Browser.getGUIModes(this.sapphireConnection, this.sapphireConnection.getDatabaseId());
                    for (int g = 0; g < guiModes.size(); ++g) {
                        String id = guiModes.get(g).getId();
                        value = (String)props.get(id + "logonpageurl");
                        if (value == null || value.length() == 0) {
                            config.resetProfileProperty(userid, id + "logonpageurl");
                        } else if (!props.get(id + "logonpageurl").equals(config.getProfileProperty(userid, id + "logonpageurl"))) {
                            config.setProfileProperty(userid, id + "logonpageurl", (String)props.get(id + "logonpageurl"));
                        }
                        value = (String)props.get(id + "logonmenu");
                        if (value == null || value.length() == 0) {
                            config.resetProfileProperty(userid, id + "logonmenu");
                        } else if (!props.get(id + "logonmenu").equals(config.getProfileProperty(userid, id + "logonmenu"))) {
                            config.setProfileProperty(userid, id + "logonmenu", (String)props.get(id + "logonmenu"));
                        }
                        value = (String)props.get(id + "logongroup");
                        if (value == null || value.length() == 0) {
                            config.resetProfileProperty(userid, id + "logongroup");
                            continue;
                        }
                        if (props.get(id + "logongroup").equals(config.getProfileProperty(userid, id + "logongroup"))) continue;
                        config.setProfileProperty(userid, id + "logongroup", (String)props.get(id + "logongroup"));
                    }
                    value = (String)props.get("startuptype");
                    if (value == null || value.length() == 0) {
                        config.resetProfileProperty(userid, "startuptype");
                    } else if (!props.get("startuptype").equals(config.getProfileProperty(userid, "startuptype"))) {
                        config.setProfileProperty(userid, "startuptype", (String)props.get("startuptype"));
                    }
                    value = (String)props.get("defaultworkbook");
                    if (value == null || value.length() == 0) {
                        config.resetProfileProperty(userid, "defaultworkbook");
                    } else if (!props.get("defaultworkbook").equals(config.getProfileProperty(userid, "defaultworkbook"))) {
                        config.setProfileProperty(userid, "defaultworkbook", (String)props.get("defaultworkbook"));
                    }
                    value = (String)props.get("bouniverse");
                    if (value == null || value.length() == 0) {
                        config.resetProfileProperty(userid, "bouniverse");
                    } else if (!props.get("bouniverse").equals(config.getProfileProperty(userid, "bouniverse"))) {
                        config.setProfileProperty(userid, "bouniverse", (String)props.get("bouniverse"));
                    }
                    value = (String)props.get("bodocumentdomain");
                    if (value == null || value.length() == 0) {
                        config.resetProfileProperty(userid, "bodocumentdomain");
                    } else if (!props.get("bodocumentdomain").equals(config.getProfileProperty(userid, "bodocumentdomain"))) {
                        config.setProfileProperty(userid, "bodocumentdomain", (String)props.get("bodocumentdomain"));
                    }
                    value = (String)props.get("bodomain");
                    if (value == null || value.length() == 0) {
                        config.resetProfileProperty(userid, "bodomain");
                    } else if (!props.get("bodomain").equals(config.getProfileProperty(userid, "bodomain"))) {
                        config.setProfileProperty(userid, "bodomain", (String)props.get("bodomain"));
                    }
                    value = (String)props.get("boexchangemode");
                    if (value == null || value.length() == 0) {
                        config.resetProfileProperty(userid, "boexchangemode");
                    } else if (!props.get("boexchangemode").equals(config.getProfileProperty(userid, "boexchangemode"))) {
                        config.setProfileProperty(userid, "boexchangemode", (String)props.get("boexchangemode"));
                    }
                    value = (String)props.get("bopassword");
                    String storedValue = config.getProfileProperty(userid, "bopassword");
                    if (value == null || value.length() == 0) {
                        config.resetProfileProperty(userid, "bopassword");
                    } else if (!value.equals(storedValue) && !value.equalsIgnoreCase("(storedpassword)")) {
                        config.setProfileProperty(userid, "bopassword", EncryptDecrypt.encrypt(value, this.sapphireConnection.getDatabaseId()));
                    }
                    value = (String)props.get("bousername");
                    if (value == null || value.length() == 0) {
                        config.resetProfileProperty(userid, "bousername");
                    } else if (!props.get("bousername").equals(config.getProfileProperty(userid, "bousername"))) {
                        config.setProfileProperty(userid, "bousername", (String)props.get("bousername"));
                    }
                    value = (String)props.get("bocmsname");
                    if (value == null || value.length() == 0) {
                        config.resetProfileProperty(userid, "bocmsname");
                    } else if (!props.get("bocmsname").equals(config.getProfileProperty(userid, "bocmsname"))) {
                        config.setProfileProperty(userid, "bocmsname", (String)props.get("bocmsname"));
                    }
                    value = (String)props.get("bourl");
                    if (value == null || value.length() == 0) {
                        config.resetProfileProperty(userid, "bourl");
                    } else if (!props.get("bourl").equals(config.getProfileProperty(userid, "bourl"))) {
                        config.setProfileProperty(userid, "bourl", (String)props.get("bourl"));
                    }
                    value = (String)props.get("boauthenticationtype");
                    if (value == null || value.length() == 0) {
                        config.resetProfileProperty(userid, "boauthenticationtype");
                    } else if (!props.get("boauthenticationtype").equals(config.getProfileProperty(userid, "boauthenticationtype"))) {
                        config.setProfileProperty(userid, "boauthenticationtype", (String)props.get("boauthenticationtype"));
                    }
                    value = (String)props.get("borootfoldername");
                    if (value == null || value.length() == 0) {
                        config.resetProfileProperty(userid, "borootfoldername");
                    } else if (!props.get("borootfoldername").equals(config.getProfileProperty(userid, "borootfoldername"))) {
                        config.setProfileProperty(userid, "borootfoldername", (String)props.get("borootfoldername"));
                    }
                    value = (String)props.get("boconnectiontimeout");
                    if (value == null || value.length() == 0) {
                        config.resetProfileProperty(userid, "boconnectiontimeout");
                    } else if (!props.get("boconnectiontimeout").equals(config.getProfileProperty(userid, "boconnectiontimeout"))) {
                        config.setProfileProperty(userid, "boconnectiontimeout", (String)props.get("boconnectiontimeout"));
                    }
                    value = (String)props.get("bocrystaldateformat");
                    if (value == null || value.length() == 0) {
                        config.resetProfileProperty(userid, "bocrystaldateformat");
                    } else if (!props.get("bocrystaldateformat").equals(config.getProfileProperty(userid, "bocrystaldateformat"))) {
                        config.setProfileProperty(userid, "bocrystaldateformat", (String)props.get("bocrystaldateformat"));
                    }
                    value = (String)props.get("bodeskidateformat");
                    if (value == null || value.length() == 0) {
                        config.resetProfileProperty(userid, "bodeskidateformat");
                    } else if (!props.get("bodeskidateformat").equals(config.getProfileProperty(userid, "bodeskidateformat"))) {
                        config.setProfileProperty(userid, "bodeskidateformat", (String)props.get("bodeskidateformat"));
                    }
                    value = (String)props.get("bowebidateformat");
                    if (value == null || value.length() == 0) {
                        config.resetProfileProperty(userid, "bowebidateformat");
                    } else if (!props.get("bowebidateformat").equals(config.getProfileProperty(userid, "bowebidateformat"))) {
                        config.setProfileProperty(userid, "bowebidateformat", (String)props.get("bowebidateformat"));
                    }
                    value = (String)props.get("menuanimation");
                    if (value == null || value.length() == 0) {
                        config.resetProfileProperty(userid, "menuanimation");
                    } else if (!props.get("menuanimation").equals(config.getProfileProperty(userid, "menuanimation"))) {
                        config.setProfileProperty(userid, "menuanimation", (String)props.get("menuanimation"));
                    }
                    value = (String)props.get("resizeanimation");
                    if (value == null || value.length() == 0) {
                        config.resetProfileProperty(userid, "resizeanimation");
                    } else if (!props.get("resizeanimation").equals(config.getProfileProperty(userid, "resizeanimation"))) {
                        config.setProfileProperty(userid, "resizeanimation", (String)props.get("resizeanimation"));
                    }
                    value = (String)props.get("fadeanimation");
                    if (value == null || value.length() == 0) {
                        config.resetProfileProperty(userid, "fadeanimation");
                    } else if (!props.get("fadeanimation").equals(config.getProfileProperty(userid, "fadeanimation"))) {
                        config.setProfileProperty(userid, "fadeanimation", (String)props.get("fadeanimation"));
                    }
                    value = (String)props.get("notificationformat");
                    if (value == null || value.length() == 0) {
                        config.resetProfileProperty(userid, "notificationformat");
                    } else if (!props.get("notificationformat").equals(config.getProfileProperty(userid, "notificationformat"))) {
                        config.setProfileProperty(userid, "notificationformat", (String)props.get("notificationformat"));
                    }
                }
            } else {
                throw new SapphireException("Unrecognized mode '" + mode + "'");
            }
            if (submittedpd != null && submittedpd.indexOf("{|}") == 0) {
                props.put("bopassword", submittedpd);
            }
        }
        catch (ServiceException e) {
            throw new SapphireException("Failed to process " + mode + " command", e);
        }
    }
}

