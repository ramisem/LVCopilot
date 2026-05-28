/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.scheduler.ajax;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class GetCalendarTooltip
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext sc) throws ServletException {
        AjaxResponse ar = new AjaxResponse(req, resp);
        String schedulePlanId = ar.getRequestParameter("scheduleplanid", "");
        String schedulePlanItemId = ar.getRequestParameter("scheduleplanitemid", "");
        String eventDt = ar.getRequestParameter("eventdt", "");
        String eventNum = ar.getRequestParameter("eventnum", "");
        String eventStatus = ar.getRequestParameter("eventstatus", "");
        boolean userTimeZone = ar.getRequestParameter("dateinusertimezone", "").startsWith("Y");
        if (schedulePlanId.isEmpty()) {
            throw new IllegalArgumentException("Schedule Plan Id is missing");
        }
        if (schedulePlanItemId.isEmpty()) {
            throw new IllegalArgumentException("Schedule Plan Item Id is missing");
        }
        if (eventDt.isEmpty()) {
            throw new IllegalArgumentException("Event Date is missing");
        }
        if (eventNum.isEmpty()) {
            throw new IllegalArgumentException("Event Number is missing");
        }
        StringBuilder tooltip = new StringBuilder();
        tooltip.append(schedulePlanId);
        tooltip.append(" ");
        tooltip.append(schedulePlanItemId);
        tooltip.append(eventDt);
        ar.addCallbackArgument("tooltip", tooltip);
        ar.print();
    }
}

