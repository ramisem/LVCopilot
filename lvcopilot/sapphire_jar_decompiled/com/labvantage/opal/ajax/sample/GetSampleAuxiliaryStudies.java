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

public class GetSampleAuxiliaryStudies
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sampleid = ajaxResponse.getRequestParameter("sampleid", "").trim();
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select s_studyid from s_samples_study where s_sampleid = " + safeSQL.addVar(sampleid);
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        ajaxResponse.addCallbackArgument("sampleid", sampleid);
        ajaxResponse.addCallbackArgument("studyid", ds.getColumnValues("s_studyid", ";"));
        ajaxResponse.print();
    }
}

