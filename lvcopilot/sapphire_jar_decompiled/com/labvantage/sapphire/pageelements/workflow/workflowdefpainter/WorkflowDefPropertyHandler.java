/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.workflow.workflowdefpainter;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.modules.eventmanager.EventManager;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefMaint;
import com.labvantage.sapphire.pageelements.workflow.workflowdefpainter.WorkflowDefMaint;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class WorkflowDefPropertyHandler
extends PropertyHandler {
    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processProperties(HashMap props) throws SapphireException {
        PropertyListCollection tasks;
        WorkflowDefMaint.Mode mode;
        PropertyList workflowprops = props.get("properties") != null ? (PropertyList)props.get("properties") : new PropertyList();
        String sdcid = props.get("sdcid") != null ? props.get("sdcid").toString() : "";
        String keyid1 = props.get("keyid1") != null ? props.get("keyid1").toString() : "";
        String keyid2 = props.get("keyid2") != null ? props.get("keyid2").toString() : "";
        String keyid3 = props.get("keyid3") != null ? props.get("keyid3").toString() : "";
        boolean skipthumbnail = props.get("skipthumbnail") != null ? props.get("skipthumbnail").toString().equalsIgnoreCase("Y") : false;
        String changeRequestId = props.getOrDefault("changerequestid", "");
        String checkedOutToDeptId = props.getOrDefault("checkedouttodepartmentid", "");
        Object modeo = props.get("mode");
        if (modeo == null || modeo instanceof String && modeo.toString().length() == 0) {
            mode = WorkflowDefMaint.Mode.EDIT;
        } else if (modeo instanceof WorkflowDefMaint.Mode) {
            mode = (WorkflowDefMaint.Mode)((Object)modeo);
        } else {
            try {
                mode = WorkflowDefMaint.Mode.valueOf(modeo.toString().toUpperCase());
            }
            catch (Exception e) {
                mode = WorkflowDefMaint.Mode.EDIT;
            }
        }
        if (workflowprops.containsKey("__exec")) {
            workflowprops.remove("__exec");
        }
        if (workflowprops.containsKey("__queue")) {
            workflowprops.remove("__queue");
        }
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty("sdcid", sdcid);
        actionProps.setProperty("keyid1", keyid1);
        actionProps.setProperty("keyid2", keyid2);
        actionProps.setProperty("keyid3", keyid3);
        actionProps.setProperty("workflowdef", "");
        actionProps.setProperty("skipthumbnail", skipthumbnail ? "Y" : "N");
        if (keyid1.length() <= 0 || keyid2.length() <= 0 || keyid3.length() <= 0) throw new SapphireException("No workflow provided.");
        PropertyList sdc = new SDCProcessor(this.getConnectionInfo().getConnectionId()).getPropertyList("LV_WorkflowDef");
        for (Object key : workflowprops.keySet()) {
            if (!(key instanceof String) || !(workflowprops.get(key) instanceof String)) continue;
            boolean flag = false;
            PropertyList col = sdc.getCollection("columns").find("columnid", key.toString(), false);
            if (col == null && (col = sdc.getCollection("columns").find("columnid", key.toString() + "flag", false)) != null) {
                flag = true;
            }
            if (col == null || !col.getProperty("datatype").equalsIgnoreCase("C")) continue;
            actionProps.setProperty(col.getProperty("columnid"), flag && workflowprops.getProperty(key.toString(), "").length() > 0 ? workflowprops.getProperty(key.toString(), "").substring(0, 1).toUpperCase() : workflowprops.getProperty(key.toString(), ""));
        }
        String actionid = "EditSDI";
        if (mode == WorkflowDefMaint.Mode.ADD) {
            actionProps.setProperty("copies", "1");
            actionid = "AddSDI";
            actionProps.setProperty("departmentid", checkedOutToDeptId);
            actionProps.setProperty("changerequestid", changeRequestId);
        }
        ActionBlock ab = new ActionBlock();
        try {
            ab.setAction("Primary", actionid, "1", actionProps);
        }
        catch (Exception e) {
            this.logError("Failed to add edit/add sdi action to block.", e);
            throw new SapphireException("Failed to save primary.");
        }
        if (mode != WorkflowDefMaint.Mode.ADD) {
            DBUtil dbUtil = new DBUtil(this.sapphireConnection.getConnectionId());
            dbUtil.setConnection(this.sapphireConnection);
            try {
                dbUtil.executePreparedUpdate("DELETE workflowdeftaskio WHERE workflowdefid=? AND workflowdefversionid=? AND workflowdefvariantid=?", new Object[]{keyid1, keyid2, keyid3});
            }
            catch (Exception e) {
                this.logError("Failed to execute prepared delete statement", e);
                throw new SapphireException("Could not delete exsiting IO.");
            }
            try {
                dbUtil.executePreparedUpdate("DELETE workflowdeftask WHERE workflowdefid=? AND workflowdefversionid=? AND workflowdefvariantid=?", new Object[]{keyid1, keyid2, keyid3});
            }
            catch (Exception e) {
                this.logError("Failed to execute prepared delete statement", e);
                throw new SapphireException("Could not delete exsiting Tasks.");
            }
        }
        if ((tasks = workflowprops.getCollection("tasks")) != null && tasks.size() > 0) {
            PropertyList prototype = tasks.find("prototype", "Y");
            if (prototype == null) {
                PropertyList taskaction = new PropertyList();
                taskaction.setProperty("sdcid", sdcid);
                taskaction.setProperty("keyid1", keyid1);
                taskaction.setProperty("keyid2", keyid2);
                taskaction.setProperty("keyid3", keyid3);
                taskaction.setProperty("linkid", "workflowdeftask_link");
                HashMap<String, String[]> hmTaskprops = new HashMap<String, String[]>();
                for (int i = 0; i < tasks.size(); ++i) {
                    String[] sa_sf;
                    String taskdefitemid;
                    boolean startable = false;
                    PropertyList task = tasks.getPropertyList(i);
                    if (task.containsKey("__modified")) {
                        task.remove("__modified");
                    }
                    if ((taskdefitemid = task.getProperty("taskdefitemid", "")).length() > 0) {
                        if (task.containsKey("__exec")) {
                            task.remove("__exec");
                        }
                        if (task.containsKey("__queue")) {
                            task.remove("__queue");
                        }
                        if (task.containsKey("bubbletext")) {
                            task.remove("bubbletext");
                        }
                        if (task.containsKey("bubbletitle")) {
                            task.remove("bubbletitle");
                        }
                        if (task.getProperty("starttaskflag", "N").equalsIgnoreCase("Y")) {
                            startable = true;
                        }
                        for (Object key : task.keySet()) {
                            if (!(key instanceof String) || !(task.get(key) instanceof String)) continue;
                            String prop = key.toString();
                            if (hmTaskprops.containsKey(prop)) {
                                String[] sa = (String[])hmTaskprops.get(prop);
                                if (sa == null) continue;
                                sa[i] = task.getProperty(prop);
                                continue;
                            }
                            boolean flag = false;
                            String[] sa = null;
                            PropertyList link = sdc.getCollection("links").find("linkid", "workflowdeftask_link", false);
                            if (link != null) {
                                PropertyList col = link.getCollection("linkcolumns").find("linkcolumnid", prop, false);
                                if (col == null && (col = link.getCollection("linkcolumns").find("linkcolumnid", prop + "flag", false)) != null) {
                                    flag = true;
                                }
                                if (col != null && col.getProperty("linkdatatype").equalsIgnoreCase("C")) {
                                    sa = new String[tasks.size()];
                                    sa[i] = flag && task.getProperty(prop).length() > 0 ? task.getProperty(prop).substring(0, 1).toUpperCase() : task.getProperty(prop);
                                }
                            }
                            hmTaskprops.put(flag ? prop + "flag" : prop, sa);
                        }
                        PropertyListCollection taskios = task.getCollection("taskio");
                        if (taskios != null && taskios.size() > 0) {
                            PropertyList ioaction = new PropertyList();
                            ioaction.setProperty("sdcid", sdcid);
                            ioaction.setProperty("keyid1", keyid1);
                            ioaction.setProperty("keyid2", keyid2);
                            ioaction.setProperty("keyid3", keyid3);
                            ioaction.setProperty("linkid", "workflowdeftaskio_link");
                            HashMap<String, Integer> ioitemidmap = new HashMap<String, Integer>();
                            HashMap<String, String[]> hmIOprops = new HashMap<String, String[]>();
                            PropertyList ownerlist = new PropertyList();
                            SDIData ownertaskdef = TaskDefMaint.getTaskData("LV_TaskDef", task.getProperty("taskdefid"), task.getProperty("taskdefversionid"), task.getProperty("taskdefvariantid"), false, new SDIProcessor(this.getConnectionInfo().getConnectionId()), new Logger(this.getConnectionInfo().getConnectionId()), ownerlist);
                            for (int k = 0; k < taskios.size(); ++k) {
                                PropertyList taskio = taskios.getPropertyList(k);
                                if (taskio.containsKey("bubbletext")) {
                                    taskio.remove("bubbletext");
                                }
                                if (taskio.containsKey("bubbletitle")) {
                                    taskio.remove("bubbletitle");
                                }
                                String ioid = taskio.getProperty("ioid", "");
                                if (startable && taskio.getProperty("ioflag").equalsIgnoreCase("I")) {
                                    startable = false;
                                }
                                taskio.setProperty("taskdefitemid", taskdefitemid);
                                if (ownertaskdef != null && ownertaskdef.getDataset("primary") != null && ownertaskdef.getDataset("primary").getRowCount() > 0 && ownertaskdef.getDataset("primary").getValue(0, "basedontaskdefid", "").length() > 0) {
                                    taskio.setProperty("taskdefid", ownertaskdef.getDataset("primary").getValue(0, "basedontaskdefid", ""));
                                    taskio.setProperty("taskdefversionid", ownertaskdef.getDataset("primary").getValue(0, "basedontaskdefversionid", ""));
                                    taskio.setProperty("taskdefvariantid", ownertaskdef.getDataset("primary").getValue(0, "basedontaskdefvariantid", ""));
                                } else {
                                    taskio.setProperty("taskdefid", task.getProperty("taskdefid"));
                                    taskio.setProperty("taskdefversionid", task.getProperty("taskdefversionid"));
                                    taskio.setProperty("taskdefvariantid", task.getProperty("taskdefvariantid"));
                                }
                                if (ioid.length() > 0) {
                                    Integer integer;
                                    if (ioitemidmap.containsKey(ioid)) {
                                        integer = (Integer)ioitemidmap.get(ioid);
                                        integer = integer + 1;
                                        ioitemidmap.remove(ioid);
                                    } else {
                                        integer = 1;
                                    }
                                    ioitemidmap.put(ioid, integer);
                                    taskio.setProperty("ioitemid", ioid + "_" + integer);
                                    for (Object key : taskio.keySet()) {
                                        if (!(key instanceof String) || !(taskio.get(key) instanceof String)) continue;
                                        String prop = key.toString();
                                        if (hmIOprops.containsKey(prop)) {
                                            String[] sa = (String[])hmIOprops.get(prop);
                                            if (sa == null) continue;
                                            sa[k] = taskio.getProperty(prop);
                                            continue;
                                        }
                                        boolean flag = false;
                                        String[] sa = null;
                                        PropertyList link = sdc.getCollection("links").find("linkid", "workflowdeftaskio_link", false);
                                        if (link != null) {
                                            PropertyList col = link.getCollection("linkcolumns").find("linkcolumnid", prop, false);
                                            if (col == null && (col = link.getCollection("linkcolumns").find("linkcolumnid", prop + "flag", false)) != null) {
                                                flag = true;
                                            }
                                            if (col != null && col.getProperty("linkdatatype").equalsIgnoreCase("C")) {
                                                sa = new String[taskios.size()];
                                                sa[k] = flag && taskio.getProperty(prop).length() > 0 ? taskio.getProperty(prop).substring(0, 1).toUpperCase() : taskio.getProperty(prop);
                                            }
                                        }
                                        hmIOprops.put(flag ? prop + "flag" : prop, sa);
                                    }
                                }
                                taskio.remove("taskdefitemid");
                                taskio.remove("taskdefid");
                                taskio.remove("taskdefversionid");
                                taskio.remove("taskdefvariantid");
                            }
                            for (String prop : hmIOprops.keySet()) {
                                String[] sa = (String[])hmIOprops.get(prop);
                                if (sa == null) continue;
                                StringBuffer sb = new StringBuffer();
                                for (int p = 0; p < sa.length; ++p) {
                                    if (p > 0) {
                                        sb.append(";");
                                    }
                                    sb.append(sa[p] == null ? "" : sa[p]);
                                }
                                ioaction.setProperty(prop, sb.toString());
                            }
                            try {
                                ab.setAction("AddIO" + taskdefitemid, "AddSDIDetail", "1", ioaction);
                            }
                            catch (Exception e) {
                                this.logError("Failed to add add sdi detail action to block.", e);
                                throw new SapphireException("Failed to add io.");
                            }
                        }
                    }
                    if (hmTaskprops.containsKey("startableflag")) {
                        sa_sf = (String[])hmTaskprops.get("startableflag");
                    } else {
                        sa_sf = new String[tasks.size()];
                        hmTaskprops.put("startableflag", sa_sf);
                    }
                    sa_sf[i] = startable ? "Y" : "N";
                    task.setProperty("startableflag", startable ? "Y" : "N");
                }
                for (String prop : hmTaskprops.keySet()) {
                    String[] sa = (String[])hmTaskprops.get(prop);
                    if (sa == null) continue;
                    StringBuffer sb = new StringBuffer();
                    for (int p = 0; p < sa.length; ++p) {
                        if (p > 0) {
                            sb.append(";");
                        }
                        sb.append(sa[p] == null ? "" : sa[p]);
                    }
                    taskaction.setProperty(prop, sb.toString());
                }
                try {
                    ab.setAction("AddTask", "AddSDIDetail", "1", taskaction);
                }
                catch (Exception e) {
                    this.logError("Failed to add add sdi detail action to block.", e);
                    throw new SapphireException("Failed to add tasks.");
                }
            }
            this.logInfo("Prototype task found therefore workflow saved as prototype.");
        }
        if (workflowprops.containsKey("categories")) {
            StringBuffer toDelete = new StringBuffer();
            StringBuffer toAdd = new StringBuffer();
            PropertyListCollection cats = workflowprops.getCollection("categories");
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
                addc.put("sdcid", "LV_WorkflowDef");
                addc.put("keyid1", keyid1);
                addc.put("keyid2", keyid2);
                addc.put("keyid3", keyid3);
                addc.put("categoryid", toAdd.toString());
                ab.setAction("AddCategoryItem", "AddCategoryItem", "1", addc);
            }
            if (toDelete.length() > 0) {
                HashMap<String, String> delc = new HashMap<String, String>();
                delc.put("sdcid", "LV_WorkflowDef");
                delc.put("keyid1", keyid1);
                delc.put("keyid2", keyid2);
                delc.put("keyid3", keyid3);
                delc.put("categoryid", toDelete.toString());
                ab.setAction("DeleteCategoryItem", "DeleteCategoryItem", "1", delc);
            }
            workflowprops.remove("categories");
        }
        try {
            QueryProcessor queryProcessor;
            DataSet workflowexecs;
            ab.setActionProperty("Primary", "workflowdef", workflowprops.toXMLString());
            new ActionProcessor(this.connectionInfo.getConnectionId()).processActionBlock(ab, false);
            if (mode == WorkflowDefMaint.Mode.ADD) {
                DAMProcessor dam = new DAMProcessor(this.getConnectionInfo().getConnectionId());
                try {
                    String rset = dam.createLockedRSet("LV_WorkflowDef", keyid1, keyid2, keyid3);
                    props.put("rsetid", rset);
                }
                catch (Exception e) {
                    this.logError("Failed to lock new rset.", e);
                    throw new SapphireException("Failed to lock new workflow.");
                }
            }
            if ((workflowexecs = (queryProcessor = new QueryProcessor(this.connectionInfo.getConnectionId())).getPreparedSqlDataSet("SELECT workflowexecid FROM workflowexec WHERE workflowdefid = ? AND workflowdefversionid = ? AND workflowdefvariantid = ?", new Object[]{keyid1, keyid2, keyid3})).size() <= 0 || EventManager.hasWorkflowEventPlan(this.sapphireConnection, workflowexecs.getValue(0, "workflowexecid"), false)) return;
            EventManager.loadEventPlans(this.sapphireConnection);
            return;
        }
        catch (Exception e) {
            this.logError("Failed to process action block.", e);
            throw new SapphireException("Failed to save.");
        }
    }
}

