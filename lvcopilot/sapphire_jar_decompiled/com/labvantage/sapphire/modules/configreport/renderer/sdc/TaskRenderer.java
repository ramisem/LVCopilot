/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer.sdc;

import com.labvantage.sapphire.modules.configreport.util.TaskUtil;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.ConfigReportContent;
import sapphire.util.SDIData;

public class TaskRenderer
extends TaskUtil {
    @Override
    public ConfigReportContent getSpecialContent(SDIData sdiData, TranslationProcessor translationProcessor) throws SapphireException {
        ConfigReportContent str = new ConfigReportContent(this.config, "Task:" + TaskRenderer.getPrimaryValue(sdiData, "taskid"));
        str.startSubSection("Task", "The following is the summary of the Task definition:");
        str.appendSubSection(this.renderTaskInfo(sdiData), "Task", this.diffOnly);
        str.startSubSection("Task Properties", "This is the list of task properties");
        str.appendSubSection(this.renderTaskProperties(sdiData), "Task Properties", this.diffOnly);
        str.startSubSection("Task Schedule", "");
        str.appendSubSection(this.renderScheduleInfo(sdiData), "Task Schedule", this.diffOnly);
        str.endSubSection("", "Summary");
        return str;
    }

    @Override
    public ConfigReportContent getSpecialContent(SDIData srcSDIData, SDIData refSDIData, TranslationProcessor translationProcessor) throws SapphireException {
        ConfigReportContent str = new ConfigReportContent(this.config, "Task:" + TaskRenderer.getPrimaryValue(srcSDIData, "taskid"));
        str.startSubSection("Task", "The following is the summary of the Task definition:");
        str.appendSubSection(this.renderTaskInfoDiff(srcSDIData, refSDIData), "Task", this.diffOnly);
        str.startSubSection("Task Properties", "This is the list of task properties");
        str.appendSubSection(this.renderTaskPropertiesDiff(srcSDIData, refSDIData, true), "Task Properties", this.diffOnly);
        str.startSubSection("Task Schedule", "");
        str.appendSubSection(this.renderScheduleInfoDiff(srcSDIData, refSDIData), "Task Schedule", this.diffOnly);
        str.endSubSection("", "Summary");
        return str;
    }
}

