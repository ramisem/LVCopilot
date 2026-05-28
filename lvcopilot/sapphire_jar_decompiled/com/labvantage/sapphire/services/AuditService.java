/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.services;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.services.BaseService;
import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import java.sql.CallableStatement;
import java.sql.Timestamp;
import sapphire.SapphireException;
import sapphire.accessor.SequenceProcessor;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class AuditService
extends BaseService {
    public static final String LOGNAME = "AuditService";
    public static final String ESC_EQUAL = "#EQ#";

    public AuditService(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
        this.logName = LOGNAME;
    }

    public String addSDITraceLogEntry(String sdcid, String keyid1, String keyid2, String keyid3, String reason, String auditdt, String description, boolean standard) throws ServiceException {
        return this.addSDITraceLogEntry(sdcid, keyid1, keyid2, keyid3, reason, "", "", auditdt, description, standard);
    }

    public String addSDITraceLogEntry(String sdcid, String keyid1, String keyid2, String keyid3, String reason, String activity, String signedflag, String auditdt, String description, boolean standard) throws ServiceException {
        this.logInfo("Adding SDI tracelog entry for " + new SDI(sdcid, keyid1, keyid2, keyid3).getKeyText());
        if (sdcid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "SDCID not defined");
        }
        String[] keyid1list = StringUtil.split(keyid1, ";");
        int items = keyid1list.length;
        SequenceProcessor sequenceProcessor = this.getSequenceProcessor();
        int tracelogid = sequenceProcessor.getSequence("tracelog", "tracelog", 0, items);
        if (tracelogid == -1) {
            throw new ServiceException("Error getting sequence for tracelog");
        }
        reason = reason.replaceAll(ESC_EQUAL, "=");
        DataSet props = new DataSet();
        props.addColumnValues("keyid1", 0, keyid1, ";", "(null)");
        props.addColumnValues("keyid2", 0, keyid2, ";", "(null)");
        props.addColumnValues("keyid3", 0, keyid3, ";", "(null)");
        props.addColumnValues("reason", 0, reason, ";");
        props.addColumnValues("activity", 0, activity, ";", "");
        props.addColumnValues("signedflag", 0, signedflag, ";", "N");
        props.padColumns();
        String insert = "INSERT INTO TRACELOG ( tracelogid, tracelogdesc, reason, standardflag, typeflag, sdcid, keyid1, keyid2, keyid3, activity, signedflag, createdt, createby, createtool, createdevice, moddt, modby, modtool ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
        DBUtil db = new DBUtil();
        try {
            db.setConnection(this.sapphireConnection);
            this.logInfo("\tUpdating tracelog using : " + insert);
            Timestamp logdt = new DateTimeUtil().getTimestamp(auditdt);
            if (logdt == null) {
                throw new ServiceException("INVALID_PARAMETER", "Audit date generated null timestamp");
            }
            for (int i = 0; i < props.size(); ++i) {
                reason = props.getValue(i, "reason");
                activity = props.getValue(i, "activity", "DataEdit");
                String auditSignedFlag = props.getValue(i, "signedflag", "N");
                int rows = db.executePreparedUpdate(insert, new Object[]{String.valueOf(tracelogid + i), description, reason, standard ? "Y" : "N", "D", sdcid, props.getValue(i, "keyid1"), props.getValue(i, "keyid2"), props.getValue(i, "keyid3"), activity, auditSignedFlag, logdt, this.sapphireConnection.getSysuserId(), "logData", "logData", logdt, this.sapphireConnection.getSysuserId(), "logData"});
                if (rows == 1) continue;
                throw new ServiceException("EXECUTE_STMT_FAILED", "Failed to insert a row in the tracelog table.");
            }
            db.closeStatement();
        }
        catch (SapphireException e) {
            throw new ServiceException("EXECUTE_STMT_FAILED", "Failed to execute tracelog insert", e);
        }
        finally {
            db.reset();
        }
        return String.valueOf(tracelogid);
    }

    public String addActivityTraceLogEntry(String activity, String description, String activityProperty) throws ServiceException {
        int tracelogid = -1;
        this.logInfo("Adding activity tracelog entry '" + activity + "'");
        ConfigService configService = new ConfigService(this.sapphireConnection);
        boolean logActivity = configService.getProfileProperty(this.sapphireConnection.getSysuserId(), activityProperty, "N").equals("Y");
        if (logActivity) {
            DBUtil db = new DBUtil();
            try {
                db.setConnection(this.sapphireConnection);
                Timestamp now = DateTimeUtil.getNowTimestamp();
                SequenceProcessor sequenceProcessor = this.getSequenceProcessor();
                tracelogid = sequenceProcessor.getSequence("tracelog", "tracelog");
                if (tracelogid == -1) {
                    throw new ServiceException("Error getting sequence for tracelog");
                }
                String insert = "INSERT INTO tracelog ( tracelogid, tracelogdesc, standardflag, typeflag, activity, createdevice, createdt, createby, createtool, moddt, modby, modtool )  VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
                db.executePreparedUpdate(insert, new Object[]{String.valueOf(tracelogid), description, "N", "A", activity, this.sapphireConnection.getDeviceId(), now, this.sapphireConnection.getSysuserId(), this.sapphireConnection.getTool(), now, this.sapphireConnection.getSysuserId(), this.sapphireConnection.getTool()});
                String string = String.valueOf(tracelogid);
                return string;
            }
            catch (SapphireException e) {
                throw new ServiceException("EXECUTE_STMT_FAILED", "Failed to add trace log entry", e);
            }
            finally {
                db.reset();
            }
        }
        return Integer.toString(tracelogid);
    }

    public String addTraceLogEntry(String reason, String activity, String signedFlag, String auditdt, String description, boolean standard) throws ServiceException {
        this.logInfo("Adding SDI tracelog entry for " + reason + ", " + activity + ", " + signedFlag);
        reason = reason.replaceAll("#semicolon#", ";");
        reason = reason.replaceAll(ESC_EQUAL, "=");
        SequenceProcessor sequenceProcessor = this.getSequenceProcessor();
        int tracelogid = sequenceProcessor.getSequence("tracelog", "tracelog");
        if (tracelogid == -1) {
            throw new ServiceException("Error getting sequence for tracelog");
        }
        String insert = "INSERT INTO TRACELOG ( tracelogid, tracelogdesc, reason, standardflag, typeflag, activity, signedflag, createdt, createby, createtool, createdevice, moddt, modby, modtool ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
        DBUtil db = new DBUtil();
        try {
            db.setConnection(this.sapphireConnection);
            this.logInfo("\tUpdating tracelog using : " + insert);
            Timestamp logdt = new DateTimeUtil().getTimestamp(auditdt);
            if (logdt == null) {
                throw new ServiceException("INVALID_PARAMETER", "Audit date generated null timestamp");
            }
            int rows = db.executePreparedUpdate(insert, new Object[]{String.valueOf(tracelogid), description, reason, standard ? "Y" : "N", "D", activity, signedFlag, logdt, this.sapphireConnection.getSysuserId(), "logData", "logData", logdt, this.sapphireConnection.getSysuserId(), "logData"});
            if (rows != 1) {
                throw new ServiceException("EXECUTE_STMT_FAILED", "Failed to insert a row in the tracelog table.");
            }
            db.closeStatement();
        }
        catch (SapphireException e) {
            throw new ServiceException("EXECUTE_STMT_FAILED", "Failed to execute tracelog insert", e);
        }
        finally {
            db.reset();
        }
        return String.valueOf(tracelogid);
    }

    public void setTracelogIdInDBSession(String traceLogIdStr) throws ServiceException {
        try {
            this.logInfo("Set TracelogId to DB Session: " + traceLogIdStr);
            DBUtil dbUtil = new DBUtil();
            dbUtil.setConnection(this.sapphireConnection);
            String callstmt = "{call lv_app" + (dbUtil.isOracle() ? "." : "_") + "setappinfotracelog( ? ) }";
            CallableStatement callableStatement = dbUtil.prepareCall(callstmt);
            callableStatement.setString(1, traceLogIdStr);
            callableStatement.executeUpdate();
            dbUtil.closeCall();
        }
        catch (Exception e) {
            this.logError("Failed to update Tracelog ID in Database Session.", e);
            throw new ServiceException("DB_ACTION_FAILED", "Failed to update Tracelog ID in Database Session.", e);
        }
    }

    public void removeTracelogIdFromDBSession() throws ServiceException {
        this.logInfo("Clear out Tracelogid from DB Session.");
        this.setTracelogIdInDBSession("");
    }
}

