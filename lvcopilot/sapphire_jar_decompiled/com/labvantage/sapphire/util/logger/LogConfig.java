/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.Level
 *  org.apache.logging.log4j.core.config.Configurator
 */
package com.labvantage.sapphire.util.logger;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.platform.Configuration;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import sapphire.SapphireException;
import sapphire.util.Logger;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LogConfig {
    public static final String DEFAULT_LABVANTAGELOGGING = "labvantagelogging";
    public static final String DEFAULT_ROOTLOGGER_LEVEL = "INFO";
    public static final String DEFAULT_LOGGER_LEVEL = "INFO";
    public static final String DEFAULT_SAPPHIRELOG_MAXFILESIZE = "200MB";
    public static final String DEFAULT_SAPPHIRELOG_MAXBACKUPINDEX = "100";
    public static final String DEFAULT_SAPPHIRELOG_DATEPATTERN = "yyyy-MM-dd";
    public static final String DEFAULT_ERRORLOG_MAXFILESIZE = "10MB";
    public static final String DEFAULT_ERRORLOG_MAXBACKUPINDEX = "10";
    public static final String DEFAULT_ERRORLOG_DATEPATTERN = "yyyy-MM-dd";
    public static final String DEFAULT_STARTUPLOG_MAXBACKUPINDEX = "0";
    public static final String DEFAULT_LABVANTAGELOG = "labvantage";
    public static final String DEFAULT_LABVANTAGELOG_NAME = "labvantage.log";
    public static final String DEFAULT_ERRORLOG = "labvantage_errors";
    public static final String DEFAULT_ERRORLOG_NAME = "labvantage_errors.log";
    public static final String DEFAULT_STARTUPLOG = "labvantage_startup";
    public static final String DEFAULT_STARTUPLOG_NAME = "labvantage_startup.log";
    public static final String ROOTLOGGER_LEVEL = "INFO";
    public static final String SAPPHIRELOG = "sapphirelog";
    public static final String SAPPHIRELOG_MAXFILESIZE_LOG4J1 = "log4j.appender.sapphire.MaxFileSize";
    public static final String SAPPHIRELOG_MAXBACKUPINDEX_LOG4J1 = "log4j.appender.sapphire.MaxBackupIndex";
    public static final String SAPPHIRELOG_DATEPATTERN_LOG4J1 = "log4j.appender.sapphire.DatePattern";
    public static final String SAPPHIRELOG_MAXFILESIZE = "appender.sapphire.policies.size.size";
    public static final String SAPPHIRELOG_MAXBACKUPINDEX = "appender.sapphire.strategy.max";
    public static final String SAPPHIRELOG_DATEPATTERN = "custom.sapphire.datePattern";
    public static final String SAPPHIRELOG_LEVEL = "logger.sapphire.level";
    public static final String ERRORLOG = "errorlog";
    public static final String ERRORLOG_MAXFILESIZE_LOG4J1 = "log4j.appender.errors.MaxFileSize";
    public static final String ERRORLOG_MAXBACKUPINDEX_LOG4J1 = "log4j.appender.errors.MaxBackupIndex";
    public static final String ERRORLOG_DATEPATTERN_LOG4J1 = "log4j.appender.errors.DatePattern";
    public static final String ERRORLOG_MAXFILESIZE = "appender.errors.policies.size.size";
    public static final String ERRORLOG_MAXBACKUPINDEX = "appender.errors.strategy.max";
    public static final String ERRORLOG_DATEPATTERN = "custom.errors.datePattern";
    public static final String ERRORLOG_LEVEL = "logger.errors.level";
    public static final String STARTUPLOG = "startuplog";
    public static final String STARTUPLOG_MAXBACKUPINDEX = "sapphire.logging.startup.maxbackupindex";
    public static final String STARTUPLOG_LEVEL = "logger.sapphirestartup.level";
    public static final String TYPE_ROLLING = "RollingFileAppender";
    public static final String TYPE_DAILY = "DailyRollingFileAppender";
    public static final String SAPPHIRELOG_APPENDER_TYPE = "appender.sapphire.type";
    public static final String SAPPHIRELOG_APPENDER_NAME = "appender.sapphire.name";
    public static final String CURRENT_LOGFILE_REVISION = "8.8 Rev 1";
    public static final String DEFAULT_PATTERN = "%-23d %-5p [#%X{threadid}] %-30X{logcontext} : %m%n";
    public static final String ERROR_PATTERN = "%-23d %-5p [#%X{threadid}] [%X{databaseid}] %-30X{logcontext} : %m%n";
    private Properties props = new Properties();
    private String rootLoggerLevel;
    private String sapphireLogger;
    private String errorLogger;
    private String startupLogger;

    public LogConfig(String applicationid, File configFile) throws SapphireException {
        FileInputStream fis = null;
        try {
            String datePattern;
            fis = new FileInputStream(configFile);
            this.props.load(fis);
            this.rootLoggerLevel = this.props.getProperty(SAPPHIRELOG_LEVEL, "INFO");
            this.sapphireLogger = this.props.getProperty(SAPPHIRELOG_LEVEL, "INFO").equalsIgnoreCase("OFF") ? "N" : "Y";
            this.errorLogger = this.props.getProperty(ERRORLOG_LEVEL, "OFF").equalsIgnoreCase("OFF") ? "N" : "Y";
            String string = this.startupLogger = this.props.getProperty(STARTUPLOG_LEVEL, "OFF").equalsIgnoreCase("OFF") ? "N" : "Y";
            if (this.props.containsKey("appender.sapphire.filePattern") && (datePattern = this.props.getProperty("appender.sapphire.filePattern")).contains("{") && datePattern.contains("}") && datePattern.indexOf("{") < datePattern.indexOf("}")) {
                datePattern = datePattern.substring(datePattern.indexOf("{") + 1, datePattern.indexOf("}")).trim();
                this.props.setProperty(SAPPHIRELOG_DATEPATTERN, datePattern);
            }
            if (this.props.containsKey("appender.errors.filePattern") && (datePattern = this.props.getProperty("appender.errors.filePattern")).contains("{") && datePattern.contains("}") && datePattern.indexOf("{") < datePattern.indexOf("}")) {
                datePattern = datePattern.substring(datePattern.indexOf("{") + 1, datePattern.indexOf("}")).trim();
                this.props.setProperty(ERRORLOG_DATEPATTERN, datePattern);
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to load config file. Reason: " + e.getMessage(), e);
        }
        finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            }
            catch (IOException e) {
                throw new SapphireException("Failed to close input stream. Reason: " + e.getMessage(), e);
            }
        }
    }

    public String getRootLoggerLevel() {
        return this.rootLoggerLevel != null && this.rootLoggerLevel.length() > 0 ? this.rootLoggerLevel : Level.INFO.toString();
    }

    public boolean isSapphireLogEnabled() {
        return this.sapphireLogger != null && this.sapphireLogger.length() > 0 && this.sapphireLogger.trim().equals("Y");
    }

    public boolean isErrorLogEnabled() {
        return this.errorLogger != null && this.errorLogger.length() > 0 && this.errorLogger.trim().equals("Y");
    }

    public boolean isStartupLogEnabled() {
        return this.startupLogger != null && this.startupLogger.length() > 0 && this.startupLogger.trim().equals("Y");
    }

    public boolean isManaged() {
        return this.props.size() == 0 || this.props.getProperty("sapphire.logging.managed", "false").equalsIgnoreCase("true");
    }

    public boolean isSplitByDatabase() {
        return this.props.getProperty("sapphire.logging.splitbydatabase", "false").equalsIgnoreCase("true");
    }

    public String getSplitByDatabaseList() {
        return this.props.getProperty("sapphire.logging.splitbydatabaselist");
    }

    public boolean isDiagnosticLoggingEnabled() {
        return this.props.getProperty("sapphire.logging.diagnosticlogging", "false").equalsIgnoreCase("true");
    }

    public String getDiagnosticLoggingType() {
        return this.props.getProperty("sapphire.logging.diagnosticloggingtype", "");
    }

    public String getSapphireLogType() {
        if (this.props.getProperty("appender.sapphire.policies.time.type", "").equalsIgnoreCase("TimeBasedTriggeringPolicy")) {
            return TYPE_DAILY;
        }
        return TYPE_ROLLING;
    }

    public String getErrorLogType() {
        if (this.props.getProperty("appender.errors.policies.time.type", "").equalsIgnoreCase("TimeBasedTriggeringPolicy")) {
            return TYPE_DAILY;
        }
        return TYPE_ROLLING;
    }

    public String getLogProperty(String property, String defaultValue) {
        return this.props.getProperty(property, defaultValue);
    }

    public Properties getLogProperties() {
        return this.props;
    }

    public static void generateConfigFile(PropertyList props) throws SapphireException {
        LogConfig.generateConfigFile(Configuration.getInstance().getApplicationid(), Configuration.getInstance().getLogConfigFile(), "", props, true, Configuration.getInstance());
    }

    public static void generateConfigFile(String applicationid, File configFile, String serverid, PropertyList props, boolean configure) throws SapphireException {
        LogConfig.generateConfigFile(applicationid, configFile, serverid, props, configure, Configuration.getInstance());
    }

    public static void generateConfigFile(String applicationid, File configFile, String serverid, PropertyList props, boolean configure, Configuration configuration) throws SapphireException {
        try {
            String readline;
            boolean managed = props.getProperty("configtype", "managed").equals("managed");
            String string = serverid = serverid == null ? "" : serverid;
            if (managed) {
                File logsDir = new File(configFile.getParentFile().getAbsolutePath() + "/logs");
                logsDir.mkdirs();
                Calendar cal = DateTimeUtil.getNowCalendar();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                boolean splitByDatabase = props.getProperty("sapphire.logging.splitbydatabase").equalsIgnoreCase("true");
                String splitByDatabaseList = props.getProperty("sapphire.logging.splitbydatabaselist");
                boolean isDiagnosticLogging = props.getProperty("sapphire.logging.diagnosticlogging").equalsIgnoreCase("true");
                String diagnosticLoggingType = props.getProperty("sapphire.logging.diagnosticloggingtype");
                Trace.setIsLogByDatabase(splitByDatabase);
                Trace.setSplitDatabaseList(splitByDatabaseList);
                Trace.setIsDiagnosticLoggingEnabled(isDiagnosticLogging);
                Trace.setDiagnosticLoggingType(diagnosticLoggingType);
                ArrayList<String> databaseloggers = new ArrayList<String>();
                databaseloggers.add("sapphire");
                if (splitByDatabase && splitByDatabaseList.length() > 0) {
                    String[] temp = StringUtil.split(splitByDatabaseList, ",");
                    for (int i = 0; i < temp.length; ++i) {
                        String databaseid = temp[i].trim();
                        if (databaseid.length() <= 0) continue;
                        databaseloggers.add(databaseid.equalsIgnoreCase("auto") ? "database_auto" : databaseid);
                    }
                    splitByDatabaseList = "";
                    for (String databaseid : databaseloggers) {
                        if (databaseid.equals("sapphire")) continue;
                        splitByDatabaseList = splitByDatabaseList + ", " + databaseid;
                    }
                    splitByDatabaseList = splitByDatabaseList.length() > 0 ? splitByDatabaseList.substring(1) : "";
                }
                FileOutputStream fos = new FileOutputStream(configFile);
                Object object = null;
                try {
                    PrintStream out = new PrintStream(fos);
                    out.println("#LabVantage Logging (Log4J2) Properties");
                    out.println("#Generated: " + sdf.format(cal.getTime()));
                    out.println("sapphire.logging.managed=true");
                    out.println("sapphire.logging.splitbydatabase=" + splitByDatabase);
                    out.println("sapphire.logging.splitbydatabaselist=" + splitByDatabaseList);
                    out.println("sapphire.logging.diagnosticlogging=" + isDiagnosticLogging);
                    out.println("sapphire.logging.diagnosticloggingtype=" + diagnosticLoggingType);
                    out.println("sapphire.logging.revision=8.8 Rev 1");
                    out.println("sapphire.logging.startup.maxbackupindex=" + props.getProperty(STARTUPLOG_MAXBACKUPINDEX, DEFAULT_STARTUPLOG_MAXBACKUPINDEX));
                    out.println("");
                    for (String loggerName : databaseloggers) {
                        LogConfig.writeLoggerBody(applicationid, serverid, props, out, loggerName, false, configuration);
                        LogConfig.writeErrorLoggerBody(applicationid, serverid, props, out, loggerName, configuration);
                        if (!isDiagnosticLogging) continue;
                        LogConfig.writeLoggerBody(applicationid, serverid, props, out, loggerName + "_diag", true, configuration);
                    }
                    if (props.getProperty(STARTUPLOG, "Y").equals("Y")) {
                        out.println("appender.sapphirestartup.type=File");
                        out.println("appender.sapphirestartup.name=startup_file_appender");
                        out.println("appender.sapphirestartup.fileName=" + LogConfig.escapeProperties(configuration.getSapphireHome() + File.separator + "applications" + File.separator + applicationid + File.separator + "logs" + File.separator + (serverid.length() > 0 ? "labvantage_startup_" + serverid + ".log" : DEFAULT_STARTUPLOG_NAME)));
                        out.println("appender.sapphirestartup.layout.type=PatternLayout");
                        out.println("appender.sapphirestartup.layout.pattern=%-23d %-5p [#%X{threadid}] %-30X{logcontext} : %m%n");
                        out.println("appender.sapphirestartup.append=false");
                        out.println("");
                        out.println("logger.sapphirestartup.name=" + applicationid + ".STARTUP");
                        out.println("logger.sapphirestartup.level=" + (props.getProperty(STARTUPLOG, "Y").equalsIgnoreCase("N") ? "OFF" : "INFO"));
                        out.println("logger.sapphirestartup.additivity=false");
                        out.println("logger.sapphirestartup.appenderRef.sapphirestartup.ref=startup_file_appender");
                        out.println("");
                    }
                    out.close();
                }
                catch (Throwable throwable) {
                    object = throwable;
                    throw throwable;
                }
                finally {
                    if (fos != null) {
                        if (object != null) {
                            try {
                                fos.close();
                            }
                            catch (Throwable throwable) {
                                ((Throwable)object).addSuppressed(throwable);
                            }
                        } else {
                            fos.close();
                        }
                    }
                }
            }
            ArrayList<String> propsList = new ArrayList<String>();
            BufferedReader reader = new BufferedReader(new FileReader(configFile));
            while ((readline = reader.readLine()) != null) {
                String[] lineparts = StringUtil.split(readline, "=");
                if (lineparts.length == 2 && lineparts[0].equalsIgnoreCase("sapphire.logging.managed")) {
                    readline = "sapphire.logging.managed=false";
                }
                propsList.add(readline);
            }
            reader.close();
            try (FileOutputStream fos = new FileOutputStream(configFile);){
                PrintStream out = new PrintStream(fos);
                for (int i = 0; i < propsList.size(); ++i) {
                    out.println((String)propsList.get(i));
                }
                out.close();
            }
            if (configure) {
                LogConfig.configure(configFile);
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to generate new log config file. Reason: " + e.getMessage(), e);
        }
    }

    private static void writeLoggerBody(String applicationid, String serverid, PropertyList props, PrintStream out, String newLoggerName, boolean isDiagnostic, Configuration configuration) throws SapphireException {
        String sapphirelogtype = props.getProperty("sapphirelogtype", "rolling");
        String logFileName = LogConfig.escapeProperties(configuration.getSapphireHome() + File.separator + "applications" + File.separator + applicationid + File.separator + "logs" + File.separator + DEFAULT_LABVANTAGELOG + (newLoggerName.equals("sapphire") ? "" : (newLoggerName.equals("sapphire_diag") ? ".diag" : "." + StringUtil.replaceAll(newLoggerName, "_", "."))) + (serverid != null && serverid.length() > 0 ? "." + serverid : "")) + ".log";
        if (sapphirelogtype.equals("rolling") || sapphirelogtype.equals("continuous")) {
            LogConfig.writeLoggerLine(out, newLoggerName, SAPPHIRELOG_APPENDER_TYPE, "RollingFile");
            LogConfig.writeLoggerLine(out, newLoggerName, SAPPHIRELOG_APPENDER_NAME, newLoggerName + "_appender");
            LogConfig.writeLoggerLine(out, newLoggerName, "appender.sapphire.policies.type", "Policies");
            LogConfig.writeLoggerLine(out, newLoggerName, "appender.sapphire.policies.size.type", "SizeBasedTriggeringPolicy");
            LogConfig.writeLoggerLine(out, newLoggerName, SAPPHIRELOG_MAXFILESIZE, props.getProperty(SAPPHIRELOG_MAXFILESIZE, DEFAULT_SAPPHIRELOG_MAXFILESIZE));
            LogConfig.writeLoggerLine(out, newLoggerName, "appender.sapphire.strategy.type", "DefaultRolloverStrategy");
            LogConfig.writeLoggerLine(out, newLoggerName, SAPPHIRELOG_MAXBACKUPINDEX, props.getProperty(SAPPHIRELOG_MAXBACKUPINDEX, DEFAULT_SAPPHIRELOG_MAXBACKUPINDEX));
            LogConfig.writeLoggerLine(out, newLoggerName, "appender.sapphire.filePattern", logFileName + ".%i");
        } else if (sapphirelogtype.equals("daily")) {
            LogConfig.writeLoggerLine(out, newLoggerName, SAPPHIRELOG_APPENDER_TYPE, "RollingFile");
            LogConfig.writeLoggerLine(out, newLoggerName, SAPPHIRELOG_APPENDER_NAME, newLoggerName + "_appender");
            LogConfig.writeLoggerLine(out, newLoggerName, "appender.sapphire.policies.type", "Policies");
            LogConfig.writeLoggerLine(out, newLoggerName, "appender.sapphire.policies.time.type", "TimeBasedTriggeringPolicy");
            LogConfig.writeLoggerLine(out, newLoggerName, "appender.sapphire.policies.time.interval", "1");
            LogConfig.writeLoggerLine(out, newLoggerName, "appender.sapphire.policies.time.modulate", "true");
            LogConfig.writeLoggerLine(out, newLoggerName, "appender.sapphire.filePattern", logFileName + ".%d{" + props.getProperty(SAPPHIRELOG_DATEPATTERN, "yyyy-MM-dd") + "}");
        }
        LogConfig.writeLoggerLine(out, newLoggerName, "appender.sapphire.fileName", logFileName);
        LogConfig.writeLoggerLine(out, newLoggerName, "appender.sapphire.layout.pattern", DEFAULT_PATTERN);
        LogConfig.writeLoggerLine(out, newLoggerName, "appender.sapphire.layout.type", "PatternLayout");
        out.println("");
        LogConfig.writeLoggerLine(out, newLoggerName, "logger.sapphire.name", applicationid + "." + newLoggerName);
        if (props.getProperty(SAPPHIRELOG, "Y").equalsIgnoreCase("Y")) {
            if (isDiagnostic) {
                LogConfig.writeLoggerLine(out, newLoggerName, SAPPHIRELOG_LEVEL, "DEBUG");
            } else if (props.getProperty("INFO", "INFO").equalsIgnoreCase("OFF")) {
                LogConfig.writeLoggerLine(out, newLoggerName, SAPPHIRELOG_LEVEL, "INFO");
            } else {
                LogConfig.writeLoggerLine(out, newLoggerName, SAPPHIRELOG_LEVEL, props.getProperty("INFO", "INFO"));
            }
        } else {
            LogConfig.writeLoggerLine(out, newLoggerName, SAPPHIRELOG_LEVEL, "OFF");
        }
        LogConfig.writeLoggerLine(out, newLoggerName, "logger.sapphire.additivity", "false");
        LogConfig.writeLoggerLine(out, newLoggerName, "logger.sapphire.appenderRef.sapphire.ref", newLoggerName + "_appender");
        out.println("");
    }

    private static void writeErrorLoggerBody(String applicationid, String serverid, PropertyList props, PrintStream out, String loggerName, Configuration configuration) throws SapphireException {
        if (props.getProperty(ERRORLOG, "Y").equals("Y") && props.getProperty(SAPPHIRELOG, "Y").equals("Y")) {
            String errorLoggerName;
            String errorlogtype = props.getProperty("errorlogtype", "rolling");
            String logFileName = LogConfig.escapeProperties(configuration.getSapphireHome() + File.separator + "applications" + File.separator + applicationid + File.separator + "logs" + File.separator + DEFAULT_LABVANTAGELOG + (loggerName.equals("sapphire") ? "" : (loggerName.equals("sapphire_diag") ? ".diag" : "." + StringUtil.replaceAll(loggerName, "_", "."))) + (serverid != null && serverid.length() > 0 ? "." + serverid : "")) + "_errors.log";
            String string = errorLoggerName = loggerName.equalsIgnoreCase("sapphire") ? "errors" : loggerName + "_errors";
            if (errorlogtype.equals("rolling") || errorlogtype.equals("continuous")) {
                LogConfig.writeErrorLoggerLine(out, errorLoggerName, "appender.errors.type", "RollingFile");
                LogConfig.writeErrorLoggerLine(out, errorLoggerName, "appender.errors.policies.type", "Policies");
                LogConfig.writeErrorLoggerLine(out, errorLoggerName, "appender.errors.policies.size.type", "SizeBasedTriggeringPolicy");
                LogConfig.writeErrorLoggerLine(out, errorLoggerName, ERRORLOG_MAXFILESIZE, props.getProperty(ERRORLOG_MAXFILESIZE, DEFAULT_ERRORLOG_MAXFILESIZE));
                LogConfig.writeErrorLoggerLine(out, errorLoggerName, "appender.errors.strategy.type", "DefaultRolloverStrategy");
                LogConfig.writeErrorLoggerLine(out, errorLoggerName, ERRORLOG_MAXBACKUPINDEX, props.getProperty(ERRORLOG_MAXBACKUPINDEX, DEFAULT_ERRORLOG_MAXBACKUPINDEX));
                LogConfig.writeErrorLoggerLine(out, errorLoggerName, "appender.errors.filePattern", logFileName + ".%i");
            } else if (errorlogtype.equals("daily")) {
                LogConfig.writeErrorLoggerLine(out, errorLoggerName, "appender.errors.type", "RollingFile");
                LogConfig.writeErrorLoggerLine(out, errorLoggerName, "appender.errors.policies.type", "Policies");
                LogConfig.writeErrorLoggerLine(out, errorLoggerName, "appender.errors.policies.time.type", "TimeBasedTriggeringPolicy");
                LogConfig.writeErrorLoggerLine(out, errorLoggerName, "appender.errors.policies.time.interval", "1");
                LogConfig.writeErrorLoggerLine(out, errorLoggerName, "appender.errors.policies.time.modulate", "true");
                LogConfig.writeErrorLoggerLine(out, errorLoggerName, "appender.errors.filePattern", logFileName + ".%d{" + props.getProperty(ERRORLOG_DATEPATTERN, "yyyy-MM-dd") + "}");
            }
            LogConfig.writeErrorLoggerLine(out, errorLoggerName, "appender.errors.name", errorLoggerName + "_appender");
            LogConfig.writeErrorLoggerLine(out, errorLoggerName, "appender.errors.fileName", logFileName);
            LogConfig.writeErrorLoggerLine(out, errorLoggerName, "appender.errors.layout.pattern", ERROR_PATTERN);
            LogConfig.writeErrorLoggerLine(out, errorLoggerName, "appender.errors.layout.type", "PatternLayout");
            LogConfig.writeErrorLoggerLine(out, errorLoggerName, "appender.errors.filter.threshold.type", "ThresholdFilter");
            LogConfig.writeErrorLoggerLine(out, errorLoggerName, "appender.errors.filter.threshold.level", "ERROR");
            out.println("");
            LogConfig.writeErrorLoggerLine(out, errorLoggerName, "logger.errors.name", applicationid + "." + loggerName + "_error");
            LogConfig.writeErrorLoggerLine(out, errorLoggerName, ERRORLOG_LEVEL, "ERROR");
            LogConfig.writeErrorLoggerLine(out, errorLoggerName, "logger.errors.additivity", "false");
            LogConfig.writeErrorLoggerLine(out, errorLoggerName, "logger." + loggerName + ".appenderRef.errors.ref", errorLoggerName + "_appender");
            out.println("");
        }
    }

    private static void writeLoggerLine(PrintStream out, String newLoggerName, String propertyid, String propertyvalue) {
        out.println(StringUtil.replaceAll(propertyid, ".sapphire", "." + newLoggerName) + "=" + propertyvalue);
    }

    private static void writeErrorLoggerLine(PrintStream out, String newLoggerName, String propertyid, String propertyvalue) {
        out.println(StringUtil.replaceAll(propertyid, ".errors", "." + newLoggerName) + "=" + propertyvalue);
    }

    public static void configure(File logConfig) throws IOException, SapphireException {
        Configuration configuration = Configuration.getInstance();
        Properties logProps = new Properties();
        FileInputStream fis = new FileInputStream(logConfig);
        logProps.load(fis);
        PropertyList originalProperties = new PropertyList();
        for (String string : logProps.keySet()) {
            String value = logProps.getProperty(string, "");
            originalProperties.setProperty(string, value);
            logProps.setProperty(string, StringUtil.replaceAll(StringUtil.replaceAll(value, "${labvantage_home}", configuration.getSapphireHome()), "${applicationid}", configuration.getApplicationid()));
        }
        if (!"true".equals(logProps.getProperty("sapphire.logging.managed")) || CURRENT_LOGFILE_REVISION.equals(logProps.getProperty("sapphire.logging.revision"))) {
            Configurator.reconfigure((URI)logConfig.toURI());
        } else {
            LogConfig.generateConfigFile(originalProperties);
            LogConfig.configure(logConfig);
        }
    }

    private static String escapeProperties(String value) {
        value = StringUtil.replaceAll(value, "\\", "\\\\");
        value = StringUtil.replaceAll(value, ":", "\\:");
        value = StringUtil.replaceAll(value, "=", "\\=");
        value = StringUtil.replaceAll(value, "#", "\\#");
        value = StringUtil.replaceAll(value, "!", "\\!");
        return value;
    }

    public static File backupStartupLog() {
        File tempStartuplogFile = null;
        try {
            File logsDir = new File(Configuration.getInstance().getLogConfigFile().getParentFile().getAbsolutePath() + "/logs");
            File startuplogFile = new File(logsDir + "/labvantage_startup.log");
            tempStartuplogFile = File.createTempFile("startuplogtemp", "log", logsDir);
            FileUtil.copyFile(startuplogFile, tempStartuplogFile);
        }
        catch (Exception e) {
            Logger.logError("Failed copy from startup log to temp log file. Reason: " + e.getMessage());
        }
        return tempStartuplogFile;
    }

    public static void restoreStartupLog(File tempStartuplogFile) {
        try {
            File logsDir = new File(Configuration.getInstance().getLogConfigFile().getParentFile().getAbsolutePath() + "/logs");
            File startuplogFile = new File(logsDir + "/labvantage_startup.log");
            FileUtil.copyFile(tempStartuplogFile, startuplogFile);
            tempStartuplogFile.delete();
        }
        catch (Exception e) {
            Logger.logError("Failed to copy from temp log to startup log file. Reason: " + e.getMessage());
        }
    }

    public static long startuplogFileSize() throws SapphireException {
        long size = 0L;
        try {
            size = FileUtil.fileSize(new File(Configuration.getInstance().getLogConfigFile().getParentFile().getAbsolutePath() + "/logs/labvantage_startup.log"));
        }
        catch (Exception e) {
            Logger.logError("Failed to determine file size. Reason: " + e.getMessage());
        }
        return size;
    }
}

