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
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyValue;

public class WorksheetPrivsEditor
implements TypeSimple {
    @Override
    public String getEditor(String fieldName, PropertyValue propertyValue, PropertyList topPropertyList, boolean ancestorValue, HashMap attributes, PageContext pageContext, boolean debug) {
        StringBuffer output = new StringBuffer();
        output.append("<script>");
        output.append("function worksheetprivschange(){propertyChange();}");
        output.append("var worksheetprivstimer = setInterval( function(){if (sapphire.worksheet) {sapphire.worksheet.addPrivsPanel('" + propertyValue + "', '" + fieldName + "');clearInterval(worksheetprivstimer);}}, 500);");
        output.append("</script>");
        output.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td id=\"worksheetprivspanel\">");
        output.append("</td></tr><td>");
        output.append("<textarea name=\"" + fieldName + "\" id=\"" + fieldName + "\" style=\"width:250px " + (ancestorValue ? "; color:blue" : "") + "\" onchange=\"propertyChange()\" readonly >" + propertyValue + "</textarea>");
        output.append("</td></tr></table>");
        return output.toString();
    }
}

