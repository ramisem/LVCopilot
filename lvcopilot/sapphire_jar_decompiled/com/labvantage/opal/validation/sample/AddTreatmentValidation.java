/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.validation.sample;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.validation.sample.BaseSampleValidation;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.error.ErrorHandler;
import sapphire.servlet.AjaxResponse;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class AddTreatmentValidation
extends BaseSampleValidation {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        ErrorHandler errorHandler = new ErrorHandler();
        String message = "";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sampleid = ajaxResponse.getRequestParameter("keyid1");
        if (StringUtil.getLen(sampleid) > 0L) {
            DataSet sampleDs = this.getSamples(sampleid);
            if (sampleDs != null && sampleDs.size() == StringUtil.split(sampleid, ";").length) {
                this.validate("ALL", sampleDs, errorHandler, "Add Treatment");
            } else {
                errorHandler.add(this.getTranslationProcessor().translate("Add Treatment"), "", this.getTranslationProcessor().translate("Validation failed"), "VALIDATION", this.getTranslationProcessor().translate("Corrupted data: Sample(s) not available"));
            }
        }
        if (errorHandler.size() > 0) {
            message = ErrorUtil.formatErrorMessage(errorHandler);
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.print();
    }
}

