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

import com.labvantage.sapphire.pageelements.workflow.bpmn.Element;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.BPMNPlane;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(value=XmlAccessType.FIELD)
@XmlType(name="BPMNDiagram", namespace="http://www.omg.org/spec/BPMN/20100524/DI")
public class BPMNDiagram
extends Element {
    @XmlAttribute
    private String resolution;
    @XmlAttribute
    private String documentation;
    @XmlElement(name="BPMNPlane", type=BPMNPlane.class, namespace="http://www.omg.org/spec/BPMN/20100524/DI")
    private List<BPMNPlane> planes = new ArrayList<BPMNPlane>();

    public String getResolution() {
        return this.resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getDocumentation() {
        return this.documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    public List<BPMNPlane> getPlanes() {
        return this.planes;
    }

    public void setPlanes(List<BPMNPlane> planes) {
        this.planes = planes;
    }
}

