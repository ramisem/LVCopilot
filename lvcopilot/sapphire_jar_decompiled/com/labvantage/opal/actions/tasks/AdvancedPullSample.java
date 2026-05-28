/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions.tasks;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.StabilityUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.modules.wap.activity.WAPConstants;
import com.labvantage.sapphire.scheduler.BaseScheduleTask;
import com.labvantage.sapphire.scheduler.ScheduleEvent;
import com.labvantage.sapphire.stability.InventoryCalculation;
import com.labvantage.sapphire.stability.PlanItem;
import com.labvantage.sapphire.stability.ScheduleGrid;
import com.labvantage.sapphire.stability.task.GridTask;
import com.labvantage.sapphire.stability.task.GridTaskStatus;
import com.labvantage.sapphire.stability.task.HasDetails;
import com.labvantage.sapphire.stability.task.PullAmount;
import com.labvantage.sapphire.stability.task.WorkOrderCallbackable;
import com.labvantage.sapphire.util.graceperiod.GRCPeriodUtil;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.M18NUtil;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AdvancedPullSample
extends BaseScheduleTask
implements GridTask,
GridTaskStatus,
PullAmount,
WorkOrderCallbackable,
HasDetails,
WAPConstants {
    private DataSet allSamples;
    private DataSet allSampleSpecConditions;
    private DataSet allWorkorders;
    private static final String SUMMARY = "Summary";
    private static final String PULLAMOUNTS = "Pull Amounts";
    private static final String FULL = "Summary & Pull Amounts";
    private static final String WORKORDER = "Workorder Info";
    SimpleDateFormat sdf = new SimpleDateFormat();
    public static String LOG_NAME = "AdvancedPullSample";
    private int copies = 1;
    private String quantity = "";
    private String units = "";
    private String studyid = "";
    private String productid = "";
    private String productversionid = "";
    private String controlsubstanceflag = "";
    private String cocrequiredflag = "";
    private String batchid = "";
    private String condition = "";
    private String conditionid = "";
    private String containerTypeId = "";
    private String timerule = "";
    private double containerSize = 0.0;
    private String containerSizeUnit = "";
    private String partialPullFlag = "";
    private PropertyList createSdi;
    DataSet testDataSetReuse;
    DataSet testDataSetNoReuse;
    private boolean timeZeroTask;
    private String workorderqty = "";
    private String workorderqtyunit = "";
    private String securityUser = "";
    private String securityDepartment = "";
    boolean sdcDeptSecurityEnabled = false;
    int calculatedSampleCount = 0;
    private static final String MODEBOTH = "Both";
    private static final String MODEWOONLY = "WorkorderOnly";
    private static final String MODESAMPLEONLY = "SampleOnly";
    boolean createUniqSamplePerContr = false;
    private HashMap testUserSequence = new HashMap();
    private String preWOCompleteTraceogId = "";

    @Override
    public final void execute() {
        this.logger.info("Start AdvancedPullSample Task");
        try {
            this.createSdi = this.scheduleProperties.getPropertyListNotNull("createsdi");
            String deferSampleCreation = this.createSdi.getProperty("defersamplecreation", "");
            this.createUniqSamplePerContr = "Y".equalsIgnoreCase(this.createSdi.getProperty("createuniquesamplepercontainer", ""));
            long start = System.currentTimeMillis();
            int events = 1;
            PropertyList workorder = this.scheduleProperties.getPropertyListNotNull("workorder");
            if (this.scheduleEvents != null) {
                events = this.scheduleEvents.size();
                this.logger.info("Event List:" + this.scheduleEvents.toString());
                this.logger.info("Processing " + events + " Events with properties:" + this.scheduleProperties.toString());
                for (int i = 0; i < events; ++i) {
                    ScheduleEvent event = (ScheduleEvent)this.scheduleEvents.get(i);
                    String scheduleplanid = event.getSchedulePlanid();
                    String scheduleplanitemid = event.getSchedulePlanitemid();
                    this.timeZeroTask = this.isTimeZeroTask(event.getSchedulePlanid(), event.getSchedulePlanitemid());
                    String keyidList = "";
                    DataSet ds = this.loadTestProperties(scheduleplanid, scheduleplanitemid, this.timeZeroTask);
                    HashMap<String, String> hmFilter = new HashMap<String, String>();
                    hmFilter.put("reusecontainerflag", "Y");
                    this.testDataSetReuse = ds.getFilteredDataSet(hmFilter);
                    hmFilter.clear();
                    hmFilter.put("reusecontainerflag", "N");
                    this.testDataSetNoReuse = ds.getFilteredDataSet(hmFilter);
                    if (!deferSampleCreation.equalsIgnoreCase("Y") && !this.isSampleLogged(scheduleplanid, scheduleplanitemid)) {
                        keyidList = this.createSamples(scheduleplanid, scheduleplanitemid, event.getEventDt(), this.testDataSetNoReuse, false, "");
                        String workOrderId = "";
                        if ("Y".equals(workorder.getProperty("create", "")) && keyidList.length() > 0) {
                            workOrderId = this.createWO(scheduleplanid, scheduleplanitemid, event.getEventDt(), "Pull", null, this.testDataSetNoReuse, false);
                        }
                        if (workOrderId != null && workOrderId.length() > 0) {
                            PropertyList editProps = new PropertyList();
                            editProps.setProperty("sdcid", "Sample");
                            editProps.setProperty("keyid1", keyidList);
                            editProps.setProperty("workorderid", workOrderId);
                            this.getActionProcessor().processAction("EditSDI", "1", editProps);
                        }
                        this.handleContainerReuseSamples(scheduleplanid, scheduleplanitemid, event.getEventDt(), this.testDataSetReuse, MODEBOTH, false, "");
                    }
                    if (!"Y".equalsIgnoreCase(deferSampleCreation)) continue;
                    if (this.testDataSetNoReuse != null && this.testDataSetNoReuse.size() > 0) {
                        this.createWO(scheduleplanid, scheduleplanitemid, event.getEventDt(), "Pull", null, this.testDataSetNoReuse, false);
                    }
                    if (this.testDataSetReuse == null || this.testDataSetReuse.size() <= 0) continue;
                    this.handleContainerReuseSamples(scheduleplanid, scheduleplanitemid, event.getEventDt(), this.testDataSetReuse, MODEWOONLY, false, "");
                }
            }
            this.logger.info("End " + this.getLogName() + " Task. Took " + (System.currentTimeMillis() - start) + "ms");
        }
        catch (Exception e) {
            this.logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private String createSamples(String scheduleplanid, String scheduleplanitemid, Calendar eventdate, DataSet tests, boolean containerReuseSample, String workOrderId) throws SapphireException {
        String value;
        boolean multipleReuseSample;
        DataSet studyDS;
        SDCProcessor sdcProc = this.getSDCProcessor();
        if (!this.sdcDeptSecurityEnabled && AdvancedPullSample.isDeptSecurityEnabled(sdcProc, "StudySDC")) {
            this.sdcDeptSecurityEnabled = true;
        }
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT sc.conditionlabel, sc.scheduleconditionid, sr.schedulerulelabel, sr.schedulerule FROM  scheduletimerule sr, schedulecondition sc, scheduleplanitem si  WHERE si.scheduleplanid = " + safeSQL.addVar(scheduleplanid) + " AND si.scheduleplanitemid = " + safeSQL.addVar(scheduleplanitemid) + " AND sc.scheduleplanid = si.scheduleplanid AND sr.scheduleplanid = si.scheduleplanid  AND sc.scheduleconditionid = si.scheduleconditionid  AND sr.scheduletimeruleid = si.scheduletimeruleid";
        QueryProcessor qp = this.getQueryProcessor();
        DataSet labelds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (labelds.getRowCount() > 0) {
            this.condition = labelds.getString(0, "conditionlabel", "");
            this.conditionid = labelds.getString(0, "scheduleconditionid", "");
            String string = this.timerule = labelds.getString(0, "schedulerulelabel") != null ? labelds.getString(0, "schedulerulelabel") : labelds.getString(0, "schedulerule", "");
        }
        if ((studyDS = this.loadStudy(scheduleplanid, scheduleplanitemid)) != null && studyDS.getRowCount() > 0) {
            this.studyid = studyDS.getString(0, "studyid");
            this.productid = studyDS.getString(0, "s_productid", "");
            this.productversionid = studyDS.getString(0, "s_productversionid", "");
            this.controlsubstanceflag = studyDS.getString(0, "controlsubstanceflag", "");
            this.cocrequiredflag = studyDS.getString(0, "cocrequiredflag", "");
            this.batchid = studyDS.getString(0, "s_batchid", "");
            this.partialPullFlag = studyDS.getString(0, "partialpullflag", "");
            if (this.sdcDeptSecurityEnabled) {
                this.securityUser = studyDS.getString(0, "securityuser");
                this.securityDepartment = studyDS.getString(0, "securitydepartment");
            }
            this.containerTypeId = studyDS.getString(0, "containertypeid", "");
            if (this.containerTypeId.length() > 0) {
                this.database.createPreparedResultSet("SELECT sizevalue, sizeunits FROM containertype WHERE containertypeid=?", new String[]{this.containerTypeId});
                if (this.database.getNext()) {
                    this.containerSizeUnit = this.database.getString("sizeunits");
                    try {
                        this.containerSize = Double.parseDouble(this.database.getString("sizevalue"));
                    }
                    catch (NumberFormatException e) {
                        this.logger.info("Invalid container size found for container type :" + this.containerTypeId);
                    }
                }
            }
        }
        String keyidList = "";
        DataSet containerGroupDS = null;
        PropertyList pullamount = this.scheduleProperties.getPropertyListNotNull("pullamount");
        this.quantity = pullamount.getProperty("quantity", "");
        this.units = pullamount.getProperty("units", "");
        boolean createDistinctLabSamples = "Y".equalsIgnoreCase(this.createSdi.getProperty("createdistinctlaboratorysamples", ""));
        if (tests == null || tests.size() == 0) {
            return "";
        }
        int reuseTest_numtrepeats = 1;
        for (int t = 0; t < tests.getRowCount(); ++t) {
            if (containerReuseSample) {
                reuseTest_numtrepeats = tests.getBigDecimal(0, "numrepeats", new BigDecimal(1)).intValue();
            }
            if (tests.getString(t, "departmentid", "").length() != 0 || this.securityDepartment == null || this.securityDepartment.length() <= 0) continue;
            tests.setString(t, "departmentid", this.securityDepartment);
        }
        boolean bl = multipleReuseSample = "Y".equalsIgnoreCase(this.createSdi.getProperty("createmultiplereusetestingsample", "Y")) && containerReuseSample;
        if (this.quantity.length() > 0) {
            containerGroupDS = new DataSet();
            containerGroupDS.addColumn("containergroupid", 0);
            containerGroupDS.addColumn("departmentid", 0);
            containerGroupDS.addColumn("quantity", 1);
            containerGroupDS.addColumn("quantityunit", 0);
            containerGroupDS.addColumn("teststring", 0);
            if (createDistinctLabSamples) {
                tests.sort("departmentid");
                ArrayList<DataSet> deptGroups = tests.getGroupedDataSets("departmentid");
                for (int group = 0; group < deptGroups.size(); ++group) {
                    DataSet deptTests = deptGroups.get(group);
                    int rownum = containerGroupDS.addRow();
                    String departmentid = deptTests.getString(0, "departmentid");
                    containerGroupDS.setValue(rownum, "teststring", this.buildRepeatTestMethodString(deptTests, multipleReuseSample));
                    containerGroupDS.setValue(rownum, "containergroupid", "" + (group + 1));
                    containerGroupDS.setValue(rownum, "departmentid", departmentid);
                    containerGroupDS.setValue(rownum, "quantity", this.quantity);
                    containerGroupDS.setValue(rownum, "quantityunit", this.units);
                }
            } else {
                int rownum = containerGroupDS.addRow();
                containerGroupDS.setValue(rownum, "teststring", this.buildRepeatTestMethodString(tests, multipleReuseSample));
                containerGroupDS.setValue(rownum, "containergroupid", "1");
                containerGroupDS.setValue(rownum, "departmentid", "(null)");
                containerGroupDS.setValue(rownum, "quantity", this.quantity);
                containerGroupDS.setValue(rownum, "quantityunit", this.units);
            }
        } else {
            InventoryCalculation invCalc = new InventoryCalculation(this.getConnectionId(), this.getConnectionProcessor().getSapphireConnection());
            StringBuffer log = new StringBuffer();
            containerGroupDS = invCalc.getContainerGroupData(tests, "X".equalsIgnoreCase(this.partialPullFlag) ? "Y" : this.partialPullFlag, this.containerSize, this.containerSizeUnit, log);
            if (multipleReuseSample && reuseTest_numtrepeats > 1) {
                String testString = containerGroupDS.getString(0, "teststring", "");
                if (containerGroupDS.getRowCount() == 1 && testString.contains(";")) {
                    containerGroupDS.setString(0, "teststring", testString.substring(0, testString.indexOf(";")));
                } else {
                    for (int g = containerGroupDS.getRowCount() - 1; g > 0; --g) {
                        if (!testString.equals(containerGroupDS.getString(g, "teststring"))) continue;
                        containerGroupDS.remove(g);
                    }
                }
            }
        }
        this.createUniqSamplePerContr = "Y".equalsIgnoreCase(this.createSdi.getProperty("createuniquesamplepercontainer", ""));
        int sampleCount = 0;
        String copiesStr = this.createSdi.getProperty("copies", "");
        if (copiesStr.length() > 0) {
            try {
                this.copies = Integer.parseInt(copiesStr);
            }
            catch (NumberFormatException nfe) {
                this.copies = 0;
            }
        } else {
            this.copies = 0;
        }
        sampleCount = containerReuseSample ? (multipleReuseSample ? reuseTest_numtrepeats : 1) : this.copies;
        int origSampleCount = sampleCount;
        if (this.createUniqSamplePerContr && !containerReuseSample && !createDistinctLabSamples) {
            sampleCount *= containerGroupDS.getRowCount();
        }
        HashMap<String, StringBuffer> valuemap = new HashMap<String, StringBuffer>();
        PropertyList props = new PropertyList();
        String sdcid = "Sample";
        boolean sampleSDCDeptSecEnabled = false;
        props.put("sdcid", sdcid);
        props.put("keyid1", "(auto)");
        if (this.createSdi.getProperty("templateid", "").length() > 0) {
            props.put("templateid", this.createSdi.getProperty("templateid"));
        }
        PropertyListCollection columns = this.createSdi.getCollection("columnvalues");
        ArrayList<String> overiddenColumns = new ArrayList<String>();
        DataSet columnsDS = this.getSDCProcessor().getColumnData("Sample");
        for (int copy = 0; copy < sampleCount; ++copy) {
            String colid;
            int col;
            if (columns != null && columns.size() > 0) {
                for (col = 0; col < columns.size(); ++col) {
                    colid = columns.getPropertyList(col).getProperty("columnid");
                    value = columns.getPropertyList(col).getProperty("value");
                    String overridetemplate = columns.getPropertyList(col).getProperty("overridetemplate");
                    if (this.createSdi.getProperty("templateid").length() != 0 && overridetemplate.length() != 0 && !"Y".equals(overridetemplate)) continue;
                    if (!valuemap.containsKey(colid)) {
                        valuemap.put(colid, new StringBuffer());
                        overiddenColumns.add(colid);
                    }
                    ((StringBuffer)valuemap.get(colid)).append(";" + value);
                }
            }
            for (col = 0; col < columnsDS.getRowCount(); ++col) {
                colid = columnsDS.getString(col, "columnid");
                if (overiddenColumns.contains(colid)) continue;
                value = "";
                if (colid.equals("conditionlabel")) {
                    value = this.condition;
                } else if (colid.equals("schedulerulelabel")) {
                    value = containerReuseSample ? "Multiple" : this.timerule;
                } else if (colid.equals("eventplan")) {
                    value = scheduleplanid;
                } else if (colid.equals("eventplanitem")) {
                    value = scheduleplanitemid;
                } else if (colid.equals("eventdt")) {
                    if (this.connectionInfo != null) {
                        M18NUtil m18n = new M18NUtil(this.connectionInfo);
                        value = m18n.format(eventdate);
                    } else {
                        value = this.sdf.format(eventdate.getTime());
                    }
                } else if (colid.equals("studyid")) {
                    value = this.studyid;
                } else if (colid.equals("productid")) {
                    value = this.productid;
                } else if (colid.equals("productversionid")) {
                    value = this.productversionid;
                } else if (colid.equals("controlsubstanceflag")) {
                    value = this.controlsubstanceflag;
                } else if (colid.equals("cocrequiredflag")) {
                    value = this.cocrequiredflag;
                } else if (colid.equals("batchid")) {
                    value = this.batchid;
                } else if (colid.equals("securityuser")) {
                    value = this.securityUser;
                } else if (colid.equals("securitydepartment")) {
                    value = this.securityDepartment;
                    sampleSDCDeptSecEnabled = true;
                } else {
                    value = this.scheduleProperties.getProperty(colid);
                }
                if (value == null || value.length() <= 0) continue;
                if (valuemap.get(colid) == null) {
                    valuemap.put(colid, new StringBuffer(value));
                    continue;
                }
                ((StringBuffer)valuemap.get(colid)).append(";" + value);
            }
        }
        if (sampleCount > 0) {
            this.calculatedSampleCount = sampleCount;
            this.addCustomSampleColumns(scheduleplanid, scheduleplanitemid, this.conditionid, studyDS, props);
            this.preAddStabilitySample(scheduleplanid, scheduleplanitemid);
            Set keySet = valuemap.keySet();
            if (keySet.size() > 0) {
                for (String colid : keySet) {
                    value = ((StringBuffer)valuemap.get(colid)).indexOf(";") == 0 ? ((StringBuffer)valuemap.get(colid)).substring(1).toString() : ((StringBuffer)valuemap.get(colid)).toString();
                    props.put(colid, value);
                }
            }
            if (workOrderId != null && workOrderId.length() > 0) {
                props.put("workorderid", "" + workOrderId);
            }
            if (createDistinctLabSamples && !containerReuseSample) {
                keyidList = this.createDistinctLabSamples(scheduleplanid, scheduleplanitemid, sdcid, props, containerGroupDS, sampleCount, sampleSDCDeptSecEnabled);
            } else {
                String departmentid;
                if (createDistinctLabSamples && containerReuseSample && (departmentid = containerGroupDS.getValue(0, "departmentid")).length() > 0) {
                    props.put("securitydepartment", departmentid);
                }
                keyidList = this.createTaskSamples(scheduleplanid, scheduleplanitemid, sdcid, props, containerGroupDS, sampleCount, origSampleCount, containerReuseSample);
            }
        }
        this.postAddStabilitySample(keyidList, scheduleplanid, scheduleplanitemid);
        return keyidList;
    }

    private String buildRepeatTestMethodString(DataSet tests, boolean multiReuseSample) {
        StringBuffer workItemList = new StringBuffer();
        for (int wi = 0; wi < tests.getRowCount(); ++wi) {
            int repeats = multiReuseSample ? 1 : tests.getInt(wi, "numrepeats", 1);
            String workItemId = tests.getValue(wi, "workitemid");
            for (int r = 0; r < repeats; ++r) {
                workItemList.append(";").append(workItemId);
            }
        }
        return workItemList.substring(1);
    }

    protected DataSet loadStudy(String schedulePlanId, String schedulePlanItemId) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT study.*, s_product.controlsubstanceflag, s_product.cocrequiredflag ";
        sql = sql + " FROM study LEFT OUTER JOIN s_product ON study.s_productid = s_product.s_productid AND study.s_productversionid = s_product.s_productversionid ";
        sql = sql + " WHERE study.studyid IN ( SELECT ss.studyid from scheduleplan sp, study_scheduleplan ss where sp.scheduleplanid = " + safeSQL.addVar(schedulePlanId) + " AND sp.scheduleplanid = ss.scheduleplanid )";
        DataSet studyDS = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        return studyDS;
    }

    private String createTaskSamples(String scheduleplanid, String scheduleplanitemid, String sdcid, PropertyList addSDIProps, DataSet containerGroupDS, int sampleCount, int origSampleCount, boolean containerResuseSample) {
        String keyidList = "";
        try {
            PropertyListCollection specs;
            ActionProcessor ap = this.getActionProcessor();
            if (OpalUtil.isNotEmpty(this.preWOCompleteTraceogId)) {
                StringBuffer traceLogIds = new StringBuffer();
                for (int w = 0; w < sampleCount; ++w) {
                    traceLogIds.append(";").append(this.preWOCompleteTraceogId);
                }
                addSDIProps.put("tracelogid", traceLogIds.substring(1));
            }
            addSDIProps.put("copies", "" + sampleCount);
            ap.processAction("AddSDI", "1", addSDIProps);
            keyidList = (String)addSDIProps.get("newkeyid1");
            String[] keyids = StringUtil.split(keyidList, ";");
            double containercount = 0.0;
            DataSet dsAddSDIWorkItem = new DataSet();
            int k = 0;
            while (k < keyids.length) {
                for (int c = 0; c < containerGroupDS.getRowCount(); ++c) {
                    String departmentid = containerGroupDS.getString(c, "departmentid");
                    double noofcontainers = containerGroupDS.getDouble(c, "quantity", 0.0);
                    noofcontainers = (double)Math.round(noofcontainers * 100.0) / 100.0;
                    String containerGroupWorkItems = containerGroupDS.getString(c, "teststring", "");
                    containercount += noofcontainers;
                    this.workorderqtyunit = containerGroupDS.getString(c, "quantityunit", "");
                    String strNoofcontainers = "(Containers)".equalsIgnoreCase(this.workorderqtyunit) ? "" + new Double(noofcontainers).intValue() : "" + noofcontainers;
                    int sc = 1;
                    if (this.createUniqSamplePerContr && !containerResuseSample) {
                        sc = origSampleCount;
                    }
                    while (sc > 0) {
                        String sampleid = keyids[k];
                        if (!this.timeZeroTask) {
                            this.setCrossSampleRelations(this.getQueryProcessor(), ap, sampleid);
                        }
                        --sc;
                        if (this.createUniqSamplePerContr && !containerResuseSample) {
                            ++k;
                        }
                        PropertyList trackitemProps = new PropertyList();
                        trackitemProps.setProperty("sdcid", "Sample");
                        trackitemProps.setProperty("keyid1", sampleid);
                        trackitemProps.setProperty("quantity", strNoofcontainers);
                        trackitemProps.setProperty("quantitytype", "(Containers)".equalsIgnoreCase(this.workorderqtyunit) || "".equalsIgnoreCase(this.workorderqtyunit) ? "C" : "U");
                        trackitemProps.setProperty("quantityunit", "(Containers)".equalsIgnoreCase(this.workorderqtyunit) || "".equalsIgnoreCase(this.workorderqtyunit) ? "" : this.workorderqtyunit);
                        trackitemProps.setProperty("numoftrackitems", "1");
                        trackitemProps.setProperty("ownerdepartmentid", departmentid);
                        if (OpalUtil.isNotEmpty(this.preWOCompleteTraceogId)) {
                            trackitemProps.setProperty("tracelogid", this.preWOCompleteTraceogId);
                        }
                        ap.processAction("AddTrackItem", "1", trackitemProps);
                        String trackItemid = (String)trackitemProps.get("newkeyid1");
                        String[] workitems = StringUtil.split(containerGroupWorkItems, ";");
                        StringBuffer addWorkitems = new StringBuffer();
                        for (int w = 0; w < workitems.length; ++w) {
                            if (workitems[w] == null || workitems[w].length() <= 0) continue;
                            addWorkitems.append(";" + workitems[w]);
                        }
                        if (addWorkitems.length() <= 1) continue;
                        String[] wiArray = StringUtil.split(addWorkitems.substring(1), ";");
                        for (int w = 0; w < wiArray.length; ++w) {
                            int find;
                            int r = dsAddSDIWorkItem.addRow();
                            String workitemVersion = "";
                            if (containerResuseSample) {
                                find = this.testDataSetReuse.findRow("workitemid", wiArray[w]);
                                if (find > -1) {
                                    workitemVersion = this.testDataSetReuse.getValue(find, "workitemversionid");
                                }
                            } else {
                                find = this.testDataSetNoReuse.findRow("workitemid", wiArray[w]);
                                if (find > -1) {
                                    workitemVersion = this.testDataSetNoReuse.getValue(find, "workitemversionid");
                                }
                            }
                            dsAddSDIWorkItem.setString(r, "keyid1", sampleid);
                            dsAddSDIWorkItem.setString(r, "workitemid", wiArray[w]);
                            dsAddSDIWorkItem.setString(r, "workitemversionid", workitemVersion);
                            dsAddSDIWorkItem.setString(r, "trackitemid", trackItemid);
                            dsAddSDIWorkItem.setString(r, "scheduleplanid", scheduleplanid);
                            dsAddSDIWorkItem.setString(r, "scheduleplanitemid", scheduleplanitemid);
                            dsAddSDIWorkItem.setString(r, "applyworkitem", "Y");
                            dsAddSDIWorkItem.setString(r, "propsmatchtestmethodorder", "");
                        }
                    }
                }
                if (this.createUniqSamplePerContr && !containerResuseSample) continue;
                ++k;
            }
            if (dsAddSDIWorkItem.getRowCount() > 0) {
                dsAddSDIWorkItem.sort("keyid1");
                ArrayList<DataSet> sampleGrps = dsAddSDIWorkItem.getGroupedDataSets("keyid1");
                for (int g = 0; g < sampleGrps.size(); ++g) {
                    DataSet ds = sampleGrps.get(g);
                    this.setWorkItemListSequence(ds);
                }
                HashMap<String, String> workitemProps = new HashMap<String, String>();
                workitemProps.put("sdcid", sdcid);
                workitemProps.put("keyid1", dsAddSDIWorkItem.getColumnValues("keyid1", ";"));
                workitemProps.put("workitemid", dsAddSDIWorkItem.getColumnValues("workitemid", ";"));
                workitemProps.put("workitemversionid", dsAddSDIWorkItem.getColumnValues("workitemversionid", ";"));
                workitemProps.put("trackitemid", dsAddSDIWorkItem.getColumnValues("trackitemid", ";"));
                workitemProps.put("scheduleplanid", dsAddSDIWorkItem.getColumnValues("scheduleplanid", ";"));
                workitemProps.put("scheduleplanitemid", dsAddSDIWorkItem.getColumnValues("scheduleplanitemid", ";"));
                workitemProps.put("applyworkitem", dsAddSDIWorkItem.getColumnValues("applyworkitem", ";"));
                workitemProps.put("propsmatch", "Y");
                if (OpalUtil.isNotEmpty(this.preWOCompleteTraceogId)) {
                    StringBuffer traceLogIds = new StringBuffer();
                    for (int w = 0; w < dsAddSDIWorkItem.getRowCount(); ++w) {
                        traceLogIds.append(";").append(this.preWOCompleteTraceogId);
                    }
                    workitemProps.put("tracelogid", traceLogIds.substring(1));
                }
                workitemProps.put("propsmatchtestmethodorder", dsAddSDIWorkItem.getColumnValues("propsmatchtestmethodorder", ";"));
                ap.processAction("AddSDIWorkItem", "1", workitemProps);
            }
            if ((specs = this.createSdi.getCollectionNotNull("Specs")) != null && specs.size() > 0 && keyidList.length() > 0) {
                this.applySpec(keyidList, specs, scheduleplanid, scheduleplanitemid, sdcid);
            }
            this.workorderqty = "(Containers)".equalsIgnoreCase(this.workorderqtyunit) ? "" + new Double(containercount).intValue() : "" + containercount;
        }
        catch (Exception ae) {
            this.logger.error(ae.getMessage(), ae);
            throw new RuntimeException(ae);
        }
        return keyidList;
    }

    private void setWorkItemListSequence(DataSet ds) {
        for (int k = 0; k < ds.getRowCount(); ++k) {
            ds.setNumber(k, "usersequence", (String)this.testUserSequence.get(ds.getString(k, "workitemid")));
        }
        if (ds.getRowCount() > 1) {
            ds.sort("usersequence");
        }
        int sequence = 1;
        for (int k = 0; k < ds.getRowCount(); ++k) {
            ds.setString(k, "propsmatchtestmethodorder", "" + sequence);
            ++sequence;
        }
    }

    private String createDistinctLabSamples(String scheduleplanid, String scheduleplanitemid, String sdcid, PropertyList props, DataSet containerGroupDS, int sampleCount, boolean securityEnabled) {
        StringBuffer keyidList = new StringBuffer();
        try {
            PropertyListCollection specs;
            ActionProcessor ap = this.getActionProcessor();
            props.put("copies", "" + sampleCount);
            if (OpalUtil.isNotEmpty(this.preWOCompleteTraceogId)) {
                StringBuffer traceLogIds = new StringBuffer();
                for (int w = 0; w < sampleCount; ++w) {
                    traceLogIds.append(";").append(this.preWOCompleteTraceogId);
                }
                props.put("tracelogid", traceLogIds.substring(1));
            }
            double containercount = 0.0;
            PropertyList trackitemProps = new PropertyList();
            StringBuffer applyworkitems = new StringBuffer();
            StringBuffer addWorkitems = new StringBuffer();
            StringBuffer addWorkitemVersions = new StringBuffer();
            StringBuffer addSchedulePlanIds = new StringBuffer();
            StringBuffer addSchedulePlanItemIds = new StringBuffer();
            StringBuffer addTrackItemIds = new StringBuffer();
            StringBuffer addSampleIds = new StringBuffer();
            StringBuffer addPropsMatchOrder = new StringBuffer();
            containerGroupDS.sort("departmentid");
            ArrayList<DataSet> deptGroups = containerGroupDS.getGroupedDataSets("departmentid");
            for (int group = 0; group < deptGroups.size(); ++group) {
                DataSet deptDS = deptGroups.get(group);
                String departmentid = deptDS.getString(0, "departmentid");
                if (departmentid != null && departmentid.length() > 0 && securityEnabled) {
                    props.put("securitydepartment", departmentid);
                }
                ap.processAction("AddSDI", "1", props);
                String newKeyIds = (String)props.get("newkeyid1");
                if (this.createUniqSamplePerContr && deptDS.getRowCount() > 1) {
                    PropertyList extraprops = new PropertyList();
                    extraprops.setProperty("sdcid", "Sample");
                    extraprops.setProperty("templateid", StringUtil.split(newKeyIds, ";")[0]);
                    extraprops.put("copies", "" + sampleCount * (deptDS.getRowCount() - 1));
                    if (securityEnabled) {
                        extraprops.put("securityuser", this.securityUser);
                        extraprops.put("securitydepartment", this.securityDepartment);
                        if (departmentid != null && departmentid.length() > 0) {
                            extraprops.put("securitydepartment", departmentid);
                        }
                    }
                    ap.processAction("AddSDI", "1", extraprops);
                    String extraKeyIds = (String)extraprops.get("newkeyid1");
                    newKeyIds = newKeyIds + ";" + extraKeyIds;
                }
                keyidList.append(";").append(newKeyIds);
                String[] keyids = StringUtil.split(newKeyIds, ";");
                int k = 0;
                while (k < keyids.length) {
                    for (int i = 0; i < deptDS.getRowCount(); ++i) {
                        double noofcontainers = deptDS.getDouble(i, "quantity", 0.0);
                        noofcontainers = (double)Math.round(noofcontainers * 100.0) / 100.0;
                        String containerGroupWorkItems = deptDS.getString(i, "teststring", "");
                        containercount = this.quantity.length() == 0 ? (containercount += noofcontainers) : noofcontainers;
                        this.workorderqtyunit = deptDS.getString(i, "quantityunit", "");
                        String strNoofcontainers = "(Containers)".equalsIgnoreCase(this.workorderqtyunit) ? "" + new Double(noofcontainers).intValue() : "" + noofcontainers;
                        String[] workitems = StringUtil.split(containerGroupWorkItems, ";");
                        int sc = 1;
                        if (this.createUniqSamplePerContr) {
                            sc = sampleCount;
                        }
                        while (sc > 0) {
                            String sampleid = keyids[k];
                            if (!this.timeZeroTask) {
                                this.setCrossSampleRelations(this.getQueryProcessor(), ap, sampleid);
                            }
                            --sc;
                            if (this.createUniqSamplePerContr) {
                                ++k;
                            }
                            trackitemProps.setProperty("sdcid", "Sample");
                            trackitemProps.setProperty("keyid1", sampleid);
                            trackitemProps.setProperty("quantity", strNoofcontainers);
                            trackitemProps.setProperty("quantitytype", "(Containers)".equalsIgnoreCase(this.workorderqtyunit) || "".equalsIgnoreCase(this.workorderqtyunit) ? "C" : "U");
                            trackitemProps.setProperty("quantityunit", "(Containers)".equalsIgnoreCase(this.workorderqtyunit) || "".equalsIgnoreCase(this.workorderqtyunit) ? "" : this.workorderqtyunit);
                            trackitemProps.setProperty("numoftrackitems", "1");
                            trackitemProps.setProperty("ownerdepartmentid", departmentid);
                            if (OpalUtil.isNotEmpty(this.preWOCompleteTraceogId)) {
                                trackitemProps.setProperty("tracelogid", this.preWOCompleteTraceogId);
                            }
                            ap.processAction("AddTrackItem", "1", trackitemProps);
                            String trackItemid = (String)trackitemProps.get("newkeyid1");
                            trackitemProps.clear();
                            for (int w = 0; w < workitems.length; ++w) {
                                if (workitems[w] == null || workitems[w].length() <= 0) continue;
                                addWorkitems.append(";" + workitems[w]);
                                int findrow = this.testDataSetNoReuse.findRow("workitemid", workitems[w]);
                                if (findrow > -1) {
                                    addWorkitemVersions.append(";" + this.testDataSetNoReuse.getValue(findrow, "workitemversionid"));
                                } else {
                                    addWorkitemVersions.append(";");
                                }
                                applyworkitems.append(";Y");
                                addSampleIds.append(";" + sampleid);
                                addTrackItemIds.append(";" + trackItemid);
                                addSchedulePlanIds.append(";" + scheduleplanid);
                                addSchedulePlanItemIds.append(";" + scheduleplanitemid);
                                addPropsMatchOrder.append(";");
                            }
                        }
                    }
                    if (this.createUniqSamplePerContr) continue;
                    ++k;
                }
            }
            if (addWorkitems.length() > 1) {
                DataSet dsAddSDIWorkItems = new DataSet();
                dsAddSDIWorkItems.addColumnValues("keyid1", 0, addSampleIds.substring(1), ";");
                dsAddSDIWorkItems.addColumnValues("workitemid", 0, addWorkitems.substring(1), ";");
                dsAddSDIWorkItems.addColumnValues("workitemversionid", 0, addWorkitemVersions.substring(1), ";");
                dsAddSDIWorkItems.addColumnValues("trackitemid", 0, addTrackItemIds.substring(1), ";");
                dsAddSDIWorkItems.addColumnValues("scheduleplanid", 0, addSchedulePlanIds.substring(1), ";");
                dsAddSDIWorkItems.addColumnValues("scheduleplanitemid", 0, addSchedulePlanItemIds.substring(1), ";");
                dsAddSDIWorkItems.addColumnValues("applyworkitem", 0, applyworkitems.substring(1), ";");
                dsAddSDIWorkItems.addColumnValues("propsmatchtestmethodorder", 0, addPropsMatchOrder.substring(1), ";");
                ArrayList<DataSet> sampleGrps = dsAddSDIWorkItems.getGroupedDataSets("keyid1");
                for (int g = 0; g < sampleGrps.size(); ++g) {
                    DataSet ds = sampleGrps.get(g);
                    this.setWorkItemListSequence(ds);
                }
                HashMap<String, String> workitemProps = new HashMap<String, String>();
                workitemProps.put("sdcid", sdcid);
                workitemProps.put("keyid1", dsAddSDIWorkItems.getColumnValues("keyid1", ";"));
                workitemProps.put("workitemid", dsAddSDIWorkItems.getColumnValues("workitemid", ";"));
                workitemProps.put("workitemversionid", dsAddSDIWorkItems.getColumnValues("workitemversionid", ";"));
                workitemProps.put("trackitemid", dsAddSDIWorkItems.getColumnValues("trackitemid", ";"));
                workitemProps.put("scheduleplanid", dsAddSDIWorkItems.getColumnValues("scheduleplanid", ";"));
                workitemProps.put("scheduleplanitemid", dsAddSDIWorkItems.getColumnValues("scheduleplanitemid", ";"));
                workitemProps.put("applyworkitem", dsAddSDIWorkItems.getColumnValues("applyworkitem", ";"));
                workitemProps.put("propsmatch", "Y");
                workitemProps.put("propsmatchtestmethodorder", dsAddSDIWorkItems.getColumnValues("propsmatchtestmethodorder", ";"));
                if (OpalUtil.isNotEmpty(this.preWOCompleteTraceogId)) {
                    StringBuffer traceLogIds = new StringBuffer();
                    for (int w = 0; w < dsAddSDIWorkItems.getRowCount(); ++w) {
                        traceLogIds.append(";").append(this.preWOCompleteTraceogId);
                    }
                    workitemProps.put("tracelogid", traceLogIds.substring(1));
                }
                ap.processAction("AddSDIWorkItem", "1", workitemProps);
            }
            if ((specs = this.createSdi.getCollectionNotNull("Specs")) != null && specs.size() > 0 && keyidList.length() > 0) {
                this.applySpec(keyidList.substring(1), specs, scheduleplanid, scheduleplanitemid, sdcid);
            }
            this.workorderqty = "(Containers)".equalsIgnoreCase(this.workorderqtyunit) ? "" + new Double(containercount).intValue() : "" + containercount;
        }
        catch (Exception ae) {
            this.logger.error(ae.getMessage(), ae);
            throw new RuntimeException(ae);
        }
        return keyidList.length() > 0 ? keyidList.substring(1) : keyidList.toString();
    }

    protected void applySpec(String keyIdList, PropertyListCollection specs, String schedulePlanId, String schedulePlanItemId, String sdcid) throws SapphireException {
        HashMap<String, String> specProps = new HashMap<String, String>();
        StringBuffer specids = new StringBuffer();
        StringBuffer specversionids = new StringBuffer();
        String[] sampleIds = StringUtil.split(keyIdList, ";");
        for (int w = 0; w < specs.size(); ++w) {
            if (specs.getPropertyList(w).getProperty("specid").length() <= 0) continue;
            String[] spec = StringUtil.split(specs.getPropertyList(w).getProperty("specid"), "|");
            specids.append(";" + spec[0]);
            specversionids.append(";" + spec[1]);
        }
        if (specids.length() > 1) {
            String specIds = specids.substring(1);
            String specVerIds = specversionids.substring(1);
            for (int k = 0; k < sampleIds.length; ++k) {
                specProps.put("sdcid", sdcid);
                specProps.put("keyid1", sampleIds[k]);
                specProps.put("specid", specIds);
                specProps.put("specversionid", specVerIds);
                if (OpalUtil.isNotEmpty(this.preWOCompleteTraceogId)) {
                    specProps.put("tracelogid", this.preWOCompleteTraceogId);
                }
                this.getActionProcessor().processAction("AddSDISpec", "1", specProps);
            }
        }
    }

    private void handleContainerReuseSamples(String scheduleplanid, String scheduleplanitemid, Calendar eventdate, DataSet reuseTests, String mode, boolean woCompletion, String woType) throws SapphireException {
        if (mode == null) {
            mode = "";
        }
        PropertyList workorder = this.scheduleProperties.getPropertyListNotNull("workorder");
        try {
            ArrayList<DataSet> testDatasets = reuseTests.getGroupedDataSets("workitemid");
            for (DataSet testDataSet : testDatasets) {
                String firstPlanitemid;
                String workitemid = testDataSet.getString(0, "workitemid");
                String workitemVersionid = testDataSet.getString(0, "workitemversionid");
                int reuseTest_numtrepeats = testDataSet.getBigDecimal(0, "numrepeats", new BigDecimal(1)).intValue();
                if (this.isTestLogged(scheduleplanid, scheduleplanitemid, workitemid) || (firstPlanitemid = this.getFirstPlanItemId(scheduleplanid, scheduleplanitemid, workitemid)) == null || firstPlanitemid.length() <= 0) continue;
                if (firstPlanitemid.equals(scheduleplanitemid)) {
                    String workOrderId = "";
                    String sampleId = "";
                    if (mode.equals(MODEWOONLY) || mode.equals(MODEBOTH) && "Y".equals(workorder.getProperty("create", ""))) {
                        workOrderId = this.createWO(scheduleplanid, scheduleplanitemid, eventdate, "Pull", null, testDataSet, true);
                    }
                    if (mode.equals(MODEBOTH) || mode.equals(MODESAMPLEONLY)) {
                        sampleId = this.createSamples(scheduleplanid, scheduleplanitemid, eventdate, testDataSet, true, workOrderId);
                    }
                    if (sampleId.length() <= 0) continue;
                    this.processReuseSample(sampleId, workitemid, scheduleplanid, scheduleplanitemid, true);
                    continue;
                }
                if (woCompletion && "Pull".equals(woType)) continue;
                StringBuffer sampleIds = new StringBuffer();
                if (mode.equals(MODEBOTH) || mode.equals(MODESAMPLEONLY)) {
                    DataSet dsFirstSample = this.getFirstPlanItemSample(scheduleplanid, firstPlanitemid, workitemid);
                    for (int s = 0; s < dsFirstSample.getRowCount(); ++s) {
                        String sampleid = dsFirstSample.getString(s, "s_sampleid", "");
                        if (sampleIds.indexOf(sampleid) < 0) {
                            sampleIds.append(";").append(sampleid);
                        }
                        String trackitemid = dsFirstSample.getString(s, "trackitemid");
                        HashMap<String, String> workitemProps = new HashMap<String, String>();
                        if (dsFirstSample.getRowCount() == 1 && reuseTest_numtrepeats > 1) {
                            DataSet dsRepeatWI = new DataSet();
                            for (int rc = 0; rc < reuseTest_numtrepeats; ++rc) {
                                int r = dsRepeatWI.addRow();
                                dsRepeatWI.setString(r, "keyid1", sampleid);
                                dsRepeatWI.setString(r, "workitemid", workitemid);
                                dsRepeatWI.setString(r, "workitemversionid", workitemVersionid);
                                dsRepeatWI.setString(r, "trackitemid", trackitemid);
                                dsRepeatWI.setString(r, "scheduleplanid", scheduleplanid);
                                dsRepeatWI.setString(r, "scheduleplanitemid", scheduleplanitemid);
                                dsRepeatWI.setString(r, "applyworkitem", "Y");
                            }
                            workitemProps.put("sdcid", "Sample");
                            workitemProps.put("keyid1", dsRepeatWI.getColumnValues("keyid1", ";"));
                            workitemProps.put("workitemid", dsRepeatWI.getColumnValues("workitemid", ";"));
                            workitemProps.put("workitemversionid", dsRepeatWI.getColumnValues("workitemversionid", ";"));
                            workitemProps.put("trackitemid", dsRepeatWI.getColumnValues("trackitemid", ";"));
                            workitemProps.put("scheduleplanid", dsRepeatWI.getColumnValues("scheduleplanid", ";"));
                            workitemProps.put("scheduleplanitemid", dsRepeatWI.getColumnValues("scheduleplanitemid", ";"));
                            workitemProps.put("applyworkitem", dsRepeatWI.getColumnValues("applyworkitem", ";"));
                            workitemProps.put("propsmatch", "Y");
                            if (OpalUtil.isNotEmpty(this.preWOCompleteTraceogId)) {
                                StringBuffer traceLogIds = new StringBuffer();
                                for (int w = 0; w < dsRepeatWI.getRowCount(); ++w) {
                                    traceLogIds.append(";").append(this.preWOCompleteTraceogId);
                                }
                                workitemProps.put("tracelogid", traceLogIds.substring(1));
                            }
                            this.getActionProcessor().processAction("AddSDIWorkItem", "1", workitemProps);
                        } else {
                            workitemProps.put("sdcid", "Sample");
                            workitemProps.put("keyid1", sampleid);
                            workitemProps.put("workitemid", workitemid);
                            workitemProps.put("workitemversionid", workitemVersionid);
                            workitemProps.put("trackitemid", trackitemid);
                            workitemProps.put("scheduleplanid", scheduleplanid);
                            workitemProps.put("scheduleplanitemid", scheduleplanitemid);
                            workitemProps.put("applyworkitem", "Y");
                            if (OpalUtil.isNotEmpty(this.preWOCompleteTraceogId)) {
                                workitemProps.put("tracelogid", this.preWOCompleteTraceogId);
                            }
                            this.getActionProcessor().processAction("AddSDIWorkItem", "1", workitemProps);
                        }
                        this.processReuseSample(sampleid, workitemid, scheduleplanid, scheduleplanitemid, false);
                        if (!"AdvancedPullSample".equals(this.getLogName())) continue;
                        ArrayList<String> statusList = new ArrayList<String>();
                        statusList.add("Initial");
                        statusList.add("Received");
                        statusList.add("InProgress");
                        statusList.add("Cancelled");
                        statusList.add("Disposed");
                        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT samplestatus FROM s_sample where s_sampleid = ? ", (Object[])new String[]{sampleid});
                        if (ds.getRowCount() != 1) continue;
                        PropertyList props = new PropertyList();
                        String status = ds.getString(0, "samplestatus", "");
                        if (!statusList.contains(status)) {
                            props.setProperty("samplestatus", "InProgress");
                        }
                        if (props.size() <= 0) continue;
                        props.setProperty("sdcid", "Sample");
                        props.setProperty("keyid1", sampleid);
                        if (OpalUtil.isNotEmpty(this.preWOCompleteTraceogId)) {
                            props.put("tracelogid", this.preWOCompleteTraceogId);
                        }
                        this.getActionProcessor().processAction("EditSDI", "1", props);
                    }
                }
                if (!mode.equals(MODEWOONLY) && !mode.equals(MODEBOTH)) continue;
                this.createWO(scheduleplanid, scheduleplanitemid, eventdate, "RePull", sampleIds.length() > 0 ? sampleIds.substring(1) : "", reuseTests, true);
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    protected String getFirstPlanItemId(String schedulePlanId, String schedulePlanItemId, String workItemId) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        String firstPlanItemId = "";
        sql.append("SELECT frst.scheduleplanitemid FROM scheduleplanitem me, scheduleplanitem frst, scheduleplanitemworkitem, scheduletimerule");
        sql.append(" WHERE frst.scheduleplanitemid = scheduleplanitemworkitem.scheduleplanitemid AND frst.scheduleplanid = scheduleplanitemworkitem.scheduleplanid");
        sql.append(" AND frst.scheduleconditionid = me.scheduleconditionid AND frst.scheduleplanid = me.scheduleplanid ");
        sql.append(" AND me.scheduleplanid = ? AND me.scheduleplanitemid = ? ");
        sql.append(" AND frst.scheduletimeruleid = scheduletimerule.scheduletimeruleid AND scheduletimerule.scheduleplanid = me.scheduleplanid ");
        sql.append(" AND scheduleplanitemworkitem.workitemid = ? AND scheduleplanitemworkitem.reusecontainerflag = 'Y' ");
        sql.append(" ORDER BY scheduletimerule.usersequence");
        this.database.createPreparedResultSet("getPriorPlanItems", sql.toString(), new String[]{schedulePlanId, schedulePlanItemId, workItemId});
        if (this.database.getNext("getPriorPlanItems")) {
            firstPlanItemId = this.database.getString("getPriorPlanItems", "scheduleplanitemid");
        }
        this.database.closeResultSet("getPriorPlanItems");
        return firstPlanItemId;
    }

    protected DataSet getFirstPlanItemSample(String schedulePlanId, String firstPlanItemId, String workItemId) throws SapphireException {
        DataSet dsFirstSample = new DataSet();
        String findSampleSQL = "SELECT DISTINCT s_sample.s_sampleid, trackitem.trackitemid FROM s_sample, scheduleplanitemworkitem, sdiworkitem, trackitem WHERE  scheduleplanitemworkitem.scheduleplanid = s_sample.eventplan AND scheduleplanitemworkitem.scheduleplanitemid = s_sample.eventplanitem AND scheduleplanitemworkitem.reusecontainerflag = 'Y' AND sdiworkitem.workitemid = scheduleplanitemworkitem.workitemid AND trackitem.linkkeyid1 = s_sample.s_sampleid AND trackitem.linksdcid = 'Sample' AND sdiworkitem.keyid1 = s_sample.s_sampleid AND sdiworkitem.sdcid = 'Sample'  AND s_sample.eventplan = ?  AND s_sample.eventplanitem = ? AND sdiworkitem.workitemid = ?";
        this.database.createPreparedResultSet("getFirstPlanItemSample", findSampleSQL, new String[]{schedulePlanId, firstPlanItemId, workItemId});
        dsFirstSample.setResultSet(this.database.getResultSet("getFirstPlanItemSample"));
        this.database.closeResultSet("getFirstPlanItemSample");
        return dsFirstSample;
    }

    private String createWO(String scheduleplanid, String scheduleplanitemid, Calendar eventdate, String workordertype, String sampleid, DataSet tests, boolean containerReuse) throws SapphireException {
        if (!this.sdcDeptSecurityEnabled && AdvancedPullSample.isDeptSecurityEnabled(this.getSDCProcessor(), "StudySDC")) {
            this.sdcDeptSecurityEnabled = true;
        }
        PropertyList workorder = this.scheduleProperties.getPropertyList("workorder");
        PropertyList pullamount = this.scheduleProperties.getPropertyListNotNull("pullamount");
        this.quantity = pullamount.getProperty("quantity", "");
        this.units = pullamount.getProperty("units", "");
        boolean multiplyQtyBySampleCount = "Y".equalsIgnoreCase(this.createSdi.getProperty("multiplyqtybysamplecount", "Y"));
        boolean multipleReuseSample = "Y".equalsIgnoreCase(this.createSdi.getProperty("createmultiplereusetestingsample", "Y")) && containerReuse;
        boolean createDistinctLabSamples = "Y".equalsIgnoreCase(this.createSdi.getProperty("createdistinctlaboratorysamples", ""));
        int sampleCount = 1;
        if (!containerReuse) {
            String copiesStr = this.createSdi.getProperty("copies", "");
            if (copiesStr.length() > 0) {
                try {
                    this.copies = Integer.parseInt(copiesStr);
                }
                catch (NumberFormatException nfe) {
                    this.copies = 0;
                }
            } else {
                this.copies = 0;
            }
            sampleCount = this.copies;
        }
        PropertyList plGracePeriod = workorder.getPropertyListNotNull("graceperiod");
        PropertyList plDefaultGracePeriod = plGracePeriod.getPropertyListNotNull("graceperiod");
        PropertyList plDeviation = plGracePeriod.getPropertyListNotNull("deviation");
        PropertyListCollection plGracePeriodDetails = plGracePeriod.getCollectionNotNull("graceperioddetails");
        String defaultGrcPeriod = plDefaultGracePeriod.getProperty("grcperiod", "");
        String defaultEarlyGrcPeriod = plDefaultGracePeriod.getProperty("earlygrcperiod", defaultGrcPeriod);
        if (defaultGrcPeriod.length() == 0 && defaultEarlyGrcPeriod.length() > 0) {
            defaultGrcPeriod = defaultEarlyGrcPeriod;
        }
        String defaultGrcPeriodUnit = plDefaultGracePeriod.getProperty("grcperiodunit", "");
        String deviationflag = plDeviation.getProperty("deviationflag", "");
        String incidenttemplate = plDeviation.getProperty("incidenttemplate", "");
        HashMap<String, String> woprops = new HashMap<String, String>();
        try {
            DataSet ds = null;
            QueryProcessor qp = this.getQueryProcessor();
            SafeSQL safeSQL = new SafeSQL();
            String containertypeid = "";
            String sql = "SELECT s.studyid, s.partialpullflag, s.containertypeid ";
            if (this.sdcDeptSecurityEnabled) {
                sql = sql + ", s.securityuser, s.securitydepartment";
            }
            sql = sql + " from study s, study_scheduleplan sp WHERE s.studyid = sp.studyid AND  sp.scheduleplanid = " + safeSQL.addVar(scheduleplanid);
            if ((this.studyid == null || this.studyid.length() == 0) && (ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues())).getRowCount() > 0) {
                this.studyid = ds.getString(0, "studyid");
                this.partialPullFlag = ds.getString(0, "partialpullflag", "");
                if (this.sdcDeptSecurityEnabled) {
                    this.securityUser = ds.getString(0, "securityuser");
                    this.securityDepartment = ds.getString(0, "securitydepartment");
                }
                if ((containertypeid = ds.getString(0, "containertypeid")) != null && containertypeid.length() > 0) {
                    this.database.createPreparedResultSet("SELECT sizevalue, sizeunits FROM containertype WHERE containertypeid = ? ", new String[]{containertypeid});
                    if (this.database.getNext()) {
                        this.containerSizeUnit = this.database.getString("sizeunits");
                        try {
                            this.containerSize = Double.parseDouble(this.database.getString("sizevalue"));
                        }
                        catch (NumberFormatException e) {
                            this.logger.info("Invalid container size found for container type :" + containertypeid);
                        }
                    }
                }
            }
            woprops.put("sdcid", "WorkOrderSDC");
            woprops.put("eventplan", scheduleplanid);
            woprops.put("eventplanitem", scheduleplanitemid);
            if (this.connectionInfo != null) {
                M18NUtil m18n = new M18NUtil(this.connectionInfo);
                woprops.put("duedt", m18n.format(eventdate));
            } else {
                woprops.put("duedt", this.sdf.format(eventdate.getTime()));
            }
            StringBuffer log = new StringBuffer();
            InventoryCalculation invCalc = new InventoryCalculation(this.getConnectionId(), this.getConnectionProcessor().getSapphireConnection());
            DataSet testCopy = tests.copy();
            if ("X".equalsIgnoreCase(this.partialPullFlag)) {
                testCopy.setString(-1, "departmentid", "");
            }
            DataSet containerGroupDS = invCalc.getContainerGroupData(testCopy, "X".equalsIgnoreCase(this.partialPullFlag) ? "N" : this.partialPullFlag, this.containerSize, this.containerSizeUnit, log);
            if (multipleReuseSample) {
                int reuseTest_numtrepeats;
                sampleCount = reuseTest_numtrepeats = tests.getBigDecimal(0, "numrepeats", new BigDecimal(1)).intValue();
            } else if (this.quantity.length() == 0 && this.createUniqSamplePerContr) {
                sampleCount *= containerGroupDS.getRowCount();
            } else if (createDistinctLabSamples) {
                tests.sort("departmentid");
                ArrayList<DataSet> deptGroups = tests.getGroupedDataSets("departmentid");
                sampleCount *= deptGroups.size();
            }
            String messagetext = workorder.getProperty("message");
            double qty = 0.0;
            try {
                qty = Double.parseDouble(this.quantity);
                qty *= (double)sampleCount;
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
            String[] propertyids = new String[]{"quantity", "units", "copies", "pulltype"};
            String[] propertyvalues = new String[4];
            if (workordertype != null && workordertype.equals("Pull")) {
                if (this.quantity.length() == 0) {
                    double containercount = 0.0;
                    for (int i = 0; i < containerGroupDS.size(); ++i) {
                        double noofcontainers = containerGroupDS.getDouble(i, "quantity", 0.0);
                        noofcontainers = (double)Math.round(noofcontainers * 100.0) / 100.0;
                        this.workorderqtyunit = containerGroupDS.getString(i, "quantityunit");
                        containercount += noofcontainers;
                    }
                    if (multiplyQtyBySampleCount && this.copies > 1) {
                        containercount *= (double)this.copies;
                    }
                    propertyvalues[0] = this.workorderqty = "(Containers)".equalsIgnoreCase(this.workorderqtyunit) ? "" + new Double(containercount).intValue() : "" + containercount;
                    propertyvalues[1] = this.workorderqtyunit;
                } else {
                    propertyvalues[0] = multiplyQtyBySampleCount && sampleCount > 1 && qty > 0.0 ? ("(Containers)".equalsIgnoreCase(this.units) ? "" + new Double(qty).intValue() : new Double(qty).toString()) : this.quantity;
                    propertyvalues[1] = this.units;
                }
                propertyvalues[2] = "" + sampleCount;
                String string = propertyvalues[3] = containerReuse ? "Reuse" : "NonReuse";
            }
            messagetext = messagetext.length() == 0 ? (multiplyQtyBySampleCount && qty > 0.0 && sampleCount > 1 ? ("(Containers)".equalsIgnoreCase(this.units) ? "Pull " + new Double(qty).intValue() + " " + this.units + " for " + sampleCount + " sample" + (sampleCount == 1 ? "" : "s") + "." : "Pull " + qty + " " + this.units + " for " + sampleCount + " sample" + (sampleCount == 1 ? "" : "s") + ".") : (this.quantity != null && this.quantity.length() > 0 ? "Pull " + this.quantity + " " + this.units + " for " + sampleCount + " sample" + (sampleCount == 1 ? "" : "s") + "." : "Pull " + this.workorderqty + " " + this.workorderqtyunit + " for " + sampleCount + " sample" + (sampleCount == 1 ? "" : "s") + ".")) : this.replaceTokens(messagetext, (ScheduleEvent)this.scheduleEvents.get(0));
            if (sampleid == null) {
                sampleid = "";
            }
            if (workordertype != null && workordertype.equalsIgnoreCase("RePull")) {
                messagetext = "Sample " + sampleid + " is to be RePulled.";
                woprops.put("sourcesdcid", "Sample");
                int indexOf = sampleid.indexOf(59);
                if (indexOf > -1) {
                    woprops.put("sourcekeyid1", sampleid.substring(0, indexOf));
                } else {
                    woprops.put("sourcekeyid1", sampleid);
                }
            }
            String woAssigneddept = workorder.getProperty("assigneddepartment", "");
            String testingDepartmentid = workorder.getProperty("testingdepartmentid");
            boolean isPlannable = testingDepartmentid.length() > 0 && "Y".equals(workorder.getProperty("isplannable"));
            boolean useAssigneddeptAsSecurityDept = "Y".equalsIgnoreCase(workorder.getProperty("useassigneddepartmentassecuritydepartment", ""));
            woprops.put("messagetext", messagetext);
            woprops.put("assignedto", workorder.getProperty("assignto", ""));
            woprops.put("assigneddepartment", woAssigneddept);
            woprops.put("testingdepartmentid", testingDepartmentid);
            if (isPlannable) {
                woprops.put("wapstatus", "Pending");
            }
            woprops.put("workorderlabel", workordertype);
            woprops.put("workordertype", workordertype);
            woprops.put("workorderstatus", "Pending");
            woprops.put("scheduleplanid", scheduleplanid);
            woprops.put("scheduleplanitemid", scheduleplanitemid);
            woprops.put("studyid", this.studyid);
            if (AdvancedPullSample.isDeptSecurityEnabled(this.getSDCProcessor(), "WorkOrderSDC")) {
                if (this.sdcDeptSecurityEnabled) {
                    woprops.put("securitydepartment", this.securityDepartment);
                    woprops.put("securityuser", this.securityUser);
                }
                if (useAssigneddeptAsSecurityDept && woAssigneddept.length() > 0) {
                    woprops.put("securitydepartment", woAssigneddept);
                }
            }
            String grcperiod = "";
            String grcperiodunit = "";
            String earlygrcperiod = "";
            ds = null;
            sql = "";
            Calendar startDt = Calendar.getInstance();
            safeSQL.reset();
            sql = "SELECT sc.startdt from schedulecondition sc, scheduleplanitem si  WHERE si.scheduleplanid = " + safeSQL.addVar(scheduleplanid) + " AND si.scheduleplanitemid = " + safeSQL.addVar(scheduleplanitemid) + " AND sc.scheduleplanid = si.scheduleplanid AND sc.scheduleconditionid = si.scheduleconditionid ";
            ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (ds != null && ds.getRowCount() > 0) {
                startDt.setTime(ds.getTimestamp(0, "startdt"));
                String[] gracePeriod = StringUtil.split(GRCPeriodUtil.getGRCPeriodFromTaskCollection(plGracePeriodDetails, startDt, eventdate), ";");
                try {
                    grcperiod = gracePeriod[0];
                    grcperiodunit = gracePeriod[1];
                    earlygrcperiod = gracePeriod[2];
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            if (grcperiod.equals("") && grcperiodunit.equals("")) {
                grcperiod = defaultGrcPeriod;
                grcperiodunit = defaultGrcPeriodUnit;
                earlygrcperiod = defaultEarlyGrcPeriod;
            }
            woprops.put("deviationflag", deviationflag);
            woprops.put("deviationtemplateid", incidenttemplate);
            woprops.put("graceperiodtimeunit", grcperiodunit);
            woprops.put("graceperiod", grcperiod);
            woprops.put("graceperiodearly", earlygrcperiod);
            this.getActionProcessor().processAction("AddSDI", "1", woprops);
            String[] workorderids = StringUtil.split((String)woprops.get("newkeyid1"), ";");
            if (workordertype != null && workordertype.equals("Pull")) {
                this.setWorkOrderProperty(workorderids[0], propertyids, propertyvalues, "I");
            }
            return workorderids[0];
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    protected boolean isSampleLogged(String scheduleplanid, String scheduleplanitemid) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT count(*) FROM s_sample where eventplan = " + safeSQL.addVar(scheduleplanid) + " AND eventplanitem = " + safeSQL.addVar(scheduleplanitemid);
        try {
            if (this.database.getPreparedCount(sql.toString(), safeSQL.getValues()) > 0) {
                return true;
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        return false;
    }

    protected boolean isTestLogged(String scheduleplanid, String scheduleplanitemid, String workitemid) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT count(*) FROM sdiworkitem where scheduleplanid = " + safeSQL.addVar(scheduleplanid) + " AND scheduleplanitemid = " + safeSQL.addVar(scheduleplanitemid) + " AND workitemid = " + safeSQL.addVar(workitemid);
        try {
            if (this.database.getPreparedCount(sql.toString(), safeSQL.getValues()) > 0) {
                return true;
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        return false;
    }

    protected DataSet loadTestProperties(String scheduleplanid, String scheduleplanitemid, boolean isTimeZeroTask) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT workitemid, workitemversionid, departmentid, quantity, quantityunit, numrepeats, containerperrepeatflag, containerpertestflag, destructivetestflag, reusecontainerflag, usersequence FROM scheduleplanitemworkitem  WHERE scheduleplanid = " + safeSQL.addVar(scheduleplanid) + " AND scheduleplanitemid = " + safeSQL.addVar(scheduleplanitemid) + " AND contingentflag != 'Y'");
        if (isTimeZeroTask) {
            sql.append(" AND workitemid NOT IN ( SELECT DISTINCT workitemid FROM sdiworkitem, scheduleplanitem, scheduleconditionrefitem  WHERE scheduleplanitem.scheduleplanid = " + safeSQL.addVar(scheduleplanid) + "  AND scheduleplanitem.scheduleplanitemid = " + safeSQL.addVar(scheduleplanitemid) + "  AND scheduleconditionrefitem.scheduleplanid = scheduleplanitem.scheduleplanid  AND scheduleconditionrefitem.scheduleconditionid = scheduleplanitem.scheduleconditionid  AND scheduleconditionrefitem.refitemsdcid='Sample'  AND sdiworkitem.sdcid = 'Sample'  AND sdiworkitem.keyid1 = scheduleconditionrefitem.refitemkeyid1 ) ");
        }
        try {
            sql.append(" ORDER By scheduleplanitemworkitem.usersequence");
            this.database.createPreparedResultSet("testproperties", sql.toString(), safeSQL.getValues());
            DataSet ds = new DataSet();
            ds.setResultSet(this.database.getResultSet("testproperties"));
            for (int i = 0; i < ds.size(); ++i) {
                this.testUserSequence.put(ds.getValue(i, "workitemid"), ds.getValue(i, "usersequence", "1"));
                if (!ds.isNull(i, "reusecontainerflag")) continue;
                ds.setString(i, "reusecontainerflag", "N");
            }
            return ds;
        }
        catch (Exception se) {
            throw new SapphireException(this.getLogName() + ": DataSet creation failure.", ErrorUtil.extractMessage("Failed to get result set. Reason: " + sql.toString(), ErrorUtil.isUserAdmin(this.getConnectionId())), se);
        }
    }

    protected boolean isTimeZeroTask(String scheduleplanid, String scheduleplanitemid) throws SapphireException {
        StringBuffer timeZeroSql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        timeZeroSql.append("SELECT count(*) FROM scheduleevent, schedulecondition, scheduleplanitem  WHERE scheduleplanitem.scheduleplanid = " + safeSQL.addVar(scheduleplanid) + " AND scheduleplanitem.scheduleplanitemid = " + safeSQL.addVar(scheduleplanitemid) + " AND scheduleevent.scheduleplanitemid = scheduleplanitem.scheduleplanitemid  AND scheduleevent.scheduleplanid = scheduleplanitem.scheduleplanid AND schedulecondition.scheduleconditionid = scheduleplanitem.scheduleconditionid AND schedulecondition.scheduleplanid = scheduleplanitem.scheduleplanid");
        if (this.database.isOracle()) {
            timeZeroSql.append(" AND to_char(scheduleevent.eventdt,'dd-mon-yyyy hh24:mi') = to_char(schedulecondition.startdt,'dd-mon-yyyy hh24:mi')");
        } else {
            timeZeroSql.append(" AND convert(varchar, scheduleevent.eventdt, 0) = convert(varchar, schedulecondition.startdt, 0)");
        }
        return this.database.getPreparedCount(timeZeroSql.toString(), safeSQL.getValues()) > 0;
    }

    private String replaceTokens(String value, ScheduleEvent event) {
        if (value.indexOf("[") >= 0) {
            String[] tokens = StringUtil.getTokens(value);
            for (int i = 0; i < tokens.length; ++i) {
                value = StringUtil.replaceAll(value, "[" + tokens[i] + "]", this.getTokenValue(tokens[i], event));
            }
        }
        return value;
    }

    private String getTokenValue(String valuefrom, ScheduleEvent event) {
        String tempvalue = "";
        if ("Event Date".equals(valuefrom)) {
            if (this.connectionInfo != null) {
                M18NUtil m18n = new M18NUtil(this.connectionInfo);
                tempvalue = m18n.format(event.getEventDt());
            } else {
                tempvalue = this.sdf.format(event.getEventDt().getTime());
            }
        } else if ("Plan Id".equals(valuefrom)) {
            tempvalue = event.getSchedulePlanid();
        } else if ("Plan Item Id".equals(valuefrom)) {
            tempvalue = event.getSchedulePlanitemid();
        } else if ("Quantity".equals(valuefrom)) {
            tempvalue = this.quantity;
        } else if ("Units".equals(valuefrom)) {
            tempvalue = this.units;
        } else if ("How Many".equals(valuefrom)) {
            tempvalue = "" + this.copies;
        }
        return tempvalue;
    }

    @Override
    public String[] getDetailLevels() {
        return new String[]{SUMMARY, PULLAMOUNTS, FULL, WORKORDER};
    }

    @Override
    public String getColor() {
        return "lightblue";
    }

    @Override
    public String getTitle() {
        return "Advanced Pull Sample";
    }

    @Override
    public String getSummaryHTML(PropertyList propertyList, String detailLevel) {
        StringBuffer displayValue = new StringBuffer();
        if (detailLevel.equals(SUMMARY) || detailLevel.equals("")) {
            displayValue.append(this.getSummary(propertyList)).append("<br>");
        } else if (detailLevel.equals(PULLAMOUNTS)) {
            displayValue.append(this.getPullAmounts(propertyList)).append("<br>");
        } else if (detailLevel.equals(FULL)) {
            displayValue.append(this.getSummary(propertyList)).append("<br>");
            displayValue.append(this.getPullAmounts(propertyList)).append("<br>");
        } else if (detailLevel.equals(WORKORDER)) {
            displayValue.append(this.getWorkorder(propertyList));
        }
        return displayValue.toString();
    }

    @Override
    public String getSummaryText(PropertyList propertyList, String detailLevel) {
        StringBuffer displayValue = new StringBuffer();
        PropertyList pullamount = propertyList.getPropertyListNotNull("pullamount");
        String quantity = pullamount.getProperty("quantity");
        if (quantity.length() > 0) {
            displayValue.append("Pull " + quantity);
            String units = pullamount.getProperty("units");
            if (units.length() > 0) {
                displayValue.append(" " + units);
            }
        } else {
            displayValue.append("No amount defined");
        }
        displayValue.append(" for ").append(this.getSummary(propertyList));
        PropertyList workorder = propertyList.getPropertyList("workorder");
        String message = workorder.getProperty("message");
        if (message.length() > 0) {
            displayValue.append("\nInstructions: " + message);
        }
        return displayValue.toString();
    }

    @Override
    public final double getQuantity(PlanItem planItem) {
        PropertyList pullamount = planItem.getCollapsedPropertyList().getPropertyList("pullamount");
        String quantity = pullamount.getProperty("quantity");
        double d = 0.0;
        try {
            d = Double.parseDouble(quantity);
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
        return d;
    }

    @Override
    public final String getUnits(PlanItem planItem) {
        PropertyList pullamount = planItem.getCollapsedPropertyList().getPropertyList("pullamount");
        return pullamount.getProperty("units");
    }

    protected StringBuffer getSummary(PropertyList propertyList) {
        StringBuffer output = new StringBuffer();
        PropertyList createsdi = propertyList.getPropertyList("createsdi");
        String copies = createsdi.getProperty("copies");
        String templateid = createsdi.getProperty("templateid");
        if (copies.length() == 0) {
            copies = "0";
        }
        output.append(copies + " ");
        if (templateid.length() > 0) {
            output.append(templateid + " ");
        }
        output.append("Sample");
        if (!copies.equals("1")) {
            output.append("s");
        }
        return output;
    }

    protected StringBuffer getPullAmounts(PropertyList propertyList) {
        StringBuffer output = new StringBuffer();
        PropertyList pullamount = propertyList.getPropertyListNotNull("pullamount");
        String quantity = pullamount.getProperty("quantity");
        String units = pullamount.getProperty("units");
        if (quantity.length() > 0) {
            output.append(quantity).append(units);
        } else {
            output.append("Calculated");
        }
        return output;
    }

    protected StringBuffer getWorkorder(PropertyList propertyList) {
        StringBuffer summary = new StringBuffer();
        PropertyList workorder = propertyList.getPropertyList("workorder");
        if (workorder.getProperty("create").equals("Y")) {
            String assignTo = workorder.getProperty("assignto");
            String daysEarly = workorder.getProperty("daysearly");
            String message = workorder.getProperty("message");
            summary.append(message.length() > 0 ? message : "No message");
            if (assignTo.length() > 0) {
                summary.append("<br>Assign To: " + assignTo);
            }
            if (daysEarly.length() > 0 && !daysEarly.equals("0")) {
                summary.append("<br>" + daysEarly + " day" + (!daysEarly.equals("1") ? "s" : "") + " early");
            }
        } else {
            summary.append("Not Generated");
        }
        return summary;
    }

    @Override
    public final void statusInit(ScheduleGrid grid, PropertyList propertyList) {
        SafeSQL safeSQL = new SafeSQL();
        boolean sampleSecurityEnabled = AdvancedPullSample.isDeptSecurityEnabled(new SDCProcessor(grid.getConnectionId()), "Sample");
        String sampleSQL = "SELECT s_sampleid, samplestatus, eventplan, eventplanitem, ";
        sampleSQL = sampleSQL + (sampleSecurityEnabled ? " securitydepartment," : "");
        sampleSQL = sampleSQL + "schedulerulelabel FROM s_sample where eventplan = " + safeSQL.addVar(grid.planid);
        this.allSamples = grid.queryProcessor.getPreparedSqlDataSet(sampleSQL, safeSQL.getValues());
        safeSQL.reset();
        String specSql = "SELECT s_sampleid keyid1, (case  when (select count(1) from sdispec where sdcid = 'Sample' and keyid1 = s_sample.s_sampleid and (condition is not null  OR condition != '' ) and oosgeneratingflag  != 'N') < 1 then '' ELSE  (select refvalueid from refvalue where reftypeid = 'Spec Condition' and usersequence = (select  max(r.usersequence)   FROM refvalue r, sdispec sp  where r.reftypeid = 'Spec Condition' and r.refvalueid = sp.condition and sp.sdcid = 'Sample' and sp.keyid1 = s_sample.s_sampleid and  sp.oosgeneratingflag  != 'N' ) ) END ) worstoosspeccondition FROM s_sample WHERE s_sample.eventplan = " + safeSQL.addVar(grid.planid);
        this.allSampleSpecConditions = grid.queryProcessor.getPreparedSqlDataSet(specSql, safeSQL.getValues());
        safeSQL.reset();
        String workorderSQL = "SELECT workorderid, workorderstatus, workordertype, scheduleplanid, scheduleplanitemid FROM workorder where scheduleplanid = " + safeSQL.addVar(grid.planid);
        this.allWorkorders = grid.queryProcessor.getPreparedSqlDataSet(workorderSQL, safeSQL.getValues());
    }

    @Override
    public final String getStatusHTML(PlanItem planItem, String detailLevel) {
        StringBuffer output = new StringBuffer();
        HashMap<String, String> sampleFilter = new HashMap<String, String>();
        sampleFilter.put("eventplan", planItem.grid.planid);
        sampleFilter.put("eventplanitem", planItem.planItemid);
        DataSet samples = this.allSamples.getFilteredDataSet(sampleFilter);
        Object specInterpretation = null;
        TranslationProcessor tp = null;
        try {
            tp = new TranslationProcessor(planItem.grid.getPageContext());
        }
        catch (Exception e) {
            tp = new TranslationProcessor(planItem.grid.getConnectionId());
        }
        if (samples.size() > 0) {
            DataSet specInterpretaion = new DataSet();
            try {
                specInterpretaion = StabilityUtil.getSpecConditionRefTypeData(new QueryProcessor(planItem.grid.getConnectionId()));
            }
            catch (Exception e) {
                this.logger.info("GENERAL_ERROR", "SpecConditions RefType data could not be obtained.");
            }
            output.append("<table border=0 cellspacing=0 cellpadding=0>");
            for (int i = 0; i < samples.size(); ++i) {
                String sampleid = samples.getValue(i, "s_sampleid");
                String samplestatus = samples.getString(i, "samplestatus", "");
                String department = samples.getString(i, "securitydepartment", "");
                if (department.length() > 0) {
                    department = "( " + department + " )";
                }
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("keyid1", sampleid);
                DataSet worstOOSSpec = this.allSampleSpecConditions.getFilteredDataSet(filter);
                output.append("<tr>");
                output.append("<td><img src='WEB-OPAL/pagetypes/stability/images/sample.gif'></td><td>").append(StabilityUtil.getSpecHtml(worstOOSSpec, specInterpretaion, sampleid, tp)).append("</td><td><a target='taskdetails' title='" + department + "' href='rc?command=page&page=SampleGridStatus&keyid1=" + HttpUtil.encodeURIComponent(sampleid) + "'>" + sampleid + "</a>&nbsp;(" + samplestatus + ")</td>");
                output.append("</tr>");
            }
            output.append("</table>");
        }
        HashMap<String, String> workorderFilter = new HashMap<String, String>();
        workorderFilter.put("scheduleplanid", planItem.grid.planid);
        workorderFilter.put("scheduleplanitemid", planItem.planItemid);
        DataSet workorder = this.allWorkorders.getFilteredDataSet(workorderFilter);
        if (workorder.size() > 0) {
            for (int i = 0; i < workorder.size(); ++i) {
                String workorderid = workorder.getValue(i, "workorderid");
                String workorderstatus = workorder.getValue(i, "workorderstatus");
                String workordertype = workorder.getValue(i, "workordertype");
                output.append("<img src='WEB-OPAL/pagetypes/stability/images/workorder.gif'>&nbsp;(" + SafeHTML.encodeForHTML(workordertype) + ")&nbsp;<a target='taskdetails' href='rc?command=page&page=WorkorderGridStatus&keyid1=" + HttpUtil.encodeURIComponent(workorderid) + "'>" + workorder.getValue(i, "workorderid") + "</a>&nbsp;(" + workorderstatus + ")<br>");
            }
        }
        return output.toString();
    }

    @Override
    public boolean isComplete(String planid, String planitemid, DBAccess database) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT COUNT(1) FROM s_sample");
        sql.append(" WHERE eventplan = " + safeSQL.addVar(planid) + " AND  eventplanitem =" + safeSQL.addVar(planitemid));
        sql.append(" AND  ( samplestatus is null OR ( samplestatus not in ( 'Reviewed', 'Cancelled' ) ) ) ");
        try {
            if (database.getPreparedCount(sql.toString(), safeSQL.getValues()) > 0) {
                return false;
            }
            sql.setLength(0);
            safeSQL.reset();
            sql.append("SELECT COUNT(1) FROM workorder");
            sql.append(" WHERE scheduleplanid = " + safeSQL.addVar(planid) + " AND  scheduleplanitemid =" + safeSQL.addVar(planitemid) + "  AND completedt is null");
            if (database.getPreparedCount(sql.toString(), safeSQL.getValues()) > 0) {
                return false;
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        return true;
    }

    @Override
    public final void workorderCompleted(String workorderId, DBAccess database) {
        this.database = database;
        try {
            this.createSdi = this.scheduleProperties.getPropertyListNotNull("createsdi");
            this.postWOComplete(workorderId, database);
        }
        catch (Exception ex) {
            this.logger.error(this.getLogName() + " -> Could not process method workorderCompleted:  " + ex.getMessage(), ex);
        }
    }

    public final void preWOComplete(String workorderId, DBAccess database, String tracelogid) throws SapphireException {
        this.preWOCompleteTraceogId = tracelogid;
        this.preWOComplete(workorderId, database);
    }

    public final void preWOComplete(String workorderId, DBAccess database) throws SapphireException {
        this.database = database;
        this.createSdi = this.scheduleProperties.getPropertyListNotNull("createsdi");
        String deferSampleCreation = this.createSdi.getProperty("defersamplecreation", "");
        PropertyList pullamount = this.scheduleProperties.getPropertyListNotNull("pullamount");
        this.quantity = pullamount.getProperty("quantity", "");
        this.units = pullamount.getProperty("units", "");
        String copiesStr = this.createSdi.getProperty("copies", "");
        if (copiesStr.length() > 0) {
            try {
                this.copies = Integer.parseInt(copiesStr);
            }
            catch (NumberFormatException nfe) {
                this.copies = 0;
            }
        } else {
            this.copies = 0;
        }
        String workorderType = "";
        String pullType = "";
        database.createPreparedResultSet("wotype", "SELECT workordertype FROM workorder WHERE workorderid = ?", new Object[]{workorderId});
        if (database.getNext("wotype")) {
            workorderType = database.getString("wotype", "workordertype");
        }
        database.closeResultSet("wotype");
        if (workorderType != null && workorderType.equals("Pull")) {
            database.createPreparedResultSet("pulltype", "SELECT propertyvalue FROM workorderproperty WHERE workorderid = ? AND propertyid = 'pulltype' ", new Object[]{workorderId});
            if (database.getNext("pulltype")) {
                pullType = database.getString("pulltype", "propertyvalue");
            }
            database.closeResultSet("pulltype");
        }
        boolean sampleCount = true;
        if (this.scheduleEvents != null) {
            int events = this.scheduleEvents.size();
            for (int i = 0; i < events; ++i) {
                ScheduleEvent event = (ScheduleEvent)this.scheduleEvents.get(i);
                String scheduleplanid = event.getSchedulePlanid();
                String scheduleplanitemid = event.getSchedulePlanitemid();
                this.timeZeroTask = this.isTimeZeroTask(event.getSchedulePlanid(), event.getSchedulePlanitemid());
                DataSet ds = this.loadTestProperties(scheduleplanid, scheduleplanitemid, this.timeZeroTask);
                HashMap<String, String> hmFilter = new HashMap<String, String>();
                hmFilter.put("reusecontainerflag", "Y");
                this.testDataSetReuse = ds.getFilteredDataSet(hmFilter);
                hmFilter.clear();
                hmFilter.put("reusecontainerflag", "N");
                this.testDataSetNoReuse = ds.getFilteredDataSet(hmFilter);
                if (workorderType != null && workorderType.equalsIgnoreCase("RePull")) {
                    this.handleContainerReuseSamples(scheduleplanid, scheduleplanitemid, event.getEventDt(), this.testDataSetReuse, MODESAMPLEONLY, true, workorderType);
                    continue;
                }
                if (!deferSampleCreation.equalsIgnoreCase("Y") || this.isSampleLogged(scheduleplanid, scheduleplanitemid)) continue;
                String sampleIds = this.createSamples(scheduleplanid, scheduleplanitemid, event.getEventDt(), this.testDataSetNoReuse, false, workorderId);
                if (pullType != null && pullType.equalsIgnoreCase("NonReuse") && this.calculatedSampleCount > 0) {
                    this.updateWorkOrderProperty(workorderId, this.calculatedSampleCount);
                }
                this.handleContainerReuseSamples(scheduleplanid, scheduleplanitemid, event.getEventDt(), this.testDataSetReuse, MODESAMPLEONLY, true, workorderType);
                if (pullType != null && pullType.equalsIgnoreCase("Reuse") && this.calculatedSampleCount > 0) {
                    this.updateWorkOrderProperty(workorderId, this.calculatedSampleCount);
                }
                if (sampleIds == null || sampleIds.length() <= 0) continue;
                this.logger.info("Deferred samples are created for WorkOrder Id :" + workorderId);
            }
        }
    }

    private void updateWorkOrderProperty(String workorderId, int sampleCount) throws SapphireException {
        String[] propertyids = new String[]{"quantity", "units", "copies"};
        String[] propertyvalues = new String[3];
        if (this.quantity.length() == 0) {
            propertyvalues[0] = this.workorderqty;
            propertyvalues[1] = this.workorderqtyunit;
        } else {
            propertyvalues[0] = this.quantity;
            propertyvalues[1] = this.units;
        }
        propertyvalues[2] = "" + sampleCount;
        this.setWorkOrderProperty(workorderId, propertyids, propertyvalues, "U");
    }

    private void setWorkOrderProperty(String workorderId, String[] propertyids, String[] propertyvalues, String mode) throws SapphireException {
        try {
            if (mode.equalsIgnoreCase("I")) {
                PreparedStatement insert = this.database.prepareStatement("insert into workorderproperty (workorderid, propertyid, propertyvalue) values ( ?, ?, ? )");
                for (int j = 0; j < propertyids.length; ++j) {
                    insert.setString(1, workorderId);
                    insert.setString(2, propertyids[j]);
                    insert.setString(3, propertyvalues[j]);
                    insert.execute();
                }
            } else {
                String sql = "";
                sql = OpalUtil.isNotEmpty(this.preWOCompleteTraceogId) ? "UPDATE workorderproperty SET propertyvalue = ?, tracelogid = ?  WHERE propertyid = ? AND workorderid = '" + workorderId + "'" : "UPDATE workorderproperty SET propertyvalue = ? WHERE propertyid = ? AND workorderid = '" + workorderId + "'";
                PreparedStatement update = this.database.prepareStatement(sql);
                for (int j = 0; j < propertyids.length; ++j) {
                    update.setString(1, propertyvalues[j]);
                    if (OpalUtil.isNotEmpty(this.preWOCompleteTraceogId)) {
                        update.setString(2, this.preWOCompleteTraceogId);
                        update.setString(3, propertyids[j]);
                    } else {
                        update.setString(2, propertyids[j]);
                    }
                    update.executeUpdate();
                }
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    private void postWOComplete(String workorderId, DBAccess database) throws SapphireException {
        this.preWOComplete(workorderId, database);
        Calendar date = Calendar.getInstance();
        this.updateGracePeriod(date);
    }

    private void updateGracePeriod(Calendar date) throws SapphireException {
        String sql = "";
        if (this.scheduleEvents != null) {
            DataSet ds = new DataSet();
            int events = this.scheduleEvents.size();
            sql = "SELECT swi.duedtoffset, swi.duedtoffsettimeunit, sdi.sdcid, sdi.keyid1, sdi.workitemid  FROM sdiworkitem sdi, s_sample sample, scheduleplanitemworkitem swi  WHERE sdi.sdcid = 'Sample' AND sdi.keyid1 = sample.s_sampleid   AND swi.workitemid = sdi.workitemid and swi.scheduleplanid = sample.eventplan  AND swi.scheduleplanitemid = sample.eventplanitem AND swi.reusecontainerflag  != 'Y' AND sample.eventplan = ?  AND sample.eventplanitem = ? ";
            try {
                PreparedStatement psmt = this.database.prepareStatement(sql);
                DataSet dsUpdate = new DataSet();
                dsUpdate.addColumn("sdcid", 0);
                dsUpdate.addColumn("keyid1", 0);
                dsUpdate.addColumn("workitemid", 0);
                dsUpdate.addColumn("duedt", 2);
                for (int i = 0; i < events; ++i) {
                    ScheduleEvent event = (ScheduleEvent)this.scheduleEvents.get(i);
                    psmt.setString(1, event.getSchedulePlanid());
                    psmt.setString(2, event.getSchedulePlanitemid());
                    ResultSet rs = psmt.executeQuery();
                    ds.setResultSet(rs);
                    for (int k = 0; k < ds.size(); ++k) {
                        String duedtoffset = ds.getValue(k, "duedtoffset");
                        String duedtoffsetunit = ds.getString(k, "duedtoffsettimeunit");
                        if (duedtoffset.length() <= 0 || duedtoffsetunit.length() <= 0) continue;
                        Calendar duedate = GRCPeriodUtil.getDueDate(date, duedtoffsetunit, new BigDecimal(duedtoffset));
                        int numrow = dsUpdate.addRow();
                        dsUpdate.setString(numrow, "sdcid", ds.getString(k, "sdcid"));
                        dsUpdate.setString(numrow, "keyid1", ds.getString(k, "keyid1"));
                        dsUpdate.setString(numrow, "workitemid", ds.getString(k, "workitemid"));
                        dsUpdate.setDate(numrow, "duedt", duedate);
                    }
                }
                if (dsUpdate.size() > 0) {
                    DataSetUtil.update(this.database, dsUpdate, "sdiworkitem", new String[]{"sdcid", "keyid1", "workitemid"});
                }
            }
            catch (Exception e) {
                throw new SapphireException(e);
            }
        }
    }

    @Override
    public HashMap getDetails(PropertyList properties) {
        HashMap<String, String> details = new HashMap<String, String>();
        PropertyList pullAmount = properties.getPropertyListNotNull("pullamount");
        String quantity = pullAmount.getProperty("quantity");
        String units = pullAmount.getProperty("units");
        if (quantity.length() > 0) {
            details.put("quantity", quantity + " " + (units.length() > 0 ? units : "(Containers)"));
        }
        PropertyList createSDI = properties.getPropertyListNotNull("createsdi");
        details.put("copies", createSDI.getProperty("copies"));
        details.put("templateid", createSDI.getProperty("templateid"));
        details.put("multiplyqtybysamplecount", createSDI.getProperty("multiplyqtybysamplecount"));
        details.put("createdistinctlaboratorysamples", createSDI.getProperty("createdistinctlaboratorysamples"));
        details.put("defersamplecreation", createSDI.getProperty("defersamplecreation"));
        details.put("createmultiplereusetestingsample", createSDI.getProperty("createmultiplereusetestingsample"));
        details.put("createuniquesamplepercontainer", createSDI.getProperty("createuniquesamplepercontainer"));
        PropertyListCollection workitems = createSDI.getCollectionNotNull("WorkItems");
        StringBuffer workitemList = new StringBuffer();
        for (PropertyList workitem : workitems) {
            workitemList.append(", " + workitem.getProperty("workitemid"));
        }
        if (workitemList.length() > 0) {
            details.put("workitems", workitemList.substring(2));
        }
        PropertyListCollection specs = createSDI.getCollectionNotNull("Specs");
        StringBuffer specList = new StringBuffer();
        for (PropertyList spec : specs) {
            specList.append(", " + spec.getProperty("specid"));
        }
        if (specList.length() > 0) {
            details.put("specs", specList.substring(2));
        }
        PropertyList workorder = properties.getPropertyListNotNull("workorder");
        details.put("createworkorder", workorder.getProperty("create"));
        details.put("workordermessage", workorder.getProperty("message"));
        details.put("workorderassignedto", workorder.getProperty("assignto"));
        details.put("workorderassigneddepartment", workorder.getProperty("assigneddepartment"));
        details.put("workorderuseassigneddepartmentassecuritydepartment", workorder.getProperty("useassigneddepartmentassecuritydepartment"));
        PropertyList plGracePeriod = workorder.getPropertyListNotNull("graceperiod");
        PropertyList plDefaultGracePeriod = plGracePeriod.getPropertyListNotNull("graceperiod");
        PropertyList plDeviation = plGracePeriod.getPropertyListNotNull("deviation");
        PropertyListCollection plGracePeriodDetails = plGracePeriod.getCollectionNotNull("graceperioddetails");
        String defaultGrcPeriod = plDefaultGracePeriod.getProperty("grcperiod", "");
        String defaultEarlyGrcPeriod = plDefaultGracePeriod.getProperty("earlygrcperiod", defaultGrcPeriod);
        if (defaultGrcPeriod.length() == 0 && defaultEarlyGrcPeriod.length() > 0) {
            defaultGrcPeriod = defaultEarlyGrcPeriod;
        }
        String defaultGrcPeriodUnit = plDefaultGracePeriod.getProperty("grcperiodunit", "");
        String deviationflag = plDeviation.getProperty("deviationflag", "");
        String incidenttemplate = plDeviation.getProperty("incidenttemplate", "");
        details.put("workorderdefaultearlygraceperiod", defaultEarlyGrcPeriod);
        details.put("workorderdefaultgraceperiod", defaultGrcPeriod);
        details.put("workorderdefaultgraceperiodunit", defaultGrcPeriodUnit);
        details.put("workorderdeviationflag", deviationflag);
        details.put("workorderdeviationincidenttemplateg", incidenttemplate);
        return details;
    }

    private static boolean isDeptSecurityEnabled(SDCProcessor sdcProc, String sdcid) {
        DataSet columnDS = sdcProc.getColumnData(sdcid);
        return columnDS != null && columnDS.size() > 0 && columnDS.findRow("columnid", "securityuser") > -1 && columnDS.findRow("columnid", "securitydepartment") > -1;
    }

    private void setCrossSampleRelations(QueryProcessor qp, ActionProcessor ap, String sampleId) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("select distinct ref.refitemkeyid1 sampleid from s_sample s,scheduleconditionrefitem ref, scheduleplanitem pi").append(" where ref.scheduleplanid = pi.scheduleplanid and ref.scheduleconditionid = pi.scheduleconditionid ").append(" and pi.scheduleplanitemid = s.eventplanitem  and pi.scheduleplanid = s.eventplan ").append(" and s.s_sampleid = " + safeSQL.addVar(sampleId) + " union ").append(" select priorsample.s_sampleid sampleid from s_sample sample, s_sample priorsample, scheduleplanitem  priorpi, ").append(" scheduleplanitem  pi , schedulecondition cond ").append(" where priorsample.eventdt = cond.startdt and cond.scheduleplanid = pi.scheduleplanid ").append(" and cond.scheduleconditionid = pi.scheduleconditionid  and priorpi.scheduleplanitemid = priorsample.eventplanitem ").append(" and priorpi.scheduleplanid = priorsample.eventplan and priorpi.scheduleplanid = pi.scheduleplanid").append(" and priorpi.scheduleconditionid = pi.scheduleconditionid  and pi.scheduleplanitemid = sample.eventplanitem ").append(" and pi.scheduleplanid = sample.eventplan and sample.s_sampleid = " + safeSQL.addVar(sampleId));
        DataSet dsT0Sample = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        dsT0Sample.addColumn("relationtype", 0);
        dsT0Sample.setString(-1, "relationtype", "T0Sample");
        sql.setLength(0);
        safeSQL.reset();
        sql.append("select priorsample.s_sampleid sampleid ").append(" from s_sample sample, s_sample priorsample, scheduleplanitem priorpi, scheduleplanitem pi").append(" where priorsample.eventdt = ( select max(s.eventdt)").append(" from s_sample s, scheduleplanitem i ").append(" where s.eventplan = i.scheduleplanid and s.eventplanitem = i.scheduleplanitemid ").append(" and i.scheduleplanid = pi.scheduleplanid and i.scheduleconditionid = pi.scheduleconditionid and ").append(" s.eventplanitem <> sample.eventplanitem and s.eventdt<sample.eventdt) ").append(" and  priorpi.scheduleplanitemid = priorsample.eventplanitem and ").append(" priorpi.scheduleplanid = priorsample.eventplan and priorpi.scheduleplanid = pi.scheduleplanid ").append(" and priorpi.scheduleconditionid = pi.scheduleconditionid and pi.scheduleplanitemid = sample.eventplanitem").append(" and pi.scheduleplanid = sample.eventplan and sample.s_sampleid = " + safeSQL.addVar(sampleId));
        DataSet dsPriorSample = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        dsPriorSample.addColumn("relationtype", 0);
        dsPriorSample.setString(-1, "relationtype", "PriorTimepoint");
        for (int i = 0; i < dsT0Sample.size(); ++i) {
            int row = dsPriorSample.addRow();
            dsPriorSample.setString(row, "sampleid", dsT0Sample.getString(i, "sampleid", ""));
            dsPriorSample.setString(row, "relationtype", dsT0Sample.getString(i, "relationtype", ""));
        }
        HashMap<String, String> props = new HashMap<String, String>();
        StringBuffer fromSDCId = new StringBuffer();
        StringBuffer fromKeyId1 = new StringBuffer();
        StringBuffer fromKeyId2 = new StringBuffer();
        StringBuffer fromKeyId3 = new StringBuffer();
        StringBuffer toSDCId = new StringBuffer();
        StringBuffer toKeyId1 = new StringBuffer();
        StringBuffer toKeyId2 = new StringBuffer();
        StringBuffer toKeyId3 = new StringBuffer();
        StringBuffer relationTypes = new StringBuffer();
        StringBuffer relationIds = new StringBuffer();
        String sdcid = "SDIRelation";
        int copies = 0;
        SequenceProcessor scp = this.getSequenceProcessor();
        for (int i = 0; i < dsPriorSample.size(); ++i) {
            String targetSampleId = dsPriorSample.getString(i, "sampleid", "");
            if (targetSampleId.length() <= 0) continue;
            ++copies;
            fromSDCId.append(";Sample");
            fromKeyId1.append(";" + sampleId);
            fromKeyId2.append(";(null)");
            fromKeyId3.append(";(null)");
            toSDCId.append(";Sample");
            toKeyId1.append(";" + targetSampleId);
            toKeyId2.append(";(null)");
            toKeyId3.append(";(null)");
            String relationType = dsPriorSample.getValue(i, "relationtype");
            relationTypes.append(";" + relationType);
            relationIds.append(";" + relationType + "-" + scp.getSequence(sdcid, relationType));
        }
        if (copies > 0) {
            props.put("sdcid", sdcid);
            props.put("overrideautokey", "Y");
            props.put("keyid1", relationIds.substring(1));
            props.put("fromsdcid", fromSDCId.substring(1));
            props.put("fromkeyid1", fromKeyId1.substring(1));
            props.put("fromkeyid2", fromKeyId2.substring(1));
            props.put("fromkeyid3", fromKeyId3.substring(1));
            props.put("tosdcid", toSDCId.substring(1));
            props.put("tokeyid1", toKeyId1.substring(1));
            props.put("tokeyid2", toKeyId2.substring(1));
            props.put("tokeyid3", toKeyId3.substring(1));
            props.put("relationtype", relationTypes.substring(1));
            props.put("copies", "" + copies);
            ap.processAction("AddSDI", "1", props);
        }
    }

    protected String getLogName() {
        return "AdvancedPullSample";
    }

    protected void addCustomSampleColumns(String schedulePlanId, String schedulePlanItemId, String conditionId, DataSet studyDS, PropertyList sampleProps) throws SapphireException {
    }

    protected void postAddStabilitySample(String sampleIds, String schedulePlanId, String schedulePlanItemId) throws SapphireException {
    }

    protected void preAddStabilitySample(String schedulePlanId, String schedulePlanItemId) throws SapphireException {
    }

    protected void processReuseSample(String sampleId, String workItemId, String schedulePlanId, String schedulePlanItemId, boolean firstTimePoint) throws SapphireException {
    }
}

