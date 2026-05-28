/*
 * Decompiled with CFR 0.152.
 */
package sapphire.util;

import com.labvantage.sapphire.Trace;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.util.HttpUtil;
import sapphire.xml.PropertyList;

public class TaskContext {
    private String taskexecid;
    private String taskdefid;
    private String taskdefversionid;
    private String taskdefvariantid;
    private String workflowexecid;
    private String workflowexecname;
    private String workflowdefid;
    private String workflowdefversionid;
    private String workflowdefvariantid;
    private String taskdefitemid;
    private String taskexecgroup;
    private String stepid;
    private String hostFrameId;
    private int stepCount;
    private boolean taskPage;
    private boolean back;
    private boolean testMode;
    private boolean standaloneMode;
    private PropertyList stepProps;

    public TaskContext() {
    }

    public TaskContext(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        this.setJSONObject(jsonObject);
    }

    public TaskContext(PropertyList requestProps) {
        for (String propertyid : requestProps.keySet()) {
            if (propertyid.equals("taskpage")) {
                this.taskPage = requestProps.getProperty(propertyid).equals("Y");
                continue;
            }
            if (propertyid.equals("__taskexecid")) {
                this.setTaskexecid(requestProps.getProperty(propertyid));
                continue;
            }
            if (propertyid.equals("__taskdefid")) {
                this.setTaskdefid(requestProps.getProperty(propertyid));
                continue;
            }
            if (propertyid.equals("__taskdefversionid")) {
                this.setTaskdefversionid(requestProps.getProperty(propertyid));
                continue;
            }
            if (propertyid.equals("__taskdefvariantid")) {
                this.setTaskdefvariantid(requestProps.getProperty(propertyid));
                continue;
            }
            if (propertyid.equals("__workflowexecid")) {
                this.setWorkflowexecid(requestProps.getProperty(propertyid));
                continue;
            }
            if (propertyid.equals("__workflowdefid")) {
                this.setWorkflowdefid(requestProps.getProperty(propertyid));
                continue;
            }
            if (propertyid.equals("__workflowdefversionid")) {
                this.setWorkflowdefversionid(requestProps.getProperty(propertyid));
                continue;
            }
            if (propertyid.equals("__workflowdefvariantid")) {
                this.setWorkflowdefvariantid(requestProps.getProperty(propertyid));
                continue;
            }
            if (propertyid.equals("__taskdefitemid")) {
                this.setTaskdefitemid(requestProps.getProperty(propertyid));
                continue;
            }
            if (propertyid.equals("__taskexecgroup")) {
                this.setTaskexecgroup(requestProps.getProperty(propertyid));
                continue;
            }
            if (propertyid.equals("__stepid")) {
                this.setStepid(requestProps.getProperty(propertyid));
                continue;
            }
            if (propertyid.equals("__hostframeid")) {
                this.setHostFrameId(requestProps.getProperty(propertyid));
                continue;
            }
            if (propertyid.equals("__stepcount")) {
                this.setStepCount(Integer.parseInt(requestProps.getProperty(propertyid)));
                continue;
            }
            if (propertyid.equals("__back")) {
                this.setBack(requestProps.getProperty(propertyid, "N").equals("Y"));
                continue;
            }
            if (propertyid.equals("__testmode")) {
                this.setTestMode(requestProps.getProperty(propertyid, "N").equals("Y"));
                continue;
            }
            if (!propertyid.equals("__stepprops")) continue;
            String propsString = HttpUtil.decodeURIComponent(requestProps.getProperty(propertyid));
            try {
                this.stepProps = propsString != null && propsString.length() > 0 ? new PropertyList(new JSONObject(propsString)) : new PropertyList();
            }
            catch (JSONException e) {
                this.stepProps = new PropertyList();
            }
            this.setStepProps(this.stepProps);
        }
    }

    public boolean isTaskPage() {
        return this.taskPage;
    }

    public String getTaskexecid() {
        return this.taskexecid != null ? this.taskexecid : "";
    }

    public void setTaskexecid(String taskexecid) {
        this.taskexecid = taskexecid;
    }

    public String getTaskdefid() {
        return this.taskdefid != null ? this.taskdefid : "";
    }

    public void setTaskdefid(String taskdefid) {
        this.taskdefid = taskdefid;
    }

    public String getTaskdefversionid() {
        return this.taskdefversionid != null ? this.taskdefversionid : "";
    }

    public void setTaskdefversionid(String taskdefversionid) {
        this.taskdefversionid = taskdefversionid;
    }

    public String getTaskdefvariantid() {
        return this.taskdefvariantid != null ? this.taskdefvariantid : "";
    }

    public void setTaskdefvariantid(String taskdefvariantid) {
        this.taskdefvariantid = taskdefvariantid;
    }

    public String getWorkflowexecid() {
        return this.workflowexecid != null ? this.workflowexecid : "";
    }

    public void setWorkflowexecid(String workflowexecid) {
        this.workflowexecid = workflowexecid;
    }

    public String getWorkflowexecname() {
        return this.workflowexecname;
    }

    public void setWorkflowexecname(String workflowexecname) {
        this.workflowexecname = workflowexecname;
    }

    public String getWorkflowdefid() {
        return this.workflowdefid != null ? this.workflowdefid : "";
    }

