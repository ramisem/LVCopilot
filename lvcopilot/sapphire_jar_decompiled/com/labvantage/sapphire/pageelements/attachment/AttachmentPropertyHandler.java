/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.attachment;

import com.labvantage.sapphire.pageelements.PropertyHandler;
import com.labvantage.sapphire.util.file.FileManager;
import java.util.HashMap;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.error.ErrorHandler;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;

public class AttachmentPropertyHandler
extends PropertyHandler {
    @Override
    public void processProperties(HashMap sdiprops) throws SapphireException {
        if (sdiprops.containsKey("ERRORHANDLER") && sdiprops.get("ERRORHANDLER") instanceof ErrorHandler) {
            ErrorHandler errorHandler = (ErrorHandler)sdiprops.get("ERRORHANDLER");
        } else {
            ErrorHandler errorHandler = new ErrorHandler();
        }
        String dataset = sdiprops.containsKey("__attachment_data") ? sdiprops.get("__attachment_data").toString() : "";
        DataSet attachmentData = null;
        if (dataset.length() > 0) {
            try {
                attachmentData = new DataSet(new JSONObject(dataset));
            }
            catch (Exception e) {
                this.logDebug("Could not create attachment data.");
            }
        }
        if (attachmentData != null) {
            sdiprops.remove("__attachment_data");
            String attachmentpolicynode = "";
            if (sdiprops.containsKey("__attachment_policy")) {
                attachmentpolicynode = sdiprops.get("__attachment_policy").toString();
                sdiprops.remove("__attachment_data");
            }
            String prefix = (String)sdiprops.get("__prefix");
            String sdcid = (String)sdiprops.get("__" + prefix + "sdcid");
            M18NUtil m18n = new M18NUtil(this.getConnectionInfo());
            ActionProcessor ap = new ActionProcessor(this.sapphireConnection.getConnectionId());
            SDCProcessor sdcProcessor = new SDCProcessor(this.sapphireConnection.getConnectionId());
            FileManager.saveAttachmentData(sdcid, attachmentData, attachmentpolicynode, this.getConnectionInfo().getConnectionId());
            this.logDebug("Attachment data saved.");
        }
    }
}

