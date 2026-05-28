/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.util.http.HttpUtil;
import com.labvantage.sapphire.xml.PropertyDefinition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import sapphire.util.StringUtil;

public class PropertyDefinitionList
extends ArrayList {
    private Map map = new HashMap();
    private String propertyDefId;
    private String color;
    private String labelSingular;
    private String labelPlural;
    private String directtion;
    private String titlePropertyId;
    private String flaggedPropertyId;
    private String hiddenPropertyId;
    private String tableStyle;
    private boolean showhide;
    private boolean allowRoles;
    private boolean appRolesOnly;
    private boolean deprecated;
    private boolean advanced;
    private String addMethod;
    private String uniqueIdPropertyId;

    public PropertyDefinitionList(String propertyDefId) {
        this.propertyDefId = propertyDefId;
    }

    public String getPropertyDefId() {
        return this.propertyDefId;
    }

    public void setPropertyDefId(String propertyDefId) {
        this.propertyDefId = propertyDefId;
    }

    public String getColor() {
        return this.color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getLabelSingular() {
        return this.labelSingular;
    }

    public String getTableStyle() {
        return this.tableStyle;
    }

    public void setTableStyle(String tableStyle) {
        this.tableStyle = tableStyle;
    }

    public void setLabelSingular(String labelSingular) {
        this.labelSingular = labelSingular;
    }

    public String getLabelPlural() {
        return this.labelPlural;
    }

    public void setLabelPlural(String labelPlural) {
        this.labelPlural = labelPlural;
    }

    public String getDirecttion() {
        return this.directtion;
    }

    public void setDirection(String directtion) {
        this.directtion = directtion;
    }

    public String getFlaggedPropertyId() {
        return this.flaggedPropertyId;
    }

    public void setFlaggedPropertyId(String flaggedPropertyId) {
        this.flaggedPropertyId = flaggedPropertyId;
    }

    public String getHiddenPropertyId() {
        return this.hiddenPropertyId;
    }

    public void setHiddenPropertyId(String hiddenPropertyId) {
        this.hiddenPropertyId = hiddenPropertyId;
    }

    public String getTitlePropertyId() {
        return this.titlePropertyId;
    }

    public void setTitlePropertyId(String titlePropertyId) {
        this.titlePropertyId = titlePropertyId;
    }

    public void setUniqueIdPropertyId(String uniqueIdPropertyId) {
        this.uniqueIdPropertyId = uniqueIdPropertyId;
    }

    public String getUniqueIdPropertyId() {
        return this.uniqueIdPropertyId;
    }

    public boolean isShowhide() {
        return this.showhide;
    }

    public void setShowhide(boolean showhide) {
        this.showhide = showhide;
    }

    public boolean isAllowRoles() {
        return this.allowRoles;
    }

    public void setAllowRoles(boolean allowRoles) {
        this.allowRoles = allowRoles;
    }

    public boolean isAppRolesOnly() {
        return this.appRolesOnly;
    }

    public void setAppRolesOnly(boolean appRolesOnly) {
        this.appRolesOnly = appRolesOnly;
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

    public String getAddMethod() {
        return this.addMethod;
    }

    public void setAddMethod(String addMethod) {
        this.addMethod = addMethod;
    }

    public String findPropertyByTitle(String title) {
        String propertyid = "";
        for (Object id : this.map.keySet()) {
            if (!((PropertyDefinition)this.map.get(id)).getTitle().equalsIgnoreCase(title)) continue;
            propertyid = (String)id;
            break;
        }
        return propertyid;
    }

    public void addPropertyDef(int index, PropertyDefinition propertyDefinition) {
        this.add(index, propertyDefinition);
        this.map.put(propertyDefinition.getId(), propertyDefinition);
    }

    public void addPropertyDef(PropertyDefinition propertyDefinition) {
        this.add(propertyDefinition);
        this.map.put(propertyDefinition.getId(), propertyDefinition);
    }

    public PropertyDefinition getPropertyDef(String id) {
        return this.map.get(id) != null ? (PropertyDefinition)this.map.get(id) : null;
    }

    public String toXMLString() {
        return this.toXMLString(0);
    }

    public String toXMLString(int level) {
        return this.toXMLString(level, false);
    }

    public String toXMLString(int level, boolean htmlEncode) {
        StringBuffer xml = new StringBuffer(StringUtil.repeat("\t", level) + "<propertydeflist");
        xml.append(this.propertyDefId != null && this.propertyDefId.length() > 0 ? " propertydefid=\"" + (htmlEncode ? HttpUtil.htmlEncode(this.propertyDefId) : this.propertyDefId) + "\"" : "");
        xml.append(this.color != null && this.color.length() > 0 ? " color=\"" + (htmlEncode ? HttpUtil.htmlEncode(this.color) : this.color) + "\"" : "");
        xml.append(this.labelSingular != null && this.labelSingular.length() > 0 ? " labelsingular=\"" + (htmlEncode ? HttpUtil.htmlEncode(this.labelSingular) : this.labelSingular) + "\"" : "");
        xml.append(this.labelPlural != null && this.labelPlural.length() > 0 ? " labelplural=\"" + (htmlEncode ? HttpUtil.htmlEncode(this.labelPlural) : this.labelPlural) + "\"" : "");
        xml.append(this.directtion != null && this.directtion.length() > 0 ? " direction=\"" + (htmlEncode ? HttpUtil.htmlEncode(this.directtion) : this.directtion) + "\"" : "");
        xml.append(this.titlePropertyId != null && this.titlePropertyId.length() > 0 ? " titlepropertyid=\"" + (htmlEncode ? HttpUtil.htmlEncode(this.titlePropertyId) : this.titlePropertyId) + "\"" : "");
        xml.append(this.flaggedPropertyId != null && this.flaggedPropertyId.length() > 0 ? " flaggedpropertyid=\"" + (htmlEncode ? HttpUtil.htmlEncode(this.flaggedPropertyId) : this.tableStyle) + "\"" : "");
        xml.append(this.hiddenPropertyId != null && this.hiddenPropertyId.length() > 0 ? " hiddenpropertyid=\"" + (htmlEncode ? HttpUtil.htmlEncode(this.hiddenPropertyId) : this.tableStyle) + "\"" : "");
        xml.append(this.tableStyle != null && this.tableStyle.length() > 0 ? " tablestyle=\"" + (htmlEncode ? HttpUtil.htmlEncode(this.tableStyle) : this.tableStyle) + "\"" : "");
        xml.append(this.showhide ? " showhide=\"Y\"" : "");
        xml.append(this.allowRoles ? " allowroles=\"" + (this.appRolesOnly ? "S" : "Y") + "\"" : "");
        xml.append(this.deprecated ? " deprecated=\"Y\"" : "");
        xml.append(this.advanced ? " advanced=\"Y\"" : "");
        xml.append(this.addMethod != null && this.addMethod.length() > 0 ? " addmethod=\"" + (htmlEncode ? HttpUtil.htmlEncode(this.addMethod) : this.addMethod) + "\"" : "");
        xml.append(">\n");
        for (int i = 0; i < this.size(); ++i) {
            PropertyDefinition propertyDefinition = (PropertyDefinition)this.get(i);
            xml.append(propertyDefinition.toXMLString(level + 1, htmlEncode));
        }
        return xml.append(StringUtil.repeat("\t", level)).append("</propertydeflist>\n").toString();
    }

    public boolean hasAdvancedProperty() {
        boolean advanced = false;
        for (int i = 0; i < this.size() && !advanced; ++i) {
            PropertyDefinition propertyDefinition = (PropertyDefinition)this.get(i);
            advanced = advanced || propertyDefinition.hasAdvancedProperty();
        }
        return advanced;
    }
}

