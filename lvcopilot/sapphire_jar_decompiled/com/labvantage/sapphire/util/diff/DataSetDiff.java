/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.diff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import sapphire.util.DataSet;

public class DataSetDiff {
    public static final String ROW_ADDED = "Row Added";
    public static final String ROW_CHANGED = "Row Changed";
    public static final String ROW_DELETED = "Row Deleted";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_ROWID = "rowid";
    public static final String COLUMN_SEQUENCE = "sequence";
    public static final String COLUMN_COLUMNID = "columnid";
    public static final String COLUMN_OLDVALUE = "oldvalue";
    public static final String COLUMN_NEWVALUE = "newvalue";
    DataSet original;
    DataSet modified;
    DataSet results;
    List keyColumns;
    List ignoreColumns;
    List titleColumns;

    private DataSetDiff() {
    }

    public boolean hasDifferences() {
        return this.results.size() > 0;
    }

    public DataSetDiff(DataSet original, DataSet modified, String[] keyColumns, String[] ignoreColumns, String[] titleColumns) {
        this.original = original == null ? new DataSet() : new DataSet(original.toXML(true));
        this.modified = modified;
        this.keyColumns = Arrays.asList(keyColumns);
        this.titleColumns = Arrays.asList(titleColumns == null ? new String[]{} : titleColumns);
        this.ignoreColumns = Arrays.asList(ignoreColumns == null ? new String[]{} : ignoreColumns);
        this.results = new DataSet();
        this.performDiff();
    }

    private void performDiff() {
        HashMap<String, String> filter = new HashMap<String, String>();
        for (int modifiedRow = 0; modifiedRow < this.modified.size(); ++modifiedRow) {
            for (String keyColumnid : this.keyColumns) {
                filter.put(keyColumnid, this.modified.getValue(modifiedRow, keyColumnid));
            }
            int originalRow = this.original.findRow(filter);
            String rowId = this.getRowId(this.modified, modifiedRow);
            if (originalRow == -1) {
                this.addGroupHeader(ROW_ADDED, rowId);
                this.addAllValues(this.modified, modifiedRow, ROW_ADDED, rowId, COLUMN_NEWVALUE);
                continue;
            }
            boolean changes = this.addChangedValues(originalRow, modifiedRow, rowId);
            if (changes) {
                this.addGroupHeader(ROW_CHANGED, rowId);
            }
            this.original.deleteRow(originalRow);
        }
        for (int originalRow = 0; originalRow < this.original.size(); ++originalRow) {
            String rowId = this.getRowId(this.original, originalRow);
            this.addGroupHeader(ROW_DELETED, rowId);
            this.addAllValues(this.original, originalRow, ROW_DELETED, rowId, COLUMN_OLDVALUE);
        }
        this.results.sort("status,rowid,sequence");
    }

    private void addGroupHeader(String status, String rowId) {
        int newRow = this.results.addRow();
        this.results.setString(newRow, COLUMN_STATUS, status);
        this.results.setString(newRow, COLUMN_ROWID, rowId);
        this.results.setNumber(newRow, COLUMN_SEQUENCE, 0);
    }

    private void addAllValues(DataSet ds, int row, String status, String rowId, String oldOrNewColumn) {
        String[] columns = ds.getColumns();
        for (int i = 0; i < columns.length; ++i) {
            String columnid = columns[i];
            if (this.keyColumns.contains(columnid) || this.ignoreColumns.contains(columnid)) continue;
            String bValue = ds.getValue(row, columnid);
            int newRow = this.results.addRow();
            this.results.setString(newRow, COLUMN_STATUS, status);
            this.results.setString(newRow, COLUMN_ROWID, rowId);
            this.results.setString(newRow, COLUMN_COLUMNID, columnid);
            this.results.setString(newRow, oldOrNewColumn, bValue);
            this.results.setNumber(newRow, COLUMN_SEQUENCE, i);
        }
    }

