/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.gwt.server;

import com.labvantage.sapphire.util.http.HttpUtil;
import sapphire.pageelements.BaseElement;
import sapphire.xml.PropertyList;

public class GWTElement
extends BaseElement {
    @Override
    public String getHtml() {
        GWTElement.sanitizePropertyList(this.element);
        return HttpUtil.getGWTElementHTML(this.elementid, this.elementType, this.element);
    }

    public static void sanitizePropertyList(PropertyList propertyList) {
        propertyList.remove("webpageid");
        propertyList.remove("propertytreetype");
        propertyList.remove("propertytreeid");
        propertyList.remove("elementid");
        propertyList.remove("nodeid");
        propertyList.remove("objectname");
    }
}

