/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.cache;

import com.labvantage.sapphire.Cache;
import com.labvantage.sapphire.modules.eventmanager.NotifyManager;
import com.labvantage.sapphire.services.AutomationService;
import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.util.cache.CacheNames;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import sapphire.xml.PropertyList;

public class CacheUtil
implements CacheNames {
    private static int MAXSIZE = 1000;
    private static HashMap<String, HashMap<String, Object>> databaseCaches = new HashMap();

    private static Cache getCache(String databaseid, String cacheName) {
        return CacheUtil.getCache(databaseid, cacheName, MAXSIZE);
    }

    private static synchronized Cache getCache(String databaseid, String cacheName, int maxsize) {
        Cache cache;
        HashMap<String, Object> databaseCache = databaseCaches.get(databaseid);
        if (databaseCache == null) {
            databaseCache = new HashMap();
            databaseCaches.put(databaseid, databaseCache);
        }
        if ((cache = (Cache)databaseCache.get(cacheName)) == null) {
            maxsize = "WebPageTreeElement".equals(cacheName) ? 2000 : maxsize;
            try {
                String configmaxsize = ConfigService.getConfigProperty("com.labvantage.sapphire.server.cachemaxsize." + cacheName);
                if (Integer.parseInt(configmaxsize) > 0) {
                    maxsize = Integer.parseInt(configmaxsize);
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            cache = new Cache(cacheName, maxsize);
            databaseCache.put(cacheName, cache);
        }
        return cache;
    }

    public static void put(String databaseid, String cacheName, String cacheKey, Object cacheItem) {
        Cache cache = CacheUtil.getCache(databaseid, cacheName);
        cache.put(cacheKey, cacheItem);
    }

    public static Object get(String databaseid, String cacheName, String cacheKey) {
        Cache cache = CacheUtil.getCache(databaseid, cacheName);
        return cache == null ? null : cache.get(cacheKey);
    }

    public static Set<String> keySet(String databaseid) {
        HashMap<String, Object> databaseCache = databaseCaches.get(databaseid);
        if (databaseCache == null) {
            return new HashSet<String>();
        }
        return databaseCache.keySet();
    }

    public static Set<String> keySet(String databaseid, String cacheName) {
        Cache cache = CacheUtil.getCache(databaseid, cacheName);
        return cache.keySet();
    }

    public static void resetCache(String databaseid, boolean sendToOtherServers) {
        HashMap<String, Object> databaseCache = databaseCaches.get(databaseid);
        if (databaseCache != null) {
            databaseCache.clear();
        }
        if (sendToOtherServers) {
            AutomationService.broadcastServerCommand(databaseid, "ResetCache", "");
        }
        NotifyManager.notifyCache(databaseid, null, null, false, false);
    }

    public static void clear(String databaseid, String cacheName) {
        CacheUtil.clear(databaseid, cacheName, true);
    }

    public static void clear(String databaseid, String cacheName, boolean sendToOtherServers) {
        Cache cache = CacheUtil.getCache(databaseid, cacheName);
        cache.clear();
        if (sendToOtherServers) {
            AutomationService.broadcastServerCommand(databaseid, "ClearCache", cacheName);
        }
        NotifyManager.notifyCache(databaseid, cacheName, null, false, false);
    }

    public static void remove(String databaseid, String cacheName, String cacheKey) {
        CacheUtil.remove(databaseid, cacheName, cacheKey, true);
    }

    public static void remove(String databaseid, String cacheName, String cacheKey, boolean sendToOtherServers) {
        Cache cache = CacheUtil.getCache(databaseid, cacheName);
        cache.remove(cacheKey);
        if (sendToOtherServers) {
            PropertyList props = new PropertyList();
            props.setProperty("name", cacheName);
            props.setProperty("key", cacheKey);
            AutomationService.broadcastServerCommand(databaseid, "RemoveFromCache", props.toXMLString());
        }
        NotifyManager.notifyCache(databaseid, cacheName, cacheKey, false, false);
    }

    public static void removeAllStartWith(String databaseid, String cacheName, String startWithKey) {
        CacheUtil.removeAllStartWith(databaseid, cacheName, startWithKey, true);
    }

    public static void removeAllEndWith(String databaseid, String cacheName, String endWithKey) {
        CacheUtil.removeAllEndWith(databaseid, cacheName, endWithKey, true);
    }

    public static void removeAllStartWith(String databaseid, String cacheName, String startWithKey, boolean sendToOtherServers) {
        Cache cache = CacheUtil.getCache(databaseid, cacheName);
        cache.removeAllStartWith(startWithKey);
        if (sendToOtherServers) {
            PropertyList props = new PropertyList();
            props.setProperty("name", cacheName);
            props.setProperty("key", startWithKey);
            AutomationService.broadcastServerCommand(databaseid, "RemoveAllStartsWithFromCache", props.toXMLString());
        }
        NotifyManager.notifyCache(databaseid, cacheName, startWithKey, true, false);
    }

    public static void removeAllEndWith(String databaseid, String cacheName, String endWithKey, boolean sendToOtherServers) {
        Cache cache = CacheUtil.getCache(databaseid, cacheName);
        cache.removeAllEndsWith(endWithKey);
        if (sendToOtherServers) {
            PropertyList props = new PropertyList();
            props.setProperty("name", cacheName);
            props.setProperty("key", endWithKey);
            AutomationService.broadcastServerCommand(databaseid, "RemoveAllEndsWithFromCache", props.toXMLString());
        }
        NotifyManager.notifyCache(databaseid, cacheName, endWithKey, false, true);
    }

    public static int getCacheSize(String databaseid, String cacheName) {
        return CacheUtil.getCache(databaseid, cacheName).getSize();
    }

    public static int getMaxCacheSize(String databaseid, String cacheName) {
        return CacheUtil.getCache(databaseid, cacheName).getMaxSize();
    }
}

