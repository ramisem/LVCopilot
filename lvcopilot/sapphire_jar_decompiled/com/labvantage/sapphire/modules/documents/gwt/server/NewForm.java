/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.documents.gwt.server;

import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.modules.documents.DocumentUserException;
import com.labvantage.sapphire.modules.documents.Field;
import com.labvantage.sapphire.modules.documents.FieldSetter;
import com.labvantage.sapphire.modules.documents.Form;
import com.labvantage.sapphire.modules.documents.gwt.server.BaseDocumentCommand;
import com.labvantage.sapphire.modules.documents.gwt.server.DocumentCommand;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemTokenResolver;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import com.labvantage.sapphire.util.groovy.ProcessingUtil;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.util.M18NUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class NewForm
extends BaseDocumentCommand
implements DocumentCommand {
    public NewForm(SapphireConnection sapphireConnection, boolean debug) {
        super(sapphireConnection, debug);
    }

    @Override
    public HashMap execute(PropertyList requestData) {
        PropertyList inputFieldValues;
        String formid = requestData.getProperty("formid");
        String formversionid = requestData.getProperty("formversionid");
        String formletid = requestData.getProperty("formletid");
        String formletversionid = requestData.getProperty("formletversionid");
        String worksheetitemid = requestData.getProperty("worksheetitemid");
        String worksheetitemversionid = requestData.getProperty("worksheetitemversionid");
        boolean searchform = requestData.getProperty("searchform", "N").equals("Y");
        String taskdefid = requestData.getProperty("taskdefid");
        PropertyList searchValues = requestData.getPropertyList("searchvalues");
        PropertyList inputParamValues = requestData.getPropertyList("inputparamvalues");
        if (inputParamValues == null) {
            inputParamValues = new PropertyList();
            if (searchform) {
                inputParamValues.setProperty("sdcid", "");
                inputParamValues.setProperty("keyid1", "");
                inputParamValues.setProperty("keyid2", "");
                inputParamValues.setProperty("keyid3", "");
                inputParamValues.setProperty("paramlistid", "");
                inputParamValues.setProperty("paramlistversionid", "");
                inputParamValues.setProperty("variantid", "");
                inputParamValues.setProperty("dataset", "");
                inputParamValues.setProperty("workitemid", "");
                inputParamValues.setProperty("workiteminstance", "");
                inputParamValues.setProperty("qcbatchid", "");
            }
        }
        if ((inputFieldValues = requestData.getPropertyList("inputfieldvalues")) == null) {
            inputFieldValues = new PropertyList();
        }
        try {
            PropertyList contents = new PropertyList(new JSONObject(requestData.getProperty("contents") != null && requestData.getProperty("contents").length() > 0 ? requestData.getProperty("contents") : "{}"));
            PropertyListCollection fieldValues = contents.getCollection("fieldvalues");
            if (fieldValues != null) {
                for (int i = 0; i < fieldValues.size(); ++i) {
                    PropertyList fieldValue = fieldValues.getPropertyList(i);
                    inputFieldValues.setProperty(fieldValue.getProperty("fieldid"), fieldValue.getCollection("instances"));
                }
            }
        }
        catch (JSONException contents) {
            // empty catch block
        }
        String hostpageid = requestData.getProperty("hostpageid");
        HashMap<String, String> responseData = new HashMap<String, String>();
        M18NUtil m18n = new M18NUtil(this.sapphireConnection);
        try {
            HashMap<String, String> document;
            ConfigurationProcessor configProcessor = new ConfigurationProcessor(this.sapphireConnection.getConnectionId());
            this.clearRSet(requestData.getProperty("rsetid"));
            PropertyList directives = new PropertyList();
            directives.setProperty("ignoretrainingrecs", requestData.getProperty("ignoretrainingrecs", "N"));
            directives.setProperty("debug", this.debug ? "Y" : "N");
            Form form = formid.length() > 0 ? Form.getInstance(this.sapphireConnection, formid, formversionid, directives) : (formletid.length() > 0 ? Form.getInstance(this.sapphireConnection, formletid, formletversionid, "") : (worksheetitemid.length() > 0 ? Form.getInstance(this.sapphireConnection, worksheetitemid, worksheetitemversionid, "", "") : Form.getInstance(this.sapphireConnection, taskdefid, requestData.getProperty("taskdefversionid"), requestData.getProperty("taskdefvariantid"), requestData.getProperty("stepid"), directives)));
            form.setOverrides(requestData.getPropertyList("formoverrides"));
            if (!searchform) {
                if (form.isWorksheet() && form.getWorksheettype().length() > 0 && inputParamValues.size() == 0) {
                    String[] recentparams = StringUtil.split(configProcessor.getProfileProperty(this.sapphireConnection.getSysuserId(), "userconfig_efm_" + hostpageid + "_recentparams"), "|");
                    PropertyList recentParams = new PropertyList();
                    for (int i = 0; i < recentparams.length; ++i) {
                        if (recentparams[i].length() <= 0) continue;
                        recentParams.setProperty(recentparams[i].substring(0, recentparams[i].indexOf("=")), recentparams[i].substring(recentparams[i].indexOf("=") + 1));
                    }
                    ArrayList<String> params = form.getWorksheetParams();
                    StringBuffer paramlist = new StringBuffer();
                    for (int i = 0; i < params.size(); ++i) {
                        paramlist.append(";").append(params.get(i));
                        inputParamValues.setProperty(params.get(i), recentParams.getProperty(params.get(i)));
                    }
                    inputParamValues.setProperty("paramlist", paramlist.substring(1));
                    throw new DocumentUserException(this.trans.translate("You cannot open a worksheet form with undefined input parameters!"), "DUE_ParamsReqd", inputParamValues);
                }
                if (form.isTrainingrequired() && !form.isOverrideallowed() && !form.isTrainingexists() && !form.isDocumentManager(this.sapphireConnection.getRoleList())) {
                    throw new DocumentUserException(this.trans.translate("You do not have the necessary training/certification to use the following form:") + " " + formid + (requestData.getProperty("showformversions").equals("Y") ? " (v" + formversionid + ")" : "") + ".");
                }
                if (form.isTrainingrequired() && form.isOverrideallowed() && !form.isTrainingexists() && requestData.getProperty("overridetraining", "N").equals("N") && !form.isDocumentManager(this.sapphireConnection.getRoleList())) {
                    throw new DocumentUserException(this.trans.translate("You do not have the necessary training/certification to use the following form:") + " " + formid + (requestData.getProperty("showformversions").equals("Y") ? " (v" + formversionid + ")" : "") + ".<br/><br/>" + this.trans.translate("You have the option to override these requirements. Would you like to override the training/certification requirement?"), "DUE_Override");
                }
            }
            PropertyList documentObjects = new PropertyList();
            documentObjects.setProperty("returnmessage", "");
            PropertyList formProps = form.getFormPropertyList();
            documentObjects.setProperty("form", formProps);
            PropertyListCollection pages = form.getPages();
            documentObjects.setProperty("pages", pages);
            PropertyListCollection elements = form.getElements();
            documentObjects.setProperty("elements", elements);
            PropertyListCollection groups = form.getGroups();
            documentObjects.setProperty("groups", groups);
            PropertyListCollection sections = form.getSections();
            documentObjects.setProperty("sections", sections);
            PropertyListCollection datasources = form.getDatasources();
            documentObjects.setProperty("datasources", datasources);
            PropertyListCollection fields = form.getFields();
            documentObjects.setProperty("fields", fields);
            PropertyListCollection fieldValues = new PropertyListCollection();
            HashMap<String, Integer> fieldValuesIndex = new HashMap<String, Integer>();
            if (fields != null) {
                int i;
                int i2;
                HashMap<String, Field> fieldMap = new HashMap<String, Field>();
                HashMap<String, Object> fieldInstanceMap = new HashMap<String, Object>();
                for (int i3 = 0; i3 < fields.size(); ++i3) {
                    PropertyList field = fields.getPropertyList(i3);
                    String fieldid = field.getProperty("fieldid");
                    boolean repeatable = field.getProperty("repeatable", "N").equals("Y");
                    PropertyList fieldValue = new PropertyList();
                    fieldValue.setProperty("fieldid", fieldid);
                    PropertyListCollection instances = new PropertyListCollection();
                    fieldValue.setProperty("instances", instances);
                    PropertyListCollection inputInstances = inputFieldValues.getCollection(fieldid);
                    String defaultValue = field.getProperty("defaultvalue");
                    defaultValue = defaultValue.startsWith("$G{") && defaultValue.endsWith("}") ? "" : this.evalTokens(requestData, field.getProperty("defaultvalue"));
                    if (searchform) {
                        String searchValue;
                        String string = searchValue = searchValues != null ? searchValues.getCollection(fieldid).getPropertyList(0).getProperty("enteredtext") : "";
                        searchValue = searchValue.length() > 0 ? searchValue : (field.getProperty("identityfield", "N").equals("Y") ? defaultValue : "");
                        fieldMap.put(fieldid, new Field(field, searchValue, instances, m18n));
                    } else if (repeatable) {
                        fieldValue.setProperty("defaultvalue", defaultValue);
                        if (inputInstances != null && inputInstances.size() > 0) {
                            instances = inputInstances;
                            fieldValue.setProperty("instances", instances);
                        } else {
                            String sectionid = field.getProperty("sectionid");
                            int initialrepeats = 1;
                            try {
                                initialrepeats = Integer.parseInt(((PropertyList)form.getSectionMap().get(sectionid)).getProperty("initialrepeats", "1"));
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                            for (int j = 0; j < initialrepeats; ++j) {
                                PropertyList instance = new PropertyList();
                                instance.setProperty("fieldid", fieldid);
                                instance.setProperty("fieldinstance", String.valueOf(j));
                                instance.setProperty("enteredtext", defaultValue);
                                instances.add(instance);
                            }
                        }
                        fieldMap.put(fieldid, new Field(field, instances, m18n));
                        fieldInstanceMap.put(fieldid, Field.getValueObject(defaultValue, Field.getDatatype(field), m18n));
                    } else {
                        String token;
                        if (inputInstances != null && inputInstances.size() == 1) {
                            String token2 = field.getProperty("defaultvalue");
                            String enteredText = inputInstances.getPropertyList(0).getProperty("enteredtext");
                            if (!token2.equals("[currentuser]") && !token2.equals("[currentdate]") && !token2.equals("[currentdatetime]") || !enteredText.equals("[currentuser]") && !enteredText.equals("[currentdate]") && !enteredText.equals("[currentdatetime]")) {
                                defaultValue = enteredText;
                            }
                        } else if ((inputInstances == null || inputInstances.size() == 0) && requestData.getProperty("isworksheettemplate").equals("Y") && ((token = field.getProperty("defaultvalue")).equals("[currentuser]") || token.equals("[currentdate]") || token.equals("[currentdatetime]"))) {
                            defaultValue = token;
                        }
                        if (field.containsKey("maxlength")) {
                            int maxlen = Integer.parseInt(field.getProperty("maxlength", "4000"));
                            if (defaultValue.length() > maxlen) {
                                defaultValue = defaultValue.substring(0, maxlen);
                            }
                        }
                        fieldMap.put(fieldid, new Field(field, defaultValue, instances, m18n));
                    }
                    fieldValues.add(fieldValue);
                    fieldValuesIndex.put(fieldid, fieldValues.size() - 1);
                }
                HashMap bindings = ProcessingUtil.createBindingsMap(this.sapphireConnection, "NEWFORM");
                document = new HashMap();
                document.put("formid", formid);
                document.put("formversionid", formversionid);
                bindings.put("document", document);
                bindings.put("params", inputParamValues);
                bindings.put("fields", fieldMap);
                bindings.put("fieldinstance", fieldInstanceMap);
                PropertyList requestVars = requestData.getPropertyList("requestvars");
                bindings.put("request", requestVars);
                PropertyList worksheetVars = null;
                if (requestData.getPropertyList("worksheetvars") != null) {
                    worksheetVars = requestData.getPropertyList("worksheetvars");
                }
                if (requestVars != null && requestVars.getPropertyList("worksheetvars") != null) {
                    worksheetVars = requestVars.getPropertyList("worksheetvars");
                }
                if (worksheetVars != null && worksheetVars.size() > 0) {
                    String worksheetid = worksheetVars.getProperty("worksheetid");
                    String worksheetversionid = worksheetVars.getProperty("worksheetversionid");
                    WorksheetItemTokenResolver resolver = new WorksheetItemTokenResolver(worksheetid, worksheetversionid, new QueryProcessor(this.sapphireConnection.getConnectionId()), this.sapphireConnection);
                    resolver.populateFormBindingMap(worksheetVars, worksheetitemid, worksheetitemversionid);
                    bindings.put("worksheet", worksheetVars);
                }
                PropertyList datasourceSections = new PropertyList();
                for (i2 = 0; i2 < datasources.size(); ++i2) {
                    PropertyList datasource = datasources.getPropertyList(i2);
                    datasourceSections.putAll(this.loadDatasourceFieldValues(datasource, "0", form, bindings, requestData.getProperty("rejectbinderrors").equals("Y"), requestData.getProperty("overrideexistingbindings").equals("Y")));
                }
                if (sections != null) {
                    for (i2 = 0; i2 < sections.size(); ++i2) {
                        PropertyList section = sections.getPropertyList(i2);
                        this.loadDatasourceSectionValues(section, datasourceSections, fieldValues, fieldValuesIndex);
                    }
                }
                PropertyList defaultsection = new PropertyList();
                defaultsection.setProperty("sectionid", "(default)");
                this.loadDatasourceSectionValues(defaultsection, datasourceSections, fieldValues, fieldValuesIndex);
                for (i = 0; i < fields.size(); ++i) {
                    PropertyList field = fields.getPropertyList(i);
                    String fieldid = field.getProperty("fieldid");
                    String sectionid = field.getProperty("sectionid");
                    PropertyListCollection instances = fieldValues.getPropertyList(i).getCollection("instances");
                    boolean repeatable = field.getProperty("repeatable", "N").equals("Y");
                    String defaultValue = field.getProperty("defaultvalue");
                    boolean derivedDefaultValue = false;
                    boolean worksheetValue = false;
                    if (defaultValue.length() > 0 && defaultValue.startsWith("$G{") && defaultValue.endsWith("}")) {
                        derivedDefaultValue = true;
                        worksheetValue = defaultValue.contains("worksheet.");
                        defaultValue = this.evalGroovyExpression(fieldid, bindings, defaultValue, true, false, true, true, true, false);
                        if (field.containsKey("maxlength")) {
                            int maxlen = Integer.parseInt(field.getProperty("maxlength", "4000"));
                            if (defaultValue.length() > maxlen) {
                                defaultValue = defaultValue.substring(0, maxlen);
                            }
                        }
                    }
                    if (repeatable) {
                        for (int j = 0; j < instances.size(); ++j) {
                            PropertyList instance = instances.getPropertyList(j);
                            bindings.put("sectioninstance", j);
                            bindings.put("sectionindex", j);
                            bindings.put("fieldinstance", fieldMap.get(sectionid + "_" + j) != null ? fieldMap.get(sectionid + "_" + j) : fieldInstanceMap);
                            this.evalGroovyYNProperty(field, instance, "processingfield", bindings);
                            this.evalGroovyYNProperty(field, instance, "mandatory", bindings);
                            this.evalGroovyYNProperty(field, instance, "readonly", bindings);
                            this.evalGroovyYNProperty(field, instance, "visible", bindings);
                            this.evalGroovyProperty(field, instance, "values", bindings);
                            this.evalGroovyProperty(field, instance, "valuesqueryfrom", bindings);
                            this.evalGroovyProperty(field, instance, "valuesquerywhere", bindings);
                            this.evalGroovyProperty(field, instance, "sql", bindings);
                            this.evalGroovyProperty(field, instance, "sdcid", bindings);
                            this.evalGroovyProperty(field, instance, "reftypeid", bindings);
                            this.evalGroovyProperty(field, instance, "instrumentid", bindings);
                            if (derivedDefaultValue) {
                                // empty if block
                            }
                            if (field.getProperty("valuerule").length() <= 0) continue;
                            String value = this.evalGroovyExpression(fieldid, bindings, field.getProperty("valuerule"), true, false, true, true, true, false);
                            instance.setProperty("enteredtext", value);
                            FieldSetter.setValue((Field)fieldMap.get(fieldid), instance);
                        }
                        continue;
                    }
                    PropertyList instance = instances.getPropertyList(0);
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
                    if (derivedDefaultValue) {
                        if (worksheetValue) {
                            if (instances.getPropertyList(0).getProperty("enteredtext").length() == 0) {
                                instance.setProperty("enteredtext", defaultValue);
                                FieldSetter.setValue((Field)fieldMap.get(fieldid), instance);
                            }
                        } else {
                            instance.setProperty("enteredtext", defaultValue);
                            FieldSetter.setValue((Field)fieldMap.get(fieldid), instance);
                        }
                    }
                    if (field.getProperty("valuerule").length() <= 0) continue;
                    String value = this.evalGroovyExpression(fieldid, bindings, field.getProperty("valuerule"), true, false, true, true, true, false);
                    instance.setProperty("enteredtext", value);
                    FieldSetter.setValue((Field)fieldMap.get(fieldid), instance);
                }
                if (groups != null) {
                    for (i = 0; i < groups.size(); ++i) {
                        this.evalGroovyYNProperty(groups.getPropertyList(i), "readonly", bindings);
                        this.evalGroovyYNProperty(groups.getPropertyList(i), "visible", bindings);
                    }
                }
                if (sections != null) {
                    for (i = 0; i < sections.size(); ++i) {
                        PropertyList section = sections.getPropertyList(i);
                        this.evalGroovyYNProperty(section, "readonly", bindings);
                        this.evalGroovyYNProperty(section, "visible", bindings);
                    }
                }
                if (pages != null) {
                    for (i = 0; i < pages.size(); ++i) {
                        this.evalGroovyYNProperty(pages.getPropertyList(i), "visible", bindings);
                    }
                }
                if (elements != null) {
                    for (i = 0; i < elements.size(); ++i) {
                        this.evalGroovyYNProperty(elements.getPropertyList(i), "visible", bindings);
                        this.evalGroovyYNProperty(elements.getPropertyList(i), "readonly", bindings);
                        this.evalGroovyProperty(elements.getPropertyList(i), "color", bindings);
                        this.evalGroovyProperty(elements.getPropertyList(i), "background", bindings);
                        this.evalGroovyProperty(elements.getPropertyList(i), "class", bindings);
                    }
                }
            }
            documentObjects.setProperty("fieldvalues", fieldValues);
            String layout = form.getFormLayout();
            String[] groovyExpressions = Form.getGroovy(layout);
            if (groovyExpressions.length > 0) {
                HashMap<String, HashMap> bindingMap = new HashMap<String, HashMap>();
                document = new HashMap<String, String>();
                document.put("createdt", "");
                bindingMap.put("document", document);
                bindingMap.put("form", form.getFormPropertyList());
                bindingMap.put("fields", form.getFieldMap());
                for (int i = 0; i < groovyExpressions.length; ++i) {
                    String value = GroovyUtil.getInstance(this.sapphireConnection).evaluateSecure(groovyExpressions[i], bindingMap);
                    layout = StringUtil.replaceAll(layout, "$G{" + groovyExpressions[i] + "}", value);
                }
            }
            documentObjects.setProperty("layout", layout);
            PropertyList documentProps = new PropertyList();
            documentProps.setProperty("formid", form.getFormid());
            documentProps.setProperty("formversionid", form.getFormversionid());
            documentProps.setProperty("formletid", form.getFormletid());
            documentProps.setProperty("formletversionid", form.getFormletversionid());
            documentProps.setProperty("worksheetitemid", form.getWorksheetitemid());
            documentProps.setProperty("worksheetitemversionid", form.getWorksheetitemversionid());
            documentProps.setProperty("taskdefid", form.getTaskdefid());
            documentProps.setProperty("taskdefversionid", form.getTaskdefversionid());
            documentProps.setProperty("taskdefvariantid", form.getTaskdefvariantid());
            documentProps.setProperty("stepid", form.getStepid());
            documentProps.setProperty("userrole", "User1");
            documentProps.setProperty("documentmanager", form.isDocumentManager(this.sapphireConnection.getRoleList()) ? "Y" : "N");
            SequenceProcessor seq = new SequenceProcessor(this.sapphireConnection.getConnectionId());
            documentProps.setProperty("tempdocumentid", String.valueOf("__temp__" + seq.getSequence("LV_Document", "tempdocumentid")));
            if (requestData.getProperty("overridetraining").equals("Y")) {
                documentProps.setProperty("trainingoverriden", "Y");
            }
            documentObjects.setProperty("document", documentProps);
            documentObjects.setProperty("searchform", searchform ? "Y" : "N");
            responseData.put("jsonreturn", documentObjects.toJSONString(false));
            this.debugReturn(requestData, documentObjects);
            if (!(form.isTaskForm() || form.isFormlet() || form.isWorksheetitemForm())) {
                responseData.put("userconfig_efm_" + hostpageid + "_last" + (searchform ? "search" : "") + "formid", formid);
                responseData.put("userconfig_efm_" + hostpageid + "_last" + (searchform ? "search" : "") + "formversionid", formversionid);
                String recentforms = configProcessor.getProfileProperty(this.sapphireConnection.getSysuserId(), "userconfig_efm_" + hostpageid + "_recentforms");
                int pos = recentforms.indexOf(";" + formid + "|" + formversionid);
                if (pos == -1) {
                    recentforms = ";" + formid + "|" + formversionid + recentforms;
                } else if (pos > 0) {
                    int pos2 = recentforms.indexOf(";", pos + 1);
                    recentforms = ";" + formid + "|" + formversionid + recentforms.substring(0, pos) + (pos2 > -1 ? recentforms.substring(pos2) : "");
                }
                if (recentforms.length() > 200) {
                    recentforms = recentforms.substring(0, recentforms.lastIndexOf(";"));
                }
                responseData.put("userconfig_efm_" + hostpageid + "_recentforms", recentforms);
                StringBuffer recentparams = new StringBuffer();
                for (String param : inputParamValues.keySet()) {
                    recentparams.append("|").append(param).append("=").append(inputParamValues.getProperty(param));
                }
                responseData.put("userconfig_efm_" + hostpageid + "_recentparams", recentparams.length() > 0 ? recentparams.substring(1) : "");
            }
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
            this.logger.error("Error opening form. Exception: " + e.getMessage(), e);
            PropertyList documentObjects = new PropertyList();
            documentObjects.setProperty("status", "E");
            documentObjects.setProperty("returnmessage", "Failed to open form '" + formid + "(v" + formversionid + ")'. Reason: " + e.getMessage());
            responseData.put("jsonreturn", documentObjects.toJSONString(false));
            this.debugReturn(requestData, documentObjects);
        }
        return responseData;
    }

    private void loadDatasourceSectionValues(PropertyList section, PropertyList datasourceSections, PropertyListCollection fieldValues, HashMap fieldValuesIndex) throws SapphireException {
        PropertyListCollection datasourceFieldValues;
        PropertyList datasourceSection = datasourceSections.getPropertyList(section.getProperty("sectionid"));
        if (datasourceSection != null && (datasourceFieldValues = datasourceSection.getCollection("fieldvalues")) != null) {
            for (int j = 0; j < datasourceFieldValues.size(); ++j) {
                PropertyList datasourceFieldValue = datasourceFieldValues.getPropertyList(j);
                PropertyList fieldValue = fieldValues.getPropertyList((Integer)fieldValuesIndex.get(datasourceFieldValue.getProperty("fieldid")));
                PropertyListCollection instances = datasourceFieldValue.getCollection("instances");
                if (instances == null || instances.size() <= 0) continue;
                fieldValue.setProperty("instances", instances);
            }
            StringBuffer fieldinstancelist = new StringBuffer();
            int newInstances = Integer.parseInt(datasourceSection.getProperty("newinstances", "0"));
            for (int j = 0; j < newInstances; ++j) {
                fieldinstancelist.append(";").append(j);
            }
            section.setProperty("instancelist", fieldinstancelist.length() > 0 ? fieldinstancelist.substring(1) : "");
            section.setProperty("initialrepeats", datasourceSection.getProperty("newinstances", "0"));
        }
    }
}

