/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.ChartFactory
 *  org.jfree.chart.JFreeChart
 *  org.jfree.chart.annotations.XYAnnotation
 *  org.jfree.chart.annotations.XYDrawableAnnotation
 *  org.jfree.chart.annotations.XYTextAnnotation
 *  org.jfree.chart.axis.AxisLocation
 *  org.jfree.chart.axis.NumberAxis
 *  org.jfree.chart.axis.NumberTickUnit
 *  org.jfree.chart.axis.TickUnit
 *  org.jfree.chart.axis.TickUnitSource
 *  org.jfree.chart.axis.TickUnits
 *  org.jfree.chart.axis.ValueAxis
 *  org.jfree.chart.plot.CombinedDomainXYPlot
 *  org.jfree.chart.plot.Marker
 *  org.jfree.chart.plot.Plot
 *  org.jfree.chart.plot.PlotOrientation
 *  org.jfree.chart.plot.ValueMarker
 *  org.jfree.chart.plot.XYPlot
 *  org.jfree.chart.renderer.xy.StandardXYItemRenderer
 *  org.jfree.chart.renderer.xy.XYItemRenderer
 *  org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
 *  org.jfree.chart.title.TextTitle
 *  org.jfree.chart.title.Title
 *  org.jfree.data.xy.XYDataset
 *  org.jfree.data.xy.XYSeries
 *  org.jfree.data.xy.XYSeriesCollection
 *  org.jfree.ui.Drawable
 *  org.jfree.ui.HorizontalAlignment
 *  org.jfree.ui.Layer
 *  org.jfree.ui.LengthAdjustmentType
 *  org.jfree.ui.RectangleAnchor
 *  org.jfree.ui.RectangleEdge
 *  org.jfree.ui.RectangleInsets
 *  org.jfree.ui.TextAnchor
 *  org.jfree.ui.VerticalAlignment
 *  org.jfree.util.UnitType
 */
package com.labvantage.opal.stats;

import com.labvantage.opal.elements.chart.util.FailureCircleDrawer;
import com.labvantage.opal.elements.chart.util.Shapes;
import com.labvantage.opal.stats.Stats;
import com.labvantage.opal.stats.StatsList;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYDrawableAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnit;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Drawable;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.Layer;
import org.jfree.ui.LengthAdjustmentType;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.jfree.ui.VerticalAlignment;
import org.jfree.util.UnitType;

public class Chart {
    static final String LABVANTAGE_CVS_ID = "$Revision: 103254 $";
    private List __StatsList;
    private List __MasterQCSampleStatsList;
    private List __NoDataList;
    private HashMap __SamplePointLocationMap;
    private String __ChartTitle = "";
    private String __XAxisLabel = "X Axis";
    private String __XAxisScaleType = "A";
    private double __XAxisMinValue;
    private double __XAxisMaxValue;
    private String __YAxisLabel = "Y Axis";
    private String __YAxisScaleType = "A";
    private double __YAxisMinValue = 9.0E99;
    private double __YAxisMaxValue = 9.0E99;
    private List __MarkerCollection;
    private List[] __StackedMarkerCollection;
    private List __XYTextAnnotationCollection;
    private Color __PlotColor = Color.red;
    private boolean __ShowInfo = false;
    private boolean __ShowDataPointValue = false;
    private List __PlotColorList;
    private List __SubTitleList;
    private Color __BackgroundColor;
    private Color __BackgroundColor1;
    private boolean showdatapointsvaluesflag = false;
    private XYSeriesCollection[][] __XYRepDatasetOverlaid;
    private XYSeriesCollection[][] __XYRepDatasetOverlaidTransform;
    private XYSeriesCollection[] __XYAvgDatasetOverlaid;
    private XYSeriesCollection[] __XYAvgDatasetOverlaidTransform;
    private List[] __NoDataListAvgOverlaid;
    private List[][] __NoDataListRepOverlaid;
    private XYSeriesCollection[][] __XYRepDatasetStacked;
    private XYSeriesCollection[][] __XYRepDatasetStackedTransform;
    private XYSeriesCollection[] __XYAvgDatasetStacked;
    private XYSeriesCollection[] __XYAvgDatasetStackedTransform;
    private List[] __NoDataListAvgStacked;
    private List[][] __NoDataListRepStacked;
    private XYSeriesCollection[] __XYRepDatasetPaged;
    private XYSeriesCollection[] __XYRepDatasetPagedTransform;
    private XYSeriesCollection __XYAvgDatasetPaged;
    private XYSeriesCollection __XYAvgDatasetPagedTransform;
    private ArrayList __NoDataListAvgPaged;
    private ArrayList[] __NoDataListRepPaged;
    private int numberOfTickUnits = 0;
    private HashMap __ChartingHashmap;
    private ArrayList __SelectedParams;
    private int __StackSize;
    private double __Mean;
    private double __Sd;
    private double __Ucl;
    private double __Lcl;
    private ArrayList __StackedCL;
    private ArrayList __StackedSD;
    static final int MAX_RANGE_MULTIPLIER = 4;
    static final double ANNOTATION_CONSTANT = 5.0;
    static final double XYTEXTANNOTATION_SHIFT = 0.1;
    static final int FONT_SIZE_CONSTANT = 12;
    static final double FONT_CONST_1 = 0.01;
    static final double FONT_CONST_2 = 0.1;
    static final double GAP_CONSTANT = 8.0;
    static final float STYLE_CONST_5F = 5.0f;
    static final float STYLE_CONST_3F = 3.0f;
    static final float STYLE_CONST_6F = 6.0f;
    static final float STYLE_CONST_10F = 10.0f;

    public void instantiateStackedMarkers() {
        this.__StackedMarkerCollection = new ArrayList[this.__StackSize];
        for (int j = 0; j < this.__StackSize; ++j) {
            this.__StackedMarkerCollection[j] = new ArrayList();
        }
    }

    public Chart() {
        this.__StatsList = new ArrayList();
        this.__MasterQCSampleStatsList = new ArrayList();
        this.__SubTitleList = new ArrayList();
        this.__MarkerCollection = new ArrayList();
        this.__XYTextAnnotationCollection = new ArrayList();
        this.__PlotColorList = new ArrayList();
        this.__PlotColorList.add(Color.RED);
        this.__PlotColorList.add(Color.BLUE);
        this.__PlotColorList.add(Color.GREEN);
        this.__PlotColorList.add(Color.MAGENTA);
        this.__PlotColorList.add(Color.ORANGE);
        this.__SamplePointLocationMap = new HashMap();
        this.__ChartingHashmap = new HashMap();
        this.__StackedCL = new ArrayList();
        this.__StackedSD = new ArrayList();
        this.__NoDataList = new ArrayList();
    }

    public void setChartingHashmap(HashMap hm) {
        this.__ChartingHashmap = hm;
    }

    public Chart(Stats stats) {
        this();
        this.__StatsList.add(this.__StatsList.size(), stats);
    }

    public void addStats(Stats stats) {
        this.__StatsList.add(this.__StatsList.size(), stats);
    }

    public void addMasterQCSampleStats(Stats MasterQCSampleStats) {
        this.__MasterQCSampleStatsList.add(this.__MasterQCSampleStatsList.size(), MasterQCSampleStats);
    }

    public Stats getStats(int index) {
        Stats stats = null;
        try {
            stats = (Stats)((Object)this.__StatsList.get(index));
        }
        catch (Exception exception) {
            // empty catch block
        }
        return stats;
    }

    public JFreeChart getStackedChart(ArrayList selectedParams, Locale locale) {
        return this.createStackedXYChart(this.getChartTitle(), selectedParams, locale);
    }

    public JFreeChart getPagedChart(int selectedParamIndex, Locale locale) {
        return this.createPagedXYChart(this.getChartTitle(), selectedParamIndex, locale);
    }

    public boolean isShowDataPointValue() {
        return this.__ShowDataPointValue;
    }

    public void setShowDataPointValue(boolean __ShowDataPointValue) {
        this.__ShowDataPointValue = __ShowDataPointValue;
    }

