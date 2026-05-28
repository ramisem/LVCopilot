/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.BaseClass;
import com.labvantage.sapphire.tagext.QueryData;
import com.labvantage.sapphire.tagext.SDITag;

public class QueryStatus
extends BaseClass {
    private QueryData queryData;
    private SDITag sdiTag;

    public QueryStatus(QueryData queryData) {
        this.queryData = queryData;
    }

    public QueryStatus(QueryData queryData, SDITag sdiTag) {
        this.queryData = queryData;
        this.sdiTag = sdiTag;
    }

    public int getSize() {
        return this.queryData != null ? this.queryData.size() : 0;
    }

    public int getRowCount() {
        return this.getSize();
    }

    public boolean getHasRows() {
        return this.queryData != null && this.queryData.size() > 0;
    }

    public boolean getHasNoRows() {
        return this.getNoRows();
    }

    public boolean getNoRows() {
        return this.queryData != null && this.queryData.size() == 0;
    }

    public boolean getRetrievedRow() {
        return this.queryData != null && this.queryData.getRowStatus(this.queryData.getCurrentRow()).equals("S");
    }

    public boolean getNewRow() {
        return this.queryData != null && this.queryData.getRowStatus(this.queryData.getCurrentRow()).equals("I");
    }

    public boolean getDeletedRow() {
        return this.queryData != null && this.queryData.getRowStatus(this.queryData.getCurrentRow()).equals("D");
    }

    public boolean getNotDeletedRow() {
        return this.queryData != null && !this.queryData.getRowStatus(this.queryData.getCurrentRow()).equals("D");
    }

    public boolean getModifiedRow() {
        return this.queryData != null && this.queryData.getRowStatus(this.queryData.getCurrentRow()).equals("U");
    }

    public boolean getLockedRow() {
        return this.queryData != null && this.queryData.getValue("__lockstate", "0").equals("2");
    }

    public boolean getLockSuccess() {
        return this.getLockedRow();
    }

    public boolean getUnlockedRow() {
        return this.queryData != null && this.queryData.getValue("__lockstate", "0").equals("0");
    }

    public boolean getLockFailure() {
        return this.getUnlockedRow();
    }

    public boolean getHasLockedRows() {
        boolean hasLockedRows = false;
        if (this.sdiTag != null) {
            String datasetName = this.queryData.getDatasetName();
            hasLockedRows = this.sdiTag.getLockoption().length() > 0 ? this.sdiTag.getRequestStatus() == 2 || this.sdiTag.getRequestStatus() == 101 : (datasetName.equalsIgnoreCase("dataset") || datasetName.equalsIgnoreCase("dataitem") || datasetName.equalsIgnoreCase("datalimit") || datasetName.equalsIgnoreCase("dataapproval") || datasetName.equalsIgnoreCase("dataspec") ? this.sdiTag.getRequestStatus() == 100 || this.sdiTag.getRequestStatus() == 102 : this.sdiTag.getRequestStatus() == 100 || this.sdiTag.getRequestStatus() == 101);
        }
        return hasLockedRows;
    }
}

