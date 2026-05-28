/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.wap.activity;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.modules.wap.CalendarConverter;
import com.labvantage.sapphire.modules.wap.activity.Activity;
import com.labvantage.sapphire.modules.wap.activity.ContextMap;
import com.labvantage.sapphire.modules.wap.activity.WAPConstants;
import com.labvantage.sapphire.modules.wap.activity.WAPSelectorOptions;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.io.File;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class WAPSelector
extends BaseCustom
implements WAPConstants {
    private final CalendarConverter calendarConverter;
    private DateTimeUtil dtu;
    String databaseid;

    public WAPSelector(String connectionid) {
        this.setConnectionId(connectionid);
        ConnectionProcessor cp = this.getConnectionProcessor();
        ConnectionInfo connectionInfo = cp.getConnectionInfo(connectionid);
        this.dtu = new DateTimeUtil(connectionInfo);
        this.databaseid = connectionInfo.getDatabaseId();
        this.calendarConverter = new CalendarConverter(this.dtu);
    }

    public WAPSelector(String connectionid, File rakFile) {
        this.setConnectionId(connectionid);
        this.setRakFile(rakFile);
        ConnectionProcessor cp = this.getConnectionProcessor();
        ConnectionInfo connectionInfo = cp.getConnectionInfo(connectionid);
        this.dtu = new DateTimeUtil(connectionInfo);
        this.databaseid = connectionInfo.getDatabaseId();
        this.calendarConverter = new CalendarConverter(this.dtu);
    }

    public List<Activity> getActivitiesBetween(String rangeFromString, String rangeToString, WAPSelectorOptions options) throws SapphireException {
        Instant rangeFrom = this.calendarConverter.getIsoInstant(rangeFromString);
        Instant rangeTo = this.calendarConverter.getIsoInstant(rangeToString);
        return this.getActivitiesBetween(rangeFrom, rangeTo, options);
    }

    public List<Activity> getActivitiesBetween(Instant rangeFrom, Instant rangeTo, WAPSelectorOptions options) throws SapphireException {
        ArrayList<Activity> matching = new ArrayList<Activity>();
        SafeSQL safeSQL = new SafeSQL();
        if (options.isIncludeFixed() || options.isIncludeFloating() || options.isIncludeTimemodeNone()) {
            boolean distinct = false;
            String timeWhere = this.getTimeWhere(rangeFrom, rangeTo, options, safeSQL);
            StringBuilder extraWhere = new StringBuilder();
            if (options.getActivityStatusList().length() > 0) {
                extraWhere.append(" AND a.activitystatus in (" + safeSQL.addIn(options.getActivityStatusList(), ";") + ") ");
            }
            String extraFrom = "";
            String extraSelect = "";
            if (options.isPendingUserAssignment() || options.getAssignedUser().length() > 0 || options.getAssignedUserWorkarea().length() > 0) {
                distinct = true;
                extraFrom = "activityresource ar_user";
                extraWhere.append(" AND a.activityid = ar_user.activityid AND ar_user.resourcetypeflag='A' ");
                if (options.isPendingUserAssignment()) {
                    extraWhere.append(" AND ar_user.analystid is null AND ar_user.workareadepartmentid is null");
                } else if (options.getAssignedUser().length() > 0) {
                    extraWhere.append(" AND ar_user.analystid = " + safeSQL.addVar(options.getAssignedUser()));
                    extraSelect = extraSelect + ", ar_user.duration extra_resourceduration, ar_user.analysttype extra_resourcetype";
                } else if (options.getAssignedUserWorkarea().length() > 0) {
                    extraWhere.append(" AND ar_user.workareadepartmentid = " + safeSQL.addVar(options.getAssignedUserWorkarea()));
                    extraSelect = extraSelect + ", ar_user.duration extra_resourceduration, ar_user.analysttype extra_resourcetype";
                }
                if (options.getAnalysttype().length() > 0) {
                    extraWhere.append(" AND ar_user.analysttype=" + safeSQL.addVar(options.getAnalysttype()));
                }
            }
            if (options.isPendingInstrumentAssignment() || options.getAssignedInstrument().length() > 0 || options.getAssignedInstrumentWorkarea().length() > 0) {
                distinct = true;
                extraFrom = extraFrom + (extraFrom.length() > 0 ? "," : "") + "activityresource ar_inst";
                extraWhere.append(" AND a.activityid = ar_inst.activityid AND ar_inst.resourcetypeflag='I' ");
                if (options.isPendingInstrumentAssignment()) {
                    extraWhere.append(" AND ar_inst.instrumentid is null");
                } else if (options.getAssignedInstrument().length() > 0) {
                    extraWhere.append(" AND ar_inst.instrumentid = " + safeSQL.addVar(options.getAssignedInstrument()));
                    extraSelect = ",ar_inst.duration extra_resourceduration, ar_inst.instrumenttypeid extra_resourcetype";
                } else if (options.getAssignedInstrumentWorkarea().length() > 0) {
                    extraWhere.append(" AND ar_inst.workareadepartmentid = " + safeSQL.addVar(options.getAssignedInstrumentWorkarea()));
                    if (options.getInstrumenttypeid().length() > 0) {
                        extraWhere.append(" AND ar_inst.instrumenttypeid=" + safeSQL.addVar(options.getInstrumenttypeid()));
                        extraSelect = ",ar_inst.duration extra_resourceduration, ar_inst.instrumenttypeid extra_resourcetype";
                    }
                    if (options.getInstrumentmodelid().length() > 0) {
                        extraWhere.append(" AND ar_inst.instrumentmodelid=" + safeSQL.addVar(options.getInstrumentmodelid()));
                    }
                }
            }
            if (options.getExtraActivityFrom().length() > 0) {
                extraFrom = extraFrom + (extraFrom.length() > 0 ? "," : "") + options.getExtraActivityFrom();
            }
            if (options.getExtraActiviytWhere().length() > 0) {
                extraWhere.append(" AND ").append("(" + options.getExtraActiviytWhere() + ")");
            }
            if (timeWhere.length() > 0 || extraWhere.length() > 0) {
                String sql = "SELECT " + (distinct ? " DISTINCT " : "") + " COALESCE(a.startdt, a.startrangedt) startrangeorder, a.* " + (extraSelect.length() > 0 ? extraSelect : "") + " FROM activity a " + (extraFrom.length() > 0 ? "," + extraFrom : "") + " WHERE ( " + timeWhere + ")" + extraWhere;
                sql = sql + " ORDER BY startrangeorder ";
                DataSet activities = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                for (int i = 0; i < activities.size(); ++i) {
                    Activity activity = new Activity(this.calendarConverter, activities, i);
                    matching.add(activity);
                }
            }
        }
        return matching;
    }

    public String getTimeWhere(Instant rangeFrom, Instant rangeTo, WAPSelectorOptions options, SafeSQL safeSQL) {
        StringBuilder sql = new StringBuilder();
        if (options.isIncludeFixed()) {
            sql.append(" ( ");
            sql.append(" ( a.timemode=" + safeSQL.addVar("Fixed"));
            if (options.isTreatFixedFloatingAsFixed()) {
                sql.append(" OR a.timemode=" + safeSQL.addVar("Floating"));
            }
            sql.append(" ) AND ");
            if (options.isIncludeFixedOverlaps()) {
                sql.append(" a.enddt >= " + safeSQL.addVar(this.calendarConverter.convertInstantUtcToDatabaseTimestamp(rangeFrom)) + " AND a.startdt <= " + safeSQL.addVar(this.calendarConverter.convertInstantUtcToDatabaseTimestamp(rangeTo)));
            } else {
                sql.append(" a.startdt >= " + safeSQL.addVar(this.calendarConverter.convertInstantUtcToDatabaseTimestamp(rangeFrom)) + " AND a.enddt <= " + safeSQL.addVar(this.calendarConverter.convertInstantUtcToDatabaseTimestamp(rangeTo)));
            }
            sql.append(" ) ");
        }
        sql.append(sql.length() > 0 && options.isIncludeFloating() ? " OR " : "");
        if (options.isIncludeFloating()) {
            sql.append(" ( ");
            sql.append(" a.timemode=" + safeSQL.addVar("Floating") + " AND ");
            if (options.isTreatFixedFloatingAsFixed()) {
                sql.append(" startdt is null AND ");
            }
            if (options.isIncludeFloatingOverlaps()) {
                sql.append(" a.endrangedt >= " + safeSQL.addVar(this.calendarConverter.convertInstantUtcToDatabaseTimestamp(rangeFrom)) + " AND a.startrangedt <= " + safeSQL.addVar(this.calendarConverter.convertInstantUtcToDatabaseTimestamp(rangeTo)));
            } else {
                sql.append(" a.startrangedt >= " + safeSQL.addVar(this.calendarConverter.convertInstantUtcToDatabaseTimestamp(rangeFrom)) + " AND a.endrangedt <= " + safeSQL.addVar(this.calendarConverter.convertInstantUtcToDatabaseTimestamp(rangeTo)));
            }
            sql.append(" ) ");
        }
        sql.append(sql.length() > 0 && options.isIncludeTimemodeNone() ? " OR " : "");
        if (options.isIncludeTimemodeNone()) {
            sql.append(" ( ");
            sql.append("a.timemode=" + safeSQL.addVar("None"));
            sql.append(" ) ");
        }
        return sql.toString();
    }

    public List<Activity> getRequestReservations(String requestid, String requestitemid, String reservationType, String workitemid, String workitemversionid, String workitemitemid) {
        int i;
        ArrayList<Activity> matching = new ArrayList<Activity>();
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT a.* FROM activity a WHERE a.activitycontextsdcid=" + safeSQL.addVar("Request") + " AND a.activitycontextkeyid1=" + safeSQL.addVar(requestid);
        if (reservationType.length() > 0) {
            sql = sql + " AND a.reservationtype=" + safeSQL.addVar(reservationType);
        }
        DataSet activities = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        activities.setString(-1, "_match", "Y");
        for (i = 0; i < activities.size(); ++i) {
            ContextMap map = new ContextMap(activities.getValue(i, "reservationcontext"));
            if (requestitemid.length() > 0 && !map.get("RI").equals(requestitemid)) {
                activities.setString(i, "_match", "N");
            }
            if (workitemid.length() > 0 && !map.get("WI").equals(workitemid)) {
                activities.setString(i, "_match", "N");
            }
            if (workitemversionid.length() <= 0 || map.get("WIV").equals(workitemversionid) || !map.get("WIV").equals("C")) continue;
            activities.setString(i, "_match", "N");
        }
        for (i = 0; i < activities.size(); ++i) {
            if (!activities.getValue(i, "_match").equals("Y")) continue;
            Activity activity = new Activity(this.calendarConverter, activities, i);
            matching.add(activity);
        }
        return matching;
    }

    public Map<String, Set<String>> getScheduledWorkAreaUsers(String workAreaId, Instant rangeFrom, Instant rangeTo) throws SapphireException {
        Map<String, Set<String>> scheduledList = this.fetchScheduledListFromCache(workAreaId, rangeFrom, rangeTo, "departmentid", "sysuserid", "DepartmentSchedules");
        return scheduledList;
    }

    public Map<String, Set<String>> getScheduledUserWorkAreas(String sysUserId, Instant rangeFrom, Instant rangeTo) throws SapphireException {
        Map<String, Set<String>> scheduledList = this.fetchScheduledListFromCache(sysUserId, rangeFrom, rangeTo, "sysuserid", "departmentid", "UserSchedules");
        return scheduledList;
    }

    public Map<String, Set<String>> fetchScheduledListFromCache(String id, Instant rangeFrom, Instant rangeTo, String whereColumn, String fetchColumn, String cacheName) throws SapphireException {
        if (id != null && !id.trim().isEmpty() && rangeFrom != null && rangeTo != null) {
            DataSet cachedDataSet = (DataSet)CacheUtil.get(this.databaseid, cacheName, id);
            if (cachedDataSet == null) {
                cachedDataSet = this.fetchScheduledListFromQuery(id, rangeFrom, rangeTo, whereColumn);
                CacheUtil.put(this.databaseid, cacheName, id, cachedDataSet);
                return this.buildScheduledListMap(id, rangeFrom, rangeTo, fetchColumn, cachedDataSet);
            }
            DataSet beforeDataSet = null;
            DataSet endDataSet = null;
            DataSet finalDataSet = new DataSet();
            HashMap<String, String> filterMap = new HashMap<String, String>();
            filterMap.put("isinrange", "Y");
            DataSet inRangeDataset = cachedDataSet.getFilteredDataSet(filterMap);
            if (inRangeDataset.size() > 0) {
                String fetchValue;
                Comparable<Long> dbEndDate;
                Comparable<Long> dbStartDate;
                int i;
                inRangeDataset.sort("starttime");
                Instant startDate = this.calendarConverter.convertDatabaseCalendarToInstantUtc(inRangeDataset.getCalendar(0, "assignmentstartdt")).truncatedTo(ChronoUnit.DAYS);
                inRangeDataset.sort("endtime");
                Instant endDate = this.calendarConverter.convertDatabaseCalendarToInstantUtc(inRangeDataset.getCalendar(inRangeDataset.getRowCount() - 1, "assignmentenddt")).truncatedTo(ChronoUnit.DAYS);
                if (rangeFrom.equals(startDate) && rangeTo.equals(endDate)) {
                    return this.buildScheduledListMap(id, rangeFrom, rangeTo, fetchColumn, cachedDataSet);
                }
                startDate = startDate.minus(1L, ChronoUnit.DAYS);
                endDate = endDate.plus(1L, ChronoUnit.DAYS);
                if (rangeFrom.isBefore(startDate)) {
                    beforeDataSet = this.fetchScheduledListFromQuery(id, rangeFrom, startDate, whereColumn);
                    startDate = rangeFrom;
                }
                if (rangeTo.isAfter(endDate)) {
                    endDataSet = this.fetchScheduledListFromQuery(id, endDate.truncatedTo(ChronoUnit.DAYS), rangeTo, whereColumn);
                    endDate = rangeTo;
                }
                if (beforeDataSet != null && beforeDataSet.getRowCount() > 0) {
                    finalDataSet.addAll(beforeDataSet);
                }
                inRangeDataset.sort("starttime");
                if (inRangeDataset != null && inRangeDataset.getRowCount() > 0) {
                    finalDataSet.addAll(inRangeDataset);
                }
                if (endDataSet != null && endDataSet.getRowCount() > 0) {
                    finalDataSet.addAll(endDataSet);
                }
                HashSet<String> alreadyExist = new HashSet<String>();
                for (i = 0; i < finalDataSet.getRowCount(); ++i) {
                    if (finalDataSet.getCalendar(i, "assignmentstartdt") == null || finalDataSet.getCalendar(i, "assignmentenddt") == null) continue;
                    dbStartDate = finalDataSet.getCalendar(i, "assignmentstartdt").getTime().getTime();
                    dbEndDate = finalDataSet.getCalendar(i, "assignmentenddt").getTime().getTime();
                    fetchValue = finalDataSet.getString(i, fetchColumn);
                    alreadyExist.add(fetchValue + dbStartDate + dbEndDate);
                }
                for (i = 0; i < cachedDataSet.getRowCount(); ++i) {
                    if (cachedDataSet.getCalendar(i, "assignmentstartdt") == null || cachedDataSet.getCalendar(i, "assignmentenddt") == null) continue;
                    dbStartDate = cachedDataSet.getCalendar(i, "assignmentstartdt").getTime().getTime();
                    dbEndDate = cachedDataSet.getCalendar(i, "assignmentenddt").getTime().getTime();
                    fetchValue = cachedDataSet.getString(i, fetchColumn);
                    if (!alreadyExist.add(fetchValue + dbStartDate + dbEndDate)) continue;
                    finalDataSet.add(cachedDataSet.get(i));
                }
                finalDataSet.sort("starttime");
                for (i = 0; i < finalDataSet.getRowCount(); ++i) {
                    dbStartDate = this.calendarConverter.convertDatabaseCalendarToInstantUtc(finalDataSet.getCalendar(i, "assignmentstartdt")).truncatedTo(ChronoUnit.DAYS);
                    dbEndDate = this.calendarConverter.convertDatabaseCalendarToInstantUtc(finalDataSet.getCalendar(i, "assignmentenddt")).truncatedTo(ChronoUnit.DAYS);
                    startDate = startDate.truncatedTo(ChronoUnit.DAYS);
                    endDate = endDate.truncatedTo(ChronoUnit.DAYS);
                    if (dbStartDate == null || dbEndDate == null || !startDate.isBefore((Instant)dbStartDate) && !startDate.equals(dbStartDate) || !endDate.isAfter((Instant)dbEndDate) && !endDate.equals(dbEndDate)) continue;
                    finalDataSet.setString(i, "isinrange", "Y");
                }
            } else {
                cachedDataSet = this.fetchScheduledListFromQuery(id, rangeFrom, rangeTo, whereColumn);
                CacheUtil.put(this.databaseid, cacheName, id, cachedDataSet);
                return this.buildScheduledListMap(id, rangeFrom, rangeTo, fetchColumn, cachedDataSet);
            }
            return this.buildScheduledListMap(id, rangeFrom, rangeTo, fetchColumn, finalDataSet);
        }
        return null;
    }

    public DataSet fetchScheduledListFromQuery(String id, Instant rangeFrom, Instant rangeTo, String whereColumn) throws SapphireException {
        DataSet dataSet;
        Timestamp timestampStart = this.calendarConverter.convertInstantUtcToDatabaseTimestamp(rangeFrom);
        Timestamp timestampEnd = this.calendarConverter.convertInstantUtcToDatabaseTimestamp(rangeTo);
        SafeSQL safeSQL = new SafeSQL();
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT sysuserid,departmentid,assignmentstartdt,assignmentenddt, ");
            sql.append(" CASE WHEN assignmentstartdt<").append(safeSQL.addVar(timestampStart)).append(" OR assignmentenddt>").append(safeSQL.addVar(timestampEnd)).append(" THEN 'N' ELSE 'Y' END isinrange");
            sql.append(" FROM departmentassignment");
            sql.append(" WHERE ((assignmentstartdt>=").append(safeSQL.addVar(timestampStart)).append(" AND assignmentstartdt<=").append(safeSQL.addVar(timestampEnd)).append(") OR (assignmentenddt<=").append(safeSQL.addVar(timestampEnd)).append(" AND assignmentenddt>=").append(safeSQL.addVar(timestampStart)).append(")) AND " + whereColumn + "=").append(safeSQL.addVar(id));
            sql.append(" AND ASSIGNMENTSTATUS != 'Permanent' ");
            sql.append(" ORDER BY assignmentstartdt");
            dataSet = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (dataSet != null) {
                for (int i = 0; i < dataSet.getRowCount(); ++i) {
                    if (dataSet.getCalendar(i, "assignmentstartdt") == null || dataSet.getCalendar(i, "assignmentenddt") == null) continue;
                    dataSet.setNumber(i, "starttime", dataSet.getCalendar(i, "assignmentstartdt").getTime().getTime());
                    dataSet.setNumber(i, "endtime", dataSet.getCalendar(i, "assignmentenddt").getTime().getTime());
                }
            }
        }
        catch (Exception e) {
            throw new SapphireException("CREATE_RESULTSET_FAILED", "Unable to load up schedule details for " + id, e);
        }
        return dataSet;
    }

    public Map<String, Set<String>> buildScheduledListMap(String id, Instant rangeFrom, Instant rangeTo, String fetchColumn, DataSet dataSet) throws SapphireException {
        if (id != null && !id.trim().isEmpty() && rangeFrom != null && rangeTo != null) {
            TreeMap<String, Set<String>> scheduleMap = new TreeMap<String, Set<String>>();
            Instant tempInstantLoop = rangeFrom.truncatedTo(ChronoUnit.DAYS);
            while (tempInstantLoop.compareTo(rangeTo) <= 0) {
                for (int i = 0; i < dataSet.getRowCount(); ++i) {
                    Set<String> workAreaList;
                    Instant startDate = this.calendarConverter.convertDatabaseCalendarToInstantUtc(dataSet.getCalendar(i, "assignmentstartdt")).truncatedTo(ChronoUnit.DAYS);
                    Instant endDate = this.calendarConverter.convertDatabaseCalendarToInstantUtc(dataSet.getCalendar(i, "assignmentenddt")).truncatedTo(ChronoUnit.DAYS);
                    if (startDate == null || endDate == null || tempInstantLoop.compareTo(startDate) != 0 && tempInstantLoop.compareTo(endDate) != 0 && (!tempInstantLoop.isAfter(startDate) || !tempInstantLoop.isBefore(endDate))) continue;
                    if (scheduleMap.get(tempInstantLoop.toString()) == null) {
                        workAreaList = new HashSet<String>();
                        workAreaList.add(dataSet.getString(i, fetchColumn.toLowerCase()));
                        scheduleMap.put(tempInstantLoop.toString(), workAreaList);
                        continue;
                    }
                    workAreaList = (Set)scheduleMap.get(tempInstantLoop.toString());
                    workAreaList.add(dataSet.getString(i, fetchColumn.toLowerCase()));
                }
                if (scheduleMap.get(tempInstantLoop.toString()) == null) {
                    HashSet workAreaList = new HashSet();
                    scheduleMap.put(tempInstantLoop.toString(), workAreaList);
                }
                tempInstantLoop = tempInstantLoop.plus(1L, ChronoUnit.DAYS);
            }
            return scheduleMap;
        }
        return null;
    }
}

