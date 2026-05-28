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

import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SDIRequest;

public class GetReports
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sdcid = ajaxResponse.getRequestParameter("sdcid");
        String sqlWhere = "sdcidvalue = '" + sdcid + "'";
        sqlWhere = sqlWhere + " and ( versionstatus='C' or versionstatus='P' ) and ( outputformat = 'Report' or outputformat is null ) ";
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setRequestItem("primary");
        sdiRequest.setSDCid("Report");
        sdiRequest.setQueryFrom("report");
        sdiRequest.setQueryWhere(sqlWhere);
        sdiRequest.setQueryOrderBy("reportid,versionstatus, cast( reportversionid as integer ) desc");
        DataSet ds = this.getSDIProcessor().getSDIData(sdiRequest).getDataset("primary");
        ArrayList<DataSet> groupedReports = ds.getGroupedDataSets("reportid");
        DataSet filterDS = new DataSet();
        filterDS.addColumn("reportid", 0);
        for (int g = 0; g < groupedReports.size(); ++g) {
            DataSet report = groupedReports.get(g);
            int r = filterDS.addRow();
            filterDS.setString(r, "reportid", report.getString(0, "reportid"));
            filterDS.setString(r, "reportversionid", report.getString(0, "reportversionid"));
        }
        ajaxResponse.addCallbackArgument("ds", filterDS);
        ajaxResponse.print();
    }
}

