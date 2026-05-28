/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.util;

import com.labvantage.opal.actions.DSApproval;
import com.labvantage.opal.util.DataItem2;
import java.util.ArrayList;
import java.util.List;

public class DataSet2
extends ArrayList {
    public static String LABVANTAGE_CVS_ID = "$Revision: 62592 $";
    public static final String STATUS_INITIAL = "Initial";
    public static final String STATUS_ASSIGNED = "Assigned";
    public static final String STATUS_INPROGRESS = "InProgress";
    public static final String STATUS_REMEASURED = "Remeasured";
    public static final String STATUS_RETESTED = "Retested";
    public static final String STATUS_RELEASED = "Released";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_ALLENTERED = "DataEntered";
    public static final String STATUS_CANCELLED = "Cancelled";
    private String __ID;
    private String __SdcId;
    private String __KeyId1;
    private String __KeyId2;
    private String __KeyId3;
    private String __ParamListID;
    private String __ParamListVersionID;
    private String __VariantID;
    private String __Dataset;
    private String __Status;
    private String __ApprovalFlag = "";
    private boolean __StatusModified;
    private List __Approvals = new ArrayList();
    public static List __DatasetStatusList = new ArrayList();
    private boolean __MandatoryDataItemPresent = false;
    private boolean __MandatoryDatasetRelationIncomplete = false;
    private boolean __MandatoryAttributeNotFilledIn = false;
    private boolean __EnterOptionalToBeReleased = false;
    boolean mandatory;

    public DataSet2(String sdcId, String keyId1, String paramListID, String paramListVersionID, String variantID, String dataset) {
        this.__SdcId = sdcId;
        this.__KeyId1 = keyId1;
        this.__ParamListID = paramListID;
        this.__ParamListVersionID = paramListVersionID;
        this.__VariantID = variantID;
        this.__Dataset = dataset;
        this.__ID = sdcId + "|" + keyId1 + "|" + paramListID + "|" + paramListVersionID + "|" + variantID + "|" + dataset;
    }

    public DataSet2(String sdcId, String keyId1, String keyId2, String keyId3, String paramListID, String paramListVersionID, String variantID, String dataset) {
        this.__SdcId = sdcId;
        this.__KeyId1 = keyId1;
        this.__KeyId2 = keyId2;
        this.__KeyId3 = keyId3;
        this.__ParamListID = paramListID;
        this.__ParamListVersionID = paramListVersionID;
        this.__VariantID = variantID;
        this.__Dataset = dataset;
        this.__ID = sdcId + "|" + keyId1 + "|" + keyId2 + "|" + keyId3 + "|" + paramListID + "|" + paramListVersionID + "|" + variantID + "|" + dataset;
    }

    public String getId() {
        return this.__ID;
    }

    public String getSdcId() {
        return this.__SdcId;
    }

    public String getKeyId1() {
        return this.__KeyId1;
    }

    public String getKeyId2() {
        return this.__KeyId2;
    }

    public String getKeyId3() {
        return this.__KeyId3;
    }

    public String getParamListID() {
        return this.__ParamListID;
    }

    public String getParamListVersionID() {
        return this.__ParamListVersionID;
    }

    public String getVariantID() {
        return this.__VariantID;
    }

    public String getDataset() {
        return this.__Dataset;
    }

    public String getStatus() {
        return this.__Status;
    }

    public String getApprovalFlag() {
        return this.__ApprovalFlag;
    }

    public void setStatus(String status) {
        this.__Status = status;
    }

    public void setApprovalFlag(String approvalflag) {
        this.__ApprovalFlag = approvalflag;
    }

    public boolean isStatusModified() {
        return this.__StatusModified;
    }

    public void addApproval(DSApproval dsApproval) {
        this.__Approvals.add(dsApproval);
    }

    public void setMandatoryDatasetRelationIncomplete(boolean flag) {
        this.__MandatoryDatasetRelationIncomplete = flag;
    }

    public boolean isMandatoryDatasetRelationIncomplete() {
        return this.__MandatoryDatasetRelationIncomplete;
    }

    public void set__MandatoryAttributeNotFilledIn(boolean flag) {
        this.__MandatoryAttributeNotFilledIn = flag;
    }

    public boolean isMandatoryAttributeNotFilledIn() {
        return this.__MandatoryAttributeNotFilledIn;
    }

    public void set_EnterOptionalToBereleased(boolean enterOptionalToRelease) {
        this.__EnterOptionalToBeReleased = enterOptionalToRelease;
    }

    public boolean isEnterOptionalToBereleased() {
        return this.__EnterOptionalToBeReleased;
    }

    public boolean evaluateStatus() {
        if (this.__Status == null || this.__Status.trim().length() == 0) {
            this.__StatusModified = true;
            this.__Status = STATUS_INITIAL;
            return true;
        }
        if (!__DatasetStatusList.contains(this.__Status)) {
            return false;
        }
        String tempStatus = STATUS_INITIAL;
        if (this.size() == 0 && !this.__Status.equals(tempStatus)) {
            this.__StatusModified = true;
            this.__Status = tempStatus;
            return true;
        }
        if (this.hasDataEntryCommenced()) {
            tempStatus = this.isAnyMandatoryPending() ? STATUS_INPROGRESS : (this.isAnyReleasePending() ? STATUS_ALLENTERED : ((this.__Approvals.size() == 0 || "P".equalsIgnoreCase(this.__ApprovalFlag) || "F".equalsIgnoreCase(this.__ApprovalFlag)) && !this.isMandatoryDatasetRelationIncomplete() && !this.isMandatoryAttributeNotFilledIn() ? STATUS_COMPLETED : STATUS_RELEASED));
        }
        if (!this.__Status.equals(tempStatus)) {
            this.__StatusModified = true;
            this.__Status = tempStatus;
        } else {
            this.__StatusModified = false;
        }
        return this.__StatusModified;
    }

    private boolean hasDataEntryCommenced() {
        boolean dataEntryCommenced = false;
        for (DataItem2 di : this) {
            if (!di.hasData() && !di.isReleased()) continue;
            dataEntryCommenced = true;
            break;
        }
        return dataEntryCommenced;
    }

    private boolean isAnyMandatoryPending() {
        boolean mandatoryPending = false;
        for (DataItem2 di : this) {
            if (!di.isMandatory()) continue;
            this.__MandatoryDataItemPresent = true;
            if (di.hasData() || di.isReleased()) continue;
            mandatoryPending = true;
            break;
        }
        return mandatoryPending;
    }

    private boolean isAnyReleasePending() {
        boolean releasePending = false;
        for (DataItem2 di : this) {
            if (this.isEnterOptionalToBereleased()) {
                if (!di.hasData() || di.isReleased()) continue;
                releasePending = true;
                break;
            }
            if (this.__MandatoryDataItemPresent) {
                if (!di.isMandatory() || di.isReleased()) continue;
                releasePending = true;
                break;
            }
            releasePending = true;
            if (!di.isReleased()) continue;
            releasePending = false;
            break;
        }
        return releasePending;
    }

    private boolean isAnyApprovalPending(boolean mandatory) {
        boolean approvalPending = false;
        if (mandatory) {
            for (int i = 0; i < this.__Approvals.size(); ++i) {
                DSApproval dsApproval = (DSApproval)this.__Approvals.get(i);
                if (!dsApproval.isMandatory() || dsApproval.isApproved()) continue;
                approvalPending = true;
                break;
            }
        } else {
            for (int i = 0; i < this.__Approvals.size(); ++i) {
                DSApproval dsApproval = (DSApproval)this.__Approvals.get(i);
                if (dsApproval.isApproved()) continue;
                approvalPending = true;
                break;
            }
        }
        return approvalPending;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DataSet2)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        DataSet2 dataSet2 = (DataSet2)o;
        if (!this.__Dataset.equals(dataSet2.__Dataset)) {
            return false;
        }
        if (!this.__KeyId1.equals(dataSet2.__KeyId1)) {
            return false;
        }
        if (this.__KeyId2 == null && dataSet2.__KeyId2 != null || this.__KeyId2 != null && !this.__KeyId2.equals(dataSet2.__KeyId2)) {
            return false;
        }
        if (this.__KeyId3 == null && dataSet2.__KeyId3 != null || this.__KeyId3 != null && !this.__KeyId3.equals(dataSet2.__KeyId3)) {
            return false;
        }
        if (!this.__ParamListID.equals(dataSet2.__ParamListID)) {
            return false;
        }
        if (!this.__ParamListVersionID.equals(dataSet2.__ParamListVersionID)) {
            return false;
        }
        if (!this.__SdcId.equals(dataSet2.__SdcId)) {
            return false;
        }
        return this.__VariantID.equals(dataSet2.__VariantID);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 29 * result + this.__SdcId.hashCode();
        result = 29 * result + this.__KeyId1.hashCode();
        if (this.__KeyId2 != null) {
            result = 29 * result + this.__KeyId2.hashCode();
        }
        if (this.__KeyId3 != null) {
            result = 29 * result + this.__KeyId3.hashCode();
        }
        result = 29 * result + this.__ParamListID.hashCode();
        result = 29 * result + this.__ParamListVersionID.hashCode();
        result = 29 * result + this.__VariantID.hashCode();
        result = 29 * result + this.__Dataset.hashCode();
        return result;
    }

    public boolean isMandatory() {
        return this.mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    static {
        __DatasetStatusList.add(STATUS_INITIAL);
        __DatasetStatusList.add(STATUS_ASSIGNED);
        __DatasetStatusList.add(STATUS_INPROGRESS);
        __DatasetStatusList.add(STATUS_REMEASURED);
        __DatasetStatusList.add(STATUS_RETESTED);
        __DatasetStatusList.add(STATUS_RELEASED);
        __DatasetStatusList.add(STATUS_COMPLETED);
        __DatasetStatusList.add(STATUS_ALLENTERED);
        __DatasetStatusList.add(STATUS_CANCELLED);
    }
}

