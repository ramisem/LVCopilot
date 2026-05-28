/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.bind.annotation.XmlAccessType
 *  javax.xml.bind.annotation.XmlAccessorType
 *  javax.xml.bind.annotation.XmlAttribute
 *  javax.xml.bind.annotation.XmlElement
 *  javax.xml.bind.annotation.XmlType
 */
package com.labvantage.sapphire.pageelements.workflow.bpmn.definition;

import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.BPMNElement;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.BPMNWaypoint;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(value=XmlAccessType.FIELD)
@XmlType(name="BPMNEdge", namespace="http://www.omg.org/spec/BPMN/20100524/DI")
public class BPMNEdge
extends BPMNElement {
    @XmlElement(name="waypoint", type=BPMNWaypoint.class, namespace="http://www.omg.org/spec/DD/20100524/DI")
    private List<BPMNWaypoint> waypoints = new ArrayList<BPMNWaypoint>();
    @XmlAttribute
    private String sourceElement = "";
    @XmlAttribute
    private String targetElement = "";

    public String getSourceElement() {
        return this.sourceElement;
    }

    public void setSourceElement(String sourceElement) {
        this.sourceElement = sourceElement;
    }

    public String getTargetElement() {
        return this.targetElement;
    }

    public void setTargetElement(String targetElement) {
        this.targetElement = targetElement;
    }

    public List<BPMNWaypoint> getWaypoints() {
        return this.waypoints;
    }

    public void setWaypoints(List<BPMNWaypoint> waypoints) {
        this.waypoints = waypoints;
    }
}

