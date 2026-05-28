/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.bind.annotation.XmlAccessType
 *  javax.xml.bind.annotation.XmlAccessorType
 *  javax.xml.bind.annotation.XmlAttribute
 *  javax.xml.bind.annotation.XmlType
 */
package com.labvantage.sapphire.pageelements.workflow.bpmn.definition;

import com.labvantage.sapphire.pageelements.workflow.bpmn.Element;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(value=XmlAccessType.FIELD)
@XmlType(name="sequenceFlow", namespace="http://www.omg.org/spec/BPMN/20100524/MODEL")
public class SequenceFlow
extends Element {
    @XmlAttribute(name="sourceRef")
    private String sourceId;
    @XmlAttribute(name="targetRef")
    private String targetId;

    public String getTarget() {
        return this.targetId;
    }

    public void setTarget(String target) {
        this.targetId = target;
    }

    public String getSource() {
        return this.sourceId;
    }

    public void setSource(String source) {
        this.sourceId = source;
    }
}

