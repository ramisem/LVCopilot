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

import com.labvantage.sapphire.DateTimeUtil;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class GetFormattedDateByShorthand
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String datetime = ajaxResponse.getRequestParameter("sourceValue", "");
        String dateformat = ajaxResponse.getRequestParameter("dateFormat", "");
        DateTimeUtil dtu = new DateTimeUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
        Calendar cal = dtu.getCalendar(datetime);
        SimpleDateFormat df = new SimpleDateFormat(dateformat);
        ajaxResponse.addCallbackArgument("date", df.format(cal.getTime()));
        ajaxResponse.print();
    }
}

