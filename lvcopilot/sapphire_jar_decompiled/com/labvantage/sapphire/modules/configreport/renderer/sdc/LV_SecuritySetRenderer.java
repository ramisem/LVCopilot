/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer.sdc;

import com.labvantage.sapphire.modules.configreport.ro.LV_SecuritySetRO;
import com.labvantage.sapphire.modules.configreport.util.LV_SecuritySetUtil;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.BaseSDCRO;
import sapphire.ext.ConfigReportContent;

public class LV_SecuritySetRenderer
extends LV_SecuritySetUtil {
    @Override
    public ConfigReportContent getSectionContent(BaseSDCRO sdcRO, BaseSDCRO refSdcRO, TranslationProcessor translationProcessor) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "SecuritySet: ");
        configReportContent.setFoundDiff(false);
        LV_SecuritySetRO currRO = (LV_SecuritySetRO)sdcRO;
        if (this.includeDiffReport) {
            if (sdcRO == null || sdcRO.currentSDI == null) {
                currRO = (LV_SecuritySetRO)refSdcRO;
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
                    configReportContent.startSubSection("SecuritySet Summary", "The following is the summary of the SecuritySet definition:");
                    configReportContent.appendSubSection(this.renderSecuritySetInfo(refSdcRO), "SecuritySet Summary", this.diffOnly);
                    configReportContent.startSubSection("User SDC Operations", "");
                    configReportContent.appendSubSection(this.renderSecuritySetMatrix(refSdcRO, true), "User SDC Operations", this.diffOnly);
                    configReportContent.endSubSection("", "Summary");
                } else if (this.refRO == null || refSdcRO.currentSDI == null) {
                    configReportContent.startSubSection("SecuritySet Summary", "The following is the summary of the SecuritySet definition:");
                    configReportContent.appendSubSection(this.renderSecuritySetInfo(sdcRO), "SecuritySet Summary", this.diffOnly);
                    configReportContent.startSubHeading("User SDC Operations", "");
                    configReportContent.appendSubSection(this.renderSecuritySetMatrix(sdcRO, true), "User SDC Operations", this.diffOnly);
                    configReportContent.endSubSection("", "Summary");
                } else {
                    configReportContent.startSubSection("SecuritySet Summary", "The following is the summary of the SecuritySet definition:");
                    configReportContent.appendSubSection(this.renderSecuritySetInfoDiff(sdcRO, refSdcRO), "SecuritySet Summary", this.diffOnly);
                    configReportContent.startSubHeading("User SDC Operations", "");
                    configReportContent.appendSubSection(this.renderSecuritySetMatrixDiff(sdcRO, refSdcRO, true), "User SDC Operations", this.diffOnly);
                    configReportContent.endSubSection("", "Summary");
                }
            } else {
                configReportContent.startSubSection("SecuritySet Summary", "The following is the summary of the SecuritySet definition:");
                configReportContent.appendSubSection(this.renderSecuritySetInfo(sdcRO), "SecuritySet Summary", this.diffOnly);
                configReportContent.startSubHeading("User SDC Operations", "");
                configReportContent.appendSubSection(this.renderSecuritySetMatrix(sdcRO, true), "User SDC Operations", this.diffOnly);
                configReportContent.endSubSection("", "Summary");
            }
        }
        if (this.reportDetails) {
            this.renderDetails(configReportContent, true, this.getTranslationProcessor());
            configReportContent.endSection();
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
        this.updateSectionChangeInfo(currRO.getSDCSingular(), ConfigReportContent.generateSDISectionTitle(currRO.currentSDI), configReportContent);
        return configReportContent;
    }
}

