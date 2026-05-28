/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt;

import com.labvantage.sapphire.Build;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.cmt.CMTUtil;
import com.labvantage.sapphire.cmt.PropertyTreeSnapshotItem;
import com.labvantage.sapphire.cmt.SDISnapshot;
import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.AttachmentService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.ZipFileUtil;
import com.labvantage.sapphire.xml.ExportXML;
import com.labvantage.sapphire.xml.SaxUtil;
import com.labvantage.sapphire.xml.cmt.SnapshotPackageHandler;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.AttachmentProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.attachment.Attachment;
import sapphire.util.DataSet;
import sapphire.util.LogContext;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;
import sapphire.xml.cmt.Snapshot;
import sapphire.xml.cmt.SnapshotItem;

public class SnapshotPackage {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";
    private static final String LOGNAME = "SnapshotPackage";
    protected Logger logger = new Logger(new LogContext("SnapshotPackage", "(none)"));
    private Map<SnapshotItem, Snapshot> snapshots = new LinkedHashMap<SnapshotItem, Snapshot>();
    private Map<SnapshotItem, Snapshot> preSnapshots = new LinkedHashMap<SnapshotItem, Snapshot>();
    private List<SnapshotItem> requestedItems = new ArrayList<SnapshotItem>();
    private String uuid = "";
    private boolean isFromChangeLog = false;
    private File zipFile;
    private String createTool = "";
    private boolean isManifestCorrupted = false;
    private boolean isContentCorrupted = false;
    public static final String CREATETOOL_SNAPSHOTFACTORY = "snapshotfactory";
    public static final String CREATETOOL_XML = "xml";
    public static final String CREATETOOL_ZIP = "zip";
    public static final String CMTATTACHMENTID = "__cmtattachmentid";
    public static final String IMAGESOURCE_PRE = "PRE_IMAGE";
    public static final String IMAGESOURCE_POST = "POST_IMAGE";
    private static String attachmentsDirName = "attachments";
    private static final String CHECKSUM_START = "[checksum]";
    private static final String CHECKSUM_END = "[checksume]";

    private SnapshotPackage() {
    }

    public SnapshotPackage(boolean isFromChangeLog) {
        this.createTool = CREATETOOL_SNAPSHOTFACTORY;
        this.isFromChangeLog = isFromChangeLog;
        this.uuid = UUID.randomUUID().toString().toUpperCase();
    }

