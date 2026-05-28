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

import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.SafeHTML;
import sapphire.xml.PropertyList;

public class SetPlanItemStatus
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ar = new AjaxResponse(request, response);
        String activeFlag = ar.getRequestParameter("activeflag");
        String planItemStatus = ar.getRequestParameter("planitemstatus");
        String schedulePlanIds = ar.getRequestParameter("scheduleplanid");
        String schedulePlanItemIds = ar.getRequestParameter("scheduleplanitemid");
        if (planItemStatus.isEmpty() && activeFlag.isEmpty()) {
            throw new ServletException("Plan item Status or Active Flag not defined");
        }
        if (schedulePlanIds.isEmpty()) {
            throw new ServletException("Schedule plan ID not defined");
        }
        if (schedulePlanItemIds.isEmpty()) {
            throw new ServletException("Schedule plan item ID not defined");
        }
        List<String> planIdList = Arrays.asList(schedulePlanIds.split(";"));
        List<String> planItemIdList = Arrays.asList(schedulePlanItemIds.split(";"));
        if (planIdList.size() != planItemIdList.size()) {
            throw new ServletException("Inconsistent number of schedule plan and schedule plan item IDs");
        }
        PropertyList editSDIProps = new PropertyList();
        editSDIProps.setProperty("sdcid", "SchedulePlanItem");
        editSDIProps.setProperty("keyid1", schedulePlanIds);
        editSDIProps.setProperty("keyid2", schedulePlanItemIds);
        try {
            if (activeFlag.isEmpty()) {
                editSDIProps.setProperty("__ajaxedit", "Y");
                editSDIProps.setProperty("planitemstatus", planItemStatus);
                this.getActionProcessor().processAction("EditSDI", "1", editSDIProps);
            } else {
                editSDIProps.setProperty("activeflag", activeFlag);
                this.getActionProcessor().processAction("SetActiveFlag", "1", editSDIProps);
            }
        }
        catch (ActionException e) {
            ar.setError("Cannot change plan item status: " + SafeHTML.encodeForHTML(e.getMessage()));
        }
        ar.print();
    }
}

