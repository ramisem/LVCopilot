/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.transfer;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.actions.transfer.TransferAction;
import com.labvantage.sapphire.util.ant.AntUtil;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.xml.ExportXML;
import com.labvantage.sapphire.xml.SDIDetail;
import com.labvantage.sapphire.xml.SDITransfer;
import com.labvantage.sapphire.xml.StringLogger;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipOutputStream;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DBAccess;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ExportWebPage
extends BaseAction
implements sapphire.action.ExportWebPage,
TransferAction {
    public static final String PROPERTY_CREATEANTFILE = "createantfile";
    public static final String PROPERTY_EXPORTLOG = "exportlog";
    public static final String PROPERTY_PROPERTYEXISTS = "propertyexists";
    public static final String OPTION_WEBPAGE_INCLUDENODES = "includenodes";
    public static final String OPTION_WEBPAGE_EXCLUDEGENERICLAYOUT = "excludegenericlayout";
    public static final String OPTION_WEBPAGE_NODEEXISTS = "nodeexists";
    public static final String OPTION_WEBPAGE_NODEPROPERTYEXISTS = "nodepropertyexists";
    public static final String PROPERTY_TEMPFILEOUTPUT = "tempfileoutput";
    public static final String PROPERTY_TEMPID = "tempid";

    @Override
    public void processAction(PropertyList props) throws SapphireException {
        String webpageid = props.getProperty("webpageid");
        String productedition = props.getProperty("productedition");
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
        String filename = dirname + File.separator + props.getProperty("filename");
        ArrayList<SDITransfer> transferList = new ArrayList<SDITransfer>();
        SDITransfer sdi = new SDITransfer("WebPage");
        sdi.setKeyid1(webpageid);
        sdi.setKeyid2(productedition);
        sdi.setTransferOption(OPTION_WEBPAGE_INCLUDENODES, props.getProperty(OPTION_WEBPAGE_INCLUDENODES, "Y"));
        sdi.setTransferOption(OPTION_WEBPAGE_EXCLUDEGENERICLAYOUT, props.getProperty(OPTION_WEBPAGE_EXCLUDEGENERICLAYOUT, "Y"));
        sdi.setTransferOption(OPTION_WEBPAGE_NODEEXISTS, props.getProperty(OPTION_WEBPAGE_NODEEXISTS, "replace"));
        sdi.setTransferOption(OPTION_WEBPAGE_NODEPROPERTYEXISTS, props.getProperty(OPTION_WEBPAGE_NODEPROPERTYEXISTS, "replace"));
        if (props.getProperty("includeroles").equals("Y")) {
            sdi.addSDIDetail(new SDIDetail("sdirole"));
        }
        if (props.getProperty("includecategories").equals("Y")) {
            sdi.addSDIDetail(new SDIDetail("categoryitem"));
        }
        if (props.getProperty("overrideexists").equals("replace")) {
            sdi.setPrimaryForceUpdate("true");
            sdi.setDetailForceUpdate("true");
        }
        transferList.add(sdi);
        File file = new File(filename);
        FileOutputStream zipFos = null;
        ZipOutputStream zipOut = null;
        StringLogger logger = new StringLogger();
        try {
            ExportXML export = new ExportXML((DBAccess)((DBUtil)this.database), transferList, file);
            FileManager.TempFile tempZipFile = null;
            if (useZipFile) {
                File zipFile = new File(file.getParentFile(), file.getName().substring(0, file.getName().lastIndexOf(".")) + ".zip");
                if (generateTempFile) {
                    tempZipFile = new FileManager.TempFile(zipFile.getName(), FileManager.TempSource.DOWNLOAD, this.getConnectionId());
                    zipFile = tempZipFile.getData().getFile().toFile();
                }
                if (zipFile != null) {
                    zipFile.getParentFile().mkdirs();
                    zipFos = new FileOutputStream(zipFile);
                    zipOut = new ZipOutputStream(new BufferedOutputStream(zipFos));
                    export.setZipOut(zipOut);
                }
            }
            export.setHeaderAttribute("esigpassword", props.getProperty("esigpassword", "N"));
            export.setHeaderAttribute("esigreason", props.getProperty("esigreason", "N"));
            export.setHeaderAttribute("logimport", props.getProperty("logimport", "Y"));
            export.setHeaderAttribute("checksum", props.getProperty("checksum", "N"));
            export.setExportLog(logger);
            export.export();
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
                    export.setFilename(null);
                }
                HashMap antProps = new HashMap();
                antProps.putAll(AntUtil.getConnectionProps(this.connectionInfo));
                export.generateAntScript(antfile, "export", antProps);
                if (generateTempFile && tempAntFile != null) {
                    String id = tempAntFile.setTempFile("Sapphire Custom", this.getConnectionId(), this.getActionProcessor());
                    tempid.append(";").append(id);
                }
            }
            props.setProperty(PROPERTY_TEMPID, tempid.toString());
        }
        catch (Exception e) {
            throw new SapphireException("Failed to export webpage " + webpageid + ". Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
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
        actionProps.setProperty("webpageid", wizardProps.getProperty("webpageid"));
        actionProps.setProperty("productedition", wizardProps.getProperty("productedition"));
        actionProps.setProperty("overrideexists", wizardProps.getProperty("overrideexists"));
        actionProps.setProperty(OPTION_WEBPAGE_INCLUDENODES, wizardProps.getProperty(OPTION_WEBPAGE_INCLUDENODES));
        actionProps.setProperty(OPTION_WEBPAGE_EXCLUDEGENERICLAYOUT, wizardProps.getProperty(OPTION_WEBPAGE_EXCLUDEGENERICLAYOUT));
        actionProps.setProperty("includeroles", wizardProps.getProperty("includeroles"));
        actionProps.setProperty("includecategories", wizardProps.getProperty("includecategories"));
        actionProps.setProperty(OPTION_WEBPAGE_NODEEXISTS, wizardProps.getProperty(OPTION_WEBPAGE_NODEEXISTS));
        actionProps.setProperty(OPTION_WEBPAGE_NODEPROPERTYEXISTS, wizardProps.getProperty(OPTION_WEBPAGE_NODEPROPERTYEXISTS));
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

