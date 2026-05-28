/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.admin.system.AttachmentProcessor;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.attachment.Attachment;
import sapphire.xml.PropertyList;

public class GetSDIAttachment
extends BaseAction
implements sapphire.action.GetSDIAttachment {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        AttachmentProcessor attachmentProcessor = new AttachmentProcessor(this.getConnectionId());
        String sdcid = properties.getProperty("sdcid", "");
        String keyid1 = properties.getProperty("keyid1", "");
        String keyid2 = properties.getProperty("keyid2", "(null)");
        String keyid3 = properties.getProperty("keyid3", "(null)");
        String attachmentclass = properties.getProperty("attachmentclass", "");
        boolean thumb = properties.getProperty("returnthumbnail", "N").equalsIgnoreCase("Y");
        boolean additionalColumns = properties.getProperty("returnadditionalcols", "N").equalsIgnoreCase("Y");
        Attachment attachment = null;
        if (attachmentclass.length() > 0) {
            attachment = Attachment.getAttachment(sdcid, keyid1, keyid2, keyid3);
            attachment.setAttachmentClass(attachmentclass);
            try {
                attachmentProcessor.getSDIAttachment(attachment, thumb ? Attachment.ThumbnailGeneration.SHOWIFAVAILABLE : Attachment.ThumbnailGeneration.DISABLED);
            }
            catch (Exception e) {
                throw new SapphireException("ATTACHMENT_SERVICE_FAILED", ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
        }
        String attnum = properties.getProperty("attachmentnum", "-1");
        int an = -1;
        if (attnum.length() > 0) {
            try {
                an = Integer.parseInt(attnum);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (an > -1) {
            attachment = Attachment.getAttachment(sdcid, keyid1, keyid2, keyid3, an);
            try {
                attachmentProcessor.getSDIAttachment(attachment, thumb ? Attachment.ThumbnailGeneration.SHOWIFAVAILABLE : Attachment.ThumbnailGeneration.DISABLED);
            }
            catch (Exception e) {
                throw new SapphireException("ATTACHMENT_SERVICE_FAILED", ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
        }
        if (attachment == null) {
            throw new SapphireException("ATTACHMENT_SERVICE_FAILED", "Failed to obtain attachment.");
        }
        attachment.toPropertyList(properties, properties.getProperty("returndata", "N").equalsIgnoreCase("Y"), thumb, additionalColumns, this.getConnectionId());
        this.logger.debug("Get Attachment Finished.");
    }
}

