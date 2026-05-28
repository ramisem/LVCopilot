/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.admin.propertytree;

import com.labvantage.sapphire.admin.propertytree.TypeSimple;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.pageelements.controls.Button;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyValue;

public class LinkEditor
implements TypeSimple {
    @Override
    public String getEditor(String fieldName, PropertyValue propertyValue, PropertyList topPropertyList, boolean ancestorValue, HashMap attributes, PageContext pageContext, boolean debug) {
        StringBuffer out = new StringBuffer("<table cellpadding=\"0\" cellspacing=\"0\"><tr><td><input onchange=\"propertyChange()\" onkeyup=\"propertyChange()\" type=\"text\" name=\"" + fieldName + "\" id=\"" + fieldName + "\" style=\"width:250px " + (ancestorValue ? "; color:blue" : "") + "\" onchange=\"this.style.color='black';checkEvent( this )\" value=\"" + propertyValue + "\"/></td>");
        String pagetype = (String)attributes.get("pagetype");
        String hideparams = (String)attributes.get("hideparams");
        Button edit = new Button(pageContext);
        edit.setAction("lookuplink( '" + fieldName + "', '" + (pagetype != null ? pagetype : "") + "', " + (hideparams != null && hideparams.equals("Y") ? "true" : "false") + " )");
        edit.setImg("WEB-CORE/elements/images/ellipsisblank.gif");
        edit.setMargin("none");
        edit.setHighlight("false");
        out.append("<td>" + edit.getHtml() + "</td>");
        if (propertyValue.value != null && propertyValue.value.indexOf("page=") > 0) {
            try {
                String pageid = propertyValue.value.substring(propertyValue.value.indexOf("page=") + 5);
                pageid = this.sanitizeName(pageid);
                String[] tokens = StringUtil.getTokens(pageid);
                for (int i = 0; i < tokens.length; ++i) {
                    String token = tokens[i];
                    String value = topPropertyList.getProperty(token);
                    if (value.length() <= 0) continue;
                    pageid = StringUtil.replaceAll(pageid, "[" + token + "]", value);
                }
                WebAdminProcessor wap = new WebAdminProcessor(pageContext);
                String edition = wap.getDefaultPageEdition(pageid);
                if (edition != null && edition.length() > 0) {
                    Button navigate = new Button(pageContext);
                    navigate.setAction("window.open( 'rc?command=page&page=PageMaintNavigator&pageid=" + pageid + "&selectededition=" + edition + "')");
                    navigate.setImg("WEB-CORE/images/gif/Forward.gif");
                    navigate.setMargin("none");
                    navigate.setTip("Click this to navigate to edit this page");
                    navigate.setHighlight("false");
                    out.append("<td>" + navigate.getHtml() + "</td>");
                }
            }
            catch (Exception pageid) {}
        } else if (propertyValue.value != null && propertyValue.value.indexOf("gizmo=") > 0) {
            try {
                String gizmoid = propertyValue.value.substring(propertyValue.value.indexOf("gizmo=") + 6);
                gizmoid = this.sanitizeName(gizmoid);
                String[] tokens = StringUtil.getTokens(gizmoid);
                for (int i = 0; i < tokens.length; ++i) {
                    String token = tokens[i];
                    String value = topPropertyList.getProperty(token);
                    if (value.length() <= 0) continue;
                    gizmoid = StringUtil.replaceAll(gizmoid, "[" + token + "]", value);
                }
                WebAdminProcessor wap = new WebAdminProcessor(pageContext);
                String edition = wap.getDefaultPageEdition(gizmoid);
                if (edition != null && edition.length() > 0) {
                    Button navigate = new Button(pageContext);
                    navigate.setAction("window.open( 'rc?command=page&page=LV_GizmoDefMaint&keyid1=" + gizmoid + "')");
                    navigate.setImg("WEB-CORE/images/gif/Forward.gif");
                    navigate.setMargin("none");
                    navigate.setTip("Click this to navigate to edit this gizmo");
                    navigate.setHighlight("false");
                    out.append("<td>" + navigate.getHtml() + "</td>");
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        out.append("</tr></table>");
        return out.toString();
    }

    private String sanitizeName(String pageid) {
        if (pageid.indexOf("&") > 0) {
            pageid = pageid.substring(0, pageid.indexOf("&"));
        }
        if (pageid.indexOf("'") > 0) {
            pageid = pageid.substring(0, pageid.indexOf("'"));
        }
        if (pageid.indexOf("\"") > 0) {
            pageid = pageid.substring(0, pageid.indexOf("\""));
        }
        if ((pageid = StringUtil.replaceAll(pageid, "|}", "")).indexOf("\"") > 0) {
            pageid = pageid.substring(0, pageid.indexOf("\""));
        }
        return pageid;
    }
}

