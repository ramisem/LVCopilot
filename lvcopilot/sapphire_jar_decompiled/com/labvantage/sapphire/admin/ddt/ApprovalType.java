/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class ApprovalType
extends BaseSDCRules {
    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            String intendeduse = primary.getString(i, "intendeduse", "");
            if (!this.hasPrimaryValueChanged(primary, i, "intendeduse") || intendeduse.trim().length() != 0) continue;
            String approvaltypeid = primary.getString(i, "approvaltypeid", "");
            this.deleteApprovalRules(approvaltypeid, "");
        }
    }

    @Override
    public void postEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.removeBlankApprovalRules(sdiData);
    }

    @Override
    public void postAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.removeBlankApprovalRules(sdiData);
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    private void removeBlankApprovalRules(SDIData sdiData) throws SapphireException {
        DataSet approvaltyperule = sdiData.getDataset("approvaltyperule");
        if (approvaltyperule != null) {
            int rowCount = approvaltyperule.getRowCount();
            StringBuilder typeid = new StringBuilder();
            StringBuilder typeruleid = new StringBuilder();
            for (int i = 0; i < rowCount; ++i) {
                String rulename = approvaltyperule.getString(i, "rulename", "");
                String approvaltypeid = approvaltyperule.getString(i, "approvaltypeid", "");
                String approvaltyperuleid = approvaltyperule.getString(i, "approvaltyperuleid", "");
                if (rulename.trim().length() != 0) continue;
                typeid.append(";").append(approvaltypeid);
                typeruleid.append(";").append(approvaltyperuleid);
            }
            if (typeid.length() > 0) {
                this.deleteApprovalRules(typeid.substring(1), typeruleid.substring(1));
            }
        }
    }

    private void deleteApprovalRules(String approvaltypeid, String approvaltyperuleid) throws SapphireException {
        if (approvaltypeid.length() > 0) {
            PropertyList deleteSDIDetail = new PropertyList();
            deleteSDIDetail.setProperty("linkid", "approval type rule");
            deleteSDIDetail.setProperty("sdcid", "ApprovalType");
            deleteSDIDetail.setProperty("approvaltypeid", approvaltypeid);
            if (approvaltyperuleid.length() > 0) {
                deleteSDIDetail.setProperty("approvaltyperuleid", approvaltyperuleid);
            }
            this.getActionProcessor().processAction("DeleteSDIDetail", "1", deleteSDIDetail);
        }
    }
}

