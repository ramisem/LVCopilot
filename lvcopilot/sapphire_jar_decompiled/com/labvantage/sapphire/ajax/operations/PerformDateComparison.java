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

public class PerformDateComparison
extends BaseAjaxRequest {
    public static String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String date1 = ajaxResponse.getRequestParameter("date1", "N");
        String date2 = ajaxResponse.getRequestParameter("date2", "N");
        String includetimeforcomparison = ajaxResponse.getRequestParameter("includetimeforcomparison", "Y");
        String calcomparison = this.compareDates(date1, date2, includetimeforcomparison);
        ajaxResponse.addCallbackArgument("datecomparisonresult", calcomparison);
        ajaxResponse.print();
    }

    public String compareDates(String date1, String date2, String includetimeforcomparison) {
        int result;
        String datecomparisonresult = "";
        ConnectionProcessor cp = this.getConnectionProcessor();
        M18NUtil m18NUtil = new M18NUtil(cp.getConnectionInfo(cp.getConnectionid()));
        date1 = "N".equalsIgnoreCase(date1) ? m18NUtil.format(m18NUtil.getNowCalendar()) : date1;
        date2 = "N".equalsIgnoreCase(date2) ? m18NUtil.format(m18NUtil.getNowCalendar()) : date2;
        Calendar calDate1 = m18NUtil.parseCalendar(date1);
        Calendar calDate2 = m18NUtil.parseCalendar(date2);
        if ("N".equalsIgnoreCase(includetimeforcomparison)) {
            calDate1.set(11, 0);
            calDate1.set(12, 0);
            calDate1.set(13, 0);
            calDate1.set(14, 0);
            calDate2.set(11, 0);
            calDate2.set(12, 0);
            calDate2.set(13, 0);
            calDate2.set(14, 0);
        }
        datecomparisonresult = (result = calDate1.compareTo(calDate2)) == -1 ? "before" : (result == 0 ? "same" : "after");
        return datecomparisonresult;
    }
}

