/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt.rules;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import java.sql.Timestamp;
import java.util.Calendar;
import sapphire.SapphireException;
import sapphire.accessor.SequenceProcessor;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;

public class EventLog {
    protected static final String LABVANTAGE_CVS_ID = "$Revision: 103287 $";
    public static final String TABLEID = "s_eventlog";
    public static final String COLUMN_EVENTLOGID = "s_eventlogid";
    public static final String COLUMN_EVENTLOGDESC = "eventlogdesc";
    public static final String COLUMN_PARENTEVENTLOGID = "parenteventlogid";
    public static final String COLUMN_EVENTTYPE = "eventtype";
    public static final String COLUMN_EVENTDT = "eventdt";
    public static final String COLUMN_OLDVALUE = "oldvalue";
    public static final String COLUMN_NEWVALUE = "newvalue";
    public static final String COLUMN_DEPARTMENTID = "departmentid";
    public static final String COLUMN_TRACKITEMID = "trackitemid";
    public static final String COLUMN_EVENTSDCID = "eventsdcid";
    public static final String COLUMN_EVENTKEYID1 = "eventkeyid1";
    public static final String COLUMN_EVENTKEYID2 = "eventkeyid2";
    public static final String COLUMN_EVENTKEYID3 = "eventkeyid3";
    public static final String COLUMN_EVENTAUDITSEQUENCE = "eventauditsequence";
    public static final String COLUMN_OLDSTORAGEUNITID = "oldstorageunitid";
    public static final String COLUMN_OLDLABELPATH = "oldlabelpath";
    public static final String COLUMN_OLDLINKSDCID = "oldlinksdcid";
    public static final String COLUMN_OLDLINKKEYID1 = "oldlinkkeyid1";
    public static final String COLUMN_OLDLINKKEYID2 = "oldlinkkeyid2";
    public static final String COLUMN_OLDLINKKEYID3 = "oldlinkkeyid3";
    public static final String COLUMN_NEWSTORAGEUNITID = "newstorageunitid";
    public static final String COLUMN_NEWLABELPATH = "newlabelpath";
    public static final String COLUMN_NEWLINKSDCID = "newlinksdcid";
    public static final String COLUMN_NEWLINKKEYID1 = "newlinkkeyid1";
    public static final String COLUMN_NEWLINKKEYID2 = "newlinkkeyid2";
    public static final String COLUMN_NEWLINKKEYID3 = "newlinkkeyid3";
    public static final String COLUMN_USERSEQUENCE = "usersequence";
    public static final String COLUMN_NOTES = "notes";
    public static final String COLUMN_AUDITSEQUENCE = "auditsequence";
    public static final String COLUMN_TRACELOGID = "tracelogid";
    public static final String COLUMN_TEMPLATEFLAG = "templateflag";
    public static final String COLUMN_CREATEDT = "createdt";
    public static final String COLUMN_CREATEBY = "createby";
    public static final String COLUMN_CREATETOOL = "createtool";
    public static final String COLUMN_MODDT = "moddt";
    public static final String COLUMN_MODBY = "modby";
    public static final String COLUMN_MODTOOL = "modtool";
    public static final String EVENT_GLPCHANGE = "GLPChange";
    public static final String EVENT_CUSTODYCHANGE = "CustodyChange";
    public static final String EVENT_LOCATIONCHANGE = "LocationChange";
    public static final String EVENT_SAMPLESTATUSCHANGE = "SampleStatusChange";
    public static final String EVENT_STORAGESTATUSCHANGE = "StorageStatusChange";
    public static final String EVENT_CUSTODIALDOMAINCHANGE = "CustodialDomainChange";
    private DataSet insert;
    private DBAccess database;
    private String currentUser;
    private boolean recordParentEventLogID;
    private SequenceProcessor sequenceProcessor;

