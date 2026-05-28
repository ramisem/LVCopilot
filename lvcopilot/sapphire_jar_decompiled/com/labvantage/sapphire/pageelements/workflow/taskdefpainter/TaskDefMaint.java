/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.workflow.taskdefpainter;

import com.labvantage.opal.elements.advancedtoolbar.AdvancedToolbar;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.modules.workflow.TaskDef;
import com.labvantage.sapphire.pageelements.attachment.Files;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.controls.Image;
import com.labvantage.sapphire.pageelements.controls.Tab;
import com.labvantage.sapphire.pageelements.controls.TabGroup;
import com.labvantage.sapphire.pageelements.maint.DataView;
import com.labvantage.sapphire.pageelements.maint.MaintDetail;
import com.labvantage.sapphire.pageelements.propertybuilder.PropertyBuilder;
import com.labvantage.sapphire.pageelements.workflow.WorkflowConstants;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefPropertiesStepType;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefVariables;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import com.labvantage.sapphire.tagext.QueryData;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.http.HttpUtil;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import com.labvantage.sapphire.xml.PropertyDefinition;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import java.util.Arrays;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.tagext.SDITagInfo;
import sapphire.util.Browser;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class TaskDefMaint
extends BaseElement
implements WorkflowConstants {
    public static final String JS_CLASS = "taskMaint";
    public static final String USERCONFIG_PREFIX = "taskmaint_";
    public static final String PROPERTY_DEBUG = "debug";
    public static final String PROPERTY_SDCID = "sdcid";
    public static final String PROPERTY_KEYID1 = "keyid1";
    public static final String PROPERTY_KEYID2 = "keyid2";
    public static final String PROPERTY_KEYID3 = "keyid3";
    public static final String PROPERTY_VIEWONLY = "viewonly";
    public static final String PROPERTY_SELECTEDTAB = "selectedtab";
    public static final String PROPERTY_PARENT = "parent";
    public static final String ATTRIBUTE_CHANGED = "_changed";
    public static final String SDCID = "LV_TaskDef";
    public static final String LINK_IO = "taskdefio_link";
    public static final String LINK_STEP = "taskdefstep_link";
    public static final String TABLE_IO = "taskdefio";
    public static final String TABLE_STEP = "taskdefstep";
    public static final String DATACOL = "taskdef";
    public static final String STEPDATACOL = "valuetree";
    public static final String RESOURCE_FILE = "taskdef.xml";
    public static final String CACHE_TASKDEF_BASIC = "Basic";
    public static final String CACHE_TASKDEF_ALL = "All";
    private String keyid1;
    private String keyid2;
    private String keyid3;
    private String sdcid = "LV_TaskDef";
    private PropertyList userConfig;
    private boolean viewonly = false;
    private boolean debug = false;
    private boolean devMode;
    PropertyList taskprops = null;
    private Mode mode = Mode.EDIT;
    private PropertyDefinitionList propertyDef = null;
    private PropertyList layout = null;
    private SDIData taskData = null;
    boolean changeMade = false;
    private String rsetid = "";
    private boolean locked = false;
    private int selectedTab = -1;
    private String pageTitle = "";
    private String parentKeyid1;
    private String parentKeyid2;
    private String parentKeyid3;
    private boolean descendant = false;
    private boolean isChangeControlled = false;

    public static boolean isChangeControlled(String connectionId) {
        CMTPolicy cmtPolicy = CMTPolicy.getPolicy(connectionId, SDCID);
        return "Y".equals(cmtPolicy.getChangeControlledFlag());
    }

    public TaskDefMaint(PageContext pageContext, PropertyList pageproperties) {
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
            this.taskprops = new PropertyList();
            if (this.keyid1.length() > 0) {
                this.mode = Mode.EDIT;
                this.taskData = TaskDefMaint.getTaskData(this.sdcid, this.keyid1, this.keyid2, this.keyid3, !this.viewonly, this.getSDIProcessor(), this.logger, this.taskprops);
                this.rsetid = this.taskData.getRsetid();
                if (this.taskData.getDataset("primary") != null) {
                    if (this.taskData.getDataset("primary").getRowCount() > 0) {
                        String isCMTLockOkStr;
                        String coreFlag;
                        if (this.taskprops.getAttribute(ATTRIBUTE_CHANGED).equalsIgnoreCase("Y")) {
                            this.changeMade = true;
                        }
                        if (this.taskData.getDataset("primary").getValue(0, "basedontaskdefid", "").length() > 0) {
                            this.descendant = true;
                            this.parentKeyid1 = this.taskData.getDataset("primary").getValue(0, "basedontaskdefid", "");
                            this.parentKeyid2 = this.taskData.getDataset("primary").getValue(0, "basedontaskdefversionid", "");
                            this.parentKeyid3 = this.taskData.getDataset("primary").getValue(0, "basedontaskdefvariantid", "");
                            PropertyList parentList = TaskDefMaint.getTaskData(SDCID, this.parentKeyid1, this.parentKeyid2, this.parentKeyid3, false, this.getSDIProcessor(), this.logger);
                            PropertyList childprops = this.taskprops;
                            this.taskprops = TaskDef.getDescendantProperties(parentList, this.taskprops);
                            this.taskprops.setProperty("basedontaskdefid", this.parentKeyid1);
                            this.taskprops.setProperty("basedontaskdefversionid", this.parentKeyid2);
                            this.taskprops.setProperty("basedontaskdefvariantid", this.parentKeyid3);
                        }
                        TaskDefVariables.syncVariables(this.taskprops, null, this.logger);
                        if (!this.taskData.getDataset("primary").getValue(0, "versionstatus", "P").equalsIgnoreCase("P")) {
                            this.logger.debug("Version status current or expired thus view only mode enabled.");
                            this.viewonly = true;
                        }
                        if ((coreFlag = this.taskData.getDataset("primary").getValue(0, "coreflag", "U")).length() > 0 && (coreFlag.equalsIgnoreCase("S") || coreFlag.equalsIgnoreCase("C") || coreFlag.equalsIgnoreCase("Y"))) {
                            this.logger.debug("Task is core task.");
                            if (!this.devMode) {
                                this.viewonly = true;
                            }
                        }
                        String currCompCode = Configuration.getCompcode(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).getDatabaseId());
                        String dataCompCode = this.taskData.getDataset("primary").getValue(0, "compcode", "");
                        if (currCompCode != null && currCompCode.length() > 0) {
                            if (!currCompCode.equals(dataCompCode) && !this.devMode) {
                                this.logger.debug("Task created as part of different component " + dataCompCode + ".");
                                this.viewonly = true;
                            }
                        } else if (dataCompCode.length() > 0 && !this.devMode) {
                            this.logger.debug("Task created as part of component " + dataCompCode + ".");
                            this.viewonly = true;
                        }
                        if (TaskDefMaint.isChangeControlled(this.getConnectionId()) && !"Y".equals(isCMTLockOkStr = this.taskData.getDataset("primary").getValue(0, "isCMTLockOk", "Y"))) {
                            this.viewonly = true;
                        }
                    }
                    boolean bl = this.locked = this.taskData.getDataset("primary").getColumnValues("__lockedby", "").length() > 0;
                }
                if (this.locked) {
                    this.viewonly = true;
                }
            } else {
                this.mode = Mode.ADD;
                this.keyid2 = "1";
                this.keyid3 = "1";
                if (this.descendant) {
                    this.taskData = TaskDefMaint.getTaskData(this.sdcid, this.parentKeyid1, this.parentKeyid2, this.parentKeyid3, false, this.getSDIProcessor(), this.logger, this.taskprops);
                    pri = this.taskData.getDataset("primary");
                    if (pri != null) {
                        String basedontaskdefid = pri.getValue(0, "basedontaskdefid", "");
                        if (basedontaskdefid.length() > 0) {
                            this.parentKeyid1 = basedontaskdefid;
                            this.parentKeyid2 = pri.getValue(0, "taskdefversionid", "");
                            this.parentKeyid3 = pri.getValue(0, "taskdefvariantid", "");
                            this.taskprops = new PropertyList();
                            this.taskData = TaskDefMaint.getTaskData(this.sdcid, this.parentKeyid1, this.parentKeyid2, this.parentKeyid3, false, this.getSDIProcessor(), this.logger, this.taskprops);
                            pri = this.taskData.getDataset("primary");
                        }
                        if (pri != null) {
                            pri.addColumn("__rowstatus", 0);
                            pri.setValue(0, "taskdefid", this.keyid1);
                            pri.setValue(0, "taskdefversionid", this.keyid2);
                            pri.setValue(0, "taskdefvariantid", this.keyid3);
                            pri.setValue(0, "versionstatus", "P");
                            pri.setValue(0, "__rowstatus", "I");
                            pri.setValue(0, "basedontaskdefid", this.parentKeyid1);
                            pri.setValue(0, "basedontaskdefversionid", this.parentKeyid2);
                            pri.setValue(0, "basedontaskdefvariantid", this.parentKeyid3);
                            pri.setValue(0, "coreflag", this.devMode ? "C" : "U");
                            PropertyList childprops = new PropertyList();
                            this.taskprops = TaskDef.getDescendantProperties(this.taskprops, childprops);
                            this.taskprops.setProperty("basedontaskdefid", this.parentKeyid1);
                            this.taskprops.setProperty("basedontaskdefversionid", this.parentKeyid2);
                            this.taskprops.setProperty("basedontaskdefvariantid", this.parentKeyid3);
                        }
                    } else {
                        this.logger.error("Primary data null for parent task.");
                    }
                } else {
                    if (this.getConnectionProcessor().getSapphireConnection().isRtl()) {
                        this.taskprops.setProperty("rtl", "Y");
                    }
                    this.taskData = new SDIData(this.sdcid, "taskdefid", "taskdefversionid", "taskdefvariantid");
                    this.taskprops.setProperty("scope", "W");
                    pri = new DataSet();
                    pri.addColumn("taskdefid", 0);
                    pri.addColumn("taskdefversionid", 0);
                    pri.addColumn("taskdefvariantid", 0);
                    pri.addColumn("__rowstatus", 0);
                    pri.addColumn("__rsetseq", 0);
                    pri.addColumn("__lockstate", 0);
                    pri.addColumn("__lockedby", 0);
                    pri.addColumn("coreflag", 0);
                    pri.addColumn("basedontaskdefid", 0);
                    pri.addColumn("basedontaskdefversionid", 0);
                    pri.addColumn("basedontaskdefvariantid", 0);
                    this.taskData.setDataset("primary", pri);
                    pri.addRow();
                    pri.setValue(0, "taskdefid", this.keyid1);
                    pri.setValue(0, "taskdefversionid", this.keyid2);
                    pri.setValue(0, "taskdefvariantid", this.keyid3);
                    pri.setValue(0, "__rowstatus", "I");
                    pri.setValue(0, "coreflag", this.devMode ? "C" : "U");
                }
                DataSet cats = new DataSet();
                cats.addColumn("categoryid", 0);
                cats.addColumn(PROPERTY_SDCID, 0);
                cats.addColumn(PROPERTY_KEYID1, 0);
                cats.addColumn("__rowstatus", 0);
                cats.addColumn("__rsetseq", 0);
                cats.addColumn("__lockstate", 0);
                cats.addColumn("__lockedby", 0);
                this.taskData.setDataset("category", cats);
            }
            pri = this.taskData.getDataset("primary");
            HashMap<String, QueryData> querymap = new HashMap<String, QueryData>();
            querymap.put("primary", new QueryData("primary", pri));
            querymap.put("category", new QueryData("category", this.taskData.getDataset("category")));
            querymap.put("attachment", new QueryData("attachment", this.taskData.getDataset("attachment")));
            this.sdiInfo = new SDITagInfo(querymap);
            this.sdiInfo.setSDIData(this.taskData);
            this.sdiInfo.setKeycols(new String[]{"taskdefid", "taskdefversionid", "taskdefvariantid"});
            this.sdiInfo.setSdcid(SDCID);
            if (TaskDefMaint.syncronizeTaskProperties(pri, this.taskprops, this.propertyDef, this.mode != Mode.ADD, this.logger)) {
                this.changeMade = true;
            }
            this.setUpLayout(this.layout);
        }
        catch (Exception e) {
            this.taskprops = null;
            this.logger.error("Could not set up painter: " + e.getMessage(), e);
        }
        this.logger.debug("Set up completed.");
    }

    private static boolean syncronizeTaskProperties(DataSet pri, PropertyList taskprops, PropertyDefinitionList propertyDef, boolean syncFromData, Logger logger) {
        boolean changeMade = false;
        if (pri != null && pri.size() == 1 && propertyDef != null) {
            for (int i = 0; i < propertyDef.size(); ++i) {
                String dbvalue;
                PropertyDefinition pd = (PropertyDefinition)propertyDef.get(i);
                if (!pd.getType().equalsIgnoreCase("simple")) continue;
                String prop = pd.getId();
                String propvalue = taskprops.getProperty(prop, pd.getAttributes() != null && pd.getAttributes().get("defaultvalue") != null ? (String)pd.getAttributes().get("defaultvalue") : "");
                if (!pri.isValidColumn(prop)) {
                    pri.addColumn(prop, prop.equalsIgnoreCase("instructions") ? 3 : 0);
                    pri.setValue(0, prop, propvalue);
                    continue;
                }
                if (!syncFromData || (dbvalue = pri.getValue(0, prop, "")).equals(propvalue)) continue;
                if (dbvalue.length() == 0) {
                    pri.setValue(0, prop, propvalue);
                } else {
                    taskprops.setProperty(prop, dbvalue);
                }
                changeMade = true;
            }
            if (changeMade) {
                pri.setValue(0, "__rowstatus", "U");
            }
        } else {
            logger.error("No task data found.");
        }
        return changeMade;
    }

    private void setUpProperties(PropertyList pagedata, HttpServletRequest request) throws Exception {
        this.debug = pagedata.getProperty(PROPERTY_DEBUG, "N").equalsIgnoreCase("Y");
        this.layout = pagedata.getPropertyList("layout");
        this.keyid1 = pagedata.getProperty(PROPERTY_KEYID1, "");
        this.logger.debug("keyid1 = " + this.keyid1);
        this.keyid2 = pagedata.getProperty(PROPERTY_KEYID2, "");
        this.logger.debug("keyid2 = " + this.keyid2);
        this.keyid3 = pagedata.getProperty(PROPERTY_KEYID3, "1");
        this.logger.debug("keyid3 = " + this.keyid3);
        String parent = pagedata.getProperty(PROPERTY_PARENT, "");
        this.logger.debug("parent = " + parent);
        if (parent.length() > 0) {
            String[] par = StringUtil.split(parent, "|");
            this.descendant = true;
            this.parentKeyid1 = par[0];
            this.parentKeyid2 = par[1];
            this.parentKeyid3 = par[2];
        } else {
            this.descendant = false;
            this.parentKeyid1 = "";
            this.parentKeyid2 = "";
            this.parentKeyid3 = "";
        }
        this.viewonly = pagedata.getProperty(PROPERTY_VIEWONLY, "n").equalsIgnoreCase("y");
        this.logger.debug("viewonly = " + this.viewonly);
        if (pagedata.containsKey(PROPERTY_SELECTEDTAB)) {
            try {
                this.selectedTab = Integer.parseInt(pagedata.getProperty(PROPERTY_SELECTEDTAB, "-1"));
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
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

    public static PropertyList getTasksData(boolean listOnly, SDIProcessor sdi, SapphireConnection sapphireConnection, Logger logger) {
        String cache = listOnly ? CACHE_TASKDEF_BASIC : CACHE_TASKDEF_ALL;
        PropertyList taskprops = (PropertyList)CacheUtil.get(sapphireConnection.getDatabaseId(), "TaskDef", cache);
        if (taskprops == null) {
            taskprops = new PropertyList();
            PropertyListCollection tasks = new PropertyListCollection();
            taskprops.setProperty("tasks", tasks);
            SDIData sdiData = null;
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDCid(SDCID);
            sdiRequest.setQueryFrom(DATACOL);
            sdiRequest.setExtendedDataTypes(true);
            sdiRequest.setRequestItem("primary");
            sdiRequest.setRequestItem("category");
            sdiData = sdi.getSDIData(sdiRequest);
            if (sdiData != null) {
                DataSet pri = sdiData.getDataset("primary");
                if (pri != null && pri.getRowCount() > 0) {
                    String versionStatus;
                    String variant2;
                    int version;
                    String taskdefid;
                    int r;
                    boolean itemfound = false;
                    HashMap<String, String> versions = new HashMap<String, String>();
                    if (listOnly) {
                        for (r = 0; r < pri.getRowCount(); ++r) {
                            taskdefid = pri.getValue(r, "taskdefid");
                            version = 0;
                            try {
                                version = Integer.parseInt(pri.getValue(r, "taskdefversionid"));
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                            variant2 = pri.getValue(r, "taskdefvariantid");
                            versionStatus = pri.getValue(r, "versionstatus");
                            if (versions.containsKey(taskdefid + "_" + variant2)) {
                                String old = (String)versions.get(taskdefid + "_" + variant2);
                                if (old != null && old.equalsIgnoreCase("c")) continue;
                                if (versionStatus.equalsIgnoreCase("c")) {
                                    versions.put(taskdefid + "_" + variant2, "C");
                                    continue;
                                }
                                int oldV = 1;
                                try {
                                    oldV = old != null ? Integer.parseInt(old) : 1;
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                                if (version <= oldV) continue;
                                versions.remove(taskdefid + "_" + variant2);
                                versions.put(taskdefid + "_" + variant2, version + "");
                                continue;
                            }
                            versions.put(taskdefid + "_" + variant2, versionStatus.equalsIgnoreCase("c") ? "C" : version + "");
                        }
                    }
                    for (r = 0; r < pri.getRowCount(); ++r) {
                        String taskdefs;
                        String versionToShow;
                        taskdefid = pri.getValue(r, "taskdefid");
                        version = 0;
                        try {
                            version = Integer.parseInt(pri.getValue(r, "taskdefversionid"));
                        }
                        catch (Exception variant2) {
                            // empty catch block
                        }
                        variant2 = pri.getValue(r, "taskdefvariantid");
                        versionStatus = pri.getValue(r, "versionstatus");
                        String string = versionToShow = versions.size() > 0 ? (String)versions.get(taskdefid + "_" + variant2) : "";
                        if (versionToShow.length() != 0 && (!versionToShow.equalsIgnoreCase("C") || !versionStatus.equalsIgnoreCase("C")) && !versionToShow.equals("" + version) || (taskdefs = pri.getClob(r, DATACOL, "")).length() <= 0) continue;
                        try {
                            PropertyList pl = new PropertyList();
                            pl.setPropertyList(taskdefs, false, false);
                            boolean standalone = pl.getProperty("standaloneflag", "N").equalsIgnoreCase("Y");
                            String moddt = pri.getM18n().format(pri.getCalendar(r, "moddt"));
                            pl.setProperty("taskdefid", pri.getValue(r, "taskdefid", ""));
                            pl.setProperty("taskdefversionid", pri.getValue(r, "taskdefversionid", ""));
                            pl.setProperty("taskdefvariantid", pri.getValue(r, "taskdefvariantid", ""));
                            pl.setProperty("moddt", moddt);
                            pl.setProperty("taskkey", pl.getProperty("taskdefid", "") + ";" + pl.getProperty("taskdefversionid", "") + ";" + pl.getProperty("taskdefvariantid", ""));
                            String parentKeyid1 = pri.getValue(r, "basedontaskdefid", "");
                            if (parentKeyid1.length() > 0) {
                                String parentKeyid2 = pri.getValue(r, "basedontaskdefversionid", "");
                                String parentKeyid3 = pri.getValue(r, "basedontaskdefvariantid", "");
                                pl.setProperty("basedontaskdefid", parentKeyid1);
                                pl.setProperty("basedontaskdefversionid", parentKeyid2);
                                pl.setProperty("basedontaskdefvariantid", parentKeyid3);
                            }
                            if (listOnly && pl.containsKey("steps")) {
                                pl.remove("steps");
                            }
                            if (pl.containsKey("stages")) {
                                pl.remove("stages");
                            }
                            pl.setProperty("categories", TaskDefMaint.getCategories(pl.getProperty("taskdefid", ""), pl.getProperty("taskdefversionid", ""), pl.getProperty("taskdefvariantid", ""), sdiData.getDataset("category")));
                            tasks.add(pl);
                            itemfound = true;
                            continue;
                        }
                        catch (Exception e) {
                            logger.error("Cannot obtain task definition.", e);
                        }
                    }
                    if (itemfound) {
                        CacheUtil.put(sapphireConnection.getDatabaseId(), "TaskDef", cache, taskprops);
                    }
                } else {
                    logger.debug("Could not obtain LV_TaskDef data.");
                }
            } else {
                logger.error("Could not obtain sdidata for LV_TaskDef.");
            }
        }
        return taskprops;
    }

    public static PropertyList getTaskData(String sdcid, String keyid1, String keyid2, String keyid3, boolean lock, SDIProcessor sdi, Logger logger) {
        PropertyList out = new PropertyList();
        TaskDefMaint.getTaskData(sdcid, keyid1, keyid2, keyid3, lock, sdi, logger, out);
        return out;
    }

    private static PropertyListCollection getCategories(String keyid1, String keyid2, String keyid3, DataSet cats) {
        PropertyListCollection categories = new PropertyListCollection();
        if (cats != null && cats.getRowCount() > 0) {
            for (int i = 0; i < cats.getRowCount(); ++i) {
                String catid = cats.getValue(i, "categoryid", "");
                String taskdefId = cats.getValue(i, PROPERTY_KEYID1, "");
                if (catid.length() <= 0 || !taskdefId.equalsIgnoreCase(keyid1)) continue;
                PropertyList cat = new PropertyList();
                cat.setProperty("categoryid", catid);
                cat.setProperty("mode", "S");
                categories.add(cat);
            }
        }
        return categories;
    }

    public static SDIData getTaskData(String sdcid, String keyid1, String keyid2, String keyid3, boolean lock, SDIProcessor sdi, Logger logger, PropertyList outProps) {
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
            sdiRequest.setRequestItem("attachment");
            sdiRequest.setRequestItem(TABLE_STEP);
            if (TaskDefMaint.isChangeControlled(sdi.getConnectionid())) {
                sdiRequest.setValidateCheckout(true);
            }
            if ((sdiData = sdi.getSDIData(sdiRequest)) != null) {
                DataSet pri = sdiData.getDataset("primary");
                if (pri != null && pri.getRowCount() > 0) {
                    String taskdefs = pri.getClob(0, DATACOL, "");
                    if (taskdefs.length() > 0) {
                        try {
                            PropertyListCollection varscol;
                            outProps.setPropertyList(taskdefs, false, false);
                            PropertyListCollection stepscol = outProps.getCollection("steps");
                            if (stepscol != null && stepscol.size() > 0) {
                                if (!outProps.containsKey("stages")) {
                                    outProps.setProperty("stages", new PropertyListCollection());
                                }
                                PropertyListCollection stages = outProps.getCollection("stages");
                                for (int i = 0; i < stepscol.size(); ++i) {
                                    PropertyList find;
                                    String progressgroup = stepscol.getPropertyList(i).getProperty("stepgroupid", "");
                                    if (progressgroup.length() <= 0 || (find = stages.find("stageid", progressgroup)) != null) continue;
                                    PropertyList stage = new PropertyList();
                                    stage.setProperty("stageid", progressgroup);
                                    stage.setProperty("text", progressgroup);
                                    stages.add(stage);
                                    propertiesChanged = true;
                                }
                                DataSet steps = sdiData.getDataset(TABLE_STEP);
                                if (steps != null && steps.getRowCount() > 0) {
                                    for (int i = 0; i < steps.getRowCount(); ++i) {
                                        String s;
                                        String e;
                                        PropertyList step;
                                        String stepid = steps.getValue(i, "stepid", "");
                                        if (stepid.length() <= 0 || (step = stepscol.find("stepid", stepid)) == null) continue;
                                        String p = steps.getValue(i, "propertytreeid", step.getProperty("propertytreeid"));
                                        if (p.length() > 0) {
                                            step.setProperty("propertytreeid", p);
                                        }
                                        if ((e = steps.getValue(i, "extendnodeid", step.getProperty("extendnodeid"))).length() > 0) {
                                            step.setProperty("extendnodeid", e);
                                        }
                                        if ((s = steps.getValue(i, STEPDATACOL, "")).length() <= 0) continue;
                                        PropertyList overrides = new PropertyList();
                                        overrides.setPropertyList(s);
                                        step.setProperty("steptypeoverrides", overrides);
                                        step.setProperty("steptypemerged", TaskDefPropertiesStepType.getMergedProperties(overrides, stepid, step.getProperty("propertytreeid"), step.getProperty("extendnodeid"), sdi.getConnectionid()));
                                    }
                                }
                            }
                            if ((varscol = outProps.getCollection("variables")) != null && varscol.size() > 0) {
                                for (int v = 0; v < varscol.size(); ++v) {
                                    String type;
                                    boolean complex;
                                    PropertyList var = varscol.getPropertyList(v);
                                    if (var.containsKey("scope")) {
                                        if (!var.containsKey("setup")) {
                                            var.setProperty("setup", var.getProperty("scope", "L").equalsIgnoreCase("S") ? "Y" : "N");
                                        }
                                        if (!var.containsKey("exposed")) {
                                            var.setProperty("exposed", var.getProperty("scope", "L").equalsIgnoreCase("E") ? "Y" : "N");
                                        }
                                        var.remove("scope");
                                        propertiesChanged = true;
                                    }
                                    boolean bl = complex = (type = var.getProperty("type", "")).equalsIgnoreCase("form") || type.equalsIgnoreCase("sdilist") || type.equalsIgnoreCase("sdidatastore");
                                    if (complex && var.getProperty("exposed").equalsIgnoreCase("Y")) {
                                        var.setProperty("exposed", "N");
                                        propertiesChanged = true;
                                    }
                                    if (complex && var.getProperty("setup").equalsIgnoreCase("Y")) {
                                        var.setProperty("setup", "N");
                                        propertiesChanged = true;
                                    }
                                    var.setId(var.getProperty("variableid"));
                                }
                            }
                            outProps.setProperty("categories", TaskDefMaint.getCategories(keyid1, keyid2, keyid3, sdiData.getDataset("category")));
                        }
                        catch (Exception e) {
                            logger.error("Cannot obtain task definition.", e);
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

    public static PropertyList getTaskData(String connectionid, SDIData sdiData) throws SapphireException {
        PropertyList outProps = new PropertyList();
        boolean propertiesChanged = false;
        if (sdiData != null) {
            String taskdefs;
            DataSet pri = sdiData.getDataset("primary");
            if (pri != null && pri.getRowCount() > 0 && (taskdefs = pri.getClob(0, DATACOL, "")).length() > 0) {
                PropertyListCollection varscol;
                outProps.setPropertyList(taskdefs, false, false);
                PropertyListCollection stepscol = outProps.getCollection("steps");
                if (stepscol != null && stepscol.size() > 0) {
                    if (!outProps.containsKey("stages")) {
                        outProps.setProperty("stages", new PropertyListCollection());
                    }
                    PropertyListCollection stages = outProps.getCollection("stages");
                    for (int i = 0; i < stepscol.size(); ++i) {
                        PropertyList find;
                        String progressgroup = stepscol.getPropertyList(i).getProperty("stepgroupid", "");
                        if (progressgroup.length() <= 0 || (find = stages.find("stageid", progressgroup)) != null) continue;
                        PropertyList stage = new PropertyList();
                        stage.setProperty("stageid", progressgroup);
                        stage.setProperty("text", progressgroup);
                        stages.add(stage);
                        propertiesChanged = true;
                    }
                    DataSet steps = sdiData.getDataset(TABLE_STEP);
                    if (steps != null && steps.getRowCount() > 0) {
                        for (int i = 0; i < steps.getRowCount(); ++i) {
                            String s;
                            String e;
                            PropertyList step;
                            String stepid = steps.getValue(i, "stepid", "");
                            if (stepid.length() <= 0 || (step = stepscol.find("stepid", stepid)) == null) continue;
                            String p = steps.getValue(i, "propertytreeid", step.getProperty("propertytreeid"));
                            if (p.length() > 0) {
                                step.setProperty("propertytreeid", p);
                            }
                            if ((e = steps.getValue(i, "extendnodeid", step.getProperty("extendnodeid"))).length() > 0) {
                                step.setProperty("extendnodeid", e);
                            }
                            if ((s = steps.getValue(i, STEPDATACOL, "")).length() <= 0) continue;
                            PropertyList overrides = new PropertyList();
                            overrides.setPropertyList(s);
                            step.setProperty("steptypeoverrides", overrides);
                            step.setProperty("steptypemerged", TaskDefPropertiesStepType.getMergedProperties(overrides, stepid, step.getProperty("propertytreeid"), step.getProperty("extendnodeid"), connectionid));
                        }
                    }
                }
                if ((varscol = outProps.getCollection("variables")) != null && varscol.size() > 0) {
                    for (int v = 0; v < varscol.size(); ++v) {
                        String type;
                        boolean complex;
                        PropertyList var = varscol.getPropertyList(v);
                        if (var.containsKey("scope")) {
                            if (!var.containsKey("setup")) {
                                var.setProperty("setup", var.getProperty("scope", "L").equalsIgnoreCase("S") ? "Y" : "N");
                            }
                            if (!var.containsKey("exposed")) {
                                var.setProperty("exposed", var.getProperty("scope", "L").equalsIgnoreCase("E") ? "Y" : "N");
                            }
                            var.remove("scope");
                            propertiesChanged = true;
                        }
                        boolean bl = complex = (type = var.getProperty("type", "")).equalsIgnoreCase("form") || type.equalsIgnoreCase("sdilist") || type.equalsIgnoreCase("sdidatastore");
                        if (complex && var.getProperty("exposed").equalsIgnoreCase("Y")) {
                            var.setProperty("exposed", "N");
                            propertiesChanged = true;
                        }
                        if (complex && var.getProperty("setup").equalsIgnoreCase("Y")) {
                            var.setProperty("setup", "N");
                            propertiesChanged = true;
                        }
                        var.setId(var.getProperty("variableid"));
                    }
                }
            }
            if (outProps != null && propertiesChanged) {
                outProps.setAttribute(ATTRIBUTE_CHANGED, "Y");
            }
        }
        return outProps;
    }

    private StringBuffer getScriptAndStyle() {
        StringBuffer html = new StringBuffer();
        boolean expandedMode = true;
        html.append("\n<script> ");
        html.append("document.title='").append(SafeHTML.encodeForJavaScript(this.pageTitle.replaceAll("\\<.*?\\>", ""))).append("';");
        html.append("</script>");
        html.append(JavaScriptAPITag.getJQueryAPI(true, false, null, "", !this.devMode, this.pageContext));
        html.append("\n<script type=\"text/javascript\" src=\"WEB-CORE/elements/workflow/scripts/taskmaint").append(expandedMode ? "" : ".min").append(".js\"></script>");
        html.append("\n<link rel=\"stylesheet\" href=\"" + HttpUtil.getCSS("WEB-CORE/elements/richtext/stylesheets/formbuilder.css", this.pageContext) + "\" type=\"text/css\">");
        html.append("\n<link rel=\"stylesheet\" href=\"" + HttpUtil.getCSS("WEB-CORE/elements/workflow/stylesheets/workflowmaint.css", this.pageContext) + "\" type=\"text/css\">");
        html.append("\n<script> ");
        html.append("taskMaint.isChangeControlled = ").append(this.isChangeControlled).append(";");
        html.append("</script>");
        return html;
    }

    private StringBuffer getEndScript(PropertyList props, String sdcid, String keyid1, String keyid2, String keyid3, boolean viewOnly, boolean descendant, boolean locked, String rsetid, boolean changesMade, Mode mode) {
        StringBuffer html = new StringBuffer();
        html.append("<script type=\"text/javascript\">");
        html.append(JS_CLASS).append(".properties=sapphire.util.propertyList.create(").append(props.toJSONString(true)).append(");");
        html.append(JS_CLASS).append(".keyid1='").append(SafeHTML.encodeForJavaScript(keyid1)).append("';");
        html.append(JS_CLASS).append(".keyid2='").append(SafeHTML.encodeForJavaScript(keyid2)).append("';");
        html.append(JS_CLASS).append(".keyid3='").append(SafeHTML.encodeForJavaScript(keyid3)).append("';");
        html.append(JS_CLASS).append(".mode='").append(SafeHTML.encodeForJavaScript(mode.toString().toLowerCase())).append("';");
        if (this.changeMade) {
            html.append("sapphire.events.registerLoadListener(function(){");
            html.append(JS_CLASS).append(".isSaved(false);");
            html.append("},false,500);");
        }
        html.append("__rsetlist='").append(rsetid).append("';");
        html.append(JS_CLASS).append(".locked=").append(locked).append(";");
        html.append(JS_CLASS).append(".viewonly=").append(viewOnly).append(";");
        html.append(JS_CLASS).append(".descendant=").append(descendant).append(";");
        html.append(JS_CLASS).append(".resVars=").append(new JSONArray(Arrays.asList(RESERVED_VARIANTS)).toString()).append(";");
        html.append(JS_CLASS).append(".checkTab();");
        html.append(JS_CLASS).append(".ui.toggleStandalone();");
        html.append("if (typeof(richText)!='undefined'){");
        html.append("richText.setMaximizeProperties('pr0_instructions','tab_task_tabgroup__content0',sapphire.browser.ie?0:0,sapphire.browser.ie?0:0);");
        html.append("}");
        html.append("</script>");
        return html;
    }

    private StringBuffer getToolBar() {
        StringBuffer html = new StringBuffer();
        TranslationProcessor tp = this.getTranslationProcessor();
        html.append("<table cellpadding=0 cellspacing=0 id=\"toolbarcontainer\" class=\"pagebuttonsection\"><tr>");
        html.append("<td class=\"ribbon_group_file\">");
        PropertyList toolbar = new PropertyList();
        toolbar.setProperty("rendermode", "Button");
        toolbar.setProperty("pagetitle", "");
        toolbar.setProperty("showtitle", "N");
        PropertyListCollection buttons = new PropertyListCollection();
        PropertyList btn = new PropertyList();
        btn.setProperty("id", "btSave");
        btn.setProperty("buttontype", "User");
        PropertyList common = new PropertyList();
        common.setProperty("text", tp.translate("Save"));
        common.setProperty("image", "WEB-CORE/images/png/Save.png");
        common.setProperty("imagelarge", "WEB-CORE/images/png32/Save.png");
        common.setProperty("group", "File");
        common.setProperty("ribbonstyle", "Large");
        toolbar.setProperty("buttons", "Ribbon");
        btn.setProperty("commonprops", common);
        PropertyList user = new PropertyList();
        user.setProperty("action", "taskMaint.buttons.save()");
        user.setProperty("releaselock", "N");
        btn.setProperty("userbuttonprops", user);
        buttons.add(btn);
        btn = new PropertyList();
        btn.setProperty("id", "btTest");
        btn.setProperty("buttontype", "User");
        common = new PropertyList();
        common.setProperty("text", tp.translate("Save & Test"));
        common.setProperty("image", "WEB-CORE/images/png/Run.png");
        common.setProperty("imagelarge", "WEB-CORE/images/png/Run.png");
        common.setProperty("group", "File");
        common.setProperty("ribbonstyle", "Large");
        toolbar.setProperty("buttons", "Ribbon");
        btn.setProperty("commonprops", common);
        user = new PropertyList();
        user.setProperty("action", "taskMaint.buttons.test(true)");
        user.setProperty("releaselock", "N");
        btn.setProperty("userbuttonprops", user);
        buttons.add(btn);
        btn = new PropertyList();
        btn.setProperty("id", "btCheck");
        btn.setProperty("buttontype", "User");
        common = new PropertyList();
        common.setProperty("text", tp.translate("Check Task"));
        common.setProperty("image", "WEB-CORE/images/png/ApproveVersion.png");
        common.setProperty("imagelarge", "WEB-CORE/images/png/ApproveVersion.png");
        common.setProperty("group", "File");
        common.setProperty("ribbonstyle", "Large");
        toolbar.setProperty("buttons", "Ribbon");
        btn.setProperty("commonprops", common);
        user = new PropertyList();
        user.setProperty("action", "taskMaint.buttons.validate()");
        user.setProperty("releaselock", "N");
        btn.setProperty("userbuttonprops", user);
        buttons.add(btn);
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
        user.setProperty("action", "taskMaint.buttons.add()");
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
            user.setProperty("action", "taskMaint.buttons.checkOut()");
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
            user.setProperty("action", "taskMaint.buttons.checkIn()");
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
            user.setProperty("action", "taskMaint.buttons.undoCheckOut()");
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
            user.setProperty("action", "taskMaint.buttons.viewChangeHistory()");
            user.setProperty("releaselock", "N");
            btn.setProperty("userbuttonprops", user);
            buttons.add(btn);
        }
        if (this.requestContext.getProperty("returntolistpage").length() > 0) {
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
            user.setProperty("action", "taskMaint.buttons.returnTo();");
            user.setProperty("releaselock", "N");
            btn.setProperty("userbuttonprops", user);
            buttons.add(btn);
        } else {
            btn = new PropertyList();
            btn.setProperty("id", "btReturn");
            btn.setProperty("buttontype", "User");
            common = new PropertyList();
            common.setProperty("text", tp.translate("Close"));
            common.setProperty("image", "WEB-CORE/images/png/Close.png");
            common.setProperty("imagelarge", "WEB-CORE/images/png32/Close.png");
            common.setProperty("group", "File");
            common.setProperty("ribbonstyle", "Small");
            toolbar.setProperty("buttons", "Ribbon");
            btn.setProperty("commonprops", common);
            user = new PropertyList();
            user.setProperty("action", "taskMaint.buttons.close()");
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
        html.append("</td><td class=\"ribbon_group_taskio\">");
        toolbar = new PropertyList();
        toolbar.setProperty("rendermode", "Button");
        toolbar.setProperty("pagetitle", "");
        toolbar.setProperty("showtitle", "N");
        buttons = new PropertyListCollection();
        if (!this.descendant && !this.viewonly) {
            btn = new PropertyList();
            btn.setProperty("buttontype", "User");
            common = new PropertyList();
            btn.setProperty("id", "btAddIn");
            common.setProperty("text", "Add Input");
            common.setProperty("image", "WEB-CORE/images/png32/Add.png");
            common.setProperty("group", "Task IO");
            common.setProperty("ribbonstyle", "Large");
            toolbar.setProperty("buttons", "Ribbon");
            btn.setProperty("commonprops", common);
            user = new PropertyList();
            user.setProperty("action", "taskMaint.buttons.addIO('input')");
            user.setProperty("releaselock", "N");
            btn.setProperty("userbuttonprops", user);
            buttons.add(btn);
            btn = new PropertyList();
            btn.setProperty("buttontype", "User");
            common = new PropertyList();
            btn.setProperty("id", "btAddOut");
            common.setProperty("text", "Add Output");
            common.setProperty("image", "WEB-CORE/images/png32/Add.png");
            common.setProperty("group", "Task IO");
            common.setProperty("ribbonstyle", "Large");
            toolbar.setProperty("buttons", "Ribbon");
            btn.setProperty("commonprops", common);
            user = new PropertyList();
            user.setProperty("action", "taskMaint.buttons.addIO('output')");
            user.setProperty("releaselock", "N");
            btn.setProperty("userbuttonprops", user);
            buttons.add(btn);
            btn = new PropertyList();
            btn.setProperty("buttontype", "User");
            common = new PropertyList();
            btn.setProperty("id", "btDeleteIO");
            common.setProperty("text", "Delete");
            common.setProperty("image", "WEB-CORE/images/png32/Delete.png");
            common.setProperty("group", "Task IO");
            common.setProperty("ribbonstyle", "Large");
            toolbar.setProperty("buttons", "Ribbon");
            btn.setProperty("commonprops", common);
            user = new PropertyList();
            user.setProperty("action", "taskworkflow_frame.taskWorkflow.buttons.deleteIO()");
            user.setProperty("releaselock", "N");
            btn.setProperty("userbuttonprops", user);
            buttons.add(btn);
        }
        toolbar.setProperty("buttons", buttons);
        advancedToolbar = new AdvancedToolbar();
        advancedToolbar.setPageContext(this.pageContext);
        advancedToolbar.setElementid("TaskIO_Toolbar");
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
        common.setProperty("text", "Maximize");
        common.setProperty("image", "WEB-CORE/elements/richtext/images/gif/ToolbarMaxMin.gif");
        common.setProperty("group", "Actions");
        common.setProperty("ribbonstyle", "Large");
        toolbar.setProperty("buttons", "Ribbon");
        btn.setProperty("commonprops", common);
        user = new PropertyList();
        user.setProperty("action", "taskMaint.buttons.maxMinPainter()");
        user.setProperty("releaselock", "N");
        btn.setProperty("userbuttonprops", user);
        buttons.add(btn);
        if (!this.descendant && !this.viewonly) {
            btn = new PropertyList();
            btn.setProperty("buttontype", "User");
            common = new PropertyList();
            btn.setProperty("id", "btAddIn2");
            common.setProperty("text", "Add Input");
            common.setProperty("image", "WEB-CORE/images/png32/Add.png");
            common.setProperty("group", "Actions");
            common.setProperty("ribbonstyle", "Small");
            toolbar.setProperty("buttons", "Ribbon");
            btn.setProperty("commonprops", common);
            user = new PropertyList();
            user.setProperty("action", "taskMaint.buttons.addIO('input')");
            user.setProperty("releaselock", "N");
            btn.setProperty("userbuttonprops", user);
            buttons.add(btn);
            btn = new PropertyList();
            btn.setProperty("buttontype", "User");
            common = new PropertyList();
            btn.setProperty("id", "btAddOut2");
            common.setProperty("text", "Add Output");
            common.setProperty("image", "WEB-CORE/images/png32/Add.png");
            common.setProperty("group", "Actions");
            common.setProperty("ribbonstyle", "Small");
            toolbar.setProperty("buttons", "Ribbon");
            btn.setProperty("commonprops", common);
            user = new PropertyList();
            user.setProperty("action", "taskMaint.buttons.addIO('output')");
            user.setProperty("releaselock", "N");
            btn.setProperty("userbuttonprops", user);
            buttons.add(btn);
            btn = new PropertyList();
            btn.setProperty("buttontype", "User");
            common = new PropertyList();
            btn.setProperty("id", "btAddVar");
            common.setProperty("text", "Add Variable");
            common.setProperty("image", "WEB-CORE/images/png32/Add.png");
            common.setProperty("group", "Actions");
            common.setProperty("ribbonstyle", "Small");
            toolbar.setProperty("buttons", "Ribbon");
            btn.setProperty("commonprops", common);
            user = new PropertyList();
            user.setProperty("action", "taskdef_frame.taskDef.buttons.addVar()");
            user.setProperty("releaselock", "N");
            btn.setProperty("userbuttonprops", user);
            buttons.add(btn);
        }
        btn = new PropertyList();
        btn.setProperty("buttontype", "User");
        common = new PropertyList();
        btn.setProperty("id", "btDelete");
        common.setProperty("text", "Delete");
        common.setProperty("image", "WEB-CORE/images/png32/Delete.png");
        common.setProperty("group", "Actions");
        common.setProperty("ribbonstyle", "Large");
        toolbar.setProperty("buttons", "Ribbon");
        btn.setProperty("commonprops", common);
        user = new PropertyList();
        user.setProperty("action", "taskdef_frame.taskDef.buttons.deleteItem()");
        user.setProperty("releaselock", "N");
        btn.setProperty("userbuttonprops", user);
        buttons.add(btn);
        btn = new PropertyList();
        btn.setProperty("buttontype", "User");
        common = new PropertyList();
        btn.setProperty("id", "btCopy");
        common.setProperty("text", "Duplicate");
        common.setProperty("image", "WEB-CORE/images/png32/Copy.png");
        common.setProperty("group", "Actions");
        common.setProperty("ribbonstyle", "Large");
        toolbar.setProperty("buttons", "Ribbon");
        btn.setProperty("commonprops", common);
        user = new PropertyList();
        user.setProperty("action", "taskdef_frame.taskDef.buttons.copyItem()");
        user.setProperty("releaselock", "N");
        btn.setProperty("userbuttonprops", user);
        buttons.add(btn);
        toolbar.setProperty("buttons", buttons);
        advancedToolbar = new AdvancedToolbar();
        advancedToolbar.setPageContext(this.pageContext);
        advancedToolbar.setElementid("Actions_Toolbar");
        advancedToolbar.setElementProperties(toolbar);
        html.append(advancedToolbar.getHtml());
        html.append("</td><td class=\"ribbon_group_stages\">");
        toolbar = new PropertyList();
        toolbar.setProperty("rendermode", "Button");
        toolbar.setProperty("pagetitle", "");
        toolbar.setProperty("showtitle", "N");
        buttons = new PropertyListCollection();
        btn = new PropertyList();
        btn.setProperty("buttontype", "User");
        common = new PropertyList();
        btn.setProperty("id", "btAddStage");
        common.setProperty("text", "Add Stage");
        common.setProperty("image", "WEB-CORE/images/png32/Add.png");
        common.setProperty("group", "Stages");
        common.setProperty("ribbonstyle", "Large");
        toolbar.setProperty("buttons", "Ribbon");
        btn.setProperty("commonprops", common);
        user = new PropertyList();
        user.setProperty("action", "taskMaint.buttons.addStage()");
        user.setProperty("releaselock", "N");
        btn.setProperty("userbuttonprops", user);
        buttons.add(btn);
        toolbar.setProperty("buttons", buttons);
        advancedToolbar = new AdvancedToolbar();
        advancedToolbar.setPageContext(this.pageContext);
        advancedToolbar.setElementid("Stages_Toolbar");
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
        common.setProperty("text", "Add Category");
        common.setProperty("image", "WEB-CORE/images/png32/Add.png");
        common.setProperty("group", "Categories");
        common.setProperty("ribbonstyle", "Large");
        toolbar.setProperty("buttons", "Ribbon");
        btn.setProperty("commonprops", common);
        user = new PropertyList();
        user.setProperty("action", "taskMaint.buttons.addCategory()");
        user.setProperty("releaselock", "N");
        btn.setProperty("userbuttonprops", user);
        buttons.add(btn);
        toolbar.setProperty("buttons", buttons);
        advancedToolbar = new AdvancedToolbar();
        advancedToolbar.setPageContext(this.pageContext);
        advancedToolbar.setElementid("Categories_Toolbar");
        advancedToolbar.setElementProperties(toolbar);
        html.append(advancedToolbar.getHtml());
        html.append("</td>");
        if (this.debug || this.devMode) {
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
            common.setProperty("text", "Edit XML");
            common.setProperty("image", "rc?command=image&image=Code");
            common.setProperty("group", "Debug");
            common.setProperty("ribbonstyle", "Large");
            toolbar.setProperty("buttons", "Ribbon");
            btn.setProperty("commonprops", common);
            user = new PropertyList();
            user.setProperty("action", "taskMaint.buttons.debugProperties(false)");
            user.setProperty("releaselock", "N");
            btn.setProperty("userbuttonprops", user);
            buttons.add(btn);
            btn = new PropertyList();
            btn.setProperty("buttontype", "User");
            common = new PropertyList();
            btn.setProperty("id", "btDebugShowJSON");
            common.setProperty("text", "Edit JSON");
            common.setProperty("image", "rc?command=image&image=CodeJavascript");
            common.setProperty("group", "Debug");
            common.setProperty("ribbonstyle", "Large");
            toolbar.setProperty("buttons", "Ribbon");
            btn.setProperty("commonprops", common);
            user = new PropertyList();
            user.setProperty("action", "taskMaint.buttons.debugProperties(true)");
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

    private String getTaskTab() {
        return TaskDefMaint.getTaskTab(this.taskData.getDataset("primary"), this.sdcid, this.viewonly, this.mode, this.getConnectionId(), this.browser, this.pageContext);
    }

    public static String getTaskTab(String keyid1, String keyid2, String keyid3, PropertyList taskprops, boolean viewonly, Mode mode, ConnectionInfo connectionInfo, PageContext pageContext, Logger logger) {
        DataSet primary = new DataSet(connectionInfo);
        primary.addRow();
        PropertyBuilder pb = new PropertyBuilder(pageContext, RESOURCE_FILE);
        TaskDefMaint.syncronizeTaskProperties(primary, taskprops, pb.getPropertyDefinition(), false, logger);
        primary.addColumn("taskdefid", 0);
        primary.setValue(0, "taskdefid", keyid1);
        primary.addColumn("taskdefversionid", 0);
        primary.setValue(0, "taskdefversionid", keyid2);
        primary.setValue(0, "taskdefvariantid", keyid3);
        return TaskDefMaint.getTaskTab(primary, SDCID, viewonly, mode, connectionInfo.getConnectionId(), new Browser(pageContext), pageContext);
    }

    private static String getTaskTab(DataSet primary, String sdcid, boolean viewonly, Mode mode, String connectionId, Browser browser, PageContext pageContext) {
        StringBuffer html = new StringBuffer();
        TranslationProcessor tp = new TranslationProcessor(pageContext);
        if (primary != null) {
            boolean descendant = primary.getValue(0, "basedontaskdefid", "").length() > 0;
            DataView maint = new DataView(pageContext, "primary", primary, "", connectionId);
            maint.setElementid("task_primary_dataview");
            maint.setSDCId(sdcid);
            maint.getSDIInfo().setSdcid(sdcid);
            PropertyList maintProps = new PropertyList();
            maintProps.setProperty(PROPERTY_SDCID, sdcid);
            maintProps.setProperty("style", "FormWithFieldGroups");
            maintProps.setProperty("formcols", "2");
            if (viewonly) {
                maintProps.setProperty(PROPERTY_VIEWONLY, "Y");
            }
            PropertyListCollection columns = new PropertyListCollection();
            PropertyListCollection events = new PropertyListCollection();
            if (!viewonly) {
                PropertyList event = new PropertyList();
                event.setProperty("event", "onchange");
                event.setProperty("js", "taskMaint.maintFieldChange(event,this)");
                events.add(event);
            }
            PropertyList column = new PropertyList();
            column = new PropertyList();
            column.setProperty("columnid", "taskdefid");
            column.setProperty("title", tp.translate("Task Id"));
            column.setProperty("mode", !viewonly ? "input" : "readonly");
            column.setProperty("groupid", "Task");
            if (mode == Mode.ADD) {
                column.setProperty("class", "mandatoryfield");
            }
            column.setProperty("events", events);
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "taskdefversionid");
            column.setProperty("title", tp.translate("Version"));
            column.setProperty("mode", "readonly");
            column.setProperty("groupid", "Task");
            column.setProperty("events", events);
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "taskdefvariantid");
            column.setProperty("title", tp.translate("Variant"));
            column.setProperty("mode", mode == Mode.ADD ? "input" : "readonly");
            column.setProperty("groupid", "Task");
            if (mode == Mode.ADD) {
                column.setProperty("class", "mandatoryfield");
            }
            column.setProperty("events", events);
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "coreflag");
            column.setProperty("title", tp.translate("System Task"));
            ConfigurationProcessor config = new ConfigurationProcessor(pageContext);
            column.setProperty("mode", "readonly");
            column.setProperty("displayvalue", "S=Yes;C=Yes;Y=Yes;N=No;U=No;=No");
            column.setProperty("groupid", "Task");
            column.setProperty("colspan", "1");
            columns.add(column);
            if (descendant) {
                column = new PropertyList();
                column.setProperty("columnid", "_descendant");
                column.setProperty("title", tp.translate("Parent Task"));
                column.setProperty("mode", "readonly");
                column.setProperty("pseudocolumn", "<a href=\"javascript:sapphire.page.navigate('rc?command=page&page=LV_TaskDefMaint&keyid1=" + primary.getValue(0, "basedontaskdefid", "") + "&keyid2=" + primary.getValue(0, "basedontaskdefversionid", "") + "&keyid3=" + primary.getValue(0, "basedontaskdefvariantid", "") + "');void(0);\">" + primary.getValue(0, "basedontaskdefid", "") + " (" + primary.getValue(0, "basedontaskdefversionid", "") + " - " + primary.getValue(0, "basedontaskdefvariantid", "") + ")</a>");
                column.setProperty("groupid", "Task");
                column.setProperty("colspan", "2");
                columns.add(column);
            }
            column = new PropertyList();
            column.setProperty("columnid", "taskdefdesc");
            column.setProperty("title", tp.translate("Description"));
            column.setProperty("mode", !viewonly ? "input" : "readonly");
            column.setProperty("class", "task_longtext");
            column.setProperty("groupid", "Task");
            column.setProperty("colspan", "1");
            column.setProperty("events", events);
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "longtitle");
            column.setProperty("title", tp.translate("Execution Title"));
            column.setProperty("mode", !viewonly ? "input" : "readonly");
            column.setProperty("class", "task_longtext");
            column.setProperty("groupid", "Task");
            column.setProperty("colspan", "1");
            column.setProperty("events", events);
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "instructions");
            column.setProperty("title", tp.translate("Instructions"));
            column.setProperty("mode", !viewonly ? "formattedtext" : "html");
            column.setProperty("size", "auto;80");
            column.setProperty("groupid", "Task");
            column.setProperty("colspan", "2");
            column.setProperty("events", events);
            columns.add(column);
            boolean autoexec = primary.getValue(0, "autoexec", "N").equalsIgnoreCase("Y");
            column = new PropertyList();
            column.setProperty("columnid", "autoexec");
            column.setProperty("title", tp.translate("Auto Execute"));
            column.setProperty("mode", !viewonly && !descendant ? "checkbox" : "readonly");
            column.setProperty("displayvalue", "Y=Yes;N=No");
            column.setProperty("groupid", "Execution");
            column.setProperty("events", events);
            column.setProperty("colspan", "1");
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "summary");
            column.setProperty("title", tp.translate("Summary Statement"));
            column.setProperty("mode", !viewonly ? "inputarea" : "readonly");
            column.setProperty("groupid", "Execution");
            column.setProperty("class", "task_longtext");
            column.setProperty("events", events);
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "allowdraft");
            column.setProperty("title", tp.translate("Allow Pause"));
            column.setProperty("mode", !viewonly && !descendant ? "checkbox" : "readonly");
            column.setProperty("displayvalue", "Y=" + tp.translate("Yes") + ";N=" + tp.translate("No"));
            column.setProperty("groupid", "Options");
            column.setProperty("events", events);
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "standaloneflag");
            column.setProperty("title", tp.translate("Standalone"));
            column.setProperty("mode", !viewonly && !descendant ? "checkbox" : "readonly");
            column.setProperty("displayvalue", "Y=Yes;N=No");
            column.setProperty("groupid", "Options");
            column.setProperty("events", events);
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "allowcancel");
            column.setProperty("title", tp.translate("Cancel Options"));
            column.setProperty("mode", !viewonly && !descendant ? "dropdownlist" : "readonly");
            column.setProperty("displayvalue", "N=" + tp.translate("Cancel not available") + ";Y=" + tp.translate("Cancel will return the allocated items to the current task queue") + ";Q=" + tp.translate("Cancel will remove the allocated items from the current task queue") + ";E=" + tp.translate("Cancel will remove the allocated items from all queues of the current workflow execution") + ";W=" + tp.translate("Cancel will remove the allocated items from all queues of the current workflow") + ";A=" + tp.translate("Cancel will remove the allocated items from all queues of all workflows"));
            column.setProperty("groupid", "Options");
            column.setProperty("events", events);
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "cancelscript");
            column.setProperty("title", tp.translate("Cancel Script"));
            column.setProperty("mode", !viewonly && !descendant ? "inputarea" : "readonly");
            column.setProperty("groupid", "Options");
            column.setProperty("class", "task_longtext");
            column.setProperty("events", events);
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "autoshowinfo");
            column.setProperty("title", tp.translate("Auto Show Info"));
            column.setProperty("mode", !viewonly && !descendant ? "dropdownlist" : "readonly");
            column.setProperty("displayvalue", "S=" + tp.translate("Step Instructions") + ";D=" + tp.translate("Diagram") + ";T=" + tp.translate("Task Instructions") + ";L=" + tp.translate("Task History"));
            column.setProperty("groupid", "Options");
            column.setProperty("events", events);
            columns.add(column);
            boolean standalone = primary.getValue(0, "standaloneflag", "N").equalsIgnoreCase("Y");
            column = new PropertyList();
            column.setProperty("columnid", "complete");
            column.setProperty("title", tp.translate("Complete Action"));
            column.setProperty("mode", !viewonly ? "dropdownlist" : "readonly");
            column.setProperty("dropdownvalues", "gotopage;restart");
            column.setProperty("groupid", "Standalone");
            column.setProperty("events", events);
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "completepage");
            column.setProperty("title", tp.translate("Complete Page"));
            column.setProperty("mode", !viewonly ? "lookup" : "readonly");
            PropertyList lookup = new PropertyList();
            lookup.setProperty("href", "javascript:taskMaint.lookupLink(this)");
            lookup.setProperty("tip", tp.translate("Lookup Link"));
            column.setProperty("lookuplink", lookup);
            column.setProperty("class", "task_longtext");
            column.setProperty("groupid", "Standalone");
            column.setProperty("events", events);
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "cancel");
            column.setProperty("title", tp.translate("Cancel Action"));
            column.setProperty("mode", !viewonly ? "dropdownlist" : "readonly");
            column.setProperty("dropdownvalues", "gotopage;restart");
            column.setProperty("groupid", "Standalone");
            column.setProperty("events", events);
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "cancelpage");
            column.setProperty("title", tp.translate("Cancel Page"));
            column.setProperty("mode", !viewonly ? "lookup" : "readonly");
            column.setProperty("events", events);
            lookup = new PropertyList();
            lookup.setProperty("href", "javascript:taskMaint.lookupLink(this)");
            lookup.setProperty("tip", "Lookup Link");
            column.setProperty("lookuplink", lookup);
            column.setProperty("class", "task_longtext");
            column.setProperty("groupid", "Standalone");
            column.setProperty("events", events);
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "cancelconfirm");
            column.setProperty("title", tp.translate("Confirm Cancel"));
            column.setProperty("mode", !viewonly ? "checkbox" : "readonly");
            column.setProperty("displayvalue", "Y=Yes;N=No");
            column.setProperty("groupid", "Standalone");
            column.setProperty("events", events);
            columns.add(column);
            maintProps.setProperty("columns", columns);
            maint.setElementProperties(maintProps);
            html.append("<div id=\"task_detailscontent\" style=\"padding: 5px 5px 5px 5px;overflow-y: auto!important;height:100%;\">");
            html.append("<form name=\"taskdef_primary\">");
            html.append(maint.getHtml());
            html.append("</form>");
            html.append("</div>");
        } else {
            html.append("<font style=\"color:red;\">" + tp.translate("Could not load data.") + "</font>");
        }
        return html.toString();
    }

    private StringBuffer getIOTab() {
        StringBuffer html = new StringBuffer();
        html.append("<div id=\"taskworkflow_frame_content\" style=\"background-color:#C3DAF9;padding:0;margin:0;").append(this.browser.isIE() ? "" : "display:inline;").append("\">");
        html.append("<iframe name=\"taskworkflow_frame\" id=\"taskworkflow_frame\" src=\"").append(this.browser.getBlankSrc()).append("\" scrolling=\"no\" style=\"width:100%;height:100%;\" frameborder=0></iframe>");
        html.append("</div>");
        if (this.browser.isIE() && this.requestContext.getProperty("html5").equalsIgnoreCase("Y")) {
            html.append("<script>");
            html.append("if (__sizeAdjust){__sizeAdjust.addElement(taskworkflow_frame_content,'(document.body.clientHeight - taskworkflow_frame_content.getBoundingClientRect().top - 5)');}");
            html.append("</script>");
        }
        html.append("<form name=\"taskworkflow_form\" id=\"taskworkflow_form\" action=\"rc?command=file&file=WEB-CORE/elements/workflow/taskworkflow.jsp\" method=POST target=\"taskworkflow_frame\" style=\"display:none;\">");
        html.append("<textarea name=\"").append("properties").append("\" id=\"").append("properties").append("\">").append("").append("</textarea>");
        html.append("<input type=\"hidden\" name=\"").append(PROPERTY_VIEWONLY).append("\" id=\"").append(PROPERTY_VIEWONLY).append("\" value=\"").append(this.viewonly ? "Y" : "N").append("\">");
        html.append("<input type=\"hidden\" name=\"").append("descendant").append("\" id=\"").append("descendant").append("\" value=\"").append(this.descendant ? "Y" : "N").append("\">");
        html.append("</form>");
        return html;
    }

    private StringBuffer getSetupTab() {
        PropertyDefinition pd;
        StringBuffer html = new StringBuffer();
        html.append("<table style=\"table-layout:fixed;\" width=\"100%\" height=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" class=\"form_bar\">");
        html.append("<tbody>");
        html.append("<tr class=\"form_merge\">");
        html.append("<td colspan=\"3\">");
        html.append("</td>");
        html.append("</tr>");
        html.append("<tr style=\"height:").append("auto").append(";\">");
        html.append("<td colspan=\"3\" class=\"form_bar_parentcell\">");
        html.append("<table width=\"100%\" height=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" class=\"form_bar_childtable\">");
        html.append("<tbody>");
        html.append("<tr>");
        html.append("<td valign=\"top\" align=\"left\" class=\"form_bar_childcell\" nowrap>");
        html.append("<div align=\"left\" id=\"task_setupcontent\"  style=\"overflow-y:auto;overflow-x:auto;width:100%;height:100%;\">");
        html.append("<iframe src=\"").append(this.browser.getBlankSrc()).append("\" id=\"task_setup_frame\" name=\"task_setup_frame\" frameborder=0 scroll=\"no\" style=\"width:100%;height:100%;\"></iframe>");
        PropertyList setupprops = new PropertyList();
        PropertyListCollection inputs = new PropertyListCollection();
        PropertyListCollection setconds = this.taskprops.getCollection("setupconditions");
        if (setconds == null) {
            setconds = new PropertyListCollection();
            this.taskprops.setProperty("setupconditions", setconds);
        }
        setupprops.setProperty("collection", setconds);
        if (this.propertyDef != null && (pd = this.propertyDef.getPropertyDef("setupconditions")) != null) {
            inputs = com.labvantage.sapphire.pageelements.forms.PropertyBuilder.getCollectionStructure(pd, new HashMap());
        }
        setupprops.setProperty("inputs", inputs);
        html.append("<form style=\"display:none\" method=\"post\" id=\"task_setup_form\" name=\"task_setup_form\" action=\"rc?command=file&file=WEB-CORE/elements/richtext/collection.jsp\" target=\"task_setup_frame\">");
        html.append("<input type=\"hidden\" name=\"iframe\" value=\"").append("Y").append("\">");
        html.append("<input type=\"hidden\" name=\"propertyid\" value=\"").append("setupconditions").append("\">");
        html.append("<input type=\"hidden\" name=\"callback\" value=\"parent.").append(JS_CLASS).append(".collectionChange").append("\">");
        html.append("<textarea style=\"display:none;\" name=\"properties\">").append(setupprops.toJSONString(false)).append("</textarea>");
        html.append("</form>");
        html.append("</div>");
        html.append("</td>");
        html.append("</tr>");
        html.append("</tbody>");
        html.append("</table>");
        html.append("</td>");
        html.append("</tr>");
        html.append("</td>");
        html.append("</tr>");
        html.append("</tbody>");
        html.append("</table>");
        return html;
    }

    private StringBuffer getVariablesTab() {
        StringBuffer html = new StringBuffer();
        html.append("<div style=\"padding: 5px 5px 5px 5px;height:100%;\" id=\"taskdef_variablescontent\">");
        html.append("<iframe src=\"").append(this.browser.getBlankSrc()).append("\" id=\"task_variables_frame\" name=\"task_variables_frame\" frameborder=0 scroll=\"no\" style=\"width:100%;height:100%;\"></iframe>");
        html.append("<form style=\"display:none\" method=\"post\" id=\"task_variables_form\" name=\"task_variables_form\" action=\"rc?command=file&file=WEB-CORE/elements/workflow/taskdefvariables.jsp\" target=\"task_variables_frame\">");
        html.append("<input type=\"hidden\" name=\"viewonly\" value=\"").append(this.viewonly ? "Y" : "N").append("\">");
        html.append("<input type=\"hidden\" name=\"descendant\" value=\"").append(this.descendant ? "Y" : "N").append("\">");
        html.append("<textarea style=\"display:none;\" name=\"properties\">").append(this.taskprops.toJSONString(false)).append("</textarea>");
        html.append("</form>");
        html.append("</div>");
        return html;
    }

    private StringBuffer getDebugTab() {
        StringBuffer html = new StringBuffer();
        html.append("Readonly View Of Stored XML (use debug buttons for modifying XML):");
        html.append("<textarea id=\"task_propertyxml\" name=\"task_propertyxml\" readonly style=\"height:100%;width:100%;\">").append(this.taskData != null && this.taskData.getDataset("primary") != null && this.taskData.getDataset("primary").getRowCount() > 0 ? this.taskData.getDataset("primary").getClob(0, DATACOL, "") : "").append("</textarea>");
        return html;
    }

    private StringBuffer getWorkTab() {
        StringBuffer html = new StringBuffer();
        html.append("<div id=\"task_workcontent\" style=\"padding: 5px 5px 5px 5px;\">");
        html.append(TaskDefMaint.getWorkTab(this.taskprops, this.viewonly, this.getConnectionId(), this.pageContext, this.logger));
        html.append("</div>");
        return html;
    }

    private StringBuffer getStagesTab() {
        StringBuffer html = new StringBuffer();
        html.append("<div id=\"task_stagescontent\" style=\"padding: 5px 5px 5px 5px;\">");
        html.append(TaskDefMaint.getStagesTab(this.taskprops, this.viewonly || this.descendant, this.getConnectionId(), this.pageContext, this.logger));
        html.append("</div>");
        return html;
    }

    public StringBuffer getCategoryTab() {
        StringBuffer html = new StringBuffer();
        html.append("<div id=\"task_catcontent\" style=\"margin: 5px 5px 5px 5px;padding: 5px 5px 5px 5px;width:auto;border:solid 1px lightSteelBlue;\">");
        PropertyList pl = new PropertyList();
        pl.setProperty("checkboxcols", "6");
        pl.setProperty("readonly", this.viewonly ? "Y" : "N");
        pl.setProperty("allowadd", "N");
        pl.setProperty("customadd", "taskMaint.category.add");
        pl.setProperty("customclick", "taskMaint.category.click");
        pl.setProperty("customtablestyle", "border:none;");
        MaintDetail md = new MaintDetail(this.pageContext, this.sdiInfo, this.getConnectionId());
        md.setElementType("category");
        md.setElementProperties(pl);
        html.append(md.getHtml());
        html.append("</div>");
        return html;
    }

    public static StringBuffer getAttachmentsTab(Mode mode, SDITagInfo sdiTagInfo, boolean viewOnly, PageContext pageContext) {
        StringBuffer html = new StringBuffer();
        if (mode == Mode.ADD) {
            html.append("Please save your task before managing attachments.");
        } else {
            Files files = new Files(pageContext);
            files.setElementid("taskattachments");
            files.setSDIInfo(sdiTagInfo);
            files.setAjax(false);
            files.setViewOnly(viewOnly);
            PropertyList attProps = new PropertyList();
            attProps.setProperty(PROPERTY_SDCID, sdiTagInfo.getSdcid());
            files.setElementProperties(attProps);
            html.append(files.getHtml());
        }
        return html;
    }

    public static StringBuffer getIncludesTab(PropertyList taskprops, boolean fullViewOnly, String connectionId, TranslationProcessor tp, Browser browser, PageContext pageContext) {
        StringBuffer html = new StringBuffer();
        if (!fullViewOnly) {
            Button bt = new Button(pageContext);
            bt.setId("__btTaskPropsAddInc");
            bt.setImg("WEB-CORE/images/png/Add.png");
            bt.setTip("Add new include");
            bt.setAction("taskMaint.buttons.addInclude()");
            html.append(bt.getHtml());
            html.append("&nbsp;");
            bt = new Button(pageContext);
            bt.setId("__btTaskPropsRemoveInc");
            bt.setImg("WEB-CORE/images/png/Delete.png");
            bt.setTip("Remove selected include");
            bt.setAction("taskMaint.buttons.deleteInclude()");
            html.append(bt.getHtml());
        }
        html.append("<form id=\"includesform\" name=\"includesform\"").append(">");
        DataSet dsIncludes = new DataSet();
        dsIncludes.addColumn("includeid", 0);
        dsIncludes.addColumn("href", 0);
        PropertyListCollection includes = taskprops.getCollection("includes");
        if (includes != null && includes.size() > 0) {
            for (int i = 0; i < includes.size(); ++i) {
                int r = dsIncludes.addRow();
                PropertyList include = includes.getPropertyList(i);
                dsIncludes.setValue(r, "includeid", include.getProperty("includeid", ""));
                dsIncludes.setValue(r, "href", include.getProperty("href", ""));
            }
            DataView detail = new DataView(pageContext, "includes", dsIncludes, "", connectionId);
            detail.setElementid("task_includes_det_dataview");
            detail.setSDCId(SDCID);
            detail.getSDIInfo().setSdcid(SDCID);
            PropertyList detailProps = new PropertyList();
            detailProps.setProperty(PROPERTY_SDCID, SDCID);
            detailProps.setProperty("style", "GridWithCheckbox");
            String taskid = taskprops.getProperty("taskdefitemid", "");
            PropertyListCollection detailColumns = new PropertyListCollection();
            PropertyListCollection detailEvents = new PropertyListCollection();
            if (!fullViewOnly) {
                PropertyList detailEvent = new PropertyList();
                detailEvent.setProperty("event", "onchange");
                detailEvent.setProperty("js", "taskMaint.maintFieldChange(event,this,'task','" + taskid + "','')");
                detailEvents.add(detailEvent);
            }
            PropertyList detailColumn = new PropertyList();
            detailColumn.setProperty("columnid", "includeid");
            detailColumn.setProperty("title", "Include Id");
            detailColumn.setProperty("tip", "Include Id");
            detailColumn.setProperty("mode", !fullViewOnly ? "readonly" : "readonly");
            if (!fullViewOnly) {
                detailColumn.setProperty("events", detailEvents);
            }
            detailColumns.add(detailColumn);
            detailColumn = new PropertyList();
            detailColumn.setProperty("columnid", "href");
            detailColumn.setProperty("title", "Href");
            detailColumn.setProperty("tip", "Href");
            detailColumn.setProperty("mode", !fullViewOnly ? "input" : "readonly");
            if (!fullViewOnly) {
                detailColumn.setProperty("events", detailEvents);
            }
            detailColumns.add(detailColumn);
            detailProps.setProperty("columns", detailColumns);
            detail.setElementProperties(detailProps);
            html.append("<div").append(browser.isIE() ? " style=\"padding-top:10px;\"" : " style=\"padding: 5px 5px 5px 5px;\"").append(">");
            html.append(detail.getHtml());
            html.append("</div>");
        }
        html.append("<div id=\"includesnorows\" style=\"display:").append(includes == null || includes.size() == 0 ? "block" : "none").append(";\">").append(tp.translate("No Includes.")).append("</div>");
        html.append("<input type=\"hidden\" id=\"__includes_cols\" value=\"includeid;href\">");
        html.append("<input type=\"hidden\" id=\"lookupinclde\" onchange=\"taskMaint.buttons.addInclude_Callback(this)\">");
        html.append("</form>");
        return html;
    }

    private StringBuffer getIncludesTab() {
        StringBuffer html = new StringBuffer();
        boolean fullViewOnly = this.viewonly || this.descendant;
        html.append("<div").append(this.browser.isIE() ? "" : " style=\"padding: 5px 5px 5px 5px;\" id=\"task_includes_content\"").append(">");
        html.append(TaskDefMaint.getIncludesTab(this.taskprops, fullViewOnly, this.getConnectionId(), this.getTranslationProcessor(), this.browser, this.pageContext));
        html.append("</div>");
        return html;
    }

    public StringBuffer getAttachmentsTab() {
        StringBuffer html = new StringBuffer();
        html.append("<div id=\"task_attachment_content\" style=\"padding:5px;margin:5px;border:solid 1px lightSteelBlue;").append("").append("\">");
        html.append(TaskDefMaint.getAttachmentsTab(this.mode, this.sdiInfo, this.viewonly, this.pageContext));
        html.append("</div>");
        return html;
    }

    public static StringBuffer getWorkTab(PropertyList taskprops, boolean viewonly, String connectionId, PageContext pageContext, Logger logger) {
        StringBuffer html = new StringBuffer();
        TranslationProcessor tp = pageContext != null ? new TranslationProcessor(pageContext) : new TranslationProcessor(connectionId);
        DataSet dsFake = new DataSet();
        dsFake.addColumn("sysuserid", 0);
        dsFake.addColumn("roleid", 0);
        dsFake.addColumn("departmentid", 0);
        String user = taskprops.getProperty("sysuserid", "");
        String role = taskprops.getProperty("roleid", "");
        String department = taskprops.getProperty("departmentid", "");
        dsFake.addRow();
        String taskid = taskprops.getProperty("taskdefitemid", "");
        dsFake.setValue(0, "sysuserid", user);
        dsFake.setValue(0, "roleid", role);
        dsFake.setValue(0, "departmentid", department);
        DataView maint = new DataView(pageContext, "primary", dsFake, "", connectionId);
        maint.setElementid("task_work_dataview");
        maint.setSDCId(SDCID);
        maint.getSDIInfo().setSdcid(SDCID);
        PropertyList maintProps = new PropertyList();
        maintProps.setProperty(PROPERTY_SDCID, SDCID);
        maintProps.setProperty("style", "Form");
        maintProps.setProperty("formcols", "2");
        if (viewonly) {
            maintProps.setProperty(PROPERTY_VIEWONLY, "Y");
        }
        PropertyListCollection columns = new PropertyListCollection();
        PropertyListCollection events = new PropertyListCollection();
        PropertyList event = new PropertyList();
        event.setProperty("event", "onchange");
        event.setProperty("js", "taskMaint.maintFieldChange(event,this,'task','" + taskid + "','')");
        events.add(event);
        PropertyList column = new PropertyList();
        column.setProperty("columnid", "sysuserid");
        column.setProperty("title", tp.translate("User"));
        column.setProperty("mode", !viewonly ? "lookup" : "readonly");
        PropertyList lookup = new PropertyList();
        lookup.setProperty(PROPERTY_SDCID, "User");
        lookup.setProperty("tip", tp.translate("Lookup User"));
        column.setProperty("lookuplink", lookup);
        if (role.length() > 0 || department.length() > 0) {
            column.setProperty("disable", "Y");
        }
        column.setProperty("groupid", "Assignment");
        column.setProperty("events", events);
        columns.add(column);
        column = new PropertyList();
        column.setProperty("columnid", "roleid");
        column.setProperty("title", tp.translate("Role"));
        column.setProperty("mode", !viewonly ? "lookup" : "readonly");
        lookup = new PropertyList();
        lookup.setProperty(PROPERTY_SDCID, "Role");
        lookup.setProperty("tip", tp.translate("Lookup Role"));
        column.setProperty("lookuplink", lookup);
        if (user.length() > 0 || department.length() > 0) {
            column.setProperty("disable", "Y");
        }
        column.setProperty("groupid", "Assignment");
        column.setProperty("events", events);
        columns.add(column);
        column = new PropertyList();
        column.setProperty("columnid", "departmentid");
        column.setProperty("title", tp.translate("Department"));
        column.setProperty("mode", !viewonly ? "lookup" : "readonly");
        lookup = new PropertyList();
        lookup.setProperty(PROPERTY_SDCID, "Department");
        lookup.setProperty("tip", tp.translate("Lookup Department"));
        column.setProperty("lookuplink", lookup);
        if (role.length() > 0 || user.length() > 0) {
            column.setProperty("disable", "Y");
        }
        column.setProperty("groupid", "Assignment");
        column.setProperty("events", events);
        columns.add(column);
        maintProps.setProperty("columns", columns);
        maint.setElementProperties(maintProps);
        html.append(maint.getHtml());
        return html;
    }

    public static StringBuffer getStagesTab(PropertyList taskprops, boolean viewonly, String connectionId, PageContext pageContext, Logger logger) {
        StringBuffer html = new StringBuffer();
        PropertyListCollection stages = taskprops.getCollection("stages");
        Image im = new Image(pageContext);
        im.setImageId("Delete");
        im.setDimensions(16, 16);
        html.append("<ul id=\"taskdef_stages\">");
        if (stages != null && stages.size() > 0 && stages != null && stages.size() > 0) {
            for (int i = 0; i < stages.size(); ++i) {
                html.append("<li class=\"ui-state-default\" stageid=\"").append(stages.getPropertyList(i).getProperty("stageid")).append("\" stagetext=\"").append(stages.getPropertyList(i).getProperty("text")).append("\">");
                if (!viewonly) {
                    html.append("<span class=\"ui-icon ui-icon-arrowthick-2-n-s\"></span>");
                }
                html.append("<font>").append(stages.getPropertyList(i).getProperty("text")).append("</font>");
                if (!viewonly) {
                    html.append("<div onclick=\"").append(JS_CLASS).append(".stages.remove(this.parentNode);").append("\">").append(im.getHtml()).append("</div>");
                }
                html.append("</li>");
            }
        }
        html.append("</ul>");
        html.append("<ul style=\"display:none;\" id=\"taskdef_stages_add\">");
        html.append("<li  class=\"ui-state-default\" stageid=\"\" stagetext=\"\"><span class=\"ui-icon ui-icon-arrowthick-2-n-s\"></span><font>").append("").append("</font><div onclick=\"").append(JS_CLASS).append(".stages.remove(this.parentNode);").append("\">").append(im.getHtml()).append("</div></li>");
        html.append("</ul>");
        html.append("<div id=\"taskdef_stages_no\" style=\"display:").append(stages != null && stages.size() > 0 ? "none" : "block").append(";\">");
        html.append(new TranslationProcessor(pageContext).translate("No stages defined."));
        html.append("</div>");
        return html;
    }

    private StringBuffer getStepsTab() {
        StringBuffer html = new StringBuffer();
        html.append("<div id=\"taskdef_frame_content\" style=\"background-color:#C3DAF9;padding:0;margin:0;").append(this.browser.isIE() ? "" : "display:inline;").append("\">");
        html.append("<iframe name=\"taskdef_frame\" id=\"taskdef_frame\" src=\"").append(this.browser.getBlankSrc()).append("\" scrolling=\"no\" style=\"width:100%;height:100%;\" frameborder=0></iframe>");
        html.append("</div>");
        if (this.browser.isIE() && this.requestContext.getProperty("html5").equalsIgnoreCase("Y")) {
            html.append("<script>");
            html.append("if (__sizeAdjust){__sizeAdjust.addElement(taskdef_frame_content,'(document.body.clientHeight - taskdef_frame_content.getBoundingClientRect().top - 5)');}");
            html.append("</script>");
        }
        html.append("<form name=\"taskdef_form\" id=\"taskdef_form\" action=\"rc?command=file&file=WEB-CORE/elements/workflow/taskdefpainter.jsp\" method=POST target=\"taskdef_frame\" style=\"display:none;\">");
        html.append("<textarea name=\"").append("properties").append("\" id=\"").append("properties").append("\">").append("").append("</textarea>");
        html.append("<input type=\"hidden\" name=\"").append(PROPERTY_SDCID).append("\" id=\"").append(PROPERTY_SDCID).append("\" value=\"").append(this.sdcid).append("\">");
        html.append("<input type=\"hidden\" name=\"").append(PROPERTY_KEYID1).append("\" id=\"").append(PROPERTY_KEYID1).append("\" value=\"").append(this.keyid1).append("\">");
        html.append("<input type=\"hidden\" name=\"").append(PROPERTY_KEYID2).append("\" id=\"").append(PROPERTY_KEYID2).append("\" value=\"").append(this.keyid2).append("\">");
        html.append("<input type=\"hidden\" name=\"").append(PROPERTY_KEYID3).append("\" id=\"").append(PROPERTY_KEYID3).append("\" value=\"").append(this.keyid3).append("\">");
        html.append("<input type=\"hidden\" name=\"").append("embedded").append("\" id=\"").append("embedded").append("\" value=\"").append("Y").append("\">");
        html.append("<input type=\"hidden\" name=\"").append("propertycontainer").append("\" id=\"").append("propertycontainer").append("\" value=\"").append("sapphire.page.getTop().").append(JS_CLASS).append(".properties").append("\">");
        html.append("<input type=\"hidden\" name=\"").append(PROPERTY_VIEWONLY).append("\" id=\"").append(PROPERTY_VIEWONLY).append("\" value=\"").append(this.viewonly ? "Y" : "N").append("\">");
        html.append("<input type=\"hidden\" name=\"").append("descendant").append("\" id=\"").append("descendant").append("\" value=\"").append(this.descendant ? "Y" : "N").append("\">");
        html.append("<input type=\"hidden\" name=\"").append("toolbox").append("\" id=\"").append("toolbox").append("\" value=\"").append(this.pageContext != null && this.pageContext.getRequest().getParameter("toolbox") != null ? this.pageContext.getRequest().getParameter("toolbox") : "").append("\">");
        html.append("<input type=\"hidden\" name=\"").append("renderhtml5").append("\" id=\"").append("renderhtml5").append("\" value=\"N\">");
        html.append("</form>");
        return html;
    }

    private Tab getTab(Tabs currentTab) {
        Tab tab = new Tab();
        tab.setPageContext(this.pageContext);
        StringBuffer html = new StringBuffer();
        switch (currentTab) {
            case TASK: {
                html.append(this.getTaskTab());
                break;
            }
            case TASKIO: {
                html.append(this.getIOTab());
                break;
            }
            case STAGES: {
                html.append(this.getStagesTab());
                break;
            }
            case STEPS: {
                html.append(this.getStepsTab());
                break;
            }
            case VARIABLES: {
                html.append(this.getVariablesTab());
                break;
            }
            case WORK: {
                html.append(this.getWorkTab());
                break;
            }
            case CATEGORIES: {
                html.append(this.getCategoryTab());
                break;
            }
            case ATTACHMENTS: {
                html.append(this.getAttachmentsTab());
                break;
            }
            case INCLUDES: {
                html.append(this.getIncludesTab());
                break;
            }
            case DEBUG: {
                html.append(this.getDebugTab());
            }
        }
        tab.setContent(html.toString());
        tab.setId("task_tab_" + currentTab.toString().toLowerCase());
        if (currentTab == Tabs.TASK) {
            if (this.keyid1.length() > 0) {
                tab.setText(this.keyid1 + " (" + this.keyid2 + " - " + this.keyid3 + ") " + currentTab.getTitle());
            } else {
                tab.setText("New Task " + currentTab.getTitle());
            }
        } else {
            tab.setText(currentTab.getTitle());
        }
        tab.setExpandable("false");
        tab.setExpanded("true");
        tab.setTip(currentTab.getTitle());
        tab.setAction("taskMaint.changeTab('" + currentTab.toString().toLowerCase() + "')");
        tab.setBodyheight("100%");
        return tab;
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        TranslationProcessor tp = this.getTranslationProcessor();
        CMTPolicy cmtPolicy = CMTPolicy.getPolicy(this.getConnectionid(), this.sdcid);
        this.isChangeControlled = "Y".equals(cmtPolicy.getChangeControlledFlag());
        if (this.taskprops != null) {
            this.pageTitle = this.getTranslationProcessor().translate("Edit Task") + " " + this.keyid1 + " (" + this.keyid2 + "-" + this.keyid3 + ")";
            if (this.mode == Mode.EDIT) {
                // empty if block
            }
            html.append(this.getScriptAndStyle());
            html.append("<div id=\"task_root_container\" style=\"min-width:800px;height:100%;width:100%;overflow:").append("hidden").append(";\">");
            String height = "44px";
            html.append("<div id=\"task_table\" style=\"background:#FFFFFF;border-bottom:1px solid #000000;height:" + height + ";width:100%;overflow-y:").append("hidden").append(";\">");
            html.append(this.getToolBar());
            html.append("</div>");
            html.append("<div id=\"taskdefmaint_tabcontainer\" style=\"overflow:auto;padding: 5px 5px 5px 5px;height:auto;width:auto;position:absolute;top:" + height + ";left:0;bottom:0;right:0;\">");
            TabGroup tabGroup = new TabGroup();
            tabGroup.setPageContext(this.pageContext);
            tabGroup.setMultiTab(true);
            tabGroup.setId("task_tabgroup");
            tabGroup.setBodyheight("100%");
            tabGroup.setAppearance("modern");
            if (this.mode != Mode.ADD && !this.locked) {
                tabGroup.setContext("LV_TaskDef_" + this.keyid1 + "_" + this.keyid2 + "_" + this.keyid3);
            }
            if (this.selectedTab > -1) {
                tabGroup.setSelectedTab(this.selectedTab);
            }
            for (Tabs tab : Tabs.values()) {
                if (tab == Tabs.DEBUG && !this.devMode && !this.debug) continue;
                tab.title = tp.translate(tab.title);
                tabGroup.setTab(this.getTab(tab));
            }
            tabGroup.setUseChangeTab(true);
            html.append(tabGroup.getHtml());
            html.append("</div>");
            html.append("</div>");
            this.buildReturnForm(html);
            html.append(this.getEndScript(this.taskprops, this.sdcid, this.keyid1, this.keyid2, this.keyid3, this.viewonly, this.descendant, this.locked, this.rsetid, this.changeMade, this.mode));
        } else {
            html.append("<font color=\"red\">").append(this.getTranslationProcessor().translate("Could not load properties for task.")).append("</font>");
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
        TASK("Details"),
        STEPS("Task Design"),
        VARIABLES("Task Variables"),
        TASKIO("Appearance"),
        STAGES("Stages"),
        WORK("Assignment"),
        CATEGORIES("Categories"),
        ATTACHMENTS("Attachments"),
        INCLUDES("Includes"),
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

