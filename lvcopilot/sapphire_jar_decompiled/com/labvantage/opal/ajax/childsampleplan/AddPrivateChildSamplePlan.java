/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.childsampleplan;

import com.labvantage.opal.actions.AddPrivatePrivateSamplePlanAction;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class AddPrivateChildSamplePlan
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        String childsampleplanid = "";
        String childsampleplanversionid = "";
        try {
            PropertyList props = new PropertyList();
            props.setProperty("sdiworkitemid", ajaxResponse.getRequestParameter("sdiworkitemid", ""));
            props.setProperty("childsampleplanid", ajaxResponse.getRequestParameter("childsampleplanid", ""));
            props.setProperty("childsampleplanversionid", ajaxResponse.getRequestParameter("childsampleplanversionid", ""));
            props.setProperty("childsampleplanitemid", ajaxResponse.getRequestParameter("childsampleplanitemid", ""));
            props.setProperty("eventdefid", ajaxResponse.getRequestParameter("eventdefid", ""));
            props.setProperty("specimendefid", ajaxResponse.getRequestParameter("specimendefid", ""));
            props.setProperty("sampletypeid", ajaxResponse.getRequestParameter("sampletypeid", ""));
            props.setProperty("workitemid", ajaxResponse.getRequestParameter("workitemid"));
            props.setProperty("workitemversionid", ajaxResponse.getRequestParameter("workitemversionid"));
            props.setProperty("workiteminstance", ajaxResponse.getRequestParameter("workiteminstance"));
            this.getActionProcessor().processActionClass(AddPrivatePrivateSamplePlanAction.class.getName(), props);
            childsampleplanid = props.getProperty("childsampleplanid");
            childsampleplanversionid = props.getProperty("childsampleplanversionid");
        }
        catch (ActionException e) {
            message = e.getMessage();
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("childsampleplanid", childsampleplanid);
        ajaxResponse.addCallbackArgument("childsampleplanversionid", childsampleplanversionid);
        ajaxResponse.addCallbackArgument("rownum", ajaxResponse.getRequestParameter("rownum", ""));
        ajaxResponse.print();
    }
}

