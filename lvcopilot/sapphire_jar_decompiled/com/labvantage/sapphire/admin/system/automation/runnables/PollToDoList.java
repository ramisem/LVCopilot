/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.system.automation.runnables;

import com.labvantage.sapphire.admin.system.automation.LAM;
import com.labvantage.sapphire.admin.system.automation.LAMException;
import com.labvantage.sapphire.admin.system.automation.LAMScheduledRunnable;

public class PollToDoList
extends LAMScheduledRunnable {
    public PollToDoList(LAM lam) {
        super(lam, "ToDoList");
    }

    @Override
    public String doRun() throws LAMException {
        try {
            this.lam.scheduledPollImmediate();
        }
        catch (Exception e) {
            throw new LAMException("Failed to Poll ToDoList: " + e.getMessage(), e);
        }
        return "";
    }
}

