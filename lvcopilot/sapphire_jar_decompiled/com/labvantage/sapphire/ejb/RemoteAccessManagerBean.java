/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.ejb.SessionBean
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.ejb.BaseAccessManager;
import com.labvantage.sapphire.ejb.ManagerException;
import com.labvantage.sapphire.ejb.RemoteAccessManagement;
import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import javax.ejb.SessionBean;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;

public class RemoteAccessManagerBean
extends BaseAccessManager
implements SessionBean,
RemoteAccessManagement {
    public RemoteAccessManagerBean() {
        this.logName = "RemoteAccessManager";
    }

    @Override
    public ActionBlock processActionBlock(String connectionid, ActionBlock actionBlock, boolean newTrans, boolean processAsync) throws ManagerException {
        try {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
            if (SecurityPolicyUtil.isActionBlockPermitted(connectionid, "remotejavaapi", "actionprocessing", actionBlock)) {
                ActionBlock actionBlock2 = super.processActionBlock(connectionid, actionBlock, newTrans, processAsync);
                return actionBlock2;
            }
            throw new ManagerException("Failed to process actionblock. Reason: Execution of one or more actions in the actionblock is not permitted by security policy.");
        }
        finally {
            Trace.clearThreadMDC();
        }
    }

    @Override
    public int execSQL(String connectionid, String sql) throws ManagerException {
        try {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
            if (SecurityPolicyUtil.isUnregisteredSQLPermitted(connectionid, "remotejavaapi", "execSQL", sql)) {
                int n = super.execSQL(connectionid, sql);
                return n;
            }
            throw new ManagerException("Failed to process execSQL. Reason: Execution of execSQL with unregistered SQL is not permitted by security policy.");
        }
        finally {
            Trace.clearThreadMDC();
        }
    }

    @Override
    public DataSet getSQLDataSet(String connectionid, String name, String sql, boolean extendedDataTypes, int queryTimeout) throws ManagerException {
        try {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
            if (SecurityPolicyUtil.isUnregisteredSQLPermitted(connectionid, "remotejavaapi", "getSQLDataSet", sql)) {
                DataSet dataSet = super.getSQLDataSet(connectionid, name, sql, extendedDataTypes, queryTimeout);
                return dataSet;
            }
            throw new ManagerException("Failed to process getSQLDataSet. Reason: Execution of getSQLDataSet with unregistered SQL is not permitted by security policy.");
        }
        finally {
            Trace.clearThreadMDC();
        }
    }

    @Override
    public DataSet getPreparedSqlDataSet(String connectionid, String name, String sql, Object[] params, boolean extendedDataTypes, int queryTimeout) throws ManagerException {
        try {
            Trace.startThreadMDCByConnectionid(connectionid, "JavaAPI");
            if (SecurityPolicyUtil.isUnregisteredSQLPermitted(connectionid, "remotejavaapi", "getPreparedSqlDataSet", sql)) {
                DataSet dataSet = super.getPreparedSqlDataSet(connectionid, name, sql, params, extendedDataTypes, queryTimeout);
                return dataSet;
            }
            throw new ManagerException("Failed to process getPreparedSqlDataSet. Reason: Execution of getPreparedSqlDataSet with unregistered SQL is not permitted by security policy.");
        }
        finally {
            Trace.clearThreadMDC();
        }
    }
}