    private JFreeChart createPagedXYChart(String chartTitle, int selectedParamIndex, Locale locale) {
        int j;
        NumberFormat currFormat = NumberFormat.getNumberInstance(locale);
        String selectedParam = (String)this.__SelectedParams.get(selectedParamIndex);
        XYDataset dataset = null;
        dataset = this.getXYDataSetPaged(selectedParam);
        JFreeChart chart = ChartFactory.createXYLineChart((String)chartTitle, (String)this.getxAxisLabel(), (String)this.getyAxisLabel(), (XYDataset)dataset, (PlotOrientation)PlotOrientation.VERTICAL, (boolean)true, (boolean)true, (boolean)false);
        StatsList list = (StatsList)this.__ChartingHashmap.get(selectedParam);
        Stats stats = (Stats)((Object)list.get(0));
        double plotYmax = 0.0;
        double plotYmin = 0.0;
        if (this.__BackgroundColor == null) {
            chart.setBackgroundPaint((Paint)Color.ORANGE);
        } else {
            chart.setBackgroundPaint((Paint)this.__BackgroundColor);
        }
        if (this.__BackgroundColor1 == null) {
            this.__BackgroundColor1 = Color.YELLOW;
        }
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint((Paint)this.__BackgroundColor1);
        ValueAxis xAxis = plot.getDomainAxis();
        xAxis.setAutoTickUnitSelection(false);
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        NumberAxis rangeaxis = (NumberAxis)plot.getRangeAxis();
        rangeaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        double maxVal = list.getOverallMax();
        double minVal = list.getOverallMin();
        if (this.getYAxisScaleType().equals("A")) {
            plotYmax = this.__Mean + 4.0 * this.__Sd > maxVal ? this.__Mean + 4.0 * this.__Sd + this.getRangeConstant(maxVal, minVal) : maxVal + this.getRangeConstant(maxVal, minVal);
            plotYmin = this.__Mean - 4.0 * this.__Sd < minVal ? this.__Mean - 4.0 * this.__Sd - this.getRangeConstant(maxVal, minVal) : minVal - this.getRangeConstant(maxVal, minVal);
        } else {
            plotYmin = this.__YAxisMinValue;
            plotYmax = this.__YAxisMaxValue;
        }
        if (plotYmin > plotYmax) {
            plotYmin = plotYmax;
        }
        rangeaxis.setRange(plotYmin, this.getYmaxRange(plotYmin, plotYmax));
        NumberAxis domainaxis = (NumberAxis)plot.getDomainAxis();
        domainaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        if (!this.getXAxisScaleType().equals("A")) {
            if (this.getXAxisMinValue() > this.getXAxisMaxValue()) {
                this.setXAxisMinValue(this.__XAxisMaxValue);
            }
            domainaxis.setRange(this.getXAxisMinValue(), this.getYmaxRange(this.getXAxisMinValue(), this.getXAxisMaxValue()));
        }
        this.addRangeMarkers(plot);
        this.addXYTextAnnotations(plot);
        XYLineAndShapeRenderer xyLineAndShapeRenderer = (XYLineAndShapeRenderer)plot.getRenderer();
        String param2 = stats.getYparam();
        List evaluationList2 = stats.getAllEvalStatus();
        ArrayList templistSample = (ArrayList)this.__NoDataListAvgPaged.get(0);
        int innerSize = list.size();
        xyLineAndShapeRenderer.setShapesFilled(new Boolean(false));
        xyLineAndShapeRenderer.setCreateEntities(new Boolean(true));
        xyLineAndShapeRenderer.setShapesVisible(new Boolean(true));
        xyLineAndShapeRenderer.setLinesVisible(new Boolean(true));
        Shapes drawer = new Shapes(0);
        Shapes drawerAvg = new Shapes(0, "avg");
        for (j = 1; j < innerSize; ++j) {
            Stats stats1 = (Stats)((Object)list.get(j));
            String param = stats1.getYparam();
            List evaluationList = stats1.getAllEvalStatus();
            ArrayList templist2 = (ArrayList)this.__NoDataListRepPaged[j].get(0);
            for (int l = 0; l < this.__XYRepDatasetPaged[j].getItemCount(0); ++l) {
                double yValueT = this.__XYRepDatasetPagedTransform[j].getYValue(0, l);
                double xValueT = this.__XYRepDatasetPagedTransform[j].getXValue(0, l);
                if (templist2.contains(Integer.toString(l))) continue;
                String xtemp = stats1.getX(l);
                String keyid1 = xtemp.substring(0, xtemp.indexOf("$"));
                String displayValue = xtemp.substring(xtemp.indexOf("$") + 1, xtemp.length());
                String status = (String)evaluationList.get(l);
                status = status.trim();
                XYDrawableAnnotation xydrawableannotation = new XYDrawableAnnotation(xValueT, yValueT, 5.0, 5.0, (Drawable)drawer);
                XYTextAnnotation xyTextAnnotation = new XYTextAnnotation(displayValue, xValueT + 0.1, yValueT);
                plot.addAnnotation((XYAnnotation)xydrawableannotation);
                if (status.equalsIgnoreCase("FAIL")) {
                    FailureCircleDrawer failuredrawer = new FailureCircleDrawer();
                    XYDrawableAnnotation xydrawableannotation1 = new XYDrawableAnnotation(xValueT, yValueT, 5.0, 5.0, (Drawable)failuredrawer);
                    xydrawableannotation1.setToolTipText("Failed Replicate Sample:" + keyid1 + " Param:" + param + " Value:" + displayValue);
                    plot.addAnnotation((XYAnnotation)xydrawableannotation1);
                    if (!this.showdatapointsvaluesflag) continue;
                    plot.addAnnotation((XYAnnotation)xyTextAnnotation);
                    continue;
                }
                xydrawableannotation.setToolTipText("Replicate Sample:" + keyid1 + " Param:" + param + " Value:" + displayValue);
                if (!this.showdatapointsvaluesflag) continue;
                plot.addAnnotation((XYAnnotation)xyTextAnnotation);
            }
        }
        for (j = 0; j < this.__XYAvgDatasetPaged.getItemCount(0); ++j) {
            double xValueT = this.__XYAvgDatasetPagedTransform.getXValue(0, j);
            double yValueT = this.__XYAvgDatasetPagedTransform.getYValue(0, j);
            if (templistSample.contains(Integer.toString(j))) continue;
            String xtemp = stats.getX(j);
            String keyid2 = xtemp.substring(0, xtemp.indexOf("$"));
            String displayValue = xtemp.substring(xtemp.indexOf("$") + 1, xtemp.length());
            String status = (String)evaluationList2.get(j);
            status = status.trim();
            XYDrawableAnnotation xydrawableannotation = new XYDrawableAnnotation(xValueT, yValueT, 5.0, 5.0, (Drawable)drawerAvg);
            XYTextAnnotation xyTextAnnotation = new XYTextAnnotation(displayValue, xValueT + 0.1, yValueT);
            plot.addAnnotation((XYAnnotation)xydrawableannotation);
            if (status.equalsIgnoreCase("FAIL")) {
                FailureCircleDrawer failuredrawer = new FailureCircleDrawer();
                XYDrawableAnnotation xydrawableannotation1 = new XYDrawableAnnotation(xValueT, yValueT, 5.0, 5.0, (Drawable)failuredrawer);
                xydrawableannotation1.setToolTipText("Failed Average Sample:" + keyid2 + " Param:" + param2 + " Value:" + displayValue);
                plot.addAnnotation((XYAnnotation)xydrawableannotation1);
                if (!this.showdatapointsvaluesflag) continue;
                plot.addAnnotation((XYAnnotation)xyTextAnnotation);
                continue;
            }
            xydrawableannotation.setToolTipText("Average Sample:" + keyid2 + " Param:" + param2 + " Value:" + displayValue);
            if (!this.showdatapointsvaluesflag) continue;
            plot.addAnnotation((XYAnnotation)xyTextAnnotation);
        }
        if (this.isShowinfo()) {
            Font infofont = new Font("SansSerif", 0, 12);
            RectangleInsets infospacer = new RectangleInsets(UnitType.RELATIVE, 0.01, 0.1, 0.1, 0.1);
            TextTitle chartinfo = new TextTitle("Chart Info");
            chartinfo.setPosition(RectangleEdge.BOTTOM);
            chartinfo.setHorizontalAlignment(HorizontalAlignment.LEFT);
            TextTitle meantitle = new TextTitle("Center Line ( Mean ) : " + currFormat.format(this.__Mean), infofont, (Paint)Color.black, RectangleEdge.BOTTOM, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, infospacer);
            TextTitle sdtitle = new TextTitle("Standard Deviation : " + currFormat.format(this.__Sd), infofont, (Paint)Color.black, RectangleEdge.BOTTOM, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, infospacer);
            chart.addSubtitle((Title)sdtitle);
            chart.addSubtitle((Title)meantitle);
        }
        for (int i = 0; i < this.__SubTitleList.size(); ++i) {
            chart.addSubtitle((Title)((TextTitle)this.__SubTitleList.get(i)));
        }
        return chart;
    }

