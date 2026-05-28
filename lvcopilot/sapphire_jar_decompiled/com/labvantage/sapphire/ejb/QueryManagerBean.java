/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.ejb.EJBException
 *  javax.ejb.SessionBean
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.ejb.BaseManager;
import com.labvantage.sapphire.ejb.ManagerException;
import com.labvantage.sapphire.ejb.QueryManagement;
import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.services.QueryService;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.logger.LogUtil;
import java.util.Arrays;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;

public class QueryManagerBean
extends BaseManager
implements SessionBean,
QueryManagement {
    public QueryManagerBean() {
        this.logName = "QueryManager";
    }

    @Override
    public DataSet getSQLDataSet(String connectionid, String name, String sql, boolean extendedDataTypes, int queryTimeout) throws ManagerException {
        String methodName = "getSQLDataSet";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, name + ";" + sql);
            this.startMethod(methodName, connectionid);
            QueryService queryService = new QueryService(this.sapphireConnection);
            DataSet dataSet = queryService.getSqlDataSet(name, sql, extendedDataTypes, queryTimeout);
            return dataSet;
        }
        catch (Exception e) {
            this.logError("Failed to get SQL DataSet", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public DataSet getSQLDataSet(String connectionid, String name, String sql, boolean extendedDataTypes, int queryTimeout, boolean keepAlive) throws ManagerException {
        String methodName = "getSQLDataSet";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, name + ";" + sql);
            this.startMethod(methodName, connectionid, keepAlive);
            QueryService queryService = new QueryService(this.sapphireConnection);
            DataSet dataSet = queryService.getSqlDataSet(name, sql, extendedDataTypes, queryTimeout);
            return dataSet;
        }
        catch (Exception e) {
            this.logError("Failed to get SQL DataSet", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public DataSet getSQLDataSet(String connectionid, int sqlcode, Object[] bindVars, boolean extendedDataTypes) throws ManagerException {
        String methodName = "getSQLDataSet";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, sqlcode + ";" + Arrays.toString(bindVars));
            this.startMethod(methodName, connectionid);
            QueryService queryService = new QueryService(this.sapphireConnection);
            DataSet dataSet = queryService.getSqlDataSet(sqlcode, bindVars, extendedDataTypes);
            return dataSet;
        }
        catch (Exception e) {
            this.logError("Failed to get SQL DataSet", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public DataSet getPreparedSqlDataSet(String connectionid, String name, String sql, Object[] params, boolean extendedDataTypes, int queryTimeout) throws ManagerException {
        String methodName = "getSQLDataSet";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, name + ";" + sql + ";" + Arrays.toString(params));
            this.startMethod(methodName, connectionid);
            QueryService queryService = new QueryService(this.sapphireConnection);
            DataSet dataSet = queryService.getPreparedSqlDataSet(name, sql, params, extendedDataTypes, queryTimeout);
            return dataSet;
        }
        catch (Exception e) {
            this.logError("Failed to get SQL DataSet", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public DataSet getPreparedSqlDataSet(String connectionid, int sqlCode, Object[] params, boolean extendedDataTypes) throws ManagerException {
        String methodName = "getSQLDataSet";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, sqlCode + ";" + Arrays.toString(params));
            this.startMethod(methodName, connectionid);
            QueryService queryService = new QueryService(this.sapphireConnection);
            DataSet dataSet = queryService.getPreparedSqlDataSet(sqlCode, params, extendedDataTypes);
            return dataSet;
        }
        catch (Exception e) {
            this.logError("Failed to get SQL DataSet", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public DataSet getRefTypeDataSet(String connectionid, String reftypeid) throws ManagerException {
        String methodName = "getRefTypeDataSet";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, reftypeid);
            this.startMethod(methodName, connectionid);
            QueryService queryService = new QueryService(this.sapphireConnection);
            DataSet dataSet = queryService.getRefTypeDataSet(reftypeid);
            return dataSet;
        }
        catch (Exception e) {
            this.logError("Failed to get DataSet for reftype '" + reftypeid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public int execPreparedUpdate(String connectionid, String sql, Object[] bindVars) throws ManagerException {
        String methodName = "execPreparedUpdate";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, sql);
            this.startMethod(methodName, connectionid);
            QueryService queryService = new QueryService(this.sapphireConnection);
            int n = queryService.execPreparedUpdate(sql, bindVars);
            return n;
        }
        catch (Exception e) {
            this.logError("Failed to execute SQL", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public int execSQL(String connectionid, String sql) throws ManagerException {
        String methodName = "executeSQL";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, sql);
            this.startMethod(methodName, connectionid);
            QueryService queryService = new QueryService(this.sapphireConnection);
            int n = queryService.execSQL(sql);
            return n;
        }
        catch (Exception e) {
            this.logError("Failed to execute SQL", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public int execSQL(String connectionid, int sqlCode, Object[] bindVars) throws ManagerException {
        String methodName = "executeSQL";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, sqlCode + ";" + Arrays.toString(bindVars));
            this.startMethod(methodName, connectionid);
            QueryService queryService = new QueryService(this.sapphireConnection);
            int n = queryService.execSQL(sqlCode, bindVars);
            return n;
        }
        catch (Exception e) {
            this.logError("Failed to execute SQL", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public String getQueryKeyid1List(String connectionid, String sdcid, String queryid, String[] params) throws ManagerException {
        String methodName = "getQueryKeyid1List";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, sdcid + ";" + queryid + ";" + Arrays.toString(params));
            this.startMethod(methodName, connectionid);
            QueryService queryService = new QueryService(this.sapphireConnection);
            String string = queryService.getKeyid1List(sdcid, queryid, params);
            return string;
        }
        catch (Exception e) {
            this.logError("Failed to get keyid1 list for sdcid '" + sdcid + "', query '" + queryid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public SDIData getSDIData(String connectionid, SDIRequest sdiRequest) throws ManagerException {
        String methodName = "getSDIData";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, sdiRequest.toString());
            this.startMethod(methodName, connectionid);
            QueryService queryService = new QueryService(this.sapphireConnection);
            SDIData sDIData = queryService.getSDIData(sdiRequest);
            return sDIData;
        }
        catch (Exception e) {
            this.logError("Failed to get SDI data", e);
            this.beforeTransactionAbort();
            if (LogUtil.getStackTraceMessages(e, "<br/>", true, true).contains("ORA-20470")) {
                ConfigService config = new ConfigService(this.sapphireConnection);
                String limit = "";
                try {
                    limit = config.getSysConfigProperty("RSetQueryLimit");
                }
                catch (ServiceException serviceException) {
                    // empty catch block
                }
                throw new EJBException("Global query limit " + (limit.length() > 0 ? "(" + limit + ") " : " ") + "exceeded - try performing a more specific search.");
            }
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public int getSDICount(String connectionid, SDIRequest sdiRequest) throws ManagerException {
        String methodName = "getSDICount";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, sdiRequest.toString());
            this.startMethod(methodName, connectionid);
            QueryService queryService = new QueryService(this.sapphireConnection);
            int n = queryService.getSDICount(sdiRequest);
            return n;
        }
        catch (Exception e) {
            this.logError("Failed to get SDI count", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public int getSDICount(String connectionid, SDIRequest sdiRequest, boolean keepAlive) throws ManagerException {
        String methodName = "getSDICount";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, sdiRequest.toString());
            this.startMethod(methodName, connectionid, keepAlive);
            QueryService queryService = new QueryService(this.sapphireConnection);
            int n = queryService.getSDICount(sdiRequest);
            return n;
        }
        catch (Exception e) {
            this.logError("Failed to get SDI count", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public String getSecurityFilterWhere(String connectionid, String sdcid) throws ManagerException {
        String methodName = "getSecurityFilterWhere";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, sdcid);
            this.startMethod(methodName, connectionid);
            QueryService queryService = new QueryService(this.sapphireConnection);
            String string = queryService.getSecurityFilterWhere(sdcid);
            return string;
        }
        catch (Exception e) {
            this.logError("Failed to get Security filter where clause for SDC '" + sdcid + "'", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }
}

