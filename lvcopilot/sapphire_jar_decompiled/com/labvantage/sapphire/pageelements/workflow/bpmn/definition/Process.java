/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.bind.annotation.XmlAccessType
 *  javax.xml.bind.annotation.XmlAccessorType
 *  javax.xml.bind.annotation.XmlAttribute
 *  javax.xml.bind.annotation.XmlElement
 *  javax.xml.bind.annotation.XmlElements
 *  javax.xml.bind.annotation.XmlType
 */
package com.labvantage.sapphire.pageelements.workflow.bpmn.definition;

import com.labvantage.sapphire.pageelements.workflow.bpmn.Element;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.Annotation;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.BusinessRuleTask;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.CallActivity;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.EndEvent;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.ExclusiveGateway;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.Group;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.ManualTask;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.ReceiveTask;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.ScriptTask;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.SendTask;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.SequenceFlow;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.ServiceTask;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.StartEvent;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.Task;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.UserTask;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(value=XmlAccessType.FIELD)
@XmlType(name="process", namespace="http://www.omg.org/spec/BPMN/20100524/MODEL")
public class Process
extends Element {
    @XmlAttribute
    private String processType = "";
    @XmlAttribute
    private boolean isExecutable;
    @XmlAttribute
    private boolean isClosed;
    @XmlElements(value={@XmlElement(name="task", type=Task.class, namespace="http://www.omg.org/spec/BPMN/20100524/MODEL"), @XmlElement(name="sendTask", type=SendTask.class, namespace="http://www.omg.org/spec/BPMN/20100524/MODEL"), @XmlElement(name="receiveTask", type=ReceiveTask.class, namespace="http://www.omg.org/spec/BPMN/20100524/MODEL"), @XmlElement(name="serviceTask", type=ServiceTask.class, namespace="http://www.omg.org/spec/BPMN/20100524/MODEL"), @XmlElement(name="userTask", type=UserTask.class, namespace="http://www.omg.org/spec/BPMN/20100524/MODEL"), @XmlElement(name="manualTask", type=ManualTask.class, namespace="http://www.omg.org/spec/BPMN/20100524/MODEL"), @XmlElement(name="businessRuleTask", type=BusinessRuleTask.class, namespace="http://www.omg.org/spec/BPMN/20100524/MODEL"), @XmlElement(name="exclusiveGateway", type=ExclusiveGateway.class, namespace="http://www.omg.org/spec/BPMN/20100524/MODEL"), @XmlElement(name="callActivity", type=CallActivity.class, namespace="http://www.omg.org/spec/BPMN/20100524/MODEL"), @XmlElement(name="scriptTask", type=ScriptTask.class, namespace="http://www.omg.org/spec/BPMN/20100524/MODEL")})
    private List<Task> tasks = new ArrayList<Task>();
    @XmlElement(name="startEvent", type=StartEvent.class, namespace="http://www.omg.org/spec/BPMN/20100524/MODEL")
    private List<StartEvent> startEvents = new ArrayList<StartEvent>();
    @XmlElement(name="endEvent", type=EndEvent.class, namespace="http://www.omg.org/spec/BPMN/20100524/MODEL")
    private List<EndEvent> endEvents = new ArrayList<EndEvent>();
    @XmlElement(name="sequenceFlow", type=SequenceFlow.class, namespace="http://www.omg.org/spec/BPMN/20100524/MODEL")
    private List<SequenceFlow> flow = new ArrayList<SequenceFlow>();
    @XmlElement(name="group", type=Group.class, namespace="http://www.omg.org/spec/BPMN/20100524/MODEL")
    private List<Group> groups = new ArrayList<Group>();
    @XmlElement(name="textAnnotation", type=Annotation.class, namespace="http://www.omg.org/spec/BPMN/20100524/MODEL")
    private List<Annotation> annotations = new ArrayList<Annotation>();

    public List<Group> getGroups() {
        return this.groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public List<Annotation> getAnnotations() {
        return this.annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    public List<Task> getTasks() {
        return this.tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<StartEvent> getStartEvents() {
        return this.startEvents;
    }

    public void setStartEvents(List<StartEvent> startEvents) {
        this.startEvents = startEvents;
    }

    public List<EndEvent> getEndEvents() {
        return this.endEvents;
    }

    public void setEndEvents(List<EndEvent> endEvents) {
        this.endEvents = endEvents;
    }

    public String getProcessType() {
        return this.processType;
    }

    public void setProcessType(String processType) {
        this.processType = processType;
    }

    public boolean isExecutable() {
        return this.isExecutable;
    }

    public void setExecutable(boolean executable) {
        this.isExecutable = executable;
    }

    public boolean isClosed() {
        return this.isClosed;
    }

    public void setClosed(boolean closed) {
        this.isClosed = closed;
    }

    public List<SequenceFlow> getFlow() {
        return this.flow;
    }

    public void setFlow(List<SequenceFlow> flow) {
        this.flow = flow;
    }

    public Task findTask(String elementId) {
        if (this.tasks != null) {
            for (Task task : this.tasks) {
                if (!task.getId().equals(elementId)) continue;
                return task;
            }
        }
        return null;
    }

    public Annotation findAnnotation(String elementId) {
        if (this.annotations != null) {
            for (Annotation annotation : this.annotations) {
                if (!annotation.getId().equals(elementId)) continue;
                return annotation;
            }
        }
        return null;
    }

    public Group findGroup(String elementId) {
        if (this.groups != null) {
            for (Group group : this.groups) {
                if (!group.getId().equals(elementId)) continue;
                return group;
            }
        }
        return null;
    }
}

