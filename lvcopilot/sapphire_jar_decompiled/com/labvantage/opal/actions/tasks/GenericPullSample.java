/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions.tasks;

import com.labvantage.opal.util.StabilityUtil;
import com.labvantage.sapphire.scheduler.BaseScheduleTask;
import com.labvantage.sapphire.scheduler.ScheduleEvent;
import com.labvantage.sapphire.stability.PlanItem;
import com.labvantage.sapphire.stability.ScheduleGrid;
import com.labvantage.sapphire.stability.task.GridTask;
import com.labvantage.sapphire.stability.task.GridTaskStatus;
import com.labvantage.sapphire.stability.task.HasDetails;
import com.labvantage.sapphire.stability.task.PullAmount;
import com.labvantage.sapphire.stability.task.WorkOrderCallbackable;
import com.labvantage.sapphire.util.graceperiod.GRCPeriodUtil;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.M18NUtil;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class GenericPullSample
extends BaseScheduleTask
implements GridTask,
GridTaskStatus,
PullAmount,
WorkOrderCallbackable,
HasDetails {
    private DataSet allSamples;
    private DataSet allWorkorders;
    private DataSet allSampleSpecConditions;
    private static final String SUMMARY = "Summary";
    private static final String TESTINFO = "Tests only";
    private static final String PULLAMOUNTS = "Pull Amounts";
    private static final String FULL = "Amounts & Tests";
    private static final String WORKORDER = "Workorder Info";
    SimpleDateFormat sdf = new SimpleDateFormat();
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
    private String timerule = "";
    private boolean workOrderCompleted = false;
    private String completedWorkOrderId = "";

    @Override
    public void execute() {
        this.logger.info("Start GenericPullSample Task");
        try {
            String copiesStr;
            long start = System.currentTimeMillis();
            HashMap<String, String> props = new HashMap<String, String>();
            String sdcid = "Sample";
            props.put("sdcid", sdcid);
            props.put("keyid1", "(auto)");
            PropertyList createsdi = this.scheduleProperties.getPropertyList("createsdi");
            String deferSampleCreation = createsdi.getProperty("defersamplecreation", "");
            PropertyList workorder = this.scheduleProperties.getPropertyList("workorder");
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
            if (createsdi.getProperty("templateid").length() > 0) {
                props.put("templateid", createsdi.getProperty("templateid"));
            }
            if ((copiesStr = createsdi.getProperty("copies")).length() > 0) {
                try {
                    this.copies = Integer.parseInt(copiesStr);
                }
                catch (NumberFormatException nfe) {
                    copiesStr = "0";
                    this.copies = 0;
                }
            } else {
                copiesStr = "0";
                this.copies = 0;
            }
            int events = 1;
            QueryProcessor qp = this.getQueryProcessor();
            SafeSQL safeSQL = new SafeSQL();
            if (this.scheduleEvents != null) {
                ArrayList<String> columnidlist = new ArrayList<String>();
                HashMap<String, StringBuffer> valuemap = new HashMap<String, StringBuffer>();
                events = this.scheduleEvents.size();
                this.logger.info("Event List:" + this.scheduleEvents.toString());
                this.logger.info("Processing " + events + " Events with properties:" + this.scheduleProperties.toString());
                PropertyListCollection columns = createsdi.getCollection("columnvalues");
                boolean isOracle = this.getConnectionProcessor().isOra();
                for (int i = 0; i < events; ++i) {
                    ScheduleEvent event = (ScheduleEvent)this.scheduleEvents.get(i);
                    String scheduleplanid = event.getSchedulePlanid();
                    String scheduleplanitemid = event.getSchedulePlanitemid();
                    if (this.condition.length() == 0) {
                        safeSQL.reset();
                        String sql = "SELECT sc.conditionlabel, sc.scheduleconditionid, sr.schedulerulelabel, sr.schedulerule from scheduletimerule sr, schedulecondition sc, scheduleplanitem si where si.scheduleplanid = " + safeSQL.addVar(scheduleplanid) + " AND si.scheduleplanitemid = " + safeSQL.addVar(scheduleplanitemid) + " AND sc.scheduleplanid = si.scheduleplanid AND sr.scheduleplanid = si.scheduleplanid AND sc.scheduleconditionid = si.scheduleconditionid AND sr.scheduletimeruleid = si.scheduletimeruleid";
                        DataSet ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
                        if (ds.getRowCount() > 0) {
                            this.condition = ds.getString(0, "conditionlabel", "");
                            String string = this.timerule = ds.getString(0, "schedulerulelabel") != null ? ds.getString(0, "schedulerulelabel") : ds.getString(0, "schedulerule", "");
                        }
                    }
                    if (this.studyid.length() == 0) {
                        safeSQL.reset();
                        DataSet ds = qp.getPreparedSqlDataSet("SELECT studyid, study.s_productid, study.s_productversionid, controlsubstanceflag, cocrequiredflag, s_batchid FROM   study LEFT OUTER JOIN s_product ON study.s_productid = s_product.s_productid AND study.s_productversionid = s_product.s_productversionid WHERE  studyid IN ( SELECT studyid FROM scheduleplan sp, study_scheduleplan ss WHERE sp.scheduleplanid = " + safeSQL.addVar(scheduleplanid) + " AND sp.scheduleplanid=ss.scheduleplanid )", safeSQL.getValues());
                        if (ds.getRowCount() > 0) {
                            this.studyid = ds.getString(0, "studyid");
                            this.productid = ds.getString(0, "s_productid", "");
                            this.productversionid = ds.getString(0, "s_productversionid", "");
                            this.controlsubstanceflag = ds.getString(0, "controlsubstanceflag", "");
                            this.cocrequiredflag = ds.getString(0, "cocrequiredflag", "");
                            this.batchid = ds.getString(0, "s_batchid", "");
                        }
                    }
                    for (int copy = 0; copy < this.copies; ++copy) {
                        String value;
                        if (columns != null && columns.size() > 0) {
                            for (int col = 0; col < columns.size(); ++col) {
                                String colid = columns.getPropertyList(col).getProperty("columnid");
                                String valuefrom = columns.getPropertyList(col).getProperty("valuefrom");
                                value = columns.getPropertyList(col).getProperty("value");
                                String overridetemplate = columns.getPropertyList(col).getProperty("overridetemplate");
                                if (createsdi.getProperty("templateid").length() != 0 && overridetemplate.length() != 0 && !"Y".equals(overridetemplate)) continue;
                                if (i == 0 && copy == 0) {
                                    columnidlist.add(colid);
                                    valuemap.put(colid, new StringBuffer());
                                }
                                String tempvalue = "";
                                tempvalue = valuefrom.length() > 0 ? this.getTokenValue(valuefrom, event) : this.replaceTokens(value, event);
                                ((StringBuffer)valuemap.get(colid)).append(";" + tempvalue);
                            }
                        }
                        DataSet columnsDS = this.getSDCProcessor().getColumnData("Sample");
                        for (int col = 0; col < columnsDS.getRowCount(); ++col) {
                            String colid = columnsDS.getString(col, "columnid");
                            value = "";
                            if (colid.equals("conditionlabel")) {
                                value = this.condition;
                            } else if (colid.equals("schedulerulelabel")) {
                                value = this.timerule;
                            } else if (colid.equals("eventplan")) {
                                value = scheduleplanid;
                            } else if (colid.equals("eventplanitem")) {
                                value = scheduleplanitemid;
                            } else if (colid.equals("eventdt")) {
                                if (this.connectionInfo != null) {
                                    M18NUtil m18n = new M18NUtil(this.connectionInfo);
                                    value = m18n.format(event.getEventDt());
                                } else {
                                    value = this.sdf.format(event.getEventDt().getTime());
                                }
                            } else {
                                value = colid.equals("studyid") ? this.studyid : (colid.equals("productid") ? this.productid : (colid.equals("productversionid") ? this.productversionid : (colid.equals("controlsubstanceflag") ? this.controlsubstanceflag : (colid.equals("cocrequiredflag") ? this.cocrequiredflag : (colid.equals("batchid") ? this.batchid : this.scheduleProperties.getProperty(colid))))));
                            }
                            if (value == null || value.length() <= 0) continue;
                            if (valuemap.get(colid) == null) {
                                valuemap.put(colid, new StringBuffer(value));
                                continue;
                            }
                            ((StringBuffer)valuemap.get(colid)).append(";" + value);
                        }
                    }
                }
                Set keySet = valuemap.keySet();
                if (keySet.size() > 0) {
                    for (String colid : keySet) {
                        String value = ((StringBuffer)valuemap.get(colid)).indexOf(";") == 0 ? ((StringBuffer)valuemap.get(colid)).substring(1).toString() : ((StringBuffer)valuemap.get(colid)).toString();
                        props.put(colid, value);
                    }
                }
                props.put("copies", "" + this.copies * events);
            } else {
                props.put("copies", copiesStr);
            }
            try {
                ActionProcessor ap = this.getActionProcessor();
                String newkeyid1 = "";
                if (this.copies > 0 && (!deferSampleCreation.equalsIgnoreCase("Y") || this.workOrderCompleted)) {
                    PropertyListCollection specs;
                    if (this.workOrderCompleted && this.completedWorkOrderId.length() > 0) {
                        props.put("workorderid", this.completedWorkOrderId);
                    }
                    ap.processAction("AddSDI", "1", props);
                    newkeyid1 = (String)props.get("newkeyid1");
                    PropertyListCollection workitems = createsdi.getCollection("WorkItems");
                    if (workitems != null && workitems.size() > 0) {
                        HashMap<String, String> workitemProps = new HashMap<String, String>();
                        StringBuffer workitemids = new StringBuffer();
                        StringBuffer workitemversionids = new StringBuffer();
                        StringBuffer applyworkitems = new StringBuffer();
                        for (int w = 0; w < workitems.size(); ++w) {
                            if (workitems.getPropertyList(w).getProperty("workitemid").length() <= 0) continue;
                            applyworkitems.append(";Y");
                            String[] wi = StringUtil.split(workitems.getPropertyList(w).getProperty("workitemid"), "|");
                            workitemids.append(";" + wi[0]);
                            if (wi.length == 2) {
                                workitemversionids.append(";" + wi[1]);
                                continue;
                            }
                            workitemversionids.append(";");
                        }
                        SimpleDateFormat sdf = new SimpleDateFormat();
                        if (workitemids.length() > 1) {
                            workitemProps.put("sdcid", sdcid);
                            workitemProps.put("keyid1", newkeyid1);
                            workitemProps.put("workitemid", workitemids.substring(1));
                            workitemProps.put("workitemversionid", workitemversionids.substring(1));
                            workitemProps.put("applyworkitem", applyworkitems.substring(1));
                            ap.processAction("AddSDIWorkItem", "1", workitemProps);
                            this.logger.info("Done AddSDIWorkItem. Took " + (System.currentTimeMillis() - start) + "ms");
                        }
                    }
                    if ((specs = createsdi.getCollection("Specs")) != null && specs.size() > 0) {
                        HashMap<String, String> specProps = new HashMap<String, String>();
                        StringBuffer specids = new StringBuffer();
                        StringBuffer specversionids = new StringBuffer();
                        for (int w = 0; w < specs.size(); ++w) {
                            if (specs.getPropertyList(w).getProperty("specid").length() <= 0) continue;
                            String[] spec = StringUtil.split(specs.getPropertyList(w).getProperty("specid"), "|");
                            specids.append(";" + spec[0]);
                            specversionids.append(";" + spec[1]);
                        }
                        if (specids.length() > 1) {
                            specProps.put("sdcid", sdcid);
                            specProps.put("keyid1", newkeyid1);
                            specProps.put("specid", specids.substring(1));
                            specProps.put("specversionid", specversionids.substring(1));
                            ap.processAction("AddSDISpec", "1", specProps);
                            this.logger.info("Done AddSDISpec. Took " + (System.currentTimeMillis() - start) + "ms");
                        }
                    }
                }
                PropertyList pullamount = this.scheduleProperties.getPropertyList("pullamount");
                this.quantity = pullamount.getProperty("quantity");
                this.units = pullamount.getProperty("units");
                if ("Y".equals(workorder.getProperty("create")) && !this.workOrderCompleted) {
                    HashMap<String, String> woprops = new HashMap<String, String>();
                    woprops.put("sdcid", "WorkOrderSDC");
                    woprops.put("copies", "" + events);
                    woprops.put("eventplan", this.schedulePlanidList);
                    woprops.put("eventplanitem", this.schedulePlanitemidList);
                    String messagetext = workorder.getProperty("message");
                    messagetext = messagetext.length() == 0 ? "Pull " + this.quantity + " " + this.units + " for " + this.copies + " sample" + (this.copies == 1 ? "" : "s") + "." : this.replaceTokens(messagetext, (ScheduleEvent)this.scheduleEvents.get(0));
                    woprops.put("messagetext", messagetext);
                    woprops.put("assignedto", workorder.getProperty("assignto", ""));
                    woprops.put("assigneddepartment", workorder.getProperty("assigneddepartment", ""));
                    woprops.put("workorderlabel", "Pull");
                    woprops.put("workordertype", "Pull");
                    woprops.put("workorderstatus", "Pending");
                    woprops.put("scheduleplanid", this.schedulePlanidList);
                    woprops.put("scheduleplanitemid", this.schedulePlanitemidList);
                    woprops.put("studyid", this.studyid);
                    String grcperiod = "";
                    String earlygrcperiod = "";
                    String grcperiodunit = "";
                    DataSet ds = null;
                    String sql = "";
                    String[] arrSchdPlanIds = StringUtil.split(this.schedulePlanidList, ";");
                    String[] arrSchdPlanItemIds = StringUtil.split(this.schedulePlanitemidList, ";");
                    String[] arrEventDates = StringUtil.split(this.eventDateList, ";");
                    Calendar startDt = Calendar.getInstance();
                    Calendar eventDt = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat();
                    StringBuffer sbGrcPeriod = new StringBuffer();
                    StringBuffer sbGrcPeriodUnit = new StringBuffer();
                    StringBuffer sbEarlyGrcPeriod = new StringBuffer();
                    StringBuffer sbEventDates = new StringBuffer();
                    for (int k = 0; k < arrSchdPlanIds.length; ++k) {
                        safeSQL.reset();
                        sql = "SELECT sc.startdt from schedulecondition sc, scheduleplanitem si  where si.scheduleplanid = " + safeSQL.addVar(arrSchdPlanIds[k]) + " AND si.scheduleplanitemid = " + safeSQL.addVar(arrSchdPlanItemIds[k]) + " AND sc.scheduleplanid = si.scheduleplanid AND sc.scheduleconditionid = si.scheduleconditionid ";
                        ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
                        if (ds == null || ds.getRowCount() <= 0) continue;
                        startDt.setTime(ds.getTimestamp(0, "startdt"));
                        eventDt.setTime(sdf.parse(arrEventDates[k]));
                        if (this.connectionInfo != null) {
                            M18NUtil m18n = new M18NUtil(this.connectionInfo);
                            sbEventDates.append(m18n.format(eventDt) + ";");
                        } else {
                            sbEventDates.append(sdf.format(eventDt.getTime()) + ";");
                        }
                        String[] gracePeriod = StringUtil.split(GRCPeriodUtil.getGRCPeriodFromTaskCollection(plGracePeriodDetails, startDt, eventDt), ";");
                        try {
                            sbGrcPeriod.append(gracePeriod[0] + ";");
                            sbGrcPeriodUnit.append(gracePeriod[1] + ";");
                            sbEarlyGrcPeriod.append(gracePeriod[2] + ";");
                            continue;
                        }
                        catch (Exception e) {
                            this.logger.info(" No appropriate grace period found for the Task with scheduleplanid :" + arrSchdPlanIds[k] + " scheduleplanitemid :" + arrSchdPlanItemIds[k]);
                        }
                    }
                    if (sbGrcPeriod.length() > 0) {
                        sbGrcPeriod.setLength(sbGrcPeriod.length() - 1);
                    }
                    if (sbEarlyGrcPeriod.length() > 0) {
                        sbEarlyGrcPeriod.setLength(sbEarlyGrcPeriod.length() - 1);
                    }
                    if (sbGrcPeriodUnit.length() > 0) {
                        sbGrcPeriodUnit.setLength(sbGrcPeriodUnit.length() - 1);
                    }
                    if (sbEventDates.length() > 0) {
                        sbEventDates.setLength(sbEventDates.length() - 1);
                    }
                    grcperiod = sbGrcPeriod.toString();
                    grcperiodunit = sbGrcPeriodUnit.toString();
                    earlygrcperiod = sbEarlyGrcPeriod.toString();
                    if (grcperiod.equals("") && grcperiodunit.equals("")) {
                        grcperiod = defaultGrcPeriod;
                        earlygrcperiod = defaultEarlyGrcPeriod;
                        grcperiodunit = defaultGrcPeriodUnit;
                    }
                    woprops.put("duedt", sbEventDates.toString());
                    woprops.put("deviationflag", deviationflag);
                    woprops.put("deviationtemplateid", incidenttemplate);
                    woprops.put("graceperiodtimeunit", grcperiodunit);
                    woprops.put("graceperiod", grcperiod);
                    woprops.put("graceperiodearly", earlygrcperiod);
                    ap.processAction("AddSDI", "1", woprops);
                    String[] workorderids = StringUtil.split((String)woprops.get("newkeyid1"), ";");
                    String[] propertyids = new String[]{"quantity", "units", "copies"};
                    String[] propertyvalues = new String[]{this.quantity, this.units, "" + this.copies};
                    PreparedStatement insert = this.database.prepareStatement("insert into workorderproperty(workorderid, propertyid, propertyvalue) values ( ?, ?, ? )");
                    for (int i = 0; i < workorderids.length; ++i) {
                        insert.setString(1, workorderids[i]);
                        for (int j = 0; j < propertyids.length; ++j) {
                            insert.setString(2, propertyids[j]);
                            insert.setString(3, propertyvalues[j]);
                            insert.execute();
                        }
                    }
                    this.logger.info("Done Create WorkOrder. Took " + (System.currentTimeMillis() - start) + "ms");
                    String workOrderId = workorderids[0];
                    if (workOrderId != null && workOrderId.length() > 0 && newkeyid1.length() > 0) {
                        PropertyList editProps = new PropertyList();
                        editProps.setProperty("sdcid", "Sample");
                        editProps.setProperty("keyid1", newkeyid1);
                        editProps.setProperty("workorderid", workOrderId);
                        this.getActionProcessor().processAction("EditSDI", "1", editProps);
                    }
                }
            }
            catch (Exception ae) {
                this.logger.error(ae.getMessage(), ae);
                throw new RuntimeException(ae);
            }
            this.logger.info("End GenricPullSample Task. Took " + (System.currentTimeMillis() - start) + "ms");
        }
        catch (Exception e) {
            this.logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
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
        return new String[]{SUMMARY, TESTINFO, PULLAMOUNTS, FULL, WORKORDER};
    }

    @Override
    public String getColor() {
        return "gold";
    }

    @Override
    public String getTitle() {
        return "Pull Sample";
    }

    @Override
    public String getSummaryHTML(PropertyList propertyList, String detailLevel) {
        StringBuffer displayValue = new StringBuffer();
        if (detailLevel.equals(SUMMARY) || detailLevel.equals("")) {
            displayValue.append(this.getSummary(propertyList)).append("<br>");
        } else if (detailLevel.equals(TESTINFO)) {
            this.getTests(propertyList, displayValue);
        } else if (detailLevel.equals(PULLAMOUNTS)) {
            this.getPullAmounts(propertyList, displayValue);
        } else if (detailLevel.equals(FULL)) {
            displayValue.append(this.getSummary(propertyList)).append("<br>");
            this.getPullAmounts(propertyList, displayValue);
            this.getTests(propertyList, displayValue);
        } else if (detailLevel.equals(WORKORDER)) {
            this.getWorkorder(propertyList, displayValue);
        }
        return displayValue.toString();
    }

    @Override
    public String getSummaryText(PropertyList propertyList, String detailLevel) {
        PropertyList pullamount = propertyList.getPropertyList("pullamount");
        StringBuffer displayValue = new StringBuffer();
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
    public double getQuantity(PlanItem planItem) {
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
    public String getUnits(PlanItem planItem) {
        PropertyList pullamount = planItem.getCollapsedPropertyList().getPropertyList("pullamount");
        return pullamount.getProperty("units");
    }

    private StringBuffer getSummary(PropertyList propertyList) {
        StringBuffer output = new StringBuffer();
        PropertyList createsdi = propertyList.getPropertyListNotNull("createsdi");
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

    private void getTests(PropertyList propertyList, StringBuffer displayValue) {
        PropertyList createsdi = propertyList.getPropertyList("createsdi");
        PropertyListCollection datasets = createsdi.getCollection("WorkItems");
        if (datasets == null) {
            displayValue.append("No tests defined<br>");
        } else {
            for (PropertyList workitem : datasets) {
                String workitemid = workitem.getProperty("workitemid");
                displayValue.append(workitemid + "<br>");
            }
        }
    }

    private void getPullAmounts(PropertyList propertyList, StringBuffer displayValue) {
        PropertyList pullamount = propertyList.getPropertyList("pullamount");
        String quantity = pullamount.getProperty("quantity");
        String units = pullamount.getProperty("units");
        if (quantity.length() > 0) {
            displayValue.append(quantity).append(units);
        } else {
            displayValue.append("<span style='color:red'>Not Defined</span>");
        }
        displayValue.append("<br>");
    }

    private void getWorkorder(PropertyList propertyList, StringBuffer displayValue) {
        PropertyList workorder = propertyList.getPropertyList("workorder");
        if (workorder.getProperty("create").equals("Y")) {
            String assignTo = workorder.getProperty("assignto");
            String daysEarly = workorder.getProperty("daysearly");
            String message = workorder.getProperty("message");
            displayValue.append(message.length() > 0 ? message : "No message");
            if (assignTo.length() > 0) {
                displayValue.append("<br>Assign To: " + assignTo);
            }
            if (daysEarly.length() > 0 && !daysEarly.equals("0")) {
                displayValue.append("<br>" + daysEarly + " day" + (!daysEarly.equals("1") ? "s" : "") + " early");
            }
        } else {
            displayValue.append("Not Generated");
        }
    }

    @Override
    public void statusInit(ScheduleGrid grid, PropertyList propertyList) {
        SafeSQL safeSQL = new SafeSQL();
        String sampleSQL = "SELECT s_sampleid, samplestatus, eventplan, eventplanitem FROM s_sample where eventplan = " + safeSQL.addVar(grid.planid);
        this.allSamples = grid.queryProcessor.getPreparedSqlDataSet(sampleSQL, safeSQL.getValues());
        safeSQL.reset();
        String workorderSQL = "SELECT workorderid, workorderstatus, scheduleplanid, scheduleplanitemid FROM workorder where scheduleplanid = " + safeSQL.addVar(grid.planid);
        this.allWorkorders = grid.queryProcessor.getPreparedSqlDataSet(workorderSQL, safeSQL.getValues());
        safeSQL.reset();
        String specSql = "SELECT s_sampleid keyid1, (case  when (select count(1) from sdispec where sdcid = 'Sample' and keyid1 = s_sample.s_sampleid and (condition is not null  OR condition != '' ) and oosgeneratingflag  != 'N') < 1 then '' ELSE  (select refvalueid from refvalue where reftypeid = 'Spec Condition' and usersequence = (select  max(r.usersequence)   FROM refvalue r, sdispec sp  where r.reftypeid = 'Spec Condition' and r.refvalueid = sp.condition and sp.sdcid = 'Sample' and sp.keyid1 = s_sample.s_sampleid and  sp.oosgeneratingflag  != 'N' ) ) END ) worstoosspeccondition FROM s_sample WHERE s_sample.eventplan = " + safeSQL.addVar(grid.planid);
        this.allSampleSpecConditions = grid.queryProcessor.getPreparedSqlDataSet(specSql, safeSQL.getValues());
    }

    @Override
    public String getStatusHTML(PlanItem planItem, String detailLevel) {
        int i;
        StringBuffer output = new StringBuffer();
        HashMap<String, String> sampleFilter = new HashMap<String, String>();
        sampleFilter.put("eventplan", planItem.grid.planid);
        sampleFilter.put("eventplanitem", planItem.planItemid);
        DataSet samples = this.allSamples.getFilteredDataSet(sampleFilter);
        if (samples.size() > 0) {
            TranslationProcessor tp = new TranslationProcessor(planItem.grid.getPageContext());
            DataSet specInterpretaion = new DataSet();
            try {
                specInterpretaion = StabilityUtil.getSpecConditionRefTypeData(new QueryProcessor(planItem.grid.getConnectionId()));
            }
            catch (Exception e) {
                this.logger.info("GENERAL_ERROR", "SpecConditions policy could not be interpreted.");
            }
            output.append("<table border=0 cellspacing=0 cellpadding=0>");
            for (i = 0; i < samples.size(); ++i) {
                String sampleid = samples.getValue(i, "s_sampleid");
                String samplestatus = samples.getValue(i, "samplestatus");
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("keyid1", sampleid);
                DataSet worstOOSSpec = this.allSampleSpecConditions.getFilteredDataSet(filter);
                output.append("<tr>");
                output.append("<td><img src='WEB-OPAL/pagetypes/stability/images/sample.gif'></td><td>").append(StabilityUtil.getSpecHtml(worstOOSSpec, specInterpretaion, sampleid, tp)).append("</td><td><a target='taskdetails' href='rc?command=page&page=SampleGridStatus&keyid1=" + HttpUtil.encodeURIComponent(sampleid) + "'>" + sampleid + "</a>&nbsp;(" + samplestatus + ")</td>");
                output.append("</tr>");
            }
            output.append("</table>");
        }
        HashMap<String, String> workorderFilter = new HashMap<String, String>();
        workorderFilter.put("scheduleplanid", planItem.grid.planid);
        workorderFilter.put("scheduleplanitemid", planItem.planItemid);
        DataSet workorder = this.allWorkorders.getFilteredDataSet(workorderFilter);
        if (workorder.size() > 0) {
            for (i = 0; i < workorder.size(); ++i) {
                String workorderid = workorder.getValue(i, "workorderid");
                String workorderstatus = workorder.getValue(i, "workorderstatus");
                output.append("<img src='WEB-OPAL/pagetypes/stability/images/workorder.gif'>&nbsp;<a target='taskdetails' href='rc?command=page&page=WorkorderGridStatus&keyid1=" + HttpUtil.encodeURIComponent(workorderid) + "'>" + workorder.getValue(i, "workorderid") + "</a>&nbsp;(" + workorderstatus + ")<br>");
            }
        }
        return output.toString();
    }

    public String[] getMenuTitles() {
        return new String[]{"Edit Tests", "Edit Specs"};
    }

    public String getCollectionEditorTitles(int index) {
        StringBuffer output = new StringBuffer();
        return output.toString();
    }

    public String getCollectionEditorCells(int index) {
        StringBuffer output = new StringBuffer();
        return output.toString();
    }

    @Override
    public boolean isComplete(String planid, String planitemid, DBAccess database) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT COUNT(1) FROM s_sample");
        sql.append(" WHERE eventplan = ? AND  eventplanitem =?");
        sql.append(" AND  ( samplestatus is null or samplestatus != 'Reviewed' )");
        try {
            if (database.getPreparedCount(sql.toString(), new Object[]{planid, planitemid}) > 0) {
                return false;
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        return true;
    }

    @Override
    public void workorderCompleted(String workorderId, DBAccess db) {
        try {
            PropertyList createsdi;
            String deferSampleCreation;
            if (this.scheduleEvents != null && this.scheduleEvents.size() > 0 && (deferSampleCreation = (createsdi = this.scheduleProperties.getPropertyList("createsdi")).getProperty("defersamplecreation", "")).equalsIgnoreCase("Y")) {
                this.workOrderCompleted = true;
                this.completedWorkOrderId = workorderId;
                this.logger.info("Deferred samples are going to be created now for WorkOrder Id :" + workorderId);
                this.execute();
                this.logger.info("Deferred samples got created sucessfully.");
            }
        }
        catch (Exception ex) {
            this.logger.error("GenericPullSample -> Could not process method workorderCompleted:  " + ex.getMessage(), ex);
        }
    }

    @Override
    public HashMap getDetails(PropertyList properties) {
        HashMap<String, String> details = new HashMap<String, String>();
        PropertyList pullAmount = properties.getPropertyListNotNull("pullamount");
        String quantity = pullAmount.getProperty("quantity");
        String units = pullAmount.getProperty("units");
        if (quantity.length() > 0) {
            details.put("quantity", units.length() > 0 ? " " + units : "");
        }
        PropertyList createSDI = properties.getPropertyListNotNull("createsdi");
        details.put("copies", createSDI.getProperty("copies"));
        details.put("templateid", createSDI.getProperty("templateid"));
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
        return details;
    }
}

