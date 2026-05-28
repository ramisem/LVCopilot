/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.system.automation;

public class Server
implements Comparable<Server> {
    public String hostid;
    public String applicationid;
    public boolean thisServer;
    public boolean isAutomationServer;
    public Integer collectorWeight = 0;

    public String toString() {
        return "Host: " + this.hostid + ", Application: " + this.applicationid;
    }

    @Override
    public int compareTo(Server server) {
        int result = this.collectorWeight.compareTo(server.collectorWeight);
        if (result == 0) {
            result = this.hostid.compareTo(server.hostid);
        }
        return result;
    }

    public void addCollectorWeight(int weight) {
        this.collectorWeight = this.collectorWeight + weight;
    }
}

