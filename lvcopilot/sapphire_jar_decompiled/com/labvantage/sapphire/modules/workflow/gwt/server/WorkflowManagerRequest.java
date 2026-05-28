/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.workflow.gwt.server;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.RSet;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.actions.workflow.StartTaskExec;
import com.labvantage.sapphire.admin.ddt.LV_WorkflowDef;
import com.labvantage.sapphire.gwt.shared.JSONable;
import com.labvantage.sapphire.gwt.shared.JSONableString;
import com.labvantage.sapphire.modules.documents.FormValue;
import com.labvantage.sapphire.modules.workflow.TaskDef;
import com.labvantage.sapphire.modules.workflow.WorkflowManager;
import com.labvantage.sapphire.modules.workflow.gwt.server.steprequests.BaseStepRequest;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandRequest;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandResponse;
import com.labvantage.sapphire.pageelements.gwt.server.command.JSONableMap;
import com.labvantage.sapphire.pageelements.gwt.server.command.SDIMaint;
import com.labvantage.sapphire.pageelements.gwt.server.command.StandardCommandRequest;
import com.labvantage.sapphire.pageelements.gwt.shared.WorkflowManagerConstants;
import com.labvantage.sapphire.services.ActionService;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.DataAccessService;
import com.labvantage.sapphire.services.QueryService;
import com.labvantage.sapphire.services.RequestService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import com.labvantage.sapphire.util.groovy.ProcessingUtil;
import java.sql.CallableStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.error.ErrorHandler;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIList;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.util.TaskContext;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class WorkflowManagerRequest
extends StandardCommandRequest
implements WorkflowManagerConstants {
    private static String[] days = new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private static String[] datetexts = new String[]{"Today", "Yesterday", "", "", "", "", "", "Last Week", "Two Weeks Ago", "Three Weeks Ago", "Last Month"};
    private static String[] dateoffsets = new String[]{"0", "-1d", "-1d", "-1d", "-1d", "-1d", "-1d", "-7d", "-7d", "-7d", "-1m"};

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected boolean processCommand(String command, CommandRequest commandRequest, CommandResponse commandResponse) throws SapphireException {
        super.processCommand(command, commandRequest, commandResponse);
        DBUtil dbu = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            dbu.setConnection(this.sapphireConnection);
            if (command.equals("lwfm")) {
                this.loadWFM(commandRequest, commandResponse);
            } else if (command.equals("lw")) {
                this.loadWorkflow(commandRequest, commandResponse, dbu);
            } else if (command.equals("ew")) {
                this.editWorkflow(commandRequest, commandResponse, dbu);
            } else if (command.equals("lwex")) {
                this.loadWorkflowExecutions(commandRequest, commandResponse, dbu);
            } else if (command.equals("nwe")) {
                this.newWorkflowExec(commandRequest, commandResponse, dbu);
            } else if (command.equals("wenc")) {
                this.workflowExecNameCheck(commandRequest, commandResponse, dbu);
            } else if (command.equals("ewe")) {
                this.editWorkflowExec(commandRequest, commandResponse);
            } else if (command.equals("dwe")) {
                this.deleteWorkflowExec(commandRequest, commandResponse);
            } else if (command.equals("lt")) {
                this.loadTask(commandRequest, commandResponse, dbu);
            } else if (command.equals("lts")) {
                this.loadTaskStep(commandRequest, commandResponse);
            } else if (command.equals("sr")) {
                this.stepRequest(commandRequest, commandResponse, dbu);
            } else if (command.equals("lte")) {
                this.loadTaskExec(commandRequest, commandResponse, dbu);
            } else if (command.equals("ltex")) {
                this.loadTaskExecutions(commandRequest, commandResponse, dbu);
            } else if (command.equals("lwc")) {
                this.loadWorkflowCounts(commandRequest, commandResponse, dbu);
            } else if (command.equals("lqi")) {
                this.loadTaskQueueItems(commandRequest, commandResponse, dbu);
            } else if (command.equals("nqi")) {
                this.newTaskQueueItems(commandRequest, commandResponse, dbu);
            } else if (command.equals("mqi")) {
                this.moveTaskQueueItems(commandRequest, commandResponse, dbu);
            } else if (command.equals("dqi")) {
                this.deleteTaskQueueItems(commandRequest, commandResponse, dbu);
            } else if (command.equals("gtqd")) {
                this.getTaskQueueDetails(commandRequest, commandResponse, dbu);
            } else if (command.equals("aqi")) {
                this.activateTaskQueueItems(commandRequest, commandResponse, dbu);
            } else if (command.equals("ltp")) {
                this.loadTaskPreview(commandRequest, commandResponse, dbu);
            } else if (command.equals("st")) {
                this.startTask(commandRequest, commandResponse, dbu);
            } else if (command.equals("atqi")) {
                this.allocateTaskQueueItems(commandRequest, commandResponse);
            } else if (command.equals("sat")) {
                this.saveTask(commandRequest, commandResponse, dbu);
            } else if (command.equals("at")) {
                this.assignTask(commandRequest, commandResponse, dbu);
            } else if (command.equals("ate")) {
                this.assignTaskExec(commandRequest, commandResponse, dbu);
            } else if (command.equals("pte")) {
                this.pauseTaskExec(commandRequest, commandResponse, dbu);
            } else if (command.equals("ct")) {
                this.completeTask(commandRequest, commandResponse, dbu);
            } else if (command.equals("cte")) {
                this.cancelTaskExec(commandRequest, commandResponse, dbu);
            } else if (command.equals("rcts")) {
                this.runCancelTaskScript(commandRequest, commandResponse, dbu);
            } else if (command.equals("dte")) {
                this.deleteTaskExec(commandRequest, commandResponse);
            } else if (command.equals("lv")) {
                this.loadVariable(commandRequest, commandResponse);
            } else if (command.equals("sv")) {
                if (this.saveVariable(commandRequest, commandResponse)) {
                    boolean bl = true;
                    return bl;
                }
            } else if (command.equals("ege")) {
                this.evalGroovyExpression(commandRequest, commandResponse, dbu);
            } else if (command.equals("ps")) {
                this.processScript(commandRequest, commandResponse);
            } else if (command.equals("s")) {
                this.search(commandRequest, commandResponse, dbu);
            } else if (command.equals("lsdiwd")) {
                this.loadSDIWorkflowData(commandRequest, commandResponse, dbu);
            } else if (command.equals("gce")) {
                this.getCertifiedUsers(commandRequest, commandResponse, dbu);
            } else if (command.equals("gct")) {
                this.getConnectorType(commandRequest, commandResponse, dbu);
            }
            boolean bl = true;
            return bl;
        }
        finally {
            dbu.releaseConnection();
        }
    }

    private void activateTaskQueueItems(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) {
        try {
            String taskqueueid = commandRequest.getString("taskqueueid");
            WorkflowManager.activateTaskQueueItems(this.sapphireConnection, dbu, taskqueueid, true);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to activate queue items. Reason: " + e.getMessage(), e);
        }
    }

    private void allocateTaskQueueItems(CommandRequest commandRequest, CommandResponse commandResponse) {
        try {
            if (!commandRequest.getBoolean("testmode")) {
                PropertyList actionProps = new PropertyList();
                SDIList sdiList = commandRequest.getSDIList("sdilist");
                actionProps.setProperty("taskexecid", commandRequest.getString("taskexecid"));
                actionProps.setProperty("taskqueueid", sdiList.getSDIAttributeList("taskqueueid"));
                actionProps.setProperty("queueusage", commandRequest.getString("queueusage"));
                this.getActionProcessor().processAction("AllocateTaskQueueItems", "1", actionProps);
                commandResponse.set("taskexecid", actionProps.getProperty("taskexecid"));
            } else {
                commandResponse.set("taskexecid", "");
            }
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to add task execution items. Reason: " + e.getMessage(), e);
        }
    }

    private void assignTask(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) {
        try {
            WorkflowManager.assignTask(this.sapphireConnection, dbu, commandRequest.getString("taskqueueid"), commandRequest.getString("sysuserid"), commandRequest.getString("departmentid"), commandRequest.getString("auditactivity"), commandRequest.getString("auditreason"), commandRequest.getString("auditsignedflag").equals("Y"));
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed assign '" + commandRequest.getString("sysuserid") + "' to task queue '" + commandRequest.getString("taskqueueid") + "'. Reason: " + e.getMessage(), e);
        }
    }

    private void assignTaskExec(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) {
        try {
            WorkflowManager.assignTaskExec(this.sapphireConnection, dbu, commandRequest.getString("taskexecid"), commandRequest.getString("sysuserid"), commandRequest.getString("departmentid"), commandRequest.getString("auditactivity"), commandRequest.getString("auditreason"), commandRequest.getString("auditsignedflag").equals("Y"));
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed assign '" + commandRequest.getString("sysuserid") + "' to taskexec '" + commandRequest.getString("taskexecid") + "'. Reason: " + e.getMessage(), e);
        }
    }

    private void cancelTaskExec(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) {
        try {
            WorkflowManager.cancelTask(this.sapphireConnection, dbu, commandRequest.getString("taskexecid"), commandRequest.getTaskContext("taskcontext"), commandRequest.getJSONableMap("variables"), commandRequest.getString("auditactivity"), commandRequest.getString("auditreason"), commandRequest.getString("auditsignedflag").equals("Y"), commandRequest.getBoolean("delete"), commandRequest.getString("canceloption"));
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to cancel task execution. Reason: " + e.getMessage(), e);
        }
    }

    private void runCancelTaskScript(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) {
        try {
            WorkflowManager.runCancelTaskScript(this.sapphireConnection, dbu, commandRequest.getTaskContext("taskcontext"), commandRequest.getJSONableMap("variables"));
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to run cancel task script. Reason: " + e.getMessage(), e);
        }
    }

    private void completeTask(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) {
        try {
            WorkflowManager.completeTask(this.sapphireConnection, dbu, commandRequest.getString("taskexecid"), commandRequest.getTaskContext("taskcontext"), commandRequest.getJSONableMap("taskdata"));
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to complete task execution. Reason: " + e.getMessage(), e);
        }
    }

    private void editWorkflow(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) throws SapphireException {
        try {
            String workflowdefid = commandRequest.getString("workflowdefid");
            String workflowdefversionid = commandRequest.getString("workflowdefversionid", "1");
            String workflowdefvariantid = commandRequest.getString("workflowdefvariantid");
            String workflowexecid = commandRequest.getString("workflowexecid");
            String copyMode = commandRequest.getString("copymode");
            String newWorkflowdefid = commandRequest.getString("newworfkflowdefid");
            String newExecstatus = commandRequest.getString("newexecstatus");
            String newWorkflowdefvariantid = commandRequest.getString("newworkflowdefvariantid");
            String newWorkflowexecname = commandRequest.getString("newworkflowexecname");
            String newWorkflowexecdesc = commandRequest.getString("newworkflowexecdesc");
            PropertyListCollection variables = commandRequest.getCollection("variables");
            String queueMode = commandRequest.getString("queuemode");
            String auditReason = commandRequest.getString("auditreason");
            String auditActivity = commandRequest.getString("auditactivity");
            String auditSignedFlag = commandRequest.getString("auditsignedflag");
            DataSet taskqueue = null;
            if (newWorkflowexecname.length() > 0 && queueMode.equals("C")) {
                QueryProcessor queryProcessor = this.getQueryProcessor();
                taskqueue = queryProcessor.getPreparedSqlDataSet("SELECT * FROM taskqueue WHERE workflowexecid = ?", new Object[]{workflowexecid});
            }
            HashMap<String, String> actionProps = new HashMap<String, String>();
            actionProps.put("auditactivity", auditActivity);
            actionProps.put("auditreason", auditReason);
            actionProps.put("auditsignedflag", auditSignedFlag);
            if (copyMode.equals("VER")) {
                actionProps.put("sdcid", "LV_WorkflowDef");
                actionProps.put("keyid1", workflowdefid);
                actionProps.put("keyid2", workflowdefversionid);
                actionProps.put("keyid3", workflowdefvariantid);
                this.getActionProcessor().processAction("AddSDIVersion", "1", actionProps);
                if (newWorkflowexecname.length() > 0) {
                    String newWorkflowversionid = (String)actionProps.get("newkeyid2");
                    newWorkflowexecname = WorkflowManager.createNewWorkflowExec(this.sapphireConnection, dbu, workflowdefid, newWorkflowversionid, workflowdefvariantid, newWorkflowexecname, newWorkflowexecdesc, variables, auditActivity, auditReason, auditSignedFlag.equals("Y"));
                    commandResponse.set("workflowexecid", newWorkflowexecname);
                    if (queueMode.equals("M")) {
                        WorkflowManager.updateTaskQueue(this.sapphireConnection, dbu, "workflowexecid = '" + newWorkflowexecname + "', workflowversionid = '" + newWorkflowversionid + "'", "workflowexecid = ?", new Object[]{workflowexecid}, false);
                    } else if (queueMode.equals("C")) {
                        taskqueue.setValue(-1, "workflowexecid", newWorkflowexecname);
                        taskqueue.setValue(-1, "workflowdefversionid", newWorkflowversionid);
                        WorkflowManager.addToTaskQueue(this.sapphireConnection, dbu, taskqueue, false);
                    }
                }
            } else if (copyMode.equals("STS")) {
                actionProps.put("sdcid", "LV_WorkflowDef");
                actionProps.put("keyid1", workflowdefid);
                actionProps.put("keyid2", workflowdefversionid);
                actionProps.put("keyid3", workflowdefvariantid);
                actionProps.put("execstatus", newExecstatus);
                this.getActionProcessor().processAction("EditSDI", "1", actionProps);
            } else if (copyMode.equals("VAR")) {
                actionProps.put("sdcid", "LV_WorkflowDef");
                actionProps.put("keyid1", workflowdefid);
                actionProps.put("keyid2", workflowdefversionid);
                actionProps.put("keyid3", newWorkflowdefvariantid);
                actionProps.put("templatekeyid1", workflowdefid);
                actionProps.put("templatekeyid2", workflowdefversionid);
                actionProps.put("templatekeyid3", workflowdefvariantid);
                this.getActionProcessor().processAction("AddSDI", "1", actionProps);
                if (newWorkflowexecname.length() > 0) {
                    newWorkflowexecname = WorkflowManager.createNewWorkflowExec(this.sapphireConnection, dbu, workflowdefid, workflowdefversionid, newWorkflowdefvariantid, newWorkflowexecname, newWorkflowexecdesc, variables, auditActivity, auditReason, auditSignedFlag.equals("Y"));
                    commandResponse.set("workflowexecid", newWorkflowexecname);
                    if (queueMode.equals("M")) {
                        WorkflowManager.updateTaskQueue(this.sapphireConnection, dbu, "workflowexecid = '" + newWorkflowexecname + "', workflowdefvariantid = '" + newWorkflowdefvariantid + "'", "workflowexecid = ?", new Object[]{workflowexecid}, false);
                    } else if (queueMode.equals("C")) {
                        taskqueue.setValue(-1, "workflowexecid", newWorkflowexecname);
                        taskqueue.setValue(-1, "workflowdefvariantid", newWorkflowdefvariantid);
                        WorkflowManager.addToTaskQueue(this.sapphireConnection, dbu, taskqueue, false);
                    }
                }
            } else if (copyMode.equals("WFL")) {
                actionProps.put("sdcid", "LV_WorkflowDef");
                actionProps.put("keyid1", newWorkflowdefid);
                actionProps.put("keyid2", "1");
                actionProps.put("keyid3", newWorkflowdefvariantid);
                actionProps.put("templatekeyid1", workflowdefid);
                actionProps.put("templatekeyid2", workflowdefversionid);
                actionProps.put("templatekeyid3", workflowdefvariantid);
                this.getActionProcessor().processAction("AddSDI", "1", actionProps);
                if (newWorkflowexecname.length() > 0) {
                    newWorkflowexecname = WorkflowManager.createNewWorkflowExec(this.sapphireConnection, dbu, newWorkflowdefid, "1", newWorkflowdefvariantid, newWorkflowexecname, newWorkflowexecdesc, variables, auditActivity, auditReason, auditSignedFlag.equals("Y"));
                    commandResponse.set("workflowexecid", newWorkflowexecname);
                    if (queueMode.equals("M")) {
                        WorkflowManager.updateTaskQueue(this.sapphireConnection, dbu, "workflowexecid = '" + newWorkflowexecname + "', workflowdefid = '" + newWorkflowdefid + "', workflowdefversionid = '1', workflowdefvariantid = '" + newWorkflowdefvariantid + "'", "workflowexecid = ?", new Object[]{workflowexecid}, false);
                    } else if (queueMode.equals("C")) {
                        taskqueue.setValue(-1, "workflowexecid", newWorkflowexecname);
                        taskqueue.setValue(-1, "workflowdefid", newWorkflowdefid);
                        taskqueue.setValue(-1, "workflowdefversionid", "1");
                        taskqueue.setValue(-1, "workflowdefvariantid", newWorkflowdefvariantid);
                        WorkflowManager.addToTaskQueue(this.sapphireConnection, dbu, taskqueue, false);
                    }
                }
            }
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to copy workflow. Reason: " + e.getMessage(), e);
        }
    }

    private void deleteTaskExec(CommandRequest commandRequest, CommandResponse commandResponse) {
        try {
            HashMap<String, String> actionProps = new HashMap<String, String>();
            actionProps.put("sdcid", "LV_TaskExec");
            actionProps.put("keyid1", commandRequest.getString("taskexecid"));
            actionProps.put("auditactivity", commandRequest.getString("auditactivity"));
            actionProps.put("auditreason", commandRequest.getString("auditreason"));
            actionProps.put("auditsignedflag", commandRequest.getString("auditsignedflag"));
            this.getActionProcessor().processAction("DeleteSDI", "1", actionProps);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to remove task exec. Reason: " + e.getMessage(), e);
        }
    }

    private void deleteTaskQueueItems(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) {
        try {
            String tempTaskqueueid = commandRequest.getString("taskqueueid");
            DataSet temp = new DataSet();
            temp.addColumnValues("taskqueueid", 0, tempTaskqueueid, ";");
            ArrayList<DataSet> blocks = temp.getSplitDataSets(1000);
            for (DataSet block : blocks) {
                String taskqueueid = block.getColumnValues("taskqueueid", ";");
                String taskdefid = commandRequest.getString("taskdefid");
                if (taskqueueid.length() == 0 && taskdefid.length() > 0) {
                    SDIList taskqueueItems = WorkflowManager.getTaskQueueItems(this.sapphireConnection, dbu, taskdefid, commandRequest.getString("taskdefversionid"), commandRequest.getString("taskdefvariantid"), commandRequest.getString("ioid"), commandRequest.getString("workflowexecid"), commandRequest.getString("workflowdefid"), commandRequest.getString("workflowdefversionid"), commandRequest.getString("workflowdefvariantid"), commandRequest.getString("taskdefitemid"), commandRequest.getString("taskexecgroup"), -1, false, false, false, "W;Wevent;Wtimer;E;S;A;Aevent;Atimer");
                    taskqueueid = taskqueueItems.getSDIAttributeList("taskqueueid");
                }
                if (taskqueueid.length() <= 0) continue;
                SafeSQL safeSQL = new SafeSQL();
                WorkflowManager.deleteTaskQueueItems(this.sapphireConnection, dbu, "taskqueueid IN (" + safeSQL.addIn(taskqueueid, ";") + ")", safeSQL, true, true);
            }
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to remove queue items. Reason: " + e.getMessage(), e);
        }
    }

    private void deleteWorkflowExec(CommandRequest commandRequest, CommandResponse commandResponse) {
        try {
            HashMap<String, String> actionProps = new HashMap<String, String>();
            actionProps.put("sdcid", "LV_WorkflowExec");
            actionProps.put("keyid1", commandRequest.getString("workflowexecid"));
            actionProps.put("auditactivity", commandRequest.getString("auditactivity"));
            actionProps.put("auditreason", commandRequest.getString("auditreason"));
            actionProps.put("auditsignedflag", commandRequest.getString("auditsignedflag"));
            this.getActionProcessor().processAction("DeleteSDI", "1", actionProps);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to remove workflow exec. Reason: " + e.getMessage(), e);
        }
    }

    private void editWorkflowExec(CommandRequest commandRequest, CommandResponse commandResponse) {
        try {
            HashMap<String, String> actionProps = new HashMap<String, String>();
            actionProps.put("sdcid", "LV_WorkflowExec");
            actionProps.put("keyid1", commandRequest.getString("workflowexecid"));
            actionProps.put("workflowexecname", commandRequest.getString("workflowexecname"));
            actionProps.put("workflowexecdesc", commandRequest.getString("workflowexecdesc"));
            actionProps.put("execstatus", commandRequest.getString("execstatus"));
            if (commandRequest.getString("execstatus").equals("C")) {
                actionProps.put("completedt", "n");
                actionProps.put("completeby", this.connectionInfo.getSysuserId());
            } else {
                actionProps.put("completedt", "(null)");
                actionProps.put("completeby", "(null)");
            }
            PropertyList workflowexec = new PropertyList();
            workflowexec.setProperty("variables", commandRequest.getCollection("variables"));
            actionProps.put("workflowexec", workflowexec.toXMLString());
            actionProps.put("auditactivity", commandRequest.getString("auditactivity"));
            actionProps.put("auditreason", commandRequest.getString("auditreason"));
            actionProps.put("auditsignedflag", commandRequest.getString("auditsignedflag"));
            this.getActionProcessor().processAction("EditSDI", "1", actionProps);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to edit new workflow exec. Reason: " + e.getMessage(), e);
        }
    }

    private void evalGroovyExpression(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) {
        String[] expressions = StringUtil.split(commandRequest.getString("expressions", "expression"), ";");
        String[] values = StringUtil.split(commandRequest.getString("values", "value"), ";");
        HashMap bindMap = WorkflowManager.getGroovyBindMap(this.sapphireConnection, dbu, commandRequest.getTaskContext("taskcontext"), commandRequest.getJSONableMap("variables"));
        for (int i = 0; i < expressions.length; ++i) {
            String[] chunks = StringUtil.split(commandRequest.getString(expressions[i]), "[!@]");
            StringBuffer expression = new StringBuffer();
            for (int j = 0; j < chunks.length; ++j) {
                expression.append(EncryptDecrypt.decryptRSA(chunks[j].substring("{|}".length())));
            }
            try {
                String script = ProcessingUtil.insertHeaderCode(expression.toString().replaceAll("&gt;", ">").replaceAll("&lt;", "<").replaceAll("&quot;", "\"").replaceAll("&amp;", "&"), "def html = sapphireobjects.html;", false);
                HashMap output = new HashMap();
                bindMap.put("output", output);
                commandResponse.set(values[i], GroovyUtil.getInstance(this.sapphireConnection).evaluateSecure(script, bindMap));
                commandResponse.set("output", new JSONableMap((Map)output));
                continue;
            }
            catch (Exception e) {
                commandResponse.setStatusFail("Failed to evaluate groovy expression '" + expression + "'. Reason: " + ProcessingUtil.stripHeaderCode(e.getMessage()), e);
            }
        }
    }

    private void getConnectorType(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) {
        try {
            String connectortypeid = commandRequest.getString("connectortypeid");
            String connectortypesdcid = commandRequest.getString("connectortypesdcid");
            SafeSQL safeSQL = new SafeSQL();
            dbu.createPreparedResultSet("SELECT connectortypeid, connectortypedesc, connectortypesdcid, title, validation, color FROM connectortype " + (connectortypeid.length() > 0 ? "WHERE connectortypeid IN ( " + safeSQL.addIn(connectortypeid, ";") + " )" : (connectortypesdcid.length() > 0 ? "WHERE connectortypesdcid IN ( " + safeSQL.addIn(connectortypesdcid, ";") + " )" : "")), safeSQL.getValues());
            DataSet connectortypes = new DataSet();
            connectortypes.setResultSet(dbu.getResultSet());
            commandResponse.set("connectortype", connectortypes);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to get taskk queue workflow. Reason: " + e.getMessage(), e);
        }
    }

    private void getTaskQueueDetails(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) {
        try {
            String[] taskqueueid = StringUtil.split(commandRequest.getString("taskqueueid"), ";");
            dbu.createPreparedResultSet("SELECT * FROM taskqueue WHERE taskqueueid = ?", new Object[]{taskqueueid[0]});
            if (dbu.getNext()) {
                commandResponse.set("taskdefid", dbu.getValue("taskdefid"));
                commandResponse.set("taskdefversionid", dbu.getValue("taskdefversionid"));
                commandResponse.set("taskdefvariantid", dbu.getValue("taskdefvariantid"));
                commandResponse.set("taskdefitemid", dbu.getValue("taskdefitemid"));
                commandResponse.set("ioid", dbu.getValue("ioid"));
                commandResponse.set("connectortypeid", dbu.getValue("connectortypeid"));
                commandResponse.set("queuesdcid", dbu.getValue("queuesdcid"));
                commandResponse.set("queuekeyid1", dbu.getValue("queuekeyid1"));
                commandResponse.set("queuekeyid2", dbu.getValue("queuekeyid2"));
                commandResponse.set("queuekeyid3", dbu.getValue("queuekeyid3"));
                commandResponse.set("workflowexecid", dbu.getValue("workflowexecid"));
                commandResponse.set("workflowdefid", dbu.getValue("workflowdefid"));
                commandResponse.set("workflowdefversionid", dbu.getValue("workflowdefversionid"));
                commandResponse.set("workflowdefvariantid", dbu.getValue("workflowdefvariantid"));
            }
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to get taskk queue workflow. Reason: " + e.getMessage(), e);
        }
    }

    private void loadTaskStep(CommandRequest commandRequest, CommandResponse commandResponse) {
        try {
            String taskdefid = commandRequest.getString("taskdefid");
            String taskdefversionid = commandRequest.getString("taskdefversionid");
            String taskdefvariantid = commandRequest.getString("taskdefvariantid");
            String stepid = commandRequest.getString("stepid");
            TaskDef taskDef = TaskDef.getInstance(this.sapphireConnection, taskdefid, taskdefversionid, taskdefvariantid, true);
            PropertyList taskdef = taskDef.getTaskdef();
            PropertyListCollection steps = taskdef.getCollection("steps");
            PropertyList step = steps.find("stepid", stepid);
            commandResponse.set(stepid, step);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to load task step. Reason: " + e.getMessage(), e);
        }
    }

    private void loadTask(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) {
        try {
            PropertyListCollection taskio;
            String cancelpage;
            String completepage;
            PropertyList inputs;
            TaskDef taskdef = null;
            String taskdefid = commandRequest.getString("taskdefid");
            String taskdefversionid = commandRequest.getString("taskdefversionid");
            String taskdefvariantid = commandRequest.getString("taskdefvariantid");
            String workflowdefid = commandRequest.getString("workflowdefid");
            String workflowdefversionid = commandRequest.getString("workflowdefversionid");
            String workflowdefvariantid = commandRequest.getString("workflowdefvariantid");
            String taskdefitemid = commandRequest.getString("taskdefitemid");
            if (taskdefitemid.length() > 0) {
                taskdef = TaskDef.getInstance(this.sapphireConnection, workflowdefid, workflowdefversionid, workflowdefvariantid, taskdefitemid, true);
            } else if (taskdefid.length() > 0) {
                taskdef = TaskDef.getInstance(this.sapphireConnection, taskdefid, taskdefversionid, taskdefvariantid, true);
            }
            taskdefid = taskdef.getTaskdefid();
            commandResponse.set("taskdefid", taskdef.getTaskdefid());
            commandResponse.set("taskdefversionid", taskdef.getTaskdefversionid());
            commandResponse.set("taskdefvariantid", taskdef.getTaskdefvariantid());
            String workflowexecid = commandRequest.getString("workflowexecid");
            if (workflowexecid.length() == 0) {
                dbu.createPreparedResultSet("SELECT workflowexec.workflowexecid, workflowdef.exectypeflag FROM workflowexec, workflowdef WHERE workflowexec.workflowdefid = workflowdef.workflowdefid AND workflowexec.workflowdefversionid = workflowdef.workflowdefversionid AND workflowexec.workflowdefvariantid = workflowdef.workflowdefvariantid  AND workflowdef.workflowdefid = ? AND workflowdef.workflowdefversionid = ? AND workflowdef.workflowdefvariantid = ?", new Object[]{workflowdefid, workflowdefversionid, workflowdefvariantid});
                if (dbu.getNext()) {
                    commandResponse.set("workflowexecid", dbu.getValue("exectypeflag").equals("A") ? "" : dbu.getValue("workflowexecid"));
                } else {
                    commandResponse.set("workflowexecid", "");
                }
            }
            dbu.createPreparedResultSet("SELECT workflowexecname FROM workflowexec WHERE workflowexec.workflowexecid = ?", new Object[]{workflowexecid});
            if (dbu.getNext()) {
                commandResponse.set("workflowexecname", dbu.getValue("workflowexecname"));
            }
            if ((inputs = commandRequest.getPropertyList("requestorinputs")) == null) {
                inputs = new PropertyList();
            }
            if ((completepage = inputs.getProperty("completepage")).length() > 0 && taskdef.getTaskdef().getProperty("completepage").length() == 0) {
                taskdef.getTaskdef().setProperty("complete", "gotopage");
                taskdef.getTaskdef().setProperty("completepage", completepage);
            }
            if ((cancelpage = inputs.getProperty("cancelpage")).length() > 0 && taskdef.getTaskdef().getProperty("cancelpage").length() == 0) {
                taskdef.getTaskdef().setProperty("cancel", "gotopage");
                taskdef.getTaskdef().setProperty("cancelpage", cancelpage);
            }
            JSONableMap taskVariables = taskdef.setupTaskVariables(taskdef.getTaskdef().getCollection("variables"), workflowexecid, inputs, this.getQueryProcessor(), this.sapphireConnection);
            commandResponse.set("taskvariables", taskVariables);
            boolean testMode = commandRequest.getBoolean("testmode");
            boolean standaloneMode = (taskdef.isStandalone() || inputs.getProperty("standalone").equalsIgnoreCase("Y")) && inputs.getProperty("__taskmanagerui").equals("Y");
            commandResponse.set("standalonemode", standaloneMode ? "Y" : "N");
            if ((inputs.getProperty("__taskmanagerui", "N").equals("N") || testMode || standaloneMode) && (taskio = taskdef.getTaskdef().getCollection("taskio")) != null) {
                for (int i = 0; i < taskio.size(); ++i) {
                    PropertyList io = taskio.getPropertyList(i);
                    if (!io.getProperty("ioflag").equals("I") || !io.getProperty("autoselect").equals("Y") && !standaloneMode) continue;
                    String ioid = io.getProperty("ioid");
                    String variableid = io.getProperty("autoselect").equals("Y") ? io.getProperty("variableid") : "inputqueue_" + ioid;
                    io.setProperty("variableid", variableid);
                    SDIList sdiList = null;
                    if (standaloneMode || testMode) {
                        String inputkeyid3;
                        sdiList = new SDIList();
                        String inputkeyid1 = testMode ? io.getProperty("exampletaskqueuekeyid1") : inputs.getProperty(io.getProperty("ioid") + ".keyid1", inputs.getProperty(io.getProperty("ioid")));
                        String inputkeyid2 = testMode ? io.getProperty("exampletaskqueuekeyid2") : inputs.getProperty(io.getProperty("ioid") + ".keyid2");
                        String string = inputkeyid3 = testMode ? io.getProperty("exampletaskqueuekeyid3") : inputs.getProperty(io.getProperty("ioid") + ".keyid3");
                        if (inputkeyid1.length() > 0) {
                            String connectortypeid = io.getProperty("connectortypeid");
                            dbu.createPreparedResultSet("SELECT connectortypesdcid FROM connectortype WHERE connectortypeid = ?", connectortypeid);
                            if (dbu.getNext()) {
                                sdiList.setSdcid(dbu.getValue("connectortypesdcid"));
                                sdiList.addSDIList(inputkeyid1, inputkeyid2, inputkeyid3);
                            }
                        }
                    } else {
                        int selectnum = -1;
                        try {
                            selectnum = Integer.parseInt(io.getProperty("selectnum"));
                        }
                        catch (Exception inputkeyid2) {
                            // empty catch block
                        }
                        sdiList = WorkflowManager.getTaskQueueItems(this.sapphireConnection, dbu, taskdef.getTaskdefid(), taskdef.getTaskdefversionid(), taskdef.getTaskdefvariantid(), ioid, workflowexecid, workflowdefid, workflowdefversionid, workflowdefvariantid, taskdefitemid, commandRequest.getString("taskgroupexec"), selectnum, true, true, false, "W;S");
                    }
                    TaskDef.setTaskVariable(taskVariables, variableid, sdiList, this.connectionInfo.getSysuserId(), "autoselectinput");
                    commandResponse.set("deferredqueueallocationitems", sdiList);
                }
            }
            commandResponse.set(taskdefid, taskdef.getTaskdef());
            PropertyListCollection setupVariables = taskdef.getSetupVariables();
            if (setupVariables.size() > 0) {
                String promptstyle = inputs.getProperty("promptstyle", "M");
                StringBuffer editorstyleid = new StringBuffer();
                PropertyListCollection promptVariables = new PropertyListCollection();
                for (int i = 0; i < setupVariables.size(); ++i) {
                    String stringValue;
                    PropertyList setupVariable = setupVariables.getPropertyList(i);
                    boolean stringVar = setupVariable.getProperty("type", "string").equalsIgnoreCase("string");
                    JSONable value = TaskDef.getTaskVariable(taskVariables, setupVariable.getProperty("variableid"));
                    String string = stringValue = stringVar && value != null ? ((JSONableString)value).toString() : "";
                    if (stringVar && stringValue != null && stringValue.length() > 0) {
                        setupVariable.setProperty("defaultvalue", stringValue);
                    }
                    if (!testMode && !promptstyle.equals("A") && (!stringVar || !setupVariable.getProperty("mandatory").equals("Y") || value != null && stringValue.length() != 0)) continue;
                    promptVariables.add(setupVariable);
                    editorstyleid.append(";").append(setupVariable.getProperty("editorstyleid"));
                }
                if (editorstyleid.length() > 0) {
                    commandResponse.set("prompteditorstyles", this.getEditorStyles(editorstyleid.substring(1)));
                }
                commandResponse.set("promptvariables", promptVariables);
            }
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to load task. Reason: " + e.getMessage(), e);
        }
    }

    private void loadTaskExec(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) {
        block19: {
            String taskexecid = commandRequest.getString("taskexecid");
            boolean updatestatus = commandRequest.getBoolean("updatestatus");
            try {
                String jsonLog;
                String taskdefid = null;
                String taskdefversionid = null;
                String taskdefvariantid = null;
                String workflowdefid = null;
                String workflowdefversionid = null;
                String workflowdefvariantid = null;
                String workflowexecid = null;
                String workflowexecname = null;
                String taskdefitemid = null;
                PropertyList taskexec = null;
                dbu.createPreparedResultSet("SELECT taskexec.taskexecid, taskexec.taskexecdesc, taskexec.execstatus, taskexec.startdt, taskexec.completedt, taskexec.createby, taskexec.createdt, taskexec.modby, taskexec.moddt, taskexec.taskdefid,        taskexec.taskdefversionid, taskexec.taskdefvariantid, taskexec.taskexec, taskexec.workflowexecid, taskexec.taskdefitemid, taskexec.summary, workflowexec.workflowdefid, workflowexec.workflowdefversionid, workflowexec.workflowdefvariantid, workflowexec.workflowexecname FROM   taskexec LEFT OUTER JOIN workflowexec ON taskexec.workflowexecid = workflowexec.workflowexecid WHERE  taskexecid = ?", new Object[]{taskexecid});
                DataSet taskexecPrimary = new DataSet();
                taskexecPrimary.setResultSet(dbu.getResultSet(), true, this.sapphireConnection.getDbms());
                if (taskexecPrimary.size() > 0) {
                    taskdefid = taskexecPrimary.getValue(0, "taskdefid");
                    taskdefversionid = taskexecPrimary.getValue(0, "taskdefversionid");
                    taskdefvariantid = taskexecPrimary.getValue(0, "taskdefvariantid");
                    workflowexecid = taskexecPrimary.getValue(0, "workflowexecid");
                    workflowexecname = taskexecPrimary.getValue(0, "workflowexecname");
                    taskdefitemid = taskexecPrimary.getValue(0, "taskdefitemid");
                    workflowdefid = taskexecPrimary.getValue(0, "workflowdefid");
                    workflowdefversionid = taskexecPrimary.getValue(0, "workflowdefversionid");
                    workflowdefvariantid = taskexecPrimary.getValue(0, "workflowdefvariantid");
                    taskexec = new PropertyList();
                    taskexec.setPropertyList(taskexecPrimary.getClob(0, "taskexec"));
                    if (workflowexecid.length() == 0) {
                        dbu.createPreparedResultSet("SELECT distinct taskexecitem.workflowexecid, taskexecitem.taskdefitemid, workflowexec.workflowdefid, workflowexec.workflowdefversionid, workflowexec.workflowdefvariantid, workflowexec.workflowexecname FROM   taskexecitem, workflowexec WHERE  taskexecitem.workflowexecid = workflowexec.workflowexecid AND taskexecid = ?", new Object[]{taskexecid});
                        StringBuffer wfid = new StringBuffer();
                        StringBuffer wfvid = new StringBuffer();
                        StringBuffer vid = new StringBuffer();
                        StringBuffer wfexecid = new StringBuffer();
                        StringBuffer wfexecname = new StringBuffer();
                        StringBuffer tditemid = new StringBuffer();
                        while (dbu.getNext()) {
                            wfid.append(";").append(dbu.getValue("workflowdefid"));
                            wfvid.append(";").append(dbu.getValue("workflowdefversionid"));
                            vid.append(";").append(dbu.getValue("workflowdefvariantid"));
                            wfexecid.append(";").append(dbu.getValue("workflowexecid"));
                            wfexecname.append(";").append(dbu.getValue("workflowexecname"));
                            tditemid.append(";").append(dbu.getValue("taskdefitemid"));
                        }
                        if (wfexecid.length() > 0) {
                            workflowdefid = wfid.substring(1);
                            workflowdefversionid = wfvid.substring(1);
                            workflowdefvariantid = vid.substring(1);
                            workflowexecid = wfexecid.substring(1);
                            workflowexecname = wfexecname.substring(1);
                            taskdefitemid = tditemid.substring(1);
                        }
                    }
                } else {
                    commandResponse.setStatusFail("Failed to load task execution. Reason: taskexecid '" + taskexecid + "' not found!");
                }
                TaskDef taskdef = null;
                if (workflowexecid == null || workflowexecid.length() <= 0) break block19;
                commandResponse.set("taskexecprimary", taskexecPrimary);
                taskdef = TaskDef.getInstance(this.sapphireConnection, taskdefid, taskdefversionid, taskdefvariantid, true);
                commandResponse.set("taskdefprimary", taskdef.getTaskdefPrimary());
                commandResponse.set("taskexecid", taskexecid);
                commandResponse.set("taskdefid", taskdef.getTaskdefid());
                commandResponse.set("taskdefversionid", taskdef.getTaskdefversionid());
                commandResponse.set("taskdefvariantid", taskdef.getTaskdefvariantid());
                commandResponse.set("workflowdefid", workflowdefid);
                commandResponse.set("workflowdefversionid", workflowdefversionid);
                commandResponse.set("workflowdefvariantid", workflowdefvariantid);
                commandResponse.set("workflowexecid", workflowexecid);
                commandResponse.set("workflowexecname", workflowexecname);
                commandResponse.set("taskdefitemid", taskdefitemid);
                JSONableMap taskVariables = new JSONableMap();
                commandResponse.set("taskvariables", taskVariables);
                PropertyListCollection variables = taskexec.getCollection("variables");
                if (variables != null) {
                    for (int i = 0; i < variables.size(); ++i) {
                        PropertyList variable = variables.getPropertyList(i);
                        String type = variable.getProperty("type", "string");
                        JSONable value = null;
                        try {
                            if (type.equalsIgnoreCase("sdilist")) {
                                value = new SDIList();
                                value.setJSONObject(new JSONObject(variable.getProperty("value")));
                            } else {
                                value = type.equalsIgnoreCase("form") ? new FormValue(variable.getProperty("value")) : new JSONableString(variable.getProperty("value"));
                            }
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        TaskDef.setTaskVariable(taskVariables, variable.getProperty("variableid"), value, variable.getProperty("sysuserid"), variable.getProperty("timestamp"), variable.getProperty("source"));
                    }
                }
                if ((jsonLog = taskexec.getProperty("log")).length() > 0) {
                    DataSet log = new DataSet(new JSONObject(jsonLog));
                    Calendar c = Calendar.getInstance();
                    M18NUtil m18n = new M18NUtil();
                    for (int i = 0; i < log.size(); ++i) {
                        try {
                            c.setTimeInMillis(Long.parseLong(log.getValue(i, "logtime")));
                            log.setString(i, "logdt", m18n.getDefaultDateFormat().format(c.getTime()) + " (" + c.get(13) + "." + c.get(14) + "s)");
                        }
                        catch (NumberFormatException numberFormatException) {
                            // empty catch block
                        }
                        if (!log.getValue(i, "typeflag").equals("T")) continue;
                        commandResponse.set("tasksummary", log.getValue(i, "logentry"));
                    }
                    commandResponse.set("tasklog", log.toJSONString());
                } else {
                    commandResponse.set("tasklog", new DataSet().toJSONString());
                }
                commandResponse.set("history", taskexec.getProperty("history"));
                commandResponse.set("stepid", taskexec.getProperty("stepid"));
                dbu.createPreparedResultSet("SELECT taskexecitem.itemsdcid, taskexecitem.itemkeyid1, taskexecitem.itemkeyid2, taskexecitem.itemkeyid3, taskexecitem.fromtaskexecid, taskexecitem.totaskexecid, taskexecitem.typeflag,        taskexecitem.ioid, taskexecitem.workflowexecid, taskexecitem.usersequence FROM   taskexecitem WHERE  taskexecid = ? ORDER BY taskexecitem.typeflag, taskexecitem.usersequence", new Object[]{taskexecid});
                DataSet taskexecitems = new DataSet();
                taskexecitems.setResultSet(dbu.getResultSet());
                commandResponse.set("taskexecitems", taskexecitems);
                commandResponse.set("notescount", String.valueOf(dbu.getPreparedCount("SELECT count(*) FROM sdinote WHERE sdcid = 'LV_TaskExec' AND keyid1 = ?", new Object[]{taskexecid})));
                commandResponse.set(taskdefid, taskdef.getTaskdef());
                if (updatestatus) {
                    PropertyList editProps = new PropertyList();
                    editProps.setProperty("sdcid", "LV_TaskExec");
                    editProps.setProperty("keyid1", taskexecid);
                    editProps.setProperty("execstatus", "A");
                    editProps.setProperty("assignedanalyst", this.sapphireConnection.getSysuserId());
                    editProps.setProperty("assigneddepartment", "");
                    editProps.setProperty("connectionid", this.sapphireConnection.getConnectionId());
                    this.getActionProcessor().processAction("EditSDI", "1", editProps);
                }
            }
            catch (Exception e) {
                commandResponse.setStatusFail("Failed to load task execution. Reason: " + e.getMessage(), e);
            }
        }
    }

    private void loadWorkflowCounts(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) throws SapphireException {
        String workflowexecid = commandRequest.getString("workflowexecid");
        String workflowdefid = commandRequest.getString("workflowdefid");
        String workflowdefversionid = commandRequest.getString("workflowdefversionid", "1");
        String workflowdefvariantid = commandRequest.getString("workflowdefvariantid");
        boolean applyUserRestrictions = commandRequest.getBoolean("applyuserrestrictions");
        if (workflowdefid.length() > 0 || workflowexecid.length() > 0) {
            if (workflowdefid.length() == 0) {
                dbu.createPreparedResultSet("SELECT workflowdefid, workflowdefversionid, workflowdefvariantid FROM   workflowexec WHERE  workflowexecid = ?", new Object[]{workflowexecid});
                if (dbu.getNext()) {
                    workflowdefid = dbu.getValue("workflowdefid");
                    workflowdefversionid = dbu.getValue("workflowdefversionid");
                    workflowdefvariantid = dbu.getValue("workflowdefvariantid");
                } else {
                    commandResponse.setStatusFail("Failed to load workflow. Reason: workflowexecid '" + workflowexecid + "' not found!");
                }
            }
            if (workflowdefid.length() > 0) {
                try {
                    dbu.createPreparedResultSet("SELECT DISTINCT taskexec.taskexecid, taskexecitem.taskdefitemid FROM   taskexec, taskexecitem, workflowexec WHERE  taskexec.taskexecid = taskexecitem.taskexecid  AND   workflowexec.workflowdefid = ? AND workflowexec.workflowdefversionid = ? AND workflowexec.workflowdefvariantid = ?  AND   taskexec.execstatus IN ( 'P', 'I' )  AND   workflowexec.workflowexecid = taskexecitem.workflowexecid " + (applyUserRestrictions ? " AND " + WorkflowManager.getUserAssignmentClause("taskexec", this.sapphireConnection.getSysuserId(), "assignedanalyst", "assigneddepartment", "assignedrole") : "") + (workflowexecid.length() > 0 ? " AND workflowexec.workflowexecid IN ('" + StringUtil.replaceAll(workflowexecid, ";", "','") + "') " : "") + "UNION SELECT DISTINCT taskexec.taskexecid, taskexec.taskdefitemid FROM   taskexec, workflowexec WHERE workflowexec.workflowdefid = ? AND workflowexec.workflowdefversionid = ? AND workflowexec.workflowdefvariantid = ? AND taskexec.execstatus IN ( '" + "P" + "', '" + "I" + "' ) AND workflowexec.workflowexecid = taskexec.workflowexecid " + (applyUserRestrictions ? " AND " + WorkflowManager.getUserAssignmentClause("taskexec", this.sapphireConnection.getSysuserId(), "assignedanalyst", "assigneddepartment", "assignedrole") : "") + (workflowexecid.length() > 0 ? " AND workflowexec.workflowexecid IN ('" + StringUtil.replaceAll(workflowexecid, ";", "','") + "') " : "") + "ORDER BY 2", new Object[]{workflowdefid, workflowdefversionid, workflowdefvariantid, workflowdefid, workflowdefversionid, workflowdefvariantid});
                    HashMap<String, Integer> taskexeccounts = new HashMap<String, Integer>();
                    while (dbu.getNext()) {
                        String taskdefitemid = dbu.getString("taskdefitemid");
                        if (taskexeccounts.containsKey(taskdefitemid)) {
                            taskexeccounts.put(taskdefitemid, (Integer)taskexeccounts.get(taskdefitemid) + 1);
                            continue;
                        }
                        taskexeccounts.put(taskdefitemid, 1);
                    }
                    JSONableMap taskexecdata = new JSONableMap(taskexeccounts);
                    commandResponse.set("taskexecdata", taskexecdata);
                    JSONableMap taskqueuecounts = new JSONableMap();
                    JSONableMap taskwaitcounts = new JSONableMap();
                    JSONableMap taskautocounts = new JSONableMap();
                    JSONableMap taskactivecounts = new JSONableMap();
                    JSONableMap tasksingluar = new JSONableMap();
                    JSONableMap taskplural = new JSONableMap();
                    dbu.createPreparedResultSet("SELECT taskdefitemid, queuesdcid, queuekeyid1, queuekeyid2, queuekeyid3, singular, plural, queuestatus FROM   taskqueue, sdc WHERE  queuesdcid = sdcid and workflowdefid = ? AND workflowdefversionid = ? AND workflowdefvariantid = ? " + (applyUserRestrictions ? " AND " + WorkflowManager.getUserAssignmentClause("taskqueue", this.sapphireConnection.getSysuserId(), "assignedanalyst", "assigneddepartment", "assignedrole") : "") + (workflowexecid.length() > 0 ? "AND workflowexecid IN ('" + StringUtil.replaceAll(workflowexecid, ";", "','") + "') " : "") + "ORDER BY taskdefitemid", new Object[]{workflowdefid, workflowdefversionid, workflowdefvariantid});
                    HashMap<String, SDIList> sdiListMap = new HashMap<String, SDIList>();
                    while (dbu.getNext()) {
                        int index;
                        String sdcid = dbu.getValue("queuesdcid");
                        SDIList sdiList = (SDIList)sdiListMap.get(sdcid);
                        if (sdiList == null) {
                            sdiList = new SDIList();
                            sdiList.setAllowDups(true);
                            sdiList.setSdcid(sdcid);
                            sdiListMap.put(sdcid, sdiList);
                        }
                        if ((index = sdiList.addSDI(dbu.getValue("queuekeyid1"), dbu.getValue("queuekeyid2"), dbu.getValue("queuekeyid3"))) < 0) continue;
                        sdiList.setSDIAttribute(index, "taskdefitemid", dbu.getValue("taskdefitemid"));
                        sdiList.setSDIAttribute(index, "singular", dbu.getValue("singular"));
                        sdiList.setSDIAttribute(index, "plural", dbu.getValue("plural"));
                        sdiList.setSDIAttribute(index, "queuestatus", dbu.getValue("queuestatus"));
                    }
                    SDIProcessor sdiProcessor = this.getSDIProcessor();
                    HashMap<String, String> keyMap = new HashMap<String, String>();
                    for (String sdcid : sdiListMap.keySet()) {
                        SDIList sdiList = (SDIList)sdiListMap.get(sdcid);
                        SDIData sdiData = WorkflowManager.getSDIData(sdiList, sdiProcessor);
                        if (sdiData == null || sdiData.getDataset("primary") == null) continue;
                        DataSet primary = sdiData.getDataset("primary");
                        String[] keys = sdiData.getKeys("primary");
                        for (int i = 0; i < sdiList.size(); ++i) {
                            int count;
                            int findRow;
                            keyMap.clear();
                            keyMap.put(keys[0], sdiList.getKeyid1(i));
                            if (keys.length >= 2 && keys[1].length() > 0) {
                                keyMap.put(keys[1], sdiList.getKeyid2(i));
                            }
                            if (keys.length >= 3 && keys[2].length() > 0) {
                                keyMap.put(keys[2], sdiList.getKeyid3(i));
                            }
                            if ((findRow = primary.findRow(keyMap)) <= -1) continue;
                            String taskdefitemid = sdiList.getSDIAttribute(i, "taskdefitemid");
                            if (sdiList.getSDIAttribute(i, "queuestatus").equals("W") || sdiList.getSDIAttribute(i, "queuestatus").equals("S")) {
                                count = taskqueuecounts.containsKey(taskdefitemid) ? Integer.parseInt(taskqueuecounts.getString(taskdefitemid)) : 0;
                                taskqueuecounts.put(taskdefitemid, String.valueOf(++count));
                            } else if (sdiList.getSDIAttribute(i, "queuestatus").equals("Wevent") || sdiList.getSDIAttribute(i, "queuestatus").equals("Aevent") || sdiList.getSDIAttribute(i, "queuestatus").equals("Wtimer") || sdiList.getSDIAttribute(i, "queuestatus").equals("Atimer")) {
                                count = taskwaitcounts.containsKey(taskdefitemid) ? Integer.parseInt(taskwaitcounts.getString(taskdefitemid)) : 0;
                                taskwaitcounts.put(taskdefitemid, String.valueOf(++count));
                            } else if (sdiList.getSDIAttribute(i, "queuestatus").equals("A")) {
                                count = taskautocounts.containsKey(taskdefitemid) ? Integer.parseInt(taskautocounts.getString(taskdefitemid)) : 0;
                                taskautocounts.put(taskdefitemid, String.valueOf(++count));
                            } else if (sdiList.getSDIAttribute(i, "queuestatus").equals("E")) {
                                count = taskactivecounts.containsKey(taskdefitemid) ? Integer.parseInt(taskactivecounts.getString(taskdefitemid)) : 0;
                                taskactivecounts.put(taskdefitemid, String.valueOf(++count));
                            }
                            tasksingluar.put(taskdefitemid, sdiList.getSDIAttribute(i, "singular").toLowerCase());
                            taskplural.put(taskdefitemid, taskplural.containsKey(taskdefitemid) ? "items" : sdiList.getSDIAttribute(i, "plural").toLowerCase());
                        }
                    }
                    JSONableMap taskqueuedata = new JSONableMap();
                    taskqueuedata.put("counts", taskqueuecounts);
                    taskqueuedata.put("waitcounts", taskwaitcounts);
                    taskqueuedata.put("autocounts", taskautocounts);
                    taskqueuedata.put("activecounts", taskactivecounts);
                    taskqueuedata.put("singulars", tasksingluar);
                    taskqueuedata.put("plurals", taskplural);
                    commandResponse.set("taskqueuedata", taskqueuedata);
                }
                catch (Exception e) {
                    commandResponse.setStatusFail("Failed to load workflow counts. Reason: " + e.getMessage(), e);
                }
            }
        } else {
            commandResponse.setStatusFail("Workflowdefid or workflowexecid missing from request!");
        }
    }

    private void loadTaskPreview(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) throws SapphireException {
        String taskdefid = commandRequest.getString("taskdefid");
        String taskdefversionid = commandRequest.getString("taskdefversionid", "1");
        String taskdefvariantid = commandRequest.getString("taskdefvariantid", "1");
        String workflowexecid = commandRequest.getString("workflowexecid");
        String workflowdefid = commandRequest.getString("workflowdefid");
        String workflowdefversionid = commandRequest.getString("workflowdefversionid", "1");
        String workflowdefvariantid = commandRequest.getString("workflowdefvariantid");
        String taskdefitemid = commandRequest.getString("taskdefitemid");
        String taskExecGroup = commandRequest.getString("taskexecgroup");
        boolean applyUserRestrictions = commandRequest.getBoolean("applyuserrestrictions");
        TaskDef taskdef = null;
        if (taskdefid.length() > 0) {
            taskdef = TaskDef.getInstance(this.sapphireConnection, taskdefid, taskdefversionid, taskdefvariantid, true);
        } else if (taskdefitemid.length() > 0) {
            taskdef = TaskDef.getInstance(this.sapphireConnection, workflowdefid, workflowdefversionid, workflowdefvariantid, taskdefitemid, true);
        }
        if (taskdef != null) {
            taskdefid = taskdef.getTaskdefid();
            taskdefversionid = taskdef.getTaskdefversionid();
            taskdefvariantid = taskdef.getTaskdefvariantid();
            commandResponse.set("taskdefid", taskdefid);
            commandResponse.set("taskdefversionid", taskdefversionid);
            commandResponse.set("taskdefvariantid", taskdefvariantid);
            commandResponse.set(taskdefid, taskdef.getTaskdef());
            try {
                SDCProcessor sdcProcessor = this.getSDCProcessor();
                PropertyListCollection taskio = taskdef.getTaskdef().getCollection("taskio");
                int inputs = 0;
                if (taskio != null) {
                    for (int i = 0; i < taskio.size(); ++i) {
                        PropertyList io = taskio.getPropertyList(i);
                        if (!io.getProperty("ioflag").equals("I")) continue;
                        ++inputs;
                        SDIList sdiList = WorkflowManager.getTaskQueueItems(this.sapphireConnection, dbu, taskdefid, taskdefversionid, taskdefvariantid, io.getProperty("ioid"), workflowexecid, workflowdefid, workflowdefversionid, workflowdefvariantid, taskdefitemid, taskExecGroup, -1, applyUserRestrictions, true, false, "W;S");
                        commandResponse.set(io.getProperty("ioid"), sdiList);
                        commandResponse.set(io.getProperty("ioid") + "_singular", StringUtil.initCaps(sdcProcessor.getProperty(sdiList.getSdcid(), "singular")));
                        commandResponse.set(io.getProperty("ioid") + "_plural", StringUtil.initCaps(sdcProcessor.getProperty(sdiList.getSdcid(), "plural")));
                        if (io.getProperty("lookuppageid").length() == 0) {
                            io.setProperty("lookuppageid", sdiList.getSdcid() + "Lookup");
                        }
                        SDIList waitList = WorkflowManager.getTaskQueueItems(this.sapphireConnection, dbu, taskdefid, taskdefversionid, taskdefvariantid, io.getProperty("ioid"), workflowexecid, workflowdefid, workflowdefversionid, workflowdefvariantid, taskdefitemid, taskExecGroup, -1, applyUserRestrictions, false, true, "Wevent;Aevent;Wtimer;Atimer");
                        commandResponse.set(io.getProperty("ioid") + "_waiting", waitList);
                        SDIList activeList = WorkflowManager.getTaskQueueItems(this.sapphireConnection, dbu, taskdefid, taskdefversionid, taskdefvariantid, io.getProperty("ioid"), workflowexecid, workflowdefid, workflowdefversionid, workflowdefvariantid, taskdefitemid, taskExecGroup, -1, applyUserRestrictions, false, false, "E;A");
                        commandResponse.set(io.getProperty("ioid") + "_active", activeList);
                        commandResponse.set(io.getProperty("ioid") + "_notes", this.getNotesColumn(sdcProcessor, sdiList.getSdcid(), true, ""));
                    }
                }
                commandResponse.set("multipleinputs", inputs > 1 ? "Y" : "N");
                commandResponse.set("attachmentscount", String.valueOf(dbu.getPreparedCount("SELECT count(*) FROM sdiattachment WHERE sdcid = 'LV_TaskDef' AND keyid1 = ? AND keyid2 = ? AND keyid3 = ?", new Object[]{taskdefid, taskdefversionid, taskdefvariantid})));
            }
            catch (Exception e) {
                commandResponse.setStatusFail("Failed to load queue items. Reason: " + e.getMessage(), e);
            }
            try {
                if (commandRequest.getBoolean("showpausedtasks")) {
                    SDIList pausedTasks = new SDIList();
                    pausedTasks.setSdcid("LV_TaskExec");
                    dbu.createPreparedResultSet("SELECT DISTINCT taskexec.taskexecid FROM   taskexec, taskexecitem, workflowexec WHERE  taskexec.taskexecid = taskexecitem.taskexecid  AND   workflowexec.workflowdefid = ? AND workflowexec.workflowdefversionid = ? AND workflowexec.workflowdefvariantid = ?  AND   taskexec.execstatus IN ( 'P', 'I' )  AND   workflowexec.workflowexecid = taskexecitem.workflowexecid " + (applyUserRestrictions ? " AND " + WorkflowManager.getUserAssignmentClause("taskexec", this.sapphireConnection.getSysuserId(), "assignedanalyst", "assigneddepartment", "assignedrole") : "") + (workflowexecid.length() > 0 ? "AND workflowexec.workflowexecid IN ('" + StringUtil.replaceAll(workflowexecid, ";", "','") + "') " : "") + " AND taskexecitem.taskdefitemid = ? UNION SELECT DISTINCT taskexec.taskexecid FROM   taskexec, workflowexec WHERE  workflowexec.workflowdefid = ? AND workflowexec.workflowdefversionid = ? AND workflowexec.workflowdefvariantid = ?  AND   taskexec.execstatus IN ( '" + "P" + "', '" + "I" + "' )  AND   workflowexec.workflowexecid = taskexec.workflowexecid " + (applyUserRestrictions ? " AND " + WorkflowManager.getUserAssignmentClause("taskexec", this.sapphireConnection.getSysuserId(), "assignedanalyst", "assigneddepartment", "assignedrole") : "") + (workflowexecid.length() > 0 ? "AND workflowexec.workflowexecid IN ('" + StringUtil.replaceAll(workflowexecid, ";", "','") + "') " : "") + " AND   taskexec.taskdefitemid = ?", new Object[]{workflowdefid, workflowdefversionid, workflowdefvariantid, taskdefitemid, workflowdefid, workflowdefversionid, workflowdefvariantid, taskdefitemid});
                    while (dbu.getNext()) {
                        pausedTasks.addSDI(dbu.getValue("taskexecid"));
                    }
                    commandResponse.set("pausedtasks", pausedTasks);
                }
            }
            catch (Exception e) {
                commandResponse.setStatusFail("Failed to load paused tasks. Reason: " + e.getMessage(), e);
            }
            try {
                if (commandRequest.getBoolean("showpausedtasks")) {
                    SDIList activeTasks = new SDIList();
                    activeTasks.setSdcid("LV_TaskExec");
                    dbu.createPreparedResultSet("SELECT DISTINCT taskexec.taskexecid FROM   taskexec, taskexecitem, workflowexec WHERE  taskexec.taskexecid = taskexecitem.taskexecid  AND   workflowexec.workflowdefid = ? AND workflowexec.workflowdefversionid = ? AND workflowexec.workflowdefvariantid = ?  AND   taskexec.execstatus = ?  AND   workflowexec.workflowexecid = taskexecitem.workflowexecid " + (applyUserRestrictions ? " AND " + WorkflowManager.getUserAssignmentClause("taskexec", this.sapphireConnection.getSysuserId(), "assignedanalyst", "assigneddepartment", "assignedrole") : "") + (workflowexecid.length() > 0 ? "AND workflowexec.workflowexecid IN ('" + StringUtil.replaceAll(workflowexecid, ";", "','") + "') " : "") + " AND taskexecitem.taskdefitemid = ? UNION SELECT DISTINCT taskexec.taskexecid FROM   taskexec, workflowexec WHERE  workflowexec.workflowdefid = ? AND workflowexec.workflowdefversionid = ? AND workflowexec.workflowdefvariantid = ?  AND   taskexec.execstatus = ?  AND   workflowexec.workflowexecid = taskexec.workflowexecid " + (applyUserRestrictions ? " AND " + WorkflowManager.getUserAssignmentClause("taskexec", this.sapphireConnection.getSysuserId(), "assignedanalyst", "assigneddepartment", "assignedrole") : "") + (workflowexecid.length() > 0 ? "AND workflowexec.workflowexecid IN ('" + StringUtil.replaceAll(workflowexecid, ";", "','") + "') " : "") + " AND   taskexec.taskdefitemid = ?", new Object[]{workflowdefid, workflowdefversionid, workflowdefvariantid, "A", taskdefitemid, workflowdefid, workflowdefversionid, workflowdefvariantid, "A", taskdefitemid});
                    while (dbu.getNext()) {
                        activeTasks.addSDI(dbu.getValue("taskexecid"));
                    }
                    commandResponse.set("activetasks", activeTasks);
                }
            }
            catch (Exception e) {
                commandResponse.setStatusFail("Failed to load active tasks. Reason: " + e.getMessage(), e);
            }
            try {
                if (commandRequest.getBoolean("loadheaderdetails")) {
                    PropertyList search = new PropertyList();
                    search.setProperty("searchid", "queueitems");
                    search.setProperty("linkid", "queueitems");
                    search.setProperty("type", "TaskQueueItem");
                    search.setProperty("queryfrom", "taskqueue");
                    search.setProperty("querywhere", "taskqueue.taskdefitemid='" + taskdefitemid + "' AND taskqueue.workflowexecid='" + workflowexecid + "'");
                    commandRequest.set("search", search);
                    commandRequest.set("postprocessing", "T");
                    this.search(commandRequest, commandResponse, dbu);
                    DataSet searchResults = (DataSet)commandResponse.get("searchresults");
                    if (searchResults == null || searchResults.size() == 0) {
                        String starttask = "N";
                        dbu.createPreparedResultSet("SELECT starttaskflag FROM   workflowdeftask WHERE  workflowdeftask.workflowdefid = ? AND workflowdeftask.workflowdefversionid = ? AND workflowdeftask.workflowdefvariantid = ?  AND   workflowdeftask.taskdefid = ? AND workflowdeftask.taskdefversionid = ? AND workflowdeftask.taskdefvariantid = ? AND workflowdeftask.taskdefitemid = ?", new Object[]{workflowdefid, workflowdefversionid, workflowdefvariantid, taskdefid, taskdefversionid, taskdefvariantid, taskdefitemid});
                        if (dbu.getNext()) {
                            starttask = dbu.getValue("starttaskflag");
                        }
                        String execstatus = "A";
                        dbu.createPreparedResultSet("SELECT execstatus FROM   workflowexec WHERE  workflowexecid = ?", new Object[]{workflowexecid});
                        if (dbu.getNext()) {
                            execstatus = dbu.getValue("execstatus");
                        }
                        DataSet taskdefPrimary = taskdef.getTaskdefPrimary();
                        searchResults = new DataSet("taskdefid;taskdefversionid;taskdefvariantid;taskdefdesc;icon;shorttitle;longtitle;scopeflag;workflowexecid;ioid;itemsdcid;duedt;createdt;taskqueueid;workflowdefid;workflowdefversionid;workflowdefvariantid;taskdefitemid;taskexecgroup;assignedanalyst;starttaskflag;taskexecid;taskexecdesc;startdt;completedt;execcreateby;execmodby;execmoddt;wfeexecstatus;summary;execstatus_text;notes;count;type", taskdefid + "|" + taskdefversionid + "|" + taskdefvariantid + "|" + taskdefPrimary.getValue(0, "taskdefdesc") + "|" + taskdefPrimary.getValue(0, "icon") + "|" + taskdefPrimary.getValue(0, "shorttitle") + "|" + taskdefPrimary.getValue(0, "longtitle") + "|" + taskdefPrimary.getValue(0, "scopeflag") + "|" + workflowexecid + "|ioid|sdcid|duedt|createdt|taskqueueid|" + workflowdefid + "|" + workflowdefversionid + "|" + workflowdefvariantid + "|" + taskdefitemid + "|" + taskExecGroup + "|assignedanalyst|" + starttask + "|taskexecid|taskexecdesc|startdt|completedt|execcreateby|execmodby|execmoddt|" + execstatus + "|summary|execstatus_text|notes|0|TD");
                        commandResponse.set("searchresults", searchResults);
                    }
                }
            }
            catch (Exception e) {
                commandResponse.setStatusFail("Failed to load paused tasks. Reason: " + e.getMessage(), e);
            }
        } else {
            commandResponse.setStatusFail("Taskdefid or taskdefitemid missing from request!");
        }
    }

    private PropertyList getNotesColumn(SDCProcessor sdcProcessor, String sdcid, boolean popupNotes, String taskexecid) {
        if (sdcid != null && sdcid.length() > 0) {
            int keycols = Integer.parseInt(sdcProcessor.getProperty(sdcid, "keycolumns"));
            String keycolid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
            String keycolid2 = sdcProcessor.getProperty(sdcid, "keycolid2");
            String keycolid3 = sdcProcessor.getProperty(sdcid, "keycolid3");
            PropertyList notes = new PropertyList();
            notes.setProperty("id", "notes");
            notes.setProperty("title", this.getTranslationProcessor().translate("Notes"));
            notes.setProperty("columnid", "(SELECT CASE COUNT(*) WHEN 0 THEN 0 ELSE 1 END FROM sdinote WHERE sdcid='" + sdcid + "' AND keyid1 = " + keycolid1 + (keycols >= 2 ? " AND keyid2 = " + keycolid2 : "") + (keycols == 3 ? " AND keyid3 = " + keycolid3 : "") + ") Notes");
            notes.setProperty("noheader", "Y");
            notes.setProperty("width", "30px");
            PropertyList noteslink = new PropertyList();
            if (popupNotes) {
                notes.setProperty("displayvalue", "1=<img src=\"WEB-CORE/imageref/finance_business_and_trade/office/notes/16/note.png\" border=\"0\" style=\"cursor:pointer\">;0=<img src=\"WEB-CORE/imageref/finance_business_and_trade/office/notes/16/note_add_ghosted.png\" border=\"0\" style=\"cursor:pointer\">");
                noteslink.setProperty("href", "javascript:top.showNotesPopup( '" + StringUtil.initCaps(sdcProcessor.getProperty(sdcid, "singular")) + " Notes', '" + sdcid + "', '[" + keycolid1 + "]', " + (keycols >= 2 ? "'[" + keycolid2 + "]'" : "''") + ", " + (keycols == 3 ? "'[" + keycolid3 + "]'" : "''") + (taskexecid.length() > 0 ? ", 'LV_TaskExec', '" + taskexecid + "', '', ''" : ", '', '', '', ''") + " )");
            } else {
                notes.setProperty("displayvalue", "1=<img src=\"WEB-CORE/imageref/finance_business_and_trade/office/notes/16/note.png\" border=\"0\" id=\"sdinote_[rowid]\" style=\"cursor:pointer\">;0=<img src=\"WEB-CORE/imageref/finance_business_and_trade/office/notes/16/note.png\" border=\"0\" id=\"sdinote_[rowid]\" style=\"display:none\">");
                noteslink.setProperty("href", "javascript:listview_openSDINotes( '" + sdcid + "', '[" + keycolid1 + "]', " + (keycols >= 2 ? "'[" + keycolid2 + "]'" : "''") + ", " + (keycols == 3 ? "'[" + keycolid3 + "]'" : "''") + " )");
            }
            notes.setProperty("link", noteslink);
            return notes;
        }
        return new PropertyList();
    }

    private void loadTaskQueueItems(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) {
        try {
            String taskdefid = commandRequest.getString("taskdefid");
            String taskdefversionid = commandRequest.getString("taskdefversionid", "1");
            String taskdefvariantid = commandRequest.getString("taskdefvariantid", "1");
            String workflowexecid = commandRequest.getString("workflowexecid");
            String workflowdefid = commandRequest.getString("workflowdefid");
            String workflowdefversionid = commandRequest.getString("workflowdefversionid", "1");
            String workflowdefvariantid = commandRequest.getString("workflowdefvariantid");
            String taskdefitemid = commandRequest.getString("taskdefitemid");
            String ioid = commandRequest.getString("ioid");
            String taskExecGroup = commandRequest.getString("taskexecgroup");
            String taskexecid = commandRequest.getString("taskexecid");
            boolean applyUserRestrictions = commandRequest.getBoolean("applyuserrestrictions");
            boolean popupNotes = commandRequest.getBoolean("popupnotes");
            boolean applySecurity = commandRequest.getBoolean("applysecurity");
            boolean testMode = commandRequest.getBoolean("testmode");
            boolean standaloneMode = commandRequest.getBoolean("standalonemode");
            String queueStatuses = commandRequest.getString("queuestatuses", "W;S");
            SDIList sdiList = null;
            if (testMode) {
                String inputkeyid1;
                sdiList = new SDIList();
                TaskDef taskDef = TaskDef.getInstance(this.sapphireConnection, taskdefid, taskdefversionid, taskdefvariantid);
                PropertyList taskdef = taskDef.getTaskdef();
                PropertyListCollection taskio = taskdef.getCollection("taskio");
                PropertyList io = taskio.find("ioid", ioid);
                if (io != null && (inputkeyid1 = io.getProperty("exampletaskqueuekeyid1")).length() > 0) {
                    String connectortypeid = io.getProperty("connectortypeid");
                    dbu.createPreparedResultSet("SELECT connectortypesdcid FROM connectortype WHERE connectortypeid = ?", connectortypeid);
                    if (dbu.getNext()) {
                        sdiList.setSdcid(dbu.getValue("connectortypesdcid"));
                        sdiList.addSDIList(inputkeyid1, io.getProperty("exampletaskqueuekeyid2"), io.getProperty("exampletaskqueuekeyid3"));
                    }
                }
            } else if (standaloneMode) {
                sdiList = commandRequest.getSDIList("inputvariable");
                if (sdiList == null) {
                    throw new SapphireException("Input queue variable not set");
                }
            } else {
                sdiList = WorkflowManager.getTaskQueueItems(this.sapphireConnection, dbu, taskdefid, taskdefversionid, taskdefvariantid, ioid, workflowexecid, workflowdefid, workflowdefversionid, workflowdefvariantid, taskdefitemid, taskExecGroup, -1, applyUserRestrictions, applySecurity, false, queueStatuses);
            }
            commandResponse.set(ioid, sdiList);
            commandResponse.set(ioid + "_notes", this.getNotesColumn(this.getSDCProcessor(), sdiList.getSdcid(), popupNotes, taskexecid));
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to load queue items. Reason: " + e.getMessage(), e);
        }
    }

    private void loadVariable(CommandRequest commandRequest, CommandResponse commandResponse) {
        PropertyList variableDef = commandRequest.getPropertyList("variabledef");
        String type = variableDef.getProperty("type");
        if (type.equalsIgnoreCase("sdidatastore")) {
            String[] requestitems;
            PropertyList sdiProps = variableDef.getPropertyList(type);
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDCid(sdiProps.getProperty("sdcid"));
            sdiRequest.setKeyid1List(sdiProps.getProperty("keyid1"));
            sdiRequest.setKeyid2List(sdiProps.getProperty("keyid2"));
            sdiRequest.setKeyid3List(sdiProps.getProperty("keyid3"));
            sdiRequest.setQueryFrom(sdiProps.getProperty("queryfrom"));
            sdiRequest.setQueryWhere(sdiProps.getProperty("querywhere"));
            for (String requestitem : requestitems = StringUtil.split(sdiProps.getProperty("requestitems"), ";")) {
                sdiRequest.setRequestItem(requestitem);
            }
            SDIProcessor sdiProcessor = this.getSDIProcessor();
            SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
            if (sdiData != null) {
                commandResponse.set(variableDef.getProperty("variableid"), new SDIMaint(this.getSDCProcessor().getPropertyList(sdiRequest.getSDCid()), sdiData));
            } else {
                commandResponse.setStatusFail("Failed to load SDI datasource. Reason: " + sdiProcessor.getLastException().getMessage(), sdiProcessor.getLastException());
            }
        }
    }

    private void loadWFM(CommandRequest commandRequest, CommandResponse commandResponse) {
        try {
            PropertyListCollection panels;
            PropertyList wfmConfig = new PropertyList();
            RequestService requestService = new RequestService(this.sapphireConnection);
            PropertyList webPageProps = requestService.getWebPageProperties(commandRequest.getString("webpageid"), new PropertyList());
            PropertyList pageData = webPageProps.getPropertyList("pagedata");
            PropertyList sidebar = pageData.getPropertyList("sidebar");
            if (sidebar != null && (panels = sidebar.getCollection("panels")) != null) {
                StringBuffer queryidlist = new StringBuffer();
                StringBuffer basedonidlist = new StringBuffer();
                for (int i = 0; i < panels.size(); ++i) {
                    PropertyListCollection links;
                    PropertyList panel = panels.getPropertyList(i);
                    if (!panel.getProperty("show", "Y").equals("Y") || (links = panel.getCollection("links")) == null) continue;
                    for (int j = 0; j < links.size(); ++j) {
                        PropertyList link = links.getPropertyList(j);
                        String linktype = link.getProperty("type");
                        if (link.getProperty("queryid").length() <= 0) continue;
                        queryidlist.append(";").append(link.getProperty("queryid"));
                        basedonidlist.append(";").append(linktype);
                    }
                }
                if (queryidlist.length() > 0) {
                    commandResponse.set("querydefinitions", this.getQueryDefinitions(queryidlist.substring(1), basedonidlist.substring(1)));
                }
            }
            commandResponse.set("wfmconfig", wfmConfig);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to load WFM properties. Reason: " + e.getMessage(), e);
        }
    }

    private void loadWorkflow(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) throws SapphireException {
        String workflowexecid = commandRequest.getString("workflowexecid");
        String workflowdefid = commandRequest.getString("workflowdefid");
        String workflowdefversionid = commandRequest.getString("workflowdefversionid", "1");
        String workflowdefvariantid = commandRequest.getString("workflowdefvariantid");
        if (workflowdefid.length() > 0 || workflowexecid.length() > 0) {
            if (workflowdefid.length() == 0) {
                dbu.createPreparedResultSet("SELECT workflowdefid, workflowdefversionid, workflowdefvariantid FROM   workflowexec WHERE  workflowexecid = ?", new Object[]{workflowexecid});
                if (dbu.getNext()) {
                    workflowdefid = dbu.getValue("workflowdefid");
                    workflowdefversionid = dbu.getValue("workflowdefversionid");
                    workflowdefvariantid = dbu.getValue("workflowdefvariantid");
                } else {
                    commandResponse.setStatusFail("Failed to load workflow. Reason: workflowexecid '" + workflowexecid + "' not found!");
                }
            }
            if (workflowdefid.length() > 0) {
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setSDIList("LV_WorkflowDef", workflowdefid, workflowdefversionid, workflowdefvariantid);
                sdiRequest.setExtendedDataTypes(true);
                sdiRequest.setRequestItem("primary[workflowdefid, workflowdefversionid, workflowdefvariantid, workflowdefdesc, exectypeflag, execstatus, workflowdef, createdt, createby, moddt, modby, versionstatus, (select count(distinct workflowexecid) from workflowexec where workflowexec.workflowdefid=workflowdef.workflowdefid and workflowexec.workflowdefversionid=workflowdef.workflowdefversionid and workflowexec.workflowdefvariantid=workflowdef.workflowdefvariantid ) count ]");
                sdiRequest.setRequestItem("workflowdeftaskio");
                SDIProcessor sdiProcessor = this.getSDIProcessor();
                SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
                if (sdiData != null) {
                    DataSet workflowdef = sdiData.getDataset("primary");
                    if (workflowdef != null && workflowdef.size() > 0) {
                        PropertyListCollection variables;
                        PropertyList workflowdefXML = new PropertyList();
                        workflowdefXML.setPropertyList(workflowdef.getClob(0, "workflowdef"));
                        workflowdef.setValue(0, "workflowdef", "");
                        commandResponse.set("primary", workflowdef);
                        commandResponse.set("workflowdef", workflowdefXML);
                        if (commandRequest.getBoolean("loadvariableeditorstyles") && (variables = workflowdefXML.getCollection("variables")) != null) {
                            StringBuffer editorstyleid = new StringBuffer();
                            for (int i = 0; i < variables.size(); ++i) {
                                PropertyList variable = variables.getPropertyList(i);
                                editorstyleid.append(";").append(variable.getProperty("editorstyleid"));
                            }
                            if (editorstyleid.length() > 0) {
                                commandResponse.set("variableeditorstyles", this.getEditorStyles(editorstyleid.substring(1)));
                            }
                        }
                        if (workflowdef.getValue(0, "exectypeflag").equals("S") && workflowexecid.length() == 0) {
                            dbu.createPreparedResultSet("SELECT workflowexecid FROM workflowexec WHERE workflowdefid = ? AND workflowdefversionid = ? AND workflowdefvariantid = ?", new Object[]{workflowdefid, workflowdefversionid, workflowdefvariantid});
                            if (dbu.getNext()) {
                                workflowexecid = dbu.getValue("workflowexecid");
                            }
                        }
                        if (workflowexecid.length() > 0) {
                            dbu.createPreparedResultSet("SELECT workflowexecid, workflowexecname, workflowexecdesc, startdt, startby, execstatus, workflowexec, notes FROM   workflowexec WHERE  workflowexecid = ?", new Object[]{workflowexecid});
                            DataSet primary = new DataSet(this.connectionInfo);
                            primary.setResultSet(dbu.getResultSet(), true, this.connectionInfo.getDbms());
                            PropertyList workflowexec = new PropertyList();
                            workflowexec.setPropertyList(primary.getValue(0, "workflowexec"));
                            primary.setValue(0, "workflowexec", "");
                            commandResponse.set("workflowexecprimary", primary);
                            commandResponse.set("workflowexec", workflowexec);
                        }
                        commandResponse.set("workflowexeccount", String.valueOf(dbu.getPreparedCount("SELECT count(*) FROM workflowexec WHERE workflowdefid = ? AND workflowdefversionid = ? AND workflowdefvariantid = ?", new Object[]{workflowdefid, workflowdefversionid, workflowdefvariantid})));
                    } else {
                        commandResponse.setStatusFail("Failed to load workflow. Reason: workflow '" + workflowdefid + "' not found!");
                    }
                } else {
                    commandResponse.setStatusFail("Failed to load workflow. Reason: " + sdiProcessor.getLastException().getMessage(), sdiProcessor.getLastException());
                }
            }
        } else {
            commandResponse.setStatusFail("Workflowdefid or workflowexecid missing from request!");
        }
    }

    private void loadWorkflowExecutions(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) throws SapphireException {
        String workflowdefid = commandRequest.getString("workflowdefid");
        String workflowdefversionid = commandRequest.getString("workflowdefversionid", "1");
        String workflowdefvariantid = commandRequest.getString("workflowdefvariantid");
        String workflowexecid = commandRequest.getString("workflowexecid");
        if (workflowdefid.length() > 0 && workflowdefversionid.length() > 0 && workflowdefvariantid.length() > 0) {
            dbu.createPreparedResultSet("SELECT workflowexecid, workflowexecname, workflowexecdesc, startdt, startby, execstatus FROM   workflowexec WHERE  workflowdefid = ? AND workflowdefversionid = ? AND workflowdefvariantid = ? ORDER BY startdt DESC", new Object[]{workflowdefid, workflowdefversionid, workflowdefvariantid});
            commandResponse.set("workflowexecutions", new DataSet(dbu.getResultSet()));
        } else if (workflowexecid.length() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            dbu.createPreparedResultSet("SELECT workflowexecid, workflowexecname, workflowexecdesc, startdt, startby, execstatus FROM   workflowexec WHERE  workflowexecid IN ( " + safeSQL.addIn(workflowexecid, ";") + " ) ORDER BY workflowexecname", safeSQL.getValues());
            commandResponse.set("workflowexecutions", new DataSet(dbu.getResultSet()));
        } else {
            commandResponse.setStatusFail("Workflowdefid or workflowdefversionid or workflowdefvariantid or workflowexecid missing from request!");
        }
    }

    private void loadTaskExecutions(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) throws SapphireException {
        String workflowexecid = commandRequest.getString("workflowexecid");
        String taskdefitemid = commandRequest.getString("taskdefitemid");
        if (workflowexecid.length() > 0 && taskdefitemid.length() > 0) {
            dbu.createPreparedResultSet("SELECT DISTINCT taskexec.taskexecid, taskexec.taskexecdesc, taskexec.startdt, taskexec.createby, taskexec.completedt, taskexec.execstatus, taskexec.assignedanalyst, taskexec.moddt, taskexec.modby FROM   taskexec, taskexecitem, workflowexec WHERE  workflowexec.workflowexecid = ?  AND   taskexecitem.taskdefitemid = ?  AND   taskexec.taskexecid = taskexecitem.taskexecid  AND   workflowexec.workflowexecid = taskexecitem.workflowexecid  AND   taskexec.execstatus IN ( 'P', 'I' ) UNION SELECT DISTINCT taskexec.taskexecid, taskexec.taskexecdesc, taskexec.startdt, taskexec.createby, taskexec.completedt, taskexec.execstatus, taskexec.assignedanalyst, taskexec.moddt, taskexec.modby FROM   taskexec, workflowexec WHERE  workflowexec.workflowexecid = ?  AND   taskexec.taskdefitemid = ?  AND   workflowexec.workflowexecid = taskexec.workflowexecid  AND   taskexec.execstatus IN ( 'P', 'I' ) ORDER BY 3 DESC", new Object[]{workflowexecid, taskdefitemid, workflowexecid, taskdefitemid});
            commandResponse.set("taskexecutions", new DataSet(dbu.getResultSet()));
        } else {
            commandResponse.setStatusFail("Workflowexecid or taskdefitemid missing from request!");
        }
    }

    private void moveTaskQueueItems(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) {
        try {
            String taskqueueid = commandRequest.getString("taskqueueid");
            String workflowexecid = commandRequest.getString("workflowexecid");
            String workflowdefid = commandRequest.getString("workflowdefid");
            String workflowdefversionid = commandRequest.getString("workflowdefversionid", "1");
            String workflowdefvariantid = commandRequest.getString("workflowdefvariantid");
            String taskdefid = commandRequest.getString("taskdefid");
            String taskdefversionid = commandRequest.getString("taskdefversionid");
            String taskdefvariantid = commandRequest.getString("taskdefvariantid");
            String taskdefitemid = commandRequest.getString("taskdefitemid");
            String ioid = commandRequest.getString("ioid");
            WorkflowManager.moveTaskQueueItems(this.sapphireConnection, dbu, taskqueueid, workflowexecid, workflowdefid, workflowdefversionid, workflowdefvariantid, taskdefid, taskdefversionid, taskdefvariantid, taskdefitemid, ioid);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to remove queue items. Reason: " + e.getMessage(), e);
        }
    }

    private void newTaskQueueItems(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) {
        try {
            String workflowexecid = commandRequest.getString("workflowexecid");
            String taskdefitemid = commandRequest.getString("taskdefitemid");
            String ioid = commandRequest.getString("ioid");
            SDIList newQueueItems = commandRequest.getSDIList("newqueueitems");
            WorkflowManager.newTaskQueueItems(this.sapphireConnection, dbu, workflowexecid, taskdefitemid, ioid, newQueueItems);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to remove queue items. Reason: " + e.getMessage(), e);
        }
    }

    private void workflowExecNameCheck(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) {
        try {
            String workflowdefid = commandRequest.getString("workflowdefid");
            String workflowdefversionid = commandRequest.getString("workflowdefversionid", "1");
            String workflowdefvariantid = commandRequest.getString("workflowdefvariantid");
            dbu.createPreparedResultSet("SELECT workflowexecname FROM workflowexec WHERE workflowdefid = ? AND workflowdefversionid = ? AND workflowdefvariantid = ? AND workflowexecname = ?", new Object[]{workflowdefid, workflowdefversionid, workflowdefvariantid, commandRequest.getString("workflowexecname")});
            if (dbu.getNext()) {
                commandResponse.set("exists", "Y");
                int seq = this.getSequenceProcessor().getSequence("LV_WorkflowExec", workflowdefid + ";" + workflowdefversionid + ";" + workflowdefvariantid);
                commandResponse.set("workflowexecname", workflowdefid + "_" + seq);
            } else {
                commandResponse.set("exists", "N");
            }
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to check new workflow exec name. Reason: " + e.getMessage(), e);
        }
    }

    private void newWorkflowExec(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) {
        try {
            String workflowexecid = WorkflowManager.createNewWorkflowExec(this.sapphireConnection, dbu, commandRequest.getString("workflowdefid"), commandRequest.getString("workflowdefversionid"), commandRequest.getString("workflowdefvariantid"), commandRequest.getString("workflowexecname"), commandRequest.getString("workflowexecdesc"), commandRequest.getCollection("variables"), commandRequest.getString("auditactivity"), commandRequest.getString("auditreason"), commandRequest.getString("auditsignedflag").equals("Y"));
            commandResponse.set("workflowexecid", workflowexecid);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to create new workflow exec. Reason: " + e.getMessage(), e);
        }
    }

    private void pauseTaskExec(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) {
        try {
            WorkflowManager.suspendTask(this.sapphireConnection, dbu, commandRequest.getString("taskexecid"), commandRequest.getTaskContext("taskcontext"), commandRequest.getJSONableMap("taskdata"), commandRequest.getString("waituntil"));
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to pause task. Reason: " + e.getMessage(), e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void processScript(CommandRequest commandRequest, CommandResponse commandResponse) {
        String auditReason = commandRequest.getString("auditreason");
        String auditActivity = commandRequest.getString("auditactivity");
        String auditSignedFlag = commandRequest.getString("auditsignedflag");
        String tracelogid = "";
        AuditService auditService = new AuditService(this.sapphireConnection);
        if (auditReason.length() > 0 && auditActivity.length() > 0) {
            try {
                tracelogid = this.getTracelogid(auditReason, auditActivity, auditSignedFlag, "", commandRequest.getString("standard").equals("Y"), commandRequest.getString("auditdesc"));
                auditService.setTracelogIdInDBSession(tracelogid);
            }
            catch (Exception e) {
                commandResponse.setStatusFail("Failed to execute action block. Reason: " + e.getMessage(), e);
                return;
            }
        }
        HashMap<String, Object> bindings = new HashMap<String, Object>();
        bindings.put("taskContext", commandRequest.getTaskContext("taskcontext"));
        bindings.put("variables", TaskDef.getTaskVariablesProcessingMap(commandRequest.getJSONableMap("variables"), this.connectionInfo));
        String scripttype = commandRequest.getString("processscripttype", "B");
        if (scripttype.equals("B")) {
            String actionblock = commandRequest.getString("processscript");
            try {
                ActionBlock ab = new ActionBlock(actionblock);
                ab.setDebugLog("");
                ab.setGroovyBindings(bindings);
                this.getActionProcessor().processActionBlock(ab);
                String log = ab.getDebugLog();
                PropertyList returnProps = new PropertyList(ab.getReturnProperties());
                JSONableMap output = new JSONableMap();
                for (String propertyid : returnProps.keySet()) {
                    output.put(propertyid, new JSONableString(returnProps.getProperty(propertyid)));
                }
                commandResponse.set("output", output);
                commandResponse.set("log", log);
            }
            catch (Exception e) {
                commandResponse.setStatusFail("Failed to execute action block. Reason: " + e.getMessage(), e);
            }
            finally {
                if (tracelogid.length() > 0) {
                    try {
                        auditService.removeTracelogIdFromDBSession();
                    }
                    catch (ServiceException e) {}
                }
            }
        } else if (scripttype.equals("G")) {
            String groovyscript = commandRequest.getString("processscript");
            StringBuffer groovyLog = new StringBuffer();
            try {
                bindings.put("stepproperty", commandRequest.getPropertyList("stepproperty"));
                HashMap output = new HashMap();
                bindings.put("output", output);
                ProcessingUtil.processScript(this.sapphireConnection, groovyscript, bindings, groovyLog, "TASKPROCESSING");
                String log = groovyLog.toString();
                commandResponse.set("output", new JSONableMap((Map)output));
                commandResponse.set("log", log);
            }
            catch (Exception e) {
                commandResponse.setStatusFail("Failed to evaluate groovy expression '" + groovyscript + "'. Reason: " + ProcessingUtil.stripHeaderCode(e.getMessage()), e);
            }
            finally {
                if (tracelogid.length() > 0) {
                    try {
                        auditService.removeTracelogIdFromDBSession();
                    }
                    catch (ServiceException serviceException) {}
                }
            }
        }
    }

    private void saveTask(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) {
        try {
            WorkflowManager.saveTask(this.sapphireConnection, dbu, commandRequest.getString("taskexecid"), commandRequest.getTaskContext("taskcontext"), commandRequest.getJSONableMap("taskdata"));
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to save task. Reason: " + e.getMessage(), e);
        }
    }

    private boolean saveVariable(CommandRequest commandRequest, CommandResponse commandResponse) {
        Object variable = commandRequest.get("variable");
        if (variable instanceof SDIMaint) {
            ActionService actionService = new ActionService(this.sapphireConnection);
            AuditService auditService = new AuditService(this.sapphireConnection);
            ErrorHandler errorHandler = new ErrorHandler();
            try {
                SDIMaint sdiMaint = (SDIMaint)variable;
                sdiMaint.setSDCProps(this.getSDCProcessor().getPropertyList(sdiMaint.getSdcid()));
                this.logInfo("Saving SDIMaint for " + new SDI(sdiMaint.getSdcid(), sdiMaint.getKeyid1(), sdiMaint.getKeyid2(), sdiMaint.getKeyid3()).toString());
                JSONableMap extraprops = (JSONableMap)commandRequest.get("inputs");
                sdiMaint.save(actionService, auditService, errorHandler, this, extraprops);
                commandResponse.set("ERRORHANDLER", errorHandler);
                return true;
            }
            catch (ServiceException se) {
                commandResponse.set("ERRORHANDLER", errorHandler);
                return true;
            }
            catch (Exception e) {
                commandResponse.setStatusFail("Failed to save SDI datasource. Reason: " + e.getMessage(), e);
            }
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void loadSDIWorkflowData(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) {
        String sdcid = commandRequest.getString("sdcid");
        String keyid1 = commandRequest.getString("keyid1");
        String keyid2 = commandRequest.getString("keyid2");
        String keyid3 = commandRequest.getString("keyid3");
        QueryService queryService = new QueryService(this.sapphireConnection);
        DataAccessService das = new DataAccessService(this.sapphireConnection);
        RSet rsetTQ = new RSet("___");
        RSet rsetTE = new RSet("___");
        try {
            StringBuffer sql = new StringBuffer();
            ArrayList<String> rsets = new ArrayList<String>();
            rsetTQ = das.createRSetQ("TaskQueueItem", "", null, "taskqueue", "taskqueue.queuesdcid='" + sdcid + "' AND queuekeyid1='" + keyid1 + "'" + (keyid2.length() > 0 ? " AND queuekeyid2='" + keyid2 + "'" : "") + (keyid3.length() > 0 ? " AND queuekeyid3='" + keyid3 + "'" : ""), "", "", 0);
            sql.append(this.getTaskQueueSQL(false, "", ""));
            rsets.add(rsetTQ.getRsetid());
            rsetTE = das.createRSetQ("LV_TaskExec", "", null, "taskexec, taskexecitem", "taskexec.taskexecid = taskexecitem.taskexecid AND taskexecitem.itemsdcid='" + sdcid + "' AND taskexecitem.itemkeyid1='" + keyid1 + "'" + (keyid2.length() > 0 ? " AND taskexecitem.itemkeyid2='" + keyid2 + "'" : "") + (keyid3.length() > 0 ? " AND taskexecitem.itemkeyid3='" + keyid3 + "'" : ""), "", "", 0);
            sql.append(" UNION ").append(this.getTaskExecSQL(false, "", ""));
            rsets.add(rsetTE.getRsetid());
            rsets.add(rsetTE.getRsetid());
            DataSet data = queryService.getPreparedSqlDataSet(sql.toString() + " ORDER BY workflowdefid, workflowdefversionid, workflowdefvariantid, workflowexecid, taskexecid, taskdefid, taskdefversionid, taskdefvariantid", rsets.toArray(new Object[rsets.size()]));
            commandResponse.set("data", data);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to load SDI workflow data. Reason: " + e.getMessage(), e);
        }
        finally {
            try {
                if (rsetTQ != null && !rsetTQ.getRsetid().equals("___")) {
                    das.clearRSet(rsetTQ);
                }
                if (rsetTE != null && !rsetTE.getRsetid().equals("___")) {
                    das.clearRSet(rsetTE);
                }
            }
            catch (ServiceException e) {
                this.logError("Failed to clear rsets. Reason: " + e.getMessage(), e);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     */
    private void search(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) {
        block62: {
            PropertyList search = commandRequest.getPropertyList("search");
            String searchtype = search.getProperty("type");
            String postprocessing = commandRequest.getString("postprocessing", "N");
            String sortby = commandRequest.getString("sortby");
            String sortbydir = commandRequest.getString("sortbydir", "ASC");
            String groupby = commandRequest.getString("groupby");
            String groupbysortdir = commandRequest.getString("groupbysortdir", "A");
            boolean applyUserRestrictions = commandRequest.getBoolean("applyuserrestrictions");
            String wfexecstatus = commandRequest.getString("wfexecstatus");
            String wfeexecstatus = commandRequest.getString("wfeexecstatus");
            String queryid = search.getProperty("queryid");
            String[] queryparams = null;
            if (commandRequest.getString("queryparams").length() > 0) {
                queryparams = StringUtil.split(commandRequest.getString("queryparams"), ";");
            }
            String queryfrom = WorkflowManagerRequest.evalTokens(this.sapphireConnection, search.getProperty("queryfrom"));
            String querywhere = WorkflowManagerRequest.evalTokens(this.sapphireConnection, search.getProperty("querywhere"));
            boolean dateGrouping = groupby.endsWith("dt");
            DataAccessService das = new DataAccessService(this.sapphireConnection);
            RSet rset = null;
            RSet rsetTD = new RSet("___");
            RSet rsetTQ = new RSet("___");
            RSet rsetTE = new RSet("___");
            DataSet searchResults = new DataSet(this.connectionInfo);
            if (dateGrouping) {
                searchResults.addColumn("datesort", 0);
                searchResults.addColumn(groupby + "_datetext", 0);
            }
            DateTimeUtil dtu = new DateTimeUtil(this.sapphireConnection);
            TranslationProcessor trans = this.getTranslationProcessor();
            SDIRequest sdiRequest = new SDIRequest();
            if (queryid.length() > 0) {
                sdiRequest.setQueryid(queryid);
                sdiRequest.setQueryParams(queryparams);
            } else {
                sdiRequest.setQueryFrom(queryfrom);
                sdiRequest.setQueryWhere(querywhere);
            }
            String taskorderby = " ORDER BY taskdefid, taskdefversionid, taskdefvariantid, taskexecgroup, workflowdefid, workflowdefversionid, workflowdefvariantid, taskdefitemid";
            String workfloworderby = " ORDER BY workflowdefid, workflowdefversionid, workflowdefvariantid, workflowexecid, taskdefid, taskdefversionid, taskdefvariantid";
            if (searchtype.equals("tasks") || searchtype.equals("workflows")) {
                PropertyList te;
                PropertyList tq;
                boolean ignoreNoQueueItemTasks = search.getProperty("ignorenoqueueitemtasks", "N").equalsIgnoreCase("Y");
                StringBuffer sql = new StringBuffer();
                ArrayList<String> rsets = new ArrayList<String>();
                PropertyList td = search.getPropertyList("LV_TaskDef");
                if (td != null && td.getProperty("include").equals("Y")) {
                    String where = td.getProperty("querywhere") + "AND " + WorkflowManager.getUserAssignmentClause("workflowdeftask", this.sapphireConnection.getSysuserId(), "sysuserid", "departmentid", "roleid");
                    rsetTD = das.createRSetQ("LV_TaskDef", "", null, WorkflowManagerRequest.evalTokens(this.sapphireConnection, td.getProperty("queryfrom")), WorkflowManagerRequest.evalTokens(this.sapphireConnection, where), "", "", 0);
                    sql.append(this.getTaskDefSQL(applyUserRestrictions, wfexecstatus, wfeexecstatus));
                    rsets.add(rsetTD.getRsetid());
                }
                if ((tq = search.getPropertyList("TaskQueueItem")) != null && tq.getProperty("include").equals("Y")) {
                    String where = tq.getProperty("querywhere") + "AND " + WorkflowManager.getUserAssignmentClause("taskqueue", this.sapphireConnection.getSysuserId(), "assignedanalyst", "assigneddepartment", "assignedrole");
                    rsetTQ = das.createRSetQ("TaskQueueItem", "", null, WorkflowManagerRequest.evalTokens(this.sapphireConnection, tq.getProperty("queryfrom")), WorkflowManagerRequest.evalTokens(this.sapphireConnection, where), "", "", 0);
                    sql.append(sql.length() > 0 ? " UNION " : "").append(this.getTaskQueueSQL(applyUserRestrictions, wfexecstatus, wfeexecstatus));
                    rsets.add(rsetTQ.getRsetid());
                }
                if ((te = search.getPropertyList("LV_TaskExec")) != null && te.getProperty("include").equals("Y")) {
                    rsetTE = das.createRSetQ("LV_TaskExec", "", null, WorkflowManagerRequest.evalTokens(this.sapphireConnection, te.getProperty("queryfrom")), WorkflowManagerRequest.evalTokens(this.sapphireConnection, te.getProperty("querywhere")), "", "", 0);
                    sql.append(sql.length() > 0 ? " UNION " : "").append(this.getTaskExecSQL(applyUserRestrictions, wfexecstatus, wfeexecstatus));
                    rsets.add(rsetTE.getRsetid());
                    rsets.add(rsetTE.getRsetid());
                }
                dbu.createPreparedResultSet(sql.append(postprocessing.equals("T") ? taskorderby : workfloworderby).toString(), rsets.toArray(new Object[rsets.size()]));
                DataSet taskdata2 = new DataSet(this.connectionInfo);
                taskdata2.setResultSet(dbu.getResultSet());
                if (postprocessing.equals("T")) {
                    this.postProcessTaskData(taskdata2, groupby, groupbysortdir, dateGrouping, commandRequest.getString("sortby", "taskdefid"), commandRequest.getString("sortbydir", "A"), searchResults, dtu, trans, ignoreNoQueueItemTasks, true);
                } else {
                    this.postProcessWorkflowData(taskdata2, searchResults, true, false);
                }
            } else if (searchtype.equals("LV_TaskDef")) {
                rset = das.createRSetQ("LV_TaskDef", queryid, queryparams, queryfrom, querywhere, "", "", 0);
                String sql = this.getTaskDefSQL(applyUserRestrictions, wfexecstatus, wfeexecstatus) + (postprocessing.equals("T") ? taskorderby : workfloworderby);
                dbu.createPreparedResultSet(sql, new Object[]{rset.getRsetid()});
                DataSet taskdata3 = new DataSet(this.connectionInfo);
                taskdata3.setResultSet(dbu.getResultSet());
                if (postprocessing.equals("T")) {
                    this.postProcessTaskData(taskdata3, groupby, groupbysortdir, dateGrouping, commandRequest.getString("sortby", "taskdefid"), commandRequest.getString("sortbydir", "A"), searchResults, dtu, trans, true, false);
                } else {
                    this.postProcessWorkflowData(taskdata3, searchResults, false, false);
                }
            } else if (searchtype.equals("TaskQueueItem")) {
                rset = das.createRSetQ("TaskQueueItem", queryid, queryparams, queryfrom, querywhere, "", "", 0);
                String sql = this.getTaskQueueSQL(applyUserRestrictions, wfexecstatus, wfeexecstatus) + (postprocessing.equals("T") ? taskorderby : workfloworderby);
                dbu.createPreparedResultSet(sql, new Object[]{rset.getRsetid()});
                DataSet taskdata4 = new DataSet(this.connectionInfo);
                taskdata4.setResultSet(dbu.getResultSet());
                if (postprocessing.equals("T")) {
                    this.postProcessTaskData(taskdata4, groupby, groupbysortdir, dateGrouping, commandRequest.getString("sortby", "taskdefid"), commandRequest.getString("sortbydir", "A"), searchResults, dtu, trans, true, false);
                } else {
                    this.postProcessWorkflowData(taskdata4, searchResults, false, false);
                }
            } else if (searchtype.equals("LV_TaskExec")) {
                rset = das.createRSetQ("LV_TaskExec", queryid, queryparams, queryfrom, querywhere, "", "", 0);
                String sql = this.getTaskExecSQL(applyUserRestrictions, wfexecstatus, wfeexecstatus) + (postprocessing.equals("T") ? " ORDER BY " + (sortby.length() > 0 ? sortby + (sortbydir.length() > 0 ? " " + sortbydir : " ASC") : "taskexecid DESC") : workfloworderby);
                dbu.createPreparedResultSet(sql, new Object[]{rset.getRsetid(), rset.getRsetid()});
                DataSet taskdata = new DataSet(this.connectionInfo);
                taskdata.setResultSet(dbu.getResultSet());
                if (postprocessing.equals("W")) {
                    this.postProcessWorkflowData(taskdata, searchResults, false, false);
                } else if (postprocessing.equals("T")) {
                    this.postProcessTaskData(taskdata, groupby, groupbysortdir, dateGrouping, commandRequest.getString("sortby", "taskdefid"), commandRequest.getString("sortbydir", "A"), searchResults, dtu, trans, true, false);
                } else {
                    searchResults = taskdata;
                    if (groupby.length() > 0) {
                        if (dateGrouping) {
                            for (int i = 0; i < searchResults.size(); ++i) {
                                WorkflowManagerRequest.setDateGroupText(groupby, searchResults, i, dtu, trans);
                            }
                        }
                        searchResults.sort((dateGrouping ? "datesort D" : groupby + " " + groupbysortdir) + "," + commandRequest.getString("sortby", "taskexecid") + " " + commandRequest.getString("sortbydir", "D"));
                    }
                }
            } else if (searchtype.equals("LV_WorkflowDef")) {
                rset = das.createRSetQ("LV_WorkflowDef", queryid, queryparams, queryfrom, querywhere, "", "", 0);
                String sql = this.getWorkflowDefSQL(applyUserRestrictions, wfexecstatus, wfeexecstatus) + (postprocessing.equals("T") ? taskorderby : workfloworderby);
                dbu.createPreparedResultSet(sql, new Object[]{rset.getRsetid()});
                DataSet workflowdata = new DataSet(this.connectionInfo);
                workflowdata.setResultSet(dbu.getResultSet());
                if (postprocessing.equals("T")) {
                    this.postProcessTaskData(workflowdata, groupby, groupbysortdir, dateGrouping, commandRequest.getString("sortby", "taskdefid"), commandRequest.getString("sortbydir", "A"), searchResults, dtu, trans, true, false);
                } else {
                    this.postProcessWorkflowData(workflowdata, searchResults, false, search.getProperty("excludeautoexecutions").equals("Y"));
                }
            } else if (searchtype.equals("LV_WorkflowExec")) {
                sdiRequest.setSDCid("LV_WorkflowExec");
                sdiRequest.setRequestItem("primary[workflowexecid, workflowdefid, workflowdefversionid, workflowdefvariantid, workflowexecdesc, startdt, startby, completedt, completeby, exectypeflag, execstatus, createdt, createby, moddt, modby, (select count(distinct taskqueue.taskdefid) from taskqueue where taskqueue.workflowexecid = workflowexec.workflowexecid ) count ]");
                SDIProcessor sdiProcessor = this.getSDIProcessor();
                SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
                if (sdiData != null) {
                    searchResults = sdiData.getDataset("primary");
                    for (int i = searchResults.size() - 1; i >= 0; --i) {
                        if (searchResults.getInt(i, "count") == 0) {
                            searchResults.deleteRow(i);
                            continue;
                        }
                        if (!dateGrouping) continue;
                        WorkflowManagerRequest.setDateGroupText(groupby, searchResults, i, dtu, trans);
                    }
                    if (groupby.length() > 0) {
                        searchResults.sort((dateGrouping ? "datesort D" : groupby) + "," + commandRequest.getString("sortby", "workflowexecid") + " " + commandRequest.getString("sortbydir", "A"));
                    }
                }
            } else if (searchtype.equals("users")) {
                sdiRequest.setSDCid("User");
                sdiRequest.setRequestItem("primary[sysuser.sysuserid, sysuser.sysuserdesc, (SELECT sdiattachment.attachmentnum FROM sdiattachment WHERE sdiattachment.sdcid = 'User' AND sdiattachment.keyid1 = sysuser.sysuserid AND attachmentclass = 'Image') icon ]");
                SDIProcessor sdiProcessor = this.getSDIProcessor();
                SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
                if (sdiData != null) {
                    searchResults = sdiData.getDataset("primary");
                }
            }
            commandResponse.set("searchresults", searchResults);
            try {
                if (rset != null) {
                    das.clearRSet(rset);
                }
                if (rsetTD != null && !rsetTD.getRsetid().equals("___")) {
                    das.clearRSet(rsetTD);
                }
                if (rsetTQ != null && !rsetTQ.getRsetid().equals("___")) {
                    das.clearRSet(rsetTQ);
                }
                if (rsetTE != null && !rsetTE.getRsetid().equals("___")) {
                    das.clearRSet(rsetTE);
                }
                break block62;
            }
            catch (ServiceException e) {
                this.logError("Failed to clear rset '" + rset.getRsetid() + "'. Reason: " + e.getMessage(), e);
            }
            break block62;
            catch (Exception e) {
                try {
                    commandResponse.setStatusFail("Failed to execute search. Reason: " + e.getMessage(), e);
                }
                catch (Throwable throwable) {
                    try {
                        if (rset != null) {
                            das.clearRSet(rset);
                        }
                        if (rsetTD != null && !rsetTD.getRsetid().equals("___")) {
                            das.clearRSet(rsetTD);
                        }
                        if (rsetTQ != null && !rsetTQ.getRsetid().equals("___")) {
                            das.clearRSet(rsetTQ);
                        }
                        if (rsetTE != null && !rsetTE.getRsetid().equals("___")) {
                            das.clearRSet(rsetTE);
                        }
                    }
                    catch (ServiceException e2) {
                        this.logError("Failed to clear rset '" + rset.getRsetid() + "'. Reason: " + e2.getMessage(), e2);
                    }
                    throw throwable;
                }
                try {
                    if (rset != null) {
                        das.clearRSet(rset);
                    }
                    if (rsetTD != null && !rsetTD.getRsetid().equals("___")) {
                        das.clearRSet(rsetTD);
                    }
                    if (rsetTQ != null && !rsetTQ.getRsetid().equals("___")) {
                        das.clearRSet(rsetTQ);
                    }
                    if (rsetTE != null && !rsetTE.getRsetid().equals("___")) {
                        das.clearRSet(rsetTE);
                    }
                }
                catch (ServiceException e3) {
                    this.logError("Failed to clear rset '" + rset.getRsetid() + "'. Reason: " + e3.getMessage(), e3);
                }
            }
        }
    }

    private String getTaskQueueSQL(boolean applyUserRestrictions, String wfexecstatus, String wfeexecstatus) {
        return "SELECT taskqueue.taskdefid, taskqueue.taskdefversionid, taskqueue.taskdefvariantid, td.taskdefdesc, td.icon, td.shorttitle, td.longtitle, td.scopeflag,        taskqueue.ioid, taskqueue.queuesdcid itemsdcid, taskqueue.queuekeyid1 itemkeyid1, taskqueue.queuekeyid2 itemkeyid2, taskqueue.queuekeyid3 itemkeyid3, taskqueue.duedt, taskqueue.createdt, taskqueue.taskqueueid, taskqueue.workflowexecid,        taskqueue.workflowdefid, taskqueue.workflowdefversionid, taskqueue.workflowdefvariantid,        taskqueue.taskdefitemid, taskqueue.taskexecgroup, taskqueue.assignedanalyst,        workflowdeftask.starttaskflag, NULL taskexecid, NULL taskexecdesc, NULL startdt, NULL completedt, NULL execcreateby, NULL execmodby, NULL execmoddt, NULL execstatus, NULL summary, NULL execstatus_text, NULL notes,        workflowexec.workflowexecname, workflowexec.workflowexecdesc, workflowdef.exectypeflag, workflowexec.execstatus wfeexecstatus, workflowdef.execstatus wfexecstatus FROM   rsetitems, taskqueue, taskdef td, workflowdeftask, workflowexec, workflowdef WHERE  taskqueue.taskqueueid = rsetitems.keyid1 AND rsetitems.rsetid = ?  AND   taskqueue.taskdefid = td.taskdefid AND taskqueue.taskdefversionid = td.taskdefversionid AND taskqueue.taskdefvariantid = td.taskdefvariantid  AND   taskqueue.workflowdefid = workflowdeftask.workflowdefid AND taskqueue.workflowdefversionid = workflowdeftask.workflowdefversionid AND taskqueue.workflowdefvariantid = workflowdeftask.workflowdefvariantid  AND   taskqueue.taskdefitemid = workflowdeftask.taskdefitemid  AND   td.taskdefid = workflowdeftask.taskdefid AND td.taskdefversionid = workflowdeftask.taskdefversionid AND td.taskdefvariantid = workflowdeftask.taskdefvariantid  AND   taskqueue.workflowexecid = workflowexec.workflowexecid  AND   taskqueue.queuestatus IN ( 'W', 'S' )  AND   workflowdef.workflowdefid = workflowexec.workflowdefid AND workflowdef.workflowdefversionid = workflowexec.workflowdefversionid AND workflowdef.workflowdefvariantid = workflowexec.workflowdefvariantid " + (applyUserRestrictions ? " AND " + WorkflowManager.getUserAssignmentClause("taskqueue", this.sapphireConnection.getSysuserId(), "assignedanalyst", "assigneddepartment", "assignedrole") : "") + (wfexecstatus.length() > 0 ? " AND ( workflowdef.execstatus IS NULL OR workflowdef.execstatus = '' OR workflowdef.execstatus IN ('" + StringUtil.replaceAll(wfexecstatus, ";", "','") + "') )" : "") + (wfeexecstatus.length() > 0 ? "AND ( workflowexec.execstatus IS NULL OR workflowexec.execstatus = '' OR workflowexec.execstatus IN ('" + StringUtil.replaceAll(wfeexecstatus, ";", "','") + "') )" : "");
    }

    private String getTaskExecSQL(boolean applyUserRestrictions, String wfexecstatus, String wfeexecstatus) {
        return "SELECT taskexec.taskdefid, taskexec.taskdefversionid, taskexec.taskdefvariantid, taskdef.taskdefdesc, taskdef.icon, taskdef.shorttitle, taskdef.longtitle, taskdef.scopeflag,        NULL ioid, NULL itemsdcid, NULL itemkeyid1, NULL itemkeyid2, NULL itemkeyid3, NULL duedt, NULL createdt, NULL taskqueueid, taskexec.workflowexecid,        workflowexec.workflowdefid, workflowexec.workflowdefversionid, workflowexec.workflowdefvariantid,        taskexec.taskdefitemid, NULL taskexecgroup, taskexec.assignedanalyst,        'N' starttaskflag, taskexec.taskexecid, taskexec.taskexecdesc, taskexec.startdt, taskexec.completedt, taskexec.createby execcreateby, taskexec.modby execmodby, taskexec.moddt execmoddt, taskexec.execstatus, taskexec.summary,        CASE WHEN taskexec.execstatus = 'A' THEN 'Active' WHEN taskexec.execstatus = 'C' THEN 'Complete' WHEN taskexec.execstatus = 'X' THEN 'Cancelled' WHEN taskexec.execstatus = 'P' THEN 'Paused' WHEN taskexec.execstatus = 'W' THEN 'Waiting' WHEN taskexec.execstatus = 'I' THEN 'Incomplete' ELSE 'Unknown' END AS execstatus_text,        (SELECT CASE COUNT(*) WHEN 0 THEN 0 ELSE 1 END FROM sdinote WHERE sdcid='LV_TaskExec' and keyid1 = taskexec.taskexecid) notes,        workflowexec.workflowexecname, workflowexec.workflowexecdesc, workflowdef.exectypeflag, workflowexec.execstatus wfeexecstatus, workflowdef.execstatus wfexecstatus FROM   rsetitems, taskexec, taskdef, workflowexec, workflowdef WHERE  taskexec.taskexecid = rsetitems.keyid1 AND rsetitems.rsetid = ?  AND   taskexec.taskdefid = taskdef.taskdefid AND taskexec.taskdefversionid = taskdef.taskdefversionid AND taskexec.taskdefvariantid = taskdef.taskdefvariantid  AND   taskexec.workflowexecid = workflowexec.workflowexecid  AND   taskexec.workflowexecid IS NOT NULL  AND   workflowdef.workflowdefid = workflowexec.workflowdefid AND workflowdef.workflowdefversionid = workflowexec.workflowdefversionid AND workflowdef.workflowdefvariantid = workflowexec.workflowdefvariantid " + (applyUserRestrictions ? " AND " + WorkflowManager.getUserAssignmentClause("taskexec", this.sapphireConnection.getSysuserId(), "assignedanalyst", "assigneddepartment", "assignedrole") : "") + (wfexecstatus.length() > 0 ? " AND ( workflowdef.execstatus IS NULL OR workflowdef.execstatus = '' OR workflowdef.execstatus IN ('" + StringUtil.replaceAll(wfexecstatus, ";", "','") + "') )" : "") + (wfeexecstatus.length() > 0 ? " AND ( workflowexec.execstatus IS NULL OR workflowexec.execstatus = '' OR workflowexec.execstatus IN ('" + StringUtil.replaceAll(wfeexecstatus, ";", "','") + "') )" : "") + " UNION  SELECT DISTINCT taskexec.taskdefid, taskexec.taskdefversionid, taskexec.taskdefvariantid, taskdef.taskdefdesc, taskdef.icon, taskdef.shorttitle, taskdef.longtitle, taskdef.scopeflag,        taskexecitem.ioid, taskexecitem.itemsdcid itemsdcid, NULL itemkeyid1, NULL itemkeyid2, NULL itemkeyid3, NULL duedt, NULL createdt, NULL taskqueueid, taskexecitem.workflowexecid,        workflowexec.workflowdefid, workflowexec.workflowdefversionid, workflowexec.workflowdefvariantid,        taskexecitem.taskdefitemid, NULL taskexecgroup, taskexec.assignedanalyst,        'N' starttaskflag, taskexec.taskexecid, taskexec.taskexecdesc, taskexec.startdt, taskexec.completedt, taskexec.createby execcreateby, taskexec.modby execmodby, taskexec.moddt execmoddt, taskexec.execstatus, taskexec.summary,        CASE WHEN taskexec.execstatus = 'A' THEN 'Active' WHEN taskexec.execstatus = 'C' THEN 'Complete' WHEN taskexec.execstatus = 'X' THEN 'Cancelled' WHEN taskexec.execstatus = 'P' THEN 'Paused' WHEN taskexec.execstatus = 'W' THEN 'Waiting' WHEN taskexec.execstatus = 'I' THEN 'Incomplete' ELSE 'Unknown' END AS execstatus_text,        (SELECT CASE COUNT(*) WHEN 0 THEN 0 ELSE 1 END FROM sdinote WHERE sdcid='LV_TaskExec' and keyid1 = taskexec.taskexecid) notes,        workflowexec.workflowexecname, workflowexec.workflowexecdesc, workflowdef.exectypeflag, workflowexec.execstatus wfeexecstatus, workflowdef.execstatus wfexecstatus FROM   rsetitems, taskexec, taskdef, taskexecitem, workflowexec, workflowdef WHERE  taskexec.taskexecid = rsetitems.keyid1 AND rsetitems.rsetid = ?  AND   taskexec.taskdefid = taskdef.taskdefid AND taskexec.taskdefversionid = taskdef.taskdefversionid AND taskexec.taskdefvariantid = taskdef.taskdefvariantid  AND   taskexec.taskexecid = taskexecitem.taskexecid  AND   taskexecitem.workflowexecid = workflowexec.workflowexecid  AND   ( taskexec.workflowexecid IS NULL OR taskexec.workflowexecid = '' )  AND   workflowdef.workflowdefid = workflowexec.workflowdefid AND workflowdef.workflowdefversionid = workflowexec.workflowdefversionid AND workflowdef.workflowdefvariantid = workflowexec.workflowdefvariantid " + (applyUserRestrictions ? " AND " + WorkflowManager.getUserAssignmentClause("taskexec", this.sapphireConnection.getSysuserId(), "assignedanalyst", "assigneddepartment", "assignedrole") : "") + (wfexecstatus.length() > 0 ? " AND ( workflowdef.execstatus IS NULL OR workflowdef.execstatus = '' OR workflowdef.execstatus IN ('" + StringUtil.replaceAll(wfexecstatus, ";", "','") + "') )" : "") + (wfeexecstatus.length() > 0 ? " AND ( workflowexec.execstatus IS NULL OR workflowexec.execstatus = '' OR workflowexec.execstatus IN ('" + StringUtil.replaceAll(wfeexecstatus, ";", "','") + "') )" : "");
    }

    private String getTaskDefSQL(boolean applyUserRestrictions, String wfexecstatus, String wfeexecstatus) {
        return "SELECT taskdata.taskdefid, taskdata.taskdefversionid, taskdata.taskdefvariantid, taskdata.taskdefdesc, taskdata.icon, taskdata.shorttitle, taskdata.longtitle, taskdata.scopeflag,        taskqueue.ioid, taskqueue.queuesdcid itemsdcid, taskqueue.queuekeyid1 itemkeyid1, taskqueue.queuekeyid2 itemkeyid2, taskqueue.queuekeyid3 itemkeyid3, taskqueue.duedt, taskqueue.createdt, taskqueue.taskqueueid,        CASE WHEN taskqueue.taskqueueid IS NULL THEN taskdata.workflowexecid ELSE taskqueue.workflowexecid END AS workflowexecid,        CASE WHEN taskqueue.taskqueueid IS NULL THEN taskdata.workflowdefid ELSE taskqueue.workflowdefid END AS workflowdefid,        CASE WHEN taskqueue.taskqueueid IS NULL THEN taskdata.workflowdefversionid ELSE taskqueue.workflowdefversionid END AS workflowdefversionid,        CASE WHEN taskqueue.taskqueueid IS NULL THEN taskdata.workflowdefvariantid ELSE taskqueue.workflowdefvariantid END AS workflowdefvariantid,        CASE WHEN taskqueue.taskqueueid IS NULL THEN taskdata.taskdefitemid ELSE taskqueue.taskdefitemid END AS taskdefitemid,        CASE WHEN taskqueue.taskqueueid IS NULL THEN taskdata.workflowdefid ELSE taskqueue.taskexecgroup END As taskexecgroup,        CASE WHEN taskqueue.taskqueueid IS NULL THEN NULL ELSE taskqueue.assignedanalyst END As assignedanalyst,        taskdata.starttaskflag, NULL taskexecid, NULL taskexecdesc, NULL startdt, NULL completedt, NULL execcreateby, NULL execmodby, NULL execmoddt, NULL execstatus, NULL summary, NULL execstatus_text, NULL notes,        taskdata.workflowexecname, taskdata.workflowexecdesc, taskdata.exectypeflag, taskdata.wfexecstatus, taskdata.wfeexecstatus FROM   taskqueue RIGHT OUTER JOIN        (        SELECT taskdef.taskdefid, taskdef.taskdefversionid, taskdef.taskdefvariantid, taskdef.taskdefdesc, taskdef.icon, taskdef.shorttitle, taskdef.longtitle, taskdef.scopeflag,               workflowdeftask.workflowdefid, workflowdeftask.workflowdefversionid, workflowdeftask.workflowdefvariantid, workflowdeftask.taskdefitemid, workflowdeftask.starttaskflag,               workflowexec.workflowexecid, workflowexec.workflowexecname, workflowexec.workflowexecdesc, workflowdef.execstatus wfexecstatus, workflowexec.execstatus wfeexecstatus, workflowdef.exectypeflag        FROM   rsetitems, taskdef, workflowdeftask,               workflowdef LEFT OUTER JOIN workflowexec                    ON workflowdef.workflowdefid = workflowexec.workflowdefid AND workflowdef.workflowdefversionid = workflowexec.workflowdefversionid AND workflowdef.workflowdefvariantid = workflowexec.workflowdefvariantid " + (wfeexecstatus.length() > 0 ? " AND ( workflowexec.execstatus IS NULL OR workflowexec.execstatus = '' OR workflowexec.execstatus IN ('" + StringUtil.replaceAll(wfeexecstatus, ";", "','") + "') )" : "") + "       WHERE  taskdef.taskdefid = rsetitems.keyid1 AND taskdef.taskdefversionid = rsetitems.keyid2 AND taskdef.taskdefvariantid = rsetitems.keyid3 AND rsetitems.rsetid = ?         AND   taskdef.taskdefid = workflowdeftask.taskdefid AND taskdef.taskdefversionid = workflowdeftask.taskdefversionid AND taskdef.taskdefvariantid = workflowdeftask.taskdefvariantid         AND   workflowdef.workflowdefid = workflowdeftask.workflowdefid AND workflowdef.workflowdefversionid = workflowdeftask.workflowdefversionid AND workflowdef.workflowdefvariantid = workflowdeftask.workflowdefvariantid " + (wfexecstatus.length() > 0 ? " AND ( workflowdef.execstatus IS NULL OR workflowdef.execstatus = '' OR workflowdef.execstatus IN ('" + StringUtil.replaceAll(wfexecstatus, ";", "','") + "') )" : "") + "       ) taskdata ON taskdata.taskdefid = taskqueue.taskdefid AND taskdata.taskdefversionid = taskqueue.taskdefversionid AND taskdata.taskdefvariantid = taskqueue.taskdefvariantid AND taskdata.taskdefitemid = taskqueue.taskdefitemid            AND taskdata.workflowexecid = taskqueue.workflowexecid AND taskdata.workflowdefid = taskqueue.workflowdefid AND taskdata.workflowdefversionid = taskqueue.workflowdefversionid AND taskdata.workflowdefvariantid = taskqueue.workflowdefvariantid            AND taskqueue.queuestatus IN ( '" + "W" + "', '" + "S" + "' ) " + (applyUserRestrictions ? " AND " + WorkflowManager.getUserAssignmentClause("taskqueue", this.sapphireConnection.getSysuserId(), "assignedanalyst", "assigneddepartment", "assignedrole") : "");
    }

    private String getWorkflowDefSQL(boolean applyUserRestrictions, String wfexecstatus, String wfeexecstatus) {
        return "SELECT workflowdata.taskdefid, workflowdata.taskdefversionid, workflowdata.taskdefvariantid, workflowdata.taskdefdesc, workflowdata.icon, workflowdata.shorttitle, workflowdata.longtitle, workflowdata.scopeflag,        CASE WHEN taskqueue.taskqueueid IS NULL THEN workflowdata.workflowexecid ELSE taskqueue.workflowexecid END AS workflowexecid,        taskqueue.ioid, taskqueue.queuesdcid itemsdcid, taskqueue.queuekeyid1 itemkeyid1, taskqueue.queuekeyid2 itemkeyid2, taskqueue.queuekeyid3 itemkeyid3, taskqueue.duedt, taskqueue.createdt, taskqueue.taskqueueid,        CASE WHEN taskqueue.taskqueueid IS NULL THEN workflowdata.workflowdefid ELSE taskqueue.workflowdefid END AS workflowdefid,        CASE WHEN taskqueue.taskqueueid IS NULL THEN workflowdata.workflowdefversionid ELSE taskqueue.workflowdefversionid END AS workflowdefversionid,        CASE WHEN taskqueue.taskqueueid IS NULL THEN workflowdata.workflowdefvariantid ELSE taskqueue.workflowdefvariantid END AS workflowdefvariantid,        CASE WHEN taskqueue.taskqueueid IS NULL THEN workflowdata.taskdefitemid ELSE taskqueue.taskdefitemid END AS taskdefitemid,        CASE WHEN taskqueue.taskqueueid IS NULL THEN workflowdata.workflowdefid ELSE taskqueue.taskexecgroup END As taskexecgroup,        CASE WHEN taskqueue.taskqueueid IS NULL THEN NULL ELSE taskqueue.assignedanalyst END As assignedanalyst,        workflowdata.starttaskflag, NULL taskexecid, NULL taskexecdesc, NULL startdt, NULL completedt, NULL execcreateby, NULL execmodby, NULL execmoddt, NULL execstatus, NULL summary, NULL execstatus_text, NULL notes,        workflowdata.workflowexecname, workflowdata.workflowexecdesc, workflowdata.exectypeflag, workflowdata.wfexecstatus, workflowdata.wfeexecstatus FROM   taskqueue RIGHT OUTER JOIN        (        SELECT taskdef.taskdefid, taskdef.taskdefversionid, taskdef.taskdefvariantid, taskdef.taskdefdesc, taskdef.icon, taskdef.shorttitle, taskdef.longtitle, taskdef.scopeflag,               workflowdeftask.workflowdefid, workflowdeftask.workflowdefversionid, workflowdeftask.workflowdefvariantid, workflowdeftask.taskdefitemid, workflowdeftask.starttaskflag,               workflowexec.workflowexecid, workflowexec.workflowexecname, workflowexec.workflowexecdesc, workflowdef.execstatus wfexecstatus, workflowexec.execstatus wfeexecstatus, workflowdef.exectypeflag        FROM   rsetitems, taskdef, workflowdeftask,               workflowdef LEFT OUTER JOIN workflowexec                    ON workflowdef.workflowdefid = workflowexec.workflowdefid AND workflowdef.workflowdefversionid = workflowexec.workflowdefversionid AND workflowdef.workflowdefvariantid = workflowexec.workflowdefvariantid " + (wfeexecstatus.length() > 0 ? " AND ( workflowexec.execstatus IS NULL OR workflowexec.execstatus = '' OR workflowexec.execstatus IN ('" + StringUtil.replaceAll(wfeexecstatus, ";", "','") + "') )" : "") + "       WHERE  workflowdef.workflowdefid = rsetitems.keyid1 AND workflowdef.workflowdefversionid = rsetitems.keyid2 AND workflowdef.workflowdefvariantid = rsetitems.keyid3 AND rsetitems.rsetid = ?         AND   workflowdef.workflowdefid = workflowdeftask.workflowdefid AND workflowdef.workflowdefversionid = workflowdeftask.workflowdefversionid AND workflowdef.workflowdefvariantid = workflowdeftask.workflowdefvariantid         AND   taskdef.taskdefid = workflowdeftask.taskdefid AND taskdef.taskdefversionid = workflowdeftask.taskdefversionid AND taskdef.taskdefvariantid = workflowdeftask.taskdefvariantid " + (wfexecstatus.length() > 0 ? " AND ( workflowdef.execstatus IS NULL OR workflowdef.execstatus = '' OR workflowdef.execstatus IN ('" + StringUtil.replaceAll(wfexecstatus, ";", "','") + "') )" : "") + "       ) workflowdata            ON workflowdata.taskdefid = taskqueue.taskdefid AND workflowdata.taskdefversionid = taskqueue.taskdefversionid AND workflowdata.taskdefvariantid = taskqueue.taskdefvariantid AND workflowdata.taskdefitemid = taskqueue.taskdefitemid            AND workflowdata.workflowexecid = taskqueue.workflowexecid AND workflowdata.workflowdefid = taskqueue.workflowdefid AND workflowdata.workflowdefversionid = taskqueue.workflowdefversionid AND workflowdata.workflowdefvariantid = taskqueue.workflowdefvariantid            AND taskqueue.queuestatus IN ( '" + "W" + "', '" + "S" + "' ) " + (applyUserRestrictions ? " AND " + WorkflowManager.getUserAssignmentClause("taskqueue", this.sapphireConnection.getSysuserId(), "assignedanalyst", "assigneddepartment", "assignedrole") : "");
    }

    private void postProcessWorkflowData(DataSet workflowdata, DataSet searchResults, boolean excludeNoWFExec, boolean excludeAutoExecutions) {
        HashMap<String, SDIList> sdiListMap = this.getPermittedSDIs(workflowdata);
        ArrayList<DataSet> workflowexecs = workflowdata.getGroupedDataSets("workflowdefid, workflowdefversionid, workflowdefvariantid, workflowexecid");
        String last = "";
        for (int i = 0; i < workflowexecs.size(); ++i) {
            int row;
            DataSet workflowexec = workflowexecs.get(i);
            int queued = 0;
            int paused = 0;
            boolean excludeRow = false;
            String exectype = workflowexec.getValue(0, "exectypeflag", "S");
            if (excludeAutoExecutions && exectype.equals("A")) {
                excludeRow = true;
                if (!last.equals(workflowexec.getValue(0, "workflowdefid") + ";" + workflowexec.getValue(0, "workflowdefversionid") + ";" + workflowexec.getValue(0, "workflowdefvariantid"))) {
                    row = searchResults.addRow();
                    searchResults.setString(row, "workflowdefid", workflowexec.getValue(0, "workflowdefid"));
                    searchResults.setString(row, "workflowdefversionid", workflowexec.getValue(0, "workflowdefversionid"));
                    searchResults.setString(row, "workflowdefvariantid", workflowexec.getValue(0, "workflowdefvariantid"));
                    searchResults.setString(row, "workflowexecid", "");
                    searchResults.setString(row, "workflowexecdesc", workflowexec.getValue(0, "workflowexecdesc"));
                    searchResults.setString(row, "workflowexecname", workflowexec.getValue(0, "workflowexecname"));
                    searchResults.setString(row, "exectypeflag", workflowexec.getValue(0, "exectypeflag"));
                    searchResults.setString(row, "wfexecstatus", workflowexec.getValue(0, "wfexecstatus"));
                    searchResults.setString(row, "wfeexecstatus", workflowexec.getValue(0, "wfeexecstatus"));
                    searchResults.setNumber(row, "queued", 0);
                    searchResults.setNumber(row, "paused", 0);
                    searchResults.setNumber(row, "maxtaskqueued", 0);
                    searchResults.setNumber(row, "maxtaskpaused", 0);
                    last = workflowexec.getValue(0, "workflowdefid") + ";" + workflowexec.getValue(0, "workflowdefversionid") + ";" + workflowexec.getValue(0, "workflowdefvariantid");
                }
            }
            if (excludeRow || excludeNoWFExec && (!excludeNoWFExec || workflowexec.getValue(0, "workflowexecid").length() <= 0) && (!exectype.equals("A") || !workflowexec.getValue(0, "starttaskflag").equals("Y"))) continue;
            row = searchResults.addRow();
            searchResults.setString(row, "workflowdefid", workflowexec.getValue(0, "workflowdefid"));
            searchResults.setString(row, "workflowdefversionid", workflowexec.getValue(0, "workflowdefversionid"));
            searchResults.setString(row, "workflowdefvariantid", workflowexec.getValue(0, "workflowdefvariantid"));
            searchResults.setString(row, "workflowexecid", workflowexec.getValue(0, "workflowexecid"));
            searchResults.setString(row, "workflowexecdesc", workflowexec.getValue(0, "workflowexecdesc"));
            searchResults.setString(row, "workflowexecname", workflowexec.getValue(0, "workflowexecname"));
            searchResults.setString(row, "exectypeflag", workflowexec.getValue(0, "exectypeflag"));
            searchResults.setString(row, "wfexecstatus", workflowexec.getValue(0, "wfexecstatus"));
            searchResults.setString(row, "wfeexecstatus", workflowexec.getValue(0, "wfeexecstatus"));
            String task = "__";
            int maxtaskqueued = 0;
            int maxtaskpaused = 0;
            int taskqueued = 0;
            int taskpaused = 0;
            for (int j = 0; j < workflowexec.size(); ++j) {
                SDIList sdiList;
                if (!task.equals(workflowexec.getValue(j, "taskdefid") + ";" + workflowexec.getValue(j, "taskdefversionid") + ";" + workflowexec.getValue(j, "taskdefvariantid"))) {
                    task = workflowexec.getValue(j, "taskdefid") + ";" + workflowexec.getValue(j, "taskdefversionid") + ";" + workflowexec.getValue(j, "taskdefvariantid");
                    maxtaskqueued = Math.max(maxtaskqueued, taskqueued);
                    maxtaskpaused = Math.max(maxtaskpaused, taskpaused);
                    taskqueued = 0;
                    taskpaused = 0;
                }
                if (workflowexec.getValue(j, "taskexecid").length() > 0) {
                    ++paused;
                    ++taskpaused;
                    continue;
                }
                if (workflowexec.getValue(j, "taskqueueid").length() <= 0 || (sdiList = sdiListMap.get(workflowexec.getValue(j, "itemsdcid"))) == null || sdiList.getListIndex(workflowexec.getValue(j, "itemkeyid1"), workflowexec.getValue(j, "itemkeyid2"), workflowexec.getValue(j, "itemkeyid3")) < 0) continue;
                ++queued;
                ++taskqueued;
            }
            searchResults.setNumber(row, "queued", queued);
            searchResults.setNumber(row, "paused", paused);
            searchResults.setNumber(row, "maxtaskqueued", Math.max(maxtaskqueued, taskqueued));
            searchResults.setNumber(row, "maxtaskpaused", Math.max(maxtaskpaused, taskpaused));
        }
    }

    private void postProcessTaskData(DataSet taskdata, String groupby, String groupbysortdir, boolean dateGrouping, String sortby, String sortbydir, DataSet searchResults, DateTimeUtil dtu, TranslationProcessor trans, boolean ignoreNoQueueItemTasks, boolean excludeNoWFExec) {
        HashMap<String, SDIList> sdiListMap = this.getPermittedSDIs(taskdata);
        ArrayList<DataSet> tasks = taskdata.getGroupedDataSets("taskdefitemid, taskdefid, taskdefversionid, taskdefvariantid, taskexecgroup, taskexecid");
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        for (int i = 0; i < tasks.size(); ++i) {
            int count;
            DataSet task = tasks.get(i);
            String taskdefid = task.getValue(0, "taskdefid");
            String taskExecGroup = task.getValue(0, "taskexecgroup", "G");
            String type = task.getValue(0, "taskexecid").length() > 0 ? "TE" : (task.getValue(0, "taskqueueid").length() > 0 ? "TQ" : "TD");
            String exectype = task.getValue(0, "exectypeflag");
            int n = count = task.getString(0, "taskqueueid") != null ? task.size() : 0;
            if (count <= 0 && (count != 0 || ignoreNoQueueItemTasks) && !type.equals("TE") || excludeNoWFExec && (!excludeNoWFExec || task.getValue(0, "workflowexecid").length() <= 0) && (!exectype.equals("A") || !task.getValue(0, "starttaskflag").equals("Y"))) continue;
            StringBuffer workflowexecs = new StringBuffer();
            StringBuffer workflowdefs = new StringBuffer();
            StringBuffer workflowdefversions = new StringBuffer();
            StringBuffer workflowdefvariants = new StringBuffer();
            StringBuffer taskdefitems = new StringBuffer();
            StringBuffer ioids = new StringBuffer();
            StringBuffer taskqueueids = new StringBuffer();
            WorkflowManagerRequest.getDistinctValues(task, workflowexecs, workflowdefs, workflowdefversions, workflowdefvariants, taskdefitems, ioids, taskqueueids);
            int row = searchResults.addRow();
            if (taskExecGroup.startsWith("G") || taskExecGroup.startsWith("WF") || taskExecGroup.startsWith("WFE")) {
                searchResults.setString(row, "taskexecgroup", taskExecGroup);
            } else {
                searchResults.setString(row, "taskexecgroup", "WF:" + taskdefid + ";" + taskExecGroup);
            }
            searchResults.setString(row, "type", type);
            searchResults.setString(row, "taskdefid", taskdefid);
            searchResults.setString(row, "taskdefversionid", task.getValue(0, "taskdefversionid"));
            searchResults.setString(row, "taskdefvariantid", task.getValue(0, "taskdefvariantid"));
            searchResults.setString(row, "workflowexecid", exectype.equals("A") && task.getValue(0, "starttaskflag").equals("Y") ? "" : (taskExecGroup.startsWith("WFE") ? task.getValue(0, "workflowexecid") : workflowexecs.toString()));
            searchResults.setString(row, "wfeexecstatus", task.getValue(0, "wfeexecstatus"));
            searchResults.setString(row, "workflowexecname", exectype.equals("A") && task.getValue(0, "starttaskflag").equals("Y") ? "" : (searchResults.getValue(row, "workflowexecid").contains(";") ? "Multiple" : task.getValue(0, "workflowexecname")));
            searchResults.setString(row, "workflowdefid", taskExecGroup.startsWith("WF") ? task.getValue(0, "workflowdefid") : workflowdefs.toString());
            searchResults.setString(row, "workflowdefversionid", taskExecGroup.startsWith("WF") ? task.getValue(0, "workflowdefversionid") : workflowdefversions.toString());
            searchResults.setString(row, "workflowtext", LV_WorkflowDef.getText(searchResults.getValue(row, "workflowdefid"), searchResults.getValue(row, "workflowdefversionid"), searchResults.getValue(row, "workflowdefvariantid")));
            searchResults.setString(row, "workflowdefvariantid", taskExecGroup.startsWith("WF") ? task.getValue(0, "workflowdefvariantid") : workflowdefvariants.toString());
            searchResults.setString(row, "taskdefitemid", !taskExecGroup.startsWith("G") ? task.getValue(0, "taskdefitemid") : taskdefitems.toString());
            searchResults.setString(row, "taskqueueid", taskqueueids.toString());
            searchResults.setString(row, "ioid", ioids.toString());
            String sdcid = task.getValue(0, "itemsdcid");
            searchResults.setString(row, "itemsdcid", sdcid);
            if (sdcid.length() > 0) {
                searchResults.setString(row, "singular", sdcProcessor.getProperty(sdcid, "singular").toLowerCase());
                searchResults.setString(row, "plural", sdcProcessor.getProperty(sdcid, "plural").toLowerCase());
                SDIList sdiList = sdiListMap.get(sdcid);
                if (sdiList != null) {
                    for (int j = 0; j < task.size(); ++j) {
                        int index = sdiList.getListIndex(task.getValue(j, "itemkeyid1"), task.getValue(j, "itemkeyid2"), task.getValue(j, "itemkeyid3"));
                        if (index != -1) continue;
                        --count;
                    }
                }
            }
            searchResults.setNumber(row, "count", count);
            searchResults.setString(row, "taskdefdesc", task.getValue(0, "taskdefdesc"));
            searchResults.setString(row, "icon", task.getValue(0, "icon"));
            searchResults.setString(row, "longtitle", task.getValue(0, "longtitle"));
            searchResults.setString(row, "shorttitle", task.getValue(0, "shorttitle"));
            searchResults.setString(row, "scopeflag", task.getValue(0, "scopeflag"));
            searchResults.setDate(row, "createdt", task.getValue(0, "createdt"));
            searchResults.setDate(row, "duedt", task.getValue(0, "duedt"));
            searchResults.setString(row, "taskexecid", task.getValue(0, "taskexecid"));
            searchResults.setString(row, "taskexecdesc", task.getValue(0, "taskexecdesc"));
            searchResults.setString(row, "execcreateby", task.getValue(0, "execcreateby"));
            searchResults.setString(row, "execmodby", task.getValue(0, "execmodby"));
            searchResults.setString(row, "startdt", task.getValue(0, "startdt"));
            searchResults.setString(row, "execmoddt", task.getValue(0, "execmoddt"));
            searchResults.setString(row, "completedt", task.getValue(0, "completedt"));
            searchResults.setString(row, "summary", task.getValue(0, "summary"));
            searchResults.setString(row, "execstatus", task.getValue(0, "execstatus"));
            searchResults.setString(row, "execstatus_text", task.getValue(0, "execstatus_text"));
            searchResults.setString(row, "notes", task.getValue(0, "notes"));
            searchResults.setString(row, "starttaskflag", task.getValue(0, "starttaskflag"));
            searchResults.setString(row, "assignedanalyst", task.getValue(0, "assignedanalyst"));
            if (groupby.length() <= 0 || !groupby.endsWith("dt")) continue;
            WorkflowManagerRequest.setDateGroupText(groupby, searchResults, i, dtu, trans);
        }
        if (groupby.length() > 0 || sortby.length() > 0) {
            if (groupby.length() > 0) {
                searchResults.sort((dateGrouping ? "datesort " + (groupbysortdir.length() > 0 ? groupbysortdir : "D") : (groupby.equalsIgnoreCase("workflowdefid") ? "workflowdefid " + groupbysortdir + ",workflowdefversionid " + groupbysortdir + ",workflowdefvariantid " + groupbysortdir : (groupby.equalsIgnoreCase("workflowexecid") ? "workflowexecname" + groupbysortdir + ",workflowdefid " + groupbysortdir + ",workflowdefversionid " + groupbysortdir + ",workflowdefvariantid " : groupby + " " + groupbysortdir))) + (sortby.length() > 0 ? "," + sortby + " " + sortbydir.substring(0, 1) : ""));
            } else {
                searchResults.sort(sortby + " " + sortbydir.substring(0, 1));
            }
        }
    }

    private HashMap<String, SDIList> getPermittedSDIs(DataSet data) {
        HashMap<String, SDIList> sdiListMap = new HashMap<String, SDIList>();
        for (int i = 0; i < data.size(); ++i) {
            String sdcid = data.getValue(i, "itemsdcid");
            if (sdcid.length() <= 0 || data.getValue(i, "taskqueueid").length() <= 0) continue;
            SDIList sdiList = sdiListMap.get(sdcid);
            if (sdiList == null) {
                sdiList = new SDIList();
                sdiList.setAllowDups(true);
                sdiList.setSdcid(sdcid);
                sdiListMap.put(sdcid, sdiList);
            }
            sdiList.addSDI(data.getValue(i, "itemkeyid1"), data.getValue(i, "itemkeyid2"), data.getValue(i, "itemkeyid3"));
        }
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        SDIProcessor sdiProcessor = this.getSDIProcessor();
        for (String sdcid : sdiListMap.keySet()) {
            String accesscontrol = sdcProcessor.getProperty(sdcid, "accesscontrolledflag");
            if (accesscontrol.length() <= 0 || accesscontrol.equals("N") && !sdcid.equals("SDIWorkItem")) continue;
            SDIList sdiList = sdiListMap.get(sdcid);
            WorkflowManager.applySecurity(sdiProcessor, sdiList);
        }
        return sdiListMap;
    }

    private void startTask(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) {
        block5: {
            try {
                TaskContext taskContext = commandRequest.getTaskContext("taskcontext");
                String workflowexecid = taskContext.getWorkflowexecid();
                if (workflowexecid.length() == 0 && taskContext.getWorkflowdefid().length() > 0 && taskContext.getWorkflowdefversionid().length() > 0 && taskContext.getWorkflowdefvariantid().length() > 0) {
                    workflowexecid = WorkflowManager.createNewWorkflowExec(this.sapphireConnection, dbu, taskContext.getWorkflowdefid(), taskContext.getWorkflowdefversionid(), taskContext.getWorkflowdefvariantid(), "", "", null, "", "", false);
                }
                if (workflowexecid.length() > 0) {
                    PropertyList actionProps = new PropertyList();
                    actionProps.setProperty("taskdefid", commandRequest.getString("taskdefid"));
                    actionProps.setProperty("taskdefversionid", commandRequest.getString("taskdefversionid", "1"));
                    actionProps.setProperty("taskdefvariantid", commandRequest.getString("taskdefvariantid", "1"));
                    actionProps.setProperty("workflowexecid", workflowexecid);
                    actionProps.setProperty("taskdefitemid", taskContext.getTaskdefitemid());
                    actionProps.setProperty("trainingoverriddenflag", commandRequest.getBoolean("trainingoverridden") ? "Y" : "N");
                    this.getActionProcessor().processActionClass(StartTaskExec.class.getName(), actionProps);
                    commandResponse.set("taskexecid", actionProps.getProperty("taskexecid"));
                    commandResponse.set("workflowexecid", workflowexecid);
                    if (commandRequest.contains("sdilist")) {
                        commandRequest.set("taskexecid", actionProps.getProperty("taskexecid"));
                        this.allocateTaskQueueItems(commandRequest, commandResponse);
                    }
                    break block5;
                }
                throw new SapphireException("No workflowexecid");
            }
            catch (Exception e) {
                commandResponse.setStatusFail("Failed to start task execution. Reason: " + e.getMessage(), e);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void getCertifiedUsers(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) {
        String rsetid = null;
        String taskqueueid = commandRequest.getString("taskqueueid");
        String taskexecid = commandRequest.getString("taskexecid");
        String taskdefid = commandRequest.getString("taskdefid");
        String taskdefversionid = commandRequest.getString("taskdefversionid");
        String taskdefvariantid = commandRequest.getString("taskdefvariantid");
        String departmentid = commandRequest.getString("departmentid");
        try {
            DataSet ds;
            String s;
            SafeSQL safeSQL;
            if (taskqueueid.length() > 0) {
                safeSQL = new SafeSQL();
                s = "SELECT taskdefid, taskdefversionid, taskdefvariantid, assigneddepartment FROM taskqueue WHERE taskqueueid IN (" + safeSQL.addIn(taskqueueid, ";") + ")";
                ds = this.getQueryProcessor().getPreparedSqlDataSet(s, safeSQL.getValues());
                if (ds.size() <= 0) throw new SapphireException("Failed to find taskqueue entry '" + taskqueueid + "'");
                taskdefid = ds.getValue(0, "taskdefid");
                taskdefversionid = ds.getValue(0, "taskdefversionid");
                taskdefvariantid = ds.getValue(0, "taskdefvariantid");
                departmentid = ds.getValue(0, "assigneddepartment");
                for (int i = 1; i < ds.size(); ++i) {
                    if (ds.getValue(i, "taskdefid").equals(taskdefid) && ds.getValue(i, "taskdefversionid").equals(taskdefversionid) && ds.getValue(i, "taskdefvariantid").equals(taskdefvariantid) && ds.getValue(i, "departmentid").equals(departmentid)) continue;
                    throw new SapphireException("Selected task queue items cannot be assigned together");
                }
            } else if (taskexecid.length() > 0) {
                safeSQL = new SafeSQL();
                s = "SELECT taskdefid, taskdefversionid, taskdefvariantid, assigneddepartment FROM taskexec WHERE taskexecid IN (" + safeSQL.addIn(taskexecid, ";") + ")";
                ds = this.getQueryProcessor().getPreparedSqlDataSet(s, safeSQL.getValues());
                if (ds.size() <= 0) throw new SapphireException("Failed to find taskexec entry '" + taskqueueid + "'");
                taskdefid = ds.getValue(0, "taskdefid");
                taskdefversionid = ds.getValue(0, "taskdefversionid");
                taskdefvariantid = ds.getValue(0, "taskdefvariantid");
                departmentid = ds.getValue(0, "assigneddepartment");
                for (int i = 1; i < ds.size(); ++i) {
                    if (ds.getValue(i, "taskdefid").equals(taskdefid) && ds.getValue(i, "taskdefversionid").equals(taskdefversionid) && ds.getValue(i, "taskdefvariantid").equals(taskdefvariantid) && ds.getValue(i, "departmentid").equals(departmentid)) continue;
                    throw new SapphireException("Selected task executions cannot be assigned together");
                }
            }
            if (taskdefid.length() <= 0 || taskdefversionid.length() <= 0 || taskdefvariantid.length() <= 0) throw new SapphireException("TaskDef not defined to get list of certified users");
            String callstmt = "{call LV_List" + (this.connectionInfo.isOracle() ? "." : "_") + "TaskDefUsersCert  ( ?, ?, ?, ?, ?, ?, ?, ? ) }";
            CallableStatement cs = dbu.prepareCall(callstmt);
            cs.registerOutParameter(1, 12);
            cs.setString(2, this.sapphireConnection.getConnectionId());
            cs.setString(3, this.sapphireConnection.getSysuserId());
            cs.setString(4, departmentid);
            cs.setString(5, taskdefid);
            cs.setString(6, taskdefversionid);
            cs.setString(7, taskdefvariantid);
            cs.setString(8, ";");
            cs.execute();
            rsetid = cs.getString(1);
            ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT keyid1 FROM rsetitems WHERE rsetid = ?", new Object[]{rsetid});
            commandResponse.set("certifiedusers", ds.getColumnValues("keyid1", ";"));
            commandResponse.set("taskdefid", taskdefid);
            commandResponse.set("taskdefversionid", taskdefversionid);
            commandResponse.set("taskdefvariantid", taskdefvariantid);
            commandResponse.set("departmentid", departmentid);
            return;
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to get list of certified users. Reason: " + e.getMessage(), e);
            return;
        }
        finally {
            if (rsetid != null && rsetid.length() > 0) {
                this.getDAMProcessor().clearRSet(rsetid);
            }
        }
    }

    private void stepRequest(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) {
        String step = commandRequest.getString("step");
        try {
            Class<?> c = Class.forName(this.getClass().getPackage().getName() + ".steprequests." + step);
            BaseStepRequest stepRequest = (BaseStepRequest)c.newInstance();
            stepRequest.init(this.sapphireConnection, dbu);
            stepRequest.executeRequest(commandRequest, commandResponse);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to process step request '" + step + "'. Reason: " + e.getMessage(), e);
        }
    }

    public static void setDateGroupText(String groupby, DataSet searchResults, int searchRow, DateTimeUtil dtu, TranslationProcessor trans) {
        Calendar dbdate = searchResults.getCalendar(searchRow, groupby);
        searchResults.setString(searchRow, "datesort", dbdate != null ? String.valueOf(dbdate.getTimeInMillis()) : "");
        Calendar date = dtu.getCalendar("today");
        int dow = date.get(7) - 1;
        String datetext = null;
        int index = 0;
        while (index < datetexts.length && datetext == null) {
            if (dateoffsets[index].equals("-1d")) {
                date.add(6, -1);
            } else if (dateoffsets[index].equals("-7d")) {
                date.add(6, -7);
            } else if (dateoffsets[index].equals("-1m")) {
                date.add(2, -1);
            }
            if (dbdate == null) {
                datetext = trans.translate("N/A");
            } else if (dbdate.after(date)) {
                String string = datetext = datetexts[index].equals("") ? trans.translate(days[date.get(7) - 1]) : trans.translate(datetexts[index]);
            }
            if (index >= dow && index < 7) {
                index += 7 - dow;
                continue;
            }
            ++index;
        }
        if (datetext == null) {
            datetext = trans.translate("Older");
        }
        searchResults.setString(searchRow, groupby + "_datetext", datetext);
    }

    public static void getDistinctValues(DataSet data, StringBuffer workflowexecs, StringBuffer workflowdefs, StringBuffer workflowdefversions, StringBuffer workflowdefvariants, StringBuffer taskdefitems, StringBuffer ioids, StringBuffer taskqueueids) {
        for (int i = 0; i < data.size(); ++i) {
            String taskqueueid;
            String ioid;
            String taskdefitemid;
            String workflowdefvariantid;
            String workflowdefversionid;
            String workflowdefid;
            String workflowexecid = data.getValue(i, "workflowexecid");
            if (workflowexecs.indexOf(workflowexecid) == -1) {
                workflowexecs.append(workflowexecs.length() > 0 ? ";" : "").append(workflowexecid);
            }
            if (workflowdefs.indexOf(workflowdefid = data.getValue(i, "workflowdefid")) == -1) {
                workflowdefs.append(workflowdefs.length() > 0 ? ";" : "").append(workflowdefid);
            }
            if (workflowdefversions.indexOf(workflowdefversionid = data.getValue(i, "workflowdefversionid")) == -1) {
                workflowdefversions.append(workflowdefversions.length() > 0 ? ";" : "").append(workflowdefversionid);
            }
            if (workflowdefvariants.indexOf(workflowdefvariantid = data.getValue(i, "workflowdefvariantid")) == -1) {
                workflowdefvariants.append(workflowdefvariants.length() > 0 ? ";" : "").append(workflowdefvariantid);
            }
            if (taskdefitems.indexOf(taskdefitemid = data.getValue(i, "taskdefitemid")) == -1) {
                taskdefitems.append(taskdefitems.length() > 0 ? ";" : "").append(taskdefitemid);
            }
            if (ioids.indexOf(ioid = data.getValue(i, "ioid")) == -1) {
                ioids.append(ioids.length() > 0 ? ";" : "").append(ioid);
            }
            if (taskqueueids.indexOf(taskqueueid = data.getValue(i, "taskqueueid")) != -1) continue;
            taskqueueids.append(taskqueueids.length() > 0 ? ";" : "").append(taskqueueid);
        }
    }

    public static String getDistinctValues(DataSet data, String columnid) {
        StringBuffer values = new StringBuffer();
        ArrayList<String> valuelist = new ArrayList<String>();
        for (int i = 0; i < data.size(); ++i) {
            String value = data.getValue(i, columnid, data.getValue(i, "wftask_" + columnid));
            if (valuelist.contains(value)) continue;
            valuelist.add(value);
            values.append(";").append(value);
        }
        return values.length() > 0 ? values.substring(1) : "";
    }

    public static String evalTokens(SapphireConnection sapphireConnection, String value) {
        String newValue = value;
        String[] tokens = StringUtil.getTokens(value);
        if (tokens != null && tokens.length > 0) {
            M18NUtil m18n = new M18NUtil(sapphireConnection);
            for (int i = 0; i < tokens.length; ++i) {
                if (tokens[i].equalsIgnoreCase("currentuser")) {
                    newValue = StringUtil.replaceAll(newValue, "[currentuser]", sapphireConnection.getSysuserId());
                    continue;
                }
                if (tokens[i].equalsIgnoreCase("currentdatetime") || tokens[i].equalsIgnoreCase("now")) {
                    newValue = StringUtil.replaceAll(newValue, "[currentdatetime]", m18n.format(m18n.getNowCalendar()));
                    continue;
                }
                if (tokens[i].equalsIgnoreCase("currentdate")) {
                    newValue = StringUtil.replaceAll(newValue, "[currentdate]", m18n.formatDateOnly(m18n.getNowCalendar()));
                    continue;
                }
                if (!tokens[i].contains("+") && !tokens[i].contains("-")) continue;
                DateTimeUtil dtu = new DateTimeUtil(sapphireConnection);
                Calendar cal = dtu.getCalendar(tokens[i]);
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy hh:mm:ss aa");
                newValue = StringUtil.replaceAll(newValue, "[" + tokens[i] + "]", sapphireConnection.isOracle() ? "TO_DATE('" + sdf.format(cal.getTime()) + "', 'mm/dd/yy hh:mi:ss am')" : "CONVERT(DATETIME,'" + sdf.format(cal.getTime()) + "')");
            }
        }
        return newValue;
    }
}

