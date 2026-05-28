/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.system.automation.runnables;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.admin.system.automation.LAM;
import com.labvantage.sapphire.admin.system.automation.LAMException;
import com.labvantage.sapphire.admin.system.automation.LAMScheduledRunnable;
import com.labvantage.sapphire.ejb.TDQManagerLocal;
import com.labvantage.sapphire.util.jndi.ServiceLocator;

public class Timeout
extends LAMScheduledRunnable {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    int rsetTimeout = 60;
    int connectionTimeout = 3600;

    public Timeout(LAM lam) {
        super(lam, "GeneralAutomation");
        ConfigurationProcessor cp = new ConfigurationProcessor(this.getConnectionid());
        try {
            this.connectionTimeout = Integer.parseInt(cp.getSysConfigProperty("connectiontimeout", String.valueOf(3600)));
        }
        catch (Exception e) {
            this.connectionTimeout = 3600;
            Trace.logError("CONNECTIONTIMOUT value invalid - using default of " + this.connectionTimeout + ". Check your system configuration.");
        }
        try {
            this.rsetTimeout = Integer.parseInt(cp.getSysConfigProperty("rsettimeout", String.valueOf(60)));
        }
        catch (Exception e) {
            this.rsetTimeout = 60;
            Trace.logError("RSETTIMOUT value invalid - using default of " + this.rsetTimeout + ". Check your system configuration.");
        }
    }

    @Override
    public String doRun() throws LAMException {
        try {
            String connectionid = this.getConnectionid();
            TDQManagerLocal tdqManager = ServiceLocator.getInstance().getTDQManager();
            tdqManager.timeoutConnections(this.getConnectionid());
            tdqManager.prepareTimeoutConnections(connectionid, this.connectionTimeout);
            tdqManager.timeoutRSets(connectionid, this.rsetTimeout);
        }
        catch (Exception e) {
            throw new LAMException("Failed to execute timeouts: " + e.getMessage(), e);
        }
        return "";
    }
}

