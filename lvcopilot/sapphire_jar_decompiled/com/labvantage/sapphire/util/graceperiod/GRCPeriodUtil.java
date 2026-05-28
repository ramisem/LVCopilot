/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.graceperiod;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyListCollection;

public class GRCPeriodUtil {
    static final String LABVANTAGE_CVS_ID = "$Revision: 61266 $";
    public static final String GRCPeriodUnit_PERCENTAGE = "%";
    public static final String GRCPeriodUnit_DAY = "Days";
    public static final String GRCPeriodUnit_MONTH = "Months";
    public static final String GRCPeriodUnit_YEAR = "Years";
    public static final String GRCPeriodUnit_HOUR = "Hours";
    public static final String GRCPeriodUnit_WEEK = "Weeks";

    public static String getGRCPeriodFromTaskCollection(PropertyListCollection plGracePeriodDetails, Calendar scheduledToDt, Calendar eventdt) {
        Calendar startDate = (Calendar)scheduledToDt.clone();
        Calendar conditionStartDate = (Calendar)scheduledToDt.clone();
        String completionBeforeTime = "";
        String completionBeforeUnit = "";
        String gracePeriodStr = "";
        String earlyGracePeriodStr = "";
        String gracePeriodUnit = "";
        float gracePeriod = 0.0f;
        float earlyGracePeriod = 0.0f;
        float endTimeOriginal = 0.0f;
        boolean considerCondStartDt = false;
        for (int i = 0; i < plGracePeriodDetails.size(); ++i) {
            completionBeforeTime = plGracePeriodDetails.getPropertyList(i).getProperty("completionbefore", "");
            completionBeforeUnit = plGracePeriodDetails.getPropertyList(i).getProperty("unit", "");
            considerCondStartDt = "Start Date".equalsIgnoreCase(plGracePeriodDetails.getPropertyList(i).getProperty("fromwhen", ""));
            gracePeriodStr = plGracePeriodDetails.getPropertyList(i).getProperty("grcperiod", "");
            earlyGracePeriodStr = plGracePeriodDetails.getPropertyList(i).getProperty("earlygrcperiod", gracePeriodStr);
            if (gracePeriodStr.length() == 0 && earlyGracePeriodStr.length() > 0) {
                gracePeriodStr = earlyGracePeriodStr;
            }
            gracePeriodUnit = plGracePeriodDetails.getPropertyList(i).getProperty("grcperiodunit", "");
            try {
                endTimeOriginal = Float.parseFloat(completionBeforeTime);
                gracePeriod = Float.parseFloat(gracePeriodStr);
                earlyGracePeriod = Float.parseFloat(earlyGracePeriodStr);
            }
            catch (NumberFormatException ne) {
                Trace.log("Error: Grace period could not be applied. Invalid or empty task property for 'Completion Before' or 'Grace Period'.");
                break;
            }
            if (completionBeforeUnit.length() == 0) {
                if (Trace.on) {
                    Trace.log("Error: Grace period could not be applied. Unit of time not specified in task property.");
                    break;
                }
            } else if (gracePeriodUnit.length() == 0 && Trace.on) {
                Trace.log("Error: Grace period could not be applied. Grace Period Unit not specified in task property.");
                break;
            }
            if (!GRCPeriodUtil.findGracePeriod(conditionStartDate, startDate, endTimeOriginal, completionBeforeUnit, eventdt, considerCondStartDt)) continue;
            return gracePeriod + ";" + gracePeriodUnit + ";" + earlyGracePeriod;
        }
        return "";
    }

