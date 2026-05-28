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

import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class SubmitStudySamples
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String value = "";
        String studyid = ajaxResponse.getRequestParameter("studyid");
        String copies = ajaxResponse.getRequestParameter("copies");
        if (studyid != null && studyid.trim().length() > 0) {
            HashMap<String, String> actionProps = new HashMap<String, String>();
            actionProps.put("sdcid", "Sample");
            actionProps.put("copies", copies == null ? "1" : copies);
            actionProps.put("sstudyid", studyid);
            actionProps.put("storagestatus", "Allocated");
            actionProps.put("samplestatus", "Initial");
            try {
                this.getActionProcessor().processAction("AddSDI", "1", actionProps);
                value = (String)actionProps.get("newkeyid1");
            }
            catch (ActionException e) {
                this.logger.error(e.getMessage(), e);
            }
        }
        ajaxResponse.addCallbackArgument("newkeyid1", value);
        ajaxResponse.print();
    }
}

