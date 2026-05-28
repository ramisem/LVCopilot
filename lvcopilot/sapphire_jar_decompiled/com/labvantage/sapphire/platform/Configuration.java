/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 */
package com.labvantage.sapphire.platform;

import com.labvantage.sapphire.BaseAccessor;
import com.labvantage.sapphire.Build;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.License;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.misc.GenerateComponentKey;
import com.labvantage.sapphire.admin.system.automation.Server;
import com.labvantage.sapphire.platform.SapphireDatabase;
import com.labvantage.sapphire.platform.StandaloneConfiguration;
import com.labvantage.sapphire.platform.jboss.JBoss6xConfiguration;
import com.labvantage.sapphire.platform.jboss.JBoss7xConfiguration;
import com.labvantage.sapphire.platform.jboss.JBossConfiguration;
import com.labvantage.sapphire.platform.weblogic.WeblogicConfiguration;
import com.labvantage.sapphire.platform.websphere.WebSphereConfiguration;
import com.labvantage.sapphire.platform.websphere.WebSphereLibertyConfiguration;
import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.util.jndi.ServiceLocator;
import com.labvantage.sapphire.util.logger.LogConfig;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.servlet.ServletContext;
import sapphire.SapphireException;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public abstract class Configuration {
    public static final int PLATFORM_STANDALONE = 0;
    public static final int PLATFORM_WEBSPHERE = 2;
    public static final int PLATFORM_WEBLOGIC = 3;
    public static final int PLATFORM_JBOSS = 4;
    public static final int PLATFORM_JBOSS6x = 5;
    public static final int PLATFORM_JBOSS7x = 6;
    public static final String CONFIG_SERVERINFO = "serverinfo";
    public static final String CONFIG_HOSTNAME = "hostname";
    public static final String CONFIG_LABVANTAGEHOME = "LABVANTAGE_HOME";
    public static final String CONFIG_LABVANTAGESERVER = "LABVANTAGE_SERVER";
    public static final String CONFIG_SERVERPROFILE = "SERVERPROFILE";
    public static final String CONFIG_AUTOMATIONSERVER = "AUTOMATION_SERVER";
    public static final String CONFIG_JUNITSERVER = "JUNIT_SERVER";
    public static final String CONFIG_APPLICATIONID = "applicationid";
    public static final String CONFIG_HTTPURL = "httpurl";
    public static final int STATE_READY = 0;
    public static final int STATE_INVALID_CONFIG = 1;
    public static final int STATE_INSTANCE_CREATED = 2;
    public static final int STATE_ADMINDB_NOT_CREATED = 3;
    public static final int STATE_ADMINDB_INVALID_VERSION = 4;
    public static final String SYSTEM_USERID = "(system)";
    public static final String SYSTEM_PROFILE = "System";
    public static final String PRODUCT_NAME = "LabVantage";
    public static final String EXPRESS_NAME = "Express";
    public static final String DEFAULT_ALLDATABASES = "(default)";
    public static final int JARLIST_POSITION_CONTEXT = 0;
    public static final int JARLIST_POSITION_JAR = 1;
    public static final int JARLIST_POSITION_LICENSE = 5;
    public static final int JARLIST_POSITION_VERSION = 4;
    public static String[][] currentAllJarDetails = Configuration.getCurrentJars(null);
    public static String[][] currentRuntimeJarDetails = Configuration.getCurrentJars("labvantage-runtime-dependencies");
    public static String[] oldJars = Configuration.getOldJars();
    private boolean valid = false;
    private License license;
    private boolean cluster = true;
    private boolean primaryServer = true;
    private boolean primaryStarted = false;
    private String applicationid;
    private String applicationdesc;
    private String applicationear;
    private String appDir = "";
    private String labvantageHome;
    private String serverInfo;
    private String serverHostName;
    private String serverHttpURL;
    private String serverid;
    private String clusterName;
    private String admindbBuild;
    private String jndiEJBPrefix;
    private HashMap<String, SapphireDatabase> databases = new HashMap();
    private ArrayList loggerFiles = new ArrayList();
    private int platform;
    private boolean requireLicensePerDatabase;
    private static HashMap<String, Boolean> devmode = new HashMap();
    private static HashMap<String, String> compcode = new HashMap();
    private static HashMap<String, PropertyList> databaseSecurityPolicy = new HashMap();
    private static HashMap<String, License> databaseLicense = new HashMap();
    private static HashMap<String, String> serverProfile = new HashMap();
    private static HashMap<String, String> isAutomationServer = new HashMap();
    private static ArrayList<String> compDevCodes = new ArrayList();
    private static int state;
    private static String errormsg;
    private static Configuration configuration;
    private static boolean junitServer;

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    static String[][] getCurrentJars(String context) {
        try (InputStream input = Configuration.class.getResourceAsStream("/current.info.jars.txt");){
            if (input == null) {
                throw new IOException("/current.info.jars.txt not found");
            }
            List stringList = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)).lines().collect(Collectors.toList());
            ArrayList<String[]> currentJarList = new ArrayList<String[]>();
            for (String s : stringList) {
                String[] splitLine = s.split("\t", -1);
                if (context != null && !splitLine[0].equals(context)) continue;
                currentJarList.add(splitLine);
            }
            String[][] stringArray = (String[][])currentJarList.toArray((T[])new String[0][0]);
            return stringArray;
        }
        catch (IOException e) {
            throw new RuntimeException("current.info.jars.txt file not found in classpath!", e);
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    static String[] getOldJars() {
        String[][] current = Configuration.getCurrentJars("labvantage-runtime-dependencies");
        ArrayList<String> currentJars = new ArrayList<String>();
        for (String[] strings : current) {
            currentJars.add(strings[1]);
        }
        try (InputStream input = Configuration.class.getResourceAsStream("/historical.jars.txt");){
            if (input == null) {
                throw new IOException("/historical.jars.txt not found");
            }
            List stringList = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)).lines().collect(Collectors.toList());
            String[] stringArray = stringList.stream().filter(line -> !currentJars.contains(line)).collect(Collectors.toList()).toArray(new String[0]);
            return stringArray;
        }
        catch (IOException e) {
            throw new RuntimeException("historical.jars.txt file not found in classpath!", e);
        }
    }

    public static boolean isJunitServer() {
        return junitServer;
    }

    public static boolean isDevmode(String databaseid) {
        return devmode.containsKey(databaseid) && devmode.get(databaseid) != false;
    }

    public static void setDevmode(String databaseid, boolean devmode) {
        Configuration.devmode.put(databaseid, devmode);
    }

    public static void setCompcode(String databaseid, String compmode) {
        compcode.put(databaseid, compmode);
    }

    public static String getCompcode(String databaseid) {
        String compcode = Configuration.compcode.get(databaseid);
        return compcode != null ? compcode : "";
    }

    public static ArrayList<String> getCompDevCodes() {
        return compDevCodes;
    }

    public static PropertyList getDatabaseSecurityPolicy(String databaseid, boolean virtualUser, boolean portalUser) {
        return databaseSecurityPolicy.get(databaseid + (virtualUser ? "_virtual" : (portalUser ? "_portal" : "")));
    }

    public static void setDatabaseSecurityPolicy(String databaseid, PropertyList standardPolicy, PropertyList virtualPolicy, PropertyList portalPolicy) {
        databaseSecurityPolicy.put(databaseid, standardPolicy);
        databaseSecurityPolicy.put(databaseid + "_virtual", virtualPolicy);
        databaseSecurityPolicy.put(databaseid + "_portal", portalPolicy);
    }

    public static String checkJarList() {
        HashSet<String> old = new HashSet<String>();
        for (String string : oldJars) {
            old.add(string.toLowerCase());
        }
        for (String string : currentRuntimeJarDetails) {
            if (!old.contains(string[1].toLowerCase())) continue;
            return "CODING ERROR: Current JAR " + string[1] + " exists in old JAR list";
        }
        return "";
    }

    public static String[] getCommonJarList() {
        String[] jars = new String[currentRuntimeJarDetails.length];
        for (int i = 0; i < currentRuntimeJarDetails.length; ++i) {
            jars[i] = currentRuntimeJarDetails[i][1];
        }
        return jars;
    }

    public static String getPlatformName(int platform) {
        return platform == 4 ? "JBoss" : (platform == 6 ? "JBoss7x" : (platform == 5 ? "JBoss6x" : (platform == 3 ? "WebLogic" : (platform == 2 ? "WebSphere" : ""))));
    }

    public static Configuration getInstance() throws SapphireException {
        if (configuration == null) {
            throw new SapphireException("INVALID_SERVER", "Configuration instance has not been created.");
        }
        return configuration;
    }

    public static boolean isCreated() {
        return configuration != null;
    }

    public static boolean getPropertyValueAsBoolean(String value, boolean defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return value.toLowerCase().startsWith("y") || value.equalsIgnoreCase("true");
    }

    public static Configuration createInstance(PropertyList configProps) throws SapphireException {
        if (configuration != null) {
            throw new SapphireException("Instance already created.");
        }
        state = 1;
        errormsg = "Unknown configuration error - check logs for errors";
        junitServer = Configuration.getPropertyValueAsBoolean(configProps.getProperty(CONFIG_JUNITSERVER), false);
        try {
            File[] files;
            boolean isRequireLicensePerDatabase;
            String serverInfo = configProps.getProperty(CONFIG_SERVERINFO);
            configuration = Configuration.getPlatformConfiguration(serverInfo);
            if (configuration == null) {
                errormsg = "Unrecognized platform: " + serverInfo;
                throw new SapphireException(errormsg);
            }
            Configuration.configuration.labvantageHome = (String)configProps.get(CONFIG_LABVANTAGEHOME);
            Configuration.configuration.serverHostName = (String)configProps.get(CONFIG_HOSTNAME);
            if (Configuration.configuration.labvantageHome == null || !new File(Configuration.configuration.labvantageHome).exists()) {
                errormsg = "LABVANTAGE_HOME (" + Configuration.configuration.labvantageHome + ") invalid or not defined";
                throw new SapphireException(errormsg);
            }
            Configuration.configuration.serverid = configProps.getProperty(CONFIG_LABVANTAGESERVER);
            Configuration.configuration.applicationid = configProps.getProperty(CONFIG_APPLICATIONID);
            if (Configuration.configuration.applicationid == null || Configuration.configuration.applicationid.length() == 0) {
                errormsg = "Application name has not been defined in web.xml - EAR may need migrating using LabVantage Console";
                throw new SapphireException(errormsg);
            }
            if (!new File(Configuration.configuration.labvantageHome + "/applications/" + Configuration.configuration.applicationid).exists()) {
                errormsg = "Application directory has not be correctly defined in LABVANTAGE_HOME";
                throw new SapphireException(errormsg + ":" + Configuration.configuration.labvantageHome + "/applications/" + Configuration.configuration.applicationid);
            }
            Configuration.configuration.appDir = "/applications/" + Configuration.configuration.applicationid;
            Configuration.configuration.serverHttpURL = StringUtil.replaceAll(configProps.getProperty(CONFIG_HTTPURL), "[httpport]", configuration.getHttpPort());
            Configuration.configuration.serverHttpURL = StringUtil.replaceAll(Configuration.configuration.serverHttpURL, "[httphost]", configuration.getHttpHost());
            String serverProfile = configProps.getProperty(CONFIG_SERVERPROFILE, ConfigService.getConfigProperty("com.labvantage.sapphire.server.serverprofile"));
            Configuration.serverProfile.put(DEFAULT_ALLDATABASES, serverProfile);
            String automationServer = configProps.getProperty(CONFIG_AUTOMATIONSERVER, ConfigService.getConfigProperty("com.labvantage.sapphire.server.automationserver"));
            isAutomationServer.put(DEFAULT_ALLDATABASES, automationServer);
            try {
                Configuration.upgradeLog4jConfig(configuration.getServerid(), configuration.getApplicationid(), configuration.getApplicationHome());
                File logConfig = configuration.getServerLogConfigFile();
                if (!logConfig.exists()) {
                    throw new SapphireException("Failed to find logging configuration file (" + logConfig.getAbsolutePath() + ")");
                }
                if (!new File(logConfig.getParentFile(), "/logs").exists()) {
                    new File(logConfig.getParentFile(), "/logs").mkdirs();
                }
                LogConfig.configure(logConfig);
            }
            catch (SapphireException e) {
                errormsg = "Failed to setup logging options: Reason: " + e.getMessage();
                throw e;
            }
            Configuration.configuration.requireLicensePerDatabase = isRequireLicensePerDatabase = Configuration.getPropertyValueAsBoolean(ConfigService.getConfigProperty("com.labvantage.sapphire.server.requiredatabaselicense"), false);
            if (!isRequireLicensePerDatabase || configuration.getServerLicenseFile().exists()) {
                try {
                    configuration.setServerLicense(configuration.getServerLicenseFile());
                }
                catch (SapphireException e) {
                    errormsg = "Failed to load license file. Reason: " + e.getMessage();
                    throw e;
                }
            }
            if ((files = new File(Configuration.configuration.labvantageHome).listFiles()) != null) {
                for (int i = 0; i < files.length; ++i) {
                    if (!files[i].getName().startsWith("Comp-")) continue;
                    String compcode = files[i].getName().substring(5, files[i].getName().indexOf("."));
                    Properties props = new Properties();
                    props.load(new FileInputStream(files[i]));
                    if (!EncryptDecrypt.encodeComponentKey(compcode, GenerateComponentKey.class.getSimpleName()).equals(props.getProperty("key"))) continue;
                    compDevCodes.add(compcode);
                }
            }
            Configuration.configuration.jndiEJBPrefix = ConfigService.getConfigProperty("com.labvantage.sapphire.server.jndiprefix", "com/labvantage/sapphire");
            BaseAccessor.setLocal(true);
            BaseAccessor.setJNDIPrefix(configuration.getJNDIEJBPrefix());
            BaseAccessor.setLocalJNDIPrefix(configuration.getJNDILocalEJBPrefix());
            state = 2;
            errormsg = "";
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        return configuration;
    }

    public static void upgradeLog4jConfig(String serverId, String applicationId, String applicationHomePath) throws SapphireException {
        File oldConfigFile;
        File newConfigFile = new File(applicationHomePath + "/" + "labvantagelogging" + ".properties");
        if (newConfigFile.exists()) {
            return;
        }
        if (!serverId.isEmpty() && (newConfigFile = new File(applicationHomePath + "/" + "labvantagelogging" + "_" + serverId + ".properties")).exists()) {
            return;
        }
        boolean isOldConfigExists = false;
        if (!serverId.isEmpty()) {
            oldConfigFile = new File(applicationHomePath + "/" + "labvantagelogging" + "_" + serverId + ".props");
            if (oldConfigFile.exists()) {
                isOldConfigExists = true;
            }
            if (!isOldConfigExists && (oldConfigFile = new File(applicationHomePath + "/sapphirelogging_" + serverId + ".props")).exists()) {
                isOldConfigExists = true;
            }
        } else {
            oldConfigFile = new File(applicationHomePath + "/" + "labvantagelogging" + ".props");
            if (oldConfigFile.exists()) {
                isOldConfigExists = true;
            }
            if (!isOldConfigExists && (oldConfigFile = new File(applicationHomePath + "/sapphirelogging.props")).exists()) {
                isOldConfigExists = true;
            }
        }
        if (isOldConfigExists) {
            String datePattern;
            String[] valueSplitArr;
            String value;
            Properties log4jProps = new Properties();
            try (FileInputStream fis = new FileInputStream(oldConfigFile);){
                log4jProps.load(fis);
                if (log4jProps.getProperty("sapphire.logging.managed", "false").equals("false")) {
                    throw new SapphireException("log4j file needs to be either a) manually converted to log4j2 format or b) switched to sapphire.logging.managed=true");
                }
            }
            catch (Exception e) {
                throw new SapphireException("Failed to load config file. Reason: " + e.getMessage(), e);
            }
            PropertyList props = new PropertyList();
            if (log4jProps.containsKey("log4j.logger.labvantage.sapphire")) {
                value = log4jProps.getProperty("log4j.logger.labvantage.sapphire");
                valueSplitArr = value.split(",");
                props.setProperty("INFO", valueSplitArr[0].trim());
                props.setProperty("sapphirelog", valueSplitArr.length > 1 && valueSplitArr[1].trim().equalsIgnoreCase("sapphire") ? "Y" : "N");
                props.setProperty("errorlog", valueSplitArr.length > 2 && valueSplitArr[2].trim().equalsIgnoreCase("errors") ? "Y" : "N");
            }
            if (log4jProps.containsKey("log4j.logger.labvantage.STARTUP")) {
                value = log4jProps.getProperty("log4j.logger.labvantage.STARTUP");
                valueSplitArr = value.split(",");
                props.setProperty("startuplog", valueSplitArr.length > 1 && valueSplitArr[1].trim().equalsIgnoreCase("sapphirestartup") ? "Y" : "N");
            } else {
                props.setProperty("startuplog", "N");
            }
            if (log4jProps.containsKey("log4j.appender.sapphire.DatePattern")) {
                datePattern = log4jProps.getProperty("log4j.appender.sapphire.DatePattern", "yyyy-MM-dd");
                props.setProperty("custom.sapphire.datePattern", datePattern.replace("'.'", "").trim());
                props.put("sapphirelogtype", "daily");
            } else {
                props.setProperty("appender.sapphire.policies.size.size", log4jProps.getProperty("log4j.appender.sapphire.MaxFileSize", "200MB"));
                props.setProperty("appender.sapphire.strategy.max", log4jProps.getProperty("log4j.appender.sapphire.MaxBackupIndex", "100"));
                props.put("sapphirelogtype", "rolling");
            }
            if (log4jProps.containsKey("log4j.appender.errors.DatePattern")) {
                datePattern = log4jProps.getProperty("log4j.appender.errors.DatePattern", "yyyy-MM-dd");
                if (datePattern.toLowerCase().indexOf("y") > 0) {
                    datePattern = datePattern.trim().substring(datePattern.toLowerCase().indexOf("y"));
                }
                props.setProperty("custom.errors.datePattern", datePattern);
                props.put("errorlogtype", "daily");
            } else {
                props.setProperty("appender.errors.policies.size.size", log4jProps.getProperty("log4j.appender.errors.MaxFileSize", "10MB"));
                props.setProperty("appender.errors.strategy.max", log4jProps.getProperty("log4j.appender.errors.MaxBackupIndex", "10"));
                props.put("errorlogtype", "rolling");
            }
            File configFile = new File(applicationHomePath + "/" + "labvantagelogging" + ".properties");
            LogConfig.generateConfigFile(applicationId, configFile, "", props, false);
            if (!serverId.isEmpty()) {
                configFile = new File(applicationHomePath + "/" + "labvantagelogging" + "_" + serverId + ".properties");
                LogConfig.generateConfigFile(applicationId, configFile, "", props, false);
            }
            if ((oldConfigFile = new File(applicationHomePath + "/" + "labvantagelogging" + ".props")).exists()) {
                oldConfigFile.renameTo(new File(oldConfigFile.getAbsolutePath().replace(".props", ".props_migrated")));
            }
            if ((oldConfigFile = new File(applicationHomePath + "/sapphirelogging.props")).exists()) {
                oldConfigFile.renameTo(new File(oldConfigFile.getAbsolutePath().replace(".props", ".props_migrated")));
            }
            if (!serverId.isEmpty()) {
                oldConfigFile = new File(applicationHomePath + "/" + "labvantagelogging" + "_" + serverId + ".props");
                if (oldConfigFile.exists()) {
                    oldConfigFile.renameTo(new File(oldConfigFile.getAbsolutePath().replace(".props", ".props_migrated")));
                }
                if ((oldConfigFile = new File(applicationHomePath + "/sapphirelogging_" + serverId + ".props")).exists()) {
                    oldConfigFile.renameTo(new File(oldConfigFile.getAbsolutePath().replace(".props", ".props_migrated")));
                }
            }
        }
    }

    public String getHttpHost() throws Exception {
        String httphost = InetAddress.getLocalHost().getHostAddress();
        try {
            String confighttphost = ConfigService.getConfigProperty("com.labvantage.sapphire.server.httphost");
            if (confighttphost != null && confighttphost.length() > 0) {
                httphost = confighttphost;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return httphost;
    }

    public static Configuration getPlatformConfiguration(String serverInfo) {
        Configuration configuration = null;
        if (serverInfo != null && serverInfo.toLowerCase().contains("weblogic")) {
            configuration = new WeblogicConfiguration();
            configuration.platform = 3;
        } else if (serverInfo != null && serverInfo.toLowerCase().contains("websphere liberty")) {
            configuration = new WebSphereLibertyConfiguration();
            configuration.platform = 2;
        } else if (serverInfo != null && serverInfo.toLowerCase().contains("websphere")) {
            configuration = new WebSphereConfiguration();
            configuration.platform = 2;
        } else if (serverInfo != null && serverInfo.contains("WildFly")) {
            configuration = new JBoss7xConfiguration();
            configuration.platform = 6;
        } else if (serverInfo != null && serverInfo.contains("JBoss Web/7")) {
            configuration = new JBoss6xConfiguration();
            configuration.platform = 5;
        } else if (serverInfo != null && (serverInfo.toLowerCase().contains("apache") || serverInfo.toLowerCase().contains("jboss"))) {
            configuration = new JBossConfiguration();
            configuration.platform = 4;
        } else if (serverInfo != null && serverInfo.toLowerCase().contains("standalone")) {
            configuration = new StandaloneConfiguration();
            configuration.platform = 0;
        }
        if (configuration != null) {
            configuration.serverInfo = serverInfo;
        }
        return configuration;
    }

    public static String getSapphireHome(ServletContext servletContext) {
        String sapphireHome = null;
        try {
            sapphireHome = System.getenv(CONFIG_LABVANTAGEHOME);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        if (sapphireHome == null || sapphireHome.length() == 0) {
            sapphireHome = System.getProperty(CONFIG_LABVANTAGEHOME);
        }
        if (sapphireHome == null || sapphireHome.length() == 0) {
            sapphireHome = System.getProperty("com.labvantage.sapphire.server.labvantagehome");
        }
        if (sapphireHome == null || sapphireHome.length() == 0) {
            sapphireHome = servletContext.getInitParameter(CONFIG_LABVANTAGEHOME);
        }
        return sapphireHome;
    }

    public static String getSapphireHomeSource(ServletContext servletContext) {
        String sapphireHome = null;
        try {
            sapphireHome = System.getenv(CONFIG_LABVANTAGEHOME);
            if (sapphireHome != null && sapphireHome.length() > 0) {
                return "ENV:LABVANTAGE_HOME";
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        if ((sapphireHome == null || sapphireHome.length() == 0) && (sapphireHome = System.getProperty(CONFIG_LABVANTAGEHOME)) != null && sapphireHome.length() > 0) {
            return "JVM:LABVANTAGE_HOME";
        }
        if ((sapphireHome == null || sapphireHome.length() == 0) && (sapphireHome = System.getProperty("com.labvantage.sapphire.server.labvantagehome")) != null && sapphireHome.length() > 0) {
            return "JVM:com.labvantage.sapphire.server.labvantagehome";
        }
        if ((sapphireHome == null || sapphireHome.length() == 0) && (sapphireHome = servletContext.getInitParameter(CONFIG_LABVANTAGEHOME)) != null && sapphireHome.length() > 0) {
            return "WEB:LABVANTAGE_HOME";
        }
        return "ERR";
    }

    public void setSapphireHome(String sapphireHome) {
        this.labvantageHome = sapphireHome;
    }

    public void setServerHostName(String serverHostName) {
        this.serverHostName = serverHostName;
    }

    public void setApplicationdesc(String applicationdesc) {
        this.applicationdesc = applicationdesc;
    }

    public void setApplicationEAR(String applicationear) {
        this.applicationear = applicationear;
    }

    public void setServerid(String serverid) {
        this.serverid = serverid;
    }

    public static void clearInstance() {
        configuration = null;
    }

    public static void setState(int state) {
        Configuration.state = state;
    }

    public static int getState() {
        return state;
    }

    public static void setErrorMsg(String errorMsg) {
        errormsg = errorMsg;
    }

    public static String getErrorMsg() {
        return errormsg;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getAdmindbBuild() {
        return this.admindbBuild;
    }

    public void addSapphireDatabase(SapphireDatabase database) {
        this.databases.put(database.getDatabaseId(), database);
    }

    public void removeSapphireDatabase(String databaseid) {
        this.databases.remove(databaseid);
    }

    public SapphireDatabase getSapphireDatabase(String databaseid) throws SapphireException {
        SapphireDatabase database = this.databases.get(databaseid);
        if (database == null) {
            throw new SapphireException("Unrecognized database: " + databaseid);
        }
        return database;
    }

    public boolean isDatabaseActive(String databaseid) {
        return this.databases.get(databaseid) != null;
    }

    public List<SapphireDatabase> getSapphireDatabases() {
        ArrayList<SapphireDatabase> databaseList = new ArrayList<SapphireDatabase>();
        for (String databaseid : this.databases.keySet()) {
            databaseList.add(this.databases.get(databaseid));
        }
        return databaseList;
    }

    public void addLoggerFile(String loggerFile) {
        this.loggerFiles.add(loggerFile);
    }

    public ArrayList getLoggerFiles() {
        return this.loggerFiles;
    }

    public void setPrimaryServer(boolean primaryServer) {
        this.primaryServer = primaryServer;
    }

    public void setPrimaryStarted(boolean primaryStarted) {
        this.primaryStarted = primaryStarted;
    }

    public void setCluster(boolean cluster) {
        this.cluster = cluster;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
        this.cluster = clusterName != null && clusterName.length() > 0;
    }

    public void setServerLicense(File licenseFile) throws SapphireException {
        this.license = new License(licenseFile);
    }

    public void setLicense(File licenseFile, String databaseid) throws SapphireException {
        License newLicense = new License(licenseFile);
        this.setLicense(newLicense, databaseid);
    }

    public void setLicense(License newLicense, String databaseid) throws SapphireException {
        databaseLicense.put(databaseid, newLicense);
    }

    public boolean requireLicensePerDatabase() {
        return this.requireLicensePerDatabase;
    }

    public License getServerLicense() {
        return this.license;
    }

    public License getLicense(String databaseid) {
        if (this.requireLicensePerDatabase) {
            return databaseLicense.get(databaseid);
        }
        if (databaseLicense.containsKey(databaseid)) {
            return databaseLicense.get(databaseid);
        }
        return this.license;
    }

    public boolean hasDatabaseSpecificLicense(String databaseid) {
        return databaseLicense.containsKey(databaseid);
    }

    public boolean isWebServer() {
        return true;
    }

    public boolean isCluster() {
        return this.cluster;
    }

    public boolean isPrimaryServer() {
        return this.primaryServer;
    }

    public boolean isPrimaryStarted() {
        return this.primaryStarted;
    }

    public String getSapphireHome() {
        return this.labvantageHome;
    }

    public String getApplicationHome() {
        return this.labvantageHome + this.appDir;
    }

    public String getApplicationid() {
        return this.applicationid != null && this.applicationid.length() > 0 ? this.applicationid : "labvantage";
    }

    public String getApplicationdesc() {
        return this.applicationdesc != null && this.applicationdesc.length() > 0 ? this.applicationdesc : "";
    }

    public String getApplicationEARName() {
        return this.applicationear != null && this.applicationear.length() > 0 ? this.applicationear : this.getApplicationid();
    }

    public String getServerInfo() {
        return this.serverInfo;
    }

    public String getServerHttpURL() {
        return this.serverHttpURL;
    }

    public Server getServer() throws SapphireException {
        Server server = new Server();
        server.hostid = this.getHostid();
        server.applicationid = this.getApplicationid();
        server.isAutomationServer = this.isAutomationServer();
        return server;
    }

    public Server getServer(SapphireDatabase database) throws SapphireException {
        Server server = this.getServer();
        return server;
    }

    public String getServerHostName() throws SapphireException {
        if (this.serverHostName == null || this.serverHostName.length() == 0) {
            throw new SapphireException("ServerHostName not set in configuration");
        }
        return this.serverHostName;
    }

    public String getHostid() throws SapphireException {
        return this.getServerHostName() + (this.getServerid().length() > 0 ? ":" + this.getServerid() : "");
    }

    public String getServerid() {
        return this.serverid != null && this.serverid.length() > 0 ? this.serverid : "";
    }

    public String getClusterName() {
        return this.clusterName;
    }

    public int getPlatform() {
        return this.platform;
    }

    public void getConfigurationWarnings(ArrayList warningList) throws SapphireException {
        try {
            String securityemailtoaddress;
            String emailfromaddress;
            String smtpHost = ConfigService.getConfigProperty("com.labvantage.sapphire.server.smtphost");
            if (smtpHost == null || smtpHost.length() == 0) {
                warningList.add("Config property com.labvantage.sapphire.server.smtphost is not defined - this will prevent Mail actions and alerts from working");
            }
            if ((emailfromaddress = ConfigService.getConfigProperty("com.labvantage.sapphire.server.emailfromaddress")) == null || emailfromaddress.length() == 0) {
                warningList.add("Config property com.labvantage.sapphire.server.emailfromaddress is not defined - this will prevent Mail actions and alerts from working");
            }
            if ((securityemailtoaddress = ConfigService.getConfigProperty("com.labvantage.sapphire.server.securityemailtoaddress")) == null || securityemailtoaddress.length() == 0) {
                warningList.add("Config property com.labvantage.sapphire.server.securityemailtoaddress is not defined - this disables security violation email alerts");
            }
        }
        catch (Exception e) {
            throw new SapphireException("CONFIGURATION_ERROR", "Failed to get configuration properties whilst checking warnings", e);
        }
    }

    public void getDatabaseWarnings(String databaseid, ArrayList warningList) throws SapphireException {
    }

    public void checkAdmindb(String jndiname, String dbms) throws SapphireException {
        if (jndiname == null || jndiname.length() == 0) {
            throw new SapphireException("Admin database JNDI name property (com.labvantage.sapphire.server.admindb) not defined in sapphireconfig.props");
        }
        if (dbms == null || dbms.length() == 0) {
            throw new SapphireException("Admin database DBMS property (com.labvantage.sapphire.server.admindbms) not defined in sapphireconfig.props");
        }
        DBUtil adb = new DBUtil();
        try {
            String[] rsakeys;
            adb.setConnection(dbms, ServiceLocator.getInstance().getDataSource(jndiname).getConnection());
            try {
                adb.createResultSet("SELECT * FROM adminconfig WHERE propertyid = 'build'");
                if (adb.getNext()) {
                    this.admindbBuild = adb.getString("propertyvalue");
                }
            }
            catch (SapphireException se) {
                state = 3;
                throw new SapphireException("Admin database has not been installed", se);
            }
            if (ConfigService.getConfigProperty("com.labvantage.sapphire.server.ignorebuild", "false").equals("false")) {
                if (this.admindbBuild == null || this.admindbBuild.length() == 0 || !this.admindbBuild.equals(Build.getBuild())) {
                    state = 4;
                    throw new SapphireException("Admin database version (" + this.admindbBuild + ") is incorrect, expected " + Build.getBuild());
                }
            } else {
                Trace.log("STARTUP", "** IGNORING Admin database build  - NOT TO BE USED IN A PRODUCTION ENVIRONMENT");
            }
            adb.createResultSet("SELECT propertyid, propertyvalue FROM adminconfig WHERE propertyid in ( 'labvantageinternalkey', 'labvantageinternalrsa1', 'labvantageinternalrsa2', 'labvantageinternalrsa3' ) ");
            HashMap<String, String> keyMap = new HashMap<String, String>();
            while (adb.getNext()) {
                keyMap.put(adb.getString("propertyid"), adb.getString("propertyvalue"));
            }
            String labvantageinternalkey = (String)keyMap.get("labvantageinternalkey");
            if (keyMap.get("labvantageinternalkey") == null) {
                labvantageinternalkey = EncryptDecrypt.generateRandomAESKey();
                adb.executePreparedUpdate("INSERT INTO adminconfig ( propertyid, propertyvalue ) values( ?, ?)", new Object[]{"labvantageinternalkey", labvantageinternalkey});
                EncryptDecrypt.setLVInternalKey("admindb", labvantageinternalkey);
                adb.createResultSet("SELECT databaselistid, dbconnectionkey FROM databaselist");
                while (adb.getNext()) {
                    String databaseid = adb.getString("databaselistid");
                    String dbconnectionkey = adb.getString("dbconnectionkey");
                    String newdbconnectionkey = EncryptDecrypt.encrypt(EncryptDecrypt.decrypt(dbconnectionkey), "admindb");
                    adb.executePreparedUpdate("UPDATE databaselist SET dbconnectionkey= ? WHERE databaselistid=?", new Object[]{newdbconnectionkey, databaseid});
                }
            } else {
                EncryptDecrypt.setLVInternalKey("admindb", labvantageinternalkey);
            }
            if ((rsakeys = new String[]{(String)keyMap.get("labvantageinternalrsa1"), (String)keyMap.get("labvantageinternalrsa2"), (String)keyMap.get("labvantageinternalrsa3")})[0] == null) {
                rsakeys = EncryptDecrypt.generateNewKeys(null);
                adb.executePreparedUpdate("INSERT INTO adminconfig ( propertyid, propertyvalue ) values( ?, ? )", new Object[]{"labvantageinternalrsa1", rsakeys[0]});
                adb.executePreparedUpdate("INSERT INTO adminconfig ( propertyid, propertyvalue ) values( ?, ? )", new Object[]{"labvantageinternalrsa2", rsakeys[1]});
                adb.executePreparedUpdate("INSERT INTO adminconfig ( propertyid, propertyvalue ) values( ?, ? )", new Object[]{"labvantageinternalrsa3", rsakeys[2]});
            }
            EncryptDecrypt.setRSAKeys(rsakeys);
        }
        catch (Exception e) {
            throw new SapphireException("Admin database startup check failed. Reason: " + e.getMessage(), e);
        }
        finally {
            adb.reset();
            adb.releaseConnection();
        }
    }

    public abstract String getHttpPort() throws SapphireException;

    public abstract String getDefaultProviderPort();

    public abstract String getProviderPort();

    public File getSecurityManagerFile() throws SapphireException {
        File file = new File(this.labvantageHome + this.appDir + "/securitymanager_" + this.getServerHostName().toLowerCase() + "_" + this.getServerid().toLowerCase() + ".json");
        if (!file.exists()) {
            file = new File(this.labvantageHome + this.appDir + "/securitymanager_" + this.getServerHostName().toLowerCase() + ".json");
        }
        if (!file.exists()) {
            file = new File(this.labvantageHome + this.appDir + "/securitymanager_" + this.getServerid().toLowerCase() + ".json");
        }
        if (!file.exists()) {
            file = new File(this.labvantageHome + this.appDir + "/securitymanager.json" + Configuration.getPlatformName(this.platform).toLowerCase());
        }
        if (!file.exists()) {
            file = new File(this.labvantageHome + this.appDir + "/securitymanager.json");
        }
        return file;
    }

    public File getConfigFile() throws SapphireException {
        File file = new File(this.labvantageHome + this.appDir + "/labvantageconfig_" + this.getServerHostName().toLowerCase() + "_" + this.getServerid().toLowerCase() + ".props");
        if (!file.exists()) {
            file = new File(this.labvantageHome + this.appDir + "/sapphireconfig_" + this.getServerHostName().toLowerCase() + "_" + this.getServerid().toLowerCase() + ".props");
        }
        if (!file.exists() && !(file = new File(this.labvantageHome + this.appDir + "/labvantageconfig_" + this.getServerHostName().toLowerCase() + ".props")).exists()) {
            file = new File(this.labvantageHome + this.appDir + "/sapphireconfig_" + this.getServerHostName().toLowerCase() + ".props");
        }
        if (!file.exists() && !(file = new File(this.labvantageHome + this.appDir + "/labvantageconfig_" + this.getServerid().toLowerCase() + ".props")).exists()) {
            file = new File(this.labvantageHome + this.appDir + "/sapphireconfig_" + this.getServerid().toLowerCase() + ".props");
        }
        if (!file.exists() && !(file = new File(this.labvantageHome + this.appDir + "/labvantageconfig.props_" + Configuration.getPlatformName(this.platform).toLowerCase())).exists()) {
            file = new File(this.labvantageHome + this.appDir + "/sapphireconfig.props_" + Configuration.getPlatformName(this.platform).toLowerCase());
        }
        if (!file.exists() && !(file = new File(this.labvantageHome + this.appDir + "/labvantageconfig.props")).exists()) {
            file = new File(this.labvantageHome + this.appDir + "/sapphireconfig.props");
        }
        return file;
    }

    public File getServerLicenseFile() throws SapphireException {
        File licenseFile = new File(this.labvantageHome + "/labvantage.lic");
        return licenseFile.exists() ? licenseFile : new File(this.labvantageHome + "/sapphire.lic");
    }

    public File getLogConfigFile() throws SapphireException {
        File logConfigFile = new File(this.labvantageHome + this.appDir + "/" + "labvantagelogging" + ".properties");
        if (!logConfigFile.exists()) {
            PropertyList props = new PropertyList();
            LogConfig.generateConfigFile(this.applicationid, logConfigFile, "", props, false);
        }
        return logConfigFile;
    }

    public File getServerLogConfigFile() throws SapphireException {
        LogConfig logConfig = new LogConfig(this.getApplicationid(), this.getLogConfigFile());
        Trace.setIsLogByDatabase(logConfig.isSplitByDatabase());
        Trace.setSplitDatabaseList(logConfig.getSplitByDatabaseList());
        Trace.setIsDiagnosticLoggingEnabled(logConfig.isDiagnosticLoggingEnabled());
        Trace.setDiagnosticLoggingType(logConfig.getDiagnosticLoggingType());
        String serverid = this.getServerid();
        if (serverid.length() > 0) {
            File logFile = new File(this.labvantageHome + this.appDir + "/" + "labvantagelogging" + "_" + serverid + ".properties");
            if (logConfig.isManaged() && !logFile.exists()) {
                PropertyList props = new PropertyList();
                props.putAll(logConfig.getLogProperties());
                props.put("sapphirelogtype", logConfig.getSapphireLogType().equals("DailyRollingFileAppender") ? "daily" : (logConfig.getSapphireLogType().equals("RollingFileAppender") ? "rolling" : "continuous"));
                props.put("errorlogtype", logConfig.getErrorLogType().equals("DailyRollingFileAppender") ? "daily" : (logConfig.getErrorLogType().equals("RollingFileAppender") ? "rolling" : "continuous"));
                LogConfig.generateConfigFile(this.applicationid, logFile, serverid, props, false);
                return logFile;
            }
            if (logFile.exists()) {
                return logFile;
            }
            throw new SapphireException("Failed to find logging props file (" + logFile.getAbsolutePath() + ") for server " + serverid + ". Switch to managed log files to enabled auto generation of the server log files.");
        }
        return this.getLogConfigFile();
    }

    public abstract String getInitialContextFactory();

    public abstract String getProviderURL() throws SapphireException;

    public abstract void checkPlatformConfiguration(ArrayList var1) throws SapphireException;

    public abstract void applyPatches(ArrayList var1) throws SapphireException;

    public abstract String getAppRoot(ServletContext var1);

    public abstract String getWebAppRoot(ServletContext var1);

    public abstract String getJNDIDataSourcePrefix();

    public void setJndiEJBPrefix(String jndiEJBPrefix) {
        this.jndiEJBPrefix = jndiEJBPrefix;
    }

    public String getJNDIEJBPrefix() {
        return this.jndiEJBPrefix;
    }

    public abstract String getJNDILocalEJBPrefix();

    public String getJCEProvider() {
        String jceProvider = "SunJCE";
        Properties prop = System.getProperties();
        if (prop.getProperty("java.vendor").equals("IBM Corporation")) {
            jceProvider = "IBMJCE";
        }
        return jceProvider;
    }

    public abstract String getServerVersion();

    public boolean executePlatformAction(PropertyList actionProps) {
        return true;
    }

    public PropertyList getPlatformProperties() {
        return new PropertyList();
    }

    public boolean isAutomationServer() {
        boolean automationServer = this.isAutomationServer(DEFAULT_ALLDATABASES);
        return automationServer;
    }

    public boolean isAutomationServer(String databaseid) {
        String isAutomationServer = Configuration.isAutomationServer.get(databaseid);
        if (isAutomationServer == null) {
            isAutomationServer = Configuration.isAutomationServer.get(DEFAULT_ALLDATABASES);
        }
        return Configuration.getPropertyValueAsBoolean(isAutomationServer, true);
    }

    public void setAutomationServer(String automationServer) {
        isAutomationServer.put(DEFAULT_ALLDATABASES, automationServer);
    }

    static {
        configuration = null;
        junitServer = false;
    }
}

