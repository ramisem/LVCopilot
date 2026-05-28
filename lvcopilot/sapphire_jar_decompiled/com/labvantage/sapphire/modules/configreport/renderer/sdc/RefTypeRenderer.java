/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer.sdc;

import com.labvantage.sapphire.modules.configreport.util.RefTypeUtil;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.ConfigReportContent;
import sapphire.util.SDIData;

public class RefTypeRenderer
extends RefTypeUtil {
    @Override
    public ConfigReportContent getSpecialContent(SDIData sdiData, TranslationProcessor translationProcessor) throws SapphireException {
        ConfigReportContent str = new ConfigReportContent(this.config, "RefType: " + RefTypeRenderer.getPrimaryValue(sdiData, "reftypeid"));
        str.startSubSection("RefType", "The following is the summary of the RefType definition:");
        str.appendSubSection(this.renderRefTypeInfo(sdiData), "RefType", this.diffOnly);
        str.startSubSection("RefType Values", "");
        str.appendSubSection(this.renderRefTypeValues(sdiData), "RefType Values", this.diffOnly);
        str.endSubSection("", "Summary");
        return str;
    }

    @Override
    public ConfigReportContent getSpecialContent(SDIData srcSDIData, SDIData refSDIData, TranslationProcessor translationProcessor) throws SapphireException {
        ConfigReportContent str = new ConfigReportContent(this.config, "RefType: " + RefTypeRenderer.getPrimaryValue(srcSDIData, "reftypeid"));
        str.startSubSection("RefType", "The following is the summary of the RefType definition:");
        str.appendSubSection(this.renderRefTypeInfoDiff(srcSDIData, refSDIData), "RefType", this.diffOnly);
        str.startSubSection("RefType Values", "");
        str.appendSubSection(this.renderRefTypeValuesDiff(srcSDIData, refSDIData, true), "RefType Values", this.diffOnly);
        str.endSubSection("", "Summary");
        return str;
    }
}