    private XYDataset getXYDataSetPaged(String selectedParam) {
        XYSeriesCollection xySeriesColln11 = new XYSeriesCollection();
        this.__XYAvgDatasetPaged = new XYSeriesCollection();
        this.__XYAvgDatasetPagedTransform = new XYSeriesCollection();
        this.__NoDataListAvgPaged = new ArrayList();
        StatsList list = (StatsList)this.__ChartingHashmap.get(selectedParam);
        int innerSize = list.size();
        this.__XYRepDatasetPaged = new XYSeriesCollection[innerSize];
        this.__XYRepDatasetPagedTransform = new XYSeriesCollection[innerSize];
        this.__NoDataListRepPaged = new ArrayList[innerSize];
        for (int j = 0; j < innerSize; ++j) {
            String xValue;
            String xtemp;
            double valueT;
            double value;
            List statsvaluelist;
            double lastvalueT;
            double lastvalue;
            Stats stats;
            if (j == 0) {
                stats = (Stats)((Object)list.get(0));
                XYSeries data = new XYSeries((Comparable)((Object)stats.getYparam()), true);
                XYSeries dataTransform = new XYSeries((Comparable)((Object)stats.getYparam()), true);
                ArrayList<String> templist = new ArrayList<String>();
                lastvalue = 0.0;
                lastvalueT = 0.0;
                statsvaluelist = stats.getValuesAsList();
                List statstransformvaluelist = stats.getAllTransformValues();
                this.numberOfTickUnits = statsvaluelist.size();
                for (int k = 0; k < statsvaluelist.size(); ++k) {
                    value = (Double)statsvaluelist.get(k);
                    valueT = (Double)statstransformvaluelist.get(k);
                    xtemp = stats.getX(k);
                    xValue = xtemp.substring(0, xtemp.indexOf("$"));
                    this.__SamplePointLocationMap.put(stats.getYparam() + "|" + xValue, new Integer(k + 1));
                    if (value == 9.0E99) {
                        data.add((double)(k + 1), lastvalue);
                        dataTransform.add((double)(k + 1), lastvalueT);
                        templist.add(Integer.toString(k));
                        continue;
                    }
                    data.add((double)(k + 1), value);
                    dataTransform.add((double)(k + 1), valueT);
                    lastvalue = value;
                    lastvalueT = valueT;
                }
                xySeriesColln11 = new XYSeriesCollection();
                xySeriesColln11.addSeries(dataTransform);
                this.__XYAvgDatasetPaged = new XYSeriesCollection();
                this.__XYAvgDatasetPaged.addSeries(data);
                this.__XYAvgDatasetPagedTransform = new XYSeriesCollection();
                this.__XYAvgDatasetPagedTransform.addSeries(dataTransform);
                this.__NoDataListAvgPaged = new ArrayList();
                this.__NoDataListAvgPaged.add(templist);
                continue;
            }
            if (j <= 0) continue;
            stats = (Stats)((Object)list.get(j));
            ArrayList<String> templist = new ArrayList<String>();
            XYSeries data = new XYSeries((Comparable)((Object)(stats.getYparam() + " Master")), true);
            XYSeries dataTransform = new XYSeries((Comparable)((Object)(stats.getYparam() + " Master")), true);
            lastvalue = 0.0;
            lastvalueT = 0.0;
            statsvaluelist = stats.getValuesAsList();
            List statsvaluelist1 = stats.getAllTransformValues();
            for (int m = 0; m < statsvaluelist.size(); ++m) {
                value = (Double)statsvaluelist.get(m);
                valueT = (Double)statsvaluelist1.get(m);
                xtemp = stats.getX(m);
                xValue = xtemp.substring(0, xtemp.indexOf("$"));
                if (value == 9.0E99) {
                    data.add((double)((Integer)this.__SamplePointLocationMap.get(stats.getYparam() + "|" + xValue)).intValue(), lastvalue);
                    dataTransform.add((double)((Integer)this.__SamplePointLocationMap.get(stats.getYparam() + "|" + xValue)).intValue(), lastvalueT);
                    data.setDescription(xValue);
                    templist.add(Integer.toString(m));
                    continue;
                }
                data.add((double)((Integer)this.__SamplePointLocationMap.get(stats.getYparam() + "|" + xValue)).intValue(), value);
                dataTransform.add((double)((Integer)this.__SamplePointLocationMap.get(stats.getYparam() + "|" + xValue)).intValue(), valueT);
                data.setDescription(xValue);
                lastvalue = value;
                lastvalueT = valueT;
            }
            this.__XYRepDatasetPaged[j] = new XYSeriesCollection();
            this.__XYRepDatasetPaged[j].addSeries(data);
            this.__XYRepDatasetPagedTransform[j] = new XYSeriesCollection();
            this.__XYRepDatasetPagedTransform[j].addSeries(dataTransform);
            this.__NoDataListRepPaged[j] = new ArrayList();
            this.__NoDataListRepPaged[j].add(templist);
        }
        return xySeriesColln11;
    }

