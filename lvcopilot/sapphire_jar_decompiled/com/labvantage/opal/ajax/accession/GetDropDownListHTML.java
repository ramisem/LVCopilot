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

public class GetDropDownListHTML
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        StringBuilder sb = new StringBuilder();
        String sourcecolumnid = ajaxResponse.getRequestParameter("sourcecolumnid");
        String sourcecolumnvalue = ajaxResponse.getRequestParameter("sourcecolumnvalue");
        String ddsql = ajaxResponse.getRequestParameter("ddsql");
        if (OpalUtil.isNotEmpty(ddsql)) {
            ddsql = StringUtil.replaceAll(ddsql, "[" + sourcecolumnid + "]", sourcecolumnvalue);
            DataSet ds = this.getQueryProcessor().getSqlDataSet(ddsql);
            if (ds != null) {
                sb.append(ds.getColumnValues(ds.getColumnId(0), ";"));
            }
        }
        ajaxResponse.addCallbackArgument("rowid", ajaxResponse.getRequestParameter("rowid"));
        ajaxResponse.addCallbackArgument("destcolumnid", ajaxResponse.getRequestParameter("destcolumnid"));
        ajaxResponse.addCallbackArgument("html", sb.toString());
        ajaxResponse.print();
    }
}

