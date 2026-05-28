/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.collector.storagemodes;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.modules.sdms.SDMSUtil;
import com.labvantage.sapphire.modules.sdms.collector.SDMSCollector;
import com.labvantage.sapphire.modules.sdms.collector.collectortypes.BaseCollectorType;
import com.labvantage.sapphire.modules.sdms.collector.storagemodes.BaseFileSender;
import com.labvantage.sapphire.util.file.FileTransfer;
import com.labvantage.sapphire.util.file.FileTransferOptions;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class SendIndirect
extends BaseFileSender
implements SDMSConstants {
    Path targetFolder;

    public SendIndirect(SDMSCollector sdmsCollector, BaseCollectorType collectorType) {
        super(sdmsCollector, collectorType);
    }

    @Override
    public String init(Calendar lastTriggerDt, PropertyList captureMetaData) throws SapphireException {
        String sendId = super.init(lastTriggerDt, captureMetaData);
        this.getMetrics().startSenderTimer();
        try {
            Path collectorLocalStorageForInstrument = this.getSdmsCollector().getStoragePathLocal();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
            String foldername = this.getInstrumentid().replaceAll("[:\\\\/*?|<>\"]", "_") + "-" + sdf.format(lastTriggerDt.getTime());
            this.targetFolder = null;
            try {
                this.targetFolder = SDMSUtil.createWorkingFolder(collectorLocalStorageForInstrument, foldername, "_capturing");
                this.appendCollectorLog("Created working folder " + this.targetFolder.toFile().getName());
            }
            catch (SapphireException e) {
                this.appendCollectorLog("ALERT: Failed to create capture working folder. " + e.getMessage() + ".");
                e.printStackTrace();
            }
            if (this.targetFolder != null) {
                this.initManifest(sendId, lastTriggerDt);
            }
        }
        catch (IOException e) {
            throw new SapphireException(e.getMessage(), e);
        }
        return sendId;
    }

    @Override
    public String store(String sendId, Path file, BaseFileSender.ActionOnOrginal processOriginalMode, String attachmentClass, boolean byReference, Path archiveFolder, String archiveFolderAsSeenFromLIMS, PropertyList fileMetaData) throws SapphireException {
        this.appendCollectorLog("Adding trigger file: " + file);
        String description = "";
        String filehashvalue = "";
        boolean archiveFolderAsSeenFromLIMSFound = false;
        try {
            this.setFileManifestProperties(file, fileMetaData);
            Path targetPath = this.targetFolder.resolve(file.getFileName().toString());
            long fileSize = Files.size(file);
            boolean hashdata = false;
            if (!byReference && this.getSdmsCollector().isHashable() && fileSize <= (long)(this.getSdmsCollector().getHashMaxSizeGB() * 1024 * 1024 * 1024)) {
                hashdata = true;
            }
            FileTransferOptions options = new FileTransferOptions();
            if (processOriginalMode == BaseFileSender.ActionOnOrginal.DELETE) {
                if (byReference) {
                    throw new SapphireException("Cannot reference a file which is flagged for delete");
                }
                this.appendCollectorLog("Moving file " + file.toFile().getName());
                description = "File moved to " + targetPath.toFile().getAbsolutePath();
                options.setDeleteSourceOnSuccessfullTransfer(true);
                options.setReturnHashValue(hashdata);
                FileTransfer.safeFileTransfer(file.toFile(), targetPath.toFile(), options);
            } else if (processOriginalMode == BaseFileSender.ActionOnOrginal.LEAVE) {
                if (byReference) {
                    this.appendCollectorLog("Referencing file " + file.toFile().getName());
                } else {
                    this.appendCollectorLog("Copying file " + file.toFile().getName());
                    description = "File copied to " + targetPath.toFile().getAbsolutePath();
                    options.setReturnHashValue(hashdata);
                    FileTransfer.safeFileTransfer(file.toFile(), targetPath.toFile(), options);
                }
            } else if (processOriginalMode == BaseFileSender.ActionOnOrginal.ARCHIVE) {
                if (archiveFolder != null && !Files.exists(archiveFolder, new LinkOption[0])) {
                    throw new SapphireException("No archive folder provided or does not exist.");
                }
                this.appendCollectorLog("Moving file " + file.toFile().getName() + " to archive");
                file = FileUtil.moveFileOrFolder(file, archiveFolder, false);
                if (archiveFolderAsSeenFromLIMS != null && archiveFolderAsSeenFromLIMS.length() > 0) {
                    archiveFolderAsSeenFromLIMSFound = true;
                    fileMetaData.setProperty("originalarchivefolder", file.toString());
                }
                if (byReference) {
                    this.appendCollectorLog("Referencing file " + file.toFile().getName());
                } else {
                    this.appendCollectorLog("Copying file " + file.toFile().getName());
                    description = "File copied to " + targetPath.toFile().getAbsolutePath();
                    options.setReturnHashValue(hashdata);
                    FileTransfer.safeFileTransfer(file.toFile(), targetPath.toFile(), options);
                }
            }
            if (hashdata) {
                filehashvalue = "" + options.getSourceHashValue();
                fileMetaData.setProperty("filehashvalue", filehashvalue);
            }
        }
        catch (Exception e) {
            this.logError(sendId, "ALERT: Failed to capture instrument files " + file.getFileName() + ": " + e.getMessage() + ".");
            e.printStackTrace();
        }
        this.addFileMetaDataToManifest(sendId, file, attachmentClass, byReference, fileMetaData, archiveFolderAsSeenFromLIMSFound ? archiveFolderAsSeenFromLIMS : "");
        return description;
    }

    @Override
    public String zipAndStore(String sendId, Path zipFile, BaseFileSender.ActionOnOrginal processOriginalMode, String attachmentClass, boolean byReference, Path archiveFolder, String archiveFolderAsSeenFromLIMS, PropertyList fileMetaData, List<Path> originalFiles) throws SapphireException {
        this.addFileMetaData(originalFiles, fileMetaData);
        if (processOriginalMode == BaseFileSender.ActionOnOrginal.DELETE) {
            if (FileUtil.deleteFiles(originalFiles)) {
                this.appendCollectorLog("Original Files have been deleted");
            } else {
                this.logError(sendId, "Failed to delete original file(s).");
            }
        } else if (processOriginalMode == BaseFileSender.ActionOnOrginal.ARCHIVE) {
            if (FileUtil.moveFiles(originalFiles, archiveFolder, false)) {
                this.appendCollectorLog("Original Files have been moved");
            } else {
                this.logError(sendId, "Failed to move original file(s).");
            }
        }
        return this.store(sendId, zipFile, processOriginalMode, attachmentClass, byReference, archiveFolder, archiveFolderAsSeenFromLIMS, fileMetaData);
    }

    @Override
    public void complete(String sendId) throws SapphireException {
        try {
            super.writeManifest(sendId, this.targetFolder.resolve("lv_capture.mf"));
            Path captureFolder = SDMSUtil.renameAndZipFolder(this.targetFolder, "_capturing", "_captured");
            if (captureFolder != null) {
                this.appendCollectorLog("Capture complete: Capture stored in " + captureFolder.toFile().getName());
            }
            this.getMetrics().stopSenderTimer();
        }
        catch (Exception e) {
            throw new SapphireException(e.getMessage(), e);
        }
        super.complete(sendId);
    }
}

