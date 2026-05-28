/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.codahale.metrics.MetricRegistry
 *  com.codahale.metrics.Snapshot
 *  com.codahale.metrics.Timer
 */
package com.labvantage.sapphire.admin.system.automation;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.labvantage.sapphire.admin.system.ThreadUtil;
import com.labvantage.sapphire.admin.system.automation.ExecuteToDoListEntry;
import com.labvantage.sapphire.admin.system.automation.LAMException;
import com.labvantage.sapphire.admin.system.automation.LAMScheduledRunnable;
import com.labvantage.sapphire.admin.system.automation.LAMStats;
import com.labvantage.sapphire.admin.system.automation.PollImmediate;
import com.labvantage.sapphire.admin.system.automation.runnables.Housekeeping;
import com.labvantage.sapphire.admin.system.automation.runnables.ImportDataCaptureFolders;
import com.labvantage.sapphire.admin.system.automation.runnables.Indexer;
import com.labvantage.sapphire.admin.system.automation.runnables.PollToDoList;
import com.labvantage.sapphire.admin.system.automation.runnables.ProcessServerCommands;
import com.labvantage.sapphire.admin.system.automation.runnables.ReallocateCollectors;
import com.labvantage.sapphire.admin.system.automation.runnables.Scheduler;
import com.labvantage.sapphire.admin.system.automation.runnables.SendServerCommands;
import com.labvantage.sapphire.admin.system.automation.runnables.ServerPing;
import com.labvantage.sapphire.admin.system.automation.runnables.StartStopCollectors;
import com.labvantage.sapphire.admin.system.automation.runnables.TaskScheduler;
import com.labvantage.sapphire.admin.system.automation.runnables.Timeout;
import com.labvantage.sapphire.admin.system.automation.runnables.UpdateStats;
import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.modules.sdms.collector.SDMSCollectorInternalHolder;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.platform.SapphireDatabase;
import com.labvantage.sapphire.services.AutomationService;
import com.labvantage.sapphire.services.SapphireService;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import sapphire.SapphireException;
import sapphire.util.StringUtil;

