/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.data.Range
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.axis;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.axis.AxisConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.axis.DateAxisConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.axis.LogAxisConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.axis.NumberAxisConfiguration;
import java.io.Serializable;
import org.jfree.data.Range;
import sapphire.util.ConnectionInfo;
import sapphire.xml.PropertyList;

public final class ValueAxisConfiguration
implements Serializable {
    private static final String DEFAULT_VALUE_AXIS_TYPE = ValueAxisType.NUMBER.getName();
    private static final String DEFAULT_AUTO_TICK_UNIT_SELECTION = "Y";
    private static final String DEFAULT_AUTO_RANGE = "Y";
    private static final String DEFAULT_LOWER_MARGIN = String.valueOf(0.05);
    private static final String DEFAULT_UPPER_MARGIN = String.valueOf(0.05);
    private static final String DEFAULT_UPPER_RANGE = "1";
    private static final String DEFAULT_LOWER_RANGE = "0";
    private static final String DEFAULT_VERTICAL_TICKLABELS = "N";
    private final ValueAxisType valueAxisType;
    private final NumberAxisConfiguration numberAxisConf;
    private final DateAxisConfiguration dateAxisConf;
    private final AxisConfiguration parent;
    private final boolean autoTickUnitSelection;
    private final boolean autorangeYaxis;
    private final boolean verticalTicklabels;
    private final LogAxisConfiguration logAxisConf;
    private final double lowerMargin;
    private final double upperMargin;
    private final Range range;

    public ValueAxisConfiguration(PropertyList valueAxisProps, ConnectionInfo connectionInfo, AxisConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (valueAxisProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        if (connectionInfo == null) {
            throw new IllegalArgumentException("Connection info is null");
        }
        this.valueAxisType = ValueAxisType.fromString(valueAxisProps.getProperty("valueaxistype", DEFAULT_VALUE_AXIS_TYPE));
        this.numberAxisConf = this.valueAxisType == ValueAxisType.NUMBER ? new NumberAxisConfiguration(valueAxisProps.getPropertyListNotNull("numberaxisprops"), connectionInfo, this) : null;
        this.dateAxisConf = this.valueAxisType == ValueAxisType.DATE ? new DateAxisConfiguration(valueAxisProps.getPropertyListNotNull("dateaxisprops"), connectionInfo, this) : null;
        this.logAxisConf = this.valueAxisType == ValueAxisType.LOG ? new LogAxisConfiguration(valueAxisProps.getPropertyListNotNull("logaxisprops"), connectionInfo, this) : null;
        this.lowerMargin = Double.valueOf(valueAxisProps.getProperty("lowermargin", DEFAULT_LOWER_MARGIN));
        this.upperMargin = Double.valueOf(valueAxisProps.getProperty("uppermargin", DEFAULT_UPPER_MARGIN));
        this.verticalTicklabels = valueAxisProps.getProperty("verticalticklabels", DEFAULT_VERTICAL_TICKLABELS).toLowerCase().startsWith("y");
        this.range = new Range(Double.valueOf(valueAxisProps.getProperty("lowerrange", DEFAULT_LOWER_RANGE)).doubleValue(), Double.valueOf(valueAxisProps.getProperty("upperrange", DEFAULT_UPPER_RANGE)).doubleValue());
        this.autoTickUnitSelection = valueAxisProps.getProperty("autotickunitselection", "Y").toLowerCase().startsWith("y");
        this.autorangeYaxis = valueAxisProps.getProperty("autorangeyaxis", "Y").toLowerCase().startsWith("y");
        this.parent = parent;
    }

    public ValueAxisConfiguration(ValueAxisConfiguration copy, AxisConfiguration parent) {
        this.valueAxisType = copy.valueAxisType;
        this.numberAxisConf = copy.numberAxisConf != null ? new NumberAxisConfiguration(copy.numberAxisConf, this) : null;
        this.dateAxisConf = copy.dateAxisConf != null ? new DateAxisConfiguration(copy.dateAxisConf, this) : null;
        this.logAxisConf = copy.logAxisConf != null ? new LogAxisConfiguration(copy.logAxisConf, this) : null;
        this.lowerMargin = copy.lowerMargin;
        this.upperMargin = copy.upperMargin;
        this.range = copy.range;
        this.autoTickUnitSelection = copy.autoTickUnitSelection;
        this.autorangeYaxis = copy.autorangeYaxis;
        this.verticalTicklabels = copy.verticalTicklabels;
        this.parent = parent;
    }

    public double getLowerMargin() {
        return this.lowerMargin;
    }

    public double getUpperMargin() {
        return this.upperMargin;
    }

    public Range getRange() {
        return this.range;
    }

    public boolean isAutoTickUnitSelection() {
        return this.autoTickUnitSelection;
    }

    public boolean isAutorangeYaxis() {
        return this.autorangeYaxis;
    }

    public boolean isVerticalTicklabels() {
        return this.verticalTicklabels;
    }

    public AxisConfiguration getParent() {
        return this.parent;
    }

    public NumberAxisConfiguration getNumberAxisConfiguration() {
        if (this.numberAxisConf == null) {
            throw new IllegalStateException("Value axis type is: " + (Object)((Object)this.valueAxisType));
        }
        return this.numberAxisConf;
    }

    public DateAxisConfiguration getDateAxisConfiguration() {
        if (this.dateAxisConf == null) {
            throw new IllegalStateException("Axis type is: " + (Object)((Object)this.valueAxisType));
        }
        return this.dateAxisConf;
    }

    public ValueAxisType getValueAxisType() {
        return this.valueAxisType;
    }

    public LogAxisConfiguration getLogAxisConfiguration() {
        if (this.logAxisConf == null) {
            throw new IllegalStateException("Axis type is: " + (Object)((Object)this.valueAxisType));
        }
        return this.logAxisConf;
    }

    public static enum ValueAxisType {
        NUMBER("Number"),
        DATE("Date"),
        LOG("Log");

        private final String name;

        private ValueAxisType(String name) {
            this.name = name;
        }

        public static ValueAxisType fromString(String name) {
            if (name != null) {
                for (ValueAxisType type : ValueAxisType.values()) {
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

