/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.ejb.EJBException
 *  javax.ejb.SessionBean
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.ejb.APQManagement;
import com.labvantage.sapphire.ejb.BaseManager;
import com.labvantage.sapphire.ejb.ManagerException;
import com.labvantage.sapphire.services.ScheduleService;
import java.util.Calendar;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import sapphire.util.DataSet;

public class APQManagerBean
extends BaseManager
implements SessionBean,
APQManagement {
    public APQManagerBean() {
        this.logName = "APQManager";
    }

    @Override
    public void logInfo(Object out) {
        Trace.logDebug(this.logName, out, this.logContext);
    }

    @Override
    public void scheduleTasks(String connectionid) throws ManagerException {
        String methodName = "scheduleTasks";
        try {
            this.startMethod(methodName, connectionid);
            ScheduleService scheduleService = new ScheduleService(this.sapphireConnection);
            scheduleService.scheduleTasks();
        }
        catch (Exception e) {
            this.logError("Failed schedule tasks", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public void scheduleEvents(String connectionid) throws ManagerException {
        String methodName = "scheduleEvents";
        try {
            this.startMethod(methodName, connectionid);
            ScheduleService scheduleService = new ScheduleService(this.sapphireConnection);
            scheduleService.scheduleEvents(Calendar.getInstance());
        }
        catch (Exception e) {
            this.logError("Failed schedule events", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public int setPlanItemInProcessingFlag(String connectionid, String scheduleplanid, String scheduleplanitemid, String flag) throws ManagerException {
        String methodName = "setPlanItemProcessingFlag";
        int updateCount = 0;
        try {
            this.startMethod(methodName, connectionid);
            ScheduleService scheduleService = new ScheduleService(this.sapphireConnection);
            updateCount = scheduleService.setPlanItemInProcessingFlag(scheduleplanid, scheduleplanitemid, flag);
        }
        catch (Exception e) {
            this.logError("Failed update scheduleplanitem processingflag", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
        return updateCount;
    }

    @Override
    public void executeScheduleEvents(String connectionid, String scheduleplanid, String scheduleplanitemid, String propertytreeid, String scheduletasknodeid, String objectname) throws ManagerException {
        String methodName = "executeScheduleEvents";
        try {
            this.startMethod(methodName, connectionid);
            ScheduleService scheduleService = new ScheduleService(this.sapphireConnection);
            scheduleService.executeEvents(scheduleplanid, scheduleplanitemid, propertytreeid, scheduletasknodeid, objectname, Calendar.getInstance());
        }
        catch (Exception e) {
            this.logError("Failed execute scheduled events", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public void setScheduleEventError(String connectionid, String scheduleplanid, String scheduleplanitemid, String errorText) throws ManagerException {
        String methodName = "setScheduleEventError";
        try {
            this.startMethod(methodName, connectionid);
            ScheduleService scheduleService = new ScheduleService(this.sapphireConnection);
            scheduleService.setScheduleEventError(scheduleplanid, scheduleplanitemid, Calendar.getInstance());
        }
        catch (Exception e) {
            this.logError("Failed execute scheduled events", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }

    @Override
    public DataSet getExecutePlanItems(String connectionid) {
        String methodName = "getExecutePlanItems";
        try {
            this.startMethod(methodName, connectionid);
            ScheduleService scheduleService = new ScheduleService(this.sapphireConnection);
            DataSet dataSet = scheduleService.getExecutePlanItems(Calendar.getInstance());
            return dataSet;
        }
        catch (Exception e) {
            this.logError("Failed getting planitems", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
        }
    }
}

