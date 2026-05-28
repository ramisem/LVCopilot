/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability.action;

import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.scheduler.BaseScheduleTask;
import com.labvantage.sapphire.scheduler.ScheduleEvent;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;

public class CompleteWorkOrder
extends BaseAction {
    public static final String ID = "CompleteWorkOrder";
    public static final String VERSION = "1";
    public static final String PROPERTY_WORKORDERID = "workorderid";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String workorderId = properties.getProperty(PROPERTY_WORKORDERID, "");
        if (workorderId.equals("")) {
            throw new SapphireException("INVALID_PROPERTY", "WorkOrder Id not specified.");
        }
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT pr.objectname FROM propertytree pr, scheduleplanitem spi, workorder wo ");
        sql.append(" WHERE  pr.propertytreeid = spi.propertytreeid AND spi.scheduleplanid = wo.scheduleplanid");
        sql.append(" AND spi.scheduleplanitemid = wo.scheduleplanitemid AND wo.workorderid=?");
        sql.append(" AND workorderstatus = 'Complete'");
        this.database.createPreparedResultSet("taskname", sql.toString(), new Object[]{workorderId});
        if (this.database.getNext("taskname")) {
            String taskname = this.database.getString("taskname", "objectname");
            try {
                Class<?> c = Class.forName(taskname);
                Class[] paramtypes = new Class[]{String.class};
                Method meth = c.getMethod("preWOComplete", paramtypes);
                if (meth != null) {
                    Object o = c.newInstance();
                    BaseScheduleTask task = (BaseScheduleTask)o;
                    task.setConnectionId(this.connectionInfo.getConnectionId());
                    this.setPropertiesAndEventsToTask(task, workorderId);
                    meth.invoke(task, workorderId);
                }
            }
            catch (Exception e) {
                this.logger.error("ERROR: WorkOrderSDC Rule - > Could not process method processTaskCompletion:  " + e.getMessage(), e);
            }
        } else {
            throw new SapphireException("PROCESSACTION_FAILED", "WorkOrder " + workorderId + " does not exist with 'Complete' workorder status.");
        }
        this.database.closeResultSet("taskname");
    }

    void setPropertiesAndEventsToTask(BaseScheduleTask task, String workorderId) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT se.eventdt, spi.scheduleplanid, spi.scheduleplanitemid, spi.linksdcid, spi.linkkeyid1,");
        sql.append(" spi.linkkeyid2, spi.linkkeyid3, spi.valuetree itemtree, spi.scheduletasknodeid, pt.valuetree proptree, pt.propertytreeid ");
        sql.append(" FROM scheduleevent se, scheduleplanitem spi, workorder wo, propertytree pt");
        sql.append(" WHERE spi.propertytreeid = pt.propertytreeid AND se.scheduleplanid = spi.scheduleplanid");
        sql.append(" AND se.scheduleplanitemid = spi.scheduleplanitemid AND spi.scheduleplanid = wo.scheduleplanid");
        sql.append(" AND spi.scheduleplanitemid = wo.scheduleplanitemid AND wo.workorderid=?");
        sql.append(" ORDER BY spi.scheduleplanid, spi.scheduleplanitemid");
        try {
            WebAdminProcessor wp = new WebAdminProcessor(this.getConnectionid());
            this.database.createPreparedResultSet("plandetail", sql.toString(), new Object[]{workorderId});
            ArrayList<ScheduleEvent> scheduleEvents = new ArrayList<ScheduleEvent>();
            String taskPropertyTree = "";
            String taskNodeId = "";
            String propTreePropertyTree = "";
            PropertyList taskNodeProps = new PropertyList();
            while (this.database.getNext("plandetail")) {
                Calendar eventDt = Calendar.getInstance();
                eventDt.setTime(this.database.getTimestamp("plandetail", "eventdt"));
                scheduleEvents.add(new ScheduleEvent(this.database.getString("plandetail", "scheduleplanid"), this.database.getString("plandetail", "scheduleplanitemid"), eventDt, this.database.getString("plandetail", "linksdcid"), this.database.getString("plandetail", "linkkeyid1"), this.database.getString("plandetail", "linkkeyid2"), this.database.getString("plandetail", "linkkeyid3")));
                String propertytreeid = this.database.getString("plandetail", "propertytreeid");
                PropertyDefinitionList propertyDefinitionList = wp.getPropertyDefinitionList(propertytreeid);
                taskPropertyTree = this.database.getClob("plandetail", "itemtree");
                taskNodeId = this.database.getString("plandetail", "scheduletasknodeid");
                propTreePropertyTree = this.database.getClob("plandetail", "proptree");
                taskNodeProps.setPropertyTree(propTreePropertyTree, taskNodeId, propertyDefinitionList);
                if (taskPropertyTree != null || taskPropertyTree.trim().length() > 0) {
                    taskNodeProps.addPropertyList(DOMUtil.getNewDocument(taskPropertyTree, false).getFirstChild(), true, "");
                }
                taskNodeProps.setPropertyTreeDefaults(propTreePropertyTree, propertyDefinitionList);
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

