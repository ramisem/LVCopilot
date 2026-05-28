/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.SectionLabelConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.AbstractPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.PlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.SectionConfiguration;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import sapphire.util.ConnectionInfo;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class PiePlotConfiguration
extends AbstractPlotConfiguration
implements Serializable {
    private static final String DEFAULT_USE_3D = "N";
    private static final String DEFAULT_SHOWSHADOWS = "N";
    private static final String DEFAULT_USE_DARKER_SIDES = "Y";
    private final SectionLabelConfiguration sectionLabelConf;
    private final PlotConfiguration parent;
    private final String urlGeneratorId;
    private final Double depthFactor;
    private final boolean use3d;
    private final boolean showShadows;
    private final boolean useDarkerSides;
    private final List<SectionConfiguration> sectionConfigurationList;

    protected PiePlotConfiguration(PropertyList piePlotProps, ConnectionInfo connectionInfo, PlotConfiguration parent) {
        super(piePlotProps, parent);
        if (piePlotProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        if (connectionInfo == null) {
            throw new IllegalArgumentException("Connection info is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.urlGeneratorId = piePlotProps.getProperty("urlgeneratorid");
        this.parent = parent;
        this.sectionConfigurationList = new ArrayList<SectionConfiguration>();
        PropertyListCollection sectionCollection = piePlotProps.getCollectionNotNull("sectioncollection");
        for (int i = 0; i < sectionCollection.size(); ++i) {
            PropertyList seriesProps = sectionCollection.getPropertyList(i);
            this.sectionConfigurationList.add(new SectionConfiguration(seriesProps, this));
        }
        this.use3d = piePlotProps.getProperty("use3d", "N").toLowerCase().startsWith("y");
        this.showShadows = piePlotProps.getProperty("showshadows", "N").toLowerCase().startsWith("y");
        this.useDarkerSides = piePlotProps.getProperty("usedarkersides", DEFAULT_USE_DARKER_SIDES).toLowerCase().startsWith("y");
        Double depthFactorTmp = null;
        String depthFactorStr = piePlotProps.getProperty("depthfactor");
        if (!depthFactorStr.isEmpty()) {
            try {
                depthFactorTmp = new BigDecimal(depthFactorStr).doubleValue();
            }
            catch (NumberFormatException e) {
                throw new IllegalArgumentException("Pie Plot 3D Configuration: Not valid depth factor: " + depthFactorStr);
            }
        }
        this.depthFactor = depthFactorTmp;
        this.sectionLabelConf = new SectionLabelConfiguration(piePlotProps.getPropertyListNotNull("labelprops"), this);
    }

    protected PiePlotConfiguration(PiePlotConfiguration copy, PlotConfiguration parent) {
        super(copy, parent);
        if (copy == null) {
            throw new IllegalArgumentException("Copy is null");
        }
        this.parent = parent;
        this.sectionLabelConf = new SectionLabelConfiguration(copy.sectionLabelConf);
        this.urlGeneratorId = copy.urlGeneratorId;
        this.depthFactor = copy.depthFactor;
        this.use3d = copy.use3d;
        this.showShadows = copy.showShadows;
        this.useDarkerSides = copy.useDarkerSides;
        this.sectionConfigurationList = new ArrayList<SectionConfiguration>();
        for (SectionConfiguration sectionConf : copy.sectionConfigurationList) {
            this.sectionConfigurationList.add(new SectionConfiguration(sectionConf, this));
        }
    }

    public String getUrlGeneratorId() {
        return this.urlGeneratorId;
    }

    @Override
    public PlotConfiguration getParent() {
        return this.parent;
    }

    public List<SectionConfiguration> getSectionConfigurationList() {
        return this.sectionConfigurationList;
    }

    public Double getDepthFactor() {
        return this.depthFactor;
    }

    public boolean isUse3d() {
        return this.use3d;
    }

    public boolean isShowShadows() {
        return this.showShadows;
    }

    public boolean isUseDarkerSides() {
        return this.useDarkerSides;
    }

    public SectionLabelConfiguration getSectionLabelConfiguration() {
        return this.sectionLabelConf;
    }
}

