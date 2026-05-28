/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator.DOMEventConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator.StandardEventConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator.URLGeneratorConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.BooleanExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class EventConfiguration
implements Serializable {
    private static final String DEFAULT_EVENT_TYPE = EventType.STANDARD_EVENT.getName();
    private static final String DEFAULT_ENABLE = "Y";
    private final URLGeneratorConfiguration parent;
    private final EventType eventType;
    private final DOMEventConfiguration domEventConf;
    private final StandardEventConfiguration standardEventConf;
    private final String eventId;
    private final BooleanExpression enable;

    public EventConfiguration(PropertyList eventProps, URLGeneratorConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (eventProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.parent = parent;
        this.eventId = eventProps.getProperty("eventid");
        this.enable = new BooleanExpression(eventProps.getProperty("enable", DEFAULT_ENABLE));
        this.eventType = EventType.fromString(eventProps.getProperty("eventtype", DEFAULT_EVENT_TYPE));
        this.standardEventConf = this.eventType == EventType.STANDARD_EVENT ? new StandardEventConfiguration(eventProps.getPropertyListNotNull("standardeventprops"), this) : null;
        this.domEventConf = this.eventType == EventType.DOM_EVENT ? new DOMEventConfiguration(eventProps.getPropertyListNotNull("domeventprops"), this) : null;
    }

    public EventConfiguration(EventConfiguration copy, URLGeneratorConfiguration parent) {
        this.parent = parent;
        this.eventType = copy.eventType;
        this.eventId = copy.eventId;
        this.enable = new BooleanExpression(copy.enable);
        this.standardEventConf = copy.standardEventConf != null ? new StandardEventConfiguration(copy.standardEventConf, this) : null;
        this.domEventConf = copy.domEventConf != null ? new DOMEventConfiguration(copy.domEventConf, this) : null;
    }

    public BooleanExpression isEnabled() {
        return this.enable;
    }

    public String getEventId() {
        return this.eventId;
    }

    public URLGeneratorConfiguration getParent() {
        return this.parent;
    }

    public EventType getEventType() {
        return this.eventType;
    }

    public StandardEventConfiguration getStandardEventConfiguration() {
        if (this.standardEventConf == null) {
            throw new IllegalStateException("Event type is: " + (Object)((Object)this.eventType));
        }
        return this.standardEventConf;
    }

    public DOMEventConfiguration getDOMEventConfiguration() {
        if (this.domEventConf == null) {
            throw new IllegalStateException("Event type is: " + (Object)((Object)this.eventType));
        }
        return this.domEventConf;
    }

    public static enum EventType {
        DOM_EVENT("DOM"),
        STANDARD_EVENT("Standard");

        private final String name;

        private EventType(String name) {
            this.name = name;
        }

        public static EventType fromString(String name) {
            if (name != null) {
                for (EventType type : EventType.values()) {
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

