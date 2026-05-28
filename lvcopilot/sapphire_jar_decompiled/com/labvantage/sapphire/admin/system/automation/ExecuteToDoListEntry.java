/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.codahale.metrics.MetricRegistry
 *  com.codahale.metrics.Timer
 */
package com.labvantage.sapphire.admin.system.automation;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.system.automation.LAM;
import com.labvantage.sapphire.admin.system.automation.LAMRunnable;
import com.labvantage.sapphire.ejb.TDQManagerLocal;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.platform.SapphireDatabase;
import com.labvantage.sapphire.services.AutomationService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.SecurityService;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.jndi.ServiceLocator;
import javax.sql.DataSource;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;

public class ExecuteToDoListEntry
extends LAMRunnable {
    private final String todolistid;
    private static int executionCount = 0;
    private static final String metricsPrefix = "todolist.actions";

    public ExecuteToDoListEntry(LAM lam, String todolistid) {
        super(lam, "ToDoList");
        this.todolistid = todolistid;
    }

    private static synchronized void increaseExecutionCount() {
        ++executionCount;
    }

    public static int getExecutionCount() {
        return executionCount;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String doRun() {
        this.logger.info("Executing ToDoListEntry " + this.todolistid);
        String connectionid = this.getConnectionid();
        QueryProcessor qp = new QueryProcessor(connectionid);
        DataSet entryDetails = qp.getPreparedSqlDataSet("SELECT actionid, propertyclob FROM todolist WHERE todolistid = ?", new Object[]{this.todolistid}, true);
        String actionid = "Unknown";
        boolean isNoTransaction = false;
        if (entryDetails.size() == 1) {
            String propertyClob = entryDetails.getClob(0, "propertyclob");
            actionid = entryDetails.getClob(0, "actionid");
            isNoTransaction = propertyClob != null && propertyClob.contains("processnotransaction");
        }
        Timer timer = this.lam.getMetricRegistry().timer(MetricRegistry.name((String)metricsPrefix, (String[])new String[]{actionid}));
        TDQManagerLocal tdqManager = null;
        try {
            tdqManager = ServiceLocator.getInstance().getTDQManager();
        }
        catch (SapphireException e) {
            Trace.logError("Failed to get hold of the APQManager");
        }
        if (tdqManager != null) {
            ExecuteToDoListEntry.increaseExecutionCount();
            if (isNoTransaction) {
                try {
                    DBUtil dbu = new DBUtil(connectionid);
                    SapphireDatabase database = Configuration.getInstance().getSapphireDatabase(this.databaseid);
                    DataSource dataSource = ServiceLocator.getInstance().getDataSource(database.getJndiname());
                    dbu.setConnection(database.getDbms(), dataSource.getConnection());
                    SapphireConnection sapphireConnection = SecurityService.getSapphireConnection(dbu, connectionid);
                    AutomationService automationService = new AutomationService(sapphireConnection);
                    try {
                        automationService.processToDoListEntry(this.todolistid);
                        automationService.deleteToDoListEntry(this.todolistid);
                    }
                    catch (ServiceException e) {
                        automationService.setToDoListError(this.todolistid, e.getMessage());
                    }
                    finally {
                        dbu.reset();
                    }
                }
                catch (Exception e) {
                    tdqManager.setToDoListError(connectionid, this.todolistid, e.getMessage());
                    this.logger.error("Failed to create a no-trans database connection. Reason: " + e.getMessage(), e);
                }
            } else {
                try {
                    if (tdqManager.processToDoListEntry(connectionid, this.todolistid)) {
                        tdqManager.deleteToDoListEntry(connectionid, this.todolistid);
                    }
                }
                catch (Exception e) {
                    tdqManager.setToDoListError(connectionid, this.todolistid, e.getMessage());
                    this.logger.error("Failed to process todolist entry. Reason: " + e.getMessage(), e);
                }
            }
        }
        timer.time().stop();
        return "";
    }
}

