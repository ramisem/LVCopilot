/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.Build;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.util.file.ZipFileUtil;
import com.labvantage.sapphire.xml.ExportPackageHandler;
import com.labvantage.sapphire.xml.ImportDirective;
import com.labvantage.sapphire.xml.Logger;
import com.labvantage.sapphire.xml.SaxUtil;
import com.labvantage.sapphire.xml.TransferConstants;
import com.labvantage.sapphire.xml.TransferPackage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ImportXML
implements TransferConstants,
Logger {
    private DBAccess database;
    private ArrayList importFiles = new ArrayList();
    private String dirname;
    private Logger logger = this;
    private String commitScope = "table";
    private boolean ignoreMissingObjects = false;
    private boolean ignoreSequenceCheck = false;
    private boolean regenKeys = false;
    private String importForceUpdate = "false";
    private String checksumProcessing = "I";
    private Hashtable antProperties;
    private ArrayList<ImportDirective> importDirectives = new ArrayList();
    private String connectionid;

    public ImportXML() {
    }

    public ImportXML(DBAccess database, File importFile) {
        this.database = database;
        this.importFiles.add(importFile);
    }

    public ImportXML(DBUtil database, ArrayList importFiles) {
        this.database = database;
        this.importFiles.addAll(importFiles);
    }

    public void setDatabase(DBAccess database) {
        this.database = database;
    }

    public void setDir(File dir) {
        this.dirname = dir.getAbsolutePath();
    }

    public void setDirname(String dirname) {
        this.dirname = dirname;
    }

    public String getDirname() {
        return this.dirname;
    }

    public void setImportFiles(ArrayList importFiles) {
        this.importFiles = importFiles;
    }

    public ArrayList getImportFiles() {
        return this.importFiles;
    }

    public void setImportLog(Logger logger) {
        this.logger = logger;
    }

    public void setCommitScope(String commitScope) {
        this.commitScope = commitScope;
    }

    public void setIgnoreMissingObjects(boolean ignoreMissingObjects) {
        this.ignoreMissingObjects = ignoreMissingObjects;
    }

    public void setIgnoreSequenceCheck(boolean ignoreSequenceCheck) {
        this.ignoreSequenceCheck = ignoreSequenceCheck;
    }

    public void setRegenKeys(boolean regenKeys) {
        this.regenKeys = regenKeys;
    }

    public void setConnectionid(String connectionid) {
        this.connectionid = connectionid;
    }

    public void setImportForceUpdate(String importForceUpdate) {
        this.importForceUpdate = importForceUpdate;
    }

    public boolean isImportForceUpdate() {
        return this.importForceUpdate != null && (this.importForceUpdate.equals("true") || this.importForceUpdate.equals("Y"));
    }

    public void setImportDirective(ImportDirective importDirective) {
        this.importDirectives.add(importDirective);
    }

    public void setChecksumProcessing(String checksumProcessing) {
        this.checksumProcessing = checksumProcessing;
    }

    public void setAntProperties(Hashtable antProperties) {
        this.antProperties = antProperties;
    }

    public void evalProperties(PropertyList props) {
        if (props != null) {
            this.dirname = TransferPackage.replaceProperties(this.dirname, props);
        }
    }

    public void importFiles() throws SapphireException {
        this.importFiles(null, false);
    }

    public void importFiles(boolean verbose) throws SapphireException {
        this.importFiles(null, verbose);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void importFiles(PropertyList props, boolean verbose) throws SapphireException {
        if (this.database == null) {
            throw new SapphireException("Database not defined");
        }
        if (this.importFiles.size() == 0) {
            throw new SapphireException("Import file(s) not defined");
        }
        this.evalProperties(props);
        try {
            File dir = this.dirname != null ? new File(this.dirname) : null;
            for (int i = 0; i < this.importFiles.size(); ++i) {
                File file = (File)this.importFiles.get(i);
                if (dir != null && file.toString().indexOf("*") > -1) {
                    File[] importFiles = dir.listFiles();
                    for (int j = 0; j < importFiles.length; ++j) {
                        if (!FileUtil.wildcardMatch(importFiles[j].getName(), file.toString(), false)) continue;
                        this.importFile(new File(dir, importFiles[j].getName()), verbose);
                    }
                    continue;
                }
                File importFile = dir != null ? new File(dir, ((File)this.importFiles.get(i)).toString()) : (File)this.importFiles.get(i);
                this.importFile(importFile, verbose);
            }
            if (this.commitScope.equals("import")) {
                this.logger.log("Committing import");
                this.database.getConnection().commit();
            }
        }
        catch (SQLException sqle) {
            try {
                try (StringWriter sw = new StringWriter();
                     PrintWriter pw = new PrintWriter(sw);){
                    sqle.printStackTrace(pw);
                    this.logger.log(sw.toString());
                    throw new SapphireException("Failed to commit data. SQLException:" + sqle.getMessage(), sqle);
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            catch (Throwable throwable) {
                try {
                    String callstmt = "{call lv_app" + (this.database.isOracle() ? "." : "_") + "setappinfouser( ? ) }";
                    CallableStatement cs = this.database.prepareCall(callstmt);
                    cs.setString(1, "");
                    cs.executeUpdate();
                }
                catch (Exception ignore) {
                    this.logger.log("exception: " + ignore.getMessage());
                }
                ((DBUtil)this.database).reset();
                throw throwable;
            }
        }
        try {
            String callstmt = "{call lv_app" + (this.database.isOracle() ? "." : "_") + "setappinfouser( ? ) }";
            CallableStatement cs = this.database.prepareCall(callstmt);
            cs.setString(1, "");
            cs.executeUpdate();
        }
        catch (Exception ignore) {
            this.logger.log("exception: " + ignore.getMessage());
        }
        ((DBUtil)this.database).reset();
    }

    private void importFile(File importFile, boolean verbose) throws SapphireException, SQLException {
        ExportPackageHandler handler = new ExportPackageHandler();
        handler.setVerbose(verbose);
        handler.setXMLFile(new File(importFile.getAbsolutePath()));
        handler.setDBUtil((DBUtil)this.database);
        handler.setLogger(this.logger);
        handler.setCommitScope(this.commitScope);
        handler.setIgnoreMissingObjects(this.ignoreMissingObjects);
        handler.setIgnoreSequenceCheck(this.ignoreSequenceCheck);
        handler.setRegenKeys(this.regenKeys);
        handler.setImportForceUpdate(this.isImportForceUpdate());
        handler.setImportDirectives(this.importDirectives);
        handler.setChecksumProcessing(this.checksumProcessing);
        handler.setAntProperties(this.antProperties);
        if ((this.connectionid == null || this.connectionid.length() == 0) && this.antProperties != null && this.antProperties.containsKey("sapphire.connectionid")) {
            this.connectionid = (String)this.antProperties.get("sapphire.connectionid");
        }
        handler.setConnectionid(this.connectionid);
        String[] zipEntry = this.getZipEntry(importFile);
        if (zipEntry != null && zipEntry.length > 0) {
            for (int i = 0; i < zipEntry.length; ++i) {
                handler.setZipFile(handler.getXMLFile());
                handler.setZipFileEntry(zipEntry[i]);
                SaxUtil.parseFile(handler, zipEntry[i]);
                if (!this.commitScope.equals("file")) continue;
                this.logger.log("Committing file: " + zipEntry[i]);
                this.database.getConnection().commit();
            }
        } else {
            SaxUtil.parseFile(handler);
            if (this.commitScope.equals("file")) {
                this.logger.log("Commit File: " + handler.getXMLFile().getName());
                this.database.getConnection().commit();
            }
        }
    }

    public List parseFiles() throws SapphireException {
        return this.parseFiles(false);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Lifted jumps to return sites
     */
    public List parseFiles(boolean loadData) throws SapphireException {
        if (this.database == null) {
            throw new SapphireException("Database not defined");
        }
        if (this.importFiles.size() == 0) {
            throw new SapphireException("Import file(s) not defined");
        }
        ArrayList<List> imports = new ArrayList<List>();
        try {
            int i = 0;
            while (i < this.importFiles.size()) {
                File importFile = (File)this.importFiles.get(i);
                ExportPackageHandler handler = new ExportPackageHandler();
                handler.setXMLFile(new File(importFile.getAbsolutePath()));
                handler.setDBUtil((DBUtil)this.database);
                handler.setLogger(this.logger);
                handler.setImportTarget(loadData ? 1 : 2);
                handler.setCommitScope(this.commitScope);
                handler.setIgnoreMissingObjects(this.ignoreMissingObjects);
                handler.setIgnoreSequenceCheck(this.ignoreSequenceCheck);
                handler.setRegenKeys(this.regenKeys);
                handler.setChecksumProcessing(this.checksumProcessing);
                String[] zipEntry = this.getZipEntry(importFile);
                if (zipEntry != null && zipEntry.length > 0) {
                    for (int j = 0; j < zipEntry.length; ++j) {
                        handler.setZipFile(handler.getXMLFile());
                        handler.setZipFileEntry(zipEntry[j]);
                        SaxUtil.parseFile(handler, zipEntry[j]);
                        imports.add(handler.getImportObjects());
                    }
                } else {
                    SaxUtil.parseFile(handler);
                    imports.add(handler.getImportObjects());
                }
                ++i;
            }
            return imports;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to parse data. Reason:" + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionid)), e);
        }
        finally {
            ((DBUtil)this.database).reset();
        }
    }

    private String[] getZipEntry(File importFile) throws SapphireException {
        String[] zipEntry = null;
        try {
            HashMap manifestAttributes = ZipFileUtil.getManifest(importFile);
            String exportfiles = (String)manifestAttributes.get("exportfiles");
            if (exportfiles == null || exportfiles.length() <= 0) {
                throw new SapphireException("No export files found in manifest file");
            }
            zipEntry = StringUtil.split(exportfiles, ";");
        }
        catch (SapphireException sapphireException) {
            // empty catch block
        }
        return zipEntry;
    }

    public void syncDDT() throws SapphireException {
        if (this.database == null) {
            throw new SapphireException("Database not defined");
        }
        this.database.executePreparedUpdate("{call lv_ddt" + (this.database.isOracle() ? "." : "_") + "syncddt( ? ) }", new Object[]{Build.getBuild()});
    }

    public void addImport(File importFile) {
        this.importFiles.add(importFile);
    }

    @Override
    public void log(String message) {
    }

    public void generateTransferPackageXML(PrintStream out) {
        out.println("\t\t<importXML " + (this.dirname != null ? "dir=\"" + this.dirname + "\"" : "") + " forceupdate=\"" + this.importForceUpdate + "\">");
        for (int i = 0; i < this.importFiles.size(); ++i) {
            File importFile = (File)this.importFiles.get(i);
            out.println("\t\t\t<file file=\"" + (importFile != null ? importFile.toString() : "") + "\"/>");
        }
        out.println("\t\t</importXML>");
    }

    public boolean isValid() {
        return this.importFiles.size() > 0;
    }
}

