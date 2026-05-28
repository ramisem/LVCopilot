/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.services.SapphireConnection;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.error.ErrorHandler;
import sapphire.util.LogContext;

public abstract class PropertyHandler {
    private LogContext logContext = new LogContext();
    protected ConnectionInfo connectionInfo;
    protected SapphireConnection sapphireConnection;
    protected String logName = this.getClass().getName();
    private ErrorHandler errorHandler;
    protected String debugErrorMsg;

    public ConnectionInfo getConnectionInfo() {
        return this.connectionInfo;
    }

    public void setSapphireConnection(SapphireConnection sapphireConnection) {
        this.sapphireConnection = sapphireConnection;
        this.connectionInfo = sapphireConnection;
        this.logContext.setConnectionId(sapphireConnection.getConnectionId());
    }

    public abstract void processProperties(HashMap var1) throws SapphireException;

    public void logDebug(String sMsg) {
        Trace.logDebug(this.logName, sMsg, this.logContext);
    }

    public void logInfo(String sMsg) {
        Trace.logInfo(this.logName, sMsg, this.logContext);
    }

    public void logWarn(String sMsg) {
        Trace.logWarn(this.logName, sMsg, this.logContext);
    }

    public void logError(String sMsg) {
        this.debugErrorMsg = sMsg;
        Trace.logError(this.logName, (Object)sMsg, this.logContext);
    }

    public void logError(String sMsg, Throwable exception) {
        this.debugErrorMsg = sMsg;
        Trace.logError(this.logName, sMsg, exception, this.logContext);
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public ErrorHandler getErrorHandler() {
        if (this.errorHandler == null) {
            this.errorHandler = new ErrorHandler();
        }
        return this.errorHandler;
    }
}

