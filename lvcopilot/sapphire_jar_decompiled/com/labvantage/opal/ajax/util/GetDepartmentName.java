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

import com.labvantage.opal.util.OpalUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class GetDepartmentName
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String departmentid = ajaxResponse.getRequestParameter("departmentid");
        String name = OpalUtil.getColumnValue(this.getQueryProcessor(), "department", "departmentdesc", "departmentid = ?", new String[]{departmentid});
        ajaxResponse.addCallbackArgument("departmentname", name);
        ajaxResponse.addCallbackArgument("fieldid", ajaxResponse.getRequestParameter("fieldid"));
        ajaxResponse.print();
    }
}

