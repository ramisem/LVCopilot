/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.fileprocessor;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.xml.SAPMessageHandler;
import com.labvantage.sapphire.xml.SaxUtil;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class ProcessSAPMessage
extends BaseAction
implements sapphire.action.ProcessSAPMessage {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        Trace.log("********************** STARTING PROCESSSAPMESSAGE ACTION ::");
        String path = properties.getProperty("path");
        String fileName = properties.getProperty("filename");
        String sapMessage = properties.getProperty("sapmessage");
        String processedBy = properties.getProperty("processedby", "");
        boolean fileMode = true;
        if (fileName.equals("") && sapMessage.equals("")) {
            throw new SapphireException("INVALID_PROPERTYEither SAPMessage or filename is to be specified");
        }
        if (sapMessage.length() > 0) {
            fileMode = false;
            fileName = "";
            path = "";
        }
        if (fileMode && path.equals("")) {
            throw new SapphireException("INVALID_PARAMETER", "The path " + path + " is invalid.");
        }
        String msgLogid = this.addMsgLogEntry(this.getActionProcessor(), path, fileName, processedBy);
        try {
            InputStream inputStream;
            SAPMessageHandler handler = new SAPMessageHandler();
            handler.setConnectionid(this.getConnectionId());
            handler.setMsglogId(msgLogid);
            handler.setDBUtil((DBUtil)this.database);
            String status = null;
            String currentPath = "";
            if (fileMode) {
                inputStream = new FileInputStream(path + "/" + fileName);
                currentPath = properties.getProperty("successpath");
            } else {
                inputStream = new ByteArrayInputStream(sapMessage.getBytes());
            }
            handler.setInputStream(inputStream);
            SaxUtil.parseStream(handler, "");
            properties.put("msglogid", msgLogid);
            properties.put("sapresponse", handler.getSAPResponse());
            inputStream.close();
            inputStream = null;
            status = "Completed";
            this.updateMsgLogStatus(this.getActionProcessor(), msgLogid, status, path, fileName, currentPath, "Inbound message processed successfully");
            properties.put("status", "success");
        }
        catch (SapphireException e) {
            this.logger.error("Failed to parse file: " + e.getMessage(), e);
            this.updateMsgLogStatus(this.getActionProcessor(), msgLogid, "Failed", path, fileName, properties.getProperty("failpath"), e.getCause().getMessage());
            properties.put("msglogid", msgLogid);
            properties.put("status", "failed");
        }
        catch (Exception e) {
            this.logger.error("Failed to parse file: " + e.getMessage(), e);
            this.updateMsgLogStatus(this.getActionProcessor(), msgLogid, "Failed", path, fileName, properties.getProperty("failpath"), "Unexpected Exception:" + e.getMessage());
            properties.put("msglogid", msgLogid);
            properties.put("status", "failed");
        }
    }

    private void updateMsgLogStatus(ActionProcessor ap, String msgLogid, String status, String actualpath, String filename, String currentpath, String msgdesc) {
        try {
            HashMap<String, String> actionProps = new HashMap<String, String>();
            actionProps.put("sdcid", "LV_SAPMsgLog");
            actionProps.put("keyid1", msgLogid);
            actionProps.put("processstatus", status);
            actionProps.put("actualpath", actualpath);
            actionProps.put("actualfilename", filename);
            actionProps.put("currentpath", currentpath);
            actionProps.put("currentfilename", filename);
            actionProps.put("notes", msgdesc);
            ap.processAction("EditSDI", "1", actionProps, true);
        }
        catch (SapphireException se) {
            this.logger.error("Fail to update Msglog", se);
        }
    }

    private String addMsgLogEntry(ActionProcessor ap, String path, String fileName, String processedBy) throws SapphireException {
        String msgLogid = null;
        try {
            HashMap<String, String> actionProps = new HashMap<String, String>();
            actionProps.put("sdcid", "LV_SAPMsgLog");
            actionProps.put("processstatus", "Initial");
            actionProps.put("actualpath", path);
            actionProps.put("actualfilename", fileName);
            actionProps.put("currentpath", path);
            actionProps.put("currentfilename", fileName);
            if (!processedBy.equals("")) {
                actionProps.put("processedby", processedBy);
                actionProps.put("processeddt", "N");
            }
            ap.processAction("AddSDI", "1", actionProps, true);
            msgLogid = (String)actionProps.get("newkeyid1");
        }
        catch (SapphireException se) {
            this.logger.error("Fail to add SAPMsgLog SDI" + se.getMessage(), se);
            throw se;
        }
        return msgLogid;
    }
}

