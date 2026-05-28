/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.util.LabVantageClassLoader;
import com.labvantage.sapphire.util.file.FileType;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.attachment.Attachment;

public class LV_AttachmentHandler
extends BaseSDCRules
implements SDMSConstants {
    @Override
    public boolean requiresBeforeEditImage() {
        return false;
    }

    @Override
    public void postDeleteSDIAttachment(Attachment attachment) throws SapphireException {
        FileType f;
        if (attachment.getAttachmentType() == Attachment.AttachmentType.FILE && attachment.getSourceFilename() != null && attachment.getSourceFilename().length() > 0 && (f = FileType.getFileTypeByFileName(attachment.getSourceFilename(), this.getConnectionId())).getName().equals("JAR")) {
            LabVantageClassLoader.reset(LabVantageClassLoader.ClassLoaderType.ATTACHMENTHANDLER, this.getConnectionInfo().getDatabaseId());
        }
    }

    @Override
    public void postEditSDIAttachment(Attachment attachment) throws SapphireException {
        FileType f;
        if (attachment.getAttachmentType() == Attachment.AttachmentType.FILE && attachment.getSourceFilename() != null && attachment.getSourceFilename().length() > 0 && (f = FileType.getFileTypeByFileName(attachment.getSourceFilename(), this.getConnectionId())).getName().equals("JAR")) {
            LabVantageClassLoader.reset(LabVantageClassLoader.ClassLoaderType.ATTACHMENTHANDLER, this.getConnectionInfo().getDatabaseId());
        }
    }

    @Override
    public void postAddSDIAttachment(Attachment attachment) throws SapphireException {
        FileType f;
        if (attachment.getAttachmentType() == Attachment.AttachmentType.FILE && attachment.getSourceFilename() != null && attachment.getSourceFilename().length() > 0 && (f = FileType.getFileTypeByFileName(attachment.getSourceFilename(), this.getConnectionId())).getName().equals("JAR")) {
            LabVantageClassLoader.reset(LabVantageClassLoader.ClassLoaderType.ATTACHMENTHANDLER, this.getConnectionInfo().getDatabaseId());
        }
    }
}

