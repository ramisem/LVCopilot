/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.Level
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 *  org.apache.logging.log4j.ThreadContext
 */
package com.labvantage.sapphire;

import com.labvantage.sapphire.StatRecord;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.SecurityService;
import com.labvantage.sapphire.util.logger.LogConfig;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.LogContext;
import sapphire.util.SDIList;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class Trace {
    public static boolean on = true;
    public static boolean stats = false;
    public static boolean tracestats = false;
    public static boolean debugEnabled = false;
    private static final int MAXMSGLENGTH = 4000;
    private static HashMap<String, Long> starttimes = new HashMap();
    public static final int ACTION = 0;
    public static final int BUSINESSRULE = 1;
    public static final int SQL = 2;
    public static final int RSETS = 3;
    public static final int ACTIONBLOCK = 4;
    public static final int QUERY = 5;
    public static final int CODEBLOCK = 6;
    public static final int REQUESTCOMMAND = 7;
    public static final int CROSSSDICALCS = 8;
    public static final String NODATABASE = "";
    public static final String LOG_BROWSER = "Browser";
    public static final String LOG_SOAP = "SOAP";
    public static final String LOG_JAVAAPI = "JavaAPI";
    public static final String LOG_LABVANTAGECLASSLOADER = "LabVantageClassLoader";
    public static final String LOG_REST = "REST";
    public static final String LOG_SC = "SC";
    public static final String LOG_TODOLIST = "ToDoList";
    public static final String LOG_SDMSCOLLECTOR = "SDMSCollector";
    public static final String LOG_GENERALAUTOMATION = "GeneralAutomation";
    public static final String LOG_SCHEDULING = "Scheduling";
    public static final String LOG_INDEXING = "Indexing";
    public static final String LOG_USER = "User";
    private static String[] typetext = new String[]{"Action", "Business Rules", "SQL", "RSets", "ActionBlock", "Queries", "Code Block", "JSP Request", "Auto Cross SDI Calculations"};
    private static HashMap<String, StatRecord>[] statstore = new HashMap[9];
    private static HashSet<String> noKeepAliveThreads;
    private static String loggerPrefix;
    private static boolean isLogByDatabase;
    private static Set<String> splitByDatabaseList;
    private static boolean isDiagnosticLoggingEnabled;
    private static String diagnosticLoggingType;
    private static String lastDiagnosticLoggingType;
    private static LogContext blankLogContext;

    public static String typeCodeToFlag(int type) {
        return type == 0 ? "A" : (type == 1 ? "L" : (type == 2 ? "S" : (type == 3 ? "R" : (type == 4 ? "B" : (type == 5 ? "Q" : (type == 6 ? "C" : (type == 8 ? "D" : "U")))))));
    }

    public static void setLoggerPrefix(String loggerPrefix) {
        Trace.loggerPrefix = loggerPrefix + ".";
    }

    public static boolean isDebugEnabled() {
        return debugEnabled;
    }

    public static void setIsLogByDatabase(boolean isLogByDatabase) {
        Trace.isLogByDatabase = isLogByDatabase;
    }

    public static void setIsDiagnosticLoggingEnabled(boolean isDiagnosticLoggingEnabled) {
        Trace.isDiagnosticLoggingEnabled = isDiagnosticLoggingEnabled;
    }

    public static void setDiagnosticLoggingType(String diagnosticLoggingType) {
        Trace.diagnosticLoggingType = diagnosticLoggingType;
    }

    public static void startThreadMDCBlank(String mode) {
        Trace.clearThreadMDC();
        ThreadContext.put((String)"threadid", (String)String.valueOf(Thread.currentThread().getId()));
        ThreadContext.put((String)"databaseid", (String)NODATABASE);
        ThreadContext.put((String)"mode", (String)mode);
    }

    public static void startThreadMDCByDatabaseid(String databaseid, String mode) {
        Trace.clearThreadMDC();
        ThreadContext.put((String)"threadid", (String)String.valueOf(Thread.currentThread().getId()));
        ThreadContext.put((String)"databaseid", (String)(databaseid == null || databaseid.length() == 0 ? NODATABASE : databaseid));
        ThreadContext.put((String)"mode", (String)mode);
    }

    public static void startThreadMDCByConnectionid(String connectionid, String mode) {
        Trace.clearThreadMDC();
        ThreadContext.put((String)"threadid", (String)String.valueOf(Thread.currentThread().getId()));
        ThreadContext.put((String)"mode", (String)mode);
        Trace.setThreadMDCConnectionid(connectionid);
    }

    public static void setThreadMDCConnectionid(String connectionid) {
        ThreadContext.put((String)"threadid", (String)String.valueOf(Thread.currentThread().getId()));
        ThreadContext.put((String)"databaseid", (String)NODATABASE);
        if (connectionid != null && connectionid.length() > 0) {
            ThreadContext.put((String)"connectionid", (String)connectionid);
            ThreadContext.put((String)"databaseid", (String)SecurityService.getDatabaseId(connectionid));
        }
    }

    public static void clearThreadMDC() {
        ThreadContext.remove((String)"databaseid");
        ThreadContext.remove((String)"connectionid");
        ThreadContext.remove((String)"mode");
        ThreadContext.remove((String)"threadid");
        ThreadContext.remove((String)"logcontext");
    }

    public static void logError(String message) {
        Trace.logError(NODATABASE, (Object)message, blankLogContext);
    }

    public static void logError(String message, LogContext logContext) {
        Trace.logError(NODATABASE, (Object)message, logContext);
    }

    public static void logError(String message, Throwable t) {
        Trace.logError(NODATABASE, message, t, blankLogContext);
    }

    public static void logError(String message, Throwable t, LogContext logContext) {
        Trace.logError(NODATABASE, message, t, logContext);
    }

    public static void logError(String header, Object message, Throwable t) {
        Trace.logError(header, message, t, blankLogContext);
    }

    public static void logError(String header, Object message) {
        Trace.logError(header, message, blankLogContext);
    }

    public static void logError(String header, Object message, LogContext logContext) {
        ThreadContext.put((String)"threadid", (String)String.valueOf(Thread.currentThread().getId()));
        ThreadContext.put((String)"logcontext", (String)(header != null && header.length() > 0 ? header : (logContext != null ? logContext.getLoggerName() : NODATABASE)));
        Logger[] logger = Trace.getLogger();
        logger[0].error(message);
        if (logger[1] != null) {
            logger[1].error(message);
        }
    }

    public static void logError(String header, Object message, Throwable t, LogContext logContext) {
        ThreadContext.put((String)"threadid", (String)String.valueOf(Thread.currentThread().getId()));
        ThreadContext.put((String)"logcontext", (String)(header != null && header.length() > 0 ? header : (logContext != null ? logContext.getLoggerName() : NODATABASE)));
        Logger[] logger = Trace.getLogger();
        logger[0].error(message, t);
        if (logger[1] != null) {
            logger[1].error(message, t);
        }
    }

    public static void logWarn(String message) {
        Trace.logWarn(NODATABASE, message, blankLogContext);
    }

    public static void logWarn(String message, LogContext logContext) {
        Trace.logWarn(NODATABASE, message, logContext);
    }

    public static void logWarn(String header, Object message) {
        Trace.logWarn(header, message, blankLogContext);
    }

    public static void logWarn(String header, Object message, LogContext logContext) {
        ThreadContext.put((String)"threadid", (String)String.valueOf(Thread.currentThread().getId()));
        ThreadContext.put((String)"logcontext", (String)(logContext != null ? (logContext.getLoggerName() == null || logContext.getLoggerName().length() == 0 ? header : logContext.getLoggerName()) : header));
        Logger[] logger = Trace.getLogger();
        logger[0].warn(message);
        if (logger[1] != null) {
            logger[1].warn(message);
        }
    }

    public static void logInfo(String message) {
        Trace.logInfo(NODATABASE, message, blankLogContext);
    }

    public static void logInfo(String message, LogContext logContext) {
        Trace.logInfo(NODATABASE, message, logContext);
    }

    public static void logInfo(String header, Object message) {
        Trace.logInfo(header, message, blankLogContext);
    }

    public static void logInfo(String header, Object message, LogContext logContext) {
        ThreadContext.put((String)"threadid", (String)String.valueOf(Thread.currentThread().getId()));
        ThreadContext.put((String)"logcontext", (String)(logContext != null ? (logContext.getLoggerName() == null || logContext.getLoggerName().length() == 0 ? header : logContext.getLoggerName()) : header));
        Logger[] logger = Trace.getLogger();
        logger[0].info(message);
        if (logger[1] != null) {
            logger[1].info(message);
        }
    }

    public static void logDebug(Object message) {
        Trace.logDebug(NODATABASE, message);
    }

    public static void logDebug(Object message, LogContext logContext) {
        Trace.logDebug(NODATABASE, message, logContext);
    }

    public static void logDebug(String header, Object message) {
        Trace.logDebug(header, message, blankLogContext);
    }

    public static void logDebug(String header, Object message, LogContext logContext) {
        ThreadContext.put((String)"threadid", (String)String.valueOf(Thread.currentThread().getId()));
        ThreadContext.put((String)"logcontext", (String)(logContext != null ? (logContext.getLoggerName() == null || logContext.getLoggerName().length() == 0 ? header : logContext.getLoggerName()) : header));
        Logger[] logger = Trace.getLogger();
        logger[0].debug(message);
        if (logger[1] != null) {
            logger[1].debug(message);
        }
    }

    public static void logStartup(String message) {
        Trace.logStartup(null, message);
    }

    public static void logStartup(String targetDatabase, String message) {
        ThreadContext.put((String)"threadid", (String)String.valueOf(Thread.currentThread().getId()));
        ThreadContext.put((String)"logcontext", (String)"STARTUP");
        LogManager.getLogger((String)(loggerPrefix + "sapphire")).log(Level.INFO, message);
        LogManager.getLogger((String)(loggerPrefix + "STARTUP")).log(Level.INFO, message);
        for (String databaseid : splitByDatabaseList) {
            LogManager.getLogger((String)(loggerPrefix + databaseid)).log(Level.INFO, message);
        }
    }

    private static Logger[] getLogger() {
        String mode = ThreadContext.get((String)"mode");
        String databaseid = ThreadContext.get((String)"databaseid");
        String loggerName = mode == null || !isLogByDatabase || !Trace.isDBConfiguredAsSplitLog(databaseid) || databaseid == null || databaseid.equals(NODATABASE) ? "sapphire" : databaseid;
        Logger mainLogger = LogManager.getLogger((String)(loggerPrefix + loggerName));
        Logger diagnosticLogger = null;
        if (isDiagnosticLoggingEnabled && mode != null) {
            try {
                if (diagnosticLoggingType.startsWith(LOG_USER)) {
                    String connectionid = ThreadContext.get((String)"connectionid");
                    String userid = diagnosticLoggingType.substring(5).toLowerCase();
                    if (connectionid != null && connectionid.contains("|" + userid + "-")) {
                        diagnosticLogger = LogManager.getLogger((String)(loggerPrefix + loggerName + "_diag"));
                    }
                } else if (diagnosticLoggingType.equals(mode)) {
                    diagnosticLogger = LogManager.getLogger((String)(loggerPrefix + loggerName + "_diag"));
                }
                if (diagnosticLogger != null && !diagnosticLoggingType.equals(lastDiagnosticLoggingType)) {
                    int i;
                    for (i = 0; i < 50; ++i) {
                        diagnosticLogger.log(Level.OFF, NODATABASE);
                    }
                    diagnosticLogger.log(Level.OFF, "=================================================================");
                    diagnosticLogger.log(Level.OFF, "$$$ SWITCHING DIAGNOSTIC LOG TO " + diagnosticLoggingType);
                    diagnosticLogger.log(Level.OFF, "=================================================================");
                    for (i = 0; i < 50; ++i) {
                        diagnosticLogger.log(Level.OFF, NODATABASE);
                    }
                    lastDiagnosticLoggingType = diagnosticLoggingType;
                }
            }
            catch (Exception e) {
                mainLogger.error("Failed to create diagnostic logging: " + e.getMessage());
            }
        }
        return new Logger[]{mainLogger, diagnosticLogger};
    }

    private static boolean isDBConfiguredAsSplitLog(String databaseid) {
        boolean configuerd = false;
        Logger mainLogger = LogManager.getLogger((String)(loggerPrefix + "sapphire"));
        try {
            Configuration configuration = Configuration.getInstance();
            LogConfig logConfig = new LogConfig(configuration.getApplicationid(), configuration.getLogConfigFile());
            HashSet tempSet = new HashSet();
            String dbList = logConfig.getSplitByDatabaseList();
            String[] dbids = dbList.split(",");
            for (int len = 0; len < dbids.length; ++len) {
                String dbID = dbids[len];
                if (!databaseid.equalsIgnoreCase(dbID.trim())) continue;
                configuerd = true;
                break;
            }
        }
        catch (SapphireException spx) {
            mainLogger.error("Failed to create database specific logging: " + spx.getMessage());
        }
        return configuerd;
    }

    public static void log(String message) {
        Trace.log(NODATABASE, message);
    }

    public static void log(String header, String message) {
        if (header == null) {
            header = NODATABASE;
        }
        if (message == null) {
            message = NODATABASE;
        }
        if (message.length() > 4000) {
            message = message.substring(0, 4000) + "...";
        }
        if (message.indexOf("\n") > 0) {
            String[] lines;
            for (String line : lines = StringUtil.split(message, "\n")) {
                if (line.length() <= 0) continue;
                Trace.logInfo(header, line);
            }
        } else {
            Trace.logInfo(header, message);
        }
    }

    public static void addNoKeepAliveThread() {
        noKeepAliveThreads.add(Thread.currentThread().getName());
    }

    public static void clearNoKeepAliveThread() {
        noKeepAliveThreads.remove(Thread.currentThread().getName());
    }

    public static boolean isNoKeepAliveThread() {
        return noKeepAliveThreads.contains(Thread.currentThread().getName());
    }

    public static void startRequest(String command, String commandparam) {
        Trace.setStart(7, command + ";" + commandparam + Thread.currentThread().getName(), command + ";" + commandparam, NODATABASE);
    }

    public static void endRequest(String command, String commandparam) {
        Trace.setEnd(7, command + ";" + commandparam + Thread.currentThread().getName(), command + ";" + commandparam);
    }

    public static void setStartAction(String actionid, String sdcid, PropertyList properties) {
        SDIList sdiList = new SDIList();
        sdiList.setSdcid(properties.getProperty("sdcid"));
        sdiList.addSDIList(properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"));
        String context = sdiList.getSdcid() + ";" + sdiList.toString();
        Trace.setStart(0, actionid + ";" + sdcid + ";" + Thread.currentThread().getName(), actionid + ";" + sdcid, context.equals(";") ? NODATABASE : context);
    }

    public static int setEndAction(String actionid, String sdcid) {
        return Trace.setEnd(0, actionid + ";" + sdcid + ";" + Thread.currentThread().getName(), actionid + ";" + sdcid);
    }

    public static void startBusinessRule(String eventType, boolean systemRule) {
        Trace.setStart(1, systemRule + eventType + Thread.currentThread().getName(), systemRule + eventType, NODATABASE);
    }

    public static int endBusinessRule(String eventType, boolean systemRule) {
        return Trace.setEnd(1, systemRule + eventType + Thread.currentThread().getName(), (systemRule ? "System " : "Custom ") + ";" + eventType);
    }

    public static void startActionBlock(String label) {
        Trace.setStart(4, label + Thread.currentThread().getName(), label, NODATABASE);
    }

    public static int endActionBlock(String label) {
        return Trace.setEnd(4, label + Thread.currentThread().getName(), label);
    }

    public static void setStartSQL(String resultset, String context) {
        Trace.setStart(2, resultset + ";" + Thread.currentThread().getName(), resultset, context);
    }

    public static int setEndSQL(String resultset) {
        return Trace.setEnd(2, resultset + ";" + Thread.currentThread().getName(), resultset);
    }

    public static void startQuery(String sdcid, String queryid, String[] params) {
        Trace.setStart(5, sdcid + ";" + queryid + ";" + Thread.currentThread().getName(), sdcid + ";" + queryid, NODATABASE);
    }

    public static void endQuery(String sdcid, String queryid) {
        Trace.setEnd(5, sdcid + ";" + queryid + ";" + Thread.currentThread().getName(), sdcid + " - " + queryid);
    }

    public static int setEndRSet(String method, int rows, long starttime, String rsetid) {
        String reportRows;
        String string = rows > 5000 ? "> 5000" : (rows > 1000 ? "1001 - 5000" : (rows > 500 ? "501 - 1000" : (rows > 200 ? "201 - 500" : (rows > 100 ? "101 - 200" : (rows > 50 ? "51 - 100" : (rows > 20 ? "21 - 50" : (rows > 10 ? "11 - 20" : (rows > 1 ? "2 - 10" : (reportRows = rows > 0 ? "1" : "0")))))))));
        String sequence = rows > 5000 ? "K" : (rows > 1000 ? "J" : (rows > 500 ? "I" : (rows > 200 ? "H" : (rows > 100 ? "G" : (rows > 50 ? "F" : (rows > 20 ? "E" : (rows > 10 ? "D" : (rows > 1 ? "C" : (rows > 0 ? "B" : "A")))))))));
        return Trace.logStatistic(3, method + "_" + sequence + ";" + method + " (" + reportRows + " rows)", starttime, rsetid);
    }

    public static void setStartCrossSDICalc(String crossSDICalcDefId, String context) {
        Trace.setStart(8, crossSDICalcDefId + Thread.currentThread().getName(), crossSDICalcDefId, context);
    }

    public static int setEndCrossSDICalc(String crossSDICalcDefId, String context, int rows, long starttime) {
        String sequence;
        String reportRows;
        String rowText = " SDIs.";
        String string = rows > 5000 ? "> 5000" : (rows > 1000 ? "1001 - 5000" : (rows > 500 ? "501 - 1000" : (rows > 200 ? "201 - 500" : (rows > 100 ? "101 - 200" : (rows > 50 ? "51 - 100" : (rows > 20 ? "21 - 50" : (rows > 10 ? "11 - 20" : (rows > 1 ? "2 - 10" : (reportRows = rows > 0 ? "1" : "0")))))))));
        String string2 = rows > 5000 ? "K" : (rows > 1000 ? "J" : (rows > 500 ? "I" : (rows > 200 ? "H" : (rows > 100 ? "G" : (rows > 50 ? "F" : (rows > 20 ? "E" : (rows > 10 ? "D" : (rows > 1 ? "C" : (sequence = rows > 0 ? "B" : "A")))))))));
        if (crossSDICalcDefId.length() > 0) {
            String viewDefLink = "rc?command=page&page=LV_CrossSDICalcDefView&keyid1=" + crossSDICalcDefId + "&mode=View";
            String url = "<a href=\"#\" onclick=\"Javascript:sapphire.lookup.open('" + viewDefLink + "','', 'resizable=yes,width=700,height=400')\" title=\"View Details of " + crossSDICalcDefId + "\">" + crossSDICalcDefId + "</a>";
            return Trace.logStatistic(8, crossSDICalcDefId + " " + context + "_" + sequence + ";" + url + " " + context + " (" + reportRows + rowText + ")", starttime, NODATABASE);
        }
        return Trace.logStatistic(8, context + "_" + sequence + "; " + context + " (" + reportRows + rowText + ")", starttime, NODATABASE);
    }

    public static void setStartCodeBlock(String name) {
        Trace.setStartCodeBlock(name, NODATABASE);
    }

    public static void setStartCodeBlock(String name, String context) {
        Trace.setStart(6, name + Thread.currentThread().getName(), name, context);
    }

    public static int setEndCodeBlock(String name) {
        return Trace.setEnd(6, name + Thread.currentThread().getName(), name);
    }

    public static int setEndCodeBlock(String name, String storeAs) {
        return Trace.setEnd(6, name + Thread.currentThread().getName(), storeAs);
    }

    private static synchronized void setStart(int type, String workingkey, String storekey, String context) {
        if (starttimes.size() > 5000) {
            starttimes.clear();
        }
        starttimes.put(workingkey, new Long(System.currentTimeMillis()));
    }

    private static synchronized int setEnd(int type, String workingkey, String storekey) {
        Long starttime = starttimes.get(workingkey);
        long took = 0L;
        if (starttime != null) {
            took = System.currentTimeMillis() - starttime;
            StatRecord record = statstore[type].get(storekey);
            if (record == null) {
                record = new StatRecord();
                if (statstore[type].size() < 2000) {
                    statstore[type].put(storekey, record);
                }
            }
            if (tracestats) {
                Trace.logInfo("STATS", typetext[type] + ": " + storekey + " took " + took + "ms");
            }
            record.setTime(took);
        }
        return (int)took;
    }

    private static synchronized int logStatistic(int type, String storekey, long starttime, String context) {
        long took = System.currentTimeMillis() - starttime;
        StatRecord record = statstore[type].get(storekey);
        if (record == null) {
            record = new StatRecord();
            if (statstore[type].size() < 2000) {
                statstore[type].put(storekey, record);
            }
        }
        if (tracestats) {
            Trace.logInfo("STATS", typetext[type] + ": " + storekey + " took " + took + "ms");
        }
        record.setTime(took);
        return (int)took;
    }

    public static synchronized DataSet getStats(int type) {
        DataSet ds = new DataSet();
        ds.addColumn("id1", 0);
        ds.addColumn("id2", 0);
        ds.addColumn("id3", 0);
        ds.addColumn("count", 1);
        ds.addColumn("average", 1);
        ds.addColumn("total", 1);
        ds.addColumn("min", 1);
        ds.addColumn("max", 1);
        ds.addColumn("last", 1);
        Set<String> set = statstore[type].keySet();
        for (String key : set) {
            StatRecord record = statstore[type].get(key);
            int newrow = ds.addRow();
            String[] keyparts = StringUtil.split(key, ";");
            for (int i = 0; i < keyparts.length; ++i) {
                ds.setString(newrow, "id" + (i + 1), keyparts[i]);
            }
            ds.setNumber(newrow, "count", record.count);
            ds.setNumber(newrow, "average", record.total / record.count);
            ds.setNumber(newrow, "total", record.total);
            ds.setNumber(newrow, "min", record.min);
            ds.setNumber(newrow, "max", record.max);
            ds.setNumber(newrow, "last", record.last);
        }
        return ds;
    }

    public static synchronized void resetStats() {
        statstore[0].clear();
        statstore[1].clear();
        statstore[2].clear();
        statstore[3].clear();
        statstore[4].clear();
        statstore[7].clear();
        statstore[6].clear();
        statstore[5].clear();
        statstore[8].clear();
    }

    public static synchronized int getMonitoringTotalCounter(int storeType, String startsWith) {
        int counter = 0;
        Set<String> set = statstore[storeType].keySet();
        for (String key : set) {
            if (startsWith.length() != 0 && !key.toUpperCase().startsWith(startsWith.toUpperCase())) continue;
            StatRecord record = statstore[storeType].get(key);
            counter = (int)((long)counter + record.count);
        }
        return counter;
    }

    public static void setSplitDatabaseList(String list) {
        if (list != null && list.length() > 0) {
            String[] database = StringUtil.split(list, ",");
            for (int i = 0; i < database.length; ++i) {
                String s = database[i].trim();
                if (s.length() <= 0) continue;
                splitByDatabaseList.add(s);
            }
        }
    }

    static {
        Trace.statstore[0] = new HashMap();
        Trace.statstore[1] = new HashMap();
        Trace.statstore[2] = new HashMap();
        Trace.statstore[3] = new HashMap();
        Trace.statstore[4] = new HashMap();
        Trace.statstore[7] = new HashMap();
        Trace.statstore[6] = new HashMap();
        Trace.statstore[5] = new HashMap();
        Trace.statstore[8] = new HashMap();
        noKeepAliveThreads = new HashSet();
        loggerPrefix = NODATABASE;
        isLogByDatabase = false;
        splitByDatabaseList = new HashSet<String>();
        isDiagnosticLoggingEnabled = false;
        diagnosticLoggingType = NODATABASE;
        lastDiagnosticLoggingType = NODATABASE;
        blankLogContext = new LogContext("(none)");
    }
}

