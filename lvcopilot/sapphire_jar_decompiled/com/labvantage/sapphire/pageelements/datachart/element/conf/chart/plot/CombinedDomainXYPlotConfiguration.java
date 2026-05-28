/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.axis.AxisLocation
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot;

import com.labvantage.sapphire.pageelements.datachart.element.conf.ConfigurationUtil;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.PaintConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.StrokeConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.AbstractComponentPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.AbstractPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.ComponentPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.PlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.axis.AxisConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.marker.MarkerConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.RendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.SeriesGroupConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator.URLGeneratorConfiguration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.axis.AxisLocation;
import sapphire.util.ConnectionInfo;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class CombinedDomainXYPlotConfiguration
extends AbstractPlotConfiguration
implements ComponentPlotConfiguration,
Serializable {
    private final List<PlotConfiguration> plotConfList = new ArrayList<PlotConfiguration>();
    private final AxisConfiguration domainAxisConf;
    private final AxisLocation domainAxisLocation;
    private int nextPlotConfIndex;

    public AxisLocation getDomainAxisLocation() {
        return this.domainAxisLocation;
    }

    public CombinedDomainXYPlotConfiguration(PropertyList combinedXYPlotProps, ConnectionInfo connectionInfo, PlotConfiguration parent) {
        super(combinedXYPlotProps, parent);
        PropertyListCollection plotCollection = combinedXYPlotProps.getCollectionNotNull("plotcollection");
        for (int i = 0; i < plotCollection.size(); ++i) {
            PropertyList plotProps = plotCollection.getPropertyList(i);
            PlotConfiguration subplotConf = new PlotConfiguration(plotProps, connectionInfo, parent.getParent());
            subplotConf.clearURLGeneratorConfigurationList();
            for (URLGeneratorConfiguration urlGeneratorConf : this.getParent().getURLGeneratorConfigurationList()) {
                subplotConf.addURLGeneratorConfigurationList(urlGeneratorConf);
            }
            this.plotConfList.add(subplotConf);
        }
        if (this.plotConfList.isEmpty() || ConfigurationUtil.countAnonymousConfigurations(this.plotConfList) == 0) {
            PlotConfiguration subplotConf = new PlotConfiguration(new PropertyList(), connectionInfo, parent.getParent());
            subplotConf.clearURLGeneratorConfigurationList();
            for (URLGeneratorConfiguration urlGeneratorConf : this.getParent().getURLGeneratorConfigurationList()) {
                subplotConf.addURLGeneratorConfigurationList(urlGeneratorConf);
            }
            this.plotConfList.add(subplotConf);
        }
        this.domainAxisConf = new AxisConfiguration(combinedXYPlotProps.getPropertyListNotNull("domainaxisprops"), connectionInfo, AxisConfiguration.AxisType.VALUE, this);
        PropertyList axisLocationProps = combinedXYPlotProps.getPropertyListNotNull("domainaxislocationprops");
        String axisLocation = axisLocationProps.getProperty("axislocation", AbstractComponentPlotConfiguration.DEFAULT_AXIS_LOCATION);
        this.domainAxisLocation = AbstractComponentPlotConfiguration.AxisLocationType.fromString(axisLocation).getAxisLocation();
        this.nextPlotConfIndex = 0;
    }

    public AxisConfiguration getDomainAxisConfiguration() {
        return this.domainAxisConf;
    }

    public CombinedDomainXYPlotConfiguration(CombinedDomainXYPlotConfiguration copy, PlotConfiguration parent) {
        super(copy, parent);
        for (PlotConfiguration plotConf : copy.plotConfList) {
            this.plotConfList.add(new PlotConfiguration(plotConf, plotConf.getId(), parent.getParent()));
        }
        this.domainAxisConf = new AxisConfiguration(copy.domainAxisConf, "", this);
        this.domainAxisLocation = copy.domainAxisLocation;
        this.nextPlotConfIndex = 0;
    }

    public PlotConfiguration getSubplotConfiguration(String plotId) {
        PlotConfiguration returnConf = ConfigurationUtil.getConf(this.plotConfList, plotId);
        if (returnConf == null) {
            int index = ConfigurationUtil.getAnonymousConf(this.plotConfList, this.nextPlotConfIndex);
            ++this.nextPlotConfIndex;
            returnConf = new PlotConfiguration(this.plotConfList.get(index), plotId, this.getParent().getParent());
            this.plotConfList.add(returnConf);
        }
        return returnConf;
    }

    @Override
    public List<RendererConfiguration> getRendererConfList() {
        ArrayList<RendererConfiguration> returnList = new ArrayList<RendererConfiguration>();
        for (PlotConfiguration plotConf : this.plotConfList) {
            if (!plotConf.isPlotTypeSet() || plotConf.getPlotType() != PlotConfiguration.PlotType.XY_PLOT) continue;
            returnList.addAll(plotConf.getXYPlotConfiguration().getRendererConfList());
        }
        return returnList;
    }

    @Override
    public SeriesGroupConfiguration getSeriesGroupConfiguration(String seriesGroupId) {
        SeriesGroupConfiguration returnConf = null;
        for (PlotConfiguration plotConf : this.plotConfList) {
            if (!plotConf.isPlotTypeSet() || plotConf.getPlotType() != PlotConfiguration.PlotType.XY_PLOT) continue;
            returnConf = plotConf.getXYPlotConfiguration().getSeriesGroupConfiguration(seriesGroupId);
        }
        if (returnConf == null) {
            throw new IllegalArgumentException("Series group not found: " + seriesGroupId);
        }
        return returnConf;
    }

    @Override
    public RendererConfiguration getRendererConfiguration(String rendererId) {
        throw new UnsupportedOperationException("Combined domain XY plot configuration does not support this operation");
    }

    @Override
    public MarkerConfiguration getRangeMarkerConfiguration(String rangeMarkerId) {
        throw new UnsupportedOperationException("Combined domain XY plot configuration does not support this operation");
    }

    @Override
    public MarkerConfiguration getDomainMarkerConfiguration(String domainMarkerId) {
        throw new UnsupportedOperationException("Combined domain XY plot configuration does not support this operation");
    }

    @Override
    public AxisConfiguration getRangeAxisConfiguration(String rangeAxisId) {
        throw new UnsupportedOperationException("Combined domain XY plot configuration does not support this operation");
    }

    @Override
    public AxisConfiguration getDomainAxisConfiguration(String domainAxisId) {
        throw new UnsupportedOperationException("Combined domain XY plot configuration does not support this operation");
    }

    @Override
    public AxisLocation getRangeAxisLocation(String rangeAxisId) {
        throw new UnsupportedOperationException("Combined domain XY plot configuration does not support this operation");
    }

    @Override
    public AxisLocation getDomainAxisLocation(String domainAxisId) {
        throw new UnsupportedOperationException("Combined domain XY plot configuration does not support this operation");
    }

    @Override
    public PaintConfiguration getDomainGridlinePaintConfiguration() {
        throw new UnsupportedOperationException("Combined domain XY plot configuration does not support this operation");
    }

    @Override
    public PaintConfiguration getRangeGridlinePaintConfiguration() {
        throw new UnsupportedOperationException("Combined domain XY plot configuration does not support this operation");
    }

    @Override
    public boolean isDomainGridlineVisible() {
        throw new UnsupportedOperationException("Combined domain XY plot configuration does not support this operation");
    }

    @Override
    public boolean isRangeGridlineVisible() {
        throw new UnsupportedOperationException("Combined domain XY plot configuration does not support this operation");
    }

    @Override
    public StrokeConfiguration getDomainGridlineStrokeConfiguration() {
        throw new UnsupportedOperationException("Combined domain XY plot configuration does not support this operation");
    }

    @Override
    public StrokeConfiguration getRangeGridlineStrokeConfiguration() {
        throw new UnsupportedOperationException("Combined domain XY plot configuration does not support this operation");
    }

    @Override
    public List<SeriesGroupConfiguration> getSeriesGroupConfList() {
        ArrayList<SeriesGroupConfiguration> returnList = new ArrayList<SeriesGroupConfiguration>();
        for (PlotConfiguration plotConf : this.plotConfList) {
            if (!plotConf.isPlotTypeSet() || plotConf.getPlotType() != PlotConfiguration.PlotType.XY_PLOT) continue;
            returnList.addAll(plotConf.getXYPlotConfiguration().getSeriesGroupConfList());
        }
        return returnList;
    }
}

