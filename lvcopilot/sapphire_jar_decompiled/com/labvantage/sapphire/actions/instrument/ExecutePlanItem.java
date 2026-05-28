/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.instrument;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.scheduler.BaseScheduleTask;
import com.labvantage.sapphire.scheduler.ScheduleEvent;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import java.util.ArrayList;
import java.util.Calendar;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.StringUtil;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;

public class ExecutePlanItem
extends BaseAction
implements sapphire.action.ExecutePlanItem {
    static final String LABVANTAGE_CVS_ID = "$Revision: 89973 $";
    private TranslationProcessor tp;

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        long starttime = System.currentTimeMillis();
        this.tp = this.getTranslationProcessor();
        String planIds = properties.getProperty("planid", "");
        String planItemIds = properties.getProperty("planitemid", "");
        String adhocflag = properties.getProperty("adhocflag", "N");
        if (planIds.trim().length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", this.tp.translate("No Plan Id specified."));
        }
        if (planItemIds.trim().length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", this.tp.translate("No Plan Item Id specified."));
        }
        String[] arrPlanIds = StringUtil.split(planIds, ";");
        String[] arrPlanItemIds = StringUtil.split(planItemIds, ";");
        String[] arrAdhocflags = StringUtil.split(adhocflag, ";");
        if (arrPlanIds.length != arrPlanItemIds.length) {
            throw new SapphireException("INVALID_PROPERTY", this.tp.translate("Plan Item Id not specified for all Plan Id(s)."));
        }
        this.executePlanItems(arrPlanIds, arrPlanItemIds, arrAdhocflags);
        Trace.logDebug("Action 'ExecutePlanItem' took " + (System.currentTimeMillis() - starttime) + " ms.");
    }

    private void executePlanItems(String[] arrPlanIds, String[] arrPlanItemIds, String[] arrAdhocflags) throws SapphireException {
        for (int i = 0; i < arrPlanIds.length; ++i) {
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT pr.objectname FROM propertytree pr, scheduleplanitem spi ");
            sql.append(" WHERE  pr.propertytreeid = spi.propertytreeid AND spi.scheduleplanid = ?");
            sql.append(" AND spi.scheduleplanitemid = ?");
            this.database.createPreparedResultSet("taskname", sql.toString(), new Object[]{arrPlanIds[i], arrPlanItemIds[i]});
            if (!this.database.getNext("taskname")) continue;
            String taskname = this.database.getString("taskname", "objectname");
            try {
                BaseScheduleTask task = (BaseScheduleTask)Class.forName(taskname).newInstance();
                task.setConnectionId(this.connectionInfo.getConnectionId());
                String adhocflag = "N";
                if (arrAdhocflags.length > i) {
                    adhocflag = arrAdhocflags[i];
                } else if (arrAdhocflags.length == 1) {
                    adhocflag = arrAdhocflags[0];
                }
                this.setPropertiesAndEventsToTask(task, arrPlanIds[i], arrPlanItemIds[i], adhocflag);
                if (task.getDatabase() == null) {
                    task.setDatabase(this.database);
                }
                task.execute();
            }
            catch (Exception e) {
                this.database.closeResultSet("taskname");
                throw new SapphireException(e);
            }
            this.database.closeResultSet("taskname");
        }
    }

    void setPropertiesAndEventsToTask(BaseScheduleTask task, String scheduleplanid, String scheduleplanitemid, String adhocflag) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT spi.linksdcid, spi.linkkeyid1, spi.linkkeyid2, spi.linkkeyid3, spi.valuetree itemtree, spi.scheduletasknodeid, pt.valuetree proptree, pt.propertytreeid ");
        sql.append(" FROM scheduleplanitem spi, propertytree pt");
        sql.append(" WHERE spi.propertytreeid = pt.propertytreeid AND spi.scheduleplanid = ? AND spi.scheduleplanitemid = ?");
        sql.append(" ORDER BY spi.scheduleplanid, spi.scheduleplanitemid");
        try {
            this.database.createPreparedResultSet("plandetail", sql.toString(), new Object[]{scheduleplanid, scheduleplanitemid});
            ArrayList<ScheduleEvent> scheduleEvents = new ArrayList<ScheduleEvent>();
            String taskPropertyTree = "";
            String taskNodeId = "";
            String propTreePropertyTree = "";
            PropertyList taskNodeProps = new PropertyList();
            while (this.database.getNext("plandetail")) {
                scheduleEvents.add(new ScheduleEvent(scheduleplanid, scheduleplanitemid, Calendar.getInstance(), this.database.getString("plandetail", "linksdcid"), this.database.getString("plandetail", "linkkeyid1"), this.database.getString("plandetail", "linkkeyid2"), this.database.getString("plandetail", "linkkeyid3")));
                String propertytreeid = this.database.getString("plandetail", "propertytreeid");
                WebAdminProcessor wp = new WebAdminProcessor(this.getConnectionid());
                PropertyDefinitionList propertyDefintionList = wp.getPropertyDefinitionList(propertytreeid);
                taskPropertyTree = this.database.getClob("plandetail", "itemtree");
                taskNodeId = this.database.getString("plandetail", "scheduletasknodeid");
                propTreePropertyTree = this.database.getClob("plandetail", "proptree");
                taskNodeProps.setPropertyTree(propTreePropertyTree, taskNodeId, propertyDefintionList);
                if (taskPropertyTree != null && taskPropertyTree.trim().length() > 0) {
                    taskNodeProps.addPropertyList(DOMUtil.getNewDocument(this.getTaskPropsOverride(scheduleplanid, scheduleplanitemid), false).getLastChild(), true, "");
                }
                taskNodeProps.setPropertyTreeDefaults(propTreePropertyTree, propertyDefintionList);
            }
            if (scheduleEvents.size() > 0) {
                task.setScheduleEvents(scheduleEvents);
                task.setScheduleProperties(taskNodeProps);
                task.setAdhocFlag(adhocflag);
            }
            this.database.closeResultSet("plandetail");
        }
        catch (Exception ex) {
            Trace.logError("ExecutePlanItem-> " + this.tp.translate("Could not process method setPropertiesAndEventsToTask") + ":  " + ex.getMessage());
            throw new SapphireException(ex);
        }
    }

    public String getTaskPropsOverride(String scheduleplanid, String scheduleplanitemid) throws ServiceException {
        String ptree = null;
        try {
            String sql = "SELECT valuetree FROM scheduleplanitem WHERE scheduleplanid= ? and scheduleplanitemid= ?";
            this.database.createPreparedResultSet(sql, new Object[]{scheduleplanid, scheduleplanitemid});
            if (this.database.getNext()) {
                ptree = this.database.getClob("valuetree");
            }
            this.database.closeResultSet();
        }
        catch (SapphireException e) {
            throw new ServiceException("SCHEDULE_SERVICE_FAILED", "Failed to get task property overrides", e);
        }
        finally {
            this.database.closeResultSet();
        }
        return ptree == null || ptree.trim().length() == 0 ? "<propertylist/>" : ptree;
    }
}

