/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.messaging;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.messaging.ProcessInMessage;
import com.labvantage.sapphire.actions.sdi.EditSDIAttachment;
import com.labvantage.sapphire.util.file.FileManager;
import java.io.File;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class MessageLogUtil {
    public static final String PROCESS_STATUS_NOTSTARTED = "NOT STARTED";
    public static final String PROCESS_STATUS_PROCESSING = "PROCESSING";
    public static final String PROCESS_STATUS_COMPLETE = "COMPLETE";
    public static final String PROCESS_STATUS_VALIDATED = "VALIDATED";
    public static final String PROCESS_STATUS_ERROR = "ERROR";
    public static final String PROCESS_STATUS_WAITING = "WAITING";
    public static final String RESPONSE_STATUS_UNSENT = "UNSENT";
    public static final String RESPONSE_STATUS_NONE = "NONE";
    public static final String RESPONSE_STATUS_SENDERROR = "SENDERROR";
    public static final String RESPONSE_STATUS_COMPLETE = "COMPLETE";

    public static String addDFDMsgLogEntry(ActionProcessor ap, QueryProcessor qp, String connectionid, String messagetypeid, String tempid) {
        String msgLogid = null;
        try {
            DataSet messageTypeInfo = ProcessInMessage.getMessageTypeDetails(qp, messagetypeid);
            FileManager.TempFile tempFile = FileManager.TempFile.getTempFile(tempid, false, qp, connectionid);
            String filename = tempFile.getFileName();
            PropertyList msgLogProps = new PropertyList();
            msgLogProps.setProperty("sdcid", "LV_MessageLog");
            msgLogProps.setProperty("messagetag", filename);
            msgLogProps.setProperty("messagetypeid", messagetypeid);
            msgLogProps.setProperty("directionflag", "I");
            msgLogProps.setProperty("processstatus", PROCESS_STATUS_NOTSTARTED);
            msgLogProps.setProperty("processnotes", "");
            msgLogProps.setProperty("sendstatus", "");
            msgLogProps.setProperty("sendnotes", "");
            msgLogProps.setProperty("messageclass", messageTypeInfo.getValue(0, "messageclass"));
            ap.processAction("AddSDI", "1", msgLogProps, true);
            msgLogid = (String)msgLogProps.get("newkeyid1");
            DataSet attachmentData = new DataSet();
            attachmentData.addColumn("sdcid", 0);
            attachmentData.addColumn("keyid1", 0);
            attachmentData.addColumn("keyid2", 0);
            attachmentData.addColumn("keyid3", 0);
            attachmentData.addColumn("filename", 0);
            attachmentData.addColumn("__tempid", 0);
            attachmentData.addColumn("typeflag", 0);
            attachmentData.addColumn("__rowstatus", 0);
            attachmentData.addRow();
            attachmentData.setValue(0, "sdcid", "LV_MessageLog");
            attachmentData.setValue(0, "keyid1", msgLogid);
            attachmentData.setValue(0, "__tempid", tempid);
            attachmentData.setValue(0, "typeflag", "R");
            attachmentData.setValue(0, "__rowstatus", "I");
            attachmentData.setValue(0, "filename", filename);
            FileManager.saveAttachmentData("LV_MessageLog", attachmentData, connectionid);
        }
        catch (SapphireException se) {
            Trace.log("Failed to add MessageLog SDI" + se.getMessage());
        }
        return msgLogid;
    }

    public static String addMsgLogEntry(ActionProcessor ap, PropertyList inputProps, String messageClass, String direction) {
        return MessageLogUtil.addMsgLogEntry("", ap, inputProps, messageClass, direction);
    }

    public static String addMsgLogEntry(String connectionid, ActionProcessor ap, PropertyList inputProps, String messageClass, String direction) {
        String msgLogid = null;
        try {
            PropertyList msgLogProps = new PropertyList();
            if ("IN".equals(direction)) {
                msgLogProps.setProperty("sdcid", "LV_MessageLog");
                msgLogProps.setProperty("messagetag", inputProps.getProperty("messagetag"));
                msgLogProps.setProperty("referencetag", inputProps.getProperty("referencetag"));
                msgLogProps.setProperty("messagetypeid", inputProps.getProperty("messagetypeid"));
                msgLogProps.setProperty("directionflag", "I");
                msgLogProps.setProperty("messagebody", inputProps.getProperty("message"));
                msgLogProps.setProperty("propertylist", inputProps.toXMLString());
                msgLogProps.setProperty("processstatus", PROCESS_STATUS_PROCESSING);
                msgLogProps.setProperty("processnotes", "");
                msgLogProps.setProperty("sendstatus", "");
                msgLogProps.setProperty("sendnotes", "");
                msgLogProps.setProperty("messageclass", messageClass);
                ap.processAction("AddSDI", "1", msgLogProps, true);
                msgLogid = (String)msgLogProps.get("newkeyid1");
                String filename = inputProps.getProperty("filename", "");
                String path = inputProps.getProperty("path", "");
                if (connectionid.length() > 0 && filename.length() > 0 && path.length() > 0) {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", "LV_MessageLog");
                    props.setProperty("keyid1", msgLogid);
                    props.setProperty("filename", path + File.separator + filename);
                    props.setProperty("type", "R");
                    try {
                        new ActionProcessor(connectionid).processAction("AddSDIAttachment", "1", props, true);
                    }
                    catch (SapphireException e) {
                        Trace.log("Failed to save the input file as attachment.");
                    }
                }
            } else {
                msgLogProps.setProperty("sdcid", "LV_MessageLog");
                msgLogProps.setProperty("referencetag", inputProps.getProperty("referencetag"));
                msgLogProps.setProperty("messagetypeid", inputProps.getProperty("messagetypeid"));
                msgLogProps.setProperty("directionflag", "O");
                msgLogProps.setProperty("propertylist", inputProps.toXMLString());
                msgLogProps.setProperty("processstatus", PROCESS_STATUS_PROCESSING);
                msgLogProps.setProperty("processnotes", "");
                msgLogProps.setProperty("sendstatus", "");
                msgLogProps.setProperty("sendnotes", "");
                msgLogProps.setProperty("messageclass", messageClass);
                ap.processAction("AddSDI", "1", msgLogProps, true);
                msgLogid = (String)msgLogProps.get("newkeyid1");
            }
        }
        catch (SapphireException se) {
            Trace.log("Failed to add MessageLog SDI" + se.getMessage());
        }
        return msgLogid;
    }

    public static String getMessage(QueryProcessor qp, String msgLogId) throws SapphireException {
        String sql = "SELECT messagebody FROM messagelog WHERE messagelogid=?";
        DataSet ds = qp.getPreparedSqlDataSet(sql, new Object[]{msgLogId}, true);
        if (ds == null || ds.getRowCount() == 0) {
            throw new SapphireException("message not found in message log: " + msgLogId);
        }
        return ds.getValue(0, "messagebody");
    }

    public static void updateFilePath(ActionProcessor actionProcessor, String msgLogId, String filename) throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_MessageLog");
        props.setProperty("keyid1", msgLogId);
        props.setProperty("filename", filename);
        props.setProperty("attachmentnum", "1");
        actionProcessor.processActionClass(EditSDIAttachment.class.getName(), props, true);
    }

    public static void updateReprocessInfo(ActionProcessor ap, String msgLogId, PropertyList inputProperties) {
        if (msgLogId == null || msgLogId.length() == 0) {
            return;
        }
        try {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", "LV_MessageLog");
            actionProps.setProperty("keyid1", msgLogId);
            actionProps.setProperty("processstatus", PROCESS_STATUS_PROCESSING);
            actionProps.setProperty("processnotes", "");
            actionProps.setProperty("sendstatus", "");
            actionProps.setProperty("sendnotes", "");
            actionProps.setProperty("lastreprocesseddt", "N");
            if (inputProperties.getProperty("lastreprocessedby") != null) {
                actionProps.setProperty("lastreprocessedby", inputProperties.getProperty("lastreprocessedby"));
            }
            ap.processAction("EditSDI", "1", actionProps, true);
        }
        catch (SapphireException se) {
            Trace.log("Failed to update reprocess info in msglog" + se.getMessage());
        }
    }

    public static void updateResendInfo(ActionProcessor ap, String msgLogId, PropertyList inputProperties) {
        if (msgLogId == null || msgLogId.length() == 0) {
            return;
        }
        try {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", "LV_MessageLog");
            actionProps.setProperty("keyid1", msgLogId);
            actionProps.setProperty("sendstatus", RESPONSE_STATUS_UNSENT);
            actionProps.setProperty("sendnotes", "");
            actionProps.setProperty("lastresentdt", "N");
            if (inputProperties.getProperty("lastresentby") != null) {
                actionProps.setProperty("lastresentby", inputProperties.getProperty("lastresentby"));
            }
            ap.processAction("EditSDI", "1", actionProps, true);
        }
        catch (SapphireException se) {
            Trace.log("Failed to update resend info in msglog" + se.getMessage());
        }
    }

    public static PropertyList getPropertyList(QueryProcessor qp, String msgLogId) throws SapphireException {
        String sql = "SELECT propertylist FROM messagelog WHERE messagelogid = ?";
        DataSet result = qp.getPreparedSqlDataSet(sql, new Object[]{msgLogId}, true);
        if (result == null || result.getRowCount() == 0) {
            throw new SapphireException("No message log entry found");
        }
        PropertyList ret = new PropertyList();
        ret.setPropertyList(result.getClob(0, "propertylist"));
        return ret;
    }

    public static void changeProcessStatus(ActionProcessor ap, String msgLogID, String processStatus, String processNotes, String processLog) {
        if (msgLogID == null || msgLogID.length() == 0) {
            return;
        }
        try {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", "LV_MessageLog");
            actionProps.setProperty("keyid1", msgLogID);
            actionProps.setProperty("processstatus", processStatus);
            if (processNotes.length() > 2000) {
                processNotes = processNotes.substring(0, 2000);
            }
            actionProps.setProperty("processnotes", processNotes);
            actionProps.setProperty("processlog", processLog);
            ap.processAction("EditSDI", "1", actionProps, true);
        }
        catch (SapphireException se) {
            Trace.logError("Fail to update Msglog ", se);
        }
    }

    public static void setProcessInfo(ActionProcessor ap, String msgLogID, String processStatus, String processNotes, String processedBy, String processLog, String validationLog) {
        if (msgLogID == null || msgLogID.length() == 0) {
            return;
        }
        try {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", "LV_MessageLog");
            actionProps.setProperty("keyid1", msgLogID);
            actionProps.setProperty("processstatus", processStatus);
            if (processNotes.length() > 2000) {
                processNotes = processNotes.substring(0, 2000);
            }
            actionProps.setProperty("processnotes", processNotes);
            actionProps.setProperty("processedby", processedBy);
            actionProps.setProperty("processeddt", "N");
            actionProps.setProperty("processlog", processLog);
            if (validationLog.length() > 2 && validationLog.contains("condition")) {
                actionProps.setProperty("validationlog", validationLog);
            }
            ap.processAction("EditSDI", "1", actionProps, true);
        }
        catch (SapphireException se) {
            Trace.logError("Fail to update Msglog ", se);
        }
    }

    public static void setSendStatus(ActionProcessor ap, String msgLogID, String sendStatus, String sendStatusNotes, String responseMessage, String log) {
        if (msgLogID == null || msgLogID.length() == 0) {
            return;
        }
        try {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", "LV_MessageLog");
            actionProps.setProperty("keyid1", msgLogID);
            actionProps.setProperty("sendstatus", sendStatus);
            actionProps.setProperty("sendnotes", sendStatusNotes);
            actionProps.setProperty("responsebody", responseMessage);
            actionProps.setProperty("processlog", log);
            ap.processAction("EditSDI", "1", actionProps, true);
        }
        catch (SapphireException se) {
            Trace.logError("Fail to update Msglog ", se);
        }
    }

    public static void setMessageDetails(ActionProcessor ap, String messageLogID, String messageTag, String message) {
        if (messageLogID == null || messageLogID.length() == 0) {
            return;
        }
        try {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", "LV_MessageLog");
            actionProps.setProperty("keyid1", messageLogID);
            actionProps.setProperty("processstatus", "COMPLETE");
            actionProps.setProperty("processnotes", "Message created successfully");
            actionProps.setProperty("messagebody", message);
            actionProps.setProperty("messagetag", messageTag);
            ap.processAction("EditSDI", "1", actionProps, true);
        }
        catch (SapphireException se) {
            Trace.logError("Fail to update Msglog ", se);
        }
    }

    public static String getProcessStatus(QueryProcessor qp, String msgLogId) throws SapphireException {
        String sql = "SELECT processstatus FROM messagelog where messagelogid = ?";
        DataSet results = qp.getPreparedSqlDataSet(sql, new Object[]{msgLogId});
        if (results.getRowCount() == 0) {
            throw new SapphireException("Invalid msgLogId specified:" + msgLogId);
        }
        return results.getString(0, "processstatus");
    }

    public static String getValidationLogTxt(TranslationProcessor tp, String validationlog) throws Exception {
        PropertyListCollection validationreport = new PropertyListCollection();
        validationreport.setJSONString(validationlog);
        StringBuffer text = new StringBuffer();
        if (validationreport.size() > 0) {
            text.append(tp.translate("Cell")).append("\t");
            text.append(tp.translate("Title")).append("\t");
            text.append(tp.translate("Value")).append("\t");
            text.append(tp.translate("Action")).append("\t");
            text.append(tp.translate("Message")).append("\r\n");
            for (int i = 0; i < validationreport.size(); ++i) {
                PropertyList currentValidationItem = validationreport.getPropertyList(i);
                text.append(currentValidationItem.getProperty("column")).append(currentValidationItem.getProperty("row")).append("\t");
                text.append(currentValidationItem.getProperty("title")).append("\t");
                text.append(currentValidationItem.getProperty("value")).append("\t");
                text.append(currentValidationItem.getProperty("action")).append("\t");
                text.append(currentValidationItem.getProperty("message")).append("\r\n");
            }
        }
        return text.toString();
    }

    public static String getValidationLogHtml(TranslationProcessor tp, PropertyListCollection validationreport) {
        StringBuffer html = new StringBuffer();
        html.append("  <style>\n            table {\n                border-collapse: collapse;\n                margin-top: 10px;\n                margin-left: 20px;\n            }\n\n            table, th, td {\n                border: 1px solid black;\n            }\n        </style>");
        html.append("<P>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Validation Log:</P>");
        html.append("<TABLE>\n");
        boolean includeWorksheet = false;
        if (validationreport.size() > 0 && validationreport.getPropertyList(0).getProperty("worksheet") != null && validationreport.getPropertyList(0).getProperty("worksheet").length() > 0) {
            includeWorksheet = true;
        }
        html.append("<THEAD>");
        if (includeWorksheet) {
            html.append("<TH>" + tp.translate("Worksheet") + "</TH>");
        }
        html.append("<TH>" + tp.translate("Cell") + "</TH><TH>" + tp.translate("Title") + "</TH><TH>" + tp.translate("Value") + "</TH><TH>" + tp.translate("Validation Action") + "</TH><TH>" + tp.translate("Message") + "</TH></THEAD>");
        for (int i = 0; i < validationreport.size(); ++i) {
            PropertyList currentValidationItem = validationreport.getPropertyList(i);
            html.append("<TR>");
            String fontstr = "<font color=\"black\">";
            if (currentValidationItem.getProperty("status").equalsIgnoreCase("FAIL")) {
                fontstr = "<font color=\"red\">";
            }
            if (includeWorksheet) {
                html.append("<TD>" + fontstr + currentValidationItem.getProperty("worksheet") + "</font></TD>");
            }
            html.append("<TD>" + fontstr + currentValidationItem.getProperty("column") + currentValidationItem.getProperty("row") + "</font></TD>");
            html.append("<TD>" + fontstr + currentValidationItem.getProperty("title") + "</font></TD>");
            html.append("<TD>" + fontstr + currentValidationItem.getProperty("value") + "</font></TD>");
            html.append("<TD>" + fontstr + currentValidationItem.getProperty("action") + "</font></TD>");
            html.append("<TD>" + fontstr + currentValidationItem.getProperty("message") + "</font></TD>");
            html.append("</TR>");
        }
        html.append("</TABLE>");
        return html.toString();
    }
}

