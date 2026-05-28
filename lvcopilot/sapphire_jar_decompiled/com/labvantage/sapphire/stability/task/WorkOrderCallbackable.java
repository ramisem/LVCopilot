/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability.task;

import sapphire.util.DBAccess;

public interface WorkOrderCallbackable {
    public void workorderCompleted(String var1, DBAccess var2);
}

