/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.SDCExpiryValidationUtil;
import com.labvantage.sapphire.actions.sdidata.DataEntryLimitsUtil;
import com.labvantage.sapphire.actions.workitem.WorkItemUtil;
import com.labvantage.sapphire.admin.ddt.DDTUtil;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ParamList
extends BaseSDCRules {
    public static final String SDC_PARAMLIST = "ParamList";
    public static final String SDC_KEYCOLID1 = "PARAMLISTID";
    public static final String SDC_KEYCOLID2 = "PARAMLISTVERSIONID";
    public static final String SDC_KEYCOLID3 = "VARIANTID";
    private String LABVANTAGE_CVS_ID = "$Revision: 103323 $";
    private List<String> paramlistds = new ArrayList<String>();
    private List<String> paramlistversionids = new ArrayList<String>();
    private List<String> variantids = new ArrayList<String>();
    private List<String> targetsdcids = new ArrayList<String>();

    private void manipulateReagentTypes(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet data;
        if (actionProps.containsKey("amountunits") && (data = sdiData.getDataset("paramlistreagenttype")) != null) {
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

    private String ArrayToString(String[] array, String delimiter) {
        StringBuffer out = new StringBuffer();
        for (int i = 0; i < array.length; ++i) {
            if (i > 0) {
                out.append(delimiter);
            }
            out.append(array[i]);
        }
        return out.toString();
    }

    @Override
    public void preDeleteDetail(String rsetid, PropertyList actionProps) throws SapphireException {
        if (actionProps.getProperty("linkid", "").equalsIgnoreCase("param list items")) {
            String[] paramlistid = StringUtil.split(actionProps.getProperty("paramlistid", actionProps.getProperty("keyid1", "")), actionProps.getProperty("separator", ";"));
            String[] paramlistversionid = StringUtil.split(actionProps.getProperty("paramlistversionid", actionProps.getProperty("keyid2", "")), actionProps.getProperty("separator", ";"));
            String[] variantid = StringUtil.split(actionProps.getProperty("variantid", actionProps.getProperty("keyid3", "")), actionProps.getProperty("separator", ";"));
            if (paramlistid.length > 0 && paramlistid.length == paramlistversionid.length && paramlistversionid.length == variantid.length) {
                String[] paramid = StringUtil.split(actionProps.getProperty("paramid", ""), actionProps.getProperty("separator", ";"));
                String[] paramtype = StringUtil.split(actionProps.getProperty("paramtype", ""), actionProps.getProperty("separator", ";"));
                if (paramid.length > 0 && paramid.length == paramtype.length && (paramlistid.length == paramid.length || paramlistid.length == 1)) {
                    StringBuffer checksql = new StringBuffer();
                    SafeSQL safeSQL = new SafeSQL();
                    checksql.append("SELECT specid, specversionid, paramid, paramtype, paramlistid, paramlistversionid, variantid FROM specparamitems WHERE ");
                    checksql.append("paramlistid IN (");
                    checksql.append(safeSQL.addIn(this.ArrayToString(paramlistid, "','")));
                    checksql.append(") AND paramlistversionid IN (");
                    checksql.append(safeSQL.addIn(this.ArrayToString(paramlistversionid, "','")));
                    checksql.append(") AND variantid IN (");
                    checksql.append(safeSQL.addIn(this.ArrayToString(variantid, "','")));
                    checksql.append(") AND paramid IN (");
                    checksql.append(safeSQL.addIn(this.ArrayToString(paramid, "','")));
                    checksql.append(") AND paramtype IN (");
                    checksql.append(safeSQL.addIn(this.ArrayToString(paramtype, "','")));
                    checksql.append(")");
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("checkspecs", checksql.toString(), safeSQL.getValues());
                    if (ds != null) {
                        if (ds.size() > 0) {
                            StringBuffer found = new StringBuffer();
                            for (int i = 0; i < paramid.length; ++i) {
                                HashMap<String, String> find = new HashMap<String, String>(5);
                                if (paramlistid.length == 1) {
                                    find.put("paramlistid", paramlistid[0]);
                                    find.put("paramlistversionid", paramlistversionid[0]);
                                    find.put("variantid", variantid[0]);
                                } else {
                                    find.put("paramlistid", paramlistid[i]);
                                    find.put("paramlistversionid", paramlistversionid[i]);
                                    find.put("variantid", variantid[i]);
                                }
                                find.put("paramid", paramid[i]);
                                find.put("paramtype", paramtype[i]);
                                int _find = ds.findRow(find, 0);
                                if (_find <= -1) continue;
                                if (found.length() > 0) {
                                    found.append(", ");
                                }
                                found.append("Spec ").append(ds.getValue(_find, "specid", "")).append(" (").append(ds.getValue(_find, "specversionid", "")).append(") - ");
                                found.append(paramid[i]).append(" (").append(paramtype[i]).append(")");
                            }
                            if (found.length() > 0) {
                                this.throwError("ParamListUsed", "VALIDATION", "Parameter List Items(s) cannot be deleted because of the following references: " + found.toString());
                            }
                        }
                    } else {
                        this.throwError("ParamListUsed", "VALIDATION", "Parameter List Items(s) cannot be deleted because delete check failed.");
                    }
                } else {
                    this.throwError("ParamListUsed", "VALIDATION", "Parameter List Items(s) cannot be deleted because delete check could not be started. Parameter could not be found.");
                }
            } else {
                this.throwError("ParamListUsed", "VALIDATION", "Parameter List Items(s) cannot be deleted because delete check could not be started. Parameter list could not be found.");
            }
        }
    }

    @Override
    public void preAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (actionProps.getProperty("datatypes").contains("V") && actionProps.getProperty("entryreftypeid").equalsIgnoreCase("(null)")) {
            throw new SapphireException(this.getTranslationProcessor().translate("Cannot proceed without reference for Validated Data parameter"));
        }
        DataEntryLimitsUtil limitsUtil = new DataEntryLimitsUtil(this.getSDCProcessor(), this.getTranslationProcessor(), this.database, this.logger, this.connectionInfo);
        limitsUtil.manipulateLimits(sdiData, actionProps);
        this.manipulateReagentTypes(sdiData, actionProps);
        this.addNewParam(sdiData);
    }

    private void addNewParam(SDIData sdiData) throws SapphireException {
        DataSet dataSet = sdiData.getDataset("paramlistitem");
        if (dataSet != null && dataSet.size() > 0) {
            StringBuilder newparam = new StringBuilder();
            SafeSQL safeSQL = new SafeSQL();
            String sql = "SELECT paramid FROM param where paramid in (" + safeSQL.addIn(dataSet.getColumnValues("paramid", "','")) + ")";
            DataSet existingParamids = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            String paramid = dataSet.getColumnValues("paramid", ";");
            HashSet<String> hm = new HashSet<String>();
            String[] paramids = StringUtil.split(paramid, ";");
            for (int i = 0; i < paramids.length; ++i) {
                if (paramids[i] == null || paramids[i].length() <= 0 || existingParamids.findRow("paramid", paramids[i]) >= 0 || hm.contains(paramids[i])) continue;
                newparam.append(";").append(paramids[i]);
                hm.add(paramids[i]);
            }
            if (!hm.isEmpty()) {
                HashMap paramProps = this.getSDCProcessor().getSDCProperties("Param");
                String usabeSize = (String)paramProps.get("keyidusablesize");
                this.checkUsableKeyIdSize(newparam.substring(1), usabeSize);
                PropertyList actionProp = new PropertyList();
                actionProp.setProperty("sdcid", "Param");
                actionProp.setProperty("keyid1", newparam.substring(1));
                this.getActionProcessor().processAction("AddSDI", "1", actionProp);
            }
        }
    }

    private void checkUsableKeyIdSize(String newparam, String usablekeysize) throws SapphireException {
        if (usablekeysize != null && usablekeysize.length() > 0) {
            int usablesize = Integer.parseInt(usablekeysize);
            String[] newparamArr = newparam.split(";");
            for (int i = 0; i < newparamArr.length; ++i) {
                String param = newparamArr[i];
                if (param.length() <= usablesize) continue;
                throw new SapphireException(this.getTranslationProcessor().translate("The length of one or more Param(s) is more than usable key size."));
            }
        }
    }

    @Override
    public void preEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (actionProps.getProperty("datatypes").contains("V") && actionProps.getProperty("entryreftypeid").equalsIgnoreCase("(null)")) {
            throw new SapphireException(this.getTranslationProcessor().translate("Cannot proceed without reference for Validated Data parameter"));
        }
        DataEntryLimitsUtil limitsUtil = new DataEntryLimitsUtil(this.getSDCProcessor(), this.getTranslationProcessor(), this.database, this.logger, this.connectionInfo);
        limitsUtil.manipulateLimits(sdiData, actionProps);
        this.manipulateReagentTypes(sdiData, actionProps);
    }

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        String modifiableFlag = (String)actionProps.get("modifiableflag");
        if (modifiableFlag == null || modifiableFlag.length() == 0) {
            actionProps.setProperty("modifiableflag", "Y");
        }
        if (!this.isCMTImport()) {
            this.checkLabWorkArea(primary);
        }
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        String createworkitem = actionProps.getProperty("createworkitem", "");
        String workitemid = actionProps.getProperty("workitemid", "");
        if (createworkitem.equalsIgnoreCase("Y") && workitemid.length() > 0) {
            DataSet primary = sdiData.getDataset("primary");
            String paramlistdesc = actionProps.getProperty("paramlistdesc", "");
            DataSet workitemds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT workitemid,workitemversionid,versionstatus FROM workitem WHERE  workitemid  = ? order by workitemversionid desc", (Object[])new String[]{workitemid});
            if (workitemds.size() > 0) {
                workitemid = workitemds.getString(0, "workitemid", "");
                String workitemversionid = workitemds.getString(0, "workitemversionid", "");
                String versionStatus = workitemds.getString(0, "versionstatus", "");
                if (versionStatus.equalsIgnoreCase("A") || versionStatus.equalsIgnoreCase("C")) {
                    PropertyList actionProp = new PropertyList();
                    actionProp.setProperty("sdcid", "WorkItem");
                    actionProp.setProperty("keyid1", workitemid);
                    actionProp.setProperty("keyid2", workitemversionid);
                    this.getActionProcessor().processAction("AddSDIVersion", "1", actionProp);
                    String newkeyid2 = actionProp.getProperty("newkeyid2", "");
                    this.addParamListToWI(primary, workitemid, newkeyid2, paramlistdesc, this.getNextWorkItemItemID(workitemid, workitemversionid));
                } else {
                    this.addParamListToWI(primary, workitemid, workitemversionid, paramlistdesc, this.getNextWorkItemItemID(workitemid, workitemversionid));
                }
            } else {
                PropertyList props = new PropertyList();
                String securitydepartment = actionProps.getProperty("securitydepartment", "");
                props.setProperty("sdcid", "WorkItem");
                props.setProperty("keyid1", workitemid);
                props.setProperty("keyid2", "1");
                props.setProperty("versionstatus", "P");
                props.setProperty("workitemdesc", paramlistdesc);
                props.setProperty("workitemtypeflag", "W");
                if (securitydepartment.length() > 0) {
                    props.setProperty("securitydepartment", securitydepartment);
                }
                this.getActionProcessor().processAction("AddSDI", "1", props);
                this.addParamListToWI(primary, workitemid, "1", paramlistdesc, 1);
            }
        }
        if (this.isCMTImport()) {
            this.handleCMT(sdiData);
        }
        CacheUtil.clear(this.connectionInfo.getDatabaseId(), "ParamList_CurrentVersion", true);
        CacheUtil.clear(this.connectionInfo.getDatabaseId(), "Workitem", true);
    }

    private void handleCMT(SDIData sdiData) throws ActionException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            if (!primary.getValue(i, "enableautoredocalcflag", "N").equalsIgnoreCase("Y") || !OpalUtil.isNotEmpty(primary.getValue(i, "targetsdcid"))) continue;
            this.populateList(primary, i, this.paramlistds, this.paramlistversionids, this.variantids, this.targetsdcids);
        }
        if (this.needToSync()) {
            this.getActionProcessor().processAction("SyncCrossSDICalcInfoForParamList", "1", this.populateSyncActionProps(this.paramlistds, this.paramlistversionids, this.variantids, this.targetsdcids));
        }
        this.clearList();
    }

    private int getNextWorkItemItemID(String workitemid, String workitemversionid) throws SapphireException {
        String maxWorkItemItemId = this.connectionInfo.isOracle() ? " nvl( max( to_number( workitemitemid ) ), 0 )" : " isnull( max( cast( workitemitemid AS Integer ) ), 0 )";
        int maxid = this.database.getPreparedCount("SELECT " + maxWorkItemItemId + " FROM  workitemitem  WHERE workitemid =? and workitemversionid=?", new Object[]{workitemid, workitemversionid});
        return maxid + 1;
    }

    private void addParamListToWI(DataSet primary, String workitemid, String workitemversionid, String workitemitemdesc, int workitemitemid) throws SapphireException {
        StringBuffer paramlistidStr = new StringBuffer();
        StringBuffer variantidStr = new StringBuffer();
        StringBuffer workitemitemidStr = new StringBuffer();
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "WorkItem");
        props.setProperty("keyid1", workitemid);
        props.setProperty("keyid2", workitemversionid);
        props.setProperty("linkid", "WorkItemItems");
        props.setProperty("__sdcid", SDC_PARAMLIST);
        props.setProperty("__keyid2", "C");
        props.setProperty("repeatcount", "1");
        props.setProperty("forcenewflag", "Y");
        props.setProperty("mandatoryflag", "Y");
        props.setProperty("workitemitemdesc", workitemitemdesc);
        for (int i = 0; i < primary.size(); ++i) {
            workitemitemidStr.append(";").append(i + workitemitemid);
            paramlistidStr.append(";").append(primary.getString(i, "paramlistid", ""));
            variantidStr.append(";").append(primary.getString(i, "variantid", ""));
        }
        props.setProperty("workitemitemid", workitemitemidStr.substring(1));
        props.setProperty("usersequence", workitemitemidStr.substring(1));
        props.setProperty("__keyid1", paramlistidStr.substring(1));
        props.setProperty("__keyid3", variantidStr.substring(1));
        this.getActionProcessor().processAction("AddSDIDetail", "1", props);
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        TranslationProcessor tp = this.getTranslationProcessor();
        String check = "SELECT sdidata.sdcid, sdidata.keyid1, sdidata.keyid2, sdidata.keyid3 FROM   sdidata, rsetitems WHERE  rsetitems.rsetid = ? AND    sdidata.paramlistid = rsetitems.keyid1 AND    sdidata.paramlistversionid = rsetitems.keyid2 AND    sdidata.variantid = rsetitems.keyid3 ORDER BY 1, 2, 3, 4";
        this.database.createPreparedResultSet(check, new Object[]{rsetid});
        StringBuffer refs = new StringBuffer();
        for (int i = 0; i < 10 && this.database.getNext(); ++i) {
            refs.append("<br/>").append(this.database.getString("sdcid")).append(": ").append(this.database.getString("keyid1"));
        }
        if (refs.length() > 0) {
            boolean more = this.database.getNext();
            this.throwError("ParamListUsed", "VALIDATION", "Parameter List(s) cannot be deleted because of " + (more ? "at least" : "") + " the following references:" + refs + (more ? "<br/>..." : ""));
        }
        if (DDTUtil.checkTableExists(this.database, "s_sdicertification")) {
            String sqlStmt = "DELETE FROM s_sdicertification WHERE ";
            sqlStmt = sqlStmt + (this.connectionInfo.getDbms().equals("ORA") ? " ( certifiedforsdcid, certifiedforkeyid1, certifiedforkeyid2, certifiedforkeyid3 ) IN ( SELECT sdcid, keyid1, keyid2, keyid3 from rsetitems WHERE rsetid = ? ) " : " certifiedforsdcid + ' ' + certifiedforkeyid1 + ' ' + certifiedforkeyid2 + ' ' + certifiedforkeyid3  IN ( SELECT sdcid + ' ' + keyid1 + ' ' + keyid2 + ' ' + keyid3 from rsetitems WHERE rsetid = ? ) ");
            this.database.executePreparedUpdate(sqlStmt, new Object[]{rsetid});
        }
        String checkSpecUsage = "SELECT specid, specversionid, paramlistid, paramlistversionid, variantid FROM specparamitems, rsetitems WHERE rsetitems.rsetid = ? AND specparamitems.paramlistid = rsetitems.keyid1 AND specparamitems.paramlistversionid = rsetitems.keyid2 AND specparamitems.variantid = rsetitems.keyid3 ORDER BY 1, 2, 3, 4, 5";
        this.database.createPreparedResultSet(checkSpecUsage, new Object[]{rsetid});
        refs = new StringBuffer();
        for (int i = 0; i < 10 && this.database.getNext(); ++i) {
            refs.append("<br/>").append(this.database.getString("specid")).append("( ").append(this.database.getString("specversionid")).append(" )");
        }
        if (refs.length() > 0) {
            boolean more = this.database.getNext();
            this.throwError("ParamListUsed", "VALIDATION", tp.translate("Parameter List(s) cannot be deleted because of ") + (more ? tp.translate("at least") : "") + tp.translate(" the following Specification references:") + refs + (more ? "<br/>..." : ""));
        }
        WorkItemUtil.deleteFromWorkItemItem(rsetid, actionProps, this.connectionInfo, this.getTranslationProcessor(), this.logger, SDC_PARAMLIST, this.getQueryProcessor(), this.getActionProcessor());
        CacheUtil.clear(this.connectionInfo.getDatabaseId(), "ParamList_CurrentVersion", true);
        CacheUtil.clear(this.connectionInfo.getDatabaseId(), "Workitem", true);
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.doExpireVersionValidation(primary);
        CacheUtil.clear(this.connectionInfo.getDatabaseId(), "ParamList_CurrentVersion", true);
        CacheUtil.clear(this.connectionInfo.getDatabaseId(), "Workitem", true);
        this.checkLabWorkArea(primary);
        for (int i = 0; i < primary.size(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, "accreditedflag")) continue;
            if (primary.getValue(i, "accreditedflag", "").equalsIgnoreCase("N")) {
                primary.setString(i, "accreditedaddressid", "");
                primary.setString(i, "accreditedaddresstype", "");
                primary.setDate(i, "accrediteddt", "null");
                continue;
            }
            primary.setString(i, "accreditedaddresstype", "AccreditedOrganization");
        }
    }

    private void doExpireVersionValidation(DataSet primary) throws SapphireException {
        String queryWorkItemWithCurrentPL = "SELECT wii.workitemid, wii.workitemversionid FROM workitem w, workitemitem wii WHERE w.workitemid = wii.workitemid AND w.workitemversionid = wii.workitemversionid AND wii.sdcid = 'ParamList' AND wii.keyid1 = ? AND wii.keyid2 = 'C' AND wii.keyid3 = ? and exists(select workitemid from workitem wi where wi.workitemid=wii.workitemid and wi.workitemversionid=wii.workitemversionid and wi.versionstatus in ('P','C','A') )";
        PreparedStatement workItemWithCurrentPLStatement = this.database.prepareStatement("workItemWithCurrentPL", queryWorkItemWithCurrentPL);
        String queryWorkItemWithPL = "SELECT wii.workitemid, wii.workitemversionid FROM workitem w, workitemitem wii WHERE w.workitemid = wii.workitemid AND w.workitemversionid = wii.workitemversionid AND wii.sdcid = 'ParamList' AND wii.keyid1 = ? AND wii.keyid2 = ? AND wii.keyid3 = ? and exists(select workitemid from workitem wi where wi.workitemid=wii.workitemid and wi.workitemversionid=wii.workitemversionid and wi.versionstatus in ('P','C','A') )";
        PreparedStatement workItemWithPLStatement = this.database.prepareStatement("workItemWithPL", queryWorkItemWithPL);
        String queryParameterListSisterVersions = "SELECT paramlistid FROM paramlist WHERE paramlistid = ? AND variantid = ?  AND versionstatus IN ( 'P', 'C' )";
        PreparedStatement parameterListSisterVersionsStatement = this.database.prepareStatement("parameterListSisterVersions", queryParameterListSisterVersions);
        String queryConsumableTypeWithCurrentPL = "SELECT T.REAGENTTYPEID, T.REAGENTTYPEVERSIONID FROM PARAMLISTREAGENTTYPE T WHERE T.PARAMLISTID = ? AND T.PARAMLISTVERSIONID = ? AND T.VARIANTID = ? AND EXISTS(SELECT TT.REAGENTTYPEID FROM REAGENTTYPE TT WHERE TT.REAGENTTYPEID=T.REAGENTTYPEID AND TT.REAGENTTYPEVERSIONID=T.REAGENTTYPEVERSIONID AND TT.VERSIONSTATUS IN ('P','C','A') )";
        PreparedStatement consumableTypeWithCurrentPLStatement = this.database.prepareStatement("consumableTypeWithCurrentPL", queryConsumableTypeWithCurrentPL);
        try {
            StringBuffer message = new StringBuffer();
            for (int i = 0; i < primary.size(); ++i) {
                String actualPlVersion;
                String isVersionProtectEnabled;
                PropertyList versionprotection;
                String oldVersionStatus;
                if (!this.hasPrimaryValueChanged(primary, i, "versionstatus") || !"E".equals(primary.getValue(i, "versionstatus", "")) || !"C".equals(oldVersionStatus = this.getOldPrimaryValue(primary, i, "versionstatus")) && !"A".equals(oldVersionStatus) && !"P".equals(oldVersionStatus)) continue;
                String paramListId = primary.getValue(i, "paramlistid", "");
                String paramListVariantId = primary.getValue(i, "variantid", "");
                String paramListVersionId = primary.getValue(i, "paramlistversionid", "");
                PropertyList paramListPolicy = this.getConfigurationProcessor().getPolicy("ParamListPolicy", "Sapphire Custom");
                if (paramListPolicy == null || (versionprotection = paramListPolicy.getPropertyListNotNull("expireddataprotection")) == null || versionprotection.size() <= 0 || !(isVersionProtectEnabled = versionprotection.getProperty(SDC_PARAMLIST.toLowerCase(), "")).equalsIgnoreCase("Y")) continue;
                LinkedHashMap<String, String> keyColsValues = new LinkedHashMap<String, String>();
                keyColsValues.put(SDC_KEYCOLID1, paramListId);
                keyColsValues.put(SDC_KEYCOLID2, paramListVersionId);
                keyColsValues.put(SDC_KEYCOLID3, paramListVariantId);
                LinkedHashMap<String, String> keyCols = new LinkedHashMap<String, String>();
                keyCols.put("keyid1", SDC_KEYCOLID1);
                keyCols.put("keyid2", SDC_KEYCOLID2);
                keyCols.put("keyid3", SDC_KEYCOLID3);
                SDCExpiryValidationUtil.validateExpiry(SDC_PARAMLIST, "LV_ReagentType", keyCols, keyColsValues, message, this.getSDCProcessor());
                if (keyColsValues.get(SDC_KEYCOLID2) == "C") {
                    actualPlVersion = SDCExpiryValidationUtil.getActualVersionId(SDC_PARAMLIST, keyCols, keyColsValues, this.getSDCProcessor());
                    keyColsValues.put(SDC_KEYCOLID2, actualPlVersion);
                }
                SDCExpiryValidationUtil.validateExpiry(SDC_PARAMLIST, "QCMethod", keyCols, keyColsValues, message, this.getSDCProcessor());
                if (keyColsValues.get(SDC_KEYCOLID2) == "C") {
                    actualPlVersion = SDCExpiryValidationUtil.getActualVersionId(SDC_PARAMLIST, keyCols, keyColsValues, this.getSDCProcessor());
                    keyColsValues.put(SDC_KEYCOLID2, actualPlVersion);
                }
                SDCExpiryValidationUtil.validateExpiry(SDC_PARAMLIST, "LV_FormulationMethod", keyCols, keyColsValues, message, this.getSDCProcessor());
                if (keyColsValues.get(SDC_KEYCOLID2) == "C") {
                    actualPlVersion = SDCExpiryValidationUtil.getActualVersionId(SDC_PARAMLIST, keyCols, keyColsValues, this.getSDCProcessor());
                    keyColsValues.put(SDC_KEYCOLID2, actualPlVersion);
                }
                SDCExpiryValidationUtil.validateExpiry(SDC_PARAMLIST, "SpecSDC", keyCols, keyColsValues, message, this.getSDCProcessor());
                SDCExpiryValidationUtil.validateExpiryWithTableName(SDC_PARAMLIST, "SpecSDC", "specparamitems", keyCols, keyColsValues, message, this.getSDCProcessor());
                workItemWithPLStatement.setString(1, paramListId);
                workItemWithPLStatement.setString(2, paramListVersionId);
                workItemWithPLStatement.setString(3, paramListVariantId);
                DataSet workitemWithPL = new DataSet(workItemWithPLStatement.executeQuery());
                if (workitemWithPL.getRowCount() > 0) {
                    for (int j = 0; j < workitemWithPL.getRowCount(); ++j) {
                        if (message.length() > 0) {
                            message.append(", ").append("<br>");
                        }
                        message.append("Test Method ").append("-").append(workitemWithPL.getValue(j, "workitemid")).append(";").append(workitemWithPL.getValue(j, "workitemversionid"));
                    }
                }
                if ("A".equals(oldVersionStatus)) continue;
                this.checkWorkitemExpiryStatus(workItemWithCurrentPLStatement, parameterListSisterVersionsStatement, message, paramListId, paramListVariantId);
            }
            if (message.length() > 0) {
                String validationMessage = this.getTranslationProcessor().translate("Parameter List(s) cannot be 'Expired' because of the following 'Provisional', 'Active' or 'Current' references in the following <br>");
                validationMessage = validationMessage + message.toString() + ".";
                throw new SapphireException("Failed to add capture operation.", validationMessage);
            }
            workItemWithCurrentPLStatement.close();
            parameterListSisterVersionsStatement.close();
        }
        catch (SQLException e) {
            throw new SapphireException(this.getTranslationProcessor().translate("Failed to retrieve TestMethod and ParameterList detail"));
        }
    }

    private DataSet fetchLinkedSDI(String sdcId, String keyid1, String keyid2, String keyid3, String requestItem, String queryFrom, String querywhere) {
        SDIRequest sdireq = new SDIRequest();
        sdireq.setSDCid(sdcId);
        sdireq.setRequestItem(requestItem);
        if (keyid1 != null) {
            sdireq.setKeyid1List(keyid1);
        }
        if (keyid2 != null) {
            sdireq.setKeyid2List(keyid2);
        }
        if (keyid3 != null) {
            sdireq.setKeyid3List(keyid3);
        }
        if (queryFrom != null) {
            sdireq.setQueryFrom(queryFrom);
        }
        if (querywhere != null) {
            sdireq.setQueryWhere(querywhere);
        }
        DataSet ds = this.getSDIProcessor().getSDIData(sdireq).getDataset(requestItem);
        return ds;
    }

    private void validateExpirywithRefTable(String linkSDC, String linktableid, HashMap<String, String> keyCols, StringBuffer message) {
        HashMap<String, ArrayList> errorcol = new HashMap<String, ArrayList>();
        String paramListId = keyCols.get("paramlistid");
        String paramListVersionId = keyCols.get("paramlistversionid");
        String paramListVariantId = keyCols.get("variantid");
        PropertyList sdcPropertyList = this.getSDCProcessor().getPropertyList(linkSDC);
        String tableid = sdcPropertyList.getProperty("tableid");
        String keycolid1 = sdcPropertyList.getProperty("keycolid1");
        String keycolid2 = sdcPropertyList.getProperty("keycolid2");
        String keycolid3 = sdcPropertyList.getProperty("keycolid3");
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT DISTINCT ").append(keycolid1).append(OpalUtil.isNotEmpty(keycolid2) ? "," + keycolid2 : "").append(OpalUtil.isNotEmpty(keycolid3) ? "," + keycolid3 : "").append(" From " + linktableid).append(" WHERE ").append(SDC_KEYCOLID1).append("= '").append(SafeSQL.encodeForSQL(paramListId, this.getConnectionProcessor().isOra())).append("' AND ").append(SDC_KEYCOLID2).append("= '").append(SafeSQL.encodeForSQL(paramListVersionId, this.getConnectionProcessor().isOra())).append("' AND ").append(SDC_KEYCOLID3).append("= '").append(SafeSQL.encodeForSQL(paramListVariantId, this.getConnectionProcessor().isOra())).append("'");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[0]);
        if (ds != null && !ds.isEmpty()) {
            ds.getColumnValues(keycolid1, ";");
            String queryWhere = "VERSIONSTATUS NOT IN ('E','H')";
            DataSet linkedDs = this.fetchLinkedSDI(linkSDC, ds.getColumnValues(keycolid1, ";"), ds.getColumnValues(keycolid2, ";"), ds.getColumnValues(keycolid3, ";"), "primary", null, queryWhere);
            if (linkedDs != null) {
                ArrayList error = this.getErrorMsg(keycolid1, keycolid2, keycolid3, linkedDs);
                errorcol.put(linkSDC, error);
            }
        }
        this.addToErrorMessage(linkSDC, message, errorcol);
    }

    private void validateExpiry(String linkSDC, HashMap<String, String> keyCols, StringBuffer message) {
        boolean isReverseDetailLink;
        HashMap<String, HashMap<String, String>> linkKeyCols = new HashMap<String, HashMap<String, String>>();
        HashMap<String, String> detailLinkProps = new HashMap<String, String>();
        String paramListId = keyCols.get("paramlistid");
        String paramListVersionId = keyCols.get("paramlistversionid");
        String paramListVariantId = keyCols.get("variantid");
        boolean isReversePrimaryLink = this.checkLinkData(linkSDC, SDC_PARAMLIST, linkKeyCols);
        if (isReversePrimaryLink) {
            HashMap errors = this.validateExpiryWithFkPrimaryData(linkSDC, paramListId, paramListVersionId, paramListVariantId);
            this.addToErrorMessage(linkSDC, message, errors);
        }
        if (isReverseDetailLink = this.checkDetailLinkData(linkSDC, SDC_PARAMLIST, detailLinkProps)) {
            HashMap errors = this.validateExpiryWithFkDetailLinkData(linkSDC, paramListId, paramListVariantId, paramListVersionId, detailLinkProps);
            this.addToErrorMessage(linkSDC, message, errors);
        }
    }

    private void addToErrorMessage(String linkSDC, StringBuffer message, HashMap errors) {
        if (errors.size() > 0 && errors.get(linkSDC) != null) {
            if (message.length() > 0) {
                message.append(", ");
            }
            message.append("<br>").append(linkSDC).append("<br>");
            ArrayList errorItems = (ArrayList)errors.get(linkSDC);
            Iterator i = errorItems.iterator();
            while (i.hasNext()) {
                message.append(i.next()).append("<br>");
            }
        }
    }

    private HashMap validateExpiryWithFkPrimaryData(String linkSDC, String paramListId, String paramListVersionId, String paramListVariantId) {
        PropertyList sdcPropertyList = this.getSDCProcessor().getPropertyList(linkSDC);
        String tableid = sdcPropertyList.getProperty("tableid");
        String keycolid1 = sdcPropertyList.getProperty("keycolid1");
        String keycolid2 = sdcPropertyList.getProperty("keycolid2");
        String keycolid3 = sdcPropertyList.getProperty("keycolid3");
        HashMap<String, ArrayList> errorcol = new HashMap<String, ArrayList>();
        StringBuffer sqlwhere = new StringBuffer();
        sqlwhere.append(SDC_KEYCOLID1).append("= '").append(SafeSQL.encodeForSQL(paramListId, this.getConnectionProcessor().isOra())).append("' AND ").append(SDC_KEYCOLID2).append("= '").append(SafeSQL.encodeForSQL(paramListVersionId, this.getConnectionProcessor().isOra())).append("' AND ").append(SDC_KEYCOLID3).append("= '").append(SafeSQL.encodeForSQL(paramListVariantId, this.getConnectionProcessor().isOra()));
        sqlwhere.append("' AND VERSIONSTATUS IN ('A','C','P') ");
        DataSet linkedDs = this.fetchLinkedSDI(linkSDC, paramListId, paramListVersionId, paramListVariantId, "primary", tableid, sqlwhere.toString());
        if (linkedDs.size() > 0) {
            ArrayList error = this.getErrorMsg(keycolid1, keycolid2, keycolid3, linkedDs);
            errorcol.put(linkSDC, error);
        }
        return errorcol;
    }

    private HashMap validateExpiryForParentDetailData(String sdcid, String keyid1, String keyid2, String keyid3, HashMap detailLinkData) {
        HashMap<String, ArrayList> errorcol = new HashMap<String, ArrayList>();
        String linksdcid = detailLinkData.get("linksdcid").toString();
        String linktableid = detailLinkData.get("linktableid") != null ? detailLinkData.get("linktableid").toString() : "";
        String sdccolumnid = detailLinkData.get("sdccolumnid") != null ? detailLinkData.get("sdccolumnid").toString() : "";
        String sdccolumnid2 = detailLinkData.get("sdccolumnid2") != null ? detailLinkData.get("sdccolumnid2").toString() : "";
        String sdccolumnid3 = detailLinkData.get("sdccolumnid3") != null ? detailLinkData.get("sdccolumnid3").toString() : "";
        DataSet ds = this.fetchLinkedSDI(sdcid, keyid1, keyid2, keyid3, detailLinkData.get("linktableid").toString(), null, null);
        if (ds.size() > 0) {
            ArrayList error = new ArrayList();
            for (int l = 0; l < ds.size(); ++l) {
                String queryWhere = "VERSIONSTATUS NOT IN ('E','H')";
                DataSet linkedDs = this.fetchLinkedSDI(linksdcid, ds.getValue(l, sdccolumnid), ds.getValue(l, sdccolumnid2), ds.getValue(l, sdccolumnid3), "primary", null, queryWhere);
                if (linkedDs == null) continue;
                error = this.getErrorMsg(sdccolumnid, sdccolumnid2, sdccolumnid3, linkedDs);
                errorcol.put(linksdcid, error);
            }
        }
        return errorcol;
    }

    private HashMap validateExpiryWithFkDetailLinkData(String sdcid, String keyid1, String keyid2, String keyid3, HashMap detailLinkData) {
        HashMap<String, ArrayList> errorcol = new HashMap<String, ArrayList>();
        String linksdcid = detailLinkData.get("linksdcid").toString();
        String linktableid = detailLinkData.get("linktableid") != null ? detailLinkData.get("linktableid").toString() : "";
        String sdccolumnid = detailLinkData.get("sdccolumnid") != null ? detailLinkData.get("sdccolumnid").toString() : "";
        String sdccolumnid2 = detailLinkData.get("sdccolumnid2") != null ? detailLinkData.get("sdccolumnid2").toString() : "";
        String sdccolumnid3 = detailLinkData.get("sdccolumnid3") != null ? detailLinkData.get("sdccolumnid3").toString() : "";
        PropertyList sdcPropertyList = this.getSDCProcessor().getPropertyList(sdcid);
        String tableid = sdcPropertyList.getProperty("tableid");
        Integer keycolCount = Integer.parseInt(sdcPropertyList.getProperty("keycolumns"));
        String keycolid1 = sdcPropertyList.getProperty("keycolid1");
        String keycolid2 = sdcPropertyList.getProperty("keycolid2");
        String keycolid3 = sdcPropertyList.getProperty("keycolid3");
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT ").append(keycolid1).append(OpalUtil.isNotEmpty(keycolid2) ? "," + keycolid2 : "").append(OpalUtil.isNotEmpty(keycolid3) ? "," + keycolid3 : "").append(" From " + linktableid).append(" WHERE ").append(SDC_KEYCOLID1).append("= '").append(SafeSQL.encodeForSQL(keyid1, this.getConnectionProcessor().isOra())).append("' AND ").append(SDC_KEYCOLID2).append("= '").append(SafeSQL.encodeForSQL(keyid2, this.getConnectionProcessor().isOra())).append("' AND ").append(SDC_KEYCOLID3).append("= '").append(SafeSQL.encodeForSQL(keyid3, this.getConnectionProcessor().isOra())).append("'");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[0]);
        if (ds != null && !ds.isEmpty()) {
            for (int l = 0; l < ds.size(); ++l) {
                String queryWhere = "VERSIONSTATUS NOT IN ('E','H')";
                DataSet linkedDs = this.fetchLinkedSDI(sdcid, ds.getValue(l, keycolid1), ds.getValue(l, keycolid2), ds.getValue(l, keycolid3), "primary", null, queryWhere);
                if (linkedDs == null) continue;
                ArrayList error = this.getErrorMsg(keycolid1, keycolid2, keycolid3, linkedDs);
                errorcol.put(sdcid, error);
            }
        }
        return errorcol;
    }

    private ArrayList getErrorMsg(String sdccolumnid, String sdccolumnid2, String sdccolumnid3, DataSet linkedDs) {
        ArrayList<String> error = new ArrayList<String>();
        for (int k = 0; k < linkedDs.size(); ++k) {
            StringBuffer errMsg = new StringBuffer();
            errMsg.append(linkedDs.getString(k, sdccolumnid));
            if (linkedDs.getString(k, sdccolumnid2) != null) {
                errMsg.append(";").append(linkedDs.getString(k, sdccolumnid2));
            }
            error.add(errMsg.toString());
        }
        return error;
    }

    private boolean checkLinkData(String SDC2, String toCheckSDC, HashMap<String, HashMap<String, String>> linkedKeyCols) {
        HashMap<String, String> keycols = new HashMap<String, String>();
        boolean linkfound = false;
        DataSet dt = this.getSDCProcessor().getLinksData(SDC2);
        if (dt.size() > 0) {
            for (int k = 0; k < dt.size(); ++k) {
                HashMap link = (HashMap)dt.get(k);
                if (link.get("linksdcid") == null || !((String)link.get("linksdcid")).equalsIgnoreCase(toCheckSDC) || link.get("linktype") == null || !((String)link.get("linktype")).equalsIgnoreCase("F")) continue;
                linkfound = true;
                if (link.get("sdccolumnid") != null) {
                    keycols.put("sdccolumnid", (String)link.get("sdccolumnid"));
                }
                if (link.get("sdccolumnid2") != null) {
                    keycols.put("sdccolumnid2", (String)link.get("sdccolumnid2"));
                }
                if (link.get("sdccolumnid3") != null) {
                    keycols.put("sdccolumnid3", (String)link.get("sdccolumnid3"));
                }
                linkedKeyCols.put(link.get("linkid").toString(), keycols);
            }
        }
        return linkfound;
    }

    private boolean checkDetailLinkData(String SDC2, String toCheckSDC, HashMap<String, String> linkDetailProps) {
        boolean linkfound = false;
        PropertyListCollection detailLink = this.getSDCProcessor().getDetailLinks(SDC2);
        PropertyList detailProp = detailLink.find("linksdcid", toCheckSDC);
        if (detailProp != null && detailProp.size() > 0 && detailProp.getProperty("linksdcid") != null && detailProp.getProperty("linktableid") != null && detailProp.getProperty("sdccolumnid") != null) {
            linkfound = true;
            linkDetailProps.put("sdccolumnid", detailProp.getProperty("sdccolumnid"));
            linkDetailProps.put("sdccolumnid2", detailProp.getProperty("sdccolumnid2"));
            linkDetailProps.put("sdccolumnid3", detailProp.getProperty("sdccolumnid3"));
            linkDetailProps.put("linktableid", detailProp.getProperty("linktableid"));
            linkDetailProps.put("linkid", detailProp.getProperty("linkid"));
            linkDetailProps.put("linksdcid", detailProp.getProperty("linksdcid"));
        }
        return linkfound;
    }

    private void checkWorkitemExpiryStatus(PreparedStatement workItemWithCurrentPLStatement, PreparedStatement parameterListSisterVersionsStatement, StringBuffer message, String paramListId, String paramListVariantId) throws SQLException {
        workItemWithCurrentPLStatement.setString(1, paramListId);
        workItemWithCurrentPLStatement.setString(2, paramListVariantId);
        DataSet workItemWithCurrentPL = new DataSet(workItemWithCurrentPLStatement.executeQuery());
        if (workItemWithCurrentPL.getRowCount() > 0) {
            parameterListSisterVersionsStatement.setString(1, paramListId);
            parameterListSisterVersionsStatement.setString(2, paramListVariantId);
            DataSet parameterListSisterVersions = new DataSet(parameterListSisterVersionsStatement.executeQuery());
            if (parameterListSisterVersions.getRowCount() == 1) {
                for (int j = 0; j < workItemWithCurrentPL.getRowCount(); ++j) {
                    if (message.length() > 0) {
                        message.append(", ");
                    }
                    message.append("<br>").append(workItemWithCurrentPL.getValue(j, "workitemid")).append(";").append(workItemWithCurrentPL.getValue(j, "workitemversionid"));
                }
            }
        }
    }

    private void checkConsumableTypeExpiryStatus(PreparedStatement consumableTypeWithCurrentPLStatement, PreparedStatement parameterListSisterVersionsStatement, StringBuffer message, String paramListId, String paramListVariantId, String paramListVersionId) throws SQLException {
        consumableTypeWithCurrentPLStatement.setString(1, paramListId);
        consumableTypeWithCurrentPLStatement.setString(2, paramListVersionId);
        consumableTypeWithCurrentPLStatement.setString(3, paramListVariantId);
        DataSet consumableTypeWithCurrentPL = new DataSet(consumableTypeWithCurrentPLStatement.executeQuery());
        if (consumableTypeWithCurrentPL.getRowCount() > 0) {
            parameterListSisterVersionsStatement.setString(1, paramListId);
            parameterListSisterVersionsStatement.setString(2, paramListVariantId);
            DataSet parameterListSisterVersions = new DataSet(parameterListSisterVersionsStatement.executeQuery());
            if (parameterListSisterVersions.getRowCount() == 1) {
                for (int j = 0; j < consumableTypeWithCurrentPL.getRowCount(); ++j) {
                    if (message.length() > 0) {
                        message.append(", ");
                    }
                    message.append("<br>").append(consumableTypeWithCurrentPL.getValue(j, "REAGENTTYPEID")).append(";").append(consumableTypeWithCurrentPL.getValue(j, "REAGENTTYPEVERSIONID"));
                }
            }
        }
    }

    private void checkLabWorkArea(DataSet primary) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        for (int indx = 0; indx < primary.size(); ++indx) {
            String labid = this.getColumnValue(primary, indx, "testingdepartmentid");
            String workarea = this.getColumnValue(primary, indx, "workareadepartmentid");
            if (!this.hasPrimaryValueChanged(primary, indx, "workareadepartmentid") && !this.hasPrimaryValueChanged(primary, indx, "testingdepartmentid") || workarea.length() <= 0 || labid.length() <= 0 || labid.equals(workarea)) continue;
            safeSQL.reset();
            sql.delete(0, sql.length());
            sql.append("select departmentid from department where departmentid = ").append(safeSQL.addVar(workarea)).append(" and parentdepartmentid=").append(safeSQL.addVar(labid));
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null && ds.size() != 0) continue;
            throw new SapphireException(this.getTranslationProcessor().translate("Work Area (" + workarea + ") is not in the Testing Lab (" + labid + ")."));
        }
    }

    private String getColumnValue(DataSet primary, int indx, String columnid) {
        String value = "";
        value = primary.isValidColumn(columnid) ? primary.getString(indx, columnid, "") : this.getOldPrimaryValue(primary, indx, columnid);
        return value;
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    @Override
    public boolean requiresBeforeEditDetailImage() {
        return true;
    }

    @Override
    public void postAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet paramlistitems = sdiData.getDataset("paramlistitem");
        if (OpalUtil.isNotEmpty(paramlistitems) && this.isCalcrulepresent(paramlistitems)) {
            String separator = actionProps.getProperty("separator", actionProps.getProperty("delimeter", ";"));
            DataSet primary = this.getPrimaryDataset(actionProps.getProperty("paramlistid"), actionProps.getProperty("paramlistversionid"), actionProps.getProperty("variantid"), separator);
            for (int i = 0; i < primary.size(); ++i) {
                if (!primary.getValue(i, "enableautoredocalcflag").equalsIgnoreCase("Y") || !OpalUtil.isNotEmpty(primary.getValue(i, "targetsdcid"))) continue;
                this.populateList(primary, i, this.paramlistds, this.paramlistversionids, this.variantids, this.targetsdcids);
            }
            if (this.needToSync()) {
                this.getActionProcessor().processAction("SyncCrossSDICalcInfoForParamList", "1", this.populateSyncActionProps(this.paramlistds, this.paramlistversionids, this.variantids, this.targetsdcids));
            }
            this.clearList();
        }
    }

    @Override
    public void postDeleteDetail(String rsetid, PropertyList actionProps) throws SapphireException {
        String separator = actionProps.getProperty("separator", actionProps.getProperty("delimeter", ";"));
        DataSet primary = this.getPrimaryDataset(actionProps.getProperty("paramlistid"), actionProps.getProperty("paramlistversionid"), actionProps.getProperty("variantid"), separator);
        for (int i = 0; i < primary.size(); ++i) {
            if (!primary.getValue(i, "enableautoredocalcflag").equalsIgnoreCase("Y") || !OpalUtil.isNotEmpty(primary.getValue(i, "targetsdcid"))) continue;
            this.populateList(primary, i, this.paramlistds, this.paramlistversionids, this.variantids, this.targetsdcids);
        }
        if (this.needToSync()) {
            this.getActionProcessor().processAction("SyncCrossSDICalcInfoForParamList", "1", this.populateSyncActionProps(this.paramlistds, this.paramlistversionids, this.variantids, this.targetsdcids));
        }
        this.clearList();
    }

    @Override
    public void postEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet paramlistitems = sdiData.getDataset("paramlistitem");
        if (OpalUtil.isNotEmpty(paramlistitems)) {
            String separator = actionProps.getProperty("separator", actionProps.getProperty("delimeter", ";"));
            DataSet primary = this.getPrimaryDataset(actionProps.getProperty("paramlistid"), actionProps.getProperty("paramlistversionid"), actionProps.getProperty("variantid"), separator);
            if (this.isCalcruleChanged(paramlistitems)) {
                for (int i = 0; i < primary.size(); ++i) {
                    if (!primary.getValue(i, "enableautoredocalcflag").equalsIgnoreCase("Y") || !OpalUtil.isNotEmpty(primary.getValue(i, "targetsdcid"))) continue;
                    this.populateList(primary, i, this.paramlistds, this.paramlistversionids, this.variantids, this.targetsdcids);
                }
                if (this.needToSync()) {
                    this.getActionProcessor().processAction("SyncCrossSDICalcInfoForParamList", "1", this.populateSyncActionProps(this.paramlistds, this.paramlistversionids, this.variantids, this.targetsdcids));
                }
                this.clearList();
            }
        }
    }

    private boolean isCalcruleChanged(String newCalcrule, String paramid) {
        boolean changed = false;
        DataSet oldParamlistItems = this.getBeforeEditImage().getDataset("paramlistitem");
        for (int i = 0; i < oldParamlistItems.size(); ++i) {
            if (!oldParamlistItems.getValue(i, "paramid").equalsIgnoreCase(paramid) || oldParamlistItems.getValue(i, "calcrule").equalsIgnoreCase(newCalcrule)) continue;
            changed = true;
            break;
        }
        return changed;
    }

    private boolean isCalcruleChanged(DataSet paramlistitems) {
        boolean changed = false;
        for (int i = 0; i < paramlistitems.size(); ++i) {
            if (!this.isCalcruleChanged(paramlistitems.getValue(i, "calcrule"), paramlistitems.getValue(i, "paramid"))) continue;
            changed = true;
            break;
        }
        return changed;
    }

    private boolean isCalcrulepresent(DataSet paramlistitems) {
        boolean present = false;
        block0: for (int i = 0; i < paramlistitems.size(); ++i) {
            String calcrules = paramlistitems.getValue(i, "calcrule");
            String[] calcrule = StringUtil.getExpressionTokens(calcrules);
            for (int len = 0; len < calcrule.length; ++len) {
                String[] tokenParts = StringUtil.split(calcrule[len], "|");
                if (tokenParts.length != 3) continue;
                present = true;
                continue block0;
            }
        }
        return present;
    }

    private DataSet getPrimaryDataset(String paramlistid, String paramlistversionid, String variantid, String separator) {
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid(SDC_PARAMLIST);
        sdiRequest.setRequestItem("primary");
        sdiRequest.setKeyid1List(StringUtil.split(paramlistid, separator)[0]);
        sdiRequest.setKeyid2List(StringUtil.split(paramlistversionid, separator)[0]);
        sdiRequest.setKeyid3List(StringUtil.split(variantid, separator)[0]);
        SDIData data = this.getSDIProcessor().getSDIData(sdiRequest);
        DataSet primary = data.getDataset("primary");
        return primary;
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, "enableautoredocalcflag") || !primary.getValue(i, "enableautoredocalcflag").equalsIgnoreCase("Y") || !OpalUtil.isNotEmpty(primary.getValue(i, "targetsdcid"))) continue;
            this.populateList(primary, i, this.paramlistds, this.paramlistversionids, this.variantids, this.targetsdcids);
        }
        if (this.needToSync()) {
            this.getActionProcessor().processAction("SyncCrossSDICalcInfoForParamList", "1", this.populateSyncActionProps(this.paramlistds, this.paramlistversionids, this.variantids, this.targetsdcids));
        }
        if (this.isCMTImport()) {
            this.handleCMT(sdiData);
        }
        this.clearList();
    }

    private boolean needToSync() {
        return OpalUtil.isNotEmpty(this.paramlistds) && OpalUtil.isNotEmpty(this.paramlistversionids) && OpalUtil.isNotEmpty(this.variantids) && OpalUtil.isNotEmpty(this.targetsdcids);
    }

    private void clearList() {
        this.paramlistds.clear();
        this.paramlistversionids.clear();
        this.variantids.clear();
        this.targetsdcids.clear();
    }

    private PropertyList populateSyncActionProps(List<String> paramlistds, List<String> paramlistversionids, List<String> variantids, List<String> targetsdcids) {
        PropertyList syncCrossProps = new PropertyList();
        syncCrossProps.setProperty("paramlistid", String.join((CharSequence)"~", paramlistds));
        syncCrossProps.setProperty("paramlistversionid", String.join((CharSequence)"~", paramlistversionids));
        syncCrossProps.setProperty("variantid", String.join((CharSequence)"~", variantids));
        syncCrossProps.setProperty("fromsdc", String.join((CharSequence)"~", targetsdcids));
        syncCrossProps.setProperty("separator", "~");
        return syncCrossProps;
    }

    private void populateList(DataSet primary, int rowcount, List<String> paramlistds, List<String> paramlistversionids, List<String> variantids, List<String> targetsdcids) {
        paramlistds.add(primary.getValue(rowcount, "paramlistid"));
        paramlistversionids.add(primary.getValue(rowcount, "paramlistversionid"));
        variantids.add(primary.getValue(rowcount, "variantid"));
        targetsdcids.add(primary.getValue(rowcount, "targetsdcid"));
    }
}

