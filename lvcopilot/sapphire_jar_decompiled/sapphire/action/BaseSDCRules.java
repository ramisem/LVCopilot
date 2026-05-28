/*
 * Decompiled with CFR 0.152.
 */
package sapphire.action;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.attachment.Attachment;
import sapphire.error.ErrorDetail;
import sapphire.error.ErrorHandler;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIView;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.cmt.Snapshot;

public class BaseSDCRules
extends BaseCustom {
    private String sdcid;
    private PropertyList sdc;
    private DBUtil dbu;
    private String event;
    protected DBAccess database;
    protected ConnectionInfo connectionInfo;
    private ErrorHandler errorHandler;
    private BaseSDCRules[] customRules;
    private SDIData beforeEditImage;
    private boolean isCMTImport = false;
    public static final String TYPE_CONFIRM = "CONFIRM";
    public static final String TYPE_VALIDATION = "VALIDATION";
    public static final String TYPE_INFORMATION = "INFORMATION";

    public BaseSDCRules() {
    }

    public BaseSDCRules(ConnectionInfo connectionInfo) {
        this.setConnectionInfo(connectionInfo);
    }

    public static BaseSDCRules getInstance(SapphireConnection sapphireConnection, ErrorHandler errorHandler, String sdcid, PropertyList sdcProps, String event) throws SapphireException {
        BaseSDCRules systemRules = null;
        try {
            systemRules = (BaseSDCRules)Class.forName("com.labvantage.sapphire.admin.ddt." + sdcid).newInstance();
            systemRules.setSdcid(sdcid);
            systemRules.setConnectionInfo(sapphireConnection);
            systemRules.setSDCProps(sdcProps);
            systemRules.setEvent(event);
        }
        catch (InstantiationException instantiationException) {
        }
        catch (IllegalAccessException illegalAccessException) {
        }
        catch (ClassNotFoundException e) {
            systemRules = new BaseSDCRules(sapphireConnection);
            systemRules.setSdcid(sdcid);
            systemRules.setSDCProps(sdcProps);
            systemRules.setEvent(event);
        }
        systemRules.dbu = new DBUtil();
        systemRules.dbu.setConnection(sapphireConnection);
        systemRules.database = systemRules.dbu;
        systemRules.errorHandler = errorHandler;
        systemRules.logger.setLoggerName(sdcid);
        try {
            ConfigService configService = new ConfigService(sapphireConnection);
            String[] packagePropertySequence = (String[])CacheUtil.get(sapphireConnection.getDatabaseId(), "customrulespackagesequence", "customrulespackagesequence");
            if (packagePropertySequence == null) {
                String sdcRuleSequence = configService.getProfileProperty("(system)", "customrulessequence_" + sdcid);
                String customrulessequence = sdcRuleSequence.length() > 0 ? sdcRuleSequence : configService.getProfileProperty("(system)", "customrulessequence");
                String[] ruleSequence = StringUtil.split(customrulessequence, ";");
                String[] properties = configService.loadProfilePropertiesStartingWith("(system)", "customrulesjavapackage");
                if (properties != null) {
                    packagePropertySequence = new String[properties.length];
                    if (customrulessequence.length() > 0) {
                        int i;
                        int seq = -1;
                        block10: for (i = 0; i < ruleSequence.length; ++i) {
                            if (ruleSequence[i].length() <= 0) continue;
                            for (int j = 0; j < properties.length; ++j) {
                                if (!properties[j].equals(ruleSequence[i])) continue;
                                packagePropertySequence[++seq] = properties[j];
                                properties[j] = "";
                                continue block10;
                            }
                        }
                        if (seq + 1 < properties.length) {
                            for (i = 0; i < properties.length; ++i) {
                                if (properties[i].length() <= 0) continue;
                                packagePropertySequence[++seq] = properties[i];
                            }
                        }
                    } else {
                        for (int i2 = 0; i2 < properties.length; ++i2) {
                            packagePropertySequence[i2] = properties[i2];
                        }
                    }
                } else {
                    packagePropertySequence = new String[]{};
                }
                CacheUtil.put(sapphireConnection.getDatabaseId(), "customrulespackagesequence", "customrulespackagesequence", packagePropertySequence);
            }
            ArrayList<BaseSDCRules> customRulesList = new ArrayList<BaseSDCRules>(packagePropertySequence.length > 0 ? packagePropertySequence.length : 1);
            if (packagePropertySequence.length > 0) {
                for (int i = 0; i < packagePropertySequence.length; ++i) {
                    String customPackage = configService.getProfileProperty("(system)", packagePropertySequence[i]);
                    if (customPackage != null && customPackage.length() > 0) {
                        BaseSDCRules customRules = null;
                        try {
                            customRules = (BaseSDCRules)Class.forName(customPackage + "." + sdcid).newInstance();
                            customRules.setSdcid(sdcid);
                            customRules.setConnectionInfo(sapphireConnection);
                            customRules.setSDCProps(sdcProps);
                            customRules.setEvent(event);
                            customRules.database = systemRules.dbu;
                            customRules.errorHandler = systemRules.errorHandler;
                            customRules.logger = systemRules.logger;
                            customRulesList.add(customRules);
                        }
                        catch (InstantiationException i2) {
                        }
                        catch (IllegalAccessException i2) {
                        }
                        catch (ClassNotFoundException e) {
                            customRulesList.add(new BaseSDCRules(sapphireConnection));
                        }
                        continue;
                    }
                    customRulesList.add(new BaseSDCRules(sapphireConnection));
                }
            } else {
                customRulesList.add(new BaseSDCRules(sapphireConnection));
            }
            systemRules.setCustomRules(customRulesList.toArray(new BaseSDCRules[0]));
        }
        catch (ServiceException e) {
            throw new SapphireException("Failed to get custom rules package property", e);
        }
        return systemRules;
    }

    public void endRule() throws SapphireException {
        this.dbu.reset();
        if (this.errorHandler.size() > 0 && this.hasErrors()) {
            throw new SapphireException(this.errorHandler.getEncodedString());
        }
    }

    public boolean hasErrors() {
        if (this.errorHandler.size() > 0) {
            for (int i = 0; i < this.errorHandler.size(); ++i) {
                ErrorDetail ruleError = (ErrorDetail)this.errorHandler.get(i);
                if (!ruleError.getErrorType().equals(TYPE_VALIDATION) && !ruleError.getErrorType().equals(TYPE_CONFIRM)) continue;
                return true;
            }
        }
        return false;
    }

    public void setCMTImport(boolean CMTImport) {
        this.isCMTImport = CMTImport;
    }

    public boolean isCMTImport() {
        return this.isCMTImport;
    }

    public boolean requiresBeforeEditImage() {
        return false;
    }

    public boolean requiresBeforeEditDetailImage() {
        return false;
    }

    public boolean requiresBeforeEditSDIDataImage() {
        return false;
    }

    public boolean requiresBeforeEditWorkItemImage() {
        return false;
    }

    public boolean requiresBeforeEditSDIAttributeImage() {
        return false;
    }

    public boolean requiresBeforeDataEntryImage() {
        return false;
    }

    public boolean requiresBeforeDataReleaseImage() {
        return false;
    }

    public boolean requiresEditDetailPrimary() {
        return false;
    }

    public boolean requiresDataEntryPrimary() {
        return false;
    }

    public boolean requiresEditSDIDataPrimary() {
        return false;
    }

    public boolean requiresAddDataSetPrimary() {
        return false;
    }

    public boolean requiresAddWorkItemPrimary() {
        return false;
    }

    public boolean requiresEditWorkItemPrimary() {
        return false;
    }

    public boolean requiresDataReleasePrimary() {
        return false;
    }

    public boolean customRulesRequiresBeforeEditImage() {
        for (BaseSDCRules customRules : this.getCustomRuleList()) {
            if (!customRules.requiresBeforeEditImage()) continue;
            return true;
        }
        return false;
    }

    public boolean customRulesRequiresEditDetailPrimary() {
        for (BaseSDCRules customRules : this.getCustomRuleList()) {
            if (!customRules.requiresEditDetailPrimary()) continue;
            return true;
        }
        return false;
    }

    public boolean customRulesRequiresBeforeEditDetailImage() {
        for (BaseSDCRules customRules : this.getCustomRuleList()) {
            if (!customRules.requiresBeforeEditDetailImage()) continue;
            return true;
        }
        return false;
    }

    public boolean customRulesRequiresBeforeEditSDIAttributeImage() {
        for (BaseSDCRules customRules : this.getCustomRuleList()) {
            if (!customRules.requiresBeforeEditSDIAttributeImage()) continue;
            return true;
        }
        return false;
    }

    public boolean customRulesRequiresAddDataSetPrimary() {
        for (BaseSDCRules customRules : this.getCustomRuleList()) {
            if (!customRules.requiresAddDataSetPrimary()) continue;
            return true;
        }
        return false;
    }

    public boolean customRulesRequiresDataReleasePrimary() {
        for (BaseSDCRules customRules : this.getCustomRuleList()) {
            if (!customRules.requiresDataReleasePrimary()) continue;
            return true;
        }
        return false;
    }

    public boolean customRulesRequiresBeforeDataReleaseImage() {
        for (BaseSDCRules customRules : this.getCustomRuleList()) {
            if (!customRules.requiresBeforeDataReleaseImage()) continue;
            return true;
        }
        return false;
    }

    public boolean customRulesRequiresEditSDIDataPrimary() {
        for (BaseSDCRules customRules : this.getCustomRuleList()) {
            if (!customRules.requiresEditSDIDataPrimary()) continue;
            return true;
        }
        return false;
    }

    public boolean customRulesRequiresBeforeEditSDIDataImage() {
        for (BaseSDCRules customRules : this.getCustomRuleList()) {
            if (!customRules.requiresBeforeEditSDIDataImage()) continue;
            return true;
        }
        return false;
    }

    public boolean customRulesRequiresDataEntryPrimary() {
        for (BaseSDCRules customRules : this.getCustomRuleList()) {
            if (!customRules.requiresDataEntryPrimary()) continue;
            return true;
        }
        return false;
    }

    public boolean customRulesRequiresBeforeDataEntryImage() {
        for (BaseSDCRules customRules : this.getCustomRuleList()) {
            if (!customRules.requiresBeforeDataEntryImage()) continue;
            return true;
        }
        return false;
    }

    public boolean customRulesRequiresAddWorkItemPrimary() {
        for (BaseSDCRules customRules : this.getCustomRuleList()) {
            if (!customRules.requiresAddWorkItemPrimary()) continue;
            return true;
        }
        return false;
    }

    public boolean customRulesRequiresEditWorkItemPrimary() {
        for (BaseSDCRules customRules : this.getCustomRuleList()) {
            if (!customRules.requiresEditWorkItemPrimary()) continue;
            return true;
        }
        return false;
    }

    public boolean customRulesRequiresBeforeEditWorkItemImage() {
        for (BaseSDCRules customRules : this.getCustomRuleList()) {
            if (!customRules.requiresBeforeEditWorkItemImage()) continue;
            return true;
        }
        return false;
    }

    public void setBeforeEditImage(SDIData beforeEditImage) {
        this.beforeEditImage = beforeEditImage;
        for (BaseSDCRules customRules : this.getCustomRuleList()) {
            customRules.beforeEditImage = beforeEditImage;
        }
    }

    public SDIData getBeforeEditImage() {
        return this.beforeEditImage;
    }

    public boolean hasPrimaryValueChanged(DataSet newPrimary, int primaryRow, String columnId) {
        boolean hasChanged = false;
        if (newPrimary.isValidColumn(columnId)) {
            String newValue = newPrimary.getValue(primaryRow, columnId);
            String oldValue = this.getOldPrimaryValue(newPrimary, primaryRow, columnId);
            if (oldValue == null && newValue != null || oldValue != null && newValue == null || oldValue != null && newValue != null && !oldValue.equals(newValue)) {
                hasChanged = true;
            }
        }
        return hasChanged;
    }

    public String getOldPrimaryValue(DataSet newPrimary, int primaryRow, String columnId) {
        int oldRow;
        DataSet oldPrimary;
        String oldValue = "";
        String keyid1 = newPrimary.getValue(primaryRow, this.sdc.getProperty("keycolid1"));
        String keyid2 = newPrimary.getValue(primaryRow, this.sdc.getProperty("keycolid2"));
        String keyid3 = newPrimary.getValue(primaryRow, this.sdc.getProperty("keycolid3"));
        HashMap<String, String> findMap = new HashMap<String, String>();
        findMap.put(this.sdc.getProperty("keycolid1"), keyid1);
        if (keyid2 != null && keyid2.length() > 0) {
            findMap.put(this.sdc.getProperty("keycolid2"), keyid2);
        }
        if (keyid3 != null && keyid3.length() > 0) {
            findMap.put(this.sdc.getProperty("keycolid3"), keyid3);
        }
        if (this.beforeEditImage != null && (oldPrimary = this.beforeEditImage.getDataset("primary")) != null && (oldRow = oldPrimary.findRow(findMap)) >= 0 && oldRow < oldPrimary.size()) {
            oldValue = oldPrimary.getValue(oldRow, columnId);
        }
        return oldValue;
    }

    public Calendar getOldPrimaryCalendar(DataSet newPrimary, int primaryRow, String columnId) {
        int oldRow;
        DataSet oldPrimary;
        Calendar oldValue = null;
        String keyid1 = newPrimary.getValue(primaryRow, this.sdc.getProperty("keycolid1"));
        String keyid2 = newPrimary.getValue(primaryRow, this.sdc.getProperty("keycolid2"));
        String keyid3 = newPrimary.getValue(primaryRow, this.sdc.getProperty("keycolid3"));
        HashMap<String, String> findMap = new HashMap<String, String>();
        findMap.put(this.sdc.getProperty("keycolid1"), keyid1);
        if (keyid2 != null && keyid2.length() > 0) {
            findMap.put(this.sdc.getProperty("keycolid2"), keyid2);
        }
        if (keyid3 != null && keyid3.length() > 0) {
            findMap.put(this.sdc.getProperty("keycolid3"), keyid3);
        }
        if (this.beforeEditImage != null && (oldPrimary = this.beforeEditImage.getDataset("primary")) != null && (oldRow = oldPrimary.findRow(findMap)) >= 0 && oldRow < oldPrimary.size()) {
            oldValue = oldPrimary.getCalendar(oldRow, columnId);
        }
        return oldValue;
    }

    public boolean hasSDIWorkItemValueChanged(DataSet newSDIWI, int row, String columnId) {
        boolean hasChanged = false;
        if (newSDIWI.isValidColumn(columnId)) {
            String newValue = newSDIWI.getValue(row, columnId);
            String oldValue = this.getOldSDIWorkItemValue(newSDIWI, row, columnId);
            if (oldValue == null && newValue != null || oldValue != null && newValue == null || oldValue != null && newValue != null && !oldValue.equals(newValue)) {
                hasChanged = true;
            }
        }
        return hasChanged;
    }

    public String getOldSDIWorkItemValue(DataSet newSDIWI, int row, String columnId) {
        int oldRow;
        DataSet oldSDIWI;
        String oldValue = "";
        String keyid1 = newSDIWI.getValue(row, "keyid1");
        String keyid2 = newSDIWI.getValue(row, "keyid2");
        String keyid3 = newSDIWI.getValue(row, "keyid3");
        String workItemId = newSDIWI.getValue(row, "workitemid");
        BigDecimal workItemInstance = newSDIWI.getBigDecimal(row, "workiteminstance");
        HashMap<String, Object> findMap = new HashMap<String, Object>();
        findMap.put("keyid1", keyid1);
        if (this.sdc.getProperty("keycolid2").length() > 0) {
            findMap.put("keyid2", keyid2);
        }
        if (this.sdc.getProperty("keycolid3").length() > 0) {
            findMap.put("keyid3", keyid3);
        }
        findMap.put("workitemid", workItemId);
        findMap.put("workiteminstance", workItemInstance);
        if (this.beforeEditImage != null && (oldSDIWI = this.beforeEditImage.getDataset("sdiworkitem")) != null && (oldRow = oldSDIWI.findRow(findMap)) >= 0 && oldRow < oldSDIWI.size()) {
            oldValue = oldSDIWI.getValue(oldRow, columnId);
        }
        return oldValue;
    }

    public boolean hasSDIDataValueChanged(DataSet newSDIData, int row, String columnId) {
        boolean hasChanged = false;
        if (newSDIData.isValidColumn(columnId)) {
            String newValue = newSDIData.getValue(row, columnId);
            String oldValue = this.getOldSDIDataValue(newSDIData, row, columnId);
            if (oldValue == null && newValue != null || oldValue != null && newValue == null || oldValue != null && newValue != null && !oldValue.equals(newValue)) {
                hasChanged = true;
            }
        }
        return hasChanged;
    }

    public String getOldSDIDataValue(DataSet newSDIData, int row, String columnId) {
        int oldRow;
        DataSet oldSDIWI;
        String oldValue = "";
        String keyid1 = newSDIData.getValue(row, "keyid1");
        String keyid2 = newSDIData.getValue(row, "keyid2");
        String keyid3 = newSDIData.getValue(row, "keyid3");
        String paramListId = newSDIData.getValue(row, "paramlistid");
        String paramListVersionId = newSDIData.getValue(row, "paramlistversionid");
        String variantId = newSDIData.getValue(row, "variantid");
        BigDecimal datasetNum = newSDIData.getBigDecimal(row, "dataset");
        HashMap<String, Object> findMap = new HashMap<String, Object>();
        findMap.put("keyid1", keyid1);
        if (this.sdc.getProperty("keycolid2").length() > 0) {
            findMap.put("keyid2", keyid2);
        }
        if (this.sdc.getProperty("keycolid3").length() > 0) {
            findMap.put("keyid3", keyid3);
        }
        findMap.put("paramlistid", paramListId);
        findMap.put("paramlistversionid", paramListVersionId);
        findMap.put("variantid", variantId);
        findMap.put("dataset", datasetNum);
        if (this.beforeEditImage != null && (oldSDIWI = this.beforeEditImage.getDataset("dataset")) != null && (oldRow = oldSDIWI.findRow(findMap)) >= 0 && oldRow < oldSDIWI.size()) {
            oldValue = oldSDIWI.getValue(oldRow, columnId);
        }
        return oldValue;
    }

    public boolean hasSDIAttributeValueChanged(DataSet newAttribute, int row, String columnId) {
        boolean hasChanged = false;
        if (newAttribute.isValidColumn(columnId)) {
            String newValue = newAttribute.getValue(row, columnId);
            String oldValue = this.getOldSDIAttributeValue(newAttribute, row, columnId);
            if (oldValue == null && newValue != null || oldValue != null && newValue == null || oldValue != null && newValue != null && !oldValue.equals(newValue)) {
                hasChanged = true;
            }
        }
        return hasChanged;
    }

    public String getOldSDIAttributeValue(DataSet newAttribute, int row, String columnId) {
        int oldRow;
        DataSet oldAttributes;
        String oldValue = "";
        String keyid1 = newAttribute.getValue(row, "keyid1");
        String keyid2 = newAttribute.getValue(row, "keyid2");
        String keyid3 = newAttribute.getValue(row, "keyid3");
        String attributeid = newAttribute.getValue(row, "attributeid");
        String attributesdcid = newAttribute.getValue(row, "attributesdcid");
        BigDecimal attributeinstance = newAttribute.getBigDecimal(row, "attributeinstance");
        HashMap<String, Object> findMap = new HashMap<String, Object>();
        findMap.put("keyid1", keyid1);
        if (this.sdc.getProperty("keycolid2").length() > 0) {
            findMap.put("keyid2", keyid2);
        }
        if (this.sdc.getProperty("keycolid3").length() > 0) {
            findMap.put("keyid3", keyid3);
        }
        findMap.put("attributeid", attributeid);
        findMap.put("attributesdcid", attributesdcid);
        findMap.put("attributeinstance", attributeinstance);
        if (this.beforeEditImage != null && (oldAttributes = this.beforeEditImage.getDataset("attribute")) != null && (oldRow = oldAttributes.findRow(findMap)) >= 0 && oldRow < oldAttributes.size()) {
            oldValue = oldAttributes.getValue(oldRow, columnId);
        }
        return oldValue;
    }

    public String getEvent() {
        return this.event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getSdcid() {
        return this.sdcid;
    }

    public ConnectionInfo getConnectionInfo() {
        return this.connectionInfo;
    }

    public void setConnectionInfo(ConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
        this.setConnectionId(connectionInfo.getConnectionId());
    }

    public void setSdcid(String sdcid) {
        this.sdcid = sdcid;
    }

    public PropertyList getSdcProps() {
        return this.sdc;
    }

    public void setSDCProps(PropertyList sdc) {
        this.sdc = sdc;
    }

    public String getDatabaseid() {
        return this.connectionInfo.getDatabaseId();
    }

    public BaseSDCRules[] getCustomRuleList() {
        return this.customRules;
    }

    public void setCustomRules(BaseSDCRules[] customRules) {
        this.customRules = customRules;
    }

    public void setWarning(String ruleid, String message) {
        this.errorHandler.add(this.sdcid, this.event, ruleid, TYPE_INFORMATION, message);
    }

    public void setError(String ruleid, String errorType, String message) {
        this.errorHandler.add(this.sdcid, this.event, ruleid, errorType, message);
    }

    public void setErrors(ErrorHandler errorHandler) {
        this.errorHandler.addAll(errorHandler);
    }

    public void throwError(String ruleid, String errorType, String message) throws SapphireException {
        this.errorHandler.add(this.sdcid, this.event, ruleid, errorType, message);
        throw new SapphireException(this.errorHandler.getEncodedString());
    }

    protected void logTrace(String message) {
        this.logger.info(this.event + " (" + this.sdcid + "): " + message);
    }

    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    public void preCMTImport(SDIData sdiData, PropertyList actionProps, boolean isAddSDI) throws SapphireException {
    }

    public void postCMTImport(SDIData sdiData, PropertyList actionProps, boolean isAddSDI) throws SapphireException {
    }

    public void postAddKey(DataSet primary, PropertyList actionProps) {
    }

    public void preAddKey(DataSet primary, PropertyList actionProps) throws SapphireException {
    }

    public void postGenerateSnapshot(Snapshot snapshot, boolean isPackaging) throws SapphireException {
    }

    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
    }

    public void postDelete(String rsetid, PropertyList actionProps) throws SapphireException {
    }

    public void preApprove(DataSet approve) throws SapphireException {
    }

    public void postApprove(DataSet approve) throws SapphireException {
    }

    public void preAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    public void postAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    public void preEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    public void postEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    public void preDeleteDetail(String rsetid, PropertyList actionProps) throws SapphireException {
    }

    public void postDeleteDetail(String rsetid, PropertyList actionProps) throws SapphireException {
    }

    public void postDataEntry(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    public void preEditSDIDataItem(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    public void preEditSDIData(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    public void preEditSDIDataApproval(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    public void postEditSDIDataItem(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    public void postEditSDIData(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    public void postEditSDIDataApproval(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    public void preReleaseData(DataSet releaseData, PropertyList actionProps) throws SapphireException {
    }

    public void postReleaseData(DataSet releaseData, PropertyList actionProps) throws SapphireException {
    }

    public void preAddDataSet(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    public void postAddDataSet(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    public void preAddWorkItem(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    public void postAddWorkitem(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    public void postAddWorkItem(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.postAddWorkitem(sdiData, actionProps);
    }

    public void preEditWorkitem(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    public void preEditWorkItem(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.preEditWorkitem(sdiData, actionProps);
    }

    public void postEditWorkitem(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    public void postEditWorkItem(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.postEditWorkitem(sdiData, actionProps);
    }

    public void preAddAttribute(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    public void postAddAttribute(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    public void preEditAttribute(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    public void postEditAttribute(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    public void preDeleteAttribute(String rsetid, PropertyList actionProps) throws SapphireException {
    }

    public void postDeleteAttribute(String rsetid, PropertyList actionProps) throws SapphireException {
    }

    public void preAddNote(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    public void preEditNote(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    public void postAddNote(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    public void postEditNote(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    public SDIView getSDIView(SDIData sdiData) {
        return new SDIView(sdiData, 0, null, this.getConnectionId());
    }

    public void preGetSDIAttachment(Attachment attachment) throws SapphireException {
    }

    public void preAddSDIAttachment(Attachment attachment) throws SapphireException {
    }

    public void postAddSDIAttachment(Attachment attachment) throws SapphireException {
    }

    public void preEditSDIAttachment(Attachment attachment, Attachment preEditAttachment) throws SapphireException {
    }

    public void postEditSDIAttachment(Attachment attachment) throws SapphireException {
    }

    public void preDeleteSDIAttachment(Attachment attachment) throws SapphireException {
    }

    public void postDeleteSDIAttachment(Attachment attachment) throws SapphireException {
    }
}

