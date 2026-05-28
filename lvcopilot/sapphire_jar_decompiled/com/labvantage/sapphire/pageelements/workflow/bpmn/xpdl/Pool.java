/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.bind.annotation.XmlAccessType
 *  javax.xml.bind.annotation.XmlAccessorType
 *  javax.xml.bind.annotation.XmlAttribute
 *  javax.xml.bind.annotation.XmlElement
 *  javax.xml.bind.annotation.XmlElementWrapper
 *  javax.xml.bind.annotation.XmlType
 */
package com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl;

import com.labvantage.sapphire.pageelements.workflow.bpmn.Element;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.Lane;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.NodeGraphicsInfo;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(value=XmlAccessType.FIELD)
@XmlType(name="Pool", namespace="http://www.wfmc.org/2009/XPDL2.2")
public class Pool
extends Element {
    @XmlAttribute(name="BoundaryVisible")
    private boolean boundaryVisible;
    @XmlAttribute(name="Process")
    private String process = "";
    @XmlElement(name="NodeGraphicsInfo", type=NodeGraphicsInfo.class, namespace="http://www.wfmc.org/2009/XPDL2.2")
    @XmlElementWrapper(name="NodeGraphicsInfos", namespace="http://www.wfmc.org/2009/XPDL2.2")
    private List<NodeGraphicsInfo> nodeGraphicsInfos = new ArrayList<NodeGraphicsInfo>();
    @XmlElement(name="Lane", type=Lane.class, namespace="http://www.wfmc.org/2009/XPDL2.2")
    @XmlElementWrapper(name="Lanes", namespace="http://www.wfmc.org/2009/XPDL2.2")
    private List<Lane> lanes = new ArrayList<Lane>();

    public List<NodeGraphicsInfo> getNodeGraphicsInfos() {
        return this.nodeGraphicsInfos;
    }

    public void setNodeGraphicsInfos(List<NodeGraphicsInfo> nodeGraphicsInfos) {
        this.nodeGraphicsInfos = nodeGraphicsInfos;
    }

    public boolean isBoundaryVisible() {
        return this.boundaryVisible;
    }

    public void setBoundaryVisible(boolean boundaryVisible) {
        this.boundaryVisible = boundaryVisible;
    }

    public List<Lane> getLanes() {
        return this.lanes;
    }

    public void setLanes(List<Lane> lanes) {
        this.lanes = lanes;
    }

    public String getProcess() {
        return this.process;
    }

    public void setProcess(String process) {
        this.process = process;
    }
}

