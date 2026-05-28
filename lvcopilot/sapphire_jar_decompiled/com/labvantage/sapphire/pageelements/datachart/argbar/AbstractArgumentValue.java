/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.argbar;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.pageelements.datachart.argbar.Argument;
import com.labvantage.sapphire.pageelements.datachart.argbar.ArgumentValue;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.ArgumentConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.ArgumentValueConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ArgumentBarBindingMap;
import java.io.Serializable;
import java.util.List;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public abstract class AbstractArgumentValue
extends BaseCustom
implements ArgumentValue,
Serializable {
    private List<String> valueList;
    private List<String> displayValueList;
    private final String valueSeparator;
    private final String displayValueSeparator;
    private final Argument parent;
    private String requestId;

    public AbstractArgumentValue(String connectionId, ArgumentValueConfiguration argumentValueConf, Argument parent) {
        if (connectionId == null) {
            throw new IllegalArgumentException("Connection ID is null");
        }
        if (connectionId.isEmpty()) {
            throw new IllegalArgumentException("Connection ID is empty");
        }
        if (argumentValueConf == null) {
            throw new IllegalArgumentException("Source configuration is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.valueList = null;
        this.displayValueList = null;
        this.requestId = null;
        this.valueSeparator = argumentValueConf.getValueSeparator();
        this.displayValueSeparator = argumentValueConf.getDisplayValueSeparator();
        this.parent = parent;
        this.setConnectionId(connectionId);
    }

    @Override
    public List<String> getValues(String requestId) {
        if (requestId == null) {
            throw new IllegalArgumentException("Request ID is null");
        }
        Object returnValue = this.requestId == null ? null : (this.parent.getArgumentConfiguration().getArgumentValueScope() == ArgumentConfiguration.ArgumentValueScope.REQUEST && !this.requestId.equals(requestId) ? null : this.valueList);
        return returnValue;
    }

    @Override
    public List<String> getDisplayValues(String requestId) {
        if (requestId == null) {
            throw new IllegalArgumentException("Request ID is null");
        }
        Object returnValue = this.requestId == null ? null : (this.parent.getArgumentConfiguration().getArgumentValueScope() == ArgumentConfiguration.ArgumentValueScope.REQUEST && !this.requestId.equals(requestId) ? null : this.displayValueList);
        return returnValue;
    }

    @Override
    public PropertyList getProps(ArgumentBarBindingMap argumentBarBindingMap, String requestId) throws SapphireException {
        if (argumentBarBindingMap == null) {
            throw new IllegalArgumentException("Argument binding map is null");
        }
        if (requestId == null) {
            throw new IllegalArgumentException("Request ID is null");
        }
        if (requestId.isEmpty()) {
            throw new IllegalArgumentException("Request ID is empty");
        }
        PropertyList argumentValueProps = new PropertyList();
        argumentValueProps.setProperty("valueseparator", this.valueSeparator);
        argumentValueProps.setProperty("displayvalueseparator", this.displayValueSeparator);
        boolean isDefaultValue = false;
        List<String> values = this.getValues(requestId);
        List<String> displayValues = this.getDisplayValues(requestId);
        if (values == null) {
            values = this.getDefaultValues(argumentBarBindingMap);
            displayValues = this.parent.createDisplayValueList(values);
            isDefaultValue = true;
        }
        PropertyListCollection valueCollection = new PropertyListCollection();
        for (String value : values) {
            PropertyList valueProps = new PropertyList();
            valueProps.setProperty("value", value);
            valueCollection.add(valueProps);
        }
        PropertyListCollection displayValueCollection = new PropertyListCollection();
        for (String displayValue : displayValues) {
            PropertyList displayValueProps = new PropertyList();
            displayValueProps.setProperty("value", displayValue);
            displayValueCollection.add(displayValueProps);
        }
        argumentValueProps.setProperty("valuecollection", valueCollection);
        argumentValueProps.setProperty("displayvaluecollection", displayValueCollection);
        argumentValueProps.setProperty("isdefaultvalue", isDefaultValue ? "Y" : "N");
        return argumentValueProps;
    }

    @Override
    public void setValueList(List<String> valueList, List<String> displayValueList, String requestId) {
        if (valueList == null) {
            throw new IllegalArgumentException("Value list is null");
        }
        if (displayValueList == null) {
            throw new IllegalArgumentException("Display value list is null");
        }
        if (requestId == null) {
            throw new IllegalArgumentException("Request ID is null");
        }
        if (requestId.isEmpty()) {
            throw new IllegalArgumentException("Request ID is empty");
        }
        this.requestId = requestId;
        this.valueList = valueList;
        this.displayValueList = displayValueList;
    }
}

