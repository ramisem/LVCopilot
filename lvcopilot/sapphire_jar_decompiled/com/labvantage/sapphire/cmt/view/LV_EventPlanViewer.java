/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt.view;

import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.view.SDISnapshotViewer;
import com.labvantage.sapphire.modules.configreport.ro.LV_EventPlanRO;
import com.labvantage.sapphire.modules.configreport.util.LV_EventPlanUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;

public class LV_EventPlanViewer
extends SDISnapshotViewer {
    public LV_EventPlanViewer() {
    }

    public LV_EventPlanViewer(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
    }

    @Override
    public ConfigReportContent renderPrimaryDiff(SDISnapshotItem source, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns, boolean hideInheritedProperties, boolean isChild) throws SapphireException {
        LV_EventPlanRO sdcRO = new LV_EventPlanRO();
        sdcRO.initialize("LV_EventPlan", this.sapphireConnection);
        sdcRO.setCurrentSDIData(source.getSDIData());
        LV_EventPlanRO refSdcRO = new LV_EventPlanRO();
        refSdcRO.initialize("LV_EventPlan", this.sapphireConnection);
        if (refItem != null) {
            refSdcRO.setCurrentSDIData(refItem.getSDIData());
        }
        LV_EventPlanUtil util = new LV_EventPlanUtil();
        util.initialize(this.sapphireConnection, sdcRO, refSdcRO);
        ConfigReportContent configReportContent = new ConfigReportContent("Event Plan", this.translationProcessor);
        String sdiTitle = this.getFormattedItemLabel(source.getSDIData(), this.getSDITableLabelInfo(source.getSDIData().getSdcid())[1]);
        configReportContent.startSection(sdiTitle);
        configReportContent.appendSubSection(util.renderEventPlanTreeDiff(sdcRO, refSdcRO, false), "Event Plan", false);
        configReportContent.startSubSection("Event Plan Details", "");
        configReportContent.appendSubSection(util.renderEventPlanInfoDiff(sdcRO, refSdcRO), "Event Plan Details", false);
        configReportContent.startSubSection("Event Plan Properties", "");
        configReportContent.appendSubSection(util.renderEventPlanPropertiesDiff(sdcRO, refSdcRO), "Event Plan Properties", false);
        configReportContent.startSubSection("Event Details", "");
        configReportContent.appendSubSection(util.renderEventDetailsDiff(configReportContent.getApplicationRoot(), configReportContent.getFolder(), sdcRO, refSdcRO, false), "Event Details", false);
        ConfigReportContent cat = new ConfigReportContent("categories", this.translationProcessor);
        this.renderCategores(cat, source, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        configReportContent.appendNodeContent(cat, "categoryitem", "Categories");
        ConfigReportContent str = new ConfigReportContent("other", this.translationProcessor);
        this.renderOtherCommonDetails(str, source, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        configReportContent.appendSpecialContent(str);
        return configReportContent;
    }

    @Override
    protected void renderItemDetailsDiff(ConfigReportContent configReportContent, SDISnapshotItem sourceItem, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns) throws SapphireException {
    }

    @Override
    public String[] getIgnoreDataSets() {
        return new String[]{"eventplanitem", "eventplanitemcondition"};
    }
}