    public String getUUID() {
        return this.uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public String getCreateTool() {
        return this.createTool;
    }

    public void setCreateTool(String createTool) {
        this.createTool = createTool;
    }

    public File getZipFile() {
        return this.zipFile;
    }

    public void setZipFile(File zipFile) {
        this.zipFile = zipFile;
    }

    public boolean isFromChangeLog() {
        return this.isFromChangeLog;
    }

    public void setFromChangeLog(boolean fromChangeLog) {
        this.isFromChangeLog = fromChangeLog;
    }

    public boolean isManifestCorrupted() {
        return this.isManifestCorrupted;
    }

    public boolean isContentCorrupted() {
        return this.isContentCorrupted;
    }

    public Snapshot getSnapshot(SnapshotItem snapshotItem) {
        Snapshot snapshot = this.snapshots.get(snapshotItem);
        return snapshot;
    }

    public void addSnapshot(SnapshotItem snapshotItem, Snapshot snapshot, boolean isRequested) {
        if (isRequested) {
            this.requestedItems.add(snapshotItem);
        }
        this.snapshots.put(snapshotItem, snapshot);
        if (snapshot != null) {
            ((SDISnapshot)snapshot).setSnapshotPackage(this);
        }
    }

    public List<SnapshotItem> getSnapshotItems() {
        return new ArrayList<SnapshotItem>(this.snapshots.keySet());
    }

    public List<SnapshotItem> getRequestedSnapshotItems() {
        return this.requestedItems;
    }

    public List<SnapshotItem> getExtraSnapshotItems() {
        ArrayList<SnapshotItem> extraSnapshotItems = new ArrayList<SnapshotItem>();
        for (SnapshotItem snapshotItem : this.snapshots.keySet()) {
            if (this.requestedItems.contains(snapshotItem)) continue;
            extraSnapshotItems.add(snapshotItem);
        }
        return extraSnapshotItems;
    }

    public List<Snapshot> getRequestedSnapshots() {
        ArrayList<Snapshot> requestedSnapshots = new ArrayList<Snapshot>();
        for (SnapshotItem snapshotItem : this.requestedItems) {
            requestedSnapshots.add(this.getSnapshot(snapshotItem));
        }
        return requestedSnapshots;
    }

    public List<Snapshot> getExtraSnapshots() {
        ArrayList<Snapshot> extraSnapshots = new ArrayList<Snapshot>();
        for (SnapshotItem snapshotItem : this.snapshots.keySet()) {
            if (this.requestedItems.contains(snapshotItem)) continue;
            extraSnapshots.add(this.getSnapshot(snapshotItem));
        }
        return extraSnapshots;
    }

    public void addPreSnapshot(SnapshotItem snapshotItem, Snapshot preSnapshot) {
        this.preSnapshots.put(snapshotItem, preSnapshot);
        if (preSnapshot != null) {
            ((SDISnapshot)preSnapshot).setSnapshotPackage(this);
        }
    }

    public SnapshotItem getPreSnapshotItem(SnapshotItem postSnapshotItem) {
        for (SnapshotItem snapshotitem : this.preSnapshots.keySet()) {
            if (!snapshotitem.equals(postSnapshotItem)) continue;
            return snapshotitem;
        }
        return null;
    }

    public Snapshot getPreSnapshot(SnapshotItem snapshotItem) {
        return this.preSnapshots.get(snapshotItem);
    }

    public List<SnapshotItem> getPreSnapshotItems() {
        return new ArrayList<SnapshotItem>(this.preSnapshots.keySet());
    }

    public InputStream getAttachmentFile(String fileName) throws SapphireException {
        InputStream inputStream = null;
        try {
            File file = this.getZipFile();
            ZipFile zipFile = new ZipFile(file);
            ZipEntry zipEntry = zipFile.getEntry(attachmentsDirName + "/" + fileName);
            if (zipEntry != null) {
                inputStream = zipFile.getInputStream(zipEntry);
            }
        }
        catch (IOException e) {
            throw new SapphireException("Error occured when trying to access Attachment File: " + fileName, e);
        }
        return inputStream;
    }

    @Deprecated
    public String toXML() {
        StringBuffer xml = new StringBuffer();
        xml.append("<snapshotpackage uuid='" + this.uuid + "' isFromChangeLog='" + (this.isFromChangeLog() ? "Y" : "N") + "'>");
        this.appendSnapshotsToXML(xml, this.snapshots, IMAGESOURCE_POST);
        this.appendSnapshotsToXML(xml, this.preSnapshots, IMAGESOURCE_PRE);
        xml.append("\n</snapshotpackage>");
        return xml.toString();
    }

    private void appendSnapshotsToXML(StringBuffer xml, Map<SnapshotItem, Snapshot> snapshots, String source) {
        for (SnapshotItem snapshotItem : snapshots.keySet()) {
            String snapshotItemId = snapshotItem.toString();
            Snapshot snapshot = snapshots.get(snapshotItem);
            xml.append("\n\t<snapshotpackageitem id='" + StringUtil.escapeXMLAttributeValue(snapshotItemId) + "' type = '" + (Object)((Object)snapshotItem.getType()) + "' isRequested=" + (IMAGESOURCE_POST.equals(source) ? (this.requestedItems.contains(snapshotItem) ? "'Y'" : "'N'") : "'N'") + (snapshot == null ? " nullSnapshot='Y'" : "") + " imageSource='" + source + "'>");
            if (snapshot != null) {
                xml.append("\n\t\t" + snapshot.toXML());
            }
            xml.append("\n\t</snapshotpackageitem>");
        }
    }

    public File toFile(String targetDirectory, String targetZipFileName, String connectionId) throws SapphireException {
        return this.toFile(targetDirectory, targetZipFileName, connectionId, null);
    }

    public File toFile(String targetDirectory, String targetZipFileName, String connectionId, File rakFile) throws SapphireException {
        boolean expandedZip = false;
        File returnFile = null;
        DBUtil dbAccess = null;
        try {
            if (targetDirectory == null || targetDirectory.length() == 0) {
                throw new SapphireException("Please provide a target directory.");
            }
            if (targetZipFileName == null || targetZipFileName.length() == 0) {
                throw new SapphireException("Please provide a target ZIP file name.");
            }
            if (targetDirectory.endsWith(File.separator)) {
                targetDirectory = targetDirectory.substring(0, targetDirectory.length() - 1);
            }
            ConnectionProcessor connectionProcessor = rakFile == null ? new ConnectionProcessor(connectionId) : new ConnectionProcessor(rakFile, connectionId);
            AttachmentProcessor attachmentProcessor = rakFile == null ? new AttachmentProcessor(connectionId) : new AttachmentProcessor(rakFile, connectionId);
            QueryProcessor queryProcessor = rakFile == null ? new QueryProcessor(connectionId) : new QueryProcessor(rakFile, connectionId);
            SapphireConnection sapphireConnection = connectionProcessor.getSapphireConnection();
            dbAccess = (DBUtil)CMTUtil.getDBAccess(connectionId, rakFile);
            sapphireConnection.setConnection(dbAccess.getConnection());
            AttachmentService attachmentService = new AttachmentService(sapphireConnection);
            if (rakFile != null) {
                attachmentService.setConnectionId(connectionId);
                attachmentService.setRakFile(rakFile);
            }
            String tempFolderName = targetZipFileName + "_temp" + System.currentTimeMillis();
            Path tempDirPath = Paths.get(targetDirectory + File.separator + tempFolderName, new String[0]);
            Path tempAttDirPath = Paths.get(tempDirPath + File.separator + attachmentsDirName, new String[0]);
            Path zipFilePath = Paths.get(targetDirectory + File.separator + targetZipFileName, new String[0]);
            File zipFile = zipFilePath.toFile();
            try (FileOutputStream zipFOS = new FileOutputStream(zipFile);
                 ZipOutputStream zipOutStream = new ZipOutputStream(zipFOS);){
                DataSet compCodeDS;
                if (expandedZip) {
                    Files.createDirectories(tempAttDirPath, new FileAttribute[0]);
                    returnFile = tempDirPath.toFile();
                } else {
                    Files.createDirectories(Paths.get(targetDirectory, new String[0]), new FileAttribute[0]);
                    returnFile = zipFile;
                    zipOutStream.putNextEntry(new ZipEntry(attachmentsDirName + "/"));
                    zipOutStream.closeEntry();
                }
                JSONObject manifest = new JSONObject();
                manifest.put("build", Build.getBuild());
                manifest.put("packagedt", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Calendar.getInstance().getTime()));
                manifest.put("packageby", connectionProcessor.getSapphireConnection().getSysuserId());
                manifest.put("uuid", this.getUUID());
                manifest.put("isFromChangeLog", this.isFromChangeLog());
                if (rakFile == null) {
                    if (Configuration.isDevmode(connectionProcessor.getSapphireConnection().getDatabaseId())) {
                        manifest.put("isDevMode", true);
                    }
                } else if (dbAccess.getCount("SELECT count(propertyid) FROM sysconfig WHERE propertyid = 'devmode' AND propertyvalue = 'Y'") > 1) {
                    manifest.put("isDevMode", true);
                }
                if ((compCodeDS = queryProcessor.getSqlDataSet("SELECT propertyvalue FROM sysconfig WHERE propertyid='compcode'")) != null && compCodeDS.getRowCount() > 0) {
                    String compCode = compCodeDS.getString(0, "propertyvalue", "");
                    manifest.put("compCode", compCode);
                }
                manifest.put("checksum", CHECKSUM_START + StringUtil.repeat("*", 100) + CHECKSUM_END);
                JSONArray snapshotArr = new JSONArray();
                manifest.put("snapshots", snapshotArr);
                Map<SnapshotItem, Snapshot> snapshotMapObj = null;
                String snapshotSource = "";
                AtomicInteger fileCounter = new AtomicInteger(0);
                JSONArray attInfoJSonArr = new JSONArray();
                for (int snapshotSourceCounter = 0; snapshotSourceCounter < 2; ++snapshotSourceCounter) {
                    if (snapshotSourceCounter == 0) {
                        snapshotMapObj = this.snapshots;
                        snapshotSource = IMAGESOURCE_POST;
                    } else if (snapshotSourceCounter == 1) {
                        snapshotMapObj = this.preSnapshots;
                        snapshotSource = IMAGESOURCE_PRE;
                    }
                    for (SnapshotItem snapshotItem : snapshotMapObj.keySet()) {
                        SDISnapshotItem sdiSnapshotItem = (SDISnapshotItem)snapshotItem;
                        JSONObject snapshotInfoJSON = new JSONObject();
                        snapshotArr.put(snapshotInfoJSON);
                        snapshotInfoJSON.put("id", sdiSnapshotItem);
                        snapshotInfoJSON.put("type", (Object)sdiSnapshotItem.getType());
                        snapshotInfoJSON.put("isRequested", this.requestedItems.contains(sdiSnapshotItem));
                        snapshotInfoJSON.put("imageSource", snapshotSource);
                        snapshotInfoJSON.put("isDeleted", sdiSnapshotItem.isDeleted());
                        Snapshot snapshot = snapshotMapObj.get(sdiSnapshotItem);
                        if (snapshot != null) {
                            String fileName;
                            SDISnapshot sdiSnapshot = (SDISnapshot)snapshot;
                            if (!sdiSnapshotItem.isDeleted() && !Snapshot.Type.PROPERTYTREE.equals((Object)sdiSnapshotItem.getType())) {
                                this.serializeAttachments(sdiSnapshotItem, attachmentService, expandedZip, tempAttDirPath, attachmentsDirName, zipOutStream, attInfoJSonArr, fileCounter, snapshotSourceCounter == 0);
                            }
                            if ((fileName = sdiSnapshotItem.toString(true).replaceAll("[^a-zA-Z0-9-_\\.]", "_")).length() > 200) {
                                fileName = fileName.substring(0, 200);
                            }
                            fileName = fileName + "_" + fileCounter.addAndGet(1) + ".xml";
                            snapshotInfoJSON.put("fileName", fileName);
                            String snapshotXML = sdiSnapshot.toXML();
                            ByteArrayInputStream xmlInputStream = new ByteArrayInputStream(snapshotXML.getBytes(StandardCharsets.UTF_8));
                            Throwable throwable = null;
                            try {
                                if (expandedZip) {
                                    Path outputFilePath = Paths.get(tempDirPath + File.separator + fileName, new String[0]);
                                    Files.copy(xmlInputStream, outputFilePath, new CopyOption[0]);
                                    continue;
                                }
                                StringBuffer checkSumHolder = new StringBuffer();
                                ZipFileUtil.addEntry(fileName, xmlInputStream, zipOutStream, checkSumHolder);
                                xmlInputStream.close();
                                snapshotInfoJSON.put("checksum", checkSumHolder.toString());
                                continue;
                            }
                            catch (Throwable throwable2) {
                                throwable = throwable2;
                                throw throwable2;
                            }
                            finally {
                                if (xmlInputStream == null) continue;
                                if (throwable != null) {
                                    try {
                                        xmlInputStream.close();
                                    }
                                    catch (Throwable throwable3) {
                                        throwable.addSuppressed(throwable3);
                                    }
                                    continue;
                                }
                                xmlInputStream.close();
                                continue;
                            }
                        }
                        snapshotInfoJSON.put("nullSnapshot", true);
                        if (!Snapshot.Type.PROPERTYTREE.equals((Object)sdiSnapshotItem.getType())) continue;
                        PropertyTreeSnapshotItem psi = (PropertyTreeSnapshotItem)sdiSnapshotItem;
                        snapshotInfoJSON.put("isRenamed", psi.isRenamed());
                        if (!psi.isRenamed()) continue;
                        snapshotInfoJSON.put("renamedNodeId", psi.getRenamedNodeId());
                    }
                }
                if (attInfoJSonArr.length() > 0) {
                    manifest.put("attachments", attInfoJSonArr);
                }
                ByteArrayInputStream mfInputStream = new ByteArrayInputStream(manifest.toString(2).getBytes(StandardCharsets.UTF_8));
                Object object = null;
                try {
                    if (expandedZip) {
                        Path manifestFilePath = Paths.get(tempDirPath + File.separator + "manifest.json", new String[0]);
                        Files.copy(mfInputStream, manifestFilePath, new CopyOption[0]);
                    } else {
                        Path tempManifestPath = Paths.get(FileManager.TempFile.createTempDir(), new String[0]).resolve("manifest.json.tmp");
                        File tempManifestFile = new File(tempManifestPath.toString());
                        tempManifestFile.deleteOnExit();
                        Files.copy(mfInputStream, Paths.get(tempManifestFile.getAbsolutePath(), new String[0]), StandardCopyOption.REPLACE_EXISTING);
                        String manifestFileChecksum = ExportXML.generateCheckSum(tempManifestFile, false, "", "");
                        ZipFileUtil.addEntry("manifest.json", new FileInputStream(tempManifestFile), zipOutStream);
                        ZipFileUtil.addEntry("manifest.crc", new ByteArrayInputStream(manifestFileChecksum.getBytes(StandardCharsets.UTF_8)), zipOutStream);
                    }
                }
                catch (Throwable throwable) {
                    object = throwable;
                    throw throwable;
                }
                finally {
                    if (mfInputStream != null) {
                        if (object != null) {
                            try {
                                mfInputStream.close();
                            }
                            catch (Throwable throwable) {
                                ((Throwable)object).addSuppressed(throwable);
                            }
                        } else {
                            mfInputStream.close();
                        }
                    }
                }
            }
        }
        catch (IOException | JSONException e) {
            throw new SapphireException(e);
        }
        finally {
            if (dbAccess != null) {
                dbAccess.releaseConnection();
                dbAccess.reset();
            }
        }
        return returnFile;
    }

