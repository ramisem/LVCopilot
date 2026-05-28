/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.labels.StandardPieSectionLabelGenerator
 *  org.jfree.chart.plot.DrawingSupplier
 *  org.jfree.chart.plot.PiePlot
 *  org.jfree.chart.plot.PiePlot3D
 *  org.jfree.chart.urls.PieURLGenerator
 *  org.jfree.data.general.PieDataset
 */
package com.labvantage.sapphire.pageelements.datachart.chart.plot;

import com.labvantage.sapphire.pageelements.datachart.chart.plot.ConfigurableDrawingSupplier;
import com.labvantage.sapphire.pageelements.datachart.chart.urls.URLGenerator;
import com.labvantage.sapphire.pageelements.datachart.data.Data;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.PiePlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.SectionConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator.URLGeneratorConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.data.general.PieDataset;
import sapphire.util.ConnectionInfo;

public final class PiePlotBuilder {
    private final Data data;
    private final PiePlot piePlot;
    private final Map<String, URLGenerator> urlGeneratorMap;

    public PiePlotBuilder(PiePlotConfiguration piePlotConf, Data data, ChartBindingMap chartBindingMap, ConnectionInfo connectionInfo) {
        if (piePlotConf == null) {
            throw new IllegalArgumentException("Source configuration is null");
        }
        if (data == null) {
            throw new IllegalArgumentException("Data is null");
        }
        if (chartBindingMap == null) {
            throw new IllegalArgumentException("Binding map is null");
        }
        this.data = data;
        this.urlGeneratorMap = new HashMap<String, URLGenerator>();
        this.piePlot = piePlotConf.isUse3d() ? new PiePlot3D() : new PiePlot();
        if (!data.isEmpty()) {
            for (URLGeneratorConfiguration urlGeneratorConf : piePlotConf.getParent().getURLGeneratorConfigurationList()) {
                URLGenerator urlGenerator = new URLGenerator(urlGeneratorConf, data, connectionInfo.getConnectionId(), chartBindingMap);
                this.urlGeneratorMap.put(urlGeneratorConf.getUrlGeneratorId(), urlGenerator);
            }
            this.piePlot.setDrawingSupplier((DrawingSupplier)new ConfigurableDrawingSupplier(piePlotConf.getParent().getDrawingSupplierConfiguration()));
            if (!piePlotConf.getParent().hasDrawingSupplier()) {
                piePlotConf.getParent().setDrawingSupplier(this.piePlot.getDrawingSupplier());
            }
            this.piePlot.setForegroundAlpha(piePlotConf.getParent().getDrawingSupplierConfiguration().getForegroundAlpha());
            this.configurePiePlot(piePlotConf, chartBindingMap);
        } else {
            this.piePlot.setNoDataMessage(piePlotConf.getParent().getNoDataMessage());
        }
    }

    private void configurePiePlot(PiePlotConfiguration piePlotConf, ChartBindingMap chartBindingMap) {
        if (this.data.getTraceableSeriesGroupCount() > 1) {
            throw new IllegalArgumentException("Data contains more than one series group for pie plot, series group count: " + this.data.getTraceableSeriesGroupCount());
        }
        String urlGeneratorId = piePlotConf.getUrlGeneratorId();
        if (!urlGeneratorId.isEmpty()) {
            URLGenerator urlGenerator = this.urlGeneratorMap.get(urlGeneratorId);
            this.piePlot.setURLGenerator((PieURLGenerator)urlGenerator);
        }
        this.piePlot.setDataset((PieDataset)this.data.getTraceableSeriesGroup(0).getSeriesGroup());
        for (SectionConfiguration sectionConfiguration : piePlotConf.getSectionConfigurationList()) {
            StringExpression sectionIdExpression = sectionConfiguration.getSectionId();
            String sectionId = sectionIdExpression.evaluateNoException(chartBindingMap);
            if (sectionId == null || sectionId.isEmpty()) continue;
            if (sectionConfiguration.getSectionPaintConfiguration().hasPaint()) {
                this.piePlot.setSectionPaint((Comparable)((Object)sectionId), sectionConfiguration.getSectionPaintConfiguration().getPaint());
            }
            if (sectionConfiguration.getSectionOutLinePaintConfiguration().hasPaint()) {
                this.piePlot.setSectionOutlinePaint((Comparable)((Object)sectionId), sectionConfiguration.getSectionOutLinePaintConfiguration().getPaint());
            }
            if (this.piePlot instanceof PiePlot3D && sectionConfiguration.getSectionExplodePercent() != null) continue;
            this.piePlot.setExplodePercent((Comparable)((Object)sectionId), sectionConfiguration.getSectionExplodePercent().doubleValue());
        }
        if (this.piePlot instanceof PiePlot3D) {
            PiePlot3D piePlot3D = (PiePlot3D)this.piePlot;
            if (piePlotConf.getDepthFactor() != null) {
                piePlot3D.setDepthFactor(piePlotConf.getDepthFactor().doubleValue());
            }
            piePlot3D.setDarkerSides(piePlotConf.isUseDarkerSides());
        }
        StandardPieSectionLabelGenerator standardPieSectionLabelGenerator = null;
        if (piePlotConf.getSectionLabelConfiguration().isLabelsVisible() && piePlotConf.getSectionLabelConfiguration().getLabelType().equals("Standard")) {
            standardPieSectionLabelGenerator = new StandardPieSectionLabelGenerator(piePlotConf.getSectionLabelConfiguration().getStandardSectionItemLabelConfiguration().getLabelString(), (NumberFormat)new DecimalFormat(piePlotConf.getSectionLabelConfiguration().getStandardSectionItemLabelConfiguration().getNumberFormat()), (NumberFormat)new DecimalFormat(piePlotConf.getSectionLabelConfiguration().getStandardSectionItemLabelConfiguration().getPercentageNumberFormat()));
            this.piePlot.setLabelFont(piePlotConf.getSectionLabelConfiguration().getFontConf().getFont(this.piePlot.getLabelFont(), this.piePlot.getLabelPaint()));
        }
        this.piePlot.setLabelBackgroundPaint(piePlotConf.getSectionLabelConfiguration().getBackgroundColor().getPaint());
        if (!piePlotConf.isShowShadows()) {
            this.piePlot.setShadowPaint(null);
        }
        this.piePlot.setLabelGenerator(standardPieSectionLabelGenerator);
    }

    public PiePlot getPlot() {
        return this.piePlot;
    }
}

