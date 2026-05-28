/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.ejb.EJBException
 *  javax.ejb.SessionBean
 *  org.apache.logging.log4j.Level
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.ejb.BaseManager;
import com.labvantage.sapphire.ejb.ManagerException;
import com.labvantage.sapphire.ejb.SecurityManagement;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.SecurityService;
import com.labvantage.sapphire.services.ServiceException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import org.apache.logging.log4j.Level;

public class SecurityManagerBean
extends BaseManager
implements SessionBean,
SecurityManagement {
    public SecurityManagerBean() {
        this.logName = "SecurityManager";
    }

    @Override
    public String getConnectionId(SapphireConnection sapphireConnection) throws ManagerException {
        String methodName = "getConnectionId";
        DBUtil database = null;
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, sapphireConnection.toString());
            this.startMethod(methodName);
            database = this.getDatabase(sapphireConnection.getDatabaseId());
            sapphireConnection.setDbms(database.getDbms());
            sapphireConnection.setConnection(database.getConnection());
            String string = SecurityService.getConnectionId(sapphireConnection);
            return string;
        }
        catch (Exception e) {
            this.logError("Failed to get connectionid", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            if (database != null) {
                database.reset();
                database.releaseConnection();
            }
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public SapphireConnection getSapphireConnection(String connectionid) throws ManagerException {
        try {
            this.startMethod("", true, connectionid, false, Level.DEBUG);
            this.sapphireConnection.getConnection().close();
            this.sapphireConnection.setConnection(null);
        }
        catch (Exception e) {
            this.logError("Failed to get SapphireConnection for connectionid '" + connectionid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod("", Level.DEBUG);
        }
        return this.sapphireConnection;
    }

    @Override
    public void changePassword(String databaseid, String sysuserid, String oldpassword, String newpassword) throws ManagerException {
        String methodName = "changePassword";
        DBUtil database = null;
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, databaseid + ";" + sysuserid);
            this.startMethod(methodName);
            database = this.getDatabase(databaseid);
            SecurityService.changePassword(database, databaseid, sysuserid, oldpassword, newpassword);
        }
        catch (Exception e) {
            this.logError("Failed to change password for sysuserid '" + sysuserid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            if (database != null) {
                database.reset();
                database.releaseConnection();
            }
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public boolean isValidPassword(String databaseid, String sysuserid, String password) throws ManagerException {
        String methodName = "isValidPassword";
        DBUtil database = null;
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, databaseid + ";" + sysuserid);
            this.startMethod(methodName);
            database = this.getDatabase(databaseid);
            boolean bl = SecurityService.isValidPassword(database, sysuserid, password);
            return bl;
        }
        catch (Exception e) {
            this.logError("Failed to check password for sysuserid '" + sysuserid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            if (database != null) {
                database.reset();
                database.releaseConnection();
            }
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void clearConnection(String connectionid, String clearConnectionid) throws ManagerException {
        String methodName = "clearConnection";
        try {
            Trace.setStartCodeBlock(this.logName + "." + clearConnectionid);
            this.startMethod(methodName, connectionid);
            SecurityService securityService = new SecurityService(this.sapphireConnection);
            securityService.clearConnection(clearConnectionid, false);
        }
        catch (Exception e) {
            this.logError("Failed to clear connectionid '" + clearConnectionid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void prepareToDeleteConnection(String connectionid, String deleteConnectionid) throws ManagerException {
        String methodName = "prepareToDeleteConnection";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, deleteConnectionid);
            this.startMethod(methodName, connectionid);
            SecurityService securityService = new SecurityService(this.sapphireConnection);
            securityService.prepareToDeleteConnection(deleteConnectionid);
        }
        catch (Exception e) {
            this.logError("Failed to prepare connectionid '" + deleteConnectionid + "' for delete", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public boolean checkUser(String connectionid, String sysuserid, String password) throws ManagerException {
        String methodName = "checkUser";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, sysuserid);
            this.startMethod(methodName, connectionid);
            SecurityService securityService = new SecurityService(this.sapphireConnection);
            boolean bl = securityService.checkUser(sysuserid, password);
            return bl;
        }
        catch (Exception e) {
            this.logError("Failed to check user '" + sysuserid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    /*
     * Loose catch block
     */
    @Override
    public boolean checkConnection(String connectionid, boolean checkCacheFirst) throws ManagerException {
        DBUtil database = null;
        try {
            this.startMethod("");
            database = new DBUtil();
            database = this.getDatabase(SecurityService.getDatabaseId(connectionid));
            if (checkCacheFirst) {
                try {
                    SapphireConnection sapphireConnection = SecurityService.getSapphireConnection(database, connectionid, false);
                    boolean bl = sapphireConnection.getDeleteDt() == null || SecurityService.checkConnection(database, connectionid);
                    return bl;
                }
                catch (ServiceException e) {
                    boolean bl;
                    block11: {
                        bl = false;
                        if (database == null) break block11;
                        database.reset();
                        database.releaseConnection();
                    }
                    this.endMethod("");
                    return bl;
                }
            }
            boolean e = SecurityService.checkConnection(database, connectionid);
            return e;
            {
                catch (Exception e2) {
                    this.logError("Failed to check connectionid '" + connectionid + "'", e2);
                    this.beforeTransactionAbort();
                    throw new EJBException(e2);
                }
                catch (Throwable throwable) {
                    throw throwable;
                }
            }
        }
        finally {
            if (database != null) {
                database.reset();
                database.releaseConnection();
            }
            this.endMethod("");
        }
    }

    @Override
    public void disableUser(String connectionid, String sysuserid, String disableReason) throws ManagerException {
        String methodName = "disableUser";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, sysuserid + ";" + disableReason);
            this.startMethod(methodName, connectionid);
            SecurityService securityService = new SecurityService(this.sapphireConnection);
            securityService.disableUser(sysuserid, disableReason);
        }
        catch (Exception e) {
            this.logError("Failed to disable user '" + sysuserid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void enableUser(String connectionid, String sysuserid) throws ManagerException {
        String methodName = "enableUser";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, sysuserid);
            this.startMethod(methodName, connectionid);
            SecurityService securityService = new SecurityService(this.sapphireConnection);
            securityService.enableUser(sysuserid);
        }
        catch (Exception e) {
            this.logError("Failed to enable user '" + sysuserid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void forcePasswordChange(String connectionid, String sysuserid) throws ManagerException {
        String methodName = "forcePasswordChange";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, sysuserid);
            this.startMethod(methodName, connectionid);
            SecurityService securityService = new SecurityService(this.sapphireConnection);
            securityService.forcePasswordChange(sysuserid);
        }
        catch (Exception e) {
            this.logError("Failed to force password change for user '" + sysuserid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }
}

