/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.wap.activity;

import com.labvantage.sapphire.modules.wap.activity.AssignmentPage;
import java.io.Serializable;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.TimeZone;
import sapphire.util.DataSet;

public class AssignmentPageResourceContainer
implements Serializable {
    DataSet resourceData;
    DataSet workareaData;
    DataSet detailData;
    DataSet attachmentData;
    AssignmentPage.ResourceSDC resourceSDC;
    String resourceid;
    int resourcenum;
    String resourceLabel;
    String resourceType;
    String resourceModel;
    String autoresource;
    String autoworkarea;
    boolean showAll;
    String preferredSDI;
    String transientSDI;
    String preferredWorkarea;
    HashMap<String, ZoneId> timezones;

    public AssignmentPageResourceContainer(String resourceid, int resourcenum, String preferredSDI, String preferredWorkarea, String transientSDI, String resourceLabel, AssignmentPage.ResourceSDC resourceSDC, String resourceType, String resourceModel, String autoresource, String autoworkarea, boolean showAll, DataSet resourceData, DataSet workareaData, DataSet detailData, DataSet attachmentData, HashMap<String, ZoneId> timezones) {
        this.resourceData = resourceData;
        this.workareaData = workareaData;
        this.detailData = detailData;
        this.attachmentData = attachmentData;
        this.resourceSDC = resourceSDC;
        this.resourceLabel = resourceLabel;
        this.resourceid = resourceid;
        this.resourcenum = resourcenum;
        this.resourceType = resourceType;
        this.resourceModel = resourceModel;
        this.autoresource = autoresource;
        this.autoworkarea = autoworkarea;
        this.preferredSDI = preferredSDI;
        this.transientSDI = transientSDI;
        this.preferredWorkarea = preferredWorkarea;
        this.showAll = showAll;
        this.timezones = timezones;
    }

    public String getId() {
        return this.resourceid;
    }

    public TimeZone getTimeZone(String resourceid) {
        if (this.timezones.containsKey(resourceid)) {
            return TimeZone.getTimeZone(this.timezones.get(resourceid));
        }
        return TimeZone.getDefault();
    }

    public int getNum() {
        return this.resourcenum;
    }

    public String getLabel() {
        return this.resourceLabel;
    }

    public String getType() {
        return this.resourceType;
    }

    public String getModel() {
        return this.resourceModel;
    }

    public DataSet getData() {
        return this.resourceData;
    }

    public DataSet getWorkareas() {
        return this.workareaData;
    }

    public DataSet getDetail() {
        return this.detailData;
    }

    public DataSet getAttachment() {
        return this.attachmentData;
    }

    public AssignmentPage.ResourceSDC getResourceSDC() {
        return this.resourceSDC;
    }

    public String getAutoAssignResource() {
        return this.autoresource;
    }

    public String getAutoAssignWorkarea() {
        return this.autoworkarea;
    }

    public void setShowAll(boolean showAll) {
        this.showAll = showAll;
    }

    public boolean isShowAll() {
        return this.showAll;
    }
}

