/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.groovy;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.util.format.DateFormatter;
import sapphire.util.LogContext;
import sapphire.util.Logger;

public class GroovyLogger {
    private Logger logger;
    private StringBuffer log;

    public GroovyLogger(LogContext logContext) {
        this.logger = new Logger(logContext);
    }

    public GroovyLogger(LogContext logContext, StringBuffer log) {
        this.logger = new Logger(logContext);
        this.log = log;
    }

    public void info(String message) {
        this.logger.info(message);
        this.addLogEntry(message);
    }

    public void debug(String message) {
        this.logger.debug(message);
        this.addLogEntry(message);
    }

    public void error(String message) {
        this.logger.error(message);
        this.addLogEntry(message);
    }

    public void error(String message, Throwable t) {
        this.logger.error(message, t);
        this.addLogEntry(message);
    }

    private void addLogEntry(String message) {
        if (this.log != null) {
            String now = DateFormatter.formatDateTime(DateTimeUtil.getNowCalendar(), "yyyy-MM-dd HH:mm:ss,SSS");
            this.log.append(now).append(" ").append(message).append("\n");
        }
    }
}

