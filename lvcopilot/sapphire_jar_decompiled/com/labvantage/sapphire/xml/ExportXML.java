/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.Build;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.util.IntHolder;
import com.labvantage.sapphire.util.ant.AntUtil;
import com.labvantage.sapphire.util.file.ZipFileUtil;
import com.labvantage.sapphire.xml.Logger;
import com.labvantage.sapphire.xml.PropertyTreeTransfer;
import com.labvantage.sapphire.xml.SDCTransfer;
import com.labvantage.sapphire.xml.SDITransfer;
import com.labvantage.sapphire.xml.TableTransfer;
import com.labvantage.sapphire.xml.TransferConstants;
import com.labvantage.sapphire.xml.TransferPackage;
import com.labvantage.sapphire.xml.Transferable;
import com.labvantage.sapphire.xml.ant.ConnectionTask;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.xml.PropertyList;

public class ExportXML
implements Logger,
TransferConstants {
    private DBAccess database;
    private ArrayList exports = new ArrayList();
    private String dirname;
    private String filename;
    private Logger logger = this;
    private String forceUpdate = "false";
    private String forceNullUpdate = "false";
    private PropertyList props;
    private PropertyList headerAttributes = new PropertyList();
    private boolean multifile = false;
    private boolean excludeExportAttributes = false;
    private ZipOutputStream zipOut;
    private boolean resetDatabase = true;
    private boolean overwriteExistingFiles = true;
    private boolean verbose = false;

    public ExportXML() {
    }

    public ExportXML(DBAccess database, Transferable export, File dirOrFile) {
        this.database = database;
        this.exports.add(export);
        if (dirOrFile != null && dirOrFile.isDirectory()) {
            this.setDir(dirOrFile);
        } else {
            this.setFile(dirOrFile);
        }
    }

    public ExportXML(DBAccess database, ArrayList exports, File dirOrFile) {
        this.database = database;
        this.exports.addAll(exports);
        if (dirOrFile != null && dirOrFile.isDirectory()) {
            this.setDir(dirOrFile);
        } else {
            this.setFile(dirOrFile);
        }
    }

    public void setDatabase(DBAccess database) {
        this.database = database;
    }

    public void setExports(ArrayList exports) {
        this.exports = exports;
    }

    public Transferable getExport(int i) {
        return i >= 0 && i < this.exports.size() ? (Transferable)this.exports.get(i) : null;
    }

    public Transferable getLatestExport() {
        return this.exports.size() > 0 ? (Transferable)this.exports.get(this.exports.size() - 1) : null;
    }

    public ArrayList getExports() {
        return this.exports;
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

    public void setFile(File file) {
        this.filename = file.getAbsolutePath();
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setExportLog(Logger logger) {
        this.logger = logger;
    }

    public void setForceUpdate(String forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    public String getForceUpdate() {
        return this.forceUpdate;
    }

    public void setForceNullUpdate(String forceNullUpdate) {
        this.forceNullUpdate = forceNullUpdate;
    }

    public String getForceNullUpdate() {
        return this.forceNullUpdate;
    }

    public void setMultifile(boolean multifile) {
        this.multifile = multifile;
    }

    public void setExcludeExportAttributes(boolean excludeExportAttributes) {
        this.excludeExportAttributes = excludeExportAttributes;
    }

    public void setZipOut(ZipOutputStream zipOut) {
        this.zipOut = zipOut;
    }

    public void setResetDatabase(boolean resetDatabase) {
        this.resetDatabase = resetDatabase;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setOverwriteExistingFiles(boolean overwriteExistingFiles) {
        this.overwriteExistingFiles = overwriteExistingFiles;
    }

    public void setHeaderAttribute(String propertyid, String propertyValue) {
        this.headerAttributes.setProperty(propertyid, propertyValue);
    }

    public void evalProperties(PropertyList props) {
        this.dirname = TransferPackage.replaceProperties(this.dirname, props);
        this.filename = TransferPackage.replaceProperties(this.filename, props);
        this.forceUpdate = TransferPackage.replaceProperties(this.forceUpdate, props);
        this.forceNullUpdate = TransferPackage.replaceProperties(this.forceNullUpdate, props);
    }

    public void export(PropertyList props) throws SapphireException {
        this.props = props;
        this.evalProperties(props);
        this.export();
    }

    public void export() throws SapphireException {
        if (this.database == null) {
            throw new SapphireException("DBUtil not defined");
        }
        if (this.exports.size() == 0) {
            throw new SapphireException("SDC, SDI or Table export(s) not defined");
        }
        long start = System.currentTimeMillis();
        File exportFile = null;
        FileOutputStream fos = null;
        PrintStream out = null;
        File dir = this.dirname != null ? new File(this.dirname) : null;
        File file = this.filename != null ? new File(this.filename) : null;
        try {
            Transferable exportItem;
            int i;
            LinkedHashMap exported = new LinkedHashMap();
            boolean filePerExport = true;
            Calendar cal = DateTimeUtil.getNowCalendar();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy hh:mmaa", Locale.US);
            String exportDate = sdf.format(cal.getTime());
            StringBuffer exportFiles = new StringBuffer();
            if (dir != null) {
                dir.mkdirs();
            }
            if (file != null) {
                filePerExport = false;
                File file2 = exportFile = dir != null ? new File(dir, file.getName()) : file;
                if (ExportXML.hasUnicodeChars(exportFile.getAbsolutePath())) {
                    throw new SapphireException("Export file '" + exportFile.getAbsolutePath() + "' contains Unicode characters - this is not supported in the current version of Sapphire!");
                }
                exportFile.getParentFile().mkdirs();
                if (!this.overwriteExistingFiles && exportFile.exists()) {
                    throw new SapphireException("Export file '" + exportFile.getAbsolutePath() + "' already exists - try setting the OverrideExistingFiles property = 'true'");
                }
                fos = new FileOutputStream(exportFile);
                out = new PrintStream((OutputStream)fos, true, "UTF-8");
                this.exportHeader(out, exportDate);
                this.logger.log("Exporting EXPORT PACKAGE " + exportFile.getName());
                this.logger.log("");
            }
            StringBuffer sdcsynclist = new StringBuffer();
            for (i = 0; i < this.exports.size(); ++i) {
                SDITransfer sdi;
                exportItem = (Transferable)this.exports.get(i);
                if (!(exportItem instanceof SDITransfer) || !(sdi = (SDITransfer)exportItem).isSyncDataModel()) continue;
                sdi.setSyncDataModel("false");
                sdcsynclist.append(";").append(sdi.getSdcid());
            }
            if (sdcsynclist.length() > 0) {
                SDCTransfer sdc = new SDCTransfer(sdcsynclist.substring(1));
                sdc.setFilename("SyncDDT.xml");
                this.exports.add(0, sdc);
            }
            for (i = 0; i < this.exports.size(); ++i) {
                exportItem = (Transferable)this.exports.get(i);
                exportItem.evalProperties(this.props);
                if (filePerExport) {
                    if (!this.multifile) {
                        File temp = exportItem.getFile() != null ? exportItem.getFile() : new File(exportItem.getId() + ".xml");
                        File file3 = exportFile = dir != null ? new File(dir, temp.getName()) : temp;
                        if (exportFile.getParentFile() != null) {
                            exportFile.getParentFile().mkdirs();
                        }
                        if (!this.overwriteExistingFiles && exportFile.exists()) {
                            throw new SapphireException("Export file '" + exportFile.getAbsolutePath() + "' already exists - try setting the OverrideExistingFiles property = 'true'");
                        }
                        fos = new FileOutputStream(dir != null ? new File(dir, exportFile.getName()) : exportFile);
                        out = new PrintStream((OutputStream)fos, true, "UTF-8");
                        this.exportHeader(out, exportDate);
                    } else if (dir != null) {
                        exportFile = dir;
                        exportFile.getParentFile().mkdirs();
                    } else {
                        throw new SapphireException("Dir attribute not specified for export");
                    }
                }
                ExportXML.exportItem(exportItem, exportFile, out, this.zipOut, this.database, this.logger, exported, this.multifile, this.excludeExportAttributes, this.verbose);
                if (!exportFiles.toString().endsWith(exportFile.getName())) {
                    exportFiles.append(";").append(exportFile.getName());
                }
                if (!filePerExport || this.multifile) continue;
                this.exportFooter(fos, out, exportFile);
            }
            if (!filePerExport) {
                this.exportFooter(fos, out, exportFile);
            }
            if (this.zipOut != null) {
                HashMap<String, String> manifestAttributes = new HashMap<String, String>();
                manifestAttributes.put("exportdate", exportDate);
                manifestAttributes.put("exportbuild", Build.getBuild());
                manifestAttributes.put("fileperexport", filePerExport ? "true" : "false");
                manifestAttributes.put("exportfiles", exportFiles.substring(1));
                manifestAttributes.put("esigpassword", this.headerAttributes.getProperty("esigpassword"));
                manifestAttributes.put("esigreason", this.headerAttributes.getProperty("esigreason"));
                manifestAttributes.put("logimport", this.headerAttributes.getProperty("logimport"));
                manifestAttributes.put("checksum", this.headerAttributes.getProperty("checksum"));
                manifestAttributes.put("manifestchecksum", String.valueOf((exportDate + "|" + Build.getBuild() + "|" + exportFiles.substring(1) + "|" + this.headerAttributes.getProperty("esigpassword") + "|" + this.headerAttributes.getProperty("esigreason") + "|" + this.headerAttributes.getProperty("logimport") + "|" + this.headerAttributes.getProperty("checksum")).hashCode()));
                ZipFileUtil.addManifest(manifestAttributes, this.zipOut);
            }
        }
        catch (CloneNotSupportedException clonee) {
            throw new SapphireException("CloneException: " + clonee.getMessage());
        }
        catch (SQLException sqle) {
            throw new SapphireException("SQLException: " + sqle.getMessage());
        }
        catch (SapphireException se) {
            throw new SapphireException("SapphireException: " + se.getMessage());
        }
        catch (IOException ioe) {
            throw new SapphireException("IOException: " + ioe.getMessage());
        }
        finally {
            if (this.resetDatabase) {
                ((DBUtil)this.database).reset();
            }
            try {
                if (out != null) {
                    out.close();
                }
                if (fos != null) {
                    fos.close();
                }
            }
            catch (IOException e) {
                throw new SapphireException("IOException: " + e.getMessage());
            }
        }
        this.logger.log("");
        this.logger.log("ExportPackage export took " + (System.currentTimeMillis() - start) + "ms");
    }

    private void exportHeader(PrintStream out, String exportDate) {
        out.println("<?xml version=\"1.0\" ?>");
        out.println("<exportpackage" + (this.excludeExportAttributes ? "" : " exportdate=\"" + exportDate + "\" exportbuild=\"" + Build.getBuild() + "\"" + this.getHeaderAttributes()) + ">");
        out.println("\t<checksum>checksum********************************************************************************************</checksum>");
    }

    private String exportFooter(FileOutputStream fos, PrintStream out, File exportFile) throws IOException, SapphireException {
        out.println("</exportpackage>");
        out.close();
        fos.close();
        String cs = ExportXML.generateCheckSum(exportFile, true);
        if (this.zipOut != null) {
            ZipFileUtil.addEntry(exportFile.getParentFile(), exportFile, this.zipOut);
            exportFile.delete();
        }
        return cs;
    }

    static void exportItem(Transferable exportItem, File exportFile, PrintStream out, ZipOutputStream zipOut, DBAccess database, Logger logger, Map exported, boolean multifile, boolean excludeExportAttributes, boolean verbose) throws SapphireException, IOException, CloneNotSupportedException, SQLException {
        exportItem.setVerbose(verbose);
        if (exportItem instanceof TableTransfer) {
            TableTransfer table = (TableTransfer)exportItem;
            table.setExcludeExportAttributes(excludeExportAttributes);
            table.export(exportFile, out, zipOut, database, 1, logger, exported);
        } else if (exportItem instanceof SDCTransfer) {
            SDCTransfer sdc = (SDCTransfer)exportItem;
            sdc.export(exportFile, out, zipOut, database, 1, logger, exported);
        } else if (exportItem instanceof SDITransfer) {
            SDITransfer sdi = (SDITransfer)exportItem;
            sdi.setMultifile(multifile);
            sdi.setExcludeExportAttributes(excludeExportAttributes);
            sdi.export(exportFile, out, zipOut, database, 1, logger, exported);
        } else if (exportItem instanceof PropertyTreeTransfer) {
            PropertyTreeTransfer propertyTreeTransfer = (PropertyTreeTransfer)exportItem;
            propertyTreeTransfer.export(exportFile, out, zipOut, database, 1, logger, exported);
        }
        List items = exportItem.getReferencedItems();
        if (items != null && items.size() > 0) {
            logger.log("\t");
            logger.log("\t----------- Exporting Referenced Items -----------");
            Iterator iterator = items.iterator();
            while (iterator.hasNext()) {
                logger.log("\t");
                Transferable referencedExportItem = (Transferable)iterator.next();
                ExportXML.exportItem(referencedExportItem, exportFile, out, zipOut, database, logger, exported, multifile, excludeExportAttributes, verbose);
            }
        }
    }

    private String getHeaderAttributes() {
        StringBuffer directives = new StringBuffer();
        if (this.headerAttributes != null) {
            for (String propertyid : this.headerAttributes.keySet()) {
                directives.append(" ").append(propertyid).append("=\"").append(this.headerAttributes.getProperty(propertyid)).append("\"");
            }
        }
        return directives.length() > 0 ? directives.toString() : "";
    }

    private static String generateCheckSum(InputStream fis, IntHolder csPos) throws SapphireException {
        try {
            int numRead;
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024];
            int bufferCount = 0;
            int cspos = 0;
            do {
                if ((numRead = fis.read(buffer)) <= 0) continue;
                String sbuffer = new String(buffer, 0, numRead);
                int pos = sbuffer.indexOf("<checksum>");
                if (pos > -1 && sbuffer.indexOf("</checksum>", pos + 110) > -1) {
                    cspos = bufferCount * 1024 + pos;
                    messageDigest.update(buffer, 0, pos);
                    messageDigest.update(buffer, pos + 121, numRead - cspos - 121 > 0 ? numRead - cspos - 121 : 0);
                } else {
                    messageDigest.update(buffer, 0, numRead);
                }
                ++bufferCount;
            } while (numRead != -1);
            byte[] cs = messageDigest.digest();
            StringBuffer scs = new StringBuffer();
            for (int i = 0; i < cs.length; ++i) {
                scs.append(cs[i]).append(" ");
            }
            csPos.value = cspos;
            return scs.toString();
        }
        catch (Exception e) {
            throw new SapphireException("Failed to checksum file. Reason: " + e.getMessage(), e.getMessage());
        }
    }

    public static String generateCheckSum(File exportZipFile, String exportEntry) throws SapphireException {
        try {
            ZipFile zipFile = new ZipFile(exportZipFile);
            ZipEntry zipEntry = new ZipEntry(exportEntry);
            BufferedInputStream inputStream = new BufferedInputStream(zipFile.getInputStream(zipEntry));
            IntHolder cspos = new IntHolder();
            String scs = ExportXML.generateCheckSum(inputStream, cspos);
            inputStream.close();
            return scs;
        }
        catch (IOException e) {
            throw new SapphireException("Failed to checksum zip file entry. Reason: " + e.getMessage(), e);
        }
    }

    public static String generateCheckSum(File exportFile, boolean updateFile) throws SapphireException {
        try {
            FileInputStream fis = new FileInputStream(exportFile);
            IntHolder cspos = new IntHolder();
            String scs = ExportXML.generateCheckSum(fis, cspos);
            ((InputStream)fis).close();
            if (updateFile) {
                RandomAccessFile raf = new RandomAccessFile(exportFile, "rw");
                raf.seek(cspos.value + 10);
                raf.write(scs.toString().getBytes());
                raf.close();
            }
            return scs.toString();
        }
        catch (IOException e) {
            throw new SapphireException("Failed to checksum file. Reason: " + e.getMessage(), e.getMessage());
        }
    }

    public static String generateCheckSum(File exportFile, boolean updateFile, String startStr, String endStr) throws SapphireException {
        try {
            FileInputStream fis = new FileInputStream(exportFile);
            IntHolder checksumPositionHolder = new IntHolder();
            String checksum = ExportXML.generateCheckSum(fis, checksumPositionHolder, startStr, endStr, 1024);
            ((InputStream)fis).close();
            if (checksumPositionHolder.value == -1 && startStr != null && startStr.length() > 0) {
                int retryCount = 0;
                do {
                    Trace.logInfo("ExportXML.generateCheckSum", "Unable to find Checksum String. Re-trying with buffer size: " + (1024 + ++retryCount) + " for file: " + exportFile.getName());
                    fis = new FileInputStream(exportFile);
                    checksumPositionHolder = new IntHolder();
                    checksum = ExportXML.generateCheckSum(fis, checksumPositionHolder, startStr, endStr, 1024 + retryCount);
                    ((InputStream)fis).close();
                } while (checksumPositionHolder.value == -1 && retryCount <= startStr.length());
                if (checksumPositionHolder.value == -1) {
                    throw new SapphireException("GenerateChecksum", "FAILURE", "Unable to generate Checksum for: " + exportFile.getName() + ". '" + startStr + "' literal not found in file.");
                }
            }
            Trace.logInfo("Checksum generated for file: " + exportFile.getName() + ": " + checksum);
            if (updateFile) {
                RandomAccessFile raf = new RandomAccessFile(exportFile, "rw");
                raf.seek(checksumPositionHolder.value + startStr.length());
                raf.write(checksum.getBytes());
                raf.close();
            }
            return checksum;
        }
        catch (IOException e) {
            throw new SapphireException("Failed to checksum file. Reason: " + e.getMessage(), e.getMessage());
        }
    }

    public static String generateCheckSum(InputStream fis, IntHolder csPos, String startStr, String endStr) throws SapphireException {
        return ExportXML.generateCheckSum(fis, csPos, startStr, endStr, 1024);
    }

    private static String generateCheckSum(InputStream fis, IntHolder checksumPositionHolder, String startStr, String endStr, int bufferSize) throws SapphireException {
        try {
            int numRead;
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[bufferSize];
            int bufferCount = 0;
            int checksumPosition = -1;
            do {
                if ((numRead = fis.read(buffer)) <= 0) continue;
                if (checksumPositionHolder == null || startStr == null || startStr.length() == 0 || endStr == null || endStr.length() == 0) {
                    messageDigest.update(buffer, 0, numRead);
                    continue;
                }
                String sbuffer = new String(buffer, 0, numRead);
                int startPos = sbuffer.indexOf(startStr);
                int endPos = sbuffer.indexOf(endStr);
                if (startPos > -1 && endPos > -1) {
                    checksumPosition = bufferCount * bufferSize + startPos;
                    messageDigest.update(buffer, 0, startPos);
                    messageDigest.update(buffer, startPos + (startStr.length() + 100 + endStr.length()), numRead - checksumPosition - (startStr.length() + 100 + endStr.length()) > 0 ? numRead - checksumPosition - (startStr.length() + 100 + endStr.length()) : 0);
                } else if (startPos > -1 && endPos == -1) {
                    checksumPosition = bufferCount * bufferSize + startPos;
                    messageDigest.update(buffer, 0, startPos);
                } else if (startPos == -1 && endPos > -1) {
                    messageDigest.update(buffer, endPos + endStr.length(), numRead - (endPos + endStr.length()));
                } else {
                    messageDigest.update(buffer, 0, numRead);
                }
                ++bufferCount;
            } while (numRead != -1);
            byte[] cs = messageDigest.digest();
            StringBuffer checksum = new StringBuffer();
            for (int i = 0; i < cs.length; ++i) {
                checksum.append(cs[i]).append(" ");
            }
            if (checksumPositionHolder != null) {
                checksumPositionHolder.value = checksumPosition;
            }
            return checksum.toString();
        }
        catch (Exception e) {
            throw new SapphireException("Failed to checksum file. Reason: " + e.getMessage(), e.getMessage());
        }
    }

    public void generateAntScript(File antFile, String target, HashMap antProps) throws SapphireException {
        if (this.exports == null || this.exports.size() == 0) {
            throw new SapphireException("No export items have been defined");
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(antFile);
            PrintStream out = new PrintStream((OutputStream)fos, true, "UTF-8");
            out.print(AntUtil.getAntFileHeader(target, target, antProps));
            out.println("\t<target name=\"" + target + "\">");
            out.println("\t\t<exportXML " + (this.dirname != null ? "dir=\"" + this.dirname + "\"" : (this.filename != null ? "file=\"" + this.filename + "\"" : "")) + ">");
            ConnectionTask.generateAntTask(out, 3, this.database.isOracle() ? "ORA" : "MSS");
            for (int i = 0; i < this.exports.size(); ++i) {
                Transferable export = (Transferable)this.exports.get(i);
                export.generateAntTask(out, 3);
            }
            out.println("\t\t</exportXML>");
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

    public void generateTransferPackageXML(PrintStream out) {
        out.println("\t\t<exportXML " + (this.dirname != null ? "dir=\"" + this.dirname + "\"" : (this.filename != null ? "file=\"" + this.filename + "\"" : "")) + " forceupdate=\"" + this.forceUpdate + "\" forcenullupdate=\"" + this.forceNullUpdate + "\">");
        for (int i = 0; i < this.exports.size(); ++i) {
            Transferable export = (Transferable)this.exports.get(i);
            export.generateAntTask(out, 3);
        }
        out.println("\t\t</exportXML>");
    }

    public void syncDDT() throws SapphireException {
        if (this.database == null) {
            throw new SapphireException("DBUtil not defined");
        }
        this.database.executePreparedUpdate("{call lv_ddt" + (this.database.isOracle() ? "." : "_") + "syncddt( ? ) }", new Object[]{Build.getBuild()});
    }

    public void addExport(Transferable export) {
        this.exports.add(export);
    }

    public int moveExport(int pos, int direction) {
        if (pos + direction >= 0 && pos + direction < this.exports.size()) {
            Transferable export1 = (Transferable)this.exports.get(pos);
            Transferable export2 = (Transferable)this.exports.get(pos + direction);
            this.exports.set(pos, export2);
            this.exports.set(pos + direction, export1);
            return pos + direction;
        }
        return pos;
    }

    @Override
    public void log(String message) {
    }

    public boolean isValid() {
        return this.dirname != null || this.filename != null;
    }

    public static String removeIllegalChars(String input) {
        StringBuffer outputString = new StringBuffer(input.length());
        int count = 1;
        for (int i = 0; i < input.length(); ++i) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(input.charAt(i));
            if (input.charAt(i) == '/' || input.charAt(i) == '\\' || input.charAt(i) == '%' || input.charAt(i) == ':' || input.charAt(i) == '*' || input.charAt(i) == '?' || input.charAt(i) == '<' || input.charAt(i) == '>' || input.charAt(i) == '|' || input.charAt(i) == '&') {
                outputString.append("~").append(count++);
                continue;
            }
            if (block.equals(Character.UnicodeBlock.BASIC_LATIN)) {
                outputString.append(input.charAt(i));
                continue;
            }
            outputString.append("~").append(count++);
        }
        return outputString.toString();
    }

    public static boolean hasUnicodeChars(String input) {
        for (int i = 0; i < input.length(); ++i) {
            if (Character.UnicodeBlock.of(input.charAt(i)).equals(Character.UnicodeBlock.BASIC_LATIN)) continue;
            return true;
        }
        return false;
    }
}

