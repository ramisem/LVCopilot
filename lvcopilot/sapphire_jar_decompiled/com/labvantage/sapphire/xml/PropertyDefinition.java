/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.util.http.HttpUtil;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import java.io.Serializable;
import java.util.HashMap;
import sapphire.util.StringUtil;

public class PropertyDefinition
implements Serializable {
    private String id;
    private String type;
    private String title;
    private String editor;
    private String showif;
    private String help;
    private String sdcid;
    private String extendedWhere;
    private String values;
    private String translate;
    private String matchProperty;
    private boolean deprecated;
    private boolean expression;
    private boolean resolution;
    private boolean advanced;
    private PropertyDefinitionList propertyDefinitionList;
    private HashMap attributes = new HashMap();
    private String defaultValue;

    public String getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEditor() {
        return this.editor;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public String getShowIf() {
        return this.showif;
    }

    public void setShowIf(String showif) {
        this.showif = showif;
    }

    public String getHelp() {
        return this.help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public String getSdcid() {
        return this.sdcid;
    }

    public void setSdcid(String sdcid) {
        this.sdcid = sdcid;
    }

    public String getExtendedWhere() {
        return this.extendedWhere;
    }

    public void setExtendedWhere(String extendedWhere) {
        this.extendedWhere = extendedWhere;
    }

    public String getValues() {
        return this.values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public String getTranslate() {
        return this.translate;
    }

    public boolean isDeprecated() {
        return this.deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public boolean isAdvanced() {
        return this.advanced;
    }

    public void setAdvanced(boolean advanced) {
        this.advanced = advanced;
    }

    public void setTranslate(String translate) {
        this.translate = translate;
    }

    public String getMatchProperty() {
        return this.matchProperty;
    }

    public void setMatchProperty(String matchProperty) {
        this.matchProperty = matchProperty;
    }

    public PropertyDefinitionList getPropertyDefinitionList() {
        return this.propertyDefinitionList;
    }

    public void setPropertyDefinitionList(PropertyDefinitionList propertyDefinitionList) {
        this.propertyDefinitionList = propertyDefinitionList;
    }

    public HashMap getAttributes() {
        return (HashMap)this.attributes.clone();
    }

    public void setAttributes(HashMap attributes) {
        this.attributes = attributes;
    }

    public void setExpression(boolean expression) {
        this.expression = expression;
    }

    public void setResolution(boolean resolution) {
        this.resolution = resolution;
    }

    public boolean isExpression() {
        return this.expression;
    }

    public boolean isResolution() {
        return this.resolution;
    }

    public String toXMLString() {
        return this.toXMLString(0);
    }

    public String toXMLString(int level) {
        return this.toXMLString(level, false);
    }

    public String toXMLString(int level, boolean htmlEncode) {
        StringBuffer xml = new StringBuffer(StringUtil.repeat("\t", level) + "<propertydef id=\"" + (htmlEncode ? HttpUtil.htmlEncode(this.id) : this.id) + "\" type=\"" + (htmlEncode ? HttpUtil.htmlEncode(this.type) : this.type) + "\" title=\"" + (htmlEncode ? HttpUtil.htmlEncode(this.title) : this.title) + "\" editor=\"" + (htmlEncode ? HttpUtil.htmlEncode(this.editor) : this.editor) + "\"");
        xml.append(this.advanced ? " advanced=\"Y\"" : "");
        xml.append(this.deprecated ? " deprecate=\"Y\"" : "");
        xml.append(this.expression ? " expression=\"Y\"" : "");
        xml.append(this.resolution ? " resolution=\"Y\"" : "");
        xml.append(this.translate != null && this.translate.length() > 0 ? " translate=\"Y\"" : "");
        xml.append(this.showif != null && this.showif.length() > 0 ? " showif=\"" + this.showif + "\"" : "");
        xml.append(" help=\"" + (htmlEncode ? HttpUtil.htmlEncode(this.help) : this.help) + "\"");
        xml.append(this.sdcid != null && this.sdcid.length() > 0 ? " sdcid=\"" + (htmlEncode ? HttpUtil.htmlEncode(this.sdcid) : this.sdcid) + "\"" : "");
        xml.append(this.values != null && this.values.length() > 0 ? " values=\"" + (htmlEncode ? HttpUtil.htmlEncode(this.values) : this.values) + "\"" : "");
        xml.append(this.extendedWhere != null && this.extendedWhere.length() > 0 ? " extendedwhere=\"" + (htmlEncode ? HttpUtil.htmlEncode(this.extendedWhere) : this.extendedWhere) + "\"" : "");
        for (String attributeid : this.getAttributes().keySet()) {
            if (attributeid.equals("id") || attributeid.equals("type") || attributeid.equals("title") || attributeid.equals("editor") || attributeid.equals("advanced") || attributeid.equals("deprecate") || attributeid.equals("expression") || attributeid.equals("defaultvalue") || attributeid.equals("resolution") || attributeid.equals("translate") || attributeid.equals("showif") || attributeid.equals("help") || attributeid.equals("sdcid") || attributeid.equals("values") || attributeid.equals("extendedwhere")) continue;
            xml.append(" ").append(attributeid).append("=\"").append((Object)(htmlEncode ? HttpUtil.htmlEncode((String)this.getAttributes().get(attributeid)) : this.getAttributes().get(attributeid))).append("\"");
        }
        if (this.propertyDefinitionList != null) {
            xml.append(">\n" + this.propertyDefinitionList.toXMLString(level + 1, htmlEncode) + StringUtil.repeat("\t", level) + "</propertydef>\n");
        } else if (this.defaultValue != null && this.defaultValue.length() > 0) {
            xml.append("><![CDATA[" + this.defaultValue + "]]></propertydef>");
        } else {
            xml.append("/>\n");
        }
        return xml.toString();
    }

    public boolean hasAdvancedProperty() {
        if (this.type.equals("simple")) {
            return this.isAdvanced();
        }
        return this.propertyDefinitionList == null ? false : this.propertyDefinitionList.hasAdvancedProperty();
    }
}

