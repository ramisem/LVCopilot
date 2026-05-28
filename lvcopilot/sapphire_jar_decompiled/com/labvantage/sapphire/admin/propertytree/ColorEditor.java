/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.admin.propertytree;

import com.labvantage.sapphire.admin.propertytree.TypeSimple;
import com.labvantage.sapphire.pageelements.controls.Button;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyValue;

public class ColorEditor
implements TypeSimple {
    @Override
    public String getEditor(String fieldName, PropertyValue propertyValue, PropertyList topPropertyList, boolean ancestorValue, HashMap attributes, PageContext pageContext, boolean debug) {
        String val = propertyValue.value.startsWith("{|") && propertyValue.value.endsWith("|}") ? propertyValue.value.substring(2, propertyValue.value.length() - 2) : propertyValue.value;
        boolean stellar = attributes.containsKey("stellarcolor") && (attributes.get("stellarcolor").toString().equalsIgnoreCase("Y") || attributes.get("stellarcolor").toString().equalsIgnoreCase("P"));
        StringBuffer out = new StringBuffer("").append("<table style=\"display:inline-block\" cellpadding=\"1\" cellspacing=\"0\"><tr><td style=\"vertical-align:top\">");
        out.append("<div id=\"").append(fieldName).append("_div\" style=\"background-position:center;background-repeat:no-repeat;width:20px;height:20px;border:solid 1px gray;background-color:").append(val.startsWith("palette.") ? "#FFFFFF" : val).append(";background-image:").append(val.startsWith("palette.") ? "url(rc?command=image&image=FlatBlackStar)" : "none").append(";\">").append("</div>");
        out.append("</td><td style=\"vertical-align:top\">").append("<input onchange=\"propertyChange();color_onchange(this,").append(fieldName).append("_div);\" onkeyup=\"propertyChange()\" type=\"text\" name=\"").append(fieldName).append("\" id=\"").append(fieldName).append("\" style=\"width:250px ").append(ancestorValue ? "; color:blue" : "").append("\" onchange=\"this.style.color='black';checkEvent( this );\" value=\"").append(propertyValue).append("\"/>").append("</td>");
        Button b = new Button(pageContext);
        b.setAction("sapphire.lookup.color.open('" + fieldName + "', '', '', '', '', true" + (stellar ? ", '" + attributes.get("stellarcolor").toString() + "'" : "") + ")");
        b.setImg("WEB-CORE/elements/images/ellipsisblank.gif");
        b.setMargin("none");
        b.setHighlight("false");
        out.append("<td style=\"vertical-align:top\">" + b.getHtml() + "</td></tr></table>");
        return out.toString();
    }
}

