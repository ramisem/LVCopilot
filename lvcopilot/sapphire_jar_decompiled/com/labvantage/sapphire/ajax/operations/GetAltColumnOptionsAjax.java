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

import com.labvantage.sapphire.modules.datafile.ValidationEditorUtil;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class GetAltColumnOptionsAjax
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "getAltColumn_Callback");
        String sdcid = ajaxResponse.getRequestParameter("sdcid");
        String column = ajaxResponse.getRequestParameter("columnid");
        String sdicheckttype = ajaxResponse.getRequestParameter("sdichecktype");
        StringBuffer html = ValidationEditorUtil.getSDICheckColumnOptions(this.getSDCProcessor(), sdcid, column);
        HashMap sdcProps = this.getSDCProcessor().getSDCProperties(sdcid);
        HashMap c = this.getSDCProcessor().getSDCProperties(sdcid);
        ajaxResponse.addCallbackArgument("table", sdcProps.get("tableid"));
        ajaxResponse.addCallbackArgument("keycolid", sdcProps.get("keycolid1"));
        ajaxResponse.addCallbackArgument("html", html.toString());
        ajaxResponse.addCallbackArgument("ispk", column.length() == 0 || column.equals(c.get("keycolid1")));
        ajaxResponse.addCallbackArgument("sdichecktype", sdicheckttype);
        ajaxResponse.print();
    }
}

