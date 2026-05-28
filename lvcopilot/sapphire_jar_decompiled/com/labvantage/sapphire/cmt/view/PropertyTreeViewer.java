/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt.view;

import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.view.SDISnapshotViewer;
import com.labvantage.sapphire.modules.configreport.ro.PropertyTreeRO;
import com.labvantage.sapphire.modules.configreport.util.PropertyTreeUtil;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;

public class PropertyTreeViewer
extends SDISnapshotViewer {
    @Override
    protected ConfigReportContent renderPrimaryDiff(SDISnapshotItem source, SDISnapshotItem ref, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns, boolean hideInheritedProperties, boolean isChild) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent("PropertyTree", this.translationProcessor);
        configReportContent.setFoundDiff(false);
        PropertyTreeRO sdcRO = new PropertyTreeRO();
        sdcRO.initialize("PropertyTree", this.sapphireConnection);
        sdcRO.setCurrentSDIData(source.getSDIData());
        PropertyTreeRO refSdcRO = new PropertyTreeRO();
        refSdcRO.initialize("PropertyTree", this.sapphireConnection);
        if (ref != null) {
            refSdcRO.setCurrentSDIData(ref.getSDIData());
        }
        PropertyTreeUtil util = new PropertyTreeUtil();
        util.initialize(this.sapphireConnection, sdcRO, refSdcRO);
        boolean diffOnly = false;
        configReportContent.startSDISectionDiff(sdcRO.currentSDI, sdcRO.getDescription(), refSdcRO.getDescription());
        configReportContent.startSubSection("PropertyTree Info", "");
        configReportContent.appendSubSection(util.renderPropertyTreeInfoDiff(sdcRO, refSdcRO), "PropertyTree Info", diffOnly);
        configReportContent.startSubSection("Node Hierarchy", "");
        configReportContent.appendSubSection(util.renderNodeHierarchyDiff(sdcRO, refSdcRO, hideInheritedProperties, false), "Node Hierarchy", diffOnly);
        configReportContent.endSection();
        return configReportContent;
    }
}

