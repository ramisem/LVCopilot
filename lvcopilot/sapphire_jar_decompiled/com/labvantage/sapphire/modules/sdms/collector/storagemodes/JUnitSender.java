/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.collector.storagemodes;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.modules.sdms.collector.SDMSCollector;
import com.labvantage.sapphire.modules.sdms.collector.collectortypes.BaseCollectorType;
import com.labvantage.sapphire.modules.sdms.collector.storagemodes.BaseFileSender;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class JUnitSender
extends BaseFileSender {
    List<Path> files = new ArrayList<Path>();

    public JUnitSender(SDMSCollector sdmsCollector, BaseCollectorType collectorType) {
        super(sdmsCollector, collectorType);
    }

    @Override
    public String init(Calendar lastTriggerDt, PropertyList captureMetaData) throws SapphireException {
        String sendId = super.init(lastTriggerDt, captureMetaData);
        this.files = new ArrayList<Path>();
        return sendId;
    }

    @Override
    public String store(String sendId, Path file, BaseFileSender.ActionOnOrginal processOriginalMode, String attachmentClass, boolean byReference, Path archiveFolder, String archiveFolderAsSeenFromLIMS, PropertyList fileMetaData) throws SapphireException {
        this.files.add(file);
        System.out.println("Storing " + file.toFile().getAbsolutePath());
        return "Added file";
    }

    @Override
    public String zipAndStore(String sendId, Path zipFile, BaseFileSender.ActionOnOrginal processOriginalMode, String attachmentClass, boolean byReference, Path archiveFolder, String archiveFolderAsSeenFromLIMS, PropertyList fileMetaData, List<Path> originalFiles) throws SapphireException {
        if (processOriginalMode == BaseFileSender.ActionOnOrginal.DELETE) {
            if (FileUtil.deleteFiles(originalFiles)) {
                this.appendCollectorLog("Original Files have been deleted");
            } else {
                this.appendCollectorLog("Failed to delete original file(s).");
            }
        }
        return this.store(sendId, zipFile, processOriginalMode, attachmentClass, byReference, archiveFolder, archiveFolderAsSeenFromLIMS, fileMetaData);
    }

    @Override
    public void complete(String sendId) throws SapphireException {
    }

    public List<Path> getFiles() {
        return this.files;
    }

    public int getFileCount() {
        return this.files.size();
    }

    public boolean containsFilename(String fileName) {
        return this.containsFilename(fileName, true);
    }

    public boolean containsFilename(String fileName, boolean matchPath) {
        System.out.println("Filename to match: " + fileName);
        if (matchPath) {
            for (Path p : this.files) {
                System.out.println("Filename found: " + p.toFile().getName());
                if (!p.toFile().getName().equalsIgnoreCase(fileName)) continue;
                return true;
            }
        } else {
            for (Path p : this.files) {
                System.out.println("Filename found: " + p.toFile().getName());
                if (!p.toFile().getName().toLowerCase().endsWith(fileName.toLowerCase())) continue;
                return true;
            }
        }
        return false;
    }
}

