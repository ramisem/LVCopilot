/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.SDCExpiryValidationUtil;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.actions.sdi.AddSDIVersion;
import com.labvantage.sapphire.actions.sdi.DeleteSDI;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.actions.workitem.WorkItemUtil;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.samplingplan.SamplingPlanUtil;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class WorkItem
extends BaseSDCRules {
    public static final String SDCID = "WorkItem";
    public static final String SDC_KEYCOLID1 = "workitemid";
    public static final String SDC_KEYCOLID2 = "workitemversionid";

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (this.isCMTImport()) {
            return;
        }
        DataSet primary = sdiData.getDataset("primary");
        if (primary != null) {
            PropertyList props = new PropertyList();
            for (int i = 0; i < primary.size(); ++i) {
                String childsampleplanid;
                String embedchildsampleplanid = primary.getString(i, "embedchildsampleplanid", "");
                String embedchildsampleplanversionid = primary.getString(i, "embedchildsampleplanversionid", "");
                if (embedchildsampleplanid.length() > 0 && embedchildsampleplanversionid.length() > 0) {
                    if ("Y".equals(actionProps.getProperty("newversion"))) {
                        props.clear();
                        props.setProperty("sdcid", "LV_ChildSamplePlan");
                        props.setProperty("keyid1", embedchildsampleplanid);
                        props.setProperty("keyid2", embedchildsampleplanversionid);
                        props.setProperty("auditreason", "Added new WorkItem version");
                        this.getActionProcessor().processActionClass(AddSDIVersion.class.getName(), props);
                        primary.setString(i, "embedchildsampleplanversionid", props.getProperty("newkeyid2"));
                    } else {
                        childsampleplanid = "ECP-" + this.getSequenceProcessor().getSequence("LV_ChildSamplePlan", "ECP");
                        props.clear();
                        props.setProperty("sdcid", "LV_ChildSamplePlan");
                        props.setProperty("keyid1", childsampleplanid);
                        props.setProperty("keyid2", "1");
                        props.setProperty("templatekeyid1", embedchildsampleplanid);
                        props.setProperty("templatekeyid2", embedchildsampleplanversionid);
                        props.setProperty("embeddedflag", "Y");
                        this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                        primary.setString(i, "embedchildsampleplanid", childsampleplanid);
                        primary.setString(i, "embedchildsampleplanversionid", "1");
                    }
                } else if ("Y".equals(primary.getValue(i, "supportembeddedchildplanflag", "N"))) {
                    childsampleplanid = "ECP-" + this.getSequenceProcessor().getSequence("LV_ChildSamplePlan", "ECP");
                    props.clear();
                    props.setProperty("sdcid", "LV_ChildSamplePlan");
                    props.setProperty("keyid1", childsampleplanid);
                    props.setProperty("keyid2", primary.getString(i, SDC_KEYCOLID2));
                    props.setProperty("sampletypeid", primary.getString(i, "applicablesampletypeid", ""));
                    props.setProperty("embeddedflag", "Y");
                    this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                    primary.setString(i, "embedchildsampleplanid", childsampleplanid);
                    primary.setString(i, "embedchildsampleplanversionid", "1");
                }
                this.checkLabWorkArea(primary, i);
            }
        }
    }

    private void checkExpiredPL(DataSet primary) throws SapphireException {
        String queryWorkItemWithExpiredPl = "SELECT DISTINCT wii.keyid1, wii.keyid2, wii.keyid3, w.workitemid, w.workitemversionid FROM workitem w, workitemitem wii, paramlist pl WHERE w.workitemid = wii.workitemid AND w.workitemversionid = wii.workitemversionid AND pl.PARAMLISTID = keyid1 AND pl.PARAMLISTVERSIONID = keyid2 AND pl.VARIANTID = keyid3 AND sdcid = 'ParamList' AND w.workitemid = ? and w.workitemversionid = ? AND pl.versionstatus = ? ORDER BY w.workitemid, w.workitemversionid";
        PreparedStatement workItemWithExpiredPlStatement = this.database.prepareStatement("workItemWithExpiredPl", queryWorkItemWithExpiredPl);
        if (primary != null) {
            for (int i = 0; i < primary.size(); ++i) {
                String workitemid = primary.getValue(i, SDC_KEYCOLID1);
                String workitemversionid = primary.getValue(i, SDC_KEYCOLID2);
                try {
                    workItemWithExpiredPlStatement.setString(1, workitemid);
                    workItemWithExpiredPlStatement.setString(2, workitemversionid);
                    workItemWithExpiredPlStatement.setString(3, "E");
                    DataSet workitemPL = new DataSet(workItemWithExpiredPlStatement.executeQuery());
                    if (workitemPL.size() <= 0) continue;
                    throw new SapphireException(this.getTranslationProcessor().translate("One or more paramlist is expired for the WorkItem"));
                }
                catch (SQLException e) {
                    throw new SapphireException(this.getTranslationProcessor().translate("Failed to retrieve ParameterList detail for TestMethod"));
                }
            }
        }
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if ("Y".equals(actionProps.getProperty("newversion"))) {
            this.checkExpiredPL(primary);
        }
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (primary != null) {
            String isVersionProtectEnabled;
            PropertyList versionprotection;
            String workitems = primary.getColumnValues(SDC_KEYCOLID1, ";");
            String workitemversions = primary.getColumnValues(SDC_KEYCOLID2, ";");
            if (workitems != null && workitems.length() > 0) {
                this.clearCache(StringUtil.split(workitems, ";"), workitemversions.length() > 0 ? StringUtil.split(workitemversions, ";") : new String[]{});
            }
            for (int i = 0; i < primary.size(); ++i) {
                if (this.hasPrimaryValueChanged(primary, i, "supportembeddedchildplanflag") && "Y".equals(primary.getValue(i, "supportembeddedchildplanflag", "N"))) {
                    String workitemid = primary.getValue(i, SDC_KEYCOLID1);
                    String workitemversionid = primary.getValue(i, SDC_KEYCOLID2);
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select embedchildsampleplanid, embedchildsampleplanversionid, applicablesampletypeid from workitem where workitemid = ? and workitemversionid = ?", (Object[])new String[]{workitemid, workitemversionid});
                    if (ds != null && ds.size() > 0 && OpalUtil.isEmpty(ds.getString(0, "embedchildsampleplanid"))) {
                        String childsampleplanid = "ECP-" + this.getSequenceProcessor().getSequence("LV_ChildSamplePlan", "ECP");
                        PropertyList props = new PropertyList();
                        props.setProperty("sdcid", "LV_ChildSamplePlan");
                        props.setProperty("keyid1", childsampleplanid);
                        props.setProperty("keyid2", "1");
                        props.setProperty("embeddedflag", "Y");
                        props.setProperty("sampletypeid", ds.getString(0, "applicablesampletypeid", ""));
                        this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                        primary.setString(i, "embedchildsampleplanid", childsampleplanid);
                        primary.setString(i, "embedchildsampleplanversionid", "1");
                    }
                }
                this.checkLabWorkArea(primary, i);
            }
            PropertyList paramListPolicy = this.getConfigurationProcessor().getPolicy("ParamListPolicy", "Sapphire Custom");
            if (paramListPolicy != null && (versionprotection = paramListPolicy.getPropertyListNotNull("expireddataprotection")) != null && versionprotection.size() > 0 && (isVersionProtectEnabled = versionprotection.getProperty(SDCID.toLowerCase(), "")).equalsIgnoreCase("Y")) {
                this.doExpireVersionValidation(primary);
            }
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (primary != null) {
            for (int i = 0; i < primary.size(); ++i) {
                String embedchildsampleplanversionid;
                String embedchildsampleplanid;
                String workitemid = primary.getString(i, SDC_KEYCOLID1);
                String workitemVersionId = primary.getString(i, SDC_KEYCOLID2);
                if (this.hasPrimaryValueChanged(primary, i, "applicablesampletypeid")) {
                    embedchildsampleplanid = this.getOldPrimaryValue(primary, i, "embedchildsampleplanid");
                    embedchildsampleplanversionid = this.getOldPrimaryValue(primary, i, "embedchildsampleplanversionid");
                    if (OpalUtil.isNotEmpty(embedchildsampleplanid) && OpalUtil.isNotEmpty(embedchildsampleplanversionid)) {
                        PropertyList props = new PropertyList();
                        props.setProperty("sdcid", "LV_ChildSamplePlan");
                        props.setProperty("keyid1", embedchildsampleplanid);
                        props.setProperty("keyid2", embedchildsampleplanversionid);
                        props.setProperty("sampletypeid", primary.getValue(0, "applicablesampletypeid", ""));
                        this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                    }
                }
                if (this.hasPrimaryValueChanged(primary, i, "versionstatus") && "C".equals(primary.getString(i, "versionstatus"))) {
                    embedchildsampleplanid = this.getOldPrimaryValue(primary, i, "embedchildsampleplanid");
                    embedchildsampleplanversionid = this.getOldPrimaryValue(primary, i, "embedchildsampleplanversionid");
                    if (OpalUtil.isNotEmpty(embedchildsampleplanid) && OpalUtil.isNotEmpty(embedchildsampleplanversionid)) {
                        PropertyList props;
                        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_childsampleplanid, s_childsampleplanversionid from s_childsampleplan where s_childsampleplanid = ? and versionstatus = 'C'", (Object[])new String[]{embedchildsampleplanid});
                        if (ds != null && ds.size() > 0) {
                            props = new PropertyList();
                            props.setProperty("sdcid", "LV_ChildSamplePlan");
                            props.setProperty("keyid1", embedchildsampleplanid + ";" + embedchildsampleplanid);
                            props.setProperty("keyid2", embedchildsampleplanversionid + ";" + ds.getValue(0, "s_childsampleplanversionid"));
                            props.setProperty("versionstatus", "C;A");
                            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                        } else {
                            props = new PropertyList();
                            props.setProperty("sdcid", "LV_ChildSamplePlan");
                            props.setProperty("keyid1", embedchildsampleplanid);
                            props.setProperty("keyid2", embedchildsampleplanversionid);
                            props.setProperty("versionstatus", "C");
                            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                        }
                    }
                }
                this.clearCache("Workitem", workitemid + ";" + workitemVersionId);
                this.clearCache("TestMethod", workitemid + ";" + workitemVersionId);
            }
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        String check = "SELECT sdiworkitem.sdcid, sdiworkitem.keyid1, sdiworkitem.keyid2, sdiworkitem.keyid3 FROM   sdiworkitem, rsetitems WHERE  rsetitems.rsetid = ? AND    sdiworkitem.workitemid = rsetitems.keyid1 AND    sdiworkitem.workitemversionid = rsetitems.keyid2 ORDER BY 1, 2, 3, 4";
        this.database.createPreparedResultSet(check, new Object[]{rsetid});
        StringBuffer refs = new StringBuffer();
        for (int i = 0; i < 10 && this.database.getNext(); ++i) {
            refs.append("<br/>").append(this.database.getString("sdcid")).append(": ").append(this.database.getString("keyid1"));
        }
        if (refs.length() > 0) {
            boolean more = this.database.getNext();
            this.throwError("WorkItemUsed", "VALIDATION", "Work Item(s) cannot be deleted because of " + (more ? "at least" : "") + " the following references:" + refs + (more ? "<br/>..." : ""));
        }
        if (actionProps != null) {
            String workitems = "";
            String workitemversions = "";
            if (actionProps.containsKey(SDC_KEYCOLID1)) {
                workitems = actionProps.get(SDC_KEYCOLID1).toString();
            } else if (actionProps.containsKey("keyid1")) {
                workitems = actionProps.get("keyid1").toString();
            }
            if (actionProps.containsKey(SDC_KEYCOLID2)) {
                workitemversions = actionProps.get(SDC_KEYCOLID2).toString();
            } else if (actionProps.containsKey("keyid2")) {
                workitemversions = actionProps.get("keyid2").toString();
            }
            if (workitems != null && workitems.length() > 0) {
                this.clearCache(StringUtil.split(workitems, ";"), workitemversions.length() > 0 ? StringUtil.split(workitemversions, ";") : new String[]{});
            }
        }
        if (!"Y".equals(actionProps.getProperty("__sdcruleconfirm"))) {
            SamplingPlanUtil.checkSPItemLinks(SDCID, rsetid, this.getQueryProcessor(), this.getTranslationProcessor(), this.getActionProcessor(), this.getSDCProcessor());
        }
        WorkItemUtil.deleteFromWorkItemItem(rsetid, actionProps, this.connectionInfo, this.getTranslationProcessor(), this.logger, SDCID, this.getQueryProcessor(), this.getActionProcessor());
        WorkItemUtil.checkWICurrentUsage(rsetid, this.getQueryProcessor(), this.getTranslationProcessor(), this.getConnectionInfo());
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select w.embedchildsampleplanid, w.embedchildsampleplanversionid from workitem w, rsetitems r where w.workitemid = r.keyid1 and w.workitemversionid = r.keyid2 and r.sdcid = 'WorkItem' and r.rsetid = ?", new Object[]{rsetid});
        if (ds != null && ds.size() > 0) {
            DataSet deleteds = new DataSet();
            for (int i = 0; i < ds.size(); ++i) {
                String childsampleplanid = ds.getString(i, "embedchildsampleplanid", "");
                String childsampleplanversionid = ds.getString(i, "embedchildsampleplanversionid", "");
                if (childsampleplanid.length() <= 0 || childsampleplanversionid.length() <= 0) continue;
                int row = deleteds.addRow();
                deleteds.setString(row, "childsampleplanid", childsampleplanid);
                deleteds.setString(row, "childsampleplanversionid", childsampleplanversionid);
            }
            if (deleteds.size() > 0) {
                actionProps.setProperty("__delete_childsampleplanid", deleteds.getColumnValues("childsampleplanid", ";"));
                actionProps.setProperty("__delete_childsampleplanversionid", deleteds.getColumnValues("childsampleplanversionid", ";"));
            }
        }
    }

    @Override
    public void postDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        if ("Y".equals(actionProps.getProperty("__sdcruleconfirm"))) {
            SamplingPlanUtil.deleteSPItemLinks(SDCID, rsetid, this.getQueryProcessor(), this.getActionProcessor(), this.getSDCProcessor());
        }
        String deleteChildSamplePlanId = actionProps.getProperty("__delete_childsampleplanid", "");
        String deleteChildSamplePlanVersionId = actionProps.getProperty("__delete_childsampleplanversionid", "");
        if (deleteChildSamplePlanId.length() > 0 && deleteChildSamplePlanVersionId.length() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "LV_ChildSamplePlan");
            props.setProperty("keyid1", deleteChildSamplePlanId);
            props.setProperty("keyid2", deleteChildSamplePlanVersionId);
            props.setProperty("auditreason", "Deleted parent WorkItem");
            this.getActionProcessor().processActionClass(DeleteSDI.class.getName(), props);
        }
        String[] workItemIds = actionProps.getProperty("keyid1", "").trim().split(";");
        String[] workItemVersionIds = actionProps.getProperty("keyid2", "").trim().split(";");
        if (workItemIds != null && workItemVersionIds != null && workItemIds.length == workItemVersionIds.length) {
            for (int i = 0; i < workItemIds.length; ++i) {
                this.clearCache("Workitem", workItemIds[i] + ";" + workItemVersionIds[i]);
                this.clearCache("TestMethod", workItemIds[i] + ";" + workItemVersionIds[i]);
            }
        }
    }

    private void preDetail(PropertyList actionProps) {
        if (actionProps != null) {
            String workitems = "";
            String workitemversions = "";
            String separator = actionProps.getProperty("separator", ";");
            if (actionProps.containsKey(SDC_KEYCOLID1)) {
                workitems = actionProps.get(SDC_KEYCOLID1).toString();
            } else if (actionProps.containsKey("keyid1")) {
                workitems = actionProps.get("keyid1").toString();
            }
            if (actionProps.containsKey(SDC_KEYCOLID2)) {
                workitemversions = actionProps.get(SDC_KEYCOLID2).toString();
            } else if (actionProps.containsKey("keyid2")) {
                workitemversions = actionProps.get("keyid2").toString();
            }
            if (workitems != null && workitems.length() > 0) {
                this.clearCache(StringUtil.split(workitems, separator), workitemversions.length() > 0 ? StringUtil.split(workitemversions, separator) : new String[]{});
            }
        }
    }

    @Override
    public void preAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.preDetail(actionProps);
        this.manipulateReagentTypes(sdiData, actionProps);
    }

    @Override
    public void preEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.preDetail(actionProps);
        this.manipulateReagentTypes(sdiData, actionProps);
    }

    @Override
    public void preDeleteDetail(String rsetid, PropertyList actionProps) throws SapphireException {
        this.preDetail(actionProps);
    }

    private void clearCache(String[] workitems, String[] workitemversions) {
        for (int i = 0; i < workitems.length; ++i) {
            String cacheKey = workitems[i] + ";" + (workitemversions.length > i ? workitemversions[i] : "");
            CacheUtil.remove(this.connectionInfo.getDatabaseId(), "Workitem", cacheKey);
        }
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    private void manipulateReagentTypes(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet data;
        if (actionProps.containsKey("amountunits") && (data = sdiData.getDataset("workitemreagenttype")) != null) {
            if (!data.isValidColumn("amountunitstype")) {
                data.addColumn("amountunitstype", 0);
            }
            try {
                for (int row = 0; row < data.getRowCount(); ++row) {
                    String units = data.getValue(row, "amountunits", "");
                    if (units.length() <= 0) continue;
                    if (units.equalsIgnoreCase("(containers)")) {
                        data.setValue(row, "amountunitstype", "C");
                        data.setValue(row, "amountunits", "");
                        continue;
                    }
                    data.setValue(row, "amountunitstype", "U");
                }
            }
            catch (Exception e) {
                throw new SapphireException(this.getTranslationProcessor().translate(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId()))), e);
            }
        }
    }

    private void checkLabWorkArea(DataSet primary, int i) throws SapphireException {
        String labid = this.getColumnValue(primary, i, "testingdepartmentid");
        String workarea = this.getColumnValue(primary, i, "workareadepartmentid");
        if ((this.hasPrimaryValueChanged(primary, i, "workareadepartmentid") || this.hasPrimaryValueChanged(primary, i, "testingdepartmentid")) && workarea.length() > 0 && labid.length() > 0 && !labid.equals(workarea)) {
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer sql = new StringBuffer();
            sql.append("select departmentid from department where departmentid = ").append(safeSQL.addVar(workarea)).append(" and parentdepartmentid=").append(safeSQL.addVar(labid));
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds == null || ds.size() == 0) {
                throw new SapphireException(this.getTranslationProcessor().translate("Work Area (" + workarea + ") is not in the Testing Lab (" + labid + ")."));
            }
        }
    }

    private String getColumnValue(DataSet primary, int indx, String columnid) {
        String value = "";
        value = primary.isValidColumn(columnid) ? primary.getString(indx, columnid, "") : this.getOldPrimaryValue(primary, indx, columnid);
        return value;
    }

    private void clearCache(String cacheName, String key) {
        CacheUtil.remove(this.getDatabaseid(), cacheName, key);
        CacheUtil.removeAllStartWith(this.getQueryProcessor().getConnectionid(), cacheName, key);
    }

    private void doExpireVersionValidation(DataSet primary) throws SapphireException {
        String queryWorkItemWithWorkitem = "SELECT wii.workitemid, wii.workitemversionid FROM workitem wi, workitemitem wii WHERE wii. sdcid = 'WorkItem' AND wii.keyid1 = ? AND wii.keyid2 = ? AND wi.workitemid = wii.workitemid AND wi.workitemversionid = wii.workitemversionid AND wi.versionstatus <> 'E'";
        PreparedStatement workItemWithWorkitemStatement = this.database.prepareStatement("workItemWithSpec", queryWorkItemWithWorkitem);
        String queryWorkItemWithCurrentWorkitem = "SELECT wii.workitemid, wii.workitemversionid FROM workitem wi, workitemitem wii WHERE wii. sdcid = 'WorkItem' AND wii.keyid1 = ? AND wii.keyid2 = 'C' AND wi.workitemid = wii.workitemid AND wi.workitemversionid = wii.workitemversionid AND wi.versionstatus <> 'E'";
        PreparedStatement workItemWithCurrentWorkItemStatement = this.database.prepareStatement("workItemWithCurrentSpec", queryWorkItemWithCurrentWorkitem);
        String queryWorkItemSisterVersions = "SELECT wokitemid FROM workitem WHERE wokitemid = ? AND versionstatus IN ( 'P', 'C' )";
        PreparedStatement workItemSisterVersionsStatement = this.database.prepareStatement("wokritemSisterVersions", queryWorkItemSisterVersions);
        String querySdiWorkItem = "SELECT sdcid, keyid1, keyid2, keyid3,workitemid,workitemversionid FROM sdiworkitem where SDCID in ('Product','LV_ProductStage','SamplePoint','Location','LV_ReagentType','Study') and coalesce(sdiworkitem.workitemstatus,'null') not in ('Cancelled','Completed') and workitemid = ? and workitemversionid = ? ";
        PreparedStatement sdiWorkItemStatement = this.database.prepareStatement("querySdiWorkItem", querySdiWorkItem);
        String querySdiWorkItemForSampleTemplate = "SELECT sdiworkitem.keyid1,sdiworkitem.workitemid,sdiworkitem.workitemversionid FROM   sdiworkitem, s_sample where sdiworkitem.SDCID = 'Sample' and sdiworkitem.keyid1 = s_sample.s_sampleid and s_sample.templateflag='Y' and (s_sample.scheduletemplateflag = 'N' or s_sample.scheduletemplateflag is null) and coalesce(sdiworkitem.workitemstatus,'null') not in ('Cancelled','Completed') and sdiworkitem.workitemid = ? and sdiworkitem.workitemversionid = ?";
        PreparedStatement sdiWorkItemForSampleTemplateStatement = this.database.prepareStatement("querySdiWorkItemForSampleTemplate", querySdiWorkItemForSampleTemplate);
        try {
            StringBuffer message = new StringBuffer();
            for (int i = 0; i < primary.size(); ++i) {
                String oldVersionStatus;
                if (!this.hasPrimaryValueChanged(primary, i, "versionstatus") || !"E".equals(primary.getValue(i, "versionstatus", "")) || !"C".equals(oldVersionStatus = this.getOldPrimaryValue(primary, i, "versionstatus")) && !"P".equals(oldVersionStatus) && !"A".equals(oldVersionStatus)) continue;
                String workitemid = primary.getValue(i, SDC_KEYCOLID1, "");
                String workitemversionid = primary.getValue(i, SDC_KEYCOLID2, "");
                LinkedHashMap<String, String> keyColsValues = new LinkedHashMap<String, String>();
                keyColsValues.put(SDC_KEYCOLID1, workitemid);
                keyColsValues.put(SDC_KEYCOLID2, workitemversionid);
                LinkedHashMap<String, String> keyCols = new LinkedHashMap<String, String>();
                keyCols.put("keyid1", SDC_KEYCOLID1);
                keyCols.put("keyid2", SDC_KEYCOLID2);
                SDCExpiryValidationUtil.validateExpiry(SDCID, "QCMethod", keyCols, keyColsValues, message, this.getSDCProcessor());
                SDCExpiryValidationUtil.validateExpiryWithRefTable(SDCID, "QCMethodSampleType", "QCMethod", keyCols, keyColsValues, message, this.getSDCProcessor());
                SDCExpiryValidationUtil.validateExpiry(SDCID, "LV_ChildSamplePlan", keyCols, keyColsValues, message, this.getSDCProcessor());
                SDCExpiryValidationUtil.validateExpiry(SDCID, "LV_ArrayMethod", keyCols, keyColsValues, message, this.getSDCProcessor());
                SDCExpiryValidationUtil.validateExpiryWithTableName(SDCID, "LV_EventDef", "s_eventdefstatmap", keyCols, keyColsValues, message, this.getSDCProcessor());
                SDCExpiryValidationUtil.validateExpiryWithTableName(SDCID, "LV_EventDef", "s_eventdefstspecimendefwi", keyCols, keyColsValues, message, this.getSDCProcessor());
                SDCExpiryValidationUtil.validateSamplingPlanReference(SDCID, keyCols, keyColsValues, message, this.getSDCProcessor());
                SDCExpiryValidationUtil.validateRequestTemplateReference(SDCID, keyCols, keyColsValues, message, this.getSDCProcessor());
                sdiWorkItemStatement.setString(1, workitemid);
                sdiWorkItemStatement.setString(2, workitemversionid);
                DataSet sdiWorkItemWithWI = new DataSet(sdiWorkItemStatement.executeQuery());
                if (sdiWorkItemWithWI.getRowCount() > 0) {
                    for (int j = 0; j < sdiWorkItemWithWI.getRowCount(); ++j) {
                        String expired;
                        SDIRequest sdiRequest = new SDIRequest();
                        sdiRequest.setSDIList(sdiWorkItemWithWI.getValue(j, "sdcid"), sdiWorkItemWithWI.getValue(j, "keyid1"), sdiWorkItemWithWI.getValue(j, "keyid2"), sdiWorkItemWithWI.getValue(j, "keyid3"));
                        sdiRequest.setRequestItem("primary");
                        SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
                        DataSet primaryDs = sdiData.getDataset("primary");
                        if (primaryDs != null && primaryDs.size() > 0 && "E".equalsIgnoreCase(expired = primaryDs.getString(0, "VERSIONSTATUS", ""))) continue;
                        if (message.length() > 0) {
                            message.append(", ").append("<br>");
                        }
                        message.append(sdiWorkItemWithWI.getValue(j, "sdcid")).append("-").append(sdiWorkItemWithWI.getValue(j, "keyid1")).append(sdiWorkItemWithWI.getValue(j, "keyid2").equalsIgnoreCase("(null)") ? "" : ";" + sdiWorkItemWithWI.getValue(j, "keyid2")).append(sdiWorkItemWithWI.getValue(j, "keyid3").equalsIgnoreCase("(null)") ? "" : ";" + sdiWorkItemWithWI.getValue(j, "keyid3"));
                    }
                }
                sdiWorkItemForSampleTemplateStatement.setString(1, workitemid);
                sdiWorkItemForSampleTemplateStatement.setString(2, workitemversionid);
                DataSet sdiWorkItemForSampleTemplateWithWI = new DataSet(sdiWorkItemForSampleTemplateStatement.executeQuery());
                if (sdiWorkItemForSampleTemplateWithWI.getRowCount() > 0) {
                    for (int j = 0; j < sdiWorkItemForSampleTemplateWithWI.getRowCount(); ++j) {
                        if (message.length() > 0) {
                            message.append(", ").append("<br>");
                        }
                        message.append("Sample Template").append("-").append(sdiWorkItemForSampleTemplateWithWI.getValue(j, "keyid1")).append(";");
                    }
                }
                workItemWithWorkitemStatement.setString(1, workitemid);
                workItemWithWorkitemStatement.setString(2, workitemversionid);
                DataSet workItemWithWorkitem = new DataSet(workItemWithWorkitemStatement.executeQuery());
                workItemWithCurrentWorkItemStatement.setString(1, workitemid);
                DataSet workItemWithCurrentWorkItem = new DataSet(workItemWithCurrentWorkItemStatement.executeQuery());
                if (workItemWithWorkitem.getRowCount() > 0) {
                    for (int j = 0; j < workItemWithWorkitem.getRowCount(); ++j) {
                        if (message.length() > 0) {
                            message.append(", ");
                        }
                        message.append("<br>").append(workItemWithWorkitem.getValue(j, SDC_KEYCOLID1)).append(";").append(workItemWithWorkitem.getValue(j, SDC_KEYCOLID2)).append("<br>");
                    }
                }
                if ("A".equals(oldVersionStatus) || workItemWithCurrentWorkItem.getRowCount() <= 0) continue;
                workItemSisterVersionsStatement.setString(1, workitemid);
                DataSet specSisterVersions = new DataSet(workItemSisterVersionsStatement.executeQuery());
                if (specSisterVersions.getRowCount() != 1) continue;
                for (int j = 0; j < workItemWithCurrentWorkItem.getRowCount(); ++j) {
                    if (message.length() > 0) {
                        message.append(", ");
                    }
                    message.append("TestMethod(s)").append("<br>").append(workItemWithCurrentWorkItem.getValue(j, SDC_KEYCOLID1)).append(";").append(workItemWithCurrentWorkItem.getValue(j, SDC_KEYCOLID2)).append("<br>");
                }
            }
            if (message.length() > 0) {
                String validationMessage = this.getTranslationProcessor().translate("TestMethod(s) cannot be 'Expired' because of references, direct or as 'Current', in the following ") + "<br>";
                validationMessage = validationMessage + message.toString() + ".";
                this.throwError("TestMethod(s)", "VALIDATION", validationMessage);
            }
        }
        catch (SQLException e) {
            throw new SapphireException(this.getTranslationProcessor().translate("Failed to retrieve TestMethod and Specification detail"));
        }
    }
}

