/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.datafile;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.system.AttachmentProcessor;
import com.labvantage.sapphire.modules.datafile.DataFile;
import com.labvantage.sapphire.modules.datafile.DataFileReader;
import com.labvantage.sapphire.modules.datafile.DataFileUtil;
import com.labvantage.sapphire.modules.datafile.ExcelFileReader;
import com.labvantage.sapphire.modules.datafile.TextFileReader;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.servlet.command.AttachmentRequest;
import java.io.File;
import java.io.FileOutputStream;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ImportDataFile
extends BaseAction
implements sapphire.action.ImportDataFile {
    public static final String VERSION = "1";
    private PropertyListCollection processLogItems = new PropertyListCollection();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        StringBuffer log = new StringBuffer();
        String messageTypeId = properties.getProperty("messagetypeid", "");
        String dataFileDefId = properties.getProperty("datafiledefid", "");
        String dataFileDefVersionId = properties.getProperty("datafiledefversionid", VERSION);
        String documentField = properties.getProperty("documentfield");
        String overrideworksheet = properties.getProperty("worksheet");
        String overrideworksheetnum = properties.getProperty("worksheetnumber");
        boolean verbose = "Y".equals(properties.getProperty("verbose", "Y"));
        String validateonly = properties.getProperty("validateonly", "N");
        boolean process = !validateonly.equals("Y");
        String commitScope = properties.getProperty("commitscope", "Default");
        String timeout = properties.getProperty("timeout", "-1");
        int timeoutperiod = Integer.parseInt(timeout);
        long starttime = System.currentTimeMillis();
        DataFile dataFile = new DataFile();
        if (messageTypeId.length() == 0 && dataFileDefId.length() == 0) {
            throw new SapphireException("Message Type or DataFileDef Id need to be provided");
        }
        File tempFile = null;
        String messagelogid = properties.getProperty("messagelogid", "");
        try {
            if (messageTypeId.length() > 0 || messagelogid.length() > 0) {
                String message = properties.getProperty("message", "");
                String filePath = properties.getProperty("path", "");
                String fileName = properties.getProperty("filename", "");
                if (message.length() == 0 || message.equals(fileName) || message.equals(messagelogid)) {
                    if (messagelogid.length() == 0 && (fileName.length() == 0 || filePath.length() == 0)) {
                        throw new SapphireException("Import Data failed: Either message should be passed in or file specified");
                    }
                    Trace.logDebug("Initializing the DataFile object");
                    dataFile.initialize(this.getConnectionProcessor().getSapphireConnection(), this.getActionProcessor(), this.getQueryProcessor(), this.getSDCProcessor(), this.getTranslationProcessor(), messageTypeId, overrideworksheet, overrideworksheetnum, commitScope);
                    String type = dataFile.getStyle();
                    if (type.equals("P")) {
                        int i;
                        if (!dataFile.getFileType().equals("excel")) {
                            throw new SapphireException("Composite import can be done on excel files only!");
                        }
                        ExcelFileReader dataReader = fileName.length() == 0 && messagelogid.length() > 0 ? new ExcelFileReader(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()), "M", "", messagelogid, true) : new ExcelFileReader(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()), filePath, fileName, true);
                        dataReader.initialize();
                        String[] actualsheetnames = dataReader.getSheetNames();
                        DataSet childDFDInfo = dataFile.getChildDFDInfo();
                        for (i = 0; i < childDFDInfo.getRowCount(); ++i) {
                            String expectedworksheet = childDFDInfo.getValue(i, "excelworksheetname", "");
                            boolean found = false;
                            for (int j = 0; j < actualsheetnames.length; ++j) {
                                if (!actualsheetnames[j].equals(expectedworksheet)) continue;
                                found = true;
                                break;
                            }
                            if (found) continue;
                            throw new SapphireException("Did not find worksheet with name:" + expectedworksheet + " in the input excel file");
                        }
                        for (i = 0; i < childDFDInfo.getRowCount(); ++i) {
                            PropertyList props = properties.copy();
                            props.setProperty("messagetypeid", childDFDInfo.getValue(i, "refdatafiledefid", ""));
                            props.setProperty("datafiledefid", childDFDInfo.getValue(i, "refdatafiledefid", ""));
                            props.setProperty("datafiledefversionid", childDFDInfo.getValue(i, "refdatafiledefversionid", ""));
                            props.setProperty("worksheet", childDFDInfo.getValue(i, "excelworksheetname"));
                            if (dataFile.commitPerWorksheet) {
                                props.setProperty("commitscope", "All");
                            } else if (dataFile.deferToChildTransaction) {
                                props.setProperty("commitscope", "Default");
                            } else if (dataFile.noNewTransaction) {
                                props.setProperty("commitscope", "None");
                            }
                            try {
                                this.getActionProcessor().processAction("ImportDataFile", VERSION, props);
                                String childvalidationlogstr = props.getProperty("validationlog", "");
                                String childprocesslog = props.getProperty("log", "");
                                String childprocessstatus = props.getProperty("status", "SUCCESS");
                                log.append(childprocesslog);
                                if (!childprocessstatus.equals("SUCCESS")) {
                                    PropertyListCollection childvalidationlog = new PropertyListCollection();
                                    childvalidationlog.setJSONString(childvalidationlogstr);
                                    dataFile.reviewItems.addAll(childvalidationlog);
                                }
                                if (dataFile.getReviewItems().find("status", "FAIL") != null) {
                                    log.append("<P>Processing of child DFD has validation errors.</P>");
                                    properties.setProperty("status", "FAILED");
                                } else if (dataFile.getReviewItems().find("status", "WARNING") != null) {
                                    log.append("<P>Processing of child DFD has validation warnings.<P>");
                                    properties.setProperty("status", "WARNING");
                                } else {
                                    log.append("<P>Processing of child DFD was done successfully.</P>");
                                    properties.setProperty("status", "SUCCESS");
                                }
                                properties.setProperty("validationlog", dataFile.getReviewItems().toJSONString());
                                if (!childprocessstatus.equals("FAILED") || !process) continue;
                                log.append("<P><font color='red'>Cannot process another other child DFDs since the processing of worksheet " + childDFDInfo.getValue(i, "excelworksheetname") + " failed!</font>");
                                return;
                            }
                            catch (SapphireException e) {
                                log.append("Failed to process child DFD:" + childDFDInfo.getValue(i, "refdatafiledefid", "") + "," + childDFDInfo.getValue(i, "refdatafiledefversionid", ""));
                                log.append("\nImportDataFile failed: " + e.getMessage());
                                properties.setProperty("status", "FAILED");
                                properties.setProperty("error", e.getMessage());
                                properties.setProperty("validationlog", dataFile.getReviewItems().toJSONString());
                            }
                        }
                        return;
                    }
                    int sliceSize = -1;
                    DataFileReader dataReader = dataFile.getFileType().equals("excel") ? (fileName.length() == 0 && messagelogid.length() > 0 ? new ExcelFileReader(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()), "M", "", messagelogid, dataFile.getWorksheet(), sliceSize) : new ExcelFileReader(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()), filePath, fileName, dataFile.getWorksheet(), sliceSize)) : (fileName.length() == 0 && messagelogid.length() > 0 ? new TextFileReader(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()), "M", "", messagelogid, sliceSize, dataFile.getDelimiter()) : new TextFileReader(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()), filePath, fileName, sliceSize, dataFile.getDelimiter()));
                    dataReader.setFileEncoding(properties.getProperty("encoding", "UTF-8"));
                    Trace.logDebug("Initializing the DataFile Reader object");
                    dataReader.initialize();
                    if (type.equals("S") && dataFile.canBeProcessedInChunks()) {
                        int headerRowNum = dataFile.getHeaderRowNum();
                        DataSet headerRow = dataReader.getHeaderRow(headerRowNum);
                        if (dataFile.getBlocksPerTransaction() != -1 && dataFile.getBlockSize() != -1) {
                            sliceSize = dataFile.getBlockSize() * dataFile.getBlocksPerTransaction();
                            dataReader.setSliceSize(sliceSize);
                        }
                        Trace.logDebug("SliceSize is:" + sliceSize);
                        int sliceStart = headerRowNum + 1;
                        dataReader.setSliceStart(sliceStart);
                        dataFile.getColumnDataTypes();
                        DataSet currDataSlice = dataReader.getNextSlice(dataFile);
                        DataSet currFinalSlice = new DataSet();
                        currFinalSlice.setColidCaseSensitive(false);
                        if (headerRowNum > 0) {
                            currFinalSlice.copyRow(headerRow, 0, 1);
                        }
                        currFinalSlice.copyRow(currDataSlice, -1, 1);
                        boolean timedout = false;
                        while (currFinalSlice != null && currFinalSlice.getRowCount() >= 2) {
                            long currenttime;
                            Trace.logDebug("Processing Slice...");
                            if ("Y".equals(validateonly) && timeoutperiod != -1 && (currenttime = System.currentTimeMillis()) - starttime > (long)timeoutperiod) {
                                timedout = true;
                                break;
                            }
                            if (sliceSize == -1) {
                                sliceSize = currDataSlice.getRowCount();
                            }
                            int sliceEnd = sliceStart + Math.min(sliceSize, currFinalSlice.getRowCount()) - 1;
                            StringBuffer out = new StringBuffer();
                            DataFileUtil.renderExcelSheet(out, currFinalSlice, sliceStart, true);
                            if (!process) {
                                log.append("<P>Validating rows:</P>" + out);
                            } else {
                                log.append("<P>Processing rows:</P>" + out);
                            }
                            int currslicestatus = dataFile.processSlice(headerRowNum, sliceStart, currFinalSlice, properties, verbose, process);
                            if (currslicestatus == 1 || currslicestatus == 2) {
                                if (process) {
                                    if (dataFile.getReviewItems().find("status", "FAIL") != null) {
                                        if (sliceStart == sliceEnd) {
                                            log.append("<P>Row(" + sliceStart + ") has validation errors.</P>");
                                        } else {
                                            log.append("<P>Rows(" + sliceStart + " to " + sliceEnd + ") has validation errors.</P>");
                                        }
                                        properties.setProperty("status", "FAILED");
                                    } else if (dataFile.getReviewItems().find("status", "WARNING") != null) {
                                        if (sliceStart == sliceEnd) {
                                            log.append("<P>Row(" + sliceStart + ") has validation warnings.<P>");
                                        } else {
                                            log.append("<P>Rows(" + sliceStart + " to " + sliceEnd + ") has validation warnings.<P>");
                                        }
                                        properties.setProperty("status", "WARNING");
                                    } else {
                                        if (sliceStart == sliceEnd) {
                                            log.append("<P>Row(" + sliceStart + ") was done successfully.</P>");
                                        } else {
                                            log.append("<P>Rows(" + sliceStart + " to " + sliceEnd + ") was done successfully.</P>");
                                        }
                                        properties.setProperty("status", "SUCCESS");
                                    }
                                    properties.setProperty("validationlog", dataFile.getReviewItems().toJSONString());
                                } else {
                                    if (dataFile.getReviewItems().find("status", "FAIL") != null) {
                                        properties.setProperty("status", "FAILED");
                                    } else if (dataFile.getReviewItems().find("status", "WARNING") != null) {
                                        properties.setProperty("status", "WARNING");
                                    } else {
                                        properties.setProperty("status", "SUCCESS");
                                    }
                                    properties.setProperty("validationlog", dataFile.getReviewItems().toJSONString());
                                }
                            } else {
                                if (currslicestatus == -1) {
                                    log.append("<P>Error: Failed to process items:" + sliceStart + " to " + sliceEnd + "</P>");
                                    log.append("<P>Aborted processing the file.</P>");
                                    properties.setProperty("status", "FAILED");
                                    properties.setProperty("validationlog", dataFile.getReviewItems().toJSONString());
                                    properties.setProperty("error", ErrorUtil.extractMessage(properties.getProperty("ErrorMsg", ""), ErrorUtil.isUserAdmin(this.getConnectionId())));
                                    if (!verbose) break;
                                    log.append("<P>");
                                    log.append(dataFile.getLog());
                                    break;
                                }
                                if (currslicestatus == 0) {
                                    log.append("<P>Failed processing:" + sliceStart + " to " + sliceEnd + "</P>");
                                    properties.setProperty("validationlog", dataFile.getReviewItems().toJSONString());
                                    properties.setProperty("status", "FAILED");
                                    properties.setProperty("error", ErrorUtil.extractMessage(properties.getProperty("ErrorMsg", ""), ErrorUtil.isUserAdmin(this.getConnectionId())));
                                }
                            }
                            if (verbose) {
                                log.append("<P>");
                                log.append(dataFile.getLog());
                            }
                            if ((sliceStart = sliceEnd + 1) > dataReader.getLineCount()) break;
                            currDataSlice = dataReader.getNextSlice(dataFile);
                            currFinalSlice = new DataSet();
                            if (headerRowNum > 0) {
                                currFinalSlice.copyRow(headerRow, 0, 1);
                            }
                            currFinalSlice.copyRow(currDataSlice, -1, 1);
                            dataFile.clearLog();
                        }
                        if (!timedout) return;
                        properties.setProperty("timedout", "Y");
                        return;
                    } else {
                        dataFile.initialize(this.getConnectionProcessor().getSapphireConnection(), this.getActionProcessor(), this.getQueryProcessor(), this.getSDCProcessor(), this.getTranslationProcessor(), messageTypeId, overrideworksheet, overrideworksheetnum, commitScope);
                        log.append("Initialization Successful.\n");
                        if (dataReader instanceof ExcelFileReader) {
                            ((ExcelFileReader)dataReader).fetchDataFileParams();
                        }
                        DataSet data = dataReader.getFileContent(dataFile);
                        int status = dataFile.process(data, properties, verbose, process);
                        log.append("<P>");
                        log.append(dataFile.getLog());
                        if (status == -1) {
                            properties.setProperty("status", "FAILED");
                            properties.setProperty("error", ErrorUtil.extractMessage(properties.getProperty("ErrorMsg", ""), ErrorUtil.isUserAdmin(this.getConnectionId())) + ". Processing Aborted.");
                            properties.setProperty("validationlog", dataFile.getReviewItems().toJSONString());
                            return;
                        } else if (status == 0) {
                            properties.setProperty("status", "FAILED");
                            properties.setProperty("error", ErrorUtil.extractMessage(properties.getProperty("ErrorMsg", ""), ErrorUtil.isUserAdmin(this.getConnectionId())) + ". Processing completed with errors.");
                            properties.setProperty("validationlog", dataFile.getReviewItems().toJSONString());
                            return;
                        } else {
                            properties.setProperty("status", "SUCCESS");
                            properties.setProperty("error", ErrorUtil.extractMessage(properties.getProperty("ErrorMsg", ""), ErrorUtil.isUserAdmin(this.getConnectionId())) + ". Processing completed with no errors.");
                            properties.setProperty("responsemessage", "Import process is done");
                            properties.setProperty("validationlog", dataFile.getReviewItems().toJSONString());
                        }
                    }
                    return;
                }
                DataSet excelGrid = new DataSet(message);
                dataFile.initialize(this.getConnectionProcessor().getSapphireConnection(), this.getActionProcessor(), this.getQueryProcessor(), this.getSDCProcessor(), this.getTranslationProcessor(), messageTypeId, overrideworksheet, overrideworksheetnum, commitScope);
                log.append("Initialization completed successfully.\n");
                int status = dataFile.process(excelGrid, properties, verbose, process);
                Trace.logDebug("DataFile process log..." + dataFile.getLog());
                log.append("<P>");
                log.append(dataFile.getLog());
                if (status == -1) {
                    properties.setProperty("status", "FAILED");
                    properties.setProperty("error", ErrorUtil.extractMessage(properties.getProperty("ErrorMsg", ""), ErrorUtil.isUserAdmin(this.getConnectionId())) + ". Processing Aborted.");
                    properties.setProperty("validationlog", dataFile.getReviewItems().toJSONString());
                    return;
                } else if (status == 0) {
                    properties.setProperty("status", "FAILED");
                    properties.setProperty("error", ErrorUtil.extractMessage(properties.getProperty("ErrorMsg", ""), ErrorUtil.isUserAdmin(this.getConnectionId())) + ". Processing completed with errors.");
                    properties.setProperty("validationlog", dataFile.getReviewItems().toJSONString());
                    return;
                } else {
                    properties.setProperty("status", "SUCCESS");
                    properties.setProperty("error", ErrorUtil.extractMessage(properties.getProperty("ErrorMsg", ""), ErrorUtil.isUserAdmin(this.getConnectionId())) + ". Processing completed with no errors.");
                    properties.setProperty("responsemessage", "Import process is done");
                    properties.setProperty("validationlog", dataFile.getReviewItems().toJSONString());
                }
                return;
            }
            String filePath = properties.getProperty("path", "");
            String fileName = properties.getProperty("filename", "");
            if (fileName.length() == 0 && documentField.length() > 0) {
                String[] parts = StringUtil.split(documentField, "|");
                if (parts.length != 4) throw new SapphireException("Document field reference '" + documentField + "' is invalid");
                this.database.createPreparedResultSet("SELECT attachmentnum FROM sdiattachment WHERE sdcid = 'LV_Document' AND keyid1 = ? AND keyid2 = ? AND attachmentuse = 'FileField' AND attachmentlabel  = ?", new Object[]{parts[0], parts[1], parts[2] + "|" + parts[3] + "|0"});
                if (!this.database.getNext()) throw new SapphireException("Document field attachment not found!");
                AttachmentProcessor arp = new AttachmentProcessor(this.connectionInfo.getConnectionId());
                Attachment attachment = arp.getSDIAttachment("LV_Document", parts[0], parts[1], "(null)", this.database.getInt("attachmentnum"));
                tempFile = File.createTempFile("documentfield_", "");
                AttachmentRequest.streamAttachment(attachment.getInputStream(), new FileOutputStream(tempFile));
                filePath = tempFile.getParentFile().getAbsolutePath();
                fileName = tempFile.getName();
            }
            String deleteFile = properties.getProperty("deletefile", "N");
            if (filePath.length() == 0) {
                throw new SapphireException("File path is invalid");
            }
            filePath = filePath.replaceAll("\\\\", "/");
            if (fileName.length() == 0) {
                fileName = filePath.substring(filePath.lastIndexOf(47) + 1);
                filePath = filePath.substring(0, filePath.lastIndexOf(47) + 1);
            }
            if (dataFileDefVersionId.equals("C")) {
                dataFileDefVersionId = DataFile.getDataFileDefCurrentVersion(this.getQueryProcessor(), dataFileDefId);
            }
            PropertyList readActionProps = new PropertyList();
            readActionProps.setProperty("path", filePath);
            readActionProps.setProperty("filename", fileName);
            readActionProps.setProperty("datafiledefid", dataFileDefId);
            readActionProps.setProperty("datafiledefversionid", dataFileDefVersionId);
            readActionProps.setProperty("deletefile", deleteFile);
            readActionProps.setProperty("messagetypeid", dataFileDefId);
            readActionProps.setProperty("worksheet", overrideworksheet);
            readActionProps.setProperty("worksheetnumber", overrideworksheetnum);
            this.getActionProcessor().processAction("ReadDataFile", VERSION, readActionProps);
            String fileContent = readActionProps.getProperty("filecontent", "");
            DataSet excelGrid = new DataSet(fileContent);
            dataFile.initialize(this.getConnectionProcessor().getSapphireConnection(), this.getActionProcessor(), this.getQueryProcessor(), this.getSDCProcessor(), this.getTranslationProcessor(), dataFileDefId, dataFileDefVersionId, overrideworksheet, overrideworksheetnum);
            log.append("Initialization Successful.\n");
            int status = dataFile.process(excelGrid, properties, verbose, process);
            log.append("<P>");
            log.append(dataFile.getLog());
            if (status == -1) {
                properties.setProperty("status", "FAILED");
                properties.setProperty("error", ErrorUtil.extractMessage(properties.getProperty("ErrorMsg", ""), ErrorUtil.isUserAdmin(this.getConnectionId())) + ". Processing Aborted.");
                properties.setProperty("validationlog", dataFile.getReviewItems().toJSONString());
                return;
            } else if (status == 0) {
                properties.setProperty("status", "FAILED");
                properties.setProperty("validationlog", dataFile.getReviewItems().toJSONString());
                properties.setProperty("error", ErrorUtil.extractMessage(properties.getProperty("ErrorMsg", ""), ErrorUtil.isUserAdmin(this.getConnectionId())) + ". Processing completed with errors.");
                return;
            } else {
                properties.setProperty("status", "SUCCESS");
                properties.setProperty("validationlog", dataFile.getReviewItems().toJSONString());
                properties.setProperty("responsemessage", "Import process is done");
            }
            return;
        }
        catch (SapphireException e) {
            log.append("\nImportDataFile failed: " + e.getMessage());
            properties.setProperty("status", "FAILED");
            properties.setProperty("error", e.getMessage());
            properties.setProperty("validationlog", dataFile.getReviewItems().toJSONString());
            if (tempFile == null || !tempFile.exists()) return;
            tempFile.delete();
            return;
        }
        catch (Exception e) {
            log.append("\nImportDataFile failed: " + e.getMessage());
            properties.setProperty("status", "FAILED");
            properties.setProperty("error", "Unexpected exception." + e.getMessage());
            properties.setProperty("validationlog", dataFile.getReviewItems().toJSONString());
            if (tempFile == null || !tempFile.exists()) return;
            tempFile.delete();
            return;
        }
        finally {
            properties.setProperty("log", log.toString());
        }
    }

    void addProcessLogEntry(String heading, String description, String timestamp) {
        PropertyList props = new PropertyList();
        props.setProperty("heading", heading);
        props.setProperty("description", description);
        props.setProperty("timestamp", timestamp);
        this.processLogItems.add(props);
    }
}

