/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.AsyncContext
 *  javax.servlet.AsyncListener
 */
package com.labvantage.sapphire.modules.eventmanager;

import com.labvantage.sapphire.modules.eventmanager.AsyncNotifyListener;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.NotificationConstants;
import com.labvantage.sapphire.servlet.NotificationController;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncListener;
import sapphire.xml.PropertyList;

public class AsyncNotify
implements NotificationConstants {
    private AsyncContext asyncContext;
    private AsyncNotifyListener asyncNotifyListener;
    private String callback;
    private String connectionid;
    private String starttime = new SimpleDateFormat("yyyy-MMM-dd h:mm:ss").format(Calendar.getInstance().getTime());

    public AsyncNotify(String connectionid, String subscriberid, AsyncContext asyncContext) {
        this.connectionid = connectionid;
        asyncContext.setTimeout(0L);
        this.asyncNotifyListener = new AsyncNotifyListener(subscriberid);
        asyncContext.addListener((AsyncListener)this.asyncNotifyListener);
        this.asyncContext = asyncContext;
        this.callback = asyncContext.getRequest().getParameter("callback");
    }

    public String getConnectionid() {
        return this.connectionid;
    }

    public void servletLog(String message) {
        if (NotificationController.isDebug() && this.asyncContext.getRequest() != null && this.asyncContext.getRequest().getServletContext() != null) {
            this.asyncContext.getRequest().getServletContext().log(message);
        }
    }

    public void unRequest() {
        this.asyncNotifyListener.setActivity("UNREQUEST");
        this.asyncContext.complete();
    }

    public boolean canRespond() {
        return this.asyncContext.getRequest() != null && this.asyncContext.getResponse() != null;
    }

    public void respond(PropertyList response) {
        try {
            if (!this.asyncContext.getResponse().isCommitted()) {
                this.asyncNotifyListener.setActivity("REQUEST");
                response.setProperty("callback", this.callback);
                this.asyncContext.getResponse().getWriter().print(response.toJSONString(false) + "||END||");
            } else {
                this.servletLog("ERROR: Response already committed!");
            }
        }
        catch (IOException e) {
            this.servletLog("ERROR: Cannot write response.\n" + e.getMessage());
        }
        catch (Exception e) {
            this.servletLog("ERROR: Failed to write to async response.\n" + e.getMessage());
        }
        finally {
            try {
                this.asyncContext.complete();
            }
            catch (Exception e) {
                this.servletLog("ERROR: " + e.getMessage());
            }
        }
    }

    public String toString() {
        return "Requested at " + this.starttime + " callback=" + this.callback;
    }
}

