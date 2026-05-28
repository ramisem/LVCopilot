/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.axis.NumberAxis
 *  org.jfree.chart.axis.TickUnitSource
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.axis;

import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.axis.ValueAxisConfiguration;
import java.io.Serializable;
import java.util.Locale;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;
import sapphire.util.ConnectionInfo;
import sapphire.xml.PropertyList;

public final class NumberAxisConfiguration
implements Serializable {
    private static final String DEFAULT_STANDARD_TICK_UNIT_SOURCE = StandardTickUnitSourceType.STANDARD.getName();
    private static final String DEFAULT_AXIS_MAXIMUM_FRACTION_DIGITS = "10";
    private static final String DEFAULT_AUTO_RANGE_INCLUDES_ZERO = "Y";
    private final TickUnitSource standardTickUnitSource;
    private final ValueAxisConfiguration parent;
    private final boolean autoRangeIncludesZero;
    private final int maximumFractionDigits;

    public NumberAxisConfiguration(PropertyList numberAxisProps, ConnectionInfo connectionInfo, ValueAxisConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (numberAxisProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        Locale locale = I18nUtil.getConnectionLocale(connectionInfo);
        this.standardTickUnitSource = this.getStandardTickUnitSource(numberAxisProps, locale);
        this.autoRangeIncludesZero = numberAxisProps.getProperty("autorangeincludeszero", DEFAULT_AUTO_RANGE_INCLUDES_ZERO).toLowerCase().startsWith("y");
        this.maximumFractionDigits = Integer.valueOf(numberAxisProps.getProperty("maximumfractiondigits", DEFAULT_AXIS_MAXIMUM_FRACTION_DIGITS));
        this.parent = parent;
    }

    public NumberAxisConfiguration(NumberAxisConfiguration copy, ValueAxisConfiguration parent) {
        this.standardTickUnitSource = copy.standardTickUnitSource;
        this.autoRangeIncludesZero = copy.autoRangeIncludesZero;
        this.maximumFractionDigits = copy.maximumFractionDigits;
        this.parent = parent;
    }

    public boolean isAutoRangeIncludesZero() {
        return this.autoRangeIncludesZero;
    }

    private TickUnitSource getStandardTickUnitSource(PropertyList numberAxisProps, Locale locale) {
        TickUnitSource tickUnitSource;
        StandardTickUnitSourceType standardTickUnitSourceType = StandardTickUnitSourceType.fromString(numberAxisProps.getProperty("standardtickunitsource", DEFAULT_STANDARD_TICK_UNIT_SOURCE));
        if (standardTickUnitSourceType == StandardTickUnitSourceType.STANDARD) {
            tickUnitSource = NumberAxis.createStandardTickUnits((Locale)locale);
        } else if (standardTickUnitSourceType == StandardTickUnitSourceType.INTEGER) {
            tickUnitSource = NumberAxis.createIntegerTickUnits((Locale)locale);
        } else {
            throw new IllegalArgumentException("Unknown standard tick unit source type: " + (Object)((Object)standardTickUnitSourceType));
        }
        return tickUnitSource;
    }

    public TickUnitSource getStandardTickUnitSource() {
        return this.standardTickUnitSource;
    }

    public ValueAxisConfiguration getParent() {
        return this.parent;
    }

    public int getMaximumFractionDigits() {
        return this.maximumFractionDigits;
    }

    public static enum StandardTickUnitSourceType {
        STANDARD("Standard"),
        INTEGER("Integer");

        private final String name;

        private StandardTickUnitSourceType(String name) {
            this.name = name;
        }

        public static StandardTickUnitSourceType fromString(String name) {
            if (name != null) {
                for (StandardTickUnitSourceType type : StandardTickUnitSourceType.values()) {
                    if (!name.equalsIgnoreCase(type.name)) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown name: " + name);
        }

        public String getName() {
            return this.name;
        }
    }
}

