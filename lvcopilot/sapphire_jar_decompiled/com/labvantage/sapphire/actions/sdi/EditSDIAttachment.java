/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.services.AttachmentService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class EditSDIAttachment
extends BaseAction {
    public static final String ID = "EditSDIAttachment";
    public static final String VERSION = "1";
    public static final String PROPERTY_SDCID = "sdcid";
    public static final String PROPERTY_KEYID1 = "keyid1";
    public static final String PROPERTY_KEYID2 = "keyid2";
    public static final String PROPERTY_KEYID3 = "keyid3";
    public static final String PROPERTY_ATTACHMENTNUM = "attachmentnum";
    public static final String PROPERTY_FILENAME = "filename";
    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_DESCRIPTION = "description";
    public static final String PROPERTY_APPLYLOCK = "applylock";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        AttachmentService attachment = new AttachmentService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
        try {
            byte[] data = null;
            attachment.editSDIAttachment((HashMap)properties, data);
        }
        catch (ServiceException e) {
            throw new SapphireException("ATTACHMENT_SERVICE_FAILED", ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
    }
}

