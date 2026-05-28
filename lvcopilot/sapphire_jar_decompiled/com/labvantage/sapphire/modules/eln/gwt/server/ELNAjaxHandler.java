/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eln.gwt.server;

import com.labvantage.sapphire.actions.eln.BaseELNAction;
import com.labvantage.sapphire.modules.eln.Worksheet;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandRequest;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandRequestAjaxHandler;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandResponse;
import com.labvantage.sapphire.pageelements.gwt.shared.ELNConstants;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;

public class ELNAjaxHandler
extends CommandRequestAjaxHandler
implements ELNConstants {
    @Override
    protected boolean processCommand(String command, CommandRequest commandRequest, CommandResponse commandResponse) throws SapphireException {
        if (command.equals("lws")) {
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            try {
                ConnectionProcessor connectionProcessor = new ConnectionProcessor(this.getConnectionid());
                SapphireConnection sapphireConnection = connectionProcessor.getSapphireConnection();
                Worksheet worksheet = new Worksheet(sapphireConnection);
                worksheet.open(worksheetid, worksheetversionid, commandRequest, commandResponse);
            }
            catch (Exception e) {
                commandResponse.setStatusFail("Failed to load worksheet " + BaseELNAction.getIdVersionText(worksheetid, worksheetversionid) + ". Reason: " + e.getMessage(), e);
            }
            return false;
        }
        return true;
    }
}

