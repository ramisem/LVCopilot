/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.install;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.util.file.ZipFileListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.jar.Attributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import sapphire.SapphireException;

public class PackagerFile {
    private int bufferSize = 2048;
    private File packagerFile;
    private ArrayList<File> files = new ArrayList();
    private ArrayList<String> filesPrefix = new ArrayList();
    private ArrayList<File> filesRootDir = new ArrayList();
    private ArrayList<String> filesFilters = new ArrayList();
    private ArrayList<File> packagerEntryFiles = new ArrayList();
    private ArrayList<String> packagerEntries = new ArrayList();
    private HashSet exclusions = new HashSet();
    private HashMap manifestAttributes = new HashMap();

    public PackagerFile(File packagerFile) {
        this.packagerFile = packagerFile;
        this.addExclusion("CVS");
        this.addExclusion(".cvsignore");
        this.addExclusion("vssver.scc");
    }

    public File getFile() {
        return this.packagerFile;
    }

    public void addExclusion(String exclusion) {
        this.exclusions.add(exclusion);
    }

    public void addManifestAttribute(String name, String value) {
        this.manifestAttributes.put(name, value);
    }

    public void addFile(File rootDir, File file) {
        this.addFile("", rootDir, file, "*");
    }

    public void addFile(String entryPrefix, File rootDir, File file) {
        this.addFile(entryPrefix, rootDir, file, "*");
    }

    public void addFile(File rootDir, File file, String filter) {
        this.addFile("", rootDir, file, "*");
    }

    public void addFile(String entryPrefix, File rootDir, File file, String filter) {
        this.filesPrefix.add(entryPrefix);
        this.files.add(file);
        this.filesRootDir.add(rootDir);
        this.filesFilters.add(filter);
    }

    public void addPackagerEntry(File packagerFile, String entry) throws SapphireException {
        this.packagerEntryFiles.add(packagerFile);
        this.packagerEntries.add(entry);
    }

    public int fileCount() throws IOException {
        int count = 0;
        for (int i = 0; i < this.files.size(); ++i) {
            count += FileUtil.fileCount(this.files.get(i));
        }
        return count;
    }

    public void save() throws SapphireException {
        this.save(null);
    }

