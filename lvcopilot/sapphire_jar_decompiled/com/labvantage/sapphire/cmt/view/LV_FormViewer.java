/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt.view;

import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.view.SDISnapshotViewer;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.util.SDIData;

public class LV_FormViewer
extends SDISnapshotViewer {
    @Override
    protected ConfigReportContent renderPrimaryDiff(SDISnapshotItem source, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns, boolean hideInheritedProperties, boolean isChild) throws SapphireException {
        ConfigReportContent content = new ConfigReportContent("Form Viewer ", this.translationProcessor);
        String sdiTitle = this.getFormattedItemLabel(source.getSDIData(), this.getSDITableLabelInfo(source.getSDIData().getSdcid())[1]);
        content.startSection(sdiTitle);
        SDIData sourceSDIData = source.getSDIData();
        SDIData refSDIData = refItem == null ? new SDIData() : refItem.getSDIData();
        content.startTable();
        content.startRow();
        content.addDiffRowItem("Form", this.getPrimaryValue(sourceSDIData, "formid"), this.getPrimaryValue(refSDIData, "formid"), this.translationProcessor);
        content.addDiffRowItem("Version", this.getPrimaryValue(sourceSDIData, "formversionid"), this.getPrimaryValue(refSDIData, "formversionid"), this.translationProcessor);
        content.addDiffRowItem("Version Status", this.getVersionStatus(sourceSDIData), this.getVersionStatus(refSDIData), this.translationProcessor);
        content.endRow();
        content.startRow();
        content.addDiffRowItem("Description", this.getPrimaryValue(sourceSDIData, "formdesc"), this.getPrimaryValue(refSDIData, "formdesc"), 6, this.translationProcessor);
        content.endRow();
        content.startRow();
        content.addDiffRowItem("Title", this.getPrimaryValue(sourceSDIData, "formtitle"), this.getPrimaryValue(refSDIData, "formtitle"), 6, this.translationProcessor);
        content.endRow();
        content.startRow();
        content.addDiffRowItem("Document Description Rule", this.getPrimaryValue(sourceSDIData, "documentdescrule"), this.getPrimaryValue(refSDIData, "documentdescrule"), 6, this.translationProcessor);
        content.endRow();
        content.startRow();
        content.addDiffRowItem("Transient", this.getPrimaryValue(sourceSDIData, "transientformflag"), this.getPrimaryValue(refSDIData, "transientformflag"), this.translationProcessor);
        content.addDiffRowItem("Virtual", this.getPrimaryValue(sourceSDIData, "virtualformflag"), this.getPrimaryValue(refSDIData, "virtualformflag"), this.translationProcessor);
        content.addDiffRowItem("Translate", this.getPrimaryValue(sourceSDIData, "translateflag"), this.getPrimaryValue(refSDIData, "translateflag"), this.translationProcessor);
        content.endRow();
        content.startRow();
        content.addDiffRowItem("Form Type", this.getPrimaryValue(sourceSDIData, "formtype"), this.getPrimaryValue(refSDIData, "formtype"), this.translationProcessor);
        content.addDiffRowItem("Worksheet Type", this.getPrimaryValue(sourceSDIData, "worksheettype"), this.getPrimaryValue(refSDIData, "worksheettype"), this.translationProcessor);
        content.addDiffRowItem("Items/Worksheet", this.getPrimaryValue(sourceSDIData, "worksheetqty"), this.getPrimaryValue(refSDIData, "worksheetqty"), this.translationProcessor);
        content.endRow();
        content.startRow();
        content.addDiffRowItem("Draftable", this.getPrimaryValue(sourceSDIData, "draftableflag"), this.getPrimaryValue(refSDIData, "draftableflag"), this.translationProcessor);
        content.addDiffRowItem("Validate Data when Drafting", this.getPrimaryValue(sourceSDIData, "validateondraftflag"), this.getPrimaryValue(refSDIData, "validateondraftflag"), this.translationProcessor);
        content.addDiffRowItem("Save Invalid Data", this.getPrimaryValue(sourceSDIData, "saveinvaliddataflag"), this.getPrimaryValue(refSDIData, "saveinvaliddataflag"), this.translationProcessor);
        content.endRow();
        content.startRow();
        content.addDiffRowItem("Checkable", this.getPrimaryValue(sourceSDIData, "checkableflag"), this.getPrimaryValue(refSDIData, "checkableflag"), this.translationProcessor);
        content.addDiffRowItem("Submitable", this.getPrimaryValue(sourceSDIData, "submitableflag"), this.getPrimaryValue(refSDIData, "submitableflag"), this.translationProcessor);
        content.addDiffRowItem("Submit Invalid Data", this.getPrimaryValue(sourceSDIData, "submitinvaliddataflag"), this.getPrimaryValue(refSDIData, "submitinvaliddataflag"), this.translationProcessor);
        content.endRow();
        content.startRow();
        content.addDiffRowItem("Approval Type", this.getPrimaryValue(sourceSDIData, "approvaltypeid"), this.getPrimaryValue(refSDIData, "approvaltypeid"), this.translationProcessor);
        content.addDiffRowItem("Approve Invalid Data", this.getPrimaryValue(sourceSDIData, "approveinvaliddataflag"), this.getPrimaryValue(refSDIData, "approveinvaliddataflag"), 4, this.translationProcessor);
        content.endRow();
        content.startRow();
        content.addDiffRowItem("Double Data Entry (DDE)", this.getPrimaryValue(sourceSDIData, "checkableflag"), this.getPrimaryValue(refSDIData, "checkableflag"), this.translationProcessor);
        content.addDiffRowItem("DDE User 2 Receives Discrepancy Alerts", this.getPrimaryValue(sourceSDIData, "ddeuser2alertflag"), this.getPrimaryValue(refSDIData, "ddeuser2alertflag"), this.translationProcessor);
        content.addDiffRowItem("DDE User 2 can Reconcile Data", this.getPrimaryValue(sourceSDIData, "ddeuser2reconcileflag"), this.getPrimaryValue(refSDIData, "ddeuser2reconcileflag"), this.translationProcessor);
        content.endRow();
        content.startRow();
        content.addDiffRowItem("Reconciliation Role", this.getPrimaryValue(sourceSDIData, "reconciliationroleid"), this.getPrimaryValue(refSDIData, "reconciliationroleid"), this.translationProcessor);
        content.addDiffRowItem("Document Manager Role", this.getPrimaryValue(sourceSDIData, "documentmanagerroleid"), this.getPrimaryValue(refSDIData, "documentmanagerroleid"), this.translationProcessor);
        content.addDiffRowItem("Lock Document when Done", this.getPrimaryValue(sourceSDIData, "lockondoneflag"), this.getPrimaryValue(refSDIData, "lockondoneflag"), this.translationProcessor);
        content.endRow();
        content.startRow();
        content.addDiffRowItem("User Training Required", this.getPrimaryValue(sourceSDIData, "trainingreqflag"), this.getPrimaryValue(refSDIData, "trainingreqflag"), this.translationProcessor);
        content.addDiffRowItem("User Training Overrides", this.getPrimaryValue(sourceSDIData, "overrideallowedflag"), this.getPrimaryValue(refSDIData, "overrideallowedflag"), this.translationProcessor);
        content.addDiffRowItem("Versionable", this.getPrimaryValue(sourceSDIData, "versionableflag"), this.getPrimaryValue(refSDIData, "versionableflag"), this.translationProcessor);
        content.endRow();
        content.endTable();
        String sourceProcessingScript = this.getPrimaryValue(sourceSDIData, "processingscript");
        String refProcessingScript = this.getPrimaryValue(refSDIData, "processingscript");
        if (sourceProcessingScript.length() > 0 && refProcessingScript.length() == 0) {
            content.startNewSubSection("Form Processing", "");
            content.renderProcessingScript(sourceProcessingScript, this.translationProcessor);
        } else if (sourceProcessingScript.length() == 0 && refProcessingScript.length() > 0) {
            content.startDeletedSubSection("Form Processing", "");
            content.renderProcessingScript(refProcessingScript, this.translationProcessor);
        } else if (sourceProcessingScript.length() > 0 && refProcessingScript.length() > 0) {
            content.startSubSection("Form Processing", "");
            content.renderProcessingScriptDiff(sourceProcessingScript, refProcessingScript, true, this.translationProcessor);
        }
        String sourcefieldobj = this.getPrimaryValue(sourceSDIData, "formobjects");
        String reffieldobj = this.getPrimaryValue(refSDIData, "formobjects");
        if (sourcefieldobj.length() > 0 || reffieldobj.length() > 0) {
            content.appendSubSection(this.renderFieldObjectDiff(sourcefieldobj, reffieldobj, this.translationProcessor, hideEmptyColumns), "Form Object");
        }
        String sourcethumbnailhtml = this.getPrimaryValue(sourceSDIData, "thumbnailhtml");
        String refthumbnailhtml = this.getPrimaryValue(refSDIData, "thumbnailhtml");
        if (sourcethumbnailhtml.length() == refthumbnailhtml.length()) {
            content.startSubSection("Form Preview", "");
            content.append("<table style=\"border:3px; border-style:solid; border-color:black; padding: 1em;\"><tr><td> " + sourcethumbnailhtml + "</td></tr></table>");
        } else {
            content.startNewSubSection("New Form Preview:", "");
            content.append("<table style=\"border:3px; border-style:solid; border-color:green; padding: 1em;\"><tr><td>" + sourcethumbnailhtml + "</td></tr></table>");
            if (refItem != null && refthumbnailhtml.length() > 0) {
                content.startDeletedSubSection("Old Form Preview:", "");
                content.append("<table style=\"border:3px; border-style:solid; border-color:red; padding: 1em;\"><tr><td>" + refthumbnailhtml + "</td></tr></table>");
            }
        }
        return content;
    }

    @Override
    protected void renderItemDetailsDiff(ConfigReportContent configReportContent, SDISnapshotItem sourceItem, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns) throws SapphireException {
        super.renderItemDetailsDiff(configReportContent, sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
    }
}

