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

public class FileEditor
implements TypeSimple {
    @Override
    public String getEditor(String fieldname, PropertyValue propertyvalue, PropertyList toppropertylist, boolean ancestorvalue, HashMap attributes, PageContext pageContext, boolean debug) {
        String params = null;
        String extension = (String)attributes.get("extension");
        params = extension != null && extension.length() > 0 ? " '" + extension + "', " : "null, ";
        String baseDir = (String)attributes.get("basedir");
        params = baseDir != null && baseDir.length() > 0 ? params + "'" + baseDir + "'," : params + "null,";
        String relative = (String)attributes.get("relative");
        params = relative != null && relative.length() > 0 ? params + (relative.equals("Y") ? "true" : "false") + "," : params + "false,";
        String forcesub = (String)attributes.get("forcesub");
        params = forcesub != null && forcesub.length() > 0 ? params + (forcesub.equals("Y") ? "true" : "false") + "," : params + "false,";
        String folders = (String)attributes.get("folders");
        params = folders != null && folders.length() > 0 ? params + (folders.equals("Y") ? "true" : "false") + "," : params + "false,";
        String filelocation = (String)attributes.get("filelocationtype");
        params = filelocation != null && filelocation.length() > 0 ? params + "'" + filelocation + "'" : params + "''";
        Button b = new Button(pageContext);
        b.setAction("lookupfilesystem( '" + fieldname + "', " + params + ")");
        b.setImg("WEB-CORE/elements/images/ellipsisblank.gif");
        b.setMargin("none");
        b.setHighlight("false");
        StringBuffer output = new StringBuffer();
        output.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td>");
        output.append("<input type=\"text\" name=\"" + fieldname + "\" id=\"" + fieldname + "\" style=\"width:250px " + (ancestorvalue ? "; color:blue" : "") + "\" onchange=\"propertyChange();checkEvent( this );this.style.color='black'\" value=\"" + propertyvalue + "\"/>");
        output.append("</td><td>").append(b.getHtml()).append("</td></tr></table>");
        return output.toString();
    }
}

