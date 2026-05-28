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

import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class SetQCRequestedReviewDisposition
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String keyid1 = ajaxResponse.getRequestParameter("qcbatchid");
        String reviewDisp = ajaxResponse.getRequestParameter("requestedreviewdisposition");
        String resp = "";
        String error = "";
        TranslationProcessor tp = this.getTranslationProcessor();
        HashMap<String, String> actionprops = new HashMap<String, String>();
        actionprops.put("sdcid", "QCBatch");
        actionprops.put("keyid1", keyid1);
        actionprops.put("requestedreviewdisposition", reviewDisp);
        try {
            this.getActionProcessor().processAction("EditSDI", "1", actionprops);
            resp = tp.translate("Update successfull.");
        }
        catch (ActionException e) {
            error = e.getMessage();
        }
        if (error.length() > 0) {
            ajaxResponse.setError(tp.translate(error));
        }
        ajaxResponse.addCallbackArgument("resp", resp);
        ajaxResponse.addCallbackArgument("error", error);
        ajaxResponse.print();
    }
}

