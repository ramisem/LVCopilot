/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt.view;

import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.view.SDISnapshotViewer;
import com.labvantage.sapphire.modules.configreport.ro.TaskRO;
import com.labvantage.sapphire.modules.configreport.util.TaskUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.util.SDIData;

public class TaskViewer
extends SDISnapshotViewer {
    public TaskViewer() {
    }

    public TaskViewer(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
    }

    @Override
    protected ConfigReportContent renderPrimaryDiff(SDISnapshotItem source, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns, boolean hideInheritedProperties, boolean isChild) throws SapphireException {
        SDIData srcSdiData = source.getSDIData();
        TaskRO srcRO = new TaskRO();
        TaskRO refRO = new TaskRO();
        srcRO.initialize(this.sapphireConnection);
        refRO.initialize(this.sapphireConnection);
        srcRO.setCurrentSDIData(srcSdiData);
        if (refItem != null) {
            refRO.setCurrentSDIData(refItem.getSDIData());
        }
        TaskUtil util = new TaskUtil();
        util.initialize(this.sapphireConnection, srcRO, refRO);
        ConfigReportContent str = new ConfigReportContent("Task:" + this.getPrimaryValue(srcSdiData, "taskid"), this.translationProcessor);
        String sdiTitle = this.getFormattedItemLabel(srcSdiData, this.getSDITableLabelInfo(srcSdiData.getSdcid())[1]);
        str.startSection(sdiTitle);
        boolean diffOnly = false;
        str.appendSubSection(util.renderTaskInfoDiff(srcSdiData, refItem == null ? new SDIData() : refItem.getSDIData()), "Task", diffOnly);
        str.startSubSection("Task Schedule", "");
        str.appendSubSection(util.renderScheduleInfoDiff(srcSdiData, refItem == null ? new SDIData() : refItem.getSDIData()), "Task Schedule", diffOnly);
        str.endSubSection("", "Summary");
        return str;
    }

    @Override
    protected void renderItemDetailsDiff(ConfigReportContent configReportContent, SDISnapshotItem sourceItem, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns) throws SapphireException {
        SDIData srcSdiData = sourceItem.getSDIData();
        TaskRO srcRO = new TaskRO();
        TaskRO refRO = new TaskRO();
        srcRO.initialize(this.sapphireConnection);
        refRO.initialize(this.sapphireConnection);
        srcRO.setCurrentSDIData(srcSdiData);
        if (refItem != null) {
            refRO.setCurrentSDIData(refItem.getSDIData());
        }
        TaskUtil util = new TaskUtil();
        util.initialize(this.sapphireConnection, srcRO, refRO);
        ConfigReportContent props = util.renderTaskPropertiesDiff(srcSdiData, refItem == null ? new SDIData() : refItem.getSDIData(), hideEmptyColumns);
        if (props.length() > 0) {
            configReportContent.startSubSection("Task Properties", "");
            configReportContent.appendNodeContent(props, "taskproperty", "Task Properties");
        }
        ConfigReportContent str = new ConfigReportContent("categories", this.translationProcessor);
        this.renderCategores(str, sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        configReportContent.appendNodeContent(str, "categoryitem", "Categories");
        str = new ConfigReportContent("other", this.translationProcessor);
        this.renderOtherCommonDetails(str, sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        configReportContent.appendSpecialContent(str);
    }
}

