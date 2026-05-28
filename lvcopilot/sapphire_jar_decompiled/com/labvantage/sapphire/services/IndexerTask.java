/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.services;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.search.Indexer;
import java.util.TimerTask;
import sapphire.SapphireException;

public class IndexerTask
extends TimerTask {
    private long interval;
    private String databaseid;

    public void setDatabaseid(String databaseid) {
        this.databaseid = databaseid;
    }

    public long getInterval() {
        return this.interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    @Override
    public void run() {
        Trace.logDebug("INDEXER timer task for database: " + this.databaseid);
        try {
            Indexer indexer = Indexer.getInstance(this.databaseid);
            if (!indexer.isIndexLocked()) {
                indexer.crawlDatabase();
            } else {
                Trace.logDebug("Indexer locked - giving up");
            }
        }
        catch (SapphireException e) {
            Trace.logError(e.getMessage(), e);
        }
    }
}

