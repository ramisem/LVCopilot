/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletOutputStream
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  org.apache.commons.codec.binary.Base64
 *  org.jfree.chart.ChartColor
 *  org.jfree.chart.ChartFactory
 *  org.jfree.chart.ChartRenderingInfo
 *  org.jfree.chart.JFreeChart
 *  org.jfree.chart.axis.ValueAxis
 *  org.jfree.chart.encoders.ImageEncoder
 *  org.jfree.chart.encoders.ImageEncoderFactory
 *  org.jfree.chart.plot.PiePlot
 *  org.jfree.chart.plot.PlotOrientation
 *  org.jfree.chart.plot.XYPlot
 *  org.jfree.chart.renderer.xy.StandardXYBarPainter
 *  org.jfree.chart.renderer.xy.XYBarPainter
 *  org.jfree.chart.renderer.xy.XYBarRenderer
 *  org.jfree.chart.renderer.xy.XYItemRenderer
 *  org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
 *  org.jfree.data.general.DefaultPieDataset
 *  org.jfree.data.general.PieDataset
 *  org.jfree.data.xy.IntervalXYDataset
 *  org.jfree.data.xy.XYBarDataset
 *  org.jfree.data.xy.XYDataset
 *  org.jfree.data.xy.XYSeries
 *  org.jfree.data.xy.XYSeriesCollection
 *  org.jfree.ui.RectangleInsets
 */
package com.labvantage.sapphire.pageelements.spreadsheeteditor;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.servlet.command.BaseRequest;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.encoders.ImageEncoder;
import org.jfree.chart.encoders.ImageEncoderFactory;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYBarDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.util.Logger;

