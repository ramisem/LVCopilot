/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.actions;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.modules.sdms.SDMSUtil;
import com.labvantage.sapphire.modules.sdms.actions.CreateDataCapture;
import com.labvantage.sapphire.modules.sdms.actions.UpdateDataCapture;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileTransfer;
import com.labvantage.sapphire.util.file.FileType;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.Iterator;
import sapphire.SapphireException;
import sapphire.accessor.AttachmentProcessor;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ImportDataCaptureFolder
extends BaseAction
implements SDMSConstants {
    public static final String ID = "ImportDataCaptureFolder";
    public static final String PROPERTY_PATH = "path";
    public static final String PROPERTY_COLLECTORID = "collectorid";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        block15: {
            String collectorid = properties.getProperty(PROPERTY_COLLECTORID);
            String p = properties.getProperty(PROPERTY_PATH);
            boolean error = false;
            this.logger.debug("ImportDataCaptureFolder - Input path: " + p);
            Path originalPath = Paths.get(p, new String[0]);
            if (Files.exists(originalPath, new LinkOption[0])) {
                try {
                    Path workingPath = null;
                    Path renamedPath = null;
                    if (!Files.isDirectory(originalPath, new LinkOption[0])) {
                        renamedPath = SDMSUtil.renameFile(originalPath, "_captured", "_storing");
                        this.logger.debug("ImportDataCaptureFolder - Renamed file path: " + renamedPath.toString());
                        File target = Files.createTempDirectory("temp", new FileAttribute[0]).toFile();
                        FileTransfer.safeFileTransfer(renamedPath.toFile(), target, null);
                        workingPath = target.toPath();
                    } else {
                        renamedPath = SDMSUtil.renameFolder(originalPath, "_captured", "_storing");
                        this.logger.debug("ImportDataCaptureFolder - Renamed folder path: " + renamedPath.toString());
                        workingPath = renamedPath;
                    }
                    this.logger.debug("ImportDataCaptureFolder - Working path: " + workingPath.toString());
                    Path manifest = workingPath.resolve("lv_capture.mf");
                    if (!manifest.toFile().exists()) break block15;
                    PropertyList manifestProps = new PropertyList();
                    manifestProps.setPropertyList(manifest.toFile());
                    String instrumentid = manifestProps.getProperty("instrumentid");
                    String sdmscollectorid = manifestProps.getProperty("sdmscollectorid", collectorid);
                    String datacaptureLog = manifestProps.getProperty("datacapturelog");
                    PropertyList props = new PropertyList();
                    props.setProperty("instrumentid", instrumentid);
                    props.setProperty("sdmscollectorid", sdmscollectorid);
                    PropertyList metadata = manifestProps.getPropertyListNotNull("capturemetadata");
                    for (Object key : metadata.keySet()) {
                        if (key.toString().equalsIgnoreCase("instrumentid") || key.toString().equalsIgnoreCase("labvantageversion") || key.toString().equalsIgnoreCase("labvantagebuild") || key.toString().equalsIgnoreCase("collectortypeid")) continue;
                        props.put(key, metadata.getProperty(key.toString(), ""));
                    }
                    this.getActionProcessor().processActionClass(CreateDataCapture.class.getName(), props);
                    String dataCaptureId = props.getProperty("datacaptureid");
                    PropertyListCollection files = manifestProps.getCollectionNotNull("files");
                    for (int i = 0; i < files.size(); ++i) {
                        Path file;
                        PropertyList fileProp = files.getPropertyList(i);
                        String filename = fileProp.getProperty("filename");
                        String attachmentclass = fileProp.getProperty("attachmentclass", "");
                        boolean byReference = fileProp.getProperty("byreference", "N").equalsIgnoreCase("Y");
                        Path path = file = byReference ? Paths.get(filename, new String[0]) : workingPath.resolve(filename);
                        if (file.toFile().exists()) {
                            long fileHashValue;
                            PropertyList metaData = fileProp.getPropertyListNotNull("filemetadata").copy();
                            Iterator iterator = fileProp.keySet().iterator();
                            while (iterator.hasNext()) {
                                String key = iterator.next().toString();
                                if (key.equalsIgnoreCase("filemetadata") || key.equalsIgnoreCase("filename") || key.equalsIgnoreCase("attachmentclass") || key.equalsIgnoreCase("byreference")) continue;
                                metaData.setProperty(key, fileProp.getProperty(key));
                            }
                            if (metaData != null && metaData.containsKey("filename") && !metaData.containsKey("mime")) {
                                String fname = metaData.getProperty("filename");
                                metaData.setProperty("mime", FileType.getFileTypeByFileName(fname, this.connectionInfo.getConnectionId()).getMime());
                            }
                            AttachmentProcessor ap = this.getRakFile() != null ? new AttachmentProcessor(this.getRakFile(), this.getConnectionid()) : new AttachmentProcessor(this.getConnectionid());
                            sapphire.attachment.Attachment attachment = null;
                            if (byReference) {
                                attachment = FileManager.addFileReferenceAttachment("LV_DataCapture", dataCaptureId, "", "", file, attachmentclass, metaData, this.getActionProcessor(), this.getQueryProcessor(), ap, this.getSDCProcessor(), this.getConnectionId());
                                continue;
                            }
                            attachment = FileManager.addFileAttachment("LV_DataCapture", dataCaptureId, "", "", file, attachmentclass, false, metaData, this.getActionProcessor(), this.getQueryProcessor(), ap, this.getSDCProcessor(), this.getConnectionId());
                            Attachment att = Attachment.getAttachment((Attachment)attachment, this.getQueryProcessor(), this.getConnectionId());
                            long attachmentDataHash = att.getDataHash();
                            String filehashvalue = metaData.getProperty("filehashvalue", "");
                            if (filehashvalue.length() <= 0 || attachmentDataHash == 0L || attachmentDataHash == (fileHashValue = Long.parseLong(filehashvalue))) continue;
                            error = true;
                            String hashLog = "File corrupted: File Hash before transfer is " + fileHashValue + " and after transfer is " + attachmentDataHash;
                            datacaptureLog = datacaptureLog + (datacaptureLog.length() > 0 ? "\n" : "") + "" + hashLog;
                            SDMSUtil.raiseSDMSAlert(this.getActionProcessor(), "LV_DataCapture", dataCaptureId, "SDMS Delivery", "Failure", true, "File corrupted", hashLog);
                            continue;
                        }
                        if (!byReference) continue;
                        error = true;
                        datacaptureLog = datacaptureLog + (datacaptureLog.length() > 0 ? "\n" : "") + "Unable to find referenced file from LIMS server.";
                    }
                    if (error) {
                        UpdateDataCapture.markFailure(dataCaptureId, datacaptureLog, this.getActionProcessor());
                    } else {
                        UpdateDataCapture.markCaptured(dataCaptureId, datacaptureLog, "", this.getActionProcessor());
                    }
                    Path finalPath = null;
                    finalPath = Files.isDirectory(renamedPath, new LinkOption[0]) ? SDMSUtil.renameFolder(renamedPath, "_storing", "_stored") : SDMSUtil.renameFile(renamedPath, "_storing", "_stored");
                    if (finalPath != null) {
                        PropertyList origin = new PropertyList();
                        origin.setProperty("captureorigin", finalPath.toString());
                        UpdateDataCapture.addMetaData(origin, null, dataCaptureId, false, this.getSDCProcessor(), this.getActionProcessor());
                    }
                }
                catch (Exception e) {
                    SDMSUtil.raiseSDMSAlert(this.getActionProcessor(), "LV_SDMSCollector", collectorid, "SDMS Delivery", "Failure", true, "Failed to import DCF " + p, e.getMessage());
                    Trace.logError("Failed to import DCF " + p + ": " + e.getMessage(), e);
                }
            } else {
                this.logger.warn("Could not locate SDMS data-capture-folder: " + properties.getProperty(PROPERTY_PATH) + ". It might have been already processed.");
            }
        }
    }
}

