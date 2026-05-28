/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.xml.PropertyDefaultList;
import java.io.Serializable;
import sapphire.util.StringUtil;

public class PropertyDefault
implements Serializable {
    private String id;
    private String type;
    private String translate = "";
    private String value;
    private PropertyDefaultList propertyDefaultList;

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

    public String getTranslate() {
        return "".equals(this.translate) ? "W" : this.translate;
    }

    public void setTranslate(String translate) {
        this.translate = translate;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public PropertyDefaultList getPropertyDefaultList() {
        return this.propertyDefaultList;
    }

    public void setPropertyDefaultList(PropertyDefaultList propertyDefaultList) {
        this.propertyDefaultList = propertyDefaultList;
    }

    public String toXMLString() {
        return this.toXMLString(0);
    }

    public String toXMLString(int level) {
        StringBuffer xml = new StringBuffer(StringUtil.repeat("\t", level) + "<propertydefault id=\"" + this.id + "\" type=\"" + this.type + "\" translate=\"" + this.translate + "\"");
        if (this.propertyDefaultList != null) {
            xml.append(">\n" + this.propertyDefaultList.toXMLString(level + 1) + StringUtil.repeat("\t", level) + "</propertydefault>\n");
        } else {
            xml.append(this.value != null && this.value.length() > 0 ? "><![CDATA[" + this.value + "]]></propertydefault>\n" : "/>\n");
        }
        return xml.toString();
    }
}

