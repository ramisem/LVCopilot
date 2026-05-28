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

public class ThirdPartyTransferValidation
extends BaseSampleValidation {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        ErrorHandler errorHandler = new ErrorHandler();
        String sampleid = ajaxResponse.getRequestParameter("keyid1");
        DataSet sampleDs = this.getSamples(sampleid);
        if (sampleDs != null) {
            this.validate("ALL", sampleDs, errorHandler, "");
        }
        if (errorHandler.size() > 0) {
            message = ErrorUtil.formatErrorMessage(errorHandler);
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.print();
    }
}

