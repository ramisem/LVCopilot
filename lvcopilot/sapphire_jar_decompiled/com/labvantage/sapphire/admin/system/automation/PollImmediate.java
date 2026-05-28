/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.system.automation;

import com.labvantage.sapphire.admin.system.automation.LAM;
import com.labvantage.sapphire.admin.system.automation.LAMException;
import com.labvantage.sapphire.admin.system.automation.LAMRunnable;
import com.labvantage.sapphire.ejb.TDQManagerLocal;
import com.labvantage.sapphire.util.jndi.ServiceLocator;

public class PollImmediate
extends LAMRunnable {
    private int bufferMillis;

    public PollImmediate(LAM lam, int bufferMillis) {
        super(lam, "ToDoList");
        this.bufferMillis = bufferMillis;
    }

    @Override
    public String doRun() throws LAMException {
        try {
            Thread.sleep(this.bufferMillis);
        }
        catch (InterruptedException interruptedException) {
            // empty catch block
        }
        int maxThreads = this.lam.getTDLMaxThreads();
        int activeThreads = this.lam.getTDLActiveThreads();
        int entriesToRetrieve = 1 + maxThreads - activeThreads;
        if (entriesToRetrieve > 0) {
            try {
                TDQManagerLocal tdqManager = ServiceLocator.getInstance().getTDQManager();
                String todolistids = tdqManager.getNextToDoListItems(this.getConnectionid(), entriesToRetrieve);
                if (todolistids != null && todolistids.length() > 0) {
                    this.lam.processToDoListEntries(todolistids);
                }
            }
            catch (Exception e) {
                throw new LAMException("Failed to Poll ToDoList: " + e.getMessage(), e);
            }
        }
        return "";
    }
}

