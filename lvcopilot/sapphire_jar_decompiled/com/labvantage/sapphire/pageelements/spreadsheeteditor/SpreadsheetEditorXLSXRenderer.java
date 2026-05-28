/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.aspose.cells.Cell
 *  com.aspose.cells.CellsHelper
 *  com.aspose.cells.Color
 *  com.aspose.cells.Font
 *  com.aspose.cells.Name
 *  com.aspose.cells.NameCollection
 *  com.aspose.cells.Range
 *  com.aspose.cells.Row
 *  com.aspose.cells.Style
 *  com.aspose.cells.Workbook
 *  com.aspose.cells.Worksheet
 *  com.aspose.cells.WorksheetCollection
 */
package com.labvantage.sapphire.pageelements.spreadsheeteditor;

import com.aspose.cells.Cell;
import com.aspose.cells.CellsHelper;
import com.aspose.cells.Color;
import com.aspose.cells.Font;
import com.aspose.cells.Name;
import com.aspose.cells.NameCollection;
import com.aspose.cells.Range;
import com.aspose.cells.Row;
import com.aspose.cells.Style;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.aspose.cells.WorksheetCollection;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.pageelements.spreadsheeteditor.SpreadsheetEditorModel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import sapphire.util.StringUtil;

public class SpreadsheetEditorXLSXRenderer {
    SpreadsheetEditorModel model;
    public static final short EXCEL_COLUMN_WIDTH_FACTOR = 1;
    public static final short EXCEL_ROW_HEIGHT_FACTOR = 20;
    public static final int UNIT_OFFSET_LENGTH = 7;
    public static final int[] UNIT_OFFSET_MAP = new int[]{0, 36, 73, 109, 146, 182, 219};

    public SpreadsheetEditorXLSXRenderer(SpreadsheetEditorModel model) {
        this.model = model;
    }

    public Workbook toWorkbook() {
        Workbook workbook = new Workbook();
        workbook.getWorksheets().removeAt(0);
        for (SpreadsheetEditorModel.Sheet sheet : this.model.sheets) {
            WorksheetCollection worksheetCollection = workbook.getWorksheets();
            Worksheet xlSheet = worksheetCollection.add(sheet.name);
            this.buildExcelSheet(sheet, xlSheet, workbook);
        }
        return workbook;
    }

