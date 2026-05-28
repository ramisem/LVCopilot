/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.BuildException
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.xml.ExportXML;
import com.labvantage.sapphire.xml.Logger;
import com.labvantage.sapphire.xml.Transferable;
import com.labvantage.sapphire.xml.ant.ConnectionTask;
import com.labvantage.sapphire.xml.ant.PropertyTreeTask;
import com.labvantage.sapphire.xml.ant.SDCTask;
import com.labvantage.sapphire.xml.ant.SDITask;
import com.labvantage.sapphire.xml.ant.SyncDDTTask;
import com.labvantage.sapphire.xml.ant.TableTask;
import com.labvantage.sapphire.xml.ant.WebPageTask;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipOutputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import sapphire.SapphireException;
import sapphire.util.DBAccess;

public class ExportXMLTask
extends Task
implements Logger {
    ConnectionTask connection;
    ArrayList exports = new ArrayList();
    SyncDDTTask syncDDT;
    File zipFile;
    File dir;
    File file;
    boolean logimport = false;
    boolean esigpassword = false;
    boolean esigreason = false;
    boolean checksum = false;
    boolean multifile = false;
    boolean excludeexportattributes = false;
    boolean verbose = false;
    private boolean overwriteExistingFiles = true;

    public void setDir(File dir) {
        this.dir = dir;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setZipFile(File zipFile) {
        this.zipFile = zipFile;
    }

    public void setLogimport(boolean logimport) {
        this.logimport = logimport;
    }

    public void setEsigpassword(boolean esigpassword) {
        this.esigpassword = esigpassword;
    }

    public void setEsigreason(boolean esigreason) {
        this.esigreason = esigreason;
    }

    public void setChecksum(boolean checksum) {
        this.checksum = checksum;
    }

    public void setExcludeexportattributes(boolean excludeexportattributes) {
        this.excludeexportattributes = excludeexportattributes;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setMultifile(boolean multifile) {
        this.multifile = multifile;
    }

    public void setOverwriteExistingFiles(boolean overwriteExistingFiles) {
        this.overwriteExistingFiles = overwriteExistingFiles;
    }

    public void execute() throws BuildException {
        if (this.connection == null) {
            throw new BuildException("Connection task not defined");
        }
        if (this.exports.size() == 0) {
            throw new BuildException("SDC or Table task(s) not defined");
        }
        this.log("Exporting XML...");
        DBUtil dbu = this.connection.getConnection();
        FileOutputStream zipFos = null;
        ZipOutputStream zipOut = null;
        try {
            String comparebeforeoverwrite = this.getProject().getProperty("devmode.exportxml.comparebeforeoverwrite");
            boolean compareBeforeOverwrite = comparebeforeoverwrite != null && comparebeforeoverwrite.equals("true");
            String compareBeforeOverwriteTempDir = this.getProject().getProperty("devmode.exportxml.comparebeforeoverwrite.tempdir");
            if (this.dir != null) {
                this.dir.mkdirs();
            }
            File tempExportDir = null;
            if (compareBeforeOverwrite) {
                tempExportDir = new File(compareBeforeOverwriteTempDir != null && compareBeforeOverwriteTempDir.length() > 0 ? compareBeforeOverwriteTempDir : System.getProperty("java.io.tmpdir") + "/sapphireexport");
                if (tempExportDir.exists()) {
                    FileUtil.deleteAll(tempExportDir);
                }
                tempExportDir.mkdirs();
            }
            File dirOrFile = this.dir != null && this.dir.isDirectory() ? (compareBeforeOverwrite ? tempExportDir : this.dir) : this.file;
            ArrayList<Transferable> exportItems = new ArrayList<Transferable>();
            for (int i = 0; i < this.exports.size(); ++i) {
                if (this.exports.get(i) instanceof TableTask) {
                    exportItems.add(((TableTask)((Object)this.exports.get(i))).getTableTransfer());
                    continue;
                }
                if (this.exports.get(i) instanceof SDCTask) {
                    exportItems.add(((SDCTask)((Object)this.exports.get(i))).getSDCTransfer());
                    continue;
                }
                if (this.exports.get(i) instanceof WebPageTask) {
                    exportItems.add(((WebPageTask)this.exports.get(i)).getWebPageTransfer());
                    continue;
                }
                if (this.exports.get(i) instanceof SDITask) {
                    exportItems.add(((SDITask)((Object)this.exports.get(i))).getSDITransfer());
                    continue;
                }
                if (!(this.exports.get(i) instanceof PropertyTreeTask)) continue;
                exportItems.add(((PropertyTreeTask)((Object)this.exports.get(i))).getPropertyTreeTransfer());
            }
            ExportXML exportXML = new ExportXML((DBAccess)dbu, exportItems, dirOrFile);
            exportXML.setHeaderAttribute("esigpassword", this.esigpassword ? "Y" : "N");
            exportXML.setHeaderAttribute("esigreason", this.esigreason ? "Y" : "N");
            exportXML.setHeaderAttribute("logimport", this.logimport ? "Y" : "N");
            exportXML.setHeaderAttribute("checksum", this.checksum ? "Y" : "N");
            exportXML.setExportLog(this);
            exportXML.setMultifile(this.multifile);
            exportXML.setVerbose(this.verbose);
            exportXML.setOverwriteExistingFiles(this.overwriteExistingFiles);
            exportXML.setExcludeExportAttributes(this.excludeexportattributes);
            if (this.syncDDT != null) {
                exportXML.syncDDT();
            }
            if (this.zipFile != null) {
                this.zipFile.getParentFile().mkdirs();
                if (!this.overwriteExistingFiles && this.zipFile.exists()) {
                    throw new SapphireException("Export file '" + this.zipFile.getAbsolutePath() + "' already exists - try setting the OverrideExistingFiles property = 'true'");
                }
                zipFos = new FileOutputStream(this.zipFile);
                zipOut = new ZipOutputStream(new BufferedOutputStream(zipFos));
                exportXML.setZipOut(zipOut);
            }
            exportXML.export();
            if (compareBeforeOverwrite) {
                this.tempDirCompare(tempExportDir, tempExportDir.getAbsolutePath().length());
                FileUtil.deleteAll(tempExportDir);
                tempExportDir.deleteOnExit();
            }
        }
        catch (Exception se) {
            throw new BuildException("SapphireException: " + se.getMessage(), (Throwable)se);
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
            dbu.reset();
        }
        this.log("Export complete");
    }

    private void tempDirCompare(File tempDir, int dirOffset) throws SapphireException, IOException {
        File[] files = tempDir.listFiles();
        for (int i = 0; i < files.length; ++i) {
            if (files[i].isDirectory()) {
                this.tempDirCompare(files[i], dirOffset);
                continue;
            }
            File sourceFile = new File(this.dir, files[i].getAbsolutePath().substring(dirOffset));
            if (sourceFile.exists() && this.fileMatch(files[i], sourceFile)) continue;
            sourceFile.getParentFile().mkdirs();
            FileUtil.copyFile(files[i], sourceFile);
        }
    }

    private boolean fileMatch(File a, File b) throws SapphireException {
        boolean match = true;
        FileReader frA = null;
        FileReader frB = null;
        BufferedReader brA = null;
        BufferedReader brB = null;
        try {
            frA = new FileReader(a);
            frB = new FileReader(b);
            brA = new BufferedReader(frA);
            brB = new BufferedReader(frB);
            String lineA = null;
            String lineB = null;
            do {
                lineA = brA.readLine();
                lineB = brB.readLine();
                if (lineA != null && lineB != null) {
                    if (lineA.equals(lineB)) continue;
                    match = false;
                    continue;
                }
                if ((lineA == null || lineB != null) && (lineA != null || lineB == null)) continue;
                match = false;
            } while (match && lineA != null && lineB != null);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to compare files. Reason: " + e.getMessage(), e);
        }
        finally {
            try {
                if (brA != null) {
                    brA.close();
                }
                if (brB != null) {
                    brB.close();
                }
                if (frA != null) {
                    frA.close();
                }
                if (frA != null) {
                    frB.close();
                }
            }
            catch (IOException e) {
                throw new SapphireException("Failed to close readers. Reason: " + e.getMessage(), e);
            }
        }
        return match;
    }

    public void addConfiguredConnection(ConnectionTask connection) {
        this.connection = connection;
    }

    public void addConfiguredTable(TableTask table) {
        this.exports.add(table);
    }

    public void addConfiguredSDC(SDCTask sdc) {
        this.exports.add(sdc);
    }

    public void addConfiguredSDI(SDITask sdi) {
        this.exports.add(sdi);
    }

    public void addConfiguredPropertyTree(PropertyTreeTask propertyTree) {
        this.exports.add(propertyTree);
    }

    public void addConfiguredWebPage(WebPageTask webPage) {
        this.exports.add(webPage);
    }

    public void addConfiguredSyncDDT(SyncDDTTask syncDDT) {
        this.syncDDT = syncDDT;
    }
}

