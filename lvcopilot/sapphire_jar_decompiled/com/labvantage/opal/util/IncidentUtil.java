/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.util;

import com.labvantage.opal.handler.ErrorUtil;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class IncidentUtil {
    static final String LABVANTAGE_CVS_ID = "$Revision: 82252 $";

    public static void workorderCompleted(String workorderId, QueryProcessor qp, ActionProcessor ap) throws SapphireException {
        String sourcesdc = "";
        String sourcekeyid = "";
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select sourcesdcid,sourcekeyid1 from workorder where workorderid = " + safeSQL.addVar(workorderId);
        DataSet itemIds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
        sourcesdc = itemIds.getString(0, "sourcesdcid", "");
        sourcekeyid = itemIds.getString(0, "sourcekeyid1", "");
        if (sourcesdc.length() > 0 && sourcesdc.equals("LV_Incdt") && sourcekeyid.length() > 0) {
            IncidentUtil.rollupIncidentStatus(sourcekeyid, qp, ap);
        } else if (sourcesdc.length() > 0 && sourcesdc.equals("LV_ActionPlan") && sourcekeyid.length() > 0) {
            String incidentId;
            StringBuffer sbTotalWO = new StringBuffer();
            safeSQL.reset();
            sbTotalWO.append("select workorderid, workorderstatus from workorder where sourcesdcid='LV_ActionPlan' and sourcekeyid1 = ").append(safeSQL.addVar(sourcekeyid));
            DataSet dsTotalWO = qp.getPreparedSqlDataSet(sbTotalWO.toString(), safeSQL.getValues());
            HashMap<String, String> hmFilter = new HashMap<String, String>();
            hmFilter.put("workorderstatus", "Complete");
            DataSet dsCompletedWO = dsTotalWO.getFilteredDataSet(hmFilter);
            int totWOComplCount = dsCompletedWO.getRowCount();
            hmFilter.put("workorderstatus", "Cancelled");
            DataSet dsCancelledWO = dsTotalWO.getFilteredDataSet(hmFilter);
            int totWOCancelledCount = dsCancelledWO.getRowCount();
            safeSQL.reset();
            String sqlGetActionPlanStatus = "select actionplanstatus from actionplan where actionplanid = " + safeSQL.addVar(sourcekeyid);
            DataSet dsGetActionPlanStatus = qp.getPreparedSqlDataSet(sqlGetActionPlanStatus, safeSQL.getValues());
            String actionPlanStatus = dsGetActionPlanStatus.getValue(0, "actionplanstatus", "");
            if (dsTotalWO.getRowCount() == totWOComplCount + totWOCancelledCount && actionPlanStatus.equalsIgnoreCase("Approved")) {
                PropertyList plUpdateIncidentStatus = new PropertyList();
                plUpdateIncidentStatus.put("sdcid", "LV_ActionPlan");
                plUpdateIncidentStatus.put("keyid1", sourcekeyid);
                plUpdateIncidentStatus.put("actionplanstatus", "Completed");
                try {
                    ap.processAction("EditSDI", "1", plUpdateIncidentStatus);
                }
                catch (Exception ex) {
                    throw new SapphireException("DB_ACTION_FAILED", "Error :Exception generated on trying to Update ActionPlan Status:" + ErrorUtil.extractMessageFromException(ex, ErrorUtil.isUserAdmin(qp.getConnectionid())), ex);
                }
            }
            safeSQL.reset();
            String sqlIncidentId = "Select incidentid from actionplan where actionplanid = " + safeSQL.addVar(sourcekeyid);
            DataSet dsIncidentId = qp.getPreparedSqlDataSet(sqlIncidentId, safeSQL.getValues());
            if (dsIncidentId != null && dsIncidentId.getRowCount() > 0 && (incidentId = dsIncidentId.getString(0, "incidentid", "")).length() > 0) {
                IncidentUtil.rollupIncidentStatus(incidentId, qp, ap);
            }
        }
    }

    public static void actionPlanCancelled(String actionPlanid, QueryProcessor qp, ActionProcessor ap, PropertyList extraProps) throws SapphireException {
        StringBuffer sbIncompleteWorkorder = new StringBuffer();
        StringBuffer sbIncompleteWorkorderQuery = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sbIncompleteWorkorderQuery.append("select workorderid from workorder");
        sbIncompleteWorkorderQuery.append(" where workorderstatus in ('Scheduled','Pending') and sourcekeyid1 = ").append(safeSQL.addVar(actionPlanid));
        DataSet dsIncompleteWorkorder = qp.getPreparedSqlDataSet(sbIncompleteWorkorderQuery.toString(), safeSQL.getValues());
        if (dsIncompleteWorkorder.getRowCount() > 0) {
            for (int count = 0; count < dsIncompleteWorkorder.getRowCount(); ++count) {
                sbIncompleteWorkorder.append(";").append(dsIncompleteWorkorder.getString(count, "workorderid"));
            }
            PropertyList plIncompleteWorkorder = new PropertyList();
            plIncompleteWorkorder.put("sdcid", "WorkOrderSDC");
            plIncompleteWorkorder.put("keyid1", sbIncompleteWorkorder.toString().substring(1));
            plIncompleteWorkorder.put("workorderstatus", "Cancelled");
            plIncompleteWorkorder.putAll(extraProps);
            try {
                ap.processAction("EditSDI", "1", plIncompleteWorkorder);
            }
            catch (Exception ex) {
                throw new SapphireException("DB_ACTION_FAILED", "Error :Exception generated on trying to Update Workorder Status:" + ErrorUtil.extractMessageFromException(ex, ErrorUtil.isUserAdmin(qp.getConnectionid())), ex);
            }
        }
    }

    public static void incidentInvestigated(String incidentId, QueryProcessor qp, ActionProcessor ap) throws SapphireException {
        StringBuffer sbActionplanCount = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sbActionplanCount.append("select count(1) count from actionplan");
        sbActionplanCount.append(" where actionplanstatus = 'Completed' and  incidentid = ").append(safeSQL.addVar(incidentId));
        DataSet dsActionplanCount = qp.getPreparedSqlDataSet(sbActionplanCount.toString(), safeSQL.getValues());
        safeSQL.reset();
        StringBuffer sbTotalActionplanCount = new StringBuffer();
        sbTotalActionplanCount.append("select count(1) totalcount from actionplan ");
        sbTotalActionplanCount.append("where  incidentid = ").append(safeSQL.addVar(incidentId));
        DataSet dsTotalActionplanCount = qp.getPreparedSqlDataSet(sbTotalActionplanCount.toString(), safeSQL.getValues());
        StringBuffer sbApprovedActionPlan = new StringBuffer();
        safeSQL.reset();
        sbApprovedActionPlan.append("select count(1) countApprove from actionplan");
        sbApprovedActionPlan.append(" where actionplanstatus = 'Approved' and  incidentid = ").append(safeSQL.addVar(incidentId));
        DataSet dsApprovedActionPlan = qp.getPreparedSqlDataSet(sbApprovedActionPlan.toString(), safeSQL.getValues());
        PropertyList plIncidentStatus = new PropertyList();
        if (dsApprovedActionPlan.getInt(0, "countApprove") > 0) {
            plIncidentStatus.put("sdcid", "LV_Incdt");
            plIncidentStatus.put("keyid1", incidentId);
            plIncidentStatus.put("incidentstatus", "InProgress");
            try {
                ap.processAction("EditSDI", "1", plIncidentStatus);
            }
            catch (Exception ex) {
                throw new SapphireException("DB_ACTION_FAILED", "Error :Exception generated on trying to Update Incident Status:" + ErrorUtil.extractMessageFromException(ex, ErrorUtil.isUserAdmin(qp.getConnectionid())), ex);
            }
        }
    }

    public static void actionPlanApproved(String actionPlanId, QueryProcessor qp, ActionProcessor ap, PropertyList extraProps) throws SapphireException {
        StringBuffer sbIncident = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sbIncident.append("select incidentid,investigationreqflag,incidentstatus from incident");
        sbIncident.append(" where incidentid = (select incidentid from actionplan where actionplanid =").append(safeSQL.addVar(actionPlanId)).append(")");
        DataSet dsInvestigation = qp.getPreparedSqlDataSet(sbIncident.toString(), safeSQL.getValues());
        PropertyList plIncdtInprogressStatus = new PropertyList();
        if (dsInvestigation.getString(0, "incidentstatus").equals("Investigated") && dsInvestigation.getString(0, "investigationreqflag", "").equals("Y") || dsInvestigation.getString(0, "investigationreqflag", "").equals("N")) {
            plIncdtInprogressStatus.put("sdcid", "LV_Incdt");
            plIncdtInprogressStatus.put("keyid1", dsInvestigation.getString(0, "incidentid"));
            plIncdtInprogressStatus.put("incidentstatus", "InProgress");
        } else {
            plIncdtInprogressStatus.put("sdcid", "LV_Incdt");
            plIncdtInprogressStatus.put("keyid1", dsInvestigation.getString(0, "incidentid"));
            plIncdtInprogressStatus.put("incidentstatus", dsInvestigation.getString(0, "incidentstatus"));
        }
        try {
            plIncdtInprogressStatus.putAll(extraProps);
            ap.processAction("EditSDI", "1", plIncdtInprogressStatus);
        }
        catch (Exception ex) {
            throw new SapphireException("DB_ACTION_FAILED", "Error :Exception generated on trying to Update Incident Status:" + ErrorUtil.extractMessageFromException(ex, ErrorUtil.isUserAdmin(qp.getConnectionid())), ex);
        }
        boolean allactionplansareapproved = true;
        safeSQL.reset();
        String getactionplans = "select actionplanid, actionplanstatus from actionplan where incidentid = (select incidentid from actionplan where actionplanid =" + safeSQL.addVar(actionPlanId) + ")";
        DataSet actionplanvalues = qp.getPreparedSqlDataSet(getactionplans, safeSQL.getValues());
        for (int actplan = 0; actplan < actionplanvalues.getRowCount(); ++actplan) {
            String actplnstatus = actionplanvalues.getValue(actplan, "actionplanstatus", "");
            if (actplnstatus.equalsIgnoreCase("Approved")) continue;
            allactionplansareapproved = false;
            break;
        }
        if (allactionplansareapproved) {
            PropertyList pl = new PropertyList();
            pl.put("sdcid", "LV_Incdt");
            pl.put("keyid1", dsInvestigation.getString(0, "incidentid"));
            pl.put("incidentstatus", "InProgress");
            pl.putAll(extraProps);
            try {
                ap.processAction("EditSDI", "1", pl);
            }
            catch (Exception ex) {
                throw new SapphireException("DB_ACTION_FAILED", "Error :Exception generated on trying to Update Incident Status:" + ErrorUtil.extractMessageFromException(ex, ErrorUtil.isUserAdmin(qp.getConnectionid())), ex);
            }
        }
        StringBuffer sbWorkorderID = new StringBuffer();
        safeSQL.reset();
        sbWorkorderID.append("select workorderid,workorderstatus from workorder where sourcekeyid1 = ").append(safeSQL.addVar(actionPlanId));
        DataSet dsWorkorder = qp.getPreparedSqlDataSet(sbWorkorderID.toString(), safeSQL.getValues());
        StringBuffer sbWorkorder = new StringBuffer();
        if (dsWorkorder.getRowCount() > 0) {
            for (int k = 0; k < dsWorkorder.getRowCount(); ++k) {
                if (dsWorkorder.getString(k, "workorderstatus").equalsIgnoreCase("Complete")) continue;
                sbWorkorder.append(";").append(dsWorkorder.getString(k, "workorderid"));
            }
            if (sbWorkorder.length() > 0) {
                PropertyList plWorkorder = new PropertyList();
                plWorkorder.put("sdcid", "WorkOrderSDC");
                plWorkorder.put("copies", new Integer(dsWorkorder.getRowCount()));
                plWorkorder.put("keyid1", sbWorkorder.toString().substring(1));
                plWorkorder.put("workorderstatus", "Pending");
                plWorkorder.putAll(extraProps);
                try {
                    ap.processAction("EditSDI", "1", plWorkorder);
                }
                catch (Exception ex) {
                    throw new SapphireException("DB_ACTION_FAILED", "Error :Exception generated on trying to Update Workorder Status:" + ErrorUtil.extractMessageFromException(ex, ErrorUtil.isUserAdmin(qp.getConnectionid())), ex);
                }
            }
        }
    }

    public static void actionPlanCompleted(String actionPlanId, QueryProcessor qp, ActionProcessor ap) throws SapphireException {
        StringBuffer sbActionPlan = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sbActionPlan.append("select incidentid from actionplan where actionplanid = ").append(safeSQL.addVar(actionPlanId));
        DataSet dsActionPlan = qp.getPreparedSqlDataSet(sbActionPlan.toString(), safeSQL.getValues());
        String incidentId = dsActionPlan.getString(0, "incidentid");
        safeSQL.reset();
        IncidentUtil.rollupIncidentStatus(incidentId, qp, ap);
    }

    public static void actionPlanClosed(String actionplanId, QueryProcessor qp, ActionProcessor ap, PropertyList extraProps) throws SapphireException {
        int noClosedIncident = 0;
        int nototalIncident = 0;
        StringBuffer sbActionPlan = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sbActionPlan.append("select incidentid,actionplanid from actionplan where incidentid = (select incidentid from actionplan where actionplanid = ").append(safeSQL.addVar(actionplanId)).append(")");
        DataSet dsActionPlan = qp.getPreparedSqlDataSet(sbActionPlan.toString(), safeSQL.getValues());
        noClosedIncident = dsActionPlan.getRowCount();
        safeSQL.reset();
        StringBuffer sbTotalActionPlan = new StringBuffer();
        sbTotalActionPlan.append("select count(1) counttotalactionplan from actionplan ");
        sbTotalActionPlan.append("where actionplanstatus ='Closed' and incidentid = (select incidentid from actionplan where actionplanid = ").append(safeSQL.addVar(actionplanId)).append(")");
        DataSet dsTotalActionPlan = qp.getPreparedSqlDataSet(sbTotalActionPlan.toString(), safeSQL.getValues());
        nototalIncident = dsTotalActionPlan.getInt(0, "counttotalactionplan");
        if (nototalIncident == noClosedIncident) {
            PropertyList plCloseIncident = new PropertyList();
            plCloseIncident.put("sdcid", "LV_Incdt");
            plCloseIncident.put("keyid1", dsActionPlan.getString(0, "incidentid"));
            plCloseIncident.put("incidentstatus", "Completed");
            plCloseIncident.putAll(extraProps);
            try {
                ap.processAction("EditSDI", "1", plCloseIncident);
            }
            catch (Exception e) {
                throw new SapphireException("DB_ACTION_FAILED", "Error :Exception generated on trying to Update Incident Status:" + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(qp.getConnectionid())), e);
            }
        }
    }

    public static void incidentCancelled(String incidentId, QueryProcessor qp, ActionProcessor ap) throws SapphireException {
        StringBuffer workorderid = new StringBuffer();
        StringBuffer sbWorkorder = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sbWorkorder.append("Select workorderid from workorder where sourcekeyid1 = ").append(safeSQL.addVar(incidentId));
        DataSet dsWorkorder = qp.getPreparedSqlDataSet(sbWorkorder.toString(), safeSQL.getValues());
        if (dsWorkorder.getRowCount() > 0) {
            for (int count = 0; count < dsWorkorder.getRowCount(); ++count) {
                workorderid.append(";").append(dsWorkorder.getString(count, "workorderid"));
            }
            PropertyList plIncidentCancelledStatus = new PropertyList();
            plIncidentCancelledStatus.put("sdcid", "WorkOrderSDC");
            plIncidentCancelledStatus.put("copies", new Integer(dsWorkorder.getRowCount()));
            plIncidentCancelledStatus.put("keyid1", workorderid.toString().substring(1));
            plIncidentCancelledStatus.put("workorderstatus", "Cancelled");
            try {
                ap.processAction("EditSDI", "1", plIncidentCancelledStatus);
            }
            catch (Exception ex) {
                throw new SapphireException("DB_ACTION_FAILED", "Error :Exception generated on trying to Update workorder Status:" + ErrorUtil.extractMessageFromException(ex, ErrorUtil.isUserAdmin(qp.getConnectionid())), ex);
            }
        }
    }

    static void rollupIncidentStatus(String keyid1, QueryProcessor qp, ActionProcessor ap) throws SapphireException {
        int totWOCount = 0;
        int totWOComplCount = 0;
        int totApCount = 0;
        int totApComplCount = 0;
        int totApClosedCount = 0;
        SafeSQL safeSQL = new SafeSQL();
        DataSet dsIncident = qp.getPreparedSqlDataSet("select investigationreqflag, incidentstatus  from incident where incidentid = " + safeSQL.addVar(keyid1), safeSQL.getValues());
        if (dsIncident != null && dsIncident.getRowCount() > 0) {
            String investigationReqFlag = dsIncident.getValue(0, "investigationreqflag", "N");
            String incidentStatus = dsIncident.getValue(0, "incidentstatus", "");
            if (!incidentStatus.equalsIgnoreCase("Completed") && !incidentStatus.equalsIgnoreCase("Cancelled") && (investigationReqFlag.equalsIgnoreCase("N") || incidentStatus.equalsIgnoreCase("Investigated"))) {
                safeSQL.reset();
                DataSet dsTotalWO = qp.getPreparedSqlDataSet("select workorderid, workorderstatus from workorder where sourcekeyid1 = " + safeSQL.addVar(keyid1), safeSQL.getValues());
                totWOCount = dsTotalWO.getRowCount();
                HashMap<String, String> hmFilter = new HashMap<String, String>();
                hmFilter.put("workorderstatus", "Complete");
                DataSet dsCompletedWO = dsTotalWO.getFilteredDataSet(hmFilter);
                totWOComplCount = dsCompletedWO.getRowCount();
                hmFilter.put("workorderstatus", "Cancelled");
                DataSet dsCancelledWO = dsTotalWO.getFilteredDataSet(hmFilter);
                int totWOCancelledCount = dsCancelledWO.getRowCount();
                safeSQL.reset();
                DataSet dsTotalActionPlan = qp.getPreparedSqlDataSet("select actionplanstatus from actionplan where incidentid = " + safeSQL.addVar(keyid1), safeSQL.getValues());
                totApCount = dsTotalActionPlan.getRowCount();
                hmFilter = new HashMap();
                hmFilter.put("actionplanstatus", "Completed");
                DataSet dsCompleteActionPlan = dsTotalActionPlan.getFilteredDataSet(hmFilter);
                totApComplCount = dsCompleteActionPlan.getRowCount();
                hmFilter.put("actionplanstatus", "Closed");
                DataSet dsClosedActionPlan = dsTotalActionPlan.getFilteredDataSet(hmFilter);
                totApClosedCount = dsClosedActionPlan.getRowCount();
                hmFilter.put("actionplanstatus", "Cancelled");
                DataSet dsCancelledActionPlan = dsTotalActionPlan.getFilteredDataSet(hmFilter);
                int totAPCalcelledCnt = dsCancelledActionPlan.getRowCount();
                if (totWOComplCount + totWOCancelledCount == totWOCount && totApComplCount + totAPCalcelledCnt + totApClosedCount == totApCount) {
                    PropertyList plUpdateIncidentStatus = new PropertyList();
                    plUpdateIncidentStatus.put("sdcid", "LV_Incdt");
                    plUpdateIncidentStatus.put("keyid1", keyid1);
                    plUpdateIncidentStatus.put("incidentstatus", "Completed");
                    try {
                        ap.processAction("EditSDI", "1", plUpdateIncidentStatus);
                    }
                    catch (Exception ex) {
                        throw new SapphireException("PROCESSACTION_FAILED", "Error :Exception generated on trying to Update Incident Status:" + ErrorUtil.extractMessageFromException(ex, ErrorUtil.isUserAdmin(qp.getConnectionid())), ex);
                    }
                }
            }
        }
    }

    public static HashMap getIncidentDetailDatasets(String incidentId, QueryProcessor qp) {
        HashMap<String, DataSet> hmDatasets = new HashMap<String, DataSet>();
        DataSet dsIncident = new DataSet();
        DataSet dsIncident_Finding = new DataSet();
        DataSet dsIncident_ActionPlan = new DataSet();
        DataSet dsIncident_Workorder = new DataSet();
        DataSet dsFinding_ActionPlan = new DataSet();
        DataSet dsFinding_Workorder = new DataSet();
        DataSet dsIncident_ActionPlan_Workorder = new DataSet();
        DataSet dsFinding_ActionPlan_Workorder = new DataSet();
        String finding_actionPlans = "";
        String incident_findings = "";
        SafeSQL safeSQL = new SafeSQL();
        dsIncident = qp.getPreparedSqlDataSet("select * from incident where incidentid in (" + safeSQL.addIn(incidentId) + ") order by incidentid", safeSQL.getValues());
        safeSQL.reset();
        dsIncident_Finding = qp.getPreparedSqlDataSet("select * from incidentfind where incidentid in (" + safeSQL.addIn(incidentId) + ") order by incidentid, incidentfindid", safeSQL.getValues());
        if (dsIncident_Finding.getColumnValues("incidentfindid", "','").trim().length() > 0) {
            incident_findings = dsIncident_Finding.getColumnValues("incidentfindid", "','");
        }
        safeSQL.reset();
        dsIncident_ActionPlan = qp.getPreparedSqlDataSet("select * from actionplan where incidentid in (" + safeSQL.addIn(incidentId) + ") and (incidentfindid is null or incidentfindid = '') order by incidentid, actionplanid", safeSQL.getValues());
        String incident_actionPlans = dsIncident_ActionPlan.getColumnValues("actionplanid", "','");
        safeSQL.reset();
        dsIncident_Workorder = qp.getPreparedSqlDataSet("select * from workorder where sourcesdcid = 'LV_Incdt' and sourcekeyid1 in (" + safeSQL.addIn(incidentId) + ") order by sourcekeyid1, workorderid", safeSQL.getValues());
        if (incident_findings != null && incident_findings.trim().length() > 0) {
            safeSQL.reset();
            dsFinding_ActionPlan = qp.getPreparedSqlDataSet("select * from actionplan where incidentid in (" + safeSQL.addIn(incidentId) + ") and incidentfindid in (" + safeSQL.addIn(incident_findings) + ")  order by incidentid, incidentfindid, actionplanid", safeSQL.getValues());
            finding_actionPlans = dsFinding_ActionPlan.getColumnValues("actionplanid", "','");
            safeSQL.reset();
            dsFinding_Workorder = qp.getPreparedSqlDataSet("select * from workorder where sourcesdcid = 'LV_IncdtFind' and sourcekeyid1 in (" + safeSQL.addIn(incident_findings) + ")  order by sourcekeyid1, workorderid", safeSQL.getValues());
        }
        safeSQL.reset();
        dsIncident_ActionPlan_Workorder = qp.getPreparedSqlDataSet("select * from workorder where sourcesdcid = 'LV_ActionPlan' and sourcekeyid1 in (" + safeSQL.addIn(incident_actionPlans) + ")  order by sourcekeyid1, workorderid", safeSQL.getValues());
        if (finding_actionPlans != null && finding_actionPlans.trim().length() > 0) {
            safeSQL.reset();
            dsFinding_ActionPlan_Workorder = qp.getPreparedSqlDataSet("select * from workorder where sourcesdcid = 'LV_ActionPlan' and sourcekeyid1 in (" + safeSQL.addIn(finding_actionPlans) + ")  order by sourcekeyid1, workorderid", safeSQL.getValues());
        }
        hmDatasets.put("Incident", dsIncident);
        hmDatasets.put("Incident_Finding", dsIncident_Finding);
        hmDatasets.put("Incident_ActionPlan", dsIncident_ActionPlan);
        hmDatasets.put("Incident_Workorder", dsIncident_Workorder);
        hmDatasets.put("Finding_ActionPlan", dsFinding_ActionPlan);
        hmDatasets.put("Finding_Workorder", dsFinding_Workorder);
        hmDatasets.put("Incident_ActionPlan_Workorder", dsIncident_ActionPlan_Workorder);
        hmDatasets.put("Finding_ActionPlan_Workorder", dsFinding_ActionPlan_Workorder);
        return hmDatasets;
    }

    public static void workorderDeleted(String sdcid, String workorderId, String srcKeyId, QueryProcessor qp, ActionProcessor ap) throws SapphireException {
        if (sdcid.equals("LV_Incdt")) {
            DataSet dsActionPlan;
            DataSet dsWO;
            SafeSQL safeSQL = new SafeSQL();
            DataSet dsIncident = qp.getPreparedSqlDataSet("select investigationreqflag, incidentstatus  from incident where incidentid = " + safeSQL.addVar(srcKeyId), safeSQL.getValues());
            String investigationReqFlag = dsIncident.getValue(0, "investigationreqflag", "N");
            String incidentStatus = dsIncident.getValue(0, "incidentstatus", "");
            if (!incidentStatus.equalsIgnoreCase("Completed") && (investigationReqFlag.equalsIgnoreCase("N") || incidentStatus.equalsIgnoreCase("Investigated")) && (dsWO = qp.getPreparedSqlDataSet("select workorderid from workorder where sourcesdcid= 'LV_Incdt' and sourcekeyid1 = ? and workorderid != ? and workorderstatus not in('Complete', 'Cancelled')", new Object[]{srcKeyId, workorderId})).getRowCount() == 0 && (dsActionPlan = qp.getPreparedSqlDataSet("select actionplanid from actionplan where incidentid = ? and actionplanstatus not in('Completed', 'Cancelled' )", new Object[]{srcKeyId})).getRowCount() == 0) {
                PropertyList plUpdateIncidentStatus = new PropertyList();
                plUpdateIncidentStatus.put("sdcid", "LV_Incdt");
                plUpdateIncidentStatus.put("keyid1", srcKeyId);
                plUpdateIncidentStatus.put("incidentstatus", "Completed");
                try {
                    ap.processAction("EditSDI", "1", plUpdateIncidentStatus);
                }
                catch (Exception ex) {
                    throw new SapphireException("PROCESSACTION_FAILED", "Error :Exception generated on trying to Update Incident Status:" + ErrorUtil.extractMessageFromException(ex, ErrorUtil.isUserAdmin(qp.getConnectionid())), ex);
                }
            }
        } else if (sdcid.equals("LV_ActionPlan")) {
            DataSet dsWO;
            DataSet dsActionPlan = qp.getPreparedSqlDataSet("select incidentid, actionplanstatus from actionplan where actionplanid = ?", new Object[]{srcKeyId});
            String actionPlanStatus = dsActionPlan.getValue(0, "actionplanstatus", "");
            String incidentId = dsActionPlan.getString(0, "incidentid", "");
            if (actionPlanStatus.equalsIgnoreCase("Approved") && (dsWO = qp.getPreparedSqlDataSet("select workorderid from workorder where sourcesdcid='LV_ActionPlan' and sourcekeyid1 = ? and workorderid != ? and workorderstatus not in('Complete', 'Cancelled')", new Object[]{srcKeyId, workorderId})).getRowCount() == 0) {
                PropertyList plUpdateIncidentStatus = new PropertyList();
                plUpdateIncidentStatus.put("sdcid", "LV_ActionPlan");
                plUpdateIncidentStatus.put("keyid1", srcKeyId);
                plUpdateIncidentStatus.put("actionplanstatus", "Completed");
                try {
                    ap.processAction("EditSDI", "1", plUpdateIncidentStatus);
                }
                catch (Exception ex) {
                    throw new SapphireException("DB_ACTION_FAILED", "Error :Exception generated on trying to Update ActionPlan Status:" + ErrorUtil.extractMessageFromException(ex, ErrorUtil.isUserAdmin(qp.getConnectionid())), ex);
                }
            }
            if (incidentId.length() > 0) {
                IncidentUtil.rollupIncidentStatus(incidentId, qp, ap);
            }
        }
    }

    public static void actionPlanUnCancelled(String actionPlanid, QueryProcessor qp, ActionProcessor ap, PropertyList extraProps) throws SapphireException {
        StringBuffer allWorkorder = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        allWorkorder.append("select workorderid,workorderstatus from workorder where sourcekeyid1 = ").append(safeSQL.addVar(actionPlanid));
        DataSet dsWorkorder = qp.getPreparedSqlDataSet(allWorkorder.toString(), safeSQL.getValues());
        StringBuffer uncancelledWorkorder = new StringBuffer();
        if (dsWorkorder.getRowCount() > 0) {
            for (int k = 0; k < dsWorkorder.getRowCount(); ++k) {
                if (!dsWorkorder.getString(k, "workorderstatus").equalsIgnoreCase("Cancelled")) continue;
                uncancelledWorkorder.append(";").append(dsWorkorder.getString(k, "workorderid"));
            }
            if (uncancelledWorkorder.length() > 0) {
                PropertyList props = new PropertyList();
                props.put("sdcid", "WorkOrderSDC");
                props.put("keyid1", uncancelledWorkorder.toString().substring(1));
                props.put("statuscolumn", "workorderstatus");
                props.put("validatestatuscolumnvalue", "Cancelled");
                props.putAll(extraProps);
                try {
                    ap.processAction("UndoSDIColumnValue", "1", props);
                }
                catch (Exception ex) {
                    throw new SapphireException("DB_ACTION_FAILED", "Error :Exception generated on trying to Update Workorder Status:" + ErrorUtil.extractMessageFromException(ex, ErrorUtil.isUserAdmin(qp.getConnectionid())), ex);
                }
            }
        }
    }
}

