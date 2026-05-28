/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer.sdc;

import com.labvantage.sapphire.modules.configreport.ro.PropertyTreeRO;
import com.labvantage.sapphire.modules.configreport.util.PropertyTreeUtil;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.BaseSDCRO;
import sapphire.ext.ConfigReportContent;

public class PropertyTreeRenderer
extends PropertyTreeUtil {
    @Override
    public ConfigReportContent getSectionContent(BaseSDCRO sdcRO, BaseSDCRO refSdcRO, TranslationProcessor translationProcessor) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "PropertyTree");
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
                    configReportContent.startSubSection("PropertyTree Info", "");
                    configReportContent.appendSubSection(this.renderPropertyTreeInfo((PropertyTreeRO)refSdcRO), "PropertyTree Info", this.diffOnly);
                    configReportContent.startSubSection("Node Hierarchy", "");
                    configReportContent.appendSubSection(this.renderNodeHierarchy((PropertyTreeRO)refSdcRO, "D"), "Node Hierarchy", this.diffOnly);
                } else if (this.refRO == null || refSdcRO.currentSDI == null) {
                    configReportContent.startSubSection("PropertyTree Info", "");
                    configReportContent.appendSubSection(this.renderPropertyTreeInfo((PropertyTreeRO)sdcRO), "PropertyTree Info", this.diffOnly);
                    configReportContent.startSubSection("Node Hierarchy", "");
                    configReportContent.appendSubSection(this.renderNodeHierarchy((PropertyTreeRO)sdcRO, "N"), "Node Hierarchy", this.diffOnly);
                } else {
                    configReportContent.startSubSection("PropertyTree Info", "");
                    configReportContent.appendSubSection(this.renderPropertyTreeInfoDiff((PropertyTreeRO)sdcRO, (PropertyTreeRO)refSdcRO), "PropertyTree Info", this.diffOnly);
                    configReportContent.startSubSection("Node Hierarchy", "");
                    ConfigReportContent nodehierarchydiff = this.renderNodeHierarchyDiff((PropertyTreeRO)sdcRO, (PropertyTreeRO)refSdcRO, false, true);
                    configReportContent.appendSubSection(nodehierarchydiff, "Node Hierarchy", this.diffOnly);
                }
            } else {
                configReportContent.startSubSection("PropertyTree Info", "");
                configReportContent.appendSubSection(this.renderPropertyTreeInfo((PropertyTreeRO)sdcRO), "PropertyTree Info", this.diffOnly);
                configReportContent.startSubSection("Node Hierarchy", "");
                ConfigReportContent nodehierarchy = this.renderNodeHierarchy((PropertyTreeRO)sdcRO, "S");
                configReportContent.appendSubSection(nodehierarchy, "Node Hierarchy", this.diffOnly);
            }
            configReportContent.endSubSection("", "Summary");
        }
        if (this.reportDetails) {
            this.renderDetails(configReportContent, true, translationProcessor);
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
        this.nodeHtmlMap = new HashMap();
        configReportContent.endSection();
        this.updateSectionChangeInfo("PropertyTree", ConfigReportContent.generateSDISectionTitle(currRO.currentSDI), configReportContent);
        return configReportContent;
    }
}

