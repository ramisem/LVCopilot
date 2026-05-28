/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.ExportDataConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.OpenURLConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.OperationConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.SetArgumentValueConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.ShowSDINotesConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class StandardOperationConfiguration
implements Serializable {
    private static final String DEFAULT_STANDARD_OPERATION_TYPE = StandardOperationType.EXPORT_DATA.getName();
    private final StandardOperationType standardOperationType;
    private final OperationConfiguration parent;
    private final ExportDataConfiguration exportDataConf;
    private final SetArgumentValueConfiguration setArgumentValueConf;
    private final OpenURLConfiguration openURLConf;
    private final ShowSDINotesConfiguration showSDINotesConf;

    public StandardOperationConfiguration(PropertyList standardOperationProps, OperationConfiguration parent) {
        if (standardOperationProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.parent = parent;
        this.standardOperationType = StandardOperationType.fromString(standardOperationProps.getProperty("standardoperationtype", DEFAULT_STANDARD_OPERATION_TYPE));
        this.exportDataConf = this.standardOperationType == StandardOperationType.EXPORT_DATA ? new ExportDataConfiguration(standardOperationProps.getPropertyListNotNull("exportdataprops"), this) : null;
        this.setArgumentValueConf = this.standardOperationType == StandardOperationType.SET_ARGUMENT_VALUE ? new SetArgumentValueConfiguration(standardOperationProps.getPropertyListNotNull("setargumentvalueprops"), this) : null;
        this.openURLConf = this.standardOperationType == StandardOperationType.OPEN_URL ? new OpenURLConfiguration(standardOperationProps.getPropertyListNotNull("openurlprops"), this) : null;
        this.showSDINotesConf = this.standardOperationType == StandardOperationType.SHOW_SDI_NOTES ? new ShowSDINotesConfiguration(standardOperationProps.getPropertyListNotNull("showsdinotesprops"), this) : null;
    }

    public StandardOperationConfiguration(StandardOperationConfiguration copy, OperationConfiguration parent) {
        this.parent = parent;
        this.standardOperationType = copy.standardOperationType;
        this.exportDataConf = copy.exportDataConf != null ? new ExportDataConfiguration(copy.exportDataConf, this) : null;
        this.setArgumentValueConf = copy.setArgumentValueConf != null ? new SetArgumentValueConfiguration(copy.setArgumentValueConf, this) : null;
        this.openURLConf = copy.openURLConf != null ? new OpenURLConfiguration(copy.openURLConf, this) : null;
        this.showSDINotesConf = copy.showSDINotesConf != null ? new ShowSDINotesConfiguration(copy.showSDINotesConf, this) : null;
    }

    public OperationConfiguration getParent() {
        return this.parent;
    }

    public StandardOperationType getStandardOperationType() {
        return this.standardOperationType;
    }

    public ExportDataConfiguration getExportDataConfiguration() {
        if (this.exportDataConf == null) {
            throw new IllegalStateException("Standard operation type is: " + (Object)((Object)this.standardOperationType));
        }
        return this.exportDataConf;
    }

    public OpenURLConfiguration getOpenURLConfiguration() {
        if (this.openURLConf == null) {
            throw new IllegalStateException("Standard operation type is: " + (Object)((Object)this.standardOperationType));
        }
        return this.openURLConf;
    }

    public ShowSDINotesConfiguration getShowSDINotesConfiguration() {
        if (this.showSDINotesConf == null) {
            throw new IllegalStateException("Standard operation type is: " + (Object)((Object)this.standardOperationType));
        }
        return this.showSDINotesConf;
    }

    public SetArgumentValueConfiguration getSetArgumentValueConfiguration() {
        if (this.setArgumentValueConf == null) {
            throw new IllegalStateException("Standard operation type is: " + (Object)((Object)this.standardOperationType));
        }
        return this.setArgumentValueConf;
    }

    public static enum StandardOperationType {
        EXPORT_DATA("Export Data"),
        REFRESH("Refresh"),
        SET_ARGUMENT_VALUE("Set Argument Value"),
        OPEN_URL("Open URL"),
        SHOW_SDI_NOTES("Show SDI Notes");

        private final String name;

        private StandardOperationType(String name) {
            this.name = name;
        }

        public static StandardOperationType fromString(String name) {
            if (name != null) {
                for (StandardOperationType type : StandardOperationType.values()) {
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

