/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.stability;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.scheduler.SchedulerAdminProcessor;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.stability.ConditionAxis;
import com.labvantage.sapphire.stability.PlanItemList;
import com.labvantage.sapphire.stability.PlanWorkItemList;
import com.labvantage.sapphire.stability.TaskTypeList;
import com.labvantage.sapphire.stability.TimeAxis;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyTree;
import java.io.File;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Iterator;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class ScheduleGrid
extends BaseCustom
implements Serializable {
    public String planid = null;
    public String plantype = null;
    private boolean changesMade = false;
    private boolean partialDistribution;
    public static final String STUDY = "Y";
    public static final String PROTOCOL = "P";
    public static final String TEMPLATE = "T";
    public boolean templateflag = true;
    public String executeahead;
    public String executeaheadUnit;
    public String default_executeahead;
    public String default_executeaheadUnit;
    public TimeAxis timeAxis;
    public PlanWorkItemList workItems;
    public ConditionAxis conditionAxis;
    public PlanItemList planItems;
    public TaskTypeList taskTypes;
    public String label;
    public String auditReason;
    public String auditActivity;
    public String auditSignedFlag;
    public String traceLogId;
    public DataSet conditionsWithSampleWO = new DataSet();
    public DataSet conditionsWithReadOnlyPlanItem = new DataSet();
    public HashMap propertyTreeMap = new HashMap();
    public transient ConnectionProcessor connectionProcessor;
    public transient SDCProcessor sdcProcessor;
    public transient QueryProcessor queryProcessor;
    public transient SchedulerAdminProcessor planProcessor;
    public transient ActionProcessor actionProcessor;
    public transient WebAdminProcessor webadminProcessor;
    public transient DAMProcessor damProcessor;
    private transient PageContext pageContext;

    public ScheduleGrid(PageContext pageContext) {
        this(HttpUtil.getConnectionId(pageContext));
    }

    public ScheduleGrid(String connectionid) {
        this((File)null, connectionid);
    }

    public ScheduleGrid(String connectionid, String tracelogid) {
        this((File)null, connectionid);
        this.traceLogId = tracelogid;
    }

    public ScheduleGrid(File rakFile, String connectionid) {
        if (Trace.stats) {
            Trace.setStartCodeBlock("ScheduleGrid - Constructor");
        }
        this.setRakFile(rakFile);
        this.setConnectionId(connectionid);
        this.initGrid(this.getConnectionProcessor(), this.getSDCProcessor(), this.getQueryProcessor(), this.getActionProcessor(), rakFile == null ? new WebAdminProcessor(connectionid) : null, rakFile == null ? new SchedulerAdminProcessor(connectionid) : null, this.getDAMProcessor());
        this.planItems = new PlanItemList(this);
        this.workItems = new PlanWorkItemList(this);
        this.timeAxis = new TimeAxis(this);
        this.conditionAxis = new ConditionAxis(this);
        if (Trace.stats) {
            Trace.setEndCodeBlock("ScheduleGrid - Constructor");
        }
    }

    public void initGrid(PageContext pageContext) {
        this.setConnectionId(HttpUtil.getConnectionId(pageContext));
        this.pageContext = pageContext;
        if (this.connectionProcessor == null || this.taskTypes == null || this.taskTypes.size() == 0 || this.taskTypes.isInstanceGridTaskNull()) {
            this.initGrid(new ConnectionProcessor(pageContext), new SDCProcessor(pageContext), new QueryProcessor(pageContext), new ActionProcessor(pageContext), new WebAdminProcessor(pageContext), new SchedulerAdminProcessor(pageContext), new DAMProcessor(pageContext));
        }
    }

    public void initGrid(ConnectionInfo connectionInfo) {
        this.setConnectionId(connectionInfo.getConnectionId());
        if (this.connectionProcessor == null || this.taskTypes == null || this.taskTypes.size() == 0 || this.taskTypes.isInstanceGridTaskNull()) {
            this.initGrid(new ConnectionProcessor(connectionInfo.getConnectionId()), new SDCProcessor(connectionInfo.getConnectionId()), new QueryProcessor(connectionInfo.getConnectionId()), new ActionProcessor(connectionInfo.getConnectionId()), new WebAdminProcessor(connectionInfo.getConnectionId()), new SchedulerAdminProcessor(connectionInfo.getConnectionId()), new DAMProcessor(connectionInfo.getConnectionId()));
        }
    }

    private void initGrid(ConnectionProcessor connectionProcessor, SDCProcessor sdcProcessor, QueryProcessor queryProcessor, ActionProcessor actionProcessor, WebAdminProcessor webadminProcessor, SchedulerAdminProcessor planProcessor, DAMProcessor damProcessor) {
        this.connectionProcessor = connectionProcessor;
        this.sdcProcessor = sdcProcessor;
        this.queryProcessor = queryProcessor;
        this.actionProcessor = actionProcessor;
        this.webadminProcessor = webadminProcessor;
        this.planProcessor = planProcessor;
        this.damProcessor = damProcessor;
        if (this.taskTypes == null || this.taskTypes.size() == 0) {
            this.taskTypes = new TaskTypeList();
            DataSet ds = queryProcessor.getSqlDataSet("SELECT propertytreeid, objectname FROM propertytree WHERE propertytreetype = 'ScheduleTask' order by usersequence, propertytreeid");
            for (int i = 0; i < ds.size(); ++i) {
                String propertyTreeid = ds.getValue(i, "propertytreeid");
                String objectName = ds.getValue(i, "objectname");
                if (objectName.length() <= 0) continue;
                try {
                    this.taskTypes.addTaskType(propertyTreeid, objectName);
                    continue;
                }
                catch (Exception e) {
                    Trace.log("Error Adding task for :" + objectName + ": " + e.getMessage());
                }
            }
        } else {
            try {
                this.taskTypes.initTask();
            }
            catch (Exception e) {
                Trace.log("Error in initializing the tasks :" + e.getMessage());
            }
        }
    }

    public void setReadonly() {
        this.planItems.setReadonly();
        this.conditionAxis.setReadonly();
        this.timeAxis.setReadonly();
    }

    public PageContext getPageContext() {
        return this.pageContext;
    }

    public void renamePlan(String planid) {
        this.planid = planid;
    }

    public void retrieve(String planid) throws SapphireException {
        this.planid = planid;
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT scheduleplandesc, scheduleplantypeflag, templateflag, executeahead, executeaheadunits FROM scheduleplan WHERE scheduleplanid = " + safeSQL.addVar(planid);
        DataSet ds = this.queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds.size() == 0) {
            throw new SapphireException(ErrorUtil.extractMessage("Unable to load details using sql Exception: " + sql, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        this.plantype = ds.getValue(0, "scheduleplantypeflag");
        this.templateflag = ds.getValue(0, "templateflag").equals(STUDY);
        this.label = ds.getValue(0, "scheduleplandesc");
        if (this.label.length() == 0) {
            this.label = "New Plan";
        }
        this.executeahead = ds.getValue(0, "executeahead");
        this.executeaheadUnit = ds.getValue(0, "executeaheadunits");
        try {
            DataSet dsExecAhead = this.queryProcessor.getPreparedSqlDataSet("SELECT sysuserid, propertyvalue FROM profileproperty WHERE profileid = ? AND propertyid = ? AND sysuserid = ? ORDER BY 1 DESC", new Object[]{"System", "executeahead", "(system)"});
            this.default_executeahead = dsExecAhead.getValue(0, "propertyvalue", "5");
            dsExecAhead = this.queryProcessor.getPreparedSqlDataSet("SELECT sysuserid, propertyvalue FROM profileproperty WHERE profileid = ? AND propertyid = ? AND sysuserid = ? ORDER BY 1 DESC", new Object[]{"System", "executeaheadunits", "(system)"});
            this.default_executeaheadUnit = dsExecAhead.getValue(0, "propertyvalue", "Day");
        }
        catch (Exception se) {
            Logger.logInfo("Exception occured in retrieving system default execute ahead time and unit." + se.getMessage());
        }
        if (this.webadminProcessor != null) {
            Iterator iterator = this.taskTypes.iterator();
            while (iterator.hasNext()) {
                String propertytreeid = (String)iterator.next();
                try {
                    PropertyTree tree = this.webadminProcessor.getPropertyTree(propertytreeid);
                    tree.setId(propertytreeid);
                    this.propertyTreeMap.put(propertytreeid, tree);
                    String valueTree = this.planProcessor.loadPlanValueTree(planid, propertytreeid);
                    String extendnodeid = this.planProcessor.getPlanValueTreeExtendNode(planid, propertytreeid);
                    PropertyList propertyList = new PropertyList();
                    propertyList.setUsePropertyValues(true);
                    propertyList.setPropertyList(valueTree, false, "__plan", false);
                    Node extendNode = tree.getNode(extendnodeid);
                    Node planNode = tree.createNode("__plan", extendNode);
                    planNode.setPropertyList(propertyList);
                }
                catch (Exception e) {
                    Trace.logError("Unable to load details for " + propertytreeid + ".");
                    iterator.remove();
                }
            }
        }
        this.timeAxis.retrieve(planid);
        this.conditionAxis.retrieve(planid);
        this.planItems.retrieve(planid);
        this.workItems.retrieve(planid);
    }

    public void save(SapphireConnection sapphireConnection, DBUtil database) throws Exception {
        String update = "UPDATE scheduleplan SET scheduleplandesc=?, executeahead=?, executeaheadunits=?, tracelogid=?, moddt=?, modby=? WHERE scheduleplanid=?";
        Integer tracelogid = null;
        if (this.traceLogId == null || this.traceLogId.trim().length() == 0) {
            if (this.auditReason == null || this.auditReason.trim().length() == 0) {
                this.auditReason = "Updating SchedulePlan " + this.planid;
            }
            String sdcid = "SchedulePlan";
            AuditService audit = new AuditService(sapphireConnection);
            try {
                tracelogid = new Integer(audit.addSDITraceLogEntry(sdcid, this.planid, "", "", this.auditReason, this.auditActivity, this.auditSignedFlag == null ? "N" : this.auditSignedFlag, "now", "Save", true));
                this.traceLogId = tracelogid.toString();
            }
            catch (ServiceException e) {
                throw new SapphireException("Failed to add audit records", e);
            }
        }
        if (this.executeahead != null && this.executeahead.length() > 0 && this.executeaheadUnit != null && this.executeaheadUnit.length() > 0) {
            database.executePreparedUpdate(update, new Object[]{this.label, this.executeahead, this.executeaheadUnit, this.traceLogId, DateTimeUtil.getNowTimestamp(), sapphireConnection.getSysuserId(), this.planid});
        } else {
            database.executePreparedUpdate(update, new Object[]{this.label, null, null, this.traceLogId, DateTimeUtil.getNowTimestamp(), sapphireConnection.getSysuserId(), this.planid});
        }
        for (int type = 0; type < this.taskTypes.size(); ++type) {
            String propertytreeid = (String)this.taskTypes.get(type);
            PropertyTree tree = (PropertyTree)this.propertyTreeMap.get(propertytreeid);
            Node planNode = tree.getNode("__plan");
            String extendnodeid = planNode.getParent() == null ? "(root)" : planNode.getParent().getId();
            PropertyList propertyList = tree.getNodePropertyList("__plan", false);
            String valuetree = propertyList == null ? "<propertylist />" : propertyList.toXMLString();
            try {
                String sql = "UPDATE scheduleplandefaults set valuetree = ?, extendnodeid=? WHERE scheduleplanid = ? AND propertytreeid=? ";
                PreparedStatement ps = database.prepareStatement(sql);
                ps.setCharacterStream(1, (Reader)new StringReader(valuetree), valuetree.length());
                ps.setString(2, extendnodeid);
                ps.setString(3, this.planid);
                ps.setString(4, propertytreeid);
                if (ps.executeUpdate() == 0) {
                    String sql2 = "INSERT INTO scheduleplandefaults ( valuetree, scheduleplanid, propertytreeid, extendnodeid ) values ( ?, ?, ?, ? )";
                    PreparedStatement ps2 = database.prepareStatement(sql2);
                    ps2.setCharacterStream(1, (Reader)new StringReader(valuetree), valuetree.length());
                    ps2.setString(2, this.planid);
                    ps2.setString(3, propertytreeid);
                    ps2.setString(4, extendnodeid);
                    ps2.executeUpdate();
                    ps2.close();
                }
                ps.close();
                continue;
            }
            catch (Exception e) {
                throw new SapphireException("Unable to update tree: " + this.planid + ";" + propertytreeid);
            }
            finally {
                database.reset();
            }
        }
        try {
            Trace.logDebug("Saving times");
            this.timeAxis.save(database);
            Trace.logDebug("Saving conditions");
            this.conditionAxis.save(database);
            Trace.logDebug("Saving planitems");
            this.planItems.save(database);
            Trace.logDebug("Saving plan workitems");
            this.workItems.save(database);
        }
        catch (Exception e) {
            Trace.logError("Failed to save schedule grid: " + e.getMessage(), e);
        }
        CacheUtil.clear(sapphireConnection.getDatabaseId(), "ScheduleGrid");
        CacheUtil.clear(sapphireConnection.getDatabaseId(), "ScheduleTaskNodeProperties");
        CacheUtil.clear(sapphireConnection.getDatabaseId(), "ScheduleTaskPropertyTree");
    }

    public void changesMade() {
        this.changesMade = true;
    }

    public boolean isChangesMade() {
        return this.changesMade;
    }

    public String toString() {
        return this.planid;
    }

    public PropertyTree getPropertyTree(String propertyTreeid) {
        return (PropertyTree)this.propertyTreeMap.get(propertyTreeid);
    }

    public boolean getPartialDistribution() {
        return this.partialDistribution;
    }

    public void setPartialDistribution(boolean partialDistribution) {
        this.partialDistribution = partialDistribution;
    }
}

