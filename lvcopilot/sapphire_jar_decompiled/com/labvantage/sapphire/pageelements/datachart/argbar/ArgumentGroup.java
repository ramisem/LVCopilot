/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.argbar;

import com.labvantage.sapphire.pageelements.datachart.argbar.AbstractArgument;
import com.labvantage.sapphire.pageelements.datachart.argbar.Argument;
import com.labvantage.sapphire.pageelements.datachart.argbar.DropDownArgument;
import com.labvantage.sapphire.pageelements.datachart.argbar.EditorStyleArgument;
import com.labvantage.sapphire.pageelements.datachart.argbar.HiddenArgument;
import com.labvantage.sapphire.pageelements.datachart.argbar.JavaScriptArgument;
import com.labvantage.sapphire.pageelements.datachart.argbar.NumberArgument;
import com.labvantage.sapphire.pageelements.datachart.argbar.TextArgument;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.ArgumentConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.ArgumentGroupConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ArgumentBarBindingMap;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ArgumentGroup
implements Serializable {
    private final ArgumentGroupConfiguration argumentGroupConf;
    private final List<Argument> argumentList;
    private final String argumentGroupId;

    public ArgumentGroup(ArgumentGroupConfiguration argumentGroupConf, String connectionId, PropertyList requestParams, PropertyList argumentValueList, String requestId) throws SapphireException {
        if (argumentGroupConf == null) {
            throw new IllegalArgumentException("Source configuration is null");
        }
        if (connectionId == null) {
            throw new IllegalArgumentException("Connection ID is null");
        }
        if (connectionId.isEmpty()) {
            throw new IllegalArgumentException("Connection ID is empty");
        }
        if (requestParams == null) {
            throw new IllegalArgumentException("Request params is null");
        }
        if (argumentValueList == null) {
            throw new IllegalArgumentException("Argument value list is null");
        }
        this.argumentGroupConf = argumentGroupConf;
        this.argumentList = new ArrayList<Argument>();
        this.argumentGroupId = argumentGroupConf.getArgumentGroupId();
        for (ArgumentConfiguration argumentConf : argumentGroupConf.getArgumentConfigurationList()) {
            AbstractArgument argument;
            ArgumentConfiguration.ArgumentType argumentType = argumentConf.getArgumentType();
            if (argumentType == ArgumentConfiguration.ArgumentType.DROP_DOWN_ARGUMENT) {
                argument = new DropDownArgument(argumentConf.getDropDownArgumentConfiguration(), connectionId, requestParams, argumentValueList, requestId);
            } else if (argumentType == ArgumentConfiguration.ArgumentType.TEXT_ARGUMENT) {
                argument = new TextArgument(argumentConf.getTextArgumentConfiguration(), connectionId, requestParams, argumentValueList, requestId);
            } else if (argumentType == ArgumentConfiguration.ArgumentType.HIDDEN_ARGUMENT) {
                argument = new HiddenArgument(argumentConf, connectionId, requestParams, argumentValueList, requestId);
            } else if (argumentType == ArgumentConfiguration.ArgumentType.NUMBER_ARGUMENT) {
                argument = new NumberArgument(argumentConf.getNumberArgumentConfiguration(), connectionId, requestParams, argumentValueList, requestId);
            } else if (argumentType == ArgumentConfiguration.ArgumentType.JAVA_SCRIPT_ARGUMENT) {
                argument = new JavaScriptArgument(argumentConf.getJavaScriptArgumentConfiguration(), connectionId, requestParams, argumentValueList, requestId);
            } else if (argumentType == ArgumentConfiguration.ArgumentType.EDITOR_STYLE_ARGUMENT) {
                argument = new EditorStyleArgument(argumentConf.getEditorStyleArgumentConfiguration(), connectionId, requestParams, argumentValueList, requestId);
            } else {
                throw new IllegalArgumentException("Unknown argument type: " + (Object)((Object)argumentType));
            }
            this.argumentList.add(argument);
        }
    }

    public PropertyList getProps(ArgumentBarBindingMap bindingMap, String requestId) throws SapphireException {
        PropertyList argumentGroupProps = new PropertyList();
        argumentGroupProps.setProperty("argumentgroupid", this.argumentGroupConf.getArgumentGroupId());
        argumentGroupProps.setProperty("title", this.argumentGroupConf.getTitle());
        argumentGroupProps.setProperty("visible", this.argumentGroupConf.getVisible().evaluate(bindingMap) != false ? "Y" : "N");
        PropertyListCollection argumentCollection = new PropertyListCollection();
        argumentGroupProps.setProperty("argumentcollection", argumentCollection);
        for (Argument argument : this.argumentList) {
            argumentCollection.add(argument.getProps(bindingMap, requestId));
        }
        return argumentGroupProps;
    }

    public String getArgumentGroupId() {
        return this.argumentGroupId;
    }

    public void applyChanges(PropertyList argumentGroupProps, String requestId) {
        if (argumentGroupProps == null) {
            throw new IllegalArgumentException("Argument group properties is null");
        }
        if (requestId == null) {
            throw new IllegalArgumentException("Request ID is null");
        }
        if (requestId.isEmpty()) {
            throw new IllegalArgumentException("Request ID is empty");
        }
        PropertyListCollection argumentCollection = argumentGroupProps.getCollectionNotNull("argumentcollection");
        for (int i = 0; i < argumentCollection.size(); ++i) {
            PropertyList argumentProps = argumentCollection.getPropertyList(i);
            String argumentId = argumentProps.getProperty("argumentid");
            for (Argument argument : this.argumentList) {
                if (!argument.getArgumentId().equals(argumentId)) continue;
                argument.applyChanges(argumentProps, requestId);
            }
        }
    }

    public Argument getArgument(String argumentId) {
        if (argumentId == null) {
            throw new IllegalArgumentException("Argument ID is null");
        }
        if (argumentId.isEmpty()) {
            throw new IllegalArgumentException("Argument ID is empty");
        }
        for (Argument argument : this.argumentList) {
            if (!argument.getArgumentId().equals(argumentId)) continue;
            return argument;
        }
        return null;
    }

    public void populateArgumentValueList(PropertyList argumentValueList, ArgumentBarBindingMap argumentBarBindingMap, String requestId) throws SapphireException {
        if (argumentBarBindingMap == null) {
            throw new IllegalArgumentException("Argument bar binding map is null");
        }
        if (argumentValueList == null) {
            throw new IllegalArgumentException("Argument value list is null");
        }
        for (Argument argument : this.argumentList) {
            argument.populateArgumentValueList(argumentValueList, argumentBarBindingMap, requestId);
        }
    }
}

