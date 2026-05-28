/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.messaging;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.messaging.MessageLogUtil;
import com.labvantage.sapphire.services.AutomationService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.error.ErrorDetail;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ResendMessage
extends BaseAction
implements sapphire.action.ResendMessage {
    @Override
    public void processAction(PropertyList propertyList) throws SapphireException {
        String msgLogIds = propertyList.getProperty("messagelogid");
        if (msgLogIds == null || msgLogIds.length() == 0) {
            throw new ActionException("MessageLogId is empty or null");
        }
        String[] msgLogId = StringUtil.split(msgLogIds, ";");
        String successList = "";
        String failList = "";
        for (int i = 0; i < msgLogId.length; ++i) {
            boolean allowLogFlag;
            String sql = "SELECT messagetype.directionflag, allowresendflag, allowlogflag, sendstatus, messagetype.messagetypeid, sendactionid, sendactionversionid, sendactionflag FROM messagetype, messagelog where messagelogid = ? and messagelog.messagetypeid = messagetype.messagetypeid";
            DataSet results = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{msgLogId[i]});
            if (results.getRowCount() == 0) {
                Trace.log("Invalid msglogid: " + msgLogId[i]);
                this.setError("Invalid msglogid: " + msgLogId[i]);
                continue;
            }
            String direction = results.getString(0, "directionflag");
            String allowResendFlag = results.getString(0, "allowresendflag");
            String sendstatus = results.getString(0, "sendstatus");
            String messageType = results.getString(0, "messagetypeid");
            boolean bl = allowLogFlag = !"N".equalsIgnoreCase(results.getString(0, "allowlogflag"));
            if (!allowLogFlag) {
                throw new ActionException("Allow Log Flag set to N for messagetype " + messageType);
            }
            if ("N".equals(allowResendFlag)) {
                Trace.log("Cannot resend messages of type:  " + messageType);
                this.setError("Cannot resend messages of type:  " + messageType);
                if (failList.length() == 0) {
                    failList = failList + msgLogId[i];
                    continue;
                }
                failList = failList + "," + msgLogId[i];
                continue;
            }
            if ("F".equals(allowResendFlag) && !"SENDERROR".equals(sendstatus)) {
                this.setError(" Cannot resend message " + msgLogId[i] + " with SendStatus as: " + sendstatus);
                if (failList.length() == 0) {
                    failList = failList + msgLogId[i];
                    continue;
                }
                failList = failList + "," + msgLogId[i];
                continue;
            }
            if ("O".equalsIgnoreCase(direction)) {
                MessageLogUtil.updateResendInfo(this.getActionProcessor(), msgLogId[i], propertyList);
                PropertyList oldPropertyList = MessageLogUtil.getPropertyList(this.getQueryProcessor(), msgLogId[i]);
                Object[] keyes = oldPropertyList.keySet().toArray();
                for (int j = 0; j < keyes.length; ++j) {
                    propertyList.setProperty((String)keyes[j], oldPropertyList.getProperty((String)keyes[j]));
                }
                propertyList.setProperty("message", MessageLogUtil.getMessage(this.getQueryProcessor(), msgLogId[i]));
                String sendMessageAction = results.getString(0, "sendactionid");
                String sendMessageActionVersion = results.getString(0, "sendactionversionid", "1");
                String sendMessageActionMode = results.getString(0, "sendactionflag", "A");
                StringBuffer processLog = new StringBuffer();
                try {
                    if ("S".equals(sendMessageActionMode)) {
                        processLog.append("ResendMessage: calling sendAction synchronously:").append(sendMessageAction).append("\n");
                        this.getActionProcessor().processAction(sendMessageAction, sendMessageActionVersion, propertyList);
                        MessageLogUtil.setSendStatus(this.getActionProcessor(), msgLogId[i], "COMPLETE", "Message sent successfully", "", processLog.toString());
                        if (successList.length() == 0) {
                            successList = successList + msgLogId[i];
                            continue;
                        }
                        successList = successList + "," + msgLogId[i];
                        continue;
                    }
                    processLog.append("ResendMessage: calling sendAction Asynchronously:").append(sendMessageAction).append("\n");
                    AutomationService ac = new AutomationService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                    ac.addToDoListEntry(null, sendMessageAction, sendMessageActionVersion, propertyList, null, true);
                    MessageLogUtil.setSendStatus(this.getActionProcessor(), msgLogId[i], "COMPLETE", "Send Response request on todo list", "", processLog.toString());
                    if (successList.length() == 0) {
                        successList = successList + msgLogId[i];
                        continue;
                    }
                    successList = successList + "," + msgLogId[i];
                }
                catch (ActionException e) {
                    ErrorDetail errorDetail;
                    String error = e.getMessage();
                    if (e.getErrorHandler() != null && e.getErrorHandler().hasErrors() && (error = (errorDetail = (ErrorDetail)e.getErrorHandler().get(0)).getMessage()).endsWith("|")) {
                        error = error.substring(0, error.lastIndexOf("|"));
                    }
                    processLog.append("ResendMessage: sendAction failed:").append(error).append("\n");
                    MessageLogUtil.setSendStatus(this.getActionProcessor(), msgLogId[i], "SENDERROR", error, "", processLog.toString());
                    this.setError(error);
                    if (failList.length() == 0) {
                        failList = failList + msgLogId[i];
                        continue;
                    }
                    failList = failList + "," + msgLogId[i];
                }
                catch (ServiceException e) {
                    processLog.append("ResendMessage: sendAction failed:").append(e.getMessage()).append("\n");
                    MessageLogUtil.setSendStatus(this.getActionProcessor(), msgLogId[i], "SENDERROR", e.getMessage(), "", processLog.toString());
                    this.setError("Failed to service request: " + e.getMessage());
                    if (failList.length() == 0) {
                        failList = failList + msgLogId[i];
                        continue;
                    }
                    failList = failList + "," + msgLogId[i];
                }
                continue;
            }
            if ("I".equalsIgnoreCase(direction)) {
                this.setError("Cannot resend IN messages : " + msgLogId[i]);
                if (failList.length() == 0) {
                    failList = failList + msgLogId[i];
                    continue;
                }
                failList = failList + "," + msgLogId[i];
                continue;
            }
            this.setError("Direction is not known for msglogid: " + msgLogId[i]);
            failList = failList.length() == 0 ? failList + msgLogId[i] : failList + "," + msgLogId[i];
        }
    }
}

