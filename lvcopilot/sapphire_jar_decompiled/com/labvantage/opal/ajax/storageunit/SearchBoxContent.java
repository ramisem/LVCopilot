/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.storageunit;

import com.labvantage.opal.util.OpalUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class SearchBoxContent
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        String elementid = ajaxResponse.getRequestParameter("elementid", "").trim();
        String searchtext = ajaxResponse.getRequestParameter("searchtext", "").trim();
        String sql = ajaxResponse.getRequestParameter("sql", "").trim();
        String trackitemid = ajaxResponse.getRequestParameter("trackitemid", "").trim();
        String trackitems = "";
        if (OpalUtil.isEmpty(searchtext)) {
            message = this.getTranslationProcessor().translate("Search field is empty");
        } else if (OpalUtil.isEmpty(trackitemid)) {
            message = this.getTranslationProcessor().translate("Box is empty");
        } else {
            SafeSQL safeSQL = new SafeSQL();
            sql = StringUtil.replaceAll(sql, "[searchtext]", safeSQL.addVar(searchtext));
            sql = sql + " and trackitem.trackitemid in (" + safeSQL.addIn(trackitemid, ";") + ")";
            DataSet dataSet = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (OpalUtil.isNotEmpty(dataSet)) {
                trackitems = dataSet.getColumnValues("trackitemid", ";");
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("elementid", elementid);
        ajaxResponse.addCallbackArgument("trackitemid", trackitems);
        ajaxResponse.addCallbackArgument("searchtext", searchtext);
        ajaxResponse.print();
    }
}

