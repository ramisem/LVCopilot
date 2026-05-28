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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class RemoveScheduleGroupItems
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ar = new AjaxResponse(request, response);
        String scheduleGroupId = ar.getRequestParameter("schedulegroupid");
        if (scheduleGroupId.isEmpty()) {
            throw new ServletException("Schedule group Id is empty");
        }
        if (scheduleGroupId.contains(";")) {
            throw new ServletException("Only one schedule group Id allowed");
        }
        String planItems = ar.getRequestParameter("planitems");
        try {
            planItems = URLDecoder.decode(planItems, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new ServletException("Cannot decode input params");
        }
        StringBuilder schedulePlanIds = new StringBuilder();
        StringBuilder schedulePlanItemIds = new StringBuilder();
        for (String planItem : planItems.split(";")) {
            String schedulePlanId = planItem.split("\\|")[0];
            String schedulePlanItemId = planItem.split("\\|")[1];
            schedulePlanIds.append(";").append(schedulePlanId);
            schedulePlanItemIds.append(";").append(schedulePlanItemId);
        }
        if (schedulePlanIds.length() > 0 && schedulePlanItemIds.length() > 0) {
            PropertyList deleteSDIDetailProps = new PropertyList();
            deleteSDIDetailProps.setProperty("sdcid", "LV_ScheduleGroup");
            deleteSDIDetailProps.setProperty("keyid1", scheduleGroupId);
            deleteSDIDetailProps.setProperty("linkid", "ScheduleGroup Items");
            deleteSDIDetailProps.setProperty("scheduleplanid", schedulePlanIds.substring(1));
            deleteSDIDetailProps.setProperty("scheduleplanitemid", schedulePlanItemIds.substring(1));
            try {
                this.getActionProcessor().processAction("DeleteSDIDetail", "1", deleteSDIDetailProps);
            }
            catch (ActionException e) {
                throw new ServletException("Cannot delete schedule group items", (Throwable)e);
            }
        }
        ar.print();
    }
}

