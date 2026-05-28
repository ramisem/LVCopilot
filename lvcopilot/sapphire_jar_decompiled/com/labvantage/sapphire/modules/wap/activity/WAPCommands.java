/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.wap.activity;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.wap.CalendarConverter;
import com.labvantage.sapphire.modules.wap.activity.Activity;
import com.labvantage.sapphire.modules.wap.activity.ActivityClassHandler;
import com.labvantage.sapphire.modules.wap.activity.ContextMap;
import com.labvantage.sapphire.modules.wap.activity.NewWorkDetails;
import com.labvantage.sapphire.modules.wap.activity.WAPAvailability;
import com.labvantage.sapphire.modules.wap.activity.WAPConstants;
import com.labvantage.sapphire.modules.wap.activity.WAPSelector;
import com.labvantage.sapphire.modules.wap.calendar.CalendarFactory;
import com.labvantage.sapphire.modules.wap.calendar.LVCalendar;
import com.labvantage.sapphire.modules.wap.workhours.WorkHours;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class WAPCommands
extends BaseCustom
implements WAPConstants {
    public final CalendarConverter calendarConverter;
    private DateTimeUtil dtu;
    private SimpleDateFormat actionDateFormatter;
    private static final String LOGNAME = "WAPCommands";
    private HashMap<String, String> departmentWorkareaCache = new HashMap();
    private String databaseid;
    private PropertyList wapPolicy = null;
    private boolean isOracle;

    public WAPCommands(String connectionid) {
        this.setConnectionId(connectionid);
        ConnectionProcessor cp = this.getConnectionProcessor();
        ConnectionInfo connectionInfo = cp.getConnectionInfo(connectionid);
        this.dtu = new DateTimeUtil(connectionInfo);
        this.actionDateFormatter = (SimpleDateFormat)DateFormat.getDateTimeInstance(2, 2, this.dtu.getLocale());
        this.databaseid = connectionInfo.getDatabaseId();
        this.isOracle = connectionInfo.isOracle();
        this.calendarConverter = new CalendarConverter(this.dtu);
    }

    public WAPCommands(String connectionid, File rakFile) {
        this.setConnectionId(connectionid);
        this.setRakFile(rakFile);
        ConnectionProcessor cp = this.getConnectionProcessor();
        ConnectionInfo connectionInfo = cp.getConnectionInfo(connectionid);
        this.dtu = new DateTimeUtil(connectionInfo);
        this.actionDateFormatter = (SimpleDateFormat)DateFormat.getDateTimeInstance(2, 2, this.dtu.getLocale());
        this.databaseid = connectionInfo.getDatabaseId();
        this.isOracle = connectionInfo.isOracle();
        this.calendarConverter = new CalendarConverter(this.dtu);
    }

    public String createActivity(Activity activity) throws SapphireException {
        Trace.logInfo(LOGNAME, "Creating new activity with label " + activity.getLabel());
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_Activity");
        props.setProperty("activitydesc", activity.getLabel());
        props.setProperty("worksdcid", activity.getWorksdcid());
        props.setProperty("workcontext", activity.getWorkContext());
        props.setProperty("testingdepartmentid", activity.getTestingDepartmentid());
        props.setProperty("activitysize", "" + activity.getActivitySize());
        props.setProperty("maxactivitysize", "" + activity.getMaxActivitySize());
        props.setProperty("maxduration", "" + activity.getMaxDurationMinutes());
        props.setProperty("timemode", activity.getTimeMode());
        props.setProperty("activitystatus", activity.getStatus().length() == 0 ? "Draft" : activity.getStatus());
        props.setProperty("activityclass", activity.getActivityClass());
        props.setProperty("activitycontextsdcid", activity.getActivityContextSdcid());
        props.setProperty("activitycontextkeyid1", activity.getActivityContextKeyid1());
        props.setProperty("activitycontextkeyid2", activity.getActivityContextKeyid2());
        props.setProperty("activitycontextkeyid3", activity.getActivityContextKeyid3());
        if (activity.getTimeMode().equals("Fixed") || activity.getTimeMode().equals("Floating")) {
            if (activity.getStartDateInstantUTC() == null) {
                if (activity.getTimeMode().equals("Fixed")) {
                    throw new SapphireException("You must supply a startdt for Fixed timemode");
                }
            } else {
                props.setProperty("startdt", this.calendarConverter.convertInstantUtcToUserActionDateString(activity.getStartDateInstantUTC()));
                if (activity.getEndDateInstantUTC() != null) {
                    props.setProperty("enddt", this.calendarConverter.convertInstantUtcToUserActionDateString(activity.getEndDateInstantUTC()));
                    props.setProperty("enddtfixedflag", "Y");
                }
            }
        }
        if (activity.isReservation()) {
            props.setProperty("reservationflag", "Y");
            props.setProperty("reservationtype", activity.getReservationType());
            props.setProperty("reservationcontext", activity.getReservationContext());
        }
        if (activity.getTimeMode().equals("Floating")) {
            if (activity.getStartRangeInstantUTC() == null || activity.getEndRangeInstantUTC() == null) {
                throw new ActionException("You must specify a startrangedt and an endrangedt for Floatingtime mode");
            }
            props.setProperty("startrangedt", this.calendarConverter.convertInstantUtcToUserActionDateString(activity.getStartRangeInstantUTC()));
            props.setProperty("endrangedt", this.calendarConverter.convertInstantUtcToUserActionDateString(activity.getEndRangeInstantUTC()));
        }
        this.getActionProcessor().processAction("AddSDI", "1", props);
        return props.getProperty("newkeyid1");
    }

    public String getCertifiedUsers(String userlist, String[] paramlistidVersionVariant) throws SapphireException {
        String certifiedUsers = userlist;
        for (int i = 0; i < paramlistidVersionVariant.length && certifiedUsers.length() > 0; ++i) {
            String[] parts = StringUtil.split(paramlistidVersionVariant[i], ";");
            String paramlistid = parts[0];
            String paramlistversionid = parts[1];
            String variantid = parts[2];
            StringBuilder sql = new StringBuilder();
            SafeSQL safeSQL = new SafeSQL();
            int count = this.getQueryProcessor().getPreparedCount("SELECT count(*) FROM paramlist WHERE s_trainingreqflag='Y' AND paramlistid=" + safeSQL.addVar(paramlistid) + " AND paramlistversionid=" + safeSQL.addVar(paramlistversionid) + " AND variantid=" + safeSQL.addVar(variantid), safeSQL.getValues());
            if (count != 1) continue;
            safeSQL.reset();
            sql.append("SELECT resourcekeyid1 FROM s_sdicertification  ");
            sql.append(" WHERE certificationstatus IN ('Valid', 'In Training') ");
            sql.append(" AND resourcesdcid = 'User' ");
            sql.append(" AND resourcekeyid1 IN ( " + safeSQL.addIn(certifiedUsers, ";") + " ) ");
            sql.append(" AND certificationtype = 'Analyst Training' ");
            sql.append(" AND certifiedforsdcid = 'ParamList' ");
            sql.append(" AND certifiedforkeyid1 = " + safeSQL.addVar(paramlistid));
            sql.append(" AND ( certifiedforkeyid2 = " + safeSQL.addVar(paramlistversionid) + " OR certifiedforkeyid2='(null)' ) ");
            sql.append(" AND ( certifiedforkeyid3 = " + safeSQL.addVar(variantid) + " OR certifiedforkeyid3='(null)' ) ");
            if (this.isOracle) {
                sql.append(" AND ( expirationdt IS NULL  ");
                sql.append("     OR  ( SYSDATE < DECODE(graceperiodunits, 'Days', EXPIRATIONDT + NVL(GRACEPERIOD, 0),  ");
                sql.append("                                       'Weeks', EXPIRATIONDT + 7 * NVL(GRACEPERIOD, 0),  ");
                sql.append("                                       'Months', ADD_MONTHS(EXPIRATIONDT, NVL(GRACEPERIOD, 0)),  ");
                sql.append("                                       'Years', ADD_MONTHS(EXPIRATIONDT, 12 * NVL(GRACEPERIOD, 0)),  ");
                sql.append("                                       expirationdt)  ");
                sql.append("            ) ");
                sql.append("     ) ");
            } else {
                sql.append("AND ( expirationdt IS NULL  ");
                sql.append("     OR  ( GETDATE() < CASE  graceperiodunits  ");
                sql.append("                       \tWHEN 'Days' THEN DATEADD( DAY, ISNULL(GRACEPERIOD,0),EXPIRATIONDT) ");
                sql.append("                              WHEN 'Weeks' THEN DATEADD( WEEK, ISNULL(GRACEPERIOD,0),EXPIRATIONDT) ");
                sql.append("                              WHEN 'Months' THEN DATEADD( MONTH, ISNULL(GRACEPERIOD,0),EXPIRATIONDT) ");
                sql.append("                              WHEN 'Years' THEN DATEADD( YEAR, ISNULL(GRACEPERIOD,0),EXPIRATIONDT) ");
                sql.append("\t\t\t\t                ELSE expirationdt ");
                sql.append("\t\t\t              END ");
                sql.append("          ) ");
                sql.append("     ) ");
            }
            safeSQL.setPreparedSQL(sql.toString());
            certifiedUsers = this.getQueryProcessor().getPreparedSqlDataSet(safeSQL.getPreparedSQL(), safeSQL.getValues()).getColumnValues("resourcekeyid1", ";");
        }
        return certifiedUsers;
    }

    public boolean prepareEditActivityDates(Activity editActivity, Activity currentActivity, PropertyList props) throws SapphireException {
        Trace.logInfo(LOGNAME, "Preparing activity " + editActivity.getActivityid());
        boolean calculatedEndDate = false;
        if (editActivity.getStartDateInstantUTC() == null) {
            props.setProperty("startdt", "(null)");
            props.setProperty("enddt", "(null)");
            if (editActivity.isEndDateFixed() != currentActivity.isEndDateFixed()) {
                props.setProperty("enddtfixedflag", editActivity.isEndDateFixed() ? "Y" : "N");
            }
            if (editActivity.getStartRangeInstantUTC() != null) {
                props.setProperty("startrangedt", this.calendarConverter.convertInstantUtcToUserActionDateString(editActivity.getStartRangeInstantUTC()));
            }
            if (editActivity.getEndRangeInstantUTC() != null) {
                props.setProperty("endrangedt", this.calendarConverter.convertInstantUtcToUserActionDateString(editActivity.getEndRangeInstantUTC()));
            } else if (currentActivity.getEndDateInstantUTC() != null) {
                props.setProperty("endrangedt", this.calendarConverter.convertInstantUtcToUserActionDateString(currentActivity.getEndDateInstantUTC()));
            }
        } else {
            props.setProperty("startdt", this.calendarConverter.convertInstantUtcToUserActionDateString(editActivity.getStartDateInstantUTC()));
            if (editActivity.getEndDateInstantUTC() != null) {
                props.setProperty("enddt", this.calendarConverter.convertInstantUtcToUserActionDateString(editActivity.getEndDateInstantUTC()));
                props.setProperty("enddtfixedflag", "Y");
            } else if (currentActivity.getStartDateInstantUTC() != null && currentActivity.getEndDateInstantUTC() != null && currentActivity.isEndDateFixed()) {
                long seconds = ChronoUnit.SECONDS.between(currentActivity.getStartDateInstantUTC(), currentActivity.getEndDateInstantUTC());
                Instant enddt = editActivity.getStartDateInstantUTC().plus(seconds, ChronoUnit.SECONDS);
                props.setProperty("enddt", this.calendarConverter.convertInstantUtcToUserActionDateString(enddt));
                props.setProperty("enddtfixedflag", "Y");
            } else {
                calculatedEndDate = true;
                props.setProperty("enddt", "(null)");
            }
            props.setProperty("startrangedt", "(null)");
            props.setProperty("endrangedt", "(null)");
        }
        return calculatedEndDate;
    }

    public void editActivity(Activity editActivity, boolean updateDates) throws SapphireException {
        Trace.logInfo(LOGNAME, "Editing activity " + editActivity.getActivityid());
        Activity current = this.getActivityDetails(editActivity.getActivityid());
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_Activity");
        props.setProperty("keyid1", editActivity.getActivityid());
        if (editActivity.getStatus().length() > 0) {
            throw new SapphireException("You cannot change the status of an activity using this command. Try calling the setActivityStatus command instead");
        }
        if (editActivity.getActivityContextSdcid().length() > 0) {
            props.setProperty("activitycontextsdcid", editActivity.getActivityContextSdcid());
            props.setProperty("activitycontextkeyid1", editActivity.getActivityContextKeyid1());
            props.setProperty("activitycontextkeyid2", editActivity.getActivityContextKeyid2());
            props.setProperty("activitycontextkeyid3", editActivity.getActivityContextKeyid3());
        }
        if (editActivity.isReservationChange()) {
            if (editActivity.isReservation()) {
                props.setProperty("reservationflag", "Y");
                props.setProperty("reservationtype", editActivity.getReservationType());
                props.setProperty("reservationcontext", editActivity.getReservationContext());
            } else {
                props.setProperty("reservationflag", "N");
            }
        }
        if (editActivity.getActivityClass().length() > 0) {
            props.setProperty("activityclass", editActivity.getActivityClass());
        }
        if (editActivity.getLabel().length() > 0) {
            props.setProperty("activitydesc", editActivity.getLabel());
        }
        if (editActivity.getWorksdcid().length() > 0) {
            props.setProperty("worksdcid", editActivity.getWorksdcid());
        }
        if (editActivity.getWorkContext().length() > 0) {
            props.setProperty("workcontext", editActivity.getWorkContext());
        }
        if (editActivity.getTestingDepartmentid().length() > 0) {
            props.setProperty("testingdepartmentid", editActivity.getTestingDepartmentid());
        }
        if (editActivity.getEditActivitySize() > -1) {
            props.setProperty("activitysize", "" + editActivity.getActivitySize());
        }
        if (editActivity.getEditMaxActivitySize() > -1) {
            props.setProperty("maxactivitysize", "" + editActivity.getMaxActivitySize());
        }
        boolean calculatedEndDate = false;
        if (updateDates) {
            calculatedEndDate = this.prepareEditActivityDates(editActivity, current, props);
        }
        this.getActionProcessor().processAction("EditSDI", "1", props);
        if (calculatedEndDate) {
            Activity activity = this.getActivityDetails(editActivity.getActivityid());
            this.syncMaxDurationEndDateDueDateCompleteCount(activity);
        }
    }

    public void deleteActivities(String activitylist) throws SapphireException {
        String[] activities = StringUtil.split(activitylist, ";");
        if (activities.length > 1000) {
            throw new SapphireException("This operation can only be performed on up to 1000 Activities at a time");
        }
        for (String activityid : activities) {
            DataSet work = this.getActivityWorkSDIs(activityid);
            if (work.size() <= 0) continue;
            this.setWAPStatus(work.getString(0, "worksdcid"), work.getColumnValues("workkeyid1", ";"), work.getColumnValues("workkeyid2", ";"), work.getColumnValues("workkeyid3", ";"), "Pending", true);
        }
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_Activity");
        props.setProperty("keyid1", activitylist);
        this.getActionProcessor().processAction("DeleteSDI", "1", props);
    }

    public void cancelActivities(String activitylist, String tracelogid) throws SapphireException {
        String[] activities = StringUtil.split(activitylist, ";");
        if (activities.length > 1000) {
            throw new SapphireException("This operation can only be performed on up to 1000 Activities at a time");
        }
        DataSet statuses = this.getActivityStatusList(activitylist);
        if (statuses.findRow("activitystatus", "Completed") >= 0) {
            throw new SapphireException("This operation can not be performed on completed activities");
        }
        if (statuses.findRow("activitystatus", "Cancelled") >= 0) {
            throw new SapphireException("This operation can not be performed on cancelled activities");
        }
        for (String activityid : activities) {
            DataSet work = this.getActivityWorkSDIs(activityid);
            if (work.size() <= 0) continue;
            this.setWAPStatus(work.getString(0, "worksdcid"), work.getColumnValues("workkeyid1", ";"), work.getColumnValues("workkeyid2", ";"), work.getColumnValues("workkeyid3", ";"), "Pending", true);
        }
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_Activity");
        props.setProperty("keyid1", activitylist);
        props.setProperty("tracelogid", tracelogid);
        props.setProperty("activitystatus", "Cancelled");
        props.setProperty("cancelleddt", "n");
        this.getActionProcessor().processAction("EditSDI", "1", props);
    }

    public void startActivities(String activitylist, String tracelogid) throws SapphireException {
        String[] activities = StringUtil.split(activitylist, ";");
        if (activities.length > 1000) {
            throw new SapphireException("This operation can only be performed on up to 1000 Activities at a time");
        }
        DataSet statuses = this.getActivityStatusList(activitylist);
        if (statuses.findRow("activitystatus", "Cancelled") >= 0) {
            throw new SapphireException("This operation can not be performed on cancelled activities");
        }
        if (statuses.findRow("activitystatus", "In Progress") >= 0) {
            throw new SapphireException("This operation can not be performed on activities already in progress");
        }
        boolean restarting = false;
        if (statuses.findRow("activitystatus", "Completed") >= 0) {
            int i;
            restarting = true;
            for (i = 0; i < statuses.size(); ++i) {
                if (statuses.getValue(i, "activitystatus").equals("Completed")) continue;
                throw new SapphireException("Restarting a completed activity cannot be performed alongside other activities.");
            }
            for (i = 0; i < statuses.size(); ++i) {
                int completeCount;
                String activityid = statuses.getString(i, "activityid");
                Activity activity = this.getActivityDetails(activityid);
                ActivityClassHandler activityClassHandler = ActivityClassHandler.getInstance(this.getConnectionid(), this.getWapPolicy(), activity.getActivityClass(), activity.getWorksdcid());
                if (!activityClassHandler.isAutoComplete() || (completeCount = this.getWorkCompleteCount(activity)) != activity.getActivitySize()) continue;
                throw new SapphireException("Cannot restart activities as one or more of them will immediately auto-complete.");
            }
        }
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("activitystatus", "Draft");
        DataSet pendingActivation = statuses.getFilteredDataSet(filter);
        if (pendingActivation.size() > 0) {
            String activateList = pendingActivation.getColumnValues("activityid", ";");
            this.activateActivities(activateList, tracelogid);
        }
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_Activity");
        props.setProperty("keyid1", activitylist);
        props.setProperty("activitystatus", "In Progress");
        if (!restarting) {
            props.setProperty("actualstartdt", "n");
        }
        props.setProperty("actualenddt", "(null)");
        props.setProperty("tracelogid", tracelogid);
        this.getActionProcessor().processAction("EditSDI", "1", props);
    }

    public void stopActivities(String activitylist, String tracelogid, PropertyList actionProps) throws SapphireException {
        String[] activities = StringUtil.split(activitylist, ";");
        if (activities.length > 1000) {
            throw new SapphireException("This operation can only be performed on up to 1000 Activities at a time");
        }
        DataSet statuses = this.getActivityStatusList(activitylist);
        if (statuses.findRow("activitystatus", "Cancelled") >= 0) {
            throw new SapphireException("This operation can not be performed on cancelled activities");
        }
        if (statuses.findRow("activitystatus", "Completed") >= 0) {
            throw new SapphireException("This operation can not be performed on activities already completed");
        }
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_Activity");
        props.setProperty("keyid1", activitylist);
        props.setProperty("activitystatus", "Completed");
        props.setProperty("actualenddt", actionProps.getProperty("actualenddt", "n"));
        props.setProperty("tracelogid", tracelogid);
        this.getActionProcessor().processAction("EditSDI", "1", props);
    }

    public void activateActivities(String activitylist, String tracelogid) throws SapphireException {
        String[] activities = StringUtil.split(activitylist, ";");
        if (activities.length > 1000) {
            throw new SapphireException("This operation can only be performed on up to 1000 Activities at a time");
        }
        for (String activityid : activities) {
            String[] resources = this.getActivityResources(activityid);
            for (int i = 0; i < resources.size(); ++i) {
                if (!(resources.getValue(i, "resourcetypeflag").equals("A") ? resources.getValue(i, "analystid").length() == 0 && resources.getValue(i, "workareadepartmentid").length() == 0 : resources.getValue(i, "resourcetypeflag").equals("I") && resources.getValue(i, "instrumentid").length() == 0 && resources.getValue(i, "workareadepartmentid").length() == 0)) continue;
                throw new SapphireException("Unable to activate because not all resource requirements have been assigned.");
            }
        }
        DataSet statuses = this.getActivityStatusList(activitylist);
        if (statuses.findRow("activitystatus", "Cancelled") >= 0) {
            throw new SapphireException("This operation can not be performed on cancelled activities");
        }
        if (statuses.findRow("activitystatus", "Completed") >= 0) {
            throw new SapphireException("This operation can not be performed on completed activities");
        }
        if (statuses.findRow("activitystatus", "In Progress") >= 0) {
            throw new SapphireException("This operation can not be performed on in progress activities");
        }
        String activatedList = this.activateActivity(activitylist, tracelogid, true);
        this.pushDownPlannedStartDt(activatedList, true);
        StringBuffer activitystatus = new StringBuffer();
        StringBuffer actualstartdt = new StringBuffer();
        for (String activityid : activities) {
            if (this.isActivityLIMSWorkStarted(activityid)) {
                activitystatus.append(";").append("In Progress");
                actualstartdt.append(";").append("n");
                continue;
            }
            activitystatus.append(";").append("Activated");
            actualstartdt.append(";").append("(null)");
        }
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_Activity");
        props.setProperty("keyid1", activitylist);
        props.setProperty("activitystatus", activitystatus.substring(1));
        props.setProperty("activateddt", "n");
        if (activitystatus.indexOf("In Progress") != -1) {
            props.setProperty("actualstartdt", actualstartdt.substring(1));
        }
        props.setProperty("tracelogid", tracelogid);
        this.getActionProcessor().processAction("EditSDI", "1", props);
    }

    public void pushDownPlannedStartDt(String activitylist, boolean activate) {
        if (activitylist != null && activitylist.length() > 0) {
            QueryProcessor qp = this.getQueryProcessor();
            try {
                if (activate) {
                    qp.execPreparedUpdate("{call lv_activity" + (this.isOracle ? "." : "_") + "ActivateList( ?,? ) }", new Object[]{activitylist, null});
                } else {
                    qp.execPreparedUpdate("{call lv_activity" + (this.isOracle ? "." : "_") + "UnActivateList( ? ) }", new Object[]{activitylist});
                }
            }
            catch (Exception e) {
                this.logger.warn("Failed to synchronize planned startdt for activities " + activitylist + ": " + e.getMessage() + ". Continuing with Activate operation anyway.");
            }
        }
    }

    public void unactivateActivities(String activitylist, String tracelogid) throws SapphireException {
        String[] activities = StringUtil.split(activitylist, ";");
        if (activities.length > 1000) {
            throw new SapphireException("This operation can only be performed on up to 1000 Activities at a time");
        }
        DataSet statuses = this.getActivityStatusList(activitylist);
        if (statuses.findRow("activitystatus", "Draft") >= 0) {
            throw new SapphireException("This operation can not be performed on draft activities");
        }
        if (statuses.findRow("activitystatus", "Cancelled") >= 0) {
            throw new SapphireException("This operation can not be performed on cancelled activities");
        }
        if (statuses.findRow("activitystatus", "Completed") >= 0) {
            throw new SapphireException("This operation can not be performed on completed activities");
        }
        if (statuses.findRow("activitystatus", "In Progress") >= 0) {
            throw new SapphireException("This operation can not be performed on in progress activities");
        }
        String unactivatedList = this.activateActivity(activitylist, tracelogid, false);
        this.pushDownPlannedStartDt(unactivatedList, false);
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_Activity");
        props.setProperty("keyid1", activitylist);
        props.setProperty("activitystatus", "Draft");
        props.setProperty("activateddt", "(null)");
        props.setProperty("tracelogid", tracelogid);
        this.getActionProcessor().processAction("EditSDI", "1", props);
    }

    private String activateActivity(String activityList, String tracelogid, boolean activate) throws SapphireException {
        String[] activatelist = StringUtil.split(activityList, ";");
        String activatedList = "";
        DataSet editSample = new DataSet();
        DataSet editSDIWorkItem = new DataSet();
        DataSet editSDIData = new DataSet();
        DataSet editSDIWorkItemRelation = new DataSet();
        DataSet editSDIDataRelation = new DataSet();
        QueryProcessor qp = this.getQueryProcessor();
        ActionProcessor ap = this.getActionProcessor();
        HashMap<String, DataSet> customEdits = new HashMap<String, DataSet>();
        for (int i = 0; i < activatelist.length; ++i) {
            String activityid = activatelist[i];
            Activity activity = this.getActivityDetails(activityid);
            DataSet resources = this.getActivityResources(activityid);
            String workSDCId = activity.getWorksdcid();
            if (workSDCId.length() <= 0 || activity.getActivitySize() <= 0) continue;
            activatedList = activatedList + ";" + activityid;
            for (int j = 0; j < resources.size(); ++j) {
                DataSet ds;
                DataSet dsRelation;
                DataSet dsWIRelation;
                String resourceType = resources.getValue(j, "resourcetypeflag");
                String analysttype = resources.getValue(j, "analysttype");
                if ("A".equalsIgnoreCase(resourceType) && analysttype.equals("Analyst")) {
                    String workareadepartmentId;
                    String analystid;
                    if (activate) {
                        analystid = resources.getValue(j, "analystid");
                        workareadepartmentId = resources.getValue(j, "workareadepartmentid");
                    } else {
                        analystid = "(null)";
                        workareadepartmentId = "(null)";
                    }
                    DataSet dsTempSamples = new DataSet();
                    DataSet dsTempSDIWorkitem = new DataSet();
                    DataSet dsTemplSDIData = new DataSet();
                    if (workSDCId.equals("Sample")) {
                        dsTempSamples = qp.getPreparedSqlDataSet("SELECT activityworksdi.workkeyid1 FROM activityworksdi WHERE activityworksdi.activityid=? AND activityworksdi.worksdcid=?", (Object[])new String[]{activityid, "Sample"});
                        dsTempSDIWorkitem = qp.getPreparedSqlDataSet("SELECT sdiworkitemid, sdcid, keyid1, keyid2, keyid3, workitemid, workiteminstance  FROM sdiworkitem, activityworksdi  WHERE activityworksdi.activityid=?   AND activityworksdi.worksdcid=?    AND activityworksdi.workkeyid1 = sdiworkitem.keyid1 AND sdiworkitem.sdcid='Sample'", (Object[])new String[]{activityid, "Sample"});
                        dsTemplSDIData = qp.getPreparedSqlDataSet("SELECT sdidata.sdidataid, sdidata.sdcid, sdidata.keyid1, sdidata.keyid2, sdidata.keyid3, sdidata.paramlistid, sdidata.paramlistversionid, sdidata.variantid, sdidata.dataset  FROM sdidata, activityworksdi  WHERE activityworksdi.activityid=?   AND activityworksdi.worksdcid=?    AND activityworksdi.workkeyid1 = sdidata.keyid1 AND sdidata.sdcid='Sample'", (Object[])new String[]{activityid, "Sample"});
                    } else if (workSDCId.equals("SDIWorkItem")) {
                        dsTempSDIWorkitem = qp.getPreparedSqlDataSet("SELECT w.sdiworkitemid, w.sdcid, w.keyid1, w.keyid2, w.keyid3, w.workitemid, w.workiteminstance  FROM sdiworkitem w, activityworksdi  WHERE activityworksdi.activityid=?   AND activityworksdi.worksdcid=?    AND activityworksdi.workkeyid1 = w.sdiworkitemid", (Object[])new String[]{activityid, "SDIWorkItem"});
                        dsTemplSDIData = qp.getPreparedSqlDataSet("SELECT d.sdidataid, d.sdcid, d.keyid1, d.keyid2, d.keyid3, d.paramlistid, d.paramlistversionid, d.variantid, d.dataset  FROM sdidata d, sdiworkitem w, activityworksdi  WHERE activityworksdi.activityid=?   AND activityworksdi.worksdcid=?    AND activityworksdi.workkeyid1 = w.sdiworkitemid   AND d.sdcid=w.sdcid AND d.keyid1=w.keyid1 AND d.keyid2=w.keyid2 AND d.keyid3=w.keyid3    AND d.sourceworkitemid = w.workitemid AND d.sourceworkiteminstance = w.workiteminstance", (Object[])new String[]{activityid, "SDIWorkItem"});
                    } else if (workSDCId.equals("DataSet")) {
                        dsTemplSDIData = qp.getPreparedSqlDataSet("SELECT d.sdidataid, d.sdcid, d.keyid1, d.keyid2, d.keyid3, d.paramlistid, d.paramlistversionid, d.variantid, d.dataset  FROM sdidata d, activityworksdi  WHERE activityworksdi.activityid=?   AND activityworksdi.worksdcid=?    AND activityworksdi.workkeyid1 = d.sdidataid", (Object[])new String[]{activityid, "DataSet"});
                    } else {
                        PropertyList plannableProps = this.getPlannableSDCProperties(workSDCId);
                        if (plannableProps != null) {
                            DataSet dsTempWorkSDIs;
                            SDCProcessor sdcp = this.getSDCProcessor();
                            String tableid = sdcp.getProperty(workSDCId, "tableid");
                            String keycolid1 = sdcp.getProperty(workSDCId, "keycolid1");
                            String assigntoanalystcolumnid = plannableProps.getProperty("assigntoanalystcolumnid");
                            String assigntoworkareacolumnid = plannableProps.getProperty("assigntoworkareacolumnid");
                            if ((assigntoanalystcolumnid.length() > 0 || assigntoworkareacolumnid.length() > 0) && (dsTempWorkSDIs = qp.getPreparedSqlDataSet("SELECT " + tableid + "." + keycolid1 + " keyid1 FROM activityworksdi," + tableid + " WHERE " + tableid + "." + keycolid1 + "=activityworksdi.workkeyid1 AND activityworksdi.activityid=? AND activityworksdi.worksdcid=?", (Object[])new String[]{activityid, workSDCId})).getRowCount() > 0) {
                                DataSet editCustomSDI;
                                if (assigntoanalystcolumnid.length() > 0 && analystid.length() > 0) {
                                    dsTempWorkSDIs.setString(-1, assigntoanalystcolumnid, analystid);
                                }
                                if (assigntoworkareacolumnid.length() > 0 && workareadepartmentId.length() > 0) {
                                    dsTempWorkSDIs.setString(-1, assigntoworkareacolumnid, workareadepartmentId);
                                }
                                if ((editCustomSDI = (DataSet)customEdits.get(workSDCId)) == null) {
                                    editCustomSDI = new DataSet();
                                    customEdits.put(workSDCId, editCustomSDI);
                                }
                                editCustomSDI.copyRow(dsTempWorkSDIs, -1, 1);
                            }
                        }
                    }
                    if (dsTempSamples.getRowCount() > 0) {
                        if (analystid.length() > 0) {
                            dsTempSamples.setString(-1, "assignedanalystid", analystid);
                        }
                        if (workareadepartmentId.length() > 0) {
                            dsTempSamples.setString(-1, "assigneddepartmentid", workareadepartmentId);
                        }
                        editSample.copyRow(dsTempSamples, -1, 1);
                    }
                    if (dsTempSDIWorkitem.getRowCount() > 0) {
                        if (analystid.length() > 0) {
                            dsTempSDIWorkitem.setString(-1, "s_assignedanalyst", analystid);
                        }
                        if (workareadepartmentId.length() > 0) {
                            dsTempSDIWorkitem.setString(-1, "s_assigneddepartment", workareadepartmentId);
                        }
                        editSDIWorkItem.copyRow(dsTempSDIWorkitem, -1, 1);
                    }
                    if (dsTemplSDIData.getRowCount() <= 0) continue;
                    if (analystid.length() > 0) {
                        dsTemplSDIData.setString(-1, "s_assignedanalyst", analystid);
                    }
                    if (workareadepartmentId.length() > 0) {
                        dsTemplSDIData.setString(-1, "s_assigneddepartment", workareadepartmentId);
                    }
                    editSDIData.copyRow(dsTemplSDIData, -1, 1);
                    continue;
                }
                if (!"I".equalsIgnoreCase(resourceType)) continue;
                String instrumentid = activate ? resources.getValue(j, "instrumentid") : "(null)";
                String instrumentType = resources.getValue(j, "instrumenttypeid");
                String instrumentModel = resources.getValue(j, "instrumentmodelid");
                if (instrumentid.length() <= 0) continue;
                StringBuilder sdiworkitemRelation_sql = new StringBuilder();
                StringBuilder sdidataRelation_sql = new StringBuilder();
                StringBuilder sdidata_sql = new StringBuilder();
                SafeSQL safeSqlSDIWorkitemRelation = new SafeSQL();
                SafeSQL safeSqlDatasetRelation = new SafeSQL();
                SafeSQL safeSqlDataSet = new SafeSQL();
                if (workSDCId.equals("Sample")) {
                    sdiworkitemRelation_sql.append("SELECT r.sdcid, r.keyid1, r.keyid2, r.keyid3, r.workitemid, r.workiteminstance, r.relationid").append(" FROM sdiworkitemrelation r, activityworksdi a ").append(" WHERE r.sdcid='Sample' AND r.keyid1 = a.workkeyid1 AND a.worksdcid='Sample' AND a.activityid=" + safeSqlSDIWorkitemRelation.addVar(activityid));
                    sdidataRelation_sql.append("SELECT r.sdcid, r.keyid1, r.keyid2, r.keyid3, r.paramlistid, r.paramlistversionid, r.variantid, r.dataset, r.relationid").append(" FROM  sdidatarelation r, activityworksdi a ").append(" WHERE r.sdcid='Sample' AND r.keyid1 = a.workkeyid1 AND a.worksdcid='Sample' AND a.activityid=" + safeSqlDatasetRelation.addVar(activityid));
                    sdidata_sql.append("SELECT d.sdidataid, d.sdcid, d.keyid1, d.keyid2, d.keyid3, d.paramlistid, d.paramlistversionid, d.variantid, d.dataset ").append(" FROM sdidata d, activityworksdi a, paramlist pl ").append(" WHERE d.paramlistid=pl.paramlistid AND d.paramlistversionid=pl.paramlistversionid AND d.variantid=pl.variantid ").append(" AND d.sdcid='Sample' AND d.keyid1 = a.workkeyid1 AND a.worksdcid='Sample' AND a.activityid=" + safeSqlDataSet.addVar(activityid));
                } else if (workSDCId.equals("SDIWorkItem")) {
                    sdiworkitemRelation_sql.append("SELECT r.sdcid, r.keyid1, r.keyid2, r.keyid3, r.workitemid, r.workiteminstance, r.relationid ").append(" FROM sdiworkitemrelation r, sdiworkitem w, activityworksdi a ").append(" WHERE r.sdcid = w.sdcid AND r.keyid1 = w.keyid1 AND r.keyid2 = w.keyid2 AND r.keyid3 = w.keyid3 ").append(" AND r.workitemid = w.workitemid AND r.workiteminstance = w.workiteminstance ").append(" AND w.sdiworkitemid = a.workkeyid1 AND a.worksdcid='SDIWorkItem' AND a.activityid=" + safeSqlSDIWorkitemRelation.addVar(activityid));
                    sdidataRelation_sql.append("SELECT r.sdcid, r.keyid1, r.keyid2, r.keyid3, r.paramlistid, r.paramlistversionid, r.variantid, r.dataset, r.relationid ").append(" FROM sdidatarelation r, sdidata d, sdiworkitem w, activityworksdi a ").append(" WHERE r.sdcid = d.sdcid AND r.keyid1 = d.keyid1 and r.keyid2 = d.keyid2 and r.keyid3 = d.keyid3 and r.paramlistid = d.paramlistid and r.paramlistversionid = d.paramlistversionid and r.variantid = d.variantid and r.dataset = d.dataset ").append(" AND d.sdcid = w.sdcid AND d.keyid1 = w.keyid1 and d.keyid2 = w.keyid2 and d.keyid3 = w.keyid3 and d.sourceworkitemid = w.workitemid and d.sourceworkiteminstance = w.workiteminstance ").append(" AND w.sdiworkitemid = a.workkeyid1 AND a.worksdcid='SDIWorkItem' AND a.activityid=" + safeSqlDatasetRelation.addVar(activityid));
                    sdidata_sql.append("SELECT d.sdidataid, d.sdcid, d.keyid1, d.keyid2, d.keyid3, d.paramlistid, d.paramlistversionid, d.variantid, d.dataset ").append(" FROM sdidata d, sdiworkitem w, activityworksdi a, paramlist pl ").append(" WHERE d.paramlistid=pl.paramlistid AND d.paramlistversionid=pl.paramlistversionid AND d.variantid=pl.variantid ").append(" AND d.sdcid = w.sdcid AND d.keyid1 = w.keyid1 and d.keyid2 = w.keyid2 and d.keyid3 = w.keyid3 and d.sourceworkitemid = w.workitemid and d.sourceworkiteminstance = w.workiteminstance").append(" AND w.sdiworkitemid = a.workkeyid1 AND a.worksdcid='SDIWorkItem' AND a.activityid=" + safeSqlDataSet.addVar(activityid));
                } else if (workSDCId.equals("DataSet")) {
                    sdidataRelation_sql.append("SELECT r.sdcid, r.keyid1, r.keyid2, r.keyid3, r.paramlistid, r.paramlistversionid, r.variantid, r.dataset, r.relationid ").append(" FROM sdidatarelation r, sdidata d, activityworksdi a").append(" WHERE r.sdcid = d.sdcid and r.keyid1 = d.keyid1 and r.keyid2 = d.keyid2 and r.keyid3 = d.keyid3 and r.paramlistid = d.paramlistid ").append(" AND r.paramlistversionid = d.paramlistversionid and r.variantid = d.variantid and r.dataset = d.dataset").append(" AND d.sdidataid = a.workkeyid1 AND a.worksdcid='DataSet' AND a.activityid=" + safeSqlDatasetRelation.addVar(activityid));
                    sdidata_sql.append("SELECT d.sdidataid, d.sdcid, d.keyid1, d.keyid2, d.keyid3, d.paramlistid, d.paramlistversionid, d.variantid, d.dataset ").append(" FROM sdidata d, activityworksdi a, paramlist pl ").append(" WHERE d.paramlistid=pl.paramlistid AND d.paramlistversionid=pl.paramlistversionid AND d.variantid=pl.variantid ").append(" AND d.sdidataid = a.workkeyid1 AND a.worksdcid='DataSet' AND a.activityid=" + safeSqlDataSet.addVar(activityid));
                }
                if (sdiworkitemRelation_sql.length() <= 0 && sdidataRelation_sql.length() <= 0 && sdidata_sql.length() <= 0) continue;
                if (instrumentModel.length() > 0 && instrumentType.length() > 0) {
                    if (sdiworkitemRelation_sql.length() > 0) {
                        sdiworkitemRelation_sql.append(" and r.relationfunction = 'Instrument' and  ( ( r.sourcesdcid = ").append(safeSqlSDIWorkitemRelation.addVar("LV_InstrumentModel")).append(" and r.sourcekeyid1 = ").append(safeSqlSDIWorkitemRelation.addVar(instrumentModel)).append(" and r.sourcekeyid2 = ").append(safeSqlSDIWorkitemRelation.addVar(instrumentType)).append(") ").append(" OR ( r.sourcesdcid = ").append(safeSqlSDIWorkitemRelation.addVar("LV_InstrumentType")).append(" and r.sourcekeyid1 = ").append(safeSqlSDIWorkitemRelation.addVar(instrumentType)).append(") )");
                    }
                    if (sdidataRelation_sql.length() > 0) {
                        sdidataRelation_sql.append(" and r.relationfunction = 'Instrument' and  ( ( r.sourcesdcid = ").append(safeSqlDatasetRelation.addVar("LV_InstrumentModel")).append(" and r.sourcekeyid1 = ").append(safeSqlDatasetRelation.addVar(instrumentModel)).append(" and r.sourcekeyid2 = ").append(safeSqlDatasetRelation.addVar(instrumentType)).append(") ").append(" OR ( r.sourcesdcid = ").append(safeSqlDatasetRelation.addVar("LV_InstrumentType")).append(" and r.sourcekeyid1 = ").append(safeSqlDatasetRelation.addVar(instrumentType)).append(") )");
                    }
                    if (sdidata_sql.length() > 0) {
                        sdidata_sql.append(" AND pl.s_instrumenttype=" + safeSqlDataSet.addVar(instrumentType)).append(" AND pl.s_instrumentmodel=" + safeSqlDataSet.addVar(instrumentModel));
                    }
                } else if (instrumentType.length() > 0) {
                    StringBuilder sql = new StringBuilder();
                    if (sdiworkitemRelation_sql.length() > 0) {
                        sql.append("  and r.relationfunction = 'Instrument' and ( ( r.sourcesdcid = ").append(safeSqlSDIWorkitemRelation.addVar("LV_InstrumentType")).append(" and r.sourcekeyid1 = ").append(safeSqlSDIWorkitemRelation.addVar(instrumentType)).append(")").append(" OR ( r.sourcesdcid = ").append(safeSqlSDIWorkitemRelation.addVar("LV_InstrumentModel")).append(" and r.sourcekeyid2 = ").append(safeSqlSDIWorkitemRelation.addVar(instrumentType)).append(") )");
                        sdiworkitemRelation_sql.append((CharSequence)sql);
                    }
                    sql.setLength(0);
                    sql.append("  and r.relationfunction = 'Instrument' and ( ( r.sourcesdcid = ").append(safeSqlDatasetRelation.addVar("LV_InstrumentType")).append(" and r.sourcekeyid1 = ").append(safeSqlDatasetRelation.addVar(instrumentType)).append(")").append(" OR ( r.sourcesdcid = ").append(safeSqlDatasetRelation.addVar("LV_InstrumentModel")).append(" and r.sourcekeyid2 = ").append(safeSqlDatasetRelation.addVar(instrumentType)).append(") )");
                    sdidataRelation_sql.append((CharSequence)sql);
                    sdidata_sql.append(" AND pl.s_instrumenttype=" + safeSqlDataSet.addVar(instrumentType));
                }
                if (sdiworkitemRelation_sql.length() > 0 && (dsWIRelation = qp.getPreparedSqlDataSet(sdiworkitemRelation_sql.toString(), safeSqlSDIWorkitemRelation.getValues())).getRowCount() > 0) {
                    dsWIRelation.setString(-1, "tosdcid", "Instrument");
                    dsWIRelation.setString(-1, "tokeyid1", instrumentid);
                    editSDIWorkItemRelation.copyRow(dsWIRelation, -1, 1);
                }
                if (sdidataRelation_sql.length() > 0 && (dsRelation = qp.getPreparedSqlDataSet(sdidataRelation_sql.toString(), safeSqlDatasetRelation.getValues())).getRowCount() > 0) {
                    dsRelation.setString(-1, "tosdcid", "Instrument");
                    dsRelation.setString(-1, "tokeyid1", instrumentid);
                    editSDIDataRelation.copyRow(dsRelation, -1, 1);
                }
                if (sdidata_sql.length() <= 0 || (ds = qp.getPreparedSqlDataSet(sdidata_sql.toString(), safeSqlDataSet.getValues())).getRowCount() <= 0) continue;
                for (int k = 0; k < ds.size(); ++k) {
                    int findRow = editSDIData.findRow("sdidataid", ds.getValue(k, "sdidataid"));
                    if (findRow >= 0) {
                        editSDIData.setString(findRow, "s_instrumentid", instrumentid);
                        continue;
                    }
                    ds.setString(k, "s_instrumentid", instrumentid);
                    editSDIData.copyRow(ds, k, 1);
                }
            }
        }
        PropertyList props = new PropertyList();
        if (editSample.getRowCount() > 0) {
            props.clear();
            props.setProperty("sdcid", "Sample");
            props.setProperty("keyid1", editSample.getColumnValues("workkeyid1", ";"));
            if (editSample.isValidColumn("assignedanalystid")) {
                props.setProperty("assignedanalystid", editSample.getColumnValues("assignedanalystid", ";"));
            }
            if (editSample.isValidColumn("assigneddepartmentid")) {
                props.setProperty("assigneddepartmentid", editSample.getColumnValues("assigneddepartmentid", ";"));
            }
            if (tracelogid != null && tracelogid.length() > 0) {
                props.setProperty("tracelogid", tracelogid);
            }
            ap.processAction("EditSDI", "1", props);
        }
        if (customEdits.size() > 0) {
            for (String sdcid : customEdits.keySet()) {
                PropertyList plannableProps;
                DataSet customSDI = (DataSet)customEdits.get(sdcid);
                if (customSDI.size() <= 0 || (plannableProps = this.getPlannableSDCProperties(sdcid)) == null) continue;
                String assigntoanalystcolumnid = plannableProps.getProperty("assigntoanalystcolumnid");
                String assigntoworkareacolumnid = plannableProps.getProperty("assigntoworkareacolumnid");
                props.clear();
                props.setProperty("sdcid", sdcid);
                props.setProperty("keyid1", customSDI.getColumnValues("keyid1", ";"));
                if (customSDI.isValidColumn(assigntoanalystcolumnid)) {
                    props.setProperty(assigntoanalystcolumnid, customSDI.getColumnValues(assigntoanalystcolumnid, ";"));
                }
                if (customSDI.isValidColumn(assigntoworkareacolumnid)) {
                    props.setProperty(assigntoworkareacolumnid, customSDI.getColumnValues(assigntoworkareacolumnid, ";"));
                }
                if (tracelogid != null && tracelogid.length() > 0) {
                    props.setProperty("tracelogid", tracelogid);
                }
                ap.processAction("EditSDI", "1", props);
            }
        }
        if (editSDIWorkItem.getRowCount() > 0) {
            props.clear();
            props.setProperty("sdcid", editSDIWorkItem.getValue(0, "sdcid"));
            props.setProperty("keyid1", editSDIWorkItem.getColumnValues("keyid1", ";"));
            props.setProperty("keyid2", editSDIWorkItem.getColumnValues("keyid2", ";"));
            props.setProperty("keyid3", editSDIWorkItem.getColumnValues("keyid3", ";"));
            props.setProperty("workitemid", editSDIWorkItem.getColumnValues("workitemid", ";"));
            props.setProperty("workiteminstance", editSDIWorkItem.getColumnValues("workiteminstance", ";"));
            props.setProperty("propsmatch", "Y");
            if (editSDIWorkItem.isValidColumn("s_assignedanalyst")) {
                props.setProperty("s_assignedanalyst", editSDIWorkItem.getColumnValues("s_assignedanalyst", ";"));
            }
            if (editSDIWorkItem.isValidColumn("s_assigneddepartment")) {
                props.setProperty("s_assigneddepartment", editSDIWorkItem.getColumnValues("s_assigneddepartment", ";"));
            }
            if (tracelogid != null && tracelogid.length() > 0) {
                props.setProperty("tracelogid", tracelogid);
            }
            ap.processAction("EditSDIWorkItem", "1", props);
        }
        if (editSDIData.getRowCount() > 0) {
            props.clear();
            props.setProperty("sdcid", editSDIData.getValue(0, "sdcid"));
            props.setProperty("keyid1", editSDIData.getColumnValues("keyid1", ";"));
            props.setProperty("keyid2", editSDIData.getColumnValues("keyid2", ";"));
            props.setProperty("keyid3", editSDIData.getColumnValues("keyid3", ";"));
            props.setProperty("paramlistid", editSDIData.getColumnValues("paramlistid", ";"));
            props.setProperty("paramlistversionid", editSDIData.getColumnValues("paramlistversionid", ";"));
            props.setProperty("variantid", editSDIData.getColumnValues("variantid", ";"));
            props.setProperty("dataset", editSDIData.getColumnValues("dataset", ";"));
            props.setProperty("propsmatch", "Y");
            if (editSDIData.isValidColumn("s_assignedanalyst")) {
                props.setProperty("s_assignedanalyst", editSDIData.getColumnValues("s_assignedanalyst", ";"));
            }
            if (editSDIData.isValidColumn("s_assigneddepartment")) {
                props.setProperty("s_assigneddepartment", editSDIData.getColumnValues("s_assigneddepartment", ";"));
            }
            if (editSDIData.isValidColumn("s_instrumentid")) {
                props.setProperty("s_instrumentid", editSDIData.getColumnValues("s_instrumentid", ";"));
            }
            if (tracelogid != null && tracelogid.length() > 0) {
                props.setProperty("tracelogid", tracelogid);
            }
            ap.processAction("EditDataSet", "1", props);
        }
        if (editSDIWorkItemRelation.getRowCount() > 0) {
            props.clear();
            props.setProperty("sdcid", editSDIWorkItemRelation.getValue(0, "sdcid"));
            props.setProperty("keyid1", editSDIWorkItemRelation.getColumnValues("keyid1", ";"));
            props.setProperty("keyid2", editSDIWorkItemRelation.getColumnValues("keyid2", ";"));
            props.setProperty("keyid3", editSDIWorkItemRelation.getColumnValues("keyid3", ";"));
            props.setProperty("workitemid", editSDIWorkItemRelation.getColumnValues("workitemid", ";"));
            props.setProperty("workiteminstance", editSDIWorkItemRelation.getColumnValues("workiteminstance", ";"));
            props.setProperty("relationid", editSDIWorkItemRelation.getColumnValues("relationid", ";"));
            props.setProperty("tosdcid", editSDIWorkItemRelation.getColumnValues("tosdcid", ";"));
            props.setProperty("tokeyid1", editSDIWorkItemRelation.getColumnValues("tokeyid1", ";"));
            if (tracelogid != null && tracelogid.length() > 0) {
                props.setProperty("tracelogid", tracelogid);
            }
            ap.processAction("EditSDIWorkItemRelation", "1", props);
        }
        if (editSDIDataRelation.getRowCount() > 0) {
            props.clear();
            props.setProperty("sdcid", editSDIDataRelation.getValue(0, "sdcid"));
            props.setProperty("keyid1", editSDIDataRelation.getColumnValues("keyid1", ";"));
            props.setProperty("keyid2", editSDIDataRelation.getColumnValues("keyid2", ";"));
            props.setProperty("keyid3", editSDIDataRelation.getColumnValues("keyid3", ";"));
            props.setProperty("paramlistid", editSDIDataRelation.getColumnValues("paramlistid", ";"));
            props.setProperty("paramlistversionid", editSDIDataRelation.getColumnValues("paramlistversionid", ";"));
            props.setProperty("variantid", editSDIDataRelation.getColumnValues("variantid", ";"));
            props.setProperty("dataset", editSDIDataRelation.getColumnValues("dataset", ";"));
            props.setProperty("relationid", editSDIDataRelation.getColumnValues("relationid", ";"));
            props.setProperty("tosdcid", editSDIDataRelation.getColumnValues("tosdcid", ";"));
            props.setProperty("tokeyid1", editSDIDataRelation.getColumnValues("tokeyid1", ";"));
            if (tracelogid != null && tracelogid.length() > 0) {
                props.setProperty("tracelogid", tracelogid);
            }
            ap.processAction("EditSDIDataRelation", "1", props);
        }
        return activatedList.length() > 0 ? activatedList.substring(1) : "";
    }

    private DataSet getActivityStatusList(String activitylist) {
        SafeSQL safeSQL = new SafeSQL();
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT activityid, activitystatus FROM activity WHERE activityid in (" + safeSQL.addIn(activitylist, ";") + ")", safeSQL.getValues());
        return ds;
    }

    public Activity getActivityDetails(String activityid) throws SapphireException {
        Trace.logInfo(LOGNAME, "Getting activity for " + activityid);
        return this.getActivityDetails(this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM activity WHERE activityid=?", (Object[])new String[]{activityid}), 0);
    }

    public Activity getActivityDetails(DataSet dataSet, int row) throws SapphireException {
        Trace.logInfo(LOGNAME, "Getting activity from dataset");
        if (dataSet.size() == 0) {
            throw new SapphireException("Unable to find activity");
        }
        return new Activity(this.calendarConverter, dataSet, row);
    }

    public DataSet getActivityResources(String activityid) {
        Trace.logInfo(LOGNAME, "Getting resrouces for " + activityid);
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM activityresource WHERE activityid=?", (Object[])new String[]{activityid});
        return ds;
    }

    public DataSet getActivityWorkSDIs(String activityid) {
        Trace.logInfo(LOGNAME, "Getting label for " + activityid);
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM activityworksdi WHERE activityid=?", (Object[])new String[]{activityid});
        return ds;
    }

    public boolean isActivityLIMSWorkStarted(String activityId) throws SapphireException {
        Activity activity = this.getActivityDetails(activityId);
        String workSDCId = activity.getWorksdcid();
        DataSet ds = new DataSet();
        if (workSDCId.equals("Sample")) {
            ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT 1 FROM s_sample s, activityworksdi a  WHERE s.s_sampleid = a.workkeyid1 AND a.worksdcid = ? AND a.activityid = ? AND starttestingdt IS NOT NULL", (Object[])new String[]{"Sample", activityId});
        } else if (workSDCId.equals("SDIWorkItem")) {
            ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT 1 FROM sdiworkitem s, activityworksdi a  WHERE s.sdiworkitemid = a.workkeyid1 AND a.worksdcid = ? AND  a.activityid = ? AND starteddt IS NOT NULL", (Object[])new String[]{"SDIWorkItem", activityId});
        } else if (workSDCId.equals("DataSet")) {
            ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT 1 FROM sdidata s, activityworksdi a  WHERE s.sdidataid = a.workkeyid1 AND a.worksdcid = ? AND  a.activityid = ? AND starteddt IS NOT NULL", (Object[])new String[]{"DataSet", activityId});
        } else if (workSDCId.equals("WorkOrderSDC")) {
            ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT 1 FROM workorder w, activityworksdi a  WHERE w.workorderid = a.workkeyid1 AND a.worksdcid = ? AND a.activityid = ? AND workorderstatus != 'Pending'", (Object[])new String[]{"WorkOrderSDC", activityId});
        }
        return ds.getRowCount() > 0;
    }

    public DataSet getActivityResourceDetails(String activitylist) {
        SafeSQL safeSQL = new SafeSQL();
        DataSet returnResourceRequirements = new DataSet();
        int activitycount = StringUtil.split(activitylist, ";").length;
        DataSet resources = this.getQueryProcessor().getPreparedSqlDataSet("SELECT activityresource.*, activity.workcontext, activity.testingdepartmentid FROM activityresource, activity WHERE activity.activityid = activityresource.activityid AND activity.activityid in (" + safeSQL.addIn(activitylist, ";") + ") ", safeSQL.getValues());
        resources.sort("resourcetypeflag, analysttype, instrumenttype, instrumentmodelid");
        ArrayList<DataSet> resourcetypes = resources.getGroupedDataSets("resourcetypeflag, analysttype, instrumenttype, instrumentmodelid");
        for (DataSet resourceType : resourcetypes) {
            if (resourceType.size() != activitycount) continue;
            String resourcetypeflag = resourceType.getValue(0, "resourcetypeflag");
            String baseTestingDepartmentid = "";
            for (int i = 0; i < resourceType.size() && baseTestingDepartmentid.length() == 0; ++i) {
                baseTestingDepartmentid = resourceType.getValue(i, "testingdepartmentid");
            }
            String resourceColumnid = resourcetypeflag.equals("A") ? "analystid" : (resourcetypeflag.equals("I") ? "instrumentid" : "workareadepartmentid");
            String mergedWorkContext = "";
            String foundResourceid = "";
            boolean addresource = true;
            for (int i = 0; i < resourceType.size(); ++i) {
                String resourceid = resourceType.getValue(i, resourceColumnid);
                String workContext = resourceType.getValue(i, "workcontext");
                String testingDepartmentid = resourceType.getValue(i, "testingdepartmentid");
                try {
                    mergedWorkContext = this.mergeWorkContext(mergedWorkContext, workContext);
                }
                catch (SapphireException e) {
                    addresource = false;
                }
                if (testingDepartmentid.length() > 0 && !testingDepartmentid.equals(baseTestingDepartmentid)) {
                    addresource = false;
                }
                if (resourceid.length() <= 0) continue;
                if (foundResourceid.length() > 0) {
                    addresource = false;
                    continue;
                }
                foundResourceid = resourceid;
            }
            if (!addresource) continue;
            returnResourceRequirements.copyRow(resourceType, 0, 1);
            int row = returnResourceRequirements.size() - 1;
            if (foundResourceid.length() > 0) {
                returnResourceRequirements.setString(row, resourceColumnid, foundResourceid);
                continue;
            }
            this.populateResourceSDIs(returnResourceRequirements, baseTestingDepartmentid, row, new ContextMap(mergedWorkContext));
        }
        return returnResourceRequirements;
    }

    public DataSet getNewWorkTestingDepartments(String worksdcid, String workkeyid1) {
        DataSet returnDepartments = new DataSet();
        SafeSQL safeSQL = new SafeSQL();
        DataSet allTestingDepartments = null;
        String columnid = null;
        if (worksdcid.equals("SDIWorkItem")) {
            allTestingDepartments = this.getQueryProcessor().getPreparedSqlDataSet("SELECT testingdepartmentid FROM sdiworkitem WHERE sdiworkitemid in (" + safeSQL.addIn(workkeyid1, ";") + ")", safeSQL.getValues());
            columnid = "testingdepartmentid";
        } else if (worksdcid.equals("DataSet")) {
            allTestingDepartments = this.getQueryProcessor().getPreparedSqlDataSet("SELECT testingdepartmentid FROM sdidata WHERE sdidataid in (" + safeSQL.addIn(workkeyid1, ";") + ")", safeSQL.getValues());
            columnid = "testingdepartmentid";
        } else if (worksdcid.equals("Sample")) {
            allTestingDepartments = this.getQueryProcessor().getPreparedSqlDataSet("SELECT testingdepartmentid FROM s_sample WHERE s_sampleid in (" + safeSQL.addIn(workkeyid1, ";") + ")", safeSQL.getValues());
            columnid = "testingdepartmentid";
        } else if (worksdcid.equals("LV_Activity")) {
            allTestingDepartments = this.getQueryProcessor().getPreparedSqlDataSet("SELECT testingdepartmentid FROM activity WHERE activityid in (" + safeSQL.addIn(workkeyid1, ";") + ") ", safeSQL.getValues());
            columnid = "testingdepartmentid";
        } else {
            PropertyList plannableProps = this.getPlannableSDCProperties(worksdcid);
            if (plannableProps != null) {
                SDCProcessor sdcp = this.getSDCProcessor();
                String tableid = sdcp.getProperty(worksdcid, "tableid");
                String keycolid1 = sdcp.getProperty(worksdcid, "keycolid1");
                allTestingDepartments = this.getQueryProcessor().getPreparedSqlDataSet("SELECT testingdepartmentid FROM " + tableid + " WHERE " + keycolid1 + " in (" + safeSQL.addIn(workkeyid1, ";") + ")", safeSQL.getValues());
                columnid = "testingdepartmentid";
            }
        }
        if (allTestingDepartments != null) {
            allTestingDepartments.sort(columnid);
            ArrayList<DataSet> distinctDepartments = allTestingDepartments.getGroupedDataSets(columnid);
            for (DataSet distinceDepartment : distinctDepartments) {
                int row = returnDepartments.addRow();
                returnDepartments.setString(row, "testingdepartmentid", distinceDepartment.getValue(0, columnid, "(none)"));
                returnDepartments.setNumber(row, "count", distinceDepartment.size());
            }
        }
        return returnDepartments;
    }

    public NewWorkDetails getNewWorkDetails(ActivityClassHandler activityClassHandler, String worksdcid, String workkeyid1) throws SapphireException {
        DataSet resourceRequirements = null;
        SafeSQL safeSQL = new SafeSQL();
        if (activityClassHandler == null) {
            activityClassHandler = ActivityClassHandler.getInstance(this.getConnectionid(), this.getRakFile(), this.getWapPolicy(), "", worksdcid);
        }
        int smallestMaxActivitySize = activityClassHandler.getMaxActivitySize();
        String testingDepartmentid = "";
        int workCount = StringUtil.split(workkeyid1, ";").length;
        ContextMap workContext = new ContextMap();
        if (workCount > 1000) {
            throw new SapphireException("You have tried to exceed the maximum activity size of 1000.");
        }
        if (worksdcid.equals("SDIWorkItem")) {
            DataSet distinctMasterSDI = this.getQueryProcessor().getPreparedSqlDataSet("SELECT DISTINCT workitem.workitemid, workitem.workitemversionid, workitem.maxactivitysize, sdiworkitem.testingdepartmentid, sdiworkitem.workareadepartmentid  FROM sdiworkitem, workitem WHERE sdiworkitem.workitemid = workitem.workitemid AND sdiworkitem.workitemversionid = workitem.workitemversionid AND sdiworkitem.sdiworkitemid in (" + safeSQL.addIn(workkeyid1, ";") + ")", safeSQL.getValues());
            testingDepartmentid = this.getUniqueDataSetColumnValue(distinctMasterSDI, "testingdepartmentid");
            String workareaDepartmentid = this.getDistinctDataSetColumnValues(distinctMasterSDI, "workareadepartmentid");
            HashSet<String> masterSDIs = new HashSet<String>();
            for (int i = 0; i < distinctMasterSDI.size(); ++i) {
                masterSDIs.add(distinctMasterSDI.getValue(i, "workitemid") + ";" + distinctMasterSDI.getValue(i, "workitemversionid"));
            }
            workContext.put("WSDC", worksdcid);
            workContext.put("MDSDI", String.join((CharSequence)"|", masterSDIs));
            workContext.put("WA", workareaDepartmentid);
            smallestMaxActivitySize = Math.min(smallestMaxActivitySize, distinctMasterSDI.getInt(0, "maxactivitysize", 1000));
            String workitemid = distinctMasterSDI.getValue(0, "workitemid");
            String workitemversionid = distinctMasterSDI.getValue(0, "workitemversionid");
            resourceRequirements = this.retrieveWorkitemResourceRequirements(workitemid, workitemversionid);
            if (distinctMasterSDI.size() > 1) {
                for (int i = 1; i < distinctMasterSDI.size(); ++i) {
                    String extraWorkitemversionid;
                    int extraSmallestMaxActivitySize = distinctMasterSDI.getInt(i, "maxactivitysize", 1000);
                    smallestMaxActivitySize = Math.min(smallestMaxActivitySize, extraSmallestMaxActivitySize);
                    String extraWorkitemid = distinctMasterSDI.getValue(i, "workitemid");
                    DataSet extraResourceRequirements = this.retrieveWorkitemResourceRequirements(extraWorkitemid, extraWorkitemversionid = distinctMasterSDI.getValue(i, "workitemversionid"));
                    if (this.areResourceRequirementsCompatible(resourceRequirements, extraResourceRequirements, true)) continue;
                    throw new SapphireException("Workitems do not have compatible resource requirements");
                }
            }
        } else if (worksdcid.equals("DataSet")) {
            DataSet distinctMasterSDI = this.getQueryProcessor().getPreparedSqlDataSet("SELECT DISTINCT sdidata.paramlistid, sdidata.paramlistversionid, sdidata.variantid, sdidata.testingdepartmentid, sdidata.workareadepartmentid,  paramlist.maxactivitysize paramlistmax, workitemitem.workitemid, workitemitem.workitemversionid, workitemitem.workitemitemid, workitemitem.maxactivitysize workitemmax FROM sdidata LEFT JOIN paramlist ON    sdidata.paramlistid = paramlist.paramlistid    AND sdidata.paramlistversionid = paramlist.paramlistversionid    AND sdidata.variantid = paramlist.variantid LEFT OUTER JOIN sdiworkitem ON    sdidata.sdcid=sdiworkitem.sdcid AND    sdidata.keyid1=sdiworkitem.keyid1 AND    sdidata.keyid2=sdiworkitem.keyid2 AND    sdidata.keyid3=sdiworkitem.keyid3 AND    sdidata.sourceworkitemid = sdiworkitem.workitemid AND    sdidata.sourceworkiteminstance = sdiworkitem.workiteminstance LEFT JOIN workitemitem ON    workitemitem.workitemid = sdiworkitem.workitemid AND    workitemitem.workitemversionid = sdiworkitem.workitemversionid AND    workitemitem.sdcid='ParamList' AND    workitemitem.keyid1=sdidata.paramlistid AND    ( workitemitem.keyid2=sdidata.paramlistversionid OR workitemitem.keyid2='C' ) AND    workitemitem.keyid3=sdidata.variantid WHERE sdidata.sdidataid in (" + safeSQL.addIn(workkeyid1, ";") + ")", safeSQL.getValues());
            testingDepartmentid = this.getUniqueDataSetColumnValue(distinctMasterSDI, "testingdepartmentid");
            String workareaDepartmentid = this.getDistinctDataSetColumnValues(distinctMasterSDI, "workareadepartmentid");
            HashSet<String> masterSDIs = new HashSet<String>();
            for (int i = 0; i < distinctMasterSDI.size(); ++i) {
                masterSDIs.add(distinctMasterSDI.getValue(i, "paramlistid") + ";" + distinctMasterSDI.getValue(i, "paramlistversionid") + ";" + distinctMasterSDI.getValue(i, "variantid"));
            }
            workContext.put("WSDC", worksdcid);
            workContext.put("MDSDI", String.join((CharSequence)"|", masterSDIs));
            workContext.put("WA", workareaDepartmentid);
            String workitemitemid = distinctMasterSDI.getValue(0, "workitemitemid");
            if (workitemitemid.length() > 0) {
                String workitemid = distinctMasterSDI.getValue(0, "workitemid");
                String workitemversionid = distinctMasterSDI.getValue(0, "workitemversionid");
                smallestMaxActivitySize = Math.min(smallestMaxActivitySize, distinctMasterSDI.getInt(0, "workitemmax", 1000));
                resourceRequirements = this.retrieveWorkitemItemResourceRequirements(workitemid, workitemversionid, workitemitemid);
            } else {
                String paramlistid = distinctMasterSDI.getValue(0, "paramlistid");
                String paramlistversionid = distinctMasterSDI.getValue(0, "paramlistversionid");
                String variantid = distinctMasterSDI.getValue(0, "variantid");
                smallestMaxActivitySize = Math.min(smallestMaxActivitySize, distinctMasterSDI.getInt(0, "paramlistmax", 1000));
                resourceRequirements = this.retrieveSDIResourceRequirements("ParamList", paramlistid, paramlistversionid, variantid);
            }
            if (distinctMasterSDI.size() > 1) {
                for (int i = 1; i < distinctMasterSDI.size(); ++i) {
                    String extraVariantid;
                    String extraParamlistversionid;
                    int extraSmallestMaxActivitySize;
                    String extraworkitemitemid = distinctMasterSDI.getValue(i, "workitemitemid");
                    if (extraworkitemitemid.length() > 0) {
                        String extraworkitemversionid;
                        extraSmallestMaxActivitySize = distinctMasterSDI.getInt(i, "workitemmax", 1000);
                        smallestMaxActivitySize = Math.min(smallestMaxActivitySize, extraSmallestMaxActivitySize);
                        String extraworkitemid = distinctMasterSDI.getValue(i, "workitemid");
                        DataSet extraResourceRequirements = this.retrieveWorkitemItemResourceRequirements(extraworkitemid, extraworkitemversionid = distinctMasterSDI.getValue(i, "workitemversionid"), extraworkitemitemid);
                        if (this.areResourceRequirementsCompatible(resourceRequirements, extraResourceRequirements, true)) continue;
                        throw new SapphireException("ParamLists or WorkItemItems do not have compatible resource requirements");
                    }
                    extraSmallestMaxActivitySize = distinctMasterSDI.getValue(i, "workitemid").length() > 0 ? distinctMasterSDI.getInt(i, "workitemmax", 1000) : distinctMasterSDI.getInt(i, "paramlistmax", 1000);
                    smallestMaxActivitySize = Math.min(smallestMaxActivitySize, extraSmallestMaxActivitySize);
                    String extraParamlistid = distinctMasterSDI.getValue(i, "paramlistid");
                    DataSet extraResourceRequirements = this.retrieveSDIResourceRequirements("ParamList", extraParamlistid, extraParamlistversionid = distinctMasterSDI.getValue(i, "paramlistversionid"), extraVariantid = distinctMasterSDI.getValue(i, "variantid"));
                    if (this.areResourceRequirementsCompatible(resourceRequirements, extraResourceRequirements, true)) continue;
                    throw new SapphireException("ParamLists do not have compatible resource requirements");
                }
            }
        } else if (worksdcid.equals("Sample")) {
            int i;
            DataSet distinctMasterSDI = this.getQueryProcessor().getPreparedSqlDataSet("SELECT DISTINCT s_sample.testingdepartmentid, s_sample.workareadepartmentid,  s_product.s_productid, s_product.s_productversionid, s_product.maxactivitysize productactivitysize, s_samplepoint.s_samplepointid, s_samplepoint.maxactivitysize samplepointactivitysize,  s_location.s_locationid, s_location.maxactivitysize locationactivitysize FROM s_sample LEFT OUTER JOIN s_product ON s_sample.productid=s_product.s_productid AND s_sample.productversionid=s_product.s_productversionid LEFT OUTER JOIN s_samplepoint ON s_sample.samplepointid = s_samplepoint.s_samplepointid LEFT OUTER JOIN s_location ON s_sample.locationid = s_location.s_locationid WHERE s_sampleid in (" + safeSQL.addIn(workkeyid1, ";") + ")", safeSQL.getValues());
            String masterDataSdcid = "";
            String masterDataKeyid1 = "";
            String masterDataKeyid2 = "";
            String activitySizeColumn = "";
            if (distinctMasterSDI.getValue(0, "s_samplepointid").length() > 0) {
                masterDataSdcid = "SamplePoint";
                masterDataKeyid1 = distinctMasterSDI.getValue(0, "s_samplepointid");
                activitySizeColumn = "samplepointactivitysize";
            } else if (distinctMasterSDI.getValue(0, "s_locationid").length() > 0) {
                masterDataSdcid = "Location";
                masterDataKeyid1 = distinctMasterSDI.getValue(0, "s_locationid");
                activitySizeColumn = "locationactivitysize";
            } else if (distinctMasterSDI.getValue(0, "s_productid").length() > 0) {
                masterDataSdcid = "Product";
                masterDataKeyid1 = distinctMasterSDI.getValue(0, "s_productid");
                masterDataKeyid2 = distinctMasterSDI.getValue(0, "s_productversionid");
                activitySizeColumn = "productactivitysize";
            }
            if (masterDataSdcid.length() == 0) {
                throw new SapphireException("Samples must be linked to a Product, SamplePoint or Location");
            }
            testingDepartmentid = this.getUniqueDataSetColumnValue(distinctMasterSDI, "testingdepartmentid");
            String workareaDepartmentid = this.getDistinctDataSetColumnValues(distinctMasterSDI, "workareadepartmentid");
            HashSet<String> masterSDIs = new HashSet<String>();
            for (i = 0; i < distinctMasterSDI.size(); ++i) {
                masterSDIs.add(masterDataSdcid.equals("SamplePoint") ? distinctMasterSDI.getValue(i, "s_samplepointid") : (masterDataSdcid.equals("Location") ? distinctMasterSDI.getValue(i, "s_locationid") : distinctMasterSDI.getValue(i, "s_productid") + ";" + distinctMasterSDI.getValue(i, "s_productversionid")));
            }
            workContext.put("WSDC", worksdcid);
            workContext.put("MDSDI", String.join((CharSequence)"|", masterSDIs));
            workContext.put("WA", workareaDepartmentid);
            smallestMaxActivitySize = Math.min(smallestMaxActivitySize, distinctMasterSDI.getInt(0, activitySizeColumn, 1000));
            resourceRequirements = this.retrieveSDIResourceRequirements(masterDataSdcid, masterDataKeyid1, masterDataKeyid2, "");
            if (distinctMasterSDI.size() > 1) {
                for (i = 1; i < distinctMasterSDI.size(); ++i) {
                    String extraMasterDataKeyid1 = "";
                    String extraMasterDataKeyid2 = "";
                    if (masterDataSdcid.equals("SamplePoint")) {
                        extraMasterDataKeyid1 = distinctMasterSDI.getValue(i, "s_samplepointid");
                    } else if (masterDataSdcid.equals("Location")) {
                        extraMasterDataKeyid1 = distinctMasterSDI.getValue(i, "s_locationid");
                    } else {
                        extraMasterDataKeyid1 = distinctMasterSDI.getValue(i, "s_productid");
                        extraMasterDataKeyid2 = distinctMasterSDI.getValue(i, "s_productversionid");
                    }
                    if (extraMasterDataKeyid1.length() == 0) {
                        throw new SapphireException("All samples must have a " + masterDataSdcid);
                    }
                    int extraSmallestMaxActivitySize = distinctMasterSDI.getInt(i, "maxactivitysize", 1000);
                    smallestMaxActivitySize = Math.min(smallestMaxActivitySize, extraSmallestMaxActivitySize);
                    DataSet extraResourceRequirements = this.retrieveSDIResourceRequirements(masterDataSdcid, extraMasterDataKeyid1, extraMasterDataKeyid2, "");
                    if (this.areResourceRequirementsCompatible(resourceRequirements, extraResourceRequirements, true)) continue;
                    throw new SapphireException("Samples do not have compatible resource requirements");
                }
            }
        } else {
            PropertyList plannableProps = this.getPlannableSDCProperties(worksdcid);
            if (plannableProps != null) {
                SDCProcessor sdcp = this.getSDCProcessor();
                String tableid = sdcp.getProperty(worksdcid, "tableid");
                String keycolid1 = sdcp.getProperty(worksdcid, "keycolid1");
                DataSet distinctMasterSDI = this.getQueryProcessor().getPreparedSqlDataSet("SELECT DISTINCT testingdepartmentid, workareadepartmentid FROM " + tableid + " WHERE " + keycolid1 + " in (" + safeSQL.addIn(workkeyid1, ";") + ")", safeSQL.getValues());
                testingDepartmentid = this.getUniqueDataSetColumnValue(distinctMasterSDI, "testingdepartmentid");
                String workareaDepartmentid = this.getDistinctDataSetColumnValues(distinctMasterSDI, "workareadepartmentid");
                workContext.put("WSDC", worksdcid);
                workContext.put("MDSDI", "None");
                workContext.put("WA", workareaDepartmentid);
                resourceRequirements = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM sdiresourcerequirement WHERE 1=0", (Object[])new String[0]);
                activityClassHandler.addResourceRequirements(resourceRequirements);
            } else {
                resourceRequirements = new DataSet();
            }
        }
        return new NewWorkDetails(worksdcid, workCount, testingDepartmentid, smallestMaxActivitySize, resourceRequirements, workContext.toString());
    }

    private String getUniqueDataSetColumnValue(DataSet ds, String columnid) throws SapphireException {
        String value = "";
        for (int i = 0; i < ds.size(); ++i) {
            String temp = ds.getValue(i, columnid);
            if (temp.length() <= 0) continue;
            if (value.length() > 0 && !temp.equals(value)) {
                throw new SapphireException("Two distinct testing departments found");
            }
            value = temp;
        }
        return value;
    }

    private String getDistinctDataSetColumnValues(DataSet ds, String columnid) throws SapphireException {
        HashSet<String> set = new HashSet<String>();
        for (int i = 0; i < ds.size(); ++i) {
            set.add(ds.getValue(i, columnid));
        }
        StringBuilder sb = new StringBuilder();
        for (String value : set) {
            sb.append(";").append(value);
        }
        return sb.length() == 0 ? "" : sb.substring(1);
    }

    public void populateResourceSDIs(DataSet resourceRequirements, String testingDepartmentid, int row, ContextMap workContext) {
        this.populateResourceSDIsForRow(resourceRequirements, testingDepartmentid, row, workContext);
    }

    public void populateResourceSDIs(DataSet resourceRequirements, String testingDepartmentid, ContextMap workContext) {
        for (int i = 0; i < resourceRequirements.size(); ++i) {
            this.populateResourceSDIsForRow(resourceRequirements, testingDepartmentid, i, workContext);
        }
    }

    private void populateResourceSDIsForRow(DataSet resourceRequirements, String testingDepartmentid, int i, ContextMap workContext) {
        String resourcetypeflag = resourceRequirements.getValue(i, "resourcetypeflag");
        String workContextWorkareas = workContext.get("WA");
        if ("A".equals(resourcetypeflag)) {
            Object workareas;
            String users = "";
            String transientUsers = "";
            if (workContextWorkareas.length() > 0) {
                workareas = StringUtil.split(workContextWorkareas, ";");
                LinkedHashSet<String> masterUsers = new LinkedHashSet<String>();
                for (String tempworkarea : workareas) {
                    LinkedHashSet<String> linkedHashSet = new LinkedHashSet<String>(Arrays.asList(StringUtil.split(this.getPermanentWorkareaUsers(tempworkarea), ";")));
                    if (masterUsers.size() == 0) {
                        masterUsers.addAll(linkedHashSet);
                        continue;
                    }
                    masterUsers.retainAll(linkedHashSet);
                }
                WAPSelector selector = new WAPSelector(this.getConnectionid(), this.getRakFile());
                Instant timeStart = Instant.now();
                Instant timeEnd = timeStart.plus(365L, ChronoUnit.DAYS);
                ArrayList<String> transientUsersList = new ArrayList<String>();
                for (String tempworkarea : workareas) {
                    try {
                        Map<String, Set<String>> scheduled = selector.getScheduledWorkAreaUsers(tempworkarea, timeStart, timeEnd);
                        if (scheduled == null) continue;
                        for (Set<String> value : scheduled.values()) {
                            for (String userid : value) {
                                if (masterUsers.contains(userid) || transientUsersList.contains(userid)) continue;
                                transientUsersList.add(userid);
                            }
                        }
                    }
                    catch (SapphireException e) {
                        this.logger.warn("Failed to retrieve scheduled workareas for workarea " + tempworkarea + ": " + e.getMessage());
                    }
                }
                transientUsers = StringUtil.arrayToString(transientUsersList.toArray(new String[0]), ";");
                users = StringUtil.arrayToString(masterUsers.toArray(new String[0]), ";");
            } else {
                users = this.getDepartmentUsers(testingDepartmentid);
            }
            if (workContext.get("WSDC").equals("DataSet")) {
                try {
                    String[] paramlists = StringUtil.split(workContext.get("MDSDI"), "|");
                    users = this.getCertifiedUsers(users, paramlists);
                }
                catch (SapphireException e) {
                    this.logger.warn("Failed to establish training details for users. Continuing");
                }
            }
            resourceRequirements.setString(i, "_resourcesdis", users);
            resourceRequirements.setString(i, "_tempresourcesdis", transientUsers);
            workareas = "";
            if (workContextWorkareas.length() > 0) {
                List<String> templist = Arrays.asList(StringUtil.split(workContextWorkareas, ";"));
                Collections.sort(templist);
                workareas = StringUtil.arrayToString(templist.toArray(new String[0]), ";");
            } else {
                workareas = this.getDepartmentUserWorkareas(testingDepartmentid);
            }
            resourceRequirements.setString(i, "_resourcesworkareas", (String)workareas);
        } else if ("I".equals(resourcetypeflag)) {
            Object workareas;
            String instrumenttypeid = resourceRequirements.getValue(i, "instrumenttypeid");
            String instrumentmodelid = resourceRequirements.getValue(i, "instrumentmodelid");
            String instruments = "";
            if (workContextWorkareas.length() > 0) {
                workareas = StringUtil.split(workContextWorkareas, ";");
                TreeSet<String> masterInstruments = new TreeSet<String>();
                for (String string : workareas) {
                    HashSet<String> tempInstruments = new HashSet<String>(Arrays.asList(StringUtil.split(this.getDepartmentInstruments("", string, instrumenttypeid, instrumentmodelid), ";")));
                    masterInstruments.addAll(tempInstruments);
                }
                instruments = StringUtil.arrayToString(masterInstruments.toArray(new String[0]), ";");
            } else {
                instruments = this.getDepartmentInstruments(testingDepartmentid, "", instrumenttypeid, instrumentmodelid);
            }
            resourceRequirements.setString(i, "_resourcesdis", instruments);
            workareas = "";
            if (workContextWorkareas.length() > 0) {
                List<String> templist = Arrays.asList(StringUtil.split(workContextWorkareas, ";"));
                Collections.sort(templist);
                workareas = StringUtil.arrayToString(templist.toArray(new String[0]), ";");
            } else {
                workareas = this.getDepartmentInstrumentWorkareas(testingDepartmentid, instrumenttypeid, instrumentmodelid);
            }
            resourceRequirements.setString(i, "_resourcesworkareas", (String)workareas);
        }
    }

    public String getDepartmentInstruments(String testingDepartmentid, String workareaDepartmentid, String instrumenttypeid, String instrumentmodelid) {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT instrumentid FROM instrument WHERE instrumenttype = " + safeSQL.addVar(instrumenttypeid);
        if (instrumentmodelid != null && instrumentmodelid.length() > 0) {
            sql = sql + " AND instrumentmodelid=" + safeSQL.addVar(instrumentmodelid);
        }
        if (testingDepartmentid != null && testingDepartmentid.length() > 0) {
            sql = sql + " AND testingdepartmentid=" + safeSQL.addVar(testingDepartmentid);
        }
        if (workareaDepartmentid != null && workareaDepartmentid.length() > 0) {
            sql = sql + " AND workareadepartmentid=" + safeSQL.addVar(workareaDepartmentid);
        }
        sql = sql + " order by instrumentid";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        String list = ds.getColumnValues("instrumentid", ";");
        return list;
    }

    public String getPermanentWorkareaUsers(String workareaid) {
        return this.getDepartmentUsers(workareaid);
    }

    public String getUserWorkareas(String userid) {
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT department.departmentid FROM departmentsysuser, department  WHERE departmentsysuser.sysuserid = ? AND departmentsysuser.departmentid = department.departmentid  AND department.workassignmentflag='Y' order by department.departmentid", new Object[]{userid});
        String list = ds.getColumnValues("departmentid", ";");
        return list;
    }

    public String getInstrumentWorkarea(String instrumentid) {
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT workareadepartmentid FROM instrument WHERE instrumentid = ?", new Object[]{instrumentid});
        String list = ds.getColumnValues("workareadepartmentid", ";");
        return list;
    }

    public String getDepartmentUsers(String departmentid) {
        String list = (String)CacheUtil.get(this.databaseid, "DepartmentUsers", departmentid);
        if (list == null) {
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT sysuser.sysuserid, departmentsysuser.shiftid FROM departmentsysuser, sysuser WHERE departmentsysuser.sysuserid = sysuser.sysuserid AND ( sysuser.disabledflag is null or sysuser.disabledflag = 'N' ) AND departmentsysuser.departmentid = ? AND ( transientflag is null or transientflag='N' ) order by sysuser.sysuserid", new Object[]{departmentid});
            list = ds.getColumnValues("sysuserid", ";");
            CacheUtil.put(this.databaseid, "DepartmentUsers", departmentid, list);
        }
        return list;
    }

    public String getDepartmentUserWorkareas(String testinglabid) {
        String list = this.departmentWorkareaCache.get(testinglabid);
        if (list == null) {
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT departmentid FROM department WHERE workassignmentflag='Y' AND parentdepartmentid=? order by departmentid", new Object[]{testinglabid});
            list = ds.getColumnValues("departmentid", ";");
            this.departmentWorkareaCache.put(testinglabid, list);
        }
        return list;
    }

    public String getDepartmentInstrumentWorkareas(String testinglabid, String instrumenttypeid, String instrumentmodelid) {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT DISTINCT departmentid FROM department, instrument WHERE department.workassignmentflag='Y' AND department.parentdepartmentid=" + safeSQL.addVar(testinglabid) + " AND  department.departmentid = instrument.workareadepartmentid";
        if (instrumenttypeid.length() > 0) {
            sql = sql + " AND instrument.instrumenttype=" + safeSQL.addVar(instrumenttypeid);
        }
        if (instrumentmodelid.length() > 0) {
            sql = sql + " AND instrument.instrumentmodelid=" + safeSQL.addVar(instrumentmodelid);
        }
        sql = sql + " ORDER BY departmentid";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        String list = ds.getColumnValues("departmentid", ";");
        return list;
    }

    public void updateResourceSDIByNum(String activityid, DataSet resources, String resourceNum, String resourceSDI, String resourceSDCID) throws ActionException {
        String[] resourceNumList = StringUtil.split(resourceNum, ";");
        String[] resourceIdList = StringUtil.split(resourceSDI, ";");
        String[] resourceSDCIDList = StringUtil.split(resourceSDCID, ";");
        if (resourceNumList.length != resourceIdList.length) {
            throw new ActionException("Mismatch between the list of resource nums and resource ids");
        }
        if (resourceSDCID.length() > 0 && resourceNumList.length != resourceSDCIDList.length) {
            throw new ActionException("Mismatch between the list of resource nums and resource sdcids");
        }
        for (int i = 0; i < resourceNumList.length; ++i) {
            boolean isWorkarea;
            int row = resources.findRow("resourcenum", resourceNumList[i]);
            if (row < 0) continue;
            String resourcetypeflag = resources.getValue(row, "resourcetypeflag");
            boolean bl = isWorkarea = resourceSDCID.length() > 0 && resourceSDCIDList[i].equals("Department");
            if ("A".equals(resourcetypeflag)) {
                resources.setString(row, isWorkarea ? "_workareaid" : "_analystid", resourceIdList[i]);
                resources.setString(row, "_modified", "Y");
                continue;
            }
            if (!"I".equals(resourcetypeflag)) continue;
            resources.setString(row, isWorkarea ? "_workareaid" : "_instrumentid", resourceIdList[i]);
            resources.setString(row, "_modified", "Y");
        }
        this.updateResourceSDIs(activityid, resources);
    }

    public void resetResources(String activityid) throws ActionException {
        DataSet resources = this.getActivityResources(activityid);
        for (int i = resources.size() - 1; i >= 0; --i) {
            if (!resources.getValue(i, "fixedresourceflag").equals("Y")) continue;
            resources.deleteRow(i);
        }
        if (resources.size() > 0) {
            resources.setString(-1, "analystid", "(null)");
            resources.setString(-1, "instrumentid", "(null)");
            resources.setString(-1, "workareadepartmentid", "(null)");
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", "LV_Activity");
            actionProps.setProperty("linkid", "Activity Resources");
            actionProps.setProperty("keyid1", activityid);
            actionProps.setProperty("resourcenum", resources.getColumnValues("resourcenum", ";"));
            actionProps.setProperty("analystid", resources.getColumnValues("analystid", ";"));
            actionProps.setProperty("instrumentid", resources.getColumnValues("instrumentid", ";"));
            actionProps.setProperty("workareadepartmentid", resources.getColumnValues("workareadepartmentid", ";"));
            this.getActionProcessor().processAction("EditSDIDetail", "1", actionProps);
        }
    }

    public void updateResourceSDIsByInferring(String activityid, DataSet resources, String analystid, String analysttype, String analystworkareaid, String instrumentid, String instrumentworkareaid, String instrumenttypeid, String instrumentmodelid) throws ActionException {
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("resourcetypeflag", "I");
        DataSet resourceRequirementsInstruments = resources.getFilteredDataSet(filter);
        filter.put("resourcetypeflag", "A");
        DataSet resourceRequirementsAnalysts = resources.getFilteredDataSet(filter);
        DataSet instrumentDS = null;
        if (resourceRequirementsInstruments.size() > 1 && instrumenttypeid.length() == 0 && instrumentid.length() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            DataSet instrumentsDS = this.getQueryProcessor().getPreparedSqlDataSet("SELECT instrumentid, instrumenttype, instrumentmodelid FROM instrument where instrumentid in (" + safeSQL.addIn(instrumentid, ";") + ")", safeSQL.getValues());
            if (instrumentsDS.size() > 0) {
                instrumentid = instrumentsDS.getColumnValues("instrumentid", ";");
                instrumenttypeid = instrumentsDS.getColumnValues("instrumenttype", ";");
                instrumentmodelid = instrumentsDS.getColumnValues("instrumentmodelid", ";");
            }
        }
        if (instrumentid.length() > 0 || instrumentworkareaid.length() > 0) {
            instrumentDS = new DataSet();
            instrumentDS.addColumnValues("instrumentid", 0, instrumentid, ";");
            instrumentDS.addColumnValues("instrumentworkareaid", 0, instrumentworkareaid, ";");
            instrumentDS.addColumnValues("instrumenttypeid", 0, instrumenttypeid, ";");
            instrumentDS.addColumnValues("instrumentmodelid", 0, instrumentmodelid, ";");
        }
        DataSet analystDS = null;
        if (analystid.length() > 0 || analystworkareaid.length() > 0) {
            analystDS = new DataSet();
            analystDS.addColumnValues("analystid", 0, analystid, ";");
            analystDS.addColumnValues("analystworkareaid", 0, analystworkareaid, ";");
            analystDS.addColumnValues("analysttype", 0, analysttype, ";");
        }
        for (int i = 0; i < resources.size(); ++i) {
            int foundRow;
            String requiredType;
            HashMap<String, String> findMap;
            String resourcetypeflag = resources.getValue(i, "resourcetypeflag");
            if ("A".equals(resourcetypeflag)) {
                if (resourceRequirementsAnalysts.size() == 1) {
                    if (analystid.length() > 0) {
                        resources.setString(i, "_analystid", analystid);
                        resources.setString(i, "_modified", "Y");
                        continue;
                    }
                    if (analystworkareaid.length() <= 0) continue;
                    resources.setString(i, "_workareaid", analystworkareaid);
                    resources.setString(i, "_modified", "Y");
                    continue;
                }
                if (analystDS == null) continue;
                findMap = new HashMap<String, String>();
                requiredType = resources.getValue(i, "analysttype", "Analyst");
                findMap.put("analysttype", requiredType);
                int foundRow2 = analystDS.findRow(findMap);
                if (foundRow2 < 0) continue;
                String foundAnalystid = analystDS.getValue(foundRow2, "analystid");
                String foundAnalystWorkarea = analystDS.getValue(foundRow2, "analystworkareaid");
                if (foundAnalystid.length() > 0) {
                    resources.setString(i, "_analystid", foundAnalystid);
                    resources.setString(i, "_modified", "Y");
                    continue;
                }
                if (foundAnalystWorkarea.length() <= 0) continue;
                resources.setString(i, "_workareaid", foundAnalystWorkarea);
                resources.setString(i, "_modified", "Y");
                continue;
            }
            if (!"I".equals(resourcetypeflag)) continue;
            if (resourceRequirementsInstruments.size() == 1) {
                if (instrumentid.length() > 0) {
                    resources.setString(i, "_instrumentid", instrumentid);
                    resources.setString(i, "_modified", "Y");
                    continue;
                }
                if (instrumentworkareaid.length() <= 0) continue;
                resources.setString(i, "_workareaid", instrumentworkareaid);
                resources.setString(i, "_modified", "Y");
                continue;
            }
            if (instrumentDS == null) continue;
            findMap = new HashMap();
            requiredType = resources.getValue(i, "instrumenttypeid");
            String requiredModel = resources.getValue(i, "instrumentmodelid");
            findMap.put("instrumenttypeid", requiredType);
            if (requiredModel.length() > 0) {
                findMap.put("instrumentmodelid", requiredModel);
            }
            if ((foundRow = instrumentDS.findRow(findMap)) == -1) {
                findMap.remove("instrumentmodelid");
                foundRow = instrumentDS.findRow(findMap);
            }
            if (foundRow < 0) continue;
            String foundInstrumentid = instrumentDS.getValue(foundRow, "instrumentid");
            String foundInstrumentWorkarea = instrumentDS.getValue(foundRow, "instrumentworkareaid");
            if (foundInstrumentid.length() > 0) {
                resources.setString(i, "_instrumentid", foundInstrumentid);
                resources.setString(i, "_modified", "Y");
                continue;
            }
            if (foundInstrumentWorkarea.length() <= 0) continue;
            resources.setString(i, "_workareaid", foundInstrumentWorkarea);
            resources.setString(i, "_modified", "Y");
        }
        this.updateResourceSDIs(activityid, resources);
    }

    private void updateResourceSDIs(String activityid, DataSet resources) throws ActionException {
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("_modified", "Y");
        DataSet updates = resources.getFilteredDataSet(filter);
        for (int i = updates.size() - 1; i >= 0; --i) {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", "LV_Activity");
            actionProps.setProperty("linkid", "Activity Resources");
            actionProps.setProperty("keyid1", activityid);
            boolean processAction = false;
            actionProps.setProperty("resourcenum", updates.getValue(i, "resourcenum"));
            String resourcetypeflag = updates.getValue(i, "resourcetypeflag");
            String analystid = updates.getValue(i, "_analystid");
            String instrumentid = updates.getValue(i, "_instrumentid");
            String workareaid = updates.getValue(i, "_workareaid");
            if ("A".equals(resourcetypeflag)) {
                if (analystid.length() > 0) {
                    actionProps.setProperty("analystid", analystid);
                    actionProps.setProperty("workareadepartmentid", "(null)");
                } else {
                    if (workareaid.length() > 0) {
                        actionProps.setProperty("workareadepartmentid", workareaid);
                    } else {
                        actionProps.setProperty("workareadepartmentid", "(null)");
                    }
                    actionProps.setProperty("analystid", "(null)");
                }
                processAction = true;
            } else if ("I".equals(resourcetypeflag)) {
                if (instrumentid.length() > 0) {
                    actionProps.setProperty("instrumentid", instrumentid);
                    actionProps.setProperty("workareadepartmentid", "(null)");
                } else {
                    actionProps.setProperty("workareadepartmentid", workareaid);
                    actionProps.setProperty("instrumentid", "(null)");
                }
                processAction = true;
            }
            if (!processAction) continue;
            this.getActionProcessor().processAction("EditSDIDetail", "1", actionProps);
        }
    }

    public NewWorkDetails addWorkSDI(String activityid, String worksdcid, String workkeyid1, String workkeyid2, String workkeyid3) throws SapphireException {
        return this.addWorkSDI(this.getActivityDetails(activityid), worksdcid, workkeyid1, workkeyid2, workkeyid3, null);
    }

    public NewWorkDetails addWorkSDI(Activity activity, String worksdcid, String workkeyid1, String workkeyid2, String workkeyid3, NewWorkDetails newWorkDetails) throws SapphireException {
        Trace.logInfo(LOGNAME, "Adding sdis for " + activity.getActivityid());
        if (worksdcid.length() == 0 || workkeyid1.length() == 0) {
            throw new SapphireException("No work details were provided");
        }
        DataSet temp = new DataSet();
        temp.addColumnValues("keyid1", 0, activity.getActivityid(), ";");
        temp.addColumnValues("worksdcid", 0, worksdcid, ";");
        temp.addColumnValues("workkeyid1", 0, workkeyid1, ";");
        temp.addColumnValues("workkeyid2", 0, workkeyid2, ";", "__null");
        temp.addColumnValues("workkeyid3", 0, workkeyid3, ";", "__null");
        temp.padColumns();
        ActivityClassHandler activityClassHandler = ActivityClassHandler.getInstance(this.getConnectionid(), this.getWapPolicy(), activity.getActivityClass(), worksdcid);
        if (newWorkDetails == null) {
            newWorkDetails = this.getNewWorkDetails(activityClassHandler, worksdcid, workkeyid1);
        }
        int smallestMaxActivitySize = Math.min(newWorkDetails.getMaxActivitySize(), activity.getMaxActivitySize());
        if (newWorkDetails.getTestingDepartmentid().length() > 0 && activity.getTestingDepartmentid().length() > 0 && !newWorkDetails.getTestingDepartmentid().equals(activity.getTestingDepartmentid())) {
            throw new SapphireException("The new work is for a different testing department");
        }
        String mergedWorkContext = this.mergeWorkContext(activity.getWorkContext(), newWorkDetails.getWorkContext());
        newWorkDetails.setWorkContext(mergedWorkContext);
        int currentWorkCount = this.getActivityWorkSDICount(activity.getActivityid());
        if (currentWorkCount + temp.size() > smallestMaxActivitySize) {
            throw new SapphireException("Maximum number of worksdis (" + smallestMaxActivitySize + ") exceeded.");
        }
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty("sdcid", "LV_Activity");
        actionProps.setProperty("linkid", "Activity Work List");
        actionProps.setProperty("keyid1", temp.getColumnValues("keyid1", ";"));
        actionProps.setProperty("worksdcid", temp.getColumnValues("worksdcid", ";"));
        actionProps.setProperty("workkeyid1", temp.getColumnValues("workkeyid1", ";"));
        actionProps.setProperty("workkeyid2", temp.getColumnValues("workkeyid2", ";"));
        actionProps.setProperty("workkeyid3", temp.getColumnValues("workkeyid3", ";"));
        this.getActionProcessor().processAction("AddSDIDetail", "1", actionProps);
        this.setWAPStatus(worksdcid, workkeyid1, workkeyid2, workkeyid3, "Assigned", false);
        DataSet existingResources = this.getActivityResources(activity.getActivityid());
        DataSet newResources = newWorkDetails.getResourceRequirements();
        if (existingResources.size() == 0) {
            int workCount = StringUtil.split(workkeyid1, ";").length;
            this.addActivityResources(activity.getActivityid(), newResources, workCount);
        } else if (!this.areResourceRequirementsCompatible(existingResources, newResources, false)) {
            throw new SapphireException("Resources for the new work do not match the current resources requirements");
        }
        PropertyList plannableProps = this.getPlannableSDCProperties(worksdcid);
        if (plannableProps != null) {
            String fixedinstrumentcolumnid = plannableProps.getProperty("fixedinstrumentcolumnid");
            String fixedinstrumentduration = plannableProps.getProperty("fixedinstrumentduration");
            if (fixedinstrumentcolumnid.length() > 0 && fixedinstrumentduration.length() > 0) {
                SDCProcessor sdcp = this.getSDCProcessor();
                String tableid = sdcp.getProperty(worksdcid, "tableid");
                String keycolid1 = sdcp.getProperty(worksdcid, "keycolid1");
                SafeSQL safeSQL = new SafeSQL();
                DataSet fixedInstruments = this.getQueryProcessor().getPreparedSqlDataSet("SELECT instrument.instrumenttype, instrument.instrumentmodelid, " + tableid + "." + fixedinstrumentcolumnid + "," + tableid + "." + fixedinstrumentduration + " FROM instrument, " + tableid + " WHERE " + tableid + "." + fixedinstrumentcolumnid + "=instrument.instrumentid AND " + fixedinstrumentduration + " is not null AND " + keycolid1 + " in (" + safeSQL.addIn(workkeyid1, ";") + ")", safeSQL.getValues());
                DataSet existing = this.getActivityResources(activity.getActivityid());
                existing.sort("resourcenum");
                int nextResourcenum = existing.size() == 0 ? 0 : existing.getInt(existing.size() - 1, "resourcenum") + 1;
                for (int i = 0; i < fixedInstruments.size(); ++i) {
                    String instrumentid = fixedInstruments.getValue(i, fixedinstrumentcolumnid);
                    if (instrumentid.length() <= 0) continue;
                    String duration = fixedInstruments.getValue(i, fixedinstrumentduration);
                    this.addActivityResource(activity.getActivityid(), "I", nextResourcenum + i, "", fixedInstruments.getValue(0, "instrumenttype"), fixedInstruments.getValue(0, "instrumentmodelid"), instrumentid, "", duration + " minutes", 1, true);
                }
            }
        }
        return newWorkDetails;
    }

    private String mergeWorkContext(String oldWorkContext, String newWorkContext) throws SapphireException {
        oldWorkContext = oldWorkContext == null ? "" : oldWorkContext;
        String string = newWorkContext = newWorkContext == null ? "" : newWorkContext;
        if (oldWorkContext.length() == 0 && newWorkContext.length() == 0) {
            return "";
        }
        if (oldWorkContext.length() == 0 && newWorkContext.length() > 0) {
            return newWorkContext;
        }
        if (oldWorkContext.length() > 0 && newWorkContext.length() == 0) {
            return oldWorkContext;
        }
        if (oldWorkContext.equals(newWorkContext)) {
            return oldWorkContext;
        }
        ContextMap oldMap = new ContextMap(oldWorkContext);
        ContextMap newMap = new ContextMap(newWorkContext);
        if (oldMap.get("WSDC").length() > 0 && newMap.get("WSDC").length() > 0 && !oldMap.get("WSDC").equals(newMap.get("WSDC"))) {
            throw new SapphireException("New worksdis belong to a different SDCs.");
        }
        String oldMDSDI = oldMap.get("MDSDI");
        String newMDSDI = newMap.get("MDSDI");
        HashSet<String> s = new HashSet<String>(Arrays.asList(StringUtil.split(oldMDSDI, "|")));
        s.addAll(new HashSet<String>(Arrays.asList(StringUtil.split(newMDSDI, "|"))));
        oldMap.put("MDSDI", String.join((CharSequence)"|", s));
        return oldMap.toString();
    }

    public boolean areResourceRequirementsCompatible(DataSet currentResources, DataSet newResources, boolean setCurrentModelIfNotPresent) {
        if (newResources.size() != currentResources.size()) {
            return false;
        }
        currentResources.sort("resourcetypeflag,instrumenttypeid,instrumentmodelid");
        newResources.sort("resourcetypeflag,instrumenttypeid,instrumentmodelid");
        for (int i = 0; i < newResources.size(); ++i) {
            if (!newResources.getValue(i, "resourcetypeflag").equals(currentResources.getValue(i, "resourcetypeflag"))) {
                return false;
            }
            if (!newResources.getValue(i, "instrumenttypeid").equals(currentResources.getValue(i, "instrumenttypeid"))) {
                return false;
            }
            if (!newResources.getValue(i, "durationrule").equalsIgnoreCase(currentResources.getValue(i, "durationrule"))) {
                return false;
            }
            String currentModel = currentResources.getValue(i, "instrumentmodelid");
            String requiredModel = newResources.getValue(i, "instrumentmodelid");
            if (currentModel.length() == 0 && requiredModel.length() > 0) {
                if (setCurrentModelIfNotPresent) {
                    currentResources.setString(i, "instrumentmodelid", requiredModel);
                    continue;
                }
                return false;
            }
            if (requiredModel.length() <= 0 || currentModel.length() <= 0 || requiredModel.equals(currentModel)) continue;
            return false;
        }
        return true;
    }

    public void setWAPStatus(String worksdcid, String workkeyid1, String workkeyid2, String workkeyid3, String status, boolean omitCancelled) throws ActionException {
        Trace.logInfo(LOGNAME, "Updating WAPStatus to " + status);
        if (worksdcid.equals("SDIWorkItem")) {
            SafeSQL safeSQL = new SafeSQL();
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select sdcid, keyid1, keyid2, keyid3, workitemid, workiteminstance from sdiworkitem WHERE sdiworkitemid in (" + safeSQL.addIn(workkeyid1, ";") + ") " + (omitCancelled ? " AND wapstatus<>'Cancelled'" : ""), safeSQL.getValues());
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", ds.getString(0, "sdcid"));
            actionProps.setProperty("keyid1", ds.getColumnValues("keyid1", ";"));
            actionProps.setProperty("keyid2", ds.getColumnValues("keyid2", ";"));
            actionProps.setProperty("keyid3", ds.getColumnValues("keyid3", ";"));
            actionProps.setProperty("workitemid", ds.getColumnValues("workitemid", ";"));
            actionProps.setProperty("workiteminstance", ds.getColumnValues("workiteminstance", ";"));
            actionProps.setProperty("propsmatch", "Y");
            actionProps.setProperty("wapstatus", status);
            this.getActionProcessor().processAction("EditSDIWorkItem", "1", actionProps);
        } else if (worksdcid.equals("DataSet")) {
            SafeSQL safeSQL = new SafeSQL();
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset from sdidata where sdidataid in (" + safeSQL.addIn(workkeyid1, ";") + ")" + (omitCancelled ? " AND wapstatus<>'Cancelled'" : ""), safeSQL.getValues());
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", ds.getString(0, "sdcid"));
            actionProps.setProperty("keyid1", ds.getColumnValues("keyid1", ";"));
            actionProps.setProperty("keyid2", ds.getColumnValues("keyid2", ";"));
            actionProps.setProperty("keyid3", ds.getColumnValues("keyid3", ";"));
            actionProps.setProperty("paramlistid", ds.getColumnValues("paramlistid", ";"));
            actionProps.setProperty("paramlistversionid", ds.getColumnValues("paramlistversionid", ";"));
            actionProps.setProperty("variantid", ds.getColumnValues("variantid", ";"));
            actionProps.setProperty("dataset", ds.getColumnValues("dataset", ";"));
            actionProps.setProperty("propsmatch", "Y");
            actionProps.setProperty("wapstatus", status);
            this.getActionProcessor().processAction("EditDataSet", "1", actionProps);
        } else if (worksdcid.equals("Sample")) {
            String thiskeyid1;
            if (status.equals("Cancelled")) {
                SafeSQL safeSQL = new SafeSQL();
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_sampleid from s_sample where s_sampleid in (" + safeSQL.addIn(workkeyid1, ";") + ")" + (omitCancelled ? " AND wapstatus<>'Cancelled'" : ""), safeSQL.getValues());
                thiskeyid1 = ds.getColumnValues("s_sampleid", ";");
            } else {
                thiskeyid1 = workkeyid1;
            }
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", "Sample");
            actionProps.setProperty("keyid1", thiskeyid1);
            actionProps.setProperty("wapstatus", status);
            this.getActionProcessor().processAction("EditSDI", "1", actionProps);
        } else {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", worksdcid);
            actionProps.setProperty("keyid1", workkeyid1);
            actionProps.setProperty("wapstatus", status);
            this.getActionProcessor().processAction("EditSDI", "1", actionProps);
        }
    }

    public void syncActivityWorkDetails(Activity activity, NewWorkDetails newWorkDetails, boolean updateContextSDI) throws SapphireException {
        String testingDepartmentid;
        String worksdcid;
        Activity editActivity = new Activity();
        editActivity.setActivityid(activity.getActivityid());
        if (updateContextSDI) {
            editActivity.setActivityContextSdcid(activity.getActivityContextSdcid());
            editActivity.setActivityContextKeyid1(activity.getActivityContextKeyid1());
            editActivity.setActivityContextKeyid2(activity.getActivityContextKeyid2());
            editActivity.setActivityContextKeyid3(activity.getActivityContextKeyid3());
        }
        if ((worksdcid = activity.getWorksdcid()).length() == 0) {
            editActivity.setWorksdcid(newWorkDetails.getWorksdcid());
            activity.setWorksdcid(newWorkDetails.getWorksdcid());
        }
        if ((testingDepartmentid = activity.getTestingDepartmentid()).length() == 0) {
            editActivity.setTestingDepartmentid(newWorkDetails.getTestingDepartmentid());
            activity.setTestingDepartmentid(newWorkDetails.getTestingDepartmentid());
        }
        if (!activity.getWorkContext().equals(newWorkDetails.getWorkContext())) {
            editActivity.setWorkContext(newWorkDetails.getWorkContext());
            activity.setWorkContext(newWorkDetails.getWorkContext());
        }
        int currentWorkCount = this.getActivityWorkSDICount(activity.getActivityid());
        editActivity.setActivitySize(activity.getActivitySize() != currentWorkCount ? currentWorkCount : -1);
        activity.setActivitySize(activity.getActivitySize() != currentWorkCount ? currentWorkCount : -1);
        editActivity.setMaxActivitySize(activity.getMaxActivitySize() != newWorkDetails.getMaxActivitySize() ? newWorkDetails.getMaxActivitySize() : -1);
        activity.setMaxActivitySize(activity.getMaxActivitySize() != newWorkDetails.getMaxActivitySize() ? newWorkDetails.getMaxActivitySize() : -1);
        this.editActivity(editActivity, false);
    }

    public void syncMaxDurationEndDateDueDateCompleteCount(Activity activity) throws SapphireException {
        DataSet resources = this.getActivityResources(activity.getActivityid());
        int maxDuration = 0;
        int foundRow = 0;
        for (int i = 0; i < resources.size(); ++i) {
            int duration = resources.getInt(i, "duration");
            if (duration <= maxDuration) continue;
            maxDuration = duration;
            foundRow = i;
        }
        int completeCount = this.getWorkCompleteCount(activity);
        boolean completeCountChanged = completeCount != activity.getWorkCompleteCount();
        Instant dueDate = this.getWorkDueDate(activity);
        boolean dueDateChanged = (dueDate != null || activity.getWorkDuedt() != null) && (dueDate == null && activity.getWorkDuedt() != null || dueDate != null && activity.getWorkDuedt() == null || dueDate.getEpochSecond() != activity.getWorkDuedt().getEpochSecond());
        boolean durationChanged = activity.getMaxDurationMinutes() != maxDuration;
        boolean updateEnddt = activity.getEndDateInstantUTC() == null && activity.getStartDateInstantUTC() != null;
        updateEnddt |= durationChanged && activity.getStartDateInstantUTC() != null;
        if (durationChanged || (updateEnddt &= !activity.isEndDateFixed()) || dueDateChanged) {
            SafeSQL safeSQL = new SafeSQL();
            StringBuilder sql = new StringBuilder();
            if (durationChanged) {
                sql.append(sql.length() > 0 ? "," : "");
                sql.append("maxduration=").append(safeSQL.addVar(maxDuration));
            }
            if (dueDateChanged) {
                sql.append(sql.length() > 0 ? "," : "");
                sql.append("workduedt=").append(safeSQL.addVar(dueDate == null ? null : this.calendarConverter.convertInstantUtcToDatabaseTimestamp(dueDate)));
            }
            if (completeCountChanged) {
                sql.append(sql.length() > 0 ? "," : "");
                sql.append("workcompletecount=").append(safeSQL.addVar(completeCount));
            }
            if (updateEnddt) {
                CalendarFactory factory;
                String testinglabid = activity.getTestingDepartmentid();
                String analystid = resources.getValue(foundRow, "analystid");
                String instrumentid = resources.getValue(foundRow, "instrumentid");
                String workareaid = resources.getValue(foundRow, "workareadepartmentid");
                int durationMinutes = resources.getInt(foundRow, "duration");
                CalendarFactory calendarFactory = factory = this.getRakFile() == null ? new CalendarFactory(this.getConnectionid()) : new CalendarFactory(this.getConnectionid(), this.getRakFile());
                LVCalendar calendar = analystid.length() > 0 ? factory.getUserCalendar(analystid, true, true) : (instrumentid.length() > 0 ? factory.getInstrumentCalendar(instrumentid, true, true) : (workareaid.length() > 0 ? factory.getDepartmentCalendar(workareaid, true, true) : (testinglabid != null && testinglabid.length() > 0 ? factory.getDepartmentCalendar(testinglabid, true, true) : null)));
                Instant enddt = calendar == null ? activity.getStartDateInstantUTC().plus((long)durationMinutes, ChronoUnit.MINUTES) : this.calculateEndDtOffset(activity.getStartDateInstantUTC(), durationMinutes, calendar);
                sql.append(sql.length() > 0 ? "," : "");
                sql.append("enddt=" + safeSQL.addVar(this.calendarConverter.convertInstantUtcToDatabaseTimestamp(enddt)));
            }
            sql.append(" WHERE activityid=" + safeSQL.addVar(activity.getActivityid()));
            this.getQueryProcessor().execPreparedUpdate("UPDATE activity SET " + sql, safeSQL.getValues());
        }
    }

    public Instant getWorkDueDate(Activity activity) throws SapphireException {
        String duedateColumn;
        String worksdcid = activity.getWorksdcid();
        if (worksdcid.equals("SDIWorkItem")) {
            String sql = "SELECT min( coalesce(sdiworkitem.duedt, s_sample.duedt )) minduedt FROM sdiworkitem, s_sample, activityworksdi WHERE s_sample.s_sampleid = sdiworkitem.keyid1      AND activityworksdi.activityid = ?      AND activityworksdi.worksdcid = 'SDIWorkItem'      AND ( sdiworkitem.duedt is not null OR s_sample.duedt is not null )      AND sdiworkitem.sdiworkitemid = activityworksdi.workkeyid1";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{activity.getActivityid()});
            return ds.size() == 1 && ds.getCalendar(0, "minduedt") != null ? ds.getCalendar(0, "minduedt").toInstant() : null;
        }
        if (worksdcid.equals("DataSet")) {
            String sql = "SELECT min( coalesce(sdiworkitem.duedt, s_sample.duedt )) minduedt  FROM sdiworkitem, s_sample, sdidata, activityworksdi, sdiworkitemitem WHERE s_sample.s_sampleid = sdiworkitem.keyid1      AND activityworksdi.worksdcid = 'DataSet'      AND sdidata.sdidataid = activityworksdi.WORKKEYID1      AND sdiworkitemitem.sdcid = sdidata.sdcid      AND sdiworkitemitem.keyid1= sdidata.keyid1      AND sdiworkitemitem.keyid2= sdidata.keyid2      AND sdiworkitemitem.keyid3= sdidata.keyid3      AND sdiworkitemitem.itemkeyid1 = sdidata.paramlistid      AND sdiworkitemitem.itemkeyid2 = sdidata.paramlistversionid      AND sdiworkitemitem.itemkeyid3 = sdidata.variantid      AND sdiworkitemitem.iteminstance = sdidata.dataset      AND sdiworkitem.sdcid = sdiworkitemitem.SDCID      AND sdiworkitem.keyid1= sdiworkitemitem.keyid1      AND sdiworkitem.keyid2= sdiworkitemitem.keyid2      AND sdiworkitem.keyid3= sdiworkitemitem.keyid3      AND sdiworkitem.workitemid= sdiworkitemitem.workitemid      AND sdiworkitem.workiteminstance= sdiworkitemitem.workiteminstance      AND (sdiworkitem.duedt IS NOT NULL OR s_sample.duedt IS NOT NULL)      AND activityworksdi.activityid = ?";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{activity.getActivityid()});
            return ds.size() == 1 && ds.getCalendar(0, "minduedt") != null ? ds.getCalendar(0, "minduedt").toInstant() : null;
        }
        if (worksdcid.equals("Sample")) {
            String sql = "SELECT min( s_sample.duedt ) minduedt FROM s_sample, activityworksdi WHERE s_sample.s_sampleid = activityworksdi.workkeyid1      AND activityworksdi.activityid = ?      AND activityworksdi.worksdcid = 'Sample'";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{activity.getActivityid()});
            return ds.size() == 1 && ds.getCalendar(0, "minduedt") != null ? ds.getCalendar(0, "minduedt").toInstant() : null;
        }
        PropertyList plannableProps = this.getPlannableSDCProperties(worksdcid);
        if (plannableProps != null && (duedateColumn = plannableProps.getProperty("duedatecolumnid")).length() > 0) {
            SDCProcessor sdcp = this.getSDCProcessor();
            String tableid = sdcp.getProperty(worksdcid, "tableid");
            String keycolid1 = sdcp.getProperty(worksdcid, "keycolid1");
            String sql = "SELECT min( " + tableid + "." + duedateColumn + " ) minduedt FROM " + tableid + ", activityworksdi WHERE " + tableid + "." + keycolid1 + " = activityworksdi.workkeyid1      AND activityworksdi.activityid = ?      AND activityworksdi.worksdcid = '" + worksdcid + "'";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{activity.getActivityid()});
            return ds.size() == 1 && ds.getCalendar(0, "minduedt") != null ? ds.getCalendar(0, "minduedt").toInstant() : null;
        }
        return Instant.now();
    }

    private Instant calculateEndDtOffset(Instant startDate, int duration, LVCalendar calendar) {
        int durationRemaining = duration;
        Instant candidateEndDate = startDate.plus((long)duration, ChronoUnit.MINUTES);
        if (calendar != null) {
            WorkHours coreHours = calendar.getCoreHours();
            int emptycount = 0;
            ZonedDateTime tempStart = ZonedDateTime.ofInstant(startDate, calendar.getTimeZone());
            while (durationRemaining > 0 && emptycount < 14) {
                int workingMinutes = WAPAvailability.getWorkingMinutesAfterDataTime(coreHours.getWorkingRanges(tempStart), tempStart);
                if (workingMinutes > 0) {
                    candidateEndDate = tempStart.plus(durationRemaining, ChronoUnit.MINUTES).toInstant();
                    durationRemaining -= workingMinutes;
                    emptycount = 0;
                } else {
                    ++emptycount;
                }
                tempStart = tempStart.plus(1L, ChronoUnit.DAYS).with(ChronoField.HOUR_OF_DAY, coreHours.getStartHour()).with(ChronoField.MINUTE_OF_HOUR, coreHours.getStartMinute());
            }
        }
        return candidateEndDate;
    }

    public void addActivityResources(String activityid, DataSet resourceRequirements, int count) throws SapphireException {
        if (resourceRequirements.size() > 0) {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", "LV_Activity");
            actionProps.setProperty("linkid", "Activity Resources");
            DataSet temp = new DataSet();
            for (int i = 0; i < resourceRequirements.size(); ++i) {
                int row = temp.addRow();
                String resourcetypeflag = resourceRequirements.getValue(i, "resourcetypeflag");
                temp.setString(row, "keyid1", activityid);
                temp.setString(row, "resourcenum", resourceRequirements.getValue(i, "resourcenum"));
                temp.setString(row, "resourcetypeflag", resourcetypeflag);
                if ("A".equals(resourcetypeflag)) {
                    temp.setString(row, "analystid", resourceRequirements.getString(i, "_analystid"));
                    temp.setString(row, "analysttype", resourceRequirements.getValue(i, "analysttype", "Analyst"));
                } else if ("I".equals(resourcetypeflag)) {
                    temp.setString(row, "instrumentid", resourceRequirements.getString(i, "_instrumentid"));
                    temp.setString(row, "instrumenttypeid", resourceRequirements.getValue(i, "instrumenttypeid"));
                    temp.setString(row, "instrumentmodelid", resourceRequirements.getValue(i, "instrumentmodelid"));
                }
                if (count <= 0) continue;
                String durationRule = resourceRequirements.getValue(i, "durationrule");
                int duration = this.getDurationMinutes(durationRule, count);
                temp.setNumber(row, "duration", duration);
                temp.setString(row, "durationrule", durationRule);
                resourceRequirements.setNumber(i, "_duration", duration);
            }
            actionProps.setProperty("keyid1", temp.getColumnValues("keyid1", ";"));
            actionProps.setProperty("resourcenum", temp.getColumnValues("resourcenum", ";"));
            actionProps.setProperty("resourcetypeflag", temp.getColumnValues("resourcetypeflag", ";"));
            actionProps.setProperty("analystid", temp.getColumnValues("analystid", ";"));
            actionProps.setProperty("analysttype", temp.getColumnValues("analysttype", ";"));
            actionProps.setProperty("workareaid", temp.getColumnValues("workareaid", ";"));
            actionProps.setProperty("instrumentid", temp.getColumnValues("instrumentid", ";"));
            actionProps.setProperty("instrumenttypeid", temp.getColumnValues("instrumenttypeid", ";"));
            actionProps.setProperty("instrumentmodelid", temp.getColumnValues("instrumentmodelid", ";"));
            actionProps.setProperty("duration", temp.getColumnValues("duration", ";"));
            actionProps.setProperty("durationrule", temp.getColumnValues("durationrule", ";"));
            this.getActionProcessor().processAction("AddSDIDetail", "1", actionProps);
        }
    }

    public void addActivityResource(String activityid, String resourceTypeFlag, int resourceNum, String analystid, String instrumentTypeid, String instrumentModelid, String instrumentid, String workareaid, String durationRule, int count, boolean fixedResourceFlag) throws ActionException {
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty("sdcid", "LV_Activity");
        actionProps.setProperty("linkid", "Activity Resources");
        actionProps.setProperty("keyid1", activityid);
        actionProps.setProperty("resourcenum", "" + resourceNum);
        actionProps.setProperty("resourcetypeflag", resourceTypeFlag);
        if ("A".equals(resourceTypeFlag)) {
            if (analystid != null && analystid.length() > 0) {
                actionProps.setProperty("analystid", analystid);
            }
        } else if ("I".equals(resourceTypeFlag)) {
            actionProps.setProperty("instrumenttypeid", instrumentTypeid);
            actionProps.setProperty("instrumentmodelid", instrumentModelid);
            if (instrumentid != null && instrumentid.length() > 0) {
                actionProps.setProperty("instrumentid", instrumentid);
            }
        }
        actionProps.setProperty("durationrule", durationRule);
        actionProps.setProperty("duration", "" + this.getDurationMinutes(durationRule, count));
        actionProps.setProperty("fixedresourceflag", fixedResourceFlag ? "Y" : "");
        this.getActionProcessor().processAction("AddSDIDetail", "1", actionProps);
    }

    public int getActivityWorkSDICount(String activityId) {
        try {
            return this.getQueryProcessor().getPreparedCount("SELECT count(*) FROM activityworksdi WHERE activityid=?", new String[]{activityId});
        }
        catch (Exception e) {
            return 0;
        }
    }

    public void updateResourceDurations(String activityid) throws SapphireException {
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty("sdcid", "LV_Activity");
        actionProps.setProperty("linkid", "Activity Resources");
        DataSet resources = this.getActivityResources(activityid);
        if (resources.size() > 0) {
            int count = this.getActivityWorkSDICount(activityid);
            for (int i = 0; i < resources.size(); ++i) {
                String durationRule = resources.getValue(i, "durationrule");
                if (durationRule.length() > 0 && count > 0) {
                    int duration = this.getDurationMinutes(durationRule, count);
                    resources.setNumber(i, "duration", duration);
                    continue;
                }
                resources.setNumber(i, "duration", 0);
            }
            actionProps.setProperty("keyid1", resources.getColumnValues("activityid", ";"));
            actionProps.setProperty("resourcenum", resources.getColumnValues("resourcenum", ";"));
            actionProps.setProperty("duration", resources.getColumnValues("duration", ";"));
            this.getActionProcessor().processAction("EditSDIDetail", "1", actionProps);
        }
    }

    protected int getDurationMinutes(String durationRule, int count) {
        int minutes = 0;
        String[] parts = StringUtil.split(durationRule, " ");
        if (parts.length < 2) {
            minutes = 0;
        }
        if (parts.length >= 2) {
            minutes = this.getMinutes(this.getInt(parts[0]), parts[1]);
        }
        if (parts.length >= 4) {
            int per = this.getInt(parts[3]);
            int lots = 1 + (count - 1) / per;
            minutes = lots * minutes;
        }
        if (parts.length >= 6) {
            int extra = this.getMinutes(this.getInt(parts[5]), parts[6]);
            minutes = parts[4].equals("+") || parts[4].equalsIgnoreCase("plus") ? (minutes += extra) : (minutes -= extra);
        }
        return minutes;
    }

    private int getMinutes(int d, String unit) {
        if (unit.toUpperCase().startsWith("M")) {
            return d;
        }
        if (unit.toUpperCase().startsWith("H")) {
            return d * 60;
        }
        if (unit.toUpperCase().startsWith("D")) {
            return d * 60 * 24;
        }
        if (unit.toUpperCase().startsWith("W")) {
            return d * 60 * 24 * 7;
        }
        return 0;
    }

    protected static String getDurationDisplay(int durationMinutes) {
        int minutes = 0;
        int hours = 0;
        int days = 0;
        if (durationMinutes < 60) {
            minutes = durationMinutes;
        } else if (durationMinutes < 1440) {
            hours = durationMinutes / 60;
            minutes = durationMinutes - 60 * hours;
        } else {
            days = durationMinutes / 1440;
            int remain = durationMinutes - days * 60 * 24;
            hours = remain / 60;
            minutes = remain - 60 * hours;
        }
        StringBuilder out = new StringBuilder();
        if (days > 0) {
            out.append(days + (days == 1 ? " day" : " days"));
        }
        if (hours > 0) {
            if (out.length() > 0) {
                out.append(", ");
            }
            out.append(hours + (hours == 1 ? " hour" : " hours"));
        }
        if (minutes > 0 || out.length() == 0) {
            if (out.length() > 0) {
                out.append(", ");
            }
            out.append(minutes + (minutes == 1 ? " minute" : " minutes"));
        }
        return out.toString();
    }

    public List<String> getWorkareaUsers(String workareaid, Instant rangeFrom, Instant rangeTo) {
        HashSet<String> users = new HashSet<String>();
        WAPSelector selector = new WAPSelector(this.getConnectionid(), this.getRakFile());
        try {
            Map<String, Set<String>> scheduled = selector.getScheduledWorkAreaUsers(workareaid, rangeFrom, rangeTo);
            for (Set<String> value : scheduled.values()) {
                users.addAll(value);
            }
        }
        catch (SapphireException e) {
            this.logger.warn("Failed to retrieve scheduled workareas for workarea " + workareaid + ": " + e.getMessage());
        }
        String permanent = this.getPermanentWorkareaUsers(workareaid);
        users.addAll(Arrays.asList(StringUtil.split(permanent, ";")));
        return new ArrayList<String>(users);
    }

    public DataSet retrieveSDIResourceRequirements(String sdcid, String keyid1, String keyid2, String keyid3) {
        return this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM sdiresourcerequirement WHERE sdcid=? AND keyid1=? AND keyid2=? AND keyid3=?", (Object[])new String[]{sdcid, keyid1, keyid2.length() == 0 ? "(null)" : keyid2, keyid3.length() == 0 ? "(null)" : keyid3});
    }

    public DataSet retrieveWorkitemResourceRequirements(String workitemid, String workitemversionid) {
        return this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM sdiresourcerequirement WHERE sdcid=? AND keyid1=? AND keyid2=? AND ( linktocontext='' or linktocontext is null)", (Object[])new String[]{"WorkItem", workitemid, workitemversionid});
    }

    public DataSet retrieveWorkitemItemResourceRequirements(String workitemid, String workitemversionid, String workitemitemid) {
        DataSet pl;
        DataSet requirements = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM sdiresourcerequirement WHERE sdcid=? AND keyid1=? AND keyid2=? AND linktocontext=?", (Object[])new String[]{"WorkItem", workitemid, workitemversionid, workitemitemid});
        if (requirements.size() == 0 && (pl = this.getQueryProcessor().getPreparedSqlDataSet("SELECT keyid1, keyid2, keyid3 FROM workitemitem WHERE sdcid='ParamList' AND workitemid=? AND workitemversionid=? and workitemitemid=?", (Object[])new String[]{workitemid, workitemversionid, workitemitemid})).size() == 1) {
            String paramlistid = pl.getString(0, "keyid1");
            String paramlistversionid = pl.getString(0, "keyid2");
            String variantid = pl.getString(0, "keyid3");
            if ("C".equals(paramlistversionid)) {
                DataSet plv = this.getQueryProcessor().getPreparedSqlDataSet("SELECT paramlistversionid FROM paramlist WHERE paramlistid=? and variantid=? and ( versionstatus='P' or versionstatus='C' ) order by versionstatus, cast (paramlistversionid as integer) desc", new Object[]{paramlistid, variantid});
                paramlistversionid = plv.getValue(0, "paramlistversionid", "1");
            }
            requirements = this.retrieveSDIResourceRequirements("ParamList", paramlistid, paramlistversionid, variantid);
        }
        return requirements;
    }

    public int getWorkCompleteCount(Activity activity) throws SapphireException {
        String worksdcid = activity.getWorksdcid();
        int count = 0;
        if (worksdcid.equals("Sample")) {
            String sql = "SELECT count(*)  FROM activityworksdi a, s_sample  WHERE a.worksdcid = ? AND a.workkeyid1 = s_sample.s_sampleid  AND (s_sample.samplestatus = 'Reviewed' OR s_sample.samplestatus = 'Cancelled' OR (s_sample.samplestatus='Completed' AND s_sample.reviewrequiredflag='N')) AND a.activityid=?";
            count = this.getQueryProcessor().getPreparedCount(sql, new String[]{"Sample", activity.getActivityid()});
        } else if (worksdcid.equals("SDIWorkItem")) {
            String sql = "SELECT count(*)  FROM activityworksdi a, sdiworkitem  WHERE a.worksdcid = ? AND a.workkeyid1 = sdiworkitem.sdiworkitemid  AND sdiworkitem.workitemstatus in ( 'Completed', 'Cancelled' )  AND a.activityid=?";
            count = this.getQueryProcessor().getPreparedCount(sql, new String[]{"SDIWorkItem", activity.getActivityid()});
        } else if (worksdcid.equals("DataSet")) {
            String sql = "SELECT count(*)  FROM activityworksdi a, sdidata  WHERE a.worksdcid = ? AND a.workkeyid1 = sdidata.sdidataid  AND sdidata.s_datasetstatus in ( 'Completed', 'Cancelled' )  AND a.activityid=?";
            count = this.getQueryProcessor().getPreparedCount(sql, new String[]{"DataSet", activity.getActivityid()});
        } else {
            PropertyList plannableProps = this.getPlannableSDCProperties(worksdcid);
            if (plannableProps != null) {
                SDCProcessor sdcp = this.getSDCProcessor();
                String tableid = sdcp.getProperty(worksdcid, "tableid");
                String keycolid1 = sdcp.getProperty(worksdcid, "keycolid1");
                String cancelledcolumnid = plannableProps.getProperty("cancelcolumnid");
                String cancelledvalues = plannableProps.getProperty("cancelledvalues");
                String completecolumnid = plannableProps.getProperty("completecolumnid");
                String completevalues = plannableProps.getProperty("completevalues");
                if (cancelledcolumnid.length() > 0 && cancelledvalues.length() > 0 && completecolumnid.length() > 0 && completevalues.length() > 0) {
                    SafeSQL safeSQL = new SafeSQL();
                    String sql = "SELECT count(*)  FROM activityworksdi a, " + tableid + "  WHERE a.worksdcid=" + safeSQL.addVar(worksdcid) + " AND a.workkeyid1 = " + tableid + "." + keycolid1 + " AND (" + tableid + "." + cancelledcolumnid + " in ( " + safeSQL.addIn(cancelledvalues, ";") + ") OR " + tableid + "." + completecolumnid + " in ( " + safeSQL.addIn(completevalues, ";") + ") ) AND a.activityid=" + safeSQL.addVar(activity.getActivityid());
                    count = this.getQueryProcessor().getPreparedCount(sql, safeSQL.getValues());
                } else if (completecolumnid.length() > 0 && completevalues.length() > 0) {
                    SafeSQL safeSQL = new SafeSQL();
                    String sql = "SELECT count(*)  FROM activityworksdi a, " + tableid + "  WHERE a.worksdcid=" + safeSQL.addVar(worksdcid) + " AND a.workkeyid1 = " + tableid + "." + keycolid1 + " AND " + tableid + "." + completecolumnid + " in ( " + safeSQL.addIn(completevalues, ";") + ") AND a.activityid=" + safeSQL.addVar(activity.getActivityid());
                    count = this.getQueryProcessor().getPreparedCount(sql, safeSQL.getValues());
                }
            }
        }
        return count;
    }

    public int getWorkCancelledCount(Activity activity) throws SapphireException {
        String worksdcid = activity.getWorksdcid();
        int count = 0;
        if (worksdcid.equals("Sample")) {
            String sql = "SELECT count(*)  FROM activityworksdi a, s_sample  WHERE a.worksdcid = ? AND a.workkeyid1 = s_sample.s_sampleid  AND s_sample.samplestatus = 'Cancelled'  AND a.activityid=?";
            count = this.getQueryProcessor().getPreparedCount(sql, new String[]{"Sample", activity.getActivityid()});
        } else if (worksdcid.equals("SDIWorkItem")) {
            String sql = "SELECT count(*)  FROM activityworksdi a, sdiworkitem  WHERE a.worksdcid = ? AND a.workkeyid1 = sdiworkitem.sdiworkitemid  AND sdiworkitem.workitemstatus in ( 'Cancelled' )  AND a.activityid=?";
            count = this.getQueryProcessor().getPreparedCount(sql, new String[]{"SDIWorkItem", activity.getActivityid()});
        } else if (worksdcid.equals("DataSet")) {
            String sql = "SELECT count(*)  FROM activityworksdi a, sdidata  WHERE a.worksdcid = ? AND a.workkeyid1 = sdidata.sdidataid  AND sdidata.s_datasetstatus in ( 'Cancelled' )  AND a.activityid=?";
            count = this.getQueryProcessor().getPreparedCount(sql, new String[]{"DataSet", activity.getActivityid()});
        } else {
            PropertyList plannableProps = this.getPlannableSDCProperties(worksdcid);
            if (plannableProps != null) {
                SDCProcessor sdcp = this.getSDCProcessor();
                String tableid = sdcp.getProperty(worksdcid, "tableid");
                String keycolid1 = sdcp.getProperty(worksdcid, "keycolid1");
                String cancelledcolumnid = plannableProps.getProperty("cancelcolumnid");
                String cancelledvalues = plannableProps.getProperty("cancelledvalues");
                if (cancelledcolumnid.length() > 0 && cancelledvalues.length() > 0) {
                    SafeSQL safeSQL = new SafeSQL();
                    String sql = "SELECT count(*)  FROM activityworksdi a, " + tableid + "  WHERE a.worksdcid = " + safeSQL.addVar(worksdcid) + " AND a.workkeyid1 = " + tableid + "." + keycolid1 + " AND " + tableid + "." + cancelledcolumnid + " in ( " + safeSQL.addIn(cancelledvalues, ";") + " )  AND a.activityid = " + safeSQL.addVar(activity.getActivityid());
                    count = this.getQueryProcessor().getPreparedCount(sql, safeSQL.getValues());
                }
            }
        }
        return count;
    }

    public PropertyList getPlannableSDCProperties(String worksdcid) {
        PropertyListCollection plannablesdcs = this.getWapPolicy().getCollectionNotNull("plannablesdcs");
        return plannablesdcs.find("sdcid", worksdcid);
    }

    private int getInt(String s) {
        return Integer.parseInt(s);
    }

    public String getActivitiesForWorkRset(String worksdcid, String rsetid) {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT DISTINCT activity.activityid FROM activity, activityworksdi,rsetitems WHERE activity.activitystatus<>" + safeSQL.addVar("Cancelled") + "   AND activity.worksdcid=" + safeSQL.addVar(worksdcid) + "   AND activity.activityid=activityworksdi.activityid    AND activityworksdi.worksdcid=rsetitems.sdcid   AND activityworksdi.workkeyid1=rsetitems.keyid1   AND activityworksdi.workkeyid2=rsetitems.keyid2   AND activityworksdi.workkeyid3=rsetitems.keyid3   AND rsetitems.rsetid=" + safeSQL.addVar(rsetid);
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        return ds.getColumnValues("activityid", ";");
    }

    public String getActivityForWorkSDI(String worksdcid, String workkeyid1, String workkeyid2, String workkeyid3) {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT activity.activityid FROM activity, activityworksdi WHERE activity.activitystatus<>" + safeSQL.addVar("Cancelled") + " AND activity.worksdcid=" + safeSQL.addVar(worksdcid) + " AND activity.activityid=activityworksdi.activityid  AND activityworksdi.worksdcid=" + safeSQL.addVar(worksdcid) + " AND activityworksdi.workkeyid1=" + safeSQL.addVar(workkeyid1);
        if (workkeyid2.length() > 0) {
            sql = sql + " AND activityworksdi.workkeyid2=" + safeSQL.addVar(workkeyid2);
        }
        if (workkeyid3.length() > 0) {
            sql = sql + " AND activityworksdi.workkeyid3=" + safeSQL.addVar(workkeyid3);
        }
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        return ds.getColumnValues("activityid", ";");
    }

    public void setContextSDI(Activity createActivity, String worksdcid, String workkeyid1, String workkeyid2, String workkeyid3, PropertyList wapPolicy) throws SapphireException {
        ActivityClassHandler activityClassHandler = ActivityClassHandler.getInstance(this.getConnectionid(), this.getRakFile(), wapPolicy, createActivity.getActivityClass(), worksdcid);
        String[] contextsdi = activityClassHandler.getContextSDI(workkeyid1);
        if (contextsdi != null) {
            createActivity.setActivityContextSdcid(contextsdi[0]);
            createActivity.setActivityContextKeyid1(contextsdi[1]);
            createActivity.setActivityContextKeyid2(contextsdi[2]);
            createActivity.setActivityContextKeyid3(contextsdi[3]);
        }
    }

    public PropertyList getWapPolicy() {
        if (this.wapPolicy == null) {
            try {
                this.wapPolicy = this.getConfigurationProcessor().getPolicy("WAPPolicy", "Sapphire Custom");
            }
            catch (SapphireException e) {
                this.logger.error("Failed to obtain WAP Policy. Continuing but problems expected.", e);
                return new PropertyList();
            }
        }
        return this.wapPolicy;
    }

    public void removeWorkSDI(String activityid, String worksdcid, String workkeyid1, String workkeyid2, String workkeyid3) throws ActionException {
        DataSet temp = new DataSet();
        temp.addColumnValues("keyid1", 0, activityid, ";");
        temp.addColumnValues("worksdcid", 0, worksdcid, ";");
        temp.addColumnValues("workkeyid1", 0, workkeyid1, ";");
        temp.addColumnValues("workkeyid2", 0, workkeyid2, ";", "__null");
        temp.addColumnValues("workkeyid3", 0, workkeyid3, ";", "__null");
        temp.padColumns();
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty("sdcid", "LV_Activity");
        actionProps.setProperty("linkid", "Activity Work List");
        actionProps.setProperty("keyid1", temp.getColumnValues("keyid1", ";"));
        actionProps.setProperty("worksdcid", temp.getColumnValues("worksdcid", ";"));
        actionProps.setProperty("workkeyid1", temp.getColumnValues("workkeyid1", ";"));
        actionProps.setProperty("workkeyid2", temp.getColumnValues("workkeyid2", ";"));
        actionProps.setProperty("workkeyid3", temp.getColumnValues("workkeyid3", ";"));
        this.getActionProcessor().processAction("DeleteSDIDetail", "1", actionProps);
    }
}

