/*
 * Decompiled with CFR 0.152.
 */
package sapphire.util;

import com.labvantage.sapphire.gwt.shared.JSONable;
import java.util.Arrays;
import java.util.List;
import org.json.JSONObject;
import sapphire.util.ResultDataGrid;
import sapphire.xml.PropertyList;

public class ResultGridOptions
implements JSONable {
    private AutoAddSDI autoAddSDI = AutoAddSDI.NEVER;
    private PropertyList addSDIProperties = null;
    private AutoAddDataSet autoAddDataset = AutoAddDataSet.NEVER;
    private PropertyList addDatasetProperties = null;
    private AutoAddWorkItem autoAddWorkItem = AutoAddWorkItem.NEVER;
    private AutoAddParameter autoAddParameter = AutoAddParameter.NEVER;
    private AutoAddReplicate autoAddReplicate = AutoAddReplicate.NEVER;
    private PropertyList addDataItemProperties = null;
    private PropertyList enterDataItemProperties = null;
    private boolean autoRelease = false;
    private DefaultReplicateId defaultReplicateId = DefaultReplicateId.FIRST_AVAILABLE;
    private DefaultDataSet defaultDataSet = DefaultDataSet.FIRST_AVAILABLE;
    private ReleaseHandlingRule releaseHandlingRule = ReleaseHandlingRule.ERROR;
    private MissingDataErrorHandling missingDataErrorHandling = MissingDataErrorHandling.ERROR;
    private String sdcId = "";
    private String auditReason = "";
    private String auditActivity = "";
    private String auditSignedFlag = "";
    private String auditDt = "";
    private String traceLogId = "";
    private boolean applyLock = true;
    private String[] corecolumns = new String[]{ResultDataGrid.CoreColumns.ID.getColumnId(), ResultDataGrid.CoreColumns.SDCID.getColumnId(), ResultDataGrid.CoreColumns.KEYID1.getColumnId(), ResultDataGrid.CoreColumns.KEYID2.getColumnId(), ResultDataGrid.CoreColumns.KEYID3.getColumnId(), ResultDataGrid.CoreColumns.WORKITEMID.getColumnId(), ResultDataGrid.CoreColumns.PARAMLISTID.getColumnId(), ResultDataGrid.CoreColumns.PARAMLISTID.getColumnId(), ResultDataGrid.CoreColumns.PARAMLISTVERSIONID.getColumnId(), ResultDataGrid.CoreColumns.VARIANTID.getColumnId(), ResultDataGrid.CoreColumns.DATASET.getColumnId(), ResultDataGrid.CoreColumns.PARAMID.getColumnId(), ResultDataGrid.CoreColumns.PARAMTYPE.getColumnId(), ResultDataGrid.CoreColumns.REPLICATEID.getColumnId(), ResultDataGrid.CoreColumns.VALUE.getColumnId(), ResultDataGrid.CoreColumns.ENTEREDTEXT.getColumnId()};
    private List coreColsList = Arrays.asList(this.corecolumns);

    public ResultGridOptions() {
    }

    public ResultGridOptions(JSONObject jsonObject) {
        this.setFromPropertyList(new PropertyList(jsonObject));
    }

    public ResultGridOptions(PropertyList propertyList) {
        this.setFromPropertyList(propertyList);
    }

    private void setFromPropertyList(PropertyList p) {
        for (Object pid : p.keySet()) {
            String propertyId = ((String)pid).toLowerCase();
            try {
                switch (propertyId) {
                    case "autoaddsdi": {
                        this.autoAddSDI = AutoAddSDI.valueOf(p.getProperty(pid.toString()).toUpperCase());
                        break;
                    }
                    case "addsdiproperties": {
                        this.addSDIProperties = p.getPropertyList(pid.toString());
                        break;
                    }
                    case "autoadddataset": {
                        this.autoAddDataset = AutoAddDataSet.valueOf(p.getProperty(pid.toString()).toUpperCase());
                        break;
                    }
                    case "adddatasetproperties": {
                        this.addDatasetProperties = p.getPropertyList(pid.toString());
                        break;
                    }
                    case "autoaddworkitem": {
                        this.autoAddWorkItem = AutoAddWorkItem.valueOf(p.getProperty(pid.toString()).toUpperCase());
                        break;
                    }
                    case "autoaddparameter": {
                        this.autoAddParameter = AutoAddParameter.valueOf(p.getProperty(pid.toString()).toUpperCase());
                        break;
                    }
                    case "autoaddreplicate": {
                        this.autoAddReplicate = AutoAddReplicate.valueOf(p.getProperty(pid.toString()).toUpperCase());
                        break;
                    }
                    case "defaultreplicateid": {
                        this.defaultReplicateId = DefaultReplicateId.valueOfDefaultReplicateId(p.getProperty(pid.toString()).toUpperCase());
                        break;
                    }
                    case "defaultdataset": {
                        this.defaultDataSet = DefaultDataSet.valueOfDefaultDataSet(p.getProperty(pid.toString()).toUpperCase());
                        break;
                    }
                    case "adddataitemproperties": {
                        this.addDataItemProperties = p.getPropertyList(pid.toString());
                        break;
                    }
                    case "enterdataitemproperties": {
                        this.enterDataItemProperties = p.getPropertyList(pid.toString());
                        break;
                    }
                    case "autorelease": {
                        this.autoRelease = p.getProperty(pid.toString()).equalsIgnoreCase("Y");
                        break;
                    }
                    case "releasehandlingrule": {
                        this.releaseHandlingRule = ReleaseHandlingRule.valueOf(p.getProperty(pid.toString()).toUpperCase());
                        break;
                    }
                    case "missingdataerrorhandling": {
                        this.missingDataErrorHandling = MissingDataErrorHandling.valueOf(p.getProperty(pid.toString()).toUpperCase());
                        break;
                    }
                    case "sdcid": {
                        this.sdcId = p.getProperty(pid.toString());
                    }
                }
            }
            catch (Exception exception) {}
        }
    }

    public PropertyList toPropertyList() {
        PropertyList p = new PropertyList();
        p.setProperty("autoaddsdi", this.autoAddSDI.toString().toLowerCase());
        if (this.addSDIProperties != null) {
            p.setProperty("addsdiproperties", this.addSDIProperties);
        }
        p.setProperty("autoadddataset", this.autoAddDataset.toString().toLowerCase());
        if (this.addDatasetProperties != null) {
            p.setProperty("adddatasetproperties", this.addDatasetProperties);
        }
        p.setProperty("autoaddworkitem", this.autoAddWorkItem.toString().toLowerCase());
        p.setProperty("autoaddparameter", this.autoAddParameter.toString().toLowerCase());
        p.setProperty("autoaddreplicate", this.autoAddReplicate.toString().toLowerCase());
        p.setProperty("defaultreplicateid", this.defaultReplicateId.toString().toLowerCase());
        p.setProperty("defaultdataset", this.defaultDataSet.toString().toLowerCase());
        if (this.addDataItemProperties != null) {
            p.setProperty("adddataitemproperties", this.addDataItemProperties);
        }
        if (this.enterDataItemProperties != null) {
            p.setProperty("enterdataitemproperties", this.enterDataItemProperties);
        }
        p.setProperty("autorelease", this.autoRelease ? "Y" : "N");
        p.setProperty("releasehandlingrule", this.releaseHandlingRule.toString().toLowerCase());
        p.setProperty("missingdataerrorhandling", this.missingDataErrorHandling.toString().toLowerCase());
        p.setProperty("sdcid", this.sdcId);
        return p;
    }

    @Override
    public String toJSONString() {
        return this.toPropertyList().toJSONString();
    }

    public List getCoreColsList() {
        return this.coreColsList;
    }

    public void setAutoAddSDI(AutoAddSDI autoAddSDI) {
        this.autoAddSDI = autoAddSDI;
    }

    public AutoAddSDI getAutoAddSDI() {
        return this.autoAddSDI;
    }

    public void setAddSDIProperties(PropertyList properties) {
        this.addSDIProperties = properties;
    }

    public PropertyList getAddSDIProperties() {
        return this.addSDIProperties;
    }

    public void setAutoAddDataset(AutoAddDataSet autoAddDataset) {
        this.autoAddDataset = autoAddDataset;
    }

    public AutoAddDataSet getAutoAddDataset() {
        return this.autoAddDataset;
    }

    public void setAddDatasetProperties(PropertyList properties) {
        this.addDatasetProperties = properties;
    }

    public PropertyList getAddDatasetProperties() {
        return this.addDatasetProperties;
    }

    public void setAutoAddWorkItem(AutoAddWorkItem autoAddWorkItem) {
        this.autoAddWorkItem = autoAddWorkItem;
    }

    public AutoAddWorkItem getAutoAddWorkItem() {
        return this.autoAddWorkItem;
    }

    public void setAutoAddParameter(AutoAddParameter autoAddParameter) {
        this.autoAddParameter = autoAddParameter;
    }

    public AutoAddParameter getAutoAddParameter() {
        return this.autoAddParameter;
    }

    public void setAutoAddReplicate(AutoAddReplicate autoAddReplicate) {
        this.autoAddReplicate = autoAddReplicate;
    }

    public AutoAddReplicate getAutoAddReplicate() {
        return this.autoAddReplicate;
    }

    public void setDefaultReplicateId(DefaultReplicateId replicateId) {
        this.defaultReplicateId = replicateId;
    }

    public DefaultReplicateId getDefaultReplicateId() {
        return this.defaultReplicateId;
    }

    public void setDefaultDataSet(DefaultDataSet dataset) {
        this.defaultDataSet = dataset;
    }

    public DefaultDataSet getDefaultDataSet() {
        return this.defaultDataSet;
    }

    public void setAddDataItemProperties(PropertyList properties) {
        this.addDataItemProperties = properties;
    }

    public PropertyList getAddDataItemProperties() {
        return this.addDataItemProperties;
    }

    public void setEnterDataItemProperties(PropertyList properties) {
        this.enterDataItemProperties = properties;
    }

    public PropertyList getEnterDataItemProperties() {
        return this.enterDataItemProperties;
    }

    public void setAutoRelease(boolean autoRelease) {
        this.autoRelease = autoRelease;
    }

    public boolean getAutoRelease() {
        return this.autoRelease;
    }

    public void setReleaseHandlingRule(ReleaseHandlingRule releaseHandlingRule) {
        this.releaseHandlingRule = releaseHandlingRule;
    }

    public ReleaseHandlingRule getReleaseHandlingRule() {
        return this.releaseHandlingRule;
    }

    public void setMissingDataErrorHandling(MissingDataErrorHandling missingDataErrorHandling) {
        this.missingDataErrorHandling = missingDataErrorHandling;
    }

    public MissingDataErrorHandling getMissingDataErrorHandling() {
        return this.missingDataErrorHandling;
    }

    public String getAuditReason() {
        return this.auditReason;
    }

    public void setAuditReason(String reason) {
        this.auditReason = reason;
    }

    public String getAuditActivity() {
        return this.auditActivity;
    }

    public void setAuditActivity(String activity) {
        this.auditActivity = activity;
    }

    public String getAuditSignedFlag() {
        return this.auditSignedFlag;
    }

    public void setAuditSignedFlag(String signedFlag) {
        this.auditSignedFlag = signedFlag;
    }

    public String getAuditDt() {
        return this.auditDt;
    }

    public void setAuditDt(String auditdt) {
        this.auditDt = auditdt;
    }

    public boolean getApplyLock() {
        return this.applyLock;
    }

    public void setApplyLock(boolean lock) {
        this.applyLock = lock;
    }

    public String getTraceLogId() {
        return this.traceLogId;
    }

    public void setTraceLogId(String tracelogId) {
        this.traceLogId = tracelogId;
    }

    public String getSdcId() {
        return this.sdcId;
    }

    public void setSdcId(String sdcid) {
        this.sdcId = sdcid;
    }

    public static enum DefaultDataSet {
        DATASET_ONE("One"),
        MAX_PLUS_ONE("Max Plus One"),
        FIRST_AVAILABLE("First Available");

        private final String datasetDesc;

        private DefaultDataSet(String datasetDesc) {
            this.datasetDesc = datasetDesc;
        }

        public String toString() {
            return this.datasetDesc;
        }

        public static DefaultDataSet valueOfDefaultDataSet(String datasetDesc) {
            for (DefaultDataSet type : (DefaultDataSet[])DefaultDataSet.class.getEnumConstants()) {
                if (!type.toString().equalsIgnoreCase(datasetDesc)) continue;
                return type;
            }
            return DefaultDataSet.valueOf(datasetDesc);
        }
    }

    public static enum DefaultReplicateId {
        REPLICATE_ONE("One"),
        MAX_PLUS_ONE("Max Plus One"),
        FIRST_AVAILABLE("First Available");

        private final String replicateDesc;

        private DefaultReplicateId(String desc) {
            this.replicateDesc = desc;
        }

        public String toString() {
            return this.replicateDesc;
        }

        public static DefaultReplicateId valueOfDefaultReplicateId(String desc) {
            for (DefaultReplicateId type : (DefaultReplicateId[])DefaultReplicateId.class.getEnumConstants()) {
                if (!type.toString().equalsIgnoreCase(desc)) continue;
                return type;
            }
            return DefaultReplicateId.valueOf(desc);
        }
    }

    public static enum MissingDataErrorHandling {
        ERROR,
        IGNORE;

    }

    public static enum ReleaseHandlingRule {
        ERROR,
        IGNORE,
        OVERRIDE;

    }

    public static enum AutoAddReplicate {
        NEVER,
        ALWAYS;

    }

    public static enum AutoAddParameter {
        NEVER,
        ALWAYS;

    }

    public static enum AutoAddWorkItem {
        NEVER,
        ALWAYS;

    }

    public static enum AutoAddDataSet {
        NEVER,
        ALWAYS,
        REMEASURE;

    }

    public static enum AutoAddSDI {
        NEVER,
        ALWAYS;

    }
}

