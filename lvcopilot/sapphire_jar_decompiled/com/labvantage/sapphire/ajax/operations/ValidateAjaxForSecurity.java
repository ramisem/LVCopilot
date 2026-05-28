/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.ServletRequest
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class ValidateAjaxForSecurity
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sdcId = ajaxResponse.getRequestParameter("sdcid");
        String keyId1 = ajaxResponse.getRequestParameter("keyid1");
        TranslationProcessor tp = this.getTranslationProcessor();
        boolean secure = true;
        if (SecurityPolicyUtil.hasJSTags((ServletRequest)request, keyId1)) {
            secure = false;
        }
        ajaxResponse.addCallbackArgument("secure", secure);
        ajaxResponse.print();
    }
}

