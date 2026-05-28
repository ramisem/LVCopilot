/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire;

import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.util.file.FileOperationListener;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import sapphire.SapphireException;
import sapphire.util.StringUtil;

public class FileUtil {
    private static final char[] hexDigit = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static Path createTempFile() throws IOException {
        return FileUtil.createTempFile(null, null);
    }

    public static Path createTempFile(String prefix, String suffix) throws IOException {
        return FileUtil.createTempFile(prefix, suffix, true);
    }

    public static Path createTempFile(String prefix, String suffix, boolean deleteOnExit) throws IOException {
        if (suffix == null || suffix.length() == 0) {
            suffix = ".tmp";
        }
        if (prefix == null || prefix.length() == 0) {
            SecureRandom random = new SecureRandom();
            prefix = new BigInteger(130, random).toString(32);
        }
        Path out = Files.createTempFile(prefix, suffix, new FileAttribute[0]);
        out.toFile().deleteOnExit();
        return out;
    }

    public static Path createTempDirectory(String prefix, boolean deleteOnExit) throws IOException {
        if (prefix == null || prefix.length() == 0) {
            SecureRandom random = new SecureRandom();
            prefix = new BigInteger(130, random).toString(32);
        }
        Path out = Files.createTempDirectory(prefix, new FileAttribute[0]);
        if (deleteOnExit) {
            out.toFile().deleteOnExit();
        }
        return out;
    }

    public static String substituteConfigurationPaths(String filename) {
        try {
            Configuration config = Configuration.getInstance();
            filename = StringUtil.replaceAll(filename, "[labvantagehome]", config.getSapphireHome(), false);
            filename = StringUtil.replaceAll(filename, "[sapphirehome]", config.getSapphireHome(), false);
            filename = StringUtil.replaceAll(filename, "[applicationhome]", config.getApplicationHome(), false);
            filename = StringUtil.replaceAll(filename, "\\", "/");
        }
        catch (SapphireException sapphireException) {
            // empty catch block
        }
        return filename;
    }

    public static int fileCount(File file) throws IOException {
        return FileUtil.fileCount(file, 0);
    }

