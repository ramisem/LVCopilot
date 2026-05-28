/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.documents;

import com.labvantage.sapphire.modules.documents.Form;
import com.labvantage.sapphire.modules.documents.gwt.server.NewForm;
import com.labvantage.sapphire.modules.documents.gwt.server.ProcessDocument;
import com.labvantage.sapphire.pageelements.gwt.shared.DocumentConstants;
import com.labvantage.sapphire.services.SapphireConnection;
import java.util.HashMap;
import org.json.JSONObject;
import org.json.JSONTokener;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class BaseAddDocumentAction
extends BaseAction
implements DocumentConstants {
    protected void addDocument(SapphireConnection sapphireConnection, String formid, String formversionid, PropertyList properties, PropertyList datasourceParams, PropertyList inputFieldValues) throws Exception {
        PropertyList document;
        PropertyList requestData = new PropertyList();
        requestData.setProperty("formid", formid);
        requestData.setProperty("formversionid", formversionid);
        requestData.setProperty("assignto", properties.getProperty("assignto"));
        requestData.setProperty("assigntodepartment", properties.getProperty("assigntodepartment"));
        requestData.setProperty("priority", properties.getProperty("priority"));
        requestData.setProperty("duedt", properties.getProperty("duedt"));
        requestData.setProperty("generatepagehtml", "Y");
        requestData.setProperty("blockdatasets", "Y");
        requestData.setProperty("rejectbinderrors", "Y");
        requestData.setProperty("overrideexistingbindings", properties.getProperty("overrideexistingbindings", "N"));
        requestData.setProperty("newdocumentid", properties.getProperty("newdocumentid"));
        requestData.setProperty("newdocumentversionid", properties.getProperty("newdocumentversionid"));
        requestData.setProperty("actioncall", "Y");
        requestData.setProperty("inputparamvalues", datasourceParams != null ? datasourceParams : new PropertyList());
        requestData.setProperty("inputfieldvalues", inputFieldValues);
        requestData.setProperty("ignoretrainingrecs", "Y");
        boolean overlayInputValues = properties.getProperty("overlayinputvalues", "N").equals("Y");
        NewForm newForm = new NewForm(sapphireConnection, false);
        HashMap responseData = newForm.execute(requestData);
        JSONObject jsonObject = new JSONObject(new JSONTokener((String)responseData.get("jsonreturn")));
        PropertyList response = new PropertyList(jsonObject);
        if (!response.getProperty("status").equals("E") && !response.getProperty("status").startsWith("DUE")) {
            PropertyListCollection newFieldValues = response.getCollection("fieldvalues");
            PropertyList submitFieldValues = new PropertyList();
            for (int i = 0; i < newFieldValues.size(); ++i) {
                PropertyList newFieldValue = newFieldValues.getPropertyList(i);
                String fieldid = newFieldValue.getProperty("fieldid");
                PropertyListCollection inputFieldInstances = inputFieldValues.getCollection(fieldid);
                if (overlayInputValues && inputFieldInstances != null) {
                    PropertyListCollection newFieldInstances = newFieldValue.getCollection("instances");
                    for (int j = 0; j < newFieldInstances.size(); ++j) {
                        PropertyList newFieldInstance = newFieldInstances.getPropertyList(j);
                        if (j >= inputFieldInstances.size()) continue;
                        PropertyList inputFieldInstance = inputFieldInstances.getPropertyList(j);
                        newFieldInstance.setProperty("enteredtext", inputFieldInstance.getProperty("enteredtext"));
                        newFieldInstance.setProperty("displayvalue", inputFieldInstance.getProperty("displayvalue", newFieldInstance.getProperty("enteredtext")));
                    }
                }
                submitFieldValues.setProperty(fieldid, newFieldValue.getCollection("instances"));
            }
            PropertyList datasourcebinding = new PropertyList();
            PropertyListCollection datasources = response.getCollection("datasources");
            boolean multiSampleCalcs = false;
            for (int i = 0; i < datasources.size(); ++i) {
                PropertyList datasource = datasources.getPropertyList(i);
                datasourcebinding.setProperty(datasource.getProperty("datasourceid"), datasource.getPropertyList("params"));
                if (!datasource.getProperty("multisamplecalcs").equals("Y")) continue;
                multiSampleCalcs = true;
            }
            PropertyList sectionbinding = new PropertyList();
            PropertyListCollection sections = response.getCollection("sections");
            for (int i = 0; i < sections.size(); ++i) {
                PropertyList section = sections.getPropertyList(i);
                if (!section.getProperty("formletbyreference", "N").equals("Y")) continue;
                PropertyList params = new PropertyList();
                params.setProperty("formletid", section.getProperty("formlet"));
                params.setProperty("formletversionid", section.getProperty("formletversionid"));
                sectionbinding.setProperty(section.getProperty("sectionid"), params);
            }
            ProcessDocument processDocument = new ProcessDocument(sapphireConnection, false);
            requestData.setProperty("paramvalues", datasourceParams);
            requestData.setProperty("fieldvalues", submitFieldValues);
            requestData.setProperty("datasourcebindings", datasourcebinding);
            requestData.setProperty("sectionbindings", sectionbinding);
            requestData.setProperty("multisamplecalcs", multiSampleCalcs ? "Y" : "N");
            requestData.setProperty("requestcommand", properties.getProperty("validate", "N").equals("Y") ? "draft" : "pending");
            responseData = processDocument.execute(requestData);
            jsonObject = new JSONObject(new JSONTokener((String)responseData.get("jsonreturn")));
            PropertyList documentObjects = new PropertyList(jsonObject);
            document = documentObjects.getPropertyList("document");
            if (!documentObjects.getProperty("saved", "N").equals("Y")) {
                throw new SapphireException(documentObjects.getProperty("returnmessage"));
            }
        } else {
            throw new SapphireException(response.getProperty("returnmessage"));
        }
        properties.setProperty("documentid", document.getProperty("documentid"));
        properties.setProperty("documentversionid", document.getProperty("documentversionid"));
    }

    protected PropertyList getInputFieldValues(Form form, PropertyList properties) {
        PropertyList inputFieldValues = new PropertyList();
        for (int i = 0; i < form.getFields().size(); ++i) {
            PropertyList field = form.getFields().getPropertyList(i);
            String fieldid = field.getProperty("fieldid");
            if (!properties.containsKey(fieldid)) continue;
            String inputFieldValue = properties.getProperty(fieldid);
            PropertyListCollection instances = new PropertyListCollection();
            if (field.getProperty("repeatable", "N").equals("Y")) {
                String[] enteredtext = StringUtil.split(inputFieldValue, ";");
                for (int j = 0; j < enteredtext.length; ++j) {
                    PropertyList instance = new PropertyList();
                    instance.setProperty("fieldid", fieldid);
                    instance.setProperty("fieldinstance", String.valueOf(j));
                    instance.setProperty("enteredtext", enteredtext[j]);
                    instances.add(instance);
                }
            } else {
                PropertyList instance = new PropertyList();
                instance.setProperty("fieldid", fieldid);
                instance.setProperty("fieldinstance", "0");
                instance.setProperty("enteredtext", inputFieldValue);
                instances.add(instance);
            }
            inputFieldValues.setProperty(fieldid, instances);
        }
        return inputFieldValues;
    }
}

