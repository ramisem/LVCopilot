/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.ejb.EJBException
 *  javax.ejb.SessionBean
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.ejb.BaseManager;
import com.labvantage.sapphire.ejb.ManagerException;
import com.labvantage.sapphire.ejb.StatusManagement;
import com.labvantage.sapphire.services.StatusService;
import java.util.List;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import sapphire.util.DataSet;

public class StatusManagerBean
extends BaseManager
implements SessionBean,
StatusManagement {
    public StatusManagerBean() {
        this.logName = "StatusManager";
    }

    @Override
    public DataSet getStats(String connectionid, int type) throws ManagerException {
        String methodName = "getStats";
        try {
            this.startMethod(methodName, connectionid);
            StatusService statusService = new StatusService(this.sapphireConnection);
            DataSet dataSet = statusService.getStats(type);
            return dataSet;
        }
        catch (Exception e) {
            this.logError("Failed to get statistics", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public void resetStats(String connectionid) throws ManagerException {
        String methodName = "resetStats";
        try {
            this.startMethod(methodName, connectionid);
            StatusService statusService = new StatusService(this.sapphireConnection);
            statusService.resetStats();
        }
        catch (Exception e) {
            this.logError("Failed to reset statistics", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public DataSet getCacheSizes(String connectionid, boolean includeContents) throws ManagerException {
        String methodName = "getCacheSizes";
        try {
            this.startMethod(methodName, connectionid);
            StatusService statusService = new StatusService(this.sapphireConnection);
            DataSet dataSet = statusService.getCacheSizes(includeContents);
            return dataSet;
        }
        catch (Exception e) {
            this.logError("Failed to get cache sizes", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public List<String> getLSMExceptions(String connectionid) throws ManagerException {
        String methodName = "getLSMExceptions";
        try {
            this.startMethod(methodName, connectionid);
            StatusService statusService = new StatusService(this.sapphireConnection);
            List<String> list = statusService.getLSMExceptions();
            return list;
        }
        catch (Exception e) {
            this.logError("Failed to get LSM Exceptions", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public DataSet getClassLoaderStats(String connectionid, boolean includeContents) throws ManagerException {
        String methodName = "getClassLoaderStats";
        try {
            this.startMethod(methodName, connectionid);
            StatusService statusService = new StatusService(this.sapphireConnection);
            DataSet dataSet = statusService.getClassLoaderStats(includeContents);
            return dataSet;
        }
        catch (Exception e) {
            this.logError("Failed to get class loader stats", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public DataSet getTableSizes(String connectionid) throws ManagerException {
        String methodName = "getTableSizes";
        try {
            this.startMethod(methodName, connectionid);
            StatusService statusService = new StatusService(this.sapphireConnection);
            DataSet dataSet = statusService.getTableSizes();
            return dataSet;
        }
        catch (Exception e) {
            this.logError("Failed to get table sizes", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public DataSet getMemoryStats(String connectionid) throws ManagerException {
        String methodName = "getMemoryStats";
        try {
            this.startMethod(methodName, connectionid);
            StatusService statusService = new StatusService(this.sapphireConnection);
            DataSet dataSet = statusService.getMemoryStats();
            return dataSet;
        }
        catch (Exception e) {
            this.logError("Failed to get memory stats", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public double getStatsMonitoringValue(String connectionid, String statsmonitorgroupid, String statsmonitoritemid) throws ManagerException {
        String methodName = "getStatsMonitoringData";
        try {
            this.startMethod(methodName, connectionid);
            StatusService statusService = new StatusService(this.sapphireConnection);
            double d = statusService.getStatsMonitoringValue(statsmonitorgroupid, statsmonitoritemid);
            return d;
        }
        catch (Exception e) {
            this.logError("Failed to get monitoring data", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }
}

