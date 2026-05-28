/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.search;

import com.labvantage.sapphire.gwt.shared.constants.DatasetNameConstants;
import java.util.HashSet;

public class IndexRequest
implements DatasetNameConstants {
    private boolean unlock = false;
    private boolean reset = false;
    private boolean processImmediate = true;
    private String sdcid = "(all)";
    private String keyid1 = "(all)";
    private String keyid2 = "(all)";
    private String keyid3 = "(all)";
    private HashSet datasets = new HashSet();

    public boolean isUnlock() {
        return this.unlock;
    }

    public void setUnlock(boolean unlock) {
        this.unlock = unlock;
    }

    public boolean isReset() {
        return this.reset;
    }

    public void setReset(boolean reset) {
        this.reset = reset;
    }

    public boolean isProcessImmediate() {
        return this.processImmediate;
    }

    public void setProcessImmediate(boolean processImmediate) {
        this.processImmediate = processImmediate;
    }

    public String getSdcid() {
        return this.sdcid;
    }

    public void setSdcid(String sdcid) {
        this.sdcid = sdcid;
    }

    public String getKeyid1() {
        return this.keyid1;
    }

    public void setKeyid1(String keyid1) {
        this.keyid1 = keyid1;
    }

    public String getKeyid2() {
        return this.keyid2;
    }

    public void setKeyid2(String keyid2) {
        this.keyid2 = keyid2;
    }

    public String getKeyid3() {
        return this.keyid3;
    }

    public void setKeyid3(String keyid3) {
        this.keyid3 = keyid3;
    }

    public String[] getDatasets() {
        return this.datasets.toArray(new String[0]);
    }

    public void addDataset(String datasetname) {
        this.datasets.add(datasetname);
    }

    public void addAllDatasets() {
        this.datasets.add("all");
    }

    public boolean hasAllDatasets() {
        return this.datasets.contains("all");
    }

    public boolean hasDataset(String datasetname) {
        return this.datasets.contains(datasetname);
    }
}

