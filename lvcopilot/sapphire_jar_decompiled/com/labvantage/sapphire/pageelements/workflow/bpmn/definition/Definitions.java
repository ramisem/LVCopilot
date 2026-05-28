/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.bind.annotation.XmlAccessType
 *  javax.xml.bind.annotation.XmlAccessorType
 *  javax.xml.bind.annotation.XmlAttribute
 *  javax.xml.bind.annotation.XmlElement
 *  javax.xml.bind.annotation.XmlRootElement
 */
package com.labvantage.sapphire.pageelements.workflow.bpmn.definition;

import com.labvantage.sapphire.pageelements.workflow.bpmn.Element;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.Annotation;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.BPMNBounds;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.BPMNDiagram;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.BPMNEdge;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.BPMNPlane;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.BPMNShape;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.EndEvent;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.Group;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.Incoming;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.Outgoing;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.Process;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.ProcessElement;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.SequenceFlow;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.StartEvent;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.Task;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(value=XmlAccessType.FIELD)
@XmlRootElement(name="definitions", namespace="http://www.omg.org/spec/BPMN/20100524/MODEL")
public class Definitions
extends Element {
    @XmlAttribute
    private String exporter = "";
    @XmlAttribute
    private String exporterVersion = "";
    @XmlAttribute
    private String targetNamespace = "";
    @XmlElement(name="process", type=Process.class, namespace="http://www.omg.org/spec/BPMN/20100524/MODEL")
    protected List<Process> processes = new ArrayList<Process>();
    @XmlElement(name="BPMNDiagram", type=BPMNDiagram.class, namespace="http://www.omg.org/spec/BPMN/20100524/DI")
    protected BPMNDiagram diagram = new BPMNDiagram();

    public String getTargetNamespace() {
        return this.targetNamespace;
    }

    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }

    public String getExporter() {
        return this.exporter;
    }

    public void setExporter(String exporter) {
        this.exporter = exporter;
    }

    public String getExporterVersion() {
        return this.exporterVersion;
    }

    public void setExporterVersion(String exporterVersion) {
        this.exporterVersion = exporterVersion;
    }

    public List<Process> getProcesses() {
        return this.processes;
    }

    public void setProcesses(ArrayList<Process> processes) {
        this.processes = processes;
    }

    public BPMNDiagram getDiagram() {
        return this.diagram;
    }

    public void setDiagram(BPMNDiagram diagram) {
        this.diagram = diagram;
    }

    /*
     * WARNING - void declaration
     */
    public String toString() {
        StringBuffer out = new StringBuffer();
        out.append("Definition Id: ").append(this.getId());
        out.append("Definition Name: ").append(this.getName());
        out.append("Definition Exporter: ").append(this.getExporter()).append(" ").append(this.getExporterVersion());
        List<Process> processes = this.getProcesses();
        if (processes != null && processes.size() > 0) {
            int i = 0;
            for (Process process : processes) {
                List<SequenceFlow> list;
                List<Group> groups;
                List<Annotation> list2;
                List<EndEvent> endEvents;
                List<StartEvent> startEvents;
                out.append("Process:").append(++i).append(' ').append(process.getId()).append(' ').append(process.getName()).append(' ').append(process.getProcessType()).append("\n");
                List<Task> tasks = process.getTasks();
                if (tasks != null) {
                    int j = 0;
                    for (Task task : tasks) {
                        List<Outgoing> list3;
                        out.append("\tTask:").append(++j).append(' ').append(task.getId()).append(' ').append(task.getName()).append(' ').append(task.getStartQuantity()).append("\n");
                        List<Incoming> incomings = task.getInputs();
                        if (incomings != null) {
                            boolean bl = false;
                            for (Incoming incoming : incomings) {
                                void var11_32;
                                out.append("\t\tIncoming:").append((int)(++var11_32)).append(" ").append(incoming.getFlowId()).append("\n");
                            }
                        }
                        if ((list3 = task.getOutputs()) == null) continue;
                        int io = 0;
                        for (Outgoing outgoing : list3) {
                            out.append("\t\tOutgoing:").append(++io).append(" ").append(outgoing.getFlowId()).append("\n");
                        }
                    }
                }
                if ((startEvents = process.getStartEvents()) != null) {
                    int j = 0;
                    for (Object event : startEvents) {
                        List<Outgoing> outgoings;
                        out.append("\tStart Event:").append(++j).append(' ').append(((Element)event).getId()).append(' ').append(((Element)event).getName()).append("\n");
                        List<Incoming> list4 = ((ProcessElement)event).getInputs();
                        if (list4 != null) {
                            int io = 0;
                            for (Incoming incoming : list4) {
                                out.append("\t\tIncoming:").append(++io).append(" ").append(incoming.getFlowId()).append("\n");
                            }
                        }
                        if ((outgoings = ((ProcessElement)event).getOutputs()) == null) continue;
                        boolean bl = false;
                        for (Outgoing outgoing : outgoings) {
                            void var13_54;
                            out.append("\t\tOutgoing:").append((int)(++var13_54)).append(" ").append(outgoing.getFlowId()).append("\n");
                        }
                    }
                }
                if ((endEvents = process.getEndEvents()) != null) {
                    Object event;
                    boolean bl = false;
                    event = endEvents.iterator();
                    while (event.hasNext()) {
                        List<Outgoing> list5;
                        void var9_19;
                        EndEvent endEvent = (EndEvent)event.next();
                        out.append("\tEnd Event:").append((int)(++var9_19)).append(' ').append(endEvent.getId()).append(' ').append(endEvent.getName()).append("\n");
                        List<Incoming> incomings = endEvent.getInputs();
                        if (incomings != null) {
                            boolean bl2 = false;
                            for (Incoming incoming : incomings) {
                                void var13_56;
                                out.append("\t\tIncoming:").append((int)(++var13_56)).append(" ").append(incoming.getFlowId()).append("\n");
                            }
                        }
                        if ((list5 = endEvent.getOutputs()) == null) continue;
                        boolean bl3 = false;
                        for (Outgoing outgoing : list5) {
                            void var14_65;
                            out.append("\t\tOutgoing:").append((int)(++var14_65)).append(" ").append(outgoing.getFlowId()).append("\n");
                        }
                    }
                }
                if ((list2 = process.getAnnotations()) != null) {
                    int j = 0;
                    for (Object annotation : list2) {
                        out.append("\tAnnotation:").append(++j).append(' ').append(((Annotation)annotation).getText()).append("\n");
                    }
                }
                if ((groups = process.getGroups()) != null) {
                    Object annotation;
                    boolean bl = false;
                    annotation = groups.iterator();
                    while (annotation.hasNext()) {
                        void var11_41;
                        Group group = (Group)annotation.next();
                        out.append("\tGroup:").append((int)(++var11_41)).append(' ').append(group.getCategoryValueRef()).append("\n");
                    }
                }
                if ((list = process.getFlow()) == null) continue;
                int j = 0;
                for (SequenceFlow sequenceFlow : list) {
                    out.append("\tFlow:").append(++j).append(' ').append(sequenceFlow.getId()).append(' ').append(sequenceFlow.getName()).append(" Source: ").append(sequenceFlow.getSource()).append(" Target: ").append(sequenceFlow.getTarget()).append("\n");
                }
            }
        } else {
            out.append("No Processes.\n");
        }
        BPMNDiagram diagram = this.getDiagram();
        out.append("");
        out.append("Diagram Id: " + diagram.getId());
        out.append("Diagram Name: " + diagram.getName());
        out.append("Documentation: " + diagram.getDocumentation());
        List<BPMNPlane> planes = diagram.getPlanes();
        if (planes != null && planes.size() > 0) {
            int i = 0;
            for (BPMNPlane plane : planes) {
                List<BPMNEdge> list;
                out.append("\tplane:").append(++i).append(' ').append(plane.getId()).append("\n");
                List<BPMNShape> shapes = plane.getShapes();
                if (shapes != null) {
                    boolean bl = false;
                    for (BPMNShape bPMNShape : shapes) {
                        void var9_22;
                        out.append("\t\tShape:").append((int)(++var9_22)).append(' ').append(bPMNShape.getId()).append(" Element: ").append(bPMNShape.getElement()).append("\n");
                        List<BPMNBounds> bounds = bPMNShape.getBounds();
                        out.append("\t\t\tBounds:").append("X - ").append(bounds.get(0).getX()).append(" Y - ").append(bounds.get(0).getY()).append("\n");
                    }
                }
                if ((list = plane.getEdges()) == null) continue;
                int j = 0;
                for (BPMNEdge edge : list) {
                    out.append("\t\tEdge:").append(++j + 32 + edge.getId()).append(" Element: ").append(edge.getElement()).append("\n");
                }
            }
        } else {
            out.append("No Diagram.\n");
        }
        return out.toString();
    }
}

