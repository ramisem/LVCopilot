/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.dataexport;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.ChartConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.dataexport.ExcelExportConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class DataExportConfiguration
implements Serializable {
    private final ChartConfiguration parent;
    private final ExcelExportConfiguration excelExportConf;

    public DataExportConfiguration(PropertyList dataExportProps, ChartConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (dataExportProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.excelExportConf = new ExcelExportConfiguration(dataExportProps.getPropertyListNotNull("excelexportprops"), this);
        this.parent = parent;
    }

    public ChartConfiguration getParent() {
        return this.parent;
    }

    public ExcelExportConfiguration getExcelExportConfiguration() {
        return this.excelExportConf;
    }
}

