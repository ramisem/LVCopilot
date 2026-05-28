/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.xml.PropertyDefault;
import java.util.HashMap;
import java.util.Iterator;
import sapphire.util.StringUtil;

public class PropertyDefaultList
extends HashMap {
    private String propertyDefaultId;

    public PropertyDefaultList(String propertyDefaultId) {
        this.propertyDefaultId = propertyDefaultId;
    }

    public String getPropertyDefaultId() {
        return this.propertyDefaultId;
    }

    public void setPropertyDefaultId(String propertyDefaultId) {
        this.propertyDefaultId = propertyDefaultId;
    }

    public void setPropertyDefault(String id, PropertyDefault propertyDefault) {
        this.put(id, propertyDefault);
    }

    public PropertyDefault getPropertyDefault(String id) {
        return this.get(id) != null ? (PropertyDefault)this.get(id) : null;
    }

    public String toXMLString() {
        return this.toXMLString(0);
    }

    public String toXMLString(int level) {
        StringBuffer xml = new StringBuffer(StringUtil.repeat("\t", level) + "<propertydefaultlist>\n");
        Iterator it = this.keySet().iterator();
        while (it.hasNext()) {
            PropertyDefault propertyDefault = (PropertyDefault)this.get(it.next());
            xml.append(propertyDefault.toXMLString(level + 1));
        }
        return xml.append(StringUtil.repeat("\t", level) + "</propertydefaultlist>\n").toString();
    }
}

