/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer.sdc;

import com.labvantage.sapphire.modules.configreport.ro.LV_TaskDefRO;
import com.labvantage.sapphire.modules.configreport.util.LV_TaskDefUtil;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.BaseSDCRO;
import sapphire.ext.ConfigReportContent;

public class LV_TaskDefRenderer
extends LV_TaskDefUtil {
    @Override
    public ConfigReportContent getSectionContent(BaseSDCRO sdcRO, BaseSDCRO refSdcRO, TranslationProcessor translationProcessor) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "LV_TaskDef");
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
                    configReportContent.startSubSection("Task", "");
                    configReportContent.appendSubSection(this.renderTaskDefInfo((LV_TaskDefRO)refSdcRO), "Task", this.diffOnly);
                    configReportContent.startSubSection("Appearance", "");
                    configReportContent.appendSubSection(this.renderAppearance((LV_TaskDefRO)refSdcRO, true), "Appearance", this.diffOnly);
                    configReportContent.startSubSection("Queues", "");
                    configReportContent.appendSubSection(this.renderQueuesInfo((LV_TaskDefRO)refSdcRO), "Queues", this.diffOnly);
                    configReportContent.startSubSection("Task Design", "");
                    configReportContent.appendSubSection(this.renderSteps((LV_TaskDefRO)refSdcRO, true), "Task Design", this.diffOnly);
                    configReportContent.startSubSection("Task Variables", "");
                    configReportContent.appendSubSection(this.renderVariables((LV_TaskDefRO)refSdcRO), "Task Variables", this.diffOnly);
                    configReportContent.startSubSection("Stages", "");
                    configReportContent.appendSubSection(this.renderStages((LV_TaskDefRO)refSdcRO), "Stages", this.diffOnly);
                    configReportContent.startSubSection("Assignment", "");
                    configReportContent.appendSubSection(this.renderTaskAssignment((LV_TaskDefRO)refSdcRO), "Assignment", this.diffOnly);
                } else if (this.refRO == null || refSdcRO.currentSDI == null) {
                    configReportContent.startSubSection("Task", "");
                    configReportContent.appendSubSection(this.renderTaskDefInfo((LV_TaskDefRO)sdcRO), "Task", this.diffOnly);
                    configReportContent.startSubSection("Appearance", "");
                    configReportContent.appendSubSection(this.renderAppearance((LV_TaskDefRO)sdcRO, true), "Appearance", this.diffOnly);
                    configReportContent.startSubSection("Queues", "");
                    configReportContent.appendSubSection(this.renderQueuesInfo((LV_TaskDefRO)sdcRO), "Queues", this.diffOnly);
                    configReportContent.startSubSection("Task Design", "");
                    configReportContent.appendSubSection(this.renderSteps((LV_TaskDefRO)sdcRO, true), "Task Design", this.diffOnly);
                    configReportContent.startSubSection("Task Variables", "");
                    configReportContent.appendSubSection(this.renderVariables((LV_TaskDefRO)sdcRO), "Task Variables", this.diffOnly);
                    configReportContent.startSubSection("Stages", "");
                    configReportContent.appendSubSection(this.renderStages((LV_TaskDefRO)sdcRO), "Stages", this.diffOnly);
                    configReportContent.startSubSection("Assignment", "");
                    configReportContent.appendSubSection(this.renderTaskAssignment((LV_TaskDefRO)sdcRO), "Assignment", this.diffOnly);
                } else {
                    configReportContent.startSubSection("Task", "");
                    configReportContent.appendSubSection(this.renderTaskDefInfoDiff((LV_TaskDefRO)sdcRO, (LV_TaskDefRO)refSdcRO), "Task", this.diffOnly);
                    configReportContent.startSubSection("Appearance", "");
                    configReportContent.appendSubSection(this.renderAppearanceDiff(this.config.getProperty("applicationurl"), (LV_TaskDefRO)sdcRO, (LV_TaskDefRO)refSdcRO, true), "Appearance", this.diffOnly);
                    configReportContent.startSubSection("Queues", "");
                    configReportContent.appendSubSection(this.renderQueuesInfoDiff((LV_TaskDefRO)sdcRO, (LV_TaskDefRO)refSdcRO), "Queues", this.diffOnly);
                    configReportContent.startSubSection("Task Design", "");
                    configReportContent.appendSubSection(this.renderStepsDiff(this.config.getProperty("applicationurl"), (LV_TaskDefRO)sdcRO, (LV_TaskDefRO)refSdcRO, true, true), "Task Design", this.diffOnly);
                    configReportContent.startSubSection("Task Variables", "");
                    configReportContent.appendSubSection(this.renderVariablesDiff((LV_TaskDefRO)sdcRO, (LV_TaskDefRO)refSdcRO), "Task Variables", this.diffOnly);
                    configReportContent.startSubSection("Stages", "");
                    configReportContent.appendSubSection(this.renderStagesDiff((LV_TaskDefRO)sdcRO, (LV_TaskDefRO)refSdcRO), "Stages", this.diffOnly);
                    configReportContent.startSubSection("Assignment", "");
                    configReportContent.appendSubSection(this.renderTaskAssignmentDiff((LV_TaskDefRO)sdcRO, (LV_TaskDefRO)refSdcRO), "Assignment", this.diffOnly);
                }
            } else {
                configReportContent.startSubSection("Task", "");
                configReportContent.appendSubSection(this.renderTaskDefInfo((LV_TaskDefRO)sdcRO), "Task", this.diffOnly);
                configReportContent.startSubSection("Appearance", "");
                configReportContent.appendSubSection(this.renderAppearance((LV_TaskDefRO)sdcRO, true), "Appearance", this.diffOnly);
                configReportContent.startSubSection("Queues", "");
                configReportContent.appendSubSection(this.renderQueuesInfo((LV_TaskDefRO)sdcRO), "Queues", this.diffOnly);
                configReportContent.startSubSection("Task Design", "");
                configReportContent.appendSubSection(this.renderSteps((LV_TaskDefRO)sdcRO, true), "Task Design", this.diffOnly);
                configReportContent.startSubSection("Task Variables", "");
                configReportContent.appendSubSection(this.renderVariables((LV_TaskDefRO)sdcRO), "Task Variables", this.diffOnly);
                configReportContent.startSubSection("Stages", "");
                configReportContent.appendSubSection(this.renderStages((LV_TaskDefRO)sdcRO), "Stages", this.diffOnly);
                configReportContent.startSubSection("Assignment", "");
                configReportContent.appendSubSection(this.renderTaskAssignment((LV_TaskDefRO)sdcRO), "Assignment", this.diffOnly);
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
        this.updateSectionChangeInfo("LV_TaskDef", ConfigReportContent.generateSDISectionTitle(currRO.currentSDI), configReportContent);
        return configReportContent;
    }

    @Override
    public void createXMLReport() throws SapphireException {
        super.createXMLReport();
        if (this.sdcRO != null && this.sdcRO.currentSDIData != null) {
            FileOutputStream taskDataFile;
            String taskData = ((LV_TaskDefRO)this.sdcRO).getTaskData().toXMLString();
            String xmlSdiFileName = ConfigReportContent.generateSDISectionXMLFileName(this.sdcRO.currentSDI);
            String xmlTaskDataFileName = this.folder + "/xmlreport/" + xmlSdiFileName.replace(".xml", "_taskdata.xml");
            String taskImageDefFileName = this.folder + "/xmlreport/" + xmlSdiFileName.replace(".xml", "_taskdefimage.png");
            String taskStepsImageFileName = this.folder + "/xmlreport/" + xmlSdiFileName.replace(".xml", "_taskstepsimage.png");
            try {
                taskDataFile = new FileOutputStream(xmlTaskDataFileName);
            }
            catch (FileNotFoundException e) {
                throw new SapphireException("Cannot create report xml file " + xmlSdiFileName);
            }
            try {
                taskDataFile.write(taskData.getBytes());
                taskDataFile.close();
            }
            catch (IOException e) {
                throw new SapphireException("Failed to create a section file");
            }
            ((LV_TaskDefRO)this.sdcRO).getTaskDefImage("", taskImageDefFileName, this.getConnectionId(), "TASK");
            ((LV_TaskDefRO)this.sdcRO).getTaskDefImage("", taskStepsImageFileName, this.getConnectionId(), "STEPS");
        }
    }
}

