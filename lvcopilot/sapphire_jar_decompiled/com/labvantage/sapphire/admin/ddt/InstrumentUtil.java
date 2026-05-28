/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.admin.ddt.Instrument;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ScheduleService;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class InstrumentUtil {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    public static final String STATUS_INITIAL = "Initial";
    public static final String STATUS_AVAILABLE = "Available";
    public static final String STATUS_UNAVAILABLE = "Unavailable";
    public static final String STATUS_ACCEPTED = "Accepted";
    public static final String STATUS_REJECTED = "Rejected";
    public static final String STATUS_CANCELLED = "Cancelled";
    public static final String STATUS_COMPLETE = "Complete";
    public static final String WORKORDERTYPE_CERTIFICATION = "Certification";

    private static boolean hasPrimaryValueChanged(DataSet newPrimary, DataSet oldPrimary, int rowposition, String columnAlias) {
        boolean hasChanged = false;
        if (newPrimary.isValidColumn(columnAlias)) {
            String newValue = newPrimary.getValue(rowposition, columnAlias);
            String oldValue = oldPrimary.getValue(rowposition, columnAlias);
            if (newValue == null && oldValue != null || newValue != null && oldValue == null || newValue != null && oldValue != null && !newValue.equals(oldValue)) {
                hasChanged = true;
            }
        }
        return hasChanged;
    }

    public static void instrumentPostEdit(Instrument thisObj, SDIData sdiDataAEI, SDIData sdiDataBEI, PropertyList list, QueryProcessor qp, ActionProcessor ap, TranslationProcessor tp) throws SapphireException {
        DataSet newPrimary = sdiDataAEI.getDataset("primary");
        DataSet oldPrimary = sdiDataBEI.getDataset("primary");
        String sql = "select instrumentstatus,trackusageflag from instrument where instrumentid=?";
        String sqlUnavailableParts = "select 1 from instrument where parentinstrumentid=? and instrumentid!=? and instrumentstatus='Unavailable'";
        for (int i = 0; i < newPrimary.size(); ++i) {
            DataSet dsUnavailableParts;
            String status = newPrimary.getValue(i, "instrumentstatus", "");
            String isPart = newPrimary.getValue(i, "partflag", "");
            String instrumentid = newPrimary.getValue(i, "instrumentid");
            String parentInstrumentId = newPrimary.getValue(i, "parentinstrumentid");
            if (InstrumentUtil.hasPrimaryValueChanged(newPrimary, oldPrimary, i, "parentinstrumentid")) {
                if (parentInstrumentId.trim().length() == 0) {
                    parentInstrumentId = oldPrimary.getValue(i, "parentinstrumentid");
                }
                if (parentInstrumentId.trim().length() > 0) {
                    DataSet dsInstr;
                    String parentInstId = newPrimary.getValue(i, "parentinstrumentid", "").trim();
                    String partStatus = newPrimary.getValue(i, "instrumentstatus", "");
                    if (partStatus.length() == 0) {
                        partStatus = oldPrimary.getValue(i, "instrumentstatus", "");
                    }
                    if (parentInstId.length() > 0 && (dsInstr = qp.getPreparedSqlDataSet("instrument", sql, new String[]{parentInstId})) != null && dsInstr.size() > 0) {
                        String trackusageFlag = oldPrimary.getValue(i, "trackusageflag", "");
                        if (trackusageFlag.length() > 0 && trackusageFlag.equalsIgnoreCase("Y") && dsInstr.getString(0, "trackusageflag", "N").equalsIgnoreCase("N")) {
                            throw new SapphireException(tp.translate("Cannot add Child Instrument with TrackUsage flag is on because Parent Instrument's TrackUsage flag is not set."));
                        }
                        if (STATUS_UNAVAILABLE.equalsIgnoreCase(partStatus) && !STATUS_UNAVAILABLE.equalsIgnoreCase(dsInstr.getString(0, "instrumentstatus", ""))) {
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("sdcid", "Instrument");
                            hm.put("keyid1", parentInstrumentId);
                            hm.put("unavailabilityreason", "Unavailable Child instrument '" + instrumentid + "' is added to instrument '" + parentInstrumentId + "'.");
                            hm.put("instrumentstatus", STATUS_UNAVAILABLE);
                            ap.processAction("EditSDI", "1", hm);
                        }
                    }
                }
            }
            if (isPart.length() == 0) {
                isPart = oldPrimary.getValue(i, "partflag", "");
            }
            if (InstrumentUtil.hasPrimaryValueChanged(newPrimary, oldPrimary, i, "trackusageflag")) {
                DataSet dsInstr;
                if (parentInstrumentId.trim().length() == 0) {
                    parentInstrumentId = oldPrimary.getValue(i, "parentinstrumentid", "");
                }
                if (isPart.equalsIgnoreCase("Y") && parentInstrumentId.length() > 0 && newPrimary.getValue(i, "trackusageflag", "").equalsIgnoreCase("Y") && (dsInstr = qp.getPreparedSqlDataSet("instrument", sql, new String[]{parentInstrumentId})) != null && dsInstr.size() > 0 && dsInstr.getString(0, "trackusageflag", "N").equalsIgnoreCase("N")) {
                    throw new SapphireException(tp.translate("Cannot set the TrackUsage flag because Parent Instrument's TrackUsage flag is not set."));
                }
                String partSql = "select count(1) from instrument where parentinstrumentid=? and trackusageflag='Y'";
                if (!isPart.equalsIgnoreCase("Y") && qp.getPreparedCount(partSql, new Object[]{instrumentid}) > 0) {
                    throw new SapphireException(tp.translate("Cannot uncheck the TrackUsage flag because the TrackUsage flag is set for one or more of it's part."));
                }
            }
            if (!InstrumentUtil.hasPrimaryValueChanged(newPrimary, oldPrimary, i, "instrumentstatus") || !isPart.equalsIgnoreCase("Y")) continue;
            if (parentInstrumentId.trim().length() == 0) {
                parentInstrumentId = oldPrimary.getValue(i, "parentinstrumentid");
            }
            if (STATUS_UNAVAILABLE.equalsIgnoreCase(status)) {
                DataSet dsInstrStatus = qp.getPreparedSqlDataSet("instrumentstatus", sql, new String[]{parentInstrumentId});
                if (parentInstrumentId.trim().length() <= 0 || dsInstrStatus == null || STATUS_UNAVAILABLE.equalsIgnoreCase(dsInstrStatus.getString(0, "instrumentstatus", ""))) continue;
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("sdcid", "Instrument");
                hm.put("keyid1", parentInstrumentId);
                hm.put("instrumentstatus", STATUS_UNAVAILABLE);
                hm.put("unavailabilityreason", "Child Instrument " + instrumentid + " has become Unavailable.");
                ap.processAction("EditSDI", "1", hm);
                continue;
            }
            if (!STATUS_AVAILABLE.equalsIgnoreCase(status) || (dsUnavailableParts = qp.getPreparedSqlDataSet("unavailableparts", sqlUnavailableParts, new String[]{parentInstrumentId, instrumentid})) == null || dsUnavailableParts.size() != 0) continue;
            HashMap<String, String> valueMap = new HashMap<String, String>();
            valueMap.put("parentInstrumentId", "<b>" + parentInstrumentId + "</b>");
            thisObj.setError("Instrument", "INFORMATION", tp.translate("Now Parent Instrument [parentInstrumentId] can be made available forcefully.", valueMap));
        }
    }

    public static void workOrderSDCPostEdit(SDIData sdiDataAEI, SDIData sdiDataBEI, PropertyList list, QueryProcessor qp, ActionProcessor ap, SapphireConnection sapphireConnection) throws SapphireException {
        DataSet newPrimary = sdiDataAEI.getDataset("primary");
        DataSet oldPrimary = sdiDataBEI.getDataset("primary");
        DBUtil dbUtil = new DBUtil(sapphireConnection.getConnectionId());
        dbUtil.setConnection(sapphireConnection);
        StringBuilder completedPlanId = new StringBuilder();
        StringBuilder completedPlanItemId = new StringBuilder();
        StringBuilder scheduleRule = new StringBuilder();
        StringBuilder resetToZero = new StringBuilder();
        PreparedStatement planItemDetails = dbUtil.prepareStatement("planItemDetails", "SELECT calendarflag, schedulerule FROM scheduleplanitem WHERE scheduleplanid = ? AND scheduleplanitemid=?");
        for (int i = 0; i < newPrimary.size(); ++i) {
            String workOrderId = newPrimary.getValue(i, "workorderid");
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put("sdcid", "Instrument");
            if (InstrumentUtil.hasPrimaryValueChanged(newPrimary, oldPrimary, i, "workorderstatus")) {
                String workOrderType = newPrimary.getValue(i, "workordertype");
                SafeSQL safeSQL = new SafeSQL();
                String sqlWO = "select sourcekeyid1,workordertype from workorder where workorderid=" + safeSQL.addVar(workOrderId);
                DataSet woDS = qp.getPreparedSqlDataSet(sqlWO, safeSQL.getValues());
                if (workOrderType == null || workOrderType.trim().length() == 0) {
                    workOrderType = woDS.getValue(0, "workordertype", "");
                }
                if (workOrderType.equalsIgnoreCase(WORKORDERTYPE_CERTIFICATION)) {
                    String workOrderStatus = newPrimary.getValue(i, "workorderstatus");
                    String workOrderDisposition = newPrimary.getValue(i, "disposition");
                    String keyid1 = woDS.getValue(0, "sourcekeyid1");
                    hm.put("keyid1", keyid1);
                    String instrumentStatusToBeChanged = "";
                    String reason = "";
                    if (workOrderStatus.equalsIgnoreCase(STATUS_COMPLETE) && STATUS_ACCEPTED.equalsIgnoreCase(workOrderDisposition)) {
                        instrumentStatusToBeChanged = STATUS_AVAILABLE;
                        String planId = oldPrimary.getValue(i, "scheduleplanid");
                        String planItemID = oldPrimary.getValue(i, "scheduleplanitemid");
                        try {
                            planItemDetails.setString(1, planId);
                            planItemDetails.setString(2, planItemID);
                            DataSet planItemDetailsDs = new DataSet(planItemDetails.executeQuery());
                            String calendarFlag = planItemDetailsDs.getValue(0, "calendarflag");
                            if (calendarFlag.equals("N")) {
                                String rule = planItemDetailsDs.getValue(0, "schedulerule");
                                completedPlanId.append(";").append(planId);
                                completedPlanItemId.append(";").append(planItemID);
                                scheduleRule.append(";").append(rule);
                                resetToZero.append(";").append("N");
                            }
                        }
                        catch (SQLException e) {
                            throw new SapphireException("Sql exception getting planitem details");
                        }
                    }
                    if (STATUS_AVAILABLE.equalsIgnoreCase(instrumentStatusToBeChanged)) {
                        String sqlPendingWOs = "select count(1) from workorder where sourcesdcid='Instrument' and sourcekeyid1=? and workorderstatus='Pending' and outsidegraceperiodflag='Y'";
                        String sqlUnavailableParts = "select count(1) from instrument where parentinstrumentid=? and instrumentstatus=?";
                        if (qp.getPreparedCount(sqlPendingWOs, new Object[]{keyid1}) == 0 && qp.getPreparedCount(sqlUnavailableParts, new Object[]{keyid1, STATUS_UNAVAILABLE}) == 0) {
                            hm.put("instrumentstatus", STATUS_AVAILABLE);
                            hm.put("unavailabilityreason", reason);
                            ap.processAction("EditSDI", "1", hm);
                        }
                    } else if (STATUS_UNAVAILABLE.equalsIgnoreCase(instrumentStatusToBeChanged)) {
                        hm.put("instrumentstatus", STATUS_UNAVAILABLE);
                        hm.put("unavailabilityreason", reason);
                        ap.processAction("EditSDI", "1", hm);
                    }
                }
            }
            if (!InstrumentUtil.hasPrimaryValueChanged(newPrimary, oldPrimary, i, "outsidegraceperiodflag")) continue;
            String outSideGracePeriodFlag = newPrimary.getValue(i, "outsidegraceperiodflag");
            String workOrderType = newPrimary.getValue(i, "workordertype", "");
            if (outSideGracePeriodFlag == null || !outSideGracePeriodFlag.equalsIgnoreCase("Y")) continue;
            SafeSQL safeSQL = new SafeSQL();
            String sqlWO = "select workordertype,sourcekeyid1 from workorder where workorderid=" + safeSQL.addVar(workOrderId);
            DataSet woDS = qp.getPreparedSqlDataSet(sqlWO, safeSQL.getValues());
            if (workOrderType.trim().length() == 0) {
                workOrderType = woDS.getValue(0, "workordertype", "");
            }
            if (!workOrderType.equalsIgnoreCase(WORKORDERTYPE_CERTIFICATION)) continue;
            hm.put("keyid1", woDS.getValue(0, "sourcekeyid1"));
            hm.put("instrumentstatus", STATUS_UNAVAILABLE);
            hm.put("unavailabilityreason", "WorkOrder " + workOrderId + " is not completed with in grace period.");
            ap.processAction("EditSDI", "1", hm);
        }
        if (completedPlanId.length() > 0) {
            ScheduleService scheduleService = new ScheduleService(sapphireConnection);
            PropertyList propertyList = new PropertyList();
            propertyList.setProperty("scheduleplanid", completedPlanId.substring(1));
            propertyList.setProperty("scheduleplanitemid", completedPlanItemId.substring(1));
            propertyList.setProperty("schedulerule", scheduleRule.substring(1));
            propertyList.setProperty("resettozero", resetToZero.substring(1));
            scheduleService.resetCounter(propertyList);
        }
    }

    public static void copyCaptureOperationPropertiesFromTemplate(DataSet primary, PropertyList actionProps, String sdcid, QueryProcessor qp, DBAccess database) throws SapphireException {
        if (sdcid.equalsIgnoreCase("Instrument") || sdcid.equalsIgnoreCase("LV_InstrumentModel")) {
            String templateKeyId = actionProps.getProperty("templateid", actionProps.getProperty("templatekeyid1", ""));
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer sql = new StringBuffer("select keyid1,attachmentoperationid,propertyclob from sdiattachmentoperation");
            sql.append(" where sdcid=").append(safeSQL.addVar(sdcid));
            sql.append(" and keyid1=").append(safeSQL.addVar(templateKeyId));
            if (sdcid.equalsIgnoreCase("LV_InstrumentModel")) {
                sql.append(" and keyid2=").append(safeSQL.addVar(actionProps.getProperty("templatekeyid2", "")));
            }
            DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues(), true);
            DataSet updateds = new DataSet();
            updateds.addColumn("sdcid", 0);
            updateds.addColumn("keyid1", 0);
            updateds.addColumn("keyid2", 0);
            updateds.addColumn("keyid3", 0);
            updateds.addColumn("attachmentoperationid", 0);
            updateds.addColumn("propertyclob", 3);
            for (int t = 0; t < ds.size(); ++t) {
                for (int i = 0; i < primary.size(); ++i) {
                    int row = updateds.addRow();
                    updateds.setString(row, "sdcid", sdcid);
                    if (sdcid.equalsIgnoreCase("LV_InstrumentModel")) {
                        updateds.setString(row, "keyid1", primary.getString(i, "instrumentmodelid", ""));
                        updateds.setString(row, "keyid2", primary.getString(i, "instrumenttypeid", ""));
                        updateds.setString(row, "keyid3", "(null)");
                    } else {
                        updateds.setString(row, "keyid1", primary.getString(i, "instrumentid", ""));
                        updateds.setString(row, "keyid2", "(null)");
                        updateds.setString(row, "keyid3", "(null)");
                    }
                    updateds.setString(row, "attachmentoperationid", ds.getString(t, "attachmentoperationid", ""));
                    updateds.setClob(row, "propertyclob", ds.getClob(t, "propertyclob", ""));
                }
            }
            if (updateds.getRowCount() > 0) {
                try {
                    String[] keycols = new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "attachmentoperationid"};
                    DataSetUtil.update(database, updateds, "sdiattachmentoperation", keycols);
                }
                catch (Exception e) {
                    throw new SapphireException("Failed to attachmentoperation properties.", e);
                }
            }
        }
    }

    public boolean requiresBeforeEditImage() {
        return true;
    }

    public static boolean isDeptSecurityEnabled(SDCProcessor sdcProc, String sdcid) {
        DataSet columnDS = sdcProc.getColumnData(sdcid);
        return columnDS != null && columnDS.size() > 0 && columnDS.findRow("columnid", "securityuser") > -1 && columnDS.findRow("columnid", "securitydepartment") > -1;
    }
}

