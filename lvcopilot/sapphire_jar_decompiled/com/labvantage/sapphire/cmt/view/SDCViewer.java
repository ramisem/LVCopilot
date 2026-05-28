/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt.view;

import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.view.SDISnapshotViewer;
import com.labvantage.sapphire.modules.configreport.util.DDTLabelsUtil;
import com.labvantage.sapphire.modules.configreport.util.SDCUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;

public class SDCViewer
extends SDISnapshotViewer {
    public SDCViewer() {
    }

    public SDCViewer(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
    }

    @Override
    protected ConfigReportContent renderPrimaryDiff(SDISnapshotItem source, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns, boolean hideInheritedProperties, boolean isChild) throws SapphireException {
        SDIData srcSdiData = source.getSDIData();
        SDIData refSdiData = refItem == null ? new SDIData() : refItem.getSDIData();
        boolean diffOnly = false;
        ConfigReportContent configReportContent = new ConfigReportContent("SDC", this.translationProcessor);
        String sdiTitle = this.getFormattedItemLabel(srcSdiData, this.getSDITableLabelInfo(srcSdiData.getSdcid())[1]);
        configReportContent.startSection(sdiTitle);
        configReportContent.startSubSection("SDC", "");
        configReportContent.append(this.renderSDCSummaryDiff(srcSdiData, refSdiData).toString());
        configReportContent.appendSubSection(this.renderSDCInfoDiff(srcSdiData, refSdiData, hideEmptyColumns), "SDC", diffOnly);
        configReportContent.startSubSection("Security/Update Options", "");
        configReportContent.appendSubSection(this.renderSecurityOptionsDiff(srcSdiData, refSdiData), "Security/Update Options", diffOnly);
        configReportContent.startSubSection("Definition Options", "");
        configReportContent.appendSubSection(this.renderDefinitionOptionsDiff(srcSdiData, refSdiData), "Definition Options", diffOnly);
        configReportContent.startSubSection("SDC Relationship Model", "");
        configReportContent.appendSubSection(this.renderRelationshipsDiff(srcSdiData, refSdiData, false), "SDC Relationship Model", diffOnly);
        configReportContent.endSubSection("SDC", "");
        return configReportContent;
    }

    @Override
    protected void renderItemDetailsDiff(ConfigReportContent configReportContent, SDISnapshotItem sourceItem, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns) throws SapphireException {
        SDIData srcSdiData = sourceItem.getSDIData();
        SDIData refSdiData = refItem == null ? new SDIData() : refItem.getSDIData();
        boolean diffOnly = false;
        ConfigReportContent str = new ConfigReportContent("Tables", this.translationProcessor);
        str.appendSpecialContent(this.renderTablesInfoDiff(srcSdiData, refSdiData, hideEmptyColumns));
        configReportContent.appendNodeContent(str, "systable", "Tables");
        ConfigReportContent columns = new ConfigReportContent("Columns", this.translationProcessor);
        columns.startSubSection("Columns", "");
        columns.appendSpecialContent(this.renderColumnsInfoDiff(false, srcSdiData, refSdiData, hideEmptyColumns));
        configReportContent.appendNodeContent(columns, "syscolumn", "Columns");
        ConfigReportContent links = new ConfigReportContent("Links", this.translationProcessor);
        links.startSubSection("Links", "");
        links.appendSpecialContent(this.renderLinksInfoDiff(srcSdiData, refSdiData));
        configReportContent.appendNodeContent(links, "sdclink", "Links");
        ConfigReportContent operations = this.renderOperationsDiff(srcSdiData, refSdiData, hideEmptyColumns);
        if (operations.length() > 0) {
            configReportContent.startSubSection("Operations", "");
            configReportContent.appendNodeContent(operations, "sdcoperations", "Operations");
        }
        ConfigReportContent indexes = new ConfigReportContent("Indexes", this.translationProcessor);
        indexes.startSubSection("Indexes", "");
        indexes.appendSpecialContent(this.renderIndexInfoDiff(srcSdiData, refSdiData, hideEmptyColumns), diffOnly);
        configReportContent.appendNodeContent(indexes, "sysref", "Indexes");
        LinkedHashSet<String> datasets = new LinkedHashSet<String>();
        datasets.add("sdcexport");
        datasets.add("sdcoperation");
        super.renderItemDetailsDiff(configReportContent, sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns, datasets);
        ConfigReportContent attributes = this.renderAttrsInfoDiff(srcSdiData, refSdiData, hideEmptyColumns);
        if (attributes.length() > 0) {
            configReportContent.startSubSection("Attributes", "");
            configReportContent.appendNodeContent(attributes, "sdcattributedef", "Attributes");
        }
        str = new ConfigReportContent("other", this.translationProcessor);
        this.renderOtherCommonDetails(str, sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        configReportContent.appendSpecialContent(str);
    }

    @Override
    public String[] getIgnoreDataSets() {
        return new String[]{"sysextendedcolumn", "syscolumnproperty", "sdclink", "sdcdetaillink", "sysrefcolumn"};
    }

    public String getCurrentSDCType(SDIData sdiData) {
        String colValue = this.getPrimaryValue(sdiData, "sdctype");
        if ("C".equals(colValue)) {
            return "Core";
        }
        if ("S".equals(colValue)) {
            return "System";
        }
        if ("D".equals(colValue)) {
            return "Data";
        }
        return colValue;
    }

    public String getCurrentVersioned(SDIData sdiData) {
        String flag = this.getPrimaryValue(sdiData, "versionedflag");
        if (flag == null) {
            return "";
        }
        if ("N".equals(flag.toString())) {
            return "No";
        }
        if ("Y".equals(flag.toString())) {
            return "Yes";
        }
        return "";
    }

    public ConfigReportContent renderSDCInfoDiff(SDIData srcSDIData, SDIData refSDIData, boolean hideEmptyColumns) {
        return SDCUtil.renderSDCInfoDiff(srcSDIData, refSDIData, hideEmptyColumns, this.translationProcessor);
    }

    public ConfigReportContent renderSecurityOptionsDiff(SDIData srcSDIData, SDIData refSDIData) {
        return SDCUtil.renderSecurityOptionsDiff(srcSDIData, refSDIData, this.translationProcessor);
    }

    public ConfigReportContent renderDefinitionOptionsDiff(SDIData srcSDIData, SDIData refSDIData) {
        return SDCUtil.renderDefinitionOptionsDiff(srcSDIData, refSDIData, this.translationProcessor);
    }

    public ConfigReportContent renderTablesInfoDiff(SDIData srcSDIData, SDIData refSDIData, boolean hideEmptyColumns) throws SapphireException {
        return SDCUtil.renderTablesInfoDiff(srcSDIData, refSDIData, hideEmptyColumns, this.translationProcessor, this.sdcProcessor);
    }

    private DataSet getTableDoc(SDIData sdiData) {
        DataSet dtypeLinks = this.getDTypeTables(sdiData);
        String dTypeTables = dtypeLinks.getColumnValues("linktableid", ";");
        String cuurentTablename = this.getPrimaryValue(sdiData, "tableid");
        String tableids = dTypeTables + ";" + cuurentTablename;
        String[] tablelist = StringUtil.split(tableids, ";");
        DataSet tabledocDS = new DataSet();
        DataSet systable = sdiData.getDataset("systable");
        if (systable != null && systable.getRowCount() > 0) {
            tabledocDS.addColumn("tableid", 0);
            tabledocDS.addColumn("tabledoc", 0);
            for (int i = 0; i < tablelist.length; ++i) {
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("tableid", tablelist[i]);
                DataSet match = systable.getFilteredDataSet(filter);
                tabledocDS.addRow();
                tabledocDS.setString(i, "tableid", tablelist[i]);
                tabledocDS.setString(i, "tabledoc", match.getString(0, "tabledoc", ""));
            }
        }
        return tabledocDS;
    }

    public HashMap getTableDocHM(SDIData sdiData) {
        HashMap<String, String> tableDocHM = new HashMap<String, String>();
        DataSet tableDocDS = this.getTableDoc(sdiData);
        if (tableDocDS != null) {
            for (int i = 0; i < tableDocDS.size(); ++i) {
                tableDocHM.put(tableDocDS.getString(i, "tableid"), tableDocDS.getString(i, "tabledoc"));
            }
        }
        return tableDocHM;
    }

    public DataSet getDTypeTables(SDIData sdiData) {
        DataSet sdclinks = sdiData.getDataset("sdclink");
        if (sdclinks == null) {
            return new DataSet();
        }
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("linktype", "D");
        return sdclinks.getFilteredDataSet(filter);
    }

    public ConfigReportContent renderColumnsInfoDiff(boolean reportSDCRelationshipModelOnly, SDIData srcSDIData, SDIData refSDIData, boolean hideEmptyColumns) throws SapphireException {
        String[] keycols = new String[]{"Table", "Column Name"};
        String primarytablename = this.getPrimaryValue(srcSDIData, "tableid");
        DataSet srcPrimaryColumns = this.getColumnsInfo(srcSDIData, primarytablename);
        DataSet refPrimaryColumns = this.getColumnsInfo(refSDIData, primarytablename);
        String tablelabel = "Columns";
        String itemdisplay = "[Table],[Column Name]";
        DataSet allSrcColumns = srcPrimaryColumns;
        DataSet allRefColumns = refPrimaryColumns;
        DataSet tables = this.getDTypeTables(srcSDIData);
        for (int i = 0; i < tables.getRowCount(); ++i) {
            String table = tables.getString(i, "linktableid");
            DataSet srcTableColumnInfo = this.getDetailsColumnsInfo(srcSDIData, table);
            DataSet refTableColumnInfo = this.getDetailsColumnsInfo(refSDIData, table);
            allSrcColumns.copyRow(srcTableColumnInfo, -1, 1);
            allRefColumns.copyRow(refTableColumnInfo, -1, 1);
        }
        HashMap<String, String> columnTitleMap = DDTLabelsUtil.getColumnTitleMap(this.getSDCProcessor(), "syscolumn", allSrcColumns.getColumns());
        ConfigReportContent cols = new ConfigReportContent("Columns", this.translationProcessor);
        cols.renderDetailTablesDiff(columnTitleMap, "syscolumn", tablelabel, itemdisplay, allSrcColumns, allRefColumns, keycols, this.getTranslationProcessor(), hideEmptyColumns);
        ConfigReportContent configReportContent = new ConfigReportContent("SDC columns info:", this.translationProcessor);
        configReportContent.appendNodeContent(cols, "syscolumn", "Columns");
        return configReportContent;
    }

    public DataSet getColumnsInfo(SDIData sdiData, String table) {
        DataSet cols = sdiData.getDataset("syscolumn");
        if (cols == null) {
            return new DataSet();
        }
        if (table != null && table.length() > 0) {
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("tableid", table);
            cols = cols.getFilteredDataSet(filter);
        }
        DataSet ret = new DataSet();
        ret.setColidCaseSensitive(true);
        ret.addColumn("Table", 0);
        ret.addColumn("Column Name", 0);
        ret.addColumn("Column Label", 0);
        ret.addColumn("Data Type", 0);
        ret.addColumn("Length", 0);
        ret.addColumn("Time Zone Ind.", 0);
        ret.addColumn("Searchable", 0);
        ret.addColumn("Documentation", 0);
        ret.addColumn("Default Editor Style", 0);
        DataSet colProps = sdiData.getDataset("syscolumnproperty");
        for (int i = 0; i < cols.size(); ++i) {
            String label = cols.getString(i, "columnlabel", "");
            ret.addRow();
            if ("Y".equals(cols.getString(i, "pkflag"))) {
                ret.setString(i, "Column Name", cols.getString(i, "columnid") + " (key)");
            } else {
                ret.setString(i, "Column Name", cols.getString(i, "columnid"));
            }
            ret.setString(i, "Column Label", label);
            String datatype = cols.getString(i, "datatype");
            String val = "Character";
            if ("C".equals(datatype)) {
                val = "Character";
            } else if ("D".equals(datatype)) {
                val = "Date";
            } else if ("N".equals(datatype)) {
                val = "Numeric";
            }
            ret.setString(i, "Data Type", val);
            ret.setString(i, "Length", cols.getValue(i, "columnlength"));
            String tz = this.getColumnProp(cols, colProps, table, cols.getString(i, "columnid"), "timezoneindependent");
            val = "Y".equals(tz) ? "Yes" : "No";
            ret.setString(i, "Time Zone Ind.", val);
            String searchable = cols.getString(i, "searchableflag", "");
            val = "Y".equals(searchable) ? "Yes" : "No";
            ret.setString(i, "Searchable", val);
            ret.setString(i, "Documentation", cols.getString(i, "columndoc", ""));
            ret.setString(i, "Table", table);
            String editorstype = this.getColumnProp(cols, colProps, table, cols.getString(i, "columnid"), "editorstyleid");
            if (editorstype == null || editorstype.length() <= 0) continue;
            ret.setString(i, "Default Editor Style", editorstype);
        }
        return ret;
    }

    private String getColumnProp(DataSet cols, DataSet colProps, String table, String colid, String propertyid) {
        DataSet match;
        HashMap<String, String> findEditorStyle = new HashMap<String, String>();
        findEditorStyle.put("tableid", table);
        findEditorStyle.put("columnid", colid);
        findEditorStyle.put("propertyid", propertyid);
        if (colProps != null && (match = colProps.getFilteredDataSet(findEditorStyle)) != null && match.getRowCount() > 0) {
            return match.getString(0, "propertyvalue");
        }
        return "";
    }

    public DataSet getDetailsColumnsInfo(SDIData sdiData, String table) {
        DataSet ret = new DataSet();
        DataSet currentDetailsCols = sdiData.getDataset("syscolumn");
        if (currentDetailsCols != null) {
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("tableid", table);
            DataSet columnData = currentDetailsCols.getFilteredDataSet(filter);
            ret.setColidCaseSensitive(true);
            ret.addColumn("Table", 0);
            ret.addColumn("Column Name", 0);
            ret.addColumn("Column Label", 0);
            ret.addColumn("Data Type", 0);
            ret.addColumn("Length", 0);
            ret.addColumn("Time Zone Ind.", 0);
            ret.addColumn("Searchable", 0);
            ret.addColumn("Documentation", 0);
            for (int i = 0; i < columnData.size(); ++i) {
                String label = columnData.getString(i, "columnlabel", "");
                ret.addRow();
                if ("Y".equals(columnData.getString(i, "pkflag"))) {
                    ret.setString(i, "Column Name", columnData.getString(i, "columnid") + " (key)");
                } else {
                    ret.setString(i, "Column Name", columnData.getString(i, "columnid"));
                }
                ret.setString(i, "Column Label", label);
                String datatype = columnData.getString(i, "datatype");
                String val = "Character";
                if ("C".equals(datatype)) {
                    val = "Character";
                } else if ("D".equals(datatype)) {
                    val = "Date";
                } else if ("N".equals(datatype)) {
                    val = "Numeric";
                }
                ret.setString(i, "Data Type", val);
                ret.setString(i, "Length", columnData.getValue(i, "columnlength"));
                String tz = columnData.getString(i, "timezoneindependent", "");
                val = "Y".equals(tz) ? "Yes" : "No";
                ret.setString(i, "Time Zone Ind.", val);
                String searchable = columnData.getString(i, "searchableflag", "");
                val = "Y".equals(searchable) ? "Yes" : "No";
                ret.setString(i, "Searchable", val);
                ret.setString(i, "Documentation", columnData.getString(i, "columndoc", ""));
                ret.setString(i, "Table", table);
            }
        }
        return ret;
    }

    public ConfigReportContent renderLinksInfoDiff(SDIData srcSDIData, SDIData refSDIData) {
        ConfigReportContent configReportContent = new ConfigReportContent("SDC links:", this.translationProcessor);
        DataSet linksinfo = this.getLinksInfo(srcSDIData);
        DataSet reflinksinfo = this.getLinksInfo(refSDIData);
        String[] keycols = new String[]{"Link Id"};
        configReportContent.renderDiffListTable(linksinfo, reflinksinfo, keycols, null, this.getTranslationProcessor());
        return configReportContent;
    }

    private DataSet getLinksInfo(SDIData sdiData) {
        DataSet linksinfo = new DataSet();
        linksinfo.setColidCaseSensitive(true);
        linksinfo.addColumn("Link Id", 0);
        linksinfo.addColumn("Link Type", 0);
        linksinfo.addColumn("Link Col 1", 0);
        linksinfo.addColumn("Link Col 2", 0);
        linksinfo.addColumn("Linked SDC", 0);
        linksinfo.addColumn("Linked RefType", 0);
        linksinfo.addColumn("Link Table", 0);
        linksinfo.addColumn("Delete Rule", 0);
        DataSet links = sdiData.getDataset("sdclink");
        if (links == null || links.size() == 0) {
            return new DataSet();
        }
        for (int i = 0; i < links.size(); ++i) {
            String linktype;
            String val = linktype = links.getString(i, "linktype");
            if ("F".equals(linktype)) {
                val = "Foreign Key";
            } else {
                if ("D".equals(linktype)) {
                    val = "Detail";
                    continue;
                }
                if ("V".equals(linktype)) {
                    val = "Reference ( Validated )";
                } else if ("M".equals(linktype)) {
                    val = "Many to Many";
                }
            }
            int currrow = linksinfo.addRow();
            linksinfo.setString(currrow, "Link Id", links.getString(i, "linkid"));
            linksinfo.setString(currrow, "Link Type", val);
            linksinfo.setString(currrow, "Link Col 1", links.getString(i, "sdccolumnid"));
            linksinfo.setString(currrow, "Link Col 2", links.getString(i, "sdccolumnid2"));
            linksinfo.setString(currrow, "Linked SDC", links.getString(i, "linksdcid"));
            linksinfo.setString(currrow, "Linked RefType", links.getString(i, "reftypeid"));
            linksinfo.setString(currrow, "Link Table", links.getString(i, "tableid"));
            linksinfo.setString(currrow, "Delete Rule", links.getString(i, "deleteflag"));
        }
        return linksinfo;
    }

    public ConfigReportContent renderOperationsDiff(SDIData srcSDIData, SDIData refSDIData, boolean hideEmptyColumns) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent("SDC operations:", this.translationProcessor);
        DataSet operInfo = this.getOperationInfo(srcSDIData);
        DataSet refoperInfo = this.getOperationInfo(refSDIData);
        if (operInfo.getRowCount() > 0 || refoperInfo.getRowCount() > 0) {
            String[] keycols = new String[]{"Operation"};
            String tablelabel = "Attributes";
            String itemdisplay = "[Attribute Id]";
            configReportContent.startSubSection(tablelabel, "");
            HashMap<String, String> columnTitleMap = DDTLabelsUtil.getColumnTitleMap(this.getSDCProcessor(), "sdcoperations", operInfo.getColumns());
            configReportContent.renderDetailTablesDiff(columnTitleMap, "sdcoperations", tablelabel, itemdisplay, operInfo, refoperInfo, keycols, this.getTranslationProcessor(), hideEmptyColumns);
        }
        return configReportContent;
    }

    private DataSet getOperationInfo(SDIData sdiData) {
        DataSet operations = sdiData.getDataset("sdcoperations ");
        if (operations == null || operations.size() == 0) {
            return new DataSet();
        }
        DataSet renderopsinfo = new DataSet();
        renderopsinfo.setColidCaseSensitive(true);
        renderopsinfo.addColumn("Operation", 0);
        renderopsinfo.addColumn("Description", 0);
        for (int i = 0; i < operations.size(); ++i) {
            int currrow = renderopsinfo.addRow();
            renderopsinfo.setString(currrow, "Operation", operations.getValue(i, "operationid"));
            renderopsinfo.setString(currrow, "Description", operations.getValue(i, "operationdesc"));
        }
        return renderopsinfo;
    }

    public ConfigReportContent renderIndexInfoDiff(SDIData srcSDIData, SDIData refSDIData, boolean hideEmptyColumns) throws SapphireException {
        String[] keycols = new String[]{"Name"};
        DataSet allSrcIndices = new DataSet();
        DataSet allRefIndices = new DataSet();
        allSrcIndices.setColidCaseSensitive(true);
        allRefIndices.setColidCaseSensitive(true);
        DataSet primarySrcIndx = this.getIndexInfo(srcSDIData, this.getPrimaryValue(srcSDIData, "tableid"));
        DataSet primaryRefIdx = this.getIndexInfo(refSDIData, this.getPrimaryValue(refSDIData, "tableid"));
        allSrcIndices = primarySrcIndx;
        allRefIndices = primaryRefIdx;
        DataSet tables = this.getDTypeTables(srcSDIData);
        for (int i = 0; i < tables.getRowCount(); ++i) {
            String table = tables.getString(i, "linktableid");
            DataSet src = this.getIndexInfo(srcSDIData, table);
            DataSet ref = this.getIndexInfo(refSDIData, table);
            allSrcIndices.copyRow(src, -1, 1);
            allRefIndices.copyRow(ref, -1, 1);
        }
        String tablelabel = "Indexes";
        String itemdisplay = "[Name]";
        ConfigReportContent configReportContent = new ConfigReportContent("SDC index info:", this.translationProcessor);
        HashMap<String, String> columnTitleMap = DDTLabelsUtil.getColumnTitleMap(this.getSDCProcessor(), "sysref", allSrcIndices.getColumns());
        configReportContent.renderDetailTablesDiff(columnTitleMap, "sysref", tablelabel, itemdisplay, allSrcIndices, allRefIndices, keycols, this.getTranslationProcessor(), hideEmptyColumns);
        return configReportContent;
    }

    private DataSet getIndexInfo(SDIData sdiData, String table) {
        DataSet allindexes = this.getIndexContent(sdiData);
        HashMap<String, String> indexFilter = new HashMap<String, String>();
        indexFilter.put("tableid", table);
        DataSet indexes = allindexes.getFilteredDataSet(indexFilter);
        DataSet renderindexinfo = new DataSet();
        if (indexes != null && indexes.getRowCount() > 0) {
            renderindexinfo.setColidCaseSensitive(true);
            renderindexinfo.addColumn("Table", 0);
            renderindexinfo.addColumn("Type", 0);
            renderindexinfo.addColumn("Name", 0);
            renderindexinfo.addColumn("Columns", 0);
            int i = 0;
            while (i < indexes.size()) {
                String tableid = indexes.getValue(i, "tableid");
                String refid = indexes.getValue(i, "refid");
                String reftypeflag = indexes.getValue(i, "reftypeflag");
                int currrow = renderindexinfo.addRow();
                renderindexinfo.setString(currrow, "Name", refid);
                renderindexinfo.setString(currrow, "Type", this.getRefType(reftypeflag));
                renderindexinfo.setString(currrow, "Table", tableid);
                String cols = "";
                while (i < indexes.size() && refid.equals(indexes.getValue(i, "refid"))) {
                    if (cols.length() > 0) {
                        cols = cols + "<br>";
                    }
                    cols = cols + indexes.getValue(i, "columnid");
                    ++i;
                }
                renderindexinfo.setString(currrow, "Columns", cols);
            }
        }
        return renderindexinfo;
    }

    DataSet getIndexContent(SDIData sdiData) {
        DataSet allsysrefDS = sdiData.getDataset("sysref");
        DataSet allsysrefcolDS = sdiData.getDataset("sysrefcolumn");
        String tableid = this.getPrimaryValue(sdiData, "tableid");
        DataSet allsdclinks = sdiData.getDataset("sdclink");
        String linktableids = "";
        if (allsdclinks != null) {
            for (int i = 0; i < allsdclinks.getRowCount(); ++i) {
                if (!allsdclinks.getValue(i, "linktype").equals("D") && !allsdclinks.getValue(i, "linktype").equals("M")) continue;
                linktableids = linktableids + ";";
                linktableids = linktableids + allsdclinks.getValue(i, "linktableid");
            }
        }
        String detailinktableids = "";
        DataSet allsdcdetaillinks = sdiData.getDataset("sdcdetaillink");
        if (allsdcdetaillinks != null) {
            for (int i = 0; i < allsdcdetaillinks.getRowCount(); ++i) {
                if (!allsdcdetaillinks.getValue(i, "linktype").equals("D") && !allsdcdetaillinks.getValue(i, "linktype").equals("M")) continue;
                detailinktableids = detailinktableids + ";";
                detailinktableids = detailinktableids + allsdclinks.getValue(i, "linktableid");
            }
        }
        String allTables = tableid + linktableids + detailinktableids;
        String[] tables = StringUtil.split(allTables, ";");
        DataSet sysRef = new DataSet();
        if (allsysrefDS != null) {
            for (int i = 0; i < allsysrefDS.getRowCount(); ++i) {
                if (allsysrefDS.getValue(i, "reftypeflag").equals("F")) continue;
                String currtableid = allsysrefDS.getValue(i, "tableid");
                boolean match = false;
                for (int x = 0; x < tables.length; ++x) {
                    if (!tables[x].equals(currtableid)) continue;
                    match = true;
                    break;
                }
                if (!match) continue;
                sysRef.copyRow(allsysrefDS, i, 1);
            }
        }
        DataSet sysColRef = new DataSet();
        if (allsysrefcolDS != null) {
            for (int i = 0; i < allsysrefcolDS.getRowCount(); ++i) {
                String currrefid = allsysrefcolDS.getValue(i, "refid");
                int match = sysRef.findRow("refid", currrefid);
                if (match == -1) continue;
                sysColRef.copyRow(allsysrefcolDS, i, 1);
                sysColRef.setString(sysColRef.getRowCount() - 1, "tableid", sysRef.getValue(match, "tableid"));
                sysColRef.setString(sysColRef.getRowCount() - 1, "reftypeflag", sysRef.getValue(match, "reftypeflag"));
            }
        }
        return sysColRef;
    }

    private String getRefType(String refTypeFlag) {
        if ("P".equals(refTypeFlag)) {
            return "Primary Key Index";
        }
        if ("U".equals(refTypeFlag)) {
            return "Unique Index";
        }
        if ("I".equals(refTypeFlag)) {
            return "Non-unique Index";
        }
        return "Foreign Key";
    }

    public ConfigReportContent renderSDCSummaryDiff(SDIData srcSDIData, SDIData refSDIData) {
        ConfigReportContent configReportContent = new ConfigReportContent("SDC summary:", this.translationProcessor);
        String sdcid = this.getPrimaryValue(srcSDIData, "sdcid");
        String sdcType = this.getCurrentSDCType(srcSDIData);
        String versioned = this.getCurrentVersioned(srcSDIData);
        String keygeneration = this.getPrimaryValue(srcSDIData, "keygenerationrule");
        String summary = "";
        String refSdcType = this.getCurrentSDCType(refSDIData);
        String refVersioned = this.getCurrentVersioned(refSDIData);
        String refKeyGeneration = this.getPrimaryValue(refSDIData, "keygenerationrule");
        if (!refSdcType.equals(sdcType)) {
            sdcType = sdcType + " " + ConfigReportContent.getDeletedString(refSdcType);
        }
        summary = sdcid + " is a " + sdcType + " SDC. ";
        summary = !versioned.equals(refVersioned) ? (versioned.equals("Yes") ? (refVersioned.length() > 0 ? summary + " It is versioned " + ConfigReportContent.getDeletedString("not versioned") : summary + " It is versioned ") : (versioned.equals("No") ? (refVersioned.length() > 0 ? summary + " It is not versioned " + ConfigReportContent.getDeletedString("versioned") : summary + " It is not versioned ") : summary + "")) : (versioned.equals("Yes") ? summary + " It is versioned " : (versioned.equals("No") ? summary + " It is not versioned " : summary + ""));
        configReportContent.append("<P>");
        configReportContent.append(summary);
        configReportContent.append("<P>");
        return configReportContent;
    }

    public ConfigReportContent renderAttrsInfoDiff(SDIData srcSDIData, SDIData refSDIData, boolean hideEmptyColumns) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent("SDC attributes:", this.translationProcessor);
        DataSet attsinfo = this.getAttrsInfo(srcSDIData);
        DataSet refattrsinfo = this.getAttrsInfo(refSDIData);
        if (attsinfo.getRowCount() > 0 || refattrsinfo.getRowCount() > 0) {
            String[] keycols = new String[]{"Attribute Id"};
            String tablelabel = "Attributes";
            String itemdisplay = "[Attribute Id]";
            configReportContent.startSubSection(tablelabel, "");
            HashMap<String, String> columnTitleMap = DDTLabelsUtil.getColumnTitleMap(this.getSDCProcessor(), "sdcattributedef", attsinfo.getColumns());
            configReportContent.renderDetailTablesDiff(columnTitleMap, "sdcattributedef", tablelabel, itemdisplay, attsinfo, refattrsinfo, keycols, this.getTranslationProcessor(), hideEmptyColumns);
        }
        return configReportContent;
    }

    private DataSet getAttrsInfo(SDIData sdiData) {
        DataSet attributes = sdiData.getDataset("sdcattributedef");
        if (attributes == null || attributes.size() == 0) {
            return new DataSet();
        }
        DataSet renderattrsinfo = new DataSet();
        renderattrsinfo.setColidCaseSensitive(true);
        renderattrsinfo.addColumn("Attribute Id", 0);
        renderattrsinfo.addColumn("Title", 0);
        renderattrsinfo.addColumn("Always Add", 0);
        renderattrsinfo.addColumn("Add Count", 0);
        renderattrsinfo.addColumn("Allow Duplicates", 0);
        renderattrsinfo.addColumn("Data Type", 0);
        renderattrsinfo.addColumn("Editor Style", 0);
        renderattrsinfo.addColumn("Editor SDC ID", 0);
        renderattrsinfo.addColumn("Editor RefType", 0);
        renderattrsinfo.addColumn("Default", 0);
        renderattrsinfo.addColumn("Help", 0);
        for (int i = 0; i < attributes.size(); ++i) {
            String attributeid = attributes.getValue(i, "attributeid");
            String attributeTitle = attributes.getValue(i, "attributetitle");
            String helpText = attributes.getValue(i, "helptext");
            String allowDubFlag = attributes.getValue(i, "allowduplicatesflag");
            String alwaysAddFlag = attributes.getValue(i, "alwaysaddflag");
            String alwaysAddCount = attributes.getValue(i, "alwaysaddcount");
            String dataType = attributes.getValue(i, "datatype");
            String defaultVal = attributes.getValue(i, "defaulttextvalue");
            String editorStyle = attributes.getValue(i, "editorstyleid");
            String editSdcId = attributes.getValue(i, "editsdcid");
            String editorRefType = attributes.getValue(i, "editreftypeid");
            String defaultValue = attributes.getValue(i, "default");
            int currrow = renderattrsinfo.addRow();
            renderattrsinfo.setString(currrow, "Attribute Id", attributeid);
            renderattrsinfo.setString(currrow, "Title", attributeTitle);
            renderattrsinfo.setString(currrow, "Always Add", this.YesNo(alwaysAddFlag));
            renderattrsinfo.setString(currrow, "Add Count", alwaysAddCount);
            renderattrsinfo.setString(currrow, "Allow Duplicates", this.YesNo(allowDubFlag));
            renderattrsinfo.setString(currrow, "Data Type", this.getDateType(dataType));
            renderattrsinfo.setString(currrow, "Editor Style", editorStyle);
            renderattrsinfo.setString(currrow, "Editor SDC ID", editSdcId);
            renderattrsinfo.setString(currrow, "Editor RefType", editorRefType);
            renderattrsinfo.setString(currrow, "Default", defaultValue);
            renderattrsinfo.setString(currrow, "Help", helpText);
        }
        return renderattrsinfo;
    }

    private String getDateType(String dataTypeFlag) {
        if ("S".equals(dataTypeFlag)) {
            return "String";
        }
        if ("N".equals(dataTypeFlag)) {
            return "Number";
        }
        if ("D".equals(dataTypeFlag)) {
            return "Date";
        }
        return "Clob";
    }

    private String YesNo(String yesNoFlag) {
        if ("Y".equals(yesNoFlag)) {
            return "Yes";
        }
        return "No";
    }

    public DataSet getRefersToSDCs(SDIData sdiData) {
        DataSet allsdclink = sdiData.getDataset("sdclink");
        DataSet ret = new DataSet();
        if (allsdclink != null) {
            for (int i = 0; i < allsdclink.getRowCount(); ++i) {
                String linksdcid = this.getPrimaryValue(sdiData, "linksdcid");
                String linktype = allsdclink.getValue(i, "linktype");
                if (!linktype.equals("F") && !linktype.equals("M")) continue;
                ret.copyRow(allsdclink, i, 1);
                ret.setValue(ret.getRowCount() - 1, "sdcdesc", linksdcid);
                ret.setValue(ret.getRowCount() - 1, "tableid", "TBD");
            }
        }
        return ret;
    }

    private ConfigReportContent drawModel(SDIData srcSDIData, SDIData refSDIData, boolean configreport) {
        String col2;
        String linkcolumns;
        int i;
        String fklink;
        String color;
        int i2;
        DataSet details = this.getDTypeTables(srcSDIData);
        DataSet refersTo = this.getRefersToSDCs(srcSDIData);
        DataSet referredBy = new DataSet();
        DataSet referredByDetails = new DataSet();
        DataSet refDetails = this.getDTypeTables(refSDIData);
        DataSet refRefersTo = this.getRefersToSDCs(refSDIData);
        DataSet refReferredBy = new DataSet();
        DataSet refReferredByDetails = new DataSet();
        ConfigReportContent configReportContent = new ConfigReportContent("SDC draw model:", this.translationProcessor);
        details = configReportContent.addDiffInfo(details, refDetails, new String[]{"linktableid"});
        refersTo = configReportContent.addDiffInfo(refersTo, refRefersTo, new String[]{"linkid"});
        referredBy = configReportContent.addDiffInfo(referredBy, refReferredBy, new String[]{"linkid", "sdcid"});
        referredByDetails = configReportContent.addDiffInfo(referredByDetails, refReferredByDetails, new String[]{"detaillinkid", "sdcid"});
        if (refersTo == null) {
            refersTo = new DataSet();
        }
        StringBuffer firstcolumn = new StringBuffer();
        firstcolumn.append("<TABLE class=\"datamodel\">");
        for (i2 = 0; i2 < referredBy.getRowCount(); ++i2) {
            firstcolumn.append("<TR><TD height=\"30\"  style=\"padding:0;border-spacing: 0;\" /></TR>");
            color = "black";
            if (referredBy.getString(i2, "_status").equals("New")) {
                color = "green";
            } else if (referredBy.getString(i2, "_status").equals("Modified")) {
                color = "orange";
            } else if (referredBy.getString(i2, "_status").equals("Deleted")) {
                color = "red";
            }
            firstcolumn.append("<TR><TD height=\"30\" width=\"80\" align=\"center\"  title=\"" + referredBy.getString(i2, "sdcdesc", "TBD") + "\"  class=\"datamodelsdc\" >");
            fklink = referredBy.getString(i2, "sdcid") + "(" + referredBy.getString(i2, "tableid") + ")";
            firstcolumn.append(fklink);
            firstcolumn.append("</TD></TR>");
            firstcolumn.append("<TR><TD height=\"30\" style=\"padding:0;border-spacing: 0;\"/></TR>");
        }
        for (i2 = 0; i2 < referredByDetails.getRowCount(); ++i2) {
            firstcolumn.append("<TR><TD height=\"30\" style=\"padding:0;border-spacing: 0;\" /></TR>");
            color = "black";
            if (referredByDetails.getString(i2, "_status").equals("New")) {
                color = "green";
            } else if (referredByDetails.getString(i2, "_status").equals("Modified")) {
                color = "orange";
            } else if (referredByDetails.getString(i2, "_status").equals("Deleted")) {
                color = "red";
            }
            firstcolumn.append("<TR><TD height=\"30\" title=\"" + referredByDetails.getString(i2, "sdcdesc") + "\" class=\"datamodelsdc\">");
            fklink = referredByDetails.getString(i2, "sdcid");
            firstcolumn.append(fklink + "(" + referredByDetails.getString(i2, "linktableid") + ")");
            firstcolumn.append("</TD></TR>");
            firstcolumn.append("<TR><TD height=\"30\" style=\"padding:0;border-spacing: 0;\"/></TR>");
        }
        firstcolumn.append("</TABLE>");
        StringBuffer after1 = new StringBuffer();
        after1.append("<TABLE class=\"datamodel\">");
        for (i = 0; i < referredBy.getRowCount(); ++i) {
            after1.append("<TR>");
            after1.append("<TD height=\"30\" width=\"300\" align=\"center\" style=\"padding:0;border-spacing: 0;\">");
            if (referredBy.getString(i, "_status").equals("New")) {
                after1.append("<font color=\"green\" style=\"background-color:yellow\">");
            } else if (referredBy.getString(i, "_status").equals("Deleted")) {
                after1.append("<font color=\"red\"><strike>");
            } else if (referredBy.getString(i, "_status").equals("Modified")) {
                after1.append("<font color=\"orange\">");
            }
            if ("F".equals(referredBy.getString(i, "linktype", ""))) {
                linkcolumns = referredBy.getString(i, "sdccolumnid", "");
                col2 = referredBy.getString(i, "sdccolumnid2");
                if (col2 != null && col2.length() > 0) {
                    linkcolumns = linkcolumns + ", ";
                    linkcolumns = linkcolumns + col2;
                }
                after1.append(referredBy.getString(i, "linkid") + "(" + linkcolumns + ")");
            } else {
                after1.append(referredBy.getString(i, "linkid"));
            }
            if (referredBy.getString(i, "_status", "").equals("New")) {
                after1.append("</font>");
            } else if (referredBy.getString(i, "_status", "").equals("Deleted")) {
                after1.append("</strike></font>");
            } else if (referredBy.getString(i, "_status", "").equals("Modified")) {
                after1.append("</font>");
            }
            after1.append("</TD>");
            after1.append("</TR>");
            after1.append("<TR><TD height=\"30\" width=\"300\" style=\"padding:0;border-spacing: 0;\">");
            if (referredBy.getString(i, "linktype", "").equals("F")) {
                if (referredBy.getString(i, "_status", "").equals("New")) {
                    if (configreport) {
                        after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\greenflink_from.GIF\"/>");
                    } else {
                        after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\greenflink_from.GIF\"/>");
                    }
                } else if (referredBy.getString(i, "_status", "").equals("Deleted")) {
                    if (configreport) {
                        after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\redflink_from.GIF\"/>");
                    } else {
                        after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\redflink_from.GIF\"/>");
                    }
                } else if (referredBy.getString(i, "_status", "").equals("Modified")) {
                    if (configreport) {
                        after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\orangeflink_from.GIF\"/>");
                    } else {
                        after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\orangeflink_from.GIF\"/>");
                    }
                } else if (configreport) {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\flink_from.GIF\"/>");
                } else {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\flink_from.GIF\"/>");
                }
            } else if (referredBy.getString(i, "_status", "").equals("New")) {
                if (configreport) {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\greenmlink.GIF\"/>");
                } else {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\greenmlink.GIF\"/>");
                }
            } else if (referredBy.getString(i, "_status", "").equals("Deleted")) {
                if (configreport) {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\redmlink.GIF\"/>");
                } else {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\redmlink.GIF\"/>");
                }
            } else if (referredBy.getString(i, "_status", "").equals("Modified")) {
                if (configreport) {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\orangemlink.GIF\"/>");
                } else {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\orangemlink.GIF\"/>");
                }
            } else if (configreport) {
                after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\mlink.gif\"/>");
            } else {
                after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\mlink.gif\"/>");
            }
            after1.append("</TD></TR>");
            after1.append("<TR><TD height=\"30\" style=\"padding:0;border-spacing: 0;\" /></TR>");
        }
        for (i = 0; i < referredByDetails.getRowCount(); ++i) {
            after1.append("<TR>");
            after1.append("<TD height=\"30\" width=\"300\" align=\"center\" style=\"padding:0;border-spacing: 0;\">");
            if (referredByDetails.getString(i, "_status", "").equals("New")) {
                after1.append("<font color=\"green\"  style=\"background-color:yellow\">");
            } else if (referredByDetails.getString(i, "_status", "").equals("Deleted")) {
                after1.append("<font color=\"red\"><strike>");
            } else if (referredByDetails.getString(i, "_status", "").equals("Modified")) {
                after1.append("<font color=\"orange\">");
            }
            if (referredByDetails.getString(i, "linktype", "").equals("F")) {
                linkcolumns = referredByDetails.getString(i, "sdccolumnid", "");
                col2 = referredByDetails.getString(i, "sdccolumnid2", "");
                if (col2 != null && col2.length() > 0) {
                    linkcolumns = linkcolumns + ", ";
                    linkcolumns = linkcolumns + col2;
                }
                after1.append(referredByDetails.getString(i, "detaillinkid", "") + "(" + linkcolumns + ")");
            } else {
                after1.append(referredByDetails.getString(i, "detaillinkid", ""));
            }
            if (referredByDetails.getString(i, "_status", "").equals("New")) {
                after1.append("</font>");
            } else if (referredByDetails.getString(i, "_status", "").equals("Deleted")) {
                after1.append("</strike></font>");
            } else if (referredByDetails.getString(i, "_status", "").equals("Modified")) {
                after1.append("</font>");
            }
            after1.append("</TD>");
            after1.append("</TR>");
            after1.append("<TR><TD height=\"30\" width=\"300\" style=\"padding:0;border-spacing: 0;\">");
            if (referredByDetails.getString(i, "linktype", "").equals("F")) {
                if (referredByDetails.getString(i, "_status").equals("New")) {
                    if (configreport) {
                        after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\greenflink_from.GIF\"/>");
                    } else {
                        after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\greenflink_from.GIF\"/>");
                    }
                } else if (referredByDetails.getString(i, "_status", "").equals("Deleted")) {
                    if (configreport) {
                        after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\redflink_from.GIF\"/>");
                    } else {
                        after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\redflink_from.GIF\"/>");
                    }
                } else if (referredByDetails.getString(i, "_status", "").equals("Modified")) {
                    if (configreport) {
                        after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\orangeflink_from.GIF\"/>");
                    } else {
                        after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\orangeflink_from.GIF\"/>");
                    }
                } else if (configreport) {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\flink_from.GIF\"/>");
                } else {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\flink_from.GIF\"/>");
                }
            } else if (referredByDetails.getString(i, "_status", "").equals("New")) {
                if (configreport) {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\greenmlink.GIF\"/>");
                } else {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\greenmlink.GIF\"/>");
                }
            } else if (referredByDetails.getString(i, "_status", "").equals("Deleted")) {
                if (configreport) {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\redmlink.GIF\"/>");
                } else {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\redmlink.GIF\"/>");
                }
            } else if (referredByDetails.getString(i, "_status", "").equals("Modified")) {
                if (configreport) {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\orangemlink.GIF\"/>");
                } else {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\orangemlink.GIF\"/>");
                }
            } else if (configreport) {
                after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\mlink.gif\"/>");
            } else {
                after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\mlink.gif\"/>");
            }
            after1.append("</TD></TR>");
            after1.append("<TR><TD height=\"30\" style=\"padding:0;border-spacing: 0;\" /></TR>");
        }
        after1.append("</TABLE>");
        int referredByCount = referredBy.getRowCount() + referredByDetails.getRowCount();
        int secondcolumnlen = referredByCount > refersTo.getRowCount() ? referredByCount : refersTo.getRowCount();
        StringBuffer secondcolumn = new StringBuffer();
        int secondcolumnheight = 90 * secondcolumnlen;
        secondcolumn.append("<TABLE  width=\"250\" class=\"datamodel\">");
        secondcolumn.append("<TR><TD title=\"" + this.getPrimaryValue(srcSDIData, "sdcid") + "(" + this.getPrimaryValue(srcSDIData, "sdcdesc") + ")\" align=\"center\" height=\"" + secondcolumnheight + "\"  class=\"datamodelsdc\" >");
        secondcolumn.append(this.getPrimaryValue(srcSDIData, "sdcid"));
        secondcolumn.append("</TD></TR>");
        StringBuffer detailInfo = new StringBuffer();
        detailInfo.append("<TABLE class=\"datamodel\" >");
        for (int i3 = 0; i3 < details.getRowCount(); ++i3) {
            detailInfo.append("<TR>");
            detailInfo.append("<TD NOWRAP style=\"padding:0;border-spacing:0;white-space:nowrap\">");
            if (details.getString(i3, "_status", "").equals("New")) {
                detailInfo.append("<font color=\"green\"  style=\"background-color:yellow\">");
            } else if (details.getString(i3, "_status", "").equals("Deleted")) {
                detailInfo.append("<font color=\"red\"><strike>");
            } else if (details.getString(i3, "_status").equals("Modified")) {
                detailInfo.append("<font color=\"orange\">");
            }
            detailInfo.append(details.getString(i3, "linkid", ""));
            if (details.getString(i3, "_status").equals("New")) {
                detailInfo.append("</font>");
            } else if (details.getString(i3, "_status", "").equals("Deleted")) {
                detailInfo.append("</strike></font>");
            } else if (details.getString(i3, "_status", "").equals("Modified")) {
                detailInfo.append("</font>");
            }
            detailInfo.append("</TD>");
            detailInfo.append("<TD style=\"padding:0;border-spacing:0;\" >");
            if (configreport) {
                detailInfo.append("<IMG SRC=\"..\\images\\black_verticalline.gif\" />");
            } else {
                detailInfo.append("<IMG SRC=\"WEB-CORE\\images\\black_verticalline.gif\" />");
            }
            detailInfo.append("</TD>");
            detailInfo.append("</TR>");
            detailInfo.append("<TR>");
            detailInfo.append("<TD  height=\"30\" style=\"padding:0;border-spacing:0;\">");
            detailInfo.append("");
            detailInfo.append("</TD>");
            detailInfo.append("<TD height=\"30\" style=\"padding:0;border-spacing:0;\" >");
            if (i3 == details.getRowCount() - 1) {
                if (details.getString(i3, "_status", "").equals("Modified")) {
                    if (configreport) {
                        detailInfo.append("<IMG  SRC=\"..\\images\\orangelastdlink.GIF\" />");
                    } else {
                        detailInfo.append("<IMG  SRC=\"WEB-CORE\\images\\orangelastdlink.GIF\" />");
                    }
                } else if (details.getString(i3, "_status", "").equals("New")) {
                    if (configreport) {
                        detailInfo.append("<IMG  SRC=\"..\\images\\greenlastdlink.GIF\" />");
                    } else {
                        detailInfo.append("<IMG  SRC=\"WEB-CORE\\images\\greenlastdlink.GIF\" />");
                    }
                } else if (details.getString(i3, "_status", "").equals("Deleted")) {
                    if (configreport) {
                        detailInfo.append("<IMG  SRC=\"..\\images\\redlastdlink.GIF\" />");
                    } else {
                        detailInfo.append("<IMG  SRC=\"WEB-CORE\\images\\redlastdlink.GIF\" />");
                    }
                } else if (configreport) {
                    detailInfo.append("<IMG  SRC=\"..\\images\\lastdlink.GIF\" />");
                } else {
                    detailInfo.append("<IMG  SRC=\"WEB-CORE\\images\\lastdlink.GIF\" />");
                }
            } else if (details.getString(i3, "_status", "").equals("Modified")) {
                if (configreport) {
                    detailInfo.append("<IMG  SRC=\"..\\images\\orangedlink.GIF\" />");
                } else {
                    detailInfo.append("<IMG  SRC=\"WEB-CORE\\images\\orangedlink.GIF\" />");
                }
            } else if (details.getString(i3, "_status", "").equals("New")) {
                if (configreport) {
                    detailInfo.append("<IMG  SRC=\"..\\images\\greendlink.GIF\" />");
                } else {
                    detailInfo.append("<IMG  SRC=\"WEB-CORE\\images\\greendlink.GIF\" />");
                }
            } else if (details.getString(i3, "_status", "").equals("Deleted")) {
                if (configreport) {
                    detailInfo.append("<IMG  SRC=\"..\\images\\reddlink.GIF\" />");
                } else {
                    detailInfo.append("<IMG  SRC=\"WEB-CORE\\images\\reddlink.GIF\" />");
                }
            } else if (configreport) {
                detailInfo.append("<IMG  SRC=\"..\\images\\dlink.gif\" />");
            } else {
                detailInfo.append("<IMG  SRC=\"WEB-CORE\\images\\dlink.gif\" />");
            }
            detailInfo.append("</TD> ");
            detailInfo.append("<TD height=\"30\" class=\"datamodelsdc\">");
            detailInfo.append(details.getString(i3, "linktableid", ""));
            detailInfo.append("</TD></TR>");
        }
        detailInfo.append("</TABLE>");
        secondcolumn.append("</TABLE>");
        StringBuffer beforethird = new StringBuffer();
        beforethird.append("<TABLE  class=\"datamodel\">");
        for (int i4 = 0; i4 < refersTo.getRowCount(); ++i4) {
            beforethird.append("<TR>");
            beforethird.append("<TD height=\"30\" width=\"300\" align=\"center\" style=\"padding:0;border-spacing: 0;\">");
            if (refersTo.getString(i4, "_status", "").equals("New")) {
                beforethird.append("<font color=\"green\"  style=\"background-color:yellow\">");
            } else if (refersTo.getString(i4, "_status", "").equals("Deleted")) {
                beforethird.append("<font color=\"red\"><strike>");
            } else if (refersTo.getString(i4, "_status", "").equals("Modified")) {
                beforethird.append("<font color=\"orange\">");
            }
            if ("F".equals(refersTo.getString(i4, "linktype"))) {
                String linkcolumns2 = refersTo.getString(i4, "sdccolumnid", "");
                String col22 = refersTo.getString(i4, "sdccolumnid2", "");
                if (col22 != null && col22.length() > 0) {
                    linkcolumns2 = linkcolumns2 + ", ";
                    linkcolumns2 = linkcolumns2 + col22;
                }
                beforethird.append(refersTo.getString(i4, "linkid", "") + "(" + linkcolumns2 + ")");
            } else {
                beforethird.append(refersTo.getString(i4, "linkid", ""));
            }
            if ("New".equals(refersTo.getString(i4, "_status", ""))) {
                beforethird.append("</font>");
            } else if ("Deleted".equals(refersTo.getString(i4, "_status", ""))) {
                beforethird.append("</strike></font>");
            } else if ("Modified".equals(refersTo.getString(i4, "_status", ""))) {
                beforethird.append("</font>");
            }
            beforethird.append("</TD>");
            beforethird.append("</TR>");
            beforethird.append("<TR><TD height=\"30\" width=\"300\" style=\"padding:0;border-spacing: 0;\">");
            if ("F".equals(refersTo.getString(i4, "linktype", ""))) {
                if ("New".equals(refersTo.getString(i4, "_status", ""))) {
                    if (configreport) {
                        beforethird.append("<IMG width=\"300\" SRC=\"..\\images\\greenflink.GIF\"/>");
                    } else {
                        beforethird.append("<IMG width=\"300\" SRC=\"WEB-CORE\\images\\greenflink.GIF\"/>");
                    }
                } else if ("Deleted".equals(refersTo.getString(i4, "_status", ""))) {
                    if (configreport) {
                        beforethird.append("<IMG width=\"300\" SRC=\"..\\images\\redflink.GIF\"/>");
                    } else {
                        beforethird.append("<IMG width=\"300\" SRC=\"WEB-CORE\\images\\redflink.GIF\"/>");
                    }
                } else if ("Modified".equals(refersTo.getString(i4, "_status", ""))) {
                    if (configreport) {
                        beforethird.append("<IMG width=\"300\" SRC=\"..\\images\\orangeflink.GIF\"/>");
                    } else {
                        beforethird.append("<IMG width=\"300\" SRC=\"WEB-CORE\\images\\orangeflink.GIF\"/>");
                    }
                } else if (configreport) {
                    beforethird.append("<IMG width=\"300\" SRC=\"..\\images\\flink.GIF\"/>");
                } else {
                    beforethird.append("<IMG width=\"300\" SRC=\"WEB-CORE\\images\\flink.GIF\"/>");
                }
            } else if ("New".equals(refersTo.getString(i4, "_status", ""))) {
                if (configreport) {
                    beforethird.append("<IMG width=\"300\" SRC=\"..\\images\\greenmlink.GIF\"/>");
                } else {
                    beforethird.append("<IMG width=\"300\" SRC=\"WEB-CORE\\images\\greenmlink.GIF\"/>");
                }
            } else if ("Deleted".equals(refersTo.getString(i4, "_status", ""))) {
                if (configreport) {
                    beforethird.append("<IMG width=\"300\" SRC=\"..\\images\\redmlink.GIF\"/>");
                } else {
                    beforethird.append("<IMG width=\"300\" SRC=\"WEB-CORE\\images\\redmlink.GIF\"/>");
                }
            } else if ("Modified".equals(refersTo.getString(i4, "_status", ""))) {
                if (configreport) {
                    beforethird.append("<IMG width=\"300\" SRC=\"..\\images\\orangemlink.GIF\"/>");
                } else {
                    beforethird.append("<IMG width=\"300\" SRC=\"WEB-CORE\\images\\orangemlink.GIF\"/>");
                }
            } else if (configreport) {
                beforethird.append("<IMG width=\"300\" SRC=\"..\\images\\mlink.gif\"/>");
            } else {
                beforethird.append("<IMG width=\"300\" SRC=\"WEB-CORE\\images\\mlink.gif\"/>");
            }
            beforethird.append("</TD></TR>");
            beforethird.append("<TR><TD height=\"30\" style=\"padding:0;border-spacing: 0;\"/></TR>");
        }
        beforethird.append("</TABLE>");
        StringBuffer thirdcolumn = new StringBuffer();
        thirdcolumn.append("<TABLE  class=\"datamodel\">");
        for (int i5 = 0; i5 < refersTo.getRowCount(); ++i5) {
            thirdcolumn.append("<TR><TD height=\"30\"  style=\"padding:0;border-spacing: 0;\" /></TR>");
            String color2 = "black";
            if ("New".equals(refersTo.getString(i5, "_status", ""))) {
                color2 = "green";
            } else if ("Deleted".equals(refersTo.getString(i5, "_status", ""))) {
                color2 = "red";
            } else if ("Modified".equals(refersTo.getString(i5, "_status", ""))) {
                color2 = "orange";
            }
            thirdcolumn.append("<TR><TD height=\"30\"  width=\"80\" title=\"" + refersTo.getString(i5, "sdcdesc") + "\" class=\"datamodelsdc\" >");
            String fklink2 = refersTo.getString(i5, "linksdcid", "");
            thirdcolumn.append(fklink2);
            thirdcolumn.append("</TD></TR>");
            thirdcolumn.append("<TR><TD height=\"30\" /></TR>");
        }
        thirdcolumn.append("</TABLE>");
        configReportContent.append("<TABLE class=\"datamodel\">");
        configReportContent.append("<TR>");
        configReportContent.append("<TD valign=\"Top\" style=\"padding:0;border-spacing: 0;\">");
        configReportContent.append(firstcolumn);
        configReportContent.append("</TD>");
        configReportContent.append("<TD valign=\"Top\" style=\"padding:0;border-spacing: 0;\">");
        configReportContent.append(after1);
        configReportContent.append("</TD>");
        configReportContent.append("<TD valign=\"Top\" style=\"padding:0;border-spacing: 0;\">");
        configReportContent.append(secondcolumn);
        configReportContent.append("</TD>");
        configReportContent.append("<TD valign=\"Top\" style=\"padding:0;border-spacing: 0;\">");
        configReportContent.append(beforethird);
        configReportContent.append("</TD>");
        configReportContent.append("<TD valign=\"Top\" style=\"padding:0;border-spacing: 0;\" >");
        configReportContent.append(thirdcolumn);
        configReportContent.append("</TD>");
        configReportContent.append("</TR>");
        configReportContent.append("<TR>");
        configReportContent.append("<TD colspan=2 />");
        configReportContent.append("<TD colspan=3 valign=\"Top\" style=\"padding:0;border-spacing: 0;\" >");
        configReportContent.append(detailInfo);
        configReportContent.append("</TD>");
        configReportContent.append("</TABLE>");
        return configReportContent;
    }

    public ConfigReportContent renderRelationshipsDiff(SDIData sdcRO, SDIData refSdcRO, boolean configreport) {
        return this.drawModel(sdcRO, refSdcRO, configreport);
    }
}

