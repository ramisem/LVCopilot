/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  org.jfree.chart.ChartRenderingInfo
 *  org.jfree.chart.JFreeChart
 *  org.jfree.chart.plot.Plot
 *  org.jfree.chart.plot.PlotOrientation
 *  org.jfree.chart.plot.PlotRenderingInfo
 *  org.jfree.chart.plot.Zoomable
 */
package com.labvantage.sapphire.pageelements.datachart.ajax;

import com.labvantage.sapphire.pageelements.datachart.chart.Chart;
import com.labvantage.sapphire.pageelements.datachart.session.CachedDataChart;
import com.labvantage.sapphire.pageelements.datachart.util.Util;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.Zoomable;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public final class ZoomPlot
extends BaseAjaxRequest {
    private static final int ZOOM_TRIGGER_DISTANCE = 10;

    private static void zoom(JFreeChart jFreeChart, Rectangle2D zoomArea, ChartRenderingInfo chartRenderingInfo, int plotIndex) {
        Plot plot;
        PlotRenderingInfo plotRenderingInfo = chartRenderingInfo.getPlotInfo();
        if (plotRenderingInfo.getSubplotCount() > 0) {
            plotRenderingInfo = chartRenderingInfo.getPlotInfo().getSubplotInfo(plotIndex);
        }
        if ((plot = jFreeChart.getPlot()) instanceof Zoomable) {
            Zoomable zoomable = (Zoomable)plot;
            Rectangle2D screenDataArea = plotRenderingInfo.getDataArea();
            Point2D.Double selectOrigin = new Point2D.Double((int)Math.ceil(zoomArea.getX()), (int)Math.ceil(zoomArea.getY()));
            if (zoomArea.getHeight() > 0.0 && zoomArea.getWidth() > 0.0) {
                double hLower = (zoomArea.getMinX() - screenDataArea.getMinX()) / screenDataArea.getWidth();
                double hUpper = (zoomArea.getMaxX() - screenDataArea.getMinX()) / screenDataArea.getWidth();
                double vLower = (screenDataArea.getMaxY() - zoomArea.getMaxY()) / screenDataArea.getHeight();
                double vUpper = (screenDataArea.getMaxY() - zoomArea.getMinY()) / screenDataArea.getHeight();
                if (zoomable.getOrientation() == PlotOrientation.HORIZONTAL) {
                    zoomable.zoomDomainAxes(vLower, vUpper, plotRenderingInfo, (Point2D)selectOrigin);
                    zoomable.zoomRangeAxes(hLower, hUpper, plotRenderingInfo, (Point2D)selectOrigin);
                } else {
                    zoomable.zoomDomainAxes(hLower, hUpper, plotRenderingInfo, (Point2D)selectOrigin);
                    zoomable.zoomRangeAxes(vLower, vUpper, plotRenderingInfo, (Point2D)selectOrigin);
                }
            }
        }
    }

    private static void restoreAutoDomainBounds(JFreeChart chart, Point2D zoomPoint, ChartRenderingInfo info) {
        Plot plot = chart.getPlot();
        if (plot instanceof Zoomable) {
            Point2D zp;
            Zoomable z = (Zoomable)plot;
            Point2D point2D = zp = zoomPoint != null ? zoomPoint : new Point();
            if (info.getPlotInfo().getSubplotCount() > 0) {
                for (int i = 0; i < info.getPlotInfo().getSubplotCount(); ++i) {
                    z.zoomDomainAxes(0.0, info.getPlotInfo().getSubplotInfo(i), zp);
                }
            } else {
                z.zoomDomainAxes(0.0, info.getPlotInfo(), zp);
            }
        }
    }

    private static void restoreAutoRangeBounds(JFreeChart chart, Point2D zoomPoint, ChartRenderingInfo info) {
        Plot plot = chart.getPlot();
        if (plot instanceof Zoomable) {
            Point2D zp;
            Zoomable z = (Zoomable)plot;
            Point2D point2D = zp = zoomPoint != null ? zoomPoint : new Point();
            if (info.getPlotInfo().getSubplotCount() > 0) {
                for (int i = 0; i < info.getPlotInfo().getSubplotCount(); ++i) {
                    z.zoomRangeAxes(0.0, info.getPlotInfo().getSubplotInfo(i), zp);
                }
            } else {
                z.zoomRangeAxes(0.0, info.getPlotInfo(), zp);
            }
        }
    }

    private static void restoreAutoBounds(JFreeChart chart, Point2D zoomPoint, ChartRenderingInfo info) {
        Plot plot = chart.getPlot();
        if (plot == null) {
            return;
        }
        ZoomPlot.restoreAutoDomainBounds(chart, zoomPoint, info);
        ZoomPlot.restoreAutoRangeBounds(chart, zoomPoint, info);
    }

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext sc) throws ServletException {
        boolean reload;
        boolean zoomTrigger2;
        PropertyList mouseReleasedProps;
        PropertyList zoomRectangleProps;
        AjaxResponse ar = new AjaxResponse(request, response);
        String chartId = ar.getRequestParameter("chartid");
        String zoomRectangleJson = ar.getRequestParameter("zoomrectangle");
        String mouseReleasePointJson = ar.getRequestParameter("mousereleasedpoint");
        String plotIndexString = ar.getRequestParameter("plotindex");
        if (chartId == null || chartId.isEmpty()) {
            throw new IllegalArgumentException("Chart ID is null or empty: " + chartId);
        }
        if (zoomRectangleJson == null || zoomRectangleJson.isEmpty()) {
            throw new IllegalArgumentException("Zoom rectangle JSON is null or empty: " + zoomRectangleJson);
        }
        if (mouseReleasePointJson == null || mouseReleasePointJson.isEmpty()) {
            throw new IllegalArgumentException("Mouse released point JSON is null or empty: " + mouseReleasePointJson);
        }
        if (plotIndexString == null || plotIndexString.isEmpty()) {
            throw new IllegalArgumentException("Plot index is null or empty: " + plotIndexString);
        }
        try {
            zoomRectangleProps = new PropertyList(new JSONObject(zoomRectangleJson));
        }
        catch (JSONException ex) {
            throw new IllegalArgumentException("Zoom rectangle JSON is not valid: " + zoomRectangleJson, ex);
        }
        try {
            mouseReleasedProps = new PropertyList(new JSONObject(mouseReleasePointJson));
        }
        catch (JSONException ex) {
            throw new IllegalArgumentException("Mouse released point JSON is not valid: " + mouseReleasePointJson, ex);
        }
        int plotIndex = Integer.valueOf(plotIndexString);
        double zoomX = Double.valueOf(zoomRectangleProps.getProperty("x"));
        double zoomY = Double.valueOf(zoomRectangleProps.getProperty("y"));
        double zoomWidth = Double.valueOf(zoomRectangleProps.getProperty("width"));
        double zoomHeight = Double.valueOf(zoomRectangleProps.getProperty("height"));
        double mouseReleasedX = Double.valueOf(mouseReleasedProps.getProperty("x"));
        double mouseReleasedY = Double.valueOf(mouseReleasedProps.getProperty("y"));
        String userCacheChartId = Util.getUserChartCacheId(chartId, this.getConnectionId());
        CachedDataChart cachedDataChart = (CachedDataChart)CacheUtil.get(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getDatabaseId(), "DataCharts", userCacheChartId);
        if (cachedDataChart == null) {
            throw new IllegalArgumentException("Cache does not contain attribute " + chartId);
        }
        Chart chart = cachedDataChart.getChart();
        ChartRenderingInfo info = cachedDataChart.getChartRenderingInfo();
        PlotRenderingInfo plotRenderingInfo = info.getPlotInfo();
        if (plotRenderingInfo.getSubplotCount() > 0) {
            plotRenderingInfo = info.getPlotInfo().getSubplotInfo(plotIndex);
        }
        Rectangle2D screenDataArea = plotRenderingInfo.getDataArea();
        double maxX = Math.min(zoomX, screenDataArea.getMaxX());
        double maxY = Math.min(zoomY, screenDataArea.getMaxY());
        Point2D.Double mouseReleasePoint = new Point2D.Double(mouseReleasedX, mouseReleasedY);
        Point2D.Double zoomPoint = new Point2D.Double(zoomX, zoomY);
        Rectangle2D.Double zoomRectangle = new Rectangle2D.Double(maxX, maxY, zoomWidth, zoomHeight);
        boolean zoomTrigger1 = Math.abs(((Point2D)mouseReleasePoint).getX() - ((Point2D)zoomPoint).getX()) >= 10.0;
        boolean bl = zoomTrigger2 = Math.abs(((Point2D)mouseReleasePoint).getY() - ((Point2D)zoomPoint).getY()) >= 10.0;
        if (zoomTrigger1 || zoomTrigger2) {
            if (((Point2D)mouseReleasePoint).getX() < ((Point2D)zoomPoint).getX() || ((Point2D)mouseReleasePoint).getY() < ((Point2D)zoomPoint).getY()) {
                ZoomPlot.restoreAutoBounds(chart.getJFreeChart(), zoomPoint, info);
            } else {
                double x = ((Point2D)zoomPoint).getX();
                double y = ((Point2D)zoomPoint).getY();
                double w = Math.min(zoomRectangle.getWidth(), screenDataArea.getMaxX() - ((Point2D)zoomPoint).getX());
                double h = Math.min(zoomRectangle.getHeight(), screenDataArea.getMaxY() - ((Point2D)zoomPoint).getY());
                Rectangle2D.Double zoomArea = new Rectangle2D.Double(x, y, w, h);
                ZoomPlot.zoom(chart.getJFreeChart(), zoomArea, info, plotIndex);
            }
            reload = true;
        } else {
            reload = false;
        }
        if (reload) {
            cachedDataChart = new CachedDataChart.Builder(chartId, cachedDataChart.getDataChartProps(), chart, cachedDataChart.getArgumentBar(), cachedDataChart.getData(), cachedDataChart.getRequestParams(), cachedDataChart.getElements(), cachedDataChart.getRequestId()).build();
            CacheUtil.put(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getDatabaseId(), "DataCharts", userCacheChartId, cachedDataChart);
        }
        ar.addCallbackArgument("reload", reload);
        ar.print();
    }
}

