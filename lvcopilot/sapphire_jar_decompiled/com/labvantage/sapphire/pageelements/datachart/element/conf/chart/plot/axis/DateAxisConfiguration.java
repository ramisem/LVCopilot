/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.axis;

import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.axis.ValueAxisConfiguration;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Locale;
import java.util.TimeZone;
import sapphire.util.ConnectionInfo;
import sapphire.xml.PropertyList;

public class DateAxisConfiguration
implements Serializable {
    private static final String DEFAULT_DATE_FORMAT = "short";
    private final ValueAxisConfiguration parent;
    private final DateFormat dateFormatOverride;

    public DateAxisConfiguration(PropertyList dateAxisProps, ConnectionInfo connectionInfo, ValueAxisConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (dateAxisProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        Locale locale = I18nUtil.getConnectionLocale(connectionInfo);
        TimeZone timeZone = I18nUtil.getConnectionTimeZone(connectionInfo);
        String dateFormatStr = dateAxisProps.getProperty("dateformatoverride");
        String timeFormatStr = dateAxisProps.getProperty("timeformatoverride");
        if (!dateFormatStr.isEmpty() || !timeFormatStr.isEmpty()) {
            if (dateFormatStr.length() == 0) {
                dateFormatStr = DEFAULT_DATE_FORMAT;
            }
            FormatStyle dateFormatStyle = FormatStyle.fromString(dateFormatStr);
            if (timeFormatStr.length() == 0) {
                this.dateFormatOverride = DateFormat.getDateInstance(dateFormatStyle.getDateFormat(), locale);
            } else {
                FormatStyle timeFormatStyle = FormatStyle.fromString(timeFormatStr);
                this.dateFormatOverride = DateFormat.getDateTimeInstance(dateFormatStyle.getDateFormat(), timeFormatStyle.getDateFormat(), locale);
            }
            this.dateFormatOverride.setTimeZone(timeZone);
        } else {
            this.dateFormatOverride = null;
        }
        this.parent = parent;
    }

    public DateAxisConfiguration(DateAxisConfiguration copy, ValueAxisConfiguration parent) {
        this.parent = parent;
        this.dateFormatOverride = copy.dateFormatOverride;
    }

    public DateFormat getDateFormatOverride() {
        if (this.dateFormatOverride == null) {
            throw new IllegalStateException("Date format override has not been set.");
        }
        return this.dateFormatOverride;
    }

    public boolean hasDateFormatOverride() {
        return this.dateFormatOverride != null;
    }

    public ValueAxisConfiguration getParent() {
        return this.parent;
    }

    public static enum FormatStyle {
        SHORT("Short", 3),
        MEDIUM("Medium", 2),
        LONG("Long", 1);

        private final int dateFormat;
        private final String name;

        private FormatStyle(String name, int dateFormat) {
            this.name = name;
            this.dateFormat = dateFormat;
        }

        public static FormatStyle fromString(String name) {
            if (name != null) {
                for (FormatStyle type : FormatStyle.values()) {
                    if (!name.equalsIgnoreCase(type.name)) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown name: " + name);
        }

        public String getName() {
            return this.name;
        }

        public int getDateFormat() {
            return this.dateFormat;
        }
    }
}

