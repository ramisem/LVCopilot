/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire;

import com.labvantage.sapphire.Build;
import com.labvantage.sapphire.StatsBlock;
import com.labvantage.sapphire.Trace;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import sapphire.util.StringUtil;

public class Stats {
    private static HashMap _blocks = new HashMap();
    private static HashSet _names = new HashSet();
    private static HashSet _threads = new HashSet();

    public static void list(String filename) {
        if (filename != null && filename.length() > 0) {
            try {
                FileOutputStream out = new FileOutputStream(filename);
                PrintStream printout = new PrintStream(out);
                Stats.list(printout);
            }
            catch (IOException ioe) {
                Trace.logError("Failed to create output to file '" + filename + "'. Exception: " + ioe.getMessage(), ioe);
            }
        }
    }

    public static synchronized void list(PrintStream out) {
        StatsBlock block;
        String name;
        DateFormat df = DateFormat.getDateTimeInstance();
        out.println("Sapphire Statistics File");
        out.println("========================");
        out.println("Build:\t\t" + Build.getAppServerBuild());
        out.println("Created:\t" + df.format(Calendar.getInstance().getTime()));
        out.println("");
        long hits = 0L;
        long total = 0L;
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        out.println(StringUtil.padRight("Block Name", 40, ' ') + " " + StringUtil.padLeft("Hits", 6, ' ') + " " + StringUtil.padLeft("Ave", 6, ' ') + " " + StringUtil.padLeft("Min", 6, ' ') + " " + StringUtil.padLeft("Max", 6, ' '));
        out.println(StringUtil.padRight("", 40, '=') + " " + StringUtil.padLeft("", 6, '=') + " " + StringUtil.padLeft("", 6, '=') + " " + StringUtil.padLeft("", 6, '=') + " " + StringUtil.padLeft("", 6, '='));
        for (Object _name : _names) {
            name = (String)_name;
            block = (StatsBlock)_blocks.get(name);
            if (block == null) {
                for (Object _thread : _threads) {
                    block = (StatsBlock)_blocks.get(name + "##" + _thread);
                    if (block == null) continue;
                    hits += (long)block.getTimeCount();
                    total += block.getTimeTotal();
                    min = Math.min(min, block.getTimeMin());
                    max = Math.max(max, block.getTimeMax());
                }
            } else {
                hits = block.getTimeCount();
                total = block.getTimeTotal();
                min = Math.min(min, block.getTimeMin());
                max = Math.max(max, block.getTimeMax());
            }
            out.println(StringUtil.padRight(name, 40, ' ') + " " + StringUtil.padLeft(String.valueOf(hits), 6, ' ') + " " + StringUtil.padLeft(String.valueOf(hits > 0L ? total / hits : 0L), 6, ' ') + " " + StringUtil.padLeft(String.valueOf(min), 6, ' ') + " " + StringUtil.padLeft(String.valueOf(max), 6, ' '));
            total = 0L;
            hits = 0L;
            min = Long.MAX_VALUE;
            max = Long.MIN_VALUE;
        }
        out.println("");
        out.println(StringUtil.padRight("Block Name", 40, ' ') + " " + StringUtil.padRight("Threads", 40, ' '));
        out.println(StringUtil.padRight("", 40, '=') + " " + StringUtil.padLeft("", 40, '='));
        for (Object _name : _names) {
            name = (String)_name;
            block = (StatsBlock)_blocks.get(name);
            StringBuilder threadusage = new StringBuilder();
            if (block == null) {
                for (Object _thread : _threads) {
                    block = (StatsBlock)_blocks.get(name + "##" + _thread);
                    if (block == null) continue;
                    threadusage.append(", ").append(block.getThreadName()).append(": ").append(block.getTimeCount());
                }
            }
            out.println(StringUtil.padRight(name, 40, ' ') + " " + threadusage.substring(1));
        }
    }

    public static synchronized void setStart(String name) {
        Stats.set(name, 1, false);
    }

    public static synchronized void setEnd(String name) {
        Stats.set(name, 2, false);
    }

    private static StatsBlock set(String name, int setblockitem, boolean ignorethreads) {
        Thread current = Thread.currentThread();
        String threadname = current.getName();
        StatsBlock block = (StatsBlock)_blocks.get(name + (ignorethreads ? "" : "##" + threadname));
        if (block == null) {
            block = new StatsBlock(name, threadname);
        }
        switch (setblockitem) {
            case 0: {
                block.incCount();
                break;
            }
            case 1: {
                block.setStart();
                block.incCount();
                break;
            }
            case 2: {
                block.setEnd();
            }
        }
        _blocks.put(name + (ignorethreads ? "" : "##" + threadname), block);
        _names.add(name);
        _threads.add(threadname);
        return block;
    }
}

