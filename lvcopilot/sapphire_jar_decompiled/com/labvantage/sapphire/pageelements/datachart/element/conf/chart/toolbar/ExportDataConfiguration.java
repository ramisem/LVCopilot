/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.StandardOperationConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class ExportDataConfiguration
implements Serializable {
    private static final String DEFAULT_EXPORT_TYPE = ExportType.EXCEL.getName();
    private final StandardOperationConfiguration parent;
    private final ExportType exportType;

    public ExportDataConfiguration(PropertyList exportDataProps, StandardOperationConfiguration parent) {
        if (exportDataProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.parent = parent;
        this.exportType = ExportType.fromString(exportDataProps.getProperty("exporttype", DEFAULT_EXPORT_TYPE));
    }

    public ExportDataConfiguration(ExportDataConfiguration copy, StandardOperationConfiguration parent) {
        this.parent = parent;
        this.exportType = copy.exportType;
    }

    public StandardOperationConfiguration getParent() {
        return this.parent;
    }

    public String getJavaScript(ChartBindingMap chartBindingMap) {
        return chartBindingMap.getDataChartJsObject() + ".standardOperation.exportData('" + (Object)((Object)this.exportType) + "');";
    }

    public static enum ExportType {
        EXCEL("Excel");

        private final String name;

        private ExportType(String name) {
            this.name = name;
        }

        public static ExportType fromString(String name) {
            if (name != null) {
                for (ExportType type : ExportType.values()) {
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

