/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.webservices.messages;

import java.io.Serializable;

public abstract class BaseSECMessage
implements Serializable {
    protected static final int FLOW_IN = 0;
    protected static final int FLOW_OUT = 1;
    private String typeId = "";
    private String id = "";
    private String version = "1";
    private String error = "";
    private String status = "";
    private String log = "";
    private DataFlow msgFlow = DataFlow.IN;

    public BaseSECMessage() {
    }

    public BaseSECMessage(DataFlow flow, String messageTypeId, String messageVersion) {
        this.msgFlow = flow;
        this.typeId = messageTypeId;
        this.version = messageVersion;
    }

    public String getLog() {
        return this.log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getError() {
        return this.error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMsgFlow() {
        switch (this.msgFlow) {
            case IN: {
                return 0;
            }
            case OUT: {
                return 1;
            }
        }
        return 0;
    }

    public void setMsgFlow(int msgFlow) {
        switch (msgFlow) {
            case 0: {
                this.msgFlow = DataFlow.IN;
                break;
            }
            case 1: {
                this.msgFlow = DataFlow.OUT;
                break;
            }
            default: {
                this.msgFlow = DataFlow.IN;
            }
        }
    }

    public String getTypeId() {
        return this.typeId;
    }

    public void setTypeId(String value) {
        this.typeId = value;
    }

    public abstract void fromMessage(String var1);

    public String toString() {
        return this.toMessage();
    }

    public abstract String toMessage();

    public static enum DataFlow {
        IN,
        OUT;

    }
}

