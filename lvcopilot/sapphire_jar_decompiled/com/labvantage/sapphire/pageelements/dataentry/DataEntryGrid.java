/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.dataentry;

import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.pageelements.dataentry.DataEntryColumn;
import com.labvantage.sapphire.pageelements.list.ListColumn;
import com.labvantage.sapphire.pageelements.maint.RegexConverter;
import com.labvantage.sapphire.pageelements.maint.SmartScrollGridRenderer;
import com.labvantage.sapphire.tagext.QueryData;
import com.labvantage.sapphire.tagext.SDITagUtil;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sapphire.pageelements.BaseElement;
import sapphire.tagext.SDITagInfo;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DataEntryGrid
extends BaseElement {
    @Override
    public String getHtml() {
        PropertyList rc1;
        PropertyListCollection rowColumns;
        SDITagInfo sdiInfo = this.getSDIInfo();
        if (sdiInfo == null || sdiInfo.getQueryData("dataitem") == null || sdiInfo.getQueryData("dataitem").getQuerydata() == null) {
            return "SDIDataItem data not found. DataEntryGrid must be inside an SDI tag and include dataitem in the request attribute.";
        }
        QueryData queryData = sdiInfo.getQueryData("dataitem");
        DataSet dataitems = queryData.getQuerydata();
        I18nUtil.localizeDisplayValues(dataitems, this.pageContext);
        DataSet columns = new DataSet();
        PropertyListCollection propertyListCollection = rowColumns = this.element != null ? this.element.getCollection("columns") : null;
        if (rowColumns == null) {
            rowColumns = new PropertyListCollection();
        }
        String testOrientation = this.element != null ? this.element.getProperty("testorientation", "Across") : "Across";
        PropertyList dataEntryColumn = this.element != null ? this.element.getPropertyList("dataentrycolumn") : null;
        String dataEntryTitle = null;
        String[] rowGroup = null;
        String[] rowSort = null;
        String[] colGroup = null;
        String[] colSort = null;
        if (testOrientation.equalsIgnoreCase("Across")) {
            dataEntryTitle = dataEntryColumn != null ? dataEntryColumn.getProperty("title", "[paramlistid]<br/>[paramid] ([replicateid])<br/>[paramtype]<br/>") : "[paramlistid]<br/>[paramid]<br/>[paramtype]<br/>";
            colGroup = new String[]{"paramlistid", "paramlistversionid", "variantid", "paramid", "paramtype", "replicateid"};
            colSort = new String[]{"__sdidata_usersequence", "paramlistid", "paramlistversionid", "variantid", "usersequence", "paramid", "paramtype", "replicateid"};
            rowGroup = new String[]{"keyid1", "dataset"};
            rowSort = new String[]{"keyid1", "dataset"};
            if (rowColumns.size() == 0) {
                rc1 = new PropertyList();
                rc1.setProperty("columnid", "keyid1");
                rc1.setProperty("title", "Key");
                rowColumns.add(rc1);
            }
        } else if (testOrientation.equalsIgnoreCase("Down")) {
            dataEntryTitle = dataEntryColumn != null ? dataEntryColumn.getProperty("title", "[keyid1]<br/>") : "[keyid1]<br/>";
            colGroup = new String[]{"keyid1", "dataset"};
            colSort = new String[]{"keyid1", "dataset"};
            rowGroup = new String[]{"paramlistid", "paramlistversionid", "variantid", "paramid", "paramtype", "replicateid"};
            rowSort = new String[]{"paramlistid", "paramlistversionid", "variantid", "usersequence", "paramid", "paramtype", "replicateid"};
            if (rowColumns.size() == 0) {
                rc1 = new PropertyList();
                rc1.setProperty("columnid", "paramlist");
                rc1.setProperty("title", "Param List (ds#)");
                rc1.setProperty("pseudocolumn", "[paramlistid] ([dataset])");
                rowColumns.add(rc1);
                PropertyList rc2 = new PropertyList();
                rc2.setProperty("columnid", "paramid");
                rc2.setProperty("title", "Parameter");
                rowColumns.add(rc2);
                PropertyList rc3 = new PropertyList();
                rc3.setProperty("columnid", "type");
                rc3.setProperty("title", "Type (Rep#)");
                rc3.setProperty("pseudocolumn", "[paramtype] ([replicateid])");
                rowColumns.add(rc3);
            }
        }
        DataSet grid = this.getGrid(dataitems, columns, rowGroup, rowSort, colGroup, colSort, testOrientation, sdiInfo.getSDIRequest().getPropsMatch());
        Trace.log(columns.toString());
        Trace.log(grid.toString());
        StringBuffer html = new StringBuffer();
        html.append("<script language=\"JavaScript\" src=\"WEB-CORE/elements/scripts/dataentry.js\"></script>\n");
        html.append("<script language=\"JavaScript\" src=\"WEB-CORE/scripts/grid.js\"></script>\n");
        html.append("<script language=\"JavaScript\" src=\"WEB-CORE/elements/scripts/lookup.js\"></script>\n");
        html.append("<script>\n");
        html.append("function choosecol( col ) {\n");
        html.append("\thandler1.selectColumn( col );\n");
        html.append("}\n");
        html.append("function chooserow( row ) {\n");
        html.append("\thandler1.selectRow( row );\n");
        html.append("}\n");
        html.append("function chooseall() {\n");
        html.append("\thandler1.selectAll();\n");
        html.append("}\n");
        html.append("var sapdateformat = " + RegexConverter.getSapDateFormat(this.pageContext) + ";\n");
        FormatUtil formatUtil = FormatUtil.getInstance(I18nUtil.getSessionLocale(this.pageContext));
        html.append("var decimalSeparator = \"" + formatUtil.getDecimalSeparator() + "\";\n");
        html.append("var groupingSeparator = \"" + formatUtil.getGroupingSeparator() + "\";\n");
        html.append("</script>\n");
        html.append("<textarea style=\"display:none;width:0;height:0\" id=\"clipboard\"></textarea>\n");
        SmartScrollGridRenderer scrollgrid = new SmartScrollGridRenderer(this.elementid);
        scrollgrid.setDefaultRowHeight("22");
        for (int j = 0; j < rowColumns.size(); ++j) {
            if (rowColumns.getPropertyList(j).getProperty("mode").equalsIgnoreCase("Hidden Value")) continue;
            scrollgrid.addTopLeftCell(rowColumns.getPropertyList(j).getProperty("title").length() > 0 ? rowColumns.getPropertyList(j).getProperty("title") : rowColumns.getPropertyList(j).getProperty("columnid"), "dataentry_grid_columnheader", true);
        }
        Pattern p = Pattern.compile("\\[[a-zA-Z0-9._]+\\]");
        for (int j = 0; j < columns.size(); ++j) {
            String title = dataEntryTitle;
            if (grid.size() > 0) {
                Matcher m = p.matcher(title);
                block2: while (m.find()) {
                    String match = m.group();
                    for (int k = 0; k < grid.size(); ++k) {
                        if (grid.getBigDecimal(k, columns.getString(j, "__columnid")) == null) continue;
                        int dataRow = grid.getInt(k, columns.getString(j, "__columnid"));
                        title = StringUtil.replaceAll(title, match, dataitems.getValue(dataRow, match.substring(1, match.length() - 1)));
                        title = dataEntryColumn.getProperty("translatevalue", "N").equals("N") ? title : this.getTranslationProcessor().translate(title);
                        continue block2;
                    }
                }
            }
            scrollgrid.addTopRightCell(title, "dataentry_grid_columnheader", "choosecol(" + j + ")", true);
        }
        html.append(SDITagUtil.getFixedRowInputs("dataitem", dataitems.getColumns(), dataitems.size(), ""));
        SDIData sdiData = new SDIData();
        DataEntryColumn dataColumn = new DataEntryColumn(this.pageContext, sdiInfo, this.getConnectionId());
        dataColumn.setElementProperties(this.element);
        dataColumn.setColumnProperties(dataEntryColumn);
        dataColumn.setKeyCols(sdiData.getKeys("dataitem"));
        dataColumn.setLayout("grid");
        ListColumn listColumn = new ListColumn(this.pageContext, sdiInfo);
        listColumn.setElementProperties(this.element);
        listColumn.setDatasetname("dataitem");
        for (int i = 0; i < grid.size(); ++i) {
            int j;
            int headerRow = grid.getInt(i, "__rownumber");
            listColumn.setRow(headerRow);
            for (j = 0; j < rowColumns.size(); ++j) {
                listColumn.setColumn(rowColumns.getPropertyList(j));
                listColumn.setColumnProperty("class", "dataentry_grid_rowheader" + (j == rowColumns.size() - 1 ? "last" : ""));
                listColumn.setColumnProperty("tdattributes", "nowrap");
                if ("Hidden Value".equals(rowColumns.getPropertyList(j).getProperty("mode"))) {
                    html.append(listColumn.getHtml());
                    continue;
                }
                scrollgrid.addBottomLeftCell(listColumn, j == 0);
            }
            for (j = 0; j < columns.size(); ++j) {
                dataColumn.setGridPos(i, j);
                int dataRow = grid.getInt(i, columns.getString(j, "__columnid"));
                if (dataRow >= 0 && dataRow < dataitems.size()) {
                    queryData.setCurrentRow(dataRow);
                    scrollgrid.addBottomRightCell(dataColumn.getHtml(), queryData.getValue(dataRow, "__lockedby", "").length() > 0 ? "dataentry_grid_cell_locked" : "dataentry_grid_cell", j == 0);
                    continue;
                }
                scrollgrid.addBottomRightCell("&nbsp;", "dataentry_grid_blankcell", j == 0);
            }
        }
        scrollgrid.renderGrid(html);
        html.append(SDITagUtil.getGrid(grid.size(), columns.size(), this.getTranslationProcessor()));
        html.append("<div style=\"position:absolute; display:none\" id=\"dd_div\" class=\"dropdowndiv\" onkeydown=\"dd_divKeyPress()\" onmouseover=\"this.onblur = null;\" onmouseout=\"this.onblur = dd_divBlur;\"></div>\n");
        html.append("\n<script>\n");
        html.append(this.pageContext.getAttribute("dd_dropdownvalues") != null ? this.pageContext.getAttribute("dd_dropdownvalues") : "");
        html.append("handler1.setContainingdiv( '" + this.elementid + "br_div' );");
        html.append("handler1.allowCherryPicking=true;");
        html.append("scrollgridusedom = true;");
        html.append("\n</script>\n");
        return html.toString();
    }

    private DataSet getGrid(DataSet data, DataSet columns, String[] rowGroup, String[] rowSortCols, String[] colGroup, String[] colSortCols, String testOrientation, boolean propsMatch) {
        int i;
        DataSet grid = new DataSet();
        String currentRowId = "";
        String currentColId = "";
        int gridRow = -1;
        StringBuffer rowSort = new StringBuffer();
        for (int i2 = 0; i2 < rowSortCols.length; ++i2) {
            rowSort.append((i2 > 0 ? "," : "") + rowSortCols[i2]);
        }
        StringBuffer colSort = new StringBuffer();
        for (i = 0; i < colSortCols.length; ++i) {
            colSort.append((i > 0 ? "," : "") + colSortCols[i]);
        }
        data.sort(propsMatch && testOrientation.equalsIgnoreCase("Across") ? "__rsetseq, usersequence" : rowSort.append(",").append(colSort).toString());
        for (i = 0; i < data.size(); ++i) {
            StringBuffer tempRowId = new StringBuffer();
            for (int j = 0; j < rowGroup.length; ++j) {
                tempRowId.append((j > 0 ? ";" : "") + data.getValue(i, rowGroup[j]));
            }
            if (!currentRowId.equals(tempRowId.toString())) {
                currentRowId = tempRowId.toString();
                gridRow = grid.addRow();
                grid.setNumber(gridRow, "__rownumber", i);
                if (testOrientation.equalsIgnoreCase("Down")) {
                    grid.setNumber(gridRow, "__sdidatasequence", data.getInt(i, "__sdidata_usersequence"));
                }
            }
            StringBuffer tempColId = new StringBuffer();
            for (int j = 0; j < colGroup.length; ++j) {
                tempColId.append((j > 0 ? ";" : "") + data.getValue(i, colGroup[j]));
            }
            if (!currentColId.equals(tempColId.toString()) && !grid.isValidColumn(currentColId = tempColId.toString())) {
                grid.addColumn(currentColId, 1);
                int colsRow = columns.addRow();
                columns.setString(colsRow, "__columnid", currentColId);
                columns.setNumber(colsRow, "__gridrownumber", gridRow);
                if (propsMatch) {
                    columns.setNumber(colsRow, "__rsetseq", data.getBigDecimal(i, "__rsetseq"));
                }
                block10: for (int j = 0; j < colSortCols.length; ++j) {
                    switch (data.getColumnType(colSortCols[j])) {
                        case 0: {
                            columns.setString(colsRow, colSortCols[j], data.getString(i, colSortCols[j]));
                            continue block10;
                        }
                        case 1: {
                            columns.setNumber(colsRow, colSortCols[j], data.getBigDecimal(i, colSortCols[j]));
                            continue block10;
                        }
                        case 2: {
                            columns.setDate(colsRow, colSortCols[j], data.getTimestamp(i, colSortCols[j]));
                        }
                    }
                }
            }
            grid.setObject(gridRow, currentColId, new BigDecimal(i));
        }
        columns.sort(propsMatch ? "__rsetseq" : colSort.toString());
        if (testOrientation.equalsIgnoreCase("Down")) {
            grid.sort("__sdidatasequence, __rownumber");
        }
        return grid;
    }
}

