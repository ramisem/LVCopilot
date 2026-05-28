/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt.view;

import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.view.SDISnapshotViewer;
import com.labvantage.sapphire.modules.configreport.ro.WebPageRO;
import com.labvantage.sapphire.modules.configreport.util.WebPageUtil;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.util.SDIData;

public class WebPageViewer
extends SDISnapshotViewer {
    boolean hideInheritedProperties = false;

    @Override
    public ConfigReportContent renderPrimaryDiff(SDISnapshotItem source, SDISnapshotItem ref, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns, boolean hideInheritedProperties, boolean isChild) throws SapphireException {
        this.hideInheritedProperties = hideInheritedProperties;
        ConfigReportContent configReportContent = new ConfigReportContent("WebPage", this.translationProcessor);
        configReportContent.setFoundDiff(false);
        WebPageRO sdcRO = new WebPageRO();
        sdcRO.initialize("WebPage", this.sapphireConnection);
        sdcRO.setCurrentSDIData(source.getSDIData());
        WebPageRO refSdcRO = new WebPageRO();
        refSdcRO.initialize("WebPage", this.sapphireConnection);
        if (ref != null) {
            refSdcRO.setCurrentSDIData(ref.getSDIData());
        }
        WebPageUtil util = new WebPageUtil();
        util.initialize(this.sapphireConnection, sdcRO, refSdcRO);
        String sdiTitle = this.getFormattedItemLabel(source.getSDIData(), this.getSDITableLabelInfo(source.getSDIData().getSdcid())[1]);
        configReportContent.startSection(sdiTitle);
        String currWebPageId = sdcRO.getWebPageId();
        String currEdition = sdcRO.getWebPageProductEdition();
        configReportContent.appendSpecialContent(util.renderWebPageInfo(sdcRO.currentSDIData, ref == null ? new SDIData() : refSdcRO.currentSDIData, currWebPageId, currEdition, hideEmptyColumns));
        configReportContent.endSection();
        configReportContent.endSection();
        return configReportContent;
    }

    @Override
    protected void renderItemDetailsDiff(ConfigReportContent configReportContent, SDISnapshotItem sourceItem, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns) throws SapphireException {
        WebPageRO sdcRO = new WebPageRO();
        sdcRO.initialize("WebPage", this.sapphireConnection);
        sdcRO.setCurrentSDIData(sourceItem.getSDIData());
        WebPageRO refSdcRO = new WebPageRO();
        refSdcRO.initialize("WebPage", this.sapphireConnection);
        if (refItem != null) {
            refSdcRO.setCurrentSDIData(refItem.getSDIData());
        }
        WebPageUtil util = new WebPageUtil();
        util.initialize(this.sapphireConnection, sdcRO, refSdcRO);
        String webpageid = sourceItem.getKeyId1();
        DataSet src = sourceItem.getSDIData().getDataset("webpagepropertytree");
        DataSet ref = null;
        ref = refItem != null ? refItem.getSDIData().getDataset("webpagepropertytree") : new DataSet();
        if (src == null) {
            src = new DataSet();
        }
        if (ref == null) {
            ref = new DataSet();
        }
        ConfigReportContent out = util.renderWebPagePropertyTreeDiff(webpageid, src, ref, this.hideInheritedProperties, hideEmptyColumns, this.translationProcessor);
        configReportContent.appendSpecialContent(out);
        ConfigReportContent str = new ConfigReportContent("categories", this.translationProcessor);
        this.renderCategores(str, sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        configReportContent.appendSpecialContent(str);
        str = new ConfigReportContent("other", this.translationProcessor);
        this.renderOtherCommonDetails(str, sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        configReportContent.appendSpecialContent(str);
        str = new ConfigReportContent("roles", this.translationProcessor);
        this.renderRoles(str, sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        configReportContent.appendSpecialContent(str);
    }
}

