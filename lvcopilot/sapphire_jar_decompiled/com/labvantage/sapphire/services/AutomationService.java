/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.io.FileUtils
 */
package com.labvantage.sapphire.services;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.automation.AddToDoListEntry;
import com.labvantage.sapphire.admin.logfileviewer.LogViewerUtil;
import com.labvantage.sapphire.admin.system.automation.LAM;
import com.labvantage.sapphire.admin.system.automation.LAMException;
import com.labvantage.sapphire.admin.system.automation.LAMStats;
import com.labvantage.sapphire.admin.system.automation.Server;
import com.labvantage.sapphire.ajax.operations.GetLAMStats;
import com.labvantage.sapphire.modules.eventmanager.NotifyManager;
import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.modules.sdms.SDMSUtil;
import com.labvantage.sapphire.modules.sdms.collector.SDMSCollectorInternalHolder;
import com.labvantage.sapphire.modules.statsmonitor.BaseMonitor;
import com.labvantage.sapphire.modules.statsmonitor.MonitorConstants;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.ActionService;
import com.labvantage.sapphire.services.BaseService;
import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.SapphireService;
import com.labvantage.sapphire.services.SecurityService;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.UnitsUtil;
import com.labvantage.sapphire.util.cache.CacheNames;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.xml.PropertyListUtil;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.apache.commons.io.FileUtils;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;

