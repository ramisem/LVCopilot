/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.ejb.EJBException
 *  javax.ejb.SessionBean
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.admin.system.automation.LAM;
import com.labvantage.sapphire.admin.system.automation.LAMException;
import com.labvantage.sapphire.admin.system.automation.Server;
import com.labvantage.sapphire.ejb.AutomationManagement;
import com.labvantage.sapphire.ejb.BaseManager;
import com.labvantage.sapphire.ejb.ManagerException;
import com.labvantage.sapphire.services.AutomationService;
import com.labvantage.sapphire.services.RequestService;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import sapphire.xml.PropertyList;

public class AutomationManagerBean
extends BaseManager
implements SessionBean,
AutomationManagement {
    public AutomationManagerBean() {
        this.logName = "AutomationManager";
    }

    @Override
    public PropertyList getToDoListProperties(String connectionid, String todolistid) throws ManagerException {
        String methodName = "getToDoListProperties";
        try {
            this.startMethod(methodName, connectionid);
            AutomationService automationService = new AutomationService(this.sapphireConnection);
            PropertyList propertyList = automationService.getToDoListProperties(todolistid);
            return propertyList;
        }
        catch (Exception e) {
            this.logError("Failed to get todolist properties for todolistid '" + todolistid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public void loadWebPageCache(String connectionid) throws ManagerException {
        String methodName = "loadWebPageCache";
        try {
            this.startMethod(methodName, connectionid);
            RequestService requestService = new RequestService(this.sapphireConnection);
            requestService.loadWebPageCache();
        }
        catch (Exception e) {
            this.logError("Failed to initialize the webpage cache", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public LAM createLAM(String connectionid) throws LAMException {
        String methodName = "createLAM";
        try {
            this.startMethod(methodName, connectionid);
            AutomationService automationService = new AutomationService(this.sapphireConnection);
            LAM lAM = automationService.createLAM(this.sapphireConnection.getDatabaseId());
            return lAM;
        }
        catch (Exception e) {
            this.logError("Failed to create LAM", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public void destroyLAM(String connectionid) throws LAMException {
        String methodName = "destroyLAM";
        try {
            this.startMethod(methodName, connectionid);
            AutomationService automationService = new AutomationService(this.sapphireConnection);
            automationService.destroyLAM(this.sapphireConnection.getDatabaseId());
        }
        catch (Exception e) {
            this.logError("Failed to destroy LAM", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public void pausePoller(String connectionid, int type) {
        String methodName = "pausePoller";
        try {
            this.startMethod(methodName, connectionid);
            AutomationService automationService = new AutomationService(this.sapphireConnection);
            LAM lam = AutomationService.getLAM(this.sapphireConnection.getDatabaseId());
            lam.pausePoller(type);
        }
        catch (Exception e) {
            this.logError("Failed to pause poller", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public void resumePoller(String connectionid, int type) {
        String methodName = "resumePoller";
        try {
            this.startMethod(methodName, connectionid);
            AutomationService automationService = new AutomationService(this.sapphireConnection);
            LAM lam = AutomationService.getLAM(this.sapphireConnection.getDatabaseId());
            lam.resumePoller(type);
        }
        catch (Exception e) {
            this.logError("Failed to pause poller", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public boolean pingServer(String connectionid, Server server) {
        boolean moreServersExists = true;
        try {
            this.startMethod("", connectionid);
            AutomationService automationService = new AutomationService(this.sapphireConnection);
            moreServersExists = automationService.pingServer(server);
        }
        catch (Exception e) {
            this.logError("Failed to ping server", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod("");
        }
        return moreServersExists;
    }

    @Override
    public void killRedundantServers(String connectionid, int serverLatency) {
        try {
            this.startMethod("", connectionid);
            AutomationService automationService = new AutomationService(this.sapphireConnection);
            automationService.killRedundantServers(serverLatency);
        }
        catch (Exception e) {
            this.logError("Failed to kill redundant servers", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod("");
        }
    }

    @Override
    public void scanServerList(String connectionid) {
        try {
            this.startMethod("", connectionid);
            AutomationService automationService = new AutomationService(this.sapphireConnection);
            automationService.scanServerList();
        }
        catch (Exception e) {
            this.logError("Failed to scan server list", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod("");
        }
    }

    @Override
    public void startStopCollectors(String connectionid) {
        try {
            this.startMethod("", connectionid);
            AutomationService automationService = new AutomationService(this.sapphireConnection);
            automationService.startStopCollectors();
        }
        catch (Exception e) {
            this.logError("Failed to stop and start internal collectors", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod("");
        }
    }

    @Override
    public void reallocateCollectors(String connectionid) {
        try {
            this.startMethod("", connectionid);
            AutomationService automationService = new AutomationService(this.sapphireConnection);
            automationService.reallocateCollectors();
        }
        catch (Exception e) {
            this.logError("Failed to reallocate internal collcetors", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod("");
        }
    }
}

