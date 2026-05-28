/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.diff;

import sapphire.util.DataSet;

public class BaseDiff {
    DataSet results = new DataSet();
    public static final String COLUMN_DIFFITEM = "level";
    public static final String COLUMN_PROPERTYSTATUS = "propertystatus";
    public static final String COLUMN_PROPERTYID = "propertyid";
    public static final String COLUMN_OLDVALUE = "oldvalue";
    public static final String COLUMN_NEWVALUE = "newvalue";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String HEADER_ITEM = "Item";
    public static final String HEADER_STATUS = "Status";
    public static final String HEADER_IDENTIFIER = "Identifier";
    public static final String HEADER_OLDVALUE = "From";
    public static final String HEADER_NEWVALUE = "To";
    public static final String MODIFIED = "Changed";
    public static final String DELETED = "Deleted";
    public static final String ADDED = "Added";
    public static final String MOVED = "Moved";

    BaseDiff() {
    }

    DataSet getResults() {
        return this.results;
    }

    boolean hasDifferences() {
        return this.results.getRowCount() > 0;
    }

    protected void printResults() {
        this.results.showData();
    }

    protected void addDiffResult(String diffItem, String diffItemId, String diffType, String originalval, String modifiedval, String description) {
        int newRow = this.results.addRow();
        this.results.setString(newRow, COLUMN_PROPERTYID, diffItemId);
        this.results.setString(newRow, COLUMN_DIFFITEM, diffItem);
        this.results.setString(newRow, COLUMN_PROPERTYSTATUS, diffType);
        this.results.setString(newRow, COLUMN_OLDVALUE, originalval);
        this.results.setString(newRow, COLUMN_NEWVALUE, modifiedval);
    }

    protected DataSet addDiffResults(DataSet original, DataSet diffItems, String diffItemType) {
        for (int i = 0; i < diffItems.getRowCount(); ++i) {
            int newRow = original.addRow();
            original.setString(newRow, COLUMN_PROPERTYID, diffItems.getString(i, COLUMN_PROPERTYID));
            original.setString(newRow, COLUMN_DIFFITEM, diffItems.getString(i, COLUMN_DIFFITEM));
            original.setString(newRow, COLUMN_PROPERTYSTATUS, diffItems.getString(i, COLUMN_PROPERTYSTATUS));
            original.setString(newRow, COLUMN_OLDVALUE, diffItems.getString(i, COLUMN_OLDVALUE));
            original.setString(newRow, COLUMN_NEWVALUE, diffItems.getString(i, COLUMN_NEWVALUE));
        }
        return original;
    }

    protected void addNodeDiffResult(String nodeId, String diffType, String description) {
        int newRow = this.results.addRow();
        this.results.setString(newRow, COLUMN_DIFFITEM, nodeId);
        this.results.setString(newRow, COLUMN_PROPERTYSTATUS, diffType);
        this.results.setString(newRow, COLUMN_DESCRIPTION, description);
    }

    protected DataSet addNodeDiffResults(DataSet original, DataSet diffItems) {
        for (int i = 0; i < diffItems.getRowCount(); ++i) {
            int newRow = original.addRow();
            original.setString(newRow, COLUMN_DIFFITEM, diffItems.getString(i, COLUMN_DIFFITEM));
            original.setString(newRow, COLUMN_PROPERTYSTATUS, diffItems.getString(i, COLUMN_PROPERTYSTATUS));
            original.setString(newRow, COLUMN_DESCRIPTION, diffItems.getString(i, COLUMN_DESCRIPTION));
        }
        return original;
    }
}

