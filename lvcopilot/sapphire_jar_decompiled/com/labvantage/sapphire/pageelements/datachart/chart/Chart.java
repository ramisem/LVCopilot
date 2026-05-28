/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.JFreeChart
 *  org.jfree.chart.LegendItemSource
 *  org.jfree.chart.block.BlockFrame
 *  org.jfree.chart.block.LineBorder
 *  org.jfree.chart.plot.Plot
 *  org.jfree.chart.plot.XYPlot
 *  org.jfree.chart.title.LegendTitle
 *  org.jfree.chart.title.TextTitle
 *  org.jfree.chart.title.Title
 *  org.jfree.ui.HorizontalAlignment
 *  org.jfree.ui.RectangleEdge
 *  org.jfree.ui.RectangleInsets
 */
package com.labvantage.sapphire.pageelements.datachart.chart;

import com.labvantage.sapphire.pageelements.datachart.chart.ConfigurableComponentLegendItemSource;
import com.labvantage.sapphire.pageelements.datachart.chart.plot.CategoryPlotBuilder;
import com.labvantage.sapphire.pageelements.datachart.chart.plot.CombinedDomainCategoryPlotBuilder;
import com.labvantage.sapphire.pageelements.datachart.chart.plot.CombinedDomainXYPlotBuilder;
import com.labvantage.sapphire.pageelements.datachart.chart.plot.PiePlotBuilder;
import com.labvantage.sapphire.pageelements.datachart.chart.plot.XYPlotBuilder;
import com.labvantage.sapphire.pageelements.datachart.chart.toolbar.Toolbar;
import com.labvantage.sapphire.pageelements.datachart.data.Data;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.ChartConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.LegendTitleConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.PaintConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.TextTitleConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.TitleConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.AbstractPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.CategoryPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.CombinedDomainCategoryPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.CombinedDomainXYPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.ComponentPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.PiePlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.PlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.XYPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.block.BlockFrame;
import org.jfree.chart.block.LineBorder;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import sapphire.SapphireException;
import sapphire.util.ConnectionInfo;
import sapphire.xml.PropertyList;