    private static boolean findGracePeriod(Calendar conditionStartDt, Calendar startDate, float endTimeOriginal, String unit, Calendar eventdt, boolean considerCondStartDt) {
        boolean grcPeriodFound = false;
        Calendar endDate = considerCondStartDt ? (Calendar)conditionStartDt.clone() : (Calendar)startDate.clone();
        boolean validUnit = false;
        if (unit.equalsIgnoreCase(GRCPeriodUnit_DAY)) {
            int eTime = Math.round(endTimeOriginal * 24.0f);
            endDate.add(11, eTime);
            validUnit = true;
        } else if (unit.equalsIgnoreCase(GRCPeriodUnit_MONTH)) {
            int eTime = (int)endTimeOriginal;
            endDate.add(2, eTime);
            float frEndTime = endTimeOriginal - (float)eTime;
            if ((double)frEndTime > 0.0) {
                int fraction = Math.round(frEndTime * 30.0f);
                endDate.add(5, fraction);
            }
            validUnit = true;
        } else if (unit.equalsIgnoreCase(GRCPeriodUnit_YEAR)) {
            int eTime = (int)endTimeOriginal;
            endDate.add(1, eTime);
            float frEndTime = endTimeOriginal - (float)eTime;
            if ((double)frEndTime > 0.0) {
                int fraction = Math.round(frEndTime * 365.0f);
                endDate.add(5, fraction);
            }
            validUnit = true;
        } else if (unit.equalsIgnoreCase(GRCPeriodUnit_HOUR)) {
            int eTime = (int)endTimeOriginal;
            endDate.add(11, eTime);
            float frEndTime = endTimeOriginal - (float)eTime;
            if ((double)frEndTime > 0.0) {
                int fraction = Math.round(frEndTime * 60.0f);
                endDate.add(12, fraction);
            }
            validUnit = true;
        } else if (unit.equalsIgnoreCase(GRCPeriodUnit_WEEK)) {
            int eTime = (int)endTimeOriginal;
            endDate.add(4, eTime);
            float frEndTime = endTimeOriginal - (float)eTime;
            if ((double)frEndTime > 0.0) {
                int fraction = Math.round(frEndTime * 7.0f);
                endDate.add(5, fraction);
            }
            validUnit = true;
        }
        if (!validUnit && Trace.on) {
            Trace.log("Error: Invalid Unit specified in task property.");
            return false;
        }
        if (considerCondStartDt) {
            if ((eventdt.after(conditionStartDt) || eventdt.equals(conditionStartDt)) && (eventdt.before(endDate) || eventdt.equals(endDate))) {
                grcPeriodFound = true;
            }
        } else if ((eventdt.after(startDate) || eventdt.equals(startDate)) && eventdt.before(endDate)) {
            grcPeriodFound = true;
        }
        startDate.setTimeInMillis(endDate.getTimeInMillis());
        return grcPeriodFound;
    }

    public static String getWindowStartEndDates(Calendar startDt, Calendar dueDt, String grcPeriodUnit, float grcPeriod) {
        return GRCPeriodUtil.getWindowStartEndDates(startDt, dueDt, grcPeriodUnit, grcPeriod, grcPeriod);
    }

