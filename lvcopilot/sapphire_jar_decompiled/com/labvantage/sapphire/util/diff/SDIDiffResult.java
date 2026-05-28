/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.diff;

import java.util.ArrayList;

public class SDIDiffResult {
    public String sdcid;
    public String keyid1;
    public String keyid2;
    public String keyid3;
    public String status;
    public static final String STATUS_ADDED = "Added to local";
    public static final String STATUS_CHANGED = "Difference Found";
    public static final String STATUS_MISSING = "Missing from local";
    public static final String STATUS_SAME = "No Change";
    ArrayList changeDetails = new ArrayList();

    public void addChangeDetail(String changeType, String changeTypeDetail, String sourceValue, String targetValue) {
        ChangeDetail cd = new ChangeDetail();
        cd.changeType = changeType;
        cd.changeTypeDetails = changeTypeDetail;
        cd.sourceValue = sourceValue;
        cd.targetValue = targetValue;
        this.changeDetails.add(cd);
    }

    class ChangeDetail {
        String changeType;
        String changeTypeDetails;
        String sourceValue;
        String targetValue;

        ChangeDetail() {
        }
    }
}

