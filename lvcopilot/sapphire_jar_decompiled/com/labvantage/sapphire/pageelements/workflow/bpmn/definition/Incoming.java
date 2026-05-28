/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.bind.annotation.XmlAccessType
 *  javax.xml.bind.annotation.XmlAccessorType
 *  javax.xml.bind.annotation.XmlType
 *  javax.xml.bind.annotation.XmlValue
 */
package com.labvantage.sapphire.pageelements.workflow.bpmn.definition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(value=XmlAccessType.FIELD)
@XmlType(name="incoming", namespace="http://www.omg.org/spec/BPMN/20100524/MODEL")
public class Incoming {
    @XmlValue
    private String flowId = "";

    public String getFlowId() {
        return this.flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }
}

