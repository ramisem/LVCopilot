/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.storageunit;

import com.labvantage.sapphire.actions.storage.GrantCustody;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class PerformGrantCustody
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        String fromdepartmentid = this.getConnectionProcessor().getSapphireConnection().getDefaultDepartment();
        String fromsysuserid = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
        String todepartmentid = ajaxResponse.getRequestParameter("todepartmentid", "");
        String tosysuserid = ajaxResponse.getRequestParameter("tosysuserid", "");
        String reason = ajaxResponse.getRequestParameter("reason", "");
        String trackitemid = ajaxResponse.getRequestParameter("trackitemid", "");
        PropertyList props = new PropertyList();
        props.setProperty("fromdepartmentid", fromdepartmentid);
        props.setProperty("fromsysuserid", fromsysuserid);
        props.setProperty("todepartmentid", todepartmentid);
        props.setProperty("tosysuserid", tosysuserid);
        props.setProperty("reason", reason);
        props.setProperty("trackitemid", trackitemid);
        try {
            this.getActionProcessor().processActionClass(GrantCustody.class.getName(), props);
        }
        catch (SapphireException e) {
            message = e.getMessage();
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("todepartmentid", todepartmentid);
        ajaxResponse.addCallbackArgument("tosysuserid", tosysuserid);
        ajaxResponse.print();
    }
}

