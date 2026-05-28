/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package sapphire.util;

import com.labvantage.sapphire.Trace;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import sapphire.util.ConnectionInfo;
import sapphire.util.HttpUtil;
import sapphire.util.LogContext;

public class Logger {
    private LogContext logContext;
    private String loggerName;
    private boolean nolog = false;

    public static boolean isDebugEnabled() {
        return Trace.isDebugEnabled();
    }

    public static void logStackTrace(Throwable t) {
        Trace.logError("Stack Trace", t);
    }

    public static void logError(String message) {
        Trace.logError(message);
    }

    public static void logError(String message, Throwable t) {
        Trace.logError(message, t);
    }

    public static void logError(String loggerName, Object message) {
        Trace.logError(loggerName, message);
    }

    public static void logError(String loggerName, Object message, Throwable t) {
        Trace.logError(loggerName, message, t);
    }

    public static void logInfo(String message) {
        Trace.logInfo(message);
    }

    public static void logInfo(String loggerName, Object message) {
        Trace.logInfo(loggerName, message);
    }

    public static void logWarn(String message) {
        Trace.logWarn(message);
    }

    public static void logWarn(String loggerName, Object message) {
        Trace.logWarn(loggerName, message);
    }

    public static void logDebug(Object message) {
        Trace.logDebug(message);
    }

    public static void logDebug(String loggerName, Object message) {
        Trace.logDebug(loggerName, message);
    }

    public Logger(String connectionid) {
        this.logContext = new LogContext(connectionid);
    }

    public Logger(LogContext logContext) {
        this.logContext = logContext;
        this.loggerName = logContext.getLoggerName();
    }

    public Logger(ConnectionInfo connectionInfo) {
        this.logContext = new LogContext(connectionInfo.getConnectionId());
    }

    public Logger(PageContext pageContext) {
        String file;
        this.logContext = new LogContext(HttpUtil.getConnectionId(pageContext));
        HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
        String page = request.getParameter("page");
        this.loggerName = page != null && page.length() > 0 ? page.toUpperCase() : ((file = request.getParameter("file")) != null && file.length() > 0 ? file.substring(file.lastIndexOf("/") + 1).toUpperCase() : "PAGECONTEXT");
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public void setLogContextConnectionId(String connectionid) {
        this.logContext.setConnectionId(connectionid);
    }

    public void stackTrace(Throwable t) {
        if (this.logging()) {
            Trace.logError("Stack Trace", t);
        }
    }

    public void error(String message) {
        if (this.logging()) {
            Trace.logError(this.loggerName, (Object)message, this.logContext);
        }
    }

    public void error(String message, Throwable t) {
        if (this.logging()) {
            Trace.logError(this.loggerName, message, t, this.logContext);
        }
    }

    public void error(String loggerName, String message) {
        if (this.logging()) {
            Trace.logError(loggerName, (Object)message, this.logContext);
        }
    }

    public void error(String loggerName, String message, Throwable t) {
        if (this.logging()) {
            Trace.logError(loggerName, message, t, this.logContext);
        }
    }

    public void info(String message) {
        if (this.logging()) {
            Trace.logInfo(this.loggerName, message, this.logContext);
        }
    }

    public void info(String loggerName, String message) {
        if (this.logging()) {
            Trace.logInfo(loggerName, message, this.logContext);
        }
    }

    public void warn(String message) {
        if (this.logging()) {
            Trace.logWarn(this.loggerName, message, this.logContext);
        }
    }

    public void warn(String loggerName, String message) {
        if (this.logging()) {
            Trace.logWarn(loggerName, message, this.logContext);
        }
    }

    public void debug(Object message) {
        if (this.logging()) {
            Trace.logDebug(this.loggerName, message, this.logContext);
        }
    }

    public void debug(String loggerName, Object message) {
        if (this.logging()) {
            Trace.logDebug(loggerName, message, this.logContext);
        }
    }

    public void noLog(boolean nolog) {
        this.nolog = nolog;
    }

    protected boolean logging() {
        return !this.nolog;
    }
}

