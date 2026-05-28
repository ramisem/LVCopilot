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

public class GetStudySiteCohortVisits
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String studysiteid = ajaxResponse.getRequestParameter("studysiteid", "").trim();
        String cohortid = ajaxResponse.getRequestParameter("cohortid", "").trim();
        String rowindex = ajaxResponse.getRequestParameter("rowindex", "").trim();
        String eventdefid = "";
        String eventdeflabel = "";
        if (studysiteid.length() > 0 && rowindex.length() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            StringBuilder sql = new StringBuilder();
            sql.append("select ed.s_eventdefid, ed.eventdeflabel");
            sql.append(" from s_eventdef ed, s_studysite ss, s_clinicalprotocol cp");
            sql.append(" where ss.s_studysiteid = ").append(safeSQL.addVar(studysiteid));
            sql.append(" and ed.eventdeftype = 'Visit'");
            sql.append(" and ed.cohortid = ").append(safeSQL.addVar(cohortid));
            sql.append(" and ss.clinicalprotocolid = ed.clinicalprotocolid");
            sql.append(" and ss.clinicalprotocolrevision = ed.clinicalprotocolrevision");
            sql.append(" and ed.clinicalprotocolid = cp.s_clinicalprotocolid");
            sql.append(" and ed.clinicalprotocolrevision = cp.s_clinicalprotocolrevision");
            sql.append(" and ed.clinicalprotocolversionid = cp.s_clinicalprotocolversionid");
            sql.append(" and cp.versionstatus = 'C'");
            sql.append(" order by ed.usersequence");
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

