/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.request;

import com.labvantage.sapphire.actions.label.GenerateLabel;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class PrintRequestKitLabels
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        String labelmethodid = ajaxResponse.getRequestParameter("labelmethodid", "").trim();
        String printeraddressid = ajaxResponse.getRequestParameter("printeraddressid", "").trim();
        String numcopies = ajaxResponse.getRequestParameter("numcopies", "").trim();
        String trackitemid = ajaxResponse.getRequestParameter("trackitemid", "").trim();
        PropertyList props = new PropertyList();
        props.setProperty("labelsdcid", "TrackItemSDC");
        props.setProperty("keyid1", trackitemid);
        props.setProperty("labelmethodid", labelmethodid);
        props.setProperty("numcopies", numcopies);
        props.setProperty("numcopies", numcopies);
        props.setProperty("printeraddressid", printeraddressid);
        props.setProperty("printeraddresstype", "Device");
        try {
            this.getActionProcessor().processActionClass(GenerateLabel.class.getName(), props);
        }
        catch (ActionException e) {
            message = e.getMessage();
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.print();
    }
}

