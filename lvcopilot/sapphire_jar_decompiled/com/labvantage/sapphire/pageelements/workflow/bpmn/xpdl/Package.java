/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.bind.annotation.XmlAccessType
 *  javax.xml.bind.annotation.XmlAccessorType
 *  javax.xml.bind.annotation.XmlElement
 *  javax.xml.bind.annotation.XmlElementWrapper
 *  javax.xml.bind.annotation.XmlRootElement
 */
package com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl;

import com.labvantage.sapphire.pageelements.workflow.bpmn.Element;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.Activity;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.Artifact;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.Coordinate;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.EndEvent;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.Event;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.PackageHeader;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.Pool;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.RedefinableHeader;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.StartEvent;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.Transition;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.WorkflowProcess;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(value=XmlAccessType.FIELD)
@XmlRootElement(name="Package", namespace="http://www.wfmc.org/2009/XPDL2.2")
public class Package
extends Element {
    @XmlElement(name="PackageHeader", type=PackageHeader.class, namespace="http://www.wfmc.org/2009/XPDL2.2")
    private PackageHeader packageHeader = new PackageHeader();
    @XmlElement(name="RedefinableHeader", type=RedefinableHeader.class, namespace="http://www.wfmc.org/2009/XPDL2.2")
    private RedefinableHeader redefinableHeader = new RedefinableHeader();
    @XmlElement(name="Pool", type=Pool.class, namespace="http://www.wfmc.org/2009/XPDL2.2")
    @XmlElementWrapper(name="Pools", namespace="http://www.wfmc.org/2009/XPDL2.2")
    private List<Pool> pools = new ArrayList<Pool>();
    @XmlElement(name="Artifact", type=Artifact.class, namespace="http://www.wfmc.org/2009/XPDL2.2")
    @XmlElementWrapper(name="Artifacts", namespace="http://www.wfmc.org/2009/XPDL2.2")
    private List<Artifact> artifacts = new ArrayList<Artifact>();
    @XmlElement(name="WorkflowProcess", type=WorkflowProcess.class, namespace="http://www.wfmc.org/2009/XPDL2.2")
    @XmlElementWrapper(name="WorkflowProcesses", namespace="http://www.wfmc.org/2009/XPDL2.2")
    private List<WorkflowProcess> processes = new ArrayList<WorkflowProcess>();

    public List<Pool> getPools() {
        return this.pools;
    }

    public void setPools(List<Pool> pools) {
        this.pools = pools;
    }

    public List<Artifact> getArtifacts() {
        return this.artifacts;
    }

    public void setArtifacts(List<Artifact> artifacts) {
        this.artifacts = artifacts;
    }

    public PackageHeader getPackageHeader() {
        return this.packageHeader;
    }

    public void setPackageHeader(PackageHeader packageHeader) {
        this.packageHeader = packageHeader;
    }

    public RedefinableHeader getRedefinableHeader() {
        return this.redefinableHeader;
    }

    public void setRedefinableHeader(RedefinableHeader redefinableHeader) {
        this.redefinableHeader = redefinableHeader;
    }

    public List<WorkflowProcess> getProcesses() {
        return this.processes;
    }

    public void setProcesses(List<WorkflowProcess> activities) {
        this.processes = activities;
    }

    public String toString() {
        StringBuffer out = new StringBuffer();
        out.append("Package Id: ").append(this.getId());
        out.append("Package Name: ").append(this.getName());
        out.append("Package Head: XPDL ").append(this.getPackageHeader().getXpdlVersion()).append("Description ").append(this.getPackageHeader().getDescription()).append("\n");
        out.append("Author: ").append(this.getRedefinableHeader().getAuthor()).append("\n");
        List<WorkflowProcess> processes = this.getProcesses();
        if (processes != null && processes.size() > 0) {
            int i = 0;
            for (WorkflowProcess process : processes) {
                List<Transition> transitions;
                out.append("Process:").append(++i).append(" ").append(process.getId()).append(" ").append(process.getName()).append("\n");
                List<Activity> activities = process.getActivities();
                if (activities != null) {
                    int j = 0;
                    for (Activity activity : activities) {
                        out.append("\tActivity:").append(++j).append(" ").append(activity.getId()).append(" ").append(activity.getName()).append(" ").append(activity.getDescription()).append("\n");
                        Coordinate coordinates = activity.getNodeGraphicsInfos() != null && activity.getNodeGraphicsInfos().size() > 0 ? activity.getNodeGraphicsInfos().get(0).getCoordinates() : new Coordinate();
                        out.append("\t\tCoordinate:").append(coordinates.getX()).append(" x ").append(coordinates.getY()).append("\n");
                        List<Event> events = activity.getEvents();
                        if (events == null) continue;
                        int e = 0;
                        for (Event event : events) {
                            ++e;
                            if (event instanceof StartEvent) {
                                out.append("\tStart Event:").append(e).append(" ").append(event.getTrigger()).append("\n");
                                continue;
                            }
                            if (event instanceof EndEvent) {
                                out.append("\tEnd Event:").append(e).append(" ").append(event.getResult()).append("\n");
                                continue;
                            }
                            out.append("\tEvent:").append(e).append("\n");
                        }
                    }
                }
                if ((transitions = process.getTransitions()) == null) continue;
                int j = 0;
                for (Transition transition : transitions) {
                    out.append("\tTransition:").append(++j).append(" ").append(transition.getId()).append(" ").append(transition.getName()).append(" Source: ").append(transition.getFrom()).append(" Target: ").append(transition.getTo()).append("\n");
                }
            }
        } else {
            out.append("No Processes.\n");
        }
        return out.toString();
    }
}