    private JFreeChart createStackedXYChart(String chartTitle, ArrayList selectedParams, Locale locale) {
        XYDataset[] datasets = new XYDataset[selectedParams.size()];
        datasets = this.getXYDataSetArrays(selectedParams);
        XYPlot[] xyplot = new XYPlot[datasets.length];
        for (int m = 0; m < datasets.length; ++m) {
            int j;
            XYDataset xydataset = datasets[m];
            StatsList list = (StatsList)this.__ChartingHashmap.get(selectedParams.get(m));
            Stats stats = (Stats)((Object)list.get(0));
            String paramid = stats.getYparam();
            XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
            xyLineAndShapeRenderer.setShapesFilled(new Boolean(false));
            xyLineAndShapeRenderer.setCreateEntities(new Boolean(true));
            xyLineAndShapeRenderer.setShapesVisible(new Boolean(true));
            xyLineAndShapeRenderer.setLinesVisible(new Boolean(true));
            int iNumTickUnits = this.numberOfTickUnits;
            TickUnits tickunits = new TickUnits();
            for (int k = 0; k <= iNumTickUnits; ++k) {
                tickunits.add((TickUnit)new NumberTickUnit((double)k));
            }
            NumberAxis newDomainAxis = new NumberAxis(paramid);
            newDomainAxis.setStandardTickUnits((TickUnitSource)tickunits);
            String yaxisLabel = this.getyAxisLabel();
            NumberAxis newRangeAxis = new NumberAxis(yaxisLabel);
            xyplot[m] = new XYPlot(xydataset, (ValueAxis)newDomainAxis, (ValueAxis)newRangeAxis, (XYItemRenderer)xyLineAndShapeRenderer);
            xyplot[m].setDomainAxis(m + 1, (ValueAxis)newDomainAxis);
            xyplot[m].setDomainAxisLocation(m + 1, AxisLocation.BOTTOM_OR_LEFT);
            NumberAxis rangeaxis = (NumberAxis)xyplot[m].getRangeAxis();
            NumberAxis domainaxis = (NumberAxis)xyplot[m].getDomainAxis();
            double plotYmax = 0.0;
            double plotYmin = 0.0;
            double maxVal = list.getOverallMax();
            double minVal = list.getOverallMin();
            if (this.getYAxisScaleType().equals("A")) {
                double upper = (Double)this.__StackedCL.get(m) + 4.0 * (Double)this.__StackedSD.get(m);
                double lower = (Double)this.__StackedCL.get(m) - 4.0 * (Double)this.__StackedSD.get(m);
                plotYmax = upper > maxVal ? upper + this.getRangeConstant(maxVal, minVal) : maxVal + this.getRangeConstant(maxVal, minVal);
                plotYmin = lower < minVal ? lower - this.getRangeConstant(maxVal, minVal) : minVal - this.getRangeConstant(maxVal, minVal);
            } else {
                plotYmin = this.__YAxisMinValue;
                plotYmax = this.__YAxisMaxValue;
            }
            if (plotYmin > plotYmax) {
                plotYmin = plotYmax;
            }
            rangeaxis.setRange(plotYmin, this.getYmaxRange(plotYmin, plotYmax));
            if (!this.getXAxisScaleType().equals("A")) {
                if (this.getXAxisMinValue() > this.getXAxisMaxValue()) {
                    this.setXAxisMinValue(this.__XAxisMaxValue);
                }
                domainaxis.setRange(this.getXAxisMinValue(), this.getYmaxRange(this.getXAxisMinValue(), this.getXAxisMaxValue()));
            }
            if (this.__BackgroundColor1 == null) {
                this.__BackgroundColor1 = Color.YELLOW;
            }
            xyplot[m].setBackgroundPaint((Paint)this.__BackgroundColor1);
            this.addStackedRangeMarkers(xyplot[m], m);
            String param2 = stats.getYparam();
            List evaluationList2 = stats.getAllEvalStatus();
            ArrayList templist1 = (ArrayList)this.__NoDataListAvgStacked[m].get(0);
            Shapes drawer = new Shapes(m);
            Shapes drawerAvg = new Shapes(m, "avg");
            int innerSize = list.size();
            for (j = 1; j < innerSize; ++j) {
                Stats stats1 = (Stats)((Object)list.get(j));
                String param = stats1.getYparam();
                List evaluationList = stats1.getAllEvalStatus();
                ArrayList templist2 = (ArrayList)this.__NoDataListRepStacked[m][j].get(0);
                for (int l = 0; l < this.__XYRepDatasetStacked[m][j].getItemCount(0); ++l) {
                    double yValueT = this.__XYRepDatasetStackedTransform[m][j].getYValue(0, l);
                    double xValueT = this.__XYRepDatasetStackedTransform[m][j].getXValue(0, l);
                    if (templist2.contains(Integer.toString(l))) continue;
                    String xtemp = stats1.getX(l);
                    String keyid1 = xtemp.substring(0, xtemp.indexOf("$"));
                    String displayValue = xtemp.substring(xtemp.indexOf("$") + 1, xtemp.length());
                    String status = (String)evaluationList.get(l);
                    status = status.trim();
                    XYDrawableAnnotation xydrawableannotation = new XYDrawableAnnotation(xValueT, yValueT, 5.0, 5.0, (Drawable)drawer);
                    XYTextAnnotation xyTextAnnotation = new XYTextAnnotation(displayValue, xValueT + 0.1, yValueT);
                    xyplot[m].addAnnotation((XYAnnotation)xydrawableannotation);
                    if (status.equalsIgnoreCase("FAIL")) {
                        FailureCircleDrawer failuredrawer = new FailureCircleDrawer();
                        XYDrawableAnnotation xydrawableannotation1 = new XYDrawableAnnotation(xValueT, yValueT, 5.0, 5.0, (Drawable)failuredrawer);
                        xydrawableannotation1.setToolTipText("Failed Replicate Sample:" + keyid1 + " Param:" + param + " Value:" + displayValue);
                        xyplot[m].addAnnotation((XYAnnotation)xydrawableannotation1);
                        if (!this.showdatapointsvaluesflag) continue;
                        xyplot[m].addAnnotation((XYAnnotation)xyTextAnnotation);
                        continue;
                    }
                    xydrawableannotation.setToolTipText("Replicate Sample:" + keyid1 + " Param:" + param + " Value:" + displayValue);
                    if (!this.showdatapointsvaluesflag) continue;
                    xyplot[m].addAnnotation((XYAnnotation)xyTextAnnotation);
                }
            }
            for (j = 0; j < this.__XYAvgDatasetStacked[m].getItemCount(0); ++j) {
                double yValueT = this.__XYAvgDatasetStackedTransform[m].getYValue(0, j);
                double xValueT = this.__XYAvgDatasetStackedTransform[m].getXValue(0, j);
                if (templist1.contains(Integer.toString(j))) continue;
                String xtemp = stats.getX(j);
                String keyid1 = xtemp.substring(0, xtemp.indexOf("$"));
                String displayValue = xtemp.substring(xtemp.indexOf("$") + 1, xtemp.length());
                String status = (String)evaluationList2.get(j);
                status = status.trim();
                XYDrawableAnnotation xydrawableannotation = new XYDrawableAnnotation(xValueT, yValueT, 5.0, 5.0, (Drawable)drawerAvg);
                XYTextAnnotation xyTextAnnotation = new XYTextAnnotation(displayValue, xValueT + 0.1, yValueT);
                xyplot[m].addAnnotation((XYAnnotation)xydrawableannotation);
                if (status.equalsIgnoreCase("FAIL")) {
                    FailureCircleDrawer failuredrawer = new FailureCircleDrawer();
                    XYDrawableAnnotation xydrawableannotation1 = new XYDrawableAnnotation(xValueT, yValueT, 5.0, 5.0, (Drawable)failuredrawer);
                    xydrawableannotation1.setToolTipText("Failed Average Sample:" + keyid1 + " Param:" + param2 + " Value:" + displayValue);
                    xyplot[m].addAnnotation((XYAnnotation)xydrawableannotation1);
                    if (!this.showdatapointsvaluesflag) continue;
                    xyplot[m].addAnnotation((XYAnnotation)xyTextAnnotation);
                    continue;
                }
                xydrawableannotation.setToolTipText("Average Sample:" + keyid1 + " Param:" + param2 + " Value:" + displayValue);
                if (!this.showdatapointsvaluesflag) continue;
                xyplot[m].addAnnotation((XYAnnotation)xyTextAnnotation);
            }
            xyplot[m].getRenderer().setSeriesVisibleInLegend(new Boolean(false));
        }
        CombinedDomainXYPlot combineddomainxyplot = new CombinedDomainXYPlot();
        combineddomainxyplot.setGap(8.0);
        for (int k = 0; k < xyplot.length; ++k) {
            combineddomainxyplot.add(xyplot[k], 1);
        }
        combineddomainxyplot.setOrientation(PlotOrientation.VERTICAL);
        ValueAxis s = combineddomainxyplot.getDomainAxis();
        s.setAxisLineVisible(false);
        s.setTickMarksVisible(false);
        s.setTickLabelsVisible(false);
        JFreeChart stackedChart = new JFreeChart(chartTitle, JFreeChart.DEFAULT_TITLE_FONT, (Plot)combineddomainxyplot, true);
        String xaxisLabel = this.getxAxisLabel();
        Font infofont = new Font("SansSerif", 0, 12);
        if (this.__BackgroundColor == null) {
            stackedChart.setBackgroundPaint((Paint)Color.ORANGE);
        } else {
            stackedChart.setBackgroundPaint((Paint)this.__BackgroundColor);
        }
        infofont = new Font("SansSerif", 0, 12);
        RectangleInsets infospacer = new RectangleInsets(UnitType.RELATIVE, 0.01, 0.1, 0.1, 0.1);
        TextTitle xaxisTitle = new TextTitle(xaxisLabel, infofont, (Paint)Color.black, RectangleEdge.BOTTOM, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, infospacer);
        for (int i = 0; i < this.__SubTitleList.size(); ++i) {
            stackedChart.addSubtitle((Title)((TextTitle)this.__SubTitleList.get(i)));
        }
        stackedChart.addSubtitle((Title)xaxisTitle);
        return stackedChart;
    }

