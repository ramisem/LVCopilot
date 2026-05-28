/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.bind.annotation.XmlAccessType
 *  javax.xml.bind.annotation.XmlAccessorType
 *  javax.xml.bind.annotation.XmlElement
 *  javax.xml.bind.annotation.XmlElementWrapper
 *  javax.xml.bind.annotation.XmlElements
 *  javax.xml.bind.annotation.XmlType
 */
package com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl;

import com.labvantage.sapphire.pageelements.workflow.bpmn.Element;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.EndEvent;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.Event;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.Implementation;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.NodeGraphicsInfo;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.StartEvent;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(value=XmlAccessType.FIELD)
@XmlType(name="Activity", namespace="http://www.wfmc.org/2009/XPDL2.2")
public class Activity
extends Element {
    @XmlElement(name="Description", namespace="http://www.wfmc.org/2009/XPDL2.2")
    private String description = "";
    @XmlElement(name="Implementation", namespace="http://www.wfmc.org/2009/XPDL2.2")
    private Implementation implementation = new Implementation();
    @XmlElement(name="Documentation", namespace="http://www.wfmc.org/2009/XPDL2.2")
    private String documentation = "";
    @XmlElement(name="NodeGraphicsInfo", type=NodeGraphicsInfo.class, namespace="http://www.wfmc.org/2009/XPDL2.2")
    @XmlElementWrapper(name="NodeGraphicsInfos", namespace="http://www.wfmc.org/2009/XPDL2.2")
    private List<NodeGraphicsInfo> nodeGraphicsInfos = new ArrayList<NodeGraphicsInfo>();
    @XmlElements(value={@XmlElement(name="StartEvent", type=StartEvent.class, namespace="http://www.wfmc.org/2009/XPDL2.2"), @XmlElement(name="EndEvent", type=EndEvent.class, namespace="http://www.wfmc.org/2009/XPDL2.2")})
    @XmlElementWrapper(name="Event", namespace="http://www.wfmc.org/2009/XPDL2.2")
    private List<Event> events = new ArrayList<Event>();

    public List<Event> getEvents() {
        return this.events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Implementation getImplementation() {
        return this.implementation;
    }

    public void setImplementation(Implementation implementation) {
        this.implementation = implementation;
    }

    public String getDocumentation() {
        return this.documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    public List<NodeGraphicsInfo> getNodeGraphicsInfos() {
        return this.nodeGraphicsInfos;
    }

    public void setNodeGraphicsInfos(List<NodeGraphicsInfo> nodeGraphicsInfos) {
        this.nodeGraphicsInfos = nodeGraphicsInfos;
    }
}

