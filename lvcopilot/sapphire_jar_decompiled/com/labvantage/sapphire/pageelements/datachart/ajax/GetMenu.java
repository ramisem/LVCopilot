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

import com.labvantage.sapphire.pageelements.datachart.chart.Chart;
import com.labvantage.sapphire.pageelements.datachart.chart.toolbar.Operation;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.MenuItemConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator.EventConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator.OpenMenuEventConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator.URLGeneratorConfiguration;
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
import sapphire.xml.PropertyListCollection;

public final class GetMenu
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext sc) throws ServletException {
        ChartBindingMap chartBindingMap;
        AjaxResponse ar = new AjaxResponse(request, response);
        String chartId = ar.getRequestParameter("chartid");
        String plotId = ar.getRequestParameter("plotid");
        String urlGeneratorId = ar.getRequestParameter("urlgeneratorid");
        String eventId = ar.getRequestParameter("eventid");
        String seriesGroupId = ar.getRequestParameter("seriesgroupid");
        String seriesId = ar.getRequestParameter("seriesid");
        String item = ar.getRequestParameter("item");
        if (chartId == null || chartId.isEmpty()) {
            throw new IllegalArgumentException("Chart ID is null or empty: " + chartId);
        }
        if (plotId == null || plotId.isEmpty()) {
            throw new IllegalArgumentException("Plot ID is null or empty: " + chartId);
        }
        if (urlGeneratorId == null || urlGeneratorId.isEmpty()) {
            throw new IllegalArgumentException("URL generator ID is null or empty: " + chartId);
        }
        if (eventId == null || eventId.isEmpty()) {
            throw new IllegalArgumentException("Event ID is null or empty: " + chartId);
        }
        if (seriesGroupId == null || seriesGroupId.isEmpty()) {
            throw new IllegalArgumentException("Series group ID is null or empty: " + chartId);
        }
        if (seriesId == null || seriesId.isEmpty()) {
            throw new IllegalArgumentException("series ID is null or empty: " + chartId);
        }
        if (item == null || item.isEmpty()) {
            throw new IllegalArgumentException("Item index is null or empty: " + chartId);
        }
        String userCacheChartId = Util.getUserChartCacheId(chartId, this.getConnectionId());
        CachedDataChart cachedDataChart = (CachedDataChart)CacheUtil.get(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getDatabaseId(), "DataCharts", userCacheChartId);
        if (cachedDataChart == null) {
            throw new IllegalArgumentException("Cache does not contain attribute " + chartId);
        }
        Chart chart = cachedDataChart.getChart();
        OpenMenuEventConfiguration openMenuEventConf = null;
        for (URLGeneratorConfiguration urlGeneratorConf : chart.getChartConfiguration().getPlotConfiguration(plotId).getURLGeneratorConfigurationList()) {
            if (!urlGeneratorConf.getUrlGeneratorId().equals(urlGeneratorId)) continue;
            for (EventConfiguration eventConf : urlGeneratorConf.getEventConfigurationList()) {
                if (!eventConf.getEventId().equals(eventId)) continue;
                openMenuEventConf = eventConf.getStandardEventConfiguration().getOpenMenuEventConfiguration();
            }
        }
        if (openMenuEventConf == null) {
            throw new ServletException("Open menu event configuration not found for event: " + eventId);
        }
        try {
            chartBindingMap = new ChartBindingMap(chartId, cachedDataChart.getData(), cachedDataChart.getArgumentBar().getArgumentValueList(cachedDataChart.getRequestParams(), cachedDataChart.getRequestId()), this.getConnectionId());
        }
        catch (SapphireException e) {
            throw new ServletException("Cannot create chart binding map", (Throwable)e);
        }
        PropertyList menuProps = new PropertyList();
        menuProps.setProperty("menuid", plotId + urlGeneratorId + eventId + "_menu");
        PropertyListCollection menuItemCollection = new PropertyListCollection();
        for (MenuItemConfiguration menuItemConf : openMenuEventConf.getMenuItemConfigurationList()) {
            PropertyList menuItemProps = new PropertyList();
            menuItemProps.setProperty("itemid", menuItemConf.getItemId());
            menuItemProps.setProperty("text", menuItemConf.getText());
            menuItemProps.setProperty("tip", menuItemConf.getTip());
            Operation operation = new Operation(menuItemConf.getOperationConfiguration());
            try {
                chartBindingMap.setActiveItem(plotId, seriesGroupId, seriesId, Integer.parseInt(item));
                menuItemProps.setProperty("operationprops", operation.getProps(chartBindingMap));
            }
            catch (SapphireException e) {
                throw new ServletException("Cannot evaluate operation expression for event: " + eventId, (Throwable)e);
            }
            menuItemCollection.add(menuItemProps);
        }
        menuProps.setProperty("menuwidth", Integer.toString(openMenuEventConf.getMenuWidth()));
        menuProps.setProperty("menuitemcollection", menuItemCollection);
        ar.addCallbackArgument("menujson", menuProps.toJSONString(false));
        ar.print();
    }
}

