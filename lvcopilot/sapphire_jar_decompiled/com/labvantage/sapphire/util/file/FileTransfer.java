/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.compress.archivers.ArchiveEntry
 *  org.apache.commons.compress.archivers.ArchiveStreamFactory
 *  org.apache.commons.compress.archivers.tar.TarArchiveEntry
 *  org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
 *  org.apache.commons.io.FileUtils
 *  org.apache.commons.io.FilenameUtils
 *  org.apache.commons.io.IOUtils
 */
package com.labvantage.sapphire.util.file;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.attachment.HashedAttachmentInputStream;
import com.labvantage.sapphire.modules.sdms.collector.SDMSCollector;
import com.labvantage.sapphire.util.file.FileTransferOptions;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.net.URI;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.file.CopyOption;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.Key;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.zip.CRC32;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import sapphire.SapphireException;
import sapphire.attachment.Attachment;
import sapphire.util.Logger;

public class FileTransfer {
    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static void safeFileTransfer(File source, File target, FileTransferOptions options) throws Exception {
        if (source == null) {
            throw new IOException("Source is not provided");
        }
        if (target == null) {
            throw new IOException("Destination is not provided");
        }
        if (options == null) {
            options = new FileTransferOptions();
        }
        if (source.exists() && source.canRead()) {
            boolean isSourceFile = source.isFile();
            boolean isSourceDirectory = source.isDirectory();
            boolean isSourceZip = "zip".equalsIgnoreCase(FilenameUtils.getExtension((String)source.getName()));
            boolean isTargerFile = target.isFile() || FilenameUtils.getExtension((String)target.getName()).length() > 0;
            boolean isTargetDirectory = target.isDirectory() || FilenameUtils.getExtension((String)target.getName()).length() == 0;
            boolean isTargetZip = "zip".equalsIgnoreCase(FilenameUtils.getExtension((String)target.getName()));
            boolean generateZip = isTargetZip && !isSourceZip;
            boolean isAppendToZip = false;
            if (target.exists() && isTargerFile) {
                if (options.replaceTarget()) {
                    FileTransfer.deleteTarget(target, options);
                } else if (!isTargetZip) {
                    throw new IOException("Destination already exists.");
                }
            }
            if (isSourceFile && isTargerFile) {
                if (generateZip) {
                    if (target.exists()) {
                        isAppendToZip = true;
                        FileTransfer.appendToZipFile(source, target);
                    } else {
                        FileTransfer.generateZip(source.toString(), target.toString(), options);
                    }
                } else {
                    if (options.validateHashValue() && options.getSourceHashValue() == 0L) {
                        options.setSourceHashValue(FileTransfer.generateHashValue(source.toString(), FileTransfer.getHashAlgorithm(options)));
                    }
                    if (options.validateFileSize()) {
                        options.setSourceFileSize(FileUtil.fileSize(source));
                    }
                    FileTransfer.doCopy(source, target, options.getCopyOption(), options);
                    if (options.validateFileSize()) {
                        long tofilesize = FileUtil.fileSize(target);
                        options.setTransferredFileSize(tofilesize);
                        if (options.getSourceFileSize() != tofilesize) {
                            throw new IOException("File corrupted, To and From File Sizes are different.");
                        }
                    }
                    if (options.validateHashValue()) {
                        long tohashValue = FileTransfer.generateHashValue(target.toString(), FileTransfer.getHashAlgorithm(options));
                        options.setTargetHashValue(tohashValue);
                        if (options.getSourceHashValue() != tohashValue) {
                            throw new IOException("File corrupted, To and From HashValues are different.");
                        }
                    }
                }
            } else if (isSourceDirectory && isTargetDirectory) {
                options.setSourceFileSize(FileUtil.fileSize(source));
                FileTransfer.copyDirectory(source, target, options);
                options.setTransferredFileSize(FileUtil.fileSize(target));
                if (options.validateFileSize() && options.getSourceFileSize() != options.getTransferredFileSize()) {
                    throw new IOException("Folder corrupted, To and From Folder Sizes are different.");
                }
            } else if (isSourceDirectory && isTargetZip) {
                FileTransfer.generateZip(source.toString(), target.toString(), options);
            } else if (isSourceZip && isTargetDirectory) {
                FileTransfer.extractZipFile(source, target);
            } else {
                if (!isSourceFile || !isTargetDirectory) throw new IOException("Unable to transfer source.");
                Path targetPath = target.toPath().resolve(source.getName());
                FileTransfer.safeFileTransfer(source, targetPath.toFile(), options);
            }
            if (!options.deleteSourceOnSuccessfullTransfer()) return;
            if (!isAppendToZip && !FileTransfer.isTransferredFileSafe(options, source, target)) throw new IOException("Transferred File corrupted, To and From File Sizes are different.");
            try {
                FileUtils.forceDelete((File)source);
                return;
            }
            catch (IOException e) {
                Trace.logDebug("Unable to delete using FileUtils.forceDelete:" + e.getMessage() + " --> File path:" + source.toString());
            }
            return;
        } else {
            if (!source.exists()) {
                throw new IOException("Source does not exist.");
            }
            if (source.canRead()) return;
            throw new IOException("Source is locked");
        }
    }

