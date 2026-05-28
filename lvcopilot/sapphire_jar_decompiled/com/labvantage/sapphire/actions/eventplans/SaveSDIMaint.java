/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eventplans;

import com.labvantage.sapphire.pageelements.PropertyHandler;
import com.labvantage.sapphire.pageelements.gwt.server.command.SDIMaint;
import com.labvantage.sapphire.services.ActionService;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.error.ErrorHandler;
import sapphire.xml.PropertyList;

public class SaveSDIMaint
extends BaseAction {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        SapphireConnection sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
        ActionService actionService = new ActionService(sapphireConnection);
        AuditService auditService = new AuditService(sapphireConnection);
        ErrorHandler errorHandler = new ErrorHandler();
        Logger logger = new Logger();
        SDIMaint sdiMaint = new SDIMaint(this.getSDCProcessor(), properties.getProperty("sdimaint"));
        try {
            sdiMaint.save(actionService, auditService, errorHandler, logger);
        }
        catch (ServiceException e) {
            this.getErrorHandler().addAll(errorHandler);
        }
    }

    private class Logger
    extends PropertyHandler {
        private Logger() {
        }

        @Override
        public void processProperties(HashMap props) throws SapphireException {
        }
    }
}

