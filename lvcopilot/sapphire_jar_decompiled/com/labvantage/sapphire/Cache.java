/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Cache {
    private int MAX_ENTRIES = 500;
    public Map<String, Object> cache = null;
    private String name;
    private int maxSize = 0;
    private long lastResetFlagCheckTime = 0L;
    private int currentResetFlagValue = -1;

    public Cache(String name) {
        this.name = name;
        this.createCache();
    }

    public Cache(String name, int maxsize) {
        this.MAX_ENTRIES = maxsize;
        this.name = name;
        this.createCache();
    }

    private synchronized void createCache() {
        if (this.cache == null) {
            this.cache = Collections.synchronizedMap(new HashMap());
        }
    }

    public Object get(String key) {
        return this.cache.get(key);
    }

    public void remove(String key) {
        this.cache.remove(key);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void removeAllStartWith(String startwith) {
        Set<String> keys = this.cache.keySet();
        Map<String, Object> map = this.cache;
        synchronized (map) {
            Iterator<String> it = keys.iterator();
            while (it.hasNext()) {
                String key = it.next();
                if (!key.startsWith(startwith)) continue;
                it.remove();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void removeAllEndsWith(String endswith) {
        Set<String> keys = this.cache.keySet();
        Map<String, Object> map = this.cache;
        synchronized (map) {
            Iterator<String> it = keys.iterator();
            while (it.hasNext()) {
                String key = it.next();
                if (!key.endsWith(endswith)) continue;
                it.remove();
            }
        }
    }

    public Object put(String key, Object value) {
        if (this.cache.size() > this.MAX_ENTRIES) {
            this.maxSize = this.MAX_ENTRIES;
            this.cache.clear();
        }
        Object o = this.cache.put(key, value);
        if (this.cache.size() > this.maxSize) {
            this.maxSize = this.cache.size();
        }
        return o;
    }

    public void clear() {
        if (this.cache.size() > this.maxSize) {
            this.maxSize = this.cache.size();
        }
        this.cache.clear();
    }

    public Set<String> keySet() {
        return this.cache.keySet();
    }

    public String getName() {
        return this.name;
    }

    public int getMaxSize() {
        return this.maxSize;
    }

    public int getSize() {
        return this.cache.size();
    }

    public long getLastResetFlagCheckTime() {
        return this.lastResetFlagCheckTime;
    }

    public void setLastResetFlagCheckTime(long lastResetFlagCheckTime) {
        this.lastResetFlagCheckTime = lastResetFlagCheckTime;
    }

    public int getCurrentResetFlagValue() {
        return this.currentResetFlagValue;
    }

    public void setCurrentResetFlagValue(int currentResetFlagValue) {
        this.currentResetFlagValue = currentResetFlagValue;
    }
}

