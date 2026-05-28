/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.spreadsheeteditor;

import com.labvantage.sapphire.pageelements.spreadsheeteditor.SpreadsheetEditorModel;
import java.util.ArrayList;
import java.util.HashSet;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.StringUtil;

public class SpreadsheetEditorDiffer {
    private final String text_newsheet;
    private final String text_deletesheet;
    private final String text_nochanges;
    private final String text_rowdeleted;
    private final String text_rowinserted;
    private final String text_rowinsertednotshown;
    private final String text_merge;
    private TranslationProcessor tp;
    private SpreadsheetEditorModel model1;
    private SpreadsheetEditorModel model2;
    private String prefix;
    private String id;
    public static final int DIFF_FORMULA = 1;
    public static final int DIFF_VALUE = 2;
    public static final int DIFF_NEWROW = 3;
    public static final int DIFF_NEWROWNOTSHOWN = 4;
    public static final int DIFF_DELETEDROW = 5;
    public static final int DIFF_MERGE = 6;

    public void setId(String id) {
        this.id = id;
    }

    public SpreadsheetEditorDiffer(SpreadsheetEditorModel model1, SpreadsheetEditorModel model2, String prefix, TranslationProcessor tp) {
        this.model1 = model1;
        this.model2 = model2;
        this.prefix = prefix;
        this.text_newsheet = tp.translate("Sheet \"[name]\" was created");
        this.text_deletesheet = tp.translate("Sheet \"[name]\" was deleted");
        this.text_nochanges = tp.translate("No formula or value changes detected.");
        this.text_rowdeleted = tp.translate("Original row [row] was deleted");
        this.text_rowinserted = tp.translate("Row [row] inserted");
        this.text_rowinsertednotshown = tp.translate("Row [row] added or inserted (not shown)");
        this.text_merge = tp.translate("merged or unmerged");
        this.tp = tp;
    }

    public ArrayList<DiffRecord> diffSheets(SpreadsheetEditorModel.Sheet sheet1, SpreadsheetEditorModel.Sheet sheet2) {
        ArrayList<DiffRecord> diffs = new ArrayList<DiffRecord>();
        String sheetname1 = sheet1.name;
        String sheetname2 = sheet2.name;
        int fromRow = 0;
        int fromColumn = 0;
        int rows = sheet1.rowCount;
        int cols = sheet1.colCount;
        ArrayList<Integer> priorDeletes = sheet1.getPriorDeleteRows();
        if (priorDeletes != null && priorDeletes.size() > 0) {
            for (int row : priorDeletes) {
                if (sheet1.isRowNew(row)) continue;
                int originalRow = sheet2.deleteRow(row);
                diffs.add(new DiffRecord("", sheet1, row, 0, 5, "" + (originalRow + 1), ""));
            }
        }
        for (int col = fromColumn; col <= cols; ++col) {
            int newRowCount = 0;
            for (int row = fromRow; row <= rows; ++row) {
                String formula2;
                if (sheet1.isRowNew(row)) {
                    if (col == fromColumn) {
                        String rowid = this.prefix + "spread_" + this.id + "_" + sheetname1 + "_row" + row;
                        if (row < sheet1.firstContentRow || row > sheet1.lastContentRow) {
                            diffs.add(new DiffRecord(rowid, sheet1, row, 0, 4, "" + (row + 1), ""));
                        } else {
                            diffs.add(new DiffRecord(rowid, sheet1, row, 0, 3, "" + (row + 1), ""));
                        }
                    }
                    ++newRowCount;
                    continue;
                }
                int compareRow = row - newRowCount;
                String cellid = this.prefix + "spread_" + this.id + "_" + sheetname1 + "_" + row + "_" + col;
                sheet1.getCell(row, col);
                SpreadsheetEditorModel.Cell cell1 = sheet1.getCell(row, col);
                SpreadsheetEditorModel.Cell cell2 = sheet2.getCell(compareRow, col);
                boolean cell1exists = cell1 != null && !cell1.isHiddenBySpan;
                boolean cell2exists = cell2 != null && !cell2.isHiddenBySpan;
                String displayText1 = cell1exists ? cell1.getDisplayText() : "";
                String formula1 = cell1exists ? cell1.formula : "";
                String displayText2 = cell2exists ? cell2.getDisplayText() : "";
                String string = formula2 = cell2exists ? cell2.formula : "";
                if (!formula1.equals(formula2)) {
                    diffs.add(new DiffRecord(cellid, sheet1, row, col, 1, formula1, formula2));
                }
                if (!displayText1.equals(displayText2)) {
                    diffs.add(new DiffRecord(cellid, sheet1, row, col, 2, displayText1, displayText2));
                }
                if (!cell1exists || !cell2exists || cell1.colSpan == cell2.colSpan && cell1.rowSpan == cell2.rowSpan) continue;
                diffs.add(new DiffRecord(cellid, sheet1, row, col, 6, "", ""));
            }
        }
        return diffs;
    }

