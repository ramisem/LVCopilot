/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.gwt.server;

import sapphire.util.DataSet;

public class ApprovalStepUtil {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    public static void checkApprovalSteps(DataSet currentapproval, String rolelist, String sysuserid, String approvalsequenceflag, String uniquenessflag, DataSet forcepeerCheckDataSet, String forcepeerCheckColumnid) {
        ApprovalStepUtil.checkApprovalSteps(currentapproval, rolelist, sysuserid, approvalsequenceflag, uniquenessflag, forcepeerCheckDataSet, forcepeerCheckColumnid, true);
    }

    public static void checkApprovalSteps(DataSet currentapproval, String rolelist, String sysuserid, String approvalsequenceflag, String uniquenessflag, DataSet forcepeerCheckDataSet, String forcepeerCheckColumnid, boolean allowCurrentStepApproval) {
        currentapproval.sort("usersequence");
        int firstMatchStep = ApprovalStepUtil.checkRoles(currentapproval, rolelist, uniquenessflag, sysuserid, allowCurrentStepApproval);
        if (firstMatchStep >= 0 && "Y".equals(approvalsequenceflag)) {
            if (ApprovalStepUtil.checkSequential(currentapproval, firstMatchStep) && "Y".equals(uniquenessflag)) {
                ApprovalStepUtil.checkUniqueness(currentapproval, firstMatchStep, sysuserid);
            }
        } else if ("Y".equals(uniquenessflag)) {
            ApprovalStepUtil.checkUniqueness(currentapproval, firstMatchStep, sysuserid);
        }
        ApprovalStepUtil.checkForcePeer(currentapproval, forcepeerCheckDataSet, forcepeerCheckColumnid, sysuserid);
    }

    private static int checkRoles(DataSet currentapproval, String rolelist, String uniquenessflag, String sysuserid, boolean allowCurrentStepApproval) {
        int firstMatch = -1;
        for (int step = 0; step < currentapproval.getRowCount(); ++step) {
            boolean isDoneByDifferentUser;
            String roleid = currentapproval.getValue(step, "roleid");
            String approvalflag = currentapproval.getValue(step, "approvalflag");
            boolean bl = isDoneByDifferentUser = ("P".equals(approvalflag) || "F".equals(approvalflag)) && !sysuserid.equals(currentapproval.getValue(step, "modby"));
            if (!isDoneByDifferentUser && (";" + rolelist + ";").indexOf(";" + roleid + ";") > -1) {
                if (firstMatch < 0) {
                    if (!allowCurrentStepApproval && ("P".equals(approvalflag) || "F".equals(approvalflag))) {
                        currentapproval.setValue(step, "stepstatusflag", "A");
                        if (!"Y".equals(uniquenessflag)) continue;
                        firstMatch = step;
                        continue;
                    }
                    currentapproval.setValue(step, "stepstatusflag", "C");
                    firstMatch = step;
                    continue;
                }
                currentapproval.setValue(step, "stepstatusflag", "V");
                continue;
            }
            currentapproval.setValue(step, "stepstatusflag", "W");
        }
        return firstMatch;
    }

    private static boolean checkSequential(DataSet currentapproval, int firstmatch) {
        int step;
        boolean firstMatchOK = true;
        if (firstmatch > 0) {
            for (step = 0; step < firstmatch; ++step) {
                String approvalflag = currentapproval.getValue(step, "approvalflag");
                if ("P".equals(approvalflag) || "F".equals(approvalflag)) continue;
                firstMatchOK = false;
                break;
            }
        }
        if (!firstMatchOK) {
            for (step = firstmatch; step < currentapproval.getRowCount(); ++step) {
                currentapproval.setValue(step, "stepstatusflag", "W");
            }
        }
        return firstMatchOK;
    }

    private static void checkUniqueness(DataSet currentapproval, int firstmatch, String sysuserid) {
        int stepIsUniqAndByCurrentUser = -1;
        for (int step = 0; step < currentapproval.getRowCount(); ++step) {
            String approvalflag = currentapproval.getValue(step, "approvalflag");
            if (!"P".equals(approvalflag) && !"F".equals(approvalflag) || !currentapproval.getValue(step, "modby").equals(sysuserid)) continue;
            stepIsUniqAndByCurrentUser = step;
            break;
        }
        int allowedstep = firstmatch;
        if (stepIsUniqAndByCurrentUser >= 0 && stepIsUniqAndByCurrentUser != firstmatch) {
            allowedstep = stepIsUniqAndByCurrentUser;
        }
        for (int step = 0; step < currentapproval.getRowCount(); ++step) {
            if (step == allowedstep) {
                if (!"V".equals(currentapproval.getValue(step, "stepstatusflag")) || currentapproval.getValue(step, "modby") != null && currentapproval.getValue(step, "modby").length() != 0) continue;
                currentapproval.setValue(step, "stepstatusflag", "C");
                continue;
            }
            currentapproval.setValue(step, "stepstatusflag", "W");
        }
    }

    private static void checkForcePeer(DataSet currentapproval, DataSet checkDs, String checkcolumnid, String sysuserid) {
        if (checkDs != null) {
            for (int step = 0; step < currentapproval.getRowCount(); ++step) {
                if (!"Y".equals(currentapproval.getValue(step, "forcepeerflag")) || checkDs.findRow(checkcolumnid, sysuserid) < 0) continue;
                currentapproval.setValue(step, "stepstatusflag", "W");
            }
        }
    }

    public static boolean hasApprovalStarted(DataSet currentapproval) {
        boolean started = false;
        for (int step = 0; step < currentapproval.getRowCount(); ++step) {
            String approvalflag = currentapproval.getValue(step, "approvalflag");
            if (approvalflag.equals("U")) continue;
            started = true;
        }
        return started;
    }
}