public class AutomationService
extends BaseService
implements CacheNames,
MonitorConstants,
SDMSConstants {
    public static final String LOGNAME = "AutomationService";
    public static final String TODOLIST_STATUS_WAITING = "W";
    public static final String TODOLIST_STATUS_SENT = "S";
    public static final String TODOLIST_STATUS_PROCESSING = "P";
    public static final String TODOLIST_STATUS_ERROR = "E";
    public static final String PROCESSAS_TOOL = "TDLProcessing";
    public static final String COMMAND_RESETCACHE = "ResetCache";
    public static final String COMMAND_UPDATEUNITCONVERSIONCACHE = "UpdateUnitConversionCache";
    public static final String COMMAND_REMOVEALLSTARTSWITHFROMCACHE = "RemoveAllStartsWithFromCache";
    public static final String COMMAND_REMOVEALLENDSWITHFROMCACHE = "RemoveAllEndsWithFromCache";
    public static final String COMMAND_CLEARCACHE = "ClearCache";
    public static final String COMMAND_REMOVEFROMCACHE = "RemoveFromCache";
    public static final String COMMAND_NOTIFICATION = "Notification";
    public static final String COMMAND_REQUESTLAMSTATS = "RequestLamStats";
    public static final String COMMAND_RESPONDLAMSTATS = "RespondLamStats";
    public static final String COMMAND_GENERATESNAPSHOT = "GenerateSnapshot";
    public static final String COMMAND_CLEARCONNECTIONFROMCACHE = "ClearConnectionFromCache";
    private static String systemPassword;
    private static HashMap<String, LAM> lams;
    private static HashMap<String, ArrayList> otherAutomationServerListCache;
    private static HashMap<String, ArrayList> otherServerListCache;
    private static HashMap<String, ArrayList> fullServerListCache;
    private static HashMap<String, Boolean> isPrimaryAutomationServer;
    private static HashMap<String, Boolean> isAutomationServer;
    private static final HashMap<String, ArrayList<Command>> databaseServerCommands;
    private static final HashMap<String, Integer> lastCommandSequence;
    private static final HashMap<String, String> serverLamStats;

    public LAM createLAM(String databaseid) throws LAMException {
        LAM lam = new LAM(databaseid);
        lams.put(databaseid, lam);
        return lam;
    }

    public void destroyLAM(String databaseid) throws LAMException {
        LAM lam = AutomationService.getLAM(databaseid);
        lam.stopAll();
        lams.remove(databaseid);
    }

    public static LAM getLAM(String databaseid) {
        return lams.get(databaseid);
    }

    public static void setSystemPassword(String systemPswd) {
        systemPassword = systemPswd;
    }

    public AutomationService(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
        this.logName = LOGNAME;
    }

    public String addToDoListEntry(String requestorid, ActionBlock actionBlock, String dueDate, boolean delete, String processAsSysuserid, String processName) throws ServiceException {
        this.logInfo("Adding todolist entry for actionblock '" + actionBlock.getName() + "'");
        PropertyList props = new PropertyList();
        props.setProperty("actionblockname", actionBlock.getName());
        props.setProperty("actionblockxml", actionBlock.toXML());
        return this.addToDoListEntry(requestorid, "ProcessActionBlock", "1", props, dueDate, delete, processAsSysuserid, processName, "", "");
    }

    public String addToDoListEntry(String requestorid, String actionid, String actionversionid, PropertyList propertyList, String dueDate, boolean delete) throws ServiceException {
        return this.addToDoListEntry(requestorid, actionid, actionversionid, propertyList, dueDate, delete, "", "", "", "");
    }

    public String addToDoListEntry(String requestorid, String actionid, String actionversionid, PropertyList propertyList, String dueDate, boolean delete, String processAsSysuserid, String processName, String groupName, String groupErrorRule) throws ServiceException {
        String todolistid = "";
        this.logInfo("Adding todolist entry for action '" + actionid + "'");
        if (actionid == null || actionid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Actionid not specified");
        }
        if (requestorid == null || requestorid.length() == 0) {
            requestorid = this.connectionInfo.getSysuserId();
        }
        if (actionversionid == null || actionversionid.length() == 0) {
            actionversionid = "1";
        }
        if (dueDate == null || dueDate.length() == 0) {
            dueDate = "YESTERDAY";
        }
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        db.setConnection(this.sapphireConnection);
        try {
            int namedprocesscount = 0;
            if (processName != null && processName.length() > 0 && (namedprocesscount = db.getPreparedCount("SELECT count(*) FROM todolist WHERE processname=? AND statusflag in ( 'W', 'S', 'P' )", processName)) > 0) {
                this.logInfo("Adding todolist entry for action '" + actionid + "' ignored as the process " + processName + " already exists.");
            }
            if (namedprocesscount == 0) {
                String callstmt = "{call lv_tdl" + (this.connectionInfo.isOracle() ? "." : "_") + "addentry( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) }";
                CallableStatement cs = db.prepareCall(callstmt);
                cs.setString(1, actionid);
                cs.setString(2, actionversionid);
                cs.setString(3, TODOLIST_STATUS_WAITING);
                cs.setString(4, requestorid);
                cs.setString(5, delete ? "Y" : "N");
                cs.registerOutParameter(6, 12);
                cs.setString(7, PropertyListUtil.concatenateProps(propertyList));
                if (requestorid.equals("(system)")) {
                    cs.setTimestamp(8, new DateTimeUtil().getTimestamp(dueDate));
                } else {
                    cs.setTimestamp(8, new DateTimeUtil(this.connectionInfo).getTimestamp(dueDate));
                }
                cs.setString(9, processAsSysuserid);
                cs.setString(10, processName);
                cs.setString(11, Configuration.getInstance().getHostid());
                cs.setString(12, groupName);
                cs.setString(13, this.isToDoListLogging());
                cs.setString(14, groupErrorRule);
                cs.executeUpdate();
                todolistid = cs.getString(6);
                db.closeCall();
                this.logInfo("Todolist entry added for action: " + actionid);
            }
        }
        catch (SQLException e) {
            throw new ServiceException("DB_ACTION_FAILED", "Exception thrown adding todolist entry: " + e.getMessage(), e);
        }
        catch (SapphireException e) {
            throw new ServiceException("AUTOMATION_SERVICE_FAILED", "Failed to add a new TODOLIST entry.", e);
        }
        finally {
            db.reset();
        }
        return todolistid;
    }

    private String isToDoListLogging() {
        try {
            return new com.labvantage.sapphire.admin.system.ConfigurationProcessor(this.getConnectionid()).getSysConfigProperty("todolistlogging", "N");
        }
        catch (SapphireException e) {
            return "N";
        }
    }

    public void deleteToDoListEntry(String todolistidlist) throws ServiceException {
        this.logInfo("Deleting todolist entry '" + todolistidlist + "'");
        if (todolistidlist == null || todolistidlist.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Todolistid not specified");
        }
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            String[] todolistids = StringUtil.split(todolistidlist, ";");
            for (int i = 0; i < todolistids.length; ++i) {
                String todolistid = todolistids[i];
                try {
                    String callstmt = "{call lv_tdl" + (this.connectionInfo.isOracle() ? "." : "_") + "removeentry( ?, ?, ? ) }";
                    CallableStatement cs = db.prepareCall(callstmt);
                    cs.setString(1, todolistid);
                    cs.registerOutParameter(2, 12);
                    cs.setString(3, this.isToDoListLogging());
                    cs.executeUpdate();
                    String status = cs.getString(2);
                    if (!"B".equals(status)) continue;
                    throw new ServiceException("DELETE_TDL_FAILED", "Failed to delete todolistid: " + todolistid + " as it is currently locked.");
                }
                catch (SQLException e) {
                    throw new ServiceException("DELETE_TDL_FAILED", "Failed to delete todolistid: " + todolistid);
                }
            }
            this.logInfo("Deleted todolist entries " + todolistidlist);
        }
        catch (SapphireException e) {
            throw new ServiceException("DELETE_TDL_FAILED", "Failed to delete to do list entries '" + todolistidlist + "': " + e.getMessage(), e);
        }
        finally {
            db.reset();
        }
    }

    public void resendToDoListEntry(String todolistid) throws ServiceException {
        this.logInfo("Resending todolist entry '" + todolistid + "'");
        if (todolistid == null || todolistid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Todolistid not specified");
        }
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            SafeSQL safeSQL = new SafeSQL();
            db.executePreparedUpdate("UPDATE todolist SET statusflag = 'W', errortext = null WHERE todolistid IN ( " + safeSQL.addIn(todolistid, ";") + ")", safeSQL.getValues());
            this.logInfo("Resent todolist entries " + todolistid);
        }
        catch (SapphireException e) {
            throw new ServiceException("DB_ACTION_FAILED", "Failed to resend to do list entry '" + todolistid + "'", e);
        }
        finally {
            db.reset();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean processToDoListEntry(String todolistid) throws ServiceException {
        boolean success = false;
        this.logInfo("Processing todolist entry '" + todolistid + "'");
        if (todolistid == null || todolistid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Todolistid not specified");
        }
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            String hostid = Configuration.getInstance().getHostid();
            db.setConnection(this.sapphireConnection);
            try {
                String callstmt = "{call lv_tdl" + (this.connectionInfo.isOracle() ? "." : "_") + "processstart( ?, ?, ?, ? ) }";
                CallableStatement cs = db.prepareCall(callstmt);
                cs.setString(1, todolistid);
                cs.registerOutParameter(2, 12);
                cs.setString(3, hostid);
                cs.setString(4, this.isToDoListLogging());
                cs.executeUpdate();
                String status = cs.getString(2);
                if ("N".equals(status)) {
                    this.logger.warn("Failed to start todolist entry " + todolistid + " because it could not be found.");
                } else if ("B".equals(status)) {
                    this.logger.warn("Failed to lock todolist entry " + todolistid + " for processing because it is already locked.");
                } else if ("U".equals(status)) {
                    success = true;
                }
            }
            catch (SQLException e) {
                this.logger.warn("Failed to mark todolistid " + todolistid + " for processing: " + e.getMessage());
            }
            if (!success) return success;
            db.createPreparedResultSet("todolist", "SELECT actionid, actionversionid, propertyclob, requestorid, processassysuserid FROM todolist WHERE todolistid=? AND statusflag=?", new String[]{todolistid, TODOLIST_STATUS_PROCESSING});
            if (db.getNext("todolist")) {
                String actionid = db.getString("todolist", "actionid");
                String actionversionid = db.getString("todolist", "actionversionid");
                PropertyList actionProps = new PropertyList();
                String propertyClob = db.getClob("todolist", "propertyclob");
                if (propertyClob != null && propertyClob.length() > 0) {
                    if (!PropertyListUtil.isDelimeteredProps(propertyClob)) {
                        propertyClob = db.getClob("todolist", "propertyclob");
                        if (db.isSqlServer() && propertyClob.endsWith("\r\n")) {
                            propertyClob = propertyClob.substring(0, propertyClob.length() - 2);
                        }
                    }
                    PropertyListUtil.setDelimiteredProps(actionProps, propertyClob);
                }
                if (db.getValue("todolist", "processassysuserid").length() == 0) {
                    String requestorid = db.getString("todolist", "requestorid");
                    if (!"(system)".equals(requestorid) && !"TaskList".equals(requestorid)) {
                        db.createPreparedResultSet("checkLocale", "SELECT localeid, timezone FROM sysuser WHERE sysuserid = ? AND (localeid IS NOT NULL OR timezone IS NOT NULL)", requestorid);
                        if (db.getNext("checkLocale")) {
                            this.sapphireConnection.setLocale(db.getString("checkLocale", "localeid"));
                            this.sapphireConnection.setTimezone(db.getString("checkLocale", "timezone"));
                        }
                    }
                    ActionService actionService = new ActionService(this.sapphireConnection);
                    actionService.processAction(actionid, actionversionid, actionProps);
                    this.sapphireConnection.setLocale("");
                    this.sapphireConnection.setTimezone("");
                    return success;
                }
                Class<AutomationService> actionService = AutomationService.class;
                synchronized (AutomationService.class) {
                    String connectionid = (String)CacheUtil.get(this.sapphireConnection.getDatabaseId(), "ProcessAsConnections", db.getValue("todolist", "processassysuserid"));
                    if (!SecurityService.checkConnection(db, connectionid)) {
                        connectionid = "";
                    } else {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
                        String callstmt = "{call lv_rset" + (db.isOracle() ? "." : "_") + "ConnPingList( ?, ?, ? ) }";
                        CallableStatement cs = db.prepareCall(callstmt);
                        try {
                            cs.setString(1, connectionid);
                            cs.setString(2, sdf.format(DateTimeUtil.getNowCalendar().getTime()));
                            if (db.isOracle()) {
                                cs.setString(3, "yyyy/mm/dd hh24:mi:ss");
                            } else {
                                cs.setInt(3, 120);
                            }
                            cs.executeUpdate();
                        }
                        catch (SQLException sqle) {
                            this.logWarn("Failed to ping '" + connectionid + "' in database " + this.connectionInfo.getDatabaseId() + ". Getting a new connectionid to process task");
                            connectionid = "";
                        }
                    }
                    if (connectionid == null || connectionid.length() == 0) {
                        ConnectionProcessor cp = this.getConnectionProcessor();
                        String databaseid = this.sapphireConnection.getDatabaseId();
                        String userid = db.getValue("todolist", "processassysuserid");
                        String password = systemPassword;
                        HashMap<String, String> options = new HashMap<String, String>();
                        options.put("tool", PROCESSAS_TOOL);
                        options.put("dbms", db.getDbms());
                        options.put("jobtype", "(null)");
                        options.put("ignoreexpirywarning", "true");
                        connectionid = cp.getConnectionid(databaseid, userid, password, options);
                        CacheUtil.put(this.sapphireConnection.getDatabaseId(), "ProcessAsConnections", db.getValue("todolist", "processassysuserid"), connectionid);
                    }
                    Trace.setThreadMDCConnectionid(connectionid);
                    // ** MonitorExit[actionService] (shouldn't be in output)
                    SapphireConnection userConnection = SecurityService.getSapphireConnection(db, connectionid);
                    ActionService actionService2 = new ActionService(userConnection);
                    actionService2.processAction(actionid, actionversionid, actionProps);
                    return success;
                }
            }
            this.logWarn("Failed to find todolistid '" + todolistid + "' in database " + this.connectionInfo.getDatabaseId());
            return success;
        }
        catch (SapphireException e) {
            throw new ServiceException("DB_ACTION_FAILED", "Failed to process to do list entry '" + todolistid + "'", e);
        }
        finally {
            db.reset();
        }
    }

    public String getNextToDoListItems(int count) throws ServiceException {
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        String list = "";
        try {
            db.setConnection(this.sapphireConnection);
            String hostid = Configuration.getInstance().getHostid();
            String callstmt = "{call lv_tdl" + (this.connectionInfo.isOracle() ? "." : "_") + "getnextlist( ?, ?, ?, ?, ?, ? ) }";
            CallableStatement cs = db.prepareCall(callstmt);
            cs.setInt(1, count);
            cs.registerOutParameter(2, 12);
            cs.registerOutParameter(3, 4);
            cs.registerOutParameter(4, 12);
            cs.setString(5, hostid);
            cs.setString(6, this.isToDoListLogging());
            cs.executeUpdate();
            list = cs.getString(2);
            String status = cs.getString(4);
            if ("B".equals(status)) {
                this.logger.info("Unable to fetch next todolist items because of a locking issue.");
            }
            db.closeCall();
        }
        catch (Exception e) {
            throw new ServiceException("DB_ACTION_FAILED", "Failed to fetch next todolist items for database '" + this.connectionInfo.getDatabaseId() + "'", e);
        }
        finally {
            db.reset();
        }
        return list;
    }

    public void setToDoListError(String todolistid, String errorText) throws ServiceException {
        this.logInfo("Setting todolist error for todolistid '" + todolistid + "'");
        if (todolistid == null || todolistid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Todolistid not specified");
        }
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            String callstmt = "{call lv_tdl" + (this.connectionInfo.isOracle() ? "." : "_") + "seterror( ?, ?, ?, ? ) }";
            CallableStatement cs = db.prepareCall(callstmt);
            cs.setString(1, todolistid);
            cs.registerOutParameter(2, 12);
            cs.setString(3, errorText);
            cs.setString(4, this.isToDoListLogging());
            cs.executeUpdate();
            String status = cs.getString(2);
            if ("B".equals(status)) {
                throw new ServiceException("DELETE_TDL_FAILED", "Failed to set todolistid " + todolistid + " to error state as it is currently locked.");
            }
        }
        catch (Exception e) {
            throw new ServiceException("DELETE_TDL_FAILED", "Failed to set todolistid " + todolistid + " to error state.");
        }
        finally {
            db.reset();
        }
    }

    public PropertyList getToDoListProperties(String todolistid) throws ServiceException {
        this.logInfo("Getting todolist properties for todolistid '" + todolistid + "'");
        if (todolistid == null || todolistid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Todolistid not specified");
        }
        DBUtil dbu = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            dbu.setConnection(this.sapphireConnection);
            dbu.createPreparedResultSet("SELECT propertyclob FROM todolist WHERE todolistid = ?", todolistid);
            if (dbu.getNext()) {
                PropertyList propertyList = new PropertyList();
                PropertyListUtil.setDelimiteredProps(propertyList, dbu.getClob("propertyclob"));
                PropertyList propertyList2 = propertyList;
                return propertyList2;
            }
            try {
                throw new ServiceException("INVALID_PARAMETER", "Todolist entry '" + todolistid + "' not found");
            }
            catch (Exception e) {
                throw new ServiceException("DB_ACTION_FAILED", "Failed to select properties for todolistid '" + todolistid + "'", e);
            }
        }
        finally {
            dbu.reset();
        }
    }

    public void performHouseKeeping() throws ServiceException {
        this.logDebug("Performing housekeeping tasks");
        DateTimeUtil dtu = new DateTimeUtil();
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            Timestamp statsmonitortimeout;
            String departmentAssignmentHistory;
            String webpagetimeoutdays;
            db.setConnection(this.sapphireConnection);
            int totalrows = 0;
            ConfigService config = new ConfigService(this.sapphireConnection);
            String connectionlogtimeoutdays = config.getSysConfigProperty("hkconnectionlog", String.valueOf("180"));
            if (connectionlogtimeoutdays.length() > 0 && !connectionlogtimeoutdays.equals("0")) {
                String callstmt = "{call lv_clean" + (this.connectionInfo.isOracle() ? "." : "_") + "cleanconnectionlogcnt( ? ) }";
                CallableStatement cs = db.prepareCall(callstmt);
                try {
                    cs.registerOutParameter(1, 2);
                    cs.executeUpdate();
                    totalrows += cs.getInt(1);
                }
                catch (SQLException e) {
                    this.logError("Deleting  connectionlog failed.", e);
                }
            }
            if ((webpagetimeoutdays = config.getSysConfigProperty("hkwebpagehistory", String.valueOf("30"))).length() > 0 && !webpagetimeoutdays.equals("0")) {
                String callstmt = "{call lv_clean" + (this.connectionInfo.isOracle() ? "." : "_") + "cleanwebpagelogcnt( ?, ?, ? ) }";
                CallableStatement cs = db.prepareCall(callstmt);
                try {
                    cs.registerOutParameter(1, 2);
                    cs.registerOutParameter(2, 2);
                    cs.registerOutParameter(3, 2);
                    cs.executeUpdate();
                    int wlcnt_out = cs.getInt(1);
                    int wltcnt_out = cs.getInt(2);
                    int wlpcnt_out = cs.getInt(3);
                    totalrows += wlcnt_out + wlpcnt_out + wltcnt_out;
                }
                catch (SQLException e) {
                    this.logError("Deleting  webpagelog failed.", e);
                }
            }
            if ((departmentAssignmentHistory = config.getSysConfigProperty("hkdeptassignment", String.valueOf("30"))).length() > 0 && !departmentAssignmentHistory.equals("0")) {
                Timestamp enddt = dtu.getTimestamp("now-" + departmentAssignmentHistory + "d");
                totalrows += db.executePreparedUpdate("DELETE FROM departmentassignment WHERE assignmentstatus='Done' AND assignmentenddt < ?", enddt);
            }
            String callstmt = "{call lv_rset" + (this.connectionInfo.isOracle() ? "." : "_") + "ClearOrphanedRSet() }";
            CallableStatement cs = db.prepareCall(callstmt);
            try {
                totalrows += cs.executeUpdate();
            }
            catch (SQLException e) {
                this.logError("Deleting orphaned RSETs failed.", e);
            }
            String statsmonitoryhourlytimeout = config.getSysConfigProperty("hkhourlystats", String.valueOf("24"));
            String statsmonitorydailytimeout = config.getSysConfigProperty("hkdailystats", String.valueOf("30"));
            if (statsmonitoryhourlytimeout.length() > 0 && !statsmonitoryhourlytimeout.equals("0")) {
                statsmonitortimeout = dtu.getTimestamp("now-" + statsmonitoryhourlytimeout + "h");
                totalrows += db.executePreparedUpdate("DELETE FROM statsmonitorvalue WHERE capturemode='Hourly' AND capturedt < ?", statsmonitortimeout);
            }
            if (statsmonitorydailytimeout.length() > 0 && !statsmonitorydailytimeout.equals("0")) {
                statsmonitortimeout = dtu.getTimestamp("now-" + statsmonitorydailytimeout + "d");
                totalrows += db.executePreparedUpdate("DELETE FROM statsmonitorvalue WHERE capturemode='Daily' AND capturedt < ?", statsmonitortimeout);
            }
            Timestamp servercommandTimeout = dtu.getTimestamp("now-1h");
            totalrows += db.executePreparedUpdate("DELETE FROM servercommand WHERE createdt < ?", servercommandTimeout);
            DataSet expireTokens = this.getQueryProcessor().getPreparedSqlDataSet("SELECT authtokenid FROM authtoken WHERE expirydt < ? AND tokenstatus=?", new Object[]{DateTimeUtil.getNowTimestamp(), "Active"});
            for (int i = 0; i < expireTokens.size(); ++i) {
                String authtokenid = expireTokens.getValue(i, "authtokenid");
                db.executePreparedUpdate("UPDATE authtoken SET tokenstatus=? WHERE authtokenid=?", new Object[]{"Expired", authtokenid});
            }
            String bulletintimeoutdays = config.getSysConfigProperty("hkbulletins", String.valueOf("0"));
            if (bulletintimeoutdays.length() > 0 && !bulletintimeoutdays.equals("0")) {
                Timestamp bulletintimeout = dtu.getTimestamp("now-" + bulletintimeoutdays + "d");
                int deleteRate = Integer.parseInt(ConfigService.getConfigProperty("com.labvantage.sapphire.server.bulletindeleterate", "500"));
                String sql = db.isOracle() ? "SELECT bulletinid FROM bulletin WHERE createdt < ? and rownum < " + deleteRate : "SELECT TOP( " + deleteRate + ") bulletinid FROM bulletin WHERE createdt < ?";
                String resultSetName = "clearbulletins";
                db.createPreparedResultSet(resultSetName, sql, bulletintimeout);
                DataSet ds = new DataSet(db.getResultSet(resultSetName));
                db.closeResultSet(resultSetName);
                if (ds.size() > 0) {
                    String in = "'" + ds.getColumnValues("bulletinid", "','") + "'";
                    this.logInfo("About to delete " + ds.size() + " bulletins");
                    totalrows += db.executeUpdate("DELETE FROM bulletinsysuser WHERE bulletinid IN ( " + in + " )");
                    totalrows += db.executeUpdate("DELETE FROM bulletin WHERE bulletinid IN ( " + in + " )");
                }
            }
            Timestamp temptimeout = dtu.getTimestamp("now-30d");
            totalrows += db.executePreparedUpdate("DELETE FROM sditemp WHERE moddt < ?", temptimeout);
            int days = 14;
            totalrows += db.executePreparedUpdate("DELETE FROM rsetlog WHERE startdt < ?", dtu.getTimestamp("now-" + days + "d"));
            totalrows += db.executePreparedUpdate("DELETE FROM todolistlog WHERE logdt < ?", dtu.getTimestamp("now-" + days + "d"));
            if ((totalrows += FileManager.TempFile.removeOldTempFiles(dtu, db, this.getConnectionId())) > 0) {
                this.logInfo("House keeping updates complete. " + totalrows + " rows deleted from the database.");
            } else {
                this.logDebug("No housekeeping updates to be done.");
            }
        }
        catch (SapphireException e) {
            throw new ServiceException("DB_ACTION_FAILED", "Failed to perform housekeeping for database '" + this.connectionInfo.getDatabaseId() + "'", e);
        }
        finally {
            db.reset();
        }
    }

    public void performStatsMonitoring(String captureMode) throws ServiceException {
        this.logInfo("Performing stats monitoring");
        try {
            DataSet stats = new DataSet();
            stats.addColumn("statsmonitorgroupid", 0);
            stats.addColumn("statsmonitoritemid", 0);
            stats.addColumn("hostname", 0);
            stats.addColumn("capturedt", 2);
            stats.addColumn("capturevalue", 1);
            DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
            db.setConnection(this.sapphireConnection);
            HashMap<String, BaseMonitor> javaMonitors = new HashMap<String, BaseMonitor>();
            QueryProcessor qp = new QueryProcessor(this.connectionInfo.getConnectionId());
            DataSet items = qp.getSqlDataSet("SELECT * FROM statsmonitoritem WHERE activeflag='Y' ORDER BY usersequence, statsmonitorgroupid, statsmonitoritemid");
            for (int i = 0; i < items.size(); ++i) {
                String groupid = items.getString(i, "statsmonitorgroupid");
                String itemid = items.getString(i, "statsmonitoritemid");
                String className = items.getValue(i, "classname").trim();
                String args = items.getValue(i, "classargs");
                if (className.length() <= 0) continue;
                try {
                    double value = -99999.0;
                    BaseMonitor monitor = (BaseMonitor)javaMonitors.get(className);
                    if (monitor == null) {
                        Class<?> c = Class.forName(className);
                        monitor = (BaseMonitor)c.newInstance();
                        monitor.init(this.sapphireConnection.getDatabaseId(), db);
                        javaMonitors.put(className, monitor);
                    }
                    if (!((value = monitor.getValue(itemid, args)) > -99999.0)) continue;
                    int newrow = stats.addRow();
                    stats.setString(newrow, "statsmonitorgroupid", groupid);
                    stats.setString(newrow, "statsmonitoritemid", itemid);
                    stats.setNumber(newrow, "capturevalue", value);
                    continue;
                }
                catch (Exception e) {
                    this.logWarn("Unable to load stats monitor details for class " + className + ". Reason: " + e.getMessage());
                }
            }
            Configuration configuration = Configuration.getInstance();
            String hostname = configuration.getServerHostName();
            Calendar now = Calendar.getInstance();
            stats.setString(-1, "hostname", hostname);
            stats.setDate(-1, "capturedt", now);
            stats.setString(-1, "capturemode", captureMode);
            db.setConnection(this.sapphireConnection);
            DataSetUtil.insert(db, stats, "statsmonitorvalue");
        }
        catch (SapphireException e) {
            e.printStackTrace();
        }
    }

    public static void stopAllLams() {
        for (LAM lam : lams.values()) {
            lam.stopAll();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean pingServer(Server server) {
        boolean moreServersExist = true;
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            String ignoreEARId;
            db.setConnection(this.sapphireConnection);
            String earid = "";
            try {
                earid = FileUtil.getInputStreamString(SapphireService.class.getResourceAsStream("/ear.id"));
            }
            catch (Exception exception) {
                // empty catch block
            }
            int rows = db.executePreparedUpdate("UPDATE serverinstance SET pingdt=" + (db.isOracle() ? "sysdate" : "GetDate()") + ", earid=?, automationserverflag=? WHERE hostid=? AND applicationid=?", new Object[]{earid, server.isAutomationServer ? "Y" : "N", server.hostid, server.applicationid});
            if (rows == 0) {
                db.executePreparedUpdate("INSERT INTO serverinstance( pingdt, hostid, applicationid, earid, automationserverflag ) VALUES ( " + (db.isOracle() ? "sysdate" : "GetDate()") + ", ?, ?, ?, ? )", new Object[]{server.hostid, server.applicationid, earid, server.isAutomationServer ? "Y" : "N"});
            }
            if ((ignoreEARId = ConfigService.getConfigProperty("com.labvantage.sapphire.server.ignoreearid")).length() == 0 || ignoreEARId.equals("false") || ignoreEARId.equals("N")) {
                db.createPreparedResultSet("SELECT hostid, earid FROM serverinstance WHERE applicationid = ? AND earid <> ?", new Object[]{server.applicationid, earid});
                if (db.getNext()) {
                    Configuration.setState(1);
                    String error = "Incorrect EAR identifier (" + db.getValue("earid") + ") found on host '" + db.getValue("hostid") + " for application '" + server.applicationid + ". Server connections will be disabled - check that the same EAR is used throughout the system configuration.";
                    Configuration.setErrorMsg(error);
                    Configuration.getInstance().setValid(false);
                    Trace.logError(error);
                }
            }
            moreServersExist = db.getCount("SELECT count(*) FROM serverinstance") > 1;
        }
        catch (Exception e) {
            Trace.logError("Failed to ping server " + server, e);
        }
        finally {
            db.reset();
        }
        return moreServersExist;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void killRedundantServers(int serverLatency) {
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            if (db.isOracle()) {
                db.createResultSet("SELECT * FROM serverinstance WHERE pingdt < ( sysdate - " + serverLatency + "/86400 )");
            } else {
                db.createResultSet("SELECT * FROM serverinstance WHERE pingdt < DateAdd(s,-" + serverLatency + ",GetDate())");
            }
            DataSet killlist = new DataSet(db.getResultSet());
            for (int i = 0; i < killlist.size(); ++i) {
                Server server = new Server();
                server.hostid = killlist.getValue(i, "hostid");
                server.applicationid = killlist.getValue(i, "applicationid");
                server.isAutomationServer = killlist.getValue(i, "automationserverflag").equals("Y");
                AutomationService.killServer(db, server);
            }
        }
        catch (Exception e) {
            Trace.logError("Failed kill redundant servers", e);
        }
        finally {
            db.reset();
        }
    }

    private static void killServer(DBUtil db, Server server) {
        try {
            db.executePreparedUpdate("DELETE FROM serverinstance WHERE hostid=? AND applicationid=?", new Object[]{server.hostid, server.applicationid});
            String callstmt = "{call lv_tdl" + (db.isOracle() ? "." : "_") + "hostreset( ?, ?, ? ) }";
            CallableStatement cs = db.prepareCall(callstmt);
            cs.setString(1, server.hostid);
            cs.registerOutParameter(2, 12);
            cs.setString(3, "N");
            cs.executeUpdate();
            String status = cs.getString(2);
            if ("B".equals(status)) {
                Trace.logError("Failed reset all todolist entries for " + server.hostid + " because some where locked.");
            }
        }
        catch (Exception e) {
            Trace.logError("Failed kill server " + server, e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void scanServerList() {
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            Configuration c = Configuration.getInstance();
            Server thisServer = c.getServer();
            ArrayList<Server> otherServerList = new ArrayList<Server>();
            ArrayList<Server> otherAutomationServerList = new ArrayList<Server>();
            ArrayList<Server> fullServerList = new ArrayList<Server>();
            db.createResultSet("SELECT * FROM serverinstance ORDER BY hostid, applicationid");
            DataSet serverList = new DataSet(db.getResultSet());
            for (int i = 0; i < serverList.size(); ++i) {
                if (serverList.isNull(i, "primaryautomationserverflag")) {
                    serverList.setString(i, "primaryautomationserverflag", "N");
                }
                if (!serverList.isNull(i, "automationserverflag")) continue;
                serverList.setString(i, "automationserverflag", "Y");
            }
            HashMap<String, String> findMap = new HashMap<String, String>();
            findMap.put("primaryautomationserverflag", "Y");
            int firstPrimaryAutomationServer = serverList.findRow(findMap);
            int secondPrimaryAutomationServer = serverList.findRow(findMap, firstPrimaryAutomationServer + 1);
            boolean hasPrimaryAutomationServer = firstPrimaryAutomationServer >= 0;
            boolean hasExtraPrimaryAutomationServer = secondPrimaryAutomationServer >= 0;
            LAM lam = AutomationService.getLAM(this.sapphireConnection.getDatabaseId());
            if (serverList.size() == 1) {
                if (lam.isPollerActive(10)) {
                    lam.pausePoller(10);
                    lam.pausePoller(11);
                }
            } else if (!lam.isPollerActive(10)) {
                lam.resumePoller(10);
                lam.resumePoller(11);
            }
            findMap.clear();
            findMap.put("primaryautomationserverflag", "N");
            findMap.put("automationserverflag", "Y");
            int firstAutomationServerAvailable = serverList.findRow(findMap);
            findMap.clear();
            findMap.put("primaryautomationserverflag", "Y");
            findMap.put("automationserverflag", "N");
            boolean isPrimaryServerNonAutomation = serverList.findRow(findMap) >= 0;
            serverList.findRow(findMap);
            for (int i = 0; i < serverList.size(); ++i) {
                Server server = new Server();
                server.hostid = serverList.getValue(i, "hostid");
                server.applicationid = serverList.getValue(i, "applicationid");
                server.isAutomationServer = serverList.getValue(i, "automationserverflag").equals("Y");
                String primaryAutomationServerFlag = serverList.getValue(i, "primaryautomationserverflag");
                String automationServerFlag = serverList.getValue(i, "automationserverflag");
                if (!hasPrimaryAutomationServer && i == (firstAutomationServerAvailable < 0 ? 0 : firstAutomationServerAvailable)) {
                    db.executePreparedUpdate("UPDATE serverinstance SET primaryautomationserverflag='Y' WHERE hostid=? AND applicationid=?", new Object[]{thisServer.hostid, thisServer.applicationid});
                    primaryAutomationServerFlag = "Y";
                }
                if (hasExtraPrimaryAutomationServer && i == secondPrimaryAutomationServer) {
                    db.executePreparedUpdate("UPDATE serverinstance SET primaryautomationserverflag='N' WHERE hostid=? AND applicationid=?", new Object[]{thisServer.hostid, thisServer.applicationid});
                    primaryAutomationServerFlag = "N";
                }
                if (isPrimaryServerNonAutomation && firstAutomationServerAvailable >= 0 && i == firstPrimaryAutomationServer) {
                    db.executePreparedUpdate("UPDATE serverinstance SET primaryautomationserverflag='N' WHERE hostid=? AND applicationid=?", new Object[]{thisServer.hostid, thisServer.applicationid});
                    primaryAutomationServerFlag = "N";
                }
                if (server.hostid.equals(thisServer.hostid) && (server.applicationid.equals(thisServer.applicationid) || db.isSqlServer() && server.applicationid.toLowerCase().equals(thisServer.applicationid.toLowerCase()))) {
                    isPrimaryAutomationServer.put(this.sapphireConnection.getDatabaseId(), "Y".equals(primaryAutomationServerFlag));
                    isAutomationServer.put(this.sapphireConnection.getDatabaseId(), "Y".equals(automationServerFlag));
                    server.thisServer = true;
                } else {
                    otherServerList.add(server);
                    if (server.isAutomationServer || i == firstPrimaryAutomationServer) {
                        otherAutomationServerList.add(server);
                    }
                }
                fullServerList.add(server);
            }
            otherServerListCache.put(this.sapphireConnection.getDatabaseId(), otherServerList);
            otherAutomationServerListCache.put(this.sapphireConnection.getDatabaseId(), otherAutomationServerList);
            fullServerListCache.put(this.sapphireConnection.getDatabaseId(), fullServerList);
        }
        catch (Exception e) {
            Trace.logError("Failed to determine the list of other servers.", e);
        }
        finally {
            db.reset();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String sendServerCommands() {
        block8: {
            String connectionid = this.sapphireConnection.getConnectionId();
            DBUtil db = new DBUtil(connectionid);
            try {
                db.setConnection(this.sapphireConnection);
                String databaseId = this.sapphireConnection.getDatabaseId();
                ArrayList commands = AutomationService.getServerCommands(databaseId);
                if (commands.size() <= 0 || otherServerListCache.size() <= 0) break block8;
                try {
                    String hostid = Configuration.getInstance().getHostid();
                    for (int i = 0; i < commands.size(); ++i) {
                        Command command = (Command)commands.get(i);
                        String callstmt = "{call lv_app" + (this.connectionInfo.isOracle() ? "." : "_") + "commandinsert( ?, ?, ?, ? ) }";
                        CallableStatement cs = db.prepareCall(callstmt);
                        cs.setString(1, hostid);
                        cs.setString(2, command.targetHostid);
                        cs.setString(3, command.command);
                        cs.setString(4, command.commandValues);
                        cs.executeUpdate();
                        db.closeCall();
                    }
                }
                catch (SapphireException e) {
                    Trace.logError("Unable to send server commands " + e.getMessage(), e);
                }
                String string = commands.size() + " messages broadcast";
                return string;
            }
            catch (Exception e) {
                Trace.logError("Failed to send server commands", e);
            }
            finally {
                db.reset();
            }
        }
        return "";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String processServerCommands() {
        if (AutomationService.getOtherServerList(this.sapphireConnection.getDatabaseId()).size() > 0) {
            String connectionid = this.sapphireConnection.getConnectionId();
            DBUtil db = new DBUtil(connectionid);
            try {
                db.setConnection(this.sapphireConnection);
                String databaseid = this.sapphireConnection.getDatabaseId();
                int commandsequence = lastCommandSequence.containsKey(databaseid) ? lastCommandSequence.get(databaseid) : -1;
                String thisHostid = Configuration.getInstance().getHostid();
                db.createPreparedResultSet("SELECT * FROM servercommand WHERE commandsequence>? AND fromhostid<>? AND ( tohostid = ? OR tohostid is null ) ORDER BY commandsequence", new Object[]{commandsequence, thisHostid, thisHostid});
                DataSet ds = new DataSet(db.getResultSet());
                if (ds.size() > 0) {
                    lastCommandSequence.put(databaseid, ds.getInt(ds.size() - 1, "commandsequence"));
                    for (int i = 0; i < ds.size(); ++i) {
                        String key;
                        String cacheName;
                        String command = ds.getValue(i, "command");
                        String commandValues = ds.getValue(i, "commandvalues");
                        String fromHostid = ds.getValue(i, "fromhostid");
                        PropertyList props = new PropertyList();
                        props.setPropertyList(commandValues.startsWith("<propertylist") ? commandValues : "");
                        if (command.equalsIgnoreCase(COMMAND_RESETCACHE)) {
                            CacheUtil.resetCache(databaseid, false);
                            ConfigService.resetCache();
                            DOMUtil.cache.clear();
                            continue;
                        }
                        if (command.equalsIgnoreCase(COMMAND_REMOVEFROMCACHE)) {
                            cacheName = props.getProperty("name");
                            key = props.getProperty("key");
                            CacheUtil.remove(databaseid, cacheName, key, false);
                            continue;
                        }
                        if (command.equalsIgnoreCase(COMMAND_REMOVEALLSTARTSWITHFROMCACHE)) {
                            cacheName = props.getProperty("name");
                            key = props.getProperty("key");
                            CacheUtil.removeAllStartWith(databaseid, cacheName, key, false);
                            continue;
                        }
                        if (command.equalsIgnoreCase(COMMAND_REMOVEALLENDSWITHFROMCACHE)) {
                            cacheName = props.getProperty("name");
                            key = props.getProperty("key");
                            CacheUtil.removeAllEndWith(databaseid, cacheName, key, false);
                            continue;
                        }
                        if (command.equalsIgnoreCase(COMMAND_CLEARCACHE)) {
                            cacheName = commandValues;
                            CacheUtil.clear(databaseid, cacheName, false);
                            continue;
                        }
                        if (command.equalsIgnoreCase(COMMAND_NOTIFICATION)) {
                            NotifyManager.notify(props);
                            continue;
                        }
                        if (command.equalsIgnoreCase(COMMAND_UPDATEUNITCONVERSIONCACHE)) {
                            String unitsid = commandValues;
                            QueryProcessor qp = new QueryProcessor(connectionid);
                            UnitsUtil.updateUnitConversationCache(databaseid, qp, unitsid);
                            continue;
                        }
                        if (command.equalsIgnoreCase(COMMAND_REQUESTLAMSTATS)) {
                            LAM lam = AutomationService.getLAM(databaseid);
                            LAMStats stats = lam.getStats();
                            TranslationProcessor tp = new TranslationProcessor(connectionid);
                            String html = GetLAMStats.statsToHTML(stats, tp);
                            AutomationService.broadcastServerCommand(databaseid, COMMAND_RESPONDLAMSTATS, html);
                            continue;
                        }
                        if (command.equalsIgnoreCase(COMMAND_RESPONDLAMSTATS)) {
                            serverLamStats.put(databaseid + ";" + fromHostid, commandValues);
                            continue;
                        }
                        if (command.equalsIgnoreCase(COMMAND_CLEARCONNECTIONFROMCACHE)) {
                            SecurityService.clearConnectionFromCache(commandValues);
                            continue;
                        }
                        if (!command.equalsIgnoreCase(COMMAND_GENERATESNAPSHOT)) continue;
                        String hostid = Configuration.getInstance().getHostid();
                        String workingFolder = LogViewerUtil.getLogFolder() + "/working";
                        File temp = new File(workingFolder);
                        if (!temp.exists()) {
                            temp.mkdir();
                        }
                        Path generateFolder = Files.createTempDirectory(Paths.get(workingFolder, new String[0]), "generatesnapshot", new FileAttribute[0]);
                        LogViewerUtil.generateSnapshot(connectionid, generateFolder.toFile().getAbsolutePath(), props.getProperty("sysuserid"), props.getProperty("snapshotid"), props.getProperty("description"), props.getProperty("timemode"), props.getProperty("fromdate"), props.getProperty("todate"), hostid, props.getProperty("dbid"), props.getProperty("source"), props.getProperty("logType"));
                    }
                }
            }
            catch (Exception e) {
                Trace.logError("Failed to process server commands", e);
            }
            finally {
                db.reset();
            }
        }
        return "";
    }

    public static String getLamStats(String databaseid, String hostid) {
        return serverLamStats.get(databaseid + ";" + hostid);
    }

    public static boolean isPrimaryAutomationServer(String databaseid) {
        return isPrimaryAutomationServer.get(databaseid) == null ? false : isPrimaryAutomationServer.get(databaseid);
    }

    public static boolean isAutomationServer(String databaseid) {
        return isAutomationServer.get(databaseid) == null ? true : isAutomationServer.get(databaseid);
    }

    public static boolean isCluster(String databaseid) {
        List list = otherServerListCache.get(databaseid);
        return list != null && list.size() > 0;
    }

    public static List<Server> getOtherServerList(String databaseid) {
        List list = otherServerListCache.get(databaseid);
        return list == null ? new ArrayList() : list;
    }

    public static List<Server> getOtherAutomationServerList(String databaseid) {
        List list = otherAutomationServerListCache.get(databaseid);
        return list == null ? new ArrayList() : list;
    }

    public static List<Server> getFullServerList(String databaseid) throws SapphireException {
        ArrayList<Server> list = fullServerListCache.get(databaseid);
        if (list == null) {
            list = new ArrayList<Server>();
            list.add(Configuration.getInstance().getServer());
        }
        return list;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void broadcastServerCommand(String databaseid, String command, String commandValues) {
        if (AutomationService.isCluster(databaseid)) {
            Trace.logDebug("broadcastServerCommand - Is Cluster");
            if (commandValues.length() > 4000) {
                Trace.logError("Automation", "Unable to broadcase server message - too long. " + command + "=" + commandValues.substring(0, 1000) + "...");
            } else {
                HashMap<String, ArrayList<Command>> hashMap = databaseServerCommands;
                synchronized (hashMap) {
                    ArrayList<Command> serverCommands = databaseServerCommands.get(databaseid);
                    if (serverCommands == null) {
                        serverCommands = new ArrayList();
                        databaseServerCommands.put(databaseid, serverCommands);
                    }
                    Command c = new Command(command, commandValues);
                    serverCommands.add(c);
                    Trace.logDebug("serverCommand '" + command + "' with values '" + commandValues + "' added.");
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void broadcastServerCommandAutomationOnly(String databaseid, String command, String commandValues) {
        if (AutomationService.isCluster(databaseid)) {
            if (commandValues.length() > 4000) {
                Trace.logError("Automation", "Unable to broadcase server message - too long. " + command + "=" + commandValues.substring(0, 1000) + "...");
            } else {
                HashMap<String, ArrayList<Command>> hashMap = databaseServerCommands;
                synchronized (hashMap) {
                    ArrayList<Command> serverCommands = databaseServerCommands.get(databaseid);
                    if (serverCommands == null) {
                        serverCommands = new ArrayList();
                        databaseServerCommands.put(databaseid, serverCommands);
                    }
                    List<Server> otherAutomations = AutomationService.getOtherAutomationServerList(databaseid);
                    for (Server server : otherAutomations) {
                        Command c = new Command(server.hostid, command, commandValues);
                        serverCommands.add(c);
                    }
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void sendServerCommand(String databaseid, String targetHostid, String command, String commandValues) {
        if (AutomationService.isCluster(databaseid)) {
            if (commandValues.length() > 4000) {
                Trace.logError("Automation", "Unable to broadcase server message - too long. " + command + "=" + commandValues.substring(0, 1000) + "...");
            } else {
                HashMap<String, ArrayList<Command>> hashMap = databaseServerCommands;
                synchronized (hashMap) {
                    ArrayList<Command> serverCommands = databaseServerCommands.get(databaseid);
                    if (serverCommands == null) {
                        serverCommands = new ArrayList();
                        databaseServerCommands.put(databaseid, serverCommands);
                    }
                    Command c = new Command(targetHostid, command, commandValues);
                    serverCommands.add(c);
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void reallocateCollectors() throws SapphireException {
        String databaseId = this.sapphireConnection.getDatabaseId();
        if (AutomationService.isPrimaryAutomationServer(databaseId)) {
            ArrayList<Server> automationServers = new ArrayList<Server>(AutomationService.getOtherAutomationServerList(databaseId));
            Server thisServer = Configuration.getInstance().getServer();
            automationServers.add(thisServer);
            DataSet collectors = this.getQueryProcessor().getPreparedSqlDataSet("ReallocateCollectors_nolog", "SELECT sdmscollectorid, automationweight, (SELECT count(*) FROM instrument i WHERE i.sdmscollectorid=sdmscollector.sdmscollectorid ) instrumentcount, targethostname FROM sdmscollector WHERE internalflag=? AND ( disabledflag is null OR disabledflag='N' )", new String[]{"Y"});
            collectors.sort("automationweight d, instrumentcount d, sdmscollectorid");
            for (int i = 0; i < collectors.size(); ++i) {
                Collections.sort(automationServers);
                Server chosen = (Server)automationServers.get(0);
                chosen.addCollectorWeight(collectors.getInt(i, "automationweight", collectors.getInt(i, "instrumentcount", 1)));
                collectors.setString(i, "_newtargethostname", chosen.hostid);
            }
            DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
            try {
                db.setConnection(this.sapphireConnection);
                for (int i = 0; i < collectors.size(); ++i) {
                    String collectorid = collectors.getString(i, "sdmscollectorid");
                    String targetHostname = collectors.getString(i, "_newtargethostname");
                    db.executePreparedUpdate("UPDATE sdmscollector SET targethostname=? WHERE ( targethostname is null OR targethostname<>? ) AND sdmscollectorid=?", new String[]{targetHostname, targetHostname, collectorid});
                }
            }
            catch (Exception e) {
                Trace.logError("Failed to determine the list of other servers.", e);
            }
            finally {
                db.reset();
            }
        }
    }

    public void startStopCollectors() throws SapphireException {
        String hostid = Configuration.getInstance().getHostid();
        String currentdateclause = this.sapphireConnection.isSqlServer() ? "getdate() sysdate" : "sysdate";
        DataSet collectors = this.getQueryProcessor().getPreparedSqlDataSet("StartStopCollectors_nolog", "SELECT sdmscollectorid, lastpingdt, " + currentdateclause + " FROM sdmscollector WHERE internalflag=? AND ( disabledflag is null OR disabledflag='N' ) AND targethostname=?", new String[]{"Y", hostid});
        LAM lam = AutomationService.getLAM(this.sapphireConnection.getDatabaseId());
        ArrayList<SDMSCollectorInternalHolder> currentCollectors = new ArrayList<SDMSCollectorInternalHolder>(lam.getSdmsCollectorHolders());
        for (SDMSCollectorInternalHolder collector : currentCollectors) {
            String collectorid = collector.getCollectorid();
            int row = collectors.findRow("sdmscollectorid", collectorid);
            if (row == -1) {
                lam.stopCollector(collectorid);
                continue;
            }
            collectors.deleteRow(row);
        }
        PropertyList SDMSPolicy = new ConfigurationProcessor(this.sapphireConnection.getConnectionId()).getPolicy("SDMSPolicy", "Sapphire Custom");
        int pingIntervalSeconds = Integer.parseInt(SDMSPolicy.getPropertyListNotNull("collectordefaults").getProperty("serverpinginterval", "10"));
        for (int i = 0; i < collectors.size(); ++i) {
            String collectorid = collectors.getString(i, "sdmscollectorid");
            Calendar lastPingDt = collectors.getCalendar(i, "lastpingdt");
            Calendar sysdate = collectors.getCalendar(i, "sysdate");
            if (lastPingDt != null && lastPingDt.getTimeInMillis() >= sysdate.getTimeInMillis() - (long)(2 * pingIntervalSeconds * 1000)) continue;
            try {
                lam.startCollector(collectorid);
                continue;
            }
            catch (LAMException e) {
                SDMSUtil.raiseSDMSAlert(this.getActionProcessor(), "LV_SDMSCollector", collectorid, "SDMS Startup", "Failure", false, false, "Failed to start Internal Collector", e.getMessage());
            }
        }
    }

    public void importDataCaptureFolders() {
        DataSet collectors = this.getQueryProcessor().getPreparedSqlDataSet("ImportDataCaptureFolders_nolog", "SELECT sdmscollectorid, internalflag, storagepathremote FROM sdmscollector WHERE ( disabledflag is null OR disabledflag='N' ) AND (storagemodeflag=? OR allowisolatedflag='Y')", new String[]{"I"});
        String retentionTime = "1 month";
        try {
            PropertyList SDMSPolicy = new ConfigurationProcessor(this.sapphireConnection.getConnectionId()).getPolicy("SDMSPolicy", "Sapphire Custom");
            retentionTime = SDMSPolicy.getPropertyListNotNull("automationsettings").getProperty("indirectfilesfoldersretentiontime", "1 month");
        }
        catch (SapphireException e) {
            Trace.logError("HouseKeeping Storage Folders", (Object)"Failed to get SDMSPolicy for Indirect Files/Folders retention time", e);
        }
        if (collectors != null && collectors.size() > 0) {
            for (int i = 0; i < collectors.size(); ++i) {
                String collectorid = collectors.getValue(i, "sdmscollectorid");
                try {
                    String path = collectors.getValue(i, "storagepathremote");
                    if (path.length() > 0) {
                        Path storageRoot = Paths.get(path, new String[0]);
                        if (!storageRoot.toFile().exists()) {
                            throw new SapphireException("Unable to find storage root " + path + " for collector " + collectorid);
                        }
                        File[] files = storageRoot.toFile().listFiles();
                        for (int j = 0; j < files.length; ++j) {
                            String fname;
                            File file = files[j];
                            String string = fname = file.isDirectory() ? file.getName() : FileManager.getFileName(file.getName(), false).toLowerCase();
                            if (fname.endsWith("_captured")) {
                                PropertyList props = new PropertyList();
                                props.setProperty("actionid", "ImportDataCaptureFolder");
                                props.setProperty("actionversionid", "1");
                                props.setProperty("processname", fname);
                                props.setProperty("path", file.getAbsolutePath());
                                props.setProperty("collectorid", collectorid);
                                this.getActionProcessor().processActionClass(AddToDoListEntry.class.getName(), props, true);
                                continue;
                            }
                            if (!fname.endsWith("_stored")) continue;
                            this.houseKeeping(file, retentionTime);
                        }
                    }
                }
                catch (Exception e) {
                    SDMSUtil.raiseSDMSAlert(this.getActionProcessor(), "LV_SDMSCollector", collectorid, "SDMS Startup", "Failure", false, "Failed to initiate ImportDataCaptureFolders automation", e.getMessage());
                }
                DateTimeUtil dtu = new DateTimeUtil();
                Timestamp deleteCommands = dtu.getTimestamp("now-1d");
                this.getQueryProcessor().execPreparedUpdate("DELETE FROM sdmscollectorcommand WHERE sdmscollectorid=? AND commanddt < ?", new Object[]{collectorid, deleteCommands});
            }
        }
    }

    private void houseKeeping(File file, String retentionRule) throws Exception {
        if (!retentionRule.equalsIgnoreCase("Keep Forever")) {
            BasicFileAttributes attr = Files.readAttributes(Paths.get(file.toString(), new String[0]), BasicFileAttributes.class, new LinkOption[0]);
            long creationTime = attr.creationTime().toMillis();
            Calendar createDt = Calendar.getInstance();
            createDt.setTimeInMillis(creationTime);
            if (retentionRule.equalsIgnoreCase("1 day")) {
                createDt.add(5, 1);
            } else if (retentionRule.equalsIgnoreCase("2 days")) {
                createDt.add(5, 2);
            } else if (retentionRule.equalsIgnoreCase("1 week")) {
                createDt.add(4, 1);
            } else if (retentionRule.equalsIgnoreCase("2 week")) {
                createDt.add(4, 2);
            } else if (retentionRule.equalsIgnoreCase("1 month")) {
                createDt.add(2, 1);
            } else if (retentionRule.equalsIgnoreCase("1 year")) {
                createDt.add(1, 1);
            } else {
                createDt.add(2, 1);
            }
            Calendar currentDt = Calendar.getInstance();
            currentDt.setTimeInMillis(System.currentTimeMillis());
            if (currentDt.after(createDt)) {
                FileUtils.forceDelete((File)file);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static ArrayList getServerCommands(String databaseid) {
        ArrayList copyCommands;
        ArrayList<Command> serverCommands = databaseServerCommands.get(databaseid);
        if (serverCommands != null && serverCommands.size() > 0) {
            HashMap<String, ArrayList<Command>> hashMap = databaseServerCommands;
            synchronized (hashMap) {
                copyCommands = (ArrayList)serverCommands.clone();
                serverCommands.clear();
            }
        } else {
            copyCommands = new ArrayList();
        }
        return copyCommands;
    }

    static {
        lams = new HashMap();
        otherAutomationServerListCache = new HashMap();
        otherServerListCache = new HashMap();
        fullServerListCache = new HashMap();
        isPrimaryAutomationServer = new HashMap();
        isAutomationServer = new HashMap();
        databaseServerCommands = new HashMap();
        lastCommandSequence = new HashMap();
        serverLamStats = new HashMap();
    }

    public static class Command {
        public String targetHostid = null;
        public String command;
        public String commandValues;

        public Command(String targetHostid, String command, String commandValues) {
            this.targetHostid = targetHostid;
            this.command = command;
            this.commandValues = commandValues;
        }

        public Command(String command, String commandValues) {
            this.command = command;
            this.commandValues = commandValues;
        }
    }
}

