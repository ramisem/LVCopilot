/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.services;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.statsmonitor.BaseMonitor;
import com.labvantage.sapphire.modules.statsmonitor.MonitorConstants;
import com.labvantage.sapphire.services.BaseService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.LabVantageClassLoader;
import com.labvantage.sapphire.util.LabVantageSecurityManager;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class StatusService
extends BaseService
implements MonitorConstants {
    static final String LABVANTAGE_CVS_ID = "$Revision: 91631 $";
    public static final String LOGNAME = "StatusService";
    private static final String MONITORING_STATS_ALLACTIONS = "All Actions";
    private static final String MONITORING_STATS_PAGEREQUESTS = "Page Requests";
    private static final String MONITORING_STATS_AJAXREQUESTS = "Ajax Requests";
    private static final String MONITORING_STATS_ALLWEBREQUESTS = "All Web Requests";

    public StatusService(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
        this.logName = LOGNAME;
    }

    public DataSet getStats(int type) throws ServiceException {
        this.logInfo("Getting statistics");
        return Trace.getStats(type);
    }

    public void resetStats() throws ServiceException {
        this.logInfo("Resetting statistics");
        Trace.resetStats();
    }

    public DataSet getCacheSizes(boolean includeContents) throws ServiceException {
        this.logInfo("Getting cache sizes");
        String databaseid = this.sapphireConnection.getDatabaseId();
        DataSet ds = new DataSet();
        ds.addColumn("id", 0);
        ds.addColumn("size", 1);
        ds.addColumn("max", 1);
        Set<String> cacheNames = CacheUtil.keySet(databaseid);
        TranslationProcessor tp = new TranslationProcessor(this.getConnectionId());
        for (String cacheName : cacheNames) {
            int newrow = ds.addRow();
            ds.setString(newrow, "id", tp.translate(cacheName));
            ds.setNumber(newrow, "size", CacheUtil.getCacheSize(databaseid, cacheName));
            ds.setNumber(newrow, "max", CacheUtil.getMaxCacheSize(databaseid, cacheName));
            if (!includeContents) continue;
            DataSet contents = new DataSet();
            Set<String> keys = CacheUtil.keySet(databaseid, cacheName);
            for (String key : keys) {
                int row = contents.addRow();
                contents.setString(row, "key", key);
                Object o = CacheUtil.get(databaseid, cacheName, key);
                contents.setString(row, "value", o == null ? "[Empty]" : o.toString());
            }
            ds.setString(newrow, "contents", contents.toXML());
        }
        return ds;
    }

    public List<String> getLSMExceptions() throws ServiceException {
        this.logInfo("Getting LSM Exceptions");
        if (System.getSecurityManager() != null && System.getSecurityManager() instanceof LabVantageSecurityManager) {
            return ((LabVantageSecurityManager)System.getSecurityManager()).getErrorStack();
        }
        ArrayList<String> out = new ArrayList<String>();
        out.add("LabVantage Security Manager (LSM) Not Installed");
        return out;
    }

    public DataSet getClassLoaderStats(boolean includeContents) throws ServiceException {
        this.logInfo("Getting class loaders");
        String databaseid = this.sapphireConnection.getDatabaseId();
        DataSet ds = new DataSet();
        ds.addColumn("id", 0);
        ds.addColumn("type", 0);
        ds.addColumn("area", 0);
        ds.addColumn("loaded", 1);
        ds.addColumn("failed", 1);
        ds.addColumn("loadedcontents", 0);
        ds.addColumn("failedcontents", 0);
        ds.addColumn("classpath", 0);
        ds.addColumn("parent", 0);
        Set<String> keys = CacheUtil.keySet(databaseid, "ClassLoaders");
        if (keys != null && keys.size() > 0) {
            for (String currentClassLoader : keys) {
                int row;
                int l;
                Object cl = CacheUtil.get(databaseid, "ClassLoaders", currentClassLoader);
                if (cl == null || !(cl instanceof LabVantageClassLoader)) continue;
                int r = ds.addRow();
                LabVantageClassLoader labVantageClassLoader = (LabVantageClassLoader)cl;
                String parent = "";
                if (labVantageClassLoader.getParent() != null) {
                    parent = labVantageClassLoader.getParent() instanceof LabVantageClassLoader ? ((LabVantageClassLoader)labVantageClassLoader.getParent()).getId() : "(system)";
                }
                ds.setValue(r, "id", labVantageClassLoader.getId());
                ds.setValue(r, "type", labVantageClassLoader.getType().getTypeName());
                ds.setValue(r, "area", labVantageClassLoader.getType().getArea());
                ds.setNumber(r, "classes", labVantageClassLoader.getClassPathArray() != null ? labVantageClassLoader.getClassPathArray().size() : 0);
                ds.setNumber(r, "loaded", labVantageClassLoader.getLoadedClasses() != null ? labVantageClassLoader.getLoadedClasses().size() : 0);
                ds.setNumber(r, "failed", labVantageClassLoader.getFailedClasses() != null ? labVantageClassLoader.getFailedClasses().size() : 0);
                ds.setString(r, "parent", parent);
                if (!includeContents) continue;
                DataSet contents = new DataSet();
                ds.addColumn("class", 0);
                if (labVantageClassLoader.getLoadedClasses() != null) {
                    for (l = 0; l < labVantageClassLoader.getLoadedClasses().size(); ++l) {
                        row = contents.addRow();
                        contents.setString(row, "class", labVantageClassLoader.getLoadedClasses().get(l));
                    }
                }
                ds.setString(r, "loadedcontents", contents.toXML());
                contents = new DataSet();
                ds.addColumn("class", 0);
                if (labVantageClassLoader.getFailedClasses() != null) {
                    for (l = 0; l < labVantageClassLoader.getFailedClasses().size(); ++l) {
                        row = contents.addRow();
                        contents.setString(row, "class", labVantageClassLoader.getFailedClasses().get(l));
                    }
                }
                ds.setString(r, "failedcontents", contents.toXML());
                contents = new DataSet();
                ds.addColumn("class", 0);
                if (labVantageClassLoader.getClassPathArray() != null) {
                    for (l = 0; l < labVantageClassLoader.getClassPathArray().size(); ++l) {
                        row = contents.addRow();
                        contents.setString(row, "class", labVantageClassLoader.getClassPathArray().get(l));
                    }
                }
                ds.setString(r, "classpath", contents.toXML());
            }
        }
        return ds;
    }

    public DataSet getTableSizes() throws ServiceException {
        this.logInfo("Getting tables sizes");
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            db.setConnection(this.sapphireConnection);
            DataSet ds = new DataSet();
            ds.addColumn("tableid", 0);
            ds.addColumn("rows", 1);
            ds.addColumn("description", 0);
            TranslationProcessor tp = new TranslationProcessor(this.getConnectionId());
            String[] tableid = new String[]{"todolist", "connection", "rset", "rsetlog", "tracelog"};
            String[] description = new String[]{tp.translate("Number of queued actions in error or waiting to be processed"), tp.translate("Number of connections active or waiting to be timed-out"), tp.translate("Number of resultsets current or waiting to be timed out"), tp.translate("Number of resultsets log entries"), tp.translate("Number of trace or e-sig entries")};
            String[] tablename = new String[]{tp.translate("ToDoList"), tp.translate("Connection"), tp.translate("RSet"), tp.translate("RSetLog"), tp.translate("TraceLog")};
            for (int i = 0; i < tableid.length; ++i) {
                int newrow = ds.addRow();
                ds.setString(newrow, "tableid", tablename[i]);
                ds.setString(newrow, "description", description[i]);
                int rows = db.getCount("select count(*) from " + tableid[i]);
                ds.setNumber(newrow, "rows", rows);
            }
            DataSet dataSet = ds;
            return dataSet;
        }
        catch (SapphireException se) {
            throw new ServiceException("DB_ACTION_FAILED", "Failed to get table sizes.", se);
        }
        finally {
            db.reset();
        }
    }

    public DataSet getMemoryStats() throws ServiceException {
        this.logInfo("Getting memory statistics");
        TranslationProcessor tp = new TranslationProcessor(this.getConnectionId());
        try {
            DataSet ds = new DataSet();
            ds.addColumnValues("title", 0, tp.translate("Total Memory") + ";" + tp.translate("Free Memory") + ";" + tp.translate("Maximum Memory"), ";");
            ds.addColumnValues("description", 0, tp.translate("The total amount of memory in the Java virtual machine") + ";" + tp.translate("The amount of free memory in the Java virtual machine") + ";" + tp.translate("The maximum amount of memory the Java virtual machine will attempt to use."), ";");
            ds.addColumn("size", 1);
            Runtime r = Runtime.getRuntime();
            ds.setNumber(0, "size", r.totalMemory() / 0x100000L);
            ds.setNumber(1, "size", r.freeMemory() / 0x100000L);
            ds.setNumber(2, "size", r.maxMemory() / 0x100000L);
            return ds;
        }
        catch (Exception se) {
            throw new ServiceException(tp.translate("Failed to get memory stats."), se);
        }
    }

    public double getStatsMonitoringValue(String groupid, String itemid) throws ServiceException {
        double value = -99999.0;
        DBUtil db = new DBUtil(this.sapphireConnection.getConnectionId());
        db.setConnection(this.sapphireConnection);
        QueryProcessor qp = new QueryProcessor(this.getConnectionId());
        SafeSQL safeSQL = new SafeSQL();
        DataSet item = qp.getPreparedSqlDataSet("SELECT * FROM statsmonitoritem WHERE statsmonitorgroupid=" + safeSQL.addVar(groupid) + " AND statsmonitoritemid=" + safeSQL.addVar(itemid), safeSQL.getValues());
        if (item.size() > 0) {
            String className = item.getValue(0, "classname");
            String args = item.getValue(0, "classargs");
            try {
                Class<?> c = Class.forName(className);
                BaseMonitor monitor = (BaseMonitor)c.newInstance();
                monitor.init(this.sapphireConnection.getDatabaseId(), db);
                value = monitor.getValue(itemid, args);
            }
            catch (Exception e) {
                this.logWarn("Unable to load stats monitor details for class " + className + ". Reason: " + e.getMessage());
            }
        }
        return value;
    }
}

