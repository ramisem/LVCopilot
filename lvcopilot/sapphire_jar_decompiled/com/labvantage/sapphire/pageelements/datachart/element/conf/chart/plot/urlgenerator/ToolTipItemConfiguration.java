/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator.ToolTipEventConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class ToolTipItemConfiguration
implements Serializable {
    private final ToolTipEventConfiguration parent;
    private final String columnId;
    private final String title;
    private final boolean translate;
    private final String displayValue;
    private final String dateFormat;
    private final String timeFormat;
    private final String numberFormat;
    private final boolean enabled;

    public ToolTipItemConfiguration(PropertyList toolTipItemProps, ToolTipEventConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (toolTipItemProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.columnId = toolTipItemProps.getProperty("columnId", "");
        this.title = toolTipItemProps.getProperty("title", "");
        this.translate = toolTipItemProps.getProperty("translate", "").equals("Y");
        this.displayValue = toolTipItemProps.getProperty("displayvalue", "");
        this.dateFormat = toolTipItemProps.getProperty("dateformat", "");
        this.timeFormat = toolTipItemProps.getProperty("timeformat", "");
        this.numberFormat = toolTipItemProps.getProperty("numberformat", "");
        this.enabled = toolTipItemProps.getProperty("enabled", "Y").equals("Y");
        this.parent = parent;
    }

    public ToolTipItemConfiguration(ToolTipItemConfiguration copy, ToolTipEventConfiguration parent) {
        this.parent = parent;
        this.columnId = copy.columnId;
        this.title = copy.title;
        this.translate = copy.translate;
        this.displayValue = copy.displayValue;
        this.dateFormat = copy.dateFormat;
        this.timeFormat = copy.timeFormat;
        this.numberFormat = copy.numberFormat;
        this.enabled = copy.enabled;
    }

    public String getColumnId() {
        return this.columnId;
    }

    public String getTitle() {
        return this.title;
    }

    public boolean doTranslate() {
        return this.translate;
    }

    public String getDisplayValue() {
        return this.displayValue;
    }

    public int getDateFormat() {
        int retVal = this.dateFormat.equalsIgnoreCase("short") ? 3 : (this.dateFormat.equalsIgnoreCase("medium") ? 2 : (this.dateFormat.equalsIgnoreCase("long") ? 1 : 3));
        return retVal;
    }

    public int getTimeFormat() {
        int retVal = this.timeFormat.equalsIgnoreCase("short") ? 3 : (this.timeFormat.equalsIgnoreCase("medium") ? 2 : (this.timeFormat.equalsIgnoreCase("long") ? 1 : -1));
        return retVal;
    }

    public String getNumberFormat() {
        return this.numberFormat;
    }

    public ToolTipEventConfiguration getParent() {
        return this.parent;
    }
}

