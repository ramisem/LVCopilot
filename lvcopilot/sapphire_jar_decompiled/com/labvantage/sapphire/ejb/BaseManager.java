/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.ejb.EJBException
 *  org.apache.logging.log4j.Level
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.ejb.BaseSessionBean;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.platform.SapphireDatabase;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.SecurityService;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.jndi.ServiceLocator;
import java.sql.CallableStatement;
import java.sql.SQLException;
import javax.ejb.EJBException;
import javax.sql.DataSource;
import org.apache.logging.log4j.Level;
import sapphire.SapphireException;
import sapphire.util.LogContext;

public abstract class BaseManager
extends BaseSessionBean {
    protected String logName = this.getClass().getName();
    protected SapphireConnection sapphireConnection;
    protected LogContext logContext;

    private void setupSapphireConnection(String connectionid) throws SapphireException {
        this.setupSapphireConnection(connectionid, true);
    }

    private void setupSapphireConnection(String connectionid, boolean keepAlive) throws SapphireException {
        this.setupSapphireConnection(connectionid, keepAlive, false);
    }

    private void setupSapphireConnection(String connectionid, boolean keepAlive, boolean readonly) throws SapphireException {
        String databaseid = SecurityService.getDatabaseId(connectionid);
        DBUtil dbu = null;
        try {
            dbu = this.getDatabase(databaseid);
            this.sapphireConnection = SecurityService.getSapphireConnection(dbu, connectionid, keepAlive);
            if (!readonly) {
                try {
                    String callstmt = "{call lv_app" + (dbu.isOracle() ? "." : "_") + "setappinfouser( ? ) }";
                    CallableStatement cs = dbu.prepareCall(callstmt);
                    cs.setString(1, this.sapphireConnection.getSysuserId());
                    cs.executeUpdate();
                }
                catch (Exception ignore) {
                    this.logInfo("exception: " + ignore.getMessage());
                }
            }
        }
        catch (ServiceException e) {
            if (dbu != null) {
                dbu.setReleaseConnection(true);
            }
            throw new SapphireException("Failed to get sapphire connection", e);
        }
        finally {
            if (dbu != null) {
                dbu.reset();
            }
        }
    }

    protected DBUtil getDatabase(String databaseid) throws SapphireException {
        DBUtil dbu = new DBUtil();
        try {
            SapphireDatabase database = Configuration.getInstance().getSapphireDatabase(databaseid);
            DataSource dataSource = ServiceLocator.getInstance().getDataSource(database.getJndiname());
            dbu.setConnection(database.getDbms(), dataSource.getConnection());
            return dbu;
        }
        catch (SQLException e) {
            throw new SapphireException("Failed to get connection from DataSource", e);
        }
    }

    protected void startMethod(String methodName) {
        if (methodName.length() > 0) {
            this.logContext = new LogContext();
            this.logDebug("START: " + methodName);
        }
    }

    protected void startMethod(String methodName, String connectionid) throws SapphireException {
        if (methodName.length() > 0) {
            this.logContext = new LogContext(connectionid);
            this.logDebug("START: " + methodName);
        }
        this.setupSapphireConnection(connectionid);
    }

    protected void startMethod(String methodName, boolean readonly, String connectionid) throws SapphireException {
        if (methodName.length() > 0) {
            this.logContext = new LogContext(connectionid);
            this.logDebug("START: " + methodName);
        }
        this.setupSapphireConnection(connectionid, true, readonly);
    }

    protected void startMethod(String methodName, String connectionid, boolean keepAlive) throws SapphireException {
        this.startMethod(methodName, false, connectionid, keepAlive, Level.INFO);
    }

    protected void startMethod(String methodName, String connectionid, boolean keepAlive, Level level) throws SapphireException {
        this.startMethod(methodName, false, connectionid, keepAlive, level);
    }

    protected void startMethod(String methodName, boolean readonly, String connectionid, boolean keepAlive, Level level) throws SapphireException {
        if (methodName != null && methodName.length() > 0) {
            this.logContext = new LogContext(connectionid);
            if (level.equals((Object)Level.INFO)) {
                this.logDebug("START: " + methodName);
            } else {
                this.logDebug("START: " + methodName);
            }
        }
        this.setupSapphireConnection(connectionid, keepAlive, readonly);
    }

    protected void endMethod(String methodName) throws EJBException {
        this.endMethod(methodName, Level.INFO);
    }

    protected void endMethod(String methodName, Level level) throws EJBException {
        if (methodName != null && methodName.length() > 0) {
            if (level.equals((Object)Level.INFO)) {
                this.logDebug("END: " + methodName);
            } else {
                this.logDebug("END: " + methodName);
            }
        }
        try {
            if (this.sapphireConnection != null && this.sapphireConnection.getConnection() != null && !this.sapphireConnection.getConnection().isClosed()) {
                this.sapphireConnection.getConnection().close();
            }
        }
        catch (SQLException e) {
            throw new EJBException("Failed to close connection", (Exception)e);
        }
    }

    protected void beforeTransactionAbort() {
        try {
            if (this.sapphireConnection != null) {
                this.logInfo("Aborting Transaction. Delete the Tracelogid (if any) from DB Session.");
                new AuditService(this.sapphireConnection).removeTracelogIdFromDBSession();
            }
        }
        catch (ServiceException e) {
            this.logError("Exception occurred when trying to reset Tracelogid.", e);
        }
    }

    protected void logError(Object out) {
        Trace.logError(this.logName, out, this.logContext);
    }

    protected void logError(Object out, Throwable t) {
        Trace.logError(this.logName, out, t, this.logContext);
    }

    protected void logWarn(Object out) {
        Trace.logWarn(this.logName, out, this.logContext);
    }

    protected void logInfo(Object out) {
        Trace.logInfo(this.logName, out, this.logContext);
    }

    protected void logDebug(Object out) {
        Trace.logDebug(this.logName, out, this.logContext);
    }

    protected void isDebugEnabled() {
        Trace.isDebugEnabled();
    }
}

