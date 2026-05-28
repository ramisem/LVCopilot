/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.system.automation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

public class LAMStats
implements Serializable {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    public boolean isPrimaryAutomationServer;
    public boolean isAutomationServer;
    public ArrayList<Poller> pollers = new ArrayList();
    public ArrayList<Timer> timers = new ArrayList();
    public int poolSize;
    public long totalTasksCompleted;
    public int activeThreads;
    public String status;

    public void addPoller(String name, boolean active, int interval, Calendar lastRun, int runCount, String status, String lastMessage, String lastError) {
        Poller p = new Poller();
        p.name = name;
        p.active = active;
        p.interval = interval;
        p.lastRun = lastRun;
        p.runCount = runCount;
        p.status = status;
        p.lastMessage = lastMessage;
        p.lastError = lastError;
        this.pollers.add(p);
    }

    public void addTimer(String name, long count, String oneRate, String fiveRate, String fifteenRate, String mean, String stdev, String nintyfive) {
        Timer t = new Timer();
        t.name = name;
        t.count = count;
        t.oneRate = oneRate;
        t.fiveRate = fiveRate;
        t.fifteenRate = fifteenRate;
        t.mean = mean;
        t.stdev = stdev;
        t.nintyfive = nintyfive;
        this.timers.add(t);
    }

    public class Timer
    implements Serializable {
        public String name;
        public long count;
        public String oneRate;
        public String fiveRate;
        public String fifteenRate;
        public String mean;
        public String stdev;
        public String nintyfive;
    }

    public class Poller
    implements Serializable {
        public String name;
        public boolean active;
        public int interval;
        public Calendar lastRun;
        public int runCount;
        public String status;
        public String lastMessage;
        public String lastError;
    }
}

