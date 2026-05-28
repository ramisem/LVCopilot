/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.bind.annotation.XmlAccessType
 *  javax.xml.bind.annotation.XmlAccessorType
 *  javax.xml.bind.annotation.XmlElement
 *  javax.xml.bind.annotation.XmlType
 */
package com.labvantage.sapphire.pageelements.workflow.bpmn.definition;

import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.BPMNEdge;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.BPMNElement;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.BPMNShape;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(value=XmlAccessType.FIELD)
@XmlType(name="BPMNPlane", namespace="http://www.omg.org/spec/BPMN/20100524/DI")
public class BPMNPlane
extends BPMNElement {
    @XmlElement(name="BPMNShape", type=BPMNShape.class, namespace="http://www.omg.org/spec/BPMN/20100524/DI")
    private List<BPMNShape> shapes = new ArrayList<BPMNShape>();
    @XmlElement(name="BPMNEdge", type=BPMNEdge.class, namespace="http://www.omg.org/spec/BPMN/20100524/DI")
    private List<BPMNEdge> edges = new ArrayList<BPMNEdge>();

    public List<BPMNShape> getShapes() {
        return this.shapes;
    }

    public void setShapes(List<BPMNShape> shapes) {
        this.shapes = shapes;
    }

    public List<BPMNEdge> getEdges() {
        return this.edges;
    }

    public void setEdges(List<BPMNEdge> edges) {
        this.edges = edges;
    }
}