    public static String getWindowStartEndDates(Calendar startDt, Calendar dueDt, String grcPeriodUnit, float grcPeriod, float earlyGrcPeriod) {
        Calendar earliestDt = (Calendar)dueDt.clone();
        Calendar latestDt = (Calendar)dueDt.clone();
        boolean validGrcUnit = false;
        String returnDates = "";
        if (grcPeriodUnit.equalsIgnoreCase(GRCPeriodUnit_DAY)) {
            int graceperiod = Math.round(grcPeriod * 24.0f);
            int earlygraceperiod = Math.round(earlyGrcPeriod * 24.0f);
            latestDt.add(11, graceperiod);
            earliestDt.add(11, earlygraceperiod * -1);
            validGrcUnit = true;
        } else if (grcPeriodUnit.equalsIgnoreCase(GRCPeriodUnit_MONTH)) {
            float frPartOfEarlyGrcPeriod;
            int fraction;
            int graceperiod = (int)grcPeriod;
            int earlygraceperiod = (int)earlyGrcPeriod;
            latestDt.add(2, graceperiod);
            earliestDt.add(2, earlygraceperiod * -1);
            float frPartOfGrcPeriod = grcPeriod - (float)graceperiod;
            if ((double)frPartOfGrcPeriod > 0.0) {
                fraction = Math.round(frPartOfGrcPeriod * 30.0f);
                latestDt.add(5, fraction);
            }
            if ((double)(frPartOfEarlyGrcPeriod = earlyGrcPeriod - (float)earlygraceperiod) > 0.0) {
                fraction = Math.round(frPartOfEarlyGrcPeriod * 30.0f);
                earliestDt.add(5, fraction * -1);
            }
            validGrcUnit = true;
        } else if (grcPeriodUnit.equalsIgnoreCase(GRCPeriodUnit_YEAR)) {
            float frPartOfEarlyGrcPeriod;
            int fraction;
            int graceperiod = (int)grcPeriod;
            int earlygraceperiod = (int)earlyGrcPeriod;
            latestDt.add(1, graceperiod);
            earliestDt.add(1, earlygraceperiod * -1);
            float frPartOfGrcPeriod = grcPeriod - (float)graceperiod;
            if ((double)frPartOfGrcPeriod > 0.0) {
                fraction = Math.round(frPartOfGrcPeriod * 365.0f);
                latestDt.add(5, fraction);
            }
            if ((double)(frPartOfEarlyGrcPeriod = earlyGrcPeriod - (float)earlygraceperiod) > 0.0) {
                fraction = Math.round(frPartOfEarlyGrcPeriod * 365.0f);
                earliestDt.add(5, fraction * -1);
            }
            validGrcUnit = true;
        } else if (grcPeriodUnit.equalsIgnoreCase(GRCPeriodUnit_HOUR)) {
            float frPartOfEarlyGrcPeriod;
            int fraction;
            int graceperiod = (int)grcPeriod;
            int earlygraceperiod = (int)earlyGrcPeriod;
            latestDt.add(11, graceperiod);
            earliestDt.add(11, earlygraceperiod * -1);
            float frPartOfGrcPeriod = grcPeriod - (float)graceperiod;
            if ((double)frPartOfGrcPeriod > 0.0) {
                fraction = Math.round(frPartOfGrcPeriod * 60.0f);
                latestDt.add(12, fraction);
            }
            if ((double)(frPartOfEarlyGrcPeriod = earlyGrcPeriod - (float)earlygraceperiod) > 0.0) {
                fraction = Math.round(frPartOfEarlyGrcPeriod * 60.0f);
                earliestDt.add(12, fraction * -1);
            }
            validGrcUnit = true;
        } else if (grcPeriodUnit.equalsIgnoreCase(GRCPeriodUnit_WEEK)) {
            float frPartOfEarlyGrcPeriod;
            int fraction;
            int graceperiod = (int)grcPeriod;
            int earlygraceperiod = (int)earlyGrcPeriod;
            latestDt.add(4, graceperiod);
            earliestDt.add(4, earlygraceperiod * -1);
            float frPartOfGrcPeriod = grcPeriod - (float)graceperiod;
            if ((double)frPartOfGrcPeriod > 0.0) {
                fraction = Math.round(frPartOfGrcPeriod * 7.0f);
                latestDt.add(5, fraction);
            }
            if ((double)(frPartOfEarlyGrcPeriod = earlyGrcPeriod - (float)earlygraceperiod) > 0.0) {
                fraction = Math.round(frPartOfEarlyGrcPeriod * 7.0f);
                earliestDt.add(5, fraction * -1);
            }
            validGrcUnit = true;
        } else if (grcPeriodUnit.equalsIgnoreCase(GRCPeriodUnit_PERCENTAGE)) {
            long eventdtInMillis = dueDt.getTimeInMillis();
            long scheduledToDtInMillis = startDt.getTimeInMillis();
            long timeDiffInMillis = eventdtInMillis - scheduledToDtInMillis;
            int graceperiod = Math.round((float)timeDiffInMillis * (grcPeriod / 100.0f));
            int earlygraceperiod = Math.round((float)timeDiffInMillis * (earlyGrcPeriod / 100.0f));
            latestDt.add(14, graceperiod);
            earliestDt.add(14, earlygraceperiod * -1);
            validGrcUnit = true;
        }
        if (validGrcUnit) {
            SimpleDateFormat sdf = new SimpleDateFormat();
            returnDates = sdf.format(earliestDt.getTime()) + ";" + sdf.format(latestDt.getTime());
        } else if (Trace.on) {
            Trace.log("Error: Failed to generate windowstartdate and windowenddate. Found invalid Grace Period Unit " + grcPeriodUnit);
        }
        return returnDates;
    }

