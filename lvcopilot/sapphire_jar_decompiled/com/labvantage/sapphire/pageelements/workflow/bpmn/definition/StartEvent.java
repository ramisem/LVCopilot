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

import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.ProcessElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(value=XmlAccessType.FIELD)
@XmlType(name="startEvent", namespace="http://www.omg.org/spec/BPMN/20100524/MODEL")
public class StartEvent
extends ProcessElement {
    @XmlAttribute
    private boolean parallelMultiple;
    @XmlAttribute
    private boolean isInterrupting;

    public boolean isParallelMultiple() {
        return this.parallelMultiple;
    }

    public void setParallelMultiple(boolean parallelMultiple) {
        this.parallelMultiple = parallelMultiple;
    }

    public boolean isInterrupting() {
        return this.isInterrupting;
    }

    public void setInterrupting(boolean isInterrupting) {
        this.isInterrupting = isInterrupting;
    }
}

