/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.ejb.EJBException
 *  javax.ejb.SessionBean
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.ejb.ActionManagement;
import com.labvantage.sapphire.ejb.ActionTransManagerLocal;
import com.labvantage.sapphire.ejb.BaseManager;
import com.labvantage.sapphire.ejb.ManagerException;
import com.labvantage.sapphire.modules.eventmanager.EventManager;
import com.labvantage.sapphire.services.ActionService;
import com.labvantage.sapphire.services.AutomationService;
import com.labvantage.sapphire.util.jndi.ServiceLocator;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import sapphire.util.ActionBlock;

public class ActionManagerBean
extends BaseManager
implements SessionBean,
ActionManagement {
    public ActionManagerBean() {
        this.logName = "ActionManager";
    }

    @Override
    public ActionBlock processActionBlock(String connectionid, ActionBlock actionBlock, boolean newTrans, boolean processAsync) throws ManagerException {
        String methodName = "processActionBlock";
        if (newTrans) {
            try {
                ActionTransManagerLocal atm = ServiceLocator.getInstance().getActionTransManager();
                return atm.processActionBlock(connectionid, actionBlock, false, processAsync);
            }
            catch (Exception e) {
                throw new EJBException(e);
            }
        }
        if (!processAsync) {
            try {
                Trace.setStartCodeBlock(this.logName + "." + methodName, actionBlock.toString());
                this.startMethod(methodName, connectionid);
                ActionService actionService = new ActionService(this.sapphireConnection);
                actionService.processActionBlock(actionBlock);
                ActionBlock actionBlock2 = actionBlock;
                return actionBlock2;
            }
            catch (Exception e) {
                this.beforeTransactionAbort();
                if (actionBlock.getErrorHandler() != null && actionBlock.getErrorHandler().hasErrors()) {
                    ActionBlock actionBlock3 = actionBlock;
                    return actionBlock3;
                }
                if (actionBlock.isDebugMode()) {
                    actionBlock.getErrorHandler().add("", "", "Untrapped Error", "FAILURE", "Untrapped error encountered: " + e.getMessage());
                    ActionBlock actionBlock4 = actionBlock;
                    return actionBlock4;
                }
                this.logError("Failed to process action actionblock", e);
                throw new EJBException(e);
            }
            finally {
                this.endMethod(methodName);
                Trace.setEndCodeBlock(this.logName + "." + methodName);
            }
        }
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, actionBlock.toString());
            this.startMethod(methodName, connectionid);
            AutomationService automationService = new AutomationService(this.sapphireConnection);
            actionBlock.setTodolistid(automationService.addToDoListEntry(EventManager.isTestMode() ? "JUnit Test" : "ActionManager", actionBlock, actionBlock.getAsyncDueDt(), true, this.sapphireConnection.getSysuserId().equals("(system)") ? "" : this.sapphireConnection.getSysuserId(), ""));
            actionBlock.setDebugLog("Action Block added to To Do List");
            ActionBlock actionBlock5 = actionBlock;
            return actionBlock5;
        }
        catch (Exception e) {
            this.beforeTransactionAbort();
            this.logError("Failed to add actionblock to to do list", e);
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }
}

