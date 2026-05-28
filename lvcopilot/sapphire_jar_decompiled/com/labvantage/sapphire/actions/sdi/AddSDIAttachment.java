/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.services.AttachmentService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileType;
import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class AddSDIAttachment
extends BaseAction
implements sapphire.action.AddSDIAttachment {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        AttachmentService attachment = new AttachmentService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
        byte[] data = null;
        if (properties.containsKey("data") && properties.getProperty("data").length() > 0) {
            FileManager.FileData fileData = new FileManager.FileData(properties.getProperty("data"), FileType.getFileType(properties.getProperty("filename"), this.getConnectionId()).getMime());
            data = fileData.getData();
        }
        try {
            String sdcid = properties.getProperty("sdcid");
            if (SecurityPolicyUtil.isFileLocationPolicyEnforcedForAction(this.connectionInfo.getConnectionId()) && !sdcid.equalsIgnoreCase("MessageLog") && !sdcid.equalsIgnoreCase("ReportEvent") && !sdcid.equalsIgnoreCase("LicenseKey")) {
                FileManager.validateFileLocationPolicyForUpload(properties, this.getConfigurationProcessor(), this.connectionInfo.getConnectionId());
            }
            sapphire.attachment.Attachment attachment1 = Attachment.getAttachment(properties, this.connectionInfo.getConnectionId());
            attachment1.setData(data);
            attachment.addSDIAttachment(attachment1, false, false, "");
            properties.put("attachmentnum", attachment1.getAttachmentNum());
        }
        catch (ServiceException e) {
            throw new SapphireException("ATTACHMENT_SERVICE_FAILED", ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
    }
}

