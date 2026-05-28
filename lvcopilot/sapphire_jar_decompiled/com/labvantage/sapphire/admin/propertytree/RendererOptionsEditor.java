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

public class RendererOptionsEditor
implements TypeSimple {
    @Override
    public String getEditor(String fieldname, PropertyValue propertyValue, PropertyList topPropertyList, boolean ancestorvalue, HashMap attributes, PageContext pagecontext, boolean debug) {
        String chapter = "";
        StringBuffer out = new StringBuffer("<table cellpadding=\"0\" cellspacing=\"0\"><tr><td><input onchange=\"propertyChange()\" onkeyup=\"propertyChange()\" type=\"text\" name=\"" + fieldname + "\" id=\"" + fieldname + "\" style=\"width:250px " + (ancestorvalue ? "; color:blue" : "") + "\" onchange=\"this.style.color='black';checkEvent( this )\" value=\"" + propertyValue + "\"/></td>");
        Button b = new Button(pagecontext);
        String customRendererPackage = "";
        if (topPropertyList != null) {
            customRendererPackage = topPropertyList.getProperty("customrendererpackage");
            chapter = propertyValue.getParentPropertyList().getParentPropertyValue().getParentPropertyList().getProperty("chapter");
        }
        b.setAction("lookuprendereroptions( '" + fieldname + "', '" + chapter + "', '" + customRendererPackage + "' )");
        b.setImg("WEB-CORE/elements/images/ellipsisblank.gif");
        b.setMargin("none");
        b.setHighlight("false");
        out.append("<td>" + b.getHtml() + "</td></tr></table>");
        return out.toString();
    }
}

