/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.instrument.csv;

import com.labvantage.sapphire.instrument.csv.BaseCSV;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import sapphire.SapphireException;
import sapphire.util.DataSet;

public class CSVWriter
extends BaseCSV {
    public CSVWriter() {
    }

    public CSVWriter(boolean headerrow, char separatorChar, char quoteChar, char escapeChar) {
        this.separator = separatorChar;
        this.quote = quoteChar;
        this.escape = escapeChar;
        this.headerrow = headerrow;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String writeCSVString(DataSet data) throws SapphireException {
        StringWriter outString = new StringWriter();
        try {
            PrintWriter out = new PrintWriter(outString);
            try {
                this.writeCSVToWriter(data, out);
            }
            finally {
                out.flush();
                out.close();
            }
        }
        catch (Exception e) {
            throw new SapphireException("Could not write BaseCSV to file.", e);
        }
        return outString.toString();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void writeCSVToFile(DataSet data, File fileOut) throws SapphireException {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(fileOut));
            try {
                this.writeCSVToWriter(data, out);
            }
            finally {
                out.flush();
                out.close();
            }
        }
        catch (Exception e) {
            throw new SapphireException("Could not write BaseCSV to file.", e);
        }
    }

    public void writeCSVToWriter(DataSet data, PrintWriter writer) throws SapphireException {
        try {
            String[] columns = data.getColumns();
            if (this.headerrow) {
                String[] writeCols;
                if (this.columnMapping.size() > 0) {
                    writeCols = (String[])columns.clone();
                    for (int i = 0; i < columns.length; ++i) {
                        if (!this.columnMapping.containsKey(columns[i])) continue;
                        writeCols[i] = (String)this.columnMapping.get(columns[i]);
                    }
                } else {
                    writeCols = columns;
                }
                this.writeNext(writeCols, writer);
            }
            for (int row = 0; row < data.getRowCount(); ++row) {
                String[] rowValues = new String[data.getColumnCount()];
                for (int i = 0; i < columns.length; ++i) {
                    rowValues[i] = data.getValue(row, columns[i], this.nullvalue);
                }
                this.writeNext(rowValues, writer);
            }
        }
        catch (Exception e) {
            throw new SapphireException("Could not write BaseCSV to writer.", e);
        }
    }

    private void writeNext(String[] row, PrintWriter writer) {
        if (row == null || row.length == 0) {
            return;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < row.length; ++i) {
            String nextElement;
            if (i != 0) {
                sb.append(this.separator);
            }
            if ((nextElement = row[i]) == null) continue;
            if (this.quote != '\u0000') {
                sb.append(this.quote);
            }
            sb.append(this.stringContainsSpecialCharacters(nextElement) ? this.processLine(nextElement) : nextElement);
            if (this.quote == '\u0000') continue;
            sb.append(this.quote);
        }
        sb.append(this.lineEnd);
        writer.write(sb.toString());
    }

    private boolean stringContainsSpecialCharacters(String line) {
        return line.indexOf(this.quote) != -1 || line.indexOf(this.escape) != -1;
    }

    private StringBuffer processLine(String nextElement) {
        StringBuffer sb = new StringBuffer();
        for (int j = 0; j < nextElement.length(); ++j) {
            char nextChar = nextElement.charAt(j);
            if (this.escape != '\u0000' && nextChar == this.quote) {
                sb.append(this.escape).append(nextChar);
                continue;
            }
            if (this.escape != '\u0000' && nextChar == this.escape) {
                sb.append(this.escape).append(nextChar);
                continue;
            }
            sb.append(nextChar);
        }
        return sb;
    }
}

