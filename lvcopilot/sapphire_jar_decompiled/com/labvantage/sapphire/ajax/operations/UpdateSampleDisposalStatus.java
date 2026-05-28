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
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class UpdateSampleDisposalStatus
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sampleid = ajaxResponse.getRequestParameter("sampleid");
        String disposalstatus = ajaxResponse.getRequestParameter("disposalstatus");
        String resp = "";
        if (disposalstatus != null && disposalstatus.length() > 0) {
            HashMap<String, String> actionprops = new HashMap<String, String>();
            actionprops.put("sdcid", "Sample");
            actionprops.put("keyid1", sampleid);
            actionprops.put("disposalstatus", disposalstatus);
            try {
                this.getActionProcessor().processAction("EditSDI", "1", actionprops);
                resp = "update successfull";
            }
            catch (ActionException e) {
                ajaxResponse.setError(e.getMessage());
            }
        }
        ajaxResponse.addCallbackArgument("resp", resp);
        ajaxResponse.print();
    }
}

