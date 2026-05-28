/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.bind.annotation.XmlAccessType
 *  javax.xml.bind.annotation.XmlAccessorType
 *  javax.xml.bind.annotation.XmlElement
 */
package com.labvantage.sapphire.pageelements.workflow.bpmn.definition;

import com.labvantage.sapphire.pageelements.workflow.bpmn.Element;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.Incoming;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.Outgoing;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(value=XmlAccessType.FIELD)
public class ProcessElement
extends Element {
    @XmlElement(name="incoming", type=Incoming.class, namespace="http://www.omg.org/spec/BPMN/20100524/MODEL")
    private List<Incoming> inputs = new ArrayList<Incoming>();
    @XmlElement(name="outgoing", type=Outgoing.class, namespace="http://www.omg.org/spec/BPMN/20100524/MODEL")
    private List<Outgoing> outputs = new ArrayList<Outgoing>();

    public List<Incoming> getInputs() {
        return this.inputs;
    }

    public void setInputs(List<Incoming> inputs) {
        this.inputs = inputs;
    }

    public List<Outgoing> getOutputs() {
        return this.outputs;
    }

    public void setOutputs(List<Outgoing> outputs) {
        this.outputs = outputs;
    }
}

