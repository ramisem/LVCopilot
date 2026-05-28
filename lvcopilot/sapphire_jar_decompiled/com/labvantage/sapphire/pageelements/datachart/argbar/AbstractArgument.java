/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.argbar;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.pageelements.datachart.argbar.Argument;
import com.labvantage.sapphire.pageelements.datachart.argbar.ArgumentValue;
import com.labvantage.sapphire.pageelements.datachart.argbar.SQLArgumentValue;
import com.labvantage.sapphire.pageelements.datachart.argbar.TextArgumentValue;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.ArgumentConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.ArgumentValueConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ArgumentBarBindingMap;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public abstract class AbstractArgument
extends BaseCustom
implements Argument,
Serializable {
    private final String valueSeparator;
    private final String displayValueSeparator;
    private final ArgumentConfiguration argumentConf;
    private final String argumentId;
    private ArgumentValue argumentValue;
    private boolean cascadedArgument;
    private Set<String> cascadedArgumentInputs = new HashSet<String>();

    public AbstractArgument(String connectionId, ArgumentConfiguration argumentConf, ArgumentBarBindingMap argumentBarBindingMap, PropertyList argumentValueList, String requestId) throws SapphireException {
        if (connectionId == null) {
            throw new IllegalArgumentException("Connection ID is null");
        }
        if (connectionId.isEmpty()) {
            throw new IllegalArgumentException("Connection ID is empty");
        }
        if (argumentConf == null) {
            throw new IllegalArgumentException("Source configuration is null");
        }
        if (argumentBarBindingMap == null) {
            throw new IllegalArgumentException("Binding map is null");
        }
        if (argumentValueList == null) {
            throw new IllegalArgumentException("Argument value list is null");
        }
        this.setConnectionId(connectionId);
        this.argumentId = argumentConf.getArgumentId();
        ArgumentValueConfiguration argumentValueConf = argumentConf.getArgumentValueConfiguration();
        ArgumentValueConfiguration.ArgumentValueType argumentValueType = argumentValueConf.getArgumentValueType();
        if (argumentValueType == ArgumentValueConfiguration.ArgumentValueType.TEXT) {
            this.argumentValue = new TextArgumentValue(argumentValueConf.getTextValueConfiguration(), connectionId, argumentBarBindingMap, this);
        } else if (argumentValueType == ArgumentValueConfiguration.ArgumentValueType.SQL) {
            this.argumentValue = new SQLArgumentValue(argumentValueConf.getSqlValueConfiguration(), connectionId, argumentBarBindingMap, this);
        } else {
            throw new IllegalArgumentException("Unknown argument value type: " + (Object)((Object)argumentValueType));
        }
        this.argumentConf = argumentConf;
        this.valueSeparator = argumentValueConf.getValueSeparator();
        this.displayValueSeparator = argumentValueConf.getDisplayValueSeparator();
        this.populateArgumentValueList(argumentValueList, argumentBarBindingMap, requestId);
        this.cascadedArgument = false;
    }

    public void setCascadedArgument(boolean cascadedArgument) {
        this.cascadedArgument = cascadedArgument;
    }

    public ArgumentValue getArgumentValue() {
        return this.argumentValue;
    }

    public void setCascadedArgumentInputs(Set<String> cascadedArgumentInputs) {
        this.cascadedArgumentInputs.addAll(cascadedArgumentInputs);
    }

    @Override
    public void populateArgumentValueList(PropertyList argumentValueList, ArgumentBarBindingMap argumentBarBindingMap, String requestId) throws SapphireException {
        if (argumentValueList == null) {
            throw new IllegalArgumentException("Argument value list is null");
        }
        if (argumentBarBindingMap == null) {
            throw new IllegalArgumentException("Argument bar binding map is null");
        }
        if (requestId == null) {
            throw new IllegalArgumentException("Request ID is null");
        }
        List<String> valueList = this.argumentValue.getValues(requestId);
        if (valueList == null) {
            valueList = this.argumentValue.getDefaultValues(argumentBarBindingMap);
        }
        PropertyList argumentValueProps = new PropertyList();
        PropertyListCollection valueCollection = new PropertyListCollection();
        for (String value : valueList) {
            PropertyList valueProps = new PropertyList();
            valueProps.setProperty("value", value);
            valueCollection.add(valueProps);
        }
        argumentValueProps.setProperty("valueseparator", this.valueSeparator);
        argumentValueProps.setProperty("displayvalueseparator", this.displayValueSeparator);
        argumentValueProps.setProperty("valuecollection", valueCollection);
        argumentValueList.setProperty(this.argumentId, argumentValueProps);
    }

    @Override
    public PropertyList getProps(ArgumentBarBindingMap bindingMap, String requestId) throws SapphireException {
        if (bindingMap == null) {
            throw new IllegalArgumentException("Binding map is null");
        }
        PropertyList props = new PropertyList();
        props.setProperty("argumentid", this.argumentConf.getArgumentId());
        props.setProperty("argumenttype", this.argumentConf.getArgumentType().getName());
        props.setProperty("title", this.argumentConf.getTitle());
        props.setProperty("width", this.argumentConf.getWidth());
        props.setProperty("tip", this.argumentConf.getTip());
        props.setProperty("valueprops", this.argumentValue.getProps(bindingMap, requestId));
        props.setProperty("cascadedargument", this.cascadedArgument ? "Y" : "N");
        StringBuilder argumentString = new StringBuilder();
        for (String argument : this.cascadedArgumentInputs) {
            if (argumentString.length() > 0) {
                argumentString.append(";");
            }
            argumentString.append(argument);
        }
        props.setProperty("cascadedargumentinputs", argumentString.toString());
        return props;
    }

    @Override
    public String getArgumentId() {
        return this.argumentId;
    }

    @Override
    public void applyChanges(PropertyList argumentProps, String requestId) {
        if (argumentProps == null) {
            throw new IllegalArgumentException("Argument props is null");
        }
        if (requestId == null) {
            throw new IllegalArgumentException("Request ID is null");
        }
        if (requestId.isEmpty()) {
            throw new IllegalArgumentException("Request ID is empty");
        }
        PropertyList valuesProps = argumentProps.getPropertyListNotNull("valueprops");
        boolean isDefaultValue = valuesProps.getProperty("isdefaultvalue").toLowerCase().startsWith("y");
        PropertyListCollection displayValueCollection = valuesProps.getCollectionNotNull("displayvaluecollection");
        if (!isDefaultValue) {
            ArrayList<String> displayValueList = new ArrayList<String>();
            for (int i = 0; i < displayValueCollection.size(); ++i) {
                PropertyList displayValueProps = displayValueCollection.getPropertyList(i);
                displayValueList.add(displayValueProps.getProperty("value"));
            }
            List<String> valueList = this.createValueList(displayValueList);
            this.argumentValue.setValueList(valueList, displayValueList, requestId);
        }
    }

    @Override
    public List<String> createValueList(List<String> displayValueList) {
        if (displayValueList == null) {
            throw new IllegalArgumentException("Display value list is null");
        }
        return new ArrayList<String>(displayValueList);
    }

    @Override
    public List<String> createDisplayValueList(List<String> valueList) {
        if (valueList == null) {
            throw new IllegalArgumentException("Value list is null");
        }
        return new ArrayList<String>(valueList);
    }

    @Override
    public void setArgumentValue(String valueString, String requestId) {
        if (valueString == null) {
            throw new IllegalArgumentException("Value is null");
        }
        String valueSeparator = this.argumentConf.getArgumentValueConfiguration().getValueSeparator();
        List<String> valueList = Arrays.asList(valueString.split(valueSeparator));
        this.argumentValue.setValueList(valueList, new ArrayList<String>(valueList), requestId);
    }

    @Override
    public ArgumentConfiguration getArgumentConfiguration() {
        return this.argumentConf;
    }
}

