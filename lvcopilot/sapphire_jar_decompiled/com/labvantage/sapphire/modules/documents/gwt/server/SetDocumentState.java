/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.documents.gwt.server;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.actions.documents.SetDocumentStatus;
import com.labvantage.sapphire.actions.documents.SetDocumentValue;
import com.labvantage.sapphire.modules.documents.Document;
import com.labvantage.sapphire.modules.documents.gwt.server.BaseDocumentCommand;
import com.labvantage.sapphire.modules.documents.gwt.server.DocumentCommand;
import com.labvantage.sapphire.services.SapphireConnection;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.xml.PropertyList;

public class SetDocumentState
extends BaseDocumentCommand
implements DocumentCommand {
    public SetDocumentState(SapphireConnection sapphireConnection, boolean debug) {
        super(sapphireConnection, debug);
    }

    @Override
    public HashMap execute(PropertyList requestData) {
        String operation = requestData.getProperty("requestcommand");
        String rsetid = requestData.getProperty("rsetid");
        String documentid = requestData.getProperty("documentid");
        String documentversionid = requestData.getProperty("documentversionid", "1");
        boolean preview = requestData.getProperty("preview", "N").equals("Y");
        String hostpageid = requestData.getProperty("hostpageid");
        HashMap<String, String> responseData = new HashMap<String, String>();
        try {
            Document document;
            if (rsetid.length() > 0) {
                this.logger.info("Setting document state to " + operation + " - rsetid=" + rsetid);
                document = Document.getInstance(this.sapphireConnection, rsetid, !preview, this.debug);
            } else if (documentid.length() > 0) {
                this.logger.info("Setting document state to " + operation + " - documentid=" + documentid);
                document = Document.getInstance(this.sapphireConnection, documentid, documentversionid, this.evalTokens(requestData, EncryptDecrypt.unobfsql(requestData.getProperty("restrictivewhere"))), !preview, this.debug);
            } else {
                throw new SapphireException("Document rset or id not passed in request.");
            }
            boolean saved = false;
            String status = "OK";
            String returnmessage = "";
            if (document.getLockedby().length() == 0) {
                String actionClass;
                PropertyList actionProps = new PropertyList();
                actionProps.setProperty("documentid", documentid);
                actionProps.setProperty("documentversionid", documentversionid);
                if (operation.equals("assign")) {
                    actionClass = SetDocumentValue.class.getName();
                    actionProps.setProperty("sysuserid1", requestData.getProperty("sysuserid1"));
                } else {
                    actionClass = SetDocumentStatus.class.getName();
                    actionProps.setProperty("documentstatus", operation.equals("cancel") ? "CN" : (operation.equals("lock") ? "LK" : "DN"));
                    actionProps.setProperty("statusmessage", requestData.getProperty("auditreason"));
                }
                actionProps.setProperty("auditreason", requestData.getProperty("auditreason"));
                actionProps.setProperty("auditactivity", requestData.getProperty("auditactivity"));
                actionProps.setProperty("auditsignedflag", requestData.getProperty("auditsignedflag"));
                actionProps.setProperty("auditdt", requestData.getProperty("auditdt"));
                try {
                    ActionProcessor actionProcessor = new ActionProcessor(this.sapphireConnection.getConnectionId());
                    actionProcessor.processActionClass(actionClass, actionProps);
                    saved = true;
                }
                catch (Exception e) {
                    throw new SapphireException("Failed to set document state. Exception: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.sapphireConnection.getConnectionId())), e);
                }
            } else {
                status = "REFRESH";
                HashMap<String, String> transmap = new HashMap<String, String>();
                transmap.put("lockedby", document.getLockedby());
                transmap.put("operation", operation);
                returnmessage = this.trans.translate("Document locked by [lockedby]. [operation] operation aborted!", transmap);
            }
            PropertyList documentObjects = new PropertyList();
            documentObjects.setProperty("saved", saved ? "Y" : "N");
            documentObjects.setProperty("status", status);
            documentObjects.setProperty("returnmessage", returnmessage);
            documentObjects.setProperty("document", document.getDocumentProperties());
            responseData.put("jsonreturn", documentObjects.toJSONString(false));
            responseData.put("userconfig_efm_" + hostpageid + "_lastdocumentid", document.getDocumentid());
            responseData.put("userconfig_efm_" + hostpageid + "_lastdocumentversionid", document.getDocumentversionid());
            this.debugReturn(requestData, documentObjects);
        }
        catch (Exception e) {
            this.logger.error("Error processing document. Exception: " + e.getMessage(), e);
            PropertyList documentObjects = new PropertyList();
            documentObjects.setProperty("status", "E");
            documentObjects.setProperty("returnmessage", "Failed to process document request '" + operation + "' - documentRequest. Exception: " + e.getMessage());
            responseData.put("jsonreturn", documentObjects.toJSONString(false));
            this.debugReturn(requestData, documentObjects);
        }
        return responseData;
    }
}