    public void buildExcelSheet(SpreadsheetEditorModel.Sheet sheet, Worksheet xsheet, Workbook workbook) {
        for (int row = 0; row <= sheet.lastContentRow; ++row) {
            int col;
            xsheet.getCells().insertRow(row);
            Row sheetRow = xsheet.getCells().getRows().get(row);
            sheetRow.setHeight((double)((float)sheet.getRowHeight(row) / 1.3f));
            for (col = 0; col <= sheet.lastContentCol; ++col) {
                int width = sheet.getColumnWidth(col);
                xsheet.getCells().setColumnWidthPixel(col, width);
            }
            for (col = 0; col <= sheet.lastContentCol; ++col) {
                SpreadsheetEditorModel.Cell cell = sheet.getCell(row, col);
                if (!cell.isHiddenBySpan) {
                    Cell xcell = sheetRow.get(col);
                    try {
                        sheetRow.setHeight((double)((float)sheet.getRowHeight(row) / 1.3f));
                        if (cell.fieldid.length() > 0) {
                            NameCollection names = workbook.getWorksheets().getNames();
                            Name name = names.get(names.add(StringUtil.replaceAll(cell.fieldid, " ", "_")));
                            String reference = xsheet.getName() + "!$" + CellsHelper.columnIndexToName((int)xcell.getColumn()) + "$" + (xcell.getRow() + 1);
                            name.setRefersTo(reference);
                        }
                        boolean hasFormula = false;
                        boolean hasValue = false;
                        boolean isDate = false;
                        boolean isNumber = false;
                        boolean isSparkline = false;
                        if (cell.formula != null && cell.formula.length() > 0) {
                            if (cell.formula.toUpperCase().contains("SPARKLINE")) {
                                xcell.setValue((Object)"SPARKLINE");
                                hasValue = true;
                                isSparkline = true;
                            } else {
                                xcell.setFormula(cell.formula);
                                hasFormula = true;
                            }
                        } else if (cell.value != null) {
                            hasValue = true;
                            if (cell.value instanceof String) {
                                xcell.setValue((Object)((String)cell.value));
                            } else if (cell.value instanceof Date) {
                                xcell.setValue((Object)((Date)cell.value));
                                isDate = true;
                            } else {
                                xcell.setValue((Object)new Double("" + cell.value));
                                isNumber = true;
                            }
                        }
                        if (cell.rowSpan > 1 || cell.colSpan > 1) {
                            Range range = xsheet.getCells().createRange(row - 1, col, cell.rowSpan - 1, cell.colSpan - 1);
                        }
                        Style style = workbook.createStyle();
                        if (cell.format != null && cell.format.length() > 0) {
                            style.setCustom(cell.format);
                        } else if (isDate && (cell.format == null || cell.format.length() == 0)) {
                            style.setCustom(((SimpleDateFormat)DateFormat.getDateInstance(3, this.model.m18n.getLocale())).toPattern());
                        }
                        style.setTextWrapped(cell.wordWrap);
                        if (cell.borderTop.length() > 0) {
                            style.setBorder(this.getBorderStyle(cell.borderTop), 4, this.getBorderColor(cell.borderTop));
                        }
                        if (cell.borderRight.length() > 0) {
                            style.setBorder(this.getBorderStyle(cell.borderRight), 2, this.getBorderColor(cell.borderRight));
                        }
                        if (cell.borderBottom.length() > 0) {
                            style.setBorder(this.getBorderStyle(cell.borderBottom), 8, this.getBorderColor(cell.borderBottom));
                        }
                        if (cell.borderLeft.length() > 0) {
                            style.setBorder(this.getBorderStyle(cell.borderLeft), 1, this.getBorderColor(cell.borderLeft));
                        }
                        if (isSparkline) {
                            style.setHorizontalAlignment(1);
                            style.setVerticalAlignment(1);
                        } else {
                            if (cell.hAlign.length() > 0) {
                                style.setHorizontalAlignment(cell.hAlign.equals("left") ? 7 : (cell.hAlign.equals("center") ? 1 : (cell.hAlign.equals("right") ? 8 : 5)));
                            } else {
                                style.setHorizontalAlignment(isNumber || isDate ? 8 : 7);
                            }
                            if (cell.vAlign.length() > 0) {
                                style.setVerticalAlignment(cell.vAlign.equals("top") ? 9 : (cell.vAlign.equals("middle") ? 1 : (cell.vAlign.equals("bottom") ? 0 : 9)));
                            }
                        }
                        if (cell.backColor.length() > 0) {
                            style.setPattern(1);
                            style.setForegroundColor(Color.fromArgb((int)java.awt.Color.decode(cell.backColor).getRGB()));
                        }
                        Font font = style.getFont();
                        if (cell.isBold) {
                            font.setBold(true);
                        }
                        if (cell.isItalic) {
                            font.setItalic(true);
                        }
                        if (cell.isUnderline) {
                            font.setUnderline(1);
                        }
                        if (cell.isStrikethrough) {
                            font.setStrikeout(true);
                        }
                        if (cell.foreColor.length() > 0) {
                            java.awt.Color c1 = java.awt.Color.decode(cell.foreColor);
                            Color color = new Color();
                            font.setColor(Color.fromArgb((int)c1.getRed(), (int)c1.getGreen(), (int)c1.getBlue()));
                        }
                        xcell.setStyle(style);
                    }
                    catch (Exception e) {
                        Trace.logWarn("Failed to fully export to Excel. : " + e.getMessage());
                    }
                    continue;
                }
                Style style = workbook.createStyle();
                Cell xcell = sheetRow.get(col);
                if (cell.borderTop.length() > 0) {
                    style.setBorder(this.getBorderStyle(cell.borderTop), 4, null);
                }
                if (cell.borderRight.length() > 0) {
                    style.setBorder(this.getBorderStyle(cell.borderRight), 2, null);
                }
                if (cell.borderBottom.length() > 0) {
                    style.setBorder(this.getBorderStyle(cell.borderBottom), 8, null);
                }
                if (cell.borderLeft.length() > 0) {
                    style.setBorder(this.getBorderStyle(cell.borderLeft), 1, null);
                }
                xcell.setStyle(style);
            }
        }
    }

    private int getBorderStyle(String border) {
        String[] parts = border.split(" ");
        return parts[0].equals("1px") ? 1 : (parts[0].equals("2px") ? 2 : (parts[0].equals("3px") ? 5 : 1));
    }

    private Color getBorderColor(String border) {
        String[] parts = border.split(" ");
        String color = parts.length == 3 && parts[2].length() > 0 ? parts[2] : "black";
        try {
            if (color.startsWith("#")) {
                return Color.fromArgb((int)java.awt.Color.decode(color).getRGB());
            }
            return Color.fromArgb((int)((java.awt.Color)java.awt.Color.class.getField(color).get(null)).getRGB());
        }
        catch (Exception e) {
            return Color.fromArgb((int)java.awt.Color.decode("#000000").getRGB());
        }
    }
}

