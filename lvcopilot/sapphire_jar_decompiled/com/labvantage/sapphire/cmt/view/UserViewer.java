/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt.view;

import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.view.SDISnapshotViewer;
import com.labvantage.sapphire.modules.configreport.ro.UserRO;
import com.labvantage.sapphire.modules.configreport.util.UserUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.util.SDIData;

public class UserViewer
extends SDISnapshotViewer {
    public UserViewer() {
    }

    public UserViewer(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
    }

    @Override
    public ConfigReportContent renderPrimaryDiff(SDISnapshotItem source, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns, boolean hideInheritedProperties, boolean isChild) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent("User: ", this.translationProcessor);
        SDIData srcSdiData = source.getSDIData();
        UserRO srcRO = new UserRO();
        UserRO refRO = new UserRO();
        srcRO.initialize(this.sapphireConnection);
        refRO.initialize(this.sapphireConnection);
        srcRO.setCurrentSDIData(srcSdiData);
        if (refItem != null) {
            refRO.setCurrentSDIData(refItem.getSDIData());
        }
        UserUtil util = new UserUtil();
        util.initialize(this.sapphireConnection, srcRO, refRO);
        String sdiTitle = this.getFormattedItemLabel(srcSdiData, this.getSDITableLabelInfo(srcSdiData.getSdcid())[1]);
        configReportContent.startSection(sdiTitle);
        boolean diffOnly = false;
        configReportContent.appendSubSection(util.renderUserInfoDiff(), "User", diffOnly);
        ConfigReportContent matrix = util.renderUserSDCAccessMatrixDiff();
        if (matrix.length() > 0) {
            configReportContent.startSubSection("SDC Access Matrix", "");
            configReportContent.appendSubSection(matrix, "SDC Access Matrix", diffOnly);
        }
        if ((matrix = util.renderUserDeptAccessMatrixDiff()).length() > 0) {
            configReportContent.startSubSection("Department Access Matrix", "");
            configReportContent.appendSubSection(matrix, "Department Access Matrix", diffOnly);
        }
        configReportContent.endSubSection("", "Summary");
        configReportContent.endSection();
        return configReportContent;
    }

    @Override
    public void renderItemDetailsDiff(ConfigReportContent configReportContent, SDISnapshotItem sourceItem, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns) throws SapphireException {
        ConfigReportContent cert = this.renderCertifications(sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        configReportContent.appendNodeContent(cert, "s_sdicertification", "Certification");
        ConfigReportContent str = new ConfigReportContent("categories", this.translationProcessor);
        this.renderCategores(str, sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        configReportContent.appendNodeContent(str, "categoryitem", "Categories");
        str = new ConfigReportContent("other", this.translationProcessor);
        this.renderOtherCommonDetails(str, sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        configReportContent.appendSpecialContent(str);
    }

    @Override
    public String[] getIgnoreDataSets() {
        return new String[]{"sdcsecurity", "sysuserrole", "departmentsysuser", "modulesysuser", "sysuserjobtype"};
    }
}

