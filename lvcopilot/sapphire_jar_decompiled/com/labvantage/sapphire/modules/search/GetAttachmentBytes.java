/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.search;

import com.labvantage.sapphire.services.Attachment;
import sapphire.SapphireException;
import sapphire.accessor.AttachmentProcessor;
import sapphire.action.BaseAction;
import sapphire.attachment.Attachment;
import sapphire.xml.PropertyList;

public class GetAttachmentBytes
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        try {
            String sdcid = properties.getProperty("sdcid");
            String keyid1 = properties.getProperty("keyid1");
            String keyid2 = properties.getProperty("keyid2");
            String keyid3 = properties.getProperty("keyid3");
            String attachmentNum = properties.getProperty("attachmentnum", "0");
            AttachmentProcessor attachmentProcessor = new AttachmentProcessor(this.getConnectionid());
            Attachment attachment = (Attachment)Attachment.getAttachment(sdcid, keyid1, keyid2, keyid3, Integer.parseInt(attachmentNum));
            attachment.setTriggerBusinessRule(false);
            attachment.setAllowLocalCache(false);
            sapphire.attachment.Attachment attachment2 = attachmentProcessor.getSDIAttachment(attachment, Attachment.ThumbnailGeneration.DISABLED);
            if (attachment2 != null) {
                properties.put("data", attachment2);
            }
        }
        catch (Exception e) {
            this.logger.error("Exception occurred when trying to retrieve attachment: ", e);
        }
    }
}

