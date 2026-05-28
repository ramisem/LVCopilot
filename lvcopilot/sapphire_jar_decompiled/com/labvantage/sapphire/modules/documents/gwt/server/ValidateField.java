/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.documents.gwt.server;

import com.labvantage.sapphire.modules.documents.Document;
import com.labvantage.sapphire.modules.documents.Form;
import com.labvantage.sapphire.modules.documents.gwt.server.BaseDocumentValidationCommand;
import com.labvantage.sapphire.modules.documents.gwt.server.DocumentCommand;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.groovy.ProcessingUtil;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ValidateField
extends BaseDocumentValidationCommand
implements DocumentCommand {
    public ValidateField(SapphireConnection sapphireConnection, boolean debug) {
        super(sapphireConnection, debug);
    }

    @Override
    public HashMap execute(PropertyList requestData) {
        int i;
        boolean searchform = requestData.getProperty("searchform").equals("Y");
        PropertyList document = requestData.getPropertyList("document");
        String fieldid = requestData.getProperty("fieldid");
        String fieldinstance = requestData.getProperty("fieldinstance");
        String fieldindex = requestData.getProperty("fieldindex");
        String enteredtext = requestData.getProperty("enteredtext");
        PropertyListCollection instances = requestData.getCollection("instances");
        PropertyListCollection dependents = requestData.getCollection("dependents");
        PropertyListCollection precedents = requestData.getCollection("precedents");
        PropertyListCollection keyset = requestData.getCollection("keyset");
        PropertyListCollection identityset = requestData.getCollection("identityset");
        PropertyListCollection dataentryset = this.dataSetToPLC(requestData.getProperty("dataentryset"));
        String formid = document.getProperty("formid");
        String formversionid = document.getProperty("formversionid");
        String formletid = document.getProperty("formletid");
        String formletversionid = document.getProperty("formletversionid");
        String worksheetitemid = document.getProperty("worksheetitemid");
        String worksheetitemversionid = document.getProperty("worksheetitemversionid");
        String taskdefid = document.getProperty("taskdefid");
        String taskdefversionid = document.getProperty("taskdefversionid");
        String taskdefvariantid = document.getProperty("taskdefvariantid");
        String stepid = document.getProperty("stepid");
        boolean multiSampleCalcs = document.containsKey("documentobjects") && document.getPropertyList("documentobjects") != null && document.getPropertyList("documentobjects").getProperty("multisamplecalcs", "N").equals("Y");
        Form form = null;
        boolean completePartialEntries = false;
        try {
            form = formid.length() > 0 ? Form.getInstance(this.sapphireConnection, formid, formversionid, this.debug) : (formletid.length() > 0 ? Form.getInstance(this.sapphireConnection, formletid, formletversionid, "") : (worksheetitemid.length() > 0 ? Form.getInstance(this.sapphireConnection, worksheetitemid, worksheetitemversionid, "", "") : Form.getInstance(this.sapphireConnection, taskdefid, taskdefversionid, taskdefvariantid, stepid, null)));
            form.setOverrides(requestData.getPropertyList("formoverrides"));
        }
        catch (Exception e) {
            this.logger.error("Failed to get form instance '" + formid + "(v" + formversionid + ")' - validateField. Exception: " + e.getMessage(), e);
        }
        PropertyList field = form.getField(fieldid);
        String sectionid = field.getProperty("sectionid");
        PropertyList submittedValues = new PropertyList();
        submittedValues.setProperty(fieldid, instances);
        if (precedents != null) {
            for (i = 0; i < precedents.size(); ++i) {
                PropertyList precedent = precedents.getPropertyList(i);
                submittedValues.setProperty(precedent.getProperty("fieldid"), precedent.getCollection("instances"));
            }
        }
        if (keyset != null) {
            for (i = 0; i < keyset.size(); ++i) {
                PropertyList key = keyset.getPropertyList(i);
                submittedValues.setProperty(key.getProperty("fieldid"), key.getCollection("instances"));
            }
        }
        if (identityset != null) {
            for (i = 0; i < identityset.size(); ++i) {
                PropertyList identity = identityset.getPropertyList(i);
                submittedValues.setProperty(identity.getProperty("fieldid"), identity.getCollection("instances"));
            }
        }
        if (dataentryset != null) {
            for (i = 0; i < dataentryset.size(); ++i) {
                PropertyList dataentry = dataentryset.getPropertyList(i);
                submittedValues.setProperty(dataentry.getProperty("fieldid"), dataentry.getCollection("instances"));
            }
        }
        HashMap fieldMap = ProcessingUtil.createFieldMap(this.sapphireConnection, form, submittedValues, "enteredtext");
        PropertyList fieldValue = new PropertyList();
        fieldValue.setProperty("fieldid", fieldid);
        fieldValue.setProperty("fieldinstance", fieldinstance);
        if (!searchform && field != null) {
            this.inspectField(form, field, "check", enteredtext.length() > 0, completePartialEntries, false, true, multiSampleCalcs, fieldValue, fieldMap, new SDCProcessor(this.sapphireConnection.getConnectionId()), new QueryProcessor(this.sapphireConnection.getConnectionId()));
        }
        if (searchform) {
            fieldValue.setProperty("enteredtext", enteredtext);
        }
        if (requestData.getProperty("ddedata", "N").equals("Y")) {
            // empty if block
        }
        if (!searchform && identityset != null && identityset.size() > 0) {
            DataSet existingDocument = Document.getExistingDocument(this.sapphireConnection, formid, formversionid, identityset, fieldMap);
            if (existingDocument != null && existingDocument.size() > 0) {
                fieldValue.setProperty("documentid", existingDocument.getValue(0, "documentid"));
                fieldValue.setProperty("documentversionid", existingDocument.getValue(0, "documentversionid"));
                fieldValue.setProperty("sysuserid1", existingDocument.getValue(0, "sysuserid1"));
                fieldValue.setProperty("documentstatus", existingDocument.getValue(0, "documentstatus"));
                fieldValue.setProperty("documentexists", "Y");
            } else {
                fieldValue.setProperty("documentexists", "N");
            }
        }
        if (dependents != null) {
            for (int i2 = 0; i2 < dependents.size(); ++i2) {
                PropertyList dependent = dependents.getPropertyList(i2);
                PropertyListCollection dependentPrecedents = dependent.getCollection("precedents");
                PropertyList precedentValues = new PropertyList();
                boolean instanceprecedent = false;
                if (dependentPrecedents != null) {
                    for (int j = 0; j < dependentPrecedents.size(); ++j) {
                        PropertyList dependentPrecedent = dependentPrecedents.getPropertyList(j);
                        instanceprecedent = dependentPrecedent.getProperty("instanceprecedent", "N").equals("Y");
                        precedentValues.setProperty(dependentPrecedent.getProperty("fieldid"), dependentPrecedent.getCollection("instances"));
                    }
                }
                HashMap precedentMap = ProcessingUtil.createFieldMap(this.sapphireConnection, form, precedentValues, "enteredtext");
                String dependentObjectid = null;
                PropertyList dependentObject = null;
                if (dependent.getProperty("dependenttype").equals("field")) {
                    dependentObjectid = dependent.getProperty("fieldid");
                    dependentObject = form.getField(dependentObjectid);
                } else if (dependent.getProperty("dependenttype").equals("group")) {
                    dependentObjectid = dependent.getProperty("groupid");
                    dependentObject = form.getGroup(dependentObjectid);
                } else if (dependent.getProperty("dependenttype").equals("section")) {
                    dependentObjectid = dependent.getProperty("sectionid");
                    dependentObject = form.getSection(dependentObjectid);
                } else if (dependent.getProperty("dependenttype").equals("datasource")) {
                    dependentObjectid = dependent.getProperty("datasourceid");
                    dependentObject = form.getDatasource(dependentObjectid);
                } else if (dependent.getProperty("dependenttype").equals("element")) {
                    dependentObjectid = dependent.getProperty("elementid");
                    dependentObject = form.getElement(dependentObjectid);
                } else if (dependent.getProperty("dependenttype").equals("page")) {
                    dependentObjectid = dependent.getProperty("pageidid");
                    dependentObject = form.getPage(dependent.getProperty("pageid"));
                }
                String dependentFunction = dependent.getProperty("functionid");
                try {
                    if (dependentFunction.equals("value1") || dependentFunction.equals("value2")) {
                        dependent.setProperty(dependentFunction, "Y");
                        continue;
                    }
                    HashMap bindings = ProcessingUtil.createBindingsMap(this.sapphireConnection, "DEPENDENTFIELDS");
                    bindings.put("fields", precedentMap);
                    if (dependentObject.getProperty("sectionid").length() > 0) {
                        bindings.put("fieldinstance", precedentMap.get(dependentObject.getProperty("sectionid") + "_" + fieldinstance));
                    }
                    bindings.put("sectioninstance", Integer.parseInt(fieldinstance));
                    bindings.put("sectionindex", Integer.parseInt(fieldindex));
                    if (dependentFunction.equals("sql") || dependentFunction.equals("values") || dependentFunction.equals("sdcid") || dependentFunction.equals("reftypeid")) {
                        String functionValue = this.evalGroovyExpression(dependentObjectid, bindings, dependentObject.getProperty(dependentFunction), false);
                        dependent.setProperty(dependentFunction, functionValue);
                        dependent.setProperty("values", Form.defineValues(this.sapphireConnection, dependentFunction.equals("sdcid") ? functionValue : "", dependentFunction.equals("sql") ? functionValue : "", dependentFunction.equals("reftypeid") ? functionValue : "", dependentFunction.equals("values") ? functionValue : "", "", ""));
                    } else if (dependentFunction.equals("datasource")) {
                        if (!searchform) {
                            dependent.setProperty("sections", this.loadDatasourceFieldValues(dependentObject, fieldinstance, form, bindings, false, false));
                            dependent.setProperty("params", dependentObject.getPropertyList("params"));
                        }
                    } else if (dependentFunction.equals("valuerule") || dependentFunction.equals("color") || dependentFunction.equals("background") || dependentFunction.equals("style") || dependentFunction.equals("instrumentid") || dependentFunction.equals("instrumenttypeid") || dependentFunction.equals("valuesqueryfrom") || dependentFunction.equals("valuesquerywhere")) {
                        if (!searchform) {
                            dependent.setProperty(dependentFunction, this.evalGroovyExpression(dependentObjectid, bindings, dependentObject.getProperty(dependentFunction), true, false, true, true, true, false));
                        }
                    } else {
                        dependent.setProperty(dependentFunction, this.isExpressionY(dependentObjectid, dependentObject.getProperty(dependentFunction, "N"), bindings) ? "Y" : "N");
                    }
                    dependent.setProperty("instanceprecedent", instanceprecedent ? "Y" : "N");
                    continue;
                }
                catch (Exception e) {
                    fieldValue.setProperty("errormsg", "Failed to evaluate Groovy expression for function '" + dependentFunction + "'. Exception: " + e.getMessage());
                    this.logger.error(fieldValue.getProperty("errormsg"), e);
                }
            }
            fieldValue.setProperty("dependents", dependents);
        }
        HashMap<String, String> responseData = new HashMap<String, String>();
        responseData.put("jsonreturn", fieldValue.toJSONString(false));
        this.debugReturn(requestData, fieldValue);
        return responseData;
    }

    protected PropertyListCollection dataSetToPLC(String jsonString) {
        PropertyListCollection plc = new PropertyListCollection();
        if (jsonString.length() > 0) {
            try {
                JSONObject jsonObject = new JSONObject(new JSONTokener(jsonString));
                JSONArray dataset = jsonObject.getJSONArray("dataset");
                JSONArray columns = jsonObject.getJSONArray("columns");
                String lastfieldid = "";
                int rows = dataset.length();
                PropertyList pl = null;
                ArrayList instances = null;
                for (int i = 0; i < rows; ++i) {
                    String fieldid = dataset.getJSONArray(i).getString(0);
                    if (!lastfieldid.equals(fieldid)) {
                        pl = new PropertyList();
                        pl.setProperty("fieldid", fieldid);
                        instances = new PropertyListCollection();
                        pl.setProperty("instances", (PropertyListCollection)instances);
                        plc.add(pl);
                        lastfieldid = fieldid;
                    }
                    PropertyList instance = new PropertyList();
                    instances.add(instance);
                    PropertyList binding = null;
                    for (int j = 0; j < columns.length(); ++j) {
                        String columnid = columns.getString(j);
                        if (columnid.startsWith("bd_")) {
                            if (binding == null) {
                                binding = new PropertyList();
                                instance.setProperty("binding", binding);
                            }
                            binding.setProperty(columnid.substring(3), dataset.getJSONArray(i).getString(j));
                            continue;
                        }
                        instance.setProperty(columnid, dataset.getJSONArray(i).getString(j));
                    }
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return plc;
    }
}

