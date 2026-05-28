/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.accession;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class GetAccessionStatus
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String info = "";
        String error = "";
        String confirm = "";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String accessionid = ajaxResponse.getRequestParameter("accessionid", "");
        String sampleid = "";
        if (!accessionid.isEmpty()) {
            String status;
            boolean deleteAccessionProperty = false;
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT PROPERTYVALUE FROM PROFILEPROPERTY WHERE PROFILEID = ? and SYSUSERID = ? and PROPERTYID = ?", (Object[])new String[]{"System", this.getConnectionProcessor().getSapphireConnection().getSysuserId(), "ACTIONPROGRESS-" + accessionid});
            String string = status = ds != null && ds.size() > 0 ? ds.getString(0, "propertyvalue", "") : "";
            if (status != null) {
                if (status.startsWith("||SAMPLE||")) {
                    sampleid = status.substring(10);
                    deleteAccessionProperty = true;
                } else if (status.startsWith("||INFO||")) {
                    info = status.substring(8);
                } else if (status.startsWith("||ERROR||")) {
                    error = status.substring(9);
                    deleteAccessionProperty = true;
                } else if (status.startsWith("||CONFIRM||")) {
                    confirm = status.substring(11);
                    deleteAccessionProperty = true;
                }
            }
            if (deleteAccessionProperty) {
                this.getQueryProcessor().execPreparedUpdate("DELETE FROM PROFILEPROPERTY WHERE PROFILEID = ? and SYSUSERID = ? and PROPERTYID = ?", new String[]{"System", this.getConnectionProcessor().getSapphireConnection().getSysuserId(), "ACTIONPROGRESS-" + accessionid});
            }
        }
        ajaxResponse.addCallbackArgument("errormsg", error);
        ajaxResponse.addCallbackArgument("confirm", confirm);
        ajaxResponse.addCallbackArgument("info", info);
        ajaxResponse.addCallbackArgument("sampleid", sampleid);
        ajaxResponse.print();
    }
}

