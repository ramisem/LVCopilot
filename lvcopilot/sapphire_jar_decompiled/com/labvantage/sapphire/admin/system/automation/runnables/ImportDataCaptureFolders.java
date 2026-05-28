/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.system.automation.runnables;

import com.labvantage.sapphire.admin.system.automation.LAM;
import com.labvantage.sapphire.admin.system.automation.LAMException;
import com.labvantage.sapphire.admin.system.automation.LAMScheduledRunnable;
import com.labvantage.sapphire.ejb.TDQManagerLocal;
import com.labvantage.sapphire.util.jndi.ServiceLocator;

public class ImportDataCaptureFolders
extends LAMScheduledRunnable {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    public ImportDataCaptureFolders(LAM lam) {
        super(lam, "SDMSCollector");
    }

    @Override
    public String doRun() throws LAMException {
        try {
            TDQManagerLocal tdqManager = ServiceLocator.getInstance().getTDQManager();
            tdqManager.importDataCaptureFolders(this.getConnectionid());
        }
        catch (Exception e) {
            throw new LAMException("Poll DataCapture Folder  failure: " + e.getMessage(), e);
        }
        return "";
    }
}

