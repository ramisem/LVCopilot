/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.aspose.cells.Border
 *  com.aspose.cells.BorderCollection
 *  com.aspose.cells.Cell
 *  com.aspose.cells.Cells
 *  com.aspose.cells.CellsHelper
 *  com.aspose.cells.Font
 *  com.aspose.cells.Range
 *  com.aspose.cells.Row
 *  com.aspose.cells.RowCollection
 *  com.aspose.cells.Style
 *  com.aspose.cells.Workbook
 *  com.aspose.cells.Worksheet
 *  com.aspose.cells.WorksheetCollection
 */
package com.labvantage.sapphire.pageelements.spreadsheeteditor;

import com.aspose.cells.Border;
import com.aspose.cells.BorderCollection;
import com.aspose.cells.Cells;
import com.aspose.cells.CellsHelper;
import com.aspose.cells.Font;
import com.aspose.cells.Range;
import com.aspose.cells.Row;
import com.aspose.cells.RowCollection;
import com.aspose.cells.Style;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.aspose.cells.WorksheetCollection;
import com.labvantage.sapphire.services.SapphireConnection;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.util.M18NUtil;
import sapphire.util.StringUtil;

public class SpreadsheetEditorModel {
    ArrayList<Sheet> sheets = new ArrayList();
    public final String[] NUMBER_FORMAT = new String[]{"", "0", "0.00", "#,##0", "#,##0.00", "$#,##0;$-#,##0", "$#,##0;Red$-#,##0", "$#,##0.00;$-#,##0.00", "$#,##0.00;Red$-#,##0.00", "0%", "0.00%", "0.00E+00", "# ?/?", "# /", "m/d/yy", "d-mmm-yy", "d-mmm", "mmm-yy", "h:mm AM/PM", "h:mm:ss AM/PM", "h:mm", "h:mm:ss", "m/d/yy h:mm", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "#,##0;-#,##0", "#,##0;Red-#,##0", "#,##0.00;-#,##0.00", "#,##0.00;Red-#,##0.00", "_ * #,##0_ ;_ * \"_ ;_ @_", "_ $* #,##0_ ;_ $* \"_ ;_ @_", "_ * #,##0.00_ ;_ * \"??_ ;_ @_", "_ $* #,##0.00_ ;_ $* \"??_ ;_ @_", "mm:ss", "H:mm:ss", "mm:ss.0", "##0.0E+00", "@"};
    ArrayList<Integer> priorDeletedRows = new ArrayList();
    public M18NUtil m18n;
    private Workbook dummyWorkbook = null;
    private com.aspose.cells.Cell dummyCell = null;

    public SpreadsheetEditorModel(String json, SapphireConnection sapphireConnection) throws JSONException {
        this.m18n = sapphireConnection == null ? new M18NUtil() : new M18NUtil(sapphireConnection);
        JSONObject jso = new JSONObject(json);
        int sheetCount = jso.optInt("sheetCount", 1);
        Sheet[] sheetsArray = new Sheet[sheetCount];
        JSONObject oSheets = jso.getJSONObject("sheets");
        Iterator keys = oSheets.keys();
        while (keys.hasNext()) {
            String key = (String)keys.next();
            JSONObject oSheet = oSheets.getJSONObject(key);
            int index = oSheet.optInt("index", 0);
            sheetsArray[index] = new Sheet(oSheet, key);
        }
        for (int i = 0; i < sheetsArray.length; ++i) {
            this.sheets.add(sheetsArray[i]);
        }
        JSONObject extra = jso.optJSONObject("lv_extra");
        if (extra != null) {
            for (Sheet sheet : this.sheets) {
                JSONArray deletedRows;
                JSONObject sheetExtra = extra.optJSONObject(sheet.name);
                if (sheetExtra == null) continue;
                JSONArray insertedRows = sheetExtra.optJSONArray("insertedrows");
                if (insertedRows != null) {
                    for (int j = 0; j < insertedRows.length(); ++j) {
                        int row = insertedRows.optJSONObject(j).optInt("row");
                        sheet.setRowNew(row);
                    }
                }
                if ((deletedRows = sheetExtra.optJSONArray("deletedrows")) == null) continue;
                for (int j = 0; j < deletedRows.length(); ++j) {
                    int row = deletedRows.optJSONObject(j).optInt("row");
                    sheet.addPriorDeleteRow(row);
                }
            }
        }
    }