    private void serializeAttachments(SDISnapshotItem sdiSnapshotItem, AttachmentService attachmentService, boolean expandedZip, Path tempAttDirPath, String attachmentsDirName, ZipOutputStream zipOutStream, JSONArray checkSums, AtomicInteger fileCounter, boolean isPostSnapshot) throws SapphireException, IOException, JSONException {
        SDIData sdiData = sdiSnapshotItem.getSDIData();
        DataSet attachments = sdiData.getDataset("attachment");
        if (attachments != null) {
            this.logger.info("Serialize Attachment for: " + sdiSnapshotItem.toString(true, true));
            for (int i = 0; i < attachments.getRowCount(); ++i) {
                JSONObject attInfo = new JSONObject();
                int attachmentNum = attachments.getInt(i, "attachmentnum");
                this.logger.debug("Serialize Attachment Num#: " + attachmentNum);
                Attachment attachment = Attachment.getAttachment(sdiSnapshotItem.getSDCId(), sdiSnapshotItem.getKeyId1(), sdiSnapshotItem.getKeyId2(), sdiSnapshotItem.getKeyId3(), attachmentNum);
                try {
                    attachmentService.getSDIAttachment(attachment, Attachment.ThumbnailGeneration.DISABLED);
                }
                catch (Exception e) {
                    if (isPostSnapshot) {
                        throw new SapphireException("Error occurred when retrieving Attachment Info for: " + sdiSnapshotItem.toString(true, true) + ". Att#: " + attachmentNum + ". Reason: " + e.getMessage(), e);
                    }
                    this.logger.error("Exception occurred when retrieving Attachment for: " + sdiSnapshotItem.toString(true, true) + ". Att#: " + attachmentNum, e);
                }
                if (CMTUtil.EXCLUDED_ATTACHMENT_TYPES.contains(attachment.getType())) continue;
                if (attachment != null) {
                    this.logger.debug("Loaded Attachment Info: " + attachment.toJSONString());
                    try (InputStream attachmentInStream = attachment.getInputStream();){
                        if (attachmentInStream != null) {
                            String attOutFileName = String.valueOf(fileCounter.addAndGet(1));
                            attachments.setString(i, CMTATTACHMENTID, attOutFileName);
                            if (expandedZip) {
                                Path attachmentOutputFilePath = Paths.get(tempAttDirPath + File.separator + attOutFileName, new String[0]);
                                Files.copy(attachmentInStream, attachmentOutputFilePath, new CopyOption[0]);
                                continue;
                            }
                            StringBuffer checkSum = new StringBuffer();
                            ZipFileUtil.addEntry(attachmentsDirName + "/" + attOutFileName, attachmentInStream, zipOutStream, checkSum);
                            attInfo.put("attachmentid", attOutFileName);
                            attInfo.put("checksum", checkSum.toString());
                            checkSums.put(attInfo);
                            continue;
                        }
                        if (isPostSnapshot) {
                            throw new SapphireException("Attachment InputStream is NULL for: " + sdiSnapshotItem.toString(true, true) + ". Att#: " + attachmentNum);
                        }
                        this.logger.error("Attachment InputStream is NULL. Att#: " + attachmentNum);
                        continue;
                    }
                }
                if (!isPostSnapshot) continue;
                throw new SapphireException("Attachment object is NULL for: " + sdiSnapshotItem.toString(true, true) + ". Att#: " + attachmentNum);
            }
        }
        for (SnapshotItem linkItem : sdiSnapshotItem.getLinkItems()) {
            SDISnapshotItem linkSDIItem = (SDISnapshotItem)linkItem;
            if (linkSDIItem.isIncludedForTransfer() || !linkSDIItem.isLoadedSuccessfully()) continue;
            this.serializeAttachments(linkSDIItem, attachmentService, expandedZip, tempAttDirPath, attachmentsDirName, zipOutStream, checkSums, fileCounter, isPostSnapshot);
        }
    }

