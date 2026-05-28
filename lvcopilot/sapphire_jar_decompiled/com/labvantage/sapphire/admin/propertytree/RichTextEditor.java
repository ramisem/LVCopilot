/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.admin.propertytree;

import com.labvantage.sapphire.admin.propertytree.TypeSimple;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.pageelements.controls.HTMLEditorControl;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import sapphire.servlet.RequestContext;
import sapphire.util.LogContext;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyValue;

public class RichTextEditor
implements TypeSimple {
    @Override
    public String getEditor(String fieldName, PropertyValue propertyValue, PropertyList topPropertyList, boolean ancestorValue, HashMap attributes, PageContext pageContext, boolean debug) {
        StringBuffer out = new StringBuffer("");
        RequestContext rc = RequestContext.getRequestContext(pageContext);
        HTMLEditorControl editor = new HTMLEditorControl(new Logger(new LogContext(rc.getConnectionId())));
        editor.setId(fieldName);
        String width = attributes.containsKey("width") ? attributes.get("width").toString() : "auto";
        String height = attributes.containsKey("height") ? attributes.get("height").toString() : "auto";
        editor.setWidth(width);
        editor.setHeight(height);
        int tb = -1;
        boolean isDevMode = false;
        try {
            isDevMode = "Y".equals(new ConfigurationProcessor(pageContext).getSysConfigProperty("devmode"));
        }
        catch (Exception exception) {
            // empty catch block
        }
        boolean showtoolbar = attributes.containsKey("showtoolbar") && attributes.get("showtoolbar").toString().length() > 0 ? attributes.get("showtoolbar").toString().substring(0, 1).equalsIgnoreCase("Y") : true;
        boolean printable = attributes.containsKey("printable") && attributes.get("printable").toString().length() > 0 ? attributes.get("printable").toString().substring(0, 1).equalsIgnoreCase("Y") : true;
        StringBuffer buttons = new StringBuffer();
        if (!printable) {
            editor.setEditorType(HTMLEditorControl.EditorType.FULL);
            editor.setViewOnly(false);
        } else {
            editor.setEditorType(HTMLEditorControl.EditorType.PRINTABLE);
        }
        if (!showtoolbar) {
            editor.setInline(true);
        }
        editor.setContent(propertyValue.toString());
        editor.setEvent("changefield", HTMLEditorControl.Events.FIELDCHANGE, "function(){propertyChange();}");
        editor.setEvent("changeeditor", HTMLEditorControl.Events.CHANGE, "function(event){htmlEditor.save(event.target)}");
        editor.setAutoUpdateField(true);
        ConfigurationProcessor config = new ConfigurationProcessor(pageContext);
        editor.setDevMode(isDevMode);
        out.append(editor.getIncludesHTML((HttpServletRequest)pageContext.getRequest()));
        out.append(editor.getHtml());
        out.append("<script type=\"text/javascript\">");
        out.append(editor.getScript());
        out.append("sapphire.events.attachEvent(window,'load',function(){").append(editor.getInitScript("")).append("});");
        out.append("</script>");
        return out.toString();
    }
}

