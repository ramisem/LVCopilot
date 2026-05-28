/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.http.HttpSession
 */
package com.labvantage.sapphire.pageelements.datachart.ajax;

import com.labvantage.sapphire.pageelements.datachart.argbar.Argument;
import com.labvantage.sapphire.pageelements.datachart.argbar.ArgumentBar;
import com.labvantage.sapphire.pageelements.datachart.chart.Chart;
import com.labvantage.sapphire.pageelements.datachart.data.Data;
import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import com.labvantage.sapphire.pageelements.datachart.groovy.DataBindingMap;
import com.labvantage.sapphire.pageelements.datachart.session.CachedDataChart;
import com.labvantage.sapphire.pageelements.datachart.util.Util;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.ConnectionInfo;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SetArgumentValue
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        Chart chart;
        Data data;
        PropertyList argumentValueList;
        PropertyList setArgumentValueProps;
        AjaxResponse ar = new AjaxResponse(request, response);
        String chartId = ar.getRequestParameter("chartid");
        String setArgumentValueJson = ar.getRequestParameter("setargumentvaluejson");
        if (chartId == null || chartId.isEmpty()) {
            throw new IllegalArgumentException("Chart ID is null or empty: " + chartId);
        }
        if (setArgumentValueJson == null || setArgumentValueJson.isEmpty()) {
            throw new IllegalArgumentException("Set argument value JSON is null or empty: " + setArgumentValueJson);
        }
        try {
            setArgumentValueProps = new PropertyList(new JSONObject(setArgumentValueJson));
        }
        catch (JSONException ex) {
            throw new IllegalArgumentException("Set argument value JSON is not valid: " + setArgumentValueJson, ex);
        }
        String userCacheChartId = Util.getUserChartCacheId(chartId, this.getConnectionId());
        HttpSession session = request.getSession(true);
        CachedDataChart cachedDataChart = (CachedDataChart)CacheUtil.get(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getDatabaseId(), "DataCharts", userCacheChartId);
        if (cachedDataChart == null) {
            throw new IllegalArgumentException("Cache does not contain attribute " + chartId);
        }
        ConnectionInfo connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId());
        ArgumentBar argumentBar = cachedDataChart.getArgumentBar();
        PropertyListCollection argumentDetailsCollection = setArgumentValueProps.getCollectionNotNull("argumentdetailscollection");
        for (int i = 0; i < argumentDetailsCollection.size(); ++i) {
            String argumentValue;
            PropertyList argumentDetailProps = argumentDetailsCollection.getPropertyList(i);
            String argumentId = argumentDetailProps.getProperty("argumentid");
            try {
                argumentValue = URLDecoder.decode(argumentDetailProps.getProperty("argumentvalue"), "UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                throw new ServletException("Cannot decode argument value", (Throwable)e);
            }
            Argument argument = argumentBar.getArgument(argumentId);
            argument.setArgumentValue(argumentValue, cachedDataChart.getRequestId());
        }
        try {
            argumentValueList = argumentBar.getArgumentValueList(cachedDataChart.getRequestParams(), cachedDataChart.getData().getAdapterOutputProperties(), cachedDataChart.getRequestId());
        }
        catch (SapphireException e) {
            ar.setError("Error while creating new argument value list", e);
            throw new ServletException("Error while creating new argument value list", (Throwable)e);
        }
        try {
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
        cachedDataChart = new CachedDataChart.Builder(chartId, cachedDataChart.getDataChartProps(), chart, argumentBar, data, cachedDataChart.getRequestParams(), cachedDataChart.getElements(), cachedDataChart.getRequestId()).build();
        CacheUtil.put(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getDatabaseId(), "DataCharts", userCacheChartId, cachedDataChart);
        ar.print();
    }
}

