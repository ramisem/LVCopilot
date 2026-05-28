/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.ro;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.services.SapphireConnection;
import java.io.File;
import java.io.IOException;
import sapphire.SapphireException;
import sapphire.ext.BaseSDCRO;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;

public class LV_DataFileDefRO
extends BaseSDCRO {
    int blockSize = -1;
    boolean abortOnFailure = true;
    String transactionSize = "";
    public static final String DELIMITER = "|!|";
    String fileType = "";
    String delimiter = "";
    String worksheet = "";
    String datafileexample = "";
    String datafileobjects = "";
    String processingscript = "";

    public void initialize(SapphireConnection connection) throws SapphireException {
        super.initialize("LV_DataFileDef", connection);
    }

    @Override
    public int gotoSection(SDI sdi) {
        int ret = super.gotoSection(sdi);
        if (ret > -1) {
            if (this.dataSource.equals("XMLREPORT")) {
                this.processingscript = this.getProcessingScriptFromXMLReport(this.refReportFolder);
                this.datafileobjects = this.getDataFileObjectsFromXMLReport(this.refReportFolder);
                this.datafileexample = this.getDataFileExampleFromXMLReport(this.refReportFolder);
            } else {
                this.processingscript = this.getPrimaryValue("processingscript");
                this.datafileobjects = this.getPrimaryValue("datafileobjects");
                this.datafileexample = this.getPrimaryValue("datafileexample");
            }
        }
        return ret;
    }

    public String getDataFileDefId() {
        return this.getKeyid1();
    }

    public String getDataFileDefDesc() {
        return this.getDescription();
    }

    public String getDataFileDefVersionId() {
        return this.getKeyid2();
    }

    public String getStyle() {
        String colValue = this.getPrimaryValue("datafilestyleflag");
        if ("S".equals(colValue)) {
            return "SimpleGrid";
        }
        if ("C".equals(colValue)) {
            return "CrosstabGrid";
        }
        if ("P".equals(colValue)) {
            return "Composite";
        }
        if ("F".equals(colValue)) {
            return "Freeform";
        }
        return "";
    }

    public String getProcessBlockSize() {
        if (this.currentSDIData != null && this.getPrimaryValue("processingoptions").length() > 0) {
            this.parseProcessOptions();
            if (this.blockSize == -1) {
                return "";
            }
            if (this.blockSize == 1) {
                return "At Each Block";
            }
            return this.blockSize + " Items";
        }
        return "";
    }

    public String getCommitAt() {
        if (this.currentSDIData != null) {
            if (this.getPrimaryValue("processingoptions").length() > 0) {
                this.parseProcessOptions();
                return this.transactionSize;
            }
            return "At Each Block";
        }
        return "";
    }

    public String getDataFileObjects() {
        return this.datafileobjects;
    }

    public DataSet getExampleFile() {
        String xml = this.datafileexample;
        if (xml != null) {
            return new DataSet(xml);
        }
        return new DataSet();
    }

    public String getFailureAction() {
        if (this.currentSDIData != null) {
            if (this.getPrimaryValue("processingoptions").length() > 0) {
                this.parseProcessOptions();
                if (this.abortOnFailure) {
                    return "Abort";
                }
                return "Continue";
            }
            return "Abort";
        }
        return "";
    }

    public String getFileType() {
        if (this.currentSDIData != null) {
            this.parseReadOptions();
            if (this.fileType == null || this.fileType.length() == 0) {
                this.fileType = "Excel";
            }
            return this.fileType;
        }
        return "";
    }

    public String getWorksheet() {
        if (this.currentSDIData != null) {
            this.parseReadOptions();
            if (this.worksheet.length() == 0) {
                this.worksheet = "Sheet1";
            }
            return this.worksheet;
        }
        return "";
    }

    public String getDelimiter() {
        if (this.currentSDIData != null) {
            this.parseReadOptions();
            return this.delimiter;
        }
        return "";
    }

    public String getProcessingScript() throws SapphireException {
        return this.processingscript;
    }

    @Override
    public void setCurrentSDIData(SDIData sdiData) throws SapphireException {
        super.setCurrentSDIData(sdiData);
        this.processingscript = this.getPrimaryValue("processingscript");
        this.datafileobjects = this.getPrimaryValue("datafileobjects");
        this.datafileexample = this.getPrimaryValue("datafileexample");
    }

    private void parseProcessOptions() {
        String processOptions = this.getPrimaryValue("processingoptions");
        if (processOptions.length() > 0) {
            this.blockSize = 1;
            this.abortOnFailure = true;
            if (processOptions == null || processOptions.length() == 0) {
                return;
            }
            String[] options = StringUtil.split(processOptions, DELIMITER);
            for (int i = 0; i < options.length; ++i) {
                String[] tokens = StringUtil.split(options[i], "=");
                if (tokens[0].equals("B")) {
                    if (tokens[1].equals("ALL")) {
                        this.blockSize = -1;
                        this.transactionSize = "At End of All Blocks";
                        continue;
                    }
                    this.blockSize = Integer.parseInt(tokens[1]);
                    continue;
                }
                if (tokens[0].equals("T")) {
                    try {
                        if (tokens[1].equals("1")) {
                            this.transactionSize = "At Each Block";
                            continue;
                        }
                        this.transactionSize = "Every " + Integer.parseInt(tokens[1]) + " Blocks";
                    }
                    catch (Exception ignore) {
                        if (!tokens[1].equals("ALL")) continue;
                        this.transactionSize = "At End of All Blocks";
                    }
                    continue;
                }
                if (!tokens[0].equals("F") || !tokens[1].equals("C")) continue;
                this.abortOnFailure = false;
            }
        }
    }

    private void parseReadOptions() {
        String readOptions = this.getPrimaryValue("readoptions");
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

    private String getProcessingScriptFromXMLReport(String folder) {
        if (this.currentSDI != null) {
            String xmlSdiFileName = this.generateSDISectionXMLFileName(this.currentSDI);
            String xmlProcessingScriptFileName = folder + "/xmlreport/" + xmlSdiFileName.replace(".xml", "_processingscript.xml");
            File f = new File(xmlProcessingScriptFileName);
            try {
                if (f.exists()) {
                    String xml = FileUtil.getFileString(f);
                    return xml;
                }
            }
            catch (IOException e) {
                Trace.log("processing script does not exist in the ref report");
            }
        }
        return "";
    }

    private String getDataFileObjectsFromXMLReport(String folder) {
        if (this.currentSDI != null) {
            String xmlSdiFileName = this.generateSDISectionXMLFileName(this.currentSDI);
            String xmlDataFileObjectsFileName = folder + "/xmlreport/" + xmlSdiFileName.replace(".xml", "_datafileobjects.xml");
            File f = new File(xmlDataFileObjectsFileName);
            try {
                if (f.exists()) {
                    String xml = FileUtil.getFileString(f);
                    return xml;
                }
            }
            catch (IOException e) {
                Trace.log("processing script does not exist in the ref report");
            }
        }
        return "";
    }

    private String getDataFileExampleFromXMLReport(String folder) {
        if (this.currentSDI != null) {
            String xmlSdiFileName = this.generateSDISectionXMLFileName(this.currentSDI);
            String xmlDataFileExampleFileName = folder + "/xmlreport/" + xmlSdiFileName.replace(".xml", "_datafileexample.xml");
            File f = new File(xmlDataFileExampleFileName);
            try {
                if (f.exists()) {
                    String xml = FileUtil.getFileString(f);
                    return xml;
                }
            }
            catch (IOException iOException) {
                // empty catch block
            }
            Trace.log("processing script does not exist in the ref report");
        }
        return "";
    }
}

