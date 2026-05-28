/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.util;

import com.labvantage.sapphire.modules.configreport.ro.LV_DataFileDefRO;
import com.labvantage.sapphire.modules.configreport.util.DDTLabelsUtil;
import com.labvantage.sapphire.modules.datafile.ValidationEditorUtil;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.BaseSDCRenderer;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class LV_DataFileDefUtil
extends BaseSDCRenderer {
    public static final String SEPARATOR = ";";

    public ConfigReportContent renderDFDInfo(LV_DataFileDefRO dataFileDefRO, TranslationProcessor translationProcessor) {
        ConfigReportContent content = new ConfigReportContent("DataFileDef info: ", this.getTranslationProcessor());
        content.startTable();
        content.startRow();
        content.addRowItem("DataFileDefinition ID", dataFileDefRO.getDataFileDefId(), translationProcessor);
        content.addRowItem("DataFileDefinition Version", dataFileDefRO.getDataFileDefVersionId(), translationProcessor);
        content.endRow();
        content.startRow();
        content.addRowItem("Description", dataFileDefRO.getDataFileDefDesc(), translationProcessor);
        content.addRowItem("Style", dataFileDefRO.getStyle(), translationProcessor);
        content.endRow();
        content.startRow();
        content.addRowItem("Process Block Size", dataFileDefRO.getProcessBlockSize(), translationProcessor);
        content.addRowItem("Commit", dataFileDefRO.getCommitAt(), translationProcessor);
        content.endRow();
        content.startRow();
        content.addRowItem("On Failure", dataFileDefRO.getFailureAction(), translationProcessor);
        content.addRowItem("File Type", dataFileDefRO.getFileType(), translationProcessor);
        content.endRow();
        content.startRow();
        content.addRowItem("Delimiter", dataFileDefRO.getDelimiter(), translationProcessor);
        content.addRowItem("Worksheet", dataFileDefRO.getWorksheet(), translationProcessor);
        content.endRow();
        content.startRow();
        content.addRowItem("Notes", dataFileDefRO.getDelimiter(), translationProcessor);
        content.endRow();
        content.endTable();
        return content;
    }

    public ConfigReportContent renderDFDInfoDiff(LV_DataFileDefRO dataFileDefRO, LV_DataFileDefRO refDataFileDefRO, TranslationProcessor translationProcessor) {
        ConfigReportContent content = new ConfigReportContent("DataFileDef Info: ", translationProcessor);
        content.startTable();
        content.startRow();
        content.addDiffRowItem("DataFileDefinition ID", dataFileDefRO.getDataFileDefId(), refDataFileDefRO.getDataFileDefId(), translationProcessor);
        content.addDiffRowItem("DataFileDefinition Version", dataFileDefRO.getDataFileDefVersionId(), refDataFileDefRO.getDataFileDefVersionId(), translationProcessor);
        content.endRow();
        content.startRow();
        content.addDiffRowItem("Description", dataFileDefRO.getDataFileDefDesc(), refDataFileDefRO.getDataFileDefDesc(), translationProcessor);
        content.addDiffRowItem("Style", dataFileDefRO.getStyle(), refDataFileDefRO.getStyle(), translationProcessor);
        content.endRow();
        content.startRow();
        content.addDiffRowItem("Process Block Size", dataFileDefRO.getProcessBlockSize(), refDataFileDefRO.getProcessBlockSize(), translationProcessor);
        content.addDiffRowItem("Commit", dataFileDefRO.getCommitAt(), refDataFileDefRO.getCommitAt(), translationProcessor);
        content.endRow();
        content.startRow();
        content.addDiffRowItem("On Failure", dataFileDefRO.getFailureAction(), refDataFileDefRO.getFailureAction(), translationProcessor);
        content.addDiffRowItem("File Type", dataFileDefRO.getFileType(), refDataFileDefRO.getFileType(), translationProcessor);
        content.endRow();
        content.startRow();
        content.addDiffRowItem("Delimiter", dataFileDefRO.getDelimiter(), refDataFileDefRO.getDelimiter(), translationProcessor);
        content.addDiffRowItem("Worksheet", dataFileDefRO.getWorksheet(), refDataFileDefRO.getWorksheet(), translationProcessor);
        content.endRow();
        content.startRow();
        content.addDiffRowItem("Notes", dataFileDefRO.getPrimaryValue("notes"), refDataFileDefRO.getPrimaryValue("notes"), 3, translationProcessor);
        content.endRow();
        content.endTable();
        return content;
    }

    public ConfigReportContent renderExampleFile(LV_DataFileDefRO dataFileDefRO, TranslationProcessor translationProcessor) {
        ConfigReportContent configReportContent = new ConfigReportContent("DataFileDef example file: ", this.getTranslationProcessor());
        DataSet ds = dataFileDefRO.getExampleFile();
        ds.addColumn("eof", 0);
        ds.setString(0, "eof", "EOF");
        ds.padColumn("eof");
        int lastRow = ds.addRow();
        String[] cols = ds.getColumns();
        for (int i = 0; i < ds.getColumnCount(); ++i) {
            ds.setString(lastRow, cols[i], "EOF");
        }
        configReportContent.renderListTable(ds, translationProcessor);
        return configReportContent;
    }

    public ConfigReportContent renderExampleFileDiff(LV_DataFileDefRO dataFileDefRO, LV_DataFileDefRO refDataFileDefRO, TranslationProcessor translationProcessor) {
        ConfigReportContent configReportContent = new ConfigReportContent("DatafileDef example file:", translationProcessor);
        DataSet srcds = dataFileDefRO.getExampleFile();
        DataSet refds = refDataFileDefRO.getExampleFile();
        configReportContent.renderDiffListTable(srcds, refds, srcds.getColumns(), false, translationProcessor, false);
        return configReportContent;
    }

    public DataSet getFieldsSummary(PropertyListCollection fields, TranslationProcessor translationProcessor) {
        DataSet fieldSummary = new DataSet();
        fieldSummary.setColidCaseSensitive(true);
        for (int i = 0; i < fields.size(); ++i) {
            PropertyList currField = fields.getPropertyList(i);
            fieldSummary.addRow();
            fieldSummary.setString(i, "Field", ConfigReportContent.createHyperLink(currField.getProperty("fieldid"), currField.getProperty("fieldid")));
            fieldSummary.setString(i, "Title", currField.getProperty("title"));
            fieldSummary.setString(i, "Type", currField.getProperty("type"));
            fieldSummary.setString(i, "Validation Rule", ValidationEditorUtil.getValidationDescriptionHtml(currField.getProperty("validationrule"), currField.getProperty("type"), this.getTranslationProcessor()));
            fieldSummary.setString(i, "Start Column", currField.getProperty("startcolumn"));
            fieldSummary.setString(i, "Start Column Type", currField.getProperty("startcolumntype"));
            fieldSummary.setString(i, "End Column", currField.getProperty("endcolumn"));
            fieldSummary.setString(i, "End Column Type", currField.getProperty("endcolumntype"));
            fieldSummary.setString(i, "Start Row", currField.getProperty("startrow"));
            fieldSummary.setString(i, "Start Row Type", currField.getProperty("startrowtype"));
            fieldSummary.setString(i, "End Row", currField.getProperty("endrow"));
            fieldSummary.setString(i, "End Row Type", currField.getProperty("endrowtype"));
            fieldSummary.setString(i, "Target", currField.getProperty("target"));
        }
        return fieldSummary;
    }

    public ConfigReportContent renderFieldDefinitions(LV_DataFileDefRO dataFileDefRO, TranslationProcessor translationProcessor) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent("DataFileDef field definitions:", translationProcessor);
        DataSet ds = dataFileDefRO.getExampleFile();
        ds.addColumn("eof", 0);
        ds.setString(0, "eof", "EOF");
        ds.padColumn("eof");
        int lastRow = ds.addRow();
        String[] cols = ds.getColumns();
        for (int i = 0; i < ds.getColumnCount(); ++i) {
            ds.setString(lastRow, cols[i], "EOF");
        }
        String dataFileObjects = dataFileDefRO.getDataFileObjects();
        PropertyList props = new PropertyList();
        props.setPropertyList(dataFileObjects);
        PropertyListCollection fields = props.getCollectionNotNull("fields");
        configReportContent.startSubHeading("List of Fields", "");
        DataSet fieldSummary = this.getFieldsSummary(fields, translationProcessor);
        configReportContent.renderListTable(fieldSummary, translationProcessor);
        return configReportContent;
    }

    public ConfigReportContent renderDataFileDefItemsDiff(SDIData sdiData, SDIData refSDIData, boolean hideEmptyColumns) throws SapphireException {
        ConfigReportContent content = new ConfigReportContent("Child DFDs:", this.getTranslationProcessor());
        DataSet srcChildDFDProps = this.getDataFileDefItems(sdiData);
        DataSet refChildDFDProps = this.getDataFileDefItems(refSDIData);
        String[] keycols = new String[]{"Data File Definition", "Version"};
        HashMap<String, String> columnTitleMap = DDTLabelsUtil.getColumnTitleMap(this.getSDCProcessor(), "datafiledefitem", srcChildDFDProps.getColumns());
        String tablelabel = DDTLabelsUtil.getLinkTableLabel(this.getSDCProcessor(), "LV_DataFileDef", "Child Data File Def", "datafiledefitem");
        String itemdisplay = "[Data File Definition]([Version])";
        content.startSubSection(tablelabel, "");
        content.renderDetailTablesDiff(columnTitleMap, "datafiledefitem", tablelabel, itemdisplay, srcChildDFDProps, refChildDFDProps, keycols, this.getTranslationProcessor(), hideEmptyColumns);
        return content;
    }

    public DataSet getDataFileDefItems(SDIData sdiData) {
        DataSet ds = sdiData.getDataset("datafiledefitem");
        DataSet ret = new DataSet();
        ret.setColidCaseSensitive(true);
        ret.addColumn("Data File Definition", 0);
        ret.addColumn("Version", 0);
        ret.addColumn("Worksheet", 0);
        if (ds != null && ds.getRowCount() > 0) {
            ret.addColumnValues("Data File Definition", 0, ds.getColumnValues("refdatafiledefid", SEPARATOR), SEPARATOR);
            ret.addColumnValues("Version", 0, ds.getColumnValues("refdatafiledefversionid", SEPARATOR), SEPARATOR);
            ret.addColumnValues("Worksheet", 0, ds.getColumnValues("excelworksheetname", SEPARATOR), SEPARATOR);
        }
        return ret;
    }

    private PropertyList findField(String fieldid, PropertyListCollection fields) {
        for (int i = 0; i < fields.size(); ++i) {
            PropertyList currField = fields.getPropertyList(i);
            if (!currField.getProperty("fieldid").equals(fieldid)) continue;
            return currField;
        }
        return null;
    }

    public ConfigReportContent renderFieldDefinitionsDiff(LV_DataFileDefRO srcDataFileDefRO, LV_DataFileDefRO refDataFileDefRO) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent("DataFileDef field definitions: ", this.getTranslationProcessor());
        DataSet srcds = srcDataFileDefRO.getExampleFile();
        srcds.addColumn("eof", 0);
        srcds.setString(0, "eof", "EOF");
        srcds.padColumn("eof");
        int lastRow = srcds.addRow();
        String[] cols = srcds.getColumns();
        for (int i = 0; i < srcds.getColumnCount(); ++i) {
            srcds.setString(lastRow, cols[i], "EOF");
        }
        String srcDataFileObjects = srcDataFileDefRO.getDataFileObjects();
        PropertyList srcprops = new PropertyList();
        srcprops.setPropertyList(srcDataFileObjects);
        PropertyListCollection srcfields = srcprops.getCollectionNotNull("fields");
        String refDataFileObjects = refDataFileDefRO.getDataFileObjects();
        PropertyList refprops = new PropertyList();
        refprops.setPropertyList(refDataFileObjects);
        PropertyListCollection reffields = refprops.getCollectionNotNull("fields");
        configReportContent.startSubHeading("List of Fields", "");
        DataSet srcFieldSummary = this.getFieldsSummary(srcfields, this.getTranslationProcessor());
        DataSet refFieldSummary = this.getFieldsSummary(reffields, this.getTranslationProcessor());
        String[] keys = new String[]{"Field"};
        configReportContent.renderDiffListTable(srcFieldSummary, refFieldSummary, keys, true, this.getTranslationProcessor(), true);
        return configReportContent;
    }

    public ConfigReportContent renderProcessingScript(LV_DataFileDefRO ro) throws SapphireException {
        return this.renderProcessingScript("", "", ro, false);
    }

    public ConfigReportContent renderProcessingScript(String applicationRoot, String folder, LV_DataFileDefRO ro, boolean configreport) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent("DataFileDef processing script: ", this.getTranslationProcessor());
        String processingScript = ro.getProcessingScript();
        if (processingScript != null && processingScript.length() > 0) {
            configReportContent.renderProcessingScript(applicationRoot, folder, processingScript, this.getTranslationProcessor(), configreport);
        }
        return configReportContent;
    }

    public ConfigReportContent renderProcessingScriptDiff(LV_DataFileDefRO srcRO, LV_DataFileDefRO refRO) throws SapphireException {
        return this.renderProcessingScriptDiff("", "", srcRO, refRO, false);
    }

    public ConfigReportContent renderProcessingScriptDiff(String applicationRoot, String folder, LV_DataFileDefRO srcRO, LV_DataFileDefRO refRO, boolean configreport) throws SapphireException {
        ConfigReportContent buffer = new ConfigReportContent("DataFileDef processing script:", this.getTranslationProcessor());
        String srcProcessingScript = srcRO.getProcessingScript();
        String refProcessingScript = refRO.getProcessingScript();
        if ((refProcessingScript == null || refProcessingScript.length() == 0) && srcProcessingScript != null && srcProcessingScript.length() > 0) {
            buffer.append(ConfigReportContent.getNewString("New:"));
            buffer.renderProcessingScript(applicationRoot, folder, srcProcessingScript, this.getTranslationProcessor(), configreport);
        } else if ((srcProcessingScript == null || srcProcessingScript.length() == 0) && refProcessingScript != null && refProcessingScript.length() > 0) {
            buffer.append(ConfigReportContent.getDeletedString("Old:"));
            buffer.renderProcessingScript(applicationRoot, folder, refProcessingScript, this.getTranslationProcessor(), configreport);
        } else if (srcProcessingScript != null && srcProcessingScript.length() > 0 && refProcessingScript != null && refProcessingScript.length() > 0) {
            buffer.renderProcessingScriptDiff(applicationRoot, folder, srcProcessingScript, refProcessingScript, true, this.getTranslationProcessor(), configreport);
        }
        return buffer;
    }
}

