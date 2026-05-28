/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.documents;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.gwt.shared.JSONable;
import com.labvantage.sapphire.modules.documents.Field;
import com.labvantage.sapphire.pageelements.gwt.shared.DocumentConstants;
import com.labvantage.sapphire.services.ConnectionInfo;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.util.M18NUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class FormValue
implements JSONable,
DocumentConstants {
    private PropertyListCollection fields;
    private PropertyListCollection fieldvalues;
    private HashMap<String, Integer> fieldIndex = new HashMap();
    private PropertyListCollection groups;
    private PropertyListCollection groupvalues;
    private HashMap<String, Integer> groupIndex = new HashMap();
    private PropertyListCollection sections;
    private HashMap<String, Integer> sectionIndex = new HashMap();
    private HashMap fieldValueMap = new HashMap();
    private PropertyList outputs;
    private String formid;
    private String formversionid;
    private PropertyList formProps;
    private boolean dde = false;
    private boolean worksheet = false;

    public FormValue(String jsonForm) {
        this.setJSONString(jsonForm);
    }

    public FormValue(PropertyList documentObjects) {
        int i;
        this.formProps = documentObjects.getPropertyList("form");
        this.formid = this.formProps.getProperty("formid");
        this.formversionid = this.formProps.getProperty("formversionid");
        this.dde = this.formProps.getProperty("dde", "N").equals("Y");
        this.worksheet = this.formProps.getProperty("worksheet", "N").equals("Y");
        this.fields = documentObjects.getCollection("fields");
        for (i = 0; i < this.fields.size(); ++i) {
            this.fieldIndex.put(this.fields.getPropertyList(i).getProperty("fieldid"), i);
        }
        this.fieldvalues = documentObjects.getCollection("fieldvalues");
        this.groups = documentObjects.getCollection("groups");
        for (i = 0; i < this.groups.size(); ++i) {
            this.groupIndex.put(this.groups.getPropertyList(i).getProperty("groupid"), i);
        }
        this.groupvalues = documentObjects.getCollection("groupvalues");
        this.sections = documentObjects.getCollection("sections");
        this.outputs = new PropertyList();
    }

    public void setJSONString(String jsonString) {
        try {
            int i;
            JSONObject jsonObject = new JSONObject(jsonString);
            this.formProps = new PropertyList(jsonObject.getJSONObject("formprops"));
            this.formid = this.formProps.getProperty("formid");
            this.formversionid = this.formProps.getProperty("formversionid");
            this.dde = this.formProps.getProperty("dde", "N").equals("Y");
            this.worksheet = this.formProps.getProperty("worksheet", "N").equals("Y");
            this.fields = new PropertyListCollection();
            this.fields.setJSONArray(jsonObject.getJSONArray("fields"));
            for (i = 0; i < this.fields.size(); ++i) {
                this.fieldIndex.put(this.fields.getPropertyList(i).getProperty("fieldid"), i);
            }
            this.fieldvalues = new PropertyListCollection();
            this.fieldvalues.setJSONArray(jsonObject.getJSONArray("fieldvalues"));
            this.groups = new PropertyListCollection();
            this.groups.setJSONArray(jsonObject.getJSONArray("groups"));
            for (i = 0; i < this.groups.size(); ++i) {
                this.groupIndex.put(this.groups.getPropertyList(i).getProperty("groupid"), i);
            }
            this.groupvalues = new PropertyListCollection();
            this.groupvalues.setJSONArray(jsonObject.getJSONArray("groupvalues"));
            this.sections = new PropertyListCollection();
            this.sections.setJSONArray(jsonObject.getJSONArray("sections"));
            this.outputs = new PropertyList(jsonObject.getJSONObject("outputs"));
        }
        catch (Exception e) {
            Trace.logError("Failed setJSONString in FormValue. Reason: " + e.getMessage(), e);
        }
    }

    void defineFieldMap(ConnectionInfo connectionInfo) {
        M18NUtil m18n = new M18NUtil(connectionInfo);
        for (int i = 0; i < this.fieldvalues.size(); ++i) {
            PropertyList fieldvalue = this.fieldvalues.getPropertyList(i);
            this.fieldValueMap.put(fieldvalue.getProperty("fieldid"), new Field(this.fields.getPropertyList(this.fieldIndex.get(fieldvalue.getProperty("fieldid"))), fieldvalue.getCollection("instances"), m18n));
        }
    }

    public HashMap getFields() {
        return this.fieldValueMap;
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("formprops", this.formProps.toJSONObject());
            jsonObject.put("fields", this.fields.toJSONArray());
            jsonObject.put("fieldvalues", this.fieldvalues != null ? this.fieldvalues.toJSONArray() : new PropertyListCollection().toJSONArray());
            jsonObject.put("groups", this.groups != null ? this.groups.toJSONArray() : new PropertyListCollection().toJSONArray());
            jsonObject.put("groupvalues", this.groupvalues != null ? this.groupvalues.toJSONArray() : new PropertyListCollection().toJSONArray());
            jsonObject.put("sections", this.sections != null ? this.sections.toJSONArray() : new PropertyListCollection().toJSONArray());
            jsonObject.put("outputs", this.outputs != null ? this.outputs.toJSONObject() : new PropertyList().toJSONObject());
        }
        catch (JSONException e) {
            Trace.logError("Failed toJSONObject in FormValue. Reason: " + e.getMessage(), e);
        }
        return jsonObject;
    }

    @Override
    public String toJSONString() {
        return this.toJSONObject().toString();
    }

    public String toString(String delimeter) {
        StringBuffer values = new StringBuffer();
        for (int i = 0; i < this.fieldvalues.size(); ++i) {
            PropertyList fieldvalue = this.fieldvalues.getPropertyList(i);
            values.append(delimeter).append(fieldvalue.getProperty("fieldid")).append("=");
            PropertyListCollection instances = fieldvalue.getCollection("instances");
            StringBuffer instancevalues = new StringBuffer();
            for (int j = 0; j < instances.size(); ++j) {
                PropertyList instance = instances.getPropertyList(j);
                instancevalues.append(";").append(instance.getProperty("enteredtext"));
            }
            values.append(instancevalues.substring(1));
        }
        return values.length() > 0 ? values.substring(delimeter.length()) : "";
    }

    public String toString() {
        return this.toString(", ");
    }
}

