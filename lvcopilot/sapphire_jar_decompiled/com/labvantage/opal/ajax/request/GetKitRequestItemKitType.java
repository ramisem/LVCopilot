/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.request;

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

public class GetKitRequestItemKitType
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        String requestitemid = ajaxResponse.getRequestParameter("requestitemid", "");
        String reagenttypeid = "";
        String reagenttypeversionid = "";
        if (requestitemid.length() > 0) {
            requestitemid = StringUtil.replaceAll(requestitemid, "|", ";");
            SafeSQL safeSQL = new SafeSQL();
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select distinct reagenttypeid, coalesce(reagenttypeversionid, '1') reagenttypeversionid from s_requestitem where s_requestitemid in (" + safeSQL.addIn(requestitemid, ";") + ")", safeSQL.getValues());
            if (OpalUtil.isNotEmpty(ds)) {
                reagenttypeid = ds.getColumnValues("reagenttypeid", ";");
                reagenttypeversionid = ds.getColumnValues("reagenttypeversionid", ";");
            } else {
                message = this.getTranslationProcessor().translate("Failed to get Kit type information. If problem persists, please contact Administrator.");
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("reagenttypeid", reagenttypeid);
        ajaxResponse.addCallbackArgument("reagenttypeversionid", reagenttypeversionid);
        ajaxResponse.print();
    }
}

