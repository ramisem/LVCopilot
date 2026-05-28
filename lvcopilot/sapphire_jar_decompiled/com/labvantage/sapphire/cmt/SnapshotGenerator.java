/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt;

import com.labvantage.sapphire.cmt.SnapshotFactory;
import com.labvantage.sapphire.cmt.SnapshotPackage;
import com.labvantage.sapphire.xml.StringLogger;
import java.util.Calendar;
import java.util.Objects;
import java.util.UUID;

public class SnapshotGenerator {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";
    String id = UUID.randomUUID().toString().toUpperCase();
    SnapshotPackage snapshotPackage;
    Status status;
    String statusMessage = "";
    Calendar startDateCal = Calendar.getInstance();
    StringLogger stringLoggerRuntime;

    SnapshotGenerator() {
        this.status = Status.INITIATED;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        SnapshotGenerator runInfo = (SnapshotGenerator)o;
        return Objects.equals(this.id, runInfo.id);
    }

    public int hashCode() {
        return Objects.hash(this.id);
    }

    public String getId() {
        return this.id;
    }

    public SnapshotPackage getSnapshotPackage() {
        return this.snapshotPackage;
    }

    public SnapshotPackage getSnapshotPackage(boolean selfDestruct) {
        if (selfDestruct) {
            SnapshotFactory.removeSnapshotGenerator(this.id);
        }
        return this.getSnapshotPackage();
    }

    public Status getStatus() {
        return this.status;
    }

    public String getStatusMessage() {
        return this.statusMessage;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String getLogUpdate() {
        String logRunTime = "";
        StringLogger stringLogger = this.stringLoggerRuntime;
        synchronized (stringLogger) {
            logRunTime = this.stringLoggerRuntime.getLog();
            this.stringLoggerRuntime.clear();
        }
        return logRunTime;
    }

    public static enum Status {
        INITIATED,
        ERROR,
        INPROGRESS,
        COMPLETE;

    }
}

