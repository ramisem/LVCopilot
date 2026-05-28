/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator.EventConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class DOMEventConfiguration
implements Serializable {
    private static final String DEFAULT_JAVA_SCRIPT = "";
    private static final String DEFAULT_DOM_EVENT_TYPE = DOMEventType.ON_CLICK.getName();
    private final EventConfiguration parent;
    private final DOMEventType domEventType;
    private final StringExpression javaScript;

    public DOMEventConfiguration(PropertyList domEventProps, EventConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (domEventProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.domEventType = DOMEventType.fromString(domEventProps.getProperty("domeventtype", DEFAULT_DOM_EVENT_TYPE));
        this.javaScript = new StringExpression(domEventProps.getProperty("javascript", DEFAULT_JAVA_SCRIPT));
        this.parent = parent;
    }

    public DOMEventConfiguration(DOMEventConfiguration copy, EventConfiguration parent) {
        this.javaScript = copy.javaScript;
        this.parent = parent;
        this.domEventType = copy.domEventType;
    }

    public StringExpression getJavaScript() {
        return this.javaScript;
    }

    public EventConfiguration getParent() {
        return this.parent;
    }

    public DOMEventType getDOMEventType() {
        return this.domEventType;
    }

    public static enum DOMEventType {
        ON_CLICK("Click"),
        MOUSE_OVER("Mouse Over"),
        MOUSE_OUT("Mouse Out"),
        MOUSE_UP("Mouse Up");

        private final String name;

        private DOMEventType(String name) {
            this.name = name;
        }

        public static DOMEventType fromString(String name) {
            if (name != null) {
                for (DOMEventType type : DOMEventType.values()) {
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

