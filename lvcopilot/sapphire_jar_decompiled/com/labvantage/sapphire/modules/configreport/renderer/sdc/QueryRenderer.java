/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer.sdc;

import com.labvantage.sapphire.modules.configreport.util.QueryUtil;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.ConfigReportContent;
import sapphire.util.SDIData;

public class QueryRenderer
extends QueryUtil {
    public static final String SEPARATOR = "|!|";

    @Override
    public ConfigReportContent getSpecialContent(SDIData sdiData, TranslationProcessor translationProcessor) throws SapphireException {
        return this.getSpecialContent(sdiData, sdiData, translationProcessor);
    }

    @Override
    public ConfigReportContent getSpecialContent(SDIData srcSdiData, SDIData refSdiData, TranslationProcessor translationProcessor) throws SapphireException {
        ConfigReportContent str = new ConfigReportContent(this.config, "Query: " + srcSdiData.getDataset("primary").getString(0, "queryid"));
        str.startSubSection("Query", "The following is the summary of the Query definition:");
        str.appendSubSection(this.renderQueryInfoDiff(srcSdiData, refSdiData), "Query", this.diffOnly);
        ConfigReportContent[] two = this.renderQueryArgsDiff(srcSdiData, refSdiData, true);
        if (two[0].length() > 0) {
            str.appendSubSection(two[0], "Arguments", this.diffOnly);
        }
        if (two[1].length() > 0) {
            str.appendSubSection(two[1], "Product Arguments", this.diffOnly);
        }
        if ((two = this.renderQueryUnionDiff(srcSdiData, refSdiData, true))[0].length() > 0) {
            str.appendSubSection(two[0], "Union Clauses", this.diffOnly);
        }
        if (two[1].length() > 0) {
            str.appendSubSection(two[1], "Product Union Clauses", this.diffOnly);
        }
        str.endSubSection("", "Query");
        return str;
    }
}

