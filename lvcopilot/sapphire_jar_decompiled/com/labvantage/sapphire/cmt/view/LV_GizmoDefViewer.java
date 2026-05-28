/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt.view;

import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.view.SDISnapshotViewer;
import com.labvantage.sapphire.modules.configreport.ro.LV_GizmoDefRO;
import com.labvantage.sapphire.modules.configreport.util.LV_GizmoDefUtil;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.ext.ConfigReportContent;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LV_GizmoDefViewer
extends SDISnapshotViewer {
    @Override
    public ConfigReportContent renderPrimaryDiff(SDISnapshotItem source, SDISnapshotItem ref, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns, boolean hideInheritedProperties, boolean isChild) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent("GizmoDef", this.translationProcessor);
        configReportContent.setFoundDiff(false);
        LV_GizmoDefRO sdcRO = new LV_GizmoDefRO();
        sdcRO.initialize("LV_GizmoDef", this.sapphireConnection);
        sdcRO.setCurrentSDIData(source.getSDIData());
        LV_GizmoDefRO refSdcRO = new LV_GizmoDefRO();
        refSdcRO.initialize("LV_GizmoDef", this.sapphireConnection);
        if (ref != null) {
            refSdcRO.setCurrentSDIData(ref.getSDIData());
        }
        LV_GizmoDefUtil util = new LV_GizmoDefUtil();
        SDIData srcSdiData = source.getSDIData();
        SDIData refSdiData = null;
        refSdiData = ref != null ? ref.getSDIData() : new SDIData();
        util.initialize(this.sapphireConnection, sdcRO, refSdcRO);
        String sdiTitle = this.getFormattedItemLabel(srcSdiData, this.getSDITableLabelInfo(srcSdiData.getSdcid())[1]);
        configReportContent.startSection(sdiTitle);
        boolean diffOnly = false;
        configReportContent.appendSubSection(util.renderGizmoDiff(sdcRO, refSdcRO, hideEmptyColumns), "Gizmo Info", diffOnly);
        configReportContent.startSubSection("Gizmo Properties", "");
        configReportContent.appendSubSection(this.renderGizmoPropertiesDiffForSnapshot("Gizmo Properties", srcSdiData, refSdiData, hideInheritedProperties, hideEmptyColumns), "Gizmo Properties", diffOnly);
        configReportContent.endSection();
        return configReportContent;
    }

    public ConfigReportContent renderGizmoPropertiesDiffForSnapshot(String title, SDIData srcSDIData, SDIData refSDIData, boolean hideInheritedProperties, boolean hideEmptyColumns) throws SapphireException {
        String gizmodefid = LV_GizmoDefViewer.getSDI(srcSDIData).getKeyid1();
        ConfigReportContent configReportContent = new ConfigReportContent("Gizmo info:", this.translationProcessor);
        HashMap<String, PropertyList> srcProperties = LV_GizmoDefViewer.getOverridingPropertyTrees(srcSDIData.getDataset("primary"), 0);
        HashMap<String, PropertyList> refProperties = LV_GizmoDefViewer.getOverridingPropertyTrees(refSDIData.getDataset("primary"), 0);
        String propertytreeid = this.getPrimaryValue(srcSDIData, "propertytreeid");
        String srcextendnodeid = this.getPrimaryValue(srcSDIData, "extendnodeid");
        String refextendnodeid = this.getPrimaryValue(refSDIData, "extendnodeid");
        try {
            String configCompCode;
            ConfigurationProcessor config = new ConfigurationProcessor(this.sapphireConnection.getConnectionId());
            boolean isDevMode = "Y".equals(config.getSysConfigProperty("devmode"));
            boolean isCompMode = false;
            String string = configCompCode = isDevMode ? "" : config.getSysConfigProperty("compcode");
            if (configCompCode.length() > 0) {
                String compcode = new QueryProcessor(this.getConnectionId()).getPreparedSqlDataSet("SELECT compcode FROM gizmodef WHERE gizmodefid=?", (Object[])new String[]{gizmodefid}).getValue(0, "compcode");
                isCompMode = compcode.equals(configCompCode);
            }
            configReportContent = SDISnapshotViewer.renderOverriddenPropertyList(isDevMode, isCompMode, this.sapphireConnection.getConnectionId(), title, propertytreeid, srcextendnodeid, refextendnodeid, srcProperties, refProperties, hideInheritedProperties, this.getTranslationProcessor(), hideEmptyColumns);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to fetch propertyTree");
        }
        if (configReportContent.length() == 0) {
            configReportContent.append("No Properties");
        }
        return configReportContent;
    }
}

