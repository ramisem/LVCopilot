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
import com.labvantage.sapphire.platform.Configuration;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class ToggleDevMode
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 66113 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "getTokensHandler");
        try {
            List userRoleList = (List)this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getUserAttributeMap().get("rolelist");
            if (!userRoleList.contains("WebPage-Admin") && !userRoleList.contains("Administrator")) {
                throw new SapphireException("Must have Administrator or WebPage-Admin role to toggle Dev Mode");
            }
            boolean ctrlKey = ajaxResponse.getRequestParameter("ctrl").equals("true");
            boolean shiftKey = ajaxResponse.getRequestParameter("shift").equals("true");
            ConfigurationProcessor cp = new ConfigurationProcessor(this.getConnectionId());
            String devmode = cp.getSysConfigProperty("devmode");
            String compcode = cp.getSysConfigProperty("compcode");
            ArrayList<String> compDevCodes = Configuration.getCompDevCodes();
            if (devmode.length() > 0) {
                if (ctrlKey && shiftKey && compDevCodes.size() > 0) {
                    cp.setSysConfigProperty("compcode", compDevCodes.get(0));
                    Configuration.setCompcode(this.getConnectionProcessor().getSapphireConnection().getDatabaseId(), compDevCodes.get(0));
                    cp.setSysConfigProperty("devmode", "");
                    Configuration.setDevmode(this.getConnectionProcessor().getSapphireConnection().getDatabaseId(), false);
                } else {
                    boolean isDevMode = "Y".equals(devmode);
                    isDevMode = !isDevMode;
                    cp.setSysConfigProperty("devmode", isDevMode ? "Y" : "S");
                    Configuration.setDevmode(this.getConnectionProcessor().getSapphireConnection().getDatabaseId(), isDevMode);
                    cp.setSysConfigProperty("compcode", "");
                    Configuration.setCompcode(this.getConnectionProcessor().getSapphireConnection().getDatabaseId(), "");
                }
            } else if (ctrlKey && shiftKey) {
                if (this.getQueryProcessor().getSqlDataSet("SELECT propertyid, propertyvalue FROM sysconfig WHERE propertyid='devmode'").size() == 1) {
                    cp.setSysConfigProperty("devmode", "Y");
                    Configuration.setDevmode(this.getConnectionProcessor().getSapphireConnection().getDatabaseId(), true);
                    cp.setSysConfigProperty("compcode", "");
                    Configuration.setCompcode(this.getConnectionProcessor().getSapphireConnection().getDatabaseId(), "");
                }
            } else if (compDevCodes.size() > 0) {
                if (compcode.length() == 0) {
                    compcode = compDevCodes.get(0);
                } else if (compDevCodes.get(compDevCodes.size() - 1).equals(compcode)) {
                    compcode = "";
                } else {
                    for (int i = 0; i < compDevCodes.size() - 1; ++i) {
                        if (!compDevCodes.get(i).equals(compcode)) continue;
                        compcode = compDevCodes.get(i + 1);
                        break;
                    }
                }
                cp.setSysConfigProperty("compcode", compcode);
                Configuration.setCompcode(this.getConnectionProcessor().getSapphireConnection().getDatabaseId(), compcode);
            }
        }
        catch (SapphireException e) {
            ajaxResponse.setError("Unable to change dev mode: " + e.getMessage());
        }
        ajaxResponse.print();
    }
}

