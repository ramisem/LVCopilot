/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.axis.AxisLocation
 *  org.jfree.chart.plot.DatasetRenderingOrder
 *  org.jfree.chart.plot.PlotOrientation
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot;

import com.labvantage.sapphire.pageelements.datachart.element.conf.ConfigurationUtil;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.PaintConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.StrokeConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.AbstractPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.ComponentPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.PlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.axis.AxisConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.marker.MarkerConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.RendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.SeriesGroupConfiguration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import sapphire.util.ConnectionInfo;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public abstract class AbstractComponentPlotConfiguration
extends AbstractPlotConfiguration
implements ComponentPlotConfiguration,
Serializable {
    public static final String DEFAULT_PLOT_ORIENTATION = PlotOrientationType.fromPlotOrientation(PlotOrientation.VERTICAL).getName();
    public static final String DEFAULT_AXIS_LOCATION = AxisLocationType.BOTTOM_OR_LEFT.getName();
    private static final String DEFAULT_RANGE_GRID_LINE_VISIBLE = "Y";
    private static final String DEFAULT_DOMAIN_GRID_LINE_VISIBLE = "Y";
    private static final String DEFAULT_DATA_SET_RENDERING_ORDER = DatasetRenderingOrderType.fromDatasetRenderingOrder(DatasetRenderingOrder.REVERSE).getName();
    private final List<RendererConfiguration> rendererConfList;
    private final PlotConfiguration parent;
    private final List<String> annotationIdList;
    private final PlotOrientation plotOrientation;
    private final DatasetRenderingOrder datasetRenderingOrder;
    private final Map<String, AxisLocation> rangeAxisLocationMap;
    private final Map<String, AxisLocation> domainAxisLocationMap;
    private final PaintConfiguration rangeGridlinePaintConf;
    private final PaintConfiguration domainGridlinePaintConf;
    private final boolean rangeGridlineVisible;
    private final boolean domainGridlineVisible;
    private final StrokeConfiguration rangeGridlineStrokeConf;
    private final StrokeConfiguration domainGridlineStrokeConf;
    private final List<AxisConfiguration> rangeAxisConfList;
    private final List<AxisConfiguration> domainAxisConfList;
    private final List<MarkerConfiguration> domainMarkerConfList;
    private final List<MarkerConfiguration> rangeMarkerConfList;
    private final List<SeriesGroupConfiguration> seriesGroupConfList;
    private int nextSeriesGroupConfIndex;
    private int nextPlotRendererConfIndex;
    private int nextRangeAxisConfIndex;
    private int nextDomainAxisConfIndex;

    public AbstractComponentPlotConfiguration(PropertyList componentPlotProps, ConnectionInfo connectionInfo, PlotConfiguration parent) {
        super(componentPlotProps, parent);
        PropertyList markerProps;
        if (connectionInfo == null) {
            throw new IllegalArgumentException("Connection info is null");
        }
        this.parent = parent;
        this.rangeMarkerConfList = new ArrayList<MarkerConfiguration>();
        PropertyListCollection rangeMarkerCollection = componentPlotProps.getCollectionNotNull("rangemarkercollection");
        for (int i = 0; i < rangeMarkerCollection.size(); ++i) {
            markerProps = rangeMarkerCollection.getPropertyList(i);
            this.rangeMarkerConfList.add(new MarkerConfiguration(markerProps, this.getDefaultRangeMarkerType(), this));
        }
        this.domainMarkerConfList = new ArrayList<MarkerConfiguration>();
        PropertyListCollection domainMarkerCollection = componentPlotProps.getCollectionNotNull("domainmarkercollection");
        for (int i = 0; i < domainMarkerCollection.size(); ++i) {
            markerProps = domainMarkerCollection.getPropertyList(i);
            this.domainMarkerConfList.add(new MarkerConfiguration(markerProps, this.getDefaultDomainMarkerType(), this));
        }
        this.rangeAxisConfList = new ArrayList<AxisConfiguration>();
        PropertyListCollection rangeAxisCollection = componentPlotProps.getCollectionNotNull("rangeaxiscollection");
        for (int i = 0; i < rangeAxisCollection.size(); ++i) {
            PropertyList axisProps = rangeAxisCollection.getPropertyList(i);
            this.rangeAxisConfList.add(new AxisConfiguration(axisProps, connectionInfo, this.getDefaultRangeAxisType(), this));
        }
        if (this.rangeAxisConfList.isEmpty() || ConfigurationUtil.countAnonymousConfigurations(this.rangeAxisConfList) == 0) {
            this.rangeAxisConfList.add(new AxisConfiguration(new PropertyList(), connectionInfo, this.getDefaultRangeAxisType(), this));
        }
        this.domainAxisConfList = new ArrayList<AxisConfiguration>();
        PropertyListCollection domainAxisCollection = componentPlotProps.getCollectionNotNull("domainaxiscollection");
        for (int i = 0; i < domainAxisCollection.size(); ++i) {
            PropertyList axisProps = domainAxisCollection.getPropertyList(i);
            this.domainAxisConfList.add(new AxisConfiguration(axisProps, connectionInfo, this.getDefaultDomainAxisType(), this));
        }
        if (this.domainAxisConfList.isEmpty() || ConfigurationUtil.countAnonymousConfigurations(this.domainAxisConfList) == 0) {
            this.domainAxisConfList.add(new AxisConfiguration(new PropertyList(), connectionInfo, this.getDefaultDomainAxisType(), this));
        }
        this.rangeAxisLocationMap = new HashMap<String, AxisLocation>();
        PropertyListCollection rangeAxisLocationCollection = componentPlotProps.getCollectionNotNull("rangeaxislocationcollection");
        for (int i = 0; i < rangeAxisLocationCollection.size(); ++i) {
            PropertyList axisLocationProps = rangeAxisLocationCollection.getPropertyList(i);
            String axisId = axisLocationProps.getProperty("rangeaxisid");
            String axisLocation = axisLocationProps.getProperty("axislocation", DEFAULT_AXIS_LOCATION);
            this.rangeAxisLocationMap.put(axisId, AxisLocationType.fromString(axisLocation).getAxisLocation());
        }
        this.domainAxisLocationMap = new HashMap<String, AxisLocation>();
        PropertyListCollection domainAxisLocationCollection = componentPlotProps.getCollectionNotNull("domainaxislocationcollection");
        for (int i = 0; i < domainAxisLocationCollection.size(); ++i) {
            PropertyList axisLocationProps = domainAxisLocationCollection.getPropertyList(i);
            String axisId = axisLocationProps.getProperty("domainaxisid");
            String axisLocation = axisLocationProps.getProperty("axislocation", DEFAULT_AXIS_LOCATION);
            this.domainAxisLocationMap.put(axisId, AxisLocationType.fromString(axisLocation).getAxisLocation());
        }
        this.seriesGroupConfList = new ArrayList<SeriesGroupConfiguration>();
        PropertyListCollection seriesGroupCollection = componentPlotProps.getCollectionNotNull("seriesgroupcollection");
        for (int i = 0; i < seriesGroupCollection.size(); ++i) {
            PropertyList seriesGroupProps = seriesGroupCollection.getPropertyList(i);
            this.seriesGroupConfList.add(new SeriesGroupConfiguration(seriesGroupProps, this));
        }
        if (this.seriesGroupConfList.isEmpty() || ConfigurationUtil.countAnonymousConfigurations(this.seriesGroupConfList) == 0) {
            this.seriesGroupConfList.add(new SeriesGroupConfiguration(new PropertyList(), this));
        }
        this.rendererConfList = new ArrayList<RendererConfiguration>();
        PropertyListCollection rendererCollection = componentPlotProps.getCollectionNotNull("renderercollection");
        for (int i = 0; i < rendererCollection.size(); ++i) {
            PropertyList rendererProps = rendererCollection.getPropertyList(i);
            this.rendererConfList.add(new RendererConfiguration(rendererProps, this.getDefaultRendererType(), (ComponentPlotConfiguration)this));
        }
        if (this.rendererConfList.isEmpty() || ConfigurationUtil.countAnonymousConfigurations(this.rendererConfList) == 0) {
            this.rendererConfList.add(new RendererConfiguration(new PropertyList(), this.getDefaultRendererType(), (ComponentPlotConfiguration)this));
        }
        this.annotationIdList = new ArrayList<String>();
        PropertyListCollection annotationIdCollection = componentPlotProps.getCollectionNotNull("annotationcollection");
        for (int i = 0; i < annotationIdCollection.size(); ++i) {
            PropertyList annotationIdProps = annotationIdCollection.getPropertyList(i);
            this.annotationIdList.add(annotationIdProps.getProperty("annotationid"));
        }
        this.plotOrientation = PlotOrientationType.fromString(componentPlotProps.getProperty("plotorientation", DEFAULT_PLOT_ORIENTATION)).getPlotOrientation();
        this.datasetRenderingOrder = DatasetRenderingOrderType.fromString(componentPlotProps.getProperty("datasetrenderingorder", DEFAULT_DATA_SET_RENDERING_ORDER)).getDatasetRenderingOrder();
        this.rangeGridlinePaintConf = new PaintConfiguration(componentPlotProps.getPropertyListNotNull("rangegridlinepaintprops"));
        this.domainGridlinePaintConf = new PaintConfiguration(componentPlotProps.getPropertyListNotNull("domaingridlinepaintprops"));
        this.rangeGridlineVisible = componentPlotProps.getProperty("rangegridlinevisible", "Y").toLowerCase().startsWith("y");
        this.domainGridlineVisible = componentPlotProps.getProperty("domaingridlinevisible", "Y").toLowerCase().startsWith("y");
        this.rangeGridlineStrokeConf = new StrokeConfiguration(componentPlotProps.getPropertyListNotNull("rangegridlinestrokeprops"));
        this.domainGridlineStrokeConf = new StrokeConfiguration(componentPlotProps.getPropertyListNotNull("domaingridlinestrokeprops"));
        this.nextSeriesGroupConfIndex = 0;
        this.nextPlotRendererConfIndex = 0;
        this.nextRangeAxisConfIndex = 0;
        this.nextDomainAxisConfIndex = 0;
    }

    protected abstract RendererConfiguration.RendererType getDefaultRendererType();

    public AbstractComponentPlotConfiguration(AbstractComponentPlotConfiguration copy, PlotConfiguration parent) {
        super(copy, parent);
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.parent = parent;
        this.rendererConfList = new ArrayList<RendererConfiguration>();
        for (RendererConfiguration rendererConfiguration : copy.rendererConfList) {
            this.rendererConfList.add(new RendererConfiguration(rendererConfiguration, rendererConfiguration.getRendererId(), (ComponentPlotConfiguration)this));
        }
        this.annotationIdList = new ArrayList<String>();
        for (String string : copy.annotationIdList) {
            this.annotationIdList.add(string);
        }
        this.plotOrientation = copy.plotOrientation;
        this.datasetRenderingOrder = copy.datasetRenderingOrder;
        this.rangeAxisLocationMap = new HashMap<String, AxisLocation>();
        Set<Map.Entry<String, AxisLocation>> rangeAxisLocationMapEntry = this.rangeAxisLocationMap.entrySet();
        for (Map.Entry<String, AxisLocation> entry : rangeAxisLocationMapEntry) {
            this.rangeAxisLocationMap.put(entry.getKey(), entry.getValue());
        }
        this.domainAxisLocationMap = new HashMap<String, AxisLocation>();
        Set<Map.Entry<String, AxisLocation>> set = this.domainAxisLocationMap.entrySet();
        for (Map.Entry<String, AxisLocation> entry : set) {
            this.domainAxisLocationMap.put(entry.getKey(), entry.getValue());
        }
        this.rangeGridlinePaintConf = new PaintConfiguration(copy.getRangeGridlinePaintConfiguration());
        this.domainGridlinePaintConf = new PaintConfiguration(copy.getDomainGridlinePaintConfiguration());
        this.rangeGridlineVisible = copy.rangeGridlineVisible;
        this.domainGridlineVisible = copy.domainGridlineVisible;
        this.rangeGridlineStrokeConf = new StrokeConfiguration(copy.getRangeGridlineStrokeConfiguration());
        this.domainGridlineStrokeConf = new StrokeConfiguration(copy.getDomainGridlineStrokeConfiguration());
        this.rangeAxisConfList = new ArrayList<AxisConfiguration>();
        for (AxisConfiguration axisConf : copy.rangeAxisConfList) {
            this.rangeAxisConfList.add(new AxisConfiguration(axisConf, axisConf.getAxisId(), this));
        }
        this.domainAxisConfList = new ArrayList<AxisConfiguration>();
        for (AxisConfiguration axisConf : copy.domainAxisConfList) {
            this.domainAxisConfList.add(new AxisConfiguration(axisConf, axisConf.getAxisId(), this));
        }
        this.domainMarkerConfList = new ArrayList<MarkerConfiguration>();
        for (MarkerConfiguration markerConf : copy.domainMarkerConfList) {
            this.domainMarkerConfList.add(new MarkerConfiguration(markerConf, this));
        }
        this.rangeMarkerConfList = new ArrayList<MarkerConfiguration>();
        for (MarkerConfiguration markerConf : copy.rangeMarkerConfList) {
            this.rangeMarkerConfList.add(new MarkerConfiguration(markerConf, this));
        }
        this.seriesGroupConfList = new ArrayList<SeriesGroupConfiguration>();
        for (SeriesGroupConfiguration seriesGroupConf : copy.seriesGroupConfList) {
            this.seriesGroupConfList.add(new SeriesGroupConfiguration(seriesGroupConf, seriesGroupConf.getSeriesGroupId(), this));
        }
        this.nextSeriesGroupConfIndex = 0;
        this.nextPlotRendererConfIndex = 0;
        this.nextRangeAxisConfIndex = 0;
        this.nextDomainAxisConfIndex = 0;
    }

    protected abstract MarkerConfiguration.MarkerType getDefaultRangeMarkerType();

    protected abstract MarkerConfiguration.MarkerType getDefaultDomainMarkerType();

    @Override
    public List<SeriesGroupConfiguration> getSeriesGroupConfList() {
        return this.seriesGroupConfList;
    }

    @Override
    public PaintConfiguration getRangeGridlinePaintConfiguration() {
        return this.rangeGridlinePaintConf;
    }

    @Override
    public PaintConfiguration getDomainGridlinePaintConfiguration() {
        return this.domainGridlinePaintConf;
    }

    @Override
    public boolean isRangeGridlineVisible() {
        return this.rangeGridlineVisible;
    }

    @Override
    public boolean isDomainGridlineVisible() {
        return this.domainGridlineVisible;
    }

    @Override
    public StrokeConfiguration getRangeGridlineStrokeConfiguration() {
        return this.rangeGridlineStrokeConf;
    }

    @Override
    public StrokeConfiguration getDomainGridlineStrokeConfiguration() {
        return this.domainGridlineStrokeConf;
    }

    public PlotOrientation getPlotOrientation() {
        return this.plotOrientation;
    }

    public DatasetRenderingOrder getDatasetRenderingOrder() {
        return this.datasetRenderingOrder;
    }

    @Override
    public PlotConfiguration getParent() {
        return this.parent;
    }

    protected abstract AxisConfiguration.AxisType getDefaultRangeAxisType();

    protected abstract AxisConfiguration.AxisType getDefaultDomainAxisType();

    @Override
    public MarkerConfiguration getRangeMarkerConfiguration(String rangeMarkerId) {
        if (rangeMarkerId == null) {
            throw new IllegalArgumentException("Range marker ID is null");
        }
        if (rangeMarkerId.isEmpty()) {
            throw new IllegalArgumentException("Range marker ID is empty");
        }
        MarkerConfiguration returnConf = null;
        for (MarkerConfiguration markerConf : this.rangeMarkerConfList) {
            if (!markerConf.getMarkerId().equals(rangeMarkerId)) continue;
            returnConf = markerConf;
        }
        if (returnConf == null) {
            throw new IllegalArgumentException("Range marker with given ID not found: " + rangeMarkerId);
        }
        return returnConf;
    }

    @Override
    public MarkerConfiguration getDomainMarkerConfiguration(String domainMarkerId) {
        if (domainMarkerId == null) {
            throw new IllegalArgumentException("Domain marker ID is null");
        }
        if (domainMarkerId.isEmpty()) {
            throw new IllegalArgumentException("Domain marker ID is empty");
        }
        MarkerConfiguration returnConf = null;
        for (MarkerConfiguration markerConf : this.domainMarkerConfList) {
            if (!markerConf.getMarkerId().equals(domainMarkerId)) continue;
            returnConf = markerConf;
        }
        if (returnConf == null) {
            throw new IllegalArgumentException("Domain marker with given ID not found: " + domainMarkerId);
        }
        return returnConf;
    }

    @Override
    public List<RendererConfiguration> getRendererConfList() {
        return this.rendererConfList;
    }

    @Override
    public SeriesGroupConfiguration getSeriesGroupConfiguration(String seriesGroupId) {
        SeriesGroupConfiguration returnConf = ConfigurationUtil.getConf(this.seriesGroupConfList, seriesGroupId);
        if (returnConf == null) {
            int index = ConfigurationUtil.getAnonymousConf(this.seriesGroupConfList, this.nextSeriesGroupConfIndex);
            ++this.nextSeriesGroupConfIndex;
            returnConf = new SeriesGroupConfiguration(this.seriesGroupConfList.get(index), seriesGroupId, this);
            this.seriesGroupConfList.add(returnConf);
        }
        return returnConf;
    }

    @Override
    public RendererConfiguration getRendererConfiguration(String rendererId) {
        RendererConfiguration returnConf = ConfigurationUtil.getConf(this.rendererConfList, rendererId);
        if (returnConf == null) {
            int index = ConfigurationUtil.getAnonymousConf(this.rendererConfList, this.nextPlotRendererConfIndex);
            ++this.nextPlotRendererConfIndex;
            returnConf = new RendererConfiguration(this.rendererConfList.get(index), rendererId, (ComponentPlotConfiguration)this);
            this.rendererConfList.add(returnConf);
        }
        return returnConf;
    }

    @Override
    public AxisConfiguration getRangeAxisConfiguration(String rangeAxisId) {
        AxisConfiguration returnConf = ConfigurationUtil.getConf(this.rangeAxisConfList, rangeAxisId);
        if (returnConf == null) {
            int index = ConfigurationUtil.getAnonymousConf(this.rangeAxisConfList, this.nextRangeAxisConfIndex);
            ++this.nextRangeAxisConfIndex;
            returnConf = new AxisConfiguration(this.rangeAxisConfList.get(index), rangeAxisId, this);
            this.rangeAxisConfList.add(returnConf);
        }
        return returnConf;
    }

    @Override
    public AxisConfiguration getDomainAxisConfiguration(String domainAxisId) {
        AxisConfiguration returnConf = ConfigurationUtil.getConf(this.domainAxisConfList, domainAxisId);
        if (returnConf == null) {
            int index = ConfigurationUtil.getAnonymousConf(this.domainAxisConfList, this.nextDomainAxisConfIndex);
            ++this.nextDomainAxisConfIndex;
            returnConf = new AxisConfiguration(this.domainAxisConfList.get(index), domainAxisId, this);
            this.domainAxisConfList.add(returnConf);
        }
        return returnConf;
    }

    @Override
    public AxisLocation getRangeAxisLocation(String rangeAxisId) {
        return this.rangeAxisLocationMap.get(rangeAxisId);
    }

    @Override
    public AxisLocation getDomainAxisLocation(String domainAxisId) {
        return this.domainAxisLocationMap.get(domainAxisId);
    }

    public List<String> getAnnotationIdList() {
        return this.annotationIdList;
    }

    public static enum AxisLocationType {
        BOTTOM_OR_LEFT("Bottom or Left", AxisLocation.BOTTOM_OR_LEFT),
        BOTTOM_OR_RIGHT("Bottom or Right", AxisLocation.BOTTOM_OR_RIGHT),
        TOP_OR_LEFT("Top or Left", AxisLocation.TOP_OR_LEFT),
        TOP_OR_RIGHT("Top or Right", AxisLocation.TOP_OR_RIGHT);

        private final String name;
        private final AxisLocation axisLocation;

        private AxisLocationType(String name, AxisLocation axisLocation) {
            this.name = name;
            this.axisLocation = axisLocation;
        }

        public static AxisLocationType fromString(String name) {
            if (name != null) {
                for (AxisLocationType type : AxisLocationType.values()) {
                    if (!name.equalsIgnoreCase(type.name)) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown name: " + name);
        }

        public String getName() {
            return this.name;
        }

        public AxisLocation getAxisLocation() {
            return this.axisLocation;
        }
    }

    public static enum DatasetRenderingOrderType {
        REVERSE("Reverse", DatasetRenderingOrder.REVERSE),
        FORWARD("Forward", DatasetRenderingOrder.FORWARD);

        private final String name;
        private final DatasetRenderingOrder datasetRenderingOrder;

        private DatasetRenderingOrderType(String name, DatasetRenderingOrder datasetRenderingOrder) {
            this.name = name;
            this.datasetRenderingOrder = datasetRenderingOrder;
        }

        public static DatasetRenderingOrderType fromDatasetRenderingOrder(DatasetRenderingOrder datasetRenderingOrder) {
            if (datasetRenderingOrder != null) {
                for (DatasetRenderingOrderType type : DatasetRenderingOrderType.values()) {
                    if (datasetRenderingOrder != type.datasetRenderingOrder) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown data set rendering order: " + datasetRenderingOrder);
        }

        public static DatasetRenderingOrderType fromString(String name) {
            if (name != null) {
                for (DatasetRenderingOrderType type : DatasetRenderingOrderType.values()) {
                    if (!name.equalsIgnoreCase(type.name)) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown name: " + name);
        }

        public DatasetRenderingOrder getDatasetRenderingOrder() {
            return this.datasetRenderingOrder;
        }

        public String getName() {
            return this.name;
        }
    }

    public static enum PlotOrientationType {
        HORIZONTAL("Horizontal", PlotOrientation.HORIZONTAL),
        VERTICAL("Vertical", PlotOrientation.VERTICAL);

        private final String name;
        private final PlotOrientation plotOrientation;

        private PlotOrientationType(String name, PlotOrientation plotOrientation) {
            this.name = name;
            this.plotOrientation = plotOrientation;
        }

        public static PlotOrientationType fromPlotOrientation(PlotOrientation plotOrientation) {
            if (plotOrientation != null) {
                for (PlotOrientationType type : PlotOrientationType.values()) {
                    if (plotOrientation != type.plotOrientation) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown plot orientation: " + plotOrientation);
        }

        public static PlotOrientationType fromString(String name) {
            if (name != null) {
                for (PlotOrientationType type : PlotOrientationType.values()) {
                    if (!name.equalsIgnoreCase(type.name)) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown name: " + name);
        }

        public PlotOrientation getPlotOrientation() {
            return this.plotOrientation;
        }

        public String getName() {
            return this.name;
        }
    }
}

