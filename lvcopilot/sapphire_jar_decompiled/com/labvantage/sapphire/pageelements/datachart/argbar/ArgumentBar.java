/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.argbar;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.pageelements.datachart.argbar.Argument;
import com.labvantage.sapphire.pageelements.datachart.argbar.ArgumentGroup;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.ArgumentBarConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.ArgumentGroupConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.IncludeConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ArgumentBarBindingMap;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ArgumentBar
extends BaseCustom
implements Serializable {
    public static final String PROPERTY_VALUE = "value";
    public static final String PROPERTY_VALUE_SEPARATOR = "valueseparator";
    public static final String PROPERTY_DISPLAY_VALUE_SEPARATOR = "displayvalueseparator";
    public static final String PROPERTY_VALUE_COLLECTION = "valuecollection";
    public static final String PROPERTY_DISPLAY_VALUE_COLLECTION = "displayvaluecollection";
    public static final String PROPERTY_IS_DEFAULT_VALUE = "isdefaultvalue";
    public static final String PROPERTY_VALUE_PROPS = "valueprops";
    private final ArgumentBarConfiguration argumentBarConf;
    private final List<ArgumentGroup> argumentGroupList;

    public ArgumentBar(ArgumentBarConfiguration argumentBarConf, PropertyList requestParams, String connectionId, String requestId) throws SapphireException {
        if (argumentBarConf == null) {
            throw new IllegalArgumentException("Argument bar configuration is null");
        }
        if (requestParams == null) {
            throw new IllegalArgumentException("Request params is null");
        }
        if (connectionId == null || connectionId.isEmpty()) {
            throw new IllegalArgumentException("Connection ID is null or empty: " + connectionId);
        }
        this.setConnectionId(connectionId);
        this.argumentBarConf = argumentBarConf;
        this.argumentGroupList = new ArrayList<ArgumentGroup>();
        PropertyList argumentValueList = this.createArgumentValueList();
        for (ArgumentGroupConfiguration argumentGroupConf : argumentBarConf.getArgumentGroupConfigurationList()) {
            this.argumentGroupList.add(new ArgumentGroup(argumentGroupConf, connectionId, requestParams, argumentValueList, requestId));
        }
    }

    private PropertyList createArgumentValueList() {
        PropertyList argumentValueList = new PropertyList();
        argumentValueList.setProperty("currentuser", this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getSysuserId());
        return argumentValueList;
    }

    public PropertyList getArgumentValueList(PropertyList requestParams, String requestId) throws SapphireException {
        return this.getArgumentValueList(requestParams, new PropertyList(), requestId);
    }

    public PropertyList getArgumentValueList(PropertyList requestParams, PropertyList dataSetAdapterOutputProps, String requestId) throws SapphireException {
        if (requestParams == null) {
            throw new IllegalArgumentException("Request params is null");
        }
        PropertyList argumentValueList = this.createArgumentValueList();
        ArgumentBarBindingMap argumentBarBindingMap = new ArgumentBarBindingMap(argumentValueList, requestParams, dataSetAdapterOutputProps, this.getConnectionId());
        for (ArgumentGroup argumentGroup : this.argumentGroupList) {
            argumentGroup.populateArgumentValueList(argumentValueList, argumentBarBindingMap, requestId);
        }
        return argumentValueList;
    }

    public PropertyList getProps(ArgumentBarBindingMap bindingMap, String requestId) throws SapphireException {
        if (bindingMap == null) {
            throw new IllegalArgumentException("Binding map is null");
        }
        PropertyList argumentBarProps = new PropertyList();
        PropertyListCollection argumentGroupCollection = new PropertyListCollection();
        argumentBarProps.setProperty("argumentgroupcollection", argumentGroupCollection);
        for (ArgumentGroup argumentGroup : this.argumentGroupList) {
            argumentGroupCollection.add(argumentGroup.getProps(bindingMap, requestId));
        }
        argumentBarProps.setProperty("width", Integer.toString(this.argumentBarConf.getWidth()));
        argumentBarProps.setProperty("visible", this.argumentBarConf.getVisible().evaluateNoException(bindingMap));
        argumentBarProps.setProperty("refreshbuttonvisible", this.argumentBarConf.isRefreshButtonVisible() ? "Y" : "N");
        PropertyListCollection includeCollection = new PropertyListCollection();
        argumentBarProps.setProperty("includecollection", includeCollection);
        for (IncludeConfiguration includeConfiguration : this.argumentBarConf.getIncludeConfigurationList()) {
            PropertyList includeProps = new PropertyList();
            includeProps.setProperty("url", includeConfiguration.getUrl());
            includeCollection.add(includeProps);
        }
        return argumentBarProps;
    }

    public void applyChanges(PropertyList argumentBarProps, String requestId) {
        if (argumentBarProps == null) {
            throw new IllegalArgumentException("Argument bar props is null");
        }
        if (requestId == null) {
            throw new IllegalArgumentException("Request ID is null");
        }
        if (requestId.isEmpty()) {
            throw new IllegalArgumentException("Request ID is empty");
        }
        PropertyListCollection argumentGroupCollection = argumentBarProps.getCollectionNotNull("argumentgroupcollection");
        for (int i = 0; i < argumentGroupCollection.size(); ++i) {
            PropertyList argumentGroupProps = argumentGroupCollection.getPropertyList(i);
            String argumentGroupId = argumentGroupProps.getProperty("argumentgroupid");
            for (ArgumentGroup argumentGroup : this.argumentGroupList) {
                if (!argumentGroup.getArgumentGroupId().equals(argumentGroupId)) continue;
                argumentGroup.applyChanges(argumentGroupProps, requestId);
            }
        }
    }

    public ArgumentBarConfiguration getArgumentBarConfiguration() {
        return this.argumentBarConf;
    }

    public Argument getArgument(String argumentId) {
        if (argumentId == null) {
            throw new IllegalArgumentException("Argument ID is null");
        }
        if (argumentId.isEmpty()) {
            throw new IllegalArgumentException("Argument ID is empty");
        }
        for (ArgumentGroup argumentGroup : this.argumentGroupList) {
            Argument argument = argumentGroup.getArgument(argumentId);
            if (argument == null) continue;
            return argument;
        }
        throw new IllegalArgumentException("Argument with given ID not found: " + argumentId);
    }
}

