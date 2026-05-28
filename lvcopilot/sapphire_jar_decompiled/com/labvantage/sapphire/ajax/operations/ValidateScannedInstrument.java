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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class ValidateScannedInstrument
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        QueryProcessor qp = this.getQueryProcessor();
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        TranslationProcessor tp = this.getTranslationProcessor();
        this.validateInstrument(qp, ajaxResponse, tp);
    }

    private void validateInstrument(QueryProcessor qp, AjaxResponse ajaxResponse, TranslationProcessor tp) {
        String instrumenttypeid = ajaxResponse.getRequestParameter("instrumenttypeid", "");
        String instrumentid = ajaxResponse.getRequestParameter("instrumentid", "");
        String instrumentmodelid = ajaxResponse.getRequestParameter("instrumentmodelid", "");
        String instrumentindexid = ajaxResponse.getRequestParameter("instrumentindexid");
        String testingdepartmentid = ajaxResponse.getRequestParameter("testingdepartmentid", "");
        String selectclause = ajaxResponse.getRequestParameter("selectclause");
        selectclause = StringUtil.replaceAll(selectclause, "%3B", ",");
        String errorMsg = null;
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer("select instrumentid ");
        if (selectclause.length() > 0) {
            sql.append("," + selectclause);
        }
        sql.append(" from instrument ");
        sql.append(" where instrumenttype=" + safeSQL.addVar(instrumenttypeid) + "  AND instrumentmodelid=coalesce(NULLIF(" + safeSQL.addVar(instrumentmodelid) + ",'') ,instrumentmodelid)");
        sql.append(" AND (coalesce(testingdepartmentid,' ')=coalesce(NULLIF(" + safeSQL.addVar(testingdepartmentid) + ",'') ,testingdepartmentid,' ') or testingdepartmentid is null)");
        sql.append(" AND instrumentid=" + safeSQL.addVar(instrumentid));
        DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds == null || ds.size() <= 0) {
            errorMsg = "Not a Valid Instrument";
        }
        if (errorMsg == null) {
            ajaxResponse.addCallbackArgument("errormsg", errorMsg);
        } else {
            ajaxResponse.addCallbackArgument("errormsg", tp.translate(errorMsg));
        }
        ajaxResponse.addCallbackArgument("instrumentid", instrumentid);
        ajaxResponse.addCallbackArgument("instrumentindexid", instrumentindexid);
        ajaxResponse.addCallbackArgument("ds", ds);
        ajaxResponse.print();
    }
}

