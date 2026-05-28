/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.sample;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class GetVisitTimepoints
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String visitid = ajaxResponse.getRequestParameter("visitid", "").trim();
        String rowindex = ajaxResponse.getRequestParameter("rowindex", "").trim();
        String eventdefid = "";
        String eventdeflabel = "";
        if (visitid.length() > 0 && rowindex.length() > 0) {
            if (visitid.contains("|")) {
                visitid = visitid.substring(0, visitid.indexOf("|"));
            }
            SafeSQL safeSQL = new SafeSQL();
            StringBuilder sql = new StringBuilder();
            sql.append("select ed.s_eventdefid, ed.eventdeflabel");
            sql.append(" from s_eventdef ed");
            sql.append(" where ed.parenteventdefid = ").append(safeSQL.addVar(visitid));
            sql.append(" order by ed.eventdeflabel");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                eventdefid = ds.getColumnValues("s_eventdefid", ";");
                eventdeflabel = ds.getColumnValues("eventdeflabel", ";");
            }
        }
        ajaxResponse.addCallbackArgument("eventdefid", eventdefid);
        ajaxResponse.addCallbackArgument("eventdeflabel", eventdeflabel);
        ajaxResponse.addCallbackArgument("rowindex", rowindex);
        ajaxResponse.print();
    }
}

