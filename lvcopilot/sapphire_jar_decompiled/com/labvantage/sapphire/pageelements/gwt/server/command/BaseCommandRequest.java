/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.gwt.server.command;

import com.labvantage.sapphire.pageelements.PropertyHandler;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandRequest;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandResponse;
import com.labvantage.sapphire.pageelements.gwt.shared.CommandConstants;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.accessor.TranslationProcessor;

public abstract class BaseCommandRequest
extends PropertyHandler
implements CommandConstants {
    private ActionProcessor actionProcessor;
    private ConnectionProcessor connectionProcessor;
    private DAMProcessor damProcessor;
    private QueryProcessor queryProcessor;
    private SDIProcessor sdiProcessor;
    private SDCProcessor sdcProcessor;
    private SequenceProcessor sequenceProcessor;
    private TranslationProcessor translationProcessor;
    private ConfigurationProcessor configurationProcessor;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processProperties(HashMap props) throws SapphireException {
        CommandRequest commandRequest = (CommandRequest)props.get("commandrequest");
        CommandResponse commandResponse = (CommandResponse)props.get("commandresponse");
        try {
            this.processCommand(commandRequest.getCommand(), commandRequest, commandResponse);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to process command '" + commandRequest.getCommand() + "'. Reason: " + e.getMessage() + "\nSee log for details.");
            this.logError("Failed to process command '" + commandRequest.getCommand() + "'. Reason: " + e.getMessage(), e);
            commandResponse.addErrorHandler(commandResponse.getStatusMessage());
        }
        finally {
            if (commandResponse.containsKey("ERRORHANDLER")) {
                props.put("ERRORHANDLER", commandResponse.get("ERRORHANDLER"));
            }
        }
    }

    protected abstract boolean processCommand(String var1, CommandRequest var2, CommandResponse var3) throws SapphireException;

    protected ActionProcessor getActionProcessor() {
        if (this.actionProcessor == null) {
            this.actionProcessor = new ActionProcessor(this.sapphireConnection.getConnectionId());
        }
        return this.actionProcessor;
    }

    protected ConnectionProcessor getConnectionProcessor() {
        if (this.connectionProcessor == null) {
            this.connectionProcessor = new ConnectionProcessor(this.sapphireConnection.getConnectionId());
        }
        return this.connectionProcessor;
    }

    protected DAMProcessor getDAMProcessor() {
        if (this.damProcessor == null) {
            this.damProcessor = new DAMProcessor(this.sapphireConnection.getConnectionId());
        }
        return this.damProcessor;
    }

    protected QueryProcessor getQueryProcessor() {
        if (this.queryProcessor == null) {
            this.queryProcessor = new QueryProcessor(this.sapphireConnection.getConnectionId());
        }
        return this.queryProcessor;
    }

    protected SDCProcessor getSDCProcessor() {
        if (this.sdcProcessor == null) {
            this.sdcProcessor = new SDCProcessor(this.sapphireConnection.getConnectionId());
        }
        return this.sdcProcessor;
    }

    protected SDIProcessor getSDIProcessor() {
        if (this.sdiProcessor == null) {
            this.sdiProcessor = new SDIProcessor(this.sapphireConnection.getConnectionId());
        }
        return this.sdiProcessor;
    }

    protected SequenceProcessor getSequenceProcessor() {
        if (this.sequenceProcessor == null) {
            this.sequenceProcessor = new SequenceProcessor(this.sapphireConnection.getConnectionId());
        }
        return this.sequenceProcessor;
    }

    protected TranslationProcessor getTranslationProcessor() {
        if (this.translationProcessor == null) {
            this.translationProcessor = new TranslationProcessor(this.sapphireConnection.getConnectionId());
        }
        return this.translationProcessor;
    }

    protected ConfigurationProcessor getConfigurationProcessor() {
        if (this.configurationProcessor == null) {
            this.configurationProcessor = new ConfigurationProcessor(this.sapphireConnection.getConnectionId());
        }
        return this.configurationProcessor;
    }
}

