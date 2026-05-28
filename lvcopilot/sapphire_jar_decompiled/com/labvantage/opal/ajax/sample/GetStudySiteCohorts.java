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

public class GetStudySiteCohorts
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String studysiteid = ajaxResponse.getRequestParameter("studysiteid", "").trim();
        String rowindex = ajaxResponse.getRequestParameter("rowindex", "").trim();
        String data = "";
        if (studysiteid.length() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            StringBuilder sql = new StringBuilder();
            sql.append("select co.s_cpcohortid");
            sql.append(" from s_cpcohort co, s_studysite ss, s_clinicalprotocol cp");
            sql.append(" where ss.s_studysiteid= ").append(safeSQL.addVar(studysiteid));
            sql.append(" and ss.clinicalprotocolid = co.s_clinicalprotocolid");
            sql.append(" and ss.clinicalprotocolrevision = co.s_clinicalprotocolrevision");
            sql.append(" and co.s_clinicalprotocolid = cp.s_clinicalprotocolid");
            sql.append(" and co.s_clinicalprotocolrevision = cp.s_clinicalprotocolrevision");
            sql.append(" and co.s_clinicalprotocolversionid = cp.s_clinicalprotocolversionid");
            sql.append(" and cp.versionstatus = 'C'");
            sql.append(" order by co.s_cpcohortid");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                data = ds.getColumnValues("s_cpcohortid", ";");
            }
        }
        ajaxResponse.addCallbackArgument("data", data);
        ajaxResponse.addCallbackArgument("rowindex", rowindex);
        ajaxResponse.print();
    }
}

