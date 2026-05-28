/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.bind.annotation.XmlAccessType
 *  javax.xml.bind.annotation.XmlAccessorType
 *  javax.xml.bind.annotation.XmlElement
 *  javax.xml.bind.annotation.XmlElementWrapper
 *  javax.xml.bind.annotation.XmlType
 */
package com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl;

import com.labvantage.sapphire.pageelements.workflow.bpmn.Element;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.Activity;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.ProcessHeader;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.RedefinableHeader;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.Transition;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(value=XmlAccessType.FIELD)
@XmlType(name="WorkflowProcess", namespace="http://www.wfmc.org/2009/XPDL2.2")
public class WorkflowProcess
extends Element {
    @XmlElement(name="ProcessHeader", type=ProcessHeader.class, namespace="http://www.wfmc.org/2009/XPDL2.2")
    private ProcessHeader processHeader = new ProcessHeader();
    @XmlElement(name="RedefinableHeader", type=RedefinableHeader.class, namespace="http://www.wfmc.org/2009/XPDL2.2")
    private RedefinableHeader redefinableHeader = new RedefinableHeader();
    @XmlElement(name="Activity", type=Activity.class, namespace="http://www.wfmc.org/2009/XPDL2.2")
    @XmlElementWrapper(name="Activities", namespace="http://www.wfmc.org/2009/XPDL2.2")
    private List<Activity> activities = new ArrayList<Activity>();
    @XmlElement(name="Transition", type=Transition.class, namespace="http://www.wfmc.org/2009/XPDL2.2")
    @XmlElementWrapper(name="Transitions", namespace="http://www.wfmc.org/2009/XPDL2.2")
    private List<Transition> transitions = new ArrayList<Transition>();

    public ProcessHeader getProcessHeader() {
        return this.processHeader;
    }

    public void setProcessHeader(ProcessHeader processHeader) {
        this.processHeader = processHeader;
    }

    public RedefinableHeader getRedefinableHeader() {
        return this.redefinableHeader;
    }

    public void setRedefinableHeader(RedefinableHeader redefinableHeader) {
        this.redefinableHeader = redefinableHeader;
    }

    public List<Activity> getActivities() {
        return this.activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }

    public List<Transition> getTransitions() {
        return this.transitions;
    }

    public void setTransitions(List<Transition> transitions) {
        this.transitions = transitions;
    }
}

