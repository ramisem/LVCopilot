/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.simplespec.element;

import com.labvantage.opal.elements.BasePropertyHandler;
import com.labvantage.sapphire.pageelements.simplespec.action.AddOrEditSpec;
import com.labvantage.sapphire.pageelements.simplespec.action.AddSPDetailItem;
import com.labvantage.sapphire.pageelements.simplespec.action.AddSPWorkItem;
import com.labvantage.sapphire.pageelements.simplespec.action.AddTestingLevel;
import com.labvantage.sapphire.pageelements.simplespec.action.DelSPDetailItem;
import com.labvantage.sapphire.pageelements.simplespec.action.DelTestingLevel;
import com.labvantage.sapphire.pageelements.simplespec.action.DeleteSPWorkItem;
import com.labvantage.sapphire.pageelements.simplespec.action.DeleteSpecParam;
import com.labvantage.sapphire.pageelements.simplespec.action.EditSPWorkItem;
import com.labvantage.sapphire.pageelements.simplespec.util.SimpleSpecHelper;
import com.labvantage.sapphire.servlet.RequestProcessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.servlet.RequestContext;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SimpleSpecPropertyHandler
extends BasePropertyHandler {
    private static final List<String> ILLEGAL_COLUMN_IDS_LIST = new ArrayList<String>();
    private static final String ADD_STATUS = "a";
    private static final String DELETE_STATUS = "d";
    private static final String VIRTUAL_STATUS = "v";
    private String automaticSamplingPlanVersioning;
    private String automaticSpecVersioning;
    private String specIdColumn;
    private String specVersionIdColumn;
    private boolean enableTestingLevels;
    private String samplingPlanIdColumn;
    private String samplingPlanVersionIdColumn;
    private String sdcId;
    private String keyId1;
    private String keyId2;
    private String keyId3;
    private String highLimit3Type;
    private String highLimit2Type;
    private String highLimit1Type;
    private String inSpecType;
    private String targetType;
    private String lowLimit1Type;
    private String lowLimit2Type;
    private String lowLimit3Type;
    private String webPageId;

    @Override
    public void processProperties(HashMap hashMap) throws SapphireException {
        JSONObject tablePropsJsonObject;
        JSONObject testingLevelPropsJson;
        PropertyList props = new PropertyList(hashMap);
        ActionBlock testingLevelActionBlock = new ActionBlock();
        String elementId = props.getProperty("__propertyhandler_elementid");
        this.sdcId = props.getProperty("_" + elementId + "_sdcid");
        this.keyId1 = props.getProperty("_" + elementId + "_keyid1");
        this.keyId2 = props.getProperty("_" + elementId + "_keyid2");
        this.keyId3 = props.getProperty("_" + elementId + "_keyid3");
        this.webPageId = props.getProperty("_" + elementId + "_webpageid");
        if (this.webPageId.length() == 0) {
            throw new SapphireException("No Webpage id found.");
        }
        String tablePropsJsonString = HttpUtil.decodeURIComponent(props.getProperty("_" + elementId + "_tablepropsjson"));
        String testingLevelPropsJsonString = HttpUtil.decodeURIComponent(props.getProperty("_" + elementId + "_testinglevelpropsjson"));
        RequestProcessor requestProcessor = new RequestProcessor(this.sapphireConnection.getConnectionId());
        PropertyList pageProps = requestProcessor.getWebPageProperties(this.webPageId, new RequestContext(new PropertyList()));
        PropertyList elementProps = pageProps.getPropertyList(elementId);
        if (elementProps == null || elementProps.size() == 0) {
            throw new IllegalArgumentException("Element not found!");
        }
        try {
            testingLevelPropsJson = new JSONObject(testingLevelPropsJsonString);
        }
        catch (JSONException e) {
            throw new SapphireException("Cannot parse testing level collection", e);
        }
        PropertyListCollection testingLevelCollection = new PropertyList(testingLevelPropsJson).getCollectionNotNull("rowcollection");
        PropertyListCollection configuredSDIWorkItemColumnCollection = elementProps.getCollectionNotNull("sdiworkitemcolumncollection");
        PropertyListCollection configuredWorkItemColumnCollection = elementProps.getCollectionNotNull("workitemcolumncollection");
        PropertyListCollection configuredParamListItemColumnCollection = elementProps.getCollectionNotNull("paramlistitemcolumncollection");
        PropertyListCollection configuredSpecParamItemColumnCollection = elementProps.getCollectionNotNull("specparamitemcolumncollection");
        SimpleSpecHelper simpleSpecHelper = new SimpleSpecHelper(this.sapphireConnection.getConnectionId());
        simpleSpecHelper.completeConfiguredColumns(configuredSDIWorkItemColumnCollection, "sdiworkitem");
        simpleSpecHelper.completeConfiguredColumns(configuredSpecParamItemColumnCollection, "specparamitems");
        simpleSpecHelper.completeConfiguredColumns(configuredWorkItemColumnCollection, "workitem");
        simpleSpecHelper.completeConfiguredColumns(configuredParamListItemColumnCollection, "paramlistitem");
        PropertyList columnProps = new PropertyList();
        columnProps.setProperty("workitemcolumncollection", configuredWorkItemColumnCollection);
        columnProps.setProperty("sdiworkitemcolumncollection", configuredSDIWorkItemColumnCollection);
        columnProps.setProperty("paramlistitemcolumncollection", configuredParamListItemColumnCollection);
        columnProps.setProperty("specparamitemcolumncollection", configuredSpecParamItemColumnCollection);
        this.automaticSamplingPlanVersioning = elementProps.getProperty("automaticsamplingplanversioning");
        this.automaticSpecVersioning = elementProps.getProperty("automaticspecversioning");
        this.specIdColumn = elementProps.getProperty("specidcolumn");
        this.specVersionIdColumn = elementProps.getProperty("specversionidcolumn");
        this.enableTestingLevels = elementProps.getProperty("enabletestinglevels", "y").toLowerCase().startsWith("y");
        this.samplingPlanIdColumn = elementProps.getProperty("samplingplanidcolumn");
        this.samplingPlanVersionIdColumn = elementProps.getProperty("samplingplanversionidcolumn");
        PropertyList limitProps = elementProps.getPropertyListNotNull("limitprops");
        this.highLimit3Type = limitProps.getPropertyListNotNull("limit3props").getProperty("highlimittype");
        this.highLimit2Type = limitProps.getPropertyListNotNull("limit2props").getProperty("highlimittype");
        this.highLimit1Type = limitProps.getPropertyListNotNull("limit1props").getProperty("highlimittype");
        this.inSpecType = limitProps.getPropertyListNotNull("inspecprops").getProperty("limittype");
        this.targetType = limitProps.getPropertyListNotNull("targetprops").getProperty("limittype");
        this.lowLimit1Type = limitProps.getPropertyListNotNull("limit1props").getProperty("lowlimittype");
        this.lowLimit2Type = limitProps.getPropertyListNotNull("limit2props").getProperty("lowlimittype");
        this.lowLimit3Type = limitProps.getPropertyListNotNull("limit3props").getProperty("lowlimittype");
        PropertyListCollection sdiWorkItemColumnCollection = columnProps.getCollectionNotNull("sdiworkitemcolumncollection");
        PropertyListCollection specParamitemColumnCollection = columnProps.getCollectionNotNull("specparamitemcolumncollection");
        StringBuilder addLevelIds = new StringBuilder();
        StringBuilder deleteSamplingPlanDetailNumbers = new StringBuilder();
        ArrayList<String> deleteLevelIdList = new ArrayList<String>();
        ArrayList<String> addLevelIdList = new ArrayList<String>();
        for (int i = 0; i < testingLevelCollection.size(); ++i) {
            String testingLevelId;
            PropertyList testingLevelRowProps = testingLevelCollection.getPropertyList(i);
            if (testingLevelRowProps.getProperty("rowstatus").toLowerCase().startsWith(ADD_STATUS)) {
                testingLevelId = testingLevelRowProps.getProperty("levelid");
                addLevelIds.append(";").append(testingLevelId);
                addLevelIdList.add(testingLevelId);
            }
            if (!testingLevelRowProps.getProperty("rowstatus").toLowerCase().startsWith(DELETE_STATUS)) continue;
            String samplingPlanDetailNo = testingLevelRowProps.getProperty("samplingplandetailno");
            testingLevelId = testingLevelRowProps.getProperty("levelid");
            deleteSamplingPlanDetailNumbers.append(";").append(samplingPlanDetailNo);
            deleteLevelIdList.add(testingLevelId);
        }
        PropertyList addTestingLevelProps = null;
        if (addLevelIds.length() > 0) {
            addTestingLevelProps = new PropertyList();
            addTestingLevelProps.setProperty("sdcid", this.sdcId);
            addTestingLevelProps.setProperty("keyid1", this.keyId1);
            if (this.keyId2.length() > 0) {
                addTestingLevelProps.setProperty("keyid2", this.keyId2);
                if (this.keyId3.length() > 0) {
                    addTestingLevelProps.setProperty("keyid3", this.keyId3);
                }
            }
            addTestingLevelProps.setProperty("levelid", addLevelIds.substring(1));
            addTestingLevelProps.setProperty("automaticversioning", elementProps.getProperty("automaticsamplingplanversioning"));
            addTestingLevelProps.setProperty("samplingplanidcolumn", this.samplingPlanIdColumn);
            addTestingLevelProps.setProperty("samplingplanversionidcolumn", this.samplingPlanVersionIdColumn);
            testingLevelActionBlock.setActionClass("addtestinglevel", AddTestingLevel.class.getName(), addTestingLevelProps);
        }
        if (deleteSamplingPlanDetailNumbers.length() > 0) {
            PropertyList delTestingLevels = new PropertyList();
            delTestingLevels.setProperty("sdcid", this.sdcId);
            delTestingLevels.setProperty("keyid1", this.keyId1);
            if (this.keyId2.length() > 0) {
                delTestingLevels.setProperty("keyid2", this.keyId2);
                if (this.keyId3.length() > 0) {
                    delTestingLevels.setProperty("keyid3", this.keyId3);
                }
            }
            delTestingLevels.setProperty("samplingplandetailno", deleteSamplingPlanDetailNumbers.substring(1));
            delTestingLevels.setProperty("automaticversioning", elementProps.getProperty("automaticsamplingplanversioning"));
            delTestingLevels.setProperty("samplingplanidcolumn", this.samplingPlanIdColumn);
            delTestingLevels.setProperty("samplingplanversionidcolumn", this.samplingPlanVersionIdColumn);
            testingLevelActionBlock.setActionClass("deltestinglevel", DelTestingLevel.class.getName(), delTestingLevels);
        }
        this.getActionProcessor().processActionBlock(testingLevelActionBlock);
        if (addTestingLevelProps != null) {
            tablePropsJsonString = this.replaceAutoLevelIds(tablePropsJsonString, addLevelIdList);
        }
        try {
            tablePropsJsonObject = new JSONObject(tablePropsJsonString);
        }
        catch (JSONException e) {
            throw new SapphireException("Cannot parse table property list JSON", e);
        }
        PropertyList tableProps = new PropertyList(tablePropsJsonObject);
        this.parseTableProps(tableProps, sdiWorkItemColumnCollection, specParamitemColumnCollection, deleteLevelIdList, addLevelIdList);
    }

    /*
     * Could not resolve type clashes
     */
    private void parseTableProps(PropertyList tableProps, PropertyListCollection sdiWorkItemColumnCollection, PropertyListCollection specParamitemColumnCollection, List<String> deleteLevelIdList, List<String> addLevelIdList) throws SapphireException {
        int j;
        ActionBlock rowCollectionActionBlock = new ActionBlock();
        PropertyListCollection rowCollection = tableProps.getCollection("rowcollection");
        List<Object> detailItemList = new ArrayList();
        List<Object> originalDetailItemList = new ArrayList();
        ArrayList<List<String>> addedTestingLevelItemKeyList = new ArrayList<List<String>>();
        ArrayList<List<String>> deletedTestingLevelItemKeyList = new ArrayList<List<String>>();
        HashSet<List<String>> addedWorkItemGroupSet = new HashSet<List<String>>();
        StringBuilder addedWorkItemIds = new StringBuilder();
        StringBuilder addedWorkItemVersionIds = new StringBuilder();
        StringBuilder addedWorkItemApplyWorkItem = new StringBuilder();
        StringBuilder addedWorkItemUserSequence = new StringBuilder();
        PropertyListCollection addedWorkItemColumnCollection = new PropertyListCollection();
        StringBuilder editWorkItemWorkItemId = new StringBuilder();
        StringBuilder editWorkItemWorkItemVersionId = new StringBuilder();
        StringBuilder editWorkItemWorkItemInstance = new StringBuilder();
        StringBuilder editWorkItemUserSequence = new StringBuilder();
        PropertyListCollection editSDIWorkItemColumnCollection = new PropertyListCollection();
        PropertyListCollection editSpecParamItemColumnCollection = new PropertyListCollection();
        StringBuilder addSPDetailItemsWorkItemId = new StringBuilder();
        StringBuilder addSPDetailItemsWorkItemVersionId = new StringBuilder();
        StringBuilder addSPDetailItemsSamplingPlanDetailNo = new StringBuilder();
        StringBuilder deleteSPDetailItemsWorkItemId = new StringBuilder();
        StringBuilder deleteSPDetailItemsWorkItemVersionId = new StringBuilder();
        StringBuilder deleteSPDetailItemsSamplingPlanDetailNo = new StringBuilder();
        StringBuilder deletedWorkItemIds = new StringBuilder();
        StringBuilder deletedWorkItemVersionIds = new StringBuilder();
        StringBuilder addOrEditSpecParamListIds = new StringBuilder();
        StringBuilder addOrEditSpecParamListVersionIds = new StringBuilder();
        StringBuilder addOrEditSpecVariantIds = new StringBuilder();
        StringBuilder addOrEditSpecParamIds = new StringBuilder();
        StringBuilder addOrEditSpecParamTypes = new StringBuilder();
        StringBuilder addOrEditSpecDataTypes = new StringBuilder();
        StringBuilder addOrEditSpecHighLimit3 = new StringBuilder();
        StringBuilder addOrEditSpecHighLimit2 = new StringBuilder();
        StringBuilder addOrEditSpecHighLimit1 = new StringBuilder();
        StringBuilder addOrEditSpecTargets = new StringBuilder();
        StringBuilder addOrEditSpecLowLimit1 = new StringBuilder();
        StringBuilder addOrEditSpecLowLimit2 = new StringBuilder();
        StringBuilder addOrEditSpecLowLimit3 = new StringBuilder();
        StringBuilder deleteSpecParamParamListIds = new StringBuilder();
        StringBuilder deleteSpecParamParamListVersionIds = new StringBuilder();
        StringBuilder deleteSpecParamVariantIds = new StringBuilder();
        StringBuilder deleteSpecParamParamIds = new StringBuilder();
        StringBuilder deleteSpecParamParamTypes = new StringBuilder();
        HashMap workItemGroupItemMap = new HashMap();
        for (j = 0; j < sdiWorkItemColumnCollection.size(); ++j) {
            PropertyList sdiWorkItemColumnProps = sdiWorkItemColumnCollection.getPropertyList(j);
            PropertyList editWorkItemColumn = new PropertyList();
            editWorkItemColumn.setProperty("columnid", sdiWorkItemColumnProps.getProperty("columnid"));
            editWorkItemColumn.setProperty("columnvalue", "");
            PropertyList addWorkItemColumn = new PropertyList();
            addWorkItemColumn.setProperty("columnid", sdiWorkItemColumnProps.getProperty("columnid"));
            addWorkItemColumn.setProperty("columnvalue", "");
            editSDIWorkItemColumnCollection.add(editWorkItemColumn);
            addedWorkItemColumnCollection.add(addWorkItemColumn);
        }
        for (j = 0; j < specParamitemColumnCollection.size(); ++j) {
            PropertyList specParamItemColumnProps = specParamitemColumnCollection.getPropertyList(j);
            String columnId = specParamItemColumnProps.getProperty("columnid");
            if (AddOrEditSpec.BLOCKED_COLUMNS_LIST.contains(columnId.toLowerCase())) continue;
            PropertyList editSpecParamItemColumn = new PropertyList();
            editSpecParamItemColumn.setProperty("columnid", columnId);
            editSpecParamItemColumn.setProperty("columnvalue", "");
            editSpecParamItemColumnCollection.add(editSpecParamItemColumn);
        }
        for (int i = 0; i < rowCollection.size(); ++i) {
            List<String> detailItemItemKey;
            String testingLevelId;
            String originalTestingLevels;
            String testingLevels;
            boolean foundChanges;
            PropertyList rowProps = rowCollection.getPropertyList(i);
            String groupId = rowProps.getProperty("groupid");
            String workItemId = rowProps.getProperty("workitemid");
            String workItemVersionId = rowProps.getProperty("workitemversionid");
            String isFirst = rowProps.getProperty("isfirst");
            String paramListId = rowProps.getProperty("paramlistid");
            String paramListVersionId = rowProps.getProperty("paramlistversionid");
            String currentParamListVersionId = rowProps.getProperty("currentparamlistversionid");
            String variantId = rowProps.getProperty("variantid");
            String paramId = rowProps.getProperty("paramid");
            String paramType = rowProps.getProperty("paramtype");
            String dataTypes = rowProps.getProperty("datatypes");
            String workItemStatus = rowProps.getProperty("workitemstatus");
            String specParamItemStatus = rowProps.getProperty("specparamitemstatus");
            String highLimit3 = rowProps.getProperty("highlimit3");
            String highLimit2 = rowProps.getProperty("highlimit2");
            String highLimit1 = rowProps.getProperty("highlimit1");
            String target = rowProps.getProperty("target");
            String lowLimit1 = rowProps.getProperty("lowlimit1");
            String lowLimit2 = rowProps.getProperty("lowlimit2");
            String lowLimit3 = rowProps.getProperty("lowlimit3");
            String originalHighLimit3 = rowProps.getProperty("highlimit3_original");
            String originalHighLimit2 = rowProps.getProperty("highlimit2_original");
            String originalHighLimit1 = rowProps.getProperty("highlimit1_original");
            String originalTarget = rowProps.getProperty("target_original");
            String originalLowLimit1 = rowProps.getProperty("lowlimit1_original");
            String originalLowLimit2 = rowProps.getProperty("lowlimit2_original");
            String originalLowLimit3 = rowProps.getProperty("lowlimit3_original");
            String workItemUserSequence = rowProps.getProperty("workitemsequence");
            String originalWorkItemUserSequence = rowProps.getProperty("workitemsequence_original");
            List<String> workItemKey = Arrays.asList(workItemId, workItemVersionId);
            List workItemGroupItemList = (ArrayList<List<String>>)workItemGroupItemMap.get(groupId);
            if (isFirst.toLowerCase().startsWith("y")) {
                if (workItemGroupItemList == null) {
                    workItemGroupItemList = new ArrayList<List<String>>();
                    workItemGroupItemList.add(workItemKey);
                    workItemGroupItemMap.put(groupId, workItemGroupItemList);
                } else if (!workItemGroupItemList.contains(workItemKey)) {
                    workItemGroupItemList.add(workItemKey);
                }
            }
            if (!(specParamItemStatus.toLowerCase().startsWith(DELETE_STATUS) || specParamItemStatus.toLowerCase().startsWith(VIRTUAL_STATUS) || paramListId.isEmpty() || paramListVersionId.isEmpty() || variantId.isEmpty() || paramId.isEmpty() || paramType.isEmpty())) {
                PropertyList editSpecParamItemColumn;
                int k;
                String columnId;
                PropertyList specParamItemColumnProps;
                int j2;
                foundChanges = false;
                if (!(highLimit3.equals(originalHighLimit3) && highLimit2.equals(originalHighLimit2) && highLimit1.equals(originalHighLimit1) && target.equals(originalTarget) && lowLimit1.equals(originalLowLimit1) && lowLimit2.equals(originalLowLimit2) && lowLimit3.equals(originalLowLimit3))) {
                    foundChanges = true;
                }
                block5: for (j2 = 0; j2 < specParamitemColumnCollection.size() && !foundChanges; ++j2) {
                    specParamItemColumnProps = specParamitemColumnCollection.getPropertyList(j2);
                    columnId = specParamItemColumnProps.getProperty("columnid");
                    for (k = 0; k < editSpecParamItemColumnCollection.size(); ++k) {
                        editSpecParamItemColumn = editSpecParamItemColumnCollection.getPropertyList(k);
                        if (!editSpecParamItemColumn.getProperty("columnid").equals(columnId) || rowProps.getProperty(columnId).equals(rowProps.getProperty(columnId + "_original")) && !workItemStatus.toLowerCase().startsWith(ADD_STATUS)) continue;
                        foundChanges = true;
                        continue block5;
                    }
                }
                if (foundChanges) {
                    addOrEditSpecParamListIds.append(";").append(paramListId);
                    addOrEditSpecParamListVersionIds.append(";").append(currentParamListVersionId);
                    addOrEditSpecVariantIds.append(";").append(variantId);
                    addOrEditSpecParamIds.append(";").append(paramId);
                    addOrEditSpecParamTypes.append(";").append(paramType);
                    addOrEditSpecDataTypes.append(";").append(dataTypes);
                    addOrEditSpecHighLimit3.append(";").append(highLimit3.replaceAll(";", "|"));
                    addOrEditSpecHighLimit2.append(";").append(highLimit2.replaceAll(";", "|"));
                    addOrEditSpecHighLimit1.append(";").append(highLimit1.replaceAll(";", "|"));
                    addOrEditSpecTargets.append(";").append(target.replaceAll(";", "|"));
                    addOrEditSpecLowLimit1.append(";").append(lowLimit1.replaceAll(";", "|"));
                    addOrEditSpecLowLimit2.append(";").append(lowLimit2.replaceAll(";", "|"));
                    addOrEditSpecLowLimit3.append(";").append(lowLimit3.replaceAll(";", "|"));
                    block7: for (j2 = 0; j2 < specParamitemColumnCollection.size(); ++j2) {
                        specParamItemColumnProps = specParamitemColumnCollection.getPropertyList(j2);
                        columnId = specParamItemColumnProps.getProperty("columnid");
                        for (k = 0; k < editSpecParamItemColumnCollection.size(); ++k) {
                            editSpecParamItemColumn = editSpecParamItemColumnCollection.getPropertyList(k);
                            if (!editSpecParamItemColumn.getProperty("columnid").equals(columnId)) continue;
                            String columnValue = editSpecParamItemColumn.getProperty("columnvalue");
                            columnValue = columnValue + ";" + rowProps.getProperty(columnId);
                            editSpecParamItemColumn.setProperty("columnvalue", columnValue);
                            continue block7;
                        }
                    }
                }
            }
            if (!(!specParamItemStatus.toLowerCase().startsWith(DELETE_STATUS) || paramListId.isEmpty() || paramListVersionId.isEmpty() || variantId.isEmpty() || paramId.isEmpty() || paramType.isEmpty())) {
                deleteSpecParamParamListIds.append(";").append(paramListId);
                deleteSpecParamParamListVersionIds.append(";").append(paramListVersionId);
                deleteSpecParamVariantIds.append(";").append(variantId);
                deleteSpecParamParamIds.append(";").append(paramId);
                deleteSpecParamParamTypes.append(";").append(paramType);
            }
            if (isFirst.toLowerCase().startsWith("y") && workItemStatus.toLowerCase().startsWith(ADD_STATUS)) {
                if (groupId.isEmpty()) {
                    addedWorkItemIds.append(";").append(workItemId);
                    addedWorkItemVersionIds.append(";").append(workItemVersionId.length() == 0 ? "C" : workItemVersionId);
                    addedWorkItemApplyWorkItem.append(";").append("N");
                    addedWorkItemUserSequence.append(";").append(workItemUserSequence);
                    for (int j3 = 0; j3 < sdiWorkItemColumnCollection.size(); ++j3) {
                        PropertyList sdiWorkItemColumnProps = sdiWorkItemColumnCollection.getPropertyList(j3);
                        String columnId = sdiWorkItemColumnProps.getProperty("columnid");
                        for (int k = 0; k < addedWorkItemColumnCollection.size(); ++k) {
                            PropertyList addWorkItemColumn = addedWorkItemColumnCollection.getPropertyList(k);
                            if (!addWorkItemColumn.getProperty("columnid").equals(columnId)) continue;
                            String columnValue = addWorkItemColumn.getProperty("columnvalue");
                            columnValue = !rowProps.getProperty(columnId).isEmpty() ? columnValue + ";" + rowProps.getProperty(columnId) : columnValue + ";";
                            addWorkItemColumn.setProperty("columnvalue", columnValue);
                        }
                    }
                } else {
                    if (!addedWorkItemGroupSet.contains(Arrays.asList(groupId, "1"))) {
                        addedWorkItemIds.append(";").append(groupId);
                        addedWorkItemVersionIds.append(";").append("1");
                        addedWorkItemApplyWorkItem.append(";").append("N");
                        addedWorkItemUserSequence.append(";").append(workItemUserSequence);
                        addedWorkItemGroupSet.add(Arrays.asList(groupId, "1"));
                    }
                    workItemStatus = "";
                }
            }
            if (isFirst.toLowerCase().startsWith("y") && !workItemStatus.toLowerCase().startsWith(ADD_STATUS) && !workItemStatus.toLowerCase().startsWith(DELETE_STATUS)) {
                foundChanges = false;
                StringBuilder editWorkItemUserSequenceBackup = new StringBuilder(editWorkItemUserSequence);
                if (!originalWorkItemUserSequence.equals(workItemUserSequence)) {
                    editWorkItemUserSequence.append(";").append(workItemUserSequence);
                    foundChanges = true;
                } else {
                    editWorkItemUserSequence.append(";").append(originalWorkItemUserSequence);
                }
                String editWorkItemColumnCollectionJson = editSDIWorkItemColumnCollection.toJSONString();
                for (int j4 = 0; j4 < sdiWorkItemColumnCollection.size(); ++j4) {
                    PropertyList sdiWorkItemColumnProps = sdiWorkItemColumnCollection.getPropertyList(j4);
                    String columnId = sdiWorkItemColumnProps.getProperty("columnid");
                    for (int k = 0; k < editSDIWorkItemColumnCollection.size(); ++k) {
                        PropertyList editWorkItemColumn = editSDIWorkItemColumnCollection.getPropertyList(k);
                        if (!editWorkItemColumn.getProperty("columnid").equals(columnId)) continue;
                        String columnValue = editWorkItemColumn.getProperty("columnvalue");
                        if (!rowProps.getProperty(columnId).equals(rowProps.getProperty(columnId + "_original"))) {
                            columnValue = columnValue + ";" + rowProps.getProperty(columnId);
                            foundChanges = true;
                        } else {
                            columnValue = columnValue + ";" + rowProps.getProperty(columnId + "_original");
                        }
                        editWorkItemColumn.setProperty("columnvalue", columnValue);
                    }
                }
                if (foundChanges) {
                    editWorkItemWorkItemId.append(";").append(workItemId);
                    editWorkItemWorkItemVersionId.append(";").append(workItemVersionId.length() == 0 ? "C" : workItemVersionId);
                    editWorkItemWorkItemInstance.append(";").append("1");
                } else {
                    editSDIWorkItemColumnCollection = new PropertyListCollection();
                    try {
                        editSDIWorkItemColumnCollection.setJSONString(editWorkItemColumnCollectionJson);
                    }
                    catch (JSONException e) {
                        throw new SapphireException("Cannot parse JSON: " + editWorkItemColumnCollectionJson, e);
                    }
                    editWorkItemUserSequence = new StringBuilder(editWorkItemUserSequenceBackup);
                }
            }
            if (isFirst.toLowerCase().startsWith("y") && workItemStatus.toLowerCase().startsWith(DELETE_STATUS)) {
                deletedWorkItemIds.append(";").append(workItemId);
                deletedWorkItemVersionIds.append(";").append(workItemVersionId.length() == 0 ? "C" : workItemVersionId);
                if (!groupId.isEmpty()) {
                    workItemGroupItemList = (List)workItemGroupItemMap.get(groupId);
                    workItemGroupItemList.remove(workItemKey);
                }
            }
            if ((testingLevels = rowProps.getProperty("testinglevellist")).equals(originalTestingLevels = rowProps.getProperty("testinglevellist_original")) || workItemStatus.toLowerCase().startsWith(DELETE_STATUS)) continue;
            detailItemList = testingLevels.length() > 0 ? Arrays.asList(testingLevels.split(";")) : Collections.emptyList();
            originalDetailItemList = originalTestingLevels.length() > 0 ? Arrays.asList(originalTestingLevels.split(";")) : Collections.emptyList();
            for (String testingLevelKey : detailItemList) {
                String samplingPlanDetailNo = testingLevelKey.split("\\|")[1];
                testingLevelId = testingLevelKey.split("\\|")[2];
                if (originalTestingLevels.contains(testingLevelKey) || deleteLevelIdList.contains(testingLevelId) || addedTestingLevelItemKeyList.contains(detailItemItemKey = Arrays.asList(samplingPlanDetailNo, workItemId, workItemVersionId))) continue;
                addSPDetailItemsWorkItemId.append(";").append(workItemId);
                addSPDetailItemsWorkItemVersionId.append(";").append(workItemVersionId.length() == 0 ? "C" : workItemVersionId);
                addSPDetailItemsSamplingPlanDetailNo.append(";").append(samplingPlanDetailNo);
                addedTestingLevelItemKeyList.add(detailItemItemKey);
            }
            for (String testingLevelKey : originalDetailItemList) {
                String samplingPlanDetailNo = testingLevelKey.split("\\|")[1];
                testingLevelId = testingLevelKey.split("\\|")[2];
                if (detailItemList.contains(testingLevelKey) || deleteLevelIdList.contains(testingLevelId) || addLevelIdList.contains(testingLevelId) || deletedTestingLevelItemKeyList.contains(detailItemItemKey = Arrays.asList(samplingPlanDetailNo, workItemId, workItemVersionId))) continue;
                deleteSPDetailItemsWorkItemId.append(";").append(workItemId);
                deleteSPDetailItemsWorkItemVersionId.append(";").append(workItemVersionId.length() == 0 ? "C" : workItemVersionId);
                deleteSPDetailItemsSamplingPlanDetailNo.append(";").append(samplingPlanDetailNo);
                deletedTestingLevelItemKeyList.add(detailItemItemKey);
            }
        }
        Set workItemGroupItemSet = workItemGroupItemMap.entrySet();
        for (Map.Entry workItemGroupItem : workItemGroupItemSet) {
            List workItemGroupItemList = (List)workItemGroupItem.getValue();
            if (!workItemGroupItemList.isEmpty()) continue;
            deletedWorkItemIds.append(";").append((String)workItemGroupItem.getKey());
            deletedWorkItemVersionIds.append(";").append("1");
        }
        this.deleteSDIWorkItems(rowCollectionActionBlock, "delsdiworkitem", deletedWorkItemIds, deletedWorkItemVersionIds);
        this.deleteSDIWorkItems(rowCollectionActionBlock, "delsdiworkitemitem", deletedWorkItemIds, deletedWorkItemVersionIds);
        this.addSDIWorkItems(rowCollectionActionBlock, addedWorkItemIds, addedWorkItemVersionIds, addedWorkItemApplyWorkItem, addedWorkItemUserSequence, addedWorkItemColumnCollection);
        this.editSDIWorkItems(rowCollectionActionBlock, editWorkItemWorkItemId, editWorkItemWorkItemVersionId, editWorkItemWorkItemInstance, editWorkItemUserSequence, editSDIWorkItemColumnCollection);
        this.deleteSpecParamItems(rowCollectionActionBlock, deleteSpecParamParamListIds, deleteSpecParamParamListVersionIds, deleteSpecParamVariantIds, deleteSpecParamParamIds, deleteSpecParamParamTypes);
        this.addOrEditSpec(rowCollectionActionBlock, editSpecParamItemColumnCollection, addOrEditSpecParamListIds, addOrEditSpecParamListVersionIds, addOrEditSpecVariantIds, addOrEditSpecParamIds, addOrEditSpecParamTypes, addOrEditSpecDataTypes, addOrEditSpecHighLimit3, addOrEditSpecHighLimit2, addOrEditSpecHighLimit1, addOrEditSpecTargets, addOrEditSpecLowLimit1, addOrEditSpecLowLimit2, addOrEditSpecLowLimit3);
        this.addSamplingPlanDetailItems(rowCollectionActionBlock, addSPDetailItemsWorkItemId, addSPDetailItemsWorkItemVersionId, addSPDetailItemsSamplingPlanDetailNo);
        this.deleteSamplingPlanDetailItems(rowCollectionActionBlock, deleteSPDetailItemsWorkItemId, deleteSPDetailItemsWorkItemVersionId, deleteSPDetailItemsSamplingPlanDetailNo);
        this.getActionProcessor().processActionBlock(rowCollectionActionBlock);
    }

    private void addSDIWorkItems(ActionBlock rowCollectionActionBlock, StringBuilder addedWorkItemIds, StringBuilder addedWorkItemVersionIds, StringBuilder addedWorkItemApplyWorkItem, StringBuilder addedWorkItemUserSequence, PropertyListCollection addedWorkItemColumnCollection) throws ActionException {
        if (addedWorkItemIds.length() > 0 && addedWorkItemVersionIds.length() > 0) {
            PropertyList addSDIWorkItemProps = new PropertyList();
            addSDIWorkItemProps.setProperty("sdcid", this.sdcId);
            addSDIWorkItemProps.setProperty("keyid1", this.keyId1);
            if (this.keyId2.length() > 0) {
                addSDIWorkItemProps.setProperty("keyid2", this.keyId2);
            }
            if (this.keyId3.length() > 0) {
                addSDIWorkItemProps.setProperty("keyid3", this.keyId3);
            }
            addSDIWorkItemProps.setProperty("workitemid", addedWorkItemIds.substring(1));
            addSDIWorkItemProps.setProperty("workitemversionid", addedWorkItemVersionIds.substring(1));
            addSDIWorkItemProps.setProperty("applyworkitem", addedWorkItemApplyWorkItem.substring(1));
            addSDIWorkItemProps.setProperty("autousersequence", "N");
            addSDIWorkItemProps.setProperty("usersequence", addedWorkItemUserSequence.substring(1));
            for (int i = 0; i < addedWorkItemColumnCollection.size(); ++i) {
                String columnValue;
                PropertyList addSDIWorkItemColumn = addedWorkItemColumnCollection.getPropertyList(i);
                if (ILLEGAL_COLUMN_IDS_LIST.contains(addSDIWorkItemColumn.getProperty("columnid").toLowerCase()) || (columnValue = addSDIWorkItemColumn.getProperty("columnvalue")).isEmpty()) continue;
                addSDIWorkItemProps.setProperty(addSDIWorkItemColumn.getProperty("columnid"), columnValue.substring(1));
            }
            rowCollectionActionBlock.setAction("addsdiworkitem", "AddSDIWorkItem", "1", addSDIWorkItemProps);
            if (this.enableTestingLevels) {
                addSDIWorkItemProps.setProperty("samplingplanidcolumn", this.samplingPlanIdColumn);
                addSDIWorkItemProps.setProperty("samplingplanversionidcolumn", this.samplingPlanVersionIdColumn);
                addSDIWorkItemProps.setProperty("automaticversioning", this.automaticSamplingPlanVersioning);
                rowCollectionActionBlock.setActionClass("addspworkitem", AddSPWorkItem.class.getName(), addSDIWorkItemProps);
            }
        }
    }

    private void deleteSDIWorkItems(ActionBlock rowCollectionActionBlock, String actionName, StringBuilder deletedWorkItemIds, StringBuilder deletedWorkItemVersionIds) throws ActionException {
        if (deletedWorkItemIds.length() > 0) {
            PropertyList deletedSDIWorkItemProps = new PropertyList();
            deletedSDIWorkItemProps.setProperty("sdcid", this.sdcId);
            deletedSDIWorkItemProps.setProperty("keyid1", this.keyId1);
            if (this.keyId2.length() > 0) {
                deletedSDIWorkItemProps.setProperty("keyid2", this.keyId2);
            }
            if (this.keyId3.length() > 0) {
                deletedSDIWorkItemProps.setProperty("keyid3", this.keyId3);
            }
            deletedSDIWorkItemProps.setProperty("workitemid", deletedWorkItemIds.substring(1));
            deletedSDIWorkItemProps.setProperty("workitemversionid", deletedWorkItemVersionIds.substring(1));
            rowCollectionActionBlock.setAction(actionName, "DeleteSDIWorkItem", "1", deletedSDIWorkItemProps);
            if (this.enableTestingLevels) {
                deletedSDIWorkItemProps.setProperty("samplingplanidcolumn", this.samplingPlanIdColumn);
                deletedSDIWorkItemProps.setProperty("samplingplanversionidcolumn", this.samplingPlanVersionIdColumn);
                deletedSDIWorkItemProps.setProperty("automaticversioning", this.automaticSamplingPlanVersioning);
                rowCollectionActionBlock.setActionClass("sp_" + actionName, DeleteSPWorkItem.class.getName(), deletedSDIWorkItemProps);
            }
        }
    }

    private void editSDIWorkItems(ActionBlock rowCollectionActionBlock, StringBuilder editWorkItemWorkItemId, StringBuilder editWorkItemWorkItemVersionId, StringBuilder editWorkItemWorkItemInstance, StringBuilder editWorkItemUserSequence, PropertyListCollection editSDIWorkItemColumnCollection) throws ActionException {
        if (editWorkItemWorkItemId.length() > 0) {
            PropertyList editSDIWorkItemProps = new PropertyList();
            editSDIWorkItemProps.setProperty("sdcid", this.sdcId);
            editSDIWorkItemProps.setProperty("keyid1", this.keyId1);
            if (this.keyId2.length() > 0) {
                editSDIWorkItemProps.setProperty("keyid2", this.keyId2);
            }
            if (this.keyId3.length() > 0) {
                editSDIWorkItemProps.setProperty("keyid3", this.keyId3);
            }
            editSDIWorkItemProps.setProperty("workitemid", editWorkItemWorkItemId.substring(1));
            editSDIWorkItemProps.setProperty("workiteminstance", editWorkItemWorkItemInstance.substring(1));
            editSDIWorkItemProps.setProperty("usersequence", editWorkItemUserSequence.substring(1));
            for (int i = 0; i < editSDIWorkItemColumnCollection.size(); ++i) {
                String columnValue;
                PropertyList editSDIWorkItemColumn = editSDIWorkItemColumnCollection.getPropertyList(i);
                if (ILLEGAL_COLUMN_IDS_LIST.contains(editSDIWorkItemColumn.getProperty("columnid").toLowerCase()) || (columnValue = editSDIWorkItemColumn.getProperty("columnvalue")).isEmpty()) continue;
                editSDIWorkItemProps.setProperty(editSDIWorkItemColumn.getProperty("columnid"), columnValue.substring(1));
            }
            rowCollectionActionBlock.setAction("editdiworkitem", "EditSDIWorkItem", "1", editSDIWorkItemProps);
            if (this.enableTestingLevels) {
                editSDIWorkItemProps.setProperty("workitemversionid", editWorkItemWorkItemVersionId.substring(1));
                editSDIWorkItemProps.setProperty("samplingplanidcolumn", this.samplingPlanIdColumn);
                editSDIWorkItemProps.setProperty("samplingplanversionidcolumn", this.samplingPlanVersionIdColumn);
                editSDIWorkItemProps.setProperty("automaticversioning", this.automaticSamplingPlanVersioning);
                rowCollectionActionBlock.setActionClass("editspworkitem", EditSPWorkItem.class.getName(), editSDIWorkItemProps);
            }
        }
    }

    private void deleteSpecParamItems(ActionBlock rowCollectionActionBlock, StringBuilder deleteSpecParamParamListIds, StringBuilder deleteSpecParamParamListVersionIds, StringBuilder deleteSpecParamVariantIds, StringBuilder deleteSpecParamParamIds, StringBuilder deleteSpecParamParamTypes) throws ActionException {
        if (deleteSpecParamParamListIds.length() > 0) {
            PropertyList deleteSpecParamProps = new PropertyList();
            deleteSpecParamProps.setProperty("sdcid", this.sdcId);
            deleteSpecParamProps.setProperty("keyid1", this.keyId1);
            deleteSpecParamProps.setProperty("keyid2", this.keyId2);
            deleteSpecParamProps.setProperty("keyid3", this.keyId3);
            deleteSpecParamProps.setProperty("paramlistid", deleteSpecParamParamListIds.substring(1));
            deleteSpecParamProps.setProperty("paramlistversionid", deleteSpecParamParamListVersionIds.substring(1));
            deleteSpecParamProps.setProperty("variantid", deleteSpecParamVariantIds.substring(1));
            deleteSpecParamProps.setProperty("paramid", deleteSpecParamParamIds.substring(1));
            deleteSpecParamProps.setProperty("paramtype", deleteSpecParamParamTypes.substring(1));
            deleteSpecParamProps.setProperty("automaticversioning", this.automaticSpecVersioning);
            deleteSpecParamProps.setProperty("specidcolumn", this.specIdColumn);
            deleteSpecParamProps.setProperty("specversionidcolumn", this.specVersionIdColumn);
            rowCollectionActionBlock.setActionClass("deletespecparam", DeleteSpecParam.class.getName(), deleteSpecParamProps);
        }
    }

    private void addOrEditSpec(ActionBlock rowCollectionActionBlock, PropertyListCollection editSpecParamItemColumnCollection, StringBuilder addOrEditSpecParamListIds, StringBuilder addOrEditSpecParamListVersionIds, StringBuilder addOrEditSpecVariantIds, StringBuilder addOrEditSpecParamIds, StringBuilder addOrEditSpecParamTypes, StringBuilder addOrEditSpecDataTypes, StringBuilder addOrEditSpecHighLimit3, StringBuilder addOrEditSpecHighLimit2, StringBuilder addOrEditSpecHighLimit1, StringBuilder addOrEditSpecTargets, StringBuilder addOrEditSpecLowLimit1, StringBuilder addOrEditSpecLowLimit2, StringBuilder addOrEditSpecLowLimit3) throws ActionException {
        if (addOrEditSpecParamListIds.length() > 0) {
            PropertyList addOrEditSpecProps = new PropertyList();
            addOrEditSpecProps.setProperty("sdcid", this.sdcId);
            addOrEditSpecProps.setProperty("keyid1", this.keyId1);
            addOrEditSpecProps.setProperty("keyid2", this.keyId2);
            addOrEditSpecProps.setProperty("keyid3", this.keyId3);
            addOrEditSpecProps.setProperty("paramlistid", addOrEditSpecParamListIds.substring(1));
            addOrEditSpecProps.setProperty("paramlistversionid", addOrEditSpecParamListVersionIds.substring(1));
            addOrEditSpecProps.setProperty("variantid", addOrEditSpecVariantIds.substring(1));
            addOrEditSpecProps.setProperty("paramid", addOrEditSpecParamIds.substring(1));
            addOrEditSpecProps.setProperty("paramtype", addOrEditSpecParamTypes.substring(1));
            addOrEditSpecProps.setProperty("datatypes", addOrEditSpecDataTypes.substring(1));
            addOrEditSpecProps.setProperty("highlimit3", addOrEditSpecHighLimit3.substring(1));
            addOrEditSpecProps.setProperty("highlimit2", addOrEditSpecHighLimit2.substring(1));
            addOrEditSpecProps.setProperty("highlimit1", addOrEditSpecHighLimit1.substring(1));
            addOrEditSpecProps.setProperty("target", addOrEditSpecTargets.substring(1));
            addOrEditSpecProps.setProperty("lowlimit1", addOrEditSpecLowLimit1.substring(1));
            addOrEditSpecProps.setProperty("lowlimit2", addOrEditSpecLowLimit2.substring(1));
            addOrEditSpecProps.setProperty("lowlimit3", addOrEditSpecLowLimit3.substring(1));
            addOrEditSpecProps.setProperty("automaticversioning", this.automaticSpecVersioning);
            addOrEditSpecProps.setProperty("specidcolumn", this.specIdColumn);
            addOrEditSpecProps.setProperty("specversionidcolumn", this.specVersionIdColumn);
            addOrEditSpecProps.setProperty("highlimit3type", this.highLimit3Type);
            addOrEditSpecProps.setProperty("highlimit2type", this.highLimit2Type);
            addOrEditSpecProps.setProperty("highlimit1type", this.highLimit1Type);
            addOrEditSpecProps.setProperty("inspectype", this.inSpecType);
            addOrEditSpecProps.setProperty("targettype", this.targetType);
            addOrEditSpecProps.setProperty("lowlimit1type", this.lowLimit1Type);
            addOrEditSpecProps.setProperty("lowlimit2type", this.lowLimit2Type);
            addOrEditSpecProps.setProperty("lowlimit3type", this.lowLimit3Type);
            for (int i = 0; i < editSpecParamItemColumnCollection.size(); ++i) {
                PropertyList editSpecParamItemColumn = editSpecParamItemColumnCollection.getPropertyList(i);
                String columnValue = editSpecParamItemColumn.getProperty("columnvalue");
                if (columnValue.isEmpty()) continue;
                addOrEditSpecProps.setProperty(editSpecParamItemColumn.getProperty("columnid"), columnValue.substring(1));
            }
            rowCollectionActionBlock.setActionClass("addoreditspec", AddOrEditSpec.class.getName(), addOrEditSpecProps);
        }
    }

    private void addSamplingPlanDetailItems(ActionBlock rowCollectionActionBlock, StringBuilder addSPDetailItemsWorkItemId, StringBuilder addSPDetailItemsWorkItemVersionId, StringBuilder addSPDetailItemsSamplingPlanDetailNo) throws ActionException {
        if (addSPDetailItemsWorkItemId.length() > 0) {
            PropertyList addSPDetailItemProps = new PropertyList();
            addSPDetailItemProps.setProperty("sdcid", this.sdcId);
            addSPDetailItemProps.setProperty("keyid1", this.keyId1);
            addSPDetailItemProps.setProperty("keyid2", this.keyId2);
            addSPDetailItemProps.setProperty("keyid3", this.keyId3);
            addSPDetailItemProps.setProperty("samplingplandetailno", addSPDetailItemsSamplingPlanDetailNo.substring(1));
            addSPDetailItemProps.setProperty("workitemid", addSPDetailItemsWorkItemId.substring(1));
            addSPDetailItemProps.setProperty("workitemversionid", addSPDetailItemsWorkItemVersionId.substring(1));
            addSPDetailItemProps.setProperty("samplingplanidcolumn", this.samplingPlanIdColumn);
            addSPDetailItemProps.setProperty("samplingplanversionidcolumn", this.samplingPlanVersionIdColumn);
            addSPDetailItemProps.setProperty("automaticversioning", this.automaticSamplingPlanVersioning);
            rowCollectionActionBlock.setActionClass("addspdetailitem", AddSPDetailItem.class.getName(), addSPDetailItemProps);
        }
    }

    private void deleteSamplingPlanDetailItems(ActionBlock rowCollectionActionBlock, StringBuilder deleteSPDetailItemsWorkItemId, StringBuilder deleteSPDetailItemsWorkItemVersionId, StringBuilder deleteSPDetailItemsSamplingPlanDetailNo) throws ActionException {
        if (deleteSPDetailItemsWorkItemId.length() > 0) {
            PropertyList delSPDetailItemProps = new PropertyList();
            delSPDetailItemProps.setProperty("sdcid", this.sdcId);
            delSPDetailItemProps.setProperty("keyid1", this.keyId1);
            delSPDetailItemProps.setProperty("keyid2", this.keyId2);
            delSPDetailItemProps.setProperty("keyid3", this.keyId3);
            delSPDetailItemProps.setProperty("samplingplandetailno", deleteSPDetailItemsSamplingPlanDetailNo.substring(1));
            delSPDetailItemProps.setProperty("workitemid", deleteSPDetailItemsWorkItemId.substring(1));
            delSPDetailItemProps.setProperty("workitemversionid", deleteSPDetailItemsWorkItemVersionId.substring(1));
            delSPDetailItemProps.setProperty("samplingplanidcolumn", this.samplingPlanIdColumn);
            delSPDetailItemProps.setProperty("samplingplanversionidcolumn", this.samplingPlanVersionIdColumn);
            delSPDetailItemProps.setProperty("automaticversioning", this.automaticSamplingPlanVersioning);
            rowCollectionActionBlock.setActionClass("delspdetailitem", DelSPDetailItem.class.getName(), delSPDetailItemProps);
        }
    }

    private String replaceAutoLevelIds(String tablePropsJsonString, List<String> addLevelIdList) {
        PropertyList primaryProps = new PropertyList(this.getSdcProcessor().getSDCProperties(this.sdcId));
        String primaryTableId = primaryProps.getProperty("tableid");
        String primaryKeyColId1 = primaryProps.getProperty("keycolid1");
        String primaryKeyColId2 = primaryProps.getProperty("keycolid2");
        String primaryKeyColId3 = primaryProps.getProperty("keycolid3");
        String getSamplingPlanDetailNoSql = "SELECT s_samplingplandetailno FROM " + primaryTableId + " p JOIN s_spdetail d ON p." + this.samplingPlanIdColumn + " = d.s_samplingplanid AND p." + this.samplingPlanVersionIdColumn + " = d.s_samplingplanversionid WHERE d.levelid = ? AND p." + primaryKeyColId1 + " = ?" + (!primaryKeyColId2.isEmpty() ? " AND p." + primaryKeyColId2 + " = ?" : "") + (!primaryKeyColId3.isEmpty() ? " AND p." + primaryKeyColId3 + " = ?" : "");
        int paramCount = 2;
        if (this.keyId2.length() > 0) {
            ++paramCount;
        }
        if (this.keyId3.length() > 0) {
            ++paramCount;
        }
        Object[] getSamplingPlanDetailNoParams = new String[paramCount];
        Iterator<String> iterator = addLevelIdList.iterator();
        while (iterator.hasNext()) {
            String levelId;
            getSamplingPlanDetailNoParams[0] = levelId = iterator.next();
            getSamplingPlanDetailNoParams[1] = this.keyId1;
            if (this.keyId2.length() > 0) {
                getSamplingPlanDetailNoParams[2] = this.keyId2;
            }
            if (this.keyId3.length() > 0) {
                getSamplingPlanDetailNoParams[3] = this.keyId3;
            }
            DataSet getSamplingPlanDetailNumberDs = this.getQueryProcessor().getPreparedSqlDataSet(getSamplingPlanDetailNoSql, getSamplingPlanDetailNoParams);
            String samplingPlanDetailNo = getSamplingPlanDetailNumberDs.getBigDecimal(0, "s_samplingplandetailno").toPlainString();
            tablePropsJsonString = tablePropsJsonString.replaceAll("\\(auto\\)\\|" + levelId + "\\|", samplingPlanDetailNo + "|" + levelId);
        }
        return tablePropsJsonString;
    }

    static {
        ILLEGAL_COLUMN_IDS_LIST.add("sdcid");
        ILLEGAL_COLUMN_IDS_LIST.add("keyid1");
        ILLEGAL_COLUMN_IDS_LIST.add("keyid2");
        ILLEGAL_COLUMN_IDS_LIST.add("keyid3");
        ILLEGAL_COLUMN_IDS_LIST.add("workitemid");
        ILLEGAL_COLUMN_IDS_LIST.add("workitemversionid");
        ILLEGAL_COLUMN_IDS_LIST.add("workiteminstance");
        ILLEGAL_COLUMN_IDS_LIST.add("paramlistid");
        ILLEGAL_COLUMN_IDS_LIST.add("paramlistversionid");
        ILLEGAL_COLUMN_IDS_LIST.add("variantid");
        ILLEGAL_COLUMN_IDS_LIST.add("paramid");
        ILLEGAL_COLUMN_IDS_LIST.add("paramtype");
    }
}

