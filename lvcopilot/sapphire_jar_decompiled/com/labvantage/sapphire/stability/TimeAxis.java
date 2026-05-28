/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability;

import com.labvantage.sapphire.scheduler.ScheduleRule;
import com.labvantage.sapphire.stability.BaseAxis;
import com.labvantage.sapphire.stability.PlanItem;
import com.labvantage.sapphire.stability.ScheduleGrid;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import sapphire.SapphireException;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class TimeAxis
extends BaseAxis {
    TimeAxis(ScheduleGrid grid) {
        this.grid = grid;
        this.idColumn = "scheduletimeruleid";
        this.tableid = "scheduletimerule";
        this.labelColumn = "schedulerulelabel";
        this.cellIdColumn = "scheduletimeruleid";
        this.buttonLabel = "Time Point";
        this.connectionInfo = grid.connectionProcessor.getConnectionInfo(grid.getConnectionId());
    }

    public int findOrAddScheduleRule(String scheduleRule, String scheduleRuleLabel) {
        if (scheduleRule != null) {
            HashMap<String, String> find = new HashMap<String, String>();
            find.put("schedulerule", scheduleRule);
            int findRow = this.items.findRow(find);
            if (findRow == -1) {
                HashMap<String, String> values = new HashMap<String, String>();
                values.put("schedulerule", scheduleRule);
                values.put("schedulerulelabel", scheduleRuleLabel);
                return super.addItem(values);
            }
            return findRow;
        }
        return -1;
    }

    public void renameTimeruleid(String fromid, String newid) {
        int row = this.findRow(fromid);
        this.items.setValue(row, this.idColumn, newid);
        ArrayList planItems = this.grid.planItems.findByTime(fromid);
        for (PlanItem item : planItems) {
            item.timeruleid = newid;
        }
    }

    public void retrieve(String planid) {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select * from scheduletimerule where scheduleplanid = " + safeSQL.addVar(planid) + " order by usersequence";
        this.setItems(this.grid.queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues()));
        this.items.addColumn("__readonly", 0);
    }

    @Override
    public void editItem(int row, HashMap values) {
        for (int i = 0; i < this.items.getColumnCount(); ++i) {
            String columnid = this.items.getColumnId(i);
            if (!values.containsKey(columnid)) continue;
            this.items.setValue(row, columnid, values.get(columnid) == null ? "" : (String)values.get(columnid));
        }
    }

    @Override
    public int copyItem(int row, HashMap values) throws SapphireException {
        String fromid = this.items.getValue(row, this.idColumn);
        String executeahead = this.items.getValue(row, "executeahead");
        String executeaheadtimeUnits = this.items.getValue(row, "executeaheadunits");
        if (executeahead.length() > 0 && executeaheadtimeUnits.length() > 0) {
            values.put("executeahead", executeahead);
            values.put("executeaheadunits", executeaheadtimeUnits);
        }
        int newrow = this.addItem(row + 1, values);
        String toid = this.items.getValue(newrow, this.idColumn);
        this.grid.planItems.copyTimeItems(fromid, toid);
        return newrow;
    }

    @Override
    public boolean isReadOnly(int row) {
        String readonly = this.items.getValue(row, "__readonly");
        if (readonly.equals("")) {
            readonly = "N";
            String timeruleid = this.items.getValue(row, this.idColumn);
            ArrayList planitems = this.grid.planItems.findByTime(timeruleid);
            Iterator it = planitems.iterator();
            while (it.hasNext() && readonly.equals("N")) {
                PlanItem planItem = (PlanItem)it.next();
                if (!planItem.readonly) continue;
                readonly = "Y";
            }
            this.items.setObject(row, "__readonly", readonly);
        }
        return readonly.equals("Y");
    }

    @Override
    public void deleteItem(int pos) {
        String itemid = this.items.getValue(pos, this.idColumn);
        this.grid.planItems.deleteTimeItems(itemid);
        super.deleteItem(pos);
    }

    public void sort() throws SapphireException {
        String[] values = StringUtil.split(this.items.getColumnValues("schedulerule", ";"), ";");
        for (int i = 0; i < values.length; ++i) {
            values[i] = values[i] + " @ 00:01";
        }
        Calendar start = Calendar.getInstance();
        start.set(11, 0);
        start.set(12, 1);
        int[] sequence = ScheduleRule.getRulesSequence(values, start);
        for (int i = 0; i < this.items.size(); ++i) {
            this.items.setNumber(sequence[i], "usersequence", i);
        }
        this.items.sort("usersequence");
    }
}

