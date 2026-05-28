/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability.task;

import com.labvantage.sapphire.stability.task.CreateWorkOrder;
import com.labvantage.sapphire.stability.task.GridTask;
import java.util.HashMap;

public class CreateMoveWorkOrder
extends CreateWorkOrder
implements GridTask {
    @Override
    public void execute() {
        HashMap<String, String> woProperties = new HashMap<String, String>();
        String toEnvironmentid = this.scheduleProperties.getProperty("toenvironment");
        woProperties.put("toenvironmentid", toEnvironmentid);
        this.createWorkOrders(this.getActionProcessor(), this.getQueryProcessor(), this.getSDCProcessor(), this.scheduleProperties, this.scheduleEvents, "Move", woProperties);
    }

    @Override
    public String getTitle() {
        return "Move";
    }

    @Override
    public String getColor() {
        return "orchid";
    }
}

