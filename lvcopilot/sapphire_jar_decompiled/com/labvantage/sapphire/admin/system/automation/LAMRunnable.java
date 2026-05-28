/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.system.automation;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.system.automation.LAM;
import com.labvantage.sapphire.admin.system.automation.LAMException;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.platform.SapphireDatabase;
import com.labvantage.sapphire.services.SapphireService;
import sapphire.SapphireException;
import sapphire.util.LogContext;
import sapphire.util.Logger;

public abstract class LAMRunnable
implements Runnable {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    protected final String databaseid;
    protected String hostid;
    protected SapphireDatabase database;
    protected String lastError = "";
    protected LAM lam;
    protected String logName = this.getClass().getName().substring(this.getClass().getPackage().getName().length() + 1);
    protected Logger logger;
    protected String traceLogMode;

    protected LAMRunnable(LAM lam, String traceLogMode) {
        this.database = lam.database;
        this.databaseid = lam.database.getDatabaseId();
        this.lam = lam;
        this.traceLogMode = traceLogMode;
        this.logger = new Logger(new LogContext(this.logName, this.getConnectionid()));
        try {
            Configuration configuration = Configuration.getInstance();
            this.hostid = configuration.getHostid();
        }
        catch (SapphireException e) {
            this.hostid = "Unknown";
        }
    }

    protected String getConnectionid() {
        return SapphireService.getInternalConnectionid(this.databaseid);
    }

    @Override
    public void run() {
        Trace.startThreadMDCByDatabaseid(this.databaseid, this.traceLogMode);
        try {
            this.doRun();
        }
        catch (Throwable e) {
            this.lastError = e.getMessage();
            this.logger.error(this.lastError, e);
        }
        Trace.clearThreadMDC();
    }

    public String getLastError() {
        return this.lastError;
    }

    public abstract String doRun() throws LAMException;
}

