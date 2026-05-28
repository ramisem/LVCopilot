/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.simplespec.element;

import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.simplespec.util.SimpleSpecHelper;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.M18NUtil;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SimpleSpec
extends BaseElement {
    private static final String JS_VAR = "simpleSpec";
    private String sdcId;
    private String keyId1;
    private String keyId2;
    private String keyId3;
    private String primaryTableId;
    private String primaryKeyColId1;
    private String primaryKeyColId2;
    private String primaryKeyColId3;
    private M18NUtil m18client;
    private M18NUtil m18server;

    @Override
    public String getHtml() {
        boolean isReadOnly;
        String columnPropsJson;
        StringBuilder html = new StringBuilder();
        this.m18client = new M18NUtil(this.connectionInfo);
        this.m18server = new M18NUtil();
        this.sdcId = this.requestContext.getProperty("sdcid");
        this.keyId1 = this.requestContext.getProperty("keyid1");
        this.keyId2 = this.requestContext.getProperty("keyid2");
        this.keyId3 = this.requestContext.getProperty("keyid3");
        boolean formSuccess = this.requestContext.getProperty("__formsuccess").equals("true");
        String webPageId = this.requestContext.getProperty("page");
        PropertyList linkDefinitionProps = this.element.getPropertyListNotNull("linkdefinitionprops");
        PropertyList sdcLinkProps = linkDefinitionProps.getPropertyListNotNull("sdclinkprops");
        String linkId = sdcLinkProps.getProperty("linkid");
        if (!linkId.isEmpty()) {
            this.updateKeysFromLink(linkId);
        }
        String tablePropsJson = HttpUtil.decodeURIComponent(this.requestContext.getProperty("_" + this.elementid + "_tablepropsjson"));
        String testingLevelPropsJson = HttpUtil.decodeURIComponent(this.requestContext.getProperty("_" + this.elementid + "_testinglevelpropsjson"));
        PropertyListCollection configuredSDIWorkItemColumnCollection = this.element.getCollectionNotNull("sdiworkitemcolumncollection");
        PropertyListCollection configuredWorkItemColumnCollection = this.element.getCollectionNotNull("workitemcolumncollection");
        PropertyListCollection configuredParamListItemColumnCollection = this.element.getCollectionNotNull("paramlistitemcolumncollection");
        PropertyListCollection configuredSpecParamItemColumnCollection = this.element.getCollectionNotNull("specparamitemcolumncollection");
        try {
            SimpleSpecHelper simpleSpecHelper = new SimpleSpecHelper(this.getConnectionId());
            simpleSpecHelper.completeConfiguredColumns(configuredSDIWorkItemColumnCollection, "sdiworkitem");
            simpleSpecHelper.completeConfiguredColumns(configuredSpecParamItemColumnCollection, "specparamitems");
            simpleSpecHelper.completeConfiguredColumns(configuredWorkItemColumnCollection, "workitem");
            simpleSpecHelper.completeConfiguredColumns(configuredParamListItemColumnCollection, "paramlistitem");
            PropertyList columnProps = new PropertyList();
            columnProps.setProperty("workitemcolumncollection", configuredWorkItemColumnCollection);
            columnProps.setProperty("sdiworkitemcolumncollection", configuredSDIWorkItemColumnCollection);
            columnProps.setProperty("paramlistitemcolumncollection", configuredParamListItemColumnCollection);
            columnProps.setProperty("specparamitemcolumncollection", configuredSpecParamItemColumnCollection);
            columnPropsJson = columnProps.toJSONString(false);
        }
        catch (SapphireException ex) {
            this.logger.error("Error while creating simple spec element", ex);
            return "Error while creating simple spec element: " + ex.getMessage();
        }
        if (tablePropsJson.isEmpty() && testingLevelPropsJson.isEmpty() || formSuccess) {
            PropertyList primaryProps = new PropertyList(this.getSDCProcessor().getSDCProperties(this.sdcId));
            this.primaryTableId = primaryProps.getProperty("tableid");
            this.primaryKeyColId1 = primaryProps.getProperty("keycolid1");
            this.primaryKeyColId2 = primaryProps.getProperty("keycolid2");
            this.primaryKeyColId3 = primaryProps.getProperty("keycolid3");
            String primarySpecIdColumn = this.element.getProperty("specidcolumn");
            String primarySpecVersionIdColumn = this.element.getProperty("specversionidcolumn");
            String primarySamplingPlanIdColumn = this.element.getProperty("samplingplanidcolumn");
            String primarySamplingPlanVersionIdColumn = this.element.getProperty("samplingplanversionidcolumn");
            String filterParamType = this.element.getProperty("filterparamtype", "");
            try {
                DataSet getTestingLevelsDs;
                SimpleSpecHelper simpleSpecHelper = new SimpleSpecHelper(this.getConnectionId());
                DataSet sdiWorkItemDs = this.getSDIWorkItemDs(configuredSDIWorkItemColumnCollection);
                ArrayList<String> workItemIdList = new ArrayList<String>();
                ArrayList<String> workItemVersionIdList = new ArrayList<String>();
                for (int i = 0; i < sdiWorkItemDs.getRowCount(); ++i) {
                    workItemIdList.add(sdiWorkItemDs.getString(i, "workitemid"));
                    workItemVersionIdList.add(sdiWorkItemDs.getString(i, "workitemversionid"));
                }
                List<String> actualWorkItemVersionIdList = simpleSpecHelper.getActualWorkItemVersionIdList(workItemIdList, workItemVersionIdList);
                List<String> currentWorkItemIdList = simpleSpecHelper.getCurrentWorkItemIdList(workItemIdList, workItemVersionIdList);
                DataSet workItemItemsDs = simpleSpecHelper.getWorkItemItems(workItemIdList, actualWorkItemVersionIdList, configuredWorkItemColumnCollection);
                DataSet completeWorkItemDs = simpleSpecHelper.mergeSDIWorkItemsWithWorkItemItems(sdiWorkItemDs, workItemItemsDs);
                ArrayList<String> paramListIdList = new ArrayList<String>();
                ArrayList<String> paramListVersionIdList = new ArrayList<String>();
                ArrayList<String> variantIdList = new ArrayList<String>();
                for (int i = 0; i < completeWorkItemDs.getRowCount(); ++i) {
                    String paramListId = completeWorkItemDs.getString(i, "paramlistid", "");
                    String paramListVersionId = completeWorkItemDs.getString(i, "paramlistversionid", "");
                    String variantId = completeWorkItemDs.getString(i, "variantid", "");
                    if (paramListId.isEmpty() || paramListVersionId.isEmpty() || variantId.isEmpty()) continue;
                    paramListIdList.add(paramListId);
                    paramListVersionIdList.add(paramListVersionId);
                    variantIdList.add(variantId);
                }
                List<String> actualParamListVersionIdList = simpleSpecHelper.getActualParamListVersionIdList(paramListIdList, paramListVersionIdList, variantIdList);
                DataSet getParamItemsDs = simpleSpecHelper.getParamItems(paramListIdList, actualParamListVersionIdList, variantIdList, configuredParamListItemColumnCollection, filterParamType);
                DataSet paramItemsDs = simpleSpecHelper.mergeWorkItemsWithParamItems(completeWorkItemDs, getParamItemsDs);
                StringBuilder specParamItemColumns = new StringBuilder();
                for (int i = 0; i < configuredSpecParamItemColumnCollection.size(); ++i) {
                    PropertyList columnProps = configuredSpecParamItemColumnCollection.getPropertyList(i);
                    specParamItemColumns.append(", ").append(columnProps.getProperty("sql"));
                }
                ArrayList<String> getSpecParamItemsParams = new ArrayList<String>();
                getSpecParamItemsParams.add(this.keyId1);
                if (!this.keyId2.isEmpty()) {
                    getSpecParamItemsParams.add(this.keyId2);
                }
                if (!this.keyId3.isEmpty()) {
                    getSpecParamItemsParams.add(this.keyId3);
                }
                String getSpecParamItemsSql = "SELECT specparamitems.paramlistid, specparamitems.variantid, specparamitems.paramid, specparamitems.paramtype, specparamitems.datatypes, spl.limittypesequence, spl.operator1, spl.operator2, spl.value1, spl.value2, spl.value1num, spl.value2num" + specParamItemColumns + " FROM " + this.primaryTableId + " p JOIN specparamitems ON specparamitems.specid = p." + primarySpecIdColumn + " AND specparamitems.specversionid = p." + primarySpecVersionIdColumn + " LEFT JOIN specparamlimits spl ON spl.specid = p." + primarySpecIdColumn + " AND spl.specversionid = p." + primarySpecVersionIdColumn + " AND specparamitems.paramlistid = spl.paramlistid AND specparamitems.paramlistversionid = spl.paramlistversionid AND specparamitems.variantid = spl.variantid AND specparamitems.paramid = spl.paramid AND specparamitems.paramtype = spl.paramtype WHERE p." + this.primaryKeyColId1 + " = ?" + (!this.primaryKeyColId2.isEmpty() ? " AND p." + this.primaryKeyColId2 + " = ?" : "") + (!this.primaryKeyColId3.isEmpty() ? " AND p." + this.primaryKeyColId3 + " = ?" : "") + " ORDER BY specparamitems.paramlistid, specparamitems.variantid, specparamitems.paramlistversionid, specparamitems.paramid";
                DataSet getSpecParamItemsDs = this.getQueryProcessor().getPreparedSqlDataSet(getSpecParamItemsSql, getSpecParamItemsParams.toArray());
                DataSet rowsDs = this.mergeParamItemsWithSpecParamItems(paramItemsDs, getSpecParamItemsDs);
                rowsDs.sort("workitemsequence, workitemitemsequence, paramlistitemsequence");
                ArrayList<String> getTestingLevelsParams = new ArrayList<String>();
                getTestingLevelsParams.add(this.keyId1);
                if (!this.primaryKeyColId2.isEmpty()) {
                    getTestingLevelsParams.add(this.keyId2);
                }
                if (!this.primaryKeyColId3.isEmpty()) {
                    getTestingLevelsParams.add(this.keyId3);
                }
                boolean enableTestingLevels = this.element.getProperty("enabletestinglevels", "y").toLowerCase().startsWith("y");
                if (!primarySamplingPlanIdColumn.trim().isEmpty() && !primarySamplingPlanVersionIdColumn.trim().isEmpty()) {
                    String getTestingLevelsSql = "SELECT d.s_samplingplandetailno, d.levelid, i.itemkeyid1 workitemid, i.itemkeyid2 workitemversionid FROM " + this.primaryTableId + " p JOIN s_samplingplan sp ON p." + primarySamplingPlanIdColumn + " = sp.s_samplingplanid AND p." + primarySamplingPlanVersionIdColumn + " = sp.s_samplingplanversionid JOIN s_spdetail d ON sp.s_samplingplanid = d.s_samplingplanid AND sp.s_samplingplanversionid = d.s_samplingplanversionid LEFT JOIN s_spdetailitem di ON sp.s_samplingplanid = di.s_samplingplanid AND sp.s_samplingplanversionid = di.s_samplingplanversionid AND di.s_samplingplandetailno = d.s_samplingplandetailno LEFT JOIN s_spitem i ON i.s_samplingplanid = sp.s_samplingplanid AND i.s_samplingplanversionid = sp.s_samplingplanversionid AND i.itemsdcid = 'WorkItem' AND i.s_samplingplanitemno = di.s_samplingplanitemno WHERE p." + this.primaryKeyColId1 + " = ?" + (!this.primaryKeyColId2.isEmpty() ? " AND p." + this.primaryKeyColId2 + " = ?" : "") + (!this.primaryKeyColId3.isEmpty() ? " AND p." + this.primaryKeyColId3 + " = ?" : "") + " ORDER BY d.s_samplingplandetailno";
                    getTestingLevelsDs = this.getQueryProcessor().getPreparedSqlDataSet(getTestingLevelsSql, getTestingLevelsParams.toArray());
                } else {
                    getTestingLevelsDs = new DataSet();
                    if (enableTestingLevels) {
                        this.logger.warn("Simple spec testing levels are enabled but sampling plan ID column or sampling plan version ID column is empty");
                    }
                }
                HashMap<String, String> limitTypeMap = new HashMap<String, String>();
                limitTypeMap.put("1", "lowlimit3");
                limitTypeMap.put("2", "lowlimit2");
                limitTypeMap.put("3", "lowlimit1");
                limitTypeMap.put("5", "target");
                limitTypeMap.put("6", "highlimit1");
                limitTypeMap.put("7", "highlimit2");
                limitTypeMap.put("8", "highlimit3");
                PropertyListCollection testingLevelCollection = new PropertyListCollection();
                LinkedHashMap testingLevelIdMap = new LinkedHashMap();
                for (int i = 0; i < getTestingLevelsDs.getRowCount(); ++i) {
                    String levelId = getTestingLevelsDs.getString(i, "levelid");
                    BigDecimal samplingPlanDetailNo = getTestingLevelsDs.getBigDecimal(i, "s_samplingplandetailno");
                    List<String> testingLevelKey = Arrays.asList(samplingPlanDetailNo.toPlainString(), levelId);
                    HashSet<List<String>> testingLevelWorkItemSet = (HashSet<List<String>>)testingLevelIdMap.get(testingLevelKey);
                    String workItemId = getTestingLevelsDs.getString(i, "workitemid");
                    String workItemVersionId = getTestingLevelsDs.getString(i, "workitemversionid");
                    if (workItemVersionId != null && workItemVersionId.equals("C")) {
                        workItemVersionId = "";
                    }
                    List<String> workItemKey = Arrays.asList(workItemId, workItemVersionId);
                    if (testingLevelWorkItemSet == null) {
                        testingLevelWorkItemSet = new HashSet<List<String>>();
                        testingLevelIdMap.put(testingLevelKey, testingLevelWorkItemSet);
                        if (workItemId == null || workItemVersionId == null) continue;
                        testingLevelWorkItemSet.add(workItemKey);
                        continue;
                    }
                    if (workItemId == null || workItemVersionId == null) continue;
                    testingLevelWorkItemSet.add(workItemKey);
                }
                int testingLevelRowNum = 1;
                for (List aSetKey : testingLevelIdMap.keySet()) {
                    BigDecimal samplingPlanDetailNo = new BigDecimal((String)aSetKey.get(0));
                    String levelId = (String)aSetKey.get(1);
                    PropertyList testingLevelRowProps = new PropertyList();
                    testingLevelRowProps.setProperty("levelid", levelId);
                    testingLevelRowProps.setProperty("samplingplandetailno", samplingPlanDetailNo.toPlainString());
                    testingLevelRowProps.setProperty("id", this.elementid + "_testinglevel_" + testingLevelRowNum++);
                    testingLevelCollection.add(testingLevelRowProps);
                }
                HashMap<List<String>, Set<List<String>>> workItemParamMap = new HashMap<List<String>, Set<List<String>>>();
                PropertyList tableProps = new PropertyList();
                PropertyListCollection rowCollection = new PropertyListCollection();
                tableProps.setProperty("rowcollection", rowCollection);
                HashSet<List<String>> paramSet = new HashSet<List<String>>();
                HashSet<List<String>> paramItemSet = new HashSet<List<String>>();
                int rowNum = 0;
                for (int i = 0; i < rowsDs.getRowCount(); ++i) {
                    PropertyList rowProps;
                    String workItemId = rowsDs.getString(i, "workitemid");
                    String currentWorkItemVersionId = rowsDs.getString(i, "workitemversionid");
                    if (currentWorkItemIdList.contains(workItemId)) {
                        rowsDs.setString(i, "workitemversionid", "");
                    }
                    String workItemVersionId = rowsDs.getString(i, "workitemversionid");
                    String paramListId = rowsDs.getString(i, "paramlistid");
                    String paramListVersionId = rowsDs.getString(i, "paramlistversionid");
                    String variantId = rowsDs.getString(i, "variantid");
                    String paramId = rowsDs.getString(i, "paramid");
                    String paramType = rowsDs.getString(i, "paramtype");
                    List<String> paramKey = Arrays.asList(workItemId, workItemVersionId, paramListId, paramListVersionId, variantId, paramId, paramType);
                    List<String> workItemKey = Arrays.asList(workItemId, workItemVersionId);
                    List<String> paramItemKey = Arrays.asList(paramListId, paramListVersionId, variantId, paramId, paramType);
                    String specParamItemStatus = paramItemSet.contains(paramItemKey) ? "V" : "";
                    paramItemSet.add(paramItemKey);
                    if (paramSet.contains(paramKey)) {
                        for (int j = 0; j < rowCollection.size(); ++j) {
                            rowProps = rowCollection.getPropertyList(j);
                            workItemId = rowProps.getProperty("workitemid");
                            List<String> rowParamKey = Arrays.asList(workItemId, workItemVersionId = rowProps.getProperty("workitemversionid"), paramListId = rowProps.getProperty("paramlistid"), paramListVersionId = rowProps.getProperty("paramlistversionid"), variantId = rowProps.getProperty("variantid"), paramId = rowProps.getProperty("paramid"), paramType = rowProps.getProperty("paramtype"));
                            if (!rowParamKey.equals(paramKey)) continue;
                            String limitSequenceName = (String)limitTypeMap.get(rowsDs.getValue(i, "limittypesequence"));
                            if (limitSequenceName != null) {
                                rowProps.setProperty(limitSequenceName, this.getLimitValue(rowsDs, i, limitSequenceName));
                                rowProps.setProperty(limitSequenceName + "_original", rowProps.getProperty(limitSequenceName));
                            }
                            break;
                        }
                    } else {
                        paramSet.add(paramKey);
                        rowProps = new PropertyList();
                        simpleSpecHelper.addAllValues(rowsDs, i, rowProps);
                        rowProps.setProperty("elementid", this.elementid);
                        rowProps.setProperty("workitemstatus", "");
                        rowProps.setProperty("specparamitemstatus", specParamItemStatus);
                        rowProps.setProperty("selected", "N");
                        rowProps.setProperty("currentworkitemversionid", currentWorkItemVersionId);
                        rowProps.setProperty("currentparamlistversionid", paramListVersionId);
                        rowProps.setProperty("id", this.elementid + "_paramitem_" + ++rowNum);
                        String limitSequenceName = (String)limitTypeMap.get(rowsDs.getValue(i, "limittypesequence"));
                        if (limitSequenceName != null) {
                            rowProps.setProperty(limitSequenceName, this.getLimitValue(rowsDs, i, limitSequenceName));
                            rowProps.setProperty(limitSequenceName + "_original", rowProps.getProperty(limitSequenceName));
                        }
                        StringBuilder rowTestingLevelList = new StringBuilder();
                        for (Map.Entry entry : testingLevelIdMap.entrySet()) {
                            if (!((Set)entry.getValue()).contains(workItemKey)) continue;
                            rowTestingLevelList.append(";").append("|").append((String)((List)entry.getKey()).get(0)).append("|").append((String)((List)entry.getKey()).get(1)).append("|");
                        }
                        if (rowTestingLevelList.length() > 0) {
                            rowProps.setProperty("testinglevellist", rowTestingLevelList.substring(1));
                        } else {
                            rowProps.setProperty("testinglevellist", "");
                        }
                        rowProps.setProperty("testinglevellist_original", rowProps.getProperty("testinglevellist"));
                        rowCollection.add(rowProps);
                    }
                    HashSet<List<String>> workItemParamSet = (HashSet<List<String>>)workItemParamMap.get(workItemKey);
                    if (workItemParamSet == null) {
                        workItemParamSet = new HashSet<List<String>>();
                        workItemParamSet.add(paramKey);
                        workItemParamMap.put(workItemKey, workItemParamSet);
                        continue;
                    }
                    workItemParamSet.add(paramKey);
                }
                simpleSpecHelper.setParamCounts(workItemParamMap, rowCollection);
                tablePropsJson = tableProps.toJSONString(false);
                PropertyList testingLevelProps = new PropertyList();
                testingLevelProps.setProperty("rowcollection", testingLevelCollection);
                testingLevelPropsJson = testingLevelProps.toJSONString(false);
            }
            catch (SapphireException ex) {
                this.logger.error("Error while creating simple spec element", ex);
                return "Error while creating simple spec element: " + ex.getMessage();
            }
        }
        html.append("<div id='").append(this.elementid).append("_container'></div>");
        String lockedBy = this.sdiInfo.getDataSet("primary").getString(0, "__lockedby", "");
        boolean isLocked = lockedBy.length() > 0;
        boolean bl = isReadOnly = isLocked || this.element.getProperty("readonly").toLowerCase().startsWith("y");
        if (!isReadOnly) {
            html.append("<table style='width: 1px;'><tbody><tr>");
            PropertyListCollection buttonCollection = this.element.getCollectionNotNull("buttoncollection");
            for (int i = 0; i < buttonCollection.size(); ++i) {
                PropertyList buttonProps = buttonCollection.getPropertyList(i);
                if (!buttonProps.getProperty("show", "y").toLowerCase().startsWith("y")) continue;
                Button button = new Button(this.pageContext);
                button.setId(buttonProps.getProperty("buttonid"));
                button.setText(buttonProps.getProperty("text"));
                button.setTip(buttonProps.getProperty("tip"));
                button.setAction(buttonProps.getProperty("function"));
                button.setImg(buttonProps.getProperty("image"));
                button.setAppearance(buttonProps.getProperty("appearance"));
                button.setMargin(buttonProps.getProperty("margin"));
                button.setStyle(buttonProps.getProperty("style"));
                button.setHighlight(buttonProps.getProperty("highlight"));
                button.setWidth(buttonProps.getProperty("width") + "px");
                html.append("<td style='width: 1px;'>").append(button.getHtml()).append("</td>");
            }
            html.append("<td style='width: 100%;'>&nbsp;</td></tr></tbody></table>");
        }
        html.append("<script type='text/javascript' language='JavaScript' src='WEB-CORE/elements/simplespec/scripts/simplespec.js'></script>");
        html.append(JavaScriptAPITag.getJQueryAPI(false, false, null, this.pageContext));
        html.append("<script type='text/javascript' language='JavaScript' src='WEB-CORE/scripts/json.js'></script>");
        html.append("<input type='hidden' name='__propertyhandler_").append(this.elementid).append("' value='").append("com.labvantage.sapphire.pageelements.simplespec.element.SimpleSpecPropertyHandler").append("'/>");
        html.append("<input type='hidden' id='_").append(this.elementid).append("_webpageid' name='_").append(this.elementid).append("_webpageid' value='").append(webPageId).append("'>");
        html.append("<input type='hidden' id='_").append(this.elementid).append("_sdcid' name='_").append(this.elementid).append("_sdcid' value='").append(this.sdcId).append("'>");
        html.append("<input type='hidden' id='_").append(this.elementid).append("_keyid1' name='_").append(this.elementid).append("_keyid1' value='").append(this.keyId1).append("'>");
        html.append("<input type='hidden' id='_").append(this.elementid).append("_keyid2' name='_").append(this.elementid).append("_keyid2' value='").append(this.keyId2).append("'>");
        html.append("<input type='hidden' id='_").append(this.elementid).append("_keyid3' name='_").append(this.elementid).append("_keyid3' value='").append(this.keyId3).append("'>");
        html.append("<input type='hidden' id='_").append(this.elementid).append("_elementpropsjson' name='_").append(this.elementid).append("_elementpropsjson' value=\"").append(HttpUtil.encodeURIComponent(this.element.toJSONString(false))).append("\">");
        html.append("<input type='hidden' id='_").append(this.elementid).append("_tablepropsjson' name='_").append(this.elementid).append("_tablepropsjson' value=\"").append(HttpUtil.encodeURIComponent(tablePropsJson)).append("\">");
        html.append("<input type='hidden' id='_").append(this.elementid).append("_testinglevelpropsjson' name='_").append(this.elementid).append("_testinglevelpropsjson' value=\"").append(HttpUtil.encodeURIComponent(testingLevelPropsJson)).append("\">");
        html.append("<input type='hidden' id='_").append(this.elementid).append("_columnpropsjson' name='_").append(this.elementid).append("_columnpropsjson' value=\"").append(HttpUtil.encodeURIComponent(columnPropsJson)).append("\">");
        html.append("<script type='text/javascript' language='JavaScript'>var ").append(JS_VAR).append(" = new SPE.SimpleSpec('").append(JS_VAR).append("', '").append(this.elementid).append("'); simpleSpec.renderTable();</script>");
        return html.toString();
    }

    private String getLimitValue(DataSet rowsDs, int row, String limitSequenceName) {
        BigDecimal valueNum;
        String value;
        String value1 = rowsDs.getString(row, "value1", "");
        String value2 = rowsDs.getString(row, "value2", "");
        BigDecimal value1Num = rowsDs.getBigDecimal(row, "value1num");
        BigDecimal value2Num = rowsDs.getBigDecimal(row, "value2num");
        if (limitSequenceName.startsWith("low")) {
            if (!value2.isEmpty()) {
                value = value2;
                valueNum = value2Num;
            } else {
                value = value1;
                valueNum = value1Num;
            }
        } else {
            value = value1;
            valueNum = value1Num;
        }
        String returnValue = valueNum != null ? this.m18client.format(this.m18server.parseBigDecimal(value), false, false) : value;
        return returnValue;
    }

    private void updateKeysFromLink(String linkId) {
        DataSet linkDs = this.getSDCProcessor().getLinksData(this.sdcId);
        String linkType = "";
        String linkSdcId = "";
        String linkColumnId = "";
        String linkColumnId2 = "";
        for (int i = 0; i < linkDs.getRowCount(); ++i) {
            if (!linkDs.getString(i, "linkid", "").equalsIgnoreCase(linkId)) continue;
            linkType = linkDs.getString(i, "linktype", "");
            linkColumnId = linkDs.getString(i, "sdccolumnid", "");
            linkColumnId2 = linkDs.getString(i, "sdccolumnid2", "");
            linkSdcId = linkDs.getString(i, "linksdcid", "");
            break;
        }
        if (linkType.equals("F")) {
            HashMap sdcProps = this.getSDCProcessor().getSDCProperties(linkSdcId);
            String primaryKeyColId1 = (String)sdcProps.get("keycolid1");
            String primaryKeyColId2 = (String)sdcProps.get("keycolid2");
            String tableId = (String)sdcProps.get("tableid");
            boolean isOracle = this.getConnectionProcessor().isOra();
            String queryWhere = primaryKeyColId1 + "='" + SafeSQL.encodeForSQL(this.keyId1, isOracle) + "'";
            if (!linkColumnId2.isEmpty()) {
                queryWhere = queryWhere + " and " + primaryKeyColId2 + "='" + SafeSQL.encodeForSQL(this.keyId2, isOracle) + "'";
            }
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDCid(linkSdcId);
            sdiRequest.setQueryFrom(tableId);
            sdiRequest.setQueryWhere(queryWhere);
            sdiRequest.setRequestItem("primary");
            sdiRequest.setRetainRsetid(false);
            sdiRequest.setDataLockOption("LA");
            sdiRequest.setLockOption("LA");
            sdiRequest.setPrimaryLockOption("LA");
            DataSet ds = this.getSDIProcessor().getSDIData(sdiRequest).getDataset("primary");
            if (ds.getRowCount() > 0) {
                this.sdcId = linkSdcId;
                this.keyId1 = ds.getString(0, linkColumnId, this.keyId1);
                this.keyId2 = !linkColumnId2.isEmpty() ? ds.getString(0, linkColumnId2, this.keyId2) : "";
                this.keyId3 = "";
            }
        }
    }

    private DataSet getSDIWorkItemDs(PropertyListCollection configuredSDIWorkItemColumnCollection) {
        StringBuilder sdiWorkItemSqlColumns = new StringBuilder();
        sdiWorkItemSqlColumns.append("sdiworkitem.workitemid");
        sdiWorkItemSqlColumns.append(", coalesce(sdiworkitem.workitemversionid, 'C') workitemversionid");
        sdiWorkItemSqlColumns.append(", coalesce(sdiworkitem.usersequence, 1) workitemsequence");
        sdiWorkItemSqlColumns.append(", sdiworkitem.groupid");
        for (int i = 0; i < configuredSDIWorkItemColumnCollection.size(); ++i) {
            PropertyList columnProps = configuredSDIWorkItemColumnCollection.getPropertyList(i);
            String sqlColumn = columnProps.getProperty("sql");
            if (sqlColumn.isEmpty()) continue;
            sdiWorkItemSqlColumns.append(", ").append(sqlColumn);
        }
        ArrayList<String> getSDIWorkItemsParams = new ArrayList<String>();
        getSDIWorkItemsParams.add(this.keyId1);
        if (!this.primaryKeyColId2.isEmpty()) {
            getSDIWorkItemsParams.add(this.keyId2);
        }
        if (!this.primaryKeyColId3.isEmpty()) {
            getSDIWorkItemsParams.add(this.keyId3);
        }
        String getSDIWorkItemsSql = "SELECT " + sdiWorkItemSqlColumns + " FROM " + this.primaryTableId + " p JOIN sdiworkitem ON sdiworkitem.sdcid = '" + this.sdcId + "' AND sdiworkitem.keyid1 = p." + this.primaryKeyColId1 + (!this.primaryKeyColId2.isEmpty() ? " AND sdiworkitem.keyid2 = p." + this.primaryKeyColId2 : "") + (!this.primaryKeyColId3.isEmpty() ? " AND sdiworkitem.keyid3 = p." + this.primaryKeyColId3 : "") + " AND sdiworkitem.workiteminstance = '1' AND sdiworkitem.workitemtypeflag <> 'P' WHERE p." + this.primaryKeyColId1 + " = ?" + (!this.primaryKeyColId2.isEmpty() ? " AND p." + this.primaryKeyColId2 + " = ?" : "") + (!this.primaryKeyColId3.isEmpty() ? " AND p." + this.primaryKeyColId3 + " = ?" : "") + " ORDER BY sdiworkitem.usersequence";
        return this.getQueryProcessor().getPreparedSqlDataSet(getSDIWorkItemsSql, getSDIWorkItemsParams.toArray());
    }

    private DataSet mergeParamItemsWithSpecParamItems(DataSet paramItemsDs, DataSet specParamItemsDs) {
        DataSet returnDs = new DataSet(this.connectionInfo);
        for (int i = 0; i < paramItemsDs.getRowCount(); ++i) {
            String paramListId = paramItemsDs.getString(i, "paramlistid");
            String variantId = paramItemsDs.getString(i, "variantid");
            String paramId = paramItemsDs.getString(i, "paramid");
            String paramType = paramItemsDs.getString(i, "paramtype");
            HashMap<String, String> filterMap = new HashMap<String, String>();
            filterMap.put("paramlistid", paramListId);
            filterMap.put("variantid", variantId);
            filterMap.put("paramid", paramId);
            filterMap.put("paramtype", paramType);
            DataSet filteredDs = specParamItemsDs.getFilteredDataSet(filterMap);
            if (filteredDs.getRowCount() > 0) {
                for (int j = 0; j < filteredDs.getRowCount(); ++j) {
                    returnDs.copyRow(paramItemsDs, i, 1);
                    int row = returnDs.getRowCount() - 1;
                    for (int k = 0; k < filteredDs.getColumnCount(); ++k) {
                        Object value;
                        String columnId = filteredDs.getColumnId(k);
                        int columnType = filteredDs.getColumnType(columnId);
                        if (!returnDs.isValidColumn(columnId)) {
                            returnDs.addColumn(columnId, columnType);
                        }
                        if (columnType == 0) {
                            value = filteredDs.getString(j, columnId);
                            returnDs.setString(row, columnId, (String)value);
                            continue;
                        }
                        if (columnType == 1) {
                            value = filteredDs.getBigDecimal(j, columnId);
                            returnDs.setNumber(row, columnId, (BigDecimal)value);
                            continue;
                        }
                        if (columnType == 2) {
                            value = filteredDs.getCalendar(j, columnId);
                            returnDs.setDate(row, columnId, (Calendar)value);
                            continue;
                        }
                        value = filteredDs.getValue(j, columnId);
                        returnDs.setValue(row, columnId, (String)value);
                    }
                }
                continue;
            }
            returnDs.copyRow(paramItemsDs, i, 1);
        }
        return returnDs;
    }
}

