/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt.rules;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.services.ConnectionInfo;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.HttpProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DBAccess;
import sapphire.util.LogContext;
import sapphire.util.Logger;

public class BaseRule {
    protected static final String LABVANTAGE_CVS_ID = "$Revision: 59507 $";
    protected DBAccess database;
    protected ConnectionInfo connectionInfo;
    private String connectionId;
    private ActionProcessor actionProcessor;
    private ConnectionProcessor connectionProcessor;
    private DAMProcessor damProcessor;
    private HttpProcessor httpProcessor;
    private QueryProcessor queryProcessor;
    private SDIProcessor sdiProcessor;
    private SDCProcessor sdcProcessor;
    private SequenceProcessor sequenceProcessor;
    private TranslationProcessor translationProcessor;
    private ConfigurationProcessor configurationProcessor;
    protected String logName = this.getClass().getName().substring(this.getClass().getPackage().getName().length() + 1).toUpperCase();
    protected Logger logger = new Logger(new LogContext(this.logName, "(none)"));
    long starttime = 0L;

    public BaseRule() {
    }

    protected BaseRule(DBAccess database, ConnectionInfo connectionInfo) {
        this.database = database;
        this.connectionInfo = connectionInfo;
        this.connectionId = connectionInfo.getConnectionId();
        this.logger.setLogContextConnectionId(connectionInfo.getConnectionId());
        this.logger.setLoggerName(this.logName);
    }

    public void setDatabase(DBAccess database) {
        this.database = database;
    }

    public void setConnectionInfo(ConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
        this.connectionId = connectionInfo.getConnectionId();
    }

    protected void startRule() {
        if (Trace.on) {
            Trace.log("Rule ", "Started " + this.getClass().getName());
        }
        Trace.setStartCodeBlock("Rule: " + this.getClass().getName());
    }

    protected void endRule() {
        Trace.setEndCodeBlock("Rule: " + this.getClass().getName());
    }

    protected void endRule(String message) {
        Trace.setEndCodeBlock("Rule: " + this.getClass().getName() + ", " + message);
    }

    protected ActionProcessor getActionProcessor() {
        if (this.actionProcessor == null) {
            this.actionProcessor = new ActionProcessor(this.connectionId);
        }
        return this.actionProcessor;
    }

    protected ConnectionProcessor getConnectionProcessor() {
        if (this.connectionProcessor == null) {
            this.connectionProcessor = new ConnectionProcessor(this.connectionId);
        }
        return this.connectionProcessor;
    }

    protected DAMProcessor getDAMProcessor() {
        if (this.damProcessor == null) {
            this.damProcessor = new DAMProcessor(this.connectionId);
        }
        return this.damProcessor;
    }

    protected HttpProcessor getHttpProcessor() {
        if (this.httpProcessor == null) {
            this.httpProcessor = new HttpProcessor(this.connectionId);
        }
        return this.httpProcessor;
    }

    protected QueryProcessor getQueryProcessor() {
        if (this.queryProcessor == null) {
            this.queryProcessor = new QueryProcessor(this.connectionId);
        }
        return this.queryProcessor;
    }

    protected SDCProcessor getSDCProcessor() {
        if (this.sdcProcessor == null) {
            this.sdcProcessor = new SDCProcessor(this.connectionId);
        }
        return this.sdcProcessor;
    }

    protected SDIProcessor getSDIProcessor() {
        if (this.sdiProcessor == null) {
            this.sdiProcessor = new SDIProcessor(this.connectionId);
        }
        return this.sdiProcessor;
    }

    protected SequenceProcessor getSequenceProcessor() {
        if (this.sequenceProcessor == null) {
            this.sequenceProcessor = new SequenceProcessor(this.connectionId);
        }
        return this.sequenceProcessor;
    }

    protected TranslationProcessor getTranslationProcessor() {
        if (this.translationProcessor == null) {
            this.translationProcessor = new TranslationProcessor(this.connectionId);
        }
        return this.translationProcessor;
    }

    protected ConfigurationProcessor getConfigurationProcessor() {
        if (this.configurationProcessor == null) {
            this.configurationProcessor = new ConfigurationProcessor(this.connectionId);
        }
        return this.configurationProcessor;
    }
}

