/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.messaging;

import com.labvantage.sapphire.messaging.MessageLogUtil;
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

public class ProcessOutMessage
extends BaseAction
implements sapphire.action.ProcessOutMessage {
    @Override
    public void processAction(PropertyList propertyList) throws SapphireException {
        String msgLogId = propertyList.getProperty("messagelogid", "");
        boolean reprocessFlag = !"".equals(msgLogId);
        String msgTypeId = "";
        String processAction = "";
        String processActionVersion = "";
        String sendMessageAction = "";
        String sendMessageActionVersion = "";
        String sendMessageActionMode = "A";
        DataSet messageTypeInfo = null;
        String message = "";
        StringBuffer processLog = new StringBuffer();
        String processedBy = propertyList.getProperty("processedby", "");
        if (propertyList.getProperty("log", "").length() > 0) {
            processLog.append(propertyList.getProperty("log", ""));
        }
        String allowLogFlag = "Y";
        if (!reprocessFlag) {
            msgTypeId = propertyList.getProperty("messagetypeid", "");
            if (msgTypeId.length() == 0) {
                throw new ActionException("MessageTypeId is null or empty");
            }
            messageTypeInfo = this.getMessageTypeDetails(this.getQueryProcessor(), msgTypeId);
            processLog.append("ProcessOutMessage: got the message type details.\n");
            allowLogFlag = messageTypeInfo.getString(0, "allowlogflag", "Y");
            if ("Y".equals(allowLogFlag)) {
                msgLogId = MessageLogUtil.addMsgLogEntry(this.getActionProcessor(), propertyList, messageTypeInfo.getString(0, "messageclass", ""), processLog.toString());
            }
        } else {
            processLog.append("ProcessOutMessage: reprocess flag is set, fetching properties from messagelog.\n");
            PropertyList oldPropertyList = MessageLogUtil.getPropertyList(this.getQueryProcessor(), msgLogId);
            msgTypeId = oldPropertyList.getProperty("messagetypeid", "");
            messageTypeInfo = this.getMessageTypeDetails(this.getQueryProcessor(), msgTypeId);
            Object[] keyes = oldPropertyList.keySet().toArray();
            for (int j = 0; j < keyes.length; ++j) {
                propertyList.setProperty((String)keyes[j], oldPropertyList.getProperty((String)keyes[j]));
            }
            if (msgTypeId.length() == 0) {
                throw new ActionException("MessageTypeId is null or empty");
            }
            processLog.append("ProcessOutMessage: starting reprocess.\n");
            MessageLogUtil.updateReprocessInfo(this.getActionProcessor(), msgLogId, propertyList);
        }
        processAction = messageTypeInfo.getString(0, "processactionid", "CreateSECMessage");
        processActionVersion = messageTypeInfo.getString(0, "processactionversionid", "1");
        sendMessageAction = messageTypeInfo.getString(0, "sendactionid", "");
        sendMessageActionVersion = messageTypeInfo.getString(0, "sendactionversionid", "1");
        sendMessageActionMode = messageTypeInfo.getString(0, "sendactionflag", "A");
        String messageTag = "";
        try {
            processLog.append("ProcessOutMessage: calling processAction: ").append(processAction).append("\n");
            MessageLogUtil.setProcessInfo(this.getActionProcessor(), msgLogId, "PROCESSING", "Processing outgoing message", processedBy, processLog.toString(), "");
            this.getActionProcessor().processAction(processAction, processActionVersion, propertyList);
            message = propertyList.getProperty("message");
            messageTag = propertyList.getProperty("messagetag");
            String log = propertyList.getProperty("log", "");
            processLog.append("ProcessOutMessage: processAction retuend message: \n");
            processLog.append("______________________________________________________________\n");
            processLog.append(message);
            processLog.append("\n_______________________________________________________________\n");
            if (log.length() > 0) {
                processLog.append("ProcessOutMessage: processAction returned log:\n");
                processLog.append("______________________________________________________________\n");
                processLog.append(log);
                processLog.append("\n_______________________________________________________________\n");
            }
            MessageLogUtil.setMessageDetails(this.getActionProcessor(), msgLogId, messageTag, message);
        }
        catch (ActionException e) {
            ErrorDetail errorDetail;
            String error = e.getMessage();
            if (e.getErrorHandler() != null && e.getErrorHandler().hasErrors() && (error = (errorDetail = (ErrorDetail)e.getErrorHandler().get(0)).getMessage()).endsWith("|")) {
                error = error.substring(0, error.lastIndexOf("|"));
            }
            processLog.append("ProcessOutMessage: processAction failed with error: ").append(error).append("\n");
            if (msgLogId.length() == 0 && "F".equals(allowLogFlag)) {
                msgLogId = MessageLogUtil.addMsgLogEntry(this.getActionProcessor(), propertyList, messageTypeInfo.getString(0, "messageclass", ""), processLog.toString());
            }
            MessageLogUtil.changeProcessStatus(this.getActionProcessor(), msgLogId, "ERROR", error, "");
            propertyList.setProperty("status", "FAILED");
            propertyList.setProperty("error", error);
            propertyList.setProperty("log", processLog.toString());
            return;
        }
        if (sendMessageAction == null || sendMessageAction.length() == 0) {
            processLog.append("ProcessOutMessage: sendAction not configured.");
            MessageLogUtil.changeProcessStatus(this.getActionProcessor(), msgLogId, "COMPLETE", "Message created successfully", processLog.toString());
            propertyList.setProperty("status", "SUCCESS");
            return;
        }
        String responseMessage = "";
        try {
            if ("S".equals(sendMessageActionMode)) {
                processLog.append("ProcessOutMessage: Send message action is being called synchronously: ").append(sendMessageAction).append("\n");
                this.getActionProcessor().processAction(sendMessageAction, sendMessageActionVersion, propertyList);
                MessageLogUtil.setSendStatus(this.getActionProcessor(), msgLogId, "COMPLETE", "Message sent successfully", responseMessage, processLog.toString());
                responseMessage = propertyList.getProperty("responsemessage");
            } else {
                processLog.append("ProcessOutMessage: Send message action is being called Asynchronously: ").append(sendMessageAction).append("\n");
                AutomationService ac = new AutomationService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                ac.addToDoListEntry(null, sendMessageAction, sendMessageActionVersion, propertyList, null, true);
                MessageLogUtil.setSendStatus(this.getActionProcessor(), msgLogId, "COMPLETE", "Send Response request on todo list", responseMessage, processLog.toString());
            }
        }
        catch (ActionException e) {
            ErrorDetail errorDetail;
            String error = e.getMessage();
            if (e.getErrorHandler() != null && e.getErrorHandler().hasErrors() && (error = (errorDetail = (ErrorDetail)e.getErrorHandler().get(0)).getMessage()).endsWith("|")) {
                error = error.substring(0, error.lastIndexOf("|"));
            }
            processLog.append("ProcessOutMessage: sendAction failed with error: ").append(error).append("\n");
            if (msgLogId.length() == 0 && "F".equals(allowLogFlag)) {
                msgLogId = MessageLogUtil.addMsgLogEntry(this.getActionProcessor(), propertyList, messageTypeInfo.getString(0, "messageclass", ""), processLog.toString());
            }
            MessageLogUtil.setSendStatus(this.getActionProcessor(), msgLogId, "SENDERROR", error, responseMessage, processLog.toString());
            propertyList.setProperty("status", "FAILED");
            propertyList.setProperty("error", error);
            propertyList.setProperty("log", processLog.toString());
            return;
        }
        catch (ServiceException e) {
            if (msgLogId.length() == 0 && "F".equals(allowLogFlag)) {
                msgLogId = MessageLogUtil.addMsgLogEntry(this.getActionProcessor(), propertyList, messageTypeInfo.getString(0, "messageclass", ""), processLog.toString());
            }
            processLog.append("ProcessOutMessage: sendAction failed with error: ").append(e.getMessage()).append("\n");
            MessageLogUtil.setSendStatus(this.getActionProcessor(), msgLogId, "SENDERROR", e.getMessage(), responseMessage, processLog.toString());
            propertyList.setProperty("status", "FAILED");
            propertyList.setProperty("error", e.getMessage());
            propertyList.setProperty("log", processLog.toString());
            return;
        }
        MessageLogUtil.setSendStatus(this.getActionProcessor(), msgLogId, "COMPLETE", "Sent successfully", responseMessage, processLog.toString());
        propertyList.setProperty("status", "SUCCESS");
        propertyList.setProperty("log", processLog.toString());
    }

    private DataSet getMessageTypeDetails(QueryProcessor qp, String messageTypeId) throws ActionException {
        String sql = "SELECT directionflag, processactionid, processactionversionid, sendactionid, sendactionversionid, sendactionflag, allowreprocessflag, allowresendflag, messageclass, allowlogflag FROM messagetype WHERE messagetypeid=?";
        DataSet result = qp.getPreparedSqlDataSet(sql, new Object[]{messageTypeId});
        if (result == null || result.getRowCount() == 0) {
            throw new ActionException("Message Type details not found for messagetypeid: " + messageTypeId);
        }
        return result;
    }
}