    public EventLog(DBAccess database, SequenceProcessor sequenceProcessor) {
        this.database = database;
        this.sequenceProcessor = sequenceProcessor;
        this.insert = new DataSet();
        this.insert.addColumn(COLUMN_EVENTLOGID, 0);
        this.insert.addColumn(COLUMN_EVENTLOGID, 0);
        this.insert.addColumn(COLUMN_EVENTLOGDESC, 0);
        this.insert.addColumn(COLUMN_PARENTEVENTLOGID, 0);
        this.insert.addColumn(COLUMN_EVENTTYPE, 0);
        this.insert.addColumn(COLUMN_EVENTDT, 2);
        this.insert.addColumn(COLUMN_OLDVALUE, 0);
        this.insert.addColumn(COLUMN_NEWVALUE, 0);
        this.insert.addColumn(COLUMN_DEPARTMENTID, 0);
        this.insert.addColumn(COLUMN_TRACKITEMID, 0);
        this.insert.addColumn(COLUMN_EVENTSDCID, 0);
        this.insert.addColumn(COLUMN_EVENTKEYID1, 0);
        this.insert.addColumn(COLUMN_EVENTKEYID2, 0);
        this.insert.addColumn(COLUMN_EVENTKEYID3, 0);
        this.insert.addColumn(COLUMN_EVENTAUDITSEQUENCE, 0);
        this.insert.addColumn(COLUMN_OLDSTORAGEUNITID, 0);
        this.insert.addColumn(COLUMN_OLDLABELPATH, 0);
        this.insert.addColumn(COLUMN_OLDLINKSDCID, 0);
        this.insert.addColumn(COLUMN_OLDLINKKEYID1, 0);
        this.insert.addColumn(COLUMN_OLDLINKKEYID2, 0);
        this.insert.addColumn(COLUMN_OLDLINKKEYID3, 0);
        this.insert.addColumn(COLUMN_NEWSTORAGEUNITID, 0);
        this.insert.addColumn(COLUMN_NEWLABELPATH, 0);
        this.insert.addColumn(COLUMN_NEWLINKSDCID, 0);
        this.insert.addColumn(COLUMN_NEWLINKKEYID1, 0);
        this.insert.addColumn(COLUMN_NEWLINKKEYID2, 0);
        this.insert.addColumn(COLUMN_NEWLINKKEYID3, 0);
        this.insert.addColumn(COLUMN_USERSEQUENCE, 0);
        this.insert.addColumn(COLUMN_NOTES, 0);
        this.insert.addColumn(COLUMN_AUDITSEQUENCE, 0);
        this.insert.addColumn(COLUMN_TRACELOGID, 0);
        this.insert.addColumn(COLUMN_TEMPLATEFLAG, 0);
        this.insert.addColumn(COLUMN_CREATEDT, 2);
        this.insert.addColumn(COLUMN_CREATEBY, 0);
        this.insert.addColumn(COLUMN_CREATETOOL, 0);
        this.insert.addColumn(COLUMN_MODDT, 2);
        this.insert.addColumn(COLUMN_MODBY, 0);
        this.insert.addColumn(COLUMN_MODTOOL, 0);
    }

    public int addRow() {
        return this.insert.addRow();
    }

    public void setString(int row, String columnid, String value) {
        this.insert.setString(row, columnid, value);
    }

    public void setDate(int row, String columnid, Timestamp value) {
        this.insert.setDate(row, columnid, value);
    }

    public void process() throws SapphireException {
        if (this.size() > 0) {
            Timestamp eventdt = DateTimeUtil.getNowTimestamp();
            Calendar calendar = Calendar.getInstance();
            String sequenceid = String.valueOf(calendar.get(1)) + String.valueOf(calendar.get(2));
            int sequence = this.sequenceProcessor.getSequence(TABLEID, sequenceid, this.insert.size());
            for (int i = 0; i < this.insert.size(); ++i) {
                String eventLogID = sequenceid + "-" + sequence++;
                this.insert.setString(i, COLUMN_EVENTLOGID, eventLogID);
                this.insert.setDate(i, COLUMN_EVENTDT, eventdt);
                this.insert.setDate(i, COLUMN_CREATEDT, eventdt);
                this.insert.setString(i, COLUMN_CREATEBY, this.getCurrentUser());
                this.insert.setString(i, COLUMN_CREATETOOL, "EventLog");
                this.insert.setDate(i, COLUMN_MODDT, eventdt);
                this.insert.setString(i, COLUMN_MODBY, this.getCurrentUser());
                this.insert.setString(i, COLUMN_MODTOOL, "EventLog");
                if (!this.recordParentEventLogID || i < 0) continue;
                this.insert.setString(i, COLUMN_PARENTEVENTLOGID, this.insert.getString(i, COLUMN_EVENTLOGID));
            }
            DataSetUtil.insert(this.database, this.insert, TABLEID);
        }
    }

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    public String getCurrentUser() {
        if (this.currentUser == null) {
            this.currentUser = "";
        }
        return this.currentUser;
    }

    public int size() {
        return this.insert.size();
    }

    public void setRecordParentEventLogID(boolean recordParentEventLogID) {
        this.recordParentEventLogID = recordParentEventLogID;
    }
}