    public static void safeDataTransfer(InputStream source, File target, FileTransferOptions options) throws Exception {
        long targetHashValue;
        if (source == null) {
            throw new IOException("Source is not provided");
        }
        if (target == null) {
            throw new IOException("Destination is not provided");
        }
        if (options == null) {
            options = new FileTransferOptions();
        }
        if (target.exists()) {
            if (options.replaceTarget()) {
                FileTransfer.deleteTarget(target, options);
            } else {
                throw new IOException("Destination already exists.");
            }
        }
        if (options.encryptData()) {
            FileTransfer.encryptDecryptDataAndTransfer(source, target, 1, options);
        } else if (options.decryptData()) {
            FileTransfer.encryptDecryptDataAndTransfer(source, target, 2, options);
        } else {
            Files.copy(source, target.toPath(), new CopyOption[0]);
        }
        if (target.exists() && options.getSourceHashValue() > 0L && !options.encryptData() && !options.decryptData() && (targetHashValue = FileTransfer.generateHashValue(target.toString(), FileTransfer.getHashAlgorithm(options))) != options.getSourceHashValue()) {
            throw new IOException("Target file corrupted");
        }
        if (options.closeInputStream()) {
            source.close();
        }
    }

    public static void safeFileTransfer(File source, OutputStream target, FileTransferOptions options) throws Exception {
        if (source == null) {
            throw new IOException("Source is not provided");
        }
        if (target == null) {
            throw new IOException("Destination is not provided");
        }
        if (options == null) {
            options = new FileTransferOptions();
        }
        if (source.exists() && source.canRead()) {
            if (options.encryptData()) {
                FileTransfer.encryptDecryptDataAndTransfer(source, target, 1, options);
            } else if (options.decryptData()) {
                FileTransfer.encryptDecryptDataAndTransfer(source, target, 2, options);
            } else {
                Files.copy(source.toPath(), target);
            }
            if (options.validateHashValue() && options.getSourceHashValue() == 0L) {
                options.setSourceHashValue(FileTransfer.generateHashValue(source.toString(), FileTransfer.getHashAlgorithm(options)));
            }
            if (options.deleteSourceOnSuccessfullTransfer()) {
                try {
                    FileUtils.forceDelete((File)source);
                }
                catch (IOException e) {
                    throw new IOException("Unable to delete source:" + e.getMessage());
                }
            }
        } else {
            if (!source.exists()) {
                throw new IOException("Source does not exist.");
            }
            if (!source.canRead()) {
                throw new IOException("Source is locked");
            }
        }
        if (options.closeOutputStream()) {
            target.close();
        }
    }

    private static boolean isTransferredFileSafe(FileTransferOptions options, File source, File target) {
        boolean flag = true;
        String sourceFileType = FilenameUtils.getExtension((String)source.getName());
        String targetFileType = FilenameUtils.getExtension((String)target.getName());
        if (!options.validateFileSize() && !options.validateHashValue() && sourceFileType.equalsIgnoreCase(targetFileType)) {
            Trace.logDebug("Calculatng file size using FileUtil.fileSize...");
            try {
                if (FileUtil.fileSize(source) != FileUtil.fileSize(target)) {
                    flag = false;
                }
            }
            catch (Exception e) {
                Trace.logDebug("Failed to get size using FileUtil.fileSize:", e.getMessage());
            }
            Trace.logDebug("*****isTransferredFileSafe-->:" + flag);
        }
        return flag;
    }

