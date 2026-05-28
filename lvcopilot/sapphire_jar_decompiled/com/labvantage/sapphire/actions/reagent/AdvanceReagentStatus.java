/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.reagent;

import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class AdvanceReagentStatus
extends BaseAction
implements sapphire.action.AdvanceReagentStatus {
    public static final String SDCID = "LV_ReagentLot";
    private static final String STATUS_PENDINGAPPROVAL = "PendingApproval";
    private static final String STATUS_ACTIVE = "Active";
    private static final String STATUS_REJECTED = "Rejected";
    public static final String APPROVALSTATUS_PASSED = "passed";
    public static final String APPROVALSTATUS_FAILED = "failed";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String reagentlotid = properties.getProperty("reagentlotid");
        String isQualitySampleTested = properties.getProperty("qualitysampletested");
        String approvalStatus = properties.getProperty("approvalstatus");
        String sqlApprovalType = "SELECT  sdiapp.approvaltypeid approvaltypeid, lot.qualitysamplereqflag qualitysamplereqflag, lot.reagentstatus reagentstatus  FROM reagentlot lot left join sdiapproval sdiapp on sdiapp.sdcid='LV_ReagentLot' and sdiapp.keyid1=lot.reagentlotid  WHERE lot.reagentlotid=? ";
        this.database.createPreparedResultSet(sqlApprovalType, new Object[]{reagentlotid});
        if (this.database.getNext()) {
            String approvalTypeid = this.database.getValue("approvaltypeid");
            String qualitySampleRequired = this.database.getValue("qualitysamplereqflag");
            String oldReagentStatus = this.database.getValue("reagentstatus");
            String newReagentStatus = this.database.getValue("reagentstatus");
            if (approvalTypeid.trim().length() == 0 && "N".equalsIgnoreCase(qualitySampleRequired)) {
                newReagentStatus = STATUS_ACTIVE;
            } else if (approvalTypeid.trim().length() > 0 && "N".equalsIgnoreCase(qualitySampleRequired) && APPROVALSTATUS_PASSED.equalsIgnoreCase(approvalStatus)) {
                newReagentStatus = STATUS_ACTIVE;
            } else if (approvalTypeid.trim().length() > 0 && "N".equalsIgnoreCase(qualitySampleRequired) && APPROVALSTATUS_FAILED.equalsIgnoreCase(approvalStatus)) {
                newReagentStatus = STATUS_REJECTED;
            } else if (approvalTypeid.trim().length() == 0 && "Y".equalsIgnoreCase(qualitySampleRequired) && "Y".equalsIgnoreCase(isQualitySampleTested)) {
                newReagentStatus = STATUS_ACTIVE;
            } else if (approvalTypeid.trim().length() > 0 && "Y".equalsIgnoreCase(qualitySampleRequired) && "Y".equalsIgnoreCase(isQualitySampleTested)) {
                newReagentStatus = STATUS_PENDINGAPPROVAL;
            } else if (approvalTypeid.trim().length() > 0 && STATUS_PENDINGAPPROVAL.equalsIgnoreCase(newReagentStatus) && APPROVALSTATUS_PASSED.equalsIgnoreCase(approvalStatus)) {
                newReagentStatus = STATUS_ACTIVE;
            } else if (approvalTypeid.trim().length() > 0 && STATUS_PENDINGAPPROVAL.equalsIgnoreCase(newReagentStatus) && APPROVALSTATUS_FAILED.equalsIgnoreCase(approvalStatus)) {
                newReagentStatus = STATUS_REJECTED;
            }
            if (!oldReagentStatus.equalsIgnoreCase(newReagentStatus)) {
                try {
                    PropertyList reagentStatusProps = new PropertyList();
                    reagentStatusProps.setProperty("sdcid", SDCID);
                    reagentStatusProps.setProperty("keyid1", reagentlotid);
                    reagentStatusProps.setProperty("reagentstatus", newReagentStatus);
                    this.getActionProcessor().processAction("EditSDI", "1", reagentStatusProps);
                }
                catch (Exception e) {
                    throw new SapphireException("Not Able to set Reagent Status " + e);
                }
            }
        }
    }
}

