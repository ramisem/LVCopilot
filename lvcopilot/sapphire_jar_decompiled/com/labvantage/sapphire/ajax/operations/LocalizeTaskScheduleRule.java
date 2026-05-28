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

import com.labvantage.sapphire.I18nUtil;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.ConnectionInfo;
import sapphire.util.M18NUtil;
import sapphire.util.StringUtil;

public class LocalizeTaskScheduleRule
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "handleLocalizedScheduleRule");
        String rule = ajaxResponse.getRequestParameter("rule");
        ConnectionInfo connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId());
        M18NUtil m18n = new M18NUtil(connectionInfo);
        if (rule.length() > 0) {
            try {
                String[] parts = StringUtil.split(rule, ";");
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                String startdate = parts[1];
                sdf.parse(startdate);
                startdate = m18n.formatDateOnly(sdf.getCalendar(), false);
                String starttime = parts[2];
                String enddate = "";
                String parts3start = parts[3].substring(0, 1);
                if ("D".equals(parts3start)) {
                    enddate = parts[3].substring(1);
                    sdf.parse(enddate);
                    enddate = m18n.formatDateOnly(sdf.getCalendar(), false);
                } else if ("O".equals(parts3start)) {
                    enddate = parts[3].substring(1);
                }
                ajaxResponse.addCallbackArgument("rule", startdate + ";" + starttime + ";" + parts[3].substring(0, 1) + enddate + ";" + (I18nUtil.getConnectionTimeZone(connectionInfo).equals(TimeZone.getDefault()) ? "" : "(Time in system time zone,<br/>" + TimeZone.getDefault().getDisplayName(m18n.getLocale()) + ")"));
            }
            catch (Exception e) {
                ajaxResponse.setError("Cannot parse schedule rule:" + rule);
            }
        }
        ajaxResponse.print();
    }
}

