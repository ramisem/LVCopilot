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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class GetBasedOnPropertyColumns
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        int i;
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        DataSet ds = this.getSDCProcessor().getColumnData("Sample");
        for (i = 0; i < ds.size(); ++i) {
            if (ds.getString(i, "columnlabel", "").length() != 0) continue;
            ds.setString(i, "columnlabel", ds.getString(i, "columnid"));
        }
        ds.sort("columnlabel");
        ajaxResponse.addCallbackArgument("columns_sample", ds.getColumnValues("columnid", ";"));
        ajaxResponse.addCallbackArgument("columnnames_sample", ds.getColumnValues("columnlabel", ";"));
        ds = this.getSDCProcessor().getColumnData("LV_SampleFamily");
        for (i = 0; i < ds.size(); ++i) {
            if (ds.getString(i, "columnlabel", "").length() != 0) continue;
            ds.setString(i, "columnlabel", ds.getString(i, "columnid"));
        }
        ds.sort("columnlabel");
        ajaxResponse.addCallbackArgument("columns_samplefamily", ds.getColumnValues("columnid", ";"));
        ajaxResponse.addCallbackArgument("columnnames_samplefamily", ds.getColumnValues("columnlabel", ";"));
        ds = this.getSDCProcessor().getColumnData("TrackItemSDC");
        for (i = 0; i < ds.size(); ++i) {
            if (ds.getString(i, "columnlabel", "").length() != 0) continue;
            ds.setString(i, "columnlabel", ds.getString(i, "columnid"));
        }
        ds.sort("columnlabel");
        ajaxResponse.addCallbackArgument("columns_trackitem", ds.getColumnValues("columnid", ";"));
        ajaxResponse.addCallbackArgument("columnnames_trackitem", ds.getColumnValues("columnlabel", ";"));
        ajaxResponse.print();
    }
}

