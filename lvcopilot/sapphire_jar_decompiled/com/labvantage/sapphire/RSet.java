/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire;

import java.io.Serializable;
import sapphire.util.DataSet;

public class RSet
implements Serializable {
    public static final String LOCK_ALL = "DA";
    public static final String LOCK_PARTIAL = "LA";
    public static final int LOCK_PRIMARY = 1;
    public static final int LOCK_DATASET = 2;
    public static final int LOCK_BOTH = 3;
    private String rsetid;
    private String sdcid;
    private DataSet rsetitems;
    private DataSet rsetitemsds;
    private int qualifiedRows;
    private int requestStatus;
    private int primaryStatus;
    private int datasetStatus;

    public RSet(String rsetid) {
        this.rsetid = rsetid;
    }

    public void setRSet(RSet rset) {
        this.rsetid = rset.getRsetid();
        this.rsetitems = rset.getRsetitems();
        this.rsetitemsds = rset.getRsetitemsds();
        this.qualifiedRows = rset.getQualifiedRows();
        this.requestStatus = rset.getRequestStatus();
        this.primaryStatus = rset.getPrimaryStatus();
        this.datasetStatus = rset.getDatasetStatus();
    }

    public String getRsetid() {
        return this.rsetid;
    }

    public void setRsetid(String rsetid) {
        this.rsetid = rsetid;
    }

    public String getSdcid() {
        return this.sdcid;
    }

    public void setSdcid(String sdcid) {
        this.sdcid = sdcid;
    }

    public DataSet getRsetitems() {
        return this.rsetitems;
    }

    public void setRsetitems(DataSet rsetitems) {
        this.rsetitems = rsetitems;
    }

    public DataSet getRsetitemsds() {
        return this.rsetitemsds;
    }

    public void setRsetitemsds(DataSet rsetitemsds) {
        this.rsetitemsds = rsetitemsds;
    }

    public int getQualifiedRows() {
        return this.qualifiedRows;
    }

    public void setQualifiedRows(int qualifiedRows) {
        this.qualifiedRows = qualifiedRows;
    }

    public int getRequestStatus() {
        return this.requestStatus;
    }

    public void setRequestStatus(int requestStatus) {
        this.requestStatus = requestStatus;
    }

    public int getPrimaryStatus() {
        return this.primaryStatus;
    }

    public void setPrimaryStatus(int primaryStatus) {
        this.primaryStatus = primaryStatus;
    }

    public int getDatasetStatus() {
        return this.datasetStatus;
    }

    public void setDatasetStatus(int datasetStatus) {
        this.datasetStatus = datasetStatus;
    }

    public String toString() {
        return this.rsetid;
    }
}

