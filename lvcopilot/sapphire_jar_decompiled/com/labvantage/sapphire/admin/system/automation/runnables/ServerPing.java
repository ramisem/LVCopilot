/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.system.automation.runnables;

import com.labvantage.sapphire.admin.system.automation.LAM;
import com.labvantage.sapphire.admin.system.automation.LAMException;
import com.labvantage.sapphire.admin.system.automation.LAMScheduledRunnable;
import com.labvantage.sapphire.admin.system.automation.Server;
import com.labvantage.sapphire.ejb.AutomationManagerLocal;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.AutomationService;
import com.labvantage.sapphire.util.jndi.ServiceLocator;
import sapphire.SapphireException;

public class ServerPing
extends LAMScheduledRunnable {
    private Server server;
    private int serverLatency;

    public ServerPing(LAM lam) {
        super(lam, "GeneralAutomation");
        try {
            Configuration configuration = Configuration.getInstance();
            this.server = configuration.getServer(this.database);
        }
        catch (SapphireException e) {
            this.logger.error("Unable to initiate server object");
        }
    }

    public void setServerLatency(int latency) {
        this.serverLatency = latency;
    }

    @Override
    public String doRun() throws LAMException {
        String connectionid = this.getConnectionid();
        try {
            AutomationManagerLocal automationManager = ServiceLocator.getInstance().getAutomationManager();
            automationManager.pingServer(connectionid, this.server);
            automationManager.killRedundantServers(connectionid, this.serverLatency);
            automationManager.scanServerList(connectionid);
        }
        catch (Exception e) {
            throw new LAMException("Server ping failure: " + e.getMessage(), e);
        }
        return AutomationService.getOtherServerList(this.databaseid).size() + " other server(s) found";
    }
}

