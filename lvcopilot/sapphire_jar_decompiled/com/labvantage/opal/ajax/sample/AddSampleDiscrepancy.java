/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.sample;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class AddSampleDiscrepancy
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sampleid = ajaxResponse.getRequestParameter("sampleid");
        String discrepancy = ajaxResponse.getRequestParameter("discrepancy");
        PropertyList props = new PropertyList();
        props.setProperty("sampleid", sampleid.replaceAll("%3B", ";"));
        props.setProperty("discrepancy", discrepancy);
        try {
            this.getActionProcessor().processActionClass(com.labvantage.opal.actions.AddSampleDiscrepancy.class.getName(), props);
            ajaxResponse.addCallbackArgument("message", this.getTranslationProcessor().translate("Discrepancies have been added successfully to samples"));
        }
        catch (ActionException e) {
            this.logger.error("Error", e);
            ajaxResponse.addCallbackArgument("message", this.getTranslationProcessor().translate(e.getMessage()));
        }
        ajaxResponse.print();
    }
}

