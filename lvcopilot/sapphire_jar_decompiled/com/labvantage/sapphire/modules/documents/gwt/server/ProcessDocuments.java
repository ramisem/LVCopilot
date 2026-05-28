/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.documents.gwt.server;

import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.modules.documents.gwt.server.BaseDocumentCommand;
import com.labvantage.sapphire.modules.documents.gwt.server.DocumentCommand;
import com.labvantage.sapphire.modules.documents.gwt.server.OpenDocument;
import com.labvantage.sapphire.modules.documents.gwt.server.SetDocumentState;
import com.labvantage.sapphire.services.SapphireConnection;
import java.util.HashMap;
import org.json.JSONObject;
import org.json.JSONTokener;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ProcessDocuments
extends BaseDocumentCommand
implements DocumentCommand {
    public ProcessDocuments(SapphireConnection sapphireConnection, boolean debug) {
        super(sapphireConnection, debug);
    }

    @Override
    public HashMap execute(PropertyList requestData) {
        String status;
        PropertyList documentRequestData;
        String operation = requestData.getProperty("operation");
        PropertyListCollection documents = requestData.getCollection("documents");
        boolean error = false;
        for (int i = 0; i < documents.size(); ++i) {
            PropertyList documentProps = documents.getPropertyList(i);
            try {
                OpenDocument openDocument = new OpenDocument(this.sapphireConnection, this.debug);
                documentRequestData = new PropertyList();
                documentRequestData.setProperty("documentid", documentProps.getProperty("documentid"));
                documentRequestData.setProperty("documentversionid", documentProps.getProperty("documentversionid"));
                documentRequestData.setProperty("ddedata", documentProps.getProperty("ddeflag"));
                documentRequestData.setProperty("preview", "Y");
                documentRequestData.setProperty("restrictivewhere", EncryptDecrypt.unobfsql(requestData.getProperty("restrictivewhere")));
                HashMap responseData = openDocument.execute(documentRequestData);
                JSONObject jsonObject = new JSONObject(new JSONTokener((String)responseData.get("jsonreturn")));
                PropertyList response = new PropertyList(jsonObject);
                PropertyList document = response.getPropertyList("document");
                if ((operation.equals("cancel") || operation.equals("lock") || operation.equals("unlock") || operation.equals("assign")) && document.getProperty("documentmanager", "N").equals("N")) {
                    documentProps.setProperty("status", "E");
                    documentProps.setProperty("returnmessage", this.trans.translate("Insufficient privileges to " + operation + " document!"));
                    error = true;
                    continue;
                }
                if (document.getProperty("lockedby").length() > 0) {
                    documentProps.setProperty("status", "E");
                    documentProps.setProperty("returnmessage", this.trans.translate("Document locked by " + document.getProperty("lockedby")));
                    error = true;
                    continue;
                }
                if (operation.equals("cancel") && (document.getProperty("documentstatus").equals("LK") || document.getProperty("documentstatus").equals("CN"))) {
                    documentProps.setProperty("status", "E");
                    documentProps.setProperty("returnmessage", this.trans.translate("Document status is locked or cancelled!"));
                    error = true;
                    continue;
                }
                if (operation.equals("lock") && (document.getProperty("documentstatus").equals("LK") || document.getProperty("documentstatus").equals("CN"))) {
                    documentProps.setProperty("status", "E");
                    documentProps.setProperty("returnmessage", this.trans.translate("Document status is locked or cancelled!"));
                    error = true;
                    continue;
                }
                if (operation.equals("unlock") && (document.getProperty("documentstatus").equals("DN") || document.getProperty("documentstatus").equals("CN"))) {
                    documentProps.setProperty("status", "E");
                    documentProps.setProperty("returnmessage", this.trans.translate("Document status is done or cancelled!"));
                    error = true;
                    continue;
                }
                if (operation.equals("assign") && (document.getProperty("documentstatus").equals("LK") || document.getProperty("documentstatus").equals("DN") || document.getProperty("documentstatus").equals("CN"))) {
                    documentProps.setProperty("status", "E");
                    documentProps.setProperty("returnmessage", this.trans.translate("Document status is locked, done or cancelled!"));
                    error = true;
                    continue;
                }
                documentProps.setProperty("status", "OK");
                continue;
            }
            catch (Exception e) {
                documentProps.setProperty("status", "E");
                documentProps.setProperty("returnmessage", this.trans.translate("Document failed to open!"));
                error = true;
            }
        }
        String string = status = error ? "E" : "OK";
        if (!error) {
            for (int i = 0; i < documents.size(); ++i) {
                PropertyList documentProps = documents.getPropertyList(i);
                try {
                    documentRequestData = new PropertyList();
                    documentRequestData.setProperty("requestcommand", operation);
                    documentRequestData.setProperty("documentid", documentProps.getProperty("documentid"));
                    documentRequestData.setProperty("documentversionid", documentProps.getProperty("documentversionid"));
                    documentRequestData.setProperty("preview", "Y");
                    documentRequestData.setProperty("restrictivewhere", EncryptDecrypt.unobfsql(requestData.getProperty("restrictivewhere")));
                    documentRequestData.setProperty("sysuserid1", requestData.getProperty("sysuserid1"));
                    documentRequestData.setProperty("auditreason", requestData.getProperty("auditreason"));
                    documentRequestData.setProperty("auditactivity", requestData.getProperty("auditactivity"));
                    documentRequestData.setProperty("auditsignedflag", requestData.getProperty("auditsignedflag"));
                    documentRequestData.setProperty("auditdt", requestData.getProperty("auditdt"));
                    SetDocumentState setDocumentState = new SetDocumentState(this.sapphireConnection, this.debug);
                    HashMap responseData = setDocumentState.execute(documentRequestData);
                    JSONObject jsonObject = new JSONObject(new JSONTokener((String)responseData.get("jsonreturn")));
                    PropertyList response = new PropertyList(jsonObject);
                    if (response.getProperty("status").equals("OK")) continue;
                    documentProps.setProperty("status", "E");
                    documentProps.setProperty("returnmessage", response.getProperty("returnmessage"));
                    continue;
                }
                catch (Exception e) {
                    documentProps.setProperty("status", "E");
                    documentProps.setProperty("returnmessage", this.trans.translate("SetDocumentState failed!"));
                    error = true;
                }
            }
        }
        PropertyList returnData = new PropertyList();
        returnData.setProperty("documents", documents);
        returnData.setProperty("status", status);
        HashMap<String, String> responseData = new HashMap<String, String>();
        responseData.put("jsonreturn", returnData.toJSONString(false));
        return responseData;
    }
}

