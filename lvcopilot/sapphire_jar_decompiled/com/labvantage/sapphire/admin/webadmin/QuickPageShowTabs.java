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
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class QuickPageShowTabs
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 85988 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        try {
            String nodeid = ajaxResponse.getRequestParameter("nodeid");
            String selectedTabid = ajaxResponse.getRequestParameter("selectedtabid", "");
            String newTabTitle = ajaxResponse.getRequestParameter("newtabtitle", "NewTab");
            WebAdminProcessor wp = new WebAdminProcessor(this.getConnectionId());
            PropertyTree generic = wp.getPropertyTree("menugizmo");
            PropertyList node = generic.getNodePropertyList(nodeid, true);
            PropertyListCollection tabs = node.getCollectionNotNull("tabs");
            StringBuffer html = new StringBuffer();
            html.append("<div class=\"ws_navigation header_back\">");
            for (int i = 0; i < tabs.size(); ++i) {
                PropertyList tab = tabs.getPropertyList(i);
                String tabid = tab.getProperty("id");
                if (tabid.length() <= 0) continue;
                String text = tab.getProperty("text");
                html.append("<span class=\"menugizmo_menu menugizmo_full menugizmo_menu_up\">");
                html.append("<input " + (tabid.equals(selectedTabid) ? " checked " : "") + " onclick=\"showMenus( '" + nodeid + "', '" + tabid + "' )\" id=\"tab__" + i + "\" type=\"radio\" tabid=\"" + tabid + "\" name=\"tabselector\">");
                html.append(text);
                html.append("</span>");
            }
            html.append("<span class=\"menugizmo_menu menugizmo_full menugizmo_menu_up\">");
            html.append("<input " + (selectedTabid.equals("__NEW__") ? " checked " : "") + " onclick=\"showMenus( '" + nodeid + "', '__NEW__' )\" type=\"radio\" id=\"newtabselector\" tabid=\"__NEW__\" name=\"tabselector\">");
            html.append("<input onkeypress=\"document.getElementById( 'newtabselector' ).checked=true;showMenus( '" + nodeid + "', '__NEW__' ) \" style=\"width: 70px\" value=\"" + newTabTitle + "\" id=\"newtabtitle\">");
            html.append("</span>");
            html.append("</div>");
            ajaxResponse.addCallbackArgument("html", html.toString());
            ajaxResponse.print();
        }
        catch (Exception e) {
            ajaxResponse.setError("Failed to find tabs. Exception: " + e.getMessage(), e);
        }
    }
}

