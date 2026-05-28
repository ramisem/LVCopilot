/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt.view;

import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.view.SDISnapshotViewer;
import com.labvantage.sapphire.modules.configreport.ro.LV_JobTypeRO;
import com.labvantage.sapphire.modules.configreport.util.LV_JobTypeUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;

public class LV_JobTypeViewer
extends SDISnapshotViewer {
    public LV_JobTypeViewer() {
    }

    public LV_JobTypeViewer(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
    }

    @Override
    public ConfigReportContent renderPrimaryDiff(SDISnapshotItem source, SDISnapshotItem ref, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns, boolean hideInheritedProperties, boolean isChild) throws SapphireException {
        LV_JobTypeRO sdcRO = new LV_JobTypeRO();
        sdcRO.initialize("LV_JobType", this.sapphireConnection);
        sdcRO.setCurrentSDIData(source.getSDIData());
        LV_JobTypeRO refSdcRO = new LV_JobTypeRO();
        if (ref != null) {
            refSdcRO.initialize("LV_JobType", this.sapphireConnection);
            refSdcRO.setCurrentSDIData(ref.getSDIData());
        }
        LV_JobTypeUtil util = new LV_JobTypeUtil();
        util.initialize(this.sapphireConnection, sdcRO, refSdcRO);
        ConfigReportContent str = new ConfigReportContent("Job type:", this.translationProcessor);
        String sdiTitle = this.getFormattedItemLabel(source.getSDIData(), this.getSDITableLabelInfo(source.getSDIData().getSdcid())[1]);
        str.startSection(sdiTitle);
        str.appendSubSection(util.renderLV_JobTypeInfoDiff(), sdcRO.currentSDI.toString() + " JobType Summary", false);
        str.startSubHeading("SDC Access Matrix", "");
        str.appendSubSection(util.renderLV_JobTypeSDCAccessMatrixDiff(), sdcRO.currentSDI.toString() + " JobType SDC Access", false);
        str.appendSubSection(util.renderLV_JobTypeDeptAccessMatrixDiff(), sdcRO.currentSDI.toString() + " JobType Dept Access", false);
        str.endSubSection("", "Summary");
        return str;
    }

    @Override
    public void renderItemDetailsDiff(ConfigReportContent configReportContent, SDISnapshotItem sourceItem, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns) throws SapphireException {
        ConfigReportContent str = new ConfigReportContent("categories", this.translationProcessor);
        this.renderCategores(str, sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        configReportContent.appendNodeContent(str, "categoryitem", "Categories");
        str = new ConfigReportContent("other", this.translationProcessor);
        this.renderOtherCommonDetails(str, sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        configReportContent.appendSpecialContent(str);
    }

    @Override
    public String[] getIgnoreDataSets() {
        return new String[]{"sdcjobtypesecurity", "jobtyperole", "jobtypedepartment"};
    }
}

