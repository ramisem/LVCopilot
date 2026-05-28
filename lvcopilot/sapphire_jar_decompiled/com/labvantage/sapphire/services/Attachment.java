/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.services;

import com.labvantage.sapphire.attachment.AttachmentRule;
import com.labvantage.sapphire.pageelements.attachment.AttachmentType_File;
import com.labvantage.sapphire.pageelements.attachment.AttachmentType_FormattedText;
import com.labvantage.sapphire.pageelements.attachment.AttachmentType_LinkedReference;
import com.labvantage.sapphire.pageelements.attachment.AttachmentType_PlainText;
import com.labvantage.sapphire.pageelements.attachment.AttachmentType_URL;
import com.labvantage.sapphire.pageelements.attachment.BaseAttachmentType;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.file.FileManager;
import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.attachment.Attachment;
import sapphire.attachment.BaseAttachmentRepository;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class Attachment
extends sapphire.attachment.Attachment
implements Serializable {
    static final String LABVANTAGE_CVS_ID = "$Revision: 96650 $";
    public static final String PROPERTY_SDCID = "sdcid";
    public static final String PROPERTY_KEYID1 = "keyid1";
    public static final String PROPERTY_KEYID2 = "keyid2";
    public static final String PROPERTY_KEYID3 = "keyid3";
    public static final String PROPERTY_FILENAME = "filename";
    public static final String PROPERTY_URL = "url";
    public static final String PROPERTY_PLAINTEXT = "plaintext";
    public static final String PROPERTY_FORMATTEDTEXT = "formattedtext";
    public static final String PROPERTY_ATTACHMENTCLOB = "attachmentclob";
    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_DESCRIPTION = "description";
    public static final String PROPERTY_APPLYLOCK = "applylock";
    public static final String PROPERTY_INDEX = "index";
    public static final String PROPERTY_UPLOADTO = "uploadto";
    public static final String PROPERTY_OLECLASS = "oleclass";
    public static final String PROPERTY_ATTACHMENTNUM = "attachmentnum";
    public static final String PROPERTY_SOURCEFILENAME = "sourcefilename";
    public static final String PROPERTY_LINKSDCID = "linksdcid";
    public static final String PROPERTY_LINKKEYID1 = "linkkeyid1";
    public static final String PROPERTY_LINKKEYID2 = "linkkeyid2";
    public static final String PROPERTY_LINKKEYID3 = "linkkeyid3";
    public static final String PROPERTY_LINKATTACHMENTNUM = "linkattachmentnum";
    public static final String PROPERTY_CONTENTREVISION = "contentrevision";
    public static final String PROPERTY_THUMBNAMEIMAGE = "thumbnailimage";
    public static final String PROPERTY_ATTACHMENTPOLICYNODE = "attachmentpolicynode";
    private static ArrayList<BaseAttachmentType> allAttachmentTypesList = new ArrayList();
    private static HashMap<String, BaseAttachmentType> allAttachmentTypesMap = new HashMap();
    private static String displayValue = "";
    private static String allTypeflagList = "";

    @Override
    public void setParentDepartment(String parentDepartment) {
        super.setParentDepartment(parentDepartment);
    }

    @Override
    public String getParentDepartment() {
        return super.getParentDepartment();
    }

    @Override
    public boolean checkHash(long hash) {
        return super.checkHash(hash);
    }

    @Override
    public void setInvalidHash(boolean invalidHash) {
        super.setInvalidHash(invalidHash);
    }

    @Override
    public boolean checkHash() {
        return super.checkHash();
    }

    @Override
    public boolean getTriggerBusinessRule() {
        return super.getTriggerBusinessRule();
    }

    @Override
    public void setTriggerBusinessRule(boolean triggerBusinessRule) {
        super.setTriggerBusinessRule(triggerBusinessRule);
    }

    @Override
    public String getSDIAttachmentId() {
        return super.getSDIAttachmentId();
    }

    @Override
    public void setSDIAttachmentId(String sdiattachmentid) {
        super.setSDIAttachmentId(sdiattachmentid);
    }

    @Override
    public void setSDCId(String sdcid) {
        super.setSDCId(sdcid);
    }

    @Override
    public void setKeyId1(String keyid1) {
        super.setKeyId1(keyid1);
    }

    @Override
    public void setKeyId2(String keyid2) {
        super.setKeyId2(keyid2);
    }

    @Override
    public void setKeyId3(String keyid3) {
        super.setKeyId3(keyid3);
    }

    @Override
    public void setAttachmentNum(int attnum) {
        super.setAttachmentNum(attnum);
    }

    @Override
    public void setAttachmentClass(String attclass) {
        super.setAttachmentClass(attclass);
    }

    @Override
    public void setUploadTo(String uploadTo) {
        super.setUploadTo(uploadTo);
    }

    @Override
    public String getUploadTo() {
        return super.getUploadTo();
    }

    @Override
    public void setAttachmentRepositoryId(String attachmentRepositoryId) {
        super.setAttachmentRepositoryId(attachmentRepositoryId);
    }

    @Override
    public void setAttachmentRepositoryNodeId(String attachmentRepositoryNodeId) {
        super.setAttachmentRepositoryNodeId(attachmentRepositoryNodeId);
    }

    @Override
    public String getAttachmentRepositoryId() {
        return super.getAttachmentRepositoryId();
    }

    @Override
    public String getAttachmentRepositoryNodeId() {
        return super.getAttachmentRepositoryNodeId();
    }

    @Override
    public Path getLocalFile() {
        return super.getLocalFile();
    }

    public Path saveLocalFile(boolean cache, String databaseid) {
        return super.saveLocalFile(cache, databaseid, false, false, null, true);
    }

    public Path saveLocalFile() {
        return super.saveLocalFile(false, null, false, false, null, false);
    }

    public Path saveLocalFile(Path usepath) {
        return super.saveLocalFile(false, null, false, false, usepath, true);
    }

    public Path saveLocalFile(boolean cache, String databaseid, boolean checkHash, boolean generateHash) {
        return super.saveLocalFile(cache, databaseid, checkHash, generateHash, null, true);
    }

    @Override
    public void loadLocalFile(Path p) {
        super.loadLocalFile(p);
    }

    @Override
    protected Path getCachedPath(String databaseid, boolean explodedzip) {
        return super.getCachedPath(databaseid, explodedzip);
    }

    public static BaseAttachmentType getAttachmentType(String typeflag) {
        return allAttachmentTypesMap.get(typeflag);
    }

    public static BaseAttachmentType getAttachmentType(Attachment.AttachmentType type) {
        return allAttachmentTypesMap.get(type.getFlag());
    }

    public static String getDisplayValue() {
        return displayValue;
    }

    public static String getSelectedDisplayValue(String types) {
        String[] typesArray;
        String display = "";
        block0: for (String typeFlag : typesArray = types.split(";")) {
            for (BaseAttachmentType type : allAttachmentTypesList) {
                if (!type.getAllTypeflagList().contains(typeFlag)) continue;
                display = display + (display.length() > 0 ? ";" : "") + type.getDisplayValue(typeFlag);
                continue block0;
            }
        }
        return display;
    }

    public static String getAllTypeflagList() {
        return allTypeflagList;
    }

    public static String getAllTypeflagList(boolean allowFormattedText) {
        if (allowFormattedText) {
            return allTypeflagList;
        }
        String typeList = "";
        for (BaseAttachmentType type : allAttachmentTypesList) {
            if (type.getAllTypeflagList().equals("M")) continue;
            typeList = typeList + (typeList.length() > 0 ? ";" : "") + type.getAllTypeflagList();
        }
        return typeList;
    }

    public static ArrayList<BaseAttachmentType> getAllAttachmentTypesList() {
        return allAttachmentTypesList;
    }

    @Override
    public void setAttachmentType(Attachment.AttachmentType type) {
        this.setType(type.getFlag());
    }

    @Override
    public void setType(String type) {
        super.setType(type);
    }

    @Override
    public void setUrl(String url) {
        super.setUrl(url);
    }

    public static String correctTypeFlag(String typeflag) {
        if (typeflag == null) {
            return "";
        }
        if (typeflag.equalsIgnoreCase("R") || typeflag.equalsIgnoreCase("reference")) {
            return "R";
        }
        if (typeflag.equalsIgnoreCase("F") || typeflag.equalsIgnoreCase("store")) {
            return "F";
        }
        if (typeflag.equalsIgnoreCase("U") || typeflag.equalsIgnoreCase("UploadAndReference") || typeflag.equalsIgnoreCase("upload")) {
            return "U";
        }
        if (typeflag.equalsIgnoreCase("S") || typeflag.equalsIgnoreCase("UploadAndStore") || typeflag.equalsIgnoreCase("file")) {
            return "S";
        }
        if (typeflag.equalsIgnoreCase("L") || typeflag.equalsIgnoreCase(PROPERTY_URL)) {
            return "L";
        }
        if (typeflag.equalsIgnoreCase("P") || typeflag.equalsIgnoreCase(PROPERTY_PLAINTEXT)) {
            return "P";
        }
        if (typeflag.equalsIgnoreCase("D") || typeflag.equalsIgnoreCase("linkedreference")) {
            return "D";
        }
        if (typeflag.equalsIgnoreCase("M") || typeflag.equalsIgnoreCase(PROPERTY_FORMATTEDTEXT) || typeflag.equalsIgnoreCase("richtext")) {
            return "M";
        }
        return typeflag;
    }

    public void writeToDataSet(DataSet sdiAttachmentData, int sdiAttRow) {
        if (!sdiAttachmentData.isValidColumn(PROPERTY_ATTACHMENTNUM)) {
            sdiAttachmentData.addColumn(PROPERTY_ATTACHMENTNUM, 1);
        }
        for (int c = 0; c < sdiAttachmentData.getColumnCount(); ++c) {
            String columnid = sdiAttachmentData.getColumnId(c);
            try {
                if (columnid.equalsIgnoreCase(PROPERTY_SDCID)) {
                    sdiAttachmentData.setValue(sdiAttRow, PROPERTY_SDCID, this.getSDCId());
                    continue;
                }
                if (columnid.equalsIgnoreCase(PROPERTY_KEYID1)) {
                    sdiAttachmentData.setValue(sdiAttRow, PROPERTY_KEYID1, this.getKeyId1());
                    continue;
                }
                if (columnid.equalsIgnoreCase(PROPERTY_KEYID2)) {
                    sdiAttachmentData.setValue(sdiAttRow, PROPERTY_KEYID2, this.getKeyId2());
                    continue;
                }
                if (columnid.equalsIgnoreCase(PROPERTY_KEYID3)) {
                    sdiAttachmentData.setValue(sdiAttRow, PROPERTY_KEYID3, this.getKeyId3());
                    continue;
                }
                if (columnid.equalsIgnoreCase(PROPERTY_ATTACHMENTNUM)) {
                    sdiAttachmentData.setNumber(sdiAttRow, PROPERTY_ATTACHMENTNUM, this.getAttachmentNum());
                    continue;
                }
                if (columnid.equalsIgnoreCase("attachmentdesc")) {
                    sdiAttachmentData.setValue(sdiAttRow, "attachmentdesc", this.getDescription());
                    continue;
                }
                if (columnid.equalsIgnoreCase(PROPERTY_DESCRIPTION)) {
                    sdiAttachmentData.setValue(sdiAttRow, PROPERTY_DESCRIPTION, this.getDescription());
                    continue;
                }
                if (columnid.equalsIgnoreCase("typeflag")) {
                    sdiAttachmentData.setValue(sdiAttRow, "typeflag", this.getType());
                    continue;
                }
                if (columnid.equalsIgnoreCase(PROPERTY_TYPE)) {
                    sdiAttachmentData.setValue(sdiAttRow, PROPERTY_TYPE, this.getType());
                    continue;
                }
                if (columnid.equalsIgnoreCase(PROPERTY_FILENAME)) {
                    sdiAttachmentData.setValue(sdiAttRow, PROPERTY_FILENAME, this.getFilename());
                    continue;
                }
                if (columnid.equalsIgnoreCase(PROPERTY_SOURCEFILENAME)) {
                    sdiAttachmentData.setValue(sdiAttRow, PROPERTY_SOURCEFILENAME, this.getSourceFilename());
                    continue;
                }
                if (columnid.equalsIgnoreCase("attachmentclass")) {
                    sdiAttachmentData.setValue(sdiAttRow, "attachmentclass", this.getAttachmentClass());
                    continue;
                }
                if (columnid.equalsIgnoreCase("attachmentsize")) {
                    sdiAttachmentData.setNumber(sdiAttRow, "attachmentsize", this.getSize());
                    continue;
                }
                if (columnid.equalsIgnoreCase("attachmentrepositoryid")) {
                    sdiAttachmentData.setValue(sdiAttRow, "attachmentrepositoryid", this.getAttachmentRepositoryId());
                    continue;
                }
                if (columnid.equalsIgnoreCase("attachmentrepositorynodeid")) {
                    sdiAttachmentData.setValue(sdiAttRow, "attachmentrepositorynodeid", this.getAttachmentRepositoryNodeId());
                    continue;
                }
                if (columnid.equalsIgnoreCase("externalrepository")) {
                    sdiAttachmentData.setValue(sdiAttRow, "externalrepository", this.getRepositoryId());
                    continue;
                }
                if (columnid.equalsIgnoreCase("datahash")) {
                    sdiAttachmentData.setValue(sdiAttRow, "datahash", "" + this.getDataHash());
                    continue;
                }
                if (columnid.equalsIgnoreCase("compressedflag")) {
                    sdiAttachmentData.setValue(sdiAttRow, "compressedflag", this.isCompressed() ? "Y" : "N");
                    continue;
                }
                if (!columnid.equalsIgnoreCase("encryptedflag")) continue;
                sdiAttachmentData.setValue(sdiAttRow, "encryptedflag", this.isEncrypted() ? "Y" : "N");
                continue;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    @Override
    public void setLinkSDI(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentnum) {
        super.setLinkSDI(sdcid, keyid1, keyid2, keyid3, attachmentnum);
    }

    public static Attachment getAttachment(Attachment attachment, QueryProcessor queryProcessor, String connectionId) {
        DataSet sdiAttachmentData = null;
        sdiAttachmentData = attachment.getAttachmentClass() != null && attachment.getAttachmentClass().length() > 0 ? (attachment.getAttachmentNum() > -1 ? queryProcessor.getPreparedSqlDataSet("SELECT a.* FROM sdiattachment a WHERE a.sdcid=? AND a.keyid1=? AND a.keyid2=? AND a.keyid3=? AND a.attachmentnum=? AND a.attachmentclass=?", new Object[]{attachment.getSDCId(), attachment.getKeyId1(), attachment.getKeyId2(), attachment.getKeyId3(), attachment.getAttachmentNum(), attachment.getAttachmentClass()}, true) : queryProcessor.getPreparedSqlDataSet("SELECT a.* FROM sdiattachment a WHERE a.sdcid=? AND a.keyid1=? AND a.keyid2=? AND a.keyid3=? AND a.attachmentclass=?", new Object[]{attachment.getSDCId(), attachment.getKeyId1(), attachment.getKeyId2(), attachment.getKeyId3(), attachment.getAttachmentClass()}, true)) : queryProcessor.getPreparedSqlDataSet("SELECT a.* FROM sdiattachment a WHERE a.sdcid=? AND a.keyid1=? AND a.keyid2=? AND a.keyid3=? AND a.attachmentnum=?", new Object[]{attachment.getSDCId(), attachment.getKeyId1(), attachment.getKeyId2(), attachment.getKeyId3(), attachment.getAttachmentNum()}, true);
        if (sdiAttachmentData != null && sdiAttachmentData.getRowCount() > 0) {
            return (Attachment)Attachment.getAttachment(sdiAttachmentData, 0, connectionId);
        }
        return null;
    }

    @Override
    public void setSize(long size) {
        super.setSize(size);
    }

    public static Attachment getAttachment(DataSet sdiAttachmentData, String sdcid, String keyid1, String keyid2, String keyid3, int attachmentNum, String connectionId) {
        if (sdiAttachmentData == null || sdiAttachmentData.getRowCount() == 0) {
            return null;
        }
        HashMap<String, Object> findMap = new HashMap<String, Object>();
        findMap.put(PROPERTY_SDCID, sdcid);
        findMap.put(PROPERTY_KEYID1, keyid1);
        findMap.put(PROPERTY_KEYID2, keyid2);
        findMap.put(PROPERTY_KEYID3, keyid3);
        findMap.put(PROPERTY_ATTACHMENTNUM, new BigDecimal(attachmentNum));
        int sdiAttRow = sdiAttachmentData.findRow(findMap);
        if (sdiAttRow > -1) {
            return (Attachment)Attachment.getAttachment(sdiAttachmentData, sdiAttRow, connectionId);
        }
        return null;
    }

    @Override
    public void setAdditionalColumn(String key, String value) {
        super.setAdditionalColumn(key, value);
    }

    @Override
    public PropertyList getAdditionalColumns() {
        return super.getAdditionalColumns();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void readTempFile(QueryProcessor queryProcessor, ActionProcessor actionProcessor, String connectionId) throws SapphireException {
        if (this.getTempId() != null && this.getTempId().length() > 0) {
            ConfigurationProcessor cp = new ConfigurationProcessor(connectionId);
            PropertyList attachmentPolicy = null;
            if (cp != null) {
                try {
                    attachmentPolicy = cp.getPolicy("AttachmentPolicy", "Sapphire Custom");
                }
                catch (Exception e) {
                    attachmentPolicy = null;
                }
            }
            Attachment.ThumbnailGeneration thumbnailGeneration = attachmentPolicy != null ? Attachment.ThumbnailGeneration.getThumbnailGeneration(attachmentPolicy.getProperty("thumbnails", Attachment.ThumbnailGeneration.GENERATEANDSTORE.getTitle()), Attachment.ThumbnailGeneration.GENERATEANDSTORE) : Attachment.ThumbnailGeneration.GENERATEANDSTORE;
            FileManager.TempFile tempFile = FileManager.TempFile.getTempFile(this.getTempId(), thumbnailGeneration.canGenerate(), queryProcessor, connectionId);
            if (tempFile != null) {
                try {
                    if (tempFile.getOrgData() != null) {
                        JSONObject job = tempFile.toJSONObject(false, true, false);
                        this.setClob(StringUtil.replaceAll(job.toString(), ";", "#semicolon#", false));
                        this.setInputStream(tempFile.getData() != null ? tempFile.getData().getInputStream() : null);
                    } else if (tempFile.getData() != null) {
                        this.setInputStream(tempFile.getData() != null ? tempFile.getData().getInputStream() : null);
                    }
                    if (this.getFilename() == null || this.getFilename().length() == 0) {
                        this.setFilename(tempFile.getFileName());
                    }
                    if ((this.getThumbnailImage() == null || this.getThumbnailImage().length() == 0) && tempFile.getThumbnail() != null && tempFile.getThumbnail().length() > 0) {
                        this.setThumbnailImage(tempFile.getThumbnail());
                    }
                    if (this.getSourceFilename() == null || this.getSourceFilename().length() == 0) {
                        this.setSourceFilename(tempFile.getFileName());
                    }
                }
                finally {
                    FileManager.TempFile.removeTempFile(this.getTempId(), actionProcessor, queryProcessor, connectionId);
                }
            }
        }
    }

    @Override
    public AttachmentRule getAttachmentRule() {
        return super.getAttachmentRule();
    }

    @Override
    public long getDataHash() {
        return super.getDataHash();
    }

    @Override
    public void setLockAttachment(boolean lockAttachment) {
        super.setLockAttachment(lockAttachment);
    }

    @Override
    public boolean getAllowLocalCache() {
        return super.getAllowLocalCache();
    }

    @Override
    public void setAllowLocalCache(boolean allowlocalcache) {
        super.setAllowLocalCache(allowlocalcache);
    }

    @Override
    public BaseAttachmentRepository getAttachmentRepository(SapphireConnection sapphireConnection, PropertyList attachmentPolicy, boolean defaultToCompatability) throws SapphireException {
        return super.getAttachmentRepository(sapphireConnection, attachmentPolicy, defaultToCompatability);
    }

    @Override
    public BaseAttachmentRepository getAttachmentRepository(SapphireConnection sapphireConnection, PropertyList attachmentPolicy, boolean defaultToCompatability, sapphire.attachment.Attachment basedOn) throws SapphireException {
        return super.getAttachmentRepository(sapphireConnection, attachmentPolicy, defaultToCompatability, basedOn);
    }

    @Override
    public boolean isZippingRequired(PropertyList attachmentPolicy, ConfigurationProcessor cp) {
        return super.isZippingRequired(attachmentPolicy, cp);
    }

    @Override
    public boolean isEncryptionRequired(PropertyList attachmentPolicy, ConfigurationProcessor cp) {
        return super.isEncryptionRequired(attachmentPolicy, cp);
    }

    @Override
    public BaseAttachmentRepository getAttachmentRepository(SapphireConnection sapphireConnection, PropertyList attachmentPolicy) throws SapphireException {
        return super.getAttachmentRepository(sapphireConnection, attachmentPolicy);
    }

    static {
        AttachmentType_File file = new AttachmentType_File();
        allAttachmentTypesList.add(file);
        allAttachmentTypesMap.put("R", file);
        allAttachmentTypesMap.put("S", file);
        allAttachmentTypesMap.put("U", file);
        allAttachmentTypesMap.put("F", file);
        AttachmentType_URL url = new AttachmentType_URL();
        allAttachmentTypesList.add(url);
        allAttachmentTypesMap.put("L", url);
        AttachmentType_PlainText plaintext = new AttachmentType_PlainText();
        allAttachmentTypesList.add(plaintext);
        allAttachmentTypesMap.put("P", plaintext);
        AttachmentType_LinkedReference linkedref = new AttachmentType_LinkedReference();
        allAttachmentTypesList.add(linkedref);
        allAttachmentTypesMap.put("D", linkedref);
        AttachmentType_FormattedText formattedtext = new AttachmentType_FormattedText();
        allAttachmentTypesList.add(formattedtext);
        allAttachmentTypesMap.put("M", formattedtext);
        for (BaseAttachmentType type : allAttachmentTypesList) {
            displayValue = displayValue + (displayValue.length() > 0 ? ";" : "") + type.getDisplayValue();
            allTypeflagList = allTypeflagList + (allTypeflagList.length() > 0 ? ";" : "") + type.getAllTypeflagList();
        }
    }
}

