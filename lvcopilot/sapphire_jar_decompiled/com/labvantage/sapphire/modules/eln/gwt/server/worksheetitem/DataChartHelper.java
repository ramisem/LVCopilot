/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.codec.binary.Base64
 *  org.jfree.chart.ChartRenderingInfo
 *  org.jfree.chart.JFreeChart
 *  org.jfree.chart.encoders.ImageEncoder
 *  org.jfree.chart.encoders.ImageEncoderFactory
 *  org.jfree.chart.entity.EntityCollection
 *  org.jfree.chart.entity.StandardEntityCollection
 *  org.jfree.chart.imagemap.ImageMapUtilities
 *  org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator
 *  org.jfree.chart.imagemap.ToolTipTagFragmentGenerator
 *  org.jfree.chart.imagemap.URLTagFragmentGenerator
 *  org.jfree.chart.servlet.ServletUtilities
 */
package com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.pageelements.datachart.ajax.CreateChartImageFile;
import com.labvantage.sapphire.pageelements.datachart.argbar.ArgumentBar;
import com.labvantage.sapphire.pageelements.datachart.chart.Chart;
import com.labvantage.sapphire.pageelements.datachart.chart.ChartEntityHandler;
import com.labvantage.sapphire.pageelements.datachart.chart.imagemap.CustomURLTagFragmentGenerator;
import com.labvantage.sapphire.pageelements.datachart.data.Data;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.ChartConfiguration;
import com.labvantage.sapphire.pageelements.datachart.session.CachedDataChart;
import com.labvantage.sapphire.pageelements.datachart.util.Util;
import com.labvantage.sapphire.servlet.RequestProcessor;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import org.apache.commons.codec.binary.Base64;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.encoders.ImageEncoder;
import org.jfree.chart.encoders.ImageEncoderFactory;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.imagemap.ImageMapUtilities;
import org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator;
import org.jfree.chart.imagemap.ToolTipTagFragmentGenerator;
import org.jfree.chart.imagemap.URLTagFragmentGenerator;
import org.jfree.chart.servlet.ServletUtilities;
import sapphire.SapphireException;
import sapphire.servlet.RequestContext;
import sapphire.xml.PropertyList;

public class DataChartHelper
extends BaseCustom {
    public static final String DEFAULT_WIDTH = "400";
    public static final String DEFAULT_HEIGHT = "400";

    public DataChartHelper(String connectionId) {
        if (connectionId == null) {
            throw new IllegalArgumentException("Connection Id is null");
        }
        if (connectionId.isEmpty()) {
            throw new IllegalArgumentException("Connection Id is empty");
        }
        this.setConnectionId(connectionId);
    }

    public PropertyList getWebPageConfiguration(String webPageId, RequestContext requestContext) throws SapphireException {
        RequestProcessor requestProcessor = new RequestProcessor(this.getConnectionProcessor().getConnectionid());
        return requestProcessor.getWebPageProperties(webPageId, requestContext);
    }

    public String formatErrorMessage(String message) throws SapphireException {
        return "<div style='padding: 5px; color: red;'>" + this.getTranslationProcessor().translate(message) + "<div>";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public String getChartImageTag(String chartId, String clientWidth, String clientHeight, boolean isExport) throws SapphireException {
        if (chartId == null || chartId.isEmpty()) {
            throw new IllegalArgumentException("Chart ID is null or empty: " + chartId);
        }
        if (clientWidth == null || clientWidth.isEmpty()) {
            throw new IllegalArgumentException("Client width is null or empty: " + clientWidth);
        }
        if (clientHeight == null || clientHeight.isEmpty()) {
            throw new IllegalArgumentException("Client height is null or empty: " + clientHeight);
        }
        StringBuilder html = new StringBuilder();
        String connectionId = this.getConnectionId();
        String userCacheChartId = Util.getUserChartCacheId(chartId, this.getConnectionId());
        CachedDataChart cachedDataChart = (CachedDataChart)CacheUtil.get(this.getConnectionProcessor().getConnectionInfo(connectionId).getDatabaseId(), "DataCharts", userCacheChartId);
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
            if (width <= 0 || height <= 0) return html.toString();
            Class<CreateChartImageFile> clazz = CreateChartImageFile.class;
            synchronized (CreateChartImageFile.class) {
                ServletUtilities.setTempFilePrefix((String)chartId);
                String fileName = ServletUtilities.saveChartAsPNG((JFreeChart)jFreeChart, (int)width, (int)height, (ChartRenderingInfo)chartRenderingInfo, null);
                File f = new File(System.getProperty("java.io.tmpdir"), fileName);
                f.deleteOnExit();
                // ** MonitorExit[var20_22] (shouldn't be in output)
                String chartImageMapId = chartId + "_map";
                ChartEntityHandler.setURL(chartId + "_JS", (EntityCollection)entityCollection, jFreeChart, chartConf);
                String imageMap = ImageMapUtilities.getImageMap((String)chartImageMapId, (ChartRenderingInfo)chartRenderingInfo, (ToolTipTagFragmentGenerator)new StandardToolTipTagFragmentGenerator(), (URLTagFragmentGenerator)new CustomURLTagFragmentGenerator());
                cachedDataChart = new CachedDataChart.Builder(chartId, dataTrendingProps, chart, argumentBar, data, cachedDataChart.getRequestParams(), cachedDataChart.getElements(), cachedDataChart.getRequestId()).imageMap(imageMap).fileName(fileName).chartRenderingInfo(chartRenderingInfo).build();
                CacheUtil.put(this.getConnectionProcessor().getConnectionInfo(connectionId).getDatabaseId(), "DataCharts", userCacheChartId, cachedDataChart);
                if (isExport) {
                    html.append("<img src='").append(this.getEncodedImageUrl(cachedDataChart, width, height)).append("' border = 0>");
                    return html.toString();
                } else {
                    html.append(imageMap);
                    html.append("<img src='rc?command=operation&operationclass=com.labvantage.sapphire.pageelements.datachart.servlet.ChartImageStreamer&filename=").append(fileName).append("' class='chartImage' usemap ='#" + chartImageMapId + "'></img>");
                }
                return html.toString();
            }
        }
        catch (Throwable t) {
            this.logger.error("Cannot create chart image file", t);
            String errorMessage = "Cannot create chart image file: " + t.getClass().getName() + ": " + t.getStackTrace()[0] + "<br>" + t.getStackTrace()[1] + "<br>... " + (t.getStackTrace().length - 2) + " more";
            Throwable cause = t.getCause();
            if (cause != null) {
                errorMessage = errorMessage + "<br><br>Caused by:" + cause.getClass().getName() + ": " + cause.getStackTrace()[0] + "<br>" + cause.getStackTrace()[1] + "<br>... " + (cause.getStackTrace().length - 2) + " more";
            }
            html = new StringBuilder();
            html.append(errorMessage);
        }
        return html.toString();
    }

    private String getEncodedImageUrl(CachedDataChart cachedDataChart, int width, int height) throws SapphireException {
        byte[] data;
        JFreeChart chart = cachedDataChart.getChart().getJFreeChart();
        ChartRenderingInfo info = new ChartRenderingInfo();
        BufferedImage image = chart.createBufferedImage(width, height, info);
        ImageEncoder imageEncoder = ImageEncoderFactory.newInstance((String)"png");
        try {
            data = imageEncoder.encode(image);
        }
        catch (IOException e) {
            throw new SapphireException("Cannot create chart encoded chart image", e);
        }
        String base64data = Base64.encodeBase64String((byte[])data);
        return "data:image/png;base64," + base64data;
    }
}

