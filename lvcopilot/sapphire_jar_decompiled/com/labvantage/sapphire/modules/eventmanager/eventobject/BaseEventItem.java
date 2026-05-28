/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventobject;

import java.util.ArrayList;

public abstract class BaseEventItem {
    private boolean hasFired = false;
    private int dataIndex = -1;

    public boolean hasFired() {
        return this.hasFired;
    }

    public void setHasFired(boolean hasFired) {
        this.hasFired = hasFired;
    }

    public int getDataIndex() {
        return this.dataIndex;
    }

    public void setDataIndex(int dataIndex) {
        this.dataIndex = dataIndex;
    }

    public abstract void logEvent(String var1, String var2, String var3, String var4, String var5, String var6, String var7);

    public abstract ArrayList getLogEvents(String var1, String var2, String var3);

    public abstract String toString();
}

