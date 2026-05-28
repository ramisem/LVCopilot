/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.collector.collectortypes;

import com.labvantage.sapphire.modules.sdms.collector.collectortypes.AdvancedFileCollectorType;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SimpleFileCollectorType
extends AdvancedFileCollectorType {
    @Override
    public void configure(PropertyList collectorTypeProps) throws SapphireException {
        this.instrumentRootFolder = collectorTypeProps.getProperty("instrumentremoteroot");
        String msg = "";
        this.isCollectionEnabled = collectorTypeProps.getProperty("enablecollection").equals("Y");
        this.isDeliveryEnbabled = collectorTypeProps.getProperty("enablerunfiledelivery").equals("Y");
        this.isEmulatorEnabled = collectorTypeProps.getProperty("enableemulator").equals("Y");
        this.triggerRoot = Paths.get(this.instrumentRootFolder, new String[0]);
        if (!this.triggerRoot.toFile().exists() && (this.isCollectionEnabled || this.isDeliveryEnbabled || this.isEmulatorEnabled)) {
            msg = "Root folder " + this.instrumentRootFolder + " could not be located for " + this.getInstrumentid();
            this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to configure Collector", msg, false, true);
        }
        this.logStartup("Instrument root: " + this.instrumentRootFolder);
        if (this.isCollectionEnabled) {
            PropertyList collectorProps = collectorTypeProps.getPropertyListNotNull("collectorprops");
            this.collectorPollInterval = Integer.parseInt(collectorProps.getProperty("triggerpollintervalseconds", "" + this.getDefaultInstrumentPollInterval()));
            this.triggerType = "FileCreated";
            this.triggerSubFolderMatcher = this.instrumentRootFolder;
            this.triggerGlobFileMatcher = collectorProps.getProperty("globpathmatcher", "*.*");
            this.triggerSubFolderChecking = "Root";
            this.logStartup("Looking for files matching " + this.triggerGlobFileMatcher + " in root folder");
            this.waitType = "Time";
            this.waitSeconds = Integer.parseInt(collectorProps.getProperty("waitseconds", "5"));
            this.collectConfigCollection = new PropertyListCollection();
            PropertyList collect = new PropertyList();
            collect.setProperty("collecttype", "TriggerFile");
            collect.setProperty("attachmentclass", collectorProps.getProperty("attachmentclass"));
            collect.setProperty("actiononoriginal", collectorProps.getProperty("actiononoriginal"));
            collect.setProperty("zipmultiplefiles", "N");
            this.collectConfigCollection.add(collect);
        }
        if (this.isDeliveryEnbabled) {
            PropertyList deliveryProps = collectorTypeProps.getPropertyListNotNull("deliveryprops");
            this.deliveryRoot = Paths.get(this.instrumentRootFolder, new String[0]);
            this.deliveryFileOverwrite = deliveryProps.getProperty("fileexistsbehavior").equalsIgnoreCase("Overwrite");
            boolean collectdeliverRunFile = "Y".equalsIgnoreCase(deliveryProps.getProperty("collectrunfile"));
            if (this.isCollectionEnabled && collectdeliverRunFile) {
                PropertyList collect = new PropertyList();
                collect.setProperty("collecttype", "RunFile");
                collect.setProperty("actiononoriginal", deliveryProps.getProperty("actiononoriginal"));
                this.collectConfigCollection.add(collect);
            }
            this.logStartup("Looking for files in " + this.instrumentRootFolder + " every " + this.collectorPollInterval + "s.");
        }
        if (this.isEmulatorEnabled) {
            PropertyList emulatorProps = collectorTypeProps.getPropertyListNotNull("emulatorprops");
            this.emulatorPollInterval = Integer.parseInt(emulatorProps.getProperty("frequency", "" + this.getDefaultInstrumentPollInterval()));
            this.emulatorMode = "Simple";
            this.emulatorFilename = emulatorProps.getProperty("filename");
            this.emulatorFileContent = emulatorProps.getProperty("filecontent", "I am some results");
            this.emulatorRoot = Paths.get(this.instrumentRootFolder, new String[0]);
            this.logStartup("Emulating by generating file " + this.emulatorFilename + " in " + this.instrumentRootFolder + " every " + this.emulatorPollInterval + "s.");
        }
    }

    @Override
    public List<String> getReportsForSDC(PropertyList collectorTypeProps, String sdcid) {
        ArrayList<String> reports = new ArrayList<String>();
        PropertyList deliveryProps = collectorTypeProps.getPropertyListNotNull("deliveryprops");
        String reportid = deliveryProps.getProperty("reportid");
        if (reportid.length() > 0) {
            reports.add(reportid + ";C");
        }
        return reports;
    }
}

