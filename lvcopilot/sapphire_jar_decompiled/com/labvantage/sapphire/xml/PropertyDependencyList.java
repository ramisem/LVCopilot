/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.xml.PropertyDependency;
import java.util.ArrayList;
import sapphire.util.StringUtil;

public class PropertyDependencyList
extends ArrayList {
    public void addPropertyDep(PropertyDependency propertyDependency) {
        this.add(propertyDependency);
    }

    public String toXMLString() {
        return this.toXMLString(0);
    }

    public String toXMLString(int level) {
        return this.toXMLString(level, false);
    }

    public String toXMLString(int level, boolean htmlEncode) {
        StringBuffer xml = new StringBuffer(StringUtil.repeat("\t", level) + "<propertydeplist>\n");
        for (int i = 0; i < this.size(); ++i) {
            PropertyDependency propertyDependency = (PropertyDependency)this.get(i);
            xml.append(propertyDependency.toXMLString(level + 1, htmlEncode));
        }
        return xml.append(StringUtil.repeat("\t", level)).append("</propertydeplist>\n").toString();
    }
}