    @Deprecated
    public static Calendar getDueDate(Calendar offsetfromDt, String grcPeriodUnit, float grcPeriod) throws SapphireException {
        return DateTimeUtil.getOffsetDate(offsetfromDt, grcPeriodUnit, grcPeriod);
    }

    public static Calendar getDueDate(Calendar offsetfromDt, String grcPeriodUnit, BigDecimal grcPeriod) throws SapphireException {
        return DateTimeUtil.getOffsetDate(offsetfromDt, grcPeriodUnit, grcPeriod);
    }

    public static boolean isDeviated(String workorderId, DBAccess db) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT count(1) FROM workorder WHERE completedt NOT BETWEEN windowstartdt AND windowenddt ");
        sql.append(" AND workorderid = ? AND deviationflag = 'Y'");
        try {
            if (db.getPreparedCount(sql.toString(), new Object[]{workorderId}) > 0) {
                return true;
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        return false;
    }

    public static void recordDeviation(String workorderId, String templateId, String connectionId, Calendar windowStartDt, Calendar windowEndDt, String workorderType, DBAccess db) throws SapphireException {
        DataSet incidentColumnDS;
        HashMap<String, String> props = new HashMap<String, String>();
        if (templateId != null && templateId.length() > 0) {
            props.put("templateid", templateId);
        }
        SDCProcessor sdcProc = new SDCProcessor(connectionId);
        boolean sdcDeptSecurityEnabled = false;
        String securityUser = "";
        String securityDepartment = "";
        DataSet workOrderColumnDS = sdcProc.getColumnData("WorkOrderSDC");
        if (workOrderColumnDS != null && workOrderColumnDS.size() > 0 && workOrderColumnDS.findRow("columnid", "securityuser") > -1 && workOrderColumnDS.findRow("columnid", "securitydepartment") > -1 && (incidentColumnDS = sdcProc.getColumnData("LV_Incdt")) != null && incidentColumnDS.size() > 0 && incidentColumnDS.findRow("columnid", "securityuser") > -1 && incidentColumnDS.findRow("columnid", "securitydepartment") > -1) {
            sdcDeptSecurityEnabled = true;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMMyy");
        StringBuffer sourceSdcid = new StringBuffer();
        StringBuffer sourceKeyid1 = new StringBuffer();
        sourceSdcid.append("WorkOrderSDC").append(";");
        sourceKeyid1.append(workorderId).append(";");
        String sql = "";
        SafeSQL safeSQL = new SafeSQL();
        if (workorderType.equalsIgnoreCase("RePull")) {
            sql = "SELECT distinct workorder.studyid, sdiwi.keyid1 s_sampleid ";
            if (sdcDeptSecurityEnabled) {
                sql = sql + ",workorder.securityuser, workorder.securitydepartment";
            }
            sql = sql + " FROM workorder LEFT OUTER JOIN  sdiworkitem sdiwi ON workorder.sourcesdcid = sdiwi.sdcid AND workorder.scheduleplanid = sdiwi.scheduleplanid AND workorder.scheduleplanitemid = sdiwi.scheduleplanitemid  WHERE workorder.workorderid = " + safeSQL.addVar(workorderId);
        } else if (workorderType.equalsIgnoreCase("Pull")) {
            sql = "SELECT distinct workorder.studyid, s_sample.s_sampleid ";
            if (sdcDeptSecurityEnabled) {
                sql = sql + ",workorder.securityuser, workorder.securitydepartment";
            }
            sql = sql + " FROM workorder LEFT OUTER JOIN s_sample ON workorder.scheduleplanid = s_sample.eventplan AND workorder.scheduleplanitemid = s_sample.eventplanitem WHERE workorder.workorderid = " + safeSQL.addVar(workorderId);
            db.createPreparedResultSet("getpulltype", "SELECT propertyvalue from workorderproperty WHERE workorderid = ? AND propertyid = 'pulltype'", new String[]{workorderId});
            DataSet dsPullType = new DataSet(db.getResultSet("getpulltype"));
            if (dsPullType != null && dsPullType.getRowCount() > 0) {
                sql = "Reuse".equalsIgnoreCase(dsPullType.getString(0, "propertyvalue", "")) ? sql + " AND s_sampleid IN (SELECT sdwi.keyid1 FROM sdiworkitem sdwi , scheduleplanitemworkitem spw  WHERE sdwi.scheduleplanid = s_sample.eventplan AND sdwi.scheduleplanitemid = s_sample.eventplanitem  AND spw.scheduleplanid = sdwi.scheduleplanid AND spw.scheduleplanitemid = sdwi.scheduleplanitemid AND spw.workitemid = sdwi.workitemid AND spw.reusecontainerflag ='Y')" : sql + " AND s_sampleid IN (SELECT sdwi.keyid1 FROM sdiworkitem sdwi , scheduleplanitemworkitem spw  WHERE sdwi.scheduleplanid = s_sample.eventplan AND sdwi.scheduleplanitemid = s_sample.eventplanitem  AND spw.scheduleplanid = sdwi.scheduleplanid AND spw.scheduleplanitemid = sdwi.scheduleplanitemid AND spw.workitemid = sdwi.workitemid AND spw.reusecontainerflag <>'Y')";
            }
        }
        int i = 0;
        try {
            db.createPreparedResultSet("rsstudyid", sql, safeSQL.getValues());
            while (db.getNext("rsstudyid")) {
                String sampleid;
                if (++i == 1) {
                    String studyid = db.getString("rsstudyid", "studyid");
                    if (studyid != null && studyid.length() > 0) {
                        sourceKeyid1.append(studyid).append(";");
                        sourceSdcid.append("StudySDC").append(";");
                    }
                    if (sdcDeptSecurityEnabled) {
                        securityUser = db.getString("rsstudyid", "securityuser");
                        securityDepartment = db.getString("rsstudyid", "securitydepartment");
                    }
                }
                if ((sampleid = db.getString("rsstudyid", "s_sampleid")) == null || sampleid.length() <= 0) continue;
                sourceKeyid1.append(sampleid).append(";");
                sourceSdcid.append("Sample").append(";");
            }
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
        }
        if (sourceKeyid1.length() > 0) {
            sourceKeyid1.setLength(sourceKeyid1.length() - 1);
            sourceSdcid.setLength(sourceSdcid.length() - 1);
        }
        props.put("sourcesdcid", sourceSdcid.toString());
        props.put("sourcekeyid1", sourceKeyid1.toString());
        props.put("incidentcategory", "UnPlanned");
        props.put("incidentdesc", "WorkOrder Grace Period Violation,Window:" + sdf.format(windowStartDt.getTime()) + "-" + sdf.format(windowEndDt.getTime()) + ",Completed:" + sdf.format(Calendar.getInstance().getTime()));
        if (sdcDeptSecurityEnabled) {
            props.put("securityuser", securityUser);
            props.put("securitydepartment", securityDepartment);
        }
        ActionProcessor ap = new ActionProcessor(connectionId);
        try {
            ap.processAction("RecordIncident", "1", props);
            if (Trace.on) {
                Trace.log("Deviation recorded for WorkOrder with workorder id:" + workorderId);
            }
        }
        catch (SapphireException ex) {
            throw new SapphireException(ex);
        }
    }
}

