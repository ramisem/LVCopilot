/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.bind.JAXBException
 */
package com.labvantage.sapphire.pageelements.workflow.bpmn;

import com.labvantage.sapphire.pageelements.workflow.bpmn.BPMNUtil;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.Annotation;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.BPMNPlane;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.BPMNShape;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.BusinessRuleTask;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.CallActivity;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.Definitions;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.EndEvent;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.ExclusiveGateway;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.Group;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.ManualTask;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.Process;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.ReceiveTask;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.ScriptTask;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.SendTask;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.SequenceFlow;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.ServiceTask;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.Task;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.UserTask;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.Activity;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.Artifact;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.Coordinate;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.Event;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.Lane;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.Package;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.Pool;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.StartEvent;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.Transition;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.WorkflowProcess;
import com.labvantage.sapphire.pageelements.workflow.workflowdefpainter.WorkflowDefPainter;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import sapphire.SapphireException;
import sapphire.util.Logger;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class BPMNImporter {
    public static final float XPDL_X_ADJUSTMENT = 1.33f;
    public static final float BPMN_X_ADJUSTMENT = 0.0f;

    public static void main(String[] args) {
        String file = "c:\\Temp\\TestXPDL.xpdl";
        if (args.length > 0) {
            file = args[0];
        }
        try {
            if (file.toLowerCase().endsWith("xpdl")) {
                Package def = BPMNUtil.readXPDL(new File(file));
                System.out.print(def.toString());
            } else {
                Definitions def = BPMNUtil.readBPMN(new File(file));
                System.out.print(def.toString());
            }
        }
        catch (JAXBException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    public static PropertyList importBPMN(Package xlpdpackage) {
        PropertyList out = new PropertyList();
        if (xlpdpackage.getProcesses() != null && xlpdpackage.getProcesses().size() > 0) {
            Object coordinates;
            for (WorkflowProcess process : xlpdpackage.getProcesses()) {
                if (process.getActivities() == null || process.getActivities().size() <= 0) continue;
                if (out.getCollection("tasks") == null) {
                    out.setProperty("tasks", new PropertyListCollection());
                }
                for (Activity activity : process.getActivities()) {
                    if (activity.getEvents() != null && activity.getEvents().size() != 0) continue;
                    PropertyList taskProps = BPMNImporter.getPrototypeTask(activity.getId(), activity.getName(), activity.getId(), "");
                    Coordinate coordinates2 = activity.getNodeGraphicsInfos() != null && activity.getNodeGraphicsInfos().size() > 0 ? activity.getNodeGraphicsInfos().get(0).getCoordinates() : new Coordinate();
                    taskProps.setProperty("x", "" + (float)coordinates2.getX() * 1.33f);
                    taskProps.setProperty("y", "" + coordinates2.getY());
                    out.getCollection("tasks").add(taskProps);
                }
                PropertyListCollection newtasks = out.getCollection("tasks");
                if (newtasks.size() <= 0 || process.getTransitions() == null || process.getTransitions().size() <= 0) break;
                for (Transition transition : process.getTransitions()) {
                    String outputElementId = transition.getFrom();
                    Iterator<Transition> inputElementId = transition.getTo();
                    BPMNImporter.addIO(newtasks, (String)((Object)inputElementId), outputElementId, transition.getId());
                }
                block3: for (Activity activity : process.getActivities()) {
                    PropertyList taskProps;
                    if (activity.getEvents() == null || activity.getEvents().size() <= 0) continue;
                    Event event = activity.getEvents().get(0);
                    if (event instanceof StartEvent) {
                        for (Transition transition : process.getTransitions()) {
                            if (!activity.getId().equals(transition.getFrom())) continue;
                            String starttaskid = transition.getTo();
                            taskProps = newtasks.find("bpmnelementid", starttaskid);
                            if (taskProps == null) continue block3;
                            taskProps.setProperty("starttaskflag", "Y");
                            continue block3;
                        }
                        continue;
                    }
                    if (!(event instanceof com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.EndEvent)) continue;
                    for (Transition transition : process.getTransitions()) {
                        if (!activity.getId().equals(transition.getTo())) continue;
                        String endtaskid = transition.getFrom();
                        taskProps = newtasks.find("bpmnelementid", endtaskid);
                        if (taskProps == null) continue block3;
                        taskProps.setProperty("endtaskflag", "Y");
                        continue block3;
                    }
                }
            }
            if (xlpdpackage.getPools() != null && xlpdpackage.getPools().size() > 0) {
                for (Pool pool : xlpdpackage.getPools()) {
                    int n;
                    int poolX = 0;
                    boolean bl = false;
                    int poolW = 100;
                    int poolH = 100;
                    if (pool.isBoundaryVisible() && pool.getNodeGraphicsInfos() != null && pool.getNodeGraphicsInfos().size() > 0) {
                        if (out.getCollection("elements") == null) {
                            out.setProperty("elements", new PropertyListCollection());
                        }
                        poolX = (coordinates = pool.getNodeGraphicsInfos().get(0).getCoordinates()) != null ? ((Coordinate)coordinates).getX() : 0;
                        n = coordinates != null ? ((Coordinate)coordinates).getY() : 0;
                        poolW = pool.getNodeGraphicsInfos().get(0).getWidth();
                        poolH = pool.getNodeGraphicsInfos().get(0).getHeight();
                        PropertyList element = WorkflowDefPainter.Tools.BOX.getProperties("element" + (int)(Math.random() * 100.0), "" + (float)poolX * 1.33f, "" + n);
                        element.setProperty("width", "" + (float)poolW * 1.33f);
                        element.setProperty("height", "" + poolH);
                        element.setProperty("backgroundcolor", "#FFFFFF");
                        element.setProperty("linestyle", "dashed");
                        out.getCollection("elements").add(element);
                        element = WorkflowDefPainter.Tools.LABEL.getProperties("element" + (int)(Math.random() * 100.0), "" + ((float)poolX * 1.33f - 0.0f), "" + (n - 20));
                        element.setProperty("text", pool.getName());
                        element.setProperty("width", "" + (float)poolW * 1.33f);
                        element.setProperty("height", "" + poolH);
                        out.getCollection("elements").add(element);
                    }
                    if (pool.getLanes() == null || pool.getLanes().size() <= 0) continue;
                    coordinates = pool.getLanes().iterator();
                    while (coordinates.hasNext()) {
                        Coordinate coordinates3;
                        Lane lane = coordinates.next();
                        if (lane.getNodeGraphicsInfos() == null || lane.getNodeGraphicsInfos().size() <= 0) continue;
                        if (out.getCollection("elements") == null) {
                            out.setProperty("elements", new PropertyListCollection());
                        }
                        int x = ((coordinates3 = lane.getNodeGraphicsInfos().get(0).getCoordinates()) != null ? coordinates3.getX() : 0) + poolX;
                        int y = (coordinates3 != null ? coordinates3.getY() : 0) + n;
                        int width = lane.getNodeGraphicsInfos().get(0).getWidth();
                        int height = lane.getNodeGraphicsInfos().get(0).getHeight();
                        PropertyList element = WorkflowDefPainter.Tools.BOX.getProperties("element" + (int)(Math.random() * 100.0), "" + (float)x * 1.33f, "" + y);
                        element.setProperty("width", "" + (float)width * 1.33f);
                        element.setProperty("height", "" + height);
                        element.setProperty("backgroundcolor", "#FFFFFF");
                        out.getCollection("elements").add(element);
                        element = WorkflowDefPainter.Tools.LABEL.getProperties("element" + (int)(Math.random() * 100.0), "" + ((float)x * 1.33f + 10.0f), "" + (y + 10));
                        element.setProperty("text", lane.getName());
                        element.setProperty("width", "" + (float)width * 1.33f);
                        element.setProperty("height", "" + height);
                        out.getCollection("elements").add(element);
                    }
                }
            }
            if (xlpdpackage.getArtifacts() != null && xlpdpackage.getArtifacts().size() > 0) {
                for (Artifact artifact : xlpdpackage.getArtifacts()) {
                    int n;
                    if (artifact.getTextAnnotation() == null && artifact.getTextAnnotation().length() <= 0) continue;
                    if (out.getCollection("elements") == null) {
                        out.setProperty("elements", new PropertyListCollection());
                    }
                    int x = 0;
                    boolean bl = false;
                    int height = 100;
                    int width = 100;
                    if (artifact.getNodeGraphicsInfos() != null && artifact.getNodeGraphicsInfos().size() > 0) {
                        coordinates = artifact.getNodeGraphicsInfos().get(0).getCoordinates();
                        x = ((Coordinate)coordinates).getX();
                        n = ((Coordinate)coordinates).getY();
                        width = artifact.getNodeGraphicsInfos().get(0).getWidth();
                        height = artifact.getNodeGraphicsInfos().get(0).getHeight();
                    }
                    PropertyList element = WorkflowDefPainter.Tools.LABEL.getProperties("element" + (int)(Math.random() * 100.0), "" + (float)x * 1.33f, "" + n);
                    element.setProperty("text", artifact.getTextAnnotation());
                    element.setProperty("width", "" + (float)width * 1.33f);
                    element.setProperty("height", "" + height);
                    out.getCollection("elements").add(element);
                }
            }
        } else {
            Logger.logDebug("No process in package.");
        }
        return out;
    }

    private static void addIO(PropertyListCollection newtasks, String inputElementId, String outputElementId, String bpmnid) {
        PropertyList outputTaskProps;
        PropertyList inputTaskProps = newtasks.find("bpmnelementid", inputElementId);
        String taskinputioid = "";
        if (inputTaskProps != null) {
            if (inputTaskProps.getCollection("taskio") == null) {
                inputTaskProps.setProperty("taskio", new PropertyListCollection());
            }
            taskinputioid = "input" + (int)(Math.random() * 100.0);
            PropertyList intputIOProps = new PropertyList();
            intputIOProps.setProperty("ioid", taskinputioid);
            intputIOProps.setProperty("iodesc", "Input");
            intputIOProps.setProperty("ioflag", "I");
            intputIOProps.setProperty("connectortypeid", "Sample");
            intputIOProps.setProperty("bpmnelementid", bpmnid);
            inputTaskProps.getCollection("taskio").add(intputIOProps);
        }
        if ((outputTaskProps = newtasks.find("bpmnelementid", outputElementId)) != null) {
            if (outputTaskProps.getCollection("taskio") == null) {
                outputTaskProps.setProperty("taskio", new PropertyListCollection());
            }
            PropertyList outputIOProps = new PropertyList();
            outputIOProps.setProperty("ioid", "output" + (int)(Math.random() * 100.0));
            outputIOProps.setProperty("iodesc", "Output");
            outputIOProps.setProperty("ioflag", "O");
            outputIOProps.setProperty("connectortypeid", "Sample");
            outputIOProps.setProperty("bpmnelementid", bpmnid);
            if (inputTaskProps != null && taskinputioid.length() > 0) {
                outputIOProps.setProperty("connecttaskdefitemid", inputTaskProps.getProperty("taskdefitemid"));
                outputIOProps.setProperty("connectioid", taskinputioid);
            }
            outputTaskProps.getCollection("taskio").add(outputIOProps);
        }
    }

    private static PropertyList getPrototypeTask(String id, String name, String elementid, String shapeid) {
        String sn = name.length() > 18 ? name.substring(0, 18) + "..." : name;
        name = StringUtil.replaceAll(StringUtil.replaceAll(StringUtil.replaceAll(name, "\"", "&quote"), ">", "&rt;"), "<", "&lt;");
        sn = StringUtil.replaceAll(StringUtil.replaceAll(StringUtil.replaceAll(sn, "\"", "&quote"), ">", "&rt;"), "<", "&lt;");
        PropertyList taskProps = new PropertyList();
        taskProps.setProperty("prototype", "Y");
        taskProps.setProperty("taskdefid", WorkflowDefPainter.generateId(id, "", "", "__import_", false));
        taskProps.setProperty("taskdefitemid", WorkflowDefPainter.generateId(id, "1", "1", "__import_", false));
        taskProps.setProperty("longtitle", name);
        taskProps.setProperty("shorttitle", sn);
        taskProps.setProperty("taskdefvariantid", "1");
        taskProps.setProperty("taskdefversionid", "1");
        taskProps.setProperty("bpmnelementid", elementid);
        taskProps.setProperty("bpmnshapeid", shapeid);
        return taskProps;
    }

    public static PropertyList importBPMN(Definitions bpmndefinitions) {
        PropertyList out = new PropertyList();
        if (bpmndefinitions.getProcesses() != null && bpmndefinitions.getProcesses().size() > 0) {
            Process process = bpmndefinitions.getProcesses().get(0);
            if (bpmndefinitions.getDiagram() != null && bpmndefinitions.getDiagram().getPlanes() != null && bpmndefinitions.getDiagram().getPlanes().size() > 0) {
                PropertyListCollection newtasks;
                BPMNPlane plane = bpmndefinitions.getDiagram().getPlanes().get(0);
                if (plane.getShapes() != null && plane.getShapes().size() > 0) {
                    for (BPMNShape shape : plane.getShapes()) {
                        Group group;
                        Annotation annotation;
                        Task task = process.findTask(shape.getElement());
                        if (task != null) {
                            if (out.getCollection("tasks") == null) {
                                out.setProperty("tasks", new PropertyListCollection());
                            }
                            PropertyList taskProps = BPMNImporter.getPrototypeTask(task.getId(), task.getName(), task.getId(), shape.getId());
                            if (task instanceof ScriptTask) {
                                taskProps.setProperty("icon", "rc?command=image&image=CodeColored");
                            } else if (task instanceof UserTask) {
                                taskProps.setProperty("icon", "rc?command=image&image=User3");
                            } else if (task instanceof SendTask) {
                                taskProps.setProperty("icon", "rc?command=image&image=MailForward");
                            } else if (task instanceof ReceiveTask) {
                                taskProps.setProperty("icon", "rc?command=image&image=MailInto");
                            } else if (task instanceof ManualTask) {
                                taskProps.setProperty("icon", "rc?command=image&image=EmergencyStopButton");
                            } else if (task instanceof ServiceTask) {
                                taskProps.setProperty("icon", "rc?command=image&image=CloudComputingNetwork");
                            } else if (task instanceof BusinessRuleTask) {
                                taskProps.setProperty("icon", "rc?command=image&image=BranchElement");
                            } else if (task instanceof ExclusiveGateway) {
                                taskProps.setProperty("icon", "");
                                taskProps.setProperty("appearance", "DECISION");
                            } else if (task instanceof CallActivity) {
                                taskProps.setProperty("icon", "rc?command=image&image=Workstation");
                                taskProps.setProperty("appearance", "IMAGE");
                            } else {
                                taskProps.setProperty("icon", "rc?command=image&image=Blueprint");
                            }
                            if (shape.getBounds() != null && shape.getBounds().size() > 0) {
                                taskProps.setProperty("x", "" + Math.round(shape.getBounds().get(0).getX()));
                                taskProps.setProperty("y", "" + Math.round(shape.getBounds().get(0).getY()));
                            }
                            out.getCollection("tasks").add(taskProps);
                        }
                        if ((annotation = process.findAnnotation(shape.getElement())) != null) {
                            if (out.getCollection("elements") == null) {
                                out.setProperty("elements", new PropertyListCollection());
                            }
                            int x = 0;
                            int y = 0;
                            int height = 100;
                            int width = 100;
                            if (shape.getBounds() != null && shape.getBounds().size() > 0) {
                                x = Math.round(shape.getBounds().get(0).getX());
                                y = Math.round(shape.getBounds().get(0).getY());
                                width = Math.round(shape.getBounds().get(0).getWidth());
                                height = Math.round(shape.getBounds().get(0).getHeight());
                            }
                            PropertyList element = WorkflowDefPainter.Tools.TEXT.getProperties("element" + (int)(Math.random() * 100.0), "" + x, "" + y);
                            element.setProperty("text", annotation.getText());
                            element.setProperty("width", "" + width);
                            element.setProperty("height", "" + height);
                            out.getCollection("elements").add(element);
                        }
                        if ((group = process.findGroup(shape.getElement())) == null) continue;
                        if (out.getCollection("elements") == null) {
                            out.setProperty("elements", new PropertyListCollection());
                        }
                        int x = 0;
                        int y = 0;
                        int height = 100;
                        int width = 100;
                        if (shape.getBounds() != null && shape.getBounds().size() > 0) {
                            x = Math.round(shape.getBounds().get(0).getX());
                            y = Math.round(shape.getBounds().get(0).getY());
                            width = Math.round(shape.getBounds().get(0).getWidth());
                            height = Math.round(shape.getBounds().get(0).getHeight());
                        }
                        PropertyList element = WorkflowDefPainter.Tools.BOX.getProperties("element" + (int)(Math.random() * 100.0), "" + x, "" + y);
                        element.setProperty("width", "" + width);
                        element.setProperty("height", "" + height);
                        out.getCollection("elements").add(element);
                    }
                }
                if ((newtasks = out.getCollection("tasks")).size() > 0 && process.getFlow() != null && process.getFlow().size() > 0) {
                    for (SequenceFlow sequenceFlow : process.getFlow()) {
                        String outputElementId = sequenceFlow.getSource();
                        String inputElementId = sequenceFlow.getTarget();
                        BPMNImporter.addIO(newtasks, inputElementId, outputElementId, sequenceFlow.getId());
                    }
                    if (process.getStartEvents() != null && process.getStartEvents().size() > 0) {
                        block2: for (com.labvantage.sapphire.pageelements.workflow.bpmn.definition.StartEvent startEvent : process.getStartEvents()) {
                            for (SequenceFlow flow : process.getFlow()) {
                                String source = flow.getSource();
                                if (!source.equals(startEvent.getId())) continue;
                                String target = flow.getTarget();
                                PropertyList taskProps = newtasks.find("bpmnelementid", target);
                                if (taskProps == null) continue block2;
                                taskProps.setProperty("starttaskflag", "Y");
                                continue block2;
                            }
                        }
                    }
                    if (process.getEndEvents() != null && process.getEndEvents().size() > 0) {
                        block4: for (EndEvent endEvent : process.getEndEvents()) {
                            for (SequenceFlow flow : process.getFlow()) {
                                String target = flow.getTarget();
                                if (!target.equals(endEvent.getId())) continue;
                                String source = flow.getSource();
                                PropertyList taskProps = newtasks.find("bpmnelementid", source);
                                if (taskProps == null) continue block4;
                                taskProps.setProperty("endtaskflag", "Y");
                                continue block4;
                            }
                        }
                    }
                }
            } else {
                Logger.logDebug("No diagram or planes in diagram.");
            }
        } else {
            Logger.logDebug("No process in definition.");
        }
        return out;
    }

    public static PropertyList importBPMN(File f) throws SapphireException {
        try {
            if (f.getPath().toLowerCase().endsWith("xpdl")) {
                return BPMNImporter.importBPMN(BPMNUtil.readXPDL(f));
            }
            return BPMNImporter.importBPMN(BPMNUtil.readBPMN(f));
        }
        catch (JAXBException e) {
            Logger.logError("Error occured in JAX Parser", e);
            throw new SapphireException(e);
        }
        catch (IOException e) {
            Logger.logError("Error occured in Reading file.", e);
            throw new SapphireException(e);
        }
        catch (XMLStreamException e) {
            Logger.logError("Error occured in Reading xml string.", e);
            throw new SapphireException(e);
        }
    }

    public static PropertyList importBPMN(String xml) throws SapphireException {
        try {
            if (xml.contains("<Package ")) {
                return BPMNImporter.importBPMN(BPMNUtil.readXPDL(xml));
            }
            return BPMNImporter.importBPMN(BPMNUtil.readBPMN(xml));
        }
        catch (JAXBException e) {
            Logger.logError("Error occured in JAX Parser", e);
            throw new SapphireException(e);
        }
        catch (IOException e) {
            Logger.logError("Error occured in Reading xml string.", e);
            throw new SapphireException(e);
        }
        catch (XMLStreamException e) {
            Logger.logError("Error occured in Reading xml string.", e);
            throw new SapphireException(e);
        }
    }
}

