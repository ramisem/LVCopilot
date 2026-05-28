/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdiapproval;

import com.labvantage.sapphire.actions.sdiapproval.ApprovalRuleUtil;
import java.sql.PreparedStatement;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class CheckDSApprovalRule
extends BaseAction
implements sapphire.action.CheckDSApprovalRule {
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
        String paramlistid = properties.getProperty("paramlistid", "");
        String paramlistversionid = properties.getProperty("paramlistversionid", "");
        String variantid = properties.getProperty("variantid", "");
        String dataset = properties.getProperty("dataset", "");
        PreparedStatement apRulesStmnt = this.database.prepareStatement("aprules", ApprovalRuleUtil.getSDIDataAPRulesSQL());
        PreparedStatement apStepsStmnt = this.database.prepareStatement("apsteps", ApprovalRuleUtil.getSDIDataAPStepsSQL());
        String[] keyid1Arr = StringUtil.split(keyid1, ";");
        String[] keyid2Arr = StringUtil.split(keyid2, ";");
        String[] keyid3Arr = StringUtil.split(keyid3, ";");
        String[] paramlistidArr = StringUtil.split(paramlistid, ";");
        String[] paramlistversionidArr = StringUtil.split(paramlistversionid, ";");
        String[] variantidArr = StringUtil.split(variantid, ";");
        String[] datasetArr = StringUtil.split(dataset, ";");
        PropertyList primarykeyProps = new PropertyList();
        PropertyList autoApprovedSDIProps = new PropertyList();
        PropertyList sampleProps = new PropertyList();
        PropertyList nonAutoApprovedSDIProps = new PropertyList();
        DataSet nonAutoApprovedSteps = new DataSet();
        DataSet autoApprovedSteps = new DataSet();
        ApprovalRuleUtil.populateColumns(nonAutoApprovedSteps, true, false);
        ApprovalRuleUtil.populateColumns(autoApprovedSteps, true, true);
        try {
            for (int i = 0; i < keyid1Arr.length; ++i) {
                int r;
                apRulesStmnt.setString(1, paramlistidArr[i]);
                apRulesStmnt.setString(2, paramlistversionidArr[i]);
                apRulesStmnt.setString(3, variantidArr[i]);
                DataSet apRulesDS = new DataSet(apRulesStmnt.executeQuery());
                if (apRulesDS.getRowCount() <= 0) continue;
                apRulesDS.setString(-1, "approvalrulestatus", "");
                HashMap<String, String> ruleNameMap = new HashMap<String, String>();
                HashMap<String, String> failureMsgMap = new HashMap<String, String>();
                apStepsStmnt.setString(1, sdcid);
                apStepsStmnt.setString(2, keyid1Arr[i]);
                apStepsStmnt.setString(3, keyid2Arr[i]);
                apStepsStmnt.setString(4, keyid3Arr[i]);
                apStepsStmnt.setString(5, paramlistidArr[i]);
                apStepsStmnt.setString(6, paramlistversionidArr[i]);
                apStepsStmnt.setString(7, variantidArr[i]);
                apStepsStmnt.setString(8, datasetArr[i]);
                DataSet apStepsDS = new DataSet(apStepsStmnt.executeQuery());
                String[] stepsArr = StringUtil.split(apStepsDS.getColumnValues("approvalstep", ","), ",");
                primarykeyProps.clear();
                ApprovalRuleUtil.populateProps(primarykeyProps, sdcid, keyid1Arr[i], keyid2Arr[i], keyid3Arr[i], paramlistidArr[i], paramlistversionidArr[i], variantidArr[i], datasetArr[i]);
                ApprovalRuleUtil.evaluateAPRules(apRulesDS, primarykeyProps, this.getQueryProcessor(), true);
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("approvalrulestatus", "F");
                DataSet failedAPRule = apRulesDS.getFilteredDataSet(hm);
                if (failedAPRule.getRowCount() > 0) {
                    for (r = 0; r < failedAPRule.size(); ++r) {
                        String rulename = failedAPRule.getString(r, "rulename", "");
                        String failuremessage = failedAPRule.getString(r, "failuremessage", "");
                        String approvalstep = failedAPRule.getString(r, "approvalstep", "");
                        if (approvalstep.trim().length() > 0) {
                            ApprovalRuleUtil.updateMap(ruleNameMap, approvalstep, rulename, "||");
                            ApprovalRuleUtil.updateMap(failureMsgMap, approvalstep, failuremessage, "||");
                            continue;
                        }
                        for (String value : stepsArr) {
                            ApprovalRuleUtil.updateMap(ruleNameMap, value, rulename, "||");
                            ApprovalRuleUtil.updateMap(failureMsgMap, value, failuremessage, "||");
                        }
                    }
                    ApprovalRuleUtil.populateProps(nonAutoApprovedSDIProps, sdcid, keyid1Arr[i], keyid2Arr[i], keyid3Arr[i], paramlistidArr[i], paramlistversionidArr[i], variantidArr[i], datasetArr[i], "s_datasetstatus", "Released", "", "", "");
                } else {
                    ApprovalRuleUtil.populateProps(sampleProps, sdcid, keyid1Arr[i], keyid2Arr[i], keyid3Arr[i]);
                    ApprovalRuleUtil.populateProps(autoApprovedSDIProps, sdcid, keyid1Arr[i], keyid2Arr[i], keyid3Arr[i], paramlistidArr[i], paramlistversionidArr[i], variantidArr[i], datasetArr[i], "s_datasetstatus", "Completed", "Completed", "approvedby", "approveddt");
                }
                for (r = 0; r < apStepsDS.size(); ++r) {
                    String step = apStepsDS.getString(r, "approvalstep", "");
                    if (ruleNameMap.containsKey(step)) {
                        String rulename = ruleNameMap.get(step);
                        String failuremessage = failureMsgMap.get(step);
                        ApprovalRuleUtil.addToDS(nonAutoApprovedSteps, sdcid, keyid1Arr[i], keyid2Arr[i], keyid3Arr[i], paramlistidArr[i], paramlistversionidArr[i], variantidArr[i], datasetArr[i], step, rulename, failuremessage);
                        continue;
                    }
                    ApprovalRuleUtil.addToDS(autoApprovedSteps, sdcid, keyid1Arr[i], keyid2Arr[i], keyid3Arr[i], paramlistidArr[i], paramlistversionidArr[i], variantidArr[i], datasetArr[i], step, "P");
                }
            }
        }
        catch (Exception e) {
            this.logger.error("Failed to check Approval Rule for DataSet-->" + e.getMessage());
        }
        finally {
            this.database.closeStatement("aprules");
            this.database.closeStatement("apsteps");
        }
        if (nonAutoApprovedSDIProps.size() > 0) {
            nonAutoApprovedSDIProps.setProperty("skipapprovalrulecheck", "Y");
            this.getActionProcessor().processAction("EditDataSet", "1", nonAutoApprovedSDIProps);
        }
        if (autoApprovedSDIProps.size() > 0) {
            ActionBlock ab = new ActionBlock();
            autoApprovedSDIProps.setProperty("propsmatch", "Y");
            ab.setAction("EditDataSet", "EditDataSet", "1", autoApprovedSDIProps);
            ab.setAction("SyncSDIWIStatus", "SyncSDIWIStatus", "1", sampleProps);
            ab.setAction("SyncSDIDataSetStatus", "SyncSDIDataSetStatus", "1", sampleProps);
            this.getActionProcessor().processActionBlock(ab);
        }
        if (autoApprovedSteps.getRowCount() > 0) {
            ApprovalRuleUtil.updateSDIDataApprovalSteps(this.database, autoApprovedSteps);
        }
        if (nonAutoApprovedSteps.getRowCount() > 0) {
            ApprovalRuleUtil.updateSDIDataApprovalSteps(this.database, nonAutoApprovedSteps);
        }
    }
}

