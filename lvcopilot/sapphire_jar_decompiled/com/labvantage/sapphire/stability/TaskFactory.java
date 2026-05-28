/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability;

import com.labvantage.sapphire.Cache;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.scheduler.BaseScheduleTask;
import com.labvantage.sapphire.stability.task.GridTask;
import sapphire.util.Logger;

public class TaskFactory {
    private static Cache cache = new Cache("Tasks");
    private static Cache cacheScheduleTasks = new Cache("Tasks");

    public static GridTask getGridTask(String objectname) {
        GridTask task = (GridTask)cache.get(objectname);
        if (task == null) {
            try {
                Trace.log("Creating new instance of " + objectname);
                Class<?> c = Class.forName(objectname);
                Object o = c.newInstance();
                task = (GridTask)o;
                task = (GridTask)Class.forName(objectname).newInstance();
                cache.put(objectname, task);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return task;
    }

    public static BaseScheduleTask getScheduleTask(String objectname) {
        BaseScheduleTask task = (BaseScheduleTask)cacheScheduleTasks.get(objectname);
        if (task == null) {
            try {
                Trace.log("Creating new instance of " + objectname);
                Class<?> c = Class.forName(objectname);
                Object o = c.newInstance();
                task = (BaseScheduleTask)o;
                cacheScheduleTasks.put(objectname, task);
            }
            catch (Exception e) {
                Logger.logStackTrace(e);
            }
        }
        return task;
    }
}

