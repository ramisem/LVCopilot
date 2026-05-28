/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability.task;

import com.labvantage.sapphire.modules.wap.activity.WAPConstants;
import com.labvantage.sapphire.scheduler.BaseScheduleTask;
import com.labvantage.sapphire.scheduler.ScheduleEvent;
import com.labvantage.sapphire.stability.PlanItem;
import com.labvantage.sapphire.stability.ScheduleGrid;
import com.labvantage.sapphire.stability.task.GridTask;
import com.labvantage.sapphire.stability.task.GridTaskStatus;
import com.labvantage.sapphire.stability.task.HasDetails;
import com.labvantage.sapphire.stability.task.WorkOrderCallbackable;
import com.labvantage.sapphire.util.graceperiod.GRCPeriodUtil;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.M18NUtil;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class CreateWorkOrder
extends BaseScheduleTask
implements GridTask,
GridTaskStatus,
WorkOrderCallbackable,
HasDetails,
WAPConstants {
    private static final String SUMMARY = "Summary";
    private static final String FULL = "Full Details";
    private DataSet allWorkorders;

    @Override
    public String getColor() {
        return "yellowgreen";
    }

    @Override
    public String getTitle() {
        return "Create Workorder";
    }

    @Override
    public String[] getDetailLevels() {
        return new String[]{SUMMARY, FULL};
    }

    @Override
    public String getSummaryHTML(PropertyList propertyList, String detailLevel) {
        StringBuffer displayValue = new StringBuffer();
        String type = propertyList.getProperty("workordertype");
        String message = propertyList.getProperty("messagetext");
        String priority = propertyList.getProperty("priority");
        if (type.length() > 0 || message.length() > 0) {
            displayValue.append(type);
            displayValue.append(displayValue.length() > 0 ? "<br>" : "");
            if (detailLevel.equals(SUMMARY)) {
                if (type.length() > 0) {
                    displayValue.append("Type: " + type + "<br>");
                }
                displayValue.append("Message: " + (message.length() > 40 ? message.substring(0, 40) + "..." : message) + "<br>");
            } else {
                displayValue.append("Type: " + type + "<br>");
                displayValue.append("Priority: " + priority + "<br>");
                displayValue.append("Message: " + message + "<br>");
            }
        } else {
            displayValue.append("No workorder defined");
        }
        return displayValue.toString();
    }

    @Override
    public String getSummaryText(PropertyList propertyList, String detailLevel) {
        String type = propertyList.getProperty("workordertype");
        StringBuffer displayValue = new StringBuffer();
        if (type.length() > 0) {
            displayValue.append("Type: " + type + "\n");
        }
        displayValue.append("Message: " + propertyList.getProperty("messagetext"));
        return displayValue.toString();
    }

    @Override
    public void statusInit(ScheduleGrid grid, PropertyList propertyList) {
        SafeSQL safeSQL = new SafeSQL();
        String workorderSQL = "SELECT workorderid, workorderstatus, scheduleplanid, scheduleplanitemid FROM workorder where scheduleplanid = " + safeSQL.addVar(grid.planid);
        this.allWorkorders = grid.queryProcessor.getPreparedSqlDataSet(workorderSQL, safeSQL.getValues());
    }

    @Override
    public String getStatusHTML(PlanItem planItem, String detailLevel) {
        StringBuffer output = new StringBuffer();
        HashMap<String, String> workorderFilter = new HashMap<String, String>();
        workorderFilter.put("scheduleplanid", planItem.grid.planid);
        workorderFilter.put("scheduleplanitemid", planItem.planItemid);
        DataSet workorder = this.allWorkorders.getFilteredDataSet(workorderFilter);
        if (workorder.size() > 0) {
            for (int i = 0; i < workorder.size(); ++i) {
                String workorderid = workorder.getValue(i, "workorderid");
                String workorderstatus = workorder.getValue(i, "workorderstatus");
                output.append("<img src='WEB-OPAL/pagetypes/stability/images/workorder.gif'>&nbsp;<a target='taskdetails' href='rc?command=page&page=WorkorderGridStatus&keyid1=" + HttpUtil.encodeURIComponent(workorderid) + "'>" + workorder.getValue(i, "workorderid") + "</a>&nbsp;(" + workorderstatus + ")<br>");
            }
        }
        return output.toString();
    }

    @Override
    public void execute() {
        this.createWorkOrders(this.getActionProcessor(), this.getQueryProcessor(), this.getSDCProcessor(), this.scheduleProperties, this.scheduleEvents, "", null);
    }

    protected void createWorkOrders(ActionProcessor ap, QueryProcessor qp, SDCProcessor sdcProcessor, PropertyList scheduleProperties, ArrayList scheduleEvents, String workordertype, HashMap workorderProperties) {
        long start = System.currentTimeMillis();
        HashMap<String, String> props = new HashMap<String, String>();
        String sdcid = "WorkOrderSDC";
        props.put("sdcid", sdcid);
        props.put("keyid1", "(auto)");
        String studyid = "";
        SimpleDateFormat sdf = new SimpleDateFormat();
        int events = 1;
        if (scheduleEvents != null) {
            HashMap<String, StringBuffer> valuemap = new HashMap<String, StringBuffer>();
            events = scheduleEvents.size();
            this.logger.info("Event List:" + scheduleEvents.toString());
            this.logger.info("Processing " + events + " Events with properties:" + scheduleProperties.toString());
            PropertyListCollection columns = scheduleProperties.getCollectionNotNull("columnvalues");
            PropertyList plGracePeriod = scheduleProperties.getPropertyListNotNull("graceperiod");
            PropertyList plDefaultGracePeriod = plGracePeriod.getPropertyListNotNull("graceperiod");
            PropertyList plDeviation = plGracePeriod.getPropertyListNotNull("deviation");
            PropertyListCollection plGracePeriodDetails = plGracePeriod.getCollectionNotNull("graceperioddetails");
            String defaultGrcPeriod = plDefaultGracePeriod.getProperty("grcperiod", "");
            String defaultEarlyGrcPeriod = plDefaultGracePeriod.getProperty("earlygrcperiod", defaultGrcPeriod);
            if (defaultGrcPeriod.length() == 0 && defaultEarlyGrcPeriod.length() > 0) {
                defaultGrcPeriod = defaultEarlyGrcPeriod;
            }
            String defaultGrcPeriodUnit = plDefaultGracePeriod.getProperty("grcperiodunit", "");
            String deviationflag = plDeviation.getProperty("deviationflag", "");
            String incidenttemplate = plDeviation.getProperty("incidenttemplate", "");
            Calendar startDt = Calendar.getInstance();
            Calendar eventDt = Calendar.getInstance();
            DataSet dsStartdt = null;
            String sql = "";
            SafeSQL safeSQL = new SafeSQL();
            for (int i = 0; i < events; ++i) {
                ScheduleEvent event = (ScheduleEvent)scheduleEvents.get(i);
                String scheduleplanid = event.getSchedulePlanid();
                String scheduleplanitemid = event.getSchedulePlanitemid();
                String grcPeriod = "";
                String earlyGrcPeriod = "";
                String grcPeriodUnit = "";
                sql = "SELECT sc.startdt from schedulecondition sc, scheduleplanitem si  where si.scheduleplanid = " + safeSQL.addVar(scheduleplanid) + " AND si.scheduleplanitemid = " + safeSQL.addVar(scheduleplanitemid) + " AND sc.scheduleplanid = si.scheduleplanid AND sc.scheduleconditionid = si.scheduleconditionid ";
                dsStartdt = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
                safeSQL.reset();
                if (dsStartdt != null && dsStartdt.getRowCount() > 0) {
                    startDt.setTime(dsStartdt.getTimestamp(0, "startdt"));
                    eventDt.setTime(event.getEventDt().getTime());
                    String[] gracePeriod = GRCPeriodUtil.getGRCPeriodFromTaskCollection(plGracePeriodDetails, startDt, eventDt).split(";");
                    try {
                        grcPeriodUnit = gracePeriod[1];
                        grcPeriod = gracePeriod[0];
                        earlyGrcPeriod = gracePeriod[2];
                    }
                    catch (Exception e) {
                        this.logger.info(" No appropriate grace period found for the Task with scheduleplanid :" + scheduleplanid + " scheduleplanitemid :" + scheduleplanitemid);
                    }
                }
                if (grcPeriod.equals("") && grcPeriodUnit.equals("")) {
                    grcPeriod = defaultGrcPeriod;
                    grcPeriodUnit = defaultGrcPeriodUnit;
                    earlyGrcPeriod = defaultEarlyGrcPeriod;
                }
                if (studyid.length() == 0) {
                    safeSQL.reset();
                    DataSet ds = qp.getPreparedSqlDataSet("SELECT studyid from scheduleplan sp, study_scheduleplan ss where sp.scheduleplanid = " + safeSQL.addVar(scheduleplanid) + " AND sp.scheduleplanid = ss.scheduleplanid", safeSQL.getValues());
                    if (ds.getRowCount() > 0) {
                        studyid = ds.getString(0, "studyid");
                    }
                }
                for (int col = 0; col < columns.size(); ++col) {
                    String colid = columns.getPropertyList(col).getProperty("columnid");
                    String value = columns.getPropertyList(col).getProperty("value");
                    if (i == 0) {
                        valuemap.put(colid, new StringBuffer());
                    }
                    String tempvalue = value;
                    ((StringBuffer)valuemap.get(colid)).append(";" + tempvalue);
                }
                DataSet columnsDS = sdcProcessor.getColumnData("WorkOrderSDC");
                for (int col = 0; col < columnsDS.getRowCount(); ++col) {
                    String colid = columnsDS.getString(col, "columnid");
                    String value = "";
                    if (colid.equals("workordertype")) {
                        value = workordertype.length() == 0 ? scheduleProperties.getProperty("workordertype") : workordertype;
                    } else if (colid.equals("scheduleplanid")) {
                        value = event.getSchedulePlanid();
                    } else if (colid.equals("scheduleplanitemid")) {
                        value = event.getSchedulePlanitemid();
                    } else if (colid.equals("workorderstatus")) {
                        value = "Pending";
                    } else if (colid.equals("duedt")) {
                        if (this.connectionInfo != null) {
                            M18NUtil m18n = new M18NUtil(this.connectionInfo);
                            value = m18n.format(event.getEventDt());
                        } else {
                            value = sdf.format(event.getEventDt().getTime());
                        }
                    } else {
                        value = colid.equals("studyid") ? studyid : (colid.equals("deviationflag") ? deviationflag : (colid.equals("deviationtemplateid") ? incidenttemplate : (colid.equals("graceperiodtimeunit") ? grcPeriodUnit : (colid.equals("graceperiodearly") ? earlyGrcPeriod : (colid.equals("graceperiod") ? grcPeriod : scheduleProperties.getProperty(colid))))));
                    }
                    if (value.length() <= 0) continue;
                    if (valuemap.get(colid) == null) {
                        valuemap.put(colid, new StringBuffer(value));
                        continue;
                    }
                    ((StringBuffer)valuemap.get(colid)).append(";" + value);
                }
            }
            Set keySet = valuemap.keySet();
            if (keySet.size() > 0) {
                for (String colid : keySet) {
                    String value = ((StringBuffer)valuemap.get(colid)).indexOf(";") == 0 ? ((StringBuffer)valuemap.get(colid)).substring(1).toString() : ((StringBuffer)valuemap.get(colid)).toString();
                    props.put(colid, value);
                }
            }
            if ("Y".equals(scheduleProperties.getProperty("isplannable")) && props.get("testingdepartmentid") != null && props.get("testingdepartmentid").toString().length() > 0) {
                props.put("wapstatus", "Pending");
            }
            try {
                ap.processAction("AddSDI", "1", props);
                String newkeyid1 = (String)props.get("newkeyid1");
                this.logger.info("Done Create WorkOrder:" + newkeyid1 + ". Took " + (System.currentTimeMillis() - start) + "ms");
                if (workorderProperties != null && workorderProperties.size() > 0) {
                    PreparedStatement insert = this.database.prepareStatement("insert into workorderproperty(workorderid, propertyid, propertyvalue) values ( ?, ?, ? )");
                    insert.setString(1, newkeyid1);
                    Set key = workorderProperties.keySet();
                    for (String propertyid : key) {
                        String value = (String)workorderProperties.get(propertyid);
                        insert.setString(2, propertyid);
                        insert.setString(3, value);
                        insert.execute();
                    }
                }
            }
            catch (Exception e) {
                this.logger.stackTrace(e);
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public boolean isComplete(String planid, String planitemid, DBAccess database) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT COUNT(1) FROM workorder");
        sql.append(" WHERE scheduleplanid =? AND  scheduleplanitemid=?");
        sql.append(" AND  ( workorderstatus is null or workorderstatus != 'Complete' )");
        try {
            if (database.getPreparedCount(sql.toString(), new Object[]{planid, planitemid}) > 0) {
                return false;
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        return true;
    }

    @Override
    public void workorderCompleted(String workorderId, DBAccess db) {
    }

    @Override
    public HashMap getDetails(PropertyList props) {
        HashMap<String, String> details = new HashMap<String, String>();
        details.put("workordertype", props.getProperty("workordertype"));
        details.put("workordermessage", props.getProperty("messagetext"));
        details.put("workorderassignedto", props.getProperty("assignedto"));
        details.put("workorderpriority", props.getProperty("priority"));
        return details;
    }
}

