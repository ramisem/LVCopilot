/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.bind.JAXBException
 */
package com.labvantage.sapphire.pageelements.workflow.bpmn;

import com.labvantage.sapphire.pageelements.workflow.bpmn.BPMNUtil;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.Annotation;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.BPMNBounds;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.BPMNDiagram;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.BPMNEdge;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.BPMNPlane;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.BPMNShape;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.BPMNWaypoint;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.Definitions;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.EndEvent;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.Incoming;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.Outgoing;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.Process;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.SequenceFlow;
import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.Task;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.Activity;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.Artifact;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.Event;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.NodeGraphicsInfo;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.Package;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.Pool;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.StartEvent;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.Transition;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.WorkflowProcess;
import com.labvantage.sapphire.pageelements.workflow.workflowdefpainter.WorkflowDefPainter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javax.xml.bind.JAXBException;
import sapphire.SapphireException;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class BPMNExporter {
    public static final float XPDL_X_ADJUSTMENT = 1.33f;
    public static final float BPMN_X_ADJUSTMENT = 1.33f;

    public static Package exportXPDL(PropertyList workflowProps, String userId) {
        PropertyListCollection elements;
        Package out = new Package();
        String wid = workflowProps.getProperty("workflowdefid") + ";" + workflowProps.getProperty("workflowdefversionid") + ";" + workflowProps.getProperty("workflowdefvariantid");
        out.setId(BPMNExporter.getXPDLId());
        out.setName(wid);
        out.getPackageHeader().setId(BPMNExporter.getXPDLId());
        out.getPackageHeader().setName(wid);
        out.getPackageHeader().setDocumentation(workflowProps.getProperty("workflowdefdesc"));
        out.getPackageHeader().setVendor("LabVantage");
        out.getPackageHeader().setXpdlVersion("2.2");
        out.getRedefinableHeader().setAuthor(userId);
        Pool pool = new Pool();
        pool.setId(BPMNExporter.getXPDLId());
        pool.setBoundaryVisible(false);
        pool.setName(workflowProps.getProperty("workflowdefid"));
        out.getPools().add(pool);
        NodeGraphicsInfo poolngi = new NodeGraphicsInfo();
        poolngi.setToolId("LabVantage");
        poolngi.setWidth(900);
        poolngi.setHeight(600);
        poolngi.getCoordinates().setX(40);
        poolngi.getCoordinates().setY(40);
        pool.getNodeGraphicsInfos().add(poolngi);
        List<WorkflowProcess> processes = out.getProcesses();
        WorkflowProcess process = new WorkflowProcess();
        process.setId(BPMNExporter.getXPDLId());
        process.setName("Main Process");
        processes.add(process);
        pool.setProcess(process.getId());
        List<Activity> activities = process.getActivities();
        PropertyListCollection tasks = workflowProps.getCollection("tasks");
        if (tasks != null) {
            PropertyList task;
            int t;
            HashMap<String, String> idmap = new HashMap<String, String>();
            for (t = 0; t < tasks.size(); ++t) {
                Transition trans;
                Event event;
                task = tasks.getPropertyList(t);
                Activity activity = new Activity();
                String aid = BPMNExporter.getXPDLId();
                idmap.put(task.getProperty("taskdefitemid"), aid);
                activity.setId(aid);
                activity.setName(task.getProperty("shorttitle"));
                activity.setDescription(task.getProperty("longtitle"));
                activity.getImplementation().getTask().setId(BPMNExporter.getXPDLId());
                activity.getImplementation().getTask().setName(task.getProperty("taskdefid") + ";" + task.getProperty("taskdefversionid") + ";" + task.getProperty("taskdefvariantid"));
                activity.setDocumentation(task.getProperty("instructions"));
                List<NodeGraphicsInfo> ngis = activity.getNodeGraphicsInfos();
                NodeGraphicsInfo ngi = new NodeGraphicsInfo();
                ngi.setToolId("LabVantage");
                ngi.setWidth(90);
                ngi.setHeight(60);
                ngi.setBorderColor("-16553830");
                ngi.setFillColor("-1249281");
                ngis.add(ngi);
                try {
                    ngi.getCoordinates().setX(Math.round(Float.parseFloat(task.getProperty("x"))));
                    ngi.getCoordinates().setY(Math.round(Float.parseFloat(task.getProperty("y"))));
                }
                catch (NumberFormatException e) {
                    Logger.logWarn("Could not obtain coordinates.");
                }
                activities.add(activity);
                if (task.getProperty("starttaskflag", "N").equalsIgnoreCase("Y")) {
                    Activity startactivity = new Activity();
                    String startId = BPMNExporter.getXPDLId();
                    startactivity.setId(startId);
                    NodeGraphicsInfo startngi = new NodeGraphicsInfo();
                    startngi.setToolId("LabVantage");
                    startngi.setWidth(30);
                    startngi.setHeight(30);
                    startngi.setBorderColor("-10311914");
                    startngi.setFillColor("-1638505");
                    startactivity.getNodeGraphicsInfos().add(startngi);
                    event = new StartEvent();
                    event.setTrigger("None");
                    startactivity.getEvents().add(event);
                    try {
                        startngi.getCoordinates().setX(Math.round(Float.parseFloat(task.getProperty("x"))) - 40);
                        startngi.getCoordinates().setY(Math.round(Float.parseFloat(task.getProperty("y"))) - 40);
                    }
                    catch (NumberFormatException e) {
                        Logger.logWarn("Could not obtain coordinates.");
                    }
                    startactivity.setImplementation(null);
                    activities.add(startactivity);
                    trans = new Transition();
                    trans.setId(BPMNExporter.getXPDLId());
                    trans.setFrom(startId);
                    trans.setTo(aid);
                    process.getTransitions().add(trans);
                }
                if (!task.getProperty("endtaskflag", "N").equalsIgnoreCase("Y")) continue;
                Activity endactivity = new Activity();
                String endId = BPMNExporter.getXPDLId();
                endactivity.setId(endId);
                NodeGraphicsInfo endngi = new NodeGraphicsInfo();
                endngi.setToolId("LabVantage");
                endngi.setWidth(30);
                endngi.setHeight(30);
                endngi.setBorderColor("-6750208");
                endngi.setFillColor("-1135958");
                endactivity.getNodeGraphicsInfos().add(endngi);
                event = new com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.EndEvent();
                event.setResult("None");
                endactivity.getEvents().add(event);
                try {
                    endngi.getCoordinates().setX(Math.round(Float.parseFloat(task.getProperty("x"))) + 130);
                    endngi.getCoordinates().setY(Math.round(Float.parseFloat(task.getProperty("y"))) + 100);
                }
                catch (NumberFormatException e) {
                    Logger.logWarn("Could not obtain coordinates.");
                }
                endactivity.setImplementation(null);
                activities.add(endactivity);
                trans = new Transition();
                trans.setId(BPMNExporter.getXPDLId());
                trans.setFrom(aid);
                trans.setTo(endId);
                process.getTransitions().add(trans);
            }
            for (t = 0; t < tasks.size(); ++t) {
                task = tasks.getPropertyList(t);
                PropertyListCollection ios = task.getCollection("taskio");
                for (int i = 0; i < ios.size(); ++i) {
                    PropertyList io = ios.getPropertyList(i);
                    if (!io.getProperty("ioflag", "I").equalsIgnoreCase("O")) continue;
                    String fid = task.getProperty("taskdefitemid");
                    String tid = io.getProperty("connecttaskdefitemid");
                    if (!idmap.containsKey(fid) || !idmap.containsKey(tid)) continue;
                    Transition trans = new Transition();
                    trans.setId(BPMNExporter.getXPDLId());
                    trans.setFrom((String)idmap.get(fid));
                    trans.setTo((String)idmap.get(tid));
                    process.getTransitions().add(trans);
                }
            }
        }
        if ((elements = workflowProps.getCollection("elements")) != null) {
            block20: for (int e = 0; e < elements.size(); ++e) {
                WorkflowDefPainter.Tools type;
                PropertyList element = elements.getPropertyList(e);
                try {
                    type = WorkflowDefPainter.Tools.valueOf(element.getProperty("type").toUpperCase());
                }
                catch (Exception e2) {
                    type = WorkflowDefPainter.Tools.LABEL;
                }
                boolean label = false;
                switch (type) {
                    case LABEL: {
                        label = true;
                    }
                    case TEXT: {
                        Artifact annotation = new Artifact();
                        annotation.setId(BPMNExporter.getXPDLId());
                        annotation.setArtifactType("Annotation");
                        annotation.setTextAnnotation(element.getProperty("text"));
                        NodeGraphicsInfo annotationNgi = new NodeGraphicsInfo();
                        annotationNgi.setToolId("LabVantage");
                        annotationNgi.setFillColor("-1");
                        try {
                            if (label) {
                                annotationNgi.setWidth(150);
                                annotationNgi.setHeight(20);
                                annotationNgi.setBorderColor("-1");
                            } else {
                                annotationNgi.setWidth(Math.round(Float.parseFloat(element.getProperty("width")) * 1.2f));
                                annotationNgi.setHeight(Math.round(Float.parseFloat(element.getProperty("height"))));
                                annotationNgi.setBorderColor("-5263441");
                            }
                            annotationNgi.getCoordinates().setX(Math.round(Float.parseFloat(element.getProperty("x"))));
                            annotationNgi.getCoordinates().setY(Math.round(Float.parseFloat(element.getProperty("y"))));
                        }
                        catch (NumberFormatException e2) {
                            Logger.logWarn("Could not obtain coordinates.");
                        }
                        annotation.getNodeGraphicsInfos().add(annotationNgi);
                        annotation.setGroup(null);
                        out.getArtifacts().add(annotation);
                        continue block20;
                    }
                    case BOX: {
                        Artifact group = new Artifact();
                        group.setId(BPMNExporter.getXPDLId());
                        group.setArtifactType("Group");
                        NodeGraphicsInfo groupNgi = new NodeGraphicsInfo();
                        groupNgi.setToolId("LabVantage");
                        groupNgi.setBorderColor("-11834615");
                        groupNgi.setFillColor("-6571175");
                        try {
                            groupNgi.setWidth(Math.round(Float.parseFloat(element.getProperty("width"))));
                            groupNgi.setHeight(Math.round(Float.parseFloat(element.getProperty("height"))));
                            groupNgi.getCoordinates().setX(Math.round(Float.parseFloat(element.getProperty("x"))));
                            groupNgi.getCoordinates().setY(Math.round(Float.parseFloat(element.getProperty("y"))));
                        }
                        catch (NumberFormatException e2) {
                            Logger.logWarn("Could not obtain coordinates.");
                        }
                        group.getNodeGraphicsInfos().add(groupNgi);
                        group.getGroup().setId(BPMNExporter.getXPDLId());
                        out.getArtifacts().add(group);
                    }
                }
            }
        }
        return out;
    }

    private static String getXPDLId() {
        return BPMNExporter.getRandomHexString(8) + "-" + BPMNExporter.getRandomHexString(4) + "-" + BPMNExporter.getRandomHexString(4) + "-" + BPMNExporter.getRandomHexString(4) + "-" + BPMNExporter.getRandomHexString(12);
    }

    private static String getRandomHexString(int numchars) {
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        while (sb.length() < numchars) {
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, numchars);
    }

    public static Definitions exportBPMN(PropertyList workflowProps, String userId) {
        PropertyListCollection elements;
        Definitions out = new Definitions();
        int bpmnIdCount = 1;
        int lvIdCount = 1;
        String wid = workflowProps.getProperty("workflowdefid") + "_" + workflowProps.getProperty("workflowdefversionid") + "_" + workflowProps.getProperty("workflowdefvariantid");
        out.setId(wid);
        out.setName(workflowProps.getProperty("workflowdefid"));
        out.setExporter("LabVantage");
        out.setExporterVersion("8");
        out.setTargetNamespace("http://sourceforge.net/bpmn/definitions/_1384949576338");
        List<Process> processes = out.getProcesses();
        Process process = new Process();
        process.setId("PROCESS_1");
        process.setClosed(false);
        process.setExecutable(true);
        process.setProcessType("None");
        process.setName("Main Process");
        processes.add(process);
        List<Task> bpmntasks = process.getTasks();
        List<com.labvantage.sapphire.pageelements.workflow.bpmn.definition.StartEvent> bpmnstarts = process.getStartEvents();
        List<EndEvent> bpmnends = process.getEndEvents();
        List<Annotation> bpmnannotations = process.getAnnotations();
        List<SequenceFlow> bpmnSequenceflows = process.getFlow();
        PropertyListCollection lvstasks = workflowProps.getCollection("tasks");
        BPMNDiagram bpmnDiagram = out.getDiagram();
        bpmnDiagram.setId("lvs_" + lvIdCount);
        bpmnDiagram.setName(workflowProps.getProperty("workflowdefid") + " Diagram");
        List<BPMNPlane> bpmnplanes = bpmnDiagram.getPlanes();
        BPMNPlane bpmnplane = new BPMNPlane();
        bpmnplane.setId("lvs_" + ++lvIdCount);
        ++lvIdCount;
        bpmnplane.setElement(process.getId());
        bpmnplanes.add(bpmnplane);
        List<BPMNShape> bpmnShapes = bpmnplane.getShapes();
        List<BPMNEdge> bpmnEdges = bpmnplane.getEdges();
        if (lvstasks != null) {
            int t;
            HashMap<String, String> idmap = new HashMap<String, String>();
            for (t = 0; t < lvstasks.size(); ++t) {
                String flowid;
                SequenceFlow flow2;
                PropertyList task = lvstasks.getPropertyList(t);
                Task bpmntask = new Task();
                String tid = "_" + bpmnIdCount;
                ++bpmnIdCount;
                bpmntask.setId(tid);
                idmap.put(task.getProperty("taskdefitemid"), tid);
                bpmntask.setCompletionQuantity(1);
                bpmntask.setForCompensation(false);
                bpmntask.setStartQuantity(1);
                bpmntask.setName(task.getProperty("shorttitle", task.getProperty("longtitle", task.getProperty("taskdefitemid"))));
                bpmntask.setImplementation(null);
                BPMNShape bpmnShape = new BPMNShape();
                bpmnShape.setId("lvs_" + lvIdCount);
                ++lvIdCount;
                bpmnShapes.add(bpmnShape);
                bpmnShape.setElement(tid);
                List<BPMNBounds> bounds = bpmnShape.getBounds();
                BPMNBounds bound = new BPMNBounds();
                bound.setHeight(60.0f);
                bound.setWidth(100.0f);
                try {
                    bound.setX(Float.parseFloat(task.getProperty("x", "1")) / 1.33f);
                    bound.setY(Float.parseFloat(task.getProperty("y", "1")));
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
                bounds.add(bound);
                bpmntasks.add(bpmntask);
                if (task.getProperty("starttaskflag", "N").equalsIgnoreCase("Y")) {
                    com.labvantage.sapphire.pageelements.workflow.bpmn.definition.StartEvent startevent = new com.labvantage.sapphire.pageelements.workflow.bpmn.definition.StartEvent();
                    String startId = "_" + bpmnIdCount;
                    ++bpmnIdCount;
                    startevent.setId(startId);
                    startevent.setInterrupting(true);
                    startevent.setName("Start Event");
                    startevent.setParallelMultiple(false);
                    List<Outgoing> outgoings = startevent.getOutputs();
                    Outgoing outgoing = new Outgoing();
                    BPMNShape bpmnStartShape = new BPMNShape();
                    bpmnStartShape.setId("lvs_" + lvIdCount);
                    ++lvIdCount;
                    bpmnShapes.add(bpmnStartShape);
                    bpmnStartShape.setElement(startId);
                    List<BPMNBounds> startbounds = bpmnStartShape.getBounds();
                    BPMNBounds startbound = new BPMNBounds();
                    startbound.setHeight(32.0f);
                    startbound.setWidth(32.0f);
                    try {
                        startbound.setX(bound.getX());
                        startbound.setY(bound.getY() + 60.0f + 60.0f);
                    }
                    catch (NumberFormatException numberFormatException) {
                        // empty catch block
                    }
                    startbounds.add(startbound);
                    flow2 = new SequenceFlow();
                    flowid = "_" + bpmnIdCount;
                    ++bpmnIdCount;
                    flow2.setId(flowid);
                    flow2.setSource(startId);
                    flow2.setTarget(tid);
                    bpmnSequenceflows.add(flow2);
                    BPMNEdge bpmnStartEdge = new BPMNEdge();
                    bpmnStartEdge.setId("lvs_" + lvIdCount);
                    ++lvIdCount;
                    bpmnStartEdge.setSourceElement(startId);
                    bpmnStartEdge.setTargetElement(tid);
                    bpmnEdges.add(bpmnStartEdge);
                    bpmnStartEdge.setElement(flowid);
                    List<BPMNWaypoint> startedgewaypoints = bpmnStartEdge.getWaypoints();
                    BPMNWaypoint startedgewaypoint = new BPMNWaypoint();
                    try {
                        startedgewaypoint.setX(1.0f);
                        startedgewaypoint.setY(1.0f);
                    }
                    catch (NumberFormatException numberFormatException) {
                        // empty catch block
                    }
                    startedgewaypoints.add(startedgewaypoint);
                    startedgewaypoint = new BPMNWaypoint();
                    try {
                        startedgewaypoint.setX(1.0f);
                        startedgewaypoint.setY(1.0f);
                    }
                    catch (NumberFormatException numberFormatException) {
                        // empty catch block
                    }
                    startedgewaypoints.add(startedgewaypoint);
                    outgoing.setFlowId(flowid);
                    outgoings.add(outgoing);
                    bpmnstarts.add(startevent);
                }
                if (!task.getProperty("endtaskflag", "N").equalsIgnoreCase("Y")) continue;
                EndEvent endEvent = new EndEvent();
                String endId = "_" + bpmnIdCount;
                ++bpmnIdCount;
                endEvent.setId(endId);
                endEvent.setName("End Event");
                List<Incoming> incomings = endEvent.getInputs();
                Incoming incoming = new Incoming();
                BPMNShape bpmnEndShape = new BPMNShape();
                bpmnEndShape.setId("lvs_" + lvIdCount);
                ++lvIdCount;
                bpmnShapes.add(bpmnEndShape);
                bpmnEndShape.setElement(endId);
                List<BPMNBounds> endbounds = bpmnEndShape.getBounds();
                BPMNBounds endbound = new BPMNBounds();
                endbound.setHeight(32.0f);
                endbound.setWidth(32.0f);
                try {
                    endbound.setX(bound.getX() + 100.0f + 32.0f);
                    endbound.setY(bound.getY() + 60.0f);
                }
                catch (NumberFormatException flow2) {
                    // empty catch block
                }
                endbounds.add(endbound);
                flow2 = new SequenceFlow();
                flowid = "_" + bpmnIdCount;
                ++bpmnIdCount;
                flow2.setId(flowid);
                flow2.setSource(tid);
                flow2.setTarget(endId);
                bpmnSequenceflows.add(flow2);
                BPMNEdge bpmnEndEdge = new BPMNEdge();
                bpmnEndEdge.setId("lvs_" + lvIdCount);
                ++lvIdCount;
                bpmnEndEdge.setSourceElement(tid);
                bpmnEndEdge.setTargetElement(endId);
                bpmnEdges.add(bpmnEndEdge);
                bpmnEndEdge.setElement(flowid);
                List<BPMNWaypoint> endedgewaypoints = bpmnEndEdge.getWaypoints();
                BPMNWaypoint endedgewaypoint = new BPMNWaypoint();
                try {
                    endedgewaypoint.setX(1.0f);
                    endedgewaypoint.setY(1.0f);
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
                endedgewaypoints.add(endedgewaypoint);
                endedgewaypoint = new BPMNWaypoint();
                try {
                    endedgewaypoint.setX(1.0f);
                    endedgewaypoint.setY(1.0f);
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
                endedgewaypoints.add(endedgewaypoint);
                incoming.setFlowId(flowid);
                incomings.add(incoming);
                bpmnends.add(endEvent);
            }
            for (t = 0; t < lvstasks.size(); ++t) {
                PropertyList lvtask = lvstasks.getPropertyList(t);
                PropertyListCollection ios = lvtask.getCollection("taskio");
                for (int i = 0; i < ios.size(); ++i) {
                    PropertyList io = ios.getPropertyList(i);
                    if (!io.getProperty("ioflag", "I").equalsIgnoreCase("O")) continue;
                    String fid = lvtask.getProperty("taskdefitemid");
                    String tid = io.getProperty("connecttaskdefitemid");
                    if (!idmap.containsKey(fid) || !idmap.containsKey(tid)) continue;
                    SequenceFlow sequenceFlow = new SequenceFlow();
                    String sequenceFlowId = "_" + bpmnIdCount;
                    sequenceFlow.setId(sequenceFlowId);
                    ++bpmnIdCount;
                    String fromId = (String)idmap.get(fid);
                    String toId = (String)idmap.get(tid);
                    sequenceFlow.setSource(fromId);
                    sequenceFlow.setTarget(toId);
                    bpmnSequenceflows.add(sequenceFlow);
                    BPMNEdge bpmnEdge = new BPMNEdge();
                    bpmnEdge.setId("lvs_" + lvIdCount);
                    ++lvIdCount;
                    bpmnEdge.setSourceElement(fromId);
                    bpmnEdge.setTargetElement(tid);
                    bpmnEdges.add(bpmnEdge);
                    bpmnEdge.setElement(sequenceFlowId);
                    List<BPMNWaypoint> edgewaypoints = bpmnEdge.getWaypoints();
                    BPMNWaypoint edgewaypoint = new BPMNWaypoint();
                    try {
                        edgewaypoint.setX(1.0f);
                        edgewaypoint.setY(1.0f);
                    }
                    catch (NumberFormatException numberFormatException) {
                        // empty catch block
                    }
                    edgewaypoints.add(edgewaypoint);
                    edgewaypoint = new BPMNWaypoint();
                    try {
                        edgewaypoint.setX(1.0f);
                        edgewaypoint.setY(1.0f);
                    }
                    catch (NumberFormatException numberFormatException) {
                        // empty catch block
                    }
                    edgewaypoints.add(edgewaypoint);
                }
            }
        }
        if ((elements = workflowProps.getCollection("elements")) != null) {
            for (int e = 0; e < elements.size(); ++e) {
                WorkflowDefPainter.Tools type;
                PropertyList element = elements.getPropertyList(e);
                try {
                    type = WorkflowDefPainter.Tools.valueOf(element.getProperty("type").toUpperCase());
                }
                catch (Exception e2) {
                    type = WorkflowDefPainter.Tools.LABEL;
                }
                boolean label = false;
                switch (type) {
                    case LABEL: {
                        label = true;
                    }
                    case TEXT: {
                        Annotation annotation = new Annotation();
                        String e_id = "_" + bpmnIdCount;
                        annotation.setId(e_id);
                        ++bpmnIdCount;
                        annotation.setTextFormat("text/plain");
                        annotation.setText(element.getProperty("text", ""));
                        bpmnannotations.add(annotation);
                        BPMNShape bpmnShape = new BPMNShape();
                        bpmnShape.setId("lvs_" + lvIdCount);
                        ++lvIdCount;
                        bpmnShapes.add(bpmnShape);
                        bpmnShape.setElement(e_id);
                        List<BPMNBounds> bounds = bpmnShape.getBounds();
                        BPMNBounds bound = new BPMNBounds();
                        if (label) {
                            bound.setWidth(150.0f);
                            bound.setHeight(20.0f);
                        } else {
                            bound.setWidth(Math.round(Float.parseFloat(element.getProperty("width")) / 1.33f));
                            bound.setHeight(Math.round(Float.parseFloat(element.getProperty("height"))));
                        }
                        try {
                            bound.setX(Float.parseFloat(element.getProperty("x", "1")) / 1.33f);
                            bound.setY(Float.parseFloat(element.getProperty("y", "1")));
                        }
                        catch (NumberFormatException numberFormatException) {
                            // empty catch block
                        }
                        bounds.add(bound);
                    }
                }
            }
        }
        return out;
    }

    public static String exportBPMN(PropertyList workflowProps, String userId, boolean xpdl) throws SapphireException {
        try {
            if (xpdl) {
                return BPMNUtil.writeXPDL(BPMNExporter.exportXPDL(workflowProps, userId));
            }
            return BPMNUtil.writeBPMN(BPMNExporter.exportBPMN(workflowProps, userId));
        }
        catch (JAXBException e) {
            Logger.logError("Error occured in JAX Writer", e);
            throw new SapphireException(e);
        }
        catch (IOException e) {
            Logger.logError("Error occured in Writing xml string.", e);
            throw new SapphireException(e);
        }
    }
}

