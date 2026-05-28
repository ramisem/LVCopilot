/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.stability.ScheduleGrid;
import com.labvantage.sapphire.stability.ScheduleGridUtil;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class PlanWorkItemList
implements Serializable {
    public ScheduleGrid grid;
    public DataSet items = new DataSet();
    public DataSet planWorkItems = new DataSet();
    private ArrayList alDeletedItems = new ArrayList();
    private String schedulePlanId;
    private String tableId = "scheduleplanworkitem";
    public ConnectionInfo ci;
    public transient DateTimeUtil dtu;
    public transient Calendar now;

    PlanWorkItemList(ScheduleGrid grid) {
        this.grid = grid;
        this.ci = grid.connectionProcessor.getConnectionInfo(grid.getConnectionId());
        this.dtu = new DateTimeUtil(this.ci);
        this.now = this.dtu.getCalendar("Now");
    }

    public void retrieve(String planid) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select * from " + this.tableId + " where scheduleplanid = " + safeSQL.addVar(planid) + " order by usersequence";
        this.items = this.grid.queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
        ScheduleGridUtil.toggleCurrentWIVersion(this.items, true);
        this.schedulePlanId = planid;
        this.items.addColumn("_status", 0);
        this.items.setString(-1, "_status", "R");
        this.items.showData();
        String workitemids = this.items.getColumnValues("workitemid", ";");
        String workitemVersionids = this.items.getColumnValues("workitemversionid", ";");
        if (workitemids != null && workitemids.length() > 0) {
            DAMProcessor damProcessor = this.grid.damProcessor;
            String rsetId = "";
            DataSet spWorkItems = new DataSet();
            spWorkItems.addColumnValues("workitemid", 0, workitemids, ";");
            spWorkItems.addColumnValues("workitemversionid", 0, workitemVersionids, ";");
            try {
                rsetId = damProcessor.createRSet("WorkItem", workitemids, workitemVersionids, "");
            }
            catch (Exception e) {
                throw new SapphireException("");
            }
            safeSQL.reset();
            sql = "SELECT w.workitemid, w.workitemversionid, w.workitemdesc FROM workitem w, rsetitems r WHERE w.workitemid = r.keyid1 AND w.workitemversionid = r.keyid2 AND r.sdcid = 'WorkItem' AND r.rsetid = " + safeSQL.addVar(rsetId);
            this.planWorkItems = this.grid.queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (this.planWorkItems.getRowCount() < spWorkItems.getRowCount()) {
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("workitemversionid", "C");
                DataSet spCurrentWI = spWorkItems.getFilteredDataSet(filter);
                for (int c = 0; c < spCurrentWI.getRowCount(); ++c) {
                    String workItemId = spCurrentWI.getValue(c, "workitemid");
                    DataSet dsDesc = this.grid.queryProcessor.getPreparedSqlDataSet("select workitemdesc  from workitem where workitemid = ? and ( versionstatus='C' or versionstatus='P') ORDER BY versionstatus, cast ( workitemversionid as integer ) DESC ", (Object[])new String[]{workItemId});
                    if (dsDesc.getRowCount() <= 0) continue;
                    int r = this.planWorkItems.addRow();
                    this.planWorkItems.setString(r, "workitemid", workItemId);
                    this.planWorkItems.setString(r, "workitemdesc", dsDesc.getValue(0, "workitemdesc"));
                }
            }
            this.planWorkItems.showData();
        }
    }

    public void addWorkItem(String workitemId, HashMap hmWorkItemProps) {
        this.addWorkItem(workitemId, "C", hmWorkItemProps);
    }

    public void addWorkItem(String workitemId, String workitemVersionId, HashMap hmWorkItemProps) {
        int newRow = this.items.addRow();
        this.items.setValue(newRow, "scheduleplanid", this.schedulePlanId);
        this.items.setValue(newRow, "workitemid", workitemId);
        this.items.setValue(newRow, "workitemversionid", "C".equals(workitemVersionId) ? "" : workitemVersionId);
        this.setWorkItemAttributes(newRow, hmWorkItemProps);
        this.items.setString(newRow, "_status", "I");
        this.items.sort("usersequence");
        for (int i = 0; i < this.items.getRowCount(); ++i) {
            HashMap hashMap = (HashMap)this.items.get(i);
        }
        this.items.showData();
        HashMap<String, String> findMap = new HashMap<String, String>();
        findMap.put("workitemid", workitemId);
        findMap.put("workitemversionid", workitemVersionId);
        if (this.planWorkItems.findRow(findMap) < 0) {
            if (this.planWorkItems.size() == 0) {
                this.planWorkItems.addColumn("workitemid", 0);
                this.planWorkItems.addColumn("workitemversionid", 0);
                this.planWorkItems.addColumn("workitemdesc", 0);
            }
            newRow = this.planWorkItems.addRow();
            this.planWorkItems.setValue(newRow, "workitemid", workitemId);
            this.planWorkItems.setValue(newRow, "workitemversionid", workitemVersionId);
            this.planWorkItems.setValue(newRow, "workitemdesc", hmWorkItemProps.get("workitemdesc") == null ? "" : (String)hmWorkItemProps.get("workitemdesc"));
        }
    }

    public void editWorkItem(String workitemId, HashMap hmWorkItemProps) {
        this.editWorkItem(workitemId, "1", hmWorkItemProps);
    }

    public void editWorkItem(String workitemId, String workitemVersionId, HashMap hmWorkItemProps) {
        int row = this.getWorkItemRow(workitemId, workitemVersionId);
        if (row >= 0) {
            this.setWorkItemAttributes(row, hmWorkItemProps);
            String status = this.items.getValue(row, "_status", "");
            if (status.equals("R")) {
                this.items.setString(row, "_status", "U");
            }
        }
        this.items.sort("usersequence");
        for (int i = 0; i < this.items.getRowCount(); ++i) {
            HashMap hashMap = (HashMap)this.items.get(i);
        }
        this.items.showData();
    }

    public void deleteWorkItem(String workitemId) {
        this.deleteWorkItem(workitemId, "1");
    }

    public void deleteWorkItem(String workitemId, String workitemVersionId) {
        int row = this.getWorkItemRow(workitemId, workitemVersionId);
        if (row >= 0) {
            this.items.deleteRow(row);
            this.alDeletedItems.add(workitemId + ";" + workitemVersionId);
        }
        this.items.showData();
    }

    private int getWorkItemRow(String workItemId, String workitemVersionId) {
        int findRow;
        if (workitemVersionId == null || workitemVersionId.length() == 0) {
            findRow = this.items.findRow("workitemid", workItemId);
        } else if ("C".equals(workitemVersionId)) {
            HashMap<String, String> findMap = new HashMap<String, String>();
            findMap.put("workitemid", workItemId);
            findMap.put("workitemversionid", "C");
            findRow = this.items.findRow(findMap);
        } else {
            HashMap<String, String> findMap = new HashMap<String, String>();
            findMap.put("workitemid", workItemId);
            findMap.put("workitemversionid", workitemVersionId);
            findRow = this.items.findRow(findMap);
        }
        return findRow;
    }

    private void setWorkItemAttributes(int row, HashMap hmWorkItemProps) {
        for (int i = 0; i < this.items.getColumnCount(); ++i) {
            String value;
            String columnid = this.items.getColumnId(i);
            if (!hmWorkItemProps.containsKey(columnid)) continue;
            String string = value = hmWorkItemProps.get(columnid) == null ? "" : (String)hmWorkItemProps.get(columnid);
            if (columnid.equals("quantityunit")) {
                if (value.equals("(Containers)")) {
                    this.items.setValue(row, "quantitytype", "C");
                    value = "";
                } else {
                    this.items.setValue(row, "quantitytype", "U");
                }
            }
            this.items.setValue(row, columnid, value);
        }
    }

    public void save(DBUtil database) throws SapphireException, SQLException {
        block8: {
            try {
                Statement psAudit;
                block9: {
                    this.ci = this.grid.connectionProcessor.getConnectionInfo(this.grid.getConnectionId());
                    this.dtu = new DateTimeUtil(this.ci);
                    this.now = this.dtu.getCalendar("Now");
                    ScheduleGridUtil.toggleCurrentWIVersion(this.items, false);
                    ArrayList<DataSet> alDatasets = this.items.getGroupedDataSets("_status");
                    for (int i = 0; i < alDatasets.size(); ++i) {
                        DataSet ds = alDatasets.get(i);
                        String status = ds.getValue(0, "_status", "R");
                        if (status.equals("I")) {
                            this.setAuditColumns("Add", ds);
                            DataSetUtil.insert(database, ds, this.tableId);
                            continue;
                        }
                        if (!status.equals("U")) continue;
                        this.setAuditColumns("Edit", ds);
                        DataSetUtil.update(database, ds, this.tableId, new String[]{"scheduleplanid", "workitemid"});
                    }
                    if (this.alDeletedItems == null || this.alDeletedItems.size() <= 0) break block8;
                    String psName = "deleteplanworkitems";
                    String sql = "delete from " + this.tableId + " where scheduleplanid=? AND workitemid=?";
                    PreparedStatement ps = database.prepareStatement(psName, sql);
                    ps.setString(1, this.schedulePlanId);
                    String schedPlanSDCId = "SchedulePlan";
                    boolean isAuditTableExist = !this.grid.sdcProcessor.getProperty(schedPlanSDCId, "auditedflag").equalsIgnoreCase("N");
                    StringBuffer updateAuditSql = new StringBuffer();
                    psAudit = null;
                    if (isAuditTableExist) {
                        updateAuditSql.append("UPDATE a_" + this.tableId + " SET modby = ?, modtool = 'DELETE', moddt = ?, tracelogid = ? ").append(" WHERE scheduleplanid = ? AND workitemid = ? AND tracelogid = 'DELETED' ").append(" AND auditsequence = ( SELECT max( auditsequence ) FROM a_" + this.tableId + " WHERE scheduleplanid=? AND workitemid = ? AND tracelogid = 'DELETED')");
                        psAudit = database.prepareStatement("audit" + psName, updateAuditSql.toString());
                        psAudit.setString(1, this.grid.connectionProcessor.getSapphireConnection().getSysuserId());
                        psAudit.setTimestamp(2, DateTimeUtil.getNowTimestamp());
                        psAudit.setString(3, this.grid.traceLogId);
                        psAudit.setString(4, this.schedulePlanId);
                        psAudit.setString(6, this.schedulePlanId);
                    }
                    for (String widWithVersion : this.alDeletedItems) {
                        String wid = StringUtil.split(widWithVersion, ";")[0];
                        ps.setString(2, wid);
                        ps.addBatch();
                        if (!isAuditTableExist || this.grid.traceLogId == null || this.grid.traceLogId.trim().length() <= 0) continue;
                        psAudit.setString(5, wid);
                        psAudit.setString(7, wid);
                        psAudit.addBatch();
                    }
                    ps.executeBatch();
                    database.closeStatement(ps);
                    if (!isAuditTableExist || this.grid.traceLogId == null || this.grid.traceLogId.trim().length() <= 0) break block8;
                    try {
                        psAudit.executeBatch();
                    }
                    catch (SQLException ex) {
                        int errorCode = ex.getErrorCode();
                        if (errorCode == (database.isOracle() ? 942 : 240)) break block9;
                        throw new SapphireException("DB_UPDATE_FAILED", ex);
                    }
                }
                database.closeStatement(psAudit);
            }
            catch (Exception e) {
                throw new SapphireException("Unable to save: " + e.getMessage());
            }
        }
    }

    private void setAuditColumns(String mode, DataSet ds) {
        if (mode.equalsIgnoreCase("Add")) {
            ds.setString(-1, "createtool", this.ci.getTool());
            ds.setString(-1, "createby", this.ci.getSysuserId());
            ds.setDate(-1, "createdt", this.now);
        }
        ds.setString(-1, "modby", this.ci.getSysuserId());
        ds.setDate(-1, "moddt", this.now);
        ds.setString(-1, "modtool", this.ci.getTool());
        if (this.grid.traceLogId != null && this.grid.traceLogId.trim().length() > 0) {
            ds.setString(-1, "tracelogid", this.grid.traceLogId);
        }
    }

    public DataSet getColumnEditors() {
        DataSet ds = new DataSet();
        int newRow = ds.addRow();
        ds.setString(newRow, "quantity", "text|3");
        ds.setString(newRow, "quantityunit", "dropdown");
        ds.setString(newRow, "numrepeats", "text|3");
        ds.setString(newRow, "containerperrepeatflag", "checkbox");
        ds.setString(newRow, "containerpertestflag", "checkbox");
        ds.setString(newRow, "destructivetestflag", "checkbox");
        ds.setString(newRow, "reusecontainerflag", "checkbox||checkRepeatPerContainerFlag(this)");
        ds.setString(newRow, "contingentflag", "checkbox");
        ds.setString(newRow, "departmentid", "lookup|15");
        ds.setString(newRow, "duedtoffset", "text|3");
        ds.setString(newRow, "duedtoffsettimeunit", "dropdown");
        ds.setString(newRow, "workitemversionid", "");
        return ds;
    }

    public void copyAll(DataSet newItems) {
        for (int i = 0; i < newItems.size(); ++i) {
            int row = this.items.addRow();
            for (int j = 0; j < newItems.getColumnCount(); ++j) {
                String columnid = newItems.getColumnId(j);
                this.items.setObject(row, columnid, newItems.getObject(i, columnid));
            }
            this.items.setValue(row, "scheduleplanid", this.schedulePlanId);
            this.items.setValue(row, "_status", "I");
        }
    }
}