public final class Chart
implements Serializable {
    private final ChartConfiguration chartConf;
    private final JFreeChart jFreeChart;
    private final Toolbar toolbar;

    public Chart(ChartConfiguration chartConf, Data data, ChartBindingMap chartBindingMap, ConnectionInfo connectionInfo, PropertyList elements) throws SapphireException {
        String plotId;
        if (chartConf == null) {
            throw new IllegalArgumentException("Chart configuration is null");
        }
        if (data == null) {
            throw new IllegalArgumentException("Data is null");
        }
        if (chartBindingMap == null) {
            throw new IllegalArgumentException("Binding map is null");
        }
        if (elements == null) {
            throw new IllegalArgumentException("Elements is null");
        }
        this.chartConf = chartConf;
        this.toolbar = new Toolbar(chartConf.getToolbarConfiguration());
        Set<String> plotIdSet = data.getPlotIdSet();
        if (plotIdSet.size() > 1) {
            StringBuilder rootPlotId = new StringBuilder();
            for (String subplotId : data.getPlotIdSet()) {
                rootPlotId.append(";").append(subplotId);
            }
            plotId = rootPlotId.substring(1);
        } else {
            plotId = data.getPlotType() != PlotConfiguration.PlotType.NO_DATA_PLOT ? plotIdSet.iterator().next() : PlotConfiguration.PlotType.NO_DATA_PLOT.getName();
        }
        PlotConfiguration plotConf = chartConf.getPlotConfiguration(plotId);
        String elementId = plotConf.getElementId().evaluateNoException(chartBindingMap);
        PlotConfiguration.PlotType plotType = !plotConf.isPlotCreatedFromElement(plotId, elementId) ? plotConf.createPlotFromElement(elements.getPropertyListNotNull(elementId), elementId, connectionInfo) : plotConf.getPlotType();
        this.jFreeChart = new JFreeChart(this.createPlot(data, chartBindingMap, connectionInfo, elements, plotConf));
        Plot plot = this.jFreeChart.getPlot();
        PaintConfiguration backgroundPaintConf = plotConf.getBackgroundPaintConfiguration();
        plot.setBackgroundPaint(backgroundPaintConf.getOrSetPaint(Plot.DEFAULT_BACKGROUND_PAINT));
        plot.setOutlinePaint(plotConf.getOutlinePaintConf().getOrSetPaint(Plot.DEFAULT_OUTLINE_PAINT));
        plot.setOutlineVisible(plotConf.isOutlineVisible());
        this.jFreeChart.setBackgroundPaint(chartConf.getBackgroundPaintConfiguration().getOrSetPaint(JFreeChart.DEFAULT_BACKGROUND_PAINT));
        this.jFreeChart.setTextAntiAlias(chartConf.isTextAntiAlias());
        this.jFreeChart.setAntiAlias(chartConf.isAntiAlias());
        if (!data.isEmpty()) {
            TitleConfiguration titleConf;
            String chartTitleId = chartConf.getChartTitleId();
            if (!chartTitleId.isEmpty() && (titleConf = chartConf.getTitleConfiguration(chartTitleId)).getTitleType() == TitleConfiguration.TitleType.TEXT) {
                TextTitleConfiguration textTitleConf = titleConf.getTextTitleConfiguration();
                TextTitle textTitle = Chart.createTextTitle(textTitleConf, chartBindingMap, Color.BLACK, JFreeChart.DEFAULT_TITLE_FONT);
                Chart.buildTitle((Title)textTitle, titleConf, chartBindingMap, connectionInfo);
                this.jFreeChart.setTitle(textTitle);
            }
            this.addSubtitles(chartBindingMap, connectionInfo);
            AbstractPlotConfiguration componentPlotConf = null;
            if (plotType == PlotConfiguration.PlotType.XY_PLOT) {
                componentPlotConf = plotConf.getXYPlotConfiguration();
            } else if (plotType == PlotConfiguration.PlotType.CATEGORY_PLOT) {
                componentPlotConf = plotConf.getCategoryPlotConfiguration();
            } else if (plotType == PlotConfiguration.PlotType.COMBINED_DOMAIN_XY_PLOT) {
                componentPlotConf = plotConf.getCombinedDomainXYPlotConfiguration();
            } else if (plotType == PlotConfiguration.PlotType.COMBINED_DOMAIN_CATEGORY_PLOT) {
                componentPlotConf = plotConf.getCombinedDomainCategoryPlotConfiguration();
            }
            if (componentPlotConf != null) {
                TitleConfiguration legendTitleConf;
                String legendTitleId = chartConf.getLegendTitleId();
                LegendTitle legendTitle = null;
                if (!legendTitleId.isEmpty() && (legendTitleConf = chartConf.getTitleConfiguration(legendTitleId)).getTitleType() == TitleConfiguration.TitleType.LEGEND) {
                    legendTitle = Chart.createLegendTitle(legendTitleConf.getLegendTitleConfiguration(), this.jFreeChart.getPlot(), componentPlotConf, data, chartBindingMap, connectionInfo, legendTitleConf.getHonorRTLmode());
                    Chart.buildTitle((Title)legendTitle, legendTitleConf, chartBindingMap, connectionInfo);
                }
                if (legendTitle == null) {
                    legendTitle = this.createDefaultLegendTitle(this.jFreeChart.getPlot(), (ComponentPlotConfiguration)((Object)componentPlotConf), data, chartBindingMap, connectionInfo, chartConf.isHonorRTLmode());
                }
                if (chartConf.isHonorRTLmode() && connectionInfo.isRtl() && legendTitle.getPosition() == RectangleEdge.RIGHT) {
                    legendTitle.setPosition(RectangleEdge.LEFT);
                }
                this.jFreeChart.removeLegend();
                this.jFreeChart.addLegend(legendTitle);
            }
            if (!chartConf.showLegend()) {
                this.jFreeChart.removeLegend();
            }
        }
    }

    public static Title createChartTitle(TitleConfiguration titleConf, ChartBindingMap chartBindingMap, ConnectionInfo connectionInfo) throws SapphireException {
        if (titleConf == null) {
            throw new IllegalArgumentException("Title conf is null");
        }
        if (chartBindingMap == null) {
            throw new IllegalArgumentException("Chart binding map is null");
        }
        return Chart.createAnyTitle(titleConf, chartBindingMap, null, null, null, connectionInfo);
    }

    public static Title createTitle(TitleConfiguration titleConf, ChartBindingMap chartBindingMap, Plot plot, ComponentPlotConfiguration componentPlotConf, Data data, ConnectionInfo connectionInfo) throws SapphireException {
        if (titleConf == null) {
            throw new IllegalArgumentException("Title conf is null");
        }
        if (chartBindingMap == null) {
            throw new IllegalArgumentException("Chart binding map is null");
        }
        if (plot == null) {
            throw new IllegalArgumentException("Plot is null");
        }
        if (componentPlotConf == null) {
            throw new IllegalArgumentException("Component plot conf is null");
        }
        if (data == null) {
            throw new IllegalArgumentException("Data is null");
        }
        return Chart.createAnyTitle(titleConf, chartBindingMap, plot, componentPlotConf, data, connectionInfo);
    }

    private static Title createAnyTitle(TitleConfiguration titleConf, ChartBindingMap chartBindingMap, Plot plot, ComponentPlotConfiguration componentPlotConf, Data data, ConnectionInfo connectionInfo) throws SapphireException {
        TextTitle title;
        TitleConfiguration.TitleType titleType = titleConf.getTitleType();
        if (titleType == TitleConfiguration.TitleType.TEXT) {
            title = Chart.createTextTitle(titleConf.getTextTitleConfiguration(), chartBindingMap, TextTitle.DEFAULT_TEXT_PAINT, TextTitle.DEFAULT_FONT);
        } else if (titleType == TitleConfiguration.TitleType.LEGEND && plot != null && componentPlotConf != null && data != null) {
            title = Chart.createLegendTitle(titleConf.getLegendTitleConfiguration(), plot, componentPlotConf, data, chartBindingMap, connectionInfo, titleConf.getHonorRTLmode());
        } else {
            throw new IllegalArgumentException("Unknown title type: " + (Object)((Object)titleType));
        }
        Chart.buildTitle((Title)title, titleConf, chartBindingMap, connectionInfo);
        return title;
    }

    private static void buildTitle(Title title, TitleConfiguration titleConf, ChartBindingMap chartBindingMap, ConnectionInfo connectionInfo) throws SapphireException {
        title.setVisible(titleConf.getVisible().evaluate(chartBindingMap).booleanValue());
        title.setHorizontalAlignment(titleConf.getHorizontalAlignment());
        title.setVerticalAlignment(titleConf.getVerticalAlignment());
        if (titleConf.getHonorRTLmode() && connectionInfo.isRtl()) {
            if (titleConf.getPosition() == RectangleEdge.LEFT) {
                title.setPosition(RectangleEdge.RIGHT);
            } else if (titleConf.getPosition() == RectangleEdge.RIGHT) {
                title.setPosition(RectangleEdge.LEFT);
            } else {
                title.setPosition(titleConf.getPosition());
            }
            if (titleConf.getPosition() == RectangleEdge.BOTTOM || titleConf.getPosition() == RectangleEdge.TOP) {
                if (titleConf.getHorizontalAlignment() == HorizontalAlignment.LEFT) {
                    title.setHorizontalAlignment(HorizontalAlignment.RIGHT);
                } else if (titleConf.getHorizontalAlignment() == HorizontalAlignment.RIGHT) {
                    title.setHorizontalAlignment(HorizontalAlignment.LEFT);
                }
            }
            title.setPadding(titleConf.getPaddingTop(), titleConf.getPaddingRight(), titleConf.getPaddingBottom(), titleConf.getPaddingLeft());
        } else {
            title.setPosition(titleConf.getPosition());
            title.setPadding(titleConf.getPaddingTop(), titleConf.getPaddingLeft(), titleConf.getPaddingBottom(), titleConf.getPaddingRight());
        }
        if (titleConf.showBorder()) {
            title.setFrame((BlockFrame)new LineBorder());
        }
    }

    private static TextTitle createTextTitle(TextTitleConfiguration textTitleConf, ChartBindingMap chartBindingMap, Paint defaultPaint, Font defaultFont) {
        TextTitle textTitle = new TextTitle();
        String title = textTitleConf.getText().evaluateNoException(chartBindingMap);
        textTitle.setText(title.isEmpty() ? " " : title);
        textTitle.setPaint(textTitleConf.getPaintConfiguration().getOrSetPaint(defaultPaint));
        textTitle.setFont(textTitleConf.getFontConfiguration().getFont(defaultFont, Color.BLACK));
        return textTitle;
    }

    public static LegendTitle createLegendTitle(LegendTitleConfiguration legendTitleConf, Plot plot, ComponentPlotConfiguration componentPlotConf, Data data, ChartBindingMap chartBindingMap, ConnectionInfo connectionInfo, boolean chartHonorRTLmode) {
        ConfigurableComponentLegendItemSource legendItemSource = new ConfigurableComponentLegendItemSource((LegendItemSource)plot, componentPlotConf, data, chartBindingMap, connectionInfo.getConnectionId());
        LegendTitle legendTitle = new LegendTitle((LegendItemSource)legendItemSource);
        if (chartHonorRTLmode && connectionInfo.isRtl()) {
            legendTitle.setLegendItemGraphicEdge(RectangleEdge.RIGHT);
            legendTitle.setHorizontalAlignment(HorizontalAlignment.RIGHT);
        }
        legendTitle.setBackgroundPaint(legendTitleConf.getBackgroundPaint().getOrSetPaint(Color.WHITE));
        return legendTitle;
    }

    public Toolbar getToolbar() {
        return this.toolbar;
    }

    private LegendTitle createDefaultLegendTitle(Plot plot, ComponentPlotConfiguration componentPlotConf, Data data, ChartBindingMap chartBindingMap, ConnectionInfo connectionInfo, boolean chartHonorRTLmode) {
        ConfigurableComponentLegendItemSource legendItemSource = new ConfigurableComponentLegendItemSource((LegendItemSource)plot, componentPlotConf, data, chartBindingMap, connectionInfo.getConnectionId());
        LegendTitle legend = new LegendTitle((LegendItemSource)legendItemSource);
        if (chartHonorRTLmode && connectionInfo.isRtl()) {
            legend.setLegendItemGraphicEdge(RectangleEdge.RIGHT);
            legend.setHorizontalAlignment(HorizontalAlignment.RIGHT);
        }
        legend.setLegendItemGraphicEdge(RectangleEdge.RIGHT);
        legend.setMargin(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
        legend.setFrame((BlockFrame)new LineBorder());
        legend.setBackgroundPaint((Paint)Color.white);
        legend.setPosition(RectangleEdge.BOTTOM);
        return legend;
    }

    public ChartConfiguration getChartConfiguration() {
        return this.chartConf;
    }

    private void addSubtitles(ChartBindingMap chartBindingMap, ConnectionInfo connectionInfo) throws SapphireException {
        List<String> subtitleIdList = this.chartConf.getSubtitleIdList();
        for (String subtitleId : subtitleIdList) {
            if (subtitleId.isEmpty()) continue;
            TitleConfiguration titleConf = this.chartConf.getTitleConfiguration(subtitleId);
            Title title = Chart.createChartTitle(titleConf, chartBindingMap, connectionInfo);
            this.jFreeChart.addSubtitle(title);
            titleConf.setIndex(this.jFreeChart.getSubtitles().indexOf(title));
        }
    }

    private Plot createPlot(Data data, ChartBindingMap chartBindingMap, ConnectionInfo connectionInfo, PropertyList elements, PlotConfiguration plotConf) throws SapphireException {
        AbstractPlotConfiguration toolbarPlotConf;
        XYPlot plot;
        PlotConfiguration.PlotType chartPlotType = plotConf.getPlotType();
        PlotConfiguration.PlotType dataPlotType = data.getPlotType();
        if (dataPlotType != chartPlotType && dataPlotType != chartPlotType.getSubplotType() && dataPlotType != PlotConfiguration.PlotType.NO_DATA_PLOT) {
            throw new IllegalStateException("Chart plot type " + (Object)((Object)chartPlotType) + " does not match data plot type " + (Object)((Object)dataPlotType));
        }
        if (chartPlotType == PlotConfiguration.PlotType.XY_PLOT) {
            XYPlotConfiguration xyPlotConf = plotConf.getXYPlotConfiguration();
            XYPlotBuilder xyPlotBuilder = new XYPlotBuilder(xyPlotConf, data, chartBindingMap, connectionInfo, this.chartConf.isHonorRTLmode());
            plot = xyPlotBuilder.getPlot();
            toolbarPlotConf = xyPlotConf;
        } else if (chartPlotType == PlotConfiguration.PlotType.CATEGORY_PLOT) {
            CategoryPlotConfiguration categoryPlotConf = plotConf.getCategoryPlotConfiguration();
            CategoryPlotBuilder categoryPlotBuilder = new CategoryPlotBuilder(categoryPlotConf, data, chartBindingMap, connectionInfo, this.chartConf.isHonorRTLmode());
            plot = categoryPlotBuilder.getPlot();
            toolbarPlotConf = categoryPlotConf;
        } else if (chartPlotType == PlotConfiguration.PlotType.PIE_PLOT) {
            PiePlotConfiguration piePlotConf = plotConf.getPiePlotConfiguration();
            PiePlotBuilder piePlotBuilder = new PiePlotBuilder(piePlotConf, data, chartBindingMap, connectionInfo);
            plot = piePlotBuilder.getPlot();
            toolbarPlotConf = piePlotConf;
        } else if (chartPlotType == PlotConfiguration.PlotType.COMBINED_DOMAIN_XY_PLOT) {
            CombinedDomainXYPlotConfiguration combinedDomainXYPlotConf = plotConf.getCombinedDomainXYPlotConfiguration();
            CombinedDomainXYPlotBuilder combinedDomainXYPlotBuilder = new CombinedDomainXYPlotBuilder(combinedDomainXYPlotConf, data, chartBindingMap, connectionInfo, elements, this.chartConf.isHonorRTLmode());
            plot = combinedDomainXYPlotBuilder.getPlot();
            toolbarPlotConf = combinedDomainXYPlotConf;
        } else if (chartPlotType == PlotConfiguration.PlotType.COMBINED_DOMAIN_CATEGORY_PLOT) {
            CombinedDomainCategoryPlotConfiguration combinedDomainCategoryPlotConf = plotConf.getCombinedDomainCategoryPlotConfiguration();
            CombinedDomainCategoryPlotBuilder combinedDomainCategoryPlotBuilder = new CombinedDomainCategoryPlotBuilder(combinedDomainCategoryPlotConf, data, chartBindingMap, connectionInfo, elements, this.chartConf.isHonorRTLmode());
            plot = combinedDomainCategoryPlotBuilder.getPlot();
            toolbarPlotConf = combinedDomainCategoryPlotConf;
        } else {
            throw new IllegalArgumentException("Unknown plot type: " + (Object)((Object)chartPlotType));
        }
        if (toolbarPlotConf == null) {
            throw new IllegalStateException("Plot toolbar configuration is null");
        }
        this.toolbar.setButtonVisibility(toolbarPlotConf, chartBindingMap);
        return plot;
    }

    public JFreeChart getJFreeChart() {
        return this.jFreeChart;
    }
}

