/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.SDCExpiryValidationUtil;
import com.labvantage.sapphire.actions.sdidata.DataEntryLimitsUtil;
import com.labvantage.sapphire.actions.workitem.WorkItemUtil;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.util.MiscUtil;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.samplingplan.SamplingPlanUtil;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SpecSDC
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 99120 $";
    public static final String SDCID = "SpecSDC";
    public static final String SDC_KEYCOLID1 = "specid";
    public static final String SDC_KEYCOLID2 = "specversionid";
    public static final String SDC_KEYCOLID3 = "";
    public static final String COLUMN_EMBEDDEDFLAG = "embeddedflag";
    public static final String[] ROUNDING_FUNCTIONS = new String[]{"round", "astmround", "sigfig", "clpinorgsigfig", "clporgsigfig", "maxsigfigdp"};

    protected static void processLimitValue(String value, int row, DataSet data, int num, ConnectionInfo connectionInfo) throws SapphireException {
        StringBuffer value1 = new StringBuffer();
        StringBuffer value1num = new StringBuffer();
        try {
            M18NUtil m18n = new M18NUtil(connectionInfo);
            MiscUtil.MiscString.parseComplexNumber(value, value1num, value1, m18n, data.getLocale().equals(m18n.getLocale()));
            data.setValue(row, "value" + num + SDC_KEYCOLID3, value1.toString());
            data.setValue(row, "value" + num + "num", value1num.toString());
        }
        catch (NumberFormatException e) {
            throw new SapphireException("Number entered for value" + num + " invalid", e);
        }
    }

    private void manipulateLimits(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet data;
        if (actionProps.containsKey("value1") && actionProps.containsKey("value2") && (data = sdiData.getDataset("specparamlimits")) != null) {
            if (!data.isValidColumn("value1num")) {
                data.addColumn("value1num", 1);
            }
            if (!data.isValidColumn("value2num")) {
                data.addColumn("value2num", 1);
            }
            try {
                for (int row = 0; row < data.getRowCount(); ++row) {
                    String datatype = data.getValue(row, "datatypes", SDC_KEYCOLID3);
                    if (datatype.equalsIgnoreCase("N") && !data.getValue(row, "operator1", SDC_KEYCOLID3).equals("In") && !data.getValue(row, "operator1", SDC_KEYCOLID3).equals("Not In") || datatype.equalsIgnoreCase("NC")) {
                        SpecSDC.processLimitValue(data.getValue(row, "value1", SDC_KEYCOLID3), row, data, 1, this.getConnectionInfo());
                        SpecSDC.processLimitValue(data.getValue(row, "value2", SDC_KEYCOLID3), row, data, 2, this.getConnectionInfo());
                        continue;
                    }
                    if (datatype.equalsIgnoreCase("N") && (data.getValue(row, "operator1", SDC_KEYCOLID3).equals("In") || data.getValue(row, "operator1", SDC_KEYCOLID3).equals("Not In"))) {
                        String value1Str = data.getValue(row, "value1", SDC_KEYCOLID3);
                        String[] value1Arr = null;
                        value1Arr = value1Str.indexOf(";") != -1 ? StringUtil.split(value1Str, ";") : new String[]{value1Str};
                        StringBuffer value1 = new StringBuffer();
                        M18NUtil m18n = new M18NUtil(this.connectionInfo);
                        for (int i = 0; i < value1Arr.length; ++i) {
                            value1.append(";");
                            MiscUtil.MiscString.parseComplexNumber(value1Arr[i], new StringBuffer(), value1, m18n, data.getLocale().equals(m18n.getLocale()));
                        }
                        data.setValue(row, "value1", value1.toString().substring(1));
                        data.setValue(row, "value1num", SDC_KEYCOLID3);
                        continue;
                    }
                    if (datatype.equalsIgnoreCase("A")) {
                        String val1 = data.getValue(row, "value1", SDC_KEYCOLID3);
                        FormatUtil formatutil = FormatUtil.getInstance(this.connectionInfo);
                        try {
                            BigDecimal test = formatutil.parseBigDecimal(val1);
                            SpecSDC.processLimitValue(val1, row, data, 1, this.getConnectionInfo());
                        }
                        catch (NumberFormatException nfe) {
                            data.setValue(row, "value1num", SDC_KEYCOLID3);
                        }
                        String val2 = data.getValue(row, "value2", SDC_KEYCOLID3);
                        try {
                            BigDecimal test = formatutil.parseBigDecimal(val2);
                            SpecSDC.processLimitValue(val2, row, data, 2, this.getConnectionInfo());
                        }
                        catch (NumberFormatException nfe) {
                            data.setValue(row, "value2num", SDC_KEYCOLID3);
                        }
                        continue;
                    }
                    data.setValue(row, "value1num", SDC_KEYCOLID3);
                    data.setValue(row, "value2num", SDC_KEYCOLID3);
                }
            }
            catch (Exception e) {
                throw new SapphireException(this.getTranslationProcessor().translate(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId()))), e);
            }
        }
    }

    @Override
    public void preAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataEntryLimitsUtil limitsUtil = new DataEntryLimitsUtil(this.getSDCProcessor(), this.getTranslationProcessor(), this.database, this.logger, this.connectionInfo);
        limitsUtil.manipulateLimits("specparamlimits", sdiData, actionProps);
        this.populateDisplayFormatandTransformRule("specparamitems", sdiData, actionProps);
        this.updateDataType(sdiData, actionProps);
    }

    @Override
    public void preEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataEntryLimitsUtil limitsUtil = new DataEntryLimitsUtil(this.getSDCProcessor(), this.getTranslationProcessor(), this.database, this.logger, this.connectionInfo);
        limitsUtil.manipulateLimits("specparamlimits", sdiData, actionProps);
        this.populateDisplayFormatandTransformRule("specparamitems", sdiData, actionProps);
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (primary != null) {
            String isVersionProtectEnabled;
            PropertyList versionprotection;
            PropertyList paramListPolicy;
            boolean changed = false;
            for (int i = 0; i < primary.size(); ++i) {
                if (!this.hasPrimaryValueChanged(primary, i, "ruletypeflag")) continue;
                this.logger.info("Change in rule type to " + primary.getValue(i, "ruletypeflag") + " therefore clear rules...");
                String specid = primary.getString(i, SDC_KEYCOLID1);
                String specversionid = primary.getString(i, SDC_KEYCOLID2);
                this.database.executePreparedUpdate("DELETE specrulecondition WHERE specid=? AND specversionid=?", new String[]{specid, specversionid});
                this.database.executePreparedUpdate("DELETE specrule WHERE specid=? AND specversionid=?", new String[]{specid, specversionid});
                changed = true;
                this.logger.info("Rule type changed and rule data cleared for spec " + primary.getValue(i, SDC_KEYCOLID1, SDC_KEYCOLID3) + " (" + primary.getValue(i, SDC_KEYCOLID2, SDC_KEYCOLID3) + ")");
            }
            if (changed) {
                CacheUtil.clear(this.connectionInfo.getDatabaseId(), "SpecRules");
            }
            if ((paramListPolicy = this.getConfigurationProcessor().getPolicy("ParamListPolicy", "Sapphire Custom")) != null && (versionprotection = paramListPolicy.getPropertyListNotNull("expireddataprotection")) != null && versionprotection.size() > 0 && (isVersionProtectEnabled = versionprotection.getProperty(SDCID.toLowerCase(), SDC_KEYCOLID3)).equalsIgnoreCase("Y")) {
                this.doExpireVersionValidation(primary);
            }
        }
    }

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        String templateid = actionProps.getProperty("templateid");
        String templatekeyid1 = actionProps.getProperty("templatekeyid1");
        if (primary != null) {
            for (int i = 0; i < primary.size(); ++i) {
                String oosgeneratingflag = primary.getValue(i, "oosgeneratingflag");
                if (oosgeneratingflag.equals(SDC_KEYCOLID3)) {
                    primary.setString(i, "oosgeneratingflag", "Y");
                }
                String versionstatus = primary.getValue(i, "versionstatus");
                String versionapproveddt = primary.getValue(i, "versionapproveddt");
                if (templateid.length() <= 0 && templatekeyid1.length() <= 0 || versionstatus.equals("P")) continue;
                primary.setString(i, "versionstatus", "P");
                primary.setString(i, "versionapproveddt", null);
            }
        }
    }

    private void doExpireVersionValidation(DataSet primary) throws SapphireException {
        String queryWorkItemWithSpec = "SELECT wii.workitemid, wii.workitemversionid FROM workitem wi, workitemitem wii WHERE wii. sdcid = 'SpecSDC' AND wii.keyid1 = ? AND wii.keyid2 = ? AND wi.workitemid = wii.workitemid AND wi.workitemversionid = wii.workitemversionid AND wi.versionstatus <> 'E'";
        PreparedStatement workItemWithSpecStatement = this.database.prepareStatement("workItemWithSpec", queryWorkItemWithSpec);
        String queryWorkItemWithCurrentSpec = "SELECT wii.workitemid, wii.workitemversionid FROM workitem wi, workitemitem wii WHERE wii. sdcid = 'SpecSDC' AND wii.keyid1 = ? AND wii.keyid2 = 'C' AND wi.workitemid = wii.workitemid AND wi.workitemversionid = wii.workitemversionid AND wi.versionstatus <> 'E'";
        PreparedStatement workItemWithCurrentSpecStatement = this.database.prepareStatement("workItemWithCurrentSpec", queryWorkItemWithCurrentSpec);
        String querySpecSisterVersions = "SELECT specid FROM spec WHERE specid = ? AND versionstatus IN ( 'P', 'C' )";
        PreparedStatement specSisterVersionsStatement = this.database.prepareStatement("specSisterVersions", querySpecSisterVersions);
        try {
            StringBuffer message = new StringBuffer();
            for (int i = 0; i < primary.size(); ++i) {
                String actualVersion;
                String oldVersionStatus;
                if (!this.hasPrimaryValueChanged(primary, i, "versionstatus") || !"E".equals(primary.getValue(i, "versionstatus", SDC_KEYCOLID3)) || !"C".equals(oldVersionStatus = this.getOldPrimaryValue(primary, i, "versionstatus")) && !"P".equals(oldVersionStatus) && !"A".equals(oldVersionStatus)) continue;
                String specId = primary.getValue(i, SDC_KEYCOLID1, SDC_KEYCOLID3);
                String specVersionId = primary.getValue(i, SDC_KEYCOLID2, SDC_KEYCOLID3);
                LinkedHashMap<String, String> keyColsValues = new LinkedHashMap<String, String>();
                keyColsValues.put(SDC_KEYCOLID1, specId);
                keyColsValues.put(SDC_KEYCOLID2, specVersionId);
                LinkedHashMap<String, String> keyCols = new LinkedHashMap<String, String>();
                keyCols.put("keyid1", SDC_KEYCOLID1);
                keyCols.put("keyid2", SDC_KEYCOLID2);
                SDCExpiryValidationUtil.validateExpiry(SDCID, "LV_ReagentType", keyCols, keyColsValues, message, this.getSDCProcessor());
                if (keyColsValues.get(SDC_KEYCOLID2) == "C") {
                    actualVersion = SDCExpiryValidationUtil.getActualVersionId(SDCID, keyCols, keyColsValues, this.getSDCProcessor());
                    keyColsValues.put(SDC_KEYCOLID2, actualVersion);
                }
                SDCExpiryValidationUtil.validateExpiry(SDCID, "QCMethod", keyCols, keyColsValues, message, this.getSDCProcessor());
                if (keyColsValues.get(SDC_KEYCOLID2) == "C") {
                    actualVersion = SDCExpiryValidationUtil.getActualVersionId(SDCID, keyCols, keyColsValues, this.getSDCProcessor());
                    keyColsValues.put(SDC_KEYCOLID2, actualVersion);
                }
                SDCExpiryValidationUtil.validateExpiryWithRefTable(SDCID, "QCMethodSampleType", "QCMethod", keyCols, keyColsValues, message, this.getSDCProcessor());
                SDCExpiryValidationUtil.validateSamplingPlanReference(SDCID, keyCols, keyColsValues, message, this.getSDCProcessor());
                SDCExpiryValidationUtil.validateRequestTemplateReference(SDCID, keyCols, keyColsValues, message, this.getSDCProcessor());
                workItemWithSpecStatement.setString(1, specId);
                workItemWithSpecStatement.setString(2, specVersionId);
                DataSet workItemWithSpec = new DataSet(workItemWithSpecStatement.executeQuery());
                workItemWithCurrentSpecStatement.setString(1, specId);
                DataSet workItemWithCurrentSpec = new DataSet(workItemWithCurrentSpecStatement.executeQuery());
                if (workItemWithSpec.getRowCount() > 0) {
                    for (int j = 0; j < workItemWithSpec.getRowCount(); ++j) {
                        if (message.length() > 0) {
                            message.append(", ");
                        }
                        message.append("<br>").append(workItemWithSpec.getValue(j, "workitemid")).append(";").append(workItemWithSpec.getValue(j, "workitemversionid"));
                    }
                }
                if ("A".equals(oldVersionStatus) || workItemWithCurrentSpec.getRowCount() <= 0) continue;
                specSisterVersionsStatement.setString(1, specId);
                DataSet specSisterVersions = new DataSet(specSisterVersionsStatement.executeQuery());
                if (specSisterVersions.getRowCount() != 1) continue;
                for (int j = 0; j < workItemWithCurrentSpec.getRowCount(); ++j) {
                    if (message.length() > 0) {
                        message.append(", ");
                    }
                    message.append("TestMethod(s)").append("<br>").append(workItemWithCurrentSpec.getValue(j, "workitemid")).append(";").append(workItemWithCurrentSpec.getValue(j, "workitemversionid"));
                }
            }
            if (message.length() > 0) {
                String validationMessage = this.getTranslationProcessor().translate("Specification(s) cannot be 'Expired' because of references, direct or as 'Current', in the following ");
                validationMessage = validationMessage + message.toString() + ".";
                this.throwError("SpecificationUsed", "VALIDATION", validationMessage);
            }
            workItemWithCurrentSpecStatement.close();
            specSisterVersionsStatement.close();
        }
        catch (SQLException e) {
            throw new SapphireException(this.getTranslationProcessor().translate("Failed to retrieve TestMethod and Specification detail"));
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        this.checkSDISpecLinks(rsetid, this.getQueryProcessor(), this.getTranslationProcessor(), this.getActionProcessor(), this.getSDCProcessor());
        WorkItemUtil.deleteFromWorkItemItem(rsetid, actionProps, this.connectionInfo, this.getTranslationProcessor(), this.logger, SDCID, this.getQueryProcessor(), this.getActionProcessor());
        if (!"Y".equals(actionProps.getProperty("__sdcruleconfirm"))) {
            SamplingPlanUtil.checkSPItemLinks(SDCID, rsetid, this.getQueryProcessor(), this.getTranslationProcessor(), this.getActionProcessor(), this.getSDCProcessor());
            this.checkSpecLinks(rsetid, this.getQueryProcessor(), this.getTranslationProcessor(), this.getActionProcessor(), this.getSDCProcessor());
        }
    }

    @Override
    public void postDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        if ("Y".equals(actionProps.getProperty("__sdcruleconfirm"))) {
            SamplingPlanUtil.deleteSPItemLinks(SDCID, rsetid, this.getQueryProcessor(), this.getActionProcessor(), this.getSDCProcessor());
        }
    }

    @Override
    public void preDeleteDetail(String rsetid, PropertyList actionProps) throws SapphireException {
    }

    @Override
    public void postDeleteDetail(String rsetid, PropertyList actionProps) throws SapphireException {
        String linkId = actionProps.getProperty("linkid");
        if (!"Limit Types".equalsIgnoreCase(linkId) && "Param Limits".equalsIgnoreCase(linkId)) {
            String specid = StringUtil.split(actionProps.getProperty(SDC_KEYCOLID1, SDC_KEYCOLID3), actionProps.getProperty("separator", ";"))[0];
            String specversionid = StringUtil.split(actionProps.getProperty(SDC_KEYCOLID2, SDC_KEYCOLID3), actionProps.getProperty("separator", ";"))[0];
            this.updateLimitTypeSequence(specid, specversionid);
        }
        CacheUtil.clear(this.connectionInfo.getDatabaseId(), "SpecRules");
    }

    @Override
    public void postEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet ds = sdiData.getDataset("speclimittype");
        if (ds != null && ds.getRowCount() > 0) {
            try {
                this.updateLimitTypeSequence(sdiData);
            }
            catch (Exception e) {
                this.logger.info("Update of limittypesequence failed. May be due to edit running out of sequence(1).");
            }
        }
        CacheUtil.clear(this.connectionInfo.getDatabaseId(), "SpecRules");
    }

    @Override
    public void postAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet ds = sdiData.getDataset("specparamlimits");
        if (ds != null && ds.getRowCount() > 0) {
            try {
                this.updateLimitTypeSequence(sdiData);
            }
            catch (Exception e) {
                this.logger.info("Update of limittypesequence failed. May be due to add running before edit(1).");
            }
        }
        CacheUtil.clear(this.connectionInfo.getDatabaseId(), "SpecRules");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean doRowsMatch(String specid, String specversionid) throws SapphireException {
        int rows1 = 0;
        int rows2 = 0;
        try {
            DataSet test;
            String check = "SELECT COUNT(DISTINCT limittypesequence) FROM speclimittype WHERE specid=? AND specversionid=?";
            this.database.createPreparedResultSet("rows1", check, new Object[]{specid, specversionid});
            try {
                test = new DataSet(this.database.getResultSet("rows1"));
                if (test != null && test.getRowCount() > 0 && test.getColumnCount() > 0) {
                    try {
                        this.logger.debug("Obtaining rows1...");
                        rows1 = test.getBigDecimal(0, test.getColumnId(0), BigDecimal.ZERO).intValue();
                        this.logger.debug("rows1 obtained.");
                    }
                    catch (Exception e) {
                        this.logger.warn("Failed to obtain rows1.");
                        rows1 = 0;
                    }
                }
            }
            finally {
                this.database.closeStatement("rows1");
            }
            check = "SELECT COUNT(DISTINCT limittypesequence) FROM specparamlimits WHERE specid=? AND specversionid=?";
            this.database.createPreparedResultSet("rows2", check, new Object[]{specid, specversionid});
            try {
                test = new DataSet(this.database.getResultSet("rows2"));
                if (test != null && test.getRowCount() > 0 && test.getColumnCount() > 0) {
                    try {
                        this.logger.debug("Obtaining rows2...");
                        rows2 = test.getBigDecimal(0, test.getColumnId(0), BigDecimal.ZERO).intValue();
                        this.logger.debug("rows2 obtained.");
                    }
                    catch (Exception e) {
                        this.logger.warn("Failed to obtain rows2.");
                        rows2 = 0;
                    }
                }
            }
            finally {
                this.database.closeStatement("rows2");
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        return rows1 >= rows2;
    }

    private void updateLimitTypeSequence(SDIData sdiData) throws SapphireException {
        DataSet limitTypeDS = sdiData.getDataset("speclimittype");
        if (limitTypeDS != null) {
            String specIds = limitTypeDS.getColumnValues(SDC_KEYCOLID1, ";");
            String specVersionIds = limitTypeDS.getColumnValues(SDC_KEYCOLID2, ";");
            this.updateLimitTypeSequence(specIds, specVersionIds);
        } else {
            limitTypeDS = sdiData.getDataset("specparamlimits");
            String specIds = limitTypeDS.getColumnValues(SDC_KEYCOLID1, ";");
            String specVersionIds = limitTypeDS.getColumnValues(SDC_KEYCOLID2, ";");
            this.updateLimitTypeSequence(specIds, specVersionIds);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void updateLimitTypeSequence(String specid, String specversionid) throws SapphireException {
        if (specid != null && specid.length() > 0 && specversionid != null && specversionid.length() > 0) {
            String[] specversions;
            ArrayList<String> checkedSpecs = new ArrayList<String>();
            String[] specids = StringUtil.split(specid, ";");
            if (specids.length == (specversions = StringUtil.split(specversionid, ";")).length) {
                this.logger.info("updateLimitTypeSequence.");
                StringBuffer sql1 = new StringBuffer();
                sql1.append("UPDATE specparamlimits ").append("SET limittypesequence =  ").append("(").append(" SELECT usersequence FROM speclimittype slt ").append(" WHERE slt.specid = specparamlimits.specid ").append(" AND slt.specversionid = specparamlimits.specversionid ").append(" AND slt.limittypesequence = specparamlimits.limittypesequence ").append(")").append(" WHERE specparamlimits.specid = ? ").append(" AND specparamlimits.specversionid = ? ").append(" AND EXISTS ").append("(").append(" SELECT null FROM speclimittype slt ").append(" WHERE slt.specid = specparamlimits.specid ").append(" AND slt.specversionid = specparamlimits.specversionid").append(" AND slt.limittypesequence = specparamlimits.limittypesequence").append(")");
                StringBuffer sql2 = new StringBuffer();
                sql2.append(" UPDATE speclimittype ").append("SET limittypesequence = usersequence ").append(" WHERE specid = ?  AND specversionid = ?");
                if (specids.length == specversions.length) {
                    PreparedStatement update1 = this.database.prepareStatement("updatelimittypesequence1", sql1.toString());
                    PreparedStatement update2 = this.database.prepareStatement("updatelimittypesequence2", sql2.toString());
                    try {
                        for (int i = 0; i < specids.length; ++i) {
                            String specId = specids[i];
                            String specVersionId = specversions[i];
                            if (checkedSpecs.contains(specId + specVersionId)) continue;
                            checkedSpecs.add(specId + specVersionId);
                            if (!this.doRowsMatch(specId, specVersionId)) continue;
                            try {
                                update1.setString(1, specId);
                                update1.setString(2, specVersionId);
                                update1.executeUpdate();
                            }
                            catch (Exception e1) {
                                this.logger.error("Failed to run update 1 for spec " + specId, e1);
                            }
                            try {
                                update2.setString(1, specId);
                                update2.setString(2, specVersionId);
                                update2.executeUpdate();
                                continue;
                            }
                            catch (Exception e2) {
                                this.logger.error("Failed to run update 2 for spec " + specId, e2);
                            }
                        }
                    }
                    finally {
                        this.database.closeStatement("updatelimittypesequence1");
                        this.database.closeStatement("updatelimittypesequence2");
                    }
                }
            } else {
                this.logger.error("Could not update limittypesequence because spec and spec version arrays do not match.");
            }
        }
    }

    private void populateDisplayFormatandTransformRule(String dsName, SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet data;
        ArrayList<String> al = new ArrayList<String>();
        for (int i = 0; i < ROUNDING_FUNCTIONS.length; ++i) {
            al.add(ROUNDING_FUNCTIONS[i]);
        }
        if (actionProps.containsKey("roundingprecision") && actionProps.containsKey("roundingfunction") && (data = sdiData.getDataset(dsName)) != null) {
            for (int row = 0; row < data.getRowCount(); ++row) {
                String dispFormat = data.getValue(row, "displayformat", SDC_KEYCOLID3);
                String transformRule = data.getValue(row, "transformrule", SDC_KEYCOLID3);
                String roundFunction = data.getValue(row, "roundingfunction", SDC_KEYCOLID3);
                String roundPrecision = data.getValue(row, "roundingprecision");
                String paramId = data.getValue(row, "paramid");
                if (roundFunction.length() <= 0 || roundPrecision.length() <= 0) continue;
                if (!al.contains(roundFunction)) {
                    throw new SapphireException("Failed to generate display format and transform rule for " + paramId + ". Provided unsupported Rounding Function: " + roundFunction);
                }
                try {
                    String dispFormatValue = SDC_KEYCOLID3;
                    String transformRuleValue = SDC_KEYCOLID3;
                    int precision = Integer.parseInt(roundPrecision);
                    if ("round".equalsIgnoreCase(roundFunction)) {
                        dispFormatValue = "0";
                        for (int p = 0; p < precision; ++p) {
                            if (p == 0) {
                                dispFormatValue = dispFormatValue + ".";
                            }
                            dispFormatValue = dispFormatValue + '0';
                        }
                    } else {
                        dispFormatValue = "[" + roundFunction + ";" + roundPrecision + "]";
                    }
                    transformRuleValue = roundFunction + "( [this], " + roundPrecision + " )";
                    if (dispFormat.length() == 0) {
                        if (!data.isValidColumn("displayformat")) {
                            data.addColumn("displayformat", 0);
                        }
                        data.setValue(row, "displayformat", dispFormatValue);
                    }
                    if (transformRule.length() != 0) continue;
                    if (!data.isValidColumn("transformrule")) {
                        data.addColumn("transformrule", 0);
                    }
                    data.setValue(row, "transformrule", transformRuleValue);
                    continue;
                }
                catch (Exception n) {
                    throw new SapphireException("Failed to generate display format and transform rule for " + paramId + ". Provided invalid Rounding Precision: " + roundPrecision);
                }
            }
        }
    }

    private void checkSpecLinks(String rsetId, QueryProcessor qp, TranslationProcessor tp, ActionProcessor ap, SDCProcessor sdcProcessor) throws SapphireException {
        String sdcId = SDCID;
        String[] fromSDC = new String[]{"LV_ReagentType", "QCMethod", "QCMethodSampleType"};
        for (int s = 0; s < fromSDC.length; ++s) {
            String fromsdcid = fromSDC[s];
            DataSet ds = SpecSDC.getSpecLinks(qp, fromsdcid, rsetId);
            if (ds.getRowCount() <= 0) continue;
            String tableid = sdcProcessor.getProperty(sdcId, "tableid");
            String keycolid1 = sdcProcessor.getProperty(sdcId, "keycolid1");
            String keycolid2 = sdcProcessor.getProperty(sdcId, "keycolid2");
            DataSet dsRefences = new DataSet();
            dsRefences.addColumn("refkeyid1", 0);
            for (int i = 0; i < ds.getRowCount(); ++i) {
                DataSet dsVers;
                String itemId = ds.getValue(i, "keyid1");
                String itemVerId = ds.getValue(i, "keyid2");
                String spVerId = ds.getValue(i, "spversionid", "C");
                String refkeyid1 = ds.getValue(i, "refkeyid1");
                if (itemVerId.equals(spVerId)) {
                    this.addToDS(dsRefences, refkeyid1);
                    continue;
                }
                if (!"C".equalsIgnoreCase(spVerId) || (dsVers = qp.getPreparedSqlDataSet("SELECT " + keycolid1 + " itemid, " + keycolid2 + " itemversion FROM " + tableid + "  WHERE " + keycolid1 + " = ? and ( versionstatus='P' or versionstatus='C' ) order by versionstatus, cast (" + keycolid2 + " as numeric) desc", (Object[])new String[]{itemId})).getRowCount() <= 0 || !itemVerId.equals(dsVers.getValue(0, "itemversion"))) continue;
                if (dsVers.getRowCount() < 2) {
                    this.addToDS(dsRefences, refkeyid1);
                    continue;
                }
                int toDeleteVersions = 0;
                HashMap<String, String> findmap = new HashMap<String, String>();
                findmap.put("keyid1", itemId);
                for (int k = 1; k < dsVers.getRowCount(); ++k) {
                    findmap.put("keyid2", dsVers.getValue(k, "itemversion"));
                    if (ds.findRow(findmap) <= -1) continue;
                    ++toDeleteVersions;
                }
                if (toDeleteVersions != dsVers.getRowCount() - 1) continue;
                this.addToDS(dsRefences, refkeyid1);
            }
            if (dsRefences.getRowCount() <= 0) continue;
            String refkeyid1 = dsRefences.getColumnValues("refkeyid1", ",");
            PropertyList sdcProps = sdcProcessor.getPropertyList(sdcId);
            PropertyList fromsdcProps = sdcProcessor.getPropertyList(fromsdcid);
            StringBuffer sb = new StringBuffer();
            sb.append("{{Delete not allowed for}} ").append(sdcProps.getProperty("plural")).append(".");
            sb.append(" {{Child records found in}} ").append(fromsdcProps.getProperty("plural"));
            sb.append(" (").append(refkeyid1).append(")");
            this.setError(this.getTranslationProcessor().translate("Delete not allowed"), "VALIDATION", this.getTranslationProcessor().translatePartial(sb.toString()));
        }
    }

    private void checkSDISpecLinks(String rsetId, QueryProcessor qp, TranslationProcessor tp, ActionProcessor ap, SDCProcessor sdcProcessor) throws SapphireException {
        String sdcId = SDCID;
        DataSet ds = SpecSDC.getSpecLinks(qp, "SDISpec", rsetId);
        if (ds.getRowCount() > 0) {
            String tableid = sdcProcessor.getProperty(sdcId, "tableid");
            String keycolid1 = sdcProcessor.getProperty(sdcId, "keycolid1");
            String keycolid2 = sdcProcessor.getProperty(sdcId, "keycolid2");
            DataSet dsRefences = new DataSet();
            dsRefences.addColumn("sdcid", 0);
            dsRefences.addColumn("keyid1", 0);
            for (int i = 0; i < ds.getRowCount(); ++i) {
                DataSet dsVers;
                String itemId = ds.getValue(i, "keyid1");
                String itemVerId = ds.getValue(i, "keyid2");
                String spVerId = ds.getValue(i, "spversionid", "C");
                String refsdcid = ds.getValue(i, "sdcid");
                String sdispeckeyid1 = ds.getValue(i, "sdispeckeyid1");
                if (itemVerId.equals(spVerId)) {
                    this.addToDS(dsRefences, refsdcid, sdispeckeyid1);
                    continue;
                }
                if (!"C".equalsIgnoreCase(spVerId) || (dsVers = qp.getPreparedSqlDataSet("SELECT " + keycolid1 + " itemid, " + keycolid2 + " itemversion FROM " + tableid + "  WHERE " + keycolid1 + " = ? and ( versionstatus='P' or versionstatus='C' ) order by versionstatus, cast (" + keycolid2 + " as numeric) desc", (Object[])new String[]{itemId})).getRowCount() <= 0 || !itemVerId.equals(dsVers.getValue(0, "itemversion"))) continue;
                if (dsVers.getRowCount() < 2) {
                    this.addToDS(dsRefences, refsdcid, sdispeckeyid1);
                    continue;
                }
                int toDeleteVersions = 0;
                HashMap<String, String> findmap = new HashMap<String, String>();
                findmap.put("keyid1", itemId);
                for (int k = 1; k < dsVers.getRowCount(); ++k) {
                    findmap.put("keyid2", dsVers.getValue(k, "itemversion"));
                    if (ds.findRow(findmap) <= -1) continue;
                    ++toDeleteVersions;
                }
                if (toDeleteVersions != dsVers.getRowCount() - 1) continue;
                this.addToDS(dsRefences, refsdcid, sdispeckeyid1);
            }
            if (dsRefences.getRowCount() > 0) {
                StringBuffer refs = new StringBuffer();
                for (int i = 0; i < 10 && i < dsRefences.size(); ++i) {
                    refs.append("<br/>").append(dsRefences.getString(i, "sdcid")).append(": ").append(dsRefences.getString(i, "keyid1"));
                }
                if (refs.length() > 0) {
                    boolean more = dsRefences.size() >= 10;
                    this.throwError("SpecificationUsed", "VALIDATION", "Specification(s) cannot be deleted because of " + (more ? "at least" : SDC_KEYCOLID3) + " the following references:" + refs + (more ? "<br/>..." : SDC_KEYCOLID3));
                }
            }
        }
    }

    private void addToDS(DataSet dsRefences, String refkeyid1) {
        int r = dsRefences.addRow();
        dsRefences.setString(r, "refkeyid1", refkeyid1);
    }

    private void addToDS(DataSet dsRefences, String sdcid, String keyid1) {
        int r = dsRefences.addRow();
        dsRefences.setString(r, "sdcid", sdcid);
        dsRefences.setString(r, "keyid1", keyid1);
    }

    private static DataSet getSpecLinks(QueryProcessor qp, String fromsdcId, String rsetId) {
        DataSet ds = new DataSet();
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        if (fromsdcId.equalsIgnoreCase("LV_ReagentType")) {
            sql.append("SELECT DISTINCT r.keyid1, r.keyid2, rt.specversionid spversionid, rt.reagenttypeid refkeyid1 ");
            sql.append(" FROM rsetitems r, reagenttype rt");
            sql.append(" WHERE r.rsetid = " + safeSQL.addVar(rsetId) + " AND rt.specid = r.keyid1");
        } else if (fromsdcId.equalsIgnoreCase("QCMethod")) {
            sql.append("SELECT DISTINCT r.keyid1, r.keyid2, qm.specversionid spversionid, qm.s_qcmethodid refkeyid1 ");
            sql.append(" FROM rsetitems r, s_qcmethod qm");
            sql.append(" WHERE r.rsetid = " + safeSQL.addVar(rsetId) + " AND qm.specid = r.keyid1");
        } else if (fromsdcId.equalsIgnoreCase("QCMethodSampleType")) {
            sql.append("SELECT DISTINCT r.keyid1, r.keyid2, qmst.specversionid spversionid, qmst.s_qcmethodsampletypeid refkeyid1 ");
            sql.append(" FROM rsetitems r, s_qcmethodsampletype qmst");
            sql.append(" WHERE r.rsetid = " + safeSQL.addVar(rsetId) + " AND qmst.specid = r.keyid1");
        } else if (fromsdcId.equalsIgnoreCase("SDISpec")) {
            sql.append("SELECT sdispec.sdcid,sdispec.keyid1 sdispeckeyid1,rsetitems.keyid1, rsetitems.keyid2,sdispec.specversionid spversionid");
            sql.append(" FROM   sdispec, rsetitems");
            sql.append(" WHERE  rsetitems.rsetid = " + safeSQL.addVar(rsetId));
            sql.append(" AND    rsetitems.sdcid = 'SpecSDC'");
            sql.append(" AND    sdispec.specid = rsetitems.keyid1");
            sql.append(" ORDER BY 1, 2, 3, 4");
        }
        if (sql.length() > 0) {
            ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        }
        return ds;
    }

    private void updateDataType(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet splimitsdata;
        DataSet data = sdiData.getDataset("specparamitems");
        if (data != null && actionProps.getProperty("datatypes", SDC_KEYCOLID3).length() == 0) {
            for (int i = 0; i < data.size(); ++i) {
                this.database.createPreparedResultSet("specparamitemsdatatypes", " select datatypes from paramlistitem where paramlistid = ? and paramlistversionid = ? and variantid = ? and paramid = ? and paramtype = ?", new Object[]{data.getValue(i, "paramlistid"), data.getValue(i, "paramlistversionid"), data.getValue(i, "variantid"), data.getValue(i, "paramid"), data.getValue(i, "paramtype")});
                this.database.getNext("specparamitemsdatatypes");
                if (!data.isValidColumn("datatypes")) {
                    data.addColumn("datatypes", 0);
                }
                if (null == this.database.getValue("specparamitemsdatatypes", "datatypes")) continue;
                data.setValue(i, "datatypes", this.database.getValue("specparamitemsdatatypes", "datatypes"));
            }
        }
        if ((splimitsdata = sdiData.getDataset("specparamlimits")) != null && actionProps.getProperty("datatypes").length() == 0) {
            for (int i = 0; i < splimitsdata.size(); ++i) {
                this.database.createPreparedResultSet("specparamlimitsdatatypes", " select datatypes from paramlistitem where paramlistid = ? and paramlistversionid = ? and variantid = ? and paramid = ? and paramtype = ?", new Object[]{splimitsdata.getValue(i, "paramlistid"), splimitsdata.getValue(i, "paramlistversionid"), splimitsdata.getValue(i, "variantid"), splimitsdata.getValue(i, "paramid"), splimitsdata.getValue(i, "paramtype")});
                this.database.getNext("specparamlimitsdatatypes");
                if (!splimitsdata.isValidColumn("datatypes")) {
                    splimitsdata.addColumn("datatypes", 0);
                }
                if (null == this.database.getValue("specparamlimitsdatatypes", "datatypes")) continue;
                splimitsdata.setValue(i, "datatypes", this.database.getValue("specparamlimitsdatatypes", "datatypes"));
            }
        }
    }

    private void validateExpiry(String sdcid, String linkSDC, String linkSDC2, HashMap<String, String> keyCols, StringBuffer message) {
        DataSet linkds;
        HashMap errors = new HashMap();
        PropertyList sdcPropertyList = this.getSDCProcessor().getPropertyList(sdcid);
        String tableid = sdcPropertyList.getProperty("tableid");
        String keycolid1 = sdcPropertyList.getProperty("keycolid1");
        String keycolid2 = sdcPropertyList.getProperty("keycolid2");
        String keycolid3 = sdcPropertyList.getProperty("keycolid3");
        HashMap<String, String> detailLinkProps = new HashMap<String, String>();
        String keyid1 = keyCols.get(keycolid1);
        String keyid2 = keyCols.get(keycolid2);
        String keyid3 = keyCols.get(keycolid3);
        boolean isLink = this.checkLinkData(SDCID, linkSDC, detailLinkProps);
        if (isLink) {
            DataSet linkds2 = this.getLinkData(linkSDC, keyid1, keyid2, keyid3, "E");
            if (linkds2 != null && !linkds2.isEmpty()) {
                errors = this.getErrorCollection(linkSDC, keycolid1, keycolid2, keycolid3, linkds2);
            }
            this.addToErrorMessage(linkSDC, message, errors);
        } else {
            isLink = this.checkDetailLinkData(SDCID, linkSDC, detailLinkProps);
            if (isLink) {
                HashMap<String, ArrayList<String>> errorcol = new HashMap<String, ArrayList<String>>();
                linkds = this.getDetailLinkDataError(SDCID, keyid1, keyid2, keyid3, detailLinkProps, errorcol);
                this.addToErrorMessage(linkSDC, message, errors);
            }
        }
        boolean isReverseLink = this.checkLinkData(linkSDC, SDCID, detailLinkProps);
        if (isReverseLink) {
            if (OpalUtil.isNotEmpty(linkSDC2) && this.checkLinkData(linkSDC, linkSDC2, detailLinkProps = new HashMap())) {
                String abc = SDC_KEYCOLID3;
                PropertyList childSdcPropertyList = this.getSDCProcessor().getPropertyList(linkSDC);
                String childtableid = childSdcPropertyList.getProperty("tableid");
                String childkeycolid1 = childSdcPropertyList.getProperty("keycolid1");
                String childkeycolid2 = childSdcPropertyList.getProperty("keycolid2");
                String childkeycolid3 = childSdcPropertyList.getProperty("keycolid3");
                String linksdcid = detailLinkProps.get("linksdcid").toString();
                String linktableid = detailLinkProps.get("linktableid") != null ? detailLinkProps.get("linktableid").toString() : SDC_KEYCOLID3;
                String sdccolumnid = detailLinkProps.get("sdccolumnid") != null ? detailLinkProps.get("sdccolumnid").toString() : SDC_KEYCOLID3;
                String sdccolumnid2 = detailLinkProps.get("sdccolumnid2") != null ? detailLinkProps.get("sdccolumnid2").toString() : SDC_KEYCOLID3;
                String sdccolumnid3 = detailLinkProps.get("sdccolumnid3") != null ? detailLinkProps.get("sdccolumnid3").toString() : SDC_KEYCOLID3;
                StringBuffer sql = new StringBuffer();
                sql.append("SELECT ").append(sdccolumnid).append(OpalUtil.isNotEmpty(sdccolumnid2) ? "," + keycolid2 : SDC_KEYCOLID3).append(OpalUtil.isNotEmpty(sdccolumnid3) ? "," + keycolid3 : SDC_KEYCOLID3).append(" From " + childtableid).append(" WHERE ").append(SDC_KEYCOLID1).append("= '").append(SafeSQL.encodeForSQL(keyid1, this.getConnectionProcessor().isOra())).append("' AND ").append(SDC_KEYCOLID2).append("= '").append(SafeSQL.encodeForSQL(keyid2, this.getConnectionProcessor().isOra())).append("' AND ").append(SDC_KEYCOLID3).append("= '").append(SafeSQL.encodeForSQL(keyid3, this.getConnectionProcessor().isOra())).append("'");
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[0]);
                DataSet linkedDs = null;
                if (ds.size() > 0) {
                    ArrayList error = new ArrayList();
                    for (int l = 0; l < ds.size(); ++l) {
                        String queryWhere = "VERSIONSTATUS NOT IN ('E','H')";
                        linkedDs = this.fetchLinkedSDI(linkSDC2, ds.getValue(l, sdccolumnid), ds.getValue(l, sdccolumnid2), ds.getValue(l, sdccolumnid3), "primary", null, queryWhere);
                        if (linkedDs == null) continue;
                        error = this.getErrorMsg(sdccolumnid, sdccolumnid2, sdccolumnid3, linkedDs);
                    }
                }
            }
            if ((linkds = this.getLinkData(linkSDC, keyid1, keyid2, keyid3, "E")) != null && !linkds.isEmpty()) {
                errors = this.getErrorCollection(linkSDC, keycolid1, keycolid2, keycolid3, linkds);
            }
            this.addToErrorMessage(linkSDC, message, errors);
        } else {
            isReverseLink = this.checkDetailLinkData(linkSDC, SDCID, detailLinkProps);
            if (isReverseLink) {
                HashMap error = this.getDetailLinkData2(linkSDC, keyid1, keyid2, keyid3, detailLinkProps);
                this.addToErrorMessage(linkSDC, message, error);
            }
        }
    }

    private void fetchlinksdc1(String sdcid, String linksdcid1, String linksdcid2) {
    }

    private DataSet getLinkData(String linkSDC, String keyid1, String keyid2, String keyid3, String versionstatus) {
        PropertyList sdcPropertyList = this.getSDCProcessor().getPropertyList(linkSDC);
        String tableid = sdcPropertyList.getProperty("tableid");
        String keycolid1 = sdcPropertyList.getProperty("keycolid1");
        String keycolid2 = sdcPropertyList.getProperty("keycolid2");
        String keycolid3 = sdcPropertyList.getProperty("keycolid3");
        PropertyListCollection c = this.getSDCProcessor().getPropertyList(SDCID).getCollection("columns");
        c.find("columnid", "versionstatus");
        StringBuffer sqlwhere = new StringBuffer();
        sqlwhere.append(SDC_KEYCOLID1).append("= '").append(SafeSQL.encodeForSQL(keyid1, this.getConnectionProcessor().isOra())).append("'");
        if (OpalUtil.isNotEmpty(keyid2)) {
            sqlwhere.append(" AND ").append(SDC_KEYCOLID2).append("= '").append(SafeSQL.encodeForSQL(keyid2, this.getConnectionProcessor().isOra())).append("'");
        }
        if (OpalUtil.isNotEmpty(keyid3)) {
            sqlwhere.append(" AND ").append(SDC_KEYCOLID3).append("= '").append(SafeSQL.encodeForSQL(keyid2, this.getConnectionProcessor().isOra())).append("'");
        }
        if (OpalUtil.isNotEmpty(versionstatus)) {
            sqlwhere.append(" AND VERSIONSTATUS <> '").append(SafeSQL.encodeForSQL(versionstatus, this.getConnectionProcessor().isOra())).append("'");
        }
        DataSet linkedDs = this.fetchLinkedSDI(linkSDC, SDC_KEYCOLID3, SDC_KEYCOLID3, SDC_KEYCOLID3, "primary", tableid, sqlwhere.toString());
        return linkedDs;
    }

    private DataSet getDetailLinkDataError(String sdcid, String keyid1, String keyid2, String keyid3, HashMap detailLinkData, HashMap<String, ArrayList<String>> errorcol) {
        String linksdcid = detailLinkData.get("linksdcid").toString();
        String linktableid = detailLinkData.get("linktableid") != null ? detailLinkData.get("linktableid").toString() : SDC_KEYCOLID3;
        String sdccolumnid = detailLinkData.get("sdccolumnid") != null ? detailLinkData.get("sdccolumnid").toString() : SDC_KEYCOLID3;
        String sdccolumnid2 = detailLinkData.get("sdccolumnid2") != null ? detailLinkData.get("sdccolumnid2").toString() : SDC_KEYCOLID3;
        String sdccolumnid3 = detailLinkData.get("sdccolumnid3") != null ? detailLinkData.get("sdccolumnid3").toString() : SDC_KEYCOLID3;
        DataSet ds = this.fetchLinkedSDI(sdcid, keyid1, keyid2, keyid3, detailLinkData.get("linktableid").toString(), null, null);
        DataSet linkedDs = null;
        if (ds.size() > 0) {
            ArrayList error = new ArrayList();
            for (int l = 0; l < ds.size(); ++l) {
                String queryWhere = "VERSIONSTATUS NOT IN ('E','H')";
                linkedDs = this.fetchLinkedSDI(linksdcid, ds.getValue(l, sdccolumnid), ds.getValue(l, sdccolumnid2), ds.getValue(l, sdccolumnid3), "primary", null, queryWhere);
                if (linkedDs == null) continue;
                error = this.getErrorMsg(sdccolumnid, sdccolumnid2, sdccolumnid3, linkedDs);
                errorcol.put(linksdcid, error);
            }
        }
        return linkedDs;
    }

    private HashMap getErrorCollection(String linkSDC, String keycolid1, String keycolid2, String keycolid3, DataSet linkedDs) {
        HashMap<String, ArrayList> errorcol = new HashMap<String, ArrayList>();
        ArrayList error = this.getErrorMsg(keycolid1, keycolid2, keycolid3, linkedDs);
        errorcol.put(linkSDC, error);
        return errorcol;
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

    private boolean checkLinkData(String SDC2, String toCheckSDC, HashMap<String, String> keycols) {
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
                if (link.get("linktableid") != null) {
                    keycols.put("linktableid", (String)link.get("linktableid"));
                }
                if (link.get("linkid") != null) {
                    keycols.put("linkid", (String)link.get("linkid"));
                }
                if (link.get("linksdcid") == null) break;
                keycols.put("linksdcid", (String)link.get("sdccolumnid3"));
                break;
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

    private ArrayList getErrorMsg(String sdccolumnid, String sdccolumnid2, String sdccolumnid3, DataSet linkedDs) {
        ArrayList<String> error = new ArrayList<String>();
        for (int k = 0; k < linkedDs.size(); ++k) {
            String versionStatus;
            StringBuffer errMsg = new StringBuffer();
            errMsg.append(linkedDs.getString(k, sdccolumnid));
            if (linkedDs.getString(k, sdccolumnid2) != null) {
                errMsg.append(";").append(linkedDs.getString(k, sdccolumnid2));
            }
            if (linkedDs.getString(k, sdccolumnid3) != null) {
                errMsg.append(";").append(linkedDs.getString(k, sdccolumnid3));
            }
            errMsg.append("- Version Status : ");
            switch (versionStatus = linkedDs.getString(k, "VERSIONSTATUS")) {
                case "C": {
                    errMsg.append("Current");
                    break;
                }
                case "A": {
                    errMsg.append("Active");
                    break;
                }
                case "P": {
                    errMsg.append("Provisional");
                    break;
                }
                default: {
                    errMsg.append("(null)");
                }
            }
            error.add(errMsg.toString());
        }
        return error;
    }

    private HashMap getDetailLinkData2(String sdcid, String keyid1, String keyid2, String keyid3, HashMap detailLinkData) {
        HashMap errorcol = new HashMap();
        String linksdcid = detailLinkData.get("linksdcid").toString();
        String linktableid = detailLinkData.get("linktableid") != null ? detailLinkData.get("linktableid").toString() : SDC_KEYCOLID3;
        String sdccolumnid = detailLinkData.get("sdccolumnid") != null ? detailLinkData.get("sdccolumnid").toString() : SDC_KEYCOLID3;
        String sdccolumnid2 = detailLinkData.get("sdccolumnid2") != null ? detailLinkData.get("sdccolumnid2").toString() : SDC_KEYCOLID3;
        String sdccolumnid3 = detailLinkData.get("sdccolumnid3") != null ? detailLinkData.get("sdccolumnid3").toString() : SDC_KEYCOLID3;
        PropertyList sdcPropertyList = this.getSDCProcessor().getPropertyList(sdcid);
        String tableid = sdcPropertyList.getProperty("tableid");
        String keycolid1 = sdcPropertyList.getProperty("keycolid1");
        String keycolid2 = sdcPropertyList.getProperty("keycolid2");
        String keycolid3 = sdcPropertyList.getProperty("keycolid3");
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT ").append(keycolid1).append(OpalUtil.isNotEmpty(keycolid2) ? "," + keycolid2 : SDC_KEYCOLID3).append(OpalUtil.isNotEmpty(keycolid3) ? "," + keycolid3 : SDC_KEYCOLID3).append(" From " + linktableid).append(" WHERE ").append(SDC_KEYCOLID1).append("= '").append(SafeSQL.encodeForSQL(keyid1, this.getConnectionProcessor().isOra())).append("' AND ").append(SDC_KEYCOLID2).append("= '").append(SafeSQL.encodeForSQL(keyid2, this.getConnectionProcessor().isOra())).append("' AND ").append(SDC_KEYCOLID3).append("= '").append(SafeSQL.encodeForSQL(keyid3, this.getConnectionProcessor().isOra())).append("'");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[0]);
        if (ds != null && !ds.isEmpty()) {
            for (int l = 0; l < ds.size(); ++l) {
                String queryWhere = "VERSIONSTATUS NOT IN ('E','H')";
                DataSet linkedDs = this.fetchLinkedSDI(sdcid, ds.getValue(l, keycolid1), ds.getValue(l, keycolid2), ds.getValue(l, keycolid3), "primary", null, queryWhere);
                if (linkedDs == null) continue;
                this.getErrorCollection(sdcid, keycolid1, keycolid2, keycolid3, linkedDs);
            }
        }
        return errorcol;
    }
}

