/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.attachment;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.pageelements.attachment.AttachmentType_File;
import com.labvantage.sapphire.pageelements.attachment.BaseAttachmentType;
import com.labvantage.sapphire.pageelements.controls.HTMLEditorControl;
import com.labvantage.sapphire.pageelements.lookup.FileSystem;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.util.file.FileManager;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Blob;
import sapphire.SapphireException;
import sapphire.attachment.Attachment;
import sapphire.attachment.BaseAttachmentRepository;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.StringUtil;

public class StandardAttachmentRepository
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
        BaseAttachmentType type = Attachment.getAttachmentType(((Attachment)attachment).getType());
        try {
            type.processGetAttachment((Attachment)attachment, this.getConnectionId());
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    @Override
    public void preAddSDIAttachment(sapphire.attachment.Attachment attachment) throws SapphireException {
        this.processAddEdit(attachment);
        if (attachment.getType().equalsIgnoreCase("F") || attachment.getType().equalsIgnoreCase("S")) {
            if (attachment.getInputStream() != null) {
                try {
                    Blob blob = this.getSapphireConnection().getConnection().createBlob();
                    ByteBuffer buffer = ByteBuffer.allocateDirect(16384);
                    ReadableByteChannel inputChannel = Channels.newChannel(attachment.getInputStream());
                    WritableByteChannel outputChannel = Channels.newChannel(blob.setBinaryStream(1L));
                    while (inputChannel.read(buffer) != -1) {
                        buffer.flip();
                        outputChannel.write(buffer);
                        buffer.compact();
                    }
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        outputChannel.write(buffer);
                    }
                    attachment.setBlob(blob);
                }
                catch (Exception e) {
                    throw new SapphireException(e);
                }
            } else {
                File file = new File(attachment.getFilename());
                if (!file.exists()) {
                    throw new SapphireException("INVALID_PROPERTY", "The file '" + attachment.getFilename() + "' cannot be found.");
                }
                if (!file.canRead()) {
                    throw new SapphireException("INVALID_PROPERTY", "The file '" + attachment.getFilename() + "' cannot be read.");
                }
                try {
                    Blob blob = this.getSapphireConnection().getConnection().createBlob();
                    Files.copy(file.toPath(), blob.setBinaryStream(1L));
                }
                catch (Exception e) {
                    throw new SapphireException(e);
                }
            }
        } else {
            attachment.setBlob(null);
        }
    }

    @Override
    public void postAddSDIAttachment(sapphire.attachment.Attachment attachment) throws SapphireException {
        try {
            Attachment att = (Attachment)attachment;
            if (!attachment.getType().equalsIgnoreCase("F") && !attachment.getType().equalsIgnoreCase("S") && att.getUploadTo() != null && att.getUploadTo().length() > 0) {
                File file = new File(att.getUploadTo());
                file.getParentFile().mkdirs();
                this.logger.debug("About to write attachment file to '" + att.getUploadTo() + "'....");
                Files.copy(attachment.getInputStream(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    private boolean isContentChanged(sapphire.attachment.Attachment newAttachment, sapphire.attachment.Attachment oldAttachment, boolean updateFilename, boolean isUpdateBlob) {
        boolean isContentChanged = false;
        Attachment.AttachmentType attachmentType = oldAttachment.getAttachmentType();
        String type = newAttachment.getType();
        if (type == null || type.length() == 0) {
            type = oldAttachment.getType();
        }
        if (attachmentType == Attachment.AttachmentType.FILE) {
            String f2;
            String f1 = oldAttachment.getFilename() != null ? oldAttachment.getFilename() : "";
            String string = f2 = newAttachment.getFilename() != null ? newAttachment.getFilename() : "";
            if (!f1.equalsIgnoreCase(f2) && !f2.equals("(unknown)")) {
                isContentChanged = true;
            } else {
                this.logger.debug("Content not changed");
            }
        } else if (attachmentType == Attachment.AttachmentType.PLAINTEXT) {
            if (!oldAttachment.getClob().equals(newAttachment.getClob())) {
                isContentChanged = true;
            }
        } else if (attachmentType == Attachment.AttachmentType.RICHTEXT) {
            if (!oldAttachment.getClob().equals(newAttachment.getClob())) {
                isContentChanged = true;
            }
        } else if (attachmentType == Attachment.AttachmentType.URL) {
            String newUrl;
            String oldUrl = oldAttachment.getUrl();
            if (oldUrl == null || oldUrl.length() == 0) {
                oldUrl = oldAttachment.getClob();
            }
            if ((newUrl = newAttachment.getUrl()) == null || newUrl.length() == 0) {
                newUrl = newAttachment.getClob();
            }
            if (!newUrl.equals(oldUrl)) {
                isContentChanged = true;
            }
        } else if (attachmentType == Attachment.AttachmentType.LINKEDREFERENCE && !(oldAttachment.getLinkSdcid() + ";" + oldAttachment.getLinkKeyId1() + ";" + oldAttachment.getLinkKeyId2() + ";" + oldAttachment.getLinkKeyId3()).equals(newAttachment.getLinkSdcid() + ";" + newAttachment.getLinkKeyId1() + ";" + newAttachment.getLinkKeyId2() + ";" + newAttachment.getLinkKeyId3())) {
            isContentChanged = true;
        }
        return isContentChanged;
    }

    @Override
    public boolean preEditSDIAttachment(sapphire.attachment.Attachment attachment, sapphire.attachment.Attachment oldAttachment) throws SapphireException {
        this.processAddEdit(attachment);
        boolean updateFilename = attachment.getFilename() != null && attachment.getFilename().length() > 0 && !attachment.getFilename().equalsIgnoreCase("(unknown)");
        boolean isUpdateBlob = this.getAttachmentPolicy().getProperty("byreference").equalsIgnoreCase("N");
        boolean isContentChanged = this.isContentChanged(attachment, oldAttachment, updateFilename, isUpdateBlob);
        if (isUpdateBlob) {
            if (attachment.getInputStream() != null) {
                try {
                    Blob blob = this.getSapphireConnection().getConnection().createBlob();
                    ByteBuffer buffer = ByteBuffer.allocateDirect(16384);
                    ReadableByteChannel inputChannel = Channels.newChannel(attachment.getInputStream());
                    WritableByteChannel outputChannel = Channels.newChannel(blob.setBinaryStream(1L));
                    while (inputChannel.read(buffer) != -1) {
                        buffer.flip();
                        outputChannel.write(buffer);
                        buffer.compact();
                    }
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        outputChannel.write(buffer);
                    }
                    attachment.setBlob(blob);
                }
                catch (Exception e) {
                    throw new SapphireException(e);
                }
            } else {
                File file = new File(attachment.getFilename());
                if (!file.exists()) {
                    throw new SapphireException("INVALID_PROPERTY", "The file '" + attachment.getFilename() + "' cannot be found.");
                }
                if (!file.canRead()) {
                    throw new SapphireException("INVALID_PROPERTY", "The file '" + attachment.getFilename() + "' cannot be read.");
                }
                try {
                    Blob blob = this.getSapphireConnection().getConnection().createBlob();
                    Files.copy(file.toPath(), blob.setBinaryStream(1L));
                }
                catch (Exception e) {
                    throw new SapphireException(e);
                }
            }
        } else {
            attachment.setBlob(null);
        }
        return isContentChanged;
    }

    private void processAddEdit(sapphire.attachment.Attachment att) throws SapphireException {
        Attachment attachment = (Attachment)att;
        boolean availableData = false;
        if (attachment.getAttachmentType() == Attachment.AttachmentType.FILE) {
            FileManager.StorageMode storageMode;
            if (attachment.getDescription() == null || attachment.getDescription().length() == 0) {
                if (attachment.getSourceFilename() != null && attachment.getSourceFilename().length() > 0) {
                    attachment.setDescription(attachment.getSourceFilename());
                } else if (attachment.getFilename() != null && attachment.getFilename().length() > 0) {
                    attachment.setDescription(attachment.getFilename());
                }
            }
            try {
                availableData = attachment.getInputStream() != null && attachment.getInputStream().available() > 0;
            }
            catch (Exception exception) {
                // empty catch block
            }
            FileManager.StorageMode storageMode2 = storageMode = this.getAttachmentPolicy() != null ? FileManager.StorageMode.getByValue(this.getAttachmentPolicy().getProperty("byreference", "Y")) : FileManager.StorageMode.FILESYSTEM;
            if (storageMode == FileManager.StorageMode.FILESYSTEM) {
                boolean upload = availableData;
                if (upload) {
                    attachment.setType("U");
                    String[] uploadto = StringUtil.split(attachment.getUploadTo(), ";");
                    String locationpolicynode = uploadto.length > 0 ? uploadto[0] : "Upload Custom";
                    String locationpolicyitem = uploadto.length > 1 ? uploadto[1] : "";
                    String filename = attachment.getFilename();
                    filename = AttachmentType_File.renameFileFromPolicy(filename, attachment.getKeyId2() != null && attachment.getKeyId2().length() > 0 && !attachment.getKeyId2().equalsIgnoreCase("(null)"), attachment.getKeyId3() != null && attachment.getKeyId3().length() > 0 && !attachment.getKeyId3().equalsIgnoreCase("(null)"), this.getAttachmentPolicy());
                    String uploadtopath = StringUtil.replaceAll(FileManager.getFileLocation(locationpolicynode, locationpolicyitem, this.getConnectionId()), "\\", "/");
                    if (!FileSystem.validateFileName(uploadtopath)) {
                        throw new SapphireException("Could not validate file name " + uploadtopath + ".");
                    }
                    try {
                        uploadtopath = FileUtil.resolvePath(uploadtopath, filename);
                        attachment.setUploadTo(uploadtopath);
                        attachment.setFilename(uploadtopath);
                        attachment.setUploadTo(Attachment.evaluateFileNameExpressions(attachment.getSDCId(), attachment.getKeyId1(), attachment.getKeyId2(), attachment.getKeyId3(), attachment.getAttachmentNum(), 1, this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()), attachment.getUploadTo(), attachment));
                        attachment.setFilename(Attachment.evaluateFileNameExpressions(attachment.getSDCId(), attachment.getKeyId1(), attachment.getKeyId2(), attachment.getKeyId3(), attachment.getAttachmentNum(), 1, this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()), attachment.getFilename(), attachment));
                    }
                    catch (IOException e) {
                        throw new SapphireException("Failed to save attachment due to path corruption. Error - " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
                    }
                } else {
                    attachment.setType("R");
                }
            } else {
                attachment.setType("S");
            }
        } else if (attachment.getClob() != null && attachment.getClob().length() > 0 && attachment.getAttachmentType() == Attachment.AttachmentType.RICHTEXT) {
            String clob = attachment.getClob();
            clob = HttpUtil.decodeURIComponent(clob);
            StringBuffer contents = new StringBuffer(clob);
            boolean bodyonly = !clob.startsWith("<html><body>");
            HTMLEditorControl.processImages(contents, true, this.getConnectionId());
            clob = bodyonly ? StringUtil.replaceAll(HTMLEditorControl.getBody(contents.toString()), ";", "#semicolon#") : StringUtil.replaceAll(contents.toString(), ";", "#semicolon#");
            attachment.setClob(clob);
        } else if ((attachment.getClob() != null && attachment.getClob().length() > 0 || attachment.getUrl() != null && attachment.getUrl().length() > 0) && attachment.getAttachmentType() == Attachment.AttachmentType.URL) {
            String url = attachment.getUrl();
            if (url == null || url.length() == 0) {
                url = attachment.getClob();
            }
            attachment.setUrl(url);
        }
    }

    private void deleteFiles(sapphire.attachment.Attachment attachment) {
        boolean systemcontrolled = this.getAttachmentPolicy() != null && this.getAttachmentPolicy().getPropertyList("filereference") != null && this.getAttachmentPolicy().getPropertyList("filereference").getPropertyList("renameonupload") != null && this.getAttachmentPolicy().getPropertyList("filereference").getPropertyList("renameonupload").getProperty("rename").equalsIgnoreCase("S") && this.getAttachmentPolicy().getProperty("byreference", "Y").equalsIgnoreCase("Y");
        FileManager.StorageMode storageMode = this.getAttachmentPolicy() != null ? FileManager.StorageMode.getByValue(this.getAttachmentPolicy().getProperty("byreference", "Y")) : FileManager.StorageMode.FILESYSTEM;
        Path path = null;
        if (storageMode == FileManager.StorageMode.FILESYSTEM) {
            // empty if block
        }
        Object cp = null;
        String t = attachment.getType();
        Attachment.AttachmentType type = Attachment.AttachmentType.getTypeByAttachmentTypeFlag(t);
        if (type == Attachment.AttachmentType.FILE && attachment.getFilename() != null && attachment.getFilename().length() > 0 && storageMode == FileManager.StorageMode.FILESYSTEM && systemcontrolled) {
            if (path == null) {
                String s = FileManager.getFileLocation(this.getAttachmentPolicy().getProperty("locationpolicynode", "Upload Custom"), this.getAttachmentPolicy().getProperty("locationpolicyitem", this.getConnectionId()));
                path = Paths.get(s, new String[0]);
            }
            if (path != null) {
                try {
                    File file = new File(attachment.getFilename());
                    if (file.exists() && file.toPath().startsWith(path) && !file.delete()) {
                        this.logger.warn("Failed to delete file " + attachment.getFilename());
                    }
                }
                catch (Exception e) {
                    this.logger.error("Failed to delete file " + attachment.getFilename(), e);
                }
            }
        }
    }

    @Override
    public void postEditSDIAttachment(sapphire.attachment.Attachment attachment, sapphire.attachment.Attachment oldAttachment) throws SapphireException {
        if (this.getAttachmentPolicy().getProperty("byreference").equalsIgnoreCase("Y") && attachment.getInputStream() != null) {
            String filePath = "";
            if (attachment.getType().equalsIgnoreCase("R")) {
                if (oldAttachment.getFilename().length() > 0) {
                    filePath = oldAttachment.getFilename();
                    this.logger.debug("About to update reference file with data...");
                }
            } else if (attachment.getType().equalsIgnoreCase("U")) {
                filePath = attachment.getFilename().equalsIgnoreCase("(unknown)") ? oldAttachment.getFilename() : attachment.getFilename();
                this.logger.debug("About to update upload&reference file with data...");
            } else {
                filePath = oldAttachment.getFilename();
            }
            if (filePath.length() > 0) {
                File file = new File(filePath);
                if (file.exists()) {
                    this.logger.debug("File exists.");
                }
                try {
                    Files.copy(attachment.getInputStream(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                catch (Exception e) {
                    throw new SapphireException(e);
                }
            }
        }
    }

    @Override
    public void preDeleteSDIAttachment(sapphire.attachment.Attachment attachment) throws SapphireException {
    }

    @Override
    public void postDeleteSDIAttachment(sapphire.attachment.Attachment oldAttachment) throws SapphireException {
        this.deleteFiles(oldAttachment);
        boolean systemcontrolled = this.getAttachmentPolicy() != null && this.getAttachmentPolicy().getPropertyList("filereference") != null && this.getAttachmentPolicy().getPropertyList("filereference").getPropertyList("renameonupload") != null && this.getAttachmentPolicy().getPropertyList("filereference").getPropertyList("renameonupload").getProperty("rename").equalsIgnoreCase("S") && this.getAttachmentPolicy().getProperty("byreference", "Y").equalsIgnoreCase("Y");
        DataSet tempDs = this.getQueryProcessor().getPreparedSqlDataSet("auditattachment", "SELECT * FROM a_sdiattachment WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND attachmentnum = ?", new Object[]{oldAttachment.getSDCId(), oldAttachment.getKeyId1(), oldAttachment.getKeyId2(), oldAttachment.getKeyId3(), oldAttachment.getAttachmentNum()}, true);
        if (tempDs != null && tempDs.getRowCount() > 0) {
            for (int i = 0; i < tempDs.getRowCount(); ++i) {
                Attachment audit = Attachment.getAttachment(tempDs, oldAttachment.getSDCId(), oldAttachment.getKeyId1(), oldAttachment.getKeyId2(), oldAttachment.getKeyId3(), oldAttachment.getAttachmentNum(), this.getConnectionId());
                this.deleteFiles(audit);
            }
        }
    }

    @Override
    public void cleanUpRepository(sapphire.attachment.Attachment oldAttachment) throws SapphireException {
    }
}

