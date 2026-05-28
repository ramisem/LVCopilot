/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.aspose.cells.License
 *  com.aspose.imaging.License
 *  com.aspose.pdf.License
 *  com.aspose.slides.License
 *  com.aspose.words.License
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 *  org.apache.logging.log4j.core.Appender
 *  org.apache.logging.log4j.core.Logger
 *  org.apache.logging.log4j.core.appender.FileAppender
 */
package com.labvantage.sapphire.services;

import com.labvantage.sapphire.Build;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.License;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.XSS;
import com.labvantage.sapphire.admin.ddt.LV_App;
import com.labvantage.sapphire.admin.ddt.LV_Portal;
import com.labvantage.sapphire.admin.install.Installer;
import com.labvantage.sapphire.admin.system.AutomationProcessor;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.admin.system.IndexToolsPropertyHandler;
import com.labvantage.sapphire.admin.system.SQLRegister;
import com.labvantage.sapphire.admin.system.ThreadUtil;
import com.labvantage.sapphire.admin.system.automation.LAM;
import com.labvantage.sapphire.admin.system.automation.LAMException;
import com.labvantage.sapphire.admin.system.automation.runnables.ServerPing;
import com.labvantage.sapphire.admin.vc.PhoneHomeInfo;
import com.labvantage.sapphire.ejb.SapphireManagerLocal;
import com.labvantage.sapphire.modules.eventmanager.EventManager;
import com.labvantage.sapphire.modules.search.Indexer;
import com.labvantage.sapphire.modules.search.SearchRequest;
import com.labvantage.sapphire.modules.search.SearchResults;
import com.labvantage.sapphire.modules.search.Searcher;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.platform.SapphireDatabase;
import com.labvantage.sapphire.services.AutomationService;
import com.labvantage.sapphire.services.BaseService;
import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.SecurityService;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.servlet.ConsoleController;
import com.labvantage.sapphire.servlet.filter.AddHeaders;
import com.labvantage.sapphire.servlet.filter.Compression;
import com.labvantage.sapphire.servlet.filter.RejectRequest;
import com.labvantage.sapphire.servlet.rest.BaseNameSpaceHandler;
import com.labvantage.sapphire.util.LabVantageSecurityManager;
import com.labvantage.sapphire.util.UnitsUtil;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.images.ImageRef;
import com.labvantage.sapphire.util.jndi.ServiceLocator;
import com.labvantage.sapphire.util.logger.LogUtil;
import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import com.labvantage.sapphire.xml.ImportXML;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.Policy;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.FileAppender;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.ext.BaseSQLRegister;
import sapphire.util.Browser;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;

