/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.file;

import com.labvantage.sapphire.util.file.ZipFileListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import sapphire.SapphireException;
import sapphire.util.StringUtil;

public class ZipFileUtil {
    private static final int BUFFERSIZE = 2048;

    public static void extractAll(File extractFromFile, File extractToDir) throws SapphireException {
        ZipFileUtil.extractAll(extractFromFile, extractToDir, null);
    }

    public static int extractAll(File extractFromFile, File extractToDir, ZipFileListener listener) throws SapphireException {
        ZipFile zipFile = null;
        double extracted = 0.0;
        try {
            zipFile = new ZipFile(extractFromFile);
            double size = zipFile.size();
            Enumeration<? extends ZipEntry> e = zipFile.entries();
            ZipEntry zipEntry = null;
            while (e.hasMoreElements()) {
                zipEntry = e.nextElement();
                if (zipEntry.getName().endsWith("\\") || zipEntry.getName().endsWith("/")) continue;
                ZipFileUtil.extractFile(extractToDir, zipFile, zipEntry);
                if (listener == null) continue;
                listener.fileExtracted(zipEntry, (int)((extracted += 1.0) / size * 100.0));
            }
            if (listener != null) {
                listener.fileExtracted(zipEntry, 100);
            }
        }
        catch (IOException e) {
            throw new SapphireException("Failed to extractFile all files. Reason: " + e.getMessage(), e);
        }
        finally {
            try {
                if (zipFile != null) {
                    zipFile.close();
                }
            }
            catch (IOException iOException) {}
        }
        return (int)extracted;
    }

    public static File extractFile(File extractFromFile, String entry, File extractToDir) throws SapphireException {
        ZipFile zipFile = null;
        try {
            File extractFile = null;
            zipFile = new ZipFile(extractFromFile);
            ZipEntry zipEntry = zipFile.getEntry(entry);
            if (zipEntry == null) {
                throw new SapphireException("Failed to find entry '" + entry + "' in ZIP file '" + extractFromFile.getAbsolutePath());
            }
            extractFile = ZipFileUtil.extractFile(extractToDir, zipFile, zipEntry);
            File file = extractFile;
            return file;
        }
        catch (IOException e) {
            throw new SapphireException("Failed to extractFile extractFromFile. Reason: " + e.getMessage(), e);
        }
        finally {
            try {
                if (zipFile != null) {
                    zipFile.close();
                }
            }
            catch (IOException iOException) {}
        }
    }

    public static String extractString(File extractFromFile, String entry) throws SapphireException {
        ZipFile zipFile = null;
        try {
            String extractString = null;
            zipFile = new ZipFile(extractFromFile);
            ZipEntry zipEntry = zipFile.getEntry(entry);
            if (zipEntry == null) {
                throw new SapphireException("Failed to find entry '" + entry + "' in ZIP file '" + extractFromFile.getAbsolutePath());
            }
            extractString = new String(ZipFileUtil.extractByteArray(zipFile, zipEntry), "UTF-8");
            String string = extractString;
            return string;
        }
        catch (IOException e) {
            throw new SapphireException("Failed to extractFile extractFromFile. Reason: " + e.getMessage(), e);
        }
        finally {
            try {
                if (zipFile != null) {
                    zipFile.close();
                }
            }
            catch (IOException iOException) {}
        }
    }

    public static byte[] extractByteArray(File extractFromFile, String entry) throws SapphireException {
        ZipFile zipFile = null;
        try {
            byte[] extractByteArray = null;
            zipFile = new ZipFile(extractFromFile);
            ZipEntry zipEntry = zipFile.getEntry(entry);
            if (zipEntry == null) {
                throw new SapphireException("Failed to find entry '" + entry + "' in ZIP file '" + extractFromFile.getAbsolutePath());
            }
            extractByteArray = ZipFileUtil.extractByteArray(zipFile, zipEntry);
            byte[] byArray = extractByteArray;
            return byArray;
        }
        catch (IOException e) {
            throw new SapphireException("Failed to extractFile extractFromFile. Reason: " + e.getMessage(), e);
        }
        finally {
            try {
                if (zipFile != null) {
                    zipFile.close();
                }
            }
            catch (IOException iOException) {}
        }
    }

    private static File extractFile(File extractToDir, ZipFile zipFile, ZipEntry zipEntry) throws IOException {
        File extractFile = new File(extractToDir, zipEntry.getName());
        extractFile.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(extractFile);
        BufferedOutputStream outputStream = new BufferedOutputStream(fos, 2048);
        ZipFileUtil.extractStream(zipFile, zipEntry, outputStream);
        outputStream.flush();
        outputStream.close();
        fos.close();
        return extractFile;
    }

