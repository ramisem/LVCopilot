/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.util;

import com.labvantage.opal.util.DataSet2;
import com.labvantage.opal.util.OpalUtil;

class DataItem2 {
    private DataSet2 __DataSet2;
    private String __ParamID;
    private String __ParamType;
    private String __Replicate;
    private boolean __Mandatory;
    private boolean __Released;
    private String __EnteredText;

    DataItem2() {
    }

    public DataSet2 getDataSet2() {
        return this.__DataSet2;
    }

    public void setDataSet2(DataSet2 dataSet2) {
        this.__DataSet2 = dataSet2;
    }

    public String getParamID() {
        return this.__ParamID;
    }

    public void setParamID(String paramID) {
        this.__ParamID = paramID;
    }

    public String getParamType() {
        return this.__ParamType;
    }

    public void setParamType(String paramType) {
        this.__ParamType = paramType;
    }

    public String getReplicate() {
        return this.__Replicate;
    }

    public void setReplicate(String replicate) {
        this.__Replicate = replicate;
    }

    public boolean isMandatory() {
        return this.__Mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.__Mandatory = mandatory;
    }

    public boolean isReleased() {
        return this.__Released;
    }

    public void setReleased(boolean released) {
        this.__Released = released;
    }

    public String getEnteredText() {
        return this.__EnteredText;
    }

    public void setEnteredText(String enteredText) {
        this.__EnteredText = enteredText;
    }

    public boolean hasData() {
        return !OpalUtil.isSapphireNull(this.getEnteredText());
    }
}

