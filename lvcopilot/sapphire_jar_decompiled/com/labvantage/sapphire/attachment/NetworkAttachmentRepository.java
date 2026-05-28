/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.attachment;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.util.file.FileManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import sapphire.SapphireException;
import sapphire.attachment.Attachment;
import sapphire.attachment.BaseAttachmentRepository;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class NetworkAttachmentRepository
extends BaseAttachmentRepository {
    private static final String DEFAULT_PATH = "[sapphirehome]/attachments";

    @Override
    public boolean enableCaching() {
        return true;
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

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void getSDIAttachment(sapphire.attachment.Attachment attachment, Attachment.ThumbnailGeneration thumbnailGeneration) throws SapphireException {
        Attachment att = (Attachment)attachment;
        if (att.getFilename() == null || att.getFilename().length() <= 0) throw new SapphireException("No filename found.");
        File f = new File(att.getFilename());
        if (!f.exists() && att.getRepositoryId() != null && att.getRepositoryId().length() > 0) {
            try {
                this.setUploadPath(att, att.getRepositoryId(), 1);
                f = new File(att.getFilename());
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (f == null || !f.exists()) throw new SapphireException("File does not exsit. " + attachment.getFilename());
        att.setSize(f.length());
        try {
            att.setInputStream(new FileInputStream(att.getFilename()));
            return;
        }
        catch (Exception e) {
            throw new SapphireException("Could not read file.", e);
        }
    }

    @Override
    public void preAddSDIAttachment(sapphire.attachment.Attachment attachment) throws SapphireException {
        if ((attachment.getSourceFilename() == null || attachment.getSourceFilename().length() == 0) && attachment.getFilename() != null && attachment.getFilename().length() > 0) {
            attachment.setSourceFilename(attachment.getFilename());
        }
        this.processAddEdit(attachment, 1);
        attachment.setBlob(null);
    }

    @Override
    public void postAddSDIAttachment(sapphire.attachment.Attachment attachment) throws SapphireException {
        try {
            Attachment att = (Attachment)attachment;
            if (att.getUploadTo() != null && att.getUploadTo().length() > 0) {
                File file = new File(att.getUploadTo());
                file.getParentFile().mkdirs();
                this.logger.debug("About to write attachment file to '" + att.getUploadTo() + "'....");
                if (att.hasData()) {
                    this.transferData(attachment.getInputStream(), file, false, false);
                } else if (att.getInputStream() != null) {
                    Files.createFile(file.toPath(), new FileAttribute[0]);
                }
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    @Override
    public String getBrowseScript(String attachmentElementId) {
        return "fileElement.browse('" + attachmentElementId + "', '" + this.getRepositoryProperties().getPropertyListNotNull("serverside").getProperty("locationpolicynode", "Sapphire Custom") + "')";
    }

    @Override
    public String getBrowseButtonText() {
        return "Browse Network";
    }

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
        return !this.isManaged() && this.getRepositoryProperties().getPropertyListNotNull("serverside").getProperty("allow", "N").equalsIgnoreCase("Y");
    }

    @Override
    public String[] getBrowseIncludes() {
        return null;
    }

    private boolean isContentChanged(sapphire.attachment.Attachment newAttachment, sapphire.attachment.Attachment oldAttachment, boolean updateFilename) {
        String f2;
        boolean isContentChanged = false;
        String type = newAttachment.getType();
        if (type == null || type.length() == 0) {
            type = oldAttachment.getType();
        }
        String f1 = oldAttachment.getFilename() != null ? oldAttachment.getFilename() : "";
        String string = f2 = newAttachment.getFilename() != null ? newAttachment.getFilename() : "";
        if (!f1.equalsIgnoreCase(f2) && !f2.equals("(unknown)")) {
            isContentChanged = true;
        } else {
            this.logger.debug("Content not changed");
        }
        return isContentChanged;
    }

    @Override
    public boolean preEditSDIAttachment(sapphire.attachment.Attachment attachment, sapphire.attachment.Attachment oldAttachment) throws SapphireException {
        int auditsequence = 1;
        if (oldAttachment != null && oldAttachment.getAdditionalColumn("auditsequence", "").length() > 0) {
            auditsequence = Integer.parseInt(oldAttachment.getAdditionalColumn("auditsequence", "1"));
            ++auditsequence;
        }
        if ((attachment.getSourceFilename() == null || attachment.getSourceFilename().length() == 0) && oldAttachment.getSourceFilename() != null && oldAttachment.getSourceFilename().length() > 0) {
            attachment.setSourceFilename(oldAttachment.getSourceFilename());
        }
        this.processAddEdit(attachment, auditsequence);
        boolean updateFilename = attachment.getFilename() != null && attachment.getFilename().length() > 0 && !attachment.getFilename().equalsIgnoreCase("(unknown)") && !oldAttachment.getFilename().equals(attachment.getFilename());
        boolean isContentChanged = this.isContentChanged(attachment, oldAttachment, updateFilename);
        attachment.setBlob(null);
        return isContentChanged;
    }

    private void setUploadPath(Attachment attachment, String filename, int auditsequence) throws SapphireException {
        String repositorypath = StringUtil.replaceAll(this.getRepositoryProperties().getProperty("locationpath", DEFAULT_PATH), "\\", "/");
        repositorypath = FileManager.getFileLocation(Attachment.evaluateFileNameExpressions(attachment.getSDCId(), attachment.getKeyId1(), attachment.getKeyId2(), attachment.getKeyId3(), attachment.getAttachmentNum(), auditsequence, this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()), repositorypath, attachment));
        if (!FileUtil.validateFileName(repositorypath)) {
            throw new SapphireException("Could not validate file name " + repositorypath + ".");
        }
        try {
            String filepath = Attachment.evaluateFileNameExpressions(attachment.getSDCId(), attachment.getKeyId1(), attachment.getKeyId2(), attachment.getKeyId3(), attachment.getAttachmentNum(), auditsequence, this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()), filename, attachment);
            String uploadtopath = FileUtil.resolvePath(repositorypath, filepath);
            File f = new File(uploadtopath);
            int sequence = 0;
            String finalfilepath = filepath;
            while (f.exists() && sequence < 999) {
                finalfilepath = FileManager.getFileName(filepath, false) + "_" + String.format("%03d", ++sequence) + "." + FileManager.getExtension(filepath);
                uploadtopath = FileUtil.resolvePath(repositorypath, finalfilepath);
                f = new File(uploadtopath);
            }
            attachment.setUploadTo(uploadtopath);
            attachment.setFilename(uploadtopath);
            attachment.setRepositoryId(finalfilepath);
        }
        catch (IOException e) {
            throw new SapphireException("Failed to obtain attachment path corruption. Error - " + e.getMessage());
        }
    }

    private void processAddEdit(sapphire.attachment.Attachment att, int auditsequence) throws SapphireException {
        String filename;
        Attachment attachment = (Attachment)att;
        boolean availableData = false;
        String string = attachment.getFilename() == null ? (attachment.getSourceFilename() == null ? "" : FileManager.getFileName(attachment.getSourceFilename(), true)) : (filename = FileManager.getFileName(attachment.getFilename(), true));
        if (attachment.getDescription() == null || attachment.getDescription().length() == 0) {
            if (attachment.getSourceFilename() != null && attachment.getSourceFilename().length() > 0) {
                attachment.setDescription(FileManager.getFileName(attachment.getSourceFilename(), true));
            } else if (attachment.getFilename() != null && attachment.getFilename().length() > 0) {
                attachment.setDescription(filename);
            }
        }
        try {
            availableData = attachment.getInputStream() != null || attachment.hasData();
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (availableData) {
            boolean renameOnUpload = this.getRepositoryProperties().getPropertyListNotNull("renameonupload").getProperty("rename", "N").equalsIgnoreCase("Y");
            String renamepattern = renameOnUpload ? this.getRepositoryProperties().getPropertyListNotNull("renameonupload").getProperty("pattern", "") : "";
            this.logger.debug("NetworkAttachmentRepository - Rename Pattern: " + renamepattern);
            this.logger.debug("NetworkAttachmentRepository - pre-filename: " + filename);
            filename = NetworkAttachmentRepository.renameFileFromExpression(filename, attachment.getKeyId2() != null && attachment.getKeyId2().length() > 0 && !attachment.getKeyId2().equalsIgnoreCase("(null)"), attachment.getKeyId3() != null && attachment.getKeyId3().length() > 0 && !attachment.getKeyId3().equalsIgnoreCase("(null)"), this.isManaged(), renamepattern);
            this.logger.debug("NetworkAttachmentRepository - post-filename: " + filename);
            this.setUploadPath(attachment, filename, auditsequence);
        } else if (att.getFilename() != null && att.getFilename().length() > 0 && !att.getFilename().equalsIgnoreCase("(unknown)") && att.getSize() == 0L) {
            if (!this.isManaged()) {
                attachment.setType("R");
                att.setRepositoryId(att.getFilename());
            } else {
                Path filep = Paths.get(att.getFilename(), new String[0]);
                if (Files.exists(filep, new LinkOption[0])) {
                    try {
                        att.setInputStream(Files.newInputStream(filep, new OpenOption[0]));
                        availableData = attachment.hasData();
                    }
                    catch (Exception e) {
                        throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
                    }
                } else {
                    throw new SapphireException("No data provided for managed repository.");
                }
            }
        }
    }

    public static String renameFileFromExpression(String infilename, boolean useKeyId2, boolean useKeyId3, boolean managed, String renameString) {
        boolean renameOnUpload;
        String outfilename = infilename;
        boolean bl = renameOnUpload = managed || renameString.length() > 0;
        if (renameOnUpload) {
            if (managed) {
                outfilename = "[guid]." + FileManager.getExtension(infilename);
            } else if (renameString.length() > 0) {
                outfilename = StringUtil.replaceAll(renameString, "[filename]", FileManager.getFileName(infilename, false));
                if (outfilename.contains("[extension]")) {
                    outfilename = StringUtil.replaceAll(outfilename, "[extension]", FileManager.getExtension(infilename));
                } else {
                    String outFileNameExt = outfilename.lastIndexOf(".") > -1 ? outfilename.substring(outfilename.lastIndexOf(".") + 1) : "";
                    String fileNameExt = FileManager.getExtension(infilename);
                    outfilename = outfilename + (outfilename.lastIndexOf(".") > -1 ? (!fileNameExt.toLowerCase().startsWith(outFileNameExt.toLowerCase()) ? "." + FileManager.getExtension(infilename) : "") : "." + FileManager.getExtension(infilename));
                }
                outfilename = StringUtil.replaceAll(outfilename, "\\", "/");
            } else {
                outfilename = "[sdcid]_[keyid1]" + (useKeyId2 ? "_keyid2" + (useKeyId3 ? "_keyid3" : "") : "") + "_[attachmentnum]" + (outfilename.lastIndexOf(".") > -1 ? outfilename.substring(outfilename.lastIndexOf(".")) : "");
            }
        }
        return outfilename;
    }

    private void deleteFiles(sapphire.attachment.Attachment attachment, boolean check, boolean checkAudit) {
        if (attachment.getFilename() != null && attachment.getFilename().length() > 0 && this.isManaged()) {
            try {
                File file = new File(attachment.getFilename());
                if (file.exists()) {
                    if (check) {
                        if (!this.isFilePresentInOtherAttachments(attachment, true, checkAudit)) {
                            if (!file.delete()) {
                                this.logger.warn("Failed to delete file " + attachment.getFilename());
                            }
                        } else {
                            this.logger.debug("Deletion skipped due to file being referenced elsewhere");
                        }
                    } else if (!file.delete()) {
                        this.logger.warn("Failed to delete file " + attachment.getFilename());
                    }
                }
            }
            catch (Exception e) {
                this.logger.error("Failed to delete file " + attachment.getFilename(), e);
            }
        }
    }

    @Override
    public void postEditSDIAttachment(sapphire.attachment.Attachment attachment, sapphire.attachment.Attachment oldAttachment) throws SapphireException {
        if (attachment.hasData() && attachment.getInputStream() != null) {
            String filePath = "";
            filePath = attachment.getFilename().equalsIgnoreCase("(unknown)") ? oldAttachment.getFilename() : attachment.getFilename();
            this.logger.debug("About to update file with data...");
            if (filePath.length() > 0) {
                File file = new File(filePath);
                if (file.exists()) {
                    this.logger.debug("File exists.");
                }
                try {
                    this.logger.debug("NetworkAttachmentRepostiory - FileTransfer filepath = " + filePath);
                    this.transferData(attachment.getInputStream(), file, false, false);
                    this.logger.debug("NetworkAttachmentRepostiory - FileTransfer complete");
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
    }

    @Override
    public void cleanUpRepository(sapphire.attachment.Attachment oldAttachment) throws SapphireException {
        if (this.isManaged()) {
            this.deleteFiles(oldAttachment, true, true);
            DataSet tempDs = this.getUniqueFilesInAudit(oldAttachment);
            if (tempDs != null && tempDs.getRowCount() > 0) {
                for (int i = 0; i < tempDs.getRowCount(); ++i) {
                    Attachment audit = Attachment.getAttachment(tempDs, oldAttachment.getSDCId(), oldAttachment.getKeyId1(), oldAttachment.getKeyId2(), oldAttachment.getKeyId3(), oldAttachment.getAttachmentNum(), this.getConnectionId());
                    this.deleteFiles(audit, false, false);
                }
            }
        }
    }
}

