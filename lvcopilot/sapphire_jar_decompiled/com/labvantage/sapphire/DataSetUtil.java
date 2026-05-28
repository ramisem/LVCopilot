/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire;

import com.labvantage.sapphire.BaseClass;
import com.labvantage.sapphire.Trace;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;

public class DataSetUtil
extends BaseClass {
    public static final String INSERT_UPDATE = "__insertupdate";
    public static final String INSERT = "I";
    public static final String UPDATE = "U";
    public static final String IGNORE = "X";

    public static DataSet mergeColumns(DataSet outtable, List<MergeColumn> mergeColumns) {
        if (outtable.size() > 0 && mergeColumns != null && mergeColumns.size() > 0) {
            ArrayList<String> colsToDelete = new ArrayList<String>();
            for (int i = 0; i < mergeColumns.size(); ++i) {
                MergeColumn merge = mergeColumns.get(i);
                boolean start = true;
                int columnMergeTo = -1;
                try {
                    columnMergeTo = merge.from;
                }
                catch (Exception exception) {
                    // empty catch block
                }
                int columnMergeFrom = -1;
                try {
                    columnMergeFrom = merge.to;
                }
                catch (Exception exception) {
                    // empty catch block
                }
                if (columnMergeFrom <= -1 || columnMergeTo <= -1 || columnMergeFrom >= outtable.getColumnCount() || columnMergeTo >= outtable.getColumnCount()) continue;
                String colto = outtable.getColumnId(columnMergeTo);
                String colfrom = outtable.getColumnId(columnMergeFrom);
                colsToDelete.add(colfrom);
                if (colsToDelete.contains(colto)) {
                    colsToDelete.remove(colto);
                }
                for (int r = 0; r < outtable.getRowCount(); ++r) {
                    String from = outtable.getValue(r, colfrom, "");
                    String to = outtable.getValue(r, colto, "");
                    String finalString = to + (from.length() > 0 && to.length() > 0 ? " " : "") + from;
                    outtable.setValue(r, colto, finalString);
                }
            }
            DataSet out = new DataSet();
            for (int c = 0; c < outtable.getColumnCount(); ++c) {
                if (colsToDelete.contains(outtable.getColumnId(c))) continue;
                out.addColumnValues(outtable.getColumnId(c), outtable.getColumnType(outtable.getColumnId(c)), outtable.getColumnValues(outtable.getColumnId(c), "{|}"), "{|}");
            }
            outtable = out;
        }
        return outtable;
    }

    public static String[] getStandardSysCols() {
        String[] cols = new String[]{"createdt", "createby", "createtool", "moddt", "modby", "modtool", "auditsequence", "tracelogid"};
        return cols;
    }

    public static String getStandardSysColsClause() {
        return "'createdt','createby','createtool','moddt','modby','modtool','auditsequence','tracelogid'";
    }

    public static void addUpdateColumn(DataSet ds) {
        ds.addColumn(INSERT_UPDATE, 0);
        ds.setValue(-1, INSERT_UPDATE, IGNORE);
    }

    public static void setInsert(DataSet ds, int row) {
        ds.setValue(row, INSERT_UPDATE, INSERT);
    }

    public static void setUpdate(DataSet ds, int row) {
        ds.setValue(row, INSERT_UPDATE, UPDATE);
    }

    public static void setIgnore(DataSet ds, int row) {
        ds.setValue(row, INSERT_UPDATE, IGNORE);
    }

    public static void insertUpdate(DBAccess db, DataSet ds, String tableid, String[] keycolids) throws SapphireException {
        DataSetUtil.insert(db, ds, tableid);
        DataSetUtil.update(db, ds, tableid, keycolids);
    }

    public static void insert(DBAccess db, DataSet ds, String tableid) throws SapphireException {
        String[] columnid;
        int columns;
        boolean debug = Trace.isDebugEnabled();
        if (ds != null && ds.size() > 0 && (columns = (columnid = DataSetUtil.getUpdateableColumns(ds)).length) > 0) {
            StringBuffer columnlist = new StringBuffer();
            StringBuffer valuelist = new StringBuffer();
            int[] coltype = new int[columns];
            for (int column = 0; column < columns; ++column) {
                coltype[column] = ds.getColumnType(columnid[column]);
                columnlist.append(",").append(columnid[column]);
                valuelist.append(",?");
            }
            try {
                String sql = "INSERT INTO " + tableid + " ( " + columnlist.substring(1) + " ) VALUES ( " + valuelist.substring(1) + " ) ";
                if (debug) {
                    ds.showData();
                }
                if (debug) {
                    Trace.logDebug(sql);
                }
                PreparedStatement insert = db.prepareStatement(sql);
                int rows = ds.getRowCount();
                boolean useUpdateColumn = ds.isValidColumn(INSERT_UPDATE);
                for (int row = 0; row < rows; ++row) {
                    if (useUpdateColumn && (!useUpdateColumn || !ds.getValue(row, INSERT_UPDATE).equals(INSERT))) continue;
                    if (debug) {
                        Trace.logDebug("Processing row " + row);
                    }
                    block9: for (int column = 0; column < columns; ++column) {
                        switch (coltype[column]) {
                            case 0: 
                            case 3: {
                                String stringvalue = ds.getString(row, columnid[column]);
                                if (debug) {
                                    Trace.logDebug("  Col " + (column + 1) + " of " + columns + " - String " + columnid[column] + "=" + stringvalue);
                                }
                                if (stringvalue == null || stringvalue.length() == 0) {
                                    insert.setNull(column + 1, 12);
                                    continue block9;
                                }
                                if (coltype[column] == 3 && db.isSqlServer()) {
                                    insert.setCharacterStream(column + 1, (Reader)new StringReader(stringvalue), stringvalue.length());
                                    continue block9;
                                }
                                insert.setString(column + 1, stringvalue);
                                continue block9;
                            }
                            case 1: {
                                BigDecimal numbervalue = ds.getBigDecimal(row, columnid[column]);
                                if (debug) {
                                    Trace.logDebug("  Col " + (column + 1) + " of " + columns + " - Number " + columnid[column] + "=" + numbervalue);
                                }
                                if (numbervalue == null) {
                                    insert.setNull(column + 1, 8);
                                    continue block9;
                                }
                                insert.setBigDecimal(column + 1, numbervalue);
                                continue block9;
                            }
                            case 2: {
                                Calendar c = ds.getCalendar(row, columnid[column]);
                                if (debug) {
                                    Trace.logDebug("  Col " + (column + 1) + " of " + columns + " - Date " + columnid[column] + "=" + c);
                                }
                                if (c == null) {
                                    insert.setNull(column + 1, 93);
                                    continue block9;
                                }
                                Timestamp datevalue = new Timestamp(c.getTime().getTime());
                                insert.setTimestamp(column + 1, datevalue);
                                continue block9;
                            }
                            default: {
                                if (!debug) continue block9;
                                Trace.logDebug("  Col " + (column + 1) + " of " + columns + " - " + coltype[column] + "=" + columnid[column] + "=" + ds.getValue(row, columnid[column]));
                            }
                        }
                    }
                    insert.addBatch();
                }
                int[] status = insert.executeBatch();
                if (debug) {
                    for (int i = 0; i < status.length; ++i) {
                        Trace.logDebug(Integer.toString(status[i]));
                    }
                }
                db.closeStatement();
            }
            catch (Exception e) {
                Trace.logError("DS-UTIL", (Object)("ERROR: " + e.getClass() + " Exception caught during insert: " + e.getMessage()), e);
                throw new SapphireException("DataSetUtil.insert Exception: " + e.getMessage());
            }
        }
    }

    public static void update(DBAccess db, DataSet ds, String tableid, String[] keycolids) throws SapphireException {
        boolean debug = Trace.isDebugEnabled();
        if (ds != null && ds.size() > 0) {
            String[] columnid = DataSetUtil.getUpdateableColumns(ds);
            int columns = columnid.length;
            int keycols = keycolids.length;
            String[] bindcolumnid = new String[columns];
            int[] bindcolumntype = new int[columns];
            int bindcolumns = 0;
            StringBuffer updatelist = new StringBuffer("");
            for (int column = 0; column < columns; ++column) {
                boolean keycol = false;
                for (int i = 0; i < keycols; ++i) {
                    if (!columnid[column].equalsIgnoreCase(keycolids[i])) continue;
                    keycol = true;
                }
                if (keycol) continue;
                bindcolumnid[bindcolumns] = columnid[column];
                bindcolumntype[bindcolumns] = ds.getColumnType(columnid[column]);
                ++bindcolumns;
                if (updatelist.length() > 0) {
                    updatelist.append(", ");
                }
                updatelist.append(columnid[column]).append(" = ?");
            }
            StringBuffer whereclause = new StringBuffer("");
            for (int i = 0; i < keycols; ++i) {
                if (keycolids[i] == null || keycolids[i].length() <= 0) continue;
                if (i > 0) {
                    whereclause.append(" and ");
                }
                whereclause.append(keycolids[i]).append("=?");
                bindcolumnid[bindcolumns] = keycolids[i];
                bindcolumntype[bindcolumns] = ds.getColumnType(keycolids[i]);
                ++bindcolumns;
            }
            try {
                String sql = "UPDATE " + tableid + " SET " + updatelist.toString() + " WHERE " + whereclause.toString();
                if (debug) {
                    Trace.logDebug(sql);
                }
                PreparedStatement update = db.prepareStatement(sql);
                boolean useUpdateColumn = ds.isValidColumn(INSERT_UPDATE);
                int rows = ds.getRowCount();
                for (int row = 0; row < rows; ++row) {
                    if (useUpdateColumn && (!useUpdateColumn || !ds.getValue(row, INSERT_UPDATE).equals(UPDATE))) continue;
                    block11: for (int bindcolumn = 0; bindcolumn < bindcolumns; ++bindcolumn) {
                        switch (bindcolumntype[bindcolumn]) {
                            case 0: 
                            case 3: {
                                String stringvalue = ds.getString(row, bindcolumnid[bindcolumn]);
                                if (debug) {
                                    Trace.logDebug("Setting string for " + bindcolumnid[bindcolumn] + ", value is: -->" + stringvalue + "<--");
                                }
                                if (stringvalue == null || stringvalue.length() == 0) {
                                    update.setNull(bindcolumn + 1, 12);
                                } else if (bindcolumntype[bindcolumn] == 3 && db.isSqlServer()) {
                                    update.setCharacterStream(bindcolumn + 1, (Reader)new StringReader(stringvalue), stringvalue.length());
                                } else {
                                    update.setString(bindcolumn + 1, stringvalue);
                                }
                                if (!debug) continue block11;
                                Trace.logDebug("Done");
                                continue block11;
                            }
                            case 1: {
                                BigDecimal numbervalue;
                                if (debug) {
                                    Trace.logDebug("Setting number for " + bindcolumnid[bindcolumn]);
                                }
                                if ((numbervalue = ds.getBigDecimal(row, bindcolumnid[bindcolumn])) == null) {
                                    update.setNull(bindcolumn + 1, 8);
                                } else {
                                    update.setBigDecimal(bindcolumn + 1, numbervalue);
                                }
                                if (!debug) continue block11;
                                Trace.logDebug("Done");
                                continue block11;
                            }
                            case 2: {
                                Calendar c;
                                if (debug) {
                                    Trace.logDebug("Setting date for " + bindcolumnid[bindcolumn]);
                                }
                                if ((c = ds.getCalendar(row, bindcolumnid[bindcolumn])) == null) {
                                    update.setNull(bindcolumn + 1, 93);
                                } else {
                                    Timestamp datevalue = new Timestamp(c.getTime().getTime());
                                    update.setTimestamp(bindcolumn + 1, datevalue);
                                }
                                if (!debug) continue block11;
                                Trace.logDebug("Done");
                            }
                        }
                    }
                    update.addBatch();
                }
                update.executeBatch();
                db.closeStatement();
            }
            catch (Exception e) {
                Trace.logError("DS-UTIL", (Object)("ERROR: " + e.getClass() + " Exception caught during insert: " + e.getMessage()), e);
                throw new SapphireException("DataSetUtil.update Exception: " + e.getMessage());
            }
        }
    }

    public static String[] getUpdateableColumns(DataSet ds) {
        String columnid;
        int i;
        ArrayList<String> a = new ArrayList<String>();
        int cols = ds.getColumnCount();
        for (i = 0; i < cols; ++i) {
            columnid = ds.getColumnId(i);
            if (!Character.isLetter(columnid.charAt(0)) || ds.getColumnType(columnid) == 3) continue;
            a.add(columnid);
        }
        for (i = 0; i < cols; ++i) {
            columnid = ds.getColumnId(i);
            if (!Character.isLetter(columnid.charAt(0)) || ds.getColumnType(columnid) != 3) continue;
            a.add(columnid);
        }
        return a.toArray(new String[0]);
    }

    public static class MergeColumn {
        private int from = -1;
        private int to = -1;

        public int getFrom() {
            return this.from;
        }

        public int getTo() {
            return this.to;
        }

        public MergeColumn(int from, int to) {
            this.to = to;
            this.from = from;
        }
    }
}

