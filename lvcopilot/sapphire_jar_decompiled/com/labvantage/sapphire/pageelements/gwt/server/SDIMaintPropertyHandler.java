/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.gwt.server;

import com.labvantage.sapphire.pageelements.PropertyHandler;
import com.labvantage.sapphire.pageelements.gwt.server.command.SDIMaint;
import com.labvantage.sapphire.services.ActionService;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.ServiceException;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.error.ErrorHandler;
import sapphire.xml.PropertyList;

public class SDIMaintPropertyHandler
extends PropertyHandler {
    @Override
    public void processProperties(HashMap props) throws SapphireException {
        ActionService actionService = new ActionService(this.sapphireConnection);
        AuditService auditService = new AuditService(this.sapphireConnection);
        ErrorHandler errorHandler = new ErrorHandler();
        String elementid = (String)props.get("__propertyhandler_elementid");
        SDCProcessor sdcProcessor = new SDCProcessor(this.getConnectionInfo().getConnectionId());
        PropertyList sdcProps = sdcProcessor.getPropertyList((String)props.get("__sdimaint_" + elementid + "_sdcid"));
        String jsonSDIMaint = (String)props.get("__sdimaint_" + elementid);
        SDIMaint sdiMaint = new SDIMaint(sdcProps, jsonSDIMaint);
        try {
            sdiMaint.save(actionService, auditService, errorHandler, this);
        }
        catch (ServiceException se) {
            this.getErrorHandler().addAll(errorHandler);
        }
    }
}

