/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.accession;

import com.labvantage.opal.util.OpalUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class GetDisplayValue
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        StringBuilder sb = new StringBuilder();
        String displayvaluesql = ajaxResponse.getRequestParameter("displayvaluesql");
        String value = ajaxResponse.getRequestParameter("value");
        if (OpalUtil.isNotEmpty(displayvaluesql)) {
            displayvaluesql = StringUtil.replaceAll(displayvaluesql, "[]", value);
            DataSet ds = this.getQueryProcessor().getSqlDataSet(displayvaluesql);
            if (ds != null) {
                sb.append(ds.getColumnValues(ds.getColumnId(0), ";"));
            }
        }
        ajaxResponse.addCallbackArgument("fieldid", ajaxResponse.getRequestParameter("fieldid"));
        ajaxResponse.addCallbackArgument("value", sb.toString());
        ajaxResponse.addCallbackArgument("columnvalue", value);
        ajaxResponse.print();
    }
}

