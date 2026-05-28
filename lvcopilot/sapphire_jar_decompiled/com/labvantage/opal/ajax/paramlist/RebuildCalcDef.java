/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.paramlist;

import com.labvantage.opal.actions.paramlist.SyncCrossSDICalcInfoForParamList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class RebuildCalcDef
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String responseMessage;
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String paramlistidprop = ajaxResponse.getRequestParameter("paramlistid", "");
        String paramlistversionidprop = ajaxResponse.getRequestParameter("paramlistversionid", "");
        String variantidprop = ajaxResponse.getRequestParameter("variantid", "");
        String fromSDCprop = ajaxResponse.getRequestParameter("forsdc", "");
        try {
            PropertyList props = new PropertyList();
            props.setProperty("paramlistid", paramlistidprop);
            props.setProperty("paramlistversionid", paramlistversionidprop);
            props.setProperty("variantid", variantidprop);
            props.setProperty("fromsdc", fromSDCprop);
            props.setProperty("separator", "~");
            this.getActionProcessor().processActionClass(SyncCrossSDICalcInfoForParamList.class.getName(), props, true);
            responseMessage = "Rebuild CrossSDICalcDef created successfully";
        }
        catch (SapphireException e) {
            responseMessage = "Failed to Rebuild CrossSDICalcDef. Reason: " + e.getMessage();
        }
        ajaxResponse.addCallbackArgument("responseMessage", responseMessage);
        ajaxResponse.print();
    }
}

