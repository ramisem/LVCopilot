/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.admin.propertytree;

import com.labvantage.sapphire.admin.propertytree.StringEditor;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyValue;

public class NumberEditor
extends StringEditor {
    @Override
    public String getEditor(String fieldName, PropertyValue propertyValue, PropertyList topPropertyList, boolean ancestorValue, HashMap attributes, PageContext pageContext, boolean debug) {
        String value = propertyValue.value;
        boolean isNumber = true;
        if (!ancestorValue && value != null && value.length() > 0) {
            try {
                Double.parseDouble(value);
            }
            catch (NumberFormatException e) {
                isNumber = false;
            }
        }
        String output = "<input type=\"" + (ancestorValue || !isNumber ? "text" : "number") + "\" name=\"" + fieldName + "\" id=\"" + fieldName + "\" style=\"width:120px;" + (ancestorValue ? "color:blue;" : (!isNumber ? "color:red;font-weight:bold" : "")) + "\" onchange=\"propertyChange();this.style.color='black';checkEvent( this )\" value=\"" + value + "\" />";
        return output;
    }
}

