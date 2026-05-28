/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.services.AttachmentService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class DeleteSDIAttachment
extends BaseAction
implements sapphire.action.DeleteSDIAttachment {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        AttachmentService attachment = new AttachmentService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
        try {
            attachment.deleteSDIAttachment(properties.getProperty("sdcid"), properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"), properties.getProperty("attachmentnum"), properties, properties.getProperty("applylock").equals("Y"));
        }
        catch (ServiceException e) {
            throw new SapphireException("ATTACHMENT_SERVICE_FAILED", "Failed to delete the attachment: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
    }
}

