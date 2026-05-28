/*
 * Decompiled with CFR 0.152.
 */
package sapphire.servlet;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.servlet.command.BaseRequest;
import java.io.PrintWriter;
import sapphire.servlet.BaseHttpServlet;

public abstract class BaseAjaxRequest
extends BaseRequest {
    private PrintWriter out;
    protected String debugErrorMsg;
    private BaseHttpServlet servlet;

    public boolean acceptContentType(String contentType) {
        Trace.logDebug("Checking the request contenttype: " + contentType);
        return contentType != null && (contentType.toLowerCase().startsWith("application/x-www-form-urlencoded") || contentType.equalsIgnoreCase("application/json"));
    }

    public BaseHttpServlet getServlet() {
        return this.servlet;
    }

    public void setServlet(BaseHttpServlet servlet) {
        this.servlet = servlet;
    }

    public final void open(PrintWriter out) {
        this.out = out;
    }

    public final void close() {
        if (this.out != null) {
            this.out.flush();
            this.out.close();
        }
    }

    protected final void write(Object output) {
        this.print(output);
    }

    protected final void print(Object output) {
        this.out.print(output);
    }

    protected final void println(Object output) {
        this.out.println(output);
    }

    protected void logDebug(String msg) {
        this.logger.debug(msg);
    }

    protected void logInfo(String msg) {
        this.logger.info(msg);
    }

    protected void logWarn(String msg) {
        this.logger.warn(msg);
    }

    protected void logError(String msg, Throwable exception) {
        this.debugErrorMsg = msg;
        this.logger.error(msg, exception);
    }

    protected void logError(String msg) {
        this.debugErrorMsg = msg;
        this.logger.error(msg);
    }
}

