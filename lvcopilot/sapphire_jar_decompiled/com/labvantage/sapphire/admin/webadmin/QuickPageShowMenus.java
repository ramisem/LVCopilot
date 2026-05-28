/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.admin.webadmin;

import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.xml.PropertyTree;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class QuickPageShowMenus
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 85988 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        try {
            String sdcid = ajaxResponse.getRequestParameter("sdcid");
            String plural = this.getSDCProcessor().getProperty(sdcid, "plural", sdcid);
            String nodeid = ajaxResponse.getRequestParameter("nodeid");
            String tabid = ajaxResponse.getRequestParameter("tabid");
            String selectedMenuid = ajaxResponse.getRequestParameter("selectedmenuid", "");
            String newMenuTitle = ajaxResponse.getRequestParameter("newmenutitle", StringUtil.initCaps(plural));
            WebAdminProcessor wp = new WebAdminProcessor(this.getConnectionId());
            PropertyTree generic = wp.getPropertyTree("menugizmo");
            PropertyList node = generic.getNodePropertyList(nodeid, true);
            PropertyListCollection tabs = node.getCollection("tabs");
            PropertyList tab = tabs.find("id", tabid);
            StringBuffer html = new StringBuffer();
            html.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr height=\"20\">");
            if (tab != null && tab.size() > 0) {
                PropertyListCollection menus = tab.getCollectionNotNull("menus");
                for (int i = 0; i < menus.size(); ++i) {
                    PropertyList menu = menus.getPropertyList(i);
                    String menuid = menu.getProperty("id");
                    if (menuid.length() <= 0) continue;
                    String text = menu.getProperty("text");
                    html.append("<td style=\"color:#0970CE;font-weight:600\" nowrap valign=\"top\">");
                    html.append("<input " + (selectedMenuid.equals(menuid) ? " checked " : "") + " id=\"menu__" + i + "\" type=\"radio\" name=\"menuselector\" onclick=\"selectMenu( '" + nodeid + "', '" + tabid + "','" + menuid + "' )\" />");
                    html.append("<label for=\"menu__" + i + "\">");
                    html.append(text);
                    html.append("</label></td>");
                }
            }
            html.append("<td nowrap style=\"color:#0970CE;font-weight:600\" valign=\"top\">");
            html.append("<input " + (selectedMenuid.equals("__NEW__") ? " checked " : "") + " type=\"radio\" name=\"menuselector\" id=\"newmenuselector\" onclick=\"selectMenu( '" + nodeid + "', '" + tabid + "','__NEW__' )\" />");
            html.append("<input onkeypress=\"document.getElementById( 'newmenuselector' ).checked=true;selectMenu( '" + nodeid + "', '" + tabid + "','__NEW__' )\" style=\"width: 70px\" value=\"" + newMenuTitle + "\" id=\"newmenutitle\">");
            html.append("</td>");
            html.append("</tr></table>");
            ajaxResponse.addCallbackArgument("html", html.toString());
            ajaxResponse.print();
        }
        catch (Exception e) {
            ajaxResponse.setError("Failed to find tabs. Exception: " + e.getMessage(), e);
        }
    }
}

