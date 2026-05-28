/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.aspose.cells.Cell
 *  com.aspose.cells.CellsHelper
 *  com.aspose.cells.Comment
 *  com.aspose.cells.CommentCollection
 *  com.aspose.cells.DateTime
 *  com.aspose.cells.HtmlSaveOptions
 *  com.aspose.cells.ImageOrPrintOptions
 *  com.aspose.cells.Range
 *  com.aspose.cells.SaveOptions
 *  com.aspose.cells.SparklineGroup
 *  com.aspose.cells.SparklineGroupCollection
 *  com.aspose.cells.Workbook
 *  com.aspose.cells.Worksheet
 *  com.aspose.cells.WorksheetCollection
 *  org.apache.commons.codec.binary.Base64
 *  org.jsoup.Jsoup
 *  org.jsoup.nodes.Document
 *  org.jsoup.nodes.Element
 *  org.jsoup.nodes.Node
 *  org.jsoup.select.Elements
 */
package com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem;

import com.aspose.cells.Cell;
import com.aspose.cells.CellsHelper;
import com.aspose.cells.Comment;
import com.aspose.cells.CommentCollection;
import com.aspose.cells.DateTime;
import com.aspose.cells.HtmlSaveOptions;
import com.aspose.cells.ImageOrPrintOptions;
import com.aspose.cells.Range;
import com.aspose.cells.SaveOptions;
import com.aspose.cells.SparklineGroup;
import com.aspose.cells.SparklineGroupCollection;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.aspose.cells.WorksheetCollection;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemFields;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemIncludes;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemOptions;
import com.labvantage.sapphire.pageelements.spreadsheeteditor.SpreadsheetEditorDiffer;
import com.labvantage.sapphire.pageelements.spreadsheeteditor.SpreadsheetEditorHtmlRenderer;
import com.labvantage.sapphire.pageelements.spreadsheeteditor.SpreadsheetEditorModel;
import com.labvantage.sapphire.pageelements.spreadsheeteditor.SpreadsheetEditorRendererOptions;
import com.labvantage.sapphire.services.SapphireConnection;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import sapphire.SapphireException;
import sapphire.ext.BaseWorksheetItem;
import sapphire.util.HttpUtil;
import sapphire.util.M18NUtil;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SpreadSheet
extends BaseWorksheetItem {
    @Override
    public void setupOptions(WorksheetItemOptions worksheetItemOptions) {
        PropertyList config = this.getConfig();
        PropertyList renderprops = config.getPropertyListNotNull("renderprops");
        worksheetItemOptions.setSupportsExport(true);
        worksheetItemOptions.setSupportsFields(true);
        worksheetItemOptions.setSupportsHistory(true);
        worksheetItemOptions.setSupportsHistoryDiffing(true);
        worksheetItemOptions.setDoNotCloseWithCancel(true);
        worksheetItemOptions.setEditorMaxSize(config.getProperty("openmaximized", "Y").equals("Y"));
        worksheetItemOptions.setHasExportHTML(!renderprops.getProperty("maxrowsrender").equals(renderprops.getProperty("maxrowsexport")));
    }

    @Override
    public void setupIncludes(WorksheetItemIncludes worksheetItemIncludes) {
        String spreadsheetroot = "WEB-CORE/extscripts/spreadjs14/spreadsheet/";
        String designerroot = "WEB-CORE/extscripts/spreadjs14/designer/";
        worksheetItemIncludes.addScriptInclude(spreadsheetroot + "scripts/gc.spread.sheets.all.14.2.5.min.js");
        worksheetItemIncludes.addScriptInclude(spreadsheetroot + "scripts/plugins/gc.spread.sheets.charts.14.2.5.min.js");
        worksheetItemIncludes.addScriptInclude(spreadsheetroot + "scripts/plugins/gc.spread.sheets.shapes.14.2.5.min.js");
        worksheetItemIncludes.addScriptInclude(spreadsheetroot + "scripts/plugins/gc.spread.sheets.print.14.2.5.min.js");
        worksheetItemIncludes.addScriptInclude(spreadsheetroot + "scripts/plugins/gc.spread.sheets.barcode.14.2.5.min.js");
        worksheetItemIncludes.addScriptInclude(spreadsheetroot + "scripts/plugins/gc.spread.sheets.pdf.14.2.5.min.js");
        worksheetItemIncludes.addScriptInclude(spreadsheetroot + "scripts/interop/gc.spread.excelio.14.2.5.min.js");
        worksheetItemIncludes.addScriptInclude(spreadsheetroot + "scripts/plugins/gc.spread.sheets.print.14.2.5.min.js");
        worksheetItemIncludes.addScriptInclude(spreadsheetroot + "scripts/plugins/gc.spread.sheets.tablesheet.14.2.5.min.js");
        worksheetItemIncludes.addScriptInclude(spreadsheetroot + "scripts/plugins/gc.spread.calcengine.languagepackages.14.2.5.min.js");
        worksheetItemIncludes.addStyleInclude(spreadsheetroot + "css/gc.spread.sheets.14.2.5.css");
        worksheetItemIncludes.addStyleInclude(spreadsheetroot + "css/gc.spread.sheets.excel2013lightGray.14.2.5.css");
        worksheetItemIncludes.addScriptInclude(designerroot + "scripts/gc.spread.sheets.designer.resource.en.14.2.5.min.js");
        worksheetItemIncludes.addStyleInclude(designerroot + "css/gc.spread.sheets.designer.14.2.5.min.css");
        worksheetItemIncludes.addScriptInclude("rc?command=ajax&ajaxclass=com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.SpreadsheetAjaxHandler&ajaxcommand=loadspreadsheettranslations");
        worksheetItemIncludes.addScriptInclude(designerroot + "scripts/gc.spread.sheets.designer.all.14.2.5.min.js");
        worksheetItemIncludes.addScriptInclude("WEB-CORE/elements/spreadsheeteditor/scripts/designer_config.js");
        worksheetItemIncludes.addScriptInclude("WEB-CORE/elements/spreadsheeteditor/scripts/designer_commands.js");
        worksheetItemIncludes.addScriptInclude("WEB-CORE/elements/spreadsheeteditor/scripts/keyboard_commands.js");
        worksheetItemIncludes.addScriptInclude("WEB-CORE/elements/spreadsheeteditor/scripts/spreadsheeteditor.js");
        worksheetItemIncludes.addStyleInclude("WEB-CORE/elements/spreadsheeteditor/css/spreadsheeteditor.css");
        worksheetItemIncludes.setJSObjectName("spreadsheeteditor");
    }

    @Override
    public String getContentsForEdit() {
        String contents = super.getContents();
        if (contents != null && contents.length() > 0 && contents.startsWith("{")) {
            try {
                JSONObject jso = new JSONObject(contents);
                if (jso.has("blob")) {
                    jso.remove("blob");
                    return jso.toString();
                }
            }
            catch (JSONException e) {
                Trace.logError("Failed to extract contents for edit", e);
            }
        }
        return contents;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String getHTML(String dockViewPrefix, boolean export) {
        StringBuilder out = new StringBuilder();
        try {
            int i;
            SapphireConnection sapphireConnection = this.getSapphireConnection();
            M18NUtil m18n = new M18NUtil(sapphireConnection);
            Workbook workbook = null;
            JSONArray extraSheets = null;
            Locale locale = m18n.getLocale();
            if (locale == null) {
                locale = Locale.getDefault();
            }
            if (this.hasContents()) {
                String contents = this.getContents();
                JSONObject jso = new JSONObject(contents);
                if (!jso.has("blob")) {
                    return this.getHTML_old(dockViewPrefix, export);
                }
                String base64 = jso.getString("blob");
                JSONObject extra = jso.getJSONObject("extra");
                extraSheets = extra.optJSONArray("sheets");
                base64 = base64.substring(base64.indexOf("base64") + 7);
                byte[] data = Base64.decodeBase64((String)base64);
                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                workbook = new Workbook((InputStream)bis);
                workbook.getSettings().setLocale(locale);
            } else {
                workbook = new Workbook();
                Worksheet worksheet = workbook.getWorksheets().get(0);
                worksheet.setName("Sheet1");
            }
            PropertyList config = this.getConfig();
            PropertyList renderProps = config.getPropertyListNotNull("renderprops");
            boolean showRowColumnHeaders = renderProps.getProperty("showrowcolumnheaders", config.getProperty("showrowcolumnheaders", "Y")).equals("Y");
            boolean showgridlines = renderProps.getProperty("showgridlines", "Y").equals("Y");
            boolean scalablewidths = renderProps.getProperty("scalablewidths", "N").equals("Y");
            int crossstringtype = Integer.parseInt(renderProps.getProperty("crossstringtype", "0"));
            boolean alwaysshowsheettab = renderProps.getProperty("alwaysshowsheettab", "N").equals("Y");
            boolean showCommentIndicator = !export && renderProps.getProperty("showcommentindicator", "Y").equals("Y");
            boolean showFormulaIndicator = !export && renderProps.getProperty("showformulaindicator", "Y").equals("Y");
            int maxRowsRender = Integer.parseInt(renderProps.getProperty("maxrowsrender", "1000"));
            int maxRowsExport = Integer.parseInt(renderProps.getProperty("maxrowsexport", "10000"));
            int maxRows = export ? maxRowsExport : maxRowsRender;
            WorksheetCollection worksheets = workbook.getWorksheets();
            int sheetDisplayCount = 0;
            for (i = 0; i < worksheets.getCount(); ++i) {
                JSONObject currentSheet;
                JSONObject jSONObject = currentSheet = extraSheets == null || extraSheets.optJSONObject(i) == null ? new JSONObject() : extraSheets.optJSONObject(i);
                if (currentSheet.optBoolean("hideFromRender", false)) continue;
                ++sheetDisplayCount;
            }
            for (i = 0; i < worksheets.getCount(); ++i) {
                Cell cell;
                long sheetid;
                Worksheet worksheet = worksheets.get(i);
                JSONObject currentSheet = extraSheets == null || extraSheets.optJSONObject(i) == null ? new JSONObject() : extraSheets.optJSONObject(i);
                JSONObject currentTag = currentSheet.optJSONObject("tag");
                String sheetName = currentTag == null ? worksheet.getName() : currentTag.optString("sheetname", worksheet.getName());
                long l = sheetid = currentTag == null ? (long)i : currentTag.optLong("sheetid", i);
                if (currentSheet.optBoolean("hideFromRender", false)) continue;
                HtmlSaveOptions options = new HtmlSaveOptions(12);
                options.setExportActiveWorksheetOnly(true);
                options.setExportFrameScriptsAndProperties(false);
                options.setExportPrintAreaOnly(true);
                options.setExportDocumentProperties(false);
                options.setExportWorkbookProperties(false);
                options.setExportBogusRowData(false);
                options.setPresentationPreference(true);
                options.setExportImagesAsBase64(true);
                options.setExcludeUnusedStyles(true);
                options.setExportCellCoordinate(true);
                options.setHtmlCrossStringType(crossstringtype);
                options.setExportHeadings(showRowColumnHeaders);
                options.setExportGridLines(showgridlines);
                options.setWidthScalable(scalablewidths);
                options.setExportFormula(!export);
                String uniqueid = dockViewPrefix + sheetid;
                options.setCellCssPrefix("c_" + uniqueid);
                options.setTableCssId("t_" + uniqueid);
                worksheets.setActiveSheetIndex(i);
                boolean exceededMaxRows = false;
                Range maxDisplayRange = worksheet.getCells().getMaxDisplayRange();
                String printAreaAddress = worksheet.getPageSetup().getPrintArea();
                if (maxDisplayRange == null) {
                    worksheet.getPageSetup().setPrintArea("A1:E5");
                } else if (printAreaAddress != null && printAreaAddress.length() > 0) {
                    Range temp = worksheet.getCells().createRange(printAreaAddress);
                    if (temp.getRowCount() > maxRows) {
                        Range range = worksheet.getCells().createRange(temp.getFirstRow(), temp.getFirstColumn(), maxRows, temp.getColumnCount());
                        worksheet.getPageSetup().setPrintArea(range.getAddress());
                        exceededMaxRows = true;
                    }
                } else if (maxDisplayRange.getRowCount() > maxRows) {
                    Range range = worksheet.getCells().createRange(maxDisplayRange.getFirstRow(), maxDisplayRange.getFirstColumn(), maxRows, maxDisplayRange.getColumnCount());
                    worksheet.getPageSetup().setPrintArea(range.getAddress());
                    exceededMaxRows = true;
                }
                String printArea = worksheet.getPageSetup().getPrintArea();
                Range printAreaRange = null;
                if (printArea != null && printArea.length() > 0) {
                    printAreaRange = worksheet.getCells().createRange(printArea);
                    for (int row = 0; row < printAreaRange.getRowCount(); ++row) {
                        for (int col = 0; col < printAreaRange.getColumnCount(); ++col) {
                            cell = printAreaRange.get(row, col);
                            if (cell.getStringValue().length() != 0) continue;
                            cell.setValue((Object)"");
                        }
                    }
                }
                for (Object o : worksheet.getCells()) {
                    cell = (Cell)o;
                    String formula = cell.getFormula();
                    if (formula == null || formula.length() <= 0 || cell.getNumberCategoryType() != 3 && cell.getNumberCategoryType() != 4 || cell.getValue() == null || !(cell.getValue() instanceof String)) continue;
                    Double d = Double.parseDouble(cell.getValue().toString());
                    cell.setValue((Object)d);
                }
                CommentCollection comments = worksheet.getComments();
                for (int j = 0; j < comments.getCount(); ++j) {
                    Comment comment = comments.get(j);
                    int row = comment.getRow();
                    int col = comment.getColumn();
                    worksheet.getCells().get(row, col);
                }
                SparklineGroupCollection sparklines = worksheet.getSparklineGroupCollection();
                for (int j = 0; j < sparklines.getCount(); ++j) {
                    SparklineGroup sparklineGroup = sparklines.get(j);
                    int row = sparklineGroup.getSparklineCollection().get(0).getRow();
                    int col = sparklineGroup.getSparklineCollection().get(0).getColumn();
                    Cell cell2 = worksheet.getCells().get(row, col);
                    Range mergedRange = cell2.getMergedRange();
                    if (mergedRange == null) {
                        cell2.setValue((Object)"");
                        continue;
                    }
                    mergedRange.setValue((Object)"");
                }
                try (ByteArrayOutputStream bos = new ByteArrayOutputStream();){
                    String title;
                    workbook.save((OutputStream)bos, (SaveOptions)options);
                    String html = new String(bos.toByteArray(), StandardCharsets.UTF_8);
                    Document document = Jsoup.parse((String)html);
                    Element style = document.selectFirst("style");
                    Element table = document.selectFirst("table");
                    Elements select = document.select("div[style*=rotate]");
                    for (Element element : select) {
                        element.attr("style", element.attr("style") + ";text-align:center");
                    }
                    table.attr("style", table.attr("style") + ";position:relative");
                    for (int j = 0; j < sparklines.getCount(); ++j) {
                        String image;
                        SparklineGroup sparklineGroup = sparklines.get(j);
                        ImageOrPrintOptions sparkOptions = new ImageOrPrintOptions();
                        sparkOptions.setImageType(6);
                        sparkOptions.setQuality(100);
                        try (ByteArrayOutputStream sparkBos = new ByteArrayOutputStream();){
                            sparklineGroup.getSparklineCollection().get(0).toImage((OutputStream)sparkBos, sparkOptions);
                            image = Base64.encodeBase64String((byte[])sparkBos.toByteArray());
                        }
                        int row = sparklineGroup.getSparklineCollection().get(0).getRow();
                        int col = sparklineGroup.getSparklineCollection().get(0).getColumn();
                        Elements cells = this.getExcelCoordTD(table, worksheet.getCells().get(row, col));
                        if (cells.size() <= 0) continue;
                        Element cell2 = (Element)cells.get(0);
                        cell2.html("<img src=\"data:image/png;base64," + image + "\" />");
                    }
                    if (showRowColumnHeaders && printAreaRange != null) {
                        Element firstRow = document.selectFirst("tr");
                        Elements tds = firstRow.select("td");
                        for (int j = 1; j < tds.size(); ++j) {
                            Element element = (Element)tds.get(j);
                            element.text(CellsHelper.columnIndexToName((int)(printAreaRange.getFirstColumn() + j - 1)));
                        }
                    }
                    if (showCommentIndicator) {
                        for (int j = 0; j < comments.getCount(); ++j) {
                            Comment comment = comments.get(j);
                            int row = comment.getRow();
                            int col = comment.getColumn();
                            Elements cells = this.getExcelCoordTD(table, worksheet.getCells().get(row, col));
                            if (cells.size() <= 0) continue;
                            Element td = (Element)cells.get(0);
                            td.addClass("triangleContainer");
                            Element triangle = document.createElement("div");
                            triangle.addClass("triangle triangle_tr");
                            td.appendChild((Node)triangle);
                            td.attr("title", comment.getNote());
                        }
                    }
                    if (showFormulaIndicator) {
                        for (Object o : worksheet.getCells()) {
                            Elements cells;
                            Cell cell3 = (Cell)o;
                            String formula = cell3.getFormula();
                            if (formula == null || formula.length() <= 0 || (cells = this.getExcelCoordTD(table, cell3)).size() <= 0) continue;
                            Element td = (Element)cells.get(0);
                            td.attr("x:fmla", formula);
                            title = td.attr("title");
                            td.addClass("triangleContainer");
                            Element triangle = document.createElement("div");
                            triangle.addClass("triangle triangle_tl");
                            td.appendChild((Node)triangle);
                            td.attr("title", title.length() == 0 ? formula : title + "\n\n" + formula);
                        }
                    }
                    Elements trs = table.select("tr");
                    for (int row = 0; row < trs.size(); ++row) {
                        Element tr = (Element)trs.get(row);
                        Elements tds = tr.select("td");
                        for (Element td : tds) {
                            title = td.attr("title");
                            if (title.length() <= 1000) continue;
                            td.addClass("spreadsheettooltip");
                            Element span = document.createElement("div");
                            span.addClass("spreadsheettooltiptext");
                            if (row < 3) {
                                span.attr("style", "top:100%;bottom:auto");
                            }
                            span.text(title);
                            td.appendChild((Node)span);
                            td.removeAttr("title");
                        }
                    }
                    String divid = "spreadsheet_" + uniqueid;
                    String extraDirectives = "";
                    extraDirectives = extraDirectives + (showCommentIndicator ? " showcomments=\"Y\"" : "");
                    extraDirectives = extraDirectives + (showFormulaIndicator ? " showformulae=\"Y\"" : "");
                    html = "<div style=\"border:1px solid grey\" id=\"" + divid + "\" " + extraDirectives + ">";
                    html = html + style.toString();
                    html = html + table.toString();
                    if (exceededMaxRows) {
                        html = html + "<div style=\"color:red\"> " + this.getTranslationProcessor().translate("WARNING: Max Rows Exceeded") + "</div>";
                    }
                    html = html + "</div>";
                    if (sheetDisplayCount > 1 || alwaysshowsheettab) {
                        html = html + "<span style=\"position: relative;border:1px solid grey;border-top: 0;padding: 1px 8px 3px 8px;font-weight:bold\">" + sheetName + "</span><br><br>";
                    }
                    out.append(html);
                    continue;
                }
            }
        }
        catch (Exception e) {
            out.append("Failed to get spreadsheet html. Reason: " + e.getMessage());
        }
        return out.toString();
    }

    private Locale getWorkbookLocale(JSONObject extra) throws SapphireException {
        Locale locale = null;
        String localeval = extra.optString("locale");
        if (localeval.length() > 0) {
            try {
                locale = new Locale(localeval.contains("_") ? localeval.split("_")[0] : localeval);
            }
            catch (Exception exception) {}
        } else {
            SapphireConnection sapphireConnection = this.getSapphireConnection();
            M18NUtil m18n = new M18NUtil(sapphireConnection);
            locale = m18n.getLocale();
        }
        if (locale == null) {
            locale = Locale.getDefault();
        }
        return locale;
    }

    private Elements getExcelCoordTD(Element table, Cell cell) {
        Range mergedRange = cell.getMergedRange();
        String coord = "";
        coord = mergedRange == null ? cell.getName() : mergedRange.getAddress();
        Elements td = table.getElementsByAttributeValue("excelCoordinate", coord);
        if (td.size() == 0) {
            td = table.getElementsByAttributeValue("excelcoordinate", coord);
        }
        return td;
    }

    @Override
    public String getDiffHTML(String contentCurrent, String contentPrior, String prefix) throws SapphireException {
        try {
            SpreadsheetEditorRendererOptions options = new SpreadsheetEditorRendererOptions();
            options.showRowColumnHeader = true;
            options.embedImages = false;
            PropertyList config = this.getConfig();
            PropertyList diffing = config.getPropertyListNotNull("diffing");
            boolean showhiddensheets = diffing.getProperty("showhiddensheets").equals("Y");
            JSONObject currentJSO = new JSONObject(contentCurrent);
            JSONObject priorJSO = new JSONObject(contentPrior);
            if (priorJSO.has("blob") && currentJSO.has("blob")) {
                StringBuilder out = new StringBuilder();
                JSONObject currentExtra = currentJSO.getJSONObject("extra");
                JSONArray currentExtraSheets = currentExtra.optJSONArray("sheets");
                for (int i = 0; i < currentExtraSheets.length(); ++i) {
                    JSONArray deletedRows;
                    JSONArray insertedRows;
                    JSONObject currentSheet = currentExtraSheets.optJSONObject(i);
                    JSONObject currentTag = currentSheet.optJSONObject("tag");
                    String sheetName = currentTag == null ? "unknown" : currentTag.optString("sheetname", "unknown");
                    long sheetid = currentTag == null ? (long)i : currentTag.optLong("sheetid", i);
                    boolean hideFromRender = currentSheet.optBoolean("hideFromRender", false);
                    if (hideFromRender && !showhiddensheets) continue;
                    out.append("<div style=\"font-weight:bold" + (i > 0 ? ";margin-top:5px" : "") + "\">" + sheetName + (hideFromRender ? " (Hidden)" : "") + "</div>");
                    boolean hasChanges = false;
                    JSONArray dirtyCells = currentSheet.optJSONArray("dirtycells");
                    if (dirtyCells != null && dirtyCells.length() > 0) {
                        out.append("<div style=\"margin-left:5px;font-weight:bold\">Value Change</div>");
                        out.append("<table style=\"margin-left:10px\">");
                        for (int j = 0; j < dirtyCells.length(); ++j) {
                            JSONObject dirtyCell = dirtyCells.getJSONObject(j);
                            int row = dirtyCell.optInt("row");
                            int col = dirtyCell.optInt("col");
                            String coord = SpreadsheetEditorHtmlRenderer.getExcelColumnName(col + 1) + (row + 1);
                            String oldValue = dirtyCell.optString("oldValue", "(empty)");
                            String newValue = dirtyCell.optString("newValue", "(empty)");
                            out.append("<tr style=\"border-botton:1px solid darkgray\">");
                            out.append("<td onmouseover=\"var formula=dockSpreadsheet_MouseOverCell( 'spreadsheet_" + prefix + sheetid + "', '" + coord + "');this.title=formula?formula:''\" onmouseout=\"dockSpreadsheet_MouseOutCell( 'spreadsheet_" + prefix + sheetid + "', '" + coord + "')\">");
                            out.append(coord + ": " + oldValue + " \u27a1 " + newValue);
                            out.append("</td></tr>");
                            hasChanges = true;
                        }
                        out.append("</table>");
                        out.append("</div>");
                    }
                    if ((insertedRows = currentSheet.optJSONArray("insertedrows")) != null && insertedRows.length() > 0) {
                        out.append("<div style=\"margin-left:5px;font-weight:bold\">Inserted Rows</div>");
                        out.append("<table style=\"margin-left:10px\">");
                        for (int j = 0; j < insertedRows.length(); ++j) {
                            JSONObject insertedRow = insertedRows.getJSONObject(j);
                            int row = insertedRow.optInt("row", -1);
                            out.append("<tr style=\"border-botton:1px solid darkgray\">");
                            out.append("<td onmouseover=\"dockSpreadsheet_MouseOverRow( 'spreadsheet_" + prefix + sheetid + "', " + row + ")\" onmouseout=\"dockSpreadsheet_MouseOutRow( 'spreadsheet_" + prefix + sheetid + "', " + row + ")\">");
                            out.append("Row " + (row + 1) + " inserted");
                            out.append("</td></tr>");
                            hasChanges = true;
                        }
                        out.append("</table>");
                        out.append("</div>");
                    }
                    if ((deletedRows = currentSheet.optJSONArray("deletedrows")) != null && deletedRows.length() > 0) {
                        out.append("<div style=\"margin-left:5px;font-weight:bold\">Deleted Rows</div>");
                        out.append("<table style=\"margin-left:10px\">");
                        for (int j = 0; j < deletedRows.length(); ++j) {
                            JSONObject deletedRow = deletedRows.getJSONObject(j);
                            int row = deletedRow.optInt("row", -1);
                            out.append("<tr style=\"border-botton:1px solid darkgray\">");
                            out.append("<td onmouseover=\"dockSpreadsheet_MouseOver()\" onmouseout=\"dockSpreadsheet_MouseOut()\">");
                            out.append("Old row " + (row + 1) + " deleted");
                            out.append("</td></tr>");
                            hasChanges = true;
                        }
                        out.append("</table>");
                        out.append("</div>");
                    }
                    if (hasChanges) continue;
                    out.append("<span style=\"margin-left:5px\">" + this.getTranslationProcessor().translate("No changes detected.") + "</span>");
                }
                return out.toString();
            }
            if (!priorJSO.has("blob") && !currentJSO.has("blob")) {
                try {
                    SpreadsheetEditorModel model = new SpreadsheetEditorModel(contentCurrent, this.getSapphireConnection());
                    SpreadsheetEditorModel modelprior = new SpreadsheetEditorModel(contentPrior, this.getSapphireConnection());
                    SpreadsheetEditorDiffer differ = new SpreadsheetEditorDiffer(model, modelprior, prefix, this.getTranslationProcessor());
                    differ.setId(this.getWorksheetItemId());
                    return differ.getDiffHTML();
                }
                catch (Exception e) {
                    Trace.logError("Failed to generate diff: " + e.getMessage(), e);
                    return "Unable to generate Diff information.<br>Please contact your administrator for help.";
                }
            }
            return "Spreadsheet version change. Unable to generate Diff.";
        }
        catch (Exception e) {
            throw new SapphireException("Failed to generate diff.", e);
        }
    }

    @Override
    public String getViewHTML() throws SapphireException {
        return this.getHTML("", false);
    }

    @Override
    public String getExportHTML(PropertyList exportOptions) throws SapphireException {
        return this.getHTML("", true);
    }

    @Override
    public String getEditorHTML() throws SapphireException {
        boolean view = false;
        StringBuilder html = new StringBuilder();
        PropertyList config = this.getConfig();
        String id = this.getElementId();
        if (id.length() > 0) {
            String container = "spreadsheeteditor_container_" + id;
            String spreadsheetid = "spreadsheeteditor_spreadsheetid_" + id;
            String designerid = "spreadsheeteditor_designer_" + id;
            int height = 400;
            html.append("<div style=\"position:relative;border:1px solid gray;width:100%;height:" + height + "px\" id=\"" + container + "\">");
            html.append("<div id=\"" + spreadsheetid + "\" style=\"width: 100%;height:100%\"></div>\n");
            html.append("<div id=\"" + designerid + "\" style=\"width: 100%;height:100%\"></div>\n");
            html.append("</div>");
            SapphireConnection sapphireConnection = this.getSapphireConnection();
            M18NUtil m18n = new M18NUtil(sapphireConnection);
            html.append("<script>");
            html.append(" spreadsheeteditor.setUserName( '" + SafeHTML.encodeForJavaScript(sapphireConnection.getSysuserName()) + "' );");
            html.append(" spreadsheeteditor.setViewOnly( " + view + " );");
            Locale locale = m18n.getLocale();
            if (locale == null) {
                locale = Locale.getDefault();
            }
            JSONObject culture = new JSONObject();
            try {
                DateFormatSymbols datefs = new DateFormatSymbols(locale);
                JSONArray shortWeekdays = this.getJsonArray(datefs.getShortWeekdays());
                JSONArray weekdays = this.getJsonArray(datefs.getWeekdays());
                JSONArray shortMonths = this.getJsonArray(datefs.getShortMonths());
                JSONArray months = this.getJsonArray(datefs.getMonths());
                try {
                    culture.put("currencySymbol", Currency.getInstance(locale).getSymbol());
                }
                catch (Exception e) {
                    culture.put("currencySymbol", "$");
                }
                DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(locale);
                culture.put("numberDecimalSeparator", "" + decimalFormatSymbols.getDecimalSeparator());
                culture.put("numberGroupSeparator", "" + decimalFormatSymbols.getGroupingSeparator());
                culture.put("arrayGroupSeparator", ";");
                culture.put("arrayListSeparator", "\\\\");
                culture.put("listSeparator", ";");
                culture.put("amDesignator", "");
                culture.put("pmDesignator", "");
                String shortDatePattern = ((SimpleDateFormat)DateFormat.getDateInstance(3, locale)).toPattern();
                String longDatePattern = ((SimpleDateFormat)DateFormat.getDateInstance(1, locale)).toPattern();
                String fullDateTimePattern = ((SimpleDateFormat)DateFormat.getDateTimeInstance(1, 1, locale)).toPattern();
                String longTimePattern = ((SimpleDateFormat)DateFormat.getTimeInstance(1, locale)).toPattern();
                String shortTimePattern = ((SimpleDateFormat)DateFormat.getTimeInstance(3, locale)).toPattern();
                culture.put("abbreviatedMonthNames", shortMonths);
                culture.put("abbreviatedDayNames", shortWeekdays);
                culture.put("abbreviatedMonthGenitiveNames", shortMonths);
                culture.put("dayNames", weekdays);
                culture.put("fullDateTimePattern", fullDateTimePattern);
                culture.put("longDatePattern", longDatePattern);
                culture.put("longTimePattern", longTimePattern);
                culture.put("monthDayPattern", shortDatePattern);
                culture.put("monthNames", months);
                culture.put("monthGenitiveNames", months);
                culture.put("shortDatePattern", shortDatePattern);
                culture.put("shortTimePattern", shortTimePattern);
                culture.put("yearMonthPattern", shortDatePattern);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            html.append(" spreadsheeteditor.setCultureSettings( '" + HttpUtil.encodeURIComponent(culture.toString().replaceAll("'", "\\%27")) + "' );");
            html.append(" spreadsheeteditor.setLocale( '" + "" + locale + "' );");
            PropertyList toolbarSettings = config.getPropertyListNotNull("toolbarsettings");
            html.append(" spreadsheeteditor.setToolbarSettings( '" + HttpUtil.encodeURIComponent(toolbarSettings.toJSONString()) + "' );");
            PropertyList defaultSettings = config.getPropertyListNotNull("defaultsettings");
            defaultSettings.setProperty("rowcount", defaultSettings.getProperty("rowcount", "100"));
            defaultSettings.setProperty("columncount", defaultSettings.getProperty("columncount", "20"));
            defaultSettings.setProperty("tabstripvisible", defaultSettings.getProperty("tabstripvisible", "Y"));
            html.append(" spreadsheeteditor.setDefaultSettings( '" + HttpUtil.encodeURIComponent(defaultSettings.toJSONString()) + "' );");
            html.append("</script>");
        } else {
            html.append("<font style=\"color:red\">\"No ElementId provided.\"</font>");
        }
        return html.toString();
    }

    private JSONArray getJsonArray(String[] values) {
        JSONArray array = new JSONArray();
        for (int i = 0; i < values.length; ++i) {
            if (values[i] == null || values[i].length() <= 0) continue;
            array.put(StringUtil.initCaps(values[i]));
        }
        return array;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String getLiveIndexingText() {
        if (this.hasContents()) {
            StringBuilder out = new StringBuilder();
            try {
                String contents = this.getContents();
                JSONObject jso = new JSONObject(contents);
                if (!jso.has("blob")) {
                    return this.getLiveIndexingText_old();
                }
                JSONObject extra = jso.getJSONObject("extra");
                JSONArray extraSheets = extra.optJSONArray("sheets");
                String base64 = jso.getString("blob");
                base64 = base64.substring(base64.indexOf("base64") + 7);
                byte[] data = Base64.decodeBase64((String)base64);
                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                Workbook workbook = new Workbook((InputStream)bis);
                WorksheetCollection worksheets = workbook.getWorksheets();
                PropertyList config = this.getConfig();
                PropertyList renderProps = config.getPropertyListNotNull("renderprops");
                int maxRows = Integer.parseInt(renderProps.getProperty("maxrowsrender", "1000"));
                for (int i = 0; i < worksheets.getCount(); ++i) {
                    Worksheet worksheet = worksheets.get(i);
                    JSONObject currentSheet = extraSheets == null || extraSheets.optJSONObject(i) == null ? new JSONObject() : extraSheets.optJSONObject(i);
                    JSONObject currentTag = currentSheet.optJSONObject("tag");
                    if (currentSheet.optBoolean("hideFromRender", false)) continue;
                    String sheetName = currentTag == null ? worksheet.getName() : currentTag.optString("sheetname", worksheet.getName());
                    out.append(" " + sheetName);
                    HtmlSaveOptions options = new HtmlSaveOptions(12);
                    options.setExportActiveWorksheetOnly(true);
                    options.setExportFrameScriptsAndProperties(false);
                    options.setExportPrintAreaOnly(true);
                    options.setExportDocumentProperties(false);
                    options.setExportWorkbookProperties(false);
                    options.setExportBogusRowData(false);
                    options.setPresentationPreference(false);
                    options.setExportImagesAsBase64(false);
                    options.setExcludeUnusedStyles(false);
                    options.setExportHeadings(false);
                    options.setHtmlCrossStringType(2);
                    options.setExportHeadings(false);
                    options.setExportGridLines(false);
                    options.setWidthScalable(false);
                    options.setExportFormula(false);
                    options.setExportCellCoordinate(false);
                    worksheets.setActiveSheetIndex(i);
                    Range maxDisplayRange = worksheet.getCells().getMaxDisplayRange();
                    if (maxDisplayRange == null) {
                        worksheet.getPageSetup().setPrintArea("A1:E5");
                    } else if (maxDisplayRange.getRowCount() > maxRows) {
                        String printAreaAddress = worksheet.getPageSetup().getPrintArea();
                        if (printAreaAddress == null || printAreaAddress.length() == 0) {
                            Range range = worksheet.getCells().createRange(maxDisplayRange.getFirstRow(), maxDisplayRange.getFirstColumn(), maxRows, maxDisplayRange.getColumnCount());
                            worksheet.getPageSetup().setPrintArea(range.getAddress());
                        } else {
                            Range temp = worksheet.getCells().createRange(printAreaAddress);
                            if (temp.getRowCount() > maxRows) {
                                Range range = worksheet.getCells().createRange(temp.getFirstRow(), temp.getFirstColumn(), maxRows, temp.getColumnCount());
                                worksheet.getPageSetup().setPrintArea(range.getAddress());
                            }
                        }
                    }
                    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();){
                        workbook.save((OutputStream)bos, (SaveOptions)options);
                        String html = new String(bos.toByteArray(), StandardCharsets.UTF_8);
                        Document doc = Jsoup.parse((String)html);
                        out.append(" " + doc.body().text());
                        CommentCollection comments = worksheet.getComments();
                        for (int j = 0; j < comments.getCount(); ++j) {
                            Comment comment = comments.get(j);
                            out.append(" " + comment.getNote());
                        }
                        continue;
                    }
                }
            }
            catch (Exception e) {
                this.logWarn("Failed to fully index contents of Spreadsheet worksheetitem " + this.getWorksheetItemId());
            }
            return out.toString();
        }
        return "";
    }

    @Override
    public String getDockViewHTML(String prefix) throws SapphireException {
        return this.getHTML(prefix, false);
    }

    @Override
    public String validateContents(String contents) throws SapphireException {
        try {
            JSONObject jso = new JSONObject(contents);
            String base64 = jso.getString("blob");
            base64 = base64.substring(base64.indexOf("base64") + 7);
            byte[] data = Base64.decodeBase64((String)base64);
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            Workbook workbook = new Workbook((InputStream)bis);
            WorksheetCollection worksheets = workbook.getWorksheets();
            WorksheetItemFields worksheetItemFields = this.getWorksheetItemFields();
            HashSet<String> found = new HashSet<String>();
            Range[] namedRanges = worksheets.getNamedRanges();
            if (namedRanges != null) {
                for (int i = 0; i < namedRanges.length; ++i) {
                    Range namedRange = namedRanges[i];
                    if (namedRange.getRowCount() != 1 || namedRange.getColumnCount() != 1) continue;
                    String fieldid = namedRange.getName();
                    found.add(fieldid);
                    Cell cell = namedRange.getCellOrNull(0, 0);
                    int type = cell.getType();
                    if (!worksheetItemFields.contains(fieldid)) {
                        if (type == 1) {
                            worksheetItemFields.addField(fieldid, fieldid, "date", 1, null);
                        } else if (type == 4) {
                            worksheetItemFields.addField(fieldid, fieldid, "number", 1, null);
                        } else {
                            worksheetItemFields.addField(fieldid, fieldid, "string", 1, null);
                        }
                    }
                    if (type == 1) {
                        worksheetItemFields.updateFieldDatatype(fieldid, "date");
                        DateTime dateTimeValue = cell.getDateTimeValue();
                        Calendar fieldValue = null;
                        if (dateTimeValue != null) {
                            fieldValue = dateTimeValue.toCalendar();
                        }
                        worksheetItemFields.enterFieldValue(fieldid, 0, fieldValue);
                        continue;
                    }
                    if (type == 4) {
                        worksheetItemFields.updateFieldDatatype(fieldid, "number");
                        BigDecimal value = null;
                        DecimalFormat df = new DecimalFormat("#.########");
                        df.setRoundingMode(RoundingMode.HALF_UP);
                        if (cell.getDisplayStringValue().length() > 0) {
                            value = new BigDecimal(df.format(cell.getDoubleValue()));
                        }
                        worksheetItemFields.enterFieldValue(fieldid, 0, value);
                        continue;
                    }
                    worksheetItemFields.updateFieldDatatype(fieldid, "string");
                    worksheetItemFields.enterFieldValue(fieldid, 0, cell.getStringValue());
                }
            }
            Iterator<String> iterator = worksheetItemFields.iterator();
            while (iterator.hasNext()) {
                String fieldid = iterator.next();
                if (found.contains(fieldid)) continue;
                worksheetItemFields.deleteField(fieldid);
            }
            worksheetItemFields.save();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return contents;
    }

    private String getHTML_old(String dockViewPrefix, boolean export) {
        String viewhtml;
        try {
            SpreadsheetEditorRendererOptions options = new SpreadsheetEditorRendererOptions();
            options.showRowColumnHeader = dockViewPrefix.length() > 0 ? true : this.config.getProperty("showrowcolumnheaders", "N").equals("Y");
            options.embedImages = export;
            options.prefix = dockViewPrefix;
            options.showFormulae = !export && this.getConfig().getProperty("showformulaindicator", "N").equals("Y") || this.getConfig().getPropertyListNotNull("renderprops").getProperty("showformulaindicator", "Y").equals("Y");
            String contents = this.getContents();
            SpreadsheetEditorModel model = this.hasContents() ? new SpreadsheetEditorModel(contents, this.getSapphireConnection()) : new SpreadsheetEditorModel(1, 4, 4, this.getSapphireConnection());
            SpreadsheetEditorHtmlRenderer renderer = new SpreadsheetEditorHtmlRenderer(model, options);
            renderer.setId(this.getWorksheetItemId());
            viewhtml = renderer.toHTML();
        }
        catch (Exception e) {
            viewhtml = "Failed to get spreadsheet html. Reason: " + e.getMessage();
        }
        return viewhtml;
    }

    private String getLiveIndexingText_old() {
        if (this.hasContents()) {
            try {
                SpreadsheetEditorModel model = new SpreadsheetEditorModel(this.getContents(), null);
                return model.getIndexText();
            }
            catch (Exception e) {
                this.logError("Failed to read spreadsheet contents. Reason: " + e.getMessage(), e);
                return "";
            }
        }
        return "";
    }
}

