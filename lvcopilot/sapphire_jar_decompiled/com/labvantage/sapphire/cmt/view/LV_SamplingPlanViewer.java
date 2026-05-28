/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt.view;

import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.view.SDISnapshotViewer;
import com.labvantage.sapphire.modules.configreport.util.DDTLabelsUtil;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LV_SamplingPlanViewer
extends SDISnapshotViewer {
    private static final String SPDETAIL_DETAILNO = "s_samplingplandetailno";
    private static final String SPDETAIL_LEVELID = "levelid";
    private static final String SPDETAIL_SOURCELABEL = "sourcelabel";
    private static final String SPDETAIL_PROCESSSTAGE = "processstageid";
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
    private static final String RULETYPE_NOTDEFINED = "(Not Defined)";

    @Override
    protected void renderItemDetailsDiff(ConfigReportContent configReportContent, SDISnapshotItem sourceItem, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns) throws SapphireException {
        try {
            configReportContent.appendSpecialContent(this.renderProcessStages(sourceItem.getSDIData(), refItem == null ? new SDIData() : refItem.getSDIData(), hideEmptyColumns));
            configReportContent.appendSpecialContent(this.renderLevelSamples(sourceItem.getSDIData(), refItem == null ? new SDIData() : refItem.getSDIData(), hideEmptyColumns));
            ConfigReportContent tests = new ConfigReportContent("nodecontents", this.translationProcessor);
            tests.startSubSection("Tests and Specs", "");
            ConfigReportContent tableitems = this.renderTestSpecsDiff(sourceItem.getSDIData(), refItem == null ? new SDIData() : refItem.getSDIData());
            if (tableitems.length() > 0) {
                tests.appendSpecialContent(tableitems);
                configReportContent.appendNodeContent(tests, "s_spdetailitem", "Tests and Specs");
            }
        }
        catch (SapphireException e) {
            configReportContent.append("<P>Failed to render Test/Specs");
        }
        ConfigReportContent str = new ConfigReportContent("categories", this.translationProcessor);
        this.renderCategores(str, sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        configReportContent.appendSpecialContent(str);
        str = new ConfigReportContent("other", this.translationProcessor);
        this.renderOtherCommonDetails(str, sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        configReportContent.appendSpecialContent(str);
    }

    private ConfigReportContent renderProcessStages(SDIData srcSDIData, SDIData refSDIData, boolean hideEmptyColumns) throws SapphireException {
        DataSet src = this.getProcessStage(srcSDIData);
        DataSet ref = this.getProcessStage(refSDIData);
        ConfigReportContent content = new ConfigReportContent("processtages", this.translationProcessor);
        String tablelabel = "Stages";
        String itemdisplay = "[Stage]";
        HashMap<String, String> columnTitleMap = DDTLabelsUtil.getColumnTitleMap(this.getSDCProcessor(), "s_processstage", src.getColumns());
        ConfigReportContent stages = new ConfigReportContent("stages", this.translationProcessor);
        stages.startSubSection(tablelabel, "");
        ConfigReportContent tableofitems = new ConfigReportContent("table", this.translationProcessor);
        tableofitems.renderDetailTablesDiff(columnTitleMap, "s_processstage", tablelabel, itemdisplay, src, ref, new String[]{"Stage"}, this.getTranslationProcessor(), hideEmptyColumns);
        if (tableofitems.length() > 0) {
            stages.appendSpecialContent(tableofitems);
            content.appendNodeContent(stages, "s_processstage", tablelabel);
        }
        return content;
    }

    private ConfigReportContent renderLevelSamples(SDIData srcSDIData, SDIData refSDIData, boolean hideEmptyColumns) throws SapphireException {
        DataSet src = this.getLevelSamples(srcSDIData);
        DataSet ref = this.getLevelSamples(refSDIData);
        ConfigReportContent content = new ConfigReportContent("levelsamples", this.translationProcessor);
        String tablelabel = "Level Samples";
        String itemdisplay = "[Level], [Source Label]";
        HashMap<String, String> columnTitleMap = DDTLabelsUtil.getColumnTitleMap(this.getSDCProcessor(), "levelsamples", src.getColumns());
        ConfigReportContent levelsamples = new ConfigReportContent("levelsamples", this.translationProcessor);
        levelsamples.startSubSection(tablelabel, "");
        ConfigReportContent tableofitems = new ConfigReportContent("table", this.translationProcessor);
        tableofitems.renderDetailTablesDiff(columnTitleMap, "s_spdetail", tablelabel, itemdisplay, src, ref, new String[]{"Level", "Source Label"}, this.getTranslationProcessor(), hideEmptyColumns);
        if (tableofitems.length() > 0) {
            levelsamples.appendSpecialContent(tableofitems);
            content.appendNodeContent(levelsamples, "s_spdetail", tablelabel);
        }
        return content;
    }

    private DataSet getProcessStage(SDIData sdiData) {
        DataSet ds = sdiData.getDataset("s_processstage");
        DataSet ret = new DataSet();
        ret.setColidCaseSensitive(true);
        ret.addColumn("Stage", 0);
        ret.addColumn("Count", 0);
        ret.addColumn("Template", 0);
        if (ds != null) {
            for (int i = 0; i < ds.getRowCount(); ++i) {
                ret.addRow();
                ret.setValue(i, "Stage", ds.getValue(i, "label"));
                ret.setValue(i, "Count", ds.getValue(i, "repeatcount"));
                ret.setValue(i, "Template", ds.getValue(i, SPDETAIL_TEMPLATEID1, ""));
            }
        }
        return ret;
    }

    private DataSet getLevelSamples(SDIData sdiData) {
        DataSet ds = sdiData.getDataset("s_spdetail");
        DataSet ret = new DataSet();
        ret.setColidCaseSensitive(true);
        ret.addColumn("Level", 0);
        ret.addColumn("Stage", 0);
        ret.addColumn("Source Label", 0);
        ret.addColumn("Template", 0);
        ret.addColumn("Count", 0);
        ret.addColumn("Department", 0);
        if (ds != null) {
            for (int i = 0; i < ds.getRowCount(); ++i) {
                ret.addRow();
                ret.setValue(i, "Level", ds.getValue(i, SPDETAIL_LEVELID));
                ret.setValue(i, "Stage", ds.getValue(i, SPDETAIL_PROCESSSTAGE));
                ret.setValue(i, "Source Label", ds.getValue(i, SPDETAIL_SOURCELABEL, ""));
                ret.setValue(i, "Template", ds.getValue(i, SPDETAIL_TEMPLATEID1));
                ret.setValue(i, "Count", ds.getValue(i, SPDETAIL_RULEVALUE));
                ret.setValue(i, "Department", ds.getValue(i, "defaultdepartmentid"));
            }
        }
        return ret;
    }

    @Override
    public String[] getIgnoreDataSets() {
        return new String[]{"s_spitem"};
    }

    public ConfigReportContent renderTestSpecsDiff(SDIData src, SDIData ref) throws SapphireException {
        int noOfSPDetailRows;
        Logger.logInfo("SamplingPlanDetailItem: getHtml(): entered");
        StringBuffer html = new StringBuffer();
        DataSet spDetail = null;
        Set srclevelIdSet = null;
        Set reflevelIdSet = null;
        DataSet spItems = null;
        DataSet spDetailItems = null;
        DataSet refspDetailItems = null;
        DataSet srcProcessStages = null;
        DataSet refProcessStages = null;
        spItems = src.getDataset("s_spitem");
        spDetailItems = src.getDataset("s_spdetailitem");
        spDetail = src.getDataset("s_spdetail");
        DataSet refspItems = ref.getDataset("s_spitem");
        refspDetailItems = ref.getDataset("s_spdetailitem");
        srcProcessStages = src.getDataset("s_processstage");
        refProcessStages = ref.getDataset("s_processstage");
        DataSet refspDetail = ref.getDataset("s_spdetail");
        if (refspDetail != null && !refspDetail.isValidColumn(SPDETAIL_PROCESSSTAGE)) {
            refspDetail.addColumn("processtageid", 0);
        }
        if (spItems == null) {
            spItems = new DataSet();
        }
        if (spDetail == null) {
            spDetail = new DataSet();
        }
        if (spDetailItems == null) {
            spDetailItems = new DataSet();
        }
        if (refspDetail == null) {
            refspDetail = new DataSet();
        }
        if (refspDetailItems == null) {
            refspDetailItems = new DataSet();
        }
        if (srcProcessStages == null) {
            srcProcessStages = new DataSet();
        }
        if (refProcessStages == null) {
            refProcessStages = new DataSet();
        }
        if ((noOfSPDetailRows = spDetail.getRowCount()) == 0) {
            this.getTranslationProcessor().translate("No Level Samples defined yet");
        }
        Logger.logInfo("No of SamplingPlan Details: " + noOfSPDetailRows);
        srclevelIdSet = this.getDistinctLevelId(spDetail);
        reflevelIdSet = this.getDistinctLevelId(refspDetail);
        html.append("\n<table id='_maintable' class='view' cellspacing='0' >");
        html.append("\n<tbody>");
        ConfigReportContent content = new ConfigReportContent("TestSpecs", this.translationProcessor);
        if (srclevelIdSet.size() == 0 && reflevelIdSet.size() == 0) {
            return content;
        }
        this.renderLevel(html, srclevelIdSet, reflevelIdSet, spDetail, refspDetail);
        this.renderProcessStages(html, srclevelIdSet, reflevelIdSet, spDetail, refspDetail, srcProcessStages, refProcessStages);
        this.renderSourceLabels(html, srclevelIdSet, reflevelIdSet, spDetail, refspDetail);
        this.renderSamples(html, srclevelIdSet, reflevelIdSet, spDetail, refspDetail);
        try {
            this.renderSPItems(html, spItems, refspItems, srclevelIdSet, spDetail, spDetailItems, refspDetailItems);
        }
        catch (Exception e) {
            html.append("<B>Failed to render spitems");
        }
        html.append("</tbody>\n");
        html.append("</table>\n");
        content.append(html);
        return content;
    }

    private void renderProcessStages(StringBuffer html, Set<String> srclevelIdSet, Set<String> refLevelIdSet, DataSet srcspDetail, DataSet refspDetail, DataSet srcProcessStages, DataSet refProcessStages) throws SapphireException {
        try {
            html.append("\n\t<tr>");
            html.append("\n\t\t<td class='viewlhs' colspan=2><b>").append(this.translationProcessor.translate("Stages")).append("</b></td>");
            for (String levelId : srclevelIdSet) {
                DataSet levelSamples = this.getFilteredByLevelDS(levelId, srcspDetail);
                DataSet refLevlSamples = this.getFilteredByLevelDS(levelId, refspDetail);
                HashMap<String, String> filterMap = new HashMap<String, String>();
                Map srcprocessStagesMap = this.getDistinctProcessStages(levelId, levelSamples, srcProcessStages);
                Map refprocessStagesMap = this.getDistinctProcessStages(levelId, refLevlSamples, refProcessStages);
                for (String stage : srcprocessStagesMap.keySet()) {
                    filterMap.clear();
                    filterMap.put(SPDETAIL_PROCESSSTAGE, stage);
                    DataSet srcstageSamples = levelSamples.getFilteredDataSet(filterMap);
                    int colSpan = srcstageSamples.getRowCount();
                    if (colSpan <= 0) continue;
                    html.append("\n\t\t<td align='center' class='viewrhs'>").append(ConfigReportContent.getDiffString((String)srcprocessStagesMap.get(stage), (String)refprocessStagesMap.get(stage))).append("</td>");
                }
            }
            html.append("\n\t</tr>");
        }
        catch (Exception e) {
            throw new SapphireException("Could Not render Source Labels: " + e);
        }
    }

    private String getRefSourceLabel(String sourcelabel, String stage, String levelId, DataSet srcspDetails, DataSet refspDetails) {
        int row;
        DataSet match;
        String samplingplandetailno;
        String ret = "";
        HashMap<String, String> filter = new HashMap<String, String>();
        if (stage.length() > 0) {
            filter.put(SPDETAIL_PROCESSSTAGE, stage);
        }
        filter.put(SPDETAIL_LEVELID, levelId);
        filter.put(SPDETAIL_SOURCELABEL, sourcelabel);
        if (srcspDetails != null && (samplingplandetailno = (match = srcspDetails.getFilteredDataSet(filter)).getValue(0, "s_samplingplandetailno", "")).length() > 0 && (row = refspDetails.findRow("s_samplingplandetailno", samplingplandetailno)) > -1) {
            ret = refspDetails.getString(row, SPDETAIL_SOURCELABEL, "");
        }
        return ret;
    }

    private Set<String> getDistinctStageList(String levelId, DataSet spDetail) {
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put(SPDETAIL_LEVELID, levelId);
        DataSet levelspdetails = spDetail.getFilteredDataSet(filter);
        LinkedHashSet<String> s = new LinkedHashSet<String>();
        if (levelspdetails != null) {
            for (int i = 0; i < levelspdetails.getRowCount(); ++i) {
                if (levelspdetails.getString(i, SPDETAIL_PROCESSSTAGE, "").length() <= 0) continue;
                s.add(levelspdetails.getString(i, SPDETAIL_PROCESSSTAGE));
            }
        }
        return s;
    }

    private Set<String> getDistinctSourceLabelList(String stage, String levelId, DataSet spDetail) {
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put(SPDETAIL_LEVELID, levelId);
        if (stage.length() > 0) {
            filter.put(SPDETAIL_PROCESSSTAGE, stage);
        }
        DataSet levelstagespdetails = spDetail.getFilteredDataSet(filter);
        LinkedHashSet<String> s = new LinkedHashSet<String>();
        if (levelstagespdetails != null) {
            for (int i = 0; i < levelstagespdetails.getRowCount(); ++i) {
                if (levelstagespdetails.getString(i, SPDETAIL_SOURCELABEL, "").length() <= 0) continue;
                s.add(levelstagespdetails.getString(i, SPDETAIL_SOURCELABEL));
            }
        }
        return s;
    }

    private void renderSourceLabels(StringBuffer html, Set<String> srclevelIdSet, Set<String> reflevelIdSet, DataSet srcspDetail, DataSet refspDetail) throws SapphireException {
        html.append("\n\t<tr>");
        html.append("\n\t\t<td class='viewlhs' colspan=2><b>").append(this.translationProcessor.translate("Source Label")).append("</b></td>");
        LinkedHashSet<String> mergedLevelIds = new LinkedHashSet<String>();
        mergedLevelIds.addAll(srclevelIdSet);
        mergedLevelIds.addAll(reflevelIdSet);
        for (String levelId : mergedLevelIds) {
            Set<String> srcStages = this.getDistinctStageList(levelId, srcspDetail);
            Set<String> refStages = this.getDistinctStageList(levelId, refspDetail);
            LinkedHashSet<String> mergedStages = new LinkedHashSet<String>();
            mergedStages.addAll(srcStages);
            mergedStages.addAll(refStages);
            if (mergedStages.size() == 0) {
                mergedStages.add("");
            }
            for (String stage : mergedStages) {
                Set<String> srcSourceLabels = this.getDistinctSourceLabelList(stage, levelId, srcspDetail);
                for (String sourceLabel : srcSourceLabels) {
                    String reflabel = this.getRefSourceLabel(sourceLabel, stage, levelId, srcspDetail, refspDetail);
                    html.append("<td class='viewrhs' align='center'>" + ConfigReportContent.getDiffString(sourceLabel, reflabel) + "</td>");
                }
            }
        }
        html.append("\n\t</tr>");
    }

    private String getRefRuleStringForSpdetail(String samplingplandetailno, DataSet spDetail) {
        int row;
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("s_samplingplandetailno", samplingplandetailno);
        if (spDetail != null && (row = spDetail.findRow("s_samplingplandetailno", samplingplandetailno)) != -1) {
            String ruleType = spDetail.getString(row, SPDETAIL_RULETYPE, "");
            String ruleValue = "".equals(ruleType) ? RULETYPE_NOTDEFINED : (ruleType.equals("Number") ? spDetail.getString(row, SPDETAIL_RULEVALUE) : "(" + this.translationProcessor.translate(ruleType) + ")");
            String ruleStr = ruleValue + " " + spDetail.getString(row, SPDETAIL_TEMPLATEID1);
            return ruleStr;
        }
        return "";
    }

    private void renderSamples(StringBuffer html, Set<String> srclevelIdSet, Set<String> reflevelIdSet, DataSet srcspDetail, DataSet refSpDetail) throws SapphireException {
        try {
            HashMap<String, String> filterMap = new HashMap<String, String>();
            html.append("\n\t<tr>");
            html.append("\n\t\t<td class='viewlhs' colspan=2><b>").append(this.translationProcessor.translate("Samples")).append("</b></td>");
            for (String levelId : srclevelIdSet) {
                DataSet srclevelSamples = this.getFilteredByLevelDS(levelId, srcspDetail);
                Map processStagesMap = this.getDistinctProcessStageIds(levelId, srcspDetail);
                if (processStagesMap.isEmpty()) {
                    processStagesMap.put(SPDETAIL_PROCESSSTAGE, "");
                }
                for (String stage : processStagesMap.keySet()) {
                    DataSet srcstageLevelSamples;
                    if (stage.length() > 0) {
                        filterMap.clear();
                        filterMap.put(SPDETAIL_PROCESSSTAGE, stage);
                        srcstageLevelSamples = srclevelSamples.getFilteredDataSet(filterMap);
                    } else {
                        srcstageLevelSamples = srclevelSamples;
                    }
                    for (int i = 0; i < srcstageLevelSamples.getRowCount(); ++i) {
                        String srcruleType = srcstageLevelSamples.getString(i, SPDETAIL_RULETYPE, "");
                        String samplingplandetailno = srcstageLevelSamples.getValue(i, "s_samplingplandetailno", "");
                        String srcruleValue = "".equals(srcruleType) ? RULETYPE_NOTDEFINED : (srcruleType.equals("Number") ? srcstageLevelSamples.getString(i, SPDETAIL_RULEVALUE) : "(" + this.translationProcessor.translate(srcruleType) + ")");
                        String sourceRuleStr = srcruleValue + " " + srclevelSamples.getString(i, SPDETAIL_TEMPLATEID1);
                        String refRuleStr = this.getRefRuleStringForSpdetail(samplingplandetailno, refSpDetail);
                        html.append("<td class='viewrhs' align='center'>").append(ConfigReportContent.getDiffString(sourceRuleStr, refRuleStr)).append("</td>");
                    }
                }
            }
            html.append("\n\t</tr>");
        }
        catch (Exception e) {
            throw new SapphireException("Could Not render Samples: " + e);
        }
    }

    private void renderLevel(StringBuffer html, Set<String> srclevelIdSet, Set<String> reflevelIdSet, DataSet srcspDetail, DataSet refSpDetail) throws SapphireException {
        try {
            html.append("<tr>");
            html.append("<td class='viewlhs' colspan=2><b>").append(this.translationProcessor.translate("Level")).append("</b></td>");
            for (String levelId : srclevelIdSet) {
                int colSpan = this.getFilteredByLevelDS(levelId, srcspDetail).getRowCount();
                if (!reflevelIdSet.contains(levelId)) {
                    levelId = ConfigReportContent.getNewString(levelId);
                }
                html.append("<td class='viewrhs' align='center' colspan='" + colSpan + "'>").append(levelId);
                html.append("</td>");
            }
            for (String levelId : reflevelIdSet) {
                if (srclevelIdSet.contains(levelId)) continue;
                levelId = ConfigReportContent.getDeletedString(levelId);
                html.append("<td class='viewrhs' align='center'>").append(levelId);
                html.append("</td>");
            }
            html.append("</tr>");
        }
        catch (Exception e) {
            throw new SapphireException("Could Not render Levels: " + e);
        }
    }

    private void renderSPItems(StringBuffer html, DataSet srcspitems, DataSet refspitems, Set<String> levelIdSet, DataSet srcspdetail, DataSet srcspdetailitems, DataSet refspdetailitems) throws Exception {
        try {
            String elementId = "spitemselement";
            int spItemsCount = srcspitems.getRowCount();
            if (refspitems == null) {
                refspitems = new DataSet();
            }
            boolean isViewMode = true;
            for (int itemCount = 0; itemCount < spItemsCount; ++itemCount) {
                int spItemNo = srcspitems.getInt(itemCount, "s_samplingplanitemno");
                String itemSDCId = srcspitems.getString(itemCount, SPITEM_SDCID);
                String itemKeyId1 = srcspitems.getString(itemCount, SPITEM_KEYID1);
                String itemKeyId2 = srcspitems.getString(itemCount, SPITEM_KEYID2, "");
                String itemKeyId3 = srcspitems.getString(itemCount, SPITEM_KEYID3, "");
                String itemUserSeq = String.valueOf(srcspitems.getInt(itemCount, SPITEM_USERSEQ));
                html.append("\n\t<tr>");
                String keyId = itemKeyId1;
                if (itemKeyId1 != null && itemKeyId2.length() != 0) {
                    keyId = keyId + " ( " + itemKeyId2;
                    keyId = itemKeyId3.length() != 0 ? keyId + ", " + itemKeyId3 + " )" : keyId + " )";
                }
                boolean exists = false;
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put(SPITEM_SDCID, itemSDCId);
                filter.put(SPITEM_KEYID1, itemKeyId1);
                if (itemKeyId2.length() > 0) {
                    filter.put(SPITEM_KEYID2, itemKeyId2);
                }
                if (itemKeyId3.length() > 0) {
                    filter.put(SPITEM_KEYID3, itemKeyId3);
                }
                if (refspitems.getFilteredDataSet(filter).getRowCount() > 0) {
                    exists = true;
                }
                if (exists) {
                    html.append("\n\t\t<td class='viewrhs' align='center'>" + this.translationProcessor.translate(this.getItemDisplayName(itemSDCId)) + "</td>");
                    html.append("\n\t\t<td class='viewrhs' align='center'>" + keyId + "</td>");
                } else {
                    html.append("\n\t\t<td class='viewrhs' align='center'>" + ConfigReportContent.getNewString(this.translationProcessor.translate(this.getItemDisplayName(itemSDCId))) + "</td>");
                    html.append("\n\t\t<td class='viewrhs' align='center'>" + ConfigReportContent.getNewString(keyId) + "</td>");
                }
                boolean isDetailItemPresent = false;
                HashMap<String, String> filterMap = new HashMap<String, String>();
                for (String levelId : levelIdSet) {
                    DataSet levelSamples = this.getFilteredByLevelDS(levelId, srcspdetail);
                    Map processStagesMap = this.getDistinctProcessStageIds(levelId, srcspdetail);
                    for (String stage : processStagesMap.keySet()) {
                        DataSet stageSamples;
                        if (stage.length() > 0) {
                            filterMap.clear();
                            filterMap.put(SPDETAIL_PROCESSSTAGE, stage);
                            stageSamples = levelSamples.getFilteredDataSet(filterMap);
                        } else {
                            stageSamples = levelSamples;
                        }
                        int stageSamplesCount = stageSamples.getRowCount();
                        for (int i = 0; i < stageSamplesCount; ++i) {
                            int spDetailNo = stageSamples.getInt(i, "s_samplingplandetailno");
                            isDetailItemPresent = LV_SamplingPlanViewer.isDetailItemPresent(spDetailNo, spItemNo, srcspdetailitems);
                            boolean isDetailItemPresentInRef = LV_SamplingPlanViewer.isDetailItemPresent(spDetailNo, spItemNo, refspdetailitems);
                            String srcval = isDetailItemPresent ? "Y" : "";
                            String refVal = isDetailItemPresentInRef ? "Y" : "";
                            html.append("\n\t\t<td").append(" align='center'").append(" class='viewrhs'").append(">").append(ConfigReportContent.getDiffString(srcval, refVal)).append("</td>");
                        }
                    }
                }
                html.append("\n\t</tr>");
            }
        }
        catch (Exception e) {
            throw new Exception("Could Not Render Sampling Plan Detail Items: " + e);
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

    private Map getDistinctProcessStageIds(String levelId, DataSet spDetail) throws SapphireException {
        if (spDetail == null) {
            spDetail = new DataSet();
        }
        HashMap<String, String> filterMap = new HashMap<String, String>();
        filterMap.put(SPDETAIL_LEVELID, levelId);
        DataSet levelSamples = spDetail.getFilteredDataSet(filterMap);
        LinkedHashMap<String, String> levelIdMap = new LinkedHashMap<String, String>();
        int rowCount = levelSamples.getRowCount();
        for (int i = 0; i < rowCount; ++i) {
            String processstageid = levelSamples.getString(i, SPDETAIL_PROCESSSTAGE, "");
            levelIdMap.put(levelSamples.getString(i, SPDETAIL_PROCESSSTAGE, ""), processstageid);
        }
        return levelIdMap;
    }

    private Map getDistinctProcessStages(String levelId, DataSet spDetail, DataSet processStages) throws SapphireException {
        if (spDetail == null) {
            spDetail = new DataSet();
        }
        HashMap<String, String> filterMap = new HashMap<String, String>();
        filterMap.put(SPDETAIL_LEVELID, levelId);
        DataSet levelSamples = spDetail.getFilteredDataSet(filterMap);
        LinkedHashMap<String, String> levelIdMap = new LinkedHashMap<String, String>();
        int rowCount = levelSamples.getRowCount();
        for (int i = 0; i < rowCount; ++i) {
            String processstageid = levelSamples.getString(i, SPDETAIL_PROCESSSTAGE, "");
            if (processstageid.length() <= 0) continue;
            int s = processStages.findRow("s_processstageid", processstageid);
            if (s != -1) {
                levelIdMap.put(levelSamples.getString(i, SPDETAIL_PROCESSSTAGE), processStages.getValue(s, "label"));
                continue;
            }
            levelIdMap.put(levelSamples.getString(i, SPDETAIL_PROCESSSTAGE), processstageid);
        }
        return levelIdMap;
    }

    private DataSet getFilteredByLevelDS(String levelId, DataSet spDetail) throws SapphireException {
        if (spDetail == null) {
            spDetail = new DataSet();
        }
        HashMap<String, String> filterMap = new HashMap<String, String>();
        filterMap.put(SPDETAIL_LEVELID, levelId);
        DataSet temp = spDetail.getFilteredDataSet(filterMap);
        return temp;
    }

    private Set getDistinctLevelId(DataSet spDetail) throws SapphireException {
        LinkedHashSet<String> levelIdSet = new LinkedHashSet<String>();
        if (spDetail != null) {
            String[] levelIds = spDetail.getColumnValues(SPDETAIL_LEVELID, ";").split(";");
            for (int i = 0; i < levelIds.length; ++i) {
                if (levelIds[i].length() <= 0) continue;
                levelIdSet.add(levelIds[i]);
            }
        }
        return levelIdSet;
    }

    private PropertyList getItemProps(String itemSDCId) throws SapphireException {
        PropertyList props = new PropertyList();
        return props;
    }

    private String getItemDisplayName(String itemSDCId) throws SapphireException {
        return itemSDCId;
    }
}

