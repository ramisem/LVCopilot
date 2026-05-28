/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.datafile;

import com.labvantage.sapphire.modules.datafile.DataFile;
import com.labvantage.sapphire.modules.datafile.DataFileReader;
import com.labvantage.sapphire.modules.datafile.DataFileUtil;
import com.labvantage.sapphire.util.file.FileManager;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;

public class TextFileReader
extends DataFileReader {
    public static final String DEFAULT_DELIMITER = ",";
    public static final String DEFAULT_QUOTE = "\"";
    public static final String DEFAULT_ESCAPE = "\\";
    private BufferedReader inFile;
    private String delimiter;
    private String quote;
    private String escape;

    public TextFileReader(ConnectionInfo connectionInfo, String filepath, String filename, int sliceSize, String delimiter, String quote, String escape) {
        super(connectionInfo, filepath, filename, sliceSize);
        if (delimiter.equals("\\t")) {
            delimiter = "\t";
        }
        this.delimiter = delimiter;
        this.quote = quote;
        this.escape = escape;
    }

    public TextFileReader(ConnectionInfo connectionInfo, String filepath, String filename, int sliceSize, String delimiter) {
        super(connectionInfo, filepath, filename, sliceSize);
        if (delimiter.equals("\\t")) {
            delimiter = "\t";
        }
        this.delimiter = delimiter;
        this.quote = DEFAULT_QUOTE;
        this.escape = DEFAULT_ESCAPE;
    }

    public TextFileReader(ConnectionInfo connectionInfo, String inputtype, String tempid, String messagelogid, int sliceSize, String delimiter) throws SapphireException {
        super(connectionInfo, inputtype, tempid, messagelogid, sliceSize);
        if (delimiter.equals("\\t")) {
            delimiter = "\t";
        }
        this.delimiter = delimiter;
        this.quote = DEFAULT_QUOTE;
        this.escape = DEFAULT_ESCAPE;
    }

    @Override
    public void finalize() throws Throwable {
        super.finalize();
    }

    @Override
    public void initialize() throws SapphireException {
        this.totalLineCount = this.getLineCount();
    }

    @Override
    public int getLineCount() throws SapphireException {
        if (this.totalLineCount == -1) {
            int lineCount = 0;
            try {
                FileManager.FileData fileData;
                FileManager.TempFile tempFile;
                FileInputStream fis = null;
                if (this.inputtype.equals("T")) {
                    tempFile = FileManager.TempFile.getTempFile(this.tempid, false, new QueryProcessor(this.connectionId), this.connectionId);
                    if (tempFile != null) {
                        this.filename = tempFile.getFileName();
                        fileData = tempFile.getData();
                        if (fileData != null) {
                            fis = new FileInputStream(fileData.getFile().toFile());
                        }
                    }
                } else if (this.inputtype.equals("M")) {
                    tempFile = FileManager.getAttachment("LV_MessageLog", this.messagelogid, "", "", 1, this.connectionId);
                    if (tempFile != null) {
                        this.filename = tempFile.getFileName();
                        fileData = tempFile.getData();
                        if (fileData != null) {
                            fis = new FileInputStream(fileData.getFile().toFile());
                        }
                    }
                } else {
                    fis = new FileInputStream(this.filepath + "/" + this.filename);
                }
                this.inFile = new BufferedReader(new InputStreamReader((InputStream)fis, this.DEFAULT_ENCODING));
                String str = this.inFile.readLine();
                while (str != null) {
                    ++lineCount;
                    str = this.inFile.readLine();
                }
            }
            catch (Exception e) {
                throw new SapphireException("Failed to read file" + e.getMessage());
            }
            finally {
                try {
                    this.inFile.close();
                }
                catch (IOException iOException) {}
            }
            this.totalLineCount = lineCount;
        }
        return this.totalLineCount;
    }

    private FileInputStream getFileInputStream() throws FileNotFoundException {
        FileInputStream fis = null;
        if (this.inputtype.equals("T")) {
            FileManager.TempFile tempFile = FileManager.TempFile.getTempFile(this.tempid, false, new QueryProcessor(this.connectionId), this.connectionId);
            if (tempFile != null) {
                this.filename = tempFile.getFileName();
                FileManager.FileData fileData = tempFile.getData();
                if (fileData != null) {
                    fis = new FileInputStream(fileData.getFile().toFile());
                }
            }
        } else if (this.inputtype.equals("M")) {
            FileManager.TempFile tempFile = FileManager.getAttachment("LV_MessageLog", this.messagelogid, "", "", 1, this.connectionId);
            if (tempFile != null) {
                this.filename = tempFile.getFileName();
                FileManager.FileData fileData = tempFile.getData();
                if (fileData != null) {
                    fis = new FileInputStream(fileData.getFile().toFile());
                }
            }
        } else {
            fis = new FileInputStream(this.filepath + "/" + this.filename);
        }
        return fis;
    }

    @Override
    public DataSet getHeaderRow(int headerrow) throws SapphireException {
        DataSet ds = new DataSet();
        try {
            FileInputStream fis = this.getFileInputStream();
            this.inFile = new BufferedReader(new InputStreamReader((InputStream)fis, this.DEFAULT_ENCODING));
            String currLine = this.inFile.readLine();
            int currentLineRead = 1;
            while (currLine != null) {
                if (currentLineRead == headerrow) {
                    int newRow = ds.addRow();
                    ArrayList<String> tokens = this.parseLine(currLine);
                    for (int i = 0; i < tokens.size(); ++i) {
                        ds.setString(newRow, DataFileUtil.getColumnName(i + 1), tokens.get(i).toString());
                    }
                }
                currLine = this.inFile.readLine();
                ++currentLineRead;
            }
        }
        catch (Exception e1) {
            throw new SapphireException("Failed reading slice:" + e1.getMessage(), e1);
        }
        finally {
            try {
                this.inFile.close();
            }
            catch (IOException iOException) {}
        }
        return ds;
    }

    public int getSliceSize() {
        return this.sliceSize;
    }

    @Override
    public DataSet getNextSlice(DataFile dataFile) throws SapphireException {
        DataSet ds = new DataSet();
        ds.setColidCaseSensitive(true);
        if (this.sliceSize == -1) {
            this.sliceSize = this.getLineCount();
        }
        try {
            int sliceEnd;
            FileInputStream fis = this.getFileInputStream();
            this.inFile = new BufferedReader(new InputStreamReader((InputStream)fis, this.DEFAULT_ENCODING));
            int sliceStart = this.nextSliceStart;
            this.nextSliceStart = sliceEnd = sliceStart + this.sliceSize - 1;
            String currLine = this.inFile.readLine();
            int currentLineRead = 1;
            while (currLine != null) {
                if (currentLineRead >= sliceStart) {
                    int newRow = ds.addRow();
                    ArrayList<String> tokens = this.parseLine(currLine);
                    for (int i = 0; i < tokens.size(); ++i) {
                        ds.setString(newRow, DataFileUtil.getColumnName(i + 1), tokens.get(i).toString());
                    }
                }
                if (currentLineRead == sliceEnd) {
                    currLine = null;
                    continue;
                }
                currLine = this.inFile.readLine();
                ++currentLineRead;
            }
            this.nextSliceStart = sliceEnd + 1;
        }
        catch (Exception e1) {
            throw new SapphireException("Failed reading slice:" + e1.getMessage(), e1);
        }
        finally {
            try {
                this.inFile.close();
            }
            catch (IOException iOException) {}
        }
        return DataFileUtil.sanitizeDS(ds);
    }

    @Override
    public DataSet getRawLines(int start, int count) throws SapphireException {
        DataSet ds = new DataSet();
        ds.setColidCaseSensitive(true);
        try {
            FileInputStream fis = this.getFileInputStream();
            this.inFile = new BufferedReader(new InputStreamReader((InputStream)fis, this.DEFAULT_ENCODING));
            for (int i = 0; i < start - 1; ++i) {
                String string = this.inFile.readLine();
            }
            for (int j = 0; j < count; ++j) {
                String currLine = this.inFile.readLine();
                if (currLine == null) continue;
                int currrow = ds.addRow();
                ArrayList<String> tokens = this.parseLine(currLine);
                for (int i = 0; i < tokens.size(); ++i) {
                    ds.setString(currrow, DataFileUtil.getColumnName(i + 1), tokens.get(i).toString());
                }
            }
        }
        catch (Exception e1) {
            throw new SapphireException("Failed reading slice:" + e1.getMessage(), e1);
        }
        finally {
            try {
                this.inFile.close();
            }
            catch (IOException iOException) {}
        }
        return DataFileUtil.sanitizeDS(ds);
    }

    @Override
    public DataSet getLines(int start, int count, DataFile dataFile) throws SapphireException {
        DataSet ds = new DataSet();
        ds.setColidCaseSensitive(true);
        try {
            FileInputStream fis = this.getFileInputStream();
            this.inFile = new BufferedReader(new InputStreamReader((InputStream)fis, this.DEFAULT_ENCODING));
            for (int i = 0; i < start - 1; ++i) {
                String string = this.inFile.readLine();
            }
            for (int j = 0; j < count; ++j) {
                String currLine = this.inFile.readLine();
                if (currLine == null) continue;
                int currrow = ds.addRow();
                ArrayList<String> tokens = this.parseLine(currLine);
                for (int i = 0; i < tokens.size(); ++i) {
                    ds.setString(currrow, DataFileUtil.getColumnName(i + 1), tokens.get(i).toString());
                }
            }
        }
        catch (Exception e1) {
            throw new SapphireException("Failed reading slice:" + e1.getMessage(), e1);
        }
        finally {
            try {
                this.inFile.close();
            }
            catch (IOException iOException) {}
        }
        return DataFileUtil.sanitizeDS(ds);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public DataSet getFileContent(DataFile dataFile) throws SapphireException {
        if (this.delimiter == null || this.delimiter.length() == 0) {
            this.delimiter = DEFAULT_DELIMITER;
        }
        DataSet ds = new DataSet();
        try {
            FileManager.FileData fileData;
            FileManager.TempFile tempFile;
            FileInputStream fis = null;
            if (this.messagelogid.length() > 0) {
                tempFile = FileManager.getAttachment("LV_MessageLog", this.messagelogid, "", "", 1, this.connectionId);
                if (tempFile != null) {
                    this.filename = tempFile.getFileName();
                    fileData = tempFile.getData();
                    if (fileData != null) {
                        fis = new FileInputStream(fileData.getFile().toFile());
                    }
                }
            } else if (this.inputtype.equals("T")) {
                tempFile = FileManager.TempFile.getTempFile(this.tempid, false, new QueryProcessor(this.connectionId), this.connectionId);
                if (tempFile != null) {
                    this.filename = tempFile.getFileName();
                    fileData = tempFile.getData();
                    if (fileData != null) {
                        fis = new FileInputStream(fileData.getFile().toFile());
                    }
                }
            } else {
                fis = new FileInputStream(this.filepath + "/" + this.filename);
            }
            try (DataInputStream in = new DataInputStream(fis);
                 BufferedReader br = new BufferedReader(new InputStreamReader((InputStream)fis, this.DEFAULT_ENCODING));){
                String strLine;
                while ((strLine = br.readLine()) != null) {
                    int currRow = ds.addRow();
                    ArrayList<String> tokens = this.parseLine(strLine);
                    for (int i = 0; i < tokens.size(); ++i) {
                        ds.setString(currRow, DataFileUtil.getColumnName(i + 1), tokens.get(i));
                    }
                }
            }
            finally {
                fis.close();
            }
        }
        catch (FileNotFoundException e) {
            throw new SapphireException("Cannot load txt file, invalid file name");
        }
        catch (IOException e) {
            throw new SapphireException("Failed to read data file: " + e.getMessage());
        }
        return ds;
    }

    public ArrayList<String> parseLine(String nextLine) {
        ArrayList<String> tokensOnThisLine = new ArrayList<String>();
        StringBuffer sb = new StringBuffer();
        boolean inQuotes = false;
        boolean inField = false;
        boolean ignoreLeadingWhiteSpace = true;
        boolean strictQuotes = false;
        for (int i = 0; i < nextLine.length(); ++i) {
            char c = nextLine.charAt(i);
            if (c == this.escape.charAt(0)) {
                if (!this.isNextCharacterEscapable(nextLine, inQuotes || inField, i)) continue;
                sb.append(nextLine.charAt(i + 1));
                ++i;
                continue;
            }
            if (c == this.quote.charAt(0)) {
                if (this.isNextCharacterEscapedQuote(nextLine, inQuotes || inField, i)) {
                    sb.append(nextLine.charAt(i + 1));
                    ++i;
                } else {
                    if (!strictQuotes && i > 2 && nextLine.charAt(i - 1) != this.delimiter.charAt(0) && nextLine.length() > i + 1 && nextLine.charAt(i + 1) != this.delimiter.charAt(0)) {
                        if (ignoreLeadingWhiteSpace && sb.length() > 0 && this.isAllWhiteSpace(sb)) {
                            sb.setLength(0);
                        } else {
                            sb.append(c);
                        }
                    }
                    inQuotes = !inQuotes;
                }
                inField = !inField;
                continue;
            }
            if (c == this.delimiter.charAt(0) && !inQuotes) {
                tokensOnThisLine.add(sb.toString());
                sb.setLength(0);
                inField = false;
                continue;
            }
            if (strictQuotes && !inQuotes) continue;
            sb.append(c);
            inField = true;
        }
        if (sb != null) {
            tokensOnThisLine.add(sb.toString());
        }
        return tokensOnThisLine;
    }

    private boolean isNextCharacterEscapedQuote(String nextLine, boolean inQuotes, int i) {
        return inQuotes && nextLine.length() > i + 1 && nextLine.charAt(i + 1) == this.quote.charAt(0);
    }

    private boolean isNextCharacterEscapable(String nextLine, boolean inQuotes, int i) {
        return inQuotes && nextLine.length() > i + 1 && (nextLine.charAt(i + 1) == this.quote.charAt(0) || nextLine.charAt(i + 1) == this.escape.charAt(0));
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
}

