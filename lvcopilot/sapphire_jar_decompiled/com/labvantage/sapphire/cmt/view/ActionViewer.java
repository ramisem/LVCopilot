/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt.view;

import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.view.SDISnapshotViewer;
import com.labvantage.sapphire.modules.configreport.ro.ActionRO;
import com.labvantage.sapphire.modules.configreport.util.ActionUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.util.SDIData;

public class ActionViewer
extends SDISnapshotViewer {
    public ActionViewer() {
    }

    public ActionViewer(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
    }

    @Override
    protected ConfigReportContent renderPrimaryDiff(SDISnapshotItem source, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns, boolean hideInheritedProperties, boolean isChild) throws SapphireException {
        SDIData srcSdiData = source.getSDIData();
        SDIData refSdiData = refItem == null ? new SDIData() : refItem.getSDIData();
        ActionRO srcRO = new ActionRO();
        ActionRO refRO = new ActionRO();
        srcRO.initialize(this.sapphireConnection);
        refRO.initialize(this.sapphireConnection);
        srcRO.setCurrentSDIData(source.getSDIData());
        if (refItem != null) {
            refRO.setCurrentSDIData(refSdiData);
        }
        ActionUtil actionUtil = new ActionUtil();
        actionUtil.initialize(this.sapphireConnection, srcRO, refRO);
        ConfigReportContent str = new ConfigReportContent("Action", this.translationProcessor);
        str.startSDISectionDiff(this.sdcProcessor, srcSdiData, srcRO.currentSDI, "", "");
        str.appendSubSection(actionUtil.renderActionInfoDiff(srcSdiData, refSdiData, this.translationProcessor, hideEmptyColumns), "Action");
        if (this.getPrimaryValue(srcSdiData, "actionlanguage").equals("actionblock")) {
            str.startSubHeading("Flow Chart", "The following is the Flow Chart of the Action:");
            str.appendSubSection(actionUtil.renderFlowChartDiff(srcSdiData, refSdiData, this.translationProcessor), "Flow Chart");
        }
        str.endSubSection("", "Action");
        return str;
    }

    @Override
    protected void renderItemDetailsDiff(ConfigReportContent configReportContent, SDISnapshotItem sourceItem, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns) throws SapphireException {
        ActionRO srcRO = new ActionRO();
        ActionRO refRO = new ActionRO();
        srcRO.initialize(this.sapphireConnection);
        refRO.initialize(this.sapphireConnection);
        srcRO.setCurrentSDIData(sourceItem.getSDIData());
        if (refItem != null && refItem.getSDIData() != null) {
            refRO.setCurrentSDIData(refItem.getSDIData());
        }
        ActionUtil actionUtil = new ActionUtil();
        actionUtil.initialize(this.sapphireConnection, srcRO, refRO);
        ConfigReportContent str = new ConfigReportContent("Action", this.translationProcessor);
        str.appendSpecialContent(actionUtil.renderActionPropertiesDiff(sourceItem.getSDIData(), refItem != null ? refItem.getSDIData() : new SDIData(), hideEmptyColumns));
        configReportContent.appendNodeContent(str, "actionproperty", "Action Properties");
        str = new ConfigReportContent("categories", this.translationProcessor);
        this.renderCategores(str, sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        configReportContent.appendSpecialContent(str);
        str = new ConfigReportContent("other", this.translationProcessor);
        this.renderOtherCommonDetails(str, sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        configReportContent.appendSpecialContent(str);
        str = new ConfigReportContent("roles", this.translationProcessor);
        this.renderRoles(str, sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        configReportContent.appendSpecialContent(str);
    }
}

