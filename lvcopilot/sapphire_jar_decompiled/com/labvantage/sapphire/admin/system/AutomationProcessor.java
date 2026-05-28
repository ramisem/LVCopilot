/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.admin.system;

import com.labvantage.sapphire.BaseAccessor;
import com.labvantage.sapphire.admin.system.automation.LAM;
import com.labvantage.sapphire.admin.system.automation.LAMException;
import com.labvantage.sapphire.ejb.TDQManagerLocal;
import com.labvantage.sapphire.services.SapphireService;
import com.labvantage.sapphire.util.jndi.ServiceLocator;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.xml.PropertyList;

public class AutomationProcessor
extends BaseAccessor {
    public AutomationProcessor(String connectionid) {
        super(connectionid);
    }

    public AutomationProcessor(PageContext pageContext) {
        super(pageContext);
    }

    public PropertyList getToDoListProperties(String todolistid) throws SapphireException {
        try {
            return this.getAutomationManager().getToDoListProperties(this.getConnectionid(), todolistid);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to get todolist properties", e);
        }
    }

    public void loadWebPageCache(String databaseid) throws SapphireException {
        try {
            this.getAutomationManager().loadWebPageCache(databaseid);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to initialize webpage cache", e);
        }
    }

    public LAM createLAM() throws LAMException {
        try {
            return this.getAutomationManager().createLAM(this.getConnectionid());
        }
        catch (Exception e) {
            throw new LAMException("Unable to create LAM", e);
        }
    }

    public void resetAutomation() throws LAMException {
        try {
            this.getAutomationManager().destroyLAM(this.getConnectionid());
            LAM lam = this.getAutomationManager().createLAM(this.getConnectionid());
            ConnectionProcessor cp = new ConnectionProcessor(this.getConnectionid());
            SapphireService.configureAutomation(lam, cp, cp.getSapphireConnection());
        }
        catch (Exception e) {
            throw new LAMException("Unable to reset automation", e);
        }
    }

    public void pausePoller(String connectionid, int type) throws LAMException {
        try {
            this.getAutomationManager().pausePoller(this.getConnectionid(), type);
        }
        catch (Exception e) {
            throw new LAMException("Unable to pause Poller", e);
        }
    }

    public void resumePoller(String connectionid, int type) throws LAMException {
        try {
            this.getAutomationManager().resumePoller(this.getConnectionid(), type);
        }
        catch (Exception e) {
            throw new LAMException("Unable to resume Poller", e);
        }
    }

    public void performHousekeeping() throws SapphireException {
        try {
            TDQManagerLocal tdqManager = ServiceLocator.getInstance().getTDQManager();
            tdqManager.performHouseKeeping(this.getConnectionid());
        }
        catch (Exception e) {
            throw new SapphireException("Unable to perform housekeeping", e);
        }
    }
}

