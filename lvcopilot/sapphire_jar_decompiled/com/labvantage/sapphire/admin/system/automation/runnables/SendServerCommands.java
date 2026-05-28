/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.system.automation.runnables;

import com.labvantage.sapphire.admin.system.automation.LAM;
import com.labvantage.sapphire.admin.system.automation.LAMException;
import com.labvantage.sapphire.admin.system.automation.LAMScheduledRunnable;
import com.labvantage.sapphire.ejb.TDQManagerLocal;
import com.labvantage.sapphire.util.jndi.ServiceLocator;

public class SendServerCommands
extends LAMScheduledRunnable {
    public SendServerCommands(LAM lam) {
        super(lam, "GeneralAutomation");
    }

    @Override
    public String doRun() throws LAMException {
        try {
            String connectionid = this.getConnectionid();
            TDQManagerLocal tdqManager = ServiceLocator.getInstance().getTDQManager();
            tdqManager.sendServerCommands(connectionid);
        }
        catch (Exception e) {
            throw new LAMException("Failed to execute timeouts: " + e.getMessage(), e);
        }
        return "";
    }
}

