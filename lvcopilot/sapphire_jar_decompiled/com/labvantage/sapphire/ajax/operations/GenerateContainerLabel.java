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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class GenerateContainerLabel
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String keyid1 = ajaxResponse.getRequestParameter("keyid1");
        String labelmethodid = ajaxResponse.getRequestParameter("labelmethodid");
        String printerid = ajaxResponse.getRequestParameter("printerid");
        String count = ajaxResponse.getRequestParameter("count", "1");
        PropertyList props = new PropertyList();
        props.setProperty("keyid1", keyid1);
        props.setProperty("labelmethodid", labelmethodid);
        props.setProperty("printeraddressid", printerid);
        props.setProperty("numcopies", count);
        String msg = "";
        try {
            this.getActionProcessor().processAction("GenerateLabel", "1", props);
        }
        catch (ActionException e) {
            msg = "failed";
            throw new ServletException((Throwable)e);
        }
        ajaxResponse.addCallbackArgument("msg", msg);
        ajaxResponse.print();
    }
}

