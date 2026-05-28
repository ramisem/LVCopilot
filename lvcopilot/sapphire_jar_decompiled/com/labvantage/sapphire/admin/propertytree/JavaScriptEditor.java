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
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyValue;

public class JavaScriptEditor
implements TypeSimple {
    @Override
    public String getEditor(String fieldName, PropertyValue propertyValue, PropertyList topPropertyList, boolean ancestorValue, HashMap attributes, PageContext pageContext, boolean debug) {
        String codecompletion = (String)attributes.get("codecompletion");
        boolean cc = true;
        if (codecompletion != null && codecompletion.equalsIgnoreCase("N")) {
            cc = false;
        }
        propertyValue.value = DOMUtil.convertChars(propertyValue.value);
        boolean textarea = false;
        if (attributes.containsKey("longstring") && attributes.get("longstring").toString().equalsIgnoreCase("Y")) {
            textarea = true;
        }
        Button b = new Button(pageContext);
        if (textarea) {
            String url = "rc?command=file&file=WEB-CORE/pagetypes/actionblock/scripteditor.jsp&mode=groovy&fieldid=" + fieldName + "&scripttype=javascript";
            b.setAction("sapphire.ui.dialog.open( 'Java Script Editor', '" + url + "', true, 700, 500 );");
        } else {
            b.setAction("if(typeof(sapphire)!='undefined'&&typeof(sapphire.cc)!='undefined')sapphire.cc.openEditor('" + fieldName + "',true);");
        }
        b.setImg("WEB-CORE/elements/images/ellipsisblank.gif");
        b.setMargin("none");
        b.setHighlight("false");
        StringBuffer out = new StringBuffer();
        out.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td>");
        if (textarea) {
            out.append("<textarea name=\"" + fieldName + "\" id=\"" + fieldName + "\" rows=\"3\" cols=\"50\" onchange=\"propertyChange()\" onkeyup=\"propertyChange()\" ");
        } else {
            out.append("<input onpropertychange=\"propertyChange()\" oninput=\"propertyChange()\" ");
            out.append("type=\"text\" ");
        }
        out.append("name=\"").append(fieldName).append("\" ");
        out.append("id=\"").append(fieldName).append("\" ");
        if (textarea) {
            out.append("style=\"overflow:hidden;").append(ancestorValue ? " color:blue" : "").append("\" ");
        } else {
            out.append("style=\"").append(ancestorValue ? " color:blue" : "").append("\" ");
        }
        out.append("onchange=\"this.style.color='black';checkEvent( this ); \" ");
        if (textarea) {
            if (cc) {
                out.append(" onkeypress=\"if(typeof(sapphire)!='undefined'&&typeof(sapphire.cc)!='undefined')sapphire.cc.keyPress(event);\" ");
                out.append(" onkeydown=\"if(typeof(sapphire)!='undefined'&&typeof(sapphire.cc)!='undefined')sapphire.cc.keyDown(event);\" ");
            }
            out.append(">").append(propertyValue).append("</textarea>");
        } else {
            if (cc) {
                out.append(" onkeypress=\"if(typeof(sapphire)!='undefined'&&typeof(sapphire.cc)!='undefined')sapphire.cc.keyPress(event);\" ");
                out.append(" onkeydown=\"if(typeof(sapphire)!='undefined'&&typeof(sapphire.cc)!='undefined')sapphire.cc.keyDown(event);\" ");
            }
            out.append(" value=\"").append(propertyValue).append("\" ");
            out.append("/>");
        }
        out.append("</td><td>").append(b.getHtml()).append("</td></tr></table>");
        return out.toString();
    }
}

