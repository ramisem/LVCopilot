/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.util.ant.AntUtil;
import com.labvantage.sapphire.xml.ExportXML;
import com.labvantage.sapphire.xml.ImportXML;
import com.labvantage.sapphire.xml.Logger;
import com.labvantage.sapphire.xml.SaxUtil;
import com.labvantage.sapphire.xml.Transfer;
import com.labvantage.sapphire.xml.TransferPackageHandler;
import com.labvantage.sapphire.xml.ant.ConnectionTask;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipOutputStream;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class TransferPackage
implements Serializable {
    static final String LABVANTAGE_CVS_ID = "$Revision: 88217 $";
    public static final String NAME = "transferpackage";
    private String transferpackageid;
    private String transferpackageversionid;
    private ArrayList transferList = new ArrayList();
    private PropertyList propertyList = new PropertyList();
    private boolean exportScript = false;
    private boolean antScript = false;
    private PropertyList headerAttributes = new PropertyList();

    public static String replaceProperties(String source, PropertyList props) {
        if (props != null) {
            String[] tokens = StringUtil.getTokens(source);
            for (int i = 0; i < tokens.length; ++i) {
                source = StringUtil.replaceAll(source, "[" + tokens[i] + "]", props.getProperty(tokens[i]));
            }
        }
        return source;
    }

    public TransferPackage() {
    }

    public TransferPackage(boolean exportScript) {
        this.exportScript = exportScript;
    }

    public void addTransfer(Transfer transfer) {
        this.transferList.add(transfer);
    }

    public void addTransfer(int pos, Transfer transfer) {
        this.transferList.add(pos, transfer);
    }

    public void addProperty(String id, String value) {
        this.propertyList.setProperty(id, value);
    }

    public PropertyList getPropertyList() {
        return this.propertyList;
    }

    public void removeProperty(String propertyid) {
        this.propertyList.remove(propertyid);
    }

    public void removeTransfer(int index) {
        if (index > -1 && index < this.transferList.size()) {
            this.transferList.remove(index);
        }
    }

    public Transfer getTransfer(int index) {
        return index > -1 && index < this.transferList.size() ? (Transfer)this.transferList.get(index) : null;
    }

    public int size() {
        return this.transferList.size();
    }

    public void setExportScript(boolean exportScript) {
        this.exportScript = exportScript;
    }

    public boolean isExportScript() {
        return this.exportScript;
    }

    public boolean isAntScript() {
        return this.antScript;
    }

    public void setAntScript(boolean antScript) {
        this.antScript = antScript;
    }

    public void setHeaderAttribute(String propertyid, String propertyValue) {
        this.headerAttributes.setProperty(propertyid, propertyValue);
    }

    public void loadTransferPackage(DBAccess database, String transferpackageid, String transferpackageversionid) throws SapphireException {
        database.createPreparedResultSet("SELECT transferpackage FROM transferpackage WHERE transferpackageid = ? AND transferpackageversionid = ?", new Object[]{transferpackageid, transferpackageversionid});
        if (!database.getNext()) {
            throw new SapphireException("Failed to retrieve transferpackage '" + transferpackageid + "', version '" + transferpackageversionid + "'");
        }
        this.transferpackageid = transferpackageid;
        this.transferpackageversionid = transferpackageversionid;
        this.load(database.getClob(NAME));
    }

    public void loadExportScript(DBAccess database, String sdcid, String exportid) throws SapphireException {
        database.createPreparedResultSet("SELECT exportscript FROM sdcexport WHERE sdcid = ? AND exportid = ?", new Object[]{sdcid, exportid});
        if (!database.getNext()) {
            throw new SapphireException("Export script '" + exportid + "' not found for sdc '" + sdcid + "'.");
        }
        this.load(database.getClob("exportscript"));
    }

    public void loadXML(String xml) throws SapphireException {
        this.load(xml);
    }

    public void loadFile(File filename) throws SapphireException {
        this.load(filename);
    }

    private void load(Object source) throws SapphireException {
        try {
            TransferPackageHandler handler = new TransferPackageHandler();
            if (source instanceof File) {
                handler.setXMLFile((File)source);
                SaxUtil.parseFile(handler);
            } else {
                handler.setXMLString((String)source);
                if (handler.getXMLString().length() == 0) {
                    throw new SapphireException("Transfer package not properly defined!");
                }
                SaxUtil.parseString(handler);
            }
            this.transferList = handler.getTransferPackage().transferList;
            this.propertyList = handler.getTransferPackage().propertyList;
            this.exportScript = handler.getTransferPackage().exportScript;
            this.antScript = handler.getTransferPackage().antScript;
            if (this.exportScript && this.size() != 1 && this.getTransfer(0).getTransfers().size() != 1 && !(this.getTransfer(0).getTransfers().get(0) instanceof ExportXML)) {
                throw new SapphireException("Transfer package is an invalid export script. Valid export scripts contain a single export transfer");
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to load transfer package file/script. Reason: " + e.getMessage(), e);
        }
    }

    public void save(File filename) throws SapphireException {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            this.toXML(fos);
            fos.close();
        }
        catch (Exception e) {
            throw new SapphireException("Failed to save transfer package!", e);
        }
    }

    public void run(DBAccess database, String target, Logger logger) throws SapphireException {
        this.run(database, target, logger, null);
    }

    public void run(DBAccess database, String target, Logger logger, File zipFile) throws SapphireException {
        FileOutputStream zipFos = null;
        ZipOutputStream zipOut = null;
        if (target == null || target.length() == 0) {
            target = "(ALL)";
        }
        try {
            if (zipFile != null) {
                zipFile.getParentFile().mkdirs();
                zipFos = new FileOutputStream(zipFile);
                zipOut = new ZipOutputStream(new BufferedOutputStream(zipFos));
            }
            for (int i = 0; i < this.size(); ++i) {
                Transfer transfer = this.getTransfer(i);
                if (!target.equals("(ALL)") && !transfer.getId().equals(target)) continue;
                ArrayList transfers = transfer.getTransfers();
                for (int j = 0; j < transfers.size(); ++j) {
                    Object o = transfers.get(j);
                    if (o instanceof ExportXML) {
                        ExportXML export = (ExportXML)o;
                        export.setDatabase(database);
                        export.setExportLog(logger);
                        export.setZipOut(zipOut);
                        if (this.isExportScript()) {
                            export.setDir(new File(this.getPropertyList().getProperty("export.dir")));
                            export.setFile(new File(this.getPropertyList().getProperty("export.dir"), this.getPropertyList().getProperty("export.file")));
                        } else if ((export.getDirname() == null || export.getDirname().length() == 0) && zipFile != null) {
                            export.setDir(zipFile.getParentFile());
                        }
                        for (String attr : this.headerAttributes.keySet()) {
                            export.setHeaderAttribute(attr, this.headerAttributes.getProperty(attr));
                        }
                        export.export(this.getPropertyList());
                        continue;
                    }
                    if (!(o instanceof ImportXML)) continue;
                    ImportXML importxml = (ImportXML)o;
                    importxml.setDatabase(database);
                    importxml.setImportLog(logger);
                    importxml.importFiles();
                }
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to run transferpackage. Reason: " + e.getMessage(), e);
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

    public ExportXML getExportScriptExportXML() throws SapphireException {
        if (this.size() != 1 || this.getTransfer(0).getTransfers().size() != 1 || !(this.getTransfer(0).getTransfers().get(0) instanceof ExportXML)) {
            throw new SapphireException("Export method will only export a valid export script containing a single export transfer");
        }
        return (ExportXML)this.getTransfer(0).getTransfer(0);
    }

    public String toXML() throws SapphireException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.toXML(baos);
        return new String(baos.toByteArray());
    }

    private void toXML(OutputStream os) throws SapphireException {
        try {
            PrintStream out = new PrintStream(os, true, "UTF-8");
            out.println("<?xml version=\"1.0\"?>");
            out.println("<transferpackage" + (this.isExportScript() ? " exportscript=\"true\"" : "") + ">");
            out.println(this.propertyList.toXMLString(1));
            for (int i = 0; i < this.size(); ++i) {
                Transfer transfer = this.getTransfer(i);
                out.println("\t<transfer id=\"" + transfer.getId() + "\" type=\"" + transfer.getType() + "\">");
                ArrayList transfers = transfer.getTransfers();
                for (int j = 0; j < transfers.size(); ++j) {
                    Object o = transfers.get(j);
                    if (o instanceof ExportXML) {
                        ExportXML export = (ExportXML)o;
                        export.generateTransferPackageXML(out);
                        continue;
                    }
                    if (!(o instanceof ImportXML)) continue;
                    ImportXML importxml = (ImportXML)o;
                    importxml.generateTransferPackageXML(out);
                }
                out.println("\t</transfer>");
            }
            out.println("</transferpackage>");
        }
        catch (Exception e) {
            throw new SapphireException("Failed to create output stream for transfer package!", e);
        }
    }

    public void generateAntScript(File antFile, String target, HashMap antProps, DBAccess database) throws SapphireException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(antFile);
            PrintStream out = new PrintStream((OutputStream)fos, true, "UTF-8");
            out.print(AntUtil.getAntFileHeader(target, target, antProps));
            out.println("\t<target name=\"" + target + "\">");
            out.println("\t\t<transferpackage " + (this.transferpackageid != null && this.transferpackageid.length() > 0 ? "transferpackageid=\"" + this.transferpackageid + "\"" : "") + (this.transferpackageversionid != null && this.transferpackageversionid.length() > 0 ? " transferpackageversionid=\"" + this.transferpackageversionid + "\"" : "") + ">");
            ConnectionTask.generateAntTask(out, 3, database.isOracle() ? "ORA" : "MSS");
            out.println("\t\t</transferpackage>");
            out.println("\t</target>");
            out.print(AntUtil.getAntFileFooter());
            out.flush();
            out.close();
        }
        catch (IOException ioe) {
            throw new SapphireException("IOException: " + ioe.getMessage());
        }
        finally {
            try {
                fos.flush();
                fos.close();
            }
            catch (IOException e) {
                throw new SapphireException("IOException: " + e.getMessage());
            }
        }
    }
}

