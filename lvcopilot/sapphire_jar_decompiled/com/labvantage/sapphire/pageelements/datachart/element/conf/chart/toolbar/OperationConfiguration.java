/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.StandardOperationConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class OperationConfiguration
implements Serializable {
    private static final String DEFAULT_JAVA_SCRIPT = "";
    private static final String DEFAULT_OPERATION_TYPE = OperationType.STANDARD.getName();
    private final OperationType operationType;
    private final StandardOperationConfiguration standardOperationConf;
    private final StringExpression javaScript;

    public OperationConfiguration(PropertyList operationProps) {
        if (operationProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.operationType = OperationType.fromString(operationProps.getProperty("operationtype", DEFAULT_OPERATION_TYPE));
        this.standardOperationConf = this.operationType == OperationType.STANDARD ? new StandardOperationConfiguration(operationProps.getPropertyListNotNull("standardoperationprops"), this) : null;
        this.javaScript = new StringExpression(operationProps.getPropertyListNotNull("javascriptoperationprops").getProperty("javascript", DEFAULT_JAVA_SCRIPT));
    }

    public OperationConfiguration(OperationConfiguration copy) {
        this.operationType = copy.operationType;
        this.standardOperationConf = copy.standardOperationConf != null ? new StandardOperationConfiguration(copy.standardOperationConf, this) : null;
        this.javaScript = copy.javaScript;
    }

    public StringExpression getJavaScript() {
        return this.javaScript;
    }

    public OperationType getOperationType() {
        return this.operationType;
    }

    public StandardOperationConfiguration getStandardOperationConfiguration() {
        if (this.standardOperationConf == null) {
            throw new IllegalStateException("Operation type is: " + (Object)((Object)this.operationType));
        }
        return this.standardOperationConf;
    }

    public static enum OperationType {
        STANDARD("Standard"),
        JAVA_SCRIPT("JavaScript");

        private final String name;

        private OperationType(String name) {
            this.name = name;
        }

        public static OperationType fromString(String name) {
            if (name != null) {
                for (OperationType type : OperationType.values()) {
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

