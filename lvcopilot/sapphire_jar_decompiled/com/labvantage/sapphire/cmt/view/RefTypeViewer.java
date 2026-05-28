/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt.view;

import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.view.SDISnapshotViewer;
import com.labvantage.sapphire.modules.configreport.ro.RefTypeRO;
import com.labvantage.sapphire.modules.configreport.util.RefTypeUtil;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.util.SDIData;

public class RefTypeViewer
extends SDISnapshotViewer {
    @Override
    public ConfigReportContent renderPrimaryDiff(SDISnapshotItem source, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns, boolean hideInheritedProperties, boolean isChild) throws SapphireException {
        ConfigReportContent str = new ConfigReportContent("RefType: " + this.getPrimaryValue(source.getSDIData(), "reftypeid"), this.translationProcessor);
        SDIData srcSdiData = source.getSDIData();
        RefTypeRO srcRO = new RefTypeRO();
        RefTypeRO refRO = new RefTypeRO();
        srcRO.initialize(this.sapphireConnection);
        refRO.initialize(this.sapphireConnection);
        srcRO.setCurrentSDIData(srcSdiData);
        if (refItem != null) {
            refRO.setCurrentSDIData(refItem.getSDIData());
        }
        RefTypeUtil util = new RefTypeUtil();
        util.initialize(this.sapphireConnection, srcRO, refRO);
        String sdiTitle = this.getFormattedItemLabel(srcSdiData, this.getSDITableLabelInfo(srcSdiData.getSdcid())[1]);
        str.startSection(sdiTitle);
        str.appendSubSection(util.renderRefTypeInfoDiff(srcSdiData, refItem == null ? new SDIData() : refItem.getSDIData()), "RefType", false);
        return str;
    }

    @Override
    public void renderItemDetailsDiff(ConfigReportContent configReportContent, SDISnapshotItem sourceItem, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns) throws SapphireException {
        SDIData srcSdiData = sourceItem.getSDIData();
        RefTypeRO srcRO = new RefTypeRO();
        RefTypeRO refRO = new RefTypeRO();
        srcRO.initialize(this.sapphireConnection);
        refRO.initialize(this.sapphireConnection);
        srcRO.setCurrentSDIData(srcSdiData);
        if (refItem != null) {
            refRO.setCurrentSDIData(refItem.getSDIData());
        }
        RefTypeUtil util = new RefTypeUtil();
        util.initialize(this.sapphireConnection, srcRO, refRO);
        ConfigReportContent values = util.renderRefTypeValuesDiff(srcSdiData, refItem == null ? new SDIData() : refItem.getSDIData(), hideEmptyColumns);
        if (values.length() > 0) {
            configReportContent.startSubSection("Reference Values", "");
            configReportContent.appendNodeContent(values, "refvalue", "Reference Values");
        }
        ConfigReportContent str = new ConfigReportContent("categories", this.translationProcessor);
        this.renderCategores(str, sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        configReportContent.appendNodeContent(str, "categoryitem", "Categories");
        str = new ConfigReportContent("other", this.translationProcessor);
        this.renderOtherCommonDetails(str, sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        configReportContent.appendSpecialContent(str);
    }
}

