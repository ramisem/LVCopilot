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

import com.labvantage.sapphire.pageelements.datachart.argbar.ArgumentBar;
import com.labvantage.sapphire.pageelements.datachart.chart.Chart;
import com.labvantage.sapphire.pageelements.datachart.data.Data;
import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import com.labvantage.sapphire.pageelements.datachart.groovy.DataBindingMap;
import com.labvantage.sapphire.pageelements.datachart.session.CachedDataChart;
import com.labvantage.sapphire.pageelements.datachart.util.Util;
import com.labvantage.sapphire.util.cache.CacheUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.ConnectionInfo;
import sapphire.xml.PropertyList;

public final class UpdateArgumentBar
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext sc) throws ServletException {
        PropertyList argumentBarProps;
        AjaxResponse ar = new AjaxResponse(request, response);
        String chartId = ar.getRequestParameter("chartid");
        String argumentBarJson = ar.getRequestParameter("argumentbarjson");
        String refreshDataString = ar.getRequestParameter("refreshdata");
        if (chartId == null || chartId.isEmpty()) {
            throw new IllegalArgumentException("Chart ID is null or empty: " + chartId);
        }
        if (argumentBarJson == null || argumentBarJson.isEmpty()) {
            throw new IllegalArgumentException("Argument bar JSON is null or empty: " + argumentBarJson);
        }
        if (refreshDataString == null || refreshDataString.isEmpty()) {
            throw new IllegalArgumentException("Refresh data flag is null or empty");
        }
        boolean refreshData = refreshDataString.toLowerCase().startsWith("y");
        try {
            argumentBarProps = new PropertyList(new JSONObject(argumentBarJson));
        }
        catch (JSONException ex) {
            throw new IllegalArgumentException("Argument bar JSON is not valid: " + argumentBarJson, ex);
        }
        String userCacheChartId = Util.getUserChartCacheId(chartId, this.getConnectionId());
        CachedDataChart cachedDataChart = (CachedDataChart)CacheUtil.get(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getDatabaseId(), "DataCharts", userCacheChartId);
        if (cachedDataChart == null) {
            throw new IllegalArgumentException("Cache does not contain attribute " + chartId);
        }
        ConnectionInfo connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId());
        ArgumentBar argumentBar = cachedDataChart.getArgumentBar();
        argumentBar.applyChanges(argumentBarProps, cachedDataChart.getRequestId());
        Chart chart = cachedDataChart.getChart();
        Data data = cachedDataChart.getData();
        if (refreshData) {
            PropertyList argumentValueList;
            try {
                argumentValueList = argumentBar.getArgumentValueList(cachedDataChart.getRequestParams(), cachedDataChart.getData().getDataSetProviderOutputProps(), cachedDataChart.getRequestId());
                data = new Data(cachedDataChart.getData().getDataConfiguration(), new DataBindingMap(argumentValueList, this.getConnectionId()), connectionInfo);
            }
            catch (SapphireException e) {
                ar.setError("Error while creating data", e);
                throw new ServletException("Error while creating data", (Throwable)e);
            }
            try {
                chart = new Chart(cachedDataChart.getChart().getChartConfiguration(), data, new ChartBindingMap(chartId, data, argumentValueList, this.getConnectionId()), connectionInfo, cachedDataChart.getElements());
            }
            catch (SapphireException e) {
                ar.setError("Error while creating chart", e);
                throw new ServletException("Error while creating chart", (Throwable)e);
            }
        }
        cachedDataChart = new CachedDataChart.Builder(chartId, cachedDataChart.getDataChartProps(), chart, argumentBar, data, cachedDataChart.getRequestParams(), cachedDataChart.getElements(), cachedDataChart.getRequestId()).chartRenderingInfo(cachedDataChart.getChartRenderingInfo()).build();
        CacheUtil.put(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getDatabaseId(), "DataCharts", userCacheChartId, cachedDataChart);
        ar.addCallbackArgument("chartexpired", refreshData);
        ar.addCallbackArgument("argumentbarexpired", false);
        ar.print();
    }
}

