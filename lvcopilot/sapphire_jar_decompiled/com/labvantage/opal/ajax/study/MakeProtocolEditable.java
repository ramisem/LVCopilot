/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.study;

import com.labvantage.sapphire.actions.clinicalbb.CreateProtocol;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class MakeProtocolEditable
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String message = "";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String clinicalprotocolid = ajaxResponse.getRequestParameter("clinicalprotocolid", "");
        String clinicalprotocolrevision = ajaxResponse.getRequestParameter("clinicalprotocolrevision", "");
        String clinicalprotocolversionid = ajaxResponse.getRequestParameter("clinicalprotocolversionid", "");
        String clinicalstudyid = ajaxResponse.getRequestParameter("clinicalstudyid", "");
        String newclinicalprotocolid = "";
        String newclinicalprotocolversionid = "";
        String newclinicalprotocolrevision = "";
        PropertyList props = new PropertyList();
        props.setProperty("clinicalprotocolid", clinicalprotocolid);
        props.setProperty("clinicalprotocolrevision", clinicalprotocolrevision);
        props.setProperty("clinicalprotocolversionid", clinicalprotocolversionid);
        props.setProperty("clinicalstudyid", clinicalstudyid);
        try {
            this.getActionProcessor().processActionClass(CreateProtocol.class.getName(), props);
            newclinicalprotocolid = props.getProperty("returnnewclinicalprotocolid");
            newclinicalprotocolrevision = props.getProperty("returnnewclinicalprotocolrevision");
            newclinicalprotocolversionid = props.getProperty("returnnewclinicalprotocolversionid");
        }
        catch (ActionException e) {
            message = "Exception caught: " + e.getMessage();
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("newclinicalprotocolid", newclinicalprotocolid);
        ajaxResponse.addCallbackArgument("newclinicalprotocolversionid", newclinicalprotocolversionid);
        ajaxResponse.addCallbackArgument("newclinicalprotocolrevision", newclinicalprotocolrevision);
        ajaxResponse.print();
    }
}

