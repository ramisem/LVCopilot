/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.AbstractIntervalXYSeriesGroupConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.SeriesGroupBuilderConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class XYSeriesCollectionConfiguration
extends AbstractIntervalXYSeriesGroupConfiguration
implements Serializable {
    public XYSeriesCollectionConfiguration(PropertyList xySeriesCollectionProps, SeriesGroupBuilderConfiguration parent) {
        super(xySeriesCollectionProps, parent);
    }
}

