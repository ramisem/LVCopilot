/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.AbstractIntervalXYSeriesGroupConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.SeriesGroupBuilderConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class TimePeriodValuesCollectionConfiguration
extends AbstractIntervalXYSeriesGroupConfiguration
implements Serializable {
    private static final String DEFAULT_TIME_PERIOD_TYPE = TimePeriodType.MILLISECOND.getName();
    private final TimePeriodType timePeriodType;

    public TimePeriodValuesCollectionConfiguration(PropertyList timePeriodValuesCollectionProps, SeriesGroupBuilderConfiguration parent) {
        super(timePeriodValuesCollectionProps, parent);
        if (timePeriodValuesCollectionProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        this.timePeriodType = TimePeriodType.fromString(timePeriodValuesCollectionProps.getProperty("timeperiodtype", DEFAULT_TIME_PERIOD_TYPE));
    }

    public TimePeriodType getTimePeriodType() {
        return this.timePeriodType;
    }

    public static enum TimePeriodType {
        YEAR("Year"),
        QUARTER("Quarter"),
        MONTH("Month"),
        WEEK("Week"),
        DAY("Day"),
        HOUR("Hour"),
        MINUTE("Minute"),
        SECOND("Second"),
        MILLISECOND("Millisecond");

        private final String name;

        private TimePeriodType(String name) {
            this.name = name;
        }

        public static TimePeriodType fromString(String name) {
            if (name != null) {
                for (TimePeriodType type : TimePeriodType.values()) {
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

