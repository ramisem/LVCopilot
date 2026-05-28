/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.validation.misc;

import com.labvantage.sapphire.gwt.shared.util.StringUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.SafeSQL;

public class ValidateCurrentWIVersion
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        String workitemid = ajaxResponse.getRequestParameter("keyid1");
        if (workitemid.length() > 0) {
            String[] s = StringUtil.split(workitemid, ";");
            SafeSQL safeSQL = new SafeSQL();
            for (String workitem : s) {
                safeSQL.reset();
                if (this.getQueryProcessor().getPreparedSqlDataSet("select workitemid from workitem where workitemid = " + safeSQL.addVar(workitem) + " and versionstatus = 'C'", safeSQL.getValues()).size() != 0) continue;
                message = message + this.getTranslationProcessor().translate("No Current version found for selected workitem (" + workitem + ")<br>");
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.print();
    }
}

