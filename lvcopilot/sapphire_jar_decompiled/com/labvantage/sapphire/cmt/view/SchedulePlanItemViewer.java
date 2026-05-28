/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt.view;

import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.view.SDISnapshotViewer;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class SchedulePlanItemViewer
extends SDISnapshotViewer {
    @Override
    protected ConfigReportContent renderPrimaryDiff(SDISnapshotItem source, SDISnapshotItem ref, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns, boolean hideInheritedProperties, boolean isChild) throws SapphireException {
        ConfigReportContent primaryNoValueTree = super.renderPrimaryDiff(source, ref, showAuditColumns, showTranslation, hideEmptyColumns, hideInheritedProperties, isChild);
        String srcValueTree = this.getPrimaryValue(source == null ? new SDIData() : source.getSDIData(), "valuetree");
        String refValueTree = this.getPrimaryValue(ref == null ? new SDIData() : ref.getSDIData(), "valuetree");
        PropertyList srcProps = new PropertyList();
        srcProps.setPropertyList(srcValueTree);
        PropertyList refProps = new PropertyList();
        refProps.setPropertyList(refValueTree);
        primaryNoValueTree.startSubSection("Value Tree:", "");
        primaryNoValueTree.append(primaryNoValueTree.renderPropertyListDiff(srcProps, refProps, null, true, true, this.translationProcessor, hideEmptyColumns).toString());
        return primaryNoValueTree;
    }
}

