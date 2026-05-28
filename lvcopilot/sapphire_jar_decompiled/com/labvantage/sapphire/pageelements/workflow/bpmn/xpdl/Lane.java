/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.bind.annotation.XmlAccessType
 *  javax.xml.bind.annotation.XmlAccessorType
 *  javax.xml.bind.annotation.XmlElement
 *  javax.xml.bind.annotation.XmlElementWrapper
 *  javax.xml.bind.annotation.XmlType
 */
package com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl;

import com.labvantage.sapphire.pageelements.workflow.bpmn.Element;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.NodeGraphicsInfo;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(value=XmlAccessType.FIELD)
@XmlType(name="Lane", namespace="http://www.wfmc.org/2009/XPDL2.2")
public class Lane
extends Element {
    @XmlElement(name="NodeGraphicsInfo", type=NodeGraphicsInfo.class, namespace="http://www.wfmc.org/2009/XPDL2.2")
    @XmlElementWrapper(name="NodeGraphicsInfos", namespace="http://www.wfmc.org/2009/XPDL2.2")
    private List<NodeGraphicsInfo> nodeGraphicsInfos = new ArrayList<NodeGraphicsInfo>();

    public List<NodeGraphicsInfo> getNodeGraphicsInfos() {
        return this.nodeGraphicsInfos;
    }

    public void setNodeGraphicsInfos(List<NodeGraphicsInfo> nodeGraphicsInfos) {
        this.nodeGraphicsInfos = nodeGraphicsInfos;
    }
}

