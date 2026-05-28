/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability.task;

import com.labvantage.sapphire.stability.PlanItem;
import com.labvantage.sapphire.stability.ScheduleGrid;
import sapphire.xml.PropertyList;

public interface GridTaskStatus {
    public void statusInit(ScheduleGrid var1, PropertyList var2);

    public String getStatusHTML(PlanItem var1, String var2);
}

