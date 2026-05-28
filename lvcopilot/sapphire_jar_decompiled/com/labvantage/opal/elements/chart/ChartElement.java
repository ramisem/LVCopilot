/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletRequest
 *  org.jfree.chart.ChartRenderingInfo
 *  org.jfree.chart.entity.ChartEntity
 *  org.jfree.chart.entity.EntityCollection
 *  org.jfree.chart.entity.StandardEntityCollection
 *  org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator
 *  org.jfree.chart.imagemap.StandardURLTagFragmentGenerator
 *  org.jfree.chart.imagemap.ToolTipTagFragmentGenerator
 *  org.jfree.chart.imagemap.URLTagFragmentGenerator
 *  org.jfree.chart.title.TextTitle
 *  org.jfree.ui.HorizontalAlignment
 *  org.jfree.ui.VerticalAlignment
 */
package com.labvantage.opal.elements.chart;

import com.labvantage.opal.elements.chart.util.BufferedImageWrapper;
import com.labvantage.opal.stats.Chart;
import com.labvantage.opal.stats.Stats;
import com.labvantage.opal.stats.StatsList;
import com.labvantage.opal.util.OpalUtil;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import javax.servlet.ServletRequest;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator;
import org.jfree.chart.imagemap.StandardURLTagFragmentGenerator;
import org.jfree.chart.imagemap.ToolTipTagFragmentGenerator;
import org.jfree.chart.imagemap.URLTagFragmentGenerator;
import org.jfree.chart.title.TextTitle;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.VerticalAlignment;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ChartElement
extends BaseElement {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private ArrayList __SelectedParams;
    private String __TempMapString = "";
    private List __StatsList;
    static final int DATAENTRY = 0;
    static final int SPC = 1;
    static final int QC = 2;
    static final int TREND = 0;
    static final int CUSUM = 1;
    private static final int CHART_CONSTANT = 45;
    private static final int HCHART_CONSTANT = 60;
    private HashMap __ChartingHashmap = new HashMap();
    private DataSet paramsetDataSet;
    static final int DIMENSION_CONST_HEIGHT = 450;
    static final int DIMENSION_CONST_WIDTH = 500;
    static final int CONST_3 = 3;
    static final double XAXIS_LEFTOVERS = 0.5;
    static final double CHART_ADJUSTMENTS = 0.6;
    static final String MODE_NEW = "new";

    public ChartElement() {
        this.__SelectedParams = new ArrayList();
    }

    @Override
    public String getHtml() {
        this.__ChartingHashmap = (HashMap)this.pageContext.getAttribute("ChartingHashmap");
        this.paramsetDataSet = (DataSet)this.pageContext.getAttribute("paramsetDataSet");
        String version = this.requestContext.getProperty("version");
        String url = "rc?command=operation&operationclass=com.labvantage.opal.elements.chart.ChartOperation";
        if (MODE_NEW.equals(version)) {
            if (this.__ChartingHashmap == null || this.__ChartingHashmap.size() == 0) {
                return "ERROR: Data not found in Page.";
            }
            url = url + "&chartid=" + this.createQCChart();
            return "<img src='" + url + "' USEMAP='#map' border='0'>" + this.__TempMapString;
        }
        this.__StatsList = (ArrayList)this.pageContext.getAttribute("statslist");
        if (this.__StatsList == null) {
            return "ERROR: Stats not found in Page.";
        }
        url = url + "&chartid=" + this.createChart();
        return "<img src='" + url + "' border='0'>";
    }

    private String createChart() {
        Color linecolor;
        String text;
        String yaxislabel;
        String bgcolor;
        long current = System.currentTimeMillis();
        String chartid = "chart_" + current;
        Chart chart = new Chart();
        int chartheight = 500;
        int chartwidth = 600;
        try {
            PropertyList chartdimensions = this.element.getPropertyListNotNull("chartdimensions");
            String width = chartdimensions.getProperty("width", "600");
            String height = chartdimensions.getProperty("height", "500");
            chartwidth = Integer.parseInt(width);
            chartheight = Integer.parseInt(height);
        }
        catch (NumberFormatException chartdimensions) {
            // empty catch block
        }
        for (int i = 0; i < this.__StatsList.size(); ++i) {
            chart.addStats((Stats)((Object)this.__StatsList.get(i)));
        }
        String charttitle = this.element.getProperty("title");
        if (charttitle != null && charttitle.trim().length() > 0) {
            chart.setChartTitle(OpalUtil.parseRequestString(this.pageContext, charttitle));
        }
        if ((bgcolor = this.element.getProperty("bgcolor")) == null) {
            bgcolor = "Orange";
        }
        chart.setBackgroundColor(this.getColor(bgcolor));
        if ("N".equalsIgnoreCase(this.element.getProperty("showinfo"))) {
            chart.setShowinfo(false);
        } else {
            chart.setShowinfo(true);
        }
        if ("N".equalsIgnoreCase(this.element.getProperty("showdatapointsvalues"))) {
            chart.setShowDataPointValue(false);
        } else {
            chart.setShowDataPointValue(true);
        }
        String xaxislabel = this.element.getProperty("xaxislabel");
        if (xaxislabel != null && xaxislabel.trim().length() > 0) {
            chart.setxAxisLabel(OpalUtil.parseRequestString(this.pageContext, xaxislabel));
        }
        if ((yaxislabel = this.element.getProperty("yaxislabel")) != null && yaxislabel.trim().length() > 0) {
            chart.setyAxisLabel(OpalUtil.parseRequestString(this.pageContext, yaxislabel));
        }
        Stats stats = chart.getStats(0);
        PropertyList chartparams = this.element.getPropertyList("params");
        if (chartparams != null) {
            String mean = chartparams.getProperty("mean");
            String sd = chartparams.getProperty("sd");
            String ucl = chartparams.getProperty("ucl");
            String lcl = chartparams.getProperty("lcl");
            if (mean != null && mean.trim().length() > 0) {
                try {
                    stats.setCL(Double.parseDouble(mean));
                }
                catch (Exception e) {
                    this.logger.error("The given Mean " + mean + " is not a number.");
                }
            }
            if (sd != null && sd.trim().length() > 0) {
                try {
                    stats.setSD(Double.parseDouble(sd));
                }
                catch (Exception e) {
                    this.logger.debug("The given Standard Deviation " + sd + " is not a number.");
                }
            }
            if (ucl != null && ucl.trim().length() > 0) {
                try {
                    stats.setUCL(Double.parseDouble(ucl));
                }
                catch (Exception e) {
                    this.logger.debug("The given Upper Control Limit " + sd + " is not a number.");
                }
            }
            if (lcl != null && lcl.trim().length() > 0) {
                try {
                    stats.setLCL(Double.parseDouble(lcl));
                }
                catch (Exception e) {
                    this.logger.debug("The given Lower Control Limit " + sd + " is not a number.");
                }
            }
        }
        PropertyList rangemarker = this.element.getPropertyList("rangemarker");
        PropertyList centerline = rangemarker.getPropertyList("centerline");
        PropertyList onesigma = rangemarker.getPropertyList("onesigma");
        PropertyList twosigma = rangemarker.getPropertyList("twosigma");
        PropertyList threesigma = rangemarker.getPropertyList("threesigma");
        if (centerline.getProperty("show").equalsIgnoreCase("Y")) {
            chart.addRangeMarker(stats.getCL(), centerline.getProperty("text"), this.getColor(centerline.getProperty("linecolor")));
        }
        if (threesigma.getProperty("show").equalsIgnoreCase("Y")) {
            text = threesigma.getProperty("text");
            linecolor = this.getColor(threesigma.getProperty("linecolor"));
            chart.addRangeMarker(stats.getUCL(), "Upper " + text, linecolor);
            chart.addRangeMarker(stats.getLCL(), "Lower " + text, linecolor);
        }
        if (onesigma.getProperty("show").equalsIgnoreCase("Y")) {
            text = onesigma.getProperty("text");
            linecolor = this.getColor(onesigma.getProperty("linecolor"));
            chart.addRangeMarker(stats.getCL() + stats.getSD(), "Upper " + text, linecolor);
            chart.addRangeMarker(stats.getCL() - stats.getSD(), "Lower " + text, linecolor);
        }
        if (twosigma.getProperty("show").equalsIgnoreCase("Y")) {
            text = twosigma.getProperty("text");
            linecolor = this.getColor(twosigma.getProperty("linecolor"));
            chart.addRangeMarker(stats.getCL() + stats.getSD() * 2.0, "Upper " + text, linecolor);
            chart.addRangeMarker(stats.getCL() - stats.getSD() * 2.0, "Lower " + text, linecolor);
        }
        PropertyListCollection subtitlecoll = this.element.getCollection("subtitle");
        for (int i = 0; i < subtitlecoll.size(); ++i) {
            PropertyList sublist = subtitlecoll.getPropertyList(i);
            String subtitletext = OpalUtil.parseRequestString(this.pageContext, sublist.getProperty("text"));
            chart.addSubtitle(subtitletext, this.getTextTitlePosition(sublist.getProperty("verticalalignment")), this.getTextTitleAlignment(sublist.getProperty("horizontalalignment")));
        }
        BufferedImage bi = chart.getChart().createBufferedImage(chartwidth, chartheight);
        BufferedImageWrapper mbi = new BufferedImageWrapper();
        mbi.setImage(bi);
        this.pageContext.getSession().setAttribute(chartid, (Object)mbi);
        this.logger.debug("Chart complete. Took " + (System.currentTimeMillis() - current) + " ms.");
        return chartid;
    }

    private String createQCChart() {
        String threesigmathickness;
        String twosigmathickness;
        String onesigmathickness;
        String yaxislabel;
        String charttitle;
        long current = System.currentTimeMillis();
        String chartid = "chart_" + current;
        Chart chart = new Chart();
        Locale locale = (Locale)this.pageContext.getAttribute("locale");
        ServletRequest req = this.pageContext.getRequest();
        String tempSelectedParams = req.getParameter("selectedparam");
        String[] tempSelectedParamArray = StringUtil.split(tempSelectedParams, ";");
        for (int i = 0; i < tempSelectedParamArray.length; ++i) {
            this.__SelectedParams.add(tempSelectedParamArray[i]);
        }
        HashMap assortedChartingHashMap = new HashMap();
        for (int i = 0; i < this.__SelectedParams.size(); ++i) {
            assortedChartingHashMap.put(this.__SelectedParams.get(i), this.__ChartingHashmap.get(this.__SelectedParams.get(i)));
        }
        chart.setChartingHashmap(assortedChartingHashMap);
        boolean showUAL = this.requestContext.getProperty("isUAL") == null || this.requestContext.getProperty("isUAL").equals("Y");
        boolean showUWL = this.requestContext.getProperty("isUWL") == null || this.requestContext.getProperty("isUWL").equals("Y");
        boolean showUNL = this.requestContext.getProperty("isUNL") == null || this.requestContext.getProperty("isUNL").equals("Y");
        boolean showCL = this.requestContext.getProperty("isTarget") == null || this.requestContext.getProperty("isTarget").equals("Y");
        boolean showLNL = this.requestContext.getProperty("isLNL") == null || this.requestContext.getProperty("isLNL").equals("Y");
        boolean showLWL = this.requestContext.getProperty("isLWL") == null || this.requestContext.getProperty("isLWL").equals("Y");
        boolean showLAL = this.requestContext.getProperty("isLAL") == null || this.requestContext.getProperty("isLAL").equals("Y");
        String xAxisScale = this.requestContext.getProperty("xscale") != null ? this.requestContext.getProperty("xscale") : "A";
        String yAxisScale = this.requestContext.getProperty("yscale") != null ? this.requestContext.getProperty("yscale") : "A";
        String explorerwidth = this.requestContext.getProperty("explorerwidth");
        String explorerheight = this.requestContext.getProperty("explorerheight");
        String styleChosen = this.requestContext.getProperty("styleVar");
        int navigationCounter = Integer.valueOf(this.requestContext.getProperty("navigationCounter"));
        String stackedTempParamidArray = "";
        String stackedTempSDArray = "";
        String stackedTempTargetArray = "";
        String sdFromRequest = "";
        String clFromRequest = "";
        try {
            stackedTempParamidArray = this.requestContext.getProperty("stackedParamidArray");
            stackedTempSDArray = this.requestContext.getProperty("stackedSDArray");
            stackedTempTargetArray = this.requestContext.getProperty("stackedTargetArray");
        }
        catch (Exception ex) {
            this.logger.debug("Error at stacked paramid,sd and target arrays in chartelement.java");
        }
        try {
            sdFromRequest = (String)this.pageContext.getAttribute("sdFromRequest");
            clFromRequest = (String)this.pageContext.getAttribute("clFromRequest");
        }
        catch (Exception ex) {
            this.logger.debug("Error at sdFromRequest and clFromRequest in chartelement.java");
        }
        PropertyList chartdimensions = this.element.getPropertyList("chartdimensions");
        boolean showdatapointsvaluesflag = this.element.getProperty("showdatapointsvalues").equals("Y");
        chart.setDataPointShowFlag(showdatapointsvaluesflag);
        String width = "";
        String height = "";
        try {
            width = chartdimensions.getProperty("width");
            height = chartdimensions.getProperty("height");
        }
        catch (Exception e) {
            width = null;
            height = null;
        }
        double minPlotValue = 0.0;
        double maxPlotValue = 0.0;
        if (this.__SelectedParams.size() > 0) {
            minPlotValue = ((StatsList)this.__ChartingHashmap.get(this.__SelectedParams.get(0))).getOverallMin();
            maxPlotValue = ((StatsList)this.__ChartingHashmap.get(this.__SelectedParams.get(0))).getOverallMax();
            for (int i = 1; i < this.__SelectedParams.size(); ++i) {
                StatsList list = (StatsList)this.__ChartingHashmap.get(this.__SelectedParams.get(i));
                double tempMax = list.getOverallMax();
                double tempMin = list.getOverallMin();
                if (maxPlotValue < tempMax) {
                    maxPlotValue = tempMax;
                }
                if (minPlotValue > tempMin) {
                    minPlotValue = tempMin;
                }
                this.logger.debug("Min plot value:" + minPlotValue);
                this.logger.debug("Max plot value:" + maxPlotValue);
            }
        }
        int chartheight = 450;
        int chartwidth = 500;
        int chartHt = 0;
        int explorerHt = 0;
        try {
            if (width != null && Integer.valueOf(width) >= chartwidth) {
                chartwidth = Integer.valueOf(width) - 45;
            }
            if (Integer.valueOf(explorerwidth) > chartwidth) {
                chartwidth = Integer.valueOf(explorerwidth) - 45;
            }
            if (height != null && explorerheight != null) {
                chartHt = Integer.valueOf(height);
                explorerHt = Double.valueOf(explorerheight).intValue();
            }
        }
        catch (Exception e) {
            this.logger.debug("An Exception has occured with width, height or explorer width. . .");
        }
        int reducedExplorerHt = (int)((double)explorerHt * 0.6);
        if (height != null && chartHt > reducedExplorerHt) {
            chartheight = chartHt - 60;
        } else if (height != null && chartHt < reducedExplorerHt) {
            chartheight = reducedExplorerHt - 60;
        }
        chart.setXAxisScaleType(xAxisScale);
        chart.setYAxisScaleType(yAxisScale);
        if (!xAxisScale.equals("A")) {
            try {
                chart.setXAxisMinValue(Double.parseDouble(this.requestContext.getProperty("xmin")) + 0.5);
                chart.setXAxisMaxValue(Double.parseDouble(this.requestContext.getProperty("xmax")) + 0.5);
            }
            catch (Exception nfe) {
                chart.setXAxisScaleType("A");
            }
        }
        if (!yAxisScale.equals("A")) {
            try {
                chart.setYAxisMinValue(Double.parseDouble(this.requestContext.getProperty("ymin")));
                chart.setYAxisMaxValue(Double.parseDouble(this.requestContext.getProperty("ymax")));
            }
            catch (Exception nfe) {
                chart.setYAxisScaleType("A");
            }
        } else if (yAxisScale.equals("A")) {
            try {
                chart.setYAxisMinValue(minPlotValue);
                chart.setYAxisMaxValue(maxPlotValue);
                this.logger.debug("minPlotValue-->" + minPlotValue);
                this.logger.debug("maxPlotValue-->" + maxPlotValue);
            }
            catch (NumberFormatException nfe) {
                chart.setYAxisScaleType("A");
            }
            catch (NullPointerException npe) {
                chart.setYAxisScaleType("A");
            }
        }
        if ((charttitle = this.element.getProperty("title")) != null && charttitle.trim().length() > 0) {
            String qcbatchsampletypeid = (String)this.pageContext.getAttribute("qcbatchsampletypeid");
            String qcbatchid = (String)this.pageContext.getAttribute("qcbatchid");
            String qcsampletype = (String)this.pageContext.getAttribute("qcsampletype");
            String fromEvergreenElement = OpalUtil.parseRequestString(this.pageContext, charttitle);
            if (fromEvergreenElement.length() > 0) {
                fromEvergreenElement = fromEvergreenElement.replace('[', ' ');
                fromEvergreenElement = fromEvergreenElement.replace(']', ' ');
                String[] tokensArray = fromEvergreenElement.split("\\s");
                for (int i = 0; i < tokensArray.length; ++i) {
                    String text = tokensArray[i];
                    if (text.equalsIgnoreCase("qcbatchid")) {
                        tokensArray[i] = qcbatchid;
                        continue;
                    }
                    if (text.equalsIgnoreCase("qcbatchsampletypeid")) {
                        tokensArray[i] = qcbatchsampletypeid;
                        continue;
                    }
                    if (text.equalsIgnoreCase("qcsampletype")) {
                        tokensArray[i] = qcsampletype;
                        continue;
                    }
                    if (!text.equalsIgnoreCase("keyid1")) continue;
                    tokensArray[i] = " ";
                }
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < tokensArray.length; ++i) {
                    sb.append(tokensArray[i]).append(" ");
                }
                fromEvergreenElement = sb.toString();
            }
            chart.setChartTitle(fromEvergreenElement);
        }
        boolean showInfo = "Y".equals(this.element.getProperty("showinfo"));
        String bgcolor = this.element.getProperty("bgcolor");
        if (bgcolor == null || bgcolor.length() == 0) {
            bgcolor = "Orange";
        }
        chart.setBackgroundColor(this.getColor(bgcolor));
        String bgcolor1 = this.element.getProperty("bgcolor1");
        if (bgcolor1 == null || bgcolor1.length() == 0) {
            bgcolor1 = "White";
        }
        chart.setBackgroundColor1(this.getColor(bgcolor1));
        chart.setShowinfo(showInfo);
        String xaxislabel = this.element.getProperty("xaxislabel");
        if (xaxislabel != null && xaxislabel.trim().length() > 0) {
            chart.setxAxisLabel(OpalUtil.parseRequestString(this.pageContext, xaxislabel));
        }
        if ((yaxislabel = this.element.getProperty("yaxislabel")) != null && yaxislabel.trim().length() > 0) {
            chart.setyAxisLabel(OpalUtil.parseRequestString(this.pageContext, yaxislabel));
        }
        this.logger.debug("About to add Range Markers ");
        PropertyList rangemarker = this.element.getPropertyList("rangemarker");
        this.logger.debug("Range Markers " + rangemarker);
        PropertyList centerline = rangemarker.getPropertyList("centerline");
        PropertyList onesigma = rangemarker.getPropertyList("onesigma");
        PropertyList twosigma = rangemarker.getPropertyList("twosigma");
        PropertyList threesigma = rangemarker.getPropertyList("threesigma");
        String centerlinethickness = centerline.getProperty("linewidth");
        if (centerlinethickness == null || centerlinethickness.length() == 0) {
            centerlinethickness = "1";
        }
        float centerlinethicknessFloat = Float.valueOf(centerlinethickness).floatValue();
        String centerlinestyle = centerline.getProperty("linestyle");
        if (centerlinestyle == null || centerlinestyle.length() == 0) {
            centerlinestyle = "Plain Bold";
        }
        if ((onesigmathickness = onesigma.getProperty("linewidth")) == null || onesigmathickness.length() == 0) {
            onesigmathickness = "1.0";
        }
        float onesigmathicknessFloat = Float.valueOf(onesigmathickness).floatValue();
        String onesigmastyle = onesigma.getProperty("linestyle");
        if (onesigmastyle == null || onesigmastyle.length() == 0) {
            onesigmastyle = "Plain Bold";
        }
        if ((twosigmathickness = twosigma.getProperty("linewidth")) == null || twosigmathickness.length() == 0) {
            twosigmathickness = "1.0";
        }
        float twosigmathicknessFloat = Float.valueOf(twosigmathickness).floatValue();
        String twosigmastyle = twosigma.getProperty("linestyle");
        if (twosigmastyle == null || twosigmastyle.length() == 0) {
            twosigmastyle = "Plain Bold";
        }
        if ((threesigmathickness = threesigma.getProperty("linewidth")) == null || threesigmathickness.length() == 0) {
            threesigmathickness = "1.0";
        }
        float threesigmathicknessFloat = Float.valueOf(threesigmathickness).floatValue();
        String threesigmastyle = threesigma.getProperty("linestyle");
        if (threesigmastyle == null || threesigmastyle.length() == 0) {
            threesigmastyle = "Plain Bold";
        }
        if (styleChosen.equals("overlaid") || styleChosen.equals("paged")) {
            Color linecolor;
            String text;
            String params = this.paramsetDataSet.getColumnValues("PARAMID", ";");
            String strcl = this.paramsetDataSet.getColumnValues("TARGETVALUE", ";");
            String strsd = this.paramsetDataSet.getColumnValues("SD", ";");
            String clValue = "";
            String sdValue = "";
            if (params != null && strcl != null && strsd != null) {
                String[] paramarray = StringUtil.split(params, ";");
                try {
                    for (int i = 0; i < paramarray.length; ++i) {
                        if (!paramarray[i].equals(this.__SelectedParams.get(0))) continue;
                        StatsList list = (StatsList)this.__ChartingHashmap.get(this.__SelectedParams.get(0));
                        clValue = String.valueOf(list.getOverallCL());
                        sdValue = String.valueOf(list.getOverallSD());
                    }
                }
                catch (Exception i) {
                    // empty catch block
                }
            }
            PropertyList chartparams = this.element.getPropertyList("params");
            String mean = "";
            String sd = "";
            String ucl = "";
            String lcl = "";
            if (chartparams != null) {
                mean = chartparams.getProperty("mean");
                sd = chartparams.getProperty("sd");
                ucl = chartparams.getProperty("ucl");
                lcl = chartparams.getProperty("lcl");
                if (mean != null && mean.trim().length() > 0) {
                    try {
                        mean = mean.trim();
                        chart.addMean(Double.parseDouble(mean));
                    }
                    catch (Exception e) {
                        this.logger.debug("The given Mean " + mean + " is not a number.");
                    }
                } else if (clFromRequest == null || clFromRequest.length() == 0) {
                    chart.addMean(Double.valueOf(clValue));
                } else if (clFromRequest.length() != 0) {
                    if (clFromRequest.indexOf("null") >= 0) {
                        chart.addMean(Double.valueOf(clValue));
                    } else {
                        chart.addMean(Double.parseDouble(clFromRequest));
                    }
                }
                if (sd != null && sd.trim().length() > 0) {
                    try {
                        sd = sd.trim();
                        chart.addSD(Double.parseDouble(sd));
                    }
                    catch (Exception e) {}
                } else if (sdFromRequest == null || sdFromRequest.length() == 0) {
                    chart.addSD(Double.valueOf(sdValue));
                } else if (sdFromRequest.length() != 0) {
                    if (sdFromRequest.indexOf("null") >= 0) {
                        chart.addSD(Double.valueOf(sdValue));
                    } else {
                        chart.addSD(Double.parseDouble(sdFromRequest));
                    }
                }
                if (ucl != null && ucl.trim().length() > 0) {
                    try {
                        ucl = ucl.trim();
                        chart.addUCL(Double.parseDouble(ucl));
                    }
                    catch (Exception e) {
                        // empty catch block
                    }
                }
                if (lcl != null && lcl.trim().length() > 0) {
                    try {
                        lcl = lcl.trim();
                        chart.addLCL(Double.parseDouble(lcl));
                    }
                    catch (Exception e) {
                        // empty catch block
                    }
                }
            }
            if (clFromRequest == null || clFromRequest.length() == 0) {
                chart.addMean(Double.valueOf(clValue));
            } else if (clFromRequest.length() != 0) {
                if (clFromRequest.indexOf("null") >= 0) {
                    chart.addMean(Double.valueOf(clValue));
                } else {
                    chart.addMean(Double.parseDouble(clFromRequest));
                }
            }
            if (sdFromRequest == null || sdFromRequest.length() == 0) {
                chart.addSD(Double.valueOf(sdValue));
            } else if (sdFromRequest.length() != 0) {
                if (sdFromRequest.indexOf("null") >= 0) {
                    chart.addSD(Double.valueOf(sdValue));
                } else {
                    chart.addSD(Double.parseDouble(sdFromRequest));
                }
            }
            if (centerline.getProperty("show").equalsIgnoreCase("Y") && showCL) {
                if (mean != null && mean.length() > 0) {
                    try {
                        chart.addRangeMarker(Double.parseDouble(mean), centerline.getProperty("text"), this.getColor(centerline.getProperty("linecolor")), centerlinethicknessFloat, centerlinestyle);
                    }
                    catch (Exception e) {}
                } else {
                    try {
                        chart.addRangeMarker(chart.getMean(), centerline.getProperty("text"), this.getColor(centerline.getProperty("linecolor")), centerlinethicknessFloat, centerlinestyle);
                    }
                    catch (Exception e) {
                        // empty catch block
                    }
                }
            }
            if (onesigma.getProperty("show").equalsIgnoreCase("Y")) {
                text = onesigma.getProperty("text");
                linecolor = this.getColor(onesigma.getProperty("linecolor"));
                if (showUNL) {
                    if (mean != null && mean.length() > 0 && sd != null && sd.length() > 0) {
                        try {
                            chart.addRangeMarker(Double.parseDouble(mean) + Double.parseDouble(sd), "U" + text, linecolor, onesigmathicknessFloat, onesigmastyle);
                        }
                        catch (Exception e) {
                            this.logger.debug("Error in One sigma additon in ChartElement object");
                        }
                    } else {
                        try {
                            chart.addRangeMarker(chart.getMean() + chart.getSD(), "U" + text, linecolor, onesigmathicknessFloat, onesigmastyle);
                        }
                        catch (Exception e) {
                            this.logger.debug("Error in One sigma additon in ChartElement object");
                        }
                    }
                }
                if (showLNL) {
                    if (mean != null && mean.length() > 0 && sd != null && sd.length() > 0) {
                        try {
                            chart.addRangeMarker(Double.parseDouble(mean) - Double.parseDouble(sd), "L" + text, linecolor, onesigmathicknessFloat, onesigmastyle);
                        }
                        catch (Exception e) {
                            this.logger.debug("Error in One sigma additon in ChartElement object");
                        }
                    } else {
                        try {
                            chart.addRangeMarker(chart.getMean() - chart.getSD(), "L" + text, linecolor, onesigmathicknessFloat, onesigmastyle);
                        }
                        catch (Exception e) {
                            this.logger.debug("Error in One sigma additon in ChartElement object");
                        }
                    }
                }
            }
            if ("Y".equalsIgnoreCase(twosigma.getProperty("show"))) {
                text = twosigma.getProperty("text");
                linecolor = this.getColor(twosigma.getProperty("linecolor"));
                if (showUWL) {
                    if (mean != null && mean.length() > 0 && sd != null && sd.length() > 0) {
                        try {
                            chart.addRangeMarker(Double.parseDouble(mean) + 2.0 * Double.parseDouble(sd), "U" + text, linecolor, twosigmathicknessFloat, twosigmastyle);
                        }
                        catch (Exception e) {
                            this.logger.debug("Error in Two sigma additon in ChartElement object");
                        }
                    } else {
                        try {
                            chart.addRangeMarker(chart.getMean() + 2.0 * chart.getSD(), "U" + text, linecolor, twosigmathicknessFloat, twosigmastyle);
                        }
                        catch (Exception e) {
                            this.logger.debug("Error in Two sigma additon in ChartElement object");
                        }
                    }
                }
                if (showLWL) {
                    if (mean != null && mean.length() > 0 && sd != null && sd.length() > 0) {
                        try {
                            chart.addRangeMarker(Double.parseDouble(mean) - 2.0 * Double.parseDouble(sd), "L" + text, linecolor, twosigmathicknessFloat, twosigmastyle);
                        }
                        catch (Exception e) {
                            this.logger.debug("Error in Two sigma additon in ChartElement object");
                        }
                    } else {
                        try {
                            chart.addRangeMarker(chart.getMean() - 2.0 * chart.getSD(), "L" + text, linecolor, twosigmathicknessFloat, twosigmastyle);
                        }
                        catch (Exception e) {
                            this.logger.debug("Error in Two sigma additon in ChartElement object");
                        }
                    }
                }
            }
            if (threesigma.getProperty("show").equalsIgnoreCase("Y")) {
                text = threesigma.getProperty("text");
                linecolor = this.getColor(threesigma.getProperty("linecolor"));
                if (showUAL) {
                    if (mean != null && mean.length() > 0 && sd != null && sd.length() > 0) {
                        try {
                            chart.addRangeMarker(Double.parseDouble(ucl), "U" + text, linecolor, threesigmathicknessFloat, threesigmastyle);
                        }
                        catch (Exception e) {
                            this.logger.debug("Error in Three sigma additon in ChartElement object");
                        }
                    } else {
                        try {
                            chart.addRangeMarker(chart.getMean() + 3.0 * chart.getSD(), "U" + text, linecolor, threesigmathicknessFloat, threesigmastyle);
                        }
                        catch (Exception e) {
                            this.logger.debug("Error in Three sigma additon in ChartElement object");
                        }
                    }
                }
                if (showLAL) {
                    try {
                        if (mean != null && mean.length() > 0 && sd != null && sd.length() > 0) {
                            chart.addRangeMarker(Double.parseDouble(lcl), "L" + text, linecolor, threesigmathicknessFloat, threesigmastyle);
                        } else {
                            chart.addRangeMarker(chart.getMean() - 3.0 * chart.getSD(), "L" + text, linecolor, threesigmathicknessFloat, threesigmastyle);
                        }
                    }
                    catch (Exception e) {
                        this.logger.debug("Error in Three sigma additon in ChartElement object");
                    }
                }
            }
        }
        if ("stacked".equals(styleChosen)) {
            String[] stackedParamIds = stackedTempParamidArray.split(":");
            String[] stackedSDs = stackedTempSDArray.split(":");
            String[] stackedTargets = stackedTempTargetArray.split(":");
            chart.setStackSize(stackedParamIds.length);
            chart.instantiateStackedMarkers();
            for (int i = 0; i < stackedParamIds.length; ++i) {
                Color linecolor;
                String text;
                PropertyList chartparams = this.element.getPropertyList("params");
                String mean = "";
                String sd = "";
                String ucl = "";
                String lcl = "";
                if (chartparams != null) {
                    mean = chartparams.getProperty("mean");
                    sd = chartparams.getProperty("sd");
                    ucl = chartparams.getProperty("ucl");
                    lcl = chartparams.getProperty("lcl");
                    if (mean != null && mean.trim().length() > 0) {
                        try {
                            mean = mean.trim();
                            chart.addStackedMean(Double.parseDouble(mean));
                        }
                        catch (Exception e) {
                            this.logger.debug("The given Mean " + mean + " is not a number.");
                        }
                    } else {
                        try {
                            chart.addStackedMean(Double.parseDouble(stackedTargets[i]));
                        }
                        catch (Exception e) {
                            this.logger.debug("The given Mean is not a number.");
                        }
                    }
                    if (sd != null && sd.trim().length() > 0) {
                        try {
                            sd = sd.trim();
                            chart.addStackedSD(Double.parseDouble(sd));
                        }
                        catch (Exception e) {
                            this.logger.debug("The given Standard Deviation " + sd + " is not a number.");
                        }
                    } else {
                        try {
                            chart.addStackedSD(Double.parseDouble(stackedSDs[i]));
                        }
                        catch (Exception e) {
                            this.logger.debug("The given sd is not a number.");
                        }
                    }
                    if (ucl != null && ucl.trim().length() > 0) {
                        try {
                            ucl = ucl.trim();
                        }
                        catch (Exception e) {
                            this.logger.debug("The given Upper Control Limit " + ucl + " is not a number.");
                        }
                    }
                    if (lcl != null && lcl.trim().length() > 0) {
                        try {
                            lcl = lcl.trim();
                        }
                        catch (Exception e) {
                            this.logger.debug("The given Lower Control Limit " + lcl + " is not a number.");
                        }
                    }
                }
                if (centerline.getProperty("show").equalsIgnoreCase("Y") && showCL) {
                    try {
                        if (mean != null && mean.trim().length() > 0) {
                            chart.addStackedRangeMarker(i, Double.parseDouble(mean), centerline.getProperty("text"), this.getColor(centerline.getProperty("linecolor")), centerlinethicknessFloat, centerlinestyle);
                        } else {
                            chart.addStackedRangeMarker(i, Double.valueOf(stackedTargets[i]), centerline.getProperty("text"), this.getColor(centerline.getProperty("linecolor")), centerlinethicknessFloat, centerlinestyle);
                        }
                    }
                    catch (Exception e) {
                        this.logger.debug("Error in Centerline addition in ChartElement object");
                    }
                }
                if (onesigma.getProperty("show").equalsIgnoreCase("Y")) {
                    text = onesigma.getProperty("text");
                    linecolor = this.getColor(onesigma.getProperty("linecolor"));
                    if (showUNL) {
                        try {
                            if (mean != null && mean.trim().length() > 0) {
                                chart.addStackedRangeMarker(i, Double.parseDouble(mean) + Double.parseDouble(sd), "U" + text, linecolor, onesigmathicknessFloat, onesigmastyle);
                            } else {
                                chart.addStackedRangeMarker(i, Double.valueOf(stackedTargets[i]) + Double.valueOf(stackedSDs[i]), "U" + text, linecolor, onesigmathicknessFloat, onesigmastyle);
                            }
                        }
                        catch (Exception e) {
                            this.logger.debug("Error in One sigma addition in ChartElement object");
                        }
                    }
                    if (showLNL) {
                        try {
                            if (mean != null && mean.trim().length() > 0) {
                                chart.addStackedRangeMarker(i, Double.parseDouble(mean) - Double.parseDouble(sd), "L" + text, linecolor, onesigmathicknessFloat, onesigmastyle);
                            } else {
                                chart.addStackedRangeMarker(i, Double.valueOf(stackedTargets[i]) - Double.valueOf(stackedSDs[i]), "L" + text, linecolor, onesigmathicknessFloat, onesigmastyle);
                            }
                        }
                        catch (Exception e) {
                            this.logger.debug("Error in One sigma addition in ChartElement object");
                        }
                    }
                }
                if (twosigma.getProperty("show").equalsIgnoreCase("Y")) {
                    text = twosigma.getProperty("text");
                    linecolor = this.getColor(twosigma.getProperty("linecolor"));
                    try {
                        if (showUWL) {
                            if (mean != null && mean.trim().length() > 0) {
                                chart.addStackedRangeMarker(i, Double.parseDouble(mean) + 2.0 * Double.parseDouble(sd), "U" + text, linecolor, onesigmathicknessFloat, onesigmastyle);
                            } else {
                                chart.addStackedRangeMarker(i, Double.valueOf(stackedTargets[i]) + 2.0 * Double.valueOf(stackedSDs[i]), "U" + text, linecolor, onesigmathicknessFloat, onesigmastyle);
                            }
                        }
                        if (showLWL) {
                            if (mean != null && mean.trim().length() > 0) {
                                chart.addStackedRangeMarker(i, Double.parseDouble(mean) - 2.0 * Double.parseDouble(sd), "L" + text, linecolor, onesigmathicknessFloat, onesigmastyle);
                            } else {
                                chart.addStackedRangeMarker(i, Double.valueOf(stackedTargets[i]) - 2.0 * Double.valueOf(stackedSDs[i]), "L" + text, linecolor, onesigmathicknessFloat, onesigmastyle);
                            }
                        }
                    }
                    catch (Exception e) {
                        this.logger.debug("Error in Two sigma addition in ChartElement object");
                    }
                }
                if (!threesigma.getProperty("show").equalsIgnoreCase("Y")) continue;
                text = threesigma.getProperty("text");
                linecolor = this.getColor(threesigma.getProperty("linecolor"));
                try {
                    if (showUAL) {
                        if (mean != null && mean.trim().length() > 0) {
                            chart.addStackedRangeMarker(i, Double.parseDouble(ucl), "U" + text, linecolor, onesigmathicknessFloat, onesigmastyle);
                        } else {
                            chart.addStackedRangeMarker(i, Double.valueOf(stackedTargets[i]) + 3.0 * Double.valueOf(stackedSDs[i]), "U" + text, linecolor, onesigmathicknessFloat, onesigmastyle);
                        }
                    }
                    if (!showLAL) continue;
                    if (mean != null && mean.trim().length() > 0) {
                        chart.addStackedRangeMarker(i, Double.parseDouble(lcl), "L" + text, linecolor, onesigmathicknessFloat, onesigmastyle);
                        continue;
                    }
                    chart.addStackedRangeMarker(i, Double.valueOf(stackedTargets[i]) - 3.0 * Double.valueOf(stackedSDs[i]), "L" + text, linecolor, onesigmathicknessFloat, onesigmastyle);
                    continue;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        PropertyListCollection subtitlecoll = this.element.getCollection("subtitle");
        for (int i = subtitlecoll.size() - 1; i >= 0; --i) {
            PropertyList sublist = subtitlecoll.getPropertyList(i);
            String subtitletext = OpalUtil.parseRequestString(this.pageContext, sublist.getProperty("text"));
            chart.addSubtitle(subtitletext, sublist.getProperty("position"), sublist.getProperty("textalignment"));
        }
        BufferedImage bi = null;
        BufferedImageWrapper mbi = new BufferedImageWrapper();
        ChartRenderingInfo info = new ChartRenderingInfo((EntityCollection)new StandardEntityCollection());
        if (styleChosen.equals("overlaid")) {
            bi = chart.getOverlaidChart(this.__SelectedParams, locale).createBufferedImage(chartwidth, chartheight, info);
        } else if (styleChosen.equals("stacked")) {
            int changedChartHeight = chartheight * this.__SelectedParams.size();
            bi = chart.getStackedChart(this.__SelectedParams, locale).createBufferedImage(chartwidth, changedChartHeight, info);
        } else if (styleChosen.equals("paged")) {
            chart.setSelectedParams(this.__SelectedParams);
            bi = chart.getPagedChart(navigationCounter, locale).createBufferedImage(chartwidth, chartheight, info);
        }
        mbi.setImage(bi);
        this.__TempMapString = "<MAP NAME=\"map\">\n";
        EntityCollection entities = info.getEntityCollection();
        if (entities != null) {
            for (ChartEntity entity : entities) {
                String area;
                if (entity.getToolTipText() == null && entity.getURLText() == null) continue;
                String toolTipText = entity.getToolTipText();
                String paramid = toolTipText.substring(0, toolTipText.indexOf(":")).trim();
                if (paramid.startsWith("Failed") || paramid.startsWith("Replicate")) {
                    area = entity.getImageMapAreaTag((ToolTipTagFragmentGenerator)new StandardToolTipTagFragmentGenerator(), (URLTagFragmentGenerator)new StandardURLTagFragmentGenerator());
                    if (area.length() <= 0) continue;
                    this.__TempMapString = this.__TempMapString + area + "\n";
                    continue;
                }
                if (!paramid.startsWith("Average") || (area = entity.getImageMapAreaTag((ToolTipTagFragmentGenerator)new StandardToolTipTagFragmentGenerator(), (URLTagFragmentGenerator)new StandardURLTagFragmentGenerator())).length() <= 0) continue;
                this.__TempMapString = this.__TempMapString + area + "\n";
            }
        }
        this.__TempMapString = this.__TempMapString + "</MAP>\n";
        this.pageContext.getSession().setAttribute(chartid, (Object)mbi);
        return chartid;
    }

    private Color getColor(String color) {
        if (color.equals("Black")) {
            return Color.black;
        }
        if (color.equals("Blue")) {
            return Color.blue;
        }
        if (color.equals("Cyan")) {
            return Color.cyan;
        }
        if (color.equals("Green")) {
            return Color.green;
        }
        if (color.equals("Gray")) {
            return Color.gray;
        }
        if (color.equals("Dark Gray")) {
            return Color.darkGray;
        }
        if (color.equals("Light Gray")) {
            return Color.lightGray;
        }
        if (color.equals("Magenta")) {
            return Color.magenta;
        }
        if (color.equals("Orange")) {
            return Color.orange;
        }
        if (color.equals("Pink")) {
            return Color.pink;
        }
        if (color.equals("Red")) {
            return Color.red;
        }
        if (color.equals("White")) {
            return Color.white;
        }
        if (color.equals("Yellow")) {
            return Color.yellow;
        }
        return Color.black;
    }

    private VerticalAlignment getTextTitlePosition(String position) {
        if (position.equals("Top")) {
            return VerticalAlignment.TOP;
        }
        if (position.equals("Middle")) {
            return VerticalAlignment.CENTER;
        }
        if (position.equals("Bottom")) {
            return VerticalAlignment.BOTTOM;
        }
        return TextTitle.DEFAULT_VERTICAL_ALIGNMENT;
    }

    private HorizontalAlignment getTextTitleAlignment(String alignment) {
        if (alignment.equals("Left")) {
            return HorizontalAlignment.LEFT;
        }
        if (alignment.equals("Center")) {
            return HorizontalAlignment.CENTER;
        }
        if (alignment.equals("Right")) {
            return HorizontalAlignment.RIGHT;
        }
        return TextTitle.DEFAULT_HORIZONTAL_ALIGNMENT;
    }
}

