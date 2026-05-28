/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.qcbatch;

import com.labvantage.opal.qcbatch.QCBatch;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.util.Logger;

public class QCBatchPool
extends TimerTask {
    public static String LABVANTAGE_CVS_ID = "$Revision: 75574 $";
    private static HashMap __QCBatchCol;
    private static final long period = 3600000L;
    private static final long delay = 0L;
    private static QCBatchPool pool;

    public QCBatchPool() {
        Logger.logInfo("Initiating QCBatchPool...");
        __QCBatchCol = new HashMap();
        Timer timer = new Timer(true);
        timer.schedule((TimerTask)this, 0L, 3600000L);
    }

    public static synchronized QCBatch getQCBatch(QueryProcessor queryProcessor, String qcbatchid) {
        QCBatch qcBatch = null;
        if (__QCBatchCol.containsKey(qcbatchid)) {
            Logger.logInfo("[QCBatchPool] Returning instance of " + qcbatchid + " from pool...");
            qcBatch = (QCBatch)__QCBatchCol.get(qcbatchid);
            Logger.logInfo("Resetting the queryProcessor of the QCBatch " + qcbatchid + " returned from pool.");
            qcBatch.setQueryProcessor(queryProcessor);
            if (qcBatch.isModified()) {
                Logger.logInfo("[QCBatchPool] The QCBatch " + qcbatchid + " has been modified.");
                try {
                    qcBatch = new QCBatch(queryProcessor, qcbatchid);
                }
                catch (SapphireException e) {
                    Logger.logError("Error creating QCBatch", e);
                }
            }
        } else {
            try {
                Logger.logInfo("[QCBatchPool] Creating new instance of " + qcbatchid + "...");
                qcBatch = new QCBatch(queryProcessor, qcbatchid);
                __QCBatchCol.put(qcbatchid, qcBatch);
            }
            catch (SapphireException e) {
                Logger.logError("Error creating QCBatch", e);
                return null;
            }
        }
        qcBatch.raiseInstanceCount(pool);
        return qcBatch;
    }

    private static boolean removeQCBatch(String qcbatchid) {
        if (__QCBatchCol.containsKey(qcbatchid)) {
            Logger.logError("[QCBatchPool] Removing instance of " + qcbatchid + " from pool...");
            __QCBatchCol.remove(qcbatchid);
            return true;
        }
        return false;
    }

    public static synchronized int releaseQCBatch(String qcBatchId) {
        QCBatch qcBatch = null;
        if (__QCBatchCol.containsKey(qcBatchId)) {
            Logger.logInfo("[QCBatchPool] Releasing instance of " + qcBatchId + "...");
            qcBatch = (QCBatch)__QCBatchCol.get(qcBatchId);
            return qcBatch.lowerInstanceCount(pool);
        }
        return -1;
    }

    public static synchronized int releaseQCBatch(QCBatch qcBatch) {
        if (qcBatch != null) {
            Logger.logInfo("[QCBatchPool] Releasing instance of " + qcBatch.getQCBatchID() + "...");
            return qcBatch.lowerInstanceCount(pool);
        }
        return -1;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        Logger.logInfo("[QCBatchPool] Initiating purging of pool...");
        long current = System.currentTimeMillis();
        StringBuffer sb = new StringBuffer();
        HashMap hashMap = __QCBatchCol;
        synchronized (hashMap) {
            Object[] keySet;
            for (Object key : keySet = __QCBatchCol.keySet().toArray()) {
                QCBatch qcBatch = (QCBatch)__QCBatchCol.get(key);
                if (qcBatch.getInstanceCount() != 0 && current - qcBatch.getInstanceTimeMillis() <= 3600000L) continue;
                sb.append(qcBatch.getQCBatchID());
                QCBatchPool.removeQCBatch(qcBatch.getQCBatchID());
            }
        }
        Logger.logInfo("[QCBatchPool] Purged pool - " + sb);
    }

    public static synchronized void renewQCBatchInPool(QCBatch qcBatch) {
        __QCBatchCol.put(qcBatch.getQCBatchID(), qcBatch);
    }

    static {
        pool = new QCBatchPool();
    }
}

