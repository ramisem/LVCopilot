/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.util;

import com.labvantage.sapphire.modules.configreport.util.DDTLabelsUtil;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.BaseSDCRenderer;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class ActionUtil
extends BaseSDCRenderer {
    public static final String SEPARATOR = ";";

    public ConfigReportContent renderActionInfo(SDIData sdiData, TranslationProcessor translationProcessor) {
        ConfigReportContent buffer = new ConfigReportContent("Action info:" + sdiData.getDataset("primary").getString(0, "actionid"), this.getTranslationProcessor());
        buffer.startTable();
        buffer.startRow();
        buffer.addRowItem("Action", ActionUtil.getPrimaryValue(sdiData, "actionid"), translationProcessor);
        buffer.addRowItem("Action Version", ActionUtil.getPrimaryValue(sdiData, "actionversionid"), translationProcessor);
        buffer.endRow();
        buffer.startRow();
        buffer.addRowItem("Version Status", this.getVersionStatus(sdiData), translationProcessor);
        buffer.addRowItem("Type", this.getActionType(sdiData), translationProcessor);
        buffer.endRow();
        buffer.startRow();
        buffer.addRowItem("Description", ActionUtil.getPrimaryValue(sdiData, "actiondesc"), 3);
        buffer.endRow();
        buffer.startRow();
        buffer.addRowItem("Java Object Name", ActionUtil.getPrimaryValue(sdiData, "objectname"), 3);
        buffer.endRow();
        buffer.startRow();
        buffer.addRowItem("Action Language", ActionUtil.getPrimaryValue(sdiData, "actionlanguage"), 3);
        buffer.endRow();
        buffer.endTable();
        return buffer;
    }

    public ConfigReportContent renderActionInfoDiff(SDIData sdiData, SDIData refSDIData, TranslationProcessor translationProcessor, boolean hideEmptyColumns) {
        ConfigReportContent content = new ConfigReportContent("Action info: " + sdiData.getDataset("primary").getString(0, "actionid"), this.getTranslationProcessor());
        content.startTable();
        content.startRow();
        content.addDiffRowItem("Action", ActionUtil.getPrimaryValue(sdiData, "actionid"), ActionUtil.getPrimaryValue(refSDIData, "actionid"), translationProcessor);
        content.addDiffRowItem("Action Version", ActionUtil.getPrimaryValue(sdiData, "actionversionid"), ActionUtil.getPrimaryValue(refSDIData, "actionversionid"), translationProcessor);
        content.endRow();
        content.startRow();
        String versionStatus = this.getVersionStatus(sdiData);
        String refversionStatus = this.getVersionStatus(refSDIData);
        content.addDiffRowItem("Version Status", versionStatus, refversionStatus, translationProcessor);
        content.addDiffRowItem("Type", this.getActionType(sdiData), this.getActionType(refSDIData), translationProcessor);
        content.endRow();
        String srcdesc = ActionUtil.getPrimaryValue(sdiData, "actiondesc");
        String refdesc = ActionUtil.getPrimaryValue(refSDIData, "actiondesc");
        if (srcdesc.length() > 0 || refdesc.length() > 0 && !hideEmptyColumns) {
            content.startRow();
            content.addDiffRowItem("Description", srcdesc, refdesc, 3, translationProcessor);
            content.endRow();
        }
        content.startRow();
        content.addDiffRowItem("Java Object Name", ActionUtil.getPrimaryValue(sdiData, "objectname"), ActionUtil.getPrimaryValue(refSDIData, "objectname"), 3, this.getTranslationProcessor());
        content.endRow();
        content.startRow();
        content.addDiffRowItem("Action Language", ActionUtil.getPrimaryValue(sdiData, "actionlanguage"), ActionUtil.getPrimaryValue(refSDIData, "actionlanguage"), 3, this.getTranslationProcessor());
        content.endRow();
        content.endTable();
        return content;
    }

    public String getVersionStatus(SDIData sdiData) {
        String colVal = ActionUtil.getPrimaryValue(sdiData, "versionstatus");
        String versionStatus = "";
        if ("A".equals(colVal)) {
            versionStatus = "Active";
        } else if ("P".equals(colVal)) {
            versionStatus = "Provisional";
        }
        return versionStatus;
    }

    public String getActionType(SDIData sdiData) {
        String colVal = ActionUtil.getPrimaryValue(sdiData, "actiontype");
        if ("C".equals(colVal)) {
            return "Core";
        }
        if ("S".equals(colVal)) {
            return "System";
        }
        if ("U".equals(colVal)) {
            return "User";
        }
        return "";
    }

    public ConfigReportContent renderActionProperties(SDIData sdiData, TranslationProcessor translationProcessor) {
        ConfigReportContent buffer = new ConfigReportContent("action properties:" + sdiData.getDataset("primary").getString(0, "actionid"), this.getTranslationProcessor());
        DataSet ds = this.getActionProperties(sdiData);
        buffer.renderListTable(ds, translationProcessor);
        return buffer;
    }

    public ConfigReportContent renderActionPropertiesDiff(SDIData sdiData, SDIData refSDIData, boolean hideEmptyColumns) throws SapphireException {
        ConfigReportContent content = new ConfigReportContent("Action properties: " + sdiData.getDataset("primary").getString(0, "actionid"), this.getTranslationProcessor());
        DataSet actionProperties = this.getActionProperties(sdiData);
        DataSet actionRefProperties = this.getActionProperties(refSDIData);
        String[] keycols = new String[]{"Property"};
        HashMap<String, String> columnTitleMap = DDTLabelsUtil.getColumnTitleMap(this.getSDCProcessor(), "actionproperty", actionProperties.getColumns());
        String tablelabel = "Action Properties";
        String itemdisplay = "[Title]";
        ConfigReportContent contentdetails = new ConfigReportContent("details", this.getTranslationProcessor());
        contentdetails.renderDetailTablesDiff(columnTitleMap, "actionproperty", tablelabel, itemdisplay, actionProperties, actionRefProperties, keycols, this.getTranslationProcessor(), hideEmptyColumns);
        if (contentdetails.length() > 0) {
            content.startSubSection(tablelabel, "");
            content.appendSpecialContent(contentdetails);
        }
        return content;
    }

    public DataSet getActionProperties(SDIData sdiData) {
        DataSet ds = sdiData.getDataset("actionproperty");
        if (ds == null) {
            ds = new DataSet();
        }
        DataSet ret = new DataSet();
        if (ds.getRowCount() > 0) {
            ret.setColidCaseSensitive(true);
            ret.addColumn("Property", 0);
            ret.addColumn("Type", 0);
            ret.addColumn("Mode", 0);
            ret.addColumn("Title", 0);
            ret.addColumn("Editor Style", 0);
            ret.addColumn("Editor Return Mapping", 0);
            ret.addColumn("Edit SDC Id", 0);
            ret.addColumn("Edit Ref Type", 0);
            ret.addColumn("Help", 0);
            ret.addColumn("Default Value", 0);
            ret.addColumnValues("Property", 0, ds.getColumnValues("propertyid", SEPARATOR), SEPARATOR);
            ret.addColumnValues("Type", 0, ds.getColumnValues("propertytype", SEPARATOR), SEPARATOR);
            String[] modePropsVals = StringUtil.split(ds.getColumnValues("propertytypeflag", SEPARATOR), SEPARATOR);
            for (int i = 0; i < modePropsVals.length; ++i) {
                String mode = "";
                mode = "I".equals(modePropsVals[i]) ? "Input" : ("O".equals(modePropsVals[i]) ? "Output" : "Both");
                ret.setString(i, "Mode", mode);
            }
            ret.addColumnValues("Title", 0, ds.getColumnValues("propertytitle", SEPARATOR), SEPARATOR);
            ret.addColumnValues("Editor Style", 0, ds.getColumnValues("editorstyleid", SEPARATOR), SEPARATOR);
            ret.addColumnValues("Editor Return Mapping", 0, ds.getColumnValues("editorreturnmapping", SEPARATOR), SEPARATOR);
            ret.addColumnValues("Edit SDC Id", 0, ds.getColumnValues("editsdcid", SEPARATOR), SEPARATOR);
            ret.addColumnValues("Edit Ref Type", 0, ds.getColumnValues("editreftypeid", SEPARATOR), SEPARATOR);
            ret.addColumnValues("Help", 0, ds.getColumnValues("propertyhelp", SEPARATOR), SEPARATOR);
            ret.addColumnValues("Default Value", 0, ds.getColumnValues("defaultvalue", SEPARATOR), SEPARATOR);
        }
        return ret;
    }

    public ConfigReportContent renderFlowChart(String applicationRoot, String folder, SDIData sdiData, TranslationProcessor translationProcessor, boolean configreport) throws SapphireException {
        ConfigReportContent buffer = new ConfigReportContent("Action Block", translationProcessor);
        String actionScript = ActionUtil.getPrimaryValue(sdiData, "actionscript");
        if (actionScript != null && actionScript.length() > 0) {
            buffer.renderProcessingScript(applicationRoot, folder, actionScript, translationProcessor, configreport);
        }
        return buffer;
    }

    public ConfigReportContent renderFlowChartDiff(SDIData sdiData, SDIData refSDIData, TranslationProcessor translationProcessor) throws SapphireException {
        return this.renderFlowChartDiff("", "", sdiData, refSDIData, translationProcessor, false);
    }

    public ConfigReportContent renderFlowChartDiff(String applicationRoot, String folder, SDIData sdiData, SDIData refSDIData, TranslationProcessor translationProcessor, boolean configreport) throws SapphireException {
        ConfigReportContent buffer = new ConfigReportContent("Action Block", translationProcessor);
        String srcActionScript = ActionUtil.getPrimaryValue(sdiData, "actionscript");
        String refActionScript = ActionUtil.getPrimaryValue(refSDIData, "actionscript");
        buffer.renderProcessingScriptDiff(applicationRoot, folder, srcActionScript, refActionScript, true, translationProcessor, configreport);
        return buffer;
    }

    public String getLinkedRefTypeValue(String sdcid, String columnid, String value) {
        DataSet linksData = this.getSDCProcessor().getLinksData(sdcid);
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("sdccolumnid", columnid);
        filter.put("linksdcid", "RefType");
        DataSet match = linksData.getFilteredDataSet(filter);
        if (match.getRowCount() > 0 && match.getString(0, "reftypeid", "").length() > 0) {
            String reftypeid = match.getString(0, "reftypeid");
            String sql = "SELECT refvalueid, refdisplayvalue FROM refvalue where reftypeid=? and refvalueid=?";
            SafeSQL safeSQL = new SafeSQL();
            safeSQL.addVar(reftypeid);
            safeSQL.addVar(value);
            DataSet refvals = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (refvals.getRowCount() > 0 && refvals.getValue(0, "refdisplayvalue", "").length() > 0) {
                return refvals.getValue(0, "refdisplayvalue", "");
            }
        }
        return value;
    }
}

