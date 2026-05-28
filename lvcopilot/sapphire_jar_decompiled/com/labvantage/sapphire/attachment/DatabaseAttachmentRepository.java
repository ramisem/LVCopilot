/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.io.IOUtils
 */
package com.labvantage.sapphire.attachment;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.util.file.FileTransfer;
import com.labvantage.sapphire.util.file.FileTransferOptions;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.commons.io.IOUtils;
import sapphire.SapphireException;
import sapphire.attachment.Attachment;
import sapphire.attachment.BaseAttachmentRepository;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;

public class DatabaseAttachmentRepository
extends BaseAttachmentRepository {
    @Override
    public boolean checkHash(sapphire.attachment.Attachment attachment) {
        return false;
    }

    @Override
    public boolean canGenerateThumbnail() {
        return false;
    }

    @Override
    public boolean canBrowseRepository() {
        return false;
    }

    @Override
    public String[] getBrowseIncludes() {
        return null;
    }

    @Override
    public String getBrowseScript(String attachmentElementId) {
        return "";
    }

    @Override
    public String getBrowseButtonText() {
        return "";
    }

    @Override
    public boolean enableCaching() {
        return false;
    }

    @Override
    public boolean canEncrypt() {
        return false;
    }

    @Override
    public boolean canCompress() {
        return false;
    }

    @Override
    public boolean canHash() {
        return false;
    }

    @Override
    public void getSDIAttachment(sapphire.attachment.Attachment attachment, Attachment.ThumbnailGeneration thumbnailGeneration) throws SapphireException {
        Mode mode = Mode.getMode(this.getRepositoryProperties().getProperty("mode", "Attachment Table"));
        if (mode == Mode.ATTACHMENTTABLE) {
            try {
                if (attachment.getBlob() == null) {
                    DBUtil db = new DBUtil(this.getConnectionId());
                    db.setConnection(this.getSapphireConnection());
                    db.createPreparedResultSet("SELECT attachment FROM sdiattachment WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND attachmentnum = ?", new Object[]{attachment.getSDCId(), attachment.getKeyId1(), attachment.getKeyId2(), attachment.getKeyId3(), attachment.getAttachmentNum()});
                    if (db.getNext()) {
                        Blob blob = db.getBlob("attachment");
                        attachment.setBlob(blob);
                    }
                }
                attachment.setInputStream(attachment.getBlob().getBinaryStream());
            }
            catch (Exception e) {
                Logger.logError("Could not obtain blob.", e);
            }
        } else if (mode == Mode.ATTACHMENTSTORE) {
            this.getAttachmentInputStream(attachment, "attachmentstorage", "attachmentstorageid", "attachmentstorageblob");
        } else if (mode == Mode.CUSTOMTABLE) {
            PropertyList customTableInfo = this.getCustomTableInfo();
            this.getAttachmentInputStream(attachment, customTableInfo.getProperty("tableid"), customTableInfo.getProperty("idcolumn"), customTableInfo.getProperty("storagecolumn"));
        } else {
            throw new SapphireException("Unsupported Mode");
        }
    }

    @Override
    public void preAddSDIAttachment(sapphire.attachment.Attachment attachment) throws SapphireException {
        Mode mode = Mode.getMode(this.getRepositoryProperties().getProperty("mode", "Attachment Table"));
        if (mode == Mode.ATTACHMENTTABLE) {
            if (attachment.getInputStream() != null) {
                try {
                    Blob blob = this.getSapphireConnection().getConnection().createBlob();
                    FileTransferOptions fto = new FileTransferOptions();
                    fto.setCloseOutputStream(true);
                    fto.setCloseInputStream(true);
                    FileTransfer.safeDataTransfer(attachment.getInputStream(), blob.setBinaryStream(1L), fto);
                    attachment.setBlob(blob);
                }
                catch (Exception e) {
                    throw new SapphireException(e);
                }
            } else {
                attachment.setBlob(null);
            }
        } else if (mode == Mode.ATTACHMENTSTORE) {
            if (attachment.getInputStream() != null) {
                this.insertAttachment(attachment, "attachmentstorage", "attachmentstorageid", "attachmentstorageblob", mode);
            }
        } else if (mode == Mode.CUSTOMTABLE) {
            if (attachment.getInputStream() != null) {
                PropertyList customTableInfo = this.getCustomTableInfo();
                this.insertAttachment(attachment, customTableInfo.getProperty("tableid"), customTableInfo.getProperty("idcolumn"), customTableInfo.getProperty("storagecolumn"), mode);
            }
        } else {
            throw new SapphireException("Unsupported Mode");
        }
    }

    @Override
    public void postAddSDIAttachment(sapphire.attachment.Attachment attachment) throws SapphireException {
    }

    @Override
    public boolean preEditSDIAttachment(sapphire.attachment.Attachment attachment, sapphire.attachment.Attachment oldAttachment) throws SapphireException {
        Mode mode = Mode.getMode(this.getRepositoryProperties().getProperty("mode", "Attachment Table"));
        this.getSDIAttachment(oldAttachment, null);
        if (mode == Mode.ATTACHMENTTABLE) {
            if (attachment.getInputStream() != null) {
                try {
                    Blob blob = this.getSapphireConnection().getConnection().createBlob();
                    FileTransferOptions fto = new FileTransferOptions();
                    fto.setCloseOutputStream(true);
                    fto.setCloseInputStream(true);
                    FileTransfer.safeDataTransfer(attachment.getInputStream(), blob.setBinaryStream(1L), fto);
                    attachment.setBlob(blob);
                    return true;
                }
                catch (Exception e) {
                    throw new SapphireException(e);
                }
            }
        } else if (mode == Mode.ATTACHMENTSTORE) {
            if (attachment.getInputStream() != null && this.isContentChanged(attachment, oldAttachment)) {
                this.insertAttachment(attachment, "attachmentstorage", "attachmentstorageid", "attachmentstorageblob", mode);
                return true;
            }
        } else if (mode == Mode.CUSTOMTABLE) {
            if (attachment.getInputStream() != null && this.isContentChanged(attachment, oldAttachment)) {
                PropertyList customTableInfo = this.getCustomTableInfo();
                this.insertAttachment(attachment, customTableInfo.getProperty("tableid"), customTableInfo.getProperty("idcolumn"), customTableInfo.getProperty("storagecolumn"), mode);
                return true;
            }
        } else {
            throw new SapphireException("Unsupported Mode");
        }
        return false;
    }

    @Override
    public void postEditSDIAttachment(sapphire.attachment.Attachment attachment, sapphire.attachment.Attachment oldAttachment) throws SapphireException {
    }

    @Override
    public void preDeleteSDIAttachment(sapphire.attachment.Attachment attachment) throws SapphireException {
    }

    @Override
    public void postDeleteSDIAttachment(sapphire.attachment.Attachment oldAttachment) throws SapphireException {
    }

    @Override
    public void cleanUpRepository(sapphire.attachment.Attachment oldAttachment) throws SapphireException {
        Mode mode = Mode.getMode(this.getRepositoryProperties().getProperty("mode", "Attachment Table"));
        String tableId = "";
        String idColumn = "";
        boolean toBeDeleted = false;
        if (mode != Mode.ATTACHMENTTABLE) {
            if (mode == Mode.ATTACHMENTSTORE) {
                tableId = "attachmentstorage";
                idColumn = "attachmentstorageid";
                toBeDeleted = true;
            } else if (mode == Mode.CUSTOMTABLE) {
                PropertyList customTableInfo = this.getCustomTableInfo();
                tableId = customTableInfo.getProperty("tableid");
                idColumn = customTableInfo.getProperty("idcolumn");
                toBeDeleted = true;
            } else {
                throw new SapphireException("Unsupported Mode");
            }
        }
        if (toBeDeleted) {
            this.deleteFiles(oldAttachment, tableId, idColumn, true, true);
            DataSet tempDs = this.getUniqueFilesInAudit(oldAttachment);
            if (tempDs != null && tempDs.getRowCount() > 0) {
                for (int i = 0; i < tempDs.getRowCount(); ++i) {
                    Attachment audit = Attachment.getAttachment(tempDs, oldAttachment.getSDCId(), oldAttachment.getKeyId1(), oldAttachment.getKeyId2(), oldAttachment.getKeyId3(), oldAttachment.getAttachmentNum(), this.getConnectionId());
                    this.deleteFiles(audit, tableId, idColumn, false, false);
                }
            }
        }
    }

    private boolean isContentChanged(sapphire.attachment.Attachment newAttachment, sapphire.attachment.Attachment oldAttachment) {
        String f2;
        boolean isContentChanged = false;
        String f1 = oldAttachment.getFilename() != null ? oldAttachment.getFilename() : "";
        String string = f2 = newAttachment.getFilename() != null ? newAttachment.getFilename() : "";
        if (!f1.equalsIgnoreCase(f2) && !f2.equals("(unknown)")) {
            isContentChanged = true;
        } else {
            try {
                byte[] oldData = IOUtils.toByteArray((InputStream)oldAttachment.getInputStream());
                byte[] newData = IOUtils.toByteArray((InputStream)newAttachment.getInputStream());
                oldAttachment.setInputStream(new ByteArrayInputStream(oldData));
                newAttachment.setInputStream(new ByteArrayInputStream(newData));
                isContentChanged = !Arrays.equals(oldData, newData);
            }
            catch (IOException e) {
                isContentChanged = true;
            }
        }
        return isContentChanged;
    }

    private void insertAttachment(sapphire.attachment.Attachment attachment, String tableId, String idColumn, String blobColumn, Mode mode) throws SapphireException {
        String sqlInsert = "insert into " + tableId + " ( " + idColumn + ", " + blobColumn + " ) values( ?, ? )";
        DBUtil db = new DBUtil(this.getConnectionId());
        db.setConnection(this.getSapphireConnection());
        PreparedStatement insert = db.prepareStatement(sqlInsert);
        String repositoryId = "";
        try {
            Blob blob = this.getSapphireConnection().getConnection().createBlob();
            FileTransferOptions fto = new FileTransferOptions();
            fto.setCloseOutputStream(true);
            fto.setCloseInputStream(true);
            FileTransfer.safeDataTransfer(attachment.getInputStream(), blob.setBinaryStream(1L), fto);
            repositoryId = OpalUtil.getNextSequence(tableId, this.getSequenceProcessor());
            if (mode == Mode.ATTACHMENTSTORE) {
                repositoryId = "AS-" + repositoryId;
            } else if (mode == Mode.CUSTOMTABLE) {
                repositoryId = "CT-" + repositoryId;
            }
            insert.setString(1, repositoryId);
            insert.setBlob(2, blob);
            Logger.logDebug("About to execute insert in " + tableId + " table...");
            insert.execute();
            Logger.logDebug("Attachment row inserted successfully in " + tableId + " table.");
            attachment.setRepositoryId(repositoryId);
        }
        catch (Exception e) {
            throw new SapphireException("DB_INSERT_FAILED", "Failed to insert in " + tableId + " table. " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        finally {
            if (insert != null) {
                try {
                    insert.close();
                }
                catch (SQLException sQLException) {}
            }
        }
    }

    private void getAttachmentInputStream(sapphire.attachment.Attachment attachment, String tableId, String idColumn, String blobColumn) throws SapphireException {
        DBUtil db = new DBUtil(this.getConnectionId());
        db.setConnection(this.getSapphireConnection());
        db.createPreparedResultSet("getattachment", "select * from " + tableId + " where " + idColumn + " = ?", attachment.getRepositoryId());
        if (db.getNext("getattachment")) {
            try {
                attachment.setInputStream(db.getBlob("getattachment", blobColumn).getBinaryStream());
            }
            catch (SQLException e) {
                Logger.logError("Could not obtain blob from table " + tableId, e);
            }
        } else {
            HashMap<String, String> token = new HashMap<String, String>();
            token.put("table", tableId);
            token.put("repositoryid", attachment.getRepositoryId());
            throw new SapphireException(this.getTranslationProcessor().translate("Using attachment [repositoryid], no row found in the table [table].", token));
        }
    }

    private void deleteFiles(sapphire.attachment.Attachment attachment, String tableId, String idColumn, boolean check, boolean checkAudit) {
        String delSql = "Delete from " + tableId + " where " + idColumn + " = ?";
        if (attachment.getFilename() != null && attachment.getFilename().length() > 0) {
            try {
                if (check) {
                    if (!this.isFilePresentInOtherAttachments(attachment, true, checkAudit)) {
                        int deleted = this.getQueryProcessor().execPreparedUpdate(delSql, new String[]{attachment.getRepositoryId()});
                        if (deleted != 1) {
                            this.logger.warn("Failed to delete file " + attachment.getFilename());
                        }
                    } else {
                        this.logger.debug("Deletion skipped due to file being referenced elsewhere");
                    }
                } else {
                    int deleted = this.getQueryProcessor().execPreparedUpdate(delSql, new String[]{attachment.getRepositoryId()});
                    if (deleted != 1) {
                        this.logger.warn("Failed to delete file " + attachment.getFilename());
                    }
                }
            }
            catch (Exception e) {
                this.logger.error("Failed to delete file " + attachment.getFilename(), e);
            }
        }
    }

    PropertyList getCustomTableInfo() throws SapphireException {
        PropertyList customTableInfo = new PropertyList();
        String tableId = this.getRepositoryProperties().getProperty("tableid");
        String idColumn = this.getRepositoryProperties().getProperty("idcolumn");
        String storageColumn = this.getRepositoryProperties().getProperty("storagecolumn");
        if (tableId.length() == 0 || idColumn.length() == 0 || storageColumn.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("In  " + (Object)((Object)Mode.CUSTOMTABLE) + " mode, Table ID, ID Column and Storage Column need to be specified"));
        }
        customTableInfo.setProperty("tableid", tableId);
        customTableInfo.setProperty("idcolumn", idColumn);
        customTableInfo.setProperty("storagecolumn", storageColumn);
        return customTableInfo;
    }

    public static enum Mode {
        ATTACHMENTTABLE("Attachment Table"),
        ATTACHMENTSTORE("Attachment Store"),
        CUSTOMTABLE("Custom Table");

        private String title = "";

        private Mode(String title) {
            this.title = title;
        }

        public static Mode getMode(String title) {
            for (Mode m : Mode.values()) {
                if (!m.title.equalsIgnoreCase(title)) continue;
                return m;
            }
            return ATTACHMENTTABLE;
        }
    }
}

