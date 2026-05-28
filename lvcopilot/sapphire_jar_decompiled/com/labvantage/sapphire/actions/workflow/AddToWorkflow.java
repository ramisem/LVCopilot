/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.workflow;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.admin.ddt.LV_WorkflowDef;
import com.labvantage.sapphire.admin.ddt.LV_WorkflowExec;
import com.labvantage.sapphire.modules.workflow.WorkflowManager;
import com.labvantage.sapphire.pageelements.gwt.shared.WorkflowManagerCodes;
import com.labvantage.sapphire.pageelements.gwt.shared.WorkflowManagerConstants;
import com.labvantage.sapphire.services.AutomationService;
import com.labvantage.sapphire.services.SapphireConnection;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AddToWorkflow
extends BaseAction
implements sapphire.action.AddToWorkflow,
WorkflowManagerConstants {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid = properties.getProperty("sdcid");
        if (sdcid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "Sdcid not specified");
        }
        if (properties.getProperty("keyid1").length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "Keyid1 not specified");
        }
        if (properties.getProperty("workflowdefid").length() == 0 && properties.getProperty("workflowexecid").length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "Workflow execution or workflowid not specified");
        }
        boolean propsmatch = StringUtil.getYN(properties.getProperty("propsmatch"), "N").equals("Y");
        DataSet propsDS = new DataSet(this.connectionInfo);
        if (propsmatch) {
            propsDS.addColumnValues("queuesdcid", 0, sdcid, ";");
            propsDS.addColumnValues("queuekeyid1", 0, properties.getProperty("keyid1"), ";");
            propsDS.addColumnValues("queuekeyid2", 0, properties.getProperty("keyid2"), ";");
            propsDS.addColumnValues("queuekeyid3", 0, properties.getProperty("keyid3"), ";");
            propsDS.addColumnValues("workflowexecid", 0, properties.getProperty("workflowexecid"), ";");
            propsDS.addColumnValues("workflowdefid", 0, properties.getProperty("workflowdefid"), ";");
            propsDS.addColumnValues("workflowdefversionid", 0, properties.getProperty("workflowdefversionid"), ";");
            propsDS.addColumnValues("workflowdefvariantid", 0, properties.getProperty("workflowdefvariantid"), ";");
            propsDS.padColumns();
        } else {
            propsDS.addColumn("queuesdcid", 0);
            propsDS.addColumn("queuekeyid1", 0);
            propsDS.addColumn("queuekeyid2", 0);
            propsDS.addColumn("queuekeyid3", 0);
            propsDS.addColumn("workflowexecid", 0);
            propsDS.addColumn("workflowdefid", 0);
            propsDS.addColumn("workflowdefversionid", 0);
            propsDS.addColumn("workflowdefvariantid", 0);
            String[] keyid1Props = StringUtil.split(properties.getProperty("keyid1"), ";");
            String[] keyid2Props = StringUtil.split(properties.getProperty("keyid2"), ";");
            String[] keyid3Props = StringUtil.split(properties.getProperty("keyid3"), ";");
            String[] workflowexecidProps = StringUtil.split(properties.getProperty("workflowexecid"), ";");
            String[] workflowdefidProps = StringUtil.split(properties.getProperty("workflowdefid"), ";");
            String[] workflowdefversionidProps = StringUtil.split(properties.getProperty("workflowdefversionid"), ";");
            String[] workflowdefvariantidProps = StringUtil.split(properties.getProperty("workflowdefvariantid"), ";");
            for (int i = 0; i < keyid1Props.length; ++i) {
                for (int j = 0; j < workflowdefidProps.length; ++j) {
                    int row = propsDS.addRow();
                    propsDS.setValue(row, "queuesdcid", sdcid);
                    propsDS.setValue(row, "queuekeyid1", keyid1Props[i]);
                    propsDS.setValue(row, "queuekeyid2", keyid2Props.length > i && keyid2Props[i].length() > 0 ? keyid2Props[i] : "(null)");
                    propsDS.setValue(row, "queuekeyid3", keyid3Props.length > i && keyid3Props[i].length() > 0 ? keyid3Props[i] : "(null)");
                    propsDS.setValue(row, "workflowexecid", workflowexecidProps.length > j ? workflowexecidProps[j] : "");
                    propsDS.setValue(row, "workflowdefid", workflowdefidProps[j]);
                    propsDS.setValue(row, "workflowdefversionid", workflowdefversionidProps.length > j ? workflowdefversionidProps[j] : "1");
                    propsDS.setValue(row, "workflowdefvariantid", workflowdefvariantidProps.length > j ? workflowdefvariantidProps[j] : "1");
                }
            }
        }
        propsDS.addColumn("taskdefid", 0);
        propsDS.addColumn("taskdefversionid", 0);
        propsDS.addColumn("taskdefvariantid", 0);
        propsDS.addColumn("taskdefitemid", 0);
        propsDS.addColumn("ioid", 0);
        propsDS.addColumn("connectortypeid", 0);
        propsDS.addColumn("scopeflag", 0);
        propsDS.addColumn("autoexec", 0);
        propsDS.addColumn("waittype", 0);
        propsDS.addColumn("assignmentflag", 0);
        propsDS.addColumn("assigneddepartment", 0);
        propsDS.addColumn("assignedanalyst", 0);
        propsDS.addColumn("assignedrole", 0);
        SapphireConnection sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        DAMProcessor damProcessor = this.getDAMProcessor();
        String rsetid = damProcessor.createRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"));
        try {
            StringBuffer newWorkflowexecids = new StringBuffer();
            HashMap<String, String> wf_wfe = new HashMap<String, String>();
            for (int i = 0; i < propsDS.size(); ++i) {
                String newWorkflowexecid;
                if (propsDS.getValue(i, "workflowexecid").length() != 0 && !propsDS.getValue(i, "workflowexecid").equals("(Auto)")) continue;
                String workflowdefid = propsDS.getValue(i, "workflowdefid");
                String workflowdefversionid = propsDS.getValue(i, "workflowdefversionid");
                String workflowdefvariantid = propsDS.getValue(i, "workflowdefvariantid");
                if (workflowdefversionid.length() == 0 || workflowdefversionid.equalsIgnoreCase("C")) {
                    String sql = "SELECT workflowdefversionid FROM workflowdef WHERE workflowdefid = ? AND workflowdefvariantid = ? AND ( versionstatus = 'P' OR versionstatus = 'C' ) ORDER BY versionstatus, cast ( workflowdefversionid as integer ) DESC";
                    this.database.createPreparedResultSet(sql, new Object[]{workflowdefid, workflowdefvariantid});
                    if (this.database.getNext()) {
                        workflowdefversionid = this.database.getValue("workflowdefversionid");
                    } else {
                        throw new SapphireException("Failed to find workflow " + LV_WorkflowDef.getText(workflowdefid, workflowdefversionid, workflowdefvariantid));
                    }
                }
                if ((newWorkflowexecid = (String)wf_wfe.get(workflowdefid + ";" + workflowdefversionid + ";" + workflowdefvariantid)) == null) {
                    String workflowexecname = properties.getProperty("workflowexecname");
                    if (workflowexecname.length() == 0) {
                        workflowexecname = LV_WorkflowExec.generateName(this.getQueryProcessor(), this.getSDCProcessor(), this.getSequenceProcessor(), sdcid, propsDS.getColumnValues("queuekeyid1", ";"), workflowdefid, workflowdefversionid, workflowdefvariantid);
                    }
                    String workflowexecdesc = properties.getProperty("workflowexecdesc");
                    this.database.createPreparedResultSet("SELECT workflowdef FROM workflowdef WHERE workflowdefid = ? AND workflowdefversionid = ? AND workflowdefvariantid = ?", new Object[]{workflowdefid, workflowdefversionid, workflowdefvariantid});
                    PropertyListCollection variables = null;
                    if (this.database.getNext()) {
                        PropertyList workflowdef = new PropertyList();
                        workflowdef.setPropertyList(this.database.getClob("workflowdef"));
                        variables = workflowdef.getCollection("variables");
                        if (variables != null) {
                            for (int j = 0; j < variables.size(); ++j) {
                                PropertyList variable = variables.getPropertyList(j);
                                if (!properties.containsKey(variable.getProperty("variableid"))) continue;
                                variable.setProperty("value", properties.getProperty(variable.getProperty("variableid")));
                            }
                        }
                    }
                    if (variables == null) {
                        variables = new PropertyListCollection();
                    }
                    newWorkflowexecid = WorkflowManager.createNewWorkflowExec(sapphireConnection, (DBUtil)this.database, workflowdefid, workflowdefversionid, workflowdefvariantid, workflowexecname, workflowexecdesc, variables, properties.getProperty("auditactivity"), properties.getProperty("auditreason"), properties.getProperty("auditsignedflag").equals("Y"));
                    wf_wfe.put(workflowdefid + ";" + workflowdefversionid + ";" + workflowdefvariantid, newWorkflowexecid);
                    newWorkflowexecids.append(";").append(newWorkflowexecid);
                }
                propsDS.setValue(i, "workflowexecid", newWorkflowexecid);
            }
            properties.setProperty("newworkflowexecid", newWorkflowexecids.length() > 0 ? newWorkflowexecids.substring(1) : "");
            boolean waitConditions = false;
            propsDS.sort("workflowexecid");
            String workflowioname = properties.getProperty("workflowioname");
            ArrayList<DataSet> wfeGroups = propsDS.getGroupedDataSets("workflowexecid");
            for (int i = 0; i < wfeGroups.size(); ++i) {
                DataSet group = wfeGroups.get(i);
                String workflowexecid = group.getValue(0, "workflowexecid");
                this.database.createPreparedResultSet("SELECT workflowdeftask.taskdefitemid, workflowdeftask.taskdefid, workflowdeftask.taskdefversionid, workflowdeftask.taskdefvariantid,        workflowdeftask.assignmentflag, workflowdeftask.departmentid, workflowdeftask.sysuserid, workflowdeftask.roleid, taskdefio.waittype,        workflowdeftaskio.ioid, workflowdeftaskio.workflowioname, workflowdeftaskio.workflowioflag, taskdef.scopeflag, taskdef.autoexecflag, connectortype.connectortypeid, connectortype.connectortypesdcid FROM   workflowexec, workflowdef, workflowdeftask, workflowdeftaskio, taskdef, taskdefio, connectortype WHERE  workflowdef.workflowdefid = workflowexec.workflowdefid AND workflowdef.workflowdefversionid = workflowexec.workflowdefversionid AND workflowdef.workflowdefvariantid = workflowexec.workflowdefvariantid AND workflowdef.workflowdefid = workflowdeftask.workflowdefid AND workflowdef.workflowdefversionid = workflowdeftask.workflowdefversionid AND workflowdef.workflowdefvariantid = workflowdeftask.workflowdefvariantid AND workflowdeftask.workflowdefid = workflowdeftaskio.workflowdefid AND workflowdeftask.workflowdefversionid = workflowdeftaskio.workflowdefversionid AND workflowdeftask.workflowdefvariantid = workflowdeftaskio.workflowdefvariantid AND workflowdeftask.taskdefitemid = workflowdeftaskio.taskdefitemid AND workflowdeftask.starttaskflag = 'Y' AND workflowdeftaskio.ioflag = 'I' AND ( workflowdeftaskio.workflowioflag = 'Y' OR workflowdeftaskio.workflowioflag is NULL ) AND workflowdeftask.taskdefid = taskdef.taskdefid AND workflowdeftask.taskdefversionid = taskdef.taskdefversionid AND workflowdeftask.taskdefvariantid = taskdef.taskdefvariantid AND taskdef.taskdefid = taskdefio.taskdefid AND taskdef.taskdefversionid = taskdefio.taskdefversionid AND taskdef.taskdefvariantid = taskdefio.taskdefvariantid AND taskdefio.ioid = workflowdeftaskio.ioid AND taskdefio.ioflag = 'I' AND connectortype.connectortypeid = workflowdeftaskio.connectortypeid AND workflowexec.execstatus = 'A' AND workflowexecid = ?", new Object[]{workflowexecid});
                DataSet wfStartTasks = new DataSet();
                wfStartTasks.setResultSet(this.database.getResultSet());
                if (wfStartTasks.size() == 1) {
                    waitConditions = this.addStartTasks(wfStartTasks, 0, group, properties);
                    continue;
                }
                if (wfStartTasks.size() > 1) {
                    String starttaskdefitemid = properties.getProperty("starttaskdefitemid");
                    if (starttaskdefitemid.length() > 0) {
                        int findRow = wfStartTasks.findRow("taskdefitemid", starttaskdefitemid);
                        if (findRow >= 0) {
                            waitConditions = this.addStartTasks(wfStartTasks, findRow, group, properties);
                            continue;
                        }
                        throw new SapphireException("Failed to add SDIs to taskqueue because the start taskdefitemid '" + starttaskdefitemid + "' was not found for workflowexecid '" + workflowexecid + "'!");
                    }
                    if (workflowioname.length() > 0) {
                        int findRow = wfStartTasks.findRow("workflowioname", workflowioname);
                        if (findRow >= 0) {
                            waitConditions = this.addStartTasks(wfStartTasks, findRow, group, properties);
                            continue;
                        }
                        throw new SapphireException("Failed to add SDIs to taskqueue because the start IOid '" + workflowioname + "' was not found for workflowexecid '" + workflowexecid + "'!");
                    }
                    HashMap<String, String> findMap = new HashMap<String, String>();
                    findMap.put("connectortypesdcid", sdcid);
                    int findRow = wfStartTasks.findRow(findMap);
                    if (findRow >= 0) {
                        waitConditions = this.addStartTasks(wfStartTasks, findRow, group, properties);
                        if (wfStartTasks.findRow(findMap, findRow + 1) < 0) continue;
                        if (properties.getProperty("fromAjaxAddToWorkflow").equals("Y")) {
                            properties.setProperty("ambiguousstarttasks", "Y");
                            continue;
                        }
                        throw new SapphireException("Failed to add SDIs to taskqueue because there are multiple start tasks with connector of the same SDC. Unique SDC start connector cannot be found for workflowexecid '" + workflowexecid + "'!");
                    }
                    throw new SapphireException("Failed to add SDIs to taskqueue because no IOid was specifed and a matching SDC start connector cannot be found for workflowexecid '" + workflowexecid + "'!");
                }
                this.database.createPreparedResultSet("SELECT execstatus FROM workflowexec WHERE workflowexecid = ?", new Object[]{workflowexecid});
                this.database.getNext();
                String wfexecstatus = this.database.getValue("execstatus");
                if (wfexecstatus.length() > 0 && !wfexecstatus.equals("A")) {
                    throw new SapphireException("Failed to add SDIs to taskqueue because workflowexecid '" + workflowexecid + "' is in a " + WorkflowManagerCodes.getWorkflowExecStatusText(wfexecstatus) + " state!");
                }
                throw new SapphireException("Failed to add SDIs to taskqueue because no start tasks can be identified for workflowexecid '" + workflowexecid + "'!");
            }
            if (properties.getProperty("ambiguousstarttasks", "N").equals("N")) {
                WorkflowManager.addToTaskQueue(sapphireConnection, (DBUtil)this.database, propsDS, true);
                if (waitConditions) {
                    WorkflowManager.setupWaitConditions(sapphireConnection, new AutomationService(sapphireConnection), propsDS, null, null);
                }
                properties.setProperty("taskqueueitems", String.valueOf(propsDS.size()));
                properties.setProperty("statusmessage", propsDS.size() + " " + (propsDS.size() == 1 ? sdcProcessor.getProperty(sdcid, "singular") : sdcProcessor.getProperty(sdcid, "plural")).toLowerCase() + " added to task queue.");
            }
        }
        catch (Exception e) {
            throw new SapphireException("DB_ACTION_FAILED", "Failed execute db command. Exception: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
        }
        finally {
            damProcessor.clearRSet(rsetid);
        }
    }

    private boolean addStartTasks(DataSet wfStartTasks, int row, DataSet group, PropertyList properties) throws SapphireException {
        String roleid;
        String departmentid;
        String assignmentflag;
        String starttaskdefid = wfStartTasks.getValue(row, "taskdefid");
        String starttaskdefversionid = wfStartTasks.getValue(row, "taskdefversionid");
        String starttaskdefvariantid = wfStartTasks.getValue(row, "taskdefvariantid");
        String starttaskdefitemid = wfStartTasks.getValue(row, "taskdefitemid");
        String startscopeflag = wfStartTasks.getValue(row, "scopeflag");
        String autoexecflag = wfStartTasks.getValue(row, "autoexecflag", "N");
        String waittype = wfStartTasks.getValue(row, "waittype");
        String startioid = wfStartTasks.getValue(row, "ioid");
        String startconnectortypeid = wfStartTasks.getValue(row, "connectortypeid");
        String startconnectorsdcid = wfStartTasks.getValue(row, "connectortypesdcid");
        String string = properties.getProperty("assigndepartmentid").length() > 0 ? "D" : (properties.getProperty("assignsysuserid").length() > 0 ? "U" : (assignmentflag = properties.getProperty("assignroleid").length() > 0 ? "R" : wfStartTasks.getValue(row, "assignmentflag")));
        String string2 = properties.getProperty("assigndepartmentid").length() > 0 ? properties.getProperty("assigndepartmentid") : (departmentid = assignmentflag.equals("T") ? this.connectionInfo.getDefaultDepartment() : wfStartTasks.getValue(row, "departmentid"));
        String sysuserid = properties.getProperty("assignsysuserid").length() > 0 ? properties.getProperty("assignsysuserid") : (assignmentflag.equals("C") ? this.connectionInfo.getSysuserId() : wfStartTasks.getValue(row, "sysuserid"));
        String string3 = roleid = properties.getProperty("assignroleid").length() > 0 ? properties.getProperty("assignroleid") : wfStartTasks.getValue(row, "roleid");
        assignmentflag = departmentid.length() > 0 ? "D" : (sysuserid.length() > 0 ? "U" : (roleid.length() > 0 ? "R" : assignmentflag));
        for (int i = 0; i < group.size(); ++i) {
            if (!startconnectorsdcid.equals(group.getValue(i, "queuesdcid"))) {
                throw new SapphireException("New queue item sdcid '" + group.getValue(i, "queuesdcid") + "' does not match task connector type '" + startconnectorsdcid + "'");
            }
            group.setValue(i, "taskdefid", starttaskdefid);
            group.setValue(i, "taskdefversionid", starttaskdefversionid);
            group.setValue(i, "taskdefvariantid", starttaskdefvariantid);
            group.setValue(i, "taskdefitemid", starttaskdefitemid);
            group.setValue(i, "ioid", startioid);
            group.setValue(i, "connectortypeid", startconnectortypeid);
            group.setValue(i, "autoexec", autoexecflag);
            group.setValue(i, "waittype", waittype);
            group.setValue(i, "scopeflag", startscopeflag);
            group.setValue(i, "assignmentflag", assignmentflag);
            group.setValue(i, "assigneddepartment", departmentid);
            group.setValue(i, "assignedanalyst", sysuserid);
            group.setValue(i, "assignedrole", roleid);
        }
        return autoexecflag.equals("Y") || waittype.length() > 0;
    }
}

