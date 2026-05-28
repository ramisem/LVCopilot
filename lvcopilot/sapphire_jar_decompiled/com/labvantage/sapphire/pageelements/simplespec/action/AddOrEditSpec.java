/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.simplespec.action;

import com.labvantage.sapphire.admin.system.SQLRegister;
import com.labvantage.sapphire.gwt.shared.util.StringUtil;
import com.labvantage.sapphire.pageelements.simplespec.action.AutomaticVersioning;
import com.labvantage.sapphire.pageelements.simplespec.action.GetNewSpecId;
import com.labvantage.sapphire.pageelements.simplespec.action.SpecHelper;
import com.labvantage.sapphire.util.MiscUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.ext.BaseSQLRegister;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AddOrEditSpec
extends BaseAction {
    public static final String PROPERTY_SDCID = "sdcid";
    public static final String PROPERTY_KEYID1 = "keyid1";
    public static final String PROPERTY_KEYID2 = "keyid2";
    public static final String PROPERTY_KEYID3 = "keyid3";
    public static final String PROPERTY_PARAMLISTID = "paramlistid";
    public static final String PROPERTY_PARAMLISTVERSIONID = "paramlistversionid";
    public static final String PROPERTY_PARAMID = "paramid";
    public static final String PROPERTY_VARIANTID = "variantid";
    public static final String PROPERTY_PARAMTYPE = "paramtype";
    public static final String PROPERTY_DATATYPES = "datatypes";
    public static final String PROPERTY_HIGHLIMIT3 = "highlimit3";
    public static final String PROPERTY_HIGHLIMIT2 = "highlimit2";
    public static final String PROPERTY_HIGHLIMIT1 = "highlimit1";
    public static final String PROPERTY_TARGET = "target";
    public static final String PROPERTY_LOWLIMIT1 = "lowlimit1";
    public static final String PROPERTY_LOWLIMIT2 = "lowlimit2";
    public static final String PROPERTY_LOWLIMIT3 = "lowlimit3";
    public static final String PROPERTY_AUTOMATICVERSIONING = "automaticversioning";
    public static final String PROPERTY_SPECIDCOLUMN = "specidcolumn";
    public static final String PROPERTY_SPECVERSIONIDCOLUMN = "specversionidcolumn";
    public static final String PROPERTY_HIGHLIMIT3TYPE = "highlimit3type";
    public static final String PROPERTY_HIGHLIMIT2TYPE = "highlimit2type";
    public static final String PROPERTY_HIGHLIMIT1TYPE = "highlimit1type";
    public static final String PROPERTY_INSPECTYPE = "inspectype";
    public static final String PROPERTY_TARGETTYPE = "targettype";
    public static final String PROPERTY_LOWLIMIT1TYPE = "lowlimit1type";
    public static final String PROPERTY_LOWLIMIT2TYPE = "lowlimit2type";
    public static final String PROPERTY_LOWLIMIT3TYPE = "lowlimit3type";
    public static final List<String> BLOCKED_COLUMNS_LIST = new ArrayList<String>();
    private static final int UPDATE_SPEC_PARAM_ITEMS_PARAM_LIST_VERSION_ID_SQL_CODE = 1;
    private static final int UPDATE_SPEC_PARAM_LIMITS_PARAM_LIST_VERSION_ID_SQL_CODE = 2;
    private static final int DELETE_SPEC_PARAM_ITEMS_PARAM_LIST_VERSION_ID_SQL_CODE = 3;
    private static final int DELETE_SPEC_PARAM_LIMITS_PARAM_LIST_VERSION_ID_SQL_CODE = 4;
    private static final BaseSQLRegister ADD_OR_EDIT_SPEC_SQL_REGISTER;
    private List<String> paramListIdList;
    private List<String> paramListVersionIdList;
    private List<String> variantIdList;
    private List<String> paramIdList;
    private List<String> paramTypeList;
    private List<String> dataTypesList;
    private List<String> highLimit3List;
    private List<String> highLimit2List;
    private List<String> highLimit1List;
    private List<String> targetList;
    private List<String> lowLimit1List;
    private List<String> lowLimit2List;
    private List<String> lowLimit3List;
    private PropertyListCollection customColumnCollection;

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcId = properties.getProperty(PROPERTY_SDCID);
        String keyId1 = properties.getProperty(PROPERTY_KEYID1);
        String keyId2 = properties.getProperty(PROPERTY_KEYID2);
        String keyId3 = properties.getProperty(PROPERTY_KEYID3);
        String paramListIds = properties.getProperty(PROPERTY_PARAMLISTID);
        String paramListVersionIds = properties.getProperty(PROPERTY_PARAMLISTVERSIONID);
        String variantIds = properties.getProperty(PROPERTY_VARIANTID);
        String paramIds = properties.getProperty(PROPERTY_PARAMID);
        String paramTypes = properties.getProperty(PROPERTY_PARAMTYPE);
        String dataTypes = properties.getProperty(PROPERTY_DATATYPES);
        String highLimit3 = properties.getProperty(PROPERTY_HIGHLIMIT3);
        String highLimit2 = properties.getProperty(PROPERTY_HIGHLIMIT2);
        String highLimit1 = properties.getProperty(PROPERTY_HIGHLIMIT1);
        String target = properties.getProperty(PROPERTY_TARGET);
        String lowLimit1 = properties.getProperty(PROPERTY_LOWLIMIT1);
        String lowLimit2 = properties.getProperty(PROPERTY_LOWLIMIT2);
        String lowLimit3 = properties.getProperty(PROPERTY_LOWLIMIT3);
        AutomaticVersioning automaticVersioning = AutomaticVersioning.fromString(properties.getProperty(PROPERTY_AUTOMATICVERSIONING));
        String specIdColumn = properties.getProperty(PROPERTY_SPECIDCOLUMN);
        String specVersionIdColumn = properties.getProperty(PROPERTY_SPECVERSIONIDCOLUMN);
        String highLimit3Type = properties.getProperty(PROPERTY_HIGHLIMIT3TYPE);
        String highLimit2Type = properties.getProperty(PROPERTY_HIGHLIMIT2TYPE);
        String highLimit1Type = properties.getProperty(PROPERTY_HIGHLIMIT1TYPE);
        String inSpecType = properties.getProperty(PROPERTY_INSPECTYPE);
        String targetType = properties.getProperty(PROPERTY_TARGETTYPE);
        String lowLimit1Type = properties.getProperty(PROPERTY_LOWLIMIT1TYPE);
        String lowLimit2Type = properties.getProperty(PROPERTY_LOWLIMIT2TYPE);
        String lowLimit3Type = properties.getProperty(PROPERTY_LOWLIMIT3TYPE);
        if (sdcId.isEmpty() || sdcId.contains(";")) {
            throw new SapphireException("Missing or invalid mandatory input property SDC ID: " + sdcId);
        }
        if (keyId1.isEmpty() || keyId1.contains(";")) {
            throw new SapphireException("Missing or invalid mandatory input property Key ID1: " + keyId1);
        }
        if (paramListIds.isEmpty()) {
            throw new SapphireException("Missing mandatory input property parameter list ID");
        }
        if (paramListVersionIds.isEmpty()) {
            throw new SapphireException("Missing mandatory input property parameter list version ID");
        }
        if (variantIds.isEmpty()) {
            throw new SapphireException("Missing mandatory input property variant ID");
        }
        if (paramIds.isEmpty()) {
            throw new SapphireException("Missing mandatory input property parameter ID");
        }
        if (paramTypes.isEmpty()) {
            throw new SapphireException("Missing mandatory input property parameter type");
        }
        if (dataTypes.isEmpty()) {
            throw new SapphireException("Missing mandatory input property data types");
        }
        if (specIdColumn.isEmpty()) {
            throw new SapphireException("Missing mandatory input property spec ID column");
        }
        if (specVersionIdColumn.isEmpty()) {
            throw new SapphireException("Missing mandatory input property spec version ID column");
        }
        if (highLimit3Type.isEmpty()) {
            throw new SapphireException("Missing mandatory input property high limit 3 type");
        }
        if (highLimit2Type.isEmpty()) {
            throw new SapphireException("Missing mandatory input property high limit 2 type");
        }
        if (highLimit1Type.isEmpty()) {
            throw new SapphireException("Missing mandatory input property high limit 1 type");
        }
        if (inSpecType.isEmpty()) {
            throw new SapphireException("Missing mandatory input property in spec limit type");
        }
        if (targetType.isEmpty()) {
            throw new SapphireException("Missing mandatory input property target limit type");
        }
        if (lowLimit1Type.isEmpty()) {
            throw new SapphireException("Missing mandatory input property low limit 1 type");
        }
        if (lowLimit2Type.isEmpty()) {
            throw new SapphireException("Missing mandatory input property low limit 2 type");
        }
        if (lowLimit3Type.isEmpty()) {
            throw new SapphireException("Missing mandatory input property low limit 3 type");
        }
        SQLRegister.setSqlRegister(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getDatabaseId(), ADD_OR_EDIT_SPEC_SQL_REGISTER);
        this.paramListIdList = Arrays.asList(StringUtil.split(paramListIds, ";"));
        this.paramListVersionIdList = Arrays.asList(StringUtil.split(paramListVersionIds, ";"));
        this.variantIdList = Arrays.asList(StringUtil.split(variantIds, ";"));
        this.paramIdList = Arrays.asList(StringUtil.split(paramIds, ";"));
        this.paramTypeList = Arrays.asList(StringUtil.split(paramTypes, ";"));
        this.dataTypesList = Arrays.asList(StringUtil.split(dataTypes, ";"));
        this.highLimit3List = Arrays.asList(StringUtil.split(highLimit3, ";"));
        this.highLimit2List = Arrays.asList(StringUtil.split(highLimit2, ";"));
        this.highLimit1List = Arrays.asList(StringUtil.split(highLimit1, ";"));
        this.targetList = Arrays.asList(StringUtil.split(target, ";"));
        this.lowLimit1List = Arrays.asList(StringUtil.split(lowLimit1, ";"));
        this.lowLimit2List = Arrays.asList(StringUtil.split(lowLimit2, ";"));
        this.lowLimit3List = Arrays.asList(StringUtil.split(lowLimit3, ";"));
        this.customColumnCollection = new PropertyListCollection();
        this.getCustomColumns(this.customColumnCollection, properties);
        SpecHelper specHelper = new SpecHelper(this.getConnectionId());
        DataSet getSpecDs = specHelper.getSpec(sdcId, keyId1, keyId2, keyId3, specIdColumn, specVersionIdColumn);
        String specId = getSpecDs.getString(0, "specid", "");
        String specVersionId = getSpecDs.getString(0, "specversionid", "");
        if (specId.isEmpty()) {
            String specKey = this.createNewSpec(sdcId, keyId1, highLimit3Type, highLimit2Type, highLimit1Type, inSpecType, targetType, lowLimit1Type, lowLimit2Type, lowLimit3Type);
            specId = specKey.split("\\|")[0];
            specVersionId = specKey.split("\\|")[1];
            specHelper.addSpecToSDI(sdcId, keyId1, keyId2, keyId3, specId, specVersionId, specIdColumn, specVersionIdColumn);
            if (automaticVersioning == AutomaticVersioning.ALWAYS || automaticVersioning == AutomaticVersioning.ONLY_REFERENCED) {
                specHelper.syncVersionStatusWithPrimary(specId, specVersionId, sdcId, keyId1, keyId2, keyId3);
            }
        } else if (automaticVersioning == AutomaticVersioning.ALWAYS || automaticVersioning == AutomaticVersioning.ONLY_REFERENCED && specHelper.checkSpecReference(specId, specVersionId)) {
            PropertyList addSpecVersionProps = specHelper.createNewSpecVersion(specId, specVersionId);
            specId = addSpecVersionProps.getProperty("newkeyid1");
            specVersionId = addSpecVersionProps.getProperty("newkeyid2");
            specHelper.syncVersionStatusWithPrimary(specId, specVersionId, sdcId, keyId1, keyId2, keyId3);
            specHelper.addSpecToSDI(sdcId, keyId1, keyId2, keyId3, specId, specVersionId, specIdColumn, specVersionIdColumn);
        }
        int paramItemUserSequence = this.getMaxSpecParamItemSequence(specId, specVersionId);
        this.addOrEditSpecDetails(specId, specVersionId, paramItemUserSequence);
    }

    private void addOrEditSpecDetails(String specId, String specVersionId, int paramItemUserSequence) throws SapphireException {
        Map<String, List<String>> customColumnMap = this.getCustomColumns();
        this.isConsistent(customColumnMap);
        PropertyListCollection addSpecParamItemsCustomColumnCollection = new PropertyListCollection();
        PropertyListCollection editSpecParamItemsCustomColumnCollection = new PropertyListCollection();
        StringBuilder addSpecParamItemsParamListIds = new StringBuilder();
        StringBuilder addSpecParamItemsParamListVersionIds = new StringBuilder();
        StringBuilder addSpecParamItemsVariantIds = new StringBuilder();
        StringBuilder addSpecParamItemsParamIds = new StringBuilder();
        StringBuilder addSpecParamItemsParamTypes = new StringBuilder();
        StringBuilder addSpecParamItemsDataTypes = new StringBuilder();
        StringBuilder addSpecParamItemsAllowAnyParamListFlags = new StringBuilder();
        StringBuilder addSpecParamItemsUserSequences = new StringBuilder();
        PropertyList addParamLimitProps = new PropertyList();
        PropertyList editParamLimitProps = new PropertyList();
        PropertyList deleteParamLimitProps = new PropertyList();
        StringBuilder editSpecParamItemsParamListIds = new StringBuilder();
        StringBuilder editSpecParamItemsParamListVersionIds = new StringBuilder();
        StringBuilder editSpecParamItemsVariantIds = new StringBuilder();
        StringBuilder editSpecParamItemsParamIds = new StringBuilder();
        StringBuilder editSpecParamItemsParamTypes = new StringBuilder();
        String getSpecParamItemsPreparedSql = "SELECT paramlistid, paramlistversionid, variantid, paramid, paramtype FROM specparamitems WHERE specid = ? AND specversionid = ?";
        DataSet getSpecParamItemsDs = this.getQueryProcessor().getPreparedSqlDataSet(getSpecParamItemsPreparedSql, (Object[])new String[]{specId, specVersionId});
        String getSpecParamLimitsPreparedSql = "SELECT paramlistid, paramlistversionid, variantid, paramid, paramtype FROM specparamlimits WHERE specid = ? AND specversionid = ?";
        DataSet getSpecParamLimitsDs = this.getQueryProcessor().getPreparedSqlDataSet(getSpecParamLimitsPreparedSql, (Object[])new String[]{specId, specVersionId});
        for (int i = 0; i < this.paramListIdList.size(); ++i) {
            int j;
            boolean paramItemExists = false;
            boolean paramLimitsExists = false;
            String paramListId = this.paramListIdList.get(i);
            String paramListVersionId = this.paramListVersionIdList.get(i);
            String variantId = this.variantIdList.get(i);
            String paramId = this.paramIdList.get(i);
            String paramType = this.paramTypeList.get(i);
            String dataTypes = this.dataTypesList.get(i);
            List<String> paramItemKey = Arrays.asList(paramListId, variantId, paramId, paramType);
            for (j = 0; j < getSpecParamItemsDs.getRowCount(); ++j) {
                List<String> dsParamItemKey = Arrays.asList(getSpecParamItemsDs.getString(j, PROPERTY_PARAMLISTID), getSpecParamItemsDs.getString(j, PROPERTY_VARIANTID), getSpecParamItemsDs.getString(j, PROPERTY_PARAMID), getSpecParamItemsDs.getString(j, PROPERTY_PARAMTYPE));
                if (!paramItemKey.equals(dsParamItemKey)) continue;
                paramItemExists = true;
                break;
            }
            for (j = 0; j < getSpecParamLimitsDs.getRowCount(); ++j) {
                List<String> dsParamLimitsKey = Arrays.asList(getSpecParamLimitsDs.getString(j, PROPERTY_PARAMLISTID), getSpecParamLimitsDs.getString(j, PROPERTY_VARIANTID), getSpecParamLimitsDs.getString(j, PROPERTY_PARAMID), getSpecParamLimitsDs.getString(j, PROPERTY_PARAMTYPE));
                if (!paramItemKey.equals(dsParamLimitsKey)) continue;
                paramLimitsExists = true;
                break;
            }
            PropertyList props = new PropertyList();
            String highLimit3 = this.highLimit3List.get(i);
            String highLimit2 = this.highLimit2List.get(i);
            String highLimit1 = this.highLimit1List.get(i);
            String target = this.targetList.get(i);
            String lowLimit1 = this.lowLimit1List.get(i);
            String lowLimit2 = this.lowLimit2List.get(i);
            String lowLimit3 = this.lowLimit3List.get(i);
            props.setProperty(PROPERTY_HIGHLIMIT3, highLimit3);
            props.setProperty(PROPERTY_HIGHLIMIT2, highLimit2);
            props.setProperty(PROPERTY_HIGHLIMIT1, highLimit1);
            props.setProperty(PROPERTY_TARGET, target);
            props.setProperty(PROPERTY_LOWLIMIT1, lowLimit1);
            props.setProperty(PROPERTY_LOWLIMIT2, lowLimit2);
            props.setProperty(PROPERTY_LOWLIMIT3, lowLimit3);
            if (!paramItemExists) {
                addSpecParamItemsParamListIds.append(";").append(paramListId);
                addSpecParamItemsParamListVersionIds.append(";").append(paramListVersionId);
                addSpecParamItemsVariantIds.append(";").append(variantId);
                addSpecParamItemsParamIds.append(";").append(paramId);
                addSpecParamItemsParamTypes.append(";").append(paramType);
                addSpecParamItemsAllowAnyParamListFlags.append(";V");
                addSpecParamItemsUserSequences.append(";").append(++paramItemUserSequence);
                addSpecParamItemsDataTypes.append(";").append(dataTypes);
                this.appendCustomColumns(customColumnMap, addSpecParamItemsCustomColumnCollection, i);
            } else {
                editSpecParamItemsParamListIds.append(";").append(paramListId);
                editSpecParamItemsParamListVersionIds.append(";").append(paramListVersionId);
                editSpecParamItemsVariantIds.append(";").append(variantId);
                editSpecParamItemsParamIds.append(";").append(paramId);
                editSpecParamItemsParamTypes.append(";").append(paramType);
                this.appendCustomColumns(customColumnMap, editSpecParamItemsCustomColumnCollection, i);
            }
            if (!paramLimitsExists) {
                if (highLimit3.isEmpty() && highLimit2.isEmpty() && highLimit1.isEmpty() && lowLimit1.isEmpty() && lowLimit2.isEmpty() && lowLimit3.isEmpty() && target.isEmpty()) continue;
                this.collectSpecParamLimitProps(addParamLimitProps, "|", paramListId, paramListVersionId, variantId, paramId, paramType, dataTypes, props);
                continue;
            }
            if (!(highLimit3.isEmpty() && highLimit2.isEmpty() && highLimit1.isEmpty() && lowLimit1.isEmpty() && lowLimit2.isEmpty() && lowLimit3.isEmpty() && target.isEmpty())) {
                this.collectSpecParamLimitProps(editParamLimitProps, "|", paramListId, paramListVersionId, variantId, paramId, paramType, dataTypes, props);
                continue;
            }
            this.collectSpecParamLimitProps(deleteParamLimitProps, ";", paramListId, paramListVersionId, variantId, paramId, paramType, dataTypes, props);
        }
        if (addSpecParamItemsParamListIds.length() > 0) {
            PropertyList addSpecParamItemsProps = new PropertyList();
            addSpecParamItemsProps.clear();
            addSpecParamItemsProps.setProperty(PROPERTY_SDCID, "SpecSDC");
            addSpecParamItemsProps.setProperty(PROPERTY_KEYID1, specId);
            addSpecParamItemsProps.setProperty(PROPERTY_KEYID2, specVersionId);
            addSpecParamItemsProps.setProperty("linkid", "Param Items");
            addSpecParamItemsProps.setProperty(PROPERTY_PARAMLISTID, addSpecParamItemsParamListIds.substring(1));
            addSpecParamItemsProps.setProperty(PROPERTY_PARAMLISTVERSIONID, addSpecParamItemsParamListVersionIds.substring(1));
            addSpecParamItemsProps.setProperty(PROPERTY_VARIANTID, addSpecParamItemsVariantIds.substring(1));
            addSpecParamItemsProps.setProperty(PROPERTY_PARAMID, addSpecParamItemsParamIds.substring(1));
            addSpecParamItemsProps.setProperty(PROPERTY_PARAMTYPE, addSpecParamItemsParamTypes.substring(1));
            addSpecParamItemsProps.setProperty(PROPERTY_DATATYPES, addSpecParamItemsDataTypes.substring(1));
            addSpecParamItemsProps.setProperty("allowanyparamlistflag", addSpecParamItemsAllowAnyParamListFlags.substring(1));
            addSpecParamItemsProps.setProperty("usersequence", addSpecParamItemsUserSequences.substring(1));
            this.addCustomColumnsToProps(addSpecParamItemsCustomColumnCollection, addSpecParamItemsProps);
            this.getActionProcessor().processAction("AddSDIDetail", "1", addSpecParamItemsProps);
        }
        if (editSpecParamItemsParamListIds.length() > 0) {
            PropertyList editSpecParamItemsProps = new PropertyList();
            editSpecParamItemsProps.setProperty(PROPERTY_SDCID, "SpecSDC");
            editSpecParamItemsProps.setProperty(PROPERTY_KEYID1, specId);
            editSpecParamItemsProps.setProperty(PROPERTY_KEYID2, specVersionId);
            editSpecParamItemsProps.setProperty("linkid", "Param Items");
            editSpecParamItemsProps.setProperty(PROPERTY_PARAMLISTID, editSpecParamItemsParamListIds.substring(1));
            editSpecParamItemsProps.setProperty(PROPERTY_PARAMLISTVERSIONID, editSpecParamItemsParamListVersionIds.substring(1));
            editSpecParamItemsProps.setProperty(PROPERTY_VARIANTID, editSpecParamItemsVariantIds.substring(1));
            editSpecParamItemsProps.setProperty(PROPERTY_PARAMID, editSpecParamItemsParamIds.substring(1));
            editSpecParamItemsProps.setProperty(PROPERTY_PARAMTYPE, editSpecParamItemsParamTypes.substring(1));
            this.addCustomColumnsToProps(editSpecParamItemsCustomColumnCollection, editSpecParamItemsProps);
            this.getActionProcessor().processAction("EditSDIDetail", "1", editSpecParamItemsProps);
        }
        if (addParamLimitProps.size() > 0) {
            this.addOrEditSpecParamLimits(specId, specVersionId, addParamLimitProps, false);
        }
        if (editParamLimitProps.size() > 0) {
            this.addOrEditSpecParamLimits(specId, specVersionId, editParamLimitProps, true);
        }
        if (deleteParamLimitProps.size() > 0) {
            PropertyList deleteSpecParamLimitPros = new PropertyList();
            deleteSpecParamLimitPros.setProperty(PROPERTY_SDCID, "SpecSDC");
            deleteSpecParamLimitPros.setProperty(PROPERTY_KEYID1, specId);
            deleteSpecParamLimitPros.setProperty(PROPERTY_KEYID2, specVersionId);
            deleteSpecParamLimitPros.setProperty("linkid", "Param Limits");
            deleteSpecParamLimitPros.setProperty(PROPERTY_PARAMLISTID, deleteParamLimitProps.getProperty(PROPERTY_PARAMLISTID).substring(1));
            deleteSpecParamLimitPros.setProperty(PROPERTY_PARAMLISTVERSIONID, deleteParamLimitProps.getProperty(PROPERTY_PARAMLISTVERSIONID).substring(1));
            deleteSpecParamLimitPros.setProperty(PROPERTY_VARIANTID, deleteParamLimitProps.getProperty(PROPERTY_VARIANTID).substring(1));
            deleteSpecParamLimitPros.setProperty(PROPERTY_PARAMID, deleteParamLimitProps.getProperty(PROPERTY_PARAMID).substring(1));
            deleteSpecParamLimitPros.setProperty(PROPERTY_PARAMTYPE, deleteParamLimitProps.getProperty(PROPERTY_PARAMTYPE).substring(1));
            deleteSpecParamLimitPros.setProperty(PROPERTY_DATATYPES, deleteParamLimitProps.getProperty(PROPERTY_DATATYPES).substring(1));
            deleteSpecParamLimitPros.setProperty("limittypesequence", deleteParamLimitProps.getProperty("limittypesequence").substring(1));
            this.getActionProcessor().processAction("DeleteSDIDetail", "1", deleteSpecParamLimitPros);
        }
    }

    private void addCustomColumnsToProps(PropertyListCollection specParamItemsCustomColumnCollection, PropertyList addOrEditSpecParamItemsProps) {
        for (int i = 0; i < specParamItemsCustomColumnCollection.size(); ++i) {
            String columnValue;
            PropertyList editSpecParamItemsCustomColumnProps = specParamItemsCustomColumnCollection.getPropertyList(i);
            String customColumnId = editSpecParamItemsCustomColumnProps.getProperty("columnid");
            String customColumnValue = addOrEditSpecParamItemsProps.getProperty(customColumnId);
            if (!customColumnValue.isEmpty()) {
                customColumnValue = customColumnValue + ";";
            }
            if ((columnValue = editSpecParamItemsCustomColumnProps.getProperty("columnvalue")).isEmpty()) {
                columnValue = "(null)";
            }
            customColumnValue = customColumnValue + columnValue;
            addOrEditSpecParamItemsProps.setProperty(customColumnId, customColumnValue);
        }
    }

    private void addOrEditSpecParamLimits(String specId, String specVersionId, PropertyList addOrEditParamLimitProps, boolean doEdit) throws ActionException {
        PropertyList specParamLimitsProps = new PropertyList();
        if (doEdit) {
            specParamLimitsProps.setProperty(PROPERTY_SDCID, "SpecSDC");
            specParamLimitsProps.setProperty(PROPERTY_KEYID1, specId);
            specParamLimitsProps.setProperty(PROPERTY_KEYID2, specVersionId);
            specParamLimitsProps.setProperty("linkid", "Param Limits");
        } else {
            specParamLimitsProps.setProperty(PROPERTY_SDCID, "SpecSDC");
            specParamLimitsProps.setProperty(PROPERTY_KEYID1, specId);
            specParamLimitsProps.setProperty(PROPERTY_KEYID2, specVersionId);
            specParamLimitsProps.setProperty("linkid", "Param Limits");
        }
        specParamLimitsProps.setProperty(PROPERTY_PARAMLISTID, addOrEditParamLimitProps.getProperty(PROPERTY_PARAMLISTID).substring(1));
        specParamLimitsProps.setProperty(PROPERTY_PARAMLISTVERSIONID, addOrEditParamLimitProps.getProperty(PROPERTY_PARAMLISTVERSIONID).substring(1));
        specParamLimitsProps.setProperty(PROPERTY_VARIANTID, addOrEditParamLimitProps.getProperty(PROPERTY_VARIANTID).substring(1));
        specParamLimitsProps.setProperty(PROPERTY_PARAMID, addOrEditParamLimitProps.getProperty(PROPERTY_PARAMID).substring(1));
        specParamLimitsProps.setProperty(PROPERTY_PARAMTYPE, addOrEditParamLimitProps.getProperty(PROPERTY_PARAMTYPE).substring(1));
        specParamLimitsProps.setProperty(PROPERTY_DATATYPES, addOrEditParamLimitProps.getProperty(PROPERTY_DATATYPES).substring(1));
        specParamLimitsProps.setProperty("limittypesequence", addOrEditParamLimitProps.getProperty("limittypesequence").substring(1));
        specParamLimitsProps.setProperty("operator1", addOrEditParamLimitProps.getProperty("operator1").substring(1));
        specParamLimitsProps.setProperty("operator2", addOrEditParamLimitProps.getProperty("operator2").substring(1));
        specParamLimitsProps.setProperty("value1", addOrEditParamLimitProps.getProperty("value1").substring(1));
        specParamLimitsProps.setProperty("value2", addOrEditParamLimitProps.getProperty("value2").substring(1));
        if (doEdit) {
            specParamLimitsProps.setProperty("separator", "|");
            this.getActionProcessor().processAction("EditSDIDetail", "1", specParamLimitsProps);
        } else {
            specParamLimitsProps.setProperty("separator", "|");
            this.getActionProcessor().processAction("AddSDIDetail", "1", specParamLimitsProps);
        }
    }

    private void isConsistent(Map<String, List<String>> customColumnMap) throws SapphireException {
        if (this.paramListIdList.size() != this.variantIdList.size() || this.paramListIdList.size() != this.paramListVersionIdList.size() || this.paramListIdList.size() != this.paramIdList.size() || this.paramListIdList.size() != this.paramTypeList.size() || this.paramListIdList.size() != this.highLimit3List.size() || this.paramListIdList.size() != this.highLimit2List.size() || this.paramListIdList.size() != this.highLimit1List.size() || this.paramListIdList.size() != this.targetList.size() || this.paramListIdList.size() != this.lowLimit1List.size() || this.paramListIdList.size() != this.lowLimit2List.size() || this.paramListIdList.size() != this.lowLimit3List.size()) {
            throw new SapphireException("The number of values is not consistent");
        }
        Set<Map.Entry<String, List<String>>> customColumnSet = customColumnMap.entrySet();
        for (Map.Entry<String, List<String>> customColumn : customColumnSet) {
            List<String> columnValues = customColumn.getValue();
            if (this.paramListIdList.size() == columnValues.size()) continue;
            throw new SapphireException("The number of custom column values is not consistent");
        }
    }

    private Map<String, List<String>> getCustomColumns() {
        HashMap<String, List<String>> customColumnMap = new HashMap<String, List<String>>();
        for (int i = 0; i < this.customColumnCollection.size(); ++i) {
            PropertyList customColumnProps = this.customColumnCollection.getPropertyList(i);
            String id = customColumnProps.getProperty("id");
            List<String> valueList = Arrays.asList(customColumnProps.getProperty("value").split(";", -1));
            customColumnMap.put(id, valueList);
        }
        return customColumnMap;
    }

    private int getMaxSpecParamItemSequence(String specId, String specVersionId) {
        BigDecimal maxUserSequence;
        String getMaxParamItemUserSeqPreparedSql = "SELECT MAX(usersequence) maxusersequence FROM specparamitems WHERE specid = ? AND specversionid = ?";
        Object[] getMaxParamItemUserSeqPreparedParams = new String[]{specId, specVersionId};
        DataSet getMaxParamItemUserSeqDs = this.getQueryProcessor().getPreparedSqlDataSet(getMaxParamItemUserSeqPreparedSql, getMaxParamItemUserSeqPreparedParams);
        int paramItemUserSequence = 0;
        if (getMaxParamItemUserSeqDs.getRowCount() > 0 && (maxUserSequence = getMaxParamItemUserSeqDs.getBigDecimal(0, "maxusersequence")) != null) {
            paramItemUserSequence = maxUserSequence.intValue();
        }
        return paramItemUserSequence;
    }

    private void getCustomColumns(PropertyListCollection customColumnCollection, PropertyList properties) {
        Set propertyIdSet = properties.keySet();
        for (String propertyId : propertyIdSet) {
            if (BLOCKED_COLUMNS_LIST.contains(propertyId)) continue;
            String propertyValue = properties.getProperty(propertyId);
            PropertyList customColumnProps = new PropertyList();
            customColumnProps.setProperty("id", propertyId);
            customColumnProps.setProperty("value", propertyValue);
            this.addPropertyList(customColumnCollection, customColumnProps);
        }
    }

    private void appendCustomColumns(Map<String, List<String>> customColumnMap, PropertyListCollection addSpecParamItemsCustomColumnCollection, int index) {
        Set<Map.Entry<String, List<String>>> customColumnSet = customColumnMap.entrySet();
        for (Map.Entry<String, List<String>> customColumn : customColumnSet) {
            if (BLOCKED_COLUMNS_LIST.contains(customColumn.getKey() + ";")) continue;
            String customColumnValue = customColumn.getValue().get(index);
            PropertyList addSpecParamItemsCustomColumn = new PropertyList();
            addSpecParamItemsCustomColumn.setProperty("columnid", customColumn.getKey());
            addSpecParamItemsCustomColumn.setProperty("columnvalue", customColumnValue);
            this.addPropertyList(addSpecParamItemsCustomColumnCollection, addSpecParamItemsCustomColumn);
        }
    }

    private void addPropertyList(PropertyListCollection collection, PropertyList propertyList) {
        collection.add(propertyList);
    }

    private void collectSpecParamLimitProps(PropertyList storedProps, String separator, String paramListId, String paramListVersionId, String variantId, String paramId, String paramType, String aDataTypes, PropertyList props) {
        StringBuilder paramListIds = new StringBuilder();
        StringBuilder paramListVersionIds = new StringBuilder();
        StringBuilder variantIds = new StringBuilder();
        StringBuilder paramIds = new StringBuilder();
        StringBuilder paramTypes = new StringBuilder();
        StringBuilder dataTypes = new StringBuilder();
        StringBuilder limitTypeSequences = new StringBuilder();
        StringBuilder operator1 = new StringBuilder();
        StringBuilder operator2 = new StringBuilder();
        StringBuilder value1 = new StringBuilder();
        StringBuilder value2 = new StringBuilder();
        String highLimit3 = props.getProperty(PROPERTY_HIGHLIMIT3);
        String highLimit2 = props.getProperty(PROPERTY_HIGHLIMIT2);
        String highLimit1 = props.getProperty(PROPERTY_HIGHLIMIT1);
        String target = props.getProperty(PROPERTY_TARGET);
        String lowLimit1 = props.getProperty(PROPERTY_LOWLIMIT1);
        String lowLimit2 = props.getProperty(PROPERTY_LOWLIMIT2);
        String lowLimit3 = props.getProperty(PROPERTY_LOWLIMIT3);
        int limitTypeCount = 8;
        String lowLimit3Operator1 = "";
        String lowLimit3Operator2 = "";
        String lowLimit2Operator2 = "";
        String lowLimit2Operator1 = "";
        String lowLimit1Operator2 = "";
        String lowLimit1Operator1 = "";
        String inSpecificationOperator2 = "";
        String inSpecificationOperator1 = "";
        String targetOperator2 = "";
        String targetOperator1 = "";
        String highLimit1Operator1 = "";
        String highLimit1Operator2 = "";
        String highLimit2Operator1 = "";
        String highLimit2Operator2 = "";
        String highLimit3Operator1 = "";
        String highLimit3Operator2 = "";
        String lowLimit3Value1 = "";
        String lowLimit3Value2 = "";
        String lowLimit2Value1 = "";
        String lowLimit2Value2 = "";
        String lowLimit1Value1 = "";
        String lowLimit1Value2 = "";
        String inSpecificationValue1 = "";
        String inSpecificationValue2 = "";
        String targetValue1 = "";
        String targetValue2 = "";
        String highLimit1Value1 = "";
        String highLimit1Value2 = "";
        String highLimit2Value1 = "";
        String highLimit2Value2 = "";
        String highLimit3Value1 = "";
        String highLimit3Value2 = "";
        M18NUtil m18n = new M18NUtil(this.connectionInfo);
        BigDecimal lowLimit3Num = null;
        BigDecimal lowLimit2Num = null;
        BigDecimal lowLimit1Num = null;
        BigDecimal targetNum = null;
        BigDecimal highLimit1Num = null;
        BigDecimal highLimit2Num = null;
        BigDecimal highLimit3Num = null;
        if (aDataTypes.equals("N") || aDataTypes.equals("NC")) {
            BigDecimal minHighLimit;
            BigDecimal minLowLimit;
            StringBuffer valueNum;
            if (!lowLimit3.isEmpty()) {
                valueNum = new StringBuffer();
                MiscUtil.MiscString.parseComplexNumber(lowLimit3, valueNum, new StringBuffer(), m18n, false);
                lowLimit3Num = new BigDecimal(valueNum.toString());
            }
            if (!lowLimit2.isEmpty()) {
                valueNum = new StringBuffer();
                MiscUtil.MiscString.parseComplexNumber(lowLimit2, valueNum, new StringBuffer(), m18n, false);
                lowLimit2Num = new BigDecimal(valueNum.toString());
            }
            if (!lowLimit1.isEmpty()) {
                valueNum = new StringBuffer();
                MiscUtil.MiscString.parseComplexNumber(lowLimit1, valueNum, new StringBuffer(), m18n, false);
                lowLimit1Num = new BigDecimal(valueNum.toString());
            }
            if (!target.isEmpty()) {
                valueNum = new StringBuffer();
                MiscUtil.MiscString.parseComplexNumber(target, valueNum, new StringBuffer(), m18n, false);
                targetNum = new BigDecimal(valueNum.toString());
            }
            if (!highLimit1.isEmpty()) {
                valueNum = new StringBuffer();
                MiscUtil.MiscString.parseComplexNumber(highLimit1, valueNum, new StringBuffer(), m18n, false);
                highLimit1Num = new BigDecimal(valueNum.toString());
            }
            if (!highLimit2.isEmpty()) {
                valueNum = new StringBuffer();
                MiscUtil.MiscString.parseComplexNumber(highLimit2, valueNum, new StringBuffer(), m18n, false);
                highLimit2Num = new BigDecimal(valueNum.toString());
            }
            if (!highLimit3.isEmpty()) {
                valueNum = new StringBuffer();
                MiscUtil.MiscString.parseComplexNumber(highLimit3, valueNum, new StringBuffer(), m18n, false);
                highLimit3Num = new BigDecimal(valueNum.toString());
            }
            boolean hasLowLimit = false;
            boolean hasHighLimit = false;
            boolean hasTarget = false;
            if (lowLimit1Num != null || lowLimit2Num != null || lowLimit3Num != null) {
                hasLowLimit = true;
            }
            if (highLimit1Num != null || highLimit2Num != null || highLimit3Num != null) {
                hasHighLimit = true;
            }
            if (targetNum != null) {
                hasTarget = true;
            }
            if (lowLimit1Num != null) {
                if (lowLimit2Num != null) {
                    lowLimit1Value1 = m18n.format(lowLimit2Num);
                    lowLimit1Operator1 = ">=";
                    lowLimit1Value2 = m18n.format(lowLimit1Num);
                    lowLimit1Operator2 = "<";
                } else if (lowLimit3Num != null) {
                    lowLimit1Value1 = m18n.format(lowLimit3Num);
                    lowLimit1Operator1 = ">=";
                    lowLimit1Value2 = m18n.format(lowLimit1Num);
                    lowLimit1Operator2 = "<";
                } else {
                    lowLimit1Value1 = m18n.format(lowLimit1Num);
                    lowLimit1Operator1 = "<";
                }
            }
            if (lowLimit2Num != null) {
                if (lowLimit3Num != null) {
                    lowLimit2Value1 = m18n.format(lowLimit3Num);
                    lowLimit2Operator1 = ">=";
                    lowLimit2Value2 = m18n.format(lowLimit2Num);
                    lowLimit2Operator2 = "<";
                } else {
                    lowLimit2Value1 = m18n.format(lowLimit2Num);
                    lowLimit2Operator1 = "<";
                }
            }
            if (lowLimit3Num != null) {
                lowLimit3Value1 = m18n.format(lowLimit3Num);
                lowLimit3Operator1 = "<";
            }
            if (hasLowLimit && !hasHighLimit) {
                minLowLimit = this.minLimitNum(lowLimit3Num, lowLimit2Num, lowLimit1Num);
                inSpecificationValue1 = m18n.format(minLowLimit);
                inSpecificationOperator1 = ">=";
            }
            if (!hasLowLimit && hasHighLimit) {
                minHighLimit = this.minLimitNum(highLimit3Num, highLimit2Num, highLimit1Num);
                inSpecificationValue1 = m18n.format(minHighLimit);
                inSpecificationOperator1 = "<=";
            }
            if (hasLowLimit && hasHighLimit) {
                minLowLimit = this.minLimitNum(lowLimit3Num, lowLimit2Num, lowLimit1Num);
                inSpecificationValue1 = m18n.format(minLowLimit);
                inSpecificationOperator1 = ">=";
                minHighLimit = this.minLimitNum(highLimit3Num, highLimit2Num, highLimit1Num);
                inSpecificationValue2 = m18n.format(minHighLimit);
                inSpecificationOperator2 = "<=";
            }
            if (!hasLowLimit && !hasHighLimit && hasTarget) {
                inSpecificationValue1 = m18n.format(targetNum);
                inSpecificationOperator1 = "Not In";
            }
            if (hasTarget) {
                targetOperator1 = "=";
                targetValue1 = m18n.format(targetNum);
            }
            if (highLimit1Num != null) {
                highLimit1Value1 = m18n.format(highLimit1Num);
                highLimit1Operator1 = ">";
                if (highLimit2Num != null) {
                    highLimit1Value2 = m18n.format(highLimit2Num);
                    highLimit1Operator2 = "<=";
                } else if (highLimit3Num != null) {
                    highLimit1Value2 = m18n.format(highLimit3Num);
                    highLimit1Operator2 = "<=";
                }
            }
            if (highLimit2Num != null) {
                highLimit2Value1 = m18n.format(highLimit2Num);
                highLimit2Operator1 = ">";
                if (highLimit3Num != null) {
                    highLimit2Value2 = m18n.format(highLimit3Num);
                    highLimit2Operator2 = "<=";
                }
            }
            if (highLimit3Num != null) {
                highLimit3Value1 = m18n.format(highLimit3Num);
                highLimit3Operator1 = ">";
            }
        } else {
            lowLimit3 = lowLimit3.replaceAll("\\|", ";");
            lowLimit2 = lowLimit2.replaceAll("\\|", ";");
            lowLimit1 = lowLimit1.replaceAll("\\|", ";");
            target = target.replaceAll("\\|", ";");
            highLimit1 = highLimit1.replaceAll("\\|", ";");
            highLimit2 = highLimit2.replaceAll("\\|", ";");
            highLimit3 = highLimit3.replaceAll("\\|", ";");
            if (target.isEmpty()) {
                inSpecificationOperator1 = "Not In";
                if (!lowLimit3.isEmpty()) {
                    lowLimit3Operator1 = "In";
                    lowLimit3Value1 = lowLimit3;
                    inSpecificationValue1 = inSpecificationValue1 + ";" + lowLimit3Value1;
                }
                if (!lowLimit2.isEmpty()) {
                    lowLimit2Operator1 = "In";
                    lowLimit2Value1 = lowLimit2;
                    inSpecificationValue1 = inSpecificationValue1 + ";" + lowLimit2Value1;
                }
                if (!lowLimit1.isEmpty()) {
                    lowLimit1Operator1 = "In";
                    lowLimit1Value1 = lowLimit1;
                    inSpecificationValue1 = inSpecificationValue1 + ";" + lowLimit1Value1;
                }
                if (!highLimit1.isEmpty()) {
                    highLimit1Operator1 = "In";
                    highLimit1Value1 = highLimit1;
                    inSpecificationValue1 = inSpecificationValue1 + ";" + highLimit1Value1;
                }
                if (!highLimit2.isEmpty()) {
                    highLimit2Operator1 = "In";
                    highLimit2Value1 = highLimit2;
                    inSpecificationValue1 = inSpecificationValue1 + ";" + highLimit2Value1;
                }
                if (!highLimit3.isEmpty()) {
                    highLimit3Operator1 = "In";
                    highLimit3Value1 = highLimit3;
                    inSpecificationValue1 = inSpecificationValue1 + ";" + highLimit3Value1;
                }
                if (inSpecificationValue1.length() > 0) {
                    inSpecificationValue1 = inSpecificationValue1.substring(1);
                }
            } else {
                inSpecificationOperator1 = "In";
                inSpecificationValue1 = target;
                targetOperator1 = "In";
                targetValue1 = target;
                if (!lowLimit3.isEmpty()) {
                    lowLimit3Operator1 = "In";
                    lowLimit3Value1 = lowLimit3;
                }
                if (!lowLimit2.isEmpty()) {
                    lowLimit2Operator1 = "In";
                    lowLimit2Value1 = lowLimit2;
                }
                if (!lowLimit1.isEmpty()) {
                    lowLimit1Operator1 = "In";
                    lowLimit1Value1 = lowLimit1;
                }
                if (!highLimit1.isEmpty()) {
                    highLimit1Operator1 = "In";
                    highLimit1Value1 = highLimit1;
                }
                if (!highLimit2.isEmpty()) {
                    highLimit2Operator1 = "In";
                    highLimit2Value1 = highLimit2;
                }
                if (!highLimit3.isEmpty()) {
                    highLimit3Operator1 = "In";
                    highLimit3Value1 = highLimit3;
                }
            }
        }
        for (int i = 0; i < limitTypeCount; ++i) {
            paramListIds.append(separator).append(paramListId);
            paramListVersionIds.append(separator).append(paramListVersionId);
            variantIds.append(separator).append(variantId);
            paramIds.append(separator).append(paramId);
            paramTypes.append(separator).append(paramType);
            dataTypes.append(separator).append(aDataTypes);
            limitTypeSequences.append(separator).append(i + 1);
        }
        this.addLimits(operator1, operator2, value1, value2, lowLimit3Operator1, lowLimit3Operator2, lowLimit3Value1, lowLimit3Value2, separator);
        this.addLimits(operator1, operator2, value1, value2, lowLimit2Operator1, lowLimit2Operator2, lowLimit2Value1, lowLimit2Value2, separator);
        this.addLimits(operator1, operator2, value1, value2, lowLimit1Operator1, lowLimit1Operator2, lowLimit1Value1, lowLimit1Value2, separator);
        this.addLimits(operator1, operator2, value1, value2, inSpecificationOperator1, inSpecificationOperator2, inSpecificationValue1, inSpecificationValue2, separator);
        this.addLimits(operator1, operator2, value1, value2, targetOperator1, targetOperator2, targetValue1, targetValue2, separator);
        this.addLimits(operator1, operator2, value1, value2, highLimit1Operator1, highLimit1Operator2, highLimit1Value1, highLimit1Value2, separator);
        this.addLimits(operator1, operator2, value1, value2, highLimit2Operator1, highLimit2Operator2, highLimit2Value1, highLimit2Value2, separator);
        this.addLimits(operator1, operator2, value1, value2, highLimit3Operator1, highLimit3Operator2, highLimit3Value1, highLimit3Value2, separator);
        storedProps.setProperty(PROPERTY_PARAMLISTID, storedProps.getProperty(PROPERTY_PARAMLISTID) + paramListIds);
        storedProps.setProperty(PROPERTY_PARAMLISTVERSIONID, storedProps.getProperty(PROPERTY_PARAMLISTVERSIONID) + paramListVersionIds);
        storedProps.setProperty(PROPERTY_VARIANTID, storedProps.getProperty(PROPERTY_VARIANTID) + variantIds);
        storedProps.setProperty(PROPERTY_PARAMID, storedProps.getProperty(PROPERTY_PARAMID) + paramIds);
        storedProps.setProperty(PROPERTY_PARAMTYPE, storedProps.getProperty(PROPERTY_PARAMTYPE) + paramTypes);
        storedProps.setProperty(PROPERTY_DATATYPES, storedProps.getProperty(PROPERTY_DATATYPES) + dataTypes);
        storedProps.setProperty("limittypesequence", storedProps.getProperty("limittypesequence") + limitTypeSequences);
        storedProps.setProperty("operator1", storedProps.getProperty("operator1") + operator1);
        storedProps.setProperty("operator2", storedProps.getProperty("operator2") + operator2);
        storedProps.setProperty("value1", storedProps.getProperty("value1") + value1);
        storedProps.setProperty("value2", storedProps.getProperty("value2") + value2);
    }

    private BigDecimal minLimitNum(BigDecimal limit3, BigDecimal limit2, BigDecimal limit1) {
        BigDecimal min = null;
        if (limit1 != null) {
            min = limit1;
        } else if (limit2 != null) {
            min = limit2;
        } else if (limit3 != null) {
            min = limit3;
        }
        return min;
    }

    private void addLimits(StringBuilder operator1, StringBuilder operator2, StringBuilder value1, StringBuilder value2, String limitOperator1, String limitOperator2, String limitValue1, String limitValue2, String separator) {
        operator1.append(separator).append(limitOperator1);
        operator2.append(separator).append(limitOperator2);
        value1.append(separator).append(limitValue1);
        value2.append(separator).append(limitValue2);
    }

    private String createNewSpec(String sdcId, String keyId1, String highLimit3Type, String highLimit2Type, String highLimit1Type, String inSpecType, String targetType, String lowLimit1Type, String lowLimit2Type, String lowLimit3Type) throws ActionException {
        String sdcName = this.getQueryProcessor().getPreparedSqlDataSet("SELECT singular FROM sdc WHERE sdcid = ?", (Object[])new String[]{sdcId}).getString(0, "singular");
        PropertyList getNewSpecIdProps = new PropertyList();
        getNewSpecIdProps.setProperty(PROPERTY_SDCID, sdcId);
        getNewSpecIdProps.setProperty(PROPERTY_KEYID1, keyId1);
        this.getActionProcessor().processActionClass(GetNewSpecId.class.getName(), getNewSpecIdProps);
        String newSpecId = getNewSpecIdProps.getProperty("newspecid");
        PropertyList addSDIProps = new PropertyList();
        addSDIProps.setProperty(PROPERTY_SDCID, "SpecSDC");
        addSDIProps.setProperty("overrideautokey", "Y");
        addSDIProps.setProperty(PROPERTY_KEYID1, newSpecId);
        addSDIProps.setProperty(PROPERTY_KEYID2, "1");
        addSDIProps.setProperty("specdesc", "Auto created and maintained simple spec for " + sdcName);
        addSDIProps.setProperty("ruletypeflag", "S");
        addSDIProps.setProperty("versionstatus", "P");
        addSDIProps.setProperty("spectypeflag", "S");
        addSDIProps.setProperty("embeddedflag", "Y");
        this.getActionProcessor().processAction("AddSDI", "1", addSDIProps);
        String specId = addSDIProps.getProperty("newkeyid1");
        String specVersionId = addSDIProps.getProperty("newkeyid2");
        PropertyList addLimitTypeProps = new PropertyList();
        addLimitTypeProps.setProperty(PROPERTY_SDCID, "SpecSDC");
        addLimitTypeProps.setProperty(PROPERTY_KEYID1, specId);
        addLimitTypeProps.setProperty(PROPERTY_KEYID2, specVersionId);
        addLimitTypeProps.setProperty("linkid", "Limit Types");
        addLimitTypeProps.setProperty("limittypesequence", "1;2;3;4;5;6;7;8");
        addLimitTypeProps.setProperty("limittypeid", lowLimit3Type + ";" + lowLimit2Type + ";" + lowLimit1Type + ";" + inSpecType + ";" + targetType + ";" + highLimit1Type + ";" + highLimit2Type + ";" + highLimit3Type);
        addLimitTypeProps.setProperty("condition", "Fail;Warning;Notification;Pass;Pass;Notification;Warning;Fail");
        addLimitTypeProps.setProperty("operator1display", "V;V;V;V;V;V;V;V");
        addLimitTypeProps.setProperty("operator2display", "I;V;V;V;I;V;V;I");
        addLimitTypeProps.setProperty("value1display", "V;V;V;V;V;V;V;V");
        addLimitTypeProps.setProperty("value2display", "I;V;V;V;I;V;V;I");
        addLimitTypeProps.setProperty("separatordisplay", "V;V;V;V;V;V;V;V");
        addLimitTypeProps.setProperty("usersequence", "1;2;3;4;5;6;7;8");
        this.getActionProcessor().processAction("AddSDIDetail", "1", addLimitTypeProps);
        PropertyList addSpecRuleProps = new PropertyList();
        addSpecRuleProps.setProperty(PROPERTY_SDCID, "SpecSDC");
        addSpecRuleProps.setProperty(PROPERTY_KEYID1, specId);
        addSpecRuleProps.setProperty(PROPERTY_KEYID2, specVersionId);
        addSpecRuleProps.setProperty("linkid", "Spec Rules");
        addSpecRuleProps.setProperty("ruleno", "1\u00a42\u00a43\u00a44\u00a45");
        addSpecRuleProps.setProperty("ruledef", "Fail;;1;N;Y;Fail\u00a4Warning;;1;N;Y;Warning\u00a4Notification;;1;N;Y;Notification\u00a4Pass;A;A;N;Y;Pass\u00a4(none);;;;;");
        addSpecRuleProps.setProperty("separator", "\u00a4");
        this.getActionProcessor().processAction("AddSDIDetail", "1", addSpecRuleProps);
        String specKey = specId + "|" + specVersionId;
        return specKey;
    }

    static {
        BLOCKED_COLUMNS_LIST.add(PROPERTY_SDCID);
        BLOCKED_COLUMNS_LIST.add(PROPERTY_KEYID1);
        BLOCKED_COLUMNS_LIST.add(PROPERTY_KEYID2);
        BLOCKED_COLUMNS_LIST.add(PROPERTY_KEYID3);
        BLOCKED_COLUMNS_LIST.add(PROPERTY_PARAMLISTID);
        BLOCKED_COLUMNS_LIST.add(PROPERTY_PARAMLISTVERSIONID);
        BLOCKED_COLUMNS_LIST.add(PROPERTY_PARAMID);
        BLOCKED_COLUMNS_LIST.add(PROPERTY_VARIANTID);
        BLOCKED_COLUMNS_LIST.add(PROPERTY_PARAMTYPE);
        BLOCKED_COLUMNS_LIST.add(PROPERTY_DATATYPES);
        BLOCKED_COLUMNS_LIST.add(PROPERTY_HIGHLIMIT3);
        BLOCKED_COLUMNS_LIST.add(PROPERTY_HIGHLIMIT2);
        BLOCKED_COLUMNS_LIST.add(PROPERTY_HIGHLIMIT1);
        BLOCKED_COLUMNS_LIST.add(PROPERTY_TARGET);
        BLOCKED_COLUMNS_LIST.add(PROPERTY_LOWLIMIT1);
        BLOCKED_COLUMNS_LIST.add(PROPERTY_LOWLIMIT2);
        BLOCKED_COLUMNS_LIST.add(PROPERTY_LOWLIMIT3);
        BLOCKED_COLUMNS_LIST.add(PROPERTY_SPECIDCOLUMN);
        BLOCKED_COLUMNS_LIST.add(PROPERTY_SPECVERSIONIDCOLUMN);
        BLOCKED_COLUMNS_LIST.add(PROPERTY_AUTOMATICVERSIONING);
        BLOCKED_COLUMNS_LIST.add(PROPERTY_HIGHLIMIT3TYPE);
        BLOCKED_COLUMNS_LIST.add(PROPERTY_HIGHLIMIT2TYPE);
        BLOCKED_COLUMNS_LIST.add(PROPERTY_HIGHLIMIT1TYPE);
        BLOCKED_COLUMNS_LIST.add(PROPERTY_TARGETTYPE);
        BLOCKED_COLUMNS_LIST.add(PROPERTY_INSPECTYPE);
        BLOCKED_COLUMNS_LIST.add(PROPERTY_LOWLIMIT1TYPE);
        BLOCKED_COLUMNS_LIST.add(PROPERTY_LOWLIMIT2TYPE);
        BLOCKED_COLUMNS_LIST.add(PROPERTY_LOWLIMIT3TYPE);
        BLOCKED_COLUMNS_LIST.add("applylock");
        ADD_OR_EDIT_SPEC_SQL_REGISTER = new BaseSQLRegister(){

            @Override
            public String getSQLStatement(int sqlCode) {
                if (sqlCode == 1) {
                    return "UPDATE specparamitems SET paramlistversionid = ? WHERE paramlistid = ? AND paramlistversionid = ? AND variantid = ? AND paramid = ? AND paramtype = ?";
                }
                if (sqlCode == 2) {
                    return "UPDATE specparamlimits SET paramlistversionid = ? WHERE paramlistid = ? AND paramlistversionid = ? AND variantid = ? AND paramid = ? AND paramtype = ?";
                }
                if (sqlCode == 3) {
                    return "DELETE FROM specparamitems WHERE paramlistid = ? AND paramlistversionid = ? AND variantid = ? AND paramid = ? AND paramtype = ?";
                }
                if (sqlCode == 4) {
                    return "DELETE FROM specparamlimits WHERE paramlistid = ? AND paramlistversionid = ? AND variantid = ? AND paramid = ? AND paramtype = ?";
                }
                return "";
            }
        };
    }
}

