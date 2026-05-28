/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.codahale.metrics.MetricRegistry
 *  com.codahale.metrics.Snapshot
 *  com.codahale.metrics.Timer
 *  com.codahale.metrics.Timer$Context
 */
package com.labvantage.sapphire.modules.sdms.collector.collectortypes;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.modules.sdms.collector.LogEntryList;
import com.labvantage.sapphire.modules.sdms.collector.SDMSCollector;
import com.labvantage.sapphire.modules.sdms.collector.storagemodes.FileSenderFactory;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public abstract class BaseCollectorType
implements SDMSConstants {
    private boolean isEmulatorRunning = true;
    protected SDMSCollector sdmsCollector;
    protected String instrumentid;
    protected boolean isInstrumentPaused = false;
    protected boolean isShuttingDown = false;
    private String collectorType;
    private Metrics metrics;
    protected LogEntryList logger;
    private StringBuilder collectionLog = new StringBuilder();
    private StringBuilder emulatorLog = new StringBuilder();
    private long starteddt;
    private String uniqueid;
    private long lastCollectorRunTime;
    private long lastWaitBetweenTriggers;
    private String configHash;
    private ScheduledFuture collectionFuture = null;
    private ScheduledFuture emulatorFuture = null;
    private boolean isCollecting = false;
    private boolean isEmulating = false;

    public void init(SDMSCollector sdmsCollector, String instrumentid, String collectorType, MetricRegistry metricRegistry, LogEntryList logger) {
        this.sdmsCollector = sdmsCollector;
        this.instrumentid = instrumentid;
        this.collectorType = collectorType;
        this.metrics = new Metrics(metricRegistry);
        this.logger = logger;
        this.starteddt = System.currentTimeMillis();
        this.uniqueid = "" + this.hashCode();
    }

    public String getUniqueid() {
        return this.uniqueid;
    }

    public LogEntryList getLogger() {
        return this.logger;
    }

    public long getLastCollectorRunTime() {
        return this.lastCollectorRunTime;
    }

    public void setLastCollectorRunTime(long lastCollectorRunTime) {
        this.lastCollectorRunTime = lastCollectorRunTime;
    }

    public void setShuttingDown(boolean shuttingDown) {
        this.isShuttingDown = shuttingDown;
    }

    public boolean isShuttingDown() {
        return this.isShuttingDown;
    }

    public void startCollectionLog(String message) {
        this.collectionLog = new StringBuilder();
    }

    public void resetCollectionLog() {
        this.collectionLog = new StringBuilder();
    }

    public void appendCollectionLog(String message) {
        this.logger.log("COLLECTING", message);
        this.collectionLog.append(this.collectionLog.length() > 0 ? "!|!" : "").append(message);
    }

    public String getCollectionLog() {
        return this.collectionLog.toString();
    }

    public void startEmulatorLog(String message) {
        this.emulatorLog = new StringBuilder();
    }

    public void appendEmulatorLog(String message) {
        this.logger.log("EMULATING", message);
        this.emulatorLog.append(this.collectionLog.length() > 0 ? "!|!" : "").append(message);
    }

    public String getEmulatorLog() {
        return this.emulatorLog.toString();
    }

    protected void logStartup(String message) {
        this.sdmsCollector.logStartup(message);
        this.logger.log("STARTUP", message);
    }

    public abstract void configure(PropertyList var1) throws SapphireException;

    public abstract int getCollectionPollInterval();

    public boolean isContinuousOperation() {
        return false;
    }

    public abstract int getEmulatorPollInterval();

    public abstract boolean isCollectionEnabled();

    public abstract boolean isRunfileDeliveryEnabled();

    public abstract boolean isEmulatorEnabled();

    public void setRuntimeProperty(String propertyid, String value) throws SapphireException {
        this.sdmsCollector.setInstrumentRuntimeProperty(this.instrumentid, propertyid, value);
    }

    public PropertyList getRuntimeProperties() {
        return this.sdmsCollector.getInstrumentRuntimeProperties(this.instrumentid);
    }

    public String getRuntimeProperty(String propertyid) {
        return this.getRuntimeProperty(propertyid, "");
    }

    public String getRuntimeProperty(String propertyid, String defaultValue) {
        return this.sdmsCollector.getInstrumentRuntimeProperty(this.instrumentid, propertyid, defaultValue);
    }

    public JSONObject getCollectorTypeState(JSONObject state) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss ");
            state.put("instrumentid", this.instrumentid);
            if (this.isShuttingDown()) {
                state.put("shuttingdown", "Y");
            }
            state.put("collectorinterval", this.getCollectionPollInterval());
            state.put("starteddt", this.starteddt);
            state.put("continuousoperation", this.isContinuousOperation() ? "Y" : "N");
            state.put("lastwait", this.getLastWaitBetweenTriggers());
            state.put("collectortype", this.getCollectorType());
            state.put("lastcapturedt", this.getLastCaptureDt() == null ? "" : sdf.format(this.getLastCaptureDt().getTime()));
            state.put("lastcapturetime", this.getLastCaptureDt() == null ? "" : "" + this.getLastCaptureDt().getTimeInMillis());
            state.put("lastcapturedesc", this.getLastCaptureDescription() == null ? "" : this.getLastCaptureDescription());
            state.put("laststoredesc", this.getLastStoreDescription() == null ? "" : this.getLastStoreDescription());
            state.put("runtimeproperties", this.getRuntimeProperties().toXMLString());
            Timer timer = this.metrics.getTimer("collectorTimer");
            Snapshot snapshot = timer.getSnapshot();
            DecimalFormat formatter = new DecimalFormat("0.000");
            state.put("collector.count", timer.getCount());
            state.put("collector.meanRate", formatter.format(timer.getMeanRate()));
            state.put("collector.oneMinuteRate", formatter.format(timer.getOneMinuteRate()));
            state.put("collector.fiveMinuteRate", formatter.format(timer.getFiveMinuteRate()));
            state.put("collector.fifteenMinuteRate", formatter.format(timer.getFifteenMinuteRate()));
            state.put("collector.min", formatter.format((double)snapshot.getMin() / 1.0E9));
            state.put("collector.max", formatter.format((double)snapshot.getMax() / 1.0E9));
            state.put("collector.mean", formatter.format(snapshot.getMean() / 1.0E9));
            state.put("collector.median", formatter.format(snapshot.getMedian() / 1.0E9));
            state.put("collector.stdev", formatter.format(snapshot.getStdDev() / 1.0E9));
            state.put("collector.75percentile", formatter.format(snapshot.get75thPercentile() / 1.0E9));
            state.put("collector.95percentile", formatter.format(snapshot.get95thPercentile() / 1.0E9));
            state.put("collector.98percentile", formatter.format(snapshot.get98thPercentile() / 1.0E9));
            state.put("collector.99percentile", formatter.format(snapshot.get99thPercentile() / 1.0E9));
            state.put("collector.999percentile", formatter.format(snapshot.get999thPercentile() / 1.0E9));
        }
        catch (Exception exception) {
            // empty catch block
        }
        return state;
    }

    public JSONObject getEmulatorState(JSONObject state) {
        try {
            state.put("isemulator", "Y");
            state.put("emulatorinterval", this.getEmulatorPollInterval());
            state.put("isemulatorrunning", this.isEmulatorRunning() ? "Y" : "N");
            state.put("lastemulatorlog", this.getEmulatorLog());
            Timer timer = this.metrics.getTimer("emulatorTimer");
            Snapshot snapshot = timer.getSnapshot();
            DecimalFormat formatter = new DecimalFormat("0.000");
            state.put("emulator.count", timer.getCount());
            state.put("emulator.meanRate", formatter.format(timer.getMeanRate()));
            state.put("emulator.min", formatter.format(snapshot.getMin() / 1000000000L));
            state.put("emulator.max", formatter.format(snapshot.getMax() / 1000000000L));
            state.put("emulator.mean", formatter.format(snapshot.getMean() / 1.0E9));
            state.put("emulator.stdev", formatter.format(snapshot.getStdDev() / 1.0E9));
        }
        catch (Exception exception) {
            // empty catch block
        }
        return state;
    }

    protected int getDefaultInstrumentPollInterval() {
        return this.sdmsCollector.getDefaultInstrumentPollInterval();
    }

    public String getInstrumentid() {
        return this.instrumentid;
    }

    public boolean isInstrumentPaused() {
        return this.isInstrumentPaused;
    }

    public void setInstrumentPaused(boolean isInstrumentPaused) {
        this.isInstrumentPaused = isInstrumentPaused;
    }

    public String getCollectorType() {
        return this.collectorType;
    }

    public void startstopEmulator() {
        this.isEmulatorRunning = !this.isEmulatorRunning;
    }

    public boolean isEmulatorRunning() {
        return this.isEmulatorRunning;
    }

    public abstract Calendar getLastCaptureDt();

    public abstract String getLastCaptureDescription();

    public abstract String getLastStoreDescription();

    public abstract boolean doRunCollector(FileSenderFactory var1) throws Exception;

    public abstract boolean doRunEmulator() throws Exception;

    public Metrics getMetrics() {
        return this.metrics;
    }

    public SDMSCollector getSdmsCollector() {
        return this.sdmsCollector;
    }

    public String doDeliverRunFile(String filename, byte[] bytes) throws SapphireException {
        return "";
    }

    public abstract List<String> getReportsForSDC(PropertyList var1, String var2);

    public void setLastWaitBetweenTriggers(long lastWaitBetweenTriggers) {
        this.lastWaitBetweenTriggers = lastWaitBetweenTriggers;
    }

    public long getLastWaitBetweenTriggers() {
        return this.lastWaitBetweenTriggers;
    }

    public void setConfigHash(String configHash) {
        this.configHash = configHash;
    }

    public String getConfigHash() {
        return this.configHash;
    }

    public void setCollectionFuture(ScheduledFuture collectionFuture) {
        this.collectionFuture = collectionFuture;
    }

    public ScheduledFuture getCollectionFuture() {
        return this.collectionFuture;
    }

    public void setEmulatorFuture(ScheduledFuture emulatorFuture) {
        this.emulatorFuture = emulatorFuture;
    }

    public ScheduledFuture getEmulatorFuture() {
        return this.emulatorFuture;
    }

    public void setCollecting(boolean isCollecting) {
        this.isCollecting = isCollecting;
    }

    public boolean isCollecting() {
        return this.isCollecting;
    }

    public void setEmulating(boolean isEmulating) {
        this.isEmulating = isEmulating;
    }

    public boolean isEmulating() {
        return this.isEmulating;
    }

    public class Metrics {
        private MetricRegistry metricRegistry;
        String metricsPrefix;
        private ThreadLocal<Timer.Context> senderTimer = new ThreadLocal();

        public Metrics(MetricRegistry metricRegistry) {
            this.metricRegistry = metricRegistry;
            this.metricsPrefix = MetricRegistry.name((String)"sdms.collector", (String[])new String[]{BaseCollectorType.this.sdmsCollector.getCollectorid(), BaseCollectorType.this.getInstrumentid()});
        }

        public Timer getTimer(String timerType) {
            return this.metricRegistry.timer(MetricRegistry.name((String)this.metricsPrefix, (String[])new String[]{timerType}));
        }

        public void startSenderTimer() {
            Timer timer = this.metricRegistry.timer(MetricRegistry.name((String)this.metricsPrefix, (String[])new String[]{"senderTimer"}));
            this.senderTimer.set(timer.time());
        }

        public void stopSenderTimer() {
            Timer.Context context = this.senderTimer.get();
            try {
                context.stop();
            }
            catch (Throwable t) {
                t.printStackTrace();
            }
        }

        public void updateEmulatorTimer(long millis) {
            Timer timer = this.metricRegistry.timer(MetricRegistry.name((String)this.metricsPrefix, (String[])new String[]{"emulatorTimer"}));
            timer.update(millis, TimeUnit.MILLISECONDS);
        }

        public void updateCollectorTimer(long millis) {
            Timer timer = this.metricRegistry.timer(MetricRegistry.name((String)this.metricsPrefix, (String[])new String[]{"collectorTimer"}));
            timer.update(millis, TimeUnit.MILLISECONDS);
        }
    }
}