    private boolean addChangedValues(int originalRow, int modifiedRow, String rowId) {
        boolean changes = false;
        String[] columns = this.modified.getColumns();
        for (int i = 0; i < columns.length; ++i) {
            String aValue;
            String bValue;
            String columnid = columns[i];
            if (this.keyColumns.contains(columnid) || this.ignoreColumns.contains(columnid) || (bValue = this.modified.getValue(modifiedRow, columnid)).equals(aValue = this.original.getValue(originalRow, columnid))) continue;
            changes = true;
            int newRow = this.results.addRow();
            this.results.setString(newRow, COLUMN_STATUS, ROW_CHANGED);
            this.results.setString(newRow, COLUMN_ROWID, rowId);
            this.results.setString(newRow, COLUMN_COLUMNID, columnid);
            this.results.setString(newRow, COLUMN_OLDVALUE, aValue);
            this.results.setString(newRow, COLUMN_NEWVALUE, bValue);
            this.results.setNumber(newRow, COLUMN_SEQUENCE, i);
        }
        return changes;
    }

    private String getRowId(DataSet ds, int row) {
        Iterator iterator;
        StringBuffer keyValues = new StringBuffer();
        Iterator iterator2 = iterator = this.titleColumns.size() > 0 ? this.titleColumns.iterator() : this.keyColumns.iterator();
        while (iterator.hasNext()) {
            String keyColumnid = (String)iterator.next();
            keyValues.append(";").append(ds.getValue(row, keyColumnid));
        }
        return keyValues.substring(1);
    }

    public DataSet getResults() {
        return this.results;
    }

    public String toHTML(String itemName, boolean showIdColumn, String stylePrefix) {
        StringBuffer html = new StringBuffer();
        this.results.sort("status,rowid,sequence");
        ArrayList<DataSet> groups = this.results.getGroupedDataSets("status,rowid");
        if (showIdColumn) {
            html.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"1\">");
        }
        for (DataSet group : groups) {
            String status = group.getValue(0, COLUMN_STATUS);
            String rowid = group.getValue(0, COLUMN_ROWID);
            if (showIdColumn) {
                html.append("<tr>");
            }
            boolean showOld = false;
            boolean showNew = false;
            String headerText = "";
            if (status.equals(ROW_ADDED)) {
                headerText = "New " + itemName + ": " + rowid;
                showNew = true;
            } else if (status.equals(ROW_DELETED)) {
                headerText = "Deleted " + itemName + ": " + rowid;
                showOld = true;
            }
            if (status.equals(ROW_CHANGED)) {
                headerText = itemName + " Modified: " + rowid;
                showOld = true;
                showNew = true;
            }
            if (showIdColumn) {
                html.append("<td class=\"" + stylePrefix + "_rowheader\" valign=\"top\">").append(headerText).append("</td>");
                html.append("<td>");
            }
            html.append("<table class=\"" + stylePrefix + "_innertable\" cellspacing=\"0\" cellpadding=\"2\" border=\"1\">");
            html.append("<tr>");
            html.append("<th class=\"" + stylePrefix + "_colheader\">Column</th>");
            if (showOld) {
                html.append("<th class=\"" + stylePrefix + "_colheader\">Old Value</th>");
            }
            if (showNew) {
                html.append("<th class=\"" + stylePrefix + "_colheader\">New Value</th>");
            }
            html.append("</tr>");
            for (int i = 1; i < group.size(); ++i) {
                html.append("<tr>");
                html.append("<td>" + group.getValue(i, COLUMN_COLUMNID, "&nbsp;") + "</td>");
                if (showOld) {
                    html.append("<td>" + group.getValue(i, COLUMN_OLDVALUE, "&nbsp;") + "</td>");
                }
                if (showNew) {
                    html.append("<td>" + group.getValue(i, COLUMN_NEWVALUE, "&nbsp;") + "</td>");
                }
                html.append("</tr>");
            }
            html.append("</table>");
            if (!showIdColumn) continue;
            html.append("</td>");
            html.append("</tr>");
        }
        if (showIdColumn) {
            html.append("</table>");
        }
        return html.toString();
    }
}

