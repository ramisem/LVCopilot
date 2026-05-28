/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.workflow.workflowdefpainter;

import com.labvantage.opal.elements.advancedtoolbar.AdvancedToolbar;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.modules.workflow.TaskDef;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.controls.Tab;
import com.labvantage.sapphire.pageelements.controls.TabGroup;
import com.labvantage.sapphire.pageelements.maint.DataView;
import com.labvantage.sapphire.pageelements.maint.EditorStyleField;
import com.labvantage.sapphire.pageelements.maint.MaintDetail;
import com.labvantage.sapphire.pageelements.propertybuilder.PropertyBuilder;
import com.labvantage.sapphire.pageelements.workflow.WorkflowConstants;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefMaint;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefVariables;
import com.labvantage.sapphire.pageelements.workflow.workflowdefpainter.WorkflowDefPainter;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import com.labvantage.sapphire.tagext.QueryData;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import com.labvantage.sapphire.xml.PropertyDefinition;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.tagext.SDITagInfo;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class WorkflowDefMaint
extends BaseElement
implements WorkflowConstants {
    public static final String JS_CLASS = "workflowMaint";
    public static final String USERCONFIG_PREFIX = "workflowmaint_";
    public static final String PROPERTY_DEBUG = "debug";
    public static final String PROPERTY_SDCID = "sdcid";
    public static final String PROPERTY_KEYID1 = "keyid1";
    public static final String PROPERTY_KEYID2 = "keyid2";
    public static final String PROPERTY_KEYID3 = "keyid3";
    public static final String PROPERTY_VIEWONLY = "viewonly";
    public static final String ATTRIBUTE_CHANGED = "_changed";
    public static final String SDCID = "LV_WorkflowDef";
    public static final String DATACOL = "workflowdef";
    public static final String LINK_TASK = "workflowdeftask_link";
    public static final String LINK_IO = "workflowdeftaskio_link";
    public static final String RESOURCE_FILE = "workflowdef.xml";
    private String keyid1;
    private String keyid2;
    private String keyid3;
    private String sdcid = "LV_WorkflowDef";
    private PropertyList userConfig;
    private boolean viewonly = false;
    private boolean devMode;
    private boolean debug = false;
    PropertyList workflowprops = null;
    private Mode mode = Mode.EDIT;
    private PropertyList layout = null;
    private PropertyList maintElement = null;
    private SDIData workflowData = null;
    private String rsetid = "";
    private boolean locked = false;
    private PropertyDefinitionList propertyDef = null;
    boolean changeMade = false;
    private String pageTitle = "";
    private boolean isChangeControlled = false;

    public static boolean isChangeControlled(String connectionId) {
        CMTPolicy cmtPolicy = CMTPolicy.getPolicy(connectionId, SDCID);
        return "Y".equals(cmtPolicy.getChangeControlledFlag());
    }

    public WorkflowDefMaint(PageContext pageContext, PropertyList pageproperties) {
        this.setPageContext(pageContext);
        try {
            DataSet pri;
            ConfigurationProcessor config = new ConfigurationProcessor(pageContext);
            try {
                this.devMode = config.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
            }
            catch (Exception e) {
                this.devMode = false;
            }
            PropertyBuilder pb = new PropertyBuilder(pageContext, RESOURCE_FILE);
            this.propertyDef = pb.getPropertyDefinition();
            this.setUpProperties(pageproperties, (HttpServletRequest)pageContext.getRequest());
            this.workflowprops = new PropertyList();
            if (this.keyid1.length() > 0) {
                this.mode = Mode.EDIT;
                this.workflowData = WorkflowDefMaint.getWorkflowData(this.sdcid, this.keyid1, this.keyid2, this.keyid3, !this.viewonly, this.propertyDef, this.getSDIProcessor(), this.getConnectionProcessor().getSapphireConnection(), this.logger, this.workflowprops, true, false);
                this.rsetid = this.workflowData.getRsetid();
                pri = this.workflowData.getDataset("primary");
                if (pri != null) {
                    if (this.workflowprops.getAttribute(ATTRIBUTE_CHANGED).equalsIgnoreCase("Y")) {
                        this.changeMade = true;
                    }
                    this.locked = pri.getColumnValues("__lockedby", "").length() > 0;
                    String currCompCode = Configuration.getCompcode(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).getDatabaseId());
                    String dataCompCode = pri.getValue(0, "compcode", "");
                    if (currCompCode != null && currCompCode.length() > 0) {
                        if (dataCompCode != null && dataCompCode.length() > 0 && !currCompCode.equals(dataCompCode) && !this.devMode) {
                            this.logger.debug("WorkFlow created as part of different component " + dataCompCode + ".");
                            this.viewonly = true;
                        }
                    } else if (dataCompCode.length() > 0 && !this.devMode) {
                        this.logger.debug("WorkFlow created as part of component " + dataCompCode + ".");
                        this.viewonly = true;
                    }
                }
                if (this.locked) {
                    this.viewonly = true;
                }
                if (pri != null && pri.size() == 1) {
                    String isCMTLockOkStr;
                    if (!pri.getValue(0, "versionstatus", "P").equalsIgnoreCase("P")) {
                        this.logger.debug("Version status current or expired thus view only mode enabled.");
                        this.viewonly = true;
                    }
                    if (WorkflowDefMaint.isChangeControlled(this.getConnectionId()) && !"Y".equals(isCMTLockOkStr = pri.getValue(0, "isCMTLockOk", "Y"))) {
                        this.viewonly = true;
                    }
                } else {
                    this.workflowprops = null;
                    this.logger.error("No task data found.");
                }
            } else {
                this.mode = Mode.ADD;
                this.keyid2 = "1";
                this.keyid3 = "1";
                this.workflowData = new SDIData(this.sdcid, "workflowdefid", "workflowdefversionid", "workflowdefvariantid");
                pri = new DataSet();
                pri.addColumn("workflowdefid", 0);
                pri.addColumn("workflowdefversionid", 0);
                pri.addColumn("workflowdefvariantid", 0);
                pri.addColumn("workflowdefdesc", 0);
                pri.addColumn("__rowstatus", 0);
                pri.addColumn("__lockstate", 0);
                pri.addColumn("__lockedby", 0);
                pri.addRow();
                pri.setValue(0, "workflowdefid", this.keyid1);
                pri.setValue(0, "workflowdefversionid", this.keyid2);
                pri.setValue(0, "workflowdefvariantid", this.keyid3);
                pri.setValue(0, "__rowstatus", "I");
                this.workflowData.setDataset("primary", pri);
                DataSet cats = new DataSet();
                cats.addColumn("categoryid", 0);
                cats.addColumn(PROPERTY_SDCID, 0);
                cats.addColumn(PROPERTY_KEYID1, 0);
                cats.addColumn("__rowstatus", 0);
                cats.addColumn("__rsetseq", 0);
                cats.addColumn("__lockstate", 0);
                cats.addColumn("__lockedby", 0);
                this.workflowData.setDataset("category", cats);
                this.workflowprops.setProperty("exectypeflag", "S");
                this.workflowprops.setProperty("connector", WorkflowDefPainter.Connector.FLOWCHART.toString());
                if (this.getConnectionProcessor().getSapphireConnection().isRtl()) {
                    this.workflowprops.setProperty("rtl", "Y");
                }
            }
            pri = this.workflowData.getDataset("primary");
            HashMap<String, QueryData> querymap = new HashMap<String, QueryData>();
            querymap.put("primary", new QueryData("primary", pri));
            querymap.put("category", new QueryData("category", this.workflowData.getDataset("category")));
            this.sdiInfo = new SDITagInfo(querymap);
            this.sdiInfo.setSDIData(this.workflowData);
            this.sdiInfo.setSdcid(SDCID);
            if (pri != null && pri.size() == 1 && this.propertyDef != null) {
                for (int i = 0; i < this.propertyDef.size(); ++i) {
                    String dbvalue;
                    PropertyDefinition pd = (PropertyDefinition)this.propertyDef.get(i);
                    if (!pd.getType().equalsIgnoreCase("simple")) continue;
                    String prop = pd.getId();
                    String propvalue = this.workflowprops.getProperty(prop, "");
                    if (!pri.isValidColumn(prop)) {
                        pri.addColumn(prop, 0);
                        pri.setValue(0, prop, propvalue);
                        continue;
                    }
                    if (this.mode == Mode.ADD || (dbvalue = pri.getValue(0, prop, "")).equals(propvalue)) continue;
                    if (dbvalue.length() == 0) {
                        pri.setValue(0, prop, propvalue);
                    } else {
                        this.workflowprops.setProperty(prop, dbvalue);
                    }
                    this.changeMade = true;
                }
                if (this.changeMade) {
                    pri.setValue(0, "__rowstatus", "U");
                }
            } else {
                this.workflowprops = null;
                this.logger.error("No workflow data found.");
            }
            this.setUpLayout(this.layout);
        }
        catch (Exception e) {
            this.workflowprops = null;
            this.logger.error("Could not set up painter: " + e.getMessage(), e);
        }
        this.logger.debug("Set up completed.");
    }

    public static boolean isValidTaskSwap(PropertyList oldTask, PropertyList newTask, PropertyListCollection otherTasks, boolean strict) {
        boolean canContinue = false;
        PropertyListCollection oldTaskIOs = oldTask.getCollection("taskio");
        if (oldTaskIOs == null || oldTaskIOs.size() == 0) {
            canContinue = true;
        } else {
            PropertyListCollection newTaskIOs = newTask.getCollection("taskio");
            if (newTaskIOs != null) {
                int old_iCount = 0;
                int old_oCount = 0;
                int new_iCount = 0;
                int new_oCount = 0;
                ArrayList<PropertyList> foundI = new ArrayList<PropertyList>();
                ArrayList<PropertyList> foundO = new ArrayList<PropertyList>();
                block0: for (int io = 0; io < oldTaskIOs.size(); ++io) {
                    PropertyList newTaskIO;
                    int find;
                    boolean linked;
                    PropertyList oldTaskIO = oldTaskIOs.getPropertyList(io);
                    if (oldTaskIO.getProperty("ioflag", "I").equalsIgnoreCase("O")) {
                        boolean bl = linked = oldTaskIO.getProperty("connecttaskdefitemid", "").length() > 0 && oldTaskIO.getProperty("connectioid", "").length() > 0;
                        if (!strict && (strict || !linked)) continue;
                        ++old_oCount;
                        for (find = 0; find < newTaskIOs.size(); ++find) {
                            newTaskIO = newTaskIOs.getPropertyList(find);
                            if (!newTaskIO.getProperty("ioflag", "I").equalsIgnoreCase("O") || !newTaskIO.getProperty("connectortypeid", "").equals(oldTaskIO.getProperty("connectortypeid")) || foundO.contains(newTaskIO)) continue;
                            foundO.add(newTaskIO);
                            ++new_oCount;
                            continue block0;
                        }
                        continue;
                    }
                    linked = false;
                    if (!strict && otherTasks != null) {
                        for (int t = 0; t < otherTasks.size(); ++t) {
                            PropertyList tt = otherTasks.getPropertyList(t);
                            PropertyListCollection tti = tt.getCollection("taskio");
                            if (tti != null) {
                                for (int tk = 0; tk < tti.size(); ++tk) {
                                    PropertyList ttio = tti.getPropertyList(tk);
                                    if (!ttio.getProperty("ioflag").equalsIgnoreCase("O") || !ttio.getProperty("connecttaskdefitemid").equals(oldTask.getProperty("taskdefitemid")) || !ttio.getProperty("connectioid").equals(oldTaskIO.getProperty("ioid"))) continue;
                                    linked = true;
                                    break;
                                }
                            }
                            if (linked) break;
                        }
                    }
                    if (!strict && (strict || !linked)) continue;
                    ++old_iCount;
                    for (find = 0; find < newTaskIOs.size(); ++find) {
                        newTaskIO = newTaskIOs.getPropertyList(find);
                        if (!newTaskIO.getProperty("ioflag", "I").equalsIgnoreCase("I") || !newTaskIO.getProperty("connectortypeid", "").equals(oldTaskIO.getProperty("connectortypeid")) || foundI.contains(newTaskIO)) continue;
                        foundI.add(newTaskIO);
                        ++new_iCount;
                        continue block0;
                    }
                }
                if (old_iCount == new_iCount && old_oCount == new_oCount) {
                    canContinue = true;
                }
            }
        }
        return canContinue;
    }

    private void setUpProperties(PropertyList pagedata, HttpServletRequest request) throws Exception {
        this.debug = pagedata.getProperty(PROPERTY_DEBUG, "N").equalsIgnoreCase("Y");
        this.layout = pagedata.getPropertyList("layout");
        this.maintElement = pagedata.getPropertyList("maint");
        this.keyid1 = pagedata.getProperty(PROPERTY_KEYID1, "");
        this.logger.debug("keyid1 = " + this.keyid1);
        this.keyid2 = pagedata.getProperty(PROPERTY_KEYID2, "");
        this.logger.debug("keyid2 = " + this.keyid2);
        this.keyid3 = pagedata.getProperty(PROPERTY_KEYID3, "");
        this.logger.debug("keyid3 = " + this.keyid3);
        this.viewonly = pagedata.getProperty(PROPERTY_VIEWONLY, "n").equalsIgnoreCase("y");
        this.logger.debug("viewonly = " + this.viewonly);
        this.userConfig = RequestContext.getInstance(request).getPropertyList("userconfig");
        if (this.userConfig == null) {
            throw new SapphireException("User configuration could not be obtained.");
        }
    }

    private void setUpLayout(PropertyList layout) {
        if (layout != null) {
            if (layout.getProperty("objectname", "").endsWith("/genericlayout.jsp")) {
                layout.setProperty("ribbon", "Y");
                layout.setProperty("showmenubar", "Y");
            } else {
                layout.setProperty("hideshadow", "Y");
            }
        }
    }

    public static PropertyList getTaskData(String keyid1, String keyid2, String keyid3, SDIProcessor sdiProcessor, SapphireConnection sapphireConnection, Logger logger) {
        String parentKeyid1;
        PropertyList toolsdata = TaskDefMaint.getTasksData(true, sdiProcessor, sapphireConnection, logger);
        PropertyList toolprops = null;
        if (toolsdata.getCollection("tasks") != null && (toolprops = toolsdata.getCollection("tasks").find("taskkey", keyid1 + ";" + keyid2 + ";" + keyid3)) != null && (parentKeyid1 = toolprops.getProperty("basedontaskdefid", "")).length() > 0) {
            String parentKeyid2 = toolprops.getProperty("basedontaskdefversionid", "");
            String parentKeyid3 = toolprops.getProperty("basedontaskdefvariantid", "");
            PropertyList parentToolList = TaskDefMaint.getTaskData("LV_TaskDef", parentKeyid1, parentKeyid2, parentKeyid3, false, sdiProcessor, logger);
            toolprops = TaskDef.getDescendantProperties(parentToolList, toolprops);
            toolprops.setProperty("basedontaskdefid", parentKeyid1);
            toolprops.setProperty("basedontaskdefversionid", parentKeyid2);
            toolprops.setProperty("basedontaskdefvariantid", parentKeyid3);
        }
        return toolprops;
    }

    protected static PropertyList getWorkflowData(String sdcid, String keyid1, String keyid2, String keyid3, boolean lock, PropertyDefinitionList taskDef, SDIProcessor sdi, SapphireConnection sapphireConnection, Logger logger, boolean showQueues, boolean painteronly) {
        PropertyList out = new PropertyList();
        WorkflowDefMaint.getWorkflowData(sdcid, keyid1, keyid2, keyid3, lock, taskDef, sdi, sapphireConnection, logger, out, showQueues, painteronly);
        return out;
    }

    protected static SDIData getWorkflowData(String sdcid, String keyid1, String keyid2, String keyid3, boolean lock, PropertyDefinitionList workflowDef, SDIProcessor sdi, SapphireConnection sapphireConnection, Logger logger, PropertyList outProps, boolean showQueues, boolean painteronly) {
        Object workflowprops = null;
        SDIData sdiData = null;
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid(sdcid);
        boolean propertiesChanged = false;
        if (keyid2.length() == 0) {
            keyid2 = "1";
        }
        if (keyid3.length() == 0) {
            keyid3 = "1";
        }
        if (keyid1.length() > 0) {
            sdiRequest.setKeyid1List(keyid1);
            sdiRequest.setKeyid2List(keyid2);
            sdiRequest.setKeyid3List(keyid3);
            if (lock) {
                sdiRequest.setRetainRsetid(true);
                sdiRequest.setDataLockOption("LA");
                sdiRequest.setLockOption("LA");
                sdiRequest.setPrimaryLockOption("LA");
                sdiRequest.setAutoLockTimeout(true);
            }
            sdiRequest.setExtendedDataTypes(true);
            sdiRequest.setRequestItem("primary");
            sdiRequest.setRequestItem("category");
            if (WorkflowDefMaint.isChangeControlled(sdi.getConnectionid())) {
                sdiRequest.setValidateCheckout(true);
            }
            if ((sdiData = sdi.getSDIData(sdiRequest)) != null) {
                DataSet pri = sdiData.getDataset("primary");
                if (pri != null && pri.getRowCount() > 0) {
                    String workflowdefs = pri.getClob(0, DATACOL, "");
                    if (workflowdefs.length() > 0) {
                        try {
                            PropertyListCollection varscol;
                            DataSet execitems;
                            outProps.setPropertyList(workflowdefs, false, false);
                            outProps.setProperty("categories", WorkflowDefMaint.getCategories(keyid1, keyid2, sdiData.getDataset("category")));
                            QueryProcessor qp = new QueryProcessor(sdi.getConnectionid());
                            DataSet dataSet = execitems = painteronly ? null : qp.getPreparedSqlDataSet("taskexecitems", "SELECT taskdefitemid FROM taskexecitem WHERE workflowexecid IN (SELECT workflowexecid FROM workflowexec WHERE workflowdefid = ? AND workflowdefversionid = ? AND workflowdefvariantid = ?)", new Object[]{keyid1, keyid2, keyid3});
                            if (execitems != null && execitems.getRowCount() > 0) {
                                outProps.setProperty("__exec", "Y");
                            }
                            if ((varscol = outProps.getCollection("variables")) != null && varscol.size() > 0) {
                                for (int v = 0; v < varscol.size(); ++v) {
                                    PropertyList var = varscol.getPropertyList(v);
                                    if (!var.containsKey("scope")) continue;
                                    if (!var.containsKey("setup")) {
                                        var.setProperty("setup", var.getProperty("scope", "L").equalsIgnoreCase("S") ? "Y" : "N");
                                    }
                                    var.remove("scope");
                                    propertiesChanged = true;
                                }
                            }
                            DataSet queueitems = qp.getPreparedSqlDataSet("queueitems", "SELECT taskqueueid, ioid, taskdefitemid, taskdefid, taskdefversionid, taskdefvariantid, workflowdefid, workflowdefversionid, workflowdefvariantid queuesdcid, queuekeyid1, queuekeyid2, queuekeyid3, queuestatus FROM   taskqueue WHERE  workflowdefid = ? AND workflowdefversionid = ? AND workflowdefvariantid = ? ORDER BY taskqueueid DESC, ioid, queuesdcid, queuekeyid1, queuekeyid2, queuekeyid3", new Object[]{keyid1, keyid2, keyid3});
                            boolean _queue = false;
                            if (queueitems != null && queueitems.getRowCount() > 0 || execitems != null && execitems.getRowCount() > 0) {
                                _queue = true;
                                if (queueitems != null && queueitems.getRowCount() > 0) {
                                    outProps.setProperty("__queue", "" + queueitems.getRowCount());
                                    if (showQueues) {
                                        outProps.setProperty("bubbletext", "" + queueitems.getRowCount());
                                        outProps.setProperty("bubbletitle", "" + (queueitems.getRowCount() == 1 ? queueitems.getRowCount() + " queued item" : queueitems.getRowCount() + " queued items"));
                                    }
                                }
                            }
                            PropertyDefinitionList taskDef = workflowDef != null ? (workflowDef.getPropertyDef("tasks") != null ? workflowDef.getPropertyDef("tasks").getPropertyDefinitionList() : null) : null;
                            PropertyListCollection tasks = outProps.getCollection("tasks");
                            for (int i = 0; i < tasks.size(); ++i) {
                                int r;
                                PropertyList basedOnTask;
                                PropertyList task = tasks.getPropertyList(i);
                                if (!task.containsKey("taskdefversionid")) {
                                    task.setProperty("taskdefversionid", "1");
                                }
                                if (!task.containsKey("taskdefvariantid")) {
                                    task.setProperty("taskdefvariantid", "1");
                                }
                                String taskdefitemid = task.getProperty("taskdefitemid");
                                if (!task.getProperty("prototype", "N").equalsIgnoreCase("Y") && taskDef != null && (basedOnTask = WorkflowDefMaint.getTaskData(task.getProperty("taskdefid", ""), task.getProperty("taskdefversionid", ""), task.getProperty("taskdefvariantid", ""), sdi, sapphireConnection, logger)) != null) {
                                    WorkflowDefPainter.copyAccrossProperties(taskDef, task, basedOnTask);
                                }
                                if (!_queue) continue;
                                if (execitems != null && execitems.size() > 0 && (r = execitems.findRow("taskdefitemid=" + taskdefitemid + "")) > -1) {
                                    task.setProperty("__exec", "Y");
                                }
                                if (queueitems == null || queueitems.getRowCount() <= 0) continue;
                                HashMap<String, String> filter = new HashMap<String, String>();
                                filter.put("taskdefitemid", taskdefitemid);
                                DataSet filteredQueueItems = queueitems.getFilteredDataSet(filter);
                                if (filteredQueueItems.getRowCount() <= 0) continue;
                                task.setProperty("__queue", "" + filteredQueueItems.getRowCount());
                                if (!showQueues) continue;
                                task.setProperty("bubbletext", "" + filteredQueueItems.getRowCount());
                                task.setProperty("bubbletitle", "" + (filteredQueueItems.getRowCount() == 1 ? filteredQueueItems.getRowCount() + " queued item" : filteredQueueItems.getRowCount() + " queued items"));
                            }
                        }
                        catch (Exception e) {
                            logger.error("Cannot obtain workflow definition.", e);
                        }
                    }
                } else {
                    logger.debug("Could not obtain " + sdcid + " data.");
                }
            } else {
                logger.error("Could not obtain sdidata for " + sdcid + ".");
            }
        } else {
            logger.debug("No keyid1 provided, this in add mode.");
        }
        if (outProps != null && propertiesChanged) {
            outProps.setAttribute(ATTRIBUTE_CHANGED, "Y");
        }
        return sdiData;
    }

    private static PropertyListCollection getCategories(String workflowId, String version, DataSet cats) {
        PropertyListCollection categories = new PropertyListCollection();
        if (cats != null && cats.getRowCount() > 0) {
            for (int i = 0; i < cats.getRowCount(); ++i) {
                String catid = cats.getValue(i, "categoryid", "");
                String keyid1 = cats.getValue(i, PROPERTY_KEYID1, "");
                if (catid.length() <= 0 || !keyid1.equalsIgnoreCase(workflowId)) continue;
                PropertyList cat = new PropertyList();
                cat.setProperty("categoryid", catid);
                cat.setProperty("mode", "S");
                categories.add(cat);
            }
        }
        return categories;
    }

    private StringBuffer getScriptAndStyle() {
        StringBuffer html = new StringBuffer();
        boolean expandedMode = true;
        html.append("\n<script> ");
        html.append("document.title='").append(this.pageTitle.replaceAll("\\<.*?\\>", "")).append("';");
        html.append("</script>");
        html.append(JavaScriptAPITag.getJQueryAPI(true, false, null, "", !this.devMode, this.pageContext));
        html.append("\n<script type=\"text/javascript\" src=\"WEB-CORE/elements/workflow/scripts/workflowmaint").append(expandedMode ? "" : ".min").append(".js\"></script>");
        html.append("\n<script> ");
        html.append("workflowMaint.isChangeControlled = ").append(this.isChangeControlled).append(";");
        html.append("</script>");
        html.append("\n<style>");
        html.append("\n#tab_").append("workflow").append("_tabgroup__body{padding: 1px 0 0 0;}");
        html.append("\n#tab_").append("workflow").append("_tabgroup__body div{overflow-y: hidden !important;overflow-x: hidden !important;}");
        html.append("\n.workflow_longtext { width:400px; }");
        html.append("\n.maintform_fieldtitle{width:100px;)");
        html.append("\n</style>");
        return html;
    }

    private StringBuffer getEndScript(PropertyList props, String sdcid, String keyid1, String keyid2, String keyid3, boolean viewOnly, boolean locked, String rsetid, Mode mode, boolean changeMade) {
        StringBuffer html = new StringBuffer();
        html.append("<script type=\"text/javascript\">");
        html.append(JS_CLASS).append(".properties=sapphire.util.propertyList.create(").append(props.toJSONString(false)).append(");");
        html.append(JS_CLASS).append(".keyid1='").append(keyid1).append("';");
        html.append(JS_CLASS).append(".keyid2='").append(keyid2).append("';");
        html.append(JS_CLASS).append(".keyid3='").append(keyid3).append("';");
        html.append(JS_CLASS).append(".mode='").append(mode.toString().toLowerCase()).append("';");
        html.append(JS_CLASS).append(".checkTab();");
        if (changeMade) {
            html.append("sapphire.events.registerLoadListener(function(){");
            html.append(JS_CLASS).append(".isSaved(false);");
            html.append("},false,500);");
        }
        html.append("__rsetlist='").append(rsetid).append("';");
        html.append(JS_CLASS).append(".locked=").append(locked).append(";");
        html.append(JS_CLASS).append(".viewonly=").append(viewOnly).append(";");
        html.append(JS_CLASS).append(".resVars=").append(new JSONArray(Arrays.asList(RESERVED_VARIANTS)).toString()).append(";");
        html.append("</script>");
        return html;
    }

    private StringBuffer getToolBar() {
        PropertyListCollection rec;
        PropertyList user;
        PropertyList common;
        PropertyList btn;
        StringBuffer html = new StringBuffer();
        TranslationProcessor tp = this.getTranslationProcessor();
        html.append("<table cellpadding=0 cellspacing=0 id=\"toolbarcontainer\" class=\"pagebuttonsection\"><tr>");
        html.append("<td class=\"ribbon_group_file\">");
        PropertyList toolbar = new PropertyList();
        toolbar.setProperty("rendermode", "Button");
        toolbar.setProperty("pagetitle", "");
        toolbar.setProperty("showtitle", "N");
        PropertyListCollection buttons = new PropertyListCollection();
        if (!this.viewonly) {
            btn = new PropertyList();
            btn.setProperty("id", "btSave");
            btn.setProperty("buttontype", "User");
            common = new PropertyList();
            common.setProperty("text", tp.translate("Save"));
            common.setProperty("image", "WEB-CORE/images/png/Save.png");
            common.setProperty("imagelarge", "WEB-CORE/images/png32/Save.png");
            common.setProperty("group", "File");
            common.setProperty("ribbonstyle", "Large");
            toolbar.setProperty("buttons", "Ribbon");
            btn.setProperty("commonprops", common);
            user = new PropertyList();
            user.setProperty("action", "workflowMaint.buttons.save()");
            user.setProperty("releaselock", "N");
            btn.setProperty("userbuttonprops", user);
            buttons.add(btn);
        }
        btn = new PropertyList();
        btn.setProperty("id", "btReturn");
        btn.setProperty("buttontype", "User");
        common = new PropertyList();
        common.setProperty("text", tp.translate("Return"));
        common.setProperty("image", "rc?command=image&file=WEB-CORE/images/png/ReturntoList.png");
        common.setProperty("imagelarge", "rc?command=image&file=WEB-CORE/images/png/ReturntoList.png");
        common.setProperty("group", "File");
        common.setProperty("ribbonstyle", "Large");
        toolbar.setProperty("buttons", "Ribbon");
        btn.setProperty("commonprops", common);
        user = new PropertyList();
        user.setProperty("action", "top.modernLayout.navigation.goBack()");
        user.setProperty("releaselock", "N");
        btn.setProperty("userbuttonprops", user);
        buttons.add(btn);
        btn = new PropertyList();
        btn.setProperty("id", "btAdd");
        btn.setProperty("buttontype", "User");
        common = new PropertyList();
        common.setProperty("text", tp.translate("Add Another"));
        common.setProperty("image", "WEB-CORE/images/png/Add.png");
        common.setProperty("imagelarge", "WEB-CORE/images/png32/Add.png");
        common.setProperty("group", "File");
        common.setProperty("ribbonstyle", "Small");
        toolbar.setProperty("buttons", "Ribbon");
        btn.setProperty("commonprops", common);
        user = new PropertyList();
        user.setProperty("action", "workflowMaint.buttons.add()");
        user.setProperty("releaselock", "N");
        btn.setProperty("userbuttonprops", user);
        buttons.add(btn);
        if (this.isChangeControlled) {
            btn = new PropertyList();
            btn.setProperty("id", "btCheckOut");
            btn.setProperty("buttontype", "User");
            common = new PropertyList();
            common.setProperty("text", tp.translate("Check Out"));
            common.setProperty("image", "rc?command=image&image=FlatBlackCabinetOut");
            common.setProperty("imagelarge", "rc?command=image&image=FlatBlackCabinetOut");
            common.setProperty("group", tp.translate("Change Control"));
            common.setProperty("ribbonstyle", "Small");
            toolbar.setProperty("buttons", "Ribbon");
            btn.setProperty("commonprops", common);
            user = new PropertyList();
            user.setProperty("action", "workflowMaint.buttons.checkOut()");
            user.setProperty("releaselock", "N");
            btn.setProperty("userbuttonprops", user);
            buttons.add(btn);
            btn = new PropertyList();
            btn.setProperty("id", "btCheckIn");
            btn.setProperty("buttontype", "User");
            common = new PropertyList();
            common.setProperty("text", tp.translate("Check In"));
            common.setProperty("image", "rc?command=image&image=FlatBlackCabinetIn");
            common.setProperty("imagelarge", "rc?command=image&image=FlatBlackCabinetIn");
            common.setProperty("group", tp.translate("Change Control"));
            common.setProperty("ribbonstyle", "Small");
            toolbar.setProperty("buttons", "Ribbon");
            btn.setProperty("commonprops", common);
            user = new PropertyList();
            user.setProperty("action", "workflowMaint.buttons.checkIn()");
            user.setProperty("releaselock", "N");
            btn.setProperty("userbuttonprops", user);
            buttons.add(btn);
            btn = new PropertyList();
            btn.setProperty("id", "btUndoCheckOut");
            btn.setProperty("buttontype", "User");
            common = new PropertyList();
            common.setProperty("text", tp.translate("Undo Check Out"));
            common.setProperty("image", "rc?command=image&image=FlatBlackUndo");
            common.setProperty("imagelarge", "rc?command=image&image=FlatBlackUndo");
            common.setProperty("group", tp.translate("Change Control"));
            common.setProperty("ribbonstyle", "Small");
            toolbar.setProperty("buttons", "Ribbon");
            btn.setProperty("commonprops", common);
            user = new PropertyList();
            user.setProperty("action", "workflowMaint.buttons.undoCheckOut()");
            user.setProperty("releaselock", "N");
            btn.setProperty("userbuttonprops", user);
            buttons.add(btn);
            btn = new PropertyList();
            btn.setProperty("id", "btViewChangeHistory");
            btn.setProperty("buttontype", "User");
            common = new PropertyList();
            common.setProperty("text", tp.translate("View Change History"));
            common.setProperty("image", "rc?command=image&image=FlatBlackMagnifyBrowse");
            common.setProperty("imagelarge", "rc?command=image&image=FlatBlackMagnifyBrowse");
            common.setProperty("group", tp.translate("Change Control"));
            common.setProperty("ribbonstyle", "Small");
            toolbar.setProperty("buttons", "Ribbon");
            btn.setProperty("commonprops", common);
            user = new PropertyList();
            user.setProperty("action", "workflowMaint.buttons.viewChangeHistory()");
            user.setProperty("releaselock", "N");
            btn.setProperty("userbuttonprops", user);
            buttons.add(btn);
        }
        if ((rec = (PropertyListCollection)this.pageContext.getAttribute("recentitems", 2)) != null && rec.size() > 0) {
            btn = new PropertyList();
            btn.setProperty("id", "btReturn");
            btn.setProperty("buttontype", "User");
            common = new PropertyList();
            common.setProperty("text", tp.translate("Return"));
            common.setProperty("image", "WEB-CORE/images/png/ReturntoList.png");
            common.setProperty("imagelarge", "WEB-CORE/images/png32/ReturntoList.png");
            common.setProperty("group", "File");
            common.setProperty("ribbonstyle", "Small");
            toolbar.setProperty("buttons", "Ribbon");
            btn.setProperty("commonprops", common);
            user = new PropertyList();
            user.setProperty("action", "workflowMaint.buttons.returnTo(" + (rec.size() > 1 ? rec.getPropertyList(1).getProperty("webpagelogid") : Integer.valueOf(0)) + " );");
            user.setProperty("releaselock", "N");
            btn.setProperty("userbuttonprops", user);
            buttons.add(btn);
        }
        toolbar.setProperty("buttons", buttons);
        AdvancedToolbar advancedToolbar = new AdvancedToolbar();
        advancedToolbar.setPageContext(this.pageContext);
        advancedToolbar.setElementid("File_Toolbar");
        advancedToolbar.setElementProperties(toolbar);
        html.append(advancedToolbar.getHtml());
        html.append("</td><td class=\"ribbon_group_actions\">");
        toolbar = new PropertyList();
        toolbar.setProperty("rendermode", "Button");
        toolbar.setProperty("pagetitle", "");
        toolbar.setProperty("showtitle", "N");
        buttons = new PropertyListCollection();
        btn = new PropertyList();
        btn.setProperty("buttontype", "User");
        common = new PropertyList();
        btn.setProperty("id", "btMaxMin");
        common.setProperty("text", tp.translate("Maximize"));
        common.setProperty("image", "WEB-CORE/elements/richtext/images/gif/ToolbarMaxMin.gif");
        common.setProperty("group", "Actions");
        common.setProperty("ribbonstyle", "Large");
        toolbar.setProperty("buttons", "Ribbon");
        btn.setProperty("commonprops", common);
        user = new PropertyList();
        user.setProperty("action", "workflowMaint.buttons.maxMinPainter()");
        user.setProperty("releaselock", "N");
        btn.setProperty("userbuttonprops", user);
        btn = new PropertyList();
        btn.setProperty("buttontype", "User");
        common = new PropertyList();
        btn.setProperty("id", "btMaxMin");
        common.setProperty("text", tp.translate("Maximize"));
        common.setProperty("image", "WEB-CORE/elements/richtext/images/gif/ToolbarMaxMin.gif");
        common.setProperty("group", "Actions");
        common.setProperty("ribbonstyle", "Large");
        toolbar.setProperty("buttons", "Ribbon");
        btn.setProperty("commonprops", common);
        user = new PropertyList();
        user.setProperty("action", "workflowMaint.buttons.maxMinPainter()");
        user.setProperty("releaselock", "N");
        btn.setProperty("userbuttonprops", user);
        buttons.add(btn);
        if (!this.viewonly) {
            btn = new PropertyList();
            btn.setProperty("buttontype", "User");
            common = new PropertyList();
            btn.setProperty("id", "btDelete");
            common.setProperty("text", tp.translate("Delete"));
            common.setProperty("image", "WEB-CORE/images/png32/Delete.png");
            common.setProperty("group", "Actions");
            common.setProperty("ribbonstyle", "Large");
            toolbar.setProperty("buttons", "Ribbon");
            btn.setProperty("commonprops", common);
            user = new PropertyList();
            user.setProperty("action", "workflow_frame.workflow.buttons.deleteItem()");
            user.setProperty("releaselock", "N");
            btn.setProperty("userbuttonprops", user);
            buttons.add(btn);
        }
        btn = new PropertyList();
        btn.setProperty("buttontype", "User");
        common = new PropertyList();
        btn.setProperty("id", "btEdit");
        common.setProperty("text", tp.translate("Edit"));
        common.setProperty("image", "WEB-CORE/images/png32/Edit.png");
        common.setProperty("group", "Actions");
        common.setProperty("ribbonstyle", "Large");
        toolbar.setProperty("buttons", "Ribbon");
        btn.setProperty("commonprops", common);
        user = new PropertyList();
        user.setProperty("action", "workflow_frame.workflow.buttons.editTask()");
        user.setProperty("releaselock", "N");
        btn.setProperty("userbuttonprops", user);
        buttons.add(btn);
        btn = new PropertyList();
        btn.setProperty("buttontype", "User");
        common = new PropertyList();
        btn.setProperty("id", "btElements");
        common.setProperty("text", tp.translate("Hide Elements"));
        common.setProperty("image", "WEB-CORE/images/png32/View.png");
        common.setProperty("group", "Actions");
        common.setProperty("ribbonstyle", "Large");
        toolbar.setProperty("buttons", "Ribbon");
        btn.setProperty("commonprops", common);
        user = new PropertyList();
        user.setProperty("action", "workflow_frame.workflow.buttons.toggleElements()");
        user.setProperty("releaselock", "N");
        btn.setProperty("userbuttonprops", user);
        buttons.add(btn);
        btn = new PropertyList();
        btn.setProperty("buttontype", "User");
        common = new PropertyList();
        btn.setProperty("id", "btElements");
        common.setProperty("text", tp.translate("Print"));
        common.setProperty("image", "rc?command=image&image=Printer2&size=32");
        common.setProperty("group", "Actions");
        common.setProperty("ribbonstyle", "Large");
        toolbar.setProperty("buttons", "Ribbon");
        btn.setProperty("commonprops", common);
        user = new PropertyList();
        user.setProperty("action", "workflow_frame.workflow.buttons.print()");
        user.setProperty("releaselock", "N");
        btn.setProperty("userbuttonprops", user);
        buttons.add(btn);
        toolbar.setProperty("buttons", buttons);
        advancedToolbar = new AdvancedToolbar();
        advancedToolbar.setPageContext(this.pageContext);
        advancedToolbar.setElementid("Actions_Toolbar");
        advancedToolbar.setElementProperties(toolbar);
        html.append(advancedToolbar.getHtml());
        html.append("</td><td class=\"ribbon_group_categories\">");
        toolbar = new PropertyList();
        toolbar.setProperty("rendermode", "Button");
        toolbar.setProperty("pagetitle", "");
        toolbar.setProperty("showtitle", "N");
        buttons = new PropertyListCollection();
        btn = new PropertyList();
        btn.setProperty("buttontype", "User");
        common = new PropertyList();
        btn.setProperty("id", "btAddCat");
        common.setProperty("text", tp.translate("Add Category"));
        common.setProperty("image", "WEB-CORE/images/png32/Add.png");
        common.setProperty("group", "Categories");
        common.setProperty("ribbonstyle", "Large");
        toolbar.setProperty("buttons", "Ribbon");
        btn.setProperty("commonprops", common);
        user = new PropertyList();
        user.setProperty("action", "workflowMaint.buttons.addCategory()");
        user.setProperty("releaselock", "N");
        btn.setProperty("userbuttonprops", user);
        buttons.add(btn);
        toolbar.setProperty("buttons", buttons);
        advancedToolbar = new AdvancedToolbar();
        advancedToolbar.setPageContext(this.pageContext);
        advancedToolbar.setElementid("Categories_Toolbar");
        advancedToolbar.setElementProperties(toolbar);
        html.append(advancedToolbar.getHtml());
        html.append("</td><td class=\"ribbon_group_bpmn\">");
        toolbar = new PropertyList();
        toolbar.setProperty("rendermode", "Button");
        toolbar.setProperty("pagetitle", "");
        toolbar.setProperty("showtitle", "N");
        buttons = new PropertyListCollection();
        btn = new PropertyList();
        btn.setProperty("buttontype", "User");
        common = new PropertyList();
        btn.setProperty("id", "btImportBPMN");
        common.setProperty("text", tp.translate("Import"));
        common.setProperty("image", "rc?command=image&image=Import");
        common.setProperty("group", "BPMN");
        common.setProperty("ribbonstyle", "Small");
        toolbar.setProperty("buttons", "Ribbon");
        btn.setProperty("commonprops", common);
        user = new PropertyList();
        user.setProperty("action", "workflowMaint.buttons.importBPMN()");
        user.setProperty("releaselock", "N");
        btn.setProperty("userbuttonprops", user);
        buttons.add(btn);
        btn = new PropertyList();
        btn.setProperty("buttontype", "User");
        common = new PropertyList();
        btn.setProperty("id", "btExportBPMN1");
        common.setProperty("text", tp.translate("Export BPMN"));
        common.setProperty("image", "rc?command=image&image=Export2");
        common.setProperty("group", "BPMN");
        common.setProperty("ribbonstyle", "Small");
        toolbar.setProperty("buttons", "Ribbon");
        btn.setProperty("commonprops", common);
        user = new PropertyList();
        user.setProperty("action", "workflowMaint.buttons.exportBPMN(false)");
        user.setProperty("releaselock", "N");
        btn.setProperty("userbuttonprops", user);
        buttons.add(btn);
        btn = new PropertyList();
        btn.setProperty("buttontype", "User");
        common = new PropertyList();
        btn.setProperty("id", "btExportBPMN2");
        common.setProperty("text", tp.translate("Export XPDL"));
        common.setProperty("image", "rc?command=image&image=Export");
        common.setProperty("group", "BPMN");
        common.setProperty("ribbonstyle", "Small");
        toolbar.setProperty("buttons", "Ribbon");
        btn.setProperty("commonprops", common);
        user = new PropertyList();
        user.setProperty("action", "workflowMaint.buttons.exportBPMN(true)");
        user.setProperty("releaselock", "N");
        btn.setProperty("userbuttonprops", user);
        buttons.add(btn);
        toolbar.setProperty("buttons", buttons);
        advancedToolbar = new AdvancedToolbar();
        advancedToolbar.setPageContext(this.pageContext);
        advancedToolbar.setElementid("BPMN_Toolbar");
        advancedToolbar.setElementProperties(toolbar);
        html.append(advancedToolbar.getHtml());
        html.append("</td>");
        if (this.devMode || this.debug) {
            html.append("<td class=\"ribbon_group_debug\">");
            toolbar = new PropertyList();
            toolbar.setProperty("rendermode", "Button");
            toolbar.setProperty("pagetitle", "");
            toolbar.setProperty("showtitle", "N");
            buttons = new PropertyListCollection();
            btn = new PropertyList();
            btn.setProperty("buttontype", "User");
            common = new PropertyList();
            btn.setProperty("id", "btDebugShowXML");
            common.setProperty("text", tp.translate("Edit XML"));
            common.setProperty("image", "rc?command=image&image=Code");
            common.setProperty("group", "Debug");
            common.setProperty("ribbonstyle", "Large");
            toolbar.setProperty("buttons", "Ribbon");
            btn.setProperty("commonprops", common);
            user = new PropertyList();
            user.setProperty("action", "workflowMaint.buttons.debugProperties(false)");
            user.setProperty("releaselock", "N");
            btn.setProperty("userbuttonprops", user);
            buttons.add(btn);
            btn = new PropertyList();
            btn.setProperty("buttontype", "User");
            common = new PropertyList();
            btn.setProperty("id", "btDebugShowJSON");
            common.setProperty("text", tp.translate("Edit JSON"));
            common.setProperty("image", "rc?command=image&image=CodeJavascript");
            common.setProperty("group", "Debug");
            common.setProperty("ribbonstyle", "Large");
            toolbar.setProperty("buttons", "Ribbon");
            btn.setProperty("commonprops", common);
            user = new PropertyList();
            user.setProperty("action", "workflowMaint.buttons.debugProperties(true)");
            user.setProperty("releaselock", "N");
            btn.setProperty("userbuttonprops", user);
            buttons.add(btn);
            toolbar.setProperty("buttons", buttons);
            advancedToolbar = new AdvancedToolbar();
            advancedToolbar.setPageContext(this.pageContext);
            advancedToolbar.setElementid("Debug_Toolbar");
            advancedToolbar.setElementProperties(toolbar);
            html.append(advancedToolbar.getHtml());
            html.append("</td>");
        }
        html.append("</tr></table>");
        return html;
    }

    private EditorStyleField getVariableField(String field, String mode, String variableid, int realrow, String value, String change, String datatype, String reatedvariableid, String lookupcallback, boolean viewonly) {
        return TaskDefVariables.getVariableField(field, mode, variableid, realrow, value, datatype, reatedvariableid, lookupcallback, viewonly, change, this.getConnectionId(), this.pageContext, this.sdiInfo, this.getTranslationProcessor());
    }

    private StringBuffer getVariablesTab() {
        StringBuffer html = new StringBuffer();
        html.append("<div style=\"padding: 5px 5px 5px 5px;width: auto;").append(this.browser.isIE() ? "" : "position: absolute;").append("right: 0;left: 0;top:0;bottom:0;height:auto;overflow: auto;overflow-y:auto !important;\" id=\"workflow_variablescontent\">");
        html.append(WorkflowDefMaint.getVariablesTab(this.workflowprops, this.viewonly, this.getConnectionProcessor().getSapphireConnection(), this.pageContext, this.sdiInfo, this.getSDIProcessor(), this.getTranslationProcessor(), this.logger));
        html.append("</div>");
        return html;
    }

    protected static StringBuffer getVariablesTab(PropertyList workflowprops, boolean viewonly, SapphireConnection sapphireConnection, PageContext pageContext, SDITagInfo sdiInfo, SDIProcessor sdi, TranslationProcessor tp, Logger logger) {
        StringBuffer html = new StringBuffer();
        StringBuffer tabhtml = new StringBuffer();
        StringBuffer tab1 = new StringBuffer();
        PropertyListCollection variables = workflowprops.getCollection("variables");
        html.append("<form name=\"workflow_variablesform\" action=\"#\">");
        if (variables == null) {
            variables = new PropertyListCollection();
            workflowprops.setProperty("variables", variables);
        }
        StringBuffer tasklist = new StringBuffer();
        PropertyListCollection tasks = workflowprops.getCollection("tasks");
        if (tasks != null) {
            for (int i = 0; i < tasks.size(); ++i) {
                PropertyList task = tasks.getPropertyList(i);
                if (tasklist.length() > 0) {
                    tasklist.append(";");
                }
                tasklist.append(task.getProperty("taskdefitemid")).append("=").append(task.getProperty("shorttitle", task.getProperty("longtitle", task.getProperty("taskdefitemid"))));
            }
        }
        String relHelp = tp.translate("If the editor style for this variable returns multiple values (e.g. multiple keys) then enter a semicolon delimited list of variables which you want the return values to be mapped into.");
        boolean foundVar = false;
        for (int i = 0; i < variables.size(); ++i) {
            PropertyList var = variables.getPropertyList(i);
            String varId = var.getProperty("variableid", "");
            String relatedvariableid = var.getProperty("relatedvariableid", "");
            boolean setup = var.getProperty("setup", "N").equalsIgnoreCase("Y");
            String defaultvalue = var.getProperty("defaultvalue", "");
            String dataType = var.getProperty("type", "string");
            String varidrep = StringUtil.replaceAll(varId, " ", "_");
            String change = "workflowMaint.variableFieldChange(event,this,'" + varId + "'," + i + ")";
            String editorstyle = var.getProperty("editorstyleid", "");
            if (setup) {
                String prompt = var.getProperty("prompt", "");
                String help = var.getProperty("help", "");
                tabhtml.append("<tr>");
                if (!viewonly) {
                    tabhtml.append("<td class=\"gridmaint_field\">");
                    tabhtml.append("<input type=\"checkbox\" name=\"").append("variable").append("_selector").append("").append("\" id=\"__").append("variable").append(i).append("_").append(varidrep).append("\">");
                    tabhtml.append("</td>");
                }
                tabhtml.append("<td id=\"").append("variable").append(i).append("_").append(varidrep).append("_titlecell\" class=\"gridmaint_field\" style=\"\">");
                tabhtml.append(varId).append("");
                tabhtml.append("</td>");
                tabhtml.append("<td class=\"gridmaint_field\">");
                tabhtml.append(dataType);
                tabhtml.append("</td>");
                tabhtml.append("<td class=\"gridmaint_field\" nowrap>");
                EditorStyleField esf = null;
                if (editorstyle.length() > 0) {
                    esf = new EditorStyleField(pageContext, null, sapphireConnection.getConnectionId());
                    try {
                        esf.setEditorStyleId(editorstyle);
                        esf.setColumnDefinition(varidrep, EditorStyleField.getEditorStyleDataType(dataType), 100, false, false);
                    }
                    catch (Exception e) {
                        logger.warn("Invalid editor style");
                        esf = new EditorStyleField(pageContext, null, sapphireConnection.getConnectionId());
                        String dt = EditorStyleField.getEditorStyleDataType(dataType);
                        esf.setDefaultEditorStyleProperties(dt, "", "");
                        esf.setColumnDefinition(varidrep, dt, 100, false, false);
                    }
                } else {
                    esf = new EditorStyleField(pageContext, null, sapphireConnection.getConnectionId());
                    String dt = EditorStyleField.getEditorStyleDataType(dataType);
                    esf.setDefaultEditorStyleProperties(dt, "", "");
                    esf.setColumnDefinition(varidrep, dt, 100, false, false);
                }
                if (esf != null) {
                    foundVar = true;
                    if (defaultvalue.length() > 0) {
                        esf.setFieldValue(defaultvalue);
                    }
                    if (var.getProperty("help", "").length() > 0) {
                        esf.setColumnProperty("tip", var.getProperty("help", ""));
                    }
                    esf.setChangeEvent(change);
                    esf.setFieldName("variable" + i + "_defaultvalue");
                    if (viewonly) {
                        esf.setColumnProperty("disable", "Y");
                    }
                    TaskDefVariables.setEditorStyleFieldMapping(esf, varId, relatedvariableid, "workflowMaint.variableLookupCallback");
                    tabhtml.append(esf.getHtml());
                }
                tabhtml.append("</td>");
                tabhtml.append("<td class=\"gridmaint_field\">").append(TaskDefVariables.getVariableField("setup", "checkbox", varidrep, i, setup ? "Y" : "N", "string", "", "", viewonly, change, sapphireConnection.getConnectionId(), pageContext, sdiInfo, tp).getHtml()).append("</td>");
                tabhtml.append("<td class=\"gridmaint_field\">").append(TaskDefVariables.getVariableField("prompt", "input", varidrep, i, prompt, "string", "", "", viewonly, change, sapphireConnection.getConnectionId(), pageContext, sdiInfo, tp).getHtml()).append("</td>");
                tabhtml.append("<td class=\"gridmaint_field\">").append(TaskDefVariables.getVariableField("modifiable", "checkbox", varidrep, i, var.getProperty("modifiable", "Y"), "string", "", "", viewonly, change, sapphireConnection.getConnectionId(), pageContext, sdiInfo, tp).getHtml()).append("</td>");
                tabhtml.append("<td class=\"gridmaint_field\">").append(TaskDefVariables.getVariableField("mandatory", "checkbox", varidrep, i, var.getProperty("mandatory", "N"), "string", "", "", viewonly, change, sapphireConnection.getConnectionId(), pageContext, sdiInfo, tp).getHtml()).append("</td>");
                tabhtml.append("<td class=\"gridmaint_field\">").append(TaskDefVariables.getVariableField("help", "input", varidrep, i, help, "string", "", "", viewonly, change, sapphireConnection.getConnectionId(), pageContext, sdiInfo, tp).getHtml()).append("</td>");
                tabhtml.append("<td class=\"gridmaint_field\" nowrap>").append(TaskDefVariables.getVariableField("editorstyleid", "lookup", varidrep, i, editorstyle, "string", "", "", viewonly, change, sapphireConnection.getConnectionId(), pageContext, sdiInfo, tp).getHtml()).append("</td>");
                tabhtml.append("<td class=\"gridmaint_field\" title=\"").append(relHelp).append("\">").append(TaskDefVariables.getVariableField("relatedvariableid", "input", varidrep, i, relatedvariableid, "string", "", "", viewonly, change, sapphireConnection.getConnectionId(), pageContext, sdiInfo, tp).getHtml()).append("</td>");
                tabhtml.append("</tr>");
                continue;
            }
            tabhtml.append("<tr>");
            if (!viewonly) {
                tabhtml.append("<td class=\"gridmaint_field\">");
                tabhtml.append("<input type=\"checkbox\" name=\"").append("variable").append("_selector").append("").append("\" id=\"__").append("variable").append(i).append("_").append(varidrep).append("\">");
                tabhtml.append("</td>");
            }
            tabhtml.append("<td id=\"").append("variable").append(i).append("_").append(varidrep).append("_titlecell\" class=\"gridmaint_field\" style=\"\">");
            tabhtml.append(varId).append("");
            tabhtml.append("</td>");
            tabhtml.append("<td class=\"gridmaint_field\">");
            tabhtml.append(dataType);
            tabhtml.append("</td>");
            tabhtml.append("<td class=\"gridmaint_field\" nowrap>");
            EditorStyleField esf = null;
            if (editorstyle.length() > 0) {
                esf = new EditorStyleField(pageContext, null, sapphireConnection.getConnectionId());
                try {
                    esf.setEditorStyleId(editorstyle);
                    esf.setColumnDefinition(varidrep, EditorStyleField.getEditorStyleDataType(dataType), 100, false, false);
                }
                catch (Exception e) {
                    logger.warn("Invalid editor style");
                }
            } else {
                esf = new EditorStyleField(pageContext, null, sapphireConnection.getConnectionId());
                String dt = EditorStyleField.getEditorStyleDataType(dataType);
                esf.setDefaultEditorStyleProperties(dt, "", "");
                esf.setColumnDefinition(varidrep, dt, 100, false, false);
            }
            if (esf != null) {
                foundVar = true;
                if (defaultvalue.length() > 0) {
                    esf.setFieldValue(defaultvalue);
                }
                if (var.getProperty("help", "").length() > 0) {
                    esf.setColumnProperty("tip", var.getProperty("help", ""));
                }
                esf.setChangeEvent(change);
                esf.setFieldName("variable" + i + "_defaultvalue");
                if (viewonly) {
                    esf.setColumnProperty("disable", "Y");
                }
                TaskDefVariables.setEditorStyleFieldMapping(esf, varId, relatedvariableid, "workflowMaint.variableLookupCallback");
                tabhtml.append(esf.getHtml());
            }
            tabhtml.append("</td>");
            tabhtml.append("<td class=\"gridmaint_field\">").append(TaskDefVariables.getVariableField("setup", "checkbox", varidrep, i, setup ? "Y" : "N", "string", "", "", viewonly, change, sapphireConnection.getConnectionId(), pageContext, sdiInfo, tp).getHtml()).append("</td>");
            tabhtml.append("<td class=\"gridmaint_field\">").append("").append("</td>");
            tabhtml.append("<td class=\"gridmaint_field\">").append("").append("</td>");
            tabhtml.append("<td class=\"gridmaint_field\">").append("").append("</td>");
            tabhtml.append("<td class=\"gridmaint_field\">").append("").append("</td>");
            tabhtml.append("<td class=\"gridmaint_field\" nowrap>").append(TaskDefVariables.getVariableField("editorstyleid", "lookup", varidrep, i, editorstyle, "string", "", "", viewonly, change, sapphireConnection.getConnectionId(), pageContext, sdiInfo, tp).getHtml()).append("</td>");
            tabhtml.append("<td class=\"gridmaint_field\" title=\"").append(relHelp).append("\">").append(TaskDefVariables.getVariableField("relatedvariableid", "input", varidrep, i, relatedvariableid, "string", "", "", viewonly, change, sapphireConnection.getConnectionId(), pageContext, sdiInfo, tp).getHtml()).append("</td>");
            tabhtml.append("</tr>");
            foundVar = true;
        }
        if (!viewonly) {
            Button btn = new Button(pageContext);
            btn.setId("btnAddWorkflowVar");
            btn.setAction("workflowMaint.buttons.addVar(document.getElementById('__variable_change'),null,false,workflowMaint.ui.refreshWorkflowProps)");
            btn.setTip("Add Workflow Variable");
            btn.setDisabled(viewonly);
            btn.setImg("WEB-CORE/images/png/Add.png");
            tab1.append(btn.getHtml());
            tab1.append("&nbsp;");
            btn = new Button(pageContext);
            btn.setId("btnDeleteWorkflowVar");
            btn.setAction("workflowMaint.buttons.deleteVar('')");
            btn.setTip("Delete Workflow Variable");
            btn.setDisabled(viewonly || !foundVar);
            btn.setImg("WEB-CORE/images/png/Delete.png");
            tab1.append(btn.getHtml());
        }
        if (foundVar) {
            tab1.append("<table border=\"0\" cellpadding=\"5\" cellspacing=\"0\" class=\"gridmaint_table\">");
            tab1.append("<thead>");
            tab1.append("<tr class=\"gridmaint_tablehead\">");
            if (!viewonly) {
                tab1.append("<th class=\"gridmaint_fieldtitle\">").append("&nbsp;").append("</th>");
            }
            tab1.append("<th class=\"gridmaint_fieldtitle\">").append(tp.translate("Variable")).append("</th>");
            tab1.append("<th class=\"gridmaint_fieldtitle\">").append(tp.translate("Type")).append("</th>");
            tab1.append("<th class=\"gridmaint_fieldtitle\" style=\"width:180px;\">").append(tp.translate("Default Value")).append("</th>");
            tab1.append("<th class=\"gridmaint_fieldtitle\">").append(tp.translate("Setup Variable")).append("</th>");
            tab1.append("<th class=\"gridmaint_fieldtitle\">").append(tp.translate("Prompt")).append("</th>");
            tab1.append("<th class=\"gridmaint_fieldtitle\">").append(tp.translate("Modifiable")).append("</th>");
            tab1.append("<th class=\"gridmaint_fieldtitle\">").append(tp.translate("Mandatory")).append("</th>");
            tab1.append("<th class=\"gridmaint_fieldtitle\">").append(tp.translate("Help Text")).append("</th>");
            tab1.append("<th class=\"gridmaint_fieldtitle\">").append(tp.translate("Editor Style")).append("</th>");
            tab1.append("<th class=\"gridmaint_fieldtitle\">").append(tp.translate("Related Variables")).append("</th>");
            tab1.append("</tr>");
            tab1.append("</thead>");
            tab1.append("<tbody>");
            tab1.append(tabhtml);
            tab1.append("</tbody>");
            tab1.append("</table>");
            tab1.append("<br>");
        } else {
            tab1.append("<p>" + tp.translate("No Workflow Variables"));
        }
        html.append("<div id=\"section_wv\" style=\"display:").append("block").append(";padding-bottom:15px;width: 100%;overflow: auto;\">");
        html.append(tab1);
        html.append("</div>");
        html.append("<input type=hidden name=\"__variable_change\" id=\"__variable_change\" onchange=\"workflowMaint.ui.refreshVariables()\">");
        html.append("</form>");
        return html;
    }

    private StringBuffer getWorkflowTab() {
        StringBuffer html = new StringBuffer();
        TranslationProcessor tp = this.getTranslationProcessor();
        if (this.workflowData != null && this.workflowData.getDataset("primary") != null) {
            PropertyListCollection columns;
            boolean fullViewOnly;
            DataView maint = new DataView(this.pageContext, "primary", this.workflowData.getDataset("primary"), "", this.getConnectionId());
            maint.setElementid("task_primary_dataview");
            maint.setSDCId(this.sdcid);
            maint.getSDIInfo().setSdcid(this.sdcid);
            PropertyList maintProps = new PropertyList();
            maintProps.setProperty(PROPERTY_SDCID, this.sdcid);
            maintProps.setProperty("style", "FormWithFieldGroups");
            maintProps.setProperty("formcols", "2");
            if (this.viewonly) {
                maintProps.setProperty(PROPERTY_VIEWONLY, "Y");
            }
            boolean bl = fullViewOnly = this.viewonly || this.maintElement != null && this.maintElement.getProperty(PROPERTY_VIEWONLY, "N").equalsIgnoreCase("Y");
            if (this.maintElement == null || this.maintElement.getCollection("columns") == null || this.maintElement.getCollection("columns").size() == 0) {
                PropertyListCollection events = new PropertyListCollection();
                PropertyList event = new PropertyList();
                event.setProperty("event", "onchange");
                event.setProperty("js", "workflowMaint.maintFieldChange(event,this)");
                events.add(event);
                columns = new PropertyListCollection();
                PropertyList column = new PropertyList();
                column = new PropertyList();
                column.setProperty("columnid", "workflowdefid");
                column.setProperty("title", "Workflow Id");
                column.setProperty("mode", !fullViewOnly ? "input" : "readonly");
                column.setProperty("groupid", "Workflow");
                column.setProperty("events", events);
                columns.add(column);
                column = new PropertyList();
                column.setProperty("columnid", "workflowdefversionid");
                column.setProperty("title", "Version");
                column.setProperty("mode", "readonly");
                column.setProperty("groupid", "Workflow");
                column.setProperty("events", events);
                columns.add(column);
                column = new PropertyList();
                column.setProperty("columnid", "workflowdefvariantid");
                column.setProperty("title", "Variant");
                column.setProperty("colspan", "2");
                column.setProperty("mode", this.mode == Mode.ADD && !fullViewOnly ? "input" : "readonly");
                column.setProperty("groupid", "Workflow");
                if (this.mode == Mode.ADD) {
                    column.setProperty("class", "mandatoryfield");
                }
                column.setProperty("events", events);
                columns.add(column);
                column = new PropertyList();
                column.setProperty("columnid", "workflowdefdesc");
                column.setProperty("title", "Description");
                column.setProperty("colspan", "2");
                column.setProperty("mode", !fullViewOnly ? "input" : "readonly");
                column.setProperty("class", "workflow_longtext");
                column.setProperty("groupid", "Workflow");
                column.setProperty("events", events);
                columns.add(column);
                column = new PropertyList();
                column.setProperty("columnid", "exectypeflag");
                column.setProperty("title", "Execution Type");
                column.setProperty("mode", !fullViewOnly ? "dropdownlist" : "readonly");
                column.setProperty("displayvalue", "S=" + tp.translate("Single Execution") + ";N=" + tp.translate("Named Executions") + ";A=" + tp.translate("Auto Executions"));
                column.setProperty("groupid", "Workflow");
                column.setProperty("events", events);
                columns.add(column);
                column = new PropertyList();
                column.setProperty("columnid", "connector");
                column.setProperty("title", "Line Style");
                column.setProperty("mode", !fullViewOnly ? "dropdownlist" : "readonly");
                column.setProperty("displayvalue", "FLOWCHART=" + tp.translate("Flowchart") + ";BEZIER=" + tp.translate("Bezier") + ";CURVED=" + tp.translate("Curved") + ";STRAIGHT=" + tp.translate("Straight"));
                column.setProperty("groupid", "Workflow");
                column.setProperty("events", events);
                columns.add(column);
            } else {
                columns = this.maintElement.getCollection("columns");
                for (int i = 0; i < columns.size(); ++i) {
                    PropertyList event;
                    PropertyListCollection events;
                    PropertyList column = columns.getPropertyList(i);
                    if (fullViewOnly || column.getProperty("columnid").equalsIgnoreCase("workflowvariantid") && this.mode != Mode.ADD || column.getProperty("columnid").equalsIgnoreCase("workflowdefversionid")) {
                        column.setProperty("mode", "readonly");
                    }
                    if ((events = column.getCollection("events")) == null) {
                        events = new PropertyListCollection();
                        column.setProperty("events", events);
                    }
                    if ((event = events.find("event", "onchange")) != null) continue;
                    event = new PropertyList();
                    event.setProperty("event", "onchange");
                    event.setProperty("js", "workflowMaint.maintFieldChange(event,this)");
                    events.add(event);
                }
            }
            maintProps.setProperty("columns", columns);
            maint.setElementProperties(maintProps);
            html.append("<div style=\"padding: 5px 5px 5px 5px;\">");
            html.append(maint.getHtml());
            html.append("</div>");
        } else {
            html.append("<font style=\"color:red;\">" + tp.translate("Could not load data.") + "</font>");
        }
        return html;
    }

    private StringBuffer getDebugTab() {
        StringBuffer html = new StringBuffer();
        html.append("Readonly View Of Stored XML (use debug buttons for modifying XML):");
        html.append("<textarea id=\"workflow_propertyxml\" name=\"workflow_propertyxml\" readonly style=\"height:100%;width:100%;\">").append(this.workflowData != null && this.workflowData.getDataset("primary") != null && this.workflowData.getDataset("primary").getRowCount() > 0 ? this.workflowData.getDataset("primary").getClob(0, DATACOL, "") : "").append("</textarea>");
        return html;
    }

    public StringBuffer getCategoryTab() {
        StringBuffer html = new StringBuffer();
        html.append("<div id=\"workflow_catcontent\" style=\"margin: 5px 5px 5px 5px;padding: 5px 5px 5px 5px;width:400px;border:solid 1px lightSteelBlue;\">");
        PropertyList pl = new PropertyList();
        pl.setProperty("checkboxcols", "6");
        pl.setProperty("readonly", this.viewonly ? "Y" : "N");
        pl.setProperty("allowadd", "N");
        pl.setProperty("customadd", "workflowMaint.category.add");
        pl.setProperty("customclick", "workflowMaint.category.click");
        pl.setProperty("customtablestyle", "border:none;");
        MaintDetail md = new MaintDetail(this.pageContext, this.sdiInfo, this.getConnectionId());
        md.setElementType("category");
        md.setElementProperties(pl);
        html.append(md.getHtml());
        html.append("</div>");
        return html;
    }

    private StringBuffer getTasksTab() {
        StringBuffer html = new StringBuffer();
        html.append("<div id=\"workflow_frame_content\" style=\"background-color:#C3DAF9;padding:0;margin:0;").append(this.browser.isIE() ? "" : "display:inline;").append("\">");
        html.append("<iframe name=\"workflow_frame\" id=\"workflow_frame\" src=\"").append(this.browser.getBlankSrc()).append("\" scrolling=\"no\" style=\"width:100%;height:100%;\" frameborder=0></iframe>");
        html.append("</div>");
        if (this.browser.isIE() && this.requestContext.getProperty("html5").equalsIgnoreCase("Y")) {
            html.append("<script>");
            html.append("if (__sizeAdjust){__sizeAdjust.addElement(workflow_frame_content,'(document.body.clientHeight - workflow_frame_content.getBoundingClientRect().top - 5)');}");
            html.append("</script>");
        }
        html.append("<form name=\"workflow_form\" id=\"workflow_form\" action=\"rc?command=file&file=WEB-CORE/elements/workflow/workflowdefpainter.jsp\" method=POST target=\"workflow_frame\" style=\"display:none;\">");
        html.append("<textarea name=\"").append("properties").append("\" id=\"").append("properties").append("\">").append("").append("</textarea>");
        html.append("<input type=\"hidden\" name=\"").append(PROPERTY_SDCID).append("\" id=\"").append(PROPERTY_SDCID).append("\" value=\"").append(this.sdcid).append("\">");
        html.append("<input type=\"hidden\" name=\"").append(PROPERTY_KEYID1).append("\" id=\"").append(PROPERTY_KEYID1).append("\" value=\"").append(this.keyid1).append("\">");
        html.append("<input type=\"hidden\" name=\"").append(PROPERTY_KEYID2).append("\" id=\"").append(PROPERTY_KEYID2).append("\" value=\"").append(this.keyid2).append("\">");
        html.append("<input type=\"hidden\" name=\"").append(PROPERTY_KEYID3).append("\" id=\"").append(PROPERTY_KEYID3).append("\" value=\"").append(this.keyid3).append("\">");
        html.append("<input type=\"hidden\" name=\"").append("embedded").append("\" id=\"").append("embedded").append("\" value=\"").append("Y").append("\">");
        html.append("<input type=\"hidden\" name=\"").append("showqueues").append("\" id=\"").append("showqueues").append("\" value=\"").append("Y").append("\">");
        html.append("<input type=\"hidden\" name=\"").append(PROPERTY_VIEWONLY).append("\" id=\"").append(PROPERTY_VIEWONLY).append("\" value=\"").append(this.viewonly ? "Y" : "N").append("\">");
        html.append("<input type=\"hidden\" name=\"").append("propertycontainer").append("\" id=\"").append("propertycontainer").append("\" value=\"").append("sapphire.page.getTop().").append(JS_CLASS).append(".properties").append("\">");
        html.append("<input type=\"hidden\" name=\"").append("appearance").append("\" id=\"").append("appearance").append("\" value=\"").append("").append("\">");
        html.append("<input type=\"hidden\" name=\"").append("color").append("\" id=\"").append("color").append("\" value=\"").append("").append("\">");
        html.append("<input type=\"hidden\" name=\"").append("connector").append("\" id=\"").append("connector").append("\" value=\"").append("").append("\">");
        html.append("<input type=\"hidden\" name=\"").append("renderhtml5").append("\" id=\"").append("renderhtml5").append("\" value=\"").append("N").append("\">");
        boolean html5 = this.requestContext != null && (this.requestContext.getPropertyList().getProperty("html5", "N").equalsIgnoreCase("Y") || this.requestContext.getPropertyList().getProperty("html5", "N").equalsIgnoreCase("true"));
        html.append("<input type=\"hidden\" name=\"").append("inherithtml5").append("\" id=\"").append("inherithtml5").append("\" value=\"").append(html5 ? "Y" : "N").append("\">");
        html.append("</form>");
        return html;
    }

    private Tab getTab(Tabs currentTab) {
        Tab tab = new Tab();
        tab.setPageContext(this.pageContext);
        StringBuffer html = new StringBuffer();
        switch (currentTab) {
            case WORKFLOW: {
                html.append(this.getWorkflowTab());
                break;
            }
            case TASKS: {
                html.append(this.getTasksTab());
                break;
            }
            case VARIABLES: {
                html.append(this.getVariablesTab());
                break;
            }
            case CATEGORIES: {
                html.append(this.getCategoryTab());
                break;
            }
            case DEBUG: {
                html.append(this.getDebugTab());
            }
        }
        tab.setContent(html.toString());
        tab.setId("workflow_tab_" + currentTab.toString().toLowerCase());
        tab.setText(currentTab.getTitle());
        tab.setExpandable("false");
        tab.setExpanded("true");
        tab.setTip(currentTab.getTitle());
        tab.setAction("workflowMaint.changeTab('" + currentTab.toString().toLowerCase() + "')");
        tab.setBodyheight("100%");
        return tab;
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        CMTPolicy cmtPolicy = CMTPolicy.getPolicy(this.getConnectionid(), this.sdcid);
        this.isChangeControlled = "Y".equals(cmtPolicy.getChangeControlledFlag());
        if (this.workflowprops != null) {
            this.pageTitle = this.getTranslationProcessor().translate("Edit Workflow") + " " + this.keyid1 + " (" + this.keyid2 + " " + this.keyid3 + ")";
            if (this.mode == Mode.EDIT) {
                // empty if block
            }
            html.append(this.getScriptAndStyle());
            html.append("<div id=\"workflow_root_container\" style=\"min-width:800px;height:100%;width:100%;overflow:").append("hidden").append(";\">");
            boolean modernLayout = this.requestContext.getProperty("modernlayout").equalsIgnoreCase("Y");
            modernLayout = true;
            String height = "44px";
            html.append("<div id=\"workflow_table\" style=\"border-bottom:1px solid #000000;height:").append(height).append(";").append(modernLayout ? "background-color:#FFFFFF;" : "").append("width:100%;overflow-y:").append("hidden").append(";\">");
            html.append(this.getToolBar());
            html.append("</div>");
            html.append("<div id=\"workflow_tab_container\" style=\"padding: 5px 5px 5px 5px;height:auto;width:auto;position:absolute;top:").append(height).append(";left:0;bottom:0;right:0;\">");
            TabGroup tabGroup = new TabGroup();
            tabGroup.setPageContext(this.pageContext);
            tabGroup.setMultiTab(true);
            tabGroup.setId("workflow_tabgroup");
            tabGroup.setBodyheight("100%");
            tabGroup.setAppearance("modern");
            tabGroup.setContext("WorkflowDef_" + this.keyid1 + "_" + this.keyid2 + "_" + this.keyid3);
            for (Tabs tab : Tabs.values()) {
                if (tab == Tabs.DEBUG && !this.devMode && !this.debug) continue;
                tabGroup.setTab(this.getTab(tab));
            }
            tabGroup.setUseChangeTab(true);
            html.append(tabGroup.getHtml());
            html.append("</div>");
            html.append("</div>");
            html.append("<iframe name=\"bpmnframe\" id=\"bpmnframe\" src=\"").append(this.browser.getBlankSrc()).append("\" style=\"display:none;\"></iframe>");
            html.append("<form name=\"bpmnform\" id=\"bpmnform\" target=\"bpmnframe\" style=\"display:none;\" method=\"POST\" action=\"rc?command=operation\">");
            html.append("<input type=\"hidden\" name=\"operationclass\" value=\"com.labvantage.sapphire.pageelements.workflow.bpmn.BPMNDownloader\">");
            html.append("<input type=\"hidden\" name=\"xpdl\" value=\"N\">");
            html.append("<input type=\"hidden\" name=\"type\" value=\"\">");
            html.append("<textarea type=\"hidden\" name=\"properties\"></textarea>");
            html.append("<input type=\"hidden\" name=\"keyid1\" value=\"").append(this.keyid1).append("\">");
            html.append("<input type=\"hidden\" name=\"keyid2\" value=\"").append(this.keyid2).append("\">");
            html.append("<input type=\"hidden\" name=\"keyid3\" value=\"").append(this.keyid3).append("\">");
            html.append("</form>");
            this.buildReturnForm(html);
            html.append(this.getEndScript(this.workflowprops, this.sdcid, this.keyid1, this.keyid2, this.keyid3, this.viewonly, this.locked, this.rsetid, this.mode, this.changeMade));
        } else {
            html.append("<font color=\"red\">").append(this.getTranslationProcessor().translate("Could not load properties for workflow.")).append("</font>");
        }
        return html.toString();
    }

    private void buildReturnForm(StringBuffer html) {
        html.append("<form style=\"display:none;\" method=POST action=\"").append(this.requestContext.getProperty("returntolistpage")).append("\" name=\"returnForm\" id=\"returnForm\">");
        String restoreselected = this.requestContext.getProperty("restoreselected");
        html.append("<input type=hidden name=\"restoreselected\" value=\"").append(restoreselected.length() > 0 && !restoreselected.equalsIgnoreCase("[NOSTORE]") ? restoreselected : "").append("\">");
        html.append("<input type=hidden name=\"lookupcallback\" value=\"").append(this.requestContext.getProperty("lookupcallback")).append("\">");
        html.append("<input type=hidden name=\"__pagedirectives\" value=\"").append(this.requestContext.getProperty("returntolistpd")).append("\">");
        html.append("<input type=hidden name=\"forcelastsearchtype\" value=\"").append("Y").append("\">");
        html.append("<input type=hidden name=\"returntolistpage\" value=\"").append(this.requestContext.getProperty("returntolistpage")).append("\">");
        String returndata = this.requestContext.getProperty("returntolistdata");
        if (returndata.length() > 0) {
            HashMap rPL = null;
            try {
                rPL = new PropertyList(new JSONObject(returndata));
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (rPL != null) {
                for (Object key : rPL.keySet()) {
                    String val = ((PropertyList)rPL).getProperty(key.toString(), "");
                    if (val.length() <= 0) continue;
                    html.append("<input type=hidden name=\"").append(key).append("\" value=\"").append(val).append("\">");
                }
            }
        }
        html.append("</form>");
    }

    public static enum Tabs {
        WORKFLOW("Workflow Details"),
        TASKS("Workflow Tasks"),
        VARIABLES("Workflow Variables"),
        CATEGORIES("Categories"),
        DEBUG("Property XML");

        private String title;

        private Tabs(String t) {
            this.title = t;
        }

        public String getTitle() {
            return this.title;
        }
    }

    public static enum Mode {
        ADD,
        EDIT;

    }
}

