/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.modules.sdms.actions.AddSDIDataCapture;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import java.util.HashMap;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.error.ErrorHandler;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;

public class MaintDataCaptureHandler
extends PropertyHandler {
    @Override
    public void processProperties(HashMap sdiprops) throws SapphireException {
        if (sdiprops.containsKey("ERRORHANDLER") && sdiprops.get("ERRORHANDLER") instanceof ErrorHandler) {
            ErrorHandler errorHandler = (ErrorHandler)sdiprops.get("ERRORHANDLER");
        } else {
            ErrorHandler errorHandler = new ErrorHandler();
        }
        String dataset = sdiprops.containsKey("__attachmentoperation_data") ? sdiprops.get("__attachmentoperation_data").toString() : "";
        DataSet datacaptures = null;
        if (dataset.length() > 0) {
            try {
                datacaptures = new DataSet(new JSONObject(dataset));
            }
            catch (Exception e) {
                this.logDebug("Could not create attachment data.");
            }
        }
        if (datacaptures != null) {
            sdiprops.remove("__attachmentoperation_data");
            String prefix = (String)sdiprops.get("__prefix");
            String sdcid = (String)sdiprops.get("__" + prefix + "sdcid");
            M18NUtil m18n = new M18NUtil(this.getConnectionInfo());
            DBUtil dbUtil = new DBUtil();
            dbUtil.setConnection(this.sapphireConnection);
            AddSDIDataCapture.saveSDIDataCapture(sdcid, datacaptures, m18n, dbUtil, new Logger(this.sapphireConnection.getConnectionId()));
        }
    }
}

