/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.services;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.automation.AddToDoListEntry;
import com.labvantage.sapphire.modules.sdms.collector.SDMSCollector;
import com.labvantage.sapphire.modules.search.Indexer;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.BaseService;
import com.labvantage.sapphire.services.DDTService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.LabVantageClassLoader;
import com.labvantage.sapphire.util.StringHolder;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileTransfer;
import com.labvantage.sapphire.util.file.FileTransferOptions;
import com.labvantage.sapphire.util.file.FileType;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.attachment.Attachment;
import sapphire.attachment.BaseAttachmentRepository;
import sapphire.error.ErrorHandler;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AttachmentService
extends BaseService {
    public static final String LOGNAME = "AttachmentService";

    public AttachmentService(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
        this.logName = LOGNAME;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addSDIAttachment(HashMap columns, byte[] data) throws ServiceException {
        this.logInfo("Adding SDI attachment (-1)");
        ByteArrayInputStream byteArrayInputStream = data != null ? new ByteArrayInputStream(data) : null;
        try {
            this.addSDIAttachment(columns, byteArrayInputStream);
        }
        finally {
            try {
                if (byteArrayInputStream != null) {
                    byteArrayInputStream.close();
                }
            }
            catch (IOException iOException) {}
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addSDIAttachment(HashMap columns, InputStream data) throws ServiceException {
        this.logInfo("Adding SDI attachment (3)");
        PropertyList props = new PropertyList(columns);
        String keyid1 = props.getProperty("keyid1");
        String keyid2 = props.getProperty("keyid2");
        String keyid3 = props.getProperty("keyid3");
        String sdcid = props.getProperty("sdcid");
        String uploadto = props.getProperty("uploadto");
        String description = props.getProperty("attachmentdesc", props.getProperty("description", props.getProperty("desc", "")));
        String typeflag = props.getProperty("type", props.getProperty("typeflag"));
        String oleclass = props.getProperty("oleclass");
        String filename = props.getProperty("filename");
        String url = props.getProperty("url");
        String attachmentclob = props.getProperty("attachmentclob");
        String linksdcid = props.getProperty("linksdcid");
        String linkkeyid1 = props.getProperty("linkkeyid1");
        String linkkeyid2 = props.getProperty("linkkeyid2");
        String linkkeyid3 = props.getProperty("linkkeyid3");
        String linkattachmentnum = props.getProperty("linkattachmentnum");
        boolean applylock = props.getProperty("applylock").equalsIgnoreCase("Y");
        String documentField = props.getProperty("documentfield");
        boolean index = props.getProperty("index", "Y").equalsIgnoreCase("Y");
        String attachmentpolicynode = props.getProperty("attachmentpolicynode");
        props.deleteProperty("keyid1");
        props.deleteProperty("keyid2");
        props.deleteProperty("keyid3");
        props.deleteProperty("sdcid");
        props.deleteProperty("uploadto");
        props.deleteProperty("attachmentdesc");
        props.deleteProperty("description");
        props.deleteProperty("desc");
        props.deleteProperty("type");
        props.deleteProperty("typeflag");
        props.deleteProperty("oleclass");
        props.deleteProperty("filename");
        props.deleteProperty("applylock");
        props.deleteProperty("documentfield");
        props.deleteProperty("index");
        props.deleteProperty("attachmentpolicynode");
        props.deleteProperty("url");
        if (documentField.length() == 0) {
            props.setProperty("contentrevision", "1");
            this.addSDIAttachment(sdcid, keyid1, keyid2, keyid3, description, typeflag, oleclass, filename, url, attachmentclob, linksdcid, linkkeyid1, linkkeyid2, linkkeyid3, linkattachmentnum, data, uploadto, applylock, index, props, attachmentpolicynode);
        } else {
            props.setProperty("contentrevision", "-1");
            String[] keyid1list = StringUtil.split(keyid1, ";");
            String[] keyid2list = StringUtil.split(keyid2, ";");
            String[] keyid3list = StringUtil.split(keyid3, ";");
            String[] documentfieldlist = StringUtil.split(documentField, ";");
            for (int i = 0; i < documentfieldlist.length; ++i) {
                String docfield = documentfieldlist[i];
                String[] parts = StringUtil.split(docfield, "|");
                if (parts.length == 4) {
                    DBUtil dbu = new DBUtil(this.sapphireConnection.getConnectionId());
                    try {
                        dbu.setConnection(this.sapphireConnection);
                        dbu.createPreparedResultSet("SELECT attachmentdesc, filename, url, attachmentclob, linksdcid, linkkeyid1, linkkeyid2, linkkeyid3, linkattachmentnum, attachmentnum, typeflag FROM sdiattachment WHERE sdcid = 'LV_Document' AND keyid1 = ? AND keyid2 = ? AND attachmentuse = 'FileField' AND attachmentlabel  = ?", new Object[]{parts[0], parts[1], parts[2] + "|" + parts[3] + "|0"});
                        if (dbu.getNext()) {
                            Attachment attachment = this.getSDIAttachment("LV_Document", parts[0], parts[1], "(null)", dbu.getInt("attachmentnum"));
                            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(attachment.getData());
                            try {
                                this.addSDIAttachment(sdcid, keyid1list.length > i ? keyid1list[i] : keyid1list[keyid1list.length - 1], keyid2.length() == 0 ? "" : (keyid2list.length > i ? keyid2list[i] : keyid2list[keyid2list.length - 1]), keyid3.length() == 0 ? "" : (keyid3list.length > i ? keyid3list[i] : keyid3list[keyid3list.length - 1]), dbu.getString("attachmentdesc"), dbu.getString("typeflag"), oleclass, dbu.getString("filename"), dbu.getString("url"), dbu.getClob("attachmentclob"), dbu.getString("linksdcid"), dbu.getString("linkkeyid1"), dbu.getString("linkkeyid2"), dbu.getString("linkkeyid3"), "" + dbu.getInt("linkattachmentnum"), byteArrayInputStream, uploadto, applylock, index, new PropertyList(), attachmentpolicynode);
                                continue;
                            }
                            finally {
                                try {
                                    byteArrayInputStream.close();
                                }
                                catch (Exception exception) {}
                            }
                        }
                        throw new SapphireException("Document field attachment not found!");
                    }
                    catch (Exception e) {
                        throw new ServiceException("GENERAL_ERROR", "Failed to add the document field attachment: " + e.getMessage(), e);
                    }
                    finally {
                        dbu.reset();
                    }
                }
                throw new ServiceException("Document field reference '" + documentField + "' is invalid");
            }
        }
        columns.put("attachmentnum", props.getProperty("attachmentnum"));
    }

    private void addSDIAttachment(String sdcid, String keyid1, String keyid2, String keyid3, String description, String type, String oleclass, String filename, String url, String attachmentclob, String linksdcid, String linkkeyid1, String linkkeyid2, String linkkeyid3, String linkattachmentnum, InputStream data, String uploadto, boolean applylock, boolean index, PropertyList additionalColumns) throws ServiceException {
        this.addSDIAttachment(sdcid, keyid1, keyid2, keyid3, description, type, oleclass, filename, url, attachmentclob, linksdcid, linkkeyid1, linkkeyid2, linkkeyid3, linkattachmentnum, data, uploadto, applylock, index, additionalColumns, "");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addSDIAttachment(sapphire.attachment.Attachment attachment, boolean applylock, boolean index, String attachmentpolicynode) throws ServiceException {
        this.logInfo("Adding SDI attachment (4) ");
        Attachment currentAttachment = (Attachment)attachment;
        DAMProcessor dam = this.getDAMProcessor();
        StringHolder rsetidHolder = new StringHolder();
        String rsetid = "";
        if (applylock) {
            if (dam.createLockedRSet(attachment.getSDCId(), attachment.getKeyId1(), attachment.getKeyId2(), attachment.getKeyId3(), rsetidHolder) == 1) {
                rsetid = rsetidHolder.value;
            }
        } else if (dam.createRSet(attachment.getSDCId(), attachment.getKeyId1(), attachment.getKeyId2(), attachment.getKeyId3(), rsetidHolder) == 1) {
            rsetid = rsetidHolder.value;
        }
        if (rsetid.length() == 0) {
            throw new ServiceException("CREATE_RSET_FAILURE", "Failed to create RSET for add attachment");
        }
        this.generateTraceLog(attachment.getSDCId(), attachment.getKeyId1(), attachment.getKeyId2(), attachment.getKeyId3(), "Adding Attachment", ((Attachment)attachment).getAdditionalColumns());
        if (currentAttachment.getAdditionalColumns().getProperty("documentfield").length() == 0 && currentAttachment.getAdditionalColumns().getProperty("contentrevision").length() == 0) {
            currentAttachment.setAdditionalColumn("contentrevision", "1");
        } else if (Integer.parseInt(currentAttachment.getAdditionalColumns().getProperty("contentrevision")) < 0) {
            currentAttachment.getAdditionalColumns().remove("contentrevision");
        }
        this.removeInvalidColumns(new QueryProcessor(this.connectionInfo.getConnectionId()), currentAttachment.getAdditionalColumns());
        String sql = "SELECT sdiattachment.sdcid, sdiattachment.keyid1, sdiattachment.keyid2, sdiattachment.keyid3, sdiattachment.attachmentnum  FROM sdiattachment, rsetitems WHERE sdiattachment.sdcid = rsetitems.sdcid AND sdiattachment.keyid1 = rsetitems.keyid1 AND sdiattachment.keyid2 = rsetitems.keyid2 AND sdiattachment.keyid3 = rsetitems.keyid3 AND rsetitems.rsetid = ? ORDER BY sdiattachment.keyid1, sdiattachment.keyid2, sdiattachment.keyid3, sdiattachment.attachmentnum desc";
        this.logInfo("Loading the selected samples existing data.");
        DBUtil dbu = new DBUtil(this.sapphireConnection.getConnectionId());
        dbu.setConnection(this.sapphireConnection);
        currentAttachment.setAttachmentNum(1);
        try {
            dbu.createPreparedResultSet(sql, rsetid);
            DataSet ds = new DataSet(dbu.getResultSet());
            HashMap<String, String> findmap = new HashMap<String, String>();
            findmap.put("keyid1", currentAttachment.getKeyId1());
            findmap.put("keyid2", currentAttachment.getKeyId2());
            findmap.put("keyid3", currentAttachment.getKeyId3());
            int findrow = ds.findRow(findmap);
            if (findrow >= 0) {
                currentAttachment.setAttachmentNum(ds.getBigDecimal(findrow, "attachmentnum").intValue() + 1);
            }
        }
        catch (Exception e) {
            throw new ServiceException(e);
        }
        try {
            this.addSDIAttachment(currentAttachment, index, attachmentpolicynode, null, null, null, dbu);
        }
        finally {
            dbu.reset();
            if (rsetid != null) {
                dam.clearRSet(rsetid);
            }
        }
    }

    private boolean canDecrypt(Attachment currentAttachment) {
        return currentAttachment.isEncrypted() && currentAttachment.getAttachmentType() == Attachment.AttachmentType.FILE && currentAttachment.hasData();
    }

    private boolean canEncrypt(Attachment currentAttachment) {
        if (currentAttachment.getAttachmentType() == Attachment.AttachmentType.FILE && currentAttachment.hasData()) {
            return currentAttachment.isEncrypted();
        }
        return false;
    }

    private boolean canEncrypt(Attachment currentAttachment, BaseAttachmentRepository attachmentRepository, PropertyList attachmentPolicy) {
        if (currentAttachment.getAttachmentType() == Attachment.AttachmentType.FILE && currentAttachment.hasData()) {
            return !attachmentRepository.canEncrypt() && currentAttachment.isEncryptionRequired(attachmentPolicy, this.getConfigurationProcessor());
        }
        return false;
    }

    private boolean canValidateHash(Attachment currentAttachment) {
        if (currentAttachment.getAttachmentType() == Attachment.AttachmentType.FILE && currentAttachment.getSourceFilename() != null && currentAttachment.getSourceFilename().length() > 0 && currentAttachment.hasData()) {
            return currentAttachment.isHashed();
        }
        return false;
    }

    private boolean canGenerateHash(Attachment currentAttachment, sapphire.attachment.Attachment preEditAttachment) {
        if (currentAttachment.getAttachmentType() == Attachment.AttachmentType.FILE && currentAttachment.getSourceFilename() != null && currentAttachment.getSourceFilename().length() > 0 && currentAttachment.hasData()) {
            return currentAttachment.isHashed();
        }
        if (currentAttachment.getAttachmentType() == null && preEditAttachment.getAttachmentType() == Attachment.AttachmentType.FILE && (currentAttachment.getSourceFilename() == null || currentAttachment.getSourceFilename().length() == 0) && preEditAttachment.getSourceFilename() != null && preEditAttachment.getSourceFilename().length() > 0 && currentAttachment.hasData()) {
            return preEditAttachment.isHashed();
        }
        return false;
    }

    private boolean canGenerateHash(Attachment currentAttachment, BaseAttachmentRepository attachmentRepository, PropertyList attachmentPolicy) {
        if (currentAttachment.getAttachmentType() == Attachment.AttachmentType.FILE && attachmentRepository.isManaged() && currentAttachment.hasData()) {
            if (!attachmentRepository.canHash()) {
                long maxSize = 100L;
                try {
                    maxSize = Long.parseLong(attachmentPolicy.getProperty("maxhashsize", "100"));
                }
                catch (Exception e) {
                    maxSize = 100L;
                }
                if (maxSize == 0L) {
                    return false;
                }
                long maxSizeInBytes = maxSize / 1000000L;
                return maxSize == -1L || maxSizeInBytes <= currentAttachment.getSize();
            }
            return false;
        }
        return false;
    }

    private boolean canUncompress(Attachment currentAttachment) {
        return currentAttachment.isCompressed() && currentAttachment.getAttachmentType() == Attachment.AttachmentType.FILE && currentAttachment.hasData();
    }

    private boolean canCompress(Attachment currentAttachment) {
        if (currentAttachment.getAttachmentType() == Attachment.AttachmentType.FILE && currentAttachment.getSourceFilename() != null && currentAttachment.getSourceFilename().length() > 0 && currentAttachment.hasData()) {
            return currentAttachment.isCompressed();
        }
        return false;
    }

    private boolean canCompress(Attachment currentAttachment, BaseAttachmentRepository attachmentRepository, PropertyList attachmentPolicy) {
        if (currentAttachment.getAttachmentType() == Attachment.AttachmentType.FILE && currentAttachment.getSourceFilename() != null && currentAttachment.getSourceFilename().length() > 0 && currentAttachment.hasData()) {
            String connectionid = this.getConnectionid();
            if (!attachmentRepository.canCompress() && currentAttachment.isZippingRequired(attachmentPolicy, this.getConfigurationProcessor())) {
                FileType fileType = FileType.getFileTypeByFileName(currentAttachment.getSourceFilename(), connectionid);
                return !fileType.getName().equals("ZIP") && !fileType.getName().equals("GZ") && !fileType.getName().equals("JPEG") && !fileType.getName().equals("JPG") && !fileType.getName().equals("GIF") && !fileType.getName().equals("PNG") && !fileType.getName().equals("MP3") && !fileType.getName().equals("MP4") && !fileType.getName().equals("WMA") && !fileType.getName().equals("AVI") && !fileType.getName().equals("MPG") && !fileType.getName().equals("EAR") && !fileType.getName().equals("JAR") && !fileType.getName().equals("WAR");
            }
            return false;
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void addSDIAttachment(Attachment attachment, boolean index, String attachmentpolicynode, PropertyList attachmentPolicyIn, PropertyListCollection attachmentColDef, M18NUtil m18NUtil, DBUtil dbu) throws ServiceException {
        Attachment currentAttachment = attachment;
        try {
            Path p;
            if (dbu == null) {
                dbu = new DBUtil(this.sapphireConnection.getConnectionId());
                dbu.setConnection(this.sapphireConnection);
            }
            Calendar now = DateTimeUtil.getNowCalendar();
            DDTService ddtService = null;
            if (attachmentColDef == null) {
                try {
                    if (ddtService == null) {
                        ddtService = new DDTService(this.sapphireConnection);
                    }
                    attachmentColDef = ddtService.getSDCProperties("SDIAttachment").getCollection("columns");
                }
                catch (Exception e) {
                    this.logger.warn("Failed to obtain attachment column defintion.");
                }
            }
            if (m18NUtil == null) {
                try {
                    m18NUtil = new M18NUtil(new ConnectionInfo(this.sapphireConnection));
                }
                catch (Exception e) {
                    this.logger.warn("Failed to obtain m18n.");
                }
            }
            if (currentAttachment.getType() == null || currentAttachment.getType().length() == 0) {
                if (currentAttachment.getInputStream() != null && (currentAttachment.getUploadTo() == null || currentAttachment.getUploadTo().length() == 0)) {
                    currentAttachment.setType("S");
                } else {
                    currentAttachment.setType("R");
                }
            } else if (currentAttachment.getType().length() > 1) {
                currentAttachment.setType(currentAttachment.getType().substring(0, 1));
            }
            currentAttachment.setType(Attachment.correctTypeFlag(currentAttachment.getType()));
            if (currentAttachment.getInputStream() == null && attachment.getFilename() != null && attachment.getFilename().length() > 0 && attachment.getType() != "R" && Files.exists(p = Paths.get(attachment.getFilename(), new String[0]), new LinkOption[0])) {
                try {
                    currentAttachment.setInputStream(Files.newInputStream(p, new OpenOption[0]));
                }
                catch (Exception e) {
                    throw new ServiceException("Failed to read file provided as backwards compatible attachment.", e);
                }
            }
            String apn = attachmentpolicynode != null && attachmentpolicynode.length() > 0 ? attachmentpolicynode : "Sapphire Custom";
            PropertyList attachmentPolicy = attachmentPolicyIn == null ? this.getConfigurationProcessor().getPolicy("AttachmentPolicy", apn) : attachmentPolicyIn;
            if (attachment.getTempId() != null && attachment.getTempId().length() > 0) {
                attachment.readTempFile(this.getQueryProcessor(), this.getActionProcessor(), this.getConnectionId());
            }
            if (attachment.getParentDepartment() == null) {
                PropertyList priSDCProps;
                if (ddtService == null) {
                    ddtService = new DDTService(this.sapphireConnection);
                }
                PropertyListCollection priCols = (priSDCProps = ddtService.getSDCProperties(attachment.getSDCId())) != null ? priSDCProps.getCollection("columns") : new PropertyListCollection();
                BaseSDCRules[] depcol = null;
                if (priSDCProps.getProperty("accesscontrolledflag", "").equalsIgnoreCase("D") && priCols.find("columnid", "securitydepartment") != null) {
                    depcol = "securitydepartment";
                }
                if (depcol != null) {
                    StringBuilder priSql = new StringBuilder();
                    priSql.append("SELECT ").append((String)depcol).append(" FROM ").append(priSDCProps.getProperty("tableid")).append(" WHERE ").append(priSDCProps.getProperty("keycolid1")).append("=?");
                    DataSet priD = null;
                    if (priSDCProps.getProperty("keycolid2").length() > 0) {
                        priSql.append(" AND ").append(priSDCProps.getProperty("keycolid2")).append("=?");
                        if (priSDCProps.getProperty("keycolid3").length() > 0) {
                            priSql.append(" AND ").append(priSDCProps.getProperty("keycolid3")).append("=?");
                            priD = this.getQueryProcessor().getPreparedSqlDataSet(priSql.toString(), new Object[]{attachment.getKeyId1(), attachment.getKeyId2(), attachment.getKeyId3()});
                        } else {
                            priD = this.getQueryProcessor().getPreparedSqlDataSet(priSql.toString(), new Object[]{attachment.getKeyId1(), attachment.getKeyId2()});
                        }
                    } else {
                        priD = this.getQueryProcessor().getPreparedSqlDataSet(priSql.toString(), new Object[]{attachment.getKeyId1()});
                    }
                    if (priD != null && priD.size() > 0 && priD.getValue(0, (String)depcol, "").length() > 0) {
                        attachment.setParentDepartment(priD.getValue(0, (String)depcol, ""));
                    }
                }
            }
            if (currentAttachment.getTriggerBusinessRule()) {
                ErrorHandler errorHandler = new ErrorHandler();
                try {
                    BaseSDCRules sdcPreRules = BaseSDCRules.getInstance(this.sapphireConnection, errorHandler, currentAttachment.getSDCId(), this.getSDCProcessor().getProperties(currentAttachment.getSDCId()), "PreAddAttachment");
                    Trace.startBusinessRule(currentAttachment.getSDCId() + "." + "PreAddAttachment", true);
                    sdcPreRules.preAddSDIAttachment(currentAttachment);
                    Trace.endBusinessRule(currentAttachment.getSDCId() + "." + "PreAddAttachment", true);
                    Trace.startBusinessRule(currentAttachment.getSDCId() + "." + "PreAddAttachment", false);
                    for (BaseSDCRules customRules : sdcPreRules.getCustomRuleList()) {
                        customRules.preAddSDIAttachment(currentAttachment);
                    }
                    Trace.endBusinessRule(currentAttachment.getSDCId() + "." + "PreAddAttachment", false);
                    sdcPreRules.endRule();
                    if (errorHandler.size() > 0 && errorHandler.hasErrors()) {
                        throw new ServiceException(errorHandler.getEncodedString());
                    }
                }
                catch (SapphireException e) {
                    if (errorHandler.size() > 0 && errorHandler.hasErrors()) {
                        throw new ServiceException(errorHandler.getEncodedString(), e);
                    }
                    throw new ServiceException("PreAddAttachment Business Rule Failed", e);
                }
            }
            BaseAttachmentRepository attachmentRepository = currentAttachment.getAttachmentRepository(this.sapphireConnection, attachmentPolicy);
            MessageDigest hashingDigester = null;
            AtomicBoolean canGenerateHash = new AtomicBoolean();
            LabVantageClassLoader.executeCode(attachmentRepository.getClassLoader(), () -> canGenerateHash.set(this.canGenerateHash(currentAttachment, attachmentRepository, attachmentPolicy)), false);
            if (canGenerateHash.get()) {
                DigestInputStream is = FileTransfer.getHashedInputSteam(currentAttachment.getInputStream(), SDMSCollector.defaultHashingAlgorithm);
                currentAttachment.setInputStream(is);
                hashingDigester = is.getMessageDigest();
            }
            AtomicBoolean canEncrypt = new AtomicBoolean();
            LabVantageClassLoader.executeCode(attachmentRepository.getClassLoader(), () -> canEncrypt.set(this.canEncrypt(currentAttachment, attachmentRepository, attachmentPolicy)), false);
            if (canEncrypt.get()) {
                currentAttachment.setInputStream(FileTransfer.getCipherInputStream(currentAttachment.getInputStream(), false));
                currentAttachment.setEncrypted(true);
            }
            AtomicBoolean canCompress = new AtomicBoolean();
            LabVantageClassLoader.executeCode(attachmentRepository.getClassLoader(), () -> canCompress.set(this.canCompress(currentAttachment, attachmentRepository, attachmentPolicy)), false);
            if (canCompress.get()) {
                currentAttachment.setInputStream(FileTransfer.getCompressedInputStream(currentAttachment.getInputStream()));
                currentAttachment.setCompressed(true);
            }
            LabVantageClassLoader.executeCode(attachmentRepository.getClassLoader(), () -> attachmentRepository.preAddSDIAttachment(currentAttachment), false);
            StringBuffer additionalINTO = new StringBuffer(" ");
            StringBuffer additionalVALUES = new StringBuffer(" ");
            if (currentAttachment.getAdditionalColumns() != null && currentAttachment.getAdditionalColumns().size() > 0) {
                Iterator it = currentAttachment.getAdditionalColumns().keySet().iterator();
                while (it.hasNext()) {
                    String curkey = it.next().toString();
                    String value = currentAttachment.getAdditionalColumn(curkey, null);
                    if (value == null) continue;
                    value = StringUtil.replaceAll(value, "'", "''");
                    if (curkey.equalsIgnoreCase("contentrevision") || curkey.equalsIgnoreCase("linkattachmentnum") || curkey.equalsIgnoreCase("auditsequence")) {
                        if (value.length() <= 0) continue;
                        additionalINTO.append(" ,").append(curkey).append(" ");
                        additionalVALUES.append(" , ").append(value).append(" ");
                        continue;
                    }
                    if (curkey.equalsIgnoreCase("usersequence")) {
                        if (value.length() <= 0) continue;
                        additionalINTO.append(" ,").append(curkey).append(" ");
                        additionalVALUES.append(" , ").append(value).append(" ");
                        continue;
                    }
                    PropertyList col = null;
                    if (attachmentColDef != null) {
                        col = attachmentColDef.find("columnid", curkey);
                    }
                    if (col != null) {
                        String dt = col.getProperty("datatype", "C");
                        if (dt.equalsIgnoreCase("N") && value.length() > 0) {
                            additionalINTO.append(" ,").append(curkey).append(" ");
                            additionalVALUES.append(" , ").append(value).append(" ");
                            continue;
                        }
                        if (dt.equalsIgnoreCase("D") && value.length() > 0 && m18NUtil != null) {
                            Timestamp datevalue = new Timestamp(m18NUtil.parseCalendar(value).getTimeInMillis());
                            additionalINTO.append(" ,").append(curkey).append(" ");
                            additionalVALUES.append(" , timestamp'").append(datevalue).append("' ");
                            continue;
                        }
                        additionalINTO.append(" ,").append(curkey).append(" ");
                        additionalVALUES.append(" , '").append(value).append("' ");
                        continue;
                    }
                    additionalINTO.append(" ,").append(curkey).append(" ");
                    additionalVALUES.append(" , '").append(value).append("' ");
                }
            }
            this.logInfo("Adding a row for " + currentAttachment.getSDCId() + ", " + currentAttachment.getKeyId1() + ", " + currentAttachment.getKeyId2() + ", " + currentAttachment.getKeyId3() + ", " + currentAttachment.getAttachmentNum());
            StringBuffer sqlBuild = new StringBuffer("insert into sdiattachment (sdcid, keyid1, keyid2, keyid3, filename, sourcefilename, typeflag, attachmentdesc, oleclass, attachmentnum, attachmentrepositoryid, attachmentrepositorynodeid, externalrepository, attachmentsize, compressedflag, encryptedflag, createdt, createby, createtool, moddt, modby, modtool, attachmentclass, attachment, url, attachmentclob, thumbnailimage ").append(additionalINTO.toString()).append(") values (");
            sqlBuild.append("?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? , ?, ? , ?, ?, ?, ?, ?, ? ").append(",? ").append(", ? ,?, ? ").append(additionalVALUES.toString()).append(")");
            this.logInfo("Attachment SQL = " + sqlBuild.toString());
            PreparedStatement insert = dbu.prepareStatement(sqlBuild.toString());
            this.logDebug("Attachment row prepared successfully.");
            Timestamp timevalue = new Timestamp(now.getTime().getTime());
            insert.setString(1, currentAttachment.getSDCId());
            insert.setString(2, currentAttachment.getKeyId1());
            insert.setString(3, currentAttachment.getKeyId2());
            insert.setString(4, currentAttachment.getKeyId3());
            insert.setString(5, currentAttachment.getFilename() != null ? (currentAttachment.getFilename().length() > 255 ? currentAttachment.getFilename().substring(0, 255) : currentAttachment.getFilename()) : "");
            insert.setString(6, currentAttachment.getSourceFilename() != null ? (currentAttachment.getSourceFilename().length() > 255 ? currentAttachment.getSourceFilename().substring(0, 255) : currentAttachment.getSourceFilename()) : "");
            insert.setString(7, currentAttachment.getType().toUpperCase());
            insert.setString(8, currentAttachment.getDescription() != null ? (currentAttachment.getDescription().length() > 80 ? currentAttachment.getDescription().substring(0, 80) : currentAttachment.getDescription()) : "");
            insert.setString(9, currentAttachment.getOleClass());
            insert.setInt(10, currentAttachment.getAttachmentNum());
            insert.setString(11, currentAttachment.getAttachmentRepositoryId());
            insert.setString(12, currentAttachment.getAttachmentRepositoryNodeId());
            insert.setString(13, currentAttachment.getRepositoryId());
            insert.setLong(14, currentAttachment.getSize());
            insert.setString(15, currentAttachment.isCompressed() ? "Y" : "N");
            insert.setString(16, currentAttachment.isEncrypted() ? "Y" : "N");
            insert.setTimestamp(17, timevalue);
            insert.setString(18, this.connectionInfo.getSysuserId());
            insert.setString(19, this.connectionInfo.getTool());
            insert.setTimestamp(20, timevalue);
            insert.setString(21, this.connectionInfo.getSysuserId());
            insert.setString(22, this.connectionInfo.getTool());
            insert.setString(23, currentAttachment.getAttachmentClass());
            if (currentAttachment.getBlob() != null) {
                insert.setBlob(24, currentAttachment.getBlob());
            } else {
                insert.setNull(24, 2004);
            }
            insert.setString(25, currentAttachment.getUrl() != null ? currentAttachment.getUrl() : "");
            insert.setCharacterStream(26, (Reader)(attachment.getClob() != null ? new StringReader(currentAttachment.getClob()) : new StringReader("")), attachment.getClob() != null ? currentAttachment.getClob().length() : 0);
            insert.setCharacterStream(27, (Reader)(attachment.getThumbnailImage() != null ? new StringReader(currentAttachment.getThumbnailImage()) : new StringReader("")), attachment.getThumbnailImage() != null ? currentAttachment.getThumbnailImage().length() : 0);
            try {
                this.logDebug("About to execute insert...");
                insert.execute();
                this.logDebug("Attachment row inserted successfully.");
                insert.close();
            }
            catch (Exception e) {
                if (insert != null) {
                    insert.close();
                }
                throw new SapphireException("DB_INSERT_FAILED", "Failed to insert the attachment rows: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.sapphireConnection.getConnectionId())), e);
            }
            BaseAttachmentRepository attachmentRepositoryOut = currentAttachment.getAttachmentRepository(this.sapphireConnection, attachmentPolicy);
            LabVantageClassLoader.executeCode(attachmentRepositoryOut.getClassLoader(), () -> attachmentRepositoryOut.postAddSDIAttachment(currentAttachment), false);
            if (hashingDigester != null) {
                byte[] md5sum = hashingDigester.digest();
                BigInteger bigInt = new BigInteger(1, md5sum);
                long hash = bigInt.longValue();
                this.logger.debug("Generated File Hash:" + hash);
                if (hash != 0L) {
                    String whereClause = " where sdcid='" + currentAttachment.getSDCId() + "' and keyid1=? and keyid2=? and keyid3=? and attachmentnum='" + currentAttachment.getAttachmentNum() + "'";
                    String hashUpdate = "update sdiattachment set  datahash = ?" + whereClause;
                    PreparedStatement hashUpdateSQL = dbu.prepareStatement(hashUpdate);
                    hashUpdateSQL.setString(1, "" + hash);
                    hashUpdateSQL.setString(2, currentAttachment.getKeyId1());
                    hashUpdateSQL.setString(3, currentAttachment.getKeyId2());
                    hashUpdateSQL.setString(4, currentAttachment.getKeyId3());
                    try {
                        hashUpdateSQL.execute();
                    }
                    finally {
                        hashUpdateSQL.close();
                    }
                }
            }
            if (currentAttachment.getTriggerBusinessRule()) {
                ErrorHandler errorHandler = new ErrorHandler();
                try {
                    BaseSDCRules sdcPostRules = BaseSDCRules.getInstance(this.sapphireConnection, errorHandler, currentAttachment.getSDCId(), this.getSDCProcessor().getProperties(currentAttachment.getSDCId()), "PostAddAttachment");
                    Trace.startBusinessRule(currentAttachment.getSDCId() + "." + "PostAddAttachment", true);
                    sdcPostRules.postAddSDIAttachment(currentAttachment);
                    Trace.endBusinessRule(currentAttachment.getSDCId() + "." + "PostAddAttachment", true);
                    Trace.startBusinessRule(currentAttachment.getSDCId() + "." + "PostAddAttachment", false);
                    for (BaseSDCRules customRules : sdcPostRules.getCustomRuleList()) {
                        customRules.postAddSDIAttachment(currentAttachment);
                    }
                    Trace.endBusinessRule(currentAttachment.getSDCId() + "." + "PostAddAttachment", false);
                    sdcPostRules.endRule();
                    if (errorHandler.size() > 0 && errorHandler.hasErrors()) {
                        throw new ServiceException(errorHandler.getEncodedString());
                    }
                }
                catch (SapphireException e) {
                    if (errorHandler.size() > 0 && errorHandler.hasErrors()) {
                        throw new ServiceException(errorHandler.getEncodedString(), e);
                    }
                    throw new ServiceException("PostAddAttachment Business Rule Failed", e);
                }
            }
            if (index) {
                Indexer.indexAttachment(this.connectionInfo, currentAttachment.getSDCId(), currentAttachment.getKeyId1(), currentAttachment.getKeyId2(), currentAttachment.getKeyId3(), currentAttachment.getAttachmentNum());
            }
        }
        catch (Exception e) {
            throw new ServiceException(e);
        }
        finally {
            if (currentAttachment.getInputStream() != null) {
                try {
                    currentAttachment.getInputStream().close();
                }
                catch (Exception exception) {}
            }
        }
    }

    private void addSDIAttachment(String sdcid, String keyid1, String keyid2, String keyid3, String description, String type, String oleclass, String filename, String url, String attachmentclob, String linksdcid, String linkkeyid1, String linkkeyid2, String linkkeyid3, String linkattachmentnum, InputStream inputStream, String uploadto, boolean applylock, boolean index, PropertyList additionalColumns, String attachmentpolicynode) throws ServiceException {
        this.logInfo("Adding SDI attachment (2) ");
        if (sdcid.length() == 0) {
            throw new ServiceException("INVALID_PROPERTY", "No SDC specified");
        }
        String[] keyid1prop = StringUtil.split(keyid1, ";");
        String[] keyid2prop = StringUtil.split(keyid2, ";");
        String[] keyid3prop = StringUtil.split(keyid3, ";");
        String[] attachdescprop = StringUtil.split(description, ";");
        String[] oleclassprop = StringUtil.split(oleclass, ";");
        String[] urlprop = StringUtil.split(url, ";");
        String[] fileprop = StringUtil.split(filename, ";");
        String[] typeflagprop = StringUtil.split(type, ";");
        DAMProcessor dam = this.getDAMProcessor();
        StringHolder rsetidHolder = new StringHolder();
        String rsetid = "";
        if (applylock) {
            if (dam.createLockedRSet(sdcid, keyid1, keyid2, keyid3, rsetidHolder) == 1) {
                rsetid = rsetidHolder.value;
            }
        } else if (dam.createRSet(sdcid, keyid1, keyid2, keyid3, rsetidHolder) == 1) {
            rsetid = rsetidHolder.value;
        }
        if (rsetid.length() == 0) {
            throw new ServiceException("CREATE_RSET_FAILURE", "Failed to create RSET for add attachment");
        }
        DBUtil dbu = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            dbu.setConnection(this.sapphireConnection);
            Calendar now = DateTimeUtil.getNowCalendar();
            StringBuffer attachmentnumret = new StringBuffer();
            QueryProcessor qp = new QueryProcessor(this.connectionInfo.getConnectionId());
            this.generateTraceLog(sdcid, keyid1, keyid2, keyid3, "Adding Attachment", additionalColumns);
            this.removeInvalidColumns(qp, additionalColumns);
            String sql = "SELECT sdiattachment.sdcid, sdiattachment.keyid1, sdiattachment.keyid2, sdiattachment.keyid3, sdiattachment.attachmentnum  FROM sdiattachment, rsetitems WHERE sdiattachment.sdcid = rsetitems.sdcid AND sdiattachment.keyid1 = rsetitems.keyid1 AND sdiattachment.keyid2 = rsetitems.keyid2 AND sdiattachment.keyid3 = rsetitems.keyid3 AND rsetitems.rsetid = ? ORDER BY sdiattachment.keyid1, sdiattachment.keyid2, sdiattachment.keyid3, sdiattachment.attachmentnum desc";
            this.logInfo("Loading the selected samples existing data.");
            dbu.createPreparedResultSet(sql, rsetid);
            DataSet ds = new DataSet(dbu.getResultSet());
            HashMap<String, Integer> attachnumcache = new HashMap<String, Integer>();
            PropertyListCollection attachmentColDef = null;
            M18NUtil m18NUtil = null;
            try {
                DDTService ddtService = new DDTService(this.sapphireConnection);
                attachmentColDef = ddtService.getSDCProperties("SDIAttachment").getCollection("columns");
            }
            catch (Exception e) {
                this.logger.warn("Failed to obtain attachment column defintion.");
            }
            try {
                m18NUtil = new M18NUtil(new ConnectionInfo(this.sapphireConnection));
            }
            catch (Exception e) {
                this.logger.warn("Failed to obtain m18n.");
            }
            PropertyList attachmentPolicy = this.getConfigurationProcessor().getPolicy("AttachmentPolicy", attachmentpolicynode != null && attachmentpolicynode.length() > 0 ? attachmentpolicynode : "Sapphire Custom");
            for (int sdi = 0; sdi < keyid1prop.length; ++sdi) {
                Attachment currentAttachment = new Attachment();
                currentAttachment.setInputStream(inputStream);
                currentAttachment.setSDCId(sdcid);
                currentAttachment.setKeyId1(keyid1prop[sdi]);
                currentAttachment.setKeyId2(keyid2prop.length == 0 || keyid2prop.length <= sdi || keyid2prop[sdi].length() == 0 ? "(null)" : keyid2prop[sdi]);
                currentAttachment.setKeyId3(keyid3prop.length == 0 || keyid3prop.length <= sdi || keyid3prop[sdi].length() == 0 ? "(null)" : keyid3prop[sdi]);
                currentAttachment.setFilename(fileprop.length == 0 || fileprop.length <= sdi || fileprop[sdi].length() == 0 ? "(Unknown)" : fileprop[sdi]);
                if (fileprop.length == 1 && fileprop[0].length() > 0) {
                    currentAttachment.setFilename(fileprop[0]);
                }
                if (typeflagprop.length == 0) {
                    currentAttachment.setType("");
                } else if (typeflagprop.length <= sdi) {
                    currentAttachment.setType(typeflagprop[0]);
                } else {
                    currentAttachment.setType(typeflagprop[sdi]);
                }
                if (urlprop.length == 0) {
                    currentAttachment.setUrl("");
                } else if (urlprop.length <= sdi) {
                    currentAttachment.setUrl(urlprop[0]);
                } else {
                    currentAttachment.setUrl(urlprop[sdi]);
                }
                currentAttachment.setDescription(attachdescprop.length == 0 || attachdescprop.length < sdi + 1 || attachdescprop[sdi].length() == 0 ? "" : attachdescprop[sdi]);
                if (attachdescprop.length == 1 && attachdescprop[0].length() > 0) {
                    currentAttachment.setDescription(attachdescprop[0]);
                }
                currentAttachment.setOleClass(oleclassprop == null || oleclassprop.length == 0 || oleclassprop.length <= sdi || oleclassprop[sdi].length() == 0 ? "" : oleclassprop[sdi]);
                if (oleclassprop.length == 1 && oleclassprop[0].length() > 0) {
                    currentAttachment.setOleClass(oleclassprop[0]);
                }
                if (uploadto != null && uploadto.length() > 0) {
                    currentAttachment.setUploadTo(uploadto);
                }
                currentAttachment.setAttachmentNum(1);
                Integer cachednum = (Integer)attachnumcache.get(currentAttachment.getKeyId1() + ";" + currentAttachment.getKeyId2() + ";" + currentAttachment.getKeyId3());
                if (cachednum != null) {
                    currentAttachment.setAttachmentNum(cachednum);
                } else {
                    HashMap<String, String> findmap = new HashMap<String, String>();
                    findmap.put("keyid1", currentAttachment.getKeyId1());
                    findmap.put("keyid2", currentAttachment.getKeyId2());
                    findmap.put("keyid3", currentAttachment.getKeyId3());
                    int findrow = ds.findRow(findmap);
                    if (findrow >= 0) {
                        currentAttachment.setAttachmentNum(ds.getBigDecimal(findrow, "attachmentnum").intValue() + 1);
                    }
                }
                attachnumcache.put(currentAttachment.getKeyId1() + ";" + currentAttachment.getKeyId2() + ";" + currentAttachment.getKeyId3(), new Integer(currentAttachment.getAttachmentNum() + 1));
                if (attachmentnumret.length() == 0) {
                    attachmentnumret.append(currentAttachment.getAttachmentNum());
                } else {
                    attachmentnumret.append(";").append(currentAttachment.getAttachmentNum());
                }
                if (additionalColumns != null && additionalColumns.size() > 0) {
                    Iterator it = additionalColumns.keySet().iterator();
                    while (it.hasNext()) {
                        String curkey = it.next().toString();
                        String value = additionalColumns.getProperty(curkey);
                        if (value.indexOf(";") > -1) {
                            String[] vals = value.split(";");
                            value = vals.length > sdi ? vals[sdi] : vals[0];
                        }
                        value = StringUtil.replaceAll(value, "#semicolon#", ";");
                        if (curkey.equalsIgnoreCase("attachmentclass")) {
                            currentAttachment.setAttachmentClass(value);
                            continue;
                        }
                        if (curkey.equalsIgnoreCase("attachmentclob")) {
                            currentAttachment.setClob(value);
                            continue;
                        }
                        if (curkey.equalsIgnoreCase("thumbnailimage")) {
                            currentAttachment.setThumbnailImage(value);
                            continue;
                        }
                        if (curkey.equalsIgnoreCase("sourcefilename")) {
                            if (currentAttachment.getSourceFilename() != null && currentAttachment.getSourceFilename().length() != 0 || additionalColumns.getProperty("sourcefilename").length() <= 0) continue;
                            currentAttachment.setSourceFilename(value);
                            continue;
                        }
                        currentAttachment.setAdditionalColumn(curkey, value);
                    }
                }
                try {
                    if (currentAttachment.getSourceFilename().length() == 0) {
                        currentAttachment.setSourceFilename(new File(currentAttachment.getFilename()).getName());
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
                this.addSDIAttachment(currentAttachment, index, attachmentpolicynode, attachmentPolicy, attachmentColDef, m18NUtil, dbu);
            }
            if (additionalColumns != null) {
                additionalColumns.setProperty("attachmentnum", attachmentnumret.toString());
            }
        }
        catch (Exception e) {
            throw new ServiceException("GENERAL_ERROR", "Failed to add the attachment: " + e.getMessage(), e);
        }
        finally {
            dbu.reset();
        }
        if (rsetid != null) {
            dam.clearRSet(rsetid);
        }
    }

    private void removeInvalidColumns(QueryProcessor query, PropertyList additionalColumns) {
        block6: {
            String col;
            Iterator it;
            if (additionalColumns == null || additionalColumns.size() <= 0) break block6;
            this.logInfo("Validating additional columns...");
            StringBuffer getSql = new StringBuffer();
            getSql.append("SELECT columnid FROM syscolumn WHERE tableid = '").append("sdiattachment").append("' ");
            ArrayList<String> toRemove = new ArrayList<String>();
            DataSet data = query.getSqlDataSet(getSql.toString());
            if (data != null && data.size() > 0) {
                it = additionalColumns.keySet().iterator();
                while (it.hasNext()) {
                    col = it.next().toString();
                    if (col.length() <= 0) continue;
                    HashMap<String, String> find = new HashMap<String, String>();
                    find.put("columnid", col.toLowerCase());
                    int found = data.findRow(find);
                    if (found == -1) {
                        find = new HashMap();
                        find.put("columnid", col.toUpperCase());
                        found = data.findRow(find);
                    }
                    if (found != -1) continue;
                    this.logger.debug("Column " + col + " in additional columns invalid, thus exclude.");
                    toRemove.add(col);
                }
            } else {
                this.logger.warn("Could not obtain attachment columns thus remove all additional coluns.");
                it = additionalColumns.keySet().iterator();
                while (it.hasNext()) {
                    col = it.next().toString();
                    toRemove.add(col);
                }
            }
            for (int i = 0; i < toRemove.size(); ++i) {
                additionalColumns.deleteProperty((String)toRemove.get(i));
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void editSDIAttachment(HashMap columns, byte[] data) throws ServiceException {
        this.logInfo("Editing SDI attachment (-1)...");
        ByteArrayInputStream byteArrayInputStream = data != null ? new ByteArrayInputStream(data) : null;
        try {
            this.editSDIAttachment(columns, byteArrayInputStream);
        }
        finally {
            try {
                if (byteArrayInputStream != null) {
                    byteArrayInputStream.close();
                }
            }
            catch (IOException iOException) {}
        }
    }

    public void editSDIAttachment(HashMap columns, InputStream data) throws ServiceException {
        this.logInfo("Editing SDI attachment (3)...");
        PropertyList props = new PropertyList(columns);
        String keyid1 = props.getProperty("keyid1", "");
        String keyid2 = props.getProperty("keyid2", "");
        String keyid3 = props.getProperty("keyid3", "");
        String sdcid = props.getProperty("sdcid", "");
        String attachmentnum = props.getProperty("attachmentnum", "");
        String description = props.getProperty("attachmentdesc", props.getProperty("description", props.getProperty("desc", "")));
        String type = props.getProperty("type", props.getProperty("typeflag", ""));
        String oleclass = props.getProperty("oleclass", "");
        String filename = props.getProperty("filename", "");
        boolean applylock = props.getProperty("applylock").equalsIgnoreCase("Y");
        boolean index = props.getProperty("index", "Y").equalsIgnoreCase("Y");
        String attachmentpolicynode = props.getProperty("attachmentpolicynode");
        props.deleteProperty("keyid1");
        props.deleteProperty("keyid2");
        props.deleteProperty("keyid3");
        props.deleteProperty("sdcid");
        props.deleteProperty("attachmentnum");
        props.deleteProperty("attachmentdesc");
        props.deleteProperty("description");
        props.deleteProperty("desc");
        props.deleteProperty("type");
        props.deleteProperty("typeflag");
        props.deleteProperty("oleclass");
        props.deleteProperty("filename");
        props.deleteProperty("applylock");
        props.deleteProperty("index");
        props.deleteProperty("attachmentpolicynode");
        this.editSDIAttachment(sdcid, keyid1, keyid2, keyid3, attachmentnum, description, oleclass, filename, type, data, applylock, index, props, attachmentpolicynode);
    }

    private void editSDIAttachment(String sdcid, String keyid1, String keyid2, String keyid3, String attachmentnum, String description, String oleclass, String filename, String type, InputStream inputStream, boolean applylock, boolean index) throws ServiceException {
        this.logInfo("Editing SDI attachment (1) for " + new SDI(sdcid, keyid1, keyid2, keyid3).getKeyText());
        this.editSDIAttachment(sdcid, keyid1, keyid2, keyid3, attachmentnum, description, oleclass, filename, type, inputStream, applylock, index, null);
    }

    private void editSDIAttachment(String sdcid, String keyid1, String keyid2, String keyid3, String attachmentnum, String description, String oleclass, String filename, String type, InputStream inputStream, boolean applylock, boolean index, PropertyList additionalColumns) throws ServiceException {
        this.editSDIAttachment(sdcid, keyid1, keyid2, keyid3, attachmentnum, description, oleclass, filename, type, inputStream, applylock, index, additionalColumns, "");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void editSDIAttachment(sapphire.attachment.Attachment attachment, boolean applylock, boolean index, String attachmentpolicynode) throws ServiceException {
        this.logInfo("Editing SDI attachment (4) for " + new SDI(attachment.getSDCId(), attachment.getKeyId1(), attachment.getKeyId2(), attachment.getKeyId3()).getKeyText());
        if (attachment.getSDCId().length() == 0) {
            throw new ServiceException("INVALID_PROPERTY", "No SDC specified");
        }
        DAMProcessor dam = this.getDAMProcessor();
        StringHolder rsetidHolder = new StringHolder();
        String rsetid = "";
        if (applylock) {
            if (dam.createLockedRSet(attachment.getSDCId(), attachment.getKeyId1(), attachment.getKeyId2(), attachment.getKeyId3(), rsetidHolder) == 1) {
                rsetid = rsetidHolder.value;
            }
        } else if (dam.createRSet(attachment.getSDCId(), attachment.getKeyId1(), attachment.getKeyId2(), attachment.getKeyId3(), rsetidHolder) == 1) {
            rsetid = rsetidHolder.value;
        }
        if (rsetid.length() == 0) {
            throw new ServiceException("CREATE_RSET_FAILURE", "Failed to create RSET for add attachment");
        }
        try {
            Attachment currAtt = (Attachment)attachment;
            DBUtil dbu = new DBUtil(this.sapphireConnection.getConnectionId());
            dbu.setConnection(this.sapphireConnection);
            this.generateTraceLog(currAtt.getSDCId(), currAtt.getKeyId1(), currAtt.getKeyId2(), currAtt.getKeyId3(), "Editing Attachment", currAtt.getAdditionalColumns());
            this.removeInvalidColumns(new QueryProcessor(this.connectionInfo.getConnectionId()), currAtt.getAdditionalColumns());
            try {
                this.editSDIAttachment(currAtt, null, index, attachmentpolicynode, null, null, null, dbu);
            }
            finally {
                dbu.reset();
            }
        }
        finally {
            if (rsetid != null) {
                dam.clearRSet(rsetid);
            }
        }
    }

    private void clearLocalCache(Attachment currentAttachment) {
        String cachedZip;
        String cachedFile = (String)CacheUtil.get(this.sapphireConnection.getDatabaseId(), "AttachmentLocalFile", currentAttachment.getSDCId() + ";" + currentAttachment.getKeyId1() + ";" + currentAttachment.getKeyId2() + ";" + currentAttachment.getKeyId3() + ";" + currentAttachment.getAttachmentNum());
        if (cachedFile != null) {
            Path p = Paths.get(cachedFile, new String[0]);
            try {
                Files.deleteIfExists(p);
            }
            catch (Exception e) {
                this.logger.warn("Failed to delete local cache");
            }
            CacheUtil.remove(this.sapphireConnection.getDatabaseId(), "AttachmentLocalFile", currentAttachment.getSDCId() + ";" + currentAttachment.getKeyId1() + ";" + currentAttachment.getKeyId2() + ";" + currentAttachment.getKeyId3() + ";" + currentAttachment.getAttachmentNum(), true);
        }
        if ((cachedZip = (String)CacheUtil.get(this.sapphireConnection.getDatabaseId(), "AttachmentExplodedPath", currentAttachment.getSDCId() + ";" + currentAttachment.getKeyId1() + ";" + currentAttachment.getKeyId2() + ";" + currentAttachment.getKeyId3() + ";" + currentAttachment.getAttachmentNum())) != null) {
            Path p = Paths.get(cachedZip, new String[0]);
            if (Files.exists(p, new LinkOption[0])) {
                try {
                    FileUtil.deleteDirectory(p.toFile());
                }
                catch (Exception e) {
                    this.logger.warn("Failed to delete exploded cache");
                }
            }
            CacheUtil.remove(this.sapphireConnection.getDatabaseId(), "AttachmentExplodedPath", currentAttachment.getSDCId() + ";" + currentAttachment.getKeyId1() + ";" + currentAttachment.getKeyId2() + ";" + currentAttachment.getKeyId3() + ";" + currentAttachment.getAttachmentNum(), true);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void editSDIAttachment(Attachment attachment, sapphire.attachment.Attachment preEditAttachmentIn, boolean index, String attachmentpolicynode, PropertyList attachmentPolicy, PropertyListCollection attachmentColDef, M18NUtil m18NUtil, DBUtil db) throws ServiceException {
        Attachment currentAttachment = attachment;
        try {
            int psn;
            boolean defaultToCompat;
            String apn;
            boolean updateTypeflag;
            int nextauditseq = -1;
            if (currentAttachment.getAdditionalColumns().containsKey("auditsequence")) {
                try {
                    nextauditseq = Integer.parseInt(currentAttachment.getAdditionalColumn("auditsequence", "-1"));
                    nextauditseq = nextauditseq > -1 ? nextauditseq + 1 : -1;
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
                currentAttachment.getAdditionalColumns().remove("auditsequence");
            }
            if (m18NUtil == null) {
                try {
                    m18NUtil = new M18NUtil(new ConnectionInfo(this.sapphireConnection));
                }
                catch (Exception e) {
                    this.logger.warn("Failed to obtain m18n.");
                }
            }
            if (db == null) {
                db = new DBUtil(this.sapphireConnection.getConnectionId());
                db.setConnection(this.sapphireConnection);
            }
            sapphire.attachment.Attachment preEditAttachment = preEditAttachmentIn == null ? Attachment.getAttachment(currentAttachment.getSDCId(), currentAttachment.getKeyId1(), currentAttachment.getKeyId2(), currentAttachment.getKeyId3(), currentAttachment.getAttachmentNum(), this.getQueryProcessor(), this.connectionInfo.getConnectionId()) : preEditAttachmentIn;
            boolean updateOle = currentAttachment.getOleClass() != null && currentAttachment.getOleClass().length() > 0;
            boolean updateDesc = currentAttachment.getDescription() != null && currentAttachment.getDescription().length() > 0;
            boolean bl = updateTypeflag = currentAttachment.getType() != null && currentAttachment.getType().length() > 0;
            if (currentAttachment.getType() != null && currentAttachment.getType().equalsIgnoreCase(preEditAttachment.getType())) {
                updateTypeflag = false;
            }
            currentAttachment.setType(Attachment.correctTypeFlag(currentAttachment.getType()));
            if (currentAttachment.getType() != null && currentAttachment.getType().equalsIgnoreCase("U")) {
                currentAttachment.setFilename(Attachment.evaluateFileNameExpressions(currentAttachment.getSDCId(), currentAttachment.getKeyId1(), currentAttachment.getKeyId2(), currentAttachment.getKeyId3(), currentAttachment.getAttachmentNum(), nextauditseq, this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()), currentAttachment.getFilename(), currentAttachment));
            }
            String string = apn = attachmentpolicynode != null && attachmentpolicynode.length() > 0 ? attachmentpolicynode : "Sapphire Custom";
            if (currentAttachment.getAttachmentClass() == null || currentAttachment.getAttachmentClass().length() > 0) {
                // empty if block
            }
            if (attachmentPolicy == null) {
                attachmentPolicy = this.getConfigurationProcessor().getPolicy("AttachmentPolicy", apn);
            }
            if (attachmentColDef == null) {
                try {
                    DDTService ddtService = new DDTService(this.sapphireConnection);
                    attachmentColDef = ddtService.getSDCProperties("SDIAttachment").getCollection("columns");
                }
                catch (Exception e) {
                    this.logger.warn("Failed to obtain attachment column defintion.");
                }
            }
            if (attachment.getTempId() != null && attachment.getTempId().length() > 0) {
                attachment.readTempFile(this.getQueryProcessor(), this.getActionProcessor(), this.getConnectionId());
            }
            boolean updateFilename = currentAttachment.getFilename() != null && currentAttachment.getFilename().length() > 0;
            boolean updateSouceFilename = false;
            if (currentAttachment.getFilename() != null && currentAttachment.getFilename().equals(preEditAttachment.getFilename())) {
                updateFilename = false;
            }
            boolean bl2 = defaultToCompat = !updateFilename && !currentAttachment.hasData();
            if (this.canEncrypt(currentAttachment)) {
                currentAttachment.setInputStream(FileTransfer.getCipherInputStream(currentAttachment.getInputStream(), false));
            }
            MessageDigest hashingDigester = null;
            if (this.canGenerateHash(currentAttachment, preEditAttachment)) {
                DigestInputStream is = FileTransfer.getHashedInputSteam(currentAttachment.getInputStream(), SDMSCollector.defaultHashingAlgorithm);
                currentAttachment.setInputStream(is);
                hashingDigester = is.getMessageDigest();
            }
            if (this.canCompress(currentAttachment)) {
                currentAttachment.setInputStream(FileTransfer.getCompressedInputStream(currentAttachment.getInputStream()));
            }
            if (currentAttachment.getTriggerBusinessRule()) {
                ErrorHandler errorHandler = new ErrorHandler();
                try {
                    BaseSDCRules sdcPreRules = BaseSDCRules.getInstance(this.sapphireConnection, errorHandler, currentAttachment.getSDCId(), this.getSDCProcessor().getProperties(currentAttachment.getSDCId()), "PreEditAttachment");
                    Trace.startBusinessRule(currentAttachment.getSDCId() + "." + "PreEditAttachment", true);
                    sdcPreRules.preEditSDIAttachment(currentAttachment, preEditAttachment);
                    Trace.endBusinessRule(currentAttachment.getSDCId() + "." + "PreEditAttachment", true);
                    Trace.startBusinessRule(currentAttachment.getSDCId() + "." + "PreEditAttachment", false);
                    for (BaseSDCRules customRules : sdcPreRules.getCustomRuleList()) {
                        customRules.preEditSDIAttachment(currentAttachment, preEditAttachment);
                    }
                    Trace.endBusinessRule(currentAttachment.getSDCId() + "." + "PreEditAttachment", false);
                    sdcPreRules.endRule();
                    if (errorHandler.size() > 0 && errorHandler.hasErrors()) {
                        throw new ServiceException(errorHandler.getEncodedString());
                    }
                }
                catch (SapphireException e) {
                    if (errorHandler.size() > 0 && errorHandler.hasErrors()) {
                        throw new ServiceException(errorHandler.getEncodedString(), e);
                    }
                    throw new ServiceException("PreEditAttachment Business Rule Failed", e);
                }
            }
            BaseAttachmentRepository attachmentRepository = currentAttachment.getAttachmentRepository(this.sapphireConnection, attachmentPolicy, defaultToCompat, preEditAttachment);
            AtomicBoolean isContentChanged = new AtomicBoolean();
            LabVantageClassLoader.executeCode(attachmentRepository.getClassLoader(), () -> isContentChanged.set(attachmentRepository.preEditSDIAttachment(currentAttachment, preEditAttachment)), false);
            if (isContentChanged.get() && currentAttachment.getFilename() != null && !currentAttachment.getFilename().equals(preEditAttachment.getFilename())) {
                updateFilename = true;
            }
            if (isContentChanged.get() && currentAttachment.getSourceFilename() != null && preEditAttachment.getSourceFilename() != null && !currentAttachment.getSourceFilename().equals(preEditAttachment.getSourceFilename())) {
                updateSouceFilename = true;
            }
            if (isContentChanged.get()) {
                this.clearLocalCache(currentAttachment);
            }
            StringBuffer additionalSQL = new StringBuffer();
            if (currentAttachment.getAdditionalColumns() != null && currentAttachment.getAdditionalColumns().size() > 0) {
                Iterator it = currentAttachment.getAdditionalColumns().keySet().iterator();
                while (it.hasNext()) {
                    String curkey = it.next().toString();
                    String value = currentAttachment.getAdditionalColumn(curkey, "");
                    if (value == null) continue;
                    value = StringUtil.replaceAll(value, "'", "''");
                    if (curkey.equalsIgnoreCase("contentrevision") || curkey.equalsIgnoreCase("linkattachmentnum") || curkey.equalsIgnoreCase("auditsequence")) {
                        if (value.length() <= 0) continue;
                        additionalSQL.append(" ").append(curkey).append(" = ");
                        additionalSQL.append(" ").append(value).append(", ");
                        continue;
                    }
                    if (curkey.equalsIgnoreCase("usersequence")) {
                        if (value.length() <= 0) continue;
                        additionalSQL.append(" ").append(curkey).append(" = ");
                        additionalSQL.append(" ").append(value).append(", ");
                        continue;
                    }
                    PropertyList col = null;
                    if (attachmentColDef != null) {
                        col = attachmentColDef.find("columnid", curkey);
                    }
                    if (col != null) {
                        String dt = col.getProperty("datatype", "C");
                        if (dt.equalsIgnoreCase("N")) {
                            if (value.length() == 0) {
                                value = "(null)";
                            }
                            additionalSQL.append(" ").append(curkey).append(" = ");
                            additionalSQL.append(" ").append(value).append(", ");
                            continue;
                        }
                        if (dt.equalsIgnoreCase("C")) {
                            if (value.length() == 0) {
                                value = "(null)";
                            }
                            additionalSQL.append(" ").append(curkey).append(" = ");
                            if (value.equalsIgnoreCase("(null)")) {
                                additionalSQL.append(" ").append(value).append(", ");
                                continue;
                            }
                            additionalSQL.append(" '").append(value).append("', ");
                            continue;
                        }
                        if (dt.equalsIgnoreCase("D")) {
                            if (value.length() > 0 && m18NUtil != null) {
                                Timestamp datevalue = new Timestamp(m18NUtil.parseCalendar(value).getTimeInMillis());
                                additionalSQL.append(" ").append(curkey).append(" = ");
                                additionalSQL.append(" timestamp'").append(datevalue).append("', ");
                                continue;
                            }
                            additionalSQL.append(" ").append(curkey).append(" = ");
                            additionalSQL.append(" (null), ");
                            continue;
                        }
                        additionalSQL.append(" ").append(curkey).append(" = ");
                        additionalSQL.append(" '").append(value).append("', ");
                        continue;
                    }
                    additionalSQL.append(" ").append(curkey).append(" = ");
                    additionalSQL.append(" '").append(value).append("', ");
                }
            }
            boolean isUpdateBlob = currentAttachment.getBlob() != null;
            this.logInfo("Updating attachment row for " + currentAttachment.getSDCId() + ", " + currentAttachment.getKeyId1() + ", " + currentAttachment.getKeyId2() + ", " + currentAttachment.getKeyId3() + ", " + currentAttachment.getAttachmentNum());
            String sqlU = "update sdiattachment set " + (isUpdateBlob ? "attachment=?," : "") + (updateFilename ? "filename = ?, " : "") + (updateSouceFilename ? "sourcefilename = ?, " : "") + (updateTypeflag ? "typeflag='" + currentAttachment.getType() + "'," : "") + (updateDesc ? " attachmentdesc = ?, " : "") + (updateOle ? "oleclass='" + currentAttachment.getOleClass() + "'," : "") + (isContentChanged.get() ? "contentrevision=contentrevision + 1," : "") + (additionalSQL.length() > 0 ? additionalSQL.toString() : "") + " moddt = ?, modby = ?, modtool = '" + this.connectionInfo.getTool() + "', attachmentclass = ?, attachmentrepositoryid = ?,  attachmentrepositorynodeid = ?,  externalrepository = ?, " + (isContentChanged.get() ? " attachmentsize = ?, " : "") + " url = ?, attachmentclob = ?";
            if (isContentChanged.get()) {
                sqlU = sqlU + ", thumbnailimage = ?";
            }
            String whereClause = " where sdcid='" + currentAttachment.getSDCId() + "' and keyid1=? and keyid2=? and keyid3=? and attachmentnum='" + currentAttachment.getAttachmentNum() + "'";
            sqlU = sqlU + whereClause;
            PreparedStatement updateSQL = db.prepareStatement(sqlU);
            this.logDebug("Update SQL = " + sqlU);
            Calendar now = DateTimeUtil.getNowCalendar();
            if (isUpdateBlob) {
                updateSQL.setBlob(1, attachment.getBlob());
                psn = 2;
                if (updateFilename) {
                    updateSQL.setString(psn++, currentAttachment.getFilename());
                }
                if (updateSouceFilename) {
                    updateSQL.setString(psn++, currentAttachment.getSourceFilename());
                }
                if (updateDesc) {
                    updateSQL.setString(psn++, currentAttachment.getDescription());
                }
                updateSQL.setTimestamp(psn++, new Timestamp(now.getTime().getTime()));
                updateSQL.setString(psn++, this.connectionInfo.getSysuserId());
                updateSQL.setString(psn++, currentAttachment.getAttachmentClass());
                updateSQL.setString(psn++, currentAttachment.getAttachmentRepositoryId());
                updateSQL.setString(psn++, currentAttachment.getAttachmentRepositoryNodeId());
                updateSQL.setString(psn++, currentAttachment.getRepositoryId());
                if (isContentChanged.get()) {
                    updateSQL.setLong(psn++, currentAttachment.getSize());
                }
                updateSQL.setString(psn++, currentAttachment.getUrl() != null ? currentAttachment.getUrl() : "");
                updateSQL.setCharacterStream(psn++, (Reader)(currentAttachment.getClob() != null ? new StringReader(currentAttachment.getClob()) : new StringReader("")), currentAttachment.getClob() != null ? currentAttachment.getClob().length() : 0);
                if (isContentChanged.get()) {
                    updateSQL.setCharacterStream(psn++, (Reader)(currentAttachment.getThumbnailImage() != null ? new StringReader(currentAttachment.getThumbnailImage()) : new StringReader("")), currentAttachment.getThumbnailImage() != null ? currentAttachment.getThumbnailImage().length() : 0);
                }
                updateSQL.setString(psn++, currentAttachment.getKeyId1());
                updateSQL.setString(psn++, currentAttachment.getKeyId2());
                updateSQL.setString(psn, currentAttachment.getKeyId3());
            } else {
                psn = 1;
                if (updateFilename) {
                    updateSQL.setString(psn++, currentAttachment.getFilename());
                }
                if (updateSouceFilename) {
                    updateSQL.setString(psn++, currentAttachment.getSourceFilename());
                }
                if (updateDesc) {
                    updateSQL.setString(psn++, currentAttachment.getDescription());
                }
                updateSQL.setTimestamp(psn++, new Timestamp(now.getTime().getTime()));
                updateSQL.setString(psn++, this.connectionInfo.getSysuserId());
                updateSQL.setString(psn++, currentAttachment.getAttachmentClass());
                updateSQL.setString(psn++, currentAttachment.getAttachmentRepositoryId());
                updateSQL.setString(psn++, currentAttachment.getAttachmentRepositoryNodeId());
                updateSQL.setString(psn++, currentAttachment.getRepositoryId());
                if (isContentChanged.get()) {
                    updateSQL.setLong(psn++, currentAttachment.getSize());
                }
                updateSQL.setString(psn++, currentAttachment.getUrl() != null ? currentAttachment.getUrl() : "");
                updateSQL.setCharacterStream(psn++, (Reader)(currentAttachment.getClob() != null ? new StringReader(currentAttachment.getClob()) : new StringReader("")), currentAttachment.getClob() != null ? currentAttachment.getClob().length() : 0);
                if (isContentChanged.get()) {
                    updateSQL.setCharacterStream(psn++, (Reader)(currentAttachment.getThumbnailImage() != null ? new StringReader(currentAttachment.getThumbnailImage()) : new StringReader("")), currentAttachment.getThumbnailImage() != null ? currentAttachment.getThumbnailImage().length() : 0);
                }
                updateSQL.setString(psn++, currentAttachment.getKeyId1());
                updateSQL.setString(psn++, currentAttachment.getKeyId2());
                updateSQL.setString(psn, currentAttachment.getKeyId3());
            }
            updateSQL.execute();
            updateSQL.close();
            BaseAttachmentRepository attachmentRepositoryPost = currentAttachment.getAttachmentRepository(this.sapphireConnection, attachmentPolicy, defaultToCompat, preEditAttachment);
            LabVantageClassLoader.executeCode(attachmentRepositoryPost.getClassLoader(), () -> attachmentRepositoryPost.postEditSDIAttachment(currentAttachment, preEditAttachment), false);
            if (hashingDigester != null) {
                byte[] md5sum = hashingDigester.digest();
                BigInteger bigInt = new BigInteger(1, md5sum);
                long hash = bigInt.longValue();
                this.logger.debug("Generated File Hash:" + hash);
                if (hash != 0L) {
                    String hashUpdate = "update sdiattachment set  datahash = ?" + whereClause;
                    PreparedStatement hashUpdateSQL = db.prepareStatement(hashUpdate);
                    hashUpdateSQL.setString(1, "" + hash);
                    hashUpdateSQL.setString(2, currentAttachment.getKeyId1());
                    hashUpdateSQL.setString(3, currentAttachment.getKeyId2());
                    hashUpdateSQL.setString(4, currentAttachment.getKeyId3());
                    try {
                        hashUpdateSQL.execute();
                    }
                    finally {
                        hashUpdateSQL.close();
                    }
                }
            }
            if (currentAttachment.getTriggerBusinessRule()) {
                ErrorHandler errorHandler = new ErrorHandler();
                try {
                    BaseSDCRules sdcPostRules = BaseSDCRules.getInstance(this.sapphireConnection, errorHandler, currentAttachment.getSDCId(), this.getSDCProcessor().getProperties(currentAttachment.getSDCId()), "PostEditAttachment");
                    Trace.startBusinessRule(currentAttachment.getSDCId() + "." + "PostEditAttachment", true);
                    sdcPostRules.postEditSDIAttachment(currentAttachment);
                    Trace.endBusinessRule(currentAttachment.getSDCId() + "." + "PostEditAttachment", true);
                    Trace.startBusinessRule(currentAttachment.getSDCId() + "." + "PostEditAttachment", false);
                    for (BaseSDCRules customRules : sdcPostRules.getCustomRuleList()) {
                        customRules.postEditSDIAttachment(currentAttachment);
                    }
                    Trace.endBusinessRule(currentAttachment.getSDCId() + "." + "PostEditAttachment", false);
                    sdcPostRules.endRule();
                    if (errorHandler.size() > 0 && errorHandler.hasErrors()) {
                        throw new ServiceException(errorHandler.getEncodedString());
                    }
                }
                catch (SapphireException e) {
                    if (errorHandler.size() > 0 && errorHandler.hasErrors()) {
                        throw new ServiceException(errorHandler.getEncodedString(), e);
                    }
                    throw new ServiceException("PostEditAttachment Business Rule Failed", e);
                }
            }
            if (index) {
                Indexer.indexAttachment(this.connectionInfo, currentAttachment.getSDCId(), currentAttachment.getKeyId1(), currentAttachment.getKeyId2(), currentAttachment.getKeyId3(), currentAttachment.getAttachmentNum());
            }
        }
        catch (Exception e) {
            throw new ServiceException(e);
        }
        finally {
            if (currentAttachment.getInputStream() != null) {
                try {
                    currentAttachment.getInputStream().close();
                }
                catch (Exception exception) {}
            }
        }
    }

    private void editSDIAttachment(String sdcid, String keyid1, String keyid2, String keyid3, String attachmentnum, String description, String oleclass, String filename, String type, InputStream inputStream, boolean applylock, boolean index, PropertyList additionalColumns, String attachmentpolicynode) throws ServiceException {
        this.logInfo("Editing SDI attachment (2) for " + new SDI(sdcid, keyid1, keyid2, keyid3).getKeyText());
        if (sdcid.length() == 0) {
            throw new ServiceException("INVALID_PROPERTY", "No SDC specified");
        }
        DAMProcessor dam = this.getDAMProcessor();
        StringHolder rsetidHolder = new StringHolder();
        String rsetid = "";
        if (applylock) {
            if (dam.createLockedRSet(sdcid, keyid1, keyid2, keyid3, rsetidHolder) == 1) {
                rsetid = rsetidHolder.value;
            }
        } else if (dam.createRSet(sdcid, keyid1, keyid2, keyid3, rsetidHolder) == 1) {
            rsetid = rsetidHolder.value;
        }
        if (rsetid.length() == 0) {
            throw new ServiceException("CREATE_RSET_FAILURE", "Failed to create RSET for add attachment");
        }
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            String[] keyid1prop = StringUtil.split(keyid1, ";");
            String[] keyid2prop = keyid2 != null && keyid2.length() > 0 ? StringUtil.split(keyid2, ";") : StringUtil.split("", ";");
            String[] keyid3prop = keyid3 != null && keyid3.length() > 0 ? StringUtil.split(keyid3, ";") : StringUtil.split("", ";");
            String rsetId = this.getDAMProcessor().createRSet(sdcid, keyid1, keyid2, keyid3);
            DataSet sdiAttachmentData = this.getQueryProcessor().getPreparedSqlDataSet("SELECT a.* FROM sdiattachment a, rsetitems r WHERE a.sdcid=r.sdcid AND a.keyid1=r.keyid1 AND a.keyid2=r.keyid2 AND a.keyid3=r.keyid3 AND r.rsetid=?", new Object[]{rsetId}, true);
            String[] attachnumprop = StringUtil.split(attachmentnum, ";");
            String[] attachdescprop = description != null && description.length() > 0 ? StringUtil.split(description, ";") : null;
            String[] oleclassprop = oleclass != null && oleclass.length() > 0 ? StringUtil.split(oleclass, ";") : null;
            String[] fileprop = filename != null && filename.length() > 0 ? StringUtil.split(filename, ";") : null;
            String[] typeflagprop = type != null && type.length() > 0 ? StringUtil.split(type, ";") : null;
            String[] attachmentClass = StringUtil.split(additionalColumns.getProperty("attachmentclass", ""), ";");
            Calendar now = DateTimeUtil.getNowCalendar();
            boolean updateDesc = false;
            boolean updateOle = false;
            boolean updateFilename = false;
            boolean updateTypeflag = false;
            if (keyid1prop.length != attachnumprop.length) {
                throw new ServiceException("INVALID_PROPERTIES", "Incorrect attachmentnum properties.");
            }
            QueryProcessor qp = new QueryProcessor(this.connectionInfo.getConnectionId());
            this.generateTraceLog(sdcid, keyid1, keyid2, keyid3, "Editing Attachment", additionalColumns);
            this.removeInvalidColumns(qp, additionalColumns);
            PropertyListCollection attachmentColDef = null;
            M18NUtil m18NUtil = null;
            try {
                DDTService ddtService = new DDTService(this.sapphireConnection);
                attachmentColDef = ddtService.getSDCProperties("SDIAttachment").getCollection("columns");
            }
            catch (Exception e) {
                this.logger.warn("Failed to obtain attachment column defintion.");
            }
            try {
                m18NUtil = new M18NUtil(new ConnectionInfo(this.sapphireConnection));
            }
            catch (Exception e) {
                this.logger.warn("Failed to obtain m18n.");
            }
            PropertyList attachmentPolicy = this.getConfigurationProcessor().getPolicy("AttachmentPolicy", attachmentpolicynode != null && attachmentpolicynode.length() > 0 ? attachmentpolicynode : "Sapphire Custom");
            for (int sdi = 0; sdi < keyid1prop.length; ++sdi) {
                Attachment currentAttachment = new Attachment();
                currentAttachment.setInputStream(inputStream);
                currentAttachment.setSDCId(sdcid);
                currentAttachment.setKeyId1(keyid1prop[sdi]);
                currentAttachment.setKeyId2(keyid2prop.length == 0 || keyid2prop.length < sdi + 1 || keyid2prop[sdi].length() == 0 ? "(null)" : keyid2prop[sdi]);
                currentAttachment.setKeyId3(keyid3prop.length == 0 || keyid3prop.length < sdi + 1 || keyid3prop[sdi].length() == 0 ? "(null)" : keyid3prop[sdi]);
                currentAttachment.setAttachmentNum(Integer.parseInt(attachnumprop[sdi]));
                currentAttachment.setFilename("(unknown)");
                if (attachmentClass.length > 0 && attachmentClass[sdi].length() > 0) {
                    currentAttachment.setAttachmentClass(attachmentClass[sdi]);
                }
                if (attachdescprop != null && attachdescprop.length > sdi) {
                    currentAttachment.setDescription(attachdescprop[sdi]);
                }
                if (oleclassprop != null && oleclassprop.length > sdi) {
                    currentAttachment.setOleClass(oleclassprop[sdi]);
                }
                if (fileprop != null && fileprop.length > sdi) {
                    currentAttachment.setFilename(fileprop[sdi]);
                }
                Attachment preEditAttachment = Attachment.getAttachment(sdiAttachmentData, currentAttachment.getSDCId(), currentAttachment.getKeyId1(), currentAttachment.getKeyId2(), currentAttachment.getKeyId3(), currentAttachment.getAttachmentNum(), this.getConnectionId());
                currentAttachment.setType("R");
                if (typeflagprop != null && typeflagprop.length > sdi && typeflagprop[sdi].length() >= 1) {
                    this.logger.debug("typeflag provided.");
                    currentAttachment.setType(typeflagprop[sdi].length() > 1 ? typeflagprop[sdi].substring(0, 1).toUpperCase() : typeflagprop[sdi]);
                } else {
                    this.logger.debug("typeflag not provided.");
                    if (preEditAttachment.getType().length() > 0) {
                        this.logger.debug("original typeflag used.");
                        currentAttachment.setType(preEditAttachment.getType().toString());
                    }
                }
                this.logger.debug("typeflag = " + currentAttachment.getType());
                if (additionalColumns != null && additionalColumns.size() > 0) {
                    Iterator it = additionalColumns.keySet().iterator();
                    while (it.hasNext()) {
                        String curkey = it.next().toString();
                        String value = additionalColumns.getProperty(curkey);
                        if (value.indexOf(";") > -1) {
                            String[] vals = value.split(";");
                            value = vals.length > sdi ? vals[sdi] : vals[0];
                        }
                        value = StringUtil.replaceAll(value, "#semicolon#", ";");
                        if (curkey.equalsIgnoreCase("attachmentclass")) {
                            currentAttachment.setAttachmentClass(value);
                            continue;
                        }
                        if (curkey.equalsIgnoreCase("attachmentclob")) {
                            currentAttachment.setClob(value);
                            continue;
                        }
                        if (curkey.equalsIgnoreCase("thumbnailimage")) {
                            currentAttachment.setThumbnailImage(value);
                            continue;
                        }
                        currentAttachment.setAdditionalColumn(curkey, value);
                    }
                }
                this.editSDIAttachment(currentAttachment, preEditAttachment, index, attachmentpolicynode, attachmentPolicy, attachmentColDef, m18NUtil, db);
            }
        }
        catch (Exception e) {
            throw new ServiceException("DB_UPDATE_FAILED", "Failed to update the attachment rows: " + e.getMessage(), e);
        }
        finally {
            db.reset();
        }
        if (rsetid != null) {
            dam.clearRSet(rsetid);
        }
    }

    public void deleteSDIAttachment(String sdcid, String keyid1, String keyid2, String keyid3, String attachmentnum, boolean applylock) throws ServiceException {
        this.deleteSDIAttachment(sdcid, keyid1, keyid2, keyid3, attachmentnum, null, applylock);
    }

    private DataSet deleteSDIAttachment(Attachment attachment, DataSet delAtt, String attachmentpolicynode, PropertyList attpolicy, DBUtil db, DBUtil db1) throws ServiceException {
        Attachment currentAttachment = attachment;
        DataSet deletedAttachments = delAtt;
        if (db == null) {
            db = new DBUtil(this.sapphireConnection.getConnectionId());
            db.setConnection(this.sapphireConnection);
        }
        if (db1 == null) {
            db1 = new DBUtil(this.sapphireConnection.getConnectionId());
            db1.setConnection(this.sapphireConnection);
        }
        if (attpolicy == null) {
            ConfigurationProcessor cp = new ConfigurationProcessor(this.getConnectionId());
            try {
                attpolicy = cp.getPolicy("AttachmentPolicy", attachmentpolicynode != null && attachmentpolicynode.length() > 0 ? attachmentpolicynode : "Sapphire Custom");
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        try {
            if (currentAttachment.getTriggerBusinessRule()) {
                ErrorHandler errorHandler = new ErrorHandler();
                try {
                    BaseSDCRules sdcPreRules = BaseSDCRules.getInstance(this.sapphireConnection, errorHandler, currentAttachment.getSDCId(), this.getSDCProcessor().getProperties(currentAttachment.getSDCId()), "PreDeleteAttachment");
                    Trace.startBusinessRule(currentAttachment.getSDCId() + "." + "PreDeleteAttachment", true);
                    sdcPreRules.preDeleteSDIAttachment(currentAttachment);
                    Trace.endBusinessRule(currentAttachment.getSDCId() + "." + "PreDeleteAttachment", true);
                    Trace.startBusinessRule(currentAttachment.getSDCId() + "." + "PreDeleteAttachment", false);
                    for (BaseSDCRules customRules : sdcPreRules.getCustomRuleList()) {
                        customRules.preDeleteSDIAttachment(currentAttachment);
                    }
                    Trace.endBusinessRule(currentAttachment.getSDCId() + "." + "PreDeleteAttachment", false);
                    sdcPreRules.endRule();
                    if (errorHandler.size() > 0 && errorHandler.hasErrors()) {
                        throw new ServiceException(errorHandler.getEncodedString());
                    }
                }
                catch (SapphireException e) {
                    if (errorHandler.size() > 0 && errorHandler.hasErrors()) {
                        throw new ServiceException(errorHandler.getEncodedString(), e);
                    }
                    throw new ServiceException("PreDeleteAttachment Business Rule Failed", e);
                }
            }
            PreparedStatement deleteChilds = db1.prepareStatement("DELETE FROM sdiattachment WHERE linksdcid = ? AND linkkeyid1 = ? AND linkkeyid2 = ? AND linkkeyid3 = ? AND linkattachmentnum = ?");
            PreparedStatement delete = db.prepareStatement("DELETE FROM sdiattachment WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND attachmentnum = ?");
            BaseAttachmentRepository attachmentRepositoryPre = currentAttachment.getAttachmentRepository(this.sapphireConnection, attpolicy, true);
            LabVantageClassLoader.executeCode(attachmentRepositoryPre.getClassLoader(), () -> attachmentRepositoryPre.preDeleteSDIAttachment(currentAttachment), false);
            DataSet tempDs = this.getQueryProcessor().getPreparedSqlDataSet("attachment1", "SELECT sdcid, keyid1, keyid2, keyid3, filename, typeflag, attachmentnum, attachmentclob FROM sdiattachment WHERE linksdcid = ? AND linkkeyid1 = ? AND linkkeyid2 = ? AND linkkeyid3 = ? AND linkattachmentnum = ?", new Object[]{currentAttachment.getSDCId(), currentAttachment.getKeyId1(), currentAttachment.getKeyId2(), currentAttachment.getKeyId3(), currentAttachment.getAttachmentNum()}, true);
            if (tempDs != null && tempDs.getRowCount() > 0) {
                if (deletedAttachments != null && deletedAttachments.getRowCount() > 0) {
                    deletedAttachments.copyRow(tempDs, -1, 1);
                } else {
                    deletedAttachments = tempDs;
                }
            }
            deleteChilds.setString(1, currentAttachment.getSDCId());
            deleteChilds.setString(2, currentAttachment.getKeyId1());
            deleteChilds.setString(3, currentAttachment.getKeyId2());
            deleteChilds.setString(4, currentAttachment.getKeyId3());
            deleteChilds.setInt(5, currentAttachment.getAttachmentNum());
            deleteChilds.executeUpdate();
            tempDs = this.getQueryProcessor().getPreparedSqlDataSet("attachment2", "SELECT sdcid, keyid1, keyid2, keyid3, attachmentnum, typeflag, filename, attachmentclob FROM sdiattachment WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND attachmentnum = ?", new Object[]{currentAttachment.getSDCId(), currentAttachment.getKeyId1(), currentAttachment.getKeyId2(), currentAttachment.getKeyId3(), currentAttachment.getAttachmentNum()}, true);
            if (tempDs != null && tempDs.getRowCount() > 0) {
                if (deletedAttachments != null && deletedAttachments.getRowCount() > 0) {
                    deletedAttachments.copyRow(tempDs, -1, 1);
                } else {
                    deletedAttachments = tempDs;
                }
            }
            delete.setString(1, currentAttachment.getSDCId());
            delete.setString(2, currentAttachment.getKeyId1());
            delete.setString(3, currentAttachment.getKeyId2());
            delete.setString(4, currentAttachment.getKeyId3());
            delete.setInt(5, currentAttachment.getAttachmentNum());
            delete.executeUpdate();
            BaseAttachmentRepository attachmentRepositoryPost = currentAttachment.getAttachmentRepository(this.sapphireConnection, attpolicy, true);
            if (attachmentRepositoryPost.isCleanUpRepositoryEnabled()) {
                this.cleanUpRepository(currentAttachment);
            }
            if (currentAttachment.getTriggerBusinessRule()) {
                ErrorHandler errorHandler = new ErrorHandler();
                try {
                    BaseSDCRules sdcPostRules = BaseSDCRules.getInstance(this.sapphireConnection, errorHandler, currentAttachment.getSDCId(), this.getSDCProcessor().getProperties(currentAttachment.getSDCId()), "PostDeleteAttachment");
                    Trace.startBusinessRule(currentAttachment.getSDCId() + "." + "PostDeleteAttachment", true);
                    sdcPostRules.postDeleteSDIAttachment(currentAttachment);
                    Trace.endBusinessRule(currentAttachment.getSDCId() + "." + "PostDeleteAttachment", true);
                    Trace.startBusinessRule(currentAttachment.getSDCId() + "." + "PostDeleteAttachment", false);
                    for (BaseSDCRules customRules : sdcPostRules.getCustomRuleList()) {
                        customRules.postDeleteSDIAttachment(currentAttachment);
                    }
                    Trace.endBusinessRule(currentAttachment.getSDCId() + "." + "PostDeleteAttachment", false);
                    sdcPostRules.endRule();
                    if (errorHandler.size() > 0 && errorHandler.hasErrors()) {
                        throw new ServiceException(errorHandler.getEncodedString());
                    }
                }
                catch (SapphireException e) {
                    if (errorHandler.size() > 0 && errorHandler.hasErrors()) {
                        throw new ServiceException(errorHandler.getEncodedString(), e);
                    }
                    throw new ServiceException("PostDeleteAttachment Business Rule Failed", e);
                }
            }
            this.clearLocalCache(currentAttachment);
        }
        catch (Throwable e) {
            throw new ServiceException(e);
        }
        finally {
            db.closeStatement();
            db1.closeStatement();
        }
        return deletedAttachments;
    }

    public void cleanUpRepository(Attachment currentAttachment) throws ActionException {
        String sdcId = currentAttachment.getSDCId();
        String keyId1 = currentAttachment.getKeyId1();
        String keyId2 = currentAttachment.getKeyId2();
        String keyId3 = currentAttachment.getKeyId3();
        int attachmentnum = currentAttachment.getAttachmentNum();
        String attachmentRepositoryId = currentAttachment.getAttachmentRepositoryId();
        String repositoryNodeid = currentAttachment.getAttachmentRepositoryNodeId();
        String externalRepositoryId = currentAttachment.getRepositoryId();
        String fileName = currentAttachment.getFilename();
        PropertyList props = new PropertyList();
        props.setProperty("actionid", "CleanUpRepository");
        props.setProperty("actionversionid", "1");
        props.setProperty("sdcid", sdcId);
        props.setProperty("keyid1", keyId1);
        props.setProperty("keyid2", keyId2);
        props.setProperty("keyid3", keyId3);
        props.setProperty("attachmentnum", Integer.toString(attachmentnum));
        props.setProperty("attachmentrepositoryid", attachmentRepositoryId);
        props.setProperty("attachmentrepositorynodeid", repositoryNodeid);
        props.setProperty("externalrepository", externalRepositoryId);
        props.setProperty("filename", fileName);
        this.getActionProcessor().processActionClass(AddToDoListEntry.class.getName(), props, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void deleteSDIAttachment(sapphire.attachment.Attachment attachment, boolean applylock, String attachmentpolicynode) throws ServiceException {
        this.logInfo("Deleting SDI attachment (2) for " + new SDI(attachment.getSDCId(), attachment.getKeyId1(), attachment.getKeyId2(), attachment.getKeyId3()).getKeyText());
        DataSet deletedAtts = null;
        if (attachment.getSDCId().length() == 0) {
            throw new ServiceException("INVALID_PROPERTY", "No SDC specified");
        }
        if (attachment.getAttachmentNum() == 0) {
            throw new ServiceException("INVALID_PROPERTY", "Some of the Attachment numbers are incorrect.");
        }
        DAMProcessor dam = this.getDAMProcessor();
        StringHolder rsetidHolder = new StringHolder();
        String rsetid = "";
        if (applylock) {
            if (dam.createLockedRSet(attachment.getSDCId(), attachment.getKeyId1(), attachment.getKeyId2(), attachment.getKeyId3(), rsetidHolder) == 1) {
                rsetid = rsetidHolder.value;
            }
        } else if (dam.createRSet(attachment.getSDCId(), attachment.getKeyId1(), attachment.getKeyId2(), attachment.getKeyId3(), rsetidHolder) == 1) {
            rsetid = rsetidHolder.value;
        }
        if (rsetid.length() == 0) {
            throw new ServiceException("CREATE_RSET_FAILURE", "Failed to create RSET for add attachment");
        }
        Attachment currentAtt = (Attachment)attachment;
        try {
            if (attachmentpolicynode == null || attachmentpolicynode.length() == 0) {
                attachmentpolicynode = "Sapphire Custom";
            } else if (attachmentpolicynode != null && attachmentpolicynode.length() > 0 && attachmentpolicynode.contains(";")) {
                attachmentpolicynode = StringUtil.split(attachmentpolicynode, ";")[0];
            }
            ConfigurationProcessor cp = new ConfigurationProcessor(this.getConnectionId());
            PropertyList attpolicy = null;
            try {
                attpolicy = cp.getPolicy("AttachmentPolicy", attachmentpolicynode);
            }
            catch (Exception exception) {
                // empty catch block
            }
            DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
            DBUtil db1 = new DBUtil(this.sapphireConnection.getConnectionId());
            db.setConnection(this.sapphireConnection);
            db1.setConnection(this.sapphireConnection);
            try {
                deletedAtts = this.deleteSDIAttachment(currentAtt, null, attachmentpolicynode, attpolicy, db, db1);
            }
            finally {
                db1.reset();
                db.reset();
            }
        }
        finally {
            if (rsetid != null) {
                dam.clearRSet(rsetid);
            }
        }
        if (deletedAtts != null && deletedAtts.getRowCount() > 0) {
            if (currentAtt.getAdditionalColumns() != null) {
                this.generateTraceLogForAudit(deletedAtts, "Deleting Attachment", currentAtt.getAdditionalColumns());
            }
            for (int i = 0; i < deletedAtts.size(); ++i) {
                Indexer.removeAttachment(this.connectionInfo, currentAtt.getSDCId(), deletedAtts.getValue(i, "keyid1"), deletedAtts.getValue(i, "keyid2"), deletedAtts.getValue(i, "keyid3"), deletedAtts.getInt(i, "attachmentnum"));
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void deleteSDIAttachment(String sdcid, String keyid1, String keyid2, String keyid3, String attachmentnum, PropertyList additionalColumns, boolean applylock) throws ServiceException {
        DataSet deletedAttachments;
        block38: {
            this.logInfo("Deleting SDI attachment (1) for " + new SDI(sdcid, keyid1, keyid2, keyid3).getKeyText());
            if (sdcid.length() == 0) {
                throw new ServiceException("INVALID_PROPERTY", "No SDC specified");
            }
            deletedAttachments = null;
            DAMProcessor dam = this.getDAMProcessor();
            StringHolder rsetidHolder = new StringHolder();
            String rsetid = "";
            if (applylock) {
                if (dam.createLockedRSet(sdcid, keyid1, keyid2, keyid3, rsetidHolder) == 1) {
                    rsetid = rsetidHolder.value;
                }
            } else if (dam.createRSet(sdcid, keyid1, keyid2, keyid3, rsetidHolder) == 1) {
                rsetid = rsetidHolder.value;
            }
            if (rsetid.length() == 0) {
                throw new ServiceException("CREATE_RSET_FAILURE", "Failed to create RSET for delete attachment");
            }
            try {
                String attachmentpolicynode;
                DataSet sdiAttachmentData = this.getQueryProcessor().getPreparedSqlDataSet("SELECT a.* FROM sdiattachment a, rsetitems r WHERE a.sdcid=r.sdcid AND a.keyid1=r.keyid1 AND a.keyid2=r.keyid2 AND a.keyid3=r.keyid3 AND r.rsetid=?", new Object[]{rsetid}, true);
                String[] keyid1prop = StringUtil.split(keyid1, ";");
                String[] keyid2prop = StringUtil.split(keyid2, ";");
                String[] keyid3prop = StringUtil.split(keyid3, ";");
                String[] attachprop = attachmentnum != null && attachmentnum.length() > 0 ? StringUtil.split(attachmentnum, ";") : null;
                String string = attachmentpolicynode = additionalColumns != null ? additionalColumns.getProperty("attachmentpolicynode") : "";
                if (attachmentpolicynode.length() > 0 && attachmentpolicynode.contains(";")) {
                    attachmentpolicynode = StringUtil.split(attachmentpolicynode, ";")[0];
                }
                DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
                DBUtil db1 = new DBUtil(this.sapphireConnection.getConnectionId());
                try {
                    db.setConnection(this.sapphireConnection);
                    db1.setConnection(this.sapphireConnection);
                    ConfigurationProcessor cp = new ConfigurationProcessor(this.getConnectionId());
                    PropertyList attpolicy = null;
                    try {
                        attpolicy = cp.getPolicy("AttachmentPolicy", attachmentpolicynode != null && attachmentpolicynode.length() > 0 ? attachmentpolicynode : "Sapphire Custom");
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    if (attachprop == null) {
                        Object[] objectArray;
                        this.logInfo("No attachnum specified.");
                        for (int i = 0; i < sdiAttachmentData.getRowCount(); ++i) {
                            Attachment currentAttachment = Attachment.getAttachment(sdiAttachmentData, sdcid, sdiAttachmentData.getValue(i, "keyid1"), sdiAttachmentData.getValue(i, "keyid2"), sdiAttachmentData.getValue(i, "keyid3"), sdiAttachmentData.getInt(i, "attachmentnum"), this.getConnectionId());
                            BaseAttachmentRepository attachmentRepositoryPre = currentAttachment.getAttachmentRepository(this.sapphireConnection, attpolicy);
                            LabVantageClassLoader.executeCode(attachmentRepositoryPre.getClassLoader(), () -> attachmentRepositoryPre.preDeleteSDIAttachment(currentAttachment), false);
                        }
                        QueryProcessor queryProcessor = this.getQueryProcessor();
                        String string2 = "SELECT sdcid, keyid1, keyid2, keyid3, attachmentnum FROM sdiattachment WHERE " + (this.connectionInfo.getDbms().equals("ORA") ? "( linksdcid, linkkeyid1, linkkeyid2, linkkeyid3 ) IN (SELECT sdcid, keyid1, keyid2, keyid3 FROM rsetitems WHERE rsetid=?)" : "( \tlinksdcid IN (SELECT sdcid FROM rsetitems WHERE rsetid=?) AND \tlinkkeyid1 IN (SELECT keyid1 FROM rsetitems WHERE rsetid=?) AND \tlinkkeyid2 IN (SELECT keyid2 FROM rsetitems WHERE rsetid=?) AND \tlinkkeyid3 IN (SELECT keyid3 FROM rsetitems WHERE rsetid=?) )");
                        if (this.connectionInfo.getDbms().equals("ORA")) {
                            Object[] objectArray2 = new Object[1];
                            objectArray = objectArray2;
                            objectArray2[0] = rsetid;
                        } else {
                            Object[] objectArray3 = new Object[4];
                            objectArray3[0] = rsetid;
                            objectArray3[1] = rsetid;
                            objectArray3[2] = rsetid;
                            objectArray = objectArray3;
                            objectArray3[3] = rsetid;
                        }
                        deletedAttachments = queryProcessor.getPreparedSqlDataSet(string2, objectArray);
                        SafeSQL safeSQL = new SafeSQL();
                        String sql = "DELETE FROM sdiattachment WHERE " + (this.connectionInfo.getDbms().equals("ORA") ? "( linksdcid, linkkeyid1, linkkeyid2, linkkeyid3 ) IN (SELECT sdcid, keyid1, keyid2, keyid3 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ")" : "( \tlinksdcid IN (SELECT sdcid FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") AND \tlinkkeyid1 IN (SELECT keyid1 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") AND \tlinkkeyid2 IN (SELECT keyid2 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") AND \tlinkkeyid3 IN (SELECT keyid3 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") )");
                        db1.executePreparedUpdate(sql, safeSQL.getValues());
                        safeSQL.reset();
                        DataSet tempDs = this.getQueryProcessor().getPreparedSqlDataSet("SELECT sdcid, keyid1, keyid2, keyid3, attachmentnum FROM sdiattachment WHERE " + (this.connectionInfo.isOracle() ? "\t( sdcid, keyid1, keyid2, keyid3 ) IN \t( SELECT sdcid, keyid1, keyid2, keyid3 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + ") " : "\t( \t\tsdcid IN (SELECT sdcid FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") AND \t\tkeyid1 IN (SELECT keyid1 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") AND \t\tkeyid2 IN (SELECT keyid2 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") AND \t\tkeyid3 IN (SELECT keyid3 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") \t)"), safeSQL.getValues());
                        if (tempDs != null && tempDs.getRowCount() > 0) {
                            if (deletedAttachments != null && deletedAttachments.getRowCount() > 0) {
                                deletedAttachments.copyRow(tempDs, -1, 1);
                            } else {
                                deletedAttachments = tempDs;
                            }
                        }
                        safeSQL.reset();
                        sql = "DELETE FROM sdiattachment WHERE " + (this.connectionInfo.isOracle() ? "\t( sdcid, keyid1, keyid2, keyid3 ) IN \t( SELECT sdcid, keyid1, keyid2, keyid3 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + ") " : "\t( \t\tsdcid IN (SELECT sdcid FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") AND \t\tkeyid1 IN (SELECT keyid1 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") AND \t\tkeyid2 IN (SELECT keyid2 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") AND \t\tkeyid3 IN (SELECT keyid3 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") \t)");
                        this.logInfo("Processing: " + sql);
                        db.executePreparedUpdate(sql, safeSQL.getValues());
                        try {
                            for (int i = 0; i < sdiAttachmentData.getRowCount(); ++i) {
                                Attachment currentAttachment = Attachment.getAttachment(sdiAttachmentData, sdcid, sdiAttachmentData.getValue(i, "keyid1"), sdiAttachmentData.getValue(i, "keyid2"), sdiAttachmentData.getValue(i, "keyid3"), sdiAttachmentData.getInt(i, "attachmentnum"), this.getConnectionId());
                                BaseAttachmentRepository attachmentRepositoryPost = currentAttachment.getAttachmentRepository(this.sapphireConnection, attpolicy);
                                if (!attachmentRepositoryPost.isCleanUpRepositoryEnabled()) continue;
                                this.cleanUpRepository(currentAttachment);
                            }
                            break block38;
                        }
                        finally {
                            db.closeStatement();
                        }
                    }
                    if (keyid1prop.length == 1) {
                        this.logInfo("One sdi and multiple attachments specified.");
                        String currentKeyid1 = keyid1prop[0];
                        String currentKeyid2 = keyid2prop.length == 0 || keyid2prop[0].length() == 0 ? "(null)" : keyid2prop[0];
                        String currentKeyid3 = keyid3prop.length == 0 || keyid3prop[0].length() == 0 ? "(null)" : keyid3prop[0];
                        for (int sdi = 0; sdi < attachprop.length; ++sdi) {
                            int attachnum = Integer.parseInt(attachprop[sdi]);
                            if (attachnum == 0) {
                                throw new ServiceException("INVALID_PROPERTY", "Some of the Attachment numbers are incorrect.");
                            }
                            Attachment currentAttachment = Attachment.getAttachment(sdiAttachmentData, sdcid, currentKeyid1, currentKeyid2, currentKeyid3, attachnum, this.getConnectionId());
                            this.deleteSDIAttachment(currentAttachment, deletedAttachments, attachmentpolicynode, attpolicy, db, db1);
                        }
                        break block38;
                    }
                    if (attachprop.length == keyid1prop.length) {
                        this.logInfo("Multiple sdis and multiple attachments specified.");
                        PreparedStatement deleteChild = db1.prepareStatement("DELETE FROM sdiattachment WHERE linksdcid = ? AND linkkeyid1 = ? AND linkkeyid2 = ? AND linkkeyid3 = ? AND linkattachmentnum = ?");
                        PreparedStatement delete = db.prepareStatement("DELETE FROM sdiattachment WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND attachmentnum = ?");
                        for (int sdi = 0; sdi < keyid1prop.length; ++sdi) {
                            String currentKeyid1 = keyid1prop[sdi];
                            String currentKeyid2 = keyid2prop.length == 0 || keyid2prop.length <= sdi || keyid2prop[sdi].length() == 0 ? "(null)" : keyid2prop[sdi];
                            String currentKeyid3 = keyid3prop.length == 0 || keyid3prop.length <= sdi || keyid3prop[sdi].length() == 0 ? "(null)" : keyid3prop[sdi];
                            int attachnum = Integer.parseInt(attachprop[sdi]);
                            if (attachnum == 0) {
                                throw new ServiceException("INVALID_PROPERTY", "Some of the Attachment numbers are incorrect.");
                            }
                            Attachment currentAttachment = Attachment.getAttachment(sdiAttachmentData, sdcid, currentKeyid1, currentKeyid2, currentKeyid3, attachnum, this.getConnectionId());
                            this.deleteSDIAttachment(currentAttachment, deletedAttachments, attachmentpolicynode, attpolicy, db, db1);
                        }
                        break block38;
                    }
                    throw new ServiceException("INVALID_PROPERTY", "The number of attachment number parameters does not correspond to the number of SDIs specified.");
                }
                catch (Exception e) {
                    throw new ServiceException("DB_DELETE_FAILED", "Failed to delete attachment records: " + e.getMessage(), e);
                }
                finally {
                    db1.reset();
                    db.reset();
                }
            }
            finally {
                if (rsetid != null) {
                    dam.clearRSet(rsetid);
                }
            }
        }
        if (deletedAttachments != null && deletedAttachments.getRowCount() > 0) {
            if (additionalColumns != null) {
                this.generateTraceLogForAudit(deletedAttachments, "Deleting Attachment", additionalColumns);
            }
            for (int i = 0; i < deletedAttachments.size(); ++i) {
                Indexer.removeAttachment(this.connectionInfo, sdcid, deletedAttachments.getValue(i, "keyid1"), deletedAttachments.getValue(i, "keyid2"), deletedAttachments.getValue(i, "keyid3"), deletedAttachments.getInt(i, "attachmentnum"));
            }
        }
    }

    public Path getSDIAttachmentLocalFile(sapphire.attachment.Attachment att, boolean explode) throws ServiceException {
        Path p;
        Path path = p = att.isEncrypted() ? null : ((Attachment)att).getCachedPath(this.connectionInfo.getDatabaseId(), explode);
        if (p != null && Files.exists(p, new LinkOption[0])) {
            this.logger.debug("Local File Already Exists: '" + p.toString() + "'");
            return p;
        }
        this.logger.debug("Local File Not found. Recreate...");
        this.getSDIAttachment(att, Attachment.ThumbnailGeneration.DISABLED);
        if (att.hasData()) {
            boolean isZip;
            String source = att.getSourceFilename();
            FileType ft = source != null && source.length() > 0 ? FileType.getFileTypeByFileName(source, this.getConnectionid()) : FileType.getFileTypeByName("UNKNOWN", this.getConnectionid());
            boolean bl = isZip = ft.getName().equals("ZIP") || ft.getName().equals("GZ");
            if (isZip && explode) {
                try {
                    p = Files.createTempDirectory("attachmentexplodedcache", new FileAttribute[0]);
                }
                catch (Exception e) {
                    throw new ServiceException("Failed to create exploded directory");
                }
                FileTransferOptions fileTransferOptions = new FileTransferOptions();
                fileTransferOptions.setCloseInputStream(false);
                fileTransferOptions.setCloseOutputStream(true);
                fileTransferOptions.setReplaceTarget(true);
                try {
                    FileTransfer.extractZipStream(att.getInputStream(), p.toFile());
                    CacheUtil.put(this.sapphireConnection.getDatabaseId(), "AttachmentExplodedPath", att.getSDCId() + ";" + att.getKeyId1() + ";" + att.getKeyId2() + ";" + att.getKeyId3() + ";" + att.getAttachmentNum(), p.toString());
                    this.logger.debug("Local Exploded Folder Created: '" + p.toString() + "' (" + (Files.exists(p, new LinkOption[0]) ? "exists" : "does not exist") + ")");
                    return p;
                }
                catch (Exception e) {
                    this.logger.error("Failed to extract zip.");
                    throw new ServiceException(e);
                }
            }
            if (!att.isEncrypted()) {
                p = ((Attachment)att).getLocalFile();
                if (p != null && Files.exists(p, new LinkOption[0])) {
                    this.logger.debug("Path obtained and exists (1)");
                } else if (att.getFilename() != null && att.getFilename().length() > 0) {
                    p = Paths.get(att.getFilename(), new String[0]);
                    if (Files.exists(p, new LinkOption[0])) {
                        this.logger.debug("Path obtained and exists (2)");
                    } else {
                        p = ((Attachment)att).saveLocalFile();
                    }
                } else {
                    p = ((Attachment)att).saveLocalFile();
                }
                this.logger.debug("Local File Created: '" + p.toString() + "' (" + (Files.exists(p, new LinkOption[0]) ? "exists" : "does not exist") + ")");
                return p;
            }
            try {
                Path tp = FileUtil.createTempFile("lvlf", "." + FileUtil.getExtension(att.getFilename()));
                Path rp = ((Attachment)att).saveLocalFile(tp);
                this.logger.debug("Local Decrypted File Created: '" + rp.toString() + "' (" + (Files.exists(rp, new LinkOption[0]) ? "exists" : "does not exist") + ")");
                return rp;
            }
            catch (Exception e) {
                throw new ServiceException("Failed to save encrypted temp file.");
            }
        }
        throw new ServiceException("No data in attachment");
    }

    public Attachment getSDIAttachment(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentnum) throws ServiceException {
        return this.getSDIAttachment(sdcid, keyid1, keyid2, keyid3, attachmentnum, null, false);
    }

    public Attachment getSDIAttachment(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentnum, boolean lockattachment) throws ServiceException {
        return this.getSDIAttachment(sdcid, keyid1, keyid2, keyid3, attachmentnum, null, lockattachment);
    }

    public Attachment getSDIAttachment(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentnum, String thumbnailGeneration) throws ServiceException {
        return this.getSDIAttachment(sdcid, keyid1, keyid2, keyid3, attachmentnum, thumbnailGeneration, false);
    }

    public Attachment getSDIAttachment(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentnum, String thumbnailGeneration, boolean lockattachment) throws ServiceException {
        this.logInfo("Getting SDI attachment by num for " + new SDI(sdcid, keyid1, keyid2, keyid3).getKeyText());
        Attachment attachment = new Attachment();
        attachment.setSDCId(sdcid);
        attachment.setKeyId1(keyid1);
        attachment.setKeyId2(keyid2);
        attachment.setKeyId3(keyid3);
        attachment.setAttachmentNum(attachmentnum);
        attachment.setAttachmentClass("");
        attachment.setLockAttachment(lockattachment);
        try {
            this.getSDIAttachment(attachment, Attachment.ThumbnailGeneration.getThumbnailGeneration(thumbnailGeneration, Attachment.ThumbnailGeneration.DISABLED));
        }
        catch (Exception exc) {
            Trace.logError(LOGNAME, "Failed to get SDI Attachment");
            exc.printStackTrace();
        }
        return attachment;
    }

    public void getSDIAttachment(sapphire.attachment.Attachment att, Attachment.ThumbnailGeneration thumbnailGeneration) throws ServiceException {
        this.getSDIAttachment(att, -1, thumbnailGeneration);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void getSDIAttachment(sapphire.attachment.Attachment att, int auditsequence, Attachment.ThumbnailGeneration thumbnailGeneration) throws ServiceException {
        this.logInfo("Getting SDI (2) attachment for " + new SDI(att.getSDCId(), att.getKeyId1(), att.getKeyId2(), att.getKeyId3()).getKeyText());
        if (att.getSDCId().length() == 0) {
            throw new ServiceException("INVALID_PROPERTY", "No SDC specified");
        }
        boolean byClass = false;
        final Attachment attachment = (Attachment)att;
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            boolean hashResultDone;
            AtomicBoolean canHash2;
            AtomicBoolean storeNewHash;
            db.setConnection(this.sapphireConnection);
            if (auditsequence > -1) {
                if (attachment.getAttachmentNum() <= -1) throw new ServiceException("INVALID_PROPERTY", "No attachment number specified");
                db.createPreparedResultSet("getsdiattachment", "SELECT * FROM a_sdiattachment WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND attachmentnum = ? AND auditsequence = ?", new Object[]{attachment.getSDCId(), attachment.getKeyId1(), attachment.getKeyId2(), attachment.getKeyId3(), new Integer(attachment.getAttachmentNum()), new Integer(auditsequence)});
            } else if (attachment.getAttachmentNum() > -1) {
                db.createPreparedResultSet("getsdiattachment", "SELECT * FROM sdiattachment WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND attachmentnum = ?", new Object[]{attachment.getSDCId(), attachment.getKeyId1(), attachment.getKeyId2(), attachment.getKeyId3(), new Integer(attachment.getAttachmentNum())});
            } else if (attachment.getAttachmentClass() != null && attachment.getAttachmentClass().length() > 0) {
                byClass = true;
                db.createPreparedResultSet("getsdiattachment", "SELECT * FROM sdiattachment WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND attachmentclass = ?", new Object[]{attachment.getSDCId(), attachment.getKeyId1(), attachment.getKeyId2(), attachment.getKeyId3(), attachment.getAttachmentClass()});
            } else {
                if (attachment.getSDIAttachmentId() == null || attachment.getSDIAttachmentId().length() <= 0) throw new ServiceException("INVALID_PROPERTY", "No attachment number or class specified");
                db.createPreparedResultSet("getsdiattachment", "SELECT * FROM sdiattachment WHERE sdcid=? AND sdiattachmentid = ?", new Object[]{attachment.getSDCId(), attachment.getSDIAttachmentId()});
            }
            PropertyList attachmentPolicy = this.getConfigurationProcessor().getPolicy("AttachmentPolicy", "Sapphire Custom");
            DataSet dataSet = new DataSet(this.connectionInfo);
            dataSet.setResultSet(db.getResultSet("getsdiattachment"), true, this.connectionInfo.getDbms(), new DataSet.ResultSetRowProcessor(){

                @Override
                public void processRow(HashMap newrow, ResultSet resultSet, DataSet ds) {
                    try {
                        attachment.setBlob(resultSet.getBlob("attachment"));
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            });
            if (dataSet == null || dataSet.getRowCount() <= 0) throw new SapphireException("Failed to obtain attachment.");
            if (dataSet.isValidColumn("attachment")) {
                dataSet.removeColumn("attachment");
            }
            attachment.setAttachment(dataSet, 0);
            if (attachment.getAttachmentType() == Attachment.AttachmentType.LINKEDREFERENCE) {
                String orgSdcid = attachment.getSDCId();
                String orgKeyid1 = attachment.getKeyId1();
                String orgKeyid2 = attachment.getKeyId2();
                String orgKeyid3 = attachment.getKeyId3();
                int orgAttNum = attachment.getAttachmentNum();
                String linksdcid = attachment.getLinkSdcid();
                String linkk1 = attachment.getLinkKeyId1();
                String linkk2 = attachment.getLinkKeyId2();
                String linkk3 = attachment.getLinkKeyId3();
                int linkan = attachment.getLinkAttachmentNum();
                attachment.setSDCId(linksdcid);
                attachment.setKeyId1(linkk1);
                attachment.setKeyId2(linkk2 != null && linkk2.length() > 0 ? linkk2 : "(null)");
                attachment.setKeyId3(linkk3 != null && linkk3.length() > 0 ? linkk3 : "(null)");
                attachment.setAttachmentNum(linkan);
                this.getSDIAttachment(attachment, auditsequence, thumbnailGeneration);
                attachment.setLinkSDI(linksdcid, linkk1, linkk2, linkk3, linkan);
                attachment.setSDCId(orgSdcid);
                attachment.setKeyId1(orgKeyid1);
                attachment.setKeyId2(orgKeyid2);
                attachment.setKeyId3(orgKeyid3);
                attachment.setAttachmentNum(orgAttNum);
            }
            boolean existingThumbnail = attachment.getThumbnailImage() != null && attachment.getThumbnailImage().length() > 0;
            BaseAttachmentRepository attachmentRepository = attachment.getAttachmentRepository(this.sapphireConnection, attachmentPolicy, true);
            AtomicBoolean allowCache = new AtomicBoolean();
            LabVantageClassLoader.executeCode(attachmentRepository.getClassLoader(), () -> allowCache.set(attachmentRepository.enableCaching() && attachmentPolicy.getProperty("localcaching", "Y").equalsIgnoreCase("Y")), false);
            boolean allowHash = false;
            if (attachment.getTriggerBusinessRule()) {
                ErrorHandler errorHandler = new ErrorHandler();
                try {
                    BaseSDCRules sdcPreRules = BaseSDCRules.getInstance(this.sapphireConnection, errorHandler, attachment.getSDCId(), this.getSDCProcessor().getProperties(attachment.getSDCId()), "PreGetAttachment");
                    Trace.startBusinessRule(attachment.getSDCId() + "." + "PreGetAttachment", true);
                    sdcPreRules.preGetSDIAttachment(attachment);
                    Trace.endBusinessRule(attachment.getSDCId() + "." + "PreGetAttachment", true);
                    Trace.startBusinessRule(attachment.getSDCId() + "." + "PreGetAttachment", false);
                    for (BaseSDCRules customRules : sdcPreRules.getCustomRuleList()) {
                        customRules.preGetSDIAttachment(attachment);
                    }
                    Trace.endBusinessRule(attachment.getSDCId() + "." + "PreGetAttachment", false);
                    sdcPreRules.endRule();
                    if (errorHandler.size() > 0 && errorHandler.hasErrors()) {
                        throw new ServiceException(errorHandler.getEncodedString());
                    }
                }
                catch (SapphireException e) {
                    if (errorHandler.size() <= 0 || !errorHandler.hasErrors()) throw new ServiceException("PreGetAttachment Business Rule Failed", e);
                    throw new ServiceException(errorHandler.getEncodedString(), e);
                }
            }
            Path cachedFilePath = ((Attachment)att).getCachedPath(this.connectionInfo.getDatabaseId(), false);
            if (allowCache.get() && attachment.getAllowLocalCache() && cachedFilePath != null) {
                ((Attachment)att).loadLocalFile(cachedFilePath);
            }
            long newHash = 0L;
            if (attachment != null && attachment.getInputStream() == null) {
                LabVantageClassLoader.executeCode(attachmentRepository.getClassLoader(), () -> attachmentRepository.getSDIAttachment(attachment, thumbnailGeneration), false);
                if (this.canUncompress(attachment)) {
                    attachment.setInputStream(FileTransfer.getUncompressedInputStream(attachment.getInputStream()));
                }
                allowHash = this.canValidateHash(attachment);
                storeNewHash = new AtomicBoolean();
                LabVantageClassLoader.executeCode(attachmentRepository.getClassLoader(), () -> storeNewHash.set(!attachment.isHashed() && this.canGenerateHash(attachment, attachmentRepository, attachmentPolicy)), false);
                canHash2 = new AtomicBoolean();
                LabVantageClassLoader.executeCode(attachmentRepository.getClassLoader(), () -> canHash2.set(attachmentRepository.canHash()), false);
                if (canHash2.get()) {
                    AtomicBoolean checkHash = new AtomicBoolean();
                    LabVantageClassLoader.executeCode(attachmentRepository.getClassLoader(), () -> checkHash.set(!attachmentRepository.checkHash(attachment)), false);
                    if (checkHash.get()) {
                        attachment.setInvalidHash(true);
                        this.logger.warn("Failed to validate CRC hash from repository.");
                    }
                    if (allowCache.get()) {
                        attachment.saveLocalFile(allowCache.get(), this.connectionInfo.getDatabaseId());
                    }
                } else if (allowHash) {
                    attachment.saveLocalFile(allowCache.get(), this.connectionInfo.getDatabaseId(), true, storeNewHash.get());
                } else if (allowCache.get()) {
                    attachment.saveLocalFile(allowCache.get(), this.connectionInfo.getDatabaseId(), false, storeNewHash.get());
                } else if (storeNewHash.get()) {
                    attachment.saveLocalFile(false, this.connectionInfo.getDatabaseId(), false, storeNewHash.get());
                }
                if (attachment.isInvalidHash()) {
                    this.logger.warn("Failed to validate CRC hash.");
                } else if (storeNewHash.get()) {
                    this.logger.info("New hash generated.");
                    newHash = attachment.getDataHash();
                }
            } else {
                allowHash = this.canValidateHash(attachment);
                storeNewHash = new AtomicBoolean();
                LabVantageClassLoader.executeCode(attachmentRepository.getClassLoader(), () -> storeNewHash.set(!attachment.isHashed() && this.canGenerateHash(attachment, attachmentRepository, attachmentPolicy)), false);
                if (!storeNewHash.get()) {
                    canHash2 = new AtomicBoolean();
                    LabVantageClassLoader.executeCode(attachmentRepository.getClassLoader(), () -> canHash2.set(attachmentRepository.canHash()), false);
                    if (canHash2.get()) {
                        if (!attachmentRepository.checkHash(attachment)) {
                            attachment.setInvalidHash(true);
                            this.logger.warn("Failed to validate CRC hash from repository for cached attachment.");
                        }
                    } else if (allowHash) {
                        attachment.checkHash();
                    }
                }
                if (attachment.isInvalidHash() || storeNewHash.get()) {
                    try {
                        attachment.getInputStream().close();
                    }
                    catch (Exception canHash2) {
                        // empty catch block
                    }
                    try {
                        this.logger.warn("Hash check failed for local cached attachment or hashing required. Reverting to raw attachment and trying again.");
                        attachmentRepository.getSDIAttachment(attachment, thumbnailGeneration);
                        if (attachmentRepository.canHash()) {
                            AtomicBoolean checkHash = new AtomicBoolean();
                            LabVantageClassLoader.executeCode(attachmentRepository.getClassLoader(), () -> checkHash.set(!attachmentRepository.checkHash(attachment)), false);
                            if (checkHash.get()) {
                                attachment.setInvalidHash(true);
                                this.logger.warn("Failed to validate CRC hash from repository.");
                            }
                            if (allowCache.get()) {
                                attachment.saveLocalFile(allowCache.get(), this.connectionInfo.getDatabaseId());
                            }
                        } else if (allowHash) {
                            attachment.saveLocalFile(allowCache.get(), this.connectionInfo.getDatabaseId(), true, storeNewHash.get());
                        } else if (allowCache.get()) {
                            attachment.saveLocalFile(allowCache.get(), this.connectionInfo.getDatabaseId(), false, storeNewHash.get());
                        } else if (storeNewHash.get()) {
                            attachment.saveLocalFile(false, this.connectionInfo.getDatabaseId(), false, storeNewHash.get());
                        }
                        if (attachment.isInvalidHash()) {
                            this.logger.warn("Failed to validate CRC hash when reset from source.");
                            newHash = -1L;
                        } else if (storeNewHash.get()) {
                            this.logger.info("New hash generated.");
                            newHash = attachment.getDataHash();
                        }
                    }
                    finally {
                        if (cachedFilePath != null) {
                            try {
                                Files.deleteIfExists(cachedFilePath);
                            }
                            catch (Exception checkHash) {}
                        }
                    }
                }
            }
            if (this.canDecrypt(attachment)) {
                attachment.setInputStream(FileTransfer.getCipherInputStream(attachment.getInputStream(), true));
            }
            boolean lockingDone = false;
            boolean bl = hashResultDone = newHash == 0L;
            if (auditsequence < 0) {
                if (thumbnailGeneration.canGenerate() && (attachment.getThumbnailImage() == null || attachment.getThumbnailImage().length() == 0)) {
                    existingThumbnail = false;
                    attachment.generateThumbnail(this.connectionInfo.getConnectionId());
                    this.logDebug("Thumbnail generated");
                }
                if (thumbnailGeneration.canStore() && !existingThumbnail && attachment.getThumbnailImage() != null && attachment.getThumbnailImage().length() > 0) {
                    this.logDebug("About to update thumbnail (1)...");
                    try {
                        this.logger.info("Generate the tracelog records for thumbnail creation (1).");
                        AuditService audit = new AuditService(this.sapphireConnection);
                        int tracelogid = -1;
                        try {
                            tracelogid = Integer.parseInt(audit.addSDITraceLogEntry(att.getSDCId(), att.getKeyId1(), att.getKeyId2(), att.getKeyId3(), "", "Thumbnails", "N", "", "Generating Thumbnails", false));
                            if (att.getLockAttachment()) {
                                if (!hashResultDone) {
                                    if (!byClass) {
                                        db.executePreparedUpdate("UPDATE sdiattachment SET thumbnailimage=?, tracelogid=?,lockedflag=?, lockedby=?, datahash=? WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND attachmentnum = ?", new Object[]{attachment.getThumbnailImage(), tracelogid, "Y", this.connectionInfo.getSysuserId(), newHash, att.getSDCId(), att.getKeyId1(), att.getKeyId2(), att.getKeyId3(), new Integer(att.getAttachmentNum())});
                                    } else {
                                        db.executePreparedUpdate("UPDATE sdiattachment SET thumbnailimage=?, tracelogid=?,lockedflag=?, lockedby=?, datahash=? WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND attachmentclass = ?", new Object[]{attachment.getThumbnailImage(), tracelogid, "Y", this.connectionInfo.getSysuserId(), newHash, attachment.getSDCId(), attachment.getKeyId1(), attachment.getKeyId2(), attachment.getKeyId3(), attachment.getAttachmentClass()});
                                    }
                                    lockingDone = true;
                                    hashResultDone = true;
                                } else {
                                    if (!byClass) {
                                        db.executePreparedUpdate("UPDATE sdiattachment SET thumbnailimage=?, tracelogid=?,lockedflag=?, lockedby=? WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND attachmentnum = ?", new Object[]{attachment.getThumbnailImage(), tracelogid, "Y", this.connectionInfo.getSysuserId(), att.getSDCId(), att.getKeyId1(), att.getKeyId2(), att.getKeyId3(), new Integer(att.getAttachmentNum())});
                                    } else {
                                        db.executePreparedUpdate("UPDATE sdiattachment SET thumbnailimage=?, tracelogid=?,lockedflag=?, lockedby=? WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND attachmentclass = ?", new Object[]{attachment.getThumbnailImage(), tracelogid, "Y", this.connectionInfo.getSysuserId(), attachment.getSDCId(), attachment.getKeyId1(), attachment.getKeyId2(), attachment.getKeyId3(), attachment.getAttachmentClass()});
                                    }
                                    lockingDone = true;
                                }
                            } else if (!hashResultDone) {
                                if (!byClass) {
                                    db.executePreparedUpdate("UPDATE sdiattachment SET thumbnailimage=?, tracelogid=?, datahash=? WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND attachmentnum = ?", new Object[]{attachment.getThumbnailImage(), tracelogid, newHash, att.getSDCId(), att.getKeyId1(), att.getKeyId2(), att.getKeyId3(), new Integer(att.getAttachmentNum())});
                                } else {
                                    db.executePreparedUpdate("UPDATE sdiattachment SET thumbnailimage=?, tracelogid=?, datahash=? WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND attachmentclass = ?", new Object[]{attachment.getThumbnailImage(), tracelogid, newHash, attachment.getSDCId(), attachment.getKeyId1(), attachment.getKeyId2(), attachment.getKeyId3(), attachment.getAttachmentClass()});
                                }
                                hashResultDone = true;
                            } else if (!byClass) {
                                db.executePreparedUpdate("UPDATE sdiattachment SET thumbnailimage=?, tracelogid=? WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND attachmentnum = ?", new Object[]{attachment.getThumbnailImage(), tracelogid, att.getSDCId(), att.getKeyId1(), att.getKeyId2(), att.getKeyId3(), new Integer(att.getAttachmentNum())});
                            } else {
                                db.executePreparedUpdate("UPDATE sdiattachment SET thumbnailimage=?, tracelogid=? WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND attachmentclass = ?", new Object[]{attachment.getThumbnailImage(), tracelogid, attachment.getSDCId(), attachment.getKeyId1(), attachment.getKeyId2(), attachment.getKeyId3(), attachment.getAttachmentClass()});
                            }
                            this.logDebug("Thumbnail updated (1).");
                        }
                        catch (ServiceException e) {
                            throw new SapphireException("Failed to add audit records (1)", e);
                        }
                    }
                    catch (Exception e) {
                        this.logError("Failed to update thumbnail (1).");
                        this.logError(e.getMessage(), e);
                    }
                }
            }
            if (!lockingDone && att.getLockAttachment()) {
                if (!hashResultDone) {
                    if (!byClass) {
                        db.executePreparedUpdate("UPDATE sdiattachment SET  lockedflag=?, lockedby=?, datahash=? WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND attachmentnum = ?", new Object[]{"Y", this.connectionInfo.getSysuserId(), newHash, att.getSDCId(), att.getKeyId1(), att.getKeyId2(), att.getKeyId3(), new Integer(att.getAttachmentNum())});
                    } else {
                        db.executePreparedUpdate("UPDATE sdiattachment SET  lockedflag=?, lockedby=?, datahash=? WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND attachmentclass = ?", new Object[]{"Y", this.connectionInfo.getSysuserId(), newHash, attachment.getSDCId(), attachment.getKeyId1(), attachment.getKeyId2(), attachment.getKeyId3(), attachment.getAttachmentClass()});
                    }
                    hashResultDone = true;
                } else if (!byClass) {
                    db.executePreparedUpdate("UPDATE sdiattachment SET  lockedflag=?, lockedby=? WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND attachmentnum = ?", new Object[]{"Y", this.connectionInfo.getSysuserId(), att.getSDCId(), att.getKeyId1(), att.getKeyId2(), att.getKeyId3(), new Integer(att.getAttachmentNum())});
                } else {
                    db.executePreparedUpdate("UPDATE sdiattachment SET  lockedflag=?, lockedby=? WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND attachmentclass = ?", new Object[]{"Y", this.connectionInfo.getSysuserId(), attachment.getSDCId(), attachment.getKeyId1(), attachment.getKeyId2(), attachment.getKeyId3(), attachment.getAttachmentClass()});
                }
                this.logDebug("Locked the Attachment.");
            }
            if (hashResultDone) return;
            if (!byClass) {
                db.executePreparedUpdate("UPDATE sdiattachment SET datahash=? WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND attachmentnum = ?", new Object[]{newHash, att.getSDCId(), att.getKeyId1(), att.getKeyId2(), att.getKeyId3(), new Integer(att.getAttachmentNum())});
                return;
            } else {
                db.executePreparedUpdate("UPDATE sdiattachment SET datahash=? WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND attachmentclass = ?", new Object[]{newHash, attachment.getSDCId(), attachment.getKeyId1(), attachment.getKeyId2(), attachment.getKeyId3(), attachment.getAttachmentClass()});
            }
            return;
        }
        catch (SapphireException e) {
            boolean fromAttachmentService = false;
            StackTraceElement[] stackTraceElements = e.getStackTrace();
            for (int i = 0; i < stackTraceElements.length; ++i) {
                StackTraceElement element = stackTraceElements[i];
                if (element.toString().indexOf(LOGNAME) <= -1) continue;
                fromAttachmentService = true;
                break;
            }
            if (!e.getMessage().contains(" does not exsit. ") || !fromAttachmentService) throw new ServiceException("DB_ACTION_FAILED", "Failed to get attachment", e);
            Trace.logError(LOGNAME, "Attachment file does not exist." + e.getMessage());
            att = null;
            return;
        }
        finally {
            db.reset();
        }
    }

    public Attachment getTempAttachment(String sdcid, String keyid1, String keyid2, String keyid3, String tempid) throws ServiceException {
        return this.getTempAttachment(sdcid, keyid1, keyid2, keyid3, tempid, null);
    }

    public Attachment getTempAttachment(String sdcid, String keyid1, String keyid2, String keyid3, String tempid, String thumbnailGeneration) throws ServiceException {
        this.logInfo("Getting temp attachment (1) for " + new SDI(sdcid, keyid1, keyid2, keyid3).getKeyText());
        Attachment attachment = new Attachment();
        attachment.setSDCId(sdcid);
        attachment.setKeyId1(keyid1);
        attachment.setKeyId2(keyid2);
        attachment.setKeyId3(keyid3);
        attachment.setTempId(tempid);
        this.getTempAttachment(attachment, Attachment.ThumbnailGeneration.getThumbnailGeneration(thumbnailGeneration));
        return attachment;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void getTempAttachment(sapphire.attachment.Attachment att, Attachment.ThumbnailGeneration thumbnailGeneration) throws ServiceException {
        this.logInfo("Getting temp attachment (2) for " + new SDI(att.getSDCId(), att.getKeyId1(), att.getKeyId2(), att.getKeyId3()).getKeyText());
        Attachment attachment = (Attachment)att;
        if (attachment.getTempId() == null || attachment.getTempId().length() == 0) {
            throw new ServiceException("No temp id provided");
        }
        FileManager.TempFile tempdata = FileManager.TempFile.getTempFile(attachment.getSDCId(), attachment.getKeyId1(), attachment.getKeyId2(), attachment.getKeyId3(), attachment.getTempId(), true, this.getQueryProcessor(), this.getConnectionId());
        if (tempdata != null) {
            attachment.setBlob(null);
            attachment.setOleClass("");
            attachment.setFilename(tempdata.getFileName());
            attachment.setUrl("");
            attachment.setClob("");
            attachment.setType(tempdata.getTypeFlag());
            attachment.setDescription("");
            attachment.setSourceFilename(tempdata.getSourceFileName());
            attachment.setThumbnailImage(tempdata.getThumbnail());
            if (tempdata.getData() != null && tempdata.getData().getData() != null) {
                attachment.setData(tempdata.getData().getData());
            } else if (tempdata.getFileName().length() > 0) {
                File file = new File(tempdata.getFileName());
                if (file.exists()) {
                    try (FileInputStream fileInputStream = new FileInputStream(file);){
                        byte[] data = FileManager.getBinaryData(fileInputStream, -1);
                        attachment.setData(data);
                    }
                    catch (Exception e) {
                        this.logger.error("Could not read file.", e);
                    }
                } else {
                    this.logger.error("File " + tempdata.getFileName() + " does not exist.");
                }
            } else {
                this.logger.error("Temp attachment does not contain file.");
            }
        } else {
            throw new ServiceException("DB_ACTION_FAILED", "Failed to get temp attachment");
        }
    }

    public Attachment getSDIAttachment(String sdcid, String keyid1, String keyid2, String keyid3, String attachmentclass) throws ServiceException {
        return this.getSDIAttachment(sdcid, keyid1, keyid2, keyid3, attachmentclass, null);
    }

    public Attachment getSDIAttachment(String sdcid, String keyid1, String keyid2, String keyid3, String attachmentclass, String thumbnailGeneration) throws ServiceException {
        Attachment attachment = new Attachment();
        attachment.setSDCId(sdcid);
        attachment.setKeyId1(keyid1);
        attachment.setKeyId2(keyid2);
        attachment.setKeyId3(keyid3);
        attachment.setAttachmentNum(-1);
        attachment.setAttachmentClass(attachmentclass);
        this.getSDIAttachment(attachment, Attachment.ThumbnailGeneration.getThumbnailGeneration(thumbnailGeneration, Attachment.ThumbnailGeneration.DISABLED));
        return attachment;
    }

    public Attachment getSDIAttachment(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentnum, int auditsequence) throws ServiceException {
        return this.getSDIAttachment(sdcid, keyid1, keyid2, keyid3, attachmentnum, auditsequence, null);
    }

    public Attachment getSDIAttachment(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentnum, int auditsequence, String thumbnailGeneration) throws ServiceException {
        this.logInfo("Getting SDI attachment by audit for " + new SDI(sdcid, keyid1, keyid2, keyid3).getKeyText());
        Attachment attachment = new Attachment();
        attachment.setSDCId(sdcid);
        attachment.setKeyId1(keyid1);
        attachment.setKeyId2(keyid2);
        attachment.setKeyId3(keyid3);
        attachment.setAttachmentNum(attachmentnum);
        attachment.setAttachmentClass("");
        this.getSDIAttachment(attachment, auditsequence, Attachment.ThumbnailGeneration.getThumbnailGeneration(thumbnailGeneration, Attachment.ThumbnailGeneration.DISABLED));
        return attachment;
    }

    public void copySDIAttachment(PropertyList fromsdcprops, PropertyList tosdcprops) throws ServiceException {
        StringBuffer maxAttachmentNumSql = new StringBuffer("SELECT MAX(attachmentnum) AS maxattachmentnum FROM sdiattachment");
        maxAttachmentNumSql.append(" WHERE sdcid=").append("?").append("");
        maxAttachmentNumSql.append(" AND keyid1=").append("?").append("");
        maxAttachmentNumSql.append(" AND keyid2=").append("?").append("");
        maxAttachmentNumSql.append(" AND keyid3=").append("?").append("");
        DataSet maxAttachmentNumSqlDs = this.getQueryProcessor().getPreparedSqlDataSet(maxAttachmentNumSql.toString(), new Object[]{tosdcprops.getProperty("sdcid"), tosdcprops.getProperty("keyid1"), tosdcprops.getProperty("keyid2", "(null)"), tosdcprops.getProperty("keyid3", "(null)")});
        String maxAttachmentNum = maxAttachmentNumSqlDs.getRowCount() > 0 ? maxAttachmentNumSqlDs.getValue(0, "maxattachmentnum", "0") : "0";
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        db.setConnection(this.sapphireConnection);
        try {
            String[] attachmentnumArray = fromsdcprops.getProperty("attachmentnum", "").split(";");
            String[] attachmentclassArray = fromsdcprops.getProperty("attachmentclass", "").split(";");
            String[] copymodeflagArray = fromsdcprops.getProperty("copymodeflag", "N").split(";");
            if (attachmentnumArray[0].length() > 0) {
                StringBuffer linkedNum = new StringBuffer();
                StringBuffer editableNum = new StringBuffer();
                StringBuffer noneditableNum = new StringBuffer();
                StringBuffer sameAsSourceNum = new StringBuffer();
                for (int i = 0; i < attachmentnumArray.length; ++i) {
                    int index;
                    int n = index = copymodeflagArray.length == attachmentnumArray.length ? i : 0;
                    if (copymodeflagArray[index].equals("L")) {
                        linkedNum.append(",'").append(attachmentnumArray[i]).append("'");
                        continue;
                    }
                    if (copymodeflagArray[index].equals("E")) {
                        editableNum.append(",'").append(attachmentnumArray[i]).append("'");
                        continue;
                    }
                    if (copymodeflagArray[index].equals("F")) {
                        noneditableNum.append(",'").append(attachmentnumArray[i]).append("'");
                        continue;
                    }
                    if (!copymodeflagArray[index].equals("S")) continue;
                    sameAsSourceNum.append(",'").append(attachmentnumArray[i]).append("'");
                }
                if (linkedNum.length() > 1) {
                    SafeSQL safeSQL = new SafeSQL();
                    String copySql = this.getCopySqlLinked(fromsdcprops, tosdcprops, linkedNum.substring(1), "", maxAttachmentNum, safeSQL);
                    db.executePreparedUpdate(copySql, safeSQL.getValues());
                }
                if (editableNum.length() > 1) {
                    SafeSQL safeSQL = new SafeSQL();
                    String copySql = this.getCopySql(fromsdcprops, tosdcprops, "E", editableNum.substring(1), "", maxAttachmentNum, safeSQL);
                    db.executePreparedUpdate(copySql, safeSQL.getValues());
                }
                if (noneditableNum.length() > 1) {
                    SafeSQL safeSQL = new SafeSQL();
                    String copySql = this.getCopySql(fromsdcprops, tosdcprops, "F", noneditableNum.substring(1), "", maxAttachmentNum, safeSQL);
                    db.executePreparedUpdate(copySql, safeSQL.getValues());
                }
                if (sameAsSourceNum.length() > 1) {
                    SafeSQL safeSQL = new SafeSQL();
                    String copySql = this.getCopySql(fromsdcprops, tosdcprops, "S", sameAsSourceNum.substring(1), "", maxAttachmentNum, safeSQL);
                    db.executePreparedUpdate(copySql, safeSQL.getValues());
                }
            } else if (attachmentclassArray.length > 0 && attachmentclassArray[0].length() > 0) {
                StringBuffer linkedClass = new StringBuffer();
                StringBuffer editableClass = new StringBuffer();
                StringBuffer noneditableClass = new StringBuffer();
                StringBuffer sameAsSourceClass = new StringBuffer();
                for (int i = 0; i < attachmentclassArray.length; ++i) {
                    int index;
                    int n = index = copymodeflagArray.length == attachmentclassArray.length ? i : 0;
                    if (copymodeflagArray[index].equals("L")) {
                        linkedClass.append(",'").append(attachmentclassArray[i]).append("'");
                        continue;
                    }
                    if (copymodeflagArray[index].equals("E")) {
                        editableClass.append(",'").append(attachmentclassArray[i]).append("'");
                        continue;
                    }
                    if (copymodeflagArray[index].equals("F")) {
                        noneditableClass.append(",'").append(attachmentclassArray[i]).append("'");
                        continue;
                    }
                    if (!copymodeflagArray[index].equals("S")) continue;
                    sameAsSourceClass.append(",'").append(attachmentclassArray[i]).append("'");
                }
                if (linkedClass.length() > 1) {
                    SafeSQL safeSQL = new SafeSQL();
                    String copySql = this.getCopySqlLinked(fromsdcprops, tosdcprops, "", linkedClass.substring(1), maxAttachmentNum, safeSQL);
                    db.executePreparedUpdate(copySql, safeSQL.getValues());
                }
                if (editableClass.length() > 1) {
                    SafeSQL safeSQL = new SafeSQL();
                    String copySql = this.getCopySql(fromsdcprops, tosdcprops, "E", "", editableClass.substring(1), maxAttachmentNum, safeSQL);
                    db.executePreparedUpdate(copySql, safeSQL.getValues());
                }
                if (noneditableClass.length() > 1) {
                    SafeSQL safeSQL = new SafeSQL();
                    String copySql = this.getCopySql(fromsdcprops, tosdcprops, "F", "", noneditableClass.substring(1), maxAttachmentNum, safeSQL);
                    db.executePreparedUpdate(copySql, safeSQL.getValues());
                }
                if (sameAsSourceClass.length() > 1) {
                    SafeSQL safeSQL = new SafeSQL();
                    String copySql = this.getCopySql(fromsdcprops, tosdcprops, "S", "", sameAsSourceClass.substring(1), maxAttachmentNum, safeSQL);
                    db.executePreparedUpdate(copySql, safeSQL.getValues());
                }
            } else if (copymodeflagArray.length == 1 && !copymodeflagArray[0].equals("N")) {
                if (copymodeflagArray[0].equals("L")) {
                    SafeSQL safeSQL = new SafeSQL();
                    String copySql = this.getCopySqlLinked(fromsdcprops, tosdcprops, "", "", maxAttachmentNum, safeSQL);
                    db.executePreparedUpdate(copySql, safeSQL.getValues());
                } else {
                    if (copymodeflagArray[0].equals("E")) {
                        DataSet dsLinkedAttachments = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM sdiattachment WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND typeflag = 'D'", (Object[])new String[]{fromsdcprops.getProperty("sdcid"), fromsdcprops.getProperty("keyid1"), fromsdcprops.getProperty("keyid2", "(null)"), fromsdcprops.getProperty("keyid3", "(null)")});
                        for (int d = 0; d < dsLinkedAttachments.getRowCount(); ++d) {
                            String linkSDCID = dsLinkedAttachments.getValue(d, "linksdcid");
                            String linkKeyid1 = dsLinkedAttachments.getValue(d, "linkkeyid1");
                            String linkKeyid2 = dsLinkedAttachments.getValue(d, "linkkeyid2");
                            String linkKeyid3 = dsLinkedAttachments.getValue(d, "linkkeyid3");
                            String linkAttachmentNum = dsLinkedAttachments.getValue(d, "linkattachmentnum");
                            String attachmentNum = dsLinkedAttachments.getValue(d, "attachmentnum");
                            String currentAttachmentNumber = Integer.toString(Integer.parseInt(attachmentNum) + Integer.parseInt(maxAttachmentNum));
                            SafeSQL safeSQL = new SafeSQL();
                            PropertyList linkedsdcprops = new PropertyList();
                            linkedsdcprops.setProperty("sdcid", linkSDCID);
                            linkedsdcprops.setProperty("keyid1", linkKeyid1);
                            linkedsdcprops.setProperty("keyid2", linkKeyid2);
                            linkedsdcprops.setProperty("keyid3", linkKeyid3);
                            String copySql = this.getCopySql(linkedsdcprops, tosdcprops, copymodeflagArray[0], linkAttachmentNum, "", maxAttachmentNum, safeSQL, currentAttachmentNumber);
                            db.executePreparedUpdate(copySql, safeSQL.getValues());
                        }
                    }
                    SafeSQL safeSQL = new SafeSQL();
                    String copySql = this.getCopySql(fromsdcprops, tosdcprops, copymodeflagArray[0], "", "", maxAttachmentNum, safeSQL);
                    if (copymodeflagArray[0].equals("E")) {
                        copySql = copySql + " AND sdiattachment.typeflag != 'D'";
                    }
                    db.executePreparedUpdate(copySql, safeSQL.getValues());
                }
            }
        }
        catch (SapphireException e) {
            throw new ServiceException("DB_ACTION_FAILED", "Failed to copy attachment records: " + e.getMessage(), e);
        }
        finally {
            db.reset();
        }
    }

    private String getCopySql(PropertyList fromsdcprops, PropertyList tosdcprops, String copymodeflag, String attachmentnum, String attachmentclass, String maxAttachmentNum, SafeSQL safeSQL) {
        return this.getCopySql(fromsdcprops, tosdcprops, copymodeflag, attachmentnum, attachmentclass, maxAttachmentNum, safeSQL, "");
    }

    private String getCopySql(PropertyList fromsdcprops, PropertyList tosdcprops, String copymodeflag, String attachmentnum, String attachmentclass, String maxAttachmentNum, SafeSQL safeSQL, String currentAttachmentNum) {
        StringBuffer insertSql = new StringBuffer("INSERT INTO sdiattachment");
        insertSql.append(" (sdcid, keyid1, keyid2, keyid3, attachmentnum, attachmentdesc, filename, oleclass, attachment, typeflag, sourcefilename, attachmentuse, attachmentlabel,");
        insertSql.append(" activeflag, linksdcid, linkkeyid1, linkkeyid2, linkkeyid3, linkattachmentnum, attachmentclob, attachmentclass, url, ");
        insertSql.append(" compressedflag, encryptedflag, datahash, attachmentsize, externalrepository, attachmentrepositorynodeid, attachmentrepositoryid, ");
        insertSql.append(" editableflag, contentrevision )");
        insertSql.append(" SELECT ");
        insertSql.append(" ").append(safeSQL.addVar(tosdcprops.getProperty("sdcid"))).append(",");
        insertSql.append(" ").append(safeSQL.addVar(tosdcprops.getProperty("keyid1"))).append(",");
        insertSql.append(" ").append(safeSQL.addVar(tosdcprops.getProperty("keyid2", "(null)"))).append(",");
        insertSql.append(" ").append(safeSQL.addVar(tosdcprops.getProperty("keyid3", "(null)"))).append(",");
        if (currentAttachmentNum == null || currentAttachmentNum.length() == 0) {
            insertSql.append(" attachmentnum + ").append(maxAttachmentNum).append(",");
        } else {
            insertSql.append(currentAttachmentNum).append(",");
        }
        insertSql.append(" attachmentdesc, filename, oleclass, attachment, typeflag, sourcefilename, attachmentuse, attachmentlabel,");
        insertSql.append(" activeflag, linksdcid, linkkeyid1, linkkeyid2, linkkeyid3, linkattachmentnum, attachmentclob, attachmentclass, url,");
        insertSql.append(" compressedflag, encryptedflag, datahash, attachmentsize, externalrepository, attachmentrepositorynodeid, attachmentrepositoryid,");
        if ("S".equalsIgnoreCase(copymodeflag)) {
            insertSql.append("editableflag, 1");
        } else {
            insertSql.append(" (CASE WHEN '" + copymodeflag + "'='E' AND typeflag != 'D' THEN 'Y' ELSE 'N' END) editableflag, 1");
        }
        insertSql.append(" FROM sdiattachment");
        insertSql.append(" WHERE sdcid=").append(safeSQL.addVar(fromsdcprops.getProperty("sdcid")));
        insertSql.append(" AND keyid1=").append(safeSQL.addVar(fromsdcprops.getProperty("keyid1")));
        insertSql.append(" AND keyid2=").append(safeSQL.addVar(fromsdcprops.getProperty("keyid2", "(null)")));
        insertSql.append(" AND keyid3=").append(safeSQL.addVar(fromsdcprops.getProperty("keyid3", "(null)")));
        if (attachmentnum.length() > 0) {
            insertSql.append(" AND ( attachmentnum IN (").append(safeSQL.addIn(attachmentnum)).append(")");
            if (attachmentclass.length() > 0) {
                insertSql.append(" OR ( attachmentclass IN (").append(safeSQL.addIn(attachmentclass)).append(")");
            }
            insertSql.append(")");
        } else if (attachmentclass.length() > 0) {
            insertSql.append(" AND attachmentclass IN (").append(safeSQL.addIn(attachmentclass)).append(")");
        }
        return insertSql.toString();
    }

    private String getCopySqlLinked(PropertyList fromsdcprops, PropertyList tosdcprops, String attachmentnum, String attachmentclass, String maxAttachmentNum, SafeSQL safeSQL) {
        StringBuffer insertLinkedReferenceSql = new StringBuffer("INSERT INTO sdiattachment");
        insertLinkedReferenceSql.append(" (sdcid, keyid1, keyid2, keyid3, attachmentnum, attachmentdesc, filename, oleclass, typeflag, sourcefilename, attachmentuse, attachmentlabel,");
        insertLinkedReferenceSql.append(" activeflag, linksdcid, linkkeyid1, linkkeyid2, linkkeyid3, linkattachmentnum, attachmentclass, editableflag, contentrevision)");
        insertLinkedReferenceSql.append(" SELECT ");
        insertLinkedReferenceSql.append(" ").append(safeSQL.addVar(tosdcprops.getProperty("sdcid"))).append(",");
        insertLinkedReferenceSql.append(" ").append(safeSQL.addVar(tosdcprops.getProperty("keyid1"))).append(",");
        insertLinkedReferenceSql.append(" ").append(safeSQL.addVar(tosdcprops.getProperty("keyid2", "(null)"))).append(",");
        insertLinkedReferenceSql.append(" ").append(safeSQL.addVar(tosdcprops.getProperty("keyid3", "(null)"))).append(",");
        insertLinkedReferenceSql.append(" attachmentnum + ").append(maxAttachmentNum).append(",");
        insertLinkedReferenceSql.append(" attachmentdesc, filename, oleclass, 'D', sourcefilename, attachmentuse, attachmentlabel,");
        insertLinkedReferenceSql.append(" activeflag,");
        insertLinkedReferenceSql.append(" sdcid,");
        insertLinkedReferenceSql.append(" keyid1,");
        insertLinkedReferenceSql.append(" keyid2,");
        insertLinkedReferenceSql.append(" keyid3,");
        insertLinkedReferenceSql.append(" attachmentnum,");
        insertLinkedReferenceSql.append(" attachmentclass, 'N', 1");
        insertLinkedReferenceSql.append(" FROM sdiattachment");
        insertLinkedReferenceSql.append(" WHERE sdcid=").append(safeSQL.addVar(fromsdcprops.getProperty("sdcid")));
        insertLinkedReferenceSql.append(" AND keyid1=").append(safeSQL.addVar(fromsdcprops.getProperty("keyid1")));
        insertLinkedReferenceSql.append(" AND keyid2=").append(safeSQL.addVar(fromsdcprops.getProperty("keyid2", "(null)")));
        insertLinkedReferenceSql.append(" AND keyid3=").append(safeSQL.addVar(fromsdcprops.getProperty("keyid3", "(null)")));
        if (attachmentnum.length() > 0) {
            insertLinkedReferenceSql.append(" AND ( attachmentnum IN (").append(safeSQL.addIn(attachmentnum)).append(")");
            if (attachmentclass.length() > 0) {
                insertLinkedReferenceSql.append(" OR ( attachmentclass IN (").append(safeSQL.addIn(attachmentclass)).append(")");
            }
            insertLinkedReferenceSql.append(")");
        } else if (attachmentclass.length() > 0) {
            insertLinkedReferenceSql.append(" AND attachmentclass IN (").append(safeSQL.addIn(attachmentclass)).append(")");
        }
        return insertLinkedReferenceSql.toString();
    }

    private String getPropertyVal(int keyIdCount, String propertyId, PropertyList additionalProps) {
        String value = additionalProps.getProperty(propertyId);
        if (value.indexOf(";") > -1) {
            String[] vals = value.split(";");
            value = vals.length > keyIdCount ? vals[keyIdCount] : vals[0];
        }
        return value;
    }

    private void validateClassAgainstType(String attachmentClassString, String attachmentType, String attachmentpolicynode) throws ServiceException {
        try {
            PropertyListCollection allowedAttachmentTypes;
            int noOfAllowedTypes;
            Attachment.AttachmentType type = Attachment.AttachmentType.getTypeByAttachmentTypeFlag(attachmentType);
            PropertyList attachmentPolicy = this.getConfigurationProcessor().getPolicy("AttachmentPolicy", attachmentpolicynode != null && attachmentpolicynode.length() > 0 ? attachmentpolicynode : "Sapphire Custom");
            PropertyListCollection classes = attachmentPolicy.getCollection("classes");
            PropertyList attachmentClass = null;
            if (classes != null) {
                attachmentClass = classes.find("class", attachmentClassString);
            }
            if (attachmentClass != null && (noOfAllowedTypes = (allowedAttachmentTypes = attachmentClass.getCollection("allowedtypes")).size()) > 0) {
                boolean found = false;
                StringBuffer allowedTypeStringBuffer = new StringBuffer();
                for (int i = 0; i < noOfAllowedTypes; ++i) {
                    String allowedTypeString = allowedAttachmentTypes.getPropertyList(i).getProperty("attachmenttype");
                    allowedTypeStringBuffer.append(",").append(allowedTypeString);
                    Attachment.AttachmentType allowed = Attachment.AttachmentType.getTypeByAttachmentTypeFlag(Attachment.correctTypeFlag(allowedTypeString));
                    if (allowed != type) continue;
                    found = true;
                    break;
                }
                if (!found) {
                    throw new ServiceException("AttachmentType for class '" + attachmentClassString + "' can only be of type '" + allowedTypeStringBuffer.substring(1) + "' as per AttachmentPolicy");
                }
            }
        }
        catch (SapphireException e) {
            this.logger.error("Failed to load AttachmentPolicy");
        }
    }

    private void generateTraceLogForAudit(DataSet deletedAttachments, String auditDescription, PropertyList additionalColumns) {
        PropertyList sdc = this.getSDCProcessor().getPropertyList("SDIAttachment");
        String auditflag = sdc.getProperty("auditedflag");
        String promptflag = sdc.getProperty("auditpromptflag");
        if (additionalColumns.getProperty("tracelogid", "").trim().length() == 0 && !auditflag.equalsIgnoreCase("N")) {
            String reason = additionalColumns.getProperty("auditreason", "");
            String activity = additionalColumns.getProperty("auditactivity", "");
            String signedflag = additionalColumns.getProperty("auditsignedflag", "");
            String auditdt = additionalColumns.getProperty("auditdt", "");
            if (reason.length() > 0) {
                this.logger.info("Generate the tracelog records");
                String standard = "Y";
                if (!promptflag.equalsIgnoreCase("R") && !promptflag.equalsIgnoreCase("S")) {
                    standard = "N";
                }
                AuditService audit = new AuditService(this.sapphireConnection);
                try {
                    deletedAttachments.sort("sdcid");
                    ArrayList<DataSet> groupedDeletedAttachmentDs = deletedAttachments.getGroupedDataSets("sdcid");
                    for (DataSet dataSet : groupedDeletedAttachmentDs) {
                        String sdcid = dataSet.getValue(0, "sdcid", "");
                        String keyid1 = dataSet.getColumnValues("keyid1", ";");
                        String keyid2 = dataSet.getColumnValues("keyid2", ";");
                        String keyid3 = dataSet.getColumnValues("keyid3", ";");
                        String traceLogId = audit.addSDITraceLogEntry(sdcid, keyid1, keyid2, keyid3, reason, activity, signedflag, auditdt, auditDescription, standard.equals("Y"));
                        this.updateAuditTable(sdcid, keyid1, keyid2, keyid3, dataSet.getColumnValues("attachmentnum", ";"), traceLogId);
                    }
                }
                catch (ServiceException e) {
                    this.logger.error("Failed to add audit records", e);
                }
            }
        }
    }

    private void updateAuditTable(String sdcid, String keyid1list, String keyid2list, String keyid3list, String attachmentnum, String traceLogId) {
        int keycolcount = 1;
        String[] keyid1prop = StringUtil.split(keyid1list, ";");
        String[] keyid2prop = StringUtil.split(keyid2list, ";");
        String[] keyid3prop = StringUtil.split(keyid3list, ";");
        String[] traceLogIds = StringUtil.split(traceLogId, ";");
        String[] attachmentNums = StringUtil.split(attachmentnum, ";");
        String updateSQL = "UPDATE a_sdiattachment SET tracelogid = ?, modtool = ?  WHERE keyid1 = ? AND tracelogid = 'DELETED'";
        if (keyid2list != null && keyid2list.length() > 0) {
            keycolcount = 2;
            updateSQL = updateSQL + " AND keyid2 = ?";
        }
        if (keyid3list != null && keyid3list.length() > 0) {
            keycolcount = 3;
            updateSQL = updateSQL + " AND keyid3 = ?";
        }
        updateSQL = updateSQL + " AND sdcid = ? AND attachmentnum = ?";
        try {
            DBUtil database = new DBUtil(this.sapphireConnection.getConnectionId());
            database.setConnection(this.sapphireConnection);
            this.logger.info("Updating audit records using: " + updateSQL);
            PreparedStatement updateAuditPS = database.prepareStatement("updateaudit", updateSQL);
            for (int i = 0; i < keyid1prop.length; ++i) {
                updateAuditPS.setString(1, traceLogIds.length > 0 ? traceLogIds[i] : traceLogId);
                updateAuditPS.setString(2, "DeleteSDIattachment");
                updateAuditPS.setString(3, keyid1prop[i]);
                if (keycolcount > 1) {
                    updateAuditPS.setString(4, keyid2prop[i]);
                }
                if (keycolcount > 2) {
                    updateAuditPS.setString(5, keyid3prop[i]);
                }
                updateAuditPS.setString(6, sdcid);
                updateAuditPS.setString(7, attachmentNums[i]);
                try {
                    int rows = updateAuditPS.executeUpdate();
                    if (rows == 1) continue;
                    this.logger.error("Update the tracelogid in the audit table, update returned: " + String.valueOf(rows));
                    continue;
                }
                catch (SQLException e) {
                    throw new SapphireException("EXECUTE_STMT_FAILED", "Error Updating the audit record. Exception: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.sapphireConnection.getConnectionId())) + " executing " + updateSQL, e);
                }
            }
            database.closeStatement();
        }
        catch (Exception e) {
            this.logger.info("DeleteSDI", "Error Updating the audit record. Exception: " + e.getMessage() + " executing " + updateSQL);
        }
    }

    private void generateTraceLog(String sdcid, String keyid1, String keyid2, String keyid3, String auditDescription, PropertyList additionalColumns) {
        PropertyList sdc = this.getSDCProcessor().getPropertyList("SDIAttachment");
        String auditflag = sdc.getProperty("auditedflag");
        String promptflag = sdc.getProperty("auditpromptflag");
        if (additionalColumns.getProperty("tracelogid", "").trim().length() == 0 && !auditflag.equalsIgnoreCase("N")) {
            String reason = additionalColumns.getProperty("auditreason", "");
            String activity = additionalColumns.getProperty("auditactivity", "");
            String signedflag = additionalColumns.getProperty("auditsignedflag", "");
            String auditdt = additionalColumns.getProperty("auditdt", "");
            if (reason.length() > 0) {
                this.logger.info("Generate the tracelog records");
                String standard = "Y";
                if (!promptflag.equalsIgnoreCase("R") && !promptflag.equalsIgnoreCase("S")) {
                    standard = "N";
                }
                AuditService audit = new AuditService(this.sapphireConnection);
                try {
                    String traceLogId = audit.addSDITraceLogEntry(sdcid, keyid1, keyid2, keyid3, reason, activity, signedflag, auditdt, auditDescription, standard.equals("Y"));
                    additionalColumns.setProperty("tracelogid", traceLogId);
                }
                catch (ServiceException e) {
                    this.logger.error("Failed to add audit records", e);
                }
            }
        }
    }
}

