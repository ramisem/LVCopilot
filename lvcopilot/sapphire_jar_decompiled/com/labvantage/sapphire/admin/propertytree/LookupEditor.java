/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.admin.propertytree;

import com.labvantage.sapphire.admin.propertytree.EditorUtil;
import com.labvantage.sapphire.admin.propertytree.TypeSimple;
import com.labvantage.sapphire.pageelements.controls.Button;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyValue;

public class LookupEditor
implements TypeSimple {
    @Override
    public String getEditor(String fieldName, PropertyValue propertyValue, PropertyList topPropertyList, boolean ancestorValue, HashMap attributes, PageContext pageContext, boolean debug) {
        String pageid = (String)attributes.get("pageid");
        String readonly = (String)attributes.get("readonly");
        String sdcid = (String)attributes.get("sdcid");
        if (pageid.contains("[sdcid]") && topPropertyList.getProperty("sdcid").length() == 0) {
            PropertyList parentPropertyList = propertyValue.getParentPropertyList();
            if (parentPropertyList != null && parentPropertyList.getProperty("sdcid").length() == 0) {
                PropertyList propertyList = parentPropertyList = parentPropertyList.getParentPropertyValue() != null ? parentPropertyList.getParentPropertyValue().getParentPropertyList() : null;
            }
            if (parentPropertyList != null) {
                sdcid = parentPropertyList.getProperty("sdcid");
            }
            if (sdcid != null && sdcid.length() > 0) {
                pageid = StringUtil.replaceAll(pageid, "[sdcid]", sdcid);
            }
        }
        pageid = EditorUtil.replaceTokens(pageid, topPropertyList);
        StringBuffer out = new StringBuffer("<table cellpadding=\"0\" cellspacing=\"0\"><tr><td><input" + ("Y".equals(readonly) ? " readonly" : "") + " onchange=\"propertyChange()\" onkeyup=\"propertyChange()\" type=\"text\" name=\"" + fieldName + "\" id=\"" + fieldName + "\" style=\"width:250px " + (ancestorValue ? "; color:blue" : "") + "\" onchange=\"this.style.color='black';checkEvent( this )\" value=\"" + propertyValue + "\"/></td>");
        String url = "rc?command=page&page=" + pageid + "&fieldid=" + fieldName;
        Button b = new Button(pageContext);
        b.setAction("sapphire.lookup.util.openWindow( 'Lookup', 'Lookup', '" + url + "', 800, 640, true )");
        b.setImg("WEB-CORE/elements/images/ellipsisblank.gif");
        b.setMargin("none");
        b.setHighlight("false");
        out.append("<td>" + b.getHtml() + "</td></tr></table>");
        return out.toString();
    }
}

