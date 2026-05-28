/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.util.http.HttpUtil;
import java.io.Serializable;
import sapphire.util.StringUtil;

public class PropertyDependency
implements Serializable {
    private String elementid;
    private String propertytreeid;
    private String description;
    private boolean mandatory;

    public String getElementid() {
        return this.elementid;
    }

    public void setElementid(String elementid) {
        this.elementid = elementid;
    }

    public String getPropertytreeid() {
        return this.propertytreeid;
    }

    public void setPropertytreeid(String propertytreeid) {
        this.propertytreeid = propertytreeid;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isMandatory() {
        return this.mandatory;
    }

    public void setMandatory(String mandatory) {
        this.mandatory = mandatory != null && mandatory.equals("true");
    }

    public String toXMLString() {
        return this.toXMLString(0);
    }

    public String toXMLString(int level) {
        return this.toXMLString(level, false);
    }

    public String toXMLString(int level, boolean htmlEncode) {
        return StringUtil.repeat("\t", level) + "<propertydep elementid=\"" + (htmlEncode ? HttpUtil.htmlEncode(this.elementid) : this.elementid) + "\" propertytreeid=\"" + (htmlEncode ? HttpUtil.htmlEncode(this.propertytreeid) : this.propertytreeid) + "\" description=\"" + (htmlEncode ? HttpUtil.htmlEncode(this.description) : this.description) + "\" mandatory=\"" + (this.mandatory ? "true" : "false") + "\"/>\n";
    }
}

