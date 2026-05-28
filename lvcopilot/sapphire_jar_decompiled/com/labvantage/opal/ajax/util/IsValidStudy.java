/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.util;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class IsValidStudy
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        String studyid = ajaxResponse.getRequestParameter("studyid");
        SafeSQL safeSQL = new SafeSQL();
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_studyid from s_study where s_studyid = " + safeSQL.addVar(studyid), safeSQL.getValues());
        if (ds == null || ds.size() != 1) {
            message = this.getTranslationProcessor().translate("Not a valid Study") + ": " + studyid;
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("studyid", studyid);
        ajaxResponse.print();
    }
}

