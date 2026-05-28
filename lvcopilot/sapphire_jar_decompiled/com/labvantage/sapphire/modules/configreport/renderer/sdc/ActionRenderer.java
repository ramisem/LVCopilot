/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer.sdc;

import com.labvantage.sapphire.modules.configreport.util.ActionUtil;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.ConfigReportContent;
import sapphire.util.SDIData;

public class ActionRenderer
extends ActionUtil {
    @Override
    public ConfigReportContent getSpecialContent(SDIData sdiData, TranslationProcessor translationProcessor) throws SapphireException {
        ConfigReportContent str = new ConfigReportContent(this.config, "Action ");
        str.startSubSection("Action", "The following is the summary of the Action:");
        str.appendSubSection(this.renderActionInfo(sdiData, this.getTranslationProcessor()), "Action", this.diffOnly);
        str.startSubSection("Action Properties", "The following is the list of Action Properties:");
        str.appendSubSection(this.renderActionProperties(sdiData, this.getTranslationProcessor()), "Action Properties", this.diffOnly);
        if (ActionRenderer.getPrimaryValue(sdiData, "actionlanguage").equals("actionblock")) {
            str.startSubHeading("Flow Chart", "The following is the Flow Chart of the Action:");
            str.appendSubSection(this.renderFlowChart(str.getApplicationRoot(), str.getFolder(), sdiData, this.getTranslationProcessor(), true), "Flow Chart", this.diffOnly);
        }
        str.endSubSection("", "Action");
        return str;
    }

    @Override
    public ConfigReportContent getSpecialContent(SDIData sdiData, SDIData refSdiData, TranslationProcessor translationProcessor) throws SapphireException {
        ConfigReportContent str = new ConfigReportContent(this.config, "Action: " + sdiData.getDataset("primary").getString(0, "actionid"));
        str.startSubSection("Action", "The following is the summary of the Action:");
        str.appendSubSection(this.renderActionInfoDiff(sdiData, refSdiData, this.getTranslationProcessor(), false), "Action", this.diffOnly);
        str.startSubSection("Action Properties", "The following is the list of Action Properties:");
        str.appendSubSection(this.renderActionPropertiesDiff(sdiData, refSdiData, true), "Action Properties", this.diffOnly);
        if (ActionRenderer.getPrimaryValue(sdiData, "actionlanguage").equals("actionblock") && ActionRenderer.getPrimaryValue(refSdiData, "actionlanguage").equals("actionblock")) {
            str.startSubHeading("Flow Chart", "The following is the Flow Chart of the Action:");
            str.appendSubSection(this.renderFlowChartDiff(str.getApplicationRoot(), str.getFolder(), sdiData, refSdiData, this.getTranslationProcessor(), true), "Flow Chart", this.diffOnly);
        }
        str.endSubSection("", "Action");
        return str;
    }
}