    public String getDiffHTML() {
        StringBuilder out = new StringBuilder();
        HashSet<String> foundSheets = new HashSet<String>();
        boolean hideSheetTitle = this.model1.sheets.size() == 1 && this.model2.sheets.size() == 1 && this.model1.sheets.get((int)0).name.equals(this.model2.sheets.get((int)0).name);
        for (SpreadsheetEditorModel.Sheet sheet1 : this.model1.sheets) {
            foundSheets.add(sheet1.name);
            SpreadsheetEditorModel.Sheet sheet2 = this.model2.getSheetByName(sheet1.name);
            if (!hideSheetTitle) {
                out.append("<h1>" + sheet1.name + "</h1>");
            }
            if (sheet2 == null) {
                out.append(StringUtil.replaceAll(this.text_newsheet, "[name]", sheet1.name)).append("<br><br>");
                continue;
            }
            ArrayList<DiffRecord> diffs = this.diffSheets(sheet1, sheet2);
            out.append((CharSequence)this.getSheetDiffHTML(diffs, this.tp));
            out.append("<br><br>");
        }
        for (SpreadsheetEditorModel.Sheet sheet2 : this.model2.sheets) {
            if (foundSheets.contains(sheet2.name)) continue;
            if (!hideSheetTitle) {
                out.append("<h1>" + sheet2.name + "</h1>");
            }
            out.append(StringUtil.replaceAll(this.text_deletesheet, "[name]", sheet2.name)).append("<br><br>");
        }
        return out.toString();
    }

    private StringBuilder getSheetDiffHTML(ArrayList<DiffRecord> diffs, TranslationProcessor tp) {
        if (diffs.size() == 0) {
            return new StringBuilder(this.text_nochanges);
        }
        StringBuilder out = new StringBuilder();
        out.append("<table>");
        for (DiffRecord record : diffs) {
            String change = "";
            switch (record.type) {
                case 5: {
                    change = change + StringUtil.replaceAll(this.text_rowdeleted, "[row]", record.newvalue);
                    break;
                }
                case 3: {
                    change = change + StringUtil.replaceAll(this.text_rowinserted, "[row]", record.newvalue);
                    break;
                }
                case 4: {
                    change = change + StringUtil.replaceAll(this.text_rowinsertednotshown, "[row]", record.newvalue);
                    break;
                }
                case 6: {
                    change = SpreadsheetEditorDiffer.getExcelColumnName(record.col + 1) + (record.row + 1) + " " + this.text_merge;
                    break;
                }
                case 1: 
                case 2: {
                    change = SpreadsheetEditorDiffer.getExcelColumnName(record.col + 1) + (record.row + 1) + " ";
                    change = change + tp.translate(record.type == 1 ? "formula" : "value");
                    change = record.newvalue.length() == 0 ? change + " " + record.oldvalue + tp.translate(" removed") : (record.oldvalue.length() == 0 ? change + " " + record.newvalue + tp.translate(" added") : change + tp.translate(" changed: ") + record.oldvalue + " -> " + record.newvalue);
                }
            }
            out.append("<tr style=\"border-botton:1px solid darkgray\">");
            out.append("<td onmouseover=\"var o=document.getElementById('" + record.id + "');if (o){o.setAttribute('priorcolor', o.style.backgroundColor);o.style.backgroundColor='wheat';o.scrollIntoView()}\" onmouseout=\"var o=document.getElementById('" + record.id + "');if(o){o.style.backgroundColor=o.getAttribute('priorcolor')}\">" + change + "</td>");
            out.append("</tr>");
        }
        out.append("</table>");
        return out;
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

    class DiffRecord {
        private String id;
        protected SpreadsheetEditorModel.Sheet sheet;
        protected int row;
        protected int col;
        protected int type;
        protected String newvalue;
        protected String oldvalue;

        public DiffRecord(String id, SpreadsheetEditorModel.Sheet sheet, int row, int col, int type, String newvalue, String oldvalue) {
            this.id = id;
            this.sheet = sheet;
            this.row = row;
            this.col = col;
            this.type = type;
            this.newvalue = newvalue;
            this.oldvalue = oldvalue;
        }
    }
}

