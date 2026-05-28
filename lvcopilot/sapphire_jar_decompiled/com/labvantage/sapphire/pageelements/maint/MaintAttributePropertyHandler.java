/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.sapphire.pageelements.PropertyHandler;
import com.labvantage.sapphire.pageelements.maint.MaintAttribute;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.error.ErrorHandler;
import sapphire.util.M18NUtil;

public class MaintAttributePropertyHandler
extends PropertyHandler {
    @Override
    public void processProperties(HashMap sdiprops) throws SapphireException {
        String prefix = (String)sdiprops.get("__prefix");
        String sdcid = (String)sdiprops.get("__" + prefix + "sdcid");
        ErrorHandler errorHandler = sdiprops.containsKey("ERRORHANDLER") && sdiprops.get("ERRORHANDLER") instanceof ErrorHandler ? (ErrorHandler)sdiprops.get("ERRORHANDLER") : new ErrorHandler();
        M18NUtil m18n = new M18NUtil(this.getConnectionInfo());
        ActionProcessor ap = new ActionProcessor(this.sapphireConnection.getConnectionId());
        SDCProcessor sdcProcessor = new SDCProcessor(this.sapphireConnection.getConnectionId());
        if (sdcProcessor.getProperty(sdcid, "allowattributesflag", "N").equalsIgnoreCase("Y")) {
            if (sdiprops.containsKey("__attribute_data")) {
                MaintAttribute.saveAttributeData(sdiprops.get("__attribute_data").toString(), ap, m18n);
            } else if (!sdiprops.containsKey("__propertyhandler__attributesprocessed")) {
                sdiprops.put("__propertyhandler__attributesprocessed", "Y");
                for (String key : sdiprops.keySet()) {
                    if (!key.startsWith("__attribute_data")) continue;
                    MaintAttribute.saveAttributeData(sdiprops.get(key).toString(), ap, m18n);
                }
            }
        }
    }
}

