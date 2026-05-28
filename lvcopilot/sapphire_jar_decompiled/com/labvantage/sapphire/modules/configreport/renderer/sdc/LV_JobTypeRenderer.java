/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer.sdc;

import com.labvantage.sapphire.modules.configreport.util.LV_JobTypeUtil;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.BaseSDCRO;
import sapphire.ext.ConfigReportContent;

public class LV_JobTypeRenderer
extends LV_JobTypeUtil {
    @Override
    public ConfigReportContent getSpecialContent(BaseSDCRO sdcRO, TranslationProcessor translationProcessor) throws SapphireException {
        ConfigReportContent str = new ConfigReportContent(this.config, "Job type: ");
        str.startSubSection("Summary", "The following is the summary of the LV_JobType definition:");
        str.appendSubSection(this.renderLV_JobTypeInfo(sdcRO), sdcRO.currentSDI.toString() + " JobType Summary", this.diffOnly);
        str.startSubHeading("SDC Access Matrix", "");
        str.appendSubSection(this.renderLV_JobTypeSDCAccessMatrix(sdcRO), sdcRO.currentSDI.toString() + " JobType SDC Access", this.diffOnly);
        str.appendSubSection(this.renderLV_JobTypeDeptAccessMatrix(sdcRO), sdcRO.currentSDI.toString() + " JobType Dept Access", this.diffOnly);
        str.endSubSection("", "Summary");
        return str;
    }

    @Override
    public ConfigReportContent getSpecialContent(BaseSDCRO srcRO, BaseSDCRO refRO, TranslationProcessor translationProcessor) throws SapphireException {
        ConfigReportContent str = new ConfigReportContent(this.config, "Job type:");
        str.startSubSection("Summary", "The following is the summary of the LV_JobType definition:");
        str.appendSubSection(this.renderLV_JobTypeInfoDiff(), srcRO.currentSDI.toString() + " JobType Summary", this.diffOnly);
        str.startSubHeading("SDC Access Matrix", "");
        str.appendSubSection(this.renderLV_JobTypeSDCAccessMatrixDiff(), srcRO.currentSDI.toString() + " JobType SDC Access", this.diffOnly);
        str.appendSubSection(this.renderLV_JobTypeDeptAccessMatrixDiff(), srcRO.currentSDI.toString() + " JobType Dept Access", this.diffOnly);
        str.endSubSection("", "Summary");
        return str;
    }
}

