/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt.view;

import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.view.SDISnapshotViewer;
import com.labvantage.sapphire.modules.configreport.ro.QueryRO;
import com.labvantage.sapphire.modules.configreport.util.QueryUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.util.SDIData;

public class QueryViewer
extends SDISnapshotViewer {
    public QueryViewer() {
    }

    public QueryViewer(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
    }

    @Override
    public ConfigReportContent renderPrimaryDiff(SDISnapshotItem source, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns, boolean hideInherited, boolean isChild) throws SapphireException {
        SDIData srcSdiData = source.getSDIData();
        QueryRO srcRO = new QueryRO();
        QueryRO refRO = new QueryRO();
        srcRO.initialize(this.sapphireConnection);
        refRO.initialize(this.sapphireConnection);
        srcRO.setCurrentSDIData(source.getSDIData());
        if (refItem != null) {
            refRO.setCurrentSDIData(refItem.getSDIData());
        }
        QueryUtil util = new QueryUtil();
        util.initialize(this.sapphireConnection, srcRO, refRO);
        ConfigReportContent str = new ConfigReportContent("Query: " + srcSdiData.getDataset("primary").getString(0, "queryid"), this.translationProcessor);
        String sdiTitle = this.getFormattedItemLabel(srcSdiData, this.getSDITableLabelInfo(srcSdiData.getSdcid())[1]);
        str.startSection(sdiTitle);
        boolean diffOnly = false;
        str.appendSubSection(util.renderQueryInfoDiff(srcSdiData, refItem == null ? new SDIData() : refItem.getSDIData()), "Query", diffOnly);
        str.endSubSection("", "Query");
        return str;
    }

    @Override
    public void renderItemDetailsDiff(ConfigReportContent configReportContent, SDISnapshotItem sourceItem, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns) throws SapphireException {
        SDIData srcSdiData = sourceItem.getSDIData();
        QueryRO srcRO = new QueryRO();
        QueryRO refRO = new QueryRO();
        srcRO.initialize(this.sapphireConnection);
        refRO.initialize(this.sapphireConnection);
        srcRO.setCurrentSDIData(srcSdiData);
        if (refItem != null) {
            refRO.setCurrentSDIData(refItem.getSDIData());
        }
        QueryUtil util = new QueryUtil();
        util.initialize(this.sapphireConnection, srcRO, refRO);
        ConfigReportContent str = new ConfigReportContent("Query: " + srcSdiData.getDataset("primary").getString(0, "queryid"), this.translationProcessor);
        ConfigReportContent[] two = util.renderQueryArgsDiff(srcSdiData, refItem == null ? new SDIData() : refItem.getSDIData(), hideEmptyColumns);
        if (two[0].length() > 0) {
            str.appendSpecialContent(two[0]);
            configReportContent.appendNodeContent(str, "queryarg", "Arguments");
        }
        if (two[1].length() > 0) {
            str = new ConfigReportContent("Query: " + srcSdiData.getDataset("primary").getString(0, "queryid"), this.translationProcessor);
            str.appendSpecialContent(two[1]);
            configReportContent.appendNodeContent(str, "productqueryarg", "Product Arguments");
        }
        str = new ConfigReportContent("union", this.translationProcessor);
        ConfigReportContent[] union = util.renderQueryUnionDiff(srcSdiData, refItem == null ? new SDIData() : refItem.getSDIData(), hideEmptyColumns);
        if (union[0].length() > 0) {
            str.appendSpecialContent(union[0]);
            configReportContent.appendNodeContent(str, "queryunion", "Union Clauses");
        }
        str = new ConfigReportContent("union", this.translationProcessor);
        if (union[1].length() > 0) {
            str.appendSpecialContent(union[1]);
            configReportContent.appendNodeContent(str, "productqueryunion", "Product Union Clauses");
        }
        str = new ConfigReportContent("categories", this.translationProcessor);
        this.renderCategores(str, sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        configReportContent.appendNodeContent(str, "categoryitem", "Categories");
        str = new ConfigReportContent("other", this.translationProcessor);
        this.renderOtherCommonDetails(str, sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        configReportContent.appendSpecialContent(str);
    }
}

