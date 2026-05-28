/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.admin.propertytree;

import com.labvantage.sapphire.admin.propertytree.TypeSimple;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyValue;

public class LongStringEditor
implements TypeSimple {
    @Override
    public String getEditor(String fieldname, PropertyValue propertyValue, PropertyList toppropertylist, boolean ancestorvalue, HashMap attributes, PageContext pagecontext, boolean debug) {
        propertyValue.value = DOMUtil.convertChars(propertyValue.value);
        String rows = (String)attributes.get("rows");
        String cols = (String)attributes.get("cols");
        String maxsize = (String)attributes.get("maxlength");
        if (rows == null) {
            rows = "3";
        }
        if (cols == null) {
            cols = "80";
        }
        return "<textarea onchange=\"propertyChange()\" onkeyup=\"propertyChange()\" " + (maxsize == null ? "" : "maxlength=\"" + maxsize + "\" ") + "rows=\"" + rows + "\" cols=\"" + cols + "\" name=\"" + fieldname + "\" id=\"" + fieldname + "\" style=\"" + (ancestorvalue ? "; color:blue" : "") + "\" onchange=\"this.style.color='black';checkEvent( this )\" >" + propertyValue + "</textarea>";
    }
}

