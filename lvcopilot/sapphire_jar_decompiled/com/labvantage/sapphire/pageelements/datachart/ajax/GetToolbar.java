/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.datachart.ajax;

import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import com.labvantage.sapphire.pageelements.datachart.session.CachedDataChart;
import com.labvantage.sapphire.pageelements.datachart.util.Util;
import com.labvantage.sapphire.util.cache.CacheUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public final class GetToolbar
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext sc) throws ServletException {
        AjaxResponse ar = new AjaxResponse(request, response);
        String chartId = ar.getRequestParameter("chartid");
        if (chartId == null || chartId.isEmpty()) {
            throw new IllegalArgumentException("Chart ID is null or empty: " + chartId);
        }
        String userCacheChartId = Util.getUserChartCacheId(chartId, this.getConnectionId());
        CachedDataChart cachedDataChart = (CachedDataChart)CacheUtil.get(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getDatabaseId(), "DataCharts", userCacheChartId);
        if (cachedDataChart == null) {
            throw new IllegalArgumentException("Cache does not contain attribute " + userCacheChartId);
        }
        try {
            PropertyList argumentValueList = cachedDataChart.getArgumentBar().getArgumentValueList(cachedDataChart.getRequestParams(), cachedDataChart.getRequestId());
            ChartBindingMap bindingMap = new ChartBindingMap(chartId, cachedDataChart.getData(), argumentValueList, this.getConnectionId());
            ar.addCallbackArgument("toolbarjson", cachedDataChart.getChart().getToolbar().getProps(bindingMap, argumentValueList).toJSONString(false));
        }
        catch (SapphireException e) {
            throw new ServletException("Cannot create a toolbar", (Throwable)e);
        }
        ar.print();
    }
}

