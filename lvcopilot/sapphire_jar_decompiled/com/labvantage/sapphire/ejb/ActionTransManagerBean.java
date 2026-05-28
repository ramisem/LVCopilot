/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.ejb.EJBException
 *  javax.ejb.SessionBean
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.ejb.ActionManagement;
import com.labvantage.sapphire.ejb.BaseManager;
import com.labvantage.sapphire.ejb.ManagerException;
import com.labvantage.sapphire.services.ActionService;
import com.labvantage.sapphire.services.AutomationService;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import sapphire.util.ActionBlock;

public class ActionTransManagerBean
extends BaseManager
implements SessionBean,
ActionManagement {
    public ActionTransManagerBean() {
        this.logName = "ActionTransManager";
    }

    @Override
    public ActionBlock processActionBlock(String connectionid, ActionBlock actionBlock, boolean newTrans, boolean processAsync) throws ManagerException {
        String methodName = "processActionBlock";
        if (!processAsync) {
            try {
                this.startMethod(methodName, connectionid);
                ActionService actionService = new ActionService(this.sapphireConnection);
                actionService.processActionBlock(actionBlock);
                ActionBlock actionBlock2 = actionBlock;
                return actionBlock2;
            }
            catch (Exception e) {
                this.beforeTransactionAbort();
                if (actionBlock.getErrorHandler().hasErrors()) {
                    this.sessionContext.setRollbackOnly();
                    ActionBlock actionBlock3 = actionBlock;
                    return actionBlock3;
                }
                this.logError("Failed to process action actionblock", e);
                throw new EJBException(e);
            }
            finally {
                this.endMethod(methodName);
            }
        }
        try {
            this.startMethod(methodName, connectionid);
            AutomationService automationService = new AutomationService(this.sapphireConnection);
            automationService.addToDoListEntry("ActionTransManager", actionBlock, actionBlock.getAsyncDueDt(), true, this.sapphireConnection.getSysuserId(), "");
            actionBlock.setDebugLog("Action Block added to To Do List");
            ActionBlock actionBlock4 = actionBlock;
            return actionBlock4;
        }
        catch (Exception e) {
            this.logError("Failed to add actionblock to to do list", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }
}

