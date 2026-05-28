/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.documents.gwt.server;

import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.modules.documents.Document;
import com.labvantage.sapphire.modules.documents.DocumentUserException;
import com.labvantage.sapphire.modules.documents.Field;
import com.labvantage.sapphire.modules.documents.FieldSetter;
import com.labvantage.sapphire.modules.documents.Form;
import com.labvantage.sapphire.modules.documents.gwt.server.BaseDocumentCommand;
import com.labvantage.sapphire.modules.documents.gwt.server.DocumentCommand;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import com.labvantage.sapphire.util.json.JSONUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class OpenDocument
extends BaseDocumentCommand
implements DocumentCommand {
    public OpenDocument(SapphireConnection sapphireConnection, boolean debug) {
        super(sapphireConnection, debug);
    }

    @Override
    public HashMap execute(PropertyList requestData) {
        String documentid = requestData.getProperty("documentid");
        String documentversionid = requestData.getProperty("documentversionid");
        boolean preview = requestData.getProperty("preview", "N").equals("Y");
        boolean ddedata = requestData.getProperty("ddedata", "N").equals("Y");
        String hostpageid = requestData.getProperty("hostpageid");
        HashMap<String, String> responseData = new HashMap<String, String>();
        try {
            int i;
            HashMap<String, String> transmap;
            boolean locked;
            this.clearRSet(requestData.getProperty("rsetid"));
            Document document = Document.getInstance(this.sapphireConnection, documentid, documentversionid, this.evalTokens(requestData, EncryptDecrypt.unobfsql(requestData.getProperty("restrictivewhere"))), !preview, this.debug);
            this.applyRoles(requestData, document);
            if (document.getSysuserid2().length() > 0 && this.sapphireConnection.getSysuserId().equals(document.getSysuserid2())) {
                ddedata = true;
            }
            String returnmessage = "";
            Form form = document.getForm();
            if (form.isTrainingrequired() && !form.isOverrideallowed() && !form.isTrainingexists() && !document.isDocumentManager()) {
                throw new DocumentUserException(this.trans.translate("You do not have the necessary training/certification to use the following form: ") + form.getFormid() + (requestData.getProperty("showformversions").equals("Y") ? " (v" + form.getFormversionid() + ")" : "") + ".");
            }
            if (form.isTrainingrequired() && form.isOverrideallowed() && !form.isTrainingexists() && requestData.getProperty("overridetraining", "N").equals("N") && !document.isDocumentManager()) {
                throw new DocumentUserException(this.trans.translate("You do not have the necessary training/certification to use the following form: ") + form.getFormid() + (requestData.getProperty("showformversions").equals("Y") ? " (v" + form.getFormversionid() + ")" : "") + ".<br/><br/>" + this.trans.translate("You have the option to override these requirements. Would you like to override the training/certification requirement?"), "DUE_Override");
            }
            boolean bl = locked = document.getLockedby().length() > 0;
            if (preview || locked) {
                document.setMode(0);
            } else if ("DR".equals(document.getDocumentStatus()) && document.getSysuserid1().length() > 0 && !this.sapphireConnection.getSysuserId().equals(document.getSysuserid1())) {
                document.setMode(0);
            } else if ("PD".equals(document.getDocumentStatus()) && document.getSysuserid1().length() > 0 && !this.sapphireConnection.getSysuserId().equals(document.getSysuserid1())) {
                document.setMode(0);
            } else if ("SM".equals(document.getDocumentStatus()) && this.sapphireConnection.getSysuserId().equals(document.getSysuserid1())) {
                document.setMode(0);
            } else {
                if ("SM".equals(document.getDocumentStatus()) && !ddedata) {
                    throw new DocumentUserException("", "DUE_ConfirmLoad", document.getDocumentData());
                }
                if ("DDEDR".equals(document.getDocumentStatus()) && !this.sapphireConnection.getSysuserId().equals(document.getSysuserid2()) && !document.hasOpenFollowups()) {
                    document.setMode(0);
                } else if ("DDESM".equals(document.getDocumentStatus())) {
                    document.setMode(0);
                } else if (!(!"PA".equals(document.getDocumentStatus()) || document.isApprovable() || document.hasOpenFollowups() || document.isReconcilable() && document.hasFieldErrors())) {
                    document.setMode(0);
                } else if ("PR".equals(document.getDocumentStatus()) && !document.isReconcilable() && !document.hasOpenFollowups()) {
                    document.setMode(0);
                } else if ("RJ".equals(document.getDocumentStatus()) && !document.hasOpenFollowups()) {
                    document.setMode(0);
                } else if ("DN".equals(document.getDocumentStatus()) && !document.isDocumentManager() && !document.hasOpenFollowups() && form.getFormPropertyList().getProperty("allowdoneediting", "N").equals("N")) {
                    document.setMode(0);
                } else if ("ER".equals(document.getDocumentStatus()) && !document.isDocumentManager() && !this.sapphireConnection.getSysuserId().equals(document.getSysuserid1())) {
                    document.setMode(0);
                } else if ("CN".equals(document.getDocumentStatus()) || "LK".equals(document.getDocumentStatus())) {
                    document.setMode(0);
                }
            }
            document.sortFieldValues(form.getFields());
            if ("DR".equals(document.getDocumentStatus()) && !document.isDocumentManager() && document.getSysuserid1().length() > 0 && !this.sapphireConnection.getSysuserId().equals(document.getSysuserid1())) {
                transmap = new HashMap<String, String>();
                transmap.put("sysuserid1", document.getSysuserid1());
                returnmessage = this.trans.translate("User '[sysuserid1]' has not submitted this document - you may not edit this document!", transmap);
            } else if ("PD".equals(document.getDocumentStatus()) && !document.isDocumentManager() && document.getSysuserid1().length() > 0 && !this.sapphireConnection.getSysuserId().equals(document.getSysuserid1())) {
                transmap = new HashMap();
                transmap.put("sysuserid1", document.getSysuserid1());
                returnmessage = this.trans.translate("This document is assigned to '[sysuserid1]' - you may not edit this document!", transmap);
            }
            if (document.isDocumentManager()) {
                preview = false;
                document.setMode(1);
            }
            if (document.isViewMode() && document.getRsetid().length() > 0) {
                this.clearRSet(document.getRsetid());
                document.setRsetid("");
            }
            PropertyList documentObjects = new PropertyList();
            PropertyList formProps = form.getFormPropertyList();
            documentObjects.setProperty("returnmessage", returnmessage);
            documentObjects.setProperty("form", formProps);
            PropertyListCollection pages = form.getPages();
            documentObjects.setProperty("pages", pages);
            PropertyListCollection groups = form.getGroups();
            documentObjects.setProperty("groups", groups);
            documentObjects.setProperty("groupvalues", document.getGroupValues());
            PropertyListCollection sections = form.getSections();
            documentObjects.setProperty("sections", sections);
            PropertyListCollection datasources = form.getDatasources();
            documentObjects.setProperty("datasources", datasources);
            PropertyListCollection elements = form.getElements();
            documentObjects.setProperty("elements", elements);
            PropertyListCollection fields = form.getFields();
            documentObjects.setProperty("fields", fields);
            PropertyListCollection fieldValues = document.getFieldValues();
            documentObjects.setProperty("fieldvalues", fieldValues);
            documentObjects.setProperty("attachments", JSONUtil.toJSONString(document.getAttachments(false)));
            HashMap<String, Serializable> bindings = new HashMap<String, Serializable>();
            HashMap fieldValueMap = document.getFieldValueMap(ddedata);
            bindings.put("document", document.getDocumentRowMap());
            bindings.put("params", Document.getDocumentParams(document.getDocumentObjects()));
            bindings.put("fields", fieldValueMap);
            boolean worksheet = formProps.getProperty("worksheet", "N").equals("Y");
            for (i = 0; i < fields.size(); ++i) {
                PropertyList instance;
                PropertyList field = fields.getPropertyList(i);
                PropertyList fieldValue = fieldValues.getPropertyList(i);
                PropertyListCollection instances = fieldValue.getCollection("instances");
                boolean repeatable = field.getProperty("repeatable", "N").equals("Y");
                if (!preview && !locked) {
                    String defaultValue = field.getProperty("defaultvalue");
                    defaultValue = defaultValue.startsWith("$G{") && defaultValue.endsWith("}") ? this.evalGroovyExpression(field.getProperty("fieldid"), bindings, defaultValue, true, false, true, true, true, false) : this.evalTokens(requestData, defaultValue);
                    if (repeatable) {
                        fieldValue.setProperty("defaultvalue", defaultValue);
                        String sectionid = field.getProperty("sectionid");
                        for (int j = 0; j < instances.size(); ++j) {
                            PropertyList instance2 = instances.getPropertyList(j);
                            bindings.put("sectioninstance", Integer.valueOf(j));
                            bindings.put("sectionindex", Integer.valueOf(j));
                            bindings.put("fieldinstance", (Serializable)fieldValueMap.get(sectionid + "_" + instance2.getProperty("fieldinstance")));
                            this.evalGroovyYNProperty(field, instance2, "processingfield", bindings);
                            this.evalGroovyYNProperty(field, instance2, "mandatory", bindings);
                            this.evalGroovyYNProperty(field, instance2, "readonly", bindings);
                            this.evalGroovyYNProperty(field, instance2, "visible", bindings);
                            this.evalGroovyProperty(field, instance2, "values", bindings);
                            this.evalGroovyProperty(field, instance2, "valuesqueryfrom", bindings);
                            this.evalGroovyProperty(field, instance2, "valuesquerywhere", bindings);
                            this.evalGroovyProperty(field, instance2, "sql", bindings);
                            this.evalGroovyProperty(field, instance2, "sdcid", bindings);
                            this.evalGroovyProperty(field, instance2, "reftypeid", bindings);
                            this.evalGroovyProperty(field, instance2, "instrumentid", bindings);
                            if (field.getProperty("valuerule").length() > 0 && !document.isViewMode()) {
                                String value = this.evalGroovyExpression(field.getProperty("fieldid"), bindings, field.getProperty("valuerule"), true, false, true, true, true, false);
                                instance2.setProperty("enteredtext", value);
                                FieldSetter.setValue((Field)fieldValueMap.get(field.getProperty("fieldid")), instance2);
                            }
                            if (worksheet && field.getProperty("readonly").equals("C") && document.getDocumentStatus().length() > 0 || document.isViewMode()) {
                                field.setProperty("dynamic", "N");
                                field.setProperty("fieldevaluation", "N");
                                field.setProperty("fieldvalidation", "N");
                                if (field.getProperty("type").equals("dropdown")) {
                                    field.setProperty("values", field.getProperty("values").length() > 0 ? field.getProperty("values") + ";" + instance2.getProperty("enteredtext") : instance2.getProperty("enteredtext"));
                                }
                            }
                            if (field.getProperty("autosave").length() > 0 && !instance2.containsKey("binding")) {
                                instance2.setProperty("bound", "N");
                            }
                            if (!ddedata) continue;
                            this.setupDDEDataInstance(document, field, instance2, defaultValue);
                        }
                    } else {
                        if (instances.size() > 0) {
                            instance = instances.getPropertyList(0);
                        } else {
                            instance = new PropertyList();
                            instance.setProperty("fieldid", field.getProperty("fieldid"));
                            instance.setProperty("fieldinstance", "0");
                            instances.add(instance);
                        }
                        this.evalGroovyYNProperty(field, "processingfield", bindings);
                        this.evalGroovyYNProperty(field, "mandatory", bindings);
                        this.evalGroovyYNProperty(field, "readonly", bindings);
                        this.evalGroovyYNProperty(field, "visible", bindings);
                        this.evalGroovyProperty(field, "values", bindings);
                        this.evalGroovyProperty(field, "valuesqueryfrom", bindings);
                        this.evalGroovyProperty(field, "valuesquerywhere", bindings);
                        this.evalGroovyProperty(field, "sql", bindings);
                        this.evalGroovyProperty(field, "sdcid", bindings);
                        this.evalGroovyProperty(field, "reftypeid", bindings);
                        this.evalGroovyProperty(field, "instrumentid", bindings);
                        if (field.getProperty("valuerule").length() > 0 && !document.isViewMode()) {
                            String value = this.evalGroovyExpression(field.getProperty("fieldid"), bindings, field.getProperty("valuerule"), true, false, true, true, true, false);
                            instance.setProperty("enteredtext", value);
                            FieldSetter.setValue((Field)fieldValueMap.get(field.getProperty("fieldid")), instance);
                        }
                        if (worksheet && field.getProperty("readonly").equals("C") && document.getDocumentStatus().length() > 0 || document.isViewMode()) {
                            field.setProperty("dynamic", "N");
                            field.setProperty("fieldevaluation", "N");
                            field.setProperty("fieldvalidation", "N");
                            if (field.getProperty("type").equals("dropdown")) {
                                field.setProperty("values", field.getProperty("values").length() > 0 ? field.getProperty("values") + ";" + instance.getProperty("enteredtext") : instance.getProperty("enteredtext"));
                            }
                        }
                        if (field.getProperty("autosave").length() > 0 && !instance.containsKey("binding")) {
                            instance.setProperty("bound", "N");
                        }
                        if (ddedata) {
                            this.setupDDEDataInstance(document, field, instance, defaultValue);
                        }
                    }
                } else if (field.getProperty("type").equals("dropdown")) {
                    field.setProperty("dynamic", "N");
                    field.setProperty("fieldevaluation", "N");
                    field.setProperty("fieldvalidation", "N");
                    if (repeatable) {
                        for (int j = 0; j < instances.size(); ++j) {
                            instance = instances.getPropertyList(j);
                            field.setProperty("values", field.getProperty("values").length() > 0 ? field.getProperty("values") + ";" + instance.getProperty("enteredtext") : instance.getProperty("enteredtext"));
                        }
                    } else {
                        PropertyList instance3 = instances.size() > 0 ? instances.getPropertyList(0) : new PropertyList();
                        field.setProperty("values", instance3.getProperty("enteredtext"));
                    }
                }
                if (document.getDocumentStatus().equals("DR") && Integer.parseInt(document.getDocumentversionid()) <= 1 || !field.getProperty("identityfield", "N").equals("Y")) continue;
                field.setProperty("readonly", "Y");
            }
            for (i = 0; i < groups.size(); ++i) {
                PropertyList group = groups.getPropertyList(i);
                if (preview || locked) continue;
                this.evalGroovyYNProperty(group, "readonly", bindings);
                this.evalGroovyYNProperty(group, "visible", bindings);
            }
            for (i = 0; i < sections.size(); ++i) {
                PropertyList section = sections.getPropertyList(i);
                if (section.getProperty("repeatable", "N").equals("Y")) {
                    PropertyListCollection sectionfields = section.getCollection("fields");
                    ArrayList fieldinstances = (ArrayList)((Field)fieldValueMap.get(sectionfields.getPropertyList(0).getProperty("fieldid"))).get("fieldinstance");
                    StringBuffer fieldinstancelist = new StringBuffer();
                    for (int j = 0; j < fieldinstances.size(); ++j) {
                        fieldinstancelist.append(";").append((String)fieldinstances.get(j));
                    }
                    section.setProperty("instancelist", fieldinstancelist.length() > 0 ? fieldinstancelist.substring(1) : "");
                    section.setProperty("initialrepeats", String.valueOf(fieldinstances.size()));
                } else {
                    section.setProperty("initialrepeats", "1");
                }
                this.evalGroovyYNProperty(section, "readonly", bindings);
                this.evalGroovyYNProperty(section, "visible", bindings);
            }
            for (i = 0; i < pages.size(); ++i) {
                PropertyList page = pages.getPropertyList(i);
                this.evalGroovyYNProperty(page, "visible", bindings);
            }
            for (i = 0; i < elements.size(); ++i) {
                PropertyList element = elements.getPropertyList(i);
                this.evalGroovyYNProperty(element, "visible", bindings);
                this.evalGroovyYNProperty(element, "readonly", bindings);
                this.evalGroovyProperty(element, "color", bindings);
                this.evalGroovyProperty(element, "background", bindings);
                this.evalGroovyProperty(element, "class", bindings);
            }
            String layout = form.getFormLayout();
            String[] groovyExpressions = Form.getGroovy(layout);
            if (groovyExpressions.length > 0) {
                HashMap<String, HashMap> bindingMap = new HashMap<String, HashMap>();
                bindingMap.put("document", document.getDocumentRowMap());
                bindingMap.put("form", form.getFormPropertyList());
                bindingMap.put("fields", form.getFieldMap());
                for (int i2 = 0; i2 < groovyExpressions.length; ++i2) {
                    String value = GroovyUtil.getInstance(this.sapphireConnection).evaluateSecure(groovyExpressions[i2], bindingMap);
                    layout = StringUtil.replaceAll(layout, "$G{" + groovyExpressions[i2] + "}", value);
                }
            }
            documentObjects.setProperty("layout", layout);
            PropertyList documentProps = document.getDocumentProperties();
            if (document.isDocumentManager() && document.getDocumentStatus().equals("DN")) {
                if (requestData.getProperty("allowdocmgredit").length() == 0) {
                    throw new DocumentUserException(this.trans.translate("You have Document Manager privileges on this document.") + "<br/><br/>" + this.trans.translate("Do you want to allow editing when the document is in a Done state?") + "<br/><br/>(" + this.trans.translate("Note that processing will not re-execute on submit") + ")", "DUE_DocMgrEdit");
                }
                if (requestData.getProperty("allowdocmgredit").equals("Y")) {
                    documentProps.setProperty("allowdocmgredit", "Y");
                } else if (requestData.getProperty("allowdocmgredit").equals("N")) {
                    documentProps.setProperty("allowdocmgredit", "N");
                    preview = true;
                    document.setMode(0);
                }
            }
            if (requestData.getProperty("mode").equals("DocumentViewer")) {
                preview = true;
                document.setMode(0);
            }
            documentProps.setProperty("preview", preview ? "Y" : "N");
            documentProps.setProperty("readonly", document.isViewMode() ? "Y" : "N");
            documentProps.setProperty("userrole", document.isDocumentManager() ? "DocMgr" : (this.sapphireConnection.getSysuserId().equals(document.getSysuserid1()) || document.getSysuserid1().length() == 0 ? "User1" : (this.sapphireConnection.getSysuserId().equals(document.getSysuserid2()) || ddedata ? "User2" : "User3")));
            documentProps.setProperty("ddedata", ddedata ? "Y" : "N");
            if (!formProps.getProperty("versionstatus").equals("A") || !formProps.getProperty("versionstatus").equals("C")) {
                // empty if block
            }
            if (!documentProps.getProperty("documentstatus").equals("DR") && !documentProps.getProperty("documentstatus").equals("PD") || !formProps.getProperty("formversionid").equals(documentProps.getProperty("formversionid"))) {
                // empty if block
            }
            documentObjects.setProperty("document", documentProps);
            responseData.put("jsonreturn", documentObjects.toJSONString(false));
            this.debugReturn(requestData, documentObjects);
            responseData.put("userconfig_efm_" + hostpageid + "_lastdocumentid", documentid);
            responseData.put("userconfig_efm_" + hostpageid + "_lastdocumentversionid", documentversionid);
            responseData.put("userconfig_efm_" + hostpageid + "_lastdocumentddedata", documentProps.getProperty("ddedata"));
        }
        catch (DocumentUserException due) {
            PropertyList documentObjects = new PropertyList();
            documentObjects.setProperty("status", due.getStatus());
            documentObjects.setProperty("returnmessage", due.getMessage());
            documentObjects.setProperty(due.getStatus(), due.getData());
            responseData.put("jsonreturn", documentObjects.toJSONString(false));
            this.debugReturn(requestData, documentObjects);
        }
        catch (Exception e) {
            this.logger.error("Error loading document. Exception: " + e.getMessage(), e);
            PropertyList documentObjects = new PropertyList();
            documentObjects.setProperty("status", "E");
            documentObjects.setProperty("returnmessage", "Failed to load document '" + documentid + "(v" + documentversionid + ")'. Reason: " + e.getMessage());
            responseData.put("jsonreturn", documentObjects.toJSONString(false));
            this.debugReturn(requestData, documentObjects);
        }
        return responseData;
    }

    private void setupDDEDataInstance(Document document, PropertyList field, PropertyList instance, String defaultValue) {
        if (field.getProperty("identityfield", "N").equals("Y") || field.getProperty("valuerule").length() > 0 || field.getProperty("bindingmode").length() > 0 && !field.getProperty("bindingmode").endsWith("as")) {
            instance.setProperty("ddeenteredtext", instance.getProperty("enteredtext"));
            instance.setProperty("ddedisplayvalue", instance.getProperty("enteredtext"));
        } else if (defaultValue.length() > 0 && document.getDocumentStatus().equals("SM")) {
            instance.setProperty("ddeenteredtext", defaultValue);
            instance.setProperty("ddedisplayvalue", defaultValue);
        }
    }

    private void applyRoles(PropertyList requestData, Document document) {
        PropertyList users = requestData.getPropertyList("users");
        if (users != null) {
            HashMap<String, String> findMap = new HashMap<String, String>();
            findMap.put("sysuserid", this.sapphireConnection.getSysuserId());
            PropertyList documentManagers = users.getPropertyList("documentmanagers");
            if (document.isDocumentManager() && documentManagers != null && documentManagers.getProperty("enabled", "N").equals("Y")) {
                DataSet userlist = this.loadSDIData("User", this.evalTokens(requestData, EncryptDecrypt.unobfsql(documentManagers.getProperty("queryfrom", "(default)"))), this.evalTokens(requestData, EncryptDecrypt.unobfsql(documentManagers.getProperty("querywhere"))), this.evalTokens(requestData, EncryptDecrypt.unobfsql(documentManagers.getProperty("queryorderby"))), "primary[sysuserid]", "", false);
                document.setDocumentManager(userlist.findRow(findMap) >= 0);
            }
            PropertyList reconcilers = users.getPropertyList("reconcilers");
            if (document.isReconcilable() && reconcilers != null && reconcilers.getProperty("enabled", "N").equals("Y")) {
                DataSet userlist = this.loadSDIData("User", this.evalTokens(requestData, EncryptDecrypt.unobfsql(reconcilers.getProperty("queryfrom", "(default)"))), this.evalTokens(requestData, EncryptDecrypt.unobfsql(reconcilers.getProperty("querywhere"))), this.evalTokens(requestData, EncryptDecrypt.unobfsql(reconcilers.getProperty("queryorderby"))), "primary[sysuserid]", "", false);
                document.setReconcilable(userlist.findRow(findMap) >= 0);
            }
            PropertyList approvers = users.getPropertyList("approvers");
            if (document.isApprovable() && approvers != null && approvers.getProperty("enabled", "N").equals("Y")) {
                DataSet userlist = this.loadSDIData("User", this.evalTokens(requestData, EncryptDecrypt.unobfsql(approvers.getProperty("queryfrom", "(default)"))), this.evalTokens(requestData, EncryptDecrypt.unobfsql(approvers.getProperty("querywhere"))), this.evalTokens(requestData, EncryptDecrypt.unobfsql(approvers.getProperty("queryorderby"))), "primary[sysuserid]", "", false);
                document.setApprovable(userlist.findRow(findMap) >= 0);
            }
        }
    }
}

