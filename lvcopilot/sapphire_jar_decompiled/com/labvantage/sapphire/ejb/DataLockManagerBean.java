/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.ejb.EJBException
 *  javax.ejb.SessionBean
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.RSet;
import com.labvantage.sapphire.ejb.BaseManager;
import com.labvantage.sapphire.ejb.DataLockManagement;
import com.labvantage.sapphire.ejb.ManagerException;
import com.labvantage.sapphire.services.DataLockService;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;

public class DataLockManagerBean
extends BaseManager
implements SessionBean,
DataLockManagement {
    public DataLockManagerBean() {
        this.logName = "DataLockManager";
    }

    @Override
    public RSet createRSet(String connectionid, RSet rset) throws ManagerException {
        String methodName = "createRSet";
        try {
            this.startMethod(methodName, connectionid);
            DataLockService das = new DataLockService(this.sapphireConnection);
            RSet rSet = das.createRSet(rset);
            return rSet;
        }
        catch (Exception e) {
            this.logError("Failed to create rset", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public RSet lockRSet(String connectionid, RSet rset, String lockOption, int lockScope) throws ManagerException {
        String methodName = "lockRSet";
        try {
            this.startMethod(methodName, connectionid);
            DataLockService das = new DataLockService(this.sapphireConnection);
            RSet rSet = das.lockRSet(rset, lockOption, lockScope);
            return rSet;
        }
        catch (Exception e) {
            this.logError("Failed to lock rset", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public RSet lockRSet(String connectionid, RSet rset, String lockOption, int lockScope, boolean autoTimeout) throws ManagerException {
        String methodName = "lockRSet";
        try {
            this.startMethod(methodName, connectionid);
            DataLockService das = new DataLockService(this.sapphireConnection);
            RSet rSet = das.lockRSet(rset, lockOption, lockScope, autoTimeout);
            return rSet;
        }
        catch (Exception e) {
            this.logError("Failed to lock rset", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public RSet lockRSet(String connectionid, RSet rset, String lockOption, int lockScope, boolean autoTimeout, boolean isValidateCheckout) throws ManagerException {
        String methodName = "lockRSet";
        try {
            this.startMethod(methodName, connectionid);
            DataLockService das = new DataLockService(this.sapphireConnection);
            RSet rSet = das.lockRSet(rset, lockOption, lockScope, autoTimeout, isValidateCheckout);
            return rSet;
        }
        catch (Exception e) {
            this.logError("Failed to lock rset", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public void clearRSet(String connectionid, RSet rset) throws ManagerException {
        String methodName = "clearRSet";
        try {
            this.startMethod(methodName, connectionid);
            DataLockService das = new DataLockService(this.sapphireConnection);
            das.clearRSet(rset);
        }
        catch (Exception e) {
            this.logError("Failed to clear rset", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public void clearLocks(String connectionid, RSet rset) throws ManagerException {
        String methodName = "clearLocks";
        try {
            this.startMethod(methodName, connectionid);
            DataLockService das = new DataLockService(this.sapphireConnection);
            das.clearLocks(rset);
        }
        catch (Exception e) {
            this.logError("Failed to clear locks", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }
}

