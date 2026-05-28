/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements;

import com.labvantage.sapphire.pageelements.PropertyHandler;
import com.labvantage.sapphire.services.ActionService;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.accessor.TranslationProcessor;

public abstract class BasePropertyHandler
extends PropertyHandler {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    private ActionProcessor actionProcessor;
    private ConnectionProcessor connectionProcessor;
    private DAMProcessor damProcessor;
    private QueryProcessor queryProcessor;
    private SDIProcessor sdiProcessor;
    private SDCProcessor sdcProcessor;
    private SequenceProcessor sequenceProcessor;
    private TranslationProcessor translationProcessor;
    private ConfigurationProcessor configurationProcessor;
    private ActionService actionService;

    public ActionProcessor getActionProcessor() {
        if (this.actionProcessor == null) {
            this.actionProcessor = new ActionProcessor(this.connectionInfo.getConnectionId());
        }
        return this.actionProcessor;
    }

    public ConnectionProcessor getConnectionProcessor() {
        if (this.connectionProcessor == null) {
            this.connectionProcessor = new ConnectionProcessor(this.connectionInfo.getConnectionId());
        }
        return this.connectionProcessor;
    }

    public DAMProcessor getDamProcessor() {
        if (this.damProcessor == null) {
            this.damProcessor = new DAMProcessor(this.connectionInfo.getConnectionId());
        }
        return this.damProcessor;
    }

    public QueryProcessor getQueryProcessor() {
        if (this.queryProcessor == null) {
            this.queryProcessor = new QueryProcessor(this.connectionInfo.getConnectionId());
        }
        return this.queryProcessor;
    }

    public SDIProcessor getSdiProcessor() {
        if (this.sdiProcessor == null) {
            this.sdiProcessor = new SDIProcessor(this.connectionInfo.getConnectionId());
        }
        return this.sdiProcessor;
    }

    public SDCProcessor getSdcProcessor() {
        if (this.sdcProcessor == null) {
            this.sdcProcessor = new SDCProcessor(this.connectionInfo.getConnectionId());
        }
        return this.sdcProcessor;
    }

    public SequenceProcessor getSequenceProcessor() {
        if (this.sequenceProcessor == null) {
            this.sequenceProcessor = new SequenceProcessor(this.connectionInfo.getConnectionId());
        }
        return this.sequenceProcessor;
    }

    public TranslationProcessor getTranslationProcessor() {
        if (this.translationProcessor == null) {
            this.translationProcessor = new TranslationProcessor(this.connectionInfo.getConnectionId());
        }
        return this.translationProcessor;
    }

    public ConfigurationProcessor getConfigurationProcessor() {
        if (this.configurationProcessor == null) {
            this.configurationProcessor = new ConfigurationProcessor(this.connectionInfo.getConnectionId());
        }
        return this.configurationProcessor;
    }

    public ActionService getActionService() {
        if (this.actionService == null) {
            this.actionService = new ActionService(this.sapphireConnection);
        }
        return this.actionService;
    }
}

