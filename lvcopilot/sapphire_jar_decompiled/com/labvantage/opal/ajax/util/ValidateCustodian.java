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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class ValidateCustodian
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        String validated = "N";
        String img = "";
        String sysuserid = ajaxResponse.getRequestParameter("sysuserid", "");
        String sysuserpwd = ajaxResponse.getRequestParameter("sysuserpwd", "");
        String mode = ajaxResponse.getRequestParameter("mode", "");
        if (sysuserid.length() > 0 && sysuserpwd.length() > 0) {
            if (this.getConnectionProcessor().checkUser(sysuserid, sysuserpwd)) {
                validated = "Y";
                img = "<img src='rc?command=image&image=FlatBlackCorrectCheck&color=green'>";
            } else {
                validated = "N";
                img = "<img src='rc?command=image&image=FlatBlackCloseRemove2&color=red'>";
                if (this.getConnectionProcessor().getLastErrorMessage().indexOf("Max login attempts exceeded") > 0) {
                    message = this.getTranslationProcessor().translate("User disabled. Reason: Max login attempts exceeded.");
                }
            }
        } else {
            mode = "";
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("validated", validated);
        ajaxResponse.addCallbackArgument("img", img);
        ajaxResponse.addCallbackArgument("mode", mode);
        ajaxResponse.print();
    }
}

