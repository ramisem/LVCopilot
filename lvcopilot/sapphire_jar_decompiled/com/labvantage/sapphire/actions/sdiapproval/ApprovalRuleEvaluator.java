/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdiapproval;

import java.util.ArrayList;
import java.util.Iterator;

public class ApprovalRuleEvaluator
extends ArrayList {
    public void addStep(String approvalFlag, String mandatoryFlag) {
        this.add(new Step(approvalFlag, mandatoryFlag));
    }

    public String evaluateRule(String passrule, boolean honorAllMandatoryApprovalSteps) {
        String value = "P";
        Iterator it = this.iterator();
        boolean flagDetermined = false;
        boolean allStepsUndetermined = true;
        if (this.size() > 0) {
            value = passrule.equals("1P") || passrule.equals("1MP") ? "F" : "P";
            for (int row = 0; row < this.size() && !flagDetermined; ++row) {
                Step step = (Step)this.get(row);
                if (step.approvalflag == null) {
                    step.approvalflag = "U";
                }
                if (honorAllMandatoryApprovalSteps && step.mandatoryflag && step.approvalflag.equals("U")) {
                    value = "U";
                    flagDetermined = true;
                }
                if (step.approvalflag.equals("U")) continue;
                allStepsUndetermined = false;
            }
            if (!flagDetermined && allStepsUndetermined) {
                value = "U";
                flagDetermined = true;
            }
            while (it.hasNext() && !flagDetermined) {
                Step step = (Step)it.next();
                if (passrule.equals("AP")) {
                    if (step.approvalflag.equals("F")) {
                        value = "F";
                        flagDetermined = true;
                        continue;
                    }
                    if (!step.approvalflag.equals("U")) continue;
                    value = "U";
                    continue;
                }
                if (passrule.equals("AMP")) {
                    if (step.mandatoryflag && step.approvalflag.equals("F")) {
                        value = "F";
                        flagDetermined = true;
                        continue;
                    }
                    if (!step.mandatoryflag || !step.approvalflag.equals("U")) continue;
                    value = "U";
                    continue;
                }
                if (passrule.equals("1P")) {
                    if (step.approvalflag.equals("P")) {
                        value = "P";
                        flagDetermined = true;
                        continue;
                    }
                    if (!step.mandatoryflag || !step.approvalflag.equals("U")) continue;
                    value = "U";
                    continue;
                }
                if (passrule.equals("1MP")) {
                    if (step.mandatoryflag && step.approvalflag.equals("P")) {
                        value = "P";
                        flagDetermined = true;
                        continue;
                    }
                    if (!step.mandatoryflag || !step.approvalflag.equals("U")) continue;
                    value = "U";
                    continue;
                }
                if (passrule.equals("NF")) {
                    if (step.approvalflag.equals("F")) {
                        value = "F";
                        flagDetermined = true;
                        continue;
                    }
                    if (!step.mandatoryflag || !step.approvalflag.equals("U")) continue;
                    value = "U";
                    continue;
                }
                if (!passrule.equals("NMF")) continue;
                if (step.mandatoryflag && step.approvalflag.equals("F")) {
                    value = "F";
                    flagDetermined = true;
                    continue;
                }
                if (!step.mandatoryflag || !step.approvalflag.equals("U")) continue;
                value = "U";
            }
        } else {
            value = "P";
        }
        return value;
    }

    private class Step {
        String approvalflag;
        boolean mandatoryflag;

        public Step(String approvalFlag, String mandatoryFlag) {
            if (approvalFlag == null || approvalFlag.length() == 0) {
                approvalFlag = "U";
            }
            this.mandatoryflag = "Y".equals(mandatoryFlag);
            this.approvalflag = approvalFlag;
        }
    }
}

