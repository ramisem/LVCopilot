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
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class GetRefTypeValues
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String reftypeid = ajaxResponse.getRequestParameter("reftypeid");
        DataSet ds = this.getQueryProcessor().getRefTypeDataSet(reftypeid);
        String refvalueid = ds.getColumnValues("refvalueid", ";");
        String refdisplayvalue = ds.getColumnValues("refdisplayvalue", ";");
        ajaxResponse.addCallbackArgument("refvalueid", refvalueid);
        ajaxResponse.addCallbackArgument("refdisplayvalue", refdisplayvalue);
        ajaxResponse.print();
    }
}