    public static SnapshotPackage fromXML(String xml) throws SapphireException {
        return SnapshotPackage.fromXML(xml, null, null);
    }

    public static SnapshotPackage fromXML(String xml, String connectionId) throws SapphireException {
        return SnapshotPackage.fromXML(xml, connectionId, null);
    }

    public static SnapshotPackage fromXML(String xml, String connectionId, File rakFile) throws SapphireException {
        SnapshotPackageHandler snapshotPackageHandler = new SnapshotPackageHandler();
        snapshotPackageHandler.setXMLString(xml);
        if (connectionId != null && connectionId.length() > 0) {
            snapshotPackageHandler.setConnectionid(connectionId);
        }
        if (rakFile != null) {
            snapshotPackageHandler.setRakFile(rakFile);
        }
        SaxUtil.parseString(snapshotPackageHandler);
        return snapshotPackageHandler.getSnapshotPackage();
    }

    public static SnapshotPackage fromXML(File xmlFile) throws SapphireException {
        return SnapshotPackage.fromXML(xmlFile, null, null);
    }

    public static SnapshotPackage fromXML(File xmlFile, String connectionId) throws SapphireException {
        return SnapshotPackage.fromXML(xmlFile, connectionId, null);
    }

    public static SnapshotPackage fromXML(File xmlFile, String connectionId, File rakFile) throws SapphireException {
        SnapshotPackageHandler snapshotPackageHandler = new SnapshotPackageHandler();
        snapshotPackageHandler.setXMLFile(xmlFile);
        if (connectionId != null && connectionId.length() > 0) {
            snapshotPackageHandler.setConnectionid(connectionId);
        }
        if (rakFile != null) {
            snapshotPackageHandler.setRakFile(rakFile);
        }
        SaxUtil.parseFile(snapshotPackageHandler);
        return snapshotPackageHandler.getSnapshotPackage();
    }

