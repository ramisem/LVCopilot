/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.collector.storagemodes;

import com.labvantage.sapphire.Build;
import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.modules.sdms.SDMSUtil;
import com.labvantage.sapphire.modules.sdms.collector.SDMSCollector;
import com.labvantage.sapphire.modules.sdms.collector.collectortypes.BaseCollectorType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public abstract class BaseFileSender
implements SDMSConstants {
    private final SDMSCollector sdmsCollector;
    private String instrumentid;
    private BaseCollectorType collectorType;
    private HashMap<String, SendDetails> senders;

    public boolean hasError(String sendId) {
        if (this.senders.size() > 0 && this.senders.containsKey(sendId)) {
            return this.senders.get(sendId).error;
        }
        return true;
    }

    public void logError(String sendId, String msg) {
        if (this.senders.size() > 0 && this.senders.containsKey(sendId)) {
            this.senders.get(sendId).error = true;
        }
        this.appendCollectorLog(msg);
    }

    public BaseFileSender(SDMSCollector sdmsCollector, BaseCollectorType collectorType) {
        this.sdmsCollector = sdmsCollector;
        this.collectorType = collectorType;
        this.instrumentid = collectorType == null ? null : collectorType.getInstrumentid();
        this.senders = new HashMap();
    }

    protected void writeManifest(String sendId, Path file) throws IOException {
        if (this.senders.size() > 0 && this.senders.containsKey(sendId)) {
            this.senders.get(sendId).manifest.setProperty("datacapturelog", this.getCollectorType().getCollectionLog());
            Files.write(file, this.senders.get(sendId).manifest.toXMLString().getBytes("UTF-8"), new OpenOption[0]);
        }
    }

    protected PropertyList getFileMetaData(Path file, String attachmentClass, boolean byReference, String filehashvalue, PropertyList existingFileMetaData) {
        PropertyList f = this.getFileManifestPropertyList(file);
        if (existingFileMetaData != null && existingFileMetaData.size() > 0) {
            if (filehashvalue != null && filehashvalue.length() > 0) {
                existingFileMetaData.setProperty("filehashvalue", filehashvalue);
            }
            try {
                f.setPropertyList(existingFileMetaData.toXMLString(), true);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (f.containsKey("byreference")) {
            f.remove("byreference");
        }
        if (f.containsKey("attachmentclass")) {
            f.remove("attachmentclass");
        }
        if (f.containsKey("filename")) {
            f.remove("filename");
        }
        return f;
    }

    protected void addFileMetaDataToManifest(String sendId, Path file, String attachmentClass, boolean byReference, PropertyList fileMetaData, String archiveFolderAsSeenFromLIMS) {
        if (this.senders.size() > 0 && this.senders.containsKey(sendId)) {
            PropertyList fileManifestPropoertyList = new PropertyList();
            if (fileMetaData != null && fileMetaData.size() > 0) {
                fileManifestPropoertyList.setProperty("filemetadata", fileMetaData);
            }
            if (attachmentClass != null && attachmentClass.length() > 0) {
                fileManifestPropoertyList.setProperty("attachmentclass", attachmentClass);
            }
            String filename = "";
            if (byReference) {
                fileManifestPropoertyList.setProperty("byreference", "Y");
                filename = archiveFolderAsSeenFromLIMS.length() > 0 ? archiveFolderAsSeenFromLIMS + (archiveFolderAsSeenFromLIMS.contains("/") ? "/" : "\"") + file.getFileName().toString() : file.toString();
            } else {
                filename = file.getFileName().toString();
            }
            fileManifestPropoertyList.setProperty("filename", filename);
            this.senders.get(sendId).manifestCollectedFiles.add(fileManifestPropoertyList);
        }
    }

    protected void addFileMetaData(List<Path> originalFiles, PropertyList fileMetaData) {
        StringBuilder originPath = new StringBuilder("{|");
        for (int i = 0; i < originalFiles.size(); ++i) {
            if (i == 0) {
                originPath.append(originalFiles.get(i).toString());
                continue;
            }
            originPath.append(";").append(originalFiles.get(i).toString());
        }
        originPath.append("|}");
        fileMetaData.put("originpath", originPath.toString());
    }

    protected void setFileManifestProperties(Path file, PropertyList metaData) {
        PropertyList pl = metaData;
        try {
            pl.setProperty("size", "" + Files.size(file));
            BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class, new LinkOption[0]);
            pl.setProperty("creationdt", SDMSUtil.formatDate(new Date(attr.creationTime().toMillis())));
            pl.setProperty("lastaccessdt", SDMSUtil.formatDate(new Date(attr.lastAccessTime().toMillis())));
            pl.setProperty("lastmodifieddt", SDMSUtil.formatDate(new Date(attr.lastModifiedTime().toMillis())));
            if (!metaData.containsKey("originpath")) {
                pl.setProperty("originpath", file.toString());
            }
        }
        catch (Exception e) {
            this.appendCollectorLog("Failed to read file attributes: " + e.getMessage());
        }
    }

    private PropertyList getFileManifestPropertyList(Path file) {
        PropertyList pl = new PropertyList();
        this.setFileManifestProperties(file, pl);
        return pl;
    }

    protected void initManifest(String sendId, Calendar lastTriggerDt) {
        if (this.senders.size() > 0 && this.senders.containsKey(sendId)) {
            this.senders.get(sendId).manifest = new PropertyList();
            this.senders.get(sendId).manifest.setProperty("instrumentid", this.getInstrumentid());
            this.senders.get(sendId).manifest.setProperty("sdmscollectorid", this.getSdmsCollector().getCollectorid());
            this.senders.get(sendId).manifestCollectedFiles = new PropertyListCollection();
            this.senders.get(sendId).manifest.setProperty("files", this.senders.get(sendId).manifestCollectedFiles);
            this.senders.get(sendId).manifest.setProperty("capturemetadata", this.senders.get(sendId).captureMetaData);
            this.senders.get(sendId).captureMetaData.setProperty("capturedt", SDMSUtil.formatCalendar(lastTriggerDt));
        }
    }

    public String init(Calendar lastTriggerDt, PropertyList captureMetaData) throws SapphireException {
        SendDetails sendDetails = new SendDetails();
        sendDetails.captureMetaData = captureMetaData;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        captureMetaData.setProperty("instrumentid", this.instrumentid);
        captureMetaData.setProperty("labvantageversion", Build.getVersion());
        captureMetaData.setProperty("labvantagebuild", Build.getBuild());
        captureMetaData.setProperty("collectortypeid", this.collectorType.getCollectorType());
        if (lastTriggerDt != null) {
            captureMetaData.setProperty("triggerdt", sdf.format(lastTriggerDt.getTime()));
        }
        String id = lastTriggerDt.getTimeInMillis() + "";
        this.senders.put(id, sendDetails);
        return id;
    }

    public abstract String store(String var1, Path var2, ActionOnOrginal var3, String var4, boolean var5, Path var6, String var7, PropertyList var8) throws SapphireException;

    public void complete(String sendId) throws SapphireException {
        if (this.senders.containsKey(sendId)) {
            this.senders.remove(sendId);
        }
    }

    public void appendCollectorLog(String message) {
        this.collectorType.appendCollectionLog(message);
    }

    public abstract String zipAndStore(String var1, Path var2, ActionOnOrginal var3, String var4, boolean var5, Path var6, String var7, PropertyList var8, List<Path> var9) throws SapphireException;

    public String getInstrumentid() {
        return this.instrumentid;
    }

    public SDMSCollector getSdmsCollector() {
        return this.sdmsCollector;
    }

    public BaseCollectorType getCollectorType() {
        return this.collectorType;
    }

    public BaseCollectorType.Metrics getMetrics() {
        return this.getCollectorType().getMetrics();
    }

    public PropertyList getCaptureMetaData(String sendId) {
        if (this.senders.size() > 0 && this.senders.containsKey(sendId)) {
            return this.senders.get(sendId).captureMetaData;
        }
        return new PropertyList();
    }

    public PropertyList getCaptureMetaData() {
        if (this.senders.size() > 0) {
            Iterator<String> it = this.senders.keySet().iterator();
            if (it.hasNext()) {
                return this.senders.get(it.next()).captureMetaData;
            }
            return new PropertyList();
        }
        return new PropertyList();
    }

    private class SendDetails {
        private PropertyList captureMetaData;
        private PropertyList manifest;
        private PropertyListCollection manifestCollectedFiles;
        private boolean error = false;

        private SendDetails() {
        }
    }

    public static enum ActionOnOrginal {
        LEAVE,
        ARCHIVE,
        DELETE;

    }
}