    private static void doCopy(File source, File target, FileTransferOptions.CopyOptions option, FileTransferOptions options) throws Exception {
        if (options.isReturnHashValue()) {
            FileTransfer.copyFileAndHash(source, target, options);
        } else if (options.encryptData()) {
            FileTransfer.encryptDecryptDataAndTransfer(source, target, 1, options);
        } else if (options.decryptData()) {
            FileTransfer.encryptDecryptDataAndTransfer(source, target, 2, options);
        } else if (option == FileTransferOptions.CopyOptions.NIO_Channel_transferTO) {
            FileTransfer.copyFileUsingNIO_Channel_transferTO(source, target);
        } else if (option == FileTransferOptions.CopyOptions.NIO_Channel_transferFrom) {
            FileTransfer.copyFileUsingNIO_Channel_transferFrom(source, target);
        } else if (option == FileTransferOptions.CopyOptions.ApacheCommonsIO_FileUtils_CopyFile) {
            FileTransfer.copyFileUsingApacheCommonsIO_FileUtils_CopyFile(source, target);
        } else if (option == FileTransferOptions.CopyOptions.NIO_Files_copy) {
            FileTransfer.copyFileUsingNIO_Files_copy(source, target);
        } else if (option == FileTransferOptions.CopyOptions.FileInputStream) {
            FileTransfer.copyFileUsingFileInputStream(source, target);
        } else if (option == FileTransferOptions.CopyOptions.NIO_Files_Move) {
            FileTransfer.moveUsingNIO_Files_Move(source, target);
        } else if (option == FileTransferOptions.CopyOptions.IO_File_renameTO) {
            FileTransfer.moveUsingIO_File_renameTO(source, target);
        } else if (option == FileTransferOptions.CopyOptions.FileUtils_moveFile) {
            FileTransfer.moveUsingFileUtils_moveFile(source, target);
        } else {
            FileTransfer.copyFileUsingNIO_Channel_transferTO(source, target);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void encryptDecryptDataAndTransfer(File inFile, File outFile, int mode, FileTransferOptions options) throws Exception {
        FileInputStream inStream = new FileInputStream(inFile);
        FileOutputStream outStrem = new FileOutputStream(outFile);
        try {
            FileTransfer.encryptDecryptDataAndTransfer((InputStream)inStream, (OutputStream)outStrem, mode, options);
        }
        finally {
            ((InputStream)inStream).close();
            outStrem.close();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void encryptDecryptDataAndTransfer(File inFile, OutputStream outStrem, int mode, FileTransferOptions options) throws Exception {
        try (FileInputStream inStream = new FileInputStream(inFile);){
            FileTransfer.encryptDecryptDataAndTransfer((InputStream)inStream, outStrem, mode, options);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void encryptDecryptDataAndTransfer(InputStream inStream, File outFile, int mode, FileTransferOptions options) throws Exception {
        try (FileOutputStream outStrem = new FileOutputStream(outFile);){
            FileTransfer.encryptDecryptDataAndTransfer(inStream, (OutputStream)outStrem, mode, options);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void copyFileUsingNIO_Channel_transferTO(File source, File target) {
        try {
            FileChannel inChannel = new FileInputStream(source).getChannel();
            FileChannel outChannel = new FileOutputStream(target).getChannel();
            try {
                int maxCount = 0x4000000;
                long size = inChannel.size();
                for (long position = 0L; position < size; position += inChannel.transferTo(position, maxCount, outChannel)) {
                }
            }
            finally {
                if (inChannel != null) {
                    inChannel.close();
                }
                outChannel.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void copyFileUsingNIO_Channel_transferFrom(File source, File dest) throws IOException {
        FileChannel sourceChannel = null;
        AbstractInterruptibleChannel destChannel = null;
        try {
            sourceChannel = new FileInputStream(source).getChannel();
            destChannel = new FileOutputStream(dest).getChannel();
            ((FileChannel)destChannel).transferFrom(sourceChannel, 0L, sourceChannel.size());
        }
        catch (IOException ioe) {
            Trace.logError("Failed to copyFile Using NIO_Channel_transferFrom", ioe.getMessage());
        }
        finally {
            if (sourceChannel != null) {
                sourceChannel.close();
            }
            if (destChannel != null) {
                destChannel.close();
            }
        }
    }

    public static void copyFileUsingApacheCommonsIO_FileUtils_CopyFile(File source, File dest) throws IOException {
        try {
            FileUtils.copyFile((File)source, (File)dest);
        }
        catch (IOException ioe) {
            throw new IOException("Failed to copy:" + ioe.getMessage());
        }
    }

    public static void copyFileUsingNIO_Files_copy(File source, File dest) throws IOException {
        try {
            Files.copy(Paths.get(source.toString(), new String[0]), Paths.get(dest.toString(), new String[0]), new CopyOption[0]);
        }
        catch (IOException ioe) {
            throw new IOException("Failed to copy:" + ioe.getMessage());
        }
    }

    public static void copyFileUsingFileInputStream(File source, File dest) throws IOException {
        FileInputStream is = null;
        OutputStream os = null;
        try {
            int length;
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[8192];
            while ((length = ((InputStream)is).read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
        catch (IOException ioe) {
            throw new IOException("Failed to copy:" + ioe.getMessage());
        }
        finally {
            if (is != null) {
                ((InputStream)is).close();
            }
            if (os != null) {
                os.close();
            }
        }
    }

    public static void copyFileAndHash(File source, File dest, FileTransferOptions options) throws Exception {
        InputStream is = null;
        OutputStream os = null;
        MessageDigest messageDigest = null;
        try {
            int length;
            messageDigest = MessageDigest.getInstance(FileTransfer.getHashAlgorithmName(SDMSCollector.defaultHashingAlgorithm));
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[options.getBufferSize()];
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
                messageDigest.update(buffer, 0, length);
            }
        }
        catch (Exception ioe) {
            throw new Exception("Failed to copy:" + ioe.getMessage());
        }
        finally {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
        }
        if (messageDigest != null) {
            byte[] checksum = messageDigest.digest();
            BigInteger bigInt = new BigInteger(1, checksum);
            options.setSourceHashValue(bigInt.longValue());
        }
    }

    public static void moveUsingNIO_Files_Move(File source, File target) throws IOException {
        Files.move(source.toPath(), target.toPath(), new CopyOption[0]);
    }

    public static void moveUsingIO_File_renameTO(File source, File target) throws IOException {
        if (!source.renameTo(target)) {
            throw new IOException("Failed to Move");
        }
    }

    public static void moveUsingFileUtils_moveFile(File source, File target) throws IOException {
        FileUtils.moveFile((File)source, (File)target);
    }

    public static void zipFileAndMove(String source, String target) throws IOException {
        File zipFle = new File("temp.zip");
        File sourceFile = new File(source);
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFle));){
            zos.putNextEntry(new ZipEntry(sourceFile.getName()));
            Files.copy(sourceFile.toPath(), zos);
        }
        Files.move(zipFle.toPath(), new File(target).toPath(), new CopyOption[0]);
    }

    private static void deleteTarget(File target, FileTransferOptions options) throws Exception {
        int deleteAttemp = options.getForceDeleteTargetRetryCount();
        boolean deleteSuccessfull = false;
        while (!deleteSuccessfull && deleteAttemp > 0) {
            try {
                deleteSuccessfull = target.delete();
                if (deleteSuccessfull || --deleteAttemp <= 0) continue;
                Thread.sleep(100L);
            }
            catch (Exception e) {
                throw new IOException("Unable to delete source:" + e.getMessage());
            }
        }
        if (!deleteSuccessfull) {
            throw new IOException("Destination already exists and cannot be deleted in " + options.getForceDeleteTargetRetryCount() + "  attemp(s).");
        }
    }

    public static void appendToZipFile(File source, File target) throws IOException {
        HashMap<String, String> env = new HashMap<String, String>();
        env.put("create", "false");
        URI uri = URI.create("jar:" + Paths.get(target.toString(), new String[0]).toUri());
        try (FileSystem zipfs = FileSystems.newFileSystem(uri, env);){
            Path externalTxtFile = Paths.get(source.toString(), new String[0]);
            Path pathInZipfile = zipfs.getPath(source.getName(), new String[0]);
            Files.copy(externalTxtFile, pathInZipfile, new CopyOption[0]);
        }
    }

    public static long generateCheckSumUsingCRC32(InputStream inputStream) throws Exception {
        CRC32 crc = new CRC32();
        byte[] buffer = new byte[8192];
        int read = 0;
        while ((read = inputStream.read(buffer)) > 0) {
            crc.update(buffer, 0, read);
        }
        Logger.logDebug("CRC32: " + crc.getValue());
        return crc.getValue();
    }

    public static long generateCheckSumUsingCRC32(String sourceFile) throws Exception {
        FileInputStream inputStream = new FileInputStream(new File(sourceFile));
        long value = FileTransfer.generateCheckSumUsingCRC32(inputStream);
        ((InputStream)inputStream).close();
        return value;
    }

    public static long checksumMappedFile(String filepath) throws IOException {
        FileInputStream inputStream = new FileInputStream(filepath);
        FileChannel fileChannel = inputStream.getChannel();
        int len = (int)fileChannel.size();
        MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0L, len);
        CRC32 crc = new CRC32();
        for (int cnt = 0; cnt < len; ++cnt) {
            byte i = buffer.get(cnt);
            crc.update(i);
        }
        return crc.getValue();
    }

    public static long checksumBufferedInputStream(String filepath) throws IOException {
        int cnt;
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(filepath));
        CRC32 crc = new CRC32();
        while ((cnt = ((InputStream)inputStream).read()) != -1) {
            crc.update(cnt);
        }
        return crc.getValue();
    }

    public static String getHashAlgorithmName(FileTransferOptions.HashingAlgorithm option) {
        String name = "MD5";
        if (option == FileTransferOptions.HashingAlgorithm.SHA1) {
            name = "SHA-1";
        } else if (option == FileTransferOptions.HashingAlgorithm.SHA256) {
            name = "SHA-256";
        } else if (option == FileTransferOptions.HashingAlgorithm.SHA384) {
            name = "SHA-384";
        } else if (option == FileTransferOptions.HashingAlgorithm.SHA512) {
            name = "SHA-512";
        } else if (option == FileTransferOptions.HashingAlgorithm.CRC32) {
            name = "CRC32";
        } else if (option == FileTransferOptions.HashingAlgorithm.MD5) {
            name = "MD5";
        }
        return name;
    }

    public static long generateCheckSum(InputStream inputStream) throws Exception {
        return FileTransfer.generateCheckSum(inputStream, SDMSCollector.defaultHashingAlgorithm);
    }

    public static long generateCheckSum(InputStream inputStream, FileTransferOptions.HashingAlgorithm option) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(FileTransfer.getHashAlgorithmName(option));
        byte[] buffer = new byte[8192];
        int read = 0;
        try {
            while ((read = inputStream.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] checksum = digest.digest();
            BigInteger bigInt = new BigInteger(1, checksum);
            Logger.logDebug(FileTransfer.getHashAlgorithmName(option) + ": " + bigInt.longValue());
            return bigInt.longValue();
        }
        catch (IOException e) {
            throw new RuntimeException("Unable to process file for " + FileTransfer.getHashAlgorithmName(option), e);
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
                boolean isDirectory = false;
                try {
                    isDirectory = outFilename.endsWith("\\") || outFilename.endsWith("/") || entry.isDirectory();
                }
                catch (Exception exception) {
                    // empty catch block
                }
                if (isDirectory) {
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

    public static void extractZipFile(File file, File destination) throws IOException {
        if (!file.isAbsolute()) {
            file = file.toPath().toAbsolutePath().toFile();
        }
        try (FileInputStream in = new FileInputStream(file);){
            FileTransfer.extractZipStream(in, destination);
        }
    }

    public static DigestInputStream getHashedInputSteam(InputStream in, FileTransferOptions.HashingAlgorithm option) throws SapphireException {
        try {
            MessageDigest digest = MessageDigest.getInstance(FileTransfer.getHashAlgorithmName(option));
            return new DigestInputStream(in, digest);
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    public static HashedAttachmentInputStream getHashedInputSteam(Attachment attachment, FileTransferOptions.HashingAlgorithm option) throws SapphireException {
        try {
            MessageDigest digest = MessageDigest.getInstance(FileTransfer.getHashAlgorithmName(option));
            return new HashedAttachmentInputStream(attachment, digest);
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    public static InputStream getCompressedInputStream(final InputStream in) throws SapphireException {
        PipedInputStream zipped = new PipedInputStream();
        try {
            final PipedOutputStream pipe = new PipedOutputStream(zipped);
            Thread t = new Thread(new Runnable(){

                @Override
                public void run() {
                    FileTransferOptions fto = new FileTransferOptions();
                    fto.setCloseInputStream(false);
                    fto.setCloseOutputStream(false);
                    try (GZIPOutputStream zipper = new GZIPOutputStream(pipe);){
                        FileTransfer.safeDataTransfer(in, zipper, fto);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
            for (int count = 0; zipped.available() == 0 && count < 1000; ++count) {
                Thread.sleep(100L);
            }
            return zipped;
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    public static InputStream getUncompressedInputStream(InputStream in) throws SapphireException {
        try {
            GZIPInputStream zippedInput = new GZIPInputStream(in);
            return zippedInput;
        }
        catch (IOException e) {
            throw new SapphireException(e);
        }
    }

    private static FileTransferOptions.HashingAlgorithm getHashAlgorithm(FileTransferOptions options) {
        FileTransferOptions.HashingAlgorithm algo = options.getHashingAlgorithm();
        if (algo != FileTransferOptions.HashingAlgorithm.None) {
            return algo;
        }
        String encryptionAlgorithm = options.getEncryptDecryptAlgorithm();
        if (encryptionAlgorithm.length() > 0) {
            for (FileTransferOptions.HashingAlgorithm encryptedAlgo : FileTransferOptions.HashingAlgorithm.values()) {
                if (!encryptionAlgorithm.contains(FileTransfer.getHashAlgorithmName(encryptedAlgo))) continue;
                return encryptedAlgo;
            }
        }
        return options.getDefaultHashingAlgorithm();
    }

    private static String getEncryptDecryptAlgorithm(FileTransferOptions options) {
        String encryptionAlgorithm = options.getEncryptDecryptAlgorithm();
        if (encryptionAlgorithm.length() > 0) {
            return encryptionAlgorithm;
        }
        return options.getDefaultEncryptDecryptAlgorithm();
    }

    public static CipherInputStream getCipherInputStream(InputStream inputStream, boolean decrypt) throws SapphireException {
        return FileTransfer.getCipherInputStream(inputStream, null, null, decrypt);
    }

    public static CipherInputStream getCipherInputStream(final InputStream inputStream, String password, String algorithm, boolean decrypt) throws SapphireException {
        FileTransferOptions fto = new FileTransferOptions();
        if (algorithm == null || algorithm.length() == 0) {
            algorithm = fto.getDefaultEncryptDecryptAlgorithm();
        }
        if (password == null || password.length() == 0) {
            password = fto.getEncryptDecryptPassword();
        }
        byte[] salt = new byte[8];
        PBEParameterSpec defParams = new PBEParameterSpec(salt, 99);
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
        try {
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(algorithm);
            SecretKey secretKey = secretKeyFactory.generateSecret(pbeKeySpec);
            Cipher cipher = Cipher.getInstance(algorithm);
            if (decrypt) {
                cipher.init(2, (Key)secretKey, defParams);
            } else {
                cipher.init(1, (Key)secretKey, defParams);
            }
            return new CipherInputStream(inputStream, cipher){

                @Override
                public int available() throws IOException {
                    return inputStream.available();
                }
            };
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    public static void encryptDecryptDataAndTransfer(InputStream inputStream, OutputStream outputStream, int mode, FileTransferOptions options) throws Exception {
        byte[] output;
        int bytesRead;
        if (mode != 1 && mode != 2) {
            throw new Exception("Mode should be either Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE");
        }
        String password = options.getEncryptDecryptPassword();
        String algorithm = FileTransfer.getEncryptDecryptAlgorithm(options);
        byte[] salt = new byte[8];
        if (mode == 1) {
            byte[] byteArray = IOUtils.toByteArray((InputStream)inputStream);
            ByteArrayInputStream inputStream1 = new ByteArrayInputStream(byteArray);
            inputStream = new ByteArrayInputStream(byteArray);
            FileTransfer.setHashValue(inputStream1, options);
            ((InputStream)inputStream1).close();
            Random random = new Random();
            random.nextBytes(salt);
            outputStream.write(salt);
        } else {
            inputStream.read(salt);
        }
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(algorithm);
        SecretKey secretKey = secretKeyFactory.generateSecret(pbeKeySpec);
        PBEParameterSpec pbeParameterSpec = new PBEParameterSpec(salt, 100);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(mode, (Key)secretKey, pbeParameterSpec);
        byte[] input = new byte[1024];
        while ((bytesRead = inputStream.read(input)) != -1) {
            output = cipher.update(input, 0, bytesRead);
            if (output == null) continue;
            outputStream.write(output);
        }
        output = cipher.doFinal();
        if (output != null) {
            outputStream.write(output);
        }
    }

    public static void safeDataTransfer(InputStream source, OutputStream target, FileTransferOptions options) throws Exception {
        if (source == null) {
            throw new IOException("Source is not provided");
        }
        if (target == null) {
            throw new IOException("Destination is not provided");
        }
        if (options == null) {
            options = new FileTransferOptions();
        }
        if (options.encryptData()) {
            FileTransfer.encryptDecryptDataAndTransfer(source, target, 1, options);
        } else if (options.decryptData()) {
            FileTransfer.encryptDecryptDataAndTransfer(source, target, 2, options);
        } else {
            IOUtils.copy((InputStream)source, (OutputStream)target);
        }
        if (options.closeInputStream()) {
            source.close();
        }
        if (options.closeOutputStream()) {
            target.close();
        }
    }

    public static long generateHashValue(InputStream inputStream, FileTransferOptions.HashingAlgorithm option) throws Exception {
        long value = 0L;
        value = option == FileTransferOptions.HashingAlgorithm.MD5 || option == FileTransferOptions.HashingAlgorithm.SHA1 || option == FileTransferOptions.HashingAlgorithm.SHA256 || option == FileTransferOptions.HashingAlgorithm.SHA384 || option == FileTransferOptions.HashingAlgorithm.SHA512 ? FileTransfer.generateCheckSum(inputStream, option) : (option == FileTransferOptions.HashingAlgorithm.CRC32 ? FileTransfer.generateCheckSumUsingCRC32(inputStream) : FileTransfer.generateCheckSum(inputStream, FileTransferOptions.HashingAlgorithm.MD5));
        return value;
    }

    public static long generateHashValue(String file, FileTransferOptions.HashingAlgorithm option) throws Exception {
        FileInputStream inputStream = new FileInputStream(file);
        long value = FileTransfer.generateHashValue(inputStream, option);
        ((InputStream)inputStream).close();
        return value;
    }

    private static void setHashValue(InputStream inputStream, FileTransferOptions options) throws Exception {
        if (options.getSourceHashValue() == 0L) {
            FileTransferOptions.HashingAlgorithm algorithm = FileTransfer.getHashAlgorithm(options);
            options.setSourceHashValue(FileTransfer.generateHashValue(inputStream, algorithm));
        }
    }

    public static void copyDirectory(File source, File target, final FileTransferOptions options) throws IOException {
        FileUtils.copyDirectory((File)source, (File)target, (FileFilter)new FileFilter(){

            @Override
            public boolean accept(File pathname) {
                return FileTransfer.isAcceptableFile(pathname.getName(), options);
            }
        });
    }

    public static void generateZip(String inputFolder, String targetZipp, FileTransferOptions options) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(targetZipp);
        ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
        File inputFile = new File(inputFolder);
        if (inputFile.isFile()) {
            FileTransfer.zipFile(inputFile, "", zipOutputStream, options);
        } else if (inputFile.isDirectory()) {
            FileTransfer.zipFolder(zipOutputStream, inputFile, "", options, true);
        }
        zipOutputStream.close();
    }

    private static void zipFolder(ZipOutputStream zipOutputStream, File inputFolder, String parentName, FileTransferOptions options, boolean ignoreRootFolder) throws IOException {
        File[] contents;
        String myname;
        String string = myname = ignoreRootFolder ? "" : parentName + inputFolder.getName() + "/";
        if (!options.isFlattenFileStructure() && myname.length() > 0) {
            ZipEntry folderZipEntry = new ZipEntry(myname);
            zipOutputStream.putNextEntry(folderZipEntry);
        }
        if ((contents = inputFolder.listFiles()) != null) {
            for (File f : contents) {
                if (f.isFile()) {
                    FileTransfer.zipFile(f, myname, zipOutputStream, options);
                    continue;
                }
                if (!f.isDirectory() || !options.includeSubFolder()) continue;
                FileTransfer.zipFolder(zipOutputStream, f, myname, options, false);
            }
        }
        zipOutputStream.closeEntry();
    }

    private static void zipFile(File inputFile, String parentName, ZipOutputStream zipOutputStream, FileTransferOptions options) throws IOException {
        String inputFileName = inputFile.getName();
        String zipItem = parentName + inputFileName;
        if (FileTransfer.isAcceptableFile(inputFileName, options)) {
            if (options.isFlattenFileStructure()) {
                zipItem = options.getLatestFileName(inputFileName);
            }
            ZipEntry zipEntry = new ZipEntry(zipItem);
            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.setLevel(options.getCompressionLevel());
            try {
                Files.copy(inputFile.toPath(), zipOutputStream);
            }
            catch (IOException e) {
                throw new IOException("Failed to zip:" + e.getMessage());
            }
            finally {
                zipOutputStream.closeEntry();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void generateTarGZip(File originFileOrFolder, File destinationFile, FileTransferOptions zipCopyOptions) throws FileNotFoundException, IOException {
        try (FileOutputStream fos = new FileOutputStream(destinationFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             TarArchiveOutputStream taos = (TarArchiveOutputStream)new ArchiveStreamFactory().createArchiveOutputStream("tar", (OutputStream)bos);){
            taos.setLongFileMode(2);
            FileTransfer.addFileOrFolderToTarGz(taos, originFileOrFolder.getCanonicalPath(), "");
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static void addFileOrFolderToTarGz(TarArchiveOutputStream taos, String path, String base) throws IOException {
        File f = new File(path);
        String entryName = base + f.getName();
        TarArchiveEntry tarEntry = new TarArchiveEntry(f, entryName);
        taos.putArchiveEntry((ArchiveEntry)tarEntry);
        if (f.isFile()) {
            FileInputStream fin = new FileInputStream(f);
            IOUtils.copy((InputStream)fin, (OutputStream)taos);
            fin.close();
            taos.closeArchiveEntry();
        } else {
            taos.closeArchiveEntry();
            File[] children = f.listFiles();
            if (children != null) {
                for (File child : children) {
                    FileTransfer.addFileOrFolderToTarGz(taos, child.getAbsolutePath(), entryName + "/");
                }
            }
        }
    }

    public static boolean isAcceptableFile(String filename, FileTransferOptions options) {
        boolean valid = true;
        if (filename != null && filename.contains(".")) {
            List excludeTypeList;
            List includeTypeList = options.getFileIncludeList();
            if (includeTypeList.size() > 0) {
                valid = FileTransfer.matchFileName(includeTypeList, filename);
            }
            if ((excludeTypeList = options.getFileExcludeList()).size() > 0 && FileTransfer.matchFileName(excludeTypeList, filename)) {
                valid = false;
            }
        }
        return valid;
    }

    private static boolean matchFileName(List list, String name) {
        boolean valid = false;
        for (Object item : list) {
            String pattern = (String)item;
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
            if (!matcher.matches(Paths.get(name, new String[0]))) continue;
            valid = true;
            break;
        }
        return valid;
    }

    public static boolean isLocked(List<Path> paths) {
        boolean locked = false;
        try {
            for (int i = 0; i < paths.size() && !(locked = FileTransfer.isLocked(paths.get(i).toFile())); ++i) {
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return locked;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static boolean isLocked(File lockFile) throws Exception {
        boolean locked = false;
        if (lockFile.isDirectory()) {
            File[] dirFiles = lockFile.listFiles();
            for (int d = 0; d < dirFiles.length && !(locked = FileTransfer.isLocked(dirFiles[d])); ++d) {
            }
            return locked;
        } else {
            FileChannel fileChannel = null;
            FileLock lock = null;
            RandomAccessFile file = null;
            try {
                file = new RandomAccessFile(lockFile, "rw");
                fileChannel = file.getChannel();
                lock = fileChannel.tryLock();
                if (lock == null || !lock.isValid()) {
                    locked = true;
                    return locked;
                }
                if (lockFile.toString().endsWith("filepart")) {
                    locked = true;
                    return locked;
                }
                long initialFileLength = lockFile.length();
                Thread.sleep(3000L);
                long finalFileLength = lockFile.length();
                if (finalFileLength - initialFileLength <= 0L) return locked;
                locked = true;
                return locked;
            }
            catch (Exception e) {
                locked = true;
                return locked;
            }
            finally {
                if (lock != null && lock.isValid()) {
                    lock.release();
                }
                if (file != null) {
                    file.close();
                }
            }
        }
    }
}

