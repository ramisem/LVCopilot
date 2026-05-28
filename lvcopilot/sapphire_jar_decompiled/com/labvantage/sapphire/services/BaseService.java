/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.services;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.util.ConnectionInfo;
import sapphire.util.LogContext;

public class BaseService
extends BaseCustom {
    public static final String NULL = "(null)";
    protected String logName = this.getClass().getName();
    protected ConnectionInfo connectionInfo;
    protected SapphireConnection sapphireConnection;
    protected LogContext logContext;
    protected boolean nolog = false;

    public BaseService() {
    }

    public BaseService(SapphireConnection sapphireConnection) {
        this.sapphireConnection = sapphireConnection;
        this.connectionInfo = new ConnectionInfo(sapphireConnection);
        this.setConnectionId(sapphireConnection.getConnectionId());
        this.logContext = new LogContext(sapphireConnection.getConnectionId());
    }

    protected void logError(Object out) {
        if (this.logging()) {
            Trace.logError(this.logName, out, this.logContext);
        }
    }

    protected void logError(Object out, Throwable t) {
        if (this.logging()) {
            Trace.logError(this.logName, out, t, this.logContext);
        }
    }

    protected void logWarn(Object out) {
        if (this.logging()) {
            Trace.logWarn(this.logName, out, this.logContext);
        }
    }

    protected void logInfo(Object out) {
        if (this.logging()) {
            Trace.logInfo(this.logName, out, this.logContext);
        }
    }

    protected void logDebug(Object out) {
        if (this.logging()) {
            Trace.logDebug(this.logName, out, this.logContext);
        }
    }

    protected boolean isDebugEnabled() {
        return Trace.isDebugEnabled();
    }

    protected void noLog(boolean nolog) {
        this.nolog = nolog;
        if (this.logger != null) {
            this.logger.noLog(nolog);
        }
    }

    protected boolean logging() {
        return !this.nolog;
    }
}

