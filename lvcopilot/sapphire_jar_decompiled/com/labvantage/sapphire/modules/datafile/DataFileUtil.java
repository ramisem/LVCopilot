/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.datafile;

import com.labvantage.sapphire.modules.datafile.ExcelFileReader;
import com.labvantage.sapphire.modules.datafile.TextFileReader;
import com.labvantage.sapphire.util.file.FileManager;
import java.io.File;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;

public class DataFileUtil {
    public static final String DEFAULT_DELIMITER = ",";
    public static final char DEFAULT_QUOTE = '\"';

    public static DataSet readTxtFile(ConnectionInfo connectionInfo, String location, String fileName, String delimiter, String encoding) throws SapphireException {
        TextFileReader reader = new TextFileReader(connectionInfo, location, fileName, -1, delimiter);
        reader.setFileEncoding(encoding != null ? encoding : "UTF-8");
        return reader.getFileContent();
    }

    public static DataSet readTxtFile(ConnectionInfo connectionInfo, String tempid, String delimiter, String encoding) throws SapphireException {
        TextFileReader reader = new TextFileReader(connectionInfo, "T", tempid, "", -1, delimiter);
        reader.setFileEncoding(encoding != null ? encoding : "UTF-8");
        return reader.getFileContent();
    }

    public static DataSet readExcelFile(ConnectionInfo connectionInfo, String location, String fileName, String worksheetnameornum) throws SapphireException {
        ExcelFileReader reader = new ExcelFileReader(connectionInfo, location, fileName, worksheetnameornum, -1);
        reader.initialize();
        return reader.getFileContent();
    }

    public static DataSet readExcelFile(ConnectionInfo connectionInfo, String tempid, String worksheetnameornum) throws SapphireException {
        ExcelFileReader reader = new ExcelFileReader(connectionInfo, "T", tempid, "", worksheetnameornum, -1);
        reader.initialize();
        return reader.getFileContent();
    }

    public static boolean checkFile(long largefilesize, String fullFileName) throws SapphireException {
        File file = new File(fullFileName);
        if (file.length() > largefilesize) {
            throw new SapphireException("File is too large to be used as an example file.");
        }
        return true;
    }

    public static boolean checkFile(long largefilesize, String tempid, QueryProcessor qp, String connectionId) throws SapphireException {
        FileManager.TempFile tempFile = FileManager.TempFile.getTempFile(tempid, false, qp, connectionId);
        if (tempFile != null && tempFile.getData().getSize() > largefilesize) {
            throw new SapphireException("File is too large to be used as an example file.");
        }
        return true;
    }

    public static String getColumnName(int num) {
        String str = "";
        while (num > 0) {
            int remainder = (num - 1) % 26;
            num = (num - 1) / 26;
            char c = (char)(65 + remainder);
            str = c + str;
        }
        return str;
    }

    public static int getColumnNum(String name) {
        int columnNum = -1;
        try {
            columnNum = Integer.parseInt(name);
        }
        catch (NumberFormatException e) {
            int val = 0;
            for (int i = 0; i < name.length(); ++i) {
                val += (name.charAt(i) - 65 + 1) * DataFileUtil.power(26, name.length() - i - 1);
            }
            return val;
        }
        return columnNum;
    }

    private static int power(int x, int y) {
        int val = 1;
        for (int i = 0; i < y; ++i) {
            val *= x;
        }
        return val;
    }

    public static DataSet sanitizeDS(DataSet ds) {
        int lastRow;
        int lastValidRow = -1;
        for (int currRow = lastRow = ds.getRowCount() - 1; currRow >= 0; --currRow) {
            String[] columns = ds.getColumns();
            for (int currCol = 0; currCol < columns.length; ++currCol) {
                String val = ds.getString(currRow, columns[currCol], "");
                if (val.length() <= 0) continue;
                lastValidRow = currRow;
                break;
            }
            if (lastValidRow != -1) break;
            ds.deleteRow(currRow);
        }
        int lastValidColumn = -1;
        int lastColumn = ds.getColumnCount();
        DataSet newDS = new DataSet();
        for (int currCol = lastColumn; currCol >= 0; --currCol) {
            int rowCount = ds.getRowCount();
            for (int currRow = 0; currRow < rowCount; ++currRow) {
                String val = ds.getString(currRow, DataFileUtil.getColumnName(currCol), "");
                if (val.length() <= 0) continue;
                lastValidColumn = currCol;
                break;
            }
            if (lastValidColumn != -1) break;
        }
        for (int i = 1; i <= lastValidColumn; ++i) {
            newDS.addColumnValues(DataFileUtil.getColumnName(i), 0, ds.getColumnValues(DataFileUtil.getColumnName(i), ";"), ";");
        }
        return newDS;
    }

    public static DataSet convertToGrid(DataSet raw) {
        String[] colnames = new String[raw.getColumnCount()];
        for (int i = 0; i < raw.getColumnCount(); ++i) {
            colnames[i] = raw.getString(0, DataFileUtil.getColumnName(i + 1));
        }
        DataSet ds = new DataSet();
        for (int i = 1; i < raw.getRowCount(); ++i) {
            int curr = ds.addRow();
            for (int col = 0; col < raw.getColumnCount(); ++col) {
                ds.setString(curr, colnames[col], raw.getString(i, DataFileUtil.getColumnName(col + 1), ""));
            }
        }
        return ds;
    }

    public static void renderExcelSheet(StringBuffer out, DataSet rows, int startrownum, boolean ignoreheader) {
        out.append("<TABLE class=\"viewlist\" >\n");
        DataFileUtil.addListItems(out, rows, startrownum, ignoreheader);
        out.append("</TABLE>");
    }

    public static void renderExcelSheet(StringBuffer out, DataSet rows) {
        out.append("<TABLE class=\"viewlist\" >\n");
        DataFileUtil.addListItems(out, rows);
        out.append("</TABLE>");
    }

    private static void addListItems(StringBuffer content, DataSet listItems) {
        DataFileUtil.addListItems(content, listItems, 2, false);
    }

    private static void addListItems(StringBuffer content, DataSet listItems, int startrownum, boolean ignoreheader) {
        int i;
        int rows = listItems.getRowCount();
        int cols = listItems.getColumnCount();
        String[] colList = listItems.getColumns();
        content.append("<THEAD>\n");
        content.append("<th class=\"viewlisthead\">Row#</th>\n");
        for (i = 0; i < colList.length; ++i) {
            if (colList[i].startsWith("__")) continue;
            String colHeader = colList[i].toUpperCase();
            String str = "<th class=\"viewlisthead\">" + colHeader + "</th>\n";
            content.append(str);
        }
        content.append("</THEAD>");
        for (i = 0; i < rows; ++i) {
            if (ignoreheader && i == 0) continue;
            content.append("<TR VALIGN=TOP >\n");
            content.append("<td class=\"viewlistcol\">" + (i + startrownum - 1) + "</td>");
            for (int j = 0; j < cols; ++j) {
                if (colList[j].startsWith("__")) continue;
                String columnValue = listItems.getValue(i, colList[j]);
                content.append("<td class=\"viewlistcol\">\n");
                content.append(columnValue);
                content.append("</td>\n");
            }
            content.append("</TR>\n");
        }
    }
}

