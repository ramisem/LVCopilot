/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.scheduler;

import com.labvantage.sapphire.actions.eln.GenerateWorkorderWorksheet;
import com.labvantage.sapphire.admin.ddt.InstrumentUtil;
import com.labvantage.sapphire.modules.wap.activity.Activity;
import com.labvantage.sapphire.modules.wap.activity.NewWorkDetails;
import com.labvantage.sapphire.modules.wap.activity.WAPCommands;
import com.labvantage.sapphire.modules.wap.activity.WAPConstants;
import com.labvantage.sapphire.scheduler.ScheduleEvent;
import com.labvantage.sapphire.stability.task.WorkOrderCallbackable;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseScheduleTask;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SDICertificationTask
extends BaseScheduleTask
implements WorkOrderCallbackable,
WAPConstants {
    public static String LOG_NAME = "SDI Certification Task";
    private PropertyListCollection createSdi;
    private PropertyList workOrder;
    private String adhocFlag = "N";
    private String certificationType = "";
    private String certificationInterval = "";
    private DBAccess db;

    @Override
    public void execute() {
        this.logTrace("Start SDI Certification Certification Task");
        try {
            long start = System.currentTimeMillis();
            SDCProcessor sdcProc = this.getSDCProcessor();
            QueryProcessor qp = this.getQueryProcessor();
            if (this.scheduleEvents != null) {
                int events = this.scheduleEvents.size();
                this.logTrace("Event List:" + this.scheduleEvents.toString());
                this.logTrace("Processing " + events + " Events with properties:" + this.scheduleProperties.toString());
                PreparedStatement insertWOCertification = this.database.prepareStatement("insert into workorderproperty(workorderid, propertyid, propertyvalue) values ( ?, ?, ? )");
                for (int i = 0; i < events; ++i) {
                    ScheduleEvent event = (ScheduleEvent)this.scheduleEvents.get(i);
                    String scheduleplanid = event.getSchedulePlanid();
                    String scheduleplanitemid = event.getSchedulePlanitemid();
                    String linksdcid = event.getLinkSdcid();
                    String linkkeyid1 = event.getLinkKeyid1();
                    String linkkeyid2 = event.getLinkKeyid2();
                    String linkkeyid3 = event.getLinkKeyid3();
                    DataSet instrumentDS = null;
                    String testingDepartmentid = "";
                    String workareaDepartmentid = "";
                    if ("Instrument".equals(linksdcid)) {
                        instrumentDS = qp.getPreparedSqlDataSet("SELECT * FROM instrument WHERE instrumentid = ?", new Object[]{linkkeyid1});
                        testingDepartmentid = instrumentDS.getValue(0, "testingdepartmentid");
                        workareaDepartmentid = instrumentDS.getValue(0, "workareadepartmentid");
                    }
                    String eventAdhocFlag = "";
                    eventAdhocFlag = this.adhocFlag != null && this.adhocFlag.equals("Y") ? "Y" : (event.isAdHocFlag() ? "Y" : "N");
                    String securityUser = "";
                    String securityDepartment = "";
                    Calendar eventdt = event.getEventDt();
                    boolean deptSecurityEnabled = InstrumentUtil.isDeptSecurityEnabled(sdcProc, linksdcid);
                    if (deptSecurityEnabled) {
                        DataSet dsSecurityInfo;
                        String keyid1Column = sdcProc.getProperty(linksdcid, "keycolid1");
                        String keyid2Column = sdcProc.getProperty(linksdcid, "keycolid2", "");
                        String keyid3Column = sdcProc.getProperty(linksdcid, "keycolid3", "");
                        SafeSQL safeSQL = new SafeSQL();
                        String sql = "SELECT securityuser, securitydepartment FROM " + linksdcid + " WHERE " + keyid1Column + " = " + safeSQL.addVar(linkkeyid1);
                        if (keyid2Column.length() > 0) {
                            sql = sql + " AND " + keyid2Column + " = " + safeSQL.addVar(linkkeyid2);
                        }
                        if (keyid3Column.length() > 0) {
                            sql = sql + " AND " + keyid3Column + " = " + safeSQL.addVar(linkkeyid3);
                        }
                        if ((dsSecurityInfo = qp.getPreparedSqlDataSet(sql, safeSQL.getValues())) != null && dsSecurityInfo.size() > 0) {
                            securityUser = dsSecurityInfo.getValue(0, "securityuser");
                            securityDepartment = dsSecurityInfo.getValue(0, "securitydepartment");
                        }
                    }
                    this.certificationType = this.scheduleProperties.getProperty("certificationtype", "");
                    this.certificationInterval = this.scheduleProperties.getProperty("certificationinterval", "");
                    this.workOrder = this.scheduleProperties.getPropertyListNotNull("workorder");
                    boolean isPlannable = testingDepartmentid.length() > 0 && this.workOrder.getProperty("isplannable").equals("Y");
                    String workorderid = this.createWorkOrder(scheduleplanid, scheduleplanitemid, linksdcid, linkkeyid1, linkkeyid2, linkkeyid3, securityUser, securityDepartment, testingDepartmentid, workareaDepartmentid, eventdt, eventAdhocFlag, isPlannable);
                    if (workorderid != null && workorderid.trim().length() > 0) {
                        insertWOCertification.setString(1, workorderid);
                        insertWOCertification.setString(2, "certificationtype");
                        insertWOCertification.setString(3, this.certificationType);
                        insertWOCertification.execute();
                    }
                    this.createSdi = this.scheduleProperties.getCollectionNotNull("createsdi");
                    if (this.createSdi.size() > 0) {
                        this.createSamples(scheduleplanid, scheduleplanitemid, workorderid, securityUser, securityDepartment, eventdt, linkkeyid1);
                    }
                    if (!"Instrument".equals(linksdcid) || instrumentDS == null || instrumentDS.size() != 1) continue;
                    String sdcid = null;
                    String keyid1 = null;
                    String keyid2 = "(null)";
                    if (instrumentDS.getValue(0, "instrumentmodelid").length() > 0) {
                        sdcid = "LV_InstrumentModel";
                        keyid1 = instrumentDS.getValue(0, "instrumentmodelid");
                        keyid2 = instrumentDS.getValue(0, "instrumenttype");
                    } else if (instrumentDS.getValue(0, "instrumenttype").length() > 0) {
                        sdcid = "LV_InstrumentType";
                        keyid1 = instrumentDS.getValue(0, "instrumenttype");
                    }
                    if (sdcid == null || keyid1 == null || keyid1.length() <= 0) continue;
                    DataSet sdiWorksheetRule = this.getWorksheetTemplate(sdcid, keyid1, keyid2);
                    if (sdiWorksheetRule.getRowCount() != 1 && instrumentDS.getValue(0, "instrumentmodelid").length() > 0) {
                        sdiWorksheetRule = this.getWorksheetTemplate("LV_InstrumentType", instrumentDS.getValue(0, "instrumenttype"), "(null)");
                    }
                    if (sdiWorksheetRule.getRowCount() == 1) {
                        PropertyList createWSProps = new PropertyList();
                        createWSProps.setProperty("workorderid", workorderid);
                        createWSProps.setProperty("worksheetrule", this.certificationInterval);
                        createWSProps.setProperty("templateid", sdiWorksheetRule.getValue(i, "worksheetid"));
                        createWSProps.setProperty("templateversionid", sdiWorksheetRule.getValue(i, "worksheetversionid"));
                        this.getActionProcessor().processActionClass(GenerateWorkorderWorksheet.class.getName(), createWSProps);
                    } else if (sdiWorksheetRule.getRowCount() > 1) {
                        throw new SapphireException(this.getTranslationProcessor().translate("More than one Worksheet Template found with the same matching criteria. Worksheet could not be created."));
                    }
                    if (!isPlannable || !this.workOrder.getProperty("createactivity").equals("Y")) continue;
                    WAPCommands commands = new WAPCommands(this.getConnectionid());
                    int duration = Integer.parseInt(this.workOrder.getProperty("duration", "30"));
                    Activity createActivity = new Activity();
                    createActivity.setMaxActivitySize(1);
                    createActivity.setMaxDurationMinutes(duration);
                    createActivity.setLabel(this.certificationInterval + " for " + linkkeyid1);
                    createActivity.setTestingDepartmentid(testingDepartmentid);
                    createActivity.setActivityClass(this.workOrder.getProperty("activityclass"));
                    createActivity.setTimeMode("Fixed");
                    Instant eventInstant = commands.calendarConverter.convertDatabaseCalendarToInstantUtc(eventdt);
                    Instant startInstant = eventInstant.plusSeconds(60L);
                    createActivity.setStartDateInstantUTC(startInstant);
                    createActivity.setEndDateInstantUTC(eventInstant);
                    String activityid = commands.createActivity(createActivity);
                    Activity activity = commands.getActivityDetails(activityid);
                    NewWorkDetails newWorkDetails = commands.addWorkSDI(activityid, "WorkOrderSDC", workorderid, "", "");
                    commands.syncActivityWorkDetails(activity, newWorkDetails, false);
                    String assignTo = this.workOrder.getProperty("assignto", "");
                    String assignToDepartment = this.workOrder.getProperty("assigneddepartment", "");
                    DataSet resources = commands.getActivityResources(activityid);
                    commands.updateResourceSDIsByInferring(activityid, resources, assignTo, "Analyst", assignToDepartment, "", "", "", "");
                    if (!this.workOrder.getProperty("autoactivate").equals("Y") || assignTo.length() <= 0 && assignToDepartment.length() <= 0) continue;
                    commands.activateActivities(activityid, "");
                }
            }
            this.logTrace("End SDI Certification Task. Took " + (System.currentTimeMillis() - start) + "ms");
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public String getTitle() {
        return "SDI Certification Task";
    }

    private void createSamples(String schedulePlanId, String schedulePlanitemId, String workorderid, String securityUser, String securityDepartment, Calendar eventDt, String instrumentid) throws SapphireException {
        HashMap<String, StringBuffer> valuemap = new HashMap<String, StringBuffer>();
        HashMap<String, String> actionProps = new HashMap<String, String>();
        StringBuffer applyworkitems = new StringBuffer();
        StringBuffer addWorkitems = new StringBuffer();
        StringBuffer addWorkitemsversionid = new StringBuffer();
        HashMap<String, String> workitemProps = new HashMap<String, String>();
        HashMap<String, String> specProps = new HashMap<String, String>();
        StringBuffer specids = new StringBuffer();
        StringBuffer specversionids = new StringBuffer();
        StringBuffer allSampleIds = new StringBuffer();
        for (int i = 0; i < this.createSdi.size(); ++i) {
            String value;
            PropertyList createSDIInfo = this.createSdi.getPropertyList(i);
            int copies = 1;
            try {
                copies = Integer.parseInt(createSDIInfo.getProperty("copies", "1"));
            }
            catch (NumberFormatException ne) {
                this.logTrace("Invalid copies found in task property.");
            }
            String templateId = createSDIInfo.getProperty("templateid", "");
            PropertyListCollection columns = createSDIInfo.getCollectionNotNull("columnvalues");
            for (int k = 0; k < copies; ++k) {
                if (columns == null || columns.size() <= 0) continue;
                for (int col = 0; col < columns.size(); ++col) {
                    String colid = columns.getPropertyList(col).getProperty("columnid", "");
                    value = columns.getPropertyList(col).getProperty("value", "");
                    value = this.replaceTokens(value, schedulePlanId, schedulePlanitemId, eventDt, workorderid);
                    String overridetemplate = columns.getPropertyList(col).getProperty("overridetemplate", "");
                    if (templateId.length() != 0 && overridetemplate.length() != 0 && !"Y".equals(overridetemplate)) continue;
                    if (!valuemap.containsKey(colid)) {
                        valuemap.put(colid, new StringBuffer());
                    }
                    ((StringBuffer)valuemap.get(colid)).append(";" + value);
                }
            }
            if (copies > 0) {
                Set keySet = valuemap.keySet();
                if (keySet.size() > 0) {
                    for (String colid : keySet) {
                        value = ((StringBuffer)valuemap.get(colid)).indexOf(";") == 0 ? ((StringBuffer)valuemap.get(colid)).substring(1).toString() : ((StringBuffer)valuemap.get(colid)).toString();
                        actionProps.put(colid, value);
                    }
                }
                try {
                    int w;
                    ActionProcessor ap = this.getActionProcessor();
                    String sdcid = "Sample";
                    actionProps.put("sdcid", sdcid);
                    if (securityUser != null && securityUser.length() > 0) {
                        actionProps.put("securityuser", securityUser);
                    }
                    if (securityDepartment != null && securityDepartment.length() > 0) {
                        actionProps.put("securitydepartment", securityDepartment);
                    }
                    actionProps.put("keyid1", "(auto)");
                    actionProps.put("copies", "" + copies);
                    if (templateId.length() > 0) {
                        actionProps.put("templateid", templateId);
                        if (!actionProps.containsKey("autoreceiveflag")) {
                            SafeSQL safeSQL = new SafeSQL();
                            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select autoreceiveflag from s_sample where s_sampleid =" + safeSQL.addVar(templateId), safeSQL.getValues());
                            if (ds != null && ds.size() > 0) {
                                actionProps.put("autoreceiveflag", ds.getString(0, "autoreceiveflag", "N"));
                            }
                        }
                    }
                    actionProps.put("eventplan", schedulePlanId);
                    actionProps.put("eventplanitem", schedulePlanitemId);
                    actionProps.put("instrumentid", instrumentid);
                    actionProps.put("workorderid", workorderid);
                    actionProps.put("classification", "Certification");
                    actionProps.put("applyworkitems", "Y");
                    actionProps.put("wapstatus", "Never");
                    ap.processAction("AddSDI", "1", actionProps);
                    String keyidList = (String)actionProps.get("newkeyid1");
                    PropertyListCollection tests = createSDIInfo.getCollectionNotNull("tests");
                    PropertyListCollection specs = createSDIInfo.getCollectionNotNull("specs");
                    allSampleIds.append(";").append(keyidList);
                    for (w = 0; w < tests.size(); ++w) {
                        String[] workitemid = StringUtil.split(tests.getPropertyList(w).getProperty("workitemid", ""), "|");
                        if (workitemid[0].length() <= 0) continue;
                        addWorkitems.append(";" + workitemid[0]);
                        addWorkitemsversionid.append(";" + (workitemid.length > 1 ? workitemid[1] : "C"));
                        applyworkitems.append(";Y");
                    }
                    if (addWorkitems.length() > 1) {
                        workitemProps.put("sdcid", sdcid);
                        workitemProps.put("keyid1", keyidList);
                        workitemProps.put("workitemid", addWorkitems.substring(1));
                        workitemProps.put("workitemversionid", addWorkitemsversionid.substring(1));
                        workitemProps.put("applyworkitem", applyworkitems.substring(1));
                        workitemProps.put("propsmatch", "N");
                        workitemProps.put("wapstatus", "Never");
                        ap.processAction("AddSDIWorkItem", "1", workitemProps);
                        if (specs.size() > 0) {
                            for (w = 0; w < specs.size(); ++w) {
                                if (specs.getPropertyList(w).getProperty("specid", "").length() <= 0) continue;
                                String[] spec = StringUtil.split(specs.getPropertyList(w).getProperty("specid"), "|");
                                specids.append(";" + spec[0]);
                                if (spec.length == 2) {
                                    specversionids.append(";" + spec[1]);
                                    continue;
                                }
                                specversionids.append(";C");
                            }
                            if (specids.length() > 1) {
                                specProps.put("sdcid", sdcid);
                                specProps.put("keyid1", keyidList);
                                specProps.put("specid", specids.substring(1));
                                specProps.put("specversionid", specversionids.substring(1));
                                ap.processAction("AddSDISpec", "1", specProps);
                            }
                        }
                    } else {
                        this.logTrace("SDI Certification Task: No test associated with the sample(s).");
                    }
                }
                catch (Exception ae) {
                    throw new SapphireException(ae);
                }
            }
            valuemap.clear();
            actionProps.clear();
            applyworkitems.setLength(0);
            addWorkitems.setLength(0);
            workitemProps.clear();
            specProps.clear();
            specids.setLength(0);
            specversionids.setLength(0);
        }
        if (allSampleIds.length() > 0) {
            String[] sampleIds = StringUtil.split(allSampleIds.substring(1), ";");
            SafeSQL safeSQL = new SafeSQL();
            DataSet dsTrackItems = this.getQueryProcessor().getPreparedSqlDataSet("SELECT trackitemid FROM trackitem  WHERE linksdcid = 'Sample' and linkkeyid1 = " + safeSQL.addVar(sampleIds[0]), safeSQL.getValues());
            if (dsTrackItems == null || dsTrackItems.size() == 0) {
                this.logTrace("No trackitem found for sample(s): " + allSampleIds.substring(1));
                this.createTrackItems(sampleIds);
            } else {
                String inClause = allSampleIds.substring(1);
                safeSQL.reset();
                int updcount = this.database.executePreparedUpdate("UPDATE trackitem set trackitemstatus = 'Valid'  WHERE linksdcid = 'Sample' and linkkeyid1 in( " + safeSQL.addIn(inClause, ";") + ")", safeSQL.getValues());
                if (updcount > 0) {
                    this.logTrace("Updated trackitem status to Valid for the sample(s) :" + allSampleIds.substring(1));
                }
            }
        }
    }

    private void createTrackItems(String[] sampleIds) throws SapphireException {
        this.logTrace("Creating trackitems......");
        PropertyList trackitemProps = new PropertyList();
        trackitemProps.setProperty("sdcid", "TrackItemSDC");
        StringBuffer sampleids = new StringBuffer();
        for (int i = 0; i < sampleIds.length; ++i) {
            sampleids.append(";").append(sampleIds[i]);
        }
        trackitemProps.setProperty("linksdcid", "Sample");
        trackitemProps.setProperty("linkkeyid1", sampleids.substring(1));
        trackitemProps.setProperty("trackitemstatus", "Valid");
        trackitemProps.setProperty("copies", "" + sampleIds.length);
        try {
            this.getActionProcessor().processAction("AddSDI", "1", trackitemProps);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to add trackitems. ", e);
        }
    }

    private String createWorkOrder(String schedulePlanId, String schedulePlanItemId, String linkSdcId, String linkKeyid1, String linkKeyid2, String linkKeyid3, String securityUser, String securityDepartment, String testingDepartmentid, String workareaDepartmentid, Calendar eventDt, String eventAdhocFlag, boolean isPlannable) throws SapphireException {
        String message = this.workOrder.getProperty("message", "");
        String assignTo = this.workOrder.getProperty("assignto", "");
        String assignToDepartment = this.workOrder.getProperty("assigneddepartment", "");
        PropertyList gracePeriodPL = this.workOrder.getPropertyListNotNull("graceperiod");
        PropertyList grcPeriodInfo = gracePeriodPL.getPropertyListNotNull("graceperiod");
        String gracePeriod = grcPeriodInfo.getProperty("grcperiod", "");
        String gracePeriodUnit = grcPeriodInfo.getProperty("grcperiodunit", "");
        PropertyList deviationInfo = gracePeriodPL.getPropertyListNotNull("deviation");
        String deviationFlag = deviationInfo.getProperty("deviationflag", "");
        String incidentTemplate = deviationInfo.getProperty("incidenttemplate", "");
        String sql = "";
        QueryProcessor qp = this.getQueryProcessor();
        M18NUtil m18NUtil = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
        if (message.length() == 0) {
            SafeSQL safeSQL = new SafeSQL();
            sql = "SELECT scheduleplanitemdesc FROM scheduleplanitem  WHERE scheduleplanid = " + safeSQL.addVar(schedulePlanId) + " AND scheduleplanitemid = " + safeSQL.addVar(schedulePlanItemId);
            DataSet dsPlanItemDesc = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (dsPlanItemDesc != null && dsPlanItemDesc.size() > 0) {
                message = dsPlanItemDesc.getValue(0, "scheduleplanitemdesc");
            }
        } else {
            message = this.replaceTokens(message, schedulePlanId, schedulePlanItemId, eventDt, "");
        }
        if (eventDt == null) {
            eventDt = Calendar.getInstance();
        }
        String duration = this.workOrder.getProperty("duration");
        HashMap<String, String> woProps = new HashMap<String, String>();
        String workOrderType = "Certification";
        woProps.put("sdcid", "WorkOrderSDC");
        woProps.put("messagetext", message);
        woProps.put("assignedto", assignTo);
        woProps.put("assigneddepartment", assignToDepartment);
        woProps.put("workorderlabel", workOrderType);
        woProps.put("workordertype", workOrderType);
        woProps.put("workorderstatus", "Pending");
        woProps.put("scheduleplanid", schedulePlanId);
        woProps.put("scheduleplanitemid", schedulePlanItemId);
        woProps.put("duedt", m18NUtil.format(eventDt));
        woProps.put("deviationflag", deviationFlag);
        woProps.put("deviationtemplateid", incidentTemplate);
        woProps.put("graceperiodtimeunit", gracePeriodUnit);
        woProps.put("graceperiod", gracePeriod);
        woProps.put("duration", duration);
        woProps.put("wapstatus", isPlannable ? "Pending" : "");
        if (securityUser != null && securityUser.length() > 0) {
            woProps.put("securityuser", securityUser);
        }
        if (securityDepartment != null && securityDepartment.length() > 0) {
            woProps.put("securitydepartment", securityDepartment);
        }
        if (testingDepartmentid != null && testingDepartmentid.length() > 0) {
            woProps.put("testingdepartmentid", testingDepartmentid);
        }
        if (workareaDepartmentid != null && workareaDepartmentid.length() > 0) {
            woProps.put("workareadepartmentid", workareaDepartmentid);
        }
        woProps.put("sourcesdcid", linkSdcId);
        woProps.put("sourcekeyid1", linkKeyid1);
        woProps.put("sourcekeyid2", linkKeyid2);
        woProps.put("sourcekeyid3", linkKeyid3);
        woProps.put("adhocflag", eventAdhocFlag);
        woProps.put("certificationinterval", this.certificationInterval);
        this.getActionProcessor().processAction("AddSDI", "1", woProps);
        return (String)woProps.get("newkeyid1");
    }

    private String createCertificateHistoricalEvent(String schedulePlanId, String schedulePlanItemId, String linkSdcId, String linkKeyid1, String linkKeyid2, String linkKeyid3, Calendar eventDt, String eventAdhocFlag) throws SapphireException {
        M18NUtil m18NUtil = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
        String eventNum = "1";
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("sdcid", "CertHistEvent");
        props.put("sourcesdcid", linkSdcId);
        props.put("sourcekeyid1", linkKeyid1);
        props.put("sourcekeyid2", linkKeyid2);
        props.put("sourcekeyid3", linkKeyid3);
        props.put("status", "Pending");
        props.put("scheduleplanid", schedulePlanId);
        props.put("scheduleplanitemid", schedulePlanItemId);
        props.put("eventdt", m18NUtil.format(eventDt));
        props.put("eventnum", eventNum);
        props.put("adhocflag", eventAdhocFlag);
        if (this.certificationType.length() > 0) {
            props.put("certificationtype", this.certificationType);
        }
        this.getActionProcessor().processAction("AddSDI", "1", props);
        String certificateEventId = (String)props.get("newkeyid1");
        return certificateEventId;
    }

    private String replaceTokens(String value, String planId, String planItemId, Calendar eventDt, String certificateEventId) {
        if (value.indexOf("[") >= 0) {
            String[] tokens = StringUtil.getTokens(value);
            for (int i = 0; i < tokens.length; ++i) {
                value = StringUtil.replaceAll(value, "[" + tokens[i] + "]", this.getTokenValue(tokens[i], planId, planItemId, eventDt, certificateEventId));
            }
        }
        return value;
    }

    private String getTokenValue(String valuefrom, String planId, String planItemId, Calendar eventDt, String certificateEventId) {
        String tempvalue = "";
        valuefrom = valuefrom.replaceAll(" ", "");
        M18NUtil m18NUtil = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
        if ("EventDate".equalsIgnoreCase(valuefrom)) {
            tempvalue = eventDt != null ? m18NUtil.format(eventDt) : m18NUtil.format(Calendar.getInstance());
        } else if ("PlanId".equalsIgnoreCase(valuefrom)) {
            tempvalue = planId;
        } else if ("PlanItemId".equalsIgnoreCase(valuefrom)) {
            tempvalue = planItemId;
        } else if ("sourcekeyid1".equalsIgnoreCase(valuefrom)) {
            tempvalue = certificateEventId;
        }
        return tempvalue;
    }

    @Override
    public void workorderCompleted(String workorderId, DBAccess database) {
    }

    @Override
    public void setAdhocFlag(String adhocFlag) {
        this.adhocFlag = adhocFlag;
    }

    @Override
    public void setDatabase(DBAccess db) {
        this.database = db;
    }

    @Override
    public DBAccess getDatabase() {
        return this.database;
    }

    private DataSet getWorksheetTemplate(String sdcid, String keyid1, String keyid2) throws SapphireException {
        Object[] objectArray;
        QueryProcessor queryProcessor = this.getQueryProcessor();
        String string = "SELECT * FROM sdiworksheetrule WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND createflag = 'C' " + (this.certificationInterval.length() > 0 ? " AND worksheetrule = ?" : " AND worksheetrule IS NULL");
        if (this.certificationInterval.length() > 0) {
            Object[] objectArray2 = new Object[4];
            objectArray2[0] = sdcid;
            objectArray2[1] = keyid1;
            objectArray2[2] = keyid2;
            objectArray = objectArray2;
            objectArray2[3] = this.certificationInterval;
        } else {
            Object[] objectArray3 = new Object[3];
            objectArray3[0] = sdcid;
            objectArray3[1] = keyid1;
            objectArray = objectArray3;
            objectArray3[2] = keyid2;
        }
        DataSet sdiWorksheetRule = queryProcessor.getPreparedSqlDataSet(string, objectArray);
        return sdiWorksheetRule;
    }
}

