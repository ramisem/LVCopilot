/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.spreadsheeteditor;

import com.labvantage.sapphire.pageelements.spreadsheeteditor.SpreadsheetEditorAjaxRequest;
import com.labvantage.sapphire.pageelements.spreadsheeteditor.SpreadsheetEditorModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SpreadsheetEditorJSONRenderer {
    SpreadsheetEditorModel model;

    public SpreadsheetEditorJSONRenderer(SpreadsheetEditorModel model) {
        this.model = model;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        JSONObject sheets = new JSONObject();
        json.put("sheets", sheets);
        for (int i = 0; i < this.model.sheets.size(); ++i) {
            SpreadsheetEditorModel.Sheet sheet = this.model.sheets.get(i);
            JSONObject osheet = this.sheetToJSON(sheet);
            osheet.put("index", "" + i);
            sheets.put(sheet.name, osheet);
        }
        json.put("sheetCount", this.model.sheets.size());
        return json;
    }

    public JSONObject sheetToJSON(SpreadsheetEditorModel.Sheet sheet) throws JSONException {
        JSONObject osheet = new JSONObject();
        osheet.put("name", sheet.name);
        osheet.put("rowCount", sheet.rowCount);
        osheet.put("columnCount", sheet.colCount);
        JSONObject odata = new JSONObject();
        osheet.put("data", odata);
        JSONObject odataTable = new JSONObject();
        odata.put("dataTable", odataTable);
        JSONArray spans = new JSONArray();
        if (sheet.rowCount * sheet.colCount > SpreadsheetEditorAjaxRequest.MAX_SPREADSHEET_SIZE) {
            sheet.colCount = 20;
            sheet.rowCount = SpreadsheetEditorAjaxRequest.MAX_SPREADSHEET_SIZE / sheet.colCount;
        }
        for (int row = 0; row < sheet.rowCount; ++row) {
            JSONObject oRow = null;
            for (int col = 0; col < sheet.colCount; ++col) {
                boolean hasContent = false;
                JSONObject oCell = new JSONObject();
                SpreadsheetEditorModel.Cell cell = sheet.getCell(row, col);
                if (cell.value != null) {
                    oCell.put("value", cell.value);
                    hasContent = true;
                }
                if (cell.formula != null && cell.formula.length() > 0) {
                    oCell.put("formula", cell.formula);
                    hasContent = true;
                }
                JSONObject style = new JSONObject();
                oCell.put("style", style);
                if (cell.borderTop != null && cell.borderTop.length() > 0) {
                    JSONObject borderTop = new JSONObject();
                    style.put("borderTop", borderTop);
                    borderTop.put("style", cell.borderTop.equals("1px solid black") ? 1 : (cell.borderTop.equals("2px solid black") ? 2 : (cell.borderTop.equals("3px solid black") ? 3 : 1)));
                    hasContent = true;
                }
                if (cell.borderBottom != null && cell.borderBottom.length() > 0) {
                    JSONObject borderBottom = new JSONObject();
                    style.put("borderBottom", borderBottom);
                    borderBottom.put("style", cell.borderBottom.equals("1px solid black") ? 1 : (cell.borderBottom.equals("2px solid black") ? 2 : (cell.borderBottom.equals("3px solid black") ? 3 : 1)));
                    hasContent = true;
                }
                if (cell.borderRight != null && cell.borderRight.length() > 0) {
                    JSONObject borderRight = new JSONObject();
                    style.put("borderRight", borderRight);
                    borderRight.put("style", cell.borderRight.equals("1px solid black") ? 1 : (cell.borderRight.equals("2px solid black") ? 2 : (cell.borderRight.equals("3px solid black") ? 3 : 1)));
                    hasContent = true;
                }
                if (cell.borderLeft != null && cell.borderLeft.length() > 0) {
                    JSONObject borderLeft = new JSONObject();
                    style.put("borderLeft", borderLeft);
                    borderLeft.put("style", cell.borderLeft.equals("1px solid black") ? 1 : (cell.borderLeft.equals("2px solid black") ? 2 : (cell.borderLeft.equals("3px solid black") ? 3 : 1)));
                    hasContent = true;
                }
                style.put("imeMode", 1);
                style.put("themeFont", "Body");
                if (cell.foreColor != null && cell.foreColor.length() > 0) {
                    style.put("foreColor", cell.foreColor);
                }
                if (cell.backColor != null && cell.backColor.length() > 0) {
                    style.put("backColor", cell.backColor);
                }
                if (cell.hAlign != null && cell.hAlign.length() > 0) {
                    style.put("hAlign", cell.hAlign.equals("left") ? 0 : (cell.hAlign.equals("center") ? 1 : (cell.hAlign.equals("right") ? 2 : 0)));
                }
                if (cell.vAlign != null && cell.vAlign.length() > 0) {
                    style.put("vAlign", cell.vAlign.equals("top") ? 0 : (cell.vAlign.equals("middle") ? 1 : (cell.vAlign.equals("bottom") ? 2 : 0)));
                }
                boolean overrideFont = false;
                String font = "13.3333px Arial";
                if (cell.isItalic) {
                    font = "italic " + font;
                    overrideFont = true;
                }
                if (cell.isBold) {
                    font = "bold " + font;
                    overrideFont = true;
                }
                if (overrideFont) {
                    style.put("font", font);
                }
                int textDecoration = 0;
                textDecoration += cell.isUnderline ? 1 : 0;
                if ((textDecoration += cell.isStrikethrough ? 2 : 0) > 0) {
                    style.put("textDecoration", textDecoration);
                }
                if (cell.format != null && cell.format.length() > 0) {
                    style.put("formatter", cell.format);
                } else {
                    style.put("formatter", "General");
                }
                if (hasContent) {
                    if (oRow == null) {
                        oRow = new JSONObject();
                    }
                    oRow.put("" + col, oCell);
                }
                if (cell.rowSpan <= 1 && cell.colSpan <= 1) continue;
                JSONObject span = new JSONObject();
                spans.put(span);
                span.put("row", row);
                span.put("col", col);
                span.put("rowCount", cell.rowSpan);
                span.put("colCount", cell.colSpan);
            }
            if (oRow == null) continue;
            odataTable.put("" + row, oRow);
        }
        if (sheet.columns != null) {
            JSONArray columns = new JSONArray();
            osheet.put("columns", columns);
            for (int i = 0; i < sheet.colCount; ++i) {
                JSONObject column = new JSONObject();
                column.put("size", sheet.columns[i].width);
                columns.put(column);
            }
        }
        if (sheet.rows != null) {
            JSONArray rows = new JSONArray();
            osheet.put("rows", rows);
            for (int i = 0; i < sheet.rowCount; ++i) {
                JSONObject row = new JSONObject();
                row.put("size", sheet.rows[i].height);
                rows.put(row);
            }
        }
        if (spans.length() > 0) {
            osheet.put("spans", spans);
        }
        return osheet;
    }

    protected String getCellStyle(SpreadsheetEditorModel.Cell cell) {
        String style = "";
        if (cell.hAlign.length() > 0) {
            style = style + "text-align:" + cell.hAlign + ";";
        }
        if (cell.vAlign.length() > 0) {
            style = style + "vertical-align:" + cell.vAlign + ";";
        }
        if (cell.foreColor.length() > 0) {
            style = style + "color: " + cell.foreColor + ";";
        }
        if (cell.backColor.length() > 0) {
            style = style + "background-color: " + cell.backColor + ";";
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

