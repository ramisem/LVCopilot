/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.qcbatch;

public class QCBatchParamSet {
    public static String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private String __QCBatchSampleTypeId;
    private String __QCBatchParamSetId;
    private String __ParamId;
    private String __EvalStatus;
    private String __TargetValue;
    private String __TargetUnits;
    private String __SD;

    public QCBatchParamSet(String __QCBatchSampleTypeId, String __QCBatchParamSetId) {
        this.__QCBatchSampleTypeId = __QCBatchSampleTypeId;
        this.__QCBatchParamSetId = __QCBatchParamSetId;
    }

    public String getQCBatchSampleTypeId() {
        return this.__QCBatchSampleTypeId;
    }

    public String getQCBatchParamSetId() {
        return this.__QCBatchParamSetId;
    }

    public String getParamId() {
        return this.__ParamId;
    }

    public void setParamId(String __ParamId) {
        this.__ParamId = __ParamId;
    }

    public String getEvalStatus() {
        return this.__EvalStatus;
    }

    public void setEvalStatus(String __EvalStatus) {
        this.__EvalStatus = __EvalStatus;
    }

    public String getTargetValue() {
        return this.__TargetValue;
    }

    public void setTargetValue(String __TargetValue) {
        this.__TargetValue = __TargetValue;
    }

    public String getTargetUnits() {
        return this.__TargetUnits;
    }

    public void setTargetUnits(String __TargetUnits) {
        this.__TargetUnits = __TargetUnits;
    }

    public String getSD() {
        return this.__SD;
    }

    public void setSD(String __SD) {
        this.__SD = __SD;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof QCBatchParamSet)) {
            return false;
        }
        QCBatchParamSet qcBatchParamSet = (QCBatchParamSet)o;
        if (!this.__QCBatchParamSetId.equals(qcBatchParamSet.__QCBatchParamSetId)) {
            return false;
        }
        return this.__QCBatchSampleTypeId.equals(qcBatchParamSet.__QCBatchSampleTypeId);
    }

    public int hashCode() {
        int result = this.__QCBatchSampleTypeId.hashCode();
        result = 29 * result + this.__QCBatchParamSetId.hashCode();
        return result;
    }
}

