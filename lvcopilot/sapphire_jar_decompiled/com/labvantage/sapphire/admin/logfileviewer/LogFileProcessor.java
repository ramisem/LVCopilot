/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.logfileviewer;

import com.labvantage.sapphire.admin.logfileviewer.LogViewerFile;
import com.labvantage.sapphire.admin.logfileviewer.LogViewerUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public abstract class LogFileProcessor {
    public abstract void processFile(LogViewerFile var1, BufferedReader var2) throws IOException;

    public boolean processFile(LogViewerFile logViewerFile) {
        return true;
    }

    public static void processFiles(String connectionid, String snapshotFilename, LogFileProcessor processor) throws SapphireException, IOException {
        String snapshotFolderName = LogViewerUtil.getSnapshotFolderName(connectionid, snapshotFilename);
        PropertyList manifest = LogViewerUtil.getManifestPropertyList(snapshotFolderName);
        PropertyListCollection filelistCollection = manifest.getCollectionNotNull("filelist");
        for (int i = 0; i < filelistCollection.size(); ++i) {
            PropertyList filepl = filelistCollection.getPropertyList(i);
            LogViewerFile logViewerFile = new LogViewerFile();
            logViewerFile.filename = filepl.getProperty("filename");
            logViewerFile.filenumber = i;
            logViewerFile.filecount = filelistCollection.size();
            if (filepl.getProperty("starttotalrow").length() > 0) {
                logViewerFile.startTotalRow = Integer.parseInt(filepl.getProperty("starttotalrow"));
                logViewerFile.endTotalRow = Integer.parseInt(filepl.getProperty("endtotalrow"));
            }
            File file = new File(snapshotFolderName + "/" + logViewerFile.filename);
            logViewerFile.size = file.length();
            if (!processor.processFile(logViewerFile)) continue;
            FileInputStream fis = null;
            InputStreamReader isr = null;
            BufferedReader in = null;
            String line = "";
            try {
                fis = new FileInputStream(file);
                isr = new InputStreamReader((InputStream)fis, "UTF-8");
                in = new BufferedReader(isr);
                processor.processFile(logViewerFile, in);
                continue;
            }
            catch (Exception e) {
                throw new SapphireException("Error on " + line + " " + e.getMessage(), e);
            }
            finally {
                if (in != null) {
                    in.close();
                }
                if (isr != null) {
                    isr.close();
                }
                if (fis != null) {
                    fis.close();
                }
            }
        }
    }
}

