/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.documents.gwt.server;

import com.labvantage.sapphire.modules.documents.gwt.server.EvaluateField;
import com.labvantage.sapphire.modules.documents.gwt.server.LoadAttachments;
import com.labvantage.sapphire.modules.documents.gwt.server.LoadDocumentAudit;
import com.labvantage.sapphire.modules.documents.gwt.server.LoadFieldAudit;
import com.labvantage.sapphire.modules.documents.gwt.server.LoadForms;
import com.labvantage.sapphire.modules.documents.gwt.server.LoadRefTypeValues;
import com.labvantage.sapphire.modules.documents.gwt.server.LoadSDIData;
import com.labvantage.sapphire.modules.documents.gwt.server.LoadThumbnail;
import com.labvantage.sapphire.modules.documents.gwt.server.LoadValues;
import com.labvantage.sapphire.modules.documents.gwt.server.NewForm;
import com.labvantage.sapphire.modules.documents.gwt.server.OpenDocument;
import com.labvantage.sapphire.modules.documents.gwt.server.ProcessDocument;
import com.labvantage.sapphire.modules.documents.gwt.server.ProcessDocuments;
import com.labvantage.sapphire.modules.documents.gwt.server.RSetPing;
import com.labvantage.sapphire.modules.documents.gwt.server.Rebind;
import com.labvantage.sapphire.modules.documents.gwt.server.SaveReviewItems;
import com.labvantage.sapphire.modules.documents.gwt.server.Search;
import com.labvantage.sapphire.modules.documents.gwt.server.SetDocumentState;
import com.labvantage.sapphire.modules.documents.gwt.server.ValidateDocument;
import com.labvantage.sapphire.modules.documents.gwt.server.ValidateField;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import com.labvantage.sapphire.pageelements.gwt.shared.DocumentConstants;
import java.util.HashMap;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;

