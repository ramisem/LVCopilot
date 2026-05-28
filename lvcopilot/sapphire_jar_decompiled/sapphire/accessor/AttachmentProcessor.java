/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package sapphire.accessor;

import com.labvantage.sapphire.BaseAccessor;
import com.labvantage.sapphire.Trace;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.attachment.Attachment;

public class AttachmentProcessor
extends BaseAccessor {
    public AttachmentProcessor(String connectionid) {
        super(connectionid);
    }

    public AttachmentProcessor(String nameserverlist, String connectionid) {
        super(connectionid);
    }

    public AttachmentProcessor(File rakFile, String connectionid) {
        super(rakFile, connectionid);
    }

    public AttachmentProcessor(PageContext pageContext) {
        super(pageContext);
    }

    public Attachment getSDIAttachment(Attachment attachment, Attachment.ThumbnailGeneration thumbnailGeneration) {
        Trace.logInfo(this.getClass().getName(), "getSDIAttachment(1) called...");
        try {
            if (local) {
                return this.getLocalAccessManager().getSDIAttachment(this.getConnectionid(), attachment, thumbnailGeneration);
            }
            return this.getRemoteAccessManager().getSDIAttachment(this.getConnectionid(), attachment, thumbnailGeneration);
        }
        catch (Exception e) {
            Trace.log("ERROR", "Exception: " + e.getMessage());
            this.setError(e.getMessage(), e);
            return null;
        }
    }

    public Attachment getSDIAttachment(Attachment attachment, int auditLog, Attachment.ThumbnailGeneration thumbnailGeneration) {
        Trace.logInfo(this.getClass().getName(), "getSDIAttachment(2) called...");
        try {
            if (local) {
                return this.getLocalAccessManager().getSDIAttachment(this.getConnectionid(), attachment, auditLog, thumbnailGeneration);
            }
            return this.getRemoteAccessManager().getSDIAttachment(this.getConnectionid(), attachment, auditLog, thumbnailGeneration);
        }
        catch (Exception e) {
            Trace.log("ERROR", "Exception: " + e.getMessage());
            this.setError(e.getMessage(), e);
            return null;
        }
    }

    public Attachment getTempAttachment(Attachment attachment, Attachment.ThumbnailGeneration thumbnailGeneration) {
        Trace.logInfo(this.getClass().getName(), "getTempAttachment called...");
        try {
            if (local) {
                return this.getLocalAccessManager().getTempAttachment(this.getConnectionid(), attachment, thumbnailGeneration);
            }
            return this.getRemoteAccessManager().getTempAttachment(this.getConnectionid(), attachment, thumbnailGeneration);
        }
        catch (Exception e) {
            Trace.log("ERROR", "Exception: " + e.getMessage());
            this.setError(e.getMessage(), e);
            return null;
        }
    }

    public int deleteSDIAttachment(Attachment attachment) {
        return this.deleteSDIAttachment(attachment, false, null);
    }

    public int deleteSDIAttachment(Attachment attachment, boolean applyLock, String attachmentPolicyNode) {
        int rc = 1;
        Trace.logInfo(this.getClass().getName(), "deleteSDIAttachment called...");
        try {
            if (local) {
                this.getLocalAccessManager().deleteSDIAttachment(this.getConnectionid(), attachment, applyLock, attachmentPolicyNode);
            } else {
                this.getRemoteAccessManager().deleteSDIAttachment(this.getConnectionid(), attachment, applyLock, attachmentPolicyNode);
            }
        }
        catch (Exception e) {
            Trace.log("ERROR", "Exception: " + e.getMessage());
            rc = 2;
            this.setError(e.getMessage(), e);
        }
        return rc;
    }

    public Attachment addSDIAttachment(Attachment attachment) {
        return this.addSDIAttachment(attachment, false, false, null);
    }

    public Attachment addSDIAttachment(Attachment attachment, boolean applyLock, boolean index, String attachmentPolicyNode) {
        int rc = 1;
        Trace.logInfo(this.getClass().getName(), "addSDIAttachment(1) called...");
        try {
            Attachment a = local ? this.getLocalAccessManager().addSDIAttachment(this.getConnectionid(), attachment, applyLock, index, attachmentPolicyNode) : this.getRemoteAccessManager().addSDIAttachment(this.getConnectionid(), attachment, applyLock, index, attachmentPolicyNode);
            return a;
        }
        catch (Exception e) {
            Trace.log("ERROR", "Exception: " + e.getMessage());
            rc = 2;
            this.setError(e.getMessage(), e);
            return null;
        }
    }

    public int addSDIAttachment(HashMap properties, InputStream data) {
        int rc = 1;
        Trace.logInfo(this.getClass().getName(), "addSDIAttachment(3) called...");
        try {
            if (local) {
                this.getLocalAccessManager().addSDIAttachment(this.getConnectionid(), properties, this.getByteArray(data));
            } else {
                this.getRemoteAccessManager().addSDIAttachment(this.getConnectionid(), properties, this.getByteArray(data));
            }
        }
        catch (Exception e) {
            Trace.log("ERROR", "Exception: " + e.getMessage());
            rc = 2;
            this.setError(e.getMessage(), e);
        }
        return rc;
    }

    public int addSDIAttachment(HashMap properties, byte[] data) {
        int rc = 1;
        Trace.logInfo(this.getClass().getName(), "addSDIAttachment(2) called...");
        try {
            if (local) {
                this.getLocalAccessManager().addSDIAttachment(this.getConnectionid(), properties, data);
            } else {
                this.getRemoteAccessManager().addSDIAttachment(this.getConnectionid(), properties, data);
            }
        }
        catch (Exception e) {
            Trace.log("ERROR", "Exception: " + e.getMessage());
            rc = 2;
            this.setError(e.getMessage(), e);
        }
        return rc;
    }

    public Attachment editSDIAttachment(Attachment attachment) {
        return this.editSDIAttachment(attachment, false, false, null);
    }

    public Attachment editSDIAttachment(Attachment attachment, boolean applyLock, boolean index, String attachmentPolicyNode) {
        int rc = 1;
        Trace.logInfo(this.getClass().getName(), "addSDIAttachment(1) called...");
        try {
            if (local) {
                return this.getLocalAccessManager().editSDIAttachment(this.getConnectionid(), attachment, applyLock, index, attachmentPolicyNode);
            }
            return this.getRemoteAccessManager().editSDIAttachment(this.getConnectionid(), attachment, applyLock, index, attachmentPolicyNode);
        }
        catch (Exception e) {
            Trace.log("ERROR", "Exception: " + e.getMessage());
            rc = 2;
            this.setError(e.getMessage(), e);
            return null;
        }
    }

    public int editSDIAttachment(HashMap properties, byte[] data) {
        int rc = 1;
        Trace.logInfo(this.getClass().getName(), "editSDIAttachment(2) called...");
        try {
            if (local) {
                this.getLocalAccessManager().editSDIAttachment(this.getConnectionid(), properties, data);
            } else {
                this.getRemoteAccessManager().editSDIAttachment(this.getConnectionid(), properties, data);
            }
        }
        catch (Exception e) {
            Trace.log("ERROR", "Exception: " + e.getMessage());
            rc = 2;
            this.setError(e.getMessage(), e);
        }
        return rc;
    }

    public int editSDIAttachment(HashMap properties, InputStream data) {
        int rc = 1;
        Trace.logInfo(this.getClass().getName(), "editSDIAttachment(3) called...");
        try {
            if (local) {
                this.getLocalAccessManager().editSDIAttachment(this.getConnectionid(), properties, this.getByteArray(data));
            } else {
                this.getRemoteAccessManager().editSDIAttachment(this.getConnectionid(), properties, this.getByteArray(data));
            }
        }
        catch (Exception e) {
            Trace.log("ERROR", "Exception: " + e.getMessage());
            rc = 2;
            this.setError(e.getMessage(), e);
        }
        return rc;
    }

    private byte[] getByteArray(InputStream datastream) throws IOException {
        int lengthread;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] bytebuff = new byte[500];
        while ((lengthread = datastream.read(bytebuff)) != -1) {
            output.write(bytebuff, 0, lengthread);
        }
        return output.toByteArray();
    }
}

