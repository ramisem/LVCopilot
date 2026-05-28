/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.system.automation.runnables;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.system.automation.LAM;
import com.labvantage.sapphire.admin.system.automation.LAMException;
import com.labvantage.sapphire.admin.system.automation.LAMScheduledRunnable;
import sapphire.SapphireException;

public class Indexer
extends LAMScheduledRunnable {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    public Indexer(LAM lam) {
        super(lam, "Indexing");
    }

    @Override
    public String doRun() throws LAMException {
        String message = "";
        try {
            try {
                com.labvantage.sapphire.modules.search.Indexer indexer = com.labvantage.sapphire.modules.search.Indexer.getInstance(this.databaseid);
                if (!indexer.isIndexLocked()) {
                    indexer.crawlDatabase();
                } else {
                    message = "Indexer locked - giving up";
                    Trace.logDebug(message);
                }
            }
            catch (SapphireException e) {
                throw new LAMException("Indexing failure: " + e.getMessage(), e);
            }
        }
        catch (Exception e) {
            throw new LAMException("Indexing failure: " + e.getMessage(), e);
        }
        return message;
    }
}

