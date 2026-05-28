/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.collector.storagemodes;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.modules.sdms.collector.SDMSCollector;
import com.labvantage.sapphire.modules.sdms.collector.collectortypes.BaseCollectorType;
import com.labvantage.sapphire.modules.sdms.collector.storagemodes.BaseFileSender;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class SendDirectExternal
extends BaseFileSender {
    private String dataCaptureId;

    public SendDirectExternal(SDMSCollector sdmsCollector, BaseCollectorType collectorType) {
        super(sdmsCollector, collectorType);
    }

    @Override
    public String init(Calendar lastTriggerDt, PropertyList captureMetaData) throws SapphireException {
        String sendId = super.init(lastTriggerDt, captureMetaData);
        this.getMetrics().startSenderTimer();
        PropertyList commandRequest = new PropertyList();
        commandRequest.setProperty("instrumentid", this.getInstrumentid());
        commandRequest.setProperty("sdmscollectorid", this.getSdmsCollector().getCollectorid());
        PropertyList commandResponse = this.getSdmsCollector().sendCommandToLIMS("COMMAND_CREATEDATACAPTURE", commandRequest);
        this.dataCaptureId = commandResponse.getProperty("datacaptureid");
        this.appendCollectorLog("Data Capture " + this.dataCaptureId + " created.");
        this.initManifest(sendId, lastTriggerDt);
        return sendId;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public String store(String sendId, Path file, BaseFileSender.ActionOnOrginal processOriginalMode, String attachmentClass, boolean byReference, Path archiveFolder, String archivefolderAsSeenFromLIMS, PropertyList fileMetaData) throws SapphireException {
        String description;
        try {
            Object commandRequest;
            this.setFileManifestProperties(file, fileMetaData);
            Path finalFile = file;
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
                commandRequest = new PropertyList();
                ((HashMap)commandRequest).put("datacaptureid", this.dataCaptureId);
                ((HashMap)commandRequest).put("filepath", finalFile.toString());
                ((HashMap)commandRequest).put("filemetadata", fileMetaData.toJSONString());
                ((HashMap)commandRequest).put("attachmentclass", attachmentClass);
                this.getSdmsCollector().container.sendCommandToLIMS("COMMAND_ADDDATACAPTUREATTACHMENT", (PropertyList)commandRequest);
                description = "Added as attachment by reference to data capture " + this.dataCaptureId;
                this.appendCollectorLog("File has been referenced by LIMS");
            } else {
                commandRequest = new JSONObject();
                ((JSONObject)commandRequest).put("datacaptureid", this.dataCaptureId);
                ((JSONObject)commandRequest).put("performhash", !finalFile.getFileName().toString().equalsIgnoreCase("mf"));
                ((JSONObject)commandRequest).put("filemetadata", fileMetaData.toJSONString());
                ((JSONObject)commandRequest).put("attachmentclass", attachmentClass);
                JSONObject returnedP = this.getSdmsCollector().container.sendFileCommandToLIMS("", "COMMAND_ADDDATACAPTUREATTACHMENT", finalFile, (JSONObject)commandRequest);
                String filehashvalue = "";
                if (returnedP != null && returnedP.length() > 0 && returnedP.has("filehashvalue")) {
                    filehashvalue = returnedP.optString("filehashvalue");
                    String attachmentdatahash = returnedP.optString("attachmentdatahash");
                    if (attachmentdatahash.length() > 0 && filehashvalue.length() > 0) {
                        this.logError(sendId, "Hash validation failed: File Hash Value:" + filehashvalue + " but Attachment Data Hash:" + attachmentdatahash);
                    }
                }
                description = "Added as attachment to data capture " + this.dataCaptureId;
                this.appendCollectorLog("File has been sent to LIMS");
            }
            if (processOriginalMode == BaseFileSender.ActionOnOrginal.DELETE) {
                try {
                    if (!finalFile.toFile().exists()) return description;
                    Files.delete(finalFile);
                    return description;
                }
                catch (IOException e) {
                    this.appendCollectorLog("ALERT: Failed to delete file: " + finalFile.getFileName());
                }
                return description;
            }
            if (processOriginalMode != BaseFileSender.ActionOnOrginal.LEAVE && processOriginalMode != BaseFileSender.ActionOnOrginal.ARCHIVE) return description;
        }
        catch (Exception e) {
            this.logError(sendId, "WARNING - File failed send to LIMS: " + e.getMessage());
            throw new SapphireException("Failed to add attachment", e);
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
        Path mFile = null;
        try {
            mFile = FileUtil.createTempFile("manifest", ".mf");
            this.writeManifest(sendId, mFile);
        }
        catch (Exception e) {
            this.logError(sendId, e.getMessage());
        }
        if (this.hasError(sendId) || mFile == null) {
            PropertyList commandRequest = new PropertyList();
            commandRequest.setProperty("datacaptureid", this.dataCaptureId);
            commandRequest.setProperty("datacapturelog", this.getCollectorType().getCollectionLog());
            commandRequest.setProperty("failure", "Y");
            this.getSdmsCollector().sendCommandToLIMS("COMMAND_COMPLETEDATACAPTURE", commandRequest);
        } else {
            JSONObject commandRequest = new JSONObject();
            try {
                commandRequest.put("datacaptureid", this.dataCaptureId);
                commandRequest.put("datacapturelog", this.getCollectorType().getCollectionLog());
                commandRequest.put("performhash", "N");
            }
            catch (Exception exception) {
                // empty catch block
            }
            this.getSdmsCollector().container.sendFileCommandToLIMS("", "COMMAND_COMPLETEDATACAPTURE", mFile, commandRequest);
        }
        this.getMetrics().stopSenderTimer();
        super.complete(sendId);
    }
}

