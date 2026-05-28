/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.modules.portal;

import com.labvantage.sapphire.platform.Configuration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class ResetPortalTheme
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String message = "";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        if (Configuration.isDevmode(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).getDatabaseId())) {
            message = "Cannot reset theme in devmode";
        } else {
            String portalid = ajaxResponse.getRequestParameter("portalid");
            if (portalid.length() > 0) {
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT valuetree FROM portal WHERE portalid=?", (Object[])new String[]{portalid}, true);
                if (ds.size() == 1) {
                    PropertyList pl = new PropertyList();
                    try {
                        pl.setPropertyList(ds.getValue(0, "valuetree", "<propertylist></propertylist>"));
                        pl.setProperty("lighttheme", new PropertyList());
                        pl.setProperty("darktheme", new PropertyList());
                        this.getQueryProcessor().execPreparedUpdate("UPDATE portal SET valuetree=? WHERE portalid=?", new String[]{pl.toXMLString(), portalid});
                        message = "The Portal theme has been reset";
                    }
                    catch (SapphireException e) {
                        message = "Failed to reset theme: " + e.getMessage();
                    }
                } else {
                    message = "No portal found";
                }
            } else {
                message = "No portal provided";
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.print();
    }
}

