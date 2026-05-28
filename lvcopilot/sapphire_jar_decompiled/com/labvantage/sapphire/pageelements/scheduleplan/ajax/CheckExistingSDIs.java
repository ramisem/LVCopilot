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
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class CheckExistingSDIs
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ar = new AjaxResponse(request, response);
        String schedulePlanIds = ar.getRequestParameter("scheduleplanid");
        String schedulePlanItemIds = ar.getRequestParameter("scheduleplanitemid");
        if (schedulePlanIds.isEmpty()) {
            throw new ServletException("Schedule plan ID not defined");
        }
        List<String> schedulePlanIdList = Arrays.asList(schedulePlanIds.split(";"));
        List<String> schedulePlanItemIdList = Arrays.asList(schedulePlanItemIds.split(";"));
        boolean hasSamples = false;
        boolean hasWorkorders = false;
        try {
            for (int i = 0; i < schedulePlanIdList.size(); ++i) {
                int nofWorkOrders;
                String schedulePlanId = schedulePlanIdList.get(i);
                String schedulePlanItemId = schedulePlanItemIdList.get(i);
                if (schedulePlanItemId.isEmpty()) continue;
                Object[] confirmDeleteParams = new String[]{schedulePlanId, schedulePlanItemId};
                int nofSamples = this.getQueryProcessor().getPreparedCount("select count(s_sampleid) from s_sample where eventplan = ? and eventplanitem = ?", confirmDeleteParams);
                if (nofSamples > 0) {
                    hasSamples = true;
                }
                if ((nofWorkOrders = this.getQueryProcessor().getPreparedCount("select count(workorderid) from workorder w  where w.scheduleplanid = ? and w.scheduleplanitemid = ? ", confirmDeleteParams)) <= 0) continue;
                hasWorkorders = true;
            }
        }
        catch (SapphireException e) {
            ar.setError("Could not determine existing samples or workorders");
        }
        boolean hasSdis = hasSamples || hasWorkorders;
        ar.addCallbackArgument("hassdis", hasSdis);
        ar.print();
    }
}

