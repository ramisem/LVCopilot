/*
 * Decompiled with CFR 0.152.
 */
package sapphire.util;

public class LogContext {
    public static final String BLANK_CONNECTIONID = "(none)";
    private String connectionid;
    private String loggerName;

    public LogContext() {
        this.connectionid = BLANK_CONNECTIONID;
    }

    public LogContext(String connectionid) {
        this.connectionid = connectionid;
    }

    public LogContext(String loggerName, String connectionid) {
        this.loggerName = loggerName;
        this.connectionid = connectionid;
    }

    public String getConnectionId() {
        return this.connectionid == null ? "" : this.connectionid;
    }

    public String getSysuserid() {
        return this.connectionid == null ? "" : this.connectionid;
    }

    public void setConnectionId(String connectionid) {
        this.connectionid = connectionid;
    }

    public String getLoggerName() {
        return this.loggerName == null ? "" : this.loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }
}

