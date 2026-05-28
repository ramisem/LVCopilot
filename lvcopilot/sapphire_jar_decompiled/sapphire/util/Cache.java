/*
 * Decompiled with CFR 0.152.
 */
package sapphire.util;

import com.labvantage.sapphire.util.cache.CacheUtil;
import java.io.File;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.util.ConnectionInfo;

public class Cache {
    String databaseid;
    static final String PREFIX = "CUSTOM_";

    public Cache(String connectionid) throws SapphireException {
        ConnectionProcessor cp = new ConnectionProcessor(connectionid);
        ConnectionInfo info = cp.getConnectionInfo(connectionid);
        if (info == null) {
            throw new SapphireException("Invalid connectionid");
        }
        this.databaseid = info.getDatabaseId();
    }

    public Cache(File rakFile, String connectionid) throws SapphireException {
        ConnectionProcessor cp = new ConnectionProcessor(rakFile, connectionid);
        ConnectionInfo info = cp.getConnectionInfo(connectionid);
        if (info == null) {
            throw new SapphireException("Invalid connectionid");
        }
        this.databaseid = info.getDatabaseId();
    }

    public void put(String cacheName, String cacheKey, Object cacheItem) {
        CacheUtil.put(this.databaseid, PREFIX + cacheName, cacheKey, cacheItem);
    }

    public Object get(String cacheName, String cacheKey) {
        return CacheUtil.get(this.databaseid, PREFIX + cacheName, cacheKey);
    }

    public Set<String> keySet(String cacheName) {
        return CacheUtil.keySet(this.databaseid, PREFIX + cacheName);
    }

    public void clear(String cacheName) {
        CacheUtil.clear(this.databaseid, PREFIX + cacheName);
    }

    public void remove(String cacheName, String cacheKey) {
        CacheUtil.remove(this.databaseid, PREFIX + cacheName, cacheKey);
    }

    public int getCacheSize(String cacheName) {
        return CacheUtil.getCacheSize(this.databaseid, PREFIX + cacheName);
    }
}

