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

import com.labvantage.sapphire.admin.system.automation.LAM;
import com.labvantage.sapphire.services.AutomationService;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class ToggleToDoList
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        try {
            String databaseid = this.getConnectionProcessor().getSapphireConnection().getDatabaseId();
            LAM lam = AutomationService.getLAM(databaseid);
            if (lam.isPollerActive(2)) {
                lam.pausePoller(2);
            } else {
                lam.resumePoller(2);
            }
        }
        catch (Exception e) {
            ajaxResponse.setError("Failed to load stats", e);
        }
    }
}

