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

import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.BPMNBounds;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.BPMNElement;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(value=XmlAccessType.FIELD)
@XmlType(name="BPMNShape", namespace="http://www.omg.org/spec/BPMN/20100524/DI")
public class BPMNShape
extends BPMNElement {
    @XmlElement(name="Bounds", type=BPMNBounds.class, namespace="http://www.omg.org/spec/DD/20100524/DC")
    private List<BPMNBounds> bounds = new ArrayList<BPMNBounds>();

    public List<BPMNBounds> getBounds() {
        return this.bounds;
    }

    public void setBounds(List<BPMNBounds> bounds) {
        this.bounds = bounds;
    }
}

