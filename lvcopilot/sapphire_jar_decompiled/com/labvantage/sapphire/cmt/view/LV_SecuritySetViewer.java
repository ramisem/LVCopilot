/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt.view;

import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.view.SDISnapshotViewer;
import com.labvantage.sapphire.modules.configreport.ro.LV_SecuritySetRO;
import com.labvantage.sapphire.modules.configreport.util.LV_SecuritySetUtil;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;

public class LV_SecuritySetViewer
extends SDISnapshotViewer {
    @Override
    protected ConfigReportContent renderPrimaryDiff(SDISnapshotItem source, SDISnapshotItem ref, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns, boolean hideInheritedProperties, boolean isChild) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent("SecuritySet: ", this.translationProcessor);
        configReportContent.setFoundDiff(false);
        LV_SecuritySetRO sdcRO = new LV_SecuritySetRO();
        sdcRO.initialize("LV_SecuritySet", this.sapphireConnection);
        sdcRO.setCurrentSDIData(source.getSDIData());
        LV_SecuritySetRO refSdcRO = new LV_SecuritySetRO();
        refSdcRO.initialize("LV_SecuritySet", this.sapphireConnection);
        if (ref != null) {
            refSdcRO.setCurrentSDIData(ref.getSDIData());
        }
        LV_SecuritySetUtil util = new LV_SecuritySetUtil();
        util.initialize(this.sapphireConnection, sdcRO, refSdcRO);
        String sdiTitle = this.getFormattedItemLabel(source.getSDIData(), this.getSDITableLabelInfo(source.getSDIData().getSdcid())[1]);
        configReportContent.startSection(sdiTitle);
        boolean diffOnly = false;
        configReportContent.startSubSection("SecuritySet Summary", "The following is the summary of the SecuritySet definition:");
        configReportContent.appendSubSection(util.renderSecuritySetInfoDiff(sdcRO, refSdcRO), "SecuritySet Summary", diffOnly);
        configReportContent.startSubHeading("User SDC Operations", "");
        configReportContent.appendSubSection(util.renderSecuritySetMatrixDiff(sdcRO, refSdcRO, false), "User SDC Operations", diffOnly);
        configReportContent.endSubSection("", "Summary");
        return configReportContent;
    }

    @Override
    protected void renderItemDetailsDiff(ConfigReportContent configReportContent, SDISnapshotItem sourceItem, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns) throws SapphireException {
    }

    @Override
    public String[] getIgnoreDataSets() {
        return new String[]{"securitysetsdc", "securitysetitem"};
    }
}

