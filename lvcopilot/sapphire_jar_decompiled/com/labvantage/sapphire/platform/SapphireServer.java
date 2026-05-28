/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.platform;

import java.io.Serializable;

public class SapphireServer
implements Serializable {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private String serverid;
    private String serverdesc;
    private boolean webServer;
    private boolean automationServer;
    private String hostname;
    private String port;
    private String syncUsername;
    private String syncPassword;

    public String getServerid() {
        return this.serverid;
    }

    public void setServerid(String serverid) {
        this.serverid = serverid;
    }

    public String getServerdesc() {
        return this.serverdesc;
    }

    public void setServerdesc(String serverdesc) {
        this.serverdesc = serverdesc;
    }

    public boolean isWebServer() {
        return this.webServer;
    }

    public void setWebServer(boolean webServer) {
        this.webServer = webServer;
    }

    public boolean isAutomationServer() {
        return this.automationServer;
    }

    public void setAutomationServer(boolean automationServer) {
        this.automationServer = automationServer;
    }

    public String getHostname() {
        return this.hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getPort() {
        return this.port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getSyncUsername() {
        return this.syncUsername;
    }

    public void setSyncUsername(String syncUsername) {
        this.syncUsername = syncUsername;
    }

    public String getSyncPassword() {
        return this.syncPassword;
    }

    public void setSyncPassword(String syncPassword) {
        this.syncPassword = syncPassword;
    }

    public String toString() {
        return this.hostname;
    }
}

