/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.datafile;

import com.labvantage.sapphire.modules.datafile.DataFile;
import sapphire.SapphireException;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;

public abstract class DataFileReader {
    protected String tempid = "";
    protected String messagelogid = "";
    protected String filename = "";
    protected String filepath = "";
    protected int sliceSize = -1;
    protected int nextSliceStart = -1;
    protected int totalLineCount = -1;
    protected String connectionId = "";
    protected String inputtype = "";
    protected int eof = -1;
    protected int fbr = -1;
    protected int fbc = -1;
    protected int colcount = -1;
    protected M18NUtil m18n;
    protected String DEFAULT_ENCODING = "UTF-8";

    public DataFileReader(ConnectionInfo connectionInfo, String filepath, String filename, int sliceSize) {
        this.m18n = new M18NUtil(connectionInfo);
        this.connectionId = connectionInfo.getConnectionId();
        this.filename = filename;
        this.filepath = filepath;
        this.sliceSize = sliceSize;
        this.inputtype = "F";
    }

    public DataFileReader(ConnectionInfo connectionInfo, String inputtype, String tempid, String messagelogid, int sliceSize) throws SapphireException {
        this.m18n = new M18NUtil(connectionInfo);
        this.connectionId = connectionInfo.getConnectionId();
        this.filename = "";
        this.filepath = "";
        this.messagelogid = messagelogid;
        this.tempid = tempid;
        this.sliceSize = sliceSize;
        this.inputtype = inputtype;
        if (inputtype.equals("T") && (tempid == null || tempid.length() == 0)) {
            throw new SapphireException("Invalid tempid");
        }
        if (inputtype.equals("M") && (messagelogid == null || messagelogid.length() == 0)) {
            throw new SapphireException("Invalid messagelogid");
        }
    }

    public void finalize() throws Throwable {
        super.finalize();
    }

    public void setSliceStart(int sliceStart) {
        this.nextSliceStart = sliceStart;
    }

    public void setSliceSize(int sliceSize) {
        this.sliceSize = sliceSize;
    }

    public abstract void initialize() throws SapphireException;

    public DataSet getNextSlice() throws SapphireException {
        return this.getNextSlice(null);
    }

    public abstract DataSet getNextSlice(DataFile var1) throws SapphireException;

    public abstract DataSet getHeaderRow(int var1) throws SapphireException;

    public abstract int getLineCount() throws SapphireException;

    public DataSet getLines(int start, int count) throws SapphireException {
        return this.getLines(start, count, null);
    }

    public abstract DataSet getLines(int var1, int var2, DataFile var3) throws SapphireException;

    public abstract DataSet getRawLines(int var1, int var2) throws SapphireException;

    public DataSet getFileContent() throws SapphireException {
        return this.getFileContent(null);
    }

    public abstract DataSet getFileContent(DataFile var1) throws SapphireException;

    public int getEOF(DataSet fileContent) {
        return fileContent.getRowCount();
    }

    public int getFBR(DataSet fileContent) {
        boolean anycontent = false;
        String[] cols = fileContent.getColumns();
        for (int row = 0; row < fileContent.getRowCount(); ++row) {
            boolean blank = true;
            for (int col = 0; col < cols.length; ++col) {
                if (fileContent.getValue(row, cols[col], "").length() <= 0) continue;
                blank = false;
                anycontent = true;
                break;
            }
            if (!blank || !anycontent) continue;
            return row;
        }
        return fileContent.getRowCount();
    }

    public int getColCount(DataSet fileContent) {
        return fileContent.getColumnCount();
    }

    public int getFBC(DataSet fileContent) {
        String[] cols = fileContent.getColumns();
        for (int col = 0; col < cols.length; ++col) {
            boolean blank = true;
            for (int row = 0; row < fileContent.getColumnCount(); ++row) {
                String content = fileContent.getValue(row, cols[col], "");
                if (content == null || content.length() <= 0) continue;
                blank = false;
                break;
            }
            if (!blank) continue;
            return col;
        }
        return fileContent.getColumnCount();
    }

    public void setFileEncoding(String encoding) {
        this.DEFAULT_ENCODING = encoding;
    }
}

