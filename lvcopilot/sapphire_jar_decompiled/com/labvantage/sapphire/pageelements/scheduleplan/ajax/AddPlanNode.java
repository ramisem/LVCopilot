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

public class AddPlanNode
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ar = new AjaxResponse(request, response);
        String schedulePlanId = ar.getRequestParameter("scheduleplanid");
        String nodeStatus = ar.getRequestParameter("nodestatus");
        String schedulePlanNodeDesc = ar.getRequestParameter("scheduleplannodedesc");
        String nodeId = ar.getRequestParameter("nodeid");
        String sdcId = ar.getRequestParameter("sdcid");
        String keyId1 = ar.getRequestParameter("keyid1");
        String keyId2 = ar.getRequestParameter("keyid2");
        String keyId3 = ar.getRequestParameter("keyid3");
        if (schedulePlanNodeDesc.isEmpty()) {
            schedulePlanNodeDesc = keyId1;
            if (!keyId2.isEmpty()) {
                schedulePlanNodeDesc = ", " + keyId2;
            }
        }
        if (schedulePlanId.isEmpty()) {
            throw new ServletException("Schedule plan ID is empty");
        }
        if (nodeId.isEmpty()) {
            throw new ServletException("Node ID is empty");
        }
        if (schedulePlanNodeDesc.isEmpty()) {
            throw new ServletException("Schedule plan node description is empty");
        }
        SchedulerAdminProcessor schedulerAdminProcessor = new SchedulerAdminProcessor(this.getConnectionid());
        PropertyList scheduleAdminProps = new PropertyList();
        scheduleAdminProps.setProperty("scheduleplanid", schedulePlanId);
        scheduleAdminProps.setProperty("selectednodeid", nodeId);
        scheduleAdminProps.setProperty("selectednodestatus", nodeStatus);
        scheduleAdminProps.setProperty("scheduleplannodedesc", schedulePlanNodeDesc);
        scheduleAdminProps.setProperty("nodesequence", "1");
        scheduleAdminProps.setProperty("refsdcid", sdcId);
        scheduleAdminProps.setProperty("refkeyid1", keyId1);
        scheduleAdminProps.setProperty("refkeyid2", keyId2);
        scheduleAdminProps.setProperty("refkeyid3", keyId3);
        scheduleAdminProps.setProperty("mode", "AddNode");
        try {
            schedulerAdminProcessor.processScheduleAdmin(scheduleAdminProps);
        }
        catch (Exception e) {
            throw new ServletException("Cannot process schedule admin processor", (Throwable)e);
        }
        ar.print();
    }
}

