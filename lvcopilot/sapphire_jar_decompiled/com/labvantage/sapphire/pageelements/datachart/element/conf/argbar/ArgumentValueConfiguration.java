/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.argbar;

import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.ArgumentConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.SQLValueConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.TextValueConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class ArgumentValueConfiguration
implements Serializable {
    private static final String DEFAULT_VALUE_TYPE = ArgumentValueType.TEXT.getName();
    private static final String DEFAULT_VALUE_SEPARATOR = "','";
    private static final String DEFAULT_DISPLAY_VALUE_SEPARATOR = ";";
    private final ArgumentConfiguration parent;
    private final ArgumentValueType argumentValueType;
    private final SQLValueConfiguration sqlValueConf;
    private final TextValueConfiguration textValueConf;
    private final String valueSeparator;
    private final String displayValueSeparator;

    public ArgumentValueConfiguration(PropertyList argumentValueProps, ArgumentConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (argumentValueProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.argumentValueType = ArgumentValueType.fromString(argumentValueProps.getProperty("valuetype", DEFAULT_VALUE_TYPE));
        this.valueSeparator = argumentValueProps.getProperty("valueseparator", DEFAULT_VALUE_SEPARATOR);
        this.displayValueSeparator = argumentValueProps.getProperty("displayvalueseparator", DEFAULT_DISPLAY_VALUE_SEPARATOR);
        this.textValueConf = this.argumentValueType == ArgumentValueType.TEXT ? new TextValueConfiguration(argumentValueProps.getPropertyListNotNull("valueprops"), this) : null;
        this.sqlValueConf = this.argumentValueType == ArgumentValueType.SQL ? new SQLValueConfiguration(argumentValueProps.getPropertyListNotNull("sqlvalueprops"), this) : null;
        this.parent = parent;
    }

    public ArgumentConfiguration getParent() {
        return this.parent;
    }

    public ArgumentValueType getArgumentValueType() {
        return this.argumentValueType;
    }

    public SQLValueConfiguration getSqlValueConfiguration() {
        if (this.sqlValueConf == null) {
            throw new IllegalStateException("Argument value type is: " + (Object)((Object)this.argumentValueType));
        }
        return this.sqlValueConf;
    }

    public TextValueConfiguration getTextValueConfiguration() {
        if (this.textValueConf == null) {
            throw new IllegalStateException("Argument value type is: " + (Object)((Object)this.argumentValueType));
        }
        return this.textValueConf;
    }

    public String getValueSeparator() {
        return this.valueSeparator;
    }

    public String getDisplayValueSeparator() {
        return this.displayValueSeparator;
    }

    public static enum ArgumentValueType {
        SQL("SQL"),
        TEXT("Text");

        private final String name;

        private ArgumentValueType(String name) {
            this.name = name;
        }

        public static ArgumentValueType fromString(String name) {
            if (name != null) {
                for (ArgumentValueType type : ArgumentValueType.values()) {
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

