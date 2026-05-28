/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.util;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class GetActionProgressStatus
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String trackprogessid = ajaxResponse.getRequestParameter("trackprogressid", "").trim();
        String message = "";
        String progressstatus = "";
        if (!trackprogessid.isEmpty()) {
            String status;
            boolean deleteTrackProgressProperty = false;
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT PROPERTYVALUE FROM PROFILEPROPERTY WHERE PROFILEID = ? and SYSUSERID = ? and PROPERTYID = ?", (Object[])new String[]{"System", this.getConnectionProcessor().getSapphireConnection().getSysuserId(), "ACTIONPROGRESS-" + trackprogessid});
            String string = status = ds != null && ds.size() > 0 ? ds.getString(0, "propertyvalue", "") : "";
            if (status.startsWith("||COMPLETED||")) {
                message = status.substring(13);
                deleteTrackProgressProperty = true;
                progressstatus = "C";
            } else if (status.startsWith("||ERROR||")) {
                message = status.substring(9);
                deleteTrackProgressProperty = true;
                progressstatus = "E";
            } else {
                message = status;
            }
            if (deleteTrackProgressProperty) {
                this.getQueryProcessor().execPreparedUpdate("DELETE FROM PROFILEPROPERTY WHERE PROFILEID = ? and SYSUSERID = ? and PROPERTYID = ?", new String[]{"System", this.getConnectionProcessor().getSapphireConnection().getSysuserId(), "ACTIONPROGRESS-" + trackprogessid});
            }
        }
        ajaxResponse.addCallbackArgument("status", progressstatus);
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.print();
    }
}

