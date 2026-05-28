/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.documents;

import com.labvantage.sapphire.modules.documents.Field;
import java.io.Serializable;
import java.util.ArrayList;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class FormGroup
implements Serializable {
    private PropertyList group = new PropertyList();

    public FormGroup(PropertyList group) {
        this.group.putAll(group.copy());
    }

    public void setValue(String value) {
        this.group.setProperty("value", value);
    }

    public String value() {
        return this.group.getProperty("value");
    }

    public String toString() {
        return this.value();
    }

    public String valueList() {
        return this.attributeList("", "", ";");
    }

    public String valueList(String delimeter) {
        return this.attributeList("", "", delimeter);
    }

    public String attributeList(String attributeid) {
        return this.attributeList(attributeid, "Y");
    }

    public String attributeList(String attributeid, String valueCondition) {
        return this.attributeList(attributeid, valueCondition, ";");
    }

    public String attributeList(String attributeid, String valueCondition, String delimeter) {
        ArrayList members = (ArrayList)this.group.get("members");
        if (members != null) {
            StringBuffer valueList = new StringBuffer();
            block0: for (int i = 0; i < members.size(); ++i) {
                Field member = (Field)members.get(i);
                String value = member.getProperty("value");
                if (valueCondition.length() != 0 && !value.equals(valueCondition)) continue;
                if (attributeid.length() == 0) {
                    valueList.append(delimeter).append(value);
                    continue;
                }
                PropertyListCollection attributes = member.getAttributes();
                if (attributes == null) continue;
                for (int j = 0; j < attributes.size(); ++j) {
                    PropertyList attribute = attributes.getPropertyList(j);
                    if (!attribute.getProperty("attributeid").equals(attributeid)) continue;
                    valueList.append(delimeter).append(attribute.getProperty("attributevalue"));
                    continue block0;
                }
            }
            return valueList.length() > 0 ? valueList.substring(1) : "";
        }
        return "";
    }

    public void setMembers(ArrayList members) {
        this.group.put("members", members);
    }

    public PropertyListCollection getMembers() {
        return this.group.getCollection("members");
    }

    public String getProperty(String propertyid) {
        return this.group.getProperty(propertyid);
    }

    public String getProperty(String propertyid, String defaultValue) {
        return this.group.getProperty(propertyid, defaultValue);
    }

    public Object get(String propertyid) {
        return this.group.get(propertyid);
    }
}

