/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.labels.AbstractCategoryItemLabelGenerator
 *  org.jfree.chart.labels.CategoryItemLabelGenerator
 *  org.jfree.data.category.CategoryDataset
 *  org.jfree.util.PublicCloneable
 */
package com.labvantage.sapphire.pageelements.datachart.chart;

import com.labvantage.sapphire.pageelements.datachart.data.TraceableSeriesGroup;
import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import java.text.NumberFormat;
import org.jfree.chart.labels.AbstractCategoryItemLabelGenerator;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.util.PublicCloneable;

public class ConfigurableCategoryItemLabelGenerator
extends AbstractCategoryItemLabelGenerator
implements CategoryItemLabelGenerator,
Cloneable,
PublicCloneable,
Serializable {
    private StringExpression messageFormat;
    private ChartBindingMap chartBindingMap;
    private TraceableSeriesGroup traceableSeriesGroup;

    public ConfigurableCategoryItemLabelGenerator(String formatString, ChartBindingMap chartBindingMap, TraceableSeriesGroup traceableSeriesGroup) {
        super(formatString, NumberFormat.getInstance());
        this.messageFormat = new StringExpression(formatString);
        this.chartBindingMap = chartBindingMap;
        this.traceableSeriesGroup = traceableSeriesGroup;
    }

    public String generateLabel(CategoryDataset dataset, int series, int item) {
        this.chartBindingMap.setActiveItem(this.traceableSeriesGroup.getPlotId(), this.traceableSeriesGroup.getSeriesGroupId(), this.traceableSeriesGroup.getSeriesId(series), item);
        String result = this.messageFormat.evaluateNoException(this.chartBindingMap);
        return result;
    }

    public Object clone() throws CloneNotSupportedException {
        ConfigurableCategoryItemLabelGenerator clone = (ConfigurableCategoryItemLabelGenerator)super.clone();
        clone.messageFormat = new StringExpression(this.messageFormat);
        clone.chartBindingMap = this.chartBindingMap;
        clone.traceableSeriesGroup = this.traceableSeriesGroup;
        return clone;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ConfigurableCategoryItemLabelGenerator)) {
            return false;
        }
        return super.equals(obj);
    }
}

