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
import sapphire.util.HttpUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyValue;

public class WebFileEditor
implements TypeSimple {
    @Override
    public String getEditor(String fieldname, PropertyValue propertyvalue, PropertyList toppropertylist, boolean ancestorvalue, HashMap attributes, PageContext pageContext, boolean debug) {
        String filelocation;
        String extension = (String)attributes.get("extension");
        if (extension == null) {
            extension = "";
        }
        if ((filelocation = (String)attributes.get("filelocationtype")) != null) {
            filelocation = "";
        }
        Button file = new Button(pageContext);
        file.setAction("lookupwebfile( '" + fieldname + "', '" + extension + "', '" + HttpUtil.getAppRoot(pageContext.getServletContext()) + "','" + filelocation + "')");
        file.setImg("rc?command=image&image=Folder&size=16");
        file.setMargin("none");
        file.setHighlight("false");
        StringBuffer output = new StringBuffer();
        output.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td>");
        output.append("<input type=\"text\" name=\"").append(fieldname).append("\" id=\"").append(fieldname).append("\" style=\"width:250px ").append(ancestorvalue ? "; color:blue" : "").append("\" onchange=\"propertyChange();this.style.color='black';checkEvent( this )\" value=\"").append(propertyvalue).append("\"/>");
        output.append("</td><td>").append(file.getHtml()).append("</td></tr></table>");
        return output.toString();
    }
}

