/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.collector;

import com.labvantage.sapphire.modules.sdms.collector.SDMSCollector;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LogEntryList {
    private long maxAge;
    private int maxSize;
    private ArrayList<LogEntry> entries = new ArrayList();
    private SDMSCollector sdmsCollector;

    public LogEntryList(SDMSCollector sdmsCollector, long maxAge, int maxSize) {
        this.sdmsCollector = sdmsCollector;
        this.maxAge = maxAge;
        this.maxSize = maxSize;
    }

    public LogEntryList(SDMSCollector sdmsCollector) {
        this.sdmsCollector = sdmsCollector;
        this.maxAge = 3600000L;
        this.maxSize = 1000;
    }

    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public void log(String type, String message) {
        this.sdmsCollector.container.log(type, message);
        this.entries.add(new LogEntry(type, message));
        if (this.entries.size() > this.maxSize || this.entries.get((int)0).time < System.currentTimeMillis() - this.maxAge) {
            this.entries.remove(0);
        }
    }

    public void log(String type, String message, Throwable e) {
        this.sdmsCollector.container.log(type, message, e);
        this.entries.add(new LogEntry(type, message));
        if (this.entries.size() > this.maxSize || this.entries.get((int)0).time < System.currentTimeMillis() - this.maxAge) {
            this.entries.remove(0);
        }
    }

    public JSONArray toJSONArray() throws JSONException {
        JSONArray array = new JSONArray();
        for (LogEntry entry : this.entries) {
            array.put(entry.toJSONObject());
        }
        return array;
    }

    public static class LogEntry {
        protected String type;
        protected String message;
        protected long time = System.currentTimeMillis();

        public LogEntry(String type, String message) {
            this.type = type;
            this.message = message;
        }

        public JSONObject toJSONObject() throws JSONException {
            JSONObject jso = new JSONObject();
            jso.put("time", this.time);
            jso.put("type", this.type);
            jso.put("message", this.message);
            return jso;
        }
    }
}

