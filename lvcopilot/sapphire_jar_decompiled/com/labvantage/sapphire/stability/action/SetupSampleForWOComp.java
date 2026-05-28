/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability.action;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.scheduler.BaseScheduleTask;
import com.labvantage.sapphire.scheduler.ScheduleEvent;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.stability.PlanItem;
import com.labvantage.sapphire.stability.ScheduleGrid;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.error.ErrorHandler;
import sapphire.util.DBAccess;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SetupSampleForWOComp
extends BaseAction
implements sapphire.action.SetupSampleForWOComp {
    public static final String SDCID = "WorkOrderSDC";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String workorderId = properties.getProperty("workorderid", "");
        if (workorderId.equals("")) {
            throw new SapphireException("INVALID_PROPERTY", "WorkOrder Id not specified.");
        }
        String delim = properties.getProperty("separator", properties.getProperty("delimeter", ";"));
        String workOrders = properties.getProperty("workorderid", "");
        if (!delim.equals(";")) {
            workOrders = workorderId.replaceAll(delim, ";");
        }
        TranslationProcessor tp = this.getTranslationProcessor();
        String[] arrayWID = StringUtil.split(workorderId, delim);
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT pr.objectname FROM propertytree pr, scheduleplanitem spi, workorder wo ");
        sql.append(" WHERE  pr.propertytreeid = spi.propertytreeid AND spi.scheduleplanid = wo.scheduleplanid");
        sql.append(" AND spi.scheduleplanitemid = wo.scheduleplanitemid AND wo.workorderid = ?");
        sql.append(" AND workorderstatus != 'Complete'");
        PreparedStatement psmt = this.database.prepareStatement("taskname", sql.toString());
        PreparedStatement psmtAdhocCheck = this.database.prepareStatement("adhocCheck", "SELECT 1 FROM workorder WHERE workorderid = ? AND adhocflag = 'Y'");
        ResultSet rs = null;
        PropertyList sdcProps = this.getSDCProcessor().getPropertyList(SDCID);
        String tracelogid = "";
        try {
            if (!sdcProps.getProperty("auditedflag").equalsIgnoreCase("N") && OpalUtil.isEmpty(tracelogid = properties.getProperty("tracelogid")) && OpalUtil.isNotEmpty(properties.getProperty("auditreason"))) {
                try {
                    AuditService audit = new AuditService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                    tracelogid = audit.addSDITraceLogEntry(SDCID, workOrders, "", "", properties.getProperty("auditreason"), properties.getProperty("auditactivity"), properties.getProperty("auditsignedflag"), "N", "StudyPreWorkOrderComplete", true);
                }
                catch (ServiceException e) {
                    throw new SapphireException("Failed to add audit records", e);
                }
            }
            HashMap<String, String> valueMap = new HashMap<String, String>();
            for (int i = 0; i < arrayWID.length; ++i) {
                valueMap.put("workorderid", arrayWID[i]);
                psmtAdhocCheck.setString(1, arrayWID[i]);
                if (psmtAdhocCheck.executeQuery().next()) {
                    throw new SapphireException("PROCESSACTION_FAILED", tp.translate("Pre-Complete operation cannot be performed on the adhoc WorkOrder '[workorderid]'.", valueMap));
                }
                psmt.setString(1, arrayWID[i]);
                rs = psmt.executeQuery();
                if (rs.next()) {
                    String taskname = rs.getString("objectname");
                    Class<?> c = Class.forName(taskname);
                    Class[] paramtypes = new Class[]{String.class, DBAccess.class, String.class};
                    try {
                        Method meth = c.getMethod("preWOComplete", paramtypes);
                        if (meth == null) continue;
                        Object o = c.newInstance();
                        BaseScheduleTask task = (BaseScheduleTask)o;
                        SapphireConnection sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
                        task.startAction("", sapphireConnection, new ErrorHandler(), false);
                        this.setPropertiesAndEventsToTask(task, arrayWID[i]);
                        meth.invoke(task, arrayWID[i], this.database, tracelogid);
                        continue;
                    }
                    catch (NoSuchMethodException nme) {
                        this.logger.error("PROCESSACTION_FAILED" + nme.getMessage(), nme);
                        throw new SapphireException("PROCESSACTION_FAILED", tp.translate("Pre-Complete operation is not supported on the selected WorkOrder '[workorderid]'.", valueMap));
                    }
                }
                throw new SapphireException("PROCESSACTION_FAILED", tp.translate("WorkOrder [workorderid] is in <b>'Complete'</b> status.", valueMap));
            }
        }
        catch (Exception e) {
            this.logger.error("PROCESSACTION_FAILED" + e.getMessage(), e);
            throw new SapphireException("PROCESSACTION_FAILED", ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        finally {
            this.database.closeStatement("taskname");
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
}

