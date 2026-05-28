/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.util.sdiapproval.ApprovalUtil;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LV_StudySuiteSDC
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54303 $";

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (!actionProps.containsKey("__sdcruleconfirm")) {
            this.checkIfApprovalExists(sdiData.getDataset("primary"));
        }
    }

    @Override
    public void postApprove(DataSet dsApproval) throws SapphireException {
        try {
            DataSet approvedDS = ApprovalUtil.getSDIApprovalFlags(this.database, dsApproval);
            DataSet dsProp = new DataSet();
            for (int i = 0; i < approvedDS.size(); ++i) {
                int newRow = dsProp.addRow();
                String approvalFlag = approvedDS.getValue(i, "approvalflag");
                String studysuiteStatus = "Pass".equalsIgnoreCase(approvalFlag) ? "N" : "R";
                dsProp.setString(newRow, "keyid1", approvedDS.getValue(i, "keyid1"));
                dsProp.setString(newRow, "studysuitestatus", studysuiteStatus);
            }
            if (dsProp.size() > 0) {
                String sdcId = "LV_StudySuiteSDC";
                ActionProcessor actionProcessor = this.getActionProcessor();
                PropertyList props = new PropertyList();
                props.put("sdcid", sdcId);
                props.put("keyid1", dsProp.getColumnValues("keyid1", ";"));
                props.put("studysuitestatus", dsProp.getColumnValues("studysuitestatus", ";"));
                actionProcessor.processAction("EditSDI", "1", props);
            }
        }
        catch (Exception e) {
            Logger.logInfo("Exception occured in post approve rule :" + e.getMessage());
        }
    }

    private void checkIfApprovalExists(DataSet primary) throws SapphireException {
        for (int i = 0; i < primary.size(); ++i) {
            String studySuiteId;
            if (!this.hasPrimaryValueChanged(primary, i, "studysuitestatus") || !"P".equalsIgnoreCase(primary.getValue(i, "studysuitestatus", "")) || this.database.getPreparedCount("select count(1) from sdiapproval where sdcid = 'LV_StudySuiteSDC' and keyid1 = ?", new Object[]{studySuiteId = primary.getValue(i, "studysuiteid", "")}) != 0) continue;
            throw new SapphireException("Confirm Submit For Approval", "CONFIRM", this.getTranslationProcessor().translate("No approval type is associated with the Study Suite. Continue anyway?"));
        }
    }
}

