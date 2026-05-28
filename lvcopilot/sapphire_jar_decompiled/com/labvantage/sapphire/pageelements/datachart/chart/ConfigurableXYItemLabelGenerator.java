/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.labels.AbstractXYItemLabelGenerator
 *  org.jfree.chart.labels.XYItemLabelGenerator
 *  org.jfree.data.xy.XYDataset
 *  org.jfree.util.PublicCloneable
 */
package com.labvantage.sapphire.pageelements.datachart.chart;

import com.labvantage.sapphire.pageelements.datachart.data.TraceableSeriesGroup;
import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import org.jfree.chart.labels.AbstractXYItemLabelGenerator;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.data.xy.XYDataset;
import org.jfree.util.PublicCloneable;

public class ConfigurableXYItemLabelGenerator
extends AbstractXYItemLabelGenerator
implements XYItemLabelGenerator,
Cloneable,
PublicCloneable,
Serializable {
    private StringExpression messageFormat;
    private ChartBindingMap chartBindingMap;
    private TraceableSeriesGroup traceableSeriesGroup;

    public ConfigurableXYItemLabelGenerator(String formatString, ChartBindingMap chartBindingMap, TraceableSeriesGroup traceableSeriesGroup) {
        this.messageFormat = new StringExpression(formatString);
        this.chartBindingMap = chartBindingMap;
        this.traceableSeriesGroup = traceableSeriesGroup;
    }

    public String generateLabel(XYDataset dataset, int series, int item) {
        this.chartBindingMap.setActiveItem(this.traceableSeriesGroup.getPlotId(), this.traceableSeriesGroup.getSeriesGroupId(), this.traceableSeriesGroup.getSeriesId(series), item);
        String result = this.messageFormat.evaluateNoException(this.chartBindingMap);
        return result;
    }

    public Object clone() throws CloneNotSupportedException {
        ConfigurableXYItemLabelGenerator clone = (ConfigurableXYItemLabelGenerator)super.clone();
        clone.messageFormat = new StringExpression(this.messageFormat);
        clone.chartBindingMap = this.chartBindingMap;
        clone.traceableSeriesGroup = this.traceableSeriesGroup;
        return clone;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ConfigurableXYItemLabelGenerator)) {
            return false;
        }
        return super.equals(obj);
    }
}

