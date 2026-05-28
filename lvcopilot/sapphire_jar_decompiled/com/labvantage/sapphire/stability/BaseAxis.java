/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.stability.ScheduleGrid;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;

public abstract class BaseAxis
implements Serializable {
    public ScheduleGrid grid;
    public DataSet items;
    public String idColumn;
    public String tableid;
    public String labelColumn;
    public String cellIdColumn;
    public String buttonLabel;
    public ConnectionInfo connectionInfo;
    protected final String READONLYCOLUMN = "__readonly";
    private int nextId = 0;
    ArrayList deleteItems = new ArrayList();

    void setItems(DataSet items) {
        this.items = items;
        items.setSequence("usersequence");
        for (int i = 0; i < items.size(); ++i) {
            String id = items.getString(i, this.idColumn);
            try {
                int idNumber = Integer.parseInt(id);
                if (idNumber < this.nextId) continue;
                this.nextId = idNumber + 1;
                continue;
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
    }

    public void deleteItem(int row) {
        this.deleteItems.add(this.items.getValue(row, this.idColumn));
        this.items.deleteRow(row);
        this.items.setSequence("usersequence");
    }

    public abstract void editItem(int var1, HashMap var2);

    public int addItem(int beforeRow, HashMap values) {
        int row = beforeRow == -1 || beforeRow > this.items.size() ? this.items.addRow() : this.items.addRow(beforeRow);
        this.items.setValue(row, this.idColumn, Integer.toString(this.nextId++));
        this.editItem(row, values);
        this.items.setSequence("usersequence");
        return row;
    }

    public int addItem(HashMap values) {
        return this.addItem(-1, values);
    }

    public abstract int copyItem(int var1, HashMap var2) throws SapphireException;

    public void moveItem(int pos, int steps) {
        int newpos = pos + steps;
        if (newpos < 0) {
            newpos = 0;
        }
        if (newpos >= this.items.size()) {
            newpos = this.items.size() - 1;
        }
        if (newpos < pos) {
            for (int i = newpos; i < pos; ++i) {
                this.items.setNumber(i, "usersequence", this.items.getInt(i, "usersequence") + 1);
            }
            this.items.setNumber(pos, "usersequence", newpos);
        } else if (newpos > pos) {
            for (int i = pos + 1; i <= newpos; ++i) {
                this.items.setNumber(i, "usersequence", this.items.getInt(i, "usersequence") - 1);
            }
            this.items.setNumber(pos, "usersequence", newpos);
        }
        this.items.sort("usersequence");
        this.items.setSequence("usersequence");
    }

    public String getId(int row) {
        return row < this.items.size() ? this.items.getValue(row, this.idColumn) : "";
    }

    public String getLabel(String id) {
        HashMap<String, String> find = new HashMap<String, String>();
        find.put(this.idColumn, id);
        int findRow = this.items.findRow(find);
        return findRow >= 0 ? this.items.getValue(findRow, this.labelColumn) : "";
    }

    public int findRow(String id) {
        HashMap<String, String> find = new HashMap<String, String>();
        find.put(this.idColumn, id);
        return this.items.findRow(find);
    }

    public String toString() {
        return this.buttonLabel;
    }

    public void save(DBUtil db) throws SapphireException, SQLException {
        try {
            if (this.deleteItems.size() > 0) {
                String schedPlanSDCId = "SchedulePlan";
                SDCProcessor sdcProcessor = new SDCProcessor(this.connectionInfo.getConnectionId());
                boolean isAuditTableExist = !sdcProcessor.getProperty(schedPlanSDCId, "auditedflag").equalsIgnoreCase("N");
                String name = "delete " + this.tableid + "axis";
                String sql = "DELETE FROM " + this.tableid + " WHERE scheduleplanid=?  AND " + this.idColumn + " = ?";
                PreparedStatement ps = db.prepareStatement(name, sql);
                ps.setString(1, this.grid.planid);
                StringBuffer updateAuditSql = new StringBuffer();
                if (isAuditTableExist) {
                    updateAuditSql.append("UPDATE a_" + this.tableid + " SET modby = '" + this.connectionInfo.getSysuserId() + "', modtool = 'DELETE', moddt = {ts '" + DateTimeUtil.getNowTimestamp() + "'}, tracelogid = '" + this.grid.traceLogId + "'").append(" WHERE scheduleplanid = '" + this.grid.planid + "' AND " + this.idColumn + " = ? AND tracelogid = 'DELETED' AND ").append(" auditsequence = ( SELECT max( auditsequence ) FROM a_" + this.tableid + " WHERE scheduleplanid='" + this.grid.planid + "' AND " + this.idColumn + " = ? AND tracelogid = 'DELETED')");
                }
                for (String id : this.deleteItems) {
                    ps.setString(2, id);
                    ps.execute();
                    if (!isAuditTableExist || this.grid.traceLogId == null || this.grid.traceLogId.trim().length() <= 0) continue;
                    try {
                        db.executePreparedUpdate(updateAuditSql.toString(), new Object[]{id, id});
                    }
                    catch (Exception ex) {
                        String errorMsg = ex.getMessage();
                        int errIndx = db.isOracle() ? errorMsg.indexOf("942") : errorMsg.indexOf("240");
                        if (errIndx >= 0) continue;
                        throw new SapphireException("DB_UPDATE_FAILED", ex);
                    }
                }
                db.closeStatement(name);
            }
            this.items.addColumn("__status", 0);
            for (int row = 0; row < this.items.size(); ++row) {
                if (this.items.isNull(row, "scheduleplanid")) {
                    this.items.setString(row, "__status", "I");
                    this.items.setString(row, "scheduleplanid", this.grid.planid);
                    continue;
                }
                this.items.setString(row, "__status", "U");
            }
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("__status", "I");
            DataSet dsInsert = this.items.getFilteredDataSet(filter);
            if (dsInsert != null && dsInsert.size() > 0) {
                dsInsert.setDate(-1, "createdt", DateTimeUtil.getNowCalendar());
                dsInsert.setString(-1, "createby", this.connectionInfo.getSysuserId());
                dsInsert.setString(-1, "createtool", this.connectionInfo.getTool());
                if (this.grid.traceLogId != null && this.grid.traceLogId.trim().length() > 0) {
                    dsInsert.setString(-1, "tracelogid", this.grid.traceLogId);
                }
                DataSetUtil.insert(db, dsInsert, this.tableid);
            }
            filter.put("__status", "U");
            DataSet dsUpdate = this.items.getFilteredDataSet(filter);
            if (dsUpdate != null && dsUpdate.size() > 0) {
                dsUpdate.setDate(-1, "moddt", DateTimeUtil.getNowCalendar());
                dsUpdate.setString(-1, "modby", this.connectionInfo.getSysuserId());
                dsUpdate.setString(-1, "modtool", this.connectionInfo.getTool());
                if (this.grid.traceLogId != null && this.grid.traceLogId.trim().length() > 0) {
                    dsUpdate.setString(-1, "tracelogid", this.grid.traceLogId);
                }
                DataSetUtil.update(db, dsUpdate, this.tableid, new String[]{"scheduleplanid", this.idColumn});
            }
        }
        catch (Exception e) {
            throw new SapphireException("Unable to save into " + this.tableid + ": " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
        }
    }

    public void setReadonly() {
        this.items.setValue(-1, "__readonly", "Y");
    }

    public abstract boolean isReadOnly(int var1);

    public void setConnectionInfo(ConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }
}

