/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.IncidentUtil;
import com.labvantage.sapphire.util.sdiapproval.ApprovalUtil;
import java.sql.PreparedStatement;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LV_ActionPlan
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 82253 $";

    @Override
    public void preAdd(SDIData sdidata, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdidata.getDataset("primary");
        String actionPlanStatus = actionProps.getProperty("actionplanstatus");
        if (actionPlanStatus == null || actionPlanStatus.length() == 0) {
            actionPlanStatus = "Initial";
            primary.setValue(-1, "actionplanstatus", actionPlanStatus);
        }
    }

    @Override
    public void preEdit(SDIData sdidata, PropertyList actionProps) throws SapphireException {
        DataSet dsPrimary = sdidata.getDataset("primary");
        String actionPlanId = "";
        String actionPlanStatus = "";
        if (dsPrimary != null) {
            for (int i = 0; i < dsPrimary.size(); ++i) {
                actionPlanId = dsPrimary.getValue(i, "actionplanid", "");
                actionPlanStatus = dsPrimary.getValue(i, "actionplanstatus");
                if (actionPlanId.trim().length() <= 0 || actionPlanStatus.trim().length() <= 0 || !actionPlanStatus.equalsIgnoreCase("Approved")) continue;
                actionPlanStatus = this.checkStatusByWorkOrders(actionPlanStatus, actionPlanId);
                dsPrimary.setValue(i, "actionplanstatus", actionPlanStatus);
            }
        }
    }

    @Override
    public void postEdit(SDIData sdidata, PropertyList propertyList) throws SapphireException {
        DataSet dsPrimary = sdidata.getDataset("primary");
        String actionPlanId = "";
        String actionPlanStatus = "";
        PropertyList extraProps = new PropertyList();
        extraProps.setProperty("tracelogid", propertyList.getProperty("tracelogid", ""));
        extraProps.setProperty("auditreason", propertyList.getProperty("auditreason", ""));
        extraProps.setProperty("auditactivity", propertyList.getProperty("auditactivity", ""));
        extraProps.setProperty("auditsignedflag", propertyList.getProperty("auditsignedflag", ""));
        if (dsPrimary != null) {
            for (int i = 0; i < dsPrimary.size(); ++i) {
                actionPlanId = dsPrimary.getValue(i, "actionplanid", "");
                actionPlanStatus = dsPrimary.getValue(i, "actionplanstatus");
                if (actionPlanId.trim().length() <= 0) continue;
                if (actionPlanStatus.trim().length() > 0 && actionPlanStatus.equalsIgnoreCase("Approved")) {
                    IncidentUtil.actionPlanApproved(actionPlanId, this.getQueryProcessor(), this.getActionProcessor(), extraProps);
                    continue;
                }
                if (actionPlanStatus.trim().length() > 0 && actionPlanStatus.equalsIgnoreCase("Completed")) {
                    IncidentUtil.actionPlanCompleted(actionPlanId, this.getQueryProcessor(), this.getActionProcessor());
                    continue;
                }
                if (actionPlanStatus.trim().length() > 0 && actionPlanStatus.equalsIgnoreCase("Cancelled")) {
                    IncidentUtil.actionPlanCancelled(actionPlanId, this.getQueryProcessor(), this.getActionProcessor(), extraProps);
                    continue;
                }
                if (actionPlanStatus.trim().length() > 0 && actionPlanStatus.equalsIgnoreCase("Closed")) {
                    IncidentUtil.actionPlanClosed(actionPlanId, this.getQueryProcessor(), this.getActionProcessor(), extraProps);
                    continue;
                }
                if (actionPlanStatus.trim().length() <= 0 || !actionPlanStatus.equalsIgnoreCase("Initial") || !this.getOldPrimaryValue(dsPrimary, i, "actionplanstatus").equalsIgnoreCase("Cancelled")) continue;
                IncidentUtil.actionPlanUnCancelled(actionPlanId, this.getQueryProcessor(), this.getActionProcessor(), extraProps);
            }
        }
    }

    @Override
    public void postApprove(DataSet dsApproval) {
        try {
            DataSet approvedDS = ApprovalUtil.getSDIApprovalFlags(this.database, dsApproval);
            DataSet dsProp = new DataSet();
            for (int i = 0; i < approvedDS.size(); ++i) {
                String approvalFlag = approvedDS.getValue(i, "approvalflag");
                String actionplanStatus = "Pass".equalsIgnoreCase(approvalFlag) ? "Approved" : "Rejected";
                String keyid1 = approvedDS.getValue(i, "keyid1");
                actionplanStatus = this.checkStatusByWorkOrders(actionplanStatus, keyid1);
                int newRow = dsProp.addRow();
                dsProp.setString(newRow, "keyid1", keyid1);
                dsProp.setString(newRow, "actionplanstatus", actionplanStatus);
            }
            if (dsProp.size() > 0) {
                String sdcId = "LV_ActionPlan";
                ActionProcessor actionProcessor = this.getActionProcessor();
                PropertyList props = new PropertyList();
                props.put("sdcid", sdcId);
                props.put("keyid1", dsProp.getColumnValues("keyid1", ";"));
                props.put("actionplanstatus", dsProp.getColumnValues("actionplanstatus", ";"));
                props.put("tracelogid", dsApproval.getString(0, "tracelogid", ""));
                actionProcessor.processAction("EditSDI", "1", props);
            }
        }
        catch (Exception e) {
            Logger.logInfo("Exception occured in post approve rule :" + e.getMessage());
        }
    }

    private String checkStatusByWorkOrders(String oldActionPlanStatus, String actionPlanId) {
        String newActionPlanStatus = oldActionPlanStatus;
        if (oldActionPlanStatus.equals("Approved")) {
            boolean allWOComplete = true;
            QueryProcessor qp = this.getQueryProcessor();
            SafeSQL safeSQL = new SafeSQL();
            String sqlGetAllWOStatus = "select workorderstatus from workorder where sourcesdcid = 'LV_ActionPlan' and sourcekeyid1 = " + safeSQL.addVar(actionPlanId);
            DataSet dsGetAllWOStatus = qp.getPreparedSqlDataSet(sqlGetAllWOStatus, safeSQL.getValues());
            for (int j = 0; j < dsGetAllWOStatus.getRowCount(); ++j) {
                String wostatus = dsGetAllWOStatus.getValue(j, "workorderstatus", "");
                if (wostatus.equalsIgnoreCase("Complete")) continue;
                allWOComplete = false;
                break;
            }
            if (allWOComplete) {
                newActionPlanStatus = "Completed";
            }
        }
        return newActionPlanStatus;
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        this.deleteActionPlan(rsetid, actionProps);
    }

    void deleteActionPlan(String rsetid, PropertyList actionProps) throws SapphireException {
        String keyid1 = actionProps.getProperty("keyid1");
        String[] keys = StringUtil.split(keyid1, ";");
        String sql = "delete from workorder where sourcesdcid='LV_ActionPlan' and sourcekeyid1 = ?";
        try {
            PreparedStatement statement = this.database.prepareStatement(sql);
            statement.setString(1, keys[0]);
            statement.executeUpdate();
        }
        catch (Exception ex) {
            throw new SapphireException("DB_ACTION_FAILED", "Error :Exception generated on trying to delete workorder associated with the actionplan :" + ErrorUtil.extractMessageFromException(ex, ErrorUtil.isUserAdmin(this.getConnectionId())), ex);
        }
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }
}

