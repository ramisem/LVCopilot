/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.transfer;

import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.gwt.shared.util.StringUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.WebAdminService;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.xml.ImportDirective;
import com.labvantage.sapphire.xml.ImportXML;
import com.labvantage.sapphire.xml.StringLogger;
import com.labvantage.sapphire.xml.TransferConstants;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class ImportFile
extends BaseAction
implements TransferConstants {
    public static final String PROPERTY_FILENAME = "filename";
    public static final String PROPERTY_TEMPID = "tempid";
    public static final String PROPERTY_IMPORTLOG = "exportlog";
    public static final String PROPERTY_VERBOSELOG = "verboselog";
    public static final String PROPERTY_COMMITSCOPE = "commitscope";
    public static final String PROPERTY_IGNOREMISSINGOBJECTS = "ignoremissingobjects";
    public static final String PROPERTY_IGNORESEQUENCECHECK = "ignoresequencecheck";
    public static final String PROPERTY_REGENKEYS = "regenkeys";
    public static final String PROPERTY_FORCEUPDATE = "forceupdate";
    public static final String PROPERTY_EXCEPTION = "exception";
    public static final String PROPERTY_IMPORTDIRECTIVES = "importdirectives";
    public static final String PROPERTY_IMPORTDIRECTIVESEPARATOR = "|#|";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processAction(PropertyList props) throws SapphireException {
        String filename = props.getProperty(PROPERTY_FILENAME);
        String tempid = props.getProperty(PROPERTY_TEMPID);
        File file = null;
        if (tempid.length() > 0) {
            FileManager.TempFile tempFile = FileManager.TempFile.getTempFile(tempid, false, this.getQueryProcessor(), this.getConnectionId());
            if (tempFile != null) {
                FileManager.FileData fileData = tempFile.getData();
                if (fileData != null) {
                    file = fileData.getFile().toFile();
                } else {
                    this.logger.warn("Failed to obtain file data.");
                }
            } else {
                this.logger.warn("Failed to obtain temp file.");
            }
        } else {
            file = new File(filename);
        }
        if (file == null) {
            throw new SapphireException("Failed to find import file.");
        }
        ConfigurationProcessor cp = new ConfigurationProcessor(this.getConnectionId());
        String checksumProcessing = cp.getSysConfigProperty("checksumprocessing", "I");
        String exportPackageXML = props.getProperty("exportpackagexml");
        if (exportPackageXML.length() > 0 && !file.exists()) {
            try {
                file = File.createTempFile("exportpackage", ".xml");
                file.deleteOnExit();
                Files.write(Paths.get(file.toURI()), exportPackageXML.getBytes(StandardCharsets.UTF_8), StandardOpenOption.SYNC);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        ImportXML importer = new ImportXML(this.database, file);
        StringLogger outputLogger = new StringLogger();
        importer.setImportLog(outputLogger);
        importer.setImportForceUpdate(props.getProperty(PROPERTY_FORCEUPDATE, "false"));
        importer.setCommitScope(props.getProperty(PROPERTY_COMMITSCOPE, "table"));
        importer.setIgnoreMissingObjects(props.getProperty(PROPERTY_IGNOREMISSINGOBJECTS, "N").equals("Y"));
        importer.setIgnoreSequenceCheck(props.getProperty(PROPERTY_IGNORESEQUENCECHECK, "N").equals("Y"));
        importer.setRegenKeys(props.getProperty(PROPERTY_REGENKEYS, "N").equals("Y"));
        importer.setChecksumProcessing(checksumProcessing);
        importer.setConnectionid(this.getConnectionid());
        if (props.getProperty(PROPERTY_IMPORTDIRECTIVES).length() > 0) {
            String[] importDirectives = StringUtil.split(props.getProperty(PROPERTY_IMPORTDIRECTIVES), PROPERTY_IMPORTDIRECTIVESEPARATOR);
            for (int i = 0; i < importDirectives.length; ++i) {
                String[] directive = StringUtil.split(importDirectives[i], ";");
                importer.setImportDirective(ImportDirective.getReplaceValueDirective(directive[0], directive[1], "", directive[2]));
            }
        }
        try {
            importer.importFiles(props.getProperty(PROPERTY_VERBOSELOG, "N").equals("Y"));
        }
        catch (Exception e) {
            outputLogger.log("Exception raised during import: " + e.getMessage());
            props.setProperty(PROPERTY_EXCEPTION, e.getMessage());
        }
        finally {
            props.setProperty(PROPERTY_IMPORTLOG, outputLogger.getLog());
        }
        String logStr = outputLogger.getLog().toLowerCase();
        if (logStr.indexOf("propertytree") > 0 || logStr.indexOf("webpage") > 0) {
            try {
                WebAdminService was = new WebAdminService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                was.resetCache("WebPageDesigner", "");
            }
            catch (Exception ignore) {
                this.logger.error("Failed to clear the cache. Ignore the error and continue anyway.", ignore);
            }
        } else {
            this.logger.info("Done none Evergreen import.");
        }
    }
}

