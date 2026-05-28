/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt.view;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.view.SDISnapshotViewer;
import com.labvantage.sapphire.util.array.ArrayUtil;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class LV_ArrayTransferMethodViewer
extends SDISnapshotViewer {
    @Override
    protected ConfigReportContent renderPrimaryDiff(SDISnapshotItem source, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns, boolean hideInheritedProperties, boolean isChild) throws SapphireException {
        ConfigReportContent content = new ConfigReportContent("Transfer Method", this.translationProcessor);
        SDIData sdiData = source.getSDIData();
        String sdiTitle = this.getFormattedItemLabel(sdiData, this.getSDITableLabelInfo(sdiData.getSdcid())[1]);
        content.startSection(sdiTitle);
        content.startTable();
        content.startRow();
        content.addDiffRowItem("Transfer Method", this.getPrimaryValue(sdiData, "arraytransfermethodid"), this.getPrimaryValue(refItem == null ? new SDIData() : refItem.getSDIData(), "arraytransfermethodid"), this.translationProcessor);
        content.addDiffRowItem("Version", this.getPrimaryValue(sdiData, "arraytransfermethodversionid"), this.getPrimaryValue(refItem == null ? new SDIData() : refItem.getSDIData(), "arraytransfermethodversionid"), 3, this.translationProcessor);
        content.endRow();
        content.startRow();
        content.addDiffRowItem("Description", this.getPrimaryValue(sdiData, "arraytransfermethoddesc"), this.getPrimaryValue(refItem == null ? new SDIData() : refItem.getSDIData(), "arraytransfermethoddesc"), 5, this.translationProcessor);
        content.endRow();
        content.startRow();
        String sourceversionStatus = this.getVersionStatus(sdiData);
        SDIData refSDIData = refItem == null ? new SDIData() : refItem.getSDIData();
        String refversionStatus = this.getVersionStatus(refItem == null ? new SDIData() : refItem.getSDIData());
        content.addDiffRowItem("Version Status", sourceversionStatus, refversionStatus, 5, this.translationProcessor);
        content.endRow();
        content.startRow();
        content.addDiffRowItem("Source Array Type", this.getPrimaryValue(sdiData, "sourcearraytypeid"), this.getPrimaryValue(refSDIData, "sourcearraytypeid"), this.getTranslationProcessor());
        content.addDiffRowItem("Source Array Type Version", this.getPrimaryValue(sdiData, "sourcearraytypeversionid"), this.getPrimaryValue(refSDIData, "sourcearraytypeversionid"), this.getTranslationProcessor());
        content.addDiffRowItem("Number of Source Arrays", this.getPrimaryValue(sdiData, "numsourcearrays"), this.getPrimaryValue(refSDIData, "numsourcearrays"), this.getTranslationProcessor());
        content.endRow();
        content.startRow();
        content.addDiffRowItem("Target Array Type", this.getPrimaryValue(sdiData, "targetarraytypeid"), this.getPrimaryValue(refSDIData, "targetarraytypeid"), this.getTranslationProcessor());
        content.addDiffRowItem("Target Array Type Version", this.getPrimaryValue(sdiData, "targetarraytypeversionid"), this.getPrimaryValue(refSDIData, "targetarraytypeversionid"), this.getTranslationProcessor());
        content.addDiffRowItem("Number of Target Arrays", this.getPrimaryValue(sdiData, "numtargetarrays"), this.getPrimaryValue(refSDIData, "numtargetarrays"), this.getTranslationProcessor());
        content.endRow();
        content.startRow();
        content.addDiffRowItem("Target Volume", this.getPrimaryValue(sdiData, "targetvolume"), this.getPrimaryValue(refSDIData, "targetvolume"), this.translationProcessor);
        content.addDiffRowItem("Target Volume Units", this.getPrimaryValue(sdiData, "targetvolumeunits"), this.getPrimaryValue(refSDIData, "targetvolumeunits"), 3, this.translationProcessor);
        content.endRow();
        content.startRow();
        content.addDiffRowItem("Transfer Concentration", this.getPrimaryValue(sdiData, "targetconcentration"), this.getPrimaryValue(refSDIData, "targetconcentration"), this.translationProcessor);
        content.addDiffRowItem("Transfer Concentration Units", this.getPrimaryValue(sdiData, "targetconcentrationunits"), this.getPrimaryValue(refSDIData, "targetconcentrationunits"), 3, this.translationProcessor);
        content.endRow();
        content.startRow();
        content.addDiffRowItem("Create Child Samples", this.getPrimaryValue(sdiData, "createchildsampleflag", "N"), this.getPrimaryValue(refSDIData, "createchildsampleflag", "N"), this.translationProcessor);
        content.addDiffRowItem("Sample Type", this.getPrimaryValue(sdiData, "sampletypeid", "None"), this.getPrimaryValue(refSDIData, "sampletypeid", "None"), 3, this.translationProcessor);
        content.endRow();
        content.startRow();
        content.addDiffRowItem("Transfer Operation", this.getPrimaryValue(sdiData, "transferoperation"), this.getPrimaryValue(refSDIData, "transferoperation"), this.translationProcessor);
        content.addDiffRowItem("Sub Operation", this.getSubOperation(sdiData), this.getSubOperation(refSDIData), this.translationProcessor);
        content.addDiffRowItem("Transpose", this.getTranspose(sdiData), this.getTranspose(refSDIData), this.translationProcessor);
        content.endRow();
        content.endTable();
        content.append(this.renderTransferMap(sdiData, refSDIData));
        return content;
    }

    private String renderTransferMap(SDIData source, SDIData ref) {
        ConfigReportContent content = new ConfigReportContent("transfermap", this.translationProcessor);
        DataSet primary = source.getDataset("primary");
        DataSet refprimary = ref.getDataset("primary");
        boolean similarones = false;
        if (refprimary == null) {
            refprimary = new DataSet();
            refprimary.addRow();
            similarones = true;
        }
        if (primary.getString(0, "sourcearraytypeid", "").equals(refprimary.getString(0, "sourcearraytypeid", "")) && primary.getString(0, "targetarraytypeid", "").equals(refprimary.getString(0, "targetarraytypeid", ""))) {
            int numofsources = primary.getInt(0, "numsources");
            int numoftargets = primary.getInt(0, "numtargets");
            if (numofsources == refprimary.getInt(0, "numsources") && numoftargets == refprimary.getInt(0, "numtargets")) {
                similarones = true;
            }
        }
        DataSet sourceTransferMap = this.getTransferMap(source);
        DataSet refTransferMap = this.getTransferMap(ref);
        DataSet sourcetypeDS = com.labvantage.sapphire.actions.array.ArrayUtil.getArrayTypeDetails(this.getQueryProcessor(), primary.getValue(0, "sourcearraytypeid"), primary.getValue(0, "sourcearraytypeversionid"));
        DataSet targettypeDS = com.labvantage.sapphire.actions.array.ArrayUtil.getArrayTypeDetails(this.getQueryProcessor(), primary.getValue(0, "targetarraytypeid"), primary.getValue(0, "targetarraytypeversionid"));
        DataSet refsourcetypeDS = com.labvantage.sapphire.actions.array.ArrayUtil.getArrayTypeDetails(this.getQueryProcessor(), refprimary.getValue(0, "sourcearraytypeid"), refprimary.getValue(0, "sourcearraytypeversionid"));
        DataSet reftargettypeDS = com.labvantage.sapphire.actions.array.ArrayUtil.getArrayTypeDetails(this.getQueryProcessor(), refprimary.getValue(0, "targetarraytypeid"), refprimary.getValue(0, "targetarraytypeversionid"));
        String horLblType = sourcetypeDS.getValue(0, "horizontallabeltype");
        String horLblStart = sourcetypeDS.getValue(0, "horizontallabelstart");
        String horLblDir = sourcetypeDS.getValue(0, "horizontallabeldirection");
        String verLblType = sourcetypeDS.getValue(0, "verticallabeltype");
        String verLblStart = sourcetypeDS.getValue(0, "verticallabelstart");
        String verLblDir = sourcetypeDS.getValue(0, "verticallabeldirection");
        List sourceColumnLabel = ArrayUtil.generateLabel(horLblType, horLblStart, horLblDir, sourcetypeDS.getInt(0, "numcolumns"));
        List sourceRowLabel = ArrayUtil.generateLabel(verLblType, verLblStart, verLblDir, sourcetypeDS.getInt(0, "numrows"));
        horLblType = targettypeDS.getValue(0, "horizontallabeltype");
        horLblStart = targettypeDS.getValue(0, "horizontallabelstart");
        horLblDir = targettypeDS.getValue(0, "horizontallabeldirection");
        verLblType = targettypeDS.getValue(0, "verticallabeltype");
        verLblStart = targettypeDS.getValue(0, "verticallabelstart");
        verLblDir = targettypeDS.getValue(0, "verticallabeldirection");
        List targetColLabel = ArrayUtil.generateLabel(horLblType, horLblStart, horLblDir, targettypeDS.getInt(0, "numcolumns"));
        List targetRowLabel = ArrayUtil.generateLabel(verLblType, verLblStart, verLblDir, targettypeDS.getInt(0, "numrows"));
        if (similarones) {
            DataSet currRefTransferMap;
            HashMap<String, BigDecimal> filter;
            String from;
            int i;
            int numofsources = primary.getInt(0, "numsourcearrays");
            int numoftargets = primary.getInt(0, "numtargetarrays");
            for (i = 0; i < numofsources; ++i) {
                from = "Source #" + (i + 1);
                content.startSubHeading(from, "");
                filter = new HashMap<String, BigDecimal>();
                filter.put("sourcearrayindex", new BigDecimal(i + 1));
                DataSet currSourceTransferMap = sourceTransferMap.getFilteredDataSet(filter);
                currRefTransferMap = refTransferMap.getFilteredDataSet(filter);
                this.renderSourceMappingGrid(content, sourcetypeDS, targettypeDS, currSourceTransferMap, currRefTransferMap, sourceRowLabel, sourceColumnLabel, targetRowLabel, targetColLabel);
            }
            for (i = 0; i < numoftargets; ++i) {
                from = "Target #" + (i + 1);
                content.startSubHeading(from, "");
                filter = new HashMap();
                filter.put("targetarrayindex", new BigDecimal(i + 1));
                DataSet currTargetTransferMap = sourceTransferMap.getFilteredDataSet(filter);
                currRefTransferMap = refTransferMap.getFilteredDataSet(filter);
                this.renderTargetMappingGrid(content, sourcetypeDS, targettypeDS, currTargetTransferMap, currRefTransferMap, sourceRowLabel, sourceColumnLabel, targetRowLabel, targetColLabel);
            }
        } else {
            content.startSubSection("Transfer Map", "");
            DataSet convertedSourceTransferMap = this.convertToLabels(sourceTransferMap, sourcetypeDS, targettypeDS, sourceRowLabel, sourceColumnLabel, targetRowLabel, targetColLabel);
            horLblType = refsourcetypeDS.getValue(0, "horizontallabeltype");
            horLblStart = refsourcetypeDS.getValue(0, "horizontallabelstart");
            horLblDir = refsourcetypeDS.getValue(0, "horizontallabeldirection");
            verLblType = refsourcetypeDS.getValue(0, "verticallabeltype");
            verLblStart = refsourcetypeDS.getValue(0, "verticallabelstart");
            verLblDir = refsourcetypeDS.getValue(0, "verticallabeldirection");
            List refsourceColumnLabel = ArrayUtil.generateLabel(horLblType, horLblStart, horLblDir, refsourcetypeDS.getInt(0, "numcolumns"));
            List refsourceRowLabel = ArrayUtil.generateLabel(verLblType, verLblStart, verLblDir, refsourcetypeDS.getInt(0, "numrows"));
            horLblType = reftargettypeDS.getValue(0, "horizontallabeltype");
            horLblStart = reftargettypeDS.getValue(0, "horizontallabelstart");
            horLblDir = reftargettypeDS.getValue(0, "horizontallabeldirection");
            verLblType = reftargettypeDS.getValue(0, "verticallabeltype");
            verLblStart = reftargettypeDS.getValue(0, "verticallabelstart");
            verLblDir = reftargettypeDS.getValue(0, "verticallabeldirection");
            List reftargetColLabel = ArrayUtil.generateLabel(horLblType, horLblStart, horLblDir, reftargettypeDS.getInt(0, "numcolumns"));
            List reftargetRowLabel = ArrayUtil.generateLabel(verLblType, verLblStart, verLblDir, reftargettypeDS.getInt(0, "numrows"));
            DataSet convertedRefTransferMap = this.convertToLabels(refTransferMap, refsourcetypeDS, reftargettypeDS, refsourceRowLabel, refsourceColumnLabel, reftargetRowLabel, reftargetColLabel);
            content.renderDiffListTable(convertedSourceTransferMap, convertedRefTransferMap, null);
        }
        content.startSubSection("Transfer Rule", "");
        DataSet sourceTransferRule = this.getTransferRule(source);
        DataSet refTransferRule = this.getTransferRule(ref);
        content.renderDiffListTable(sourceTransferRule, refTransferRule, null);
        return content.toString();
    }

    private DataSet getTransferRule(SDIData sdiData) {
        DataSet primary = sdiData.getDataset("primary");
        DataSet ret = new DataSet();
        if (primary != null) {
            String propertyliststr = primary.getValue(0, "contenttransferrule", "");
            PropertyList transferContentProps = new PropertyList();
            try {
                transferContentProps.setPropertyList(propertyliststr);
            }
            catch (SapphireException e) {
                Trace.logError("Failed to fetch content transfer rules");
            }
            ret.setColidCaseSensitive(true);
            ret.addColumn("Zone", 0);
            ret.addColumn("Copy Zone Definition", 0);
            ret.addColumn("Copy Content", 0);
            ret.addColumn("Propagate Unknowns", 0);
            ret.addColumn("Propagate Controls", 0);
            ret.addColumn("Propagate Transfer", 0);
            ret.addColumn("Propagate Treatment", 0);
            ret.addColumn("Propagate Operation", 0);
            ret.addColumn("Propagate MasterMix", 0);
            PropertyListCollection c = transferContentProps.getCollectionNotNull("contenttransferrule");
            for (int i = 0; i < c.size(); ++i) {
                PropertyList propertyList = c.getPropertyList(i);
                ret.addRow();
                ret.setString(i, "Zone", propertyList.getProperty("zone", ""));
                ret.setString(i, "Copy Zone Definition", propertyList.getProperty("definition", ""));
                ret.setString(i, "Copy Content", propertyList.getProperty("content", ""));
                ret.setString(i, "Propagate Unknowns", propertyList.getProperty("propagateunknown", ""));
                ret.setString(i, "Propagate Controls", propertyList.getProperty("propagateControl", ""));
                ret.setString(i, "Propagate Transfer", propertyList.getProperty("propagatetransfer", ""));
                ret.setString(i, "Propagate Treatment", propertyList.getProperty("propagatetreatment", ""));
                ret.setString(i, "Propagate Operation", propertyList.getProperty("propagateoperation", ""));
                ret.setString(i, "Propagate MasterMix", propertyList.getProperty("propagateMM", ""));
            }
        }
        return ret;
    }

    private DataSet convertToLabels(DataSet transferMap, DataSet sourceArrayTypeDetails, DataSet targetArrayTypeDetails, List sourceRowLabel, List sourceColumnLabel, List targetRowLabel, List targetColumnLabel) {
        DataSet ret = new DataSet();
        ret.setColidCaseSensitive(true);
        ret.addColumn("Source#", 0);
        ret.addColumn("Source Well", 0);
        ret.addColumn("Target#", 0);
        ret.addColumn("Target Well", 0);
        for (int i = 0; i < transferMap.getRowCount(); ++i) {
            int sourcenum = transferMap.getInt(i, "sourcearrayindex");
            int targetnum = transferMap.getInt(i, "targetarrayindex");
            int sourcerowindex = transferMap.getInt(i, "sourcerowindex");
            int sourcecolindex = transferMap.getInt(i, "sourcecolindex");
            String sourcelabel = this.determineLabel(sourceArrayTypeDetails, sourcerowindex, sourcecolindex, sourceRowLabel, sourceColumnLabel);
            int targetrowindex = transferMap.getInt(i, "targetrowindex");
            int targetcolindex = transferMap.getInt(i, "targetcolindex");
            String targetlabel = this.determineLabel(targetArrayTypeDetails, targetrowindex, targetcolindex, targetRowLabel, targetColumnLabel);
            ret.addRow();
            ret.setString(i, "Source#", "S" + sourcenum);
            ret.setString(i, "Target#", "T" + targetnum);
            ret.setString(i, "Source Well", sourcelabel);
            ret.setString(i, "Target Well", targetlabel);
        }
        return ret;
    }

    private void renderSourceMappingGrid(ConfigReportContent content, DataSet sourceArrayTypeDetails, DataSet targetArrayTypeDetails, DataSet currSourceTransferMap, DataSet currRefTransferMap, List srcrowLabels, List srccolumnLabels, List targetrowlabels, List targetcolumnlabels) {
        ConfigReportContent grid = new ConfigReportContent("grid", this.translationProcessor);
        grid.startTable();
        int numrows = -1;
        int numcols = -1;
        numrows = sourceArrayTypeDetails.getInt(0, "numrows");
        numcols = sourceArrayTypeDetails.getInt(0, "numcolumns");
        for (int xpos = 0; xpos < numrows; ++xpos) {
            if (xpos == 0) {
                grid.startRow();
                grid.append("<TD class=\"viewlistcol\" align=\"center\" width=10px>&nbsp;&nbsp;</TD>");
                for (int i = 0; i < numcols; ++i) {
                    String label = srccolumnLabels.get(i).toString();
                    grid.append("<TD class=\"viewlistcol\" align=\"center\" width=10px>" + label + "</TD>");
                }
                grid.endRow();
            }
            grid.startRow();
            String rowlabel = "";
            rowlabel = srcrowLabels.get(xpos).toString();
            grid.append("<TD class=\"viewlistcol\" align=\"center\" width=10px>" + rowlabel + "</TD>");
            for (int col = 0; col < numcols; ++col) {
                HashMap<String, BigDecimal> findmap = new HashMap<String, BigDecimal>();
                findmap.put("sourcerowindex", new BigDecimal(xpos));
                findmap.put("sourcecolindex", new BigDecimal(col));
                DataSet match = currSourceTransferMap.getFilteredDataSet(findmap);
                DataSet refmatch = currRefTransferMap.getFilteredDataSet(findmap);
                if (match != null && match.getRowCount() > 0) {
                    String targetlabel;
                    int targetcolindex;
                    int targetrowindex;
                    int targetarrayindex;
                    int i;
                    String mappedTo = "";
                    String refmappedTo = "";
                    for (i = 0; i < match.getRowCount(); ++i) {
                        targetarrayindex = match.getInt(i, "targetarrayindex");
                        targetrowindex = match.getInt(i, "targetrowindex");
                        targetcolindex = match.getInt(i, "targetcolindex");
                        if (mappedTo.length() != 0) {
                            mappedTo = mappedTo + "<P>";
                        }
                        targetlabel = this.determineLabel(targetArrayTypeDetails, targetrowindex, targetcolindex, targetrowlabels, targetcolumnlabels);
                        mappedTo = mappedTo + "S" + targetarrayindex + "(" + targetlabel + ")";
                    }
                    if (refmatch != null && refmatch.getRowCount() > 0) {
                        for (i = 0; i < refmatch.getRowCount(); ++i) {
                            targetarrayindex = refmatch.getInt(i, "targetarrayindex");
                            targetrowindex = refmatch.getInt(i, "targetrowindex");
                            targetcolindex = refmatch.getInt(i, "targetcolindex");
                            if (refmappedTo.length() != 0) {
                                refmappedTo = refmappedTo + "<P>";
                            }
                            targetlabel = this.determineLabel(targetArrayTypeDetails, targetrowindex, targetcolindex, targetrowlabels, targetcolumnlabels);
                            refmappedTo = refmappedTo + "S" + targetarrayindex + "(" + targetlabel + ")";
                        }
                        grid.append("<TD align=\"center\" width=10px style=\"font: Arial;font-size: 8pt;border: 1px solid black;padding: 5px;background-color:white\">" + ConfigReportContent.getDiffString(mappedTo, refmappedTo) + "</TD>");
                        continue;
                    }
                    mappedTo = "New: " + mappedTo;
                    grid.append("<TD align=\"center\" width=10px style=\"font: Arial;font-size: 8pt;border: 1px solid black;padding: 5px;background-color:white\">" + ConfigReportContent.getNewString(mappedTo) + "</TD>");
                    continue;
                }
                if (refmatch != null) {
                    String refmappedTo = "";
                    for (int i = 0; i < refmatch.getRowCount(); ++i) {
                        int targetarrayindex = refmatch.getInt(i, "targetarrayindex");
                        int targetrowindex = refmatch.getInt(i, "targetrowindex");
                        int targetcolindex = refmatch.getInt(i, "targetcolindex");
                        if (refmappedTo.length() != 0) {
                            refmappedTo = refmappedTo + "<P>";
                        }
                        String targetlabel = this.determineLabel(targetArrayTypeDetails, targetrowindex, targetcolindex, targetrowlabels, targetcolumnlabels);
                        refmappedTo = refmappedTo + "S" + targetarrayindex + "(" + targetlabel + ")";
                    }
                    grid.append("<TD align=\"center\" width=10px style=\"font: Arial;font-size: 8pt;border: 1px solid black;padding: 5px;background-color:white\">" + ConfigReportContent.getDeletedString(refmappedTo) + "</TD>");
                    continue;
                }
                grid.append("<TD class=\"viewlistcol\" align=\"center\" width=10pt>&nbsp;&nbsp;</TD>");
            }
            grid.endRow();
        }
        grid.endTable();
        content.append(grid.toString());
    }

    private void renderTargetMappingGrid(ConfigReportContent content, DataSet sourceArrayTypeDetails, DataSet targetArrayTypeDetails, DataSet currSourceTransferMap, DataSet currRefTransferMap, List srcrowLabels, List srccolumnLabels, List targetrowlabels, List targetcolumnlabels) {
        ConfigReportContent grid = new ConfigReportContent("grid", this.translationProcessor);
        grid.startTable();
        int numrows = -1;
        int numcols = -1;
        numrows = targetArrayTypeDetails.getInt(0, "numrows");
        numcols = targetArrayTypeDetails.getInt(0, "numcolumns");
        for (int xpos = 0; xpos < numrows; ++xpos) {
            if (xpos == 0) {
                grid.startRow();
                grid.append("<TD class=\"viewlistcol\" align=\"center\" width=10px>&nbsp;&nbsp;</TD>");
                for (int i = 0; i < numcols; ++i) {
                    String label = targetcolumnlabels.get(i).toString();
                    grid.append("<TD class=\"viewlistcol\" align=\"center\" width=10px>" + label + "</TD>");
                }
                grid.endRow();
            }
            grid.startRow();
            String rowlabel = "";
            rowlabel = targetrowlabels.get(xpos).toString();
            grid.append("<TD class=\"viewlistcol\" align=\"center\" width=10px>" + rowlabel + "</TD>");
            for (int col = 0; col < numcols; ++col) {
                HashMap<String, BigDecimal> findmap = new HashMap<String, BigDecimal>();
                findmap.put("targetrowindex", new BigDecimal(xpos));
                findmap.put("targetcolindex", new BigDecimal(col));
                DataSet match = currSourceTransferMap.getFilteredDataSet(findmap);
                DataSet refmatch = currRefTransferMap.getFilteredDataSet(findmap);
                if (match != null && match.getRowCount() > 0) {
                    String sourcelabel;
                    int sourcecolindex;
                    int sourcerowindex;
                    int sourcearrayindex;
                    int i;
                    String mappedTo = "";
                    String refmappedTo = "";
                    for (i = 0; i < match.getRowCount(); ++i) {
                        sourcearrayindex = match.getInt(i, "sourcearrayindex");
                        sourcerowindex = match.getInt(i, "sourcerowindex");
                        sourcecolindex = match.getInt(i, "sourcecolindex");
                        if (mappedTo.length() != 0) {
                            mappedTo = mappedTo + "<P>";
                        }
                        sourcelabel = this.determineLabel(sourceArrayTypeDetails, sourcerowindex, sourcecolindex, srcrowLabels, srccolumnLabels);
                        mappedTo = mappedTo + "S" + sourcearrayindex + "(" + sourcelabel + ")";
                    }
                    if (refmatch != null && refmatch.getRowCount() > 0) {
                        for (i = 0; i < refmatch.getRowCount(); ++i) {
                            sourcearrayindex = refmatch.getInt(i, "sourcearrayindex");
                            sourcerowindex = refmatch.getInt(i, "sourcerowindex");
                            sourcecolindex = refmatch.getInt(i, "sourcecolindex");
                            if (refmappedTo.length() != 0) {
                                refmappedTo = refmappedTo + "<P>";
                            }
                            sourcelabel = this.determineLabel(sourceArrayTypeDetails, sourcerowindex, sourcecolindex, srcrowLabels, srccolumnLabels);
                            refmappedTo = refmappedTo + "S" + sourcearrayindex + "(" + sourcelabel + ")";
                        }
                        grid.append("<TD align=\"center\" width=10px style=\"font: Arial;font-size: 8pt;border: 1px solid black;padding: 5px;background-color:white\">" + ConfigReportContent.getDiffString(mappedTo, refmappedTo) + "</TD>");
                        continue;
                    }
                    mappedTo = "New: " + mappedTo;
                    grid.append("<TD align=\"center\" width=10px style=\"font: Arial;font-size: 8pt;border: 1px solid black;padding: 5px;background-color:white\">" + ConfigReportContent.getNewString(mappedTo) + "</TD>");
                    continue;
                }
                if (refmatch != null) {
                    String refmappedTo = "";
                    for (int i = 0; i < refmatch.getRowCount(); ++i) {
                        int sourcearrayindex = refmatch.getInt(i, "sourcearrayindex");
                        int sourcerowindex = refmatch.getInt(i, "sourcerowindex");
                        int sourcecolindex = refmatch.getInt(i, "sourcecolindex");
                        if (refmappedTo.length() != 0) {
                            refmappedTo = refmappedTo + "<P>";
                        }
                        String sourcelabel = this.determineLabel(sourceArrayTypeDetails, sourcerowindex, sourcecolindex, srcrowLabels, srccolumnLabels);
                        refmappedTo = refmappedTo + "S" + sourcearrayindex + "(" + sourcelabel + ")";
                    }
                    grid.append("<TD align=\"center\" width=10px style=\"font: Arial;font-size: 8pt;border: 1px solid black;padding: 5px;background-color:white\">" + ConfigReportContent.getDeletedString(refmappedTo) + "</TD>");
                    continue;
                }
                grid.append("<TD class=\"viewlistcol\" align=\"center\" width=10pt>&nbsp;&nbsp;</TD>");
            }
            grid.endRow();
        }
        grid.endTable();
        content.append(grid.toString());
    }

    private DataSet getTransferMap(SDIData sdiData) {
        DataSet primary = sdiData.getDataset("primary");
        if (primary == null) {
            primary = new DataSet();
            primary.addRow();
        }
        String transferMap = primary.getValue(0, "transfermap", "");
        DataSet transferDS = new DataSet();
        if (transferMap.length() > 0) {
            String[] items = StringUtil.split(transferMap, "|");
            transferDS.addColumn("sourcearrayindex", 1);
            transferDS.addColumn("sourcerowindex", 1);
            transferDS.addColumn("sourcecolindex", 1);
            transferDS.addColumn("targetarrayindex", 1);
            transferDS.addColumn("targetrowindex", 1);
            transferDS.addColumn("targetcolindex", 1);
            for (int i = 0; i < items.length; ++i) {
                int row = transferDS.addRow();
                String[] tokens = StringUtil.split(items[i], "-");
                String[] targettokens = StringUtil.split(tokens[0], ",");
                String[] sourcetokens = StringUtil.split(tokens[1], ",");
                transferDS.setNumber(row, "sourcearrayindex", sourcetokens[0]);
                transferDS.setNumber(row, "sourcerowindex", sourcetokens[1]);
                transferDS.setNumber(row, "sourcecolindex", sourcetokens[2]);
                transferDS.setNumber(row, "targetarrayindex", targettokens[0]);
                transferDS.setNumber(row, "targetrowindex", targettokens[1]);
                transferDS.setNumber(row, "targetcolindex", targettokens[2]);
            }
        }
        return transferDS;
    }

    private String getSubOperation(SDIData sdiData) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (primary != null) {
            String algorithmrule = primary.getString(0, "algorithmrule", "");
            PropertyList algorule = new PropertyList();
            algorule.setPropertyList(algorithmrule);
            return algorule.getProperty("suboperation", "");
        }
        return "";
    }

    private String getTranspose(SDIData sdiData) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (primary != null) {
            String algorithmrule = primary.getString(0, "algorithmrule", "");
            PropertyList algorule = new PropertyList();
            algorule.setPropertyList(algorithmrule);
            return algorule.getProperty("transpose", "");
        }
        return "";
    }

    private String determineLabel(DataSet arrayTypeDetails, int rowindex, int colindex, List rowlabels, List columnlabels) {
        if (arrayTypeDetails.getString(0, "verticallabeltype").equals("Alphabet")) {
            return rowlabels.get(rowindex).toString() + columnlabels.get(colindex).toString();
        }
        return columnlabels.get(colindex).toString() + rowlabels.get(rowindex).toString();
    }
}

