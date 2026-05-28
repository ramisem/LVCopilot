/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.util;

import com.labvantage.opal.util.SDIDataSet;
import com.labvantage.sapphire.SDI;

public class DataItem {
    public static String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private SDI __SDI;
    private String __ParamListID;
    private String __ParamListVersionID;
    private String __VariantID;
    private String __DataSet;
    private String __ParamID;
    private String __ParamType;
    private String __Replicate;
    private SDIDataSet __SdiDataSet;
    private String __EnteredValue;
    private String __TransformValue;
    private String __UserSequence;
    private boolean __Mandatory;
    private boolean __Released;
    private String __EnteredText;
    private String __DisplayFormat;

    public SDI getSDI() {
        return this.__SDI;
    }

    public void setSDI(SDI __SDI) {
        this.__SDI = __SDI;
    }

    public String getParamListID() {
        return this.__ParamListID;
    }

    public void setParamListID(String __ParamListID) {
        this.__ParamListID = __ParamListID;
    }

    public String getParamListVersionID() {
        return this.__ParamListVersionID;
    }

    public void setParamListVersionID(String __ParamListVersionID) {
        this.__ParamListVersionID = __ParamListVersionID;
    }

    public String getVariantID() {
        return this.__VariantID;
    }

    public void setVariantID(String __VariantID) {
        this.__VariantID = __VariantID;
    }

    public String getDataSet() {
        return this.__DataSet;
    }

    public void setDataSet(String __DataSet) {
        this.__DataSet = __DataSet;
    }

    public String getParamID() {
        return this.__ParamID;
    }

    public void setParamID(String __ParamID) {
        this.__ParamID = __ParamID;
    }

    public String getParamType() {
        return this.__ParamType;
    }

    public void setParamType(String __ParamType) {
        this.__ParamType = __ParamType;
    }

    public String getReplicate() {
        return this.__Replicate;
    }

    public void setReplicate(String __Replicate) {
        this.__Replicate = __Replicate;
    }

    public void setSDIDataSet(SDIDataSet __SdiDataSet) {
        this.__SdiDataSet = __SdiDataSet;
        this.__SDI = __SdiDataSet.getSDI();
        this.__ParamListID = __SdiDataSet.getParamListID();
        this.__ParamListVersionID = __SdiDataSet.getParamListVersionID();
        this.__VariantID = __SdiDataSet.getVariantID();
        this.__DataSet = __SdiDataSet.getDataSet();
    }

    public SDIDataSet getSDIDataSet() {
        return this.__SdiDataSet;
    }

    public String getTransformValue() {
        if (this.__TransformValue != null && this.__TransformValue.length() == 0) {
            return null;
        }
        return this.__TransformValue;
    }

    public void setTransformValue(String __TransformValue) {
        this.__TransformValue = __TransformValue;
    }

    public String getUserSequence() {
        if (this.__UserSequence != null && this.__UserSequence.length() == 0) {
            return null;
        }
        return this.__UserSequence;
    }

    public void setUserSequence(String __UserSequence) {
        this.__UserSequence = __UserSequence;
    }

    public String getEnteredValue() {
        if (this.__EnteredValue != null && this.__EnteredValue.length() == 0) {
            return null;
        }
        return this.__EnteredValue;
    }

    public void setEnteredValue(String __EnteredValue) {
        this.__EnteredValue = __EnteredValue;
    }

    public String getDisplayFormat() {
        if (this.__DisplayFormat != null && this.__DisplayFormat.length() == 0) {
            return null;
        }
        return this.__DisplayFormat;
    }

    public void setDisplayFormat(String __DisplayFormat) {
        this.__DisplayFormat = __DisplayFormat;
    }

    public String getEnteredText() {
        if (this.__EnteredText != null && (this.__EnteredText.length() == 0 || this.__EnteredText.equals("(null)"))) {
            return null;
        }
        return this.__EnteredText;
    }

    public void setEnteredText(String __EnteredText) {
        this.__EnteredText = __EnteredText;
    }

    public boolean isMandatory() {
        return this.__Mandatory;
    }

    public void setMandatory(boolean __Mandatory) {
        this.__Mandatory = __Mandatory;
    }

    public boolean isReleased() {
        return this.__Released;
    }

    public void setReleased(boolean __Released) {
        this.__Released = __Released;
    }
}

