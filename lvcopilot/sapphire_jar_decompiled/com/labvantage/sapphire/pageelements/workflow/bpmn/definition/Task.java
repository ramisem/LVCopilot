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
@XmlType(name="task", namespace="http://www.omg.org/spec/BPMN/20100524/MODEL")
public class Task
extends ProcessElement {
    @XmlAttribute
    private int startQuantity;
    @XmlAttribute
    private boolean isForCompensation;
    @XmlAttribute
    private int completionQuantity;
    @XmlAttribute
    private String implementation = "";

    public String getImplementation() {
        return this.implementation;
    }

    public void setImplementation(String implementation) {
        this.implementation = implementation;
    }

    public int getCompletionQuantity() {
        return this.completionQuantity;
    }

    public void setCompletionQuantity(int completionQuantity) {
        this.completionQuantity = completionQuantity;
    }

    public int getStartQuantity() {
        return this.startQuantity;
    }

    public void setStartQuantity(int startQuantity) {
        this.startQuantity = startQuantity;
    }

    public boolean isForCompensation() {
        return this.isForCompensation;
    }

    public void setForCompensation(boolean forCompensation) {
        this.isForCompensation = forCompensation;
    }
}