    public static SnapshotPackage fromFile(File file, String connectionId) throws SapphireException {
        return SnapshotPackage.fromFile(file, connectionId, null);
    }

    public static SnapshotPackage fromFile(File file, String connectionId, File rakFile) throws SapphireException {
        SnapshotPackage snapshotPackage;
        block22: {
            snapshotPackage = null;
            try {
                ZipFile zipFile;
                if (file.isDirectory()) {
                    throw new SapphireException("Invalid File location specified. Directory de-serialization not supported yet.");
                }
                if (!file.isFile()) break block22;
                boolean isZipFile = false;
                try {
                    zipFile = new ZipFile(file);
                    isZipFile = true;
                }
                catch (ZipException zp) {
                    isZipFile = false;
                }
                if (isZipFile) {
                    zipFile = new ZipFile(file);
                    Path manifestFilePath = SnapshotPackage.getZipEntryPath(zipFile, "manifest.json");
                    String zipEntryContent = new String(Files.readAllBytes(manifestFilePath), StandardCharsets.UTF_8);
                    JSONObject manifestJSON = new JSONObject(zipEntryContent);
                    String expectedManifestChecksum = "";
                    Path manifestCRCFilePath = null;
                    try {
                        manifestCRCFilePath = SnapshotPackage.getZipEntryPath(zipFile, "manifest.crc");
                        expectedManifestChecksum = new String(Files.readAllBytes(manifestCRCFilePath), StandardCharsets.UTF_8);
                        expectedManifestChecksum = StringUtil.padRight(expectedManifestChecksum, 100, '*');
                    }
                    catch (SapphireException e) {
                        Trace.logError("Couldn't find manifest.crc in package. Possibly an old package. Trying to get Checksum info from manifest.json.", e);
                        expectedManifestChecksum = manifestJSON.getString("checksum");
                        expectedManifestChecksum = expectedManifestChecksum.substring(10, 110);
                    }
                    String actualManifestChecksum = "";
                    actualManifestChecksum = manifestCRCFilePath != null ? ExportXML.generateCheckSum(manifestFilePath.toFile(), false, "", "") : (zipEntryContent.indexOf("<checksum>") > -1 && zipEntryContent.indexOf("<checksume>") > -1 ? ExportXML.generateCheckSum(manifestFilePath.toFile(), false, "<checksum>", "<checksume>") : ExportXML.generateCheckSum(manifestFilePath.toFile(), false, CHECKSUM_START, CHECKSUM_END));
                    actualManifestChecksum = StringUtil.padRight(actualManifestChecksum, 100, '*');
                    snapshotPackage = new SnapshotPackage(manifestJSON.getBoolean("isFromChangeLog"));
                    if (!actualManifestChecksum.equals(expectedManifestChecksum)) {
                        Trace.logDebug("Manifest Checksum failed. Expected Checksum: " + expectedManifestChecksum + ", Actual Checksum: " + actualManifestChecksum);
                        snapshotPackage.isManifestCorrupted = true;
                    }
                    snapshotPackage.setUUID(manifestJSON.getString("uuid"));
                    snapshotPackage.setCreateTool(CREATETOOL_ZIP);
                    snapshotPackage.setZipFile(file);
                    JSONArray snapshotsArr = manifestJSON.getJSONArray("snapshots");
                    for (int i = 0; i < snapshotsArr.length(); ++i) {
                        JSONObject snapshotInfo = snapshotsArr.getJSONObject(i);
                        boolean isRequested = snapshotInfo.getBoolean("isRequested");
                        String snapshotType = snapshotInfo.getString("type");
                        String id = snapshotInfo.getString("id");
                        String imageSource = snapshotInfo.getString("imageSource");
                        boolean isNullSnapshot = false;
                        if (snapshotInfo.has("nullSnapshot")) {
                            isNullSnapshot = true;
                        }
                        boolean isDeleted = false;
                        if (snapshotInfo.has("isDeleted")) {
                            isDeleted = snapshotInfo.getBoolean("isDeleted");
                        }
                        Snapshot snapshot = null;
                        Path snapshotFilePath = null;
                        if (!isNullSnapshot) {
                            String snapshotFileName = snapshotInfo.getString("fileName");
                            snapshotFilePath = SnapshotPackage.getZipEntryPath(zipFile, snapshotFileName);
                            snapshot = Snapshot.fromXML(snapshotFilePath.toFile(), connectionId, rakFile);
                        }
                        SDISnapshotItem currSnapshotItem = null;
                        Snapshot.Type type = Snapshot.Type.valueOf(snapshotType);
                        switch (type) {
                            case SDI: {
                                currSnapshotItem = new SDISnapshotItem(id);
                                break;
                            }
                            case PROPERTYTREE: {
                                PropertyTreeSnapshotItem psi = new PropertyTreeSnapshotItem(id);
                                currSnapshotItem = psi;
                                if (!snapshotInfo.has("isRenamed")) break;
                                psi.setRenamed(snapshotInfo.getBoolean("isRenamed"));
                                if (!psi.isRenamed()) break;
                                psi.setRenamedNodeId(snapshotInfo.getString("renamedNodeId"));
                            }
                        }
                        if (IMAGESOURCE_PRE.equals(imageSource)) {
                            snapshotPackage.addPreSnapshot(isNullSnapshot ? currSnapshotItem : snapshot.getSnapshotItem(), snapshot);
                        } else {
                            snapshotPackage.addSnapshot(isNullSnapshot ? currSnapshotItem : snapshot.getSnapshotItem(), snapshot, isRequested);
                        }
                        if (!isNullSnapshot && !snapshotPackage.isContentCorrupted()) {
                            String actualChecksum = ExportXML.generateCheckSum(snapshotFilePath.toFile(), false);
                            String expectedChecksum = snapshotInfo.getString("checksum");
                            if (!expectedChecksum.equals(actualChecksum)) {
                                snapshotPackage.isContentCorrupted = true;
                            }
                            if (!isDeleted) {
                                SnapshotPackage.validateAttachmentChecksum(snapshotPackage, manifestJSON, snapshot.getSnapshotItem(), connectionId);
                            }
                        }
                        if (!isDeleted) continue;
                        currSnapshotItem.setDeleted(true);
                    }
                    zipFile.close();
                    return snapshotPackage;
                }
                snapshotPackage = SnapshotPackage.fromXML(file);
            }
            catch (Exception e) {
                throw new SapphireException(e);
            }
        }
        return snapshotPackage;
    }

