/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.system.automation.runnables;

import com.labvantage.sapphire.admin.system.automation.LAM;
import com.labvantage.sapphire.admin.system.automation.LAMException;
import com.labvantage.sapphire.admin.system.automation.LAMScheduledRunnable;
import com.labvantage.sapphire.ejb.APQManagerLocal;
import com.labvantage.sapphire.util.jndi.ServiceLocator;

public class TaskScheduler
extends LAMScheduledRunnable {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    public TaskScheduler(LAM lam) {
        super(lam, "Scheduling");
    }

    @Override
    public String doRun() throws LAMException {
        try {
            APQManagerLocal apqManager = ServiceLocator.getInstance().getAPQManager();
            apqManager.scheduleTasks(this.getConnectionid());
        }
        catch (Exception e) {
            throw new LAMException("Failed to poll Tasks: " + e.getMessage(), e);
        }
        return "";
    }
}

