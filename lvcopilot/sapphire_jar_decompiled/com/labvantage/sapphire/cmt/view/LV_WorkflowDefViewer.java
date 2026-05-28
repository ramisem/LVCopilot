/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt.view;

import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.view.SDISnapshotViewer;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefWorkflow;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class LV_WorkflowDefViewer
extends SDISnapshotViewer {
    public LV_WorkflowDefViewer() {
    }

    public LV_WorkflowDefViewer(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
    }

    @Override
    public ConfigReportContent renderPrimaryDiff(SDISnapshotItem source, SDISnapshotItem ref, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns, boolean hideInheritedProperties, boolean isChild) throws SapphireException {
        String refworkflowdefs;
        ConfigReportContent configReportContent = new ConfigReportContent("LV_WorkflowDef", this.translationProcessor);
        configReportContent.setFoundDiff(false);
        SDIData srcSdiData = source.getSDIData();
        SDIData refSdiData = ref == null ? new SDIData() : ref.getSDIData();
        PropertyList srcoutputProps = new PropertyList();
        PropertyList refoutputProps = new PropertyList();
        String srcworkflowdefs = this.getPrimaryValue(srcSdiData, "workflowdef");
        if (srcworkflowdefs.length() > 0) {
            srcoutputProps.setPropertyList(srcworkflowdefs, false, false);
        }
        if ((refworkflowdefs = this.getPrimaryValue(refSdiData, "workflowdef")).length() > 0) {
            refoutputProps.setPropertyList(refworkflowdefs, false, false);
        }
        String sdiTitle = this.getFormattedItemLabel(srcSdiData, this.getSDITableLabelInfo(srcSdiData.getSdcid())[1]);
        configReportContent.startSection(sdiTitle);
        boolean diffOnly = false;
        configReportContent.startSubSection("Workflow Details", "");
        configReportContent.appendSubSection(this.renderWorkflowDetails(srcSdiData, refSdiData, srcoutputProps, refoutputProps), this.translationProcessor.translate("Workflow Details"), diffOnly);
        configReportContent.startSubSection("Workflow Tasks", "");
        configReportContent.appendSubSection(this.renderWorkflowTasks(srcSdiData, refSdiData, srcoutputProps, refoutputProps, hideEmptyColumns), this.translationProcessor.translate("Workflow Details"), diffOnly);
        configReportContent.startSubSection("Workflow Variables", "");
        configReportContent.appendSubSection(this.renderVariablesDiff(srcoutputProps, refoutputProps), "Workflow Variables", diffOnly);
        return configReportContent;
    }

    @Override
    protected void renderItemDetailsDiff(ConfigReportContent configReportContent, SDISnapshotItem sourceItem, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns) throws SapphireException {
        ConfigReportContent str = new ConfigReportContent("categories", this.translationProcessor);
        this.renderCategores(str, sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        configReportContent.appendNodeContent(str, "categoryitem", "Categories");
        str = new ConfigReportContent("other", this.translationProcessor);
        this.renderOtherCommonDetails(str, sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        configReportContent.appendSpecialContent(str);
    }

    @Override
    public String[] getIgnoreDataSets() {
        return new String[]{"workflowdeftask", "workflowdeftaskio"};
    }

    public ConfigReportContent renderWorkflowTasks(SDIData srcSdiData, SDIData refSdiData, PropertyList srcProps, PropertyList refProps, boolean hideEmptyColumns) {
        ConfigReportContent configReportContent = new ConfigReportContent("Workflow Tasks", this.translationProcessor);
        String srcImageBase64 = this.getPrimaryValue(srcSdiData, "thumbnailimagesteps");
        String refImageBase64 = this.getPrimaryValue(refSdiData, "thumbnailimagesteps");
        if (srcImageBase64.length() == refImageBase64.length() && srcImageBase64.equals(refImageBase64)) {
            if (srcImageBase64.length() > 0) {
                configReportContent.append("<img src=\"data:image/gif;base64," + srcImageBase64 + "\" />  ");
            } else {
                configReportContent.append("<P>Workflow steps image is empty.");
            }
        } else {
            configReportContent.startNewSubSection("New Workflow Steps:", "");
            String sourcethumbnailhtml = srcImageBase64.length() == 0 ? "<P>Workflow steps image is empty." : "<img src=\"data:image/gif;base64," + srcImageBase64 + "\" />  ";
            String refthumbnailhtml = refImageBase64.length() == 0 ? "<P>Workflow steps image is empty." : "<img src=\"data:image/gif;base64," + refImageBase64 + "\" />  ";
            configReportContent.append("<table style=\"border:3px; border-style:solid; border-color:green; padding: 1em;\"><tr><td>" + sourcethumbnailhtml + "</td></tr></table>");
            if (refImageBase64.length() > 0) {
                configReportContent.startDeletedSubSection("Old Workflow Steps:", "");
                configReportContent.append("<table style=\"border:3px; border-style:solid; border-color:red; padding: 1em;\"><tr><td>" + refthumbnailhtml + "</td></tr></table>");
            }
        }
        PropertyListCollection srcTasks = srcProps.getCollectionNotNull("tasks");
        PropertyListCollection refTasks = refProps.getCollectionNotNull("tasks");
        ConfigReportContent tasks = this.renderTasks(srcTasks, refTasks, hideEmptyColumns);
        configReportContent.appendSpecialContent(tasks);
        return configReportContent;
    }

    public ConfigReportContent renderTasks(PropertyListCollection srcTasks, PropertyListCollection refTasks, boolean hideEmptyColumns) {
        String taskdefitemid;
        int i;
        ConfigReportContent taskDetailList = new ConfigReportContent("Task detail list", this.translationProcessor);
        for (i = 0; i < srcTasks.size(); ++i) {
            PropertyList currentSrctask = srcTasks.getPropertyList(i);
            taskdefitemid = currentSrctask.getProperty("taskdefitemid");
            PropertyList currentReftask = refTasks.find("taskdefitemid", taskdefitemid);
            taskDetailList.append(this.renderTaskDetailsDiff(currentSrctask, currentReftask).toString());
            taskDetailList.append(this.renderSetupVariables(currentSrctask, currentReftask, hideEmptyColumns).toString());
            taskDetailList.append(this.renderTaskQueues(currentSrctask, currentReftask, hideEmptyColumns).toString());
            taskDetailList.append(this.renderAssignmentDiff(currentSrctask, currentReftask).toString());
        }
        for (i = 0; i < refTasks.size(); ++i) {
            PropertyList currentReftask = refTasks.getPropertyList(i);
            taskdefitemid = currentReftask.getProperty("taskdefitemid");
            PropertyList currentSrctask = srcTasks.find("taskdefitemid", taskdefitemid);
            if (currentSrctask != null) continue;
            taskDetailList.append(this.renderTaskDetailsDiff(currentSrctask, currentReftask).toString());
            taskDetailList.append(this.renderSetupVariables(currentSrctask, currentReftask, hideEmptyColumns).toString());
            taskDetailList.append(this.renderTaskQueues(currentSrctask, currentReftask, hideEmptyColumns).toString());
            taskDetailList.append(this.renderAssignmentDiff(currentSrctask, currentReftask).toString());
        }
        return taskDetailList;
    }

    public ConfigReportContent renderTaskDetailsDiff(PropertyList srcTask, PropertyList refTask) {
        ConfigReportContent taskDetail = new ConfigReportContent("Task Detail", this.translationProcessor);
        if (refTask == null) {
            taskDetail.startSubSection(srcTask.getProperty("taskdefid") + " Details", "");
        } else if (srcTask == null) {
            taskDetail.startSubSection(refTask.getProperty("taskdefid") + " Details", "");
        } else {
            taskDetail.startSubSection(srcTask.getProperty("taskdefid") + " Details", "");
        }
        if (srcTask == null) {
            srcTask = new PropertyList();
        }
        if (refTask == null) {
            refTask = new PropertyList();
        }
        taskDetail.startTable();
        taskDetail.startRow();
        taskDetail.addDiffRowItem("Execution Title", srcTask.getProperty("longtitle"), refTask.getProperty("longtitle"), 3, this.translationProcessor);
        taskDetail.endRow();
        taskDetail.startRow();
        taskDetail.addDiffRowItem("Icon Text", srcTask.getProperty("shorttitle"), refTask.getProperty("shorttitle"));
        String srcappearance = srcTask.getProperty("appearance", TaskDefWorkflow.Appearance.OFFSET.toString());
        String refappearance = refTask.size() > 0 ? refTask.getProperty("appearance", TaskDefWorkflow.Appearance.OFFSET.toString()) : "";
        taskDetail.addDiffRowItem("Appearance", srcappearance, refappearance);
        taskDetail.endRow();
        taskDetail.startRow();
        taskDetail.addDiffRowItem("Icon", srcTask.getProperty("icon"), refTask.getProperty("icon"), 3, this.translationProcessor);
        taskDetail.endRow();
        taskDetail.startRow();
        taskDetail.addDiffRowItem("Task Colors", this.getTaskColors(srcTask), this.getTaskColors(refTask), 3, this.translationProcessor);
        taskDetail.endRow();
        taskDetail.endTable();
        return taskDetail;
    }

    public ConfigReportContent renderSetupVariables(PropertyList srcTask, PropertyList refTask, boolean hideEmptyColumns) {
        if (srcTask == null) {
            srcTask = new PropertyList();
        }
        if (refTask == null) {
            refTask = new PropertyList();
        }
        DataSet srcSetupVar = this.getSetupVariables(srcTask);
        DataSet refSetupVar = this.getSetupVariables(refTask);
        ConfigReportContent var = new ConfigReportContent("variables", this.translationProcessor);
        var.startSubHeading("Variables", "");
        var.renderDiffListTable(srcSetupVar, refSetupVar, new String[]{"variableid"}, this.translationProcessor, hideEmptyColumns);
        return var;
    }

    public ConfigReportContent renderTaskQueues(PropertyList srcTask, PropertyList refTask, boolean hideEmptyColumns) {
        if (srcTask == null) {
            srcTask = new PropertyList();
        }
        if (refTask == null) {
            refTask = new PropertyList();
        }
        DataSet srcSetupVar = ConfigReportContent.convertSimpleCollToDS(srcTask.getCollectionNotNull("taskio"), null);
        DataSet refSetupVar = ConfigReportContent.convertSimpleCollToDS(refTask.getCollectionNotNull("taskio"), null);
        ConfigReportContent var = new ConfigReportContent("queues", this.translationProcessor);
        var.startSubHeading("Queues", "");
        var.renderDiffListTable(srcSetupVar, refSetupVar, new String[]{"queueid"}, this.translationProcessor, hideEmptyColumns);
        return var;
    }

    public ConfigReportContent renderAssignmentDiff(PropertyList srcTask, PropertyList refTask) {
        ConfigReportContent assignmentContent = new ConfigReportContent("Assignment", this.translationProcessor);
        if (srcTask == null) {
            srcTask = new PropertyList();
        }
        if (refTask == null) {
            refTask = new PropertyList();
        }
        if (srcTask.getProperty("assignmentflag", "").length() > 0 || refTask.getProperty("assignmentflag", "").length() > 0) {
            assignmentContent.startSubHeading("Assignment", "");
            assignmentContent.startTable();
            assignmentContent.startRow();
            assignmentContent.addDiffRowItem("Assigment Type", srcTask.getProperty("assignmentflag", ""), refTask.getProperty("assignmentflag", ""), this.translationProcessor);
            assignmentContent.addDiffRowItem("User", srcTask.getProperty("sysuserid", ""), refTask.getProperty("sysuserid", ""));
            assignmentContent.endRow();
            assignmentContent.startRow();
            assignmentContent.addDiffRowItem("Role", srcTask.getProperty("roleid", ""), refTask.getProperty("roleid", ""), this.translationProcessor);
            assignmentContent.addDiffRowItem("Department", srcTask.getProperty("departmentid", ""), refTask.getProperty("departmentid", ""), this.translationProcessor);
            assignmentContent.endRow();
            assignmentContent.endTable();
        }
        return assignmentContent;
    }

    private String getTaskColors(PropertyList taskProps) {
        if (taskProps.size() > 0) {
            String color1 = taskProps.getProperty("taskcolor1", "#e7f2ff");
            String color2 = taskProps.getProperty("taskcolor2", "#d4e3f2");
            String color3 = taskProps.getProperty("taskcolor3", "#c9d8ed");
            return color1 + ", " + color2 + ", " + color3;
        }
        return "";
    }

    public DataSet getSetupVariables(PropertyList task) {
        return ConfigReportContent.convertSimpleCollToDS(task.getCollectionNotNull("variables"), null);
    }

    public ConfigReportContent renderWorkflowDetails(SDIData srcSDIData, SDIData refSDIData, PropertyList srcProps, PropertyList refProps) {
        ConfigReportContent info = new ConfigReportContent("Task Definition", this.translationProcessor);
        info.startTable();
        info.startRow();
        info.addDiffRowItem("Workflow ID", this.getPrimaryValue(srcSDIData, "workflowdefid"), this.getPrimaryValue(refSDIData, "workflowdefid"));
        info.addDiffRowItem("Version", this.getPrimaryValue(srcSDIData, "workflowdefversionid"), this.getPrimaryValue(refSDIData, "workflowdefversionid"));
        info.endRow();
        info.startRow();
        info.addDiffRowItem("Variant", this.getPrimaryValue(srcSDIData, "workflowdefvariantid"), this.getPrimaryValue(refSDIData, "workflowdefvariantid"), 3, this.translationProcessor);
        info.endRow();
        info.startRow();
        info.addDiffRowItem("Description", this.getPrimaryValue(srcSDIData, "workflowdefdesc"), this.getPrimaryValue(refSDIData, "workflowdefdesc"), 3, this.translationProcessor);
        info.endRow();
        info.startRow();
        info.addDiffRowItem("Execution Type", this.getExecTypeFlag(srcSDIData), this.getExecTypeFlag(refSDIData));
        info.addDiffRowItem("Line Type", srcProps.getProperty("connector", "").toLowerCase(), refProps.getProperty("connector").toLowerCase());
        info.endRow();
        info.endTable();
        return info;
    }

    public ConfigReportContent renderVariablesDiff(PropertyList srcProps, PropertyList refProps) {
        ConfigReportContent buffer = new ConfigReportContent("Variables", this.translationProcessor);
        DataSet srcVariables = this.getWorkflowVariables(srcProps);
        DataSet refVariables = this.getWorkflowVariables(refProps);
        buffer.renderDiffListTable(srcVariables, refVariables, new String[]{"Variable"});
        return buffer;
    }

    private DataSet getWorkflowVariables(PropertyList defProps) {
        PropertyListCollection variablesCollection = defProps.getCollectionNotNull("variables");
        DataSet ds = new DataSet();
        ds.setColidCaseSensitive(true);
        for (int i = 0; i < variablesCollection.size(); ++i) {
            PropertyList currentVar = variablesCollection.getPropertyList(i);
            int currRow = ds.addRow();
            ds.setString(currRow, "Variable", currentVar.getProperty("variableid"));
            ds.setString(currRow, "Type", currentVar.getProperty("type"));
            ds.setString(currRow, "Default Value", currentVar.getProperty("defaultvalue"));
            ds.setString(currRow, "Setup Variable", currentVar.getProperty("setup"));
            ds.setString(currRow, "Exposed", currentVar.getProperty("exposed"));
            ds.setString(currRow, "Prompt", currentVar.getProperty("prompt"));
            ds.setString(currRow, "Modifiable", currentVar.getProperty("modifiable", "Y"));
            ds.setString(currRow, "Mandatory", currentVar.getProperty("mandatory"));
            ds.setString(currRow, "Hidden", currentVar.getProperty("hidden"));
            ds.setString(currRow, "Help Text", currentVar.getProperty("help"));
            ds.setString(currRow, "Editor Style", currentVar.getProperty("editorstyleid"));
            ds.setString(currRow, "Related Variabled", currentVar.getProperty("relatedvariableid"));
            ds.setString(currRow, "Description", currentVar.getProperty("description"));
        }
        return ds;
    }

    private String getExecTypeFlag(SDIData sdiData) {
        String exectypeflag = this.getPrimaryValue(sdiData, "exectypeflag");
        if (exectypeflag.equals("S")) {
            return "Single Execution";
        }
        if (exectypeflag.equals("N")) {
            return "Named Executions";
        }
        if (exectypeflag.equals("A")) {
            return "Audo Executions";
        }
        return "";
    }
}

