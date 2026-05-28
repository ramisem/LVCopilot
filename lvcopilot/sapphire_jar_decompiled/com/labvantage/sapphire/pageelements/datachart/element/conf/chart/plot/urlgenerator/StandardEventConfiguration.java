/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator.EventConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator.ExpressionToolTipEventConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator.OpenMenuEventConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator.OpenURLEventConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator.ToolTipEventConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class StandardEventConfiguration
implements Serializable {
    private static final String DEFAULT_STANDARD_EVENT_TYPE = StandardEventType.TOOL_TIP.getName();
    private final EventConfiguration parent;
    private final StandardEventType standardEventType;
    private final ToolTipEventConfiguration toolTipEventConf;
    private final ExpressionToolTipEventConfiguration expressionToolTipEventConf;
    private final OpenURLEventConfiguration openURLEventConf;
    private final OpenMenuEventConfiguration openMenuEventConf;

    public StandardEventConfiguration(PropertyList standardEventProps, EventConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (standardEventProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.parent = parent;
        this.standardEventType = StandardEventType.fromString(standardEventProps.getProperty("standardeventtype", DEFAULT_STANDARD_EVENT_TYPE));
        this.toolTipEventConf = this.standardEventType == StandardEventType.TOOL_TIP ? new ToolTipEventConfiguration(standardEventProps.getPropertyListNotNull("tooltipeventprops"), this) : null;
        this.expressionToolTipEventConf = this.standardEventType == StandardEventType.EXPRESSION_TOOL_TIP ? new ExpressionToolTipEventConfiguration(standardEventProps.getPropertyListNotNull("expressiontooltipeventprops"), this) : null;
        this.openURLEventConf = this.standardEventType == StandardEventType.OPEN_URL ? new OpenURLEventConfiguration(standardEventProps.getPropertyListNotNull("openurleventprops"), this) : null;
        this.openMenuEventConf = this.standardEventType == StandardEventType.OPEN_MENU ? new OpenMenuEventConfiguration(standardEventProps.getPropertyListNotNull("openmenueventprops"), this) : null;
    }

    public StandardEventConfiguration(StandardEventConfiguration copy, EventConfiguration parent) {
        this.parent = parent;
        this.standardEventType = copy.standardEventType;
        this.toolTipEventConf = copy.toolTipEventConf != null ? new ToolTipEventConfiguration(copy.toolTipEventConf, this) : null;
        this.expressionToolTipEventConf = copy.expressionToolTipEventConf != null ? new ExpressionToolTipEventConfiguration(copy.expressionToolTipEventConf, this) : null;
        this.openURLEventConf = copy.openURLEventConf != null ? new OpenURLEventConfiguration(copy.openURLEventConf, this) : null;
        this.openMenuEventConf = copy.openMenuEventConf != null ? new OpenMenuEventConfiguration(copy.openMenuEventConf, this) : null;
    }

    public EventConfiguration getParent() {
        return this.parent;
    }

    public StandardEventType getStandardEventType() {
        return this.standardEventType;
    }

    public ToolTipEventConfiguration getToolTipEventConfiguration() {
        if (this.toolTipEventConf == null) {
            throw new IllegalStateException("Standard event type is: " + (Object)((Object)this.standardEventType));
        }
        return this.toolTipEventConf;
    }

    public ExpressionToolTipEventConfiguration getExpressionToolTipEventConfiguration() {
        if (this.expressionToolTipEventConf == null) {
            throw new IllegalStateException("Standard event type is: " + (Object)((Object)this.standardEventType));
        }
        return this.expressionToolTipEventConf;
    }

    public OpenURLEventConfiguration getOpenURLEventConfiguration() {
        if (this.openURLEventConf == null) {
            throw new IllegalStateException("Standard event type is: " + (Object)((Object)this.standardEventType));
        }
        return this.openURLEventConf;
    }

    public OpenMenuEventConfiguration getOpenMenuEventConfiguration() {
        if (this.openMenuEventConf == null) {
            throw new IllegalStateException("Standard event type is: " + (Object)((Object)this.standardEventType));
        }
        return this.openMenuEventConf;
    }

    public static enum StandardEventType {
        TOOL_TIP("Tool Tip"),
        EXPRESSION_TOOL_TIP("Expression Tool Tip"),
        OPEN_URL("Open URL"),
        OPEN_MENU("Open Menu");

        private final String name;

        private StandardEventType(String name) {
            this.name = name;
        }

        public static StandardEventType fromString(String name) {
            if (name != null) {
                for (StandardEventType type : StandardEventType.values()) {
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

