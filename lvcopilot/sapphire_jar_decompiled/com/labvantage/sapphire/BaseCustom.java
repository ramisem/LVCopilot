/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import sapphire.util.LogContext;
import sapphire.util.Logger;
import sapphire.util.StringUtil;

public abstract class BaseCustom {
    private String connectionId;
    private ActionProcessor actionProcessor;
    private ConnectionProcessor connectionProcessor;
    private DAMProcessor damProcessor;
    private QueryProcessor queryProcessor;
    private SDIProcessor sdiProcessor;
    private SDCProcessor sdcProcessor;
    private SequenceProcessor sequenceProcessor;
    private TranslationProcessor translationProcessor;
    private ConfigurationProcessor configurationProcessor;
    private HttpProcessor httpProcessor;
    private List departmentList;
    private File rakFile = null;
    protected String logName = this.getClass().getName().substring(this.getClass().getPackage().getName().length() + 1).toUpperCase();
    protected Logger logger = new Logger(new LogContext(this.logName, "(none)"));
    protected final int SUCCESS = 1;
    protected final int FAILURE = 2;

    public File getRakFile() {
        return this.rakFile;
    }

    public void setRakFile(File rakFile) {
        this.rakFile = rakFile;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
        this.logger.setLogContextConnectionId(connectionId);
        this.logger.setLoggerName(this.logName);
    }

    public String getConnectionId() {
        return this.connectionId;
    }

    public String getConnectionid() {
        return this.connectionId;
    }

    public void setLanguage(String language) {
        this.getTranslationProcessor().setLanguage(language);
    }

    protected List getDepartmentList() {
        if (this.departmentList == null) {
            this.departmentList = new ArrayList();
            this.departmentList.addAll(Arrays.asList(StringUtil.split(this.getConnectionProcessor().getSapphireConnection().getDepartmentList(), ";")));
        }
        return this.departmentList;
    }

    protected ActionProcessor getActionProcessor() {
        if (this.actionProcessor == null) {
            this.actionProcessor = this.rakFile == null ? new ActionProcessor(this.connectionId) : new ActionProcessor(this.rakFile, this.connectionId);
        }
        return this.actionProcessor;
    }

    protected ConnectionProcessor getConnectionProcessor() {
        if (this.connectionProcessor == null) {
            this.connectionProcessor = this.rakFile == null ? new ConnectionProcessor(this.connectionId) : new ConnectionProcessor(this.rakFile, this.connectionId);
        }
        return this.connectionProcessor;
    }

    protected DAMProcessor getDAMProcessor() {
        if (this.damProcessor == null) {
            this.damProcessor = this.rakFile == null ? new DAMProcessor(this.connectionId) : new DAMProcessor(this.rakFile, this.connectionId);
        }
        return this.damProcessor;
    }

    protected QueryProcessor getQueryProcessor() {
        if (this.queryProcessor == null) {
            this.queryProcessor = this.rakFile == null ? new QueryProcessor(this.connectionId) : new QueryProcessor(this.rakFile, this.connectionId);
        }
        return this.queryProcessor;
    }

    protected SDCProcessor getSDCProcessor() {
        if (this.sdcProcessor == null) {
            this.sdcProcessor = this.rakFile == null ? new SDCProcessor(this.connectionId) : new SDCProcessor(this.rakFile, this.connectionId);
        }
        return this.sdcProcessor;
    }

    protected SDIProcessor getSDIProcessor() {
        if (this.sdiProcessor == null) {
            this.sdiProcessor = this.rakFile == null ? new SDIProcessor(this.connectionId) : new SDIProcessor(this.rakFile, this.connectionId);
        }
        return this.sdiProcessor;
    }

    protected SequenceProcessor getSequenceProcessor() {
        if (this.sequenceProcessor == null) {
            this.sequenceProcessor = this.rakFile == null ? new SequenceProcessor(this.connectionId) : new SequenceProcessor(this.rakFile, this.connectionId);
        }
        return this.sequenceProcessor;
    }

    protected TranslationProcessor getTranslationProcessor() {
        if (this.translationProcessor == null) {
            this.translationProcessor = this.rakFile == null ? new TranslationProcessor(this.connectionId) : new TranslationProcessor(this.rakFile, this.connectionId);
        }
        return this.translationProcessor;
    }

    protected ConfigurationProcessor getConfigurationProcessor() {
        if (this.configurationProcessor == null) {
            this.configurationProcessor = this.rakFile == null ? new ConfigurationProcessor(this.connectionId) : new ConfigurationProcessor(this.rakFile, this.connectionId);
        }
        return this.configurationProcessor;
    }

    protected HttpProcessor getHttpProcessor() {
        if (this.httpProcessor == null) {
            this.httpProcessor = this.rakFile == null ? new HttpProcessor(this.connectionId) : new HttpProcessor(this.rakFile, this.connectionId);
        }
        return this.httpProcessor;
    }

    protected ArrayList getAccessorErrorIds() {
        ArrayList accessorErrorids = new ArrayList();
        if (this.actionProcessor != null) {
            accessorErrorids.addAll(this.actionProcessor.getErrorCodeStack());
        }
        if (this.connectionProcessor != null) {
            accessorErrorids.addAll(this.connectionProcessor.getErrorCodeStack());
        }
        if (this.damProcessor != null) {
            accessorErrorids.addAll(this.damProcessor.getErrorCodeStack());
        }
        if (this.queryProcessor != null) {
            accessorErrorids.addAll(this.queryProcessor.getErrorCodeStack());
        }
        if (this.sdcProcessor != null) {
            accessorErrorids.addAll(this.sdcProcessor.getErrorCodeStack());
        }
        if (this.sdiProcessor != null) {
            accessorErrorids.addAll(this.sdiProcessor.getErrorCodeStack());
        }
        if (this.sequenceProcessor != null) {
            accessorErrorids.addAll(this.sequenceProcessor.getErrorCodeStack());
        }
        if (this.translationProcessor != null) {
            accessorErrorids.addAll(this.translationProcessor.getErrorCodeStack());
        }
        return accessorErrorids;
    }

    protected ArrayList getAccessorErrorMsgs() {
        ArrayList accessorErrormsgs = new ArrayList();
        if (this.actionProcessor != null) {
            accessorErrormsgs.addAll(this.actionProcessor.getErrorStack());
        }
        if (this.connectionProcessor != null) {
            accessorErrormsgs.addAll(this.connectionProcessor.getErrorStack());
        }
        if (this.damProcessor != null) {
            accessorErrormsgs.addAll(this.damProcessor.getErrorStack());
        }
        if (this.queryProcessor != null) {
            accessorErrormsgs.addAll(this.queryProcessor.getErrorStack());
        }
        if (this.sdcProcessor != null) {
            accessorErrormsgs.addAll(this.sdcProcessor.getErrorStack());
        }
        if (this.sdiProcessor != null) {
            accessorErrormsgs.addAll(this.sdiProcessor.getErrorStack());
        }
        if (this.sequenceProcessor != null) {
            accessorErrormsgs.addAll(this.sequenceProcessor.getErrorStack());
        }
        if (this.translationProcessor != null) {
            accessorErrormsgs.addAll(this.translationProcessor.getErrorStack());
        }
        return accessorErrormsgs;
    }
}

