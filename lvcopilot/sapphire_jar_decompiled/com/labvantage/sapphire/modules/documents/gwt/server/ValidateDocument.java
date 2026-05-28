/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.documents.gwt.server;

import com.labvantage.sapphire.modules.documents.Form;
import com.labvantage.sapphire.modules.documents.gwt.server.BaseDocumentValidationCommand;
import com.labvantage.sapphire.modules.documents.gwt.server.DocumentCommand;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.groovy.ProcessingUtil;
import java.util.HashMap;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ValidateDocument
extends BaseDocumentValidationCommand
implements DocumentCommand {
    public ValidateDocument(SapphireConnection sapphireConnection, boolean debug) {
        super(sapphireConnection, debug);
    }

    @Override
    public HashMap execute(PropertyList requestData) {
        String formid = requestData.getProperty("formid");
        String formversionid = requestData.getProperty("formversionid");
        String taskdefid = requestData.getProperty("taskdefid");
        String taskdefversionid = requestData.getProperty("taskdefversionid");
        String taskdefvariantid = requestData.getProperty("taskdefvariantid");
        String stepid = requestData.getProperty("stepid");
        HashMap<String, String> responseData = new HashMap<String, String>();
        boolean completePartialEntries = false;
        try {
            Form form = formid.length() > 0 ? Form.getInstance(this.sapphireConnection, formid, formversionid, this.debug) : Form.getInstance(this.sapphireConnection, taskdefid, taskdefversionid, taskdefvariantid, stepid, null);
            form.setOverrides(requestData.getPropertyList("formoverrides"));
            HashMap fieldMap = ProcessingUtil.createFieldMap(this.sapphireConnection, form, requestData.getPropertyList("fieldvalues"), "enteredtext");
            PropertyListCollection fieldValues = new PropertyListCollection();
            this.inspectFields(form, fieldMap, "check", true, false, completePartialEntries, true, fieldValues);
            PropertyListCollection groupValues = new PropertyListCollection();
            this.inspectGroups(form, fieldMap, groupValues);
            if (requestData.getProperty("ddedata", "N").equals("Y")) {
                for (int i = 0; i < fieldValues.size(); ++i) {
                    PropertyList fieldValue = fieldValues.getPropertyList(i);
                    PropertyListCollection instances = fieldValue.getCollection("instances");
                    for (int j = 0; j < instances.size(); ++j) {
                        PropertyList instance = instances.getPropertyList(j);
                        instance.setProperty("ddeenteredtext", instance.getProperty("enteredtext"));
                    }
                }
            }
            PropertyList documentObjects = new PropertyList();
            documentObjects.setProperty("document", requestData.getPropertyList("document"));
            documentObjects.setProperty("returnmessage", "");
            documentObjects.setProperty("fieldvalues", fieldValues);
            documentObjects.setProperty("groupvalues", groupValues);
            this.debugReturn(requestData, documentObjects);
            responseData.put("jsonreturn", documentObjects.toJSONString(false));
        }
        catch (Exception e) {
            PropertyList documentObjects = new PropertyList();
            documentObjects.setProperty("status", "E");
            documentObjects.setProperty("returnmessage", "Failed to validate document with form '" + formid + "(v" + formversionid + ")'. Reason: " + e.getMessage());
            this.debugReturn(requestData, documentObjects);
            responseData.put("jsonreturn", documentObjects.toJSONString(false));
        }
        return responseData;
    }
}

