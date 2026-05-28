/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.codahale.metrics.MetricRegistry
 */
package com.labvantage.sapphire.modules.sdms.collector;

import com.codahale.metrics.MetricRegistry;
import com.labvantage.sapphire.Build;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.system.ThreadUtil;
import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.modules.sdms.collector.LogEntryList;
import com.labvantage.sapphire.modules.sdms.collector.MonitoredThreadPoolExecutor;
import com.labvantage.sapphire.modules.sdms.collector.SDMSCollectorHolder;
import com.labvantage.sapphire.modules.sdms.collector.collectortypes.BaseCollectorType;
import com.labvantage.sapphire.modules.sdms.collector.storagemodes.FileSenderFactory;
import com.labvantage.sapphire.servlet.externalapp.ExternalAppException;
import com.labvantage.sapphire.util.file.FileTransferOptions;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SDMSCollector
implements SDMSConstants {
    public static FileTransferOptions.HashingAlgorithm defaultHashingAlgorithm = FileTransferOptions.HashingAlgorithm.MD5;
    private String collectorid;
    private String databaseid;
    public SDMSCollectorHolder container;
    private FileSenderFactory senderFactory;
    private MonitoredThreadPoolExecutor fileCollectorService;
    private MonitoredThreadPoolExecutor fileEmulatorService;
    private MonitoredThreadPoolExecutor utilityService;
    private ArrayList<BaseCollectorType> collectorTypeInstances = new ArrayList();
    private Map<String, BaseCollectorType> instrumentCollectorTypeInstance = new HashMap<String, BaseCollectorType>();
    private Path storagePathLocal;
    private int minDiskSpaceGb;
    private int minDiskSpaceGbSeconds = 3600;
    private int missedPollIntervalCount = 5;
    private int pingIntervalSeconds = 10;
    private int fastPingIntervalSeconds = 1;
    private int corePoolSize = 1;
    private int defaultInstrumentPollInterval = 10;
    private int shutdownTimeoutSeconds = 60;
    private int waitForIsolatedMode = 30;
    private int hashMaxSizeGB = 1;
    private int maxLogSize = 1000;
    private long maxLogAgeHours = 5L;
    private int MAX_CORE_SIZE = 20;
    private LogEntryList logger = new LogEntryList(this);
    private PropertyList collectorDefaults;
    private boolean isStorageModeDirect;
    private boolean isInternal;
    private boolean isShuttingDownCollectors = false;
    private PropertyList runtimeProperties = new PropertyList();
    private MetricRegistry metricRegistry = new MetricRegistry();
    private StringBuilder startupLog = new StringBuilder();
    private String collectorConfigHash = "";
    private HashMap<String, String> caretakerInstrumentRunnableCheck = new HashMap();
    private PingRunnable pingRunnable;
    private boolean isPaused = false;
    private boolean isDisabled = false;
    private boolean isHashable = true;

    public SDMSCollector() {
    }

    public SDMSCollector(String collectorid, String databaseid, SDMSCollectorHolder container, PropertyList configProps, FileSenderFactory senderFactory) throws SapphireException {
        this.collectorid = collectorid;
        this.container = container;
        this.senderFactory = senderFactory;
        this.databaseid = databaseid;
        this.init(configProps);
    }

    public SDMSCollector(String collectorid, String databaseid, SDMSCollectorHolder container, FileSenderFactory senderFactory) {
        this.collectorid = collectorid;
        this.databaseid = databaseid;
        this.container = container;
        this.senderFactory = senderFactory;
    }

    public void init(PropertyList configProps) throws SapphireException {
        try {
            this.collectorConfigHash = configProps.getProperty("confighash");
            this.logStartup("+++++++++++++++++++++++++++++++++++++++++++++++++++");
            this.logStartup("++ Collector " + this.collectorid + " Starting Up");
            this.logStartup("+++++++++++++++++++++++++++++++++++++++++++++++++++");
            DataSet collectorDS = new DataSet(new JSONObject(configProps.getProperty("collector_dataset")));
            DataSet instruments = new DataSet(new JSONObject(configProps.getProperty("instruments_dataset")));
            this.collectorDefaults = new PropertyList();
            this.collectorDefaults.setJSONString(configProps.getProperty("collectordefaults"));
            if (instruments.size() == 0) {
                this.raiseCollectorAlert("SDMS Startup", "Warning", "No instruments found", "No instruments have been configured for collector " + this.collectorid, false);
            }
            this.pingIntervalSeconds = Integer.parseInt(this.collectorDefaults.getProperty("serverpinginterval", "" + this.pingIntervalSeconds));
            this.fastPingIntervalSeconds = Integer.parseInt(this.collectorDefaults.getProperty("fastserverpinginterval", "" + this.fastPingIntervalSeconds));
            this.defaultInstrumentPollInterval = Integer.parseInt(this.collectorDefaults.getProperty("defaultinstrumentpollinterval", "" + this.defaultInstrumentPollInterval));
            this.shutdownTimeoutSeconds = Integer.parseInt(this.collectorDefaults.getProperty("shutdowntimeoutseconds", "" + this.shutdownTimeoutSeconds));
            this.waitForIsolatedMode = Integer.parseInt(this.collectorDefaults.getProperty("waitforisolatedmode", "" + this.waitForIsolatedMode));
            this.hashMaxSizeGB = Integer.parseInt(this.collectorDefaults.getProperty("hashingmaxsize", "" + this.hashMaxSizeGB));
            this.minDiskSpaceGbSeconds = Integer.parseInt(this.collectorDefaults.getProperty("mindiskspaceseconds", "" + this.minDiskSpaceGbSeconds));
            this.missedPollIntervalCount = Integer.parseInt(this.collectorDefaults.getProperty("missedpollintervalcount", "" + this.missedPollIntervalCount));
            this.maxLogAgeHours = Integer.parseInt(this.collectorDefaults.getProperty("maxlogagehours", "" + this.maxLogAgeHours));
            this.maxLogSize = Integer.parseInt(this.collectorDefaults.getProperty("maxlogsize", "" + this.maxLogSize));
            this.logger.setMaxAge(this.maxLogAgeHours * 3600L * 1000L);
            this.logger.setMaxSize(this.maxLogSize);
            this.storagePathLocal = Paths.get(collectorDS.getValue(0, "storagepathlocal"), new String[0]);
            this.minDiskSpaceGb = Integer.parseInt(collectorDS.getValue(0, "mindiskspace", "0"));
            this.runtimeProperties = configProps.getPropertyListNotNull("runtimeproperties");
            this.isStorageModeDirect = collectorDS.getValue(0, "storagemodeflag").equals("D");
            this.isInternal = collectorDS.getValue(0, "internalflag").equals("Y");
            this.isPaused = collectorDS.getValue(0, "pausedflag", "N").equals("Y");
            this.isDisabled = collectorDS.getValue(0, "disabledflag", "N").equals("Y");
            this.isHashable = collectorDS.getValue(0, "datahashflag", "Y").equals("Y");
            if (this.isDisabled) {
                this.logStartup("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                this.logStartup("++ Collector is DISABLED. Collecting will not start ++");
                this.logStartup("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            } else if (this.isPaused) {
                this.logStartup("+++++++++++++++++++++++++++++++++++++++++++++++++++");
                this.logStartup("++ Collector is PAUSED. Collecting will not start++");
                this.logStartup("+++++++++++++++++++++++++++++++++++++++++++++++++++");
            }
            for (int i = 0; i < instruments.size(); ++i) {
                String instrumentid = instruments.getValue(i, "instrumentid");
                boolean isInstrumentPaused = instruments.getValue(i, "sdmsspausedflag", "N").equalsIgnoreCase("Y");
                String objectname = instruments.getValue(i, "_objectname");
                String collectorType = instruments.getValue(i, "_collectortype");
                if (objectname.length() <= 0) continue;
                PropertyList collectorTypeProps = new PropertyList();
                collectorTypeProps.setPropertyList(instruments.getValue(i, "_collectorrules", "<propertylist />"));
                String configHash = instruments.getValue(i, "_confighash");
                if (i > 0) {
                    this.logStartup("\n");
                }
                this.logStartup("Configuring instrument " + instrumentid);
                BaseCollectorType collectorTypeInstance = (BaseCollectorType)Class.forName(objectname).newInstance();
                try {
                    collectorTypeInstance.init(this, instrumentid, collectorType, this.metricRegistry, new LogEntryList(this, this.maxLogAgeHours * 3600L * 1000L, this.maxLogSize));
                    collectorTypeInstance.setInstrumentPaused(isInstrumentPaused);
                    collectorTypeInstance.setConfigHash(configHash);
                    collectorTypeInstance.configure(collectorTypeProps);
                    this.collectorTypeInstances.add(collectorTypeInstance);
                    this.instrumentCollectorTypeInstance.put(instrumentid, collectorTypeInstance);
                    continue;
                }
                catch (Exception e) {
                    String message = "Failed to start instrument " + instrumentid + ". Reason: " + e.getMessage();
                    this.logStartup(message);
                    this.raiseInstrumentAlert(instrumentid, "SDMS Startup", "Failure", "Failed to start instrument", message, false);
                }
            }
            this.corePoolSize = collectorDS.getInt(0, "corepoolsize", -1);
            if (this.corePoolSize > this.MAX_CORE_SIZE) {
                this.corePoolSize = this.MAX_CORE_SIZE;
            }
            this.logStartup("Poolsize set to " + this.corePoolSize + (this.corePoolSize == this.MAX_CORE_SIZE ? " (max is " + this.MAX_CORE_SIZE + ")" : ""));
        }
        catch (Exception e) {
            this.raiseCollectorAlert("SDMS Startup", "Failure", "Failed to start Collector", "Uncaught exception starting collector " + this.collectorid + ": " + e.getMessage(), false);
            throw new SapphireException("Failed to start Collector: " + e.getMessage(), e);
        }
    }

    public String raiseCollectorAlert(String alertType, String alertSeverity, String description, String message, boolean forceNew) {
        return this.raiseAlert("LV_SDMSCollector", this.collectorid, alertType, alertSeverity, description, message, forceNew);
    }

    public String raiseInstrumentAlert(String instrumentid, String alertType, String alertSeverity, String description, String message, boolean forceNew) {
        return this.raiseAlert("Instrument", instrumentid, alertType, alertSeverity, description, message, forceNew);
    }

    private String raiseAlert(String sdcid, String keyid1, String alertType, String alertSeverity, String description, String message, boolean forceNew) {
        boolean matchDescription = true;
        PropertyList alert = new PropertyList();
        alert.setProperty("sdcid", sdcid);
        alert.setProperty("keyid1", keyid1);
        alert.setProperty("alerttype", alertType);
        alert.setProperty("alertseverity", alertSeverity);
        alert.setProperty("forcenew", forceNew ? "Y" : "N");
        alert.setProperty("matchdescription", matchDescription ? "Y" : "N");
        alert.setProperty("description", description);
        alert.setProperty("message", message);
        String newIncidentid = "";
        try {
            PropertyList props = this.sendCommandToLIMS("COMMAND_RAISEALERT", alert);
            newIncidentid = props.getProperty("newkeyid1");
        }
        catch (SapphireException e) {
            this.logger.log("ERROR", "Untrapped exception trying to raise an alert: " + e.getMessage(), e);
        }
        return newIncidentid;
    }

    public boolean isPaused() {
        return this.isPaused;
    }

    public int getDefaultInstrumentPollInterval() {
        return this.defaultInstrumentPollInterval;
    }

    public boolean isDisabled() {
        return this.isDisabled;
    }

    public boolean isHashable() {
        return this.isHashable;
    }

    public boolean isShuttingDownCollectors() {
        return this.isShuttingDownCollectors;
    }

    public int getWaitForIsolatedModeSeconds() {
        return this.waitForIsolatedMode;
    }

    public int getHashMaxSizeGB() {
        return this.hashMaxSizeGB;
    }

    public Path getStoragePathLocal() {
        return this.storagePathLocal;
    }

    public void beginOperations() {
        if (this.fileCollectorService != null || this.fileEmulatorService != null) {
            this.logger.log("STARTUP", "Checking to make sure all threads are shutdown.");
            this.shutdownCollectorTypes();
        }
        this.caretakerInstrumentRunnableCheck.clear();
        if (!this.isDisabled) {
            this.isShuttingDownCollectors = false;
            if (this.collectorTypeInstances.size() > 0) {
                int collectorCount = 0;
                int emulatorCount = 0;
                for (BaseCollectorType collectorTypeInstance : this.collectorTypeInstances) {
                    if (collectorTypeInstance.isCollectionEnabled()) {
                        ++collectorCount;
                    }
                    if (!collectorTypeInstance.isEmulatorEnabled()) continue;
                    ++emulatorCount;
                }
                this.fileCollectorService = new MonitoredThreadPoolExecutor(this.corePoolSize > 0 ? this.corePoolSize : (collectorCount > this.MAX_CORE_SIZE ? this.MAX_CORE_SIZE : collectorCount), this.metricRegistry, "InstrumentCollectors", ThreadUtil.getThreadFactory(this.databaseid, "sdms-" + this.collectorid + "-collector"));
                this.fileEmulatorService = new MonitoredThreadPoolExecutor(this.corePoolSize > 0 ? this.corePoolSize : (emulatorCount > this.MAX_CORE_SIZE ? this.MAX_CORE_SIZE : emulatorCount), this.metricRegistry, "InstrumentCollectors", ThreadUtil.getThreadFactory(this.databaseid, "sdms-" + this.collectorid + "-emulator"));
                this.fileCollectorService.setRemoveOnCancelPolicy(true);
                this.fileEmulatorService.setRemoveOnCancelPolicy(true);
                for (BaseCollectorType collectorTypeInstance : this.collectorTypeInstances) {
                    this.startCollectorTypeInstanceRunning(collectorTypeInstance);
                }
            }
        }
    }

    private void startCollectorTypeInstanceRunning(BaseCollectorType collectorTypeInstance) {
        int interval;
        ScheduledFuture<?> future;
        Runnable runnable;
        collectorTypeInstance.setShuttingDown(false);
        if (collectorTypeInstance.isEmulatorEnabled()) {
            runnable = new EmulatorRunnable(this, collectorTypeInstance);
            if (collectorTypeInstance.isContinuousOperation()) {
                future = this.fileEmulatorService.scheduleWithFixedDelay(runnable, 0L, 2L, TimeUnit.SECONDS);
            } else {
                interval = collectorTypeInstance.getEmulatorPollInterval();
                future = this.fileEmulatorService.scheduleWithFixedDelay(runnable, 0L, interval, TimeUnit.SECONDS);
            }
            collectorTypeInstance.setEmulatorFuture(future);
        }
        if (collectorTypeInstance.isCollectionEnabled()) {
            runnable = new CollectionRunnable(this, collectorTypeInstance);
            if (collectorTypeInstance.isContinuousOperation()) {
                future = this.fileCollectorService.scheduleWithFixedDelay(runnable, 0L, 2L, TimeUnit.SECONDS);
            } else {
                interval = collectorTypeInstance.getCollectionPollInterval();
                future = this.fileCollectorService.scheduleWithFixedDelay(runnable, 0L, interval, TimeUnit.SECONDS);
            }
            collectorTypeInstance.setCollectionFuture(future);
        }
    }

    public boolean shutdown() {
        this.logger.log("COLLECTOR", "Shutting down");
        boolean gracefulShutdown = this.shutdownCollectorTypes();
        if (!gracefulShutdown) {
            try {
                Thread.sleep(this.pingIntervalSeconds * 1000 * 3);
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
        }
        this.utilityService.shutdownNow();
        this.utilityService = null;
        this.logger.log("SHUTDOWN", "Shut down complete");
        return true;
    }

    private boolean shutdownCollectorTypes() {
        this.isShuttingDownCollectors = true;
        boolean gracefullShutdown = true;
        for (BaseCollectorType collectorTypeInstance : this.collectorTypeInstances) {
            ScheduledFuture collectionFuture;
            this.logger.log("SHUTDOWN", "Shutting down instrument " + collectorTypeInstance.getInstrumentid());
            collectorTypeInstance.setShuttingDown(true);
            ScheduledFuture emulatorFuture = collectorTypeInstance.getEmulatorFuture();
            if (emulatorFuture != null) {
                try {
                    this.logger.log("SHUTDOWN", "  > Shutting down emulator future");
                    this.gracefullyStopFuture(emulatorFuture, collectorTypeInstance, this.shutdownTimeoutSeconds);
                }
                catch (SapphireException e) {
                    gracefullShutdown = false;
                    this.raiseCollectorAlert("SDMS Shutdown", "Failure", "Failed to shutdown emulator gracefully.", "Emualtor failed to shutdown gracefully. Thread was forcefully terminated. Check log for details.", false);
                }
            }
            if ((collectionFuture = collectorTypeInstance.getCollectionFuture()) == null) continue;
            try {
                this.logger.log("SHUTDOWN", "  > Shutting down collector future");
                this.gracefullyStopFuture(collectionFuture, collectorTypeInstance, this.shutdownTimeoutSeconds);
            }
            catch (SapphireException e) {
                gracefullShutdown = false;
                this.raiseCollectorAlert("SDMS Shutdown", "Failure", "Failed to shutdown collector gracefully.", "Collector failed to shutdown gracefully. Thread was forcefully terminated. Check log for details.", false);
            }
        }
        if (this.fileEmulatorService != null) {
            this.fileEmulatorService.shutdownNow();
        }
        if (this.fileCollectorService != null) {
            this.fileCollectorService.shutdownNow();
        }
        this.fileCollectorService = null;
        this.fileEmulatorService = null;
        return gracefullShutdown;
    }

    private void gracefullyStopFuture(ScheduledFuture future, BaseCollectorType collectorTypeInstance, int timeoutSeconds) throws SapphireException {
        if (future != null) {
            while (collectorTypeInstance.isCollecting() || collectorTypeInstance.isEmulating()) {
                this.logger.log("SHUTDOWN", "  > > Still " + (collectorTypeInstance.isCollecting() ? "collecting" : "emulating") + "...");
                try {
                    Thread.sleep(100L);
                }
                catch (InterruptedException interruptedException) {}
            }
            future.cancel(false);
            try {
                Thread.sleep(100L);
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
            long start = System.currentTimeMillis();
            long stopAfter = start + (long)(timeoutSeconds * 1000);
            while (!future.isDone() && System.currentTimeMillis() < stopAfter) {
                this.logger.log("SHUTDOWN", "  > > Future is still active. Waiting...");
                try {
                    Thread.sleep(100L);
                }
                catch (InterruptedException interruptedException) {}
            }
            if (future.isDone()) {
                this.logger.log("SHUTDOWN", "  > > Future done. Thread shutdown complete");
            } else {
                this.logger.log("SHUTDOWN", "  > > Future NOT done. Forcing thread shutdown.");
                future.cancel(true);
                throw new SapphireException("Failed to shutdown Future. Thread terminated anyway.");
            }
        }
    }

    public void logStartup(String message) {
        this.startupLog.append(this.startupLog.length() > 0 ? "!|!" : "").append(message);
        this.logger.log("STARTUP", message);
    }

    public boolean isStorageModeDirect() {
        return this.isStorageModeDirect;
    }

    public boolean isInternal() {
        return this.isInternal;
    }

    public PropertyList sendCommandToLIMS(String command, PropertyList props) throws SapphireException {
        return this.container.sendCommandToLIMS(command, props);
    }

    public JSONObject getState() {
        JSONObject state = new JSONObject();
        try {
            state.put("collectorid", this.collectorid);
            state.put("paused", this.isPaused ? "Y" : "N");
            state.put("disabled", this.isDisabled ? "Y" : "N");
            state.put("corepoolsize", this.fileCollectorService == null ? 0 : this.fileCollectorService.getCorePoolSize());
            state.put("activecount", this.fileCollectorService == null ? 0 : this.fileCollectorService.getActiveCount());
            state.put("pingrate", this.pingRunnable == null ? 0 : this.pingRunnable.getRate());
            JSONArray runnables = new JSONArray();
            state.put("runnables", runnables);
            for (BaseCollectorType collectorTypeInstance : this.collectorTypeInstances) {
                JSONObject runnable = new JSONObject();
                collectorTypeInstance.getCollectorTypeState(runnable);
                BaseCollectorType emulator = this.getEmulatorRunnable(collectorTypeInstance.getInstrumentid());
                if (emulator != null) {
                    emulator.getEmulatorState(runnable);
                }
                runnables.put(runnable);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return state;
    }

    private void startstopEmulator(String instrumentid) {
        BaseCollectorType emulator = this.getEmulatorRunnable(instrumentid);
        if (emulator != null) {
            emulator.startstopEmulator();
        }
    }

    private void triggerEmulator(String instrumentid) throws Exception {
        BaseCollectorType emulator = this.getEmulatorRunnable(instrumentid);
        if (emulator != null) {
            long start = System.currentTimeMillis();
            emulator.setEmulating(true);
            boolean emulated = emulator.doRunEmulator();
            emulator.setEmulating(false);
            if (emulated) {
                emulator.getMetrics().updateEmulatorTimer(System.currentTimeMillis() - start);
            }
        }
    }

    private JSONObject getInstrumentState(String instrumentid) {
        JSONObject state = new JSONObject();
        BaseCollectorType collectorTypeInstance = this.instrumentCollectorTypeInstance.get(instrumentid);
        if (collectorTypeInstance != null) {
            collectorTypeInstance.getCollectorTypeState(state);
            BaseCollectorType emulator = this.getEmulatorRunnable(instrumentid);
            if (emulator != null) {
                emulator.getEmulatorState(state);
            }
        }
        return state;
    }

    private BaseCollectorType getEmulatorRunnable(String instrumentid) {
        for (BaseCollectorType collectorTypeInstance : this.collectorTypeInstances) {
            if (!collectorTypeInstance.getInstrumentid().equals(instrumentid) || !collectorTypeInstance.isEmulatorEnabled()) continue;
            return collectorTypeInstance;
        }
        return null;
    }

    private BaseCollectorType getCollectorTypeInstance(String instrumentid) {
        for (BaseCollectorType collectorTypeInstance : this.collectorTypeInstances) {
            if (!collectorTypeInstance.getInstrumentid().equals(instrumentid)) continue;
            return collectorTypeInstance;
        }
        return null;
    }

    public String getCollectorid() {
        return this.collectorid;
    }

    public void startPinging() {
        if (this.utilityService == null) {
            this.utilityService = new MonitoredThreadPoolExecutor(3, this.metricRegistry, "Collector Utilities", ThreadUtil.getThreadFactory(this.databaseid, "sdms-" + this.collectorid + "-internals"));
        }
        this.pingRunnable = new PingRunnable(this.pingIntervalSeconds, this.fastPingIntervalSeconds);
        this.pingRunnable.setSlowRate();
        this.utilityService.schedule(this.pingRunnable, 0L, TimeUnit.SECONDS);
        this.logger.log("CARETAKER", "Caretaker started");
        CaretakerRunnable caretakerRunnable = new CaretakerRunnable();
        this.utilityService.scheduleWithFixedDelay(caretakerRunnable, 2L, 10L, TimeUnit.SECONDS);
    }

    public String getCollectorRuntimeProperty(String propertyid) {
        return this.getCollectorRuntimeProperty(propertyid, "");
    }

    public String getCollectorRuntimeProperty(String propertyid, String defaultValue) {
        return this.runtimeProperties.getProperty(propertyid, defaultValue);
    }

    public String getInstrumentRuntimeProperty(String instrumentid, String propertyid) {
        return this.getInstrumentRuntimeProperty(instrumentid, propertyid, "");
    }

    public String getInstrumentRuntimeProperty(String instrumentid, String propertyid, String defaultValue) {
        PropertyListCollection instruments = this.runtimeProperties.getCollectionNotNull("instruments");
        PropertyList instrumentProps = instruments.find("_instrumentid", instrumentid);
        if (instrumentProps == null) {
            return defaultValue;
        }
        return instrumentProps.getProperty(propertyid, defaultValue);
    }

    public PropertyList getInstrumentRuntimeProperties(String instrumentid) {
        PropertyListCollection instruments = this.runtimeProperties.getCollectionNotNull("instruments");
        PropertyList instrumentProps = instruments.find("_instrumentid", instrumentid);
        if (instrumentProps == null) {
            instrumentProps = new PropertyList();
            instrumentProps.setProperty("_instrumentid", instrumentid);
            instruments.add(instrumentProps);
        }
        return instrumentProps;
    }

    public void setCollectorRuntimeProperty(String propertyid, String value) throws SapphireException, ExternalAppException {
        this.setRuntimeProperty("", propertyid, value);
    }

    public void setInstrumentRuntimeProperty(String instrumentid, String propertyid, String value) throws SapphireException {
        this.setRuntimeProperty(instrumentid, propertyid, value);
    }

    private void setRuntimeProperty(String instrumentid, String propertyid, String value) throws SapphireException {
        if (instrumentid != null && instrumentid.length() > 0) {
            PropertyListCollection instruments = this.runtimeProperties.getCollectionNotNull("instruments");
            PropertyList instrumentProps = instruments.find("_instrumentid", instrumentid);
            if (instrumentProps == null) {
                instrumentProps = new PropertyList();
                instrumentProps.setProperty("_instrumentid", instrumentid);
                instruments.add(instrumentProps);
            }
            if (value == null || value.length() == 0) {
                instrumentProps.remove(propertyid);
            } else {
                instrumentProps.setProperty(propertyid, value);
            }
        } else if (value == null || value.length() == 0) {
            this.runtimeProperties.remove(value);
        } else {
            this.runtimeProperties.setProperty(propertyid, value);
        }
        PropertyList commandRequest = new PropertyList();
        this.runtimeProperties.setProperty("_lastupdate", "" + System.currentTimeMillis());
        commandRequest.setProperty("collectorid", this.collectorid);
        commandRequest.setProperty("runtimeproperties", this.runtimeProperties);
        this.sendCommandToLIMS("COMMAND_SAVERUNTIMEPROPERTIES", commandRequest);
    }

    private void rebootInstrument(String instrumentid, String jsonConfig) {
        String message;
        this.caretakerInstrumentRunnableCheck.clear();
        BaseCollectorType oldCollectorTypeInstance = this.getCollectorTypeInstance(instrumentid);
        oldCollectorTypeInstance.setShuttingDown(true);
        boolean shutdownGracefully = true;
        try {
            if (oldCollectorTypeInstance != null) {
                ScheduledFuture emulatorFuture = oldCollectorTypeInstance.getEmulatorFuture();
                this.gracefullyStopFuture(emulatorFuture, oldCollectorTypeInstance, this.shutdownTimeoutSeconds);
                ScheduledFuture collectionFuture = oldCollectorTypeInstance.getCollectionFuture();
                this.gracefullyStopFuture(collectionFuture, oldCollectorTypeInstance, this.shutdownTimeoutSeconds);
                this.collectorTypeInstances.remove(oldCollectorTypeInstance);
                this.instrumentCollectorTypeInstance.put(instrumentid, null);
            }
        }
        catch (SapphireException e) {
            shutdownGracefully = false;
            message = "Failed to shutdown instrument " + instrumentid + ". Reason: " + e.getMessage();
            this.raiseInstrumentAlert(instrumentid, "SDMS Startup", "Failure", "Failed to shutdown instrument. Check log for details.", message, false);
        }
        if (shutdownGracefully) {
            try {
                DataSet instrument = new DataSet(new JSONObject(jsonConfig));
                boolean isInstrumentPaused = instrument.getValue(0, "sdmsspausedflag", "N").equalsIgnoreCase("Y");
                String objectname = instrument.getValue(0, "_objectname");
                String collectorTypeTree = instrument.getValue(0, "_collectortype");
                if (objectname.length() > 0) {
                    PropertyList collectorTypeProps = new PropertyList();
                    collectorTypeProps.setPropertyList(instrument.getValue(0, "_collectorrules", "<propertylist />"));
                    String configHash = instrument.getValue(0, "_confighash");
                    BaseCollectorType newCollectorTypeInstance = (BaseCollectorType)Class.forName(objectname).newInstance();
                    try {
                        newCollectorTypeInstance.init(this, instrumentid, collectorTypeTree, this.metricRegistry, new LogEntryList(this, this.maxLogAgeHours * 3600L * 1000L, this.maxLogSize));
                        newCollectorTypeInstance.setInstrumentPaused(isInstrumentPaused);
                        newCollectorTypeInstance.setConfigHash(configHash);
                        newCollectorTypeInstance.configure(collectorTypeProps);
                        this.collectorTypeInstances.add(newCollectorTypeInstance);
                        this.instrumentCollectorTypeInstance.put(instrumentid, newCollectorTypeInstance);
                        this.startCollectorTypeInstanceRunning(newCollectorTypeInstance);
                    }
                    catch (Exception e) {
                        String message2 = "Failed to start instrument " + instrumentid + ". Reason: " + e.getMessage();
                        this.raiseInstrumentAlert(instrumentid, "SDMS Startup", "Failure", "Failed to start instrument", message2, false);
                    }
                }
            }
            catch (Exception e) {
                message = "Failed to start instrument " + instrumentid + ". Reason: " + e.getMessage();
                this.raiseInstrumentAlert(instrumentid, "SDMS Startup", "Failure", "Failed to start instrument", message, false);
            }
        }
    }

    private class CaretakerRunnable
    implements Runnable {
        boolean hasLoggedDiskspaceError = false;
        long lastMindiskspaceCheck = System.currentTimeMillis();

        private CaretakerRunnable() {
        }

        @Override
        public void run() {
            try {
                Trace.startThreadMDCByDatabaseid(SDMSCollector.this.databaseid, "SDMSCollector");
                if (!this.hasLoggedDiskspaceError && SDMSCollector.this.minDiskSpaceGb > 0 && System.currentTimeMillis() > this.lastMindiskspaceCheck + (long)(1000 * SDMSCollector.this.minDiskSpaceGbSeconds) && SDMSCollector.this.storagePathLocal != null && SDMSCollector.this.storagePathLocal.toFile().exists()) {
                    try {
                        long usablespace = SDMSCollector.this.storagePathLocal.toFile().getUsableSpace() / 1024L / 1024L / 1024L;
                        if (usablespace < (long)SDMSCollector.this.minDiskSpaceGb) {
                            String newIncidentid = SDMSCollector.this.raiseCollectorAlert("SDMS Collector State", "Warning", "Running out of disk space", "There are only " + usablespace + "Gb of disk space available", false);
                            this.hasLoggedDiskspaceError = newIncidentid.length() > 0;
                        }
                    }
                    catch (Exception e) {
                        this.hasLoggedDiskspaceError = true;
                        SDMSCollector.this.logger.log("ERROR", "Failed to determine the amound of disk-space on local storage path " + SDMSCollector.this.storagePathLocal + ": " + e.getMessage(), e);
                    }
                    this.lastMindiskspaceCheck = System.currentTimeMillis();
                }
                for (BaseCollectorType collectorTypeInstance : SDMSCollector.this.collectorTypeInstances) {
                    String instrumentid = collectorTypeInstance.getInstrumentid();
                    if (SDMSCollector.this.isPaused || collectorTypeInstance.isInstrumentPaused() || SDMSCollector.this.isDisabled || SDMSCollector.this.isShuttingDownCollectors) continue;
                    long lastCollectorRun = collectorTypeInstance.getLastCollectorRunTime();
                    int pollInterval = collectorTypeInstance.getCollectionPollInterval();
                    if (lastCollectorRun > 0L && pollInterval > 0) {
                        int threshold;
                        long secondsSinceLastRun = (System.currentTimeMillis() - lastCollectorRun) / 1000L;
                        int n = threshold = pollInterval * SDMSCollector.this.missedPollIntervalCount < 30 ? 30 : pollInterval * SDMSCollector.this.missedPollIntervalCount;
                        if (secondsSinceLastRun > (long)threshold) {
                            SDMSCollector.this.raiseInstrumentAlert(instrumentid, "SDMS Collection", "Warning", "Collection polling appears to have stopped", "Caretaker determined that the Collection routine has not been trigger for " + secondsSinceLastRun + "s", false);
                        }
                    }
                    long lastWait = collectorTypeInstance.getLastWaitBetweenTriggers();
                    if (pollInterval <= 0 || lastWait <= (long)(pollInterval * 2)) continue;
                    SDMSCollector.this.raiseCollectorAlert("SDMS Collection", "Warning", "Collector appears to be thread bound", "Caretaker determined an instrument had a wait between triggers twice as long as expected. This might indicate that the Collector requires more threads or is overloaded.", false);
                }
            }
            catch (Throwable e) {
                SDMSCollector.this.logger.log("COLLECTOR", "Caretaker failed. Uh oh. " + e.getMessage());
            }
        }
    }

    private class PingRunnable
    implements Runnable {
        private int slowRate;
        private int fastRate;
        boolean isFastRate = false;
        boolean firstPing = true;

        public PingRunnable(int slowRate, int fastRate) {
            this.slowRate = slowRate;
            this.fastRate = fastRate;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void run() {
            Trace.startThreadMDCByDatabaseid(SDMSCollector.this.databaseid, "SDMSCollector");
            try {
                PropertyList commandRequest = new PropertyList();
                if (SDMSCollector.this.collectorid != null && SDMSCollector.this.collectorid.length() > 0) {
                    commandRequest.setProperty("collectorid", SDMSCollector.this.collectorid);
                }
                if (this.firstPing) {
                    commandRequest.setProperty("firstping", "Y");
                    commandRequest.setProperty("actualhostname", SDMSCollector.this.container.getActualHostname());
                    PropertyList startupState = new PropertyList();
                    commandRequest.setProperty("startupstate", startupState);
                    startupState.setProperty("internalflag", SDMSCollector.this.isInternal ? "Y" : "N");
                    startupState.setProperty("startupdate", "" + Calendar.getInstance().getTimeInMillis());
                    startupState.setProperty("labvantageversion", Build.getVersion());
                    startupState.setProperty("labvantagebuild", Build.getBuild());
                    startupState.setProperty("osname", System.getProperty("os.name"));
                    startupState.setProperty("platform", System.getProperty("os.name").toLowerCase().startsWith("window") ? "windows" : "unix");
                    startupState.setProperty("startuplog", SDMSCollector.this.startupLog.toString());
                    startupState.setProperty("confighash", SDMSCollector.this.collectorConfigHash);
                    PropertyListCollection instruments = new PropertyListCollection();
                    startupState.setProperty("instruments", instruments);
                    for (Object collectorTypeInstance : SDMSCollector.this.collectorTypeInstances) {
                        PropertyList instrument = new PropertyList();
                        instruments.add(instrument);
                        instrument.setProperty("instrumentid", ((BaseCollectorType)collectorTypeInstance).getInstrumentid());
                        instrument.setProperty("confighash", ((BaseCollectorType)collectorTypeInstance).getConfigHash());
                    }
                    SDMSCollector.this.container.setStartupStateProperties(startupState);
                    this.firstPing = false;
                }
                PropertyList commandResponse = null;
                try {
                    commandResponse = SDMSCollector.this.sendCommandToLIMS("COMMAND_PING", commandRequest);
                }
                catch (Exception e) {
                    SDMSCollector.this.logger.log("COLLECTOR", "WARNING: Failed to send PING command to LIMS.");
                }
                if (commandResponse == null) {
                    this.setSlowRate();
                } else {
                    PropertyListCollection collectorInstruments = commandResponse.getCollectionNotNull("collectorinstruments");
                    if (collectorInstruments != null && collectorInstruments.size() > 0) {
                        for (BaseCollectorType collectorTypeInstance : SDMSCollector.this.collectorTypeInstances) {
                            PropertyList instrument = collectorInstruments.find("instrumentid", collectorTypeInstance.getInstrumentid());
                            if (instrument == null || instrument.size() <= 0) continue;
                            boolean instrumentPaused = instrument.getProperty("sdmspausedflag", "N").equalsIgnoreCase("Y");
                            if (collectorTypeInstance.isInstrumentPaused() && !instrumentPaused) {
                                collectorTypeInstance.setInstrumentPaused(instrumentPaused);
                                SDMSCollector.this.logger.log("COLLECTOR", "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                                SDMSCollector.this.logger.log("COLLECTOR", "++ Instrument has been UNPAUSED. Collecting will resume ++");
                                SDMSCollector.this.logger.log("COLLECTOR", "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                                continue;
                            }
                            if (collectorTypeInstance.isInstrumentPaused() || !instrumentPaused) continue;
                            collectorTypeInstance.setInstrumentPaused(instrumentPaused);
                            SDMSCollector.this.logger.log("COLLECTOR", "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                            SDMSCollector.this.logger.log("COLLECTOR", "++ Instrument has been PAUSED. Collecting will be halted ++");
                            SDMSCollector.this.logger.log("COLLECTOR", "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                        }
                    }
                    boolean disabledRequired = commandResponse.getProperty("disabledflag", "N").equals("Y");
                    if (SDMSCollector.this.isDisabled && !disabledRequired) {
                        SDMSCollector.this.isDisabled = false;
                        SDMSCollector.this.logger.log("COLLECTOR", "+++++++++++++++++++++++++++++++++++++++++++++++++");
                        SDMSCollector.this.logger.log("COLLECTOR", "++ Collector is ENABLED. Collecting will begin ++");
                        SDMSCollector.this.logger.log("COLLECTOR", "+++++++++++++++++++++++++++++++++++++++++++++++++");
                        SDMSCollector.this.beginOperations();
                    } else if (!SDMSCollector.this.isDisabled && disabledRequired) {
                        SDMSCollector.this.isDisabled = true;
                        SDMSCollector.this.logger.log("COLLECTOR", "++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                        SDMSCollector.this.logger.log("COLLECTOR", "++ Collector is DISABLED. Collecting will not start ++");
                        SDMSCollector.this.logger.log("COLLECTOR", "++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                        SDMSCollector.this.shutdownCollectorTypes();
                    }
                    if (!SDMSCollector.this.isDisabled) {
                        if (SDMSCollector.this.isShuttingDownCollectors) {
                            // empty if block
                        }
                        boolean pauseRequired = commandResponse.getProperty("pausedflag", "N").equals("Y");
                        if (SDMSCollector.this.isPaused() && !pauseRequired) {
                            SDMSCollector.this.isPaused = false;
                            SDMSCollector.this.logger.log("COLLECTOR", "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                            SDMSCollector.this.logger.log("COLLECTOR", "++ Collector has been UNPAUSED. Collecting will resume ++");
                            SDMSCollector.this.logger.log("COLLECTOR", "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                        } else if (!SDMSCollector.this.isPaused() && pauseRequired) {
                            SDMSCollector.this.isPaused = true;
                            SDMSCollector.this.logger.log("COLLECTOR", "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                            SDMSCollector.this.logger.log("COLLECTOR", "++ Collector has been PAUSED. Collecting will be halted ++");
                            SDMSCollector.this.logger.log("COLLECTOR", "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                        }
                        boolean isFastRequired = commandResponse.getProperty("fastping", "N").equals("Y");
                        if (isFastRequired) {
                            this.setFastRate();
                        } else {
                            this.setSlowRate();
                        }
                        ArrayList<CollectorCommandRunnable> terminalCommands = new ArrayList<CollectorCommandRunnable>();
                        PropertyListCollection collectorCommands = commandResponse.getCollectionNotNull("collectorcommands");
                        if (collectorCommands.size() > 0) {
                            for (int i = 0; i < collectorCommands.size(); ++i) {
                                PropertyList collectorCommand = collectorCommands.getPropertyList(i);
                                String commandCollectorid = collectorCommand.getProperty("collectorid");
                                if (!commandCollectorid.equals(SDMSCollector.this.collectorid)) continue;
                                String commandId = collectorCommand.getProperty("commandid");
                                String commandType = collectorCommand.getProperty("commandtype");
                                String commandParams = collectorCommand.getProperty("commandparams");
                                String commandFileContents = collectorCommand.getProperty("commandfilecontents");
                                String instrumentid = collectorCommand.getProperty("instrumentid");
                                CollectorCommandRunnable ccRunnable = new CollectorCommandRunnable(commandId, commandType, commandParams, commandFileContents, instrumentid);
                                if (commandType.equals("COLLECTORCOMMAND_REBOOT")) {
                                    terminalCommands.add(ccRunnable);
                                    continue;
                                }
                                SDMSCollector.this.utilityService.schedule(ccRunnable, 0L, TimeUnit.SECONDS);
                            }
                        }
                        for (CollectorCommandRunnable ccRunnable : terminalCommands) {
                            Thread thread = new Thread(ccRunnable);
                            thread.start();
                        }
                    }
                }
            }
            catch (Throwable e) {
                SDMSCollector.this.logger.log("COLLECTOR", e.getMessage(), e);
            }
            finally {
                SDMSCollector.this.utilityService.schedule(this, (long)this.getRate(), TimeUnit.SECONDS);
            }
        }

        public void setFastRate() {
            if (!this.isFastRate) {
                SDMSCollector.this.logger.log("COLLECTOR", "   > Pinging every " + this.fastRate + "s");
            }
            this.isFastRate = true;
        }

        public void setSlowRate() {
            if (this.isFastRate) {
                SDMSCollector.this.logger.log("COLLECTOR", "   > Pinging every " + this.slowRate + "s");
            }
            this.isFastRate = false;
        }

        private int getRate() {
            return this.isFastRate ? this.fastRate : this.slowRate;
        }
    }

    private class EmulatorRunnable
    implements Runnable {
        private BaseCollectorType collectorType;

        public EmulatorRunnable(SDMSCollector sdmsCollector, BaseCollectorType collectorType) {
            this.collectorType = collectorType;
        }

        @Override
        public void run() {
            Trace.startThreadMDCByDatabaseid(SDMSCollector.this.databaseid, "SDMSCollector");
            if (!(SDMSCollector.this.isPaused() || this.collectorType.isInstrumentPaused() || SDMSCollector.this.isDisabled() || SDMSCollector.this.isShuttingDownCollectors() || !this.collectorType.isEmulatorRunning())) {
                try {
                    long start = System.currentTimeMillis();
                    this.collectorType.setEmulating(true);
                    boolean emulated = this.collectorType.doRunEmulator();
                    this.collectorType.setEmulating(false);
                    if (emulated) {
                        this.collectorType.getMetrics().updateEmulatorTimer(System.currentTimeMillis() - start);
                    }
                }
                catch (Throwable e) {
                    this.collectorType.setEmulating(false);
                    SDMSCollector.this.logger.log("EMULATING", e.getMessage(), e);
                }
            }
        }
    }

    private class CollectionRunnable
    implements Runnable {
        private BaseCollectorType collectorTypeInstance;
        private FileSenderFactory fileSenderFactory = null;
        int runCount = 0;
        int collectedCount = 0;
        long lastExecutionFinishedMillis = 0L;

        public CollectionRunnable(SDMSCollector sdmsCollector, BaseCollectorType collectorType) {
            this.collectorTypeInstance = collectorType;
            this.fileSenderFactory = SDMSCollector.this.senderFactory;
        }

        @Override
        public void run() {
            String unique = this.collectorTypeInstance.getUniqueid();
            if (SDMSCollector.this.caretakerInstrumentRunnableCheck.containsKey(this.collectorTypeInstance.getInstrumentid())) {
                if (!unique.equals(this.collectorTypeInstance.getUniqueid())) {
                    SDMSCollector.this.raiseInstrumentAlert(this.collectorTypeInstance.getInstrumentid(), "SDMS Internal", "Failure", "Two simultaneous threads detected", "Caretaker supsect that the collector has two threads collecting from the same instrument", false);
                }
            } else {
                SDMSCollector.this.caretakerInstrumentRunnableCheck.put(this.collectorTypeInstance.getInstrumentid(), this.collectorTypeInstance.getUniqueid());
            }
            if (!(SDMSCollector.this.isPaused() || SDMSCollector.this.isDisabled() || SDMSCollector.this.isShuttingDownCollectors() || this.collectorTypeInstance.isInstrumentPaused() || this.collectorTypeInstance.isShuttingDown())) {
                try {
                    if (this.lastExecutionFinishedMillis > 0L) {
                        this.collectorTypeInstance.setLastWaitBetweenTriggers((System.currentTimeMillis() - this.lastExecutionFinishedMillis) / 1000L);
                    }
                    ++this.runCount;
                    Trace.startThreadMDCByDatabaseid(SDMSCollector.this.databaseid, "SDMSCollector");
                    long start = System.currentTimeMillis();
                    this.collectorTypeInstance.setLastCollectorRunTime(System.currentTimeMillis());
                    this.collectorTypeInstance.setCollecting(true);
                    boolean collected = this.collectorTypeInstance.doRunCollector(this.fileSenderFactory);
                    this.collectorTypeInstance.setCollecting(false);
                    if (collected) {
                        ++this.collectedCount;
                        this.collectorTypeInstance.getMetrics().updateCollectorTimer(System.currentTimeMillis() - start);
                    }
                }
                catch (Throwable e) {
                    this.collectorTypeInstance.setCollecting(false);
                    SDMSCollector.this.raiseInstrumentAlert(this.collectorTypeInstance.getInstrumentid(), "SDMS Collection", "Failure", "Untrapped exception caught when collecting: " + e.getMessage(), e.getMessage(), false);
                }
            }
            this.lastExecutionFinishedMillis = System.currentTimeMillis();
        }

        public BaseCollectorType getCollectorTypeInstance() {
            return this.collectorTypeInstance;
        }
    }

    private class CollectorCommandRunnable
    implements Runnable {
        private String commandId;
        private String commandType;
        private String commandParams;
        private String commandFileContents;
        private String instrumentid;

        public CollectorCommandRunnable(String commandId, String commandType, String commandParams, String commandFileContents, String instrumentid) {
            this.commandId = commandId;
            this.commandType = commandType;
            this.commandParams = commandParams;
            this.commandFileContents = commandFileContents;
            this.instrumentid = instrumentid;
        }

        @Override
        public void run() {
            try {
                Trace.startThreadMDCByDatabaseid(SDMSCollector.this.databaseid, "SDMSCollector");
                String replyMessage = "";
                String replyError = "";
                if (!this.commandType.equals("COLLECTORCOMMAND_GETCOLLECTORSTATE") && !this.commandType.equals("COLLECTORCOMMAND_GETINSTRUMENTSTATE")) {
                    SDMSCollector.this.logger.log("COLLECTOR", "Received Command: " + this.commandType + " (" + this.commandParams + ")");
                }
                switch (this.commandType) {
                    case "COLLECTORCOMMAND_GETCOLLECTORSTATE": {
                        JSONObject state = SDMSCollector.this.getState();
                        replyMessage = state.toString();
                        break;
                    }
                    case "COLLECTORCOMMAND_GETCOLLECTORLOG": {
                        JSONObject log = new JSONObject();
                        log.put("log", SDMSCollector.this.logger.toJSONArray());
                        replyMessage = log.toString();
                        break;
                    }
                    case "COLLECTORCOMMAND_GETINSTRUMENTSTATE": {
                        JSONObject state = SDMSCollector.this.getInstrumentState(this.instrumentid);
                        replyMessage = state.toString();
                        break;
                    }
                    case "COLLECTORCOMMAND_GETINSTRUMENTLOG": {
                        JSONObject log = new JSONObject();
                        log.put("log", SDMSCollector.this.getCollectorTypeInstance(this.instrumentid).getLogger().toJSONArray());
                        replyMessage = log.toString();
                        break;
                    }
                    case "COLLECTORCOMMAND_REBOOT": {
                        SDMSCollector.this.container.executeRebootCommand();
                        break;
                    }
                    case "COLLECTORCOMMAND_REBOOTINSTRUMENT": {
                        SDMSCollector.this.rebootInstrument(this.instrumentid, this.commandParams);
                        break;
                    }
                    case "COLLECTORCOMMAND_STARTSTOPEMULATOR": {
                        SDMSCollector.this.startstopEmulator(this.instrumentid);
                        break;
                    }
                    case "COLLECTORCOMMAND_TRIGGEREMULATOR": {
                        try {
                            SDMSCollector.this.triggerEmulator(this.instrumentid);
                        }
                        catch (Exception e) {
                            BaseCollectorType emulator = SDMSCollector.this.getEmulatorRunnable(this.instrumentid);
                            if (emulator != null) {
                                emulator.setEmulating(false);
                            }
                            replyError = "Failed to trigger emulator: " + e.getMessage();
                        }
                        break;
                    }
                    case "COLLECTORCOMMAND_DELIVERINSTRUMENTFILE": {
                        String contents = this.commandFileContents;
                        String filename = this.commandParams;
                        byte[] bytes = Base64.getDecoder().decode(contents);
                        BaseCollectorType collectorTypeInstance = (BaseCollectorType)SDMSCollector.this.instrumentCollectorTypeInstance.get(this.instrumentid);
                        if (collectorTypeInstance != null && collectorTypeInstance.isRunfileDeliveryEnabled()) {
                            try {
                                replyMessage = collectorTypeInstance.doDeliverRunFile(filename, bytes);
                            }
                            catch (SapphireException e) {
                                replyError = "ERROR:Failed to deliver run-file: " + e.getMessage();
                            }
                            break;
                        }
                        replyError = "ERROR: Attempted to deliver a file where none was expected. Please check Instrument. The Instrument property \"Enable Run-File Delivery\" needs to be set to Yes.";
                        break;
                    }
                    case "COLLECTORCOMMAND_UPGRADE": {
                        try {
                            String upgradeMode = this.commandParams;
                            SDMSCollector.this.container.upgrade(upgradeMode);
                            break;
                        }
                        catch (Exception e) {
                            replyError = "Failed to perform Upgrade: " + e.getMessage() + ". Resuming normal operation...";
                        }
                    }
                }
                if (replyMessage.length() > 0 || replyError.length() > 0) {
                    try {
                        PropertyList collectorCommandReply = new PropertyList();
                        collectorCommandReply.setProperty("collectorid", SDMSCollector.this.collectorid);
                        collectorCommandReply.setProperty("commandid", this.commandId);
                        collectorCommandReply.setProperty("instrumentid", this.instrumentid == null ? "" : this.instrumentid);
                        collectorCommandReply.setProperty("commandreply", replyMessage.length() > 0 ? replyMessage : replyError);
                        SDMSCollector.this.sendCommandToLIMS("COMMAND_COLLECTORCOMMAND_REPLY", collectorCommandReply);
                    }
                    catch (SapphireException sapphireException) {}
                }
            }
            catch (Throwable e) {
                SDMSCollector.this.logger.log("COLLECTOR", "Received command " + this.commandType, e);
            }
        }
    }
}

