/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.documents.gwt.server;

import com.labvantage.sapphire.modules.documents.Form;
import com.labvantage.sapphire.modules.documents.gwt.server.BaseDocumentCommand;
import com.labvantage.sapphire.modules.documents.gwt.server.DocumentCommand;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.groovy.ProcessingUtil;
import java.io.Serializable;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class EvaluateField
extends BaseDocumentCommand
implements DocumentCommand {
    public EvaluateField(SapphireConnection sapphireConnection, boolean debug) {
        super(sapphireConnection, debug);
    }

    @Override
    public HashMap execute(PropertyList requestData) {
        PropertyList document = requestData.getPropertyList("document");
        String fieldid = requestData.getProperty("fieldid");
        String fieldinstance = requestData.getProperty("fieldinstance");
        String fieldindex = requestData.getProperty("fieldindex");
        String sectionid = requestData.getProperty("sectionid");
        PropertyListCollection precedents = requestData.getCollection("precedents");
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
        Form form = null;
        try {
            form = formid.length() > 0 ? Form.getInstance(this.sapphireConnection, formid, formversionid, this.debug) : (formletid.length() > 0 ? Form.getInstance(this.sapphireConnection, formletid, formletversionid, "") : (worksheetitemid.length() > 0 ? Form.getInstance(this.sapphireConnection, worksheetitemid, worksheetitemversionid, "", "") : Form.getInstance(this.sapphireConnection, taskdefid, taskdefversionid, taskdefvariantid, stepid, null)));
            form.setOverrides(requestData.getPropertyList("formoverrides"));
        }
        catch (Exception e) {
            this.logger.error("Failed to get form instance '" + formid + "(v" + formversionid + ")' - validateField. Exception: " + e.getMessage(), e);
        }
        PropertyList precedentValues = new PropertyList();
        for (int j = 0; j < precedents.size(); ++j) {
            PropertyList precedent = precedents.getPropertyList(j);
            precedentValues.setProperty(precedent.getProperty("fieldid"), precedent.getCollection("instances"));
        }
        HashMap precedentMap = ProcessingUtil.createFieldMap(this.sapphireConnection, form, precedentValues, "enteredtext");
        PropertyList field = form.getField(fieldid);
        field.setProperty("fieldinstance", requestData.getProperty("fieldinstance"));
        try {
            HashMap<String, Serializable> bindings = new HashMap<String, Serializable>();
            bindings.put("fields", precedentMap);
            bindings.put("fieldinstance", (Serializable)precedentMap.get(sectionid + "_" + fieldinstance));
            bindings.put("sectioninstance", Integer.valueOf(Integer.parseInt(fieldinstance)));
            bindings.put("sectionindex", Integer.valueOf(Integer.parseInt(fieldindex)));
            this.evalGroovyYNProperty(field, "processingfield", bindings);
            this.evalGroovyYNProperty(field, "mandatory", bindings);
            this.evalGroovyYNProperty(field, "readonly", bindings);
            this.evalGroovyYNProperty(field, "visible", bindings);
            this.evalGroovyProperty(field, "values", bindings);
            this.evalGroovyProperty(field, "sql", bindings);
            this.evalGroovyProperty(field, "sdcid", bindings);
            this.evalGroovyProperty(field, "reftypeid", bindings);
            field.setProperty("values", Form.defineValues(this.sapphireConnection, field.getProperty("sdcid"), field.getProperty("sql"), field.getProperty("reftypeid"), field.getProperty("values"), field.getProperty("valuesqueryfrom"), field.getProperty("valuesquerywhere")));
            if (this.debug) {
                field.setProperty("valuerule_orig", field.getProperty("valuerule"));
            }
            field.setProperty("valuerule", this.evalGroovyExpression(fieldid, bindings, field.getProperty("valuerule"), true, false, true, true, true, false));
            field.setProperty("color", this.evalGroovyExpression(fieldid, bindings, field.getProperty("color"), true, false, true, true, true, false));
            field.setProperty("background", this.evalGroovyExpression(fieldid, bindings, field.getProperty("background"), true, false, true, true, true, false));
            field.setProperty("class", this.evalGroovyExpression(fieldid, bindings, field.getProperty("class"), true, false, true, true, true, false));
        }
        catch (SapphireException e) {
            this.logger.error("Failed to evaluate Groovy expression for field '" + fieldid + "'. Exception: " + e.getMessage(), e);
        }
        HashMap<String, String> responseData = new HashMap<String, String>();
        responseData.put("jsonreturn", field.toJSONString(false));
        this.debugReturn(requestData, field);
        return responseData;
    }
}

