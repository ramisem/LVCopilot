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

import com.labvantage.sapphire.actions.workitem.AddSDIWorkItem;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class AddServiceToSamples
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String message = "";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sampleid = ajaxResponse.getRequestParameter("sampleid", "").trim();
        String workitemid = ajaxResponse.getRequestParameter("workitemid", "").trim();
        String workitemversionid = ajaxResponse.getRequestParameter("workitemversionid", "").trim();
        if (sampleid.length() > 0 && workitemid.length() > 0 && workitemversionid.length() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "Sample");
            props.setProperty("keyid1", sampleid);
            props.setProperty("workitemid", workitemid);
            props.setProperty("workitemversionid", workitemversionid);
            props.setProperty("__sdcruleconfirm", "Y");
            try {
                this.getActionProcessor().processActionClass(AddSDIWorkItem.class.getName(), props);
            }
            catch (ActionException e) {
                message = this.getTranslationProcessor().translate("Error adding service to Sample") + "<hr>" + e.getMessage();
                e.printStackTrace();
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.print();
    }
}

