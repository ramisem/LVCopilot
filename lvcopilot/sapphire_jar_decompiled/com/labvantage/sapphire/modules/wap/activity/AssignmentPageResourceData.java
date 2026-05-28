/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.wap.activity;

import com.labvantage.sapphire.gwt.shared.JSONable;
import com.labvantage.sapphire.modules.wap.activity.AssignmentPage;
import com.labvantage.sapphire.modules.wap.activity.AssignmentPageResourceContainer;
import java.io.Serializable;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.util.DataSet;

public class AssignmentPageResourceData
implements Serializable,
JSONable {
    String workcontext = "";
    String testingDepartmentid = "";
    DataSet resourceRequirements = null;
    ArrayList<AssignmentPageResourceContainer> resources = null;

    public AssignmentPageResourceData() {
        this.testingDepartmentid = "";
        this.workcontext = "";
        this.resources = new ArrayList();
    }

    public String getTestingDepartmentId() {
        return this.testingDepartmentid;
    }

    public String getWorkContext() {
        return this.workcontext;
    }

    public DataSet getResourceRequirements() {
        return this.resourceRequirements;
    }

    public void setResourceRequirements(DataSet resourceRequirements) {
        this.resourceRequirements = resourceRequirements;
    }

    public ArrayList<AssignmentPageResourceContainer> getResources() {
        return this.resources;
    }

    public AssignmentPageResourceData(String testingDepartmentid, String workcontext) {
        this.testingDepartmentid = testingDepartmentid;
        this.workcontext = workcontext;
        this.resources = new ArrayList();
    }

    public AssignmentPageResourceData(JSONObject jsonObject) {
        try {
            this.testingDepartmentid = jsonObject.has("testingdepartmentid") ? jsonObject.getString("testingdepartmentid") : "";
            String string = this.workcontext = jsonObject.has("workcontext") ? jsonObject.getString("workcontext") : "";
            if (jsonObject.has("resourceRequirements")) {
                this.resourceRequirements = new DataSet(jsonObject.getJSONObject("resourceRequirements"));
            }
            JSONArray resourcesArray = jsonObject.has("resources") ? jsonObject.getJSONArray("resources") : new JSONArray();
            this.resources = new ArrayList();
            for (int i = 0; i < resourcesArray.length(); ++i) {
                String resourceLabel;
                String resourceid;
                JSONObject resource = resourcesArray.getJSONObject(i);
                if (!resource.has("resourceData") || !resource.has("resourceSDC")) continue;
                DataSet resourceData = new DataSet(resource.getJSONObject("resourceData"));
                DataSet workareaData = null;
                DataSet detailData = null;
                DataSet attachmentData = null;
                if (resource.has("workareaData") && resource.getJSONObject("workareaData") != null) {
                    workareaData = new DataSet(resource.getJSONObject("workareaData"));
                }
                if (resource.has("detailData") && resource.getJSONObject("detailData") != null) {
                    detailData = new DataSet(resource.getJSONObject("detailData"));
                }
                if (resource.has("attachmentData") && resource.getJSONObject("attachmentData") != null) {
                    attachmentData = new DataSet(resource.getJSONObject("attachmentData"));
                }
                AssignmentPage.ResourceSDC resourceSDC = AssignmentPage.ResourceSDC.valueOf(resource.getString("resourceSDC").toUpperCase());
                String string2 = resourceid = resource.has("resourceid") ? resource.getString("resourceid") : "";
                if (resourceid.length() == 0) {
                    resourceid = "res-" + i;
                }
                int resourcenum = resource.has("resourcenum") ? resource.getInt("resourcenum") : 0;
                String string3 = resourceLabel = resource.has("resourceLabel") ? resource.getString("resourceLabel") : "";
                if (resourceLabel.length() == 0) {
                    resourceLabel = "Resource " + (i + 1);
                }
                String resourceType = resource.has("resourceType") ? resource.getString("resourceType") : "";
                String resourceModel = resource.has("resourceModel") ? resource.getString("resourceModel") : "";
                String autoresource = resource.has("autoresource") ? resource.getString("autoresource") : "";
                String autoworkarea = resource.has("autoworkarea") ? resource.getString("autoworkarea") : "";
                String preferredsdi = resource.has("preferredSDI") ? resource.getString("preferredSDI") : "";
                String transientSDI = resource.has("transientSDI") ? resource.getString("transientSDI") : "";
                String preferredworkarea = resource.has("preferredWorkarea") ? resource.getString("preferredWorkarea") : "";
                boolean showAll = resource.has("showAll") && resource.getBoolean("showAll");
                HashMap<String, ZoneId> tz = new HashMap<String, ZoneId>();
                if (resource.has("timezones")) {
                    JSONObject timezones = resource.getJSONObject("timezones");
                    Iterator t = timezones.keys();
                    while (t.hasNext()) {
                        String tid = t.next().toString();
                        ZoneId tzone = TimeZone.getTimeZone(timezones.getString(tid)).toZoneId();
                        tz.put(tid, tzone);
                    }
                }
                AssignmentPageResourceContainer resourceContainer = new AssignmentPageResourceContainer(resourceid, resourcenum, preferredsdi, preferredworkarea, transientSDI, resourceLabel, resourceSDC, resourceType, resourceModel, autoresource, autoworkarea, showAll, resourceData, workareaData, detailData, attachmentData, tz);
                this.resources.add(resourceContainer);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public JSONObject toJSONObject() {
        JSONObject out = new JSONObject();
        try {
            out.put("workcontext", this.workcontext);
            out.put("testingdepartmentid", this.testingDepartmentid);
            if (this.resourceRequirements != null) {
                out.put("resourceRequirements", this.resourceRequirements.toJSONObject());
            }
            if (this.resources != null) {
                JSONArray resourcesArray = new JSONArray();
                for (AssignmentPageResourceContainer resource : this.resources) {
                    if (resource.resourceData == null) continue;
                    JSONObject resourceContainer = new JSONObject();
                    resourceContainer.put("resourceid", resource.resourceid);
                    resourceContainer.put("resourcenum", resource.resourcenum);
                    resourceContainer.put("resourceLabel", resource.resourceLabel);
                    resourceContainer.put("resourceType", resource.resourceType);
                    resourceContainer.put("resourceModel", resource.resourceModel);
                    resourceContainer.put("resourceSDC", resource.resourceSDC.toString());
                    resourceContainer.put("resourceData", resource.resourceData.toJSONObject());
                    resourceContainer.put("autoresource", resource.autoresource);
                    resourceContainer.put("autoworkarea", resource.autoworkarea);
                    resourceContainer.put("preferredSDI", resource.preferredSDI);
                    resourceContainer.put("transientSDI", resource.transientSDI);
                    resourceContainer.put("preferredWorkarea", resource.preferredWorkarea);
                    resourceContainer.put("showAll", resource.showAll);
                    JSONObject timezones = new JSONObject();
                    for (String s : resource.timezones.keySet()) {
                        timezones.putOpt(s, TimeZone.getTimeZone(resource.timezones.get(s)).getID());
                    }
                    resourceContainer.put("timezones", timezones);
                    if (resource.workareaData != null) {
                        resourceContainer.put("workareaData", resource.workareaData.toJSONObject());
                    }
                    if (resource.detailData != null) {
                        resourceContainer.put("detailData", resource.detailData.toJSONObject());
                    }
                    if (resource.attachmentData != null) {
                        resourceContainer.put("attachmentData", resource.attachmentData.toJSONObject());
                    }
                    resourcesArray.put(resourceContainer);
                }
                out.put("resources", resourcesArray);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return out;
    }

    @Override
    public String toJSONString() {
        return this.toJSONObject().toString();
    }
}

