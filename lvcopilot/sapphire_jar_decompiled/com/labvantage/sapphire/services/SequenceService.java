/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.services;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.services.BaseService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.format.DateFormatter;
import com.labvantage.sapphire.util.format.NumericFormatter;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.util.Calendar;
import java.util.Locale;
import sapphire.SapphireException;
import sapphire.accessor.SequenceProcessor;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.StringUtil;

public class SequenceService
extends BaseService {
    public static final String LOGNAME = "SequenceService";

    public SequenceService(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
        this.logName = LOGNAME;
    }

    public int getSequence(String sdcid, String sequenceid, int start, int incrementBy) throws ServiceException {
        if (sdcid == null || sdcid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Sdcid not specified");
        }
        if (sequenceid == null || sequenceid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "Sequenceid not specified");
        }
        sdcid = sdcid.toLowerCase();
        sequenceid = sequenceid.toLowerCase();
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            int n = SequenceService.grab(db, sdcid, sequenceid, start, incrementBy);
            return n;
        }
        catch (Exception e) {
            throw new ServiceException("GRAB_FAILED", "Failed to get sequence for sdcid '" + sdcid + "', sequence '" + sequenceid + "'", e);
        }
        finally {
            db.reset();
        }
    }

    public String getUUID() throws ServiceException {
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            String uuid = "";
            if (db.isOracle()) {
                CallableStatement cs = db.prepareCall("{?=call lv_App.GetUUID() }");
                cs.registerOutParameter(1, 12);
                cs.execute();
                uuid = cs.getString(1);
            } else {
                db.createPreparedResultSet("SELECT Convert(NVARCHAR(40),Newid()) as uuid", new Object[0]);
                if (db.getNext()) {
                    uuid = db.getString("uuid");
                }
            }
            String string = uuid;
            return string;
        }
        catch (Exception e) {
            throw new ServiceException("GRAB_FAILED", "Failed to get UUID", e);
        }
        finally {
            db.reset();
        }
    }

    public static synchronized int grab(DBUtil db, String sdcid, String sequenceid, int start, int incrementBy) throws SapphireException {
        int newvalue = -1;
        if (incrementBy == 0) {
            db.createPreparedResultSet("SELECT sequencevalue FROM sdcsequence WHERE sdcid = ? AND sequenceid = ?", new String[]{sdcid, sequenceid});
            if (db.getNext()) {
                newvalue = db.getInt("sequencevalue");
            }
        } else {
            while (newvalue == -1) {
                int rowsupdated = db.executePreparedUpdate("UPDATE sdcsequence SET sequencevalue = sequencevalue + ? WHERE\tsdcid = ? AND sequenceid = ?", new Object[]{incrementBy, sdcid, sequenceid});
                if (rowsupdated == 1) {
                    db.createPreparedResultSet("SELECT sequencevalue FROM sdcsequence WHERE sdcid = ? AND sequenceid = ?", new String[]{sdcid, sequenceid});
                    if (db.getNext()) {
                        newvalue = db.getInt("sequencevalue") - incrementBy + 1;
                    }
                } else {
                    Trace.logInfo("Creating a new sequence for " + sdcid + ", " + sequenceid + ".");
                    try {
                        int rowsinserted = db.executePreparedUpdate("INSERT INTO sdcsequence ( sdcid, sequenceid, sequencevalue ) VALUES ( ?, ?, ? )", new Object[]{sdcid, sequenceid, start + incrementBy - 1});
                        if (rowsinserted == 1) {
                            newvalue = start;
                        }
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                if (newvalue != -1) continue;
                Trace.logInfo("Clashed getting the next sequence number. Trying again");
            }
        }
        Trace.logInfo("Grabbed " + newvalue + " for sequence " + sequenceid + " for " + sdcid);
        return newvalue;
    }

    public void generateKeys(String sdcid, String keycolumnid, String keyrule, DataSet ds) throws ServiceException {
        this.generateKeys(sdcid, keycolumnid, keyrule, ds, false);
    }

    public void generateKeys(String sdcid, String keycolumnid, String keyrule, DataSet ds, boolean testMode) throws ServiceException {
        this.generateKeys(sdcid, "mainkey", keycolumnid, keyrule, ds, testMode);
    }

    public void generateKeys(String sdcid, String sdcsequenceid, String keycolumnid, String keyrule, DataSet ds) throws ServiceException {
        this.generateKeys(sdcid, sdcsequenceid, keycolumnid, keyrule, ds, false);
    }

    public void generateKeys(String sdcid, String sdcsequenceid, String keycolumnid, String keyrule, DataSet ds, boolean testMode) throws ServiceException {
        this.logInfo("Generating keys for sdcid '" + sdcid + "', keycolumnid '" + keycolumnid + "'");
        try {
            int row;
            this.logInfo("Started KeyGenerator");
            long starttime = System.currentTimeMillis();
            String[] parttext = StringUtil.split(keyrule, ";");
            int parts = parttext.length;
            String[] format = new String[parts];
            String[] column = new String[parts];
            char[] parttype = new char[parts];
            long[] maxlength = new long[parts];
            String[] subpart4 = new String[parts];
            String[] partcolumn = new String[parts];
            boolean[] partactive = new boolean[parts];
            this.logInfo("KEYGEN: " + (System.currentTimeMillis() - starttime) + " Begin string parsing");
            for (int i = 0; i < parts; ++i) {
                partcolumn[i] = String.valueOf(i);
                String[] subpart = StringUtil.split(parttext[i], "^");
                if (subpart.length > 0) {
                    int n = parttype[i] = StringUtil.getLen(subpart[0]) > 0L ? (int)subpart[0].charAt(0) : 32;
                }
                if (subpart.length > 1) {
                    try {
                        maxlength[i] = subpart.length > 1 ? Integer.valueOf(subpart[1]).longValue() : 0L;
                    }
                    catch (NumberFormatException e) {
                        maxlength[i] = 0L;
                    }
                }
                if (subpart.length > 2) {
                    format[i] = subpart[2];
                }
                if (subpart.length > 3) {
                    column[i] = subpart[3];
                }
                if (subpart.length <= 4) continue;
                subpart4[i] = subpart[4];
            }
            int rows = ds.getRowCount();
            this.logInfo("KEYGEN: " + (System.currentTimeMillis() - starttime) + " Populating segment dataset");
            DataSet segment = new DataSet();
            for (row = 0; row < rows; ++row) {
                segment.addRow();
                segment.setNumber(row, "__sequence", new BigDecimal(row));
            }
            for (row = 0; row < rows; ++row) {
                block15: for (int part = 0; part < parts; ++part) {
                    switch (parttype[part]) {
                        case 'T': {
                            partactive[part] = true;
                            segment.setString(row, partcolumn[part], format[part]);
                            continue block15;
                        }
                        case 'S': {
                            partactive[part] = true;
                            String value = ds.getValue(row, column[part]);
                            if (StringUtil.getLen(value) > maxlength[part]) {
                                segment.setString(row, partcolumn[part], format[part].equals("F") ? value.substring(0, (int)maxlength[part]) : value.substring(value.length() - (int)maxlength[part], value.length()));
                                continue block15;
                            }
                            if (subpart4[part].length() > 0) {
                                segment.setString(row, partcolumn[part], format[part].equals("F") ? StringUtil.padRight(value, (int)maxlength[part], subpart4[part].charAt(0)) : StringUtil.padLeft(value, (int)maxlength[part], subpart4[part].charAt(0)));
                                continue block15;
                            }
                            segment.setString(row, partcolumn[part], value);
                            continue block15;
                        }
                        case 'D': {
                            Calendar cal;
                            partactive[part] = true;
                            M18NUtil m18n = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
                            if (column[part].equals("today") || column[part].equals("today in my time zone")) {
                                cal = DateTimeUtil.getNowCalendar();
                                if (column[part].equals("today in my time zone")) {
                                    cal.setTimeZone(m18n.getTimezone());
                                }
                            } else {
                                cal = ds.getCalendar(row, column[part]);
                                if (cal == null) {
                                    throw new ServiceException("Invalid data format specified for column " + column[part]);
                                }
                            }
                            if (format[part].equals("doy")) {
                                String value = String.valueOf(cal.get(6));
                                if (subpart4[part].length() > 0) {
                                    value = StringUtil.padLeft(value, (int)maxlength[part], subpart4[part].charAt(0));
                                }
                                segment.setString(row, partcolumn[part], value);
                                continue block15;
                            }
                            Locale locale = null;
                            locale = column[part].equals("today") ? Locale.getDefault() : m18n.getLocale();
                            segment.setString(row, partcolumn[part], DateFormatter.formatDateTime(cal, format[part], locale, cal.getTimeZone()));
                            continue block15;
                        }
                        case 'N': {
                            partactive[part] = true;
                            int intvalue = ds.getInt(row, column[part]);
                            segment.setString(row, partcolumn[part], NumericFormatter.formatNumber(intvalue, format[part]));
                            continue block15;
                        }
                    }
                }
            }
            this.logInfo("KEYGEN " + (System.currentTimeMillis() - starttime) + " Generating sequences");
            SequenceProcessor sequenceProcessor = null;
            if (!testMode) {
                sequenceProcessor = new SequenceProcessor(this.sapphireConnection.getConnectionId());
            }
            for (int part = 0; part < parts; ++part) {
                int colpart;
                if (parttype[part] != 'Q') continue;
                partactive[part] = true;
                int newsequencestart = Integer.parseInt(subpart4[part]);
                try {
                    colpart = column[part] == null || column[part].length() == 0 ? 0 : Integer.parseInt(column[part]);
                }
                catch (NumberFormatException ignore) {
                    colpart = 0;
                }
                if (column[part] != null && column[part].indexOf(",") >= 0) {
                    colpart = 1;
                }
                if (colpart == 0 || colpart >= part) {
                    segment.setString(-1, partcolumn[part], sdcsequenceid);
                } else {
                    String[] sequenceonpart = StringUtil.split(column[part], ",");
                    int sequenceonparts = sequenceonpart.length;
                    for (int row2 = 0; row2 < rows; ++row2) {
                        String sequenceid = "";
                        for (int j = 0; j < sequenceonparts; ++j) {
                            sequenceid = sequenceid + segment.getValue(row2, sequenceonpart[j], "(null)");
                        }
                        segment.setString(row2, partcolumn[part], sequenceid);
                    }
                }
                segment.sort(partcolumn[part]);
                int initialrow = 0;
                for (int row3 = 0; row3 < rows; ++row3) {
                    int i;
                    if (row3 != rows - 1 && segment.getString(row3, partcolumn[part]).equals(segment.getString(row3 + 1, partcolumn[part]))) continue;
                    int grabcount = row3 - initialrow + 1;
                    int sequencestart = newsequencestart;
                    if (!testMode) {
                        sequencestart = sequenceProcessor.getSequence(sdcid, segment.getString(row3, partcolumn[part]), newsequencestart, grabcount);
                    }
                    if (sequencestart == -1) {
                        throw new ServiceException("Unable to grab numbers for sdc " + sdcid + " and format " + format[part]);
                    }
                    int[] newnumber = new int[grabcount];
                    for (i = 0; i < grabcount; ++i) {
                        newnumber[i] = sequencestart + i;
                    }
                    String[] newvalue = NumericFormatter.formatNumber(newnumber, format[part]);
                    for (i = 0; i < grabcount; ++i) {
                        segment.setString(initialrow + i, partcolumn[part], newvalue[i]);
                    }
                    initialrow = row3 + 1;
                }
            }
            this.logInfo("KEYGEN " + (System.currentTimeMillis() - starttime) + " Extracting and populating the keys");
            segment.sort("__sequence");
            for (int row4 = 0; row4 < rows; ++row4) {
                String newkeyid = "";
                for (int part = 0; part < parts; ++part) {
                    if (!partactive[part]) continue;
                    newkeyid = newkeyid + segment.getString(row4, partcolumn[part]);
                }
                ds.setString(row4, keycolumnid, newkeyid);
            }
            this.logInfo("KEYGEN " + (System.currentTimeMillis() - starttime) + " Complete");
        }
        catch (Exception e) {
            throw new ServiceException("Unexpected exception found", e);
        }
    }

    public static String updateSequences(SequenceProcessor sequenceProcessor, String sdcid, String key, String keyrule, boolean resequence) throws SapphireException {
        sdcid = sdcid.toLowerCase();
        StringBuffer newKey = new StringBuffer();
        String[] parttext = StringUtil.split(keyrule, ";");
        int parts = parttext.length;
        String[] format = new String[parts];
        String[] column = new String[parts];
        char[] parttype = new char[parts];
        long[] maxlength = new long[parts];
        String[] subpart4 = new String[parts];
        String[] partcolumn = new String[parts];
        String[] value = new String[parts];
        for (int i = 0; i < parts; ++i) {
            partcolumn[i] = String.valueOf(i);
            String[] subpart = StringUtil.split(parttext[i], "^");
            if (subpart.length > 0) {
                int n = parttype[i] = StringUtil.getLen(subpart[0]) > 0L ? (int)subpart[0].charAt(0) : 32;
            }
            if (subpart.length > 1) {
                try {
                    maxlength[i] = subpart.length > 1 ? Integer.valueOf(subpart[1]).longValue() : 0L;
                }
                catch (NumberFormatException e) {
                    maxlength[i] = 0L;
                }
            }
            if (subpart.length > 2) {
                format[i] = subpart[2];
            }
            if (subpart.length > 3) {
                column[i] = subpart[3];
            }
            if (subpart.length <= 4) continue;
            subpart4[i] = subpart[4];
        }
        boolean matchesKeyPattern = true;
        int pos = 0;
        block14: for (int part = 1; matchesKeyPattern && part < parts; ++part) {
            int partlen = 0;
            switch (parttype[part]) {
                case 'T': {
                    partlen = format[part].length();
                    if (pos + partlen > key.length()) {
                        matchesKeyPattern = false;
                        continue block14;
                    }
                    value[part] = key.substring(pos, pos + partlen);
                    pos += partlen;
                    if (!value[part].equals(format[part])) {
                        matchesKeyPattern = false;
                        continue block14;
                    }
                    newKey.append(value[part]);
                    continue block14;
                }
                case 'S': {
                    partlen = (int)maxlength[part];
                    if (pos + partlen > key.length()) {
                        matchesKeyPattern = false;
                        continue block14;
                    }
                    value[part] = key.substring(pos, pos + partlen);
                    pos += partlen;
                    newKey.append(value[part]);
                    continue block14;
                }
                case 'D': {
                    int n = partlen = format[part].equals("doy") ? 3 : format[part].length();
                    if (pos + partlen > key.length()) {
                        matchesKeyPattern = false;
                        continue block14;
                    }
                    value[part] = key.substring(pos, pos + partlen);
                    pos += partlen;
                    newKey.append(value[part]);
                    continue block14;
                }
                case 'N': {
                    partlen = format[part].length();
                    if (pos + partlen > key.length()) {
                        matchesKeyPattern = false;
                        continue block14;
                    }
                    value[part] = key.substring(pos, pos + partlen);
                    pos += partlen;
                    newKey.append(value[part]);
                    continue block14;
                }
                case 'Q': {
                    int seqnum;
                    int colpart;
                    partlen = format[part].length();
                    if (pos + partlen > key.length()) {
                        matchesKeyPattern = false;
                        continue block14;
                    }
                    value[part] = key.substring(pos, pos + partlen);
                    try {
                        colpart = column[part] == null || column[part].length() == 0 ? 0 : Integer.parseInt(column[part]);
                    }
                    catch (NumberFormatException ignore) {
                        colpart = 0;
                    }
                    if (column[part] != null && column[part].contains(",")) {
                        colpart = 1;
                    }
                    String basedon = "";
                    if (colpart == 0 || colpart >= part) {
                        basedon = "mainkey";
                    } else {
                        String[] sequenceonpart = StringUtil.split(column[part], ",");
                        int sequenceonparts = sequenceonpart.length;
                        for (int i = 0; i < sequenceonparts; ++i) {
                            basedon = basedon + value[Integer.parseInt(sequenceonpart[i])];
                        }
                    }
                    if (resequence) {
                        seqnum = sequenceProcessor.getSequence(sdcid, basedon, Integer.parseInt(subpart4[part]), 1);
                        value[part] = NumericFormatter.formatNumber(seqnum, format[part]);
                    } else {
                        try {
                            seqnum = Integer.parseInt(value[part]);
                            int currentseq = sequenceProcessor.getSequence(sdcid, basedon, 0);
                            if (currentseq >= 0) {
                                if (currentseq < seqnum) {
                                    sequenceProcessor.getSequence(sdcid, basedon, seqnum - currentseq);
                                }
                            } else {
                                sequenceProcessor.getSequence(sdcid, basedon, seqnum, 1);
                            }
                        }
                        catch (Exception e) {
                            return key;
                        }
                    }
                    pos += partlen;
                    newKey.append(value[part]);
                }
            }
        }
        if (!matchesKeyPattern) {
            return key;
        }
        return newKey.toString();
    }
}

