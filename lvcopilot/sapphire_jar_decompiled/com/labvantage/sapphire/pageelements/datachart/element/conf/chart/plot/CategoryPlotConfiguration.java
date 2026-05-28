/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.util.SortOrder
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.AbstractComponentPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.PlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.axis.AxisConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.marker.MarkerConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.RendererConfiguration;
import java.io.Serializable;
import org.jfree.util.SortOrder;
import sapphire.util.ConnectionInfo;
import sapphire.xml.PropertyList;

public class CategoryPlotConfiguration
extends AbstractComponentPlotConfiguration
implements Serializable {
    private static final String DEFAULT_COLUMN_RENDERING_ORDER = ColumnRenderingOrderType.fromSortOrder(SortOrder.ASCENDING).getName();
    private final SortOrder columnRenderingOrder;

    public CategoryPlotConfiguration(PropertyList categoryPlotProps, ConnectionInfo connectionInfo, PlotConfiguration parent) {
        super(categoryPlotProps, connectionInfo, parent);
        this.columnRenderingOrder = ColumnRenderingOrderType.fromString(categoryPlotProps.getProperty("columnrenderingorder", DEFAULT_COLUMN_RENDERING_ORDER)).getSortOrder();
    }

    public SortOrder getColumnRenderingOrder() {
        return this.columnRenderingOrder;
    }

    @Override
    protected RendererConfiguration.RendererType getDefaultRendererType() {
        return RendererConfiguration.RendererType.LINE_AND_SHAPE;
    }

    protected CategoryPlotConfiguration(CategoryPlotConfiguration copy, PlotConfiguration parent) {
        super(copy, parent);
        this.columnRenderingOrder = copy.columnRenderingOrder;
    }

    @Override
    protected MarkerConfiguration.MarkerType getDefaultRangeMarkerType() {
        return MarkerConfiguration.MarkerType.VALUE;
    }

    @Override
    protected MarkerConfiguration.MarkerType getDefaultDomainMarkerType() {
        return MarkerConfiguration.MarkerType.CATEGORY;
    }

    @Override
    protected AxisConfiguration.AxisType getDefaultRangeAxisType() {
        return AxisConfiguration.AxisType.VALUE;
    }

    @Override
    protected AxisConfiguration.AxisType getDefaultDomainAxisType() {
        return AxisConfiguration.AxisType.CATEGORY;
    }

    public static enum ColumnRenderingOrderType {
        ASCENDING("Ascending", SortOrder.ASCENDING),
        DESCENDING("Descending", SortOrder.DESCENDING);

        private final String name;
        private final SortOrder sortOrder;

        private ColumnRenderingOrderType(String name, SortOrder sortOrder) {
            this.name = name;
            this.sortOrder = sortOrder;
        }

        public static ColumnRenderingOrderType fromSortOrder(SortOrder sortOrder) {
            if (sortOrder != null) {
                for (ColumnRenderingOrderType type : ColumnRenderingOrderType.values()) {
                    if (sortOrder != type.sortOrder) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown sort order: " + sortOrder);
        }

        public static ColumnRenderingOrderType fromString(String name) {
            if (name != null) {
                for (ColumnRenderingOrderType type : ColumnRenderingOrderType.values()) {
                    if (!name.equalsIgnoreCase(type.name)) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown name: " + name);
        }

        public SortOrder getSortOrder() {
            return this.sortOrder;
        }

        public String getName() {
            return this.name;
        }
    }
}

