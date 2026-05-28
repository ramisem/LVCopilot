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

import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SDIRequest;

public class GetLabelMethodReport
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String labelmethodversionid = "";
        String labelmethodid = ajaxResponse.getRequestParameter("labelmethodid");
        String sqlWhere = "labelmethodid = '" + labelmethodid + "'";
        sqlWhere = sqlWhere + " and ( versionstatus = 'C' or versionstatus = 'P' )  ";
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setRequestItem("primary");
        sdiRequest.setSDCid("LV_LabelMethod");
        sdiRequest.setQueryFrom("labelmethod");
        sdiRequest.setQueryWhere(sqlWhere);
        sdiRequest.setQueryOrderBy("labelmethodid, versionstatus, cast( labelmethodversionid as integer ) desc");
        DataSet labelmethodDs = this.getSDIProcessor().getSDIData(sdiRequest).getDataset("primary");
        if (labelmethodDs.getRowCount() > 0) {
            ArrayList<DataSet> groupedLabelMethods = labelmethodDs.getGroupedDataSets("labelmethodid");
            labelmethodversionid = groupedLabelMethods.get(0).getString(0, "labelmethodversionid");
        }
        ajaxResponse.addCallbackArgument("labelmethodid", labelmethodid);
        ajaxResponse.addCallbackArgument("labelmethodversionid", labelmethodversionid);
        ajaxResponse.print();
    }
}

