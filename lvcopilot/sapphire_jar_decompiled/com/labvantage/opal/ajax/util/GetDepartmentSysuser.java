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
import sapphire.util.DataSet;

public class GetDepartmentSysuser
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        DataSet ds;
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        String departmentid = ajaxResponse.getRequestParameter("departmentid", "");
        String sysuserid = "";
        if (departmentid.length() > 0 && OpalUtil.isNotEmpty(ds = this.getQueryProcessor().getPreparedSqlDataSet("select sysuser.sysuserid from departmentsysuser, sysuser where sysuser.sysuserid = departmentsysuser.sysuserid and departmentsysuser.departmentid = ? and sysuser.activeflag = 'Y' and (sysuser.disabledflag is null or sysuser.disabledflag != 'Y') and (sysuser.templateflag is null or sysuser.templateflag != 'Y') order by sysuser.sysuserid", (Object[])new String[]{departmentid}))) {
            sysuserid = ds.getColumnValues("sysuserid", ";");
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("sysuserid", sysuserid);
        ajaxResponse.print();
    }
}