public class DocumentRequest
extends PropertyHandler
implements DocumentConstants {
    public static final String SYSUSERID = "sysuserid";
    public static final String LOGNAME = "DOCUMENTREQUEST";

    @Override
    public void processProperties(HashMap props) throws SapphireException {
        try {
            Logger logger = new Logger(this.sapphireConnection.getConnectionId());
            logger.setLoggerName(LOGNAME);
            String requestCommand = (String)props.get("requestcommand");
            logger.info("Received " + requestCommand + " request");
            PropertyList requestData = new PropertyList(new JSONObject((String)props.get(requestCommand)));
            boolean debug = requestData.getProperty("debug", "N").equals("Y");
            String hostpageid = requestData.getProperty("hostpageid");
            requestData.setProperty("requestcommand", requestCommand);
            if (debug) {
                logger.info("REQUESTDATA:\n" + requestData.toXMLString());
            }
            props.put(SYSUSERID, this.sapphireConnection.getSysuserId());
            long start = System.currentTimeMillis();
            if ("loadsdidata".equalsIgnoreCase(requestCommand)) {
                LoadSDIData command = new LoadSDIData(this.sapphireConnection, debug);
                props.putAll(command.execute(requestData));
            } else if ("loadattachments".equalsIgnoreCase(requestCommand)) {
                LoadAttachments command = new LoadAttachments(this.sapphireConnection, debug);
                props.putAll(command.execute(requestData));
            } else if ("loadforms".equalsIgnoreCase(requestCommand)) {
                LoadForms command = new LoadForms(this.sapphireConnection, debug);
                props.putAll(command.execute(requestData));
            } else if ("loadthumbnail".equalsIgnoreCase(requestCommand)) {
                LoadThumbnail command = new LoadThumbnail(this.sapphireConnection, debug);
                props.putAll(command.execute(requestData));
            } else if ("loadreftypevalues".equalsIgnoreCase(requestCommand)) {
                LoadRefTypeValues command = new LoadRefTypeValues(this.sapphireConnection, debug);
                props.putAll(command.execute(requestData));
            } else if ("loadvalues".equalsIgnoreCase(requestCommand)) {
                LoadValues command = new LoadValues(this.sapphireConnection, debug);
                props.putAll(command.execute(requestData));
            } else if ("newform".equalsIgnoreCase(requestCommand)) {
                NewForm command = new NewForm(this.sapphireConnection, debug);
                props.putAll(command.execute(requestData));
                props.put("userconfig_efm_" + hostpageid + "_lastrequest", requestData.getProperty("searchform", "N").equals("Y") ? "searchform" : "newform");
            } else if ("opendocument".equalsIgnoreCase(requestCommand)) {
                OpenDocument command = new OpenDocument(this.sapphireConnection, debug);
                props.putAll(command.execute(requestData));
                props.put("userconfig_efm_" + hostpageid + "_lastrequest", "opendocument");
            } else if ("loaddocumentaudit".equalsIgnoreCase(requestCommand)) {
                LoadDocumentAudit command = new LoadDocumentAudit(this.sapphireConnection, debug);
                props.putAll(command.execute(requestData));
            } else if ("loadfieldaudit".equalsIgnoreCase(requestCommand)) {
                LoadFieldAudit command = new LoadFieldAudit(this.sapphireConnection, debug);
                props.putAll(command.execute(requestData));
            } else if ("validatefield".equalsIgnoreCase(requestCommand)) {
                ValidateField command = new ValidateField(this.sapphireConnection, debug);
                props.putAll(command.execute(requestData));
            } else if ("rebind".equalsIgnoreCase(requestCommand)) {
                Rebind command = new Rebind(this.sapphireConnection, debug);
                props.putAll(command.execute(requestData));
            } else if ("evaluatefield".equalsIgnoreCase(requestCommand)) {
                EvaluateField command = new EvaluateField(this.sapphireConnection, debug);
                props.putAll(command.execute(requestData));
            } else if ("check".equalsIgnoreCase(requestCommand)) {
                ValidateDocument command = new ValidateDocument(this.sapphireConnection, debug);
                props.putAll(command.execute(requestData));
            } else if ("processdocuments".equalsIgnoreCase(requestCommand)) {
                ProcessDocuments command = new ProcessDocuments(this.sapphireConnection, debug);
                props.putAll(command.execute(requestData));
            } else if ("draft".equalsIgnoreCase(requestCommand) || "submit".equalsIgnoreCase(requestCommand) || "resubmit".equalsIgnoreCase(requestCommand) || "followup".equalsIgnoreCase(requestCommand) || "reconcile".equalsIgnoreCase(requestCommand) || "approve".equalsIgnoreCase(requestCommand) || "reject".equalsIgnoreCase(requestCommand) || "copy".equalsIgnoreCase(requestCommand) || "documentmgr".equalsIgnoreCase(requestCommand) || "newversion".equalsIgnoreCase(requestCommand) || "pending".equalsIgnoreCase(requestCommand) || "save".equalsIgnoreCase(requestCommand)) {
                ProcessDocument command = new ProcessDocument(this.sapphireConnection, debug);
                props.putAll(command.execute(requestData));
                props.put("userconfig_efm_" + hostpageid + "_lastrequest", "opendocument");
            } else if ("cancel".equalsIgnoreCase(requestCommand) || "lock".equalsIgnoreCase(requestCommand) || "unlock".equalsIgnoreCase(requestCommand) || "assign".equalsIgnoreCase(requestCommand)) {
                SetDocumentState command = new SetDocumentState(this.sapphireConnection, debug);
                props.putAll(command.execute(requestData));
            } else if ("savereviewitems".equalsIgnoreCase(requestCommand)) {
                SaveReviewItems command = new SaveReviewItems(this.sapphireConnection, debug);
                props.putAll(command.execute(requestData));
            } else if ("rsetping".equalsIgnoreCase(requestCommand)) {
                RSetPing command = new RSetPing(this.sapphireConnection, debug);
                props.putAll(command.execute(requestData));
            } else if ("search".equalsIgnoreCase(requestCommand)) {
                Search command = new Search(this.sapphireConnection, debug);
                props.putAll(command.execute(requestData));
                props.put("userconfig_efm_" + hostpageid + "_lastrequest", "search");
            } else if ("setuserconfig".equalsIgnoreCase(requestCommand)) {
                for (String setting : requestData.keySet()) {
                    props.put("userconfig_efm_" + hostpageid + "_" + setting, requestData.getProperty(setting));
                }
            } else if ("clearrset".equalsIgnoreCase(requestCommand)) {
                RSetPing rsetPing = new RSetPing(this.sapphireConnection, debug);
                rsetPing.clearRSet(requestData.getProperty("rsetid"));
            } else {
                logger.error("Unrecognized request in DocumentRequest");
            }
            logger.info("Operation: " + requestCommand + " took " + (System.currentTimeMillis() - start) + " ms");
        }
        catch (Exception e) {
            this.logError("Error processing request: " + e.getMessage(), e);
        }
    }
}