    private static int fileCount(File file, int base) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            int count = 0;
            if (files != null) {
                for (int i = 0; i < files.length; ++i) {
                    count += FileUtil.fileCount(files[i], base);
                }
            }
            return base + count;
        }
        return 1;
    }

    public static long fileSize(File file) throws IOException {
        return FileUtil.fileSize(file, 0);
    }

    private static long fileSize(File file, int base) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            int size = 0;
            if (files != null) {
                for (int i = 0; i < files.length; ++i) {
                    size = (int)((long)size + FileUtil.fileSize(files[i], base));
                }
            }
            return base + size;
        }
        return file.length();
    }

    public static void deleteAll(File file) throws IOException {
        FileUtil.deleteAll(file, null, false);
    }

    public static void deleteAll(File file, FileOperationListener listener, boolean retry) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; ++i) {
                    FileUtil.deleteAll(files[i], listener, retry);
                }
            }
            if (retry) {
                FileUtil.delete(file);
            } else {
                file.delete();
            }
        } else {
            if (retry) {
                FileUtil.delete(file);
            } else {
                file.delete();
            }
            if (listener != null) {
                listener.fileDeleted(file);
            }
        }
    }

    private static boolean delete(File file) {
        int tries = 10;
        boolean success = false;
        for (int tryCount = 0; tryCount < tries && !(success = file.delete()); ++tryCount) {
            if (tries <= 1) continue;
            try {
                Thread.sleep(1000L);
                continue;
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
        }
        return success;
    }

    public static void copyAll(File from, File to) throws IOException {
        FileUtil.copyAll(from, to, null);
    }

    public static void copyAll(File from, File to, FileOperationListener listener) throws IOException {
        if (from.isDirectory()) {
            File[] files;
            File toPath;
            File file = toPath = to.isDirectory() ? to : new File(to.getParent(), from.getName());
            if (!toPath.exists()) {
                toPath.mkdirs();
            }
            if ((files = from.listFiles()) != null) {
                for (int i = 0; i < files.length; ++i) {
                    FileUtil.copyAll(files[i], new File(toPath, files[i].getName()), listener);
                }
            }
        } else {
            if (to.exists()) {
                to.delete();
            }
            FileUtil.copyFile(from, to);
            if (listener != null) {
                listener.fileCopied(from, to);
            }
        }
    }

    public static boolean renameTo(File from, File to) {
        return FileUtil.renameTo(from, to, null);
    }

    public static boolean renameTo(File from, File to, FileOperationListener listener) {
        int tries = 20;
        boolean success = false;
        for (int tryCount = 0; tryCount < tries && !(success = from.renameTo(to)); ++tryCount) {
            try {
                Thread.sleep(1000L);
                continue;
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
        }
        if (listener != null) {
            listener.fileRenamed(from, to);
        }
        return success;
    }

    public static String getInputStreamString(InputStream is) throws IOException {
        return FileUtil.getInputStreamString(is, "UTF-8");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String getInputStreamString(InputStream is, String encoding) throws IOException {
        if (encoding == null || encoding.length() == 0) {
            encoding = "UTF-8";
        }
        InputStreamReader isr = new InputStreamReader(is, encoding);
        try (BufferedReader in = new BufferedReader(isr);){
            StringBuffer buffer = new StringBuffer();
            String aLine = "";
            while ((aLine = in.readLine()) != null) {
                buffer.append(aLine);
                buffer.append("\n");
            }
            String string = buffer.toString();
            return string;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void copyFile(File from, File to) throws IOException {
        try (FileInputStream fis = new FileInputStream(from);){
            to.getParentFile().mkdirs();
            try (FileOutputStream fos = new FileOutputStream(to);){
                byte[] buf = new byte[1024];
                int i = 0;
                while ((i = fis.read(buf)) != -1) {
                    fos.write(buf, 0, i);
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static byte[] getFileByteArray(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);){
            int lengthread;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] bytebuff = new byte[500];
            while ((lengthread = fis.read(bytebuff)) != -1) {
                baos.write(bytebuff, 0, lengthread);
            }
            byte[] byArray = baos.toByteArray();
            return byArray;
        }
    }

    public static String getFileString(File file) throws IOException {
        return FileUtil.getFileString(file, "");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String getFileString(File file, String encoding) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);){
            String string = FileUtil.getInputStreamString(fis, encoding);
            return string;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void writeFileString(File file, String fileContents) throws IOException {
        FileOutputStream fos = null;
        PrintWriter out = null;
        try {
            fos = new FileOutputStream(file);
            out = new PrintWriter(fos);
            out.print(fileContents);
        }
        finally {
            if (out != null) {
                out.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    public static boolean wildcardMatch(String filename, String wildcardMatcher, boolean caseSensitive) {
        if (filename == null && wildcardMatcher == null) {
            return true;
        }
        if (filename == null || wildcardMatcher == null) {
            return false;
        }
        filename = caseSensitive ? filename.toLowerCase() : filename;
        wildcardMatcher = caseSensitive ? wildcardMatcher.toLowerCase() : wildcardMatcher;
        String[] wcs = FileUtil.splitOnTokens(wildcardMatcher);
        boolean anyChars = false;
        int textIdx = 0;
        int wcsIdx = 0;
        Stack<int[]> backtrack = new Stack<int[]>();
        do {
            if (backtrack.size() > 0) {
                int[] array = (int[])backtrack.pop();
                wcsIdx = array[0];
                textIdx = array[1];
                anyChars = true;
            }
            while (wcsIdx < wcs.length) {
                if (wcs[wcsIdx].equals("?")) {
                    ++textIdx;
                    anyChars = false;
                } else if (wcs[wcsIdx].equals("*")) {
                    anyChars = true;
                    if (wcsIdx == wcs.length - 1) {
                        textIdx = filename.length();
                    }
                } else {
                    if (anyChars) {
                        if ((textIdx = filename.indexOf(wcs[wcsIdx], textIdx)) == -1) break;
                        int repeat = filename.indexOf(wcs[wcsIdx], textIdx + 1);
                        if (repeat >= 0) {
                            backtrack.push(new int[]{wcsIdx, repeat});
                        }
                    } else {
                        if (wcs[wcsIdx].endsWith(System.getProperty("file.separator"))) {
                            wcs[wcsIdx] = wcs[wcsIdx].substring(0, wcs[wcsIdx].length() - 1);
                        }
                        if (!filename.startsWith(wcs[wcsIdx], textIdx)) break;
                    }
                    textIdx += wcs[wcsIdx].length();
                    anyChars = false;
                }
                ++wcsIdx;
            }
            if (wcsIdx != wcs.length || textIdx != filename.length()) continue;
            return true;
        } while (backtrack.size() > 0);
        return false;
    }

    private static String[] splitOnTokens(String text) {
        if (text.indexOf("?") == -1 && text.indexOf("*") == -1) {
            return new String[]{text};
        }
        char[] array = text.toCharArray();
        ArrayList<String> list = new ArrayList<String>();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < array.length; ++i) {
            if (array[i] == '?' || array[i] == '*') {
                if (buffer.length() != 0) {
                    list.add(buffer.toString());
                    buffer.setLength(0);
                }
                if (array[i] == '?') {
                    list.add("?");
                    continue;
                }
                if (list.size() != 0 && (i <= 0 || list.get(list.size() - 1).equals("*"))) continue;
                list.add("*");
                continue;
            }
            buffer.append(array[i]);
        }
        if (buffer.length() != 0) {
            list.add(buffer.toString());
        }
        return list.toArray(new String[0]);
    }

    public static File[] sortFilesByLastModified(File[] fileList) {
        for (int j = 0; j < fileList.length - 1; ++j) {
            for (int k = j + 1; k < fileList.length; ++k) {
                if (fileList[j].lastModified() <= fileList[k].lastModified()) continue;
                File temp = fileList[j];
                fileList[j] = fileList[k];
                fileList[k] = temp;
            }
        }
        return fileList;
    }

    public static void storeProperties(File file, Map props) throws IOException {
        FileUtil.storeProperties(file, props, "");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void storeProperties(File file, Map props, String comment) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file);
             PrintWriter out = new PrintWriter(fos);){
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
            out.println("#" + comment);
            out.println("#" + sdf.format(cal.getTime()));
            for (String property : props.keySet()) {
                out.println(property + "=" + FileUtil.saveConvert((String)props.get(property), false));
            }
        }
    }

    private static String saveConvert(String theString, boolean escapeSpace) {
        int len = theString.length();
        int bufLen = len * 2;
        if (bufLen < 0) {
            bufLen = Integer.MAX_VALUE;
        }
        StringBuffer outBuffer = new StringBuffer(bufLen);
        block8: for (int x = 0; x < len; ++x) {
            char aChar = theString.charAt(x);
            if (aChar > '=' && aChar < '\u007f') {
                if (aChar == '\\') {
                    outBuffer.append('\\');
                    outBuffer.append('\\');
                    continue;
                }
                outBuffer.append(aChar);
                continue;
            }
            switch (aChar) {
                case ' ': {
                    if (x == 0 || escapeSpace) {
                        outBuffer.append('\\');
                    }
                    outBuffer.append(' ');
                    continue block8;
                }
                case '\t': {
                    outBuffer.append('\\');
                    outBuffer.append('t');
                    continue block8;
                }
                case '\n': {
                    outBuffer.append('\\');
                    outBuffer.append('n');
                    continue block8;
                }
                case '\r': {
                    outBuffer.append('\\');
                    outBuffer.append('r');
                    continue block8;
                }
                case '\f': {
                    outBuffer.append('\\');
                    outBuffer.append('f');
                    continue block8;
                }
                case '!': 
                case '#': 
                case ':': 
                case '=': {
                    outBuffer.append('\\');
                    outBuffer.append(aChar);
                    continue block8;
                }
                default: {
                    if (aChar < ' ' || aChar > '~') {
                        outBuffer.append('\\');
                        outBuffer.append('u');
                        outBuffer.append(FileUtil.toHex(aChar >> 12 & 0xF));
                        outBuffer.append(FileUtil.toHex(aChar >> 8 & 0xF));
                        outBuffer.append(FileUtil.toHex(aChar >> 4 & 0xF));
                        outBuffer.append(FileUtil.toHex(aChar & 0xF));
                        continue block8;
                    }
                    outBuffer.append(aChar);
                }
            }
        }
        return outBuffer.toString();
    }

    private static char toHex(int nibble) {
        return hexDigit[nibble & 0xF];
    }

    public static String getExtension(String filepath) {
        String t = FileUtil.getFileName(filepath, true);
        return t.lastIndexOf(".") > -1 ? t.substring(t.lastIndexOf(".") + 1) : "";
    }

    public static String getFileName(String filepath, boolean includeExtension) {
        String filename = filepath.substring(filepath.lastIndexOf("/") > -1 ? filepath.lastIndexOf("/") + 1 : (filepath.lastIndexOf("\\") > -1 ? filepath.lastIndexOf("\\") + 1 : 0));
        if (!includeExtension) {
            filename = filename.lastIndexOf(".") > -1 ? filename.substring(0, filename.lastIndexOf(".")) : filename;
        }
        return filename;
    }

    public static boolean isParentFile(File child, File parent) {
        try {
            Path p;
            File parentPath = parent.getCanonicalFile();
            if (!parentPath.exists() || !parent.isDirectory()) {
                return false;
            }
            File childPath = child.getCanonicalFile();
            if (parentPath.equals(childPath)) {
                return true;
            }
            Path c = childPath.toPath().normalize();
            if (c.startsWith(p = parentPath.toPath().normalize())) {
                return true;
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return false;
    }

    public static void forceDelete(File file) throws IOException {
        if (file.isDirectory()) {
            FileUtil.deleteDirectory(file);
        } else {
            boolean filePresent = file.exists();
            if (!file.delete()) {
                if (!filePresent) {
                    throw new FileNotFoundException("File does not exist: " + file);
                }
                String message = "Unable to delete file: " + file;
                throw new IOException(message);
            }
        }
    }

    private static boolean isSystemWindows() {
        return File.separatorChar == '\\';
    }

    private static boolean isSymlink(File file) throws IOException {
        if (file == null) {
            throw new NullPointerException("File must not be null");
        }
        if (FileUtil.isSystemWindows()) {
            return false;
        }
        File fileInCanonicalDir = null;
        if (file.getParent() == null) {
            fileInCanonicalDir = file;
        } else {
            File canonicalDir = file.getParentFile().getCanonicalFile();
            fileInCanonicalDir = new File(canonicalDir, file.getName());
        }
        return !fileInCanonicalDir.getCanonicalFile().equals(fileInCanonicalDir.getAbsoluteFile());
    }

    public static void deleteDirectory(File directory) throws IOException {
        if (directory.exists()) {
            if (!FileUtil.isSymlink(directory)) {
                FileUtil.cleanDirectory(directory);
            }
            if (!directory.delete()) {
                String message = "Unable to delete directory " + directory + ".";
                throw new IOException(message);
            }
        }
    }

    private static void cleanDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }
        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }
        File[] files = directory.listFiles();
        if (files == null) {
            throw new IOException("Failed to list contents of " + directory);
        }
        IOException exception = null;
        File[] arr$ = files;
        int len$ = files.length;
        for (int i$ = 0; i$ < len$; ++i$) {
            File file = arr$[i$];
            try {
                FileUtil.forceDelete(file);
                continue;
            }
            catch (IOException var8) {
                exception = var8;
            }
        }
        if (null != exception) {
            throw exception;
        }
    }

    public static boolean validateFileName(String fileName) {
        File f;
        try {
            f.getCanonicalPath();
        }
        catch (Exception e) {
            return false;
        }
        for (f = new File(fileName); f != null; f = f.getParentFile()) {
            boolean containsInvalidChar;
            String n = f.getName();
            if (n.length() == 0) {
                if (f.getParentFile() == null) break;
                return false;
            }
            boolean reservedWord = n.toUpperCase().matches("(?:!CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])$");
            boolean endsWithSpaceOrDot = n.endsWith(".") || fileName.endsWith(" ");
            boolean bl = containsInvalidChar = n.contains("\\") || n.contains("/") || n.contains(":") || n.contains("\"") || n.contains("<") || n.contains(">") || n.contains("?") || n.contains("*") || n.contains("|");
            if (!(reservedWord || endsWithSpaceOrDot || containsInvalidChar)) {
                continue;
            }
            return false;
        }
        return true;
    }

    public static String sanitizeStringForFilename(String filepath, String substitute) {
        String filename;
        String originalfilename = filename = FileUtil.getFileName(filepath, true);
        filename = StringUtil.replaceAll(filename, ":", substitute);
        filename = StringUtil.replaceAll(filename, "\"", substitute);
        filename = StringUtil.replaceAll(filename, "<", substitute);
        filename = StringUtil.replaceAll(filename, ">", substitute);
        filename = StringUtil.replaceAll(filename, "?", substitute);
        filename = StringUtil.replaceAll(filename, "*", substitute);
        filename = StringUtil.replaceAll(filename, "|", substitute);
        return StringUtil.replaceAll(filepath, originalfilename, filename);
    }

    public static String resolvePath(String parentPath, String childPath) throws IOException {
        return FileUtil.resolvePath(Paths.get(parentPath, new String[0]), childPath).toString();
    }

    public static Path resolvePath(Path parentPath, String childPath) throws IOException {
        return FileUtil.resolvePath(parentPath, Paths.get(childPath, new String[0]));
    }

    public static Path resolvePath(Path parentPath, Path childPath) throws IOException {
        if (!parentPath.isAbsolute()) {
            throw new IOException("Parent path must be absolute");
        }
        if (childPath.isAbsolute()) {
            throw new IOException("Child path must be relative");
        }
        Path resolvedPath = parentPath.resolve(childPath).normalize();
        if (!resolvedPath.startsWith(parentPath)) {
            throw new IOException("Resolved path escapes the parent path.");
        }
        return resolvedPath;
    }

    public static Path moveFileOrFolder(Path file, Path destination, boolean replace) throws SapphireException {
        if (destination == null || !Files.exists(destination, new LinkOption[0])) {
            return file;
        }
        Path filename = file.getFileName();
        if (replace) {
            Path finalDestination = destination.resolve(filename);
            try {
                return Files.move(file, finalDestination, StandardCopyOption.REPLACE_EXISTING);
            }
            catch (Exception e) {
                throw new SapphireException("Could not move file.", e);
            }
        }
        Path finalDestination = FileUtil.findFileName(destination, filename.toString());
        if (!Files.exists(finalDestination, new LinkOption[0])) {
            try {
                return Files.move(file, finalDestination, new CopyOption[0]);
            }
            catch (Exception e) {
                throw new SapphireException("Could not move file.", e);
            }
        }
        throw new SapphireException("Could not move file as file exists.");
    }

    public static boolean moveFiles(List<Path> files, Path destination, boolean replaceDestination) {
        if (destination == null || !Files.exists(destination, new LinkOption[0])) {
            return false;
        }
        boolean result = true;
        for (Path file : files) {
            try {
                Path fin = FileUtil.moveFileOrFolder(file, destination, replaceDestination);
                if (fin != file) continue;
                return false;
            }
            catch (Exception e) {
                return false;
            }
        }
        return result;
    }

    public static boolean deleteFiles(List<Path> files) {
        boolean result = true;
        for (Path file : files) {
            try {
                FileUtil.deleteAll(file.toFile());
            }
            catch (Exception e) {
                return false;
            }
        }
        return result;
    }

    private static Path findFileName(Path dir, String filename) {
        String baseName = FileUtil.getFileName(filename, false);
        String extension = FileUtil.getExtension(filename);
        Path ret = dir.resolve(String.format("%s.%s", baseName, extension));
        if (!Files.exists(ret, new LinkOption[0])) {
            return ret;
        }
        for (int i = 0; i < 0x7FFFFFFE; ++i) {
            ret = dir.resolve(String.format("%s%d.%s", baseName, i + 1, extension));
            if (Files.exists(ret, new LinkOption[0])) continue;
            return ret;
        }
        throw new IllegalStateException("Cannot find filename.");
    }

    public static boolean isZipFileContainsDirectory(String filename) {
        try {
            ZipFile zipFile = new ZipFile(filename);
            Enumeration<? extends ZipEntry> e = zipFile.entries();
            while (e.hasMoreElements()) {
                ZipEntry entry = e.nextElement();
                boolean isDir = entry.isDirectory();
                if (!isDir) continue;
                return true;
            }
            zipFile.close();
        }
        catch (IOException ioe) {
            System.out.println("Error opening zip file" + ioe);
        }
        return false;
    }

    public static boolean isAnyZipFileContentMissingInFolder(File file, Path path) {
        try {
            ZipFile zipFile = new ZipFile(file);
            Enumeration<? extends ZipEntry> e = zipFile.entries();
            while (e.hasMoreElements()) {
                ZipEntry entry = e.nextElement();
                if (path.resolve(entry.getName()).toFile().exists()) continue;
                return true;
            }
            zipFile.close();
        }
        catch (IOException ioe) {
            System.out.println("Error opening zip file" + ioe);
        }
        return false;
    }

    public static String getCommonParentFolder(List<Path> filePaths) {
        int index = 0;
        String[] parentDirectoryPaths = new String[filePaths.size()];
        for (Path file : filePaths) {
            String parentDirPath = file.getParent().toString();
            parentDirectoryPaths[index] = parentDirPath.substring(parentDirPath.indexOf(File.separator) + 1);
            ++index;
        }
        ArrayList<String> commonParentFolders = new ArrayList<String>();
        String[][] pathFolderNames = new String[parentDirectoryPaths.length][];
        int minPathLength = 0;
        for (int i = 0; i < parentDirectoryPaths.length; ++i) {
            pathFolderNames[i] = StringUtil.split(parentDirectoryPaths[i], File.separator);
            int folderLength = pathFolderNames[i].length;
            if (minPathLength != 0 && minPathLength <= folderLength) continue;
            minPathLength = folderLength;
        }
        for (int k = minPathLength - 1; k >= 0; --k) {
            boolean misMatched = false;
            for (int i = 1; i < parentDirectoryPaths.length; ++i) {
                if (pathFolderNames[0][k].equalsIgnoreCase(pathFolderNames[i][k])) continue;
                misMatched = true;
                if (commonParentFolders.size() <= 0) break;
                commonParentFolders.clear();
                break;
            }
            if (misMatched) continue;
            commonParentFolders.add(pathFolderNames[0][k]);
        }
        if (commonParentFolders.size() > 0) {
            return (String)commonParentFolders.get(0);
        }
        return "";
    }
}

