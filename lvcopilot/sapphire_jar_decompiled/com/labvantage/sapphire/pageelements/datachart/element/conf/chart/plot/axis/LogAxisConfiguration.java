/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.axis.LogAxis
 *  org.jfree.chart.axis.TickUnitSource
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.axis;

import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.axis.ValueAxisConfiguration;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.TickUnitSource;
import sapphire.util.ConnectionInfo;
import sapphire.xml.PropertyList;

public final class LogAxisConfiguration
implements Serializable {
    private static final String DEFAULT_BASE = "10";
    private final ValueAxisConfiguration parent;
    private final TickUnitSource standardTickUnitSource;
    private final double base;
    private final NumberFormat numberFormatOverride;

    public LogAxisConfiguration(PropertyList logAxisProps, ConnectionInfo connectionInfo, ValueAxisConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (logAxisProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        Locale locale = I18nUtil.getConnectionLocale(connectionInfo);
        this.base = Double.valueOf(logAxisProps.getProperty("base", DEFAULT_BASE));
        this.standardTickUnitSource = LogAxis.createLogTickUnits((Locale)locale);
        String numberFormatOverrideString = logAxisProps.getProperty("numberformatoverride");
        this.numberFormatOverride = !numberFormatOverrideString.isEmpty() ? new DecimalFormat(numberFormatOverrideString) : null;
        this.parent = parent;
    }

    public LogAxisConfiguration(LogAxisConfiguration copy, ValueAxisConfiguration parent) {
        this.base = copy.base;
        this.parent = parent;
        this.standardTickUnitSource = copy.standardTickUnitSource;
        this.numberFormatOverride = (DecimalFormat)copy.numberFormatOverride.clone();
    }

    public boolean hasNumberFormatOverride() {
        return this.numberFormatOverride != null;
    }

    public NumberFormat getNumberFormatOverride() {
        if (this.numberFormatOverride == null) {
            throw new IllegalStateException("Number format override has not been set.");
        }
        return this.numberFormatOverride;
    }

    public TickUnitSource getStandardTickUnitSource() {
        return this.standardTickUnitSource;
    }

    public double getBase() {
        return this.base;
    }

    public ValueAxisConfiguration getParent() {
        return this.parent;
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

