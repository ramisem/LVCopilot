/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.datafile;

import com.labvantage.sapphire.modules.datafile.DataFile;
import com.labvantage.sapphire.modules.datafile.ExcelFileReader;
import com.labvantage.sapphire.modules.datafile.TextFileReader;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class ReadDataFile
extends BaseAction
implements sapphire.action.ReadDataFile {
    public static final String FILETYPE_EXCEL = "excel";
    public static final String FILETYPE_TXT = "txt";
    public static final String DEFAULT_DELIMITER = ",";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String path = properties.getProperty("path");
        String overrideworksheet = properties.getProperty("worksheet");
        String overrideworksheetnum = properties.getProperty("worksheetnumber");
        String encoding = properties.getProperty("encoding", "UTF-8");
        if (path.length() == 0) {
            throw new SapphireException("INVALID_PARAMETER", "The path " + path + " is invalid.");
        }
        String fileName = properties.getProperty("filename");
        if (fileName.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY The filename filename is invalid.");
        }
        String messageTypeId = properties.getProperty("messagetypeid", "");
        String dataFileDefId = properties.getProperty("datafiledefid", "");
        String dataFileDefVersionId = properties.getProperty("datafiledefversionid", "1");
        DataFile dataFile = new DataFile();
        if (messageTypeId.length() == 0 && dataFileDefId.length() == 0) {
            throw new SapphireException("Message Type of DataFileDef Id need to be provided");
        }
        if (messageTypeId.length() > 0) {
            dataFile.initialize(this.getConnectionProcessor().getSapphireConnection(), this.getActionProcessor(), this.getQueryProcessor(), this.getSDCProcessor(), this.getTranslationProcessor(), messageTypeId, overrideworksheet, overrideworksheetnum);
        } else {
            dataFile.initialize(this.getConnectionProcessor().getSapphireConnection(), this.getActionProcessor(), this.getQueryProcessor(), this.getSDCProcessor(), this.getTranslationProcessor(), dataFileDefId, dataFileDefVersionId, overrideworksheet, overrideworksheetnum);
        }
        String fileType = dataFile.getFileType();
        if (fileType.equals(FILETYPE_EXCEL)) {
            ExcelFileReader dataReader = new ExcelFileReader(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()), path, fileName, dataFile.getWorksheet(), -1);
            dataReader.initialize();
            DataSet ds = dataReader.getFileContent(dataFile);
            properties.setProperty("filecontent", ds.toXML());
        } else {
            TextFileReader dataReader = new TextFileReader(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()), path, fileName, -1, dataFile.getDelimiter());
            dataReader.setFileEncoding(encoding);
            dataReader.initialize();
            DataSet ds = dataReader.getFileContent(dataFile);
            properties.setProperty("filecontent", ds.toXML());
        }
    }
}

