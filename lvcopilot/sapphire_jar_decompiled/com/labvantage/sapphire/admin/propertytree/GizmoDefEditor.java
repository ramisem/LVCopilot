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
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyValue;

public class GizmoDefEditor
implements TypeSimple {
    @Override
    public String getEditor(String fieldName, PropertyValue propertyValue, PropertyList topPropertyList, boolean ancestorValue, HashMap attributes, PageContext pageContext, boolean debug) {
        String gizmotype = (String)attributes.get("gizmotype");
        String category = (String)attributes.get("category");
        boolean search = (String)attributes.get("search") != null && !((String)attributes.get("search")).equalsIgnoreCase("N");
        boolean filter = (String)attributes.get("filter") != null && !((String)attributes.get("filter")).equalsIgnoreCase("N");
        StringBuffer out = new StringBuffer("<table cellpadding=\"0\" cellspacing=\"0\"><tr><td><input readonly onchange=\"propertyChange()\" onkeyup=\"if(event.keyCode==46){this.value=''};propertyChange()\" type=\"text\" name=\"" + fieldName + "\" id=\"" + fieldName + "\" style=\"width:250px " + (ancestorValue ? "; color:blue" : "") + "\" onchange=\"this.style.color='black';checkEvent( this )\" value=\"" + propertyValue + "\"/></td>");
        String script = "sapphire.lookup.gizmo.open(function(sId){var e=document.getElementById('" + fieldName + "');e.value=sId;sapphire.events.fireEvent(e,'onchange');}";
        script = gizmotype != null && gizmotype.length() > 0 ? script + ",'" + gizmotype + "'" : script + ",''";
        if (!search) {
            script = script + ",false";
        }
        script = script + ")";
        Button b = new Button(pageContext);
        b.setAction(script);
        b.setImg("WEB-CORE/elements/images/ellipsisblank.gif");
        b.setMargin("none");
        b.setHighlight("false");
        String gizmoid = propertyValue.value;
        gizmoid = StringUtil.replaceAll(gizmoid, "{|", "");
        gizmoid = StringUtil.replaceAll(gizmoid, "|}", "");
        Button navigate = new Button(pageContext);
        navigate.setAction("window.open( 'rc?command=page&page=LV_GizmoDefMaint&keyid1=" + gizmoid + "')");
        navigate.setImg("WEB-CORE/images/gif/Forward.gif");
        navigate.setMargin("none");
        navigate.setTip("Click this to navigate to edit this page");
        navigate.setHighlight("false");
        out.append("<td>" + b.getHtml() + "</td><td>" + navigate.getHtml() + "</td></tr></table>");
        return out.toString();
    }
}