public class LAM
implements SDMSConstants {
    public final SapphireDatabase database;
    public static final int POLLER_SERVERPING = 1;
    public static final int POLLER_TODOLIST = 2;
    public static final int POLLER_UPDATESTATSHOURLY = 3;
    public static final int POLLER_UPDATESTATSDAILY = 4;
    public static final int POLLER_TASK = 5;
    public static final int POLLER_SCHEDULER = 6;
    public static final int POLLER_TIMEOUTS = 7;
    public static final int POLLER_HOUSEKEEPING = 8;
    public static final int POLLER_INDEXER = 9;
    public static final int POLLER_SENDSERVERCOMMAND = 10;
    public static final int POLLER_PROCESSSERVERCOMMAND = 11;
    public static final int POLLER_IMPORTDATACAPTURES = 12;
    public static final int POLLER_STARTSTOPCOLLECTORS = 13;
    public static final int POLLER_REALLOCATECOLLECTORS = 14;
    ScheduledThreadPoolExecutor pollerPool = null;
    ThreadPoolExecutor tdlPool = null;
    ThreadFactory pollerThreadFactory = null;
    ThreadFactory tdlThreadFactory = null;
    List<SDMSCollectorInternalHolder> sdmsCollectorHolders = new ArrayList<SDMSCollectorInternalHolder>();
    private int startupDelay = 0;
    HashMap<Integer, LAMScheduledRunnable> pollerRunnables = new HashMap();
    HashMap<Integer, ScheduledFuture<LAMScheduledRunnable>> pollerFutures = new HashMap();
    private final MetricRegistry metricRegistry = new MetricRegistry();
    private int bufferMillis = 100;

    public LAM(String databaseid) throws LAMException {
        Configuration instance;
        if (databaseid == null || databaseid.length() == 0) {
            throw new LAMException("No database provided");
        }
        try {
            instance = Configuration.getInstance();
        }
        catch (SapphireException e) {
            throw new LAMException("Unable to create Configuration instance: " + e.getMessage());
        }
        try {
            this.database = instance.getSapphireDatabase(databaseid);
        }
        catch (SapphireException e) {
            throw new LAMException("Database " + databaseid + " not found in Configuration class.");
        }
        this.pollerThreadFactory = ThreadUtil.getThreadFactory(databaseid, "lam-poller");
        this.tdlThreadFactory = ThreadUtil.getThreadFactory(databaseid, "lam-todolist");
    }

    public ScheduledThreadPoolExecutor getPollerPool() {
        return this.pollerPool;
    }

    public ThreadPoolExecutor getTdlPool() {
        return this.tdlPool;
    }

    public MetricRegistry getMetricRegistry() {
        return this.metricRegistry;
    }

    public void createPollerPool(String configSize, int defaultSize) {
        int size = LAM.getConfigInt(configSize, defaultSize);
        if (size > 0) {
            this.pollerPool = new ScheduledThreadPoolExecutor(size, this.pollerThreadFactory);
        }
    }

    public void createTDLPool(String configSize, int defaultSize) {
        int size = LAM.getConfigInt(configSize, defaultSize);
        if (size > 0) {
            this.tdlPool = new ThreadPoolExecutor(size, size, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), this.tdlThreadFactory);
        }
    }

    public LAMScheduledRunnable createPrimaryAutomationPoller(int type, String frequency, int defaultFrequency) throws LAMException {
        return this.createPoller(type, frequency, defaultFrequency, true, false);
    }

    public LAMScheduledRunnable createPoller(int type, String frequency, int defaultFrequency) throws LAMException {
        return this.createPoller(type, frequency, defaultFrequency, false, false);
    }

    public LAMScheduledRunnable createAnyAutomationPoller(int type, String frequency, int defaultFrequency) throws LAMException {
        return this.createPoller(type, frequency, defaultFrequency, false, true);
    }

    private LAMScheduledRunnable createPoller(int type, String frequency, int defaultFrequency, boolean isForPrimaryAutomationServerOnly, boolean isForAnyAutomationServer) throws LAMException {
        if (this.pollerRunnables.get(type) != null || this.pollerFutures.get(type) != null) {
            throw new LAMException("Poller is already running");
        }
        LAMScheduledRunnable runnable = null;
        int interval = LAM.getConfigInt(frequency, defaultFrequency);
        if (interval > 0) {
            switch (type) {
                case 2: {
                    runnable = new PollToDoList(this);
                    break;
                }
                case 5: {
                    runnable = new TaskScheduler(this);
                    break;
                }
                case 6: {
                    runnable = new Scheduler(this);
                    break;
                }
                case 7: {
                    runnable = new Timeout(this);
                    break;
                }
                case 8: {
                    runnable = new Housekeeping(this);
                    break;
                }
                case 1: {
                    runnable = new ServerPing(this);
                    break;
                }
                case 10: {
                    runnable = new SendServerCommands(this);
                    break;
                }
                case 11: {
                    runnable = new ProcessServerCommands(this);
                    break;
                }
                case 3: {
                    runnable = new UpdateStats(this, 1);
                    break;
                }
                case 4: {
                    runnable = new UpdateStats(this, 2);
                    break;
                }
                case 9: {
                    runnable = new Indexer(this);
                    break;
                }
                case 12: {
                    runnable = new ImportDataCaptureFolders(this);
                    break;
                }
                case 13: {
                    runnable = new StartStopCollectors(this);
                    break;
                }
                case 14: {
                    runnable = new ReallocateCollectors(this);
                    break;
                }
                default: {
                    throw new LAMException("Unrecognized type");
                }
            }
            runnable.setInterval(interval);
            runnable.setIsForPrimaryAutomationServerOnly(isForPrimaryAutomationServerOnly);
            runnable.setIsForAnyAutomationServer(isForAnyAutomationServer);
            ScheduledFuture<?> future = this.pollerPool.scheduleWithFixedDelay(runnable, this.startupDelay, interval, TimeUnit.SECONDS);
            this.pollerRunnables.put(type, runnable);
            this.pollerFutures.put(type, future);
        }
        return runnable;
    }

    public void destroyPoller(int type) {
        ScheduledFuture<LAMScheduledRunnable> future = this.pollerFutures.get(type);
        future.cancel(true);
        this.pollerFutures.remove(type);
        this.pollerRunnables.remove(type);
    }

    public void pausePoller(int type) {
        LAMScheduledRunnable runnable = this.pollerRunnables.get(type);
        runnable.setActive(false);
    }

    public void resumePoller(int type) {
        LAMScheduledRunnable runnable = this.pollerRunnables.get(type);
        runnable.setActive(true);
        if (type == 2) {
            this.scheduledPollImmediate();
        }
    }

    public boolean isPollerActive(int type) {
        LAMScheduledRunnable runnable = this.pollerRunnables.get(type);
        return runnable.isActive();
    }

    public void processToDoListEntries(String todolistids) {
        if (todolistids != null) {
            String[] todolistid = StringUtil.split(todolistids, ";");
            for (int i = 0; i < todolistid.length; ++i) {
                this.tdlPool.submit(new ExecuteToDoListEntry(this, todolistid[i]));
            }
            this.tdlPool.submit(new PollImmediate(this, this.bufferMillis));
        }
    }

    public void stopAll() {
        for (SDMSCollectorInternalHolder collectorHolder : this.sdmsCollectorHolders) {
            collectorHolder.shutdown();
        }
        this.sdmsCollectorHolders.clear();
        if (this.pollerPool != null) {
            this.pollerPool.shutdownNow();
        }
        if (this.tdlPool != null) {
            this.tdlPool.shutdownNow();
        }
        this.pollerRunnables.clear();
        this.pollerFutures.clear();
    }

    public void scheduledPollImmediate() {
        if (this.tdlPool.getQueue().isEmpty()) {
            this.pollImmediate();
        }
    }

    public void pollImmediate() {
        LAMScheduledRunnable runnable = this.pollerRunnables.get(2);
        if (runnable.isActive()) {
            this.tdlPool.submit(new PollImmediate(this, this.bufferMillis));
        }
    }

    public LAMStats getStats() {
        LAMStats stats = new LAMStats();
        stats.isPrimaryAutomationServer = this.isPrimaryAutomationServer();
        stats.isAutomationServer = this.isAutomationServer();
        stats.poolSize = this.tdlPool.getPoolSize();
        stats.totalTasksCompleted = ExecuteToDoListEntry.getExecutionCount();
        stats.activeThreads = this.tdlPool.getActiveCount();
        SortedMap timers = this.metricRegistry.getTimers();
        for (String name : timers.keySet()) {
            Timer timer = (Timer)timers.get(name);
            Snapshot snapshot = timer.getSnapshot();
            DecimalFormat formatter = new DecimalFormat("0.000");
            long count = timer.getCount();
            String oneRate = formatter.format(timer.getOneMinuteRate());
            String fiveRate = formatter.format(timer.getFiveMinuteRate());
            String fifteenRate = formatter.format(timer.getFifteenMinuteRate());
            String mean = formatter.format(snapshot.getMean() / 1000.0);
            String stdev = formatter.format(snapshot.getStdDev() / 1000.0);
            String nintyfive = formatter.format(snapshot.get95thPercentile() / 1000.0);
            stats.addTimer(name, count, oneRate, fiveRate, fifteenRate, mean, stdev, nintyfive);
        }
        for (LAMScheduledRunnable runnable : this.pollerRunnables.values()) {
            String status = runnable.isForPrimaryAutomationServerOnly() && !this.isPrimaryAutomationServer() ? "N/A" : (runnable.isForAnyAutomationServer() && !this.isAutomationServer() && !this.isPrimaryAutomationServer() ? "Skipped" : (!runnable.isActive() ? "Paused" : "Running"));
            stats.addPoller(runnable.getName(), runnable.isActive(), runnable.getInterval(), runnable.getLastRun(), runnable.getRunCount(), status, runnable.getLastMessage(), runnable.getLastError());
        }
        return stats;
    }

    public int getTDLMaxThreads() {
        return this.tdlPool.getMaximumPoolSize();
    }

    public int getTDLActiveThreads() {
        return this.tdlPool.getActiveCount();
    }

    public boolean isPrimaryAutomationServer() {
        return AutomationService.isPrimaryAutomationServer(this.database.getDatabaseId());
    }

    public boolean isAutomationServer() {
        return AutomationService.isAutomationServer(this.database.getDatabaseId());
    }

    public static int getConfigInt(String config, int defaultValue) {
        int value = defaultValue;
        if (config != null && config.length() > 0) {
            try {
                value = Integer.parseInt(config);
            }
            catch (Exception e) {
                value = defaultValue;
            }
        }
        return value;
    }

    public void setStartupDelay(int delay) {
        this.startupDelay = delay;
    }

    public List<SDMSCollectorInternalHolder> getSdmsCollectorHolders() {
        return this.sdmsCollectorHolders;
    }

    public void startCollector(String collectorid) throws LAMException {
        try {
            String connectionid = SapphireService.getInternalConnectionid(this.database.getDatabaseId());
            try {
                boolean startCollector = true;
                for (SDMSCollectorInternalHolder holder : this.sdmsCollectorHolders) {
                    if (!holder.getCollectorid().equals(collectorid)) continue;
                    startCollector = false;
                }
                if (startCollector) {
                    SDMSCollectorInternalHolder holder = new SDMSCollectorInternalHolder(connectionid, this.database.getDatabaseId(), collectorid);
                    this.sdmsCollectorHolders.add(holder);
                    holder.start();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new LAMException("Failed to start Collector", e);
            }
        }
        catch (LAMException lAMException) {
            // empty catch block
        }
    }

    public void stopCollector(String collectorid) {
        SDMSCollectorInternalHolder holder = this.getCollectorHolder(collectorid);
        if (holder != null && holder.shutdown()) {
            this.getSdmsCollectorHolders().remove(holder);
        }
    }

    public void stopAllCollectors() {
        for (SDMSCollectorInternalHolder holder : this.sdmsCollectorHolders) {
            holder.shutdown();
        }
        this.sdmsCollectorHolders.clear();
    }

    public SDMSCollectorInternalHolder getCollectorHolder(String collectorid) {
        SDMSCollectorInternalHolder theHolder = null;
        for (SDMSCollectorInternalHolder holder : this.sdmsCollectorHolders) {
            if (!holder.getCollectorid().equals(collectorid)) continue;
            theHolder = holder;
        }
        return theHolder;
    }

    public void setToDoListBufferMillis(int bufferMillis) {
        this.bufferMillis = bufferMillis;
    }
}