    private static void validateAttachmentChecksum(SnapshotPackage snapshotPackage, JSONObject manifestJSON, SnapshotItem snapshotItem, String connectionId) throws SapphireException {
        block5: {
            SDISnapshotItem sdiSnapshotItem = (SDISnapshotItem)snapshotItem;
            try {
                SDIData sdiData = sdiSnapshotItem.getSDIData();
                DataSet attachments = sdiData.getDataset("attachment");
                if (attachments == null || attachments.getRowCount() <= 0 || !manifestJSON.has("attachments")) break block5;
                JSONArray attachmentsInfo = manifestJSON.getJSONArray("attachments");
                for (int i = 0; i < attachments.getRowCount(); ++i) {
                    String cmtAttachmentId = attachments.getString(i, CMTATTACHMENTID, "");
                    if (cmtAttachmentId.length() == 0) continue;
                    int attachmentNum = attachments.getInt(i, "attachmentnum");
                    Attachment attachment = Attachment.getAttachment(attachments, i, connectionId);
                    if (attachment == null || CMTUtil.EXCLUDED_ATTACHMENT_TYPES.contains(attachment.getType())) continue;
                    String attChecksum = "";
                    for (int j = 0; j < attachmentsInfo.length(); ++j) {
                        JSONObject attachmentInfo = attachmentsInfo.getJSONObject(j);
                        if (!cmtAttachmentId.equals(attachmentInfo.getString("attachmentid"))) continue;
                        attChecksum = attachmentInfo.getString("checksum");
                    }
                    if (attChecksum == null || attChecksum.length() <= 0) continue;
                    InputStream attStream = sdiSnapshotItem.getAttachmentAsStream(attachmentNum);
                    if (attStream == null) {
                        throw new SapphireException("Can't access Attachment file in the Package. Might be corrupted. Id: " + cmtAttachmentId);
                    }
                    String attCurrChecksum = ExportXML.generateCheckSum(attStream, null, "", "");
                    if (attChecksum.equals(attCurrChecksum)) continue;
                    snapshotPackage.isContentCorrupted = true;
                    break;
                }
            }
            catch (JSONException e) {
                throw new SapphireException("Exception occurred when trying to validate Checksum for Attachments: " + e.getMessage(), e);
            }
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static Path getZipEntryPath(ZipFile zipFile, String zipEntryName) throws SapphireException {
        ZipEntry zipEntry = zipFile.getEntry(zipEntryName);
        try (InputStream zipEntryStream = zipFile.getInputStream(zipEntry);){
            Path tempPath = Paths.get(FileManager.TempFile.createTempDir(), new String[0]).resolve(zipEntryName + ".tmp");
            Files.copy(zipEntryStream, tempPath, StandardCopyOption.REPLACE_EXISTING);
            tempPath.toFile().deleteOnExit();
            Path path = tempPath;
            return path;
        }
        catch (Exception e) {
            throw new SapphireException("Exception occurred when trying to read Zip Entry. Package seems to be corrupted.");
        }
    }
}

