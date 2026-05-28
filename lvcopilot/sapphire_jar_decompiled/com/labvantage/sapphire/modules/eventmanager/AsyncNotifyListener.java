/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.AsyncEvent
 *  javax.servlet.AsyncListener
 */
package com.labvantage.sapphire.modules.eventmanager;

import com.labvantage.sapphire.servlet.NotificationController;
import java.io.IOException;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;

public class AsyncNotifyListener
implements AsyncListener {
    private String subscriberid;
    private String activity;

    public AsyncNotifyListener(String subscriberid) {
        this.subscriberid = subscriberid;
    }

    public void onComplete(AsyncEvent asyncEvent) throws IOException {
        this.servletLog(asyncEvent, "- " + (this.activity != null ? this.activity : "UNKNOWN") + " COMPLETE: " + this.subscriberid);
    }

    public void onTimeout(AsyncEvent asyncEvent) throws IOException {
        this.servletLog(asyncEvent, "- " + (this.activity != null ? this.activity : "UNKNOWN") + " TIMEOUT: " + this.subscriberid);
    }

    public void onError(AsyncEvent asyncEvent) throws IOException {
        this.servletLog(asyncEvent, "- " + (this.activity != null ? this.activity : "UNKNOWN") + " ERROR: " + this.subscriberid + " - " + asyncEvent.getThrowable().getMessage());
    }

    public void onStartAsync(AsyncEvent asyncEvent) throws IOException {
        this.servletLog(asyncEvent, "- " + (this.activity != null ? this.activity : "UNKNOWN") + " STARTING: " + this.subscriberid);
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public void servletLog(AsyncEvent asyncEvent, String message) {
        if (NotificationController.isDebug() && asyncEvent.getSuppliedRequest() != null && asyncEvent.getSuppliedRequest().getServletContext() != null) {
            asyncEvent.getSuppliedRequest().getServletContext().log(message);
        }
    }

    public String toString() {
        return "AsyncNotifyListener:" + this.subscriberid + ":" + this.activity;
    }
}