    private static byte[] extractByteArray(ZipFile zipFile, ZipEntry zipEntry) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedOutputStream outputStream = new BufferedOutputStream(baos);
        ZipFileUtil.extractStream(zipFile, zipEntry, outputStream);
        outputStream.flush();
        outputStream.close();
        return baos.toByteArray();
    }

    private static void extractStream(ZipFile zipFile, ZipEntry zipEntry, BufferedOutputStream outputStream) throws IOException {
        int count;
        byte[] data = new byte[2048];
        BufferedInputStream inputStream = new BufferedInputStream(zipFile.getInputStream(zipEntry));
        while ((count = inputStream.read(data, 0, 2048)) != -1) {
            outputStream.write(data, 0, count);
        }
        inputStream.close();
    }

    public static boolean entryExists(File extractFromFile, String entry) throws SapphireException {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(extractFromFile);
            ZipEntry zipEntry = zipFile.getEntry(entry);
            boolean bl = zipEntry != null;
            return bl;
        }
        catch (IOException e) {
            throw new SapphireException("Failed to extractFile extractFromFile. Reason: " + e.getMessage(), e);
        }
        finally {
            try {
                if (zipFile != null) {
                    zipFile.close();
                }
            }
            catch (IOException iOException) {}
        }
    }

    public static void addEntry(File rootDir, File file, ZipOutputStream zipOutputStream) throws SapphireException {
        FileInputStream fis = null;
        BufferedInputStream inputStream = null;
        try {
            int count;
            fis = new FileInputStream(file);
            inputStream = new BufferedInputStream(fis, 2048);
            ZipEntry entry = new ZipEntry(file.getAbsolutePath().substring(rootDir.getAbsolutePath().length() + (rootDir.getAbsolutePath().endsWith(File.separator) ? 0 : 1)));
            zipOutputStream.putNextEntry(entry);
            byte[] data = new byte[2048];
            while ((count = inputStream.read(data, 0, 2048)) != -1) {
                zipOutputStream.write(data, 0, count);
            }
        }
        catch (IOException e) {
            throw new SapphireException("Failed to add zip file entry. Reason: " + e.getMessage(), e);
        }
        finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (fis != null) {
                    fis.close();
                }
            }
            catch (IOException iOException) {}
        }
    }

    public static void addEntry(String zipEntryName, InputStream sourceInputStream, ZipOutputStream zipOutputStream) throws SapphireException {
        ZipFileUtil.addEntry(zipEntryName, sourceInputStream, zipOutputStream, null);
    }

    public static void addEntry(String zipEntryName, InputStream sourceInputStream, ZipOutputStream zipOutputStream, StringBuffer checkSumHolder) throws SapphireException {
        try (BufferedInputStream inputStream = new BufferedInputStream(sourceInputStream, 2048);){
            int count;
            boolean generateCheckSum = checkSumHolder != null;
            MessageDigest messageDigest = null;
            if (generateCheckSum) {
                messageDigest = MessageDigest.getInstance("MD5");
            }
            ZipEntry entry = new ZipEntry(zipEntryName);
            zipOutputStream.putNextEntry(entry);
            byte[] data = new byte[2048];
            while ((count = inputStream.read(data, 0, 2048)) != -1) {
                if (generateCheckSum) {
                    messageDigest.update(data, 0, count);
                }
                zipOutputStream.write(data, 0, count);
            }
            if (generateCheckSum) {
                byte[] cs = messageDigest.digest();
                for (int i = 0; i < cs.length; ++i) {
                    checkSumHolder.append(cs[i]).append(" ");
                }
            }
        }
        catch (IOException | NoSuchAlgorithmException e) {
            throw new SapphireException("Failed to add InputStream as Zip file entry. Reason: " + e.getMessage(), e);
        }
    }

    public static void addManifest(HashMap manifestAttributes, ZipOutputStream zipOutputStream) throws SapphireException {
        try {
            ZipEntry manifestFile = new ZipEntry("META-INF/MANIFEST.MF");
            zipOutputStream.putNextEntry(manifestFile);
            for (String name : manifestAttributes.keySet()) {
                zipOutputStream.write((name + ": " + manifestAttributes.get(name) + "\n").getBytes());
            }
        }
        catch (IOException e) {
            throw new SapphireException("Failed to add zip file manifest. Reason: " + e.getMessage(), e);
        }
    }

    public static HashMap getManifest(File extractFromFile) throws SapphireException {
        ZipFile zipFile = null;
        try {
            HashMap<String, String> manifestAttributes = new HashMap<String, String>();
            zipFile = new ZipFile(extractFromFile);
            ZipEntry zipEntry = zipFile.getEntry("META-INF/MANIFEST.MF");
            if (zipEntry != null) {
                String[] entries = StringUtil.split(new String(ZipFileUtil.extractByteArray(zipFile, zipEntry)), "\n");
                for (int i = 0; i < entries.length; ++i) {
                    if (entries[i].length() <= 0) continue;
                    int pos = entries[i].indexOf(":");
                    manifestAttributes.put(entries[i].substring(0, pos).trim(), entries[i].substring(pos + 1).trim());
                }
            }
            zipFile.close();
            HashMap<String, String> hashMap = manifestAttributes;
            return hashMap;
        }
        catch (IOException e) {
            throw new SapphireException("Failed to get zip file manifest. Reason: " + e.getMessage(), e);
        }
        finally {
            try {
                if (zipFile != null) {
                    zipFile.close();
                }
            }
            catch (IOException iOException) {}
        }
    }

    public static String extractString(ZipFile zipFile, ZipEntry zipEntry) throws SapphireException, IOException {
        return new String(ZipFileUtil.extractByteArray(zipFile, zipEntry), "UTF-8");
    }

    public static boolean isArchive(File f) throws SapphireException {
        int fileSignature = 0;
        try (RandomAccessFile raf = new RandomAccessFile(f, "r");){
            fileSignature = raf.readInt();
        }
        catch (IOException e) {
            throw new SapphireException(e);
        }
        return fileSignature == 1347093252 || fileSignature == 1347093766 || fileSignature == 1347094280;
    }
}

