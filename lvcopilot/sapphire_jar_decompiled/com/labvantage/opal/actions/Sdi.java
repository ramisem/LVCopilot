/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions;

import java.util.ArrayList;
import java.util.List;

class Sdi
extends ArrayList {
    private String __ID;
    private String __Status;
    private boolean __DataEntered;
    public static final String SDISTATUS_INITIAL = "Initial";
    public static final String SDISTATUS_RECEIVED = "Received";
    public static final String SDISTATUS_INPROGRESS = "InProgress";
    public static final String SDISTATUS_COMPLETED = "Completed";
    private static List __AllowedStatus = new ArrayList();
    private int __IncompleteWorksheetCount = 0;

    public Sdi(String ID, String status) {
        this.__ID = ID;
        this.__Status = status;
    }

    public String getID() {
        return this.__ID;
    }

    public String getStatus() {
        return this.__Status;
    }

    public boolean isDataEntered() {
        return this.__DataEntered;
    }

    public int getIncompleteWorksheetCount() {
        return this.__IncompleteWorksheetCount;
    }

    public void setIncompleteWorksheetCount(int count) {
        this.__IncompleteWorksheetCount = count;
    }

    public void setDataEntered(boolean dataEntered) {
        this.__DataEntered = dataEntered;
    }

    public boolean evaluateStatus() {
        if (this.__Status == null || this.__Status.trim().length() == 0) {
            this.__Status = SDISTATUS_INITIAL;
            return true;
        }
        if (!__AllowedStatus.contains(this.__Status)) {
            return false;
        }
        String tempStatus = SDISTATUS_INITIAL;
        if (__AllowedStatus.contains(this.__Status)) {
            if (this.isCompleted()) {
                tempStatus = SDISTATUS_COMPLETED;
            } else if (this.isInitial()) {
                tempStatus = SDISTATUS_INITIAL;
                if (this.__Status.equals(SDISTATUS_RECEIVED)) {
                    tempStatus = SDISTATUS_RECEIVED;
                }
            } else if (this.isInProgress()) {
                tempStatus = SDISTATUS_INPROGRESS;
            } else if (this.__Status.equals(SDISTATUS_RECEIVED)) {
                tempStatus = SDISTATUS_RECEIVED;
            }
        }
        if (!tempStatus.equals(this.__Status)) {
            this.__Status = tempStatus;
            return true;
        }
        return false;
    }

    public boolean isCompleted() {
        boolean completed = true;
        if (this.getIncompleteWorksheetCount() > 0) {
            return false;
        }
        for (Object o : this) {
            String nextElement = (String)o;
            if (nextElement.equals(SDISTATUS_COMPLETED) || nextElement.equals("Cancelled")) continue;
            completed = false;
            break;
        }
        return completed;
    }

    public boolean isInProgress() {
        boolean inprogress = false;
        for (Object o : this) {
            String nextElement = (String)o;
            if (nextElement.equals(SDISTATUS_INITIAL) || nextElement.equals("Cancelled")) continue;
            inprogress = true;
            break;
        }
        return inprogress;
    }

    public boolean isInitial() {
        boolean initial = true;
        for (Object o : this) {
            String nextElement = (String)o;
            if (nextElement.equals(SDISTATUS_INITIAL) || nextElement.equals("Cancelled")) continue;
            initial = false;
            break;
        }
        return initial;
    }

    static {
        __AllowedStatus.add(SDISTATUS_INITIAL);
        __AllowedStatus.add(SDISTATUS_RECEIVED);
        __AllowedStatus.add(SDISTATUS_INPROGRESS);
        __AllowedStatus.add(SDISTATUS_COMPLETED);
    }
}

