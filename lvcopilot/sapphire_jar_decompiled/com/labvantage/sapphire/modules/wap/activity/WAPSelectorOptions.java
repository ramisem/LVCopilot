/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.wap.activity;

public class WAPSelectorOptions {
    private boolean includeFixed = true;
    private boolean includeFloating = true;
    private boolean includeTimemodeNone = false;
    private boolean treatFixedFloatingAsFixed = true;
    private boolean includeFixedOverlaps = true;
    private boolean includeFloatingOverlaps = true;
    private String activityStatusList = "";
    private boolean pendingUserAssignment = false;
    private boolean pendingInstrumentAssignment = false;
    private String assignedUser = "";
    private String assignedInstrument = "";
    private String assignedUserWorkarea = "";
    private String assignedInstrumentWorkarea = "";
    private String analysttype = "";
    private String instrumenttypeid = "";
    private String instrumentmodelid = "";
    private String extraActivityFrom = "";
    private String extraActiviytWhere = "";

    public String getExtraActivityFrom() {
        return this.extraActivityFrom;
    }

    public void setExtraActivityFrom(String extraActivityFrom) {
        this.extraActivityFrom = extraActivityFrom;
    }

    public String getExtraActiviytWhere() {
        return this.extraActiviytWhere;
    }

    public void setExtraActiviytWhere(String extraActiviytWhere) {
        this.extraActiviytWhere = extraActiviytWhere;
    }

    public boolean isPendingUserAssignment() {
        return this.pendingUserAssignment;
    }

    public void setPendingUserAssignment(boolean pendingUserAssignment) {
        this.pendingUserAssignment = pendingUserAssignment;
    }

    public boolean isPendingInstrumentAssignment() {
        return this.pendingInstrumentAssignment;
    }

    public void setPendingInstrumentAssignment(boolean pendingInstrumentAssignment) {
        this.pendingInstrumentAssignment = pendingInstrumentAssignment;
    }

    public String getAssignedUser() {
        return this.assignedUser;
    }

    public void setAssignedUser(String assignedUser) {
        this.assignedUser = assignedUser;
        this.assignedUserWorkarea = "";
        this.assignedInstrument = "";
        this.assignedInstrumentWorkarea = "";
    }

    public String getAssignedInstrument() {
        return this.assignedInstrument;
    }

    public void setAssignedInstrument(String assignedInstrument) {
        this.assignedUser = "";
        this.assignedUserWorkarea = "";
        this.assignedInstrumentWorkarea = "";
        this.assignedInstrument = assignedInstrument;
    }

    public String getAssignedUserWorkarea() {
        return this.assignedUserWorkarea;
    }

    public void setAssignedUserWorkarea(String assignedUserWorkarea) {
        this.assignedUser = "";
        this.assignedInstrument = "";
        this.assignedInstrumentWorkarea = "";
        this.assignedUserWorkarea = assignedUserWorkarea;
    }

    public String getAssignedInstrumentWorkarea() {
        return this.assignedInstrumentWorkarea;
    }

    public void setAssignedInstrumentWorkarea(String assignedInstrumentWorkarea, String instrumenttypeid, String instrumentmodelid) {
        this.assignedUser = "";
        this.assignedUserWorkarea = "";
        this.assignedInstrument = "";
        this.assignedInstrumentWorkarea = assignedInstrumentWorkarea;
        this.instrumenttypeid = instrumenttypeid;
        this.instrumentmodelid = instrumentmodelid;
    }

    public String getInstrumenttypeid() {
        return this.instrumenttypeid;
    }

    public void setInstrumenttypeid(String instrumenttypeid) {
        this.instrumenttypeid = instrumenttypeid;
    }

    public String getAnalysttype() {
        return this.analysttype;
    }

    public void setAnalysttype(String analysttype) {
        this.analysttype = analysttype;
    }

    public String getInstrumentmodelid() {
        return this.instrumentmodelid;
    }

    public void setInstrumentmodelid(String instrumentmodelid) {
        this.instrumentmodelid = instrumentmodelid;
    }

    public String getActivityStatusList() {
        return this.activityStatusList;
    }

    public void setActivityStatusList(String activityStatusList) {
        this.activityStatusList = activityStatusList;
    }

    public boolean isIncludeTimemodeNone() {
        return this.includeTimemodeNone;
    }

    public void setIncludeTimemodeNone(boolean includeTimemodeNone) {
        this.includeTimemodeNone = includeTimemodeNone;
    }

    public boolean isIncludeFixed() {
        return this.includeFixed;
    }

    public void setIncludeFixed(boolean includeFixed) {
        this.includeFixed = includeFixed;
    }

    public boolean isIncludeFloating() {
        return this.includeFloating;
    }

    public void setIncludeFloating(boolean includeFloating) {
        this.includeFloating = includeFloating;
    }

    public boolean isTreatFixedFloatingAsFixed() {
        return this.treatFixedFloatingAsFixed;
    }

    public void setTreatFixedFloatingAsFixed(boolean treatFixedFloatingAsFixed) {
        this.treatFixedFloatingAsFixed = treatFixedFloatingAsFixed;
    }

    public boolean isIncludeFixedOverlaps() {
        return this.includeFixedOverlaps;
    }

    public void setIncludeFixedOverlaps(boolean includeFixedOverlaps) {
        this.includeFixedOverlaps = includeFixedOverlaps;
    }

    public boolean isIncludeFloatingOverlaps() {
        return this.includeFloatingOverlaps;
    }

    public void setIncludeFloatingOverlaps(boolean includeFloatingOverlaps) {
        this.includeFloatingOverlaps = includeFloatingOverlaps;
    }
}

