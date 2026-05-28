/*
 * Decompiled with CFR 0.152.
 */
package sapphire.attachment;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.attachment.AttachmentRule;
import com.labvantage.sapphire.attachment.HashedAttachmentInputStream;
import com.labvantage.sapphire.attachment.NetworkAttachmentRepository;
import com.labvantage.sapphire.attachment.StandardAttachmentRepository;
import com.labvantage.sapphire.gwt.shared.JSONable;
import com.labvantage.sapphire.modules.sdms.collector.SDMSCollector;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.LabVantageSecurityManager;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileTransfer;
import com.labvantage.sapphire.util.file.FileTransferOptions;
import com.labvantage.sapphire.util.file.FileType;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Blob;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.attachment.BaseAttachmentRepository;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.LogContext;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public abstract class Attachment
implements Serializable,
JSONable {
    public static final String TYPE_UPLOADANDSTORE = "S";
    public static final String TYPE_STORE = "F";
    public static final String TYPE_REFERENCE = "R";
    public static final String TYPE_UPLOADANDREFERENCE = "U";
    public static final String TYPE_URL = "L";
    public static final String TYPE_PLAINTEXT = "P";
    public static final String TYPE_LINKEDREFERENCE = "D";
    public static final String TYPE_FORMATTEDTEXT = "M";
    private transient Path localfile = null;
    private String sdiattachmentid;
    private String parentDepartment = null;
    private String sdcid;
    private String keyid1;
    private String keyid2;
    private String keyid3;
    private int attachmentnum = -1;
    private String attachmentClass;
    private String linksdcid;
    private String linkkeyid1;
    private String linkkeyid2;
    private String linkkeyid3;
    private int linkattachmentnum = -1;
    private String type;
    private String url;
    private String tempid;
    private String oleClass;
    private String filename;
    private String clob;
    private byte[] data;
    private long size = -1L;
    private String description;
    private String sourcefilename;
    private String thumbnailimage;
    private long datahash = 0L;
    private boolean encrypted = false;
    private boolean compressed = false;
    private boolean invalidHash = false;
    private boolean lockAttachment = false;
    private boolean allowlocalcache = true;
    private boolean triggerBusinessRule = true;
    private String uploadTo;
    private transient BaseAttachmentRepository attachmentRepository = null;
    private String attRepository;
    private String attRepositoryNodeId;
    private transient AttachmentRule attachmentRule = null;
    private String repositoryid = null;
    private PropertyList additionalColumns = new PropertyList();
    private transient Blob blob;
    private transient InputStream inputStream;

    public static Attachment getAttachment(DataSet sdiAttachmentData, String sdcid, String keyid1, String keyid2, String keyid3, int attachmentNum, String connectionId) {
        if (sdiAttachmentData == null || sdiAttachmentData.getRowCount() == 0) {
            return null;
        }
        HashMap<String, Object> findMap = new HashMap<String, Object>();
        findMap.put("sdcid", sdcid);
        findMap.put("keyid1", keyid1);
        findMap.put("keyid2", keyid2);
        findMap.put("keyid3", keyid3);
        findMap.put("attachmentnum", new BigDecimal(attachmentNum));
        int sdiAttRow = sdiAttachmentData.findRow(findMap);
        if (sdiAttRow > -1) {
            return Attachment.getAttachment(sdiAttachmentData, sdiAttRow, connectionId);
        }
        return null;
    }

    public static Attachment getAttachment(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentNum, QueryProcessor queryProcessor, String connectionId) {
        DataSet sdiAttachmentData = queryProcessor.getPreparedSqlDataSet("SELECT a.* FROM sdiattachment a WHERE a.sdcid=? AND a.keyid1=? AND a.keyid2=? AND a.keyid3=? AND a.attachmentnum=?", new Object[]{sdcid, keyid1, keyid2, keyid3, attachmentNum}, true);
        if (sdiAttachmentData != null && sdiAttachmentData.getRowCount() > 0) {
            return Attachment.getAttachment(sdiAttachmentData, 0, connectionId);
        }
        return null;
    }

    protected boolean getTriggerBusinessRule() {
        return this.triggerBusinessRule;
    }

    protected void setParentDepartment(String parentDepartment) {
        this.parentDepartment = parentDepartment;
    }

    protected String getParentDepartment() {
        return this.parentDepartment;
    }

    protected void setTriggerBusinessRule(boolean triggerBusinessRule) {
        this.triggerBusinessRule = triggerBusinessRule;
    }

    protected String getSDIAttachmentId() {
        return this.sdiattachmentid;
    }

    protected void setSDIAttachmentId(String sdiattachmentid) {
        this.sdiattachmentid = sdiattachmentid;
    }

    protected Path getLocalFile() {
        return this.localfile;
    }

    protected Path getCachedPath(String databaseid, boolean explodedzip) {
        String cachedFile = (String)CacheUtil.get(databaseid, explodedzip ? "AttachmentExplodedPath" : "AttachmentLocalFile", this.getSDCId() + ";" + this.getKeyId1() + ";" + this.getKeyId2() + ";" + this.getKeyId3() + ";" + this.getAttachmentNum());
        if (cachedFile != null) {
            Path p = Paths.get(cachedFile, new String[0]);
            if (Files.exists(p, new LinkOption[0])) {
                return p;
            }
            return null;
        }
        return null;
    }

    protected void loadLocalFile(Path p) {
        if (p != null && Files.exists(p, new LinkOption[0])) {
            try {
                this.localfile = p;
                this.setInputStream(Files.newInputStream(p, new OpenOption[0]));
            }
            catch (Exception e) {
                this.localfile = null;
                this.setInputStream(null);
            }
        } else {
            this.localfile = null;
            this.setInputStream(null);
        }
    }

    public void setAdditionalColumns(PropertyList additionalColumns, String connectionid) {
        if (additionalColumns.containsKey("data")) {
            AttachmentType t = AttachmentType.getTypeByAttachmentTypeFlag(additionalColumns.getProperty("typeflag", additionalColumns.getProperty("type")));
            if (t == AttachmentType.PLAINTEXT || t == AttachmentType.RICHTEXT) {
                this.setClob(additionalColumns.getProperty("data"));
            } else if (t == AttachmentType.URL) {
                this.setUrl(additionalColumns.getProperty("data"));
            } else {
                String mime = "";
                mime = additionalColumns.containsKey("mime") ? additionalColumns.getProperty("mime", "") : FileType.getFileType(additionalColumns.getProperty("filename", additionalColumns.getProperty("sourcefilename")), connectionid).getMime();
                FileManager.FileData fileData = new FileManager.FileData(additionalColumns.getProperty("data"), mime);
                this.setData(fileData.getData());
            }
            if (additionalColumns.containsKey("mime")) {
                additionalColumns.remove("mime");
            }
            additionalColumns.remove("data");
        }
        DataSet dataSet = new DataSet();
        for (Object key : additionalColumns.keySet()) {
            if (key.toString().equalsIgnoreCase("attachmentclob") || key.toString().equalsIgnoreCase("thumbnailimage")) {
                dataSet.addColumn(key.toString(), 3);
                continue;
            }
            if (key.toString().equalsIgnoreCase("attachmentnum") || key.toString().equalsIgnoreCase("linkattachmentnum") || key.toString().equalsIgnoreCase("contentrevision")) {
                dataSet.addColumn(key.toString(), 1);
                continue;
            }
            dataSet.addColumn(key.toString(), 0);
        }
        dataSet.addRow();
        for (Object key : additionalColumns.keySet()) {
            try {
                dataSet.setValue(0, key.toString(), additionalColumns.getProperty(key.toString()));
            }
            catch (Exception exception) {}
        }
        this.setAdditionalColumns(dataSet, 0);
    }

    public void setAdditionalColumns(DataSet sdiAttachmentData, int sdiAttRow) {
        for (int c = 0; c < sdiAttachmentData.getColumnCount(); ++c) {
            String columnid = sdiAttachmentData.getColumnId(c);
            try {
                if (columnid.equalsIgnoreCase("sdcid")) {
                    this.setSDCId(sdiAttachmentData.getValue(sdiAttRow, "sdcid", ""));
                    continue;
                }
                if (columnid.equalsIgnoreCase("sdiattachmentid")) {
                    this.setSDIAttachmentId(sdiAttachmentData.getValue(sdiAttRow, "sdiattachmentid", ""));
                    continue;
                }
                if (columnid.equalsIgnoreCase("keyid1")) {
                    this.setKeyId1(sdiAttachmentData.getValue(sdiAttRow, "keyid1", ""));
                    continue;
                }
                if (columnid.equalsIgnoreCase("keyid2")) {
                    this.setKeyId2(sdiAttachmentData.getValue(sdiAttRow, "keyid2", ""));
                    continue;
                }
                if (columnid.equalsIgnoreCase("keyid3")) {
                    this.setKeyId3(sdiAttachmentData.getValue(sdiAttRow, "keyid3", ""));
                    continue;
                }
                if (columnid.equalsIgnoreCase("attachmentnum")) {
                    this.setAttachmentNum(sdiAttachmentData.getInt(sdiAttRow, "attachmentnum", -1));
                    continue;
                }
                if (columnid.equalsIgnoreCase("attachmentdesc")) {
                    this.setDescription(sdiAttachmentData.getValue(sdiAttRow, "attachmentdesc"));
                    continue;
                }
                if (columnid.equalsIgnoreCase("description")) {
                    this.setDescription(sdiAttachmentData.getValue(sdiAttRow, "description"));
                    continue;
                }
                if (columnid.equalsIgnoreCase("typeflag")) {
                    this.setType(sdiAttachmentData.getValue(sdiAttRow, "typeflag"));
                    continue;
                }
                if (columnid.equalsIgnoreCase("type")) {
                    this.setType(sdiAttachmentData.getValue(sdiAttRow, "type"));
                    continue;
                }
                if (columnid.equalsIgnoreCase("filename")) {
                    this.setFilename(sdiAttachmentData.getValue(sdiAttRow, "filename"));
                    continue;
                }
                if (columnid.equalsIgnoreCase("__tempid")) {
                    this.setTempId(sdiAttachmentData.getValue(sdiAttRow, "__tempid", ""));
                    continue;
                }
                if (columnid.equalsIgnoreCase("__uploadto")) {
                    this.setUploadTo(sdiAttachmentData.getValue(sdiAttRow, "__uploadto"));
                    continue;
                }
                if (columnid.equalsIgnoreCase("oleclass")) {
                    this.setOleClass(sdiAttachmentData.getValue(sdiAttRow, "oleclass"));
                    continue;
                }
                if (columnid.equalsIgnoreCase("sourcefilename")) {
                    this.setSourceFilename(sdiAttachmentData.getValue(sdiAttRow, "sourcefilename", ""));
                    continue;
                }
                if (columnid.equalsIgnoreCase("attachmentclass")) {
                    this.setAttachmentClass(sdiAttachmentData.getValue(sdiAttRow, "attachmentclass"));
                    continue;
                }
                if (columnid.equalsIgnoreCase("attachmentsize")) {
                    try {
                        this.setSize(sdiAttachmentData.getLong(sdiAttRow, "attachmentsize"));
                    }
                    catch (Exception exception) {}
                    continue;
                }
                if (columnid.equalsIgnoreCase("thumbnailimage")) {
                    this.setThumbnailImage(sdiAttachmentData.getValue(sdiAttRow, "thumbnailimage"));
                    continue;
                }
                if (columnid.equalsIgnoreCase("attachment")) {
                    this.setBlob((Blob)sdiAttachmentData.getObject(sdiAttRow, "attachment"));
                    continue;
                }
                if (columnid.equalsIgnoreCase("attachmentrepositoryid")) {
                    this.attRepository = sdiAttachmentData.getValue(sdiAttRow, "attachmentrepositoryid");
                    continue;
                }
                if (columnid.equalsIgnoreCase("attachmentrepositorynodeid")) {
                    this.attRepositoryNodeId = sdiAttachmentData.getValue(sdiAttRow, "attachmentrepositorynodeid");
                    continue;
                }
                if (columnid.equalsIgnoreCase("externalrepository")) {
                    this.repositoryid = sdiAttachmentData.getValue(sdiAttRow, "externalrepository");
                    continue;
                }
                if (columnid.equalsIgnoreCase("linksdcid")) {
                    this.setLinkSDI(sdiAttachmentData.getValue(sdiAttRow, "linksdcid"), sdiAttachmentData.getValue(sdiAttRow, "linkkeyid1"), sdiAttachmentData.getValue(sdiAttRow, "linkkeyid2"), sdiAttachmentData.getValue(sdiAttRow, "linkkeyid3"), sdiAttachmentData.getInt(sdiAttRow, "linkattachmentnum"));
                    continue;
                }
                if (columnid.equalsIgnoreCase("datahash")) {
                    try {
                        this.setDataHash(Long.parseLong(sdiAttachmentData.getValue(sdiAttRow, "datahash")));
                    }
                    catch (Exception e) {
                        this.setDataHash(0L);
                    }
                    continue;
                }
                if (columnid.equalsIgnoreCase("compressedflag")) {
                    this.setCompressed(sdiAttachmentData.getValue(sdiAttRow, "compressedflag").equalsIgnoreCase("Y"));
                    continue;
                }
                if (columnid.equalsIgnoreCase("encryptedflag")) {
                    this.setEncrypted(sdiAttachmentData.getValue(sdiAttRow, "encryptedflag").equalsIgnoreCase("Y"));
                    continue;
                }
                if (columnid.equalsIgnoreCase("__rowstatus") || columnid.equalsIgnoreCase("__rowid") || columnid.equalsIgnoreCase("__rsetseq") || columnid.equalsIgnoreCase("moddt") || columnid.equalsIgnoreCase("tracelogid") || columnid.equalsIgnoreCase("createdt") || columnid.equalsIgnoreCase("modby") || columnid.equalsIgnoreCase("createtool") || columnid.equalsIgnoreCase("createby") || columnid.equalsIgnoreCase("__lockedby") || columnid.equalsIgnoreCase("modtool") || columnid.equalsIgnoreCase("__lockstate") || columnid.equalsIgnoreCase("contentrevision") || columnid.equalsIgnoreCase("__markup") || columnid.equalsIgnoreCase("sdiattachmentid") || columnid.equalsIgnoreCase("__checkedoutbydepartment") || columnid.equalsIgnoreCase("__checkedoutbyuser") || columnid.equalsIgnoreCase("linkkeyid1") || columnid.equalsIgnoreCase("linkkeyid2") || columnid.equalsIgnoreCase("linkkeyid3") || columnid.equalsIgnoreCase("linkattachmentnum")) continue;
                if (columnid.equalsIgnoreCase("url")) {
                    String url = sdiAttachmentData.getValue(sdiAttRow, "url", "");
                    this.setUrl(url);
                    continue;
                }
                if (columnid.equalsIgnoreCase("attachmentclob")) {
                    this.setClob(sdiAttachmentData.getClob(sdiAttRow, "attachmentclob", ""));
                    continue;
                }
                String value = sdiAttachmentData.getValue(sdiAttRow, columnid, "");
                value = StringUtil.replaceAll(value, ";", "#semicolon#");
                this.setAdditionalColumn(columnid, value);
                continue;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    protected AttachmentRule getAttachmentRule() {
        return this.attachmentRule;
    }

    protected void setAdditionalColumn(String key, String value) {
        if (this.additionalColumns == null) {
            this.additionalColumns = new PropertyList();
        }
        this.additionalColumns.put(key, value);
    }

    protected PropertyList getAdditionalColumns() {
        return this.additionalColumns;
    }

    protected void setSDCId(String sdcid) {
        this.sdcid = sdcid;
    }

    protected void setKeyId1(String keyid1) {
        this.keyid1 = keyid1;
    }

    protected void setKeyId2(String keyid2) {
        this.keyid2 = keyid2;
    }

    protected void setKeyId3(String keyid3) {
        this.keyid3 = keyid3;
    }

    protected void setAttachmentNum(int attnum) {
        this.attachmentnum = attnum;
    }

    protected void setAttachmentRepositoryId(String attachmentRepositoryId) {
        this.attRepository = attachmentRepositoryId;
    }

    protected void setAttachmentRepositoryNodeId(String attachmentRepositoryNodeId) {
        this.attRepositoryNodeId = attachmentRepositoryNodeId;
    }

    protected String getAttachmentRepositoryId() {
        return this.attRepository;
    }

    protected String getAttachmentRepositoryNodeId() {
        return this.attRepositoryNodeId;
    }

    protected void setType(String type) {
        this.type = type;
    }

    public void setUploadTo(String uploadTo) {
        this.uploadTo = uploadTo;
    }

    public String getUploadTo() {
        return this.uploadTo;
    }

    public void setSize(long size) {
        this.size = size;
    }

    protected void setLinkSDI(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentnum) {
        this.linksdcid = sdcid;
        this.linkkeyid1 = keyid1;
        this.linkkeyid2 = keyid2;
        this.linkkeyid3 = keyid3;
        this.linkattachmentnum = attachmentnum;
    }

    protected long getDataHash() {
        return this.datahash;
    }

    protected boolean checkHash(long hash) {
        if (this.isHashed()) {
            this.invalidHash = hash != this.datahash;
        } else {
            this.setDataHash(hash);
            this.invalidHash = false;
        }
        return !this.invalidHash;
    }

    protected void setInvalidHash(boolean invalidHash) {
        this.invalidHash = invalidHash;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected Path saveLocalFile(boolean cache, String databaseid, boolean checkHash, boolean generateHash, Path usepath, boolean doDeleteTempFile) {
        Path tempFile = null;
        try {
            String ext = this.getSourceFilename() != null && this.getSourceFilename().length() > 0 ? FileManager.getExtension(this.getSourceFilename()) : "cache";
            Path p = usepath;
            if (usepath == null) {
                p = tempFile = FileUtil.createTempFile("attachmentlocal" + (cache ? "cache" : "file"), "." + ext);
            }
            InputStream inputStream = null;
            if (checkHash && this.isHashed()) {
                if (this.datahash == -1L) {
                    inputStream = this.getInputStream();
                    this.invalidHash = true;
                } else {
                    this.invalidHash = false;
                    inputStream = FileTransfer.getHashedInputSteam(this, SDMSCollector.defaultHashingAlgorithm);
                }
            } else if (!this.isHashed() && generateHash) {
                this.datahash = 0L;
                this.invalidHash = false;
                inputStream = FileTransfer.getHashedInputSteam(this, SDMSCollector.defaultHashingAlgorithm);
            } else {
                this.invalidHash = false;
                inputStream = this.getInputStream();
            }
            FileTransferOptions fileTransferOptions = new FileTransferOptions();
            fileTransferOptions.setCloseInputStream(true);
            fileTransferOptions.setCloseOutputStream(true);
            fileTransferOptions.setReplaceTarget(true);
            FileTransfer.safeDataTransfer(inputStream, p.toFile(), fileTransferOptions);
            if (cache && usepath == null) {
                p.toFile().deleteOnExit();
                this.setInputStream(Files.newInputStream(p, new OpenOption[0]));
                CacheUtil.put(databaseid, "AttachmentLocalFile", this.getSDCId() + ";" + this.getKeyId1() + ";" + this.getKeyId2() + ";" + this.getKeyId3() + ";" + this.getAttachmentNum(), p.toString());
            } else if (usepath == null) {
                if (doDeleteTempFile) {
                    this.setInputStream(Files.newInputStream(p, StandardOpenOption.DELETE_ON_CLOSE));
                } else {
                    this.setInputStream(Files.newInputStream(p, new OpenOption[0]));
                }
            }
            Path path = p;
            return path;
        }
        catch (Exception e) {
            Trace.logWarn("Writing local file failed:" + e.getMessage());
            Path path = null;
            return path;
        }
        finally {
            if (!cache && tempFile != null && tempFile.toFile().exists()) {
                try {
                    if (doDeleteTempFile) {
                        FileUtil.deleteAll(tempFile.toFile());
                    } else {
                        tempFile.toFile().deleteOnExit();
                    }
                }
                catch (Exception e) {
                    Trace.logError("Failed to remove file.", e);
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected boolean checkHash() {
        if (this.isHashed()) {
            if (this.datahash == -1L) {
                this.invalidHash = true;
            } else {
                Path temp = null;
                try {
                    HashedAttachmentInputStream digestInputStream = FileTransfer.getHashedInputSteam(this, SDMSCollector.defaultHashingAlgorithm);
                    temp = FileUtil.createTempFile("hashcheck", ".tmp");
                    FileTransferOptions fto = new FileTransferOptions();
                    fto.setCloseOutputStream(true);
                    fto.setCloseInputStream(true);
                    fto.setReplaceTarget(true);
                    FileTransfer.safeDataTransfer((InputStream)digestInputStream, temp.toFile(), fto);
                    this.setInputStream(Files.newInputStream(temp, StandardOpenOption.DELETE_ON_CLOSE));
                }
                catch (Exception e) {
                    Trace.logWarn("Hash check failed:" + e.getMessage());
                }
                finally {
                    if (temp != null && temp.toFile().exists()) {
                        try {
                            FileUtil.deleteAll(temp.toFile());
                        }
                        catch (Exception e) {
                            Trace.logError("Failed to remove file.", e);
                        }
                    }
                }
            }
        } else {
            this.invalidHash = false;
        }
        return !this.invalidHash;
    }

    protected void setUrl(String url) {
        this.url = url;
    }

    protected boolean getAllowLocalCache() {
        return this.allowlocalcache;
    }

    protected void setAllowLocalCache(boolean allowlocalcache) {
        this.allowlocalcache = allowlocalcache;
    }

    protected BaseAttachmentRepository getAttachmentRepository(SapphireConnection sapphireConnection, PropertyList attachmentPolicy, boolean defaultToCompatability) throws SapphireException {
        return this.getAttachmentRepository(sapphireConnection, attachmentPolicy, defaultToCompatability, null);
    }

    protected BaseAttachmentRepository getAttachmentRepository(SapphireConnection sapphireConnection, PropertyList attachmentPolicy, boolean defaultToCompatability, Attachment basedOn) throws SapphireException {
        if (basedOn != null) {
            if (basedOn.attachmentRepository != null && this.attachmentRepository == null) {
                this.attachmentRepository = basedOn.attachmentRepository;
            }
            if (basedOn.repositoryid != null && basedOn.repositoryid.length() > 0 && (this.repositoryid == null || this.repositoryid.length() == 0)) {
                this.repositoryid = basedOn.repositoryid;
            }
            if (basedOn.attRepository != null && basedOn.attRepository.length() > 0 && (this.attRepository == null || this.attRepository.length() == 0)) {
                this.attRepository = basedOn.attRepository;
            }
            if (basedOn.attRepositoryNodeId != null && basedOn.attRepositoryNodeId.length() > 0 && (this.attRepositoryNodeId == null || this.attRepositoryNodeId.length() == 0)) {
                this.attRepositoryNodeId = basedOn.attRepositoryNodeId;
            }
            if (basedOn.getAttachmentType() != null && this.getAttachmentType() == null) {
                this.setAttachmentType(basedOn.getAttachmentType());
            }
        }
        if (this.attachmentRepository == null) {
            if (this.getAttachmentType() == AttachmentType.FILE) {
                if ((this.attRepository == null || this.attRepository.length() == 0) && defaultToCompatability) {
                    this.attachmentRepository = new StandardAttachmentRepository();
                    this.attachmentRepository.setConnectionId(sapphireConnection.getConnectionId());
                    this.attachmentRepository.setSapphireConnection(sapphireConnection);
                    if (this.getType().equalsIgnoreCase(TYPE_REFERENCE)) {
                        this.attachmentRepository.setManaged(false);
                    }
                } else if ((this.attRepository == null || this.attRepository.length() == 0) && this.getType().equalsIgnoreCase(TYPE_REFERENCE) && !this.hasData() && this.filename != null && this.filename.length() > 0) {
                    this.attachmentRepository = new NetworkAttachmentRepository();
                    this.attachmentRepository.setConnectionId(sapphireConnection.getConnectionId());
                    this.attachmentRepository.setSapphireConnection(sapphireConnection);
                    this.attachmentRepository.setManaged(false);
                } else if (this.attRepository != null && this.attRepository.length() > 0) {
                    this.attachmentRepository = BaseAttachmentRepository.getRepository(this.attRepository, this.attRepositoryNodeId, sapphireConnection);
                } else {
                    if (this.attachmentRule == null && attachmentPolicy != null) {
                        this.attachmentRule = AttachmentRule.evaluateRule(this.filename, this.attachmentClass, this.size, this.parentDepartment, attachmentPolicy.getCollectionNotNull("classes"), new ConfigurationProcessor(sapphireConnection.getConnectionId()));
                    }
                    if (this.attachmentRule != null) {
                        this.attRepository = this.attachmentRule.getFileRepositoryId();
                        this.attRepositoryNodeId = this.attachmentRule.getFileRepositoryNodeId();
                    }
                    if (this.attRepository == null || this.attRepository.length() == 0) {
                        this.attRepository = "NetworkFileRepository";
                    }
                    if (this.attRepositoryNodeId == null || this.attRepositoryNodeId.length() == 0) {
                        this.attRepositoryNodeId = "Sapphire Custom";
                    }
                    this.attachmentRepository = BaseAttachmentRepository.getRepository(this.attRepository, this.attRepositoryNodeId, sapphireConnection);
                }
            } else {
                this.attachmentRepository = new StandardAttachmentRepository();
                this.attachmentRepository.setConnectionId(sapphireConnection.getConnectionId());
                this.attachmentRepository.setSapphireConnection(sapphireConnection);
            }
            if (this.attachmentRepository != null) {
                this.attachmentRepository.setAttachmentPolicy(attachmentPolicy);
            }
        }
        if (attachmentPolicy == null) {
            throw new SapphireException("Could not obtain repository");
        }
        if (this.attachmentRepository != null && this.attachmentRepository instanceof NetworkAttachmentRepository && this.attachmentRepository.getRepositoryProperties() != null) {
            LabVantageSecurityManager.addFilePathException(this.attachmentRepository.getRepositoryProperties().getProperty("locationpath"), true, true, true);
        }
        return this.attachmentRepository;
    }

    protected boolean isZippingRequired(PropertyList attachmentPolicy, ConfigurationProcessor cp) {
        if (this.attachmentRule == null && attachmentPolicy != null) {
            this.attachmentRule = AttachmentRule.evaluateRule(this.filename, this.attachmentClass, this.size, this.parentDepartment, attachmentPolicy.getCollectionNotNull("classes"), cp);
        }
        if (this.attachmentRule != null) {
            return this.attachmentRule.isZippingRequired();
        }
        return false;
    }

    protected boolean isEncryptionRequired(PropertyList attachmentPolicy, ConfigurationProcessor cp) {
        if (this.attachmentRule == null && attachmentPolicy != null) {
            this.attachmentRule = AttachmentRule.evaluateRule(this.filename, this.attachmentClass, this.size, this.parentDepartment, attachmentPolicy.getCollectionNotNull("classes"), cp);
        }
        if (this.attachmentRule != null) {
            return this.attachmentRule.isEncryptionRequired();
        }
        return false;
    }

    protected BaseAttachmentRepository getAttachmentRepository(SapphireConnection sapphireConnection, PropertyList attachmentPolicy) throws SapphireException {
        return this.getAttachmentRepository(sapphireConnection, attachmentPolicy, false);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        if (this.data == null || this.data.length == 0) {
            if (this.inputStream != null) {
                try {
                    this.data = FileManager.getBinaryData(this.inputStream, -1);
                }
                catch (Exception e) {
                    throw new IOException(e);
                }
                finally {
                    this.inputStream.close();
                }
            }
            if (this.blob != null) {
                try {
                    this.data = FileManager.getBinaryData(this.blob.getBinaryStream(), -1);
                }
                catch (Exception e) {
                    throw new IOException(e);
                }
                finally {
                    this.inputStream.close();
                }
            }
        }
        try {
            out.defaultWriteObject();
        }
        catch (Exception e) {
            Trace.logError(e.getMessage(), e);
            throw new IOException(e.getMessage(), e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private byte[] getData(int maxsizelimit, boolean closeInputStream) {
        byte[] byArray;
        if (this.data != null) {
            return this.data;
        }
        try {
            byArray = FileManager.getBinaryData(this.getInputStream(), maxsizelimit);
        }
        catch (Throwable throwable) {
            try {
                this.getInputStream().close();
                throw throwable;
            }
            catch (Exception e) {
                Logger.logError("Could not read attachment stream.", e);
                return null;
            }
        }
        this.getInputStream().close();
        return byArray;
    }

    private void setDataHash(long dataHash) {
        this.datahash = dataHash;
    }

    public PropertyList toPropertyList(boolean returnData, boolean thumbnail, boolean additionalColumns, String connectionid) throws SapphireException {
        PropertyList p = new PropertyList();
        this.toPropertyList(p, returnData, thumbnail, additionalColumns, connectionid);
        return p;
    }

    @Override
    public String toJSONString() {
        try {
            return this.toPropertyList(false, false, true, "").toJSONString();
        }
        catch (Exception exception) {
            return null;
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void toPropertyList(PropertyList properties, boolean returnData, boolean thumbnail, boolean returnAdditionalColumns, String connectionid) throws SapphireException {
        properties.setProperty("sdiattachmentid", this.getSDIAttachmentId());
        properties.setProperty("sdcid", this.getSDCId());
        properties.setProperty("keyid1", this.getKeyId1());
        properties.setProperty("keyid2", this.getKeyId2());
        properties.setProperty("keyid3", this.getKeyId3());
        properties.setProperty("attachmentnum", this.getAttachmentNum() + "");
        properties.setProperty("attachmentclass", this.getAttachmentClass());
        properties.setProperty("attachmentsize", "" + this.getSize());
        properties.setProperty("oleclass", this.getOleClass());
        properties.setProperty("sourcefilename", this.getSourceFilename());
        if (thumbnail) {
            properties.setProperty("thumbnailimage", this.getThumbnailImage());
        }
        properties.setProperty("attachmentrepositoryid", this.attRepository != null ? this.attRepository : "");
        properties.setProperty("attachmentrepositorynodeid", this.attRepositoryNodeId != null ? this.attRepositoryNodeId : "");
        properties.setProperty("externalrepository", this.repositoryid != null ? this.repositoryid : "");
        properties.setProperty("description", this.getDescription());
        AttachmentType attachmentType = AttachmentType.getTypeByAttachmentTypeFlag(this.getType());
        properties.setProperty("type", this.type);
        if (attachmentType == AttachmentType.FILE) {
            properties.setProperty("filename", this.getFilename());
            properties.setProperty("sourcefilename", this.getSourceFilename());
            if (returnData) {
                FileManager fileManager = new FileManager();
                FileManager.FileData data = new FileManager.FileData(this.getData(), FileType.getFileType(this.getFilename(), connectionid).getMime());
                if (data == null) throw new SapphireException("ATTACHMENT_SERVICE_FAILED", "No data in attachment.");
                properties.setProperty("data", data.getBase64());
                properties.setProperty("mime", data.getMimetype());
            }
        } else if (attachmentType == AttachmentType.URL) {
            if (returnData) {
                properties.setProperty("data", this.getUrl() != null && this.getUrl().length() > 0 ? this.getUrl() : this.getClob());
                properties.setProperty("mime", FileType.getFileTypeByName("URL", connectionid).getMime());
            }
        } else if (returnData) {
            properties.setProperty("data", this.getClob());
            properties.setProperty("mime", FileType.getFileTypeByName("TXT", connectionid).getMime());
        }
        if (this.getLinkAttachmentNum() > -1 && this.getLinkSdcid() != null && this.getLinkSdcid().length() > 0 && this.getLinkKeyId1() != null && this.getLinkKeyId1().length() > 0) {
            properties.setProperty("linksdcid", this.getLinkSdcid());
            properties.setProperty("linkkeyid1", this.getLinkKeyId1());
            if (this.getLinkKeyId2() != null && this.getLinkKeyId2().length() > 0 && !this.getLinkKeyId2().equalsIgnoreCase("(null)")) {
                properties.setProperty("linkkeyid2", this.getLinkKeyId2());
            }
            if (this.getLinkKeyId3() != null && this.getLinkKeyId3().length() > 0 && !this.getLinkKeyId3().equalsIgnoreCase("(null)")) {
                properties.setProperty("linkkeyid3", this.getLinkKeyId3());
            }
            properties.setProperty("linkattachmentnum", "" + this.getLinkAttachmentNum());
        }
        properties.setProperty("datahash", "" + this.datahash);
        properties.setProperty("encryptedflag", this.encrypted ? "Y" : "N");
        properties.setProperty("compressedflag", this.compressed ? "Y" : "N");
        properties.setProperty("triggerbusinessruleflag", this.triggerBusinessRule ? "Y" : "N");
        if (!returnAdditionalColumns) return;
        for (Object p : this.additionalColumns.keySet()) {
            String k = (String)p;
            properties.setProperty(k, this.additionalColumns.getProperty(k));
        }
    }

    public static Attachment getAttachment(String sdcid, String keyid1, String keyid2, String keyid3) {
        return Attachment.getAttachment(sdcid, keyid1, keyid2, keyid3, -1);
    }

    public static Attachment getAttachment(PropertyList propertyList, String connectionId) {
        com.labvantage.sapphire.services.Attachment attachment = new com.labvantage.sapphire.services.Attachment();
        attachment.setAdditionalColumns(propertyList, connectionId);
        return attachment;
    }

    public static Attachment getAttachment(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentnum) {
        com.labvantage.sapphire.services.Attachment attachment = new com.labvantage.sapphire.services.Attachment();
        attachment.setSDCId(sdcid);
        attachment.setKeyId1(keyid1);
        if (keyid2 != null) {
            attachment.setKeyId2(keyid2);
        }
        if (keyid3 != null) {
            attachment.setKeyId3(keyid3);
        }
        if (attachmentnum > -1) {
            attachment.setAttachmentNum(attachmentnum);
        }
        return attachment;
    }

    public static Attachment getAttachment(DataSet sdiAttachmentData, int sdiAttRow, String connectionId) {
        if (sdiAttachmentData == null || sdiAttachmentData.getRowCount() == 0) {
            return null;
        }
        com.labvantage.sapphire.services.Attachment attachment = new com.labvantage.sapphire.services.Attachment();
        attachment.setAttachment(sdiAttachmentData, sdiAttRow);
        return attachment;
    }

    public void setAttachment(DataSet sdiAttachmentData, int sdiAttRow) {
        this.setAdditionalColumns(sdiAttachmentData, sdiAttRow);
    }

    public void setByReference(boolean byReference) {
        this.setType(byReference ? TYPE_REFERENCE : TYPE_STORE);
    }

    public boolean isByReference() {
        return this.getType().equalsIgnoreCase(TYPE_REFERENCE);
    }

    public void setAttachmentType(AttachmentType attachmentType) {
        this.type = attachmentType.getFlag();
    }

    public AttachmentType getAttachmentType() {
        AttachmentType type = AttachmentType.FILE;
        try {
            return AttachmentType.getTypeByAttachmentTypeFlag(this.type);
        }
        catch (Exception exception) {
            return type;
        }
    }

    public String getAdditionalColumn(String key, String defaultValue) {
        if (this.additionalColumns == null) {
            return defaultValue;
        }
        if (this.additionalColumns.containsKey(key)) {
            String v = this.additionalColumns.getProperty(key);
            return v != null && v.length() > 0 ? v : defaultValue;
        }
        return defaultValue;
    }

    public String getSDCId() {
        return this.sdcid;
    }

    public String getKeyId1() {
        return this.keyid1;
    }

    public String getKeyId2() {
        return this.keyid2 == null || this.keyid2.length() == 0 ? "(null)" : this.keyid2;
    }

    public String getKeyId3() {
        return this.keyid3 == null || this.keyid3.length() == 0 ? "(null)" : this.keyid3;
    }

    public int getAttachmentNum() {
        return this.attachmentnum;
    }

    public String getAttachmentClass() {
        return this.attachmentClass;
    }

    public String getTempId() {
        return this.tempid;
    }

    public void setTempId(String tempid) {
        this.tempid = tempid;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryid = repositoryId;
    }

    public String getRepositoryId() {
        return this.repositoryid;
    }

    public void setAttachmentClass(String attclass) {
        this.attachmentClass = attclass;
    }

    public String getType() {
        return this.type;
    }

    public String getSourceFilename() {
        return this.sourcefilename;
    }

    public void setSourceFilename(String sourcefilename) {
        this.sourcefilename = sourcefilename;
    }

    public String getOleClass() {
        return this.oleClass;
    }

    public void setOleClass(String oleClass) {
        this.oleClass = oleClass;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setData(byte[] data) {
        this.data = data;
        if (this.size < 0L && data != null) {
            try {
                this.size = data.length;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public String getFilename() {
        return this.filename;
    }

    public Blob getBlob() {
        return this.blob;
    }

    public void setBlob(Blob blob) {
        this.blob = blob;
    }

    public String getDescription() {
        return this.description;
    }

    public void setInputStream(InputStream in) {
        this.inputStream = in;
        if (this.size < 0L && in != null && this.inputStream.markSupported()) {
            try {
                this.size = in.available();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public InputStream getInputStream() {
        if (this.inputStream != null) {
            try {
                if (this.inputStream.markSupported() && this.getSize() == -1L) {
                    this.setSize(this.inputStream.available());
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            return this.inputStream;
        }
        if (this.data != null) {
            this.inputStream = new ByteArrayInputStream(this.data);
            this.setSize(this.data.length);
            return this.inputStream;
        }
        Logger.logWarn("No input stream set up for attachment.");
        return null;
    }

    public long getSize() {
        if (this.size > -1L) {
            return this.size;
        }
        if (this.inputStream != null) {
            try {
                this.size = this.inputStream.available();
                if (this.size >= Integer.MAX_VALUE) {
                    if (this.inputStream instanceof FileInputStream) {
                        Logger.logDebug("Max Integer size exceeded. Obtaining size from channel.");
                        this.size = ((FileInputStream)this.inputStream).getChannel().size();
                    } else {
                        Logger.logWarn("Max Integer size exceeded. Unable to obtain accurate size from '" + this.inputStream.getClass().getName() + "'");
                    }
                }
                return this.size;
            }
            catch (Exception e) {
                return 0L;
            }
        }
        if (this.data != null) {
            this.size = this.data.length;
            return this.size;
        }
        Logger.logWarn("No size provided for attachment.");
        return 0L;
    }

    public byte[] getData(int maxsizelimit) {
        return this.getData(maxsizelimit, true);
    }

    public boolean hasData() {
        InputStream i = this.getInputStream();
        return i != null && this.getSize() > 0L;
    }

    public byte[] getData() {
        return this.getData(-1);
    }

    public void setClob(String clob) {
        this.clob = clob;
    }

    public String getClob() {
        return this.clob;
    }

    public void setThumbnailImage(String thumbnailimage) {
        this.thumbnailimage = thumbnailimage;
    }

    public String getThumbnailImage() {
        return this.thumbnailimage;
    }

    public String getLinkSdcid() {
        return this.linksdcid;
    }

    public String getLinkKeyId1() {
        return this.linkkeyid1;
    }

    public String getLinkKeyId2() {
        return this.linkkeyid2;
    }

    public String getLinkKeyId3() {
        return this.linkkeyid3;
    }

    public int getLinkAttachmentNum() {
        return this.linkattachmentnum;
    }

    public boolean isHashed() {
        return this.datahash != 0L;
    }

    public boolean isInvalidHash() {
        return this.invalidHash;
    }

    public boolean isCompressed() {
        return this.compressed;
    }

    public void setCompressed(boolean compressedFlag) {
        this.compressed = compressedFlag;
    }

    public boolean isEncrypted() {
        return this.encrypted;
    }

    public void setEncrypted(boolean encryptedFlag) {
        this.encrypted = encryptedFlag;
    }

    public String getUrl() {
        return this.url;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void generateThumbnail(String connectionid) throws SapphireException {
        block11: {
            try {
                FileManager.FileData fileData;
                FileManager.FileData thumbnaildata;
                if (this.getInputStream() != null && this.getFilename() != null) {
                    FileManager.FileData fileData2 = new FileManager.FileData(this.getInputStream(), FileType.getFileType(this.getFilename(), connectionid).getMime(), true);
                    FileManager.FileData thumbnaildata2 = FileManager.generateThumbnail(fileData2, -1, -1, new Logger(new LogContext()), connectionid);
                    try {
                        if (thumbnaildata2 != null) {
                            this.setThumbnailImage(thumbnaildata2.getDataURL());
                        }
                        break block11;
                    }
                    finally {
                        this.setInputStream(fileData2.getInputStream());
                    }
                }
                if (this.getAttachmentType() == AttachmentType.PLAINTEXT) {
                    FileManager.FileData fileData3 = new FileManager.FileData(this.getClob().getBytes("UTF-8"), FileType.getFileTypeByName("TXT", connectionid).getMime());
                    FileManager.FileData thumbnaildata3 = FileManager.generateThumbnail(fileData3, -1, -1, new Logger(new LogContext()), connectionid);
                    if (thumbnaildata3 != null) {
                        this.setThumbnailImage(thumbnaildata3.getDataURL());
                    }
                } else if (this.getAttachmentType() == AttachmentType.RICHTEXT && (thumbnaildata = FileManager.generateThumbnail(fileData = new FileManager.FileData(this.getClob().getBytes("UTF-8"), FileType.getFileTypeByName("HTML", connectionid).getMime()), -1, -1, new Logger(new LogContext()), connectionid)) != null) {
                    this.setThumbnailImage(thumbnaildata.getDataURL());
                }
            }
            catch (Exception e) {
                throw new SapphireException(e);
            }
        }
    }

    public boolean getLockAttachment() {
        return this.lockAttachment;
    }

    public void setLockAttachment(boolean lockAttachment) {
        this.lockAttachment = lockAttachment;
    }

    public static String evaluateFileNameExpressions(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentnum, ConnectionInfo connectionInfo, String input, Attachment attachment) {
        return Attachment.evaluateFileNameExpressions(sdcid, keyid1, keyid2, keyid3, attachmentnum, -1, connectionInfo, input, attachment);
    }

    public static String evaluateFileNameExpressions(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentnum, int auditsequence, ConnectionInfo connectionInfo, String input, Attachment attachment) {
        String currentUser = connectionInfo.getSysuserId();
        if (currentUser != null && currentUser.length() > 0) {
            currentUser = currentUser.replaceAll("[^\\w.-]", "_");
        }
        if (sdcid != null && sdcid.length() > 0) {
            sdcid = sdcid.replaceAll("[^\\w.-]", "_");
        }
        if (keyid1 != null && keyid1.length() > 0) {
            keyid1 = keyid1.replaceAll("[^\\w.-]", "_");
        }
        if (keyid2 != null && keyid2.length() > 0 && !keyid2.equalsIgnoreCase("(null)")) {
            keyid2 = keyid2.replaceAll("[^\\w.-]", "_");
        }
        if (keyid3 != null && keyid3.length() > 0 && !keyid3.equalsIgnoreCase("(null)")) {
            keyid3 = keyid3.replaceAll("[^\\w.-]", "_");
        }
        String rnd = StringUtil.getRandomString(3);
        String guid = sdcid + "_" + keyid1 + "_" + (keyid2 != null && keyid2.length() > 0 && !keyid2.equalsIgnoreCase("(null)") ? keyid2 + "_" : "") + (keyid3 != null && keyid3.length() > 0 && !keyid3.equalsIgnoreCase("(null)") ? keyid3 + "_" : "") + attachmentnum + (auditsequence > -1 ? "_" + auditsequence : "") + "_" + rnd;
        if (keyid2 == null || keyid2.equalsIgnoreCase("(null)")) {
            keyid2 = "";
        }
        if (keyid3 == null || keyid3.equalsIgnoreCase("(null)")) {
            keyid3 = "";
        }
        input = Attachment.replaceDateTokens(input);
        input = Attachment.replaceDbTokens(input, sdcid, keyid1, keyid2, keyid3, attachment, connectionInfo);
        String fileName = StringUtil.replaceAll(StringUtil.replaceAll(StringUtil.replaceAll(StringUtil.replaceAll(StringUtil.replaceAll(StringUtil.replaceAll(StringUtil.replaceAll(StringUtil.replaceAll(input, "[sdcid]", sdcid, false), "[keyid1]", keyid1, false), "[keyid2]", keyid2, false), "[keyid3]", keyid3, false), "[attachmentnum]", "" + attachmentnum, false), "[currentuser]", currentUser, false), "[guid]", guid, false), "[auditsequence]", "" + auditsequence, false);
        return fileName;
    }

    public static String replaceDateTokens(String filename) {
        String[] tokens = StringUtil.getTokens(filename);
        DateTimeFormatter fullYearFormatter = DateTimeFormatter.ofPattern("yyyy");
        String fullYear = LocalDateTime.now().format(fullYearFormatter);
        DateTimeFormatter shortYearFormatter = DateTimeFormatter.ofPattern("yy");
        String shortYear = LocalDateTime.now().format(shortYearFormatter);
        DateTimeFormatter shortMonthFormatter = DateTimeFormatter.ofPattern("MM");
        String shortMonth = LocalDateTime.now().format(shortMonthFormatter);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd");
        String date = LocalDateTime.now().format(dateFormatter);
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM");
        String month = LocalDateTime.now().format(monthFormatter);
        for (String token : tokens) {
            if (token.startsWith("primary") || token.startsWith("currentuser") || token.startsWith("sdiattachment") || !token.toLowerCase().contains("yyyy") && !token.toLowerCase().contains("yy") && !token.toLowerCase().contains("dd") && !token.toLowerCase().contains("mmm") && !token.toLowerCase().contains("mmm")) continue;
            String replaceStr = token.toLowerCase().replace("yyyy", fullYear).replace("yy", shortYear).replace("dd", date).replace("mmm", month).replace("mm", shortMonth);
            replaceStr = Attachment.replaceInvalidChars(replaceStr, null);
            filename = filename.replace("[" + token + "]", replaceStr);
        }
        return filename;
    }

    public static String replaceDbTokens(String filename, String sdcid, String keyid1, String keyid2, String keyid3, Attachment attachment, ConnectionInfo connectionInfo) {
        String[] tokens;
        for (String token : tokens = StringUtil.getTokens(filename)) {
            SDCProcessor sdcProcessor;
            if (token.toLowerCase().contains("primary")) {
                sdcProcessor = new SDCProcessor(connectionInfo.getConnectionId());
                String keycolid2 = sdcProcessor.getProperty(sdcid, "keycolid2", "");
                String keycolid3 = sdcProcessor.getProperty(sdcid, "keycolid3", "");
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setSDCid(sdcid);
                sdiRequest.setRequestItem("primary");
                sdiRequest.setKeyid1List(keyid1);
                if (keyid2 != null && keyid2.length() > 0 && keycolid2.length() > 1) {
                    sdiRequest.setKeyid2List(keyid2);
                }
                if (keyid3 != null && keyid3.length() > 0 && keycolid3.length() > 1) {
                    sdiRequest.setKeyid3List(keyid3);
                }
                SDIProcessor sdiProcessor = new SDIProcessor(connectionInfo.getConnectionId());
                SDIData data = sdiProcessor.getSDIData(sdiRequest);
                DataSet dataset = data.getDataset("primary");
                String columnName = token.split("[.]").length == 2 ? token.split("[.]")[1] : "";
                String columnValue = columnName.length() > 1 ? dataset.getValue(0, columnName, "") : "";
                columnValue = Attachment.replaceInvalidChars(columnValue, null);
                filename = filename.replace("[" + token + "]", columnValue);
                continue;
            }
            if (token.toLowerCase().contains("currentuser") && !token.toLowerCase().contains("password")) {
                sdcProcessor = new SDCProcessor(connectionInfo.getConnectionId());
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setSDCid("User");
                sdiRequest.setRequestItem("primary");
                sdiRequest.setKeyid1List(connectionInfo.getSysuserId());
                SDIProcessor sdiProcessor = new SDIProcessor(connectionInfo.getConnectionId());
                SDIData data = sdiProcessor.getSDIData(sdiRequest);
                DataSet dataset = data.getDataset("primary");
                String columnName = token.split("[.]").length == 2 ? token.split("[.]")[1] : "";
                String columnValue = columnName.length() > 1 ? dataset.getValue(0, columnName.toLowerCase(), "") : "";
                columnValue = Attachment.replaceInvalidChars(columnValue, null);
                filename = filename.replace("[" + token + "]", columnValue);
                continue;
            }
            if (!token.toLowerCase().contains("sdiattachment") || attachment == null) continue;
            try {
                PropertyList propertyList = attachment.toPropertyList(false, false, false, connectionInfo.getConnectionId());
                String columnName = token.split("[.]").length == 2 ? token.split("[.]")[1] : "";
                String columnValue = columnName.length() > 1 ? propertyList.getProperty(columnName.toLowerCase(), "") : "";
                columnValue = Attachment.replaceInvalidChars(columnValue, null);
                filename = filename.replace("[" + token + "]", columnValue);
            }
            catch (SapphireException e) {
                // empty catch block
            }
        }
        return filename;
    }

    public static String replaceInvalidChars(String tokens, String replacement) {
        return tokens.replaceAll("[\\\\/:*?\"\\.<>|]", replacement != null ? replacement : "-").replaceAll("\\s+", "");
    }

    public static enum ThumbnailGeneration {
        GENERATEANDSTORE("Generate and Store", "Y", true, true, true),
        DONOTSTORE("Do Not Store", "D", true, false, true),
        DISABLED("Disabled", "N", false, false, false),
        SHOWIFAVAILABLE("Show if Available", "A", false, false, true);

        private String title;
        private String flag;
        private boolean store;
        private boolean generate;
        private boolean show;

        private ThumbnailGeneration(String title, String flag, boolean generate, boolean store, boolean show) {
            this.title = title;
            this.flag = flag;
            this.generate = generate;
            this.store = store;
            this.show = show;
        }

        public static ThumbnailGeneration getThumbnailGenerationByFlag(String flag) {
            for (ThumbnailGeneration thumbnailGeneration : ThumbnailGeneration.values()) {
                if (!flag.equalsIgnoreCase(thumbnailGeneration.flag)) continue;
                return thumbnailGeneration;
            }
            return null;
        }

        public static ThumbnailGeneration getThumbnailGenerationByName(String title) {
            for (ThumbnailGeneration thumbnailGeneration : ThumbnailGeneration.values()) {
                if (!title.equalsIgnoreCase(thumbnailGeneration.title)) continue;
                return thumbnailGeneration;
            }
            return null;
        }

        public static ThumbnailGeneration getThumbnailGeneration(String titleOrFlag) {
            if (titleOrFlag == null) {
                return null;
            }
            if (titleOrFlag.length() == 1) {
                return ThumbnailGeneration.getThumbnailGenerationByFlag(titleOrFlag);
            }
            return ThumbnailGeneration.getThumbnailGenerationByName(titleOrFlag);
        }

        public static ThumbnailGeneration getThumbnailGeneration(String titleOrFlag, ThumbnailGeneration defaultThumb) {
            ThumbnailGeneration out = null;
            if (titleOrFlag != null) {
                out = titleOrFlag.length() == 1 ? ThumbnailGeneration.getThumbnailGenerationByFlag(titleOrFlag) : ThumbnailGeneration.getThumbnailGenerationByName(titleOrFlag);
            }
            return out != null ? out : defaultThumb;
        }

        public boolean canStore() {
            return this.store;
        }

        public boolean canGenerate() {
            return this.generate;
        }

        public boolean canShow() {
            return this.show;
        }

        public String getFlag() {
            return this.flag;
        }

        public String getTitle() {
            return this.title;
        }
    }

    public static enum AttachmentType {
        FILE("File", "F", new String[]{"UploadAndReference", "Store", "UploadAndStore", "Reference", "CMS"}, new String[]{"U", "S", "F", "R"}, true, "FlatBlackPageQuestion"),
        RICHTEXT("RichText", "M", new String[]{"RichText"}, new String[]{"M"}, true, "FlatBlackTextSerif"),
        LINKEDREFERENCE("LinkedReference", "D", new String[]{"LinkedReference"}, new String[]{"D"}, false, "FlatBlackLink"),
        PLAINTEXT("PlainText", "P", new String[]{"PlainText"}, new String[]{"P"}, true, "FlatBlackText1"),
        URL("URL", "L", new String[]{"URL"}, new String[]{"L"}, true, "FlatBlackChainLink");

        private String image;
        private String name;
        private String flag;
        private String[] attachmenttypeflags;
        private String[] attachmenttypes;
        private boolean editable;

        private AttachmentType(String name, String flag, String[] attachmenttypes, String[] attachmenttypeflags, boolean editable, String image) {
            this.image = image;
            this.name = name;
            this.flag = flag;
            this.attachmenttypeflags = attachmenttypeflags;
            this.attachmenttypes = attachmenttypes;
            this.editable = editable;
        }

        public String getFlag() {
            return this.flag;
        }

        public static AttachmentType getType(String name) {
            for (AttachmentType type : AttachmentType.values()) {
                if (!type.name.equalsIgnoreCase(name)) continue;
                return type;
            }
            return null;
        }

        public static AttachmentType getTypeByAttachmentTypeFlag(String typeflag) {
            typeflag = com.labvantage.sapphire.services.Attachment.correctTypeFlag(typeflag);
            for (AttachmentType type : AttachmentType.values()) {
                for (String atttype : type.attachmenttypeflags) {
                    if (!atttype.equalsIgnoreCase(typeflag)) continue;
                    return type;
                }
            }
            return null;
        }

        public static AttachmentType getTypeByAttachmentTypeFullName(String typename) {
            for (AttachmentType type : AttachmentType.values()) {
                for (String atttype : type.attachmenttypes) {
                    if (!atttype.equalsIgnoreCase(typename)) continue;
                    return type;
                }
            }
            return null;
        }

        public String getImageId() {
            return this.image;
        }

        public boolean isEditable() {
            return this.editable;
        }
    }
}