public class SparklineStreamer
extends BaseRequest {
    private static final String LABVANTAGE_CVS_ID = "$Revision: 55631 $";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext context) {
        block10: {
            try {
                String name = request.getParameter("name");
                String jsonString = request.getParameter("json");
                String width = request.getParameter("width");
                String height = request.getParameter("height");
                JSONObject json = new JSONObject(jsonString);
                response.setHeader("Cache-Control", "no-store");
                response.setHeader("Pragma", "no-cache");
                response.setDateHeader("Expires", 0L);
                JFreeChart chart = null;
                if (name.equalsIgnoreCase("PIESPARKLINE")) {
                    chart = SparklineStreamer.getPieSparkline(json);
                } else if (name.equalsIgnoreCase("LINESPARKLINE")) {
                    chart = SparklineStreamer.getLineSparkline(json);
                } else if (name.equalsIgnoreCase("COLUMNSPARKLINE")) {
                    chart = SparklineStreamer.getColumnSparkline(json);
                }
                if (chart == null) break block10;
                try (ServletOutputStream ouputStream = response.getOutputStream();){
                    response.setContentType("image/png");
                    int size = this.streamChart((OutputStream)ouputStream, chart, Integer.parseInt(height), Integer.parseInt(width), true);
                    response.setContentLength(size);
                }
            }
            catch (Exception e) {
                Logger.logStackTrace(e);
            }
        }
    }

    public static String getBase64Sparkline(String name, JSONObject json, int width, int height) {
        JFreeChart chart = null;
        try {
            if (name.equalsIgnoreCase("PIESPARKLINE")) {
                chart = SparklineStreamer.getPieSparkline(json);
            } else if (name.equalsIgnoreCase("LINESPARKLINE")) {
                chart = SparklineStreamer.getLineSparkline(json);
            } else if (name.equalsIgnoreCase("COLUMNSPARKLINE")) {
                chart = SparklineStreamer.getColumnSparkline(json);
            }
            ChartRenderingInfo info = new ChartRenderingInfo();
            BufferedImage image = chart.createBufferedImage(width, height, info);
            ImageEncoder imageEncoder = ImageEncoderFactory.newInstance((String)"png");
            byte[] data = imageEncoder.encode(image);
            String base64data = Base64.encodeBase64String((byte[])data);
            return "data:image/png;base64," + base64data;
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static JFreeChart getPieSparkline(JSONObject json) throws JSONException {
        int i;
        JSONArray values = json.optJSONArray("values");
        JSONArray jcolors = json.optJSONArray("colors");
        DefaultPieDataset dpd = new DefaultPieDataset();
        Color[] baseColors = new Color[jcolors.length() == 0 ? 1 : jcolors.length()];
        if (jcolors.length() == 0) {
            baseColors[0] = SparklineStreamer.getColor("#222222");
        } else {
            for (i = 0; i < jcolors.length(); ++i) {
                baseColors[i] = SparklineStreamer.getColor((String)jcolors.get(i));
            }
        }
        for (i = 0; i < values.length(); ++i) {
            double value = 0.0;
            try {
                value = values.getDouble(i);
            }
            catch (Exception e) {
                Logger.logWarn("Failed to get measure value as double.");
            }
            dpd.setValue((Comparable)((Object)("" + i)), Math.abs(value));
        }
        JFreeChart chart = ChartFactory.createPieChart((String)"", (PieDataset)dpd, (boolean)false, (boolean)false, (boolean)false);
        PiePlot plot = (PiePlot)chart.getPlot();
        double basefactor = 1.0 / (double)values.length();
        int colorCount = 0;
        int factorCount = 0;
        for (int i2 = 0; i2 < values.length(); ++i2) {
            if (colorCount >= baseColors.length) {
                double factor = 1.0 - (double)(++factorCount) * basefactor;
                for (int k = 0; k < baseColors.length; ++k) {
                    baseColors[k] = SparklineStreamer.getDarkerColor(baseColors[k], factor);
                }
                colorCount = 0;
            }
            plot.setSectionPaint((Comparable)((Object)("" + i2)), (Paint)baseColors[colorCount]);
            ++colorCount;
        }
        plot.setCircular(true);
        plot.setIgnoreNullValues(true);
        plot.setIgnoreZeroValues(true);
        plot.setLabelGenerator(null);
        plot.setOutlinePaint(null);
        plot.setBackgroundPaint(null);
        chart.setBackgroundPaint(null);
        plot.setShadowPaint(null);
        plot.setInteriorGap(0.0);
        return chart;
    }

    public static JFreeChart getLineSparkline(JSONObject json) throws JSONException {
        XYSeriesCollection ds = new XYSeriesCollection();
        XYSeries series = new XYSeries((Comparable)((Object)"Points 1"));
        JSONObject setting = json.optJSONObject("setting");
        String displayEmptyCellsAs = setting.optString("displayEmptyCellsAs", "0");
        String lineWeight = setting.optString("lineWeight", "1");
        String seriesColor = setting.optString("seriesColor", "#FF66CC");
        String axisColor = setting.optString("axisColor", "#FFFF66");
        final String highMarkerColor = setting.optString("highMarkerColor", "#FF0000");
        final String lowMarkerColor = setting.optString("lowMarkerColor", "#009900");
        final String firstMarkerColor = setting.optString("firstMarkerColor", "#663333");
        final String lastMarkerColor = setting.optString("lastMarkerColor", "#0000FF");
        final String markersColor = setting.optString("markersColor", "#FF9999");
        boolean showAxis = "true".equals(setting.optString("displayXAxis"));
        final boolean showMarkers = "true".equals(setting.optString("showMarkers"));
        final boolean showFirst = "true".equals(setting.optString("showFirst"));
        final boolean showLast = "true".equals(setting.optString("showLast"));
        final boolean showHigh = "true".equals(setting.optString("showHigh"));
        final boolean showLow = "true".equals(setting.optString("showLow"));
        boolean anyNegative = false;
        JSONArray dataValues = json.optJSONArray("dataValues");
        JSONArray axisValues = json.optJSONArray("axisValues");
        for (int i = 0; i < dataValues.length(); ++i) {
            double dataValue = dataValues.optDouble(i);
            if (Double.isNaN(dataValue) && "1".equals(displayEmptyCellsAs)) {
                dataValue = 0.0;
            }
            if (Double.isNaN(dataValue) && "2".equals(displayEmptyCellsAs)) continue;
            double categoryValue = axisValues != null && axisValues.length() > i ? axisValues.optDouble(i) : (double)i;
            series.add(categoryValue, dataValue);
            anyNegative |= dataValue < 0.0;
        }
        int tempMinIndex = -1;
        int tempMaxIndex = -1;
        for (int i = 0; i < series.getItemCount(); ++i) {
            if (series.getMaxY() == series.getY(i).doubleValue()) {
                tempMaxIndex = i;
            }
            if (series.getMinY() != series.getY(i).doubleValue()) continue;
            tempMinIndex = i;
        }
        final int maxIndex = tempMaxIndex;
        final int minIndex = tempMinIndex;
        final int lastIndex = dataValues.length() - 1;
        showAxis &= anyNegative;
        ds.addSeries(series);
        JFreeChart chart = ChartFactory.createScatterPlot((String)"", (String)"", (String)"", (XYDataset)ds, (PlotOrientation)PlotOrientation.VERTICAL, (boolean)false, (boolean)true, (boolean)false);
        XYPlot plot = (XYPlot)chart.getPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(){

            public boolean getItemShapeVisible(int series, int item) {
                boolean visible = false;
                String color = "";
                if (showMarkers && markersColor.length() > 0) {
                    visible = true;
                    color = markersColor;
                }
                if (item == maxIndex && showHigh && highMarkerColor.length() > 0) {
                    visible = true;
                    color = highMarkerColor;
                }
                if (item == minIndex && showLow && lowMarkerColor.length() > 0) {
                    visible = true;
                    color = lowMarkerColor;
                }
                if (item == 0 && showFirst && firstMarkerColor.length() > 0) {
                    visible = true;
                    color = firstMarkerColor;
                }
                if (item == lastIndex && showLast && lastMarkerColor.length() > 0) {
                    visible = true;
                    color = lastMarkerColor;
                }
                visible |= showFirst && item == 0;
                if ((visible |= showMarkers) && color.length() > 0) {
                    this.setSeriesPaint(series, SparklineStreamer.getColor(color));
                }
                return visible;
            }
        };
        plot.setRenderer((XYItemRenderer)renderer);
        renderer.setBaseLinesVisible(true);
        renderer.setSeriesPaint(0, (Paint)SparklineStreamer.getColor(seriesColor));
        renderer.setSeriesStroke(0, (Stroke)new BasicStroke(lineWeight != null && lineWeight.length() > 0 ? Float.parseFloat(lineWeight) / 1.5f : 1.0f));
        renderer.setSeriesShape(0, (Shape)new Ellipse2D.Double(-2.0, -2.0, 2.0, 2.0));
        renderer.setBaseShapesFilled(true);
        plot.setDomainCrosshairVisible(false);
        plot.setRangeCrosshairVisible(false);
        plot.setOutlinePaint(null);
        plot.setBackgroundPaint(null);
        chart.setBackgroundPaint(null);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(false);
        plot.setRangeZeroBaselineVisible(showAxis);
        plot.setRangeZeroBaselinePaint((Paint)SparklineStreamer.getColor(axisColor));
        ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setVisible(false);
        ValueAxis domainAxis = plot.getDomainAxis();
        domainAxis.setVisible(false);
        chart.setPadding(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
        plot.setInsets(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
        return chart;
    }

    public static JFreeChart getColumnSparkline(JSONObject json) throws JSONException {
        JSONObject setting = json.optJSONObject("setting");
        String lineWeight = setting.optString("lineWeight", "1");
        String seriesColor = setting.optString("seriesColor", "#FF66CC");
        String axisColor = setting.optString("axisColor", "#FFFF66");
        final String highMarkerColor = setting.optString("highMarkerColor", "#FF0000");
        final String lowMarkerColor = setting.optString("lowMarkerColor", "#009900");
        final String firstMarkerColor = setting.optString("firstMarkerColor", "#663333");
        final String lastMarkerColor = setting.optString("lastMarkerColor", "#0000FF");
        final String negativeColor = setting.optString("negativeColor", "#FF0000");
        boolean showAxis = "true".equals(setting.optString("displayXAxis"));
        final boolean showNegative = "true".equals(setting.optString("showNegative"));
        final boolean showFirst = "true".equals(setting.optString("showFirst"));
        final boolean showLast = "true".equals(setting.optString("showLast"));
        final boolean showHigh = "true".equals(setting.optString("showHigh"));
        final boolean showLow = "true".equals(setting.optString("showLow"));
        XYSeries series = new XYSeries((Comparable)((Object)"Points 1"));
        boolean anyNegative = false;
        JSONArray dataValues = json.optJSONArray("dataValues");
        JSONArray axisValues = json.optJSONArray("axisValues");
        for (int i = 0; i < dataValues.length(); ++i) {
            double dataValue = dataValues.optDouble(i);
            double categoryValue = axisValues != null && axisValues.length() > i ? axisValues.optDouble(i) : (double)(i + 1);
            series.add(categoryValue, dataValue);
            anyNegative |= dataValue < 0.0;
        }
        int tempMinIndex = -1;
        int tempMaxIndex = -1;
        ArrayList<Integer> tempNegative = new ArrayList<Integer>();
        for (int i = 0; i < series.getItemCount(); ++i) {
            if (series.getMaxY() == series.getY(i).doubleValue()) {
                tempMaxIndex = i;
            }
            if (series.getMinY() == series.getY(i).doubleValue()) {
                tempMinIndex = i;
            }
            if (!(series.getY(i).doubleValue() < 0.0)) continue;
            tempNegative.add(i);
        }
        final int maxIndex = tempMaxIndex;
        final int minIndex = tempMinIndex;
        final int lastIndex = dataValues.length() - 1;
        final ArrayList negativeList = new ArrayList(tempNegative);
        XYSeriesCollection ds = new XYSeriesCollection();
        ds.addSeries(series);
        XYBarDataset ds2 = new XYBarDataset((XYDataset)ds, 1.0);
        showAxis &= anyNegative;
        JFreeChart chart = ChartFactory.createXYBarChart((String)"", (String)"", (boolean)true, (String)"", (IntervalXYDataset)ds2, (PlotOrientation)PlotOrientation.VERTICAL, (boolean)false, (boolean)false, (boolean)false);
        XYPlot plot = (XYPlot)chart.getPlot();
        XYBarRenderer renderer = new XYBarRenderer(){

            public Paint getItemPaint(int row, int column) {
                String color = "";
                if (column == maxIndex && showHigh && highMarkerColor.length() > 0) {
                    color = highMarkerColor;
                }
                if (column == minIndex && showLow && lowMarkerColor.length() > 0) {
                    color = lowMarkerColor;
                }
                if (column == 0 && showFirst && firstMarkerColor.length() > 0) {
                    color = firstMarkerColor;
                }
                if (column == lastIndex && showLast && lastMarkerColor.length() > 0) {
                    color = lastMarkerColor;
                }
                if (negativeList.contains(column) && showNegative && negativeColor.length() > 0) {
                    color = negativeColor;
                }
                if (color.length() > 0) {
                    return SparklineStreamer.getColor(color);
                }
                return super.getItemPaint(row, column);
            }
        };
        renderer.setShadowVisible(false);
        renderer.setBarPainter((XYBarPainter)new StandardXYBarPainter());
        renderer.setMargin(0.6);
        plot.setRenderer((XYItemRenderer)renderer);
        renderer.setSeriesPaint(0, (Paint)SparklineStreamer.getColor(seriesColor));
        renderer.setSeriesShape(0, (Shape)new Ellipse2D.Double(-2.0, -2.0, 2.0, 2.0));
        plot.setDomainCrosshairVisible(false);
        plot.setRangeCrosshairVisible(false);
        plot.setOutlinePaint(null);
        plot.setBackgroundPaint(null);
        chart.setBackgroundPaint(null);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(false);
        plot.setRangeZeroBaselineVisible(showAxis);
        plot.setRangeZeroBaselinePaint((Paint)SparklineStreamer.getColor(axisColor));
        ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setVisible(false);
        ValueAxis domainAxis = plot.getDomainAxis();
        domainAxis.setVisible(false);
        chart.setPadding(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
        plot.setInsets(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
        return chart;
    }

    private static Color getDarkerColor(Color start, double factor) {
        return new Color(Math.max((int)((double)start.getRed() * factor), 0), Math.max((int)((double)start.getGreen() * factor), 0), Math.max((int)((double)start.getBlue() * factor), 0), start.getAlpha());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int streamChart(OutputStream out, JFreeChart chart, int height, int width, boolean png) throws SapphireException {
        int output = 0;
        try {
            try {
                ImageEncoder imageEncoder;
                BufferedImage image;
                ChartRenderingInfo info = new ChartRenderingInfo();
                Trace.log("DCS", "DashboardChart: streaming chart as PNG ");
                if (png) {
                    image = chart.createBufferedImage(width, height, info);
                    Logger.logDebug("png buffered image created  ");
                    imageEncoder = ImageEncoderFactory.newInstance((String)"png");
                    Logger.logDebug("png image encoder created  ");
                } else {
                    image = chart.createBufferedImage(width, height, 1, info);
                    Logger.logDebug("jpg buffered image created  ");
                    imageEncoder = ImageEncoderFactory.newInstance((String)"jpeg");
                    Logger.logDebug("png image encoder created  ");
                }
                byte[] data = imageEncoder.encode(image);
                Logger.logDebug("image encoded to byte[]  ");
                output = data.length;
                Logger.logDebug("image size = " + output);
                if (output > 0) {
                    out.write(data);
                    Logger.logDebug("data written to output stream");
                } else {
                    Logger.logWarn("no data stream");
                }
            }
            finally {
                out.flush();
            }
        }
        catch (IOException ioe) {
            Trace.logError("DCS", (Object)("DashboardChart: streamPNG " + ioe.getMessage()), ioe);
            throw new SapphireException(ioe.getMessage(), ioe);
        }
        return output;
    }

    public static Color getColor(String colorName) {
        if (colorName == null) {
            throw new IllegalArgumentException("Color name is null");
        }
        if (colorName.equalsIgnoreCase("DARK BLUE")) {
            return ChartColor.DARK_BLUE;
        }
        if (colorName.equalsIgnoreCase("DARK CYAN")) {
            return ChartColor.DARK_CYAN;
        }
        if (colorName.equalsIgnoreCase("DARK GREEN")) {
            return ChartColor.DARK_GREEN;
        }
        if (colorName.equalsIgnoreCase("DARK MAGENTA")) {
            return ChartColor.DARK_MAGENTA;
        }
        if (colorName.equalsIgnoreCase("DARK RED")) {
            return ChartColor.DARK_RED;
        }
        if (colorName.equalsIgnoreCase("DARK YELLOW")) {
            return ChartColor.DARK_YELLOW;
        }
        if (colorName.equalsIgnoreCase("LIGHT BLUE")) {
            return ChartColor.LIGHT_BLUE;
        }
        if (colorName.equalsIgnoreCase("LIGHT CYAN")) {
            return ChartColor.LIGHT_CYAN;
        }
        if (colorName.equalsIgnoreCase("LIGHT GREEN")) {
            return ChartColor.LIGHT_GREEN;
        }
        if (colorName.equalsIgnoreCase("LIGHT MAGENTA")) {
            return ChartColor.LIGHT_MAGENTA;
        }
        if (colorName.equalsIgnoreCase("LIGHT RED")) {
            return ChartColor.LIGHT_RED;
        }
        if (colorName.equalsIgnoreCase("LIGHT YELLOW")) {
            return ChartColor.LIGHT_YELLOW;
        }
        if (colorName.equalsIgnoreCase("VERY DARK  BLUE")) {
            return ChartColor.VERY_DARK_BLUE;
        }
        if (colorName.equalsIgnoreCase("VERY DARK CYAN")) {
            return ChartColor.VERY_DARK_CYAN;
        }
        if (colorName.equalsIgnoreCase("VERY DARK GREEN")) {
            return ChartColor.VERY_DARK_GREEN;
        }
        if (colorName.equalsIgnoreCase("VERY DARK MAGENTA")) {
            return ChartColor.VERY_DARK_MAGENTA;
        }
        if (colorName.equalsIgnoreCase("VERY DARK RED")) {
            return ChartColor.VERY_DARK_RED;
        }
        if (colorName.equalsIgnoreCase("VERY DARK YELLOW")) {
            return ChartColor.VERY_DARK_YELLOW;
        }
        if (colorName.equalsIgnoreCase("VERY LIGHT BLUE")) {
            return ChartColor.VERY_LIGHT_BLUE;
        }
        if (colorName.equalsIgnoreCase("VERY LIGHT CYAN")) {
            return ChartColor.VERY_LIGHT_CYAN;
        }
        if (colorName.equalsIgnoreCase("VERY LIGHT GREEN")) {
            return ChartColor.VERY_LIGHT_GREEN;
        }
        if (colorName.equalsIgnoreCase("VERY LIGHT MAGENTA")) {
            return ChartColor.VERY_LIGHT_MAGENTA;
        }
        if (colorName.equalsIgnoreCase("VERY LIGHT RED")) {
            return ChartColor.VERY_LIGHT_RED;
        }
        if (colorName.equalsIgnoreCase("VERY LIGHT YELLOW")) {
            return ChartColor.VERY_LIGHT_YELLOW;
        }
        if (colorName.equalsIgnoreCase("BLUE")) {
            return ChartColor.BLUE;
        }
        if (colorName.equalsIgnoreCase("CYAN")) {
            return ChartColor.CYAN;
        }
        if (colorName.equalsIgnoreCase("GREEN")) {
            return ChartColor.VERY_DARK_GREEN;
        }
        if (colorName.equalsIgnoreCase("MAGENTA")) {
            return ChartColor.MAGENTA;
        }
        if (colorName.equalsIgnoreCase("RED")) {
            return ChartColor.RED;
        }
        if (colorName.equalsIgnoreCase("YELLOW")) {
            return ChartColor.YELLOW;
        }
        if (colorName.equalsIgnoreCase("BLACK")) {
            return ChartColor.BLACK;
        }
        if (colorName.equalsIgnoreCase("DARK GRAY")) {
            return ChartColor.DARK_GRAY;
        }
        if (colorName.equalsIgnoreCase("GRAY")) {
            return ChartColor.GRAY;
        }
        if (colorName.equalsIgnoreCase("LIGHT GRAY")) {
            return ChartColor.LIGHT_GRAY;
        }
        if (colorName.equalsIgnoreCase("WHITE")) {
            return ChartColor.WHITE;
        }
        if (!colorName.contains("#") && !colorName.contains("0x")) {
            colorName = "#" + colorName;
        }
        try {
            return Color.decode(colorName);
        }
        catch (Exception ignored) {
            return null;
        }
    }
}

