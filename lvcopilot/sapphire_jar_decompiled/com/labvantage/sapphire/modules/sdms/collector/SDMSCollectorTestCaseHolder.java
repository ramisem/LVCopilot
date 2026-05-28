/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.collector;

import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.modules.sdms.collector.SDMSCollector;
import com.labvantage.sapphire.modules.sdms.collector.SDMSCollectorHolder;
import com.labvantage.sapphire.modules.sdms.collector.storagemodes.ExternalSenderFactory;
import com.labvantage.sapphire.modules.sdms.collector.storagemodes.FileSenderFactory;
import com.labvantage.sapphire.modules.sdms.collector.storagemodes.JUnitSenderFactory;
import com.labvantage.sapphire.services.SapphireService;
import com.labvantage.sapphire.servlet.externalapp.ExternalAppException;
import java.nio.file.Path;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.servlet.ExternalHandlerProcessor;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class SDMSCollectorTestCaseHolder
implements SDMSConstants,
SDMSCollectorHolder {
    public static SDMSCollector sdmsCollector = null;
    private String collectorid;
    String databaseid;
    FileSenderFactory senderFactory = null;
    ExternalHandlerProcessor processor = null;

    public SDMSCollectorTestCaseHolder(String token, String collectorid, String url) throws SapphireException {
        this.databaseid = SapphireService.getDatabaseForToken(token);
        this.processor = new ExternalHandlerProcessor(token, url);
        this.collectorid = collectorid;
    }

    public void start() throws SapphireException, ExternalAppException, JSONException {
        PropertyList configProps = this.getConfigProps();
        DataSet collectorDS = new DataSet(new JSONObject(configProps.getProperty("collector_dataset")));
        String storagemodeflag = collectorDS.getValue(0, "storagemodeflag");
        this.senderFactory = storagemodeflag.equals("T") ? new JUnitSenderFactory() : new ExternalSenderFactory(false);
        sdmsCollector = new SDMSCollector(this.collectorid, this.databaseid, this, configProps, this.senderFactory);
        PropertyList commandRequest = new PropertyList();
        commandRequest.setProperty("collectorid", this.collectorid);
        this.sendCommandToLIMS("COMMAND_REGISTERCOLLECTOR", commandRequest);
        sdmsCollector.startPinging();
        sdmsCollector.beginOperations();
    }

    public void stop() {
        if (sdmsCollector != null) {
            sdmsCollector.shutdown();
        }
    }

    public List<Path> getSenderFiles() throws SapphireException {
        if (this.senderFactory instanceof JUnitSenderFactory) {
            return ((JUnitSenderFactory)this.senderFactory).getSender().getFiles();
        }
        throw new SapphireException("Cannot get sender files when sender type is external");
    }

    public PropertyList getConfigProps() throws SapphireException, ExternalAppException {
        PropertyList commandRequest = new PropertyList();
        commandRequest.setProperty("collectorid", this.collectorid);
        PropertyList configProps = this.sendCommandToLIMS("COMMAND_GETCONFIGPROPS", commandRequest);
        return configProps;
    }

    @Override
    public void log(String type, String message) {
        this.log(type, message, "");
    }

    @Override
    public void log(String type, String message, Throwable e) {
        this.log(type, message, "");
        e.printStackTrace();
    }

    @Override
    public void log(String type, String message, String instrumentid) {
        System.out.println(type + ": " + (instrumentid != null && instrumentid.length() > 0 ? instrumentid + ": " : "") + message);
    }

    @Override
    public void log(String type, String message, String instrumentid, Throwable e) {
        this.log(type, message, instrumentid);
        e.printStackTrace();
    }

    @Override
    public JSONObject sendFileCommandToLIMS(String processAs, String command, Path file, JSONObject jsonRequest) throws SapphireException {
        return this.processor.sendFileCommandToLIMS(processAs, command, file, jsonRequest);
    }

    @Override
    public PropertyList sendCommandToLIMS(String command, PropertyList commandProps) throws SapphireException {
        return this.processor.sendCommandToLIMS(command, commandProps);
    }

    @Override
    public void executeRebootCommand() {
    }

    @Override
    public void upgrade(String upgradeMode) {
    }

    @Override
    public String getActualHostname() {
        return "JUnit";
    }

    @Override
    public void setStartupStateProperties(PropertyList startupState) {
    }

    public String getCollectorid() {
        return this.collectorid;
    }
}

