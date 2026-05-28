/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.ejb.EJBException
 *  javax.ejb.SessionBean
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.ejb.AttachmentManagement;
import com.labvantage.sapphire.ejb.BaseManager;
import com.labvantage.sapphire.ejb.ManagerException;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.services.AttachmentService;
import java.nio.file.Path;
import java.util.HashMap;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import sapphire.attachment.Attachment;

public class AttachmentManagerBean
extends BaseManager
implements SessionBean,
AttachmentManagement {
    public AttachmentManagerBean() {
        this.logName = "AttachmentManager";
    }

    @Override
    public HashMap addSDIAttachment(String connectionid, HashMap columns, byte[] data) throws ManagerException {
        String methodName = "addSDIAttachment";
        try {
            this.startMethod(methodName, connectionid);
            AttachmentService attachment = new AttachmentService(this.sapphireConnection);
            attachment.addSDIAttachment(columns, data);
            HashMap hashMap = columns;
            return hashMap;
        }
        catch (Exception e) {
            this.logError("Failed to add attachment", e);
            this.beforeTransactionAbort();
            throw new EJBException(e.getMessage(), e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public sapphire.attachment.Attachment addSDIAttachment(String connectionid, sapphire.attachment.Attachment attachmentObject, boolean applyLock, boolean index, String attachmentPolicyNode) throws ManagerException {
        String methodName = "addSDIAttachment";
        try {
            this.startMethod(methodName, connectionid);
            AttachmentService attachment = new AttachmentService(this.sapphireConnection);
            attachment.addSDIAttachment(attachmentObject, applyLock, index, attachmentPolicyNode);
            sapphire.attachment.Attachment attachment2 = attachmentObject;
            return attachment2;
        }
        catch (Exception e) {
            this.logError("Failed to add attachment", e);
            this.beforeTransactionAbort();
            throw new EJBException(e.getMessage(), e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public HashMap editSDIAttachment(String connectionid, HashMap columns, byte[] data) throws ManagerException {
        String methodName = "editSDIAttachment";
        try {
            this.startMethod(methodName, connectionid);
            AttachmentService attachment = new AttachmentService(this.sapphireConnection);
            attachment.editSDIAttachment(columns, data);
            HashMap hashMap = columns;
            return hashMap;
        }
        catch (Exception e) {
            this.logError("Failed to edit attachment", e);
            this.beforeTransactionAbort();
            throw new EJBException(e.getMessage(), e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public sapphire.attachment.Attachment editSDIAttachment(String connectionid, sapphire.attachment.Attachment attachmentObject, boolean applyLock, boolean index, String attachmentPolicyNode) throws ManagerException {
        String methodName = "editSDIAttachment";
        try {
            this.startMethod(methodName, connectionid);
            AttachmentService attachment = new AttachmentService(this.sapphireConnection);
            attachment.editSDIAttachment(attachmentObject, applyLock, index, attachmentPolicyNode);
            sapphire.attachment.Attachment attachment2 = attachmentObject;
            return attachment2;
        }
        catch (Exception e) {
            this.logError("Failed to edit attachment", e);
            this.beforeTransactionAbort();
            throw new EJBException(e.getMessage(), e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public void deleteSDIAttachment(String connectionid, String sdcid, String keyid1, String keyid2, String keyid3, String attachmentnum, boolean applylock) throws ManagerException {
        String methodName = "deleteSDIAttachment";
        try {
            this.startMethod(methodName, connectionid);
            AttachmentService attachment = new AttachmentService(this.sapphireConnection);
            attachment.deleteSDIAttachment(sdcid, keyid1, keyid2, keyid3, attachmentnum, applylock);
        }
        catch (Exception e) {
            this.logError("Failed to delete attachment", e);
            this.beforeTransactionAbort();
            throw new EJBException(e.getMessage(), e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public void deleteSDIAttachment(String connectionid, sapphire.attachment.Attachment attachmentObject, boolean applylock, String attachmentPolicyNode) throws ManagerException {
        String methodName = "deleteSDIAttachment";
        try {
            this.startMethod(methodName, connectionid);
            AttachmentService attachment = new AttachmentService(this.sapphireConnection);
            attachment.deleteSDIAttachment(attachmentObject, applylock, attachmentPolicyNode);
        }
        catch (Exception e) {
            this.logError("Failed to delete attachment", e);
            this.beforeTransactionAbort();
            throw new EJBException(e.getMessage(), e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public Path getSDIAttachmentLocalFile(String connectionid, sapphire.attachment.Attachment attachment, boolean explode) throws ManagerException {
        String methodName = "getExplodedSDIAttachment";
        try {
            Path p;
            this.startMethod(methodName, connectionid);
            AttachmentService attachmentService = new AttachmentService(this.sapphireConnection);
            Path path = p = attachmentService.getSDIAttachmentLocalFile(attachment, explode);
            return path;
        }
        catch (Exception e) {
            this.logError("Failed to get exploded attachment", e);
            this.beforeTransactionAbort();
            throw new EJBException(e.getMessage(), e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public sapphire.attachment.Attachment getSDIAttachment(String connectionid, sapphire.attachment.Attachment attachment, Attachment.ThumbnailGeneration thumbnailGeneration) throws ManagerException {
        String methodName = "getSDIAttachment";
        try {
            this.startMethod(methodName, connectionid);
            AttachmentService attachmentService = new AttachmentService(this.sapphireConnection);
            attachmentService.getSDIAttachment(attachment, thumbnailGeneration);
            sapphire.attachment.Attachment attachment2 = attachment;
            return attachment2;
        }
        catch (Exception e) {
            this.logError("Failed to get attachment", e);
            this.beforeTransactionAbort();
            throw new EJBException(e.getMessage(), e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public sapphire.attachment.Attachment getSDIAttachment(String connectionid, sapphire.attachment.Attachment attachment, int auditSequence, Attachment.ThumbnailGeneration thumbnailGeneration) throws ManagerException {
        String methodName = "getSDIAttachment";
        try {
            this.startMethod(methodName, connectionid);
            AttachmentService attachmentService = new AttachmentService(this.sapphireConnection);
            attachmentService.getSDIAttachment(attachment, auditSequence, thumbnailGeneration);
            sapphire.attachment.Attachment attachment2 = attachment;
            return attachment2;
        }
        catch (Exception e) {
            this.logError("Failed to get attachment", e);
            this.beforeTransactionAbort();
            throw new EJBException(e.getMessage(), e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public sapphire.attachment.Attachment getTempAttachment(String connectionid, sapphire.attachment.Attachment attachment, Attachment.ThumbnailGeneration thumbnailGeneration) throws ManagerException {
        String methodName = "getTempAttachment";
        try {
            this.startMethod(methodName, connectionid);
            AttachmentService attachmentService = new AttachmentService(this.sapphireConnection);
            attachmentService.getTempAttachment(attachment, thumbnailGeneration);
            sapphire.attachment.Attachment attachment2 = attachment;
            return attachment2;
        }
        catch (Exception e) {
            this.logError("Failed to get temp attachment", e);
            this.beforeTransactionAbort();
            throw new EJBException(e.getMessage(), e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public sapphire.attachment.Attachment getSDIAttachment(String connectionid, String sdcid, String keyid1, String keyid2, String keyid3, int attachmentnum) throws ManagerException {
        String methodName = "getSDIAttachment";
        try {
            this.startMethod(methodName, connectionid);
            AttachmentService attachment = new AttachmentService(this.sapphireConnection);
            Attachment attachment2 = attachment.getSDIAttachment(sdcid, keyid1, keyid2, keyid3, attachmentnum);
            return attachment2;
        }
        catch (Exception e) {
            this.logError("Failed to get attachment", e);
            this.beforeTransactionAbort();
            throw new EJBException(e.getMessage(), e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public sapphire.attachment.Attachment getSDIAttachment(String connectionid, String sdcid, String keyid1, String keyid2, String keyid3, int attachmentnum, Attachment.ThumbnailGeneration thumbnailGeneration) throws ManagerException {
        String methodName = "getSDIAttachment";
        try {
            this.startMethod(methodName, connectionid);
            AttachmentService attachment = new AttachmentService(this.sapphireConnection);
            Attachment attachment2 = attachment.getSDIAttachment(sdcid, keyid1, keyid2, keyid3, attachmentnum, thumbnailGeneration.toString());
            return attachment2;
        }
        catch (Exception e) {
            this.logError("Failed to get attachment", e);
            this.beforeTransactionAbort();
            throw new EJBException(e.getMessage(), e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public sapphire.attachment.Attachment getSDIAttachment(String connectionid, String sdcid, String keyid1, String keyid2, String keyid3, String attachmentclass) throws ManagerException {
        String methodName = "getSDIAttachmentByClass";
        try {
            this.startMethod(methodName, connectionid);
            AttachmentService attachment = new AttachmentService(this.sapphireConnection);
            Attachment attachment2 = attachment.getSDIAttachment(sdcid, keyid1, keyid2, keyid3, attachmentclass);
            return attachment2;
        }
        catch (Exception e) {
            this.logError("Failed to get attachment", e);
            this.beforeTransactionAbort();
            throw new EJBException(e.getMessage(), e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public sapphire.attachment.Attachment getSDIAttachment(String connectionid, String sdcid, String keyid1, String keyid2, String keyid3, String attachmentclass, Attachment.ThumbnailGeneration thumbnailGeneration) throws ManagerException {
        String methodName = "getSDIAttachmentByClass";
        try {
            this.startMethod(methodName, connectionid);
            AttachmentService attachment = new AttachmentService(this.sapphireConnection);
            Attachment attachment2 = attachment.getSDIAttachment(sdcid, keyid1, keyid2, keyid3, attachmentclass, thumbnailGeneration.toString());
            return attachment2;
        }
        catch (Exception e) {
            this.logError("Failed to get attachment", e);
            this.beforeTransactionAbort();
            throw new EJBException(e.getMessage(), e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public sapphire.attachment.Attachment getTempAttachment(String connectionid, String sdcid, String keyid1, String keyid2, String keyid3, String tempid) throws ManagerException {
        String methodName = "getTempAttachment";
        try {
            this.startMethod(methodName, connectionid);
            AttachmentService attachment = new AttachmentService(this.sapphireConnection);
            Attachment attachment2 = attachment.getTempAttachment(sdcid, keyid1, keyid2, keyid3, tempid);
            return attachment2;
        }
        catch (Exception e) {
            this.logError("Failed to get attachment", e);
            this.beforeTransactionAbort();
            throw new EJBException(e.getMessage(), e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public sapphire.attachment.Attachment getTempAttachment(String connectionid, String sdcid, String keyid1, String keyid2, String keyid3, String tempid, Attachment.ThumbnailGeneration thumbnailGeneration) throws ManagerException {
        String methodName = "getTempAttachment";
        try {
            this.startMethod(methodName, connectionid);
            AttachmentService attachment = new AttachmentService(this.sapphireConnection);
            Attachment attachment2 = attachment.getTempAttachment(sdcid, keyid1, keyid2, keyid3, tempid, thumbnailGeneration.toString());
            return attachment2;
        }
        catch (Exception e) {
            this.logError("Failed to get attachment", e);
            this.beforeTransactionAbort();
            throw new EJBException(e.getMessage(), e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public sapphire.attachment.Attachment getSDIAttachment(String connectionid, String sdcid, String keyid1, String keyid2, String keyid3, int attachmentnum, int auditsequence) throws ManagerException {
        String methodName = "getSDIAttachment";
        try {
            this.startMethod(methodName, connectionid);
            AttachmentService attachment = new AttachmentService(this.sapphireConnection);
            Attachment attachment2 = attachment.getSDIAttachment(sdcid, keyid1, keyid2, keyid3, attachmentnum, auditsequence);
            return attachment2;
        }
        catch (Exception e) {
            this.logError("Failed to get attachment", e);
            this.beforeTransactionAbort();
            throw new EJBException(e.getMessage(), e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public sapphire.attachment.Attachment getSDIAttachment(String connectionid, String sdcid, String keyid1, String keyid2, String keyid3, int attachmentnum, int auditsequence, Attachment.ThumbnailGeneration thumbnailGeneration) throws ManagerException {
        String methodName = "getSDIAttachment";
        try {
            this.startMethod(methodName, connectionid);
            AttachmentService attachment = new AttachmentService(this.sapphireConnection);
            Attachment attachment2 = attachment.getSDIAttachment(sdcid, keyid1, keyid2, keyid3, attachmentnum, auditsequence, thumbnailGeneration.toString());
            return attachment2;
        }
        catch (Exception e) {
            this.logError("Failed to get attachment", e);
            this.beforeTransactionAbort();
            throw new EJBException(e.getMessage(), e);
        }
        finally {
            this.endMethod(methodName);
        }
    }
}

