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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class ValidateKit
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String fieldid = ajaxResponse.getRequestParameter("fieldid");
        String kittrackitem = ajaxResponse.getRequestParameter("kittrackitem", "");
        String message = "";
        if (this.getQueryProcessor().getPreparedSqlDataSet("select trackitemid from trackitem where containertypeid = 'Kit' and trackitemid = ? or trackitemlabel = ?", (Object[])new String[]{kittrackitem, kittrackitem}).size() == 0) {
            message = this.getTranslationProcessor().translate("Kit does not exist") + " (" + kittrackitem + ")";
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("fieldid", fieldid);
        ajaxResponse.addCallbackArgument("kittrackitem", kittrackitem);
        ajaxResponse.print();
    }
}

