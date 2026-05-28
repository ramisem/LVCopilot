/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdiapproval;

import com.labvantage.sapphire.actions.sdiapproval.ApprovalRuleUtil;
import java.sql.PreparedStatement;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class CheckSDIApprovalRule
extends BaseAction
implements sapphire.action.CheckSDIApprovalRule {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        ApprovalRuleUtil.setIsOracle(this.database.isOracle());
        String sdcid = properties.getProperty("sdcid", "");
        String keyid1 = properties.getProperty("keyid1", "");
        String keyid2 = properties.getProperty("keyid2", "");
        String keyid3 = properties.getProperty("keyid3", "");
        String statuscolumn = properties.getProperty("statuscolumn", "");
        String approvedstatus = properties.getProperty("approvedstatus", "");
        String pendingapprovalstatus = properties.getProperty("pendingapprovalstatus", "");
        String approvedbycolumn = properties.getProperty("approvedbycolumn", "");
        String approveddtcolumn = properties.getProperty("approveddtcolumn", "");
        PreparedStatement apRulesStmnt = this.database.prepareStatement("aprules", ApprovalRuleUtil.getSDIAPRulesSQL());
        PreparedStatement apStepsStmnt = this.database.prepareStatement("apsteps", ApprovalRuleUtil.getSDIAPStepsSQL(sdcid));
        String[] keyid1Arr = StringUtil.split(keyid1, ";");
        String[] keyid2Arr = StringUtil.split(keyid2, ";");
        String[] keyid3Arr = StringUtil.split(keyid3, ";");
        PropertyList autoApprovedSDIProps = new PropertyList();
        PropertyList nonAutoApprovedSDIProps = new PropertyList();
        DataSet nonAutoApprovedSteps = new DataSet();
        DataSet autoApprovedSteps = new DataSet();
        DataSet sdiApprovalDS = new DataSet();
        ApprovalRuleUtil.populateColumns(nonAutoApprovedSteps, false, false);
        ApprovalRuleUtil.populateColumns(autoApprovedSteps, false, true);
        ApprovalRuleUtil.populateSDIApprovalColumns(sdiApprovalDS);
        try {
            for (int i = 0; i < keyid1Arr.length; ++i) {
                String key1 = keyid1Arr[i];
                String key2 = keyid2Arr[i];
                String key3 = keyid3Arr[i];
                apRulesStmnt.setString(1, sdcid);
                apRulesStmnt.setString(2, key1);
                apRulesStmnt.setString(3, key2);
                apRulesStmnt.setString(4, key3);
                DataSet apRulesDS = new DataSet(apRulesStmnt.executeQuery());
                if (apRulesDS.getRowCount() <= 0) continue;
                apRulesDS.setString(-1, "approvalrulestatus", "U");
                ApprovalRuleUtil.SDIApprovalInfo sdiApprovalInfo = new ApprovalRuleUtil.SDIApprovalInfo(sdcid, key1, key2, key3, statuscolumn, pendingapprovalstatus, approvedstatus, approvedbycolumn, approveddtcolumn);
                ApprovalRuleUtil.evaluateSDIApprovalRules(sdiApprovalInfo, apRulesDS, apStepsStmnt, nonAutoApprovedSteps, autoApprovedSteps, sdiApprovalDS, nonAutoApprovedSDIProps, autoApprovedSDIProps, null, -1, this.connectionInfo);
            }
            ActionBlock ab = new ActionBlock();
            boolean abSet = false;
            if (nonAutoApprovedSDIProps.size() > 0) {
                nonAutoApprovedSDIProps.setProperty("skipapprovalrulecheck", "Y");
                ab.setAction("NonAutoApproved", "EditSDI", "1", nonAutoApprovedSDIProps);
                abSet = true;
            }
            if (autoApprovedSDIProps.size() > 0) {
                ab.setAction("AutoApproved", "EditSDI", "1", autoApprovedSDIProps);
                abSet = true;
            }
            if (sdiApprovalDS.getRowCount() > 0) {
                ApprovalRuleUtil.updateSDIApproval(this.database, sdiApprovalDS);
                abSet = true;
            }
            if (abSet) {
                this.getActionProcessor().processActionBlock(ab);
            }
            if (nonAutoApprovedSteps.getRowCount() > 0) {
                ApprovalRuleUtil.updateSDIApprovalSteps(this.database, nonAutoApprovedSteps);
            }
            if (autoApprovedSteps.getRowCount() > 0) {
                ApprovalRuleUtil.updateSDIApprovalSteps(this.database, autoApprovedSteps);
            }
        }
        catch (Exception e) {
            this.logger.error("Failed to check Approval Rule for SDI-->" + e.getMessage());
        }
        finally {
            this.database.closeStatement("aprules");
            this.database.closeStatement("apsteps");
        }
    }
}

