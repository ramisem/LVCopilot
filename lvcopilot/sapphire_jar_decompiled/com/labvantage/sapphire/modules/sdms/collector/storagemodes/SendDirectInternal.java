/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.collector.storagemodes;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.modules.sdms.actions.CreateDataCapture;
import com.labvantage.sapphire.modules.sdms.actions.UpdateDataCapture;
import com.labvantage.sapphire.modules.sdms.collector.SDMSCollector;
import com.labvantage.sapphire.modules.sdms.collector.collectortypes.BaseCollectorType;
import com.labvantage.sapphire.modules.sdms.collector.storagemodes.BaseFileSender;
import com.labvantage.sapphire.util.file.FileManager;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.AttachmentProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.xml.PropertyList;

public class SendDirectInternal
extends BaseFileSender {
    private final String connectionid;
    private String dataCaptureId;

    public SendDirectInternal(SDMSCollector sdmsCollector, BaseCollectorType collectorType, String connectionid) {
        super(sdmsCollector, collectorType);
        this.connectionid = connectionid;
    }

    @Override
    public String init(Calendar lastTriggerDt, PropertyList captureMetaData) throws SapphireException {
        String sendId = super.init(lastTriggerDt, captureMetaData);
        this.getMetrics().startSenderTimer();
        ActionProcessor ap = new ActionProcessor(this.connectionid);
        PropertyList props = new PropertyList();
        props.setProperty("instrumentid", this.getInstrumentid());
        props.setProperty("sdmscollectorid", this.getSdmsCollector().getCollectorid());
        if (captureMetaData.containsKey("capturedt")) {
            props.setProperty("capturedt", captureMetaData.getProperty("capturedt"));
        }
        if (captureMetaData.containsKey("triggerdt")) {
            props.setProperty("triggerdt", captureMetaData.getProperty("triggerdt"));
        }
        ap.processActionClass(CreateDataCapture.class.getName(), props);
        this.dataCaptureId = props.getProperty("datacaptureid");
        this.appendCollectorLog("Data Capture " + this.dataCaptureId + " created.");
        this.initManifest(sendId, lastTriggerDt);
        return sendId;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public String store(String sendId, Path file, BaseFileSender.ActionOnOrginal processOriginalMode, String attachmentClass, boolean byReference, Path archiveFolder, String archiveFolderAsSeenFromLIMS, PropertyList fileMetaData) throws SapphireException {
        AttachmentProcessor ap = new AttachmentProcessor(this.connectionid);
        String description = "";
        Path finalFile = file;
        try {
            fileMetaData = this.getFileMetaData(file, attachmentClass, byReference, "", fileMetaData);
            finalFile = file;
            if (processOriginalMode == BaseFileSender.ActionOnOrginal.ARCHIVE) {
                if (archiveFolder != null && !Files.exists(archiveFolder, new LinkOption[0])) {
                    throw new SapphireException("No archive folder provided or does not exist.");
                }
                Path result = FileUtil.moveFileOrFolder(file, archiveFolder, false);
                if (result == file) {
                    throw new SapphireException("Unable to archive file.");
                }
                finalFile = result;
            }
            if (byReference) {
                if (processOriginalMode == BaseFileSender.ActionOnOrginal.DELETE) {
                    throw new SapphireException("Action on orginal is delete and by reference is selected as storage mode.");
                }
                FileManager.addFileReferenceAttachment("LV_DataCapture", this.dataCaptureId, "", "", finalFile, attachmentClass, fileMetaData, new ActionProcessor(this.connectionid), new QueryProcessor(this.connectionid), ap, new SDCProcessor(this.connectionid), this.connectionid);
            } else {
                FileManager.addFileAttachment("LV_DataCapture", this.dataCaptureId, "", "", finalFile, attachmentClass, false, fileMetaData, new ActionProcessor(this.connectionid), new QueryProcessor(this.connectionid), ap, new SDCProcessor(this.connectionid), this.connectionid);
            }
            this.appendCollectorLog("File " + finalFile.toFile().getName() + " has been stored as an attachment.");
            description = "Added as attachment to data capture " + this.dataCaptureId;
            if (processOriginalMode == BaseFileSender.ActionOnOrginal.DELETE) {
                if (!finalFile.toFile().exists()) return description;
                Files.delete(finalFile);
                return description;
            }
            if (processOriginalMode != BaseFileSender.ActionOnOrginal.LEAVE && processOriginalMode != BaseFileSender.ActionOnOrginal.ARCHIVE) return description;
        }
        catch (Throwable e) {
            this.logError(sendId, "ALERT: Failed to capture instrument files " + finalFile.getFileName() + ": " + e.getClass().getName() + ": " + e.getMessage() + ".");
            throw new SapphireException(e);
        }
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
        ActionProcessor ap = new ActionProcessor(this.connectionid);
        PropertyList props = this.getCaptureMetaData(sendId).copy();
        UpdateDataCapture.addMetaData(props, new String[]{"triggerdt", "capturedt", "labvantageversion", "labvantagebuild", "instrumentid", "collectortypeid"}, this.dataCaptureId, false, new SDCProcessor(this.connectionid), ap);
        if (this.hasError(sendId)) {
            UpdateDataCapture.markFailure(this.dataCaptureId, this.getCollectorType().getCollectionLog(), ap);
        } else {
            UpdateDataCapture.markCaptured(this.dataCaptureId, this.getCollectorType().getCollectionLog(), props.getProperty("capturedt"), ap);
        }
        this.getMetrics().stopSenderTimer();
        super.complete(sendId);
    }
}

