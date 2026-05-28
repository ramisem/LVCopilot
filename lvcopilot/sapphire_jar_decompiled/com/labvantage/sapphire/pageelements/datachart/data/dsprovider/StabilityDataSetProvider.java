/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.data.dsprovider;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.pageelements.datachart.data.dsprovider.CustomDataSetProvider;
import com.labvantage.sapphire.scheduler.ScheduleEvent;
import com.labvantage.sapphire.scheduler.ScheduleRule;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class StabilityDataSetProvider
extends BaseCustom
implements CustomDataSetProvider {
    private PropertyList inputProps;
    private PropertyList outputProps;
    private PropertyList outputPropValues = new PropertyList();
    private String rsetId = "";
    private static final String COLUMN_INTERVALDATE = "intervaldate";
    private static final String COLUMN_INTERVALDATESTRING = "intervaldatestring";
    private static final int DEFAULT_MAXROWS = 1000;

    @Override
    public DataSet getDataSet() {
        if (this.inputProps == null) {
            throw new IllegalStateException("Input properties is null");
        }
        StringBuilder where = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        String studyIdParam = this.inputProps.getProperty("studyid");
        String studyIdFilterParam = this.inputProps.getProperty("studyid_filter");
        String productIdParam = this.inputProps.getProperty("productid");
        String condititionIdParam = this.inputProps.getProperty("conditionlabel");
        String scheduleTimeRuleParam = this.inputProps.getProperty("schedulerulelabel");
        String startDtParam = this.inputProps.getProperty("startdt");
        String endDtParam = this.inputProps.getProperty("enddt");
        String paramIdParam = this.inputProps.getProperty("paramlistparam");
        String intervalUnit = this.inputProps.getProperty("intervalunit", "Days");
        String includeT0Str = this.inputProps.getProperty("includet0", "auto");
        String maxRowsStr = this.inputProps.getProperty("maxrows", "1000");
        String primaryColumns = this.inputProps.getProperty("primarycolumns", "");
        int maxrows = 1000;
        try {
            maxrows = Integer.valueOf(maxRowsStr);
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
        boolean includeT0 = false;
        if (includeT0Str.equals("Y")) {
            includeT0 = true;
        } else if (includeT0Str.equals("auto") && scheduleTimeRuleParam.isEmpty()) {
            includeT0 = true;
        }
        StringBuilder from = new StringBuilder();
        from.append("s_sample");
        where.append(" s_sample.studyid is not null ");
        boolean isOracle = this.getConnectionProcessor().isOra();
        if (!studyIdParam.isEmpty()) {
            where.append(" AND ");
            where.append(" s_sample.studyid in ('").append(SafeSQL.convertToSQLInClause(studyIdParam, ";", isOracle)).append("') ");
        }
        if (!studyIdFilterParam.isEmpty()) {
            where.append(" AND ");
            where.append(" s_sample.studyid in ('").append(SafeSQL.convertToSQLInClause(studyIdFilterParam, ";", isOracle)).append("') ");
        }
        if (!productIdParam.isEmpty()) {
            where.append(" AND ");
            where.append(" s_sample.productid in ('").append(SafeSQL.convertToSQLInClause(productIdParam, ";", isOracle)).append("') ");
        }
        if (!condititionIdParam.isEmpty()) {
            from.append(", schedulecondition, scheduleplanitem");
            where.append(" AND  schedulecondition.scheduleplanid = s_sample.eventplan and schedulecondition.scheduleconditionid = scheduleplanitem.scheduleconditionid and scheduleplanitem.scheduleplanid = s_sample.eventplan and scheduleplanitem.scheduleplanitemid = s_sample.eventplanitem ");
            where.append(" AND ");
            where.append(" schedulecondition.conditionlabel in ('").append(SafeSQL.convertToSQLInClause(condititionIdParam, ";", isOracle)).append("')  ");
        }
        if (!scheduleTimeRuleParam.isEmpty()) {
            where.append(" AND ");
            where.append(" s_sample.schedulerulelabel in ('").append(SafeSQL.convertToSQLInClause(scheduleTimeRuleParam, ";", isOracle)).append("') ");
        }
        String conditionLabelFromStudy = "(select conditionlabel from schedulecondition, scheduleplanitem where schedulecondition.scheduleplanid = s_sample.eventplan and schedulecondition.scheduleconditionid = scheduleplanitem.scheduleconditionid and scheduleplanitem.scheduleplanid = s_sample.eventplan and scheduleplanitem.scheduleplanitemid = s_sample.eventplanitem ) studyconditionlabel ";
        String columns = primaryColumns + ", " + conditionLabelFromStudy;
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid("Sample");
        sdiRequest.setQueryWhere(where.toString());
        sdiRequest.setQueryFrom(from.toString());
        sdiRequest.setRetainRsetid(true);
        sdiRequest.setRequestItem("primary[" + columns + "]");
        sdiRequest.setRetainRsetid(true);
        sdiRequest.setRetrieveLimit(maxrows);
        SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
        this.rsetId = sdiData.getRsetid();
        int nofRows = sdiData.getQualifiedRows();
        if (nofRows > maxrows) {
            this.getDAMProcessor().clearRSet(this.rsetId);
            return new DataSet();
        }
        DataSet primary = sdiData.getDataset("primary");
        if (primary == null) {
            primary = new DataSet();
        }
        primary.addColumn(COLUMN_INTERVALDATE, 1);
        primary.addColumn("sampleconditionlabel", 0);
        for (int i = 0; i < primary.getRowCount(); ++i) {
            primary.setString(i, "sampleconditionlabel", primary.getString(i, "conditionlabel"));
            primary.setString(i, "conditionlabel", primary.getString(i, "studyconditionlabel"));
        }
        String studyids = this.getUniqueItems(primary.getColumnValues("studyid", ";"));
        String studySql = "select v.*, sc.conditionlabel studyconditionlabel from v_stabilitystudy v left join schedulecondition sc on sc.scheduleplanid = v.scheduleplanid and sc.scheduleconditionid = v.scheduleconditionid where studyid in (" + safeSQL.addIn(studyids, ";") + ")";
        DataSet studyInfoDs = this.getQueryProcessor().getPreparedSqlDataSet(studySql, safeSQL.getValues());
        HashMap<String, Calendar> studyConditionStart = new HashMap<String, Calendar>();
        for (int i = 0; i < studyInfoDs.getRowCount(); ++i) {
            String studyId = studyInfoDs.getValue(i, "studyid");
            String conditionLabel = studyInfoDs.getValue(i, "conditionlabel");
            String studyConditionLabel = studyInfoDs.getValue(i, "studyconditionlabel");
            String key = studyId + ";" + studyConditionLabel;
            if (studyConditionStart.containsKey(key)) continue;
            Calendar conditionStart = studyInfoDs.getCalendar(i, "condition_startdt");
            studyConditionStart.put(key, conditionStart);
        }
        if (includeT0) {
            List<String> studies = Arrays.asList(StringUtil.split(studyids, ";"));
            String t0Sql = "select S.S_SAMPLEID, sc.conditionlabel FROM S_SAMPLE S, V_STABILITYSTUDY  V left join schedulecondition sc on sc.scheduleplanid = v.scheduleplanid and sc.scheduleconditionid = v.scheduleconditionid  WHERE  S.STUDYID = ? AND   S.EVENTPLAN = V.SCHEDULEPLANID  AND S.EVENTPLANITEM = V.SCHEDULEPLANITEMID  AND S.STUDYID = V.STUDYID  AND V.EVENTDT - V.CONDITION_STARTDT = 0 UNION SELECT S.S_SAMPLEID, SC.conditionlabel FROM STUDY_SCHEDULEPLAN SSP, SCHEDULECONDITIONREFITEM SCRI, SCHEDULECONDITION SC, S_SAMPLE S WHERE SSP.STUDYID = ? AND   SCRI.SCHEDULEPLANID = SSP.SCHEDULEPLANID  AND SCRI.REFITEMKEYID1 = S.S_SAMPLEID  AND SCRI.REFITEMSDCID = 'Sample' AND SCRI.SCHEDULECONDITIONID = SC.SCHEDULECONDITIONID AND SC.SCHEDULEPLANID = SSP.SCHEDULEPLANID ";
            for (String studyid : studies) {
                DataSet t0ds = this.getQueryProcessor().getPreparedSqlDataSet(t0Sql, (Object[])new String[]{studyid, studyid});
                if (t0ds.getRowCount() <= 0) continue;
                SDIRequest t0request = new SDIRequest();
                t0request.setSDCid("Sample");
                t0request.setKeyid1List(t0ds.getColumnValues("s_sampleid", ";"));
                t0request.setRequestItem("primary");
                DataSet t0SampleDs = this.getSDIProcessor().getSDIData(t0request).getDataset("primary");
                HashSet<String> t0forConditionSet = new HashSet<String>();
                for (int i = 0; i < t0ds.getRowCount(); ++i) {
                    String studyId = t0ds.getValue(i, "studyid");
                    String conditionId = t0ds.getValue(i, "conditionlabel");
                    if (studyId.isEmpty() || conditionId.isEmpty()) continue;
                    t0forConditionSet.add(conditionId);
                }
                HashMap<String, String> filterMap = new HashMap<String, String>();
                filterMap.put("studyid", studyid);
                DataSet studyDs = primary.getFilteredDataSet(filterMap);
                String conditions = this.getUniqueItems(studyDs.getColumnValues("conditionlabel", ";"));
                List<String> conditionsList = Arrays.asList(StringUtil.split(conditions, ";"));
                for (int i = 0; i < t0SampleDs.getRowCount(); ++i) {
                    String t0STudy = t0SampleDs.getValue(i, "studyid");
                    if (!t0STudy.equals(studyid)) {
                        t0SampleDs.setValue(i, "studyid", studyid);
                    }
                    String t0conditionLabel = t0SampleDs.getValue(i, "conditionlabel");
                    String t0scheduleTimeRule = t0SampleDs.getValue(i, "schedulerulelabel");
                    for (String condition : conditionsList) {
                        if (t0conditionLabel.equals(condition) || t0forConditionSet.contains(condition)) continue;
                        t0SampleDs.setValue(i, "conditionlabel", condition);
                        t0SampleDs.setNumber(i, COLUMN_INTERVALDATE, BigDecimal.ZERO);
                        if (t0scheduleTimeRule.isEmpty()) {
                            t0SampleDs.setValue(i, "schedulerulelabel", "t0");
                        }
                        primary.copyRow(t0SampleDs, i, 1);
                    }
                }
            }
        }
        this.outputPropValues.setProperty("studyid", studyids);
        this.outputPropValues.setProperty("onestudy", studyids.contains(";") || studyids.isEmpty() ? "N" : "Y");
        String schedulerulelabels = this.getUniqueItems(primary.getColumnValues("schedulerulelabel", ";"));
        try {
            Calendar startDt = Calendar.getInstance();
            ArrayList schedulePlanLabels = new ArrayList();
            ArrayList<DataSet> eventPlansDs = primary.getGroupedDataSets("eventplan");
            ArrayList<String> appliedRules = new ArrayList<String>();
            DataSet dataSet = new DataSet();
            dataSet.addColumn("schedulerulelabel", 0);
            dataSet.addColumn("schedulerule", 0);
            dataSet.addColumn("date", 2);
            String timeRule = " @ " + (startDt.get(11) < 10 ? "0" : "") + startDt.get(11) + ":" + (startDt.get(12) < 10 ? "0" : "") + startDt.get(12);
            for (DataSet ds : eventPlansDs) {
                ArrayList<String> thisPlanCheckList = new ArrayList<String>();
                String planId = ds.getString(0, "eventplan");
                for (int j = 0; j < ds.getRowCount(); ++j) {
                    String schduleRuleLabel = ds.getString(j, "schedulerulelabel");
                    if (appliedRules.contains(schduleRuleLabel) || thisPlanCheckList.contains(schduleRuleLabel)) continue;
                    thisPlanCheckList.add(schduleRuleLabel);
                }
                if (!thisPlanCheckList.isEmpty()) {
                    String ruleSql = "select * from scheduletimerule where scheduleplanid  = ?";
                    DataSet planRules = this.getQueryProcessor().getPreparedSqlDataSet(ruleSql, (Object[])new String[]{planId});
                    for (String rulelabel : thisPlanCheckList) {
                        HashMap<String, String> filterMap = new HashMap<String, String>();
                        filterMap.put("schedulerulelabel", rulelabel);
                        int rowNum = planRules.findRow(filterMap);
                        String ruleStr = planRules.getString(rowNum, "schedulerule");
                        ScheduleRule scheduleRule = new ScheduleRule();
                        ArrayList events = new ArrayList();
                        ScheduleEvent scheduleEvent = new ScheduleEvent("", "", Calendar.getInstance(), TimeZone.getDefault());
                        scheduleEvent.setEventStatus("S");
                        scheduleRule.setRule(ruleStr + timeRule);
                        if (scheduleRule.isValidRule()) {
                            events.addAll(scheduleRule.getEvents(scheduleEvent, (Calendar)startDt.clone(), null, startDt, true));
                            if (events.size() == 1) {
                                int row = dataSet.addRow();
                                dataSet.setString(row, "schedulerulelabel", rulelabel);
                                dataSet.setString(row, "schedulerule", ruleStr);
                                Calendar eventDt = ((ScheduleEvent)events.get(0)).getEventDt();
                                dataSet.setDate(row, "date", eventDt);
                            }
                        }
                        appliedRules.add(rulelabel);
                    }
                }
                dataSet.sort("date");
                schedulerulelabels = dataSet.getColumnValues("schedulerulelabel", ";");
            }
        }
        catch (SapphireException startDt) {
            // empty catch block
        }
        this.outputPropValues.setProperty("schedulerulelabel", schedulerulelabels);
        this.outputPropValues.setProperty("oneschedulerule", schedulerulelabels.contains(";") || schedulerulelabels.isEmpty() ? "N" : "Y");
        String conditionlabels = this.getUniqueItems(primary.getColumnValues("conditionlabel", ";"));
        this.outputPropValues.setProperty("conditionlabel", conditionlabels);
        this.outputPropValues.setProperty("onecondition", conditionlabels.contains(";") || conditionlabels.isEmpty() ? "N" : "Y");
        String productids = this.getUniqueItems(primary.getColumnValues("productid", ";"));
        this.outputPropValues.setProperty("productid", productids);
        this.outputPropValues.setProperty("oneproduct", productids.contains(";") || productids.isEmpty() ? "N" : "Y");
        String oneParam = paramIdParam.contains(";") || paramIdParam.isEmpty() ? "N" : "Y";
        this.outputPropValues.setProperty("singleparam", oneParam);
        this.outputPropValues.setProperty("oneparam", oneParam);
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String studyId = primary.getValue(i, "studyid");
            String conditionLabel = primary.getValue(i, "conditionlabel");
            BigDecimal intervalDate = primary.getBigDecimal(i, COLUMN_INTERVALDATE);
            if (intervalDate != null) continue;
            String key = studyId + ";" + conditionLabel;
            Calendar conditionStart = (Calendar)studyConditionStart.get(key);
            BigDecimal condStart = new BigDecimal(conditionStart.getTimeInMillis());
            condStart = condStart.divide(new BigDecimal(3600000), 5, RoundingMode.HALF_UP);
            BigDecimal eventDtNum = new BigDecimal(primary.getCalendar(i, "eventdt").getTimeInMillis());
            eventDtNum = eventDtNum.divide(new BigDecimal(3600000), 5, RoundingMode.HALF_UP);
            intervalDate = eventDtNum.add(condStart.negate());
            if (!intervalUnit.equals("Hours")) {
                if (intervalUnit.equals("Days")) {
                    intervalDate = intervalDate.divide(BigDecimal.valueOf(24L), 5, RoundingMode.HALF_UP);
                } else if (intervalUnit.equals("Weeks")) {
                    intervalDate = intervalDate.divide(BigDecimal.valueOf(168L), 5, RoundingMode.HALF_UP);
                } else if (intervalUnit.equals("Months")) {
                    intervalDate = intervalDate.divide(BigDecimal.valueOf(730.5), 5, RoundingMode.HALF_UP);
                } else if (intervalUnit.equals("Years")) {
                    intervalDate = intervalDate.divide(BigDecimal.valueOf(8766.0), 5, RoundingMode.HALF_UP);
                }
            }
            primary.setNumber(i, COLUMN_INTERVALDATE, intervalDate);
            primary.setString(i, COLUMN_INTERVALDATESTRING, intervalDate + " " + this.getTranslationProcessor().translate(intervalUnit));
        }
        for (String outputProp : this.outputProps.keySet()) {
            this.outputPropValues.setProperty(this.outputProps.getProperty(outputProp), this.inputProps.getProperty(outputProp, ""));
        }
        primary.sort(COLUMN_INTERVALDATE);
        return primary;
    }

    private String getUniqueItems(String itemsString) {
        if (itemsString.isEmpty()) {
            return "";
        }
        StringBuilder labelsStr = new StringBuilder();
        List<String> labelsList = Arrays.asList(StringUtil.split(itemsString, ";"));
        HashSet<String> labels = new HashSet<String>(labelsList);
        ArrayList<String> labelsSorted = new ArrayList<String>(labels);
        Collections.sort(labelsSorted);
        for (String label : labelsSorted) {
            labelsStr.append(";").append(label);
        }
        return labelsStr.toString().substring(1);
    }

    @Override
    public PropertyList getOutputProps() {
        return this.outputPropValues;
    }

    @Override
    public void setInputProperties(PropertyList inputProps) {
        this.inputProps = inputProps;
    }

    @Override
    public void setOutputProperties(PropertyList outputProps) {
        this.outputProps = outputProps;
    }

    @Override
    public String getRSetId() {
        return "";
    }
}

