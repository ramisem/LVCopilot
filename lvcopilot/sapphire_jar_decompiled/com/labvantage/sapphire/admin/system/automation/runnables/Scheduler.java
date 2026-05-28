/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.system.automation.runnables;

import com.labvantage.sapphire.admin.system.automation.LAM;
import com.labvantage.sapphire.admin.system.automation.LAMException;
import com.labvantage.sapphire.admin.system.automation.LAMScheduledRunnable;
import com.labvantage.sapphire.ejb.APQManagerLocal;
import com.labvantage.sapphire.util.jndi.ServiceLocator;
import sapphire.util.DataSet;

public class Scheduler
extends LAMScheduledRunnable {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    public Scheduler(LAM lam) {
        super(lam, "Scheduling");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String doRun() throws LAMException {
        try {
            String connectionid = this.getConnectionid();
            APQManagerLocal apqManager = ServiceLocator.getInstance().getAPQManager();
            apqManager.scheduleEvents(connectionid);
            DataSet duePlanItems = apqManager.getExecutePlanItems(connectionid);
            for (int i = 0; i < duePlanItems.size(); ++i) {
                String scheduleplanitemid;
                String scheduleplanid = duePlanItems.getString(i, "scheduleplanid");
                int updateCount = apqManager.setPlanItemInProcessingFlag(connectionid, scheduleplanid, scheduleplanitemid = duePlanItems.getString(i, "scheduleplanitemid"), "Y");
                if (updateCount != 1) continue;
                try {
                    apqManager.executeScheduleEvents(connectionid, duePlanItems.getString(i, "scheduleplanid"), duePlanItems.getString(i, "scheduleplanitemid"), duePlanItems.getString(i, "propertytreeid"), duePlanItems.getString(i, "scheduletasknodeid"), duePlanItems.getString(i, "objectname"));
                    continue;
                }
                catch (Exception e) {
                    apqManager.setScheduleEventError(connectionid, duePlanItems.getString(i, "scheduleplanid"), duePlanItems.getString(i, "scheduleplanitemid"), e.getMessage());
                    continue;
                }
                finally {
                    apqManager.setPlanItemInProcessingFlag(connectionid, scheduleplanid, scheduleplanitemid, "N");
                }
            }
        }
        catch (Exception e) {
            throw new LAMException("Scheduler failure: " + e.getMessage(), e);
        }
        return "";
    }
}

