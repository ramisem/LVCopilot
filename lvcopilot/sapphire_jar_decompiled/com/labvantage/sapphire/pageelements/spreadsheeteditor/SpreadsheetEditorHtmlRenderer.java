/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.spreadsheeteditor;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.pageelements.spreadsheeteditor.SparklineStreamer;
import com.labvantage.sapphire.pageelements.spreadsheeteditor.SpreadsheetEditorModel;
import com.labvantage.sapphire.pageelements.spreadsheeteditor.SpreadsheetEditorRendererOptions;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.util.HttpUtil;

public class SpreadsheetEditorHtmlRenderer {
    private final SpreadsheetEditorRendererOptions options;
    private SpreadsheetEditorModel model;
    private String id;

    public void setId(String id) {
        this.id = id;
    }

    public SpreadsheetEditorHtmlRenderer(SpreadsheetEditorModel model, SpreadsheetEditorRendererOptions options) {
        this.options = options;
        this.model = model;
    }

    public String toHTML() {
        StringBuffer out = new StringBuffer();
        if (this.model.sheets.size() == 1) {
            out.append(this.sheetToHTML(this.model.sheets.get(0)));
        } else {
            for (SpreadsheetEditorModel.Sheet sheet : this.model.sheets) {
                out.append(sheet.name + "<br>");
                out.append(this.sheetToHTML(sheet));
                out.append("<br>");
            }
        }
        return out.toString();
    }

    public String sheetToHTML(SpreadsheetEditorModel.Sheet sheet) {
        int rows;
        StringBuffer output = new StringBuffer();
        String sheetname = sheet.name;
        String tableStyle = "";
        String tdStyle = "";
        if (!sheet.hasHorizontalGridline) {
            tableStyle = tableStyle + "border-bottom:1px solid lightgrey;";
            tdStyle = tdStyle + "border-bottom:none;";
        }
        if (!sheet.hasVerticalGridline) {
            tableStyle = tableStyle + "border-right:1px solid lightgrey;";
            tdStyle = tdStyle + "border-right:none;";
        }
        output.append("<table style=\"" + tableStyle + "\" class=\"spreadsheeteditor_table\" cellspacing=\"0\">\n");
        int fromRow = sheet.firstContentRow;
        int fromColumn = sheet.firstContentCol;
        int cols = sheet.lastContentCol < 3 && !sheet.hasPrintArea ? 3 : sheet.lastContentCol;
        int n = rows = sheet.lastContentRow < 3 && !sheet.hasPrintArea ? 3 : sheet.lastContentRow;
        if (this.options.showRowColumnHeader) {
            output.append("<tr>\n");
            output.append("<td style=\"" + tdStyle + "\" class=\"spreadsheeteditor_corner\">&nbsp;</td>");
            for (int col = fromColumn; col <= cols; ++col) {
                output.append("<td style=\"" + tdStyle + ";width:" + sheet.getColumnWidth(col) + "px;\" class=\"spreadsheeteditor_colheader\">").append(SpreadsheetEditorHtmlRenderer.getExcelColumnName(col + 1)).append("</td>");
            }
            output.append("</tr>\n");
        }
        for (int row = fromRow; row <= rows; ++row) {
            output.append("<tr style=\"height:" + sheet.getRowHeight(row) + "px\">\n");
            if (this.options.showRowColumnHeader) {
                String rowid = this.options.prefix + "spread_" + this.id + "_" + sheetname + "_row" + row;
                output.append("<td style=\"" + tdStyle + "\" id=\"" + rowid + "\" class=\"spreadsheeteditor_rowheader\">" + (row + 1) + "</td>");
            }
            for (int col = fromColumn; col <= cols; ++col) {
                Object value;
                SpreadsheetEditorModel.Cell cell = sheet.cells[row][col];
                String displayText = "";
                String style = "";
                String colSpan = "";
                String rowSpan = "";
                boolean skip = false;
                if (cell == null) {
                    value = null;
                    displayText = "&nbsp;";
                } else {
                    value = cell.value;
                    displayText = cell.getDisplayText();
                    if (cell.rowSpan > 0) {
                        rowSpan = " rowspan=\"" + cell.rowSpan + "\"";
                    }
                    if (cell.colSpan > 0) {
                        colSpan = " colspan=\"" + cell.colSpan + "\"";
                    }
                    skip = cell.isHiddenBySpan;
                }
                if (skip) continue;
                style = cell == null ? "" : this.getCellStyle(cell);
                String id = this.options.prefix + "spread_" + this.id + "_" + sheetname + "_" + row + "_" + col;
                style = style + "width:" + sheet.getColumnWidth(col) + "px;";
                if (cell != null && cell.wordWrap) {
                    style = style + "max-width:" + sheet.getColumnWidth(col) + "px;overflow-x:hidden;word-wrap:break-word";
                }
                String tip = "";
                String extraClass = "";
                String extraDiv = "";
                String extraSpan = "";
                if (this.options.showFormulae && cell != null && cell.formula != null && cell.formula.length() > 0 && !cell.formula.toUpperCase().contains("SPARKLINE")) {
                    if (cell.formula.length() < 1000) {
                        tip = " title=\"" + cell.formula + "\" ";
                        extraClass = " triangleContainer";
                        extraDiv = "<div class=\"triangle triangle_tl\"></div>";
                    } else {
                        extraClass = " triangleContainer spreadsheettooltip";
                        extraDiv = "<div class=\"triangle triangle_tl\"></div>";
                        extraSpan = "<div class=\"spreadsheettooltiptext\" style=\"" + (row < 2 ? "top:100%;bottom:auto" : "") + "\">" + cell.formula + "</div>";
                    }
                }
                output.append("<td " + rowSpan + colSpan + " id=\"" + id + "\" " + tip + " class=\"spreadsheeteditor_cell" + extraClass + "\" onmouseover=\"spreadsheeteditor.mouseOverCell('" + id + "')\" onmouseout=\"spreadsheeteditor.mouseOutCell('" + id + "')\" style=\"" + tdStyle + ";" + style + "\">");
                output.append(extraDiv);
                output.append(extraSpan);
                if (value != null && value instanceof JSONObject) {
                    try {
                        String name = ((JSONObject)value).optString("name");
                        if (name.toUpperCase().contains("SPARKLINE")) {
                            JSONObject cValue = ((JSONObject)value).optJSONObject("value");
                            if (name.equalsIgnoreCase("LINESPARKLINE") || name.equalsIgnoreCase("COLUMNSPARKLINE")) {
                                JSONObject cData = cValue.optJSONObject("data");
                                JSONArray dataValues = this.getDataValues(sheet, cData);
                                cValue.put("dataValues", dataValues);
                                JSONObject cAxis = cValue.optJSONObject("axisReference");
                                if (cAxis != null && cAxis.length() > 0) {
                                    JSONArray axisValues = this.getDataValues(sheet, cAxis);
                                    cValue.put("axisValues", axisValues);
                                }
                            }
                            String url = this.options.embedImages ? SparklineStreamer.getBase64Sparkline(name, cValue, cell.getWidth(), cell.getHeight()) : "rc?command=operation&operationclass=com.labvantage.sapphire.pageelements.spreadsheeteditor.SparklineStreamer&name=" + name + "&width=" + cell.getWidth() + "&height=" + cell.getHeight() + "&json=" + HttpUtil.encodeURIComponent(cValue.toString());
                            output.append("<img src=\"" + url + "\" border=0>");
                        } else {
                            String calcError = ((JSONObject)value).optString("_calcError");
                            if (calcError != null && calcError.length() > 0) {
                                output.append(calcError);
                            }
                        }
                    }
                    catch (JSONException e) {
                        Trace.logError("Unable to create sparkline: " + e.getMessage(), e);
                    }
                } else {
                    output.append(displayText);
                }
                output.append("</td>");
            }
            output.append("</tr>\n");
        }
        output.append("</table>\n");
        return output.toString();
    }

