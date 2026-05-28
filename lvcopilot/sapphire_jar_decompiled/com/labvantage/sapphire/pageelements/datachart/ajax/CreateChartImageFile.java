/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.http.HttpSession
 *  org.jfree.chart.ChartRenderingInfo
 *  org.jfree.chart.JFreeChart
 *  org.jfree.chart.entity.EntityCollection
 *  org.jfree.chart.entity.StandardEntityCollection
 *  org.jfree.chart.imagemap.ImageMapUtilities
 *  org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator
 *  org.jfree.chart.imagemap.ToolTipTagFragmentGenerator
 *  org.jfree.chart.imagemap.URLTagFragmentGenerator
 *  org.jfree.chart.plot.Plot
 *  org.jfree.chart.plot.PlotOrientation
 *  org.jfree.chart.plot.PlotRenderingInfo
 *  org.jfree.chart.plot.Zoomable
 *  org.jfree.chart.servlet.ServletUtilities
 */
package com.labvantage.sapphire.pageelements.datachart.ajax;

import com.labvantage.sapphire.pageelements.datachart.argbar.ArgumentBar;
import com.labvantage.sapphire.pageelements.datachart.chart.Chart;
import com.labvantage.sapphire.pageelements.datachart.chart.ChartEntityHandler;
import com.labvantage.sapphire.pageelements.datachart.chart.imagemap.CustomURLTagFragmentGenerator;
import com.labvantage.sapphire.pageelements.datachart.data.Data;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.ChartConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.AbstractComponentPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.session.CachedDataChart;
import com.labvantage.sapphire.pageelements.datachart.util.Util;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.awt.geom.Rectangle2D;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.imagemap.ImageMapUtilities;
import org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator;
import org.jfree.chart.imagemap.ToolTipTagFragmentGenerator;
import org.jfree.chart.imagemap.URLTagFragmentGenerator;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.Zoomable;
import org.jfree.chart.servlet.ServletUtilities;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public final class CreateChartImageFile
extends BaseAjaxRequest {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Converted monitor instructions to comments
     * Lifted jumps to return sites
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext sc) throws ServletException {
        AjaxResponse ar = new AjaxResponse(request, response);
        String chartId = ar.getRequestParameter("chartid");
        String clientWidth = ar.getRequestParameter("clientwidth");
        String clientHeight = ar.getRequestParameter("clientheight");
        if (chartId == null) throw new IllegalArgumentException("Chart ID is null or empty: " + chartId);
        if (chartId.isEmpty()) {
            throw new IllegalArgumentException("Chart ID is null or empty: " + chartId);
        }
        if (clientWidth == null) throw new IllegalArgumentException("Client width is null or empty: " + clientWidth);
        if (clientWidth.isEmpty()) {
            throw new IllegalArgumentException("Client width is null or empty: " + clientWidth);
        }
        if (clientHeight == null) throw new IllegalArgumentException("Client height is null or empty: " + clientHeight);
        if (clientHeight.isEmpty()) {
            throw new IllegalArgumentException("Client height is null or empty: " + clientHeight);
        }
        String userCacheChartId = Util.getUserChartCacheId(chartId, this.getConnectionId());
        HttpSession session = request.getSession(true);
        CachedDataChart cachedDataChart = (CachedDataChart)CacheUtil.get(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getDatabaseId(), "DataCharts", userCacheChartId);
        if (cachedDataChart == null) {
            throw new IllegalArgumentException("Cache does not contain attribute " + userCacheChartId);
        }
        PropertyList dataTrendingProps = cachedDataChart.getDataChartProps();
        ArgumentBar argumentBar = cachedDataChart.getArgumentBar();
        Data data = cachedDataChart.getData();
        StandardEntityCollection entityCollection = new StandardEntityCollection();
        ChartRenderingInfo chartRenderingInfo = new ChartRenderingInfo((EntityCollection)entityCollection);
        try {
            Chart chart = cachedDataChart.getChart();
            ChartConfiguration chartConf = chart.getChartConfiguration();
            chartConf.setClientDimensions(Double.valueOf(clientWidth).intValue(), Double.valueOf(clientHeight).intValue());
            JFreeChart jFreeChart = chart.getJFreeChart();
            int width = chartConf.getWidth();
            int height = chartConf.getHeight();
            if (width > 0 && height > 0) {
                Class<CreateChartImageFile> clazz = CreateChartImageFile.class;
                // MONITORENTER : com.labvantage.sapphire.pageelements.datachart.ajax.CreateChartImageFile.class
                ServletUtilities.setTempFilePrefix((String)chartId);
                String fileName = ServletUtilities.saveChartAsPNG((JFreeChart)jFreeChart, (int)width, (int)height, (ChartRenderingInfo)chartRenderingInfo, (HttpSession)session);
                // MONITOREXIT : clazz
                String chartImageMapId = chartId + "_map";
                ChartEntityHandler.setURL(chartId + "_JS", (EntityCollection)entityCollection, jFreeChart, chartConf);
                String imageMap = ImageMapUtilities.getImageMap((String)chartImageMapId, (ChartRenderingInfo)chartRenderingInfo, (ToolTipTagFragmentGenerator)new StandardToolTipTagFragmentGenerator(), (URLTagFragmentGenerator)new CustomURLTagFragmentGenerator());
                cachedDataChart = new CachedDataChart.Builder(chartId, dataTrendingProps, chart, argumentBar, data, cachedDataChart.getRequestParams(), cachedDataChart.getElements(), cachedDataChart.getRequestId()).fileName(fileName).imageMap(imageMap).chartRenderingInfo(chartRenderingInfo).build();
                CacheUtil.put(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getDatabaseId(), "DataCharts", userCacheChartId, cachedDataChart);
                Plot plot = chart.getJFreeChart().getPlot();
                boolean isRangeZoomable = false;
                boolean isDomainZoomable = false;
                PlotOrientation plotOrientation = AbstractComponentPlotConfiguration.PlotOrientationType.fromString(AbstractComponentPlotConfiguration.DEFAULT_PLOT_ORIENTATION).getPlotOrientation();
                if (plot instanceof Zoomable) {
                    Zoomable z = (Zoomable)plot;
                    isRangeZoomable = z.isRangeZoomable();
                    isDomainZoomable = z.isDomainZoomable();
                    plotOrientation = z.getOrientation();
                }
                ar.addCallbackArgument("imgsrc", "rc?command=operation&operationclass=com.labvantage.sapphire.pageelements.datachart.servlet.ChartImageStreamer&filename=" + fileName);
                ar.addCallbackArgument("imagemap", imageMap);
                ar.addCallbackArgument("imagemapid", chartImageMapId);
                ar.addCallbackArgument("plotdataarea", this.getDataAreaProps(chartRenderingInfo).toJSONString());
                ar.addCallbackArgument("israngezoomable", isRangeZoomable);
                ar.addCallbackArgument("isdomainzoomable", isDomainZoomable);
                ar.addCallbackArgument("plotorientation", plotOrientation);
            }
        }
        catch (Throwable t) {
            String errorMessage = "Cannot create chart image file: " + t.getClass().getName() + ": " + t.getStackTrace()[0] + "<br>" + t.getStackTrace()[1] + "<br>... " + (t.getStackTrace().length - 2) + " more";
            Throwable cause = t.getCause();
            if (cause != null) {
                errorMessage = errorMessage + "<br><br>Caused by:" + cause.getClass().getName() + ": " + cause.getStackTrace()[0] + "<br>" + cause.getStackTrace()[1] + "<br>... " + (cause.getStackTrace().length - 2) + " more";
            }
            ar.setError(errorMessage, t);
        }
        ar.print();
    }

    private PropertyListCollection getDataAreaProps(ChartRenderingInfo chartRenderingInfo) {
        PropertyListCollection dataAreaCollection = new PropertyListCollection();
        PlotRenderingInfo plotInfo = chartRenderingInfo.getPlotInfo();
        if (plotInfo.getSubplotCount() > 0) {
            for (int i = 0; i < plotInfo.getSubplotCount(); ++i) {
                Rectangle2D dataArea = plotInfo.getSubplotInfo(i).getDataArea();
                dataAreaCollection.add(this.createDataAreaProps(dataArea));
            }
        } else {
            Rectangle2D dataArea = plotInfo.getDataArea();
            dataAreaCollection.add(this.createDataAreaProps(dataArea));
        }
        return dataAreaCollection;
    }

    private PropertyList createDataAreaProps(Rectangle2D dataArea) {
        PropertyList dataAreaProps = new PropertyList();
        dataAreaProps.setProperty("x", String.valueOf(dataArea.getX()));
        dataAreaProps.setProperty("y", String.valueOf(dataArea.getY()));
        dataAreaProps.setProperty("width", String.valueOf(dataArea.getWidth()));
        dataAreaProps.setProperty("height", String.valueOf(dataArea.getHeight()));
        return dataAreaProps;
    }
}

