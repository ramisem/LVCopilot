/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.ServletOutputStream
 *  javax.servlet.ServletRequest
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.jsp.JspWriter
 *  javax.xml.rpc.ServiceException
 *  org.apache.axis.client.Call
 *  org.apache.axis.client.Service
 *  org.apache.logging.log4j.core.config.Configurator
 */
package com.labvantage.sapphire.servlet;

import com.labvantage.sapphire.Build;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.License;
import com.labvantage.sapphire.RemoteAccessKey;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.install.PackagerFile;
import com.labvantage.sapphire.ajax.operations.ClientTranslation;
import com.labvantage.sapphire.pageelements.dynamicmaint.util.Utils;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.platform.jboss.JBoss;
import com.labvantage.sapphire.platform.jboss.JBoss6xConfiguration;
import com.labvantage.sapphire.platform.jboss.JBossConfiguration;
import com.labvantage.sapphire.services.SapphireService;
import com.labvantage.sapphire.servlet.BaseController;
import com.labvantage.sapphire.tagext.LogonPageTag;
import com.labvantage.sapphire.util.ant.AntInstallListener;
import com.labvantage.sapphire.util.ant.AntUtil;
import com.labvantage.sapphire.util.file.ConsoleFileOperationProgress;
import com.labvantage.sapphire.util.file.ZipFileUtil;
import com.labvantage.sapphire.util.json.JSONUtil;
import com.labvantage.sapphire.util.logger.LogConfig;
import com.labvantage.sapphire.util.logger.LogUtil;
import com.labvantage.sapphire.xml.BaseConverter;
import com.labvantage.sapphire.xml.ImportXML;
import com.labvantage.sapphire.xml.Logger;
import com.labvantage.sapphire.xml.PatchXML;
import com.labvantage.sapphire.xml.SqlProcessor;
import com.labvantage.sapphire.xml.StringLogger;
import com.labvantage.sapphire.xml.TransferPackage;
import com.labvantage.sapphire.xml.ant.GetJarListTask;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;
import javax.sql.DataSource;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.logging.log4j.core.config.Configurator;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import sapphire.SapphireException;
import sapphire.util.Browser;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.StringUtil;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ConsoleController
extends BaseController {
    public static final String PRODUCT_NAME = "LabVantage Console";
    public static final String PRODUCT_HOME = "LabVantage".toUpperCase() + "_HOME";
    private static final String LOGON_COOKIE = "lvc";
    public static final String FILE_APPLICATIONBUILDPROPS = "application-build.props";
    public static final String STANDARD_APPLICATIONID = "labvantage";
    public static final String STANDARD_EAR = "labvantage.ear";
    public static final String STANDARD_JAR = "sapphire.jar";
    public static final String STANDARD_WAR = "labvantage.war";
    public static final String STANDARD_WEBCONTEXT = "labvantage";
    public static final String STANDARD_WEBDIR = "WEB-CORE";
    private static final String LOGGER = "labvantageconsole";
    private static DataSet transTable;

    public void init() throws ServletException {
        super.init();
        this.getServletContext().log(PRODUCT_NAME.toUpperCase() + ": Servlet startup (Version: " + Build.getVersion() + " Build: " + Build.getBuild() + " Build Date: " + Build.getBuildDate() + ")");
        try {
            String serverid = System.getProperty("LABVANTAGE_SERVER");
            if (serverid == null) {
                serverid = "";
            }
            this.getServletContext().log(PRODUCT_NAME.toUpperCase() + ": labvantagehome=" + Configuration.getSapphireHome(this.getServletContext()));
            this.getServletContext().log(PRODUCT_NAME.toUpperCase() + ": serverinfo=" + this.getServletContext().getServerInfo());
            this.getServletContext().log(PRODUCT_NAME.toUpperCase() + ": hostname=" + InetAddress.getLocalHost().getHostName());
            this.getServletContext().log(PRODUCT_NAME.toUpperCase() + ": serverid=" + serverid);
            String sapphireHome = Configuration.getSapphireHome(this.getServletContext());
            ConsoleController.initConsoleLoggingProps(serverid, sapphireHome);
            Trace.setLoggerPrefix(LOGGER);
            Trace.logInfo(LOGGER, "LabVantage Console Started");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void initConsoleLoggingProps(String serverid, String sapphireHome) throws IOException {
        if (sapphireHome != null && sapphireHome.length() > 0 && new File(sapphireHome, "console").exists()) {
            File logConfig = new File(sapphireHome, "console/consolelogging" + (serverid.length() > 0 ? "_" + serverid : "") + ".properties");
            if (!logConfig.exists()) {
                try (FileOutputStream fos = new FileOutputStream(logConfig);){
                    PrintStream out = new PrintStream(fos);
                    File logFile = new File(sapphireHome, "console/logs/labvantageconsole" + (serverid.length() > 0 ? "_" + serverid : "") + ".log");
                    out.println("#LabVantage Console Logging (Log4J2) Properties");
                    out.println("appender.labvantageconsole.type=File");
                    out.println("appender.labvantageconsole.name=FileAppender");
                    out.println("appender.labvantageconsole.fileName=" + StringUtil.replaceAll(logFile.getAbsolutePath(), "\\", "\\\\"));
                    out.println("appender.labvantageconsole.layout.type=PatternLayout");
                    out.println("appender.labvantageconsole.layout.pattern=%-23d [%t] %-5p %-20c %x - %m%n");
                    out.println("");
                    out.println("logger.labvantageconsole.name=labvantageconsole");
                    out.println("logger.labvantageconsole.level=INFO");
                    out.println("logger.labvantageconsole.additivity=false");
                    out.println("logger.labvantageconsole.appenderRef.labvantageconsole.ref=FileAppender");
                    out.close();
                }
                File oldLogConfig = new File(logConfig.getAbsolutePath().replace(".properties", ".props"));
                if (oldLogConfig.exists()) {
                    oldLogConfig.renameTo(new File(oldLogConfig.getAbsolutePath().replace(".props", ".props_migrated")));
                }
            }
            Configurator.reconfigure((URI)logConfig.toURI());
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PropertyList consoleProps;
        PropertyList commandProps;
        HttpUtil httpUtil;
        String loggedOn;
        String requestCommand = request.getParameter("requestcommand");
        if (requestCommand == null || requestCommand.length() == 0) {
            requestCommand = "home";
        }
        Trace.logInfo(LOGGER, "Received command: " + requestCommand);
        String command = request.getParameter("command");
        if (command == null || command.length() == 0) {
            command = "html";
        }
        String string = loggedOn = (httpUtil = new HttpUtil(request, response)).getCookieValue(LOGON_COOKIE).equals(request.getSession().getAttribute(LOGON_COOKIE)) ? "Y" : "N";
        if (command.equals("formsubmit")) {
            commandProps = HttpUtil.getRequestPropertyList((ServletRequest)request);
        } else {
            try {
                commandProps = new PropertyList(new JSONObject(request.getParameter(requestCommand)));
            }
            catch (Exception e) {
                commandProps = new PropertyList();
            }
        }
        if (transTable == null || request.getParameter("language") != null && request.getParameter("language").length() > 0 || commandProps.getProperty("language").length() > 0) {
            this.loadTranslations(transTable, "console", LOGGER);
        }
        if ((consoleProps = (PropertyList)request.getSession().getAttribute("consoleprops")) == null || consoleProps.size() == 0) {
            try {
                consoleProps = this.getConsoleProps(request, loggedOn, true);
            }
            catch (SapphireException e) {
                consoleProps = new PropertyList();
            }
        }
        if (command.equals("html")) {
            try {
                if (requestCommand.equalsIgnoreCase("logoff")) {
                    this.logoff(request, response, LOGON_COOKIE, PRODUCT_NAME, "console");
                } else if (requestCommand.equalsIgnoreCase("home")) {
                    this.home(request, response, loggedOn);
                } else if (requestCommand.equalsIgnoreCase("downloadstatus")) {
                    this.downloadStatus(response);
                } else if (requestCommand.equalsIgnoreCase("download")) {
                    this.download(response);
                }
            }
            catch (Exception e) {
                this.htmlCommandError(response, PRODUCT_NAME, e);
            }
        } else if (command.equals("ajax")) {
            PrintWriter out = response.getWriter();
            if (requestCommand.equalsIgnoreCase("logon")) {
                out.print(this.logon(request, response, commandProps, LOGON_COOKIE, LOGGER));
            } else if (requestCommand.equalsIgnoreCase("loadtranslations")) {
                out.print(ClientTranslation.generateJSTranslations("dummy", transTable, false));
            } else if (requestCommand.equalsIgnoreCase("loadsources")) {
                out.print(this.loadSources(consoleProps, commandProps));
            } else if (requestCommand.equalsIgnoreCase("loadhome")) {
                out.print(this.loadHome());
            } else if (requestCommand.equalsIgnoreCase("loadconfiguration")) {
                out.print(this.loadConfiguration());
            } else if (requestCommand.equalsIgnoreCase("loadtransferpackages")) {
                out.print(this.loadTransferPackages(commandProps));
            } else if (requestCommand.equalsIgnoreCase("loaddatabases")) {
                out.print(this.loadDatabases());
            } else if (requestCommand.equalsIgnoreCase("testdatabase")) {
                out.print(this.testDatabase(commandProps));
            } else if (requestCommand.equalsIgnoreCase("maintdatabase")) {
                out.print(this.maintDatabase(commandProps));
            } else if (requestCommand.equalsIgnoreCase("setlicense")) {
                out.print(this.setLicense(commandProps));
            } else if (requestCommand.equalsIgnoreCase("loaddatabase")) {
                out.print(this.loadDatabase(commandProps));
            } else if (requestCommand.equalsIgnoreCase("deletedatabase")) {
                out.print(this.deleteDatabase(commandProps));
            } else if (requestCommand.equalsIgnoreCase("loadapplications")) {
                out.print(this.loadApplications(consoleProps));
            } else if (requestCommand.equalsIgnoreCase("loadapplication")) {
                out.print(this.loadApplication(consoleProps, commandProps));
            } else if (requestCommand.equalsIgnoreCase("maintapplication")) {
                out.print(this.maintApplication(commandProps));
            } else if (requestCommand.equalsIgnoreCase("genapplicationrak")) {
                out.print(this.genApplicationRAK(commandProps));
            } else if (requestCommand.equalsIgnoreCase("processapplicationcommand")) {
                out.print(this.processApplicationCommand(commandProps));
            } else if (requestCommand.equalsIgnoreCase("restoreapplication")) {
                out.print(this.restoreApplication(commandProps));
            } else if (requestCommand.equalsIgnoreCase("saveconsoleconfig")) {
                out.print(this.saveConsoleConfig(commandProps));
            } else if (requestCommand.equalsIgnoreCase("savelogonpageconfig")) {
                out.print(this.saveLogonPageConfig(commandProps));
            } else if (requestCommand.equalsIgnoreCase("saveapplicationconfig")) {
                out.print(this.saveApplicationConfig(commandProps));
            } else if (requestCommand.equalsIgnoreCase("loadearupgradeparams")) {
                out.print(this.loadEARUpgradeParams(commandProps));
            } else if (requestCommand.equalsIgnoreCase("loadearconfig")) {
                out.print(this.loadEARConfig(commandProps));
            } else if (requestCommand.equalsIgnoreCase("loadplatform")) {
                out.print(this.loadPlatform());
            } else if (requestCommand.equalsIgnoreCase("maintplatform")) {
                out.print(this.maintPlatform(commandProps));
            } else if (requestCommand.equalsIgnoreCase("loadpackagingprops")) {
                out.print(this.loadPackagingProps(commandProps));
            } else if (requestCommand.equalsIgnoreCase("savepackagingprops")) {
                out.print(this.savePackagingProps(commandProps));
            } else if (requestCommand.equalsIgnoreCase("modifybuildprops")) {
                out.print(this.modifyBuildProps(commandProps));
            } else if (requestCommand.equalsIgnoreCase("modifyearconfig")) {
                out.print(this.modifyEARConfig(commandProps));
            } else if (requestCommand.equalsIgnoreCase("loadfiles")) {
                out.print(this.loadFiles(commandProps));
            } else if (requestCommand.equalsIgnoreCase("loadpatchxml")) {
                out.print(this.loadPatchXML(commandProps));
            } else if (requestCommand.equalsIgnoreCase("derivepatchfileprops")) {
                out.print(this.derivePatchFileProps(commandProps));
            } else if (requestCommand.equalsIgnoreCase("savepatchxml")) {
                out.print(this.savePatchXML(commandProps));
            } else if (requestCommand.equalsIgnoreCase("buildpatchfile")) {
                out.print(this.buildPatchFile(commandProps));
            } else if (requestCommand.equalsIgnoreCase("loadpatchfile")) {
                out.print(this.loadPatchFile(commandProps));
            } else if (requestCommand.equalsIgnoreCase("hasexistinghome")) {
                out.print(this.hasExistingHome(commandProps));
            } else if (requestCommand.equalsIgnoreCase("test")) {
                out.print(this.test(commandProps));
            }
            out.close();
        }
    }

    public static boolean isValidLicenseBasedOnDatabaseCount(String databaseid, DBUtil adb, License serverLicense, License databaseLicense, boolean requireDatabaseLicense) throws SapphireException {
        int maxDatabasesWithSameLicense = databaseLicense.getDatabaseCount();
        boolean isValidLicenseBasedOnDatabaseCount = false;
        adb.createPreparedResultSet("select databaselistid, licensekey from databaselist where databaselistid <> ?", new Object[]{databaseid});
        DataSet licenses = new DataSet(adb.getResultSet());
        String licenseKeyId = databaseLicense.getLicenseid();
        int databaseCountWithSameLicense = 0;
        for (int i = 0; i < licenses.getRowCount(); ++i) {
            License databaseLic;
            String databaseId = licenses.getString(i, "databaselistid");
            String licensekey = licenses.getString(i, "licensekey");
            if (licensekey != null && !licensekey.isEmpty()) {
                databaseLic = new License(licensekey);
            } else {
                if (serverLicense == null && !requireDatabaseLicense) {
                    throw new SapphireException("Server license not defined!");
                }
                databaseLic = serverLicense;
            }
            if (databaseLic == null || !databaseLic.getLicenseid().equals(licenseKeyId)) continue;
            ++databaseCountWithSameLicense;
        }
        if (databaseCountWithSameLicense < maxDatabasesWithSameLicense) {
            isValidLicenseBasedOnDatabaseCount = true;
        }
        return isValidLicenseBasedOnDatabaseCount;
    }

    private String loadSources(PropertyList consoleProps, PropertyList commandProps) {
        String type = commandProps.getProperty("type", "P");
        PropertyList sourceProps = new PropertyList();
        try {
            DataSet sources = new DataSet();
            Configuration configuration = this.getConfiguration();
            int row = sources.addRow();
            sources.setString(row, "sourcename", "(LabVantage EAR - build " + consoleProps.getProperty("sapphireear.build") + ")");
            sources.setString(row, "sourcevalue", "(lvear)");
            if (type.equals("P")) {
                row = sources.addRow();
                File sourceDir = new File(configuration.getSapphireHome() + "/staging");
                if (sourceDir.exists()) {
                    File[] files = sourceDir.listFiles();
                    for (int i = 0; i < files.length; ++i) {
                        row = sources.addRow();
                        sources.setString(row, "sourcename", files[i].getName());
                        sources.setString(row, "sourcevalue", files[i].getName());
                    }
                }
            } else if (type.equals("J") && configuration instanceof JBoss) {
                ((JBoss)((Object)configuration)).getExplodedApps(sources);
            }
            sourceProps.setProperty("sources", JSONUtil.toJSONString(sources));
        }
        catch (Exception e) {
            sourceProps.setProperty("status", "error");
            sourceProps.setProperty("statusmessage", "Failed to load application sources. Reason: " + e.getMessage());
            Trace.logError(LOGGER, (Object)("Failed to load sources. Reason: " + e.getMessage()), e);
        }
        return sourceProps.toJSONString(false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String loadApplications(PropertyList consoleProps) {
        PropertyList applicationConfig = new PropertyList();
        PropertyList configProps = new PropertyList();
        PropertyList admindbProps = new PropertyList();
        DataSet applications = new DataSet();
        PropertyList deployedApplications = new PropertyList();
        DBUtil adb = new DBUtil();
        try {
            Configuration configuration = this.getConfiguration();
            File configFile = ConsoleController.getConfigFile(configuration);
            String admindb = ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindb");
            String admindbms = ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindbms");
            adb.setConnection(admindbms, ConsoleController.getDataSource(configuration, admindb).getConnection());
            try {
                adb.createResultSet("SELECT propertyvalue FROM adminconfig WHERE propertyid = 'build'");
                if (adb.getNext()) {
                    int compare = adb.getValue("propertyvalue").compareTo(Build.getBuild());
                    if (compare < 0) {
                        admindbProps.setProperty("state", "upgrade");
                    } else {
                        admindbProps.setProperty("state", "ok");
                    }
                    admindbProps.setProperty("admindbbuild", adb.getValue("propertyvalue"));
                }
            }
            catch (Exception e) {
                admindbProps.setProperty("state", "install");
            }
            admindbProps.setProperty("admindb", admindb);
            admindbProps.setProperty("admindbms", admindbms);
            this.getApplicationList(consoleProps, configuration, adb, applications);
            if (configuration instanceof JBoss) {
                File temp = ConsoleController.getConsoleTemp(configuration.getSapphireHome());
                deployedApplications.putAll(((JBoss)((Object)configuration)).getDeployedApplications(temp));
                FileUtil.deleteAll(temp);
            }
        }
        catch (Exception e) {
            applicationConfig.setProperty("status", "error");
            applicationConfig.setProperty("statusmessage", "Failed to load application details. Reason: " + e.getMessage());
            Trace.logError(LOGGER, (Object)("Failed to load application details. Reason: " + e.getMessage()), e);
        }
        finally {
            adb.reset();
            adb.releaseConnection();
        }
        applicationConfig.setProperty("admindb", admindbProps);
        applicationConfig.setProperty("config", configProps);
        applicationConfig.setProperty("applicationlist", JSONUtil.toJSONString(applications));
        applicationConfig.setProperty("deployedapplications", deployedApplications);
        return applicationConfig.toJSONString(false);
    }

    private void getApplicationList(PropertyList consoleProps, Configuration configuration, DBUtil adb, DataSet applicationList) throws SapphireException {
        adb.createResultSet("SELECT * FROM application ORDER BY applicationid");
        applicationList.setResultSet(adb.getResultSet(), true, adb.getDbms());
        for (int i = 0; i < applicationList.size(); ++i) {
            try {
                File appHome = this.getAppHome(configuration, applicationList.getValue(i, "applicationid"));
                if (appHome.exists()) {
                    String managedear = applicationList.getValue(i, "managedear");
                    if (managedear.length() > 0) {
                        File ear = null;
                        File tempDir = null;
                        PropertyList appProps = null;
                        boolean deployed = false;
                        if (configuration instanceof JBoss) {
                            deployed = ((JBoss)((Object)configuration)).isDeployed(managedear);
                        }
                        if (applicationList.getValue(i, "typeflag", "P").equals("P")) {
                            File managedEAR = this.getManagedEAR(appHome, applicationList.getValue(i, "managedear"));
                            File lastEAR = new File(managedEAR.getAbsolutePath() + "-last");
                            applicationList.setString(i, "managedear", managedEAR.getName());
                            applicationList.setString(i, "origear", lastEAR.exists() ? lastEAR.getAbsolutePath() : "");
                            ear = new File(appHome, "ear/" + managedear);
                            tempDir = ConsoleController.getConsoleTemp(configuration.getSapphireHome());
                        } else if (applicationList.getValue(i, "typeflag", "P").equals("J")) {
                            ear = new File(((JBoss)((Object)configuration)).getDeploymentDir(), managedear);
                        }
                        appProps = ConsoleController.getEARBuildProps(ear, tempDir);
                        if (appProps.size() == 0) {
                            appProps = ConsoleController.getEARConfig(configuration, ear, tempDir);
                        }
                        appProps.putAll(ConsoleController.getEARPatchProps(ear, tempDir));
                        applicationList.setString(i, "status", this.deriveApplicationStatus(appProps.getProperty("application.status"), appProps.getProperty("sapphire.build"), consoleProps.getProperty("sapphireear.build")));
                        applicationList.setString(i, "deployed", deployed ? "Y" : "N");
                        applicationList.setString(i, "applicationjar", appProps.getProperty("application.jar"));
                        applicationList.setString(i, "applicationwar", appProps.getProperty("application.war"));
                        applicationList.setString(i, "build", appProps.getProperty("sapphire.build", "(unknown)"));
                        applicationList.setString(i, "patch", appProps.getProperty("sapphire.patch", "(unknown)"));
                        applicationList.setString(i, "patches", appProps.getCollection("patches") != null ? "Y" : "N");
                        applicationList.setString(i, "version", appProps.getProperty("sapphire.version", "(unknown)"));
                        continue;
                    }
                    applicationList.setString(i, "status", "NE");
                    continue;
                }
                applicationList.setString(i, "status", "MH");
                continue;
            }
            catch (Exception e) {
                applicationList.setString(i, "status", "UE");
                Trace.logError(LOGGER, (Object)("Failed to get application details. Reason: " + e.getMessage()), e);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String loadApplication(PropertyList consoleProps, PropertyList commandProps) {
        PropertyList applicationConfig = new PropertyList();
        DBUtil adb = new DBUtil();
        try {
            Configuration configuration = this.getConfiguration();
            File configFile = ConsoleController.getConfigFile(configuration);
            adb.setConnection(ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindbms"), ConsoleController.getDataSource(configuration, ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindb")).getConnection());
            String applicationid = commandProps.getProperty("applicationid");
            String logonSubConfigurationId = commandProps.getProperty("logonsubconfigurationid");
            adb.createPreparedResultSet("SELECT * FROM application WHERE applicationid = ?", applicationid);
            DataSet application = new DataSet(adb.getResultSet());
            if (application.size() == 1) {
                applicationConfig.putAll((HashMap)application.get(0));
                PropertyList buildProps = new PropertyList();
                applicationConfig.setProperty("buildprops", buildProps);
                PropertyList earConfigProps = new PropertyList();
                applicationConfig.setProperty("earconfigprops", earConfigProps);
                File appHome = this.getAppHome(configuration, applicationid);
                if (appHome.exists()) {
                    String type;
                    PropertyList configProps = new PropertyList();
                    File labvantageconfig = new File(appHome, "labvantageconfig.props");
                    configProps.putAll(ConsoleController.loadPropertiesFile(labvantageconfig.exists() ? labvantageconfig : new File(appHome, "sapphireconfig.props")));
                    applicationConfig.setProperty("sapphireconfig", configProps);
                    PropertyList defProps = new PropertyList();
                    LogonPageTag.setDefaults(defProps);
                    LogonPageTag.addOverrides(defProps, appHome);
                    applicationConfig.setProperty("logonpageconfig", defProps);
                    File logFile = new File(appHome, "labvantagelogging.properties");
                    if (!logFile.exists()) {
                        logFile = new File(appHome, "sapphirelogging.properties");
                    }
                    PropertyList loggingProps = new PropertyList();
                    loggingProps.putAll(ConsoleController.loadPropertiesFile(logFile));
                    applicationConfig.setProperty("sapphirelogging", loggingProps);
                    LogConfig logConfig = new LogConfig(applicationid, logFile);
                    loggingProps.setProperty("sapphirelogenabled", logConfig.isSapphireLogEnabled() ? "Y" : "N");
                    loggingProps.setProperty("sapphireloglevel", logConfig.getRootLoggerLevel());
                    loggingProps.setProperty("sapphirelogtype", logConfig.getSapphireLogType());
                    loggingProps.setProperty("errorlogenabled", logConfig.isErrorLogEnabled() ? "Y" : "N");
                    loggingProps.setProperty("errorlogtype", logConfig.getErrorLogType());
                    loggingProps.setProperty("startuplogenabled", logConfig.isStartupLogEnabled() ? "Y" : "N");
                    boolean deployed = false;
                    if (configuration instanceof JBoss) {
                        deployed = new File(((JBoss)((Object)configuration)).getDeploymentDir(), application.getValue(0, "managedear")).exists();
                    }
                    if ((type = application.getValue(0, "typeflag", "P")).equals("P")) {
                        File ear = this.getManagedEAR(appHome, application.getValue(0, "managedear"));
                        buildProps.putAll(ConsoleController.getEARBuildProps(ear, ConsoleController.getConsoleTemp(configuration.getSapphireHome())));
                        applicationConfig.putAll(ConsoleController.getEARComponentProps(ear, ConsoleController.getConsoleTemp(configuration.getSapphireHome())));
                        applicationConfig.putAll(ConsoleController.getEARPatchProps(ear, ConsoleController.getConsoleTemp(configuration.getSapphireHome())));
                        earConfigProps.putAll(ConsoleController.getEARConfig(configuration, ear, ConsoleController.getConsoleTemp(configuration.getSapphireHome())));
                    } else if (type.equals("J")) {
                        File earDir = deployed ? new File(((JBoss)((Object)configuration)).getDeploymentDir(), application.getValue(0, "managedear")) : new File(appHome, "earx/" + application.getValue(0, "managedear"));
                        buildProps.putAll(ConsoleController.getEARBuildProps(earDir, null));
                        applicationConfig.putAll(ConsoleController.getEARComponentProps(earDir, null));
                        applicationConfig.putAll(ConsoleController.getEARPatchProps(earDir, null));
                        earConfigProps.putAll(ConsoleController.getEARConfig(configuration, earDir, null));
                    }
                    earConfigProps.setProperty("application.status", this.deriveApplicationStatus(buildProps.getProperty("application.status"), buildProps.getProperty("sapphire.build"), consoleProps.getProperty("sapphireear.build")));
                    earConfigProps.setProperty("deployed", deployed ? "Y" : "N");
                    String string = earConfigProps.getProperty("application.webcontext");
                } else {
                    earConfigProps.setProperty("application.status", "MH");
                }
            } else {
                applicationConfig.setProperty("status", "error");
                applicationConfig.setProperty("statusmessage", "Failed to load application details.");
            }
        }
        catch (Exception e) {
            applicationConfig.setProperty("status", "error");
            applicationConfig.setProperty("statusmessage", "Failed to load application details. Reason: " + e.getMessage());
            Trace.logError(LOGGER, (Object)("Failed to load application details. Reason: " + e.getMessage()), e);
        }
        finally {
            adb.reset();
            adb.releaseConnection();
        }
        return applicationConfig.toJSONString(false);
    }

    private String deriveApplicationStatus(String appStatus, String appBuild, String sapphireBuild) {
        return appStatus.equals("NE") ? "NE" : (appBuild.length() == 0 ? "VA" : (appBuild.equals(sapphireBuild) ? "OK" : (appBuild.compareTo(sapphireBuild) < 0 ? "RU" : "NV")));
    }

    private String loadEARConfig(PropertyList commandProps) {
        PropertyList earConfig = new PropertyList();
        try {
            Configuration configuration = this.getConfiguration();
            File ear = new File(commandProps.getProperty("ear"));
            if (ear.isFile()) {
                earConfig.putAll(ConsoleController.getEARConfig(configuration, ear, ConsoleController.getConsoleTemp(configuration.getSapphireHome())));
            } else {
                earConfig.putAll(ConsoleController.getEARConfig(configuration, ear, null));
            }
        }
        catch (Exception e) {
            earConfig.setProperty("status", "error");
            earConfig.setProperty("statusmessage", "Failed to load application details. Reason: " + e.getMessage());
            Trace.logError(LOGGER, (Object)("Failed to load application details. Reason: " + e.getMessage()), e);
        }
        return earConfig.toJSONString(false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static PropertyList getEARConfig(Configuration configuration, File ear, File tempDir) {
        PropertyList earConfig = new PropertyList();
        try {
            if (ear.isFile() && tempDir != null) {
                if (tempDir.exists()) {
                    FileUtil.deleteAll(tempDir);
                }
                tempDir.mkdirs();
                ZipFileUtil.extractAll(ear, tempDir);
                PropertyList appXMLProps = ConsoleController.getApplicationXMLProps(new File(tempDir, "/META-INF/application.xml"));
                PropertyList appBuildProps = ConsoleController.getEARBuildProps(tempDir, null);
                String standardjar = appXMLProps.getProperty("standard.jar", STANDARD_JAR);
                File jarMF = ZipFileUtil.extractFile(new File(tempDir, standardjar), "META-INF/MANIFEST.MF", tempDir);
                earConfig.setProperty("application.classpath.jar", ConsoleController.getManifestValue(jarMF, "Class-Path"));
                String standardwar = appXMLProps.getProperty("application.war", STANDARD_WAR);
                File warMF = ZipFileUtil.extractFile(new File(tempDir, standardwar), "META-INF/MANIFEST.MF", tempDir);
                earConfig.setProperty("application.classpath.war", ConsoleController.getManifestValue(warMF, "Class-Path"));
                File warWebXML = ZipFileUtil.extractFile(new File(tempDir, standardwar), "WEB-INF/web.xml", tempDir);
                earConfig.put("portalconfig", ConsoleController.getPortalProps(warWebXML));
                String appServer = appBuildProps.getProperty("application.appserver", Configuration.getPlatformName(configuration.getPlatform()));
                if (appServer.startsWith("JBoss")) {
                    ZipFileUtil.extractFile(new File(tempDir, standardjar), "META-INF/jboss.xml", tempDir);
                } else if (appServer.equalsIgnoreCase("WebLogic")) {
                    ZipFileUtil.extractFile(new File(tempDir, standardjar), "META-INF/weblogic-ejb-jar.xml", tempDir);
                } else if (appServer.equalsIgnoreCase("WebSphere")) {
                    ZipFileUtil.extractFile(new File(tempDir, standardjar), "META-INF/ibm-ejb-jar-bnd.xmi", tempDir);
                }
                earConfig.putAll(ConsoleController.getJNDIProps(tempDir, appServer));
                earConfig.putAll(ConsoleController.getExplodedConfig(configuration, tempDir));
            } else {
                earConfig.putAll(ConsoleController.getExplodedConfig(configuration, ear));
            }
        }
        catch (Exception e) {
            earConfig.setProperty("application.status", "ER");
            Trace.logError(LOGGER, (Object)("Failed to get EAR configuration properties. Reason: " + e.getMessage()), e);
        }
        finally {
            if (tempDir != null && tempDir.exists()) {
                try {
                    FileUtil.deleteAll(tempDir);
                }
                catch (IOException e) {
                    Trace.logError(LOGGER, (Object)("Failed to delete temp dir. Reason: " + e.getMessage()), e);
                }
            }
        }
        return earConfig;
    }

    private static PropertyList getPortalProps(File warWebXML) {
        PropertyList portalProps = new PropertyList();
        portalProps.setProperty("database", "");
        portalProps.setProperty("urlpattern", "");
        portalProps.setProperty("servlet", "");
        portalProps.setProperty("portal", "");
        portalProps.setProperty("debug", "");
        portalProps.setProperty("encrypt", "");
        portalProps.setProperty("encryptcookies", "");
        portalProps.setProperty("autorefreshproperties", "");
        portalProps.setProperty("portalonly", "");
        try {
            int i;
            HashMap<String, String> servletMappings = new HashMap<String, String>();
            Document dom = DOMUtil.getNewDocument(warWebXML, false, "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN");
            Element docEle = dom.getDocumentElement();
            NodeList nl = docEle.getElementsByTagName("servlet-mapping");
            if (nl != null && nl.getLength() > 0) {
                for (int i2 = 0; i2 < nl.getLength(); ++i2) {
                    Element el = (Element)nl.item(i2);
                    String servletName = BaseConverter.getTextValue(el, "servlet-name");
                    String urlpattern = BaseConverter.getTextValue(el, "url-pattern");
                    if (servletName.isEmpty() || urlpattern.isEmpty()) continue;
                    servletMappings.put(servletName, urlpattern);
                }
            }
            NodeList filterNodes = docEle.getElementsByTagName("filter");
            for (i = 0; i < filterNodes.getLength(); ++i) {
                Element filterElement = (Element)filterNodes.item(i);
                String filterName = BaseConverter.getTextValue(filterElement, "filter-name");
                if (!filterName.equals("PortalRequest")) continue;
                portalProps.setProperty("portalonly", BaseConverter.getInitParam(filterElement, "enabled"));
                break;
            }
            if ((nl = docEle.getElementsByTagName("servlet")) != null && nl.getLength() > 0) {
                for (i = 0; i < nl.getLength(); ++i) {
                    Element el = (Element)nl.item(i);
                    String servletClass = BaseConverter.getTextValue(el, "servlet-class");
                    if (!servletClass.equals("com.labvantage.stellar.servlet.PortalController")) continue;
                    String servletName = BaseConverter.getTextValue(el, "servlet-name");
                    String urlPattern = (String)servletMappings.get(servletName);
                    Utils.appendToPL(portalProps, "servlet", servletName);
                    Utils.appendToPL(portalProps, "urlpattern", urlPattern);
                    Utils.appendToPL(portalProps, "database", BaseConverter.getInitParam(el, "database"));
                    Utils.appendToPL(portalProps, "portal", BaseConverter.getInitParam(el, "portal"));
                    if (portalProps.getProperty("debug", "").isEmpty()) {
                        portalProps.setProperty("debug", BaseConverter.getInitParam(el, "debug"));
                    }
                    if (portalProps.getProperty("encrypt", "").isEmpty()) {
                        portalProps.setProperty("encrypt", BaseConverter.getInitParam(el, "encrypt"));
                    }
                    if (portalProps.getProperty("encryptcookies", "").isEmpty()) {
                        portalProps.setProperty("encryptcookies", BaseConverter.getInitParam(el, "encryptcookies"));
                    }
                    if (!portalProps.getProperty("autorefreshproperties", "").isEmpty()) continue;
                    portalProps.setProperty("autorefreshproperties", BaseConverter.getInitParam(el, "autorefreshproperties"));
                }
            }
        }
        catch (Exception e) {
            portalProps.setProperty("error", "error reading portal properties: " + e.getMessage());
        }
        return portalProps;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static PropertyList getEARBuildProps(File ear, File tempDir) {
        PropertyList buildProps = new PropertyList();
        if (ear.exists()) {
            try {
                File appBuildProps = null;
                if (ear.isFile() && tempDir != null) {
                    if (tempDir.exists()) {
                        FileUtil.deleteAll(tempDir);
                    }
                    tempDir.mkdirs();
                    appBuildProps = ZipFileUtil.extractFile(ear, "META-INF/application-build.props", tempDir);
                } else {
                    appBuildProps = new File(ear, "/META-INF/application-build.props");
                }
                buildProps.putAll(ConsoleController.loadPropertiesFile(appBuildProps));
                ConsoleController.appendApplicationBuildProps(buildProps, ear);
            }
            catch (Exception e) {
                Trace.logError(LOGGER, (Object)("Failed to get EAR build properties. Reason: " + e.getMessage()), e);
            }
            finally {
                if (tempDir != null && tempDir.exists()) {
                    try {
                        FileUtil.deleteAll(tempDir);
                    }
                    catch (IOException e) {
                        Trace.logError(LOGGER, (Object)("Failed to delete temp dir. Reason: " + e.getMessage()), e);
                    }
                }
            }
        } else {
            buildProps.setProperty("application.status", "NE");
        }
        return buildProps;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static PropertyList getEARComponentProps(File ear, File tempDir) {
        PropertyList componentProps = new PropertyList();
        if (ear.exists()) {
            try {
                File appComponentXML = null;
                if (ear.isFile() && tempDir != null) {
                    if (tempDir.exists()) {
                        FileUtil.deleteAll(tempDir);
                    }
                    tempDir.mkdirs();
                    appComponentXML = ZipFileUtil.extractFile(ear, "META-INF/component.xml", tempDir);
                } else {
                    appComponentXML = new File(ear, "/META-INF/component.xml");
                }
                if (!appComponentXML.exists()) return componentProps;
                PatchXML componentXML = new PatchXML(appComponentXML);
                componentXML.load();
                componentProps.setProperty("components", componentXML.getPatchesCollection());
                return componentProps;
            }
            catch (Exception e) {
                return componentProps;
            }
            finally {
                if (tempDir != null && tempDir.exists()) {
                    try {
                        FileUtil.deleteAll(tempDir);
                    }
                    catch (IOException e) {
                        Trace.logError(LOGGER, (Object)("Failed to delete temp dir. Reason: " + e.getMessage()), e);
                    }
                }
            }
        }
        componentProps.setProperty("application.status", "NE");
        return componentProps;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static PropertyList getEARPatchProps(File ear, File tempDir) {
        PropertyList patchProps = new PropertyList();
        if (ear.exists()) {
            try {
                File appPatchXML = null;
                if (ear.isFile() && tempDir != null) {
                    if (tempDir.exists()) {
                        FileUtil.deleteAll(tempDir);
                    }
                    tempDir.mkdirs();
                    appPatchXML = ZipFileUtil.extractFile(ear, "META-INF/patch.xml", tempDir);
                } else {
                    appPatchXML = new File(ear, "/META-INF/patch.xml");
                }
                if (!appPatchXML.exists()) return patchProps;
                PatchXML patchXML = new PatchXML(appPatchXML);
                patchXML.load();
                patchProps.setProperty("patches", patchXML.getPatchesCollection());
                return patchProps;
            }
            catch (Exception e) {
                return patchProps;
            }
            finally {
                if (tempDir != null && tempDir.exists()) {
                    try {
                        FileUtil.deleteAll(tempDir);
                    }
                    catch (IOException e) {
                        Trace.logError(LOGGER, (Object)("Failed to delete temp dir. Reason: " + e.getMessage()), e);
                    }
                }
            }
        }
        patchProps.setProperty("application.status", "NE");
        return patchProps;
    }

    private static void appendApplicationBuildProps(PropertyList buildProps, File ear) {
        String applicationJAR = buildProps.getProperty("application.jar", "");
        if (applicationJAR.equals(STANDARD_JAR) || applicationJAR.equals("labvantage.jar")) {
            buildProps.setProperty(STANDARD_JAR, applicationJAR);
            buildProps.setProperty("application.jar", "");
        } else {
            buildProps.setProperty(STANDARD_JAR, STANDARD_JAR);
        }
        buildProps.setProperty("ear.location", ear.getParentFile().getAbsolutePath());
    }

    public static PropertyList getExplodedConfig(Configuration configuration, File earDir) {
        PropertyList configProps = new PropertyList();
        if (earDir.exists()) {
            try {
                File warWebXML;
                String standardwar;
                PropertyList appBuildProps = new PropertyList();
                appBuildProps.putAll(ConsoleController.loadPropertiesFile(new File(earDir, "/META-INF/application-build.props")));
                configProps.setProperty("application.appbuildpropsfile", new File(earDir, "/META-INF/application-build.props").getAbsolutePath());
                configProps.setProperty("application.appbuildprops", appBuildProps);
                configProps.putAll(ConsoleController.getApplicationXMLProps(new File(earDir, "/META-INF/application.xml")));
                String standardjar = configProps.getProperty("standard.jar", STANDARD_JAR);
                if (new File(earDir, standardjar + "/META-INF/MANIFEST.MF").exists()) {
                    configProps.setProperty("application.classpath.jar", ConsoleController.getManifestValue(new File(earDir, standardjar + "/META-INF/MANIFEST.MF"), "Class-Path"));
                }
                if (new File(earDir, (standardwar = configProps.getProperty("application.war", STANDARD_WAR)) + "/META-INF/MANIFEST.MF").exists()) {
                    configProps.setProperty("application.classpath.war", ConsoleController.getManifestValue(new File(earDir, standardwar + "/META-INF/MANIFEST.MF"), "Class-Path"));
                }
                if ((warWebXML = new File(earDir, standardwar + "/WEB-INF/web.xml")).exists()) {
                    configProps.setProperty("portalconfig", ConsoleController.getPortalProps(warWebXML));
                }
                configProps.putAll(ConsoleController.getJNDIProps(new File(earDir, standardjar), appBuildProps.getProperty("application.appserver", Configuration.getPlatformName(configuration.getPlatform()))));
                String applicationjar = appBuildProps.getProperty("application.jar");
                DataSet libJars = new DataSet();
                File libDir = new File(earDir, "lib");
                File[] libFiles = libDir.listFiles();
                if (libFiles != null) {
                    for (int i = 0; i < Configuration.currentRuntimeJarDetails.length; ++i) {
                        libJars.addRow();
                        libJars.setString(i, "name", Configuration.currentRuntimeJarDetails[i][1]);
                        libJars.setString(i, "version", Configuration.currentRuntimeJarDetails[i][4]);
                        libJars.setString(i, "license", Configuration.currentRuntimeJarDetails[i][5]);
                        if (new File(libDir, Configuration.currentRuntimeJarDetails[i][1]).exists()) {
                            libJars.setString(i, "status", "OK");
                            continue;
                        }
                        libJars.setString(i, "status", "ERROR - NOT FOUND");
                    }
                }
                configProps.setProperty("application.libjars", libJars.size() > 0 ? libJars.toJSONString() : "");
                String[] coreJARList = Configuration.getCommonJarList();
                StringBuffer corejars = new StringBuffer();
                for (int i = 0; i < coreJARList.length; ++i) {
                    coreJARList[i] = coreJARList[i].toLowerCase();
                    corejars.append(";").append(coreJARList[i]);
                }
                HashSet<String> coreJARFiles = new HashSet<String>(Arrays.asList(coreJARList));
                StringBuffer actualcorejars = new StringBuffer();
                StringBuffer externaljars = new StringBuffer();
                File[] earFiles = earDir.listFiles();
                if (earFiles != null) {
                    for (File earFile : earFiles) {
                        if (coreJARFiles.contains(earFile.getName().toLowerCase())) {
                            coreJARFiles.remove(earFile.getName().toLowerCase());
                            actualcorejars.append(";").append(earFile.getName());
                            continue;
                        }
                        if (earFile.getName().equals(standardjar) || earFile.getName().equals(standardwar) || earFile.getName().equals("META-INF")) continue;
                        if (earFile.isDirectory() && earFile.getName().endsWith(".jar") || earFile.getName().equals(applicationjar)) {
                            configProps.setProperty("application.jar", applicationjar);
                            continue;
                        }
                        if (!earFile.isFile() || !earFile.getName().endsWith(".jar")) continue;
                        externaljars.append(",").append(earFile.getName());
                    }
                }
                StringBuffer missingcorejars = new StringBuffer();
                Iterator<String> it = coreJARFiles.iterator();
                while (it.hasNext()) {
                    missingcorejars.append(";").append(it.next());
                }
                configProps.setProperty("sapphire.corejars", corejars.substring(1));
                configProps.setProperty("application.corejars", actualcorejars.length() > 0 ? actualcorejars.substring(1) : "");
                configProps.setProperty("application.externaljars", externaljars.length() > 0 ? externaljars.substring(1) : "");
                configProps.setProperty("application.missingcorejars", missingcorejars.length() > 0 ? missingcorejars.substring(1) : "");
            }
            catch (Exception e) {
                Trace.logError(LOGGER, (Object)("Failed to get exploded application properties. Reason: " + e.getMessage()), e);
            }
        } else {
            configProps.setProperty("application.status", "NE");
        }
        return configProps;
    }

    private String modifyBuildProps(PropertyList commandProps) {
        PropertyList buildProps = new PropertyList();
        String managedear = commandProps.getProperty("managedear");
        String type = commandProps.getProperty("applicationtype");
        commandProps.remove("applicationid");
        commandProps.remove("managedear");
        commandProps.remove("applicationtype");
        try {
            if (type.equals("J")) {
                Configuration configuration = this.getConfiguration();
                File appBuildPropsFile = new File(((JBoss)((Object)configuration)).getDeploymentDir(), managedear + "/META-INF/application-build.props");
                Properties appBuildProps = ConsoleController.loadPropertiesFile(appBuildPropsFile);
                appBuildProps.putAll((Map<?, ?>)commandProps);
                ConsoleController.savePropertiesFile(appBuildProps, appBuildPropsFile, "LabVantage Build Properties");
            }
        }
        catch (Exception e) {
            buildProps.setProperty("status", "error");
            buildProps.setProperty("statusmessage", "Failed to modify application build properties. Reason: " + e.getMessage());
            Trace.logError(LOGGER, "Failed to modify application build properties. Reason: " + e.getMessage());
        }
        return buildProps.toJSONString(false);
    }

    private String modifyEARConfig(PropertyList commandProps) {
        PropertyList buildProps = new PropertyList();
        String applicationid = commandProps.getProperty("applicationid");
        String managedear = commandProps.getProperty("managedear");
        String type = commandProps.getProperty("applicationtype");
        try {
            buildProps.setProperty("status", "ok");
            if (type.equals("J")) {
                Configuration configuration = this.getConfiguration();
                File appDir = new File(((JBoss)((Object)configuration)).getDeploymentDir(), managedear);
                File appBuildPropsFile = new File(appDir, "META-INF/application-build.props");
                Properties appBuildProps = ConsoleController.loadPropertiesFile(appBuildPropsFile);
                GetJarListTask task = new GetJarListTask();
                task.setSeparator(",");
                String applicationjar = commandProps.getProperty("applicationjar").toLowerCase();
                if (applicationjar.length() > 0 && applicationjar.endsWith(".jar") && !applicationjar.equals(STANDARD_JAR)) {
                    File newApplicationJAR = new File(appDir, applicationjar);
                    if (appBuildProps.getProperty("application.jar").length() > 0) {
                        File currentApplicationJAR = new File(appDir, appBuildProps.getProperty("application.jar"));
                        if (!currentApplicationJAR.exists()) {
                            new File(newApplicationJAR, "sapphire/custom/" + applicationid).mkdirs();
                        } else if (!applicationjar.equalsIgnoreCase(appBuildProps.getProperty("application.jar"))) {
                            throw new Exception("Cannot rename an existing JAR!");
                        }
                    } else {
                        new File(appDir, applicationjar + "/sapphire/custom/" + applicationid).mkdirs();
                    }
                    appBuildProps.setProperty("application.jar", applicationjar);
                }
                String externaljarlist = commandProps.getProperty("externaljarlist");
                task.setCustomJars(appBuildProps.getProperty("application.jar").length() > 0 ? appBuildProps.getProperty("application.jar") + "," + externaljarlist : externaljarlist);
                appBuildProps.setProperty("application.externaljars", externaljarlist);
                buildProps.setProperty("jarclasspath", task.getJarList());
                buildProps.setProperty("warclasspath", task.getJarList());
                String webcontext = commandProps.getProperty("webcontext");
                BaseConverter converter = BaseConverter.getInstance("JBoss");
                converter.changeWebContextName(new File(appDir, "META-INF/application.xml"), null, webcontext, appBuildProps.getProperty("application.war", STANDARD_WAR));
                appBuildProps.setProperty("application.webcontext", webcontext);
                ConsoleController.savePropertiesFile(appBuildProps, appBuildPropsFile, "LabVantage Build Properties");
            }
        }
        catch (Exception e) {
            buildProps.setProperty("status", "error");
            buildProps.setProperty("statusmessage", "Failed to modify EAR configuration. Reason: " + e.getMessage());
            Trace.logError(LOGGER, "Failed to modify EAR configuration. Reason: " + e.getMessage());
        }
        return buildProps.toJSONString(false);
    }

    private String loadFiles(PropertyList commandProps) {
        PropertyList fileProps = new PropertyList();
        fileProps.setProperty("separator", File.separator);
        try {
            String filter = commandProps.getProperty("filefilter");
            String selectFile = commandProps.getProperty("selectfile");
            if (selectFile.length() > 0) {
                File file = new File(selectFile);
                if (file.exists() && file.isFile()) {
                    fileProps.setProperty("selectfileparent", file.getParentFile().getAbsolutePath());
                } else {
                    fileProps.setProperty("selectfileparent", file.getAbsolutePath());
                }
            }
            StringBuffer fileList = new StringBuffer();
            StringBuffer nameList = new StringBuffer();
            StringBuffer sizeList = new StringBuffer();
            StringBuffer typeList = new StringBuffer();
            StringBuffer dateList = new StringBuffer();
            if (commandProps.getProperty("type").equals("root")) {
                File[] roots = File.listRoots();
                for (int i = 0; i < roots.length; ++i) {
                    fileList.append(";").append(roots[i].getAbsolutePath());
                    nameList.append(";").append(roots[i].getAbsolutePath());
                }
            } else if (commandProps.getProperty("type").equals("filedirs") || commandProps.getProperty("type").equals("filedetails")) {
                File file = new File(commandProps.getProperty("file"));
                File[] files = file.listFiles();
                for (int i = 0; i < files.length; ++i) {
                    if (files[i].isDirectory()) {
                        fileList.append(";").append(files[i].getAbsolutePath());
                        nameList.append(";").append(files[i].getName());
                        if (!commandProps.getProperty("type").equals("filedetails")) continue;
                        sizeList.append(";").append(String.valueOf(files[i].length() / 1000L)).append("KB");
                        typeList.append(";").append("File folder");
                        dateList.append(";").append(new SimpleDateFormat().format(new Date(files[i].lastModified())));
                        continue;
                    }
                    if (!commandProps.getProperty("type").equals("filedetails") || filter.length() != 0 && !FileUtil.wildcardMatch(files[i].getName(), filter, false)) continue;
                    fileList.append(";").append(files[i].getAbsolutePath());
                    nameList.append(";").append(files[i].getName());
                    sizeList.append(";").append(String.valueOf(files[i].length() / 1000L)).append("KB");
                    typeList.append(";").append(files[i].getName().contains(".") ? files[i].getName().substring(files[i].getName().indexOf(".") + 1) : files[i].getName());
                    dateList.append(";").append(new SimpleDateFormat().format(new Date(files[i].lastModified())));
                }
            }
            fileProps.setProperty("filelist", fileList.length() > 0 ? fileList.substring(1) : "");
            fileProps.setProperty("namelist", nameList.length() > 0 ? nameList.substring(1) : "");
            fileProps.setProperty("sizelist", sizeList.length() > 0 ? sizeList.substring(1) : "");
            fileProps.setProperty("typelist", typeList.length() > 0 ? typeList.substring(1) : "");
            fileProps.setProperty("datelist", dateList.length() > 0 ? dateList.substring(1) : "");
        }
        catch (Exception e) {
            fileProps.setProperty("status", "fail");
            fileProps.setProperty("statusmessage", "Failed to load files. Reason: " + e.getMessage());
        }
        return fileProps.toJSONString(false);
    }

    private String loadPatchXML(PropertyList commandProps) {
        PropertyList patchXMLProps = new PropertyList();
        try {
            PatchXML patchXML = new PatchXML(new File(commandProps.getProperty("patchxmlfile")));
            patchXML.load();
            patchXMLProps.setProperty("patches", patchXML.getPatchesCollection(File.separatorChar));
        }
        catch (Exception e) {
            patchXMLProps.setProperty("status", "error");
            patchXMLProps.setProperty("statusmessage", "Failed to load patch XML. Reason: " + e.getMessage());
            Trace.logError(LOGGER, (Object)("Failed to load patch XML. Reason: " + e.getMessage()), e);
        }
        return patchXMLProps.toJSONString(false);
    }

    private String derivePatchFileProps(PropertyList commandProps) {
        PropertyList patchFileProps;
        block31: {
            patchFileProps = new PropertyList();
            try {
                String patchfilename = commandProps.getProperty("patchfile");
                File patchFile = new File(patchfilename);
                if (patchFile.exists()) {
                    boolean match = false;
                    String name = patchFile.getName();
                    patchFileProps.setProperty("jar", "");
                    if (patchFile.isFile()) {
                        String appHomeSearchStr;
                        patchFileProps.setProperty("type", "F");
                        if ((name.endsWith(".class") || name.endsWith(".xml")) && (this.hasFilePatternMatch(patchFile, "com" + File.separator + "labvantage" + File.separator, patchFileProps) || this.hasFilePatternMatch(patchFile, "org" + File.separator + "json" + File.separator, patchFileProps) || this.hasFilePatternMatch(patchFile, "sapphire" + File.separator + "accessor" + File.separator, patchFileProps) || this.hasFilePatternMatch(patchFile, "sapphire" + File.separator + "action" + File.separator, patchFileProps) || this.hasFilePatternMatch(patchFile, "sapphire" + File.separator + "custom" + File.separator, patchFileProps) || this.hasFilePatternMatch(patchFile, "sapphire" + File.separator + "error" + File.separator, patchFileProps) || this.hasFilePatternMatch(patchFile, "sapphire" + File.separator + "ext" + File.separator, patchFileProps) || this.hasFilePatternMatch(patchFile, "sapphire" + File.separator + "pageelements" + File.separator, patchFileProps) || this.hasFilePatternMatch(patchFile, "sapphire" + File.separator + "report" + File.separator, patchFileProps) || this.hasFilePatternMatch(patchFile, "sapphire" + File.separator + "servlet" + File.separator, patchFileProps) || this.hasFilePatternMatch(patchFile, "sapphire" + File.separator + "tagext" + File.separator, patchFileProps) || this.hasFilePatternMatch(patchFile, "sapphire" + File.separator + "util" + File.separator, patchFileProps) || this.hasFilePatternMatch(patchFile, "sapphire" + File.separator + "xml" + File.separator, patchFileProps) || this.hasFilePatternMatch(patchFile, "sapphire" + File.separator + "SapphireException.", patchFileProps))) {
                            patchFileProps.setProperty("target", "jar");
                            patchFileProps.setProperty("jar", this.findParentJAR(patchFile));
                            patchFileProps.setProperty("desc", "");
                            patchFileProps.setProperty("action", "replace");
                            match = true;
                        }
                        if (!match && (this.hasFilePatternMatch(patchFile, "console" + File.separator + "install" + File.separator, patchFileProps) || this.hasFilePatternMatch(patchFile, "console" + File.separator + "languages" + File.separator, patchFileProps) || this.hasFilePatternMatch(patchFile, "console" + File.separator + "procedures" + File.separator, patchFileProps) || this.hasFilePatternMatch(patchFile, "console" + File.separator + "templates" + File.separator, patchFileProps))) {
                            patchFileProps.setProperty("target", "lvh");
                            patchFileProps.setProperty("desc", "");
                            patchFileProps.setProperty("action", "replace");
                            match = true;
                        }
                        if (!match && (name.endsWith(".html") || name.endsWith(".jsp") || name.endsWith(".tld") || name.endsWith(".wsdd") || name.endsWith(".xml") || name.endsWith(".ico") || name.endsWith(".gif") || name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".html") || name.endsWith(".js") || name.endsWith(".css") || name.endsWith(".xml")) && (this.hasFilePatternMatch(patchFile, STANDARD_WEBDIR + File.separator, patchFileProps) || this.hasFilePatternMatch(patchFile, "WEB-OPAL" + File.separator, patchFileProps) || this.hasFilePatternMatch(patchFile, "WEB-INF" + File.separator, patchFileProps) || this.hasFilePatternMatch(patchFile, "home" + File.separator, patchFileProps) || this.hasFilePatternMatch(patchFile, "blank.h", patchFileProps) || this.hasFilePatternMatch(patchFile, "favicon.i", patchFileProps) || this.hasFilePatternMatch(patchFile, "index.h", patchFileProps) || this.hasFilePatternMatch(patchFile, "logon.j", patchFileProps) || this.hasFilePatternMatch(patchFile, "ping.h", patchFileProps) || this.hasParentPatternMatch(patchFile, "*.war", patchFileProps))) {
                            patchFileProps.setProperty("target", "war");
                            patchFileProps.setProperty("desc", "");
                            patchFileProps.setProperty("action", "replace");
                            match = true;
                        }
                        if (!match && name.endsWith(".jar")) {
                            if (this.hasFilePatternMatch(patchFile, "lib" + File.separator, patchFileProps)) {
                                patchFileProps.setProperty("target", "ear");
                                patchFileProps.setProperty("desc", "");
                                patchFileProps.setProperty("action", "replace");
                                match = true;
                            } else {
                                patchFileProps.setProperty("target", "ear");
                                patchFileProps.setProperty("file", name);
                                patchFileProps.setProperty("path", patchFile.getAbsolutePath().substring(0, patchFile.getAbsolutePath().indexOf(name) - 1));
                                patchFileProps.setProperty("desc", "");
                                patchFileProps.setProperty("action", "replace");
                                match = true;
                            }
                        }
                        if (!match && name.endsWith(".zip") && this.isPatchZipFile(patchFile)) {
                            patchFileProps.setProperty("target", "app");
                            patchFileProps.setProperty("file", name);
                            patchFileProps.setProperty("path", patchFile.getAbsolutePath().substring(0, patchFile.getAbsolutePath().indexOf(name) - 1));
                            patchFileProps.setProperty("desc", "");
                            patchFileProps.setProperty("action", "executeinst");
                            match = true;
                        }
                        if (!match && name.endsWith(".zip") && this.isCTTZipFile(patchFile)) {
                            patchFileProps.setProperty("target", "database");
                            patchFileProps.setProperty("file", name);
                            patchFileProps.setProperty("path", patchFile.getAbsolutePath().substring(0, patchFile.getAbsolutePath().indexOf(name) - 1));
                            patchFileProps.setProperty("desc", "");
                            patchFileProps.setProperty("action", "executectt");
                            match = true;
                        }
                        if (!match && (name.equals("application.xml") || name.equals(FILE_APPLICATIONBUILDPROPS) || name.equals("application-j2ee-engine.xml") || name.equals("weblogic-application.xml")) && this.hasFilePatternMatch(patchFile, "META-INF" + File.separator, patchFileProps)) {
                            patchFileProps.setProperty("target", "ear");
                            patchFileProps.setProperty("desc", "");
                            patchFileProps.setProperty("action", "replace");
                            match = true;
                        }
                        if (!match && (name.equals("ejb-j2ee-engine.xml") || name.equals("ejb-jar.xml") || name.equals("jboss.xml") || name.equals("sun-j2ee-ri.xml") || name.equals("weblogic-cmp-rdbms-jar.xml") || name.equals("weblogic-ejb-jar.xml") || name.equals("ibm-ejb-jar-bnd.xmi") || name.equals("ibm-ejb-jar-ext.xmi")) && this.hasFilePatternMatch(patchFile, "META-INF" + File.separator, patchFileProps)) {
                            patchFileProps.setProperty("target", "jar");
                            patchFileProps.setProperty("jar", STANDARD_JAR);
                            patchFileProps.setProperty("desc", "");
                            patchFileProps.setProperty("action", "replace");
                            match = true;
                        }
                        if (!match && patchFile.getAbsolutePath().endsWith("META-INF" + File.separator + "MANIFEST.MF")) {
                            File parent = patchFile.getParentFile();
                            File[] files = parent.listFiles();
                            for (int i = 0; !match && i < files.length; ++i) {
                                if (files[i].getAbsolutePath().contains("com" + File.separator + "labvantage" + File.separator)) {
                                    patchFileProps.setProperty("target", "jar");
                                    patchFileProps.setProperty("jar", STANDARD_JAR);
                                    match = true;
                                    continue;
                                }
                                if (!files[i].getAbsolutePath().contains(STANDARD_WEBDIR + File.separator)) continue;
                                patchFileProps.setProperty("target", "war");
                                match = true;
                            }
                            if (!match) {
                                patchFileProps.setProperty("target", "ear");
                                match = true;
                            }
                            patchFileProps.setProperty("file", "META-INF" + File.separator + "MANIFEST.MF");
                            patchFileProps.setProperty("path", patchFile.getAbsolutePath().substring(0, patchFile.getAbsolutePath().indexOf("META-INF" + File.separator + "MANIFEST.MF") - 1));
                            patchFileProps.setProperty("desc", "");
                            patchFileProps.setProperty("action", "replace");
                        }
                        if (this.hasFilePatternMatch(patchFile, appHomeSearchStr = File.separator + "applications" + File.separator + "labvantage", patchFileProps)) {
                            patchFileProps.setProperty("target", "apphome");
                            patchFileProps.setProperty("path", patchFile.getAbsolutePath().substring(0, patchFile.getAbsolutePath().indexOf(appHomeSearchStr) + appHomeSearchStr.length()));
                            patchFileProps.setProperty("file", patchFile.getAbsolutePath().substring(patchFile.getAbsolutePath().indexOf(appHomeSearchStr) + appHomeSearchStr.length() + 1));
                            patchFileProps.setProperty("desc", "");
                            patchFileProps.setProperty("action", "replace");
                            match = true;
                        }
                        if (!match) {
                            patchFileProps.setProperty("target", "none");
                            patchFileProps.setProperty("file", name);
                            patchFileProps.setProperty("path", patchFile.getAbsolutePath().substring(0, patchFile.getAbsolutePath().indexOf(name) - 1));
                            patchFileProps.setProperty("desc", "");
                            patchFileProps.setProperty("action", "none");
                            match = true;
                        }
                    } else {
                        patchFileProps.setProperty("type", "D");
                        if (this.hasFilePatternMatch(patchFile, "console" + File.separator + "install" + File.separator, patchFileProps) || this.hasFilePatternMatch(patchFile, "console" + File.separator + "languages" + File.separator, patchFileProps) || this.hasFilePatternMatch(patchFile, "console" + File.separator + "procedures" + File.separator, patchFileProps) || this.hasFilePatternMatch(patchFile, "console" + File.separator + "templates" + File.separator, patchFileProps)) {
                            patchFileProps.setProperty("target", "lvh");
                            patchFileProps.setProperty("desc", "");
                            patchFileProps.setProperty("action", "replace");
                            match = true;
                        }
                        if (!match && (this.hasFilePatternMatch(patchFile, STANDARD_WEBDIR + File.separator, patchFileProps) || this.hasFilePatternMatch(patchFile, "WEB-OPAL" + File.separator, patchFileProps) || this.hasParentPatternMatch(patchFile, "*.war", patchFileProps))) {
                            patchFileProps.setProperty("target", "war");
                            patchFileProps.setProperty("desc", "");
                            patchFileProps.setProperty("action", "replace");
                            match = true;
                        }
                        if (!match && name.endsWith(".jar")) {
                            if (this.hasFilePatternMatch(patchFile, "lib" + File.separator, patchFileProps)) {
                                patchFileProps.setProperty("target", "ear");
                                patchFileProps.setProperty("desc", "");
                                patchFileProps.setProperty("action", "replace");
                                match = true;
                            } else {
                                patchFileProps.setProperty("target", "ear");
                                patchFileProps.setProperty("file", name);
                                patchFileProps.setProperty("path", patchFile.getAbsolutePath().substring(0, patchFile.getAbsolutePath().indexOf(name) - 1));
                                patchFileProps.setProperty("desc", "");
                                patchFileProps.setProperty("action", "replace");
                                match = true;
                            }
                        }
                        if (!match && this.hasFilePatternMatch(patchFile, "sapphire" + File.separator + "custom", patchFileProps)) {
                            patchFileProps.setProperty("target", "jar");
                            patchFileProps.setProperty("jar", this.findParentJAR(patchFile));
                            patchFileProps.setProperty("desc", "");
                            patchFileProps.setProperty("action", "replace");
                            match = true;
                        }
                        String appHomeSearchStr = File.separator + "applications" + File.separator + "labvantage";
                        if (!match && this.hasFilePatternMatch(patchFile, appHomeSearchStr, patchFileProps)) {
                            patchFileProps.setProperty("target", "apphome");
                            patchFileProps.setProperty("path", patchFile.getAbsolutePath().substring(0, patchFile.getAbsolutePath().indexOf(appHomeSearchStr) + appHomeSearchStr.length()));
                            patchFileProps.setProperty("file", patchFile.getAbsolutePath().substring(patchFile.getAbsolutePath().indexOf(appHomeSearchStr) + appHomeSearchStr.length() + 1));
                            patchFileProps.setProperty("desc", "");
                            patchFileProps.setProperty("action", "replace");
                            match = true;
                        }
                        if (!match && this.hasFilePatternMatch(patchFile, "console", patchFileProps)) {
                            patchFileProps.setProperty("target", "console");
                            patchFileProps.setProperty("file", name);
                            patchFileProps.setProperty("path", patchFile.getAbsolutePath().substring(0, patchFile.getAbsolutePath().indexOf(name) - 1));
                            patchFileProps.setProperty("desc", "");
                            patchFileProps.setProperty("action", "backupreplace");
                            match = true;
                        }
                        if (!match) {
                            patchFileProps.setProperty("target", "none");
                            patchFileProps.setProperty("file", name);
                            patchFileProps.setProperty("path", patchFile.getAbsolutePath().substring(0, patchFile.getAbsolutePath().indexOf(name) - 1));
                            patchFileProps.setProperty("desc", "");
                            patchFileProps.setProperty("action", "none");
                            match = true;
                        }
                    }
                    if (!match) {
                        throw new Exception("Unrecognized patch file '" + patchFile.getAbsolutePath() + "'");
                    }
                    break block31;
                }
                throw new Exception("Patch file '" + patchFile.getAbsolutePath() + "' does not exist");
            }
            catch (Exception e) {
                patchFileProps.setProperty("status", "error");
                patchFileProps.setProperty("statusmessage", "Failed to derive patch file properties. Reason: " + e.getMessage());
                Trace.logError(LOGGER, (Object)("Failed to derive patch file properties. Reason: " + e.getMessage()), e);
            }
        }
        return patchFileProps.toJSONString(false);
    }

    private boolean hasFilePatternMatch(File patchFile, String pattern, PropertyList patchFileProps) {
        int pos = patchFile.getAbsolutePath().indexOf(pattern);
        if (pos >= 0) {
            patchFileProps.setProperty("file", patchFile.getAbsolutePath().substring(pos));
            patchFileProps.setProperty("path", patchFile.getAbsolutePath().substring(0, pos - 1));
            return true;
        }
        return false;
    }

    private boolean hasParentPatternMatch(File patchFile, String pattern, PropertyList patchFileProps) {
        String file = patchFile.getName();
        File parent = patchFile.getParentFile();
        do {
            if (FileUtil.wildcardMatch(parent.getAbsolutePath(), pattern, false)) {
                patchFileProps.setProperty("file", file);
                patchFileProps.setProperty("path", parent.getAbsolutePath());
                return true;
            }
            file = parent.getName() + File.separator + file;
        } while ((parent = parent.getParentFile()) != null);
        return false;
    }

    private String findParentJAR(File patchFile) {
        File parent = patchFile.getParentFile();
        do {
            if (!FileUtil.wildcardMatch(parent.getAbsolutePath(), "*.jar", false)) continue;
            return parent.getName();
        } while ((parent = parent.getParentFile()) != null);
        return STANDARD_JAR;
    }

    private boolean isPatchZipFile(File patchFile) {
        try {
            return ZipFileUtil.entryExists(patchFile, "component.xml") || ZipFileUtil.entryExists(patchFile, "patch.xml");
        }
        catch (SapphireException sapphireException) {
            return false;
        }
    }

    private boolean isCTTZipFile(File patchFile) {
        try {
            Configuration configuration = this.getConfiguration();
            File tempDir = ConsoleController.getConsoleTemp(configuration.getSapphireHome());
            ZipFileUtil.extractFile(patchFile, "META-INF/MANIFEST.MF", tempDir);
            Properties manifest = ConsoleController.loadPropertiesFile(new File(tempDir, "META-INF/MANIFEST.MF"));
            try {
                FileUtil.deleteAll(tempDir);
            }
            catch (IOException iOException) {
                // empty catch block
            }
            return manifest != null && manifest.containsKey("exportbuild");
        }
        catch (SapphireException sapphireException) {
            return false;
        }
    }

    private String savePatchXML(PropertyList commandProps) {
        PropertyList saveProps = new PropertyList();
        try {
            PatchXML patchXML = new PatchXML(new File(commandProps.getProperty("patchxmlfile")));
            PropertyListCollection patches = commandProps.getCollection("patches");
            if (patches != null) {
                for (int i = 0; i < patches.size(); ++i) {
                    PropertyList patch = patches.getPropertyList(i);
                    PatchXML.Patch patchXMLPatch = patchXML.getPatchInstance(patch.getProperty("type"), patch.getProperty("patchid"), patch.getProperty("version"), patch.getProperty("patchfile"), patch.getProperty("desc"), DateTimeUtil.getNowCalendar());
                    patchXML.addPatch(patchXMLPatch);
                    PropertyListCollection patchFiles = patch.getCollection("patchfiles");
                    if (patchFiles == null) continue;
                    for (int j = 0; j < patchFiles.size(); ++j) {
                        PropertyList patchFile = patchFiles.getPropertyList(j);
                        patchXMLPatch.addPatchFile(patchXML.getPatchFileInstance(patchFile.getProperty("target"), patchFile.getProperty("jar"), patchFile.getProperty("file"), patchFile.getProperty("type"), patchFile.getProperty("path"), patchFile.getProperty("desc"), patchFile.getProperty("action"), patchFile.getProperty("build"), patchFile.getProperty("dbms"), patchFile.getProperty("platform")));
                    }
                }
            }
            patchXML.save();
        }
        catch (Exception e) {
            saveProps.setProperty("status", "error");
            saveProps.setProperty("statusmessage", "Failed to save patch XML. Reason: " + e.getMessage());
            Trace.logError(LOGGER, (Object)("Failed to save patch XML. Reason: " + e.getMessage()), e);
        }
        return saveProps.toJSONString(false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String buildPatchFile(PropertyList commandProps) {
        PropertyList buildProps = new PropertyList();
        ArrayList<String> fileList = new ArrayList<String>();
        try {
            PropertyListCollection patches = commandProps.getCollection("patches");
            if (patches != null && patches.size() == 1) {
                PropertyList patch = patches.getPropertyList(0);
                File patchZipFileLocation = new File(commandProps.getProperty("patchfilelocation"));
                File patchZipFile = new File(patchZipFileLocation.isFile() ? patchZipFileLocation.getParentFile() : patchZipFileLocation, patch.getProperty("patchfile"));
                if (patchZipFile.exists()) {
                    patchZipFile.delete();
                }
                patchZipFile.getParentFile().mkdirs();
                PackagerFile patchZip = new PackagerFile(patchZipFile);
                Configuration configuration = this.getConfiguration();
                File tempDir = ConsoleController.getConsoleTemp(configuration.getSapphireHome());
                PropertyListCollection patchFiles = patch.getCollection("patchfiles");
                if (patchFiles != null) {
                    for (int j = 0; j < patchFiles.size(); ++j) {
                        String fileCheck;
                        PropertyList patchFile = patchFiles.getPropertyList(j);
                        String patchTarget = patchFile.getProperty("target", "none").equals("none") ? "misc" : patchFile.getProperty("target");
                        String file = patchFile.getProperty("file");
                        if (patchFile.getProperty("build").equals("Y")) {
                            if (patchFile.getProperty("type").equals("T")) {
                                String databaseid = patchFile.getProperty("path");
                                PropertyList props = new PropertyList();
                                props.put("databaseid", databaseid);
                                props.put("loadextendeddata", "N");
                                PropertyList databaseConfig = new PropertyList(new JSONObject(this.loadDatabase(props)));
                                DBUtil db = new DBUtil();
                                String patchFileName = file + ".zip";
                                String fileCheck2 = patchTarget + "|" + patchFileName;
                                if (fileList.contains(fileCheck2)) {
                                    throw new SapphireException("Duplicate patch file " + patchFileName + " found. Use different file name.");
                                }
                                fileList.add(fileCheck2);
                                try {
                                    db.setConnection(databaseConfig.getProperty("dbms"), ConsoleController.getDataSource(configuration, databaseConfig.getProperty("jndiname")).getConnection());
                                    TransferPackage transferPackage = new TransferPackage();
                                    transferPackage.loadTransferPackage(db, file, "1");
                                    transferPackage.run(db, "", new StringLogger(), new File(tempDir, patchFileName));
                                    patchZip.addFile(patchTarget, tempDir, tempDir, patchFileName);
                                    patchFile.setProperty("file", patchFileName);
                                    continue;
                                }
                                catch (Exception e) {
                                    Trace.logError(LOGGER, (Object)("Failed to load transfer package. Reason: " + e.getMessage()), e);
                                    continue;
                                }
                                finally {
                                    db.reset();
                                    db.releaseConnection();
                                }
                            }
                            if (!patchFile.getProperty("type").equals("D") || !file.toLowerCase().endsWith(".jar")) continue;
                            fileCheck = patchTarget + "|" + file;
                            if (fileList.contains(fileCheck)) {
                                throw new SapphireException("Duplicate patch file " + file + " found. Use different file name.");
                            }
                            fileList.add(fileCheck);
                            tempDir.mkdirs();
                            PackagerFile jarFile = new PackagerFile(new File(tempDir, file));
                            File[] jarFiles = new File(new File(patchFile.getProperty("path")), file).listFiles();
                            for (int i = 0; i < jarFiles.length; ++i) {
                                jarFile.addFile(jarFiles[i].getParentFile(), jarFiles[i]);
                            }
                            jarFile.addManifestAttribute("Built-By", patch.getProperty("patchid"));
                            jarFile.save();
                            patchZip.addFile(patchTarget, tempDir, tempDir, file);
                            patchFile.setProperty("type", "F");
                            continue;
                        }
                        fileCheck = patchTarget + "|" + file;
                        if (fileList.contains(fileCheck)) {
                            throw new SapphireException("Duplicate patch file " + file + " found. Use different file name.");
                        }
                        fileList.add(fileCheck);
                        patchZip.addFile(patchTarget, new File(patchFile.getProperty("path")), new File(patchFile.getProperty("path"), file));
                    }
                }
                File patchXML = new File(tempDir, patch.getProperty("type").equals("C") ? "component.xml" : "patch.xml");
                patchXML.getParentFile().mkdirs();
                commandProps.setProperty("patchxmlfile", patchXML.getAbsolutePath());
                this.savePatchXML(commandProps);
                patchZip.addFile(patchXML.getParentFile(), patchXML);
                patchZip.save();
                buildProps.setProperty("patchfile", patchZip.getFile().getAbsolutePath());
            }
        }
        catch (Exception e) {
            buildProps.setProperty("status", "error");
            buildProps.setProperty("statusmessage", "Failed to build patch file. Reason: " + e.getMessage());
            Trace.logError(LOGGER, (Object)("Failed to build patch file. Reason: " + e.getMessage()), e);
        }
        return buildProps.toJSONString(false);
    }

    private String loadPatchFile(PropertyList commandProps) {
        PropertyList patchProps = new PropertyList();
        try {
            File patchFile = new File(commandProps.getProperty("patchfile"));
            if (!patchFile.exists() || patchFile.isDirectory()) {
                throw new Exception("Invalid patch file!");
            }
            Configuration configuration = this.getConfiguration();
            File patchXMLFile = ZipFileUtil.extractFile(patchFile, ZipFileUtil.entryExists(patchFile, "patch.xml") ? "patch.xml" : "component.xml", ConsoleController.getConsoleTemp(configuration.getSapphireHome()));
            PatchXML patchXML = new PatchXML(patchXMLFile);
            patchXML.load();
            patchProps.setProperty("patches", patchXML.getPatchesCollection());
        }
        catch (Exception e) {
            patchProps.setProperty("status", "error");
            patchProps.setProperty("statusmessage", "Failed to load patch file. Reason: " + e.getMessage());
            Trace.logError(LOGGER, (Object)("Failed to load patch file. Reason: " + e.getMessage()), e);
        }
        return patchProps.toJSONString(false);
    }

    private String hasExistingHome(PropertyList commandProps) {
        PropertyList appProps = new PropertyList();
        String applicationid = commandProps.getProperty("applicationid");
        try {
            Configuration configuration = this.getConfiguration();
            File appHome = new File(configuration.getSapphireHome(), "applications/" + applicationid);
            appProps.setProperty("hasexistinghome", appHome.exists() && appHome.isDirectory() ? "Y" : "N");
        }
        catch (Exception e) {
            appProps.setProperty("status", "error");
            appProps.setProperty("statusmessage", "Failed to determine existing home. Reason: " + e.getMessage());
            Trace.logError(LOGGER, (Object)("Failed to determine existing home. Reason: " + e.getMessage()), e);
        }
        return appProps.toJSONString(false);
    }

    private String test(PropertyList commandProps) {
        PropertyList testProps = new PropertyList();
        try {
            if (!commandProps.getProperty("test").equals("analyzelogfile") && commandProps.getProperty("test").equals("sortdataset")) {
                DataSet ds = new DataSet(new JSONObject(new JSONTokener(commandProps.getProperty("dataset"))));
                ds.sort(commandProps.getProperty("sort"));
                testProps.setProperty("dataset", JSONUtil.toJSONString(ds));
            }
        }
        catch (Exception e) {
            testProps.setProperty("status", "error");
            testProps.setProperty("statusmessage", "Failed to perform test. Reason: " + e.getMessage());
            Trace.logError(LOGGER, (Object)("Failed to perform test. Reason: " + e.getMessage()), e);
        }
        return testProps.toJSONString(false);
    }

    public static void getExplodedApplicationProps(File earDir, PropertyList props) {
        if (earDir.exists()) {
            try {
                File[] earFiles;
                Properties appBuildProps = ConsoleController.loadPropertiesFile(new File(earDir, "/META-INF/application-build.props"));
                String applicationJAR = appBuildProps.getProperty("application.jar", "");
                if (applicationJAR.equals(STANDARD_JAR) || applicationJAR.equals("labvantage.jar")) {
                    appBuildProps.setProperty("application.jar", "");
                }
                props.putAll(appBuildProps);
                props.setProperty("managedear.location", earDir.getParentFile().getAbsolutePath());
                props.setProperty(STANDARD_JAR, STANDARD_JAR);
                props.setProperty("application.classpath", ConsoleController.getManifestValue(new File(earDir, "sapphire.jar/META-INF/MANIFEST.MF"), "Class-Path"));
                String[] coreJARList = Configuration.getCommonJarList();
                for (int i = 0; i < coreJARList.length; ++i) {
                    coreJARList[i] = coreJARList[i].toLowerCase();
                }
                HashSet<String> coreJARFiles = new HashSet<String>(Arrays.asList(coreJARList));
                for (File earFile : earFiles = earDir.listFiles()) {
                    if (coreJARFiles.contains(earFile.getName().toLowerCase())) {
                        coreJARFiles.remove(earFile.getName().toLowerCase());
                        continue;
                    }
                    if (!earFile.getName().equals(STANDARD_JAR) && !earFile.getName().equals(STANDARD_WAR) && !earFile.getName().equals("META-INF") && (!earFile.isDirectory() || !earFile.getName().endsWith(".jar")) && earFile.isFile() && !earFile.getName().endsWith(".jar")) continue;
                }
            }
            catch (Exception e) {
                Trace.logError(LOGGER, (Object)("Failed to get exploded application properties. Reason: " + e.getMessage()), e);
            }
        } else {
            props.setProperty("application.status", "NE");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static PropertyList getManifestValues(File manifest) throws IOException {
        PropertyList values = new PropertyList();
        try (FileInputStream fis = null;){
            fis = new FileInputStream(manifest);
            Manifest mf = new Manifest(fis);
            Attributes attributes = mf.getMainAttributes();
            for (Attributes.Name name : attributes.keySet()) {
                values.setProperty(name.toString(), attributes.getValue(name));
            }
        }
        return values;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String getManifestValue(File manifest, String name) throws IOException {
        String value = "";
        try (FileInputStream fis = null;){
            fis = new FileInputStream(manifest);
            Manifest mf = new Manifest(fis);
            Attributes attributes = mf.getMainAttributes();
            value = attributes.getValue(name);
        }
        return value;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void setManifestValue(File manifest, String name, String value) throws IOException {
        Manifest mf = null;
        try (FileInputStream fis = null;){
            fis = new FileInputStream(manifest);
            mf = new Manifest(fis);
        }
        Attributes attributes = mf.getMainAttributes();
        attributes.putValue(name, value);
        String version = attributes.getValue(Attributes.Name.MANIFEST_VERSION.toString());
        if (version == null || version.length() == 0) {
            attributes.putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
        }
        try (FileOutputStream fos = null;){
            fos = new FileOutputStream(manifest);
            mf.write(fos);
        }
    }

    private String loadPackagingProps(PropertyList commandProps) {
        PropertyList packagingProps;
        block18: {
            packagingProps = new PropertyList();
            try {
                File propsFile = new File(commandProps.getProperty("applicationpropsfile"));
                if (propsFile.exists()) {
                    packagingProps.putAll(ConsoleController.loadPropertiesFile(propsFile));
                    if (packagingProps.containsKey("project")) {
                        packagingProps.setProperty("application.id", packagingProps.getProperty("project"));
                        packagingProps.remove("project");
                    }
                    if (packagingProps.containsKey("project.root")) {
                        packagingProps.setProperty("application.rootdir", packagingProps.getProperty("project.root"));
                        packagingProps.remove("project.root");
                    }
                    if (packagingProps.containsKey("project.ear")) {
                        packagingProps.setProperty("application.ear", StringUtil.replaceAll(packagingProps.getProperty("project.ear"), "${project}", packagingProps.getProperty("application.id")));
                        packagingProps.remove("project.ear");
                    }
                    if (packagingProps.containsKey("project.build")) {
                        packagingProps.setProperty("application.builddir", packagingProps.getProperty("project.build"));
                        packagingProps.remove("project.build");
                    }
                    if (packagingProps.containsKey("project.temp")) {
                        packagingProps.setProperty("application.tempdir", StringUtil.replaceAll(packagingProps.getProperty("project.temp"), "${project.root}", "${application.rootdir}"));
                        packagingProps.remove("project.temp");
                    }
                    if (packagingProps.containsKey("project.applicationserver")) {
                        packagingProps.setProperty("application.appserver", packagingProps.getProperty("project.applicationserver"));
                        packagingProps.remove("project.applicationserver");
                    }
                    if (packagingProps.containsKey("project.java")) {
                        packagingProps.setProperty("application.javasourcedir", StringUtil.replaceAll(packagingProps.getProperty("project.java"), "${project.root}", "${application.rootdir}"));
                        packagingProps.remove("project.java");
                    }
                    if (packagingProps.containsKey("project.jar")) {
                        packagingProps.setProperty("application.jar", StringUtil.replaceAll(packagingProps.getProperty("project.jar"), "${project}", packagingProps.getProperty("application.id")));
                        packagingProps.remove("project.jar");
                    }
                    if (packagingProps.containsKey("project.includesource")) {
                        packagingProps.setProperty("application.includesource", packagingProps.getProperty("project.includesource"));
                        packagingProps.remove("project.includesource");
                    }
                    if (packagingProps.containsKey("project.external")) {
                        packagingProps.setProperty("application.externaldir", StringUtil.replaceAll(packagingProps.getProperty("project.external"), "${project.root}", "${application.rootdir}"));
                        packagingProps.remove("project.external");
                    }
                    if (packagingProps.containsKey("project.externaljars")) {
                        packagingProps.setProperty("application.externaljars", packagingProps.getProperty("project.externaljars"));
                        packagingProps.remove("project.externaljars");
                    }
                    if (packagingProps.containsKey("project.webapp")) {
                        packagingProps.setProperty("application.webappsourcedir", StringUtil.replaceAll(packagingProps.getProperty("project.webapp"), "${project.root}", "${application.rootdir}"));
                        packagingProps.remove("project.webapp");
                    }
                    if (packagingProps.containsKey("project.war")) {
                        packagingProps.setProperty("application.war", StringUtil.replaceAll(packagingProps.getProperty("project.war"), "${project}", packagingProps.getProperty("application.id")));
                        packagingProps.remove("project.war");
                    }
                    if (packagingProps.containsKey("project.webcontext")) {
                        packagingProps.setProperty("application.webcontext", packagingProps.getProperty("project.webcontext"));
                        packagingProps.remove("project.webcontext");
                    }
                    if (packagingProps.containsKey("project.ejbprefix")) {
                        packagingProps.setProperty("application.ejbprefix", packagingProps.getProperty("project.ejbprefix"));
                        packagingProps.remove("project.ejbprefix");
                    }
                    break block18;
                }
                throw new Exception("Application properties file '" + propsFile.getAbsolutePath() + "' not found!");
            }
            catch (Exception e) {
                packagingProps.setProperty("status", "error");
                packagingProps.setProperty("statusmessage", e.getMessage());
                Trace.logError(LOGGER, (Object)("Failed to load packaging properties. Reason: " + e.getMessage()), e);
            }
        }
        return packagingProps.toJSONString(false);
    }

    private String savePackagingProps(PropertyList commandProps) {
        PropertyList packagingProps = new PropertyList();
        try {
            int i;
            File[] files;
            File sourcedir;
            File propsFile = new File(commandProps.getProperty("applicationpropsfile"));
            PropertyList applicationProps = commandProps.getPropertyList("applicationprops");
            PropertyList resolvedProps = new PropertyList();
            resolvedProps.putAll(AntUtil.resolveParams(applicationProps));
            if (applicationProps.getProperty("application.rootdir").length() > 0 && applicationProps.getProperty("application.webappsourcedir").length() > 0) {
                StringBuffer warfileincludes = new StringBuffer();
                sourcedir = new File(resolvedProps.getProperty("application.webappsourcedir"));
                files = sourcedir.listFiles();
                if (files != null) {
                    for (i = 0; i < files.length; ++i) {
                        if (files[i].isDirectory()) {
                            warfileincludes.append(files[i].getName()).append("/**/* ");
                            continue;
                        }
                        warfileincludes.append(files[i].getName()).append(" ");
                    }
                    applicationProps.put("application.warfileincludes", warfileincludes.length() > 0 ? warfileincludes.toString() : "");
                }
            }
            StringBuffer earfileincludes = new StringBuffer();
            if (applicationProps.getProperty("application.rootdir").length() > 0 && applicationProps.getProperty("application.javasourcedir").length() > 0 && applicationProps.getProperty("application.includesource").length() > 0 && (files = (sourcedir = new File(resolvedProps.getProperty("application.javasourcedir"))).listFiles()) != null) {
                for (i = 0; i < files.length; ++i) {
                    if (files[i].isDirectory()) {
                        earfileincludes.append(files[i].getName()).append("/**/*.java ");
                        continue;
                    }
                    if (!files[i].getName().endsWith(".java")) continue;
                    earfileincludes.append(files[i].getName()).append(" ");
                }
            }
            if (applicationProps.getProperty("application.externaljars").length() > 0) {
                earfileincludes.append(StringUtil.replaceAll(applicationProps.getProperty("application.externaljars"), ",", " "));
            }
            applicationProps.put("application.earfileincludes", earfileincludes.toString());
            Properties props = new Properties();
            props.putAll((Map<?, ?>)applicationProps);
            ConsoleController.savePropertiesFile(props, propsFile, "LabVantage Build Properties");
            packagingProps.setProperty("applicationpropsfile", commandProps.getProperty("applicationpropsfile"));
        }
        catch (Exception e) {
            packagingProps.setProperty("status", "error");
            packagingProps.setProperty("statusmessage", e.getMessage());
            Trace.logError(LOGGER, (Object)("Failed to save packaging properties. Reason: " + e.getMessage()), e);
        }
        return packagingProps.toJSONString(false);
    }

    private String loadEARUpgradeParams(PropertyList commandProps) {
        PropertyList upgradeProps = new PropertyList();
        File temp = null;
        try {
            Configuration configuration = this.getConfiguration();
            String applicationid = commandProps.getProperty("applicationid");
            String managedEAR = commandProps.getProperty("managedear");
            if (commandProps.getProperty("applicationtype", "P").equals("P")) {
                File appHome = this.getAppHome(configuration, applicationid);
                File ear = this.getManagedEAR(appHome, managedEAR);
                temp = new File(appHome, "temp");
                try {
                    File sapphireProps = ZipFileUtil.extractFile(ear, "META-INF/application-build.props", temp);
                    if (!sapphireProps.exists()) {
                        throw new Exception();
                    }
                    upgradeProps.putAll(ConsoleController.getEARBuildProps(ear, ConsoleController.getConsoleTemp(configuration.getSapphireHome())));
                }
                catch (Exception e) {
                    File wsejbxml;
                    Element el;
                    String jndiName;
                    File jbejbxml;
                    Element el2;
                    String jndiName2;
                    Document dom = null;
                    Element docEle = null;
                    NodeList nl = null;
                    File appxml = ZipFileUtil.extractFile(ear, "META-INF/application.xml", temp);
                    upgradeProps.putAll(ConsoleController.getApplicationXMLProps(appxml));
                    File sapphireejbjar = ZipFileUtil.extractFile(ear, "sapphireejb.jar", temp);
                    File wlejbxml = ZipFileUtil.extractFile(sapphireejbjar, "META-INF/weblogic-ejb-jar.xml", temp);
                    dom = DOMUtil.getNewDocument(wlejbxml, false, "-//BEA Systems, Inc.//DTD WebLogic 7.0.0 EJB//EN");
                    docEle = dom.getDocumentElement();
                    nl = docEle.getElementsByTagName("local-jndi-name");
                    if (nl != null && nl.getLength() > 0 && !(jndiName2 = (el2 = (Element)nl.item(0)).getFirstChild().getNodeValue()).substring(0, jndiName2.lastIndexOf("/ejb/")).equals("com/labvantage/sapphire")) {
                        upgradeProps.setProperty("application.ejbprefix", jndiName2.substring(0, jndiName2.lastIndexOf("/ejb/")));
                        upgradeProps.setProperty("application.appserver", "weblogic");
                    }
                    if (upgradeProps.getProperty("application.appserver").length() == 0 && (nl = (docEle = (dom = DOMUtil.getNewDocument(jbejbxml = ZipFileUtil.extractFile(sapphireejbjar, "META-INF/jboss.xml", temp), false, "-//JBoss//DTD JBOSS//EN")).getDocumentElement()).getElementsByTagName("local-jndi-name")) != null && nl.getLength() > 0 && !(jndiName = (el = (Element)nl.item(0)).getFirstChild().getNodeValue()).substring(0, jndiName.lastIndexOf("/ejb/")).equals("com/labvantage/sapphire")) {
                        upgradeProps.setProperty("application.ejbprefix", jndiName.substring(0, jndiName.lastIndexOf("/ejb/")));
                        upgradeProps.setProperty("application.appserver", "jboss");
                    }
                    if (upgradeProps.getProperty("application.appserver").length() == 0 && (nl = (docEle = (dom = DOMUtil.getNewDocument(wsejbxml = ZipFileUtil.extractFile(sapphireejbjar, "META-INF/ibm-ejb-jar-bnd.xmi", temp), false)).getDocumentElement()).getElementsByTagName("ejbBindings")) != null && nl.getLength() > 0 && !(jndiName = (el = (Element)nl.item(0)).getAttribute("jndiName")).substring(0, jndiName.lastIndexOf("/ejb/")).equals("com/labvantage/sapphire")) {
                        upgradeProps.setProperty("application.ejbprefix", jndiName.substring(0, jndiName.lastIndexOf("/ejb/")));
                        upgradeProps.setProperty("application.appserver", "websphere");
                    }
                }
            } else {
                Properties appBuildProps = ConsoleController.loadPropertiesFile(new File(new File(((JBoss)((Object)configuration)).getDeploymentDir(), managedEAR), "/META-INF/application-build.props"));
                ((JBoss)((Object)configuration)).getApplicationUpgradeFiles(managedEAR, upgradeProps, appBuildProps);
            }
        }
        catch (Exception e) {
            upgradeProps.setProperty("application.sourcetype", "Unknown");
            Trace.logError(LOGGER, (Object)("Failed to load EAR upgrade properties. Reason: " + e.getMessage()), e);
        }
        try {
            if (temp != null) {
                FileUtil.deleteAll(temp);
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return upgradeProps.toJSONString(false);
    }

    public static PropertyList getApplicationXMLProps(File applicationXML) {
        PropertyList applicationProps = new PropertyList();
        try {
            Element el;
            int i;
            Document dom = DOMUtil.getNewDocument(applicationXML, false, "-//Sun Microsystems, Inc.//DTD J2EE Application 1.3//EN");
            Element docEle = dom.getDocumentElement();
            NodeList nl = docEle.getElementsByTagName("ejb");
            if (nl != null && nl.getLength() > 0) {
                for (i = 0; i < nl.getLength(); ++i) {
                    el = (Element)nl.item(i);
                    applicationProps.setProperty("standard.jar", el.getFirstChild().getNodeValue());
                }
            }
            if ((nl = docEle.getElementsByTagName("web")) != null && nl.getLength() > 0) {
                for (i = 0; i < nl.getLength(); ++i) {
                    el = (Element)nl.item(i);
                    applicationProps.setProperty("application.war", BaseConverter.getTextValue(el, "web-uri"));
                    applicationProps.setProperty("application.webcontext", BaseConverter.getTextValue(el, "context-root"));
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return applicationProps;
    }

    public static PropertyList getJNDIProps(File jarDir, String appserver) {
        PropertyList jndiProps = new PropertyList();
        Document dom = null;
        Element docEle = null;
        NodeList nl = null;
        try {
            File jbejbxml = new File(jarDir, "META-INF/jboss.xml");
            File wlejbxml = new File(jarDir, "META-INF/weblogic-ejb-jar.xml");
            File wsejbxml = new File(jarDir, "META-INF/ibm-ejb-jar-bnd.xmi");
            if (appserver.startsWith("JBoss") && jbejbxml.exists()) {
                dom = DOMUtil.getNewDocument(jbejbxml, false, "-//JBoss//DTD JBOSS//EN");
                docEle = dom.getDocumentElement();
                nl = docEle.getElementsByTagName("local-jndi-name");
                if (nl != null && nl.getLength() > 0) {
                    Element el = (Element)nl.item(0);
                    String jndiName = el.getFirstChild().getNodeValue();
                    jndiProps.setProperty("application.ejbprefix", jndiName.substring(0, jndiName.lastIndexOf("/ejb/")));
                }
            } else if (appserver.equalsIgnoreCase("WebLogic") && wlejbxml.exists()) {
                dom = DOMUtil.getNewDocument(wlejbxml, false, "-//BEA Systems, Inc.//DTD WebLogic 7.0.0 EJB//EN");
                docEle = dom.getDocumentElement();
                nl = docEle.getElementsByTagName("local-jndi-name");
                if (nl != null && nl.getLength() > 0) {
                    Element el = (Element)nl.item(0);
                    String jndiName = el.getFirstChild().getNodeValue();
                    jndiProps.setProperty("application.ejbprefix", jndiName.substring(0, jndiName.lastIndexOf("/ejb/")));
                }
            } else if (appserver.equalsIgnoreCase("WebSphere") && wsejbxml.exists() && (nl = (docEle = (dom = DOMUtil.getNewDocument(wsejbxml, false)).getDocumentElement()).getElementsByTagName("ejbBindings")) != null && nl.getLength() > 0) {
                Element el = (Element)nl.item(0);
                String jndiName = el.getAttribute("jndiName");
                jndiProps.setProperty("application.ejbprefix", jndiName.substring(0, jndiName.lastIndexOf("/ejb/")));
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return jndiProps;
    }

    private String loadPlatform() {
        PropertyList platformProps = new PropertyList();
        try {
            Configuration configuration = this.getConfiguration();
            platformProps = configuration.getPlatformProperties();
        }
        catch (Exception e) {
            PropertyListCollection errors = new PropertyListCollection();
            platformProps.setProperty("errors", errors);
            PropertyList error = new PropertyList();
            error.setProperty("error", "Failed to load platform properties. Reason:" + e.getMessage());
            errors.add(error);
            Trace.logError(LOGGER, (Object)("Failed to load platform properties. Reason: " + e.getMessage()), e);
        }
        return platformProps.toJSONString(false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String maintApplication(PropertyList commandProps) {
        String status;
        block10: {
            status = "ok";
            DBUtil adb = new DBUtil();
            try {
                Configuration configuration = this.getConfiguration();
                File configFile = ConsoleController.getConfigFile(configuration);
                adb.setConnection(ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindbms"), ConsoleController.getDataSource(configuration, ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindb")).getConnection());
                boolean edit = commandProps.getProperty("edit").equals("Y");
                String applicationid = commandProps.getProperty("applicationid");
                adb.createPreparedResultSet("SELECT applicationid FROM application WHERE applicationid = ?", applicationid);
                if (edit) {
                    if (adb.getNext()) {
                        if (commandProps.containsKey("managedear")) {
                            adb.executePreparedUpdate("UPDATE application SET applicationdesc = ?, source = ?, managedear = ? WHERE applicationid = ?", new Object[]{commandProps.getProperty("applicationdesc"), commandProps.getProperty("source"), commandProps.getProperty("managedear"), applicationid});
                        } else {
                            adb.executePreparedUpdate("UPDATE application SET applicationdesc = ? WHERE applicationid = ?", new Object[]{commandProps.getProperty("applicationdesc"), applicationid});
                        }
                    } else {
                        status = "Failed to find application '" + applicationid + "'!";
                    }
                    break block10;
                }
                throw new SapphireException("Should never reach this bit of code!");
            }
            catch (Exception e) {
                status = "Reason: " + e.getMessage();
                Trace.logError(LOGGER, (Object)("Failed to edit application details. Reason: " + e.getMessage()), e);
            }
            finally {
                adb.reset();
                adb.releaseConnection();
            }
        }
        return status;
    }

    private String genApplicationRAK(PropertyList commandProps) {
        PropertyList applicationConfig = new PropertyList();
        String applicationid = commandProps.getProperty("applicationid");
        String managedear = commandProps.getProperty("managedear");
        String databaseid = commandProps.getProperty("databaseid");
        try {
            Configuration configuration = this.getConfiguration();
            File configFile = ConsoleController.getConfigFile(configuration);
            File rak = new File(configuration.getSapphireHome(), "rakfiles/" + applicationid + "_" + databaseid + ".rak");
            rak.getParentFile().mkdirs();
            String password = commandProps.getProperty("password");
            if (password != null && password.indexOf("{|}") == 0) {
                password = EncryptDecrypt.decryptConsoleRSA(this.getConfigPropsFile(this.getConfiguration()), password.substring("{|}".length()));
            }
            RemoteAccessKey.generateRemoteAccessKey(rak, Configuration.getPlatformName(configuration.getPlatform()), configuration.getInitialContextFactory(), applicationid, managedear, databaseid, configuration.getProviderURL(), commandProps.getProperty("username"), password, ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.jndiprefix", "com/labvantage/sapphire"));
            applicationConfig.setProperty("rakfile", rak.getAbsolutePath());
            applicationConfig.setProperty("status", "ok");
        }
        catch (Exception e) {
            applicationConfig.setProperty("status", "error");
            applicationConfig.setProperty("statusmessage", "Failed to generate RAK file for application '" + applicationid + "'! Reason: " + e.getMessage());
            Trace.logError(LOGGER, (Object)("Failed to generate RAK file. Reason: " + e.getMessage()), e);
        }
        return applicationConfig.toJSONString(false);
    }

    private String processApplicationCommand(PropertyList commandProps) {
        PropertyList applicationConfig = new PropertyList();
        String applicationid = commandProps.getProperty("applicationid");
        String type = commandProps.getProperty("applicationtype");
        String managedear = commandProps.getProperty("managedear");
        String command = commandProps.getProperty("applicationcommand");
        try {
            Configuration configuration = this.getConfiguration();
            File appHome = this.getAppHome(configuration, applicationid);
            PropertyList earProps = null;
            if (type.equals("P")) {
                earProps = ConsoleController.getEARConfig(configuration, this.getManagedEAR(appHome, managedear), ConsoleController.getConsoleTemp(configuration.getSapphireHome()));
            } else if (type.equals("J")) {
                earProps = ConsoleController.getEARConfig(configuration, new File(appHome, managedear), null);
            }
            if (earProps != null && earProps.getProperty("application.webcontext").length() > 0) {
                this.sendWSRequest(command, commandProps, configuration, earProps.getProperty("application.webcontext"));
                applicationConfig.setProperty("status", "ok");
            } else {
                applicationConfig.setProperty("status", "error");
                applicationConfig.setProperty("statusmessage", "Failed to process command '" + command + "' for application '" + applicationid + "'! Reason: Missing application.webcontext property in EAR");
            }
        }
        catch (Exception e) {
            applicationConfig.setProperty("status", "error");
            applicationConfig.setProperty("statusmessage", "Failed to process command '" + command + "' for application '" + applicationid + "'! Reason: " + e.getMessage());
            Trace.logError(LOGGER, (Object)("Failed to process command. Reason: " + e.getMessage()), e);
        }
        return applicationConfig.toJSONString(false);
    }

    private void sendWSRequest(String command, PropertyList commandProps, Configuration configuration, String webcontext) throws SapphireException, ServiceException, MalformedURLException, RemoteException {
        String endpoint = "http://" + configuration.getServerHostName() + ":" + configuration.getHttpPort() + "/" + webcontext + "/services/SapphireWS";
        Service service = new Service();
        Call call = (Call)service.createCall();
        call.setTargetEndpointAddress(new URL(endpoint));
        call.setOperationName(new QName("http://labvantage.com/", "processApplicationCommand"));
        String retProps = (String)call.invoke(new Object[]{command, commandProps.toXMLString()});
        PropertyList props = new PropertyList();
        props.setPropertyList(retProps);
        commandProps.putAll(props);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String restoreApplication(PropertyList commandProps) {
        String status = "ok";
        DBUtil adb = new DBUtil();
        try {
            Configuration configuration = this.getConfiguration();
            File configFile = ConsoleController.getConfigFile(configuration);
            adb.setConnection(ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindbms"), ConsoleController.getDataSource(configuration, ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindb")).getConnection());
            String applicationid = commandProps.getProperty("applicationid");
            File ear = this.getManagedEAR(this.getAppHome(configuration, applicationid), commandProps.getProperty("managedear"));
            File last = new File(ear.getAbsolutePath() + "-last");
            FileUtil.copyFile(last, ear);
        }
        catch (Exception e) {
            status = "Reason: " + e.getMessage();
            Trace.logError(LOGGER, (Object)("Failed to restore application. Reason: " + e.getMessage()), e);
        }
        finally {
            adb.reset();
            adb.releaseConnection();
        }
        return status;
    }

    private String saveConsoleConfig(PropertyList commandProps) {
        return ConsoleController.saveConsoleConfig(commandProps, this.getConfiguration());
    }

    public static String saveConsoleConfig(PropertyList commandProps, Configuration configuration) {
        String status = "ok";
        try {
            File configFile = ConsoleController.getConfigFile(configuration);
            Properties props = new Properties();
            if (configFile != null) {
                props.putAll((Map<?, ?>)ConsoleController.loadPropertiesFile(configFile));
            } else {
                configFile = new File(configuration.getSapphireHome() + "/console/consoleconfig.props");
            }
            props.setProperty("com.labvantage.sapphire.server.admindb", commandProps.getProperty("admindb"));
            props.setProperty("com.labvantage.sapphire.server.admindbms", commandProps.getProperty("admindbms"));
            props.setProperty("com.labvantage.sapphire.server.language", commandProps.getProperty("language"));
            ConsoleController.savePropertiesFile(props, configFile, "LabVantage Console Config Properties");
        }
        catch (Exception e) {
            status = "Reason: " + e.getMessage();
            Trace.logError(LOGGER, (Object)("Failed to save console configuration. Reason: " + e.getMessage()), e);
        }
        return status;
    }

    private String saveLogonPageConfig(PropertyList commandProps) {
        String status = "ok";
        try {
            Configuration configuration = this.getConfiguration();
            String applicationid = commandProps.getProperty("applicationid");
            File appHome = this.getAppHome(configuration, applicationid);
            File logonxml = new File(appHome, "labvantagelogon.xml");
            PropertyList defProps = new PropertyList();
            LogonPageTag.setDefaults(defProps);
            PropertyList defaultTextProps = defProps.getPropertyList("text");
            PropertyList defaultBrowserCheckProps = defProps.getPropertyList("browsercheck");
            PropertyList props = new PropertyList();
            this.addOverridePropValueIfNotEmpty(commandProps, defProps, props, "applicationtitle");
            this.addOverridedPropValue(commandProps, defProps, props, "applicationimage");
            this.addOverridedPropValue(commandProps, defProps, props, "applicationimagetitle");
            this.addOverridedPropValue(commandProps, defProps, props, "headercolor");
            this.addOverridePropValueIfNotEmpty(commandProps, defProps, props, "pagetitle");
            this.addOverridedPropValue(commandProps, defProps, props, "defaultdatabase");
            this.addOverridedPropValue(commandProps, defProps, props, "selectdatabase");
            this.addOverridedPropValue(commandProps, defProps, props, "autocomplete");
            this.addOverridedPropValue(commandProps, defProps, props, "rtl");
            this.addOverridedPropValue(commandProps, defProps, props, "showdatabase");
            this.addOverridedPropValue(commandProps, defProps, props, "showresetpassword");
            this.addOverridedPropValue(commandProps, defProps, props, "websso");
            this.addOverridedPropValue(commandProps, defProps, props, "webssodatabase");
            this.addOverridedPropValue(commandProps, defProps, props, "webssologoffurl");
            this.addOverridedPropValue(commandProps, defProps, props, "webssoesigurl");
            this.addOverridedPropValue(commandProps, defProps, props, "useridattributename");
            this.addOverridedPropValue(commandProps, defProps, props, "webssoallowheaderattributes");
            this.addOverridedPropValue(commandProps, defProps, props, "subdomainmode");
            this.addOverridedPropValue(commandProps, defProps, props, "maindomain");
            this.addOverridedPropValue(commandProps, defProps, props, "databaselist");
            PropertyList browsercheck = props.getPropertyList("browsercheck");
            if (browsercheck == null) {
                browsercheck = new PropertyList();
                props.setProperty("browsercheck", browsercheck);
            }
            this.addOverridePropValueIfNotEmpty(commandProps, defaultBrowserCheckProps, browsercheck, "stoplogin");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultBrowserCheckProps, browsercheck, "allowchrome");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultBrowserCheckProps, browsercheck, "minchrome");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultBrowserCheckProps, browsercheck, "maxchrome");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultBrowserCheckProps, browsercheck, "allowedge");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultBrowserCheckProps, browsercheck, "minedge");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultBrowserCheckProps, browsercheck, "maxedge");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultBrowserCheckProps, browsercheck, "allowsafari");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultBrowserCheckProps, browsercheck, "minsafari");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultBrowserCheckProps, browsercheck, "maxsafari");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultBrowserCheckProps, browsercheck, "allowmobile");
            PropertyList text = props.getPropertyList("text");
            if (text == null) {
                text = new PropertyList();
                props.setProperty("text", text);
            }
            this.addOverridePropValueIfNotEmpty(commandProps, defaultTextProps, text, "enterdatabaselogon");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultTextProps, text, "databaseusername");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultTextProps, text, "databasepassword");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultTextProps, text, "database");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultTextProps, text, "logonbutton");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultTextProps, text, "resetpasswordbutton");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultTextProps, text, "returntologonbutton");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultTextProps, text, "forgotcredentials");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultTextProps, text, "forgotcredentialsmessage");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultTextProps, text, "passwordreset");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultTextProps, text, "enterresetpassword");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultTextProps, text, "databaseemail");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultTextProps, text, "requestemailsubject");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultTextProps, text, "requestemailmessage");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultTextProps, text, "requestemailsent");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultTextProps, text, "resetemailsubject");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultTextProps, text, "resetemailmessage");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultTextProps, text, "resetemailsent");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultTextProps, text, "invalidresetpassword");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultTextProps, text, "invalidusernameemail");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultTextProps, text, "invalidsystememail");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultTextProps, text, "expiredresetlink");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultTextProps, text, "emailsendfail");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultTextProps, text, "externalauthentication");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultTextProps, text, "forgotdisableduser");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultTextProps, text, "invaliddatabaselogon");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultTextProps, text, "invalidbrowser");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultTextProps, text, "unsupportedbrowser");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultTextProps, text, "cookiesnotenabled");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultTextProps, text, "logonunsecureconnection");
            this.addOverridePropValueIfNotEmpty(commandProps, defaultTextProps, text, "logonsecureurl");
            if (commandProps.getProperty("databasealias").length() > 0) {
                props.setProperty("databasealias", commandProps.getPropertyList("databasealias"));
            }
            if (commandProps.getCollection("languages") != null) {
                props.setProperty("languages", commandProps.getCollection("languages"));
            }
            FileUtil.writeFileString(logonxml, props.toXMLString());
        }
        catch (Exception e) {
            status = "Reason: " + e.getMessage();
            Trace.logError(LOGGER, (Object)("Failed to save logon page configuration. Reason: " + e.getMessage()), e);
        }
        return status;
    }

    private void addOverridePropValueIfNotEmpty(PropertyList commandProps, PropertyList defaultProps, PropertyList props, String propertyId) {
        if (commandProps.getProperty(propertyId).length() > 0 && !defaultProps.getProperty(propertyId).equals(commandProps.getProperty(propertyId))) {
            props.setProperty(propertyId, commandProps.getProperty(propertyId));
        }
    }

    private String saveApplicationConfig(PropertyList commandProps) {
        String status = "ok";
        try {
            Configuration configuration = this.getConfiguration();
            String applicationid = commandProps.getProperty("applicationid");
            File appHome = this.getAppHome(configuration, applicationid);
            File labvantageConfig = new File(appHome, "labvantageconfig.props");
            Properties props = ConsoleController.loadPropertiesFile(labvantageConfig.exists() ? labvantageConfig : new File(appHome, "sapphireconfig.props"));
            props.setProperty("com.labvantage.sapphire.server.admindb", commandProps.getProperty("admindb"));
            props.setProperty("com.labvantage.sapphire.server.admindbms", commandProps.getProperty("admindbms"));
            props.setProperty("com.labvantage.sapphire.server.jndiprefix", commandProps.getProperty("jndiprefix"));
            props.setProperty("com.labvantage.sapphire.server.jndisecurityprotocol", commandProps.getProperty("jndisecurityprotocol"));
            props.setProperty("com.labvantage.sapphire.server.passwordvalidator", commandProps.getProperty("passwordvalidator"));
            props.setProperty("com.labvantage.sapphire.server.passwordvalidatornode", commandProps.getProperty("passwordvalidatornode"));
            props.setProperty("com.labvantage.sapphire.server.authenticationpropertytree", commandProps.getProperty("authenticationpropertytree"));
            props.setProperty("com.labvantage.sapphire.server.authenticationpropertytreenode", commandProps.getProperty("authenticationpropertytreenode"));
            props.setProperty("com.labvantage.sapphire.server.httpport", commandProps.getProperty("httpport"));
            props.setProperty("com.labvantage.sapphire.server.providerport", commandProps.getProperty("providerport"));
            props.setProperty("com.labvantage.sapphire.server.expirepages", commandProps.getProperty("expirepages"));
            props.setProperty("com.labvantage.sapphire.server.webappbaseurl", commandProps.getProperty("webappbaseurl"));
            props.setProperty("com.labvantage.sapphire.server.smtphost", commandProps.getProperty("smtphost"));
            props.setProperty("com.labvantage.sapphire.server.emailfromaddress", commandProps.getProperty("emailfromaddress"));
            props.setProperty("com.labvantage.sapphire.server.securityemailtoaddress", commandProps.getProperty("securityemailtoaddress"));
            props.setProperty("com.labvantage.sapphire.server.filter.compression", commandProps.getProperty("compressionfilter"));
            props.setProperty("com.labvantage.sapphire.server.filter.addheaders", commandProps.getProperty("addheadersfilter"));
            props.setProperty("com.labvantage.sapphire.server.filter.addhstsheader", commandProps.getProperty("addhstsheader"));
            props.setProperty("com.labvantage.sapphire.server.filter.rejectrequest", commandProps.getProperty("rejectrequestfilter"));
            props.setProperty("com.labvantage.sapphire.server.enablesystemuser", commandProps.getProperty("enablesystemuser"));
            props.setProperty("com.labvantage.sapphire.server.showtranslations", commandProps.getProperty("showtranslations"));
            props.setProperty("com.labvantage.sapphire.server.ignorebuild", commandProps.getProperty("ignorebuild"));
            FileOutputStream fos = new FileOutputStream(labvantageConfig);
            props.store(fos, "LabVantage Config Properties");
            fos.close();
        }
        catch (Exception e) {
            status = "Reason: " + e.getMessage();
            Trace.logError(LOGGER, (Object)("Failed to save application configuration. Reason: " + e.getMessage()), e);
        }
        return status;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String loadTransferPackages(PropertyList commandProps) {
        PropertyList transferPackageConfig = new PropertyList();
        DataSet transferPackageList = new DataSet();
        DBUtil db = new DBUtil();
        try {
            Configuration configuration = this.getConfiguration();
            db.setConnection(commandProps.getProperty("dbms"), ConsoleController.getDataSource(configuration, commandProps.getProperty("jndiname")).getConnection());
            db.createResultSet("SELECT transferpackageid, transferpackageid FROM transferpackage ORDER BY 1, 2");
            transferPackageList.setResultSet(db.getResultSet());
        }
        catch (Exception e) {
            transferPackageConfig.setProperty("status", "error");
            transferPackageConfig.setProperty("statusmessage", "Failed to load transfer packages. Reason: " + e.getMessage());
            Trace.logError(LOGGER, (Object)("Failed to load transfer packages. Reason: " + e.getMessage()), e);
        }
        finally {
            db.reset();
            db.releaseConnection();
        }
        transferPackageConfig.setProperty("transferpackagelist", JSONUtil.toJSONString(transferPackageList));
        return transferPackageConfig.toJSONString(false);
    }

    private void addOverridedPropValue(PropertyList commandProps, PropertyList defProps, PropertyList props, String attributename) {
        if (!defProps.getProperty(attributename).equals(commandProps.getProperty(attributename))) {
            props.setProperty(attributename, commandProps.getProperty(attributename));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String loadDatabases() {
        PropertyList databaseConfig = new PropertyList();
        PropertyList admindbProps = new PropertyList();
        DataSet databaseList = new DataSet();
        DBUtil adb = new DBUtil();
        try {
            Configuration configuration = this.getConfiguration();
            File configFile = ConsoleController.getConfigFile(configuration);
            String admindb = ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindb");
            String admindbms = ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindbms");
            adb.setConnection(admindbms, ConsoleController.getDataSource(configuration, admindb).getConnection());
            admindbProps.setProperty("admindb", admindb);
            admindbProps.setProperty("admindbms", admindbms);
            admindbProps.setProperty("admindbmsver", adb.getConnection().getMetaData().getDatabaseProductVersion());
            admindbProps.setProperty("admindbusername", adb.getConnection().getMetaData().getUserName());
            try {
                boolean requireLicensePerDatabase;
                adb.createResultSet("SELECT propertyvalue FROM adminconfig WHERE propertyid = 'build'");
                if (adb.getNext()) {
                    int compare = adb.getValue("propertyvalue").compareTo(Build.getBuild());
                    if (compare < 0) {
                        admindbProps.setProperty("state", "upgrade");
                    } else {
                        admindbProps.setProperty("state", "ok");
                    }
                    admindbProps.setProperty("admindbbuild", adb.getValue("propertyvalue"));
                    this.getDatabaseList(configuration, adb, databaseList, true);
                }
                if (configuration instanceof JBoss) {
                    StringBuffer deployedapps = new StringBuffer();
                    HashMap deployedApplications = ((JBoss)((Object)configuration)).getDeployedApplications(ConsoleController.getConsoleTemp(configuration.getSapphireHome()));
                    for (String key : deployedApplications.keySet()) {
                        if (key.endsWith("_exploded")) continue;
                        deployedapps.append(";").append(key);
                    }
                    databaseConfig.setProperty("deployedapps", deployedapps.length() > 0 ? deployedapps.substring(1) : "");
                }
                databaseConfig.setProperty("databaselicensemode", (requireLicensePerDatabase = this.isRequireLicensePerDatabase(configFile)) ? "Y" : "N");
            }
            catch (Exception e) {
                admindbProps.setProperty("state", "install");
            }
        }
        catch (Exception e) {
            databaseConfig.setProperty("status", "error");
            databaseConfig.setProperty("statusmessage", "Failed to load database details. Reason: " + e.getMessage());
            Trace.logError(LOGGER, (Object)("Failed to load databases. Reason: " + e.getMessage()), e);
        }
        finally {
            adb.reset();
            adb.releaseConnection();
        }
        databaseConfig.setProperty("admindb", admindbProps);
        databaseConfig.setProperty("databaselist", JSONUtil.toJSONString(databaseList));
        return databaseConfig.toJSONString(false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void getDatabaseList(Configuration configuration, DBUtil adb, DataSet databaseList, boolean full) throws SapphireException {
        int upgradePatch = 0;
        try {
            upgradePatch = Integer.parseInt(Build.getUpgradePatch());
        }
        catch (Exception exception) {
            // empty catch block
        }
        PropertyList platformConfig = configuration.getPlatformProperties();
        DataSet datasources = null;
        PropertyList jndimap = platformConfig.getPropertyList("jndimap");
        try {
            JSONObject jo = new JSONObject(new JSONTokener(platformConfig.getProperty("datasources")));
            datasources = new DataSet(jo);
        }
        catch (Exception jo) {
            // empty catch block
        }
        adb.createResultSet("SELECT " + (full ? "*" : "databaselistid, jndiname, statusflag, dbms") + " FROM databaselist ORDER BY databaselistid");
        databaseList.setResultSet(adb.getResultSet());
        for (int i = 0; i < databaseList.size(); ++i) {
            databaseList.setString(i, "disabledstatus", databaseList.getString(i, "statusflag").equals("D") || databaseList.getString(i, "statusflag").equals("X") ? "Y" : "N");
            this.getDatabaseStatus(databaseList, i);
            String jndiname = databaseList.getString(i, "jndiname", databaseList.getString(i, "databaselistid"));
            int datasourceRow = -1;
            if (datasources != null && jndimap.size() > 0 && jndimap.containsKey(jndiname)) {
                datasourceRow = Integer.parseInt(jndimap.getProperty(jndiname));
                databaseList.setString(i, "ds_datasourceid", datasources.getValue(datasourceRow, "datasourceid"));
                databaseList.setString(i, "ds_jndiname", datasources.getValue(datasourceRow, "jndiname"));
                databaseList.setString(i, "ds_dbms", datasources.getValue(datasourceRow, "dbms"));
                databaseList.setString(i, "ds_servername", datasources.getValue(datasourceRow, "servername"));
                databaseList.setString(i, "ds_port", datasources.getValue(datasourceRow, "port"));
                databaseList.setString(i, "ds_sidorinstance", datasources.getValue(datasourceRow, "sidorinstance"));
                databaseList.setString(i, "ds_sqldatabase", datasources.getValue(datasourceRow, "sqldatabase"));
                databaseList.setString(i, "ds_username", datasources.getValue(datasourceRow, "username"));
                databaseList.setString(i, "ds_password", datasources.getValue(datasourceRow, "password"));
                databaseList.setString(i, "ds_minpoolsize", datasources.getValue(datasourceRow, "minpoolsize"));
                databaseList.setString(i, "ds_maxpoolsize", datasources.getValue(datasourceRow, "maxpoolsize"));
                databaseList.setString(i, "ds_blockingtimeout", datasources.getValue(datasourceRow, "blockingtimeout"));
                databaseList.setString(i, "ds_idletimeout", datasources.getValue(datasourceRow, "idletimeout"));
            }
            DBUtil sdb = new DBUtil();
            try {
                sdb.setConnection(databaseList.getString(i, "dbms"), ConsoleController.getDataSource(configuration, jndiname).getConnection());
                this.getDatabaseBuild(databaseList, i, sdb);
                this.getDatabasePatch(databaseList, i, upgradePatch, sdb);
                this.getDatabaseOpalConf(databaseList, i, sdb);
                this.getLicenseStatus(configuration, databaseList, i);
                continue;
            }
            catch (Exception e) {
                if (datasourceRow == -1 && configuration.getPlatform() == 4) {
                    databaseList.setString(i, "status", "Missing Datasource");
                    databaseList.setString(i, "statusflag", "MD");
                } else {
                    databaseList.setString(i, "status", "Access Failed");
                    databaseList.setString(i, "statusflag", "CF");
                }
                databaseList.setString(i, "build", "Unknown");
                databaseList.setString(i, "patch", "Unknown");
                continue;
            }
            finally {
                sdb.reset();
                sdb.releaseConnection();
            }
        }
    }

    private void getDatabaseStatus(DataSet databaseList, int i) {
        String statusflag = databaseList.getString(i, "statusflag");
        databaseList.setString(i, "status", statusflag.equals("A") ? "Active" : (statusflag.equals("RU") ? "Requires Upgrade" : (statusflag.equals("RI") ? "Requires Install" : (statusflag.equals("D") ? "Disabled" : (statusflag.equals("X") ? "Invalid" : (statusflag.equals("CF") ? "Connection Failed" : "Unknown Status"))))));
        if (databaseList.getString(i, "dbms").equals("O87")) {
            databaseList.setString(i, "dbms", "ORA");
        }
    }

    private void getDatabaseBuild(DataSet database, int row, DBUtil sdb) throws SQLException, SapphireException {
        sdb.createResultSet("SELECT propertyvalue FROM sysconfig WHERE lower( propertyid ) = 'build'");
        if (sdb.getNext()) {
            String build = sdb.getString("propertyvalue");
            if (build != null && build.length() > 0) {
                database.setString(row, "build", sdb.getString("propertyvalue"));
                if (!build.equals(Build.getBuild())) {
                    database.setString(row, "status", "Requires Upgrade");
                    database.setString(row, "statusflag", "RU");
                }
            } else {
                database.setString(row, "build", "Unknown");
            }
        } else {
            database.setString(row, "build", "Unknown");
        }
    }

    private void getDatabasePatch(DataSet databaseList, int row, int upgradePatch, DBUtil sdb) throws SapphireException {
        sdb.createResultSet("SELECT propertyvalue FROM sysconfig WHERE lower( propertyid ) = 'patch'");
        if (sdb.getNext()) {
            String patch = sdb.getString("propertyvalue");
            if (patch != null && patch.length() > 0) {
                try {
                    int patchnum = Integer.parseInt(patch.substring(patch.lastIndexOf("_") + 1));
                    databaseList.setString(row, "patch", StringUtil.padLeft(String.valueOf(patchnum), 2, '0'));
                    databaseList.setString(row, "patchupgrade", upgradePatch == 0 ? "N" : (patchnum < upgradePatch ? "Y" : "N"));
                }
                catch (Exception e) {
                    databaseList.setString(row, "patch", "Error");
                    databaseList.setString(row, "patchupgrade", upgradePatch == 0 ? "N" : "Y");
                }
            } else {
                databaseList.setString(row, "patch", "None");
                databaseList.setString(row, "patchupgrade", upgradePatch == 0 ? "N" : "Y");
            }
        } else {
            databaseList.setString(row, "patch", "None");
            databaseList.setString(row, "patchupgrade", upgradePatch == 0 ? "N" : "Y");
        }
        if (databaseList.getString(row, "patchupgrade").equals("Y")) {
            databaseList.setString(row, "status", "Requires Upgrade");
            databaseList.setString(row, "statusflag", "RU");
        }
    }

    private void getDatabaseOpalConf(DataSet databaseList, int row, DBUtil sdb) throws SapphireException {
        sdb.createResultSet("SELECT propertyvalue FROM sysconfig WHERE lower( propertyid ) = 'opalconfig'");
        if (sdb.getNext()) {
            String opalConfig = sdb.getString("propertyvalue");
            if (opalConfig != null && opalConfig.length() > 0 && !opalConfig.equals("none")) {
                databaseList.setString(row, "opalconfig", "R4 (" + opalConfig + ")");
            } else {
                databaseList.setString(row, "opalconfig", "None");
            }
        } else {
            databaseList.setString(row, "opalconfig", "Not detected");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String maintDatabase(PropertyList commandProps) {
        PropertyList databaseConfig = new PropertyList();
        String databaseid = commandProps.getProperty("databaseid");
        DBUtil adb = new DBUtil();
        DataSource ds = null;
        try {
            Configuration configuration = this.getConfiguration();
            String action = commandProps.getProperty("action", "edit");
            if (action.equals("edit")) {
                File configFile = ConsoleController.getConfigFile(configuration);
                ds = ConsoleController.getDataSource(configuration, commandProps.getProperty("jndiname", databaseid));
                Connection conn = ds.getConnection();
                String url = conn.getMetaData().getURL();
                String username = conn.getMetaData().getUserName();
                String password = commandProps.getProperty("password");
                if (password != null && password.indexOf("{|}") == 0) {
                    password = EncryptDecrypt.decryptConsoleRSA(this.getConfigPropsFile(this.getConfiguration()), password.substring("{|}".length()));
                }
                Class.forName(commandProps.getProperty("dbms").equals("ORA") ? ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.jdbcdriver.oracle", "oracle.jdbc.driver.OracleDriver") : ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.jdbcdriver.sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver")).newInstance();
                DriverManager.getConnection(url, username, password);
                adb.setConnection(ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindbms"), ConsoleController.getDataSource(configuration, ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindb")).getConnection());
                String dbconnectionkey = EncryptDecrypt.encrypt(commandProps.getProperty("username") + "|" + password);
                adb.executePreparedUpdate("UPDATE databaselist SET jndiname = ?, dbconnectionkey = ? WHERE databaselistid = ?", new Object[]{commandProps.getProperty("jndiname"), dbconnectionkey, databaseid});
            } else if (action.equals("setstatus")) {
                File configFile = ConsoleController.getConfigFile(configuration);
                adb.setConnection(ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindbms"), ConsoleController.getDataSource(configuration, ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindb")).getConnection());
                adb.executePreparedUpdate("UPDATE databaselist SET statusflag = ? WHERE databaselistid = ?", new Object[]{commandProps.getProperty("statusflag"), databaseid});
            }
            databaseConfig.setProperty("status", "ok");
        }
        catch (Exception e) {
            databaseConfig.setProperty("status", "error");
            databaseConfig.setProperty("statusmessage", "Datasource not found or invalid username/password! Reason: " + e.getMessage());
            Trace.logError(LOGGER, (Object)("Failed to maintain database. Reason: " + e.getMessage()), e);
        }
        finally {
            adb.reset();
            adb.releaseConnection();
        }
        return databaseConfig.toJSONString(false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String getLicenseStatus(Configuration configuration, DataSet databaseList, int i) {
        DBUtil db = new DBUtil();
        String licenseStatus = "Invalid";
        try {
            License license;
            if (!databaseList.isValidColumn("licenseerrors")) {
                databaseList.addColumn("licenseerrors", 0);
            }
            Connection conn = ConsoleController.getDataSource(configuration, databaseList.getString(i, "jndiname", "")).getConnection();
            db.setConnection(databaseList.getString(i, "dbms"), conn);
            String databaseId = databaseList.getString(i, "databaselistid", "");
            String licenseTxt = databaseList.getString(i, "licensekey", "");
            boolean requireLicensePerDatabase = this.isRequireLicensePerDatabase(ConsoleController.getConfigFile(configuration));
            if (requireLicensePerDatabase || !licenseTxt.isEmpty()) {
                databaseList.setString(i, "licensetype", "Database");
                license = new License(licenseTxt);
            } else {
                databaseList.setString(i, "licensetype", "Server");
                license = new License(configuration.getServerLicenseFile());
            }
            ArrayList<String> errorList = new ArrayList<String>();
            SapphireService.validateLicenseUserCounts(false, false, errorList, db, databaseId, license);
            if (errorList.isEmpty()) {
                licenseStatus = "Valid";
            } else {
                databaseList.setString(i, "licenseerrors", String.join((CharSequence)", ", errorList));
            }
        }
        catch (Exception exception) {
        }
        finally {
            db.reset();
            db.releaseConnection();
        }
        databaseList.setString(i, "licensestatus", licenseStatus);
        return licenseStatus;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String setLicense(PropertyList commandProps) {
        PropertyList databaseConfig;
        block10: {
            databaseConfig = new PropertyList();
            String databaseid = commandProps.getProperty("databaseid");
            DBUtil adb = new DBUtil();
            DBUtil db = new DBUtil();
            DataSource ds = null;
            try {
                Configuration configuration = this.getConfiguration();
                String licenseText = commandProps.getProperty("license");
                boolean resetLicense = commandProps.getProperty("mode", "setlicense").equals("reset");
                boolean requireDatabaseLicense = this.isRequireLicensePerDatabase(ConsoleController.getConfigFile(configuration));
                License newLicense = resetLicense ? new License(configuration.getServerLicenseFile()) : new License(licenseText);
                ds = ConsoleController.getDataSource(configuration, commandProps.getProperty("jndiname", databaseid));
                Connection conn = ds.getConnection();
                db.setConnection(commandProps.getProperty("dbms"), conn);
                ArrayList<String> errorList = new ArrayList<String>();
                SapphireService.validateLicenseUserCounts(false, false, errorList, db, databaseid, newLicense);
                if (errorList.isEmpty()) {
                    File configFile = ConsoleController.getConfigFile(configuration);
                    adb.setConnection(ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindbms"), ConsoleController.getDataSource(configuration, ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindb")).getConnection());
                    License serverLicense = null;
                    if (configuration.getServerLicenseFile().exists()) {
                        serverLicense = new License(configuration.getServerLicenseFile());
                    }
                    if (ConsoleController.isValidLicenseBasedOnDatabaseCount(databaseid, adb, serverLicense, newLicense, requireDatabaseLicense)) {
                        if (resetLicense) {
                            adb.executePreparedUpdate("UPDATE databaselist SET licensekey = null WHERE databaselistid = ?", new Object[]{databaseid});
                        } else {
                            adb.executePreparedUpdate("UPDATE databaselist SET licensekey = ? WHERE databaselistid = ?", new Object[]{licenseText, databaseid});
                        }
                        databaseConfig.setProperty("status", "ok");
                        break block10;
                    }
                    throw new SapphireException("Maximum database count for license exceeded");
                }
                throw new SapphireException("Problem with license:" + String.join((CharSequence)", ", errorList));
            }
            catch (Exception e) {
                databaseConfig.setProperty("status", "error");
                databaseConfig.setProperty("statusmessage", "Could not set License! Reason: " + e.getMessage());
                Trace.logError(LOGGER, (Object)("Failed to set License for database. Reason: " + e.getMessage()), e);
            }
            finally {
                adb.reset();
                adb.releaseConnection();
                db.reset();
                db.releaseConnection();
            }
        }
        return databaseConfig.toJSONString(false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String testDatabase(PropertyList commandProps) {
        PropertyList databaseConfig = new PropertyList();
        DataSource ds = null;
        Connection conn = null;
        try {
            License testLicense;
            Configuration configuration = this.getConfiguration();
            File configFile = ConsoleController.getConfigFile(configuration);
            ds = ConsoleController.getDataSource(configuration, commandProps.getProperty("jndiname", commandProps.getProperty("databaseid")));
            conn = ds.getConnection();
            String url = conn.getMetaData().getURL();
            String username = conn.getMetaData().getUserName();
            String password = commandProps.getProperty("password");
            if (password != null && password.indexOf("{|}") == 0) {
                password = EncryptDecrypt.decryptConsoleRSA(this.getConfigPropsFile(this.getConfiguration()), password.substring("{|}".length()));
            }
            Class.forName(commandProps.getProperty("dbms").equals("ORA") ? ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.jdbcdriver.oracle", "oracle.jdbc.driver.OracleDriver") : ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.jdbcdriver.sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver")).newInstance();
            DriverManager.getConnection(url, username, password);
            String licenseText = commandProps.getProperty("licensetext");
            if (!licenseText.isEmpty() && (testLicense = new License(licenseText)).isLicenseExpired()) {
                throw new SapphireException("License is expired!");
            }
            databaseConfig.setProperty("status", "ok");
        }
        catch (Exception e) {
            databaseConfig.setProperty("status", "error");
            databaseConfig.setProperty("statusmessage", "Datasource not found, invalid username/password or invalid license. Reason: " + e.getMessage());
            Trace.logError(LOGGER, (Object)("Failed to test database. Reason: " + e.getMessage()), e);
        }
        finally {
            if (conn != null) {
                try {
                    conn.close();
                    conn = null;
                }
                catch (Exception exception) {}
            }
        }
        return databaseConfig.toJSONString(false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String loadDatabase(PropertyList commandProps) {
        PropertyList databaseConfig;
        block26: {
            databaseConfig = new PropertyList();
            DBUtil adb = new DBUtil();
            try {
                Configuration configuration = this.getConfiguration();
                File configFile = ConsoleController.getConfigFile(configuration);
                adb.setConnection(ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindbms"), ConsoleController.getDataSource(configuration, ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindb")).getConnection());
                String databaseid = commandProps.getProperty("databaseid");
                adb.createPreparedResultSet("SELECT * FROM databaselist WHERE databaselistid = ?", databaseid);
                DataSet database = new DataSet(adb.getResultSet());
                if (database.size() == 1) {
                    this.getDatabaseStatus(database, 0);
                    DBUtil sdb = new DBUtil();
                    try {
                        int upgradePatch = 0;
                        try {
                            upgradePatch = Integer.parseInt(Build.getUpgradePatch());
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        sdb.setConnection(database.getString(0, "dbms"), ConsoleController.getDataSource(configuration, database.getString(0, "jndiname", database.getString(0, "databaselistid"))).getConnection());
                        this.getDatabaseBuild(database, 0, sdb);
                        this.getDatabasePatch(database, 0, upgradePatch, sdb);
                        this.getLicenseStatus(configuration, database, 0);
                    }
                    catch (Exception e) {
                        database.setString(0, "status", "Access failed");
                        database.setString(0, "build", "Unknown");
                        database.setString(0, "patch", "Unknown");
                    }
                    finally {
                        sdb.reset();
                        sdb.releaseConnection();
                    }
                    databaseConfig.putAll((HashMap)database.get(0));
                    if (!commandProps.getProperty("loadextendeddata", "Y").equals("Y")) break block26;
                    try {
                        sdb.setConnection(database.getString(0, "dbms"), ConsoleController.getDataSource(configuration, database.getString(0, "jndiname", database.getString(0, "databaselistid"))).getConnection());
                        sdb.createResultSet("SELECT * FROM connection LEFT OUTER JOIN sysuser ON connection.sysuserid = sysuser.sysuserid");
                        DataSet connections = new DataSet(sdb.getResultSet());
                        databaseConfig.setProperty("connections", JSONUtil.toJSONString(connections));
                    }
                    catch (Exception connections) {
                    }
                    finally {
                        sdb.reset();
                        sdb.releaseConnection();
                    }
                    try {
                        sdb.setConnection(database.getString(0, "dbms"), ConsoleController.getDataSource(configuration, database.getString(0, "jndiname", database.getString(0, "databaselistid"))).getConnection());
                        sdb.createResultSet("SELECT * FROM sysconfig WHERE propertyid like 'LV_COMPONENT%' ORDER BY propertyid");
                        DataSet components = new DataSet(sdb.getResultSet());
                        this.parseInstallationEntries(components);
                        databaseConfig.setProperty("components", JSONUtil.toJSONString(components));
                        sdb.createResultSet("SELECT * FROM sysconfig WHERE propertyid like 'LV_PATCH%' ORDER BY propertyid");
                        DataSet patches = new DataSet(sdb.getResultSet());
                        this.parseInstallationEntries(patches);
                        databaseConfig.setProperty("patches", JSONUtil.toJSONString(patches));
                    }
                    catch (Exception e) {
                        Trace.logError(LOGGER, (Object)("Failed to load database patch/component details. Reason: " + e.getMessage()), e);
                    }
                    finally {
                        sdb.reset();
                        sdb.releaseConnection();
                    }
                    PropertyList licenseProps = new PropertyList();
                    try {
                        String licenseTxt = database.getString(0, "licensekey", "");
                        License license = !licenseTxt.isEmpty() ? new License(licenseTxt) : new License(configuration.getServerLicenseFile());
                        licenseProps.putAll(license.getProperties());
                    }
                    catch (SapphireException sapphireException) {
                        // empty catch block
                    }
                    databaseConfig.setProperty("licenseprops", licenseProps);
                    break block26;
                }
                databaseConfig.setProperty("status", "error");
                databaseConfig.setProperty("statusmessage", "Failed to load database details.");
            }
            catch (Exception e) {
                databaseConfig.setProperty("status", "error");
                databaseConfig.setProperty("statusmessage", "Failed to load database details. Reason: " + e.getMessage());
                Trace.logError(LOGGER, (Object)("Failed to load database details. Reason: " + e.getMessage()), e);
            }
            finally {
                adb.reset();
                adb.releaseConnection();
            }
        }
        return databaseConfig.toJSONString(false);
    }

    private void parseInstallationEntries(DataSet sysconfig) {
        for (int i = 0; i < sysconfig.size(); ++i) {
            String propertyid = sysconfig.getValue(i, "propertyid");
            int dateStart = propertyid.startsWith("LV_PATCH") ? 9 : 13;
            Calendar cal = Calendar.getInstance();
            if (propertyid.substring(propertyid.indexOf("-")).length() >= 7) {
                cal.set(2000 + Integer.parseInt(propertyid.substring(dateStart, dateStart + 2)), Integer.parseInt(propertyid.substring(dateStart + 2, dateStart + 4)) - 1, Integer.parseInt(propertyid.substring(dateStart + 4, dateStart + 6)), Integer.parseInt(propertyid.substring(dateStart + 7, dateStart + 9)), Integer.parseInt(propertyid.substring(dateStart + 9, dateStart + 11)), Integer.parseInt(propertyid.substring(dateStart + 11, dateStart + 13)));
            } else {
                cal.set(2000 + Integer.parseInt(propertyid.substring(dateStart, dateStart + 2)), Integer.parseInt(propertyid.substring(dateStart + 2, dateStart + 4)) - 1, Integer.parseInt(propertyid.substring(dateStart + 4, dateStart + 6)), 0, 0, 0);
            }
            sysconfig.setDate(i, "applydt", cal);
            String value = sysconfig.getValue(i, "propertyvalue");
            int pos = value.indexOf(" (v");
            if (pos == -1) {
                sysconfig.setString(i, "patchid", value.substring(value.indexOf(": ") + 2, value.indexOf(" - file ")));
                sysconfig.setString(i, "version", "N/A");
                sysconfig.setString(i, "file", value.substring(value.indexOf(" - file ") + 8, value.lastIndexOf(" executed")));
                continue;
            }
            sysconfig.setString(i, "patchid", value.substring(value.indexOf(": ") + 2, pos));
            sysconfig.setString(i, "version", value.substring(pos + 3, value.indexOf(") - file ", pos)));
            sysconfig.setString(i, "file", value.substring(value.indexOf(" - file ") + 8, value.lastIndexOf(" executed")));
        }
        sysconfig.sort("applydt D");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String deleteDatabase(PropertyList commandProps) {
        String status = "ok";
        DBUtil adb = new DBUtil();
        try {
            Configuration configuration = this.getConfiguration();
            File configFile = ConsoleController.getConfigFile(configuration);
            adb.setConnection(ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindbms"), ConsoleController.getDataSource(configuration, ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindb")).getConnection());
            String databaselistid = commandProps.getProperty("databaseid");
            adb.executePreparedUpdate("DELETE FROM databaselist WHERE databaselistid = ?", new Object[]{databaselistid});
        }
        catch (Exception e) {
            status = "Reason: " + e.getMessage();
            Trace.logError(LOGGER, (Object)("Failed to delete database. Reason: " + e.getMessage()), e);
        }
        finally {
            adb.reset();
            adb.releaseConnection();
        }
        return status;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String maintPlatform(PropertyList commandProps) {
        String status = "ok";
        try {
            Configuration configuration = this.getConfiguration();
            configuration.executePlatformAction(commandProps);
            if (!commandProps.getProperty("status", "ok").equals("ok")) {
                throw new Exception(commandProps.getProperty("status"));
            }
            if (commandProps.getProperty("admindbds").equals("Y")) {
                commandProps.setProperty("admindb", commandProps.getProperty("jndiname"));
                commandProps.setProperty("admindbms", commandProps.getProperty("dbms"));
                status = this.saveConsoleConfig(commandProps);
            }
        }
        catch (Exception e) {
            status = "Reason: " + e.getMessage();
            Trace.logError(LOGGER, (Object)("Failed to maintain platform. Reason: " + e.getMessage()), e);
        }
        return status;
    }

    private String loadConfiguration() {
        PropertyList configProps = new PropertyList();
        configProps.setProperty("build", Build.getBuild());
        configProps.setProperty("patch", Build.getPatch());
        configProps.setProperty("builddate", Build.getBuildDate());
        configProps.setProperty("version", Build.getVersion());
        configProps.setProperty("releaseversion", Build.getReleaseVersion());
        try {
            Configuration configuration = this.getConfiguration();
            configProps.setProperty("platform", Configuration.getPlatformName(configuration.getPlatform()));
            configProps.setProperty("platformversion", configuration.getServerVersion());
            configProps.setProperty("hostname", configuration.getServerHostName());
            configProps.setProperty("sapphirehome", new File(configuration.getSapphireHome()).getAbsolutePath());
            configProps.setProperty("locale", Locale.getDefault().toString());
            File configFile = ConsoleController.getConfigFile(configuration);
            configProps.setProperty("admindb", ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindb"));
            configProps.setProperty("admindbms", ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindbms"));
            configProps.setProperty("language", ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.language"));
            boolean requireLicensePerDatabase = this.isRequireLicensePerDatabase(configFile);
            configProps.setProperty("licensefile", requireLicensePerDatabase ? "Using database specific license" : configuration.getServerLicenseFile().getAbsolutePath());
            PropertyList licenseProps = new PropertyList();
            if (!requireLicensePerDatabase) {
                configuration.setServerLicense(configuration.getServerLicenseFile());
                licenseProps.putAll(configuration.getServerLicense().getProperties());
                configProps.setProperty("licenseprops", licenseProps);
            }
            Properties jvmProps = new Properties();
            jvmProps = System.getProperties();
            DataSet jvmData = new DataSet();
            jvmData.addColumn("property", 0);
            jvmData.addColumn("value", 0);
            for (String string : jvmProps.keySet()) {
                int row = jvmData.addRow();
                jvmData.setValue(row, "property", string);
                jvmData.setValue(row, "value", jvmProps.getProperty(string));
            }
            jvmData.sort("property");
            configProps.setProperty("jvmdataset", JSONUtil.toJSONString(jvmData));
        }
        catch (Exception e) {
            configProps.setProperty("status", "error");
            configProps.setProperty("statusmessage", "Failed to get configuration settings. Reason:" + e.getMessage());
            Trace.logError(LOGGER, (Object)("Failed to load configuration settings. Reason: " + e.getMessage()), e);
        }
        return configProps.toJSONString(false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     */
    private String loadHome() {
        PropertyList configProps;
        block32: {
            configProps = new PropertyList();
            PropertyListCollection statuses = new PropertyListCollection();
            configProps.setProperty("statuses", statuses);
            DBUtil adb = new DBUtil();
            try {
                Configuration configuration = this.getConfiguration();
                File configFile = ConsoleController.getConfigFile(configuration);
                String admindb = ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindb");
                String admindbms = ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindbms");
                adb.setConnection(admindbms, ConsoleController.getDataSource(configuration, admindb).getConnection());
                try {
                    BufferedReader in;
                    block33: {
                        PropertyList status;
                        adb.createResultSet("SELECT propertyvalue FROM adminconfig WHERE propertyid = 'build'");
                        if (!adb.getNext()) break block32;
                        String build = adb.getValue("propertyvalue");
                        int compare = build.compareTo(Build.getBuild());
                        if (compare < 0) {
                            status = new PropertyList();
                            status.setProperty("type", "E");
                            status.setProperty("status", "Admin database (" + build + ") requires upgrade.");
                            status.setProperty("fix", "Upgrade the admin database in the databases panel.");
                            status.setProperty("fixcode", "UADB:");
                            statuses.add(status);
                        } else if (compare > 0) {
                            status = new PropertyList();
                            status.setProperty("type", "E");
                            status.setProperty("status", "Admin database version (" + build + ") newer than " + PRODUCT_NAME + " version.");
                            status.setProperty("fix", "Check you have the right version of LabVantage Console running and/or installed.");
                            statuses.add(status);
                        } else {
                            status = new PropertyList();
                            status.setProperty("type", "M");
                            status.setProperty("status", "Admin database version (" + build + ") correct.");
                            status.setProperty("fix", "");
                            statuses.add(status);
                            PropertyList dbCount = new PropertyList();
                            adb.createResultSet("select * from databaselist ORDER BY databaselistid");
                            DataSet checkLicense = new DataSet();
                            checkLicense.setResultSet(adb.getResultSet());
                            int notLicensedCnt = 0;
                            for (int i = 0; i < checkLicense.getRowCount(); ++i) {
                                String licenseStatus = this.getLicenseStatus(configuration, checkLicense, i);
                                if (licenseStatus.equals("Valid")) continue;
                                ++notLicensedCnt;
                            }
                            dbCount.setProperty("type", "M");
                            dbCount.setProperty("status", checkLicense.getRowCount() + " Sapphire database(s) registered.");
                            dbCount.setProperty("fix", "Use the database panel to manage the databases.");
                            statuses.add(dbCount);
                            if (notLicensedCnt > 0) {
                                PropertyList notLicensedInfo = new PropertyList();
                                notLicensedInfo.setProperty("type", "E");
                                notLicensedInfo.setProperty("status", notLicensedCnt + " " + "LabVantage" + " database(s) with invalid licenses found.");
                                notLicensedInfo.setProperty("fix", "Use the database panel to manage the database specific licenses.");
                                statuses.add(notLicensedInfo);
                            }
                            PropertyList appCount = new PropertyList();
                            appCount.setProperty("type", "M");
                            appCount.setProperty("status", adb.getCount("SELECT count(*) FROM application") + " " + "LabVantage" + " application(s) registered.");
                            appCount.setProperty("fix", "Use the application panel to manage the applications.");
                            statuses.add(appCount);
                        }
                        File apphomeConsoleEAR = new File(ConsoleController.getAppHome(configuration.getSapphireHome(), configuration.getApplicationid()), "ear/labvantageconsole" + Build.getVersionSuffix() + ".ear");
                        File consoleEAR = apphomeConsoleEAR.exists() ? apphomeConsoleEAR : ConsoleController.getConsoleEAR(configuration.getSapphireHome());
                        PropertyList consoleEARProps = null;
                        if (consoleEAR.exists()) {
                            consoleEARProps = ConsoleController.getEARBuildProps(consoleEAR, ConsoleController.getConsoleTemp(configuration.getSapphireHome()));
                            if (!consoleEARProps.getProperty("sapphire.build").equals(Build.getBuild())) {
                                PropertyList status2 = new PropertyList();
                                status2.setProperty("type", "W");
                                status2.setProperty("status", "Deployed console version (" + Build.getBuild() + ") out of sync with console version (" + consoleEARProps.getProperty("sapphire.build") + ") in " + PRODUCT_HOME + "!");
                                status2.setProperty("fix", "Deploy the LabVantage Console EAR in " + PRODUCT_HOME + ".");
                                statuses.add(status2);
                            } else if (!consoleEARProps.getProperty("sapphire.builddate").equals(Build.getBuildDate())) {
                                PropertyList status3 = new PropertyList();
                                status3.setProperty("type", "W");
                                status3.setProperty("status", "Deployed console build date (" + Build.getBuildDate() + ") out of sync with console build date (" + consoleEARProps.getProperty("sapphire.builddate") + ") in " + PRODUCT_HOME + "!");
                                status3.setProperty("fix", "Deploy the LabVantage Console EAR in " + PRODUCT_HOME + ".");
                                statuses.add(status3);
                            }
                        } else {
                            PropertyList status4 = new PropertyList();
                            status4.setProperty("type", "W");
                            status4.setProperty("status", "LabVantage Console EAR missing in " + PRODUCT_HOME + "!");
                            status4.setProperty("fix", "");
                            statuses.add(status4);
                        }
                        File apphomelabvantageEAR = new File(ConsoleController.getAppHome(configuration.getSapphireHome(), configuration.getApplicationid()), "ear/" + configuration.getApplicationid() + ".ear");
                        File labvantageEAR = apphomelabvantageEAR.exists() ? apphomelabvantageEAR : ConsoleController.getLabVantageEAR(configuration.getSapphireHome());
                        if (labvantageEAR.exists()) {
                            PropertyList sapphireEARProps = ConsoleController.getEARBuildProps(labvantageEAR, ConsoleController.getConsoleTemp(configuration.getSapphireHome()));
                            if (consoleEARProps != null && !consoleEARProps.getProperty("sapphire.build").equals(sapphireEARProps.getProperty("sapphire.build"))) {
                                PropertyList status5 = new PropertyList();
                                status5.setProperty("type", "W");
                                status5.setProperty("status", "Console version (" + consoleEARProps.getProperty("sapphire.build") + ") out of sync with installed " + "LabVantage" + " version (" + sapphireEARProps.getProperty("sapphire.build") + ") in " + PRODUCT_HOME + "!");
                                status5.setProperty("fix", "");
                                statuses.add(status5);
                            }
                        } else {
                            PropertyList status6 = new PropertyList();
                            status6.setProperty("type", "W");
                            status6.setProperty("status", "LabVantage EAR missing in " + PRODUCT_HOME + "!");
                            status6.setProperty("fix", "");
                            statuses.add(status6);
                        }
                        URL url = null;
                        InputStream is = null;
                        in = null;
                        try {
                            String inputLine;
                            String downloadserver = ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.downloadserver");
                            String downloadport = ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.downloadport", "8080");
                            String downloadwebapp = ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.downloadwebapp", "labvantageconsole60");
                            String downloadquery = ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.downloadstatusquery", "console?command=downloadstatus");
                            url = new URL("http://" + downloadserver + ":" + downloadport + "/" + downloadwebapp + "/" + downloadquery);
                            is = url.openStream();
                            in = new BufferedReader(new InputStreamReader(is));
                            StringBuffer input = new StringBuffer();
                            while ((inputLine = in.readLine()) != null) {
                                input.append(inputLine);
                            }
                            if (input.length() > 0 && input.toString().compareTo(Build.getBuild()) > 0) {
                                PropertyList status7 = new PropertyList();
                                status7.setProperty("type", "W");
                                status7.setProperty("status", " A newer version of LabVantage is available.");
                                status7.setProperty("fix", "Download and install the latest version of LabVantage");
                                status7.setProperty("fixcode", "DL:");
                                statuses.add(status7);
                            }
                            if (is == null) break block33;
                        }
                        catch (Exception exception) {
                            if (is != null) {
                                is.close();
                            }
                            if (in != null) {
                                in.close();
                            }
                            break block32;
                            catch (Throwable throwable) {
                                if (is != null) {
                                    is.close();
                                }
                                if (in != null) {
                                    in.close();
                                }
                                throw throwable;
                            }
                        }
                        is.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                }
                catch (Exception e) {
                    PropertyList status = new PropertyList();
                    status.setProperty("type", "E");
                    status.setProperty("status", "Admin database not installed correctly.");
                    status.setProperty("fix", "Install the admin database in the databases panel.");
                    status.setProperty("fixcode", "IADB:");
                    statuses.add(status);
                }
            }
            catch (Exception e) {
                PropertyList status = new PropertyList();
                status.setProperty("type", "E");
                status.setProperty("status", "LabVantage Console not configured correctly.");
                status.setProperty("fix", "Check configuration settings and restart.");
                statuses.add(status);
            }
            finally {
                adb.reset();
                adb.releaseConnection();
            }
        }
        return configProps.toJSONString(false);
    }

    private void downloadStatus(HttpServletResponse response) throws SapphireException, IOException {
        Configuration configuration = this.getConfiguration();
        File configFile = ConsoleController.getConfigFile(configuration);
        if (ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.downloadhost").equals("Y")) {
            PrintWriter out = response.getWriter();
            if (new File(configuration.getSapphireHome(), "upload/labvantagehome60.zip").exists()) {
                out.print("0600.010.01");
            } else {
                out.print("NA");
            }
            out.close();
        }
    }

    private void download(HttpServletResponse response) throws SapphireException, IOException {
        Configuration configuration = this.getConfiguration();
        File configFile = ConsoleController.getConfigFile(configuration);
        if (ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.downloadhost").equals("Y")) {
            ServletOutputStream out = response.getOutputStream();
            File file = new File(configuration.getSapphireHome(), "upload/labvantagehome60.zip");
            FileInputStream fis = new FileInputStream(file);
            byte[] ba = new byte[1024];
            int bytesread = 0;
            while ((bytesread = fis.read(ba)) != -1) {
                out.write(ba, 0, bytesread);
            }
            fis.close();
            out.close();
        }
    }

    private void home(HttpServletRequest request, HttpServletResponse response, String loggedOn) throws SapphireException, IOException {
        PropertyList consoleProps = this.getConsoleProps(request, loggedOn, false);
        this.renderBootstrap(response, PRODUCT_NAME, "console", "console.nocache.js", "consoleProps", consoleProps);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private PropertyList getConsoleProps(HttpServletRequest request, String loggedOn, boolean full) throws UnknownHostException, SapphireException {
        PropertyList consoleProps = new PropertyList();
        PropertyListCollection errors = new PropertyListCollection();
        Browser browser = new Browser(request);
        consoleProps.setProperty("browser", browser.getName());
        consoleProps.setProperty("errors", errors);
        consoleProps.setProperty("build", Build.getBuild());
        consoleProps.setProperty("version", Build.getVersion());
        Configuration configuration = this.getConfiguration();
        if (configuration != null) {
            String sapphireHome;
            configuration.setServerHostName(InetAddress.getLocalHost().getHostName());
            consoleProps.setProperty("platform", Configuration.getPlatformName(configuration.getPlatform()));
            consoleProps.setProperty("platformnum", String.valueOf(configuration.getPlatform()));
            consoleProps.setProperty("hostname", InetAddress.getLocalHost().getHostName());
            consoleProps.setProperty("servernodeid", System.getProperty("com.labvantage.sapphire.server.nodeid"));
            consoleProps.setProperty("jndidatasourceprefix", configuration.getJNDIDataSourcePrefix());
            consoleProps.setProperty("fileseparator", File.separator);
            if (configuration instanceof JBoss) {
                consoleProps.setProperty("jboss.deploy", ((JBoss)((Object)configuration)).getDeploymentDir().getAbsolutePath());
                consoleProps.setProperty("jboss.customconfig", System.getProperty("com.labvantage.sapphire.server.jboss.customconfig", "false"));
            }
            if (System.getProperty("com.labvantage.sapphire.server.ignoreplatformcheck", "false").equals("false")) {
                configuration.setSapphireHome(Configuration.getSapphireHome(this.getServletContext()));
                PropertyList platformProps = configuration.getPlatformProperties();
                if (!full) {
                    platformProps.remove("datasources");
                    platformProps.remove("jndimap");
                    platformProps.remove("messagingsuckerpassword");
                    platformProps.remove("messagingjbossbeanssuckerpassword");
                }
                consoleProps.setProperty("platformprops", platformProps);
                if (platformProps.getCollection("errors") != null && platformProps.getCollection("errors").size() > 0) {
                    consoleProps.setProperty("platformerrors", "Y");
                }
            }
            if ((sapphireHome = Configuration.getSapphireHome(this.getServletContext())) != null && new File(sapphireHome).exists()) {
                configuration.setSapphireHome(new File(sapphireHome).getAbsolutePath());
                consoleProps.setProperty("sapphirehome", configuration.getSapphireHome());
                consoleProps.setProperty("sapphirehomesource", Configuration.getSapphireHomeSource(this.getServletContext()));
                File consoleDir = new File(sapphireHome, "console");
                if (consoleDir.exists()) {
                    File sapphireEAR = new File(sapphireHome, "/console/install/ear/labvantage.ear");
                    consoleProps.setProperty("sapphireear", sapphireEAR.getAbsolutePath());
                    consoleProps.setProperty("sapphireearname", sapphireEAR.getName());
                    PropertyList sapphireEARProps = ConsoleController.getEARBuildProps(sapphireEAR, ConsoleController.getConsoleTemp(configuration.getSapphireHome()));
                    consoleProps.setProperty("sapphireear.build", sapphireEARProps.getProperty("sapphire.build"));
                    consoleProps.setProperty("sapphiredocwar", new File(sapphireHome, "/console/install/ear/sapphiredoc.war").getAbsolutePath());
                    File configFile = ConsoleController.getConfigFile(configuration);
                    if (configFile != null) {
                        File licenseFile;
                        boolean requireLicensePerDatabase;
                        consoleProps.setProperty("testplatform", ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.testplatform"));
                        String admindb = ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindb");
                        consoleProps.setProperty("admindb", admindb);
                        if (admindb.length() == 0) {
                            PropertyList error = new PropertyList();
                            error.setProperty("error", "Missing property com.labvantage.sapphire.server.admindb configuration in labvantageconfig.props!");
                            error.setProperty("desc", "This property defines the JNDI lookup name used for accessing the admin database datasource and must be defined in the labvantageconfig.props file to start LabVantage Console .");
                            error.setProperty("fix", "Add com.labvantage.sapphire.server.admindb=[admindbjndiname] in the labvantageconfig.props file, where [admindbjndiname] is the JNDI name setup to access the admin database.");
                            error.setProperty("fixcode", "ECC");
                            errors.add(error);
                        }
                        String admindbms = ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindbms");
                        consoleProps.setProperty("admindbms", admindbms);
                        if (admindbms.length() == 0) {
                            PropertyList error = new PropertyList();
                            error.setProperty("error", "Missing property com.labvantage.sapphire.server.admindbms configuration in labvantageconfig.props!");
                            error.setProperty("desc", "This property defines the DBMS used for accessing the admin database and must be defined in the labvantageconfig.props file to start LabVantage Console.");
                            error.setProperty("fix", "Add com.labvantage.sapphire.server.admindbms=[admindbms] in the labvantageconfig.props file, where [admindbms] is the DBMS hosting the admin database.");
                            error.setProperty("fixcode", "ECC");
                            errors.add(error);
                        }
                        if (!((requireLicensePerDatabase = this.isRequireLicensePerDatabase(configFile)) || (licenseFile = configuration.getServerLicenseFile()) != null && licenseFile.exists())) {
                            PropertyList error = new PropertyList();
                            error.setProperty("error", "Missing license file (labvantage.lic)!");
                            error.setProperty("desc", "LabVantage requires a license file 'labvantage.lic' to run and must be located in " + PRODUCT_HOME + ".");
                            error.setProperty("fix", "Copy your license file to " + PRODUCT_HOME + " - contact " + "LabVantage" + " Solutions if you do not have a license file.");
                            errors.add(error);
                        }
                        DBUtil adb = new DBUtil();
                        try {
                            DataSource ds = ConsoleController.getDataSource(configuration, admindb);
                            Connection conn = ds.getConnection();
                            String dbmsName = conn.getMetaData().getDatabaseProductName().toLowerCase();
                            if (dbmsName.contains("oracle") || dbmsName.contains("microsoft")) {
                                if (dbmsName.contains("oracle") && admindbms.equals("ORA") || dbmsName.contains("microsoft") && admindbms.equals("MSS")) {
                                    adb.setConnection(admindbms, conn);
                                    consoleProps.setProperty(LOGON_COOKIE, loggedOn);
                                }
                                PropertyList error = new PropertyList();
                                error.setProperty("error", "Admindb and Admindb DBMS mismatch.");
                                error.setProperty("desc", "The DBMS for the admin database '" + admindb + "' defined in '" + configFile.getAbsolutePath() + "' is incorrect.");
                                error.setProperty("fix", "Check that your admin database datasource matches the admindb DBMS configured.");
                                error.setProperty("fixcode", "ECC");
                                errors.add(error);
                                consoleProps.setProperty("jbossfix", "Y");
                            }
                            PropertyList error = new PropertyList();
                            error.setProperty("error", "Admindb DBMS is not supported.");
                            error.setProperty("desc", "The DBMS for the admin database '" + admindb + "' defined in '" + configFile.getAbsolutePath() + "' is not supported. " + "LabVantage" + " only supports Oracle and Microsoft SQL Server.");
                            error.setProperty("fix", "Check that your admin database datasource is configured correctly.");
                            errors.add(error);
                            consoleProps.setProperty("jbossfix", "Y");
                        }
                        catch (Exception e) {
                            PropertyList error = new PropertyList();
                            error.setProperty("error", "Failed to locate admin database datasource! Reason: " + e.getMessage());
                            error.setProperty("desc", "The admin database datasource '" + admindb + "' defined in '" + configFile.getAbsolutePath() + "' could not be found or is incorrectly defined on the application server.");
                            error.setProperty("fix", "Check:<br/>1. The admindb datasource has been defined in your application server<br/>2. The com.labvantage.sapphire.server.admindb property is set to the JNDI name for the admin database datasource<br/>3. The datasource JDBC connection properties are correct in the datasource definition");
                            errors.add(error);
                            consoleProps.setProperty("jbossfix", "Y");
                        }
                        finally {
                            adb.reset();
                            adb.releaseConnection();
                        }
                    } else {
                        PropertyList error = new PropertyList();
                        error.setProperty("error", "Missing configuration file consoleconfig.props in " + PRODUCT_HOME + File.separator + "console (" + new File(sapphireHome + "/console").getAbsolutePath() + ")!");
                        error.setProperty("desc", "The configuration file 'consoleconfig.props', which holds essential startup properties, could not be found in " + new File(sapphireHome + "/console").getAbsolutePath() + ".");
                        error.setProperty("fix", "Create a new consoleconfig.props file with the required properties to start LabVantage Console.");
                        error.setProperty("fixcode", "ACC");
                        errors.add(error);
                    }
                } else {
                    PropertyList error = new PropertyList();
                    error.setProperty("error", "Missing console directory in " + PRODUCT_HOME + " (" + consoleDir.getAbsolutePath() + ")!");
                    error.setProperty("desc", "The " + PRODUCT_HOME + " that this server has connected to does not support " + PRODUCT_NAME + "!");
                    error.setProperty("fix", "Make sure the URL for LabVantage Console directs you to a server that has a full " + PRODUCT_HOME + ".");
                    errors.add(error);
                }
            } else {
                PropertyList error = new PropertyList();
                error.setProperty("error", PRODUCT_HOME + " (" + sapphireHome + ") invalid or not defined!");
                error.setProperty("desc", "The home directory for LabVantage could not be resolved or is invalid.");
                error.setProperty("fix", "Define " + PRODUCT_HOME + " by assigning the directory to one of the following:<br/>1. A " + PRODUCT_HOME + " environment variable<br/>2. A " + PRODUCT_HOME + " JVM property<br/>3. A " + PRODUCT_HOME + " context parameter in web.xml.");
                errors.add(error);
            }
        } else {
            PropertyList error = new PropertyList();
            error.setProperty("error", "Platform configuration cannot be created for servlet info '" + this.getServletContext().getServerInfo() + "'!");
            errors.add(error);
        }
        request.getSession().setAttribute("consoleprops", (Object)consoleProps);
        return consoleProps;
    }

    private boolean isRequireLicensePerDatabase(File configFile) {
        return "true".equals(ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.requiredatabaselicense", "false"));
    }

    public static File getConfigFile(Configuration configuration) throws SapphireException {
        return ConsoleController.getConfigPropsFile(configuration, "console", "consoleconfig.props");
    }

    @Override
    protected File getConfigPropsFile(Configuration configuration) throws SapphireException {
        return ConsoleController.getConfigPropsFile(configuration, "console", "consoleconfig.props");
    }

    public static String setConfigProperty(File file, String propertyid, String value) {
        String status = "ok";
        try {
            Properties props = ConsoleController.loadPropertiesFile(file);
            props.setProperty(propertyid, value);
            ConsoleController.savePropertiesFile(props, file, "LabVantage Console Config Properties");
        }
        catch (Exception e) {
            status = "Reason: " + e.getMessage();
        }
        return status;
    }

    private File getAppHome(Configuration configuration, String applicationid) {
        return new File(configuration.getSapphireHome() + "/applications/" + applicationid);
    }

    public static File getAppHome(String sapphireHome, String applicationid) {
        return new File(sapphireHome + "/applications/" + applicationid);
    }

    public static File getLabVantageEAR(String sapphireHome) {
        return new File(sapphireHome, "/console/install/ear/labvantage.ear");
    }

    public static File getConsoleEAR(String sapphireHome) {
        return new File(sapphireHome, "/console/install/ear/labvantageconsole" + Build.getVersionSuffix() + ".ear");
    }

    public static File getConsoleTemp(String sapphireHome) {
        return new File(sapphireHome, "/console/temp");
    }

    private File getManagedEAR(File appHome, String managedEAR) {
        return new File(appHome + "/ear/" + managedEAR);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Properties loadPropertiesFile(File propertiesFile) {
        Properties properties = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(propertiesFile);
            properties.load(fis);
        }
        catch (IOException iOException) {
        }
        finally {
            if (fis != null) {
                try {
                    fis.close();
                }
                catch (IOException iOException) {}
            }
        }
        return properties;
    }

    public static void explodeEAR(File sourceEAR, File earTargetDir, Configuration configuration, File appHome, String applicationid, String applicationjar, String externaljarlist, String applicationwar, String applicationwebdir, String webcontext, String ejbprefix, JspWriter out) throws IOException, SapphireException {
        String sapphireHome = configuration.getSapphireHome();
        if (applicationwar.length() == 0) {
            applicationwar = STANDARD_WAR;
        }
        if (webcontext.length() == 0) {
            webcontext = "labvantage";
        }
        if (ejbprefix.length() == 0) {
            ejbprefix = "com/labvantage/sapphire";
        }
        BaseConverter converter = BaseConverter.getInstance("JBoss");
        GetJarListTask task = new GetJarListTask();
        task.setSeparator(",");
        task.setCustomJars(applicationjar.length() > 0 ? applicationjar + "," + externaljarlist : externaljarlist);
        ConsoleController.jspLiveLog(out, "<b>Exploding EAR '" + sourceEAR.getAbsolutePath() + "' to '" + earTargetDir.getAbsolutePath() + "'</b>");
        if (earTargetDir.exists()) {
            throw new SapphireException("LabVantage EAR cannot be exploded as the directory '" + earTargetDir.getAbsolutePath() + "' already exists!");
        }
        earTargetDir.mkdirs();
        ConsoleFileOperationProgress progress = new ConsoleFileOperationProgress(out);
        ConsoleController.jspLiveLog(out, " - extracting latest LabVantage application files to " + earTargetDir.getAbsolutePath() + "...");
        progress.startProgress();
        ZipFileUtil.extractAll(sourceEAR, earTargetDir, progress);
        progress.endProgress();
        File sapphireJAR = new File(earTargetDir, STANDARD_JAR);
        File sapphireJARTemp = new File(earTargetDir, "sapphire.jar_temp");
        ConsoleController.jspLiveLog(out, " - renaming " + sapphireJAR.getAbsolutePath() + " to " + sapphireJARTemp.getAbsolutePath() + "...");
        File sapphireWAR = new File(earTargetDir, STANDARD_WAR);
        File sapphireWARTemp = new File(earTargetDir, "labvantage.war_temp");
        ConsoleController.jspLiveLog(out, " - renaming " + sapphireWAR.getAbsolutePath() + " to " + sapphireWARTemp.getAbsolutePath() + "...");
        ConsoleController.jspLiveLog(out, "- updating application.xml web context...");
        converter.changeWebContextName(new File(earTargetDir, "META-INF/application.xml"), null, webcontext, applicationwar);
        converter.changeWARName(new File(earTargetDir, "META-INF/jboss-deployment-structure.xml"), null, applicationwar);
        progress.startProgress();
        FileUtil.renameTo(sapphireJAR, sapphireJARTemp, progress);
        progress.endProgress();
        ConsoleController.jspLiveLog(out, " - extracting latest LabVantage code files to " + sapphireJAR.getAbsolutePath() + "...");
        progress.startProgress();
        ZipFileUtil.extractAll(sapphireJARTemp, sapphireJAR, progress);
        progress.endProgress();
        String ddfile = "jboss.xml";
        ConsoleController.jspLiveLog(out, "- updating " + ddfile + " EJB JNDI names...");
        converter.convertEjbJndiName(new File(sapphireJAR, "META-INF/" + ddfile), null, ejbprefix);
        if (configuration instanceof JBossConfiguration) {
            ConsoleController.setManifestValue(new File(sapphireJAR, "/META-INF/MANIFEST.MF"), "Class-Path", task.getJarList());
        }
        sapphireJARTemp.delete();
        if (applicationjar.length() > 0 && !applicationjar.equals(STANDARD_JAR) && applicationid.length() > 0) {
            new File(earTargetDir, (configuration instanceof JBoss6xConfiguration ? "lib/" : "") + applicationjar + "/sapphire/custom/" + applicationid).mkdirs();
        }
        progress.startProgress();
        FileUtil.renameTo(sapphireWAR, sapphireWARTemp, progress);
        progress.endProgress();
        ConsoleController.jspLiveLog(out, " - extracting latest LabVantage web files to " + sapphireWAR.getAbsolutePath() + "...");
        progress.startProgress();
        ZipFileUtil.extractAll(sapphireWARTemp, sapphireWAR, progress);
        progress.endProgress();
        if (applicationid.length() > 0) {
            ConsoleController.jspLiveLog(out, "- updating web.xml applicationid...");
            converter.changeApplicationName(new File(sapphireWAR, "WEB-INF/web.xml"), null, applicationid);
        }
        if (configuration instanceof JBossConfiguration) {
            ConsoleController.setManifestValue(new File(sapphireWAR, "/META-INF/MANIFEST.MF"), "Class-Path", task.getJarList());
        }
        if (applicationwebdir.length() > 0) {
            new File(sapphireWAR, applicationwebdir).mkdirs();
        }
        sapphireWARTemp.delete();
        if (!applicationwar.equals(STANDARD_WAR)) {
            File applicationWAR = new File(earTargetDir, applicationwar);
            ConsoleController.jspLiveLog(out, " - renaming " + sapphireWAR.getAbsolutePath() + " to " + applicationWAR.getAbsolutePath() + "...");
            progress.startProgress();
            FileUtil.renameTo(sapphireWAR, applicationWAR, progress);
            progress.endProgress();
        }
        File applicationProps = new File(earTargetDir, "/META-INF/application-build.props");
        ConsoleController.jspLiveLog(out, " - updating application properties " + applicationProps.getAbsolutePath() + "...");
        Properties appProps = ConsoleController.loadPropertiesFile(applicationProps);
        appProps.setProperty("application.id", applicationid);
        appProps.setProperty("application.ear", applicationid + ".ear");
        appProps.setProperty("application.appserver", "JBoss");
        appProps.setProperty("application.jar", applicationjar);
        appProps.setProperty("application.compilesource", "false");
        appProps.setProperty("application.includesource", "false");
        appProps.setProperty("application.externaljars", externaljarlist);
        appProps.setProperty("application.war", applicationwar);
        appProps.setProperty("application.webcontext", webcontext);
        appProps.setProperty("application.webdir", applicationwebdir);
        appProps.setProperty("application.warfileincludes", applicationwebdir + "/**/*");
        appProps.setProperty("application.ejbprefix", ejbprefix);
        appProps.setProperty(STANDARD_EAR, ConsoleController.getLabVantageEAR(sapphireHome).getAbsolutePath());
        ConsoleController.savePropertiesFile(appProps, applicationProps, "LabVantage Build Properties");
        File buildProps = new File(appHome, "build/application-build.props");
        buildProps.getParentFile().mkdirs();
        ConsoleController.jspLiveLog(out, " - generating build properties in " + buildProps.getAbsolutePath() + "...");
        appProps.setProperty("application.rootdir", appHome.getAbsolutePath());
        appProps.setProperty("application.builddir", "${application.rootdir}" + File.separator + "build");
        appProps.setProperty("application.tempdir", "${application.rootdir}" + File.separator + "temp");
        appProps.setProperty("application.javasourcedir", "${application.rootdir}" + File.separator + "java");
        appProps.setProperty("application.externaldir", "${application.rootdir}" + File.separator + "external");
        appProps.setProperty("application.webappsourcedir", "${application.rootdir}" + File.separator + "webapp");
        appProps.setProperty("application.compilesource", "true");
        appProps.setProperty("application.includesource", "true");
        ConsoleController.savePropertiesFile(appProps, buildProps, "LabVantage Build Properties");
    }

    public static File unexplodeEAR(Configuration configuration, File earSourceDir, File earTargetDir, String earName, String buildRef, JspWriter out) throws IOException, SapphireException {
        File sapphireEAR = new File(earTargetDir, earName);
        ConsoleController.jspLiveLog(out, "<b>Building EAR '" + sapphireEAR.getAbsolutePath() + "' from exploded EAR at '" + earSourceDir.getAbsolutePath() + "'</b>");
        if (!earSourceDir.exists() || !earSourceDir.isDirectory()) {
            throw new SapphireException("Exploded EAR not found or not exploded!");
        }
        File[] sourceEARFiles = earSourceDir.listFiles();
        if (earTargetDir.exists()) {
            FileUtil.deleteAll(earTargetDir);
        }
        earTargetDir.mkdirs();
        ConsoleFileOperationProgress progress = new ConsoleFileOperationProgress(out);
        ArrayList<File> deleteFiles = new ArrayList<File>();
        ConsoleController.jspLiveLog(out, "- building application JAR...");
        File sapphireJARSource = new File(earSourceDir, STANDARD_JAR);
        ConsoleController.createEARIdFile(sapphireJARSource);
        PackagerFile newAppJAR = new PackagerFile(new File(earTargetDir, STANDARD_JAR));
        newAppJAR.addExclusion("MANIFEST.MF");
        newAppJAR.addFile(sapphireJARSource, sapphireJARSource);
        newAppJAR.addManifestAttribute("Built-By", buildRef);
        if (configuration instanceof JBossConfiguration) {
            newAppJAR.addManifestAttribute("Class-Path", ConsoleController.getManifestValue(new File(sapphireJARSource, "META-INF/MANIFEST.MF"), "Class-Path"));
        }
        progress.startProgress(" - collecting JAR files", sapphireJARSource);
        newAppJAR.save(progress);
        progress.endProgress();
        deleteFiles.add(newAppJAR.getFile());
        ConsoleController.jspLiveLog(out, "- building application WAR...");
        PropertyList appXMLProps = ConsoleController.getApplicationXMLProps(new File(earSourceDir, "META-INF/application.xml"));
        String applicationwar = appXMLProps.getProperty("application.war", STANDARD_WAR);
        PackagerFile newAppWAR = new PackagerFile(new File(earTargetDir, applicationwar));
        newAppWAR.addExclusion("MANIFEST.MF");
        File sapphireWARSource = new File(earSourceDir, applicationwar);
        newAppWAR.addFile(sapphireWARSource, sapphireWARSource);
        newAppWAR.addManifestAttribute("Built-By", buildRef);
        if (configuration instanceof JBossConfiguration) {
            newAppWAR.addManifestAttribute("Class-Path", ConsoleController.getManifestValue(new File(sapphireWARSource, "META-INF/MANIFEST.MF"), "Class-Path"));
        }
        progress.startProgress(" - collecting WAR files", sapphireWARSource);
        newAppWAR.save(progress);
        progress.endProgress();
        deleteFiles.add(newAppWAR.getFile());
        ConsoleController.jspLiveLog(out, "- building application EAR...");
        File sapphireMETASource = new File(earSourceDir, "META-INF");
        File appLIBSource = new File(earSourceDir, "APP-INF");
        PackagerFile newAppEAR = new PackagerFile(sapphireEAR);
        newAppEAR.addExclusion("MANIFEST.MF");
        newAppEAR.addFile(earSourceDir, sapphireMETASource);
        if (appLIBSource.exists()) {
            newAppEAR.addFile(earSourceDir, appLIBSource);
        }
        newAppEAR.addFile(earTargetDir, newAppJAR.getFile());
        newAppEAR.addFile(earTargetDir, newAppWAR.getFile());
        int earFileCount = FileUtil.fileCount(sapphireMETASource) + 2;
        if (configuration instanceof JBoss6xConfiguration) {
            File[] libSourceFiles;
            File libSourceDir = new File(earSourceDir, "lib");
            File libTargetDir = new File(earTargetDir, "lib");
            libTargetDir.mkdirs();
            for (File sourceFile : libSourceFiles = libSourceDir.listFiles()) {
                if (sourceFile.isDirectory() && sourceFile.getName().endsWith(".jar")) {
                    ConsoleController.jspLiveLog(out, "- building application JAR " + sourceFile.getAbsolutePath() + "...");
                    File applicationJARSource = new File(libSourceDir, sourceFile.getName());
                    PackagerFile applicationJAR = new PackagerFile(new File(libTargetDir, sourceFile.getName()));
                    applicationJAR.addExclusion("MANIFEST.MF");
                    applicationJAR.addFile(applicationJARSource, applicationJARSource);
                    applicationJAR.addManifestAttribute("Built-By", buildRef);
                    progress.startProgress(" - collecting JAR files", applicationJARSource);
                    applicationJAR.save(progress);
                    progress.endProgress();
                    deleteFiles.add(applicationJAR.getFile());
                    continue;
                }
                if (!sourceFile.isFile() || !sourceFile.getName().endsWith(".jar")) continue;
                FileUtil.copyFile(sourceFile, new File(libTargetDir, sourceFile.getName()));
                deleteFiles.add(new File(libTargetDir, sourceFile.getName()));
            }
            newAppEAR.addFile(earTargetDir, libTargetDir);
            deleteFiles.add(libTargetDir);
        } else {
            String[] coreJARList = Configuration.getCommonJarList();
            for (int i = 0; i < coreJARList.length; ++i) {
                coreJARList[i] = coreJARList[i].toLowerCase();
            }
            HashSet<String> coreJARFiles = new HashSet<String>(Arrays.asList(coreJARList));
            for (File sourceFile : sourceEARFiles) {
                if (coreJARFiles.contains(sourceFile.getName().toLowerCase())) {
                    newAppEAR.addFile(earSourceDir, sourceFile);
                    ++earFileCount;
                    continue;
                }
                if (sourceFile.getName().equals(STANDARD_JAR) || sourceFile.getName().equals(STANDARD_WAR) || sourceFile.getName().equals(applicationwar) || sourceFile.getName().equals("META-INF")) continue;
                if (sourceFile.isDirectory() && sourceFile.getName().endsWith(".jar")) {
                    ConsoleController.jspLiveLog(out, "- building application JAR...");
                    File applicationJARSource = new File(earSourceDir, sourceFile.getName());
                    PackagerFile applicationJAR = new PackagerFile(new File(earTargetDir, sourceFile.getName()));
                    applicationJAR.addExclusion("MANIFEST.MF");
                    applicationJAR.addFile(applicationJARSource, applicationJARSource);
                    applicationJAR.addManifestAttribute("Built-By", buildRef);
                    progress.startProgress(" - collecting JAR files", applicationJARSource);
                    applicationJAR.save(progress);
                    progress.endProgress();
                    newAppEAR.addFile(earTargetDir, applicationJAR.getFile());
                    deleteFiles.add(applicationJAR.getFile());
                    continue;
                }
                if (!sourceFile.isFile() || !sourceFile.getName().endsWith(".jar")) continue;
                newAppEAR.addFile(earSourceDir, sourceFile);
            }
        }
        newAppEAR.addManifestAttribute("Built-By", buildRef);
        progress.startProgress(earFileCount);
        newAppEAR.save(progress);
        progress.endProgress();
        for (File deleteFile : deleteFiles) {
            deleteFile.delete();
        }
        return newAppEAR.getFile();
    }

    public static void backupFile(File file, String operation) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmm");
        Calendar cal = Calendar.getInstance();
        FileUtil.copyFile(file, new File(file.getParentFile(), file.getName() + "-" + sdf.format(cal.getTime()) + "-pre" + operation));
    }

    public static void installApp(ServletContext servletContext, JspWriter out, PropertyList params) throws IOException {
        ConsoleController.installApp(ConsoleController.getConfiguration(servletContext), out, params, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void installApp(Configuration configuration, JspWriter out, PropertyList params, DBUtil adb) throws IOException {
        boolean useIncomingAdm;
        boolean restartRequired;
        block35: {
            restartRequired = false;
            String applicationid = params.getProperty("applicationid");
            String sapphireHome = params.getProperty("sapphire.home");
            String type = params.getProperty("applicationtype");
            File appHome = ConsoleController.getAppHome(sapphireHome, applicationid);
            boolean addClusterNode = params.getProperty("addclusternode", "N").equals("Y");
            ConsoleController.jspLiveLog(out, "<h1>Adding application '" + applicationid + "'</h1>");
            ConsoleController.jspLiveLog(out, PRODUCT_HOME + ": " + sapphireHome);
            ConsoleController.jspLiveLog(out, "Application HOME: " + appHome.getAbsolutePath());
            useIncomingAdm = true;
            if (adb == null) {
                adb = new DBUtil();
                useIncomingAdm = false;
            }
            try {
                File configFile = ConsoleController.getConfigFile(configuration);
                if (!useIncomingAdm) {
                    adb.setConnection(ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindbms"), ConsoleController.getDataSource(configuration, ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindb")).getConnection());
                }
                ConsoleFileOperationProgress progress = new ConsoleFileOperationProgress(out);
                adb.createPreparedResultSet("SELECT applicationid FROM application WHERE applicationid = ?", applicationid);
                if (!adb.getNext()) {
                    File source;
                    if (appHome.exists() && params.getProperty("useexistinghome", "N").equals("N")) {
                        throw new SapphireException("Application '" + applicationid + "' already exists at " + appHome.getAbsolutePath() + "!");
                    }
                    boolean customear = params.getProperty("source").equals("customear");
                    boolean unmanaged = params.getProperty("source").equals("unmanaged");
                    File file = unmanaged ? null : (source = customear ? new File(params.getProperty("customear")) : ConsoleController.getLabVantageEAR(configuration.getSapphireHome()));
                    if (unmanaged || source.exists() && source.isFile()) {
                        File temp = new File(appHome, "temp");
                        DataSet sources = new DataSet();
                        if (customear) {
                            File applicationProps = ZipFileUtil.extractFile(source, "META-INF/application-build.props", temp);
                            Properties appProps = ConsoleController.loadPropertiesFile(applicationProps);
                            if (!appProps.getProperty("application.id").equals(applicationid)) {
                                throw new Exception("Application id (" + applicationid + ") does not match the one found in the " + source.getName() + " EAR (" + appProps.getProperty("application.id") + ")!");
                            }
                        } else if (params.getProperty("source").equals("explodedear")) {
                            ((JBoss)((Object)configuration)).getExplodedApps(sources);
                            PropertyList appProps = new PropertyList();
                            ConsoleController.getExplodedApplicationProps(new File(((JBoss)((Object)configuration)).getDeploymentDir(), sources.getValue(0, "sourcevalue")), appProps);
                            if (!appProps.getProperty("application.id").equals(applicationid)) {
                                throw new Exception("Application id (" + applicationid + ") does not match the one found in the exploded EAR (" + appProps.getProperty("application.id") + ")!");
                            }
                        }
                        String earMsg = "";
                        if (params.getProperty("useexistinghome", "N").equals("N")) {
                            ConsoleController.jspLiveLog(out, " - creating application HOME directories...");
                            appHome.mkdirs();
                            ConsoleController.jspLiveLog(out, " - copying product labels to " + new File(appHome, "labels").getAbsolutePath() + "...");
                            FileUtil.copyAll(new File(configuration.getSapphireHome(), "console/install/labels"), new File(appHome, "labels"));
                            ConsoleController.jspLiveLog(out, " - copying product reports to " + new File(appHome, "reports").getAbsolutePath() + "...");
                            FileUtil.copyAll(new File(configuration.getSapphireHome(), "console/install/reports"), new File(appHome, "reports"));
                            ConsoleController.jspLiveLog(out, " - copying office templates to " + new File(appHome, "officetemplates").getAbsolutePath() + "...");
                            FileUtil.copyAll(new File(configuration.getSapphireHome(), "console/install/officetemplates"), new File(appHome, "officetemplates"));
                            ConsoleController.jspLiveLog(out, " - copying stoplists to " + new File(appHome, "passwordstoplists").getAbsolutePath() + "...");
                            FileUtil.copyAll(new File(configuration.getSapphireHome(), "console/install/passwordstoplists"), new File(appHome, "passwordstoplists"));
                            File dncl = new File(appHome, "dynamicclasslibraries");
                            if (!dncl.exists()) {
                                ConsoleController.jspLiveLog(out, " - copying dynamicclasslibraries to " + dncl.getAbsolutePath() + "...");
                                FileUtil.copyAll(new File(configuration.getSapphireHome(), "console/install/dynamicclasslibraries"), dncl);
                            }
                        } else {
                            ConsoleController.upgradeDynamicClassLibraries(out, appHome, configuration);
                        }
                        if (!unmanaged) {
                            if (type.equals("P")) {
                                ConsoleController.jspLiveLog(out, " - clearing temp area...");
                                if (temp.exists()) {
                                    progress.startProgress(" - determining files to delete...", temp);
                                    FileUtil.deleteAll(temp, progress, false);
                                    progress.endProgress();
                                }
                                temp.mkdirs();
                                File managedear = new File(appHome, "ear/" + applicationid + ".ear");
                                managedear.getParentFile().mkdirs();
                                ConsoleController.jspLiveLog(out, " - copying source EAR to " + managedear.getAbsolutePath() + "...");
                                FileUtil.copyFile(source, managedear);
                                ConsoleController.jspLiveLog(out, " - backing up managed EAR...");
                                FileUtil.copyFile(managedear, new File(appHome, "ear/" + applicationid + ".ear-orig"));
                                earMsg = "EAR - " + managedear.getAbsolutePath();
                                if (!customear && !applicationid.equals("labvantage")) {
                                    File explodedear = new File(temp, "application");
                                    ConsoleController.jspLiveLog(out, " - expanding application EAR to " + explodedear.getAbsolutePath() + "...");
                                    progress.startProgress();
                                    ZipFileUtil.extractAll(managedear, explodedear, progress);
                                    progress.endProgress();
                                    File applicationProps = new File(explodedear, "/META-INF/application-build.props");
                                    ConsoleController.jspLiveLog(out, " - updating application properties " + applicationProps.getAbsolutePath() + "...");
                                    Properties appProps = ConsoleController.loadPropertiesFile(applicationProps);
                                    appProps.setProperty("application.id", applicationid);
                                    appProps.setProperty("application.ear", applicationid + ".ear");
                                    appProps.setProperty("application.appserver", Configuration.getPlatformName(Integer.parseInt(params.getProperty("platform"))));
                                    appProps.setProperty("application.builddir", new File(appHome, "build").getAbsolutePath());
                                    appProps.setProperty("application.tempdir", new File(appHome, "temp").getAbsolutePath());
                                    ConsoleController.savePropertiesFile(appProps, applicationProps, "LabVantage Build Properties");
                                    File sapphireWAR = new File(explodedear, STANDARD_WAR);
                                    File sapphireWARTemp = new File(explodedear, "labvantage.war_temp");
                                    ConsoleController.jspLiveLog(out, " - expanding LabVantage WAR to " + sapphireWARTemp.getAbsolutePath() + "...");
                                    progress.startProgress();
                                    ZipFileUtil.extractAll(sapphireWAR, sapphireWARTemp, progress);
                                    progress.endProgress();
                                    ConsoleController.jspLiveLog(out, "- updating web XML application name...");
                                    BaseConverter converter = BaseConverter.getInstance(appProps.getProperty("application.appserver"));
                                    converter.changeApplicationName(new File(sapphireWARTemp, "WEB-INF/web.xml"), null, applicationid);
                                    ConsoleController.jspLiveLog(out, " - repackaging LabVantage WAR to " + sapphireWAR.getAbsolutePath() + "...");
                                    PackagerFile sapphirewar = new PackagerFile(sapphireWAR);
                                    sapphirewar.addFile(sapphireWARTemp, sapphireWARTemp);
                                    sapphirewar.save();
                                    FileUtil.deleteAll(sapphireWARTemp);
                                    ConsoleController.jspLiveLog(out, " - repackaging application EAR to " + explodedear.getAbsolutePath() + "...");
                                    PackagerFile ear = new PackagerFile(managedear);
                                    ear.addFile(explodedear, explodedear);
                                    ear.save();
                                    FileUtil.deleteAll(explodedear);
                                }
                                if (configuration instanceof JBoss && params.getProperty("deployear").equals("Y")) {
                                    ConsoleController.jspLiveLog(out, " - deploying/copying managed EAR to JBoss...");
                                    ((JBoss)((Object)configuration)).deployEAR(appHome, type, managedear, false, progress);
                                    earMsg = earMsg + " (deployed to JBoss)";
                                }
                                params.setProperty("managedear", managedear.getName());
                            } else if (type.equals("J")) {
                                if (params.getProperty("source").equals("labvantageear")) {
                                    boolean deploy = params.getProperty("deployear", "N").equals("Y");
                                    File sourceEAR = ConsoleController.getLabVantageEAR(params.getProperty("sapphire.home"));
                                    File targetEAR = deploy ? new File(((JBoss)((Object)configuration)).getDeploymentDir(), applicationid + ".ear") : new File(appHome, "/earx/" + applicationid + ".ear");
                                    String applicationjar = params.getProperty("applicationjar");
                                    String externaljarlist = params.getProperty("externaljarlist");
                                    String applicationwar = params.getProperty("applicationwar", STANDARD_WAR);
                                    String applicationwebdir = params.getProperty("applicationwebdir", STANDARD_WEBDIR);
                                    String webcontext = params.getProperty("webcontext", "labvantage");
                                    String ejbprefix = params.getProperty("ejbprefix", "com/labvantage/sapphire");
                                    ConsoleController.explodeEAR(sourceEAR, targetEAR, configuration, appHome, applicationid, applicationjar, externaljarlist, applicationwar, applicationwebdir, webcontext, ejbprefix, out);
                                    earMsg = "EAR - " + targetEAR.getAbsolutePath();
                                    if (deploy && configuration instanceof JBoss6xConfiguration) {
                                        FileUtil.writeFileString(new File(((JBoss)((Object)configuration)).getDeploymentDir(), applicationid + ".ear.dodeploy"), applicationid + ".ear");
                                    }
                                    restartRequired = deploy;
                                    params.setProperty("managedear", applicationid + ".ear");
                                } else {
                                    ConsoleController.jspLiveLog(out, " - using existing exploded application '" + sources.getValue(0, "sourcevalue") + "'...");
                                    params.setProperty("managedear", sources.getValue(0, "sourcevalue"));
                                }
                            }
                        }
                        if (params.getProperty("useexistinghome", "N").equals("N")) {
                            ConsoleController.jspLiveLog(out, " - creating new labvantageconfig.props file in " + appHome.getAbsolutePath() + "...");
                            FileOutputStream fos = new FileOutputStream(new File(appHome, "labvantageconfig.props"));
                            Properties props = new Properties();
                            props.setProperty("com.labvantage.sapphire.server.admindb", ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindb"));
                            props.setProperty("com.labvantage.sapphire.server.admindbms", ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindbms"));
                            props.setProperty("com.labvantage.sapphire.server.jndiprefix", "");
                            props.store(fos, "LabVantage Config Properties");
                            fos.close();
                            ConsoleController.jspLiveLog(out, " - creating new labvantagelogging.properties file in " + appHome.getAbsolutePath() + "...");
                            LogConfig.generateConfigFile(applicationid, new File(appHome, "labvantagelogging.properties"), "", new PropertyList(), false, configuration);
                        }
                        if (!addClusterNode) {
                            ConsoleController.jspLiveLog(out, " - inserting application into admindb...");
                            adb.executePreparedUpdate("INSERT INTO application ( applicationid, typeflag, applicationdesc, source, managedear ) VALUES ( ?, ?, ?, ?, ? )", new Object[]{applicationid, params.getProperty("applicationtype"), params.getProperty("applicationdesc"), params.getProperty("source"), params.getProperty("managedear")});
                            File consoleear = new File(appHome, "ear/labvantageconsole" + Build.getVersionSuffix() + ".ear");
                            ConsoleController.jspLiveLog(out, " - copying console EAR to " + consoleear.getAbsolutePath() + "...");
                            FileUtil.copyFile(ConsoleController.getConsoleEAR(configuration.getSapphireHome()), consoleear);
                            if (configuration instanceof JBoss && params.getProperty("deployear").equals("Y")) {
                                ConsoleController.jspLiveLog(out, " - deploying/copying console EAR to JBoss...");
                                ((JBoss)((Object)configuration)).deployEAR(appHome, "P", consoleear, true, progress);
                            }
                            ConsoleController.jspLiveLog(out, "<br/><h1 style=\"color:Blue\">Application install complete</h1>");
                            ConsoleController.jspLiveLog(out, "Application Home - " + appHome.getAbsolutePath() + "<br/>");
                            if (earMsg.length() > 0) {
                                ConsoleController.jspLiveLog(out, earMsg + "<br/>");
                            }
                        }
                    } else {
                        ConsoleController.jspLiveLog(out, "<br/><b><font color=\"Red\">Application cannot be installed - the EAR specified is not a file or directory!</font></b>");
                    }
                    break block35;
                }
                ConsoleController.jspLiveLog(out, "<br/><b><font color=\"Red\">Application cannot be installed - application '" + applicationid + "' already exists!</font></b>");
            }
            catch (Exception e) {
                try {
                    ConsoleController.jspLiveLog(out, "<br/><b><font color=\"Red\">Application install failed. Reason: " + e.getMessage() + "</font></b>");
                    ConsoleController.jspLiveLog(out, "<br/><a href=\"#\" onclick=\"document.getElementById( 'stacktrace' ).style.display = 'block'\">Show full stack trace</a>", true);
                    ConsoleController.jspLiveLog(out, "<div id=\"stacktrace\" style=\"display:none\">", true);
                    ConsoleController.jspLiveLog(out, StringUtil.replaceAll(LogUtil.getStackTrace(e), "\n", "<br/>"));
                    ConsoleController.jspLiveLog(out, "</div>", true);
                }
                catch (Throwable throwable) {
                    ConsoleController.jspLiveLog(out, "<br/><h1><a href=\"javascript:parent.executeOperation( 'loadapplications' )\">Return to application list</a></h1><br/>", true);
                    ConsoleController.jspLiveLog(out, "<br id=\"complete\"/><script>document.getElementById( \"complete\" ).scrollIntoView( true )</script>", true);
                    ConsoleController.jspLiveLog(out, "<script>parent.jspComplete( 'installapp" + (restartRequired ? "restart" : "") + "' );</script>", true);
                    if (!useIncomingAdm) {
                        adb.reset();
                        adb.releaseConnection();
                    }
                    throw throwable;
                }
                ConsoleController.jspLiveLog(out, "<br/><h1><a href=\"javascript:parent.executeOperation( 'loadapplications' )\">Return to application list</a></h1><br/>", true);
                ConsoleController.jspLiveLog(out, "<br id=\"complete\"/><script>document.getElementById( \"complete\" ).scrollIntoView( true )</script>", true);
                ConsoleController.jspLiveLog(out, "<script>parent.jspComplete( 'installapp" + (restartRequired ? "restart" : "") + "' );</script>", true);
                if (!useIncomingAdm) {
                    adb.reset();
                    adb.releaseConnection();
                }
            }
        }
        ConsoleController.jspLiveLog(out, "<br/><h1><a href=\"javascript:parent.executeOperation( 'loadapplications' )\">Return to application list</a></h1><br/>", true);
        ConsoleController.jspLiveLog(out, "<br id=\"complete\"/><script>document.getElementById( \"complete\" ).scrollIntoView( true )</script>", true);
        ConsoleController.jspLiveLog(out, "<script>parent.jspComplete( 'installapp" + (restartRequired ? "restart" : "") + "' );</script>", true);
        if (!useIncomingAdm) {
            adb.reset();
            adb.releaseConnection();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void deleteApp(ServletContext servletContext, JspWriter out, PropertyList params) throws IOException {
        boolean restartRequired = false;
        String applicationid = params.getProperty("applicationid");
        String sapphireHome = params.getProperty("sapphire.home");
        File appHome = ConsoleController.getAppHome(sapphireHome, applicationid);
        ConsoleController.jspLiveLog(out, "<h1>Deleting application '" + applicationid + "'</h1>");
        ConsoleController.jspLiveLog(out, PRODUCT_HOME + ": " + sapphireHome);
        ConsoleController.jspLiveLog(out, "Application HOME: " + appHome.getAbsolutePath());
        DBUtil adb = new DBUtil();
        try {
            Configuration configuration = ConsoleController.getConfiguration(servletContext);
            File configFile = ConsoleController.getConfigFile(configuration);
            adb.setConnection(ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindbms"), ConsoleController.getDataSource(configuration, ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindb")).getConnection());
            ConsoleFileOperationProgress progress = new ConsoleFileOperationProgress(out);
            adb.executePreparedUpdate("DELETE FROM application WHERE applicationid = ?", new Object[]{applicationid});
            if (params.getProperty("deleteapphome", "Y").equals("Y")) {
                ConsoleController.jspLiveLog(out, " - deleting application HOME directories...");
                progress.startProgress(" - determining files to delete...", appHome);
                FileUtil.deleteAll(appHome, progress, true);
                progress.endProgress();
            }
            if (params.getProperty("deletejbossdir", "Y").equals("Y")) {
                restartRequired = true;
                ((JBoss)((Object)configuration)).deleteApplicationDir(params.getProperty("managedear"), progress);
            }
            out.println("<br/><h1 style=\"color:Blue\">Application delete complete</h1><br/>");
        }
        catch (Exception e) {
            try {
                out.println("<br/><b><font color=\"Red\">Application delete failed. Reason: " + e.getMessage() + "</font></b>");
                out.println("<br/><a href=\"#\" onclick=\"document.getElementById( 'stacktrace' ).style.display = 'block'\">Show full stack trace</a>");
                out.println("<div id=\"stacktrace\" style=\"display:none\">");
                out.println(LogUtil.getStackTraceMessages(e, "<br/>", true, false));
                out.println("</div>");
            }
            catch (Throwable throwable) {
                out.println("<br/><h1><a href=\"javascript:parent.executeOperation( 'loadapplications' )\">Return to application list</a></h1><br/>");
                out.println("<br id=\"complete\"/><script>document.getElementById( \"complete\" ).scrollIntoView( true )</script>");
                out.println("<script>parent.jspComplete( 'deleteapp" + (restartRequired ? "restart" : "") + "' );</script>");
                adb.reset();
                adb.releaseConnection();
                throw throwable;
            }
            out.println("<br/><h1><a href=\"javascript:parent.executeOperation( 'loadapplications' )\">Return to application list</a></h1><br/>");
            out.println("<br id=\"complete\"/><script>document.getElementById( \"complete\" ).scrollIntoView( true )</script>");
            out.println("<script>parent.jspComplete( 'deleteapp" + (restartRequired ? "restart" : "") + "' );</script>");
            adb.reset();
            adb.releaseConnection();
        }
        out.println("<br/><h1><a href=\"javascript:parent.executeOperation( 'loadapplications' )\">Return to application list</a></h1><br/>");
        out.println("<br id=\"complete\"/><script>document.getElementById( \"complete\" ).scrollIntoView( true )</script>");
        out.println("<script>parent.jspComplete( 'deleteapp" + (restartRequired ? "restart" : "") + "' );</script>");
        adb.reset();
        adb.releaseConnection();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void deployApp(ServletContext servletContext, JspWriter out, PropertyList params) throws IOException {
        String applicationid = params.getProperty("applicationid");
        String type = params.getProperty("applicationtype");
        String sapphireHome = params.getProperty("sapphire.home");
        boolean restartRequired = type.equals("J");
        boolean deployed = params.getProperty("deployed", "N").equals("Y");
        File appHome = ConsoleController.getAppHome(sapphireHome, applicationid);
        ConsoleController.jspLiveLog(out, "<h1>" + (deployed ? "Undeploying" : "Deploying") + " application '" + applicationid + "'</h1>");
        ConsoleController.jspLiveLog(out, PRODUCT_HOME + ": " + sapphireHome);
        ConsoleController.jspLiveLog(out, "Application HOME: " + appHome.getAbsolutePath());
        try {
            ConsoleFileOperationProgress progress = new ConsoleFileOperationProgress(out);
            Configuration configuration = ConsoleController.getConfiguration(servletContext);
            File earDir = type.equals("P") ? new File(appHome, "/ear") : new File(appHome, "/earx");
            File managedEAR = new File(params.getProperty("managedear"));
            if (deployed) {
                ((JBoss)((Object)configuration)).undeployEAR(appHome, type, managedEAR, progress);
            } else {
                ConsoleController.jspLiveLog(out, "<b>Copying application from " + earDir.getAbsolutePath() + " to JBoss deploy directory</b>");
                ((JBoss)((Object)configuration)).deployEAR(appHome, type, managedEAR, false, progress);
            }
            out.println("<br/><h1 style=\"color:Blue\">Application " + (deployed ? "undeploy" : "deploy") + " complete</h1><br/>");
        }
        catch (Exception e) {
            try {
                out.println("<br/><b><font color=\"Red\">Application " + (deployed ? "undeploy" : "deploy") + " failed. Reason: " + e.getMessage() + "</font></b>");
                out.println("<br/><a href=\"#\" onclick=\"document.getElementById( 'stacktrace' ).style.display = 'block'\">Show full stack trace</a>");
                out.println("<div id=\"stacktrace\" style=\"display:none\">");
                out.println(LogUtil.getStackTraceMessages(e, "<br/>", true, false));
                out.println("</div>");
            }
            catch (Throwable throwable) {
                out.println("<br/><h1><a href=\"javascript:parent.executeOperation( 'loadapplications' )\">Return to application list</a></h1><br/>");
                out.println("<br id=\"complete\"/><script>document.getElementById( \"complete\" ).scrollIntoView( true )</script>");
                out.println("<script>parent.jspComplete( 'deployapp" + (restartRequired ? "restart" : "") + "' );</script>");
                throw throwable;
            }
            out.println("<br/><h1><a href=\"javascript:parent.executeOperation( 'loadapplications' )\">Return to application list</a></h1><br/>");
            out.println("<br id=\"complete\"/><script>document.getElementById( \"complete\" ).scrollIntoView( true )</script>");
            out.println("<script>parent.jspComplete( 'deployapp" + (restartRequired ? "restart" : "") + "' );</script>");
        }
        out.println("<br/><h1><a href=\"javascript:parent.executeOperation( 'loadapplications' )\">Return to application list</a></h1><br/>");
        out.println("<br id=\"complete\"/><script>document.getElementById( \"complete\" ).scrollIntoView( true )</script>");
        out.println("<script>parent.jspComplete( 'deployapp" + (restartRequired ? "restart" : "") + "' );</script>");
    }

    public static void upgradeEAR(ServletContext servletContext, JspWriter out, PropertyList params) throws IOException {
        ConsoleController.upgradeEAR(ConsoleController.getConfiguration(servletContext), out, params);
    }

    private static void upgradeDynamicClassLibraries(JspWriter out, File appHome, Configuration configuration) throws IOException {
        File dncl = new File(appHome, "dynamicclasslibraries");
        if (!dncl.exists()) {
            ConsoleController.jspLiveLog(out, " - copying dynamicclasslibraries to " + dncl.getAbsolutePath() + "...");
            FileUtil.copyAll(new File(configuration.getSapphireHome(), "console/install/dynamicclasslibraries"), dncl);
        } else {
            File[] children;
            ConsoleController.jspLiveLog(out, " - upgrading dynamicclasslibraries to " + dncl.getAbsolutePath() + "...");
            File sourceDncl = new File(configuration.getSapphireHome(), "console/install/dynamicclasslibraries");
            File sourceProd = new File(sourceDncl, "product");
            if (sourceProd.exists() && sourceProd.isDirectory()) {
                File destProd = new File(dncl, "product");
                ConsoleController.jspLiveLog(out, " - upgrading " + sourceProd.getAbsolutePath() + " to " + destProd.getAbsolutePath() + "...");
                FileUtil.copyAll(sourceProd, destProd);
            }
            for (File child : children = sourceDncl.listFiles()) {
                if (!child.exists() || !child.isDirectory() || child.getName().equalsIgnoreCase("custom") || child.getName().equalsIgnoreCase("product")) continue;
                File dest = new File(dncl, child.getName());
                if (dest.exists()) {
                    File childProduct = new File(child, "product");
                    if (!childProduct.exists() || !childProduct.isDirectory()) continue;
                    File destProduct = new File(dest, "product");
                    ConsoleController.jspLiveLog(out, " - upgrading " + childProduct.getAbsolutePath() + " to " + destProduct.getAbsolutePath() + "...");
                    FileUtil.copyAll(childProduct, destProduct);
                    continue;
                }
                ConsoleController.jspLiveLog(out, " - upgrading " + child.getAbsolutePath() + " to " + dest.getAbsolutePath() + "...");
                FileUtil.copyAll(child, dest);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void upgradeEAR(Configuration configuration, JspWriter out, PropertyList params) throws IOException {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy hh:mmaa");
        String buildDate = sdf.format(cal.getTime());
        ConsoleController.jspLiveLog(out, "<b>Upgrading application '" + params.getProperty("applicationid") + "'</b>");
        try {
            File[] appEARFiles;
            int i;
            String applicationid = params.getProperty("applicationid");
            String sapphireHome = params.getProperty("sapphire.home");
            File appHome = ConsoleController.getAppHome(sapphireHome, applicationid);
            File temp = new File(appHome, "temp");
            temp.mkdirs();
            ConsoleFileOperationProgress progress = new ConsoleFileOperationProgress(out);
            File labVantageEAR = ConsoleController.getLabVantageEAR(sapphireHome);
            ConsoleController.upgradeDynamicClassLibraries(out, appHome, configuration);
            ConsoleController.jspLiveLog(out, "- backing up existing EAR...");
            File appEAR = new File(appHome, "ear/" + params.getProperty("managedear"));
            ConsoleController.backupFile(appEAR, "upgrade");
            ConsoleController.jspLiveLog(out, "<b>Extracting latest LabVantage application files</b>");
            ConsoleController.jspLiveLog(out, "- EAR files (sapphire.jar, labvantage.war, 3rd party jars and application deployment descriptors)...");
            progress.startProgress();
            File sapphireEARTemp = new File(temp, "labvantage.ear_temp");
            int sapphireEARCount = ZipFileUtil.extractAll(labVantageEAR, sapphireEARTemp, progress);
            progress.endProgress();
            ConsoleController.jspLiveLog(out, "- code files (java class files and EJB deployment descriptors)...");
            progress.startProgress();
            File sapphireJAR = new File(sapphireEARTemp, STANDARD_JAR);
            File sapphireJARTemp = new File(sapphireEARTemp, "sapphire.jar_temp");
            int sapphireJARCount = ZipFileUtil.extractAll(sapphireJAR, sapphireJARTemp, progress);
            FileUtil.deleteAll(sapphireJAR);
            progress.endProgress();
            ConsoleController.jspLiveLog(out, "- web files (home, WEB-CORE and logon files)...");
            progress.startProgress();
            File sapphireWAR = new File(sapphireEARTemp, STANDARD_WAR);
            File sapphireWARTemp = new File(sapphireEARTemp, "labvantage.war_temp");
            int sapphireWARCount = ZipFileUtil.extractAll(sapphireWAR, sapphireWARTemp, progress);
            FileUtil.deleteAll(sapphireWAR);
            progress.endProgress();
            ConsoleController.jspLiveLog(out, "<b>Preparing '" + applicationid + "' application files</b>");
            ConsoleController.jspLiveLog(out, "- extracting EAR files...");
            progress.startProgress();
            File appEARTemp = new File(temp, "application.ear_temp");
            ZipFileUtil.extractAll(appEAR, appEARTemp, progress);
            PropertyList applicationBuildProps = new PropertyList();
            applicationBuildProps.putAll(ConsoleController.loadPropertiesFile(new File(appEARTemp, "META-INF/application-build.props")));
            applicationBuildProps.remove("application.apqjndiname");
            applicationBuildProps.remove("application.tdqjndiname");
            applicationBuildProps.remove("application.wpqjndiname");
            applicationBuildProps.remove("sapphire.build");
            applicationBuildProps.remove("sapphire.builddate");
            applicationBuildProps.remove("sapphire.version");
            applicationBuildProps.remove("sapphire.patch");
            progress.endProgress();
            boolean appLibDirExists = ConsoleController.checkAppLibExists(appEARTemp);
            int appWARCount = 0;
            String applicationWAR = applicationBuildProps.getProperty("application.war", STANDARD_WAR);
            File appWAR = new File(appEARTemp, applicationWAR);
            File appWARTemp = new File(appEARTemp, applicationWAR + "_temp");
            ConsoleController.jspLiveLog(out, "- extracting web files...");
            progress.startProgress();
            ZipFileUtil.extractAll(appWAR, appWARTemp, progress);
            FileUtil.deleteAll(appWAR);
            progress.endProgress();
            if (applicationBuildProps.getProperty("application.warfileincludes").length() > 0) {
                applicationBuildProps.setProperty("application.warfileincludes", StringUtil.replaceAll(applicationBuildProps.getProperty("application.warfileincludes"), "/**/*", ""));
                ConsoleController.jspLiveLog(out, "- copying web files...");
                String[] includes = StringUtil.split(applicationBuildProps.getProperty("application.warfileincludes"), " ");
                for (i = 0; i < includes.length; ++i) {
                    if (includes[i].length() <= 0) continue;
                    File includeFile = new File(appWARTemp, includes[i]);
                    if (includeFile.exists()) {
                        ConsoleController.jspLiveLog(out, "- " + includes[i] + "...");
                        int fileCount = FileUtil.fileCount(includeFile);
                        progress.startProgress(fileCount);
                        FileUtil.copyAll(includeFile, new File(sapphireWARTemp, includes[i]));
                        progress.endProgress();
                        appWARCount += fileCount;
                        continue;
                    }
                    ConsoleController.jspLiveLog(out, "- WARNING: " + includes[i] + " defined in application.warfileincludes in file META-INF/application-build.props not found - ignoring...");
                }
            } else {
                File[] warFiles = appWARTemp.listFiles();
                for (i = 0; i < warFiles.length; ++i) {
                    String name;
                    if (!warFiles[i].isDirectory() || (name = warFiles[i].getName()).equals("home") || name.equals(STANDARD_WEBDIR) || name.equals("WEB-INF") || name.equals("WEB-OPAL") || name.equals("META-INF")) continue;
                    File includeFile = new File(appWARTemp, name);
                    ConsoleController.jspLiveLog(out, "- " + name + "...");
                    int fileCount = FileUtil.fileCount(includeFile);
                    progress.startProgress(fileCount);
                    FileUtil.copyAll(includeFile, new File(sapphireWARTemp, name));
                    progress.endProgress();
                    appWARCount += fileCount;
                }
            }
            ArrayList<String> snippets = ConsoleController.getWebXMLSnippets(new File(appWARTemp, "WEB-INF/web.xml"));
            ArrayList<String> oldJars = new ArrayList<String>(Arrays.asList(Configuration.oldJars));
            String applicationJAR = applicationBuildProps.getProperty("application.jar");
            for (File appEARFile : appEARFiles = appLibDirExists ? new File(appEARTemp, "lib").listFiles() : appEARTemp.listFiles()) {
                String name = appEARFile.getName();
                if (!name.toLowerCase().endsWith(".jar") || name.equals(STANDARD_JAR) || name.equals("labvantage.jar") || new File(sapphireEARTemp, "lib/" + name).exists() || oldJars.contains(name)) continue;
                ConsoleController.jspLiveLog(out, "- copying JAR file " + name + "...");
                FileUtil.copyFile(appEARFile, new File(sapphireEARTemp, "lib/" + name));
            }
            int appEARCount = 0;
            if (applicationBuildProps.getProperty("application.includesource").equals("true")) {
                File[] sourceIncludeFiles;
                for (File sourceIncludeFile : sourceIncludeFiles = appEARTemp.listFiles()) {
                    if (!sourceIncludeFile.isDirectory() || sourceIncludeFile.getName().equals("META-INF") || sourceIncludeFile.getName().equals("APP-INF") || sourceIncludeFile.getName().equals("lib") || sourceIncludeFile.getName().endsWith("ar_temp")) continue;
                    applicationBuildProps.setProperty("application.earfileincludes", applicationBuildProps.getProperty("application.earfileincludes") + " " + sourceIncludeFile.getName());
                }
            }
            if (applicationBuildProps.getProperty("application.earfileincludes").length() > 0) {
                applicationBuildProps.setProperty("application.earfileincludes", StringUtil.replaceAll(applicationBuildProps.getProperty("application.earfileincludes"), "/**/*.java", ""));
                ConsoleController.jspLiveLog(out, "- copying EAR files...");
                String[] includes = StringUtil.split(applicationBuildProps.getProperty("application.earfileincludes"), " ");
                for (int i2 = 0; i2 < includes.length; ++i2) {
                    if (includes[i2].length() <= 0) continue;
                    File includeFile = new File(appEARTemp, includes[i2]);
                    if (includeFile.exists()) {
                        if (includes[i2].toLowerCase().endsWith(".jar")) {
                            if (includes[i2].equals(STANDARD_JAR) || includes[i2].equals("labvantage.jar") || new File(sapphireEARTemp, "lib/" + includes[i2]).exists()) continue;
                            ConsoleController.jspLiveLog(out, "- copying JAR file " + includes[i2] + "...");
                            FileUtil.copyFile(includeFile, new File(sapphireEARTemp, "lib/" + includes[i2]));
                            continue;
                        }
                        ConsoleController.jspLiveLog(out, "- copying EAR include " + includes[i2] + "...");
                        int fileCount = FileUtil.fileCount(includeFile);
                        progress.startProgress(fileCount);
                        FileUtil.copyAll(includeFile, new File(sapphireEARTemp, includes[i2]));
                        progress.endProgress();
                        appEARCount += fileCount;
                        continue;
                    }
                    ConsoleController.jspLiveLog(out, "- WARNING: " + includes[i2] + " defined in application.earfileincludes in file META-INF/application-build.props not found - ignoring...");
                }
            }
            ConsoleController.jspLiveLog(out, "<b>Packaging new '" + applicationid + "' application files</b>");
            if (applicationWAR.equals("sapphire.war")) {
                applicationWAR = STANDARD_WAR;
            }
            ConsoleController.jspLiveLog(out, "- creating new build properties file...");
            File sapphireBuildFile = new File(sapphireEARTemp, "META-INF/application-build.props");
            Properties sapphireBuildProps = ConsoleController.loadPropertiesFile(sapphireBuildFile);
            sapphireBuildProps.setProperty("application.build", "");
            sapphireBuildProps.setProperty("application.patch", "");
            sapphireBuildProps.setProperty("application.version", "");
            sapphireBuildProps.setProperty("application.builddate", buildDate);
            sapphireBuildProps.setProperty("application.id", applicationid);
            sapphireBuildProps.setProperty("application.appserver", applicationBuildProps.getProperty("application.appserver"));
            sapphireBuildProps.setProperty("application.ear", applicationBuildProps.getProperty("application.ear"));
            sapphireBuildProps.setProperty("application.jar", applicationJAR);
            sapphireBuildProps.setProperty("application.war", applicationWAR);
            sapphireBuildProps.setProperty("application.warfileincludes", applicationBuildProps.getProperty("application.warfileincludes"));
            sapphireBuildProps.setProperty("application.earfileincludes", applicationBuildProps.getProperty("application.earfileincludes"));
            sapphireBuildProps.setProperty("application.webcontext", applicationBuildProps.getProperty("application.webcontext"));
            sapphireBuildProps.setProperty("application.ejbprefix", applicationBuildProps.getProperty("application.ejbprefix", "com/labvantage/sapphire"));
            sapphireBuildProps.setProperty("application.ejbjarxml", applicationBuildProps.getProperty("application.ejbjarxml"));
            sapphireBuildProps.setProperty("application.webxml", applicationBuildProps.getProperty("application.webxml"));
            sapphireBuildProps.setProperty("application.applicationxml", applicationBuildProps.getProperty("application.applicationxml"));
            sapphireBuildProps.setProperty("application.sapphirehome", sapphireHome);
            sapphireBuildProps.setProperty("application.buildprocess", params.getProperty("buildprocess"));
            sapphireBuildProps.setProperty("application.includesource", applicationBuildProps.getProperty("application.includesource"));
            sapphireBuildProps.setProperty("application.externaljars", applicationBuildProps.getProperty("application.externaljars"));
            TreeMap<Object, Object> sortedProps = new TreeMap<Object, Object>();
            sortedProps.putAll(sapphireBuildProps);
            FileUtil.storeProperties(sapphireBuildFile, sortedProps, "LabVantage Build Properties");
            File sapphirePatchFile = new File(sapphireEARTemp, "META-INF/patch.xml");
            if (sapphirePatchFile.exists()) {
                sapphirePatchFile.delete();
            }
            String appServer = applicationBuildProps.getProperty("application.appserver", Configuration.getPlatformName(Integer.parseInt(params.getProperty("platform"))));
            BaseConverter converter = BaseConverter.getInstance(appServer);
            GetJarListTask task = new GetJarListTask();
            task.setCustomJars(applicationJAR + " " + applicationBuildProps.getProperty("application.externaljars"));
            if (applicationBuildProps.getProperty("application.customejbjarxml").length() == 0) {
                String ddfile = appServer.equalsIgnoreCase("weblogic") ? "weblogic-ejb-jar.xml" : (appServer.equalsIgnoreCase("websphere") ? "ibm-ejb-jar-bnd.xmi" : (appServer.equalsIgnoreCase("jboss") || appServer.equalsIgnoreCase("jboss6x") || appServer.equalsIgnoreCase("jboss7x") ? "jboss.xml" : "jboss.xml"));
                ConsoleController.jspLiveLog(out, "- updating " + ddfile + " EJB JNDI names...");
                converter.convertEjbJndiName(new File(sapphireJARTemp, "META-INF/" + ddfile), null, applicationBuildProps.getProperty("application.ejbprefix", "com/labvantage/sapphire"));
            } else {
                ConsoleController.jspLiveLog(out, "- copying custom ejb-jar.xml file...");
                FileUtil.copyFile(new File(applicationBuildProps.getProperty("application.customejbjarxml")), new File(sapphireJARTemp, "META-INF"));
            }
            ConsoleController.jspLiveLog(out, "- making application JAR...");
            ConsoleController.createEARIdFile(sapphireJARTemp);
            PackagerFile newAppJAR = new PackagerFile(sapphireJAR);
            newAppJAR.addExclusion("MANIFEST.MF");
            newAppJAR.addFile(sapphireJARTemp, sapphireJARTemp);
            PropertyList jarManifestValues = ConsoleController.getManifestValues(new File(sapphireJARTemp, "META-INF/MANIFEST.MF"));
            for (String attributeid : jarManifestValues.keySet()) {
                newAppJAR.addManifestAttribute(attributeid, jarManifestValues.getProperty(attributeid));
            }
            newAppJAR.addManifestAttribute("Build-Ref", applicationBuildProps.getProperty("application.buildref"));
            if (configuration instanceof JBossConfiguration) {
                newAppJAR.addManifestAttribute("Class-Path", task.getJarList());
            }
            progress.startProgress(sapphireJARCount);
            newAppJAR.save(progress);
            progress.endProgress();
            ConsoleController.jspLiveLog(out, "- deleting temporary working files...");
            progress.startProgress(sapphireJARCount);
            FileUtil.deleteAll(sapphireJARTemp, progress, false);
            progress.endProgress();
            if (applicationBuildProps.getProperty("application.customwebxml").length() == 0) {
                ConsoleController.jspLiveLog(out, "- updating web.xml " + PRODUCT_HOME + "...");
                converter.changeSapphireHome(new File(sapphireWARTemp, "WEB-INF/web.xml"), null, sapphireHome);
                ConsoleController.jspLiveLog(out, "- updating web.xml applicationid...");
                converter.changeApplicationName(new File(sapphireWARTemp, "WEB-INF/web.xml"), null, applicationid);
                ConsoleController.addWebXMLSnippets(new File(sapphireWARTemp, "WEB-INF/web.xml"), snippets);
            } else {
                ConsoleController.jspLiveLog(out, "- copying custom web.xml file...");
                FileUtil.copyFile(new File(applicationBuildProps.getProperty("application.customwebxml")), new File(sapphireWARTemp, "WEB-INF"));
            }
            ConsoleController.jspLiveLog(out, "- making application WAR...");
            PackagerFile newAppWAR = new PackagerFile(new File(sapphireEARTemp, applicationWAR));
            newAppWAR.addExclusion("MANIFEST.MF");
            newAppWAR.addFile(sapphireWARTemp, sapphireWARTemp);
            PropertyList warManifestValues = ConsoleController.getManifestValues(new File(sapphireWARTemp, "META-INF/MANIFEST.MF"));
            for (String attributeid : warManifestValues.keySet()) {
                newAppWAR.addManifestAttribute(attributeid, warManifestValues.getProperty(attributeid));
            }
            newAppWAR.addManifestAttribute("Build-Ref", applicationBuildProps.getProperty("application.buildref"));
            if (configuration instanceof JBossConfiguration) {
                newAppWAR.addManifestAttribute("Class-Path", task.getJarList());
            }
            progress.startProgress(sapphireWARCount + appWARCount);
            newAppWAR.save(progress);
            progress.endProgress();
            ConsoleController.jspLiveLog(out, "- deleting temporary working files...");
            progress.startProgress(sapphireWARCount + appWARCount);
            FileUtil.deleteAll(sapphireWARTemp, progress, false);
            progress.endProgress();
            if (applicationBuildProps.getProperty("application.customapplicationxml").length() == 0) {
                ConsoleController.jspLiveLog(out, "- updating application.xml web context...");
                converter.changeWebContextName(new File(sapphireEARTemp, "META-INF/application.xml"), null, applicationBuildProps.getProperty("application.webcontext"), applicationWAR);
            } else {
                ConsoleController.jspLiveLog(out, "- copying custom application.xml file...");
                FileUtil.copyFile(new File(applicationBuildProps.getProperty("application.customapplicationxml")), new File(sapphireEARTemp, "META-INF"));
            }
            if (appServer.toLowerCase().startsWith("jboss")) {
                ConsoleController.jspLiveLog(out, "- updating jboss-deployment-structure.xml web application name...");
                converter.changeWARName(new File(sapphireEARTemp, "META-INF/jboss-deployment-structure.xml"), null, applicationWAR);
            }
            ConsoleController.jspLiveLog(out, "- making application EAR...");
            PackagerFile newAppEAR = new PackagerFile(appEAR);
            newAppEAR.addExclusion("MANIFEST.MF");
            newAppEAR.addFile(sapphireEARTemp, sapphireEARTemp);
            PropertyList earManifestValues = ConsoleController.getManifestValues(new File(sapphireEARTemp, "META-INF/MANIFEST.MF"));
            for (String attributeid : earManifestValues.keySet()) {
                newAppEAR.addManifestAttribute(attributeid, earManifestValues.getProperty(attributeid));
            }
            newAppEAR.addManifestAttribute("Build-Ref", applicationBuildProps.getProperty("application.buildref"));
            progress.startProgress(sapphireEARCount + appEARCount);
            newAppEAR.save(progress);
            progress.endProgress();
            ConsoleController.jspLiveLog(out, "- deleting temporary working files...");
            progress.startProgress(sapphireEARCount + appEARCount);
            FileUtil.deleteAll(temp, progress, false);
            progress.endProgress();
            if (params.getProperty("deployed", "N").equals("Y")) {
                ConsoleController.jspLiveLog(out, "<b>Deploying application to JBoss</b>");
                ((JBoss)((Object)configuration)).deployEAR(appHome, "P", new File(params.getProperty("managedear")), true, progress);
            }
            ConsoleController.jspLiveLog(out, "<br/><h1 style=\"color:Blue\">Upgrade complete</h1><br/>");
        }
        catch (Exception e) {
            ConsoleController.jspLiveLog(out, "<br/><b><font color=\"Red\">Application upgrade failed. Reason: " + e.getMessage() + "</font></b>");
            ConsoleController.jspLiveLog(out, "<br/><a href=\"#\" onclick=\"document.getElementById( 'stacktrace' ).style.display = 'block'\">Show full stack trace</a>");
            ConsoleController.jspLiveLog(out, "<div id=\"stacktrace\" style=\"display:none\">");
            ConsoleController.jspLiveLog(out, LogUtil.getStackTraceMessages(e, "<br/>", true, false));
            ConsoleController.jspLiveLog(out, "</div>");
        }
        finally {
            ConsoleController.jspLiveLog(out, "<br/><h1><a href=\"javascript:parent.executeOperation( 'loadapplications' )\">Return to application list</a></h1><br/>", true);
            ConsoleController.jspLiveLog(out, "<br id=\"complete\"/><script>document.getElementById( \"complete\" ).scrollIntoView( true )</script>", true);
            ConsoleController.jspLiveLog(out, "<script>parent.jspComplete( 'upgradeear' );</script>", true);
        }
    }

    static boolean checkAppLibExists(File appEARTemp) {
        boolean libExists = false;
        Path ear = appEARTemp.toPath();
        Path libfolder = ear.resolve("lib");
        try (Stream<Path> paths = Files.walk(libfolder, new FileVisitOption[0]);){
            libExists = Files.exists(ear, new LinkOption[0]) && Files.exists(libfolder, new LinkOption[0]) && paths.filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).filter(s -> s.getFileName().toString().startsWith("ant")).anyMatch(s -> s.getFileName().toString().endsWith(".jar"));
        }
        catch (IOException e) {
            Trace.logInfo(LOGGER, e.getMessage());
        }
        return libExists;
    }

    public static void updateApp(ServletContext servletContext, JspWriter out, PropertyList params) throws IOException {
        ConsoleController.updateApp(ConsoleController.getConfiguration(servletContext), out, params);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void updateApp(Configuration configuration, JspWriter out, PropertyList params) throws IOException {
        boolean restartRequired;
        block10: {
            restartRequired = false;
            String applicationid = params.getProperty("applicationid");
            String sapphireHome = params.getProperty("sapphire.home");
            File appHome = ConsoleController.getAppHome(sapphireHome, applicationid);
            ConsoleController.jspLiveLog(out, "<h1>Updating application '" + applicationid + "'</h1>");
            ConsoleController.jspLiveLog(out, PRODUCT_HOME + ": " + sapphireHome);
            ConsoleController.jspLiveLog(out, "Application HOME: " + appHome.getAbsolutePath());
            try {
                ConsoleFileOperationProgress progress = new ConsoleFileOperationProgress(out);
                File source = new File(params.getProperty("updateear"));
                if (source.exists() && source.isFile()) {
                    File managedear;
                    File temp = new File(appHome, "temp");
                    File applicationProps = ZipFileUtil.extractFile(source, "META-INF/application-build.props", temp);
                    Properties appProps = ConsoleController.loadPropertiesFile(applicationProps);
                    if (!appProps.getProperty("application.id").equals(applicationid)) {
                        throw new Exception("Application id (" + applicationid + ") does not match the one found in the " + source.getName() + " EAR (" + appProps.getProperty("application.id") + ")!");
                    }
                    ConsoleController.jspLiveLog(out, " - clearing temp area...");
                    if (temp.exists()) {
                        progress.startProgress(" - determining files to delete...", temp);
                        FileUtil.deleteAll(temp, progress, false);
                        progress.endProgress();
                    }
                    if ((managedear = new File(appHome, "ear/" + params.getProperty("managedear"))).exists()) {
                        ConsoleController.jspLiveLog(out, " - backing up managed EAR...");
                        ConsoleController.backupFile(managedear, "update");
                    }
                    managedear.getParentFile().mkdirs();
                    ConsoleController.jspLiveLog(out, " - copying source EAR to " + managedear.getAbsolutePath() + "...");
                    FileUtil.copyFile(source, managedear);
                    if (params.getProperty("deployed", "N").equals("Y")) {
                        ConsoleController.jspLiveLog(out, "<b>Deploying application to JBoss</b>");
                        ((JBoss)((Object)configuration)).deployEAR(appHome, "P", new File(params.getProperty("managedear")), true, progress);
                    }
                    ConsoleController.jspLiveLog(out, "<br/><h1 style=\"color:Blue\">Application update complete</h1>");
                    ConsoleController.jspLiveLog(out, "Application Home - " + appHome.getAbsolutePath() + "<br/>");
                    File consoleear = new File(appHome, "ear/labvantageconsole" + Build.getVersionSuffix() + ".ear");
                    ConsoleController.backupFile(consoleear, "upgrade");
                    ConsoleController.jspLiveLog(out, " - copying console EAR to " + consoleear.getAbsolutePath() + "...");
                    FileUtil.copyFile(ConsoleController.getConsoleEAR(configuration.getSapphireHome()), consoleear);
                    if (configuration instanceof JBoss && params.getProperty("deployear").equals("Y")) {
                        ConsoleController.jspLiveLog(out, "<b>Deploying console EAR to JBoss</b>");
                        ((JBoss)((Object)configuration)).deployEAR(appHome, "P", consoleear, true, progress);
                    }
                    break block10;
                }
                ConsoleController.jspLiveLog(out, "<br/><b><font color=\"Red\">Application cannot be updated - the EAR specified is not a file or directory!</font></b>");
            }
            catch (Exception e) {
                try {
                    out.println("<br/><b><font color=\"Red\">Application update failed. Reason: " + e.getMessage() + "</font></b>");
                    out.println("<br/><a href=\"#\" onclick=\"document.getElementById( 'stacktrace' ).style.display = 'block'\">Show full stack trace</a>");
                    out.println("<div id=\"stacktrace\" style=\"display:none\">");
                    out.println(StringUtil.replaceAll(LogUtil.getStackTrace(e), "\n", "<br/>"));
                    out.println("</div>");
                }
                catch (Throwable throwable) {
                    out.println("<br/><h1><a href=\"javascript:parent.executeOperation( 'loadapplications' )\">Return to application list</a></h1><br/>");
                    out.println("<br id=\"complete\"/><script>document.getElementById( \"complete\" ).scrollIntoView( true )</script>");
                    out.println("<script>parent.jspComplete( 'updateapp" + (restartRequired ? "restart" : "") + "' );</script>");
                    throw throwable;
                }
                out.println("<br/><h1><a href=\"javascript:parent.executeOperation( 'loadapplications' )\">Return to application list</a></h1><br/>");
                out.println("<br id=\"complete\"/><script>document.getElementById( \"complete\" ).scrollIntoView( true )</script>");
                out.println("<script>parent.jspComplete( 'updateapp" + (restartRequired ? "restart" : "") + "' );</script>");
            }
        }
        out.println("<br/><h1><a href=\"javascript:parent.executeOperation( 'loadapplications' )\">Return to application list</a></h1><br/>");
        out.println("<br id=\"complete\"/><script>document.getElementById( \"complete\" ).scrollIntoView( true )</script>");
        out.println("<script>parent.jspComplete( 'updateapp" + (restartRequired ? "restart" : "") + "' );</script>");
    }

    public static void upgradeExplodedEAR(ServletContext servletContext, JspWriter out, PropertyList params) throws IOException {
        ConsoleController.upgradeExplodedEAR(ConsoleController.getConfiguration(servletContext), out, params);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void upgradeExplodedEAR(Configuration configuration, JspWriter out, PropertyList params) throws IOException {
        String applicationid = params.getProperty("applicationid");
        String sapphireHome = params.getProperty("sapphire.home");
        File appHome = ConsoleController.getAppHome(sapphireHome, applicationid);
        File temp = new File(appHome, "temp");
        temp.mkdirs();
        String managedEAR = params.getProperty("managedear");
        boolean deployed = params.getProperty("deployed").equals("Y");
        File labVantageEAR = ConsoleController.getLabVantageEAR(params.getProperty("sapphire.home"));
        boolean appDDFiles = params.getProperty("appddfiles").equals("Y");
        boolean ejbDDFiles = params.getProperty("ejbddfiles").equals("Y");
        boolean webDDFiles = params.getProperty("webddfiles").equals("Y");
        boolean sapphireJARFiles = params.getProperty("sapphirejarfiles").equals("Y");
        boolean sapphireWARFiles = params.getProperty("sapphirewarfiles").equals("Y");
        boolean otherJARFiles = params.getProperty("otherjarfiles").equals("Y");
        ConsoleController.jspLiveLog(out, "<b>Upgrading application '" + applicationid + "'</b>");
        ConsoleController.jspLiveLog(out, PRODUCT_HOME + ": " + sapphireHome);
        ConsoleController.jspLiveLog(out, "Application HOME: " + appHome.getAbsolutePath());
        ConsoleController.jspLiveLog(out, "Managed EAR: " + managedEAR);
        ConsoleController.jspLiveLog(out, "Working directory: " + temp.getAbsolutePath());
        try {
            File patchFile;
            ConsoleController.upgradeDynamicClassLibraries(out, appHome, configuration);
            File earDir = deployed ? new File(((JBoss)((Object)configuration)).getDeploymentDir(), managedEAR) : new File(appHome, "/earx/" + managedEAR);
            File appPropsFile = new File(earDir, "META-INF/application-build.props");
            Properties appProps = ConsoleController.loadPropertiesFile(appPropsFile);
            appProps.remove("application.apqjndiname");
            appProps.remove("application.tdqjndiname");
            appProps.remove("application.wpqjndiname");
            appProps.remove("sapphire.build");
            appProps.remove("sapphire.builddate");
            appProps.remove("sapphire.version");
            appProps.remove("sapphire.patch");
            String webcontext = appProps.getProperty("application.webcontext", "labvantage");
            String applicationjar = appProps.getProperty("application.jar", STANDARD_JAR);
            String externaljarlist = appProps.getProperty("application.externaljars");
            String applicationwar = appProps.getProperty("application.war", STANDARD_WAR);
            String ejbprefix = appProps.getProperty("ejbprefix", "com/labvantage/sapphire");
            BaseConverter converter = BaseConverter.getInstance("JBoss");
            GetJarListTask task = new GetJarListTask();
            task.setCustomJars(applicationjar.length() > 0 ? applicationjar + ";" + externaljarlist : externaljarlist);
            ConsoleFileOperationProgress progress = new ConsoleFileOperationProgress(out);
            ConsoleController.jspLiveLog(out, "<b>Clearing temp area " + temp.getAbsolutePath() + "...</b>");
            progress.startProgress(" - determining files to delete", temp);
            FileUtil.deleteAll(temp, progress, false);
            progress.endProgress();
            temp.mkdirs();
            ConsoleController.jspLiveLog(out, "<b>Extracting latest LabVantage files to " + temp.getAbsolutePath() + "...</b>");
            progress.startProgress();
            ZipFileUtil.extractAll(labVantageEAR, temp, progress);
            progress.endProgress();
            File applicationWAR = new File(earDir, applicationwar);
            if (applicationwar.equals("sapphire.war")) {
                ConsoleController.jspLiveLog(out, " - renaming '" + applicationwar + "' to '" + STANDARD_WAR + "'...");
                if (!applicationWAR.renameTo(new File(earDir, STANDARD_WAR))) {
                    throw new SapphireException("Failed to rename WAR from '" + applicationwar + "' to '" + STANDARD_WAR + "'!");
                }
                applicationwar = STANDARD_WAR;
                applicationWAR = new File(earDir, applicationwar);
                appProps.setProperty("application.war", applicationwar);
            }
            if ((patchFile = new File(earDir, "META-INF/patch.xml")).exists()) {
                ConsoleController.jspLiveLog(out, " - deleting existing patch XML file from " + new File(earDir, "META-INF").getAbsolutePath() + "...");
                patchFile.delete();
            }
            if (appDDFiles) {
                ConsoleController.jspLiveLog(out, " - copying latest LabVantage deployment descriptor files to " + new File(earDir, "META-INF").getAbsolutePath() + "...");
                FileUtil.copyAll(new File(temp, "META-INF"), new File(earDir, "META-INF"));
                ConsoleController.jspLiveLog(out, "- updating application.xml web context...");
                converter.changeWebContextName(new File(earDir, "META-INF/application.xml"), null, webcontext, applicationwar);
                converter.changeWARName(new File(earDir, "META-INF/jboss-deployment-structure.xml"), null, applicationwar);
            }
            ConsoleController.jspLiveLog(out, " - copying latest LabVantage properties to " + new File(earDir, "META-INF").getAbsolutePath() + "...");
            Properties upgradeProps = ConsoleController.loadPropertiesFile(new File(temp, "META-INF/application-build.props"));
            upgradeProps.putAll((Map<?, ?>)appProps);
            ConsoleController.savePropertiesFile(upgradeProps, appPropsFile, "LabVantage Config Properties");
            if (otherJARFiles) {
                File[] files;
                ConsoleController.jspLiveLog(out, " - copying latest LabVantage JAR files to " + earDir.getAbsolutePath() + "...");
                File newLibFiles = new File(temp, "lib");
                File earLibDir = new File(earDir, "lib");
                if (!earLibDir.exists()) {
                    earLibDir.mkdirs();
                    for (File file : files = earDir.listFiles()) {
                        String name = file.getName();
                        if (!name.toLowerCase().endsWith(".jar") || name.equals(STANDARD_JAR) || name.equals("labvantage.jar")) continue;
                        FileUtil.copyFile(file, new File(earLibDir, name));
                        file.delete();
                        file.deleteOnExit();
                    }
                }
                for (File file : files = newLibFiles.listFiles()) {
                    if (!file.isFile() || !file.getName().toLowerCase().endsWith(".jar")) continue;
                    FileUtil.copyFile(file, new File(earLibDir, file.getName()));
                }
            }
            if (ejbDDFiles || sapphireJARFiles) {
                ConsoleController.jspLiveLog(out, "<b>Extracting latest LabVantage code files</b>");
                ConsoleController.jspLiveLog(out, " - renaming JAR files...");
                File sapphireJAR = new File(temp, STANDARD_JAR);
                File sapphireJARTemp = new File(temp, "sapphire.jar_temp");
                progress.startProgress();
                FileUtil.renameTo(sapphireJAR, sapphireJARTemp, progress);
                progress.endProgress();
                ConsoleController.jspLiveLog(out, " - extracting latest files to " + sapphireJAR.getAbsolutePath() + "...</b>");
                progress.startProgress();
                ZipFileUtil.extractAll(sapphireJARTemp, sapphireJAR, progress);
                sapphireJARTemp.delete();
                progress.endProgress();
                if (ejbDDFiles) {
                    ConsoleController.jspLiveLog(out, " - copying latest EJB deployment descriptor files to " + new File(earDir, "sapphire.jar/META-INF").getAbsolutePath() + "...");
                    FileUtil.copyAll(new File(sapphireJAR, "META-INF"), new File(earDir, "sapphire.jar/META-INF"));
                    String ddfile = "jboss.xml";
                    ConsoleController.jspLiveLog(out, "- updating " + ddfile + " EJB JNDI names...");
                    converter.convertEjbJndiName(new File(earDir, "sapphire.jar/META-INF/" + ddfile), null, ejbprefix);
                    if (configuration instanceof JBossConfiguration) {
                        ConsoleController.setManifestValue(new File(earDir, "sapphire.jar/META-INF/MANIFEST.MF"), "Class-Path", task.getJarList());
                    }
                }
                if (sapphireJARFiles) {
                    FileUtil.copyFile(new File(sapphireJAR, "ver" + Build.getVersionIdentifier() + ".id"), new File(earDir, "sapphire.jar/ver" + Build.getVersionIdentifier() + ".id"));
                    FileUtil.copyFile(new File(sapphireJAR, "jasperreports.properties"), new File(earDir, "sapphire.jar/jasperreports.properties"));
                    FileUtil.copyFile(new File(sapphireJAR, "jasperreports_extension.properties"), new File(earDir, "sapphire.jar/jasperreports_extension.properties"));
                    FileUtil.copyFile(new File(sapphireJAR, "labvantage.build.props"), new File(earDir, "sapphire.jar/labvantage.build.props"));
                    FileUtil.copyFile(new File(sapphireJAR, "current.jars.txt"), new File(earDir, "sapphire.jar/current.jars.txt"));
                    FileUtil.copyFile(new File(sapphireJAR, "current.info.jars.txt"), new File(earDir, "sapphire.jar/current.info.jars.txt"));
                    FileUtil.copyFile(new File(sapphireJAR, "historical.jars.txt"), new File(earDir, "sapphire.jar/historical.jars.txt"));
                    FileUtil.copyFile(new File(sapphireJAR, "esapi-java-logging.properties"), new File(earDir, "sapphire.jar/esapi-java-logging.properties"));
                    ConsoleController.jspLiveLog(out, " - copying latest code files to " + new File(earDir, "sapphire.jar/com").getAbsolutePath() + "...");
                    progress.startProgress(FileUtil.fileCount(new File(sapphireJAR, "com")));
                    FileUtil.copyAll(new File(sapphireJAR, "com"), new File(earDir, "sapphire.jar/com"), progress);
                    progress.endProgress();
                    ConsoleController.jspLiveLog(out, " - copying latest code files to " + new File(earDir, "sapphire.jar/org").getAbsolutePath() + "...");
                    progress.startProgress(FileUtil.fileCount(new File(sapphireJAR, "org")));
                    FileUtil.copyAll(new File(sapphireJAR, "org"), new File(earDir, "sapphire.jar/org"), progress);
                    progress.endProgress();
                    ConsoleController.jspLiveLog(out, " - copying latest code files to " + new File(earDir, "sapphire.jar/sapphire").getAbsolutePath() + "...");
                    progress.startProgress(FileUtil.fileCount(new File(sapphireJAR, "sapphire")));
                    FileUtil.copyAll(new File(sapphireJAR, "sapphire"), new File(earDir, "sapphire.jar/sapphire"), progress);
                    progress.endProgress();
                    ConsoleController.jspLiveLog(out, " - copying latest code files to " + new File(earDir, "sapphire.jar/esapi").getAbsolutePath() + "...");
                    progress.startProgress(FileUtil.fileCount(new File(sapphireJAR, "esapi")));
                    FileUtil.copyAll(new File(sapphireJAR, "esapi"), new File(earDir, "sapphire.jar/esapi"), progress);
                    progress.endProgress();
                    ConsoleController.jspLiveLog(out, " - copying latest code files to " + new File(earDir, "sapphire.jar/net").getAbsolutePath() + "...");
                    progress.startProgress(FileUtil.fileCount(new File(sapphireJAR, "net")));
                    FileUtil.copyAll(new File(sapphireJAR, "net"), new File(earDir, "sapphire.jar/net"), progress);
                    progress.endProgress();
                    File sapphireJARSource = new File(earDir, STANDARD_JAR);
                    ConsoleController.createEARIdFile(sapphireJARSource);
                }
            }
            if (webDDFiles || sapphireWARFiles) {
                ConsoleController.jspLiveLog(out, "<b>Extracting latest LabVantage web files</b>");
                ConsoleController.jspLiveLog(out, " - renaming WAR files...");
                File sapphireWAR = new File(temp, STANDARD_WAR);
                File sapphireWARTemp = new File(temp, "labvantage.war_temp");
                progress.startProgress();
                FileUtil.renameTo(sapphireWAR, sapphireWARTemp, progress);
                progress.endProgress();
                ConsoleController.jspLiveLog(out, " - extracting latest files to " + sapphireWAR.getAbsolutePath() + "...</b>");
                progress.startProgress();
                ZipFileUtil.extractAll(sapphireWARTemp, sapphireWAR, progress);
                sapphireWARTemp.delete();
                progress.endProgress();
                if (webDDFiles) {
                    ArrayList<String> snippets = new ArrayList();
                    File webXML = new File(applicationWAR, "WEB-INF/web.xml");
                    ConsoleController.jspLiveLog(out, " - Getting original web.xml snippets from " + webXML.getAbsolutePath() + "...");
                    if (webXML.exists()) {
                        snippets = ConsoleController.getWebXMLSnippets(webXML);
                    } else {
                        ConsoleController.jspLiveLog(out, "<b><font color=\"Red\">Original web.xml not found " + webXML.getAbsolutePath() + ", ignoring.</font></b>");
                    }
                    ConsoleController.jspLiveLog(out, " - copying latest web deployment descriptor files to " + new File(applicationWAR, "WEB-INF").getAbsolutePath() + "...");
                    FileUtil.copyAll(new File(sapphireWAR, "WEB-INF"), new File(applicationWAR, "WEB-INF"));
                    ConsoleController.jspLiveLog(out, "- updating web.xml applicationid...");
                    converter.changeApplicationName(new File(applicationWAR, "WEB-INF/web.xml"), null, applicationid);
                    ConsoleController.addWebXMLSnippets(new File(applicationWAR, "WEB-INF/web.xml"), snippets);
                    ConsoleController.jspLiveLog(out, " - copying latest web deployment descriptor files to " + new File(applicationWAR, "/META-INF").getAbsolutePath() + "...");
                    FileUtil.copyAll(new File(sapphireWAR, "META-INF"), new File(applicationWAR + "/META-INF"));
                    if (configuration instanceof JBossConfiguration) {
                        ConsoleController.setManifestValue(new File(applicationWAR, "/META-INF/MANIFEST.MF"), "Class-Path", task.getJarList());
                    }
                }
                if (sapphireWARFiles) {
                    ConsoleController.jspLiveLog(out, " - copying latest web files to " + new File(applicationWAR, "/WEB_CORE").getAbsolutePath() + "...");
                    progress.startProgress(FileUtil.fileCount(new File(sapphireWAR, STANDARD_WEBDIR)));
                    FileUtil.copyAll(new File(sapphireWAR, STANDARD_WEBDIR), new File(applicationWAR, "/WEB-CORE"), progress);
                    progress.endProgress();
                    ConsoleController.jspLiveLog(out, " - copying latest web files to " + new File(applicationWAR, "/WEB_OPAL").getAbsolutePath() + "...");
                    progress.startProgress(FileUtil.fileCount(new File(sapphireWAR, "WEB-OPAL")));
                    FileUtil.copyAll(new File(sapphireWAR, "WEB-OPAL"), new File(applicationWAR, "/WEB-OPAL"), progress);
                    progress.endProgress();
                    ConsoleController.jspLiveLog(out, " - copying latest web files to " + applicationWAR.getAbsolutePath() + "...");
                    FileUtil.copyFile(new File(sapphireWAR, "blank.html"), new File(applicationWAR, "/blank.html"));
                    FileUtil.copyFile(new File(sapphireWAR, "esigsso.jsp"), new File(applicationWAR, "/esigsso.jsp"));
                    FileUtil.copyFile(new File(sapphireWAR, "index.html"), new File(applicationWAR, "/index.html"));
                    FileUtil.copyFile(new File(sapphireWAR, "logon.jsp"), new File(applicationWAR, "/logon.jsp"));
                    FileUtil.copyFile(new File(sapphireWAR, "loggedoffmessage.jsp"), new File(applicationWAR, "/loggedoffmessage.jsp"));
                    FileUtil.copyFile(new File(sapphireWAR, "ping.html"), new File(applicationWAR, "/ping.html"));
                    FileUtil.copyFile(new File(sapphireWAR, "testsso.jsp"), new File(applicationWAR, "/testsso.jsp"));
                    FileUtil.copyFile(new File(sapphireWAR, "mfaprompt.jsp"), new File(applicationWAR, "/mfaprompt.jsp"));
                    FileUtil.copyFile(new File(sapphireWAR, "Duo-Frame.css"), new File(applicationWAR, "/Duo-Frame.css"));
                    FileUtil.copyFile(new File(sapphireWAR, "Duo-Web-v2.js"), new File(applicationWAR, "/Duo-Web-v2.js"));
                    FileUtil.copyFile(new File(sapphireWAR, "statements.jsp"), new File(applicationWAR, "/statements.jsp"));
                }
            }
            File earLibDir = new File(earDir, "lib");
            ConsoleController.jspLiveLog(out, "<b>Removing remaining historical jar files</b>");
            File[] earLibFiles = earLibDir.listFiles();
            if (earLibFiles != null) {
                int count = 0;
                for (int i = 0; i < Configuration.oldJars.length; ++i) {
                    String fileName = Configuration.oldJars[i];
                    File file = new File(earLibDir, fileName);
                    if (!file.exists()) continue;
                    ConsoleController.jspLiveLog(out, "- deleting " + file.getAbsolutePath() + "...");
                    if (!file.delete()) {
                        file.deleteOnExit();
                    }
                    ++count;
                }
                if (count > 0) {
                    ConsoleController.jspLiveLog(out, "- " + count + " files removed.");
                }
            }
            File consoleear = new File(appHome, "ear/labvantageconsole" + Build.getVersionSuffix() + ".ear");
            ConsoleController.backupFile(consoleear, "upgrade");
            ConsoleController.jspLiveLog(out, " - copying console EAR to " + consoleear.getAbsolutePath() + "...");
            FileUtil.copyFile(ConsoleController.getConsoleEAR(configuration.getSapphireHome()), consoleear);
            if (configuration instanceof JBoss && params.getProperty("deployear").equals("Y")) {
                ConsoleController.jspLiveLog(out, "<b>Deploying console EAR to JBoss</b>");
                ((JBoss)((Object)configuration)).deployEAR(appHome, "P", consoleear, true, progress);
            }
            ConsoleController.jspLiveLog(out, "<br/><h1 style=\"color:Blue\">Upgrade complete</h1><br/>");
        }
        catch (Exception e) {
            ConsoleController.jspLiveLog(out, "<br/><b><font color=\"Red\">Application upgrade failed. Reason: " + e.getMessage() + "</font></b>");
            ConsoleController.jspLiveLog(out, "<br/><a href=\"#\" onclick=\"document.getElementById( 'stacktrace' ).style.display = 'block'\">Show full stack trace</a>", true);
            ConsoleController.jspLiveLog(out, "<div id=\"stacktrace\" style=\"display:none\">", true);
            ConsoleController.jspLiveLog(out, LogUtil.getStackTraceMessages(e, "<br/>", true, false));
            ConsoleController.jspLiveLog(out, "</div>", true);
        }
        finally {
            ConsoleController.jspLiveLog(out, "<br/><h1><a href=\"javascript:parent.executeOperation( 'loadapplications' )\">Return to application list</a></h1><br/>");
            ConsoleController.jspLiveLog(out, "<br id=\"complete\"/><script>document.getElementById( \"complete\" ).scrollIntoView( true )</script>");
            ConsoleController.jspLiveLog(out, "<script>parent.jspComplete( 'upgradeexplodedear' );</script>");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void buildEAR(ServletContext servletContext, JspWriter out, PropertyList requestParams) throws IOException {
        File antFile = null;
        boolean fullBuild = true;
        String operation = "loadapplications";
        String message = "Return to application list";
        try {
            PropertyList params = new PropertyList();
            params.putAll(AntUtil.resolveParams(requestParams));
            String applicationid = params.getProperty("application.id");
            fullBuild = params.getProperty("buildmode", "package").equals("package");
            operation = params.getProperty("returntooperation");
            message = params.getProperty("returntomessage");
            File labvantageHome = new File(params.getProperty("labvantage.home"));
            if (fullBuild) {
                File appBuildPropsFile = new File(params.getProperty("application.buildprops"));
                ConsoleController.jspLiveLog(out, "<b>Building application '" + applicationid + "' using properties file '" + appBuildPropsFile.getAbsolutePath() + "'</b>");
                PropertyList appBuildProps = new PropertyList();
                appBuildProps.putAll(ConsoleController.loadPropertiesFile(appBuildPropsFile));
                appBuildProps.putAll(AntUtil.resolveParams(appBuildProps));
                if (appBuildProps.getProperty("application.builddir", "").length() == 0) {
                    throw new SapphireException("Application build directory not specified!");
                }
                File buildDir = new File(appBuildProps.getProperty("application.builddir"));
                buildDir.mkdirs();
                antFile = new File(labvantageHome, "/console/procedures/buildear.xml");
                HashMap<String, String> antParams = new HashMap<String, String>();
                antParams.put("labvantage.home", params.getProperty("labvantage.home"));
                antParams.put(STANDARD_EAR, params.getProperty(STANDARD_EAR));
                antParams.put("application.buildprops", params.getProperty("application.buildprops"));
                antParams.put("confirmproperties", "false");
                antParams.put("earid", String.valueOf(System.currentTimeMillis()));
                ConsoleController.jspLiveLog(out, "<br/><b>Running ANT file '" + antFile.getAbsolutePath() + "'</b>");
                AntUtil.runFile(Integer.parseInt(params.getProperty("platform")), antFile, new File(buildDir, "buildear.log"), "buildear", antParams, new AntInstallListener(out, params.getProperty("verbose", "Y").equals("Y")));
                out.println("<br/><h1 style=\"color:Blue\">Build complete.</h1>");
                out.println("EAR - " + new File(buildDir, params.getProperty("application.ear")).getAbsolutePath() + "<br/>");
                out.println("Log - " + new File(buildDir, "buildear.log").getAbsolutePath());
            } else {
                Configuration configuration = ConsoleController.getConfiguration(servletContext);
                File appHome = ConsoleController.getAppHome(params.getProperty("labvantage.home"), applicationid);
                File earSourceDir = params.getProperty("buildmode").equals("unexplode") ? new File(appHome, "/earx/" + params.getProperty("managedear")) : new File(((JBoss)((Object)configuration)).getDeploymentDir(), params.getProperty("managedear"));
                File earTargetDir = new File(appHome, "ear");
                File newAppEAR = ConsoleController.unexplodeEAR(configuration, earSourceDir, earTargetDir, params.getProperty("managedear"), params.getProperty("application.buildref"), out);
                out.println("<br/><h1 style=\"color:Blue\">Build complete.</h1>");
                out.println("EAR - " + newAppEAR.getAbsolutePath() + "<br/>");
            }
        }
        catch (Throwable t) {
            out.println("<br/><font color=\"Red\">Error building EAR. Reason: " + t.getMessage() + "</font>");
            out.println("<br/><a href=\"#\" onclick=\"document.getElementById( 'stacktrace' ).style.display = 'block'\">Show full stack trace</a>");
            out.println("<div id=\"stacktrace\" style=\"display:none\">");
            out.println(LogUtil.getStackTraceMessages(t, "<br/>", true, false));
            out.println("</div>");
        }
        finally {
            out.println("<br/><h1><a href=\"javascript:parent.executeOperation( '" + operation + "' )\">" + message + "</a></h1><br/>");
            out.println("<br id=\"complete\"/><script>document.getElementById( \"complete\" ).scrollIntoView( true )</script>");
            out.println("<script>parent.jspComplete( 'buildear' );</script>");
        }
    }

    public static void applyPatch(ServletContext servletContext, JspWriter out, PropertyList params) throws IOException {
        try {
            ConsoleController.applyPatch(ConsoleController.getConfiguration(servletContext), out, params, null, null);
        }
        catch (Exception e) {
            ConsoleController.jspLiveLog(out, "<br/><b><font color=\"Red\">Apply installer failed. Reason: " + e.getMessage() + "</font></b>");
            ConsoleController.jspLiveLog(out, "<br/><a href=\"#\" onclick=\"document.getElementById( 'stacktrace' ).style.display = 'block'\">Show full stack trace</a>", true);
            ConsoleController.jspLiveLog(out, "<div id=\"stacktrace\" style=\"display:none\">", true);
            ConsoleController.jspLiveLog(out, LogUtil.getStackTraceMessages(e, "<br/>", true, false));
            ConsoleController.jspLiveLog(out, "</div>", true);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void applyPatch(Configuration configuration, JspWriter out, PropertyList params, DBUtil adb, DBUtil db) throws IOException, SapphireException, SQLException {
        boolean restartRequired = false;
        String patchid = params.getProperty("patchid");
        String applicationid = params.getProperty("applicationid");
        String type = params.getProperty("type");
        String managedear = params.getProperty("managedear");
        String databaseid = params.getProperty("databaseid");
        String dbms = params.getProperty("dbms");
        String jndiname = params.getProperty("jndiname");
        String sapphireHome = params.getProperty("sapphire.home");
        String applyMode = params.getProperty("applymode", "");
        boolean skipEarPatch = applyMode.equalsIgnoreCase("dbonly") || applyMode.equalsIgnoreCase("antonly");
        boolean skipDbPatch = applyMode.equalsIgnoreCase("earonly") || applyMode.equalsIgnoreCase("antonly");
        File lvHome = new File(sapphireHome);
        File appHome = ConsoleController.getAppHome(sapphireHome, applicationid);
        File temp = new File(appHome, "temp");
        File patchZip = new File(params.getProperty("patchzipfile"));
        ArrayList<String> jars = new ArrayList<String>();
        ConsoleController.jspLiveLog(out, "<h1>Applying installer '" + patchid + "'</h1>");
        ConsoleController.jspLiveLog(out, "Installer File: " + patchZip.getAbsolutePath());
        ConsoleController.jspLiveLog(out, "Installer Zip: " + patchZip.getName() + " isfile: " + patchZip.exists());
        ConsoleController.jspLiveLog(out, PRODUCT_HOME + ": " + sapphireHome);
        if (!applicationid.isEmpty()) {
            ConsoleController.jspLiveLog(out, "Application: " + applicationid);
            ConsoleController.jspLiveLog(out, "Application HOME: " + appHome.getAbsolutePath());
        }
        if (!databaseid.isEmpty()) {
            ConsoleController.jspLiveLog(out, "Database: " + databaseid);
        }
        boolean initAdb = false;
        boolean initDb = false;
        if (adb == null) {
            adb = new DBUtil();
            initAdb = true;
        }
        if (db == null) {
            db = new DBUtil();
            initDb = true;
        }
        try {
            File consolePatchFile;
            File configFile = ConsoleController.getConfigFile(configuration);
            if (initAdb) {
                adb.setConnection(ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindbms"), ConsoleController.getDataSource(configuration, ConsoleController.getConfigProperty(configFile, "com.labvantage.sapphire.server.admindb")).getConnection());
            }
            ConsoleFileOperationProgress progress = new ConsoleFileOperationProgress(out);
            ConsoleController.jspLiveLog(out, " - clearing temp area...");
            if (temp.exists()) {
                progress.startProgress(" - determining files to delete...", temp);
                FileUtil.deleteAll(temp, progress, false);
                progress.endProgress();
            }
            temp.mkdirs();
            File tempPatch = new File(temp, "patch");
            ConsoleController.jspLiveLog(out, " - extracting installer files...");
            PatchZipContents[] hasWhat = new PatchZipContents[6];
            boolean isPatch = ConsoleController.extractInstallerFiles(tempPatch, patchZip, hasWhat, jars, progress);
            boolean hasEarPatch = PatchZipContents.EAR.equals((Object)hasWhat[1]);
            boolean hasJarPatch = PatchZipContents.JAR.equals((Object)hasWhat[2]);
            boolean hasWarPatch = PatchZipContents.WAR.equals((Object)hasWhat[3]);
            boolean hasDbPatch = PatchZipContents.DB.equals((Object)hasWhat[4]);
            boolean hasConsolePatch = PatchZipContents.CONSOLE.equals((Object)hasWhat[5]);
            File ear = null;
            File war = null;
            String appwar = null;
            int earFileCount = 0;
            ArrayList<Integer> jarFileCounts = new ArrayList<Integer>();
            HashMap<String, File> jarDir = new HashMap<String, File>();
            int warFileCount = 0;
            if (!applicationid.isEmpty()) {
                if (type.equals("P")) {
                    ear = new File(temp, "application");
                    if (!skipEarPatch && (hasEarPatch || hasJarPatch || hasWarPatch)) {
                        ConsoleController.jspLiveLog(out, "<b>Expanding application EAR to " + ear.getAbsolutePath() + "...</b>");
                        progress.startProgress();
                        earFileCount = ZipFileUtil.extractAll(new File(appHome, "ear/" + managedear), ear, progress);
                        progress.endProgress();
                        boolean standardJARPatch = false;
                        for (int i = 0; !standardJARPatch && i < jars.size(); ++i) {
                            if (!jars.get(i).equals(STANDARD_JAR)) continue;
                            standardJARPatch = true;
                            break;
                        }
                        if (!standardJARPatch) {
                            jars.add(STANDARD_JAR);
                            hasJarPatch = true;
                        }
                        if (hasJarPatch) {
                            for (String jar : jars) {
                                jarDir.put(jar, new File(ear, jar + "_patch"));
                                ConsoleController.jspLiveLog(out, " - expanding application JAR to " + jarDir.get(jar).getAbsolutePath() + "...");
                                progress.startProgress();
                                jarFileCounts.add(ZipFileUtil.extractAll(new File(ear, jar), jarDir.get(jar), progress));
                                progress.endProgress();
                            }
                        }
                        if (hasWarPatch) {
                            appwar = ConsoleController.getApplicationXMLProps(new File(ear, "META-INF/application.xml")).getProperty("application.war", STANDARD_WAR);
                            war = new File(ear, appwar + "_patch");
                            ConsoleController.jspLiveLog(out, " - expanding application WAR to " + war.getAbsolutePath() + "...");
                            progress.startProgress();
                            warFileCount = ZipFileUtil.extractAll(new File(ear, appwar), war, progress);
                            progress.endProgress();
                        }
                    }
                } else {
                    ear = new File(((JBoss)((Object)configuration)).getDeploymentDir(), managedear);
                    for (String jar : jars) {
                        jarDir.put(jar, new File(ear, jar));
                    }
                    appwar = ConsoleController.getApplicationXMLProps(new File(ear, "META-INF/application.xml")).getProperty("application.war", STANDARD_WAR);
                    war = new File(ear, appwar);
                }
            }
            Calendar now = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd-HHmmss");
            String id = (isPatch ? "LV_PATCH_" : "LV_COMPONENT_") + sdf.format(now.getTime()) + "-";
            int idCount = 0;
            if (!databaseid.isEmpty() && hasDbPatch && !skipDbPatch) {
                ConsoleController.jspLiveLog(out, "<b>Connecting to database " + databaseid + "...</b>");
                if (initDb) {
                    Connection conn = ConsoleController.getDataSource(configuration, jndiname).getConnection();
                    conn.setAutoCommit(false);
                    db.setConnection(dbms, conn);
                }
                ConsoleController.jspLiveLog(out, " - connected");
                db.createResultSet("SELECT propertyid FROM sysconfig WHERE propertyid like '" + id + "%'");
                while (db.getNext()) {
                    idCount = Math.max(idCount, Integer.parseInt(db.getString("propertyid").substring(id.length())));
                }
            }
            String jndiprefix = configuration.getJNDIDataSourcePrefix();
            ConsoleController.applyInstallerFiles(tempPatch, patchZip, ear, type, jarDir, war, lvHome, appHome, configuration.getPlatform(), jndiname, jndiprefix, db, id, new int[]{idCount}, applicationid, databaseid, out, params);
            PatchXML installXML = new PatchXML(isPatch ? new File(tempPatch, "patch.xml") : new File(tempPatch, "component.xml"));
            ArrayList<PatchXML.Patch> patches = installXML.load();
            if (!skipEarPatch && (hasEarPatch || hasJarPatch || hasWarPatch) && !applicationid.isEmpty()) {
                File earInstallerFile = new File(ear, "META-INF/" + (isPatch ? "patch.xml" : "component.xml"));
                if (earInstallerFile.exists()) {
                    PatchXML earInstallerXML = new PatchXML(earInstallerFile);
                    ArrayList<PatchXML.Patch> existingInstalls = earInstallerXML.load();
                    existingInstalls.addAll(patches);
                    earInstallerXML.save();
                } else {
                    installXML.setPatchXML(earInstallerFile);
                    installXML.save();
                }
            }
            if ((consolePatchFile = new File(sapphireHome, "console/consolepatch.xml")).exists()) {
                PatchXML consolePatchXML = new PatchXML(consolePatchFile);
                ArrayList<PatchXML.Patch> existingPatches = consolePatchXML.load();
                existingPatches.addAll(patches);
                consolePatchXML.save();
            } else {
                installXML.setPatchXML(consolePatchFile);
                installXML.save();
            }
            int allJarFileCount = 0;
            if (!applicationid.isEmpty() && type.equals("P") && !skipEarPatch && (hasEarPatch || hasJarPatch || hasWarPatch)) {
                ConsoleController.jspLiveLog(out, "<b>Rebuilding application...</b>");
                if (hasJarPatch) {
                    for (int i = 0; i < jars.size(); ++i) {
                        File jar = jarDir.get(jars.get(i));
                        if (jars.get(i).equals(STANDARD_JAR)) {
                            ConsoleController.createEARIdFile(jar);
                        }
                        ConsoleController.jspLiveLog(out, " - rebuilding JAR " + jars.get(i) + "...");
                        PackagerFile patchedJar = new PackagerFile(new File(ear, jars.get(i)));
                        patchedJar.addFile(jar, jar);
                        progress.startProgress(((Integer)jarFileCounts.get(i)).intValue());
                        patchedJar.save(progress);
                        progress.endProgress();
                        ConsoleController.jspLiveLog(out, " - clearing old JAR...");
                        progress.startProgress(((Integer)jarFileCounts.get(i)).intValue());
                        FileUtil.deleteAll(jar, progress, false);
                        progress.endProgress();
                        allJarFileCount += ((Integer)jarFileCounts.get(i)).intValue();
                    }
                }
                if (hasWarPatch) {
                    ConsoleController.jspLiveLog(out, " - rebuilding WAR " + appwar + "...");
                    PackagerFile patchedWar = new PackagerFile(new File(ear, appwar));
                    patchedWar.addFile(war, war);
                    progress.startProgress(warFileCount);
                    patchedWar.save(progress);
                    progress.endProgress();
                    ConsoleController.jspLiveLog(out, " - clearing old WAR...");
                    progress.startProgress(warFileCount);
                    FileUtil.deleteAll(war, progress, false);
                    progress.endProgress();
                }
                ConsoleController.jspLiveLog(out, " - rebuilding EAR...");
                File targetEar = new File(appHome, "ear/" + managedear);
                if (targetEar.exists()) {
                    ConsoleController.backupFile(targetEar, isPatch ? "patch" : "component");
                }
                PackagerFile patchedEar = new PackagerFile(targetEar);
                patchedEar.addFile(ear, ear);
                progress.startProgress(earFileCount);
                patchedEar.save(progress);
                progress.endProgress();
                restartRequired = true;
            }
            if (params.getProperty("deployed", "N").equals("Y") && type.equals("P") && !skipEarPatch) {
                ConsoleController.jspLiveLog(out, "<b>Deploying application to JBoss</b>");
                ((JBoss)((Object)configuration)).deployEAR(appHome, "P", new File(params.getProperty("managedear")), true, progress);
            }
            if (configuration instanceof JBoss && hasConsolePatch && !skipEarPatch) {
                ConsoleController.jspLiveLog(out, "<b>Deploying console EAR to JBoss</b>");
                if (!applicationid.isEmpty()) {
                    ((JBoss)((Object)configuration)).deployEAR(appHome, "P", new File(LOGGER + Build.getVersionSuffix() + ".ear"), true, progress);
                } else {
                    ((JBoss)((Object)configuration)).deployEAR(temp, "P", new File(LOGGER + Build.getVersionSuffix() + ".ear"), true, progress);
                }
                restartRequired = true;
            }
            ConsoleController.jspLiveLog(out, " - clearing temp area...");
            if (temp.exists()) {
                progress.startProgress(earFileCount + allJarFileCount + warFileCount);
                FileUtil.deleteAll(temp, progress, false);
                progress.endProgress();
            }
            ConsoleController.jspLiveLog(out, "<br/><h1 style=\"color:Blue\">Apply " + (isPatch ? "patch" : "component") + " complete</h1><br/>");
        }
        catch (Throwable throwable) {
            ConsoleController.jspLiveLog(out, "<br/><h1><a href=\"javascript:parent.executeOperation( 'loadapplypatch' )\">Return to apply installer utility</a></h1><br/>", true);
            ConsoleController.jspLiveLog(out, "<br id=\"complete\"/><script>document.getElementById( \"complete\" ).scrollIntoView( true )</script>", true);
            ConsoleController.jspLiveLog(out, "<script>parent.jspComplete( 'patchapp" + (restartRequired ? "restart" : "") + "' );</script>", true);
            adb.reset();
            adb.releaseConnection();
            db.reset();
            db.releaseConnection();
            throw throwable;
        }
        ConsoleController.jspLiveLog(out, "<br/><h1><a href=\"javascript:parent.executeOperation( 'loadapplypatch' )\">Return to apply installer utility</a></h1><br/>", true);
        ConsoleController.jspLiveLog(out, "<br id=\"complete\"/><script>document.getElementById( \"complete\" ).scrollIntoView( true )</script>", true);
        ConsoleController.jspLiveLog(out, "<script>parent.jspComplete( 'patchapp" + (restartRequired ? "restart" : "") + "' );</script>", true);
        adb.reset();
        adb.releaseConnection();
        db.reset();
        db.releaseConnection();
    }

    private static boolean extractInstallerFiles(File tempPatch, File patchZip, PatchZipContents[] hasWhat, ArrayList<String> jars, ConsoleFileOperationProgress progress) throws SapphireException {
        ZipFileUtil.extractAll(patchZip, tempPatch, progress);
        File xmlDef = new File(tempPatch, "patch.xml");
        boolean isPatch = xmlDef.exists();
        PatchXML installXML = new PatchXML(isPatch ? xmlDef : new File(tempPatch, "component.xml"));
        ArrayList<PatchXML.Patch> patches = installXML.load();
        for (int i = 0; i < patches.size(); ++i) {
            PatchXML.Patch patch = patches.get(i);
            ArrayList<PatchXML.PatchFile> patchFiles = patch.getPatchFiles();
            for (int j = 0; j < patchFiles.size(); ++j) {
                PatchXML.PatchFile patchFile = patchFiles.get(j);
                if (patchFile.getTarget().equals("app")) {
                    hasWhat[0] = PatchZipContents.APP;
                    ConsoleController.extractInstallerFiles(new File(tempPatch, "app/patch_" + patchFile.getFile()), new File(tempPatch, "app/" + patchFile.getFile()), hasWhat, jars, progress);
                }
                if (patchFile.getTarget().equals("ear")) {
                    hasWhat[1] = PatchZipContents.EAR;
                    if (patchFile.getFile().toLowerCase().endsWith(".jar")) {
                        hasWhat[2] = PatchZipContents.JAR;
                        if (!jars.contains(STANDARD_JAR)) {
                            jars.add(STANDARD_JAR);
                        }
                        hasWhat[3] = PatchZipContents.WAR;
                    }
                }
                if (patchFile.getTarget().equals("jar")) {
                    String jar;
                    hasWhat[2] = PatchZipContents.JAR;
                    String string = jar = patchFile.getJar().length() > 0 ? patchFile.getJar() : STANDARD_JAR;
                    if (!jars.contains(jar)) {
                        jars.add(jar);
                    }
                }
                if (patchFile.getTarget().equals("war") || patchFile.getTarget().equals("webxml")) {
                    hasWhat[3] = PatchZipContents.WAR;
                }
                if (patchFile.getTarget().equals("database")) {
                    hasWhat[4] = PatchZipContents.DB;
                }
                if (!patchFile.getTarget().equals("console")) continue;
                hasWhat[5] = PatchZipContents.CONSOLE;
            }
        }
        return isPatch;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void applyInstallerFiles(File tempPatch, File patchZip, File ear, String type, HashMap<String, File> jarDir, File war, File lvHome, File appHome, int platform, String jndiname, String jndiprefix, DBUtil db, String id, int[] idCount, String applicationid, String databaseid, JspWriter out, PropertyList params) throws SapphireException, IOException, SQLException {
        File xmlDef = new File(tempPatch, "patch.xml");
        String applyMode = params.getProperty("applymode", "");
        boolean skipEarPatch = applyMode.equalsIgnoreCase("dbonly") || applyMode.equalsIgnoreCase("antonly");
        boolean skipDbPatch = applyMode.equalsIgnoreCase("earonly") || applyMode.equalsIgnoreCase("antonly");
        boolean skipAntPatch = applyMode.equalsIgnoreCase("dbonly") || applyMode.equalsIgnoreCase("earonly");
        boolean isPatch = xmlDef.exists();
        String platformName = Configuration.getPlatformName(platform);
        ConsoleController.jspLiveLog(out, "<b>Applying " + (isPatch ? "patch" : "component") + " files...</b>");
        PatchXML installXML = new PatchXML(isPatch ? xmlDef : new File(tempPatch, "component.xml"));
        ArrayList<PatchXML.Patch> patches = installXML.load();
        for (int i = 0; i < patches.size(); ++i) {
            PatchXML.Patch patch = patches.get(i);
            ArrayList<PatchXML.PatchFile> patchFiles = patch.getPatchFiles();
            for (int j = 0; j < patchFiles.size(); ++j) {
                boolean extractOnly = false;
                boolean dbPatch = false;
                PatchXML.PatchFile patchFile = patchFiles.get(j);
                File targetDir = null;
                if (patchFile.getPlatform() == null || patchFile.getPlatform().isEmpty() || patchFile.getPlatform().equals(platformName)) {
                    if (patchFile.getDbms() == null || patchFile.getDbms().isEmpty() || patchFile.getDbms().equals("ORA") && db.isOracle() || patchFile.getDbms().equals("MSS") && db.isSqlServer()) {
                        File dbFile;
                        block68: {
                            int pos;
                            File targetFile;
                            if (patchFile.getTarget().equals("app")) {
                                ConsoleController.applyInstallerFiles(new File(tempPatch, "app/patch_" + patchFile.getFile()), new File(tempPatch, "app/" + patchFile.getFile()), ear, type, jarDir, war, lvHome, appHome, platform, jndiname, jndiprefix, db, id, idCount, applicationid, databaseid, out, params);
                            } else if (patchFile.getTarget().equals("ear")) {
                                targetDir = ear;
                            } else if (patchFile.getTarget().equalsIgnoreCase("jar")) {
                                targetDir = jarDir.get(patchFile.getJar());
                            } else if (patchFile.getTarget().equalsIgnoreCase("war") || patchFile.getTarget().equalsIgnoreCase("webxml")) {
                                targetDir = war;
                            } else if (patchFile.getTarget().equalsIgnoreCase("lvh")) {
                                targetDir = lvHome;
                            } else if (patchFile.getTarget().equalsIgnoreCase("apphome")) {
                                targetDir = appHome;
                            } else if (patchFile.getTarget().equalsIgnoreCase("console")) {
                                targetDir = appHome != null && appHome.exists() && !type.isEmpty() ? new File(appHome, "ear") : new File(tempPatch.getParentFile(), "ear");
                                if (!targetDir.exists()) {
                                    targetDir.mkdirs();
                                }
                            } else if (patchFile.getTarget().equalsIgnoreCase("database")) {
                                dbPatch = true;
                            } else if (patchFile.getTarget().equalsIgnoreCase("none") && patchFile.getAction().equals("extract")) {
                                targetDir = patchZip.getParentFile();
                                extractOnly = true;
                            }
                            if (!skipEarPatch && targetDir != null && !extractOnly) {
                                File webXML;
                                targetFile = new File(targetDir, patchFile.getFile());
                                pos = targetFile.getAbsolutePath().indexOf(patchFile.getFile(File.separatorChar));
                                if (pos == -1) {
                                    throw new SapphireException("Patch file '" + patchFile.getFile(File.separatorChar) + "' not found in target file '" + targetFile.getAbsolutePath() + "'");
                                }
                                if (patchFile.getAction().equals("backupreplace") || patchFile.getAction().equals("backupdelete")) {
                                    try {
                                        ConsoleController.jspLiveLog(out, " - backing up file " + targetFile.getAbsolutePath().substring(pos) + "...");
                                        if (targetFile.isFile()) {
                                            FileUtil.copyFile(targetFile, new File(targetDir, patchFile.getFile() + "_" + patch.getPatchid().replaceAll("\\.", "_")));
                                        } else if (!patchFile.getTarget().equalsIgnoreCase("console")) {
                                            targetFile.mkdirs();
                                            FileUtil.renameTo(targetFile, new File(targetDir, patchFile.getFile() + "_" + patch.getPatchid().replaceAll("\\.", "_")));
                                        }
                                    }
                                    catch (Exception e) {
                                        throw new SapphireException("Failed to backup target file '" + targetFile.getAbsolutePath() + "'. Reason: " + e.getMessage(), e);
                                    }
                                }
                                if (patchFile.getAction().equals("backupreplace") || patchFile.getAction().equals("backupdelete") || patchFile.getAction().equals("replace") || patchFile.getAction().equals("backupreplace") || patchFile.getAction().equals("delete")) {
                                    if (patchFile.getTarget().equals("webxml") && patchFile.getAction().equals("delete")) {
                                        try {
                                            ConsoleController.jspLiveLog(out, " - deleting web.xml entry from " + targetFile.getAbsolutePath().substring(pos) + "...");
                                            webXML = new File(war, "WEB-INF/web.xml");
                                            ConsoleController.applyWebXMLSnippet(patch.getPatchid() + " (v" + patch.getVersion() + ")", webXML, new File(tempPatch, patchFile.getTarget() + "/" + patchFile.getFile()), patchFile.getAction());
                                        }
                                        catch (Exception e) {
                                            throw new SapphireException("Failed to delete web.xml entry from target file '" + targetFile.getAbsolutePath() + "'. Reason: " + e.getMessage(), e);
                                        }
                                    }
                                    try {
                                        ConsoleController.jspLiveLog(out, " - deleting existing file " + targetFile.getAbsolutePath().substring(pos) + "...");
                                        FileUtil.deleteAll(targetFile);
                                    }
                                    catch (Exception e) {
                                        throw new SapphireException("Failed to delete target file '" + targetFile.getAbsolutePath() + "'. Reason: " + e.getMessage(), e);
                                    }
                                }
                                if (patchFile.getAction().equals("add") || patchFile.getAction().equals("replace") || patchFile.getAction().equals("backupreplace")) {
                                    if (patchFile.getTarget().equals("webxml")) {
                                        try {
                                            ConsoleController.jspLiveLog(out, " - " + (patchFile.getAction().equals("add") ? "adding" : "replacing") + " web.xml entry from " + targetFile.getAbsolutePath().substring(pos) + "...");
                                            webXML = new File(war, "WEB-INF/web.xml");
                                            ConsoleController.applyWebXMLSnippet(patch.getPatchid() + "(v" + patch.getVersion() + ")", webXML, new File(tempPatch, patchFile.getTarget() + "/" + patchFile.getFile()), patchFile.getAction());
                                        }
                                        catch (Exception e) {
                                            throw new SapphireException("Failed to " + (patchFile.getAction().equals("add") ? "add" : "replace") + " web.xml entry from target file '" + targetFile.getAbsolutePath() + "'. Reason: " + e.getMessage(), e);
                                        }
                                    }
                                    try {
                                        ConsoleController.jspLiveLog(out, " - " + (patchFile.getAction().equals("add") ? "adding" : "replacing") + " patch file " + targetFile.getAbsolutePath().substring(pos) + "...");
                                        File tempPatchFile = new File(tempPatch, patchFile.getTarget() + "/" + patchFile.getFile());
                                        if (tempPatchFile.isFile()) {
                                            FileUtil.copyFile(tempPatchFile, targetFile);
                                        } else {
                                            FileUtil.copyAll(tempPatchFile, targetFile);
                                        }
                                    }
                                    catch (Exception e) {
                                        throw new SapphireException("Failed to " + (patchFile.getAction().equals("add") ? "add" : "replace") + " target file '" + targetFile.getAbsolutePath() + "'. Reason: " + e.getMessage(), e);
                                    }
                                }
                                if (!patchFile.getFile().toLowerCase().endsWith(".jar")) continue;
                                try {
                                    File appBuildFile = new File(ear, "/META-INF/application-build.props");
                                    Properties appBuildProps = ConsoleController.loadPropertiesFile(appBuildFile);
                                    String externaljars = appBuildProps.getProperty("application.externaljars", "");
                                    if (externaljars.contains(patchFile.getFile())) continue;
                                    appBuildProps.setProperty("application.externaljars", externaljars.length() > 0 ? patchFile.getFile() + "," + externaljars : patchFile.getFile());
                                    ConsoleController.savePropertiesFile(appBuildProps, appBuildFile, "LabVantage Build Properties");
                                    continue;
                                }
                                catch (Exception e) {
                                    throw new SapphireException("Failed to update application-build.props. Reason: " + e.getMessage(), e);
                                }
                            }
                            if (!skipEarPatch && targetDir != null && extractOnly) {
                                targetFile = new File(targetDir, patchFile.getFile());
                                pos = targetFile.getAbsolutePath().indexOf(patchFile.getFile(File.separatorChar));
                                if (pos == -1) {
                                    throw new SapphireException("Patch file '" + patchFile.getFile(File.separatorChar) + "' not found in target file '" + targetFile.getAbsolutePath() + "'");
                                }
                                try {
                                    ConsoleController.jspLiveLog(out, " - extracting " + (isPatch ? "patch" : "component") + " file " + targetFile.getAbsolutePath().substring(targetFile.getAbsolutePath().indexOf(patchFile.getFile(File.separatorChar))) + "...");
                                    if (targetFile.isFile()) {
                                        FileUtil.copyFile(new File(tempPatch, "misc/" + patchFile.getFile()), targetFile);
                                        continue;
                                    }
                                    FileUtil.copyAll(new File(tempPatch, "misc/" + patchFile.getFile()), targetFile);
                                    continue;
                                }
                                catch (Exception e) {
                                    throw new SapphireException("Failed to extract file '" + targetFile.getAbsolutePath() + "'. Reason: " + e.getMessage(), e);
                                }
                            }
                            if (skipDbPatch || !dbPatch) continue;
                            dbFile = new File(tempPatch, patchFile.getTarget() + "/" + patchFile.getFile());
                            pos = dbFile.getAbsolutePath().indexOf(patchFile.getFile(File.separatorChar));
                            if (pos == -1) {
                                throw new SapphireException("Patch file '" + patchFile.getFile(File.separatorChar) + "' not found in target file '" + dbFile.getAbsolutePath() + "'");
                            }
                            if (patchFile.getAction().equals("executectt")) {
                                try {
                                    ConsoleController.jspLiveLog(out, " - executing CTT file " + dbFile.getAbsolutePath().substring(pos) + "...");
                                    ImportXML importXML = new ImportXML((DBAccess)db, dbFile);
                                    importXML.setImportLog(new ImportLogger(out));
                                    importXML.setCommitScope("import");
                                    importXML.setIgnoreSequenceCheck(true);
                                    importXML.importFiles();
                                }
                                catch (Exception e) {
                                    throw new SapphireException("Failed to execute CTT file '" + dbFile.getAbsolutePath() + "'. Reason: " + e.getMessage(), e);
                                }
                            }
                            if (!skipDbPatch && patchFile.getAction().equals("executesql")) {
                                try {
                                    ConsoleController.jspLiveLog(out, " - executing SQL file " + dbFile.getAbsolutePath().substring(pos) + "...");
                                    try (BufferedReader br = new BufferedReader(new FileReader(dbFile));){
                                        SqlProcessor.processSQL(db, new HashMap(), br, null);
                                        break block68;
                                    }
                                }
                                catch (Exception e) {
                                    throw new SapphireException("Failed to execute SQL file '" + dbFile.getAbsolutePath() + "'. Reason: " + e.getMessage(), e);
                                }
                            }
                            if (!skipAntPatch && patchFile.getAction().equals("runant")) {
                                ConsoleController.jspLiveLog(out, "<br/><p style=\"color:Blue\"> Executing ANT file '" + dbFile.getAbsolutePath().substring(pos) + "'</p>");
                                HashMap<String, String> antParams = new HashMap<String, String>();
                                antParams.put("sapphire.db.databaseid", databaseid);
                                antParams.put("sapphire.db.jndiname", jndiname);
                                antParams.put("sapphire.db.jndidatasourceprefix", jndiprefix);
                                antParams.put("sapphire.admindb.jndidatasourceprefix", jndiprefix);
                                String databaseType = db.isOracle() ? "ORA" : "MSS";
                                antParams.put("sapphire.db.dbms", databaseType);
                                antParams.put("sapphire.db.dbms.lower", databaseType.toLowerCase());
                                antParams.put("sapphire.db.hoststring", params.getProperty("sapphire.db.hoststring"));
                                antParams.put("sapphire.db.sqldatabase", params.getProperty("sapphire.db.sqldatabase"));
                                antParams.put("sapphire.db.username", params.getProperty("sapphire.db.username"));
                                antParams.put("sapphire.db.password", params.getProperty("sapphire.db.password"));
                                antParams.put("sapphire.db.instancename", params.getProperty("sapphire.db.instancename"));
                                antParams.put("sapphire.db.sid", params.getProperty("sapphire.db.sid"));
                                antParams.put("sapphire.db.port", params.getProperty("sapphire.db.port"));
                                antParams.put("sapphire.patch.directory", dbFile.getParent());
                                String filename = dbFile.getAbsolutePath();
                                try {
                                    AntUtil.runFile(platform, dbFile, new File(filename + "_log"), "installer", antParams, new AntInstallListener(out));
                                }
                                catch (Throwable t) {
                                    ConsoleController.jspLiveLog(out, "<br/><font color=\"Red\">Error running Ant file '" + filename + "'. Exception: " + t.getMessage() + "</font>");
                                    ConsoleController.jspLiveLog(out, "<br/><a href=\"#\" onclick=\"document.getElementById( 'stacktrace' ).style.display = 'block'\">Show full stack trace</a>", true);
                                    ConsoleController.jspLiveLog(out, "<div id=\"stacktrace\" style=\"display:none\">", true);
                                    ConsoleController.jspLiveLog(out, LogUtil.getStackTraceMessages(t, "<br/>", true, false));
                                    ConsoleController.jspLiveLog(out, "</div>", true);
                                    throw new SapphireException("Error running Ant file '" + dbFile.getAbsolutePath() + "'. Reason: " + t.getMessage(), t);
                                }
                                finally {
                                    ConsoleController.jspLiveLog(out, "<br/><p style=\"color:Blue\">Ant file '" + filename + "', target 'installer' complete</p>");
                                }
                            }
                        }
                        String log = patch.getVersion() != null && !patch.getVersion().isEmpty() ? (isPatch ? "Patch" : "Component") + ": " + patch.getPatchid() + " (v" + patch.getVersion() + ") - file " + dbFile.getName() + " executed" : (isPatch ? "Patch" : "Component") + ": " + patch.getPatchid() + " - file " + dbFile.getName() + " executed";
                        Object[] objectArray = new Object[2];
                        idCount[0] = idCount[0] + 1;
                        objectArray[0] = id + idCount[0];
                        objectArray[1] = log;
                        db.executePreparedUpdate("INSERT INTO sysconfig (propertyid, propertyvalue) VALUES ( ?, ?)", objectArray);
                        db.getConnection().commit();
                        continue;
                    }
                    ConsoleController.jspLiveLog(out, "SKIP " + patchFile.getFile(File.separatorChar) + " due to DBMS requirement not met");
                    continue;
                }
                ConsoleController.jspLiveLog(out, "SKIP " + patchFile.getFile(File.separatorChar) + " due to platform requirement not met");
            }
            patch.setApplydt(DateTimeUtil.getNowCalendar());
            patch.setApplyApplicationid(applicationid);
            patch.setApplyDatabaseid(databaseid);
            patches.set(i, patch);
        }
        if (installXML.getFile().isFile()) {
            installXML.save();
        }
    }

    public static void applyWebXMLSnippet(String snippetid, File webXML, File snippet, String action) throws IOException, SapphireException {
        String webXMLContents = FileUtil.getFileString(webXML);
        String snippetContents = FileUtil.getFileString(snippet);
        Document snippetDOM = DOMUtil.getNewDocument(snippet, false);
        webXMLContents = ConsoleController.applySnippet(snippetid, webXMLContents, snippetContents, snippetDOM, "servlet", "servlet-name", action);
        webXMLContents = ConsoleController.applySnippet(snippetid, webXMLContents, snippetContents, snippetDOM, "servlet-mapping", "servlet-name", action);
        webXMLContents = ConsoleController.applySnippet(snippetid, webXMLContents, snippetContents, snippetDOM, "filter", "filter-name", action);
        webXMLContents = ConsoleController.applySnippet(snippetid, webXMLContents, snippetContents, snippetDOM, "filter-mapping", "filter-name", action);
        FileUtil.writeFileString(webXML, webXMLContents);
    }

    private static String applySnippet(String snippetid, String webXMLContents, String snippetContents, Document snippetDOM, String element, String nameElement, String action) {
        int i;
        String[] elementSnippets = null;
        String[] elementSnippetsReplace = null;
        NodeList nodeList = snippetDOM.getElementsByTagName(element);
        int startpos = 0;
        int endpos = 0;
        int elementLen = ("<" + element + ">").length();
        int nameElementLen = ("<" + nameElement + ">").length();
        if (nodeList != null) {
            elementSnippets = new String[nodeList.getLength()];
            elementSnippetsReplace = new String[nodeList.getLength()];
            for (i = 0; i < nodeList.getLength(); ++i) {
                startpos = snippetContents.indexOf("<" + element + ">", endpos);
                endpos = snippetContents.indexOf("</" + element + ">", startpos);
                elementSnippets[i] = "    " + snippetContents.substring(startpos, endpos + elementLen + 1);
                elementSnippetsReplace[i] = "    " + snippetContents.substring(startpos, endpos + elementLen + 1);
                if (action.equals("add")) {
                    elementSnippets[i] = ConsoleController.addCustomElementComment(snippetid, element, elementSnippets[i]);
                    continue;
                }
                if (!action.equals("replace")) continue;
                elementSnippetsReplace[i] = ConsoleController.addCustomElementComment(snippetid, element, elementSnippets[i]);
            }
        }
        if (elementSnippets != null) {
            for (i = 0; i < elementSnippets.length; ++i) {
                int pos;
                if (action.equals("add")) {
                    webXMLContents = ConsoleController.addWebXMLSnippet(webXMLContents, element, elementSnippets[i], elementLen);
                    continue;
                }
                if (action.equals("replace")) {
                    String check;
                    String replaceElementName = elementSnippets[i].substring(elementSnippets[i].indexOf("<" + nameElement + ">") + nameElementLen, elementSnippets[i].indexOf("</" + nameElement + ">"));
                    pos = webXMLContents.indexOf("<" + nameElement + ">" + replaceElementName + "</" + nameElement + ">");
                    if (pos > -1 && !(check = webXMLContents.substring(0, pos).trim()).endsWith("<" + element + ">")) {
                        pos = webXMLContents.indexOf("<" + nameElement + ">" + replaceElementName + "</" + nameElement + ">", pos + nameElementLen);
                    }
                    if (pos >= 0) {
                        String elementSnippet = elementSnippets[i].substring(elementSnippets[i].indexOf("<" + nameElement + ">"));
                        webXMLContents = webXMLContents.substring(0, pos) + elementSnippet + webXMLContents.substring(webXMLContents.indexOf("</" + element + ">", pos) + elementLen + 1);
                        continue;
                    }
                    webXMLContents = ConsoleController.addWebXMLSnippet(webXMLContents, element, elementSnippetsReplace[i], elementLen);
                    continue;
                }
                if (!action.equals("delete")) continue;
                String elementName = elementSnippets[i].substring(elementSnippets[i].indexOf("<" + nameElement + ">") + nameElementLen, elementSnippets[i].indexOf("</" + nameElement + ">"));
                pos = webXMLContents.indexOf("<" + nameElement + ">" + elementName + "</" + nameElement + ">", webXMLContents.indexOf("<" + element + ">"));
                if (pos < 0) continue;
                pos -= elementLen;
                String searchStr = "<!-- Start custom " + element + " snippet";
                while (pos > -1 && !webXMLContents.substring(pos, pos + searchStr.length()).equals(searchStr)) {
                    --pos;
                }
                int end = webXMLContents.indexOf("<!-- End custom " + element + " snippet", pos);
                end = webXMLContents.indexOf("-->", end);
                if (pos <= -1) continue;
                webXMLContents = webXMLContents.substring(0, pos) + webXMLContents.substring(end + 4 + (webXMLContents.substring(end + 4, end + 8).equals("    ") ? 4 : 0));
            }
        }
        return webXMLContents;
    }

    private static String addCustomElementComment(String snippetid, String element, String elementSnippet) {
        return "    <!-- Start custom " + element + " snippet - " + snippetid + " -->\n" + elementSnippet + "\n    <!-- End custom " + element + " snippet - " + snippetid + " -->";
    }

    private static String addWebXMLSnippet(String webXMLContents, String element, String elementSnippet, int elementLen) {
        String startPortalTag = "<!-- Start portal -->";
        String endPortalTag = "<!-- End portal -->";
        int startPortalIndex = webXMLContents.indexOf(startPortalTag);
        int endPortalIndex = webXMLContents.indexOf(endPortalTag);
        String searchStr = "</" + element + ">";
        int pos = webXMLContents.lastIndexOf(searchStr);
        if (webXMLContents.substring(pos + searchStr.length(), pos + searchStr.length() + 3).equals("-->")) {
            pos += 3;
        }
        pos = pos >= startPortalIndex && pos <= endPortalIndex ? endPortalIndex + endPortalTag.length() : pos + elementLen + 1;
        webXMLContents = webXMLContents.substring(0, pos) + "\n" + elementSnippet + webXMLContents.substring(pos);
        return webXMLContents;
    }

    public static ArrayList<String> getWebXMLSnippets(File appWebXML) throws IOException {
        int end;
        String appWebXMLContents = FileUtil.getFileString(appWebXML);
        ArrayList<String> snippets = new ArrayList<String>();
        int pos = appWebXMLContents.indexOf("<!-- Start custom");
        while (pos >= 0) {
            end = appWebXMLContents.indexOf("<!-- End custom", pos);
            if (end >= 0) {
                end = appWebXMLContents.indexOf("-->", end);
                snippets.add(appWebXMLContents.substring(pos, end + 3));
                pos = appWebXMLContents.indexOf("<!-- Start custom", end);
                continue;
            }
            pos = appWebXMLContents.indexOf("<!-- Start custom", pos + 1);
        }
        pos = appWebXMLContents.indexOf("<!-- Start portal");
        while (pos >= 0) {
            end = appWebXMLContents.indexOf("<!-- End portal", pos);
            if (end >= 0) {
                end = appWebXMLContents.indexOf("-->", end);
                snippets.add(appWebXMLContents.substring(pos, end + 3));
                pos = appWebXMLContents.indexOf("<!-- Start portal", end);
                continue;
            }
            pos = appWebXMLContents.indexOf("<!-- Start portal", pos + 1);
        }
        return snippets;
    }

    public static void addWebXMLSnippets(File webXML, ArrayList<String> snippets) throws IOException {
        String startPortalTag = "<!-- Start portal -->";
        String endPortalTag = "<!-- End portal -->";
        if (snippets.size() > 0) {
            String webXMLContents = FileUtil.getFileString(webXML);
            for (String snippet : snippets) {
                int startPortalIndex = webXMLContents.indexOf(startPortalTag);
                int endPortalIndex = webXMLContents.indexOf(endPortalTag);
                if (snippet.startsWith(startPortalTag) && snippet.endsWith(endPortalTag)) {
                    if (startPortalIndex <= -1 || endPortalIndex <= -1) continue;
                    String part1 = webXMLContents.substring(0, startPortalIndex);
                    String part2 = webXMLContents.substring(endPortalIndex + endPortalTag.length());
                    webXMLContents = part1 + snippet + part2;
                    continue;
                }
                String element = snippet.substring(18, snippet.indexOf(" snippet -"));
                String endElementTag = "</" + element + ">";
                int lastElementIndex = webXMLContents.lastIndexOf(endElementTag) + endElementTag.length();
                if (lastElementIndex >= startPortalIndex && lastElementIndex <= endPortalIndex) {
                    lastElementIndex = endPortalIndex + endPortalTag.length();
                }
                int nextCommentStart = webXMLContents.indexOf("<!--", lastElementIndex);
                int nextCommentStop = webXMLContents.indexOf("-->", lastElementIndex) + 3;
                if (nextCommentStart > -1 && nextCommentStart > nextCommentStop) {
                    lastElementIndex = nextCommentStop;
                }
                String part1 = webXMLContents.substring(0, lastElementIndex) + "\n";
                String part2 = webXMLContents.substring(lastElementIndex);
                webXMLContents = part1 + snippet + part2;
            }
            FileUtil.writeFileString(webXML, webXMLContents);
        }
    }

    public static void configurePortalWebXML(ServletContext servletContext, JspWriter out, PropertyList requestdata) throws IOException {
        ArrayList<PropertyList> portalProperties = new ArrayList<PropertyList>();
        String appid = requestdata.getProperty("applicationid");
        String sapphirehome = requestdata.getProperty("sapphire.home");
        String applicationtype = requestdata.getProperty("type");
        String managedear = requestdata.getProperty("managedear");
        String platform = requestdata.getProperty("platform");
        String debug = requestdata.getProperty("debug");
        String encrypt = requestdata.getProperty("encrypt");
        String encryptcookies = requestdata.getProperty("encryptcookies");
        String autorefreshproperties = requestdata.getProperty("autorefreshproperties");
        String portalonly = requestdata.getProperty("portalonly");
        String[] servlet = requestdata.getProperty("servlet", "").split(";");
        String[] portal = requestdata.getProperty("portal", "").split(";");
        String[] databaseid = requestdata.getProperty("databaseid", "").split(";");
        String[] urlpattern = requestdata.getProperty("urlpattern", "").split(";");
        for (int i = 0; i < databaseid.length; ++i) {
            PropertyList params = new PropertyList();
            params.setProperty("applicationid", appid);
            params.setProperty("sapphire.home", sapphirehome);
            params.setProperty("type", applicationtype);
            params.setProperty("managedear", managedear);
            params.setProperty("platform", platform);
            params.setProperty("debug", debug);
            params.setProperty("encrypt", encrypt);
            params.setProperty("encryptcookies", encryptcookies);
            params.setProperty("autorefreshproperties", autorefreshproperties);
            params.setProperty("portalonly", portalonly);
            params.setProperty("databaseid", databaseid[i]);
            params.setProperty("urlpattern", urlpattern[i]);
            params.setProperty("servlet", servlet[i]);
            params.setProperty("portal", portal[i]);
            portalProperties.add(params);
        }
        ConsoleController.configurePortalWebXML(ConsoleController.getConfiguration(servletContext), out, portalProperties);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void configurePortalWebXML(Configuration configuration, JspWriter out, List<PropertyList> portalProperties) throws IOException {
        boolean restartRequired = false;
        if (portalProperties.size() > 0) {
            block15: {
                PropertyList params = portalProperties.get(0);
                String applicationid = params.getProperty("applicationid");
                String type = params.getProperty("type");
                String managedear = params.getProperty("managedear");
                boolean deployed = false;
                String databaseid = params.getProperty("databaseid");
                String sapphireHome = params.getProperty("sapphire.home");
                File appHome = ConsoleController.getAppHome(sapphireHome, applicationid);
                File temp = new File(appHome, "temp");
                if (applicationid.length() > 0) {
                    ConsoleController.jspLiveLog(out, "Application: " + applicationid);
                    ConsoleController.jspLiveLog(out, "Application HOME: " + appHome.getAbsolutePath());
                    if (configuration instanceof JBoss) {
                        deployed = ((JBoss)((Object)configuration)).isDeployed(managedear);
                        ConsoleController.jspLiveLog(out, "Deployment status: " + (deployed ? "Deployed" : "<font color=\"Red\">Undeployed</font>"));
                    }
                }
                if (databaseid.length() > 0) {
                    ConsoleController.jspLiveLog(out, "Database: " + databaseid);
                }
                try {
                    ConsoleFileOperationProgress progress = new ConsoleFileOperationProgress(out);
                    ConsoleController.jspLiveLog(out, " - clearing temp area...");
                    if (temp.exists()) {
                        progress.startProgress(" - determining files to delete...", temp);
                        FileUtil.deleteAll(temp, progress, false);
                        progress.endProgress();
                    }
                    temp.mkdirs();
                    File ear = null;
                    File war = null;
                    String appwar = null;
                    int earFileCount = 0;
                    int warFileCount = 0;
                    if (applicationid.length() <= 0) break block15;
                    if (type.equals("P")) {
                        ear = new File(temp, "application");
                        ConsoleController.jspLiveLog(out, "<b>Expanding application EAR to " + ear.getAbsolutePath() + "...</b>");
                        progress.startProgress();
                        earFileCount = ZipFileUtil.extractAll(new File(appHome, "ear/" + managedear), ear, progress);
                        progress.endProgress();
                        appwar = ConsoleController.getApplicationXMLProps(new File(ear, "META-INF/application.xml")).getProperty("application.war", STANDARD_WAR);
                        war = new File(ear, appwar + "_patch");
                        ConsoleController.jspLiveLog(out, " - expanding application WAR to " + war.getAbsolutePath() + "...");
                        progress.startProgress();
                        warFileCount = ZipFileUtil.extractAll(new File(ear, appwar), war, progress);
                        progress.endProgress();
                    } else {
                        ear = new File(((JBoss)((Object)configuration)).getDeploymentDir(), managedear);
                        appwar = ConsoleController.getApplicationXMLProps(new File(ear, "META-INF/application.xml")).getProperty("application.war", STANDARD_WAR);
                        war = new File(ear, appwar);
                    }
                    ConsoleController.jspLiveLog(out, "<b>Configuring Portal web.xml...</b>");
                    File webXML = new File(war, "WEB-INF/web.xml");
                    ConsoleController.jspLiveLog(out, webXML.getAbsolutePath());
                    BaseConverter converter = BaseConverter.getInstance(Configuration.getPlatformName(configuration.getPlatform()));
                    converter.configurePortalXML(webXML, null, portalProperties);
                    if (type.equals("P")) {
                        ConsoleController.jspLiveLog(out, "<b>Rebuilding application...</b>");
                        ConsoleController.jspLiveLog(out, " - rebuilding WAR " + appwar + "...");
                        PackagerFile patchedWar = new PackagerFile(new File(ear, appwar));
                        patchedWar.addFile(war, war);
                        progress.startProgress(warFileCount);
                        patchedWar.save(progress);
                        progress.endProgress();
                        ConsoleController.jspLiveLog(out, " - clearing old WAR...");
                        progress.startProgress(warFileCount);
                        FileUtil.deleteAll(war, progress, false);
                        progress.endProgress();
                        ConsoleController.jspLiveLog(out, " - rebuilding EAR...");
                        File targetEar = new File(appHome, "ear/" + managedear);
                        if (targetEar.exists()) {
                            ConsoleController.backupFile(targetEar, "webxml");
                        }
                        PackagerFile patchedEar = new PackagerFile(targetEar);
                        patchedEar.addFile(ear, ear);
                        progress.startProgress(earFileCount);
                        patchedEar.save(progress);
                        progress.endProgress();
                    }
                    restartRequired = true;
                    ConsoleController.jspLiveLog(out, " - clearing temp area...");
                    if (temp.exists()) {
                        progress.startProgress(earFileCount + warFileCount);
                        FileUtil.deleteAll(temp, progress, false);
                        progress.endProgress();
                    }
                    if (type.equals("P") && deployed) {
                        ConsoleController.jspLiveLog(out, "<b>Deploying application to JBoss</b>");
                        ((JBoss)((Object)configuration)).deployEAR(appHome, "P", new File(params.getProperty("managedear")), true, progress);
                    }
                    ConsoleController.jspLiveLog(out, "<br/><h1 style=\"color:Blue\">Finished Portal web.xml configuration</h1><br/>");
                }
                catch (Exception e) {
                    try {
                        ConsoleController.jspLiveLog(out, "<br/><b><font color=\"Red\">Configure Portal web.xml failed. Reason: " + e.getMessage() + "</font></b>");
                        ConsoleController.jspLiveLog(out, "<br/><a href=\"#\" onclick=\"document.getElementById( 'stacktrace' ).style.display = 'block'\">Show full stack trace</a>", true);
                        ConsoleController.jspLiveLog(out, "<div id=\"stacktrace\" style=\"display:none\">", true);
                        ConsoleController.jspLiveLog(out, LogUtil.getStackTraceMessages(e, "<br/>", true, false));
                        ConsoleController.jspLiveLog(out, "</div>", true);
                    }
                    catch (Throwable throwable) {
                        ConsoleController.jspLiveLog(out, "<br/><h1><a href=\"javascript:parent.executeOperation( 'loadapplications' )\">Return to application list</a></h1><br/>", true);
                        ConsoleController.jspLiveLog(out, "<br id=\"complete\"/><script>document.getElementById( \"complete\" ).scrollIntoView( true )</script>", true);
                        ConsoleController.jspLiveLog(out, "<script>parent.jspComplete( 'configureportal" + (restartRequired ? "restart" : "") + "' );</script>", true);
                        throw throwable;
                    }
                    ConsoleController.jspLiveLog(out, "<br/><h1><a href=\"javascript:parent.executeOperation( 'loadapplications' )\">Return to application list</a></h1><br/>", true);
                    ConsoleController.jspLiveLog(out, "<br id=\"complete\"/><script>document.getElementById( \"complete\" ).scrollIntoView( true )</script>", true);
                    ConsoleController.jspLiveLog(out, "<script>parent.jspComplete( 'configureportal" + (restartRequired ? "restart" : "") + "' );</script>", true);
                }
            }
            ConsoleController.jspLiveLog(out, "<br/><h1><a href=\"javascript:parent.executeOperation( 'loadapplications' )\">Return to application list</a></h1><br/>", true);
            ConsoleController.jspLiveLog(out, "<br id=\"complete\"/><script>document.getElementById( \"complete\" ).scrollIntoView( true )</script>", true);
            ConsoleController.jspLiveLog(out, "<script>parent.jspComplete( 'configureportal" + (restartRequired ? "restart" : "") + "' );</script>", true);
        }
    }

    private static File createEARIdFile(File dir) throws IOException {
        File file = new File(dir, "ear.id");
        FileUtil.writeFileString(file, String.valueOf(System.currentTimeMillis()));
        return file;
    }

    private static void copyMinifiedFiles(File file) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; ++i) {
                    ConsoleController.copyMinifiedFiles(files[i]);
                }
            }
        } else {
            File js;
            String name = file.getName();
            if (name.endsWith(".js-orig") && (js = new File(file.getParentFile(), name.substring(0, name.indexOf("-orig")))).exists()) {
                FileUtil.copyFile(js, new File(file.getParentFile(), name.substring(0, name.indexOf("-orig")) + "-min"));
                FileUtil.copyFile(file, new File(file.getParentFile(), name.substring(0, name.indexOf("-orig"))));
            }
        }
    }

    private static class ImportLogger
    implements Logger {
        private JspWriter out;

        public ImportLogger(JspWriter out) {
            this.out = out;
        }

        @Override
        public void log(String message) {
            try {
                BaseController.jspLiveLog(this.out, "-- " + message);
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
    }

    private static enum PatchZipContents {
        APP,
        EAR,
        JAR,
        WAR,
        DB,
        CONSOLE;

    }
}

