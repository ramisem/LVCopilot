/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package sapphire.accessor;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.BaseAccessor;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandRequest;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandResponse;
import com.labvantage.sapphire.pageelements.gwt.shared.CommandConstants;
import com.labvantage.sapphire.pageelements.gwt.shared.WorkflowManagerConstants;
import com.labvantage.sapphire.servlet.RequestProcessor;
import java.io.File;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.SDIList;
import sapphire.util.TaskContext;

public class WorkflowProcessor
extends BaseAccessor
implements WorkflowManagerConstants {
    public static final String QUEUEITEMS_ALL = "W;Wevent;Wtimer;E;S;A;Aevent;Atimer";
    public static final String QUEUEITEMS_AVAILABLE = "W;S";
    public static final String QUEUEITEMS_WAITING = "Wevent;Aevent;Wtimer;Atimer";
    public static final String QUEUEITEMS_ACTIVE = "E;A";
    private ConnectionInfo connectionInfo;
    private RequestProcessor requestProcessor;

    public WorkflowProcessor(String connectionid) {
        super(connectionid);
        ConnectionProcessor conn = new ConnectionProcessor(connectionid);
        this.connectionInfo = conn.getConnectionInfo(connectionid);
        this.requestProcessor = new RequestProcessor(connectionid);
    }

    public WorkflowProcessor(File rakFile, String connectionid) {
        super(rakFile, connectionid);
        ConnectionProcessor conn = new ConnectionProcessor(rakFile, connectionid);
        this.connectionInfo = conn.getConnectionInfo(connectionid);
        this.requestProcessor = new RequestProcessor(connectionid);
    }

    public WorkflowProcessor(PageContext pageContext) {
        super(pageContext);
        ConnectionProcessor conn = new ConnectionProcessor(pageContext);
        this.connectionInfo = conn.getConnectionInfo(this.getConnectionid());
        this.requestProcessor = new RequestProcessor(this.connectionInfo.getConnectionId());
    }

    public SDIList getTaskQueueItems(TaskContext taskContext, String ioid) throws SapphireException {
        return this.getTaskQueueItems(taskContext, ioid, QUEUEITEMS_AVAILABLE);
    }

    public SDIList getTaskQueueItems(TaskContext taskContext, String ioid, String queueStatuses) throws SapphireException {
        try {
            CommandRequest commandRequest = new CommandRequest("lqi");
            commandRequest.set("taskdefid", taskContext.getTaskdefid());
            commandRequest.set("taskdefversionid", taskContext.getTaskdefversionid());
            commandRequest.set("taskdefvariantid", taskContext.getTaskdefvariantid());
            commandRequest.set("ioid", ioid);
            commandRequest.set("workflowexecid", taskContext.getWorkflowexecid());
            commandRequest.set("workflowdefid", taskContext.getWorkflowdefid());
            commandRequest.set("workflowdefversionid", taskContext.getWorkflowdefversionid());
            commandRequest.set("workflowdefvariantid", taskContext.getWorkflowdefvariantid());
            commandRequest.set("taskdefitemid", taskContext.getTaskdefitemid());
            commandRequest.set("taskexecgroup", taskContext.getTaskexecgroup());
            commandRequest.set("applyuserrestrictions", true);
            commandRequest.set("testmode", taskContext.isTestMode() ? "Y" : "N");
            commandRequest.set("standalonemode", taskContext.isStandaloneMode() ? "Y" : "N");
            commandRequest.set("queuestatuses", queueStatuses);
            CommandResponse commandResponse = new CommandResponse();
            HashMap<String, CommandConstants> requestMap = new HashMap<String, CommandConstants>();
            requestMap.put("commandrequest", commandRequest);
            requestMap.put("commandresponse", commandResponse);
            HashMap returnMap = this.requestProcessor.processRequest("com.labvantage.sapphire.modules.workflow.gwt.server.WorkflowManagerRequest", requestMap);
            commandResponse = (CommandResponse)returnMap.get("commandresponse");
            if (commandResponse.getStatus().equals("fail")) {
                throw new SapphireException(commandResponse.getStatusMessage());
            }
            return (SDIList)commandResponse.get(ioid);
        }
        catch (Exception e) {
            throw new SapphireException("Unexpected failure getting task queue items. Reason:" + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
        }
    }

    public DataSet getTaskQueueItemsDataSet(TaskContext taskContext, String ioid) throws SapphireException {
        return this.getTaskQueueItemsDataSet(taskContext, ioid, QUEUEITEMS_AVAILABLE);
    }

    public DataSet getTaskQueueItemsDataSet(TaskContext taskContext, String ioid, String queueStatuses) throws SapphireException {
        SDIList sdiList = this.getTaskQueueItems(taskContext, ioid, queueStatuses);
        DataSet taskQueueData = new DataSet(this.connectionInfo);
        taskQueueData.addColumn("taskqueueid", 0);
        taskQueueData.addColumn("sdcid", 0);
        taskQueueData.addColumn("keyid1", 0);
        taskQueueData.addColumn("keyid2", 0);
        taskQueueData.addColumn("keyid3", 0);
        for (int i = 0; i < sdiList.size(); ++i) {
            taskQueueData.addRow();
            taskQueueData.setValue(i, "taskqueueid", sdiList.getSDIAttribute(i, "taskqueueid"));
            taskQueueData.setValue(i, "sdcid", sdiList.getSdcid());
            taskQueueData.setValue(i, "keyid1", sdiList.getKeyid1(i));
            taskQueueData.setValue(i, "keyid2", sdiList.getKeyid2(i));
            taskQueueData.setValue(i, "keyid3", sdiList.getKeyid3(i));
        }
        return taskQueueData;
    }

    public void allocateTaskQueueItems(String taskexecid, SDIList queueItems) throws SapphireException {
        this.allocateTaskQueueItems(taskexecid, queueItems, false, false);
    }

    public void allocateTaskQueueItems(String taskexecid, SDIList queueItems, boolean sharedQueue, boolean testMode) throws SapphireException {
        try {
            CommandRequest commandRequest = new CommandRequest("atqi");
            commandRequest.set("taskexecid", taskexecid);
            commandRequest.set("sdilist", queueItems);
            commandRequest.set("queueusage", sharedQueue ? "S" : "E");
            commandRequest.set("testmode", testMode);
            CommandResponse commandResponse = new CommandResponse();
            HashMap<String, CommandConstants> requestMap = new HashMap<String, CommandConstants>();
            requestMap.put("commandrequest", commandRequest);
            requestMap.put("commandresponse", commandResponse);
            HashMap returnMap = this.requestProcessor.processRequest("com.labvantage.sapphire.modules.workflow.gwt.server.WorkflowManagerRequest", requestMap);
            commandResponse = (CommandResponse)returnMap.get("commandresponse");
            if (commandResponse.getStatus().equals("fail")) {
                throw new SapphireException(commandResponse.getStatusMessage());
            }
        }
        catch (Exception e) {
            throw new SapphireException("Unexpected failure getting task queue items. Reason:" + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
        }
    }

    public void deleteTaskQueueItems(TaskContext taskContext, String ioid) throws SapphireException {
        try {
            CommandRequest commandRequest = new CommandRequest("dqi");
            commandRequest.set("taskdefid", taskContext.getTaskdefid());
            commandRequest.set("taskdefversionid", taskContext.getTaskdefversionid());
            commandRequest.set("taskdefvariantid", taskContext.getTaskdefvariantid());
            commandRequest.set("workflowexecid", taskContext.getWorkflowexecid());
            commandRequest.set("workflowdefid", taskContext.getWorkflowdefid());
            commandRequest.set("workflowdefversionid", taskContext.getWorkflowdefversionid());
            commandRequest.set("workflowdefvariantid", taskContext.getWorkflowdefvariantid());
            commandRequest.set("taskdefitemid", taskContext.getTaskdefitemid());
            commandRequest.set("taskexecgroup", taskContext.getTaskexecgroup());
            commandRequest.set("ioid", ioid);
            CommandResponse commandResponse = new CommandResponse();
            HashMap<String, CommandConstants> requestMap = new HashMap<String, CommandConstants>();
            requestMap.put("commandrequest", commandRequest);
            requestMap.put("commandresponse", commandResponse);
            HashMap returnMap = this.requestProcessor.processRequest("com.labvantage.sapphire.modules.workflow.gwt.server.WorkflowManagerRequest", requestMap);
            commandResponse = (CommandResponse)returnMap.get("commandresponse");
            if (commandResponse.getStatus().equals("fail")) {
                throw new SapphireException(commandResponse.getStatusMessage());
            }
        }
        catch (Exception e) {
            throw new SapphireException("Unexpected failure getting task queue items. Reason:" + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
        }
    }

    public DataSet getSDIWorkflowData(String sdcid, String keyid1, String keyid2, String keyid3) throws SapphireException {
        try {
            CommandRequest commandRequest = new CommandRequest("lsdiwd");
            commandRequest.set("sdcid", sdcid);
            commandRequest.set("keyid1", keyid1);
            commandRequest.set("keyid2", keyid2);
            commandRequest.set("keyid3", keyid3);
            CommandResponse commandResponse = new CommandResponse();
            HashMap<String, CommandConstants> requestMap = new HashMap<String, CommandConstants>();
            requestMap.put("commandrequest", commandRequest);
            requestMap.put("commandresponse", commandResponse);
            HashMap returnMap = this.requestProcessor.processRequest("com.labvantage.sapphire.modules.workflow.gwt.server.WorkflowManagerRequest", requestMap);
            commandResponse = (CommandResponse)returnMap.get("commandresponse");
            if (commandResponse.getStatus().equals("fail")) {
                throw new SapphireException(commandResponse.getStatusMessage());
            }
            return (DataSet)commandResponse.get("data");
        }
        catch (Exception e) {
            throw new SapphireException("Unexpected failure getting SDI workflow data. Reason:" + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
        }
    }
}

