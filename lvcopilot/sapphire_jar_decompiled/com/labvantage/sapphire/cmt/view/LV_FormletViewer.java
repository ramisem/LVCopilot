/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt.view;

import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.view.SDISnapshotViewer;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.util.SDIData;

public class LV_FormletViewer
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
        content.addDiffRowItem("Formlet", this.getPrimaryValue(sourceSDIData, "formletid"), this.getPrimaryValue(refSDIData, "formletid"), this.translationProcessor);
        content.addDiffRowItem("Version", this.getPrimaryValue(sourceSDIData, "formversionid"), this.getPrimaryValue(refSDIData, "formversionid"), this.translationProcessor);
        content.addDiffRowItem("Version Status", this.getVersionStatus(sourceSDIData), this.getVersionStatus(refSDIData), this.translationProcessor);
        content.endRow();
        content.startRow();
        content.addDiffRowItem("Description", this.getPrimaryValue(sourceSDIData, "formletdesc"), this.getPrimaryValue(refSDIData, "formletdesc"), 6, this.translationProcessor);
        content.endRow();
        content.startRow();
        content.addDiffRowItem("Editable In Form", this.getPrimaryValue(sourceSDIData, "editableflag"), this.getPrimaryValue(refSDIData, "editableflag"), this.translationProcessor);
        content.addDiffRowItem("Allow AutoSave Binding", this.getPrimaryValue(sourceSDIData, "bindableflag"), this.getPrimaryValue(refSDIData, "bindableflag"), this.translationProcessor);
        content.endRow();
        content.endTable();
        String sourcefieldobj = this.getPrimaryValue(sourceSDIData, "formletobjects");
        String reffieldobj = this.getPrimaryValue(refSDIData, "formletobjects");
        if (sourcefieldobj.length() > 0 || reffieldobj.length() > 0) {
            content.appendSubSection(this.renderFieldObjectDiff(sourcefieldobj, reffieldobj, this.translationProcessor, hideEmptyColumns), "Form Object");
        }
        String sourcethumbnailhtml = this.getPrimaryValue(sourceSDIData, "thumbnailhtml");
        String refthumbnailhtml = this.getPrimaryValue(refSDIData, "thumbnailhtml");
        if (sourcethumbnailhtml.length() == refthumbnailhtml.length()) {
            content.startSubSection("Formlet Preview", "");
            content.append("<table style=\"border:3px; border-style:solid; border-color:black; padding: 1em;\"><tr><td> " + sourcethumbnailhtml + "</td></tr></table>");
        } else {
            content.startNewSubSection("New Formlet Preview:", "");
            content.append("<table style=\"border:3px; border-style:solid; border-color:green; padding: 1em;\"><tr><td>" + sourcethumbnailhtml + "</td></tr></table>");
            if (refItem != null && refthumbnailhtml.length() > 0) {
                content.startDeletedSubSection("Old Formlet Preview:", "");
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

