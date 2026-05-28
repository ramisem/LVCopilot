/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.system.automation;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.system.automation.LAM;
import com.labvantage.sapphire.admin.system.automation.LAMRunnable;
import com.labvantage.sapphire.services.AutomationService;
import java.util.Calendar;

public abstract class LAMScheduledRunnable
extends LAMRunnable {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    protected boolean active = true;
    private long lastRunLog = 0L;
    private int interval;
    private int runCount = 0;
    private Calendar lastRun = null;
    private boolean isForPrimaryAutomationServerOnly = false;
    private boolean isForAnyAutomationServer = true;
    private String lastMessage = "";
    private String lastError = "";

    protected LAMScheduledRunnable(LAM lam, String traceLogMode) {
        super(lam, traceLogMode);
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public void setActive(boolean active) {
        this.logger.info(this.getClass().getName() + " has been " + (active ? "Resumed" : "Paused"));
        this.active = active;
    }

    public boolean isActive() {
        return this.active;
    }

    public int getInterval() {
        return this.interval;
    }

    public Calendar getLastRun() {
        return this.lastRun;
    }

    public int getRunCount() {
        return this.runCount;
    }

    public boolean isForPrimaryAutomationServerOnly() {
        return this.isForPrimaryAutomationServerOnly;
    }

    public boolean isForAnyAutomationServer() {
        return this.isForAnyAutomationServer;
    }

    public String getLastMessage() {
        return this.lastMessage;
    }

    public String getName() {
        return this.getClass().getName().substring(this.getClass().getPackage().getName().length() + 1);
    }

    @Override
    public void run() {
        if (this.active) {
            try {
                boolean skip;
                Trace.startThreadMDCByDatabaseid(this.databaseid, this.traceLogMode);
                boolean bl = skip = this.isForAnyAutomationServer && !AutomationService.isAutomationServer(this.databaseid);
                if (!this.isForPrimaryAutomationServerOnly && !skip || AutomationService.isPrimaryAutomationServer(this.databaseid)) {
                    if (System.currentTimeMillis() - this.lastRunLog > 300000L) {
                        this.logger.info(this.getClass().getName() + " is running every " + this.interval + "s");
                    }
                    this.lastRunLog = System.currentTimeMillis();
                    this.lastRun = Calendar.getInstance();
                    ++this.runCount;
                    String message = this.doRun();
                    if (message != null && message.length() > 0) {
                        this.lastMessage = message;
                    }
                }
            }
            catch (Throwable e) {
                this.lastError = e.getMessage();
                this.logger.error(this.lastError, e);
            }
            finally {
                Trace.clearThreadMDC();
            }
        }
    }

    public void setIsForPrimaryAutomationServerOnly(boolean isAutomationServerOnly) {
        this.isForPrimaryAutomationServerOnly = isAutomationServerOnly;
    }

    public void setIsForAnyAutomationServer(boolean isForAnyAutomationServer) {
        this.isForAnyAutomationServer = isForAnyAutomationServer;
    }
}

