/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.transfer;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.util.ant.AntUtil;
import com.labvantage.sapphire.xml.ExportXML;
import com.labvantage.sapphire.xml.StringLogger;
import com.labvantage.sapphire.xml.TableTransfer;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.ZipOutputStream;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ExportTable
extends BaseAction
implements sapphire.action.ExportTable {
    public static final String PROPERTY_CREATEANTFILE = "createantfile";
    public static final String PROPERTY_EXPORTLOG = "exportlog";
    public static final String PROPERTY_OVERWRITEEXISTINGFILES = "overwriteexistingfiles";

    @Override
    public void processAction(PropertyList props) throws SapphireException {
        String tableid = props.getProperty("tableid");
        if (tableid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No table specified");
        }
        String filename = props.getProperty("dirname") + "/" + props.getProperty("filename");
        if (filename.length() == 0) {
            filename = tableid + ".xml";
        }
        File file = new File(filename);
        FileOutputStream zipFos = null;
        ZipOutputStream zipOut = null;
        StringLogger logger = new StringLogger();
        try {
            File zipFile;
            ExportXML export;
            ExportXML antexport = null;
            TableTransfer table = new TableTransfer(tableid);
            table.setFrom(props.getProperty("from"));
            table.setWhere(props.getProperty("where"));
            table.setOrderby(props.getProperty("orderby"));
            table.setDefaultForceUpdate(props.getProperty("defaultforceupdate", "N"));
            table.setDefaultForceNullUpdate(props.getProperty("defaultforcenullupdate", "N"));
            table.setForceLOBExport(props.getProperty("forcelobexport", "N").equals("Y"));
            antexport = export = new ExportXML(this.database, table, file);
            if (props.getProperty("zipoutput", "N").equals("Y") && (zipFile = new File(file.getParentFile(), file.getName().substring(0, file.getName().lastIndexOf(".")) + ".zip")) != null) {
                zipFile.getParentFile().mkdirs();
                if (!props.getProperty(PROPERTY_OVERWRITEEXISTINGFILES, "Y").equals("Y") && zipFile.exists()) {
                    throw new SapphireException("Export file '" + zipFile.getAbsolutePath() + "' already exists - try setting the OverrideExistingFiles property = 'true'");
                }
                zipFos = new FileOutputStream(zipFile);
                zipOut = new ZipOutputStream(new BufferedOutputStream(zipFos));
                export.setZipOut(zipOut);
            }
            export.setOverwriteExistingFiles(props.getProperty(PROPERTY_OVERWRITEEXISTINGFILES, "Y").equals("Y"));
            export.setHeaderAttribute("esigpassword", props.getProperty("esigpassword", "N"));
            export.setHeaderAttribute("esigreason", props.getProperty("esigreason", "N"));
            export.setHeaderAttribute("logimport", props.getProperty("logimport", "Y"));
            export.setHeaderAttribute("checksum", props.getProperty("checksum", "N"));
            export.setExportLog(logger);
            export.export();
            props.setProperty(PROPERTY_EXPORTLOG, logger.getLog());
            if (props.getProperty(PROPERTY_CREATEANTFILE).equals("Y")) {
                String antfilename = StringUtil.replaceAll(filename, "/", "\\");
                int pos = antfilename.lastIndexOf("\\");
                antfilename = pos == -1 ? "ant_" + antfilename : antfilename.substring(0, pos + 1) + "ant_" + antfilename.substring(pos + 1);
                File antfile = new File(antfilename);
                HashMap antProps = new HashMap();
                antProps.putAll(AntUtil.getConnectionProps(this.connectionInfo));
                antexport.generateAntScript(antfile, "export", antProps);
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to export " + tableid + ". Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
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
        }
    }
}

