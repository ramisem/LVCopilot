/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.codahale.metrics.Counter
 *  com.codahale.metrics.MetricRegistry
 */
package com.labvantage.sapphire.modules.sdms.collector;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.modules.sdms.collector.collectortypes.BaseCollectorType;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

public class MonitoredThreadPoolExecutor
extends ScheduledThreadPoolExecutor
implements SDMSConstants {
    private final MetricRegistry metricRegistry;
    private final String metricsPrefix;

    public MonitoredThreadPoolExecutor(int corePoolSize, MetricRegistry metricRegistry, String poolName, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
        this.metricRegistry = metricRegistry;
        this.metricsPrefix = MetricRegistry.name(this.getClass(), (String[])new String[]{poolName});
    }

    public long getInstrumentFailedTasks(String instrumentid) {
        return this.metricRegistry.counter(MetricRegistry.name((String)this.metricsPrefix, (String[])new String[]{"failed-tasks", instrumentid})).getCount();
    }

    public long getInstrumentSuccessfulTasks(String instrumentid) {
        return this.metricRegistry.counter(MetricRegistry.name((String)this.metricsPrefix, (String[])new String[]{"successful-tasks", instrumentid})).getCount();
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable task, Throwable throwable) {
        if (task instanceof BaseCollectorType) {
            BaseCollectorType runnable = (BaseCollectorType)((Object)task);
            String instrumentid = runnable.getInstrumentid();
            if (throwable != null) {
                Counter failedTasksCounter = this.metricRegistry.counter(MetricRegistry.name((String)this.metricsPrefix, (String[])new String[]{"failed-tasks", instrumentid}));
                failedTasksCounter.inc();
            } else {
                Counter successfulTasksCounter = this.metricRegistry.counter(MetricRegistry.name((String)this.metricsPrefix, (String[])new String[]{"successful-tasks", instrumentid}));
                successfulTasksCounter.inc();
            }
        }
        super.afterExecute(task, throwable);
    }
}

