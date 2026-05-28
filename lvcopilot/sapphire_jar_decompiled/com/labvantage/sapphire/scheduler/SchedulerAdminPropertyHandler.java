/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.scheduler;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import com.labvantage.sapphire.scheduler.ScheduleEvent;
import com.labvantage.sapphire.scheduler.ScheduleNodeUtil;
import com.labvantage.sapphire.services.ActionService;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ScheduleService;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.io.Reader;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SchedulerAdminPropertyHandler
extends PropertyHandler {
    private static final String LOGNAME = "SchedulerAdminPropertyHandler";
    public static final String MODE_GETPLANVALUETREEEXTENDNODE = "getplanvaluetreeextendnode";

    @Override
    public void processProperties(HashMap mapProps) throws SapphireException {
        block38: {
            PropertyList props = (PropertyList)mapProps;
            String mode = props.getProperty("mode");
            String planid = props.getProperty("scheduleplanid");
            String itemid = props.getProperty("scheduleplanitemid");
            DBUtil db = new DBUtil();
            try {
                db.setConnection(this.sapphireConnection);
                if ("executenow".equals(mode)) {
                    String eventdate;
                    boolean noevent = false;
                    if ("true".equals(props.getProperty("noevent"))) {
                        noevent = true;
                        eventdate = props.getProperty("eventdate");
                    } else {
                        eventdate = props.getProperty("eventfromdate");
                    }
                    String eventNum = props.getProperty("eventnum");
                    String eventStatus = props.getProperty("eventstatus");
                    if (eventStatus == null || eventStatus.isEmpty()) {
                        eventStatus = "S";
                    }
                    String instanceCntStr = props.getProperty("instancecount");
                    Integer instanceCount = null;
                    if (instanceCntStr != null && !instanceCntStr.isEmpty()) {
                        try {
                            instanceCount = Integer.valueOf(instanceCntStr);
                        }
                        catch (NumberFormatException e) {
                            instanceCount = null;
                        }
                    }
                    boolean adhocExecution = false;
                    String adhocExecutionStr = props.getProperty("adhocexecution");
                    if ("true".equals(adhocExecutionStr)) {
                        adhocExecution = true;
                    }
                    String monitorGroupFlag = props.getProperty("monitorgroupflag", "R");
                    String monitorGroupId = null;
                    if ((monitorGroupFlag.equals("S") || monitorGroupFlag.equals("E")) && (monitorGroupId = props.getProperty("monitorgroupid")) != null && monitorGroupId.isEmpty()) {
                        monitorGroupId = null;
                    }
                    HashMap newkeyidmap = (HashMap)props.get("newkeyidmap");
                    boolean createMonitorGroup = monitorGroupFlag.equals("R");
                    this.executePlanItemsNow(db, planid, itemid, noevent, eventdate, props.getProperty("eventtodate"), eventNum, eventStatus, adhocExecution, createMonitorGroup, monitorGroupId, instanceCount, newkeyidmap);
                    break block38;
                }
                if ("setvaluetree".equals(mode)) {
                    Trace.logInfo(LOGNAME, "Save task value override...");
                    this.setTaskPropsOverride(db, planid, itemid, props.getProperty("valuetree"));
                    Trace.logInfo(LOGNAME, "Done save task value override.");
                    break block38;
                }
                if ("getconditionvaluetree".equals(mode)) {
                    Trace.logInfo(LOGNAME, "Retrieve task value override...");
                    String conditionid = props.getProperty("conditionid");
                    String propertytreeid = props.getProperty("propertytreeid");
                    props.setProperty("valuetree", this.getConditionProperties(db, planid, conditionid, propertytreeid));
                    Trace.logInfo(LOGNAME, "Done retrieve task value override.");
                    break block38;
                }
                if ("getplanvaluetree".equals(mode)) {
                    Trace.logInfo(LOGNAME, "Retrieve task value override...");
                    String propertytreeid = props.getProperty("propertytreeid");
                    props.setProperty("valuetree", this.getPlanProperties(db, planid, propertytreeid));
                    Trace.logInfo(LOGNAME, "Done retrieve task value override.");
                    break block38;
                }
                if (MODE_GETPLANVALUETREEEXTENDNODE.equals(mode)) {
                    Trace.logInfo(LOGNAME, "Retrieve task value override...");
                    String propertytreeid = props.getProperty("propertytreeid");
                    props.setProperty("extendnodeid", this.getPlanExtendNodeid(db, planid, propertytreeid));
                    Trace.logInfo(LOGNAME, "Done retrieve task value override.");
                    break block38;
                }
                if ("getvaluetree".equals(mode)) {
                    Trace.logInfo(LOGNAME, "Retrieve task value override...");
                    props.setProperty("valuetree", this.getTaskPropsOverride(db, planid, itemid));
                    Trace.logInfo(LOGNAME, "Done retrieve task value override.");
                    break block38;
                }
                if ("saveexclude".equals(mode)) {
                    Trace.logInfo(LOGNAME, "SaveScheduleExclude...");
                    this.saveScheduleExclude(db, props.getProperty("scheduleexcludeid"), props.getProperty("scheduleexcludeitemid"), props.getProperty("excludedt"), props.getProperty("excluderule"));
                    Trace.logInfo(LOGNAME, "Done saveScheduleExclude.");
                    break block38;
                }
                if ("addexclude".equals(mode) || "copyexclude".equals(mode)) {
                    Trace.logInfo(LOGNAME, "AddScheduleExclude...");
                    this.addScheduleExclude(db, props.getProperty("scheduleexcludeid"), props.getProperty("newscheduleexcludeid"), props.getProperty("newscheduleexcludedesc"), mode);
                    Trace.logInfo(LOGNAME, "Done addScheduleExclude.");
                    break block38;
                }
                if ("deleteexclude".equals(mode)) {
                    Trace.logInfo(LOGNAME, "Deleteexclude...");
                    this.deleteScheduleExclude(db, props.getProperty("scheduleexcludeid"));
                    Trace.logInfo(LOGNAME, "Done deleteexclude.");
                    break block38;
                }
                if ("copyitem".equals(mode)) {
                    throw new SapphireException("Copy planitems not supported via old schedule code");
                }
                if ("movescheduleevent".equals(mode)) {
                    Trace.logInfo(LOGNAME, "Move schedule event...");
                    boolean dateOnly = props.getProperty("dateonly", "Y").startsWith("Y");
                    boolean notExists = props.getProperty("addevent", "N").startsWith("Y");
                    this.moveScheduleEvent(db, props.getProperty("selectedeventlist"), props.getProperty("movetodate"), dateOnly, notExists);
                    Trace.logInfo(LOGNAME, "Done move schedule event...");
                    break block38;
                }
                if ("movescheduleeventwithevents".equals(mode)) {
                    Trace.logInfo(LOGNAME, "Move schedule event...");
                    boolean dateOnly = props.getProperty("dateonly", "Y").startsWith("Y");
                    boolean notExists = props.getProperty("addevent", "N").startsWith("Y");
                    ScheduleEvent event = (ScheduleEvent)props.get("event");
                    Calendar moveToDate = (Calendar)props.get("movetodt");
                    Calendar executeDt = (Calendar)props.get("executedt");
                    String auditreason = (String)props.get("auditreason");
                    String auditactivity = (String)props.get("auditactivity");
                    String auditsignedflag = (String)props.get("auditsignedflag");
                    String traceLogId = null;
                    if (auditactivity.length() > 0 || auditreason.length() > 0) {
                        AuditService audit = new AuditService(this.sapphireConnection);
                        try {
                            traceLogId = audit.addTraceLogEntry(auditreason, auditactivity, auditsignedflag, "", "Move Schedule Event", true);
                        }
                        catch (ServiceException e) {
                            throw new SapphireException("Failed to add audit records", e);
                        }
                    }
                    this.moveScheduleEvent2(db, event, moveToDate, executeDt, notExists, traceLogId);
                    Trace.logInfo(LOGNAME, "Done move schedule event...");
                    break block38;
                }
                if ("deletescheduleevent".equals(mode)) {
                    Trace.logInfo(LOGNAME, "Delete schedule event...");
                    this.deleteScheduleEvent(db, props.getProperty("selectedeventlist"));
                    Trace.logInfo(LOGNAME, "Done delete schedule event...");
                } else if ("getscheduleevents".equals(mode)) {
                    Trace.logInfo(LOGNAME, "Get schedule events...");
                    this.getScheduleEvents(db, props);
                    Trace.logInfo(LOGNAME, "Done get schedule events...");
                } else if ("getscheduleexcludes".equals(mode)) {
                    Trace.logInfo(LOGNAME, "Get schedule excludes...");
                    this.getScheduleExcludes(db, props);
                    Trace.logInfo(LOGNAME, "Done get schedule excludes...");
                } else if ("changeeventstatus".equals(mode)) {
                    Trace.logInfo(LOGNAME, "Inactivate schedule events...");
                    ArrayList events = (ArrayList)props.get("events");
                    String newStatus = props.getProperty("neweventstatus");
                    this.changeScheduleEventStatus(db, events, newStatus);
                    Trace.logInfo(LOGNAME, "Done get schedule excludes...");
                } else if ("getplanitemgrcperiod".equals(mode)) {
                    Trace.logInfo(LOGNAME, "Get Plan Item Grace Perioid...");
                    this.getPlanitemGracePerioid(props);
                    Trace.logInfo(LOGNAME, "Done Get Plan Item Grace Perioid...");
                } else {
                    ScheduleNodeUtil treeUtil = new ScheduleNodeUtil(db, props);
                    treeUtil.setConnectionId(this.getConnectionInfo().getConnectionId());
                    treeUtil.handleRequest();
                }
            }
            catch (SapphireException se) {
                throw new SapphireException("GENERAL_ERROR", "Failed to handle web request", se);
            }
            finally {
                db.reset();
            }
        }
    }

    private void getScheduleEvents(DBUtil dbu, PropertyList properties) throws SapphireException {
        ScheduleService scheduler = new ScheduleService(this.sapphireConnection);
        try {
            DataSet eventlist = scheduler.getScheduleEvents(properties);
            properties.put("eventlist", eventlist);
        }
        catch (ServiceException se) {
            Trace.logError(LOGNAME, (Object)("Get Schedule Events Failed: " + se.getMessage()), se);
            throw new SapphireException(se);
        }
    }

    private void getScheduleExcludes(DBUtil dbu, PropertyList properties) throws SapphireException {
        ScheduleService scheduler = new ScheduleService(this.sapphireConnection);
        try {
            DataSet eventlist = scheduler.getScheduleExcludes(properties);
            properties.put("eventlist", eventlist);
        }
        catch (ServiceException se) {
            Trace.logError(LOGNAME, (Object)("Get Schedule Excludes Failed: " + se.getMessage()), se);
            throw new SapphireException(se);
        }
    }

    private void executePlanItemsNow(DBUtil dbu, String planid, String planitemid, boolean noevent, String eventdate, String eventtodate, String eventNum, String eventStatus, boolean adhocexecution, boolean createMonitorGroup, String monitorGroupId, Integer instanceCount, HashMap<String, ArrayList<String>> newKeyidMap) throws SapphireException {
        try {
            SafeSQL safeSQL = new SafeSQL();
            dbu.createPreparedResultSet("SELECT distinct spi.scheduleplanid, spi.scheduleplanitemid, spi.propertytreeid, spi.scheduletasknodeid, pt.objectname FROM scheduleplanitem spi, propertytree pt WHERE spi.propertytreeid = pt.propertytreeid  AND spi.scheduleplanid=" + safeSQL.addVar(planid) + " " + (planitemid.equals("all") ? "" : " and scheduleplanitemid in (" + safeSQL.addIn(planitemid, ";") + ")") + " ORDER BY spi.scheduleplanitemid", safeSQL.getValues());
            DataSet duePlanItems = new DataSet(dbu.getResultSet());
            Trace.logInfo(LOGNAME, duePlanItems.size() + " distinct plan items found for execution");
            ScheduleService scheduler = new ScheduleService(this.sapphireConnection);
            for (int i = 0; i < duePlanItems.size(); ++i) {
                scheduler.executeNow(planid, duePlanItems.getString(i, "scheduleplanitemid"), duePlanItems.getString(i, "propertytreeid"), duePlanItems.getString(i, "scheduletasknodeid"), duePlanItems.getString(i, "objectname"), noevent, eventdate, eventtodate, eventNum, eventStatus, adhocexecution, createMonitorGroup, monitorGroupId, instanceCount, newKeyidMap);
            }
        }
        catch (ServiceException se) {
            Trace.logError(LOGNAME, (Object)("Schedule On Demand Execution Failed: " + se.getMessage()), se);
            throw new SapphireException(se);
        }
    }

    private void setTaskPropsOverride(DBUtil db, String planid, String itemid, String valuetree) {
        try {
            Timestamp now = DateTimeUtil.getNowTimestamp();
            db.executePreparedUpdate("UPDATE scheduleplanitem SET modby=?, moddt = ? WHERE scheduleplanid=? AND scheduleplanitemid=?", new Object[]{this.connectionInfo.getSysuserId(), now, planid, itemid});
            if (this.connectionInfo.isOracle()) {
                db.updateClob("scheduleplanitem", "valuetree", valuetree, new String[]{"scheduleplanid", "scheduleplanitemid"}, new String[]{planid, itemid});
            } else {
                String sql = "UPDATE scheduleplanitem set valuetree = ? WHERE scheduleplanid = ? AND scheduleplanitemid=? ";
                PreparedStatement ps = db.getConnection().prepareStatement(sql);
                ps.setCharacterStream(1, (Reader)new StringReader(valuetree), valuetree.length());
                ps.setString(2, planid);
                ps.setString(3, itemid);
                ps.executeUpdate();
                ps.close();
            }
        }
        catch (Exception e) {
            Trace.logError(LOGNAME, (Object)("Could not set task value override: " + e.getMessage()), e);
        }
    }

    private String getTaskPropsOverride(DBUtil dbu, String planid, String itemid) {
        String ptree = null;
        try {
            ScheduleService scheduler = new ScheduleService(new SapphireConnection(dbu.getConnection(), this.connectionInfo));
            ptree = scheduler.getTaskPropsOverride(planid, itemid);
        }
        catch (ServiceException se) {
            Trace.logError(LOGNAME, (Object)("Could not lookup the valuetree: " + se.getMessage()), se);
        }
        return ptree;
    }

    private String getConditionProperties(DBUtil dbu, String planid, String conditionid, String propertytreeid) throws SapphireException {
        String sql = "SELECT valuetree FROM scheduleconditiondefaults WHERE scheduleplanid= ? and scheduleconditionid = ? and propertytreeid= ?";
        dbu.createPreparedResultSet(sql, new Object[]{planid, conditionid, propertytreeid});
        String ptree = null;
        if (dbu.getNext()) {
            ptree = dbu.getClob("valuetree");
        }
        dbu.closeResultSet();
        return ptree == null || ptree.trim().length() == 0 ? "<propertylist/>" : ptree;
    }

    private void saveScheduleExclude(DBUtil db, String scheduleexcludeid, String scheduleexcludeitemid, String excludedt, String excluderule) throws SapphireException {
        DataSet insertDs = new DataSet();
        if (scheduleexcludeitemid != null && scheduleexcludeitemid.length() > 0) {
            insertDs.addColumnValues("scheduleexcludeid", 0, scheduleexcludeid, ";");
            insertDs.addColumnValues("scheduleexcludeitemid", 0, scheduleexcludeitemid, ";");
            insertDs.padColumn("scheduleexcludeid");
            insertDs.addColumnValues("excludedt", 2, excludedt, ";");
        }
        if (excluderule != null && excluderule.length() > 0) {
            String[] excludedays = StringUtil.split(excluderule, ";");
            insertDs.addColumn("scheduleexcludeid", 0);
            insertDs.addColumn("scheduleexcludeitemid", 0);
            insertDs.addColumn("excluderule", 0);
            for (int i = 0; i < excludedays.length; ++i) {
                int row = insertDs.addRow();
                insertDs.setString(row, "scheduleexcludeid", scheduleexcludeid);
                insertDs.setString(row, "scheduleexcludeitemid", excludedays[i]);
                insertDs.setString(row, "excluderule", excludedays[i]);
            }
        }
        db.executePreparedUpdate("DELETE FROM scheduleexcludeitem WHERE scheduleexcludeid=?", new Object[]{scheduleexcludeid});
        if (insertDs.getRowCount() > 0) {
            insertDs.addColumn("createdt", 2);
            insertDs.setDate(-1, "createdt", DateTimeUtil.getNowCalendar());
            insertDs.addColumn("createby", 0);
            insertDs.setValue(-1, "createby", this.connectionInfo.getSysuserId());
            insertDs.addColumn("createtool", 0);
            insertDs.setValue(-1, "createtool", this.connectionInfo.getTool());
            insertDs.addColumn("moddt", 2);
            insertDs.setDate(-1, "moddt", DateTimeUtil.getNowCalendar());
            insertDs.addColumn("modby", 0);
            insertDs.setValue(-1, "modby", this.connectionInfo.getSysuserId());
            insertDs.addColumn("modtool", 0);
            insertDs.setValue(-1, "modtool", this.connectionInfo.getTool());
            DataSetUtil.insert(db, insertDs, "scheduleexcludeitem");
        }
        CacheUtil.remove(this.sapphireConnection.getDatabaseId(), "ScheduleExcludes", scheduleexcludeid, true);
    }

    private void addScheduleExclude(DBUtil db, String scheduleexcludeid, String newscheduleexcludeid, String newscheduleexcludedesc, String mode) throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "ScheduleExclude");
        props.put("keyid1", newscheduleexcludeid);
        props.put("scheduleexcludedesc", newscheduleexcludedesc);
        try {
            ActionService actionService = new ActionService(this.sapphireConnection);
            actionService.processAction("AddSDI", "1", props);
        }
        catch (ServiceException e) {
            throw new SapphireException(e);
        }
        if ("copyexclude".equals(mode)) {
            Timestamp now = DateTimeUtil.getNowTimestamp();
            String tool = this.sapphireConnection.getTool();
            db.executePreparedUpdate("INSERT INTO scheduleexcludeitem( scheduleexcludeid, scheduleexcludeitemid, scheduleexcludeitemdesc, excludedt, excluderule, usersequence, createdt, createby, createtool, modby, modtool, moddt )  SELECT '" + newscheduleexcludeid + "', scheduleexcludeitemid, scheduleexcludeitemdesc, excludedt, excluderule, usersequence, {ts '" + now.toString() + "'}, '" + this.connectionInfo.getSysuserId() + "', '" + tool + "', '" + this.connectionInfo.getSysuserId() + "', '" + tool + "', {ts '" + now.toString() + "'} FROM scheduleexcludeitem WHERE scheduleexcludeid=?", new Object[]{scheduleexcludeid});
        }
    }

    private void deleteScheduleExclude(DBUtil db, String scheduleexcludeid) throws SapphireException {
        Object[] vars = new Object[]{scheduleexcludeid};
        db.executePreparedUpdate("DELETE FROM scheduleplanexclude WHERE scheduleexcludeid=?", vars);
        db.executePreparedUpdate("DELETE FROM scheduleexcludeitem WHERE scheduleexcludeid=?", vars);
        db.executePreparedUpdate("DELETE FROM scheduleexclude WHERE scheduleexcludeid=?", vars);
        CacheUtil.remove(this.sapphireConnection.getDatabaseId(), "ScheduleExcludes", scheduleexcludeid, true);
    }

    private void moveScheduleEvent(DBUtil dbu, String selectedeventlist, String movetodate, boolean dateOnly, boolean notExists) throws SapphireException {
        try {
            DateTimeUtil dtu = new DateTimeUtil(this.connectionInfo);
            String[] events = StringUtil.split(selectedeventlist, "%3B");
            for (int i = 0; i < events.length; ++i) {
                String[] eventStr = StringUtil.split(events[i], "|");
                Calendar movetoCal = dtu.getCalendar(movetodate);
                Calendar eventCal = dtu.getCalendar(eventStr[2]);
                Calendar executedtCal = dtu.getCalendar(eventStr[4]);
                long executeahead = eventCal.getTime().getTime() - executedtCal.getTime().getTime();
                if (dateOnly) {
                    movetoCal.set(10, eventCal.get(10));
                    movetoCal.set(12, eventCal.get(12));
                    movetoCal.set(13, eventCal.get(13));
                }
                Calendar newExecuteDt = Calendar.getInstance();
                newExecuteDt.setTimeInMillis(movetoCal.getTime().getTime() - executeahead);
                String scheduleplanid = eventStr[0];
                String scheduleplanitemid = eventStr[1];
                ScheduleEvent event = new ScheduleEvent(scheduleplanid, scheduleplanitemid, eventCal);
                event.setOriginalEventDt(eventCal);
                event.setEventNum(Integer.valueOf(eventStr[3]));
                this.moveScheduleEvent2(dbu, event, movetoCal, newExecuteDt, notExists, null);
            }
        }
        catch (Exception e) {
            Trace.logError(LOGNAME, (Object)("Move Events Failed: " + e.getMessage()), e);
            throw new SapphireException(e.getMessage(), e);
        }
    }

    private void moveScheduleEvent2(DBUtil dbu, ScheduleEvent event, Calendar moveToDate, Calendar executeDt, boolean notExists, String traceLogId) throws SapphireException {
        try {
            DateTimeUtil dtu = new DateTimeUtil(this.connectionInfo);
            Timestamp now = DateTimeUtil.getNowTimestamp();
            String currentUser = this.sapphireConnection.getSysuserId();
            String tool = this.sapphireConnection.getTool();
            PreparedStatement update = dbu.prepareStatement("UPDATE scheduleevent set eventdt=?, eventnum=?, executedt=?, moddt = ?, modby = ?, modtool = ?, tracelogid = ?  where scheduleplanid=? AND scheduleplanitemid=? AND eventdt=? AND eventnum=?");
            Timestamp eventdt = new Timestamp(event.getEventDt().getTime().getTime());
            Timestamp movetoeventdt = new Timestamp(moveToDate.getTime().getTime());
            Timestamp executedt = new Timestamp(executeDt.getTime().getTime());
            dbu.createPreparedResultSet("select", "SELECT scheduleplanid, scheduleplanitemid, eventdt, eventnum from scheduleevent where scheduleplanid=? AND scheduleplanitemid=? AND eventdt=? order by eventnum desc", new Object[]{event.getSchedulePlanid(), event.getSchedulePlanitemid(), movetoeventdt});
            int newEventnum = 1;
            if (dbu.getNext("select")) {
                newEventnum = dbu.getInt("select", "eventnum") + 1;
            }
            if (notExists) {
                ScheduleService scheduler = new ScheduleService(this.sapphireConnection);
                event.setEventStatus("S");
                event.setEventNum(newEventnum);
                scheduler.addScheduledEvent(event, false);
            } else {
                update.setTimestamp(1, movetoeventdt);
                update.setInt(2, newEventnum);
                update.setTimestamp(3, executedt);
                update.setTimestamp(4, now);
                update.setString(5, currentUser);
                update.setString(6, tool);
                update.setString(7, traceLogId);
                update.setString(8, event.getSchedulePlanid());
                update.setString(9, event.getSchedulePlanitemid());
                update.setTimestamp(10, eventdt);
                update.setInt(11, event.getEventNum());
                update.execute();
                int rowCount = update.getUpdateCount();
                if (rowCount != 1) {
                    throw new SapphireException("Update not successful");
                }
            }
        }
        catch (Exception e) {
            Trace.logError(LOGNAME, (Object)("Move Events Failed: " + e.getMessage()), e);
            throw new SapphireException(e.getMessage(), e);
        }
    }

    private String getPlanProperties(DBUtil dbu, String planid, String propertytreeid) throws SapphireException {
        String sql = "SELECT valuetree FROM scheduleplandefaults WHERE scheduleplanid= ? and propertytreeid= ?";
        dbu.createPreparedResultSet(sql, new Object[]{planid, propertytreeid});
        String ptree = null;
        if (dbu.getNext()) {
            ptree = dbu.getClob("valuetree");
        }
        dbu.closeResultSet();
        return ptree == null || ptree.trim().length() == 0 ? "<propertylist/>" : ptree;
    }

    private String getPlanExtendNodeid(DBUtil dbu, String planid, String propertytreeid) throws SapphireException {
        String sql = "SELECT extendnodeid FROM scheduleplandefaults WHERE scheduleplanid= ? and propertytreeid= ?";
        dbu.createPreparedResultSet(sql, new Object[]{planid, propertytreeid});
        String nodeid = null;
        if (dbu.getNext()) {
            nodeid = dbu.getString("extendnodeid");
        }
        dbu.closeResultSet();
        return nodeid == null ? "(root)" : nodeid;
    }

    private void deleteScheduleEvent(DBUtil dbu, String selectedeventlist) throws SapphireException {
    }

    private void changeScheduleEventStatus(DBUtil dbu, ArrayList<ScheduleEvent> eventsArr, String newStatus) throws SapphireException {
        try {
            Timestamp now = DateTimeUtil.getNowTimestamp();
            String currentUser = this.sapphireConnection.getSysuserId();
            String tool = this.sapphireConnection.getTool();
            PreparedStatement update = dbu.prepareStatement("UPDATE scheduleevent set eventstatus=?, moddt = ?, modby = ?, modtool = ?  where scheduleplanid=? AND scheduleplanitemid=? AND eventdt=? AND eventnum=?");
            for (ScheduleEvent event : eventsArr) {
                Calendar eventCal = event.getEventDt();
                Timestamp eventdt = new Timestamp(eventCal.getTime().getTime());
                String scheduleplanid = event.getSchedulePlanid();
                String scheduleplanitemid = event.getSchedulePlanitemid();
                String eventstatus = event.getEventStatus();
                if (eventstatus.equals("P")) {
                    dbu.createPreparedResultSet("select", "SELECT scheduleplanid, scheduleplanitemid, eventdt, eventnum from scheduleevent where scheduleplanid=? AND scheduleplanitemid=? AND eventdt=? order by eventnum desc", new Object[]{scheduleplanid, scheduleplanitemid, eventdt});
                    int eventnum = 1;
                    if (dbu.getNext("select")) {
                        eventnum = dbu.getInt("select", "eventnum") + 1;
                    }
                    ScheduleService scheduler = new ScheduleService(this.sapphireConnection);
                    ScheduleEvent newEvent = new ScheduleEvent(scheduleplanid, scheduleplanitemid, eventCal, event.getTimeZone());
                    newEvent.setOriginalEventDt(eventCal);
                    newEvent.setEventStatus(newStatus);
                    newEvent.setEventNum(eventnum);
                    scheduler.addScheduledEvent(newEvent, false);
                    continue;
                }
                update.setString(1, newStatus);
                update.setTimestamp(2, now);
                update.setString(3, currentUser);
                update.setString(4, tool);
                update.setString(5, scheduleplanid);
                update.setString(6, scheduleplanitemid);
                update.setTimestamp(7, eventdt);
                update.setInt(8, event.getEventNum());
                update.execute();
                int rowCount = update.getUpdateCount();
                if (rowCount == 1) continue;
                throw new SapphireException("Update not successful");
            }
        }
        catch (Exception e) {
            Trace.logError(LOGNAME, (Object)("Inactivate Events Failed: " + e.getMessage()), e);
            throw new SapphireException(e.getMessage(), e);
        }
    }

    private void getPlanitemGracePerioid(HashMap props) {
        ScheduleService scheduler = new ScheduleService(this.sapphireConnection);
        String scheduleplanid = (String)props.get("scheduleplanid");
        String scheduleplanitemid = (String)props.get("scheduleplanitemid");
        String propertytreeid = (String)props.get("propertytreeid");
        String scheduletasknodeid = (String)props.get("scheduletasknodeid");
        Calendar startDt = (Calendar)props.get("startdt");
        Calendar originaleventdt = (Calendar)props.get("originaleventdt");
        HashMap<String, Calendar> returnvalues = scheduler.calculateGRCPerioidForPlanItem(scheduleplanid, scheduleplanitemid, startDt, propertytreeid, scheduletasknodeid, originaleventdt, true);
        props.put("windowstartdt", returnvalues.get("windowstartdt"));
        props.put("windowenddt", returnvalues.get("windowenddt"));
    }
}

