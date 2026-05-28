/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.wap.activity;

import com.labvantage.sapphire.modules.wap.activity.WAPConstants;

public class WAPAvailabilityOptions
implements WAPConstants {
    public static final String TYPE_ANALYST = "Analyst";
    public static final String TYPE_INSTRUMENT = "Instrument";
    public static final String TYPE_ANALYSTWORKAREA = "AnalystWorkArea";
    public static final String TYPE_INSTRUMENTWORKAREA = "InstrumentWorkArea";
    private boolean treatFixedFloatingAsFixed = true;
    private String activityStatusList = "Draft;Activated;In Progress";
    private String type = "";
    private String resourcekeyid1 = "";
    private String instrumenttypeid = "";
    private String instrumentmodelid = "";
    private String analystType = "";
    private boolean isLog = false;

    public boolean isLog() {
        return this.isLog;
    }

    public void setIsLog(boolean isLog) {
        this.isLog = isLog;
    }

    public boolean isTreatFixedFloatingAsFixed() {
        return this.treatFixedFloatingAsFixed;
    }

    public void setTreatFixedFloatingAsFixed(boolean treatFixedFloatingAsFixed) {
        this.treatFixedFloatingAsFixed = treatFixedFloatingAsFixed;
    }

    public String getActivityStatusList() {
        return this.activityStatusList;
    }

    public void setActivityStatusList(String activityStatusList) {
        this.activityStatusList = activityStatusList;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getResourcekeyid1() {
        return this.resourcekeyid1;
    }

    public void setResourcekeyid1(String resourcekeyid1) {
        this.resourcekeyid1 = resourcekeyid1;
    }

    public String getInstrumenttypeid() {
        return this.instrumenttypeid;
    }

    public void setInstrumenttypeid(String instrumenttypeid) {
        this.instrumenttypeid = instrumenttypeid;
    }

    public String getInstrumentmodelid() {
        return this.instrumentmodelid;
    }

    public void setInstrumentmodelid(String instrumentmodelid) {
        this.instrumentmodelid = instrumentmodelid;
    }

    public void setAnalystType(String analystType) {
        this.analystType = analystType;
    }

    public String getAnalystType() {
        return this.analystType;
    }
}