    public SpreadsheetEditorModel(String[] csvrows, SapphireConnection sapphireConnection, String delimiter) throws JSONException {
        this.m18n = sapphireConnection == null ? new M18NUtil() : new M18NUtil(sapphireConnection);
        Sheet sheet = new Sheet(csvrows, delimiter);
        this.sheets.add(sheet);
    }

    public SpreadsheetEditorModel(int isheets, int rows, int cols, SapphireConnection sapphireConnection) throws JSONException {
        this.m18n = sapphireConnection == null ? new M18NUtil() : new M18NUtil(sapphireConnection);
        for (int isheet = 0; isheet < isheets; ++isheet) {
            Sheet sheet = new Sheet("Sheet" + (isheet + 1), rows, cols);
            this.sheets.add(sheet);
        }
    }

    public SpreadsheetEditorModel(Workbook workbook, SapphireConnection sapphireConnection, boolean multisheet) {
        this.m18n = sapphireConnection == null ? new M18NUtil() : new M18NUtil(sapphireConnection);
        WorksheetCollection xsheets = workbook.getWorksheets();
        int sheetCount = xsheets.getCount();
        for (int i = 0; i < (multisheet ? sheetCount : 1); ++i) {
            Worksheet xsheet = xsheets.get(i);
            Sheet sheet = new Sheet(xsheet);
            this.sheets.add(sheet);
        }
    }

    public HashMap getFields() {
        HashMap<String, Object> ret = new HashMap<String, Object>();
        for (Sheet sheet : this.sheets) {
            for (String fieldid : sheet.fields.keySet()) {
                ArrayList vals;
                if (!(sheet.fields.get(fieldid) instanceof Cell)) continue;
                Cell cell = sheet.fields.get(fieldid);
                if (!ret.containsKey(fieldid)) {
                    ret.put(fieldid, cell.value);
                    continue;
                }
                if (ret.get("fieldid") instanceof ArrayList) {
                    vals = (ArrayList)ret.get("fieldid");
                    vals.add(cell.value);
                    continue;
                }
                vals = new ArrayList();
                vals.add(ret.get("fieldid"));
                vals.add(cell.value);
                ret.put(fieldid, vals);
            }
        }
        return ret;
    }

    public String getIndexText() {
        StringBuffer out = new StringBuffer();
        for (Sheet sheet : this.sheets) {
            out.append(sheet.name).append(" ");
            for (int row = 0; row <= sheet.lastContentRow; ++row) {
                for (int col = 0; col <= sheet.lastContentCol; ++col) {
                    Cell cell = sheet.cells[row][col];
                    if (cell == null || cell.value == null || !(cell.value instanceof String)) continue;
                    out.append(cell.value).append(" ");
                }
            }
        }
        return out.toString();
    }

    public Sheet getSheetByName(String name) {
        for (Sheet sheet : this.sheets) {
            if (!name.equals(sheet.name)) continue;
            return sheet;
        }
        return null;
    }