    public void setWorkflowdefid(String workflowdefid) {
        this.workflowdefid = workflowdefid;
    }

    public String getWorkflowdefversionid() {
        return this.workflowdefversionid != null ? this.workflowdefversionid : "";
    }

    public void setWorkflowdefversionid(String workflowdefversionid) {
        this.workflowdefversionid = workflowdefversionid;
    }

    public String getWorkflowdefvariantid() {
        return this.workflowdefvariantid != null ? this.workflowdefvariantid : "";
    }

    public void setWorkflowdefvariantid(String workflowdefvariantid) {
        this.workflowdefvariantid = workflowdefvariantid;
    }

    public String getTaskdefitemid() {
        return this.taskdefitemid != null ? this.taskdefitemid : "";
    }

    public void setTaskdefitemid(String taskdefitemid) {
        this.taskdefitemid = taskdefitemid;
    }

    public String getTaskexecgroup() {
        return this.taskexecgroup != null ? this.taskexecgroup : "";
    }

    public void setTaskexecgroup(String taskexecgroup) {
        this.taskexecgroup = taskexecgroup;
    }

    public String getStepid() {
        return this.stepid != null ? this.stepid : "";
    }

    public void setStepid(String stepid) {
        this.stepid = stepid;
    }

    public String getHostFrameId() {
        return this.hostFrameId;
    }

    public void setHostFrameId(String hostFrameId) {
        this.hostFrameId = hostFrameId;
    }

    public int getStepCount() {
        return this.stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }

    public boolean isBack() {
        return this.back;
    }

    public void setBack(boolean back) {
        this.back = back;
    }

    public boolean isTestMode() {
        return this.testMode;
    }

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    public boolean isStandaloneMode() {
        return this.standaloneMode;
    }

    public void setStandaloneMode(boolean standaloneMode) {
        this.standaloneMode = standaloneMode;
    }

    public PropertyList getStepProps() {
        return this.stepProps;
    }

    public void setStepProps(PropertyList stepProps) {
        this.stepProps = stepProps;
    }

    public void setJSONObject(JSONObject jsonObject) {
        try {
            this.taskexecid = jsonObject.getString("taskexecid");
            this.taskdefid = jsonObject.getString("taskdefid");
            this.taskdefversionid = jsonObject.getString("taskdefversionid");
            this.taskdefvariantid = jsonObject.getString("taskdefvariantid");
            this.workflowexecid = jsonObject.getString("workflowexecid");
            this.workflowexecname = jsonObject.getString("workflowexecname");
            this.workflowdefid = jsonObject.getString("workflowdefid");
            this.workflowdefversionid = jsonObject.getString("workflowdefversionid");
            this.workflowdefvariantid = jsonObject.getString("workflowdefvariantid");
            this.taskdefitemid = jsonObject.getString("taskdefitemid");
            this.taskexecgroup = jsonObject.getString("taskexecgroup");
            this.stepid = jsonObject.getString("stepid");
            this.hostFrameId = jsonObject.getString("hostframeid");
            this.taskPage = jsonObject.getString("taskpage").equals("Y");
            this.testMode = jsonObject.getString("testmode").equals("Y");
            this.standaloneMode = jsonObject.getString("standalonemode").equals("Y");
            JSONObject stepprops = jsonObject.getJSONObject("stepprops");
            if (stepprops != null) {
                this.stepProps = new PropertyList(stepprops);
            }
        }
        catch (JSONException e) {
            Trace.logError("Failed setJSONObject in TaskContext. Reason: " + e.getMessage(), e);
        }
    }

    public String toJSONString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("taskdefid", this.taskdefid != null ? this.taskdefid : "");
            jsonObject.put("taskdefversionid", this.taskdefversionid != null ? this.taskdefversionid : "");
            jsonObject.put("taskdefvariantid", this.taskdefvariantid != null ? this.taskdefvariantid : "");
            jsonObject.put("taskexecid", this.taskexecid != null ? this.taskexecid : "");
            jsonObject.put("workflowexecid", this.workflowexecid != null ? this.workflowexecid : "");
            jsonObject.put("workflowexecname", this.workflowexecname != null ? this.workflowexecname : "");
            jsonObject.put("workflowdefid", this.workflowdefid != null ? this.workflowdefid : "");
            jsonObject.put("workflowdefversionid", this.workflowdefversionid != null ? this.workflowdefversionid : "");
            jsonObject.put("workflowdefvariantid", this.workflowdefvariantid != null ? this.workflowdefvariantid : "");
            jsonObject.put("taskdefitemid", this.taskdefitemid != null ? this.taskdefitemid : "");
            jsonObject.put("taskexecgroup", this.taskexecgroup != null ? this.taskexecgroup : "");
            jsonObject.put("stepid", this.stepid != null ? this.stepid : "");
            jsonObject.put("hostframeid", this.hostFrameId != null ? this.hostFrameId : "");
            jsonObject.put("taskpage", this.taskPage ? "Y" : "N");
            jsonObject.put("testmode", this.testMode ? "Y" : "N");
            jsonObject.put("standalonemode", this.standaloneMode ? "Y" : "N");
            jsonObject.put("stepprops", this.stepProps != null ? this.stepProps : new JSONObject());
        }
        catch (Exception exception) {
            // empty catch block
        }
        return jsonObject.toString();
    }
}