    public void save(ZipFileListener listener) throws SapphireException {
        FileOutputStream dest = null;
        ZipOutputStream zipOutputStream = null;
        boolean ignoreFinallyErrors = false;
        try {
            int i;
            dest = new FileOutputStream(this.packagerFile);
            zipOutputStream = new ZipOutputStream(new BufferedOutputStream(dest));
            for (i = 0; i < this.files.size(); ++i) {
                String prefix = this.filesPrefix.get(i);
                File file = this.files.get(i);
                File rootDir = this.filesRootDir.get(i);
                String filter = this.filesFilters.get(i);
                if (file.exists()) {
                    if (file.isFile()) {
                        this.zipFile(prefix, rootDir, file, filter, zipOutputStream, listener);
                        continue;
                    }
                    this.zipDir(prefix, rootDir, file, filter, zipOutputStream, listener);
                    continue;
                }
                throw new IOException("File not found - '" + file.getAbsolutePath() + "'");
            }
            for (i = 0; i < this.packagerEntryFiles.size(); ++i) {
                File packagerFile = this.packagerEntryFiles.get(i);
                String entry = this.packagerEntries.get(i);
                this.zipToZip(packagerFile, entry, zipOutputStream, listener);
            }
            if (this.manifestAttributes.size() > 0) {
                this.addManifestAttribute(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
                ZipEntry manifestFile = new ZipEntry("META-INF/MANIFEST.MF");
                zipOutputStream.putNextEntry(manifestFile);
                for (String name : this.manifestAttributes.keySet()) {
                    StringBuffer line = new StringBuffer(name + ": " + this.manifestAttributes.get(name) + "\n");
                    int length = line.length();
                    if (length > 72) {
                        int index = 70;
                        while (index < length - 2) {
                            line.insert(index, "\r\n ");
                            index += 72;
                            length += 3;
                        }
                    }
                    zipOutputStream.write(line.toString().getBytes());
                }
            }
            if (listener != null) {
                listener.fileExtracted(null, 100);
            }
        }
        catch (IOException e) {
            ignoreFinallyErrors = true;
            throw new SapphireException("Failed to save zip '" + this.packagerFile + "'", e);
        }
        finally {
            block21: {
                try {
                    if (zipOutputStream != null) {
                        zipOutputStream.close();
                    }
                    if (dest != null) {
                        dest.close();
                    }
                }
                catch (IOException e) {
                    if (ignoreFinallyErrors) break block21;
                    throw new SapphireException("Failed to close output stream.", e);
                }
            }
        }
    }

    private void zipDir(String prefix, File rootDir, File dir, String filter, ZipOutputStream zipOutputStream, ZipFileListener listener) throws IOException {
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; ++i) {
            if (this.exclusions.contains(files[i].getName())) continue;
            if (files[i].isFile()) {
                this.zipFile(prefix, rootDir, files[i], filter, zipOutputStream, listener);
                continue;
            }
            this.zipDir(prefix, rootDir, files[i], filter, zipOutputStream, listener);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void zipFile(String prefix, File rootDir, File file, String filter, ZipOutputStream zipOutputStream, ZipFileListener listener) throws IOException {
        if (!this.exclusions.contains(file.getName()) && FileUtil.wildcardMatch(file.getName(), filter, false)) {
            FileInputStream fis = null;
            BufferedInputStream inputStream = null;
            try {
                int count;
                fis = new FileInputStream(file);
                inputStream = new BufferedInputStream(fis, this.bufferSize);
                boolean corrected = false;
                if (rootDir.toString().endsWith(":")) {
                    rootDir = new File(rootDir, "/");
                    corrected = true;
                } else if (rootDir.toString().endsWith(File.separator)) {
                    corrected = true;
                }
                ZipEntry entry = new ZipEntry((prefix.length() > 0 ? prefix + "/" : "") + file.getAbsolutePath().substring(rootDir.getAbsolutePath().length() + (corrected ? 0 : 1)).replace(File.separatorChar, '/'));
                zipOutputStream.putNextEntry(entry);
                byte[] data = new byte[this.bufferSize];
                while ((count = inputStream.read(data, 0, this.bufferSize)) != -1) {
                    zipOutputStream.write(data, 0, count);
                }
                if (listener != null) {
                    listener.fileAdded(entry);
                }
            }
            finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (fis != null) {
                    fis.close();
                }
            }
        }
    }

    private void zipToZip(File from, String entry, ZipOutputStream zipOutputStream, ZipFileListener listener) throws IOException {
        ZipFile zipFile = new ZipFile(from);
        if (entry.endsWith("*")) {
            Enumeration<? extends ZipEntry> e = zipFile.entries();
            while (e.hasMoreElements()) {
                ZipEntry zipEntry = e.nextElement();
                if (!zipEntry.getName().startsWith(entry.substring(0, entry.length() - 1))) continue;
                this.copyZipEntry(zipFile, zipEntry, zipOutputStream, listener);
            }
        } else {
            ZipEntry zipEntry = zipFile.getEntry(entry);
            if (zipEntry != null) {
                this.copyZipEntry(zipFile, zipEntry, zipOutputStream, listener);
            }
        }
        zipFile.close();
    }

    private void copyZipEntry(ZipFile zipFile, ZipEntry zipEntry, ZipOutputStream zipOutputStream, ZipFileListener listener) throws IOException {
        int count;
        ZipEntry entry = new ZipEntry(zipEntry.getName());
        zipOutputStream.putNextEntry(entry);
        BufferedInputStream inputStream = new BufferedInputStream(zipFile.getInputStream(zipEntry));
        byte[] data = new byte[this.bufferSize];
        while ((count = inputStream.read(data, 0, this.bufferSize)) != -1) {
            zipOutputStream.write(data, 0, count);
        }
        inputStream.close();
        if (listener != null) {
            listener.fileAdded(entry);
        }
    }
}

