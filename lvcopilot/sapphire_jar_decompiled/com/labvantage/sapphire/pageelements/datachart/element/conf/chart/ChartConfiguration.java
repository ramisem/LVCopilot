/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.PaintConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.TitleConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.dataexport.DataExportConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.PlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.ToolbarConfiguration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.util.ConnectionInfo;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public final class ChartConfiguration
implements Serializable {
    private static final String DEFAULT_SHOW_LEGEND = "Y";
    private static final String DEFAULT_ANTI_ALIAS = "Y";
    private static final String DEFAULT_TEXT_ANTI_ALIAS = "Y";
    static final String DEFAULT_FONT_FAMILY = "SansSerif";
    private static final String DEFAULT_HONOR_RTLMODE = "Y";
    private final PlotConfiguration anonymousPlotConf;
    private PlotConfiguration plotConf;
    private final String chartTitleId;
    private final List<String> subtitleIdList;
    private final ToolbarConfiguration toolbarConf;
    private final PaintConfiguration backgroundPaint;
    private final Integer width;
    private final Integer height;
    private final boolean showLegend;
    private final String legendTitleId;
    private final List<TitleConfiguration> titleList;
    private final boolean antiAlias;
    private final boolean textAntiAlias;
    private final String fontFamily;
    private final DataExportConfiguration dataExportConf;
    private final boolean honorRTLmode;
    private Integer clientWidth;
    private Integer clientHeight;

    public ChartConfiguration(PropertyList chartProps, ConnectionInfo connectionInfo) {
        if (chartProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        if (connectionInfo == null) {
            throw new IllegalArgumentException("Connection info is null");
        }
        this.chartTitleId = chartProps.getProperty("charttitleid");
        this.backgroundPaint = new PaintConfiguration(chartProps.getPropertyListNotNull("backgroundpaintprops"));
        String widthString = chartProps.getProperty("width", "");
        this.width = !widthString.isEmpty() ? Integer.valueOf(widthString) : null;
        String heightString = chartProps.getProperty("height", "");
        this.height = !heightString.isEmpty() ? Integer.valueOf(heightString) : null;
        this.anonymousPlotConf = new PlotConfiguration(chartProps.getPropertyListNotNull("plotprops"), connectionInfo, this);
        this.toolbarConf = new ToolbarConfiguration(chartProps.getPropertyListNotNull("toolbarprops"), this);
        this.fontFamily = chartProps.getProperty("fontfamily", DEFAULT_FONT_FAMILY);
        this.clientHeight = null;
        this.clientWidth = null;
        this.subtitleIdList = new ArrayList<String>();
        PropertyListCollection subtitleCollection = chartProps.getCollectionNotNull("subtitlecollection");
        for (int i = 0; i < subtitleCollection.size(); ++i) {
            PropertyList subtitleProps = subtitleCollection.getPropertyList(i);
            this.subtitleIdList.add(subtitleProps.getProperty("titleid"));
        }
        this.honorRTLmode = chartProps.getProperty("honorrtlmode", "Y").toLowerCase().startsWith("y");
        this.titleList = new ArrayList<TitleConfiguration>();
        PropertyListCollection titleCollection = chartProps.getCollectionNotNull("titlecollection");
        for (int i = 0; i < titleCollection.size(); ++i) {
            PropertyList titleProps = titleCollection.getPropertyList(i);
            this.titleList.add(new TitleConfiguration(titleProps, this.honorRTLmode, this));
        }
        this.showLegend = chartProps.getProperty("showlegend", "Y").toLowerCase().startsWith("y");
        this.legendTitleId = chartProps.getProperty("legendtitleid");
        this.antiAlias = chartProps.getProperty("antialias", "Y").toLowerCase().startsWith("y");
        this.textAntiAlias = chartProps.getProperty("textantialias", "Y").toLowerCase().startsWith("y");
        this.dataExportConf = new DataExportConfiguration(chartProps.getPropertyListNotNull("dataexportprops"), this);
    }

    public DataExportConfiguration getDataExportConf() {
        return this.dataExportConf;
    }

    public boolean isTextAntiAlias() {
        return this.textAntiAlias;
    }

    public boolean isAntiAlias() {
        return this.antiAlias;
    }

    public boolean isHonorRTLmode() {
        return this.honorRTLmode;
    }

    public TitleConfiguration getTitleConfiguration(String titleId) {
        if (titleId == null || titleId.isEmpty()) {
            throw new IllegalArgumentException("Title ID is null or empty: " + titleId);
        }
        TitleConfiguration returnConf = null;
        for (TitleConfiguration titleConf : this.titleList) {
            if (!titleConf.getTitleId().equals(titleId)) continue;
            returnConf = titleConf;
            break;
        }
        if (returnConf == null) {
            throw new IllegalArgumentException("Title with given ID not found: " + titleId);
        }
        return returnConf;
    }

    public String getLegendTitleId() {
        return this.legendTitleId;
    }

    public boolean showLegend() {
        return this.showLegend;
    }

    public ToolbarConfiguration getToolbarConfiguration() {
        return this.toolbarConf;
    }

    public Integer getWidth() {
        Integer returnWidth = this.width;
        if (returnWidth == null) {
            returnWidth = this.clientWidth;
        }
        if (returnWidth == null) {
            throw new IllegalStateException("Configured width and client width are null");
        }
        return returnWidth;
    }

    public Integer getHeight() {
        Integer returnHeight = this.height;
        if (returnHeight == null) {
            returnHeight = this.clientHeight;
        }
        if (returnHeight == null) {
            throw new IllegalStateException("Configured height and client height are null");
        }
        return returnHeight;
    }

    public void setClientDimensions(Integer clientWidth, Integer clientHeight) {
        if (clientWidth == null) {
            throw new IllegalArgumentException("Client width is null");
        }
        if (clientHeight == null) {
            throw new IllegalArgumentException("Client height is null");
        }
        this.clientWidth = clientWidth;
        this.clientHeight = clientHeight;
    }

    public PaintConfiguration getBackgroundPaintConfiguration() {
        return this.backgroundPaint;
    }

    public String getChartTitleId() {
        return this.chartTitleId;
    }

    public PlotConfiguration getPlotConfiguration(String plotId) {
        if (plotId == null) {
            throw new IllegalArgumentException("Plot ID is null");
        }
        if (plotId.isEmpty()) {
            throw new IllegalArgumentException("Plot ID is empty");
        }
        if (this.plotConf == null || !this.plotConf.getPlotId().equals(plotId)) {
            this.plotConf = new PlotConfiguration(this.anonymousPlotConf, plotId, this);
        }
        return this.plotConf;
    }

    public List<String> getSubtitleIdList() {
        return this.subtitleIdList;
    }

    public PlotConfiguration getPlotConfiguration() {
        if (this.plotConf == null) {
            throw new IllegalStateException("Plot configuration not available yet");
        }
        return this.plotConf;
    }

    public String getFontFamily() {
        return this.fontFamily;
    }
}

