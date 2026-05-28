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
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import com.labvantage.sapphire.pageelements.datachart.session.CachedDataChart;
import com.labvantage.sapphire.pageelements.datachart.util.Util;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public final class EvaluateExpression
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext sc) throws ServletException {
        ChartBindingMap chartBindingMap;
        Integer itemIndex;
        AjaxResponse ar = new AjaxResponse(request, response);
        String chartId = ar.getRequestParameter("chartid");
        String plotId = ar.getRequestParameter("plotid");
        String seriesGroupId = ar.getRequestParameter("seriesgroupid");
        String seriesId = ar.getRequestParameter("seriesid");
        String item = ar.getRequestParameter("item");
        String expression = ar.getRequestParameter("expression");
        String target = ar.getRequestParameter("target");
        if (chartId == null || chartId.isEmpty()) {
            throw new IllegalArgumentException("Chart ID is null or empty: " + chartId);
        }
        if (plotId == null || plotId.isEmpty()) {
            throw new IllegalArgumentException("Plot ID is null or empty: " + plotId);
        }
        if (seriesGroupId == null || seriesGroupId.isEmpty()) {
            throw new IllegalArgumentException("Series group ID is null or empty: " + seriesGroupId);
        }
        if (seriesId == null || seriesId.isEmpty()) {
            throw new IllegalArgumentException("Series ID is null or empty: " + seriesId);
        }
        if (item == null || item.isEmpty()) {
            throw new IllegalArgumentException("Item is null or empty: " + item);
        }
        if (expression == null) {
            throw new IllegalArgumentException("Expression is null");
        }
        try {
            expression = URLDecoder.decode(expression, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new ServletException("Cannot decode expression with UTF-8", (Throwable)e);
        }
        try {
            itemIndex = Integer.parseInt(item);
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Item is not a valid number; " + item, e);
        }
        String userCacheChartId = Util.getUserChartCacheId(chartId, this.getConnectionId());
        CachedDataChart cachedDataChart = (CachedDataChart)CacheUtil.get(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getDatabaseId(), "DataCharts", userCacheChartId);
        if (cachedDataChart == null) {
            throw new IllegalArgumentException("Cache does not contain attribute " + chartId);
        }
        StringExpression stringExpression = new StringExpression(expression);
        try {
            chartBindingMap = new ChartBindingMap(chartId, cachedDataChart.getData(), cachedDataChart.getArgumentBar().getArgumentValueList(cachedDataChart.getRequestParams(), cachedDataChart.getRequestId()), this.getConnectionId());
        }
        catch (SapphireException e) {
            throw new ServletException("Cannot create chart binding map", (Throwable)e);
        }
        chartBindingMap.setActiveItem(plotId, seriesGroupId, seriesId, itemIndex);
        String result = stringExpression.evaluateNoException(chartBindingMap);
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("result", result);
        data.put("target", target);
        ar.addCallbackArgument("result", data);
        ar.print();
    }
}

