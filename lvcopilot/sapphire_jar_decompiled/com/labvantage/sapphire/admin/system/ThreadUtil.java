/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.system;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import sapphire.util.DataSet;

public class ThreadUtil {
    public static final String MODE_ALLTHREADS = "allthreads";
    public static final String MODE_ALLNORMALTHREADS = "allnormalthreads";
    public static final String MODE_LVAUTOMATIONTHREADS = "lvautomationthreads";
    public static final String MODE_LVSTACKTRACES = "lvstacktraces";

    public static DataSet allThreads() {
        Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
        Set<Thread> threads = allStackTraces.keySet();
        DataSet ds = new DataSet();
        for (Thread t : threads) {
            ThreadUtil.addThreadRow(ds, t);
        }
        return ds;
    }

    public static DataSet allNormalThreads() {
        Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
        Set<Thread> threads = allStackTraces.keySet();
        DataSet ds = new DataSet();
        for (Thread t : threads) {
            if (t.isDaemon()) continue;
            ThreadUtil.addThreadRow(ds, t);
        }
        return ds;
    }

    public static DataSet allLabVantageThreads() {
        Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
        Set<Thread> threads = allStackTraces.keySet();
        DataSet ds = new DataSet();
        for (Thread t : threads) {
            if (!t.getName().startsWith("lv-")) continue;
            ThreadUtil.addThreadRow(ds, t);
        }
        return ds;
    }

    public static String getThreadStackTrace(long id) {
        Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
        Set<Thread> threads = allStackTraces.keySet();
        StringBuilder b = new StringBuilder();
        for (Thread t : threads) {
            if (t.getId() != id) continue;
            StackTraceElement[] stackTraceElements = allStackTraces.get(t);
            ThreadUtil.getStackTrace(stackTraceElements, b);
        }
        return b.toString();
    }

    public static DataSet lvStackTraces() {
        Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
        Set<Thread> threads = allStackTraces.keySet();
        DataSet ds = new DataSet();
        for (Thread t : threads) {
            StringBuilder b;
            StackTraceElement[] stackTraceElements = allStackTraces.get(t);
            boolean containsSapphire = ThreadUtil.getStackTrace(stackTraceElements, b = new StringBuilder());
            if (!containsSapphire) continue;
            int row = ThreadUtil.addThreadRow(ds, t);
            ds.setString(row, "trace", b.toString());
        }
        return ds;
    }

    private static boolean getStackTrace(StackTraceElement[] trace, StringBuilder b) {
        boolean containsSapphire = false;
        int noncount = 0;
        for (int i = 0; i < trace.length; ++i) {
            StackTraceElement stackTraceElement = trace[i];
            String line = stackTraceElement.toString();
            boolean sapp = line.contains("sapphire.");
            containsSapphire |= sapp;
            if (sapp) {
                if (noncount >= 3) {
                    b.append("...<br>");
                }
                b.append(stackTraceElement.toString()).append("<br>");
                noncount = 0;
                continue;
            }
            if (++noncount >= 3) continue;
            b.append(stackTraceElement.toString()).append("<br>");
        }
        return containsSapphire;
    }

    private static int addThreadRow(DataSet ds, Thread t) {
        int row = ds.addRow();
        Thread.State state = t.getState();
        ThreadGroup group = t.getThreadGroup();
        String type = t.isDaemon() ? "Daemon" : "Normal";
        ds.setString(row, "name", t.getName());
        ds.setString(row, "state", state.toString());
        ds.setString(row, "group", group.getName());
        ds.setString(row, "priority", "" + t.getPriority());
        ds.setString(row, "type", type);
        ds.setNumber(row, "id", t.getId());
        return row;
    }

    public static LVThreadFactory getThreadFactory(String threadPrefix) {
        return new LVThreadFactory("", threadPrefix);
    }

    public static LVThreadFactory getThreadFactory(String databaseid, String threadPrefix) {
        return new LVThreadFactory(databaseid, threadPrefix);
    }

    private static class LVThreadFactory
    implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        LVThreadFactory(String databaseid, String threadPrefix) {
            SecurityManager s = System.getSecurityManager();
            ThreadGroup threadGroup = this.group = s != null ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            if (threadPrefix == null || threadPrefix.length() == 0) {
                threadPrefix = "default";
            }
            if (databaseid == null || databaseid.length() == 0) {
                databaseid = "alldb";
            }
            if (threadPrefix.startsWith("lv-")) {
                threadPrefix = threadPrefix.substring(3);
            }
            this.namePrefix = "lv-" + databaseid.toLowerCase() + "-" + threadPrefix.toLowerCase() + "-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(this.group, r, this.namePrefix + this.threadNumber.getAndIncrement(), 0L);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != 5) {
                t.setPriority(5);
            }
            return t;
        }
    }
}

