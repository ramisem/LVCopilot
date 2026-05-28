/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.system;

import com.labvantage.sapphire.services.Attachment;
import java.nio.file.Path;
import sapphire.attachment.Attachment;
import sapphire.util.Logger;

public class AttachmentProcessor
extends sapphire.accessor.AttachmentProcessor {
    public AttachmentProcessor(String connectionid) {
        super(connectionid);
    }

    public Path getSDIAttachmentLocalFile(Attachment attachment, boolean explode) {
        try {
            return this.getAttachmentManager().getSDIAttachmentLocalFile(this.getConnectionid(), attachment, explode);
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
            return null;
        }
    }

    public Attachment getSDIAttachment(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentnum) {
        try {
            return (Attachment)this.getAttachmentManager().getSDIAttachment(this.getConnectionid(), sdcid, keyid1, keyid2, keyid3, attachmentnum);
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
            return null;
        }
    }

    public Attachment getSDIAttachment(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentnum, String thumbnailGeneration) {
        try {
            return (Attachment)this.getAttachmentManager().getSDIAttachment(this.getConnectionid(), sdcid, keyid1, keyid2, keyid3, attachmentnum, Attachment.ThumbnailGeneration.getThumbnailGeneration(thumbnailGeneration));
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
            return null;
        }
    }

    public Attachment getSDIAttachment(String sdcid, String keyid1, String keyid2, String keyid3, String attachmentclass) {
        try {
            return (Attachment)this.getAttachmentManager().getSDIAttachment(this.getConnectionid(), sdcid, keyid1, keyid2, keyid3, attachmentclass);
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
            return null;
        }
    }

    public Attachment getSDIAttachment(String sdcid, String keyid1, String keyid2, String keyid3, String attachmentclass, String thumbnailGeneration) {
        try {
            return (Attachment)this.getAttachmentManager().getSDIAttachment(this.getConnectionid(), sdcid, keyid1, keyid2, keyid3, attachmentclass, Attachment.ThumbnailGeneration.getThumbnailGeneration(thumbnailGeneration));
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
            return null;
        }
    }

    public Attachment getTempAttachment(String sdcid, String keyid1, String keyid2, String keyid3, String tempid) {
        try {
            return (Attachment)this.getAttachmentManager().getTempAttachment(this.getConnectionid(), sdcid, keyid1, keyid2, keyid3, tempid);
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
            return null;
        }
    }

    public Attachment getTempAttachment(String sdcid, String keyid1, String keyid2, String keyid3, String tempid, String thumbnailGeneration) {
        try {
            return (Attachment)this.getAttachmentManager().getTempAttachment(this.getConnectionid(), sdcid, keyid1, keyid2, keyid3, tempid, Attachment.ThumbnailGeneration.getThumbnailGeneration(thumbnailGeneration));
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
            return null;
        }
    }

    public Attachment getSDIAttachment(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentnum, int auditsequence) {
        try {
            return (Attachment)this.getAttachmentManager().getSDIAttachment(this.getConnectionid(), sdcid, keyid1, keyid2, keyid3, attachmentnum, auditsequence);
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
            return null;
        }
    }

    public Attachment getSDIAttachment(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentnum, int auditsequence, String thumbnailGeneration) {
        try {
            return (Attachment)this.getAttachmentManager().getSDIAttachment(this.getConnectionid(), sdcid, keyid1, keyid2, keyid3, attachmentnum, auditsequence, Attachment.ThumbnailGeneration.getThumbnailGeneration(thumbnailGeneration));
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
            return null;
        }
    }
}

