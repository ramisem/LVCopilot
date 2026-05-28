/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.compress.archivers.tar.TarArchiveEntry
 *  org.apache.commons.compress.archivers.tar.TarArchiveInputStream
 */
package com.labvantage.sapphire.modules.sdms;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.capa.RaiseAlert;
import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.util.file.FileTransfer;
import com.labvantage.sapphire.util.file.FileTransferOptions;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SDMSUtil
implements SDMSConstants {
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static synchronized String sendCollectorCommand(QueryProcessor qp, ActionProcessor ap, String collectorid, String instrumentid, String commandType, String commandParams) throws SapphireException {
        return SDMSUtil.sendCollectorCommand(qp, ap, collectorid, instrumentid, commandType, commandParams, null);
    }

    public static synchronized String sendCollectorCommand(QueryProcessor qp, ActionProcessor ap, String collectorid, String instrumentid, String commandType, String commandParams, File file) throws SapphireException {
        String fileContents = "";
        if (file != null) {
            try {
                String filename = file.getName();
                byte[] bytes = FileUtil.getFileByteArray(file);
                fileContents = Base64.getEncoder().encodeToString(bytes);
            }
            catch (IOException e) {
                throw new SapphireException("Failed to parse " + file.getAbsolutePath(), e);
            }
        }
        String commandid = "";
        if (qp.getPreparedCount("SELECT count(*) FROM sdmscollectorcommand WHERE sdmscollectorid=? AND commandtype=? AND commandstatusflag=?", new String[]{collectorid, commandType, "P"}) == 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "LV_SDMSCollector");
            props.setProperty("keyid1", collectorid);
            props.setProperty("linkid", "SDMS Collector Command");
            props.setProperty("returngeneratedkey", "Y");
            props.setProperty("instrumentid", instrumentid);
            props.setProperty("commanddt", "n");
            props.setProperty("commandstatusflag", "P");
            props.setProperty("commandtype", commandType);
            props.setProperty("commandparams", commandParams);
            props.setProperty("commandfilecontents", fileContents);
            props.setProperty("separator", "\u00a3\u00a3$$%%^^");
            try {
                ap.processAction("AddSDIDetail", "1", props);
            }
            catch (ActionException e) {
                throw new SapphireException("Failed to add command " + commandType + " to " + collectorid + ". " + e.getMessage());
            }
            commandid = props.getProperty("sdmscollectorcommandid");
        }
        return commandid;
    }

    public static synchronized Path renameFolder(Path renameFolder, String fromSuffix, String toSuffix) throws SapphireException {
        String oldName = renameFolder.getFileName().toString();
        Path newPath = null;
        int count = 0;
        boolean renamed = false;
        while (!renamed) {
            String tempSuffix = count == 0 ? "" : "_" + count;
            String newName = StringUtil.replaceAll(oldName, fromSuffix, tempSuffix + toSuffix);
            newPath = renameFolder.getParent().resolve(newName);
            if (!Files.exists(newPath, new LinkOption[0])) {
                try {
                    Files.move(renameFolder, newPath, new CopyOption[0]);
                    renamed = true;
                }
                catch (FileAlreadyExistsException fileAlreadyExistsException) {
                }
                catch (Exception e) {
                    throw new SapphireException("Failed to rename from " + renameFolder + " to " + newPath, e);
                }
            }
            ++count;
        }
        return newPath;
    }

    public static synchronized Path renameFile(Path renameFile, String fromSuffix, String toSuffix) throws SapphireException {
        String oldName = renameFile.getFileName().toString();
        Path newPath = null;
        int count = 0;
        boolean renamed = false;
        while (!renamed) {
            String tempSuffix = count == 0 ? "" : "_" + count;
            String newName = StringUtil.replaceAll(oldName, fromSuffix, tempSuffix + toSuffix);
            newPath = renameFile.getParent().resolve(newName);
            if (!Files.exists(newPath, new LinkOption[0])) {
                try {
                    Files.move(renameFile, newPath, new CopyOption[0]);
                    renamed = true;
                }
                catch (FileAlreadyExistsException fileAlreadyExistsException) {
                }
                catch (Exception e) {
                    throw new SapphireException("Failed to rename from " + renameFile + " to " + newPath, e);
                }
            }
            ++count;
        }
        return newPath;
    }

    public static synchronized Path renameAndZipFolder(Path folderPath, String fromSuffix, String toSuffix) throws SapphireException {
        if (!folderPath.isAbsolute()) {
            folderPath = folderPath.toAbsolutePath();
        }
        String oldName = folderPath.getFileName().toString();
        Path newPath = null;
        int count = 0;
        boolean renamed = false;
        while (!renamed) {
            String tempSuffix = count == 0 ? "" : "_" + count;
            String newName = StringUtil.replaceAll(oldName, fromSuffix, tempSuffix + toSuffix);
            newPath = folderPath.getParent().resolve(newName + ".zip");
            if (!Files.exists(newPath, new LinkOption[0])) {
                try {
                    FileTransferOptions options = new FileTransferOptions();
                    options.setDeleteSourceOnSuccessfullTransfer(true);
                    FileTransfer.safeFileTransfer(folderPath.toFile(), newPath.toFile(), options);
                    renamed = true;
                }
                catch (FileAlreadyExistsException options) {
                }
                catch (Exception e) {
                    renamed = true;
                    e.printStackTrace();
                }
            }
            ++count;
        }
        return newPath;
    }

    public static synchronized Path createWorkingFolder(Path parentFolder, String baseFolderName, String suffix) throws IOException, SapphireException {
        int temp = 0;
        boolean created = false;
        Path workingFolder = null;
        while (!created) {
            String tempSuffix = temp == 0 ? "" : "_" + temp;
            workingFolder = parentFolder.resolve(baseFolderName + tempSuffix + suffix);
            if (!Files.exists(workingFolder, new LinkOption[0])) {
                try {
                    Files.createDirectory(workingFolder, new FileAttribute[0]);
                    created = true;
                }
                catch (FileAlreadyExistsException e) {
                    System.out.println("folder " + baseFolderName + tempSuffix + suffix + " exists, trying again.");
                }
                catch (Exception e) {
                    e.printStackTrace();
                    throw new SapphireException("Failed to generate a working directory: " + e.getMessage());
                }
            }
            ++temp;
        }
        return workingFolder;
    }

    public static String formatCalendar(Calendar calendar) {
        return calendar == null ? "" : sdf.format(calendar.getTime());
    }

    public static String formatDate(Date date) {
        return date == null ? "" : sdf.format(date);
    }

    public static Calendar parseCalendar(String date) throws ParseException {
        Calendar c = Calendar.getInstance();
        c.setTime(sdf.parse(date));
        return c;
    }

    public static ArrayList<Path> getMatchingFiles(Path startDir, String includePattenMatcher) throws IOException {
        return SDMSUtil.getMatchingFiles(startDir, includePattenMatcher, "");
    }

    public static ArrayList<Path> getMatchingFiles(Path startDir, String includePattenMatcher, String excludePatternMatcher) throws IOException {
        ArrayList<Path> matched = new ArrayList<Path>();
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + StringUtil.replaceAll(startDir.toFile().getCanonicalPath(), "\\", "/") + "/" + includePattenMatcher);
        File[] files = startDir.toFile().listFiles();
        for (int i = 0; i < files.length; ++i) {
            File file = files[i];
            if (!file.isFile() || !matcher.matches(file.toPath())) continue;
            matched.add(file.toPath());
        }
        if (excludePatternMatcher.length() > 0) {
            ArrayList<Path> tempMatched = new ArrayList<Path>();
            tempMatched.addAll(matched);
            PathMatcher excludeMatcher = FileSystems.getDefault().getPathMatcher("glob:**/" + excludePatternMatcher);
            for (Path path : tempMatched) {
                if (!excludeMatcher.matches(path)) continue;
                matched.remove(path);
            }
        }
        return matched;
    }

    public static ArrayList<Path> getMatchingDirectories(Path startDir, String globMatcherPattern) throws IOException {
        final ArrayList<Path> matched = new ArrayList<Path>();
        final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + StringUtil.replaceAll(startDir.toFile().getCanonicalPath(), "\\", "/") + "/" + globMatcherPattern);
        SimpleFileVisitor<Path> matcherVisitor = new SimpleFileVisitor<Path>(){

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (matcher.matches(dir)) {
                    matched.add(dir);
                }
                return FileVisitResult.CONTINUE;
            }
        };
        Files.walkFileTree(startDir, (FileVisitor<? super Path>)matcherVisitor);
        return matched;
    }

    public static File getLatestFilefromDir(String dirPath) {
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        File lastModifiedFile = files[0];
        for (int i = 1; i < files.length; ++i) {
            if (lastModifiedFile.lastModified() >= files[i].lastModified()) continue;
            lastModifiedFile = files[i];
        }
        return lastModifiedFile;
    }

    public static List<String> getMatchingFilesFromDirectory(File directory, String pattern) {
        ArrayList<String> result = new ArrayList<String>();
        for (File f : directory.listFiles()) {
            if (f.isDirectory()) {
                result.addAll(SDMSUtil.getMatchingFilesFromDirectory(f, pattern));
            }
            if (!f.isFile() || !f.getName().matches(pattern)) continue;
            result.add(f.getAbsolutePath());
        }
        return result;
    }

    public static int countFilesInDirectory(File directory) {
        int count = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                ++count;
            }
            if (!file.isDirectory()) continue;
            count += SDMSUtil.countFilesInDirectory(file);
        }
        return count;
    }

    public static int getRandomInteger(int maximum, int minimum) {
        return (int)(Math.random() * (double)(maximum - minimum)) + minimum;
    }

    public static File folderToZip(File folder, boolean includeSubfolder, String includePattern, String excludePattern, File zipFile) throws Exception {
        FileTransferOptions options = new FileTransferOptions();
        options.setReplaceTarget(true);
        if (!includeSubfolder) {
            options.setIncludeSubFolder(false);
        }
        if (includePattern != null && includePattern.length() > 0) {
            options.addFileInclude(includePattern);
        }
        if (excludePattern != null && excludePattern.length() > 0) {
            options.addFileExclude(excludePattern);
        }
        options.setForceDeleteTargetRetryCount(3);
        FileTransfer.safeFileTransfer(folder, zipFile, options);
        return zipFile;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void extractTarStream(InputStream inputStream, File destination) throws IOException {
        FileOutputStream out = null;
        if (!destination.isAbsolute()) {
            destination = destination.toPath().toAbsolutePath().toFile();
        }
        try (TarArchiveInputStream fin = new TarArchiveInputStream(inputStream);){
            TarArchiveEntry entry;
            int i = 1;
            while ((entry = fin.getNextTarEntry()) != null) {
                if (i == 1) {
                    ++i;
                    continue;
                }
                String outFilename = entry.getName().substring(entry.getName().indexOf("/") + 1);
                if (entry.isDirectory()) {
                    File folder = new File(destination, outFilename);
                    if (folder.mkdirs()) continue;
                    throw new IOException("Failed to create directory.");
                }
                out = new FileOutputStream(new File(destination, outFilename));
                try {
                    int len;
                    byte[] buf = new byte[1024];
                    while ((len = fin.read(buf)) > 0) {
                        ((OutputStream)out).write(buf, 0, len);
                    }
                }
                finally {
                    if (out == null) continue;
                    ((OutputStream)out).close();
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void extractZipStream(InputStream inputStream, File destination) throws IOException {
        ZipInputStream in = null;
        FileOutputStream out = null;
        if (!destination.isAbsolute()) {
            destination = destination.toPath().toAbsolutePath().toFile();
        }
        in = new ZipInputStream(inputStream);
        try {
            ZipEntry entry = null;
            while ((entry = in.getNextEntry()) != null) {
                String outFilename = entry.getName();
                if (outFilename.endsWith("\\") || outFilename.endsWith("/")) {
                    File folder = new File(destination, outFilename);
                    if (folder.mkdirs()) continue;
                    throw new IOException("Failed to create directory.");
                }
                out = new FileOutputStream(new File(destination, outFilename));
                try {
                    int len;
                    byte[] buf = new byte[1024];
                    while ((len = in.read(buf)) > 0) {
                        ((OutputStream)out).write(buf, 0, len);
                    }
                }
                finally {
                    if (out == null) continue;
                    ((OutputStream)out).close();
                }
            }
        }
        finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public static String getExtension(String filepath) {
        return filepath.lastIndexOf(".") > -1 ? filepath.substring(filepath.lastIndexOf(".") + 1) : "";
    }

    public static PropertyList getCollectorStartupState(QueryProcessor qp, String collectorid) {
        if (collectorid == null || collectorid.length() == 0) {
            return null;
        }
        DataSet collector = qp.getPreparedSqlDataSet("SELECT startupstate FROM sdmscollector WHERE sdmscollectorid=?", (Object[])new String[]{collectorid}, true);
        if (collector.size() == 0) {
            return null;
        }
        PropertyList startupState = new PropertyList();
        try {
            startupState.setPropertyList(collector.getValue(0, "startupstate", "<propertylist/>"));
        }
        catch (SapphireException sapphireException) {
            // empty catch block
        }
        return startupState;
    }

    public static String raiseSDMSAlert(ActionProcessor ap, String sdcid, String keyid1, String alertType, String severity, boolean forceNew, String description, String message) {
        return SDMSUtil.raiseSDMSAlert(ap, sdcid, keyid1, alertType, severity, forceNew, true, description, message);
    }

    public static String raiseSDMSAlert(ActionProcessor ap, String sdcid, String keyid1, String alertType, String severity, boolean forceNew, boolean matchDescription, String description, String message) {
        return SDMSUtil.raiseSDMSAlert(ap, sdcid, keyid1, null, null, alertType, severity, forceNew, matchDescription, description, message);
    }

    public static String raiseSDMSAlert(ActionProcessor ap, String sdcid, String keyid1, String keyid2, String keyid3, String alertType, String severity, boolean forceNew, boolean matchDescription, String description, String message) {
        PropertyList props = new PropertyList();
        props.setProperty("causalsdcid", sdcid);
        props.setProperty("causalkeyid1", keyid1);
        if (keyid2 != null && keyid2.length() > 0) {
            props.setProperty("causalkeyid2", keyid2);
        }
        if (keyid3 != null && keyid3.length() > 0) {
            props.setProperty("causalkeyid3", keyid3);
        }
        props.setProperty("alerttype", alertType);
        props.setProperty("severity", severity);
        props.setProperty("forcenew", forceNew ? "Y" : "N");
        props.setProperty("description", description);
        props.setProperty("explanation", message);
        props.setProperty("matchcausalsdi", "Y");
        props.setProperty("matchdescription", matchDescription ? "Y" : "N");
        props.setProperty("matchexplanation", "N");
        try {
            ap.processActionClass(RaiseAlert.class.getName(), props, true);
        }
        catch (ActionException e) {
            Trace.logError("Failed to raise and alert for " + sdcid + " " + keyid1 + ": " + e.getMessage(), e);
        }
        return props.getProperty("newkeyid1");
    }
}

