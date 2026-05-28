/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.system.automation.runnables;

import com.labvantage.sapphire.admin.system.automation.LAM;
import com.labvantage.sapphire.admin.system.automation.LAMException;
import com.labvantage.sapphire.admin.system.automation.LAMScheduledRunnable;
import com.labvantage.sapphire.ejb.TDQManagerLocal;
import com.labvantage.sapphire.util.jndi.ServiceLocator;

public class Housekeeping
extends LAMScheduledRunnable {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    public Housekeeping(LAM lam) {
        super(lam, "GeneralAutomation");
    }

    @Override
    public String doRun() throws LAMException {
        try {
            this.logger.info("Performing Scheduled Housekeeping");
            TDQManagerLocal tdqManager = ServiceLocator.getInstance().getTDQManager();
            tdqManager.performHouseKeeping(this.getConnectionid());
        }
        catch (Exception e) {
            throw new LAMException("Housekeeping failure: " + e.getMessage(), e);
        }
        return "";
    }
}

