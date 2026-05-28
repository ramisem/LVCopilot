/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.samplingplan;

import com.labvantage.sapphire.pageelements.controls.Button;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SamplingPlanDetailItem
extends BaseElement {
    static final String LABVANTAGE_CVS_ID = "$Revision: 87196 $";
    private static final String PROPERTYHANDLERCLASS = "com.labvantage.sapphire.pageelements.samplingplan.SamplingPlanDetailItemPropertyHandler";
    private static final String ELEMENT_JS_URL = "WEB-CORE/elements/samplingplandetailitem/samplingplandetailitem.js";
    private static final String JSON_JS_URL = "WEB-CORE/scripts/json.js";
    private String samplingPlanId;
    private String samplingPlanVersionId;
    private QueryProcessor qp;
    private TranslationProcessor tp;
    private String elementId;
    private Map levelColors;
    private boolean isViewMode;
    private boolean isLocked;
    private PropertyList samplingPlanPolicy;
    private PropertyListCollection itemSDCProps;
    private static final String SAMPLINGPLAN_POLICY = "SamplingPlanPolicy";
    private static final String SAMPLINGPLAN_POLICY_NODE = "Sapphire Custom";
    private static final String PROPERTY_READONLY = "readonly";
    private static final String PROPERTY_DATA = "data";
    private static final String PROPERTY_DATA_TEMPLATESDCLIST = "templatesdc";
    private static final String PROPERTY_DATA_SDCID = "sdcid";
    private static final String PROPERTY_DATA_DISPLAYNAME = "displayname";
    private static final String PROPERTY_DATA_SHOW = "show";
    private static final String PROPERTY_DATA_LOOKUPURL = "lookupurl";
    private static final String PROPERTY_DATA_LOOKUPURLMULTI = "lookupurlmulti";
    private static final String PROPERTY_DISPLAY = "display";
    private static final String PROPERTY_DISP_LEVELCOLORS = "levelcolors";
    private static final String PROPERTY_DISP_LEVELID = "levelid";
    private static final String PROPERTY_DISP_COLOR = "color";
    private static final String SPDETAIL_SPID = "s_samplingplanid";
    private static final String SPDETAIL_SPVERID = "s_samplingplanversionid";
    private static final String SPDETAIL_DETAILNO = "s_samplingplandetailno";
    private static final String SPDETAIL_LEVELID = "levelid";
    private static final String SPDETAIL_SOURCELABEL = "sourcelabel";
    private static final String SPDETAIL_PROCESSSTAGE = "processstageid";
    private static final String SPDETAIL_TEMPLATESDC = "templatesdcid";
    private static final String SPDETAIL_TEMPLATEID1 = "templatekeyid1";
    private static final String SPDETAIL_RULETYPE = "countruletype";
    private static final String SPDETAIL_RULEVALUE = "countrule";
    private static final String SPITEM_ITEMNO = "s_samplingplanitemno";
    private static final String SPITEM_SDCID = "itemsdcid";
    private static final String SPITEM_KEYID1 = "itemkeyid1";
    private static final String SPITEM_KEYID2 = "itemkeyid2";
    private static final String SPITEM_KEYID3 = "itemkeyid3";
    private static final String SPITEM_USERSEQ = "usersequence";
    private static final String SPDETAILITEM_DETAILNO = "s_samplingplandetailno";
    private static final String SPDETAILITEM_ITEMNO = "s_samplingplanitemno";
    private static final int INITIAL_COLSPAN = 4;
    private static final String DETAILITEM_CHECKED = "T";
    private static final String DETAILITEM_UNCHECKED = "F";
    private static final String DETAILITEM_UNCHANGED = "U";
    private static final String JS_OBJECT = "samplingPlanDetailItem";
    private static final String CSS_HEADER = "gridmaint_fieldtitle";
    private static final String CSS_HEADER_X = "maintform_field";
    private String CSS_HEADER_Y = "maintform_field";
    private String CSS_DETAILITEMS = "maintform_field";
    private static final String RULETYPE_NOTDEFINED = "(Not Defined)";
    private static final String ICON_LOOKUPURL = "WEB-CORE/elements/images/lookup.gif";
    private static final String ICON_LOCKEDURL = "WEB-CORE/elements/images/locked.gif";
    private static final String ICON_MINUS = "WEB-CORE/elements/images/minus.gif";
    private static final String ICON_PLUS = "WEB-CORE/elements/images/plus.gif";
    private static final String ICON_FILLACROSS = "WEB-CORE/images/png/Forward.png";
    private static final String ICON_FILLDOWN = "WEB-CORE/images/gif/FillDown.gif";
    private static final String ICON_MOVEUP = "WEB-CORE/images/png/MoveUp.png";
    private static final String ICON_MOVEDOWN = "WEB-CORE/images/png/MoveDown.png";
    private static final String ICON_REMOVEROW = "WEB-CORE/images/gif/RemoveRow.gif";
    private static final String ICON_ADDROW = "WEB-CORE/images/gif/AddRow.gif";
    private int noOfSPDetailRows;
    private int minTopRows = 0;

    public String getHtml(QueryProcessor qp) {
        this.qp = qp;
        return this.getHtml();
    }

    @Override
    public String getHtml() {
        Logger.logInfo("SamplingPlanDetailItem: getHtml(): entered");
        this.qp = this.getQueryProcessor();
        this.tp = this.getTranslationProcessor();
        StringBuffer html = null;
        StringBuffer jsonStr = new StringBuffer();
        try {
            try {
                this.samplingPlanId = this.requestContext.getProperty("keyid1");
                this.samplingPlanVersionId = this.requestContext.getProperty("keyid2");
                this.elementId = this.elementid;
                this.isViewMode = this.isViewMode();
                Logger.logInfo("SamplingPlanDetailItem: View Mode = " + this.isViewMode);
                this.isLocked = this.checkLockState();
                Logger.logInfo("SamplingPlanDetailItem: Locked? = " + this.isLocked);
                if (this.isLocked) {
                    this.CSS_HEADER_Y = "maint_lockedfield";
                    this.CSS_DETAILITEMS = "maint_lockedfield";
                }
            }
            catch (Exception e) {
                throw new SapphireException("Unable to instantiate Element");
            }
            try {
                Logger.logInfo("SamplingPlanDetailItem: Retrieve details of Policy: SamplingPlanPolicy");
                this.samplingPlanPolicy = this.getConfigurationProcessor().getPolicy(SAMPLINGPLAN_POLICY, SAMPLINGPLAN_POLICY_NODE);
                this.itemSDCProps = this.samplingPlanPolicy.getCollectionNotNull(PROPERTY_DATA_TEMPLATESDCLIST);
            }
            catch (SapphireException e) {
                throw new SapphireException("Unable to retrieve SamplingPlanPolicy definition.");
            }
            DataSet spDetail = null;
            Set levelIdSet = null;
            DataSet spItems = null;
            DataSet spDetailItems = null;
            html = new StringBuffer("\n<!--================= Start Render of SamplingPlanDetailItem element: " + this.elementId + "-->\n");
            Logger.logInfo("Start Render of SamplingPlanDetailItem element: " + this.elementId);
            Map allowedSPItems = this.getAllowedSPItems();
            spItems = this.getSPItems();
            spDetailItems = this.getSPDetailItems();
            spDetail = this.getSPDetail();
            this.noOfSPDetailRows = spDetail.getRowCount();
            if (this.noOfSPDetailRows == 0) {
                return this.getTranslationProcessor().translate("No Level Samples defined yet");
            }
            Logger.logInfo("No of SamplingPlan Details: " + this.noOfSPDetailRows);
            levelIdSet = this.getDistinctLevelId(spDetail);
            this.renderTemplateTable(html, spDetail, allowedSPItems, levelIdSet);
            html.append("\n<input type='hidden' name='__propertyhandler_").append(this.elementid).append("' value='").append(PROPERTYHANDLERCLASS).append("'/>");
            html.append("\n<input type='hidden' name='_" + this.elementId + "_keyid1' value='" + this.samplingPlanId + "' />");
            html.append("\n<input type='hidden' name='_" + this.elementId + "_keyid2' value='" + this.samplingPlanVersionId + "' />");
            html.append("\n<input type='hidden' id='_" + this.elementId + "_jsonstr' name='_" + this.elementId + "_jsonstr' value=''>");
            html.append("\n<table id='_" + this.elementId + "_maintable' class='gridmaint_table' cellspacing='0' >");
            html.append("\n<tbody>");
            ++this.minTopRows;
            ++this.minTopRows;
            ++this.minTopRows;
            ++this.minTopRows;
            ++this.minTopRows;
            this.renderLevel(html, levelIdSet, spDetail, spItems);
            this.renderProcessStages(html, levelIdSet, spDetail);
            this.renderSourceLabels(html, levelIdSet, spDetail);
            this.renderSamples(html, levelIdSet, spDetail);
            this.renderSelectAllRow(html, levelIdSet, spDetail);
            this.renderSPItems(html, jsonStr, spItems, levelIdSet, spDetail, spDetailItems);
            html.append("</tbody>\n");
            html.append("</table>\n");
            this.renderButtons(html, allowedSPItems);
            html.append(jsonStr);
            this.renderScripts(html, spDetail, allowedSPItems, levelIdSet, spItems);
        }
        catch (Exception e) {
            this.logger.stackTrace(e);
            html = new StringBuffer("\n<!--================= Start Rendering of SamplingPlanDetailItem element: " + this.elementId + "-->\n");
            html.append("<table cellspacing=0 cellpadding=10  border=0 bordercolor=\"#b0c4de\" style=\"border-style:solid;border-width:1px; margin:10px\"><tr><td><font color='red'>").append(this.getTranslationProcessor().translate("Element could not be rendered due to the following errors:")).append("<br>").append(this.getTranslationProcessor().translate(e.getMessage())).append("</font></td></tr></table>");
        }
        html.append("\n<!--================= End rendering of SamplingPlanDetailItem element: " + this.elementId + "-->\n");
        Logger.logInfo("End rendering of SamplingPlanDetailItem element: " + this.elementId);
        return html.toString();
    }

    private void renderLevel(StringBuffer html, Set<String> levelIdSet, DataSet spDetail, DataSet spItems) throws SapphireException {
        try {
            html.append("\n\t<tr class=\"gridmaint_tablehead\">");
            html.append("\n\t\t<td colspan='4' nowrap class=\"gridmaint_fieldtitle\"><b>").append(this.tp.translate("Level")).append("</b></td>");
            for (String levelId : levelIdSet) {
                String color = this.getLevelColor(levelId);
                int colSpan = this.getFilteredByLevelDS(levelId, spDetail).getRowCount();
                html.append("\n\t\t<td").append(" ").append("levelid").append("='").append(levelId).append("'").append(" bgcolor='").append(color).append("'").append(" onclick='").append(JS_OBJECT).append(".showLevel(\"" + this.elementId + "\", \"").append(levelId).append("\");'").append(" class='maintform_field'").append(" align='center'").append(" rowspan='").append(this.minTopRows + spItems.getRowCount()).append("'").append(" valign='top'").append(" id='_").append(this.elementId).append("_").append(levelId).append("'").append(" name='_").append(this.elementId).append("_hiddenlevel'").append(" style='display:none; cursor: pointer;'").append(" >").append("<img border='0' src='").append(ICON_PLUS).append("' /><br>");
                for (int i = 0; i < levelId.length(); ++i) {
                    html.append(levelId.charAt(i)).append("<br>");
                }
                html.append("</td>");
                html.append("\n\t\t<td").append(" ").append("levelid").append("='").append(levelId).append("'").append(" bgcolor='").append(color).append("'").append(" onclick='").append(JS_OBJECT).append(".hideLevel(\"" + this.elementId + "\", \"").append(levelId).append("\");'").append(" class='maintform_field'").append(" align='center'").append(" colspan='").append(colSpan).append("'").append(" style='cursor: pointer;'").append(">").append("<table border='0' cellspacing='0' cellpadding='0' width='100%'>").append("<tr>").append("<td width='1px'>").append("<img border='0' src='").append(ICON_MINUS).append("' />").append("</td>").append("<td align='center'>").append(levelId).append("</td>").append("</tr>").append("</table>");
                html.append("</td>");
            }
            html.append("\n\t</tr>");
        }
        catch (Exception e) {
            throw new SapphireException("Could Not render Levels: " + e);
        }
    }

    private void renderProcessStages(StringBuffer html, Set<String> levelIdSet, DataSet spDetail) throws SapphireException {
        try {
            html.append("\n\t<tr class=\"gridmaint_tablehead\">");
            html.append("\n\t\t<td colspan='4' nowrap class=\"gridmaint_fieldtitle\"><b>").append(this.tp.translate("Stages")).append("</b></td>");
            for (String levelId : levelIdSet) {
                DataSet levelSamples = this.getFilteredByLevelDS(levelId, spDetail);
                HashMap<String, String> filterMap = new HashMap<String, String>();
                Map processStagesMap = this.getDistinctProcessStages(levelId, spDetail);
                for (String stage : processStagesMap.keySet()) {
                    filterMap.clear();
                    filterMap.put(SPDETAIL_PROCESSSTAGE, stage);
                    DataSet stageSamples = levelSamples.getFilteredDataSet(filterMap);
                    int colSpan = stageSamples.getRowCount();
                    if (colSpan <= 0) continue;
                    html.append("\n\t\t<td align='center' ").append("colspan='").append(colSpan).append("'").append(" ").append("levelid").append("='").append(levelId).append("'").append(" ").append(SPDETAIL_PROCESSSTAGE).append("='").append(stage).append("'").append("class=\"maintform_field\">").append((String)processStagesMap.get(stage)).append("</td>");
                }
            }
            html.append("\n\t</tr>");
        }
        catch (Exception e) {
            throw new SapphireException("Could Not render Source Labels: " + e);
        }
    }

    private void renderSourceLabels(StringBuffer html, Set<String> levelIdSet, DataSet spDetail) throws SapphireException {
        try {
            HashMap<String, String> filterMap = new HashMap<String, String>();
            html.append("\n\t<tr class=\"gridmaint_tablehead\">");
            html.append("\n\t\t<td colspan='4' nowrap class='gridmaint_fieldtitle'><b>").append(this.tp.translate("Source Label")).append("</b></td>");
            for (String levelId : levelIdSet) {
                DataSet levelSamples = this.getFilteredByLevelDS(levelId, spDetail);
                Map processStagesMap = this.getDistinctProcessStages(levelId, spDetail);
                for (String stage : processStagesMap.keySet()) {
                    filterMap.clear();
                    filterMap.put(SPDETAIL_PROCESSSTAGE, stage);
                    DataSet stageLevels = levelSamples.getFilteredDataSet(filterMap);
                    int stageLevelsRowCount = stageLevels.getRowCount();
                    for (int i = 0; i < stageLevelsRowCount; ++i) {
                        String sourceLabel = stageLevels.getString(i, SPDETAIL_SOURCELABEL, "");
                        html.append("\n\t\t<td").append(" align='center'").append(" ").append("levelid").append("='").append(levelId).append("'").append(" ").append(SPDETAIL_PROCESSSTAGE).append("='").append(stage).append("'").append(" ").append(SPDETAIL_SOURCELABEL).append("='").append(sourceLabel).append("'").append(" class='maintform_field'>").append(sourceLabel).append("</td>");
                    }
                }
            }
            html.append("\n\t</tr>");
        }
        catch (Exception e) {
            throw new SapphireException("Could Not render Source Labels: " + e);
        }
    }

    private void renderDetailNos(StringBuffer html, Set<String> levelIdSet, DataSet spDetail) throws SapphireException {
        try {
            HashMap<String, String> filterMap = new HashMap<String, String>();
            html.append("\n\t<tr class=\"gridmaint_tablehead\">");
            html.append("\n\t\t<td colspan='4' nowrap class='gridmaint_fieldtitle'><b>").append(this.tp.translate("Detail Nos.")).append("</b></td>");
            for (String levelId : levelIdSet) {
                DataSet levelSamples = this.getFilteredByLevelDS(levelId, spDetail);
                Map processStagesMap = this.getDistinctProcessStages(levelId, spDetail);
                for (String stage : processStagesMap.keySet()) {
                    filterMap.clear();
                    filterMap.put(SPDETAIL_PROCESSSTAGE, stage);
                    DataSet stageLevels = levelSamples.getFilteredDataSet(filterMap);
                    int stageLevelsRowCount = stageLevels.getRowCount();
                    for (int i = 0; i < stageLevelsRowCount; ++i) {
                        html.append("\n\t\t<td").append(" align='center'").append(" ").append("levelid").append("='").append(levelId).append("'").append(" ").append(SPDETAIL_PROCESSSTAGE).append("='").append(stage).append("'").append(" ").append(SPDETAIL_SOURCELABEL).append("='").append(stageLevels.getString(i, SPDETAIL_SOURCELABEL, "")).append("'").append(" class='maintform_field'>").append(stageLevels.getInt(i, "s_samplingplandetailno")).append("</td>");
                    }
                }
            }
            html.append("\n\t</tr>");
        }
        catch (Exception e) {
            throw new SapphireException("Could not render Detail Nos.: " + e);
        }
    }

    private void renderSamples(StringBuffer html, Set<String> levelIdSet, DataSet spDetail) throws SapphireException {
        try {
            HashMap<String, String> filterMap = new HashMap<String, String>();
            html.append("\n\t<tr class=\"gridmaint_tablehead\">");
            html.append("\n\t\t<td colspan='4' nowrap class=\"gridmaint_fieldtitle\"><b>").append(this.tp.translate("Samples")).append("</b></td>");
            for (String levelId : levelIdSet) {
                DataSet levelSamples = this.getFilteredByLevelDS(levelId, spDetail);
                Map processStagesMap = this.getDistinctProcessStages(levelId, spDetail);
                for (String stage : processStagesMap.keySet()) {
                    filterMap.clear();
                    filterMap.put(SPDETAIL_PROCESSSTAGE, stage);
                    DataSet stageLevels = levelSamples.getFilteredDataSet(filterMap);
                    int stageLevelsRowCount = stageLevels.getRowCount();
                    for (int i = 0; i < stageLevelsRowCount; ++i) {
                        String ruleType = stageLevels.getString(i, SPDETAIL_RULETYPE, "");
                        String ruleValue = "".equals(ruleType) ? RULETYPE_NOTDEFINED : (ruleType.equals("Number") ? stageLevels.getString(i, SPDETAIL_RULEVALUE) : "(" + this.tp.translate(ruleType) + ")");
                        html.append("\n\t\t<td").append(" width='90px'").append(" class='maintform_field'").append(" ").append("levelid").append("='").append(levelId).append("'").append(" ").append(SPDETAIL_PROCESSSTAGE).append("='").append(stage).append("'").append(" ").append(SPDETAIL_SOURCELABEL).append("='").append(stageLevels.getString(i, SPDETAIL_SOURCELABEL, "")).append("'").append(" style='padding-left:5px;padding-right:5px'>").append(ruleValue + " ").append("'" + stageLevels.getString(i, SPDETAIL_TEMPLATEID1) + "'").append("</td>");
                    }
                }
            }
            html.append("\n\t</tr>");
        }
        catch (Exception e) {
            throw new SapphireException("Could Not render Samples: " + e);
        }
    }

    private void renderSelectAllRow(StringBuffer html, Set<String> levelIdSet, DataSet spDetail) throws SapphireException {
        try {
            HashMap<String, String> filterMap = new HashMap<String, String>();
            html.append("\n\t<tr class=\"gridmaint_tablehead\">");
            html.append("\n\t\t<td colspan='4' nowrap class=\"gridmaint_fieldtitle\">").append("<input type='checkbox'").append(this.isViewMode ? " disabled " : "").append(" onclick='").append(JS_OBJECT).append(".toggleItemCheckbox(\"" + this.elementId + "\", this.checked)'").append(">").append("</td>");
            for (String levelId : levelIdSet) {
                DataSet levelSamples = this.getFilteredByLevelDS(levelId, spDetail);
                Map processStagesMap = this.getDistinctProcessStages(levelId, spDetail);
                for (String stage : processStagesMap.keySet()) {
                    filterMap.clear();
                    filterMap.put(SPDETAIL_PROCESSSTAGE, stage);
                    DataSet stageLevels = levelSamples.getFilteredDataSet(filterMap);
                    int stageLevelsRowCount = stageLevels.getRowCount();
                    for (int i = 0; i < stageLevelsRowCount; ++i) {
                        html.append("\n\t\t<td").append(" align='center'").append(" class='gridmaint_fieldtitle'").append(" ").append("levelid").append("='").append(levelId).append("'").append(" ").append(SPDETAIL_PROCESSSTAGE).append("='").append(stage).append("'").append(" ").append(SPDETAIL_SOURCELABEL).append("='").append(stageLevels.getString(i, SPDETAIL_SOURCELABEL, "")).append("'").append(">").append("<input type='checkbox'").append(this.isViewMode ? " disabled " : "").append(" onclick='").append(JS_OBJECT).append(".toggleDetailItemCheckbox(\"" + this.elementId + "\", this.checked, true, \"").append(stageLevels.getInt(i, "s_samplingplandetailno")).append("\")'").append(" >").append("<br><img src='").append(ICON_FILLDOWN).append("' border='0'");
                        html.append("/>").append("</td>");
                    }
                }
            }
            html.append("\n\t</tr>");
        }
        catch (Exception e) {
            throw new SapphireException("Could Not render Select all checkboxes: " + e);
        }
    }

    private void renderSPItems(StringBuffer html, StringBuffer json, DataSet spItems, Set<String> levelIdSet, DataSet spDetail, DataSet spDetailItems) throws Exception {
        try {
            json.append("\n<script type='text/javascript' language='javascript'>");
            json.append("\nvar _" + this.elementId + "_jsonstr = {").append("\n\t\"elementId\" : \"" + this.elementId + "\",").append("\n\t\"items\" : [");
            int spItemsCount = spItems.getRowCount();
            for (int itemCount = 0; itemCount < spItemsCount; ++itemCount) {
                if (itemCount != 0) {
                    json.append(",");
                }
                json.append("\n\t\t{");
                int spItemNo = spItems.getInt(itemCount, "s_samplingplanitemno");
                String itemSDCId = spItems.getString(itemCount, SPITEM_SDCID);
                String itemKeyId1 = spItems.getString(itemCount, SPITEM_KEYID1);
                String itemKeyId2 = spItems.getString(itemCount, SPITEM_KEYID2, "");
                String itemKeyId3 = spItems.getString(itemCount, SPITEM_KEYID3, "");
                String itemUserSeq = String.valueOf(spItems.getInt(itemCount, SPITEM_USERSEQ));
                json.append("\n\t\t\t\"itemNo\" : \"" + spItemNo + "\",");
                json.append("\n\t\t\t\"sdcId\" : \"" + itemSDCId + "\",");
                json.append("\n\t\t\t\"keyId1\" : \"" + itemKeyId1 + "\",");
                json.append("\n\t\t\t\"keyId2\" : \"" + itemKeyId2 + "\",");
                json.append("\n\t\t\t\"keyId3\" : \"" + itemKeyId3 + "\",");
                json.append("\n\t\t\t\"userSequence\" : \"" + itemUserSeq + "\",");
                json.append("\n\t\t\t\"status\" : \"S\",");
                html.append("\n\t<tr id='_" + this.elementId + "_row-" + spItemNo + "' onmouseover='" + JS_OBJECT + ".mouseOver(this)' onmouseout='" + JS_OBJECT + ".mouseOut(this)'>");
                html.append("\n\t\t<td class='" + this.CSS_HEADER_Y + "'>");
                if (this.isLocked) {
                    html.append("<img src='WEB-CORE/elements/images/locked.gif' />");
                }
                html.append("<input type='checkbox'").append(" id='_" + this.elementId + "_item-" + spItemNo + "'").append(this.isViewMode ? " disabled " : "").append(" name='_" + this.elementId + "_item' ").append(">");
                html.append("</td>");
                html.append("\n\t\t<td class='" + this.CSS_HEADER_Y + "' style='padding-left:5px;padding-right:5px' nowrap>" + this.tp.translate(this.getItemDisplayName(itemSDCId)) + "</td>");
                String keyId = itemKeyId1;
                if (itemKeyId2.length() != 0) {
                    keyId = keyId + " ( " + itemKeyId2;
                    keyId = itemKeyId3.length() != 0 ? keyId + ", " + itemKeyId3 + " )" : keyId + " )";
                }
                html.append("\n\t\t<td class=\"" + this.CSS_HEADER_Y + "\" style='padding-left:5px;padding-right:5px' nowrap>" + SafeHTML.encodeForHTML(keyId) + "</td>");
                html.append("\n\t\t<td").append(" align='center'").append(" class='gridmaint_fieldtitle'").append(">").append("<input type='checkbox'").append(this.isViewMode ? " disabled " : "").append(" onclick='").append(JS_OBJECT).append(".toggleDetailItemCheckbox(\"" + this.elementId + "\", this.checked, false, \"").append(spItemNo).append("\")'").append(" >").append("<img src='").append(ICON_FILLACROSS).append("' border='0'");
                html.append("/>").append("</td>");
                json.append("\n\t\t\t\"detailItems\" : [ ");
                boolean isDetailItemPresent = false;
                int detailItemCount = 0;
                HashMap<String, String> filterMap = new HashMap<String, String>();
                for (String levelId : levelIdSet) {
                    DataSet levelSamples = this.getFilteredByLevelDS(levelId, spDetail);
                    Map processStagesMap = this.getDistinctProcessStages(levelId, spDetail);
                    for (String stage : processStagesMap.keySet()) {
                        filterMap.clear();
                        filterMap.put(SPDETAIL_PROCESSSTAGE, stage);
                        DataSet stageSamples = levelSamples.getFilteredDataSet(filterMap);
                        int stageSamplesCount = stageSamples.getRowCount();
                        for (int i = 0; i < stageSamplesCount; ++i) {
                            int spDetailNo = stageSamples.getInt(i, "s_samplingplandetailno");
                            String ruleType = stageSamples.getString(i, SPDETAIL_RULETYPE, "");
                            isDetailItemPresent = SamplingPlanDetailItem.isDetailItemPresent(spDetailNo, spItemNo, spDetailItems);
                            StringBuffer toolTipTitle = new StringBuffer();
                            if (stage != null) {
                                toolTipTitle.append(this.tp.translate("Sample Stage: ")).append((String)processStagesMap.get(stage)).append("&#013;");
                            }
                            if (levelId != null) {
                                toolTipTitle.append(this.tp.translate("Sample Level: ")).append(levelId).append("&#013;");
                            }
                            if (stageSamples.getString(i, SPDETAIL_SOURCELABEL, "").length() > 0) {
                                toolTipTitle.append(this.tp.translate("Sample Label: ")).append(stageSamples.getString(i, SPDETAIL_SOURCELABEL, "")).append("&#013;");
                            }
                            String ruleValue = null;
                            ruleValue = "".equals(ruleType) ? RULETYPE_NOTDEFINED : (ruleType.equals("Number") ? stageSamples.getString(i, SPDETAIL_RULEVALUE) : "(" + this.tp.translate(ruleType) + ")");
                            toolTipTitle.append(this.tp.translate("Sample: ")).append(ruleValue).append(" &#39;").append(stageSamples.getString(i, SPDETAIL_TEMPLATEID1)).append("&#39; &#013;");
                            if (itemSDCId.equalsIgnoreCase("workitem")) {
                                toolTipTitle.append(this.tp.translate(this.getItemDisplayName(itemSDCId))).append(": ").append(keyId).append("&#013;");
                            } else if (itemSDCId.equalsIgnoreCase("specsdc")) {
                                toolTipTitle.append(this.tp.translate("Specification")).append(": ").append(keyId).append("&#013;");
                            }
                            html.append("\n\t\t<td").append(" align='center'").append(" class='" + this.CSS_DETAILITEMS + "'").append(" ").append("levelid").append("='").append(levelId).append("'").append(" ").append(SPDETAIL_PROCESSSTAGE).append("='").append(stage).append("'").append(" ").append(SPDETAIL_SOURCELABEL).append("='").append(stageSamples.getString(i, SPDETAIL_SOURCELABEL, "")).append("'").append(">").append("<input type='checkbox'").append(" id='_" + this.elementId + "_item-" + spItemNo + "_detail-" + spDetailNo + "' ").append(" name='_" + this.elementId + "_detailitem' ").append(isDetailItemPresent ? " checked " : "").append("".equals(ruleType) || this.isViewMode ? " disabled " : "").append(" onclick='samplingPlanDetailItem.detailItemEdited(\"" + this.elementId + "\",\"" + spItemNo + "\",\"" + spDetailNo + "\");'").append(" s_samplingplandetailno='").append(spDetailNo).append("'").append(" s_samplingplanitemno='").append(spItemNo).append("'").append("title='" + toolTipTitle + "'").append(">").append("</td>");
                            if (detailItemCount != 0) {
                                json.append(",");
                            }
                            json.append("\n\t\t\t\t{");
                            json.append("\n\t\t\t\t\t\"detailNo\" : \"" + spDetailNo + "\",");
                            json.append("\n\t\t\t\t\t\"preValue\" : \"" + (isDetailItemPresent ? DETAILITEM_CHECKED : DETAILITEM_UNCHECKED) + "\",");
                            json.append("\n\t\t\t\t\t\"postValue\" : \"U\"");
                            json.append("\n\t\t\t\t}");
                            ++detailItemCount;
                        }
                    }
                }
                html.append("\n\t</tr>");
                json.append("\n\t\t\t]");
                json.append("\n\t\t}");
            }
            json.append("\n\t]").append("\n};");
            json.append("\n</script>");
        }
        catch (Exception e) {
            throw new Exception("Could Not Render Sampling Plan Detail Items: " + e);
        }
    }

    private void renderTemplateTable(StringBuffer html, DataSet spDetail, Map allowedSPItems, Set<String> levelIdSet) throws SapphireException {
        try {
            Logger.logInfo("Start rendering template table");
            html.append("\n<div style='display:none;'>");
            html.append("\n<table border=1 id='_" + this.elementId + "_templatetable' >");
            html.append("\n<tbody>");
            html.append("\n\t<tr id='_" + this.elementId + "_row-[spitemno]' onmouseover='" + JS_OBJECT + ".mouseOver(this)' onmouseout='" + JS_OBJECT + ".mouseOut(this)'>");
            html.append("\n\t\t<td class='" + this.CSS_HEADER_Y + "'>");
            html.append("\n\t\t\t<input type='checkbox' id='_" + this.elementId + "_item-[spitemno]' name='_" + this.elementId + "_item'>");
            html.append("\n\t\t</td>");
            html.append("\n\t\t<td class=\"" + this.CSS_HEADER_Y + "\">");
            html.append("\n\t\t\t<select id='_" + this.elementId + "_item-[spitemno]_sdcid'").append(" onchange='samplingPlanDetailItem.clearItemKeyIds(\"" + this.elementId + "\", \"[spitemno]\",this.value);").append("samplingPlanDetailItem.itemEdited(\"" + this.elementId + "\",\"[spitemno]\");").append("'").append(">");
            for (String itemSDCId : allowedSPItems.keySet()) {
                html.append("\n\t\t\t\t<option value='" + itemSDCId + "'>" + this.tp.translate(this.getItemDisplayName(itemSDCId)) + "</option>");
            }
            html.append("\n\t\t\t</select>");
            html.append("\n\t\t</td>");
            html.append("\n\t\t<td class=\"" + this.CSS_HEADER_Y + "\" valign='middle'>");
            html.append("\n\t\t\t<input type='text' id='_" + this.elementId + "_item-[spitemno]_keyid' readonly size='15' >");
            html.append("<a href='javascript:samplingPlanDetailItem.openSingleSDILookup(\"" + this.elementId + "\",\"[spitemno]\");'>");
            html.append("<img src='WEB-CORE/elements/images/lookup.gif' border=0></a><br>");
            html.append("\n\t\t\t<input type='hidden' id='_" + this.elementId + "_item-[spitemno]_keyid1' readonly size='10' >");
            html.append("\n\t\t\t<input type='hidden' id='_" + this.elementId + "_item-[spitemno]_keyid2' readonly size='10' >");
            html.append("\n\t\t\t<input type='hidden' id='_" + this.elementId + "_item-[spitemno]_keyid3' readonly size='10' >");
            html.append("\n\t\t</td>");
            html.append("\n\t\t<td align='center' class='gridmaint_fieldtitle'>").append("<input type='checkbox'").append(this.isViewMode ? " disabled " : "").append(" onclick='").append(JS_OBJECT).append(".toggleDetailItemCheckbox(\"" + this.elementId + "\", this.checked, false, \"[spitemno]\")'").append(" >").append("<img src='").append(ICON_FILLACROSS).append("' border='0'");
            html.append("/>").append("</td>");
            HashMap<String, String> filterMap = new HashMap<String, String>();
            for (String levelId : levelIdSet) {
                DataSet levelSamples = this.getFilteredByLevelDS(levelId, spDetail);
                Map processStagesMap = this.getDistinctProcessStages(levelId, spDetail);
                for (String stage : processStagesMap.keySet()) {
                    filterMap.clear();
                    filterMap.put(SPDETAIL_PROCESSSTAGE, stage);
                    DataSet stageSamples = levelSamples.getFilteredDataSet(filterMap);
                    int stageSamplesCount = stageSamples.getRowCount();
                    for (int i = 0; i < stageSamplesCount; ++i) {
                        int spDetailNo = stageSamples.getInt(i, "s_samplingplandetailno");
                        spDetailNo = stageSamples.getInt(i, "s_samplingplandetailno");
                        String ruleType = stageSamples.getString(i, SPDETAIL_RULETYPE, "");
                        html.append("\n\t\t<td").append(" align='center'").append(" class='" + this.CSS_DETAILITEMS + "'").append(" ").append("levelid").append("='").append(levelId).append("'").append(" ").append(SPDETAIL_PROCESSSTAGE).append("='").append(stage).append("'").append(" ").append(SPDETAIL_SOURCELABEL).append("='").append(stageSamples.getString(i, SPDETAIL_SOURCELABEL, "")).append("'").append(">").append("<input type='checkbox'").append(" id='_" + this.elementId + "_item-[spitemno]_detail-" + spDetailNo + "'").append(" name='_" + this.elementId + "_detailitem' ").append("".equals(ruleType) ? " disabled " : "").append(" onclick='samplingPlanDetailItem.detailItemEdited(\"" + this.elementId + "\",\"[spitemno]\",\"" + spDetailNo + "\");'").append(" s_samplingplandetailno='").append(spDetailNo).append("'").append(" s_samplingplanitemno='[spitemno]'").append(" >").append("</td>");
                    }
                }
            }
            html.append("\n\t</tr>");
            html.append("\n</tbody>");
            html.append("\n</table>");
            html.append("\n</div>");
            Logger.logInfo("Finished rendering template table");
        }
        catch (Exception e) {
            throw new SapphireException("Could Not render Template Table: " + e);
        }
    }

    private void renderButtons(StringBuffer html, Map allowedSPItems) throws SapphireException {
        if (!this.isViewMode) {
            Button button;
            html.append("\n<table border='0' cellspacing='0' cellpadding='5'>");
            html.append("\n\t<tr>");
            String buttonStyle = "";
            String buttonText = "";
            double buttonWidthMultiplyingFactor = 7.5;
            int buttonWidthMinimum = 30;
            int itemCount = 0;
            for (String itemSDCId : allowedSPItems.keySet()) {
                buttonText = this.tp.translate("Add " + this.getItemDisplayName(itemSDCId));
                button = new Button(this.pageContext);
                button.setId("_" + this.elementId + "_add");
                button.setText(buttonText);
                button.setImg(ICON_ADDROW);
                button.setTip(this.tp.translate("Add " + this.getItemDisplayName(itemSDCId)));
                button.setStyle(buttonStyle + "width:" + ((double)buttonText.length() * buttonWidthMultiplyingFactor + (double)buttonWidthMinimum) + "px;");
                button.setAction("samplingPlanDetailItem.openMultipleSDILookup('" + this.elementId + "','" + itemSDCId + "');");
                html.append("\n\t\t<td>");
                html.append(button.getHtml());
                html.append("</td>");
                ++itemCount;
            }
            button = new Button(this.pageContext);
            buttonText = this.tp.translate("Remove");
            button.setId("_" + this.elementId + "_remove");
            button.setText(buttonText);
            button.setTip(this.tp.translate("Remove an Item"));
            button.setAction("samplingPlanDetailItem.removeRow('" + this.elementId + "');");
            button.setImg(ICON_REMOVEROW);
            button.setStyle(buttonStyle + "width:" + ((double)buttonText.length() * buttonWidthMultiplyingFactor + (double)buttonWidthMinimum) + "px;");
            html.append("\n\t\t<td>");
            html.append(button.getHtml());
            html.append("</td>");
            button = new Button(this.pageContext);
            buttonText = "";
            button.setId("_" + this.elementId + "_moveUp");
            button.setText(buttonText);
            button.setTip(this.tp.translate("Move Up"));
            button.setAction("samplingPlanDetailItem.moveUp('" + this.elementId + "', '" + this.minTopRows + "');");
            button.setImg(ICON_MOVEUP);
            button.setStyle(buttonStyle + "width:" + ((double)buttonText.length() * buttonWidthMultiplyingFactor + (double)buttonWidthMinimum) + "px;");
            html.append("\n\t\t<td>");
            html.append(button.getHtml());
            html.append("</td>");
            button = new Button(this.pageContext);
            buttonText = "";
            button.setId("_" + this.elementId + "_moveDown");
            button.setText(buttonText);
            button.setTip(this.tp.translate("Move Down"));
            button.setAction("samplingPlanDetailItem.moveDown('" + this.elementId + "');");
            button.setImg(ICON_MOVEDOWN);
            button.setStyle(buttonStyle + "width:" + ((double)buttonText.length() * buttonWidthMultiplyingFactor + (double)buttonWidthMinimum) + "px;");
            html.append("\n\t\t<td>");
            html.append(button.getHtml());
            html.append("</td>");
            html.append("\n\t</tr>");
            html.append("\n</table>");
        }
    }

    private void renderScripts(StringBuffer html, DataSet spDetail, Map allowedSPItems, Set<String> levelIdSet, DataSet spItems) throws SapphireException {
        try {
            StringBuffer jsonItemRowTemp = new StringBuffer();
            html.append("\n<script type=\"text/javascript\" language=\"JavaScript\"  src=\"WEB-CORE/elements/samplingplandetailitem/samplingplandetailitem.js\"></script>");
            html.append("\n<script type=\"text/javascript\" language=\"JavaScript\"  src=\"WEB-CORE/scripts/json.js\"></script>");
            html.append("\n<script type=\"text/javascript\" language=\"JavaScript\">");
            html.append("\n\tvar _" + this.elementId + "_allowed_spItems = new Array();");
            html.append("\n\tvar _" + this.elementId + "_allowed_spItemsLookup = new Array();");
            html.append("\n\tvar _" + this.elementId + "_allowed_spItemsLookupMulti = new Array();");
            html.append("\n\tvar _" + this.elementId + "_allowed_spItemsKeyCols = new Array();");
            int itemCount = 0;
            for (String itemSDCId : allowedSPItems.keySet()) {
                String itemTableId = (String)allowedSPItems.get(itemSDCId);
                html.append("\n\t_" + this.elementId + "_allowed_spItems[" + itemCount + "] = '" + itemSDCId + "';");
                html.append("\n\t_" + this.elementId + "_allowed_spItemsLookup['" + itemSDCId + "'] = '" + this.getItemLookupURL(itemSDCId, false) + "';");
                html.append("\n\t_" + this.elementId + "_allowed_spItemsLookupMulti['" + itemSDCId + "'] = '" + this.getItemLookupURL(itemSDCId, false) + "';");
                html.append("\n\t_" + this.elementId + "_allowed_spItemsKeyCols['" + itemSDCId + "'] = '" + this.getItemKeyColIds(itemSDCId) + "';");
                ++itemCount;
            }
            jsonItemRowTemp.append("\n\n\tvar _" + this.elementId + "_jsonItemRowTemplate = {").append("\n\t\t\"itemNo\" : \"\",").append("\n\t\t\"sdcId\" : \"\",").append("\n\t\t\"keyId1\" : \"\",").append("\n\t\t\"keyId2\" : \"\",").append("\n\t\t\"keyId3\" : \"\",").append("\n\t\t\"userSequence\" : \"\",").append("\n\t\t\"status\" : \"N\"").append("\n\t};");
            jsonItemRowTemp.append("\n\t_" + this.elementId + "_jsonItemRowTemplate.sdcId = _" + this.elementId + "_allowed_spItems[0];");
            html.append(jsonItemRowTemp);
            html.append("\n\tvar _" + this.elementId + "_spDetailNos = new Array();");
            StringBuffer detailitemArrJson = new StringBuffer();
            detailitemArrJson.append("\n\n\tvar _" + this.elementId + "_detailItemArr = [");
            int detailItemCount = 0;
            HashMap<String, String> filterMap = new HashMap<String, String>();
            for (String levelId : levelIdSet) {
                DataSet levelSamples = this.getFilteredByLevelDS(levelId, spDetail);
                Map processStagesMap = this.getDistinctProcessStages(levelId, spDetail);
                for (String stage : processStagesMap.keySet()) {
                    filterMap.clear();
                    filterMap.put(SPDETAIL_PROCESSSTAGE, stage);
                    DataSet stageSamples = levelSamples.getFilteredDataSet(filterMap);
                    int stageSamplesCount = stageSamples.getRowCount();
                    for (int i = 0; i < stageSamplesCount; ++i) {
                        int detailNo = stageSamples.getInt(i, "s_samplingplandetailno");
                        html.append("\n\t _" + this.elementId + "_spDetailNos[" + i + "] = '" + detailNo + "';");
                        if (detailItemCount != 0) {
                            detailitemArrJson.append(",");
                        }
                        detailitemArrJson.append("\n\t\t{").append("\n\t\t\t\"detailNo\" : \"" + detailNo + "\",").append("\n\t\t\t\"preValue\" : \"F\",").append("\n\t\t\t\"postValue\" : \"U\"").append("\n\t\t}");
                        ++detailItemCount;
                    }
                }
            }
            detailitemArrJson.append("\n\t];");
            html.append(detailitemArrJson);
            int spMaxItemNo = SamplingPlanDetailItem.getMax(spItems.getColumnValues("s_samplingplanitemno", ";").split(";"), this.logger);
            html.append("\n\n\tvar _" + this.elementId + "_spMaxItemNo = '" + spMaxItemNo + "';");
            int spMaxUserSeqNo = SamplingPlanDetailItem.getMax(spItems.getColumnValues(SPITEM_USERSEQ, ";").split(";"), this.logger);
            html.append("\n\tvar _" + this.elementId + "_spMaxUserSeqNo = '" + spMaxUserSeqNo + "';");
            html.append("\n\n\tfunction " + this.elementId + "_alertJSONStr(){").append("\n\t\talert('field: '+document.getElementById('_" + this.elementId + "_jsonstr').value);").append("\n\t}");
            html.append("\n\n\tdocument.getElementById('_" + this.elementId + "_jsonstr').value = objectToJSONString(_" + this.elementId + "_jsonstr);");
            html.append("\n\n</script>");
        }
        catch (Exception e) {
            Logger.logError("Could not render Scripts: " + e);
            throw new SapphireException("Could not render Scripts: " + e);
        }
    }

    private static boolean isDetailItemPresent(int spDetailNo, int spItemNo, DataSet spDetailItems) throws SapphireException {
        try {
            HashMap<String, BigDecimal> filterMap = new HashMap<String, BigDecimal>();
            filterMap.put("s_samplingplanitemno", new BigDecimal(spItemNo));
            filterMap.put("s_samplingplandetailno", new BigDecimal(spDetailNo));
            int detailItemFlag = spDetailItems.findRow(filterMap);
            return detailItemFlag != -1;
        }
        catch (Exception e) {
            throw new SapphireException("Error in determining presence of detailitem: " + e);
        }
    }

    private DataSet getSPItems() throws SapphireException {
        try {
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer spItemQry = new StringBuffer();
            spItemQry.append("SELECT * FROM s_spitem").append(" WHERE s_samplingplanid = ").append(safeSQL.addVar(this.samplingPlanId)).append(" and s_samplingplanversionid = ").append(safeSQL.addVar(this.samplingPlanVersionId)).append(" ORDER BY usersequence, s_samplingplanitemno");
            DataSet spItems = this.qp.getPreparedSqlDataSet(spItemQry.toString(), safeSQL.getValues());
            return spItems;
        }
        catch (Exception e) {
            Logger.logError("getSPItems(): " + e);
            throw new SapphireException("Could Not retrieve allowed SP Items: " + e);
        }
    }

    private DataSet getSPDetail() throws SapphireException {
        try {
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer spDetailQry = new StringBuffer();
            spDetailQry.append("SELECT s.s_samplingplanid, s.s_samplingplanversionid, s.s_samplingplandetailno, s.levelid, s.sourcelabel, s.templatesdcid, s.templatekeyid1, s.templatekeyid2, s.templatekeyid3, s.countruletype, s.countrule, s.processstageid, s.usersequence, p.label FROM s_spdetail s left outer join s_processstage p").append(" ON p.s_samplingplanid = s.s_samplingplanid").append(" AND p.s_samplingplanversionid = s.s_samplingplanversionid").append(" AND p.s_processstageid = s.processstageid").append(" WHERE s.s_samplingplanid = ").append(safeSQL.addVar(this.samplingPlanId)).append(" AND s.s_samplingplanversionid = ").append(safeSQL.addVar(this.samplingPlanVersionId)).append(" ORDER BY s.usersequence");
            DataSet spDetail = this.qp.getPreparedSqlDataSet(spDetailQry.toString(), safeSQL.getValues());
            return spDetail;
        }
        catch (Exception e) {
            Logger.logError("getSPDetail(): " + e);
            throw new SapphireException("Could Not retrieve SP Details: " + e);
        }
    }

    private DataSet getSPDetailItems() throws SapphireException {
        try {
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer spItemQry = new StringBuffer();
            spItemQry.append("SELECT * FROM s_spdetailitem").append(" WHERE s_samplingplanid = ").append(safeSQL.addVar(this.samplingPlanId)).append(" AND s_samplingplanversionid = ").append(safeSQL.addVar(this.samplingPlanVersionId));
            DataSet spDetailItems = this.qp.getPreparedSqlDataSet(spItemQry.toString(), safeSQL.getValues());
            return spDetailItems;
        }
        catch (Exception e) {
            Logger.logError("getSPDetailItems(): " + e);
            throw new SapphireException("Could Not retrieve SP Detail Items: " + e);
        }
    }

    private DataSet getFilteredByLevelDS(String levelId, DataSet spDetail) throws SapphireException {
        HashMap<String, String> filterMap = new HashMap<String, String>();
        filterMap.put("levelid", levelId);
        DataSet temp = spDetail.getFilteredDataSet(filterMap);
        return temp;
    }

    private Set getDistinctLevelId(DataSet spDetail) throws SapphireException {
        String[] levelIds = spDetail.getColumnValues("levelid", ";").split(";");
        LinkedHashSet<String> levelIdSet = new LinkedHashSet<String>();
        for (int i = 0; i < levelIds.length; ++i) {
            levelIdSet.add(levelIds[i]);
        }
        return levelIdSet;
    }

    private Map getDistinctProcessStages(String levelId, DataSet spDetail) throws SapphireException {
        HashMap<String, String> filterMap = new HashMap<String, String>();
        filterMap.put("levelid", levelId);
        DataSet levelSamples = spDetail.getFilteredDataSet(filterMap);
        LinkedHashMap<String, String> levelIdMap = new LinkedHashMap<String, String>();
        int rowCount = levelSamples.getRowCount();
        for (int i = 0; i < rowCount; ++i) {
            levelIdMap.put(levelSamples.getString(i, SPDETAIL_PROCESSSTAGE), levelSamples.getString(i, "label", ""));
        }
        return levelIdMap;
    }

    private Map getAllowedSPItems() throws SapphireException {
        try {
            HashMap<String, String> allowedSPItems = new HashMap<String, String>();
            StringBuffer sdcs = new StringBuffer();
            for (int i = 0; i < this.itemSDCProps.size(); ++i) {
                PropertyList itemSDCProperties = this.itemSDCProps.getPropertyList(i);
                String itemSDCId = itemSDCProperties.getProperty(PROPERTY_DATA_SDCID, "");
                if (!"Y".equalsIgnoreCase(itemSDCProperties.getProperty(PROPERTY_DATA_SHOW)) || itemSDCId.length() <= 0) continue;
                allowedSPItems.put(itemSDCId, "");
            }
            if (allowedSPItems == null || allowedSPItems.size() == 0) {
                throw new SapphireException("No Item SDC provided");
            }
            return allowedSPItems;
        }
        catch (Exception e) {
            Logger.logError("getAllowedSPItems(): " + e);
            throw new SapphireException("Could Not retrieve Item SDCs");
        }
    }

    private DataSet getAllowedSPItemSDIs(String tableId) throws SapphireException {
        try {
            String keyColumnId = "";
            keyColumnId = "workitem".equals(tableId) ? "workitemid" : "specid";
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT ").append(keyColumnId).append(" as keyid1 FROM ").append(tableId);
            return this.qp.getSqlDataSet(sql.toString());
        }
        catch (Exception e) {
            Logger.logError("getAllowedSPItemSDIs(): " + e);
            throw new SapphireException("Could Not retrieve allowed SP Items SDIs: " + e);
        }
    }

    public static int getMax(String[] sourceStrArr, Logger logger) throws SapphireException {
        int max = 0;
        try {
            if (sourceStrArr.length == 0) {
                return 0;
            }
            int n = sourceStrArr.length % 2 == 0 ? sourceStrArr.length / 2 : sourceStrArr.length;
            for (int i = 0; i < n; ++i) {
                int bottom;
                int top = Integer.parseInt(sourceStrArr[i]);
                if (top > (bottom = Integer.parseInt(sourceStrArr[sourceStrArr.length - i - 1]))) {
                    if (top <= max) continue;
                    max = top;
                    continue;
                }
                if (bottom <= max) continue;
                max = bottom;
            }
        }
        catch (NumberFormatException e) {
            Logger.logInfo("SamplingPlanDetailItem", "Unable to retrieve Max from string array. Reset default value");
            max = 0;
        }
        catch (Exception e) {
            Logger.logError("SamplingPlanDetailItem", "Exception occured while retrieving max from string array: " + e);
            throw new SapphireException("getMax(): " + e);
        }
        return max;
    }

    private String getLevelColor(String levelId) throws SapphireException {
        String color = "";
        try {
            PropertyList display = this.element.getPropertyList(PROPERTY_DISPLAY);
            PropertyListCollection levelColors = display.getCollection(PROPERTY_DISP_LEVELCOLORS);
            for (int i = 0; i < levelColors.size(); ++i) {
                if (!levelColors.getPropertyList(i).getProperty("levelid").equals(levelId)) continue;
                color = levelColors.getPropertyList(i).getProperty(PROPERTY_DISP_COLOR);
            }
        }
        catch (Exception e) {
            Logger.logError("getLevelColor(): " + e);
            throw new SapphireException("Unable to retrieve level color");
        }
        finally {
            return color;
        }
    }

    private String getItemDisplayName(String itemSDCId) throws SapphireException {
        String displayName = "";
        try {
            displayName = this.getItemProps(itemSDCId).getProperty(PROPERTY_DATA_DISPLAYNAME, itemSDCId);
            return displayName;
        }
        catch (Exception e) {
            Logger.logError("getItemDisplayName(): " + e);
            throw new SapphireException("Unable to retrieve Item display Name");
        }
        finally {
            return displayName;
        }
    }

    private String getItemLookupURL(String itemSDCId, boolean multiple) throws SapphireException {
        String lookupURL = "";
        try {
            if (multiple) {
                lookupURL = this.getItemProps(itemSDCId).getProperty(PROPERTY_DATA_LOOKUPURLMULTI, "");
            }
            lookupURL = this.getItemProps(itemSDCId).getProperty(PROPERTY_DATA_LOOKUPURL, "");
            return lookupURL;
        }
        catch (Exception e) {
            Logger.logError("getItemLookupURL(): " + e);
            throw new SapphireException("Unable to retrieve Item Lookup URL");
        }
        finally {
            return lookupURL;
        }
    }

    private String getItemKeyColIds(String itemSDCId) throws SapphireException {
        StringBuffer keyColIds = new StringBuffer();
        try {
            SDCProcessor sdcProcessor = this.getSDCProcessor();
            int noOfKeyCols = Integer.parseInt(sdcProcessor.getProperty(itemSDCId, "keycolumns", "0"));
            for (int i = 0; i < noOfKeyCols; ++i) {
                keyColIds.append(";").append(sdcProcessor.getProperty(itemSDCId, "keycolid" + (i + 1), ""));
            }
        }
        catch (Exception e) {
            Logger.logError("getItemKeyColIds(): " + e);
            throw new SapphireException("Unable to retrieve Item Key Col Ids");
        }
        finally {
            return keyColIds.toString().substring(1);
        }
    }

    private PropertyList getItemProps(String itemSDCId) throws SapphireException {
        PropertyList props = null;
        try {
            for (int i = 0; i < this.itemSDCProps.size(); ++i) {
                if (!this.itemSDCProps.getPropertyList(i).getProperty(PROPERTY_DATA_SDCID).equals(itemSDCId)) continue;
                props = this.itemSDCProps.getPropertyList(i);
            }
            if (props == null) {
                props = new PropertyList();
                return props;
            }
        }
        catch (Exception e) {
            Logger.logError("getItemProps(): " + e);
            throw new SapphireException("Unable to retrieve item properties");
        }
        finally {
            return props;
        }
    }

    private boolean checkLockState() {
        boolean lockFlag;
        this.logger.debug("checkLockState called...");
        try {
            DataSet data = this.sdiInfo.getDataSet("primary");
            String lockedBy = data.getValue(0, "__lockedby", "");
            if (lockedBy == null || lockedBy.length() == 0) {
                lockFlag = false;
                this.logger.debug("Not locked.");
            } else {
                lockFlag = true;
                this.logger.debug("Locked by " + lockedBy + ".");
            }
        }
        catch (Exception e) {
            lockFlag = true;
            this.logger.warn("Could not obtain lock information therefore default to locked.");
        }
        return lockFlag;
    }

    private boolean isViewMode() {
        boolean isViewMode = false;
        isViewMode = this.checkLockState();
        if (!isViewMode) {
            isViewMode = "Y".equals(this.element.getProperty(PROPERTY_READONLY, "N"));
        }
        return isViewMode;
    }
}

