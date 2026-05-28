/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.report.digitalsignature.ajax;

import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class GetCSRForGoogleKMS
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ar = new AjaxResponse(request, response);
        try {
            HashMap hm = (HashMap)ar.getRequestParameters();
            this.getActionProcessor().processAction("GenerateCSRForGoogleSigning", "1", hm);
            ar.addCallbackArgument("csr", hm.getOrDefault("csr", ""));
        }
        catch (ActionException e) {
            ar.setError("Failed to generate CSR: " + e.getMessage());
        }
        ar.print();
    }
}

