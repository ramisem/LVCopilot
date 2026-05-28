/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.aspose.cells.Cell
 *  com.aspose.cells.DateTime
 *  com.aspose.cells.Row
 *  com.aspose.cells.Workbook
 *  com.aspose.cells.Worksheet
 */
package com.labvantage.sapphire.modules.datafile;

import com.aspose.cells.Cell;
import com.aspose.cells.DateTime;
import com.aspose.cells.Row;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.datafile.DataFile;
import com.labvantage.sapphire.modules.datafile.DataFileReader;
import com.labvantage.sapphire.modules.datafile.DataFileUtil;
import com.labvantage.sapphire.util.UnitsUtil;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.format.DateFormatter;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.StringUtil;

public class ExcelFileReader
extends DataFileReader {
    private String worksheet = "";
    private Worksheet sheet = null;
    private InputStream fis;
    FormatUtil formatUtil;
    boolean isComposite = false;
    private String[] sheetNames = null;

    public ExcelFileReader(ConnectionInfo connectionInfo, String filepath, String filename, String worksheet, int sliceSize) throws SapphireException {
        super(connectionInfo, filepath, filename, sliceSize);
        this.worksheet = worksheet;
        this.formatUtil = FormatUtil.getInstance(connectionInfo);
    }

    public ExcelFileReader(ConnectionInfo connectionInfo, String inputtype, String tempid, String messagelogid, String worksheet, int sliceSize) throws SapphireException {
        super(connectionInfo, inputtype, tempid, messagelogid, sliceSize);
        this.worksheet = worksheet;
        this.formatUtil = FormatUtil.getInstance(connectionInfo);
    }

    public ExcelFileReader(ConnectionInfo connectionInfo, String inputtype, String tempid, String messagelogid, boolean isComposite) throws SapphireException {
        super(connectionInfo, inputtype, tempid, messagelogid, -1);
        this.formatUtil = FormatUtil.getInstance(connectionInfo);
        this.isComposite = isComposite;
    }

    public ExcelFileReader(ConnectionInfo connectionInfo, String filepath, String filename, boolean isComposite) throws SapphireException {
        super(connectionInfo, filepath, filename, -1);
        this.formatUtil = FormatUtil.getInstance(connectionInfo);
        this.isComposite = isComposite;
    }

    @Override
    public void finalize() throws Throwable {
        try {
            if (this.fis != null) {
                this.fis.close();
            }
        }
        catch (IOException e) {
            Trace.log("Failed to close excel file:" + e.getMessage());
        }
        super.finalize();
    }

    public Worksheet getSheet() {
        return this.sheet;
    }

    public String[] getSheetNames() {
        return this.sheetNames;
    }

    @Override
    public void initialize() throws SapphireException {
        block34: {
            this.fis = null;
            String fileext = "";
            try {
                Workbook currentWb;
                String filename;
                int index;
                FileManager.FileData fileData;
                FileManager.TempFile tempFile;
                if (this.inputtype.equals("T")) {
                    tempFile = FileManager.TempFile.getTempFile(this.tempid, false, new QueryProcessor(this.connectionId), this.connectionId);
                    if (tempFile != null) {
                        fileData = tempFile.getData();
                        if (fileData != null) {
                            this.fis = new FileInputStream(fileData.getFile().toFile());
                        }
                        if ((index = (filename = tempFile.getFileName()).lastIndexOf("xls")) > 0) {
                            fileext = filename.substring(index);
                        }
                    }
                } else if (this.inputtype.equals("F")) {
                    this.fis = new FileInputStream(this.filepath + "/" + this.filename);
                    fileext = this.filename.substring(this.filename.lastIndexOf("xls"));
                } else {
                    tempFile = FileManager.getAttachment("LV_MessageLog", this.messagelogid, "", "", 1, this.connectionId);
                    if (tempFile != null) {
                        fileData = tempFile.getData();
                        if (fileData != null) {
                            this.fis = new ByteArrayInputStream(fileData.getData());
                        }
                        if ((index = (filename = tempFile.getFileName()).lastIndexOf("xls")) > 0) {
                            fileext = filename.substring(index);
                        }
                    }
                }
                if (!this.isComposite) {
                    try {
                        if (!fileext.contains("xlsx") && !fileext.contains("xls")) {
                            throw new SapphireException("Invalid file extension for excel file.");
                        }
                        currentWb = new Workbook(this.fis);
                    }
                    catch (Exception e) {
                        throw new SapphireException("Failed to create workbook", e);
                    }
                    if (this.worksheet.length() == 0) {
                        throw new SapphireException("Worksheet not specified.");
                    }
                    this.sheet = currentWb.getWorksheets().get(this.worksheet);
                    if (this.sheet == null) {
                        int sheetnum = -1;
                        try {
                            sheetnum = Integer.parseInt(this.worksheet);
                        }
                        catch (NumberFormatException filename2) {
                            // empty catch block
                        }
                        if (sheetnum == -1) {
                            throw new SapphireException("Excel file does not have worksheet:" + this.worksheet);
                        }
                        this.sheet = currentWb.getWorksheets().get(sheetnum);
                    }
                    if (this.sheet == null) {
                        throw new SapphireException("Excel file does not have worksheet:" + this.worksheet);
                    }
                    this.getLineCount();
                    break block34;
                }
                try {
                    if (!fileext.contains("xlsx") && !fileext.contains("xls")) {
                        throw new SapphireException("Invalid file extension for excel file.");
                    }
                    currentWb = new Workbook(this.fis);
                }
                catch (Exception e) {
                    throw new SapphireException("Failed to create workbook", e);
                }
                int sheetcount = currentWb.getWorksheets().getCount();
                this.sheetNames = new String[sheetcount];
                for (int i = 0; i < sheetcount; ++i) {
                    this.sheetNames[i] = currentWb.getWorksheets().get(i).getName();
                }
            }
            catch (FileNotFoundException e) {
                throw new SapphireException("Failed to read excel file:" + this.filepath + "/" + this.filename);
            }
            finally {
                if (this.fis != null) {
                    try {
                        this.fis.close();
                    }
                    catch (IOException e) {
                        Trace.log("Failed to close file stream:" + e.getMessage());
                    }
                }
            }
        }
    }

    public void fetchDataFileParams() throws SapphireException {
        DataSet allRows = this.getRawLines(1, this.getLineCount());
        this.fbr = this.getFBR(allRows);
        this.fbc = this.getFBC(allRows);
        this.colcount = this.getColCount(allRows);
        this.eof = this.getEOF(allRows);
    }

    @Override
    public int getLineCount() throws SapphireException {
        if (this.totalLineCount == -1) {
            this.totalLineCount = this.getRawContent().getRowCount();
        }
        return this.totalLineCount;
    }

    @Override
    public DataSet getLines(int start, int linecount, DataFile dataFile) throws SapphireException {
        DataSet ds = new DataSet();
        int end = Math.min(linecount, this.getLineCount());
        for (int i = start; i <= end; ++i) {
            DataSet row = this.getRow(i, dataFile);
            ds.copyRow(row, 0, 1);
        }
        return ds;
    }

    @Override
    public DataSet getRawLines(int start, int linecount) throws SapphireException {
        DataSet ds = new DataSet();
        ds.setColidCaseSensitive(true);
        int end = Math.min(linecount, this.getLineCount());
        for (int i = 0; i < end; ++i) {
            int currrow = ds.addRow();
            try {
                Row row = this.sheet.getCells().getRows().get(start + i - 1);
                if (row == null) continue;
                int lastCell = row.getLastCell() != null ? row.getLastCell().getColumn() : -1;
                try {
                    for (int physicalCellNum = 0; physicalCellNum <= lastCell; ++physicalCellNum) {
                        Cell cell = row.get((int)((short)physicalCellNum));
                        if (cell != null) {
                            String val;
                            short actualColNum = (short)(physicalCellNum + 1);
                            if (cell.getType() == 5) {
                                val = cell.getStringValue();
                                ds.setString(currrow, DataFileUtil.getColumnName(actualColNum), val);
                                continue;
                            }
                            if (this.getCellType(this.sheet, cell) == 1) {
                                val = "";
                                Date date = cell.getDateTimeValue().toDate();
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(date);
                                val = DateFormatter.formatDateTime(cal);
                                ds.setString(currrow, DataFileUtil.getColumnName(actualColNum), "" + val);
                                continue;
                            }
                            if (cell.getType() == 4) {
                                val = cell.getDisplayStringValue();
                                ds.setString(currrow, DataFileUtil.getColumnName(actualColNum), val);
                                continue;
                            }
                            if (this.getCellType(this.sheet, cell) == 3) {
                                ds.setString(currrow, DataFileUtil.getColumnName(actualColNum), "");
                                continue;
                            }
                            if (this.getCellType(this.sheet, cell) != 0) continue;
                            val = Boolean.toString(cell.getBoolValue());
                            ds.setString(currrow, DataFileUtil.getColumnName(actualColNum), val);
                            continue;
                        }
                        ds.setString(currrow, DataFileUtil.getColumnName(physicalCellNum + 1), "");
                    }
                    continue;
                }
                catch (Exception e1) {
                    throw new SapphireException("Failed to load header row: " + e1.getMessage(), e1);
                }
            }
            catch (Exception e1) {
                throw new SapphireException("Failed:" + e1.getMessage(), e1);
            }
        }
        return DataFileUtil.sanitizeDS(ds);
    }

    public DataSet getRawContent() throws SapphireException {
        if (this.sheet == null) {
            throw new SapphireException("Need to call initialize before you try to fetch data");
        }
        DataSet ds = new DataSet();
        ds.setColidCaseSensitive(true);
        int rownum = 0;
        while (rownum <= this.sheet.getCells().getLastCell().getRow()) {
            int currrow = ds.addRow();
            try {
                Row row = this.sheet.getCells().getRows().get(rownum++);
                if (row == null) continue;
                int lastCell = row.getLastCell() != null ? row.getLastCell().getColumn() : -1;
                try {
                    for (int physicalCellNum = 0; physicalCellNum <= lastCell; ++physicalCellNum) {
                        Cell cell = row.get((int)((short)physicalCellNum));
                        if (cell != null) {
                            String val;
                            short actualColNum = (short)(physicalCellNum + 1);
                            if (this.getCellType(this.sheet, cell) == 5) {
                                val = cell.getStringValue();
                                ds.setString(currrow, DataFileUtil.getColumnName(actualColNum), val);
                                continue;
                            }
                            if (this.getCellType(this.sheet, cell) == 1) {
                                val = "";
                                Date date = cell.getDateTimeValue().toDate();
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(date);
                                val = DateFormatter.formatDateTime(cal);
                                ds.setString(currrow, DataFileUtil.getColumnName(actualColNum), "" + val);
                                continue;
                            }
                            if (cell.getType() == 4) {
                                val = cell.getDisplayStringValue();
                                ds.setString(currrow, DataFileUtil.getColumnName(actualColNum), val);
                                continue;
                            }
                            if (cell.getType() != 0) continue;
                            val = Boolean.toString(cell.getBoolValue());
                            ds.setString(currrow, DataFileUtil.getColumnName(actualColNum), val);
                            continue;
                        }
                        ds.setString(currrow, DataFileUtil.getColumnName(physicalCellNum + 1), "");
                    }
                }
                catch (Exception e1) {
                    throw new SapphireException("Failed to load header row: " + e1.getMessage(), e1);
                }
            }
            catch (Exception e1) {
                throw new SapphireException("Failed:" + e1.getMessage(), e1);
            }
        }
        return DataFileUtil.sanitizeDS(ds);
    }

    @Override
    public DataSet getFileContent(DataFile dataFile) throws SapphireException {
        DataSet ds = this.getLines(1, this.getLineCount(), dataFile);
        return DataFileUtil.sanitizeDS(ds);
    }

    private DataSet getRow(int inrow, DataFile dataFile) throws SapphireException {
        DataSet ds = new DataSet();
        int currrow = ds.addRow();
        Row row = this.sheet.getCells().getRows().get(inrow - 1);
        if (row != null) {
            int lastCell = row.getLastCell() != null ? row.getLastCell().getColumn() : -1;
            try {
                for (int physicalCellNum = 0; physicalCellNum <= lastCell; ++physicalCellNum) {
                    Cell cell = row.get((int)((short)physicalCellNum));
                    short actualColNum = (short)(physicalCellNum + 1);
                    if (cell != null) {
                        String val;
                        if (dataFile == null) {
                            val = this.getCellContent(cell, dataFile);
                            if (val == null) continue;
                            ds.setString(currrow, DataFileUtil.getColumnName(actualColNum), val);
                            continue;
                        }
                        val = this.getCellContentByField(inrow - 1, DataFileUtil.getColumnName(actualColNum), cell, dataFile);
                        if (val == null) continue;
                        ds.setString(currrow, DataFileUtil.getColumnName(actualColNum), val);
                        continue;
                    }
                    ds.setString(currrow, DataFileUtil.getColumnName(actualColNum), "");
                }
            }
            catch (SapphireException e1) {
                throw new SapphireException(e1.getMessage());
            }
        } else {
            Trace.log("Row was empty");
            return ds;
        }
        return ds;
    }

    private String getCellContent(Cell cell, DataFile dataFile) throws SapphireException {
        Trace.log("Trying to fetch cell content:" + DataFileUtil.getColumnName(cell.getColumn()) + (cell.getRow() + 1));
        try {
            String val = this.readM18NCellContent(cell, "string", dataFile);
            return val;
        }
        catch (SapphireException e) {
            Trace.logInfo("Got exception: " + e.getMessage());
            throw e;
        }
    }

    private String getCellContentByField(int rownumber, String columnname, Cell cell, DataFile dataFile) throws SapphireException {
        String dataTypeInfo = dataFile.getCellDataTypeInfo(rownumber, columnname, this);
        if (dataTypeInfo != null) {
            if (dataTypeInfo.length() == 0) {
                dataTypeInfo = "string";
            }
            String val = this.readM18NCellContent(cell, dataTypeInfo, dataFile);
            dataFile.parsingDone = true;
            return val;
        }
        throw new SapphireException("Failed to fetch cell datatype from map");
    }

    @Override
    public DataSet getHeaderRow(int headerrow) throws SapphireException {
        DataSet ds;
        block7: {
            ds = new DataSet();
            ds.addRow();
            try {
                Row row = this.sheet.getCells().getRows().get(headerrow - 1);
                if (row == null) break block7;
                int lastCell = row.getLastCell() != null ? row.getLastCell().getColumn() : -1;
                try {
                    for (int physicalCellNum = 0; physicalCellNum <= lastCell; ++physicalCellNum) {
                        Cell cell = row.get((int)((short)physicalCellNum));
                        if (cell != null) {
                            String val;
                            short actualColNum = (short)(physicalCellNum + 1);
                            if (cell.getType() == 5) {
                                val = cell.getStringValue();
                                ds.setString(0, DataFileUtil.getColumnName(actualColNum), val);
                                continue;
                            }
                            val = cell.getDisplayStringValue();
                            ds.setString(0, DataFileUtil.getColumnName(actualColNum), val);
                            continue;
                        }
                        ds.setString(0, DataFileUtil.getColumnName(physicalCellNum + 1), "");
                    }
                }
                catch (Exception e1) {
                    throw new SapphireException("Failed to load header row: " + e1.getMessage(), e1);
                }
            }
            catch (Exception e1) {
                throw new SapphireException("Failed:" + e1.getMessage(), e1);
            }
        }
        return ds;
    }

    @Override
    public DataSet getNextSlice(DataFile dataFile) throws SapphireException {
        int totalRowCount = this.getLineCount();
        DataSet ds = new DataSet();
        if (this.sliceSize == -1) {
            this.sliceSize = totalRowCount;
        }
        if (this.nextSliceStart == -1) {
            throw new SapphireException("Starting position has not be set.");
        }
        if (this.nextSliceStart > totalRowCount) {
            return ds;
        }
        try {
            int sliceStart = this.nextSliceStart;
            int sliceEnd = sliceStart + this.sliceSize - 1;
            if (sliceEnd > totalRowCount) {
                sliceEnd = totalRowCount;
            }
            for (int actualRowNum = sliceStart; actualRowNum <= sliceEnd; ++actualRowNum) {
                DataSet temp = this.getRow(actualRowNum, dataFile);
                ds.copyRow(temp, 0, 1);
            }
            this.nextSliceStart = sliceEnd + 1;
        }
        catch (SapphireException e1) {
            throw new SapphireException(e1.getMessage());
        }
        ds = DataFileUtil.sanitizeDS(ds);
        return ds;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public String readM18NCellContent(Cell cell, String dataTypeInfo, DataFile dataFile) throws SapphireException {
        String dataType = dataTypeInfo;
        String[] tokens = StringUtil.split(dataTypeInfo, "|!|");
        String datelocale = "";
        String dateformat = "";
        String datatz = "";
        String dateformaterroraction = "";
        String numberformaterroractopm = "";
        if (dataType.startsWith("date") && dataType.contains("|!|")) {
            dataType = tokens[0];
            datelocale = tokens[1];
            dateformat = tokens[2];
            dateformat = dateformat.replaceAll("#COLON#", ":");
            datatz = tokens[3];
            dateformaterroraction = tokens[4];
        }
        String decimalseparator = "";
        String groupseparator = "";
        if (dataType.startsWith("number") && dataType.contains("|!|")) {
            dataType = tokens[0];
            decimalseparator = tokens[1];
            groupseparator = tokens[2];
            numberformaterroractopm = tokens[3];
        }
        if (cell == null) return "";
        if (this.getCellType(this.sheet, cell) == 5) {
            if (dataType.equals("string")) {
                return cell.getStringValue();
            }
            if (dataType.startsWith("number")) {
                Trace.logDebug("Converting Cell type: (" + DataFileUtil.getColumnName(cell.getColumn() + 1) + "," + (cell.getRow() + 1) + ") celltype:CELL_TYPE_ERROR");
                char currentUserDecimalSeparator = this.formatUtil.getDecimalSeparator();
                char currentGroupSeparator = this.formatUtil.getGroupingSeparator();
                if (decimalseparator.length() == 0 || decimalseparator.equals(Character.valueOf(currentUserDecimalSeparator))) {
                    String value = cell.getStringValue();
                    try {
                        BigDecimal b = this.formatUtil.parseBigDecimal(value, currentUserDecimalSeparator, currentGroupSeparator, true, true);
                        return this.m18n.format(b);
                    }
                    catch (NumberFormatException e) {
                        if (numberformaterroractopm.length() != 0) return "ParseNumberFieldFailed:Decimal Separator(" + currentUserDecimalSeparator + ") Group Separator(" + currentGroupSeparator + ")" + ":" + numberformaterroractopm + ":" + value;
                        numberformaterroractopm = "Error";
                        return "ParseNumberFieldFailed:Decimal Separator(" + currentUserDecimalSeparator + ") Group Separator(" + currentGroupSeparator + ")" + ":" + numberformaterroractopm + ":" + value;
                    }
                }
                String value = cell.getStringValue();
                try {
                    if (groupseparator.length() == 0) {
                        BigDecimal b = this.formatUtil.parseBigDecimal(value, decimalseparator.charAt(0), currentGroupSeparator, false, false);
                        return this.m18n.format(b);
                    }
                    BigDecimal b = this.formatUtil.parseBigDecimal(value, decimalseparator.charAt(0), groupseparator.charAt(0), true, true);
                    return this.m18n.format(b);
                }
                catch (NumberFormatException e) {
                    if (numberformaterroractopm.length() != 0) return "ParseNumberFieldFailed:Decimal Separator(" + decimalseparator + ") Group Separator(" + groupseparator + ")" + ":" + numberformaterroractopm + ":" + value;
                    numberformaterroractopm = "Error";
                    return "ParseNumberFieldFailed:Decimal Separator(" + decimalseparator + ") Group Separator(" + groupseparator + ")" + ":" + numberformaterroractopm + ":" + value;
                }
            }
            if (!dataType.startsWith("date")) throw new SapphireException("Invalid data type specified for field:");
            String strvalue = cell.getDisplayStringValue();
            if (dataFile == null) return "";
            return dataFile.parseDateField(strvalue, datelocale, dateformat, datatz, dateformaterroraction);
        }
        if (this.getCellType(this.sheet, cell) == 1) {
            Calendar cal = ExcelFileReader.getJavaCalendar(cell.getDoubleValue(), datatz.length() == 0 ? this.m18n.getTimezone() : TimeZone.getTimeZone(datatz));
            if (dataType.equals("string")) {
                return cell.getDisplayStringValue();
            }
            if (dataType.equals("number")) {
                throw new SapphireException("Data type mismatch for numeric for number field, check cell(" + DataFileUtil.getColumnName(cell.getColumn()) + cell.getRow() + ")");
            }
            if (!dataType.equals("date")) return "";
            return this.m18n.format(cal);
        }
        if (this.getCellType(this.sheet, cell) == 4) {
            if (dataType.equals("string")) {
                return cell.getDisplayStringValue();
            }
            if (!dataType.equals("number")) throw new SapphireException("Data type mismatch for field, check cell(" + DataFileUtil.getColumnName(cell.getColumn()) + cell.getRow() + ")");
            String newValue = UnitsUtil.convertToLocateSeperated(String.valueOf(cell.getValue()), "" + this.formatUtil.getDecimalSeparator());
            BigDecimal b = this.m18n.parseBigDecimal(newValue);
            return this.m18n.format(b, false, true);
        }
        if (this.getCellType(this.sheet, cell) == 3) {
            return "";
        }
        if (this.getCellType(this.sheet, cell) == 0) {
            if (!dataType.equals("string")) throw new SapphireException("Data type mismatch for field,check cell(" + DataFileUtil.getColumnName(cell.getColumn()) + cell.getRow() + ")");
            if (!cell.getBoolValue()) return "N";
            return "Y";
        }
        if (this.getCellType(this.sheet, cell) == 2) {
            Trace.logDebug("Cell has unexpected type: (" + DataFileUtil.getColumnName(cell.getColumn() + 1) + "," + (cell.getRow() + 1) + ") celltype:CELL_TYPE_ERROR");
            return "";
        }
        Trace.logError("Cell has unexpected type: (" + DataFileUtil.getColumnName(cell.getColumn() + 1) + "," + (cell.getRow() + 1) + ") celltype:" + cell.getType());
        return "";
    }

    private int getCellType(Worksheet sheet, Cell cell) {
        if (cell.isFormula()) {
            try {
                sheet.calculateFormula(cell.getFormula());
                return cell.getType();
            }
            catch (Exception evalE) {
                try {
                    DateTime val = cell.getDateTimeValue();
                    return 1;
                }
                catch (IllegalStateException exp1) {
                    try {
                        String val = cell.getStringValue();
                        return 5;
                    }
                    catch (IllegalStateException exp2) {
                        try {
                            boolean val = cell.getBoolValue();
                            return 0;
                        }
                        catch (IllegalStateException exp3) {
                            try {
                                double val = cell.getDoubleValue();
                                return 4;
                            }
                            catch (IllegalStateException exp4) {
                                return 2;
                            }
                        }
                    }
                }
            }
        }
        return cell.getType();
    }

    public static Calendar getJavaCalendar(double date, TimeZone timeZone) {
        int wholeDays = (int)Math.floor(date);
        int millisecondsInDay = (int)((date - (double)wholeDays) * 8.64E7 + 0.5);
        GregorianCalendar calendar = timeZone != null ? new GregorianCalendar(timeZone) : new GregorianCalendar();
        ExcelFileReader.setCalendar(calendar, wholeDays, millisecondsInDay);
        return calendar;
    }

    public static void setCalendar(Calendar calendar, int wholeDays, int millisecondsInDay) {
        int startYear = 1900;
        int dayAdjust = -1;
        if (wholeDays < 61) {
            dayAdjust = 0;
        }
        calendar.set(startYear, 0, wholeDays + dayAdjust, 0, 0, 0);
        calendar.set(14, millisecondsInDay);
    }
}

