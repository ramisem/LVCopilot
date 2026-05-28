/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.messaging;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.messaging.MessageLogUtil;
import com.labvantage.sapphire.services.ActionService;
import com.labvantage.sapphire.services.AutomationService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.QueryProcessor;
import sapphire.action.BaseAction;
import sapphire.error.ErrorDetail;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class ProcessInMessage
extends BaseAction
implements sapphire.action.ProcessInMessage {
    public static final String PROPERTY_MESSAGELOGID = "messagelogid";
    private StringBuffer processLog;
    private StringBuffer validationLog;

    @Override
    public void processAction(PropertyList propertyList) throws SapphireException {
        String processedBy;
        String tagPropertyId;
        String msgLogId = propertyList.getProperty(PROPERTY_MESSAGELOGID, "");
        String messageTypeId = propertyList.getProperty("messagetypeid", "");
        String message = propertyList.getProperty("message", "");
        String messageTag = propertyList.getProperty("messagetag", "");
        if (messageTag.length() == 0 && (tagPropertyId = propertyList.getProperty("tagpropertyid", "")).length() > 0) {
            String tagValue = propertyList.getProperty(tagPropertyId, "");
            propertyList.setProperty("messagetag", tagValue);
        }
        if ((processedBy = propertyList.getProperty("processedby", "")).length() == 0) {
            processedBy = this.connectionInfo.getSysuserId();
            propertyList.setProperty("processedby", processedBy);
        }
        if (processedBy.equalsIgnoreCase("(system)")) {
            propertyList.setProperty("processedby", "");
        }
        this.processLog = new StringBuffer();
        if (msgLogId.length() == 0) {
            DataSet messageTypeInfo = ProcessInMessage.getMessageTypeDetails(this.getQueryProcessor(), messageTypeId);
            String processActionMode = propertyList.getProperty("processactionmode", "");
            if (processActionMode.length() == 0) {
                this.processLog.append("ProcessActionMode is not specified, assumed to be S\n");
                processActionMode = messageTypeInfo.getString(0, "processactionflag", "S");
            }
            if ("S".equals(processActionMode)) {
                this.processLog.append("Processing message Synchronously.\n");
                this.processMessageSync(propertyList, messageTypeInfo);
            } else if ("A".equals(processActionMode)) {
                this.processLog.append("Creating message log entry to be processed Asynchronously.\n");
                this.handleAsyncMessage(propertyList, messageTypeInfo);
            } else if ("M".equals(processActionMode)) {
                this.processLog.append("Creating message log entry to be manually processed.\n");
                this.handleManualMessage(propertyList, messageTypeInfo);
            }
        } else {
            String processStatus = MessageLogUtil.getProcessStatus(this.getQueryProcessor(), msgLogId);
            this.processLog.append("<P>Processing file corresponding to " + msgLogId + "<P>");
            if ("NOT STARTED".equals(processStatus)) {
                this.processManualAsyncMessage(msgLogId, propertyList, "Message processed successfully.\n");
            } else if ("VALIDATED".equals(processStatus)) {
                this.processValidatedMessage(msgLogId, propertyList, "Message processed successfully.\n");
            } else if ("WAITING".equals(processStatus)) {
                this.processManualAsyncMessage(msgLogId, propertyList, "Message processed successfully.");
            } else {
                this.processLog.append("Reprocessing message.\n");
                this.reprocessMessage(msgLogId, propertyList);
            }
        }
    }

    public static DataSet getMessageTypeDetails(QueryProcessor qp, String messageTypeId) throws ActionException {
        String sql = "SELECT directionflag, processactionid, processactionversionid, processactionflag, sendactionid, sendactionversionid, sendactionflag, allowreprocessflag, allowresendflag, messageclass, allowlogflag FROM messagetype WHERE messagetypeid=?";
        DataSet result = qp.getPreparedSqlDataSet(sql, new Object[]{messageTypeId});
        if (result == null || result.getRowCount() == 0) {
            throw new ActionException("Message Type details not found for messagetypeid: " + messageTypeId);
        }
        return result;
    }

    private void processMessageSync(PropertyList propertyList, DataSet messageTypeInfo) {
        String allowLogFlag = messageTypeInfo.getString(0, "allowlogflag");
        String msgLogId = "";
        if ("Y".equals(allowLogFlag)) {
            msgLogId = MessageLogUtil.addMsgLogEntry(this.getConnectionid(), this.getActionProcessor(), propertyList, messageTypeInfo.getString(0, "messageclass", ""), "IN");
            propertyList.setProperty(PROPERTY_MESSAGELOGID, msgLogId);
        }
        String processedBy = propertyList.getProperty("processedby");
        String processAction = messageTypeInfo.getString(0, "processactionid", "ProcessSECMessage");
        String processActionVersion = messageTypeInfo.getString(0, "processactionversionid", "1");
        try {
            this.processLog.append("Calling the processAction: ").append(processAction).append("\n");
            boolean processNoTransaction = "Y".equals(propertyList.getProperty("processnotransaction"));
            if (!processNoTransaction) {
                this.getActionProcessor().processAction(processAction, processActionVersion, propertyList, true);
            } else {
                SapphireConnection sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
                ActionService actionService = new ActionService(sapphireConnection);
                actionService.processAction(processAction, processActionVersion, propertyList);
            }
            if (propertyList.getProperty("log", "").length() > 0) {
                this.processLog.append(propertyList.getProperty("log"));
            }
            if ("FAILED".equals(propertyList.getProperty("status", "SUCCESS"))) {
                throw new ActionException(propertyList.getProperty("error", "Action not processed successfully"));
            }
        }
        catch (ActionException e) {
            this.processLog.append("ERROR: processAction failed: " + e.getMessage() + "\n");
            this.handleProcessActionException(e, msgLogId, allowLogFlag, messageTypeInfo, propertyList);
            return;
        }
        catch (ServiceException e) {
            this.processLog.append("ERROR: processAction failed: ").append(e.getMessage()).append("\n");
            MessageLogUtil.setProcessInfo(this.getActionProcessor(), msgLogId, "ERROR", e.getMessage(), propertyList.getProperty("processedby"), this.processLog.toString(), propertyList.getProperty("validationlog", ""));
            propertyList.setProperty("status", "FAILED");
            propertyList.setProperty("error", e.getMessage());
            propertyList.setProperty("log", this.processLog.toString());
            return;
        }
        String processStatus = propertyList.getProperty("status", "SUCCESS");
        String error = propertyList.getProperty("error", "");
        String validationLog = propertyList.getProperty("validationlog", "");
        String responseMessage = propertyList.getProperty("responsemessage", "");
        this.processLog.append("processAction completed.\n");
        if (responseMessage.length() > 0) {
            this.processLog.append("ProcessAction responseMessage returned is: \n");
            this.processLog.append(responseMessage);
        } else {
            this.processLog.append("No responseMessage created by processAction.\n");
        }
        if (processStatus.length() > 0 && processStatus.equals("FAILED")) {
            MessageLogUtil.setProcessInfo(this.getActionProcessor(), msgLogId, "ERROR", error, processedBy, this.processLog.toString(), validationLog);
        } else if (propertyList.getProperty("validateonly", "N").equals("Y")) {
            MessageLogUtil.setProcessInfo(this.getActionProcessor(), msgLogId, "VALIDATED", "Incoming message validated successfully", processedBy, this.processLog.toString(), validationLog);
        } else {
            MessageLogUtil.setProcessInfo(this.getActionProcessor(), msgLogId, "COMPLETE", "Incoming message processed successfully", processedBy, this.processLog.toString(), validationLog);
        }
        if (responseMessage.length() != 0) {
            MessageLogUtil.setSendStatus(this.getActionProcessor(), msgLogId, "COMPLETE", "Response returned", responseMessage, this.processLog.toString());
        }
        propertyList.setProperty("status", processStatus);
        propertyList.setProperty("log", this.processLog.toString());
    }

    private void handleAsyncMessage(PropertyList propertyList, DataSet messageTypeInfo) {
        String allowLogFlag = messageTypeInfo.getString(0, "allowlogflag", "Y");
        if (!"Y".equals(allowLogFlag)) {
            allowLogFlag = "Y";
            Trace.log("Ignoring setting for allowlogflag for asynchronous message processing request");
        }
        String msgLogId = MessageLogUtil.addMsgLogEntry(this.getConnectionid(), this.getActionProcessor(), propertyList, messageTypeInfo.getString(0, "messageclass", ""), "IN");
        propertyList.setProperty(PROPERTY_MESSAGELOGID, msgLogId);
        this.processLog.append("Creating messagelog entry\n");
        MessageLogUtil.changeProcessStatus(this.getActionProcessor(), msgLogId, "WAITING", "Added to todo list", this.processLog.toString());
        PropertyList asyncProps = new PropertyList();
        asyncProps.setProperty(PROPERTY_MESSAGELOGID, msgLogId);
        asyncProps.setProperty("processedby", propertyList.getProperty("processedby"));
        asyncProps.setProperty("processassysuserid", propertyList.getProperty("processedby"));
        AutomationService ac = new AutomationService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
        try {
            ac.addToDoListEntry(null, "ProcessInMessage", "1", asyncProps, null, true, propertyList.getProperty("processedby", ""), "", "", "");
            this.processLog.append("Added asynchronous message to todo list.\n");
            propertyList.setProperty("status", "SUCCESS");
            propertyList.setProperty("log", this.processLog.toString());
        }
        catch (ServiceException e) {
            this.processLog.append("Failed to add message to todo list: ");
            this.processLog.append(e.getMessage());
            this.processLog.append("\n");
            MessageLogUtil.changeProcessStatus(this.getActionProcessor(), msgLogId, "ERROR", e.getMessage(), this.processLog.toString());
            propertyList.setProperty("status", "FAILED");
            propertyList.setProperty("error", e.getMessage());
            propertyList.setProperty("log", this.processLog.toString());
        }
    }

    private void handleManualMessage(PropertyList propertyList, DataSet messageTypeInfo) {
        String allowLogFlag = messageTypeInfo.getString(0, "allowlogflag");
        if (!"Y".equals(allowLogFlag)) {
            allowLogFlag = "Y";
            Trace.log("Ignoring setting for allowlogflag for manual message processing request");
        }
        String msgLogId = MessageLogUtil.addMsgLogEntry(this.getConnectionid(), this.getActionProcessor(), propertyList, messageTypeInfo.getString(0, "messageclass", ""), "IN");
        propertyList.setProperty(PROPERTY_MESSAGELOGID, msgLogId);
        this.processLog.append("Created a log entry for manual message. Ready for processing.\n");
        MessageLogUtil.changeProcessStatus(this.getActionProcessor(), msgLogId, "NOT STARTED", "To be executed manually", this.processLog.toString());
        propertyList.setProperty("status", "SUCCESS");
        propertyList.setProperty("log", this.processLog.toString());
    }

    private PropertyList loadOldPropertyList(String msgLogId, PropertyList propertyList) throws SapphireException {
        PropertyList oldProperties = MessageLogUtil.getPropertyList(this.getQueryProcessor(), msgLogId);
        Object[] keyes = oldProperties.keySet().toArray();
        for (int j = 0; j < keyes.length; ++j) {
            if (keyes[j].toString().equals("processactionmode") || keyes[j].toString().equals("processedby") && propertyList.getProperty("processedby", "").length() > 0) continue;
            propertyList.setProperty((String)keyes[j], oldProperties.getProperty((String)keyes[j]));
        }
        return propertyList;
    }

    private void reprocessMessage(String msgLogId, PropertyList propertyList) {
        DataSet messageTypeInfo = null;
        try {
            propertyList = this.loadOldPropertyList(msgLogId, propertyList);
            MessageLogUtil.updateReprocessInfo(this.getActionProcessor(), msgLogId, propertyList);
            String messageTypeId = propertyList.getProperty("messagetypeid");
            if (messageTypeId.length() == 0) {
                String sql = "SELECT * from messagelog WHERE messagelogid = ?";
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{msgLogId});
                messageTypeId = ds.getValue(0, "messagetypeid");
                propertyList.setProperty("messagetypeid", messageTypeId);
            }
            messageTypeInfo = ProcessInMessage.getMessageTypeDetails(this.getQueryProcessor(), messageTypeId);
            String processAction = messageTypeInfo.getString(0, "processactionid", "ProcessSECMessage");
            String processActionVersion = messageTypeInfo.getString(0, "processactionversionid", "1");
            this.getActionProcessor().processAction(processAction, processActionVersion, propertyList, true);
            if ("FAILED".equals(propertyList.getProperty("status", "SUCCESS"))) {
                throw new ActionException(propertyList.getProperty("error", "Action not processed successfully"));
            }
        }
        catch (ActionException e) {
            this.processLog.append("processAction failed: ");
            this.processLog.append(e.getMessage());
            this.processLog.append("\n");
            MessageLogUtil.changeProcessStatus(this.getActionProcessor(), msgLogId, "ERROR", e.getMessage(), this.processLog.toString());
            propertyList.setProperty("status", "FAILED");
            propertyList.setProperty("error", e.getMessage());
            propertyList.setProperty("log", this.processLog.toString());
            return;
        }
        catch (SapphireException e) {
            this.processLog.append("processAction failed: ");
            this.processLog.append(e.getMessage());
            this.processLog.append("\n");
            MessageLogUtil.changeProcessStatus(this.getActionProcessor(), msgLogId, "ERROR", e.getMessage(), this.processLog.toString());
            propertyList.setProperty("status", "FAILED");
            propertyList.setProperty("error", e.getMessage());
            propertyList.setProperty("log", this.processLog.toString());
            return;
        }
        if (propertyList.getProperty("log", "").length() > 0) {
            this.processLog.append("<P>");
            this.processLog.append("Reprocess log returned: <P>");
            this.processLog.append(propertyList.getProperty("log"));
        }
        if (propertyList.getProperty("validateonly", "N").equals("Y")) {
            MessageLogUtil.changeProcessStatus(this.getActionProcessor(), msgLogId, "COMPLETE", "Message processed successfully", this.processLog.toString());
        } else {
            MessageLogUtil.changeProcessStatus(this.getActionProcessor(), msgLogId, "VALIDATED", "Message validated successfully", this.processLog.toString());
        }
        String responseMessage = propertyList.getProperty("responsemessage", "");
        if (responseMessage.length() != 0) {
            String sendResponseAction = messageTypeInfo.getString(0, "sendactionid", "");
            String sendResponseActionVersion = messageTypeInfo.getString(0, "sendactionversionid", "1");
            String sendResponseActionMode = messageTypeInfo.getString(0, "sendactionflag", "A");
            if (sendResponseAction.length() != 0) {
                try {
                    if ("S".equals(sendResponseActionMode)) {
                        this.processLog.append("Sending response synchronously.\n");
                        this.getActionProcessor().processAction(sendResponseAction, sendResponseActionVersion, propertyList);
                        MessageLogUtil.setSendStatus(this.getActionProcessor(), msgLogId, "COMPLETE", "Response message sent successfully", responseMessage, this.processLog.toString());
                    } else {
                        this.processLog.append("Sending response asynchronously.\n");
                        AutomationService ac = new AutomationService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                        ac.addToDoListEntry(null, sendResponseAction, sendResponseActionVersion, propertyList, null, true, propertyList.getProperty("processedby", ""), "", "", "");
                        MessageLogUtil.setSendStatus(this.getActionProcessor(), msgLogId, "COMPLETE", "Send Response request on todo list", responseMessage, this.processLog.toString());
                    }
                    propertyList.setProperty("status", "SUCCESS");
                    propertyList.setProperty("log", this.processLog.toString());
                }
                catch (ActionException e) {
                    this.handleSendActionException(e, msgLogId, "Y", messageTypeInfo, propertyList);
                }
                catch (ServiceException e) {
                    this.processLog.append("Failed to send message. ").append(e.getMessage()).append("\n");
                    MessageLogUtil.setSendStatus(this.getActionProcessor(), msgLogId, "SENDERROR", e.getMessage(), responseMessage, this.processLog.toString());
                    propertyList.setProperty("status", "FAILED");
                    propertyList.setProperty("error", e.getMessage());
                    propertyList.setProperty("log", this.processLog.toString());
                }
            } else {
                Trace.log("No sendResponseAction specified");
                propertyList.setProperty("status", "SUCCESS");
                this.processLog.append("Reprocessed message successfully.\n");
                propertyList.setProperty("log", this.processLog.toString());
                MessageLogUtil.changeProcessStatus(this.getActionProcessor(), msgLogId, "COMPLETE", "Reprocessing completed", this.processLog.toString());
                MessageLogUtil.setSendStatus(this.getActionProcessor(), msgLogId, "UNSENT", "No send action specified", responseMessage, this.processLog.toString());
            }
        } else {
            propertyList.setProperty("status", "SUCCESS");
            propertyList.setProperty("log", this.processLog.toString());
            MessageLogUtil.changeProcessStatus(this.getActionProcessor(), msgLogId, "COMPLETE", "Reprocessing completed", this.processLog.toString());
        }
    }

    private void processManualAsyncMessage(String msgLogId, PropertyList propertyList, String successMessage) {
        DataSet messageTypeInfo = null;
        try {
            propertyList = this.loadOldPropertyList(msgLogId, propertyList);
            String messageTypeId = propertyList.getProperty("messagetypeid");
            messageTypeInfo = ProcessInMessage.getMessageTypeDetails(this.getQueryProcessor(), messageTypeId);
            String processAction = messageTypeInfo.getString(0, "processactionid", "ProcessSECMessage");
            String processActionVersion = messageTypeInfo.getString(0, "processactionversionid", "1");
            this.processLog.append("Calling processAction ").append(processAction).append("\n");
            MessageLogUtil.setProcessInfo(this.getActionProcessor(), msgLogId, "PROCESSING", "Processing message", propertyList.getProperty("processedby"), this.processLog.toString(), "");
            this.getActionProcessor().processAction(processAction, processActionVersion, propertyList, true);
        }
        catch (ActionException e) {
            this.processLog.append("ERROR: processAction failed.").append(e.getMessage()).append("\n");
            this.handleProcessActionException(e, msgLogId, "Y", messageTypeInfo, propertyList);
            return;
        }
        catch (SapphireException e) {
            this.processLog.append("ERROR: processAction failed.").append(e.getMessage()).append("\n");
            MessageLogUtil.setProcessInfo(this.getActionProcessor(), msgLogId, "ERROR", e.getMessage(), propertyList.getProperty("processedby"), this.processLog.toString(), propertyList.getProperty("validationlog", ""));
            propertyList.setProperty("status", "FAILED");
            propertyList.setProperty("error", e.getMessage());
            propertyList.setProperty("log", this.processLog.toString());
            return;
        }
        String actionstatus = propertyList.getProperty("status");
        if (actionstatus.equals("FAILED")) {
            if (propertyList.getProperty("validateonly", "N").equals("Y")) {
                MessageLogUtil.setProcessInfo(this.getActionProcessor(), msgLogId, "VALIDATED", successMessage, propertyList.getProperty("processedby"), this.processLog.toString(), propertyList.getProperty("validationlog", ""));
            } else {
                MessageLogUtil.setProcessInfo(this.getActionProcessor(), msgLogId, "COMPLETE", successMessage, propertyList.getProperty("processedby"), this.processLog.toString(), propertyList.getProperty("validationlog", ""));
            }
            return;
        }
        this.processLog.append("SUCCESS: processAction executed successfully.\n");
        if (propertyList.getProperty("log", "").length() > 0) {
            this.processLog.append(propertyList.getProperty("log"));
        }
        if (propertyList.getProperty("validateonly", "N").equals("Y")) {
            MessageLogUtil.setProcessInfo(this.getActionProcessor(), msgLogId, "VALIDATED", successMessage, propertyList.getProperty("processedby"), this.processLog.toString(), propertyList.getProperty("validationlog", ""));
        } else {
            MessageLogUtil.setProcessInfo(this.getActionProcessor(), msgLogId, "COMPLETE", successMessage, propertyList.getProperty("processedby"), this.processLog.toString(), propertyList.getProperty("validationlog", ""));
        }
        String responseMessage = propertyList.getProperty("responsemessage", "");
        if (responseMessage.length() > 0) {
            this.processLog.append("processAction returned responseMessage: \n");
            this.processLog.append(responseMessage);
        }
        if (responseMessage.length() != 0) {
            String sendResponseAction = messageTypeInfo.getString(0, "sendactionid", "");
            String sendResponseActionVersion = messageTypeInfo.getString(0, "sendactionversionid", "1");
            String sendResponseActionMode = messageTypeInfo.getString(0, "sendactionflag", "A");
            if (sendResponseAction.length() != 0) {
                try {
                    if ("S".equals(sendResponseActionMode)) {
                        this.processLog.append("Sending response synchronously.\n");
                        this.getActionProcessor().processAction(sendResponseAction, sendResponseActionVersion, propertyList);
                        MessageLogUtil.setSendStatus(this.getActionProcessor(), msgLogId, "COMPLETE", "Response message sent successfully", responseMessage, this.processLog.toString());
                    } else {
                        this.processLog.append("Sending response asynchronously.\n");
                        AutomationService ac = new AutomationService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                        ac.addToDoListEntry(null, sendResponseAction, sendResponseActionVersion, propertyList, null, true, propertyList.getProperty("processedby", ""), "", "", "");
                        MessageLogUtil.setSendStatus(this.getActionProcessor(), msgLogId, "COMPLETE", "Send Response request on todo list", responseMessage, this.processLog.toString());
                    }
                    propertyList.setProperty("status", "SUCCESS");
                    propertyList.setProperty("log", this.processLog.toString());
                }
                catch (ActionException e) {
                    this.handleSendActionException(e, msgLogId, "Y", messageTypeInfo, propertyList);
                }
                catch (ServiceException e) {
                    this.processLog.append("ERROR: Failed to process sendAction.").append(e.getMessage()).append("\n");
                    MessageLogUtil.setSendStatus(this.getActionProcessor(), msgLogId, "SENDERROR", e.getMessage(), responseMessage, this.processLog.toString());
                    propertyList.setProperty("status", "FAILED");
                    propertyList.setProperty("error", e.getMessage());
                    propertyList.setProperty("log", this.processLog.toString());
                }
            } else {
                Trace.log("No sendResponseAction specified");
                this.processLog.append("No sendAction specified\n");
                propertyList.setProperty("status", "SUCCESS");
                propertyList.setProperty("log", this.processLog.toString());
            }
        } else {
            propertyList.setProperty("status", "SUCCESS");
            propertyList.setProperty("log", this.processLog.toString());
        }
    }

    private void processValidatedMessage(String msgLogId, PropertyList propertyList, String successMessage) {
        String responseMessage;
        DataSet messageTypeInfo = null;
        try {
            String messageTypeId;
            boolean revalidating = false;
            if ("Y".equals(propertyList.getProperty("validateonly", "N"))) {
                revalidating = true;
            }
            if (!revalidating) {
                propertyList = this.loadOldPropertyList(msgLogId, propertyList);
                propertyList.remove("validateonly");
            }
            if ((messageTypeId = propertyList.getProperty("messagetypeid", "")).length() == 0) {
                String sql = "SELECT * from messagelog WHERE messagelogid = ?";
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{msgLogId});
                messageTypeId = ds.getValue(0, "messagetypeid");
                propertyList.setProperty("messagetypeid", messageTypeId);
            }
            messageTypeInfo = ProcessInMessage.getMessageTypeDetails(this.getQueryProcessor(), messageTypeId);
            String processAction = messageTypeInfo.getString(0, "processactionid", "ProcessSECMessage");
            String processActionVersion = messageTypeInfo.getString(0, "processactionversionid", "1");
            this.processLog.append("Calling processAction ").append(processAction).append("\n");
            MessageLogUtil.setProcessInfo(this.getActionProcessor(), msgLogId, "PROCESSING", "Processing message", propertyList.getProperty("processedby"), this.processLog.toString(), "");
            this.getActionProcessor().processAction(processAction, processActionVersion, propertyList, true);
        }
        catch (ActionException e) {
            this.processLog.append("ERROR: processAction failed.").append(e.getMessage()).append("\n");
            this.handleProcessActionException(e, msgLogId, "Y", messageTypeInfo, propertyList);
            return;
        }
        catch (SapphireException e) {
            this.processLog.append("ERROR: processAction failed.").append(e.getMessage()).append("\n");
            MessageLogUtil.setProcessInfo(this.getActionProcessor(), msgLogId, "ERROR", e.getMessage(), propertyList.getProperty("processedby"), this.processLog.toString(), propertyList.getProperty("validationlog", ""));
            propertyList.setProperty("status", "FAILED");
            propertyList.setProperty("error", e.getMessage());
            propertyList.setProperty("log", this.processLog.toString());
            return;
        }
        if (propertyList.getProperty("status").equals("FAILED")) {
            if (propertyList.getProperty("log", "").length() > 0) {
                this.processLog.append(propertyList.getProperty("log"));
            }
            MessageLogUtil.setProcessInfo(this.getActionProcessor(), msgLogId, "ERROR", propertyList.getProperty("error"), propertyList.getProperty("processedby"), this.processLog.toString(), propertyList.getProperty("validationlog", ""));
            return;
        }
        this.processLog.append("SUCCESS: processAction executed successfully.\n");
        if (propertyList.getProperty("log", "").length() > 0) {
            this.processLog.append(propertyList.getProperty("log"));
        }
        if ((responseMessage = propertyList.getProperty("responsemessage", "")).length() > 0) {
            this.processLog.append("processAction returned responseMessage: \n");
            this.processLog.append(responseMessage);
        }
        if (propertyList.getProperty("validateonly", "N").equals("Y")) {
            MessageLogUtil.setProcessInfo(this.getActionProcessor(), msgLogId, "VALIDATED", successMessage, propertyList.getProperty("processedby"), this.processLog.toString(), propertyList.getProperty("validationlog", ""));
        } else {
            MessageLogUtil.setProcessInfo(this.getActionProcessor(), msgLogId, "COMPLETE", successMessage, propertyList.getProperty("processedby"), this.processLog.toString(), propertyList.getProperty("validationlog", ""));
        }
        if (responseMessage.length() != 0) {
            String sendResponseAction = messageTypeInfo.getString(0, "sendactionid", "");
            String sendResponseActionVersion = messageTypeInfo.getString(0, "sendactionversionid", "1");
            String sendResponseActionMode = messageTypeInfo.getString(0, "sendactionflag", "A");
            if (sendResponseAction.length() != 0) {
                try {
                    if ("S".equals(sendResponseActionMode)) {
                        this.processLog.append("Sending response synchronously.\n");
                        this.getActionProcessor().processAction(sendResponseAction, sendResponseActionVersion, propertyList);
                        MessageLogUtil.setSendStatus(this.getActionProcessor(), msgLogId, "COMPLETE", "Response message sent successfully", responseMessage, this.processLog.toString());
                    } else {
                        this.processLog.append("Sending response asynchronously.\n");
                        AutomationService ac = new AutomationService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                        ac.addToDoListEntry(null, sendResponseAction, sendResponseActionVersion, propertyList, null, true, propertyList.getProperty("processedby", ""), "", "", "");
                        MessageLogUtil.setSendStatus(this.getActionProcessor(), msgLogId, "COMPLETE", "Send Response request on todo list", responseMessage, this.processLog.toString());
                    }
                    propertyList.setProperty("status", "SUCCESS");
                    propertyList.setProperty("log", this.processLog.toString());
                }
                catch (ActionException e) {
                    this.handleSendActionException(e, msgLogId, "Y", messageTypeInfo, propertyList);
                }
                catch (ServiceException e) {
                    this.processLog.append("ERROR: Failed to process sendAction.").append(e.getMessage()).append("\n");
                    MessageLogUtil.setSendStatus(this.getActionProcessor(), msgLogId, "SENDERROR", e.getMessage(), responseMessage, this.processLog.toString());
                    propertyList.setProperty("status", "FAILED");
                    propertyList.setProperty("error", e.getMessage());
                    propertyList.setProperty("log", this.processLog.toString());
                }
            } else {
                Trace.log("No sendResponseAction specified");
                this.processLog.append("No sendAction specified\n");
                propertyList.setProperty("status", "SUCCESS");
                propertyList.setProperty("log", this.processLog.toString());
            }
        } else {
            propertyList.setProperty("status", "SUCCESS");
            propertyList.setProperty("log", this.processLog.toString());
        }
    }

    private String handleProcessActionException(ActionException e, String msgLogId, String allowLogFlag, DataSet messageTypeInfo, PropertyList propertyList) {
        String error = e.getMessage();
        if ((msgLogId == null || msgLogId.length() == 0) && "F".equals(allowLogFlag)) {
            msgLogId = MessageLogUtil.addMsgLogEntry(this.getConnectionid(), this.getActionProcessor(), propertyList, messageTypeInfo.getString(0, "messageclass"), "IN");
            propertyList.setProperty(PROPERTY_MESSAGELOGID, msgLogId);
        }
        if (e.getErrorHandler() != null && e.getErrorHandler().hasErrors()) {
            ErrorDetail errorDetail = (ErrorDetail)e.getErrorHandler().get(0);
            error = errorDetail.getMessage();
            if (error.endsWith("|")) {
                error = error.substring(0, error.lastIndexOf("|"));
            }
            this.processLog.append("processAction error: ").append(error).append("\n");
        }
        if (msgLogId != null) {
            String validationLog = propertyList.getProperty("validationlog", "");
            MessageLogUtil.setProcessInfo(this.getActionProcessor(), msgLogId, "ERROR", error, propertyList.getProperty("processedby"), this.processLog.toString(), validationLog);
        }
        propertyList.setProperty("status", "FAILED");
        propertyList.setProperty("error", error);
        propertyList.setProperty("log", this.processLog.toString());
        return msgLogId;
    }

    private String handleProcessSapphireException(SapphireException e, String msgLogId, String allowLogFlag, DataSet messageTypeInfo, PropertyList propertyList) {
        String error = e.getMessage();
        if ((msgLogId == null || msgLogId.length() == 0) && "F".equals(allowLogFlag)) {
            msgLogId = MessageLogUtil.addMsgLogEntry(this.getConnectionid(), this.getActionProcessor(), propertyList, messageTypeInfo.getString(0, "messageclass"), "IN");
            propertyList.setProperty(PROPERTY_MESSAGELOGID, msgLogId);
        }
        this.processLog.append("processAction error: ").append(error).append("\n");
        if (msgLogId != null) {
            String validationLog = propertyList.getProperty("validationlog", "");
            MessageLogUtil.setProcessInfo(this.getActionProcessor(), msgLogId, "ERROR", error, propertyList.getProperty("processedby"), this.processLog.toString(), validationLog);
        }
        propertyList.setProperty("status", "FAILED");
        propertyList.setProperty("error", error);
        propertyList.setProperty("log", this.processLog.toString());
        return msgLogId;
    }

    private String handleSendActionException(ActionException e, String msgLogId, String allowLogFlag, DataSet messageTypeInfo, PropertyList propertyList) {
        if ((msgLogId == null || msgLogId.length() == 0) && "F".equals(allowLogFlag)) {
            msgLogId = MessageLogUtil.addMsgLogEntry(this.getConnectionid(), this.getActionProcessor(), propertyList, messageTypeInfo.getString(0, "messageclass"), "IN");
            propertyList.setProperty(PROPERTY_MESSAGELOGID, msgLogId);
        }
        if (e.getErrorHandler() != null && e.getErrorHandler().hasErrors()) {
            ErrorDetail errorDetail = (ErrorDetail)e.getErrorHandler().get(0);
            String error = errorDetail.getMessage();
            if (error.endsWith("|")) {
                error = error.substring(0, error.lastIndexOf("|"));
            }
            this.processLog.append("sendAction error: ").append(error).append("\n");
            if (msgLogId != null) {
                MessageLogUtil.setSendStatus(this.getActionProcessor(), msgLogId, "SENDERROR", error, propertyList.getProperty("processedby"), this.processLog.toString());
            }
            propertyList.setProperty("status", "FAILED");
            propertyList.setProperty("error", error);
            propertyList.setProperty("log", this.processLog.toString());
        }
        return msgLogId;
    }
}

