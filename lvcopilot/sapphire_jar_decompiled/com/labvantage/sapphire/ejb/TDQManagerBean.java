/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.ejb.EJBException
 *  javax.ejb.SessionBean
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.ejb.BaseManager;
import com.labvantage.sapphire.ejb.ManagerException;
import com.labvantage.sapphire.ejb.TDQManagement;
import com.labvantage.sapphire.services.AutomationService;
import com.labvantage.sapphire.services.DataAccessService;
import com.labvantage.sapphire.services.SecurityService;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;

public class TDQManagerBean
extends BaseManager
implements SessionBean,
TDQManagement {
    public TDQManagerBean() {
        this.logName = "TDQManager";
    }

    @Override
    public void logInfo(Object out) {
        Trace.logDebug(this.logName, out, this.logContext);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean processToDoListEntry(String connectionid, String todolistid) throws ManagerException {
        boolean returnStatus;
        String methodName = "processToDoListEntry";
        try {
            this.startMethod(methodName, connectionid);
            AutomationService automationService = new AutomationService(this.sapphireConnection);
            returnStatus = automationService.processToDoListEntry(todolistid);
            DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
            try {
                db.setConnection(this.sapphireConnection);
                db.getCount("select count(*) from todolist");
            }
            finally {
                db.reset();
            }
        }
        catch (Exception e) {
            this.logError("Failed process todolist entry '" + todolistid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
        return returnStatus;
    }

    @Override
    public void deleteToDoListEntry(String connectionid, String todolistid) throws ManagerException {
        String methodName = "deleteToDoListEntry";
        try {
            this.startMethod(methodName, connectionid);
            AutomationService automationService = new AutomationService(this.sapphireConnection);
            automationService.deleteToDoListEntry(todolistid);
        }
        catch (Exception e) {
            this.logError("Failed delete todolist entry '" + todolistid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public void setToDoListError(String connectionid, String todolistid, String errorText) throws ManagerException {
        String methodName = "setToDoListError";
        try {
            this.startMethod(methodName, connectionid);
            AutomationService automationService = new AutomationService(this.sapphireConnection);
            automationService.setToDoListError(todolistid, errorText);
        }
        catch (Exception e) {
            this.logError("Failed process todolist entry '" + todolistid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public String getNextToDoListItems(String connectionid, int count) throws ManagerException {
        String methodName = "getNextToDoListItems";
        try {
            this.startMethod(methodName, connectionid);
            AutomationService automationService = new AutomationService(this.sapphireConnection);
            String string = automationService.getNextToDoListItems(count);
            return string;
        }
        catch (Exception e) {
            this.logError("Failed to fetch next set of todolist entries", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public void writeConnectionCache(String connectionid) throws ManagerException {
        String methodName = "writeConnectionCache";
        try {
            this.startMethod(methodName, connectionid);
            SecurityService securityService = new SecurityService(this.sapphireConnection);
            securityService.updateConnections();
        }
        catch (Exception e) {
            this.logError("Failed to update connections", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public void prepareTimeoutConnections(String connectionid, int timeouttime) throws ManagerException {
        String methodName = "prepareTimeoutConnections";
        try {
            this.startMethod(methodName, connectionid);
            SecurityService securityService = new SecurityService(this.sapphireConnection);
            securityService.prepareTimeoutConnections(timeouttime);
        }
        catch (Exception e) {
            this.logError("Failed to prepare timeout connections", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public void timeoutConnections(String connectionid) throws ManagerException {
        String methodName = "timeoutConnections";
        try {
            this.startMethod(methodName, connectionid);
            SecurityService securityService = new SecurityService(this.sapphireConnection);
            securityService.timeoutConnections();
        }
        catch (Exception e) {
            this.logError("Failed to timeout connections", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public void timeoutConnection(String connectionid, String connectionidtotimeout) throws ManagerException {
        String methodName = "timeoutConnection";
        try {
            this.startMethod(methodName, connectionid);
            SecurityService securityService = new SecurityService(this.sapphireConnection);
            securityService.prepareToDeleteConnection(connectionidtotimeout);
        }
        catch (Exception e) {
            this.logError("Failed to timeout connection", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public void timeoutRSets(String connectionid, int timeouttime) throws ManagerException {
        String methodName = "timeoutRSets";
        try {
            this.startMethod(methodName, connectionid);
            DataAccessService das = new DataAccessService(this.sapphireConnection);
            das.timeoutRSets(timeouttime);
        }
        catch (Exception e) {
            this.logError("Failed to timeout rsets", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public void performHouseKeeping(String connectionid) throws ManagerException {
        String methodName = "performHouseKeeping";
        try {
            this.startMethod(methodName, connectionid);
            AutomationService automationService = new AutomationService(this.sapphireConnection);
            automationService.performHouseKeeping();
        }
        catch (Exception e) {
            this.logError("Failed to perform housekeeping", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public void performStatsMonitoringHourly(String connectionid) throws ManagerException {
        String methodName = "performStatsMonitoringHourly";
        try {
            this.startMethod(methodName, connectionid);
            AutomationService automationService = new AutomationService(this.sapphireConnection);
            automationService.performStatsMonitoring("Hourly");
        }
        catch (Exception e) {
            this.logError("Failed to perform stats monitoring", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public void performStatsMonitoringDaily(String connectionid) throws ManagerException {
        String methodName = "performStatsMonitoringDaily";
        try {
            this.startMethod(methodName, connectionid);
            AutomationService automationService = new AutomationService(this.sapphireConnection);
            automationService.performStatsMonitoring("Daily");
        }
        catch (Exception e) {
            this.logError("Failed to perform stats monitoring", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public void importDataCaptureFolders(String connectionid) throws ManagerException {
        String methodName = "importDataCaptureFolders";
        try {
            this.startMethod(methodName, connectionid);
            AutomationService automationService = new AutomationService(this.sapphireConnection);
            automationService.importDataCaptureFolders();
        }
        catch (Exception e) {
            this.logError("Failed to import Data Capture Folders", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public String sendServerCommands(String connectionid) throws ManagerException {
        String methodName = "";
        try {
            this.startMethod(methodName, connectionid);
            AutomationService automationService = new AutomationService(this.sapphireConnection);
            String string = automationService.sendServerCommands();
            return string;
        }
        catch (Exception e) {
            this.logError("Failed to update stats log", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public String processServerCommands(String connectionid) throws ManagerException {
        String methodName = "";
        try {
            this.startMethod(methodName, connectionid);
            AutomationService automationService = new AutomationService(this.sapphireConnection);
            String string = automationService.processServerCommands();
            return string;
        }
        catch (Exception e) {
            this.logError("Failed to update stats log", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }
}

