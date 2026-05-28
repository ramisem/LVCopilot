/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt.view;

import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.view.SDISnapshotViewer;
import com.labvantage.sapphire.modules.configreport.ro.LV_TaskDefRO;
import com.labvantage.sapphire.modules.configreport.util.LV_TaskDefUtil;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.util.SDIData;

public class LV_TaskDefViewer
extends SDISnapshotViewer {
    public LV_TaskDefViewer() {
    }

    public LV_TaskDefViewer(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
    }

    @Override
    public ConfigReportContent renderPrimaryDiff(SDISnapshotItem sourceItem, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns, boolean hideInheritedProperties, boolean isChild) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent("LV_TaskDef", this.translationProcessor);
        configReportContent.setFoundDiff(false);
        SDIData srcSdiData = sourceItem.getSDIData();
        SDIData refSdiData = refItem == null ? new SDIData() : refItem.getSDIData();
        LV_TaskDefRO srcRO = new LV_TaskDefRO();
        LV_TaskDefRO refRO = new LV_TaskDefRO();
        srcRO.initialize(this.sapphireConnection);
        refRO.initialize(this.sapphireConnection);
        String applicationurl = Configuration.getInstance().getServerHttpURL();
        srcRO.setCurrentSDIData(sourceItem.getSDIData());
        if (refItem != null) {
            refRO.setCurrentSDIData(refSdiData);
        }
        LV_TaskDefUtil util = new LV_TaskDefUtil();
        util.initialize(this.sapphireConnection, srcRO, refRO);
        String sdiTitle = this.getFormattedItemLabel(srcSdiData, this.getSDITableLabelInfo(srcSdiData.getSdcid())[1]);
        configReportContent.startSection(sdiTitle);
        boolean diffOnly = false;
        configReportContent.startSubSection("Task", "");
        configReportContent.appendSubSection(util.renderTaskDefInfoDiff(srcRO, refRO), this.translationProcessor.translate("Task"), diffOnly);
        configReportContent.startSubSection("Appearance", "");
        String srcImageBase64 = this.getPrimaryValue(srcSdiData, "thumbnailimageappearance");
        String refImageBase64 = this.getPrimaryValue(refSdiData, "thumbnailimageappearance");
        if (srcImageBase64.length() == refImageBase64.length() && srcImageBase64.equals(refImageBase64)) {
            configReportContent.append("<img src=\"data:image/gif;base64," + srcImageBase64 + "\" />  ");
        } else {
            configReportContent.startNewSubSection("New Task Appearance:", "");
            String sourcethumbnailhtml = srcImageBase64.length() == 0 ? "<P>Appearance image is empty." : "<img src=\"data:image/gif;base64," + srcImageBase64 + "\" />  ";
            String refthumbnailhtml = refImageBase64.length() == 0 ? "<P>Appearance image is empty." : "<img src=\"data:image/gif;base64," + refImageBase64 + "\" />  ";
            configReportContent.append("<table style=\"border:3px; border-style:solid; border-color:green; padding: 1em;\"><tr><td>" + sourcethumbnailhtml + "</td></tr></table>");
            if (refImageBase64.length() > 0) {
                configReportContent.startDeletedSubSection("Old Task Appearance:", "");
                configReportContent.append("<table style=\"border:3px; border-style:solid; border-color:red; padding: 1em;\"><tr><td>" + refthumbnailhtml + "</td></tr></table>");
            }
        }
        configReportContent.appendSubSection(util.renderAppearanceInfoDiff(srcRO, refRO), "Appearance", diffOnly);
        configReportContent.startSubSection("Queues", "");
        configReportContent.appendSubSection(util.renderQueuesInfoDiff(srcRO, refRO), "Queues", diffOnly);
        configReportContent.startSubSection("Task Design", "");
        configReportContent.appendSubSection(util.renderStepsDiff(applicationurl, srcRO, refRO, false, hideEmptyColumns), "Task Design", diffOnly);
        configReportContent.startSubSection("Task Variables", "");
        configReportContent.appendSubSection(util.renderVariablesDiff(srcRO, refRO), "Task Variables", diffOnly);
        configReportContent.startSubSection("Stages", "");
        configReportContent.appendSubSection(util.renderStagesDiff(srcRO, refRO), "Stages", diffOnly);
        configReportContent.startSubSection("Assignment", "");
        configReportContent.appendSubSection(util.renderTaskAssignmentDiff(srcRO, refRO), "Assignment", diffOnly);
        configReportContent.endSubSection("", "Summary");
        ConfigReportContent str = this.renderCertifications(sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        configReportContent.appendSpecialContent(str);
        str = new ConfigReportContent("categories", this.translationProcessor);
        this.renderCategores(str, sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        configReportContent.appendSpecialContent(str);
        str = new ConfigReportContent("others", this.translationProcessor);
        this.renderOtherCommonDetails(str, sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        configReportContent.appendSpecialContent(str);
        return configReportContent;
    }

    @Override
    protected void renderItemDetailsDiff(ConfigReportContent configReportContent, SDISnapshotItem sourceItem, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns) throws SapphireException {
    }

    @Override
    public String[] getIgnoreDataSets() {
        return new String[]{"taskdefio", "taskdefstep"};
    }
}

