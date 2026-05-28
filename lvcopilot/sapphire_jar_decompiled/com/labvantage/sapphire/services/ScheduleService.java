/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joda.time.DateTime
 *  org.joda.time.DateTimeZone
 *  org.joda.time.LocalDate
 *  org.joda.time.ReadableInstant
 */
package com.labvantage.sapphire.services;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.ColumnUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.RSet;
import com.labvantage.sapphire.RequestParser;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.gwt.shared.util.SDIRequest;
import com.labvantage.sapphire.pageelements.search.SearchUtil;
import com.labvantage.sapphire.scheduler.BaseScheduleTask;
import com.labvantage.sapphire.scheduler.ScheduleEvent;
import com.labvantage.sapphire.scheduler.ScheduleExclude;
import com.labvantage.sapphire.scheduler.ScheduleRule;
import com.labvantage.sapphire.services.AutomationService;
import com.labvantage.sapphire.services.BaseService;
import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.services.DataAccessService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.stability.PlanItem;
import com.labvantage.sapphire.stability.ScheduleGrid;
import com.labvantage.sapphire.util.BooleanHolder;
import com.labvantage.sapphire.util.StringHolder;
import com.labvantage.sapphire.util.cache.CacheNames;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.format.DateFormatter;
import com.labvantage.sapphire.util.graceperiod.GRCPeriodUtil;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import java.io.File;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.TimeZone;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.ReadableInstant;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.error.ErrorHandler;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.M18NUtil;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ScheduleService
extends BaseService
implements CacheNames {
    public static final String LOGNAME = "ScheduleService";
    public static final String RANDOMIZETYPE_NONE = "None";
    public static final String RANDOMIZETYPE_RANDOM = "Random";
    public static final String RANDOMIZETYPE_ROLLING = "Rolling Random";
    public static final String RANDOMIZEMODE_SOURCE = "Source";
    public static final String RANDOMIZEMODE_TEMPLATE = "Template";
    public static final String DEFAULT_RANDOMIZEMODE = "Source";
    private static HashMap PLAN_EXCLUDES;
    private File rakFile = null;

    public ScheduleService(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
        this.logName = LOGNAME;
        PLAN_EXCLUDES = new HashMap();
    }

    public ScheduleService(SapphireConnection sapphireConnection, File rakFile) {
        super(sapphireConnection);
        this.logName = LOGNAME;
        this.rakFile = rakFile;
        PLAN_EXCLUDES = new HashMap();
    }

    public static void updateNextScheduleDt(Calendar refDt, Calendar nextScheduleDt, Calendar toDt, ScheduleRule scheduleRule, int scheduleahead, String scheduleAheadUnits) {
        if (scheduleahead == 0) {
            nextScheduleDt.setTime(toDt.getTime());
        } else {
            nextScheduleDt.setTime(((Calendar)refDt.clone()).getTime());
            switch (scheduleRule.getRuleType()) {
                case 0: {
                    nextScheduleDt.add(12, scheduleRule.getOrdinal());
                    break;
                }
                case 1: {
                    nextScheduleDt.add(10, scheduleRule.getOrdinal());
                    break;
                }
                case 2: 
                case 8: {
                    nextScheduleDt.add(5, 1);
                    break;
                }
                case 3: 
                case 9: {
                    nextScheduleDt.add(5, 7);
                    break;
                }
                case 4: 
                case 5: 
                case 10: {
                    nextScheduleDt.add(2, 1);
                    break;
                }
                default: {
                    nextScheduleDt.add(5, 1);
                }
            }
            if (nextScheduleDt.compareTo(toDt) >= 0) {
                nextScheduleDt.setTime(((Calendar)refDt.clone()).getTime());
                if ("Hour".equals(scheduleAheadUnits)) {
                    nextScheduleDt.add(10, 1);
                } else {
                    nextScheduleDt.add(5, 1);
                }
            }
        }
    }

    public void scheduleEvents(Calendar refDt) throws ServiceException {
        this.scheduleEvents(refDt, "", Calendar.getInstance());
    }

    public void scheduleEvents(Calendar refDt, String schedulePlanId, Calendar nowCalendar) throws ServiceException {
        this.logDebug("Scheduling events from " + DateFormatter.formatDateTime(refDt));
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            ArrayList<Object> paramsArr = new ArrayList<Object>();
            String sql = "SELECT sp.scheduleplanid, sp.planstatus, sp.scheduleahead, sp.scheduleaheadunits, sp.excludemodetype, sp.executeahead, sp.executeaheadunits, sp.timezone, sp.backfillflag,  spi.scheduleplanitemid, spi.scheduleplanitemdesc, spi.planitemstatus, spi.schedulerule, spi.randomizationmode, spi.randomizationtype, spi.randomizationcount,  spi.linksdcid, spi.linkkeyid1, spi.linkkeyid2, spi.linkkeyid3, spi.startdt, spe.nextscheduledt, spi.stopdt, spe.scheduledtodt, spe.lasteventdt FROM scheduleplan sp left join scheduleplanitem spi on sp.scheduleplanid = spi.scheduleplanid  left outer join scheduleplanitemexec spe on spi.scheduleplanid = spe.scheduleplanid and spi.scheduleplanitemid = spe.scheduleplanitemid WHERE ";
            if (schedulePlanId.isEmpty()) {
                sql = sql + " sp.planstatus = 'A' ";
            } else {
                sql = sql + " sp.scheduleplanid = ? ";
                paramsArr.add(schedulePlanId);
            }
            sql = sql + " AND (spi.calendarflag = 'Y' or spi.calendarflag is null or spi.calendarflag = '') AND spe.nextscheduledt <= ? ORDER BY spi.schedulerule";
            paramsArr.add(new Timestamp(refDt.getTime().getTime()));
            db.createPreparedResultSet(sql, paramsArr.toArray());
            DataSet scheduleData = new DataSet(db.getResultSet());
            boolean enforceFolderStatus = true;
            if (enforceFolderStatus) {
                db.createPreparedResultSet("SELECT spi.scheduleplanid, spi.scheduleplanitemid  FROM scheduleplannode spn, scheduleplanitem spi   left outer join scheduleplanitemexec spe on spi.scheduleplanid = spe.scheduleplanid and spi.scheduleplanitemid = spe.scheduleplanitemid WHERE spn.scheduleplanid = spi.scheduleplanid AND spi.scheduleplannodeid = spn.scheduleplannodeid AND spn.nodestatus = 'X'  AND spe.nextscheduledt <= ? ", new Object[]{new Timestamp(refDt.getTime().getTime())});
                DataSet disabledItem = new DataSet(db.getResultSet());
                int row = disabledItem.getRowCount();
                HashMap<String, String> findmap = new HashMap<String, String>();
                for (int i = 0; i < row; ++i) {
                    findmap.put("scheduleplanid", disabledItem.getString(i, "scheduleplanid"));
                    findmap.put("scheduleplanitemid", disabledItem.getString(i, "scheduleplanitemid"));
                    int temp = scheduleData.findRow(findmap);
                    if (temp < 0) continue;
                    scheduleData.remove(temp);
                }
            }
            if (scheduleData.size() > 0) {
                this.addScheduleEvents(scheduleData, refDt, schedulePlanId.isEmpty(), nowCalendar);
            } else {
                this.logDebug("No plans require scheduling");
            }
        }
        catch (SapphireException e) {
            throw new ServiceException("SCHEDULE_SERVICE_FAILED", "Failed to schedule events from " + DateFormatter.formatDateTime(refDt), e);
        }
        finally {
            db.reset();
        }
    }

    public DataSet getScheduleEvents(PropertyList properties) throws ServiceException {
        DataSet events;
        block146: {
            String[] planIds;
            String scheduleplanid = properties.getProperty("scheduleplanid");
            String scheduleplanitemid = properties.getProperty("schduleplanitemid");
            String selectednodeid = properties.getProperty("selectednodeid");
            String status = properties.getProperty("status");
            String linkSdc = properties.getProperty("linksdcid");
            String linkKeyid1 = properties.getProperty("linkkeyid1");
            String linkKeyid2 = properties.getProperty("linkkeyid2");
            String linkKeyid3 = properties.getProperty("linkkeyid3");
            String tasktype = properties.getProperty("tasktype");
            String fromDateStr = properties.getProperty("fromdate");
            String toDateStr = properties.getProperty("todate");
            boolean stability = properties.getProperty("stability").equals("Y");
            PropertyList pageProps = properties.getPropertyListNotNull("pageprops");
            boolean isJUnit = properties.getProperty("junit", "N").equals("Y");
            String activeOnly = properties.getProperty("activeonly", "");
            HashMap<String, ScheduleExclude> planExcludes = new HashMap<String, ScheduleExclude>();
            boolean isSummaryMode = false;
            boolean singleStudyMode = false;
            properties.setProperty("summarymode", "N");
            String calendarMode = properties.getProperty("calendarmode");
            boolean userTimeZone = properties.getProperty("dateinusertimezone", "").startsWith("Y");
            boolean sectionsOnly = properties.getProperty("sectionsonly", "").startsWith("Y");
            HashMap<String, PropertyList> extraSDCPropsMap = new HashMap<String, PropertyList>();
            if (scheduleplanid.equals("null")) {
                scheduleplanid = "";
            }
            if (scheduleplanitemid.equals("null")) {
                scheduleplanitemid = "";
            }
            boolean multiPlanItemMode = false;
            String[] planItemsIds = StringUtil.split(scheduleplanitemid, ";");
            if (planItemsIds.length == (planIds = StringUtil.split(scheduleplanid, ";")).length && planItemsIds.length > 1) {
                multiPlanItemMode = true;
            }
            if (selectednodeid.equals("null")) {
                selectednodeid = "";
            }
            M18NUtil m18n = new M18NUtil(this.connectionInfo);
            Calendar toDate = null;
            Calendar fromDate = null;
            boolean showDisabled = properties.getProperty("showdisabled", "").startsWith("Y");
            boolean onlyExisting = properties.getProperty("onlyexisting", "").startsWith("Y");
            boolean childNodes = properties.getProperty("childnodes", "").startsWith("Y");
            boolean errorMode = properties.getProperty("errormode", "N").startsWith("Y");
            if (scheduleplanid.isEmpty() || selectednodeid.isEmpty()) {
                childNodes = false;
            }
            DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
            events = new DataSet();
            events.addColumn("eventid", 1);
            events.addColumn("scheduleplanid", 0);
            events.addColumn("scheduleplanitemid", 0);
            events.addColumn("eventdt", 2);
            events.addColumn("originaleventdt", 2);
            events.addColumn("executedt", 2);
            events.addColumn("eventdtstr", 0);
            events.addColumn("originaleventdtstr", 0);
            events.addColumn("executedtstr", 0);
            events.addColumn("eventstatus", 0);
            events.addColumn("adhocflag", 0);
            events.addColumn("movedflag", 0);
            events.addColumn("eventnum", 1);
            events.addColumn("linksdc", 0);
            events.addColumn("linkkeyid1", 0);
            events.addColumn("linkkeyid2", 0);
            events.addColumn("linkkeyid3", 0);
            events.addColumn("timezone", 0);
            events.addColumn("randomizationtype", 0);
            events.addColumn("randomizationmode", 0);
            events.addColumn("randomizationcount", 1);
            events.addColumn("scheduleplanitemdesc", 0);
            events.addColumn("source", 0);
            events.addColumn("tasknode", 0);
            events.addColumn("schedulerule", 0);
            events.addColumn("scheduletemplatesdcid", 0);
            events.addColumn("scheduletemplatekeyid1", 0);
            events.addColumn("scheduletemplatekeyid2", 0);
            events.addColumn("scheduletemplatekeyid3", 0);
            events.addColumn("propertytreeid", 0);
            events.addColumn("conditionlabel", 0);
            events.addColumn("scheduletimerule", 0);
            events.addColumn("studyid", 0);
            events.addColumn("studydesc", 0);
            events.addColumn("schedulerule", 0);
            events.addColumn("stabilitydepartmentid", 0);
            events.addColumn("qtypull", 0);
            events.addColumn("qtypulltype", 0);
            events.addColumn("qtypullunits", 0);
            events.addColumn("windowstartdt", 2);
            events.addColumn("windowstartdtstr", 0);
            events.addColumn("windowenddt", 2);
            events.addColumn("windowenddtstr", 0);
            RSet rsetPlanItem = null;
            RSet rsetPlan = null;
            DataAccessService dataAccessService = new DataAccessService(this.sapphireConnection);
            SafeSQL safeSQL = new SafeSQL();
            try {
                int schedulePoll;
                int i;
                boolean enforceFolderStatus;
                db.setConnection(this.sapphireConnection);
                boolean isOracle = this.sapphireConnection.isOracle();
                if (toDate != null && calendarMode != null && !calendarMode.startsWith("Y")) {
                    Calendar includetodate = Calendar.getInstance();
                    includetodate.setTime(toDate.getTime());
                    includetodate.add(5, 1);
                    toDate = includetodate;
                }
                ArrayList<Object> params = new ArrayList<Object>();
                String whereclause = "";
                String folderwhereclause = "";
                SDIRequest sdiRequest = new SDIRequest();
                whereclause = showDisabled || stability ? whereclause + "" : whereclause + " AND sp.planstatus = 'A' AND scheduleplanitem.planitemstatus = 'A'";
                if (stability) {
                    String securityWhereClause;
                    boolean isAllStudy;
                    String tempstudyid;
                    String studyid = properties.getProperty("studyid", "");
                    String studyid1 = properties.getProperty("studyid1", "");
                    String studyid2 = properties.getProperty("studyid2", "");
                    String studyid3 = properties.getProperty("studyid3", "");
                    String studyid4 = properties.getProperty("studyid4", "");
                    if (studyid == null || studyid.length() == 0) {
                        studyid = "All Studies";
                    }
                    if (activeOnly == null || activeOnly.length() == 0) {
                        activeOnly = "";
                    }
                    String restrictive_where = pageProps.getPropertyListNotNull("stabilityprops").getProperty("restrictivewhere");
                    boolean deptSecurityEnabled = OpalUtil.isDeptSecurityEnabled(this.getSDCProcessor(), "StudySDC");
                    String tempStudySQL = "";
                    if (studyid1 != null && studyid1.length() > 0) {
                        studyid = studyid1 = studyid1.replaceAll("%3B", ";");
                        tempstudyid = OpalUtil.getSqlWhereClause(studyid);
                        tempStudySQL = " study.studyid in (" + tempstudyid + ")";
                    } else if (studyid2 != null && studyid2.length() > 0) {
                        studyid = studyid2 = studyid2.replaceAll("%3B", ";");
                        tempstudyid = OpalUtil.getSqlWhereClause(studyid);
                        tempStudySQL = " ( study.stabilitydepartmentid in (" + tempstudyid + ") ";
                        if (deptSecurityEnabled) {
                            tempStudySQL = tempStudySQL + " OR study.securitydepartment in (" + tempstudyid + ")";
                        }
                        tempStudySQL = tempStudySQL + ")";
                    } else if (studyid3 != null && studyid3.length() > 0) {
                        studyid = studyid3 = studyid3.replaceAll("%3B", ";");
                        tempstudyid = OpalUtil.getSqlWhereClause(studyid);
                        tempStudySQL = " ( study.ownersysuserid in (" + tempstudyid + ") ";
                        if (deptSecurityEnabled) {
                            tempStudySQL = tempStudySQL + " OR study.securityuser in (" + tempstudyid + ")";
                        }
                        tempStudySQL = tempStudySQL + ")";
                    } else if (studyid4 != null && studyid4.length() > 0) {
                        studyid = studyid4 = studyid4.replaceAll("%3B", ";");
                        tempstudyid = OpalUtil.getSqlWhereClause(studyid);
                        tempStudySQL = " study.studysuiteid in (" + tempstudyid + ")";
                    }
                    if (activeOnly.equals("on")) {
                        if (tempStudySQL.length() > 0) {
                            tempStudySQL = tempStudySQL + " AND ";
                        }
                        tempStudySQL = tempStudySQL + " study.studystatus = 'A'";
                    }
                    if (!(isAllStudy = "All Studies".equals(studyid)) && !studyid.contains(";")) {
                        singleStudyMode = true;
                    }
                    if (errorMode && !fromDateStr.isEmpty()) {
                        Calendar stabilityFromDate = m18n.parseCalendar(fromDateStr);
                        Calendar stabilityToDate = m18n.parseCalendar(toDateStr);
                        if (tempStudySQL.length() > 0) {
                            tempStudySQL = tempStudySQL + " AND ";
                        }
                        tempStudySQL = tempStudySQL + " se.eventdt >= " + safeSQL.addVar(new Timestamp(stabilityFromDate.getTime().getTime())) + " AND se.eventdt <= " + safeSQL.addVar(new Timestamp(stabilityToDate.getTime().getTime())) + " ";
                    }
                    if (status != null && status.length() > 0) {
                        if (tempStudySQL.length() > 0) {
                            tempStudySQL = tempStudySQL + " AND ";
                        }
                        tempStudySQL = tempStudySQL + " se.eventstatus= " + safeSQL.addVar(status);
                    }
                    ConnectionProcessor connectionProcessor = this.getConnectionProcessor();
                    String currentUser = connectionProcessor.getSapphireConnection().getSysuserId();
                    String defaultDepartment = connectionProcessor.getSapphireConnection().getDefaultDepartment();
                    if (restrictive_where != null && restrictive_where.trim().length() > 0) {
                        if (tempStudySQL.length() > 0) {
                            tempStudySQL = tempStudySQL + " AND ";
                        }
                        restrictive_where = StringUtil.replaceAll(restrictive_where, "[currentuser]", currentUser, false);
                        restrictive_where = StringUtil.replaceAll(restrictive_where, "[defaultdepartment]", defaultDepartment, false);
                        tempStudySQL = tempStudySQL + " (" + restrictive_where + ")";
                    }
                    whereclause = whereclause + " " + (tempStudySQL.length() > 0 ? " AND " + tempStudySQL : "");
                    if (deptSecurityEnabled && (securityWhereClause = this.getQueryProcessor().getSecurityFilterWhere("StudySDC")) != null && securityWhereClause.length() > 0) {
                        whereclause = whereclause + " AND ";
                        whereclause = whereclause + securityWhereClause;
                    }
                }
                if (!multiPlanItemMode) {
                    if (scheduleplanid != null && scheduleplanid.length() > 0) {
                        whereclause = whereclause + " and sp.scheduleplanid='" + scheduleplanid + "'";
                        folderwhereclause = folderwhereclause + whereclause;
                    }
                    if (linkSdc != null && linkSdc.length() > 0) {
                        whereclause = whereclause + " and scheduleplanitem.linksdcid='" + linkSdc + "'";
                    }
                    if (linkKeyid1 != null && linkKeyid1.length() > 0) {
                        whereclause = whereclause + " and scheduleplanitem.linkkeyid1 in " + SearchUtil.toQueryInClause(linkKeyid1);
                    }
                    if (linkKeyid2 != null && linkKeyid2.length() > 0) {
                        whereclause = whereclause + " and scheduleplanitem.linkkeyid2='" + linkKeyid2 + "'";
                    }
                    if (linkKeyid3 != null && linkKeyid3.length() > 0) {
                        whereclause = whereclause + " and scheduleplanitem.linkkeyid3='" + linkKeyid3 + "'";
                    }
                    if (scheduleplanitemid != null && scheduleplanitemid.length() > 0) {
                        whereclause = whereclause + " and scheduleplanitem.scheduleplanitemid in " + SearchUtil.toQueryInClause(scheduleplanitemid);
                    }
                }
                if (selectednodeid != null && selectednodeid.length() > 0) {
                    if (childNodes) {
                        String newNodes = selectednodeid;
                        StringBuilder newNodeBuilder = new StringBuilder();
                        newNodeBuilder.append(selectednodeid);
                        this.getChildNodes(newNodeBuilder, db, newNodes);
                        selectednodeid = newNodeBuilder.toString();
                        whereclause = whereclause + " and scheduleplanitem.scheduleplannodeid in " + SearchUtil.toQueryInClause(selectednodeid);
                    } else {
                        whereclause = selectednodeid.equals("root") ? whereclause + (isOracle ? " and scheduleplanitem.scheduleplannodeid is null" : " and ( scheduleplanitem.scheduleplannodeid is null or scheduleplanitem.scheduleplannodeid='') ") : (!selectednodeid.equals("root") && selectednodeid.indexOf("root") >= 0 ? whereclause + " and scheduleplanitem.scheduleplannodeid in " + SearchUtil.toQueryInClause(selectednodeid) + "or scheduleplanitem.scheduleplannodeid is null" : whereclause + " and scheduleplanitem.scheduleplannodeid in " + SearchUtil.toQueryInClause(selectednodeid));
                    }
                }
                if (tasktype != null && !tasktype.isEmpty()) {
                    whereclause = whereclause + " and scheduleplanitem.propertytreeid = '" + tasktype + "' ";
                }
                if (errorMode) {
                    whereclause = whereclause + " and exists ( select 1 from scheduleevent se2 where se2.scheduleplanid = scheduleplanitem.scheduleplanid and se2.scheduleplanitemid = scheduleplanitem.scheduleplanitemid and se2.eventstatus = 'E' )";
                }
                String securityWhereFragment = "";
                if (stability) {
                    String sql = "SELECT sp.scheduleplanid, sp.planstatus, sp.scheduleahead, sp.scheduleaheadunits, sp.executeahead, sp.executeaheadunits, sp.backfillflag, sp.excludemodetype, sp.timezone, scheduleplanitem.scheduletemplatesdcid, scheduleplanitem.scheduletemplatekeyid1, scheduleplanitem.scheduletemplatekeyid2, scheduleplanitem.scheduletemplatekeyid3,  scheduleplanitem.scheduleplanitemid, scheduleplanitem.scheduleplanitemdesc, scheduleplanitem.propertytreeid, scheduleplanitem.scheduletasknodeid, scheduleplanitem.planitemstatus, scheduleplanitem.schedulerule,  scheduleplanitem.linksdcid, scheduleplanitem.linkkeyid1, scheduleplanitem.linkkeyid2, scheduleplanitem.linkkeyid3, scheduleplanitem.startdt, spe.nextscheduledt, scheduleplanitem.stopdt, spe.scheduledtodt, scheduleplanitem.scheduleplanitemdesc, spe.lasteventdt  , study.studyid, study.studydesc, scond.conditionlabel, str.schedulerule scheduletimerule, study.stabilitydepartmentid, scond.qtypull, scond.qtypulltype, scond.qtypullunits,  scheduleplanitem.randomizationtype, scheduleplanitem.randomizationmode, scheduleplanitem.randomizationcount, scheduleplanitem.scheduletemplatesdcid, scheduleplanitem.scheduletemplatekeyid1, scheduleplanitem.scheduletemplatekeyid2, scheduleplanitem.scheduletemplatekeyid3,  se.eventdt, se.eventstatus, se.eventnum, se.executedt, se.originaleventdt, se.adhocflag  FROM scheduleplan sp  inner join scheduleplanitem scheduleplanitem on sp.scheduleplanid = scheduleplanitem.scheduleplanid   left outer join scheduleplanitemexec spe on scheduleplanitem.scheduleplanid = spe.scheduleplanid and scheduleplanitem.scheduleplanitemid = spe.scheduleplanitemid    inner join scheduleevent se on se.scheduleplanid = scheduleplanitem.scheduleplanid and se.scheduleplanitemid = scheduleplanitem.scheduleplanitemid    inner join study_scheduleplan ssp on sp.scheduleplanid = ssp.scheduleplanid   inner join study on ssp.studyid = study.studyid   inner join schedulecondition scond on  scond.scheduleplanid = sp.scheduleplanid   AND scond.scheduleconditionid = scheduleplanitem.scheduleconditionid   inner join scheduletimerule str on str.scheduleplanid = sp.scheduleplanid  AND scheduleplanitem.scheduletimeruleid = str.scheduletimeruleid  WHERE  1=1 " + whereclause + " ORDER BY se.eventdt";
                    db.createPreparedResultSet(sql, safeSQL.getValues());
                } else if (!multiPlanItemMode) {
                    PropertyList dashboardSecurityWhereClauseProps = new PropertyList();
                    dashboardSecurityWhereClauseProps.setProperty("sdcid", "SchedulePlanItem");
                    try {
                        this.getActionProcessor().processActionClass("com.labvantage.sapphire.modules.dashboard.util.DashboardSecurityWhereClause", dashboardSecurityWhereClauseProps);
                    }
                    catch (ActionException e) {
                        throw new IllegalArgumentException("Cannot get security where clause for SchedulePlanItem");
                    }
                    securityWhereFragment = dashboardSecurityWhereClauseProps.getProperty("whereclause");
                    if (!securityWhereFragment.isEmpty()) {
                        securityWhereFragment = " AND ( " + securityWhereFragment + " )";
                    }
                    db.createResultSet("SELECT sp.scheduleplanid, sp.planstatus, sp.scheduleahead, sp.scheduleaheadunits, sp.executeahead, sp.executeaheadunits, sp.backfillflag, sp.excludemodetype, sp.timezone, scheduleplanitem.randomizationmode, scheduleplanitem.randomizationtype, scheduleplanitem.randomizationcount, scheduleplanitem.scheduletemplatesdcid, scheduleplanitem.scheduletemplatekeyid1, scheduleplanitem.scheduletemplatekeyid2, scheduleplanitem.scheduletemplatekeyid3,  scheduleplanitem.scheduleplanitemid, scheduleplanitem.scheduleplanitemdesc, scheduleplanitem.propertytreeid, scheduleplanitem.scheduletasknodeid, scheduleplanitem.planitemstatus, scheduleplanitem.schedulerule,  scheduleplanitem.linksdcid, scheduleplanitem.linkkeyid1, scheduleplanitem.linkkeyid2, scheduleplanitem.linkkeyid3, scheduleplanitem.startdt, spe.nextscheduledt, scheduleplanitem.stopdt, spe.scheduledtodt, scheduleplanitem.scheduleplanitemdesc, spe.lasteventdt FROM scheduleplan sp, scheduleplanitem scheduleplanitem  left outer join scheduleplanitemexec spe on scheduleplanitem.scheduleplanid = spe.scheduleplanid and scheduleplanitem.scheduleplanitemid = spe.scheduleplanitemid WHERE sp.scheduleplanid = scheduleplanitem.scheduleplanid AND (scheduleplanitem.calendarflag = 'Y' or scheduleplanitem.calendarflag is null or scheduleplanitem.calendarflag = '')" + whereclause + " " + securityWhereFragment + " ORDER BY scheduleplanitem.schedulerule");
                } else if (multiPlanItemMode) {
                    rsetPlan = dataAccessService.createRSet("SchedulePlan", scheduleplanid, null, null);
                    rsetPlanItem = dataAccessService.createRSet("SchedulePlanItem", scheduleplanid, scheduleplanitemid, null);
                    params.add(rsetPlanItem.getRsetid());
                    params.add(rsetPlan.getRsetid());
                    db.createPreparedResultSet("SELECT sp.scheduleplanid, sp.planstatus, sp.scheduleahead, sp.scheduleaheadunits, sp.executeahead, sp.executeaheadunits, sp.backfillflag, sp.excludemodetype, sp.timezone, scheduleplanitem.scheduletemplatesdcid, scheduleplanitem.scheduletemplatekeyid1, scheduleplanitem.scheduletemplatekeyid2, scheduleplanitem.scheduletemplatekeyid3,  scheduleplanitem.scheduleplanitemid, scheduleplanitem.scheduleplanitemdesc, scheduleplanitem.propertytreeid, scheduleplanitem.scheduletasknodeid, scheduleplanitem.planitemstatus, scheduleplanitem.schedulerule,  scheduleplanitem.linksdcid, scheduleplanitem.linkkeyid1, scheduleplanitem.linkkeyid2, scheduleplanitem.linkkeyid3, scheduleplanitem.startdt, spe.nextscheduledt, scheduleplanitem.stopdt, spe.scheduledtodt, scheduleplanitem.scheduleplanitemdesc, spe.lasteventdt FROM scheduleplan sp left join scheduleplanitem  on  sp.scheduleplanid = scheduleplanitem.scheduleplanid left outer join scheduleplanitemexec spe on scheduleplanitem.scheduleplanid = spe.scheduleplanid and scheduleplanitem.scheduleplanitemid = spe.scheduleplanitemid , rsetitems planitemrset, rsetitems planrset WHERE (scheduleplanitem.calendarflag = 'Y' or scheduleplanitem.calendarflag is null or scheduleplanitem.calendarflag = '')" + whereclause + "and  scheduleplanitem.scheduleplanid = planitemrset.keyid1 AND scheduleplanitem.scheduleplanitemid = planitemrset.keyid2 AND scheduleplanitem.scheduleplanid = planrset.keyid1 and planitemrset.rsetid = ? and planrset.rsetid = ? ORDER BY scheduleplanitem.schedulerule", params.toArray());
                }
                DataSet scheduleData = new DataSet(db.getResultSet());
                if (stability && !singleStudyMode) {
                    isSummaryMode = true;
                    properties.setProperty("summarymode", "Y");
                }
                if (showDisabled) {
                    scheduleData.addColumn("folderstatus", 0);
                }
                if ((enforceFolderStatus = true) && !stability) {
                    if (!multiPlanItemMode) {
                        db.createResultSet("SELECT scheduleplanitem.scheduleplanid, scheduleplanitem.scheduleplanitemid  FROM scheduleplan sp, scheduleplannode spn, scheduleplanitem scheduleplanitem WHERE sp.scheduleplanid = spn.scheduleplanid AND spn.scheduleplanid = scheduleplanitem.scheduleplanid AND scheduleplanitem.scheduleplannodeid = spn.scheduleplannodeid AND spn.nodestatus = 'X' " + folderwhereclause + " " + securityWhereFragment);
                    } else {
                        params = new ArrayList();
                        params.add(rsetPlan.getRsetid());
                        db.createPreparedResultSet("SELECT scheduleplanitem.scheduleplanid, scheduleplanitem.scheduleplanitemid  FROM scheduleplan sp, scheduleplannode spn, scheduleplanitem scheduleplanitem, rsetitems WHERE sp.scheduleplanid = spn.scheduleplanid AND spn.scheduleplanid = scheduleplanitem.scheduleplanid AND scheduleplanitem.scheduleplannodeid = spn.scheduleplannodeid AND spn.nodestatus = 'X' and rsetitems.keyid1 = sp.scheduleplanid and rsetitems.rsetid = ? ", params.toArray());
                    }
                    DataSet disabledItem = new DataSet(db.getResultSet());
                    int row = disabledItem.getRowCount();
                    HashMap<String, String> findmap = new HashMap<String, String>();
                    for (i = 0; i < row; ++i) {
                        findmap.put("scheduleplanid", disabledItem.getString(i, "scheduleplanid"));
                        findmap.put("scheduleplanitemid", disabledItem.getString(i, "scheduleplanitemid"));
                        int temp = scheduleData.findRow(findmap);
                        if (temp < 0) continue;
                        if (showDisabled) {
                            scheduleData.setString(temp, "folderstatus", "X");
                            continue;
                        }
                        scheduleData.remove(temp);
                    }
                }
                if (sectionsOnly) {
                    events = scheduleData;
                    break block146;
                }
                try {
                    ConfigurationProcessor cp = new ConfigurationProcessor(this.getConnectionid());
                    schedulePoll = Integer.valueOf(cp.getSysConfigProperty("schedulepoll"));
                }
                catch (Exception e) {
                    schedulePoll = 3600;
                }
                String prevTimezone = "";
                M18NUtil m18NTimezone = null;
                for (i = 0; i < scheduleData.getRowCount(); ++i) {
                    String planId = scheduleData.getString(i, "scheduleplanid");
                    String planItemId = scheduleData.getString(i, "scheduleplanitemid");
                    String planStatus = scheduleData.getString(i, "planstatus", "");
                    Calendar startDt = Calendar.getInstance();
                    String schedulePlanItemDesc = scheduleData.getString(i, "scheduleplanitemdesc");
                    String scheduleExcludeMode = scheduleData.getString(i, "excludemodetype", "S");
                    String planItemStatus = scheduleData.getString(i, "planitemstatus", "");
                    String folderStatus = scheduleData.getString(i, "folderstatus", "");
                    String randomizationType = scheduleData.getString(i, "randomizationtype", "");
                    String randomizationMode = scheduleData.getString(i, "randomizationmode", "");
                    String propertyTreeid = scheduleData.getString(i, "propertytreeid", "");
                    String scheduleTaskNodeId = scheduleData.getString(i, "scheduletasknodeid", "");
                    String scheduleTemplateSdcId = scheduleData.getString(i, "scheduletemplatesdcid", "");
                    String scheduleTemplateKeyId1 = scheduleData.getString(i, "scheduletemplatekeyid1", "");
                    String scheduleTemplateKeyId2 = scheduleData.getString(i, "scheduletemplatekeyid2", "");
                    String scheduleTemplateKeyId3 = scheduleData.getString(i, "scheduletemplatekeyid3", "");
                    String studyidstr = scheduleData.getString(i, "studyid", "");
                    String studydescstr = scheduleData.getString(i, "studydesc", "");
                    String stabilitydepartmentid = scheduleData.getString(i, "stabilitydepartmentid", "");
                    String qtypull = scheduleData.getValue(i, "qtypull", "");
                    String qtypulltype = scheduleData.getString(i, "qtypulltype", "");
                    String qtypullunits = scheduleData.getString(i, "qtypullunits", "");
                    String conditionlabel = scheduleData.getString(i, "conditionlabel", "");
                    String scheduletimerule = scheduleData.getString(i, "scheduletimerule", "");
                    BigDecimal randomizationCount = scheduleData.getBigDecimal(i, "randomizationcount");
                    String defaultTimeZone = TimeZone.getDefault().getID();
                    String timezoneStr = scheduleData.getString(i, "timezone", defaultTimeZone);
                    TimeZone timeZone = TimeZone.getTimeZone(timezoneStr);
                    DateTimeZone dateTimeZone = DateTimeZone.forID((String)timezoneStr);
                    if (!timezoneStr.equals(prevTimezone)) {
                        if (fromDateStr != null && fromDateStr.length() > 0) {
                            fromDate = m18n.parseCalendar(fromDateStr);
                        }
                        if (toDateStr != null && toDateStr.length() > 0) {
                            toDate = m18n.parseCalendar(toDateStr);
                        }
                        if (fromDate == null && !errorMode) {
                            fromDate = Calendar.getInstance();
                        }
                    }
                    DateTime scheduledToDateTime = null;
                    if (scheduleData.getCalendar(i, "scheduledtodt") != null) {
                        scheduledToDateTime = new DateTime((Object)scheduleData.getCalendar(i, "scheduledtodt"), dateTimeZone);
                    }
                    if (!timezoneStr.equals(prevTimezone)) {
                        if (!userTimeZone && fromDate != null) {
                            fromDate.setTimeZone(timeZone);
                            toDate.setTimeZone(timeZone);
                        }
                        if (userTimeZone) {
                            m18NTimezone = m18n;
                        } else {
                            m18NTimezone = new M18NUtil(this.connectionInfo);
                            m18NTimezone.setTimeZone(timeZone);
                        }
                        if (fromDateStr != null && fromDateStr.length() > 0) {
                            fromDate = m18NTimezone.parseCalendar(fromDateStr);
                        }
                        if (toDateStr != null && toDateStr.length() > 0) {
                            toDate = m18NTimezone.parseCalendar(toDateStr);
                        }
                        if (fromDate == null && !errorMode) {
                            fromDate = Calendar.getInstance(timeZone);
                        }
                    }
                    prevTimezone = timezoneStr;
                    ScheduleExclude exclude = null;
                    if (errorMode) {
                        onlyExisting = true;
                    } else {
                        exclude = (ScheduleExclude)planExcludes.get(planId);
                        if (exclude == null) {
                            exclude = this.rakFile == null ? new ScheduleExclude(this.sapphireConnection, this.logger) : new ScheduleExclude(this.sapphireConnection, this.logger, this.rakFile);
                            exclude.setTimeZone(timeZone);
                            exclude.setSchedulePlanId(planId);
                            exclude.setStartdt(fromDate);
                            exclude.setEnddt(toDate);
                            planExcludes.put(planId, exclude);
                        }
                    }
                    DateTime toDateTime = new DateTime((Object)toDate);
                    DateTime startDateTime = null;
                    DateTime lastEventDateTime = null;
                    if (scheduleData.getTimestamp(i, "startdt") != null) {
                        startDt.setTime(scheduleData.getTimestamp(i, "startdt"));
                        startDt.setTimeZone(timeZone);
                        startDt.set(14, 0);
                        startDt.get(5);
                        startDateTime = new DateTime((Object)scheduleData.getCalendar(i, "startdt"), dateTimeZone);
                    }
                    DateTime stopDateTime = scheduleData.getTimestamp(i, "stopdt") != null ? new DateTime((Object)scheduleData.getCalendar(i, "stopdt"), dateTimeZone) : null;
                    Calendar lastEventDt = null;
                    if (scheduleData.getTimestamp(i, "lasteventdt") != null) {
                        lastEventDateTime = new DateTime((Object)scheduleData.getCalendar(i, "lasteventdt"), dateTimeZone);
                        lastEventDt = Calendar.getInstance(timeZone);
                        lastEventDt.setTime(scheduleData.getTimestamp(i, "lasteventdt"));
                        lastEventDt.set(14, 0);
                        lastEventDt.get(5);
                    }
                    DateTime getNewItemsToDateTime = null;
                    DateTime getNewItemsFromDateTime = new DateTime((Object)fromDate, dateTimeZone);
                    if (toDate == null) {
                        onlyExisting = true;
                    } else {
                        getNewItemsToDateTime = toDateTime;
                        if (stopDateTime != null && getNewItemsToDateTime.isAfter((ReadableInstant)stopDateTime)) {
                            getNewItemsToDateTime = stopDateTime;
                        }
                    }
                    if (getNewItemsFromDateTime.isBefore((ReadableInstant)startDateTime)) {
                        getNewItemsFromDateTime = startDateTime;
                    }
                    ScheduleRule scheduleRule = new ScheduleRule();
                    String scheduleruleStr = scheduleData.getString(i, "schedulerule");
                    if (scheduleruleStr != null) {
                        scheduleRule.setRule(scheduleruleStr);
                    }
                    if (!scheduleRule.isValidRule()) continue;
                    boolean getNew = false;
                    boolean getExisting = true;
                    if (onlyExisting) {
                        getNew = false;
                    } else if (scheduledToDateTime == null) {
                        getNew = true;
                        if (startDt == null) {
                            getNew = false;
                        } else {
                            scheduledToDateTime = startDateTime;
                        }
                    } else if (toDate != null && toDate.after(scheduledToDateTime.toGregorianCalendar())) {
                        getNew = true;
                        getNewItemsFromDateTime = scheduledToDateTime;
                    } else if (fromDate.after(scheduledToDateTime.toGregorianCalendar())) {
                        getNew = true;
                        getExisting = false;
                        getNewItemsFromDateTime = new DateTime((Object)fromDate);
                    }
                    if (getNew && (scheduleRule.getRuleType() == 0 || scheduleRule.getRuleType() == 1 || (scheduleRule.getRuleType() == 3 || scheduleRule.getRuleType() == 2) && scheduleRule.getOrdinal() > 1)) {
                        if (lastEventDt != null) {
                            if (getNewItemsFromDateTime.isAfter((ReadableInstant)lastEventDateTime)) {
                                getNewItemsFromDateTime = lastEventDateTime;
                            }
                        } else {
                            getNewItemsFromDateTime = startDateTime;
                        }
                    }
                    if (scheduleExcludeMode.equals("N") && !getExisting) {
                        getNewItemsFromDateTime.plusDays(-7);
                    }
                    if (scheduleExcludeMode.equals("P")) {
                        getNewItemsFromDateTime.plusDays(7);
                    }
                    DataSet dbEvents = new DataSet();
                    if (getExisting) {
                        whereclause = " WHERE sp.scheduleplanid = ? and spi.scheduleplanitemid = ?";
                        params = new ArrayList();
                        params.add(0, planId);
                        params.add(1, planItemId);
                        if (status != null && status.length() > 0) {
                            whereclause = whereclause + " and  se.eventstatus=? ";
                            params.add(status);
                        }
                        if (fromDate != null) {
                            whereclause = whereclause + " and se.eventdt >= ? ";
                            params.add(new Timestamp(fromDate.getTimeInMillis()));
                        }
                        if (toDate != null) {
                            whereclause = whereclause + " and se.eventdt < ? ";
                            params.add(new Timestamp(toDate.getTimeInMillis()));
                        }
                        String eventPart = "se.scheduleplanid, se.scheduleplanitemid, se.eventdt, se.eventstatus, se.eventnum, se.executedt, se.originaleventdt, se.adhocflag, spi.scheduleplanitemdesc, spi.schedulerule, spi.linksdcid, spi.linkkeyid1, spi.linkkeyid2, spi.linkkeyid3, spi.propertytreeid, spi.scheduletasknodeid, sp.timezone, spi.randomizationtype, spi.randomizationmode, spi.randomizationcount, spi.scheduletemplatesdcid, spi.scheduletemplatekeyid1, spi.scheduletemplatekeyid2, spi.scheduletemplatekeyid3 ";
                        String queryfrom = " scheduleplan sp left join scheduleevent se on sp.scheduleplanid = se.scheduleplanid left join scheduleplanitem spi on se.scheduleplanid = spi.scheduleplanid and se.scheduleplanitemid = spi.scheduleplanitemid ";
                        String itemPart = " ";
                        String itemJoin = " ";
                        String extraSdc = "";
                        String schedulePlanIdCol = "";
                        String schedulePlanItemIdCol = "";
                        String eventDateCol = "";
                        String eventNumCol = "";
                        String linkUrl = "";
                        String orderBy = " order by se.scheduleplanid, se.scheduleplanitemid, se.eventnum, se.eventdt";
                        boolean useJoin = false;
                        PropertyListCollection collection = pageProps.getPropertyListNotNull("taskeventprops").getCollectionNotNull("taskevents");
                        for (int k = 0; k < collection.size(); ++k) {
                            String taskId;
                            PropertyList taskProps = collection.getPropertyList(k);
                            if (taskProps == null || !propertyTreeid.equals(taskId = taskProps.getProperty("taskid"))) continue;
                            schedulePlanIdCol = taskProps.getProperty("scheduleplanidcol");
                            schedulePlanItemIdCol = taskProps.getProperty("scheduleplanitemidcol");
                            eventDateCol = taskProps.getProperty("eventdatecol");
                            eventNumCol = taskProps.getProperty("eventnumcol");
                            extraSdc = taskProps.getProperty("sdcid");
                            linkUrl = taskProps.getProperty("linkurl");
                            break;
                        }
                        if (!extraSdc.isEmpty() && !isSummaryMode) {
                            PropertyList extraSDCProps = (PropertyList)extraSDCPropsMap.get(extraSdc);
                            if (extraSDCProps == null) {
                                extraSDCProps = this.getSDCProcessor().getPropertyList(extraSdc);
                                extraSDCPropsMap.put(extraSdc, extraSDCProps);
                            }
                            String tableid = extraSDCProps.getProperty("tableid");
                            String keyid1 = extraSDCProps.getProperty("keycolid1");
                            String keyid2 = extraSDCProps.getProperty("keycolid2");
                            String keyid3 = extraSDCProps.getProperty("keycolid3");
                            orderBy = orderBy + ", " + tableid + "." + keyid1;
                            if (!keyid2.isEmpty()) {
                                orderBy = orderBy + ", " + tableid + "." + keyid2;
                            }
                            if (!keyid3.isEmpty()) {
                                orderBy = orderBy + ", " + tableid + "." + keyid3;
                            }
                            PropertyListCollection columns = pageProps.getPropertyListNotNull("extracolprops").getCollectionNotNull("columns");
                            for (int k = 0; k < columns.size(); ++k) {
                                PropertyList columnProps = columns.getPropertyList(k);
                                String column = columnProps.getProperty("columnid");
                                if (column.isEmpty()) continue;
                                itemPart = column.startsWith("(") ? itemPart + ", " + column : itemPart + " , " + tableid + "." + column + " ";
                            }
                            if (!(itemPart.isEmpty() || eventDateCol.isEmpty() || !stability && eventNumCol.isEmpty() || schedulePlanIdCol.isEmpty() || schedulePlanItemIdCol.isEmpty())) {
                                useJoin = true;
                                StringBuilder querywhere = new StringBuilder();
                                querywhere.append(" LEFT JOIN ").append(tableid).append(" ON se.scheduleplanid = ").append(tableid).append(".").append(schedulePlanIdCol).append(" AND");
                                querywhere.append(" se.scheduleplanitemid = ").append(tableid).append(".").append(schedulePlanItemIdCol).append(" AND ");
                                querywhere.append(" se.eventdt = ").append(tableid).append(".").append(eventDateCol).append(" ");
                                if (!stability) {
                                    querywhere.append(" AND ").append(" se.eventnum = ").append(tableid).append(".").append(eventNumCol).append(" ");
                                }
                                itemJoin = querywhere.toString();
                            }
                        }
                        String sql = "select " + eventPart + " " + itemPart + " from " + queryfrom + itemJoin + whereclause + orderBy;
                        if (stability && isSummaryMode) {
                            dbEvents = scheduleData;
                        } else {
                            db.createPreparedResultSet(sql, params.toArray());
                            dbEvents = new DataSet(db.getResultSet());
                        }
                        Calendar previousEventDt = null;
                        String previousEventNum = "";
                        int row = 0;
                        for (int k = 0; k < (stability && isSummaryMode ? 1 : dbEvents.getRowCount()); ++k) {
                            if (stability && isSummaryMode) {
                                k = i;
                            }
                            Calendar eventDt = dbEvents.getCalendar(k, "eventdt");
                            String eventNum = dbEvents.getValue(k, "eventnum");
                            boolean uniqueRow = true;
                            if (useJoin && previousEventDt != null && eventDt.compareTo(previousEventDt) == 0 && eventNum.equals(previousEventNum)) {
                                uniqueRow = false;
                                PropertyListCollection columns = pageProps.getPropertyListNotNull("extracolprops").getCollectionNotNull("columns");
                                for (int l = 0; l < columns.size(); ++l) {
                                    PropertyList columnProps = columns.getPropertyList(l);
                                    String column = RequestParser.parseAlias(columnProps.getProperty("columnid"));
                                    String displayvalue = columnProps.getProperty("displayvalue");
                                    String columnValue = dbEvents.getValue(k, column, "");
                                    if (OpalUtil.isNotEmpty(displayvalue)) {
                                        columnValue = ColumnUtil.parseDisplayValue(columnValue, displayvalue);
                                    }
                                    String oldValue = events.getValue(row, column, "");
                                    events.setValue(row, column, oldValue + ";" + columnValue);
                                }
                            }
                            if (uniqueRow) {
                                String timeZoneStr;
                                if (stability && isSummaryMode) {
                                    events.copyRow(scheduleData, i, 1);
                                } else {
                                    events.copyRow(dbEvents, k, 1);
                                }
                                row = events.size() - 1;
                                events.setString(row, "executedtstr", m18NTimezone.format(events.getCalendar(row, "executedt")));
                                events.setString(row, "eventdtstr", m18NTimezone.format(events.getCalendar(row, "eventdt")));
                                Calendar originalEventDt = events.getCalendar(row, "originaleventdt");
                                events.setString(row, "originaleventdtstr", m18NTimezone.format(originalEventDt, true));
                                if (originalEventDt != null) {
                                    events.setString(row, "movedflag", eventDt.compareTo(originalEventDt) != 0 ? "Y" : "");
                                }
                                if ((timeZoneStr = events.getString(row, "timezone")) == null) {
                                    timeZoneStr = TimeZone.getDefault().getID();
                                }
                                events.setString(row, "timezone", timeZoneStr);
                                String adhocFlag = dbEvents.getString(row, "adhocflag", "").isEmpty() ? "0" : "1";
                                events.setNumber(row, "eventid", new BigDecimal(Integer.toString(i) + Long.toString(events.getCalendar(row, "originaleventdt").getTimeInMillis()) + Integer.toString(events.getInt(row, "eventnum"))) + adhocFlag);
                                String linksdcid = events.getString(row, "linksdcid");
                                events.setString(row, "source", isJUnit ? linksdcid : this.getTranslationProcessor().translate(linksdcid) + ": " + events.getString(row, "linkkeyid1"));
                                PropertyListCollection columns = pageProps.getPropertyListNotNull("extracolprops").getCollectionNotNull("columns");
                                for (int l = 0; l < columns.size(); ++l) {
                                    PropertyList columnProps = columns.getPropertyList(l);
                                    String column = RequestParser.parseAlias(columnProps.getProperty("columnid"));
                                    String displayvalue = columnProps.getProperty("displayvalue");
                                    String columnValue = events.getValue(row, column, "");
                                    if (OpalUtil.isNotEmpty(displayvalue)) {
                                        columnValue = ColumnUtil.parseDisplayValue(columnValue, displayvalue);
                                    }
                                    events.setValue(row, column, columnValue);
                                }
                                if (stability) {
                                    events.setString(row, "studyid", studyidstr);
                                    events.setString(row, "studydesc", studydescstr);
                                    events.setString(row, "stabilitydepartmentid", stabilitydepartmentid);
                                    events.setString(row, "qtypull", qtypull);
                                    events.setString(row, "qtypulltype", qtypulltype);
                                    events.setString(row, "qtypullunits", qtypullunits);
                                    events.setString(row, "schdulerule", scheduleruleStr);
                                    events.setString(row, "scheduletimerule", scheduletimerule);
                                    events.setString(row, "conditionlabel", conditionlabel);
                                    events.setString(row, "scheduletimerule", scheduletimerule);
                                    events.setString(row, "conditionlabel", conditionlabel);
                                    String eventStatus = events.getString(row, "eventstatus", "");
                                    if (eventStatus.equals("S") && !isSummaryMode) {
                                        HashMap<String, Calendar> grcMap = this.calculateGRCPerioidForPlanItem(planId, planItemId, startDt, propertyTreeid, scheduleTaskNodeId, originalEventDt, true);
                                        Calendar windowStartDt = grcMap.get("windowstartdt");
                                        Calendar windowEndDt = grcMap.get("windowenddt");
                                        if (windowStartDt != null) {
                                            events.setDate(row, "windowstartdt", windowStartDt);
                                            events.setString(row, "windowstartdtstr", m18NTimezone.format(windowStartDt));
                                        }
                                        if (windowEndDt != null) {
                                            events.setDate(row, "windowenddt", windowEndDt);
                                            events.setString(row, "windowenddtstr", m18NTimezone.format(windowEndDt));
                                        }
                                    }
                                }
                            }
                            previousEventDt = (Calendar)eventDt.clone();
                            previousEventNum = eventNum;
                        }
                    }
                    if (!getNew || stability) continue;
                    Calendar initialDt = Calendar.getInstance(timeZone);
                    ScheduleEvent scheduleEvent = new ScheduleEvent(scheduleData.getString(i, "scheduleplanid"), scheduleData.getString(i, "scheduleplanitemid"), initialDt, scheduleData.getString(i, "linksdcid"), scheduleData.getString(i, "linkkeyid1"), scheduleData.getString(i, "linkkeyid2"), scheduleData.getString(i, "linkkeyid3"), timeZone);
                    if (folderStatus.equals("X") || !planItemStatus.equals("A") || !planStatus.equals("A")) {
                        scheduleEvent.setEventStatus("I");
                    } else {
                        scheduleEvent.setEventStatus("P");
                    }
                    ArrayList newEvents = scheduleRule.getEvents(scheduleEvent, getNewItemsFromDateTime != null ? getNewItemsFromDateTime.toGregorianCalendar() : null, getNewItemsToDateTime != null ? getNewItemsToDateTime.toGregorianCalendar() : null, startDateTime.toGregorianCalendar(), true);
                    int executeAhead = scheduleData.getInt(i, "executeahead");
                    String executeAheadUnits = scheduleData.getString(i, "executeaheadunits");
                    String propertytreeid = scheduleData.getString(i, "propertytreeid");
                    String scheduletasknodeid = scheduleData.getString(i, "scheduletasknodeid");
                    boolean backFillFlag = scheduleData.getString(i, "backfillflag", "N").startsWith("Y");
                    for (int j = 0; j < newEvents.size(); ++j) {
                        ScheduleEvent event = (ScheduleEvent)newEvents.get(j);
                        DateTime eventDateTime = event.getEventDateTime();
                        DateTime planDateTimeNow = new DateTime(event.getDateTimeZone());
                        planDateTimeNow = planDateTimeNow.minusSeconds(schedulePoll);
                        DateTime originalDateTime = event.getOriginalEventDateTime();
                        if (!backFillFlag && !originalDateTime.isAfter((ReadableInstant)planDateTimeNow) || !eventDateTime.isEqual((ReadableInstant)scheduledToDateTime) && !eventDateTime.isAfter((ReadableInstant)scheduledToDateTime) || stopDateTime != null && !eventDateTime.isBefore((ReadableInstant)stopDateTime)) continue;
                        this.checkEventExclusion(event, scheduleExcludeMode, exclude);
                        if (event.getEventStatus().equals("O") || event.getEventDt().compareTo(fromDate) < 0 || event.getEventDt().compareTo(toDate) >= 0) continue;
                        BigDecimal eventnum = BigDecimal.ONE;
                        boolean duplicate = false;
                        for (int k = 0; k < dbEvents.getRowCount(); ++k) {
                            Calendar dbEventdt = dbEvents.getCalendar(k, "originaleventdt");
                            BigDecimal dbEventnum = dbEvents.getBigDecimal(k, "eventnum");
                            String adhocFlag = dbEvents.getString(k, "adhocflag", "");
                            if (dbEventdt.compareTo(event.getEventDt()) != 0 || dbEventnum.compareTo(eventnum) != 0 || !adhocFlag.isEmpty()) continue;
                            duplicate = true;
                            break;
                        }
                        if (duplicate || status.equals("F") && !event.getEventStatus().equals("F")) continue;
                        int row = events.addRow();
                        events.setString(row, "scheduleplanid", event.getSchedulePlanid());
                        events.setString(row, "scheduleplanitemid", event.getSchedulePlanitemid());
                        events.setString(row, "linksdcid", event.getLinkSdcid());
                        events.setString(row, "linkkeyid1", event.getLinkKeyid1());
                        events.setString(row, "linkkeyid2", event.getLinkKeyid2());
                        events.setString(row, "linkkeyid3", event.getLinkKeyid3());
                        events.setString(row, "scheduleplanitemdesc", schedulePlanItemDesc);
                        events.setDate(row, "eventdt", event.getEventDt());
                        events.setString(row, "adhocflag", event.isAdHocFlag() ? "Y" : "");
                        events.setDate(row, "originaleventdt", event.getOriginalEventDt());
                        Calendar executeDtInstance = Calendar.getInstance();
                        ScheduleService.setLookaheadDate(event.getEventDt(), executeAhead * -1, executeAheadUnits, executeDtInstance);
                        events.setDate(row, "executedt", executeDtInstance);
                        events.setString(row, "executedtstr", m18NTimezone.format(executeDtInstance));
                        events.setString(row, "eventdtstr", m18NTimezone.format(event.getEventDt()));
                        events.setString(row, "originaleventdtstr", m18NTimezone.format(event.getOriginalEventDt()));
                        events.setString(row, "eventstatus", event.getEventStatus());
                        events.setNumber(row, "eventnum", eventnum);
                        events.setString(row, "propertytreeid", propertytreeid);
                        events.setString(row, "scheduletasknodeid", scheduletasknodeid);
                        events.setString(row, "schedulerule", scheduleRule.getEnteredRule());
                        events.setString(row, "timezone", timezoneStr);
                        events.setString(row, "scheduletemplatesdcid", scheduleTemplateSdcId);
                        events.setString(row, "scheduletemplatekeyid1", scheduleTemplateKeyId1);
                        events.setString(row, "scheduletemplatekeyid2", scheduleTemplateKeyId2);
                        events.setString(row, "scheduletemplatekeyid3", scheduleTemplateKeyId3);
                        events.setString(row, "randomizationmode", randomizationMode);
                        events.setString(row, "randomizationtype", randomizationType);
                        events.setNumber(row, "randomizationcount", randomizationCount);
                        events.setString(row, "source", isJUnit ? event.getLinkSdcid() : this.getTranslationProcessor().translate(event.getLinkSdcid()) + ": " + event.getLinkKeyid1());
                        String adhocFlag = dbEvents.getString(row, "adhocflag", "").isEmpty() ? "0" : "1";
                        events.setNumber(row, "eventid", new BigDecimal(Integer.toString(i) + Long.toString(event.getOriginalEventDt().getTimeInMillis())) + eventnum.toString() + adhocFlag);
                        if (stability) {
                            events.setString(row, "studyid", studyidstr);
                            events.setString(row, "stabilitydepartmentid", stabilitydepartmentid);
                            events.setString(row, "qtypull", qtypull);
                            events.setString(row, "qtypulltype", qtypulltype);
                            events.setString(row, "qtypullunits", qtypullunits);
                            events.setString(row, "schdulerule", scheduleruleStr);
                            events.setString(row, "scheduletimerule", scheduletimerule);
                            events.setString(row, "conditionlabel", conditionlabel);
                        }
                        row = dbEvents.addRow();
                        dbEvents.setString(row, "scheduleplanid", event.getSchedulePlanid());
                        dbEvents.setString(row, "scheduleplanitemid", event.getSchedulePlanitemid());
                        dbEvents.setString(row, "linksdcid", event.getLinkSdcid());
                        dbEvents.setString(row, "linkkeyid1", event.getLinkKeyid1());
                        dbEvents.setString(row, "linkkeyid2", event.getLinkKeyid2());
                        dbEvents.setString(row, "linkkeyid3", event.getLinkKeyid3());
                        dbEvents.setString(row, "scheduleplanitemdesc", schedulePlanItemDesc);
                        dbEvents.setDate(row, "eventdt", event.getEventDt());
                        dbEvents.setDate(row, "originaleventdt", event.getOriginalEventDt());
                        dbEvents.setDate(row, "executedt", executeDtInstance);
                        dbEvents.setNumber(row, "eventnum", eventnum);
                    }
                }
            }
            catch (SapphireException e) {
                throw new ServiceException("SCHEDULE_SERVICE_FAILED", "Failed to get event list", e);
            }
            finally {
                db.reset();
                if (rsetPlanItem != null && rsetPlanItem.getRsetid().length() > 0) {
                    try {
                        dataAccessService.clearRSet(rsetPlanItem);
                    }
                    catch (ServiceException serviceException) {}
                }
                if (rsetPlan != null && rsetPlan.getRsetid().length() > 0) {
                    try {
                        dataAccessService.clearRSet(rsetPlan);
                    }
                    catch (ServiceException serviceException) {}
                }
            }
        }
        return events;
    }

    public HashMap<String, Calendar> calculateGRCPerioidForPlanItem(String planId, String planItemId, Calendar startDt, String propertyTreeid, String scheduleTaskNodeId, Calendar originalEventDt, boolean isStabilityGrid) {
        SafeSQL safeSQL = new SafeSQL();
        HashMap<String, Calendar> returnMap = new HashMap<String, Calendar>();
        try {
            PropertyList planItemProps = this.getPlanItemProperties(propertyTreeid, scheduleTaskNodeId, planId, planItemId, isStabilityGrid);
            PropertyList workorder = planItemProps.getPropertyList("workorder");
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
            String grcperiod = "";
            String grcperiodunit = "";
            String earlygrcperiod = "";
            DataSet ds = null;
            String sql = "";
            Calendar gracePerioidStartDt = Calendar.getInstance();
            safeSQL.reset();
            sql = "SELECT sc.startdt from schedulecondition sc, scheduleplanitem si  WHERE si.scheduleplanid = " + safeSQL.addVar(planId) + " AND si.scheduleplanitemid = " + safeSQL.addVar(planItemId) + " AND sc.scheduleplanid = si.scheduleplanid AND sc.scheduleconditionid = si.scheduleconditionid ";
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (ds != null && ds.getRowCount() > 0) {
                gracePerioidStartDt.setTime(ds.getTimestamp(0, "startdt"));
                String[] gracePeriod = StringUtil.split(GRCPeriodUtil.getGRCPeriodFromTaskCollection(plGracePeriodDetails, gracePerioidStartDt, originalEventDt), ";");
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
            float gracePeriod = 0.0f;
            float earlyGracePeriod = 0.0f;
            Calendar windowStartDt = Calendar.getInstance();
            Calendar windowEndDt = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat();
            gracePeriod = new BigDecimal(grcperiod).floatValue();
            earlyGracePeriod = new BigDecimal(earlygrcperiod).floatValue();
            if (grcperiodunit.length() > 0 && gracePeriod > 0.0f) {
                String[] dates = StringUtil.split(GRCPeriodUtil.getWindowStartEndDates(startDt, originalEventDt, grcperiodunit, gracePeriod, earlyGracePeriod), ";");
                try {
                    windowStartDt.setTime(sdf.parse(dates[0]));
                    windowEndDt.setTime(sdf.parse(dates[1]));
                    if (startDt != null && startDt.after(windowStartDt)) {
                        windowStartDt.setTime(startDt.getTime());
                    }
                }
                catch (Exception pe) {
                    this.logger.error("Error : Could not parse grace period for schedule event list", pe);
                }
            }
            returnMap.put("windowstartdt", windowStartDt);
            returnMap.put("windowenddt", windowEndDt);
        }
        catch (Exception exception) {
            // empty catch block
        }
        return returnMap;
    }

    private void getChildNodes(StringBuilder newNodeBuilder, DBUtil db, String newNodes) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select SCHEDULEPLANNODEID from scheduleplannode where parentnodeid in (" + safeSQL.addIn(newNodes, ";") + ")";
        db.createPreparedResultSet(sql, safeSQL.getValues());
        DataSet nodeDs = new DataSet(db.getResultSet());
        if (nodeDs.getRowCount() > 0) {
            String nodes;
            newNodes = nodes = nodeDs.getColumnValues("scheduleplannodeid", ";");
            newNodeBuilder.append(";").append(nodes);
            this.getChildNodes(newNodeBuilder, db, newNodes);
        }
    }

    public DataSet getScheduleExcludes(PropertyList properties) throws ServiceException {
        String scheduleplanid = properties.getProperty("scheduleplanid", "");
        String calendarId = properties.getProperty("calendarid", "");
        String fromDateStr = properties.getProperty("fromdate");
        String toDateStr = properties.getProperty("todate");
        if (scheduleplanid.equals("null")) {
            scheduleplanid = "";
        }
        M18NUtil m18n = new M18NUtil(this.connectionInfo);
        Calendar toDate = null;
        Calendar fromDate = null;
        if (fromDateStr != null && fromDateStr.length() > 0) {
            fromDate = m18n.parseCalendar(fromDateStr);
        }
        if (toDateStr != null && toDateStr.length() > 0) {
            toDate = m18n.parseCalendar(toDateStr);
        }
        if (fromDate == null || toDate == null) {
            return new DataSet();
        }
        fromDate.set(14, 0);
        fromDate.set(13, 0);
        fromDate.set(10, 0);
        LocalDate fromDateJoda = new LocalDate((Object)fromDate);
        LocalDate toDateJoda = new LocalDate((Object)toDate);
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        DataSet events = null;
        try {
            if (scheduleplanid.isEmpty() && !calendarId.isEmpty()) {
                ScheduleExclude exclude = new ScheduleExclude(this.sapphireConnection, this.logger);
                exclude.setCalendar(calendarId);
                exclude.setStartdt(fromDate);
                exclude.setEnddt(toDate);
                events = exclude.getExcludesDs();
            } else if (!scheduleplanid.isEmpty()) {
                HashSet<String> plansSet = new HashSet<String>();
                plansSet.addAll(Arrays.asList(StringUtil.split(scheduleplanid, ";")));
                for (String scheduleplan : plansSet) {
                    if (fromDate == null) {
                        fromDate = Calendar.getInstance();
                    }
                    ScheduleExclude exclude = new ScheduleExclude(this.sapphireConnection, this.logger);
                    exclude.setSchedulePlanId(scheduleplan);
                    exclude.setStartdt(fromDate);
                    exclude.setEnddt(toDate);
                    events = exclude.getExcludesDs();
                }
            }
        }
        catch (SapphireException e) {
            throw new ServiceException(e.getCause());
        }
        return events;
    }

    private void addScheduleEvents(DataSet scheduleData, Calendar refDt, boolean enforceStatus, Calendar nowCalendar) throws ServiceException {
        this.logInfo("Adding schedule events from " + DateFormatter.formatDateTime(refDt));
        Calendar scheduledToDt = (Calendar)nowCalendar.clone();
        Calendar nextScheduleDt = (Calendar)nowCalendar.clone();
        Timestamp now = new Timestamp(nowCalendar.getTime().getTime());
        ScheduleRule scheduleRule = new ScheduleRule();
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        String currentUser = this.sapphireConnection.getSysuserId();
        String tool = this.sapphireConnection.getTool();
        HashMap<String, ScheduleExclude> planExcludes = new HashMap<String, ScheduleExclude>();
        try {
            int scheduleBackFillGracePerioid;
            int schedulePoll;
            db.setConnection(this.sapphireConnection);
            PreparedStatement insertPS = db.prepareStatement("insert", "INSERT INTO scheduleevent ( scheduleplanid, scheduleplanitemid, eventdt, eventnum, eventstatus, createdt, executedt, originaleventdt, createby, createtool, tracelogid, adhocflag ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
            PreparedStatement updatePS = db.prepareStatement("update", "UPDATE scheduleplanitemexec SET lastscheduledt = ?, nextscheduledt = ?, scheduledtodt = ?, lasteventdt = ?, moddt = ?, modby = ?, modtool = ? WHERE scheduleplanid = ? AND scheduleplanitemid = ?");
            try {
                ConfigurationProcessor cp = new ConfigurationProcessor(this.getConnectionid());
                schedulePoll = Integer.valueOf(cp.getSysConfigProperty("schedulepoll"));
            }
            catch (Exception e) {
                schedulePoll = 3600;
            }
            try {
                ConfigurationProcessor cp = new ConfigurationProcessor(this.getConnectionid());
                scheduleBackFillGracePerioid = Integer.valueOf(cp.getSysConfigProperty("schedulebackfillgraceperiod"));
            }
            catch (Exception e) {
                scheduleBackFillGracePerioid = 600;
            }
            for (int i = 0; i < scheduleData.size(); ++i) {
                String defaultTimeZone = TimeZone.getDefault().getID();
                String timeZoneStr = scheduleData.getString(i, "timezone", defaultTimeZone);
                TimeZone timeZone = TimeZone.getTimeZone(timeZoneStr);
                DateTimeZone dateTimeZone = DateTimeZone.forID((String)timeZoneStr);
                boolean backFillFlag = scheduleData.getString(i, "backfillflag", "N").startsWith("Y");
                String scheduleItem = "SchedulePlanId = " + scheduleData.getString(i, "scheduleplanid") + ", Schedule Plan Item ID = " + scheduleData.getString(i, "scheduleplanitemid") + ", SourceSDCId = " + scheduleData.getString(i, "linksdcid") + ", SourceKeyid1 = " + scheduleData.getString(i, "linkkeyid1") + ", SourceKeyid2 = " + scheduleData.getString(i, "linkkeyid2") + ", SourceKeyid3 = " + scheduleData.getString(i, "linkkeyid3");
                if (!enforceStatus || scheduleData.getString(i, "planstatus", "X").equals("A") && scheduleData.getString(i, "planitemstatus", "X").equals("A")) {
                    if (scheduleData.getTimestamp(i, "startdt") != null && scheduleData.getTimestamp(i, "nextscheduledt") != null && scheduleData.getString(i, "schedulerule") != null) {
                        ArrayList events;
                        DateTime fromDateTime;
                        DateTime refDateTime = new DateTime((Object)refDt, dateTimeZone);
                        DateTime startDateTime = new DateTime((Object)scheduleData.getCalendar(i, "startdt"), dateTimeZone);
                        DateTime stopDateTime = null;
                        scheduleRule.setRule(scheduleData.getString(i, "schedulerule"));
                        if (!scheduleRule.isValidRule(true)) {
                            this.logger.error("Schedule Plan Item has invalid Schedule Rule. Disabling Schedule Plan Item. Item:" + scheduleItem);
                            this.disablePlanItem(db, scheduleData.getString(i, "scheduleplanid"), scheduleData.getString(i, "scheduleplanitemid"));
                            continue;
                        }
                        if (scheduleData.getTimestamp(i, "stopdt") != null) {
                            stopDateTime = new DateTime((Object)scheduleData.getCalendar(i, "stopdt"), dateTimeZone);
                        }
                        Calendar lastEventDt = null;
                        DateTime lastEventDateTime = null;
                        if (scheduleData.getTimestamp(i, "lasteventdt") != null) {
                            lastEventDt = Calendar.getInstance(timeZone);
                            lastEventDt.setTime(scheduleData.getTimestamp(i, "lasteventdt"));
                            lastEventDt.set(14, 0);
                            lastEventDt.get(5);
                            lastEventDateTime = new DateTime((Object)scheduleData.getCalendar(i, "lasteventdt"), dateTimeZone);
                        }
                        refDt.setTimeZone(timeZone);
                        refDt.get(5);
                        DateTime scheduledToDateTime = scheduleData.getTimestamp(i, "scheduledtodt") != null ? new DateTime((Object)scheduleData.getCalendar(i, "scheduledtodt"), dateTimeZone) : new DateTime((Object)scheduleData.getCalendar(i, "startdt"), dateTimeZone);
                        if (scheduleRule.getRuleType() != 0 && scheduleRule.getRuleType() != 1) {
                            scheduledToDateTime = scheduledToDateTime.toLocalDate().toDateTimeAtStartOfDay(dateTimeZone);
                        }
                        if (scheduledToDateTime.compareTo((ReadableInstant)startDateTime) < 0) {
                            scheduledToDateTime = startDateTime;
                        }
                        DateTime dateTime = fromDateTime = scheduledToDateTime.isBefore((ReadableInstant)refDateTime) ? scheduledToDateTime : refDateTime;
                        if (scheduleRule.getRuleType() == 0 || scheduleRule.getRuleType() == 1 || (scheduleRule.getRuleType() == 3 || scheduleRule.getRuleType() == 2) && scheduleRule.getOrdinal() > 1) {
                            fromDateTime = lastEventDt != null ? (startDateTime.isAfter((ReadableInstant)lastEventDateTime) ? startDateTime : lastEventDateTime) : startDateTime;
                        }
                        int scheduleahead = scheduleData.getInt(i, "scheduleahead");
                        String scheduleAheadUnits = scheduleData.getString(i, "scheduleaheadunits");
                        DateTime toDateTime = ScheduleService.setLookaheadDate(refDateTime, scheduleahead, scheduleAheadUnits);
                        Calendar initialDt = Calendar.getInstance(timeZone);
                        ScheduleEvent scheduleEvent = new ScheduleEvent(scheduleData.getString(i, "scheduleplanid"), scheduleData.getString(i, "scheduleplanitemid"), initialDt, timeZone);
                        scheduleEvent.setEventStatus("S");
                        try {
                            events = scheduleRule.getEvents(scheduleEvent, fromDateTime.toGregorianCalendar(), toDateTime.toGregorianCalendar(), startDateTime.toGregorianCalendar(), true);
                        }
                        catch (SapphireException e) {
                            this.logger.error("Could not generate events for Schedule Plan Item. Disabling Schedule Plan Item: " + scheduleItem);
                            this.disablePlanItem(db, scheduleData.getString(i, "scheduleplanid"), scheduleData.getString(i, "scheduleplanitemid"));
                            continue;
                        }
                        String excludeModetype = scheduleData.getString(i, "excludemodetype", "S");
                        String getExistingEventsSql = "select scheduleplanid, scheduleplanitemid, originaleventdt, eventdt from scheduleevent where scheduleplanid = ? and scheduleplanitemid = ? and originaleventdt >= ? and originaleventdt <= ? and coalesce(adhocflag, 'N') = 'N'";
                        String schedulePlanId = scheduleData.getString(i, "scheduleplanid");
                        String schedulePlanItemId = scheduleData.getString(i, "scheduleplanitemid");
                        events = ScheduleService.cleanEvents(scheduledToDateTime, schedulePoll, scheduleBackFillGracePerioid, backFillFlag, stopDateTime, events, refDt);
                        if (events.size() > 0) {
                            Calendar minEventDt = ((ScheduleEvent)events.get(0)).getOriginalEventDt();
                            Calendar maxEventDt = ((ScheduleEvent)events.get(events.size() - 1)).getOriginalEventDt();
                            db.createPreparedResultSet(getExistingEventsSql, new Object[]{schedulePlanId, schedulePlanItemId, new Timestamp(minEventDt.getTimeInMillis()), new Timestamp(maxEventDt.getTimeInMillis())});
                            DataSet oldEventsDs = new DataSet(db.getResultSet());
                            for (int j = 0; j < oldEventsDs.getRowCount(); ++j) {
                                Calendar existingEventOriginalEventDt = oldEventsDs.getCalendar(j, "originaleventdt");
                                existingEventOriginalEventDt.setTimeZone(timeZone);
                                for (int k = events.size() - 1; k >= 0; --k) {
                                    ScheduleEvent event2 = (ScheduleEvent)events.get(k);
                                    Calendar eventDt = event2.getEventDt();
                                    if (existingEventOriginalEventDt == null || eventDt.get(1) != existingEventOriginalEventDt.get(1) || eventDt.get(2) != existingEventOriginalEventDt.get(2) || eventDt.get(5) != existingEventOriginalEventDt.get(5) || eventDt.get(11) != existingEventOriginalEventDt.get(11) || eventDt.get(12) != existingEventOriginalEventDt.get(12) || eventDt.get(13) != existingEventOriginalEventDt.get(13)) continue;
                                    events.remove(k);
                                    this.logInfo("Event already exists (pre-check): " + event2.toString());
                                }
                            }
                        }
                        Calendar newLastEventDt = lastEventDt == null ? Calendar.getInstance(timeZone) : (Calendar)lastEventDt.clone();
                        ScheduleExclude exclude = (ScheduleExclude)planExcludes.get(schedulePlanId);
                        if (exclude == null) {
                            exclude = this.rakFile == null ? new ScheduleExclude(this.sapphireConnection, this.logger) : new ScheduleExclude(this.sapphireConnection, this.logger, this.rakFile);
                            exclude.setSchedulePlanId(schedulePlanId);
                            planExcludes.put(schedulePlanId, exclude);
                        }
                        scheduledToDt = scheduledToDateTime.toGregorianCalendar();
                        int newEvents = this.insertScheduleEvents(insertPS, events, scheduledToDt, stopDateTime, excludeModetype, scheduleData.getInt(i, "executeahead"), scheduleData.getString(i, "executeaheadunits"), null, newLastEventDt, backFillFlag, schedulePoll, scheduleBackFillGracePerioid, db, exclude, refDt);
                        boolean isValidPlanItem = true;
                        DateTime planDateTimeNow = toDateTime;
                        if (stopDateTime != null && planDateTimeNow.isAfter((ReadableInstant)stopDateTime)) {
                            isValidPlanItem = false;
                        }
                        try {
                            updatePS.setTimestamp(1, now);
                            if (isValidPlanItem) {
                                ScheduleService.updateNextScheduleDt(refDt, nextScheduleDt, toDateTime.toGregorianCalendar(), scheduleRule, scheduleahead, scheduleAheadUnits);
                                updatePS.setTimestamp(2, new Timestamp(nextScheduleDt.getTime().getTime()));
                                updatePS.setTimestamp(3, new Timestamp(toDateTime.toGregorianCalendar().getTime().getTime()));
                                if (newEvents > 0) {
                                    lastEventDt = (Calendar)newLastEventDt.clone();
                                }
                                if (lastEventDt != null) {
                                    updatePS.setTimestamp(4, new Timestamp(lastEventDt.getTime().getTime()));
                                } else {
                                    updatePS.setNull(4, 93);
                                }
                            } else {
                                updatePS.setNull(2, 93);
                                updatePS.setNull(3, 93);
                                updatePS.setNull(4, 93);
                            }
                            updatePS.setTimestamp(5, now);
                            updatePS.setString(6, currentUser);
                            updatePS.setString(7, tool);
                            updatePS.setString(8, scheduleData.getString(i, "scheduleplanid"));
                            updatePS.setString(9, scheduleData.getString(i, "scheduleplanitemid"));
                            updatePS.executeUpdate();
                        }
                        catch (SQLException sqle) {
                            throw new SapphireException("Error updating nextscheduledt for " + scheduleItem + ". Exception: " + ErrorUtil.extractMessageFromException(sqle, ErrorUtil.isUserAdmin(this.sapphireConnection.getConnectionId())));
                        }
                        this.logInfo("Generated " + newEvents + " new events for " + scheduleItem);
                        continue;
                    }
                    this.logInfo("StartDt/NextScheduleDt/ScheduleRule null: " + scheduleItem);
                    continue;
                }
                this.logInfo("Event disabled: " + scheduleItem);
            }
        }
        catch (SapphireException e) {
            throw new ServiceException("SCHEDULE_SERVICE_FAILED", "Failed to add schedule events", e);
        }
        finally {
            db.reset();
        }
    }

    public static ArrayList cleanEvents(DateTime scheduledToDt, int schedulePoll, int scheduleBackFillGracePerioid, boolean backFillFlag, DateTime stopDt, ArrayList events, Calendar refDt) {
        ArrayList<ScheduleEvent> events2 = new ArrayList<ScheduleEvent>();
        boolean done = false;
        for (int j = 0; j < events.size() && !done; ++j) {
            ScheduleEvent event = (ScheduleEvent)events.get(j);
            DateTime eventdt = event.getEventDateTime();
            DateTime planDateTimeNow = new DateTime(refDt.getTimeInMillis(), event.getDateTimeZone());
            planDateTimeNow = planDateTimeNow.minusSeconds(schedulePoll);
            planDateTimeNow = planDateTimeNow.minusSeconds(scheduleBackFillGracePerioid);
            DateTime originalDateTime = event.getOriginalEventDateTime();
            if (!backFillFlag && !originalDateTime.isAfter((ReadableInstant)planDateTimeNow) || !originalDateTime.isEqual((ReadableInstant)scheduledToDt) && !originalDateTime.isAfter((ReadableInstant)scheduledToDt)) continue;
            if (stopDt == null || eventdt.isBefore((ReadableInstant)stopDt)) {
                events2.add(event);
                continue;
            }
            done = true;
            if (!events2.isEmpty()) continue;
            scheduledToDt = originalDateTime;
        }
        return events2;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String addScheduledEvent(ScheduleEvent event, boolean executed) {
        String msg = "";
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            ArrayList<ScheduleEvent> events = new ArrayList<ScheduleEvent>();
            events.add(event);
            Calendar scheduledToDt = (Calendar)event.getEventDt().clone();
            Calendar executeDt = Calendar.getInstance();
            scheduledToDt.add(5, -1);
            if (!executed) {
                Calendar eventDt = event.getEventDt();
                int executeAhead = 1;
                String sql = "select executeahead, executeaheadunits from scheduleplan where scheduleplanid = ?";
                db.createPreparedResultSet(sql, new Object[]{event.getSchedulePlanid()});
                DataSet executeAheadDs = new DataSet(db.getResultSet());
                String executeAheadUnits = "";
                if (executeAheadDs.getRowCount() > 0) {
                    executeAhead = executeAheadDs.getInt(0, "executeahead");
                    executeAheadUnits = executeAheadDs.getString(0, "executeaheadunits");
                }
                ScheduleService.setLookaheadDate(eventDt, executeAhead * -1, executeAheadUnits, executeDt);
            }
            Timestamp now = DateTimeUtil.getNowTimestamp();
            String createBy = this.sapphireConnection.getSysuserId();
            String createTool = this.sapphireConnection.getTool();
            PreparedStatement insertPS = db.prepareStatement("insert", "INSERT INTO scheduleevent ( scheduleplanid, scheduleplanitemid, eventdt, eventnum, eventstatus, createdt, executedt, originaleventdt, createby, createtool, tracelogid, adhocflag ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
            String traceLogId = null;
            int newEvents = 0;
            try {
                this.insertEvent(insertPS, traceLogId, newEvents, executeDt, now, createBy, createTool, event, false, db);
            }
            catch (ServiceException e) {
                msg = e.getMessage();
            }
        }
        catch (SapphireException exception) {
            msg = this.getTranslationProcessor().translate("Failure in adding event.");
        }
        finally {
            db.reset();
        }
        return msg;
    }

    private int insertScheduleEvents(PreparedStatement insertPS, ArrayList events, Calendar scheduledToDt, Calendar stopDt, String scheduleExcludeMode, int executeAhead, String executeAheadUnits, String traceLogId, Calendar lastEventDt, boolean backFillFlag, int schedulePoll, int scheduleBackFillGracePerioid, DBUtil dbu, ScheduleExclude exclude) throws ServiceException {
        return this.insertScheduleEvents(insertPS, events, scheduledToDt, stopDt != null ? new DateTime((Object)stopDt) : null, scheduleExcludeMode, executeAhead, executeAheadUnits, traceLogId, lastEventDt, backFillFlag, schedulePoll, scheduleBackFillGracePerioid, dbu, exclude, Calendar.getInstance());
    }

    private int insertScheduleEvents(PreparedStatement insertPS, ArrayList events, Calendar scheduledToDt, DateTime stopDt, String scheduleExcludeMode, int executeAhead, String executeAheadUnits, String traceLogId, Calendar lastEventDt, boolean backFillFlag, int schedulePoll, int scheduleBackFillGracePerioid, DBUtil dbu, ScheduleExclude exclude, Calendar refDt) throws ServiceException {
        boolean done = false;
        int newEvents = 0;
        Calendar executeDt = Calendar.getInstance();
        Timestamp now = DateTimeUtil.getNowTimestamp();
        String createBy = this.sapphireConnection.getSysuserId();
        String createTool = this.sapphireConnection.getTool();
        Calendar firstDate = null;
        Calendar lastDate = null;
        for (int j = 0; j < events.size() && !done; ++j) {
            GregorianCalendar originalEventDt;
            ScheduleEvent event = (ScheduleEvent)events.get(j);
            if (j == 0) {
                firstDate = (Calendar)event.getEventDt().clone();
                ScheduleEvent lastEvent = (ScheduleEvent)events.get(events.size() - 1);
                lastDate = (Calendar)lastEvent.getEventDt().clone();
                exclude.setStartdt(firstDate);
                exclude.setEnddt(lastDate);
            }
            DateTime eventdt = event.getEventDateTime();
            DateTime scheduledToDateTime = new DateTime((Object)scheduledToDt, event.getDateTimeZone());
            DateTime planDateTimeNow = new DateTime(refDt.getTimeInMillis(), event.getDateTimeZone());
            planDateTimeNow = planDateTimeNow.minusSeconds(schedulePoll);
            planDateTimeNow = planDateTimeNow.minusSeconds(scheduleBackFillGracePerioid);
            DateTime originalDateTime = event.getOriginalEventDateTime();
            int timerule_executeAhead = event.getExecuteaheadTime();
            String timerule_executeAheadUnits = event.getExecuteaheadTimeUnit();
            if (timerule_executeAhead == -999999999 || timerule_executeAheadUnits == null || timerule_executeAheadUnits.length() == 0) {
                timerule_executeAhead = executeAhead;
                timerule_executeAheadUnits = executeAheadUnits;
            }
            if (!backFillFlag && !originalDateTime.isAfter((ReadableInstant)planDateTimeNow) || !originalDateTime.isEqual((ReadableInstant)scheduledToDateTime) && !originalDateTime.isAfter((ReadableInstant)scheduledToDateTime)) continue;
            if (stopDt == null || eventdt.isBefore((ReadableInstant)stopDt)) {
                this.checkEventExclusion(event, scheduleExcludeMode, exclude);
                if (!event.getEventStatus().equals("O")) {
                    executeDt = ScheduleService.setLookaheadDate(eventdt, timerule_executeAhead * -1, timerule_executeAheadUnits).toGregorianCalendar();
                    newEvents = this.insertEvent(insertPS, traceLogId, newEvents, executeDt, now, createBy, createTool, event, true, dbu);
                }
                originalEventDt = originalDateTime.toGregorianCalendar();
                lastEventDt.set(originalEventDt.get(1), originalEventDt.get(2), originalEventDt.get(5), originalEventDt.get(11), originalEventDt.get(12), originalEventDt.get(13));
                continue;
            }
            done = true;
            originalEventDt = originalDateTime.toGregorianCalendar();
            scheduledToDt.set(originalEventDt.get(1), originalEventDt.get(2), originalEventDt.get(5), originalEventDt.get(11), originalEventDt.get(12), originalEventDt.get(13));
        }
        return newEvents;
    }

    private int insertEvent(PreparedStatement insertPS, String traceLogId, int newEvents, Calendar executeDt, Timestamp now, String createBy, String createTool, ScheduleEvent event, boolean checkEventNum, DBUtil dbu) throws ServiceException {
        try {
            insertPS.setString(1, event.getSchedulePlanid());
            insertPS.setString(2, event.getSchedulePlanitemid());
            insertPS.setTimestamp(3, new Timestamp(event.getEventDt().getTime().getTime()));
            insertPS.setInt(4, event.getEventNum());
            insertPS.setString(5, event.getEventStatus());
            insertPS.setTimestamp(6, now);
            insertPS.setTimestamp(7, new Timestamp(executeDt.getTime().getTime()));
            insertPS.setTimestamp(8, new Timestamp(event.getOriginalEventDt().getTime().getTime()));
            insertPS.setString(9, createBy);
            insertPS.setString(10, createTool);
            insertPS.setString(11, traceLogId);
            insertPS.setString(12, event.isAdHocFlag() ? "Y" : null);
            insertPS.executeUpdate();
            ++newEvents;
        }
        catch (SQLException sqle) {
            if (checkEventNum) {
                try {
                    dbu.createPreparedResultSet("select", "SELECT scheduleplanid, scheduleplanitemid, eventdt, eventnum from scheduleevent where scheduleplanid=? AND scheduleplanitemid=? AND eventdt=? order by eventnum desc", new Object[]{event.getSchedulePlanid(), event.getSchedulePlanitemid(), new Timestamp(event.getEventDt().getTime().getTime())});
                    int newEventnum = 1;
                    if (dbu.getNext("select")) {
                        newEventnum = dbu.getInt("select", "eventnum") + 1;
                    }
                    event.setEventNum(newEventnum);
                    this.insertEvent(insertPS, traceLogId, newEvents, executeDt, now, createBy, createTool, event, false, dbu);
                }
                catch (SapphireException e) {
                    throw new ServiceException("Error creating schedule events for event: " + event.toString() + " Exception: " + sqle.getMessage(), sqle);
                }
            }
            throw new ServiceException("Error creating schedule events for event: " + event.toString() + " Exception: " + sqle.getMessage(), sqle);
        }
        return newEvents;
    }

    public static void setLookaheadDate(Calendar fromDt, int scheduleAhead, String scheduleAheadUnits, Calendar toDt) {
        toDt.setTime(fromDt.getTime());
        if (scheduleAheadUnits != null && scheduleAheadUnits.length() > 0 && scheduleAhead != -999999999) {
            switch (scheduleAheadUnits.charAt(0)) {
                case 'H': {
                    toDt.add(10, scheduleAhead);
                    break;
                }
                case 'D': {
                    toDt.add(5, scheduleAhead);
                    break;
                }
                case 'W': {
                    toDt.add(5, scheduleAhead * 7);
                    break;
                }
                case 'M': {
                    toDt.add(2, scheduleAhead);
                    break;
                }
                case 'Y': {
                    toDt.add(1, scheduleAhead);
                }
            }
        }
    }

    public static DateTime setLookaheadDate(DateTime fromDt, int scheduleAhead, String scheduleAheadUnits) {
        DateTime toDt = fromDt;
        if (scheduleAheadUnits != null && scheduleAheadUnits.length() > 0 && scheduleAhead != -999999999) {
            switch (scheduleAheadUnits.charAt(0)) {
                case 'H': {
                    toDt = fromDt.plusHours(scheduleAhead);
                    break;
                }
                case 'D': {
                    toDt = fromDt.plusDays(scheduleAhead);
                    break;
                }
                case 'W': {
                    toDt = fromDt.plusDays(scheduleAhead * 7);
                    break;
                }
                case 'M': {
                    toDt = fromDt.plusMonths(scheduleAhead);
                    break;
                }
                case 'Y': {
                    toDt = fromDt.plusYears(scheduleAhead);
                }
            }
        }
        return toDt;
    }

    private void checkEventExclusion(ScheduleEvent event, String excludeModetype, ScheduleExclude exclude) throws ServiceException {
        Calendar eventdt = event.getEventDt();
        boolean isScheduleExcluded = false;
        isScheduleExcluded = exclude.isExcluded(eventdt);
        if (isScheduleExcluded) {
            if (excludeModetype.equals("S")) {
                event.setEventStatus("O");
            } else if (excludeModetype.equals("F")) {
                event.setEventStatus("F");
            } else if (excludeModetype.equals("N")) {
                eventdt.add(5, 1);
                event.setEventDt(eventdt);
                this.checkEventExclusion(event, excludeModetype, exclude);
            } else if (excludeModetype.equals("P")) {
                eventdt.add(5, -1);
                event.setEventDt(eventdt);
                this.checkEventExclusion(event, excludeModetype, exclude);
            }
        }
    }

    public void scheduleEvents(String schedulePlanId, String scheduleConditionId, String traceLogId) throws ServiceException {
        block13: {
            this.logInfo("Scheduling events for scheduleplan '" + schedulePlanId + "' and condition '" + scheduleConditionId + "'");
            DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
            try {
                int scheduleBackFillGracePerioid;
                int schedulePoll;
                try {
                    ConfigurationProcessor cp = new ConfigurationProcessor(this.getConnectionid());
                    schedulePoll = Integer.valueOf(cp.getSysConfigProperty("schedulepoll"));
                }
                catch (Exception e) {
                    schedulePoll = 3600;
                }
                try {
                    ConfigurationProcessor cp = new ConfigurationProcessor(this.getConnectionid());
                    scheduleBackFillGracePerioid = Integer.valueOf(cp.getSysConfigProperty("schedulebackfillgraceperiod"));
                }
                catch (Exception e) {
                    scheduleBackFillGracePerioid = 600;
                }
                db.setConnection(this.sapphireConnection);
                db.createPreparedResultSet("SELECT\tscheduleplanitem.scheduleplanid, scheduleplanitem.scheduleplanitemid, scheduleplanitem.propertytreeid, schedulecondition.scheduleconditionid, scheduletimerule.scheduletimeruleid, schedulecondition.startdt, \t\tscheduleplanitem.planitemstatus, scheduletimerule.schedulerule, scheduleevent.eventdt, scheduleevent.eventnum, scheduleplan.executeahead, scheduletimerule.executeahead timeruleexecahead, scheduleplan.backfillflag, scheduleplan.executeaheadunits, scheduletimerule.executeaheadunits timeruleexecaheadunits, scheduleplan.timezone FROM\tschedulecondition, scheduletimerule, scheduleplan, scheduleplanitem LEFT OUTER JOIN scheduleevent ON scheduleevent.scheduleplanid = scheduleplanitem.scheduleplanid AND scheduleevent.scheduleplanitemid = scheduleplanitem.scheduleplanitemid WHERE\tschedulecondition.scheduleplanid = scheduleplanitem.scheduleplanid AND scheduleplan.scheduleplanid = scheduleplanitem.scheduleplanid AND scheduletimerule.scheduleplanid = scheduleplanitem.scheduleplanid AND scheduletimerule.scheduletimeruleid = scheduleplanitem.scheduletimeruleid AND schedulecondition.scheduleconditionid = scheduleplanitem.scheduleconditionid AND scheduleplanitem.scheduleplanid = ? AND scheduleplanitem.scheduleconditionid = ? ORDER BY scheduletimerule.usersequence", new Object[]{schedulePlanId, scheduleConditionId});
                DataSet scheduleData = new DataSet(db.getResultSet());
                if (scheduleData.size() <= 0) break block13;
                String timeZoneStr = scheduleData.getString(0, "timezone", "");
                TimeZone timeZone = timeZoneStr.isEmpty() ? TimeZone.getDefault() : TimeZone.getTimeZone(timeZoneStr);
                Calendar startDt = scheduleData.getCalendar(0, "startdt");
                startDt.setTimeZone(timeZone);
                startDt.get(5);
                startDt.clear(13);
                String timeRule = " @ " + (startDt.get(11) < 10 ? "0" : "") + startDt.get(11) + ":" + (startDt.get(12) < 10 ? "0" : "") + startDt.get(12);
                ScheduleRule scheduleRule = new ScheduleRule();
                ArrayList events = new ArrayList();
                for (int i = 0; i < scheduleData.size(); ++i) {
                    if (!scheduleData.getString(i, "planitemstatus", "X").equals("A") || scheduleData.getCalendar(i, "eventdt") != null) continue;
                    ScheduleEvent scheduleEvent = new ScheduleEvent(scheduleData.getString(i, "scheduleplanid"), scheduleData.getString(i, "scheduleplanitemid"), Calendar.getInstance(), timeZone);
                    scheduleEvent.setEventStatus("S");
                    scheduleRule.setRule(scheduleData.getString(i, "schedulerule") + timeRule);
                    this.logInfo("Rule: " + scheduleData.getString(i, "schedulerule") + timeRule);
                    int executeAhead = scheduleData.getInt(i, "timeruleexecahead");
                    scheduleEvent.setExecuteaheadTime(executeAhead);
                    String executeAheadUnits = scheduleData.getString(i, "timeruleexecaheadunits");
                    scheduleEvent.setExecuteaheadTimeUnit(executeAheadUnits);
                    events.addAll(scheduleRule.getEvents(scheduleEvent, (Calendar)startDt.clone(), null, startDt, true));
                }
                Calendar toDt = Calendar.getInstance();
                int executeAhead = scheduleData.getInt(0, "executeahead");
                String executeAheadUnits = scheduleData.getString(0, "executeaheadunits");
                if (executeAheadUnits == null || executeAheadUnits.length() == 0 || executeAhead == -999999999) {
                    ConfigService config = new ConfigService(this.sapphireConnection);
                    try {
                        int value;
                        executeAhead = value = Integer.parseInt(config.getProfileProperty("(system)", "executeahead", "5"));
                        executeAheadUnits = config.getProfileProperty("(system)", "executeaheadunits", "Day");
                    }
                    catch (NumberFormatException value) {
                        // empty catch block
                    }
                }
                PreparedStatement insertPS = db.prepareStatement("insert", "INSERT INTO scheduleevent ( scheduleplanid, scheduleplanitemid, eventdt, eventnum, eventstatus, createdt, executedt, originaleventdt, createby, createtool, tracelogid, adhocflag ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
                Calendar lastEventDt = Calendar.getInstance();
                boolean backFillFlag = scheduleData.getString(0, "backfillflag", "Y").startsWith("Y");
                ScheduleExclude exclude = this.rakFile == null ? new ScheduleExclude(this.sapphireConnection, this.logger) : new ScheduleExclude(this.sapphireConnection, this.logger, this.rakFile);
                exclude.setSchedulePlanId(schedulePlanId);
                int newEvents = this.insertScheduleEvents(insertPS, events, startDt, null, scheduleData.getString(0, "scheduleexcludeid"), executeAhead, executeAheadUnits, traceLogId, lastEventDt, backFillFlag, schedulePoll, scheduleBackFillGracePerioid, db, exclude);
                this.logInfo("Generated " + newEvents + " new events for scheduleplanid = " + schedulePlanId + ", schedulecondition = " + scheduleConditionId);
            }
            catch (SapphireException e) {
                throw new ServiceException("SCHEDULE_SERVICE_FAILED", "Failed to schedule events", e);
            }
            finally {
                db.reset();
            }
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void deleteEvents(String schedulePlanId, String scheduleConditionId, String tracelogId) throws ServiceException {
        this.logInfo("Deleting events for scheduleplan '" + schedulePlanId + "' and condition '" + scheduleConditionId + "'");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            String schedPlanItemSDCId = "SchedulePlanItem";
            SDCProcessor sdcProcessor = new SDCProcessor(this.connectionInfo.getConnectionId());
            boolean isAuditTableExist = !sdcProcessor.getProperty(schedPlanItemSDCId, "auditedflag").equalsIgnoreCase("N");
            PreparedStatement delete = db.prepareStatement("delete", "DELETE FROM scheduleevent WHERE scheduleplanid = ? AND scheduleplanitemid = ? AND eventdt = ? AND eventnum = ?");
            db.createPreparedResultSet("SELECT scheduleevent.scheduleplanid, scheduleevent.scheduleplanitemid, scheduleevent.eventdt, scheduleevent.eventnum FROM\tscheduleevent, scheduleplanitem, schedulecondition WHERE\tscheduleplanitem.scheduleplanid = schedulecondition.scheduleplanid AND\tscheduleplanitem.scheduleplanid = scheduleevent.scheduleplanid AND\tscheduleplanitem.scheduleplanitemid = scheduleevent.scheduleplanitemid AND\tscheduleplanitem.scheduleconditionid = schedulecondition.scheduleconditionid AND\tscheduleevent.eventstatus = 'S' AND\tscheduleevent.scheduleplanid = ? AND\tschedulecondition.scheduleconditionid = ?", new Object[]{schedulePlanId, scheduleConditionId});
            StringBuffer updateAuditSql = new StringBuffer();
            if (isAuditTableExist) {
                updateAuditSql.append("UPDATE a_scheduleevent SET modby = '" + this.connectionInfo.getSysuserId() + "', modtool = 'DELETE', moddt = {ts '" + DateTimeUtil.getNowTimestamp() + "'}, tracelogid = '" + tracelogId + "'").append(" WHERE scheduleplanid = ? AND scheduleplanitemid = ? AND eventdt = ? AND eventnum = ? AND tracelogid = 'DELETED'  AND ").append(" auditsequence = ( SELECT max( auditsequence ) FROM a_scheduleevent WHERE scheduleplanid=? AND scheduleplanitemid = ? AND eventdt = ? AND eventnum = ? AND tracelogid = 'DELETED')");
            }
            while (db.getNext()) {
                String planid = db.getString("scheduleplanid");
                String planitemid = db.getString("scheduleplanitemid");
                Timestamp eventdt = db.getTimestamp("eventdt");
                BigDecimal eventnum = db.getBigDecimal("eventnum");
                delete.setString(1, planid);
                delete.setString(2, planitemid);
                delete.setTimestamp(3, eventdt);
                delete.setBigDecimal(4, eventnum);
                delete.executeUpdate();
                if (!isAuditTableExist || tracelogId == null || tracelogId.trim().length() <= 0) continue;
                try {
                    db.executePreparedUpdate(updateAuditSql.toString(), new Object[]{planid, planitemid, eventdt, eventnum, planid, planitemid, eventdt, eventnum});
                }
                catch (Exception ex) {
                    String errorMsg = ex.getMessage();
                    int errIndx = db.isOracle() ? errorMsg.indexOf("942") : errorMsg.indexOf("240");
                    if (errIndx >= 0) continue;
                    throw new SapphireException("DB_UPDATE_FAILED", ex);
                    return;
                }
            }
        }
        catch (Exception e) {
            throw new ServiceException("SCHEDULE_SERVICE_FAILED", "Failed to delete schedule events", e);
        }
        finally {
            db.reset();
        }
    }

    public DataSet getExecutePlanItems(Calendar refDt) throws ServiceException {
        this.logDebug("Getting execute plan items from " + DateFormatter.formatDateTime(refDt));
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            db.createPreparedResultSet("SELECT  spi.scheduleplanid, spi.scheduleplanitemid, spi.scheduleplanitemdesc, spi.propertytreeid, spi.scheduletasknodeid, spi.randomizationmode, spi.randomizationtype, spi.randomizationcount, pt.objectname FROM scheduleplanitem spi, propertytree pt WHERE spi.propertytreeid = pt.propertytreeid AND exists (select 1 from scheduleevent se where  se.scheduleplanid = spi.scheduleplanid AND se.scheduleplanitemid = spi.scheduleplanitemid AND se.executedt <= ? AND se.eventstatus = 'S') ORDER BY (select min(se2.eventdt) from scheduleevent se2 WHERE se2.scheduleplanid = spi.scheduleplanid and se2.scheduleplanitemid = spi.scheduleplanitemid and se2.eventstatus = 'S'), spi.scheduleplanid, spi.scheduleplanitemid", new Object[]{new Timestamp(refDt.getTime().getTime())});
            DataSet ds = new DataSet(db.getResultSet());
            if (ds.size() > 0) {
                this.logInfo(ds.size() + " distinct plan items found for execution");
            } else {
                this.logDebug("No plan items found for execution");
            }
            DataSet dataSet = ds;
            return dataSet;
        }
        catch (SapphireException e) {
            throw new ServiceException("SCHEDULE_SERVICE_FAILED", "Failed to get plan items for execution", e);
        }
        finally {
            db.reset();
        }
    }

    public int setPlanItemInProcessingFlag(String scheduleplanid, String scheduleplanitemid, String flag) throws ServiceException {
        int updateCount = 0;
        DBUtil database = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            database.setConnection(this.sapphireConnection);
            PreparedStatement updatePS = database.prepareStatement("UPDATE scheduleplanitemexec SET inprocessflag = ?  WHERE scheduleplanid = ?  AND scheduleplanitemid = ? " + ("Y".equals(flag) ? " AND (inprocessflag in ('N','') OR inprocessflag is null)" : ""));
            updatePS.setString(1, flag);
            updatePS.setString(2, scheduleplanid);
            updatePS.setString(3, scheduleplanitemid);
            updateCount = updatePS.executeUpdate();
            updatePS.close();
        }
        catch (Exception e) {
            throw new ServiceException("SCHEDULE_SERVICE_FAILED", "SchedulePlanItem processingflag update failed for scheduleplanid '" + scheduleplanid + "', scheduleplanitemid '" + scheduleplanitemid + "'", e);
        }
        finally {
            database.reset();
        }
        return updateCount;
    }

    private void disablePlanItem(DBUtil database, String scheduleplanid, String scheduleplanitemid) throws ServiceException {
        try {
            PreparedStatement updatePS = database.prepareStatement("UPDATE scheduleplanitem SET planitemstatus = ?  WHERE scheduleplanid = ?  AND scheduleplanitemid = ?");
            updatePS.setString(1, "X");
            updatePS.setString(2, scheduleplanid);
            updatePS.setString(3, scheduleplanitemid);
            updatePS.executeUpdate();
            updatePS.close();
        }
        catch (Exception e) {
            throw new ServiceException("SCHEDULE_SERVICE_FAILED", "SchedulePlanItem disabling failed for scheduleplanid '" + scheduleplanid + "', scheduleplanitemid '" + scheduleplanitemid + "'", e);
        }
    }

    public void executeEvents(String schedulePlanid, String schedulePlanitemid, String propertyTreeid, String scheduleTaskNodeid, String scheduleTaskObject, Calendar refDt) throws ServiceException {
        this.logInfo("Executing events for scheduleplan '" + schedulePlanid + "', scheduleplanitem '" + schedulePlanitemid + "' from " + DateFormatter.formatDateTime(refDt));
        ArrayList<ScheduleEvent> scheduleEvents = new ArrayList<ScheduleEvent>();
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            String where = "WHERE se.scheduleplanid = ?  AND se.scheduleplanitemid = ?  AND se.executedt <= ?  AND se.eventstatus = 'S'  AND spi.scheduleplanid = se.scheduleplanid AND spi.scheduleplanitemid = se.scheduleplanitemid  AND sp.scheduleplanid = spi.scheduleplanid ";
            db.createPreparedResultSet("SELECT se.eventdt, se.eventnum, spi.scheduleplanitemdesc, spi.linksdcid, spi.linkkeyid1, spi.linkkeyid2, spi.linkkeyid3, spi.scheduleconditionid, spi.scheduletemplatesdcid, spi.scheduletemplatekeyid1, spi.scheduletemplatekeyid2, spi.scheduletemplatekeyid3, spi.scheduletemplatenumcopies, spi.randomizationmode, spi.randomizationtype, spi.randomizationcount, sp.timezone FROM scheduleevent se, scheduleplanitem spi, scheduleplan sp " + where + " ORDER BY se.scheduleplanitemid, se.eventdt ", new Object[]{schedulePlanid, schedulePlanitemid, new Timestamp(refDt.getTime().getTime())});
            boolean isStabilityGrid = false;
            int nofEvents = 0;
            while (db.getNext()) {
                int randomizationCount;
                ++nofEvents;
                String timeZoneStr = db.getString("timezone");
                TimeZone timeZone = timeZoneStr != null && !timeZoneStr.isEmpty() ? TimeZone.getTimeZone(timeZoneStr) : TimeZone.getDefault();
                Calendar eventDt = Calendar.getInstance();
                eventDt.setTime(db.getTimestamp("eventdt"));
                ScheduleEvent event = new ScheduleEvent(schedulePlanid, schedulePlanitemid, eventDt, db.getString("linksdcid"), db.getString("linkkeyid1"), db.getString("linkkeyid2"), db.getString("linkkeyid3"), timeZone);
                event.setScheduleTemplateSdcId(db.getString("scheduletemplatesdcid"));
                event.setScheduleTemplateKeydId1(db.getString("scheduletemplatekeyid1"));
                event.setScheduleTemplateKeydId2(db.getString("scheduletemplatekeyid2"));
                event.setScheduleTemplateKeydId3(db.getString("scheduletemplatekeyid3"));
                event.setNumCopies(db.getString("scheduletemplatenumcopies"));
                event.setEventNum(db.getInt("eventnum"));
                event.setSchedulePlanItemDesc(db.getString("scheduleplanitemdesc"));
                String randomizationType = db.getValue("randomizationtype");
                String randomizationMode = db.getString("randomizationmode");
                if (randomizationMode == null || randomizationMode.isEmpty()) {
                    randomizationMode = "Source";
                }
                if ((randomizationCount = db.getInt("randomizationcount")) == 0) {
                    randomizationCount = 1;
                }
                if (!(randomizationType.isEmpty() || randomizationMode.isEmpty() || randomizationType.equals(RANDOMIZETYPE_NONE))) {
                    this.randomizeEvents(scheduleEvents, schedulePlanid, schedulePlanitemid, randomizationMode, randomizationType, randomizationCount, event);
                } else {
                    scheduleEvents.add(event);
                }
                isStabilityGrid = db.getString("scheduleconditionid") != null && db.getString("scheduleconditionid").length() > 0;
            }
            if (scheduleEvents.size() > 0) {
                PropertyList taskNodeProps = this.getPlanItemProperties(propertyTreeid, scheduleTaskNodeid, schedulePlanid, schedulePlanitemid, isStabilityGrid);
                this.executeTask(scheduleTaskObject, taskNodeProps, scheduleEvents, "N", true, null, null, null);
                int updateCount = this.setScheduleEventStatus(schedulePlanid, schedulePlanitemid, refDt, "D");
                if (updateCount != nofEvents) {
                    throw new SapphireException("ScheduleEvent update count (" + updateCount + ") did not match events retrieved (" + nofEvents + ") for scheduleplanid '" + schedulePlanid + "', scheduleplanitemid '" + schedulePlanitemid + "'");
                }
            }
        }
        catch (SapphireException e) {
            throw new ServiceException("SCHEDULE_SERVICE_FAILED", "Failed to execute events", e);
        }
        finally {
            db.reset();
        }
    }

    private void randomizeEvents(ArrayList<ScheduleEvent> scheduleEvents, String schedulePlanid, String schedulePlanitemid, String randomizationMode, String randomizationType, int randomizationCount, ScheduleEvent event) throws ServiceException {
        block21: {
            String sql = "select schedulerandomizationid, randomizationsdcid, randomizationkeyid1, randomizationkeyid2, randomizationkeyid3, coalesce(executedflag, 'N') AS executedflag  from schedulerandomization where scheduleplanid = ? and scheduleplanitemid = ?";
            Object[] params = new String[]{schedulePlanid, schedulePlanitemid};
            DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
            db.setConnection(this.sapphireConnection);
            Timestamp now = DateTimeUtil.getNowTimestamp();
            String modBy = this.sapphireConnection.getSysuserId();
            String modTool = this.sapphireConnection.getTool();
            try {
                db.createPreparedResultSet(sql, params);
                DataSet ds = new DataSet(db.getResultSet());
                int numberOfRandomizedItems = ds.getRowCount();
                if (numberOfRandomizedItems > 0) {
                    DataSet randomDs;
                    Random randomGenerator = new Random();
                    HashMap<String, String> updateItems = new HashMap<String, String>();
                    if (randomizationType.equals(RANDOMIZETYPE_ROLLING)) {
                        HashMap<String, String> filterMap = new HashMap<String, String>();
                        filterMap.put("executedflag", "N");
                        DataSet filterDs = ds.getFilteredDataSet(filterMap);
                        randomDs = filterDs.copy();
                        if (randomDs.getRowCount() == 0) {
                            randomDs = ds.copy();
                            for (int j = 0; j < randomDs.getRowCount(); ++j) {
                                String schedulerandomizationid = randomDs.getString(j, "schedulerandomizationid");
                                updateItems.put(schedulerandomizationid, "N");
                            }
                        }
                    } else {
                        randomDs = ds.copy();
                    }
                    for (int i = 0; i < randomizationCount; ++i) {
                        int randomDsRowCount = randomDs.getRowCount();
                        int nextId = randomGenerator.nextInt(randomDsRowCount);
                        ScheduleEvent event2 = event.clone(event.getEventDt());
                        String sdcid = randomDs.getString(nextId, "randomizationsdcid");
                        String keyid1 = randomDs.getString(nextId, "randomizationkeyid1");
                        String keyid2 = randomDs.getString(nextId, "randomizationkeyid2");
                        String keyid3 = randomDs.getString(nextId, "randomizationkeyid3");
                        if (randomizationMode.equals("Source")) {
                            event2.setLinkSdcid(sdcid);
                            event2.setLinkKeyid1(keyid1);
                            event2.setLinkKeyid2(keyid2);
                            event2.setLinkKeyid3(keyid3);
                        } else if (randomizationMode.equals(RANDOMIZEMODE_TEMPLATE)) {
                            event2.setScheduleTemplateSdcId(sdcid);
                            event2.setScheduleTemplateKeydId1(keyid1);
                            event2.setScheduleTemplateKeydId2(keyid2);
                            event2.setScheduleTemplateKeydId3(keyid3);
                        }
                        scheduleEvents.add(event2);
                        if (randomizationType.equals(RANDOMIZETYPE_ROLLING)) {
                            String schedulerandomizationid = randomDs.getString(nextId, "schedulerandomizationid");
                            updateItems.put(schedulerandomizationid, "Y");
                        }
                        randomDs.remove(nextId);
                        if (randomDs.getRowCount() != 0 || i + 1 >= randomizationCount) continue;
                        randomDs = ds.copy();
                        if (!randomizationType.equals(RANDOMIZETYPE_ROLLING)) continue;
                        if (randomizationCount <= numberOfRandomizedItems) {
                            for (String randomizationId : updateItems.keySet()) {
                                HashMap<String, String> findMap = new HashMap<String, String>();
                                findMap.put("schedulerandomizationid", randomizationId);
                                int rowNum = randomDs.findRow(findMap);
                                randomDs.remove(rowNum);
                            }
                        }
                        for (int j = 0; j < ds.getRowCount(); ++j) {
                            String schedulerandomizationid = ds.getString(j, "schedulerandomizationid");
                            updateItems.put(schedulerandomizationid, "N");
                        }
                    }
                    PreparedStatement updatePS = db.prepareStatement("UPDATE schedulerandomization SET executedflag = ?, moddt = ?, modby = ?, modtool = ?  WHERE scheduleplanid = ?  AND scheduleplanitemid = ?  AND schedulerandomizationid = ? ");
                    for (Map.Entry entry : updateItems.entrySet()) {
                        try {
                            String executedFlag = (String)entry.getValue();
                            updatePS.setString(1, executedFlag);
                            updatePS.setTimestamp(2, now);
                            updatePS.setString(3, modBy);
                            updatePS.setString(4, modTool);
                            updatePS.setString(5, schedulePlanid);
                            updatePS.setString(6, schedulePlanitemid);
                            updatePS.setString(7, (String)entry.getKey());
                            updatePS.executeUpdate();
                        }
                        catch (SQLException e) {
                            throw new SapphireException("Could not update randomization executedflag", e);
                        }
                    }
                    break block21;
                }
                throw new SapphireException("SCHEDULE_SERVICE_FAILED", "Schedule Plan: " + event.getSchedulePlanid() + " Schedule Plan Item:" + event.getSchedulePlanitemid() + " is randomized but has nothing defined in schedulerandomization detail");
            }
            catch (SapphireException e) {
                throw new ServiceException("SCHEDULE_SERVICE_FAILED", "Failed to randomize events", e);
            }
            finally {
                db.reset();
            }
        }
    }

    private PropertyList getPlanItemProperties(String propertyTreeid, String scheduleTaskNodeid, String planid, String planitemid, boolean isStabilityGrid) throws ServiceException {
        PropertyList planitemPropertylist = null;
        if (isStabilityGrid) {
            try {
                PlanItem planItem;
                ScheduleGrid grid = (ScheduleGrid)CacheUtil.get(this.sapphireConnection.getDatabaseId(), "ScheduleGrid", planid);
                if (grid == null) {
                    grid = new ScheduleGrid(this.connectionInfo.getConnectionId());
                    grid.retrieve(planid);
                    CacheUtil.put(this.sapphireConnection.getDatabaseId(), "ScheduleGrid", planid, grid);
                }
                if ((planItem = grid.planItems.findById(planitemid)) == null) {
                    throw new ServiceException("SCHEDULE_SERVICE_FAILED", "Failed to locate planitem: " + planitemid + " in plan " + planid);
                }
                planitemPropertylist = planItem.getCollapsedPropertyList();
            }
            catch (SapphireException e) {
                throw new ServiceException("SCHEDULE_SERVICE_FAILED", "Failed to load schedulegrid: " + planid, e);
            }
        }
        planitemPropertylist = (PropertyList)CacheUtil.get(this.sapphireConnection.getDatabaseId(), "ScheduleTaskNodeProperties", propertyTreeid + "_" + scheduleTaskNodeid);
        String taskPropertyTree = (String)CacheUtil.get(this.sapphireConnection.getDatabaseId(), "ScheduleTaskPropertyTree", propertyTreeid);
        WebAdminProcessor wp = new WebAdminProcessor(this.sapphireConnection.getConnectionId());
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            PropertyDefinitionList propertyDefinitionList = wp.getPropertyDefinitionList(propertyTreeid);
            planitemPropertylist = new PropertyList();
            db.createPreparedResultSet("SELECT valuetree FROM propertytree WHERE propertytreeid = ?", propertyTreeid);
            if (db.getNext()) {
                taskPropertyTree = db.getClob("valuetree");
                planitemPropertylist.setPropertyTree(taskPropertyTree, scheduleTaskNodeid, propertyDefinitionList);
                CacheUtil.put(this.sapphireConnection.getDatabaseId(), "ScheduleTaskNodeProperties", propertyTreeid + "_" + scheduleTaskNodeid, planitemPropertylist);
            }
            planitemPropertylist.addPropertyList(DOMUtil.getNewDocument(this.getTaskPropsOverride(planid, planitemid), false).getFirstChild(), true, "");
            planitemPropertylist.setPropertyTreeDefaults(taskPropertyTree, propertyDefinitionList);
        }
        catch (Exception e) {
            throw new ServiceException("SCHEDULE_SERVICE_FAILED", "Failed to get task node properties", e);
        }
        finally {
            db.reset();
        }
        return planitemPropertylist;
    }

    public String getTaskPropsOverride(String scheduleplanid, String scheduleplanitemid) throws ServiceException {
        this.logInfo("Getting schedule task property overrides for scheduleplan '" + scheduleplanid + "', scheduleplanitem '" + scheduleplanitemid + "'");
        String ptree = null;
        DBUtil database = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            database.setConnection(this.sapphireConnection);
            String sql = "SELECT valuetree FROM scheduleplanitem WHERE scheduleplanid= ? and scheduleplanitemid= ?";
            database.createPreparedResultSet(sql, new Object[]{scheduleplanid, scheduleplanitemid});
            if (database.getNext()) {
                ptree = database.getClob("valuetree");
            }
            database.closeResultSet();
        }
        catch (SapphireException e) {
            throw new ServiceException("SCHEDULE_SERVICE_FAILED", "Failed to get task property overrides", e);
        }
        finally {
            database.reset();
        }
        return ptree == null || ptree.trim().length() == 0 ? "<propertylist/>" : ptree;
    }

    private void executeTask(String scheduleTask, PropertyList scheduleProps, ArrayList scheduleEvents, String adhocFlag, boolean createMonitorGroup, String monitorGroupId, Integer instanceCount, HashMap<String, ArrayList<String>> newKeyidMap) throws ServiceException {
        try {
            BaseScheduleTask task = (BaseScheduleTask)Class.forName(scheduleTask).newInstance();
            task.startAction("", this.sapphireConnection, new ErrorHandler(), false);
            this.logInfo("Executing schedule task " + scheduleTask + " with " + scheduleEvents.size() + " schedule event(s)");
            task.setScheduleEvents(scheduleEvents);
            task.setScheduleProperties(scheduleProps);
            task.setAdhocFlag(adhocFlag);
            task.setCreateMonitorGroup(createMonitorGroup);
            task.setMonitorGroupId(monitorGroupId);
            task.setInstanceCount(instanceCount);
            task.setNewKeyidsMap(newKeyidMap);
            task.execute();
            task.endAction();
        }
        catch (Exception e) {
            throw new ServiceException("SCHEDULE_SERVICE_FAILED", "Failed to execute task", e);
        }
    }

    private int setScheduleEventStatus(String schedulePlanid, String schedulePlanitemid, Calendar refDt, String status) throws ServiceException {
        int events;
        DBUtil database = new DBUtil(this.sapphireConnection.getConnectionId());
        Timestamp now = DateTimeUtil.getNowTimestamp();
        String currentUser = this.sapphireConnection.getSysuserId();
        String tool = this.sapphireConnection.getTool();
        try {
            database.setConnection(this.sapphireConnection);
            PreparedStatement updatePS = database.prepareStatement("UPDATE scheduleevent SET eventstatus = ?, moddt = ?, modby = ?, modtool = ?  WHERE scheduleplanid = ?  AND scheduleplanitemid = ?  AND executedt <= ?  AND eventstatus = 'S' ");
            updatePS.setString(1, status);
            updatePS.setTimestamp(2, now);
            updatePS.setString(3, currentUser);
            updatePS.setString(4, tool);
            updatePS.setString(5, schedulePlanid);
            updatePS.setString(6, schedulePlanitemid);
            updatePS.setTimestamp(7, new Timestamp(refDt.getTime().getTime()));
            events = updatePS.executeUpdate();
            updatePS.close();
        }
        catch (Exception e) {
            throw new ServiceException("SCHEDULE_SERVICE_FAILED", "ScheduleEvent update failed for scheduleplanid '" + schedulePlanid + "', scheduleplanitemid '" + schedulePlanitemid + "'", e);
        }
        finally {
            database.reset();
        }
        return events;
    }

    public void executeNow(String schedulePlanid, String schedulePlanitemid, String propertyTreeid, String scheduleTaskNodeid, String scheduleTaskObject, boolean noEvent, String eventdt, String eventtodate, String eventNum, String eventStatus, boolean adhocExecution, boolean createMonitorGroup, String monitorGroupId, Integer instanceCount, HashMap<String, ArrayList<String>> newKeyidMap) throws ServiceException {
        block27: {
            this.logInfo("Executing tasks for scheduleplan '" + schedulePlanid + "', scheduleplanitem '" + schedulePlanitemid + "' between '" + eventdt + "' and '" + eventtodate + "'");
            DateTimeUtil dtu = new DateTimeUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
            ArrayList<ScheduleEvent> scheduleEvents = new ArrayList<ScheduleEvent>();
            Timestamp now = DateTimeUtil.getNowTimestamp();
            String currentUser = this.sapphireConnection.getSysuserId();
            String tool = this.sapphireConnection.getTool();
            DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
            try {
                String where;
                db.setConnection(this.sapphireConnection);
                if (noEvent) {
                    where = "WHERE scheduleplanitem.scheduleplanid = ?  AND scheduleplanitem.scheduleplanitemid = ? AND scheduleplan.scheduleplanid = scheduleplanitem.scheduleplanid ";
                    db.createPreparedResultSet("SELECT scheduleplanitemdesc, linksdcid, linkkeyid1, linkkeyid2, linkkeyid3, scheduleconditionid, scheduleplanitem.scheduletemplatesdcid, scheduleplanitem.scheduletemplatekeyid1, scheduleplanitem.scheduletemplatekeyid2, scheduleplanitem.scheduletemplatekeyid3, scheduleplanitem.scheduletemplatenumcopies, scheduleplanitem.randomizationmode, scheduleplanitem.randomizationtype, scheduleplanitem.randomizationcount, scheduleplan.timezone FROM scheduleplanitem, scheduleplan " + where + " ORDER BY scheduleplanitemid ", new Object[]{schedulePlanid, schedulePlanitemid});
                } else {
                    where = "WHERE se.scheduleplanid = ?  AND se.scheduleplanitemid = ?  AND se.eventdt >= ?  AND se.eventdt <= ?  AND se.eventstatus = ?  AND spi.scheduleplanid = se.scheduleplanid AND spi.scheduleplanitemid = se.scheduleplanitemid  AND spi.scheduleplanid = sp.scheduleplanid ";
                    ArrayList<Object> params = new ArrayList<Object>();
                    params.add(schedulePlanid);
                    params.add(schedulePlanitemid);
                    params.add(new Timestamp(dtu.getCalendar(eventdt).getTime().getTime()));
                    params.add(new Timestamp(dtu.getCalendar(eventtodate).getTime().getTime()));
                    params.add(eventStatus);
                    if (eventNum != null && !eventNum.isEmpty()) {
                        where = where + " AND se.eventnum = ?";
                        params.add(eventNum);
                    }
                    db.createPreparedResultSet("SELECT se.eventdt, se.eventnum, se.adhocflag, spi.scheduleplanitemdesc, spi.linksdcid, spi.linkkeyid1, spi.linkkeyid2, spi.linkkeyid3, spi.scheduleconditionid, spi.scheduletemplatesdcid, spi.scheduletemplatekeyid1, spi.scheduletemplatekeyid2, spi.scheduletemplatekeyid3, spi.scheduletemplatenumcopies, spi.randomizationmode, spi.randomizationtype, spi.randomizationcount, sp.timezone FROM scheduleevent se, scheduleplanitem spi, scheduleplan sp " + where + " ORDER BY se.scheduleplanitemid, se.eventdt ", params.toArray());
                }
                boolean isStabilityGrid = false;
                while (db.getNext()) {
                    int randomizationCount;
                    Calendar eventDt = Calendar.getInstance();
                    if (noEvent) {
                        eventDt = dtu.getCalendar(eventdt);
                    } else {
                        eventDt.setTime(db.getTimestamp("eventdt"));
                    }
                    String timeZoneStr = db.getString("timezone");
                    TimeZone timeZone = timeZoneStr != null && !timeZoneStr.isEmpty() ? TimeZone.getTimeZone(timeZoneStr) : TimeZone.getDefault();
                    ScheduleEvent event = new ScheduleEvent(schedulePlanid, schedulePlanitemid, eventDt, db.getString("linksdcid"), db.getString("linkkeyid1"), db.getString("linkkeyid2"), db.getString("linkkeyid3"), timeZone);
                    event.setScheduleTemplateSdcId(db.getString("scheduletemplatesdcid"));
                    event.setScheduleTemplateKeydId1(db.getString("scheduletemplatekeyid1"));
                    event.setScheduleTemplateKeydId2(db.getString("scheduletemplatekeyid2"));
                    event.setScheduleTemplateKeydId3(db.getString("scheduletemplatekeyid3"));
                    event.setNumCopies(db.getString("scheduletemplatenumcopies"));
                    event.setSchedulePlanItemDesc(db.getString("scheduleplanitemdesc"));
                    if (noEvent) {
                        db.createPreparedResultSet("select", "SELECT scheduleplanid, scheduleplanitemid, eventdt, eventnum from scheduleevent where scheduleplanid=? AND scheduleplanitemid=? AND eventdt=? order by eventnum desc", new Object[]{event.getSchedulePlanid(), event.getSchedulePlanitemid(), new Timestamp(event.getEventDt().getTime().getTime())});
                        int eventnum = 1;
                        if (db.getNext("select")) {
                            eventnum = db.getInt("select", "eventnum") + 1;
                        }
                        event.setEventNum(eventnum);
                    } else {
                        event.setEventNum(db.getInt("eventnum"));
                    }
                    boolean eventAdhocFlag = false;
                    if (!noEvent) {
                        eventAdhocFlag = db.getValue("adhocflag").startsWith("Y");
                    }
                    if (eventAdhocFlag || adhocExecution) {
                        event.setAdHocFlag(true);
                    }
                    String randomizationType = db.getValue("randomizationtype");
                    String randomizationMode = db.getValue("randomizationmode");
                    if (randomizationMode == null || randomizationMode.isEmpty()) {
                        randomizationMode = "Source";
                    }
                    if ((randomizationCount = db.getInt("randomizationcount")) == 0) {
                        randomizationCount = 1;
                    }
                    if (!(randomizationType.isEmpty() || randomizationMode.isEmpty() || randomizationMode.equals(RANDOMIZETYPE_NONE))) {
                        this.randomizeEvents(scheduleEvents, schedulePlanid, schedulePlanitemid, randomizationMode, randomizationType, randomizationCount, event);
                    } else {
                        scheduleEvents.add(event);
                    }
                    isStabilityGrid = db.getString("scheduleconditionid") != null && db.getString("scheduleconditionid").length() > 0;
                }
                if (scheduleEvents.size() > 0) {
                    PropertyList planitemProps = this.getPlanItemProperties(propertyTreeid, scheduleTaskNodeid, schedulePlanid, schedulePlanitemid, isStabilityGrid);
                    String adhocFlag = adhocExecution ? "Y" : "N";
                    this.executeTask(scheduleTaskObject, planitemProps, scheduleEvents, adhocFlag, createMonitorGroup, monitorGroupId, instanceCount, newKeyidMap);
                }
                if (!noEvent) {
                    int updateCount;
                    try {
                        PreparedStatement updatePS = db.prepareStatement("UPDATE scheduleevent SET eventstatus = ?, moddt = ?, modby = ?, modtool = ?  WHERE scheduleplanid = ?  AND scheduleplanitemid = ?  AND eventdt >= ?  AND eventdt <= ?  AND eventstatus = ? ");
                        updatePS.setString(1, "D");
                        updatePS.setTimestamp(2, now);
                        updatePS.setString(3, currentUser);
                        updatePS.setString(4, tool);
                        updatePS.setString(5, schedulePlanid);
                        updatePS.setString(6, schedulePlanitemid);
                        updatePS.setTimestamp(7, new Timestamp(dtu.getCalendar(eventdt).getTime().getTime()));
                        updatePS.setTimestamp(8, new Timestamp(dtu.getCalendar(eventtodate).getTime().getTime()));
                        updatePS.setString(9, eventStatus);
                        updateCount = updatePS.executeUpdate();
                    }
                    catch (SQLException sqle) {
                        throw new SapphireException("ScheduleEvent update failed for scheduleplanid '" + schedulePlanid + "', scheduleplanitemid '" + schedulePlanitemid + "'. Exception: " + sqle.getMessage());
                    }
                    if (updateCount != scheduleEvents.size()) {
                        throw new SapphireException("ScheduleEvent update count (" + updateCount + ") did not match events retrieved (" + scheduleEvents.size() + ") for scheduleplanid '" + schedulePlanid + "', scheduleplanitemid '" + schedulePlanitemid + "'");
                    }
                    break block27;
                }
                for (int i = 0; i < scheduleEvents.size(); ++i) {
                    ScheduleEvent event = (ScheduleEvent)scheduleEvents.get(i);
                    if (adhocExecution) {
                        event.setAdHocFlag(true);
                    }
                    event.setEventStatus("D");
                    this.addScheduledEvent(event, true);
                }
            }
            catch (SapphireException e) {
                throw new ServiceException("SCHEDULE_SERVICE_FAILED", "Failed to execute schedule", e);
            }
            finally {
                db.reset();
            }
        }
    }

    public void setScheduleEventError(String schedulePlanid, String schedulePlanitemid, Calendar refDt) throws ServiceException {
        this.logInfo("Setting schedule event error 'E' for scheduleplan '" + schedulePlanid + "', scheduleplanitem '" + schedulePlanitemid + "' for " + DateFormatter.formatDateTime(refDt));
        this.setScheduleEventStatus(schedulePlanid, schedulePlanitemid, refDt, "E");
    }

    public void scheduleTasks() throws ServiceException {
        this.logDebug("Scheduling tasks");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            Timestamp ts = DateTimeUtil.getNowTimestamp();
            AutomationService automationService = new AutomationService(this.sapphireConnection);
            db.createPreparedResultSet("SELECT taskid, actionid, schedulerule, deletetaskflag, scheduledt, scheduledevents, planneddt, tasktypeflag, processexclusiveflag, backfillflag FROM task WHERE activeflag='Y' AND tasktypeflag IN ('P','R') AND templateflag = 'N' AND scheduledt <= ?", ts);
            while (db.getNext()) {
                String updatetask;
                boolean taskfinished;
                this.logInfo("Processing the task: '" + db.getString("taskid") + "'");
                if (db.getString("tasktypeflag").toUpperCase().equals("P")) {
                    PropertyList properties = this.getTaskProperties(db.getString("taskid"));
                    taskfinished = true;
                    updatetask = "UPDATE task SET activeflag = 'N', scheduledevents = 0, scheduledt = null, planneddt = null, tasktypeflag = 'N',  moddt = {ts '" + ts.toString() + "'}, modtool = '" + this.connectionInfo.getTool() + "', modby = '" + this.connectionInfo.getSysuserId() + "'  WHERE taskid = '" + db.getString("taskid") + "'";
                    String processassysuserid = properties.getProperty("processassysuserid");
                    this.logInfo("Adding to the todolist the one of task: " + db.getString("actionid") + " due the " + db.getString("scheduledt") + " process as " + processassysuserid);
                    automationService.addToDoListEntry(db.getString("taskid"), db.getString("actionid"), "1", properties, db.getString("scheduledt"), true, processassysuserid, "Y".equals(db.getString("processexclusiveflag")) ? db.getString("taskid") : "", "", "");
                } else {
                    BooleanHolder taskfinishedholder = new BooleanHolder();
                    StringHolder updatetaskholder = new StringHolder();
                    this.processRecurring(automationService, db, ts, updatetaskholder, taskfinishedholder);
                    updatetask = updatetaskholder.value;
                    taskfinished = taskfinishedholder.value;
                }
                if (taskfinished && db.getString("deletetaskflag") != null && db.getString("deletetaskflag").toUpperCase().equals("Y")) {
                    db.executePreparedUpdate("DELETE FROM taskproperty WHERE taskid = ?", new Object[]{db.getString("taskid")});
                    updatetask = "DELETE FROM task WHERE taskid = '" + db.getString("taskid") + "'";
                }
                this.logInfo("Executing: " + updatetask);
                db.executeSQL(updatetask);
            }
        }
        catch (SapphireException se) {
            throw new ServiceException("Failed to execute the list of active tasks", se);
        }
        finally {
            db.reset();
        }
    }

    private PropertyList getTaskProperties(String taskid) throws ServiceException {
        PropertyList propertyList = new PropertyList();
        this.logInfo("Retrieving the task properties of '" + taskid + "'");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            db.createPreparedResultSet("SELECT propertyid, propertyvalue FROM taskproperty WHERE taskid = ?", taskid);
            while (db.getNext()) {
                if (db.getString("propertyvalue") == null) continue;
                propertyList.setProperty(db.getString("propertyid"), db.getString("propertyvalue"));
            }
            db.closeResultSet();
        }
        catch (SapphireException se) {
            throw new ServiceException("Error retrieving task properties", se);
        }
        finally {
            db.reset();
        }
        return propertyList;
    }

    private void processRecurring(AutomationService automationService, DBUtil db, Timestamp now, StringHolder updatetaskholder, BooleanHolder taskfinishedholder) throws ServiceException {
        this.logInfo("Parsing a recurring rule.");
        boolean taskfinished = true;
        ArrayList schedule = null;
        try {
            String updatetask = "UPDATE task SET activeflag = 'N', scheduledevents = 0, scheduledt = null, planneddt = null, tasktypeflag = 'N',  moddt = {ts '" + now.toString() + "'}, modtool = '" + this.connectionInfo.getTool() + "', modby = '" + this.connectionInfo.getSysuserId() + "'  WHERE taskid = '" + db.getString("taskid") + "'";
            String rule = db.getString("schedulerule");
            String[] ruleparts = this.parseRule(db.getString("schedulerule"));
            switch (rule.toUpperCase().charAt(0)) {
                case 'T': {
                    schedule = this.parseTime(ruleparts, db.getTimestamp("scheduledt"), db.getInt("scheduledevents"), now);
                    break;
                }
                case 'D': {
                    schedule = this.parseDay(ruleparts, db.getTimestamp("scheduledt"), now);
                    break;
                }
                case 'W': {
                    schedule = this.parseWeek(ruleparts, db.getTimestamp("scheduledt"), now);
                    break;
                }
                case 'M': {
                    schedule = this.parseMonth(ruleparts, db.getTimestamp("scheduledt"), now);
                    break;
                }
                case 'Y': {
                    schedule = this.parseYear(ruleparts, db.getTimestamp("scheduledt"), now);
                }
            }
            PropertyList properties = this.getTaskProperties(db.getString("taskid"));
            if (schedule.size() > 0) {
                SimpleDateFormat sdf;
                Timestamp schedulets = new Timestamp(((Calendar)schedule.get(schedule.size() - 1)).getTime().getTime());
                boolean backFill = "Y".equals(db.getString("backfillflag"));
                for (int i = 0; i < schedule.size() - 1; ++i) {
                    sdf = new SimpleDateFormat();
                    sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
                    Calendar duedt = (Calendar)schedule.get(i);
                    String duedatestring = sdf.format(duedt.getTime());
                    if (backFill || duedt.after(now) || i == schedule.size() - 2) {
                        String processassysuserid = properties.getProperty("processassysuserid");
                        this.logInfo("Adding to the todolist: " + db.getString("actionid") + " at: " + db.getString("scheduledt") + " process as " + processassysuserid);
                        automationService.addToDoListEntry(db.getString("taskid"), db.getString("actionid"), "1", properties, duedatestring, true, processassysuserid, "Y".equals(db.getString("processexclusiveflag")) ? db.getString("taskid") : "", "", "");
                        continue;
                    }
                    this.logInfo("No back fill. Skip adding to the todolist: " + db.getString("actionid") + " at: " + duedatestring);
                }
                switch (ruleparts[3].charAt(0)) {
                    case 'N': {
                        updatetask = "UPDATE task SET scheduledt = {ts '" + schedulets.toString() + "'},  moddt = {ts '" + now.toString() + "'}, modtool = '" + this.connectionInfo.getTool() + "', modby = '" + this.connectionInfo.getSysuserId() + "'  WHERE taskid = '" + db.getString("taskid") + "'";
                        taskfinished = false;
                        break;
                    }
                    case 'O': {
                        int eventsleft = Integer.parseInt(ruleparts[3].substring(1)) - db.getInt("scheduledevents");
                        if (schedule.size() >= eventsleft + 1) break;
                        int eventsdone = db.getInt("scheduledevents") + schedule.size() - 1;
                        updatetask = "UPDATE task SET scheduledt = {ts '" + schedulets.toString() + "'}, scheduledevents = " + String.valueOf(eventsdone) + ",  moddt = {ts '" + now.toString() + "'}, modtool = '" + this.connectionInfo.getTool() + "', modby = '" + this.connectionInfo.getSysuserId() + "'  WHERE taskid = '" + db.getString("taskid") + "'";
                        taskfinished = false;
                        break;
                    }
                    case 'D': {
                        sdf = new SimpleDateFormat();
                        sdf.applyPattern("MM/dd/yyyy");
                        try {
                            sdf.parse(ruleparts[3].substring(1));
                            Calendar enddate = sdf.getCalendar();
                            taskfinished = enddate.before(schedule.get(schedule.size() - 1));
                            if (taskfinished) break;
                            updatetask = "UPDATE task SET scheduledt = {ts '" + schedulets.toString() + "'},  moddt = {ts '" + now.toString() + "'}, modtool = '" + this.connectionInfo.getTool() + "', modby = '" + this.connectionInfo.getSysuserId() + "'  WHERE taskid = '" + db.getString("taskid") + "'";
                            break;
                        }
                        catch (Exception e) {
                            throw new ServiceException("Could not parse the date from the rule", e);
                        }
                    }
                }
            }
            updatetaskholder.value = updatetask;
            taskfinishedholder.value = taskfinished;
        }
        catch (SapphireException se) {
            throw new ServiceException("Database access error processing recurring tasks.", se);
        }
    }

    private ArrayList parseTime(String[] ruleparts, Timestamp scheduledt, int eventsprocessed, Timestamp now) throws ServiceException {
        this.logInfo("Parsing a time based rule");
        ArrayList<Calendar> schedule = new ArrayList<Calendar>();
        int hours = Integer.parseInt(ruleparts[0].substring(1, 3));
        int minutes = Integer.parseInt(ruleparts[0].substring(4, 6));
        int seconds = Integer.parseInt(ruleparts[0].substring(7, 9));
        int secondsincrement = hours * 3600 + minutes * 60 + seconds;
        int nbevents = 0;
        switch (ruleparts[3].charAt(0)) {
            case 'N': {
                nbevents = this.getNbEvents(scheduledt, now, secondsincrement);
                this.logInfo("scheduling: " + nbevents + " events.");
                break;
            }
            case 'O': {
                int eventstoprocess = Integer.parseInt(ruleparts[3].substring(1)) - eventsprocessed;
                nbevents = this.getNbEvents(scheduledt, now, secondsincrement);
                nbevents = Math.min(nbevents, eventstoprocess);
                this.logInfo("scheduling: " + nbevents + " events.");
                break;
            }
            case 'D': {
                Timestamp enddatets = this.getEndDate(ruleparts);
                if (enddatets == null) break;
                nbevents = enddatets.after(now) ? this.getNbEvents(scheduledt, now, secondsincrement) : this.getNbEvents(scheduledt, enddatets, secondsincrement);
            }
        }
        Date tempdate = new Date();
        tempdate.setTime(scheduledt.getTime());
        Calendar startdate = Calendar.getInstance();
        startdate.setTime(tempdate);
        for (int i = 0; i <= nbevents; ++i) {
            Calendar nextscheduledate = (Calendar)startdate.clone();
            nextscheduledate.add(13, secondsincrement * i);
            schedule.add(nextscheduledate);
        }
        return schedule;
    }

    private String[] parseRule(String rule) {
        String[] ruleparts = new String[4];
        StringTokenizer st = new StringTokenizer(rule, ";");
        int i = 0;
        while (st.hasMoreTokens()) {
            ruleparts[i++] = st.nextToken();
        }
        return ruleparts;
    }

    private int getNbEvents(Timestamp startdate, Timestamp todate, int increment) {
        int events = 0;
        if (todate != null && startdate != null) {
            long secondsfrom;
            long secondsto = todate.getTime() / 1000L;
            events = secondsto < (secondsfrom = startdate.getTime() / 1000L) ? 0 : (int)((secondsto - secondsfrom) / (long)increment + 1L);
        }
        this.logInfo("found: " + events + " events.");
        return events;
    }

    private ArrayList parseDay(String[] ruleparts, Timestamp scheduledt, Timestamp now) throws ServiceException {
        this.logInfo("Parsing a day based rule");
        ArrayList<Calendar> schedule = new ArrayList<Calendar>();
        Date tempdate = new Date();
        tempdate.setTime(scheduledt.getTime());
        Calendar startdate = Calendar.getInstance();
        startdate.setTime(tempdate);
        int secondsincrement = 86400;
        int frequency = 0;
        if (ruleparts[0].charAt(1) == 'D') {
            frequency = Integer.parseInt(ruleparts[0].substring(2));
        }
        int nbevents = 0;
        if (ruleparts[3].charAt(0) == 'D') {
            Timestamp enddatets = this.getEndDate(ruleparts);
            if (enddatets != null) {
                nbevents = enddatets.after(now) ? this.getNbEvents(scheduledt, now, secondsincrement) : this.getNbEvents(scheduledt, enddatets, secondsincrement);
            }
        } else {
            nbevents = this.getNbEvents(scheduledt, now, secondsincrement);
        }
        Calendar lastscheduledate = (Calendar)startdate.clone();
        for (int i = 0; i < nbevents; ++i) {
            Calendar nextscheduledate = (Calendar)startdate.clone();
            nextscheduledate.add(5, i);
            boolean selected = false;
            switch (ruleparts[0].charAt(1)) {
                case 'A': {
                    selected = nextscheduledate.get(7) != 1 && nextscheduledate.get(7) != 7;
                    break;
                }
                case 'D': {
                    boolean bl = selected = i % frequency == 0;
                }
            }
            if (!selected) continue;
            schedule.add(nextscheduledate);
            lastscheduledate = (Calendar)nextscheduledate.clone();
        }
        this.logInfo("Scheduling " + schedule.size() + " events");
        if (frequency > 0) {
            lastscheduledate.add(5, frequency);
        } else {
            lastscheduledate.add(5, 1);
            if (lastscheduledate.get(7) == 7) {
                lastscheduledate.add(5, 2);
            } else if (lastscheduledate.get(7) == 1) {
                lastscheduledate.add(5, 1);
            }
        }
        schedule.add(lastscheduledate);
        return schedule;
    }

    private Timestamp getEndDate(String[] ruleparts) throws ServiceException {
        Timestamp result;
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("MM/dd/yyyy");
        try {
            sdf.parse(ruleparts[3].substring(1));
            Calendar enddate = sdf.getCalendar();
            enddate.set(10, Integer.parseInt(ruleparts[2].substring(0, 2)));
            enddate.set(12, Integer.parseInt(ruleparts[2].substring(3, 5)));
            enddate.set(13, ruleparts[2].length() >= 6 ? Integer.parseInt(ruleparts[2].substring(6, 8)) : 0);
            result = new Timestamp(enddate.getTime().getTime());
            this.logInfo("Calculated the end date: " + enddate.getTime().toString());
        }
        catch (Exception e) {
            throw new ServiceException("Could not parse the date from the rule", e);
        }
        return result;
    }

    private ArrayList parseWeek(String[] ruleparts, Timestamp scheduledt, Timestamp now) throws ServiceException {
        this.logInfo("Parsing a day based rule");
        ArrayList<Calendar> schedule = new ArrayList<Calendar>();
        Date tempdate = new Date();
        tempdate.setTime(scheduledt.getTime());
        Calendar startdate = Calendar.getInstance();
        startdate.setTime(tempdate);
        int secondsincrement = 86400;
        int totalweeks = 0;
        int frequency = Integer.parseInt(ruleparts[0].substring(1, 3));
        boolean[] days = new boolean[8];
        days[1] = ruleparts[0].charAt(3) == 'Y';
        days[2] = ruleparts[0].charAt(4) == 'Y';
        days[3] = ruleparts[0].charAt(5) == 'Y';
        days[4] = ruleparts[0].charAt(6) == 'Y';
        days[5] = ruleparts[0].charAt(7) == 'Y';
        days[6] = ruleparts[0].charAt(8) == 'Y';
        days[7] = ruleparts[0].charAt(9) == 'Y';
        int nbevents = 0;
        if (ruleparts[3].charAt(0) == 'D') {
            Timestamp enddatets = this.getEndDate(ruleparts);
            if (enddatets != null) {
                nbevents = enddatets.after(now) ? this.getNbEvents(scheduledt, now, secondsincrement) : this.getNbEvents(scheduledt, enddatets, secondsincrement);
            }
        } else {
            nbevents = this.getNbEvents(scheduledt, now, secondsincrement);
        }
        int lastServiceCalled = 0;
        for (int i = 0; i < nbevents; ++i) {
            boolean selected;
            Calendar nextscheduledate = (Calendar)startdate.clone();
            nextscheduledate.add(5, i);
            if (i != 0 && nextscheduledate.get(7) == 1) {
                ++totalweeks;
            }
            boolean bl = selected = totalweeks % frequency == 0;
            if (!(selected & days[nextscheduledate.get(7)])) continue;
            schedule.add(nextscheduledate);
            lastServiceCalled = i + 1;
        }
        this.logInfo("Scheduling " + schedule.size() + " events");
        Calendar nextscheduledate = (Calendar)startdate.clone();
        if (lastServiceCalled > 0) {
            nextscheduledate.add(5, lastServiceCalled);
        }
        schedule.add(nextscheduledate);
        return schedule;
    }

    private ArrayList parseMonth(String[] ruleparts, Timestamp scheduledt, Timestamp now) throws ServiceException {
        this.logInfo("Parsing a Month based rule");
        ArrayList<Calendar> schedule = new ArrayList<Calendar>();
        Date tempdate = new Date();
        tempdate.setTime(scheduledt.getTime());
        Calendar startdate = Calendar.getInstance();
        startdate.setTime(tempdate);
        int secondsincrement = 86400;
        int frequency = Integer.parseInt(ruleparts[0].substring(4));
        int day = 0;
        if (ruleparts[0].charAt(1) == 'D') {
            switch (ruleparts[0].charAt(3)) {
                case '1': {
                    day = 1;
                    break;
                }
                case '2': {
                    day = 2;
                    break;
                }
                case '3': {
                    day = 3;
                    break;
                }
                case '4': {
                    day = 4;
                    break;
                }
                case '5': {
                    day = 5;
                    break;
                }
                case '6': {
                    day = 6;
                    break;
                }
                case '7': {
                    day = 7;
                }
            }
        }
        int nbevents = 0;
        if (ruleparts[3].charAt(0) == 'D') {
            Timestamp enddatets = this.getEndDate(ruleparts);
            if (enddatets != null) {
                nbevents = enddatets.after(now) ? this.getNbEvents(scheduledt, now, secondsincrement) : this.getNbEvents(scheduledt, enddatets, secondsincrement);
            }
        } else {
            nbevents = this.getNbEvents(scheduledt, now, secondsincrement);
        }
        int totalmonths = 0;
        int currentmonth = 0;
        for (int i = 0; i < nbevents; ++i) {
            boolean selected;
            Calendar nextscheduledate = (Calendar)startdate.clone();
            nextscheduledate.add(5, i);
            if (i == 0) {
                currentmonth = nextscheduledate.get(2);
            }
            if (i != 0 && currentmonth != nextscheduledate.get(2)) {
                ++totalmonths;
                currentmonth = nextscheduledate.get(2);
            }
            boolean bl = selected = totalmonths % frequency == 0;
            if (selected) {
                switch (ruleparts[0].charAt(1)) {
                    case 'T': {
                        int ndays = Integer.parseInt(ruleparts[0].substring(2, 4));
                        selected = ndays == nextscheduledate.get(5);
                        break;
                    }
                    case 'D': {
                        int every = Integer.parseInt(ruleparts[0].substring(2, 3));
                        boolean bl2 = selected = day == nextscheduledate.get(7) && every == nextscheduledate.get(8);
                    }
                }
            }
            if (!selected) continue;
            schedule.add(nextscheduledate);
        }
        this.logInfo("Scheduling " + schedule.size() + " events");
        Calendar nextscheduledate = (Calendar)startdate.clone();
        nextscheduledate.add(5, nbevents);
        schedule.add(nextscheduledate);
        return schedule;
    }

    private ArrayList parseYear(String[] ruleparts, Timestamp scheduledt, Timestamp now) throws ServiceException {
        this.logInfo("Parsing a Year based rule");
        ArrayList<Calendar> schedule = new ArrayList<Calendar>();
        Date tempdate = new Date();
        tempdate.setTime(scheduledt.getTime());
        Calendar startdate = Calendar.getInstance();
        startdate.setTime(tempdate);
        int secondsincrement = 86400;
        int day = 0;
        if (ruleparts[0].charAt(1) == 'D') {
            switch (ruleparts[0].charAt(3)) {
                case '1': {
                    day = 1;
                    break;
                }
                case '2': {
                    day = 2;
                    break;
                }
                case '3': {
                    day = 3;
                    break;
                }
                case '4': {
                    day = 4;
                    break;
                }
                case '5': {
                    day = 5;
                    break;
                }
                case '6': {
                    day = 6;
                    break;
                }
                case '7': {
                    day = 7;
                }
            }
        }
        int month = 0;
        switch (Integer.parseInt(ruleparts[0].substring(4))) {
            case 1: {
                month = 0;
                break;
            }
            case 2: {
                month = 1;
                break;
            }
            case 3: {
                month = 2;
                break;
            }
            case 4: {
                month = 3;
                break;
            }
            case 5: {
                month = 4;
                break;
            }
            case 6: {
                month = 5;
                break;
            }
            case 7: {
                month = 6;
                break;
            }
            case 8: {
                month = 7;
                break;
            }
            case 9: {
                month = 8;
                break;
            }
            case 10: {
                month = 9;
                break;
            }
            case 11: {
                month = 10;
                break;
            }
            case 12: {
                month = 11;
            }
        }
        int nbevents = 0;
        if (ruleparts[3].charAt(0) == 'D') {
            Timestamp enddatets = this.getEndDate(ruleparts);
            if (enddatets != null) {
                nbevents = enddatets.after(now) ? this.getNbEvents(scheduledt, now, secondsincrement) : this.getNbEvents(scheduledt, enddatets, secondsincrement);
            }
        } else {
            nbevents = this.getNbEvents(scheduledt, now, secondsincrement);
        }
        for (int i = 0; i < nbevents; ++i) {
            Calendar nextscheduledate = (Calendar)startdate.clone();
            nextscheduledate.add(5, i);
            boolean selected = false;
            if (nextscheduledate.get(2) == month) {
                switch (ruleparts[0].charAt(1)) {
                    case 'T': {
                        int ndays = Integer.parseInt(ruleparts[0].substring(2, 4));
                        selected = ndays == nextscheduledate.get(5);
                        break;
                    }
                    case 'D': {
                        int every = Integer.parseInt(ruleparts[0].substring(2, 3));
                        boolean bl = selected = day == nextscheduledate.get(7) && every == nextscheduledate.get(8);
                    }
                }
            }
            if (!selected) continue;
            schedule.add(nextscheduledate);
        }
        this.logInfo("Scheduling " + schedule.size() + " events");
        Calendar nextscheduledate = (Calendar)startdate.clone();
        nextscheduledate.add(5, nbevents);
        schedule.add(nextscheduledate);
        return schedule;
    }

    public static boolean incrementCounter(String schedulePlanId, String schedulePlanItemId, int count, SapphireConnection sapphireConnection) throws SapphireException {
        boolean success = true;
        String planItemSql = "select spe.currentcount, spi.schedulerule, spe.executedflag from scheduleplanitem spi  left outer join scheduleplanitemexec spe on spi.scheduleplanid = spe.scheduleplanid and spi.scheduleplanitemid = spe.scheduleplanitemid   where spi.scheduleplanid=? and spi.scheduleplanitemid=?";
        DBUtil db = new DBUtil(sapphireConnection.getConnectionId());
        db.setConnection(sapphireConnection);
        db.createPreparedResultSet(planItemSql, new Object[]{schedulePlanId, schedulePlanItemId});
        DataSet planItemDs = new DataSet(db.getResultSet());
        int oldCount = planItemDs.getInt(0, "currentcount", 0);
        String schedulerule = planItemDs.getValue(0, "schedulerule");
        String executed = planItemDs.getValue(0, "executedflag", "N");
        int maxThreshold = 1000;
        int gracePeriod = 0;
        ScheduleRule sr = new ScheduleRule(schedulerule);
        if (sr.getRuleType() == 12) {
            String[] usages = sr.getCounts();
            maxThreshold = Integer.parseInt(usages[1]);
            if (usages.length >= 3 && usages[2].trim().length() > 0) {
                gracePeriod = Integer.parseInt(usages[2]);
            }
        }
        int currentCount = oldCount + count;
        ActionProcessor ap = new ActionProcessor(sapphireConnection.getConnectionId());
        if (currentCount > maxThreshold && executed.equalsIgnoreCase("N")) {
            ScheduleService.executeTask(schedulePlanId, schedulePlanItemId, ap);
            executed = "Y";
        }
        ScheduleService.updatePlanItem(schedulePlanId, schedulePlanItemId, currentCount, executed, sapphireConnection);
        if (currentCount > maxThreshold + gracePeriod) {
            throw new SapphireException();
        }
        return success;
    }

    private static void updatePlanItem(String schedulePlanId, String schedulePlanItemId, int incrementValue, String executedFlag, SapphireConnection sapphireConnection) throws SapphireException {
        String sdcId = "SchedulePlanItem";
        int updateCount = 0;
        DBUtil database = new DBUtil(sapphireConnection.getConnectionId());
        try {
            database.setConnection(sapphireConnection);
            PreparedStatement updatePS = database.prepareStatement("UPDATE scheduleplanitemexec SET currentcount = ?, executedflag = ?  WHERE scheduleplanid = ?  AND scheduleplanitemid = ? ");
            updatePS.setInt(1, incrementValue);
            updatePS.setString(2, executedFlag);
            updatePS.setString(3, schedulePlanId);
            updatePS.setString(4, schedulePlanItemId);
            updateCount = updatePS.executeUpdate();
            if (updateCount < 1) {
                throw new SapphireException("No scheduleplanitemexec-row found");
            }
            updatePS.close();
        }
        catch (Exception e) {
            throw new SapphireException("SCHEDULE_SERVICE_FAILED", "SchedulePlanItem  '" + schedulePlanId + "', scheduleplanitemid '" + schedulePlanItemId + "'", e);
        }
        finally {
            database.reset();
        }
    }

    private static void executeTask(String planIds, String planItemIds, ActionProcessor ap) {
        PropertyList props = new PropertyList();
        props.put("planid", planIds);
        props.put("planitemid", planItemIds);
        try {
            ap.processAction("ExecutePlanItem", "1", props);
        }
        catch (ActionException e) {
            e.printStackTrace();
        }
    }

    public void resetCounter(HashMap<String, String> planItems) throws SapphireException {
        String[] schedulePlanIdArray = StringUtil.split(planItems.get("scheduleplanid"), ";");
        String[] schedulePlanItemIdArray = StringUtil.split(planItems.get("scheduleplanitemid"), ";");
        String[] scheduleRuleArray = StringUtil.split(planItems.get("schedulerule"), ";");
        String[] resetToZeroArray = StringUtil.split(planItems.get("resettozero"), ";");
        Object[] parsedRulesArray = new Object[schedulePlanIdArray.length];
        String[] currentCountArray = new String[schedulePlanIdArray.length];
        FormatUtil formatUtil = FormatUtil.getInstance(this.connectionInfo);
        try {
            int i;
            DBUtil database = new DBUtil(this.sapphireConnection.getConnectionId());
            database.setConnection(this.sapphireConnection);
            PreparedStatement planItemDetails = database.prepareStatement("planItemDetails", "SELECT spi.scheduleplanid, spi.scheduleplanitemid, spi.schedulerule, spe.currentcount FROM scheduleplanitem spi  left outer join scheduleplanitemexec spe on spi.scheduleplanid = spe.scheduleplanid and spi.scheduleplanitemid = spe.scheduleplanitemid  WHERE spi.scheduleplanid = ? AND spi.scheduleplanitemid=?");
            if (scheduleRuleArray.length != schedulePlanIdArray.length) {
                scheduleRuleArray = new String[schedulePlanIdArray.length];
                for (i = 0; i < schedulePlanIdArray.length; ++i) {
                    planItemDetails.setString(1, schedulePlanIdArray[i]);
                    planItemDetails.setString(2, schedulePlanItemIdArray[i]);
                    DataSet planItemDetailsDs = new DataSet(planItemDetails.executeQuery());
                    scheduleRuleArray[i] = planItemDetailsDs.getValue(0, "schedulerule", "");
                    currentCountArray[i] = planItemDetailsDs.getValue(0, "currentcount", "0");
                }
            }
            for (i = 0; i < scheduleRuleArray.length; ++i) {
                ScheduleRule sr = new ScheduleRule();
                sr.setRule(scheduleRuleArray[i]);
                if (sr.getRuleType() == 12) {
                    parsedRulesArray[i] = sr.getCounts();
                    continue;
                }
                schedulePlanIdArray[i] = "";
            }
            if (resetToZeroArray.length != schedulePlanIdArray.length) {
                resetToZeroArray = new String[schedulePlanIdArray.length];
            }
            for (i = 0; i < resetToZeroArray.length; ++i) {
                if (resetToZeroArray[i].equalsIgnoreCase("Y")) continue;
                String[] parsedRule = (String[])parsedRulesArray[i];
                resetToZeroArray[i] = parsedRule[0].equalsIgnoreCase("After") ? "Y" : "N";
            }
            DataSet updateDs = new DataSet();
            updateDs.addColumn("scheduleplanitemid", 0);
            updateDs.addColumn("scheduleplanid", 0);
            updateDs.addColumn("currentcount", 1);
            updateDs.addColumn("executedflag", 0);
            for (int i2 = 0; i2 < schedulePlanIdArray.length; ++i2) {
                if (schedulePlanIdArray[i2].length() <= 0) continue;
                int row = updateDs.addRow();
                updateDs.setString(row, "scheduleplanid", schedulePlanIdArray[i2]);
                updateDs.setString(row, "scheduleplanitemid", schedulePlanItemIdArray[i2]);
                updateDs.setString(row, "executedflag", "N");
                if (resetToZeroArray[i2].equalsIgnoreCase("Y")) {
                    updateDs.setNumber(row, "currentcount", 0);
                    continue;
                }
                if (currentCountArray[i2] == null) {
                    planItemDetails.setString(1, schedulePlanIdArray[i2]);
                    planItemDetails.setString(2, schedulePlanItemIdArray[i2]);
                    DataSet planItemDetailsDs = new DataSet(planItemDetails.executeQuery());
                    currentCountArray[i2] = planItemDetailsDs.getValue(0, "currentcount", "0");
                }
                int resetValue = Math.max(formatUtil.parseBigDecimal(currentCountArray[i2]).intValue() - formatUtil.parseBigDecimal(((String[])parsedRulesArray[i2])[1]).intValue(), 0);
                updateDs.setNumber(row, "currentcount", resetValue);
            }
            if (updateDs.getRowCount() > 0) {
                DataSetUtil.update(database, updateDs, "scheduleplanitemexec", new String[]{"scheduleplanid", "scheduleplanitemid"});
            }
            database.closeStatement("planItemDetails");
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