public class SapphireService
extends BaseService {
    public static final String LOGNAME = "SapphireService";
    public static final String STARTUP = "STARTUP";
    private static String systemPassword;
    private static boolean started;
    private static String homeURL;
    public static final String COMMAND_RESETCACHE = "ResetCache";
    public static final String COMMAND_UPDATEUNITCONVERSIONCACHE = "UpdateUnitConversionCache";
    public static final String COMMAND_GETLOGGERFILES = "GetLoggerFiles";
    public static final String COMMAND_LOADEVENTPLANS = "LoadEventPlans";
    public static final String COMMAND_STARTLAMMONITOR = "StartLAMMonitor";
    public static final String COMMAND_STOPLAMMONITOR = "StopLAMMonitor";
    public static final String COMMAND_INDEX = "index";
    public static final String COMMAND_INDEXPENDING = "indexpending";
    public static final String COMMAND_RESETINDEX = "resetindex";
    public static final String COMMAND_RESETPENDINGINDEX = "resetpendingindex";
    public static final String COMMAND_RESETINDEXMAP = "resetindexmap";
    public static final String COMMAND_STARTINDEXING = "startindexing";
    public static final String COMMAND_STOPINDEXING = "stopindexing";
    public static final String COMMAND_GETINDEXINGSTATUS = "getindexingstatus";
    public static final String COMMAND_FLUSHCALENDAR = "flushcalendar";
    public static final String COMMAND_SEARCH = "search";
    public static final String COMMAND_CRAWLDATABASE = "crawldatabase";
    public static final String COMMAND_CREATEBACKLOG = "createbacklog";
    public static final String COMMAND_INDEXBACKLOG = "indexbacklog";
    public static final String RETURN_INDEXINGSTATUS = "returnindexingstatus";
    public static final String RETURN_NUMOFHITS = "numofhits";
    public static final String INTERNAL_CONNECTIONID_TOOL = "Internal";
    public static final String SAASSUPPORT_CONNECTIONID_TOOL = "Internal_SaaS";
    private static HashMap<String, String> internalConnectionidCache;

    public static void setSystemPassword(String systemPswd) {
        systemPassword = systemPswd;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * WARNING - void declaration
     */
    public static ArrayList<String> startup(HashMap startupProps) throws ServiceException {
        String LOGNAME = STARTUP;
        ArrayList<String> startupMessages = new ArrayList<String>();
        PhoneHomeInfo phoneHomeInfo = PhoneHomeInfo.getInstance();
        boolean hasServerLicense = false;
        try {
            Appender appender;
            ArrayList<String> errorList = new ArrayList<String>();
            ArrayList<Object> warningList = new ArrayList<Object>();
            Configuration configuration = Configuration.getInstance();
            Trace.setLoggerPrefix(configuration.getApplicationid());
            Trace.logDebug(LOGNAME, "Starting LabVantage");
            startupMessages.add("LabVantage " + Build.getVersion() + " startup (Build: " + Build.getBuild() + " Build Date: " + Build.getBuildDate() + ")");
            Trace.stats = ConfigService.getConfigProperty("com.labvantage.sapphire.server.stats").equals("true") || ConfigService.getConfigProperty("com.labvantage.sapphire.server.stats").equals("Y");
            Trace.tracestats = ConfigService.getConfigProperty("com.labvantage.sapphire.server.tracestats").equals("true") || ConfigService.getConfigProperty("com.labvantage.sapphire.server.tracestats").equals("Y");
            EncryptDecrypt.obfMode = ConfigService.getConfigProperty("com.labvantage.sapphire.server.obfuscatesql", "O");
            if (Configuration.isJunitServer()) {
                Trace.logStartup("****************************************************************************");
                Trace.logStartup("** JUNIT TEST SERVER - NOT TO BE USED IN A PRODUCTION ENVIRONMENT");
                Trace.logStartup("****************************************************************************");
            }
            Trace.logStartup("****************************************************************************");
            Trace.logStartup("** Starting LabVantage");
            Trace.logStartup("****************************************************************************");
            Trace.logStartup("** Platform        : " + configuration.getServerInfo());
            phoneHomeInfo.collectInfo("platform", configuration.getServerInfo());
            Trace.logStartup("** Platform version: " + configuration.getServerVersion());
            phoneHomeInfo.collectInfo("platformversion", configuration.getServerVersion());
            Trace.logStartup("** Hostname        : " + configuration.getServerHostName());
            phoneHomeInfo.collectInfo("hostname", configuration.getServerHostName());
            Trace.logStartup("** Serverid        : " + configuration.getServerid());
            phoneHomeInfo.collectInfo("serverid", configuration.getServerid());
            Trace.logStartup("** " + ConsoleController.PRODUCT_HOME + " : " + new File(configuration.getSapphireHome()).getAbsolutePath());
            Trace.logStartup("** Application id  : " + configuration.getApplicationid());
            phoneHomeInfo.collectInfo("applicationid", configuration.getApplicationid());
            if (!configuration.requireLicensePerDatabase() || configuration.getServerLicenseFile().exists()) {
                Trace.logStartup("** License file    : " + configuration.getServerLicenseFile().getAbsolutePath());
                hasServerLicense = true;
            }
            Trace.logStartup("** Config file     : " + configuration.getConfigFile().getAbsolutePath());
            Trace.logStartup("** Log config file : " + configuration.getServerLogConfigFile().getAbsolutePath());
            Trace.logStartup("** LabVantage build: " + Build.getBuild() + " Patch: " + Build.getPatch());
            phoneHomeInfo.collectInfo("build", Build.getBuild());
            phoneHomeInfo.collectInfo("patch", Build.getPatch());
            Trace.logStartup("** Java version    : " + System.getProperty("java.version"));
            phoneHomeInfo.collectInfo("javaversion", System.getProperty("java.version"));
            Trace.logStartup("** Locale setting  : " + Locale.getDefault().toString());
            phoneHomeInfo.collectInfo("locale", Locale.getDefault().toString());
            Trace.logStartup("** Tracing         : " + (Trace.on ? "Enabled" : "Disabled"));
            phoneHomeInfo.collectInfo("tracing", Trace.on ? "Enabled" : "Disabled");
            Trace.logStartup("** SQL Obfuscation : " + (EncryptDecrypt.obfMode.equals("O") ? "Obfuscating" : "Off") + " (" + EncryptDecrypt.obfMode + ")");
            Trace.logStartup("****************************************************************************");
            String jarErrors = Configuration.checkJarList();
            if (jarErrors.length() > 0) {
                Trace.logStartup("** " + jarErrors);
                errorList.add(jarErrors);
            }
            Trace.logStartup("** " + StringUtil.padRight("Context", 55) + StringUtil.padRight("Product 3rd Party JAR", 55) + StringUtil.padRight("Version", 15) + "License");
            Trace.logStartup("****************************************************************************");
            for (int i = 0; i < Configuration.currentAllJarDetails.length; ++i) {
                String[] jar = Configuration.currentAllJarDetails[i];
                Trace.logStartup("** " + StringUtil.padRight(jar[0], 55) + StringUtil.padRight(jar[1], 55) + StringUtil.padRight(jar.length > 4 ? jar[4] : "", 15) + (jar.length > 5 ? jar[5] : ""));
            }
            Trace.logStartup("****************************************************************************");
            SecurityService.generateSystemPassword();
            Trace.logStartup("****************************************************************************");
            Trace.logStartup("** LabVantage License properties");
            if (configuration.requireLicensePerDatabase()) {
                Trace.logStartup(LOGNAME, "System running in database license mode - all databases are required to have their own license key.");
            }
            if (hasServerLicense) {
                String[][] modules;
                void var13_24;
                String[][] values;
                Properties licenseProps = configuration.getServerLicense().getProperties();
                String[][] stringArray = values = License.getLicenseProperties();
                int n = stringArray.length;
                boolean bl = false;
                while (var13_24 < n) {
                    String[] value = stringArray[var13_24];
                    Trace.logStartup("** - " + value[0] + "=" + licenseProps.getProperty(value[0], ""));
                    phoneHomeInfo.collectInfo(value[0], licenseProps.getProperty(value[0], ""));
                    if ("phonehome".equals(value[0]) && "N".equals(licenseProps.getProperty(value[0], ""))) {
                        phoneHomeInfo.setPhoneHomeAllowed(false);
                    }
                    if ("phonehomeurl".equals(value[0]) && licenseProps.getProperty(value[0], "").length() > 0) {
                        homeURL = licenseProps.getProperty(value[0]);
                    }
                    ++var13_24;
                }
                for (String[] module : modules = License.getModules()) {
                    Trace.logStartup("** - " + module[0] + "=" + licenseProps.getProperty("module_" + module[0], ""));
                    phoneHomeInfo.collectInfo(module[0], licenseProps.getProperty("module_" + module[0], ""));
                }
                for (String string : licenseProps.keySet()) {
                    if (!string.startsWith("sdc_")) continue;
                    Trace.logStartup("** - " + string + "=" + licenseProps.getProperty(string, ""));
                    phoneHomeInfo.collectInfo(string, licenseProps.getProperty(string, ""));
                }
                String string = licenseProps.getProperty("module_SampleMonitoring", "");
                if (!(string.isEmpty() || string.equals("0") || string.equals("S"))) {
                    warningList.add("License key for SampleMonitoring module is incorrect. The license is not Site wide. Please contact support to get a new License key.");
                }
            } else {
                Trace.logStartup(LOGNAME, "No Server License present");
            }
            try {
                Trace.logStartup("****************************************************************************");
                Trace.logStartup("** Registering Aspose license");
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                InputStream licenseStream = classLoader.getResourceAsStream("/META-INF/Aspose.Total.Java.lic");
                com.aspose.words.License license = new com.aspose.words.License();
                license.setLicense(licenseStream);
                licenseStream.close();
                classLoader = Thread.currentThread().getContextClassLoader();
                licenseStream = classLoader.getResourceAsStream("/META-INF/Aspose.Total.Java.lic");
                license = new com.aspose.cells.License();
                license.setLicense(licenseStream);
                licenseStream.close();
                classLoader = Thread.currentThread().getContextClassLoader();
                licenseStream = classLoader.getResourceAsStream("/META-INF/Aspose.Total.Java.lic");
                license = new com.aspose.pdf.License();
                license.setLicense(licenseStream);
                licenseStream.close();
                classLoader = Thread.currentThread().getContextClassLoader();
                licenseStream = classLoader.getResourceAsStream("/META-INF/Aspose.Total.Java.lic");
                license = new com.aspose.slides.License();
                license.setLicense(licenseStream);
                licenseStream.close();
                classLoader = Thread.currentThread().getContextClassLoader();
                licenseStream = classLoader.getResourceAsStream("/META-INF/Aspose.Total.Java.lic");
                license = new com.aspose.imaging.License();
                license.setLicense(licenseStream);
                licenseStream.close();
            }
            catch (Exception e) {
                Trace.logError("Unable to locate the Aspose.Total.Java.lic");
                warningList.add("Unable to locate the Aspose.Total.Java.lic. Exporting ELN Worksheets to Word and Excel may not be available.");
            }
            Trace.logStartup("****************************************************************************");
            Trace.logStartup("** Java system properties");
            Properties props = System.getProperties();
            Set<Object> keys = props.keySet();
            for (String string : keys) {
                Trace.logStartup("** - " + string + " = " + props.getProperty(string));
            }
            Trace.logStartup("****************************************************************************");
            Trace.logStartup("** Logging files for " + configuration.getApplicationid() + " logger");
            org.apache.logging.log4j.Logger appLogger = LogManager.getLogger((String)configuration.getApplicationid());
            Map map = ((Logger)appLogger).getAppenders();
            for (Map.Entry appenderItem : map.entrySet()) {
                appender = (Appender)appenderItem.getValue();
                if (!(appender instanceof FileAppender)) continue;
                configuration.addLoggerFile(((FileAppender)appender).getFileName());
                Trace.logStartup("** - " + appender.getName() + "(" + appLogger.getLevel() + ") = " + ((FileAppender)appender).getFileName());
            }
            Trace.logStartup("** Logging files for " + LOGNAME + " logger");
            appLogger = LogManager.getLogger((String)LOGNAME);
            for (Map.Entry appenderItem : map.entrySet()) {
                appender = (Appender)appenderItem.getValue();
                if (!(appender instanceof FileAppender)) continue;
                configuration.addLoggerFile(((FileAppender)appender).getFileName());
                Trace.logStartup("** - " + appender.getName() + "(" + appLogger.getLevel() + ") = " + ((FileAppender)appender).getFileName());
            }
            Trace.logStartup("****************************************************************************");
            Trace.logStartup("** Checking configuration of server: " + configuration.getServerHostName());
            Trace.logStartup("****************************************************************************");
            String string = configuration.getApplicationid();
            configuration.checkPlatformConfiguration(errorList);
            File file = Configuration.getInstance().getConfigFile();
            if (!file.exists()) {
                throw new SapphireException("Configuration file 'sapphireconfig.props' not found!");
            }
            Trace.logStartup("** Checking version of application: " + string);
            String ignoreAppBuild = ConfigService.getConfigProperty("com.labvantage.sapphire.server.ignoreappbuild");
            if (ignoreAppBuild.length() == 0 || ignoreAppBuild.equals("false") || ignoreAppBuild.equals("N")) {
                if (new File(configuration.getSapphireHome(), "console").exists()) {
                    File labvantageEAR = new File(configuration.getSapphireHome(), "console/install/ear/labvantage.ear");
                    PropertyList sapphireEARProps = ConsoleController.getEARBuildProps(labvantageEAR, ConsoleController.getConsoleTemp(configuration.getSapphireHome()));
                    String sapphireBuild = sapphireEARProps.getProperty("sapphire.build");
                    if (Build.getBuild().equals(sapphireBuild)) {
                        Trace.logStartup("** Application version correct");
                    } else {
                        Trace.logStartup("** ERROR: Application '" + string + "' (Build: " + Build.getBuild() + ") different version from the " + "labvantage.ear" + " (Build: " + sapphireBuild + ") installed in " + labvantageEAR.getParentFile().getAbsolutePath());
                        errorList.add("Application '" + string + "' (Build: " + Build.getBuild() + ") different version from the " + "labvantage.ear" + " (Build: " + sapphireBuild + ") installed in " + labvantageEAR.getParentFile().getAbsolutePath());
                    }
                } else {
                    Trace.logStartup("** Production HOME - Application version not checked");
                }
            } else {
                warningList.add("IGNORING Application build - NOT TO BE USED IN A PRODUCTION ENVIRONMENT");
                Trace.logStartup("** IGNORING Application build - NOT TO BE USED IN A PRODUCTION ENVIRONMENT");
            }
            try {
                String id = FileUtil.getInputStreamString(SapphireService.class.getResourceAsStream("/ear.id"));
                Trace.logStartup("** EAR identifier: " + id);
            }
            catch (Exception e) {
                warningList.add("Missing EAR identifier file!");
                Trace.logStartup("** Missing EAR identifier file");
            }
            try {
                URL url = SapphireService.class.getResource("/ver" + Build.getVersionIdentifier() + ".id");
                if (url == null) {
                    throw new SapphireException();
                }
            }
            catch (Exception url) {
                // empty catch block
            }
            Trace.logStartup("** Compression filter is " + (StringUtil.getYN(ConfigService.getConfigProperty("com.labvantage.sapphire.server.filter.compression"), "Y").equals("Y") ? "enabled" : "disabled"));
            Compression.setEnabled(StringUtil.getYN(ConfigService.getConfigProperty("com.labvantage.sapphire.server.filter.compression"), "Y").equals("Y"));
            Trace.logStartup("** AddHeaders filter is " + (StringUtil.getYN(ConfigService.getConfigProperty("com.labvantage.sapphire.server.filter.addheaders"), "Y").equals("Y") ? "enabled" : "disabled"));
            AddHeaders.setEnabled(StringUtil.getYN(ConfigService.getConfigProperty("com.labvantage.sapphire.server.filter.addheaders"), "Y").equals("Y"));
            Trace.logStartup("** RejectRequest filter is " + (StringUtil.getYN(ConfigService.getConfigProperty("com.labvantage.sapphire.server.filter.rejectrequest"), "Y").equals("Y") ? "enabled" : "disabled"));
            RejectRequest.setEnabled(StringUtil.getYN(ConfigService.getConfigProperty("com.labvantage.sapphire.server.filter.rejectrequest"), "Y").equals("Y"));
            Trace.logStartup("** RejectRequest SQL logging is " + (StringUtil.getYN(ConfigService.getConfigProperty("com.labvantage.sapphire.server.filter.rejectrequest.sqllogging"), "N").equals("Y") ? "enabled" : "disabled"));
            RejectRequest.setSqlLoggingEnabled(StringUtil.getYN(ConfigService.getConfigProperty("com.labvantage.sapphire.server.filter.rejectrequest.sqllogging"), "N").equals("Y"));
            String admindb = ConfigService.getConfigProperty("com.labvantage.sapphire.server.admindb");
            String dbms = ConfigService.getConfigProperty("com.labvantage.sapphire.server.admindbms");
            Trace.logStartup("** Admin database is: " + admindb);
            Trace.logStartup("** Admin database DBMS is: " + dbms);
            configuration.checkAdmindb(admindb, dbms);
            Trace.logStartup("** Admin database running correct version");
            configuration.getConfigurationWarnings(warningList);
            HashMap<String, String> databaseStatus = new HashMap<String, String>();
            DBUtil adb = new DBUtil();
            boolean activedatabases = false;
            try {
                adb.setConnection(ConfigService.getConfigProperty("com.labvantage.sapphire.server.admindbms"), ServiceLocator.getInstance().getDataSource(admindb).getConnection());
                Trace.logStartup("** Admin database JDBC driver        : " + adb.getConnection().getMetaData().getDriverName());
                Trace.logStartup("** Admin database JDBC driver version: " + adb.getConnection().getMetaData().getDriverVersion());
                Trace.logStartup("****************************************************************************");
                Trace.logStartup("** Checking registration of application: " + string);
                adb.createPreparedResultSet("SELECT * FROM application WHERE applicationid = ?", new Object[]{string});
                if (adb.getNext()) {
                    configuration.setApplicationdesc(adb.getValue("applicationdesc"));
                    String managedEAR = adb.getValue("managedear");
                    if (managedEAR != null && managedEAR.length() > 0) {
                        configuration.setApplicationEAR(managedEAR.substring(0, managedEAR.indexOf(".")));
                    } else {
                        configuration.setApplicationEAR(adb.getValue("applicationid"));
                    }
                    Trace.logStartup("** Application registered");
                } else {
                    String ignoreRegistration = ConfigService.getConfigProperty("com.labvantage.sapphire.server.ignoreregistration");
                    if (ignoreRegistration.length() == 0 || ignoreRegistration.equals("false") || ignoreRegistration.equals("N")) {
                        Trace.logStartup("** ERROR: Application '" + string + "' not registered");
                        errorList.add("Application '" + string + "' not registered");
                    } else {
                        warningList.add("IGNORING Application registration - NOT TO BE USED IN A PRODUCTION ENVIRONMENT");
                        Trace.logStartup("** IGNORING Application registration - NOT TO BE USED IN A PRODUCTION ENVIRONMENT");
                        configuration.setApplicationEAR(string);
                    }
                }
                Trace.logStartup("****************************************************************************");
                Trace.logStartup("****************************************************************************");
                Trace.logStartup("** Configuring LabVantage Databases");
                adb.createResultSet("SELECT databaselistid, jndiname, dbms, dbserver, sqldatabase, dbconnectionkey, licensekey FROM databaselist WHERE statusflag = 'A'");
                while (adb.getNext()) {
                    activedatabases = true;
                    SapphireDatabase database = new SapphireDatabase();
                    String string2 = adb.getString("databaselistid");
                    database.setDatabaseId(string2);
                    database.setJndiname(adb.getString("jndiname"));
                    database.setDbms(adb.getString("dbms"));
                    phoneHomeInfo.collectDatabaseInfo(string2, "dbms", adb.getString("dbms"));
                    database.setDbServer(adb.getString("dbserver"));
                    database.setSqlDatabase(adb.getString("sqldatabase"));
                    database.setDbConnectionKey(adb.getString("dbconnectionkey"));
                    database.setLicense(adb.getString("licensekey"));
                    try {
                        Object error22;
                        databaseStatus.put(database.getDatabaseId(), "OK");
                        phoneHomeInfo.collectDatabaseInfo(string2, "status", "OK");
                        SapphireManagerLocal sapphireManager = ServiceLocator.getInstance().getSapphireManager();
                        ArrayList databaseErrors = sapphireManager.startupDatabase(database);
                        if (!configuration.requireLicensePerDatabase()) {
                            for (Object error22 : databaseErrors) {
                                if (((String)error22).startsWith("ERROR")) {
                                    errorList.add(((String)error22).substring(7));
                                    continue;
                                }
                                if (((String)error22).startsWith("WARNING")) {
                                    warningList.add(((String)error22).substring(9));
                                    continue;
                                }
                                warningList.add(error22);
                            }
                            continue;
                        }
                        ArrayList<String> thisDatabaseErrors = new ArrayList<String>();
                        error22 = databaseErrors.iterator();
                        while (error22.hasNext()) {
                            String error3 = (String)error22.next();
                            if (error3.startsWith("ERROR")) {
                                thisDatabaseErrors.add(error3.substring(7));
                                continue;
                            }
                            if (error3.startsWith("WARNING")) {
                                warningList.add(error3.substring(9));
                                continue;
                            }
                            warningList.add(error3);
                        }
                        if (thisDatabaseErrors.isEmpty()) continue;
                        databaseStatus.put(database.getDatabaseId(), "ERROR");
                        Trace.logStartup(LOGNAME, "Exception generated starting database '" + database.getDatabaseId() + "'. You will not be able to connect to this database. ");
                        StringBuilder errorMsg = new StringBuilder();
                        for (String anErrorList : thisDatabaseErrors) {
                            Trace.logStartup(LOGNAME, "** ERROR: - " + anErrorList);
                        }
                    }
                    catch (Exception e) {
                        databaseStatus.put(database.getDatabaseId(), "ERROR: " + e.getMessage());
                        phoneHomeInfo.collectDatabaseInfo(string2, "status", "ERROR: " + e.getMessage());
                        Trace.logError(LOGNAME, (Object)("Exception generated starting database '" + database.getDatabaseId() + "'. You will not be able to connect to this database. Exception: " + e.getMessage()), e);
                    }
                }
            }
            catch (SapphireException se) {
                Trace.logError(LOGNAME, "Failed to access database list in admindb. Exception: " + se.getMessage());
            }
            finally {
                adb.reset();
                adb.releaseConnection();
            }
            if (!activedatabases) {
                Trace.logStartup("** No active databases found (note: you may need to upgrade your databases using LabVantage Console)");
                Trace.logStartup("****************************************************************************");
            }
            configuration.applyPatches(errorList);
            Trace.logStartup("****************************************************************************");
            Trace.logStartup("** Database Startup Summary");
            startupMessages.add("Database Startup Summary");
            for (String string3 : databaseStatus.keySet()) {
                Trace.logStartup("** Database: " + string3 + "\t\tStatus: " + (String)databaseStatus.get(string3));
                startupMessages.add("- Database: " + string3 + "\t\tStatus: " + (String)databaseStatus.get(string3));
            }
            Trace.logStartup("****************************************************************************");
            if (warningList.size() > 0) {
                Trace.logStartup("****************************************************************************");
                Trace.logStartup("** WARNING: The following warnings were found during startup:");
                startupMessages.add("WARNINGS:");
                for (String string4 : warningList) {
                    Trace.logStartup("** WARNING: - " + string4);
                    startupMessages.add("- " + string4);
                }
            }
            if (Configuration.isJunitServer()) {
                startupMessages.add("WARNING: JUNIT TEST SERVER - NOT TO BE USED IN A PRODUCTION ENVIRONMENT");
            }
            if (errorList.size() > 0) {
                Trace.logStartup("****************************************************************************");
                Trace.logStartup("** ERROR: The following errors were found during startup:");
                startupMessages.add("ERRORS:");
                StringBuilder errorMsg = new StringBuilder();
                for (String anErrorList : errorList) {
                    Trace.logStartup("** ERROR: - " + anErrorList);
                    startupMessages.add("- " + anErrorList);
                    errorMsg.append("<br>").append(anErrorList);
                }
                phoneHomeInfo.collectInfo("startuperror", errorMsg.toString());
                startupMessages.add("LabVantage failed to start - ALL CONNECTIONS WILL BE DISABLED");
                Trace.logStartup("** ALL CONNECTIONS WILL BE DISABLED");
                Configuration.setErrorMsg(errorMsg.substring(4));
            } else {
                Configuration.setState(0);
                Configuration.setErrorMsg("");
                started = true;
                startupMessages.add("LabVantage started" + (warningList.size() > 0 ? " with warnings" : ""));
                Trace.logStartup("** LabVantage Service Started");
                Trace.logStartup("****************************************************************************");
            }
            LogUtil.setDebugEnabled(configuration.getApplicationid(), configuration.getServerLogConfigFile());
            LogUtil.archiveStartupLog(configuration.getApplicationid(), configuration.getApplicationHome(), configuration.getServerLogConfigFile());
            phoneHomeInfo.collectInfo("startupdt", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(DateTimeUtil.getNowCalendar().getTime()));
            if (phoneHomeInfo.isPhoneHomeAllowed()) {
                SapphireService.callHomeAsync(phoneHomeInfo);
            }
            Policy.getPolicy();
            System.setSecurityManager(new LabVantageSecurityManager());
            Trace.logStartup("** LabVantageSecurityManager Initalized");
        }
        catch (Exception e) {
            Configuration.setErrorMsg(e.getMessage());
            Trace.logError(LOGNAME, (Object)("Unable to start LabVantage - " + e.getMessage()), e);
            if (phoneHomeInfo.isPhoneHomeAllowed()) {
                phoneHomeInfo.collectInfo("startuperror", e.getMessage());
                SapphireService.callHomeAsync(phoneHomeInfo);
            }
            throw new ServiceException("Unable to start LabVantage", e);
        }
        return startupMessages;
    }

    /*
     * WARNING - void declaration
     */
    public static ArrayList<String> startupDatabase(SapphireDatabase database) throws ServiceException {
        ArrayList<String> errorList = new ArrayList<String>();
        DBUtil db = new DBUtil();
        String LOGNAME = STARTUP;
        try {
            String xssmock;
            String hostname;
            String ignoreBuild;
            boolean isLicencePerDatabase;
            String databaseid = database.getDatabaseId();
            Configuration configuration = Configuration.getInstance();
            PhoneHomeInfo phoneHomeInfo = PhoneHomeInfo.getInstance();
            Trace.logStartup(databaseid, "****************************************************************************");
            Trace.logStartup(databaseid, "** Setting up LabVantage database: " + database);
            boolean isValidDatabase = true;
            boolean bl = isLicencePerDatabase = configuration.requireLicensePerDatabase() || database.getLicense() != null;
            if (isLicencePerDatabase) {
                String[][] modules;
                void var13_20;
                String[][] values;
                try {
                    if (database.getLicense() == null) {
                        throw new SapphireException("Database does not have license. Set up license in LabVantage Console for this Database.");
                    }
                    configuration.setLicense(database.getLicense(), database.getDatabaseId());
                    Trace.logStartup(LOGNAME, "License found in the database");
                }
                catch (SapphireException e) {
                    isValidDatabase = false;
                    String errormsg = "Failed to load license. Reason: " + e.getMessage();
                    Trace.logStartup(LOGNAME, errormsg);
                    errorList.add("ERROR: Could not load license for database '" + databaseid + "'");
                    ArrayList<String> arrayList = errorList;
                    db.reset();
                    db.releaseConnection();
                    return arrayList;
                }
                Trace.logStartup(LOGNAME, "** Loaded database specific license file:");
                Trace.logStartup(LOGNAME, "**************************************");
                Properties licenseProps = configuration.getLicense(database.getDatabaseId()).getProperties();
                String[][] stringArray = values = License.getLicenseProperties();
                int n = stringArray.length;
                boolean bl2 = false;
                while (var13_20 < n) {
                    String[] value = stringArray[var13_20];
                    Trace.logStartup(LOGNAME, "** - " + value[0] + "=" + licenseProps.getProperty(value[0], ""));
                    phoneHomeInfo.collectDatabaseInfo(databaseid, value[0], licenseProps.getProperty(value[0], ""));
                    if ("phonehome".equals(value[0]) && "N".equals(licenseProps.getProperty(value[0], ""))) {
                        phoneHomeInfo.setPhoneHomeAllowed(false);
                    }
                    if ("phonehomeurl".equals(value[0]) && licenseProps.getProperty(value[0], "").length() > 0) {
                        homeURL = licenseProps.getProperty(value[0]);
                    }
                    ++var13_20;
                }
                for (String[] module : modules = License.getModules()) {
                    Trace.logStartup(LOGNAME, "** - " + module[0] + "=" + licenseProps.getProperty("module_" + module[0], ""));
                    phoneHomeInfo.collectDatabaseInfo(databaseid, module[0], licenseProps.getProperty("module_" + module[0], ""));
                }
                for (String string : licenseProps.keySet()) {
                    if (!string.startsWith("sdc_")) continue;
                    Trace.logStartup(LOGNAME, "** - " + string + "=" + licenseProps.getProperty(string, ""));
                    phoneHomeInfo.collectDatabaseInfo(databaseid, string, licenseProps.getProperty(string, ""));
                }
                String sampleMonitoringLicenseCount = licenseProps.getProperty("module_SampleMonitoring", "");
                if (!(sampleMonitoringLicenseCount.isEmpty() || sampleMonitoringLicenseCount.equals("0") || sampleMonitoringLicenseCount.equals("S"))) {
                    Trace.logStartup(LOGNAME, "License key for SampleMonitoring module is incorrect. The license is not Site wide. Please contact support to get a new License key.");
                }
                Trace.logStartup(LOGNAME, "**************************************");
            }
            db.setConnection(database.getDbms(), ServiceLocator.getInstance().getDataSource(database.getJndiname()).getConnection());
            if ((configuration.getPlatform() == 5 || configuration.getPlatform() == 6) && db.getConnection().getAutoCommit()) {
                errorList.add("ERROR: AUTOCOMMIT is set to TRUE - check your datasource configuration for database '" + databaseid + "'");
                isValidDatabase = false;
            }
            Trace.logStartup(databaseid, "** JDBC driver        : " + db.getConnection().getMetaData().getDriverName());
            phoneHomeInfo.collectDatabaseInfo(databaseid, "jdbcdriver", db.getConnection().getMetaData().getDriverName());
            Trace.logStartup(databaseid, "** JDBC driver version: " + db.getConnection().getMetaData().getDriverVersion());
            phoneHomeInfo.collectDatabaseInfo(databaseid, "jdbcdriver", db.getConnection().getMetaData().getDriverVersion());
            SapphireConnection sapphireConnection = new SapphireConnection(db.getConnection(), database);
            SecurityService securityService = new SecurityService(sapphireConnection);
            configuration.getDatabaseWarnings(databaseid, errorList);
            boolean permitted = false;
            db.createResultSet("SELECT propertyvalue, propertyid FROM sysconfig WHERE lower( propertyid ) = 'permittedservers'");
            if (db.getNext()) {
                String[] servers;
                for (String server : servers = StringUtil.split(db.getString("propertyvalue"), ";")) {
                    if (!configuration.getServerHostName().equalsIgnoreCase(server)) continue;
                    permitted = true;
                    break;
                }
            } else {
                permitted = true;
            }
            if (!permitted) {
                throw new ServiceException("Server '" + configuration.getServerHostName() + "' not permitted for database '" + databaseid + "'");
            }
            String buildErrorMsg = "";
            db.createResultSet("SELECT propertyvalue, propertyid FROM sysconfig WHERE lower( propertyid ) = 'build'");
            ResultSetMetaData resultSetMetaData = db.getResultSet().getMetaData();
            int colBytes = resultSetMetaData.getColumnDisplaySize(2);
            int bytesPerChar = colBytes / 80;
            if (bytesPerChar < 2) {
                bytesPerChar = 1;
            }
            System.setProperty("sapphire.bytesperchar." + databaseid, String.valueOf(bytesPerChar));
            if (db.isOracle()) {
                try {
                    CallableStatement cs = db.prepareCall("{? = call lv_util.dbmajorversion() }");
                    cs.registerOutParameter(1, 2);
                    cs.executeUpdate();
                    System.setProperty("sapphire.oracle.version." + databaseid, String.valueOf(cs.getInt(1)));
                    db.closeCall();
                }
                catch (SQLException se) {
                    Trace.logStartup(databaseid, "** Unable to get database version. Exception: " + se.getMessage());
                }
            }
            if (db.isSqlServer()) {
                Installer.checkRCSOn(db.getConnection());
            }
            if ((ignoreBuild = ConfigService.getConfigProperty("com.labvantage.sapphire.server.ignorebuild")).length() == 0 || ignoreBuild.equals("false") || ignoreBuild.equals("N")) {
                if (db.getNext()) {
                    String build = db.getString("propertyvalue");
                    if (build != null && build.length() > 0) {
                        Trace.logStartup(databaseid, "** Database build: " + build);
                        if (!build.equals(Build.getBuild())) {
                            buildErrorMsg = "Build number incorrect in database '" + databaseid + "'";
                        }
                    } else {
                        buildErrorMsg = "Build details not correctly set in database '" + databaseid + "'";
                    }
                } else {
                    buildErrorMsg = "Build details not set in database '" + databaseid + "'";
                }
                if (buildErrorMsg.length() > 0) {
                    throw new SapphireException(buildErrorMsg);
                }
            } else {
                errorList.add("WARNING: IGNORING Database '" + databaseid + "' build  - NOT TO BE USED IN A PRODUCTION ENVIRONMENT");
                Trace.logStartup(databaseid, "** IGNORING Database '" + databaseid + "' build  - NOT TO BE USED IN A PRODUCTION ENVIRONMENT");
            }
            db.createResultSet("SELECT propertyvalue FROM sysconfig WHERE lower( propertyid ) = 'patch'");
            if (db.getNext()) {
                Trace.logStartup(databaseid, "** Database patch: " + db.getString("propertyvalue"));
            } else {
                Trace.logStartup(databaseid, "** Database patch: None");
            }
            if (configuration.isPrimaryServer()) {
                License license = configuration.getLicense(database.getDatabaseId());
                if (!SapphireService.validateLicenseUserCounts(true, true, errorList, db, databaseid, license)) {
                    isValidDatabase = false;
                }
                if (!SapphireService.validateLicenseSDMSInstrumentCounts(true, errorList, db, databaseid, license)) {
                    isValidDatabase = false;
                }
                if (!SapphireService.validateLicenseNonCoreExternalAppsCounts(true, errorList, db, databaseid, license)) {
                    isValidDatabase = false;
                }
                String concurrentHWMSql = "SELECT Max (connectioncreateddt) maxconnectioncreateddt, Max(concurrentusercount) maxconcurrentusercount FROM(  SELECT connectioncreateddt,concurrentusercount  FROM connectionlog  WHERE concurrentusercount =     ( SELECT Max(concurrentUserCount) FROM connectionlog ) ) inlinealias";
                db.createResultSet(concurrentHWMSql);
                if (db.getNext()) {
                    int concurrentHWM = db.getInt("maxconcurrentusercount");
                    String concurrentHWMDate = db.getValue("maxconnectioncreateddt");
                    Trace.logStartup(databaseid, "** Peak Concurrent Users: " + concurrentHWM + " on " + concurrentHWMDate);
                }
                String virtualHWMSql = "SELECT Max (connectioncreateddt) maxconnectioncreateddt, Max(concurrentvirtualusercount) maxconcurrentvirtualusercount FROM(  SELECT connectioncreateddt,concurrentvirtualusercount  FROM connectionlog  WHERE concurrentvirtualusercount =     ( SELECT Max(concurrentvirtualusercount) FROM connectionlog ) ) inlinealias";
                db.createResultSet(virtualHWMSql);
                if (db.getNext()) {
                    int virtualHWM = db.getInt("maxconcurrentvirtualusercount");
                    String virtualHWMDate = db.getValue("maxconnectioncreateddt");
                    Trace.logStartup(databaseid, "** Peak Concurrent Virtual Users: " + virtualHWM + " on " + virtualHWMDate);
                }
                String portalConcurrentHWMSql = "SELECT Max (connectioncreateddt) maxconnectioncreateddt, Max(concurrentportalusercount) concurrentportalusercount FROM(  SELECT connectioncreateddt,concurrentportalusercount  FROM connectionlog  WHERE concurrentportalusercount =     ( SELECT Max(concurrentportalusercount) FROM connectionlog ) ) inlinealias";
                db.createResultSet(portalConcurrentHWMSql);
                if (db.getNext()) {
                    int concurrentHWM = db.getInt("concurrentportalusercount");
                    String concurrentHWMDate = db.getValue("maxconnectioncreateddt");
                    Trace.logStartup(databaseid, "** Peak Concurrent Portal Users: " + concurrentHWM + " on " + concurrentHWMDate);
                }
            }
            ConfigService configService = new ConfigService(sapphireConnection);
            String ignoreTimeDiff = ConfigService.getConfigProperty("com.labvantage.sapphire.server.ignoretimediff");
            if (ignoreTimeDiff.length() == 0 || ignoreTimeDiff.equals("false") || ignoreTimeDiff.equals("N")) {
                Calendar nowServer = Calendar.getInstance();
                if (db.isOracle()) {
                    db.createResultSet("SELECT sysdate as dbdate FROM dual");
                } else {
                    db.createResultSet("SELECT GetDate() as dbdate");
                }
                Calendar nowDB = Calendar.getInstance();
                ResultSet rs = db.getResultSet();
                if (rs.next()) {
                    int maxtime;
                    int mintime;
                    nowDB.setTimeInMillis(rs.getTimestamp("dbdate").getTime());
                    long diffMinutes = (nowDB.getTimeInMillis() - nowServer.getTimeInMillis()) / 60000L;
                    try {
                        mintime = Integer.parseInt(ConfigService.getConfigProperty("com.labvantage.sapphire.server.mintimediff", "5"));
                    }
                    catch (Exception e1) {
                        mintime = 5;
                    }
                    try {
                        maxtime = Integer.parseInt(ConfigService.getConfigProperty("com.labvantage.sapphire.server.maxtimediff", "30"));
                    }
                    catch (Exception e1) {
                        maxtime = 30;
                    }
                    if (diffMinutes > (long)mintime && diffMinutes < (long)maxtime || diffMinutes < (long)(-1 * mintime) && diffMinutes > (long)(-1 * maxtime)) {
                        Trace.logWarn("Database server time differs by more than " + mintime + " minutes to server time. It is recommended to have date time synchronized between servers.");
                    } else if (diffMinutes > (long)maxtime || diffMinutes < (long)(-1 * maxtime)) {
                        errorList.add("ERROR: Database '" + databaseid + "' server time differs by more than " + maxtime + " minutes to server time. Please make sure times are synchronized between the servers.");
                        isValidDatabase = false;
                    } else {
                        Trace.logStartup(databaseid, "** Server date time synchronized");
                    }
                } else {
                    errorList.add("ERROR: Unable to check server time synchronization for database '" + databaseid + "'");
                    isValidDatabase = false;
                }
            } else {
                errorList.add("WARNING: IGNORING Database '" + databaseid + "' server time difference  - NOT TO BE USED IN A PRODUCTION ENVIRONMENT");
                Trace.logStartup(databaseid, "** IGNORING Database '" + databaseid + "' server time difference  - NOT TO BE USED IN A PRODUCTION ENVIRONMENT");
            }
            if (database.getDbms().equals("ORA")) {
                try {
                    CallableStatement cs = db.prepareCall("{? = call lv_version() }");
                    cs.registerOutParameter(1, 12);
                    cs.executeUpdate();
                    String build = cs.getString(1);
                    String[] builditems = StringUtil.split(build, ";");
                    Trace.logStartup(databaseid, "** Database procedure builds:");
                    for (String builditem : builditems) {
                        Trace.logStartup(databaseid, "**       " + builditem);
                    }
                    db.closeCall();
                }
                catch (SQLException se) {
                    Trace.logStartup(databaseid, "** Unable to get database procedure builds. Exception: " + se.getMessage());
                }
            }
            int timeout = Integer.parseInt(configService.getSysConfigProperty("SQLTimeout", "300"));
            DBUtil.setDefaultQueryTimeout(databaseid, timeout);
            Trace.logStartup(databaseid, "** Database default SQL statement timeout: " + timeout + " seconds");
            db.createResultSet("SELECT propertyvalue, propertyid FROM sysconfig WHERE propertyid = 'labvantageinternalkey'");
            String labvantageinternalkey = null;
            if (db.getNext()) {
                labvantageinternalkey = db.getString("propertyvalue");
            }
            boolean upgradeBOPasswordEncryption = false;
            if (labvantageinternalkey == null) {
                labvantageinternalkey = EncryptDecrypt.generateRandomAESKey();
                db.executePreparedUpdate("INSERT INTO sysconfig ( propertyid, propertyvalue ) values( 'labvantageinternalkey', ?)", labvantageinternalkey);
                upgradeBOPasswordEncryption = true;
            }
            EncryptDecrypt.setLVInternalKey(databaseid, labvantageinternalkey);
            if (upgradeBOPasswordEncryption) {
                EncryptDecrypt.upgradeBOPasswordEncryption(db, databaseid);
            }
            if ((hostname = configuration.getServerHostName()) != null && hostname.length() > 0) {
                Trace.logStartup(databaseid, "** Recording Startup Event into statsmonitorvalue");
                Timestamp now = DateTimeUtil.getNowTimestamp();
                if (db.getCount("SELECT count(*) FROM  statsmonitoritem WHERE statsmonitorgroupid='RESTART' AND activeflag='Y'") == 1) {
                    db.executePreparedUpdate("INSERT INTO statsmonitorvalue ( statsmonitorgroupid, statsmonitoritemid, hostname, capturemode, capturedt, capturevalue ) values ( ?, ?, ?, ?, ?, ? )", new Object[]{"RESTART", "RESTART", hostname, "RESTART", now, 0});
                }
            }
            Trace.logStartup(databaseid, "** Clearing old internal and authtoken connections");
            db.createPreparedResultSet("SELECT connectionid FROM connection WHERE ( tool = 'Internal' OR NullIf (authtokenid,'') is not null ) and lastaccesseddt < ?", new DateTimeUtil().getTimestamp("now-1d"));
            while (db.getNext()) {
                securityService.clearConnection(db.getString("connectionid"), false);
            }
            String callstmt = "{call lv_tdl" + (db.isOracle() ? "." : "_") + "hostreset( ?, ?, ? ) }";
            CallableStatement cs = db.prepareCall(callstmt);
            cs.setString(1, configuration.getHostid());
            cs.registerOutParameter(2, 12);
            cs.setString(3, "N");
            cs.executeUpdate();
            String status = cs.getString(2);
            if ("B".equals(status)) {
                errorList.add("WARNING: Failed to reset todolist entries for this host because one or more of the entries was locked.");
            }
            db.executeUpdate("UPDATE scheduleplanitemexec SET inprocessflag = 'N' where inprocessflag='Y'");
            String devmode = configService.getSysConfigProperty("devmode");
            Configuration.setDevmode(sapphireConnection.getDatabaseId(), devmode.equals("Y"));
            if (devmode.equals("Y")) {
                errorList.add("WARNING: DATABASE '" + databaseid + "' CONFIGURED IN DEVELOPMENT MODE  - NOT TO BE USED IN A PRODUCTION ENVIRONMENT");
                Trace.logStartup(databaseid, "** DATABASE '" + databaseid + "' CONFIGURED IN DEVELOPMENT MODE  - NOT TO BE USED IN A PRODUCTION ENVIRONMENT");
            }
            if (devmode.length() > 0) {
                configService.setSysConfigProperty("compcode", "");
            }
            if ("Y".equalsIgnoreCase(xssmock = ConfigService.getConfigProperty("com.labvantage.sapphire.server.xssmock")) || "true".equalsIgnoreCase(xssmock) || "A".equalsIgnoreCase(xssmock) || "AA".equalsIgnoreCase(xssmock)) {
                XSS.setMock(true);
                Trace.logStartup("***************************************************************************");
                Trace.logStartup("**                                                                       **");
                Trace.logStartup("** Running in XSS Mock mode - NOT TO BE USED IN A PRODUCTION ENVIRONMENT **");
                Trace.logStartup("**                                                                       **");
                Trace.logStartup("***************************************************************************");
                if ("A".equalsIgnoreCase(xssmock)) {
                    XSS.setShowAlerts(true);
                    Trace.logStartup("** XSS Mock Alert Enabled");
                }
                if ("AA".equalsIgnoreCase(xssmock)) {
                    XSS.setShowAlertsAlways(true);
                    Trace.logStartup("** XSS Mock Alert Always Enabled");
                }
                try {
                    db.createResultSet("SELECT count(*) from XSSLOG");
                }
                catch (Exception sqle) {
                    db.executeSQL("create table XSSLOG( VAL VARCHAR2(4000), EXTRA   VARCHAR2(4000), LASTURL VARCHAR2(4000), LOGDT   DATE)");
                }
                try {
                    db.createResultSet("whitepageset", "SELECT whitepageurl from XSSWHITEPAGELOG");
                    XSS.setWhitepageSet(new DataSet(db.getResultSet("whitepageset")));
                }
                catch (Exception sqle) {
                    db.executeSQL("create table XSSWHITEPAGELOG( WHITEPAGEURL VARCHAR2(4000), LOGDT   DATE)");
                }
            }
            String compcode = configService.getSysConfigProperty("compcode");
            Configuration.setCompcode(sapphireConnection.getDatabaseId(), compcode);
            if (compcode.length() > 0) {
                errorList.add("WARNING: DATABASE '" + databaseid + "' CONFIGURED IN COMPONENT DEVELOPMENT MODE  - NOT TO BE USED IN A PRODUCTION ENVIRONMENT");
                Trace.logStartup(databaseid, "** DATABASE '" + databaseid + "' CONFIGURED IN COMPONENT DEVELOPMENT MODE  - NOT TO BE USED IN A PRODUCTION ENVIRONMENT");
            }
            try {
                String sqlRegisterClassName = configService.getProfileProperty("(system)", "sqlregisterclass");
                if (sqlRegisterClassName != null && sqlRegisterClassName.length() > 0) {
                    BaseSQLRegister sqlRegister = (BaseSQLRegister)Class.forName(sqlRegisterClassName).newInstance();
                    sqlRegister.setDbms(database.getDbms());
                    SQLRegister.setSqlRegister(databaseid, sqlRegister);
                }
            }
            catch (Exception e) {
                errorList.add("WARNING: Error encountered creating SQL register for database '" + databaseid + "'. Calls requiring registered SQL will fail!");
            }
            if (isValidDatabase) {
                configuration.addSapphireDatabase(database);
            }
            Trace.logStartup(databaseid, "** Loading Portal SSO properties");
            try {
                LV_Portal.loadPortalSSOPropsToCache(db, "", databaseid);
            }
            catch (SapphireException e) {
                Trace.logError("Failed to load Portal SSO Properties for database: " + databaseid, e);
                errorList.add("WARNING: Error encountered loading Portal Properties '" + databaseid + "'. Portal SSO authentication features will fail!");
            }
            Trace.logStartup(databaseid, "****************************************************************************");
        }
        catch (Exception e) {
            throw new ServiceException(e);
        }
        finally {
            db.reset();
            db.releaseConnection();
        }
        return errorList;
    }

    public static boolean validateLicenseUserCounts(boolean updateModules, boolean isStartup, ArrayList<String> errorList, DBUtil db, String databaseid, License license) throws SapphireException {
        boolean isValidLicense = true;
        if (license.isLicenseExpired()) {
            errorList.add("ERROR: License is expired. Contact LabVantage for new License.");
            isValidLicense = false;
        } else {
            Properties moduleProps = license.getModuleProperties();
            Enumeration<?> e = moduleProps.propertyNames();
            while (e.hasMoreElements()) {
                String module = (String)e.nextElement();
                if (updateModules) {
                    db.executePreparedUpdate("UPDATE module SET licensedflag = 'Y', controlledflag = 'Y', maxusers = ? WHERE moduleid = ?", new Object[]{moduleProps.getProperty(module), module});
                    if (isStartup) {
                        Trace.logStartup(databaseid, "** Module licenses synchronized: " + module);
                    }
                }
                if ("U".equalsIgnoreCase(moduleProps.getProperty(module)) || "S".equalsIgnoreCase(moduleProps.getProperty(module)) || db.getPreparedCount("SELECT count(*) FROM modulesysuser, sysuser WHERE modulesysuser.sysuserid = sysuser.sysuserid AND ( sysuser.templateflag is null or sysuser.templateflag = 'N' ) AND ( sysuser.activeflag = 'Y' or sysuser.activeflag is null ) AND moduleid = ?", new Object[]{module}) <= Integer.parseInt(moduleProps.getProperty(module))) continue;
                errorList.add("ERROR: Module user assignment count exceeds license for module '" + module + "' in database '" + databaseid + "'");
                isValidLicense = false;
            }
            if (updateModules && isStartup) {
                Trace.logStartup(databaseid, "** Module licenses synchronized");
            }
            db.createResultSet("SELECT moduleid, licensedflag FROM module WHERE licensedflag = 'Y'");
            while (db.getNext()) {
                String moduleid = db.getString("moduleid");
                if (moduleProps.containsKey(moduleid)) continue;
                errorList.add("WARNING: License key does not contain an entry for module '" + moduleid + "' in database '" + databaseid + "'. You need a new license to activate this module.");
                if (db.getPreparedCount("SELECT count(*) FROM modulesysuser WHERE moduleid = ?", new Object[]{moduleid}) <= 0) continue;
                errorList.add("ERROR: Module user assignment count exceeds license for module '" + moduleid + "' in database '" + databaseid + "'");
                isValidLicense = false;
            }
            Properties appProps = license.getAppProperties();
            Enumeration<?> e2 = appProps.propertyNames();
            while (e2.hasMoreElements()) {
                String licenseAppId = (String)e2.nextElement();
                if (isStartup) {
                    Trace.logStartup(databaseid, "** App licenses check: " + licenseAppId);
                }
                String appLicenseCount = appProps.getProperty(licenseAppId);
                if (updateModules) {
                    db.executePreparedUpdate("UPDATE app SET licensedflag = 'Y', maxusers = ? WHERE appid = ?", new Object[]{appLicenseCount, licenseAppId});
                    if (isStartup) {
                        Trace.logStartup(databaseid, "** App licenses synchronized");
                    }
                }
                try {
                    LV_App.checkAppLicense(licenseAppId, appLicenseCount, db, null);
                }
                catch (SapphireException ex) {
                    if ("MaxAssignmentExceeded".equals(ex.getErrorid())) {
                        errorList.add("ERROR: App user assignment count exceeds license for App '" + licenseAppId + "' in database '" + databaseid + "'");
                        isValidLicense = false;
                        continue;
                    }
                    if ("UnRegisteredApp".equals(ex.getErrorid())) {
                        errorList.add("ERROR: Unregistered App found in License - '" + licenseAppId + "' in database '" + databaseid + "'");
                        isValidLicense = false;
                        continue;
                    }
                    throw ex;
                }
            }
            db.createResultSet("SELECT appid, licensedflag FROM app WHERE licensedflag = 'Y'");
            while (db.getNext()) {
                String appId = db.getString("appid");
                if (appProps.containsKey(appId)) continue;
                errorList.add("WARNING: License key does not contain an entry for App '" + appId + "' in database '" + databaseid + "'. You need a new license to activate this App.");
                try {
                    LV_App.checkAppLicense(appId, "0", db, null);
                }
                catch (SapphireException e3) {
                    if ("MaxAssignmentExceeded".equals(e3.getErrorid())) {
                        errorList.add("ERROR: App user assignment count exceeds license for App '" + appId + "' in database '" + databaseid + "'");
                        isValidLicense = false;
                        continue;
                    }
                    if ("UnRegisteredApp".equals(e3.getErrorid())) {
                        errorList.add("ERROR: Unregistered App found in License - '" + appId + "' in database '" + databaseid + "'");
                        isValidLicense = false;
                        continue;
                    }
                    throw e3;
                }
            }
            int namedUserCount = db.getPreparedCount("SELECT count(*) FROM sysuser WHERE templateflag!='Y' AND nameduserflag = ? AND ( disabledflag is null or disabledflag = 'N' )", new Object[]{"S"});
            int namedExpressUserCount = db.getPreparedCount("SELECT count(*) FROM sysuser WHERE templateflag!='Y' AND nameduserflag = ? AND ( disabledflag is null or disabledflag = 'N' )", new Object[]{"E"});
            int namedPortalUserCount = db.getPreparedCount("SELECT count(*) FROM sysuser WHERE templateflag!='Y' AND nameduserflag = ? AND ( disabledflag is null or disabledflag = 'N' )", new Object[]{"P"});
            int namedVirtualUserCount = db.getPreparedCount("SELECT count(*) FROM sysuser WHERE templateflag!='Y' AND nameduserflag = ? AND ( disabledflag is null or disabledflag = 'N' )", new Object[]{"V"});
            if (isStartup) {
                Trace.logStartup(databaseid, "** Named User Count: " + namedUserCount);
                Trace.logStartup(databaseid, "** Named Express User Count: " + namedExpressUserCount);
                Trace.logStartup(databaseid, "** Named Portal User Count: " + namedPortalUserCount);
                Trace.logStartup(databaseid, "** Named Virtual User Count: " + namedVirtualUserCount);
            }
            if (license.isExpress()) {
                if (db.getCount("SELECT count(*) FROM sysuser WHERE  templateflag!='Y' AND nameduserflag = 'S' AND ( disabledflag is null or disabledflag = 'N' )") > 1) {
                    errorList.add("ERROR: Named user count exceeds Express license in database '" + databaseid + "'");
                    isValidLicense = false;
                }
                if (db.getCount("SELECT count(*) FROM sysuser WHERE templateflag!='Y' AND nameduserflag = 'C' AND ( disabledflag is null or disabledflag = 'N' )") > 0) {
                    errorList.add("ERROR: Concurrent users are not allowed in an Express license in database '" + databaseid + "'");
                    isValidLicense = false;
                }
                if (isStartup) {
                    Trace.logStartup(databaseid, "** Express license checked");
                }
            }
        }
        return isValidLicense;
    }

    public static boolean validateLicenseSDMSInstrumentCounts(boolean isStartup, ArrayList<String> errorList, DBUtil db, String databaseid, License license) throws SapphireException {
        boolean isValidLicense = true;
        int maxSDMSInstrumentCounts = license.getSDMSInstrumentCount();
        db.createResultSet("SELECT count(*) count FROM instrument WHERE ( ACTIVEFLAG = 'Y' OR ACTIVEFLAG IS NULL ) AND sdmscollectorid is not null");
        int sdmsInstrumentCounts = 0;
        if (db.getNext()) {
            sdmsInstrumentCounts = db.getInt("count");
        }
        if (sdmsInstrumentCounts > maxSDMSInstrumentCounts) {
            errorList.add("ERROR: SDMS Instrument count exceeds license SDMS Instrument count in database '" + databaseid + "'");
            isValidLicense = false;
        }
        if (isStartup) {
            Trace.logStartup(databaseid, "** SDMS Instrument Count: " + sdmsInstrumentCounts);
        }
        return isValidLicense;
    }

    public static boolean validateLicenseNonCoreExternalAppsCounts(boolean isStartup, ArrayList<String> errorList, DBUtil db, String databaseid, License license) throws SapphireException {
        boolean isValidLicense = true;
        int maxNonCoreExternalAppsCounts = license.getNonCoreExternalAppCount();
        db.createResultSet("SELECT count(*) count FROM externalapp WHERE ( activeflag = 'Y' OR activeflag IS NULL ) AND (coreflag = 'N' OR coreflag IS NULL)");
        int nonCoreExternalAppsCounts = 0;
        if (db.getNext()) {
            nonCoreExternalAppsCounts = db.getInt("count");
        }
        if (nonCoreExternalAppsCounts > maxNonCoreExternalAppsCounts) {
            errorList.add("ERROR: Non-core External app count exceeds license Non-core External app count in database '" + databaseid + "'");
            isValidLicense = false;
        }
        if (isStartup) {
            Trace.logStartup(databaseid, "** Non-core External App Count: " + nonCoreExternalAppsCounts);
        }
        return isValidLicense;
    }

    private static void loadImagesAsync(final SapphireConnection sapphireConnection) {
        ThreadUtil.LVThreadFactory threadFactory = ThreadUtil.getThreadFactory("ss-imageloading-" + sapphireConnection.getDatabaseId());
        Executors.newSingleThreadExecutor(threadFactory).submit(new Runnable(){

            @Override
            public void run() {
                ImageRef.getImages(0, -1, sapphireConnection);
            }
        });
    }

    private static void callHomeAsync(final PhoneHomeInfo phoneHomeInfo) {
        try {
            if (ConfigService.getConfigProperty("com.labvantage.sapphire.server.homeurl").length() > 0) {
                homeURL = ConfigService.getConfigProperty("com.labvantage.sapphire.server.homeurl");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        ThreadUtil.LVThreadFactory threadFactory = ThreadUtil.getThreadFactory("ss-phonehome");
        Executors.newSingleThreadExecutor(threadFactory).submit(new Runnable(){

            @Override
            public void run() {
                SapphireService.callHome(phoneHomeInfo);
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void callHome(PhoneHomeInfo phoneHomeInfo) {
        Trace.logStartup("************************************Call Home now");
        HttpURLConnection conn = null;
        HostnameVerifier defaulthostNameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
        SSLSocketFactory sslSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HostnameVerifier vcValid = new HostnameVerifier(){

                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            URL url = new URL(homeURL);
            conn = (HttpURLConnection)url.openConnection();
            if (conn instanceof HttpsURLConnection) {
                HttpsURLConnection.setDefaultHostnameVerifier(vcValid);
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            }
            conn.setConnectTimeout(60000);
            PhoneHomeInfo.lastInfoSend = phoneHomeInfo.getInfo().toJSONString();
            String urlParameters = "phonehomeinfo=" + PhoneHomeInfo.lastInfoSend;
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;
            conn = (HttpURLConnection)url.openConnection();
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            conn.setUseCaches(false);
            try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream());){
                wr.write(postData);
            }
            int code = conn.getResponseCode();
            if (code == 200) {
                Trace.logStartup("Phone Home Succeeded.");
            } else {
                Trace.logStartup("Failed to Phone Home with response code: " + code);
            }
            conn.disconnect();
        }
        catch (Throwable e) {
            Trace.logStartup("Failed to phone home:" + e.getMessage());
        }
        finally {
            HttpsURLConnection.setDefaultHostnameVerifier(defaulthostNameVerifier);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
            if (conn != null) {
                conn.disconnect();
            }
        }
        Trace.logStartup("************************************Done Call Home");
    }

    public static void setStarted(boolean started) {
        SapphireService.started = started;
    }

    public static boolean isStarted() {
        return started;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void resetCache() {
        try {
            Configuration configuration = Configuration.getInstance();
            List<SapphireDatabase> databases = configuration.getSapphireDatabases();
            Iterator<SapphireDatabase> iterator = databases.iterator();
            while (iterator.hasNext()) {
                DBUtil db = new DBUtil();
                String databaseid = "";
                try {
                    SapphireDatabase sapphireDatabase = iterator.next();
                    databaseid = sapphireDatabase.getDatabaseId();
                    db.setConnection(sapphireDatabase.getDbms(), ServiceLocator.getInstance().getDataSource(sapphireDatabase.getJndiname()).getConnection());
                    CacheUtil.resetCache(databaseid, true);
                }
                catch (Exception e) {
                    Trace.logError("Failed to clear system caches for " + databaseid, e);
                }
                finally {
                    db.reset();
                    db.releaseConnection();
                }
            }
        }
        catch (Exception e) {
            Trace.logError("Failed to clear system caches", e);
        }
        ConfigService.resetCache();
        DOMUtil.cache.clear();
    }

    public static void startAutomation() throws ServiceException {
        Trace.logStartup("****************************************************************************");
        Trace.logStartup("** Starting automation");
        try {
            Configuration configuration = Configuration.getInstance();
            if (configuration.isCluster() && configuration.isPrimaryServer()) {
                Trace.logStartup("** Primary server in cluster started");
            }
            configuration.setPrimaryStarted(true);
            Trace.logStartup("** Automation events will be generated from this server (" + configuration.getServerHostName() + ")");
            List<SapphireDatabase> databases = configuration.getSapphireDatabases();
            for (SapphireDatabase sapphireDatabase : databases) {
                try {
                    SapphireManagerLocal sapphireManager = ServiceLocator.getInstance().getSapphireManager();
                    sapphireManager.startDatabaseAutomation(sapphireDatabase);
                }
                catch (Exception e) {
                    Trace.logError("Failed to initialize automation tasks.", e);
                }
            }
            Trace.logStartup("** Automation setup complete. Messages will be started soon.");
            Trace.logStartup("****************************************************************************");
        }
        catch (SapphireException e) {
            throw new ServiceException("Failed to start automation", e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void startDatabaseAutomation(SapphireDatabase database) throws ServiceException {
        try {
            String databaseid = database.getDatabaseId();
            Trace.logStartup(databaseid, "** Starting up automation on database " + databaseid);
            ConnectionProcessor cp = new ConnectionProcessor();
            HashMap<String, String> options = new HashMap<String, String>();
            options.put("tool", INTERNAL_CONNECTIONID_TOOL);
            String connectionid = cp.getConnectionid(databaseid, "(system)", systemPassword, options);
            internalConnectionidCache.put(databaseid, connectionid);
            ConfigurationProcessor configProcessor = new ConfigurationProcessor(connectionid);
            AutomationProcessor automationProcessor = new AutomationProcessor(connectionid);
            SecurityPolicyUtil.setDatabaseSecurityPolicy(connectionid, databaseid);
            BaseNameSpaceHandler.setDatabaseRESTPolicy(connectionid, databaseid);
            if (configProcessor.getConfigProperty("com.labvantage.sapphire.server.loadcache").equals("true")) {
                Trace.logStartup(databaseid, "** Loading Web Page Designer Cache");
                automationProcessor.loadWebPageCache(connectionid);
            }
            UnitsUtil.popupulateUnitConversationCache(connectionid, databaseid);
            EventManager.loadEventPlans(cp.getSapphireConnection());
            DBUtil db = new DBUtil();
            try {
                Trace.logStartup(databaseid, "** Importing data");
                db.setConnection(database.getDbms(), ServiceLocator.getInstance().getDataSource(database.getJndiname()).getConnection());
                String[] configFilenames = new String[]{"worksheetconfigdata.zip"};
                String[] exampleDataFilenames = new String[]{"worksheetexampledata.zip"};
                for (String configFilename : configFilenames) {
                    SapphireService.importFile(connectionid, db, configFilename, true);
                }
                boolean isExampleDataInstalled = db.checkExists("SELECT propertyid FROM sysconfig WHERE propertyid = 'exampledataimported' and propertyvalue = 'Y'");
                if (isExampleDataInstalled) {
                    db.executeUpdate("DELETE FROM sysconfig WHERE propertyid='exampledataimported'");
                    for (String exampleDataFilename : exampleDataFilenames) {
                        SapphireService.importFile(connectionid, db, exampleDataFilename, true);
                    }
                }
                db.createResultSet("SELECT propertyid, propertyvalue FROM sysconfig WHERE propertyid like '%componentdataimport%'");
                HashMap<String, String> importFiles = new HashMap<String, String>();
                while (db.getNext()) {
                    String fileName = db.getString("propertyvalue");
                    String propertyId = db.getString("propertyid");
                    importFiles.put(propertyId, fileName);
                }
                for (Map.Entry entry : importFiles.entrySet()) {
                    String propertyId = (String)entry.getKey();
                    String fileName = (String)entry.getValue();
                    boolean success = SapphireService.importFile(connectionid, db, fileName, false);
                    if (!success) continue;
                    db.executePreparedUpdate("DELETE FROM sysconfig WHERE propertyid = ?", new Object[]{propertyId});
                }
            }
            catch (Exception e) {
                Trace.logError(LOGNAME, (Object)("** Error encountered importing data into database '" + database.getDatabaseId() + "'."), e);
            }
            finally {
                db.reset();
                db.releaseConnection();
            }
            ArrayList<Browser.GUIMode> guiModes = Browser.getGUIModes(new sapphire.accessor.ConfigurationProcessor(connectionid));
            CacheUtil.put(databaseid, "GUIModes", "GUIModes", guiModes);
            Trace.logStartup(databaseid, "** Starting LAM");
            LAM lam = automationProcessor.createLAM();
            SapphireService.configureAutomation(lam, cp, database);
            SapphireService.loadImagesAsync(cp.getSapphireConnection());
            Trace.logStartup(databaseid, "** Images loaded from ImageRef");
        }
        catch (Exception e) {
            throw new ServiceException("Failed to start automation on database '" + database.getDatabaseId() + "'. Reason: " + e.getMessage(), e);
        }
    }

    private static boolean importFile(String connectionid, DBUtil db, String fileName, boolean isBuildSpecific) throws SapphireException {
        boolean success = false;
        File file = new File(Configuration.getInstance().getSapphireHome(), "console/install/database/data/" + fileName);
        if (file.exists()) {
            if (!isBuildSpecific || !db.checkPreparedExists("SELECT propertyid FROM sysconfig WHERE propertyid = ?", new Object[]{Build.getBuild() + "-" + fileName})) {
                db.executePreparedUpdate("DELETE FROM sysconfig WHERE propertyid like ?", new Object[]{"%-" + fileName});
                Trace.logStartup("** Importing " + fileName + " data");
                ImportXML importData = new ImportXML();
                importData.setDatabase(db);
                importData.addImport(file);
                importData.setCommitScope("off");
                importData.setConnectionid(connectionid);
                importData.importFiles();
                db.executePreparedUpdate("INSERT INTO sysconfig (propertyid, propertyvalue) VALUES ( ?, 'imported')", new Object[]{Build.getBuild() + "-" + fileName});
                success = true;
            }
        } else {
            Trace.logError(LOGNAME, "** Data importfile '" + file.getAbsolutePath() + "' not found during startup.");
        }
        return success;
    }

    public static void configureAutomation(LAM lam, ConnectionProcessor cp, SapphireDatabase database) throws LAMException, SapphireException {
        String schedulerFrequency;
        String taskFrequency;
        lam.createTDLPool(cp.getSysConfigProperty("aappoolsize"), 5);
        lam.createPollerPool(cp.getSysConfigProperty("pollerpoolsize"), 3);
        lam.setStartupDelay(5);
        PropertyList SDMSPolicy = new sapphire.accessor.ConfigurationProcessor(cp.getConnectionid()).getPolicy("SDMSPolicy", "Sapphire Custom");
        int internalCollectorBalancing = Integer.parseInt(SDMSPolicy.getPropertyListNotNull("automationsettings").getProperty("internalcollectorbalancing", "60"));
        int importDataCaptureFolders = Integer.parseInt(SDMSPolicy.getPropertyListNotNull("automationsettings").getProperty("importdatacapturefolders", "20"));
        lam.createAnyAutomationPoller(13, "" + internalCollectorBalancing, internalCollectorBalancing);
        lam.createAnyAutomationPoller(2, cp.getSysConfigProperty("todolistpoll"), 1);
        int bufferMillis = LAM.getConfigInt(cp.getSysConfigProperty("todolistbuffermillis"), 100);
        lam.setToDoListBufferMillis(bufferMillis);
        lam.createPoller(3, cp.getSysConfigProperty("statsmonitorhourly"), 3600);
        lam.createPoller(4, cp.getSysConfigProperty("statsmonitordaily"), 86400);
        lam.createPoller(10, cp.getSysConfigProperty("sendservercommand"), 1);
        lam.createPoller(11, cp.getSysConfigProperty("processservercommand"), 3);
        ServerPing serverPing = (ServerPing)lam.createPoller(1, cp.getSysConfigProperty("serverheartbeat"), 10);
        if (serverPing != null) {
            int serverLatency = LAM.getConfigInt(cp.getSysConfigProperty("serverlatency"), 30);
            serverPing.setServerLatency(serverLatency);
        }
        if (!(taskFrequency = cp.getSysConfigProperty("taskpoll")).equals("0")) {
            lam.createPrimaryAutomationPoller(5, taskFrequency, 60);
        }
        if (!(schedulerFrequency = cp.getSysConfigProperty("schedulepoll")).equals("0")) {
            lam.createPrimaryAutomationPoller(6, schedulerFrequency, 3600);
        }
        lam.createPrimaryAutomationPoller(7, cp.getSysConfigProperty("timeoutpoll"), 60);
        lam.createPrimaryAutomationPoller(8, cp.getSysConfigProperty("hktpoll"), 86400);
        lam.createPrimaryAutomationPoller(14, "" + internalCollectorBalancing, internalCollectorBalancing);
        lam.createPrimaryAutomationPoller(12, "" + importDataCaptureFolders, importDataCaptureFolders);
        try {
            Indexer indexer = Indexer.createInstance(cp.getConnectionid(), database, Configuration.getInstance());
            if (indexer.isIndexing()) {
                if (!indexer.isRemoteIndexer()) {
                    lam.createPrimaryAutomationPoller(9, String.valueOf(indexer.getPollingInterval()), indexer.getPollingInterval());
                } else {
                    Trace.logStartup(database.getDatabaseId(), "** Indexer configured to start remotely");
                }
            }
        }
        catch (Exception e) {
            Trace.logError("STARTUP-LAM", (Object)"Failed to start Indexer", e);
        }
    }

    public static void stopAutomation() throws ServiceException {
        AutomationService.stopAllLams();
    }

    public static HashMap processCommand(HashMap<String, Object> commandProps) throws ServiceException {
        String command = (String)commandProps.get("command");
        if (command != null && command.length() > 0) {
            if (command.equalsIgnoreCase(COMMAND_GETLOGGERFILES)) {
                try {
                    ArrayList loggerFiles = Configuration.getInstance().getLoggerFiles();
                    StringBuilder files = new StringBuilder();
                    StringBuilder lengths = new StringBuilder();
                    for (Object loggerFile : loggerFiles) {
                        File file = new File((String)loggerFile);
                        lengths.append(";").append(file.length());
                        files.append(";").append((String)loggerFile);
                    }
                    commandProps.put("loggerfiles", files.length() > 0 ? files.substring(1) : "");
                    commandProps.put("loggerfilelengths", lengths.length() > 0 ? lengths.substring(1) : "");
                }
                catch (SapphireException e) {
                    throw new ServiceException("Failed to get logger files. Reason: " + e.getMessage());
                }
            } else if (command.equalsIgnoreCase(COMMAND_LOADEVENTPLANS)) {
                ConnectionProcessor cp = new ConnectionProcessor();
                cp.getConnectionid((String)commandProps.get("databaseid"), (String)commandProps.get("userid"), (String)commandProps.get("password"));
                EventManager.loadEventPlans(cp.getSapphireConnection(), (String)commandProps.get("querywhere"));
                String testMode = (String)commandProps.get("testmode");
                if (testMode != null && testMode.equals("Y")) {
                    EventManager.setTestMode();
                }
            } else if (command.equalsIgnoreCase(COMMAND_INDEX)) {
                try {
                    ConnectionProcessor cp = new ConnectionProcessor();
                    cp.getConnectionid((String)commandProps.get("databaseid"), (String)commandProps.get("userid"), (String)commandProps.get("password"));
                    IndexToolsPropertyHandler handler = new IndexToolsPropertyHandler();
                    handler.setSapphireConnection(cp.getSapphireConnection());
                    commandProps.put("dothis", COMMAND_INDEX);
                    ((PropertyHandler)handler).processProperties(commandProps);
                }
                catch (Exception e) {
                    throw new ServiceException(e.getMessage());
                }
            } else if (command.equalsIgnoreCase(COMMAND_INDEXPENDING)) {
                try {
                    ConnectionProcessor cp = new ConnectionProcessor();
                    cp.getConnectionid((String)commandProps.get("databaseid"), (String)commandProps.get("userid"), (String)commandProps.get("password"));
                    IndexToolsPropertyHandler handler = new IndexToolsPropertyHandler();
                    handler.setSapphireConnection(cp.getSapphireConnection());
                    commandProps.put("dothis", COMMAND_INDEXPENDING);
                    ((PropertyHandler)handler).processProperties(commandProps);
                }
                catch (Exception e) {
                    throw new ServiceException(e.getMessage());
                }
            } else if (command.equalsIgnoreCase(COMMAND_RESETINDEX)) {
                try {
                    ConnectionProcessor cp = new ConnectionProcessor();
                    cp.getConnectionid((String)commandProps.get("databaseid"), (String)commandProps.get("userid"), (String)commandProps.get("password"));
                    IndexToolsPropertyHandler handler = new IndexToolsPropertyHandler();
                    handler.setSapphireConnection(cp.getSapphireConnection());
                    commandProps.put("dothis", "reset");
                    ((PropertyHandler)handler).processProperties(commandProps);
                }
                catch (Exception e) {
                    throw new ServiceException(e.getMessage());
                }
            } else if (command.equalsIgnoreCase(COMMAND_RESETPENDINGINDEX)) {
                try {
                    ConnectionProcessor cp = new ConnectionProcessor();
                    cp.getConnectionid((String)commandProps.get("databaseid"), (String)commandProps.get("userid"), (String)commandProps.get("password"));
                    IndexToolsPropertyHandler handler = new IndexToolsPropertyHandler();
                    handler.setSapphireConnection(cp.getSapphireConnection());
                    commandProps.put("dothis", "resetpending");
                    ((PropertyHandler)handler).processProperties(commandProps);
                }
                catch (Exception e) {
                    throw new ServiceException(e.getMessage());
                }
            } else if (command.equalsIgnoreCase(COMMAND_RESETINDEXMAP)) {
                try {
                    ConnectionProcessor cp = new ConnectionProcessor();
                    cp.getConnectionid((String)commandProps.get("databaseid"), (String)commandProps.get("userid"), (String)commandProps.get("password"));
                    IndexToolsPropertyHandler handler = new IndexToolsPropertyHandler();
                    handler.setSapphireConnection(cp.getSapphireConnection());
                    commandProps.put("dothis", "resetmap");
                    ((PropertyHandler)handler).processProperties(commandProps);
                }
                catch (Exception e) {
                    throw new ServiceException(e.getMessage());
                }
            } else if (command.equalsIgnoreCase(COMMAND_STARTINDEXING)) {
                try {
                    ConnectionProcessor cp = new ConnectionProcessor();
                    cp.getConnectionid((String)commandProps.get("databaseid"), (String)commandProps.get("userid"), (String)commandProps.get("password"));
                    IndexToolsPropertyHandler handler = new IndexToolsPropertyHandler();
                    handler.setSapphireConnection(cp.getSapphireConnection());
                    commandProps.put("dothis", "start");
                    ((PropertyHandler)handler).processProperties(commandProps);
                }
                catch (Exception e) {
                    throw new ServiceException(e.getMessage());
                }
            } else if (command.equalsIgnoreCase(COMMAND_STOPINDEXING)) {
                try {
                    ConnectionProcessor cp = new ConnectionProcessor();
                    cp.getConnectionid((String)commandProps.get("databaseid"), (String)commandProps.get("userid"), (String)commandProps.get("password"));
                    IndexToolsPropertyHandler handler = new IndexToolsPropertyHandler();
                    handler.setSapphireConnection(cp.getSapphireConnection());
                    commandProps.put("dothis", "stop");
                    ((PropertyHandler)handler).processProperties(commandProps);
                }
                catch (Exception e) {
                    throw new ServiceException(e.getMessage());
                }
            } else if (command.equalsIgnoreCase(COMMAND_GETINDEXINGSTATUS)) {
                try {
                    Indexer indexer = Indexer.getInstance((String)commandProps.get("databaseid"));
                    commandProps.put(RETURN_INDEXINGSTATUS, indexer.getCurrentIndexingStatus());
                }
                catch (Exception e) {
                    throw new ServiceException(e.getMessage());
                }
            } else if (command.equalsIgnoreCase(COMMAND_SEARCH)) {
                try {
                    ConnectionProcessor cp = new ConnectionProcessor();
                    cp.getConnectionid((String)commandProps.get("databaseid"), (String)commandProps.get("userid"), (String)commandProps.get("password"));
                    Searcher searcher = new Searcher(cp.getSapphireConnection());
                    SearchRequest searchRequest = new SearchRequest((String)commandProps.get("searchquery"));
                    SearchResults results = searcher.getSearchResults(searchRequest);
                    commandProps.put(RETURN_NUMOFHITS, results.getTotalHits());
                }
                catch (Exception e) {
                    throw new ServiceException(e.getMessage());
                }
            } else if (command.equalsIgnoreCase(COMMAND_CRAWLDATABASE)) {
                try {
                    Indexer indexer = Indexer.getInstance((String)commandProps.get("databaseid"));
                    String testmode = (String)commandProps.get("testmode");
                    indexer.crawlDatabase(testmode != null && testmode.equals("Y"));
                }
                catch (SapphireException e) {
                    throw new ServiceException("Failed to crawl database" + e.getMessage());
                }
            } else if (command.equalsIgnoreCase(COMMAND_CREATEBACKLOG)) {
                try {
                    Indexer indexer = Indexer.getInstance((String)commandProps.get("databaseid"));
                    indexer.createBacklog((String)commandProps.get("sdcid"), (String)commandProps.get("aftercreatedt"), (String)commandProps.get("extendedwhere"), Integer.parseInt((String)commandProps.get("rowcount")), (String)commandProps.get("orderby"));
                }
                catch (SapphireException e) {
                    throw new ServiceException("Failed to crawl database" + e.getMessage());
                }
            } else if (command.equalsIgnoreCase(COMMAND_INDEXBACKLOG)) {
                try {
                    Indexer indexer = Indexer.getInstance((String)commandProps.get("databaseid"));
                    indexer.processBacklog(Integer.parseInt((String)commandProps.get("rowcount")));
                }
                catch (SapphireException e) {
                    throw new ServiceException("Failed to crawl database" + e.getMessage());
                }
            } else if (command.equalsIgnoreCase(COMMAND_RESETCACHE)) {
                try {
                    String databaseid = (String)commandProps.get("databaseid");
                    CacheUtil.resetCache(databaseid, false);
                    ConfigService.resetCache();
                    DOMUtil.cache.clear();
                    String connectionid = SapphireService.getInternalConnectionid(databaseid);
                    SecurityPolicyUtil.setDatabaseSecurityPolicy(connectionid, databaseid);
                    BaseNameSpaceHandler.setDatabaseRESTPolicy(connectionid, databaseid);
                }
                catch (Exception e) {
                    throw new ServiceException("Failed to reset cache for database" + commandProps.get("databaseid") + ". Reason: " + e.getMessage(), e);
                }
            } else if (command.equalsIgnoreCase(COMMAND_UPDATEUNITCONVERSIONCACHE)) {
                String databaseid = (String)commandProps.get("databaseid");
                String unitsid = (String)commandProps.get("unitsid");
                QueryProcessor qp = new QueryProcessor(SapphireService.getInternalConnectionid(databaseid));
                UnitsUtil.updateUnitConversationCache(databaseid, qp, unitsid);
            } else if (command.equalsIgnoreCase(COMMAND_FLUSHCALENDAR)) {
                String databaseid = (String)commandProps.get("databaseid");
                String calendarid = (String)commandProps.get("calendarid");
                CacheUtil.remove(databaseid, "Calendar", calendarid);
            }
        } else {
            throw new ServiceException("No command specified");
        }
        return commandProps;
    }

    public static String getInternalConnectionid(String databaseid) {
        return internalConnectionidCache.get(databaseid);
    }

    public static String getInternalConnectionIdForToken(String token) throws SapphireException {
        String connectionid = null;
        String databaseid = SapphireService.getDatabaseForToken(token);
        if (databaseid != null) {
            connectionid = SapphireService.getInternalConnectionid(databaseid);
        }
        if (connectionid == null || connectionid.length() == 0) {
            throw new SapphireException("Unable to recognize token " + token + ". Please contact your System Administrator for assistance");
        }
        return connectionid;
    }

    public static String getDatabaseForToken(String token) {
        String tokend = EncryptDecrypt.decrypt(token);
        int pos = tokend.indexOf("|");
        if (pos > 0) {
            return tokend.substring(0, pos);
        }
        return null;
    }

    static {
        started = false;
        homeURL = "";
        internalConnectionidCache = new HashMap();
    }
}

