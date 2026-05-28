/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.scheduleplan.ajax;

import com.labvantage.sapphire.scheduler.SchedulerAdminProcessor;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class RemovePlanNode
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ar = new AjaxResponse(request, response);
        String schedulePlanId = ar.getRequestParameter("scheduleplanid");
        String nodeId = ar.getRequestParameter("nodeid");
        if (schedulePlanId.isEmpty()) {
            throw new ServletException("Schedule plan ID is empty");
        }
        if (nodeId.isEmpty()) {
            throw new ServletException("Node ID is empty");
        }
        SchedulerAdminProcessor schedulerAdminProcessor = new SchedulerAdminProcessor(this.getConnectionid());
        PropertyList scheduleAdminProps = new PropertyList();
        scheduleAdminProps.setProperty("scheduleplanid", schedulePlanId);
        scheduleAdminProps.setProperty("selectednodeid", nodeId);
        scheduleAdminProps.setProperty("mode", "DeleteFolder");
        try {
            schedulerAdminProcessor.processScheduleAdmin(scheduleAdminProps);
        }
        catch (Exception e) {
            throw new ServletException("Cannot process schedule admin processor", (Throwable)e);
        }
        if (schedulerAdminProcessor.getErrorStack().size() > 0) {
            ar.setError("Cannot process schedule admin processor. Please check the logfiles.");
        }
        ar.print();
    }
}

