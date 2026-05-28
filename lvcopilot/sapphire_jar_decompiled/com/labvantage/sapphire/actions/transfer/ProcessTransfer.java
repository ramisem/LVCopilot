/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.transfer;

import com.labvantage.sapphire.actions.transfer.TransferAction;
import com.labvantage.sapphire.util.ant.AntUtil;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.xml.ExportXML;
import com.labvantage.sapphire.xml.StringLogger;
import com.labvantage.sapphire.xml.TransferConstants;
import com.labvantage.sapphire.xml.TransferPackage;
import java.io.File;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ProcessTransfer
extends BaseAction
implements TransferAction,
TransferConstants {
    public static final String ID = "ProcessTransfer";
    public static final String VERSION = "1";
    static final String LABVANTAGE_CVS_ID = "$Revision: 77792 $";
    public static final String PROPERTY_TRANSFERPACKAGEID = "transferpackageid";
    public static final String PROPERTY_TRANSFERPACKAGEVERSIONID = "transferpackageversionid";
    public static final String PROPERTY_ZIPFILENAME = "zipfilename";
    public static final String PROPERTY_TEMPFILEOUTPUT = "tempfileoutput";
    public static final String PROPERTY_TEMPID = "tempid";

    @Override
    public void processAction(PropertyList props) throws SapphireException {
        String transferpackageid = props.getProperty(PROPERTY_TRANSFERPACKAGEID);
        String transferpackageversionid = props.getProperty(PROPERTY_TRANSFERPACKAGEVERSIONID);
        String transferpackagexml = props.getProperty("transferpackagexml");
        String zipfilename = props.getProperty(PROPERTY_ZIPFILENAME);
        boolean generateTempFile = props.getProperty(PROPERTY_TEMPFILEOUTPUT).equalsIgnoreCase("Y");
        TransferPackage tp = new TransferPackage();
        tp.setHeaderAttribute("esigpassword", props.getProperty("esigpassword", "N"));
        tp.setHeaderAttribute("esigreason", props.getProperty("esigreason", "N"));
        tp.setHeaderAttribute("logimport", props.getProperty("logimport", "Y"));
        tp.setHeaderAttribute("checksum", props.getProperty("checksum", "N"));
        StringLogger logger = new StringLogger();
        if (transferpackageid.length() > 0 && transferpackageversionid.length() > 0) {
            tp.loadTransferPackage(this.database, transferpackageid, transferpackageversionid);
        } else if (transferpackagexml.length() > 0) {
            tp.loadXML(transferpackagexml);
        } else {
            throw new SapphireException("Transfer package not specified!");
        }
        PropertyList tpProps = tp.getPropertyList();
        if (tpProps != null) {
            for (String tpProperty : tpProps.keySet()) {
                String actionpropertyvalue = props.getProperty(tpProperty);
                if (actionpropertyvalue.length() <= 0) continue;
                tpProps.setProperty(tpProperty, actionpropertyvalue);
            }
            if (generateTempFile) {
                // empty if block
            }
        }
        FileManager.TempFile tempZipFile = null;
        File zip = null;
        if (generateTempFile) {
            props.setProperty("zipoutput", "Y");
            String fn = FileManager.getFileName(zipfilename, true);
            tempZipFile = new FileManager.TempFile(fn, FileManager.TempSource.DOWNLOAD, this.getConnectionId());
            zip = tempZipFile.getData().getFile().toFile();
            if (tpProps != null && zip != null) {
                tpProps.setProperty("export.dir", zip.getParent().endsWith(File.separator) ? zip.getParent() : zip.getParent() + File.separator);
            }
        } else if (props.getProperty("zipoutput", "N").equals("Y") && zipfilename.length() > 0) {
            zip = new File(zipfilename);
        }
        tp.run(this.database, "", logger, zip);
        StringBuilder tempid = new StringBuilder();
        if (generateTempFile && tempZipFile != null) {
            String id = tempZipFile.setTempFile("Sapphire Custom", this.getConnectionId(), this.getActionProcessor());
            tempid.append(id);
        }
        props.setProperty("exportlog", logger.getLog());
        if (props.getProperty("createantfile").equals("Y")) {
            File antfile = null;
            FileManager.TempFile tempAntFile = null;
            if (generateTempFile) {
                String antfilename = "ant_" + FileManager.getFileName(zipfilename, false) + ".xml";
                tempAntFile = new FileManager.TempFile(antfilename, FileManager.TempSource.DOWNLOAD, this.getConnectionId());
                antfile = tempAntFile.getData().getFile().toFile();
            } else {
                String transferexportdir = ((ExportXML)tp.getTransfer(0).getTransfer(0)).getDirname();
                String antfilename = props.getProperty("zipoutput", "N").equals("Y") && zipfilename.length() > 0 ? zipfilename.substring(0, zipfilename.length() - 4) + ".xml" : (!transferexportdir.endsWith("/") && !transferexportdir.endsWith("\\") ? "/" : "") + transferpackageid + ".xml";
                antfilename = StringUtil.replaceAll(antfilename, "/", "\\");
                int pos = antfilename.lastIndexOf("\\");
                antfilename = pos == -1 ? "ant_" + antfilename : antfilename.substring(0, pos + 1) + "ant_" + antfilename.substring(pos + 1);
                antfile = new File(antfilename);
            }
            HashMap antProps = new HashMap();
            antProps.putAll(AntUtil.getConnectionProps(this.connectionInfo));
            tp.generateAntScript(antfile, "export", antProps, this.database);
            if (generateTempFile && tempAntFile != null) {
                String id = tempAntFile.setTempFile("Sapphire Custom", this.getConnectionId(), this.getActionProcessor());
                if (tempid.length() > 0) {
                    tempid.append(";");
                }
                tempid.append(id);
            }
        }
        props.setProperty(PROPERTY_TEMPID, tempid.toString());
    }

    @Override
    public PropertyList convertWizardProperties(PropertyList wizardProps, String connectionid) throws Exception {
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty(PROPERTY_TRANSFERPACKAGEID, wizardProps.getProperty(PROPERTY_TRANSFERPACKAGEID));
        actionProps.setProperty(PROPERTY_TRANSFERPACKAGEVERSIONID, wizardProps.getProperty(PROPERTY_TRANSFERPACKAGEVERSIONID));
        actionProps.setProperty("esigpassword", wizardProps.getProperty("esigpassword"));
        actionProps.setProperty("esigreason", wizardProps.getProperty("esigreason"));
        actionProps.setProperty("logimport", wizardProps.getProperty("logimport"));
        actionProps.setProperty("checksum", wizardProps.getProperty("checksum"));
        actionProps.setProperty("zipoutput", wizardProps.getProperty("zipoutput"));
        actionProps.setProperty(PROPERTY_ZIPFILENAME, wizardProps.getProperty(PROPERTY_ZIPFILENAME));
        actionProps.setProperty("createantfile", wizardProps.getProperty("createantfile"));
        String[] propertylist = StringUtil.split(wizardProps.getProperty("propertyidlist"), ";");
        for (int i = 0; i < propertylist.length; ++i) {
            actionProps.setProperty(propertylist[i], wizardProps.getProperty(propertylist[i]));
        }
        return actionProps;
    }
}

