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

import java.util.Calendar;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ConnectionProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.M18NUtil;

public class CompareDates
extends BaseAjaxRequest {
    public static String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        long timeDifInMilliSec;
        long milliSec2;
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String date1 = ajaxResponse.getRequestParameter("date1");
        Calendar calDate1 = null;
        Calendar calDate2 = null;
        ConnectionProcessor cp = this.getConnectionProcessor();
        M18NUtil m18nUtil = new M18NUtil(cp.getConnectionInfo(cp.getConnectionid()));
        if (date1 == null || date1.length() == 0 || "N".equalsIgnoreCase(date1)) {
            date1 = m18nUtil.format(m18nUtil.getNowCalendar());
        }
        calDate1 = m18nUtil.parseCalendar(date1);
        calDate2 = m18nUtil.getNowCalendar();
        long milliSec1 = calDate1.getTimeInMillis();
        int calcomparison = milliSec1 >= (milliSec2 = calDate2.getTimeInMillis()) ? 1 : ((timeDifInMilliSec = milliSec2 - milliSec1) < 60000L ? 1 : -1);
        ajaxResponse.addCallbackArgument("retvalue", calcomparison);
        ajaxResponse.print();
    }
}

