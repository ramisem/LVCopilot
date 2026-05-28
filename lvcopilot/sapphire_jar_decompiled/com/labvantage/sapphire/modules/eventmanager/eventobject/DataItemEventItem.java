/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventobject;

import com.labvantage.sapphire.modules.eventmanager.eventobject.SDIEventItem;

public class DataItemEventItem
extends SDIEventItem {
    private String paramlistid;
    private String paramlistversionid;
    private String variantid;
    private int dataset;
    private String paramid;
    private String paramtype;
    private int replicateid;

    public DataItemEventItem(String sdcid, String keyid1, String keyid2, String keyid3, String paramlistid, String paramlistversionid, String variantid, int dataset, String paramid, String paramtype, int replicateid) {
        super(sdcid, keyid1, keyid2, keyid3);
        this.paramlistid = paramlistid;
        this.paramlistversionid = paramlistversionid;
        this.variantid = variantid;
        this.dataset = dataset;
        this.paramid = paramid;
        this.paramtype = paramtype;
        this.replicateid = replicateid;
    }

    public String getParamlistid() {
        return this.paramlistid;
    }

    public String getParamlistversionid() {
        return this.paramlistversionid;
    }

    public String getVariantid() {
        return this.variantid;
    }

    public int getDataset() {
        return this.dataset;
    }

    public String getParamid() {
        return this.paramid;
    }

    public String getParamtype() {
        return this.paramtype;
    }

    public int getReplicateid() {
        return this.replicateid;
    }

    @Override
    public String toString() {
        return super.toString() + " " + this.getParamlistid() + " (ver: " + this.getParamlistversionid() + ", var: " + this.getVariantid() + ") " + this.getParamid() + ":" + this.getParamtype() + " (" + this.getReplicateid() + ")";
    }
}

