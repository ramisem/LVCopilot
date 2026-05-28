/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer.sdc;

import com.labvantage.sapphire.modules.configreport.ro.LV_GizmoDefRO;
import com.labvantage.sapphire.modules.configreport.util.LV_GizmoDefUtil;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.BaseSDCRO;
import sapphire.ext.ConfigReportContent;

public class LV_GizmoDefRenderer
extends LV_GizmoDefUtil {
    private HashMap nodeHtmlMap = new HashMap();

    @Override
    public ConfigReportContent getSectionContent(BaseSDCRO sdcRO, BaseSDCRO refSdcRO, TranslationProcessor translationProcessor) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "GizmoDef");
        configReportContent.setFoundDiff(false);
        BaseSDCRO currRO = sdcRO;
        if (this.includeDiffReport) {
            if (sdcRO == null || sdcRO.currentSDI == null) {
                currRO = refSdcRO;
                configReportContent.startSDISection(refSdcRO.currentSDI, refSdcRO.getDescription());
            } else if (refSdcRO == null || refSdcRO.currentSDI == null) {
                configReportContent.startSDISection(sdcRO.currentSDI, sdcRO.getDescription());
            } else {
                configReportContent.startSDISectionDiff(sdcRO.currentSDI, sdcRO.getDescription(), refSdcRO.getDescription());
            }
        } else {
            configReportContent.startSDISection(sdcRO.currentSDI, sdcRO.getDescription());
        }
        if (this.reportSummary) {
            if (this.includeDiffReport) {
                if (sdcRO == null || sdcRO.currentSDI == null) {
                    configReportContent.startSubSection("Gizmo Definition", "");
                    configReportContent.appendSubSection(this.renderGizmoInfo((LV_GizmoDefRO)refSdcRO), "Gizmo Info", this.diffOnly);
                    configReportContent.startSubSection("Gizmo Properties", "");
                    configReportContent.appendSubSection(this.renderGizmoProperties((LV_GizmoDefRO)refSdcRO), "Gizmo Properties", this.diffOnly);
                } else if (this.refRO == null || refSdcRO.currentSDI == null) {
                    configReportContent.startSubSection("GizmoDef Info", "");
                    configReportContent.appendSubSection(this.renderGizmoInfo((LV_GizmoDefRO)sdcRO), "Gizmo Info", this.diffOnly);
                    configReportContent.startSubSection("Gizmo Properties", "");
                    configReportContent.appendSubSection(this.renderGizmoProperties((LV_GizmoDefRO)sdcRO), "Gizmo Properties", this.diffOnly);
                } else {
                    configReportContent.startSubSection("Gizmo Info", "");
                    configReportContent.appendSubSection(this.renderGizmoDiff((LV_GizmoDefRO)sdcRO, (LV_GizmoDefRO)refSdcRO, false), "Gizmo Info", this.diffOnly);
                    configReportContent.startSubSection("Gizmo Properties", "");
                    configReportContent.appendSubSection(this.renderGizmoPropertiesDiff((LV_GizmoDefRO)sdcRO, (LV_GizmoDefRO)refSdcRO), "Gizmo Properties", this.diffOnly);
                }
            } else {
                configReportContent.startSubSection("Gizmo Info", "");
                configReportContent.appendSubSection(this.renderGizmoInfo((LV_GizmoDefRO)sdcRO), "Gizmo Info", this.diffOnly);
                configReportContent.startSubSection("Gizmo Properties", "");
                configReportContent.appendSubSection(this.renderGizmoProperties((LV_GizmoDefRO)sdcRO), "Gizmo Properties", this.diffOnly);
            }
            configReportContent.endSubSection("", "Summary");
        }
        if (this.reportDetails) {
            this.renderDetails(configReportContent, true, this.getTranslationProcessor());
        }
        if (this.reportCategories) {
            configReportContent.startSubSection("Categories", "");
            ConfigReportContent categories = new ConfigReportContent(this.config, "categories");
            if (!this.includeDiffReport) {
                if (sdcRO != null) {
                    configReportContent.renderCategories(sdcRO.getCategories());
                }
            } else if (sdcRO == null || sdcRO.currentSDI == null) {
                if (refSdcRO != null) {
                    categories.renderCategories(refSdcRO.getCategories());
                }
                configReportContent.appendSubSection(categories, "Categories", this.diffOnly);
            } else if (refSdcRO == null || refSdcRO.currentSDI == null) {
                categories.renderCategories(sdcRO.getCategories());
                configReportContent.appendSubSection(categories, "Categories", this.diffOnly);
            } else {
                categories.renderCategoriesDiff(sdcRO.getCategories(), refSdcRO.getCategories());
                configReportContent.appendSubSection(categories, "Categories", this.diffOnly);
            }
        }
        configReportContent.endSection();
        this.updateSectionChangeInfo("LV_GizmoDef", ConfigReportContent.generateSDISectionTitle(currRO.currentSDI), configReportContent);
        return configReportContent;
    }
}

