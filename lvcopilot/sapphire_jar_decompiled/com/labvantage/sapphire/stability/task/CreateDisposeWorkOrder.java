/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability.task;

import com.labvantage.sapphire.stability.task.CreateWorkOrder;
import com.labvantage.sapphire.stability.task.GridTask;
import com.labvantage.sapphire.stability.task.HasDetails;

public class CreateDisposeWorkOrder
extends CreateWorkOrder
implements GridTask,
HasDetails {
    @Override
    public void execute() {
        this.createWorkOrders(this.getActionProcessor(), this.getQueryProcessor(), this.getSDCProcessor(), this.scheduleProperties, this.scheduleEvents, "Dispose", null);
    }

    @Override
    public String getTitle() {
        return "Dispose";
    }

    @Override
    public String getColor() {
        return "peru";
    }
}

