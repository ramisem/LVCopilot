/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.bind.annotation.XmlAccessType
 *  javax.xml.bind.annotation.XmlAccessorType
 *  javax.xml.bind.annotation.XmlAttribute
 */
package com.labvantage.sapphire.pageelements.workflow.bpmn.definition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(value=XmlAccessType.FIELD)
public class BPMNElement {
    @XmlAttribute(name="bpmnElement")
    private String elementId;
    @XmlAttribute
    private String id = "";

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getElement() {
        return this.elementId;
    }

    public void setElement(String element) {
        this.elementId = element;
    }
}

