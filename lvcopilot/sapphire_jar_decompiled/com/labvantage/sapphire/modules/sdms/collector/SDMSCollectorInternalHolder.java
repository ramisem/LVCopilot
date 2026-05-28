/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.collector;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.system.automation.LAM;
import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.modules.sdms.collector.SDMSCollector;
import com.labvantage.sapphire.modules.sdms.collector.SDMSCollectorHolder;
import com.labvantage.sapphire.modules.sdms.collector.SDMSCommandHandler;
import com.labvantage.sapphire.modules.sdms.collector.storagemodes.InternalSenderFactory;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.AutomationService;
import com.labvantage.sapphire.servlet.externalapp.ExternalAppException;
import java.nio.file.Path;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class SDMSCollectorInternalHolder
implements SDMSConstants,
SDMSCollectorHolder {
    private final String connectionid;
    private String databaseid;
    private String collectorid = "";
    private SDMSCollector sdmsCollector;
    SDMSCommandHandler sdmsCommandHandler;
    String hostname = "Internal";

    public SDMSCollectorInternalHolder(String connectionid, String databaseid, String collectorid) {
        this.databaseid = databaseid;
        this.collectorid = collectorid;
        this.connectionid = connectionid;
        this.sdmsCommandHandler = SDMSCommandHandler.getInstance(connectionid, databaseid);
        try {
            this.hostname = Configuration.getInstance().getServerHostName();
        }
        catch (SapphireException sapphireException) {
            // empty catch block
        }
    }

    public void start() throws SapphireException, ExternalAppException, JSONException {
        PropertyList configProps = this.getConfigProps();
        InternalSenderFactory senderFactory = new InternalSenderFactory(this.connectionid);
        try {
            this.sdmsCollector = new SDMSCollector(this.collectorid, this.databaseid, this, configProps, senderFactory);
            this.sdmsCollector.startPinging();
            this.sdmsCollector.beginOperations();
        }
        catch (Exception e) {
            String message = "Failed to start SDMSCollectorInternalHolder for collector " + this.collectorid + ". Reason: " + e.getMessage();
            this.sdmsCollector.raiseCollectorAlert("SDMS Collector State", "SDMS Startup", "Failed to start SDMSCollectorInternalHolder ", message, false);
            throw new SapphireException(message, e);
        }
    }

    public PropertyList getConfigProps() throws SapphireException, ExternalAppException {
        PropertyList commandRequest = new PropertyList();
        commandRequest.setProperty("collectorid", this.collectorid);
        PropertyList configProps = this.sendCommandToLIMS("COMMAND_GETCONFIGPROPS", commandRequest);
        return configProps;
    }

    @Override
    public PropertyList sendCommandToLIMS(String command, PropertyList commandRequest) throws SapphireException {
        return this.sdmsCommandHandler.processCommand(command, commandRequest);
    }

    @Override
    public void executeRebootCommand() {
        LAM lam = AutomationService.getLAM(this.databaseid);
        lam.stopCollector(this.collectorid);
    }

    @Override
    public void upgrade(String upgradeMode) {
    }

    @Override
    public String getActualHostname() {
        try {
            return Configuration.getInstance().getHostid();
        }
        catch (SapphireException e) {
            return "Unknown Internal";
        }
    }

    @Override
    public void setStartupStateProperties(PropertyList startupState) {
    }

    @Override
    public void log(String type, String message) {
        this.log(type, message, "");
    }

    @Override
    public void log(String type, String message, Throwable e) {
        this.log(type, message, "", e);
    }

    @Override
    public void log(String type, String message, String instrumentid) {
        Trace.logDebug(type, (instrumentid != null && instrumentid.length() > 0 ? instrumentid + ": " : "") + message);
    }

    @Override
    public void log(String type, String message, String instrumentid, Throwable e) {
        Trace.logError(type, (Object)((instrumentid != null && instrumentid.length() > 0 ? instrumentid + ": " : "") + message), e);
    }

    @Override
    public JSONObject sendFileCommandToLIMS(String processAs, String command, Path file, JSONObject jsonRequest) throws SapphireException {
        return this.sdmsCommandHandler.processFileCommand(command, file, jsonRequest);
    }

    public String getCollectorid() {
        return this.collectorid;
    }

    public boolean shutdown() {
        boolean shutdown = true;
        if (this.sdmsCollector != null) {
            shutdown = this.sdmsCollector.shutdown();
        }
        return shutdown;
    }
}

