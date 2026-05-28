/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.datafile;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.datafile.ProcessDataFields;
import com.labvantage.sapphire.messaging.MessageLogUtil;
import com.labvantage.sapphire.modules.datafile.DataFileReader;
import com.labvantage.sapphire.modules.datafile.DataFileUtil;
import com.labvantage.sapphire.modules.datafile.ValidationEditorUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.error.ErrorDetail;
import sapphire.error.ErrorHandler;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.M18NUtil;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DataFile {
    public static final String SIMPLE_GRID = "S";
    public static final String CROSSTAB_GRID = "C";
    public static final String FREESTYLE = "F";
    public static final String COMPOSITE = "P";
    public static final String FIELDID = "fieldid";
    public static final String DATATYPE = "datatype";
    public static final String DEFAULTVALUE = "defaultvalue";
    public static final String TITLE = "title";
    public static final String FIELDTYPE_COLUMN = "column";
    public static final String FIELDTYPE_ROW = "row";
    public static final String FIELDTYPE_CELL = "cell";
    public static final String FIELDTYPE_INPUT = "input";
    public static final String FIELDTYPE_RANGE = "range";
    public static final String FIELDTYPE_HEADERROW = "headerrow";
    public static final String FIELDTYPE_HEADERCOLUMN = "headercolumn";
    public static final String FIELDS_COLLECTION = "fields";
    public static final String FIELDTYPE = "type";
    public static final String VALIDATIONRULE = "validationrule";
    public static final String STARTROW_TYPE = "startrowtype";
    public static final String STARTROW = "startrow";
    public static final String ENDROW_TYPE = "endrowtype";
    public static final String ENDROW = "endrow";
    public static final String STARTCOLUMN_TYPE = "startcolumntype";
    public static final String STARTCOLUMN = "startcolumn";
    public static final String ENDCOLUMN_TYPE = "endcolumntype";
    public static final String ENDCOLUMN = "endcolumn";
    public static final String TARGET = "target";
    public static final String FIRST_BLANK_ROW = "firstblankrow";
    public static final String FIRST_BLANK_COLUMN = "firstblankcolumn";
    public static final String END_OF_FILE = "endoffile";
    public static final String ABSOLUTE = "absolute";
    public static final String DATATYPE_NUMBER = "number";
    public static final String DATATYPE_STRING = "string";
    public static final String DATATYPE_DATE = "date";
    public static final String ENTEREDTEXT = "enteredtext";
    public static final String SUBSTITUTE = "substitute";
    public static final String SKIPPROCESSING = "skipprocessing";
    public static final String PHYSICALROW = "physicalrow";
    public static final String PHYSICALCOLUMN = "physicalcolumn";
    public static final String TRANSACTIONDATA = "transactiondata";
    public boolean parsingDone = false;
    private DataSet dataFileDefinition;
    private PropertyList dataFileDefinitionProps;
    public static final String BLOCK_SIZE_ALL = "ALL";
    public static final String BLOCK_SIZE_ONE = "ONE";
    public static final int ABORT = -1;
    public static final int FAILED = 0;
    public static final int SUCCESS = 1;
    public static final int WARNING = 2;
    public static final String DELIMITER = "|!|";
    private int blockSize;
    private int blocksPerTransaction;
    private boolean abortOnFailure;
    private String fileType;
    private String delimiter;
    private String worksheet;
    private String overrideworksheet;
    private String overrideworksheetnum;
    private String overridecommitscope;
    private String dataFileDefinitionId = "";
    private DataSet columnDataTypes = null;
    private StringBuffer log;
    private ActionProcessor actionProcessor;
    private QueryProcessor queryProcessor;
    private SapphireConnection sapphireConnection;
    private SDCProcessor sdcProcessor;
    private TranslationProcessor translationProcessor;
    FormatUtil formatUtil;
    M18NUtil m18nUtil;
    private HashMap<String, Set> fieldDistinctValueSet = new HashMap();
    public boolean noNewTransaction = false;
    public boolean deferToChildTransaction = false;
    public boolean commitPerWorksheet = false;
    private DataSet dataTypeMap = null;
    public PropertyListCollection reviewItems;
    private boolean hasDistinctCheck = false;
    private String distinctFields = "";
    private String distinctCheckValidationErrorAction = "";
    private String distinctCheckErrorPrefix = "";
    private HashSet distinctSet = new HashSet();

    public void initialize(SapphireConnection sapphireConnection, ActionProcessor ap, QueryProcessor qp, SDCProcessor sp, TranslationProcessor tp, String messageTypeId) throws SapphireException {
        this.initialize(sapphireConnection, ap, qp, sp, tp, messageTypeId, "", "", "Default");
    }

    public void initialize(SapphireConnection sapphireConnection, ActionProcessor ap, QueryProcessor qp, SDCProcessor sp, TranslationProcessor tp, String dataFileDefId, String dataFileDefVersionId) throws SapphireException {
        this.initialize(sapphireConnection, ap, qp, sp, tp, dataFileDefId, dataFileDefVersionId, "", "", "Default");
    }

    public void initialize(SapphireConnection sapphireConnection, ActionProcessor ap, QueryProcessor qp, SDCProcessor sp, TranslationProcessor tp, String messageTypeId, String worksheet, String worksheetnum) throws SapphireException {
        this.initialize(sapphireConnection, ap, qp, sp, tp, messageTypeId, worksheet, worksheetnum, "Default");
    }

    public void initialize(SapphireConnection sapphireConnection, ActionProcessor ap, QueryProcessor qp, SDCProcessor sp, TranslationProcessor tp, String messageTypeId, String worksheet, String worksheetnum, String commitscope) throws SapphireException {
        DataSet messageTypeDetails = this.getMessageTypeDetails(qp, messageTypeId);
        String dataFileDefinitionId = messageTypeDetails.getString(0, "definitionkeyid1");
        String dataFileDefVersionId = messageTypeDetails.getString(0, "definitionkeyid2");
        this.initialize(sapphireConnection, ap, qp, sp, tp, dataFileDefinitionId, dataFileDefVersionId, worksheet, worksheetnum, commitscope);
    }

    public void initialize(SapphireConnection sapphireConnection, ActionProcessor ap, QueryProcessor qp, SDCProcessor sdcProcessor, TranslationProcessor translationProcessor, String dataFileDefId, String dataFileDefVersionId, String worksheet, String worksheetnum, String commitscope) throws SapphireException {
        this.sapphireConnection = sapphireConnection;
        this.actionProcessor = ap;
        this.queryProcessor = qp;
        this.dataFileDefinitionId = dataFileDefId;
        this.overrideworksheet = worksheet;
        this.overrideworksheetnum = worksheetnum;
        this.overridecommitscope = commitscope;
        this.sdcProcessor = sdcProcessor;
        this.translationProcessor = translationProcessor;
        this.reviewItems = new PropertyListCollection();
        this.log = new StringBuffer();
        if (dataFileDefVersionId.equals(CROSSTAB_GRID)) {
            dataFileDefVersionId = DataFile.getDataFileDefCurrentVersion(qp, dataFileDefId);
        }
        String sql = "SELECT datafiledefid, datafiledefversionid, datafileobjects, processingscript, processingscripttypeflag, datafilestyleflag, processingoptions, readoptions FROM datafiledef WHERE datafiledefid = ? AND datafiledefversionid = ?";
        this.dataFileDefinition = qp.getPreparedSqlDataSet(sql, new Object[]{dataFileDefId, dataFileDefVersionId}, true);
        if (this.dataFileDefinition == null || this.dataFileDefinition.getRowCount() == 0) {
            throw new SapphireException("DataFileDef not found for datafiledefid: " + dataFileDefId + " Version: " + dataFileDefVersionId);
        }
        this.dataFileDefinitionProps = this.getDataFileDefinitionProps();
        this.parseProcessOptions();
        this.parseReadOptions();
        ConnectionInfo connectionInfo = new ConnectionInfo(sapphireConnection);
        this.formatUtil = FormatUtil.getInstance();
        this.m18nUtil = new M18NUtil(connectionInfo);
    }

    public String getLog() {
        return this.log.toString();
    }

    public void clearLog() {
        this.log = new StringBuffer();
    }

    private void parseProcessOptions() {
        String processOptions = this.getProcessOptions();
        this.blockSize = 1;
        this.blocksPerTransaction = 1;
        this.abortOnFailure = true;
        if (processOptions == null || processOptions.length() == 0) {
            return;
        }
        String[] options = StringUtil.split(processOptions, DELIMITER);
        for (int i = 0; i < options.length; ++i) {
            String[] tokens = StringUtil.split(options[i], "=");
            if (tokens[0].equals("B")) {
                if (tokens[1].equals(BLOCK_SIZE_ALL)) {
                    this.blockSize = -1;
                    continue;
                }
                this.blockSize = Integer.parseInt(tokens[1]);
                continue;
            }
            if (tokens[0].equals("T")) {
                if (this.overridecommitscope.equalsIgnoreCase("all")) {
                    this.blocksPerTransaction = -1;
                    continue;
                }
                if (this.overridecommitscope.equalsIgnoreCase("none")) {
                    this.blocksPerTransaction = 1;
                    this.noNewTransaction = true;
                    continue;
                }
                try {
                    this.blocksPerTransaction = Integer.parseInt(tokens[1]);
                    if (this.blocksPerTransaction != 0) continue;
                    this.noNewTransaction = true;
                    this.blocksPerTransaction = 1;
                }
                catch (Exception ignore) {
                    if (tokens[1].equals(BLOCK_SIZE_ALL)) {
                        this.blocksPerTransaction = -1;
                        continue;
                    }
                    if (tokens[1].equals("NONE")) {
                        this.blocksPerTransaction = 1;
                        this.noNewTransaction = true;
                        continue;
                    }
                    if (tokens[1].equals("CH")) {
                        this.blocksPerTransaction = 1;
                        this.deferToChildTransaction = true;
                        continue;
                    }
                    if (!tokens[1].equals("WS")) continue;
                    this.blocksPerTransaction = 1;
                    this.commitPerWorksheet = true;
                }
                continue;
            }
            if (!tokens[0].equals(FREESTYLE) || !tokens[1].equals(CROSSTAB_GRID)) continue;
            this.abortOnFailure = false;
        }
    }

    private String getWorksheetFromReadoptions(String readOptions) {
        String worksheet = "";
        String[] options = StringUtil.split(readOptions, DELIMITER);
        for (int i = 0; i < options.length; ++i) {
            String[] tokens = StringUtil.split(options[i], "=");
            if (!tokens[0].equals("worksheet")) continue;
            worksheet = tokens[1];
            break;
        }
        return worksheet;
    }

    private void parseReadOptions() {
        String readOptions = this.getReadOptions();
        if (readOptions == null || readOptions.length() == 0) {
            readOptions = "filetype=excel|!|delimiter=,|!|worksheet=Sheet1";
        }
        String[] options = StringUtil.split(readOptions, DELIMITER);
        for (int i = 0; i < options.length; ++i) {
            String[] tokens = StringUtil.split(options[i], "=");
            if (tokens[0].equals("filetype")) {
                this.fileType = tokens[1];
                continue;
            }
            if (tokens[0].equals("delimiter")) {
                this.delimiter = tokens[1];
                continue;
            }
            if (!tokens[0].equals("worksheet")) continue;
            this.worksheet = tokens[1];
        }
    }

    public String getFileType() {
        if (this.fileType == null || this.fileType.length() == 0) {
            this.fileType = "excel";
        }
        return this.fileType;
    }

    public String getDelimiter() {
        return this.delimiter;
    }

    public String getWorksheet() {
        if (this.overrideworksheet != null && this.overrideworksheet.length() > 0) {
            return this.overrideworksheet;
        }
        if (this.overrideworksheetnum != null && this.overrideworksheetnum.length() > 0) {
            return this.overrideworksheetnum;
        }
        if (this.worksheet == null || this.worksheet.length() == 0) {
            this.worksheet = "Sheet1";
        }
        return this.worksheet;
    }

    private DataSet getMessageTypeDetails(QueryProcessor qp, String messageTypeId) throws ActionException {
        String sql = "SELECT directionflag, processactionid, processactionversionid, processactionflag, sendactionid, sendactionversionid, sendactionflag, allowreprocessflag, allowresendflag, messageclass, allowlogflag, definitionkeyid1, definitionkeyid2 FROM messagetype WHERE messagetypeid=?";
        DataSet result = qp.getPreparedSqlDataSet(sql, new Object[]{messageTypeId});
        if (result == null || result.getRowCount() == 0) {
            throw new ActionException("Message Type details not found for messagetypeid: " + messageTypeId);
        }
        return result;
    }

    public PropertyList getDataFileDefinitionProps() throws SapphireException {
        PropertyList props = new PropertyList();
        props.setPropertyList(this.dataFileDefinition.getClob(0, "datafileobjects"));
        return props;
    }

    public DataSet getChildDFDInfo() throws SapphireException {
        String datafiledefversionid;
        String sql = "SELECT refdatafiledefid, refdatafiledefversionid, excelworksheetname FROM datafiledefitem WHERE datafiledefid=? and datafiledefversionid=? order by usersequence";
        String datafiledefid = this.getDataFileDefId();
        DataSet ds = this.queryProcessor.getPreparedSqlDataSet(sql, new Object[]{datafiledefid, datafiledefversionid = this.getDataFileDefVersionId()});
        if (ds == null) {
            throw new SapphireException("Failed to fetch child DFD details for " + datafiledefid + "," + datafiledefversionid);
        }
        for (int i = 0; i < ds.getRowCount(); ++i) {
            String childdfdversionid;
            if (ds.getValue(i, "excelworksheetname", "").length() != 0) continue;
            String sql2 = "SELECT readoptions FROM datafiledef WHERE datafiledefid = ? and datafiledefversionid=?";
            String childdfdid = ds.getValue(i, "refdatafiledefid", "");
            DataSet child = this.queryProcessor.getPreparedSqlDataSet(sql2, new Object[]{childdfdid, childdfdversionid = ds.getValue(i, "refdatafiledefversionid", "")});
            if (child == null || child.getRowCount() == 0) {
                throw new SapphireException("Could not find definition for child DFD " + childdfdid + "," + childdfdversionid);
            }
            String readoptions = child.getValue(0, "readoptions");
            String childworksheet = this.getWorksheetFromReadoptions(readoptions);
            ds.setValue(i, "excelworksheetname", childworksheet);
        }
        return ds;
    }

    public String getDataFileDefId() throws SapphireException {
        return this.dataFileDefinition.getString(0, "datafiledefid");
    }

    public String getDataFileDefVersionId() throws SapphireException {
        return this.dataFileDefinition.getString(0, "datafiledefversionid");
    }

    public static String getDataFileDefCurrentVersion(QueryProcessor qp, String dataFileDefId) {
        String versionStatus;
        int i;
        String sql = "SELECT datafiledefversionid, versionstatus FROM datafiledef WHERE datafiledefid= ? order by datafiledefversionid desc";
        DataSet ds = qp.getPreparedSqlDataSet(sql, new Object[]{dataFileDefId});
        for (i = 0; i < ds.getRowCount(); ++i) {
            versionStatus = ds.getString(i, "versionstatus");
            if (!versionStatus.equals(CROSSTAB_GRID)) continue;
            return ds.getString(i, "datafiledefversionid");
        }
        for (i = 0; i < ds.getRowCount(); ++i) {
            versionStatus = ds.getString(i, "versionstatus");
            if (!versionStatus.equals(COMPOSITE)) continue;
            return ds.getString(i, "datafiledefversionid");
        }
        return "";
    }

    public String getCellDataTypeInfo(int row, String columname, DataFileReader reader) throws SapphireException {
        if (this.dataTypeMap == null) {
            if (this.columnDataTypes != null) {
                return this.getDataType(row, columname);
            }
            this.dataTypeMap = this.getDataTypeMap(reader.eof, reader.fbr, reader.fbc, reader.colcount);
        }
        return this.dataTypeMap.getString(row, columname, DATATYPE_STRING);
    }

    private String getDataType(int row, String columnname) {
        return this.columnDataTypes.getString(0, columnname, "");
    }

    private String getDataTypeParseDetails(String dataType, PropertyList fieldProps) {
        if (dataType.equals(DATATYPE_NUMBER)) {
            String[] parts = this.getNumberFormatRuleParts(fieldProps);
            return dataType + DELIMITER + parts[0] + DELIMITER + parts[1] + DELIMITER + parts[2];
        }
        if (dataType.equals(DATATYPE_DATE)) {
            return this.getDateFormatValidationRule(fieldProps.getProperty(VALIDATIONRULE));
        }
        return dataType;
    }

    private String[] getNumberFormatRuleParts(PropertyList fieldProps) {
        String validationrules = fieldProps.getProperty(VALIDATIONRULE);
        String decimalseparator = "";
        String groupseparator = "";
        String erroraction = "";
        if (validationrules.length() > 0) {
            String[] rules = StringUtil.split(validationrules, ";");
            for (int i = 0; i < rules.length; ++i) {
                if (!rules[i].startsWith("NumberFormatCheck")) continue;
                String actualrulestring = rules[i].substring(rules[i].indexOf("(") + 1, rules[i].indexOf(")"));
                erroraction = rules[i].substring(rules[i].indexOf("ErrorOp=") + 8);
                String[] ruleparts = StringUtil.split(actualrulestring, ":");
                decimalseparator = ruleparts[0];
                if (ruleparts.length <= 1) break;
                groupseparator = ruleparts[1];
                break;
            }
        }
        String[] parts = new String[]{decimalseparator, groupseparator, erroraction};
        return parts;
    }

    public String getDateFormatValidationRule(String validationrules) {
        String[] rules = StringUtil.split(validationrules, ";");
        for (int i = 0; i < rules.length; ++i) {
            if (!rules[i].startsWith("DateFormatCheck")) continue;
            String actualrulestring = rules[i].substring(rules[i].indexOf("(") + 1, rules[i].indexOf(")"));
            String erroraction = rules[i].substring(rules[i].indexOf("ErrorOp=") + 8);
            String[] ruleparts = StringUtil.split(actualrulestring, ":");
            return "date|!|" + ruleparts[0] + DELIMITER + ruleparts[1] + DELIMITER + ruleparts[2] + DELIMITER + erroraction;
        }
        return DATATYPE_DATE;
    }

    public DataSet getDataTypeMap(int eof, int fbr, int fbc, int colcount) throws SapphireException {
        DataSet dataTypeMap = new DataSet();
        for (int i = 0; i < colcount; ++i) {
            dataTypeMap.addColumn(DataFileUtil.getColumnName(i + 1), 0);
        }
        for (int row = 0; row < eof; ++row) {
            dataTypeMap.addRow();
        }
        PropertyListCollection fields = this.dataFileDefinitionProps.getCollectionNotNull(FIELDS_COLLECTION);
        for (int i = 0; i < fields.size(); ++i) {
            int rangeEndColumn;
            int rangeStartColumn;
            int row;
            int j;
            PropertyList fieldProps = fields.getPropertyList(i);
            String currField = fieldProps.getProperty(FIELDID);
            String currFieldType = fieldProps.getProperty(FIELDTYPE);
            String dataType = fieldProps.getProperty(DATATYPE);
            if (currFieldType.equals(FIELDTYPE_COLUMN)) {
                String column = DataFileUtil.getColumnName(Integer.parseInt(fieldProps.getProperty(STARTCOLUMN)));
                String startRowStr = fieldProps.getProperty(STARTROW);
                int startRow = Integer.parseInt(startRowStr);
                int endRow = this.getColumnEndRow(fieldProps, eof, fbr);
                for (j = startRow - 1; j <= endRow; ++j) {
                    String val = this.getDataTypeParseDetails(dataType, fieldProps);
                    dataTypeMap.setValue(j, column, val);
                }
                continue;
            }
            if (currFieldType.equals(FIELDTYPE_ROW)) {
                String startColStr = fieldProps.getProperty(STARTCOLUMN);
                int row2 = Integer.parseInt(fieldProps.getProperty(STARTROW)) - 1;
                int startCol = DataFileUtil.getColumnNum(startColStr);
                int endCol = this.getEndColumn(fields, fieldProps, fbc, colcount) - startCol + 1;
                for (j = startCol; j <= endCol; ++j) {
                    String currColumn = DataFileUtil.getColumnName(j);
                    dataTypeMap.setValue(row2, currColumn, this.getDataTypeParseDetails(dataType, fieldProps));
                }
                continue;
            }
            if (currFieldType.equals(FIELDTYPE_CELL)) {
                String currColumnNum = fieldProps.getProperty(STARTCOLUMN);
                int currrow = Integer.parseInt(fieldProps.getProperty(STARTROW)) - 1;
                dataTypeMap.setValue(currrow, DataFileUtil.getColumnName(Integer.parseInt(currColumnNum)), this.getDataTypeParseDetails(dataType, fieldProps));
                continue;
            }
            if (currFieldType.equals(FIELDTYPE_HEADERCOLUMN)) {
                int headerColumnNum = DataFileUtil.getColumnNum(fieldProps.getProperty(STARTCOLUMN));
                String rangeField = fieldProps.getProperty(TARGET);
                if (rangeField == null || rangeField.length() == 0) {
                    throw new SapphireException("Target not specified for field:" + currField);
                }
                PropertyList dimensions = this.getRangeDimensions(rangeField, fields, eof, fbr, fbc, colcount);
                int rangeStartRow = Integer.parseInt(dimensions.getProperty(STARTROW));
                int rangeEndRow = Integer.parseInt(dimensions.getProperty(ENDROW));
                for (row = rangeStartRow - 1; row < rangeEndRow; ++row) {
                    dataTypeMap.setValue(row, DataFileUtil.getColumnName(headerColumnNum), this.getDataTypeParseDetails(dataType, fieldProps));
                }
                continue;
            }
            if (currFieldType.equals(FIELDTYPE_HEADERROW)) {
                int headerRowNum = Integer.parseInt(fieldProps.getProperty(STARTROW));
                String rangeField = fieldProps.getProperty(TARGET);
                PropertyList dimensions = this.getRangeDimensions(rangeField, fields, eof, fbr, fbc, colcount);
                rangeStartColumn = Integer.parseInt(dimensions.getProperty(STARTCOLUMN));
                rangeEndColumn = Integer.parseInt(dimensions.getProperty(ENDCOLUMN));
                for (int column = rangeStartColumn; column <= rangeEndColumn; ++column) {
                    dataTypeMap.setValue(headerRowNum, DataFileUtil.getColumnName(column), this.getDataTypeParseDetails(dataType, fieldProps));
                }
                continue;
            }
            if (currFieldType.equals(FIELDTYPE_RANGE)) {
                PropertyList dim = this.getRangeDimensions(fieldProps, eof, fbr, fbc, colcount);
                int rangeStartRow = Integer.parseInt(dim.getProperty(STARTROW));
                int rangeEndRow = Integer.parseInt(dim.getProperty(ENDROW));
                rangeStartColumn = Integer.parseInt(dim.getProperty(STARTCOLUMN));
                rangeEndColumn = Integer.parseInt(dim.getProperty(ENDCOLUMN));
                for (row = rangeStartRow - 1; row < rangeEndRow; ++row) {
                    for (int column = rangeStartColumn; column <= rangeEndColumn; ++column) {
                        String colName = DataFileUtil.getColumnName(column);
                        dataTypeMap.setValue(row, colName, this.getDataTypeParseDetails(dataType, fieldProps));
                    }
                }
                continue;
            }
            Trace.logDebug("Invalid fieldtype for datamap, ignoring:" + currFieldType);
        }
        return dataTypeMap;
    }

    public DataSet getColumnDataTypes() throws SapphireException {
        if (this.columnDataTypes == null) {
            this.columnDataTypes = new DataSet();
            this.columnDataTypes.addRow();
            PropertyListCollection fields = this.dataFileDefinitionProps.getCollectionNotNull(FIELDS_COLLECTION);
            for (int i = 0; i < fields.size(); ++i) {
                PropertyList fieldProps = fields.getPropertyList(i);
                String currField = fieldProps.getProperty(FIELDID);
                String currFieldType = fieldProps.getProperty(FIELDTYPE);
                String dataType = fieldProps.getProperty(DATATYPE);
                if (!currFieldType.equals(FIELDTYPE_COLUMN)) continue;
                String column = DataFileUtil.getColumnName(Integer.parseInt(fieldProps.getProperty(STARTCOLUMN)));
                this.columnDataTypes.addColumn(column, 0);
                this.columnDataTypes.setString(0, column, this.getDataTypeParseDetails(dataType, fieldProps));
            }
        }
        return this.columnDataTypes;
    }

    public String getProcessingScript() {
        return this.dataFileDefinition.getClob(0, "processingscript", "");
    }

    public String getProcessingScriptType() {
        return this.dataFileDefinition.getString(0, "processingscripttypeflag", "");
    }

    public String getStyle() {
        return this.dataFileDefinition.getString(0, "datafilestyleflag");
    }

    public String getProcessOptions() {
        return this.dataFileDefinition.getString(0, "processingoptions");
    }

    public String getReadOptions() {
        return this.dataFileDefinition.getString(0, "readoptions");
    }

    public PropertyListCollection getFields() {
        return this.dataFileDefinitionProps.getCollectionNotNull(FIELDS_COLLECTION);
    }

    public boolean canBeProcessedInChunks() {
        PropertyListCollection fields = this.getFields();
        for (int i = 0; i < fields.size(); ++i) {
            String endRowType;
            PropertyList fieldProps = fields.getPropertyList(i);
            String currFieldType = fieldProps.getProperty(FIELDTYPE);
            if (currFieldType.equals(FIELDTYPE_CELL) || currFieldType.equals(FIELDTYPE_ROW)) {
                return false;
            }
            if (!currFieldType.equals(FIELDTYPE_COLUMN) || (endRowType = fieldProps.getProperty(ENDROW_TYPE)).equals(END_OF_FILE)) continue;
            Trace.log("Cannot be processed in chuncks because the end column type is not end of file");
            return false;
        }
        return true;
    }

    public int getHeaderRowNum() {
        PropertyListCollection fields = this.getFields();
        for (int i = 0; i < fields.size(); ++i) {
            PropertyList fieldProps = fields.getPropertyList(i);
            String currFieldType = fieldProps.getProperty(FIELDTYPE);
            if (!currFieldType.equals(FIELDTYPE_COLUMN)) continue;
            String startRow = fieldProps.getProperty(STARTROW);
            return Integer.parseInt(startRow) - 1;
        }
        return 0;
    }

    public int getBlockSize() {
        return this.blockSize;
    }

    public int getBlocksPerTransaction() {
        return this.blocksPerTransaction;
    }

    public boolean getAbortOnFailure() {
        return this.abortOnFailure;
    }

    public int processSlice(int headerRow, int sliceStartRow, DataSet currRawSlice, PropertyList inputProps, boolean verbose) throws SapphireException {
        return this.processSlice(headerRow, sliceStartRow, currRawSlice, inputProps, verbose, true);
    }

    public int processSlice(int headerRow, int sliceStartRow, DataSet currRawSlice, PropertyList inputProps, boolean verbose, boolean process) throws SapphireException {
        PropertyList dataFileDefinitionProps = this.getDataFileDefinitionProps();
        String type = this.getStyle();
        PropertyList submittedValues = this.getGridSliceFieldValues(headerRow, sliceStartRow, dataFileDefinitionProps, currRawSlice, inputProps, this.reviewItems, true);
        if (process) {
            boolean failure;
            boolean bl = failure = this.reviewItems.find("status", "FAIL") != null;
            if (!failure) {
                if (this.reviewItems.find("status", "WARNING") != null) {
                    this.log.append("<P><font color='orange'>Note: There are WARNINGS reported for this Slice.</font></P>");
                }
                return this.processSliceFields(headerRow, type, submittedValues, currRawSlice, verbose, inputProps);
            }
            this.log.append("<P>Validation errors found in this slice.</P>");
            if (this.getAbortOnFailure()) {
                return -1;
            }
            return 0;
        }
        PropertyList item = this.reviewItems.find("status", "FAIL");
        if (item != null) {
            return 0;
        }
        item = this.reviewItems.find("status", "WARNING");
        if (item != null) {
            return 2;
        }
        return 1;
    }

    public int process(DataSet rawDataGrid, PropertyList inputProps, boolean verbose) throws SapphireException {
        return this.process(rawDataGrid, inputProps, verbose, true);
    }

    public int process(DataSet rawDataGrid, PropertyList inputProps, boolean verbose, boolean process) throws SapphireException {
        PropertyList dataFileDefinitionProps = this.getDataFileDefinitionProps();
        String type = this.getStyle();
        this.log.append("<P>Started Processing Data.</P>");
        PropertyList submittedValues = type.equals(SIMPLE_GRID) ? this.getGridFieldValues(dataFileDefinitionProps, rawDataGrid, inputProps, true) : (type.equals(CROSSTAB_GRID) ? this.getCrosstabFieldValues(dataFileDefinitionProps, rawDataGrid, inputProps, true) : this.getFreeStyleFieldValues(dataFileDefinitionProps, rawDataGrid, inputProps, true));
        if (process) {
            boolean failure;
            boolean bl = failure = this.reviewItems.find("status", "FAIL") != null;
            if (!failure) {
                this.log.append("<P>All validations passed.</P>");
                return this.processFields(type, submittedValues, rawDataGrid, verbose, inputProps);
            }
            this.log.append(MessageLogUtil.getValidationLogHtml(this.translationProcessor, this.reviewItems));
            return 0;
        }
        PropertyList item = this.reviewItems.find("status", "FAIL");
        if (item != null) {
            return 0;
        }
        item = this.reviewItems.find("status", "WARNING");
        if (item != null) {
            return 2;
        }
        return 1;
    }

    private PropertyList getBlockValues(PropertyList submittedValues, PropertyListCollection fields, int startPos, int endPos) {
        PropertyList blockValues = new PropertyList();
        TreeSet<Integer> skipRows = new TreeSet<Integer>();
        for (int i = 0; i < fields.size(); ++i) {
            PropertyList fieldProps = fields.getPropertyList(i);
            String currField = fieldProps.getProperty(FIELDID);
            String currFieldType = fieldProps.getProperty(FIELDTYPE);
            if (currFieldType.equals(FIELDTYPE_CELL) || currFieldType.equals(FIELDTYPE_INPUT)) continue;
            PropertyListCollection colValues = submittedValues.getCollectionNotNull(currField);
            for (int row = startPos; row < endPos; ++row) {
                PropertyList current = colValues.getPropertyList(row);
                if ("Y".equals(current.getProperty(SKIPPROCESSING))) {
                    if (skipRows.contains(row)) continue;
                    skipRows.add(row);
                    this.log.append("<P><font color='orange'>Skipping row(" + current.getProperty(PHYSICALROW) + ")</font></P>");
                    continue;
                }
                if (current.getProperty(SUBSTITUTE) == null || current.getProperty(SUBSTITUTE).length() <= 0) continue;
                this.log.append("<P><font color='orange'>Substituting value(" + current.getProperty(ENTEREDTEXT) + ") from Cell(" + current.getProperty(PHYSICALCOLUMN) + current.getProperty(PHYSICALROW) + ") with (" + current.getProperty(SUBSTITUTE) + ")</font></P>");
                current.setProperty(ENTEREDTEXT, current.getProperty(SUBSTITUTE));
            }
        }
        boolean blockHasContents = false;
        for (int i = 0; i < fields.size(); ++i) {
            PropertyListCollection currBlockValues;
            PropertyList currentFieldProps = fields.getPropertyList(i);
            String currField = currentFieldProps.getProperty(FIELDID);
            String currFieldType = currentFieldProps.getProperty(FIELDTYPE);
            if (!currFieldType.equals(FIELDTYPE_CELL) && !currFieldType.equals(FIELDTYPE_INPUT)) {
                PropertyListCollection allValues = submittedValues.getCollectionNotNull(currField);
                currBlockValues = new PropertyListCollection();
                int slicerow = 0;
                for (int pos = startPos; pos < endPos; ++pos) {
                    PropertyList val = allValues.getPropertyList(pos);
                    if (!skipRows.contains(pos) && val != null) {
                        blockHasContents = true;
                        currBlockValues.add(val);
                    }
                    ++slicerow;
                }
                if (currBlockValues.size() <= 0) continue;
                blockValues.setProperty(currField, currBlockValues);
                continue;
            }
            PropertyListCollection oneValue = submittedValues.getCollectionNotNull(currField);
            currBlockValues = new PropertyListCollection();
            PropertyList val = oneValue.getPropertyList(0);
            if (val != null) {
                currBlockValues.add(val);
            }
            blockValues.setProperty(currField, currBlockValues);
        }
        if (!blockHasContents) {
            return null;
        }
        return blockValues;
    }

    private int getGridSize(PropertyList dataFileDefinitionProps, DataSet rawDataGrid) throws SapphireException {
        PropertyListCollection fields = dataFileDefinitionProps.getCollectionNotNull(FIELDS_COLLECTION);
        for (int i = 0; i < fields.size(); ++i) {
            PropertyList fieldProps = fields.getPropertyList(i);
            String currFieldType = fieldProps.getProperty(FIELDTYPE);
            if (currFieldType.equals(FIELDTYPE_COLUMN)) {
                String startRowStr = fieldProps.getProperty(STARTROW);
                int startRow = Integer.parseInt(startRowStr);
                return this.getEndRow(fields, fieldProps, rawDataGrid) - startRow + 1;
            }
            if (!currFieldType.equals(FIELDTYPE_ROW)) continue;
            String startColStr = fieldProps.getProperty(STARTCOLUMN);
            int startCol = DataFileUtil.getColumnNum(startColStr);
            return this.getEndColumn(fields, fieldProps, rawDataGrid) - startCol + 1;
        }
        return 0;
    }

    private PropertyList getGridSliceFieldValues(int headerRow, int sliceStartRow, PropertyList dataFileDefinitionProps, DataSet rawDataGrid, PropertyList inputProps, PropertyListCollection reviewItems, boolean validate) throws SapphireException {
        PropertyList fieldProcessingInput = new PropertyList();
        PropertyListCollection fields = dataFileDefinitionProps.getCollectionNotNull(FIELDS_COLLECTION);
        int endRow = rawDataGrid.getRowCount();
        for (int i = 0; i < fields.size(); ++i) {
            PropertyList fieldProps = fields.getPropertyList(i);
            String currField = fieldProps.getProperty(FIELDID);
            String currFieldType = fieldProps.getProperty(FIELDTYPE);
            if (currFieldType.equals(FIELDTYPE_COLUMN)) {
                PropertyListCollection colValues = this.getSliceColumnFieldValues(headerRow, sliceStartRow, fieldProps, inputProps, rawDataGrid, endRow);
                fieldProcessingInput.setProperty(currField, colValues);
                continue;
            }
            if (currFieldType.equals(FIELDTYPE_ROW)) {
                throw new SapphireException("Cannot process a grid with row fields using sliced approach");
            }
            if (currFieldType.equals(FIELDTYPE_CELL)) {
                throw new SapphireException("Cannot process a grid with cell fields using sliced approach");
            }
            if (!currFieldType.equals(FIELDTYPE_INPUT)) continue;
            fieldProcessingInput.setProperty(currField, this.getInputFieldValue(fieldProps, inputProps));
        }
        if (validate) {
            this.validateFields(fieldProcessingInput, fields);
        }
        return fieldProcessingInput;
    }

    private PropertyList getGridFieldValues(PropertyList dataFileDefinitionProps, DataSet rawDataGrid, PropertyList inputProps, boolean validate) throws SapphireException {
        PropertyList fieldProcessingInput = new PropertyList();
        PropertyListCollection fields = dataFileDefinitionProps.getCollectionNotNull(FIELDS_COLLECTION);
        int endRow = -1;
        int endColumn = -1;
        for (int i = 0; i < fields.size(); ++i) {
            PropertyListCollection colValues;
            PropertyList fieldProps = fields.getPropertyList(i);
            String currField = fieldProps.getProperty(FIELDID);
            String currFieldType = fieldProps.getProperty(FIELDTYPE);
            if (currFieldType.equals(FIELDTYPE_COLUMN)) {
                if (endRow == -1) {
                    endRow = this.getEndRow(fields, fieldProps, rawDataGrid);
                }
                colValues = this.getColumnFieldValues(fieldProps, inputProps, rawDataGrid, endRow);
                fieldProcessingInput.setProperty(currField, colValues);
                continue;
            }
            if (currFieldType.equals(FIELDTYPE_ROW)) {
                if (endColumn == -1) {
                    endColumn = this.getEndColumn(fields, fieldProps, rawDataGrid);
                }
                colValues = this.getRowFieldValues(fieldProps, inputProps, rawDataGrid, endColumn);
                fieldProcessingInput.setProperty(currField, colValues);
                continue;
            }
            if (currFieldType.equals(FIELDTYPE_CELL)) {
                fieldProcessingInput.setProperty(currField, this.getCellFieldValue(fieldProps, inputProps, rawDataGrid));
                continue;
            }
            if (!currFieldType.equals(FIELDTYPE_INPUT)) continue;
            fieldProcessingInput.setProperty(currField, this.getInputFieldValue(fieldProps, inputProps));
        }
        if (validate) {
            this.validateFields(fieldProcessingInput, fields);
        }
        return fieldProcessingInput;
    }

    private void validateFields(PropertyList fieldProcessingInput, PropertyListCollection fields) throws SapphireException {
        for (int i = 0; i < fields.size(); ++i) {
            PropertyList fieldProps = fields.getPropertyList(i);
            this.validateField(fieldProps, fieldProcessingInput);
        }
        if (this.hasDistinctCheck) {
            String[] fieldList = StringUtil.split(this.distinctFields, "|");
            boolean noErrors = true;
            boolean skip = false;
            int size = fieldProcessingInput.getCollection(fieldList[0]).size();
            for (int i = 0; i < size; ++i) {
                String key = "";
                String physicalRow = "-1";
                String prefix = "";
                PropertyListCollection fieldCollection0 = fieldProcessingInput.getCollection(fieldList[0]);
                PropertyList currentFieldRow0 = fieldCollection0.getPropertyList(i);
                String fieldtitles = "";
                String valuelist = "";
                for (int fieldno = 0; fieldno < fieldList.length; ++fieldno) {
                    if (key.length() > 0) {
                        key = key + DELIMITER;
                        fieldtitles = fieldtitles + ",";
                        valuelist = valuelist + ",";
                    }
                    PropertyListCollection fieldCollection = fieldProcessingInput.getCollection(fieldList[fieldno]);
                    PropertyList currentFieldRow = fieldCollection.getPropertyList(i);
                    String value = currentFieldRow.getProperty(ENTEREDTEXT, "");
                    physicalRow = currentFieldRow.getProperty(PHYSICALROW, "-1");
                    key = key + value;
                    valuelist = valuelist + value;
                    fieldtitles = fieldtitles + currentFieldRow.getProperty(TITLE, fieldList[fieldno]);
                }
                if (this.distinctSet.contains(key)) {
                    String finalErrorAction = "Error";
                    if (this.distinctCheckValidationErrorAction.startsWith("Sub")) {
                        finalErrorAction = this.distinctCheckValidationErrorAction;
                        currentFieldRow0.setProperty(SUBSTITUTE, finalErrorAction.substring(4, finalErrorAction.length() - 1));
                    } else if (this.distinctCheckValidationErrorAction.startsWith("Skip")) {
                        skip = true;
                        currentFieldRow0.setProperty(SKIPPROCESSING, "Y");
                    }
                    String error = ValidationEditorUtil.getDistinctErrorMessage(this.translationProcessor, fieldtitles, valuelist, physicalRow);
                    if (this.distinctCheckErrorPrefix.length() > 0) {
                        error = this.distinctCheckErrorPrefix + " " + error;
                    }
                    noErrors &= this.addDistinctValidationError(this.distinctCheckValidationErrorAction, this.reviewItems, fieldtitles, valuelist, error, physicalRow, prefix);
                    continue;
                }
                this.distinctSet.add(key);
            }
        }
    }

    private void validateField(PropertyList fieldProps, PropertyList allFieldValues) throws SapphireException {
        String validationRules = fieldProps.getProperty(VALIDATIONRULE);
        if (fieldProps.getProperty(DATATYPE).equals(DATATYPE_DATE) && (validationRules == null || validationRules.length() == 0)) {
            validationRules = "DateFormatCheck(" + this.m18nUtil.getLocale() + ":" + "" + ":" + ")" + ":" + "ErrorOp=Error";
        }
        if (validationRules != null && validationRules.length() > 0) {
            String currField = fieldProps.getProperty(FIELDID);
            PropertyListCollection currFieldValues = allFieldValues.getCollection(currField);
            for (int item = 0; item < currFieldValues.size(); ++item) {
                String message;
                String actualValue;
                String ruledesc;
                String[] tokens;
                PropertyList currentItem = currFieldValues.getPropertyList(item);
                String currentValue = currentItem.getProperty(ENTEREDTEXT);
                String currentPhysicalRow = currentItem.getProperty(PHYSICALROW);
                String currentPhysicalCol = currentItem.getProperty(PHYSICALCOLUMN);
                String finalErrorAction = "";
                String datatype = fieldProps.getProperty(DATATYPE);
                if (datatype.equals(DATATYPE_DATE) && currentValue.startsWith("ParseDateFieldFailed:")) {
                    tokens = StringUtil.split(currentValue, ":");
                    ruledesc = tokens[1];
                    finalErrorAction = tokens[2];
                    actualValue = tokens[3];
                    currentItem.setProperty(ENTEREDTEXT, actualValue);
                    message = ValidationEditorUtil.getParseErrorMessage(this.translationProcessor, actualValue, DATATYPE_DATE, "" + currentPhysicalRow, currentPhysicalCol);
                    this.addValidationError("DateFormatCheck", ruledesc, finalErrorAction, fieldProps, this.reviewItems, actualValue, message, "" + currentPhysicalRow, currentPhysicalCol);
                } else if (datatype.equals(DATATYPE_NUMBER) && currentValue.startsWith("ParseNumberFieldFailed:")) {
                    tokens = StringUtil.split(currentValue, ":");
                    ruledesc = tokens[1];
                    finalErrorAction = tokens[2];
                    actualValue = tokens[3];
                    currentItem.setProperty(ENTEREDTEXT, actualValue);
                    message = ValidationEditorUtil.getParseErrorMessage(this.translationProcessor, actualValue, DATATYPE_NUMBER, "" + currentPhysicalRow, currentPhysicalCol);
                    this.addValidationError("NumberFormatCheck", ruledesc, finalErrorAction, fieldProps, this.reviewItems, actualValue, message, "" + currentPhysicalRow, currentPhysicalCol);
                } else {
                    finalErrorAction = this.validateFieldValue(fieldProps, currField, currentValue, currentPhysicalRow, currentPhysicalCol, this.reviewItems, allFieldValues, item);
                }
                if (finalErrorAction.startsWith("Sub")) {
                    currentItem.setProperty(SUBSTITUTE, finalErrorAction.substring(4, finalErrorAction.length() - 1));
                    continue;
                }
                if (!finalErrorAction.startsWith("Skip")) continue;
                currentItem.setProperty(SKIPPROCESSING, "Y");
            }
        }
    }

    private PropertyList getFreeStyleFieldValues(PropertyList dataFileDefinitionProps, DataSet rawDataGrid, PropertyList inputProps, boolean validate) throws SapphireException {
        PropertyList fieldProcessingInput = new PropertyList();
        PropertyListCollection fields = dataFileDefinitionProps.getCollectionNotNull(FIELDS_COLLECTION);
        for (int i = 0; i < fields.size(); ++i) {
            PropertyListCollection colValues;
            PropertyList fieldProps = fields.getPropertyList(i);
            String currField = fieldProps.getProperty(FIELDID);
            String currFieldType = fieldProps.getProperty(FIELDTYPE);
            if (currFieldType.equals(FIELDTYPE_COLUMN)) {
                int endRow = this.getColumnEndRow(fieldProps, rawDataGrid);
                colValues = this.getColumnFieldValues(fieldProps, inputProps, rawDataGrid, endRow);
                fieldProcessingInput.setProperty(currField, colValues);
                continue;
            }
            if (currFieldType.equals(FIELDTYPE_ROW)) {
                int endColumn = this.getRowEndColumn(fieldProps, rawDataGrid);
                colValues = this.getRowFieldValues(fieldProps, inputProps, rawDataGrid, endColumn);
                fieldProcessingInput.setProperty(currField, colValues);
                continue;
            }
            if (currFieldType.equals(FIELDTYPE_CELL)) {
                fieldProcessingInput.setProperty(currField, this.getCellFieldValue(fieldProps, inputProps, rawDataGrid));
                continue;
            }
            if (currFieldType.equals(FIELDTYPE_RANGE)) {
                fieldProcessingInput.setProperty(currField, this.getRangeFieldValues(fieldProps, inputProps, rawDataGrid));
                continue;
            }
            if (currFieldType.equals(FIELDTYPE_HEADERROW)) {
                fieldProcessingInput.setProperty(currField, this.getHeaderRowValues(fieldProps, inputProps, fields, rawDataGrid));
                continue;
            }
            if (currFieldType.equals(FIELDTYPE_HEADERCOLUMN)) {
                fieldProcessingInput.setProperty(currField, this.getHeaderColumnValues(fieldProps, inputProps, fields, rawDataGrid));
                continue;
            }
            if (!currFieldType.equals(FIELDTYPE_INPUT)) continue;
            fieldProcessingInput.setProperty(currField, this.getInputFieldValue(fieldProps, inputProps));
        }
        if (validate) {
            this.validateFields(fieldProcessingInput, fields);
        }
        return fieldProcessingInput;
    }

    private int getColumnEndRow(PropertyList fieldProps, int eof, int fbr) {
        String startRowStr = fieldProps.getProperty(STARTROW);
        String startColStr = fieldProps.getProperty(STARTCOLUMN);
        String endRowType = fieldProps.getProperty(ENDROW_TYPE);
        int endRow = FIRST_BLANK_ROW.equals(endRowType) ? fbr : (END_OF_FILE.equals(endRowType) ? eof : Integer.parseInt(fieldProps.getProperty(ENDROW)));
        return endRow;
    }

    private int getColumnEndRow(PropertyList fieldProps, DataSet rawDataGrid) throws SapphireException {
        String startRowStr = fieldProps.getProperty(STARTROW);
        String startColStr = fieldProps.getProperty(STARTCOLUMN);
        String endRowType = fieldProps.getProperty(ENDROW_TYPE);
        int startRow = Integer.parseInt(startRowStr);
        int startCol = Integer.parseInt(startColStr);
        int endRow = FIRST_BLANK_ROW.equals(endRowType) ? this.determineFreeformFBR(startRow, startCol, rawDataGrid) : (END_OF_FILE.equals(endRowType) ? rawDataGrid.getRowCount() : Integer.parseInt(fieldProps.getProperty(ENDROW)));
        return endRow;
    }

    private int getRowEndColumn(PropertyList fieldProps, DataSet rawDataGrid) throws SapphireException {
        String startRowStr = fieldProps.getProperty(STARTROW);
        String startColStr = fieldProps.getProperty(STARTCOLUMN);
        String endColumnType = fieldProps.getProperty(ENDCOLUMN_TYPE);
        int startRow = Integer.parseInt(startRowStr);
        int startCol = Integer.parseInt(startColStr);
        int endRow = FIRST_BLANK_COLUMN.equals(endColumnType) ? this.determineFreeformFBC(startRow, startCol, rawDataGrid) : (END_OF_FILE.equals(endColumnType) ? rawDataGrid.getColumnCount() : Integer.parseInt(fieldProps.getProperty(ENDCOLUMN)));
        return endRow;
    }

    private int getEndRow(PropertyListCollection fields, PropertyList fieldProps, DataSet rawDataGrid) throws SapphireException {
        String startRowStr = fieldProps.getProperty(STARTROW);
        String endRowType = fieldProps.getProperty(ENDROW_TYPE);
        int startRow = Integer.parseInt(startRowStr);
        int endRow = FIRST_BLANK_ROW.equals(endRowType) ? this.determineSimpleGridFBR(startRow, fields, rawDataGrid) : (END_OF_FILE.equals(endRowType) ? rawDataGrid.getRowCount() : Integer.parseInt(fieldProps.getProperty(ENDROW)));
        return endRow;
    }

    private PropertyListCollection getColumnFieldValues(PropertyList fieldProps, PropertyList inputProps, DataSet rawDataGrid, int endRow) throws SapphireException {
        String column = DataFileUtil.getColumnName(Integer.parseInt(fieldProps.getProperty(STARTCOLUMN)));
        String startRowStr = fieldProps.getProperty(STARTROW);
        String currField = fieldProps.getProperty(FIELDID);
        int startRow = Integer.parseInt(startRowStr);
        PropertyListCollection values = new PropertyListCollection();
        for (int i = startRow - 1; i < endRow; ++i) {
            PropertyList fieldValueInfo = new PropertyList();
            String rawValue = rawDataGrid.getString(i, column, "");
            if (rawValue.contains(";")) {
                rawValue = rawValue.replaceAll(";", "#semicolon#");
            }
            fieldValueInfo.setProperty(ENTEREDTEXT, rawValue);
            fieldValueInfo.setProperty(PHYSICALROW, "" + (i + 1));
            fieldValueInfo.setProperty(PHYSICALCOLUMN, column);
            fieldValueInfo.setProperty(DATATYPE, fieldProps.getProperty(DATATYPE));
            values.add(fieldValueInfo);
        }
        return values;
    }

    private PropertyListCollection getSliceColumnFieldValues(int headerRow, int physicalSliceStartRow, PropertyList fieldProps, PropertyList inputProps, DataSet rawDataGrid, int endRow) throws SapphireException {
        String column = DataFileUtil.getColumnName(Integer.parseInt(fieldProps.getProperty(STARTCOLUMN)));
        String currField = fieldProps.getProperty(FIELDID);
        int startRow = 2;
        PropertyListCollection values = new PropertyListCollection();
        for (int i = startRow - 1; i < endRow; ++i) {
            PropertyList enteredText = new PropertyList();
            String rawValue = rawDataGrid.getString(i, column, "");
            if (rawValue.contains(";")) {
                rawValue = rawValue.replaceAll(";", "#semicolon#");
            }
            rawValue = this.parseValidations(rawValue, fieldProps, i, column, physicalSliceStartRow + i - 1, column);
            enteredText.setProperty(ENTEREDTEXT, rawValue);
            enteredText.setProperty(PHYSICALROW, "" + (physicalSliceStartRow + i - 1));
            enteredText.setProperty(PHYSICALCOLUMN, column);
            enteredText.setProperty(DATATYPE, fieldProps.getProperty(DATATYPE));
            values.add(enteredText);
        }
        return values;
    }

    private String parseValidations(String rawValue, PropertyList fieldProps, int row, String colname, int physicalRow, String physicalCol) {
        if (!this.parsingDone) {
            String dateformatcheckrule;
            String validationRules;
            if (fieldProps.getProperty(DATATYPE).equals(DATATYPE_NUMBER) && rawValue.length() > 0) {
                rawValue = this.parseNumericField(rawValue, row, colname, fieldProps, physicalRow, physicalCol);
            } else if (fieldProps.getProperty(DATATYPE).equals(DATATYPE_DATE) && rawValue.length() > 0 && (validationRules = fieldProps.getProperty(VALIDATIONRULE)) != null && validationRules.length() > 0 && (dateformatcheckrule = this.getDateFormatCheckRule(validationRules)).length() > 0) {
                String[] parts = StringUtil.split(dateformatcheckrule, ":");
                String erraction = this.getErrorActionForRule("DateFormatCheck", validationRules);
                rawValue = this.parseDateField(rawValue, parts[0], parts[1], parts[2], erraction);
            }
        }
        return rawValue;
    }

    private String getDateFormatCheckRule(String validationRules) {
        String[] rules = StringUtil.split(validationRules, ";");
        for (int i = 0; i < rules.length; ++i) {
            if (!rules[i].startsWith("DateFormatCheck")) continue;
            String checkstr = rules[i].substring(rules[i].indexOf("(") + 1, rules[i].indexOf(")"));
            return checkstr;
        }
        return "";
    }

    private String getErrorActionForRule(String ruleid, String validationRules) {
        String[] rules = StringUtil.split(validationRules, ";");
        String erraction = "Error";
        for (int i = 0; i < rules.length; ++i) {
            if (!rules[i].startsWith(ruleid)) continue;
            if (rules[i].contains("ErrorOp")) {
                erraction = rules[i].substring(rules[i].indexOf("ErrorOp=") + 8);
            }
            return erraction;
        }
        return erraction;
    }

    private PropertyListCollection getCellFieldValue(PropertyList fieldProps, PropertyList inputProps, DataSet rawDataGrid) throws SapphireException {
        String column = DataFileUtil.getColumnName(Integer.parseInt(fieldProps.getProperty(STARTCOLUMN)));
        String rowStr = fieldProps.getProperty(STARTROW);
        String currField = fieldProps.getProperty(FIELDID);
        int row = Integer.parseInt(rowStr) - 1;
        PropertyListCollection values = new PropertyListCollection();
        PropertyList enteredText = new PropertyList();
        String rawValue = rawDataGrid.getString(row, column, "");
        if ((rawValue = this.parseValidations(rawValue, fieldProps, row, column, row, column)).contains(";")) {
            rawValue = rawValue.replaceAll(";", "#semicolon#");
        }
        enteredText.setProperty(ENTEREDTEXT, rawValue);
        enteredText.setProperty(PHYSICALROW, rowStr);
        enteredText.setProperty(PHYSICALCOLUMN, column);
        enteredText.setProperty(DATATYPE, fieldProps.getProperty(DATATYPE));
        values.add(enteredText);
        return values;
    }

    private PropertyListCollection getInputFieldValue(PropertyList fieldProps, PropertyList inputProps) throws SapphireException {
        String currField = fieldProps.getProperty(FIELDID);
        String validationRules = fieldProps.getProperty(VALIDATIONRULE);
        PropertyListCollection values = new PropertyListCollection();
        String defaultValue = fieldProps.getProperty(DEFAULTVALUE);
        String input = inputProps.getProperty(currField, defaultValue);
        if (input.contains(";")) {
            input = input.replaceAll(";", "#semicolon#");
        }
        PropertyList enteredText = new PropertyList();
        input = this.parseValidations(input, fieldProps, -1, "", -1, "N/A");
        enteredText.setProperty(ENTEREDTEXT, input);
        enteredText.setProperty(PHYSICALROW, "N/A");
        enteredText.setProperty(PHYSICALCOLUMN, "N/A");
        enteredText.setProperty(DATATYPE, fieldProps.getProperty(DATATYPE));
        values.add(enteredText);
        return values;
    }

    private int getEndColumn(PropertyListCollection fields, PropertyList fieldProps, int fbc, int colcount) throws SapphireException {
        String endColType = fieldProps.getProperty(ENDCOLUMN_TYPE);
        if (FIRST_BLANK_COLUMN.equals(endColType)) {
            return fbc;
        }
        if (END_OF_FILE.equals(endColType)) {
            return colcount;
        }
        return DataFileUtil.getColumnNum(fieldProps.getProperty(ENDCOLUMN));
    }

    private int getEndColumn(PropertyListCollection fields, PropertyList fieldProps, DataSet rawDataGrid) throws SapphireException {
        String col = fieldProps.getProperty(STARTCOLUMN);
        String endColType = fieldProps.getProperty(ENDCOLUMN_TYPE);
        if (FIRST_BLANK_COLUMN.equals(endColType)) {
            return this.determineSimpleGridFBC(DataFileUtil.getColumnNum(col), fields, rawDataGrid);
        }
        if (END_OF_FILE.equals(endColType)) {
            return rawDataGrid.getColumnCount();
        }
        return DataFileUtil.getColumnNum(fieldProps.getProperty(ENDCOLUMN));
    }

    private PropertyListCollection getRowFieldValues(PropertyList fieldProps, PropertyList inputProps, DataSet rawDataGrid, int endColumn) throws SapphireException {
        String rowStr = fieldProps.getProperty(STARTROW);
        String startColStr = fieldProps.getProperty(STARTCOLUMN);
        String currField = fieldProps.getProperty(FIELDID);
        int startCol = DataFileUtil.getColumnNum(startColStr);
        PropertyListCollection values = new PropertyListCollection();
        int row = Integer.parseInt(rowStr) - 1;
        for (int i = startCol; i <= endColumn; ++i) {
            PropertyList enteredText = new PropertyList();
            String rawValue = rawDataGrid.getString(row, DataFileUtil.getColumnName(i), "");
            if (rawValue.contains(";")) {
                rawValue = rawValue.replaceAll(";", "#semicolon#");
            }
            rawValue = this.parseValidations(rawValue, fieldProps, row, DataFileUtil.getColumnName(i), row, DataFileUtil.getColumnName(i));
            enteredText.setProperty(ENTEREDTEXT, rawValue);
            enteredText.setProperty(PHYSICALROW, rowStr);
            enteredText.setProperty(PHYSICALCOLUMN, DataFileUtil.getColumnName(i));
            enteredText.setProperty(DATATYPE, fieldProps.getProperty(DATATYPE));
            values.add(enteredText);
        }
        return values;
    }

    private PropertyList getCrosstabFieldValues(PropertyList dataFileDefinitionProps, DataSet rawDataGrid, PropertyList inputProps, boolean validate) throws SapphireException {
        String fieldtype;
        PropertyList fieldProps;
        int i;
        PropertyList fieldProcessingProps = new PropertyList();
        PropertyListCollection fields = dataFileDefinitionProps.getCollectionNotNull(FIELDS_COLLECTION);
        int rangeEndRow = -1;
        int rangeEndColumn = -1;
        int rangeStartRow = -1;
        int rangeStartColumn = -1;
        PropertyList rangeDimensions = null;
        for (i = 0; i < fields.size(); ++i) {
            fieldProps = fields.getPropertyList(i);
            fieldtype = fieldProps.getProperty(FIELDTYPE);
            if (!fieldtype.equals(FIELDTYPE_RANGE)) continue;
            rangeDimensions = this.getRangeDimensions(fieldProps, rawDataGrid);
            rangeStartRow = Integer.parseInt(rangeDimensions.getProperty(STARTROW));
            rangeStartColumn = Integer.parseInt(rangeDimensions.getProperty(STARTCOLUMN));
            rangeEndRow = Integer.parseInt(rangeDimensions.getProperty(ENDROW));
            rangeEndColumn = Integer.parseInt(rangeDimensions.getProperty(ENDCOLUMN));
        }
        for (i = 0; i < fields.size(); ++i) {
            fieldProps = fields.getPropertyList(i);
            fieldtype = fieldProps.getProperty(FIELDTYPE);
            String fieldId = fieldProps.getProperty(FIELDID);
            if (FIELDTYPE_RANGE.equals(fieldtype)) {
                PropertyListCollection range = this.getRangeFieldValues(fieldProps, inputProps, rawDataGrid);
                fieldProcessingProps.setProperty(fieldId, range);
                continue;
            }
            if (FIELDTYPE_HEADERROW.equals(fieldtype)) {
                PropertyListCollection headerRow = this.getHeaderRowValues(fieldProps, inputProps, rangeStartRow, rangeEndRow, rangeStartColumn, rangeEndColumn, rawDataGrid);
                fieldProcessingProps.setProperty(fieldId, headerRow);
                continue;
            }
            if (FIELDTYPE_HEADERCOLUMN.equals(fieldtype)) {
                PropertyListCollection headerCol = this.getHeaderColumnValues(fieldProps, inputProps, rangeStartRow, rangeEndRow, rangeStartColumn, rangeEndColumn, rawDataGrid);
                fieldProcessingProps.setProperty(fieldId, headerCol);
                continue;
            }
            if (FIELDTYPE_CELL.equals(fieldtype)) {
                fieldProcessingProps.setProperty(fieldId, this.getCellFieldValue(fieldProps, inputProps, rawDataGrid));
                continue;
            }
            if (!fieldtype.equals(FIELDTYPE_INPUT)) continue;
            fieldProcessingProps.setProperty(fieldId, this.getInputFieldValue(fieldProps, inputProps));
        }
        if (validate) {
            this.validateFields(fieldProcessingProps, fields);
        }
        return fieldProcessingProps;
    }

    private PropertyListCollection getRangeFieldValues(PropertyList fieldProps, PropertyList inputProps, DataSet rawDataGrid) throws SapphireException {
        PropertyList dim = this.getRangeDimensions(fieldProps, rawDataGrid);
        int rangeStartRow = Integer.parseInt(dim.getProperty(STARTROW));
        int rangeEndRow = Integer.parseInt(dim.getProperty(ENDROW));
        int rangeStartColumn = Integer.parseInt(dim.getProperty(STARTCOLUMN));
        int rangeEndColumn = Integer.parseInt(dim.getProperty(ENDCOLUMN));
        PropertyListCollection range = this.getRangeValues(fieldProps, inputProps, rangeStartRow, rangeEndRow, rangeStartColumn, rangeEndColumn, rawDataGrid);
        return range;
    }

    private PropertyList getRangeDimensions(String rangeField, PropertyListCollection fields, int eof, int fbr, int fbc, int colcount) throws SapphireException {
        for (int i = 0; i < fields.size(); ++i) {
            PropertyList fieldProps = fields.getPropertyList(i);
            String fieldtype = fieldProps.getProperty(FIELDTYPE);
            String fieldId = fieldProps.getProperty(FIELDID);
            if (!fieldId.equals(rangeField) || !fieldtype.equals(FIELDTYPE_RANGE)) continue;
            return this.getRangeDimensions(fieldProps, eof, fbr, fbc, colcount);
        }
        throw new SapphireException("Target range field not found: " + rangeField);
    }

    private PropertyList getRangeDimensions(PropertyList fieldProps, int eof, int fbr, int fbc, int colcount) throws SapphireException {
        int rangeStartRow = Integer.parseInt(fieldProps.getProperty(STARTROW));
        int rangeStartColumn = DataFileUtil.getColumnNum(fieldProps.getProperty(STARTCOLUMN));
        String rangeEndRowType = fieldProps.getProperty(ENDROW_TYPE);
        int rangeEndRow = -1;
        int rangeEndColumn = -1;
        rangeEndRow = rangeEndRowType.equals(FIRST_BLANK_ROW) ? fbr - 1 : (rangeEndRowType.equals(END_OF_FILE) ? eof : Integer.parseInt(fieldProps.getProperty(ENDROW)));
        String rangeEndColType = fieldProps.getProperty(ENDCOLUMN_TYPE);
        rangeEndColumn = rangeEndColType.equals(FIRST_BLANK_COLUMN) ? fbc - 1 : (rangeEndColType.equals(END_OF_FILE) ? colcount : DataFileUtil.getColumnNum(fieldProps.getProperty(ENDCOLUMN)));
        PropertyList rangeDim = new PropertyList();
        rangeDim.setProperty(STARTROW, "" + rangeStartRow);
        rangeDim.setProperty(STARTCOLUMN, "" + rangeStartColumn);
        rangeDim.setProperty(ENDROW, "" + rangeEndRow);
        rangeDim.setProperty(ENDCOLUMN, "" + rangeEndColumn);
        return rangeDim;
    }

    private PropertyList getRangeDimensions(String rangeField, PropertyListCollection fields, DataSet rawDataGrid) throws SapphireException {
        for (int i = 0; i < fields.size(); ++i) {
            PropertyList fieldProps = fields.getPropertyList(i);
            String fieldtype = fieldProps.getProperty(FIELDTYPE);
            String fieldId = fieldProps.getProperty(FIELDID);
            if (!fieldId.equals(rangeField) || !fieldtype.equals(FIELDTYPE_RANGE)) continue;
            return this.getRangeDimensions(fieldProps, rawDataGrid);
        }
        throw new SapphireException("Target range field not found: " + rangeField);
    }

    private PropertyList getRangeDimensions(PropertyList fieldProps, DataSet rawDataGrid) throws SapphireException {
        int rangeStartRow = Integer.parseInt(fieldProps.getProperty(STARTROW));
        int rangeStartColumn = DataFileUtil.getColumnNum(fieldProps.getProperty(STARTCOLUMN));
        String rangeEndRowType = fieldProps.getProperty(ENDROW_TYPE);
        int rangeEndRow = -1;
        int rangeEndColumn = -1;
        rangeEndRow = rangeEndRowType.equals(FIRST_BLANK_ROW) ? this.determineCrosstabGridFBR(rangeStartRow, rangeStartColumn, rawDataGrid) - 1 : (rangeEndRowType.equals(END_OF_FILE) ? rawDataGrid.getRowCount() : Integer.parseInt(fieldProps.getProperty(ENDROW)));
        String rangeEndColType = fieldProps.getProperty(ENDCOLUMN_TYPE);
        rangeEndColumn = rangeEndColType.equals(FIRST_BLANK_COLUMN) ? this.determineCrosstabGridFBC(rangeStartRow, rangeEndRow, rangeStartColumn, rawDataGrid) - 1 : (rangeEndColType.equals(END_OF_FILE) ? rawDataGrid.getColumnCount() : DataFileUtil.getColumnNum(fieldProps.getProperty(ENDCOLUMN)));
        PropertyList rangeDim = new PropertyList();
        rangeDim.setProperty(STARTROW, "" + rangeStartRow);
        rangeDim.setProperty(STARTCOLUMN, "" + rangeStartColumn);
        rangeDim.setProperty(ENDROW, "" + rangeEndRow);
        rangeDim.setProperty(ENDCOLUMN, "" + rangeEndColumn);
        return rangeDim;
    }

    private int getCrosstabSize(PropertyList dataFileDefinitionProps, DataSet rawDataGrid) throws SapphireException {
        PropertyListCollection fields = dataFileDefinitionProps.getCollectionNotNull(FIELDS_COLLECTION);
        int rangeEndRow = -1;
        int rangeEndColumn = -1;
        int rangeStartRow = -1;
        int rangeStartColumn = -1;
        for (int i = 0; i < fields.size(); ++i) {
            PropertyList fieldProps = fields.getPropertyList(i);
            String fieldtype = fieldProps.getProperty(FIELDTYPE);
            if (!FIELDTYPE_RANGE.equals(fieldtype)) continue;
            rangeStartRow = Integer.parseInt(fieldProps.getProperty(STARTROW));
            rangeStartColumn = DataFileUtil.getColumnNum(fieldProps.getProperty(STARTCOLUMN));
            String rangeEndRowType = fieldProps.getProperty(ENDROW_TYPE);
            rangeEndRow = rangeEndRowType.equals(FIRST_BLANK_ROW) ? this.determineCrosstabGridFBR(rangeStartRow, rangeStartColumn, rawDataGrid) - 1 : (rangeEndRowType.equals(END_OF_FILE) ? rawDataGrid.getRowCount() : Integer.parseInt(fieldProps.getProperty(ENDROW)));
            String rangeEndColType = fieldProps.getProperty(ENDCOLUMN_TYPE);
            rangeEndColumn = rangeEndColType.equals(FIRST_BLANK_COLUMN) ? this.determineCrosstabGridFBC(rangeStartRow, rangeEndRow, rangeStartColumn, rawDataGrid) - 1 : (rangeEndColType.equals(END_OF_FILE) ? rawDataGrid.getColumnCount() : DataFileUtil.getColumnNum(fieldProps.getProperty(ENDCOLUMN)));
            int numOfCols = rangeEndColumn - rangeStartColumn + 1;
            int numOfRows = rangeEndRow - rangeStartRow + 1;
            return numOfCols * numOfRows;
        }
        return 0;
    }

    private PropertyListCollection getRangeValues(PropertyList fieldProps, PropertyList inputProps, int startRow, int endRow, int startColumn, int endColumn, DataSet rawDataGrid) throws SapphireException {
        PropertyListCollection values = new PropertyListCollection();
        String validationRules = fieldProps.getProperty(VALIDATIONRULE);
        for (int row = startRow - 1; row < endRow; ++row) {
            for (int column = startColumn; column <= endColumn; ++column) {
                String colName = DataFileUtil.getColumnName(column);
                PropertyList valProps = new PropertyList();
                String rawValue = rawDataGrid.getString(row, colName, "");
                if (rawValue.contains(";")) {
                    rawValue = rawValue.replaceAll(";", "#semicolon#");
                }
                int physicalRow = row;
                valProps.setProperty(ENTEREDTEXT, rawValue);
                valProps.setProperty(PHYSICALROW, row + 1 + "");
                valProps.setProperty(PHYSICALCOLUMN, colName);
                valProps.setProperty(DATATYPE, fieldProps.getProperty(DATATYPE));
                values.add(valProps);
            }
        }
        return values;
    }

    private PropertyListCollection getHeaderRowValues(PropertyList fieldProps, PropertyList inputProps, PropertyListCollection fields, DataSet rawDataGrid) throws SapphireException {
        int headerRowNum = Integer.parseInt(fieldProps.getProperty(STARTROW));
        String rangeField = fieldProps.getProperty(TARGET);
        PropertyList dimensions = this.getRangeDimensions(rangeField, fields, rawDataGrid);
        int rangeStartRow = Integer.parseInt(dimensions.getProperty(STARTROW));
        int rangeEndRow = Integer.parseInt(dimensions.getProperty(ENDROW));
        int rangeStartColumn = Integer.parseInt(dimensions.getProperty(STARTCOLUMN));
        int rangeEndColumn = Integer.parseInt(dimensions.getProperty(ENDCOLUMN));
        PropertyListCollection headerRowValues = new PropertyListCollection();
        for (int row = rangeStartRow - 1; row < rangeEndRow; ++row) {
            for (int column = rangeStartColumn; column <= rangeEndColumn; ++column) {
                PropertyList valProps = new PropertyList();
                String rawValue = rawDataGrid.getString(headerRowNum - 1, DataFileUtil.getColumnName(column), "");
                if (rawValue.contains(";")) {
                    rawValue = rawValue.replaceAll(";", "#semicolon#");
                }
                int physicalRow = headerRowNum;
                rawValue = this.parseValidations(rawValue, fieldProps, row, DataFileUtil.getColumnName(column), headerRowNum, DataFileUtil.getColumnName(column));
                valProps.setProperty(ENTEREDTEXT, rawValue);
                valProps.setProperty(PHYSICALROW, headerRowNum + "");
                valProps.setProperty(PHYSICALCOLUMN, DataFileUtil.getColumnName(column));
                valProps.setProperty(DATATYPE, fieldProps.getProperty(DATATYPE));
                headerRowValues.add(valProps);
            }
        }
        return headerRowValues;
    }

    private PropertyListCollection getHeaderRowValues(PropertyList fieldProps, PropertyList inputProps, int rangeStartRow, int rangeEndRow, int rangeStartColumn, int rangeEndColumn, DataSet rawDataGrid) throws SapphireException {
        int headerRowNum = Integer.parseInt(fieldProps.getProperty(STARTROW));
        PropertyListCollection headerRowValues = new PropertyListCollection();
        for (int row = rangeStartRow - 1; row < rangeEndRow; ++row) {
            for (int column = rangeStartColumn; column <= rangeEndColumn; ++column) {
                PropertyList valProps = new PropertyList();
                String rawValue = rawDataGrid.getString(headerRowNum - 1, DataFileUtil.getColumnName(column), "");
                if (rawValue.contains(";")) {
                    rawValue = rawValue.replaceAll(";", "#semicolon#");
                }
                int physicalRow = headerRowNum;
                rawValue = this.parseValidations(rawValue, fieldProps, row, DataFileUtil.getColumnName(column), headerRowNum, DataFileUtil.getColumnName(column));
                valProps.setProperty(ENTEREDTEXT, rawValue);
                valProps.setProperty(PHYSICALROW, headerRowNum + "");
                valProps.setProperty(PHYSICALCOLUMN, DataFileUtil.getColumnName(column));
                valProps.setProperty(DATATYPE, fieldProps.getProperty(DATATYPE));
                headerRowValues.add(valProps);
            }
        }
        return headerRowValues;
    }

    private PropertyListCollection getHeaderColumnValues(PropertyList fieldProps, PropertyList inputProps, PropertyListCollection fields, DataSet rawDataGrid) throws SapphireException {
        int headerColumnNum = DataFileUtil.getColumnNum(fieldProps.getProperty(STARTCOLUMN));
        String rangeField = fieldProps.getProperty(TARGET);
        PropertyList dimensions = this.getRangeDimensions(rangeField, fields, rawDataGrid);
        int rangeStartRow = Integer.parseInt(dimensions.getProperty(STARTROW));
        int rangeEndRow = Integer.parseInt(dimensions.getProperty(ENDROW));
        int rangeStartColumn = Integer.parseInt(dimensions.getProperty(STARTCOLUMN));
        int rangeEndColumn = Integer.parseInt(dimensions.getProperty(ENDCOLUMN));
        PropertyListCollection headerColumnValues = new PropertyListCollection();
        for (int row = rangeStartRow - 1; row < rangeEndRow; ++row) {
            for (int column = rangeStartColumn; column <= rangeEndColumn; ++column) {
                PropertyList enteredVal = new PropertyList();
                String rawValue = rawDataGrid.getString(row, DataFileUtil.getColumnName(headerColumnNum), "");
                if (rawValue.contains(";")) {
                    rawValue = rawValue.replaceAll(";", "#semicolon#");
                }
                rawValue = this.parseValidations(rawValue, fieldProps, row, DataFileUtil.getColumnName(headerColumnNum), row + 1, DataFileUtil.getColumnName(headerColumnNum));
                enteredVal.setProperty(ENTEREDTEXT, rawValue);
                enteredVal.setProperty(PHYSICALROW, row + 1 + "");
                enteredVal.setProperty(PHYSICALCOLUMN, DataFileUtil.getColumnName(headerColumnNum));
                enteredVal.setProperty(DATATYPE, fieldProps.getProperty(DATATYPE));
                headerColumnValues.add(enteredVal);
            }
        }
        return headerColumnValues;
    }

    private PropertyListCollection getHeaderColumnValues(PropertyList fieldProps, PropertyList inputProps, int rangeStartRow, int rangeEndRow, int rangeStartColumn, int rangeEndColumn, DataSet rawDataGrid) throws SapphireException {
        int headerColumnNum = DataFileUtil.getColumnNum(fieldProps.getProperty(STARTCOLUMN));
        PropertyListCollection headerColumnValues = new PropertyListCollection();
        for (int row = rangeStartRow - 1; row < rangeEndRow; ++row) {
            for (int column = rangeStartColumn; column <= rangeEndColumn; ++column) {
                PropertyList enteredVal = new PropertyList();
                String rawValue = rawDataGrid.getString(row, DataFileUtil.getColumnName(headerColumnNum), "");
                if (rawValue.contains(";")) {
                    rawValue = rawValue.replaceAll(";", "#semicolon#");
                }
                rawValue = this.parseValidations(rawValue, fieldProps, row, DataFileUtil.getColumnName(headerColumnNum), row + 1, DataFileUtil.getColumnName(headerColumnNum));
                enteredVal.setProperty(ENTEREDTEXT, rawValue);
                enteredVal.setProperty(PHYSICALROW, row + 1 + "");
                enteredVal.setProperty(PHYSICALCOLUMN, DataFileUtil.getColumnName(headerColumnNum));
                enteredVal.setProperty(DATATYPE, fieldProps.getProperty(DATATYPE));
                headerColumnValues.add(enteredVal);
            }
        }
        return headerColumnValues;
    }

    private int determineSimpleGridFBR(int startRow, PropertyListCollection fields, DataSet rawDataGrid) {
        for (int currRow = startRow - 1; currRow < rawDataGrid.getRowCount(); ++currRow) {
            boolean found = true;
            for (int i = 0; i < fields.size(); ++i) {
                String currColumn;
                String cellVal;
                PropertyList checkFieldProps = fields.getPropertyList(i);
                String currFieldType = checkFieldProps.getProperty(FIELDTYPE);
                if (!currFieldType.equals(FIELDTYPE_COLUMN) || (cellVal = rawDataGrid.getString(currRow, currColumn = DataFileUtil.getColumnName(Integer.parseInt(checkFieldProps.getProperty(STARTCOLUMN))))) == null || cellVal.length() <= 0) continue;
                found = false;
                break;
            }
            if (!found) continue;
            return currRow;
        }
        return rawDataGrid.getRowCount();
    }

    private int determineCrosstabGridFBR(int startRow, int startCol, DataSet rawDataGrid) {
        for (int currRow = startRow - 1; currRow < rawDataGrid.getRowCount(); ++currRow) {
            boolean found = true;
            for (int currCol = startCol; currCol < rawDataGrid.getColumnCount(); ++currCol) {
                String cellVal = rawDataGrid.getString(currRow, DataFileUtil.getColumnName(currCol));
                if (cellVal == null || cellVal.length() <= 0) continue;
                found = false;
                break;
            }
            if (!found) continue;
            return currRow + 1;
        }
        return rawDataGrid.getRowCount() + 1;
    }

    private int determineFreeformFBR(int startRow, int currCol, DataSet rawDataGrid) {
        for (int currRow = startRow - 1; currRow < rawDataGrid.getRowCount(); ++currRow) {
            String cellVal = rawDataGrid.getString(currRow, DataFileUtil.getColumnName(currCol));
            if (cellVal != null && cellVal.length() != 0) continue;
            return currRow;
        }
        return rawDataGrid.getRowCount() + 1;
    }

    private int determineFreeformFBC(int startCol, int currRow, DataSet rawDataGrid) {
        for (int currCol = startCol; currCol < rawDataGrid.getColumnCount(); ++currCol) {
            String cellVal = rawDataGrid.getString(currRow, DataFileUtil.getColumnName(currCol));
            if (cellVal != null && cellVal.length() != 0) continue;
            return currCol;
        }
        return rawDataGrid.getColumnCount();
    }

    private int determineSimpleGridFBC(int startColumn, PropertyListCollection fields, DataSet rawDataGrid) {
        for (int currCol = startColumn; currCol < rawDataGrid.getColumnCount(); ++currCol) {
            boolean found = true;
            for (int i = 0; i < fields.size(); ++i) {
                String currRow;
                String cellVal;
                PropertyList checkFieldProps = fields.getPropertyList(i);
                String currFieldType = checkFieldProps.getProperty(FIELDTYPE);
                if (!currFieldType.equals(FIELDTYPE_ROW) || (cellVal = rawDataGrid.getString(Integer.parseInt(currRow = checkFieldProps.getProperty(STARTROW)), DataFileUtil.getColumnName(currCol))) == null || cellVal.length() <= 0) continue;
                found = false;
                break;
            }
            if (!found) continue;
            return currCol;
        }
        return rawDataGrid.getColumnCount();
    }

    private int determineCrosstabGridFBC(int startRow, int endRow, int startColumn, DataSet rawDataGrid) {
        for (int currCol = startColumn; currCol < rawDataGrid.getColumnCount(); ++currCol) {
            boolean found = true;
            for (int currRow = startRow; currRow < endRow; ++currRow) {
                String cellVal = rawDataGrid.getString(currRow, DataFileUtil.getColumnName(currCol));
                if (cellVal == null || cellVal.length() <= 0) continue;
                found = false;
                break;
            }
            if (!found) continue;
            return currCol;
        }
        return rawDataGrid.getColumnCount();
    }

    private int processFields(String gridType, PropertyList submittedValues, DataSet rawDataGrid, boolean verbose, PropertyList inputProps) throws SapphireException {
        if (gridType.equals(SIMPLE_GRID) || gridType.equals(CROSSTAB_GRID)) {
            PropertyList dataFileDefinitionProps = this.getDataFileDefinitionProps();
            int totalNumOfRecords = -1;
            totalNumOfRecords = gridType.equals(SIMPLE_GRID) ? this.getGridSize(dataFileDefinitionProps, rawDataGrid) : this.getCrosstabSize(dataFileDefinitionProps, rawDataGrid);
            int blockSize = this.getBlockSize();
            int blocksPerTransaction = this.getBlocksPerTransaction();
            if (blockSize == -1) {
                blockSize = totalNumOfRecords;
                blocksPerTransaction = 1;
            }
            if (blocksPerTransaction == -1) {
                blocksPerTransaction = this.getNumOfBlocks(blockSize, totalNumOfRecords);
            }
            int currPos = 0;
            int currTransaction = 0;
            PropertyList fieldsInfo = new PropertyList();
            fieldsInfo.setProperty(FIELDS_COLLECTION, this.getFields());
            if (!verbose || !this.noNewTransaction) {
                // empty if block
            }
            boolean nofailures = true;
            while (currPos < totalNumOfRecords) {
                int startTransactionPos = currTransaction * (blockSize * blocksPerTransaction);
                int endTransactionPos = Math.min((currTransaction + 1) * (blockSize * blocksPerTransaction), totalNumOfRecords);
                int currBlock = 0;
                PropertyList transactionProps = new PropertyList();
                PropertyListCollection blocksProcessed = new PropertyListCollection();
                while (currPos < endTransactionPos) {
                    int startBlockPos = startTransactionPos + currBlock * blockSize;
                    int endBlockPos = Math.min(startBlockPos + blockSize, totalNumOfRecords);
                    PropertyList blockSubmittedValues = this.getBlockValues(submittedValues, dataFileDefinitionProps.getCollectionNotNull(FIELDS_COLLECTION), startBlockPos, endBlockPos);
                    if (blockSubmittedValues == null) {
                        this.log.append("<P>Block has no records to be processed.</P>");
                    } else {
                        blocksProcessed.add(blockSubmittedValues);
                    }
                    ++currBlock;
                    currPos = endBlockPos;
                }
                ++currTransaction;
                if (blocksProcessed.size() > 0) {
                    transactionProps.setProperty(TRANSACTIONDATA, blocksProcessed);
                    if (this.processTransaction(transactionProps, fieldsInfo, this.getProcessingScript(), this.getProcessingScriptType(), verbose, inputProps, this.noNewTransaction)) continue;
                    nofailures = false;
                    this.log.append("<P>").append("Processing failed.").append("</P>");
                    if (!this.getAbortOnFailure()) continue;
                    this.log.append("</p>Aborting further processing.</P>");
                    return -1;
                }
                this.log.append("<P>All blocks are skipped.</P>");
            }
            if (nofailures) {
                return 1;
            }
            return 0;
        }
        PropertyList transactionProps = new PropertyList();
        PropertyListCollection blockToProcess = new PropertyListCollection();
        blockToProcess.add(submittedValues);
        transactionProps.setProperty(TRANSACTIONDATA, blockToProcess);
        PropertyList fieldsInfo = new PropertyList();
        fieldsInfo.setProperty(FIELDS_COLLECTION, this.getFields());
        if (!this.processTransaction(transactionProps, fieldsInfo, this.getProcessingScript(), this.getProcessingScriptType(), verbose, inputProps, this.noNewTransaction)) {
            this.log.append("<P>Transaction failed.</P>");
            return 0;
        }
        return 1;
    }

    private int processSliceFields(int headerRow, String gridType, PropertyList submittedValues, DataSet rawDataGrid, boolean verbose, PropertyList inputProps) throws SapphireException {
        if (gridType.equals(SIMPLE_GRID)) {
            PropertyList dataFileDefinitionProps = this.getDataFileDefinitionProps();
            int totalNumOfRecords = 0;
            totalNumOfRecords = headerRow > 0 ? rawDataGrid.getRowCount() - 1 : rawDataGrid.getRowCount();
            int blockSize = this.getBlockSize();
            int blocksPerTransaction = this.getBlocksPerTransaction();
            if (blockSize == -1) {
                blockSize = totalNumOfRecords;
                blocksPerTransaction = 1;
            }
            if (blocksPerTransaction == -1) {
                blocksPerTransaction = this.getNumOfBlocks(blockSize, totalNumOfRecords);
            }
            int currPos = 0;
            int currTransaction = 0;
            PropertyList fieldsInfo = new PropertyList();
            fieldsInfo.setProperty(FIELDS_COLLECTION, this.getFields());
            if (verbose) {
                // empty if block
            }
            boolean nofailures = true;
            while (currPos < totalNumOfRecords) {
                int startTransactionPos = currTransaction * (blockSize * blocksPerTransaction);
                int endTransactionPos = Math.min((currTransaction + 1) * (blockSize * blocksPerTransaction), totalNumOfRecords);
                int currBlock = 0;
                PropertyList transactionProps = new PropertyList();
                PropertyListCollection blocksProcessed = new PropertyListCollection();
                while (currPos < endTransactionPos) {
                    int startBlockPos = startTransactionPos + currBlock * blockSize;
                    int endBlockPos = Math.min(startBlockPos + blockSize, totalNumOfRecords);
                    PropertyList blockSubmittedValues = this.getBlockValues(submittedValues, dataFileDefinitionProps.getCollectionNotNull(FIELDS_COLLECTION), startBlockPos, endBlockPos);
                    if (blockSubmittedValues != null) {
                        blocksProcessed.add(blockSubmittedValues);
                    } else {
                        this.log.append("<P>Block has no records to process.</P>");
                    }
                    ++currBlock;
                    currPos = endBlockPos;
                }
                ++currTransaction;
                transactionProps.setProperty(TRANSACTIONDATA, blocksProcessed);
                if (this.processTransaction(transactionProps, fieldsInfo, this.getProcessingScript(), this.getProcessingScriptType(), verbose, inputProps, this.noNewTransaction)) continue;
                nofailures = false;
                this.log.append("<P>").append("Transaction failed.").append("</P>");
                if (this.getAbortOnFailure()) {
                    this.log.append("<P>Aborting further processing.</P>");
                    return -1;
                }
                this.log.append("<P>Continuing with next transaction (if any)").append("</P>");
            }
            if (nofailures) {
                return 1;
            }
            return 0;
        }
        throw new SapphireException("This method processes on grids in slices");
    }

    private boolean processTransaction(PropertyList transactionProps, PropertyList fieldsInfo, String processingScript, String processingScriptType, boolean verbose, PropertyList inputProps, boolean noNewTransaction) {
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty(FIELDS_COLLECTION, fieldsInfo.toXMLString());
        actionProps.setProperty("submittedvalues", transactionProps.toXMLString());
        actionProps.setProperty("processingscript", processingScript);
        actionProps.setProperty("processingscripttype", processingScriptType);
        actionProps.setProperty("datafiledefinitionid", this.dataFileDefinitionId);
        actionProps.setProperty("verbose", verbose ? "Y" : "N");
        try {
            if (noNewTransaction) {
                this.actionProcessor.processActionClass(ProcessDataFields.class.getName(), actionProps);
            } else {
                this.actionProcessor.processActionClass(ProcessDataFields.class.getName(), actionProps, true);
            }
            this.log.append(actionProps.get("log"));
            if ("SUCCESS".equals(actionProps.get("status"))) {
                actionProps.remove(FIELDS_COLLECTION);
                actionProps.remove("submittedvalues");
                actionProps.remove("processingscript");
                actionProps.remove("processingscripttype");
                actionProps.remove("datafiledefinitionid");
                actionProps.remove("verbose");
                Iterator iter = actionProps.keySet().iterator();
                while (iter.hasNext()) {
                    String key = iter.next().toString();
                    inputProps.setProperty(key, actionProps.getProperty(key, ""));
                }
                return true;
            }
            inputProps.setProperty("ErrorMsg", actionProps.get("log").toString());
            this.log.append("<P>ProcessDataFields action call failed,</P>");
            return false;
        }
        catch (ActionException e) {
            ErrorHandler eh = e.getErrorHandler();
            for (int i = 0; i < eh.size(); ++i) {
                ErrorDetail err = (ErrorDetail)eh.get(i);
                this.log.append("<P>").append(err.getErrorType()).append(":").append(err.getMessage());
                inputProps.setProperty("ErrorMsg", err.getMessage());
                this.log.append("<P>ProcessDataFields action call failed,</P>");
            }
            return false;
        }
    }

    private int getNumOfBlocks(int blockSize, int totalNumOfRecords) {
        int blockCount = 0;
        for (int remaining = totalNumOfRecords; remaining > 0; remaining -= blockSize) {
            ++blockCount;
        }
        return blockCount;
    }

    public PropertyListCollection getReviewItems() {
        return this.reviewItems;
    }

    private boolean isNumeric(String value) {
        try {
            Double.parseDouble(value);
        }
        catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public String validateFieldValue(PropertyList fieldProps, String fieldId, String value, String physicalRow, String physicalCol, PropertyListCollection reviewItems, PropertyList allFieldValues, int itemnum) {
        PropertyList currentFieldValues;
        String validationRulesStr = fieldProps.getProperty(VALIDATIONRULE);
        boolean noErrors = true;
        String finalErrorAction = "";
        boolean skip = false;
        PropertyListCollection fieldValuesColl = allFieldValues.getCollection(fieldId);
        PropertyList propertyList = currentFieldValues = fieldValuesColl.size() == 1 ? fieldValuesColl.getPropertyList(0) : fieldValuesColl.getPropertyList(itemnum);
        if (validationRulesStr != null && validationRulesStr.length() > 0) {
            String[] validationRules = StringUtil.split(validationRulesStr, ";");
            for (int i = 0; i < validationRules.length; ++i) {
                String[] tokens;
                String values;
                String conditions;
                String[] conditionList;
                if (validationRules[i].length() <= 0) continue;
                String action = "Error";
                if (validationRules[i].indexOf("ErrorOp=") > 0) {
                    action = validationRules[i].substring(validationRules[i].indexOf("ErrorOp=") + 8);
                }
                if (validationRules[i].startsWith("Mandatory")) {
                    conditionList = null;
                    conditions = validationRules[i].substring(validationRules[i].indexOf("(") + 1, validationRules[i].indexOf(")"));
                    if (conditions.length() > 0) {
                        conditionList = StringUtil.split(conditions, "|");
                    }
                    if (value != null && value.length() != 0) continue;
                    finalErrorAction = "Error";
                    int conditionInError = -1;
                    if (conditionList != null && conditionList.length > 0 && (conditionInError = this.evaluateCondition(fieldId, value, fieldProps.getProperty(DATATYPE), conditionList, allFieldValues, itemnum)) != -1) continue;
                    if (action.startsWith("Sub")) {
                        finalErrorAction = action;
                    } else if (action.startsWith("Skip")) {
                        skip = true;
                    }
                    String error = ValidationEditorUtil.getErrorMessage(this.translationProcessor, "Mandatory", fieldProps, value, physicalRow, physicalCol);
                    noErrors &= this.addValidationError("Mandatory", conditions, action, fieldProps, reviewItems, value, error, physicalRow, physicalCol);
                    continue;
                }
                if (validationRules[i].startsWith("DistinctCheck")) {
                    if (this.hasDistinctCheck) continue;
                    this.hasDistinctCheck = true;
                    this.distinctFields = validationRules[i].substring(validationRules[i].indexOf("(") + 1, validationRules[i].indexOf(")"));
                    this.distinctCheckValidationErrorAction = action;
                    this.distinctCheckErrorPrefix = fieldProps.getProperty("errorprefix", "");
                    continue;
                }
                if (validationRules[i].startsWith("ValueCheck")) {
                    if (value.length() <= 0) continue;
                    conditionList = null;
                    conditions = validationRules[i].substring(validationRules[i].indexOf("(") + 1, validationRules[i].indexOf(")"));
                    if (conditions.length() > 0) {
                        conditionList = StringUtil.split(conditions, "|");
                    }
                    finalErrorAction = "Error";
                    int conditionInError = -1;
                    if (conditionList != null && conditionList.length > 0 && (conditionInError = this.evaluateCondition(fieldId, value, fieldProps.getProperty(DATATYPE), conditionList, allFieldValues, itemnum)) == -1) continue;
                    if (action.startsWith("Sub")) {
                        finalErrorAction = action;
                    } else if (action.startsWith("Skip")) {
                        skip = true;
                    }
                    String error = ValidationEditorUtil.getValueCheckErrorMessage(this.translationProcessor, fieldProps, value, physicalRow, physicalCol, conditionList[conditionInError]);
                    noErrors &= this.addValidationError("ValueCheck", conditions, action, fieldProps, reviewItems, value, error, physicalRow, physicalCol);
                    continue;
                }
                if (validationRules[i].startsWith("Length")) {
                    if (validationRules[i].length() <= 0 || validationRules[i].indexOf("(") <= 0 || validationRules[i].indexOf(")") <= 0) continue;
                    values = validationRules[i].substring(validationRules[i].indexOf("(") + 1, validationRules[i].indexOf(")"));
                    tokens = StringUtil.split(values, ":");
                    int lengthMin = -1;
                    int lengthMax = -1;
                    if (this.isInteger(tokens[0])) {
                        lengthMin = Integer.parseInt(tokens[0]);
                    }
                    if (this.isInteger(tokens[1])) {
                        lengthMax = Integer.parseInt(tokens[1]);
                    }
                    if (lengthMin > 0 && value.length() < lengthMin) {
                        if (action.startsWith("Sub")) {
                            finalErrorAction = action;
                        } else if (action.startsWith("Skip")) {
                            skip = true;
                        }
                        noErrors &= this.addValidationError("Length", "", action, fieldProps, reviewItems, value, "Length of value is less than " + lengthMin, physicalRow, physicalCol);
                    }
                    if (lengthMax <= 0 || value == null || value.length() <= lengthMax) continue;
                    if (action.startsWith("Sub")) {
                        finalErrorAction = action;
                    } else if (action.startsWith("Skip")) {
                        skip = true;
                    }
                    noErrors &= this.addValidationError("Length", "", action, fieldProps, reviewItems, value, "Length of value is greater than " + lengthMax, physicalRow, physicalCol);
                    continue;
                }
                if (validationRules[i].startsWith("ValueList")) {
                    if (value.length() <= 0 || validationRules[i].length() <= 0 || validationRules[i].indexOf("(") <= 0 || validationRules[i].indexOf(")") <= 0) continue;
                    values = validationRules[i].substring(validationRules[i].indexOf("(") + 1, validationRules[i].indexOf(")"));
                    String[] valueitems = StringUtil.split(values, "|");
                    boolean found = false;
                    for (int j = 0; j < valueitems.length; ++j) {
                        String datavalue = valueitems[j];
                        String internalvalue = valueitems[j];
                        if (valueitems[j].contains(":")) {
                            String[] nv = StringUtil.split(valueitems[j], ":");
                            datavalue = nv[0];
                            internalvalue = nv[1];
                        }
                        datavalue = StringUtil.unescape(datavalue);
                        internalvalue = StringUtil.unescape(internalvalue);
                        if (!datavalue.equals(value)) continue;
                        found = true;
                        if (internalvalue.length() <= 0) continue;
                        currentFieldValues.setProperty(SUBSTITUTE, internalvalue);
                    }
                    if (found) continue;
                    if (action.startsWith("Sub")) {
                        finalErrorAction = action;
                    } else if (action.startsWith("Skip")) {
                        skip = true;
                    }
                    String errmsg = ValidationEditorUtil.getValueListErrorMessage(this.translationProcessor, values, fieldProps, value, physicalRow, physicalCol);
                    noErrors &= this.addValidationError("ValueList", values, action, fieldProps, reviewItems, value, errmsg, physicalRow, physicalCol);
                    continue;
                }
                if (validationRules[i].startsWith("ValueRefType")) {
                    int j;
                    if (value.length() <= 0) continue;
                    String reftypeid = validationRules[i].substring(validationRules[i].indexOf("(") + 1, validationRules[i].indexOf(")"));
                    tokens = StringUtil.split(validationRules[i], ":");
                    String option = tokens[1];
                    String sql = "SELECT refvalueid, refdisplayvalue FROM refvalue where reftypeid=?";
                    SafeSQL safeSQL = new SafeSQL();
                    safeSQL.addVar(reftypeid);
                    DataSet vals = this.queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
                    String[] refTypeValueList = null;
                    String[] refTypeDisplayValueList = null;
                    if (vals == null) {
                        this.log.append("Invalid validation rule, reftype not found, ignored");
                        continue;
                    }
                    refTypeValueList = StringUtil.split(vals.getColumnValues("refvalueid", ";"), ";");
                    refTypeDisplayValueList = StringUtil.split(vals.getColumnValues("refdisplayvalue", ";"), ";");
                    boolean found = false;
                    if (option.equals("Option1")) {
                        for (j = 0; j < refTypeValueList.length; ++j) {
                            if (!refTypeValueList[j].equalsIgnoreCase(value)) continue;
                            found = true;
                            break;
                        }
                    } else {
                        for (j = 0; j < refTypeDisplayValueList.length; ++j) {
                            if (!refTypeDisplayValueList[j].equalsIgnoreCase(value)) continue;
                            found = true;
                            if (!option.equals("Option3")) break;
                            currentFieldValues.setProperty(SUBSTITUTE, refTypeValueList[j]);
                            break;
                        }
                    }
                    if (found) continue;
                    finalErrorAction = "Error";
                    if (action.startsWith("Sub")) {
                        finalErrorAction = action;
                    } else if (action.startsWith("Skip")) {
                        skip = true;
                    }
                    String errmsg = ValidationEditorUtil.getValueReftypeErrorMessage(this.translationProcessor, reftypeid, option, fieldProps, value, physicalRow, physicalCol);
                    noErrors &= this.addValidationError("ValueRefType", reftypeid + "(" + option + ")", action, fieldProps, reviewItems, value, errmsg, physicalRow, physicalCol);
                    continue;
                }
                if (validationRules[i].startsWith("GroovyCheck")) {
                    if (!validationRules[i].contains("(") || !validationRules[i].contains(")")) continue;
                    String groovyscriptescape = validationRules[i].substring(validationRules[i].indexOf("(") + 1, validationRules[i].indexOf(")"));
                    String groovyscript = StringUtil.unescape(groovyscriptescape);
                    groovyscript = this.replaceFieldReferences(groovyscript, allFieldValues, itemnum, fieldId);
                    HashMap<String, HashMap> bindings = new HashMap<String, HashMap>();
                    bindings.put(FIELDS_COLLECTION, this.getFieldBindings(this.getFields(), allFieldValues, itemnum));
                    bindings.put("output", new HashMap());
                    bindings.put("user", new ConnectionInfo(this.sapphireConnection).getUserAttributeMap());
                    bindings.put("currentfield", this.getCurrentFieldMap(fieldProps, allFieldValues, itemnum));
                    boolean success = true;
                    try {
                        GroovyUtil groovyUtil = GroovyUtil.getInstance(new ConnectionInfo(this.sapphireConnection), new ConfigurationProcessor(this.sapphireConnection.getConnectionId()), this.queryProcessor);
                        String returnValue = groovyUtil.evaluateSecure(groovyscript, bindings);
                        if (returnValue.equalsIgnoreCase("N") || returnValue.equalsIgnoreCase("false") || returnValue.equalsIgnoreCase("No")) {
                            success = false;
                        }
                    }
                    catch (SapphireException e) {
                        Trace.log("Groovy exception evaluation failed. Ignoring the rule");
                    }
                    if (success) continue;
                    finalErrorAction = "Error";
                    if (action.startsWith("Sub")) {
                        finalErrorAction = action;
                    } else if (action.startsWith("Skip")) {
                        skip = true;
                    }
                    String error = ValidationEditorUtil.getErrorMessage(this.translationProcessor, "GroovyCheck", fieldProps, value, physicalRow, physicalCol);
                    noErrors &= this.addValidationError("GroovyCheck", groovyscript, action, fieldProps, reviewItems, value, error, physicalRow, physicalCol);
                    continue;
                }
                if (validationRules[i].startsWith("RegExCheck")) {
                    if (validationRules[i].contains("(") && validationRules[i].contains(")")) {
                        String regexescape = validationRules[i].substring(validationRules[i].indexOf("(") + 1, validationRules[i].indexOf(")"));
                        String regex = StringUtil.unescape(regexescape);
                        boolean matches = true;
                        if (value.length() > 0) {
                            matches = this.regexEvaluate(regex, value);
                        }
                        if (matches) continue;
                        finalErrorAction = "Error";
                        if (action.startsWith("Sub")) {
                            finalErrorAction = action;
                        } else if (action.startsWith("Skip")) {
                            skip = true;
                        }
                        String error = ValidationEditorUtil.getErrorMessage(this.translationProcessor, "RegExCheck", fieldProps, value, physicalRow, physicalCol);
                        noErrors &= this.addValidationError("RegExCheck", regex, action, fieldProps, reviewItems, value, error, physicalRow, physicalCol);
                        continue;
                    }
                    Trace.log("Invalid validation rule, ignore.");
                    continue;
                }
                if (!validationRules[i].startsWith("SDICheck") || value.length() <= 0 || !validationRules[i].contains("(") || !validationRules[i].contains(")")) continue;
                String checkstr = validationRules[i].substring(validationRules[i].indexOf("(") + 1, validationRules[i].indexOf(")"));
                String sdichecktype = "Exists";
                String sdichecksdcid = "";
                String sdicheckcolumnid = "";
                String sdicheckfrom = "";
                String sdicheckwhere = "";
                String substitutekeycol = "";
                if (checkstr.length() > 0) {
                    String[] tokens2 = StringUtil.split(checkstr, ":");
                    sdichecktype = tokens2[0];
                    if (tokens2.length > 1) {
                        sdichecksdcid = tokens2[1];
                    }
                    if (tokens2.length > 2) {
                        sdicheckcolumnid = tokens2[2];
                    }
                    if (tokens2.length > 3) {
                        substitutekeycol = tokens2[3];
                    }
                    if (tokens2.length > 4) {
                        sdicheckfrom = StringUtil.unescape(tokens2[4]);
                    }
                    if (tokens2.length > 5) {
                        sdicheckwhere = StringUtil.unescape(tokens2[5]);
                    }
                }
                boolean exists = false;
                String substitutekeycolvalue = "";
                if (sdichecksdcid.length() > 0) {
                    HashMap sdcprops = this.sdcProcessor.getSDCProperties(sdichecksdcid);
                    if (sdcprops != null && sdcprops.size() > 0) {
                        DataSet ds;
                        String keycol;
                        String selectlist = keycol = (String)sdcprops.get("keycolid1");
                        String comparecol = keycol;
                        if (!sdicheckcolumnid.equals(keycol)) {
                            selectlist = selectlist + "," + sdicheckcolumnid;
                            comparecol = sdicheckcolumnid;
                        }
                        String sql = "SELECT " + selectlist + " FROM " + sdicheckfrom + " WHERE ";
                        SafeSQL safeSQL = new SafeSQL();
                        safeSQL.setPreparedSQL(sql);
                        if (sdicheckwhere.length() > 0) {
                            sql = sql + "" + sdicheckwhere;
                            safeSQL.setPreparedSQL(sql);
                            this.replaceFieldValues(safeSQL, allFieldValues, itemnum, fieldId);
                        }
                        if ((ds = this.queryProcessor.getPreparedSqlDataSet(safeSQL.getPreparedSQL(), safeSQL.getValues())) != null && ds.getRowCount() > 0) {
                            exists = true;
                            substitutekeycolvalue = ds.getValue(0, keycol);
                        }
                    } else {
                        Trace.log("Invalid sdcid specified in the rules");
                    }
                }
                if (sdichecktype.equals("Exists") && !exists || sdichecktype.equals("NotExists") && exists) {
                    finalErrorAction = "Error";
                    if (action.startsWith("Sub")) {
                        finalErrorAction = action;
                    } else if (action.startsWith("Skip")) {
                        skip = true;
                    }
                    String error = ValidationEditorUtil.getSDICheckErrorMessage(this.translationProcessor, fieldProps, value, physicalRow, physicalCol, sdichecktype, sdichecksdcid, sdicheckcolumnid);
                    noErrors &= this.addValidationError("SDICheck", sdichecktype + ":" + sdichecksdcid + ":" + sdicheckcolumnid + ":" + sdicheckwhere, action, fieldProps, reviewItems, value, error, physicalRow, physicalCol);
                    continue;
                }
                if (substitutekeycol.length() <= 0) continue;
                currentFieldValues.setProperty(SUBSTITUTE, substitutekeycolvalue);
            }
        }
        if (skip) {
            return "Skip";
        }
        return finalErrorAction;
    }

    private boolean regexEvaluate(String regex, String value) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }

    private String replaceFieldReferences(String str, PropertyList allFieldValues, int itemnum, String thisfieldname) {
        if (!str.contains(".value]") && !str.contains(".length")) {
            return str;
        }
        if (str.contains("[this.value]")) {
            str = str.replaceAll("this.", "" + thisfieldname + ".");
        }
        Object[] fieldnames = allFieldValues.keySet().toArray();
        for (int i = 0; i < fieldnames.length; ++i) {
            PropertyListCollection fieldValuesColl = allFieldValues.getCollection((String)fieldnames[i]);
            PropertyList currentFieldVal = fieldValuesColl.size() == 1 ? fieldValuesColl.getPropertyList(0) : fieldValuesColl.getPropertyList(itemnum);
            String currentFieldId = (String)fieldnames[i];
            String currentDataType = currentFieldVal.getProperty(DATATYPE);
            String currentFieldRef = "fields." + currentFieldId + ".value";
            while (str.contains("[" + currentFieldId + ".value]")) {
                String pattern = "[" + currentFieldId + ".value]";
                int pos = str.indexOf(pattern);
                if (pos <= -1) continue;
                String pre = str.substring(0, pos);
                String post = str.substring(pos + pattern.length());
                str = pre + currentFieldRef + post;
            }
            if (!str.contains(".value]") && !str.contains(".length")) break;
        }
        return str;
    }

    public int evaluateCondition(String fieldid, String currfieldvalue, String currfieldtype, String[] conditions, PropertyList allFieldValues, int itemnum) {
        for (int i = 0; i < conditions.length; ++i) {
            PropertyList rhsfieldvaluedetailsforitemnum;
            String rhsfield;
            PropertyList lhsfieldvaluedetailsforitemnum;
            String lhsfield;
            String currentCondition = conditions[i];
            String[] tokens = StringUtil.split(currentCondition, ":");
            if (tokens.length != 3) continue;
            String lhs = tokens[0];
            String op = tokens[1];
            String rhs = tokens[2];
            String datatype = DATATYPE_STRING;
            if (lhs.contains(".value") && lhs.startsWith("[") && lhs.endsWith("]")) {
                lhsfield = lhs.substring(1, lhs.indexOf(".value"));
                if (lhsfield.equals("this")) {
                    lhsfield = fieldid;
                }
                lhsfieldvaluedetailsforitemnum = allFieldValues.getCollection(lhsfield).getPropertyList(itemnum);
                lhs = lhsfieldvaluedetailsforitemnum.getProperty(ENTEREDTEXT);
                datatype = lhsfieldvaluedetailsforitemnum.getProperty(DATATYPE);
            } else if (lhs.contains(".length]") && lhs.startsWith("[") && lhs.endsWith("]")) {
                lhsfield = lhs.substring(1, lhs.indexOf(".length"));
                if (lhsfield.equals("this")) {
                    lhsfield = fieldid;
                }
                lhsfieldvaluedetailsforitemnum = allFieldValues.getCollection(lhsfield).getPropertyList(itemnum);
                lhs = "" + lhsfieldvaluedetailsforitemnum.getProperty(ENTEREDTEXT).length();
                datatype = DATATYPE_NUMBER;
            } else if (lhs.length() == 0) {
                lhs = currfieldvalue;
                datatype = currfieldtype;
            }
            if (rhs.contains(".value") && rhs.startsWith("[") && rhs.endsWith("]")) {
                rhsfield = rhs.substring(1, rhs.indexOf(".value"));
                if (rhsfield.equals("this")) {
                    rhsfield = fieldid;
                }
                rhsfieldvaluedetailsforitemnum = allFieldValues.getCollection(rhsfield).getPropertyList(itemnum);
                rhs = rhsfieldvaluedetailsforitemnum.getProperty(ENTEREDTEXT);
                datatype = rhsfieldvaluedetailsforitemnum.getProperty(DATATYPE);
            } else if (rhs.contains(".length]") && rhs.startsWith("[") && rhs.endsWith("]")) {
                rhsfield = rhs.substring(1, rhs.indexOf(".length"));
                if (rhsfield.equals("this")) {
                    rhsfield = fieldid;
                }
                rhsfieldvaluedetailsforitemnum = allFieldValues.getCollection(rhsfield).getPropertyList(itemnum);
                rhs = "" + rhsfieldvaluedetailsforitemnum.getProperty(ENTEREDTEXT).length();
                datatype = DATATYPE_NUMBER;
            }
            if (lhs.startsWith("[this.")) {
                lhs = lhs.replace("this", fieldid);
            }
            if (rhs.startsWith("[this.")) {
                rhs = rhs.replace("this", fieldid);
            }
            if (this.evaluateExpression(datatype, lhs, op, rhs)) continue;
            return i;
        }
        return -1;
    }

    public boolean addValidationError(String rule, String condition, String action, PropertyList fieldProps, PropertyListCollection reviewItems, String value, String message, String row, String column) {
        PropertyList reviewItem = new PropertyList();
        if (this.getFileType().equals("excel")) {
            reviewItem.setProperty("worksheet", this.worksheet);
        }
        reviewItem.setProperty(FIELDID, fieldProps.getProperty(FIELDID));
        reviewItem.setProperty(TITLE, fieldProps.getProperty(TITLE));
        reviewItem.setProperty("validation", rule);
        reviewItem.setProperty("action", action);
        if (action.startsWith("Skip")) {
            reviewItem.setProperty("action", "Skip Record");
        } else if (action.startsWith("Sub")) {
            String substitutionval = action.substring(4, action.length() - 1);
            reviewItem.setProperty("action", "Substitute Value (" + substitutionval + ")");
        }
        reviewItem.setProperty("value", value);
        String prefix = fieldProps.getProperty("errorprefix", "");
        if (prefix.length() > 0) {
            message = prefix + " " + message;
        }
        reviewItem.setProperty("message", message);
        reviewItem.setProperty("condition", condition);
        if (action.startsWith("Skip") || action.startsWith("Sub")) {
            reviewItem.setProperty("status", "WARNING");
        } else {
            reviewItem.setProperty("status", "FAIL");
        }
        reviewItem.setProperty(FIELDTYPE_ROW, row);
        reviewItem.setProperty(FIELDTYPE_COLUMN, column);
        reviewItems.add(reviewItem);
        return false;
    }

    public boolean addDistinctValidationError(String action, PropertyListCollection reviewItems, String fieldlist, String value, String message, String row, String prefix) {
        PropertyList reviewItem = new PropertyList();
        if (this.getFileType().equals("excel")) {
            reviewItem.setProperty("worksheet", this.worksheet);
        }
        reviewItem.setProperty(FIELDID, fieldlist);
        reviewItem.setProperty(TITLE, fieldlist);
        reviewItem.setProperty("validation", "Distinct Check");
        reviewItem.setProperty("action", action);
        if (action.startsWith("Skip")) {
            reviewItem.setProperty("action", "Skip Record");
        } else if (action.startsWith("Sub")) {
            String substitutionval = action.substring(4, action.length() - 1);
            reviewItem.setProperty("action", "Substitute Value (" + substitutionval + ")");
        }
        reviewItem.setProperty("value", value);
        if (prefix.length() > 0) {
            message = prefix + " " + message;
        }
        reviewItem.setProperty("message", message);
        reviewItem.setProperty("condition", "Distinct");
        if (action.startsWith("Skip") || action.startsWith("Sub")) {
            reviewItem.setProperty("status", "WARNING");
        } else {
            reviewItem.setProperty("status", "FAIL");
        }
        reviewItem.setProperty(FIELDTYPE_ROW, row);
        reviewItems.add(reviewItem);
        return false;
    }

    protected String evalTokens(PropertyList fieldProps, PropertyList inputProps, PropertyList validationRule, String message) {
        String[] tokens;
        String newValue = message;
        if (validationRule != null && (tokens = StringUtil.getTokens(message)) != null && tokens.length > 0) {
            for (int i = 0; i < tokens.length; ++i) {
                if (tokens[i].equalsIgnoreCase("filename")) {
                    newValue = StringUtil.replaceAll(newValue, "[filename]", inputProps.getProperty("filename"));
                }
                if (tokens[i].equalsIgnoreCase("path")) {
                    newValue = StringUtil.replaceAll(newValue, "[path]", inputProps.getProperty("path"));
                }
                if (tokens[i].equalsIgnoreCase("currentuser")) {
                    newValue = StringUtil.replaceAll(newValue, "[currentuser]", this.sapphireConnection.getSysuserId());
                }
                if (tokens[i].equalsIgnoreCase("currentusername")) {
                    newValue = StringUtil.replaceAll(newValue, "[currentusername]", this.sapphireConnection.getSysuserName());
                    continue;
                }
                if (tokens[i].equalsIgnoreCase("currentdatetime")) {
                    newValue = StringUtil.replaceAll(newValue, "[currentdatetime]", this.m18nUtil.format(this.m18nUtil.getNowCalendar()));
                    continue;
                }
                if (tokens[i].equalsIgnoreCase("currentdate")) {
                    newValue = StringUtil.replaceAll(newValue, "[currentdate]", this.m18nUtil.formatDateOnly(this.m18nUtil.getNowCalendar()));
                    continue;
                }
                if (tokens[i].equalsIgnoreCase(FIELDID)) {
                    newValue = StringUtil.replaceAll(newValue, "[fieldid]", fieldProps.getProperty(FIELDID));
                    continue;
                }
                if (tokens[i].equalsIgnoreCase("value")) {
                    newValue = StringUtil.replaceAll(newValue, "[value]", validationRule.getProperty(tokens[i]));
                    continue;
                }
                if (tokens[i].equalsIgnoreCase("operator")) {
                    newValue = StringUtil.replaceAll(newValue, "[operator]", validationRule.getProperty(tokens[i]));
                    continue;
                }
                if (tokens[i].equalsIgnoreCase(TITLE)) {
                    newValue = StringUtil.replaceAll(newValue, "[title]", fieldProps.getProperty(TITLE));
                    continue;
                }
                if (tokens[i].equalsIgnoreCase("value1")) {
                    newValue = StringUtil.replaceAll(newValue, "[value1]", validationRule.getProperty("value1_value", validationRule.getProperty(tokens[i])));
                    continue;
                }
                if (!tokens[i].equalsIgnoreCase("value2")) continue;
                newValue = StringUtil.replaceAll(newValue, "[value2]", validationRule.getProperty("value2_value", validationRule.getProperty(tokens[i])));
            }
        }
        return newValue;
    }

    private boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
        }
        catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public boolean evaluateExpression(String datatype, String lhs, String operator, String rhs) {
        try {
            if (datatype.equals(DATATYPE_NUMBER)) {
                M18NUtil m18n = new M18NUtil(Locale.US, TimeZone.getDefault());
                if (lhs.length() > 0 && rhs.length() > 0) {
                    double lhsVal = m18n.parseBigDecimal(lhs).doubleValue();
                    double rhsVal = m18n.parseBigDecimal(rhs).doubleValue();
                    if ("EQ".equals(operator)) {
                        return lhsVal == rhsVal;
                    }
                    if ("GT".equals(operator)) {
                        return lhsVal > rhsVal;
                    }
                    if ("GE".equals(operator)) {
                        return lhsVal >= rhsVal;
                    }
                    if ("LT".equals(operator)) {
                        return lhsVal < rhsVal;
                    }
                    if ("LE".equals(operator)) {
                        return lhsVal <= rhsVal;
                    }
                    if ("NE".equals(operator)) {
                        return lhsVal != rhsVal;
                    }
                    return false;
                }
            } else {
                if (datatype.equals(DATATYPE_STRING)) {
                    if ("EQ".equals(operator)) {
                        return lhs.equals(rhs);
                    }
                    if ("NE".equals(operator)) {
                        return !lhs.equals(rhs);
                    }
                    if ("CT".equals(operator)) {
                        return lhs.contains(rhs);
                    }
                    if ("DC".equals(operator)) {
                        return !lhs.contains(rhs);
                    }
                    if ("SW".equals(operator)) {
                        return lhs.startsWith(rhs);
                    }
                    if ("EW".equals(operator)) {
                        return lhs.endsWith(rhs);
                    }
                    return false;
                }
                if (datatype.equals(DATATYPE_DATE) && lhs.length() > 0 && rhs.length() > 0) {
                    DateTimeUtil dtu = new DateTimeUtil(this.m18nUtil.getLocale());
                    Calendar lhsDate = dtu.getCalendar(lhs);
                    Calendar rhsDate = dtu.getCalendar(rhs);
                    if (lhsDate != null && rhsDate != null) {
                        int compare = lhsDate.compareTo(rhsDate);
                        if ("EQ".equals(operator)) {
                            return compare == 0;
                        }
                        if ("LT".equals(operator)) {
                            return compare < 0;
                        }
                        if ("GT".equals(operator)) {
                            return compare > 0;
                        }
                        if ("LE".equals(operator)) {
                            return compare <= 0;
                        }
                        if ("GE".equals(operator)) {
                            return compare >= 0;
                        }
                        if ("NE".equals(operator)) {
                            return compare != 0;
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            Trace.log("Failed to evaluate condition:" + lhs + " " + operator + " " + rhs + "." + e.getMessage());
        }
        return false;
    }

    private void replaceFieldValues(SafeSQL safeSQL, PropertyList allFieldValues, int itemnum, String thisfieldname) {
        int i;
        String str = safeSQL.getPreparedSQL();
        if (!str.contains(".value]") && !str.contains(".length")) {
            return;
        }
        Object[] fieldnames = allFieldValues.keySet().toArray();
        DataSet temp = new DataSet();
        temp.addColumn("position", 1);
        temp.addColumn("field", 0);
        temp.addColumn("value", 0);
        temp.addColumn(DATATYPE, 0);
        if (str.contains("[this.value]")) {
            str = str.replaceAll("this.", "" + thisfieldname + ".");
        }
        for (i = 0; i < fieldnames.length; ++i) {
            PropertyListCollection fieldValuesColl = allFieldValues.getCollection((String)fieldnames[i]);
            PropertyList currentFieldVal = fieldValuesColl.size() == 1 ? fieldValuesColl.getPropertyList(0) : fieldValuesColl.getPropertyList(itemnum);
            String currentFieldId = (String)fieldnames[i];
            String currentFieldValue = currentFieldVal.getProperty(ENTEREDTEXT);
            String currentDataType = currentFieldVal.getProperty(DATATYPE);
            while (str.contains("[" + currentFieldId + ".value]")) {
                String pattern = "[" + currentFieldId + ".value]";
                int pos = str.indexOf(pattern);
                if (pos <= -1) continue;
                int item = temp.addRow();
                temp.setNumber(item, "position", pos);
                temp.setValue(item, "field", currentFieldId);
                temp.setValue(item, "value", currentFieldValue);
                temp.setValue(item, DATATYPE, currentDataType);
                String pre = str.substring(0, pos);
                String post = str.substring(pos + pattern.length());
                str = pre + " ? " + post;
            }
            if (!str.contains(".value]") && !str.contains(".length")) break;
        }
        safeSQL.setPreparedSQL(str);
        temp.sort("position");
        for (i = 0; i < temp.size(); ++i) {
            String currVal = temp.getValue(i, "value");
            String currDataType = temp.getValue(i, DATATYPE);
            Object currValObj = currVal;
            if (currDataType.equals(DATATYPE_NUMBER)) {
                currValObj = this.m18nUtil.parseBigDecimal(currVal);
            } else if (currValObj.equals(DATATYPE_DATE)) {
                currValObj = this.m18nUtil.parseCalendar(currVal);
            }
            safeSQL.addVar(currValObj);
        }
    }

    private HashMap getFieldBindings(PropertyListCollection fields, PropertyList allFieldValues, int itemnum) {
        HashMap bindings = new HashMap();
        for (int i = 0; i < fields.size(); ++i) {
            PropertyList currentFieldProps = fields.getPropertyList(i);
            HashMap<String, Object> fieldproperties = new HashMap<String, Object>();
            String currentfieldid = currentFieldProps.getProperty(FIELDID);
            PropertyListCollection currentFieldValues = allFieldValues.getCollection(currentfieldid);
            PropertyList currentItem = currentFieldValues.size() == 1 ? currentFieldValues.getPropertyList(0) : currentFieldValues.getPropertyList(itemnum);
            String currentValue = currentItem.getProperty(ENTEREDTEXT);
            String currentPhysicalRow = currentItem.getProperty(PHYSICALROW);
            String currentPhysicalCol = currentItem.getProperty(PHYSICALCOLUMN);
            String currentDataType = currentItem.getProperty(DATATYPE);
            if (currentDataType.equals(DATATYPE_STRING)) {
                fieldproperties.put("value", currentValue);
            } else if (currentDataType.equals(DATATYPE_DATE)) {
                fieldproperties.put("value", this.m18nUtil.parseCalendar(currentValue));
            } else if (currentDataType.equals(DATATYPE_NUMBER)) {
                fieldproperties.put("value", this.m18nUtil.parseBigDecimal(currentValue));
            }
            fieldproperties.put(DATATYPE, currentFieldProps.getProperty(DATATYPE));
            fieldproperties.put(FIELDTYPE_ROW, currentPhysicalRow);
            fieldproperties.put(FIELDTYPE_COLUMN, currentPhysicalCol);
            bindings.put(currentfieldid, fieldproperties);
        }
        return bindings;
    }

    private HashMap getCurrentFieldMap(PropertyList currentFieldProps, PropertyList allFieldValues, int itemnum) {
        HashMap<String, Object> fieldproperties = new HashMap<String, Object>();
        String currentfieldid = currentFieldProps.getProperty(FIELDID);
        PropertyListCollection currentFieldValues = allFieldValues.getCollection(currentfieldid);
        PropertyList currentItem = currentFieldValues.size() == 1 ? currentFieldValues.getPropertyList(0) : currentFieldValues.getPropertyList(itemnum);
        String currentValue = currentItem.getProperty(ENTEREDTEXT);
        String currentPhysicalRow = currentItem.getProperty(PHYSICALROW);
        String currentPhysicalCol = currentItem.getProperty(PHYSICALCOLUMN);
        String currentDataType = currentItem.getProperty(DATATYPE);
        if (currentDataType.equals(DATATYPE_STRING)) {
            fieldproperties.put("value", currentValue);
        } else if (currentDataType.equals(DATATYPE_DATE)) {
            fieldproperties.put("value", this.m18nUtil.parseCalendar(currentValue));
        } else if (currentDataType.equals(DATATYPE_NUMBER)) {
            fieldproperties.put("value", this.m18nUtil.parseBigDecimal(currentValue));
        }
        fieldproperties.put(DATATYPE, currentFieldProps.getProperty(DATATYPE));
        fieldproperties.put(FIELDTYPE_ROW, currentPhysicalRow);
        fieldproperties.put(FIELDTYPE_COLUMN, currentPhysicalCol);
        return fieldproperties;
    }

    public String parseDateField(String strvalue, String datelocale, String dateformat, String datatz, String erraction) {
        Locale dateLocaleObj = null;
        if (datelocale.length() == 0) {
            dateLocaleObj = this.m18nUtil.getLocale();
            datelocale = dateLocaleObj.getLanguage() + "_" + dateLocaleObj.getCountry();
        } else {
            String[] parts = StringUtil.split(datelocale, "_");
            dateLocaleObj = new Locale(parts[0], parts[1]);
        }
        TimeZone tzObj = null;
        tzObj = datatz.length() == 0 ? this.m18nUtil.getTimezone() : TimeZone.getTimeZone(datatz);
        String ruledesc = "Locale(" + datelocale + "), Format(" + dateformat + "), TimeZone(" + datatz + ")";
        if (dateformat.length() > 0) {
            String def = this.m18nUtil.getDefaultDateFormat().toString();
            SimpleDateFormat csdf = new SimpleDateFormat(dateformat, dateLocaleObj);
            csdf.setLenient(false);
            csdf.setTimeZone(tzObj);
            try {
                Date d = csdf.parse(strvalue);
                return this.m18nUtil.getDefaultDateFormat().format(d);
            }
            catch (ParseException e) {
                return "ParseDateFieldFailed:" + ruledesc + ":" + erraction + ":" + strvalue;
            }
        }
        M18NUtil dateM18N = new M18NUtil(dateLocaleObj, tzObj);
        Calendar o = dateM18N.parseCalendar(strvalue);
        if (o == null) {
            if (erraction.length() == 0) {
                erraction = "Error";
            }
            return "ParseDateFieldFailed:" + ruledesc + ":" + erraction + ":" + strvalue;
        }
        return this.m18nUtil.getDefaultDateFormat().format(o.getTime());
    }

    public String parseNumericField(String value, int row, String col, PropertyList fieldProps, int physicalRow, String physicalCol) {
        String[] parts = this.getNumberFormatRuleParts(fieldProps);
        String decimalseparator = parts[0];
        String groupseparator = parts[1];
        String erroraction = parts[2];
        Trace.logDebug("Converting Cell type: (" + col + "," + (row + 1) + ") celltype:CELL_TYPE_ERROR");
        char currentUserDecimalSeparator = this.formatUtil.getDecimalSeparator();
        char currentGroupSeparator = this.formatUtil.getGroupingSeparator();
        if (decimalseparator.length() == 0 || decimalseparator.equals(Character.valueOf(currentUserDecimalSeparator))) {
            try {
                BigDecimal b = this.formatUtil.parseBigDecimal(value, currentUserDecimalSeparator, currentGroupSeparator, true, true);
                return this.m18nUtil.format(b);
            }
            catch (NumberFormatException e) {
                if (erroraction.length() == 0) {
                    erroraction = "Error";
                }
                return "ParseNumberFieldFailed:Decimal Separator(" + currentUserDecimalSeparator + ") Group Separator(" + currentGroupSeparator + ")" + ":" + erroraction + ":" + value;
            }
        }
        try {
            if (groupseparator.length() == 0) {
                BigDecimal b = this.formatUtil.parseBigDecimal(value, decimalseparator.charAt(0), currentGroupSeparator, false, false);
                return this.m18nUtil.format(b);
            }
            BigDecimal b = this.formatUtil.parseBigDecimal(value, decimalseparator.charAt(0), groupseparator.charAt(0), true, true);
            return this.m18nUtil.format(b);
        }
        catch (NumberFormatException e) {
            if (erroraction.length() == 0) {
                erroraction = "Error";
            }
            return "ParseNumberFieldFailed:Decimal Separator(" + decimalseparator + ") Group Separator(" + groupseparator + ")" + ":" + erroraction + ":" + value;
        }
    }
}

