/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.system.automation.runnables;

import com.labvantage.sapphire.admin.system.automation.LAM;
import com.labvantage.sapphire.admin.system.automation.LAMException;
import com.labvantage.sapphire.admin.system.automation.LAMScheduledRunnable;
import com.labvantage.sapphire.ejb.AutomationManagerLocal;
import com.labvantage.sapphire.util.jndi.ServiceLocator;

public class ReallocateCollectors
extends LAMScheduledRunnable {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    public ReallocateCollectors(LAM lam) {
        super(lam, "SDMSCollector");
    }

    @Override
    public String doRun() throws LAMException {
        try {
            AutomationManagerLocal automationManager = ServiceLocator.getInstance().getAutomationManager();
            automationManager.reallocateCollectors(this.getConnectionid());
        }
        catch (Exception e) {
            throw new LAMException("Failed to reallocate collectors: " + e.getMessage(), e);
        }
        return "";
    }
}

