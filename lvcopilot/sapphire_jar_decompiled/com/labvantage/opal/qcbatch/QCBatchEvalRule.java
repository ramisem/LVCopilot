/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.qcbatch;

public class QCBatchEvalRule {
    public static String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private String __QCBatchSampleTypeID;
    private String __QCBatchEvalRuleId;
    private String __ViolationCount;
    private String __WindowSize;
    private String __SigmaAboveCL;
    private String __SigmaBelowCL;
    private String __RulePatternFlag;
    private String __InsideLimitFlag;
    private String __WarningFlag;

    public QCBatchEvalRule(String __QCBatchSampleTypeID, String __QCBatchEvalRuleId) {
        this.__QCBatchSampleTypeID = __QCBatchSampleTypeID;
        this.__QCBatchEvalRuleId = __QCBatchEvalRuleId;
    }

    public String getQCBatchSampleTypeID() {
        return this.__QCBatchSampleTypeID;
    }

    public String getQCBatchEvalRuleId() {
        return this.__QCBatchEvalRuleId;
    }

    public String getViolationCount() {
        return this.__ViolationCount;
    }

    public void setViolationCount(String __ViolationCount) {
        this.__ViolationCount = __ViolationCount;
    }

    public String getWindowSize() {
        return this.__WindowSize;
    }

    public void setWindowSize(String __WindowSize) {
        this.__WindowSize = __WindowSize;
    }

    public String getSigmaAboveCL() {
        return this.__SigmaAboveCL;
    }

    public void setSigmaAboveCL(String __SigmaAboveCL) {
        this.__SigmaAboveCL = __SigmaAboveCL;
    }

    public String getSigmaBelowCL() {
        return this.__SigmaBelowCL;
    }

    public void setSigmaBelowCL(String __SigmaBelowCL) {
        this.__SigmaBelowCL = __SigmaBelowCL;
    }

    public String getRulePatternFlag() {
        return this.__RulePatternFlag;
    }

    public void setRulePatternFlag(String __RulePatternFlag) {
        this.__RulePatternFlag = __RulePatternFlag;
    }

    public String getInsideLimitFlag() {
        return this.__InsideLimitFlag;
    }

    public void setInsideLimitFlag(String __InsideLimitFlag) {
        this.__InsideLimitFlag = __InsideLimitFlag;
    }

    public String getWarningFlag() {
        return this.__WarningFlag;
    }

    public void setWarningFlag(String __WarningFlag) {
        this.__WarningFlag = __WarningFlag;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof QCBatchEvalRule)) {
            return false;
        }
        QCBatchEvalRule qcBatchEvalRule = (QCBatchEvalRule)o;
        if (!this.__QCBatchEvalRuleId.equals(qcBatchEvalRule.__QCBatchEvalRuleId)) {
            return false;
        }
        return this.__QCBatchSampleTypeID.equals(qcBatchEvalRule.__QCBatchSampleTypeID);
    }

    public int hashCode() {
        int result = this.__QCBatchSampleTypeID.hashCode();
        result = 29 * result + this.__QCBatchEvalRuleId.hashCode();
        return result;
    }
}

