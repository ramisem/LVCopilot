/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.study;

import com.labvantage.opal.util.OpalUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class GetEventDefData
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String eventdefid = ajaxResponse.getRequestParameter("eventdefid", "");
        String eventdeflabel = "";
        String timepointcount = "";
        String assaytypecount = "";
        String sampletypecount = "";
        if (OpalUtil.isNotEmpty(eventdefid)) {
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT s.s_eventdefid, s.eventdeflabel, (SELECT count(t.s_eventdefid) FROM s_eventdef t WHERE t.parenteventdefid = s.s_eventdefid) timepointcount, (SELECT count(edsm.s_eventdefid) from S_EVENTDEFSTATMAP edsm where edsm.s_eventdefid = s.s_eventdefid) assaytypecount, (SELECT count(edst.s_eventdefid) FROM S_EVENTDEFSAMPLETYPE edst WHERE edst.s_eventdefid = s.s_eventdefid) sampletypecount FROM s_eventdef s WHERE s.S_EVENTDEFID = ?", (Object[])new String[]{eventdefid});
            if (ds != null && ds.size() > 0) {
                eventdeflabel = ds.getString(0, "eventdeflabel");
                timepointcount = ds.getValue(0, "timepointcount");
                assaytypecount = ds.getValue(0, "assaytypecount");
                sampletypecount = ds.getValue(0, "sampletypecount");
            } else {
                eventdefid = "";
            }
        }
        ajaxResponse.addCallbackArgument("eventdefid", eventdefid);
        ajaxResponse.addCallbackArgument("eventdeflabel", eventdeflabel);
        ajaxResponse.addCallbackArgument("timepointcount", timepointcount);
        ajaxResponse.addCallbackArgument("assaytypecount", assaytypecount);
        ajaxResponse.addCallbackArgument("sampletypecount", sampletypecount);
        ajaxResponse.print();
    }
}

