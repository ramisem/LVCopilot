/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.BuildException
 *  org.apache.tools.ant.DirectoryScanner
 *  org.apache.tools.ant.Task
 *  org.apache.tools.ant.types.FileSet
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.xml.ImportXML;
import com.labvantage.sapphire.xml.Logger;
import com.labvantage.sapphire.xml.ant.ConnectionTask;
import com.labvantage.sapphire.xml.ant.SyncDDTTask;
import java.io.File;
import java.util.ArrayList;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import sapphire.SapphireException;

public class ImportXMLTask
extends Task
implements Logger {
    boolean verbose = false;
    String commitScope = "table";
    ConnectionTask connection;
    ArrayList filesets = new ArrayList();
    SyncDDTTask syncDDT;
    boolean forceupdate;
    boolean ignoreMissingObjects;
    boolean regenKeys;

    public void setForceupdate(boolean forceupdate) {
        this.forceupdate = forceupdate;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setIgnoreMissingObjects(boolean ignoreMissingObjects) {
        this.ignoreMissingObjects = ignoreMissingObjects;
    }

    public void setRegenKeys(boolean regenKeys) {
        this.regenKeys = regenKeys;
    }

    public void setCommitscope(String commitScope) {
        this.commitScope = commitScope;
    }

    public void execute() throws BuildException {
        if (this.connection == null) {
            throw new BuildException("Connection task not defined");
        }
        if (this.filesets.size() == 0) {
            throw new BuildException("No files have been defined for import");
        }
        this.log("Importing XML...");
        DBUtil dbu = this.connection.getConnection();
        try {
            ArrayList<File> importFiles = new ArrayList<File>();
            for (int i = 0; i < this.filesets.size(); ++i) {
                FileSet fileset = (FileSet)this.filesets.get(i);
                DirectoryScanner ds = fileset.getDirectoryScanner(this.getProject());
                ds.scan();
                String[] includedFiles = ds.getIncludedFiles();
                for (int j = 0; j < includedFiles.length; ++j) {
                    File file = new File(ds.getBasedir(), includedFiles[j]);
                    importFiles.add(file);
                }
            }
            if (importFiles.size() > 0) {
                ImportXML importXML = new ImportXML(dbu, importFiles);
                importXML.setImportLog(this);
                importXML.setCommitScope(this.commitScope);
                importXML.setIgnoreMissingObjects(this.ignoreMissingObjects);
                importXML.setRegenKeys(this.regenKeys);
                importXML.setImportForceUpdate(this.forceupdate ? "true" : "false");
                importXML.setAntProperties(this.getProject().getProperties());
                if (this.syncDDT != null) {
                    importXML.syncDDT();
                }
                importXML.importFiles(this.verbose);
            } else {
                this.log("No import files found");
            }
        }
        catch (SapphireException se) {
            throw new BuildException("SapphireException: " + se.getMessage());
        }
        finally {
            dbu.reset();
        }
        this.log("Import XML complete");
    }

    public void addConfiguredConnection(ConnectionTask connection) {
        this.connection = connection;
    }

    public void addFileset(FileSet fileset) {
        this.filesets.add(fileset);
    }

    public void addConfiguredSyncDDT(SyncDDTTask syncDDT) {
        this.syncDDT = syncDDT;
    }
}