    public Date fromOADate(double d) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(1899, 11, 30, 0, 0, 0);
        double mantissa = d - (double)((long)d);
        double hour = mantissa * 24.0;
        double min = (hour - (double)((long)hour)) * 60.0;
        double sec = (min - (double)((long)min)) * 60.0;
        cal.add(5, (int)d);
        cal.add(10, (int)hour);
        cal.add(12, (int)min);
        cal.add(13, (int)(sec + 0.001));
        return cal.getTime();
    }

    public String formatDateAsExcel(Date value, String format) {
        Workbook wb = new Workbook();
        Worksheet sheet = wb.getWorksheets().get(0);
        com.aspose.cells.Cell cell = sheet.getCells().get(1, 1);
        Style style = cell.getStyle();
        style.setCustom(format);
        cell.setValue((Object)value);
        cell.setStyle(style);
        return cell.getDisplayStringValue();
    }

    public String formatDoubleAsExcel(double value, String format) {
        Workbook wb = new Workbook();
        Worksheet sheet = wb.getWorksheets().get(0);
        com.aspose.cells.Cell cell = sheet.getCells().get(1, 1);
        Style style = cell.getStyle();
        style.setCustom(format);
        cell.setValue((Object)value);
        cell.setStyle(style);
        return cell.getDisplayStringValue();
    }

    public class Cell {
        int row = 1;
        int col = 1;
        Sheet sheet = null;
        Object value = null;
        String format = "";
        boolean isBold = false;
        boolean isItalic = false;
        boolean isUnderline = false;
        boolean isStrikethrough = false;
        int rowSpan = 1;
        int colSpan = 1;
        boolean isHiddenBySpan = false;
        String hAlign = "";
        String vAlign = "";
        public String formula = "";
        public String backColor = "";
        public String foreColor = "";
        public String fieldid = "";
        public boolean wordWrap = false;
        String borderLeft = "";
        String borderRight = "";
        String borderTop = "";
        String borderBottom = "";

        public Cell(Sheet sheet, int row, int col) {
            this.sheet = sheet;
            this.row = row;
            this.col = col;
        }

        public String getDisplayText() {
            if (this.value == null) {
                return "";
            }
            if (this.value instanceof Number) {
                double d;
                double d2 = this.value instanceof Long ? new Long((Long)this.value).doubleValue() : (this.value instanceof Integer ? new Integer((Integer)this.value).doubleValue() : (d = this.value instanceof Double ? (Double)this.value : Double.parseDouble((String)this.value)));
                if (this.format == null || this.format.length() == 0) {
                    NumberFormat nf = NumberFormat.getInstance(SpreadsheetEditorModel.this.m18n.getLocale());
                    nf.setMaximumFractionDigits(12);
                    return nf.format(d);
                }
                return SpreadsheetEditorModel.this.formatDoubleAsExcel(d, this.format);
            }
            if (this.value instanceof Date) {
                if (this.format == null || this.format.length() == 0) {
                    DateFormat df = DateFormat.getDateInstance(3, SpreadsheetEditorModel.this.m18n.getLocale());
                    return df.format((Date)this.value);
                }
                return SpreadsheetEditorModel.this.formatDateAsExcel((Date)this.value, this.format);
            }
            if (this.value instanceof Boolean) {
                return ("" + this.value).toUpperCase();
            }
            return "" + this.value;
        }

        public Cell getCellBelow() {
            if (this.row + this.rowSpan > this.sheet.lastContentRow) {
                return null;
            }
            return this.sheet.getCell(this.row + this.rowSpan, this.col);
        }

        public Cell getCellRight() {
            if (this.col + this.colSpan > this.sheet.lastContentCol) {
                return null;
            }
            return this.sheet.getCell(this.row, this.col + this.colSpan);
        }

        public int getHeight() {
            int height = 0;
            for (int i = this.row; i < this.row + this.rowSpan; ++i) {
                height += this.sheet.getRowHeight(i);
            }
            return height;
        }

        public int getWidth() {
            int width = 0;
            for (int i = this.col; i < this.col + this.colSpan; ++i) {
                width += this.sheet.getColumnWidth(i);
            }
            return width;
        }

        public String getBorderStyle(JSONObject border) {
            if (border == null) {
                return "";
            }
            String type = border.optString("style");
            if (type == null || type.length() == 0 || type.equals(0)) {
                return "";
            }
            String color = border.optString("color", "black");
            switch (type) {
                case "1": {
                    return "1px solid " + color;
                }
                case "2": {
                    return "2px solid " + color;
                }
                case "5": {
                    return "3px solid " + color;
                }
                case "3": {
                    return "1px dashed " + color;
                }
            }
            return "";
        }

        public boolean hasFormat() {
            return this.borderTop.length() > 0 || this.borderLeft.length() > 0 || this.borderBottom.length() > 0 || this.borderRight.length() > 0 || this.backColor.length() > 0;
        }

        public String getBorderStyle(Border border) {
            String ret = "";
            if (border.getLineStyle() == 1) {
                ret = "1px solid black";
            }
            if (border.getLineStyle() == 2) {
                ret = "2px solid black";
            }
            if (border.getLineStyle() == 5) {
                ret = "3px solid black";
            }
            return ret;
        }
    }

    public class Sheet {
        boolean hasHorizontalGridline = true;
        boolean hasVerticalGridline = true;
        String name = "";
        int rowCount;
        int colCount;
        boolean hasPrintArea = false;
        int firstContentRow = 0;
        int firstContentCol = 0;
        int lastContentRow = 0;
        int lastContentCol = 0;
        int defaultColumnWidth = 60;
        int defaultRowHeight = 20;
        Row[] rows = null;
        Column[] columns = null;
        Cell[][] cells;
        HashMap<String, Cell> fields = new HashMap();

        public Sheet(JSONObject json, String id) throws JSONException {
            JSONObject oData;
            this.name = json.optString("name", id);
            this.rowCount = Integer.parseInt(json.getString("rowCount"));
            this.colCount = Integer.parseInt(json.getString("columnCount"));
            if (this.rowCount < 4) {
                this.rowCount = 4;
            }
            if (this.colCount < 4) {
                this.colCount = 4;
            }
            this.rows = new Row[this.rowCount];
            this.columns = new Column[this.colCount];
            this.cells = new Cell[this.rowCount][this.colCount];
            if (json.has("data") && !json.isNull("data") && (oData = json.getJSONObject("data")).has("dataTable") && !oData.isNull("dataTable")) {
                JSONObject dataTable = oData.getJSONObject("dataTable");
                for (int row = 0; row < this.rowCount; ++row) {
                    this.rows[row] = new Row(row);
                    if (!dataTable.has("" + row)) continue;
                    JSONObject rowObject = dataTable.getJSONObject("" + row);
                    for (int col = 0; col < this.colCount; ++col) {
                        String formula;
                        JSONObject style;
                        JSONObject cellType;
                        if (!rowObject.has("" + col)) continue;
                        JSONObject oCell = rowObject.getJSONObject("" + col);
                        Cell cell = this.getCell(row, col);
                        Object value = oCell.opt("value");
                        JSONObject jSONObject = cellType = oCell.optJSONObject("style") == null ? null : oCell.optJSONObject("style").optJSONObject("cellType");
                        if (cellType != null && cellType.optString("typeName").equals("5")) {
                            String trueValue = cellType.optString("textTrue");
                            String falseValue = cellType.optString("textFalse");
                            if (trueValue.length() > 0) {
                                value = value != null && value instanceof Boolean && (Boolean)value != false ? trueValue : falseValue;
                            }
                        }
                        boolean isDate = false;
                        if (value != null) {
                            if (value instanceof String && ((String)value).startsWith("/OADate(")) {
                                isDate = true;
                                double d = Double.parseDouble(StringUtil.replaceAll(StringUtil.replaceAll((String)value, "/OADate(", ""), ")/", ""));
                                cell.value = SpreadsheetEditorModel.this.fromOADate(d);
                            } else if (value instanceof String) {
                                Calendar cal = Calendar.getInstance();
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                sdf.setCalendar(cal);
                                try {
                                    cal.setTime(sdf.parse((String)value));
                                    cell.value = cal.getTime();
                                }
                                catch (Exception e) {
                                    cell.value = value;
                                }
                            } else {
                                cell.value = value;
                            }
                            String string = cell.hAlign = value instanceof String && !isDate ? "left" : "right";
                        }
                        if ((style = oCell.optJSONObject("style")) != null) {
                            JSONObject autoFormatter;
                            cell.borderTop = cell.getBorderStyle(style.optJSONObject("borderTop"));
                            cell.borderLeft = cell.getBorderStyle(style.optJSONObject("borderLeft"));
                            cell.borderBottom = cell.getBorderStyle(style.optJSONObject("borderBottom"));
                            cell.borderRight = cell.getBorderStyle(style.optJSONObject("borderRight"));
                            cell.backColor = style.optString("backColor", "");
                            cell.foreColor = style.optString("foreColor", "");
                            cell.wordWrap = style.optBoolean("wordWrap", false);
                            String font = style.optString("font", "");
                            cell.isBold = font.contains("bold") || font.contains("800") || font.contains("700");
                            cell.isItalic = font.contains("italic");
                            int textDecoration = style.optInt("textDecoration", 0);
                            cell.isUnderline = (textDecoration & 1) == 1;
                            cell.isStrikethrough = (textDecoration & 2) == 2;
                            int hAlign = style.optInt("hAlign", 3);
                            cell.hAlign = hAlign == 0 ? "left" : (hAlign == 1 ? "center" : (hAlign == 2 ? "right" : cell.hAlign));
                            int vAlign = style.optInt("vAlign", 3);
                            cell.vAlign = vAlign == 0 ? "top" : (vAlign == 1 ? "middle" : (vAlign == 2 ? "bottom" : cell.vAlign));
                            cell.format = style.optString("formatter");
                            if ((cell.format == null || cell.format.length() == 0) && (autoFormatter = style.optJSONObject("autoFormatter")) != null) {
                                cell.format = autoFormatter.optString("formatCached");
                            }
                        }
                        if ((formula = oCell.optString("formula")) != null) {
                            cell.formula = formula;
                        }
                        if (cell.value == null && cell.formula.length() <= 0 && !cell.hasFormat()) continue;
                        this.lastContentRow = row > this.lastContentRow ? row : this.lastContentRow;
                        this.lastContentCol = col > this.lastContentCol ? col : this.lastContentCol;
                    }
                }
            }
            if (json.has("columns") && !json.isNull("columns")) {
                JSONArray jcolumns = json.optJSONArray("columns");
                for (int i = 0; i < jcolumns.length(); ++i) {
                    Object jcolumn = jcolumns.get(i);
                    if (jcolumn == null || !(jcolumn instanceof JSONObject)) continue;
                    this.columns[i] = new Column();
                    this.columns[i].width = ((JSONObject)jcolumn).optInt("size");
                }
            }
            if (json.has("rows") && !json.isNull("rows")) {
                JSONArray jrows = json.optJSONArray("rows");
                for (int i = 0; i < jrows.length(); ++i) {
                    Object jrow = jrows.get(i);
                    if (jrow == null || !(jrow instanceof JSONObject)) continue;
                    this.rows[i] = new Row(i);
                    this.rows[i].height = ((JSONObject)jrow).optInt("size");
                }
            }
            if (json.has("printInfo") && !json.isNull("printInfo") && json.optJSONObject("printInfo").optInt("rowEnd", 0) >= 0 && json.optJSONObject("printInfo").optInt("columnEnd", 0) >= 0) {
                JSONObject printInfo = json.optJSONObject("printInfo");
                this.hasPrintArea = true;
                this.firstContentRow = printInfo.optInt("rowStart", this.firstContentRow);
                this.firstContentCol = printInfo.optInt("columnStart", this.firstContentCol);
                this.lastContentRow = printInfo.optInt("rowEnd", this.lastContentRow);
                this.lastContentCol = printInfo.optInt("columnEnd", this.lastContentCol);
                if (this.lastContentRow >= this.rowCount) {
                    this.lastContentRow = this.rowCount - 1;
                }
                if (this.lastContentCol >= this.colCount) {
                    this.lastContentCol = this.colCount - 1;
                }
            }
            if (json.has("gridline") && !json.isNull("gridline")) {
                JSONObject gridline = json.optJSONObject("gridline");
                this.hasVerticalGridline = gridline.optBoolean("showVerticalGridline", true);
                this.hasHorizontalGridline = gridline.optBoolean("showHorizontalGridline", true);
            }
            if (json.has("names") && !json.isNull("names")) {
                JSONArray names = json.getJSONArray("names");
                for (int i = 0; i < names.length(); ++i) {
                    JSONObject name = names.getJSONObject(i);
                    String fieldid = name.getString("name");
                    String formula = name.getString("formula");
                    Cell cell = this.getCell(formula);
                    cell.fieldid = fieldid;
                    this.fields.put(fieldid, cell);
                }
            }
            if (json.has("spans") && !json.isNull("spans")) {
                JSONArray spans = json.getJSONArray("spans");
                for (int i = 0; i < spans.length(); ++i) {
                    JSONObject span = spans.getJSONObject(i);
                    int startRow = span.getInt("row");
                    int startCol = span.getInt("col");
                    int colSpan = span.getInt("colCount");
                    int rowSpan = span.getInt("rowCount");
                    Cell cell = this.getCell(startRow, startCol);
                    if (rowSpan > 1) {
                        cell.rowSpan = rowSpan;
                    }
                    if (colSpan > 1) {
                        cell.colSpan = colSpan;
                    }
                    for (int row = startRow; row < startRow + rowSpan; ++row) {
                        for (int col = startCol; col < startCol + colSpan; ++col) {
                            if (row <= startRow && col <= startCol) continue;
                            Cell skipCell = this.getCell(row, col);
                            skipCell.isHiddenBySpan = true;
                            if (this.hasPrintArea) continue;
                            this.lastContentRow = row > this.lastContentRow ? row : this.lastContentRow;
                            this.lastContentCol = col > this.lastContentCol ? col : this.lastContentCol;
                        }
                    }
                }
            }
        }

        public Sheet(String[] csvrows, String delimiter) throws JSONException {
            int i;
            this.name = "Sheet 1";
            this.rowCount = csvrows.length;
            this.colCount = 0;
            for (i = 0; i < csvrows.length; ++i) {
                int size = StringUtil.split(csvrows[i], delimiter).length;
                if (size <= this.colCount) continue;
                this.colCount = size;
            }
            this.cells = new Cell[this.rowCount][this.colCount];
            for (i = 0; i < csvrows.length; ++i) {
                String[] values = csvrows[i].split(delimiter);
                for (int j = 0; j < values.length; ++j) {
                    Cell cell = this.getCell(i, j);
                    try {
                        Float value = Float.valueOf(Float.parseFloat(values[j].trim()));
                        cell.value = value;
                        continue;
                    }
                    catch (Exception e) {
                        cell.value = values[j].trim();
                    }
                }
            }
        }

        public Sheet(String name, int rows, int cols) {
            this.name = name;
            this.rowCount = rows;
            this.colCount = cols;
            this.lastContentRow = rows - 1;
            this.lastContentCol = cols - 1;
            this.cells = new Cell[this.rowCount][this.colCount];
        }

        public Sheet(Worksheet xsheet) {
            this.name = xsheet.getName();
            Cells xcells = xsheet.getCells();
            this.rowCount = xcells.getMaxDisplayRange().getRowCount();
            this.colCount = xcells.getMaxDisplayRange().getColumnCount();
            RowCollection xrows = xcells.getRows();
            if (this.rowCount < 4) {
                this.rowCount = 4;
            }
            if (this.colCount < 4) {
                this.colCount = 4;
            }
            this.rows = new Row[this.rowCount];
            this.columns = new Column[this.colCount];
            this.cells = new Cell[this.rowCount][this.colCount];
            for (int col = 0; col < this.colCount; ++col) {
                this.columns[col] = new Column();
                this.columns[col].width = xcells.getColumnWidthPixel(col);
            }
            for (int r = 0; r < this.rowCount; ++r) {
                com.aspose.cells.Row row = xrows.get(r);
                this.rows[r] = new Row(r);
                if (row == null) continue;
                this.rows[r].height = xcells.getRowHeightPixel(r);
                for (int c = 0; c < this.colCount; ++c) {
                    Style style;
                    int styleNumber;
                    Object value;
                    com.aspose.cells.Cell xcell = row.get(c);
                    if (xcell == null) continue;
                    Cell cell = this.getCell(r, c);
                    int type = xcell.getType();
                    String formula = xcell.getFormula();
                    if (formula != null && formula.length() > 0) {
                        cell.formula = formula;
                    }
                    if (type == 5) {
                        cell.value = value = xcell.getStringValue();
                    } else if (type == 4) {
                        cell.value = value = new BigDecimal(xcell.getDoubleValue());
                    } else if (type == 1) {
                        cell.value = xcell.getDoubleValue();
                    }
                    Range range = xcell.getMergedRange();
                    if (range != null) {
                        if (range.getFirstRow() == xcell.getRow() && range.getFirstColumn() == xcell.getColumn()) {
                            cell.rowSpan = range.getRowCount();
                            cell.colSpan = range.getColumnCount();
                        } else {
                            cell.isHiddenBySpan = true;
                        }
                    }
                    cell.format = (styleNumber = (style = xcell.getStyle()).getNumber()) > 0 ? SpreadsheetEditorModel.this.NUMBER_FORMAT[styleNumber] : style.getCustom();
                    BorderCollection xborders = style.getBorders();
                    cell.borderTop = cell.getBorderStyle(xborders.getByBorderType(4));
                    cell.borderRight = cell.getBorderStyle(xborders.getByBorderType(2));
                    cell.borderBottom = cell.getBorderStyle(xborders.getByBorderType(8));
                    cell.borderLeft = cell.getBorderStyle(xborders.getByBorderType(1));
                    String string = style.getHorizontalAlignment() == 7 ? "left" : (style.getHorizontalAlignment() == 1 ? "center" : (cell.hAlign = style.getHorizontalAlignment() == 8 ? "right" : ""));
                    String string2 = style.getVerticalAlignment() == 9 ? "top" : (style.getVerticalAlignment() == 1 ? "middle" : (cell.vAlign = style.getVerticalAlignment() == 0 ? "bottom" : "top"));
                    if (!style.getForegroundColor().isEmpty()) {
                        String hexColour = Integer.toHexString(style.getForegroundArgbColor() & 0xFFFFFF);
                        if (hexColour.length() < 6) {
                            hexColour = "000000".substring(0, 6 - hexColour.length()) + hexColour;
                        }
                        cell.backColor = "#" + hexColour;
                    }
                    Font font = style.getFont();
                    cell.isBold = font.isBold();
                    cell.isItalic = font.isItalic();
                    cell.isUnderline = font.getUnderline() == 1;
                    cell.isStrikethrough = font.isStrikeout();
                    String hexColour = Integer.toHexString(font.getArgbColor() & 0xFFFFFF);
                    if (hexColour.length() < 6) {
                        hexColour = "000000".substring(0, 6 - hexColour.length()) + hexColour;
                    }
                    cell.foreColor = "#" + hexColour;
                }
            }
        }

        public int getRowHeight(int row) {
            return this.rows == null || this.rows.length <= row || this.rows[row] == null ? this.defaultRowHeight : this.rows[row].height;
        }

        public boolean isRowNew(int row) {
            return this.rows == null || this.rows.length <= row || this.rows[row] == null ? false : this.rows[row].isNew;
        }

        public int getColumnWidth(int col) {
            return this.columns == null || this.columns.length <= col || this.columns[col] == null ? this.defaultColumnWidth : this.columns[col].width;
        }

        protected Cell getCell(int row, int col) {
            if (row >= this.cells.length || col >= this.cells[row].length) {
                return new Cell(this, row, col);
            }
            if (this.cells[row][col] == null) {
                this.cells[row][col] = new Cell(this, row, col);
            }
            return this.cells[row][col];
        }

        protected Cell getCell(String reference) {
            reference = StringUtil.replaceAll(reference, "$", "");
            int[] cell = CellsHelper.cellNameToIndex((String)reference);
            return this.getCell(cell[0], cell[1]);
        }

        public void setRowNew(int row) {
            if (this.rows != null && row < this.rows.length && this.rows[row] != null) {
                this.rows[row].isNew = true;
            }
        }

        public void addPriorDeleteRow(int row) {
            SpreadsheetEditorModel.this.priorDeletedRows.add(row);
        }

        public ArrayList<Integer> getPriorDeleteRows() {
            return SpreadsheetEditorModel.this.priorDeletedRows;
        }

        public int deleteRow(int row) {
            int returnRow = -1;
            if (row < this.cells.length) {
                LinkedList temp = new LinkedList(Arrays.asList(this.cells));
                temp.remove(row);
                this.cells = (Cell[][])temp.toArray((T[])new Cell[0][]);
            }
            if (row < this.rows.length) {
                LinkedList<Row> temp2 = new LinkedList<Row>(Arrays.asList(this.rows));
                returnRow = ((Row)temp2.get((int)row)).originalRowNumber;
                temp2.remove(row);
                this.rows = temp2.toArray(new Row[0]);
            }
            --this.rowCount;
            return returnRow;
        }

        public class Column {
            int width;

            public Column() {
                this.width = Sheet.this.defaultColumnWidth;
            }
        }

        public class Row {
            int height;
            boolean isNew;
            int originalRowNumber;

            public Row(int originalRowNumber) {
                this.height = Sheet.this.defaultRowHeight;
                this.isNew = false;
                this.originalRowNumber = originalRowNumber;
            }
        }
    }
}

