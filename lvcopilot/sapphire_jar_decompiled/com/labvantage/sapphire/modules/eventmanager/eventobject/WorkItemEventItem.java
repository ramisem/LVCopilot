/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventobject;

import com.labvantage.sapphire.modules.eventmanager.eventobject.SDIEventItem;

public class WorkItemEventItem
extends SDIEventItem {
    private String workitemid;
    private String workitemversionid;
    private int workiteminstance;

    public WorkItemEventItem(String sdcid, String keyid1, String keyid2, String keyid3, String workitemid, String workitemversionid, int workiteminstance) {
        super(sdcid, keyid1, keyid2, keyid3);
        this.workitemid = workitemid;
        this.workitemversionid = workitemversionid;
        this.workiteminstance = workiteminstance;
    }

    public String getWorkitemid() {
        return this.workitemid;
    }

    public String getWorkitemversionid() {
        return this.workitemversionid;
    }

    public int getWorkiteminstance() {
        return this.workiteminstance;
    }

    @Override
    public String toString() {
        return super.toString() + " " + this.getWorkitemid() + " (ver: " + this.getWorkitemversionid() + ") Instance: " + this.getWorkiteminstance();
    }
}

