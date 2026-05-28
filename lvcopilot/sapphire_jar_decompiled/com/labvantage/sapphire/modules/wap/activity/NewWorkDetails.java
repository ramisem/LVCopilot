/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.wap.activity;

import sapphire.util.DataSet;

public class NewWorkDetails {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    private String worksdcid;
    private int workCount;
    private int maxActivitySize;
    private DataSet resourceRequirements;
    private String workContext = "";
    private String testingDepartmentid;

    public NewWorkDetails(String worksdcid, int workCount, String testingDepartmentid, int maxActivitySize, DataSet resourceRequirements, String workContext) {
        this.worksdcid = worksdcid;
        this.workCount = workCount;
        this.resourceRequirements = resourceRequirements;
        this.maxActivitySize = maxActivitySize;
        this.testingDepartmentid = testingDepartmentid;
        this.workContext = workContext;
    }

    public String getTestingDepartmentid() {
        return this.testingDepartmentid;
    }

    public DataSet getResourceRequirements() {
        return this.resourceRequirements;
    }

    public int getMaxActivitySize() {
        return this.maxActivitySize;
    }

    public String getWorksdcid() {
        return this.worksdcid;
    }

    public int getWorkCount() {
        return this.workCount;
    }

    public String getWorkContext() {
        return this.workContext;
    }

    public void setWorkContext(String workContext) {
        this.workContext = workContext;
    }

    public void setWorkCount(int workCount) {
        this.workCount = workCount;
    }
}