    private XYDataset[] getXYDataSetArrays(ArrayList selectedParamids) {
        int size = selectedParamids.size();
        XYSeriesCollection[] xySeriesColln = new XYSeriesCollection[size];
        this.__XYRepDatasetStacked = new XYSeriesCollection[size][];
        this.__XYRepDatasetStackedTransform = new XYSeriesCollection[size][];
        this.__XYAvgDatasetStacked = new XYSeriesCollection[size];
        this.__XYAvgDatasetStackedTransform = new XYSeriesCollection[size];
        this.__NoDataListAvgStacked = new ArrayList[size];
        this.__NoDataListRepStacked = new ArrayList[size][];
        for (int i = 0; i < size; ++i) {
            StatsList list = (StatsList)this.__ChartingHashmap.get(selectedParamids.get(i));
            int innerSize = list.size();
            this.__XYRepDatasetStacked[i] = new XYSeriesCollection[innerSize];
            this.__XYRepDatasetStackedTransform[i] = new XYSeriesCollection[innerSize];
            this.__NoDataListRepStacked[i] = new ArrayList[innerSize];
            for (int j = 0; j < innerSize; ++j) {
                String xValue;
                String xtemp;
                double valueT;
                double value;
                List statsvaluelist;
                double lastvalueT;
                double lastvalue;
                Stats stats;
                if (j == 0) {
                    stats = (Stats)((Object)list.get(0));
                    XYSeries data = new XYSeries((Comparable)((Object)stats.getYparam()), true);
                    XYSeries dataTransform = new XYSeries((Comparable)((Object)stats.getYparam()), true);
                    ArrayList<String> templist = new ArrayList<String>();
                    lastvalue = 0.0;
                    lastvalueT = 0.0;
                    statsvaluelist = stats.getValuesAsList();
                    List statstransformvaluelist = stats.getAllTransformValues();
                    this.numberOfTickUnits = statsvaluelist.size();
                    for (int k = 0; k < statsvaluelist.size(); ++k) {
                        value = (Double)statsvaluelist.get(k);
                        valueT = (Double)statstransformvaluelist.get(k);
                        xtemp = stats.getX(k);
                        xValue = xtemp.substring(0, xtemp.indexOf("$"));
                        this.__SamplePointLocationMap.put(stats.getYparam() + "|" + xValue, new Integer(k + 1));
                        if (value == 9.0E99) {
                            data.add((double)(k + 1), lastvalue);
                            dataTransform.add((double)(k + 1), lastvalueT);
                            templist.add(Integer.toString(k));
                            continue;
                        }
                        data.add((double)(k + 1), value);
                        dataTransform.add((double)(k + 1), valueT);
                        lastvalue = value;
                        lastvalueT = valueT;
                    }
                    xySeriesColln[i] = new XYSeriesCollection();
                    xySeriesColln[i].addSeries(dataTransform);
                    this.__XYAvgDatasetStacked[i] = new XYSeriesCollection();
                    this.__XYAvgDatasetStacked[i].addSeries(data);
                    this.__XYAvgDatasetStackedTransform[i] = new XYSeriesCollection();
                    this.__XYAvgDatasetStackedTransform[i].addSeries(dataTransform);
                    this.__NoDataListAvgStacked[i] = new ArrayList();
                    this.__NoDataListAvgStacked[i].add(templist);
                    continue;
                }
                if (j <= 0) continue;
                stats = (Stats)((Object)list.get(j));
                ArrayList<String> templist = new ArrayList<String>();
                XYSeries data = new XYSeries((Comparable)((Object)(stats.getYparam() + " Master")), true);
                XYSeries dataTransform = new XYSeries((Comparable)((Object)(stats.getYparam() + " Master")), true);
                lastvalue = 0.0;
                lastvalueT = 0.0;
                statsvaluelist = stats.getValuesAsList();
                List statsvaluelist1 = stats.getAllTransformValues();
                for (int m = 0; m < statsvaluelist.size(); ++m) {
                    value = (Double)statsvaluelist.get(m);
                    valueT = (Double)statsvaluelist1.get(m);
                    xtemp = stats.getX(m);
                    xValue = xtemp.substring(0, xtemp.indexOf("$"));
                    if (value == 9.0E99) {
                        data.add((double)((Integer)this.__SamplePointLocationMap.get(stats.getYparam() + "|" + xValue)).intValue(), lastvalue);
                        dataTransform.add((double)((Integer)this.__SamplePointLocationMap.get(stats.getYparam() + "|" + xValue)).intValue(), lastvalueT);
                        data.setDescription(xValue);
                        templist.add(Integer.toString(m));
                        continue;
                    }
                    data.add((double)((Integer)this.__SamplePointLocationMap.get(stats.getYparam() + "|" + xValue)).intValue(), value);
                    dataTransform.add((double)((Integer)this.__SamplePointLocationMap.get(stats.getYparam() + "|" + xValue)).intValue(), valueT);
                    data.setDescription(xValue);
                    lastvalue = value;
                    lastvalueT = valueT;
                }
                this.__XYRepDatasetStacked[i][j] = new XYSeriesCollection();
                this.__XYRepDatasetStacked[i][j].addSeries(data);
                this.__XYRepDatasetStackedTransform[i][j] = new XYSeriesCollection();
                this.__XYRepDatasetStackedTransform[i][j].addSeries(dataTransform);
                this.__NoDataListRepStacked[i][j] = new ArrayList();
                this.__NoDataListRepStacked[i][j].add(templist);
            }
        }
        return xySeriesColln;
    }

    private void addRangeMarkers(XYPlot plot) {
        for (Marker marker : this.__MarkerCollection) {
            plot.addRangeMarker(marker, Layer.BACKGROUND);
        }
    }

    private void addStackedRangeMarkers(XYPlot plot, int index) {
        for (Marker marker : this.__StackedMarkerCollection[index]) {
            plot.addRangeMarker(marker, Layer.BACKGROUND);
        }
    }

    public void addRangeMarker(double value, String label, Color color) {
        BasicStroke s = new BasicStroke(1.0f);
        ValueMarker marker = new ValueMarker(value, (Paint)color, (Stroke)s);
        marker.setLabel("                              " + label);
        this.__MarkerCollection.add(marker);
    }

    public void addRangeMarker(double value, String label, Color color, float width, String style) {
        BasicStroke s = null;
        if (style.equalsIgnoreCase("Plain Solid")) {
            s = new BasicStroke(width);
        } else if (style.equalsIgnoreCase("Small Interval")) {
            s = new BasicStroke(width, 1, 1, 1.0f, new float[]{5.0f, 3.0f}, 0.0f);
        } else if (style.equalsIgnoreCase("Medium Interval")) {
            s = new BasicStroke(width, 1, 1, 1.0f, new float[]{5.0f, 6.0f}, 0.0f);
        } else if (style.equalsIgnoreCase("Large Interval")) {
            s = new BasicStroke(width, 1, 1, 1.0f, new float[]{5.0f, 10.0f}, 0.0f);
        }
        ValueMarker marker = new ValueMarker(value, (Paint)color, (Stroke)s);
        marker.setLabel(label);
        marker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        marker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
        marker.setLabelOffsetType(LengthAdjustmentType.EXPAND);
        this.__MarkerCollection.add(marker);
    }

    public void addStackedRangeMarker(int index, double value, String label, Color color, float width, String style) {
        BasicStroke s = null;
        if (style.equalsIgnoreCase("Plain Solid")) {
            s = new BasicStroke(width);
        } else if (style.equalsIgnoreCase("Small Interval")) {
            s = new BasicStroke(width, 1, 1, 1.0f, new float[]{5.0f, 3.0f}, 0.0f);
        } else if (style.equalsIgnoreCase("Medium Interval")) {
            s = new BasicStroke(width, 1, 1, 1.0f, new float[]{5.0f, 6.0f}, 0.0f);
        } else if (style.equalsIgnoreCase("Large Interval")) {
            s = new BasicStroke(width, 1, 1, 1.0f, new float[]{5.0f, 10.0f}, 0.0f);
        }
        ValueMarker marker = new ValueMarker(value, (Paint)color, (Stroke)s);
        marker.setLabel(label);
        marker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        marker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
        marker.setLabelOffsetType(LengthAdjustmentType.EXPAND);
        this.__StackedMarkerCollection[index].add(marker);
    }

    public void addIntervalRangeMarker(double value, String label, Color color, float width) {
        BasicStroke s = new BasicStroke(width, 1, 1, 1.0f, new float[]{6.0f, 6.0f}, 0.0f);
        ValueMarker marker = new ValueMarker(value, (Paint)color, (Stroke)s);
        marker.setLabel(label);
        marker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        this.__MarkerCollection.add(marker);
    }

    private void addXYTextAnnotations(XYPlot plot) {
        for (XYTextAnnotation xyTextAnnotation : this.__XYTextAnnotationCollection) {
            plot.addAnnotation((XYAnnotation)xyTextAnnotation);
        }
    }

    public void addXYTextAnnotation(String text, double xValue, double yValue) {
        XYTextAnnotation xyTextAnnotation = new XYTextAnnotation(text, xValue, yValue);
        this.__XYTextAnnotationCollection.add(xyTextAnnotation);
    }

    public String getChartTitle() {
        return this.__ChartTitle;
    }

    public void setChartTitle(String parChartTitle) {
        this.__ChartTitle = parChartTitle;
    }

    public String getxAxisLabel() {
        return this.__XAxisLabel;
    }

    public void setxAxisLabel(String parXAxisLabel) {
        this.__XAxisLabel = parXAxisLabel;
    }

    public String getyAxisLabel() {
        return this.__YAxisLabel;
    }

    public void setyAxisLabel(String parYAxisLabel) {
        this.__YAxisLabel = parYAxisLabel;
    }

    public Color getPlotColor() {
        return this.__PlotColor;
    }

    public void setPlotColor(Color parPlotColor) {
        this.__PlotColor = parPlotColor;
    }

    public boolean isShowinfo() {
        return this.__ShowInfo;
    }

    public void setShowinfo(boolean parShowinfo) {
        this.__ShowInfo = parShowinfo;
    }

