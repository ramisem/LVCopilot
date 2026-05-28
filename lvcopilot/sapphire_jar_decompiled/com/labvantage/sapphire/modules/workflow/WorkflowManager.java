/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.workflow;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.workflow.CheckEventByPass;
import com.labvantage.sapphire.actions.workflow.TaskInputTimer;
import com.labvantage.sapphire.admin.ddt.LV_TaskDef;
import com.labvantage.sapphire.admin.ddt.LV_WorkflowDef;
import com.labvantage.sapphire.gwt.shared.JSONable;
import com.labvantage.sapphire.gwt.shared.JSONableString;
import com.labvantage.sapphire.modules.documents.Form;
import com.labvantage.sapphire.modules.documents.FormValue;
import com.labvantage.sapphire.modules.eventmanager.EventManager;
import com.labvantage.sapphire.modules.eventmanager.eventobject.TaskCompleteEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.TaskQueueEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.WorkflowCompleteEventObject;
import com.labvantage.sapphire.modules.workflow.TaskDef;
import com.labvantage.sapphire.pageelements.gwt.server.command.JSONableMap;
import com.labvantage.sapphire.pageelements.gwt.shared.TaskManagerConstants;
import com.labvantage.sapphire.pageelements.gwt.shared.WorkflowManagerCodes;
import com.labvantage.sapphire.pageelements.gwt.shared.WorkflowManagerConstants;
import com.labvantage.sapphire.services.ActionService;
import com.labvantage.sapphire.services.AutomationService;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.format.NumericFormatter;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import com.labvantage.sapphire.util.groovy.ProcessingUtil;
import com.labvantage.sapphire.util.groovy.RenderUtil;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIList;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.util.TaskContext;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class WorkflowManager
implements WorkflowManagerConstants,
TaskManagerConstants {
    public static final String LOGNAME = "WORKFLOWMANAGER";

    public static String createNewWorkflowExec(SapphireConnection sapphireConnection, DBUtil dbu, String workflowdefid, String workflowdefversionid, String workflowdefvariantid, String workflowexecname, String workflowexecdesc, PropertyListCollection variables, String activity, String reason, boolean signed) throws SapphireException {
        try {
            dbu.createPreparedResultSet("SELECT workflowdefid, execstatus, exectypeflag FROM workflowdef WHERE workflowdefid = ?  AND workflowdefversionid = ?  AND workflowdefvariantid = ?", new Object[]{workflowdefid, workflowdefversionid, workflowdefvariantid});
            if (dbu.getNext()) {
                String wfexecstatus = dbu.getValue("execstatus");
                if (wfexecstatus.equals("A") || wfexecstatus.length() == 0) {
                    String exectype = dbu.getValue("exectypeflag");
                    if (exectype.length() == 0) {
                        exectype = "S";
                    }
                    dbu.createPreparedResultSet("SELECT taskdefitemid FROM workflowdeftask WHERE workflowdefid = ?  AND workflowdefversionid = ?  AND workflowdefvariantid = ?  AND starttaskflag ='Y'", new Object[]{workflowdefid, workflowdefversionid, workflowdefvariantid});
                    if (dbu.getNext()) {
                        if (exectype.equals("S")) {
                            dbu.createPreparedResultSet("SELECT workflowexecid FROM workflowexec WHERE workflowdefid = ? AND workflowdefversionid = ? AND workflowdefvariantid = ?", new Object[]{workflowdefid, workflowdefversionid, workflowdefvariantid});
                            if (dbu.getNext()) {
                                return dbu.getValue("workflowexecid");
                            }
                            if (workflowexecname == null || workflowexecname.length() == 0) {
                                SequenceProcessor sequenceProcessor = new SequenceProcessor(sapphireConnection.getConnectionId());
                                int seq = sequenceProcessor.getSequence("LV_WorkflowExec", workflowdefid + ";" + workflowdefversionid + ";" + workflowdefvariantid);
                                workflowexecname = workflowdefid + "_" + seq;
                            }
                            if (workflowexecdesc == null || workflowexecdesc.length() == 0) {
                                workflowexecdesc = workflowdefid + " execution";
                            }
                        }
                        PropertyList actionProps = new PropertyList();
                        actionProps.setProperty("sdcid", "LV_WorkflowExec");
                        actionProps.setProperty("workflowexecname", workflowexecname);
                        actionProps.setProperty("workflowexecdesc", workflowexecdesc);
                        actionProps.setProperty("workflowdefid", workflowdefid);
                        actionProps.setProperty("workflowdefversionid", workflowdefversionid);
                        actionProps.setProperty("workflowdefvariantid", workflowdefvariantid);
                        actionProps.setProperty("startdt", "n");
                        actionProps.setProperty("startby", sapphireConnection.getSysuserId());
                        actionProps.setProperty("exectypeflag", exectype);
                        actionProps.setProperty("execstatus", "A");
                        actionProps.setProperty("auditactivity", activity);
                        actionProps.setProperty("auditreason", reason);
                        actionProps.setProperty("auditsignedflag", signed ? "Y" : "N");
                        PropertyList workflowexec = new PropertyList();
                        workflowexec.setProperty("variables", variables != null ? variables : new PropertyListCollection());
                        actionProps.setProperty("workflowexec", workflowexec.toXMLString());
                        ActionProcessor actionProcessor = new ActionProcessor(sapphireConnection.getConnectionId());
                        actionProcessor.processAction("AddSDI", "1", actionProps);
                        return actionProps.getProperty("newkeyid1");
                    }
                    throw new SapphireException("Failed to create new workflow execution because workflow " + LV_WorkflowDef.getText(workflowdefid, workflowdefversionid, workflowdefvariantid) + " does not have a start task defined!");
                }
                throw new SapphireException("Failed to create new workflow execution because workflow " + LV_WorkflowDef.getText(workflowdefid, workflowdefversionid, workflowdefvariantid) + " is in a " + WorkflowManagerCodes.getWorkflowExecStatusText(wfexecstatus) + " state!");
            }
            throw new SapphireException("Failed to create new workflow execution because workflow " + LV_WorkflowDef.getText(workflowdefid, workflowdefversionid, workflowdefvariantid) + " is invalid!");
        }
        catch (Exception e) {
            throw new SapphireException("Failed to create new workflow execution for workflow '" + LV_WorkflowDef.getText(workflowdefid, workflowdefversionid, workflowdefvariantid) + "'. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(sapphireConnection.getConnectionId())), e);
        }
    }

    public static SDIList getTaskQueueItems(SapphireConnection sapphireConnection, DBUtil dbu, String taskdefid, String taskdefversionid, String taskdefvariantid, String ioid, String workflowexecid, String workflowdefid, String workflowdefversionid, String workflowdefvariantid, String taskdefitemid, String taskExecGroup, int maxItems, boolean applyUserRestrictions, boolean applySecurity, boolean loadWaitTimes, String queueItems) throws SapphireException {
        SDIList sdiList = new SDIList();
        String sql = "";
        try {
            StringBuffer where = new StringBuffer((ioid != null && ioid.length() > 0 ? "ioid = '" + ioid + "' AND " : "") + " taskdefid = '" + taskdefid + "' AND taskdefversionid = '" + taskdefversionid + "' AND taskdefvariantid = '" + taskdefvariantid + "'");
            if (workflowdefid.length() > 0) {
                where.append(" AND workflowdefid = '").append(workflowdefid).append("' AND workflowdefversionid = '").append(workflowdefversionid).append("' AND workflowdefvariantid = '").append(workflowdefvariantid).append("'");
            }
            if (workflowexecid.length() > 0) {
                where.append(" AND workflowexecid IN ( '").append(StringUtil.replaceAll(workflowexecid, ";", "','")).append("' )");
            }
            if (taskExecGroup.length() > 0) {
                where.append(" AND taskexecgroup = '").append(taskExecGroup).append("'");
            }
            if (taskdefitemid.length() > 0) {
                where.append(" AND taskdefitemid = '").append(taskdefitemid).append("'");
            }
            sql = "SELECT taskqueueid, ioid, queuesdcid, queuekeyid1, queuekeyid2, queuekeyid3, queuestatus, assignedanalyst, assigneddepartment, queuestatus " + (loadWaitTimes ? (dbu.isOracle() ? ", to_char( trunc((((86400*(sysdate-createdt))/60)/60)/24) ) || ' days, ' || to_char( trunc(((86400*(sysdate-createdt))/60)/60)-24*(trunc((((86400*(sysdate-createdt))/60)/60)/24)) ) || ' hrs, ' || to_char( trunc((86400*(sysdate-createdt))/60)-60*(trunc(((86400*(sysdate-createdt))/60)/60)) ) || ' mins ' as queuetime " : ", CAST(DATEDIFF(MINUTE, createdt, getdate()) / 60 / 24 AS NVARCHAR(50)) + ' days, ' + CAST(DATEDIFF(MINUTE, createdt, getdate()) / 60 % 24  AS NVARCHAR(50)) + ' hrs, ' + CAST(DATEDIFF(MINUTE, createdt, getdate()) % 60 AS NVARCHAR(50)) + ' mins ' as queuetime ") : "") + "FROM   taskqueue WHERE  queuestatus IN ('" + StringUtil.replaceAll(queueItems, ";", "','") + "') AND " + where.toString() + " " + (applyUserRestrictions ? " AND " + WorkflowManager.getUserAssignmentClause("taskqueue", sapphireConnection.getSysuserId(), "assignedanalyst", "assigneddepartment", "assignedrole") : "") + (maxItems == -1 ? "ORDER BY taskqueueid, ioid, queuesdcid, queuekeyid1, queuekeyid2, queuekeyid3" : "ORDER BY createdt, ioid, queuesdcid, queuekeyid1, queuekeyid2, queuekeyid3");
            dbu.createResultSet(sql);
            while (dbu.getNext() && (maxItems == -1 || sdiList.size() < maxItems)) {
                sdiList.setSdcid(dbu.getValue("queuesdcid"));
                int index = sdiList.addSDI(dbu.getValue("queuekeyid1"), dbu.getValue("queuekeyid2"), dbu.getValue("queuekeyid3"));
                if (index == -1) continue;
                sdiList.setSDIAttribute(index, "taskqueueid", dbu.getValue("taskqueueid"));
                sdiList.setSDIAttribute(index, "assignedanalyst", dbu.getValue("assignedanalyst"));
                sdiList.setSDIAttribute(index, "assigneddepartment", dbu.getValue("assigneddepartment"));
                sdiList.setSDIAttribute(index, "queuestatus", dbu.getValue("queuestatus"));
                if (!loadWaitTimes) continue;
                sdiList.setSDIAttribute(index, "queuetime", dbu.getValue("queuetime"));
            }
            if (sdiList.size() > 0 && applySecurity) {
                WorkflowManager.applySecurity(new SDIProcessor(sapphireConnection.getConnectionId()), sdiList);
            } else if (ioid != null && ioid.length() > 0) {
                dbu.createPreparedResultSet("SELECT connectortype.connectortypesdcid FROM connectortype, taskdefio WHERE taskdefio.taskdefid = ? AND taskdefio.taskdefversionid = ? AND taskdefio.taskdefvariantid = ? AND taskdefio.ioid = ? AND   taskdefio.connectortypeid = connectortype.connectortypeid", new Object[]{taskdefid, taskdefversionid, taskdefvariantid, ioid});
                if (dbu.getNext()) {
                    sdiList.setSdcid(dbu.getValue("connectortypesdcid"));
                }
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to get task queue items with query '" + sql + "'. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(sapphireConnection.getConnectionId())), e);
        }
        return sdiList;
    }

    private static String[] generateTaskQueueID(SequenceProcessor sequenceProcessor, int count) throws SapphireException {
        String[] ret = new String[count];
        for (int i = 0; i < count; ++i) {
            String num;
            int id = sequenceProcessor.getSequence("taskqueue", "taskqueue");
            if (id == -1) {
                throw new SapphireException("Error getting sequence for tracelog");
            }
            ret[i] = num = NumericFormatter.formatNumber(id, "00000000");
        }
        return ret;
    }

    public static SDIData getSDIData(SDIList sdiList, SDIProcessor sdiProcessor) {
        SDIData returnSDIData = null;
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setRequestItem("primarykeys");
        String sdcid = sdiList.getSdcid();
        if (sdcid.equalsIgnoreCase("SDIWorkItem")) {
            StringBuffer list = new StringBuffer();
            int itemcount = 0;
            for (int i = 0; i < sdiList.size(); ++i) {
                String sdiworkitemid = sdiList.getKeyid1(i);
                list.append(",'").append(sdiworkitemid).append("'");
                if (list.length() <= 3900 && ++itemcount <= 500 && i != sdiList.size() - 1) continue;
                sdiRequest.setSDCid("SDIWorkItem");
                sdiRequest.setQueryFrom("sdiworkitem");
                sdiRequest.setQueryWhere("sdiworkitemid IN (" + list.substring(1) + ")");
                SDIData tempSdiData = sdiProcessor.getSDIData(sdiRequest);
                if (returnSDIData == null) {
                    returnSDIData = tempSdiData;
                } else {
                    DataSet subprimary = tempSdiData.getDataset("primary");
                    returnSDIData.getDataset("primary").copyRow(subprimary, -1, 1);
                }
                list.setLength(0);
                itemcount = 0;
            }
        } else {
            sdiRequest.setSDIList(sdiList);
            returnSDIData = sdiProcessor.getSDIData(sdiRequest);
        }
        return returnSDIData;
    }

    public static void applySecurity(SDIProcessor sdiProcessor, SDIList sdiList) {
        SDIData sdiData = WorkflowManager.getSDIData(sdiList, sdiProcessor);
        if (sdiData != null && sdiData.getDataset("primary") != null) {
            DataSet primary = sdiData.getDataset("primary");
            String[] keys = sdiData.getKeys("primary");
            primary.setKeyColumns(new HashSet<String>(Arrays.asList(keys)));
            primary.getIndex().setAutoIndexing(true);
            HashMap<String, String> keyMap = new HashMap<String, String>();
            for (int i = sdiList.size() - 1; i >= 0; --i) {
                int findRow;
                keyMap.clear();
                keyMap.put(keys[0], sdiList.getKeyid1(i));
                if (keys.length >= 2 && keys[1].length() > 0) {
                    keyMap.put(keys[1], sdiList.getKeyid2(i));
                }
                if (keys.length >= 3 && keys[2].length() > 0) {
                    keyMap.put(keys[2], sdiList.getKeyid3(i));
                }
                if ((findRow = primary.findRow(keyMap)) != -1) continue;
                sdiList.removeSDI(i);
            }
        }
    }

    public static void moveTaskQueueItems(SapphireConnection sapphireConnection, DBUtil dbu, String taskqueueid, String workflowexecid, String workflowdefid, String workflowdefversionid, String workflowdefvariantid, String taskdefid, String taskdefversionid, String taskdefvariantid, String taskdefitemid, String ioid) throws SapphireException {
        try {
            WorkflowManager.updateTaskQueue(sapphireConnection, dbu, "workflowexecid = ?, workflowdefid = ?, workflowdefversionid = ?, workflowdefvariantid = ?, taskdefid = ?, taskdefversionid = ?, taskdefvariantid = ?, taskdefitemid = ?, ioid = ?", "taskqueueid IN ( '" + StringUtil.replaceAll(taskqueueid, ";", "','") + "' )", new Object[]{workflowexecid, workflowdefid, workflowdefversionid, workflowdefvariantid, taskdefid, taskdefversionid, taskdefvariantid, taskdefitemid, ioid}, false);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to move task queue items. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(sapphireConnection.getConnectionId())), e);
        }
    }

    public static void newTaskQueueItems(SapphireConnection sapphireConnection, DBUtil dbu, String workflowexecid, String taskdefitemid, String ioid, SDIList newQueueItems) throws SapphireException {
        try {
            DataSet taskqueue;
            dbu.createPreparedResultSet("SELECT workflowdeftask.workflowdefid, workflowdeftask.workflowdefversionid, workflowdeftask.workflowdefvariantid, workflowdeftask.taskdefid, workflowdeftask.taskdefversionid, workflowdeftask.taskdefvariantid,       workflowdeftask.departmentid, workflowdeftask.sysuserid, workflowdeftask.roleid, workflowdeftaskio.connectortypeid FROM   workflowexec, workflowdeftask, workflowdeftaskio WHERE  workflowexec.workflowdefid = workflowdeftask.workflowdefid  AND   workflowexec.workflowdefversionid = workflowdeftask.workflowdefversionid  AND   workflowexec.workflowdefvariantid = workflowdeftask.workflowdefvariantid  AND   workflowdeftaskio.workflowdefid = workflowdeftask.workflowdefid  AND   workflowdeftaskio.workflowdefversionid = workflowdeftask.workflowdefversionid  AND   workflowdeftaskio.workflowdefvariantid = workflowdeftask.workflowdefvariantid  AND   workflowdeftaskio.taskdefitemid = workflowdeftask.taskdefitemid  AND   workflowdeftaskio.ioid = ?  AND   workflowexec.workflowexecid = ?  AND   workflowdeftask.taskdefitemid = ? ", new Object[]{ioid, workflowexecid, taskdefitemid});
            if (dbu.getNext()) {
                String workflowdefid = dbu.getValue("workflowdefid");
                String workflowdefversionid = dbu.getValue("workflowdefversionid");
                String workflowdefvariantid = dbu.getValue("workflowdefvariantid");
                String taskdefid = dbu.getValue("taskdefid");
                String taskdefversionid = dbu.getValue("taskdefversionid");
                String taskdefvariantid = dbu.getValue("taskdefvariantid");
                String departmentid = dbu.getValue("departmentid");
                String sysuserid = dbu.getValue("sysuserid");
                String roleid = dbu.getValue("roleid");
                String connectortypeid = dbu.getValue("connectortypeid");
                taskqueue = new DataSet();
                for (int i = 0; i < newQueueItems.size(); ++i) {
                    int row = taskqueue.addRow();
                    taskqueue.setString(row, "queuesdcid", newQueueItems.getSdcid());
                    taskqueue.setString(row, "queuekeyid1", newQueueItems.getKeyid1(i));
                    taskqueue.setString(row, "queuekeyid2", newQueueItems.getKeyid2(i));
                    taskqueue.setString(row, "queuekeyid3", newQueueItems.getKeyid3(i));
                    taskqueue.setString(row, "workflowexecid", workflowexecid);
                    taskqueue.setString(row, "workflowdefid", workflowdefid);
                    taskqueue.setString(row, "workflowdefversionid", workflowdefversionid);
                    taskqueue.setString(row, "workflowdefvariantid", workflowdefvariantid);
                    taskqueue.setString(row, "taskdefitemid", taskdefitemid);
                    taskqueue.setString(row, "taskdefid", taskdefid);
                    taskqueue.setString(row, "taskdefversionid", taskdefversionid);
                    taskqueue.setString(row, "taskdefvariantid", taskdefvariantid);
                    taskqueue.setString(row, "ioid", ioid);
                    taskqueue.setString(row, "connectortypeid", connectortypeid);
                    taskqueue.setString(row, "assigneddepartment", departmentid);
                    taskqueue.setString(row, "assignedanalyst", sysuserid);
                    taskqueue.setString(row, "assignedrole", roleid);
                }
            } else {
                throw new SapphireException("Failed to find workflowdeftask for workflowexec '" + workflowexecid + "' and taskdefitemid '" + taskdefitemid + "'!");
            }
            WorkflowManager.addToTaskQueue(sapphireConnection, dbu, taskqueue, true);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to add new task queue items. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(sapphireConnection.getConnectionId())), e);
        }
    }

    public static void saveTask(SapphireConnection sapphireConnection, DBUtil dbu, String taskexecid, TaskContext taskContext, JSONableMap taskData) throws SapphireException {
        try {
            dbu.createPreparedResultSet("SELECT taskdefid, taskdefversionid, taskdefvariantid, taskexec FROM taskexec WHERE taskexecid = ?", new Object[]{taskexecid});
            if (!dbu.getNext()) {
                throw new SapphireException("Failed to find taskexec '" + taskexecid + "'!");
            }
            PropertyList taskexec = new PropertyList();
            taskexec.setPropertyList(dbu.getClob("taskexec"));
            WorkflowManager.updateTaskExec(sapphireConnection, dbu, taskexec, taskContext, taskData, null, new StringBuffer());
            taskexec.setProperty("stepid", taskData.getString("stepid"));
            taskexec.setProperty("cancellable", taskData.getString("cancellable"));
            PropertyList editProps = new PropertyList();
            editProps.setProperty("sdcid", "LV_TaskExec");
            editProps.setProperty("keyid1", taskexecid);
            editProps.setProperty("taskexec", taskexec.toXMLString());
            ActionService actionService = new ActionService(sapphireConnection);
            actionService.processAction("EditSDI", "1", editProps);
        }
        catch (Exception e) {
            throw new SapphireException("Failed save task. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(sapphireConnection.getConnectionId())), e);
        }
    }

    public static void suspendTask(SapphireConnection sapphireConnection, DBUtil dbu, String taskexecid, TaskContext taskContext, JSONableMap taskData, String waitUntil) throws SapphireException {
        try {
            dbu.createPreparedResultSet("SELECT taskdefid, taskdefversionid, taskdefvariantid, taskexec FROM taskexec WHERE taskexecid = ?", new Object[]{taskexecid});
            if (!dbu.getNext()) {
                throw new SapphireException("Failed to find taskexec '" + taskexecid + "'!");
            }
            PropertyList taskexec = new PropertyList();
            taskexec.setPropertyList(dbu.getClob("taskexec"));
            WorkflowManager.updateTaskExec(sapphireConnection, dbu, taskexec, taskContext, taskData, null, new StringBuffer());
            taskexec.setProperty("stepid", taskData.getString("stepid"));
            taskexec.setProperty("cancellable", taskData.getString("cancellable"));
            String status = "P";
            if (waitUntil.length() > 0) {
                status = "W";
                AutomationService automationService = new AutomationService(sapphireConnection);
                PropertyList actionProps = new PropertyList();
                actionProps.setProperty("taskexecid", taskexecid);
                automationService.addToDoListEntry(sapphireConnection.getSysuserId(), "WakeTaskExec", "1", actionProps, waitUntil, true);
            }
            PropertyList editProps = new PropertyList();
            editProps.setProperty("sdcid", "LV_TaskExec");
            editProps.setProperty("keyid1", taskexecid);
            editProps.setProperty("taskexec", taskexec.toXMLString());
            editProps.setProperty("execstatus", status);
            editProps.setProperty("connectionid", "(null)");
            ActionService actionService = new ActionService(sapphireConnection);
            actionService.processAction("EditSDI", "1", editProps);
        }
        catch (Exception e) {
            throw new SapphireException("Failed save task. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(sapphireConnection.getConnectionId())), e);
        }
    }

    public static boolean completeTask(SapphireConnection sapphireConnection, DBUtil dbu, String taskexecid, TaskContext taskContext, JSONableMap taskData) throws SapphireException {
        DataSet logDebug = new DataSet();
        try {
            int i;
            boolean pollImmediate = false;
            dbu.createPreparedResultSet("SELECT taskdefid, taskdefversionid, taskdefvariantid, taskexec, workflowexecid, taskdefitemid FROM taskexec WHERE taskexecid = ?", new Object[]{taskexecid});
            if (!dbu.getNext()) {
                throw new SapphireException("Failed to find taskexec '" + taskexecid + "'!");
            }
            TaskDef taskDef = TaskDef.getInstance(sapphireConnection, dbu.getValue("taskdefid"), dbu.getValue("taskdefversionid"), dbu.getValue("taskdefvariantid"));
            PropertyList taskexec = new PropertyList();
            taskexec.setPropertyList(dbu.getClob("taskexec"));
            String workflowexecid = dbu.getValue("workflowexecid");
            String taskdefitemid = dbu.getValue("taskdefitemid");
            boolean removeFromQueue = true;
            JSONableMap taskVariables = taskData.getJSONableMap("variables");
            dbu.createPreparedResultSet("SELECT taskexecitem.itemsdcid, taskexecitem.itemkeyid1, taskexecitem.itemkeyid2, taskexecitem.itemkeyid3, taskexecitem.workflowexecid, taskexecitem.taskdefitemid, taskexecitem.ioid,        taskqueue.assignmentflag, taskqueue.assignedanalyst, taskqueue.assigneddepartment, taskqueue.assignedrole FROM taskexecitem, taskqueue WHERE taskexecitem.taskqueueid = taskqueue.taskqueueid AND taskexecid = ? AND typeflag = 'I'", new Object[]{taskexecid});
            DataSet taskexecitems = new DataSet(dbu.getResultSet());
            WorkflowManager.addDebugLog(sapphireConnection, taskexecid, logDebug, "TASKDEFID: " + taskDef.getTaskdefid() + ", TASKDEFVERSIONID: " + taskDef.getTaskdefversionid() + ", TASKDEFVARIANTID: " + taskDef.getTaskdefvariantid(), false);
            ArrayList<String> workflowexecs = new ArrayList<String>();
            ArrayList<String> taskdefitemsids = new ArrayList<String>();
            if (workflowexecid.length() == 0 && taskexecitems.size() > 0) {
                for (int i2 = 0; i2 < taskexecitems.size(); ++i2) {
                    if (workflowexecs.contains(taskexecitems.getValue(i2, "workflowexecid"))) continue;
                    workflowexecs.add(taskexecitems.getValue(i2, "workflowexecid"));
                    taskdefitemsids.add(taskexecitems.getValue(i2, "taskdefitemid"));
                }
            } else {
                workflowexecs.add(workflowexecid);
                taskdefitemsids.add(taskdefitemid);
            }
            if (workflowexecs.size() > 0) {
                WorkflowManager.addDebugLog(sapphireConnection, taskexecid, logDebug, taskexecitems.size() + " task input(s) in " + workflowexecs.size() + " workflow execution(s)", true);
                StringBuffer inClause = new StringBuffer();
                for (int i3 = 0; i3 < workflowexecs.size(); ++i3) {
                    inClause.append(",'").append((String)workflowexecs.get(i3)).append("'");
                }
                SafeSQL safeSQL = new SafeSQL();
                dbu.createPreparedResultSet("workflowexec", "SELECT workflowexecid, workflowexec FROM workflowexec WHERE workflowexecid IN (" + safeSQL.addIn(inClause.substring(1)) + ")", safeSQL.getValues());
                while (dbu.getNext("workflowexec")) {
                    int index = workflowexecs.indexOf(dbu.getValue("workflowexec", "workflowexecid"));
                    PropertyList workflowexec = new PropertyList();
                    workflowexec.setPropertyList(dbu.getClob("workflowexec", "workflowexec"));
                    PropertyListCollection wfeVariables = workflowexec.getCollection("variables");
                    if (wfeVariables == null || wfeVariables.size() <= 0) continue;
                    StringBuffer updates = new StringBuffer();
                    for (i = 0; i < wfeVariables.size(); ++i) {
                        JSONable value;
                        PropertyList wfeVariable = wfeVariables.getPropertyList(i);
                        String wfeTaskdefitemid = wfeVariable.getProperty("taskdefitemid");
                        if (wfeTaskdefitemid.length() <= 0 || !wfeTaskdefitemid.equals(taskdefitemsids.get(index)) || wfeVariable.getProperty("taskdefvariableid").length() <= 0 || (value = TaskDef.getTaskVariable(taskVariables, wfeVariable.getProperty("taskdefvariableid"))) == null || !(value instanceof JSONableString)) continue;
                        wfeVariable.setProperty("value", ((JSONableString)value).toString());
                        updates.append(", ").append(wfeVariable.getProperty("taskdefvariableid")).append("=").append(value);
                    }
                    if (updates.length() <= 0) continue;
                    WorkflowManager.addDebugLog(sapphireConnection, taskexecid, logDebug, "Updating workflowexec variables: " + updates.substring(2), true);
                    dbu.executePreparedUpdate("UPDATE workflowexec SET workflowexec = ? WHERE workflowexecid = ?", new Object[]{workflowexec.toXMLString(), workflowexecs.get(index)});
                }
            }
            if (taskexecitems.size() > 0) {
                dbu.createPreparedResultSet("SELECT distinct taskexecitem.workflowexecid, workflowexec.workflowdefid, workflowexec.workflowdefversionid, workflowexec.workflowdefvariantid, taskexecitem.taskdefitemid,        workflowdeftask.departmentid, workflowdeftask.sysuserid, workflowdeftask.roleid, workflowdeftask.assignmentflag,        connectortype.connectortypesdcid, workflowdeftaskio.ioid, workflowdeftaskio.connectioid, workflowdeftaskio.connecttaskdefitemid,         connectwftask.departmentid \"connectdepartmentid\", connectwftask.sysuserid \"connectsysuserid\", connectwftask.roleid \"connectroleid\", connectwftask.assignmentflag \"connectassignmentflag\",        connectwftaskio.taskdefid \"connecttaskdefid\", connectwftaskio.taskdefversionid \"connecttaskdefversionid\", connectwftaskio.taskdefvariantid \"connecttaskdefvariantid\", connectwftaskio.connectortypeid \"connectconnectortypeid\",        taskdefio.waittype, taskdef.autoexecflag \"connecttaskautoexec\", taskdef.scopeflag \"connecttaskscope\" FROM   workflowexec, workflowdeftask, workflowdeftaskio, workflowdeftask connectwftask, workflowdeftaskio connectwftaskio, taskdefio, taskexecitem, connectortype, taskdef  WHERE  workflowdeftaskio.connectortypeid = connectortype.connectortypeid  AND workflowexec.workflowexecid = taskexecitem.workflowexecid AND workflowdeftask.workflowdefid = workflowdeftaskio.workflowdefid AND workflowdeftask.workflowdefversionid = workflowdeftaskio.workflowdefversionid AND workflowdeftask.workflowdefvariantid = workflowdeftaskio.workflowdefvariantid AND workflowdeftask.taskdefitemid = workflowdeftaskio.taskdefitemid AND workflowdeftaskio.workflowdefid = connectwftask.workflowdefid AND workflowdeftaskio.workflowdefversionid = connectwftask.workflowdefversionid AND workflowdeftaskio.workflowdefvariantid = connectwftask.workflowdefvariantid AND workflowdeftaskio.workflowdefid = connectwftaskio.workflowdefid AND workflowdeftaskio.workflowdefversionid = connectwftaskio.workflowdefversionid AND workflowdeftaskio.workflowdefvariantid = connectwftaskio.workflowdefvariantid AND workflowdeftaskio.connecttaskdefitemid = connectwftask.taskdefitemid AND workflowdeftaskio.connecttaskdefitemid = connectwftaskio.taskdefitemid AND workflowdeftaskio.connectioid = connectwftaskio.ioid  AND workflowdeftaskio.workflowdefid = workflowexec.workflowdefid AND workflowdeftaskio.workflowdefversionid = workflowexec.workflowdefversionid AND workflowdeftaskio.workflowdefvariantid = workflowexec.workflowdefvariantid AND workflowdeftaskio.taskdefitemid = taskexecitem.taskdefitemid  AND connectwftask.taskdefid = taskdef.taskdefid AND connectwftask.taskdefversionid = taskdef.taskdefversionid AND connectwftask.taskdefvariantid = taskdef.taskdefvariantid AND connectwftaskio.taskdefid = taskdef.taskdefid AND connectwftaskio.taskdefversionid = taskdef.taskdefversionid AND connectwftaskio.taskdefvariantid = taskdef.taskdefvariantid AND connectwftaskio.taskdefid = taskdefio.taskdefid AND connectwftaskio.taskdefversionid = taskdefio.taskdefversionid AND connectwftaskio.taskdefvariantid = taskdefio.taskdefvariantid AND taskdefio.ioid = workflowdeftaskio.connectioid AND taskexecitem.taskexecid = ? AND taskexecitem.typeflag = 'I' AND workflowdeftaskio.ioflag = 'O' ORDER BY workflowdeftaskio.ioid", new Object[]{taskexecid});
            } else {
                dbu.createPreparedResultSet("SELECT taskexec.workflowexecid, workflowexec.workflowdefid, workflowexec.workflowdefversionid, workflowexec.workflowdefvariantid, taskexec.taskdefitemid,        workflowdeftask.departmentid, workflowdeftask.sysuserid, workflowdeftask.roleid, workflowdeftask.assignmentflag,        connectortype.connectortypesdcid, workflowdeftaskio.ioid, workflowdeftaskio.connectioid, workflowdeftaskio.connecttaskdefitemid,         connectwftask.departmentid \"connectdepartmentid\", connectwftask.sysuserid \"connectsysuserid\", connectwftask.roleid \"connectroleid\", connectwftask.assignmentflag \"connectassignmentflag\",        connectwftaskio.taskdefid \"connecttaskdefid\", connectwftaskio.taskdefversionid \"connecttaskdefversionid\", connectwftaskio.taskdefvariantid \"connecttaskdefvariantid\", connectwftaskio.connectortypeid \"connectconnectortypeid\",        taskdefio.waittype, taskdef.autoexecflag \"connecttaskautoexec\", taskdef.scopeflag \"connecttaskscope\" FROM   workflowexec, workflowdeftask, workflowdeftaskio, workflowdeftask connectwftask, workflowdeftaskio connectwftaskio, taskdefio, taskexec, connectortype, taskdef  WHERE  workflowdeftaskio.connectortypeid = connectortype.connectortypeid  AND workflowexec.workflowexecid = taskexec.workflowexecid AND workflowdeftask.workflowdefid = workflowdeftaskio.workflowdefid AND workflowdeftask.workflowdefversionid = workflowdeftaskio.workflowdefversionid AND workflowdeftask.workflowdefvariantid = workflowdeftaskio.workflowdefvariantid AND workflowdeftask.taskdefitemid = workflowdeftaskio.taskdefitemid AND workflowdeftaskio.workflowdefid = connectwftask.workflowdefid AND workflowdeftaskio.workflowdefversionid = connectwftask.workflowdefversionid AND workflowdeftaskio.workflowdefvariantid = connectwftask.workflowdefvariantid AND workflowdeftaskio.workflowdefid = connectwftaskio.workflowdefid AND workflowdeftaskio.workflowdefversionid = connectwftaskio.workflowdefversionid AND workflowdeftaskio.workflowdefvariantid = connectwftaskio.workflowdefvariantid AND workflowdeftaskio.connecttaskdefitemid = connectwftask.taskdefitemid AND workflowdeftaskio.connecttaskdefitemid = connectwftaskio.taskdefitemid AND workflowdeftaskio.connectioid = connectwftaskio.ioid  AND workflowdeftaskio.workflowdefid = workflowexec.workflowdefid AND workflowdeftaskio.workflowdefversionid = workflowexec.workflowdefversionid AND workflowdeftaskio.workflowdefvariantid = workflowexec.workflowdefvariantid AND workflowdeftaskio.taskdefitemid = taskexec.taskdefitemid  AND connectwftask.taskdefid = taskdef.taskdefid AND connectwftask.taskdefversionid = taskdef.taskdefversionid AND connectwftask.taskdefvariantid = taskdef.taskdefvariantid AND connectwftaskio.taskdefid = taskdef.taskdefid AND connectwftaskio.taskdefversionid = taskdef.taskdefversionid AND connectwftaskio.taskdefvariantid = taskdef.taskdefvariantid AND connectwftaskio.taskdefid = taskdefio.taskdefid AND connectwftaskio.taskdefversionid = taskdefio.taskdefversionid AND connectwftaskio.taskdefvariantid = taskdefio.taskdefvariantid AND taskdefio.ioid = workflowdeftaskio.connectioid AND taskexec.taskexecid = ? AND workflowdeftaskio.ioflag = 'O' ORDER BY workflowdeftaskio.ioid", new Object[]{taskexecid});
            }
            DataSet workflowconnections = new DataSet(dbu.getResultSet());
            boolean autoexecItems = false;
            boolean waitItems = false;
            DataSet taskqueue = new DataSet();
            HashMap<String, String> findMap = new HashMap<String, String>();
            PropertyListCollection taskio = taskDef.getTaskdef().getCollection("taskio");
            if (taskio != null) {
                for (i = 0; i < taskio.size(); ++i) {
                    PropertyList io = taskio.getPropertyList(i);
                    if (!io.getProperty("ioflag").equals("O")) continue;
                    String ioid = io.getProperty("ioid");
                    String outputtype = io.getProperty("outputtypeflag", "G");
                    String inputioid = io.getProperty("inputioid");
                    String variableid = io.getProperty("variableid");
                    SDIList taskitemList = new SDIList();
                    if (outputtype.equals("F")) {
                        WorkflowManager.addDebugLog(sapphireConnection, taskexecid, logDebug, "Deriving next input queue items for output queue '" + ioid + "' using inputs queue items from '" + inputioid + "' - PASS THROUGH", true);
                        for (int j = 0; j < taskexecitems.size(); ++j) {
                            if (!taskexecitems.getValue(j, "ioid").equals(inputioid)) continue;
                            taskitemList.setSdcid(taskexecitems.getValue(j, "itemsdcid"));
                            taskitemList.addSDI(taskexecitems.getValue(j, "itemkeyid1"), taskexecitems.getValue(j, "itemkeyid2"), taskexecitems.getValue(j, "itemkeyid3"));
                            findMap.put("workflowexecid", taskexecitems.getValue(j, "workflowexecid"));
                            findMap.put("taskdefitemid", taskexecitems.getValue(j, "taskdefitemid"));
                            findMap.put("ioid", ioid);
                            int findRow = workflowconnections.findRow(findMap);
                            if (findRow <= -1) continue;
                            String assignmentflag = taskexecitems.getValue(j, "assignmentflag");
                            String assigneddepartment = taskexecitems.getValue(j, "assigneddepartment");
                            String assignedanalyst = taskexecitems.getValue(j, "assignedanalyst");
                            String assignedrole = taskexecitems.getValue(j, "assignedrole");
                            String connectAssignmentFlag = workflowconnections.getValue(findRow, "connectassignmentflag", "I");
                            if (!connectAssignmentFlag.equals("I")) {
                                assignmentflag = connectAssignmentFlag;
                                assigneddepartment = connectAssignmentFlag.equals("T") ? sapphireConnection.getDefaultDepartment() : workflowconnections.getValue(findRow, "connectdepartmentid");
                                assignedanalyst = connectAssignmentFlag.equals("C") ? sapphireConnection.getSysuserId() : workflowconnections.getValue(findRow, "connectsysuserid");
                                assignedrole = workflowconnections.getValue(findRow, "connectroleid");
                            }
                            int row = taskqueue.addRow();
                            taskqueue.setString(row, "queuesdcid", taskexecitems.getValue(j, "itemsdcid"));
                            taskqueue.setString(row, "queuekeyid1", taskexecitems.getValue(j, "itemkeyid1"));
                            taskqueue.setString(row, "queuekeyid2", taskexecitems.getValue(j, "itemkeyid2"));
                            taskqueue.setString(row, "queuekeyid3", taskexecitems.getValue(j, "itemkeyid3"));
                            taskqueue.setString(row, "workflowexecid", workflowconnections.getValue(findRow, "workflowexecid"));
                            taskqueue.setString(row, "workflowdefid", workflowconnections.getValue(findRow, "workflowdefid"));
                            taskqueue.setString(row, "workflowdefversionid", workflowconnections.getValue(findRow, "workflowdefversionid"));
                            taskqueue.setString(row, "workflowdefvariantid", workflowconnections.getValue(findRow, "workflowdefvariantid"));
                            taskqueue.setString(row, "taskdefitemid", workflowconnections.getValue(findRow, "connecttaskdefitemid"));
                            taskqueue.setString(row, "taskdefid", workflowconnections.getValue(findRow, "connecttaskdefid"));
                            taskqueue.setString(row, "taskdefversionid", workflowconnections.getValue(findRow, "connecttaskdefversionid"));
                            taskqueue.setString(row, "taskdefvariantid", workflowconnections.getValue(findRow, "connecttaskdefvariantid"));
                            taskqueue.setString(row, "assignmentflag", assignmentflag);
                            taskqueue.setString(row, "assigneddepartment", assigneddepartment);
                            taskqueue.setString(row, "assignedanalyst", assignedanalyst);
                            taskqueue.setString(row, "assignedrole", assignedrole);
                            taskqueue.setString(row, "autoexec", workflowconnections.getValue(findRow, "connecttaskautoexec"));
                            taskqueue.setString(row, "waittype", workflowconnections.getValue(findRow, "waittype"));
                            taskqueue.setString(row, "scopeflag", workflowconnections.getValue(findRow, "connecttaskscope"));
                            taskqueue.setString(row, "fromioid", workflowconnections.getValue(findRow, "ioid"));
                            taskqueue.setString(row, "fromtaskexecid", taskexecid);
                            taskqueue.setString(row, "ioid", workflowconnections.getValue(findRow, "connectioid"));
                            taskqueue.setString(row, "connectortypeid", workflowconnections.getValue(findRow, "connectconnectortypeid"));
                            if (workflowconnections.getValue(findRow, "connecttaskautoexec").equals("Y")) {
                                autoexecItems = true;
                            }
                            if (workflowconnections.getValue(findRow, "waittype").length() <= 0) continue;
                            waitItems = true;
                        }
                        dbu.executePreparedUpdate("UPDATE taskexecitem SET typeflag = 'P' WHERE taskexecid = ? AND ioid = ? AND typeflag = 'I'", new Object[]{taskexecid, inputioid});
                    } else {
                        SDIList outputList = null;
                        JSONable value = TaskDef.getTaskVariable(taskVariables, variableid);
                        if (value != null) {
                            if (value instanceof SDIList) {
                                outputList = (SDIList)value;
                            } else if (value instanceof JSONableString) {
                                outputList = new SDIList();
                                outputList.addSDIList(((JSONableString)value).toString());
                            } else {
                                throw new SapphireException("Output variable '" + variableid + "' is not of type SDIList or String!");
                            }
                        }
                        WorkflowManager.addDebugLog(sapphireConnection, taskexecid, logDebug, "Deriving next input queue items for output queue '" + ioid + "' using variable " + variableid + "=" + outputList + " - " + (outputtype.equals("I") ? "ITEMIZED PASS THROUGH" : "GENERATED OUTPUTS"), true);
                        if (outputList != null && outputList.size() > 0) {
                            String connectAssignmentFlag;
                            if (outputtype.equals("I")) {
                                for (int j = 0; j < taskexecitems.size(); ++j) {
                                    int index = outputList.getListIndex(taskexecitems.getValue(j, "itemkeyid1"), taskexecitems.getValue(j, "itemkeyid2"), taskexecitems.getValue(j, "itemkeyid3"));
                                    if (index > -1) {
                                        taskitemList.setSdcid(taskexecitems.getValue(j, "itemsdcid"));
                                        taskitemList.addSDI(taskexecitems.getValue(j, "itemkeyid1"), taskexecitems.getValue(j, "itemkeyid2"), taskexecitems.getValue(j, "itemkeyid3"));
                                        if (workflowconnections.size() > 0) {
                                            findMap.put("workflowexecid", taskexecitems.getValue(j, "workflowexecid"));
                                            findMap.put("taskdefitemid", taskexecitems.getValue(j, "taskdefitemid"));
                                            findMap.put("ioid", ioid);
                                            int findRow = workflowconnections.findRow(findMap);
                                            if (findRow > -1) {
                                                String assignmentflag = taskexecitems.getValue(j, "assignmentflag");
                                                String assigneddepartment = taskexecitems.getValue(findRow, "assigneddepartment");
                                                String assignedanalyst = taskexecitems.getValue(j, "assignedanalyst");
                                                String assignedrole = taskexecitems.getValue(j, "assignedrole");
                                                connectAssignmentFlag = workflowconnections.getValue(findRow, "connectassignmentflag", "I");
                                                if (!connectAssignmentFlag.equals("I")) {
                                                    assignmentflag = connectAssignmentFlag;
                                                    assigneddepartment = connectAssignmentFlag.equals("T") ? sapphireConnection.getDefaultDepartment() : workflowconnections.getValue(findRow, "connectdepartmentid");
                                                    assignedanalyst = connectAssignmentFlag.equals("C") ? sapphireConnection.getSysuserId() : workflowconnections.getValue(findRow, "connectsysuserid");
                                                    assignedrole = workflowconnections.getValue(findRow, "connectroleid");
                                                }
                                                int row = taskqueue.addRow();
                                                taskqueue.setString(row, "queuesdcid", taskexecitems.getValue(j, "itemsdcid"));
                                                taskqueue.setString(row, "queuekeyid1", taskexecitems.getValue(j, "itemkeyid1"));
                                                taskqueue.setString(row, "queuekeyid2", taskexecitems.getValue(j, "itemkeyid2"));
                                                taskqueue.setString(row, "queuekeyid3", taskexecitems.getValue(j, "itemkeyid3"));
                                                taskqueue.setString(row, "workflowexecid", workflowconnections.getValue(findRow, "workflowexecid"));
                                                taskqueue.setString(row, "workflowdefid", workflowconnections.getValue(findRow, "workflowdefid"));
                                                taskqueue.setString(row, "workflowdefversionid", workflowconnections.getValue(findRow, "workflowdefversionid"));
                                                taskqueue.setString(row, "workflowdefvariantid", workflowconnections.getValue(findRow, "workflowdefvariantid"));
                                                taskqueue.setString(row, "taskdefitemid", workflowconnections.getValue(findRow, "connecttaskdefitemid"));
                                                taskqueue.setString(row, "taskdefid", workflowconnections.getValue(findRow, "connecttaskdefid"));
                                                taskqueue.setString(row, "taskdefversionid", workflowconnections.getValue(findRow, "connecttaskdefversionid"));
                                                taskqueue.setString(row, "taskdefvariantid", workflowconnections.getValue(findRow, "connecttaskdefvariantid"));
                                                taskqueue.setString(row, "assignmentflag", assignmentflag);
                                                taskqueue.setString(row, "assigneddepartment", assigneddepartment);
                                                taskqueue.setString(row, "assignedanalyst", assignedanalyst);
                                                taskqueue.setString(row, "assignedrole", assignedrole);
                                                taskqueue.setString(row, "autoexec", workflowconnections.getValue(findRow, "connecttaskautoexec"));
                                                taskqueue.setString(row, "waittype", workflowconnections.getValue(findRow, "waittype"));
                                                taskqueue.setString(row, "scopeflag", workflowconnections.getValue(findRow, "connecttaskscope"));
                                                taskqueue.setString(row, "fromioid", workflowconnections.getValue(findRow, "ioid"));
                                                taskqueue.setString(row, "fromtaskexecid", taskexecid);
                                                taskqueue.setString(row, "ioid", workflowconnections.getValue(findRow, "connectioid"));
                                                taskqueue.setString(row, "connectortypeid", workflowconnections.getValue(findRow, "connectconnectortypeid"));
                                                if (workflowconnections.getValue(findRow, "connecttaskautoexec").equals("Y")) {
                                                    autoexecItems = true;
                                                }
                                                if (workflowconnections.getValue(findRow, "waittype").length() > 0) {
                                                    waitItems = true;
                                                }
                                            }
                                        }
                                    }
                                    dbu.executePreparedUpdate("UPDATE taskexecitem SET typeflag = 'P' WHERE taskexecid = ? AND ioid = ? AND typeflag = 'I' AND itemsdcid = ? AND itemkeyid1 = ? AND itemkeyid2 = ? AND itemkeyid3 = ?", new Object[]{taskexecid, inputioid, taskexecitems.getValue(j, "itemsdcid"), taskexecitems.getValue(j, "itemkeyid1"), taskexecitems.getValue(j, "itemkeyid2"), taskexecitems.getValue(j, "itemkeyid3")});
                                }
                            } else {
                                HashMap<String, String> filterMap = new HashMap<String, String>();
                                filterMap.put("ioid", ioid);
                                DataSet workflowoutputs = workflowconnections.getFilteredDataSet(filterMap);
                                if (workflowoutputs.size() > 0) {
                                    String insert = "INSERT INTO taskexecitem ( taskexecid, itemsdcid, itemkeyid1, itemkeyid2, itemkeyid3, workflowexecid, taskdefitemid, taskdefid, taskdefversionid, taskdefvariantid, ioid, typeflag, usersequence,   createdt, createby, fromtaskexecid ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
                                    PreparedStatement ps = dbu.prepareStatement("addtotaskexecitem", insert);
                                    Timestamp now = DateTimeUtil.getNowTimestamp();
                                    for (int j = 0; j < workflowoutputs.size(); ++j) {
                                        String currentAssignmentFlag = workflowoutputs.getValue(j, "assignmentflag", "I");
                                        connectAssignmentFlag = workflowoutputs.getValue(j, "connectassignmentflag", "I");
                                        HashMap<String, String> findTEIMap = new HashMap<String, String>();
                                        for (int k = 0; k < outputList.size(); ++k) {
                                            taskitemList.setSdcid(workflowoutputs.getValue(j, "connectortypesdcid"));
                                            taskitemList.addSDI(outputList.getKeyid1(k), outputList.getKeyid2(k), outputList.getKeyid3(k));
                                            String assignmentflag = currentAssignmentFlag;
                                            String assigneddepartment = currentAssignmentFlag.equals("T") ? sapphireConnection.getDefaultDepartment() : workflowoutputs.getValue(j, "departmentid");
                                            String assignedanalyst = currentAssignmentFlag.equals("C") ? sapphireConnection.getSysuserId() : workflowoutputs.getValue(j, "sysuserid");
                                            String assignedrole = workflowoutputs.getValue(j, "roleid");
                                            findTEIMap.put("itemkeyid1", outputList.getKeyid1(k));
                                            findTEIMap.put("itemkeyid2", outputList.getKeyid2(k));
                                            findTEIMap.put("itemkeyid3", outputList.getKeyid3(k));
                                            int findRow = taskexecitems.findRow(findTEIMap);
                                            if (connectAssignmentFlag.equals("I")) {
                                                if (findRow > -1) {
                                                    assignmentflag = taskexecitems.getValue(findRow, "assignmentflag");
                                                    assigneddepartment = taskexecitems.getValue(findRow, "assigneddepartment");
                                                    assignedanalyst = taskexecitems.getValue(findRow, "assignedanalyst");
                                                    assignedrole = taskexecitems.getValue(findRow, "assignedrole");
                                                }
                                            } else {
                                                assignmentflag = connectAssignmentFlag;
                                                assigneddepartment = connectAssignmentFlag.equals("T") ? sapphireConnection.getDefaultDepartment() : workflowoutputs.getValue(j, "connectdepartmentid");
                                                assignedanalyst = connectAssignmentFlag.equals("C") ? sapphireConnection.getSysuserId() : workflowoutputs.getValue(j, "connectsysuserid");
                                                assignedrole = workflowoutputs.getValue(j, "connectroleid");
                                            }
                                            int row = taskqueue.addRow();
                                            String connectortypesdcid = workflowoutputs.getValue(j, "connectortypesdcid", outputList.getSdcid());
                                            taskqueue.setString(row, "queuesdcid", connectortypesdcid);
                                            taskqueue.setString(row, "queuekeyid1", outputList.getKeyid1(k));
                                            taskqueue.setString(row, "queuekeyid2", outputList.getKeyid2(k));
                                            taskqueue.setString(row, "queuekeyid3", outputList.getKeyid3(k));
                                            taskqueue.setString(row, "workflowexecid", workflowoutputs.getValue(j, "workflowexecid"));
                                            taskqueue.setString(row, "workflowdefid", workflowoutputs.getValue(j, "workflowdefid"));
                                            taskqueue.setString(row, "workflowdefversionid", workflowoutputs.getValue(j, "workflowdefversionid"));
                                            taskqueue.setString(row, "workflowdefvariantid", workflowoutputs.getValue(j, "workflowdefvariantid"));
                                            taskqueue.setString(row, "taskdefitemid", workflowoutputs.getValue(j, "connecttaskdefitemid"));
                                            taskqueue.setString(row, "taskdefid", workflowoutputs.getValue(j, "connecttaskdefid"));
                                            taskqueue.setString(row, "taskdefversionid", workflowoutputs.getValue(j, "connecttaskdefversionid"));
                                            taskqueue.setString(row, "taskdefvariantid", workflowoutputs.getValue(j, "connecttaskdefvariantid"));
                                            taskqueue.setString(row, "assignmentflag", assignmentflag);
                                            taskqueue.setString(row, "assigneddepartment", assigneddepartment);
                                            taskqueue.setString(row, "assignedanalyst", assignedanalyst);
                                            taskqueue.setString(row, "assignedrole", assignedrole);
                                            taskqueue.setString(row, "autoexec", workflowoutputs.getValue(j, "connecttaskautoexec"));
                                            taskqueue.setString(row, "waittype", workflowoutputs.getValue(j, "waittype"));
                                            taskqueue.setString(row, "scopeflag", workflowoutputs.getValue(j, "connecttaskscope"));
                                            taskqueue.setString(row, "fromioid", workflowoutputs.getValue(j, "ioid"));
                                            taskqueue.setString(row, "fromtaskexecid", taskexecid);
                                            taskqueue.setString(row, "ioid", workflowoutputs.getValue(j, "connectioid"));
                                            taskqueue.setString(row, "connectortypeid", workflowoutputs.getValue(j, "connectconnectortypeid"));
                                            if (workflowoutputs.getValue(j, "connecttaskautoexec").equals("Y")) {
                                                autoexecItems = true;
                                            }
                                            if (workflowoutputs.getValue(j, "waittype").length() > 0) {
                                                waitItems = true;
                                            }
                                            try {
                                                ps.setString(1, taskexecid);
                                                ps.setString(2, connectortypesdcid);
                                                ps.setString(3, outputList.getKeyid1(k));
                                                ps.setString(4, outputList.getKeyid2(k));
                                                ps.setString(5, outputList.getKeyid3(k));
                                                ps.setString(6, workflowoutputs.getValue(j, "workflowexecid"));
                                                ps.setString(7, workflowoutputs.getValue(j, "taskdefitemid"));
                                                ps.setString(8, taskDef.getTaskdefid());
                                                ps.setString(9, taskDef.getTaskdefversionid());
                                                ps.setString(10, taskDef.getTaskdefvariantid());
                                                ps.setString(11, ioid);
                                                ps.setString(12, "O");
                                                ps.setInt(13, k);
                                                ps.setTimestamp(14, now);
                                                ps.setString(15, sapphireConnection.getSysuserId());
                                                ps.setNull(16, 12);
                                                ps.executeUpdate();
                                                continue;
                                            }
                                            catch (SQLException e) {
                                                dbu.executePreparedUpdate("UPDATE taskexecitem SET typeflag = 'P' WHERE taskexecid = ? AND ioid = ? AND typeflag = 'I' AND itemsdcid = ? AND itemkeyid1 = ? AND itemkeyid2 = ? AND itemkeyid3 = ?", new Object[]{taskexecid, inputioid, workflowoutputs.getValue(j, "connectortypesdcid", outputList.getSdcid()), outputList.getKeyid1(k), outputList.getKeyid2(k), outputList.getKeyid3(k)});
                                            }
                                        }
                                    }
                                } else {
                                    taskitemList.setSdcid(outputList.getSdcid());
                                    for (int j = 0; j < outputList.size(); ++j) {
                                        taskitemList.addSDI(outputList.getKeyid1(j), outputList.getKeyid2(j), outputList.getKeyid3(j));
                                    }
                                }
                            }
                        }
                    }
                    EventManager.generateEvent(sapphireConnection, null, new TaskCompleteEventObject(taskDef.getTaskdefid(), taskDef.getTaskdefversionid(), taskDef.getTaskdefvariantid(), ioid, taskexecid, taskitemList));
                    if (workflowexecs.size() <= 0) continue;
                    StringBuffer inClause = new StringBuffer();
                    for (int j = 0; j < workflowexecs.size(); ++j) {
                        inClause.append(",'").append((String)workflowexecs.get(j)).append("'");
                    }
                    dbu.createPreparedResultSet("workflowtask", "SELECT workflowdeftask.workflowdefid, workflowdeftask.workflowdefversionid, workflowdeftask.workflowdefvariantid, endtaskflag, assignmentflag FROM workflowexec, workflowdeftask WHERE workflowexecid IN (" + inClause.substring(1) + ")  AND workflowdeftask.workflowdefid = workflowexec.workflowdefid  AND workflowdeftask.workflowdefversionid = workflowexec.workflowdefversionid  AND workflowdeftask.workflowdefvariantid = workflowexec.workflowdefvariantid   AND workflowdeftask.taskdefid = ?  AND workflowdeftask.taskdefversionid = ?  AND workflowdeftask.taskdefvariantid = ?", new Object[]{taskDef.getTaskdefid(), taskDef.getTaskdefversionid(), taskDef.getTaskdefvariantid()});
                    while (dbu.getNext("workflowtask")) {
                        if (!dbu.getValue("workflowtask", "endtaskflag").equals("Y")) continue;
                        EventManager.generateEvent(sapphireConnection, null, new WorkflowCompleteEventObject(dbu.getValue("workflowtask", "workflowdefid"), dbu.getValue("workflowtask", "workflowdefversionid"), dbu.getValue("workflowtask", "workflowdefvariantid"), taskDef.getTaskdefid(), taskDef.getTaskdefversionid(), taskDef.getTaskdefvariantid(), ioid, taskexecid, taskitemList));
                    }
                }
            }
            WorkflowManager.addDebugLog(sapphireConnection, taskexecid, logDebug, "Adding " + taskqueue.size() + " items to taskqueue", true);
            WorkflowManager.addToTaskQueue(sapphireConnection, dbu, taskqueue, true);
            if (autoexecItems || waitItems) {
                AutomationService automationService = new AutomationService(sapphireConnection);
                ArrayList<DataSet> outputQueues = taskqueue.getGroupedDataSets("fromioid,taskdefitemid");
                for (int i4 = 0; i4 < outputQueues.size(); ++i4) {
                    DataSet outputQueue = outputQueues.get(i4);
                    if (outputQueue.size() <= 0) continue;
                    WorkflowManager.setupWaitConditions(sapphireConnection, automationService, outputQueue, taskexecid, logDebug);
                }
                pollImmediate = true;
            }
            if (removeFromQueue) {
                SafeSQL safeSQL = new SafeSQL();
                WorkflowManager.deleteTaskQueueItems(sapphireConnection, dbu, "taskqueueid IN (SELECT taskqueueid FROM taskexecitem WHERE taskexecid = " + safeSQL.addVar(taskexecid) + ")", safeSQL, false, true);
                dbu.executePreparedUpdate("UPDATE taskexecitem SET taskqueueid = NULL WHERE taskqueueid IN (SELECT taskqueueid FROM taskexecitem WHERE taskexecid = ?)", new Object[]{taskexecid});
                WorkflowManager.addDebugLog(sapphireConnection, taskexecid, logDebug, "Removed items from queue", true);
            }
            StringBuffer summary = new StringBuffer();
            WorkflowManager.updateTaskExec(sapphireConnection, dbu, taskexec, taskContext, taskData, logDebug, summary);
            taskexec.setProperty("stepid", "");
            PropertyList editProps = new PropertyList();
            editProps.setProperty("sdcid", "LV_TaskExec");
            editProps.setProperty("keyid1", taskexecid);
            editProps.setProperty("execstatus", "C");
            editProps.setProperty("taskexec", taskexec.toXMLString());
            editProps.setProperty("completedt", "now");
            editProps.setProperty("connectionid", "(null)");
            editProps.setProperty("summary", summary.toString());
            ActionService actionService = new ActionService(sapphireConnection);
            actionService.processAction("EditSDI", "1", editProps);
            if (workflowexecs.size() > 0) {
                StringBuffer inClause = new StringBuffer();
                for (int i5 = 0; i5 < workflowexecs.size(); ++i5) {
                    inClause.append(",'").append((String)workflowexecs.get(i5)).append("'");
                }
                SafeSQL safeSQL = new SafeSQL();
                dbu.createPreparedResultSet("workflowexeccomplete", "SELECT workflowexec.workflowexecid, count(taskqueue.workflowexecid) count FROM workflowdef, workflowexec LEFT OUTER JOIN taskqueue ON workflowexec.workflowexecid = taskqueue.workflowexecid WHERE workflowexec.workflowdefid = workflowdef.workflowdefid   AND workflowexec.workflowdefversionid = workflowdef.workflowdefversionid   AND workflowexec.workflowdefvariantid = workflowdef.workflowdefvariantid   AND workflowdef.exectypeflag = " + safeSQL.addVar("A") + "   AND workflowexec.workflowexecid IN (" + safeSQL.addIn(inClause.substring(1)) + ") GROUP BY workflowexec.workflowexecid", safeSQL.getValues());
                StringBuffer wfecomplete = new StringBuffer();
                while (dbu.getNext("workflowexeccomplete")) {
                    if (dbu.getInt("workflowexeccomplete", "count") != 0) continue;
                    wfecomplete.append(";").append(dbu.getValue("workflowexeccomplete", "workflowexecid"));
                }
                if (wfecomplete.length() > 0) {
                    PropertyList editWFEProps = new PropertyList();
                    editWFEProps.setProperty("sdcid", "LV_WorkflowExec");
                    editWFEProps.setProperty("keyid1", wfecomplete.substring(1));
                    editWFEProps.setProperty("execstatus", "C");
                    editWFEProps.setProperty("completedt", "now");
                    editWFEProps.setProperty("completeby", sapphireConnection.getSysuserId());
                    actionService.processAction("EditSDI", "1", editWFEProps);
                }
            }
            return pollImmediate;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to complete task exec '" + taskexecid + "'. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(sapphireConnection.getConnectionId())), e);
        }
    }

    public static void setupWaitConditions(SapphireConnection sapphireConnection, AutomationService automationService, DataSet queueItems, String taskexecid, DataSet logDebug) throws ServiceException, SapphireException {
        PropertyList actionProps = new PropertyList();
        String waittype = queueItems.getValue(0, "waittype");
        if (queueItems.getValue(0, "autoexec").equals("Y") && waittype.length() == 0) {
            actionProps.setProperty("taskqueueid", queueItems.getColumnValues("taskqueueid", ";"));
            automationService.addToDoListEntry(sapphireConnection.getSysuserId(), "ProcessAutoExecTask", "1", actionProps, "Y", true, sapphireConnection.getSysuserId(), "", "", "");
            if (taskexecid != null && taskexecid.length() > 0) {
                WorkflowManager.addDebugLog(sapphireConnection, taskexecid, logDebug, "Autoexec queue items added to TODOLIST: " + actionProps.getProperty("taskqueueid"), true);
            }
        } else if (waittype.length() > 0) {
            String taskdefid = queueItems.getValue(0, "taskdefid");
            String taskdefversionid = queueItems.getValue(0, "taskdefversionid");
            String taskdefvariantid = queueItems.getValue(0, "taskdefvariantid");
            String ioid = queueItems.getValue(0, "ioid");
            TaskDef waitTaskDef = TaskDef.getInstance(sapphireConnection, taskdefid, taskdefversionid, taskdefvariantid);
            PropertyListCollection waittaskio = waitTaskDef.getTaskdef().getCollection("taskio");
            PropertyList waitio = waittaskio.find("ioid", ioid);
            if (waitio != null) {
                if (waittype.equals("event")) {
                    String eventbypass;
                    PropertyList event = waitio.getPropertyList("event");
                    if (event != null && (eventbypass = event.getProperty("eventbypass")).length() > 0) {
                        actionProps.setProperty("taskdefid", taskdefid);
                        actionProps.setProperty("taskdefversionid", taskdefversionid);
                        actionProps.setProperty("taskdefvariantid", taskdefvariantid);
                        actionProps.setProperty("autoexec", waitTaskDef.isAutoexec() ? "Y" : "N");
                        actionProps.setProperty("expression", eventbypass);
                        actionProps.setProperty("sdcid", queueItems.getValue(0, "queuesdcid"));
                        actionProps.setProperty("keyid1", queueItems.getColumnValues("queuekeyid1", ";"));
                        actionProps.setProperty("keyid2", queueItems.getColumnValues("queuekeyid2", ";"));
                        actionProps.setProperty("keyid3", queueItems.getColumnValues("queuekeyid3", ";"));
                        ActionBlock ab = new ActionBlock();
                        ab.setActionClass("CheckEventByPass", CheckEventByPass.class.getName(), actionProps);
                        automationService.addToDoListEntry(sapphireConnection.getSysuserId(), ab, "Y", true, "", "");
                        if (taskexecid != null && taskexecid.length() > 0) {
                            WorkflowManager.addDebugLog(sapphireConnection, taskexecid, logDebug, "Wait queue items added to TODOLIST for event bypass check: " + new SDI(actionProps.getProperty("sdcid"), actionProps.getProperty("keyid1"), actionProps.getProperty("keyid2"), actionProps.getProperty("keyid3")), true);
                        }
                    }
                } else if (waittype.equals("timer")) {
                    int time = 0;
                    try {
                        time = Integer.parseInt(waitio.getProperty("waittime", "0"));
                    }
                    catch (Exception eventbypass) {
                        // empty catch block
                    }
                    actionProps.setProperty("taskqueueid", queueItems.getColumnValues("taskqueueid", ";"));
                    actionProps.setProperty("taskdefid", taskdefid);
                    actionProps.setProperty("taskdefversionid", taskdefversionid);
                    actionProps.setProperty("taskdefvariantid", taskdefvariantid);
                    actionProps.setProperty("autoexec", waitTaskDef.isAutoexec() ? "Y" : "N");
                    ActionBlock ab = new ActionBlock();
                    ab.setActionClass("TaskInputTimer", TaskInputTimer.class.getName(), actionProps);
                    automationService.addToDoListEntry(sapphireConnection.getSysuserId(), ab, "n+" + time + waitio.getProperty("waitunits", "M"), true, "", "");
                    if (taskexecid != null && taskexecid.length() > 0) {
                        WorkflowManager.addDebugLog(sapphireConnection, taskexecid, logDebug, "Timer queue items added to TODOLIST: " + actionProps.getProperty("taskqueueid"), true);
                    }
                }
            }
        }
    }

    private static void updateTaskExec(SapphireConnection sapphireConnection, DBUtil dbu, PropertyList taskexec, TaskContext taskContext, JSONableMap taskData, DataSet logDebug, StringBuffer summary) {
        JSONableMap variables = taskData.getJSONableMap("variables");
        DataSet log = taskData.getDataSet("log");
        if (log != null && log.size() > 0) {
            String summaryLogEntry = log.getValue(log.size() - 1, "logentry");
            if (summaryLogEntry.contains("$G{")) {
                HashMap bindMap = WorkflowManager.getGroovyBindMap(sapphireConnection, dbu, taskContext, variables);
                String[] groovyExpressions = Form.getGroovy(summaryLogEntry);
                for (int i = 0; i < groovyExpressions.length; ++i) {
                    String script = ProcessingUtil.insertHeaderCode(groovyExpressions[i], "def html = sapphireobjects.html;", false);
                    String value = null;
                    try {
                        value = GroovyUtil.getInstance(sapphireConnection).evaluateSecure(script, bindMap);
                        summaryLogEntry = StringUtil.replaceAll(summaryLogEntry, "$G{" + groovyExpressions[i] + "}", value);
                        continue;
                    }
                    catch (SapphireException e) {
                        Trace.logError("Failed to evaluate groovy expression in task summary. Reason: " + e.getMessage(), e);
                    }
                }
                log.setValue(log.size() - 1, "logentry", summaryLogEntry);
            }
            summary.append(summaryLogEntry.length() > 4000 ? summaryLogEntry.substring(0, 4000) : summaryLogEntry);
            if (logDebug != null) {
                log.copyRow(logDebug, -1, 1);
            }
            taskexec.setProperty("log", log.toJSONString());
        }
        PropertyListCollection taskexecVariables = taskexec.getCollection("variables");
        for (int i = 0; i < taskexecVariables.size(); ++i) {
            PropertyList taskexecVariable;
            String variableid = (taskexecVariable = taskexecVariables.getPropertyList(i)).getProperty("variableid");
            JSONableMap variable = (JSONableMap)variables.get(variableid);
            taskexecVariable.setProperty("value", variable != null ? variable.getJSONString("value") : "");
            taskexecVariable.setProperty("sysuserid", variable != null ? variable.getJSONString("sysuserid") : "");
            taskexecVariable.setProperty("timestamp", variable != null ? variable.getJSONString("timestamp") : "");
            taskexecVariable.setProperty("source", variable != null ? variable.getJSONString("source") : "");
            variables.remove(variableid);
        }
        for (String variableid : variables.keySet()) {
            JSONableMap variable = (JSONableMap)variables.get(variableid);
            PropertyList newvariable = new PropertyList();
            newvariable.setProperty("variableid", variableid);
            newvariable.setProperty("value", variable != null ? variable.getJSONString("value") : "");
            newvariable.setProperty("sysuserid", variable != null ? variable.getJSONString("sysuserid") : "");
            newvariable.setProperty("timestamp", variable != null ? variable.getJSONString("timestamp") : "");
            newvariable.setProperty("source", variable != null ? variable.getJSONString("source") : "");
            taskexecVariables.add(newvariable);
        }
        String history = taskData.getString("history");
        if (history != null && history.length() > 0) {
            taskexec.setProperty("history", history);
        }
    }

    private static void addDebugLog(SapphireConnection sapphireConnection, String taskexecid, DataSet logDebug, String logStatement, boolean addToTaskLog) {
        Trace.logDebug(LOGNAME, "Completing TASKEXECID: " + taskexecid + " - " + logStatement);
        if (addToTaskLog) {
            int row = logDebug.addRow();
            long now = System.currentTimeMillis();
            Date d = new Date(now);
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss.SSS");
            logDebug.setString(row, "sysuserid", sapphireConnection.getSysuserId());
            logDebug.setString(row, "logdt", sdf.format(d));
            logDebug.setString(row, "logtime", String.valueOf(now));
            logDebug.setString(row, "logentry", "Server debug - " + logStatement);
            logDebug.setString(row, "typeflag", "D");
        }
    }

    public static void cancelTask(SapphireConnection sapphireConnection, DBUtil dbu, String taskexecid, TaskContext taskContext, JSONableMap taskVariables, String activity, String reason, boolean signed, boolean delete, String canceloption) throws SapphireException {
        try {
            SafeSQL safeSQL;
            StringBuffer deleteClause;
            dbu.createPreparedResultSet("SELECT taskdefid, taskdefversionid, taskdefvariantid FROM taskexec WHERE taskexecid = ?", new Object[]{taskexecid});
            if (dbu.getNext()) {
                String taskdefid = dbu.getValue("taskdefid");
                String taskdefversionid = dbu.getValue("taskdefversionid");
                String taskdefvariantid = dbu.getValue("taskdefvariantid");
                WorkflowManager.doRunCancelTaskScript(sapphireConnection, dbu, taskContext, taskVariables, taskdefid, taskdefversionid, taskdefvariantid, "Cancel script ignored for task execution '" + taskexecid + "' - missing taskContext and/or taskVariables");
            }
            if (!delete) {
                PropertyList editProps = new PropertyList();
                editProps.setProperty("sdcid", "LV_TaskExec");
                editProps.setProperty("keyid1", taskexecid);
                editProps.setProperty("summary", "Task Cancelled");
                editProps.setProperty("execstatus", "X");
                editProps.setProperty("completedt", "now");
                editProps.setProperty("connectionid", "(null)");
                editProps.setProperty("auditactivity", activity);
                editProps.setProperty("auditreason", reason);
                editProps.setProperty("auditsignedflag", signed ? "Y" : "N");
                ActionService actionService = new ActionService(sapphireConnection);
                actionService.processAction("EditSDI", "1", editProps);
            }
            if (canceloption.equals("Y")) {
                WorkflowManager.updateTaskQueue(sapphireConnection, dbu, "queuestatus = 'W'", "taskqueueid IN (SELECT taskqueueid FROM taskexecitem WHERE taskexecid = ?) AND queuestatus = 'E'", new Object[]{taskexecid}, true);
            } else if (canceloption.equals("Q")) {
                SafeSQL safeSQL2 = new SafeSQL();
                WorkflowManager.deleteTaskQueueItems(sapphireConnection, dbu, "taskqueueid IN (SELECT taskqueueid FROM taskexecitem WHERE taskexecid = " + safeSQL2.addVar(taskexecid) + ") AND queuestatus = " + safeSQL2.addVar("E"), safeSQL2, false, true);
            } else if (canceloption.equals("E")) {
                deleteClause = new StringBuffer();
                safeSQL = new SafeSQL();
                dbu.createPreparedResultSet("SELECT itemsdcid, itemkeyid1, itemkeyid2, itemkeyid3, workflowexecid FROM taskexecitem WHERE taskexecid = ?", new Object[]{taskexecid});
                while (dbu.getNext()) {
                    deleteClause.append(" OR ( queuesdcid = ").append(safeSQL.addVar(dbu.getValue("itemsdcid"))).append(" AND ").append("queuekeyid1 = ").append(safeSQL.addVar(dbu.getValue("itemkeyid1"))).append(" AND ").append("queuekeyid2 = ").append(safeSQL.addVar(dbu.getValue("itemkeyid2"))).append(" AND ").append("queuekeyid3 = ").append(safeSQL.addVar(dbu.getValue("itemkeyid3"))).append(" AND ").append("workflowexecid = ").append(safeSQL.addVar(dbu.getValue("workflowexecid"))).append(" )");
                }
                if (deleteClause.length() > 0) {
                    WorkflowManager.deleteTaskQueueItems(sapphireConnection, dbu, deleteClause.substring(3), safeSQL, false, true);
                }
            } else if (canceloption.equals("W")) {
                deleteClause = new StringBuffer();
                safeSQL = new SafeSQL();
                dbu.createPreparedResultSet("SELECT itemsdcid, itemkeyid1, itemkeyid2, itemkeyid3, workflowdefid, workflowdefversionid, workflowdefvariantid FROM taskexecitem, workflowexec WHERE taskexecitem.workflowexecid = workflowexec.workflowexecid AND taskexecid = ?", new Object[]{taskexecid});
                while (dbu.getNext()) {
                    deleteClause.append(" OR ( queuesdcid = ").append(safeSQL.addVar(dbu.getValue("itemsdcid"))).append(" AND ").append("queuekeyid1 = ").append(safeSQL.addVar(dbu.getValue("itemkeyid1"))).append(" AND ").append("queuekeyid2 = ").append(safeSQL.addVar(dbu.getValue("itemkeyid2"))).append(" AND ").append("queuekeyid3 = ").append(safeSQL.addVar(dbu.getValue("itemkeyid3"))).append(" AND ").append("workflowdefid = ").append(safeSQL.addVar(dbu.getValue("workflowdefid"))).append(" AND ").append("workflowdefversionid = ").append(safeSQL.addVar(dbu.getValue("workflowdefversionid"))).append(" AND ").append("workflowdefvariantid = ").append(safeSQL.addVar(dbu.getValue("workflowdefvariantid"))).append(" )");
                }
                if (deleteClause.length() > 0) {
                    WorkflowManager.deleteTaskQueueItems(sapphireConnection, dbu, deleteClause.substring(3), safeSQL, false, true);
                }
            } else if (canceloption.equals("A")) {
                deleteClause = new StringBuffer();
                dbu.createPreparedResultSet("SELECT itemsdcid, itemkeyid1, itemkeyid2, itemkeyid3 FROM taskexecitem WHERE taskexecid = ?", new Object[]{taskexecid});
                safeSQL = new SafeSQL();
                while (dbu.getNext()) {
                    deleteClause.append(" OR ( queuesdcid = ").append(safeSQL.addVar(dbu.getValue("itemsdcid"))).append(" AND ").append("queuekeyid1 = ").append(safeSQL.addVar(dbu.getValue("itemkeyid1"))).append(" AND ").append("queuekeyid2 = ").append(safeSQL.addVar(dbu.getValue("itemkeyid2"))).append(" AND ").append("queuekeyid3 = ").append(safeSQL.addVar(dbu.getValue("itemkeyid3"))).append(" ) ");
                }
                if (deleteClause.length() > 0) {
                    WorkflowManager.deleteTaskQueueItems(sapphireConnection, dbu, deleteClause.substring(3), safeSQL, false, true);
                }
            }
            dbu.executePreparedUpdate("UPDATE taskexecitem SET taskqueueid = NULL WHERE taskexecid = ?", new Object[]{taskexecid});
            if (delete) {
                dbu.executePreparedUpdate("DELETE FROM taskexecitem WHERE taskexecid = ?", new Object[]{taskexecid});
                dbu.executePreparedUpdate("DELETE FROM taskexec WHERE taskexecid = ?", new Object[]{taskexecid});
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed cancel task. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(sapphireConnection.getConnectionId())), e);
        }
    }

    public static void runCancelTaskScript(SapphireConnection sapphireConnection, DBUtil dbu, TaskContext taskContext, JSONableMap taskVariables) throws SapphireException {
        try {
            String taskdefid;
            if (taskContext != null && (taskdefid = taskContext.getTaskdefid()) != null && taskdefid.length() > 0) {
                WorkflowManager.doRunCancelTaskScript(sapphireConnection, dbu, taskContext, taskVariables, taskdefid, taskContext.getTaskdefversionid(), taskContext.getTaskdefvariantid(), "Cancel script ignored - missing taskContext and/or taskVariables");
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed cancel task. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(sapphireConnection.getConnectionId())), e);
        }
    }

    private static void doRunCancelTaskScript(SapphireConnection sapphireConnection, DBUtil dbu, TaskContext taskContext, JSONableMap taskVariables, String taskdefid, String taskdefversionid, String taskdefvariantid, String errorMessage) throws SapphireException {
        TaskDef taskDef = TaskDef.getInstance(sapphireConnection, taskdefid, taskdefversionid, taskdefvariantid);
        String cancelscript = taskDef.getTaskdef().getProperty("cancelscript");
        if (cancelscript.length() > 0) {
            if (taskContext != null && taskVariables != null) {
                HashMap bindMap = WorkflowManager.getGroovyBindMap(sapphireConnection, dbu, taskContext, taskVariables);
                String script = ProcessingUtil.insertHeaderCode(cancelscript, true);
                GroovyUtil.getInstance(sapphireConnection).evaluateSecure(script, bindMap);
            } else {
                Trace.logWarn(errorMessage);
            }
        }
    }

    public static void assignTask(SapphireConnection sapphireConnection, DBUtil dbu, String taskqueueid, String sysuserid, String departmentid, String activity, String reason, boolean signed) throws SapphireException {
        try {
            PropertyList editProps = new PropertyList();
            editProps.setProperty("sdcid", "TaskQueueItem");
            editProps.setProperty("keyid1", taskqueueid);
            editProps.setProperty("assignmentflag", sysuserid.length() > 0 ? "U" : (departmentid.length() > 0 ? "D" : "I"));
            editProps.setProperty("assignedanalyst", sysuserid);
            editProps.setProperty("assigneddepartment", departmentid);
            editProps.setProperty("auditactivity", activity);
            editProps.setProperty("auditreason", reason);
            editProps.setProperty("auditsignedflag", signed ? "Y" : "N");
            ActionService actionService = new ActionService(sapphireConnection);
            actionService.processAction("EditSDI", "1", editProps);
        }
        catch (Exception e) {
            throw new SapphireException("Failed assign '" + sysuserid + "' to task queue '" + taskqueueid + "'. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(sapphireConnection.getConnectionId())), e);
        }
    }

    public static void assignTaskExec(SapphireConnection sapphireConnection, DBUtil dbu, String taskexecid, String sysuserid, String departmentid, String activity, String reason, boolean signed) throws SapphireException {
        try {
            PropertyList editProps = new PropertyList();
            editProps.setProperty("sdcid", "LV_TaskExec");
            editProps.setProperty("keyid1", taskexecid);
            editProps.setProperty("assignedanalyst", sysuserid);
            editProps.setProperty("assigneddepartment", departmentid);
            editProps.setProperty("auditactivity", activity);
            editProps.setProperty("auditreason", reason);
            editProps.setProperty("auditsignedflag", signed ? "Y" : "N");
            ActionService actionService = new ActionService(sapphireConnection);
            actionService.processAction("EditSDI", "1", editProps);
        }
        catch (Exception e) {
            throw new SapphireException("Failed assign '" + sysuserid + "' to taskexec '" + taskexecid + "'. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(sapphireConnection.getConnectionId())), e);
        }
    }

    public static void addToTaskQueue(SapphireConnection sapphireConnection, DBUtil dbu, DataSet entries, boolean generateEvent) throws SapphireException {
        if (entries.size() == 0) {
            return;
        }
        if (!(entries.isValidColumn("workflowexecid") && entries.isValidColumn("workflowdefid") && entries.isValidColumn("workflowdefversionid") && entries.isValidColumn("workflowdefvariantid") && entries.isValidColumn("taskdefitemid") && entries.isValidColumn("taskdefid") && entries.isValidColumn("taskdefversionid") && entries.isValidColumn("taskdefvariantid") && entries.isValidColumn("ioid") && entries.isValidColumn("connectortypeid") && entries.isValidColumn("queuesdcid") && entries.isValidColumn("queuekeyid1") && entries.isValidColumn("queuekeyid2") && entries.isValidColumn("queuekeyid3"))) {
            throw new SapphireException("Missing columns in taskqueue entries!");
        }
        try {
            SequenceProcessor sequenceProcessor = new SequenceProcessor(sapphireConnection.getConnectionId());
            String[] taskqueueid = WorkflowManager.generateTaskQueueID(sequenceProcessor, entries.getRowCount());
            String insert = "INSERT INTO taskqueue ( taskqueueid, workflowexecid, workflowdefid, workflowdefversionid, workflowdefvariantid, taskdefitemid, taskdefid, taskdefversionid, taskdefvariantid, ioid, connectortypeid, queuesdcid, queuekeyid1, queuekeyid2, queuekeyid3,   queuestatus, taskexecutions, taskexecgroup, createdt, createby, fromtaskexecid, assignmentflag, assigneddepartment, assignedanalyst, assignedrole ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?, ?, ?, ?, ?, ?, ? )";
            PreparedStatement ps = dbu.prepareStatement("addtotaskqueue", insert);
            Timestamp now = DateTimeUtil.getNowTimestamp();
            entries.addColumn("taskqueueid", 0);
            SDCProcessor sdcProcessor = new SDCProcessor(sapphireConnection.getConnectionId());
            for (int i = 0; i < entries.size(); ++i) {
                String sdcid = entries.getValue(i, "queuesdcid");
                int keycols = Integer.parseInt(sdcProcessor.getProperty(sdcid, "keycolumns"));
                String keyid1 = entries.getValue(i, "queuekeyid1");
                String keyid2 = keycols >= 2 ? entries.getValue(i, "queuekeyid2", "(null)") : "(null)";
                String keyid3 = keycols >= 3 ? entries.getValue(i, "queuekeyid3", "(null)") : "(null)";
                String workflowexecid = entries.getValue(i, "workflowexecid");
                String taskdefitemid = entries.getValue(i, "taskdefitemid");
                String ioid = entries.getValue(i, "ioid");
                if (sdcid.length() > 0 && keyid1.length() > 0 && !keyid1.equals("(null)")) {
                    dbu.createPreparedResultSet("checkexists", "SELECT taskqueueid FROM taskqueue WHERE queuesdcid = ? AND queuekeyid1 = ? AND queuekeyid2 = ? AND queuekeyid3 = ? AND workflowexecid = ? AND taskdefitemid = ? AND ioid = ?", new Object[]{sdcid, keyid1, keyid2, keyid3, workflowexecid, taskdefitemid, ioid});
                    if (!dbu.getNext("checkexists")) {
                        String taskexecgroup = entries.getValue(i, "taskexecgroup");
                        if (taskexecgroup.length() == 0) {
                            String scopeflag = "E";
                            if (scopeflag.equals("E")) {
                                taskexecgroup = "WFE:" + entries.getValue(i, "taskdefid") + ";" + entries.getValue(i, "workflowdefid") + ";" + workflowexecid;
                            } else if (scopeflag.equals("W")) {
                                taskexecgroup = "WF:" + entries.getValue(i, "taskdefid") + ";" + entries.getValue(i, "workflowdefid");
                            }
                        }
                        String queuestatus = entries.getValue(i, "queuestatus", "W");
                        if (entries.getValue(i, "autoexec").equals("Y")) {
                            queuestatus = "A";
                        }
                        if (entries.getValue(i, "waittype").length() > 0 && !entries.getValue(i, "waittype").equalsIgnoreCase("none")) {
                            queuestatus = queuestatus + entries.getValue(i, "waittype");
                        }
                        entries.setValue(i, "taskqueueid", "TQI_" + taskqueueid[i]);
                        ps.setString(1, entries.getValue(i, "taskqueueid"));
                        ps.setString(2, workflowexecid);
                        ps.setString(3, entries.getValue(i, "workflowdefid"));
                        ps.setString(4, entries.getValue(i, "workflowdefversionid"));
                        ps.setString(5, entries.getValue(i, "workflowdefvariantid"));
                        ps.setString(6, taskdefitemid);
                        ps.setString(7, entries.getValue(i, "taskdefid"));
                        ps.setString(8, entries.getValue(i, "taskdefversionid"));
                        ps.setString(9, entries.getValue(i, "taskdefvariantid"));
                        ps.setString(10, ioid);
                        ps.setString(11, entries.getValue(i, "connectortypeid"));
                        ps.setString(12, sdcid);
                        ps.setString(13, keyid1);
                        ps.setString(14, keyid2);
                        ps.setString(15, keyid3);
                        ps.setString(16, queuestatus);
                        ps.setString(17, taskexecgroup);
                        ps.setTimestamp(18, now);
                        ps.setString(19, sapphireConnection.getSysuserId());
                        ps.setString(20, entries.getValue(i, "fromtaskexecid"));
                        ps.setString(21, entries.getValue(i, "assignmentflag"));
                        ps.setString(22, entries.getValue(i, "assigneddepartment"));
                        ps.setString(23, entries.getValue(i, "assignedanalyst"));
                        ps.setString(24, entries.getValue(i, "assignedrole"));
                        ps.executeUpdate();
                        continue;
                    }
                    Trace.logInfo("Entry SDC/Keyid '" + sdcid + "/" + keyid1 + "' already exists on queue for task '" + LV_TaskDef.getText(entries.getValue(i, "taskdefid"), entries.getValue(i, "taskdefversionid"), entries.getValue(i, "taskdefvariantid")) + "' in workflow " + LV_WorkflowDef.getText(entries.getValue(i, "workflowdefid"), entries.getValue(i, "workflowdefversionid"), entries.getValue(i, "workflowdefvariantid")) + " - ignoring");
                    continue;
                }
                Trace.logError("Invalid SDC/Keyid '" + sdcid + "/" + keyid1 + "' for task queue entry for workflow " + LV_WorkflowDef.getText(entries.getValue(i, "workflowdefid"), entries.getValue(i, "workflowdefversionid"), entries.getValue(i, "workflowdefvariantid")));
            }
            if (generateEvent) {
                HashMap<String, String> filterMap = new HashMap<String, String>();
                filterMap.put("queuestatus", "W");
                DataSet waitingItems = entries.getFilteredDataSet(filterMap);
                if (waitingItems.size() > 0) {
                    EventManager.generateEvent(sapphireConnection, null, new TaskQueueEventObject(waitingItems, "add"));
                }
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to insert entries in taskqueue. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(sapphireConnection.getConnectionId())), e);
        }
    }

    public static int updateTaskQueue(SapphireConnection sapphireConnection, DBUtil dbu, String setClause, String whereClause, Object[] whereBindVars, boolean generateEvent) throws SapphireException {
        DataSet taskqueue = null;
        if (generateEvent) {
            if (whereBindVars != null) {
                dbu.createPreparedResultSet("SELECT * FROM taskqueue WHERE " + whereClause, whereBindVars);
            } else {
                dbu.createResultSet("SELECT * FROM taskqueue WHERE " + whereClause);
            }
            taskqueue = new DataSet(dbu.getResultSet());
        }
        int updates = 0;
        updates = whereBindVars != null ? dbu.executePreparedUpdate("UPDATE taskqueue SET " + setClause + " WHERE " + whereClause, whereBindVars) : dbu.executeUpdate("UPDATE taskqueue SET " + setClause + " WHERE " + whereClause);
        if (generateEvent && taskqueue != null && taskqueue.size() > 0) {
            EventManager.generateEvent(sapphireConnection, null, new TaskQueueEventObject(taskqueue, "update"));
        }
        return updates;
    }

    public static int deleteTaskQueueItems(SapphireConnection sapphireConnection, DBUtil dbu, String whereClause, SafeSQL safeSQL, boolean removeTaskexecitems, boolean generateEvent) throws SapphireException {
        DataSet taskqueue = null;
        if (generateEvent) {
            dbu.createPreparedResultSet("SELECT * FROM taskqueue WHERE " + whereClause, safeSQL.getValues());
            taskqueue = new DataSet(dbu.getResultSet());
        }
        if (removeTaskexecitems) {
            dbu.executePreparedUpdate("DELETE FROM taskexecitem WHERE taskqueueid IN ( SELECT taskqueueid FROM taskqueue WHERE " + whereClause + ")", safeSQL.getValues());
        }
        int deletes = dbu.executePreparedUpdate("DELETE FROM taskqueue WHERE " + whereClause, safeSQL.getValues());
        if (generateEvent && taskqueue != null && taskqueue.size() > 0) {
            EventManager.generateEvent(sapphireConnection, null, new TaskQueueEventObject(taskqueue, "delete"));
        }
        return deletes;
    }

    public static int activateTaskQueueItems(SapphireConnection sapphireConnection, DBUtil dbu, String taskqueueid, boolean generateEvent) throws SapphireException {
        QueryProcessor queryProcessor = new QueryProcessor(sapphireConnection.getConnectionId());
        SafeSQL safeSQL = new SafeSQL();
        String s = "SELECT taskqueueid, queuestatus FROM taskqueue WHERE taskqueueid IN (" + safeSQL.addIn(taskqueueid, ";") + ")";
        DataSet taskqueue = queryProcessor.getPreparedSqlDataSet(s, safeSQL.getValues());
        StringBuffer autoexecTaskqueueid = new StringBuffer();
        StringBuffer otherTaskqueueid = new StringBuffer();
        for (int i = 0; i < taskqueue.size(); ++i) {
            String queuestatus = taskqueue.getValue(0, "queuestatus");
            if (queuestatus.equals("Aevent") || queuestatus.equals("Atimer") || queuestatus.equals("A")) {
                autoexecTaskqueueid.append(";").append(taskqueue.getValue(i, "taskqueueid"));
                continue;
            }
            if (!queuestatus.equals("Wevent") && !queuestatus.equals("Wtimer") && !queuestatus.equals("W") && !queuestatus.equals("E")) continue;
            otherTaskqueueid.append(";").append(taskqueue.getValue(i, "taskqueueid"));
        }
        int activatedItems = 0;
        if (autoexecTaskqueueid.length() > 0) {
            AutomationService automationService = new AutomationService(sapphireConnection);
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("taskqueueid", autoexecTaskqueueid.substring(1));
            try {
                activatedItems += WorkflowManager.updateTaskQueue(sapphireConnection, dbu, "queuestatus = 'A'", "taskqueueid IN ('" + StringUtil.replaceAll(autoexecTaskqueueid.substring(1), ";", "','") + "')", null, generateEvent);
                automationService.addToDoListEntry(sapphireConnection.getSysuserId(), "ProcessAutoExecTask", "1", actionProps, "Y", true);
            }
            catch (ServiceException e) {
                throw new SapphireException("Failed to add autoexec taskqueue items to TODO list. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(sapphireConnection.getConnectionId())), e);
            }
        }
        if (otherTaskqueueid.length() > 0) {
            activatedItems += WorkflowManager.updateTaskQueue(sapphireConnection, dbu, "queuestatus = 'W'", "taskqueueid IN ('" + StringUtil.replaceAll(otherTaskqueueid.substring(1), ";", "','") + "')", null, generateEvent);
        }
        return activatedItems;
    }

    /*
     * Enabled aggressive block sorting
     */
    public static String resolveTokens(String value, JSONableMap variables, PropertyListCollection variablesDef, PropertyList step, ConnectionInfo connectionInfo) {
        if (value.length() > 0) {
            String[] tokens;
            for (String token : tokens = StringUtil.getTokens(value, "[", "]", false)) {
                String propertyid;
                String replaceWith = "";
                if (token.startsWith("$G")) continue;
                if (token.startsWith("variables.")) {
                    JSONable variableValue;
                    String variableid = token.indexOf(".", 10) > 0 ? token.substring(10, token.indexOf(".", 10)) : token.substring(10);
                    String variablePath = token.length() > 10 + variableid.length() + 1 ? token.substring(10 + variableid.length() + 1) : "";
                    JSONableMap variable = (JSONableMap)variables.get(variableid);
                    if (variable != null && (variableValue = variable.get("value")) != null) {
                        String type;
                        PropertyList variableDef = variablesDef.getIndexedPropertyList(variableid);
                        String string = variableDef != null ? variableDef.getProperty("type", "string") : (type = variableid.startsWith("__queue_") ? "sdilist" : "string");
                        if (type.equalsIgnoreCase("form") && variableValue instanceof FormValue) {
                            FormValue form = (FormValue)variableValue;
                            if (!variablePath.startsWith("fields.")) {
                                // empty if block
                            }
                        } else if (type.equalsIgnoreCase("sdilist") && variableValue instanceof SDIList) {
                            replaceWith = ((SDIList)variableValue).getSDIList(variablePath.equals("keyid1") ? SDIList.KeyId.KEYID1 : (variablePath.equals("keyid2") ? SDIList.KeyId.KEYID2 : (variablePath.equals("keyid3") ? SDIList.KeyId.KEYID3 : SDIList.KeyId.KEYID1)));
                        } else if (type.equalsIgnoreCase("string") || type.equalsIgnoreCase("number") || type.equalsIgnoreCase("date")) {
                            replaceWith = variableValue instanceof JSONableString ? ((JSONableString)variableValue).toString() : (variableValue instanceof String ? (String)((Object)variableValue) : variable.toString());
                        }
                    }
                    value = StringUtil.replaceAll(value, "[" + token + "]", replaceWith);
                    continue;
                }
                if (token.startsWith("steptypeproperty.") && step != null) {
                    PropertyList stepTypeProps = step.getPropertyList(step.getProperty("type"));
                    if (stepTypeProps != null) {
                        propertyid = token.substring(17);
                        replaceWith = WorkflowManager.resolveTokens(stepTypeProps.getProperty(propertyid), variables, variablesDef, step, connectionInfo);
                    }
                    value = StringUtil.replaceAll(value, "[" + token + "]", replaceWith);
                    continue;
                }
                if (token.startsWith("stepproperty.") && step != null) {
                    PropertyList stepProps = step.getPropertyList("stepprops");
                    if (stepProps != null) {
                        propertyid = token.substring(13);
                        replaceWith = WorkflowManager.resolveTokens(stepProps.getProperty(propertyid), variables, variablesDef, step, connectionInfo);
                    }
                    value = StringUtil.replaceAll(value, "[" + token + "]", replaceWith);
                    continue;
                }
                if (!token.startsWith("user.")) continue;
                String property = token.substring(5);
                if (property.equalsIgnoreCase("sysuserid")) {
                    value = StringUtil.replaceAll(value, "[" + token + "]", connectionInfo.getSysuserId());
                    continue;
                }
                if (property.equalsIgnoreCase("sysusername")) {
                    value = StringUtil.replaceAll(value, "[" + token + "]", connectionInfo.getSysuserName());
                    continue;
                }
                if (property.equalsIgnoreCase("defaultdepartment")) {
                    value = StringUtil.replaceAll(value, "[" + token + "]", connectionInfo.getDefaultDepartment());
                    continue;
                }
                if (property.equalsIgnoreCase("departmentlist")) {
                    value = StringUtil.replaceAll(value, "[" + token + "]", connectionInfo.getDepartmentList());
                    continue;
                }
                if (property.equalsIgnoreCase("connectionid")) {
                    value = StringUtil.replaceAll(value, "[" + token + "]", connectionInfo.getConnectionId());
                    continue;
                }
                if (!property.equalsIgnoreCase("databaseid")) continue;
                value = StringUtil.replaceAll(value, "[" + token + "]", connectionInfo.getDatabaseId());
            }
        }
        return value;
    }

    public static HashMap getGroovyBindMap(SapphireConnection sapphireConnection, DBUtil dbu, TaskContext taskContext, JSONableMap variables) {
        HashMap<String, Object> bindMap = new HashMap<String, Object>();
        sapphire.util.ConnectionInfo connectionInfo = new sapphire.util.ConnectionInfo(sapphireConnection);
        bindMap.put("taskContext", taskContext);
        bindMap.put("variables", TaskDef.getTaskVariablesProcessingMap(variables, connectionInfo));
        bindMap.put("user", connectionInfo.getUserAttributeMap());
        StringBuffer log = new StringBuffer();
        ProcessingUtil.getSapphireObjectBindings(sapphireConnection, bindMap, dbu, log, "EXPRESSIONEVAL", true, true, true, true, true, true);
        ((HashMap)bindMap.get("sapphireobjects")).put("html", new RenderUtil(sapphireConnection, 0));
        return bindMap;
    }

    public static String getUserAssignmentClause(String table, String sysuserid, String sysusercol, String departmentcol, String rolecol) {
        if (table.equalsIgnoreCase("taskexec")) {
            return "( " + WorkflowManager.getUserDeptRoleClauses(table, sysuserid, sysusercol, departmentcol, rolecol) + " )";
        }
        return "( " + table + ".assignmentflag = 'A' OR ( ( " + table + ".assignmentflag IS NULL OR " + table + ".assignmentflag = '' OR " + table + ".assignmentflag <> 'M' ) AND " + WorkflowManager.getUserDeptRoleClauses(table, sysuserid, sysusercol, departmentcol, rolecol) + ") )";
    }

    private static String getUserDeptRoleClauses(String table, String sysuserid, String sysusercol, String departmentcol, String rolecol) {
        return "( " + table + "." + sysusercol + " IS NULL OR " + table + "." + sysusercol + " = '' OR " + table + "." + sysusercol + " = '" + sysuserid + "' ) AND( " + table + "." + departmentcol + " IS NULL OR " + table + "." + departmentcol + " = '' OR " + table + "." + departmentcol + " IN ( SELECT departmentid FROM departmentsysuser WHERE sysuserid = '" + sysuserid + "' UNION SELECT defaultdepartment departmentid FROM sysuser WHERE sysuserid = '" + sysuserid + "' AND defaultdepartment IS NOT NULL ) ) AND( " + table + "." + rolecol + " IS NULL OR " + table + "." + rolecol + " = '' OR " + table + "." + rolecol + " IN ( SELECT roleid FROM sysuserrole WHERE sysuserid = '" + sysuserid + "' ) )";
    }
}

