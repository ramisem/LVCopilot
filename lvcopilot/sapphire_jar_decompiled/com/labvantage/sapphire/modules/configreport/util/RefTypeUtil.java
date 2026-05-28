/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.util;

import com.labvantage.sapphire.modules.configreport.util.DDTLabelsUtil;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.ext.BaseSDCRenderer;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyListCollection;

public class RefTypeUtil
extends BaseSDCRenderer {
    public ConfigReportContent renderRefTypeInfo(SDIData sdiData) {
        return this.renderRefTypeInfoDiff(sdiData, sdiData);
    }

    public ConfigReportContent renderRefTypeInfoDiff(SDIData srcSDIData, SDIData refSDIData) {
        ConfigReportContent buffer = new ConfigReportContent(this.config, "RefType Info: " + RefTypeUtil.getPrimaryValue(srcSDIData, "reftypeid"));
        buffer.startTable();
        buffer.startRow();
        buffer.addDiffRowItem("Reference Type", RefTypeUtil.getPrimaryValue(srcSDIData, "reftypeid"), RefTypeUtil.getPrimaryValue(refSDIData, "reftypeid"), this.getTranslationProcessor());
        buffer.addDiffRowItem("Type", this.getType(srcSDIData), this.getType(refSDIData), this.getTranslationProcessor());
        buffer.endRow();
        buffer.startRow();
        buffer.addDiffRowItem("Description", RefTypeUtil.getPrimaryValue(srcSDIData, "reftypedesc"), RefTypeUtil.getPrimaryValue(refSDIData, "reftypedesc"), 3, this.getTranslationProcessor());
        buffer.endRow();
        buffer.endTable();
        return buffer;
    }

    public ConfigReportContent renderRefTypeValues(SDIData sdiData) {
        ConfigReportContent buffer = new ConfigReportContent(this.config, "RefType values: " + RefTypeUtil.getPrimaryValue(sdiData, "reftypeid"));
        buffer.renderListTable(this.getRefTypeValues(sdiData), this.getTranslationProcessor());
        return buffer;
    }

    public ConfigReportContent renderRefTypeValuesDiff(SDIData srcSDIData, SDIData refSDIData, boolean hideEmptyColumns) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "RefType values: " + RefTypeUtil.getPrimaryValue(srcSDIData, "reftypeid"));
        String[] keycols = new String[]{"Reference Value"};
        PropertyListCollection ignoreDiffs = this.getIgnoreDetailsDiffCols("refvalue");
        DataSet srcRefValues = this.getRefTypeValues(srcSDIData);
        DataSet refRefValues = this.getRefTypeValues(refSDIData);
        if (srcRefValues.getRowCount() > 0 || refRefValues.getRowCount() > 0) {
            HashMap<String, String> columnTitleMap = DDTLabelsUtil.getColumnTitleMap(this.getSDCProcessor(), "actionproperty", srcRefValues.getColumns());
            String tablelabel = DDTLabelsUtil.getLinkTableLabel(this.getSDCProcessor(), "RefType", "ref values", "refvalue");
            String itemdisplay = "[Reference Value]";
            configReportContent.renderDetailTablesDiff(columnTitleMap, "refvalue", tablelabel, itemdisplay, srcRefValues, refRefValues, keycols, this.getTranslationProcessor(), hideEmptyColumns);
        }
        return configReportContent;
    }

    public String getType(SDIData sdiData) {
        String colValue = RefTypeUtil.getPrimaryValue(sdiData, "typeflag");
        if ("C".equals(colValue)) {
            return "Core";
        }
        if ("S".equals(colValue)) {
            return "System";
        }
        if ("U".equals(colValue)) {
            return "User";
        }
        return "";
    }

    public DataSet getRefTypeValues(SDIData sdiData) {
        DataSet ds = sdiData.getDataset("refvalue");
        DataSet ret = new DataSet();
        if (ds != null && ds.getRowCount() > 0) {
            ret.setColidCaseSensitive(true);
            ret.addColumn("Reference Value", 0);
            ret.addColumn("Description", 0);
            ret.addColumn("Display Value", 0);
            ret.addColumn("Display Value", 0);
            ret.addColumn("Display Icon URL", 0);
            ret.addColumn("Hidden", 0);
            ret.addColumnValues("Reference Value", 0, ds.getColumnValues("refvalueid", ";"), ";");
            ret.addColumnValues("Description", 0, ds.getColumnValues("refvaluedesc", ";"), ";");
            ret.addColumnValues("Display Value", 0, ds.getColumnValues("refdisplayvalue", ";"), ";");
            ret.addColumnValues("Display Icon URL", 0, ds.getColumnValues("refdisplayicon", ";"), ";");
            String hidden = "";
            for (int i = 0; i < ds.getRowCount(); ++i) {
                if (i != 0) {
                    hidden = hidden + ";";
                }
                if ("N".equals(ds.getValue(i, "activeflag", ""))) {
                    hidden = hidden + "Yes";
                    continue;
                }
                if (!"Y".equals(ds.getValue(i, "activeflag", ""))) continue;
                hidden = hidden + "No";
            }
            ret.addColumnValues("Hidden", 0, hidden, ";");
        }
        return ret;
    }
}

