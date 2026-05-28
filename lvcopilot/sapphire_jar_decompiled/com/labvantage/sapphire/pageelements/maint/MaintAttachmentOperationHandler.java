/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.modules.sdms.actions.AddSDIAttachmentOperation;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import java.util.HashMap;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.error.ErrorHandler;
import sapphire.util.DataSet;
import sapphire.util.LogContext;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;

public class MaintAttachmentOperationHandler
extends PropertyHandler {
    @Override
    public void processProperties(HashMap sdiprops) throws SapphireException {
        if (sdiprops.containsKey("ERRORHANDLER") && sdiprops.get("ERRORHANDLER") instanceof ErrorHandler) {
            ErrorHandler errorHandler = (ErrorHandler)sdiprops.get("ERRORHANDLER");
        } else {
            ErrorHandler errorHandler = new ErrorHandler();
        }
        String dataset = sdiprops.containsKey("__attachmentoperation_data") ? sdiprops.get("__attachmentoperation_data").toString() : "";
        DataSet capturedata = null;
        if (dataset.length() > 0) {
            try {
                capturedata = new DataSet(new JSONObject(dataset));
            }
            catch (Exception e) {
                this.logDebug("Could not create attachment data.");
            }
        }
        if (capturedata != null) {
            sdiprops.remove("__attachmentoperation_data");
            String prefix = (String)sdiprops.get("__prefix");
            String sdcid = (String)sdiprops.get("__" + prefix + "sdcid");
            M18NUtil m18n = new M18NUtil(this.getConnectionInfo());
            DBUtil dbUtil = new DBUtil();
            dbUtil.setConnection(this.sapphireConnection);
            QueryProcessor qp = new QueryProcessor(this.sapphireConnection.getConnectionId());
            ConnectionProcessor cp = new ConnectionProcessor(this.getConnectionInfo().getConnectionId());
            AddSDIAttachmentOperation.saveAttachmentOperations(sdcid, capturedata, m18n, dbUtil, qp, cp, new Logger(new LogContext(this.getConnectionInfo().getConnectionId())), false);
        }
    }
}

