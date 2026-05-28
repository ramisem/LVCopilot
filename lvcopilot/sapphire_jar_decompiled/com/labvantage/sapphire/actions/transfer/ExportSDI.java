/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.transfer;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.actions.transfer.TransferAction;
import com.labvantage.sapphire.util.StringHolder;
import com.labvantage.sapphire.util.ant.AntUtil;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.xml.ExportXML;
import com.labvantage.sapphire.xml.SDIDetail;
import com.labvantage.sapphire.xml.SDITransfer;
import com.labvantage.sapphire.xml.StringLogger;
import com.labvantage.sapphire.xml.TransferPackage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.zip.ZipOutputStream;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.action.BaseAction;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ExportSDI
extends BaseAction
implements sapphire.action.ExportSDI,
TransferAction {
    public static final String PROPERTY_CREATEANTFILE = "createantfile";
    public static final String PROPERTY_EXPORTLOG = "exportlog";
    public static final String PROPERTY_OVERWRITEEXISTINGFILES = "overwriteexistingfiles";
    public static final String PROPERTY_TEMPFILEOUTPUT = "tempfileoutput";
    public static final String PROPERTY_TEMPID = "tempid";

    @Override
    public void processAction(PropertyList props) throws SapphireException {
        String sdcid = props.getProperty("sdcid");
        if (sdcid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No SDC specified");
        }
        String keyid1 = props.getProperty("keyid1");
        String keyid2 = props.getProperty("keyid2");
        String keyid3 = props.getProperty("keyid3");
        boolean generateTempFile = props.getProperty(PROPERTY_TEMPFILEOUTPUT, "N").equalsIgnoreCase("Y");
        boolean useZipFile = false;
        String dirname = "";
        if (generateTempFile) {
            useZipFile = true;
            try {
                dirname = FileManager.TempFile.createTempDir();
            }
            catch (Exception e) {
                throw new SapphireException("Failed to create temporary directory.", e);
            }
        } else {
            useZipFile = props.getProperty("zipoutput", "N").equals("Y");
            dirname = props.getProperty("dirname");
        }
        File file = null;
        String filename = dirname + File.separator + props.getProperty("filename");
        if (filename.length() == 0) {
            filename = sdcid + ".xml";
        }
        file = new File(filename);
        FileOutputStream zipFos = null;
        ZipOutputStream zipOut = null;
        StringLogger logger = new StringLogger();
        PropertyList exportProps = new PropertyList();
        exportProps.setProperty("export.sdcid", sdcid);
        exportProps.setProperty("export.keyid1", keyid1);
        exportProps.setProperty("export.keyid2", keyid2);
        exportProps.setProperty("export.keyid3", keyid3);
        DAMProcessor dam = this.getDAMProcessor();
        StringHolder rsetHolder = new StringHolder();
        try {
            ExportXML export;
            ExportXML antexport = null;
            if (props.getProperty("exporttype", "S").equals("S")) {
                SDITransfer sdi = new SDITransfer(sdcid);
                sdi.setKeyid1(keyid1);
                sdi.setKeyid2(keyid2);
                sdi.setKeyid3(keyid3);
                sdi.setPrimaryForceUpdate(props.getProperty("primaryforceupdate", "N"));
                sdi.setPrimaryForceNullUpdate(props.getProperty("primaryforcenullupdate", "N"));
                sdi.setDetailForceUpdate(props.getProperty("primaryforceupdate", "N"));
                sdi.setDetailForceNullUpdate(props.getProperty("primaryforcenullupdate", "N"));
                sdi.setExportDetails(props.getProperty("includedetails", "Y"));
                sdi.setFlushSDI(props.getProperty("flushsdi", "N"));
                sdi.setFlushDetails(props.getProperty("flushdetails", "N"));
                sdi.setExportSDIDetails(props.getProperty("includesdidetails", "Y"));
                sdi.setFlushSDIDetails(props.getProperty("flushsdidetails", "N"));
                sdi.setFlushChildSDI(props.getProperty("flushchildsdi", "N"));
                sdi.setExportFKDetails(props.getProperty("includefkdetails", "N"));
                sdi.setExportSecurityDetails(props.getProperty("includesecuritydetails", "N"));
                if (props.getProperty("includeroles", "Y").equals("Y")) {
                    SDIDetail role = new SDIDetail("sdirole");
                    role.setFlush(props.getProperty("flushroles", "N").equals("Y"));
                    sdi.addSDIDetail(role);
                }
                if (props.getProperty("includecategories", "Y").equals("Y")) {
                    SDIDetail category = new SDIDetail("categoryitem");
                    category.setFlush(props.getProperty("flushcategories", "N").equals("Y"));
                    sdi.addSDIDetail(category);
                }
                sdi.setExcludeAuditColumns(props.getProperty("excludeauditcolumns", "Y").equals("Y"));
                sdi.setSyncDataModel(props.getProperty("syncdatamodel", "N"));
                antexport = export = new ExportXML(this.database, sdi, file);
            } else {
                dam.createRSet(sdcid, keyid1, keyid2, keyid3, rsetHolder);
                exportProps.setProperty("export.rsetid", rsetHolder.value);
                TransferPackage transferPackage = new TransferPackage();
                transferPackage.loadExportScript(this.database, props.getProperty("sdcid"), props.getProperty("exportid"));
                export = transferPackage.getExportScriptExportXML();
                export.setDatabase(this.database);
                export.setFile(file);
                if (props.getProperty(PROPERTY_CREATEANTFILE).equals("Y")) {
                    SDITransfer sdi = new SDITransfer(sdcid);
                    sdi.setKeyid1(keyid1);
                    sdi.setKeyid2(keyid2);
                    sdi.setKeyid3(keyid3);
                    sdi.setExportid(props.getProperty("exportid"));
                    antexport = new ExportXML(this.database, sdi, file);
                }
            }
            FileManager.TempFile tempZipFile = null;
            if (useZipFile) {
                File zipFile = new File(file.getParentFile(), file.getName().substring(0, file.getName().lastIndexOf(".")) + ".zip");
                if (generateTempFile) {
                    tempZipFile = new FileManager.TempFile(zipFile.getName(), FileManager.TempSource.DOWNLOAD, this.getConnectionId());
                    zipFile = tempZipFile.getData().getFile().toFile();
                }
                if (zipFile != null) {
                    zipFile.getParentFile().mkdirs();
                    if (!props.getProperty(PROPERTY_OVERWRITEEXISTINGFILES, "Y").equals("Y") && zipFile.exists()) {
                        throw new SapphireException("Export file '" + zipFile.getAbsolutePath() + "' already exists - try setting the OverrideExistingFiles property = 'true'");
                    }
                    zipFos = new FileOutputStream(zipFile);
                    zipOut = new ZipOutputStream(new BufferedOutputStream(zipFos));
                    export.setZipOut(zipOut);
                }
            }
            export.setOverwriteExistingFiles(props.getProperty(PROPERTY_OVERWRITEEXISTINGFILES, "Y").equals("Y"));
            export.setHeaderAttribute("esigpassword", props.getProperty("esigpassword", "N"));
            export.setHeaderAttribute("esigreason", props.getProperty("esigreason", "N"));
            export.setHeaderAttribute("logimport", props.getProperty("logimport", "Y"));
            export.setHeaderAttribute("checksum", props.getProperty("checksum", "N"));
            export.setExportLog(logger);
            export.export(exportProps);
            StringBuilder tempid = new StringBuilder();
            if (generateTempFile && tempZipFile != null) {
                String id = tempZipFile.setTempFile("Sapphire Custom", this.getConnectionId(), this.getActionProcessor());
                tempid.append(id);
            }
            props.setProperty(PROPERTY_EXPORTLOG, logger.getLog());
            if (props.getProperty(PROPERTY_CREATEANTFILE).equals("Y")) {
                String antfilename = StringUtil.replaceAll(filename, "/", "\\");
                int pos = antfilename.lastIndexOf("\\");
                antfilename = pos == -1 ? "ant_" + antfilename : antfilename.substring(0, pos + 1) + "ant_" + antfilename.substring(pos + 1);
                File antfile = new File(antfilename);
                FileManager.TempFile tempAntFile = null;
                if (generateTempFile) {
                    tempAntFile = new FileManager.TempFile(antfile.getName(), FileManager.TempSource.DOWNLOAD, this.getConnectionId());
                    antfile = tempAntFile.getData().getFile().toFile();
                    antexport.setFilename(null);
                }
                HashMap antProps = new HashMap();
                antProps.putAll(AntUtil.getConnectionProps(this.connectionInfo));
                antexport.generateAntScript(antfile, "export", antProps);
                if (generateTempFile && tempAntFile != null) {
                    String id = tempAntFile.setTempFile("Sapphire Custom", this.getConnectionId(), this.getActionProcessor());
                    tempid.append(";").append(id);
                }
            }
            props.setProperty(PROPERTY_TEMPID, tempid.toString());
        }
        catch (Exception e) {
            throw new SapphireException("Failed to export " + new SDI(sdcid, keyid1, keyid2, keyid3).getKeyText() + ". Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        finally {
            try {
                if (zipOut != null) {
                    zipOut.close();
                }
                if (zipFos != null) {
                    zipFos.close();
                }
            }
            catch (IOException iOException) {}
            if (rsetHolder.value != null && rsetHolder.value.length() > 0) {
                dam.clearRSet(rsetHolder.value);
            }
            if (generateTempFile) {
                try {
                    Files.delete(FileSystems.getDefault().getPath(dirname, new String[0]));
                }
                catch (Exception e2) {
                    this.logTrace("Failed to delete temp directory.");
                }
            }
        }
    }

    @Override
    public PropertyList convertWizardProperties(PropertyList wizardProps, String connectionid) throws Exception {
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty("sdcid", wizardProps.getProperty("sdcid"));
        actionProps.setProperty("keyid1", wizardProps.getProperty("keyid1"));
        actionProps.setProperty("keyid2", wizardProps.getProperty("keyid2"));
        actionProps.setProperty("keyid3", wizardProps.getProperty("keyid3"));
        actionProps.setProperty("exporttype", wizardProps.getProperty("exporttype"));
        actionProps.setProperty("flushsdi", wizardProps.getProperty("flushsdi"));
        actionProps.setProperty("includedetails", wizardProps.getProperty("includedetails"));
        actionProps.setProperty("flushdetails", wizardProps.getProperty("flushdetails"));
        actionProps.setProperty("includesdidetails", wizardProps.getProperty("includesdidetails"));
        actionProps.setProperty("flushsdidetails", wizardProps.getProperty("flushsdidetails"));
        actionProps.setProperty("flushchildsdi", wizardProps.getProperty("flushchildsdi"));
        actionProps.setProperty("includefkdetails", wizardProps.getProperty("includefkdetails"));
        actionProps.setProperty("includesecuritydetails", wizardProps.getProperty("includesecuritydetails"));
        actionProps.setProperty("includeroles", wizardProps.getProperty("includeroles"));
        actionProps.setProperty("flushroles", wizardProps.getProperty("flushroles"));
        actionProps.setProperty("includecategories", wizardProps.getProperty("includecategories"));
        actionProps.setProperty("flushcategories", wizardProps.getProperty("flushcategories"));
        actionProps.setProperty("primaryforceupdate", wizardProps.getProperty("primaryforceupdate"));
        actionProps.setProperty("primaryforcenullupdate", wizardProps.getProperty("primaryforcenullupdate"));
        actionProps.setProperty("syncdatamodel", wizardProps.getProperty("syncdatamodel"));
        actionProps.setProperty("excludeauditcolumns", wizardProps.getProperty("excludeauditcolumns"));
        actionProps.setProperty(PROPERTY_OVERWRITEEXISTINGFILES, wizardProps.getProperty(PROPERTY_OVERWRITEEXISTINGFILES));
        actionProps.setProperty("exportid", wizardProps.getProperty("exportid"));
        actionProps.setProperty("esigpassword", wizardProps.getProperty("esigpassword"));
        actionProps.setProperty("esigreason", wizardProps.getProperty("esigreason"));
        actionProps.setProperty("logimport", wizardProps.getProperty("logimport"));
        actionProps.setProperty("checksum", wizardProps.getProperty("checksum"));
        actionProps.setProperty("zipoutput", wizardProps.getProperty("zipoutput"));
        actionProps.setProperty(PROPERTY_CREATEANTFILE, wizardProps.getProperty(PROPERTY_CREATEANTFILE));
        actionProps.setProperty("dirname", wizardProps.getProperty("folderlocation"));
        actionProps.setProperty("filename", wizardProps.getProperty("filename"));
        actionProps.setProperty(PROPERTY_TEMPFILEOUTPUT, wizardProps.getProperty(PROPERTY_TEMPFILEOUTPUT));
        return actionProps;
    }
}

