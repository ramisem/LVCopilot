/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.IncidentUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.eln.GenerateWorkorderWorksheet;
import com.labvantage.sapphire.actions.wap.UpdateActivityCompleteCount;
import com.labvantage.sapphire.admin.ddt.InstrumentUtil;
import com.labvantage.sapphire.scheduler.BaseScheduleTask;
import com.labvantage.sapphire.scheduler.ScheduleEvent;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.stability.PlanItem;
import com.labvantage.sapphire.stability.ScheduleGrid;
import com.labvantage.sapphire.stability.task.GridTask;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.graceperiod.GRCPeriodUtil;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.QueryProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.error.ErrorHandler;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class WorkOrderSDC
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 98436 $";

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    @Override
    public void preAdd(SDIData sdidata, PropertyList propertyList) throws SapphireException {
        DataSet primary = sdidata.getDataset("primary");
        this.setCompletionDateRange(primary);
        this.setWorkOrderUserSeq(primary);
    }

    public void setWorkOrderUserSeq(DataSet primary) throws SapphireException {
        HashMap<String, String> incidentFilter = new HashMap<String, String>();
        incidentFilter.put("sourcesdcid", "LV_Incdt");
        primary.addColumn("usersequence", 1);
        DataSet incidentDS = primary.getFilteredDataSet(incidentFilter);
        if (incidentDS.getRowCount() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            String incidentIdList = incidentDS.getColumnValues("sourcekeyid1", ";");
            String rsetId = this.getDAMProcessor().createRSet("LV_Incdt", incidentIdList, "", "");
            String sql = "SELECT wo.sourcekeyid1,max(wo.usersequence) usersequence FROM workorder wo, rsetitems ri where wo.sourcekeyid1 = ri.keyid1 and ri.rsetid = " + safeSQL.addVar(rsetId) + "  GROUP BY wo.sourcekeyid1 ORDER BY WO.sourcekeyid1";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            for (int i = 0; i < incidentDS.getRowCount(); ++i) {
                String incidentId = incidentDS.getValue(i, "sourcekeyid1");
                int rowNum = ds.findRow("sourcekeyid1", incidentId);
                int maxUserSequence = rowNum > -1 ? ds.getInt(rowNum, "usersequence", 0) : 0;
                incidentDS.setNumber(i, "usersequence", ++maxUserSequence);
                if (rowNum == -1) {
                    rowNum = ds.addRow();
                    ds.setString(rowNum, "sourcekeyid1", incidentId);
                }
                ds.setNumber(rowNum, "usersequence", maxUserSequence);
            }
        }
    }

    @Override
    public void preEdit(SDIData sdidata, PropertyList propertyList) throws SapphireException {
        DataSet primary = sdidata.getDataset("primary");
        this.setCompletionDateRange(primary);
    }

    @Override
    public void postEdit(SDIData sdidata, PropertyList propertyList) throws SapphireException {
        DataSet dsPrimary = sdidata.getDataset("primary");
        if (dsPrimary != null) {
            this.processWorkOrder(dsPrimary);
            this.processTaskCompletion(dsPrimary);
            this.processRecordDeviation(dsPrimary);
            if (propertyList.containsKey("workorderproperty_values")) {
                this.editWorkOrderProperty(dsPrimary, propertyList.getProperty("workorderproperty_values"));
            }
        }
        this.updatePulledAmount(propertyList);
        SDIData sdiDataBEI = this.getBeforeEditImage();
        SapphireConnection sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
        InstrumentUtil.workOrderSDCPostEdit(sdidata, sdiDataBEI, propertyList, this.getQueryProcessor(), this.getActionProcessor(), sapphireConnection);
        if (propertyList.containsKey("assignedto")) {
            this.createLESWorkSheetOnWorkOrderAssignment(dsPrimary);
        }
        this.autoStartStopWapActivity(dsPrimary);
    }

    private void autoStartStopWapActivity(DataSet primary) throws ActionException {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer updateCompleteCountActivities = new StringBuffer();
        ArrayList<String> listCompleteActivities = new ArrayList<String>();
        for (int i = 0; i < primary.size(); ++i) {
            boolean completedorcancelled;
            String wapStatus = primary.getValue(i, "wapstatus", this.getOldPrimaryValue(primary, i, "wapstatus"));
            String newStatus = primary.getValue(i, "workorderstatus");
            boolean bl = completedorcancelled = "Assigned".equalsIgnoreCase(wapStatus) && this.hasPrimaryValueChanged(primary, i, "workorderstatus") && ("cancelled".equalsIgnoreCase(newStatus) || "complete".equalsIgnoreCase(newStatus));
            if (!completedorcancelled) continue;
            String workorderId = primary.getValue(i, "workorderid");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select distinct a.activityid, a.activitystatus from activity a, activityworksdi w, workorder o  where o.workorderid = " + safeSQL.addVar(workorderId) + " and  w.worksdcid = 'WorkOrderSDC' and w.workkeyid1 = o.workorderid and a.activityid = w.activityid ", safeSQL.getValues());
            if (ds.getRowCount() > 0) {
                String activityid = ds.getValue(0, "activityid");
                if (completedorcancelled && !listCompleteActivities.contains(activityid)) {
                    listCompleteActivities.add(activityid);
                    updateCompleteCountActivities.append(";").append(activityid);
                }
            }
            safeSQL.reset();
        }
        if (updateCompleteCountActivities.length() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("activityid", updateCompleteCountActivities.substring(1));
            this.getActionProcessor().processActionClass(UpdateActivityCompleteCount.class.getName(), props);
        }
    }

    void createLESWorkSheetOnWorkOrderAssignment(DataSet dsPrimary) throws SapphireException {
        for (int i = 0; i < dsPrimary.size(); ++i) {
            Object[] objectArray;
            String assignedto = dsPrimary.getValue(i, "assignedto");
            if (!this.hasPrimaryValueChanged(dsPrimary, i, "assignedto") || assignedto.length() <= 0) continue;
            String workOrderType = dsPrimary.getValue(i, "workordertype");
            String workOrderId = dsPrimary.getValue(i, "workorderid");
            if (workOrderType == null || workOrderType.length() == 0) {
                workOrderType = this.getOldPrimaryValue(dsPrimary, i, "workordertype");
            }
            if (!workOrderType.equalsIgnoreCase("Certification")) continue;
            String certificationInterval = this.getOldPrimaryValue(dsPrimary, i, "certificationinterval");
            int wsCnt = this.database.getPreparedCount("select 1 from worksheetsdi where sdcid = ? and keyid1 = ?", new String[]{"WorkOrderSDC", workOrderId});
            if (wsCnt >= 1) continue;
            String sourceSDCId = this.getOldPrimaryValue(dsPrimary, i, "sourcesdcid");
            String sourcekeyId1 = this.getOldPrimaryValue(dsPrimary, i, "sourcekeyid1");
            if (!"Instrument".equals(sourceSDCId)) continue;
            this.database.createPreparedResultSet("SELECT * FROM instrument WHERE instrumentid = ?", new Object[]{sourcekeyId1});
            DataSet instrument = new DataSet(this.database.getResultSet());
            if (instrument.size() != 1) continue;
            String sdcid = null;
            String keyid1 = null;
            String keyid2 = "(null)";
            if (instrument.getValue(0, "instrumentmodelid").length() > 0) {
                sdcid = "LV_InstrumentModel";
                keyid1 = instrument.getValue(0, "instrumentmodelid");
                keyid2 = instrument.getValue(0, "instrumenttype");
            } else if (instrument.getValue(0, "instrumenttype").length() > 0) {
                sdcid = "LV_InstrumentType";
                keyid1 = instrument.getValue(0, "instrumenttype");
            }
            QueryProcessor queryProcessor = this.getQueryProcessor();
            String string = "SELECT * FROM sdiworksheetrule WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND createflag = 'A' " + (certificationInterval.length() > 0 ? " AND worksheetrule = ?" : "");
            if (certificationInterval.length() > 0) {
                Object[] objectArray2 = new Object[4];
                objectArray2[0] = sdcid;
                objectArray2[1] = keyid1;
                objectArray2[2] = keyid2;
                objectArray = objectArray2;
                objectArray2[3] = certificationInterval;
            } else {
                Object[] objectArray3 = new Object[3];
                objectArray3[0] = sdcid;
                objectArray3[1] = keyid1;
                objectArray = objectArray3;
                objectArray3[2] = keyid2;
            }
            DataSet sdiWorksheetRule = queryProcessor.getPreparedSqlDataSet(string, objectArray);
            if (sdiWorksheetRule.getRowCount() != 1 && instrument.getValue(0, "instrumentmodelid").length() > 0) {
                Object[] objectArray4;
                QueryProcessor queryProcessor2 = this.getQueryProcessor();
                String string2 = "SELECT * FROM sdiworksheetrule WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND createflag = 'A' " + (certificationInterval.length() > 0 ? " AND worksheetrule = ?" : "");
                if (certificationInterval.length() > 0) {
                    Object[] objectArray5 = new Object[4];
                    objectArray5[0] = "LV_InstrumentType";
                    objectArray5[1] = instrument.getValue(0, "instrumenttype");
                    objectArray5[2] = "(null)";
                    objectArray4 = objectArray5;
                    objectArray5[3] = certificationInterval;
                } else {
                    Object[] objectArray6 = new Object[3];
                    objectArray6[0] = "LV_InstrumentType";
                    objectArray6[1] = instrument.getValue(0, "instrumenttype");
                    objectArray4 = objectArray6;
                    objectArray6[2] = "(null)";
                }
                sdiWorksheetRule = queryProcessor2.getPreparedSqlDataSet(string2, objectArray4);
            }
            if (sdiWorksheetRule.getRowCount() == 1) {
                PropertyList createWSProps = new PropertyList();
                createWSProps.setProperty("workorderid", workOrderId);
                createWSProps.setProperty("worksheetrule", certificationInterval);
                createWSProps.setProperty("templateid", sdiWorksheetRule.getValue(i, "worksheetid"));
                createWSProps.setProperty("templateversionid", sdiWorksheetRule.getValue(i, "worksheetversionid"));
                this.getActionProcessor().processActionClass(GenerateWorkorderWorksheet.class.getName(), createWSProps);
                continue;
            }
            if (sdiWorksheetRule.getRowCount() <= 1) continue;
            throw new SapphireException(this.getTranslationProcessor().translate("More than one Worksheet Template found with the matching criteria. On Analyst assignment, Worksheet could not be created."));
        }
    }

    void updatePulledAmount(PropertyList propertyList) throws SapphireException {
        String wopProps = propertyList.getProperty("wop_pulledamounts");
        DataSet ds = new DataSet();
        if (wopProps.length() > 0) {
            String[] rows = StringUtil.split(wopProps, "|");
            PreparedStatement insert = this.database.prepareStatement("InsertWOP", "INSERT into workorderproperty(workorderid, propertyid, propertyvalue) values(?,'amountpulled',?)");
            for (int i = 0; i < rows.length; ++i) {
                String[] props = StringUtil.split(rows[i], "#semicolon#");
                if (props.length <= 1) continue;
                String wid = props[0];
                if (props[1].length() <= 0) continue;
                try {
                    Double.parseDouble(props[1]);
                }
                catch (NumberFormatException nme) {
                    throw new SapphireException(" Pull amount '" + props[1] + "' is not a valid number." + ErrorUtil.extractMessageFromException(nme, ErrorUtil.isUserAdmin(this.getConnectionId())));
                }
                int find = ds.findRow("workorderid", wid);
                if (find > -1) {
                    double amt = Double.parseDouble(ds.getValue(find, "amountpulled"));
                    ds.setNumber(find, "amountpulled", amt += Double.parseDouble(props[1]));
                    continue;
                }
                int r = ds.addRow();
                ds.setString(r, "workorderid", wid);
                ds.setNumber(r, "amountpulled", props[1]);
            }
            int dsRows = ds.getRowCount();
            try {
                for (int i = 0; i < dsRows; ++i) {
                    String wId = ds.getString(i, "workorderid");
                    String amtPulled = ds.getValue(i, "amountpulled");
                    insert.setString(1, wId);
                    insert.setString(2, amtPulled);
                    insert.execute();
                }
            }
            catch (Exception e) {
                throw new SapphireException(e);
            }
            finally {
                this.database.closeStatement("insertWOP");
            }
        }
    }

    void processWorkOrder(DataSet dsPrimary) throws SapphireException {
        for (int i = 0; i < dsPrimary.size(); ++i) {
            if (!this.hasPrimaryValueChanged(dsPrimary, i, "workorderstatus")) continue;
            String workorderId = dsPrimary.getValue(i, "workorderid", "");
            String workorderStatus = dsPrimary.getValue(i, "workorderstatus", "");
            if (!workorderStatus.equalsIgnoreCase("complete") && !workorderStatus.equalsIgnoreCase("cancelled")) continue;
            IncidentUtil.workorderCompleted(workorderId, this.getQueryProcessor(), this.getActionProcessor());
        }
    }

    /*
     * Unable to fully structure code
     */
    void setCompletionDateRange(DataSet primary) throws SapphireException {
        sql = new StringBuffer();
        sdf = new SimpleDateFormat();
        schedulePlanId = "";
        schedulePlanItemId = "";
        workorderId = "";
        gracePeriodUnit = "";
        gracePeriod = 0.0f;
        earlyGracePeriod = 0.0f;
        dueDt = null;
        tsStartDt = null;
        startDt = Calendar.getInstance();
        windowStartDt = Calendar.getInstance();
        windowEndDt = Calendar.getInstance();
        sql.append("SELECT sc.startdt FROM schedulecondition sc, scheduleplanitem si WHERE ");
        sql.append(" sc.scheduleplanid = si.scheduleplanid AND sc.scheduleconditionid = si.scheduleconditionid AND ");
        sql.append(" si.scheduleplanid = ? AND si.scheduleplanitemid = ? ");
        psmt = this.database.prepareStatement(sql.toString());
        for (i = 0; i < primary.size(); ++i) {
            block11: {
                workorderId = primary.getValue(i, "workorderid", "");
                gracePeriodUnit = primary.getValue(i, "graceperiodtimeunit", "");
                if (this.hasPrimaryValueChanged(primary, i, "workorderstatus") && primary.getValue(i, "workorderstatus", "").equalsIgnoreCase("complete")) {
                    if (primary.getCalendar(i, "completedt") == null) {
                        primary.setDate(i, "completedt", Calendar.getInstance());
                    }
                    if (primary.getValue(i, "completedby").length() != 0) continue;
                    primary.setString(i, "completedby", this.connectionInfo.getSysuserId());
                    continue;
                }
                dueDt = primary.getCalendar(i, "duedt");
                if (dueDt == null) continue;
                gracePeriod = primary.getBigDecimal(i, "graceperiod", new BigDecimal(0.0)).floatValue();
                earlyGracePeriod = primary.getBigDecimal(i, "graceperiodearly", new BigDecimal(gracePeriod)).floatValue();
                if (primary.getBigDecimal(i, "graceperiod") == null) {
                    gracePeriod = earlyGracePeriod;
                }
                schedulePlanId = primary.getValue(i, "scheduleplanid", "");
                schedulePlanItemId = primary.getValue(i, "scheduleplanitemid", "");
                if (schedulePlanId.length() > 0 && schedulePlanItemId.length() > 0) {
                    try {
                        psmt.setString(1, schedulePlanId);
                        psmt.setString(2, schedulePlanItemId);
                        rs = psmt.executeQuery();
                        if (!rs.next()) ** GOTO lbl55
                        tsStartDt = rs.getTimestamp("startdt");
                        startDt = startDt == null ? Calendar.getInstance() : startDt;
                        startDt.setTimeInMillis(tsStartDt.getTime());
                    }
                    catch (Exception e) {
                        throw new SapphireException(e);
                    }
                } else {
                    startDt = null;
                    if (gracePeriodUnit.equalsIgnoreCase("%")) {
                        throw new SapphireException("\"%\" is not a valid unit for adhoc workorder.");
                    }
                }
lbl55:
                // 4 sources

                if (!OpalUtil.isNotEmpty(gracePeriodUnit) || primary.getBigDecimal(i, "graceperiod") == null && primary.getBigDecimal(i, "graceperiodearly") == null) continue;
                dates = StringUtil.split(GRCPeriodUtil.getWindowStartEndDates(startDt, dueDt, gracePeriodUnit, gracePeriod, earlyGracePeriod), ";");
                try {
                    windowStartDt.setTime(sdf.parse(dates[0]));
                    windowEndDt.setTime(sdf.parse(dates[1]));
                    if (startDt == null || !startDt.after(windowStartDt)) break block11;
                    windowStartDt.setTime(startDt.getTime());
                }
                catch (Exception pe) {
                    this.logger.error("Error : WorkOrderSDC Rule - > Could not parse window start date, window end date for WorkOrder id: " + workorderId + ".", pe);
                    continue;
                }
            }
            primary.setDate(i, "windowstartdt", windowStartDt);
            primary.setDate(i, "windowenddt", windowEndDt);
        }
    }

    void processRecordDeviation(DataSet dsPrimary) throws SapphireException {
        for (int i = 0; i < dsPrimary.size(); ++i) {
            String workorderId = dsPrimary.getValue(i, "workorderid", "");
            String workorderStatus = dsPrimary.getValue(i, "workorderstatus", "");
            String deviationTemplate = "";
            if (!workorderStatus.equalsIgnoreCase("complete") || !GRCPeriodUtil.isDeviated(workorderId, this.database)) continue;
            this.database.createPreparedResultSet("rsdevtemp", "SELECT deviationtemplateid, windowstartdt, windowenddt, workordertype FROM workorder  WHERE workorderid = ?", new Object[]{workorderId});
            DataSet ds = new DataSet();
            ds.setResultSet(this.database.getResultSet("rsdevtemp"));
            if (ds.size() > 0) {
                deviationTemplate = ds.getString(0, "deviationtemplateid");
                Calendar windowStartDt = ds.getCalendar(0, "windowstartdt", null);
                Calendar windowEndDt = ds.getCalendar(0, "windowenddt", null);
                String workorderType = ds.getString(0, "workordertype", "");
                this.logger.info("WorkOrder " + workorderId + " has not been completed within scheduled date.");
                GRCPeriodUtil.recordDeviation(workorderId, deviationTemplate, this.getConnectionId(), windowStartDt, windowEndDt, workorderType, this.database);
                this.logger.info("Recorded deviation of WorkOrder completion.");
            }
            this.database.closeResultSet("rsdevtemp");
        }
    }

    void processTaskCompletion(DataSet dsPrimary) throws SapphireException {
        for (int i = 0; i < dsPrimary.size(); ++i) {
            String workorderId = dsPrimary.getValue(i, "workorderid", "");
            String workorderStatus = dsPrimary.getValue(i, "workorderstatus", "");
            if (!workorderStatus.equalsIgnoreCase("complete")) continue;
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT pr.objectname FROM propertytree pr, scheduleplanitem spi, workorder wo ");
            sql.append(" WHERE  pr.propertytreeid = spi.propertytreeid AND spi.scheduleplanid = wo.scheduleplanid");
            sql.append(" AND spi.scheduleplanitemid = wo.scheduleplanitemid AND wo.workorderid=?");
            this.database.createPreparedResultSet("taskname", sql.toString(), new Object[]{workorderId});
            if (this.database.getNext("taskname")) {
                String taskname = this.database.getString("taskname", "objectname");
                try {
                    Class<?> c = Class.forName(taskname);
                    Class[] partypes = new Class[]{String.class, DBAccess.class};
                    Method meth = c.getMethod("workorderCompleted", partypes);
                    if (meth != null) {
                        Object o = c.newInstance();
                        BaseScheduleTask task = (BaseScheduleTask)o;
                        SapphireConnection sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
                        task.startAction("", sapphireConnection, new ErrorHandler(), false);
                        if (o instanceof GridTask) {
                            this.setPropertiesAndEventsToTask(task, workorderId);
                        }
                        meth.invoke(task, workorderId, this.database);
                    }
                }
                catch (Exception e) {
                    this.logger.error("ERROR: WorkOrderSDC Rule - > Could not process method processTaskCompletion:  " + e.getMessage(), e);
                }
            }
            this.database.closeResultSet("taskname");
        }
    }

    void setPropertiesAndEventsToTask(BaseScheduleTask task, String workorderId) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT se.eventdt, spi.scheduleplanid, spi.scheduleplanitemid, spi.linksdcid, spi.linkkeyid1,");
        sql.append(" spi.linkkeyid2, spi.linkkeyid3, spi.valuetree itemtree, spi.scheduletasknodeid, pt.valuetree proptree");
        sql.append(" FROM scheduleevent se, scheduleplanitem spi, workorder wo, propertytree pt");
        sql.append(" WHERE spi.propertytreeid = pt.propertytreeid AND se.scheduleplanid = spi.scheduleplanid");
        sql.append(" AND se.scheduleplanitemid = spi.scheduleplanitemid AND spi.scheduleplanid = wo.scheduleplanid");
        sql.append(" AND spi.scheduleplanitemid = wo.scheduleplanitemid AND wo.workorderid=?");
        sql.append(" ORDER BY spi.scheduleplanid, spi.scheduleplanitemid");
        String planid = "";
        String planitemid = "";
        try {
            this.database.createPreparedResultSet("plandetail", sql.toString(), new Object[]{workorderId});
            ArrayList<ScheduleEvent> scheduleEvents = new ArrayList<ScheduleEvent>();
            PropertyList taskNodeProps = new PropertyList();
            while (this.database.getNext("plandetail")) {
                PlanItem planItem;
                planid = this.database.getString("plandetail", "scheduleplanid");
                planitemid = this.database.getString("plandetail", "scheduleplanitemid");
                Calendar eventDt = Calendar.getInstance();
                eventDt.setTime(this.database.getTimestamp("plandetail", "eventdt"));
                scheduleEvents.add(new ScheduleEvent(this.database.getString("plandetail", "scheduleplanid"), this.database.getString("plandetail", "scheduleplanitemid"), eventDt, this.database.getString("plandetail", "linksdcid"), this.database.getString("plandetail", "linkkeyid1"), this.database.getString("plandetail", "linkkeyid2"), this.database.getString("plandetail", "linkkeyid3")));
                SapphireConnection sapphireConnection = this.getConnectionProcessor().getSapphireConnection();
                ScheduleGrid grid = (ScheduleGrid)CacheUtil.get(sapphireConnection.getDatabaseId(), "ScheduleGrid", planid);
                if (grid == null) {
                    grid = new ScheduleGrid(this.connectionInfo.getConnectionId());
                    grid.retrieve(planid);
                    CacheUtil.put(sapphireConnection.getDatabaseId(), "ScheduleGrid", planid, grid);
                }
                if ((planItem = grid.planItems.findById(planitemid)) == null) {
                    throw new SapphireException("SCHEDULE_SERVICE_FAILED", "Failed to locate planitem: " + planitemid + " in plan " + planid);
                }
                taskNodeProps = planItem.getCollapsedPropertyList();
            }
            if (scheduleEvents.size() > 0) {
                task.setScheduleEvents(scheduleEvents);
                task.setScheduleProperties(taskNodeProps);
            }
            this.database.closeResultSet("plandetail");
        }
        catch (Exception ex) {
            this.logger.error("ERROR: WorkOrderSDC Rule - > Could not process method setPropertiesAndEventsToTask:  " + ex.getMessage(), ex);
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        this.rollUpIncidentActionPlan(rsetid);
    }

    void rollUpIncidentActionPlan(String rsetid) throws SapphireException {
        String sql = "SELECT w.workorderid, w.sourcesdcid, w.sourcekeyid1 FROM workorder w, rsetitems r WHERE w.workorderid = r.keyid1 AND r.sdcid = 'WorkOrderSDC' AND r.rsetid = ? ";
        this.database.createPreparedResultSet("deletedWO", sql, new Object[]{rsetid});
        DataSet ds = new DataSet(this.database.getResultSet("deletedWO"));
        this.database.closeResultSet("deletedWO");
        if (ds != null && ds.size() > 0) {
            for (int i = 0; i < ds.size(); ++i) {
                String workorderId = ds.getString(i, "workorderid");
                String sourceSdcId = ds.getString(i, "sourcesdcid", "");
                String sourceKeyId = ds.getString(i, "sourcekeyid1", "");
                if (!sourceSdcId.equals("LV_Incdt") && !sourceSdcId.equals("LV_ActionPlan")) continue;
                IncidentUtil.workorderDeleted(sourceSdcId, workorderId, sourceKeyId, this.getQueryProcessor(), this.getActionProcessor());
            }
        }
    }

    @Override
    public void postAdd(SDIData sdidata, PropertyList propertyList) throws SapphireException {
        DataSet dsPrimary = sdidata.getDataset("primary");
        if (dsPrimary.getRowCount() == 1 && propertyList.containsKey("workorderproperty_values")) {
            this.addWorkOrderProperty(dsPrimary, propertyList.getProperty("workorderproperty_values"));
        }
    }

    void addWorkOrderProperty(DataSet dsPrimary, String wopProps) throws SapphireException {
        String[] propertyids = new String[]{"sampleid", "quantity", "units"};
        String[] propertyvalues = new String[3];
        String[] props = StringUtil.split(wopProps, "#semicolon#");
        if (props.length > 3) {
            propertyvalues[0] = props[1];
            propertyvalues[1] = props[2];
            propertyvalues[2] = props[3];
            String workorderId = dsPrimary.getString(0, "workorderid");
            PreparedStatement insert = this.database.prepareStatement("insertWOP", "insert into workorderproperty (workorderid, propertyid, propertyvalue) values ( ?, ?, ? )");
            try {
                for (int j = 0; j < propertyids.length; ++j) {
                    insert.setString(1, workorderId);
                    insert.setString(2, propertyids[j]);
                    insert.setString(3, propertyvalues[j]);
                    insert.execute();
                }
                this.updateWorkOrderScheduleplanItemId(dsPrimary);
            }
            catch (Exception e) {
                throw new SapphireException(e);
            }
            finally {
                this.database.closeStatement("insertWOP");
            }
        }
    }

    void editWorkOrderProperty(DataSet dsPrimary, String wopProps) throws SapphireException {
        PreparedStatement update = this.database.prepareStatement("updateWOP", "UPDATE workorderproperty SET propertyvalue = ? WHERE propertyid = ? AND workorderid = ?");
        String[] propertyids = new String[]{"sampleid", "quantity", "units"};
        String[] propertyvalues = new String[3];
        String[] rows = StringUtil.split(wopProps, "|");
        try {
            int updCnt = 0;
            for (int i = 0; i < rows.length; ++i) {
                String[] props = StringUtil.split(rows[i], "#semicolon#");
                if (props.length <= 3) continue;
                String wId = props[0];
                propertyvalues[0] = props[1];
                propertyvalues[1] = props[2];
                propertyvalues[2] = props[3];
                for (int j = 0; j < propertyids.length; ++j) {
                    update.setString(1, propertyvalues[j]);
                    update.setString(2, propertyids[j]);
                    update.setString(3, wId);
                    updCnt += update.executeUpdate();
                }
            }
            if (updCnt > 0) {
                this.updateWorkOrderScheduleplanItemId(dsPrimary);
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        finally {
            this.database.closeStatement("updateWOP");
        }
    }

    void updateWorkOrderScheduleplanItemId(DataSet dsPrimary) throws SapphireException {
        PreparedStatement update = this.database.prepareStatement("updateWO", "update workorder set scheduleplanitemid=?  where workorderid=? ");
        PreparedStatement selStmt = this.database.prepareStatement("selectWOSample", "select s.eventplanitem from s_sample s, workorderproperty w where w.workorderid = ?  and w.propertyid= 'sampleid' and s.s_sampleid=w.propertyvalue");
        try {
            for (int i = 0; i < dsPrimary.getRowCount(); ++i) {
                String planItemId;
                String wId = dsPrimary.getString(i, "workorderid");
                selStmt.setString(1, wId);
                DataSet ds = new DataSet(selStmt.executeQuery());
                if (ds == null || ds.getRowCount() <= 0 || (planItemId = ds.getString(0, "eventplanitem", "")).length() <= 0) continue;
                update.setString(1, planItemId);
                update.setString(2, wId);
                update.executeUpdate();
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        finally {
            this.database.closeStatement("updateWO");
            this.database.closeStatement("selectWOSample");
        }
    }
}

