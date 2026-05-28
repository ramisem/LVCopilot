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

public class TaskDefEditor
implements TypeSimple {
    @Override
    public String getEditor(String fieldName, PropertyValue propertyValue, PropertyList topPropertyList, boolean ancestorValue, HashMap attributes, PageContext pageContext, boolean debug) {
        String readonly = (String)attributes.get("readonly");
        StringBuffer out = new StringBuffer("<table cellpadding=\"0\" cellspacing=\"0\"><tr><td><textarea rows=\"2\"readonly onchange=\"propertyChange()\" onkeydown=\"if(event.keyCode == 46){this.value='';}\" onkeyup=\"propertyChange()\" name=\"" + fieldName + "\" id=\"" + fieldName + "\" style=\"width:250px " + (ancestorValue ? "; color:blue" : "") + "\" onchange=\"this.style.color='black';checkEvent( this )\">" + propertyValue + "</textarea></td>");
        String url = "rc?command=file&file=WEB-CORE/elements/workflow/taskdef_editorlookup.jsp&fieldid=" + fieldName;
        Button b = new Button(pageContext);
        out.append("<script>");
        out.append("function ").append(fieldName).append("_p(){");
        out.append("out = sapphire.util.propertyList.create();");
        out.append("out.set('taskdef',document.getElementById('").append(fieldName).append("').value);");
        out.append("return out;");
        out.append("}");
        out.append("</script>");
        b.setAction("sapphire.lookup.util.openWindow( 'TaskLookup','Task Lookup','" + url + "',800,600,false, " + fieldName + "_p() );");
        b.setImg("WEB-CORE/elements/images/ellipsisblank.gif");
        b.setMargin("none");
        b.setHighlight("false");
        out.append("<td>" + b.getHtml() + "</td></tr></table>");
        return out.toString();
    }
}

