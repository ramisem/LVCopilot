/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jsoup.Jsoup
 *  org.jsoup.nodes.Attributes
 *  org.jsoup.nodes.Document
 *  org.jsoup.nodes.Element
 *  org.jsoup.nodes.Node
 *  org.jsoup.nodes.TextNode
 *  org.jsoup.parser.Tag
 */
package com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem;

import com.labvantage.sapphire.modules.documents.gwt.server.NewForm;
import com.labvantage.sapphire.modules.documents.gwt.server.ProcessDocument;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemFields;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemIncludes;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemOptions;
import com.labvantage.sapphire.pageelements.gwt.shared.DocumentConstants;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Tag;
import sapphire.SapphireException;
import sapphire.ext.BaseWorksheetItem;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class Form
extends BaseWorksheetItem
implements DocumentConstants {
    private String defaultForm = "<propertylist>\n\t<property id=\"formlayout\" type=\"simple\"><![CDATA[<div  style=\"box-sizing:border-box;padding:5px 5px 5px 5px;\"  id=\"page001\" sapphire=\"page\"></div>]]></property>\n\t<property id=\"formproperties\" type=\"propertylist\">\n\t\t<propertylist >\n\t\t\t<property id=\"sections\" type=\"collection\"><collection/></property>\n\t\t\t<property id=\"datasources\" type=\"collection\"><collection/></property>\n\t\t\t<property id=\"pages\" type=\"collection\">\n\t\t\t\t<collection>\n\t\t\t\t\t<propertylist >\n\t\t\t\t\t\t<property id=\"sections\" type=\"collection\"><collection/></property>\n\t\t\t\t\t\t<property id=\"width\" type=\"simple\">100%</property>\n\t\t\t\t\t\t<property id=\"fields\" type=\"collection\"><collection/></property>\n\t\t\t\t\t\t<property id=\"pageid\" type=\"simple\">page001</property>\n\t\t\t\t\t</propertylist>\n\t\t\t\t</collection>\n\t\t\t</property>\n\t\t\t<property id=\"elements\" type=\"collection\"><collection/></property>\n\t\t\t<property id=\"groups\" type=\"collection\"><collection/></property>\n\t\t\t<property id=\"fields\" type=\"collection\"><collection/></property>\n\t\t</propertylist>\n\t</property>\n</propertylist>";

    @Override
    public void setupOptions(WorksheetItemOptions worksheetItemOptions) {
        this.setClientRenderer("Form");
        worksheetItemOptions.setSupportsFields(true);
        worksheetItemOptions.setSupportsHistory(true);
        worksheetItemOptions.setHasExportHTML(true);
    }

    @Override
    public void setupIncludes(WorksheetItemIncludes worksheetItemIncludes) {
        worksheetItemIncludes.addScriptInclude("WEB-CORE/modules/eln/worksheetitem/scripts/form.js");
        worksheetItemIncludes.setJSObjectName("formEditor");
    }

    @Override
    public void validateAdd(PropertyList properties) throws SapphireException {
        if (properties.containsKey("config")) {
            PropertyList config = new PropertyList();
            config.setPropertyList(properties.getProperty("config"));
            properties.setProperty("config", this.syncConfig(config).toXMLString());
        }
    }

    @Override
    public void validateConfig(PropertyList config) throws SapphireException {
        this.addConfigParamExclude("form");
        this.syncConfig(config);
    }

    @Override
    public void postConfig() throws SapphireException {
        this.syncContents();
    }

    @Override
    public String validateContents(String contents) throws SapphireException {
        try {
            PropertyListCollection fieldValues;
            PropertyList form = new PropertyList(new JSONObject(contents != null && contents.length() > 0 ? this.unescapeChars(contents) : "{}"));
            HashMap<String, String> resetFieldValue = new HashMap<String, String>();
            if (this.isTemplate()) {
                PropertyListCollection fields = form.getCollectionNotNull("fields");
                for (int i = 0; i < fields.size(); ++i) {
                    PropertyList field = fields.getPropertyList(i);
                    String defaultValue = field.getProperty("defaultvalue");
                    if (!defaultValue.equals("[currentuser]") && !defaultValue.equals("[currentdate]") && !defaultValue.equals("[currentdatetime]")) continue;
                    resetFieldValue.put(field.getProperty("fieldid"), defaultValue);
                }
            }
            if ((fieldValues = form.getCollectionNotNull("fieldvalues")) != null) {
                WorksheetItemFields worksheetItemFields = this.getWorksheetItemFields();
                for (int i = 0; i < fieldValues.size(); ++i) {
                    PropertyList instance;
                    int j;
                    PropertyListCollection instances;
                    PropertyList fieldValue = fieldValues.getPropertyList(i);
                    String fieldid = fieldValue.getProperty("fieldid");
                    if (resetFieldValue.keySet().contains(fieldid)) {
                        instances = fieldValue.getCollectionNotNull("instances");
                        for (j = 0; j < instances.size(); ++j) {
                            instance = instances.getPropertyList(j);
                            instance.setProperty("enteredtext", (String)resetFieldValue.get(fieldid));
                            instance.setProperty("displayvalue", (String)resetFieldValue.get(fieldid));
                        }
                        continue;
                    }
                    if (!worksheetItemFields.contains(fieldid)) continue;
                    instances = fieldValue.getCollectionNotNull("instances");
                    for (j = 0; j < instances.size(); ++j) {
                        instance = instances.getPropertyList(j);
                        instance.setProperty("displayvalue", instance.getProperty("enteredtext"));
                        worksheetItemFields.enterFieldValue(fieldid, Integer.parseInt(instance.getProperty("fieldinstance")), instance.getProperty("enteredtext"));
                    }
                }
                worksheetItemFields.save();
                contents = form.toJSONString();
            }
            return contents;
        }
        catch (JSONException e) {
            throw new SapphireException("Failed to validate contents. Reason: " + e.getMessage(), e);
        }
    }

    @Override
    public String validateStateChange(String fromStatus, String toStatus) throws SapphireException {
        if (toStatus.equals("Complete")) {
            try {
                PropertyList form = new PropertyList();
                form.setPropertyList(this.config.getProperty("form"));
                PropertyList formObjects = form.getPropertyListNotNull("formproperties");
                PropertyListCollection fields = formObjects != null ? formObjects.getCollectionNotNull("fields") : null;
                PropertyList contents = this.hasContents() ? new PropertyList(new JSONObject(this.getContents())) : new PropertyList();
                PropertyListCollection fieldValues = contents.getCollectionNotNull("fieldvalues");
                PropertyList submitFieldValues = new PropertyList();
                if (fieldValues != null) {
                    fieldValues.index("fieldid");
                    for (int i = 0; i < fields.size(); ++i) {
                        PropertyList field = fields.getPropertyList(i);
                        String fieldid = field.getProperty("fieldid");
                        PropertyList fieldValue = fieldValues.getIndexedPropertyList(field.getProperty("fieldid"));
                        if (fieldValue == null) continue;
                        submitFieldValues.setProperty(fieldid, fieldValue.getCollectionNotNull("instances"));
                    }
                }
                PropertyList requestData = new PropertyList();
                requestData.setProperty("worksheetitemid", this.getWorksheetItemId());
                requestData.setProperty("worksheetitemversionid", this.getWorksheetItemVersionId());
                requestData.setProperty("fieldvalues", submitFieldValues);
                requestData.setProperty("requestcommand", "draft");
                ProcessDocument processDocument = new ProcessDocument(this.getSapphireConnection(), false);
                HashMap responseData = processDocument.execute(requestData);
                JSONObject jsonObject = new JSONObject(new JSONTokener((String)responseData.get("jsonreturn")));
                PropertyList documentObjects = new PropertyList(jsonObject);
                StringBuffer message = new StringBuffer();
                fieldValues = documentObjects.getCollectionNotNull("fieldvalues");
                if (fieldValues != null) {
                    for (int i = 0; i < fieldValues.size(); ++i) {
                        PropertyList fieldValue = fieldValues.getPropertyList(i);
                        PropertyListCollection instances = fieldValue.getCollectionNotNull("instances");
                        for (int j = 0; j < instances.size(); ++j) {
                            PropertyList instance = instances.getPropertyList(j);
                            PropertyListCollection reviewitems = instance.getCollectionNotNull("reviewitems");
                            if (reviewitems == null) continue;
                            for (int k = 0; k < reviewitems.size(); ++k) {
                                PropertyList reviewitem = reviewitems.getPropertyList(k);
                                message.append("<br/>").append(reviewitem.getProperty("reviewitemtext"));
                            }
                        }
                    }
                }
                return message.length() > 0 ? message.substring(5) : "";
            }
            catch (Exception e) {
                throw new SapphireException("Failed to validate as complete. Reason: " + e.getMessage(), e);
            }
        }
        return "";
    }

    @Override
    public String getViewHTML() throws SapphireException {
        return this.getViewHTML(false);
    }

    @Override
    public String getExportHTML(PropertyList exportOptions) throws SapphireException {
        return this.getViewHTML(true);
    }

    private String getViewHTML(boolean isExport) throws SapphireException {
        try {
            PropertyListCollection sections;
            PropertyList contents;
            if (this.config.getProperty("form").length() == 0) {
                this.config.setProperty("form", this.defaultForm);
            }
            PropertyList propertyList = contents = this.hasContents() ? new PropertyList(new JSONObject(this.getContents())) : new PropertyList();
            if (contents.getProperty("status").equals("E") || contents.getProperty("status").startsWith("DUE")) {
                String message = contents.getProperty("returnmessage");
                this.worksheetItemOptions.setRequiresConfig(true, "ERROR: " + message + " - click to design form");
            }
            PropertyList form = new PropertyList();
            form.setPropertyList(this.config.getProperty("form"));
            String formlayout = form.getProperty("formlayout");
            PropertyList formObjects = form.getPropertyListNotNull("formproperties");
            PropertyListCollection fields = formObjects != null ? formObjects.getCollectionNotNull("fields") : null;
            PropertyListCollection propertyListCollection = sections = formObjects != null ? formObjects.getCollectionNotNull("sections") : null;
            if (!(fields != null && fields.size() != 0 || this.formHasContent(formlayout))) {
                this.worksheetItemOptions.setRequiresConfig(true, "Form Control requires configuration - click to design form");
                return "";
            }
            if (fields != null) {
                PropertyListCollection contentFieldValues;
                PropertyListCollection contentFields = contents.getCollectionNotNull("fields");
                if (contentFields != null) {
                    contentFields.index("fieldid");
                }
                if ((contentFieldValues = contents.getCollectionNotNull("fieldvalues")) != null) {
                    contentFieldValues.index("fieldid");
                }
                Document doc = Jsoup.parseBodyFragment((String)form.getProperty("formlayout"));
                for (int i = 0; i < fields.size(); ++i) {
                    Object fieldvalue;
                    PropertyList field = fields.getPropertyList(i);
                    String fieldid = field.getProperty("fieldid");
                    Element fieldElement = doc.getElementById(fieldid);
                    if (fieldElement == null) continue;
                    Attributes attributes = new Attributes();
                    String tag = "input";
                    String value = "";
                    String title = "";
                    boolean visible = true;
                    boolean hasErrors = false;
                    if (contentFieldValues != null && (fieldvalue = contentFieldValues.getIndexedPropertyList(fieldid)) != null) {
                        PropertyListCollection instances = ((PropertyList)fieldvalue).getCollectionNotNull("instances");
                        value = instances.getPropertyList(0).getProperty("displayvalue", instances.getPropertyList(0).getProperty("enteredtext"));
                        visible = instances.getPropertyList(0).getProperty("visible", "Y").equals("Y");
                        PropertyListCollection reviewitems = instances.getPropertyList(0).getCollectionNotNull("reviewitems");
                        if (reviewitems != null) {
                            for (int j = 0; j < reviewitems.size(); ++j) {
                                PropertyList reviewitem = reviewitems.getPropertyList(j);
                                if (reviewitem.getProperty("reviewitemtype").equals("Y")) {
                                    title = "Mandatory field";
                                    hasErrors = true;
                                    continue;
                                }
                                if (!reviewitem.getProperty("reviewitemtype").equals("V")) continue;
                                title = "Validation error";
                                hasErrors = true;
                            }
                        }
                    }
                    attributes.put("value", value);
                    attributes.put("title", title);
                    attributes.put("readonly", "true");
                    attributes.put("disabled", "true");
                    if (hasErrors) {
                        attributes.put("class", "validation_border");
                    }
                    if (isExport) {
                        fieldElement.appendChild((Node)new TextNode(value));
                        continue;
                    }
                    switch (field.getProperty("type")) {
                        case "checkbox": {
                            attributes.put("type", "checkbox");
                            if (!value.equals("Y")) break;
                            attributes.put("checked", "true");
                            break;
                        }
                        case "textarea": {
                            tag = "textarea";
                            attributes.remove("value");
                            attributes.put("width", "100%");
                            attributes.put("height", "100%");
                            break;
                        }
                        case "password": {
                            attributes.put("type", "password");
                            break;
                        }
                        case "hidden": {
                            attributes.put("style", "display:none");
                        }
                    }
                    PropertyListCollection labels = field.getCollectionNotNull("labels");
                    if (visible) {
                        String style = fieldElement.attr("style");
                        attributes.put("style", style);
                        if (labels != null) {
                            for (int j = 0; j < labels.size(); ++j) {
                                Element label = doc.getElementById(labels.getPropertyList(j).getProperty("labelid"));
                                label.attr("style", "border:none");
                            }
                        }
                    } else {
                        attributes.put("style", "display:none");
                        if (labels != null) {
                            for (int j = 0; j < labels.size(); ++j) {
                                Element label = doc.getElementById(labels.getPropertyList(j).getProperty("labelid"));
                                label.attr("style", "display:none");
                            }
                        }
                    }
                    Element element = new Element(Tag.valueOf((String)tag), "", attributes);
                    if (field.getProperty("type").equals("textarea")) {
                        element.appendText(value);
                    }
                    fieldElement.replaceWith((Node)element);
                }
                if (sections != null) {
                    PropertyListCollection contentSections = contents.getCollectionNotNull("sections");
                    if (contentSections != null) {
                        contentSections.index("sectionid");
                    }
                    for (int i = 0; i < sections.size(); ++i) {
                        boolean visible;
                        PropertyList section = sections.getPropertyList(i);
                        String sectionid = section.getProperty("sectionid");
                        Element sectionElement = doc.getElementById(sectionid);
                        if (sectionElement == null) continue;
                        PropertyList contentSection = contentSections.getIndexedPropertyList(sectionid);
                        if (contentFieldValues == null || (visible = contentSection.getProperty("isvisible", contentSection.getProperty("visible")).equals("Y"))) continue;
                        sectionElement.attr("style", "display:none");
                    }
                }
                return doc.body().children().toString();
            }
            return formlayout;
        }
        catch (Exception e) {
            return "<font color=\"red\">Failed to get form viewer. Reason: " + e.getMessage() + "</font>";
        }
    }

    @Override
    public String getEditorHTML() throws SapphireException {
        try {
            PropertyListCollection fields;
            if (this.config.getProperty("form").length() == 0) {
                this.config.setProperty("form", this.defaultForm);
            }
            PropertyList form = new PropertyList();
            form.setPropertyList(this.config.getProperty("form"));
            PropertyList formObjects = form.getPropertyListNotNull("formproperties");
            PropertyListCollection propertyListCollection = fields = formObjects != null ? formObjects.getCollectionNotNull("fields") : null;
            if (fields == null || fields.size() == 0) {
                this.worksheetItemOptions.setRequiresConfig(true, "Form Control requires configuration - click to design form");
            }
            return "GWTEditor";
        }
        catch (SapphireException e) {
            return "Failed to get form editor. Reason: " + e.getMessage();
        }
    }

    @Override
    public String getLiveIndexingText() {
        try {
            PropertyList contents = this.hasContents() ? new PropertyList(new JSONObject(this.getContents())) : new PropertyList();
            PropertyListCollection fieldValues = contents.getCollectionNotNull("fieldvalues");
            if (fieldValues != null) {
                StringBuffer text = new StringBuffer();
                for (int i = 0; i < fieldValues.size(); ++i) {
                    PropertyList fieldValue = fieldValues.getPropertyList(i);
                    text.append(" ").append(fieldValue.getProperty("enteredtext")).append(" ").append(fieldValue.getProperty("displayvalue"));
                }
                return text.toString();
            }
        }
        catch (Exception e) {
            this.logError("Failed to load contents for workitem " + this.getWorksheetItemId() + " Reason: " + e.getMessage());
        }
        return "";
    }

    private boolean formHasContent(String html) throws SapphireException {
        int pos = com.labvantage.sapphire.modules.documents.Form.findStartOfTag("<div ", html, "id=\"page001\"", 0);
        if (pos > -1) {
            int endPos = html.indexOf(">", pos + 1);
            return !html.substring(endPos + 1).trim().toLowerCase().equals("</div>");
        }
        return false;
    }

    private PropertyList syncConfig(PropertyList config) throws SapphireException {
        PropertyList form = new PropertyList();
        form.setPropertyList(config.getProperty("form", this.defaultForm));
        String html = form.getProperty("formlayout");
        int pos = com.labvantage.sapphire.modules.documents.Form.findStartOfTag("<div ", html, "id=\"page001\"", 0);
        if (pos > -1) {
            int next;
            html = StringUtil.replaceAll(StringUtil.replaceAll(html.substring(pos), "contenteditable=\"true\"", ""), "class=\"page\"", "");
            int endPos = html.indexOf(">", pos + 1);
            pos = html.indexOf("width:");
            if (pos > -1 && pos < endPos) {
                next = html.indexOf(";", pos + 1);
                if (next > -1) {
                    html = html.substring(0, pos) + html.substring(next + 1);
                } else {
                    next = html.indexOf("", pos + 1);
                    if (next > -1) {
                        html = html.substring(0, pos) + html.substring(next);
                    }
                }
            }
            if ((pos = html.indexOf("height:")) > -1 && pos < endPos) {
                next = html.indexOf(";", pos + 1);
                if (next > -1) {
                    html = html.substring(0, pos) + html.substring(next + 1);
                } else {
                    next = html.indexOf("", pos + 1);
                    if (next > -1) {
                        html = html.substring(0, pos) + html.substring(next);
                    }
                }
            }
        }
        form.setProperty("formlayout", html);
        PropertyList formProps = form.getPropertyListNotNull("formproperties");
        if (formProps != null) {
            PropertyListCollection pages = formProps.getCollectionNotNull("pages");
            if (pages != null) {
                for (int i = 0; i < pages.size(); ++i) {
                    PropertyList page = pages.getPropertyList(i);
                    page.setProperty("width", "100%");
                }
            }
            WorksheetItemFields worksheetItemFields = this.getWorksheetItemFields();
            PropertyListCollection formFields = formProps.getCollectionNotNull("fields");
            if (formFields != null) {
                formFields.index("fieldid");
                for (int i = 0; i < formFields.size(); ++i) {
                    PropertyList field = formFields.getPropertyList(i);
                    String fieldid = field.getProperty("fieldid");
                    if (worksheetItemFields.contains(fieldid)) {
                        worksheetItemFields.updateFieldTitle(fieldid, field.getProperty("title"));
                        worksheetItemFields.updateFieldDatatype(fieldid, field.getProperty("datatype"));
                        worksheetItemFields.updateFieldSequence(fieldid, i);
                        worksheetItemFields.updateFieldDef(fieldid, field);
                        continue;
                    }
                    if (field.getProperty("type").equals("password") || field.getProperty("type").equals("hidden") || field.getProperty("type").equals("file") || field.getProperty("type").equals("formattedtext")) continue;
                    worksheetItemFields.addField(fieldid, field.getProperty("title"), field.getProperty("datatype"), i, field);
                }
                Iterator<String> iterator = worksheetItemFields.iterator();
                while (iterator.hasNext()) {
                    String fieldid = iterator.next();
                    if (formFields.getIndexedPropertyList(fieldid) != null) continue;
                    worksheetItemFields.deleteField(fieldid);
                }
            } else if (worksheetItemFields.size() > 0) {
                worksheetItemFields.deleteAll();
            }
            worksheetItemFields.save();
        }
        config.setProperty("form", form.toXMLString());
        return config;
    }

    private String syncContents() throws SapphireException {
        try {
            PropertyListCollection contentFieldValues;
            PropertyListCollection contentFields;
            PropertyList contents;
            if (this.hasContents()) {
                contents = new PropertyList(new JSONObject(this.getContents()));
                contentFields = contents.getCollectionNotNull("fields");
                contentFieldValues = contents.getCollectionNotNull("fieldvalues");
            } else {
                contents = new PropertyList();
                contentFields = new PropertyListCollection();
                contentFieldValues = new PropertyListCollection();
            }
            contentFields.index("fieldid");
            contentFieldValues.index("fieldid");
            PropertyList requestData = new PropertyList();
            requestData.setProperty("worksheetitemid", this.getWorksheetItemId());
            requestData.setProperty("worksheetitemversionid", this.getWorksheetItemVersionId());
            requestData.setProperty("ignoretrainingrecs", "Y");
            requestData.setProperty("isworksheettemplate", this.isTemplate() ? "Y" : "N");
            requestData.setProperty("contents", contents.toJSONString());
            PropertyList worksheetVars = new PropertyList();
            worksheetVars.setProperty("worksheetid", this.getWorksheetId());
            worksheetVars.setProperty("worksheetversionid", this.getWorksheetVersionId());
            worksheetVars.setProperty("worksheetitemid", this.getWorksheetItemId());
            worksheetVars.setProperty("worksheetitemversionid", this.getWorksheetItemVersionId());
            requestData.setProperty("worksheetvars", worksheetVars);
            NewForm newForm = new NewForm(this.getSapphireConnection(), false);
            HashMap responseData = newForm.execute(requestData);
            JSONObject jsonObject = new JSONObject(new JSONTokener((String)responseData.get("jsonreturn")));
            PropertyList response = new PropertyList(jsonObject);
            PropertyListCollection generatedFieldValues = response.getCollectionNotNull("fieldvalues");
            for (int i = 0; i < generatedFieldValues.size(); ++i) {
                PropertyList generatedFieldValue = generatedFieldValues.getPropertyList(i);
                String fieldid = generatedFieldValue.getProperty("fieldid");
                PropertyList contentFieldValue = contentFieldValues.getIndexedPropertyList(fieldid);
                if (contentFieldValue == null) continue;
                PropertyListCollection generatedInstances = generatedFieldValue.getCollectionNotNull("instances");
                PropertyListCollection contentInstances = contentFieldValue.getCollectionNotNull("instances");
                if (generatedInstances == null || contentInstances == null) continue;
                PropertyList generatedInstance = generatedInstances.getPropertyList(0);
                PropertyList contentInstance = contentInstances.getPropertyList(0);
                if (generatedInstance == null || contentInstance == null || contentInstance.getProperty("enteredtext").length() <= 0) continue;
                generatedInstance.setProperty("enteredtext", contentInstance.getProperty("enteredtext"));
                generatedInstance.setProperty("displayvalue", contentInstance.getProperty("enteredtext"));
            }
            contents.setProperty("formprops", response.getPropertyListNotNull("form"));
            contents.setProperty("pages", response.getCollectionNotNull("pages"));
            contents.setProperty("fields", response.getCollectionNotNull("fields"));
            contents.setProperty("fieldvalues", generatedFieldValues);
            contents.setProperty("groups", response.getCollectionNotNull("groups"));
            contents.setProperty("groupvalues", new PropertyListCollection());
            contents.setProperty("sections", response.getCollectionNotNull("sections"));
            contents.setProperty("elements", response.getCollectionNotNull("elements"));
            contents.setProperty("outputs", new PropertyList());
            contents.setProperty("status", response.getProperty("status"));
            contents.setProperty("returnmessage", response.getProperty("returnmessage"));
            PropertyList editProps = new PropertyList();
            editProps.setProperty("sdcid", "LV_WorksheetItem");
            editProps.setProperty("keyid1", this.getWorksheetItemId());
            editProps.setProperty("keyid2", this.getWorksheetItemVersionId());
            editProps.setProperty("contents", contents.toJSONString());
            editProps.setProperty("worksheet_action", "Y");
            this.getActionProcessor().processAction("EditSDI", "1", editProps);
            return editProps.getProperty("contents");
        }
        catch (Exception e) {
            throw new SapphireException("Failed to sync new configuration with contents. Reason: " + e.getMessage(), e);
        }
    }
}

