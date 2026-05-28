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

public class QueryUtil
extends BaseSDCRenderer {
    public static final String SEPARATOR = ";";

    public ConfigReportContent renderQueryInfo(SDIData sdiData) {
        return this.renderQueryInfoDiff(sdiData, sdiData);
    }

    public ConfigReportContent renderQueryInfoDiff(SDIData srcSDIData, SDIData refSDIData) {
        ConfigReportContent buffer = new ConfigReportContent("Query Info: " + srcSDIData.getDataset("primary").getString(0, "queryid"), this.getTranslationProcessor());
        buffer.startTable();
        buffer.startRow();
        buffer.addDiffRowItem("Query ID", QueryUtil.getPrimaryValue(srcSDIData, "queryid"), QueryUtil.getPrimaryValue(refSDIData, "queryid"), 3, this.getTranslationProcessor());
        buffer.endRow();
        buffer.startRow();
        buffer.addDiffRowItem("Query Label", QueryUtil.getPrimaryValue(srcSDIData, "querylabel"), QueryUtil.getPrimaryValue(refSDIData, "querylabel"), 3, this.getTranslationProcessor());
        buffer.endRow();
        buffer.startRow();
        buffer.addDiffRowItem("Description", QueryUtil.getPrimaryValue(srcSDIData, "querydesc"), QueryUtil.getPrimaryValue(refSDIData, "querydesc"), 3, this.getTranslationProcessor());
        buffer.endRow();
        buffer.startRow();
        buffer.addDiffRowItem("For SDC", ConfigReportContent.createHyperLink("SDC", QueryUtil.getPrimaryValue(srcSDIData, "basedonid"), "", "", this.sdisIncluded, this.frames), ConfigReportContent.createHyperLink("SDC", QueryUtil.getPrimaryValue(refSDIData, "basedonid"), "", "", this.sdisIncluded, this.frames), 3, this.getTranslationProcessor());
        buffer.endRow();
        buffer.startRow();
        buffer.addDiffRowItem("From Clause", QueryUtil.getPrimaryValue(srcSDIData, "fromclause"), QueryUtil.getPrimaryValue(refSDIData, "fromclause"), 3, this.getTranslationProcessor());
        buffer.endRow();
        buffer.startRow();
        buffer.addDiffRowItem("Order By Clause", QueryUtil.getPrimaryValue(srcSDIData, "orderbyclause"), QueryUtil.getPrimaryValue(refSDIData, "orderbyclause"), 3, this.getTranslationProcessor());
        buffer.endRow();
        buffer.startRow();
        buffer.addDiffRowItem("Where Clause", QueryUtil.getPrimaryValue(srcSDIData, "whereclause"), QueryUtil.getPrimaryValue(refSDIData, "whereclause"), 3, false, this.getTranslationProcessor(), false);
        buffer.endRow();
        buffer.startRow();
        buffer.addDiffRowItem("Cascaded Arg", this.getCascadedArgFlag(srcSDIData), this.getCascadedArgFlag(refSDIData));
        buffer.addDiffRowItem("Distinct", this.getDistinctFlag(srcSDIData), this.getDistinctFlag(refSDIData));
        buffer.endRow();
        buffer.startRow();
        buffer.addDiffRowItem("Union Query", QueryUtil.getPrimaryValue(srcSDIData, "unionflag"), QueryUtil.getPrimaryValue(refSDIData, "unionflag"), 3, false, this.getTranslationProcessor(), false);
        buffer.endRow();
        buffer.startRow();
        buffer.addDiffRowItem("Embed Security", this.getCascadedArgFlag(srcSDIData), this.getCascadedArgFlag(refSDIData));
        buffer.addDiffRowItem("Query Limit", this.getDistinctFlag(srcSDIData), this.getDistinctFlag(refSDIData));
        buffer.endRow();
        buffer.startRow();
        buffer.addDiffRowItem("Optimizer Hint", QueryUtil.getPrimaryValue(srcSDIData, "optimizerhint"), QueryUtil.getPrimaryValue(refSDIData, "optimizerhint"), 3, false, this.getTranslationProcessor(), false);
        buffer.endRow();
        buffer.startRow();
        buffer.addDiffRowItem("With Clause", QueryUtil.getPrimaryValue(srcSDIData, "withclause"), QueryUtil.getPrimaryValue(refSDIData, "productwithclause"), 3, false, this.getTranslationProcessor(), false);
        buffer.endRow();
        buffer.startRow();
        buffer.addDiffRowItem("Notes", QueryUtil.getPrimaryValue(srcSDIData, "notes"), QueryUtil.getPrimaryValue(refSDIData, "notes"), 3, false, this.getTranslationProcessor(), false);
        buffer.endRow();
        buffer.endTable();
        return buffer;
    }

    public ConfigReportContent[] renderQueryArgs(SDIData sdiData, boolean hideEmptyColumns) throws SapphireException {
        return this.renderQueryArgsDiff(sdiData, sdiData, hideEmptyColumns);
    }

    public ConfigReportContent[] renderQueryArgsDiff(SDIData srcSDIData, SDIData refSDIData, boolean hideEmptyColumns) throws SapphireException {
        ConfigReportContent buffer = new ConfigReportContent("Query Args: " + srcSDIData.getDataset("primary").getString(0, "queryid"), this.getTranslationProcessor());
        DataSet queryArgs = this.getQueryArgs(srcSDIData, "queryarg");
        DataSet refQueryArgs = this.getQueryArgs(refSDIData, "queryarg");
        String[] keycols = new String[]{"Argument"};
        if (queryArgs == null) {
            queryArgs = new DataSet();
        }
        if (refQueryArgs == null) {
            refQueryArgs = new DataSet();
        }
        HashMap<String, String> columnTitleMap = DDTLabelsUtil.getColumnTitleMap(this.getSDCProcessor(), "queryarg", queryArgs.getColumns());
        String tablelabel = DDTLabelsUtil.getLinkTableLabel(this.getSDCProcessor(), "Query", "Query Args", "queryarg");
        String itemdisplay = "[Argument]";
        ConfigReportContent section = new ConfigReportContent("details", this.getTranslationProcessor());
        section.renderDetailTablesDiff(columnTitleMap, "queryarg", tablelabel, itemdisplay, queryArgs, refQueryArgs, keycols, this.getTranslationProcessor(), hideEmptyColumns);
        if (section.length() > 0) {
            buffer.startSubSection(tablelabel, "");
            buffer.appendSpecialContent(section);
        }
        ConfigReportContent buffer2 = new ConfigReportContent("Query Args: " + srcSDIData.getDataset("primary").getString(0, "queryid"), this.getTranslationProcessor());
        DataSet prodqueryArgs = this.getQueryArgs(srcSDIData, "productqueryarg");
        DataSet refprodQueryArgs = this.getQueryArgs(refSDIData, "productqueryarg");
        String[] keycols2 = new String[]{"Argument"};
        if (prodqueryArgs == null) {
            prodqueryArgs = new DataSet();
        }
        if (refprodQueryArgs == null) {
            refprodQueryArgs = new DataSet();
        }
        columnTitleMap = DDTLabelsUtil.getColumnTitleMap(this.getSDCProcessor(), "productqueryarg", prodqueryArgs.getColumns());
        tablelabel = DDTLabelsUtil.getLinkTableLabel(this.getSDCProcessor(), "Query", "Product Query Args", "productqueryarg");
        itemdisplay = "[Argument]";
        section = new ConfigReportContent("details", this.getTranslationProcessor());
        section.renderDetailTablesDiff(columnTitleMap, "productqueryarg", tablelabel, itemdisplay, prodqueryArgs, refprodQueryArgs, keycols, this.getTranslationProcessor(), hideEmptyColumns);
        if (section.length() > 0) {
            buffer2.startSubSection(tablelabel, "");
            buffer2.appendSpecialContent(section);
        }
        ConfigReportContent[] two = new ConfigReportContent[]{buffer, buffer2};
        return two;
    }

    public ConfigReportContent[] renderQueryUnionDiff(SDIData srcSDIData, SDIData refSDIData, boolean hideEmptyColumns) throws SapphireException {
        ConfigReportContent buffer = new ConfigReportContent("Query Union: " + srcSDIData.getDataset("primary").getString(0, "queryid"), this.getTranslationProcessor());
        DataSet queryunion = this.getQueryUnion(srcSDIData, "queryunion");
        DataSet refqueryunion = this.getQueryUnion(refSDIData, "queryunion");
        String[] keycols = new String[]{"Query Union Number"};
        if (queryunion == null) {
            queryunion = new DataSet();
        }
        if (refqueryunion == null) {
            refqueryunion = new DataSet();
        }
        HashMap<String, String> columnTitleMap = DDTLabelsUtil.getColumnTitleMap(this.getSDCProcessor(), "queryunion", queryunion.getColumns());
        String tablelabel = DDTLabelsUtil.getLinkTableLabel(this.getSDCProcessor(), "Query", "Union Query Components", "queryunion");
        String itemdisplay = "[From Clause], [Query Union Number]";
        ConfigReportContent section = new ConfigReportContent("details", this.getTranslationProcessor());
        section.renderDetailTablesDiff(columnTitleMap, "queryunion", tablelabel, itemdisplay, queryunion, refqueryunion, keycols, this.getTranslationProcessor(), hideEmptyColumns);
        if (section.length() > 0) {
            buffer.startSubSection(tablelabel, "");
            buffer.appendSpecialContent(section);
        }
        ConfigReportContent buffer2 = new ConfigReportContent("Query Union: " + srcSDIData.getDataset("primary").getString(0, "queryid"), this.getTranslationProcessor());
        DataSet productqueryunion = this.getQueryUnion(srcSDIData, "prodqueryunion");
        DataSet refproductqueryunion = this.getQueryUnion(refSDIData, "queryunion");
        String[] keycols2 = new String[]{"Query Union Number"};
        if (productqueryunion == null) {
            productqueryunion = new DataSet();
        }
        if (refproductqueryunion == null) {
            refproductqueryunion = new DataSet();
        }
        tablelabel = DDTLabelsUtil.getLinkTableLabel(this.getSDCProcessor(), "Query", "Product Union Query Components", "productqueryunion");
        itemdisplay = "[From Clause], [Query Union Number]";
        section = new ConfigReportContent("details", this.getTranslationProcessor());
        section.renderDetailTablesDiff(columnTitleMap, "productqueryunion", tablelabel, itemdisplay, productqueryunion, refproductqueryunion, keycols, this.getTranslationProcessor(), hideEmptyColumns);
        if (section.length() > 0) {
            buffer2.startSubSection(tablelabel, "");
            buffer2.appendSpecialContent(section);
        }
        ConfigReportContent[] two = new ConfigReportContent[]{buffer, buffer2};
        return two;
    }

    public DataSet getQueryArgs(SDIData sdiData, String dsname) {
        DataSet ds = sdiData.getDataset(dsname);
        DataSet ret = new DataSet();
        ret.setColidCaseSensitive(true);
        ret.addColumn("Argument", 0);
        ret.addColumn("Title", 0);
        ret.addColumn("Mandatory", 0);
        ret.addColumn("Arg Into", 0);
        ret.addColumn("Arg Type", 0);
        ret.addColumn("Use Editor Style", 0);
        ret.addColumn("Editor Style", 0);
        ret.addColumn("Reference Type", 0);
        ret.addColumn("Arg From SDC", 0);
        ret.addColumn("Look Up URL", 0);
        ret.addColumn("Arg From SQL", 0);
        ret.addColumn("Default Value", 0);
        ret.addColumn("Allow Quotes", 0);
        if (ds != null && ds.getRowCount() > 0) {
            ret.addColumnValues("Argument", 0, ds.getColumnValues("argid", SEPARATOR), SEPARATOR);
            ret.addColumnValues("Title", 0, ds.getColumnValues("argdesc", SEPARATOR), SEPARATOR);
            ret.addColumnValues("Mandatory", 0, ds.getColumnValues("mandatoryflag", SEPARATOR), SEPARATOR);
            ret.addColumnValues("Arg Into", 0, ds.getColumnValues("arginto", SEPARATOR), SEPARATOR);
            ret.addColumnValues("Arg Type", 0, ds.getColumnValues("argtype", SEPARATOR), SEPARATOR);
            ret.addColumnValues("Use Editor Style", 0, ds.getColumnValues("useeditorstyleflag", SEPARATOR), SEPARATOR);
            ret.addColumnValues("Editor Style", 0, ds.getColumnValues("editorstyleid", SEPARATOR), SEPARATOR);
            ret.addColumnValues("Reference Type", 0, ds.getColumnValues("reftypeid", SEPARATOR), SEPARATOR);
            ret.addColumnValues("Arg From SDC", 0, ds.getColumnValues("sdcid", SEPARATOR), SEPARATOR);
            ret.addColumnValues("Look Up URL", 0, ds.getColumnValues("weblookupurl", SEPARATOR), SEPARATOR);
            ret.addColumnValues("Arg From SQL", 0, ds.getColumnValues("argdata", SEPARATOR), SEPARATOR);
            ret.addColumnValues("Default Value", 0, ds.getColumnValues("defaultvalue", SEPARATOR), SEPARATOR);
            ret.addColumnValues("Allow Quotes", 0, ds.getColumnValues("allowquotesflag", SEPARATOR), SEPARATOR);
        }
        return ret;
    }

    public DataSet getQueryUnion(SDIData sdiData, String dsname) {
        DataSet ds = sdiData.getDataset(dsname);
        DataSet ret = new DataSet();
        ret.setColidCaseSensitive(true);
        ret.addColumn("Query Union Number", 0);
        ret.addColumn("From Clause", 0);
        ret.addColumn("Where Clause", 0);
        if (ds != null && ds.getRowCount() > 0) {
            ret.addColumnValues("Query Union Number", 0, ds.getColumnValues("queryunionno", SEPARATOR), SEPARATOR);
            ret.addColumnValues("From Clause", 0, ds.getColumnValues("fromclause", SEPARATOR), SEPARATOR);
            ret.addColumnValues("Where Clause", 0, ds.getColumnValues("whereclause", SEPARATOR), SEPARATOR);
        }
        return ret;
    }

    public ConfigReportContent renderExample(SDIData sdiData) {
        ConfigReportContent buffer = new ConfigReportContent("Query example: " + sdiData.getDataset("primary").getString(0, "queryid"), this.getTranslationProcessor());
        String sql = this.getExampleString(sdiData);
        buffer.append(sql);
        return buffer;
    }

    public ConfigReportContent renderExampleDiff(SDIData srcSDIData, SDIData refSDIData) {
        ConfigReportContent configReportContent = new ConfigReportContent("Query example: " + srcSDIData.getDataset("primary").getString(0, "queryid"), this.getTranslationProcessor());
        String sql = this.getExampleString(srcSDIData);
        String refSql = this.getExampleString(refSDIData);
        configReportContent.append(ConfigReportContent.getDiffString(sql, refSql));
        return configReportContent;
    }

    public String getExampleString(SDIData sdiData) {
        String distinct = this.getDistinctFlag(sdiData);
        if (sdiData.getDataset("primary") != null) {
            String sql = "<P>SELECT * FROM " + QueryUtil.getPrimaryValue(sdiData, "fromclause").trim();
            sql = sql + "<P>WHERE " + QueryUtil.getPrimaryValue(sdiData, "whereclause").trim();
            if (QueryUtil.getPrimaryValue(sdiData, "orderbyclause").length() > 0) {
                sql = sql + "<P>ORDER BY " + QueryUtil.getPrimaryValue(sdiData, "orderbyclause").trim();
            }
            if ("Y".equals(QueryUtil.getPrimaryValue(sdiData, "unionflag"))) {
                // empty if block
            }
            return sql;
        }
        return "";
    }

    public String getArgDetails(String argTypeCode, DataSet ds, int row) {
        if ("string".equals(argTypeCode)) {
            return "Arg Type: String";
        }
        if ("number".equals(argTypeCode)) {
            return "Arg Type: Number";
        }
        if ("absreldt".equals(argTypeCode)) {
            return "Arg Type: Date";
        }
        if ("dateonly".equals(argTypeCode)) {
            return "Arg Type: Date Only";
        }
        if ("sdc".equals(argTypeCode)) {
            String details = "Arg Type: SDC ";
            String fklink = ConfigReportContent.createHyperLink("SDC", ds.getString(row, "sdcid"), "", "", this.sdisIncluded, this.frames);
            details = details + ", SDC: " + fklink;
            details = details + ", Lookup Page: " + ds.getString(row, "lookuppageid");
            return details;
        }
        if ("ddsdc".equals(argTypeCode)) {
            String details = "Arg Type: SDC Dropdown";
            String fklink = ConfigReportContent.createHyperLink("SDC", ds.getString(row, "sdcid"), "", "", this.sdisIncluded, this.frames);
            details = details + ", SDC: " + fklink;
            return details;
        }
        if ("ddsql".equals(argTypeCode)) {
            String details = "Arg Type: SQL Dropdown";
            details = details + ", SQL: " + ds.getString(row, "argdata");
            return details;
        }
        if ("sqllookup".equals(argTypeCode)) {
            String details = "Arg Type: SQL Lookup";
            details = details + ", SQL: " + ds.getString(row, "argdata");
            return details;
        }
        if ("reftype".equals(argTypeCode)) {
            String details = "Arg Type: Ref Type";
            String fklink = ConfigReportContent.createHyperLink("RefType", ds.getString(row, "reftypeid"), "", "", this.sdisIncluded, this.frames);
            details = details + ", Ref Type: " + fklink;
            return details;
        }
        return argTypeCode;
    }

    public String getCascadedArgFlag(SDIData sdiData) {
        String flag = QueryUtil.getPrimaryValue(sdiData, "cascadedargflag");
        if ("Y".equals(flag)) {
            return "Yes";
        }
        return "";
    }

    public String getDistinctFlag(SDIData sdiData) {
        String flag = QueryUtil.getPrimaryValue(sdiData, "distinctflag");
        if ("Y".equals(flag)) {
            return "Yes";
        }
        return "";
    }
}

