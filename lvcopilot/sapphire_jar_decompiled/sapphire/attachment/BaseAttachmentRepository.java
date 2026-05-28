/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package sapphire.attachment;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.LabVantageClassLoader;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.file.FileTransfer;
import com.labvantage.sapphire.util.file.FileTransferOptions;
import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import com.labvantage.sapphire.xml.PropertyTree;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.attachment.Attachment;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public abstract class BaseAttachmentRepository
extends BaseCustom {
    private SapphireConnection sapphireConnection = null;
    private PropertyList attachmentPolicy = null;
    private PropertyList repository = null;
    private boolean managed = true;
    private boolean enableCleanUpRepository = true;
    private ClassLoader classLoader = null;

    protected void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    public static BaseAttachmentRepository getRepository(String filerepositoryid, String filerepositorynode, SapphireConnection conn) throws SapphireException {
        BaseAttachmentRepository baseAttachmentRepository = null;
        if (filerepositoryid != null && filerepositoryid.length() > 0) {
            if (filerepositorynode == null || filerepositorynode.length() == 0) {
                filerepositorynode = "Sapphire Custom";
            }
            WebAdminProcessor wap = new WebAdminProcessor(conn.getConnectionId());
            PropertyList repositoryPropertyList = null;
            try {
                repositoryPropertyList = (PropertyList)CacheUtil.get(conn.getDatabaseId(), "PropertyTreeNode", filerepositoryid + ";" + filerepositorynode);
                if (repositoryPropertyList == null) {
                    PropertyTree repository = wap.getPropertyTree(filerepositoryid);
                    repositoryPropertyList = repository.getNodePropertyList(filerepositorynode, true);
                    CacheUtil.put(conn.getDatabaseId(), "PropertyTreeNode", filerepositorynode + ";" + filerepositorynode, repositoryPropertyList);
                }
                if (repositoryPropertyList != null) {
                    if (wap == null) {
                        wap = new WebAdminProcessor(conn.getConnectionId());
                    }
                    String rolelist = conn.getRoleList();
                    String modulelist = conn.getModuleList();
                    Set<String> inactiveRoles = wap.getInactiveRoleList();
                    String languageid = conn.getLanguage();
                    repositoryPropertyList.setDbms(conn.getDbms());
                    repositoryPropertyList.setDatabaseid(conn.getDatabaseId());
                    repositoryPropertyList = repositoryPropertyList.copy(languageid == null || languageid.length() == 0 ? "(null)" : languageid, new TranslationProcessor(conn.getConnectionId()), rolelist, modulelist, inactiveRoles);
                }
            }
            catch (Exception e) {
                Trace.logWarn("Failed to load properties. Error: " + e.getMessage());
            }
            if (repositoryPropertyList == null) {
                Trace.logWarn("Unable to load properties for repository " + filerepositoryid + " and node " + filerepositorynode);
                repositoryPropertyList = new PropertyList();
            }
            try {
                LabVantageClassLoader labVantageClassLoader = null;
                if (SecurityPolicyUtil.isJavaAttachmentsPermitted(conn.getConnectionId(), LabVantageClassLoader.ClassLoaderType.ATTACHMENTREPOSITORY.getArea())) {
                    String appresource = repositoryPropertyList != null ? repositoryPropertyList.getProperty("appresource", "") : "";
                    labVantageClassLoader = LabVantageClassLoader.getClassLoader(LabVantageClassLoader.ClassLoaderType.ATTACHMENTREPOSITORY, filerepositoryid, appresource, null, null, null, conn);
                    Trace.logDebug("AttachmentRepository loaded.");
                } else {
                    Trace.logDebug("Class loaders disabled in security policy.");
                }
                String objectname = wap.getPropertyTreeObject(filerepositoryid);
                baseAttachmentRepository = (BaseAttachmentRepository)LabVantageClassLoader.instanitateClass(labVantageClassLoader, objectname);
                baseAttachmentRepository.setClassLoader(labVantageClassLoader);
                baseAttachmentRepository.setConnectionId(conn.getConnectionId());
                baseAttachmentRepository.setSapphireConnection(conn);
                baseAttachmentRepository.setRepositoryProperties(repositoryPropertyList);
            }
            catch (Exception e) {
                throw new SapphireException("Failed to get repository", e);
            }
            return baseAttachmentRepository;
        }
        return baseAttachmentRepository;
    }

    protected void setSapphireConnection(SapphireConnection sapphireConnection) {
        this.sapphireConnection = sapphireConnection;
    }

    protected void setCleanUpRepository(boolean enableCleanUpRepository) {
        this.enableCleanUpRepository = enableCleanUpRepository;
    }

    protected void setManaged(boolean managed) {
        this.managed = managed;
    }

    protected void setAttachmentPolicy(PropertyList attachmentPolicy) {
        this.attachmentPolicy = attachmentPolicy;
    }

    protected void setRepositoryProperties(PropertyList repositoryProperties) {
        this.repository = repositoryProperties;
        if (this.repository != null && this.repository.containsKey("managed")) {
            this.setManaged(this.repository.getProperty("managed", "Y").equalsIgnoreCase("Y"));
        }
        if (this.repository != null && this.repository.containsKey("enablecleanuprepository")) {
            this.setCleanUpRepository(this.repository.getProperty("enablecleanuprepository", "N").equalsIgnoreCase("Y"));
        }
    }

    protected SapphireConnection getSapphireConnection() {
        return this.sapphireConnection;
    }

    public boolean isManaged() {
        return this.managed;
    }

    public boolean isCleanUpRepositoryEnabled() {
        return this.enableCleanUpRepository;
    }

    public PropertyList getAttachmentPolicy() {
        return this.attachmentPolicy;
    }

    public PropertyList getRepositoryProperties() {
        return this.repository;
    }

    public abstract boolean checkHash(Attachment var1);

    public abstract boolean canGenerateThumbnail();

    public abstract boolean canBrowseRepository();

    public abstract String[] getBrowseIncludes();

    public abstract String getBrowseScript(String var1);

    public abstract String getBrowseButtonText();

    public abstract boolean enableCaching();

    public abstract boolean canEncrypt();

    public abstract boolean canCompress();

    public abstract boolean canHash();

    public abstract void getSDIAttachment(Attachment var1, Attachment.ThumbnailGeneration var2) throws SapphireException;

    public abstract void preAddSDIAttachment(Attachment var1) throws SapphireException;

    public abstract void postAddSDIAttachment(Attachment var1) throws SapphireException;

    public abstract boolean preEditSDIAttachment(Attachment var1, Attachment var2) throws SapphireException;

    public abstract void postEditSDIAttachment(Attachment var1, Attachment var2) throws SapphireException;

    public abstract void preDeleteSDIAttachment(Attachment var1) throws SapphireException;

    public abstract void postDeleteSDIAttachment(Attachment var1) throws SapphireException;

    public abstract void cleanUpRepository(Attachment var1) throws SapphireException;

    protected boolean isFilePresentInOtherAttachments(Attachment oldAttachment, boolean postDelete) {
        return this.isFilePresentInOtherAttachments(oldAttachment.getAttachmentRepositoryId(), oldAttachment.getAttachmentRepositoryNodeId(), oldAttachment.getRepositoryId(), postDelete, true);
    }

    protected boolean isFilePresentInOtherAttachments(Attachment oldAttachment, boolean postDelete, boolean checkAudit) {
        return this.isFilePresentInOtherAttachments(oldAttachment.getAttachmentRepositoryId(), oldAttachment.getAttachmentRepositoryNodeId(), oldAttachment.getRepositoryId(), postDelete, checkAudit);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private boolean isFilePresentInOtherAttachments(String repositoryid, String repositorynode, String externalrepositoryId, boolean postDelete, boolean checkAudit) {
        String sql = "SELECT count(keyid1) FROM sdiattachment WHERE attachmentrepositoryid=? AND attachmentrepositorynodeid=? AND externalrepository=?";
        int c = this.getQueryProcessor().getPreparedCount(sql, new Object[]{repositoryid, repositorynode, externalrepositoryId});
        if (c != (postDelete ? 0 : 1)) return true;
        if (!checkAudit) return false;
        try {
            sql = "SELECT count(keyid1) FROM a_sdiattachment WHERE attachmentrepositoryid=? AND attachmentrepositorynodeid=? AND externalrepository=?";
            c = this.getQueryProcessor().getPreparedCount(sql, new Object[]{repositoryid, repositorynode, externalrepositoryId});
            return c != 0;
        }
        catch (Exception e) {
            try {
                return false;
            }
            catch (Exception e2) {
                return true;
            }
        }
    }

    protected DataSet getUniqueFilesInAudit(Attachment oldAttachment) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT sdcid, keyid1, keyid2, keyid3, attachmentnum, filename, externalrepository, attachmentrepositorynodeid, attachmentrepositoryid ");
        sql.append("FROM a_sdiattachment ");
        sql.append("WHERE sdcid = ? ");
        sql.append("AND keyid1 = ? ");
        sql.append("AND keyid2 = ? ");
        sql.append("AND keyid3 = ? ");
        sql.append("AND attachmentnum = ? ");
        sql.append("AND externalrepository = ? ");
        if (this.getSapphireConnection().isOracle()) {
            sql.append("AND (externalrepository,attachmentrepositorynodeid,attachmentrepositoryid) IN (SELECT externalrepository,attachmentrepositorynodeid,attachmentrepositoryid ");
        } else {
            sql.append("AND externalrepository+attachmentrepositorynodeid+attachmentrepositoryid IN (SELECT externalrepository+attachmentrepositorynodeid+attachmentrepositoryid ");
        }
        sql.append("FROM a_sdiattachment ");
        sql.append("WHERE sdcid = ? ");
        sql.append("AND keyid1 = ? ");
        sql.append("AND keyid2 = ? ");
        sql.append("AND keyid3 = ? ");
        sql.append("AND attachmentnum = ? ");
        if (this.getSapphireConnection().isOracle()) {
            sql.append("MINUS ");
        } else {
            sql.append("EXCEPT ");
        }
        sql.append("( ");
        if (this.getSapphireConnection().isOracle()) {
            sql.append("SELECT asa.externalrepository,asa.attachmentrepositorynodeid,asa.attachmentrepositoryid ");
        } else {
            sql.append("SELECT asa.externalrepository+asa.attachmentrepositorynodeid+asa.attachmentrepositoryid ");
        }
        sql.append("FROM a_sdiattachment asa,sdiattachment s1 ");
        sql.append("WHERE asa.sdcid = ? ");
        sql.append("AND asa.keyid1 = ? ");
        sql.append("AND asa.keyid2 = ? ");
        sql.append("AND asa.keyid3 = ? ");
        sql.append("AND asa.attachmentnum = ? ");
        sql.append("AND s1.attachmentrepositoryid = asa.attachmentrepositoryid ");
        sql.append("AND s1.attachmentrepositorynodeid = asa.attachmentrepositorynodeid ");
        sql.append("AND s1.externalrepository = asa.externalrepository ");
        sql.append("AND ( ");
        sql.append("s1.sdcid != asa.sdcid ");
        sql.append("OR s1.keyid1 != asa.keyid1 ");
        sql.append("OR s1.keyid2 != asa.keyid2 ");
        sql.append("OR s1.keyid3 != asa.keyid3 ");
        sql.append("OR s1.attachmentnum != asa.attachmentnum ");
        sql.append(" ) ");
        sql.append("UNION ");
        if (this.getSapphireConnection().isOracle()) {
            sql.append("SELECT asa.externalrepository,asa.attachmentrepositorynodeid,asa.attachmentrepositoryid ");
        } else {
            sql.append("SELECT asa.externalrepository+asa.attachmentrepositorynodeid+asa.attachmentrepositoryid ");
        }
        sql.append("FROM a_sdiattachment asa,a_sdiattachment s1 ");
        sql.append("WHERE asa.sdcid = ? ");
        sql.append("AND asa.keyid1 = ? ");
        sql.append("AND asa.keyid2 = ? ");
        sql.append("AND asa.keyid3 = ? ");
        sql.append("AND asa.attachmentnum = ? ");
        sql.append("AND s1.attachmentrepositoryid = asa.attachmentrepositoryid ");
        sql.append("AND s1.attachmentrepositorynodeid = asa.attachmentrepositorynodeid ");
        sql.append("AND s1.externalrepository = asa.externalrepository ");
        sql.append("AND ( ");
        sql.append("s1.sdcid != asa.sdcid ");
        sql.append("OR s1.keyid1 != asa.keyid1 ");
        sql.append("OR s1.keyid2 != asa.keyid2 ");
        sql.append("OR s1.keyid3 != asa.keyid3 ");
        sql.append("OR s1.attachmentnum != asa.attachmentnum ");
        sql.append(" ) ");
        sql.append(") ");
        sql.append(") ");
        return this.getQueryProcessor().getPreparedSqlDataSet("auditattachment", sql.toString(), new Object[]{oldAttachment.getSDCId(), oldAttachment.getKeyId1(), oldAttachment.getKeyId2(), oldAttachment.getKeyId3(), oldAttachment.getAttachmentNum(), oldAttachment.getRepositoryId(), oldAttachment.getSDCId(), oldAttachment.getKeyId1(), oldAttachment.getKeyId2(), oldAttachment.getKeyId3(), oldAttachment.getAttachmentNum(), oldAttachment.getSDCId(), oldAttachment.getKeyId1(), oldAttachment.getKeyId2(), oldAttachment.getKeyId3(), oldAttachment.getAttachmentNum(), oldAttachment.getSDCId(), oldAttachment.getKeyId1(), oldAttachment.getKeyId2(), oldAttachment.getKeyId3(), oldAttachment.getAttachmentNum()}, true);
    }

    protected void transferData(InputStream inputStream, File output, boolean closeInput, boolean closeOutput) throws SapphireException {
        FileTransferOptions fto = new FileTransferOptions();
        fto.setCloseInputStream(closeInput);
        fto.setCloseOutputStream(closeOutput);
        try {
            FileTransfer.safeDataTransfer(inputStream, output, fto);
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    protected void transferData(InputStream inputStream, OutputStream outputStream, boolean closeInput, boolean closeOutput) throws SapphireException {
        FileTransferOptions fto = new FileTransferOptions();
        fto.setCloseInputStream(closeInput);
        fto.setCloseOutputStream(closeOutput);
        try {
            FileTransfer.safeDataTransfer(inputStream, outputStream, fto);
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    public InputStream getDirectInputStream(PropertyList propertyList) throws SapphireException {
        return null;
    }

    public String getFileViewerHTML(String fileViewerId, PropertyList propertyList, PageContext pageContext) throws SapphireException {
        return "";
    }

    public static String getFileViewerHTML(String repositoryId, String repositoryNodeId, String fileViewerId, PropertyList propertyList, PageContext pageContext, SapphireConnection sapphireConnection) throws SapphireException {
        AtomicReference stringRef = new AtomicReference();
        try {
            BaseAttachmentRepository baseAttachmentRepository = BaseAttachmentRepository.getRepository(repositoryId, repositoryNodeId, sapphireConnection);
            LabVantageClassLoader.executeCode(baseAttachmentRepository.getClass().getClassLoader(), () -> stringRef.set(baseAttachmentRepository.getFileViewerHTML(fileViewerId, propertyList, pageContext)), false);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to obtain view", e);
        }
        return (String)stringRef.get();
    }

    public static InputStream getDirectInputStream(String repositoryId, String repositoryNodeId, PropertyList props, SapphireConnection sapphireConnection) throws SapphireException {
        AtomicReference inputStreamReference = new AtomicReference();
        try {
            BaseAttachmentRepository baseAttachmentRepository = BaseAttachmentRepository.getRepository(repositoryId, repositoryNodeId, sapphireConnection);
            LabVantageClassLoader.executeCode(baseAttachmentRepository.getClass().getClassLoader(), () -> inputStreamReference.set(baseAttachmentRepository.getDirectInputStream(props)), false);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to obtain repository information", e);
        }
        return (InputStream)inputStreamReference.get();
    }
}