    public void addSubtitle(String text, String position, String textalignment) {
        TextTitle subtitle = new TextTitle(text);
        if (position.equals("Top")) {
            subtitle.setPosition(RectangleEdge.TOP);
        } else if (position.equals("Bottom")) {
            subtitle.setPosition(RectangleEdge.BOTTOM);
        } else if (position.equals("Right")) {
            subtitle.setPosition(RectangleEdge.RIGHT);
        } else if (position.equals("Left")) {
            subtitle.setPosition(RectangleEdge.LEFT);
        } else {
            subtitle.setPosition(RectangleEdge.BOTTOM);
        }
        if (position.equals("Top") || position.equals("Bottom")) {
            if (textalignment.equals("Left")) {
                subtitle.setHorizontalAlignment(HorizontalAlignment.LEFT);
            }
            if (textalignment.equals("Center")) {
                subtitle.setHorizontalAlignment(HorizontalAlignment.CENTER);
            }
            if (textalignment.equals("Right")) {
                subtitle.setHorizontalAlignment(HorizontalAlignment.RIGHT);
            }
        } else {
            if (textalignment.equals("Left") && position.equals("Right")) {
                subtitle.setVerticalAlignment(VerticalAlignment.TOP);
            }
            if (textalignment.equals("Left") && position.equals("Left")) {
                subtitle.setVerticalAlignment(VerticalAlignment.BOTTOM);
            }
            if (textalignment.equals("Center") && position.equals("Right") && position.equals("Right")) {
                subtitle.setVerticalAlignment(VerticalAlignment.CENTER);
            }
            if (textalignment.equals("Right") && position.equals("Right")) {
                subtitle.setVerticalAlignment(VerticalAlignment.BOTTOM);
            }
            if (textalignment.equals("Right") && position.equals("Left")) {
                subtitle.setVerticalAlignment(VerticalAlignment.TOP);
            }
        }
        subtitle.setFont(new Font("SansSerif", 0, 12));
        subtitle.setPadding(new RectangleInsets(UnitType.RELATIVE, 0.01, 0.1, 0.1, 0.1));
        this.__SubTitleList.add(subtitle);
    }

    public void addSubtitle(String text, VerticalAlignment verticalAlignment, HorizontalAlignment horizontalAlignment) {
        TextTitle subtitle = new TextTitle(text);
        subtitle.setVerticalAlignment(verticalAlignment);
        subtitle.setHorizontalAlignment(horizontalAlignment);
        subtitle.setFont(new Font("SansSerif", 0, 12));
        subtitle.setPadding(new RectangleInsets(UnitType.RELATIVE, 0.01, 0.1, 0.1, 0.1));
        this.__SubTitleList.add(subtitle);
    }

    public void setBackgroundColor(Color parBackgroundColor) {
        this.__BackgroundColor = parBackgroundColor;
    }

    public void setBackgroundColor1(Color parBackgroundColor1) {
        this.__BackgroundColor1 = parBackgroundColor1;
    }

    public JFreeChart getOverlaidChart(ArrayList selectedParams, Locale locale) {
        return this.createOverlaidXYChart(this.getChartTitle(), selectedParams, locale);
    }

    private JFreeChart createOverlaidXYChart(String chartTitle, ArrayList selectedParams, Locale locale) {
        int numberOfParameters = 0;
        numberOfParameters = selectedParams.size();
        XYDataset dataset = this.getOverlaidXYDataSet(selectedParams);
        JFreeChart chart = ChartFactory.createXYLineChart((String)chartTitle, (String)this.getxAxisLabel(), (String)this.getyAxisLabel(), (XYDataset)dataset, (PlotOrientation)PlotOrientation.VERTICAL, (boolean)true, (boolean)true, (boolean)false);
        XYPlot xyplot = (XYPlot)chart.getPlot();
        XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
        xyLineAndShapeRenderer.setShapesFilled(new Boolean(false));
        xyLineAndShapeRenderer.setCreateEntities(new Boolean(true));
        xyLineAndShapeRenderer.setShapesVisible(new Boolean(true));
        xyLineAndShapeRenderer.setLinesVisible(new Boolean(true));
        NumberAxis rangeaxis = (NumberAxis)xyplot.getRangeAxis();
        NumberAxis domainaxis = (NumberAxis)xyplot.getDomainAxis();
        double plotYmax = 0.0;
        double plotYmin = 0.0;
        double maxVal = this.getYAxisMaxValue();
        double minVal = this.getYAxisMinValue();
        if (this.getYAxisScaleType().equals("A")) {
            plotYmax = this.__Mean + 4.0 * this.__Sd > maxVal ? this.__Mean + 4.0 * this.__Sd + this.getRangeConstant(maxVal, minVal) : maxVal + this.getRangeConstant(maxVal, minVal);
            plotYmin = this.__Mean - 4.0 * this.__Sd < minVal ? this.__Mean - 4.0 * this.__Sd - this.getRangeConstant(maxVal, minVal) : minVal - this.getRangeConstant(maxVal, minVal);
        } else {
            plotYmin = this.getYAxisMinValue();
            plotYmax = this.getYAxisMaxValue();
        }
        if (plotYmin > plotYmax) {
            plotYmin = plotYmax;
        }
        if (!this.getXAxisScaleType().equals("A")) {
            if (this.getXAxisMinValue() > this.getXAxisMaxValue()) {
                this.setXAxisMinValue(this.__XAxisMaxValue);
            }
            domainaxis.setRange(this.getXAxisMinValue(), this.getYmaxRange(this.getXAxisMinValue(), this.getXAxisMaxValue()));
        }
        if (this.__BackgroundColor == null) {
            chart.setBackgroundPaint((Paint)Color.ORANGE);
        } else {
            chart.setBackgroundPaint((Paint)this.__BackgroundColor);
        }
        if (this.__BackgroundColor1 == null) {
            this.__BackgroundColor1 = Color.YELLOW;
        }
        xyplot.setBackgroundPaint((Paint)this.__BackgroundColor1);
        this.addRangeMarkers(xyplot);
        this.addXYTextAnnotations(xyplot);
        NumberFormat currFormat = NumberFormat.getNumberInstance(locale);
        for (int m = 0; m < numberOfParameters; ++m) {
            int j;
            StatsList list = (StatsList)this.__ChartingHashmap.get(selectedParams.get(m));
            Stats stats = (Stats)((Object)list.get(0));
            String param2 = stats.getYparam();
            List evaluationList2 = stats.getAllEvalStatus();
            ArrayList templist1 = (ArrayList)this.__NoDataListAvgOverlaid[m].get(0);
            Shapes drawer = new Shapes(m);
            Shapes drawerAvg = new Shapes(m, "avg");
            int innerSize = list.size();
            for (j = 1; j < innerSize; ++j) {
                Stats stats1 = (Stats)((Object)list.get(j));
                String param = stats1.getYparam();
                List evaluationList = stats1.getAllEvalStatus();
                ArrayList templist2 = (ArrayList)this.__NoDataListRepOverlaid[m][j].get(0);
                for (int l = 0; l < this.__XYRepDatasetOverlaid[m][j].getItemCount(0); ++l) {
                    double yValueT = this.__XYRepDatasetOverlaidTransform[m][j].getYValue(0, l);
                    double xValueT = this.__XYRepDatasetOverlaidTransform[m][j].getXValue(0, l);
                    String xtemp = stats1.getX(l);
                    String keyid1 = xtemp.substring(0, xtemp.indexOf("$"));
                    String displayValue = xtemp.substring(xtemp.indexOf("$") + 1, xtemp.length());
                    String status = (String)evaluationList.get(l);
                    status = status.trim();
                    if (templist2.contains(Integer.toString(l))) continue;
                    XYDrawableAnnotation xydrawableannotation = new XYDrawableAnnotation(xValueT, yValueT, 5.0, 5.0, (Drawable)drawer);
                    XYTextAnnotation xyTextAnnotation = new XYTextAnnotation(displayValue, xValueT + 0.1, yValueT);
                    xyplot.addAnnotation((XYAnnotation)xydrawableannotation);
                    if (status.equalsIgnoreCase("FAIL")) {
                        FailureCircleDrawer failuredrawer = new FailureCircleDrawer();
                        XYDrawableAnnotation xydrawableannotation1 = new XYDrawableAnnotation(xValueT, yValueT, 5.0, 5.0, (Drawable)failuredrawer);
                        xydrawableannotation1.setToolTipText("Failed Replicate Sample:" + keyid1 + " Param:" + param + " Value:" + displayValue);
                        xyplot.addAnnotation((XYAnnotation)xydrawableannotation1);
                        if (!this.showdatapointsvaluesflag) continue;
                        xyplot.addAnnotation((XYAnnotation)xyTextAnnotation);
                        continue;
                    }
                    xydrawableannotation.setToolTipText("Replicate Sample:" + keyid1 + " Param:" + param + " Value:" + displayValue);
                    if (!this.showdatapointsvaluesflag) continue;
                    xyplot.addAnnotation((XYAnnotation)xyTextAnnotation);
                }
            }
            for (j = 0; j < this.__XYAvgDatasetOverlaid[m].getItemCount(0); ++j) {
                double yValueT = this.__XYAvgDatasetOverlaidTransform[m].getYValue(0, j);
                double xValueT = this.__XYAvgDatasetOverlaidTransform[m].getXValue(0, j);
                String xtemp = stats.getX(j);
                String keyid1 = xtemp.substring(0, xtemp.indexOf("$"));
                String displayValue = xtemp.substring(xtemp.indexOf("$") + 1, xtemp.length());
                String status = (String)evaluationList2.get(j);
                status = status.trim();
                if (templist1.contains(Integer.toString(j))) continue;
                XYDrawableAnnotation xydrawableannotation = new XYDrawableAnnotation(xValueT, yValueT, 5.0, 5.0, (Drawable)drawerAvg);
                XYTextAnnotation xyTextAnnotation = new XYTextAnnotation(displayValue, xValueT + 0.1, yValueT);
                xyplot.addAnnotation((XYAnnotation)xydrawableannotation);
                if (status.equalsIgnoreCase("FAIL")) {
                    FailureCircleDrawer failuredrawer = new FailureCircleDrawer();
                    XYDrawableAnnotation xydrawableannotation1 = new XYDrawableAnnotation(xValueT, yValueT, 5.0, 5.0, (Drawable)failuredrawer);
                    xydrawableannotation1.setToolTipText("Failed Average Sample:" + keyid1 + " Param:" + param2 + " Value:" + displayValue);
                    xyplot.addAnnotation((XYAnnotation)xydrawableannotation1);
                    if (!this.showdatapointsvaluesflag) continue;
                    xyplot.addAnnotation((XYAnnotation)xyTextAnnotation);
                    continue;
                }
                xydrawableannotation.setToolTipText("Average Sample:" + keyid1 + " Param:" + param2 + " Value:" + displayValue);
                if (!this.showdatapointsvaluesflag) continue;
                xyplot.addAnnotation((XYAnnotation)xyTextAnnotation);
            }
        }
        rangeaxis.setRange(plotYmin, this.getYmaxRange(plotYmin, plotYmax));
        rangeaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        domainaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        if (this.isShowinfo()) {
            Font infofont = new Font("SansSerif", 0, 12);
            RectangleInsets infospacer = new RectangleInsets(UnitType.RELATIVE, 0.01, 0.1, 0.1, 0.1);
            TextTitle chartinfo = new TextTitle("Chart Info");
            chartinfo.setPosition(RectangleEdge.BOTTOM);
            chartinfo.setHorizontalAlignment(HorizontalAlignment.LEFT);
            TextTitle meantitle = new TextTitle("Center Line ( Mean ) : " + currFormat.format(this.__Mean), infofont, (Paint)Color.black, RectangleEdge.BOTTOM, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, infospacer);
            TextTitle sdtitle = new TextTitle("Standard Deviation : " + currFormat.format(this.__Sd), infofont, (Paint)Color.black, RectangleEdge.BOTTOM, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, infospacer);
            chart.addSubtitle((Title)sdtitle);
            chart.addSubtitle((Title)meantitle);
        }
        for (int i = 0; i < this.__SubTitleList.size(); ++i) {
            chart.addSubtitle((Title)((TextTitle)this.__SubTitleList.get(i)));
        }
        return chart;
    }

