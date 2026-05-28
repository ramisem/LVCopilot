/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class ToggleViewHiddenRecords
extends BaseAjaxRequest {
    public static String LABVANTAGE_CVS_ID = "$Revision: 66114 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(this.getConnectionId());
        String userid = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
        String viewhidden = "";
        try {
            List userRoleList = (List)this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getUserAttributeMap().get("rolelist");
            if (!(userRoleList.contains("WebPage-Admin") || userRoleList.contains("Administrator") || userRoleList.contains("View Hidden"))) {
                throw new SapphireException("Must have View Hidden, Administrator or WebPage-Admin role to toggle View Hidden mode");
            }
            boolean viewHidden = "Y".equals(configurationProcessor.getProfileProperty(userid, "viewhidden", "N"));
            viewhidden = viewHidden ? "N" : "Y";
            configurationProcessor.setProfileProperty(userid, "viewhidden", viewhidden);
        }
        catch (SapphireException e) {
            this.logger.error("Error", e);
        }
        ajaxResponse.addCallbackArgument("viewhidden", viewhidden);
        ajaxResponse.print();
    }
}

