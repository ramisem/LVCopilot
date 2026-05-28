/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.bind.annotation.XmlAccessType
 *  javax.xml.bind.annotation.XmlAccessorType
 *  javax.xml.bind.annotation.XmlElement
 *  javax.xml.bind.annotation.XmlType
 */
package com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl;

import com.labvantage.sapphire.pageelements.workflow.bpmn.Element;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.Task;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(value=XmlAccessType.FIELD)
@XmlType(name="Implementation", namespace="http://www.wfmc.org/2009/XPDL2.2")
public class Implementation
extends Element {
    @XmlElement(name="Task", type=Task.class, namespace="http://www.wfmc.org/2009/XPDL2.2")
    private Task task = new Task();

    public Task getTask() {
        return this.task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}

