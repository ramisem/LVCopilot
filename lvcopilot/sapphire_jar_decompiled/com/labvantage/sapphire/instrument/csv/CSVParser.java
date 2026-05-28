/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.instrument.csv;

import com.labvantage.sapphire.instrument.csv.BaseCSV;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.util.DataSet;

public class CSVParser
extends BaseCSV {
    private boolean inField = false;

    public CSVParser() {
    }

    public CSVParser(boolean headerrow, char separatorChar, char quoteChar, char escapeChar) {
        this.separator = separatorChar;
        this.quote = quoteChar;
        this.escape = escapeChar;
        this.headerrow = headerrow;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public DataSet parseCSVString(String csvString) throws SapphireException {
        DataSet dataSet;
        ByteArrayInputStream bais = new ByteArrayInputStream(csvString.getBytes(this.charset));
        try {
            dataSet = this.parseCSVStream(bais);
        }
        catch (Throwable throwable) {
            try {
                bais.close();
                throw throwable;
            }
            catch (Exception e) {
                throw new SapphireException("Could not parse BaseCSV String.", e);
            }
        }
        bais.close();
        return dataSet;
    }

    public DataSet parseCSVStream(InputStream is) throws SapphireException {
        DataSet out = new DataSet();
        out.setColidCaseSensitive(true);
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(is, this.charset));
            String line = null;
            int rowcount = 0;
            while ((line = in.readLine()) != null) {
                int i;
                int row;
                ArrayList<String> lineElements = this.parseLine(line);
                if (rowcount == 0) {
                    if (this.headerrow) {
                        for (int i2 = 0; i2 < lineElements.size(); ++i2) {
                            String columnname = lineElements.get(i2);
                            if (this.columnMapping.size() > 0 && this.columnMapping.containsKey(columnname)) {
                                columnname = (String)this.columnMapping.get(columnname);
                            }
                            out.addColumn(columnname, 0);
                        }
                    } else {
                        row = out.addRow();
                        for (i = 0; i < lineElements.size(); ++i) {
                            if (this.columnMapping.size() > 0) {
                                if (this.columnMapping.containsKey("" + (i + 1))) {
                                    out.addColumn((String)this.columnMapping.get("" + (i + 1)), 0);
                                } else if (this.columnMapping.containsKey("column" + (i + 1))) {
                                    out.addColumn((String)this.columnMapping.get("column" + (i + 1)), 0);
                                } else if (this.columnMapping.containsKey("col" + (i + 1))) {
                                    out.addColumn((String)this.columnMapping.get("col" + (i + 1)), 0);
                                } else {
                                    out.addColumn("column" + (i + 1), 0);
                                }
                            } else {
                                out.addColumn("column" + (i + 1), 0);
                            }
                            out.setValue(row, out.getColumnId(i), lineElements.get(i));
                        }
                    }
                } else {
                    row = out.addRow();
                    for (i = 0; i < lineElements.size(); ++i) {
                        out.setValue(row, out.getColumnId(i), lineElements.get(i));
                    }
                }
                ++rowcount;
            }
        }
        catch (Exception e) {
            throw new SapphireException("Could not parse BaseCSV input stream.", e);
        }
        return out;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public DataSet parseCSVFile(File csvFile) throws SapphireException {
        DataSet dataSet;
        FileInputStream fis = new FileInputStream(csvFile);
        try {
            dataSet = this.parseCSVStream(fis);
        }
        catch (Throwable throwable) {
            try {
                fis.close();
                throw throwable;
            }
            catch (Exception e) {
                throw new SapphireException("Could not parse BaseCSV file.", e);
            }
        }
        fis.close();
        return dataSet;
    }

    private boolean anyCharactersAreTheSame(char separator, char quotechar, char escape) {
        return this.isSameCharacter(separator, quotechar) || this.isSameCharacter(separator, escape) || this.isSameCharacter(quotechar, escape);
    }

    private boolean isSameCharacter(char c1, char c2) {
        return c1 != '\u0000' && c1 == c2;
    }

    private boolean isNextCharacterEscapedQuote(String nextLine, boolean inQuotes, int i) {
        return inQuotes && nextLine.length() > i + 1 && nextLine.charAt(i + 1) == this.quote;
    }

    private boolean isNextCharacterEscapable(String nextLine, boolean inQuotes, int i) {
        return inQuotes && nextLine.length() > i + 1 && (nextLine.charAt(i + 1) == this.quote || nextLine.charAt(i + 1) == this.escape);
    }

    private boolean isAllWhiteSpace(CharSequence sb) {
        boolean result = true;
        for (int i = 0; i < sb.length(); ++i) {
            char c = sb.charAt(i);
            if (Character.isWhitespace(c)) continue;
            return false;
        }
        return result;
    }

    private ArrayList<String> parseLine(String nextLine) throws IOException {
        ArrayList<String> tokensOnThisLine = new ArrayList<String>();
        StringBuffer sb = new StringBuffer();
        boolean inQuotes = false;
        for (int i = 0; i < nextLine.length(); ++i) {
            char c = nextLine.charAt(i);
            if (c == this.escape) {
                if (!this.isNextCharacterEscapable(nextLine, inQuotes || this.inField, i)) continue;
                sb.append(nextLine.charAt(i + 1));
                ++i;
                continue;
            }
            if (c == this.quote) {
                if (this.isNextCharacterEscapedQuote(nextLine, inQuotes || this.inField, i)) {
                    sb.append(nextLine.charAt(i + 1));
                    ++i;
                } else {
                    if (!this.strictQuotes && i > 2 && nextLine.charAt(i - 1) != this.separator && nextLine.length() > i + 1 && nextLine.charAt(i + 1) != this.separator) {
                        if (this.ignoreLeadingWhiteSpace && sb.length() > 0 && this.isAllWhiteSpace(sb)) {
                            sb.setLength(0);
                        } else {
                            sb.append(c);
                        }
                    }
                    inQuotes = !inQuotes;
                }
                this.inField = !this.inField;
                continue;
            }
            if (c == this.separator && !inQuotes) {
                tokensOnThisLine.add(sb.toString());
                sb.setLength(0);
                this.inField = false;
                continue;
            }
            if (this.strictQuotes && !inQuotes) continue;
            sb.append(c);
            this.inField = true;
        }
        if (sb != null) {
            tokensOnThisLine.add(sb.toString());
        }
        return tokensOnThisLine;
    }
}

