/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.AccessControlAdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.AdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.AddSDINotesAdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.ColumnConfigurationAdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.DataItemParamListKeyAdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.ExtractBoundariesAdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.SDIDataItemAdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.SDISpecAdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.SequenceAdapterConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class StandardAdapterConfiguration
implements Serializable {
    private static final String DEFAULT_STANDARD_ADAPTER_TYPE = StandardAdapterType.NONE.getName();
    private final AdapterConfiguration parent;
    private final StandardAdapterType standardAdapterType;
    private final SDIDataItemAdapterConfiguration sdiDataItemAdapterConf;
    private final ExtractBoundariesAdapterConfiguration extractBoundariesAdapterConf;
    private final ColumnConfigurationAdapterConfiguration columnConfigurationAdapterConf;
    private final SDISpecAdapterConfiguration sdiSpecAdapterConfiguration;
    private final AccessControlAdapterConfiguration accessControlAdapterConf;
    private final SequenceAdapterConfiguration sequenceAdapterConf;
    private final AddSDINotesAdapterConfiguration addSDINotesAdapterConf;
    private final DataItemParamListKeyAdapterConfiguration dataItemParamListKeyAdapterConf;

    public StandardAdapterConfiguration(PropertyList standardAdapterProps, AdapterConfiguration parent) {
        if (standardAdapterProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent configuration is null");
        }
        this.parent = parent;
        this.standardAdapterType = StandardAdapterType.fromString(standardAdapterProps.getProperty("standardadaptertype", DEFAULT_STANDARD_ADAPTER_TYPE));
        if (this.standardAdapterType == StandardAdapterType.SDI_DATA_ITEM_ADAPTER) {
            PropertyList sdiDataItemAdapterProps = standardAdapterProps.getPropertyListNotNull("sdidataitemadapterprops");
            this.sdiDataItemAdapterConf = new SDIDataItemAdapterConfiguration(sdiDataItemAdapterProps, this);
        } else {
            this.sdiDataItemAdapterConf = null;
        }
        if (this.standardAdapterType == StandardAdapterType.EXTRACT_BOUNDARIES_ADAPTER) {
            PropertyList extractBoundariesAdapterProps = standardAdapterProps.getPropertyListNotNull("extractboundariesadapterprops");
            this.extractBoundariesAdapterConf = new ExtractBoundariesAdapterConfiguration(extractBoundariesAdapterProps, this);
        } else {
            this.extractBoundariesAdapterConf = null;
        }
        if (this.standardAdapterType == StandardAdapterType.COLUMN_CONFIGURATION_ADAPTER) {
            PropertyList columnConfigurationAdapterProps = standardAdapterProps.getPropertyListNotNull("columnconfigurationadapterprops");
            this.columnConfigurationAdapterConf = new ColumnConfigurationAdapterConfiguration(columnConfigurationAdapterProps, this);
        } else {
            this.columnConfigurationAdapterConf = null;
        }
        if (this.standardAdapterType == StandardAdapterType.SDI_SPEC_ADAPTER) {
            PropertyList sdiSpecAdapterProps = standardAdapterProps.getPropertyListNotNull("sdispecadapterprops");
            this.sdiSpecAdapterConfiguration = new SDISpecAdapterConfiguration(sdiSpecAdapterProps, this);
        } else {
            this.sdiSpecAdapterConfiguration = null;
        }
        this.accessControlAdapterConf = this.standardAdapterType == StandardAdapterType.ACCESS_CONTROL_ADAPTER ? new AccessControlAdapterConfiguration(standardAdapterProps.getPropertyListNotNull("accesscontroladapterprops"), this) : null;
        this.sequenceAdapterConf = this.standardAdapterType == StandardAdapterType.SEQUENCE ? new SequenceAdapterConfiguration(standardAdapterProps.getPropertyListNotNull("sequenceadapterprops"), this) : null;
        this.addSDINotesAdapterConf = this.standardAdapterType == StandardAdapterType.ADD_SDI_NOTES ? new AddSDINotesAdapterConfiguration(standardAdapterProps.getPropertyListNotNull("addsdinotesadapterprops"), this) : null;
        this.dataItemParamListKeyAdapterConf = this.standardAdapterType == StandardAdapterType.ADD_DATAITEM_PARAMLIST_KEY ? new DataItemParamListKeyAdapterConfiguration(standardAdapterProps.getPropertyListNotNull("dataitemparamlistkeyadapterprops"), this) : null;
    }

    public AdapterConfiguration getParent() {
        return this.parent;
    }

    public StandardAdapterType getStandardAdapterType() {
        return this.standardAdapterType;
    }

    public SDIDataItemAdapterConfiguration getSdiDataItemAdapterConfiguration() {
        if (this.standardAdapterType != StandardAdapterType.SDI_DATA_ITEM_ADAPTER) {
            throw new IllegalStateException("Adapter type is: " + (Object)((Object)this.standardAdapterType) + ", should be " + (Object)((Object)StandardAdapterType.SDI_DATA_ITEM_ADAPTER));
        }
        return this.sdiDataItemAdapterConf;
    }

    public ExtractBoundariesAdapterConfiguration getExtractBoundariesAdapterConfiguration() {
        if (this.standardAdapterType != StandardAdapterType.EXTRACT_BOUNDARIES_ADAPTER) {
            throw new IllegalStateException("Adapter type is: " + (Object)((Object)this.standardAdapterType) + ", should be " + (Object)((Object)StandardAdapterType.EXTRACT_BOUNDARIES_ADAPTER));
        }
        return this.extractBoundariesAdapterConf;
    }

    public ColumnConfigurationAdapterConfiguration getColumnConfigurationAdapterConfiguration() {
        if (this.standardAdapterType != StandardAdapterType.COLUMN_CONFIGURATION_ADAPTER) {
            throw new IllegalStateException("Adapter type is: " + (Object)((Object)this.standardAdapterType) + ", should be " + (Object)((Object)StandardAdapterType.COLUMN_CONFIGURATION_ADAPTER));
        }
        return this.columnConfigurationAdapterConf;
    }

    public SDISpecAdapterConfiguration getSdiSpecAdapterConfiguration() {
        if (this.standardAdapterType != StandardAdapterType.SDI_SPEC_ADAPTER) {
            throw new IllegalStateException("Adapter type is: " + (Object)((Object)this.standardAdapterType) + ", should be " + (Object)((Object)StandardAdapterType.SDI_SPEC_ADAPTER));
        }
        return this.sdiSpecAdapterConfiguration;
    }

    public SequenceAdapterConfiguration getSequenceAdapterConfiguration() {
        if (this.standardAdapterType != StandardAdapterType.SEQUENCE) {
            throw new IllegalStateException("Adapter type is: " + (Object)((Object)this.standardAdapterType) + ", should be " + (Object)((Object)StandardAdapterType.SEQUENCE));
        }
        return this.sequenceAdapterConf;
    }

    public AccessControlAdapterConfiguration getAccessControlAdapterConfiguration() {
        if (this.standardAdapterType != StandardAdapterType.ACCESS_CONTROL_ADAPTER) {
            throw new IllegalStateException("Adapter type is: " + (Object)((Object)this.standardAdapterType) + ", should be " + (Object)((Object)StandardAdapterType.ACCESS_CONTROL_ADAPTER));
        }
        return this.accessControlAdapterConf;
    }

    public AddSDINotesAdapterConfiguration getAddSDINotesAdapterConfiguration() {
        if (this.standardAdapterType != StandardAdapterType.ADD_SDI_NOTES) {
            throw new IllegalStateException("Adapter type is: " + (Object)((Object)this.standardAdapterType) + ", should be " + (Object)((Object)StandardAdapterType.ADD_SDI_NOTES));
        }
        return this.addSDINotesAdapterConf;
    }

    public DataItemParamListKeyAdapterConfiguration getDataItemParamListKeyAdapterConf() {
        return this.dataItemParamListKeyAdapterConf;
    }

    public static enum StandardAdapterType {
        SDI_DATA_ITEM_ADAPTER("Add SDI Data Items"),
        EXTRACT_BOUNDARIES_ADAPTER("Extract Boundaries"),
        COLUMN_CONFIGURATION_ADAPTER("Column Configuration"),
        SDI_SPEC_ADAPTER("Add SDI Spec"),
        ACCESS_CONTROL_ADAPTER("Access Control"),
        SEQUENCE("Sequence"),
        ADD_SDI_NOTES("Add SDI Notes"),
        ADD_DATAITEM_PARAMLIST_KEY("Find ParamListItem keys"),
        NONE("None");

        private final String name;

        private StandardAdapterType(String name) {
            this.name = name;
        }

        public static StandardAdapterType fromString(String name) {
            if (name != null) {
                for (StandardAdapterType type : StandardAdapterType.values()) {
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