    private JSONArray getDataValues(SpreadsheetEditorModel.Sheet sheet, JSONObject cData) {
        JSONArray dataValues = new JSONArray();
        int iCol = cData.optInt("col");
        int iRow = cData.optInt("row");
        int iRowCount = cData.optInt("rowCount");
        int iColCount = cData.optInt("colCount");
        for (int ii = 0; ii < iRowCount; ++ii) {
            for (int jj = 0; jj < iColCount; ++jj) {
                Object v = sheet.getCell((int)(iRow + ii), (int)(iCol + jj)).value;
                if (v instanceof Date) {
                    dataValues.put(((Date)v).getTime());
                    continue;
                }
                dataValues.put(v);
            }
        }
        return dataValues;
    }

    protected String getCellStyle(SpreadsheetEditorModel.Cell cell) {
        SpreadsheetEditorModel.Cell temp;
        String style = "";
        if (cell.hAlign.length() > 0) {
            style = style + "text-align:" + cell.hAlign + ";";
        }
        style = cell.vAlign.length() > 0 ? style + "vertical-align:" + cell.vAlign + ";" : style + "vertical-align: top;";
        if (cell.foreColor.length() > 0) {
            style = style + "color: " + cell.foreColor + ";";
        }
        if (cell.backColor.length() > 0) {
            style = style + "background-color: " + cell.backColor + ";";
        }
        if (!cell.wordWrap) {
            style = style + "white-space:nowrap;";
        }
        if (cell.borderTop.length() > 0) {
            style = style + "border-top: " + cell.borderTop + ";";
        }
        if (cell.borderLeft.length() > 0) {
            style = style + "border-left: " + cell.borderLeft + ";";
        }
        if (cell.rowSpan > 1) {
            temp = cell.sheet.getCell(cell.row + cell.rowSpan - 1, cell.col);
            cell.borderBottom = temp.borderBottom;
        }
        if (cell.borderBottom.length() > 0) {
            SpreadsheetEditorModel.Cell cellBelow = cell.getCellBelow();
            style = cellBelow == null || cellBelow.borderTop.equals("") ? style + "border-bottom: " + cell.borderBottom + ";" : style + "border-bottom: 0;";
        }
        if (cell.colSpan > 1) {
            temp = cell.sheet.getCell(cell.row, cell.col + cell.colSpan - 1);
            cell.borderRight = temp.borderRight;
        }
        if (cell.borderRight.length() > 0) {
            SpreadsheetEditorModel.Cell cellRight = cell.getCellRight();
            style = cellRight == null || cellRight.borderTop.equals("") ? style + "border-right: " + cell.borderRight + ";" : style + "border-right: 0;";
        }
        if (cell.isBold) {
            style = style + "font-weight: bold;";
        }
        if (cell.isItalic) {
            style = style + "font-style: italic;";
        }
        if (cell.isUnderline && cell.isStrikethrough) {
            style = style + "text-decoration: underline line-through;";
        } else if (cell.isUnderline) {
            style = style + "text-decoration: underline;";
        } else if (cell.isStrikethrough) {
            style = style + "text-decoration: line-through;";
        }
        return style;
    }

    public static String getExcelColumnName(int number) {
        StringBuilder sb = new StringBuilder();
        int num = number - 1;
        while (num >= 0) {
            int numChar = num % 26 + 65;
            sb.append((char)numChar);
            num = num / 26 - 1;
        }
        return sb.reverse().toString();
    }
}

