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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class GetPlanTimeZone
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ar = new AjaxResponse(request, response);
        String schedulePlanId = ar.getRequestParameter("scheduleplanid");
        if (schedulePlanId.isEmpty()) {
            throw new ServletException("Schedule plan ID is empty");
        }
        String getTimeZoneSql = "SELECT timezone FROM scheduleplan WHERE scheduleplanid = ?";
        DataSet getTimeZoneDs = this.getQueryProcessor().getPreparedSqlDataSet(getTimeZoneSql, (Object[])new String[]{schedulePlanId});
        ar.addCallbackArgument("timezone", getTimeZoneDs.getString(0, "timezone"));
        ar.print();
    }
}

