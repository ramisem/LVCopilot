/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt.view;

import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.view.SDISnapshotViewer;
import com.labvantage.sapphire.modules.configreport.ro.LV_DataFileDefRO;
import com.labvantage.sapphire.modules.configreport.util.LV_DataFileDefUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.util.SDIData;

public class LV_DataFileDefViewer
extends SDISnapshotViewer {
    public static final String FIELDS_COLLECTION = "fields";

    public LV_DataFileDefViewer() {
    }

    public LV_DataFileDefViewer(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
    }

    @Override
    protected ConfigReportContent renderPrimaryDiff(SDISnapshotItem source, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns, boolean hideInheritedProperties, boolean isChild) throws SapphireException {
        SDIData srcSdiData = source.getSDIData();
        LV_DataFileDefRO dataFileDefRO = new LV_DataFileDefRO();
        dataFileDefRO.initialize(this.sapphireConnection);
        dataFileDefRO.setCurrentSDIData(srcSdiData);
        LV_DataFileDefRO refDataFileDefRO = new LV_DataFileDefRO();
        refDataFileDefRO.initialize(this.sapphireConnection);
        if (refItem != null) {
            refDataFileDefRO.setCurrentSDIData(refItem.getSDIData());
        }
        ConfigReportContent configReportContent = new ConfigReportContent("render summary ", this.translationProcessor);
        String sdiTitle = this.getFormattedItemLabel(srcSdiData, this.getSDITableLabelInfo(srcSdiData.getSdcid())[1]);
        configReportContent.startSection(sdiTitle);
        LV_DataFileDefUtil util = new LV_DataFileDefUtil();
        util.initialize(this.sapphireConnection, dataFileDefRO, refDataFileDefRO);
        configReportContent.appendSubSection(util.renderDFDInfoDiff(dataFileDefRO, refDataFileDefRO, this.translationProcessor), "DataFileDef");
        if (!dataFileDefRO.getStyle().equalsIgnoreCase("Composite")) {
            configReportContent.startSubSection("Example File", "Content of the example Excel/txt file");
            configReportContent.appendSubSection(util.renderExampleFileDiff(dataFileDefRO, refDataFileDefRO, this.translationProcessor), "Example File");
            configReportContent.startSubSection("Field Definitions", "");
            configReportContent.appendSubSection(util.renderFieldDefinitionsDiff(dataFileDefRO, refDataFileDefRO), "Field Definitions");
            configReportContent.startSubSection("Processing Script", "");
            configReportContent.appendSubSection(util.renderProcessingScriptDiff(dataFileDefRO, refDataFileDefRO), "Processing Rules");
        }
        return configReportContent;
    }
}

