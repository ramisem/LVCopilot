/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.system.automation.runnables;

import com.labvantage.sapphire.admin.system.automation.LAM;
import com.labvantage.sapphire.admin.system.automation.LAMException;
import com.labvantage.sapphire.admin.system.automation.LAMScheduledRunnable;
import com.labvantage.sapphire.ejb.TDQManagerLocal;
import com.labvantage.sapphire.util.jndi.ServiceLocator;

public class UpdateStats
extends LAMScheduledRunnable {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    public static final int HOURLY = 1;
    public static final int DAILY = 2;
    private final int type;

    public UpdateStats(LAM lam, int type) {
        super(lam, "GeneralAutomation");
        this.type = type;
    }

    @Override
    public String getName() {
        return super.getName() + " (" + (this.type == 1 ? "Hourly" : "Daily") + ")";
    }

    @Override
    public String doRun() throws LAMException {
        try {
            String connectionid = this.getConnectionid();
            TDQManagerLocal tdqManager = ServiceLocator.getInstance().getTDQManager();
            if (this.type == 1) {
                tdqManager.performStatsMonitoringHourly(connectionid);
            } else if (this.type == 2) {
                tdqManager.performStatsMonitoringDaily(connectionid);
            }
        }
        catch (Exception e) {
            throw new LAMException("Failed to update stats: " + e.getMessage(), e);
        }
        return "";
    }
}

