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

import com.labvantage.sapphire.scheduler.ScheduleRule;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ConnectionProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.ConnectionInfo;

public class TestScheduleRule
extends BaseAjaxRequest {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxresponse = new AjaxResponse(request, response);
        ConnectionProcessor cp = this.getConnectionProcessor();
        ConnectionInfo info = cp.getConnectionInfo(cp.getConnectionid());
        try {
            String rule = ajaxresponse.getRequestParameter("rule");
            if (rule.length() > 0) {
                ScheduleRule scheduleRule = new ScheduleRule();
                int r = scheduleRule.setRule(rule);
                if (r == -1 || !scheduleRule.isValidRule()) {
                    ajaxresponse.addCallbackArgument("result", "N");
                } else {
                    ajaxresponse.addCallbackArgument("result", "Y");
                }
            } else {
                ajaxresponse.setError("No rule provided.");
            }
        }
        catch (Exception e) {
            this.logger.error("Unable to execute TestScheduleRule", e);
            ajaxresponse.setError("Unable to execute TestScheduleRule");
        }
        finally {
            ajaxresponse.print();
        }
    }
}

