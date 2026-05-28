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

import com.labvantage.opal.util.OpalUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class GetSampleTypeDescription
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sampletypeid = ajaxResponse.getRequestParameter("sampletypeid", "");
        ajaxResponse.addCallbackArgument("sampletypeid", ajaxResponse.getRequestParameter("sampletypeid", ""));
        ajaxResponse.addCallbackArgument("rowindex", ajaxResponse.getRequestParameter("rowindex", ""));
        ajaxResponse.addCallbackArgument("description", OpalUtil.getColumnValue(this.getQueryProcessor(), "s_sampletype", "sampletypedesc", "s_sampletypeid=?", new String[]{sampletypeid}));
        ajaxResponse.print();
    }
}

