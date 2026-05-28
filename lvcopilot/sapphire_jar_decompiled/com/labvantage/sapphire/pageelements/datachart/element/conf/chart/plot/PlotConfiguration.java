/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.plot.DrawingSupplier
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot;

import com.labvantage.sapphire.pageelements.datachart.element.conf.ConfigurationListItem;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.ChartConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.PaintConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.annotation.AnnotationConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.CategoryPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.CombinedDomainCategoryPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.CombinedDomainXYPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.DrawingSupplierConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.PiePlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.XYPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator.URLGeneratorConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.plot.DrawingSupplier;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public final class PlotConfiguration
implements ConfigurationListItem,
Serializable {
    private static final String DEFAULT_NO_DATA_MESSAGE = "No data.";
    private static final String DEFAULT_OUTLINE_VISIBLE = "Y";
    private static final String DEFAULT_PLOT_ID = "";
    private PlotType plotType;
    private XYPlotConfiguration xyPlotConf;
    private CategoryPlotConfiguration categoryPlotConf;
    private PiePlotConfiguration piePlotConf;
    private CombinedDomainXYPlotConfiguration combinedDomainXYPlotConf;
    private CombinedDomainCategoryPlotConfiguration combinedDomainCategoryPlotConf;
    private final ChartConfiguration parent;
    private final String noDataMessage;
    private final DrawingSupplierConfiguration drawingSupplierConf;
    private final List<AnnotationConfiguration> annotationConfList;
    private final PaintConfiguration backgroundPaintConf;
    private final List<URLGeneratorConfiguration> urlGeneratorConfList;
    private final PaintConfiguration outlinePaintConf;
    private final boolean outlineVisible;
    private DrawingSupplier drawingSupplier;
    private final String plotId;
    private String usedElementId;
    private final StringExpression elementId;

    public String getPlotId() {
        return this.plotId;
    }

    public PlotConfiguration(PlotConfiguration copy, String plotId, ChartConfiguration parent) {
        if (copy == null) {
            throw new IllegalArgumentException("Source is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (plotId == null) {
            throw new IllegalArgumentException("Plot ID is null");
        }
        this.plotId = plotId;
        this.elementId = new StringExpression(copy.elementId);
        this.plotType = copy.plotType;
        this.xyPlotConf = copy.xyPlotConf != null ? new XYPlotConfiguration(copy.xyPlotConf, this) : null;
        this.categoryPlotConf = copy.categoryPlotConf != null ? new CategoryPlotConfiguration(copy.categoryPlotConf, this) : null;
        this.combinedDomainXYPlotConf = copy.combinedDomainXYPlotConf != null ? new CombinedDomainXYPlotConfiguration(copy.combinedDomainXYPlotConf, this) : null;
        this.piePlotConf = copy.piePlotConf != null ? new PiePlotConfiguration(copy.piePlotConf, this) : null;
        this.noDataMessage = copy.noDataMessage;
        this.drawingSupplierConf = new DrawingSupplierConfiguration(copy.drawingSupplierConf, this);
        this.annotationConfList = new ArrayList<AnnotationConfiguration>();
        for (AnnotationConfiguration annotationConf : copy.annotationConfList) {
            this.annotationConfList.add(new AnnotationConfiguration(annotationConf));
        }
        this.urlGeneratorConfList = new ArrayList<URLGeneratorConfiguration>();
        for (URLGeneratorConfiguration urlGeneratorConf : copy.urlGeneratorConfList) {
            this.urlGeneratorConfList.add(new URLGeneratorConfiguration(urlGeneratorConf, this));
        }
        this.backgroundPaintConf = new PaintConfiguration(copy.backgroundPaintConf);
        this.outlinePaintConf = new PaintConfiguration(copy.outlinePaintConf);
        this.outlineVisible = copy.outlineVisible;
        this.parent = parent;
        this.usedElementId = null;
    }

    public PlotConfiguration(PropertyList plotProps, ConnectionInfo connectionInfo, ChartConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (plotProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        if (connectionInfo == null) {
            throw new IllegalArgumentException("Connection info is null");
        }
        this.parent = parent;
        this.backgroundPaintConf = new PaintConfiguration(plotProps.getPropertyListNotNull("backgroundpaintprops"));
        this.annotationConfList = new ArrayList<AnnotationConfiguration>();
        PropertyListCollection annotationCollection = plotProps.getCollectionNotNull("annotationcollection");
        for (int i = 0; i < annotationCollection.size(); ++i) {
            PropertyList annotationProps = annotationCollection.getPropertyList(i);
            this.annotationConfList.add(new AnnotationConfiguration(annotationProps));
        }
        this.urlGeneratorConfList = new ArrayList<URLGeneratorConfiguration>();
        PropertyListCollection urlGeneratorCollection = plotProps.getCollectionNotNull("urlgeneratorcollection");
        for (int i = 0; i < urlGeneratorCollection.size(); ++i) {
            PropertyList urlGeneratorProps = urlGeneratorCollection.getPropertyList(i);
            this.urlGeneratorConfList.add(new URLGeneratorConfiguration(urlGeneratorProps, this));
        }
        this.drawingSupplierConf = new DrawingSupplierConfiguration(plotProps.getPropertyListNotNull("drawingsupplierprops"), this);
        this.noDataMessage = plotProps.getProperty("nodatamessage", new TranslationProcessor(connectionInfo.getConnectionId()).translate(DEFAULT_NO_DATA_MESSAGE));
        this.outlinePaintConf = new PaintConfiguration(plotProps.getPropertyListNotNull("outlinepaintprops"));
        this.outlineVisible = plotProps.getProperty("outlinevisible", DEFAULT_OUTLINE_VISIBLE).toLowerCase().startsWith("y");
        this.plotId = plotProps.getProperty("plotid", DEFAULT_PLOT_ID);
        this.elementId = new StringExpression(plotProps.getProperty("elementid"));
        this.drawingSupplier = null;
        this.plotType = null;
        this.usedElementId = null;
    }

    public StringExpression getElementId() {
        return this.elementId;
    }

    public boolean isOutlineVisible() {
        return this.outlineVisible;
    }

    public PaintConfiguration getOutlinePaintConf() {
        return this.outlinePaintConf;
    }

    public PaintConfiguration getBackgroundPaintConfiguration() {
        return this.backgroundPaintConf;
    }

    public AnnotationConfiguration getAnnotationConfiguration(String annotationId) {
        if (annotationId == null) {
            throw new IllegalArgumentException("Annotation ID is null");
        }
        for (AnnotationConfiguration annotationConf : this.annotationConfList) {
            if (!annotationId.equals(annotationConf.getAnnotationId())) continue;
            return annotationConf;
        }
        throw new IllegalArgumentException("Annotation configuration for given ID not found: " + annotationId);
    }

    public DrawingSupplierConfiguration getDrawingSupplierConfiguration() {
        return this.drawingSupplierConf;
    }

    public List<URLGeneratorConfiguration> getURLGeneratorConfigurationList() {
        return this.urlGeneratorConfList;
    }

    public void addURLGeneratorConfigurationList(URLGeneratorConfiguration urlGeneratorConf) {
        if (urlGeneratorConf == null) {
            throw new IllegalArgumentException("URL generator configuration is null");
        }
        this.urlGeneratorConfList.add(urlGeneratorConf);
    }

    public void clearURLGeneratorConfigurationList() {
        this.urlGeneratorConfList.clear();
    }

    public DrawingSupplier getDrawingSupplier() {
        if (this.drawingSupplier == null) {
            throw new IllegalStateException("Drawing supplier not set yet");
        }
        return this.drawingSupplier;
    }

    public void setDrawingSupplier(DrawingSupplier drawingSupplier) {
        if (drawingSupplier == null) {
            throw new IllegalArgumentException("Drawing supplier is null");
        }
        if (this.drawingSupplier != null) {
            throw new IllegalArgumentException("Drawing supplier already set");
        }
        this.drawingSupplier = drawingSupplier;
    }

    public boolean hasDrawingSupplier() {
        return this.drawingSupplier != null;
    }

    public ChartConfiguration getParent() {
        return this.parent;
    }

    public String getNoDataMessage() {
        return this.noDataMessage;
    }

    public PiePlotConfiguration getPiePlotConfiguration() {
        if (this.piePlotConf == null) {
            throw new IllegalStateException("Plot type is: " + (Object)((Object)this.plotType));
        }
        return this.piePlotConf;
    }

    public XYPlotConfiguration getXYPlotConfiguration() {
        if (this.xyPlotConf == null) {
            throw new IllegalStateException("Plot type is: " + (Object)((Object)this.plotType));
        }
        return this.xyPlotConf;
    }

    public CategoryPlotConfiguration getCategoryPlotConfiguration() {
        if (this.categoryPlotConf == null) {
            throw new IllegalStateException("Plot type is: " + (Object)((Object)this.plotType));
        }
        return this.categoryPlotConf;
    }

    public CombinedDomainXYPlotConfiguration getCombinedDomainXYPlotConfiguration() {
        if (this.combinedDomainXYPlotConf == null) {
            throw new IllegalStateException("Plot type is: " + (Object)((Object)this.plotType));
        }
        return this.combinedDomainXYPlotConf;
    }

    public CombinedDomainCategoryPlotConfiguration getCombinedDomainCategoryPlotConfiguration() {
        if (this.combinedDomainCategoryPlotConf == null) {
            throw new IllegalStateException("Plot type is: " + (Object)((Object)this.plotType));
        }
        return this.combinedDomainCategoryPlotConf;
    }

    public PlotType createPlotFromElement(PropertyList plotElementProps, String elementId, ConnectionInfo connectionInfo) {
        String plotTypeString = plotElementProps.getProperty("propertytreeid", DEFAULT_PLOT_ID);
        if (plotTypeString.isEmpty()) {
            throw new IllegalArgumentException("Plot element does not contain plot type property. Element ID: " + elementId);
        }
        this.plotType = PlotType.fromString(plotElementProps.getProperty("propertytreeid"));
        this.xyPlotConf = this.plotType == PlotType.XY_PLOT ? new XYPlotConfiguration(plotElementProps, connectionInfo, this) : null;
        this.categoryPlotConf = this.plotType == PlotType.CATEGORY_PLOT ? new CategoryPlotConfiguration(plotElementProps, connectionInfo, this) : null;
        this.piePlotConf = this.plotType == PlotType.PIE_PLOT ? new PiePlotConfiguration(plotElementProps, connectionInfo, this) : null;
        this.combinedDomainXYPlotConf = this.plotType == PlotType.COMBINED_DOMAIN_XY_PLOT ? new CombinedDomainXYPlotConfiguration(plotElementProps, connectionInfo, this) : null;
        this.combinedDomainCategoryPlotConf = this.plotType == PlotType.COMBINED_DOMAIN_CATEGORY_PLOT ? new CombinedDomainCategoryPlotConfiguration(plotElementProps, connectionInfo, this) : null;
        this.usedElementId = elementId;
        return this.plotType;
    }

    public PlotType getPlotType() {
        if (this.plotType == null) {
            throw new IllegalStateException("Plot type not set yet.");
        }
        return this.plotType;
    }

    public boolean isPlotTypeSet() {
        return this.plotType != null;
    }

    @Override
    public String getId() {
        return this.getPlotId();
    }

    public boolean hasSubplot() {
        boolean hasSubplots = false;
        if (this.getPlotType().getSubplotType() != null) {
            hasSubplots = true;
        }
        return hasSubplots;
    }

    public PlotConfiguration getSubplotConfiguration(String plotId) {
        if (plotId == null) {
            throw new IllegalArgumentException("Plot ID is null");
        }
        if (plotId.isEmpty()) {
            throw new IllegalArgumentException("Plot ID is empty");
        }
        if (!this.hasSubplot()) {
            throw new IllegalStateException("Plot type cannot not contain subplots: " + (Object)((Object)this.getPlotType()));
        }
        if (this.plotType == PlotType.COMBINED_DOMAIN_XY_PLOT) {
            return this.getCombinedDomainXYPlotConfiguration().getSubplotConfiguration(plotId);
        }
        if (this.plotType == PlotType.COMBINED_DOMAIN_CATEGORY_PLOT) {
            return this.getCombinedDomainCategoryPlotConfiguration().getSubplotConfiguration(plotId);
        }
        throw new IllegalStateException("Unknown root plot type: " + (Object)((Object)this.getPlotType()));
    }

    public boolean isPlotCreatedFromElement(String plotId, String elementId) {
        if (plotId == null) {
            throw new IllegalArgumentException("Plot ID is null");
        }
        if (plotId.isEmpty()) {
            throw new IllegalArgumentException("Plot ID is empty");
        }
        if (elementId == null) {
            throw new IllegalArgumentException("Element ID is null");
        }
        if (elementId.isEmpty()) {
            throw new IllegalArgumentException("Element ID is empty");
        }
        boolean returnFlag = true;
        if (this.plotId == null || !this.plotId.equals(plotId)) {
            returnFlag = false;
        } else if (this.plotType == null) {
            returnFlag = false;
        } else if (this.usedElementId == null || !this.usedElementId.equals(elementId)) {
            returnFlag = false;
        }
        return returnFlag;
    }

    public static enum PlotType {
        XY_PLOT("xyplot"),
        CATEGORY_PLOT("categoryplot"),
        PIE_PLOT("pieplot"),
        NO_DATA_PLOT("No data plot"),
        COMBINED_DOMAIN_XY_PLOT("combineddomainxyplot", XY_PLOT),
        COMBINED_DOMAIN_CATEGORY_PLOT("combineddomaincategoryplot", CATEGORY_PLOT);

        private final String name;
        private final PlotType subplotType;

        private PlotType(String name) {
            this.name = name;
            this.subplotType = null;
        }

        private PlotType(String name, PlotType subplotType) {
            this.name = name;
            this.subplotType = subplotType;
        }

        public static PlotType fromString(String name) {
            if (name != null) {
                for (PlotType type : PlotType.values()) {
                    if (!name.equalsIgnoreCase(type.name)) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown name: " + name);
        }

        public String getName() {
            return this.name;
        }

        public PlotType getSubplotType() {
            return this.subplotType;
        }
    }
}

