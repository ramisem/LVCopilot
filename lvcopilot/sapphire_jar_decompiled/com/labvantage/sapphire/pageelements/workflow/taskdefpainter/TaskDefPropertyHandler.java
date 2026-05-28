/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.workflow.taskdefpainter;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.modules.eventmanager.EventManager;
import com.labvantage.sapphire.modules.workflow.TaskDef;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefMaint;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class TaskDefPropertyHandler
extends PropertyHandler {
    public static final String PRIMARY_ACTION = "Primary";

    private void trimPropertyList(PropertyList childList, PropertyList parentList, String[] excludes) {
        List<String> excludeList = Arrays.asList(excludes);
        ArrayList<String> removeList = new ArrayList<String>();
        for (Object key : childList.keySet()) {
            if (!(childList.get(key) instanceof String)) continue;
            String propertyId = key.toString();
            String value = childList.getProperty(propertyId, "");
            if (value.length() == 0) {
                removeList.add(propertyId);
                continue;
            }
            if (excludeList.contains(propertyId) || !value.equalsIgnoreCase(parentList.getProperty(propertyId, ""))) continue;
            removeList.add(propertyId);
        }
        for (String id : removeList) {
            childList.remove(id);
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processProperties(HashMap props) throws SapphireException {
        PropertyListCollection taskios;
        PropertyList col;
        PropertyListCollection tasksteps;
        PropertyListCollection taskvariables;
        TaskDefMaint.Mode mode;
        PropertyList taskprops = props.get("properties") != null ? (PropertyList)props.get("properties") : new PropertyList();
        boolean syncNeeded = false;
        String sdcid = props.get("sdcid") != null ? props.get("sdcid").toString() : "";
        String keyid1 = props.get("keyid1") != null ? props.get("keyid1").toString() : "";
        String keyid2 = props.get("keyid2") != null ? props.get("keyid2").toString() : "";
        String keyid3 = props.get("keyid3") != null ? props.get("keyid3").toString() : "";
        String changeRequestId = props.getOrDefault("changerequestid", "");
        boolean skipthumbnail = props.get("skipthumbnail") != null ? props.get("skipthumbnail").toString().equalsIgnoreCase("Y") : false;
        String checkedOutToDeptId = props.getOrDefault("checkedouttodepartmentid", "");
        Object modeo = props.get("mode");
        if (modeo == null || modeo instanceof String && modeo.toString().length() == 0) {
            mode = TaskDefMaint.Mode.EDIT;
        } else if (modeo instanceof TaskDefMaint.Mode) {
            mode = (TaskDefMaint.Mode)((Object)modeo);
        } else {
            try {
                mode = TaskDefMaint.Mode.valueOf(modeo.toString().toUpperCase());
            }
            catch (Exception e) {
                mode = TaskDefMaint.Mode.EDIT;
            }
        }
        boolean descendant = props.get("descendant") != null ? props.get("descendant").toString().equalsIgnoreCase("Y") : false;
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty("sdcid", sdcid);
        actionProps.setProperty("keyid1", keyid1);
        actionProps.setProperty("keyid2", keyid2);
        actionProps.setProperty("keyid3", keyid3);
        actionProps.setProperty("taskdef", "");
        actionProps.setProperty("skipthumbnail", skipthumbnail ? "Y" : "N");
        if (descendant) {
            PropertyList descendantList = taskprops.getPropertyList("parentprops");
            PropertyList mergedList = taskprops;
            taskprops = taskprops.getPropertyList("childprops");
            if (descendantList == null) {
                descendantList = new PropertyList();
            }
            if (taskprops == null) {
                taskprops = new PropertyList();
            }
            if (mergedList.getProperty("taskdefdesc").length() > 0) {
                taskprops.setProperty("taskdefdesc", mergedList.getProperty("taskdefdesc"));
            }
            if (mergedList.getProperty("shorttitle").length() > 0 && !mergedList.getProperty("shorttitle").equals(descendantList.getProperty("shorttitle"))) {
                taskprops.setProperty("shorttitle", mergedList.getProperty("shorttitle"));
            }
            if (mergedList.getProperty("icon").length() > 0 && !mergedList.getProperty("icon").equals(descendantList.getProperty("icon"))) {
                taskprops.setProperty("icon", mergedList.getProperty("icon"));
            }
            if (mergedList.getProperty("appearance").length() > 0 && !mergedList.getProperty("appearance").equals(descendantList.getProperty("appearance"))) {
                taskprops.setProperty("appearance", mergedList.getProperty("appearance"));
            }
            if (mergedList.getProperty("taskcolor1").length() > 0 && !mergedList.getProperty("taskcolor1").equals(descendantList.getProperty("taskcolor1"))) {
                taskprops.setProperty("taskcolor1", mergedList.getProperty("taskcolor1"));
            }
            if (mergedList.getProperty("taskcolor2").length() > 0 && !mergedList.getProperty("taskcolor2").equals(descendantList.getProperty("taskcolor2"))) {
                taskprops.setProperty("taskcolor2", mergedList.getProperty("taskcolor2"));
            }
            if (mergedList.getProperty("taskcolor3").length() > 0 && !mergedList.getProperty("taskcolor3").equals(descendantList.getProperty("taskcolor3"))) {
                taskprops.setProperty("taskcolor3", mergedList.getProperty("taskcolor3"));
            }
            taskprops.remove("parentprops");
            taskprops.remove("childprops");
            taskprops.setProperty("autoexec", descendantList.getProperty("autoexec", "N"));
            actionProps.setProperty("basedontaskdefid", mergedList.getProperty("basedontaskdefid", ""));
            actionProps.setProperty("basedontaskdefversionid", mergedList.getProperty("basedontaskdefversionid", ""));
            actionProps.setProperty("basedontaskdefvariantid", mergedList.getProperty("basedontaskdefvariantid", ""));
            this.logDebug("Saving descendant task...");
        }
        if (keyid1.length() <= 0 || keyid2.length() <= 0 || keyid3.length() <= 0) throw new SapphireException("No task provided.");
        PropertyList sdc = new SDCProcessor(this.getConnectionInfo().getConnectionId()).getPropertyList("LV_TaskDef");
        for (Object key : taskprops.keySet()) {
            if (!(key instanceof String) || !(taskprops.get(key) instanceof String)) continue;
            boolean flag = false;
            PropertyList col2 = sdc.getCollection("columns").find("columnid", key.toString(), false);
            if (col2 == null && (col2 = sdc.getCollection("columns").find("columnid", key.toString() + "flag", false)) != null) {
                flag = true;
            }
            if (col2 == null || !col2.getProperty("datatype").equalsIgnoreCase("C")) continue;
            String v = taskprops.getProperty(key.toString(), "");
            if (v.length() > 0 && flag) {
                v = v.substring(0, 1).toUpperCase();
            }
            if (v.contains(";")) {
                v = v.replaceAll(";", "#semicolon#");
            }
            actionProps.setProperty(col2.getProperty("columnid"), v);
        }
        String actionid = "EditSDI";
        if (mode == TaskDefMaint.Mode.ADD) {
            boolean devMode;
            actionProps.setProperty("copies", "1");
            actionid = "AddSDI";
            ConfigurationProcessor config = new ConfigurationProcessor(this.getConnectionInfo().getConnectionId());
            try {
                devMode = config.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
            }
            catch (Exception e) {
                devMode = false;
            }
            actionProps.setProperty("coreflag", devMode ? "C" : "U");
            actionProps.setProperty("departmentid", checkedOutToDeptId);
            actionProps.setProperty("changerequestid", changeRequestId);
        }
        ActionBlock ab = new ActionBlock();
        try {
            ab.setAction(PRIMARY_ACTION, actionid, "1", actionProps);
        }
        catch (Exception e) {
            this.logError("Failed to add edit/add sdi action to block.", e);
            throw new SapphireException("Failed to save primary.");
        }
        if (mode != TaskDefMaint.Mode.ADD) {
            DBUtil dbUtil = new DBUtil(this.sapphireConnection.getConnectionId());
            dbUtil.setConnection(this.sapphireConnection);
            try {
                dbUtil.executePreparedUpdate("DELETE taskdefio WHERE taskdefid=? AND taskdefversionid=? AND taskdefvariantid=?", new Object[]{keyid1, keyid2, keyid3});
            }
            catch (Exception e) {
                this.logError("Failed to execute prepared delete statement", e);
                throw new SapphireException("Could not delete exsiting IO.");
            }
            try {
                dbUtil.executePreparedUpdate("DELETE taskdefstep WHERE taskdefid=? AND taskdefversionid=? AND taskdefvariantid=?", new Object[]{keyid1, keyid2, keyid3});
            }
            catch (Exception e) {
                this.logError("Failed to execute prepared delete statement", e);
                throw new SapphireException("Could not delete exsiting Steps.");
            }
        }
        if ((taskvariables = taskprops.getCollection("variables")) != null && taskvariables.size() > 0) {
            for (int i = 0; i < taskvariables.size(); ++i) {
                PropertyList variable = taskvariables.getPropertyList(i);
                if (!variable.containsKey("usages")) continue;
                variable.remove("usages");
            }
        }
        if ((tasksteps = taskprops.getCollection("steps")) != null && tasksteps.size() > 0) {
            PropertyList stepaction = new PropertyList();
            stepaction.setProperty("sdcid", sdcid);
            stepaction.setProperty("keyid1", keyid1);
            stepaction.setProperty("keyid2", keyid2);
            stepaction.setProperty("keyid3", keyid3);
            stepaction.setProperty("linkid", "taskdefstep_link");
            HashMap<String, String[]> hmprops = new HashMap<String, String[]>();
            for (int i = 0; i < tasksteps.size(); ++i) {
                PropertyList taskstep = tasksteps.getPropertyList(i);
                String stepid = taskstep.getProperty("stepid", "");
                if (stepid.length() <= 0) continue;
                taskstep.remove("steptypemerged");
                for (Object key : taskstep.keySet()) {
                    String[] sa;
                    if (!(key instanceof String)) continue;
                    String prop = key.toString();
                    if (hmprops.containsKey(prop)) {
                        sa = (String[])hmprops.get(prop);
                        if (sa == null) continue;
                        if (taskstep.get(key) instanceof String) {
                            sa[i] = taskstep.getProperty(prop);
                            continue;
                        }
                        if (!prop.equalsIgnoreCase("steptypeoverrides") || !(taskstep.get(key) instanceof PropertyList)) continue;
                        sa[i] = taskstep.getPropertyList(prop).toXMLString();
                        taskstep.setProperty(prop, new PropertyList());
                        continue;
                    }
                    sa = null;
                    boolean flag = false;
                    PropertyList link = sdc.getCollection("links").find("linkid", "taskdefstep_link", false);
                    if (link != null) {
                        boolean isstepdata = prop.equalsIgnoreCase("steptypeoverrides");
                        col = link.getCollection("linkcolumns").find("linkcolumnid", isstepdata ? "valuetree" : prop, false);
                        if (!isstepdata && col == null && (col = link.getCollection("linkcolumns").find("linkcolumnid", prop + "flag", false)) != null) {
                            flag = true;
                        }
                        if (col != null && (isstepdata && col.getProperty("linkdatatype").equalsIgnoreCase("T") || !isstepdata && col.getProperty("linkdatatype").equalsIgnoreCase("C"))) {
                            sa = new String[tasksteps.size()];
                            if (!isstepdata && key instanceof String && taskstep.get(key) instanceof String) {
                                sa[i] = taskstep.getProperty(prop).length() > 0 && flag ? taskstep.getProperty(prop).substring(0, 1).toUpperCase() : taskstep.getProperty(prop);
                            } else if (isstepdata && taskstep.get(key) instanceof PropertyList) {
                                sa[i] = taskstep.getPropertyList(prop).toXMLString();
                                taskstep.setProperty(prop, new PropertyList());
                            }
                        }
                    }
                    hmprops.put(flag ? prop + "flag" : prop, sa);
                }
                taskstep.remove("steptypeoverrides");
                taskstep.remove("extendnodeid");
                taskstep.remove("propertytreeid");
            }
            for (String prop : hmprops.keySet()) {
                String[] sa = (String[])hmprops.get(prop);
                if (sa == null) continue;
                StringBuffer sb = new StringBuffer();
                for (int p = 0; p < sa.length; ++p) {
                    String v;
                    String string = v = sa[p] == null ? "" : sa[p];
                    if (v.contains(";")) {
                        v = v.replaceAll(";", "#semicolon#");
                    }
                    if (p > 0) {
                        sb.append(";");
                    }
                    sb.append(v);
                }
                if (prop.equalsIgnoreCase("steptypeoverrides")) {
                    stepaction.setProperty("valuetree", sb.toString());
                    continue;
                }
                stepaction.setProperty(prop, sb.toString());
            }
            try {
                ab.setAction("AddStep", "AddSDIDetail", "1", stepaction);
            }
            catch (Exception e) {
                this.logError("Failed to add add sdi detail action to block.", e);
                throw new SapphireException("Failed to add io.");
            }
        }
        if ((taskios = taskprops.getCollection("taskio")) != null && taskios.size() > 0) {
            syncNeeded = true;
            PropertyList ioaction = new PropertyList();
            ioaction.setProperty("sdcid", sdcid);
            ioaction.setProperty("keyid1", keyid1);
            ioaction.setProperty("keyid2", keyid2);
            ioaction.setProperty("keyid3", keyid3);
            ioaction.setProperty("linkid", "taskdefio_link");
            HashMap<String, String[]> hmprops = new HashMap<String, String[]>();
            for (int i = 0; i < taskios.size(); ++i) {
                PropertyList taskio = taskios.getPropertyList(i);
                String ioid = taskio.getProperty("ioid", "");
                if (ioid.length() <= 0) continue;
                for (Object key : taskio.keySet()) {
                    String[] sa;
                    if (!(key instanceof String) || !(taskio.get(key) instanceof String)) continue;
                    String prop = key.toString();
                    if (hmprops.containsKey(prop)) {
                        sa = (String[])hmprops.get(prop);
                        if (sa == null) continue;
                        sa[i] = taskio.getProperty(prop);
                        continue;
                    }
                    sa = null;
                    boolean flag = false;
                    PropertyList link = sdc.getCollection("links").find("linkid", "taskdefio_link", false);
                    if (link != null) {
                        col = link.getCollection("linkcolumns").find("linkcolumnid", prop, false);
                        if (col == null && (col = link.getCollection("linkcolumns").find("linkcolumnid", prop + "flag", false)) != null) {
                            flag = true;
                        }
                        if (col != null && col.getProperty("linkdatatype").equalsIgnoreCase("C")) {
                            sa = new String[taskios.size()];
                            sa[i] = flag && taskio.getProperty(prop).length() > 0 ? taskio.getProperty(prop).substring(0, 1).toUpperCase() : taskio.getProperty(prop);
                        }
                    }
                    hmprops.put(flag ? prop + "flag" : prop, sa);
                }
            }
            for (String prop : hmprops.keySet()) {
                String[] sa = (String[])hmprops.get(prop);
                if (sa == null) continue;
                StringBuffer sb = new StringBuffer();
                for (int p = 0; p < sa.length; ++p) {
                    String v;
                    String string = v = sa[p] == null ? "" : sa[p];
                    if (v.contains(";")) {
                        v = v.replaceAll(";", "#semicolon#");
                    }
                    if (p > 0) {
                        sb.append(";");
                    }
                    sb.append(v);
                }
                ioaction.setProperty(prop, sb.toString());
            }
            try {
                ab.setAction("AddIO", "AddSDIDetail", "1", ioaction);
            }
            catch (Exception e) {
                this.logError("Failed to add add sdi detail action to block.", e);
                throw new SapphireException("Failed to add io.");
            }
        }
        if (taskprops.containsKey("categories")) {
            StringBuffer toDelete = new StringBuffer();
            StringBuffer toAdd = new StringBuffer();
            PropertyListCollection cats = taskprops.getCollection("categories");
            for (int i = 0; i < cats.size(); ++i) {
                PropertyList cat = cats.getPropertyList(i);
                String m = cat.getProperty("mode", "S");
                if (m.equalsIgnoreCase("I")) {
                    if (toAdd.length() > 0) {
                        toAdd.append(";");
                    }
                    toAdd.append(cat.getProperty("categoryid"));
                    continue;
                }
                if (!m.equalsIgnoreCase("D")) continue;
                if (toDelete.length() > 0) {
                    toDelete.append(";");
                }
                toDelete.append(cat.getProperty("categoryid"));
            }
            if (toAdd.length() > 0) {
                HashMap<String, String> addc = new HashMap<String, String>();
                addc.put("sdcid", "LV_TaskDef");
                addc.put("keyid1", keyid1);
                addc.put("keyid2", keyid2);
                addc.put("keyid3", keyid3);
                addc.put("categoryid", toAdd.toString());
                ab.setAction("AddCategoryItem", "AddCategoryItem", "1", addc);
            }
            if (toDelete.length() > 0) {
                HashMap<String, String> delc = new HashMap<String, String>();
                delc.put("sdcid", "LV_TaskDef");
                delc.put("keyid1", keyid1);
                delc.put("keyid2", keyid2);
                delc.put("keyid3", keyid3);
                delc.put("categoryid", toDelete.toString());
                ab.setAction("DeleteCategoryItem", "DeleteCategoryItem", "1", delc);
            }
            taskprops.remove("categories");
        }
        try {
            QueryProcessor queryProcessor;
            DataSet workflowexecs;
            PropertyList tasksdata;
            PropertyListCollection tasksdataCol;
            ab.getAction((String)PRIMARY_ACTION).properties.setProperty("taskdef", taskprops.toXMLString());
            new ActionProcessor(this.connectionInfo.getConnectionId()).processActionBlock(ab, false);
            CacheUtil.clear(this.sapphireConnection.getDatabaseId(), "TaskDef");
            CacheUtil.clear(this.sapphireConnection.getDatabaseId(), "TaskStepProps");
            CacheUtil.clear(this.sapphireConnection.getDatabaseId(), "TaskStepTypeProps");
            if (mode == TaskDefMaint.Mode.ADD) {
                DAMProcessor dam = new DAMProcessor(this.getConnectionInfo().getConnectionId());
                try {
                    String rset = dam.createLockedRSet("LV_TaskDef", keyid1, keyid2, keyid3);
                    props.put("rsetid", rset);
                }
                catch (Exception e) {
                    this.logError("Failed to lock new rset.", e);
                    throw new SapphireException("Failed to lock new task.");
                }
            }
            if (syncNeeded && (tasksdataCol = (tasksdata = TaskDefMaint.getTasksData(false, new SDIProcessor(this.getConnectionInfo().getConnectionId()), this.sapphireConnection, new Logger(this.getConnectionInfo().getConnectionId()))).getCollection("tasks")) != null && tasksdataCol.size() > 0) {
                Logger.logDebug("Finding descendant tasks... ");
                for (int t = 0; t < tasksdataCol.size(); ++t) {
                    PropertyList taskdata = tasksdataCol.getPropertyList(t);
                    if (!taskdata.getProperty("basedontaskdefid").equals(keyid1) || !taskdata.getProperty("basedontaskdefversionid").equals(keyid2) || !taskdata.getProperty("basedontaskdefvariantid").equals(keyid3)) continue;
                    Logger.logDebug("Decendant " + taskdata.getProperty("taskdefid") + " (" + taskdata.getProperty("taskdefversionid") + " - " + taskdata.getProperty("taskdefvariantid") + ") found. Syncronising...");
                    PropertyList descendantProps = TaskDef.getDescendantProperties(taskprops, taskdata);
                    descendantProps.setProperty("basedontaskdefid", keyid1);
                    descendantProps.setProperty("basedontaskdefversionid", keyid2);
                    descendantProps.setProperty("basedontaskdefvariantid", keyid3);
                    HashMap<String, Object> descendantHandlerProps = new HashMap<String, Object>();
                    descendantHandlerProps.put("properties", descendantProps);
                    descendantHandlerProps.put("sdcid", sdcid);
                    descendantHandlerProps.put("keyid1", taskdata.getProperty("taskdefid"));
                    descendantHandlerProps.put("keyid2", taskdata.getProperty("taskdefversionid"));
                    descendantHandlerProps.put("keyid3", taskdata.getProperty("taskdefvariantid"));
                    descendantHandlerProps.put("mode", TaskDefMaint.Mode.EDIT.toString());
                    descendantHandlerProps.put("descendant", "Y");
                    try {
                        this.processProperties(descendantHandlerProps);
                        Logger.logDebug("Decendant task " + taskdata.getProperty("taskdefid") + " (" + taskdata.getProperty("taskdefversionid") + " - " + taskdata.getProperty("taskdefvariantid") + ") Synced!");
                        continue;
                    }
                    catch (Exception e1) {
                        Logger.logError("Could not sync decendant task " + taskdata.getProperty("taskdefid") + " (" + taskdata.getProperty("taskdefversionid") + " - " + taskdata.getProperty("taskdefvariantid") + ").");
                        Logger.logError(e1.getMessage(), e1);
                    }
                }
            }
            if ((workflowexecs = (queryProcessor = new QueryProcessor(this.connectionInfo.getConnectionId())).getPreparedSqlDataSet("SELECT workflowexec.workflowexecid FROM   workflowexec, workflowdeftaskio, taskdefio WHERE  workflowexec.execstatus = 'A' AND    workflowexec.workflowdefid = workflowdeftaskio.workflowdefid AND    workflowexec.workflowdefversionid = workflowdeftaskio.workflowdefversionid AND    workflowexec.workflowdefvariantid = workflowdeftaskio.workflowdefvariantid AND    workflowdeftaskio.taskdefid = taskdefio.taskdefid AND    workflowdeftaskio.taskdefversionid = taskdefio.taskdefversionid AND    workflowdeftaskio.taskdefvariantid = taskdefio.taskdefvariantid AND    workflowdeftaskio.ioid = taskdefio.ioid AND    taskdefio.taskdefid = ? AND    taskdefio.taskdefversionid = ? AND    taskdefio.taskdefvariantid = ? AND    taskdefio.waittype = 'event'", new Object[]{keyid1, keyid2, keyid3})).size() <= 0 || EventManager.hasWorkflowEventPlan(this.sapphireConnection, workflowexecs.getValue(0, "workflowexecid"), false)) return;
            EventManager.loadEventPlans(this.sapphireConnection);
            return;
        }
        catch (Exception e) {
            this.logError("Failed to process action block.", e);
            throw new SapphireException("Failed to save.", e);
        }
    }
}