    private XYDataset getOverlaidXYDataSet(ArrayList selectedParamids) {
        XYSeriesCollection col = new XYSeriesCollection();
        int size = selectedParamids.size();
        this.__XYRepDatasetOverlaid = new XYSeriesCollection[size][];
        this.__XYRepDatasetOverlaidTransform = new XYSeriesCollection[size][];
        this.__XYAvgDatasetOverlaid = new XYSeriesCollection[size];
        this.__XYAvgDatasetOverlaidTransform = new XYSeriesCollection[size];
        this.__NoDataListAvgOverlaid = new ArrayList[size];
        this.__NoDataListRepOverlaid = new ArrayList[size][];
        for (int i = 0; i < size; ++i) {
            StatsList list = (StatsList)this.__ChartingHashmap.get(selectedParamids.get(i));
            int innerSize = list.size();
            this.__XYRepDatasetOverlaid[i] = new XYSeriesCollection[innerSize];
            this.__XYRepDatasetOverlaidTransform[i] = new XYSeriesCollection[innerSize];
            this.__NoDataListRepOverlaid[i] = new ArrayList[innerSize];
            for (int j = 0; j < innerSize; ++j) {
                String xValue;
                String xtemp;
                double valueT;
                double value;
                List statsvaluelist;
                double lastvalueT;
                double lastvalue;
                Stats stats;
                if (j == 0) {
                    stats = (Stats)((Object)list.get(0));
                    XYSeries data = new XYSeries((Comparable)((Object)stats.getYparam()), true);
                    XYSeries dataTransform = new XYSeries((Comparable)((Object)stats.getYparam()), true);
                    ArrayList<String> templist = new ArrayList<String>();
                    lastvalue = 0.0;
                    lastvalueT = 0.0;
                    statsvaluelist = stats.getValuesAsList();
                    List statstransformvaluelist = stats.getAllTransformValues();
                    for (int k = 0; k < statsvaluelist.size(); ++k) {
                        value = (Double)statsvaluelist.get(k);
                        valueT = (Double)statstransformvaluelist.get(k);
                        xtemp = stats.getX(k);
                        xValue = xtemp.substring(0, xtemp.indexOf("$"));
                        this.__SamplePointLocationMap.put(stats.getYparam() + "|" + xValue, new Integer(k + 1));
                        if (value == 9.0E99) {
                            data.add((double)(k + 1), lastvalue);
                            dataTransform.add((double)(k + 1), lastvalueT);
                            templist.add(Integer.toString(k));
                            continue;
                        }
                        data.add((double)(k + 1), value);
                        dataTransform.add((double)(k + 1), valueT);
                        lastvalue = value;
                        lastvalueT = valueT;
                    }
                    col.addSeries(dataTransform);
                    this.__XYAvgDatasetOverlaid[i] = new XYSeriesCollection();
                    this.__XYAvgDatasetOverlaid[i].addSeries(data);
                    this.__XYAvgDatasetOverlaidTransform[i] = new XYSeriesCollection();
                    this.__XYAvgDatasetOverlaidTransform[i].addSeries(dataTransform);
                    this.__NoDataListAvgOverlaid[i] = new ArrayList();
                    this.__NoDataListAvgOverlaid[i].add(templist);
                    continue;
                }
                if (j <= 0) continue;
                stats = (Stats)((Object)list.get(j));
                ArrayList<String> templist = new ArrayList<String>();
                XYSeries data = new XYSeries((Comparable)((Object)(stats.getYparam() + " Master")), true);
                XYSeries dataTransform = new XYSeries((Comparable)((Object)(stats.getYparam() + " Master")), true);
                lastvalue = 0.0;
                lastvalueT = 0.0;
                statsvaluelist = stats.getValuesAsList();
                List statsvaluelist1 = stats.getAllTransformValues();
                for (int m = 0; m < statsvaluelist.size(); ++m) {
                    value = (Double)statsvaluelist.get(m);
                    valueT = (Double)statsvaluelist1.get(m);
                    xtemp = stats.getX(m);
                    xValue = xtemp.substring(0, xtemp.indexOf("$"));
                    if (value == 9.0E99) {
                        data.add((double)((Integer)this.__SamplePointLocationMap.get(stats.getYparam() + "|" + xValue)).intValue(), lastvalue);
                        dataTransform.add((double)((Integer)this.__SamplePointLocationMap.get(stats.getYparam() + "|" + xValue)).intValue(), lastvalueT);
                        data.setDescription(xValue);
                        templist.add(Integer.toString(m));
                        continue;
                    }
                    data.add((double)((Integer)this.__SamplePointLocationMap.get(stats.getYparam() + "|" + xValue)).intValue(), value);
                    dataTransform.add((double)((Integer)this.__SamplePointLocationMap.get(stats.getYparam() + "|" + xValue)).intValue(), valueT);
                    data.setDescription(xValue);
                    lastvalue = value;
                    lastvalueT = valueT;
                }
                this.__XYRepDatasetOverlaid[i][j] = new XYSeriesCollection();
                this.__XYRepDatasetOverlaid[i][j].addSeries(data);
                this.__XYRepDatasetOverlaidTransform[i][j] = new XYSeriesCollection();
                this.__XYRepDatasetOverlaidTransform[i][j].addSeries(dataTransform);
                this.__NoDataListRepOverlaid[i][j] = new ArrayList();
                this.__NoDataListRepOverlaid[i][j].add(templist);
            }
        }
        return col;
    }

