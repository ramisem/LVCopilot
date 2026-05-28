/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.documents.gwt.server;

import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.modules.documents.Document;
import com.labvantage.sapphire.modules.documents.Field;
import com.labvantage.sapphire.modules.documents.FieldSetter;
import com.labvantage.sapphire.modules.documents.Form;
import com.labvantage.sapphire.modules.documents.FormGroup;
import com.labvantage.sapphire.modules.documents.gwt.server.BaseDocumentValidationCommand;
import com.labvantage.sapphire.modules.documents.gwt.server.DocumentCommand;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.groovy.ProcessingUtil;
import com.labvantage.sapphire.util.json.JSONUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.accessor.ActionProcessor;
import sapphire.error.ErrorHandler;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ProcessDocument
extends BaseDocumentValidationCommand
implements DocumentCommand {
    public ProcessDocument(SapphireConnection sapphireConnection, boolean debug) {
        super(sapphireConnection, debug);
    }

    @Override
    public HashMap execute(PropertyList requestData) {
        String operation = requestData.getProperty("requestcommand");
        String formid = requestData.getProperty("formid");
        String formversionid = requestData.getProperty("formversionid");
        String formletid = requestData.getProperty("formletid");
        String formletversionid = requestData.getProperty("formletversionid");
        String worksheetitemid = requestData.getProperty("worksheetitemid");
        String worksheetitemversionid = requestData.getProperty("worksheetitemversionid");
        String rsetid = requestData.getProperty("rsetid");
        String documentid = requestData.getProperty("documentid");
        String documentversionid = requestData.getProperty("documentversionid", "1");
        boolean ddedata = requestData.getProperty("ddedata", "N").equals("Y");
        String hostpageid = requestData.getProperty("hostpageid");
        HashMap<String, Object> responseData = new HashMap<String, Object>();
        boolean completePartialEntries = false;
        boolean formOnly = requestData.getProperty("formonly", "N").equals("Y");
        String taskdefid = requestData.getProperty("taskdefid");
        String taskdefversionid = requestData.getProperty("taskdefversionid");
        String taskdefvariantid = requestData.getProperty("taskdefvariantid");
        String stepid = requestData.getProperty("stepid");
        String variableid = requestData.getProperty("variableid");
        try {
            Form form;
            Document document;
            boolean resetFieldInstances = false;
            if (rsetid.length() > 0) {
                this.logger.info("Processing document with " + operation + " operation - rsetid=" + rsetid);
                document = Document.getInstance(this.sapphireConnection, rsetid, true, this.debug);
                form = document.getForm();
                form.setOverrides(requestData.getPropertyList("formoverrides"));
                resetFieldInstances = !(!document.getDocumentStatus().equals("DR") && !document.getDocumentStatus().equals("PD") || !operation.equalsIgnoreCase("draft") && !operation.equalsIgnoreCase("submit") || !requestData.getProperty("missinginstances", "N").equals("Y"));
            } else if (documentid.length() > 0) {
                this.logger.info("Processing document with " + operation + " operation - documentid=" + documentid);
                document = Document.getInstance(this.sapphireConnection, documentid, documentversionid, this.evalTokens(requestData, EncryptDecrypt.unobfsql(requestData.getProperty("restrictivewhere"))), false, this.debug);
                form = document.getForm();
                form.setOverrides(requestData.getPropertyList("formoverrides"));
            } else {
                this.logger.info("Processing new document with " + operation + " operation");
                form = formid.length() > 0 ? Form.getInstance(this.sapphireConnection, formid, formversionid, this.debug) : (formletid.length() > 0 ? Form.getInstance(this.sapphireConnection, formletid, formletversionid, "") : (worksheetitemid.length() > 0 ? Form.getInstance(this.sapphireConnection, worksheetitemid, worksheetitemversionid, "", "") : Form.getInstance(this.sapphireConnection, taskdefid, taskdefversionid, taskdefvariantid, stepid, null)));
                form.setOverrides(requestData.getPropertyList("formoverrides"));
                document = Document.getInstance(this.sapphireConnection, form, this.debug);
                resetFieldInstances = (operation.equalsIgnoreCase("draft") || operation.equalsIgnoreCase("submit")) && requestData.getProperty("missinginstances", "N").equals("Y");
            }
            HashMap fieldMap = ProcessingUtil.createFieldMap(this.sapphireConnection, form, requestData.getPropertyList("fieldvalues"), "enteredtext", resetFieldInstances);
            PropertyListCollection fieldValues = new PropertyListCollection();
            boolean validate = operation.equalsIgnoreCase("copy") || operation.equalsIgnoreCase("newversion") || (form.isValidateOnSave() || operation.equalsIgnoreCase("submit") || operation.equalsIgnoreCase("reconcile") || operation.equalsIgnoreCase("approve")) && !operation.equalsIgnoreCase("reject") && !operation.equalsIgnoreCase("pending") && !operation.equalsIgnoreCase("save");
            boolean forceValidate = operation.equalsIgnoreCase("approve") && !form.isApproveInvalidData();
            this.logger.info("Validating fields...");
            boolean validationErrors = this.inspectFields(form, fieldMap, operation, validate, forceValidate, completePartialEntries, true, fieldValues);
            PropertyListCollection groupValues = new PropertyListCollection();
            if (!operation.equalsIgnoreCase("reject")) {
                this.logger.info("Validating groups...");
                if (this.inspectGroups(form, fieldMap, groupValues)) {
                    validationErrors = true;
                }
            }
            if (operation.equalsIgnoreCase("copy")) {
                document.setDocumentid("");
                document.setDocumentversionid("1");
                document.setDocumentStatus("DR");
            } else if (operation.equalsIgnoreCase("newversion")) {
                document.setDocumentversionid(String.valueOf(Integer.parseInt(document.getDocumentversionid()) + 1));
                document.setDocumentStatus("DR");
            }
            document.setStatusmessage(requestData.getProperty("statusmessage"));
            document.setReviewitems(requestData.getCollection("reviewitems"));
            boolean unAcknowledgedErrors = fieldMap.get("__unacknowledgederrors") != null;
            boolean saved = true;
            if (!form.isTransientForm() && !formOnly) {
                if (requestData.getProperty("pagehtml").length() == 0 && requestData.getProperty("generatepagehtml").equals("Y")) {
                    requestData.setProperty("pagehtml", Document.generatePageThumbnail(form, fieldMap));
                }
                this.logger.info("Saving document...");
                saved = document.save(operation, fieldValues, fieldMap, groupValues, ddedata, validationErrors, unAcknowledgedErrors, requestData);
            } else {
                document.setDocumentStatus("PP");
            }
            String returnmessage = document.getReturnmessage();
            String status = resetFieldInstances ? "REFRESH" : "OK";
            HashMap output = new HashMap();
            PropertyList outputProps = new PropertyList();
            if (!formOnly && saved && returnmessage.length() == 0 && (form.hasProcessing() || form.hasAutoSaveFields())) {
                HashMap<String, HashMap> formMap = form.getFormMap();
                this.logger.info("Preparing data for processing...");
                HashMap fileFields = form.getFileFieldMap();
                if (fileFields != null) {
                    for (String fieldid : fileFields.keySet()) {
                        Field field = (Field)fieldMap.get(fieldid);
                        if (field == null) continue;
                        FieldSetter.setConnectionid(field, this.sapphireConnection.getConnectionId());
                        FieldSetter.setDocument(field, document.getDocumentid(), document.getDocumentversionid());
                    }
                }
                HashMap groups = formMap.get("groups");
                HashMap<String, FormGroup> processingGroupMap = new HashMap<String, FormGroup>();
                for (int i = 0; i < groupValues.size(); ++i) {
                    PropertyList groupValue = groupValues.getPropertyList(i);
                    FormGroup formGroup = new FormGroup((PropertyList)groups.get(groupValue.getProperty("groupid")));
                    formGroup.setValue(groupValue.getProperty("value"));
                    PropertyListCollection members = formGroup.getMembers();
                    ArrayList memberList = new ArrayList();
                    for (int j = 0; j < members.size(); ++j) {
                        memberList.add(fieldMap.get(members.getPropertyList(j).getProperty("fieldid")));
                    }
                    formGroup.setMembers(memberList);
                    processingGroupMap.put(groupValue.getProperty("groupid"), formGroup);
                }
                HashMap<String, Serializable> bindings = new HashMap<String, Serializable>();
                bindings.put("params", requestData.getPropertyList("paramvalues"));
                bindings.put("fields", fieldMap);
                bindings.put("groups", processingGroupMap);
                bindings.put("output", output);
                if (document.getDocumentStatus().equals("PP") || document.getDocumentStatus().equals("DN") && form.getFormPropertyList().getProperty("allowdoneediting", "N").equals("Y")) {
                    bindings.put("initialProcessing", new Boolean(document.getDocumentStatus().equals("PP")));
                    bindings.put("subsequentProcessing", new Boolean(document.getDocumentStatus().equals("DN")));
                    if (document.processScript(form, bindings)) {
                        for (String key : output.keySet()) {
                            outputProps.setProperty(key, output.get(key).toString());
                        }
                    } else {
                        responseData.put("ERRORHANDLER", new ErrorHandler("", "FAILURE", ""));
                        returnmessage = document.getReturnmessage();
                        status = "REFRESH";
                    }
                }
                if (form.hasAutoSaveFields() && (document.getDocumentStatus().equals("PP") || document.getDocumentStatus().equals("DN"))) {
                    if (document.processAutoSave(fieldValues, fieldMap, form, operation, requestData)) {
                        if (document.getDocumentStatus().equals("PP")) {
                            PropertyList setStatusProps = new PropertyList();
                            setStatusProps.setProperty("documentid", document.getDocumentid());
                            setStatusProps.setProperty("documentversionid", document.getDocumentversionid());
                            setStatusProps.setProperty("documentstatus", form.isLockondone() ? "LK" : "DN");
                            ActionProcessor ap = new ActionProcessor(this.sapphireConnection.getConnectionId());
                            ap.processAction("SetDocumentStatus", "1", setStatusProps);
                            document.setDocumentStatus(setStatusProps.getProperty("documentstatus"));
                        }
                    } else {
                        responseData.put("ERRORHANDLER", new ErrorHandler("", "FAILURE", ""));
                        returnmessage = document.getReturnmessage();
                        status = "REFRESH";
                    }
                }
                if (fileFields != null) {
                    for (String fieldid : fileFields.keySet()) {
                        Field field = (Field)fieldMap.get(fieldid);
                        if (field == null || !field.isFileCreated()) continue;
                        FieldSetter.deleteTempFile(field);
                    }
                }
            }
            if (form.isTransientForm() || formOnly) {
                status = "TRANSIENT";
                document.setDocumentStatus("");
            }
            PropertyList documentObjects = new PropertyList();
            documentObjects.setProperty("saved", saved ? "Y" : "N");
            if (document.getDocumentStatus().equals("EXISTS")) {
                status = "EXISTS";
            }
            if (returnmessage.length() == 0 && document.getDocumentStatus().equals("PD")) {
                status = "REFRESH";
            }
            documentObjects.setProperty("status", status);
            documentObjects.setProperty("returnmessage", returnmessage);
            PropertyList documentProps = document.getDocumentProperties();
            documentProps.setProperty("userrole", this.sapphireConnection.getSysuserId().equals(document.getSysuserid1()) ? "User1" : (this.sapphireConnection.getSysuserId().equals(document.getSysuserid2()) || ddedata ? "User2" : "User3"));
            documentProps.setProperty("ddedata", ddedata ? "Y" : "N");
            documentProps.setProperty("tempdocumentid", requestData.getProperty("tempdocumentid"));
            documentObjects.setProperty("document", documentProps);
            documentObjects.setProperty("fieldvalues", fieldValues);
            documentObjects.setProperty("groupvalues", groupValues);
            DataSet attachments = document.getAttachments(true, requestData.getProperty("tempdocumentid"));
            documentObjects.setProperty("attachments", JSONUtil.toJSONString(attachments != null ? attachments : new DataSet()));
            documentObjects.setProperty("output", outputProps);
            documentObjects.setProperty("stepid", stepid);
            documentObjects.setProperty("variableid", variableid);
            responseData.put("jsonreturn", documentObjects.toJSONString(false));
            this.debugReturn(requestData, documentObjects);
            responseData.put("userconfig_efm_" + hostpageid + "_lastdocumentid", document.getDocumentid());
            responseData.put("userconfig_efm_" + hostpageid + "_lastdocumentversionid", document.getDocumentversionid());
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