    public void addMean(double mean) {
        this.__Mean = mean;
    }

    public double getMean() {
        return this.__Mean;
    }

    public void addSD(double sd) {
        this.__Sd = sd;
    }

    public double getSD() {
        return this.__Sd;
    }

    public void addUCL(double ucl) {
        this.__Ucl = ucl;
    }

    public void addLCL(double lcl) {
        this.__Lcl = lcl;
    }

    public void addStackedMean(double cl) {
        this.__StackedCL.add(new Double(cl));
    }

    public void addStackedSD(double sd) {
        this.__StackedSD.add(new Double(sd));
    }

    public void setDataPointShowFlag(boolean flag) {
        this.showdatapointsvaluesflag = flag;
    }

    public void setXAxisScaleType(String xAxisScaleType) {
        this.__XAxisScaleType = xAxisScaleType;
    }

    public String getXAxisScaleType() {
        return this.__XAxisScaleType;
    }

    public void setYAxisScaleType(String yAxisScaleType) {
        this.__YAxisScaleType = yAxisScaleType;
    }

    public String getYAxisScaleType() {
        return this.__YAxisScaleType;
    }

    public void setXAxisMinValue(double xAxisMin) {
        this.__XAxisMinValue = xAxisMin;
    }

    public double getXAxisMinValue() {
        return this.__XAxisMinValue;
    }

    public void setXAxisMaxValue(double xAxisMax) {
        this.__XAxisMaxValue = xAxisMax;
    }

    public double getXAxisMaxValue() {
        return this.__XAxisMaxValue;
    }

    public void setYAxisMinValue(double yAxisMin) {
        this.__YAxisMinValue = yAxisMin;
    }

    public double getYAxisMinValue() {
        return this.__YAxisMinValue;
    }

    public void setYAxisMaxValue(double yAxisMax) {
        this.__YAxisMaxValue = yAxisMax;
    }

    public double getYAxisMaxValue() {
        return this.__YAxisMaxValue;
    }

    public void setStackSize(int length) {
        this.__StackSize = length;
    }

    public void setSelectedParams(ArrayList list) {
        this.__SelectedParams = list;
    }

    public JFreeChart getChart() {
        return this.createXYChart(this.getChartTitle(), this.createXYDataset());
    }

    private JFreeChart createXYChart(String title, XYDataset dataset) {
        int i;
        JFreeChart chart = ChartFactory.createXYLineChart((String)title, (String)this.getxAxisLabel(), (String)this.getyAxisLabel(), (XYDataset)dataset, (PlotOrientation)PlotOrientation.VERTICAL, (boolean)true, (boolean)true, (boolean)false);
        Stats stats = (Stats)((Object)this.__StatsList.get(0));
        double plotYmax = 0.0;
        double plotYmin = 0.0;
        if (this.__BackgroundColor == null) {
            chart.setBackgroundPaint((Paint)Color.ORANGE);
        } else {
            chart.setBackgroundPaint((Paint)this.__BackgroundColor);
        }
        XYPlot plot = chart.getXYPlot();
        plot.setRenderer((XYItemRenderer)new StandardXYItemRenderer());
        ValueAxis xAxis = plot.getDomainAxis();
        xAxis.setAutoTickUnitSelection(false);
        NumberAxis rangeaxis = (NumberAxis)plot.getRangeAxis();
        double rangeConstant = this.getRangeConstant(stats.getMax(), stats.getMin());
        plotYmax = Math.max(stats.getUCL(), stats.getMax()) + rangeConstant;
        plotYmin = Math.min(stats.getLCL(), stats.getMin()) - rangeConstant;
        if (plotYmin > plotYmax) {
            plotYmin = plotYmax;
        }
        rangeaxis.setRange(plotYmin, this.getYmaxRange(plotYmin, plotYmax));
        this.addRangeMarkers(plot);
        XYItemRenderer renderer = plot.getRenderer();
        if (renderer instanceof StandardXYItemRenderer) {
            StandardXYItemRenderer rr = (StandardXYItemRenderer)renderer;
            rr.setBaseShapesVisible(true);
            rr.setShapesFilled(true);
            rr.setItemLabelsVisible(true);
        }
        int seriescount = dataset.getSeriesCount();
        for (i = 0; i < seriescount; ++i) {
            renderer.setSeriesPaint(i, (Paint)((Color)this.__PlotColorList.get(i)));
            if (!this.isShowDataPointValue()) continue;
            List templist = (List)this.__NoDataList.get(i);
            for (int j = 0; j < dataset.getItemCount(i); ++j) {
                double yValue = dataset.getYValue(i, j);
                if (!templist.contains(Integer.toString(j))) {
                    plot.addAnnotation((XYAnnotation)new XYTextAnnotation(Double.toString(yValue), (double)(j + 1), yValue + rangeConstant));
                    continue;
                }
                plot.addAnnotation((XYAnnotation)new XYTextAnnotation("*", (double)(j + 1), yValue + rangeConstant));
            }
        }
        for (i = 0; i < this.__SubTitleList.size(); ++i) {
            chart.addSubtitle((Title)((TextTitle)this.__SubTitleList.get(i)));
        }
        if (this.isShowinfo()) {
            Font infofont = new Font("SansSerif", 0, 12);
            Font italicfont = new Font("SansSerif", 2, 12);
            RectangleInsets rectangleInsets = new RectangleInsets(0.01, 0.1, 0.1, 0.1);
            TextTitle chartinfo = new TextTitle("Chart Info");
            chartinfo.setPosition(RectangleEdge.BOTTOM);
            chartinfo.setHorizontalAlignment(HorizontalAlignment.LEFT);
            TextTitle meantitle = new TextTitle("Center Line ( Mean ) : " + stats.getCL(), infofont, (Paint)Color.black, RectangleEdge.BOTTOM, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, rectangleInsets);
            TextTitle sdtitle = new TextTitle("Standard Deviation : " + stats.getSD(), infofont, (Paint)Color.black, RectangleEdge.BOTTOM, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, rectangleInsets);
            TextTitle nodatatitle = new TextTitle("* No Data Found.", italicfont, (Paint)Color.black, RectangleEdge.BOTTOM, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, rectangleInsets);
            chart.addSubtitle((Title)nodatatitle);
            chart.addSubtitle((Title)sdtitle);
            chart.addSubtitle((Title)meantitle);
        }
        return chart;
    }

    private XYDataset createXYDataset() {
        XYSeriesCollection col = new XYSeriesCollection();
        for (int i = 0; i < this.__StatsList.size(); ++i) {
            Stats stats = (Stats)((Object)this.__StatsList.get(i));
            XYSeries data = new XYSeries((Comparable)((Object)stats.getYparam()), true);
            ArrayList<String> templist = new ArrayList<String>();
            double lastvalue = 0.0;
            List statsvaluelist = stats.getValuesAsList();
            for (int j = 0; j < statsvaluelist.size(); ++j) {
                double value = (Double)statsvaluelist.get(j);
                if (value == 9.0E99) {
                    data.add((double)(j + 1), lastvalue);
                    templist.add(Integer.toString(j));
                    continue;
                }
                data.add((double)(j + 1), value);
                lastvalue = value;
            }
            col.addSeries(data);
            this.__NoDataList.add(templist);
        }
        return col;
    }

    public double get__Ucl() {
        return this.__Ucl;
    }

    public double get__Lcl() {
        return this.__Lcl;
    }

    private double getRangeConstant(double max, double min) {
        return (max - min) * 0.04;
    }

    private double getYmaxRange(double plotYmin, double plotYmax) {
        double finalplotYmax = plotYmax;
        if (plotYmax == plotYmin) {
            String text = Double.toString(Math.abs(plotYmax));
            int integerPlaces = text.indexOf(46);
            int decimalPlaces = text.length() - integerPlaces - 1;
            finalplotYmax = Double.sum(plotYmax, 1.0 / Math.pow(10.0, decimalPlaces));
        }
        return finalplotYmax;
    }
}

